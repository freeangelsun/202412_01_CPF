<#
.SYNOPSIS
    Full Runtime 의 Runtime 검증 구간을 독립 실행 가능한 3~5분 Work Unit 으로 수행합니다.

.DESCRIPTION
    Full Runtime 163 단계를 결함 하나마다 처음부터 반복하면 사이클당 50 분이 소모된다.
    이 스크립트는 Full Runtime 이 수행하는 것과 **동일한 명령·동일한 인자·동일한 격리 DB 계약**으로
    Runtime 검증 구간만 떼어 실행한다. 검증 범위를 줄이지 않으며, 최종 Closure 는 여전히 전체
    Full Runtime 으로 수행한다.

    각 Work Unit 은 다음을 출력한다.
      - Unit 이름 / 검증 범위 / 예상 소요 / prerequisite
      - 입력 Source Identity
      - 단계별 PASS/FAIL 과 소요시간
      - Evidence 경로, 로그 절대경로

    Full Runtime 과 같은 verifier-owned run-scoped DB(cpf_verify_<runId>_runtime / cpfv_<runId>_pr)를
    사용하고, 종료 시 반드시 정리한다. 비밀값은 자식 프로세스 환경으로만 전달하며 명령줄에 남기지 않는다.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('BATCH_TWO_WORKER', 'GATEWAY_BATCH', 'BATCH_AND_GATEWAY', 'ONE_WAS')]
    [string] $Unit,
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $DockerRoot = 'C:\dev\Docker',
    [string] $DockerSecretFile = '',
    [string] $OutputRoot = '',
    [switch] $SkipBuild
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Full Runtime child-process UTF-8 계약과 동일하게 고정합니다.
$CpfUtf8 = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8
    [Console]::OutputEncoding = $CpfUtf8
    $OutputEncoding = $CpfUtf8
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
if ([string]::IsNullOrWhiteSpace($DockerSecretFile)) {
    $DockerSecretFile = Join-Path $DockerRoot 'Secrets\cpf-runtime.env'
}
if ([string]::IsNullOrWhiteSpace($OutputRoot)) {
    $OutputRoot = Join-Path $env:USERPROFILE 'Downloads'
}

$startedAt = Get-Date
$stamp = $startedAt.ToString('yyyyMMdd_HHmmss')
$evidenceDir = Join-Path $OutputRoot ("CPF_WORKUNIT_{0}_{1}" -f $Unit, $stamp)
New-Item -ItemType Directory -Force -Path $evidenceDir | Out-Null
$logPath = Join-Path $evidenceDir 'work-unit.log'

$script:Steps = @()
function Write-Line([string] $text) {
    Write-Host $text
    Add-Content -LiteralPath $logPath -Value $text -Encoding UTF8
}

