[CmdletBinding(SupportsShouldProcess=$true, ConfirmImpact='Medium')]
param([string]$Root=(Get-Location).Path)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
$protectedBuild=(Join-Path $Root 'cpf-tools\build')
$generatedNames=@('.gradle','node_modules','dist','coverage','playwright-report','test-results','__pycache__','.pytest_cache','.mypy_cache','.vite','.turbo')
$removedDirs=0;$removedFiles=0;$removedEmpty=0
$dirs=Get-ChildItem -LiteralPath $Root -Directory -Recurse -Force -ErrorAction SilentlyContinue |
 Where-Object {
   -not $_.FullName.StartsWith((Join-Path $Root '.git')) -and
   (($_.Name -in $generatedNames) -or ($_.Name -eq 'build' -and $_.FullName -ne $protectedBuild))
 } | Sort-Object {$_.FullName.Length} -Descending
foreach($d in $dirs){if($PSCmdlet.ShouldProcess($d.FullName,'Remove generated directory')){Remove-Item -LiteralPath $d.FullName -Recurse -Force;$removedDirs++}}
$files=Get-ChildItem -LiteralPath $Root -File -Recurse -Force -ErrorAction SilentlyContinue |
 Where-Object {
   -not $_.FullName.StartsWith((Join-Path $Root '.git')) -and
   ($_.Name -match '^(npm-debug|yarn-error|hs_err_pid)' -or $_.Extension -in @('.log','.tmp','.bak','.orig','.rej','.pyc','.pyo'))
 }
foreach($f in $files){if($PSCmdlet.ShouldProcess($f.FullName,'Remove generated file')){Remove-Item -LiteralPath $f.FullName -Force;$removedFiles++}}
$empty=Get-ChildItem -LiteralPath $Root -Directory -Recurse -Force -ErrorAction SilentlyContinue |
 Where-Object {-not $_.FullName.StartsWith((Join-Path $Root '.git')) -and $_.FullName -ne $protectedBuild} |
 Sort-Object {$_.FullName.Length} -Descending
foreach($d in $empty){
 if((Test-Path -LiteralPath $d.FullName) -and -not (Get-ChildItem -LiteralPath $d.FullName -Force -ErrorAction SilentlyContinue | Select-Object -First 1)){
   if($PSCmdlet.ShouldProcess($d.FullName,'Remove empty directory')){Remove-Item -LiteralPath $d.FullName -Force;$removedEmpty++}
 }
}
Write-Host "[CPF][CLEANUP][PASS] generatedDirs=$removedDirs generatedFiles=$removedFiles emptyDirs=$removedEmpty protected=$protectedBuild"
