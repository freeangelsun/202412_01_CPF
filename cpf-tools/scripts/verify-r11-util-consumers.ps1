[CmdletBinding()]
param(
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
)
$ErrorActionPreference='Stop'
$legacyRel='cpf-common\src\main\java\com\cpf\common\utils'
$legacy=Join-Path $Root $legacyRel
$hits=@()
Get-ChildItem $Root -Recurse -File -Filter '*.java' |
  Where-Object { $_.FullName -notlike "$legacy*" } |
  ForEach-Object {
    $m=Select-String -Path $_.FullName -Pattern '^\s*import\s+com\.cpf\.common\.utils\.|com\.cpf\.common\.utils\.'
    if($m){ $hits += $m }
  }
if($hits.Count){
  Write-Host '[FAIL] Legacy cpf-common.utils consumers remain.' -ForegroundColor Red
  $hits | Sort-Object Path,LineNumber -Unique | ForEach-Object {
    Write-Host "  $($_.Path):$($_.LineNumber) $($_.Line.Trim())"
  }
  throw 'R11 utility consumer migration is incomplete.'
}
$required=@(
  'cpf-core\src\main\java\com\cpf\core\api\util\CpfStrings.java',
  'cpf-core\src\main\java\com\cpf\core\api\util\CpfIds.java',
  'cpf-core\src\main\java\com\cpf\core\api\util\CpfTimes.java',
  'cpf-core\src\main\java\com\cpf\core\api\security\CpfMasking.java'
)
foreach($rel in $required){
  if(!(Test-Path (Join-Path $Root $rel))){ throw "Required public API is missing: $rel" }
}
Write-Host '[PASS] Legacy cpf-common.utils consumer count = 0.' -ForegroundColor Green
