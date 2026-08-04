[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-f]{40}$')][string]$ExpectedHead,
  [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
  [string]$LogDir='cpf-docs/evidence/qa/full-campaign-exact-head'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path -LiteralPath $Root).Path
Set-Location -LiteralPath $rootPath
$logRoot=Join-Path $rootPath $LogDir
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss'
$transcript=Join-Path $logRoot "cpf_full_qa_exact_head_$stamp.log"

function Run-Step([string]$Name,[scriptblock]$Action){
  "BEGIN $Name $(Get-Date -Format o)" | Tee-Object -FilePath $transcript -Append
  $global:LASTEXITCODE=0
  & $Action 2>&1 | Tee-Object -FilePath $transcript -Append
  if($LASTEXITCODE -ne 0){throw "$Name 실패 exit=$LASTEXITCODE"}
  "PASS $Name $(Get-Date -Format o)" | Tee-Object -FilePath $transcript -Append
}

# Fresh-clone/exact-HEAD preconditions. No Reset/Restore/Stash/Clean is performed.
Run-Step 'GIT_FETCH' { git fetch origin }
$branch=(& git branch --show-current).Trim()
$localHead=(& git rev-parse HEAD).Trim()
$originHead=(& git rev-parse origin/master).Trim()
if($branch -ne 'master'){throw "branch 불일치 expected=master actual=$branch"}
if($localHead -ne $ExpectedHead){throw "Local HEAD 불일치 expected=$ExpectedHead actual=$localHead"}
if($originHead -ne $ExpectedHead){throw "origin/master 불일치 expected=$ExpectedHead actual=$originHead"}
$dirty=@(& git status --porcelain)
if($dirty.Count -gt 0){throw "Working Tree가 clean이 아닙니다: $($dirty -join '; ')"}
Run-Step 'GIT_DIFF_CHECK' { git diff --check }

$javaText=(& java -version 2>&1 | Out-String)
if($javaText -notmatch 'version\s+"?(\d+)'){throw 'Java version 확인 실패'}
$javaMajor=[int]$Matches[1]
if($javaMajor -lt 25){throw "Java 25 이상 필요 actual=$javaMajor"}

