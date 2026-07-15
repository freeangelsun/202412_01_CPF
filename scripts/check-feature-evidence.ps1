param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260715_01")
)

$ErrorActionPreference = "Stop"
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param([string] $Id, [bool] $Passed, [string] $Evidence)
    $checks.Add([ordered]@{ id = $Id; passed = $Passed; evidence = $Evidence }) | Out-Null
}

function Test-File {
    param([string] $RelativePath)
    return Test-Path -LiteralPath (Join-Path $Root $RelativePath) -PathType Leaf
}

function Test-Text {
    param([string] $RelativePath, [string] $Pattern)
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        return $false
    }
    $text = [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
    return [Regex]::IsMatch($text, $Pattern)
}

$settingsText = [IO.File]::ReadAllText((Join-Path $Root "settings.gradle"), [Text.Encoding]::UTF8)
Add-Check "BASELINE_MODULES" `
    ($settingsText.Contains("include 'pfw', 'cmn', 'mbr', 'xyz', 'adm', 'bza', 'bat'")) `
    "settings.gradle"
Add-Check "REMOVED_MODULE_DIRECTORIES" `
    (-not (Test-Path (Join-Path $Root "acc")) -and -not (Test-Path (Join-Path $Root "exs"))) `
    "acc, exs"
Add-Check "PFW_STANDARD_EXECUTION" `
    ((Test-File "pfw/src/main/java/cpf/pfw/common/execution/CpfOnlineTransaction.java") -and
     (Test-File "pfw/src/main/java/cpf/pfw/common/execution/CpfBatchJob.java") -and
     (Test-File "pfw/src/main/java/cpf/pfw/common/execution/CpfExecutionCatalogScanner.java")) `
    "pfw/common/execution"
Add-Check "PFW_PASSWORD_PORT" `
    ((Test-File "pfw/src/main/java/cpf/pfw/common/security/password/CpfPasswordHashingPort.java") -and
     (Test-File "pfw/src/main/java/cpf/pfw/common/security/password/CpfPbkdf2PasswordHasher.java")) `
    "pfw/common/security/password"
Add-Check "ADM_STANDARD_EXECUTION_API" `
    (Test-File "adm/src/main/java/cpf/adm/opr/controller/AdmStandardExecutionController.java") `
    "AdmStandardExecutionController.java"
Add-Check "PFW_REMOTE_LOG_PORT" `
    ((Test-File "pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogArtifactPort.java") -and
     (Test-File "pfw/src/main/java/cpf/pfw/common/remotelog/LocalCpfRemoteLogArtifactAdapter.java") -and
     (Test-File "adm/src/main/java/cpf/adm/opr/controller/AdmRemoteLogController.java")) `
    "PFW remote log port and ADM API"
Add-Check "BZA_APPLICATION" `
    ((Test-File "bza/src/main/java/cpf/bza/BzaApplication.java") -and
     (Test-File "bza/src/main/resources/static/bza/index.html")) `
    "bza application and UI"
Add-Check "BZA_AUTHORIZATION" `
    ((Test-File "bza/src/main/java/cpf/bza/auth/filter/BzaApiAuthFilter.java") -and
     (Test-Text "bza/src/main/java/cpf/bza/auth/service/BzaAuthService.java" "authorize\s*\(")) `
    "BZA server authorization"
Add-Check "BZA_APPROVAL" `
    ((Test-File "bza/src/main/java/cpf/bza/backoffice/service/BzaBackofficeService.java") -and
     (Test-Text "specs/sql/40_business_modules_schema.sql" "bza_approval_document")) `
    "BZA approval source and SQL"
Add-Check "BZA_FLYWAY" `
    ((Test-File "specs/sql/migration/flyway/V29__bza_backoffice_baseline.sql") -and
     (Test-File "specs/sql/migration/flyway/V30__baseline_service_registry.sql")) `
    "V29 and V30"
Add-Check "XYZ_SERVICE_CALL_EDU" `
    ((Test-File "xyz/src/main/java/cpf/xyz/edu/servicecall/XyzServiceCallEngineEducationSample.java") -and
     (Test-File "xyz/src/test/java/cpf/xyz/edu/servicecall/XyzServiceCallEngineEducationSampleTest.java")) `
    "XYZ Service Call EDU"
Add-Check "BAT_OPTIONAL_RUNTIME" `
    ((Test-File "bat/build.gradle") -and (Test-File "deploy/env/local-bat.env")) `
    "BAT optional runtime"
Add-Check "INSTALL_SQL" `
    ((Test-File "specs/sql/00_all_install.sql") -and
     (Test-File "specs/sql/00_all_install_and_smoke.sql")) `
    "all-in-one SQL"

$failed = @($checks | Where-Object { -not $_.passed })
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    status = if ($failed.Count -eq 0) { "DONE" } else { "FAILED" }
    checkCount = $checks.Count
    failureCount = $failed.Count
    checks = $checks
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "feature-evidence-result.sanitized.json"
[IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    [Text.UTF8Encoding]::new($false)
)

if ($failed.Count -gt 0) {
    $failedIds = ($failed | ForEach-Object { $_.id }) -join ", "
    throw "CPF 핵심 기능 증적 검사가 실패했습니다: $failedIds"
}
Write-Host "CPF 핵심 기능 증적 검사 통과: $($checks.Count)건"
