param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,[switch]$RequireRuntimePlan,[string]$RuntimeEvidenceFile)
$ErrorActionPreference='Stop';$errors=[System.Collections.Generic.List[string]]::new()
$manifest=Join-Path $Root 'cpf-tools/db/metadata/database-schema-manifest.json'
if(-not(Test-Path $manifest)){throw "DB schema manifest not found: $manifest"}
$data=Get-Content $manifest -Raw|ConvertFrom-Json
$tables=@($data.tables)
if(-not $tables.Count){$errors.Add('schema manifest has no tables')}
foreach($t in $tables){
  $cols=@($t.columns);$idx=@($t.indexes)
  if(-not $cols.Count){$errors.Add("table has no columns: $($t.name)")}
  # Large/operational tables must never be index-free. PK representation differs by parser, so require any index metadata.
  if($cols.Count -ge 8 -and -not $idx.Count){$errors.Add("wide table has no index metadata: $($t.name)")}
}
if($RequireRuntimePlan){
 if(-not $RuntimeEvidenceFile -or -not(Test-Path $RuntimeEvidenceFile)){$errors.Add('runtime EXPLAIN/slow-query evidence file is required')}
 else {
  $text=Get-Content $RuntimeEvidenceFile -Raw
  foreach($token in @('EXPLAIN','query','duration')){if($text -notmatch $token){$errors.Add("runtime performance evidence missing token: $token")}}
 }
}
if($errors.Count){$errors|ForEach-Object{Write-Error $_};exit 1}
Write-Host "CPF DB performance baseline static gate PASS. tables=$($tables.Count)"
