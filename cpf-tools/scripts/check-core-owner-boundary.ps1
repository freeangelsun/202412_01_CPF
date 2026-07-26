param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()
function Fail([string]$m) { $failures.Add($m) }

$adminJava = Join-Path $Root 'cpf-admin/src/main/java'
if (Test-Path $adminJava) {
    $directBatDb = Get-ChildItem $adminJava -Recurse -File -Filter *.java | Select-String -Pattern 'batJdbcTemplate|mbrJdbcTemplate|refJdbcTemplate|accJdbcTemplate|@Qualifier\("(?:bat|mbr|ref|acc)JdbcTemplate"\)|\bFROM\s+(?:bat|mbr|ref|acc)_|\bUPDATE\s+(?:bat|mbr|ref|acc)_|\bINSERT\s+INTO\s+(?:bat|mbr|ref|acc)_|\bDELETE\s+FROM\s+(?:bat|mbr|ref|acc)_' -CaseSensitive:$false
    foreach ($m in $directBatDb) { Fail "ADM cross-owner DB access: $($m.Path):$($m.LineNumber)" }
}

$batchJavaRoots = @(
    'cpf-batch/control-server/src/main/java',
    'cpf-batch/scheduler/src/main/java',
    'cpf-batch/worker/src/main/java',
    'cpf-batch/center-cut-runner/src/main/java',
    'cpf-batch/host-agent/src/main/java',
    'cpf-batch/runtime-common/src/main/java',
    'cpf-batch/contract/src/main/java'
)
foreach ($relativeRoot in $batchJavaRoots) {
    $batchJava = Join-Path $Root $relativeRoot
    if (-not (Test-Path $batchJava -PathType Container)) { continue }
    $legacyRuntime = Get-ChildItem $batchJava -Recurse -File -Filter *.java |
        Select-String -Pattern 'com\.cpf\.core\.common\.batch\.(CpfBatchFileLogWriter|CpfBatchGhostDetectionService|CpfBatchHeartbeatService|CpfBatchLauncher|CpfBatchLockManager|CpfBatchLoggingEventPublisher|CpfBatchOperationRepository|CpfBatchRuntimeListener|CpfBatchRuntimeProgress)|com\.cpf\.core\.common\.batch\.centercut\.CpfCenterCutService'
    foreach ($m in $legacyRuntime) { Fail "BAT still imports Core-owned runtime compatibility type: $($m.Path):$($m.LineNumber)" }
}

foreach ($rel in @('cpf-core/src/main/java/com/cpf/core/config/CpfBatchAutoConfiguration.java','cpf-core/src/main/java/com/cpf/core/config/CpfCenterCutAutoConfiguration.java')) {
    $p = Join-Path $Root $rel
    if (Test-Path $p) {
        $t = Get-Content $p -Raw
        if ($t -notmatch 'legacy-batch-runtime-enabled') { Fail "Core compatibility config has no explicit legacy opt-in: $rel" }
        if ($t -match 'matchIfMissing\s*=\s*true') { Fail "Core legacy batch runtime is enabled by default: $rel" }
    }
}

$coreBuild = Join-Path $Root 'cpf-core/build.gradle'
if (Test-Path $coreBuild) {
    $t = Get-Content $coreBuild -Raw
    if ($t -match 'project\([''"]:cpf-batch[''"]\)') { Fail 'cpf-core must not depend on cpf-batch.' }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "Core/BAT owner boundary gate failed: $($failures.Count) issue(s)."
}
Write-Host 'Core/BAT owner boundary gate PASS.'
