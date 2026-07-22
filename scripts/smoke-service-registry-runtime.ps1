param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "service-registry-runtime-result.json"

$StatusDone = "DONE"
$StatusFailed = "FAILED"

function Test-RequiredFile {
    param([string] $Path, [string] $Name)
    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "필수 파일이 없습니다. name=$Name path=$Path"
    }
    return [ordered]@{ name = $Name; path = $Path; exists = $true }
}

function Test-RequiredText {
    param([string] $Path, [string] $Text, [string] $Name)
    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "필수 파일이 없습니다. name=$Name path=$Path"
    }
    $content = [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
    if ($content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "필수 문자열이 없습니다. name=$Name path=$Path text=$Text"
    }
    return [ordered]@{ name = $Name; path = $Path; text = $Text; exists = $true }
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    validationMode = "source-sql-contract"
    checks = @()
}

try {
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java" "CPF_SERVICE_CALL_ENGINE"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceRegistry.java" "CPF_SERVICE_REGISTRY"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfEndpointRegistry.java" "CPF_ENDPOINT_REGISTRY"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceInstanceRegistry.java" "CPF_INSTANCE_REGISTRY"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfEndpointResolver.java" "CPF_ENDPOINT_RESOLVER"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfRoutingPolicyResolver.java" "CPF_ROUTING_RESOLVER"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfHealthAwareInstanceSelector.java" "CPF_HEALTH_AWARE_SELECTOR"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfRemoteFacadeProxySupport.java" "CPF_REMOTE_FACADE_PROXY_SUPPORT"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallLogWriter.java" "CPF_SERVICE_CALL_LOG_WRITER"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceHealthChecker.java" "CPF_SERVICE_HEALTH_CHECKER"
    $result.checks += Test-RequiredFile "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallProperties.java" "CPF_SERVICE_CALL_PROPERTIES"
    $result.checks += Test-RequiredText "cpf-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports" "CpfServiceCallAutoConfiguration" "CPF_SERVICE_CALL_AUTO_CONFIGURATION"
    $result.checks += Test-RequiredText "specs/sql/10_cpf_schema.sql" "CREATE TABLE IF NOT EXISTS cpf_service" "SQL_CPF_SERVICE"
    $result.checks += Test-RequiredText "specs/sql/10_cpf_schema.sql" "CREATE TABLE IF NOT EXISTS cpf_service_endpoint" "SQL_CPF_SERVICE_ENDPOINT"
    $result.checks += Test-RequiredText "specs/sql/10_cpf_schema.sql" "CREATE TABLE IF NOT EXISTS cpf_service_instance" "SQL_CPF_SERVICE_INSTANCE"
    $result.checks += Test-RequiredText "specs/sql/10_cpf_schema.sql" "CREATE TABLE IF NOT EXISTS cpf_service_call_history" "SQL_CPF_SERVICE_CALL_HISTORY"
    $result.checks += Test-RequiredText "specs/sql/50_framework_seed_data.sql" "MBR_API" "SQL_SERVICE_REGISTRY_SEED"
    $result.checks += Test-RequiredFile "specs/sql/migration/flyway/V21__cpf_service_call_registry.sql" "FLYWAY_CPF_SERVICE_CALL_REGISTRY"
    $result.status = $StatusDone
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
}

$result.finishedAt = (Get-Date).ToString("o")
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)

if ($result.status -eq $StatusFailed) {
    throw $result.error
}
Write-Host "service registry source/sql smoke passed. result=$resultPath"
