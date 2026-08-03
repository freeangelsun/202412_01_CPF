[CmdletBinding()]
param(
  [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
  [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-f]{40}$')][string]$ExpectedHead,
  [string]$ReviewDir='cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4',
  [string]$LogDir='cpf-docs/evidence/runtime/r4-exact-head'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path -LiteralPath $Root).Path
Set-Location -LiteralPath $rootPath
$logRoot=Join-Path $rootPath $LogDir
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss'
$transcript=Join-Path $logRoot "r4_exact_head_$stamp.log"
function Run-Step([string]$Name,[scriptblock]$Action){
  "BEGIN $Name $(Get-Date -Format o)" | Tee-Object -FilePath $transcript -Append
  & $Action 2>&1 | Tee-Object -FilePath $transcript -Append
  if($LASTEXITCODE -ne 0){throw "$Name 실패 exit=$LASTEXITCODE"}
  "PASS $Name $(Get-Date -Format o)" | Tee-Object -FilePath $transcript -Append
}
$actualHead=(& git rev-parse HEAD).Trim()
if($actualHead -ne $ExpectedHead){throw "HEAD 불일치 expected=$ExpectedHead actual=$actualHead"}
$dirty=@(& git status --porcelain)
if($dirty.Count -gt 0){throw "Working Tree가 clean이 아닙니다: $($dirty -join '; ')"}
Run-Step 'GIT_DIFF_CHECK' { git diff --check }
$javaText=(& java -version 2>&1 | Out-String)
if($javaText -notmatch 'version\s+"?(\d+)'){throw 'Java version 확인 실패'}
$javaMajor=[int]$Matches[1]
if($javaMajor -lt 25){throw "Java 25 이상 필요 actual=$javaMajor"}
Run-Step 'EVIDENCE_INTEGRITY' {
  python .\cpf-tools\scripts\verify-cpf-development-evidence-integrity.py --root . --review-dir $ReviewDir --expected-sha $ExpectedHead --expected-requirements 10558 --expected-findings 25
}
Run-Step 'OWNER_BOUNDARIES' { python .\cpf-tools\scripts\verify-cpf-owner-boundaries.py --root . --json-output (Join-Path $logRoot 'owner-boundaries.json') }
Run-Step 'STARTER_CATALOG' { python .\cpf-tools\scripts\verify-cpf-starter-catalog-truth.py --root . --json-output (Join-Path $logRoot 'starter-catalog.json') }
Run-Step 'OPERATOR_TRUST' { python .\cpf-tools\scripts\verify-cpf-operator-trust-boundary.py --root . --json-output (Join-Path $logRoot 'operator-trust.json') }
Run-Step 'TRANSACTION_ID' { python .\cpf-tools\scripts\verify-cpf-transaction-id-standard.py --root . --json-output (Join-Path $logRoot 'transaction-id.json') }
Run-Step 'JAVA25_GRADLE' {
  .\gradlew.bat :cpf-core:test :cpf-admin:test :cpf-biz-admin:test :cpf-batch:contract:test :cpf-batch:execution-runtime:test :cpf-batch:control-server:test :cpf-starters:data:persistence-jdbc:test :cpf-starters:data:persistence-mybatis:test :cpf-starters:integration:http-client:test --no-daemon
}
foreach($vendor in @('postgresql','oracle','mariadb')){
  foreach($mode in @('FreshInstall','Upgrade','RollbackReapply')){
    Run-Step "DB_${vendor}_${mode}" { pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\run-db-vendor-lifecycle.ps1 -Vendor $vendor -Mode $mode -Root . }
  }
}
Run-Step 'ADM_FRONTEND_TEST' { npm --prefix .\cpf-admin\frontend test }
Run-Step 'BZA_FRONTEND_TEST' { npm --prefix .\cpf-biz-admin\frontend test }
Run-Step 'ADM_BROWSER_E2E' { npm --prefix .\cpf-admin\frontend run test:e2e }
Run-Step 'BZA_BROWSER_E2E' { npm --prefix .\cpf-biz-admin\frontend run test:e2e }
Run-Step 'AUDIT_SPRING_MULTI_INSTANCE' { pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\run-adm-audit-multi-instance.ps1 -Root . }
$result=[ordered]@{status='PASS';head=$actualHead;javaMajor=$javaMajor;transcript=$transcript;completedAt=(Get-Date).ToString('o')}
$result | ConvertTo-Json | Tee-Object -FilePath $transcript -Append
