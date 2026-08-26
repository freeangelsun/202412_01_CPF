param([string]$Readme = 'README.md')
$ErrorActionPreference='Stop'
function Fail([string]$m){ Write-Host ('README=FAIL '+$m); exit 1 }
if(-not (Test-Path -LiteralPath $Readme -PathType Leaf)){ Fail('missing '+$Readme) }
$text=Get-Content -LiteralPath $Readme -Raw -Encoding UTF8
if([regex]::IsMatch($text,'(?m)^##?\s*목차\s*$')){ Fail('README 목차 금지') }
$nums=@(); foreach($m in [regex]::Matches($text,'(?m)^# (\d+)\. ')){ $nums += [int]$m.Groups[1].Value }
if($nums.Count -eq 0){ Fail('번호형 README H1 없음') }
for($i=0;$i-lt$nums.Count;$i++){ if($nums[$i]-ne($i+1)){ Fail('README H1 번호 불연속') } }
$exact='CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.'
if(([regex]::Matches($text,[regex]::Escape($exact))).Count-ne1){ Fail('License 지정 문장 정확히 1회 필요') }
$imgs=[regex]::Matches($text,'!\[[^\]]*\]\(([^)]+)\)')
if($imgs.Count-lt5-or$imgs.Count-gt8){ Fail("Visual count $($imgs.Count) not 5..8") }
$arch=$false; foreach($m in $imgs){ if($m.Groups[1].Value -match 'architecture'){ $arch=$true } }; if(-not$arch){ Fail('Architecture visual reference missing') }
# Format link integrity: explicit PDF/DOCX labels must target matching extension.
foreach($m in [regex]::Matches($text,'\[([^\]]+)\]\(([^)]+)\)')){
  $label=$m.Groups[1].Value; $target=($m.Groups[2].Value -split '#')[0] -replace '%20',' '
  if($label -match '(?i)PDF' -and $target -notmatch '(?i)\.pdf$'){ Fail("PDF label target mismatch: $label -> $target") }
  if($label -match '(?i)DOCX' -and $target -notmatch '(?i)\.docx$'){ Fail("DOCX label target mismatch: $label -> $target") }
  if($target -match '^(https?:|mailto:|#)'){ continue }
  $base=Split-Path -Parent ([IO.Path]::GetFullPath($Readme)); $local=Join-Path $base ($target -replace '/','\')
  if(($label -match '(?i)(PDF|DOCX)') -and -not(Test-Path -LiteralPath $local -PathType Leaf)){ Fail("document link target missing: $target") }
}
Write-Host 'README=PASS'; exit 0
