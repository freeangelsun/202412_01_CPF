param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$requiredFiles = @(
    'cpf-local-runtime\build.gradle',
    'cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeApplication.java',
    'cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeSafetyGuard.java',
    'cpf-local-runtime\src\main\resources\application-local.yml',
    'cpf-local-runtime\src\main\resources\application-local-minimal.yml',
    'cpf-local-runtime\src\main\resources\application-local-standard.yml',
    'cpf-local-runtime\src\main\resources\application-local-full.yml',
    'cpf-local-runtime\src\main\resources\application-local-integration.yml',
    'cpf-local-batch-runtime\build.gradle',
    'cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeApplication.java',
    'cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeSafetyGuard.java',
    'cpf-tools\scripts\start-cpf-local.ps1',
    'cpf-tools\scripts\status-cpf-local.ps1',
    'cpf-tools\scripts\stop-cpf-local.ps1'
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
$webGuard = Get-Content -LiteralPath (Join-Path $Root 'cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeSafetyGuard.java') -Raw -Encoding UTF8
$batchGuard = Get-Content -LiteralPath (Join-Path $Root 'cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeSafetyGuard.java') -Raw -Encoding UTF8
foreach ($marker in @('enabled=true','local Profile','Production/Stage','allow-remote-bind','127.0.0.1')) {
    if ($webGuard -notmatch [regex]::Escape($marker)) { throw "Web guard marker missing: $marker" }
}
foreach ($marker in @('enabled=true','local Profile','Production/Stage','allow-remote-bind')) {
    if ($batchGuard -notmatch [regex]::Escape($marker)) { throw "Batch guard marker missing: $marker" }
}
$batchLauncher = Get-Content -LiteralPath (Join-Path $Root 'cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeApplication.java') -Raw -Encoding UTF8
foreach ($marker in @('SpringApplicationBuilder','BatchControlServerApplication','BatchSchedulerApplication','BatchWorkerApplication','CpfLocalBatchRuntimeSafetyGuard','closeReverse')) {
    if ($batchLauncher -notmatch [regex]::Escape($marker)) { throw "Local Batch launcher marker missing: $marker" }
}
if ($batchLauncher -match 'ComponentScan') {
    throw 'Local Batch Launcher가 여러 Batch Application을 단일 ComponentScan Context에 합치면 안 됩니다.'
}
$startScript = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools\scripts\start-cpf-local.ps1') -Raw -Encoding UTF8
foreach ($marker in @("ValidateSet('minimal','standard','full','integration')",'BatchControlPort','BatchWorkerPort','-Xms','-Xmx')) {
    if ($startScript -notmatch [regex]::Escape($marker)) { throw "Local launcher marker missing: $marker" }
}
Write-Host '[PASS] Local Web 1 JVM/1 Port and role-isolated Local Batch single-JVM topology'
