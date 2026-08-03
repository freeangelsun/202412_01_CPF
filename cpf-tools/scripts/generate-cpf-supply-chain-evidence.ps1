[CmdletBinding()]
param(
    [string]$Root='.',
    [Alias('OutputDir')][string]$EvidenceDir,
    [string[]]$ArtifactPaths=@(),
    [switch]$KeepStage
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path $Root).Path
$sourceSha=(& git -C $rootPath rev-parse HEAD).Trim()
if($LASTEXITCODE-ne0 -or $sourceSha-notmatch'^[0-9a-f]{40}$'){throw'exact Git SHA required'}
if((& git -C $rootPath status --porcelain=v1 --untracked-files=all|Out-String).Trim()){throw'clean tree required'}
$started=[DateTimeOffset]::UtcNow
$stage=if($EvidenceDir){[IO.Path]::GetFullPath($EvidenceDir)}else{Join-Path ([IO.Path]::GetTempPath()) ("cpf-supply-chain-{0}"-f[guid]::NewGuid().ToString('N'))}
New-Item -ItemType Directory -Force $stage|Out-Null
function Tool([string]$Name){$c=Get-Command $Name -ErrorAction SilentlyContinue;if(-not$c){throw"$Name executable is required"};$c.Source}
function Run([string]$Name,[string]$Exe,[string[]]$Args){Write-Host"[CPF][SUPPLY] START $Name";&$Exe @Args;if($LASTEXITCODE-ne0){throw"$Name failed (exit=$LASTEXITCODE)"};Write-Host"[CPF][SUPPLY] PASS $Name"}
function FileHash([string]$Path){(Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()}
function TreeHash([string]$Path){
 if(Test-Path -LiteralPath $Path -PathType Leaf){return FileHash $Path}
 $base=(Resolve-Path $Path).Path;$lines=@(Get-ChildItem -LiteralPath $base -Recurse -File|Sort-Object FullName|ForEach-Object{"$(FileHash $_.FullName)  $([IO.Path]::GetRelativePath($base,$_.FullName).Replace('\','/'))"})
 if($lines.Count-eq0){throw"empty artifact: $Path"};$tmp=[IO.Path]::GetTempFileName();try{[IO.File]::WriteAllLines($tmp,$lines,[Text.UTF8Encoding]::new($false));FileHash $tmp}finally{Remove-Item -Force $tmp -ErrorAction SilentlyContinue}
}
$gradle=Join-Path $rootPath 'gradlew.bat';$ort=Tool'ort';$syft=Tool'syft';$grype=Tool'grype';$java=Tool'java'
try{
 Run'CycloneDX'$gradle @('cyclonedxBom','--no-daemon','--stacktrace')
 if($ArtifactPaths.Count-eq0){
  $ArtifactPaths=@(Get-ChildItem -Recurse -File -Path @((Join-Path $rootPath 'cpf-*/build/libs/*.jar'),(Join-Path $rootPath 'cpf-*/build/libs/*.war'),(Join-Path $rootPath 'cpf-*/build/distributions/*.zip')) -ErrorAction SilentlyContinue|Where-Object{$_.Name-notmatch'(-plain|-sources|-javadoc)\.'}|ForEach-Object{$_.FullName};Get-ChildItem -Directory -Path @((Join-Path $rootPath 'cpf-admin/frontend/dist'),(Join-Path $rootPath 'cpf-biz-admin/frontend/dist')) -ErrorAction SilentlyContinue|ForEach-Object{$_.FullName})
 }
 $ArtifactPaths=@($ArtifactPaths|ForEach-Object{(Resolve-Path $_).Path}|Sort-Object -Unique);if($ArtifactPaths.Count-eq0){throw'No final deployable artifact found'}
 $ortDir=Join-Path $stage 'ort';Run'ORT analyze'$ort @('analyze','-i',$rootPath,'-o',$ortDir)
 $analyzer=Get-ChildItem -Recurse -File $ortDir|Where-Object{$_.Name-match'analyzer-result.*\.(yml|yaml|json)$'}|Select-Object -First 1;if(-not$analyzer){throw'ORT analyzer result missing'}
 $rules=Join-Path $rootPath 'cpf-tools/supply-chain/ort/evaluator.rules.kts';if(-not(Test-Path $rules)){throw'ORT evaluator rules missing'}
 $evalDir=Join-Path $stage 'ort-evaluation';Run'ORT evaluate'$ort @('evaluate','-i',$analyzer.FullName,'-o',$evalDir,'--rules-file',$rules)
 $evaluation=Get-ChildItem -Recurse -File $evalDir|Where-Object{$_.Name-match'evaluation-result.*\.(yml|yaml|json)$'}|Select-Object -First 1;if(-not$evaluation){throw'ORT evaluation result missing'}
 $reportDir=Join-Path $stage 'ort-report';Run'ORT report'$ort @('report','-i',$evaluation.FullName,'-o',$reportDir,'-f','WebApp,NoticeTemplate')
 $records=@();$index=0
 foreach($artifact in $ArtifactPaths){
  $index++;$safe=(([IO.Path]::GetFileName($artifact))-replace'[^A-Za-z0-9._-]','_');if(-not$safe){$safe="artifact-$index"}
  $sbom=Join-Path $stage "$index-$safe.syft.cdx.json";$vuln=Join-Path $stage "$index-$safe.grype.json"
  Run"Syft $safe"$syft @($artifact,'-o',"cyclonedx-json=$sbom");Run"Grype $safe"$grype @("sbom:$sbom",'-o','json','--file',$vuln,'--fail-on','high')
  Run"License $safe"$java @((Join-Path $rootPath 'cpf-tools/scripts/Qa39Tool.java'),'supply-chain','--root',$rootPath,'--sbom',$sbom)
  $records+=[ordered]@{sourceSha=$sourceSha;artifactPath=[IO.Path]::GetRelativePath($rootPath,$artifact).Replace('\','/');artifactSha256=TreeHash $artifact;sbomPath=[IO.Path]::GetFileName($sbom);sbomSha256=FileHash $sbom;vulnerabilityReportPath=[IO.Path]::GetFileName($vuln);vulnerabilityReportSha256=FileHash $vuln}
 }
 if((& git -C $rootPath rev-parse HEAD).Trim()-ne$sourceSha){throw'SHA changed during supply-chain scan'}
 if((& git -C $rootPath status --porcelain=v1 --untracked-files=all|Out-String).Trim()){throw'Source changed during supply-chain scan'}
 $e=[ordered]@{schemaVersion=3;evidenceId='QA34-SUPPLY-CHAIN';sourceSha=$sourceSha;resultSha=$sourceSha;sourceDirty=$false;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=0;requirements=@('QA34-REQ-018');tools=@{ort=(& $ort version 2>&1|Out-String).Trim();syft=(& $syft version 2>&1|Out-String).Trim();grype=(& $grype version 2>&1|Out-String).Trim()};ortAnalyzerSha256=FileHash $analyzer.FullName;ortEvaluationSha256=FileHash $evaluation.FullName;artifacts=$records;sanitized=$true;releaseEligible=$true}
 $indexPath=Join-Path $stage 'CPF_QA34_SUPPLY_CHAIN_EVIDENCE_INDEX.sanitized.json';[IO.File]::WriteAllText($indexPath,($e|ConvertTo-Json -Depth 12)+"`n",[Text.UTF8Encoding]::new($false));$hash=FileHash $indexPath;[IO.File]::WriteAllText("$indexPath.sha256","$hash  $([IO.Path]::GetFileName($indexPath))`n",[Text.UTF8Encoding]::new($false));Write-Host"[CPF][SUPPLY][PASS] $indexPath"
}catch{throw}