function Invoke-Unit-Stage([string] $name, [string[]] $arguments, [hashtable] $environment) {
    $begin = Get-Date
    $previous = @{}
    foreach ($key in $environment.Keys) {
        $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
        [Environment]::SetEnvironmentVariable($key, [string] $environment[$key], 'Process')
    }
    try {
        Push-Location $RepoRoot
        $output = & pwsh @arguments 2>&1
        $code = $LASTEXITCODE
        Pop-Location
    } finally {
        foreach ($key in $previous.Keys) {
            [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process')
        }
    }
    $elapsed = ((Get-Date) - $begin).TotalSeconds
    $status = if ($code -eq 0) { 'PASS' } else { 'FAIL' }
    Add-Content -LiteralPath $logPath -Value ($output | Out-String) -Encoding UTF8
    Write-Line ("[{0}] {1} rc={2} {3:n1}s" -f $status, $name, $code, $elapsed)
    $script:Steps += [ordered]@{ name = $name; status = $status; exitCode = $code; seconds = [math]::Round($elapsed, 1) }
    return ($code -eq 0)
}

# 검증기 소유 DB 에 Runtime 전제 데이터를 넣는다. Docker MariaDB 컨테이너를 통해 실행하며,
# 비밀값은 자식 환경으로만 전달하고 명령줄/로그에 남기지 않는다.
function Invoke-Unit-Sql([string] $name, [string] $sql) {
    $begin = Get-Date
    $environment = @{ MYSQL_PWD = $rootPassword }
    $previous = @{}
    foreach ($key in $environment.Keys) {
        $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
        [Environment]::SetEnvironmentVariable($key, [string] $environment[$key], 'Process')
    }
    try {
        $output = & docker exec -e MYSQL_PWD -i cpf-mariadb mariadb -uroot -N -B -e $sql 2>&1
        $code = $LASTEXITCODE
    } finally {
        foreach ($key in $previous.Keys) {
            [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process')
        }
    }
    $elapsed = ((Get-Date) - $begin).TotalSeconds
    $status = if ($code -eq 0) { 'PASS' } else { 'FAIL' }
    Add-Content -LiteralPath $logPath -Value ($output | Out-String) -Encoding UTF8
    Write-Line ("[{0}] {1} rc={2} {3:n1}s" -f $status, $name, $code, $elapsed)
    $script:Steps += [ordered]@{ name = $name; status = $status; exitCode = $code; seconds = [math]::Round($elapsed, 1) }
    return ($code -eq 0)
}

# 입력 Source Identity 는 Full Runtime 과 같은 정본 계산기를 사용합니다.
$sourceState = (& python -B (Join-Path $RepoRoot 'cpf-tools\verification\tools\cpf-source-state.py') --root $RepoRoot --scope source | ConvertFrom-Json)
$sourceIdentity = [string]$sourceState.contentSha256
# Runtime OpenAPI 계약 검증기는 40-hex(contentSha1) Source Identity 만 받는다. Full Runtime 도
# 같은 값을 넘긴다. 64-hex 를 넘기면 검증을 시작도 못 하고 거절한다.
$sourceIdentitySha1 = [string]$sourceState.contentSha1

$scopeByUnit = @{
    BATCH_TWO_WORKER   = '5 Batch 역할 + 2번째 Worker + Generated Domain 기동, DB claim/lease/fencing, drain/resume, Worker kill -> UNKNOWN -> reconcile -> takeover'
    GATEWAY_BATCH      = 'Gateway + Batch 동시 기동, Gateway registry/route, Runtime OpenAPI 계약'
    BATCH_AND_GATEWAY  = '위 두 Unit 을 같은 격리 DB 에서 연속 수행 (Full Runtime 과 동일한 순서)'
    ONE_WAS            = '1-WAS(ADM+Backoffice+Common) 기동, File Log 표준, DB Log 정책, 통합 로그 상관관계, ADM/Backoffice Runtime OpenAPI 계약, 정지/정리'
}
$expectedByUnit = @{ BATCH_TWO_WORKER = '약 4분'; GATEWAY_BATCH = '약 1분'; BATCH_AND_GATEWAY = '약 5분'; ONE_WAS = '약 6분' }

Write-Line "=============================================================="
Write-Line ("CPF Runtime Work Unit : {0}" -f $Unit)
Write-Line ("검증 범위             : {0}" -f $scopeByUnit[$Unit])
Write-Line ("예상 소요             : {0}" -f $expectedByUnit[$Unit])
Write-Line  "prerequisite          : Java 25, Docker(cpf-mariadb), Docker secret env, Gradle 산출물(bootJar)"
Write-Line ("Source Identity       : {0}" -f $sourceIdentity)
Write-Line ("시작                  : {0}" -f $startedAt.ToString('o'))
Write-Line ("Evidence              : {0}" -f $evidenceDir)
Write-Line ("Log                   : {0}" -f $logPath)
Write-Line "=============================================================="

if (-not (Test-Path -LiteralPath $DockerSecretFile -PathType Leaf)) {
    throw "Docker secret env 를 찾을 수 없습니다: $DockerSecretFile"
}
# Full Runtime 과 동일하게 secret 을 provisioning 한 뒤 현재 프로세스 환경으로 읽어들입니다.
$ensureSecrets = Join-Path $RepoRoot 'cpf-tools\environment\docker-development-test\ensure-cpf-runtime-secrets.ps1'
if (Test-Path -LiteralPath $ensureSecrets -PathType Leaf) {
    try { & $ensureSecrets -SecretFile $DockerSecretFile | Out-Null } catch { }
}
foreach ($line in Get-Content -LiteralPath $DockerSecretFile -Encoding UTF8) {
    if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim(), 'Process')
    }
}
$adminPassword = [Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD', 'Process')
$rootPassword = [Environment]::GetEnvironmentVariable('CPF_DB_ROOT_PASSWORD', 'Process')
if ([string]::IsNullOrWhiteSpace($adminPassword)) { throw 'CPF_ADMIN_PASSWORD 를 Docker secret env 에서 확인할 수 없습니다.' }

# Full Runtime 과 동일한 verifier-owned run-scoped DB 계약입니다.
$allPassed = $true
$runId = ([guid]::NewGuid().ToString('N').Substring(0, 12)).ToLowerInvariant()

# 이전 실행이 빌드 실패나 중단으로 정리 단계에 도달하지 못하면 verifier-owned 스키마/사용자가
# Docker 에 그대로 남는다. 실제로 실패 실행들이 스키마 8개와 사용자 16개를 쌓아 두었다.
# 검증기는 자기 namespace(cpf_verify_<runId>_* / cpfv_<runId>_*)를 소유하므로, 실행 전에
# **현재 실행이 아닌** 잔여물을 먼저 제거한다. 정본 명명 계약에 맞는 것만 지우고, 시스템 DB 와
# 제품 스키마는 절대 건드리지 않는다.
function Remove-CpfOrphanVerifierDatabases {
    param([string] $CurrentRunId)
    $listSql = "SELECT schema_name FROM information_schema.schemata WHERE schema_name REGEXP '^cpf_verify_[a-f0-9]{8,24}_(runtime|mbw)$';"
    $orphanDatabases = @(& docker exec -e MYSQL_PWD -i cpf-mariadb mariadb -uroot -N -B -e $listSql 2>$null |
        Where-Object { $_ -and ($_ -notlike "cpf_verify_${CurrentRunId}_*") })
    $userSql = "SELECT user FROM mysql.user WHERE user REGEXP '^cpfv_[a-f0-9]{8,24}_(pr|pm|br|bm)$';"
    $orphanUsers = @(& docker exec -e MYSQL_PWD -i cpf-mariadb mariadb -uroot -N -B -e $userSql 2>$null |
        Where-Object { $_ -and ($_ -notlike "cpfv_${CurrentRunId}_*") })
    if ($orphanDatabases.Count -eq 0 -and $orphanUsers.Count -eq 0) {
        Write-Line '[PASS] VERIFIER_DB_ORPHAN_SWEEP 잔여 없음'
        return
    }
    $statements = @()
    foreach ($database in $orphanDatabases) { $statements += "DROP DATABASE IF EXISTS ``$database``;" }
    foreach ($user in $orphanUsers) { $statements += "DROP USER IF EXISTS ``$user``@'%';" }
    $statements += 'FLUSH PRIVILEGES;'
    [void](& docker exec -e MYSQL_PWD -i cpf-mariadb mariadb -uroot -e ($statements -join '') 2>$null)
    Write-Line ('[PASS] VERIFIER_DB_ORPHAN_SWEEP databases={0} users={1}' -f $orphanDatabases.Count, $orphanUsers.Count)
}
$dbSecret = "CpfBat!$([guid]::NewGuid().ToString('N').Substring(0,20))9a"
$migrationSecret = "CpfBatMig!$([guid]::NewGuid().ToString('N').Substring(0,20))8b"
$dbEvidence = Join-Path $evidenceDir 'batch-runtime-db'
$prepEnv = @{
    CPF_ADMIN_PASSWORD = $adminPassword
    CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD = $migrationSecret
    CPF_LOCAL_RUNTIME_DB_PASSWORD = $dbSecret
}
$runtimeEnv = @{
    CPF_DB_APP_PASSWORD = $dbSecret
    CPF_CORE_DB_RUNTIME_PASSWORD = $dbSecret
    CPF_LOCAL_RUNTIME_DB_PASSWORD = $dbSecret
    CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD = $migrationSecret
    CPF_ADMIN_PASSWORD = $adminPassword
}
if (-not [string]::IsNullOrWhiteSpace($rootPassword)) { $runtimeEnv.CPF_DB_ROOT_PASSWORD = $rootPassword }

# Work Unit 은 Full Runtime 의 GRADLE 단계 없이 단독 실행되므로 필요한 bootJar 를 스스로 갖춘다.
# Full Runtime 의 GRADLE_FULL_BUILD_QUALITY 는 'clean cpfBuildAll qualityGate' 로 전량 재빌드하지만,
# 여기서는 clean 없이 증분 build 만 수행한다(검증 범위가 아니라 준비 단계이므로 축소가 아니다).
if (-not $SkipBuild) {
    $gradleBase = @('-PcpfResourceProfile=local', '-PcpfSkipFrontendBuild=true',
        '-PcpfIncludeGeneratedDomains=true', '-PcpfDbVendor=mariadb', '--no-daemon', '--no-parallel')
    $buildBegin = Get-Date
    Push-Location $RepoRoot
    $buildOutput = & (Join-Path $RepoRoot 'gradlew.bat') (@('cpfBuildAll', '-x', 'test', '--continue') + $gradleBase) 2>&1
    $buildCode = $LASTEXITCODE
    Pop-Location
    $buildElapsed = ((Get-Date) - $buildBegin).TotalSeconds
    Add-Content -LiteralPath $logPath -Value ($buildOutput | Out-String) -Encoding UTF8
    $buildStatus = if ($buildCode -eq 0) { 'PASS' } else { 'FAIL' }
    Write-Line ("[{0}] PREREQUISITE_BOOTJAR_BUILD rc={1} {2:n1}s" -f $buildStatus, $buildCode, $buildElapsed)
    $script:Steps += [ordered]@{ name = 'PREREQUISITE_BOOTJAR_BUILD'; status = $buildStatus; exitCode = $buildCode; seconds = [math]::Round($buildElapsed, 1) }
    if ($buildCode -ne 0) { $allPassed = $false }
}

$profilePath = Join-Path $dbEvidence 'profile.json'

$loopbackProfilePath = $null

# 이전 실행 잔여물을 먼저 걷어낸다. 남겨 두면 Docker 에 테스트 스키마가 계속 쌓인다.
$previousMysqlPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')
try {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $rootPassword, 'Process')
    Remove-CpfOrphanVerifierDatabases -CurrentRunId $runId
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousMysqlPassword, 'Process')
}

