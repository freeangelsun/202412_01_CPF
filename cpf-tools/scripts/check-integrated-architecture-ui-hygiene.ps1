param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path

function Invoke-Required([string]$Name,[string]$Script,[string[]]$Arguments=@()) {
    $path=Join-Path $PSScriptRoot $Script
    if(-not(Test-Path -LiteralPath $path -PathType Leaf)){throw "required QA gate missing: $Script"}
    Write-Host "==> $Name"
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $path -Root $Root @Arguments
    if($LASTEXITCODE -ne 0){throw "$Name failed (exit=$LASTEXITCODE)"}
}
function Require-Text([string]$Relative,[string[]]$Markers) {
    $path=Join-Path $Root $Relative
    if(-not(Test-Path -LiteralPath $path -PathType Leaf)){throw "required file missing: $Relative"}
    $text=Get-Content -LiteralPath $path -Raw -Encoding UTF8
    foreach($marker in $Markers){if($text -notmatch [regex]::Escape($marker)){throw "required marker missing: $Relative :: $marker"}}
}

$ledger=Join-Path $Root 'cpf-tools/verification/20260729_02/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv'
$manifestPath=Join-Path $Root 'cpf-tools/verification/20260729_02/CPF_FINAL_QA_SOURCE_MANIFEST_20260729.json'
foreach($path in @($ledger,$manifestPath)){if(-not(Test-Path -LiteralPath $path -PathType Leaf)){throw "full QA source missing: $path"}}
$manifest=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
if([int]$manifest.baseline_total -ne 2118){throw "baseline QA total drift: $($manifest.baseline_total)"}
if([int]$manifest.merged_total -lt 2118){throw "merged QA ledger lost baseline items: $($manifest.merged_total)"}
$rows=@(Import-Csv -LiteralPath $ledger)
if($rows.Count -ne [int]$manifest.merged_total){throw "merged QA ledger count mismatch: rows=$($rows.Count) manifest=$($manifest.merged_total)"}
$duplicateIds=@($rows | Group-Object id | Where-Object Count -gt 1)
if($duplicateIds.Count -gt 0){throw "duplicate IDs in merged QA ledger: $((@($duplicateIds | Select-Object -First 20 -ExpandProperty Name)) -join ', ')"}
$requirementCount=@($rows | Where-Object kind -eq 'REQUIREMENT').Count
$scenarioCount=@($rows | Where-Object kind -eq 'SCENARIO').Count
if($requirementCount -ne [int]$manifest.merged_requirements){throw "merged requirement count mismatch: rows=$requirementCount manifest=$($manifest.merged_requirements)"}
if($scenarioCount -ne [int]$manifest.merged_scenarios){throw "merged scenario count mismatch: rows=$scenarioCount manifest=$($manifest.merged_scenarios)"}

$baselineLedger=Join-Path $Root 'cpf-tools/verification/20260728_04/CPF_FINAL_QA_MASTER_LEDGER.csv'
if(-not(Test-Path -LiteralPath $baselineLedger -PathType Leaf)){throw "baseline QA ledger missing: $baselineLedger"}
$baselineRows=@(Import-Csv -LiteralPath $baselineLedger)
if($baselineRows.Count -ne [int]$manifest.baseline_total){throw "baseline ledger count mismatch: rows=$($baselineRows.Count) manifest=$($manifest.baseline_total)"}
$mergedIdSet=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach($row in $rows){[void]$mergedIdSet.Add([string]$row.id)}
$missingBaselineIds=@($baselineRows | Where-Object {-not $mergedIdSet.Contains([string]$_.id)} | Select-Object -ExpandProperty id)
if($missingBaselineIds.Count -gt 0){throw "merged QA ledger lost baseline IDs: $((@($missingBaselineIds | Select-Object -First 20)) -join ', ')"}

$allowed=@('완료','부분 구현','미구현','미검증','실패','재확인 필요')
$bad=@($rows | Where-Object { $_.closing_status -notin $allowed })
if($bad.Count -gt 0){throw "unsupported closing status in merged QA ledger: $($bad.Count)"}

