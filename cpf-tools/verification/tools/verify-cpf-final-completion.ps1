param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $SkipFrontend,
    [switch] $SkipRuntime,
    [switch] $RunDatabaseLifecycle,
    [string[]] $DatabaseProfilePath = @(),
    [switch] $RunGitHubGovernance,
    [string] $ExpectedSourceSha = '',
    [switch] $RequireFullCompletion
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'CPF 최종 검증은 pwsh 7 이상이 필요합니다.'
}

$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$headSha = (& git -C $RepoRoot rev-parse HEAD).Trim().ToLowerInvariant()
if ([string]::IsNullOrWhiteSpace($ExpectedSourceSha)) { $ExpectedSourceSha = $headSha }
if ($ExpectedSourceSha -notmatch '^[0-9a-fA-F]{40}$') { throw "ExpectedSourceSha must be a full SHA: $ExpectedSourceSha" }
$ExpectedSourceSha = $ExpectedSourceSha.ToLowerInvariant()
if ($ExpectedSourceSha -ne $headSha) {
    & git -C $RepoRoot merge-base --is-ancestor $ExpectedSourceSha $headSha
    if ($LASTEXITCODE -ne 0) { throw "ExpectedSourceSha is not an ancestor of HEAD: expected=$ExpectedSourceSha head=$headSha" }
    $nonEvidenceChanges = @(& git -C $RepoRoot diff --name-only "$ExpectedSourceSha..$headSha" | Where-Object {
        $_ -notmatch '^(cpf-docs/evidence/|cpf-docs/work/(current|handover|state)/|cpf-tools/verification/)'
    })
    if ($nonEvidenceChanges.Count -gt 0) {
        throw "HEAD contains source changes after ExpectedSourceSha: $($nonEvidenceChanges -join ', ')"
    }
}
$gradle = if ($IsWindows) { '.\gradlew.bat' } else { './gradlew' }

