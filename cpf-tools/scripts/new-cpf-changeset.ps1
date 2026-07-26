[CmdletBinding()] param([Parameter(Mandatory)][string]$ChangeSetId,[Parameter(Mandatory)][string]$SourceEnvironment,[Parameter(Mandatory)][string]$TargetEnvironment,[Parameter(Mandatory)][string]$Reason,[Parameter(Mandatory)][string[]]$Files,[string]$Root='.',[string]$OutputDirectory='cpf-docs/work/evidence/promotion')
$ErrorActionPreference='Stop'; $root=(Resolve-Path $Root).Path; $items=@()
foreach($f in $Files){$p=Resolve-Path (Join-Path $root $f); $items += [ordered]@{path=$f.Replace('\\','/');sha256=(Get-FileHash $p -Algorithm SHA256).Hash.ToLowerInvariant()}}
$sha=(git -C $root rev-parse HEAD 2>$null); if(-not $sha){$sha='UNKNOWN'}
$m=[ordered]@{schemaVersion=1;changeSetId=$ChangeSetId;sourceEnvironment=$SourceEnvironment;targetEnvironment=$TargetEnvironment;baseCommit=$sha;reason=$Reason;createdAt=(Get-Date).ToUniversalTime().ToString('o');files=$items}
$out=Join-Path $root $OutputDirectory;New-Item -ItemType Directory -Force -Path $out|Out-Null;$path=Join-Path $out "$ChangeSetId.json";$m|ConvertTo-Json -Depth 8|Set-Content -Encoding UTF8 $path;Write-Host $path
