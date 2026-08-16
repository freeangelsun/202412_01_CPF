[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/quality-gate"),
    [string] $BaselineSha = "bd7bbfccc720e8703f3073eafb32705f97ef168b"
)
$Utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding=$Utf8; [Console]::OutputEncoding=$Utf8; $OutputEncoding=$Utf8
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
if(-not [IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}
$failures=[Collections.Generic.List[string]]::new()
function Fail([string]$m){$failures.Add($m)|Out-Null}
function RequireFile([string]$r){if(-not(Test-Path -LiteralPath (Join-Path $Root $r) -PathType Leaf)){Fail "필수 파일 누락: $r"}}
function RequireDir([string]$r){if(-not(Test-Path -LiteralPath (Join-Path $Root $r) -PathType Container)){Fail "필수 디렉터리 누락: $r"}}

$expected=@(':runtime:batch:api',':runtime:batch:runtime-support',':runtime:batch:runtime',':runtime:batch:control-plane',':runtime:batch:scheduler',':runtime:batch:worker',':runtime:batch:center-cut',':runtime:batch:agent',':runtime:batch:testkit')
$expected|ForEach-Object{RequireDir ($_.TrimStart(':').Replace(':','/'))}
$required=@(
 'cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeRegistration.java',
 'cpf-batch/runtime-support/src/main/java/com/cpf/batch/runtime/RuntimeCommonConfiguration.java',
 'cpf-batch/runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java',
 'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/BatchControlPlaneApplication.java',
 'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java',
 'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchSchedulerApplication.java',
 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchWorkerApplication.java',
 'cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/CenterCutApplication.java',
 'cpf-batch/agent/src/main/java/com/cpf/batch/agent/BatchAgentApplication.java',
 'cpf-batch/testkit/src/main/java/com/cpf/batch/testkit/RuntimeRegistrationFixture.java',
 'cpf-education/src/main/java/com/cpf/education/batch/support/config/EducationBatchEducationConfig.java',
 'cpf-education/src/main/java/com/cpf/education/batch/support/controller/EducationBatchEducationController.java',
 'cpf-education/src/main/java/com/cpf/education/batch/support/operation/SpringBatchEduBusinessConsumer.java'
)
$required|ForEach-Object{RequireFile $_}
$settings=Join-Path $Root 'settings.gradle'
if(Test-Path $settings){$t=[IO.File]::ReadAllText($settings,[Text.Encoding]::UTF8); foreach($p in $expected){if($t -notmatch [regex]::Escape($p)){Fail "settings.gradle Batch project 누락: $p"}}}else{Fail 'settings.gradle 누락'}
$retired=@('cpf-batch/api','cpf-batch/runtime-support','cpf-batch/runtime','cpf-batch/control-plane','cpf-batch/center-cut','cpf-batch/agent','cpf-reference')
foreach($r in $retired){if(Test-Path -LiteralPath (Join-Path $Root $r)){Fail "Retired 경로 잔존: $r"}}
# 퇴역 Batch project가 물리 삭제되었더라도 Consumer build.gradle에 남아 있으면 Root configuration이 즉시 실패합니다.
$retiredBatchProjects = @(':runtime:batch:api', ':runtime:batch:runtime-support', ':runtime:batch:runtime', ':runtime:batch:control-plane', ':runtime:batch:center-cut', ':runtime:batch:agent')
$dependencyPattern = '^\s*(?:api|implementation|compileOnly|runtimeOnly|testImplementation|testCompileOnly|testRuntimeOnly|annotationProcessor)\s+project\(\s*["''](?<project>:cpf-batch:[^"'']+)["'']\s*\)'
Get-ChildItem -LiteralPath $Root -Recurse -File -Filter 'build.gradle' -ErrorAction SilentlyContinue | ForEach-Object {
    $relative = $_.FullName.Substring($Root.Length + 1).Replace('\\','/')
    foreach ($line in [IO.File]::ReadAllLines($_.FullName, [Text.Encoding]::UTF8)) {
        $match = [regex]::Match($line, $dependencyPattern)
        if ($match.Success -and $retiredBatchProjects -contains $match.Groups['project'].Value) {
            Fail "퇴역 Batch project Consumer 잔존: $relative -> $($match.Groups['project'].Value)"
        }
    }
}
$eduRoot=Join-Path $Root 'cpf-education/src'
if(Test-Path $eduRoot){Get-ChildItem $eduRoot -Recurse -File -Filter '*.java'|ForEach-Object{$s=[IO.File]::ReadAllText($_.FullName,[Text.Encoding]::UTF8);if($s -match 'import\s+com\.cpf\.batch\.(execution|control|scheduler|worker|centercut|agent)'){Fail "EDU가 Batch Runtime 구현을 직접 import: $($_.FullName.Substring($Root.Length+1))"}}}
New-Item -ItemType Directory -Force -Path $ResultDir|Out-Null
$result=[ordered]@{generatedAt=[DateTimeOffset]::Now.ToString('o');status=if($failures.Count-eq 0){'완료'}else{'실패'};baselineSha=$BaselineSha;expectedProjectCount=$expected.Count;requiredFileCount=$required.Count;failures=@($failures)}
[IO.File]::WriteAllText((Join-Path $ResultDir 'legacy-batch-migration-result.sanitized.json'),($result|ConvertTo-Json -Depth 8),$Utf8)
if($failures.Count){$failures|ForEach-Object{Write-Error $_}; throw "Batch canonical migration gate failed: $($failures.Count)"}
Write-Host "CPF_BATCH_CANONICAL_MIGRATION_GATE=PASS projects=$($expected.Count)"
