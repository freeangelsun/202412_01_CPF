param(
    [string]$DockerRoot = "C:\dev\Docker",
    [switch]$StopAfter
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Read-EnvMap {
    param([Parameter(Mandatory)][string]$Path)
    $map = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        if ($line -match '^(?<key>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$') {
            $map[$Matches.key] = $Matches.value
        }
    }
    return $map
}

function Invoke-DockerChecked {
    param([Parameter(Mandatory)][string[]]$Arguments, [switch]$DiscardOutput)
    $callerLine = $MyInvocation.ScriptLineNumber
    Write-Host "Docker 단계 시작(line=$callerLine)"
    if ($DiscardOutput) {
        & docker @Arguments *> $null
    } else {
        & docker @Arguments
    }
    if ($LASTEXITCODE -ne 0) {
        throw "Docker 단계 실패(line=$callerLine, exit=$LASTEXITCODE). Secret 보호를 위해 인자는 기록하지 않습니다."
    }
}

function Wait-Http {
    param([Parameter(Mandatory)][string]$Uri, [int]$TimeoutSeconds = 180)
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) { return }
        } catch {
            Start-Sleep -Seconds 2
        }
    } while ([DateTimeOffset]::UtcNow -lt $deadline)
    throw "HTTP 준비 시간 초과: $Uri"
}

function Wait-ContainerHealthy {
    param([Parameter(Mandatory)][string]$Name, [int]$TimeoutSeconds = 180)
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        $statusOutput = & docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $Name 2>$null
        $inspectExitCode = $LASTEXITCODE
        $status = ($statusOutput -join "`n").Trim()
        if ($inspectExitCode -eq 0 -and $status -in @("healthy", "running")) { return }
        if ($status -eq "unhealthy") { throw "Container Health 실패: $Name" }
        Start-Sleep -Seconds 2
    } while ([DateTimeOffset]::UtcNow -lt $deadline)
    throw "Container 준비 시간 초과: $Name"
}

function Convert-JsonLinesToArray {
    param([object[]]$Lines)
    $text = ($Lines -join "`n").Trim()
    if ([string]::IsNullOrWhiteSpace($text)) { return @() }
    return @(($text | ConvertFrom-Json))
}

$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$envPath = Join-Path $secretRoot "cpf-runtime.env"
$toolEnvPath = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $envPath -PathType Leaf)) { throw "환경 파일이 없습니다: $envPath" }
if (-not (Test-Path -LiteralPath $toolEnvPath -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $toolEnvPath" }

$envMap = Read-EnvMap -Path $envPath
$toolMap = Read-EnvMap -Path $toolEnvPath
foreach ($name in @(
    "CPF_SFTP_USER", "CPF_KEYCLOAK_ADMIN_USER", "CPF_KEYCLOAK_TEST_USER",
    "CPF_KEYCLOAK_REALM", "CPF_KEYCLOAK_PUBLIC_CLIENT", "CPF_KEYCLOAK_SERVICE_CLIENT"
)) {
    if (-not $envMap.ContainsKey($name)) { throw "환경값이 없습니다: $name" }
}
if (-not $toolMap.ContainsKey("FULL_TOOLCHAIN_IMAGE")) { throw "FULL_TOOLCHAIN_IMAGE가 없습니다." }

$requiredSecrets = @(
    (Join-Path $secretRoot "sftp-password.txt"),
    (Join-Path $secretRoot "vault-token.txt"),
    (Join-Path $secretRoot "keycloak-admin-password.txt"),
    (Join-Path $secretRoot "keycloak-test-password.txt"),
    (Join-Path $secretRoot "keycloak-service-client-secret.txt")
)
foreach ($path in $requiredSecrets) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-Item -LiteralPath $path).Length -le 0) {
        throw "Secret 파일이 없거나 비어 있습니다: $path"
    }
}
$sftpPasswordPath = Join-Path $secretRoot "sftp-password.txt"
$keycloakTestPasswordPath = Join-Path $secretRoot "keycloak-test-password.txt"
$keycloakClientSecretPath = Join-Path $secretRoot "keycloak-service-client-secret.txt"

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $cpfRoot "cpf-env.ps1") -Action up -Target external
if ($LASTEXITCODE -ne 0) { throw "확장 연동 Service 시작 실패(exit=$LASTEXITCODE)" }

