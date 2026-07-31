param(
  [string]$ProjectRoot='.',
  [string]$ExpectedBaseSha='d31bd127aa12bb9368933216642a5a9d25bd0bfd'
)
$ErrorActionPreference='Stop'; Set-Location $ProjectRoot
$head=(git rev-parse HEAD).Trim(); if($LASTEXITCODE -ne 0){throw 'Git HEAD 확인 실패'}
if($head -ne $ExpectedBaseSha){
  git merge-base --is-ancestor $ExpectedBaseSha $head
  if($LASTEXITCODE -ne 0){throw "Overlay 기준 SHA가 현재 HEAD의 ancestor가 아닙니다. expected=$ExpectedBaseSha head=$head"}
}
$excluded=git diff --name-only -- README.md ':(glob)**/README*' 'cpf-docs/guides/**' 'cpf-docs/assets/readme/**'
if($excluded){throw "README/Guide 제외 범위에 기존 변경이 있습니다. 별도 작업과 충돌을 정리하십시오:`n$excluded"}
$manifest='cpf-docs/work/manifest/CPF_20260731_QA32_DELETE_MANIFEST.txt'
Get-Content $manifest | Where-Object {$_ -and -not $_.StartsWith('#')} | ForEach-Object {
  if(Test-Path $_){Remove-Item -LiteralPath $_ -Force}
}
foreach($frontend in @('cpf-admin/frontend','cpf-biz-admin/frontend')){
  Push-Location $frontend
  npm install --package-lock-only --ignore-scripts
  if($LASTEXITCODE -ne 0){throw "$frontend package-lock 생성 실패"}
  npm ci --ignore-scripts
  if($LASTEXITCODE -ne 0){throw "$frontend npm ci 실패"}
  npm run verify
  if($LASTEXITCODE -ne 0){throw "$frontend verify 실패"}
  Pop-Location
}
python cpf-tools/scripts/verify-cpf-qa32-primary-engines.py --root . --json-report cpf-docs/evidence/current/qa32-static-primary-engines.json
if($LASTEXITCODE -ne 0){throw 'QA32 Primary Engine Gate 실패'}
python cpf-tools/scripts/verify-cpf-qa32-repository-security.py --root . --json-report cpf-docs/evidence/current/qa32-static-security.json
if($LASTEXITCODE -ne 0){throw 'QA32 Repository Security Gate 실패'}
python cpf-tools/scripts/verify-cpf-supply-chain.py --root .
if($LASTEXITCODE -ne 0){throw 'QA32 Supply-chain 정책 Gate 실패'}
python cpf-tools/scripts/verify-cpf-qa32-generator.py --root .
if($LASTEXITCODE -ne 0){throw 'QA32 Generator Gate 실패'}
python cpf-tools/scripts/verify-cpf-qa32-completion.py --root .
if($LASTEXITCODE -ne 0){throw 'QA32 Development Coverage Gate 실패'}
Write-Host "QA32 overlay applied and static gates passed. HEAD=$head"
