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
foreach($bad in @('그림 해석','그림 설명')){ if($text.Contains($bad)){Fail('generic figure label '+$bad)} }
$imgTargets=New-Object System.Collections.Generic.List[string]
foreach($m in [regex]::Matches($text,'!\[[^\]]*\]\(([^)]+)\)')){ if(-not $imgTargets.Contains($m.Groups[1].Value)){[void]$imgTargets.Add($m.Groups[1].Value)} }
foreach($m in [regex]::Matches($text,'<img\b[^>]*\bsrc=["'']([^"'']+)["''][^>]*>','IgnoreCase')){ if(-not $imgTargets.Contains($m.Groups[1].Value)){[void]$imgTargets.Add($m.Groups[1].Value)} }
if($imgTargets.Count-lt5-or$imgTargets.Count-gt8){ Fail("Visual count $($imgTargets.Count) not 5..8") }
$arch=$false; foreach($target in $imgTargets){ if($target -match 'architecture'){ $arch=$true }; if($target -notmatch '^(?i:https?:|data:)'){ $base=Split-Path -Parent ([IO.Path]::GetFullPath($Readme)); $local=Join-Path $base ($target -replace '/','\'); if(-not(Test-Path -LiteralPath $local -PathType Leaf)){Fail("visual target missing: $target")} } }; if(-not$arch){ Fail('Architecture visual reference missing') }
if($text -notmatch 'CPF-DARK-CONTENT-SURFACE'){Fail('CPF owned dark content surface marker missing')}
foreach($m in [regex]::Matches($text,'\[([^\]]+)\]\(([^)]+)\)')){
  $label=$m.Groups[1].Value; $target=($m.Groups[2].Value -split '#')[0] -replace '%20',' '
  if($label -match '(?i)DOCX' -or $target -match '(?i)\.docx$'){Fail("DOCX user link forbidden: $label -> $target")}
  if($label -match '(?i)PDF' -and $target -notmatch '(?i)\.pdf$'){ Fail("PDF label target mismatch: $label -> $target") }
  if($target -match '^(https?:|mailto:|#)'){ continue }
  $base=Split-Path -Parent ([IO.Path]::GetFullPath($Readme)); $local=Join-Path $base ($target -replace '/','\')
  if(($label -match '(?i)PDF') -and -not(Test-Path -LiteralPath $local -PathType Leaf)){ Fail("document link target missing: $target") }
}
Write-Host 'README=PASS'; exit 0

if($text -match 'Harness\s+v?\d+(?:\.\d+)+' -or $text -match 'Source(?: snapshot)?\s*[:·]?\s*(?:ZIP_SHA256:)?[0-9A-Fa-f]{16,}' -or $text -match 'Documentation baseline'){throw 'README=FAIL user-facing provenance forbidden'}
