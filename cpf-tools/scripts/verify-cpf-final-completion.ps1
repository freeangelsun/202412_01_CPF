param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $SkipFrontend,
    [switch] $SkipRuntime,
    [switch] $RunDatabaseLifecycle,
    [string[]] $DatabaseProfilePath = @(),
    [switch] $RunGitHubGovernance
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'CPF 최종 검증은 pwsh 7 이상이 필요합니다.'
}

$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$gradle = if ($IsWindows) { '.\gradlew.bat' } else { './gradlew' }

function Invoke-CpfGate {
    param(
        [Parameter(Mandatory = $true)][string] $Name,
        [Parameter(Mandatory = $true)][scriptblock] $Body
    )
    Write-Host "==> $Name"
    & $Body
    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed (exit=$LASTEXITCODE)"
    }
}

function Assert-CpfRemovedPath {
    param([Parameter(Mandatory = $true)][string] $RelativePath)
    if (Test-Path -LiteralPath (Join-Path $RepoRoot $RelativePath)) {
        throw "Removed/obsolete path regression detected: $RelativePath"
    }
}

function Assert-CpfGeneratedDomainTopology {
    $fixedRoots = @(
        'cpf-core','cpf-common','cpf-admin','cpf-biz-admin','cpf-batch',
        'cpf-gateway','cpf-reference','cpf-tools','cpf-docs'
    )
    $settings = Get-Content -LiteralPath (Join-Path $RepoRoot 'settings.gradle') -Raw -Encoding UTF8
    $identities = [System.Collections.Generic.List[object]]::new()
    $candidates = @(Get-ChildItem -LiteralPath $RepoRoot -Directory -Filter 'cpf-*' |
        Where-Object { $_.Name -notin $fixedRoots })
    foreach ($candidate in $candidates) {
        $manifestPath = Join-Path $candidate.FullName 'manifest/domain-manifest.json'
        $ownershipPath = Join-Path $candidate.FullName 'manifest/generator-ownership.json'
        if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf) -or
                -not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
            throw "Unknown CPF root must have a Generator manifest pair: $($candidate.Name)"
        }
        try {
            $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 |
                ConvertFrom-Json -ErrorAction Stop
            $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 |
                ConvertFrom-Json -ErrorAction Stop
        } catch {
            throw "Generated Domain manifest JSON parse failed: $($candidate.Name) :: $($_.Exception.Message)"
        }
        if ([string]$manifest.domainType -cne 'GENERATED_DOMAIN' -or
                [string]$manifest.dependencyModel -cne 'root-project' -or
                [string]$ownership.dependencyModel -cne 'root-project') {
            throw "Generated Domain type/dependencyModel mismatch: $($candidate.Name)"
        }
        foreach ($propertyName in @(
                'projectName','moduleCode','moduleName','domainName',
                'systemCode','packageName','schemaName','tablePrefix')) {
            $manifestValue = [string]$manifest.$propertyName
            $ownershipValue = [string]$ownership.$propertyName
            if ([string]::IsNullOrWhiteSpace($manifestValue) -or $manifestValue -cne $ownershipValue) {
                throw "Generated Domain identity mismatch ($propertyName): $($candidate.Name)"
            }
        }
        if ([string]$manifest.projectName -cne $candidate.Name -or
                [string]$ownership.moduleDirectory -cne $candidate.Name -or
                [string]$ownership.outputDirectory -cne $candidate.Name) {
            throw "Generated Domain directory identity mismatch: $($candidate.Name)"
        }
        if ([string]$manifest.systemCode -cnotmatch '^[A-Z][A-Z0-9]{2}$' -or
                [string]$manifest.domainName -cnotmatch '^[a-z][a-z0-9]{1,29}$' -or
                [string]$manifest.packageName -cnotmatch '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$') {
            throw "Generated Domain canonical identity format is invalid: $($candidate.Name)"
        }
        $escapedProject = [regex]::Escape($candidate.Name)
        if ($settings -notmatch "(?m)^\s*include(?:\s*\()?[^`r`n]*['`"]:?$escapedProject['`"]") {
            throw "Generated Domain is not registered in settings.gradle: $($candidate.Name)"
        }
        $identities.Add([pscustomobject]@{
            projectName = $candidate.Name
            systemCode = [string]$manifest.systemCode
            packageName = [string]$manifest.packageName
        }) | Out-Null
    }
    foreach ($propertyName in @('systemCode','packageName')) {
        $duplicates = @($identities | Group-Object $propertyName | Where-Object Count -gt 1)
        if ($duplicates.Count -gt 0) {
            throw "Duplicate Generated Domain $propertyName`: $(($duplicates.Name | Sort-Object) -join ', ')"
        }
    }
}

