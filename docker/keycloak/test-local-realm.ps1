param(
    [string]$DirectoryClientId = 'cost-calc-directory',
    [string]$DirectoryClientSecret = 'cost-calc-local-directory-secret'
)

$ErrorActionPreference = 'Stop'
$testContainer = 'cost-calc-realm-test-' + [guid]::NewGuid().ToString('N')
$realmFile = Join-Path $PSScriptRoot 'realm-config.json'
$testStarted = $false

try {
    # Unique container, ephemeral database, random localhost port: no running stack changes.
    docker run --detach --name $testContainer --publish '127.0.0.1::8080' `
        --env 'KC_BOOTSTRAP_ADMIN_USERNAME=admin' --env 'KC_BOOTSTRAP_ADMIN_PASSWORD=admin' `
        --env "KEYCLOAK_DIRECTORY_CLIENT_ID=$DirectoryClientId" `
        --env "KEYCLOAK_DIRECTORY_CLIENT_SECRET=$DirectoryClientSecret" `
        --mount "type=bind,source=$realmFile,target=/opt/keycloak/data/import/cost-calc-realm-realm.json,readonly" `
        quay.io/keycloak/keycloak:26.4.0 start-dev --import-realm | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not start isolated Keycloak.' }
    $testStarted = $true

    $testPort = docker port $testContainer 8080/tcp
    if ($LASTEXITCODE -ne 0) { throw 'Could not resolve isolated Keycloak port.' }
    $testBase = 'http://' + $testPort.Trim()
    $realmBase = "$testBase/realms/cost-calc-realm"
    $ready = $false
    $deadline = (Get-Date).AddMinutes(3)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-RestMethod "$realmBase/.well-known/openid-configuration" -TimeoutSec 3 | Out-Null
            $ready = $true
            break
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    if (-not $ready) { throw 'Isolated Keycloak realm did not become ready.' }

    $composeFile = Join-Path $PSScriptRoot '../../docker-compose.yml'
    $compose = docker compose -f $composeFile config --format json | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0) { throw 'Compose configuration is invalid.' }
    $healthCommand = $compose.services.keycloak.healthcheck.test[3]
    docker exec $testContainer bash -c $healthCommand
    if ($LASTEXITCODE -ne 0) { throw 'Compose realm health check failed.' }
    if ($compose.services.api.environment.KEYCLOAK_DIRECTORY_CLIENT_SECRET -ne
        $compose.services.keycloak.environment.KEYCLOAK_DIRECTORY_CLIENT_SECRET -or
        $compose.services.api.environment.KEYCLOAK_DIRECTORY_CLIENT_ID -ne
        $compose.services.keycloak.environment.KEYCLOAK_DIRECTORY_CLIENT_ID) {
        throw 'Compose API and Keycloak directory configuration differ.'
    }

    $directoryToken = Invoke-RestMethod "$realmBase/protocol/openid-connect/token" -Method Post `
        -Body @{ grant_type = 'client_credentials'; client_id = $DirectoryClientId; client_secret = $DirectoryClientSecret }
    $directoryHeaders = @{ Authorization = 'Bearer ' + $directoryToken.access_token }
    $users = @(Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users?first=0&max=100&briefRepresentation=false" -Headers $directoryHeaders)
    if ($users.Count -ne 1 -or $users[0].username -ne 'tester@cost-calc.eu') {
        throw 'Directory read did not return exactly the imported human tester.'
    }

    # A read-only client must not be able to create a user.
    $writeDenied = $false
    try {
        Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users" -Headers $directoryHeaders `
            -Method Post -ContentType 'application/json' -Body '{"username":"must-not-be-created"}' | Out-Null
    } catch {
        $writeDenied = [int]$_.Exception.Response.StatusCode -eq 403
    }
    if (-not $writeDenied) { throw 'Directory client user creation was not rejected with HTTP 403.' }

    $adminToken = Invoke-RestMethod "$testBase/realms/master/protocol/openid-connect/token" -Method Post `
        -Body @{ grant_type = 'password'; client_id = 'admin-cli'; username = 'admin'; password = 'admin' }
    $adminHeaders = @{ Authorization = 'Bearer ' + $adminToken.access_token }
    $roles = @(Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users/$($users[0].id)/role-mappings/realm" -Headers $adminHeaders)
    if ($roles.name -notcontains 'ADMIN' -or $roles.name -notcontains 'USER') {
        throw 'Tester is missing ADMIN or USER.'
    }
    Write-Output 'PASS: realm import, Compose readiness and matching credentials, directory token, human user read, tester roles, and write denial.'
} finally {
    if ($testStarted) {
        docker rm --force $testContainer | Out-Null
        if ($LASTEXITCODE -ne 0) { Write-Warning "Could not remove isolated test container $testContainer" }
    }
}
