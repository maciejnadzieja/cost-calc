param(
    [string]$DirectoryClientId = 'cost-calc-directory',
    [string]$DirectoryClientSecret = 'cost-calc-local-directory-secret'
)

$ErrorActionPreference = 'Stop'
$testContainer = 'cost-calc-realm-test-' + [guid]::NewGuid().ToString('N')
$realmFile = Join-Path $PSScriptRoot 'realm-config.json'
$testStarted = $false
$testDataRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../data'))
$testDataDir = Join-Path $testDataRoot $testContainer

function Start-IsolatedKeycloak {
    # Unique container and data directory, random localhost port: no running stack changes.
    docker run --detach --name $testContainer --publish '127.0.0.1::8080' `
        --env 'KC_BOOTSTRAP_ADMIN_USERNAME=admin' --env 'KC_BOOTSTRAP_ADMIN_PASSWORD=admin' `
        --env "KEYCLOAK_DIRECTORY_CLIENT_ID=$DirectoryClientId" `
        --env "KEYCLOAK_DIRECTORY_CLIENT_SECRET=$DirectoryClientSecret" `
        --mount "type=bind,source=$realmFile,target=/opt/keycloak/data/import/cost-calc-realm-realm.json,readonly" `
        --mount "type=bind,source=$testDataDir,target=/opt/keycloak/data/h2" `
        quay.io/keycloak/keycloak:26.4.0 start-dev --import-realm | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not start isolated Keycloak.' }
    $script:testStarted = $true

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
    return $testBase
}

try {
    New-Item -ItemType Directory -Path $testDataDir | Out-Null
    $testBase = Start-IsolatedKeycloak
    $realmBase = "$testBase/realms/cost-calc-realm"

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
    $users = Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users?first=0&max=100&briefRepresentation=false" -Headers $directoryHeaders
    $users = @($users)
    if ($users.Count -ne 2 -or $users.username -notcontains 'tester@cost-calc.eu' -or
        $users.username -notcontains 'admin@cost-calc.eu') {
        throw 'Directory read did not return exactly the two imported human accounts.'
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
    foreach ($fixture in $users) {
        $roles = Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users/$($fixture.id)/role-mappings/realm" -Headers $adminHeaders
        if ($fixture.username -eq 'tester@cost-calc.eu') {
            if ($roles.name -notcontains 'USER' -or $roles.name -contains 'ADMIN') {
                throw 'Tester must have USER without ADMIN.'
            }
        } elseif ($roles.name -notcontains 'ADMIN' -or $roles.name -contains 'USER') {
            throw 'Administrator must have ADMIN without USER.'
        }
    }
    Write-Output 'PASS: realm import, Compose readiness and matching credentials, directory token, human user read, tester roles, and write denial.'

    # Add state absent from the import, then recreate the container with the same database.
    Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users" -Headers $adminHeaders `
        -Method Post -ContentType 'application/json' -Body '{"username":"persisted-local-test","enabled":true}' | Out-Null
    docker stop --time 30 $testContainer | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not stop isolated Keycloak cleanly.' }
    docker rm $testContainer | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not remove isolated Keycloak for recreation.' }
    $testStarted = $false
    $testBase = Start-IsolatedKeycloak
    $realmBase = "$testBase/realms/cost-calc-realm"
    $directoryToken = Invoke-RestMethod "$realmBase/protocol/openid-connect/token" -Method Post `
        -Body @{ grant_type = 'client_credentials'; client_id = $DirectoryClientId; client_secret = $DirectoryClientSecret }
    $directoryHeaders = @{ Authorization = 'Bearer ' + $directoryToken.access_token }
    $users = Invoke-RestMethod "$testBase/admin/realms/cost-calc-realm/users" -Headers $directoryHeaders
    $users = @($users)
    if ($users.Count -ne 3 -or $users.username -notcontains 'persisted-local-test' -or
        $users.username -notcontains 'tester@cost-calc.eu' -or $users.username -notcontains 'admin@cost-calc.eu') {
        throw 'Realm state was lost on container recreation.'
    }
    Write-Output 'PASS: added human account and directory credentials survive container recreation.'
} finally {
    if ($testStarted) {
        docker rm --force $testContainer | Out-Null
        if ($LASTEXITCODE -ne 0) { Write-Warning "Could not remove isolated test container $testContainer" }
    }
    # Verify the exact generated child path before removing only test-owned files.
    $resolvedTestData = [IO.Path]::GetFullPath($testDataDir)
    if ($resolvedTestData -ne (Join-Path $testDataRoot $testContainer) -or
        $testContainer -notmatch '^cost-calc-realm-test-[a-f0-9]{32}$') {
        throw 'Refusing cleanup outside the isolated test data directory.'
    }
    if (Test-Path -LiteralPath $resolvedTestData) {
        Remove-Item -LiteralPath $resolvedTestData -Recurse -Force
    }
}
