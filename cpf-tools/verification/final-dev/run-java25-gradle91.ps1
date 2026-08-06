param(
  [Parameter(Mandatory=$true)][string]$ExpectedHead,
  [string]$RepoRoot=(Get-Location).Path,
  [string]$EvidenceDir="cpf-docs/work/v9i/fdr/r1/evidence/runtime/FDEV-004"
)
$ErrorActionPreference='Stop'
Set-Location $RepoRoot
$EvidencePath=Join-Path $RepoRoot $EvidenceDir
New-Item -ItemType Directory -Force $EvidencePath | Out-Null
$Head=(git rev-parse HEAD).Trim(); if($LASTEXITCODE-ne 0 -or $Head-ne $ExpectedHead){throw "Expected committed post-overlay SHA $ExpectedHead, actual $Head"}
if((git status --porcelain)){throw 'Working Tree must be clean before target-runtime execution.'}
$Java=(& java -version 2>&1 | Out-String); $Gradle=(& .\gradlew.bat --version 2>&1 | Out-String)
$Java|Set-Content (Join-Path $EvidencePath 'java-version.txt'); $Gradle|Set-Content (Join-Path $EvidencePath 'gradle-version.txt')
if($Java -notmatch 'version "25') {throw 'Java 25 is required'}
if($Gradle -notmatch 'Gradle 9\.1') {throw 'Gradle 9.1 is required'}
$PublicationTasks=if($env:CPF_PUBLICATION_TASKS){$env:CPF_PUBLICATION_TASKS -split '\s+'}else{@('publishAllPublicationsToCpfStagingRepository')}
$Tasks=@('clean','check')+$PublicationTasks
& .\gradlew.bat --no-daemon @Tasks 2>&1 | Tee-Object (Join-Path $EvidencePath 'build-test-publication.log')
if($LASTEXITCODE-ne 0){throw "Gradle gate failed: $LASTEXITCODE"}
git status --short --branch | Set-Content (Join-Path $EvidencePath 'git-status.txt')