# Gate implementation and canonical split datasets must be independently valid first.
Run-Step 'PYTHON_GATE_TESTS' { python -m pytest -q .\cpf-tools\scripts\tests }
Run-Step 'PYTHON_COMPILEALL' { python -m compileall -q .\cpf-tools\scripts .\cpf-tools\verification }
Run-Step 'SPLIT_MASTER_DATASET' {
  python .\cpf-tools\scripts\verify-cpf-split-master-dataset.py --root . --expected-sha $ExpectedHead --require-clean --json-output (Join-Path $logRoot 'split-master.json')
}
Run-Step 'OWNER_BOUNDARIES' { python .\cpf-tools\scripts\verify-cpf-owner-boundaries.py --root . --json-output (Join-Path $logRoot 'owner-boundaries.json') }
Run-Step 'SPRING_REQUEST_MAPPING_UNIQUENESS' { python .\cpf-tools\scripts\verify-cpf-spring-request-mapping-uniqueness.py --root . --json-output (Join-Path $logRoot 'spring-request-mappings.json') }
Run-Step 'APPROVAL_STATE_MACHINE' { python .\cpf-tools\scripts\verify-cpf-approval-state-machine.py --root . --json-output (Join-Path $logRoot 'approval-state-machine.json') }
Run-Step 'STARTER_CATALOG' { python .\cpf-tools\scripts\verify-cpf-starter-catalog-truth.py --root . --json-output (Join-Path $logRoot 'starter-catalog.json') }
Run-Step 'OPERATOR_TRUST' { python .\cpf-tools\scripts\verify-cpf-operator-trust-boundary.py --root . --json-output (Join-Path $logRoot 'operator-trust.json') }
Run-Step 'INTERNAL_SERVICE_IDENTITY_BINDING' { python .\cpf-tools\scripts\verify-cpf-internal-service-identity-binding.py --root . --json-output (Join-Path $logRoot 'internal-service-identity.json') }
Run-Step 'BATCH_APPROVAL_TRUST_BOUNDARY' { python .\cpf-tools\scripts\verify-cpf-batch-approval-trust-boundary.py --root . --json-output (Join-Path $logRoot 'batch-approval-trust.json') }
Run-Step 'BATCH_RUNTIME_COMMAND_VERSIONING' { python .\cpf-tools\scripts\verify-cpf-batch-runtime-command-versioning.py --root . --json-output (Join-Path $logRoot 'batch-runtime-versioning.json') }
Run-Step 'TRANSACTION_ID' { python .\cpf-tools\scripts\verify-cpf-transaction-id-standard.py --root . --json-output (Join-Path $logRoot 'transaction-id.json') }
Run-Step 'FRONTEND_CONSUMER_CLOSURE' { python .\cpf-tools\scripts\verify-cpf-frontend-consumer-closure.py --root . --json-output (Join-Path $logRoot 'frontend-consumer.json') }
Run-Step 'NETWORK_POLICY_CONSUMERS' { python .\cpf-tools\scripts\verify-cpf-network-policy-consumers.py --root . --json-output (Join-Path $logRoot 'network-consumers.json') }
Run-Step 'RUNTIME_SNAPSHOT_VERSIONING' { python .\cpf-tools\scripts\verify-cpf-runtime-snapshot-versioning.py --root . --json-output (Join-Path $logRoot 'runtime-snapshot-versioning.json') }
Run-Step 'DB_LESS_FAIL_CLOSED' { python .\cpf-tools\scripts\verify-cpf-db-less-fail-closed.py --root . --json-output (Join-Path $logRoot 'db-less.json') }
Run-Step 'DB_VENDOR_SEMANTIC' { python .\cpf-tools\scripts\verify-cpf-db-vendor-semantic-parity.py --root . --json-output (Join-Path $logRoot 'db-vendor-semantic.json') }
Run-Step 'BAT_OPERATION_LEDGER_LIFECYCLE' { python .\cpf-tools\scripts\verify-cpf-bat-operation-ledger-lifecycle.py --root . --json-output (Join-Path $logRoot 'bat-operation-ledger.json') }
Run-Step 'GENERATOR_CANONICAL_CLOSURE' { python .\cpf-tools\verification\qa39\verify-qa39-canonical-starter-closure.py }
Run-Step 'CUSTOMER_PROVIDER_CONFORMANCE' { python .\cpf-tools\verification\qa39\verify-cpf-provider-conformance.py }

# Root graph execution on Java 25 covers all included modules, generated/reference domains and publications assembled by Gradle.
$gradle=if($IsWindows){'.\gradlew.bat'}else{'./gradlew'}
Run-Step 'JAVA25_ROOT_CLEAN_TEST_ASSEMBLE' { & $gradle clean test assemble --no-daemon }

foreach($surface in @('cpf-admin/frontend','cpf-biz-admin/frontend')){
  Run-Step "${surface}_NPM_CI" { npm --prefix $surface ci }
  Run-Step "${surface}_UNIT" { npm --prefix $surface test }
  Run-Step "${surface}_BUILD" { npm --prefix $surface run build }
  Run-Step "${surface}_BROWSER_E2E" { npm --prefix $surface run test:e2e }
}

foreach($vendor in @('postgresql','oracle','mariadb')){
  foreach($mode in @('FreshInstall','Upgrade','RollbackReapply')){
    Run-Step "DB_${vendor}_${mode}" { pwsh -NoProfile -File .\cpf-tools\scripts\run-db-vendor-lifecycle.ps1 -Vendor $vendor -Mode $mode -Root . }
  }
}
Run-Step 'AUDIT_SPRING_MULTI_INSTANCE_KILL_RESTART' { pwsh -NoProfile -File .\cpf-tools\scripts\run-adm-audit-multi-instance.ps1 -Root . }

# This is deliberately last: no partial/common Work Package evidence can bypass per-row completion.
Run-Step 'FULL_QA_PRODUCT_PASS_71321' {
  python .\cpf-tools\scripts\verify-cpf-full-qa-completion.py --root . --expected-sha $ExpectedHead --mode product-pass --json-output (Join-Path $logRoot 'full-qa-product-pass.json')
}

$result=[ordered]@{
  status='PASS'; branch=$branch; head=$localHead; originMaster=$originHead; javaMajor=$javaMajor
  requirementCount=30558; scenarioCount=40763; logicalItemCount=71321
  transcript=$transcript; completedAt=(Get-Date).ToString('o')
}
$result|ConvertTo-Json|Tee-Object -FilePath $transcript -Append