if ($RequireFullCompletion) {
    if ($SkipFrontend) { throw '-RequireFullCompletion에서는 -SkipFrontend를 사용할 수 없습니다.' }
    if ($SkipRuntime) { throw '-RequireFullCompletion에서는 -SkipRuntime을 사용할 수 없습니다.' }
    if (-not $RunDatabaseLifecycle) { throw '-RequireFullCompletion에서는 -RunDatabaseLifecycle이 필수입니다.' }
    if (-not $RunGitHubGovernance) { throw '-RequireFullCompletion에서는 -RunGitHubGovernance가 필수입니다.' }
    if ($DatabaseProfilePath.Count -ne 3) {
        throw '-RequireFullCompletion에서는 MariaDB/PostgreSQL/Oracle ProfilePath 3개가 모두 필요합니다.'
    }
    $status = (& git -C $RepoRoot status --porcelain=v1)
    if ($LASTEXITCODE -ne 0) { throw 'git status 확인에 실패했습니다.' }
    if (@($status).Count -gt 0) { throw '전체 완료 검증은 Clean Working Tree에서만 수행할 수 있습니다.' }
}

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
    $surfacePolicyPath = Join-Path $RepoRoot 'cpf-tools/governance/cpf-product-surface-policy.json'
    if (-not (Test-Path -LiteralPath $surfacePolicyPath -PathType Leaf)) {
        throw "Product surface policy is missing: $surfacePolicyPath"
    }
    $surfacePolicy = Get-Content -LiteralPath $surfacePolicyPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 20
    $fixedRoots = @($surfacePolicy.moduleOwners |
        Where-Object {
            [string]$_.prefix -match '^cpf-[^/]+/$' -and
            [string]$_.owner -ne 'generated-domain'
        } |
        ForEach-Object { ([string]$_.prefix).TrimEnd('/') } |
        Sort-Object -Unique)
    if ($fixedRoots.Count -eq 0) {
        throw 'Product surface policy has no fixed CPF roots.'
    }
    . (Join-Path $RepoRoot 'cpf-tools/generator/tools/generated-domain-common.ps1')
    $definitions = @(Get-CpfGeneratedDomainInventory -Root $RepoRoot)
    if ($definitions.Count -eq 0) {
        throw 'Canonical Generated Domain definition/output이 없습니다.'
    }
    $canonicalProjects = @($definitions | ForEach-Object { [string]$_.projectName } | Sort-Object -Unique)
    $candidates = @(Get-ChildItem -LiteralPath $RepoRoot -Directory -Filter 'cpf-*' |
        Where-Object { $_.Name -notin $fixedRoots })
    $unexpected = @($candidates | Where-Object { $_.Name -notin $canonicalProjects })
    if ($unexpected.Count -gt 0) {
        throw "Unknown CPF root has no canonical cpf-domain.yaml definition: $(($unexpected.Name | Sort-Object) -join ', ')"
    }

    $settings = Get-Content -LiteralPath (Join-Path $RepoRoot 'settings.gradle') -Raw -Encoding UTF8
    foreach ($token in @('cpf-tools/generator/definitions', 'cpfIncludeGeneratedDomains', 'includeBuild(canonical)')) {
        if (-not $settings.Contains($token)) {
            throw "Generated Domain dynamic composite settings contract is missing: $token"
        }
    }

    $identities = [System.Collections.Generic.List[object]]::new()
    foreach ($definition in $definitions) {
        $projectPath = Join-Path $RepoRoot ([string]$definition.projectName)
        if (-not [bool]$definition.exists -or
                -not (Test-Path -LiteralPath $projectPath -PathType Container)) {
            throw "Canonical Generated Domain output is missing: $($definition.projectName)"
        }
        if ([string]$definition.generatedProjectMetadata -cne 'NONE' -or
                @($definition.forbiddenPermanentMetadata).Count -ne 0) {
            throw "Generated Domain permanent metadata is forbidden: $($definition.projectName)"
        }
        if ([string]$definition.systemCode -cnotmatch '^[A-Z][A-Z0-9]{2}$' -or
                [string]$definition.domainName -cnotmatch '^[a-z][a-z0-9-]{1,49}$' -or
                [string]$definition.packageName -cnotmatch '^[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+$') {
            throw "Generated Domain canonical identity format is invalid: $($definition.projectName)"
        }
        $verification = Invoke-CpfCanonicalCli -Root $RepoRoot -Arguments @(
            'verify', 'domain', '--file', ([string]$definition.definitionPath), '--output', $projectPath)
        if ([string]$verification.status -cne 'PASS') {
            throw "Generated Domain canonical verification failed: $($definition.projectName)"
        }
        $identities.Add([pscustomobject]@{
            projectName = [string]$definition.projectName
            systemCode = [string]$definition.systemCode
            packageName = [string]$definition.packageName
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
    Invoke-CpfGate 'P00 split logical master integrity and execution scope' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-split-master-dataset.ps1 `
            -Root $RepoRoot -ScopeLimit 10027 `
            -JsonOutput .\build\reports\cpf\split-master-validation.json
    }
    foreach ($obsolete in @(
        'cpf-tools/db/source',
        'cpf-tools/db/vendor/mysql',
        'cpf-tools/db/vendor/sqlserver',
        'cpf-tools/db/runtime-template/cpf/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bza/vendor/sqlserver',
        'cpf-tools/db/runtime-template/bat/vendor/sqlserver',
        'cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRoute.java',
        'cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java',
        'cpf-core/src/test/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalogTest.java'
    )) {
        Assert-CpfRemovedPath $obsolete
    }
    Assert-CpfGeneratedDomainTopology

    Invoke-CpfGate 'P02 Core/Admin/BAT owner boundaries' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-owner-boundaries.ps1 `
            -Root $RepoRoot `
            -JsonOutput .\build\reports\cpf\owner-boundaries.json
    }

    Invoke-CpfGate 'P03 DB-less and product persistence fail-closed' {
        & pwsh -NoProfile -File .\cpf-tools\db\verification\verify-cpf-db-less-fail-closed.ps1 `
            -Root $RepoRoot `
            -JsonOutput .\build\reports\cpf\db-less-fail-closed.json
    }

    Invoke-CpfGate 'P03 transaction identity and execution ID truth' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\check-transaction-id-standard.ps1 `
            -Root $RepoRoot `
            -JsonOutput .\build\reports\cpf\transaction-id-standard.json
    }

    Invoke-CpfGate 'P03 shared outbound network policy consumers' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-network-policy-consumers.ps1 `
            -ProjectRoot $RepoRoot `
            -JsonOutput .\build\reports\cpf\network-policy-consumers.json
    }

    Invoke-CpfGate 'P03 durable audit reservation and recovery' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-audit-fail-closed.ps1 `
            -ProjectRoot $RepoRoot `
            -JsonOutput .\build\reports\cpf\audit-fail-closed.json
    }

    Invoke-CpfGate 'P03 server-owned operator trust boundary' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-operator-trust-boundary.ps1 `
            -ProjectRoot $RepoRoot `
            -JsonOutput .\build\reports\cpf\operator-trust-boundary.json
    }

    Invoke-CpfGate 'P05 canonical starter catalog and derivative truth' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-cpf-starter-catalog-truth.ps1 `
            -Root $RepoRoot `
            -JsonOutput .\build\reports\cpf\starter-catalog-truth.json
    }

    Invoke-CpfGate 'P04 official DB vendor manifest truth' {
        & pwsh -NoProfile -File .\cpf-tools\db\verification\verify-cpf-db-vendor-manifest.ps1 `
            -Root $RepoRoot `
            -JsonOutput .\build\reports\cpf\db-vendor-manifest.json
    }

    Invoke-CpfGate 'QA31 request/result/source integrity gate' {
        & pwsh -NoProfile -File .\cpf-tools\scripts\verify-cpf-qa31-development-result.ps1 `
            -Root $RepoRoot -BaseSha '9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e' `
            -RequireExactHeadEvidence:$RequireFullCompletion `
            -RequireIntegratedClosure:$RequireFullCompletion
    }

    Invoke-CpfGate 'QA30 canonical/runtime/navigation static gate' {
        & python .\cpf-tools\scripts\verify-qa30-completion.py --root $RepoRoot --scope $RepoRoot `
            --basis-sha $ExpectedSourceSha `
            --report .\build\reports\cpf\qa30-static-gate.json
    }
    Invoke-CpfGate 'Canonical DB lifecycle contract' {
        & pwsh -NoProfile -File .\cpf-tools\db\verification\check-canonical-db-lifecycle-contract.ps1 -Root $RepoRoot
    }

    Invoke-CpfGate 'Work/Handover/Evidence exact-SHA' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\check-work-context-sha.ps1 -ExpectedSha $ExpectedSourceSha -RequireCurrentEvidence
    }

    if (Test-Path -LiteralPath 'cpf-batch/src') {
        $legacyFiles = @(Get-ChildItem -LiteralPath 'cpf-batch/src' -Recurse -File -Force)
        if ($legacyFiles.Count -gt 0) {
            throw 'Legacy executable cpf-batch/src still contains files.'
        }
    }

    Invoke-CpfGate 'Enterprise QA closing static gate' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\check-enterprise-qa-closing.ps1 -Root $RepoRoot
    }
    Invoke-CpfGate 'Public API/SPI boundary' {
        & pwsh -NoProfile -File .\cpf-tools\scripts\check-r11-public-boundary.ps1
    }

    Invoke-CpfGate 'Legacy BAT migration' {
        & pwsh -NoProfile -File .\cpf-tools\db\verification\check-legacy-batch-migration.ps1
    }
    Invoke-CpfGate 'Spring Batch Control Plane fencing and reconciliation' {
        & python .\cpf-tools\scripts\verify-cpf-qa33-batch-control-plane.py --root $RepoRoot `
            --json-report .\build\reports\cpf\qa33-batch-control-plane.json
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
    Invoke-CpfGate 'Requirement/Matrix/Evidence semantic gate' {
        & pwsh -NoProfile -File .\cpf-tools\verification\tools\check-current-requirement-evidence-consistency.ps1 -Root $RepoRoot -ExpectedSha $ExpectedSourceSha
    }
    Invoke-CpfGate 'Full Java tests and assemble' {
        & $gradle clean test assemble --no-daemon
    }
    Invoke-CpfGate 'Generated Domain federation static gate' {
        & pwsh -NoProfile -File .\cpf-tools\generator\verify-domain-federation.ps1
    }
    Invoke-CpfGate 'Generated Domain golden path' {
        & pwsh -NoProfile -File .\cpf-tools\generator\verification\check-generator-golden-path.ps1
    }
    Invoke-CpfGate 'Generated arbitrary-domain parity' {
        & pwsh -NoProfile -File .\cpf-tools\generator\verification\check-generator-arbitrary-domain-parity.ps1
    }
    Invoke-CpfGate 'Canonical generator Java template compile' {
        & python .\cpf-tools\generator\verification\verify-cpf-generator-java-template-compile.py $RepoRoot
    }
    Invoke-CpfGate 'Generated Domain idempotency and three-DB lifecycle templates' {
        & python .\cpf-tools\generator\verification\verify-cpf-generator-idempotency-templates.py $RepoRoot
    }
    Invoke-CpfGate 'SQL canonical/static synchronization' {
        & $gradle checkSqlCanonical --no-daemon
    }

    if (-not $SkipFrontend) {
        Invoke-CpfGate 'ADM route source consumer and generated operation closure' {
            & python .\cpf-tools\verification\tools\verify-cpf-adm-route-source-consumers.py --root $RepoRoot
        }
        Invoke-CpfGate 'ADM frontend' {
            & $gradle :cpf-admin:frontendVerify --no-daemon
        }
        Invoke-CpfGate 'BZA frontend' {
            & $gradle :cpf-backoffice/online:frontendVerify --no-daemon
        }
        Invoke-CpfGate 'ADM browser/UI smoke' {
            & pwsh -NoProfile -File .\cpf-tools\verification\tools\smoke-adm-ui.ps1
        }
        Invoke-CpfGate 'BZA browser/UI smoke' {
            & pwsh -NoProfile -File .\cpf-tools\verification\tools\smoke-backoffice-ui.ps1
        }
    }

    if ($RunDatabaseLifecycle) {
        Invoke-CpfGate 'Official DB vendor readiness before lifecycle' {
            & pwsh -NoProfile -File .\cpf-tools\db\verification\check-official-db-vendor-readiness.ps1
        }
        Invoke-CpfGate 'Platform runtime query packs before lifecycle' {
            & pwsh -NoProfile -File .\cpf-tools\runtime\tools\check-platform-runtime-query-packs.ps1
        }
        Invoke-CpfGate 'BAT runtime query packs before lifecycle' {
            & pwsh -NoProfile -File .\cpf-tools\runtime\tools\check-bat-runtime-query-pack.ps1
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
                & pwsh -NoProfile -File .\cpf-tools\db\tools\initialize-cpf-database.ps1 `
                    -ProfilePath $resolvedProfilePath -All -RequireRun
            }

            if ($profileVendor -eq 'mariadb') {
                Invoke-CpfGate "MariaDB platform runtime query smoke: $resolvedProfilePath" {
                    & pwsh -NoProfile -File .\cpf-tools\runtime\tools\smoke-platform-runtime-query-packs-mariadb.ps1 `
                        -ProfilePath $resolvedProfilePath
                }
                Invoke-CpfGate "MariaDB BAT runtime query smoke: $resolvedProfilePath" {
                    & pwsh -NoProfile -File .\cpf-tools\runtime\tools\smoke-bat-runtime-query-pack-mariadb.ps1 `
                        -ProfilePath $resolvedProfilePath
                }
            } else {
                Invoke-CpfGate "Runtime query compile-smoke [$profileVendor]: $resolvedProfilePath" {
                    & pwsh -NoProfile -File .\cpf-tools\db\verification\smoke-platform-runtime-query-packs-official-db.ps1 `
                        -Vendor $profileVendor -ProfilePath $resolvedProfilePath
                }
            }
        }
    }

    if (-not $SkipRuntime) {
        Invoke-CpfGate 'Gateway + BAT runtime smoke' {
            & pwsh -NoProfile -File .\cpf-tools\runtime\tools\smoke-gateway-bat-runtime.ps1
        }
        Invoke-CpfGate 'Service Call failover runtime smoke' {
            & pwsh -NoProfile -File .\cpf-tools\runtime\tools\smoke-service-call-engine-failover-runtime.ps1
        }
        Invoke-CpfGate 'BAT local distributed topology' {
            & pwsh -NoProfile -File .\cpf-tools\verification\tools\start-bat-local-distributed.ps1
        }
        try {
            $registryPath = '.\build\bat-local-runtime\process-registry.json'
            if (-not (Test-Path -LiteralPath $registryPath)) {
                throw "BAT process registry not found: $registryPath"
            }
            $registry = Get-Content -LiteralPath $registryPath -Raw -Encoding UTF8 | ConvertFrom-Json
            foreach ($role in @('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT')) {
                if (@($registry | Where-Object { $_.role -eq $role }).Count -lt 2) {
                    throw "$role instance count < 2"
                }
            }
        } finally {
            & pwsh -NoProfile -File .\cpf-tools\verification\tools\stop-bat-local-distributed.ps1
        }
        Invoke-CpfGate 'BAT two-worker lease/drain/crash scenario' {
            & pwsh -NoProfile -File .\cpf-tools\runtime\tools\smoke-bat-two-worker-runtime.ps1
        }
    }

    if ($RunGitHubGovernance) {
        Invoke-CpfGate 'GitHub branch/source governance' {
            & pwsh -NoProfile -File .\cpf-tools\governance\tools\verify-github-governance.ps1
        }
    }

    Write-Host '[INFO] Developer closing gates passed. QA/Codex final status is managed by the current role-specific result documents.'`n`n    if ($RequireFullCompletion) {
        Write-Host "CPF FULL FRAMEWORK COMPLETION GATE PASS. sourceSha=$ExpectedSourceSha headSha=$headSha"
    } else {
        Write-Host "Selected CPF verification gates PASS. sourceSha=$ExpectedSourceSha headSha=$headSha"
        Write-Host '선택 검증 결과는 전체 Framework 완료를 의미하지 않습니다. 전체 완료 판정에는 -RequireFullCompletion을 사용해야 합니다.'
    }
    Write-Host 'Runtime/Browser/3-DB/multi-instance/release evidence is PASS only when the command actually ran on the current commit.'
} finally {
    Pop-Location
}
