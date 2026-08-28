$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim());Set-Location $root
$h=Join-Path $root 'cpf-docs\governance\documentation-harness'
& (Join-Path $h 'validators\validate_harness.ps1')
& (Join-Path $h 'validators\validate_quality_fixtures.ps1')
& (Join-Path $h 'validators\validate_readme.ps1') -ReadmePath (Join-Path $root 'README.md')
& (Join-Path $h 'validators\validate_visual_assets.ps1')
& (Join-Path $h 'validators\validate_docx_artifacts.ps1')
$docs=@(Get-ChildItem (Join-Path $root 'cpf-docs\guides') -File | Where-Object Extension -in '.docx','.pdf')+@(Get-ChildItem (Join-Path $root 'cpf-docs\deliverables') -File | Where-Object Extension -in '.docx','.pdf')
$docx=@($docs|Where-Object Extension -eq '.docx');$pdf=@($docs|Where-Object Extension -eq '.pdf')
if($docx.Count-ne11){throw "DOCX COUNT=$($docx.Count)"};if($pdf.Count-ne11){throw "PDF COUNT=$($pdf.Count)"}
$si=Get-Content -LiteralPath (Join-Path $root 'cpf-docs\deliverables\documentation\SOURCE_IDENTITY.json') -Raw -Encoding UTF8|ConvertFrom-Json
if($si.harnessVersion-ne'2.9.0'){throw 'HARNESS VERSION MISMATCH'}
$sums=Join-Path $root 'cpf-docs\deliverables\documentation\SHA256SUMS.txt'
Get-Content -LiteralPath $sums -Encoding UTF8|ForEach-Object{
 if($_.Trim()){$a=$_ -split '  ',2;if($a.Count-ne2){throw "BAD CHECKSUM LINE: $_"};$p=Join-Path $root ($a[1]-replace '/','\\');if(!(Test-Path -LiteralPath $p -PathType Leaf)){throw "CHECKSUM FILE MISSING: $($a[1])"};$actual=(Get-FileHash -Algorithm SHA256 -LiteralPath $p).Hash.ToUpperInvariant();if($actual-ne$a[0].ToUpperInvariant()){throw "CHECKSUM MISMATCH: $($a[1])"}}
}
Write-Host '[CPF][DOC] HARNESS 2.9.0 VERIFY PASS';Write-Host 'DOCX=11 PDF=11';Write-Host "SOURCE_ZIP_SHA256=$($si.sourceZipSha256)";Write-Host "GIT_EXACT_SHA=$($si.gitExactSha)"
