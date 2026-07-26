param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
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

    $contractTarget = Join-Path $sandboxRoot "cpf-tools/generator/contracts"
    New-Item -ItemType Directory -Force -Path $contractTarget | Out-Null
    foreach ($contractName in @(
            "central-domain-template-contract.json",
            "domain-metadata.schema.json")) {
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/generator/contracts/$contractName") `
            -Destination $contractTarget
    }

    $scriptTarget = Join-Path $sandboxRoot "cpf-tools/scripts"
    New-Item -ItemType Directory -Force -Path $scriptTarget | Out-Null
    foreach ($scriptName in @(
            "initialize-domain-database.ps1",
            "database-profile-common.ps1")) {
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/scripts/$scriptName") `
            -Destination $scriptTarget
    }

    foreach ($vendor in @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")) {
        $targetVendorRoot = Join-Path $sandboxRoot "cpf-tools/db/vendor/$vendor"
        New-Item -ItemType Directory -Force -Path $targetVendorRoot | Out-Null
        Copy-Item `
            -LiteralPath (Join-Path $Root "cpf-tools/db/vendor/$vendor/domain-template") `
            -Destination $targetVendorRoot `
            -Recurse
    }

    $cases = @(
        [ordered]@{ domain = "alpha"; code = "ALP"; vendor = "mariadb"; port = 19001 },
        [ordered]@{ domain = "bravo"; code = "BRV"; vendor = "mysql"; port = 19002 },
        [ordered]@{ domain = "charlie"; code = "CHR"; vendor = "postgresql"; port = 19003 },
        [ordered]@{ domain = "delta"; code = "DLT"; vendor = "oracle"; port = 19004 },
        [ordered]@{ domain = "echoes"; code = "ECH"; vendor = "sqlserver"; port = 19005 }
    )
    $generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
    $initializer = Join-Path $scriptTarget "initialize-domain-database.ps1"
    foreach ($case in $cases) {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File $generator `
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
        & pwsh -NoProfile -ExecutionPolicy Bypass -File $initializer `
            -Root $sandboxRoot `
            -DomainName $case.domain `
            -SystemCode $case.code `
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
                $principalText -match 'Cpf[A-Z]{3}(?:Mig|App)#2026' -or
                $principalText -notmatch "__CPF_SECRET_REDACTED__" -or
                $principalText -match "@CPF_[A-Z_]+@") {
            throw "Generated Domain principal static render가 Secret/token 보호 계약을 위반합니다: vendor=$($case.vendor)"
        }
        Write-Host "Generated Domain DB static plan PASS: vendor=$($case.vendor) phases=5 secretPersisted=false"
    }
} finally {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Assert-SafeSandboxPath $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}