try {
    Wait-ContainerHealthy -Name "cpf-sftp" -TimeoutSeconds 120
    Wait-ContainerHealthy -Name "cpf-vault" -TimeoutSeconds 120
    Wait-ContainerHealthy -Name "cpf-keycloak" -TimeoutSeconds 300
    Wait-Http -Uri "http://127.0.0.1:18080/cpf-test/health" -TimeoutSeconds 120
    Wait-Http -Uri "http://127.0.0.1:8200/v1/sys/health" -TimeoutSeconds 120
    $oidcDiscoveryUri = "http://127.0.0.1:18081/realms/$($envMap['CPF_KEYCLOAK_REALM'])/.well-known/openid-configuration"
    Wait-Http -Uri $oidcDiscoveryUri -TimeoutSeconds 300
    $oidcDiscovery = Invoke-RestMethod -Uri $oidcDiscoveryUri -Method Get
    if ([string]::IsNullOrWhiteSpace([string]$oidcDiscovery.issuer) -or [string]::IsNullOrWhiteSpace([string]$oidcDiscovery.token_endpoint)) {
        throw "Keycloak OIDC Discovery 확인 실패"
    }

    $wiremockHealth = Invoke-RestMethod -Uri "http://127.0.0.1:18080/cpf-test/health" -Method Get
    if ($wiremockHealth.status -ne "UP") { throw "WireMock Health Fixture 상태 불일치" }

    $requestId = "cpf-fixture-$([Guid]::NewGuid().ToString('N'))"
    $wiremockSuccess = Invoke-RestMethod -Uri "http://127.0.0.1:18080/cpf-test/transactions" -Method Post -Headers @{ "X-Request-Id" = $requestId } -ContentType "application/json" -Body '{"amount":1000}'
    if ($wiremockSuccess.result -ne "SUCCESS" -or $wiremockSuccess.requestId -ne $requestId) {
        throw "WireMock 정상 응답 Fixture 확인 실패"
    }

    $unavailableConfirmed = $false
    try {
        Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:18080/cpf-test/unavailable" -TimeoutSec 10 | Out-Null
    } catch {
        if ($null -ne $_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 503) {
            $unavailableConfirmed = $true
        } else {
            throw
        }
    }
    if (-not $unavailableConfirmed) { throw "WireMock 503 Fixture 확인 실패" }

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:18080/cpf-test/slow" -TimeoutSec 10 | Out-Null
    $stopwatch.Stop()
    if ($stopwatch.ElapsedMilliseconds -lt 2500) { throw "WireMock 지연 Fixture 확인 실패" }

    $resetConfirmed = $false
    try {
        Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:18080/cpf-test/connection-reset" -TimeoutSec 10 | Out-Null
    } catch {
        $resetConfirmed = $true
    }
    if (-not $resetConfirmed) { throw "WireMock Connection Reset Fixture 확인 실패" }

    Invoke-DockerChecked -DiscardOutput -Arguments @(
        "exec", "cpf-vault", "/bin/sh", "-ec",
        'export VAULT_ADDR=http://127.0.0.1:8200; export VAULT_TOKEN="$(cat /run/secrets/vault_token)"; vault kv put secret/cpf/test status=ready >/dev/null'
    )
    $vaultResult = & docker exec cpf-vault /bin/sh -ec 'export VAULT_ADDR=http://127.0.0.1:8200; export VAULT_TOKEN="$(cat /run/secrets/vault_token)"; vault kv get -field=status secret/cpf/test'
    if ($LASTEXITCODE -ne 0 -or ($vaultResult -join "`n").Trim() -ne "ready") { throw "Vault Fixture 확인 실패" }

    Invoke-DockerChecked -DiscardOutput -Arguments @(
        "exec", "cpf-keycloak", "/bin/bash", "-ec",
        '/opt/keycloak/bin/kcadm.sh config credentials --server http://127.0.0.1:8080 --realm master --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$(cat /run/secrets/keycloak_admin_password)"'
    )

    $realm = $envMap["CPF_KEYCLOAK_REALM"]
    $testUser = $envMap["CPF_KEYCLOAK_TEST_USER"]
    $userJson = & docker exec cpf-keycloak /opt/keycloak/bin/kcadm.sh get users -r $realm -q "username=$testUser" --fields id,username
    if ($LASTEXITCODE -ne 0) { throw "Keycloak 사용자 조회 실패" }
    $users = @(Convert-JsonLinesToArray -Lines $userJson)
    if ($users.Count -eq 0) {
        Invoke-DockerChecked -DiscardOutput -Arguments @(
            "exec", "cpf-keycloak", "/opt/keycloak/bin/kcadm.sh", "create", "users", "-r", $realm,
            "-s", "username=$testUser", "-s", "enabled=true", "-s", "emailVerified=true", "-s", "firstName=CPF", "-s", "lastName=Reviewer", "-s", "email=$testUser@cpf.local", "-s", "requiredActions=[]"
        )
    }
    Invoke-DockerChecked -DiscardOutput -Arguments @(
        "exec", "-e", "CPF_REALM=$realm", "-e", "CPF_TEST_USER=$testUser",
        "cpf-keycloak", "/bin/bash", "-ec",
        '/opt/keycloak/bin/kcadm.sh set-password -r "$CPF_REALM" --username "$CPF_TEST_USER" --new-password "$(cat /run/secrets/keycloak_test_password)" --temporary=false'
    )

    $userJson = & docker exec cpf-keycloak /opt/keycloak/bin/kcadm.sh get users -r $realm -q "username=$testUser" --fields id,username
    if ($LASTEXITCODE -ne 0) { throw "Keycloak 사용자 재조회 실패" }
    $users = @(Convert-JsonLinesToArray -Lines $userJson)
    if ($users.Count -ne 1) { throw "Keycloak 테스트 사용자 식별 실패: $testUser" }
    $userId = [string]$users[0].id
    $roleJson = & docker exec cpf-keycloak /opt/keycloak/bin/kcadm.sh get "users/$userId/role-mappings/realm" -r $realm --fields name
    if ($LASTEXITCODE -ne 0) { throw "Keycloak Realm Role 조회 실패" }
    $assignedRoles = @(Convert-JsonLinesToArray -Lines $roleJson)
    $assignedRoleNames = @($assignedRoles | ForEach-Object { [string]$_.name })
    if ($assignedRoleNames -notcontains "CPF_OPERATOR") {
        Invoke-DockerChecked -DiscardOutput -Arguments @(
            "exec", "cpf-keycloak", "/opt/keycloak/bin/kcadm.sh", "add-roles", "-r", $realm,
            "--uusername", $testUser, "--rolename", "CPF_OPERATOR"
        )
    }

    $serviceClient = $envMap["CPF_KEYCLOAK_SERVICE_CLIENT"]
    $clientJson = & docker exec cpf-keycloak /opt/keycloak/bin/kcadm.sh get clients -r $realm -q "clientId=$serviceClient" --fields id,clientId
    if ($LASTEXITCODE -ne 0) { throw "Keycloak Client 조회 실패" }
    $clients = @(Convert-JsonLinesToArray -Lines $clientJson)
    if ($clients.Count -gt 1) { throw "Keycloak Service Client 중복: $serviceClient" }
    if ($clients.Count -eq 0) {
        Invoke-DockerChecked -DiscardOutput -Arguments @(
            "exec", "-e", "CPF_REALM=$realm", "-e", "CPF_SERVICE_CLIENT=$serviceClient",
            "cpf-keycloak", "/bin/bash", "-ec",
            '/opt/keycloak/bin/kcadm.sh create clients -r "$CPF_REALM" -s "clientId=$CPF_SERVICE_CLIENT" -s enabled=true -s publicClient=false -s serviceAccountsEnabled=true -s clientAuthenticatorType=client-secret -s "secret=$(cat /run/secrets/keycloak_service_client_secret)"'
        )
    } else {
        $clientId = [string]$clients[0].id
        Invoke-DockerChecked -DiscardOutput -Arguments @(
            "exec", "-e", "CPF_REALM=$realm", "-e", "CPF_CLIENT_UUID=$clientId",
            "cpf-keycloak", "/bin/bash", "-ec",
            '/opt/keycloak/bin/kcadm.sh update "clients/$CPF_CLIENT_UUID" -r "$CPF_REALM" -s "secret=$(cat /run/secrets/keycloak_service_client_secret)"'
        )
    }

    $runnerImage = $toolMap["FULL_TOOLCHAIN_IMAGE"]
    $identityScript = @'
set -euo pipefail
realm="__REALM__"
user="__TEST_USER__"
public_client="__PUBLIC_CLIENT__"
service_client="__SERVICE_CLIENT__"
test_password="$(cat /run/secrets/keycloak_test_password)"
service_secret="$(cat /run/secrets/keycloak_service_client_secret)"
curl -fsS -X POST "http://cpf-keycloak:8080/realms/$realm/protocol/openid-connect/token" \
  -d grant_type=password -d client_id="$public_client" -d username="$user" \
  --data-urlencode "password=$test_password" | jq -e '.access_token | length > 20' >/dev/null
curl -fsS -X POST "http://cpf-keycloak:8080/realms/$realm/protocol/openid-connect/token" \
  -d grant_type=client_credentials -d client_id="$service_client" \
  --data-urlencode "client_secret=$service_secret" | jq -e '.access_token | length > 20' >/dev/null
'@
    $identityScript = $identityScript.Replace("__REALM__", $realm).Replace("__TEST_USER__", $testUser).Replace("__PUBLIC_CLIENT__", $envMap["CPF_KEYCLOAK_PUBLIC_CLIENT"]).Replace("__SERVICE_CLIENT__", $serviceClient)
    Invoke-DockerChecked -DiscardOutput -Arguments @(
        "run", "--rm", "--network", "cpf_default",
        "--mount", "type=bind,source=$keycloakTestPasswordPath,target=/run/secrets/keycloak_test_password,readonly",
        "--mount", "type=bind,source=$keycloakClientSecretPath,target=/run/secrets/keycloak_service_client_secret,readonly",
        $runnerImage, "bash", "-lc", $identityScript
    )

    $sftpUser = $envMap["CPF_SFTP_USER"]
        $sftpScript = @'
set -euo pipefail
work="$(mktemp -d)"
printf 'cpf-sftp-ready\n' > "$work/upload.txt"
{ printf 'put %s /exchange/inbound/cpf-sftp-ready.txt\n' "$work/upload.txt"; printf 'get /exchange/inbound/cpf-sftp-ready.txt %s\n' "$work/download.txt"; printf 'bye\n'; } | sshpass -f /run/secrets/sftp_password sftp -o BatchMode=no -o PreferredAuthentications=password -o PubkeyAuthentication=no -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P 22 "__SFTP_USER__@cpf-sftp"
cmp "$work/upload.txt" "$work/download.txt"
'@.Replace("__SFTP_USER__", $sftpUser)
    Invoke-DockerChecked -DiscardOutput -Arguments @(
        "run", "--rm", "--network", "cpf_default",
        "--mount", "type=bind,source=$sftpPasswordPath,target=/run/secrets/sftp_password,readonly",
        $runnerImage, "bash", "-lc", $sftpScript
    )

    $result = [ordered]@{
        generatedAt = [DateTimeOffset]::Now.ToString("o")
        wiremock = "PASS"
        wiremockScenarios = @("health", "success", "unavailable-503", "slow", "connection-reset")
        sftp = "PASS"
        vault = "PASS"
        keycloak = "PASS"
        keycloakFlows = @("password-grant-local-fixture", "client-credentials-local-fixture")
        realm = $realm
        testUser = $testUser
        publicClient = $envMap["CPF_KEYCLOAK_PUBLIC_CLIENT"]
        serviceClient = $serviceClient
        secretsPrinted = $false
    }
    $outputPath = Join-Path $cpfRoot "output\integration\integration-fixture-result.json"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $outputPath) | Out-Null
    [System.IO.File]::WriteAllText($outputPath, (($result | ConvertTo-Json -Depth 5) + "`n"), [System.Text.UTF8Encoding]::new($false))
    Write-Host "확장 연동 Fixture 초기화 및 연결 확인 완료" -ForegroundColor Green
    Write-Host "결과: $outputPath"
    Write-Host "Secret 값은 출력하지 않았습니다."
} finally {
    if ($StopAfter) {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $cpfRoot "cpf-env.ps1") -Action stop -Target external
    }
}
