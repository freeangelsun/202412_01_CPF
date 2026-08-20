param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$requiredFiles = @(
    'cpf-tools\runtime\cpf-local-runtime\build.gradle',
    'cpf-tools\runtime\cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeApplication.java',
    'cpf-tools\runtime\cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeSafetyGuard.java',
    'cpf-tools\runtime\cpf-local-runtime\src\main\resources\application-local.yml',
    'cpf-tools\runtime\cpf-local-runtime\src\main\resources\application-local-minimal.yml',
    'cpf-tools\runtime\cpf-local-runtime\src\main\resources\application-local-standard.yml',
    'cpf-tools\runtime\cpf-local-runtime\src\main\resources\application-local-full.yml',
    'cpf-tools\runtime\cpf-local-runtime\src\main\resources\application-local-integration.yml',
    'cpf-tools\runtime\cpf-local-batch-runtime\build.gradle',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeApplication.java',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeSafetyGuard.java',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\resources\application-local-batch-minimal.yml',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\resources\application-local-batch-standard.yml',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\resources\application-local-batch-full.yml',
    'cpf-tools\runtime\cpf-local-batch-runtime\src\main\resources\application-local-batch-integration.yml',
    'cpf-tools\runtime\tools\start-cpf-local.ps1',
    'cpf-tools\runtime\tools\status-cpf-local.ps1',
    'cpf-tools\runtime\tools\stop-cpf-local.ps1'
)
foreach ($relative in $requiredFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $relative) -PathType Leaf)) {
        throw "Local runtime contract file missing: $relative"
    }
}
$settings = Get-Content -LiteralPath (Join-Path $Root 'settings.gradle') -Raw -Encoding UTF8
foreach ($module in @('cpf-local-runtime','cpf-local-batch-runtime')) {
    if ($settings -notmatch [regex]::Escape($module)) {
        throw "settings.gradle local module missing: $module"
    }
}
foreach ($mapping in @(
    "project(':runtime:local').projectDir = file('cpf-tools/runtime/cpf-local-runtime')",
    "project(':runtime:local-batch').projectDir = file('cpf-tools/runtime/cpf-local-batch-runtime')"
)) {
    if ($settings -notmatch [regex]::Escape($mapping)) {
        throw "settings.gradle local runtime physical mapping missing: $mapping"
    }
}
foreach ($legacyRoot in @('cpf-local-runtime','cpf-local-batch-runtime')) {
    if (Test-Path -LiteralPath (Join-Path $Root $legacyRoot)) {
        throw "Legacy local runtime module remains at repository root: $legacyRoot"
    }
}
$webGuard = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools\runtime\cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeSafetyGuard.java') -Raw -Encoding UTF8
$batchGuard = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools\runtime\cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeSafetyGuard.java') -Raw -Encoding UTF8
foreach ($marker in @('enabled=true','local Profile','Production/Stage','allow-remote-bind','127.0.0.1')) {
    if ($webGuard -notmatch [regex]::Escape($marker)) { throw "Web guard marker missing: $marker" }
}
foreach ($marker in @('enabled=true','local Profile','Production/Stage','allow-remote-bind')) {
    if ($batchGuard -notmatch [regex]::Escape($marker)) { throw "Batch guard marker missing: $marker" }
}
$batchLauncher = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools\runtime\cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeApplication.java') -Raw -Encoding UTF8
foreach ($marker in @('SpringApplicationBuilder','BatchControlPlaneApplication','BatchSchedulerApplication','BatchWorkerApplication','CenterCutApplication','BatchAgentApplication','CpfLocalBatchRuntimeSafetyGuard','closeReverse')) {
    if ($batchLauncher -notmatch [regex]::Escape($marker)) { throw "Local Batch launcher marker missing: $marker" }
}
if ($batchLauncher -match 'ComponentScan') {
    throw 'Local Batch Launcher가 여러 Batch Application을 단일 ComponentScan Context에 합치면 안 됩니다.'
}
$startScript = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools\runtime\tools\start-cpf-local.ps1') -Raw -Encoding UTF8
foreach ($marker in @("ValidateSet('integrated','minimal','standard','full','integration')",'local-batch-$Mode','healthUrls','Wait-CpfHealth',"'control-plane'","'agent'",'BatchControlPlanePort','BatchWorkerPort','AgentPort','-Xms','-Xmx')) {
    if ($startScript -notmatch [regex]::Escape($marker)) { throw "Local launcher marker missing: $marker" }
}
foreach ($retiredMarker in @('BatchControlServerApplication', "'control-server'", "'host-agent'", 'BatchControlPort', 'HostAgentPort', 'EnableHostAgent')) {
    if ($startScript -match [regex]::Escape($retiredMarker) -or $batchLauncher -match [regex]::Escape($retiredMarker)) {
        throw "Retired Local Batch contract remains: $retiredMarker"
    }
}
$legacyTwoLevelRoot = '$PSScriptRoot' + '\..' + '\..'
$retiredRootExpressions = @(
    '(Resolve-Path "' + $legacyTwoLevelRoot + '").Path',
    "(Resolve-Path (Join-Path `$PSScriptRoot '" + '../' + "..')).Path"
)
foreach ($scriptFile in @(Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-tools\runtime\tools') -Filter '*.ps1' -File)) {
    $scriptText = Get-Content -LiteralPath $scriptFile.FullName -Raw -Encoding UTF8
    foreach ($retiredExpression in $retiredRootExpressions) {
        if ($scriptText.Contains($retiredExpression)) {
            throw "Runtime Tool의 기본 Repository Root가 이전 물리 위치를 가리킵니다: $($scriptFile.Name)"
        }
    }
}

$canonicalDatabaseProfile = Join-Path $Root 'cpf-tools\db\config\database-install.default.json'
if (-not (Test-Path -LiteralPath $canonicalDatabaseProfile -PathType Leaf)) {
    throw 'Canonical database install profile missing: cpf-tools/db/config/database-install.default.json'
}
$canonicalDatabaseProfileCommon = Join-Path $Root 'cpf-tools\db\tools\database-profile-common.ps1'
if (-not (Test-Path -LiteralPath $canonicalDatabaseProfileCommon -PathType Leaf)) {
    throw 'Canonical database profile helper missing: cpf-tools/db/tools/database-profile-common.ps1'
}
$runtimeToolRoot = Join-Path $Root 'cpf-tools\runtime\tools'
foreach ($relative in @(
    'runtime-start-services.ps1',
    'smoke-bat-runtime-query-pack-mariadb.ps1',
    'smoke-platform-runtime-query-packs-mariadb.ps1'
)) {
    $scriptText = Get-Content -LiteralPath (Join-Path $runtimeToolRoot $relative) -Raw -Encoding UTF8
    if ($scriptText.Contains('cpf-tools\config\database-install.default.json')) {
        throw "Runtime Tool이 폐기된 DB Profile 경로를 사용합니다: $relative"
    }
    if (-not $scriptText.Contains('cpf-tools\db\config\database-install.default.json')) {
        throw "Runtime Tool의 canonical DB Profile 경로가 없습니다: $relative"
    }
}
foreach ($relative in @(
    'runtime-start-services.ps1',
    'smoke-bat-runtime-query-pack-mariadb.ps1',
    'smoke-platform-runtime-query-packs-mariadb.ps1',
    'smoke-platform-runtime-query-packs-official-db.ps1'
)) {
    $scriptText = Get-Content -LiteralPath (Join-Path $runtimeToolRoot $relative) -Raw -Encoding UTF8
    if (-not $scriptText.Contains('cpf-tools\db\tools\database-profile-common.ps1') -and
            -not $scriptText.Contains('cpf-tools/db/tools/database-profile-common.ps1')) {
        throw "Runtime Tool이 canonical DB Profile helper를 사용하지 않습니다: $relative"
    }
}

foreach ($consumer in @(
    [ordered]@{ path = (Join-Path $runtimeToolRoot 'runtime-start-services.ps1'); name = 'runtime-start-services.ps1'; parameter = 'Modules' },
    [ordered]@{ path = (Join-Path $runtimeToolRoot 'runtime-status.ps1'); name = 'runtime-status.ps1'; parameter = 'Modules' },
    [ordered]@{ path = (Join-Path $runtimeToolRoot 'runtime-diagnostics.ps1'); name = 'runtime-diagnostics.ps1'; parameter = 'DiagnosticsModules' },
    [ordered]@{ path = (Join-Path $runtimeToolRoot 'check-packaged-runtime-resources.ps1'); name = 'check-packaged-runtime-resources.ps1'; parameter = 'Modules' },
    [ordered]@{ path = (Join-Path $runtimeToolRoot 'smoke-runtime-closure.ps1'); name = 'smoke-runtime-closure.ps1'; parameter = 'Modules' },
    [ordered]@{ path = (Join-Path $Root 'cpf-tools\verification\openapi\smoke-openapi.ps1'); name = 'verification/openapi/smoke-openapi.ps1'; parameter = 'Modules' }
)) {
    $scriptText = Get-Content -LiteralPath $consumer.path -Raw -Encoding UTF8
    foreach ($retiredDomain in @('REF', 'MBR', 'EXS')) {
        if ($scriptText -match ('"' + $retiredDomain + '"')) {
            throw "Runtime Tool에 고정 Generated/Retired Domain literal이 남아 있습니다: file=$($consumer.name) code=$retiredDomain"
        }
    }
    $emptyDefaultPattern = '\[string\[\]\]\s+\$' + [regex]::Escape([string]$consumer.parameter) + '\s*=\s*@\(\s*\)'
    if ($scriptText -notmatch $emptyDefaultPattern) {
        throw "Runtime Tool의 기본 module 입력은 빈 배열이어야 합니다: $($consumer.name)"
    }
}

$runtimeStartText = Get-Content -LiteralPath (Join-Path $runtimeToolRoot 'runtime-start-services.ps1') -Raw -Encoding UTF8
foreach ($retiredGeneratedProfileMarker in @('databaseProfilePath', 'manifest/DB Profile', 'deploy/database/database-profile.json')) {
    if ($runtimeStartText.Contains($retiredGeneratedProfileMarker)) {
        throw "Runtime start가 Generated Project 내부의 폐기된 DB Profile을 사용합니다: $retiredGeneratedProfileMarker"
    }
}
foreach ($requiredGeneratedBindingMarker in @('CUSTOMER_BUSINESS_DB', '${prefix}_DATASOURCE_URL', 'deployment-environment')) {
    if (-not $runtimeStartText.Contains($requiredGeneratedBindingMarker)) {
        throw "Runtime start의 metadata 기반 Generated Domain DB binding marker가 없습니다: $requiredGeneratedBindingMarker"
    }
}

. (Join-Path $runtimeToolRoot 'runtime-common.ps1')
$resolvedDefaultModules = @(Resolve-CpfRuntimeModules -Modules @() -Root $Root)
$resolvedCodes = @($resolvedDefaultModules | ForEach-Object { [string]$_.module })
foreach ($platformCode in @('ADM', 'BAT', 'MBW', 'EDU', 'GWY')) {
    if ($resolvedCodes -notcontains $platformCode) {
        throw "Default Runtime inventory에 platform module이 없습니다: $platformCode"
    }
}
foreach ($generatedModule in @(Get-CpfGeneratedRuntimeModuleMap -Root $Root)) {
    if ($resolvedCodes -notcontains [string]$generatedModule.module) {
        throw "Metadata 기반 Generated Domain이 default Runtime inventory에 없습니다: $($generatedModule.module)"
    }
}
Write-Host '[PASS] Local Web 1 JVM/1 Port and role-isolated Local Batch single-JVM topology'
