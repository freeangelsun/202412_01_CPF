param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root "cpf-tools/scripts/database-profile-common.ps1")
$supportedVendors = @(Get-CpfSupportedDatabaseVendors)
$sandbox = Join-Path $Root "build/domain-db-bootstrap-static"
$sandboxRoot = Join-Path $sandbox "repository"
$allowedCleanupRoot = [IO.Path]::GetFullPath((Join-Path $Root "build"))
$Utf8NoBom = [Text.UTF8Encoding]::new($false)

function Assert-SafeSandboxPath([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith(
                $allowedCleanupRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
        throw "Domain DB static sandbox가 build 경로 밖을 가리킵니다: $resolved"
    }
}

Assert-SafeSandboxPath $sandbox
try {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $sandboxRoot | Out-Null
    [IO.File]::WriteAllText(
            (Join-Path $sandboxRoot "settings.gradle"),
            "rootProject.name = 'cpf-domain-db-bootstrap-static'`n",
            $Utf8NoBom)
    $gradleTarget = Join-Path $sandboxRoot "gradle"
    New-Item -ItemType Directory -Force -Path $gradleTarget | Out-Null
    Copy-Item `
        -LiteralPath (Join-Path $Root "gradle/cpf-stack.properties") `
        -Destination $gradleTarget

    $contractTarget = Join-Path $sandboxRoot "cpf-tools/generator/contracts"
    New-Item -ItemType Directory -Force -Path $contractTarget | Out-Null
    foreach ($contractName in @(
            "central-domain-template-contract.json",
            "domain-metadata.schema.json")) {
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/generator/contracts/$contractName") `
            -Destination $contractTarget
    }
    $vendorManifestTarget = Join-Path $sandboxRoot "cpf-tools/db"
    New-Item -ItemType Directory -Force -Path $vendorManifestTarget | Out-Null
    Copy-Item `
        -LiteralPath (Join-Path $Root "cpf-tools/db/vendor-pack-manifest.json") `
        -Destination $vendorManifestTarget

    $scriptTarget = Join-Path $sandboxRoot "cpf-tools/scripts"
    New-Item -ItemType Directory -Force -Path $scriptTarget | Out-Null
    foreach ($scriptName in @(
            "initialize-domain-database.ps1",
            "initialize-generated-domain-databases.ps1",
            "database-profile-common.ps1")) {
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/scripts/$scriptName") `
            -Destination $scriptTarget
    }

    foreach ($vendor in $supportedVendors) {
        $targetVendorRoot = Join-Path $sandboxRoot "cpf-tools/db/vendor/$vendor"
        New-Item -ItemType Directory -Force -Path $targetVendorRoot | Out-Null
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/db/vendor/$vendor/domain-template") `
            -Destination $targetVendorRoot `
            -Recurse
    }

    $caseIdentities = @(
        [ordered]@{ domain = "alpha"; code = "ALP" },
        [ordered]@{ domain = "bravo"; code = "BRV" },
        [ordered]@{ domain = "charlie"; code = "CHR" }
    )
    if ($supportedVendors.Count -gt $caseIdentities.Count) {
        throw "Generated Domain DB static case identity가 부족합니다: vendors=$($supportedVendors.Count)"
    }
    $cases = @(
        for ($index = 0; $index -lt $supportedVendors.Count; $index++) {
            [ordered]@{
                domain = $caseIdentities[$index].domain
                code = $caseIdentities[$index].code
                vendor = $supportedVendors[$index]
                port = 19001 + $index
            }
        }
    )
    $generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
    $initializer = Join-Path $scriptTarget "initialize-domain-database.ps1"
    foreach ($case in $cases) {
        & pwsh -NoProfile -File $generator `
            -Root $sandboxRoot `
            -DomainName $case.domain `
            -SystemCode $case.code `
            -DatabaseVendor $case.vendor `
            -Port $case.port `
            -Capabilities "database local-call" `
            -Apply | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain static 생성 실패: vendor=$($case.vendor)"
        }

        $resultDir = Join-Path $sandbox "result-$($case.vendor)"
        & pwsh -NoProfile -File $initializer `
            -Root $sandboxRoot `
            -DomainName $case.domain `
            -SystemCode $case.code `
            -DatabasePassword "CPF_STATIC_PLAN_MIGRATION" `
            -RuntimePassword "CPF_STATIC_PLAN_RUNTIME" `
            -AdminPassword "CPF_STATIC_PLAN_ADMIN" `
            -ResultDir $resultDir | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain DB static plan 실패: vendor=$($case.vendor)"
        }

        $result = Get-Content `
            -LiteralPath (Join-Path $resultDir "domain-db-init-result.json") `
            -Raw `
            -Encoding UTF8 | ConvertFrom-Json -Depth 50
        $phaseNames = @($result.phases | ForEach-Object { [string]$_.phase })
        if ($phaseNames.Count -ne 5 -or
                ($phaseNames -join ",") -ne "provision,principals,install,seed,verify") {
            throw "Generated Domain DB bootstrap phase 계약 불일치: vendor=$($case.vendor)"
        }
        $principalPath = Join-Path $resultDir `
                "rendered-sql/$($case.vendor)/principals-02_principals.sql"
        $principalText = Get-Content -LiteralPath $principalPath -Raw -Encoding UTF8
        if ($principalText.Contains("CPF_STATIC_PLAN_ADMIN") -or
                $principalText.Contains("CPF_STATIC_PLAN_MIGRATION") -or
                $principalText.Contains("CPF_STATIC_PLAN_RUNTIME") -or
                $principalText -match 'Cpf[A-Z]{3}(?:Mig|App)#2026' -or
                $principalText -notmatch "__CPF_SECRET_REDACTED__" -or
                $principalText -match "@CPF_[A-Z_]+@") {
            throw "Generated Domain principal static render가 Secret/token 보호 계약을 위반합니다: vendor=$($case.vendor)"
        }
        Write-Host "Generated Domain DB static plan PASS: vendor=$($case.vendor) phases=5 secretPersisted=false"
    }

    $batchInitializer = Join-Path $scriptTarget "initialize-generated-domain-databases.ps1"
    $secretEnvironment = @{
        CPF_DB_ROOT_PASSWORD = "CPF_STATIC_BATCH_ADMIN"
        CPF_DB_MIGRATION_PASSWORD = "CPF_STATIC_BATCH_MIGRATION"
        CPF_DB_APP_PASSWORD = "CPF_STATIC_BATCH_RUNTIME"
    }
    $previousEnvironment = @{}
    try {
        foreach ($entry in $secretEnvironment.GetEnumerator()) {
            $previousEnvironment[$entry.Key] = [Environment]::GetEnvironmentVariable($entry.Key)
            [Environment]::SetEnvironmentVariable($entry.Key, $entry.Value)
        }
        & pwsh -NoProfile -File $batchInitializer `
            -Root $sandboxRoot `
            -All | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain DB batch static plan 실패: exitCode=$LASTEXITCODE"
        }
    } finally {
        foreach ($entry in $previousEnvironment.GetEnumerator()) {
            [Environment]::SetEnvironmentVariable($entry.Key, $entry.Value)
        }
    }
    $batchResultPath = Join-Path $sandboxRoot `
            "build/db-install/generated-domains/generated-domain-batch-result.sanitized.json"
    $batchResult = Get-Content -LiteralPath $batchResultPath -Raw -Encoding UTF8 |
            ConvertFrom-Json -Depth 30
    if (@($batchResult.domains).Count -ne $supportedVendors.Count -or
            @($batchResult.domains | Where-Object status -ne "미검증").Count -gt 0) {
        throw "Generated Domain DB batch contract/version static 결과가 올바르지 않습니다."
    }
    Write-Host "Generated Domain DB batch contract PASS: contractVersion=current domains=$($supportedVendors.Count)"
} finally {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Assert-SafeSandboxPath $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}
