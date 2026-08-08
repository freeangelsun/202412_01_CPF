param()
$ErrorActionPreference='Stop'
$root=(Get-Location).Path
if(-not (Test-Path -LiteralPath (Join-Path $root '.git'))){throw 'Repository root에서 실행하세요.'}
$f=Join-Path $root 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'
$c=Get-Content -LiteralPath $f -Raw
if($c -notmatch '현재 Canonical Requirement Count는 \*\*169개\*\*'){throw 'Canonical 169 marker missing'}
if($c -match '현재 Canonical Requirement Count는 \*\*162개\*\*'){throw 'Stale canonical 162 marker remains'}
if($c -notmatch '정식 거래 기동 Channel'){throw 'TransactionId channel-origin contract missing'}
if($c -notmatch '동일 transactionId를 End-to-End로 승계·보존'){throw 'TransactionId E2E propagation contract missing'}
$forbidden=@(
 'cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md',
 'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md',
 'cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9',
 'cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1',
 'cpf-docs/work/codex/qa38',
 'cpf-docs/work/r6i-dev'
)
foreach($rel in $forbidden){
 $p=Join-Path $root $rel
 if(Test-Path -LiteralPath $p){throw "Stale project-control path remains: $rel"}
}
$changed=@(git status --short)
$protected=$changed | Where-Object {
 $_ -match 'README\.md$' -or
 $_ -match 'cpf-docs[\\/]guides[\\/]' -or
 $_ -match 'cpf-docs[\\/]deliverables[\\/]' -or
 $_ -match 'cpf-docs[\\/]assets[\\/]manuals[\\/]' -or
 $_ -match 'cpf-docs[\\/]assets[\\/]readme[\\/]' -or
 $_ -match 'cpf-docs[\\/]specification[\\/]CPF_DOCUMENTATION_STANDARD\.md' -or
 $_ -match 'cpf-docs[\\/]environment[\\/]docker[\\/]' -or
 $_ -match 'cpf-tools[\\/]environment[\\/]docker-development-test[\\/]'
}
if($protected){$protected; throw 'Protected/customer-document path changed'}
git diff --check
if($LASTEXITCODE -ne 0){throw 'git diff --check failed'}
Write-Host 'CENTRAL CURRENTIZATION LOW-COST VALIDATION PASS'
git status --short
