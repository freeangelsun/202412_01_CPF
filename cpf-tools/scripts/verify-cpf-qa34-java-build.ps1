[CmdletBinding()]
param(
    [string]$Root = (Get-Location).Path,
    [string]$EvidenceOutput,
    [switch]$KeepWorkspace
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$rootPath = (Resolve-Path $Root).Path
if (-not $EvidenceOutput) {
    $EvidenceOutput = Join-Path ([IO.Path]::GetTempPath()) ("cpf-qa34-java-build-{0}.sanitized.json" -f [guid]::NewGuid().ToString('N'))
}
$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') { throw 'QA34 Java build requires exact Git HEAD.' }
if ((& git -C $rootPath status --porcelain=v1 | Out-String).Trim()) { throw 'QA34 Java build requires a clean Working Tree.' }
$javaText = (& java -version 2>&1 | Out-String)
if ($javaText -notmatch '(?m)version\s+"25(?:\.|"|\s)') { throw 'QA34 Java build requires Java 25.' }
$started = [DateTimeOffset]::UtcNow
$work = Join-Path ([IO.Path]::GetTempPath()) ("cpf-qa34-java-{0}" -f [guid]::NewGuid().ToString('N'))
$gradleHome = Join-Path $work 'gradle-user-home'
$localRepo = Join-Path $work 'cpf-local-repository'
$stagingRepo = Join-Path $rootPath 'build/cpf-artifact-staging/repository'
New-Item -ItemType Directory -Force -Path $gradleHome,$localRepo | Out-Null
$results = [System.Collections.Generic.List[object]]::new()
function Invoke-Step([string]$Name,[string[]]$Arguments,[string]$WorkingDirectory=$rootPath) {
    $stepStart=[DateTimeOffset]::UtcNow
    $stdout=Join-Path $work "$Name.stdout.log"; $stderr=Join-Path $work "$Name.stderr.log"
    $psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=(Join-Path $rootPath 'gradlew.bat');$psi.WorkingDirectory=$WorkingDirectory
    $psi.UseShellExecute=$false;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
    $psi.Environment['GRADLE_USER_HOME']=$gradleHome
    $psi.Environment['CPF_LOCAL_ARTIFACT_REPOSITORY']=$localRepo
    $psi.Environment['CPF_SOURCE_SHA']=$sourceSha
    foreach($arg in $Arguments){[void]$psi.ArgumentList.Add($arg)}
    $p=[Diagnostics.Process]::Start($psi);$out=$p.StandardOutput.ReadToEnd();$err=$p.StandardError.ReadToEnd();$p.WaitForExit()
    [IO.File]::WriteAllText($stdout,$out,[Text.UTF8Encoding]::new($false));[IO.File]::WriteAllText($stderr,$err,[Text.UTF8Encoding]::new($false))
    $record=[ordered]@{name=$Name;command="gradlew.bat $($Arguments -join ' ')";startedAt=$stepStart.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=$p.ExitCode;stdoutSha256=(Get-FileHash $stdout -Algorithm SHA256).Hash.ToLowerInvariant();stderrSha256=(Get-FileHash $stderr -Algorithm SHA256).Hash.ToLowerInvariant()}
    $results.Add($record)
    if($p.ExitCode-ne0){throw "$Name failed (exit=$($p.ExitCode))"}
}
try {
    Invoke-Step 'help' @('help','--no-daemon','--stacktrace')
    Invoke-Step 'projects' @('projects','--no-daemon','--stacktrace')
    Invoke-Step 'aggregate-quality-build' @('aggregateQualityBuild','--no-daemon','--max-workers=1','--stacktrace')
    Invoke-Step 'publish-staging-artifacts' @('publishCpfStagingPlatformArtifacts','--no-daemon','--max-workers=1','--stacktrace')
    $qa39Tool = Join-Path $rootPath 'cpf-tools/scripts/Qa39Tool.java'
    & java $qa39Tool 'build-contract' '--root' $rootPath
    if($LASTEXITCODE-ne0){throw 'Canonical build contract gate failed'}
    & (Join-Path $rootPath 'cpf-tools/scripts/verify-local-artifact-propagation.ps1') -Root $rootPath -LocalRepository $stagingRepo
    if($LASTEXITCODE-ne0){throw 'Staging artifact propagation gate failed'}
    $finalSha=(& git -C $rootPath rev-parse HEAD).Trim();if($finalSha-ne$sourceSha){throw 'Source SHA changed during Java build'}
    if((& git -C $rootPath status --porcelain=v1 | Out-String).Trim()){throw 'Source tree changed during Java build'}
    $evidence=[ordered]@{schemaVersion=2;evidenceId='QA34-JAVA25-FRESH-CACHE';sourceSha=$sourceSha;resultSha=$sourceSha;branch=(& git -C $rootPath branch --show-current).Trim();sourceDirty=$false;profile='QA34_RELEASE';os=[Environment]::OSVersion.ToString();javaVersion=($javaText.Trim());gradleUserHomeMode='EMPTY_TEMP';cpfLocalRepositoryMode='EMPTY_TEMP';startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=0;requirements=@('QA34-REQ-001','QA34-REQ-002','QA34-REQ-003');results=$results;sanitized=$true;releaseEligible=$true}
    New-Item -ItemType Directory -Force -Path (Split-Path $EvidenceOutput) | Out-Null
    [IO.File]::WriteAllText($EvidenceOutput,($evidence|ConvertTo-Json -Depth 12)+"`n",[Text.UTF8Encoding]::new($false))
    Write-Host "[CPF][QA34][PASS] Java25 fresh-cache build evidence=$EvidenceOutput"
} finally { if(-not $KeepWorkspace){Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $work} }