$prepOk = Invoke-Unit-Stage 'RUNTIME_DB_PREP' @(
    '-NoProfile', '-File', '.\cpf-tools\db\verification\prepare-cpf-local-runtime-db.ps1',
    '-Root', $RepoRoot, '-VerifierRunId', $runId, '-EvidenceRoot', $dbEvidence) $prepEnv

if (-not $prepOk) {
    $allPassed = $false
} else {
    # profile 의 host 는 Docker 네트워크 이름이라 호스트 JVM 이 해석하지 못한다.
    # Full Runtime 과 동일하게 loopback profile 을 별도로 만든다.
    if (Test-Path -LiteralPath $profilePath -PathType Leaf) {
        $hostProfile = Get-Content -LiteralPath $profilePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
        foreach ($moduleProperty in @($hostProfile.modules.PSObject.Properties)) {
            $moduleProperty.Value.host = '127.0.0.1'
        }
        $loopbackProfilePath = Join-Path $dbEvidence 'profile-loopback.json'
        $hostProfile | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath $loopbackProfilePath -Encoding UTF8
    }
    try {
        if ($Unit -in @('BATCH_TWO_WORKER', 'BATCH_AND_GATEWAY')) {
            $ok = Invoke-Unit-Stage 'BATCH_TWO_WORKER_CRASH_UNKNOWN' @(
                '-NoProfile', '-File', '.\cpf-tools\runtime\tools\smoke-bat-two-worker-runtime.ps1',
                '-Root', $RepoRoot, '-ResultDir', (Join-Path $evidenceDir 'batch-two-worker'),
                '-ClientAdapter', 'Docker', '-MariaDbContainer', 'cpf-mariadb',
                '-DatabaseName', "cpf_verify_${runId}_runtime", '-DbUser', "cpfv_${runId}_pr") $runtimeEnv
            if (-not $ok) { $allPassed = $false }
        }
        if ($Unit -in @('GATEWAY_BATCH', 'BATCH_AND_GATEWAY')) {
            $gatewayArgs = @('-NoProfile', '-File', '.\cpf-tools\runtime\tools\smoke-gateway-bat-runtime.ps1',
                '-Root', $RepoRoot, '-ResultDir', (Join-Path $evidenceDir 'gateway-batch-runtime'),
                '-DbVendor', 'mariadb')
            if ($loopbackProfilePath -and (Test-Path -LiteralPath $loopbackProfilePath -PathType Leaf)) {
                $gatewayArgs += @('-DatabaseProfilePath', $loopbackProfilePath)
            }
            $ok = Invoke-Unit-Stage 'GATEWAY_BATCH_RUNTIME' $gatewayArgs $runtimeEnv
            if (-not $ok) { $allPassed = $false }
        }
        if ($Unit -eq 'ONE_WAS') {
            # Full Runtime 의 1-WAS 구간(LOCAL_ONE_WAS_* / LOCAL_FILE_LOG_STANDARD /
            # LOCAL_DB_LOG_POLICY_RUNTIME / LOCAL_INTEGRATED_LOG_CORRELATION /
            # ADM|BACKOFFICE_RUNTIME_OPENAPI_RELEASE)을 같은 명령·같은 인자로 수행한다.
            # 검증 범위를 줄이지 않는다. 축소는 이 Unit 의 목적이 아니다.
            $oneWasSecretDirectory = Join-Path $evidenceDir 'runtime-secrets'
            $runtimeFileLogRoot = Join-Path $evidenceDir 'runtime-file-logs'
            [IO.Directory]::CreateDirectory($oneWasSecretDirectory) | Out-Null
            [IO.Directory]::CreateDirectory($runtimeFileLogRoot) | Out-Null

            # 검증기 소유 자격증명은 매 실행마다 새로 만들고 명령줄에 남기지 않는다.
            $runtimePepper = "CpfPepper-$([guid]::NewGuid().ToString('N'))"
            # Bootstrap 비밀번호와, 강제 변경 이후 모든 검증기가 사용할 비밀번호를 분리한다.
            # 회전은 Runtime 준비 단계가 한 번만 수행한다(검증기마다 회전하면 뒤 검증기가 로그인에 실패한다).
            $admBootstrapPassword = "Adm!$([guid]::NewGuid().ToString('N').Substring(0,20))7X"
            $admSmokePassword = "Adm!$([guid]::NewGuid().ToString('N').Substring(0,20))9Z"
            $admApprovalProofKey = [Convert]::ToBase64String(
                [Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
            $backofficeSmokePassword = "Backoffice!$([guid]::NewGuid().ToString('N').Substring(0,20))6Y"

            $runtimeDbResultPath = Join-Path $dbEvidence 'runtime-db.json'
            $backofficeBootstrapResultPath = Join-Path $dbEvidence 'backoffice-bootstrap.json'
            $bootstrapOk = Invoke-Unit-Stage 'LOCAL_ONE_WAS_BACKOFFICE_BOOTSTRAP_PREP' @(
                '-NoProfile', '-File', '.\cpf-tools\db\verification\prepare-cpf-local-backoffice-bootstrap.ps1',
                '-VerifierRunId', $runId, '-RuntimeDbResultPath', $runtimeDbResultPath,
                '-SecretDirectory', $oneWasSecretDirectory,
                '-ResultPath', $backofficeBootstrapResultPath) @{
                    CPF_ADMIN_PASSWORD = $adminPassword
                    CPF_BACKOFFICE_SMOKE_PASSWORD = $backofficeSmokePassword
                }
            if (-not $bootstrapOk) { $allPassed = $false }

            $runtimeDb = Get-Content -LiteralPath $runtimeDbResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
            $bootstrap = if (Test-Path -LiteralPath $backofficeBootstrapResultPath -PathType Leaf) {
                Get-Content -LiteralPath $backofficeBootstrapResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
            } else { $null }
            $platformDatabase = [string]$runtimeDb.platformDatabase
            $platformUrl = "jdbc:mariadb://127.0.0.1:3306/$($runtimeDb.platformDatabase)"
            $backofficeUrl = "jdbc:mariadb://127.0.0.1:3306/$($runtimeDb.backofficeDatabase)"

            $oneWasBaseUrl = 'http://127.0.0.1:8080'

            # Full Runtime 과 동일한 1-WAS 실행 환경이다. 값 하나라도 빠지면 같은 기동이 아니다.
            # 검증 Runtime은 loopback HTTP를 쓰므로, production HTTPS cookie 기본값을 그대로
            # 상속하면 session/CSRF cookie가 되돌아오지 않는다. 이 값은 verifier-owned local
            # transport 설정이며 product 기본값/production 보안 정책을 바꾸지 않는다.
            $oneWasRuntimeEnv = @{
                CPF_LOG_ROOT = $runtimeFileLogRoot
                CPF_PASSWORD_PEPPER = $runtimePepper
                CPF_ENVIRONMENT_CODE = 'local'
                CPF_RUNTIME_INSTANCE_ID = if ($bootstrap) { [string]$bootstrap.instanceId } else { "cpf-local-$runId" }
                CPF_ADM_BOOTSTRAP_ENABLED = 'true'
                CPF_ADM_BOOTSTRAP_PASSWORD = $admBootstrapPassword
                CPF_ADM_BOOTSTRAP_OPERATOR_ID = 'admin'
                CPF_ADM_BOOTSTRAP_OPERATOR_NAME = 'CPF FullLocal Admin'
                CPF_ADM_APPROVAL_PROOF_KEY_BASE64 = $admApprovalProofKey
                CPF_ADM_SESSION_COOKIE_SECURE = 'false'
                CPF_ADM_ALLOWED_ORIGINS = $oneWasBaseUrl
                CPF_BACKOFFICE_DATASOURCE_ENABLED = 'true'
                CPF_BACKOFFICE_BOOTSTRAP_APPROVAL_TOKEN_FILE = if ($bootstrap) { [string]$bootstrap.tokenFile } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_PASSWORD_FILE = if ($bootstrap) { [string]$bootstrap.passwordFile } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_APPROVAL_SCOPE = if ($bootstrap) { [string]$bootstrap.approvalScope } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_OPERATION_ID = if ($bootstrap) { [string]$bootstrap.operationId } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_LOGIN_ID = if ($bootstrap) { [string]$bootstrap.loginId } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_OPERATOR_NAME = if ($bootstrap) { [string]$bootstrap.operatorName } else { '' }
                CPF_BACKOFFICE_BOOTSTRAP_ROLE_CODE = if ($bootstrap) { [string]$bootstrap.roleCode } else { 'BACKOFFICE_MANAGER' }
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_ENABLED = 'true'
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_URL = $platformUrl
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_USERNAME = [string]$runtimeDb.platformRuntimeUser
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD = $dbSecret
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_DRIVER_CLASS_NAME = 'org.mariadb.jdbc.Driver'
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_ENABLED = 'true'
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_URL = $backofficeUrl
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_USERNAME = [string]$runtimeDb.backofficeRuntimeUser
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_PASSWORD = $dbSecret
                CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_DRIVER_CLASS_NAME = 'org.mariadb.jdbc.Driver'
            }

            # 1-WAS Runtime Agent 는 기동 시 자기 instance 를 중앙 Registry 에 등록하고,
            # `CpfRuntimeControlPlaneRepository.ensureServiceAndEndpoint` 가 그 전제를 fail-closed 로
            # 확인한다. 검증기 소유 DB 는 product seed 만 적재하므로 그 행이 없어
            # "Runtime Agent service가 중앙 Registry에 등록되어 있지 않습니다: LOCAL" 로 기동이 실패했다.
            # Runtime 데이터 전제는 Verifier 가 만든다(Harness §25.6.2). 제품 조건을 완화하지 않는다.
            # Service Identity 는 SystemCode 가 아니다(Harness 30.11/30.13). 1-WAS 는 System 이 아니므로
            # 가상 SystemCode 를 만들지 않고, Runtime 이 선언한 application 이름을 Service Identity 로 쓴다.
            # 값을 여기 하드코딩하지 않고 Runtime 정본 설정에서 읽는다(Harness 30.24).
            $oneWasRuntimeYml = Join-Path $RepoRoot 'cpf-tools/runtime/cpf-local-runtime/src/main/resources/application.yml'
            $oneWasServiceId = (Select-String -LiteralPath $oneWasRuntimeYml -Pattern '^\s{4}name:\s*(\S+)\s*$' |
                Select-Object -First 1).Matches[0].Groups[1].Value
            if ([string]::IsNullOrWhiteSpace($oneWasServiceId)) {
                throw "1-WAS Service Identity 를 정본 설정에서 읽지 못했습니다: $oneWasRuntimeYml"
            }

            # 이 Unit 이 구동하는 업무 거래는 MBW Domain 소유다(아래 ProbeOperationId 참조).
            # System6 Header 와 거래ID issuer 는 그 Domain 의 canonical Identity 를 써야 한다
            # (Harness 30.2/30.7). 값을 여기 하드코딩하지 않고 소유 Domain 의 정본 설정에서 읽는다.
            $mbwRuntimeYml = Join-Path $RepoRoot 'cpf-backoffice/online/src/main/resources/application.yml'
            $oneWasBusinessSystemCode = (Select-String -LiteralPath $mbwRuntimeYml -Pattern '^\s*system-code:\s*([A-Z0-9]{3})\s*$' |
                Select-Object -First 1).Matches[0].Groups[1].Value
            if ([string]::IsNullOrWhiteSpace($oneWasBusinessSystemCode)) {
                throw "업무 Domain 의 canonical SystemCode 를 정본 설정에서 읽지 못했습니다: $mbwRuntimeYml"
            }

            # ADM 운영 API 는 Business SystemCode 가 아니라 정본 운영 ChannelCode 로 lineage 를 구성한다
            # (Harness 30.11 / 30.16.1). 값을 하드코딩하지 않고 ADM 정본 설정에서 읽는다.
            $admRuntimeYml = Join-Path $RepoRoot 'cpf-admin/src/main/resources/application.yml'
            $admOperationalChannelCode = (Select-String -LiteralPath $admRuntimeYml -Pattern '^\s*channel-code:\s*([A-Z0-9]{3})\s*$' |
                Select-Object -First 1).Matches[0].Groups[1].Value
            if ([string]::IsNullOrWhiteSpace($admOperationalChannelCode)) {
                throw "ADM 운영 ChannelCode 를 정본 설정에서 읽지 못했습니다: $admRuntimeYml"
            }
            $oneWasEndpointCode = "${oneWasServiceId}_API"
            $seedSql = @(
                "INSERT INTO ${platformDatabase}.OPS_SERVICE(service_id,service_name,service_type,owner_module_code,description,use_yn,created_by,updated_by)",
                " VALUES('$oneWasServiceId','CPF Local 1-WAS','INTERNAL','$oneWasServiceId','Verifier-owned 1-WAS runtime service','Y','HARNESS','HARNESS')",
                " ON DUPLICATE KEY UPDATE service_name=VALUES(service_name),service_type=VALUES(service_type),owner_module_code=VALUES(owner_module_code),use_yn='Y',updated_by='HARNESS';",
                "INSERT INTO ${platformDatabase}.OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,created_by,updated_by)",
                " VALUES('$oneWasEndpointCode','$oneWasServiceId','CPF Local 1-WAS API','HTTP','$oneWasBaseUrl','/',30000,0,'Y','HARNESS','HARNESS')",
                " ON DUPLICATE KEY UPDATE service_id=VALUES(service_id),endpoint_name=VALUES(endpoint_name),endpoint_type=VALUES(endpoint_type),base_url=VALUES(base_url),context_path=VALUES(context_path),use_yn='Y',updated_by='HARNESS';"
            ) -join ''
            $seedOk = Invoke-Unit-Sql 'LOCAL_ONE_WAS_RUNTIME_REGISTRY_SEED' $seedSql
            if (-not $seedOk) { $allPassed = $false }

            $started = Invoke-Unit-Stage 'LOCAL_ONE_WAS_START' @(
                '-NoProfile', '-File', '.\cpf-tools\runtime\tools\start-cpf-local.ps1',
                '-Mode', 'integrated', '-ResourceProfile', 'local') $oneWasRuntimeEnv
            if (-not $started) { $allPassed = $false }

            try {
                if ($started) {
                    $integratedLogRoot = Join-Path $evidenceDir 'integrated-logging'
                    $fileLogEvidence = Join-Path $integratedLogRoot 'file'
                    $policyLogEvidence = Join-Path $integratedLogRoot 'policy'
                    [IO.Directory]::CreateDirectory($fileLogEvidence) | Out-Null
                    [IO.Directory]::CreateDirectory($policyLogEvidence) | Out-Null
                    $admSecretEnv = @{
                        CPF_ADM_SMOKE_PASSWORD = $admSmokePassword
                        CPF_ADM_APPROVAL_PROOF_KEY_BASE64 = $admApprovalProofKey
                    }

                    # ADM Bootstrap 계정의 강제 비밀번호 변경은 Runtime 당 한 번만 수행한다.
                    # 검증기마다 회전하면 먼저 실행된 검증기가 비밀번호를 바꿔 뒤 검증기가
                    # 검증 대상과 무관한 인증 실패로 죽는다.
                    if (-not (Invoke-Unit-Stage 'LOCAL_ONE_WAS_ADM_BOOTSTRAP_ROTATION' @(
                        '-NoProfile', '-File', '.\cpf-tools\runtime\tools\prepare-cpf-adm-bootstrap-rotation.ps1',
                        '-AdmBaseUrl', 'http://127.0.0.1:8080',
                        '-ChannelCode', $admOperationalChannelCode) @{
                            CPF_ADM_BOOTSTRAP_SMOKE_PASSWORD = $admBootstrapPassword
                            CPF_ADM_SMOKE_PASSWORD = $admSmokePassword
                        })) { $allPassed = $false }

                    if (-not (Invoke-Unit-Stage 'LOCAL_FILE_LOG_STANDARD' @(
                        '-NoProfile', '-File', '.\cpf-tools\runtime\tools\smoke-file-log-standard-runtime.ps1',
                        '-Root', $RepoRoot, '-ReferenceBaseUrl', 'http://127.0.0.1:8080',
                        '-ResultDir', $fileLogEvidence, '-LogBasePath', $runtimeFileLogRoot,
                        '-SystemCode', $oneWasBusinessSystemCode,
                        # 1-WAS Local Module Catalog(CpfLocalRuntimeModules)에는 EDU 가 없다.
                        # 조립 안에 실제로 존재하는 무인증 @CpfOnlineTransaction 인 MBW_AUTH_LOGIN 으로
                        # 표준 File Log 22개 필드를 검증한다. 자격증명 없이도 같은 필드가 남으므로
                        # 업무 결과(401)는 허용한다.
                        '-ProbePath', '/api/v1/backoffice/auth/login',
                        '-ProbeOperationId', 'MBW_AUTH_LOGIN',
                        '-ProbeBody', '{"loginId":"cpf-file-log-probe","password":"cpf-file-log-probe"}',
                        '-ProbeExtraHeaders', 'Idempotency-Key=',
                        '-AllowNonSuccessProbe',
                        # 1-WAS 는 앱별이 아니라 Runtime 자신의 모듈 코드로 File Log 를 남긴다.
                        '-ProbeModuleCode', 'local-runtime',
                        '-ProbeDiagnosticPorts', '8080',
                        '-RequireRuntime') @{})) { $allPassed = $false }

                    if (-not (Invoke-Unit-Stage 'LOCAL_DB_LOG_POLICY_RUNTIME' @(
                        '-NoProfile', '-File', '.\cpf-tools\runtime\tools\smoke-log-policy-runtime.ps1',
                        '-Root', $RepoRoot, '-AdmBaseUrl', 'http://127.0.0.1:8080',
                        '-AdmUsername', 'admin', '-ChannelCode', $admOperationalChannelCode,
                        # DB Log Policy 는 @CpfOnlineTransaction 업무 거래에만 적용된다.
                        # ADM 은 Control Plane 이라 업무 거래 로그를 만들지 않으므로,
                        # File Log smoke 와 같이 1-WAS 조립 안의 MBW_AUTH_LOGIN 을 대상으로 쓴다.
                        '-TargetTransactionId', 'MBW_AUTH_LOGIN',
                        '-TargetSystemCode', $oneWasBusinessSystemCode,
                        '-TargetProbePath', '/api/v1/backoffice/auth/login',
                        '-TargetProbeBody', '{"loginId":"cpf-log-policy-probe","password":"cpf-log-policy-probe"}',
                        '-LogDir', $policyLogEvidence) $admSecretEnv)) { $allPassed = $false }

                    if (-not (Invoke-Unit-Stage 'LOCAL_INTEGRATED_LOG_CORRELATION' @(
                        '-NoProfile', '-File', '.\cpf-tools\runtime\tools\smoke-integrated-log-correlation.ps1',
                        '-Root', $RepoRoot, '-BaseUrl', 'http://127.0.0.1:8080',
                        '-LogBasePath', $runtimeFileLogRoot,
                        '-RuntimeLogRoot', (Join-Path $RepoRoot 'build\cpf-local-runtime\logs'),
                        '-FileLogResultPath', (Join-Path $fileLogEvidence 'file-log-standard-result.json'),
                        '-LogPolicyResultPath', (Join-Path $policyLogEvidence 'log-policy-runtime-smoke-result.json'),
                        '-AdmUsername', 'admin', '-SystemCode', $oneWasBusinessSystemCode,
                        '-AdmChannelCode', $admOperationalChannelCode,
                        # File Log smoke 와 같은 이유로 1-WAS 조립 안의 MBW_AUTH_LOGIN 을 쓴다.
                        '-ProbePath', '/api/v1/backoffice/auth/login',
                        '-ProbeOperationId', 'MBW_AUTH_LOGIN',
                        '-ProbeBody', '{"loginId":"cpf-file-log-probe","password":"cpf-file-log-probe"}',
                        '-ProbeExtraHeaders', 'Idempotency-Key=',
                        '-AllowNonSuccessProbe',
                        '-ResultPath', (Join-Path $integratedLogRoot 'integrated-log-correlation-result.json')) $admSecretEnv)) { $allPassed = $false }

                    foreach ($module in @('ADM', 'MBW')) {
                        if (-not (Invoke-Unit-Stage ($module + '_RUNTIME_OPENAPI_RELEASE') @(
                            '-NoProfile', '-File', '.\cpf-tools\contracts\openapi\verify-cpf-runtime-openapi-release.ps1',
                            '-Module', $module, '-BaseUrl', 'http://127.0.0.1:8080', '-Root', $RepoRoot,
                            '-EvidenceDirectory', (Join-Path $evidenceDir ('runtime-openapi\' + $module.ToLowerInvariant())),
                            '-SourceIdentity', $sourceIdentitySha1) @{})) { $allPassed = $false }
                    }
                }
            } finally {
                # 기동한 Runtime 은 어떤 실패 경로에서도 반드시 정지한다.
                if (-not (Invoke-Unit-Stage 'LOCAL_ONE_WAS_STOP' @(
                    '-NoProfile', '-File', '.\cpf-tools\runtime\tools\stop-cpf-local.ps1') @{})) { $allPassed = $false }
                if (Test-Path -LiteralPath $oneWasSecretDirectory -PathType Container) {
                    Remove-Item -LiteralPath $oneWasSecretDirectory -Recurse -Force -ErrorAction SilentlyContinue
                }
            }
        }
    } finally {
        if (Test-Path -LiteralPath $profilePath -PathType Leaf) {
            $cleanupOk = Invoke-Unit-Stage 'RUNTIME_DB_CLEANUP' @(
                '-NoProfile', '-File', '.\cpf-tools\db\verification\cleanup-cpf-local-runtime-db.ps1',
                '-ProfilePath', $profilePath, '-VerifierRunId', $runId) $prepEnv
            if (-not $cleanupOk) {
                # A run-scoped verifier schema that cannot be removed is a failed lifecycle, not
                # a harmless post-processing warning. Do not leave test data as a false PASS.
                $allPassed = $false
            }
        }
    }
}

$completedAt = Get-Date
$sourceIdentityAfter = (& python -B (Join-Path $RepoRoot 'cpf-tools\verification\tools\cpf-source-state.py') --root $RepoRoot --scope source | ConvertFrom-Json).contentSha256
if ($sourceIdentityAfter -ne $sourceIdentity) {
    $allPassed = $false
    Write-Line ("[FAIL] SOURCE_IDENTITY_AFTER changed before={0} after={1}" -f $sourceIdentity, $sourceIdentityAfter)
    $script:Steps += [ordered]@{ name = 'SOURCE_IDENTITY_AFTER'; status = 'FAIL'; exitCode = 1; seconds = 0 }
} else {
    Write-Line ("[PASS] SOURCE_IDENTITY_AFTER {0}" -f $sourceIdentityAfter)
    $script:Steps += [ordered]@{ name = 'SOURCE_IDENTITY_AFTER'; status = 'PASS'; exitCode = 0; seconds = 0 }
}
$total = ($completedAt - $startedAt).TotalSeconds
$overall = if ($allPassed) { 'PASS' } else { 'FAIL' }
Write-Line "=============================================================="
foreach ($step in $script:Steps) {
    Write-Line ("  {0,-32} {1,-5} rc={2} {3}s" -f $step.name, $step.status, $step.exitCode, $step.seconds)
}
Write-Line ("CPF_WORK_UNIT={0} unit={1} totalSeconds={2:n1} sourceIdentity={3}" -f $overall, $Unit, $total, $sourceIdentity)
Write-Line ("완료 : {0}" -f $completedAt.ToString('o'))
Write-Line ("Log  : {0}" -f $logPath)
Write-Line "=============================================================="

$summary = [ordered]@{
    unit = $Unit
    status = $overall
    sourceIdentity = $sourceIdentity
    sourceIdentityAfter = $sourceIdentityAfter
    startedAt = $startedAt.ToString('o')
    completedAt = $completedAt.ToString('o')
    totalSeconds = [math]::Round($total, 1)
    steps = $script:Steps
    evidenceDir = $evidenceDir
    logPath = $logPath
}
$summary | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $evidenceDir 'work-unit-result.json') -Encoding UTF8
if (-not $allPassed) { exit 1 }