Invoke-Required 'Architecture ownership' 'check-architecture-ownership.ps1'
Invoke-Required 'Generated Domain capability inventory' 'export-acc-exs-capability-inventory.ps1'
Invoke-Required 'Frontend feature/route coverage' 'check-frontend-feature-route-coverage.ps1'
Invoke-Required 'Frontend route targets' 'check-frontend-route-targets.ps1'
Invoke-Required 'Modern frontend and external asset policy' 'check-modern-frontend.ps1'
Invoke-Required 'ADM/BZA UX and security' 'check-r11-admin-ux-security.ps1'
Invoke-Required 'Repository hygiene' 'check-repository-hygiene.ps1'
Invoke-Required 'R10 cleanup regression' 'check-r10-cleanup.ps1'
Invoke-Required 'Public API/SPI boundary' 'check-r11-public-boundary.ps1'

# 개발자용 Result 객체를 그대로 출력하는 JSON <pre> 운영 화면은 상용 ADM/BZA 완료로 인정하지 않습니다.
$rawJsonPages = [Collections.Generic.List[string]]::new()
foreach ($frontendRoot in @('cpf-admin/frontend/src','cpf-biz-admin/frontend/src')) {
    $absoluteFrontendRoot = Join-Path $Root $frontendRoot
    if (-not (Test-Path -LiteralPath $absoluteFrontendRoot -PathType Container)) { continue }
    foreach ($page in Get-ChildItem -LiteralPath $absoluteFrontendRoot -Recurse -File -Filter '*.vue') {
        $pageText = Get-Content -LiteralPath $page.FullName -Raw -Encoding UTF8
        if ($pageText -match '(?is)<pre\b[^>]*>.*?(JSON[.]stringify|\{\{\s*[A-Za-z0-9_.]*(Result|result|raw)[A-Za-z0-9_.]*\s*\}\}).*?</pre>') {
            $rawJsonPages.Add($page.FullName.Substring($Root.Length + 1).Replace('\','/')) | Out-Null
        }
    }
}
if ($rawJsonPages.Count -gt 0) {
    throw "Raw JSON <pre> operator pages remain: $($rawJsonPages -join ', ')"
}

$generatorParityScript = Join-Path $Root 'cpf-tools/scripts/check-generator-arbitrary-domain-parity.ps1'
$generatorParityText = Get-Content -LiteralPath $generatorParityScript -Raw -Encoding UTF8
foreach($vendor in @('mariadb','postgresql','oracle')) {
    if($generatorParityText -notmatch [regex]::Escape($vendor)) { throw "official DB vendor missing from generator parity gate: $vendor" }
}
if($generatorParityText -match '(?i)mysql|sqlserver|mssql') {
    throw 'unsupported DB vendor remains in generator parity gate'
}

Require-Text 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java' @(
    'cpf_notification_delivery_attempt','LEASE_EXPIRED_UNKNOWN_RESULT','expectedVersion','AND version = ?',
    "attempt_status = 'UNKNOWN_RESULT'",'sanitizeProviderMessage')
Require-Text 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmNotificationController.java' @(
    '/delivery-logs/{deliveryId}/attempts','admNotificationFindDeliveryAttempts','expectedVersion')
Require-Text 'cpf-admin/frontend/src/features/notifications/NotificationsPage.vue' @(
    'Durable Outbox 발송 이력','Provider Attempt 이력','retryNotificationDelivery','cancelNotificationDelivery')
Require-Text 'cpf-tools/db/canonical/platform-schema.json' @('cpf_notification_delivery_attempt')
foreach($vendorFile in @(
    'cpf-tools/db/vendor/mariadb/migration/flyway/V68__notification_delivery_attempt_history.sql',
    'cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V68__notification_delivery_attempt_history.sql',
    'cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V68__notification_delivery_attempt_history.sql')) {
    Require-Text $vendorFile @('cpf_notification_delivery_attempt','attempt_status','lease_version')
}

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'export-full-qa-closure-matrices.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "full QA matrix export failed: $LASTEXITCODE"}
Write-Host "[PASS] Integrated Architecture/UI/Hygiene QA gate. mergedLedger=$($rows.Count)"