Push-Location $RepoRoot
try {
    foreach ($obsolete in @(
        'cpf-tools/db/source',
        'cpf-tools/db/vendor/mysql',
        'cpf-tools/db/vendor/sqlserver',
        'cpf-tools/db/runtime-template/cpf/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bza/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bat/vendor/sqlserver'
    )) {
        Assert-CpfRemovedPath $obsolete
    }
    Assert-CpfGeneratedDomainTopology

    if (Test-Path -LiteralPath 'cpf-batch/src') {
        $legacyFiles = @(Get-ChildItem -LiteralPath 'cpf-batch/src' -Recurse -File -Force)
        if ($legacyFiles.Count -gt 0) {
            throw 'Legacy executable cpf-batch/src still contains files.'
        }
    }

    Invoke-CpfGate 'Enterprise QA closing static gate' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1 -Root $RepoRoot
    }
    Invoke-CpfGate 'Public API/SPI boundary' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-r11-public-boundary.ps1
    }

    Invoke-CpfGate 'Legacy BAT migration' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-legacy-batch-migration.ps1
    }
    Invoke-CpfGate 'Final source architecture gates' {
        & $gradle verifyCpfFinalSourceGates --no-daemon
    }
    Invoke-CpfGate 'Java 25 standard' {
        & $gradle checkJava25Standard --no-daemon
    }
    Invoke-CpfGate 'Spring Boot/CPF stack support' {
        & $gradle checkCpfStackSupport --no-daemon
    }
    Invoke-CpfGate 'Official DB vendor readiness' {
        & $gradle checkOfficialDbVendorReadiness --no-daemon
    }
    Invoke-CpfGate 'Runtime query contracts' {
        & $gradle checkRuntimeQueryContracts --no-daemon
    }
    Invoke-CpfGate 'Full CPF quality gate' {
        & $gradle qualityGate --no-daemon
    }
    Invoke-CpfGate 'Full Java tests and assemble' {
        & $gradle clean test assemble --no-daemon
    }
    Invoke-CpfGate 'Generated Domain federation static gate' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\generator\verify-domain-federation.ps1
    }
    Invoke-CpfGate 'Generated Domain golden path' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-generator-golden-path.ps1
    }
    Invoke-CpfGate 'Generated arbitrary-domain parity' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1
    }
    Invoke-CpfGate 'SQL canonical/static synchronization' {
        & $gradle checkSqlCanonical --no-daemon
    }

    if (-not $SkipFrontend) {
        Invoke-CpfGate 'ADM frontend' {
            & $gradle :cpf-admin:frontendVerify --no-daemon
        }
        Invoke-CpfGate 'BZA frontend' {
            & $gradle :cpf-biz-admin:frontendVerify --no-daemon
        }
        Invoke-CpfGate 'ADM browser/UI smoke' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-adm-ui.ps1
        }
        Invoke-CpfGate 'BZA browser/UI smoke' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-bza-ui.ps1
        }
    }

    if ($RunDatabaseLifecycle) {
        Invoke-CpfGate 'Official DB vendor readiness before lifecycle' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-official-db-vendor-readiness.ps1
        }
        Invoke-CpfGate 'Platform runtime query packs before lifecycle' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-platform-runtime-query-packs.ps1
        }
        Invoke-CpfGate 'BAT runtime query packs before lifecycle' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-bat-runtime-query-pack.ps1
        }

        if ($DatabaseProfilePath.Count -eq 0) {
            throw '-RunDatabaseLifecycle 사용 시 MariaDB/PostgreSQL/Oracle 각각의 실제 ProfilePath를 -DatabaseProfilePath로 전달해야 합니다.'
        }
        foreach ($profilePath in $DatabaseProfilePath) {
            $resolvedProfilePath = (Resolve-Path -LiteralPath $profilePath).Path
            $profileDocument = Get-Content -LiteralPath $resolvedProfilePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
            $profileVendors = @(
                $profileDocument.modules.PSObject.Properties |
                    Where-Object { [bool]$_.Value.enabled } |
                    ForEach-Object { ([string]$_.Value.vendor).Trim().ToLowerInvariant() } |
                    Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
                    Sort-Object -Unique
            )
            if ($profileVendors.Count -ne 1) {
                throw "DB lifecycle Profile은 단일 Vendor여야 합니다. profile=$resolvedProfilePath vendors=$($profileVendors -join ',')"
            }
            $profileVendor = $profileVendors[0]
            if ($profileVendor -notin @('mariadb', 'postgresql', 'oracle')) {
                throw "지원하지 않는 DB Vendor입니다. profile=$resolvedProfilePath vendor=$profileVendor"
            }

            Invoke-CpfGate "DB lifecycle [$profileVendor]: $resolvedProfilePath" {
                & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 `
                    -ProfilePath $resolvedProfilePath -All -RequireRun
            }

            if ($profileVendor -eq 'mariadb') {
                Invoke-CpfGate "MariaDB platform runtime query smoke: $resolvedProfilePath" {
                    & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-platform-runtime-query-packs-mariadb.ps1 `
                        -ProfilePath $resolvedProfilePath
                }
                Invoke-CpfGate "MariaDB BAT runtime query smoke: $resolvedProfilePath" {
                    & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-bat-runtime-query-pack-mariadb.ps1 `
                        -ProfilePath $resolvedProfilePath
                }
            } else {
                Invoke-CpfGate "Runtime query compile-smoke [$profileVendor]: $resolvedProfilePath" {
                    & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-platform-runtime-query-packs-official-db.ps1 `
                        -Vendor $profileVendor -ProfilePath $resolvedProfilePath
                }
            }
        }
    }

    if (-not $SkipRuntime) {
        Invoke-CpfGate 'Gateway + BAT runtime smoke' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-gateway-bat-runtime.ps1
        }
        Invoke-CpfGate 'Service Call failover runtime smoke' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-service-call-engine-failover-runtime.ps1
        }
        Invoke-CpfGate 'BAT local distributed topology' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
        }
        try {
            $registryPath = '.\build\bat-local-runtime\process-registry.json'
            if (-not (Test-Path -LiteralPath $registryPath)) {
                throw "BAT process registry not found: $registryPath"
            }
            $registry = Get-Content -LiteralPath $registryPath -Raw -Encoding UTF8 | ConvertFrom-Json
            foreach ($role in @('CONTROL_SERVER', 'SCHEDULER', 'WORKER', 'CENTER_CUT_RUNNER')) {
                if (@($registry | Where-Object { $_.role -eq $role }).Count -lt 2) {
                    throw "$role instance count < 2"
                }
            }
        } finally {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
        }
        Invoke-CpfGate 'BAT two-worker lease/drain/crash scenario' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-bat-two-worker-runtime.ps1
        }
    }

    if ($RunGitHubGovernance) {
        Invoke-CpfGate 'GitHub branch/source governance' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-github-governance.ps1
        }
    }

    $finalLedgerPath = Join-Path $RepoRoot 'cpf-tools/verification/20260729_02/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv'
    if (-not (Test-Path -LiteralPath $finalLedgerPath -PathType Leaf)) {
        throw "Merged full-QA ledger not found: $finalLedgerPath"
    }
    $finalLedgerRows = @(Import-Csv -LiteralPath $finalLedgerPath)
    $notCompleted = @($finalLedgerRows | Where-Object { $_.closing_status -ne '완료' })
    if ($notCompleted.Count -gt 0) {
        $statusSummary = ($notCompleted | Group-Object closing_status | Sort-Object Name | ForEach-Object {
            "$($_.Name)=$($_.Count)"
        }) -join ', '
        $sampleIds = @($notCompleted | Select-Object -First 20 -ExpandProperty id) -join ', '
        throw "Final completion is blocked because merged QA ledger has non-completed items. total=$($notCompleted.Count), status=[$statusSummary], sample=[$sampleIds]"
    }
    $missingClosingEvidence = @($finalLedgerRows | Where-Object { [string]::IsNullOrWhiteSpace($_.closing_evidence) })
    if ($missingClosingEvidence.Count -gt 0) {
        $sampleIds = @($missingClosingEvidence | Select-Object -First 20 -ExpandProperty id) -join ', '
        throw "Final completion is blocked because closing evidence is missing. total=$($missingClosingEvidence.Count), sample=[$sampleIds]"
    }

    Write-Host 'Selected CPF final completion gates PASS.'
    Write-Host 'Runtime/Browser/3-DB/multi-instance/release evidence is PASS only when the command actually ran on the current commit.'
} finally {
    Pop-Location
}
