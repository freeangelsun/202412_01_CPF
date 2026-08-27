param([Parameter(Mandatory=$true)][string]$ZipPath)
$ErrorActionPreference='Stop'
$zip=[IO.Path]::GetFullPath($ZipPath)
if(!(Test-Path -LiteralPath $zip -PathType Leaf)){throw "SOURCE_ZIP=FAIL missing $zip"}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead($zip)
try{$names=@($z.Entries|Where-Object{-not [string]::IsNullOrEmpty($_.Name)}|ForEach-Object{$_.FullName.Replace('\\','/')})}finally{$z.Dispose()}
$errs=@()
if($names -notcontains 'README.md'){$errs+='README missing'}
foreach($p in @('cpf-docs/guides/','cpf-docs/deliverables/','cpf-docs/assets/','cpf-docs/governance/documentation-harness/','cpf-tools/build/')){if(-not @($names|Where-Object{$_.StartsWith($p)}).Count){$errs+="forced include missing $p"}}
$docx=@($names|Where-Object{$_ -match '^(cpf-docs/guides|cpf-docs/deliverables)/.*\.docx$'}).Count
$pdf=@($names|Where-Object{$_ -match '^(cpf-docs/guides|cpf-docs/deliverables)/.*\.pdf$'}).Count
$visual=@($names|Where-Object{$_.StartsWith('cpf-docs/assets/product-docs/') -and $_.ToLowerInvariant().EndsWith('.png')}).Count
$harness=@($names|Where-Object{$_.StartsWith('cpf-docs/governance/documentation-harness/')}).Count
$tools=@($names|Where-Object{$_.StartsWith('cpf-tools/build/')}).Count
if($docx -lt 11){$errs+="DOCX $docx<11"};if($pdf -lt 11){$errs+="PDF $pdf<11"};if($visual -lt 8){$errs+="VISUAL $visual<8"};if($harness -lt 57){$errs+="HARNESS $harness<57"};if($tools -lt 1){$errs+='CPF_TOOLS_BUILD empty'}
if($errs.Count){Write-Host 'SOURCE_ZIP=FAIL';$errs|%{Write-Host "- $_"};throw 'SOURCE_ZIP_FAIL'}
Write-Host 'SOURCE_ZIP=PASS';Write-Host "FILES=$($names.Count)";Write-Host "CPF_DOCX=$docx";Write-Host "CPF_PDF=$pdf";Write-Host "PRODUCT_VISUAL_PNG=$visual";Write-Host "HARNESS=$harness";Write-Host "CPF_TOOLS_BUILD=$tools"
