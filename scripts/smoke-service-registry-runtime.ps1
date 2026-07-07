param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

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
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallEngine.java" "PFW_SERVICE_CALL_ENGINE"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceRegistry.java" "PFW_SERVICE_REGISTRY"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfEndpointRegistry.java" "PFW_ENDPOINT_REGISTRY"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceInstanceRegistry.java" "PFW_INSTANCE_REGISTRY"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfEndpointResolver.java" "PFW_ENDPOINT_RESOLVER"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfRoutingPolicyResolver.java" "PFW_ROUTING_RESOLVER"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfHealthAwareInstanceSelector.java" "PFW_HEALTH_AWARE_SELECTOR"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfRemoteFacadeProxySupport.java" "PFW_REMOTE_FACADE_PROXY_SUPPORT"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallLogWriter.java" "PFW_SERVICE_CALL_LOG_WRITER"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceHealthChecker.java" "PFW_SERVICE_HEALTH_CHECKER"
    $result.checks += Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallProperties.java" "PFW_SERVICE_CALL_PROPERTIES"
    $result.checks += Test-RequiredText "pfw/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports" "CpfServiceCallAutoConfiguration" "PFW_SERVICE_CALL_AUTO_CONFIGURATION"
    $result.checks += Test-RequiredText "specs/sql/10_pfw_schema.sql" "CREATE TABLE IF NOT EXISTS pfw_service" "SQL_PFW_SERVICE"
    $result.checks += Test-RequiredText "specs/sql/10_pfw_schema.sql" "CREATE TABLE IF NOT EXISTS pfw_service_endpoint" "SQL_PFW_SERVICE_ENDPOINT"
    $result.checks += Test-RequiredText "specs/sql/10_pfw_schema.sql" "CREATE TABLE IF NOT EXISTS pfw_service_instance" "SQL_PFW_SERVICE_INSTANCE"
    $result.checks += Test-RequiredText "specs/sql/10_pfw_schema.sql" "CREATE TABLE IF NOT EXISTS pfw_service_call_history" "SQL_PFW_SERVICE_CALL_HISTORY"
    $result.checks += Test-RequiredText "specs/sql/50_framework_seed_data.sql" "MBR_API" "SQL_SERVICE_REGISTRY_SEED"
    $result.checks += Test-RequiredFile "specs/sql/migration/flyway/V21__pfw_service_call_registry.sql" "FLYWAY_PFW_SERVICE_CALL_REGISTRY"
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
