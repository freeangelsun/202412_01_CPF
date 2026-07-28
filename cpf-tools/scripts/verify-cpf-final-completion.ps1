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

Push-Location $RepoRoot
try {
    foreach ($obsolete in @(
        'cpf-external',
        'cpf-tools/db/source',
        'cpf-tools/db/vendor/mysql',
        'cpf-tools/db/vendor/sqlserver',
        'cpf-tools/db/runtime-template/cpf/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bza/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bat/vendor/sqlserver'
    )) {
        Assert-CpfRemovedPath $obsolete
    }

    if (Test-Path -LiteralPath 'cpf-batch/src') {
        $legacyFiles = @(Get-ChildItem -LiteralPath 'cpf-batch/src' -Recurse -File -Force)
        if ($legacyFiles.Count -gt 0) {
            throw 'Legacy executable cpf-batch/src still contains files.'
        }
    }

    Invoke-CpfGate 'Enterprise QA closing static gate' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
    }
    Invoke-CpfGate 'Runtime Control public API boundary' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-runtime-control-public-boundary.ps1
    }
    Invoke-CpfGate 'Notification authentication and portable SQL' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-notification-portable-sql.ps1
    }
    Invoke-CpfGate 'Local development runtime topology' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-local-runtime-topology.ps1
    }

    Invoke-CpfGate 'Enterprise QA closing static gate' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1 -Root $RepoRoot
    }
    Invoke-CpfGate 'ADM Runtime Capability consumer gate' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-runtime-capability-consumers.ps1 -Root $RepoRoot
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
    Invoke-CpfGate 'Generated Domain create/build/remove lifecycle' {
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-generated-domain-lifecycle.ps1
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
        Invoke-CpfGate 'Database canonical artifact synchronization' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1
        }
        Invoke-CpfGate 'Official DB vendor readiness after synchronization' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-official-db-vendor-readiness.ps1
        }
        Invoke-CpfGate 'Platform runtime query packs after synchronization' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-platform-runtime-query-packs.ps1
        }
        Invoke-CpfGate 'BAT runtime query packs after synchronization' {
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

    Write-Host 'Selected CPF final completion gates PASS.'
    Write-Host 'Runtime/Browser/3-DB/multi-instance/release evidence is PASS only when the command actually ran on the current commit.'
} finally {
    Pop-Location
}
