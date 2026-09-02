# CPF generated garbage cleanup.
#
# build/ 는 지우지 않는다. verify-cpf-clean-source-tree.py 는 build/ 를 검사 대상에서 제외하며
# 그 주석이 build/classes/java/main 을 "IDE classpath 계약이 요구하는 정본 출력 위치" 로 명시한다.
# 즉 build/ 삭제는 어떤 게이트도 요구하지 않는 부수피해였고, VS Code(JDT LS)가 build/** 를
# 감시하지 않기 때문에 삭제 즉시 'missing required library' 오류가 남아 IDE 재시작 전까지
# 사라지지 않았다. Gradle 산출물은 up-to-date 검사로 관리되므로 남겨 두는 편이 안전하다.
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
   ($_.Name -in $generatedNames)
 } | Sort-Object {$_.FullName.Length} -Descending
foreach($d in $dirs){if($PSCmdlet.ShouldProcess($d.FullName,'Remove generated directory')){Remove-Item -LiteralPath $d.FullName -Recurse -Force;$removedDirs++}}
$files=Get-ChildItem -LiteralPath $Root -File -Recurse -Force -ErrorAction SilentlyContinue |
 Where-Object {
   -not $_.FullName.StartsWith((Join-Path $Root '.git')) -and
   ($_.Name -match '^(npm-debug|yarn-error|hs_err_pid)' -or $_.Extension -in @('.log','.tmp','.bak','.orig','.rej','.pyc','.pyo'))
 }
foreach($f in $files){if($PSCmdlet.ShouldProcess($f.FullName,'Remove generated file')){Remove-Item -LiteralPath $f.FullName -Force;$removedFiles++}}
$empty=Get-ChildItem -LiteralPath $Root -Directory -Recurse -Force -ErrorAction SilentlyContinue |
 Where-Object {
   # source-empty project 의 canonical compile output(build/classes/java/main)은 의도적으로
   # 비어 있다. 빈 디렉터리라는 이유로 지우면 IDE classpath 가 즉시 깨진다.
   -not $_.FullName.StartsWith((Join-Path $Root '.git')) -and
   $_.FullName -ne $protectedBuild -and
   -not ($_.FullName -like ('*' + [IO.Path]::DirectorySeparatorChar + 'build' + [IO.Path]::DirectorySeparatorChar + '*'))
 } |
 Sort-Object {$_.FullName.Length} -Descending
foreach($d in $empty){
 if((Test-Path -LiteralPath $d.FullName) -and -not (Get-ChildItem -LiteralPath $d.FullName -Force -ErrorAction SilentlyContinue | Select-Object -First 1)){
   if($PSCmdlet.ShouldProcess($d.FullName,'Remove empty directory')){Remove-Item -LiteralPath $d.FullName -Force;$removedEmpty++}
 }
}
Write-Host "[CPF][CLEANUP][PASS] generatedDirs=$removedDirs generatedFiles=$removedFiles emptyDirs=$removedEmpty protected=$protectedBuild"
