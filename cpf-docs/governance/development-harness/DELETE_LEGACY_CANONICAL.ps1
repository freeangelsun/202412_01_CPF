param([switch]$ApplyApprovedManifest)
$ErrorActionPreference='Stop'
$PSNativeCommandUseErrorActionPreference=$true
if(!$ApplyApprovedManifest){throw 'DELETE NOT APPROVED: rerun with -ApplyApprovedManifest'}
$root=(git rev-parse --show-toplevel).Trim()
Set-Location $root
$h=Join-Path $root 'cpf-docs\governance\development-harness'
$mf=Join-Path $h 'DELETE_MANIFEST.csv'
if(!(Test-Path -LiteralPath $mf)){throw "DELETE MANIFEST NOT FOUND: $mf"}
python (Join-Path $h 'validators\run_all_gates.py')
if($LASTEXITCODE-ne0){throw 'HARNESS FINAL GATE FAIL - DELETE BLOCKED'}
python (Join-Path $h 'validators\validate_migration_semantic_closure.py')
if($LASTEXITCODE-ne0){throw 'MIGRATION SEMANTIC GATE FAIL - DELETE BLOCKED'}
$authority=Get-Content (Join-Path $h 'contracts\current-authority-registry.json') -Raw -Encoding UTF8|ConvertFrom-Json
$forbidden=@($authority.authoritative)+@(
  'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
  'cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md'
)
$alwaysProtected=@(
  'cpf-docs/deliverables/',
  'cpf-docs/guides/',
  'cpf-docs/environment/docker/',
  'cpf-tools/environment/docker-development-test/',
  'cpf-docs/governance/documentation-harness/'
)
$deleted=0;$missing=0;$selected=0
$parents=[Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
Import-Csv -LiteralPath $mf -Encoding UTF8|Where-Object{
  $_.approved-eq'true' -and $_.user_approved-eq'true' -and $_.delete_eligible-eq'true' -and
  $_.semantic_status-eq'PASS' -and $_.precondition-eq'HARNESS_AUTHORITY_AND_MIGRATION_SEMANTIC_GATE_PASS'
}|ForEach-Object{
  $selected++
  $rel=([string]$_.path).Replace('\','/').TrimStart('/')
  if([string]::IsNullOrWhiteSpace($rel)-or[IO.Path]::IsPathRooted([string]$_.path)-or$rel.Contains('../')-or($forbidden -contains $rel)){throw "UNSAFE DELETE: $rel"}
  if($alwaysProtected|Where-Object{$rel.StartsWith($_,[StringComparison]::OrdinalIgnoreCase)}){throw "PROTECTED PATH DELETE FORBIDDEN: $rel"}
  $p=[IO.Path]::GetFullPath((Join-Path $root ($rel-replace'/','\')))
  if(!(Test-Path -LiteralPath $p)){$missing++;return}
  if(Test-Path -LiteralPath $p -PathType Container){throw "DIRECTORY DELETE REJECTED: $rel"}
  $replacement=Join-Path $root (([string]$_.replacement_path)-replace'/','\')
  if(!(Test-Path -LiteralPath $replacement -PathType Leaf)){throw "REPLACEMENT MISSING: $($_.replacement_path)"}
  $expected=([string]$_.expected_sha256).ToUpperInvariant()
  $actual=(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToUpperInvariant()
  if($expected-ne$actual){throw "DELETE SHA256 DRIFT: $rel expected=$expected actual=$actual"}
  [void]$parents.Add((Split-Path -Parent $p))
  Remove-Item -LiteralPath $p -Force
  $deleted++
}
$empty=0
foreach($start in @($parents)|Sort-Object Length -Descending){
  $d=$start
  while($d-and$d-ne$root){
    $rrel=$d.Substring($root.Length).TrimStart('\','/').Replace('\','/')+'/'
    if($alwaysProtected|Where-Object{$rrel.StartsWith($_,[StringComparison]::OrdinalIgnoreCase)}){break}
    if(!(Test-Path -LiteralPath $d -PathType Container)){break}
    if(Get-ChildItem -LiteralPath $d -Force|Select-Object -First 1){break}
    Remove-Item -LiteralPath $d -Force
    $empty++
    $d=Split-Path -Parent $d
  }
}
python (Join-Path $h 'validators\run_all_gates.py')
if($LASTEXITCODE-ne0){throw 'POST-DELETE HARNESS FINAL GATE FAIL'}
Write-Host "CPF_DEV_HARNESS_LEGACY_DELETE=PASS SELECTED=$selected DELETED=$deleted ALREADY_MISSING=$missing EMPTY_DIRS_DELETED=$empty"
git status --short
