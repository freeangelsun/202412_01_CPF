param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $ModuleCode = "pym"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "create-domain-result.json"
$previewDir = Join-Path $Root "build/domain-generator/$ModuleCode"

function Save-Result {
    param([object] $Result)
    [System.IO.File]::WriteAllText($resultPath, ($Result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function Invoke-CreateDomain {
    param(
        [switch] $DryRun,
        [switch] $GeneratePatch
    )

    $arguments = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $Root "scripts/create-domain.ps1"),
        "-ModuleCode", $ModuleCode,
        "-ModuleName", "Payment",
        "-BasePackage", "cpf.$ModuleCode",
        "-TablePrefix", $ModuleCode
    )
    if ($DryRun) {
        $arguments += "-DryRun"
    }
    if ($GeneratePatch) {
        $arguments += "-GeneratePatch"
    }

    $output = & powershell @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "create-domain script failed. exitCode=$LASTEXITCODE"
    }
    return ([string]::Join("`n", @($output)) | ConvertFrom-Json)
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    moduleCode = $ModuleCode
    dryRun = [ordered]@{}
    generate = [ordered]@{}
    generatePatch = [ordered]@{}
    requiredFiles = @()
    requiredPatchFiles = @()
}

try {
    $result.dryRun = Invoke-CreateDomain -DryRun

    if (Test-Path -LiteralPath $previewDir) {
        Remove-Item -LiteralPath $previewDir -Recurse -Force
    }

    $result.generate = Invoke-CreateDomain

    $required = @(
        "build.gradle",
        "README.md",
        "src/main/resources/application.yml",
        "src/main/resources/application-${ModuleCode}.yml",
        "src/main/java/cpf/$ModuleCode/controller/PaymentController.java",
        "src/main/java/cpf/$ModuleCode/facade/PaymentFacade.java",
        "src/main/java/cpf/$ModuleCode/service/PaymentService.java",
        "src/main/java/cpf/$ModuleCode/repository/PaymentRepository.java",
        "src/main/java/cpf/$ModuleCode/dto/PaymentSearchRequest.java",
        "src/main/java/cpf/$ModuleCode/validation/PaymentSearchValidator.java",
        "src/test/java/cpf/$ModuleCode/service/PaymentServiceTest.java",
        "src/main/resources/mybatis/mapper/$ModuleCode/PaymentMapper.xml",
        "smoke/smoke-${ModuleCode}.ps1",
        "sql/Vxx__${ModuleCode}_domain.sql"
    )
    foreach ($relative in $required) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        $result.requiredFiles += [ordered]@{
            path = $path.Substring($Root.Length).TrimStart('\', '/')
            exists = $exists
        }
        if (-not $exists) {
            throw "create-domain generated file is missing. path=$path"
        }
    }

    if (Test-Path -LiteralPath $previewDir) {
        Remove-Item -LiteralPath $previewDir -Recurse -Force
    }
    $result.generatePatch = Invoke-CreateDomain -GeneratePatch

    foreach ($relative in $required) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        if (-not $exists) {
            throw "create-domain generate-patch base file is missing. path=$path"
        }
    }
    $requiredPatch = @(
        "patch-candidates/apply-order.md",
        "patch-candidates/settings.gradle.patch",
        "patch-candidates/sql/40_business_modules_schema.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/50_framework_seed.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/60_adm_seed.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/99_smoke_check.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/migration/Vxx__${ModuleCode}_domain.sql",
        "patch-candidates/smoke-${ModuleCode}.ps1"
    )
    foreach ($relative in $requiredPatch) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        $result.requiredPatchFiles += [ordered]@{
            path = $path.Substring($Root.Length).TrimStart('\', '/')
            exists = $exists
        }
        if (-not $exists) {
            throw "create-domain patch candidate is missing. path=$path"
        }
    }

    $result.status = $StatusDone
    $result.finishedAt = (Get-Date).ToString("o")
    Save-Result $result
    Write-Host "create-domain smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    $result.finishedAt = (Get-Date).ToString("o")
    Save-Result $result
    throw
}
