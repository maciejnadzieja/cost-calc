#Requires -Version 5.1
# Command-boundary tests. No Docker daemon, image builds or service changes are used.
$ErrorActionPreference = 'Stop'
$sourceScript = Join-Path (Split-Path -Parent $PSScriptRoot) 'rebuild-local.ps1'
$testRoot = Join-Path ([IO.Path]::GetTempPath()) ('cost calc rebuild tests ' + [guid]::NewGuid())
$fixtureRepository = Join-Path $testRoot 'repository with spaces'
$callerDirectory = Join-Path $testRoot 'separate caller directory'
$originalLocation = Get-Location
$originalDocker = Get-Item Function:\docker -ErrorAction SilentlyContinue
$global:rebuildTestCalls = [Collections.Generic.List[object]]::new()
$global:rebuildTestFailure = ''

function Assert-True {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw $Message }
}

function global:docker {
    $commandArguments = @($args)
    $global:rebuildTestCalls.Add($commandArguments)
    $command = if ($commandArguments[0] -eq 'info') { 'info' }
        elseif ($commandArguments[1] -eq 'version') { 'version' }
        else { $commandArguments[5] }
    $global:LASTEXITCODE = if ($command -eq $global:rebuildTestFailure) { 23 } else { 0 }
}
$dockerStub = ${function:global:docker}

function Invoke-Scenario {
    param([hashtable] $Parameters = @{}, [string] $FailOn = '', [bool] $ExpectFailure = $false)
    $global:rebuildTestCalls.Clear()
    $global:rebuildTestFailure = $FailOn
    $failed = $false
    try { & $testScript @Parameters *>&1 | Out-Null }
    catch { $failed = $true }
    Assert-True ($failed -eq $ExpectFailure) "Unexpected failure result for '$FailOn'."
    Assert-True ((Get-Location).Path -eq $callerDirectory) 'Script changed the caller working directory.'
}

try {
    New-Item -ItemType Directory -Path (Join-Path $fixtureRepository 'scripts'), $callerDirectory -Force | Out-Null
    $testScript = Join-Path $fixtureRepository 'scripts/rebuild-local.ps1'
    Copy-Item -LiteralPath $sourceScript -Destination $testScript
    $testCompose = Join-Path $fixtureRepository 'docker-compose.yml'
    Set-Content -LiteralPath $testCompose -Value 'services: {}'
    Set-Location -LiteralPath $callerDirectory

    Invoke-Scenario -Parameters @{ WaitTimeoutSeconds = 42 }
    $calls = $global:rebuildTestCalls
    Assert-True ($calls.Count -eq 6) 'Expected version, info, config, build, up and ps.'
    Assert-True ($calls[0][1] -eq 'version' -and $calls[1][0] -eq 'info') 'Preflight order differs.'
    foreach ($call in @($calls[2], $calls[3], $calls[4], $calls[5])) {
        Assert-True ($call[1] -eq '--project-directory' -and $call[2] -eq $fixtureRepository) 'Wrong project directory.'
        Assert-True ($call[3] -eq '--file' -and $call[4] -eq $testCompose) 'Wrong Compose path.'
    }
    Assert-True (($calls[3][5..7] -join ',') -eq 'build,api,frontend') 'Both images must build first.'
    Assert-True (($calls[4][5..13] -join ',') -eq 'up,--detach,--no-build,--force-recreate,--wait,--wait-timeout,42,api,frontend') 'Unexpected restart command.'
    Assert-True ($calls[5][5] -eq 'ps') 'Missing final status.'

    Invoke-Scenario -Parameters @{ BuildOnly = $true }
    Assert-True ($global:rebuildTestCalls.Count -eq 4) 'BuildOnly must stop after the build.'
    Invoke-Scenario -Parameters @{ WhatIf = $true }
    Assert-True ($global:rebuildTestCalls.Count -eq 0) 'WhatIf must not invoke Docker.'

    $expectedCounts = @{ version = 1; info = 2; config = 3; build = 4; up = 5; ps = 6 }
    foreach ($failure in @('version', 'info', 'config', 'build', 'up', 'ps')) {
        Invoke-Scenario -FailOn $failure -ExpectFailure $true
        Assert-True ($global:rebuildTestCalls.Count -eq $expectedCounts[$failure]) "Continued after $failure failed."
    }
    Invoke-Scenario -Parameters @{ WaitTimeoutSeconds = 0 } -ExpectFailure $true
    Assert-True ($global:rebuildTestCalls.Count -eq 0) 'Invalid timeout must fail before Docker.'

    $originalPath = $env:PATH
    try {
        Remove-Item Function:\docker
        $env:PATH = ''
        Invoke-Scenario -ExpectFailure $true
        Assert-True ($global:rebuildTestCalls.Count -eq 0) 'Missing Docker must fail before any command.'
    }
    finally {
        $env:PATH = $originalPath
        $function:global:docker = $dockerStub
    }

    Remove-Item -LiteralPath $testCompose
    Invoke-Scenario -ExpectFailure $true
    Assert-True ($global:rebuildTestCalls.Count -eq 0) 'Missing Compose file must fail before Docker.'
    Write-Host 'Passed: command sequence, space-containing paths, non-script working directory, BuildOnly, WhatIf, six failure boundaries, invalid timeout, missing Docker and missing Compose file.'
}
finally {
    Set-Location -LiteralPath $originalLocation.Path
    if ($null -eq $originalDocker) { Remove-Item Function:\docker -ErrorAction SilentlyContinue }
    else { $function:global:docker = $originalDocker.ScriptBlock }
    Remove-Variable rebuildTestCalls, rebuildTestFailure -Scope Global -ErrorAction SilentlyContinue
    # Resolve and validate this exact temporary subtree before recursive cleanup.
    $resolvedRoot = [IO.Path]::GetFullPath($testRoot)
    $tempParent = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if ($resolvedRoot.StartsWith($tempParent, [StringComparison]::OrdinalIgnoreCase) -and
        (Split-Path -Leaf $resolvedRoot).StartsWith('cost calc rebuild tests ')) {
        Remove-Item -LiteralPath $resolvedRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
