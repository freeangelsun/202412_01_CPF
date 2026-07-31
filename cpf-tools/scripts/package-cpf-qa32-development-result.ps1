param([string]$Root='.',[string]$BaseSha='d31bd127aa12bb9368933216642a5a9d25bd0bfd',[string]$OutputZip='CPF_QA32_DEVELOPMENT_RESULT_ROOT_OVERLAY_20260731.zip')
$ErrorActionPreference='Stop';Set-Location $Root
$files=@(git diff --name-only $BaseSha --; git ls-files --others --exclude-standard) | Sort-Object -Unique
$files=$files | Where-Object {$_ -and -not($_ -match '(^|/)(\.git|node_modules|build|dist|coverage|playwright-report|test-results|\.gradle)(/|$)') -and -not($_ -match '(^|/)(README[^/]*|cpf-docs/guides/|cpf-docs/assets/readme/)')}
if(-not $files){throw '패키징할 변경 파일이 없습니다.'}
$tmp=Join-Path $env:TEMP ('cpf-qa32-package-'+[guid]::NewGuid());New-Item -ItemType Directory -Force $tmp|Out-Null
foreach($f in $files){if(Test-Path $f){$dest=Join-Path $tmp $f;New-Item -ItemType Directory -Force (Split-Path $dest)|Out-Null;Copy-Item $f $dest -Force}}
if(Test-Path $OutputZip){Remove-Item $OutputZip -Force};Compress-Archive -Path (Join-Path $tmp '*') -DestinationPath $OutputZip -CompressionLevel Optimal
$hash=(Get-FileHash $OutputZip -Algorithm SHA256).Hash.ToLowerInvariant();$count=(Get-ChildItem $tmp -Recurse -File).Count
Remove-Item $tmp -Recurse -Force;Write-Host "ZIP=$OutputZip FILES=$count SHA256=$hash BASE=$BaseSha"
