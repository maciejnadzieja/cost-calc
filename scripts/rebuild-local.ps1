#Requires -Version 5.1
<#
.SYNOPSIS
Build the application images, then recreate the local Compose application services.
.EXAMPLE
./scripts/rebuild-local.ps1
.EXAMPLE
./scripts/rebuild-local.ps1 -BuildOnly
.EXAMPLE
./scripts/rebuild-local.ps1 -WhatIf
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [switch] $BuildOnly,

    [ValidateRange(1, 3600)]
    [int] $WaitTimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $repositoryRoot 'docker-compose.yml'
$composeArguments = @('compose', '--project-directory', $repositoryRoot, '--file', $composeFile)

function Invoke-Docker {
    param([string[]] $DockerArguments)

    & docker @DockerArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed (exit $LASTEXITCODE): docker $($DockerArguments -join ' ')"
    }
}

$operation = 'Build API and frontend Docker images'
if (-not $BuildOnly) {
    $operation += ' and recreate the local API/frontend containers'
}
if (-not $PSCmdlet.ShouldProcess($repositoryRoot, $operation)) {
    return
}

if (-not (Test-Path -LiteralPath $composeFile -PathType Leaf)) {
    throw "Compose file not found: $composeFile"
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker CLI is required. Install Docker Desktop with Docker Compose v2.'
}

Write-Host 'Checking Docker Compose, the Docker daemon, and the Compose configuration...'
Invoke-Docker -DockerArguments @('compose', 'version')
Invoke-Docker -DockerArguments @('info', '--format', '{{.ServerVersion}}')
Invoke-Docker -DockerArguments ($composeArguments + @('config', '--quiet'))

Write-Host 'Building backend and frontend images before changing running containers...'
Invoke-Docker -DockerArguments ($composeArguments + @('build', 'api', 'frontend'))

if ($BuildOnly) {
    Write-Host 'Images built. Running containers were not changed.'
    return
}

Write-Host 'Recreating the application containers and waiting for Compose running/healthy state...'
Invoke-Docker -DockerArguments ($composeArguments + @(
    'up', '--detach', '--no-build', '--force-recreate', '--wait',
    '--wait-timeout', [string] $WaitTimeoutSeconds, 'api', 'frontend'
))
Invoke-Docker -DockerArguments ($composeArguments + @('ps'))
Write-Host 'Compose startup completed. Frontend: http://localhost:5173; API: http://localhost:8888'
