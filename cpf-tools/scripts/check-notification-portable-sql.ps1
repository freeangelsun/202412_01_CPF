param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$service = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationService.java'
$outboxService = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationOutboxService.java'
$controller = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmNotificationController.java'
foreach ($path in @($service,$outboxService,$controller)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "missing: $path" }
}
$serviceText = Get-Content -LiteralPath $service -Raw -Encoding UTF8
$outboxText = Get-Content -LiteralPath $outboxService -Raw -Encoding UTF8
foreach ($source in @(
    [pscustomobject]@{ Name='notification service'; Text=$serviceText },
    [pscustomobject]@{ Name='notification outbox'; Text=$outboxText }
)) {
    foreach ($forbidden in @(
        'ON DUPLICATE KEY','LAST_INSERT_ID','LIMIT ?','CURRENT_TIMESTAMP(3)',
        'DATABASE()','com.cpf.core.common.','com.cpf.core.internal.')) {
        if ($source.Text -match [regex]::Escape($forbidden)) {
            throw "$($source.Name) forbidden dependency/SQL: $forbidden"
        }
    }
}
foreach ($required in @(
    'com.cpf.core.api.error.CpfValidationException',
    'com.cpf.core.api.logging.CpfTransactionContext',
    'GeneratedKeyHolder','new String[] {"rule_id"}',
    'setMaxRows','DuplicateKeyException','redactSensitiveText(rs.getString("delivery_message"))',
    'notificationOutboxService.findStatus(deliveryId)','String.valueOf(before)')) {
    if ($serviceText -notmatch [regex]::Escape($required)) {
        throw "Portable notification service marker missing: $required"
    }
}
foreach ($required in @(
    'GeneratedKeyHolder','new String[] {"delivery_id"}',
    'AdmNotificationVersionConflictException',
    'expectedVersion',
    'AND version = ?',
    'public AdmNotificationDeliveryStatusResponse findStatus(long deliveryId)',
    'setMaxRows')) {
    if ($outboxText -notmatch [regex]::Escape($required)) {
        throw "Portable notification outbox marker missing: $required"
    }
}
foreach ($required in @(
    'recoverExpiredProcessing(owner)',
    "delivery_status = 'UNKNOWN_RESULT'",
    "delivery_status = 'PROCESSING'",
    'LEASE_EXPIRED_UNKNOWN_RESULT',
    'lease_until < ?',
    'cpf_notification_delivery_attempt',
    "attempt_status = 'UNKNOWN_RESULT'",
    "attempt_status = 'PROCESSING'",
    'sanitizeProviderMessage',
    'setMaxRows')) {
    if ($outboxText -notmatch [regex]::Escape($required)) {
        throw "Durable notification outbox recovery marker missing: $required"
    }
}

$recoveryBlock = [regex]::Match(
    $outboxText,
    '(?s)int\s+recoverExpiredProcessing\s*\([^)]*\)\s*\{(?<body>.*?)\n\s*\}\n\s*private\s+List<Candidate>')
if (-not $recoveryBlock.Success) {
    throw 'Durable notification outbox recovery method shape is missing.'
}
if ($recoveryBlock.Groups['body'].Value -match "delivery_status\s*=\s*'RETRY'") {
    throw 'Expired PROCESSING notification must not be automatically retried because provider result is unknown.'
}
$deliveryResponse = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\dto\AdmNotificationDeliveryLogResponse.java'
$attemptResponse = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\dto\AdmNotificationDeliveryAttemptResponse.java'
$notificationPage = Join-Path $Root 'cpf-admin\frontend\src\features\notifications\NotificationsPage.vue'
$controllerTest = Join-Path $Root 'cpf-admin\src\test\java\com\cpf\admin\opr\controller\AdmNotificationControllerAuthenticationTest.java'
$outboxTest = Join-Path $Root 'cpf-admin\src\test\java\com\cpf\admin\opr\service\AdmNotificationOutboxServiceTest.java'
foreach ($path in @($deliveryResponse,$attemptResponse,$notificationPage,$controllerTest,$outboxTest)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "missing: $path" }
}
$deliveryResponseText = Get-Content -LiteralPath $deliveryResponse -Raw -Encoding UTF8
foreach ($required in @(
    'operationId','requestHash','attemptCount','maxAttempts','nextAttemptAt',
    'leaseOwner','leaseUntil','version','lastErrorCode')) {
    if ($deliveryResponseText -notmatch [regex]::Escape($required)) {
        throw "Notification operation visibility marker missing: $required"
    }
}
$attemptResponseText = Get-Content -LiteralPath $attemptResponse -Raw -Encoding UTF8
foreach ($required in @('attemptNo','attemptStatus','providerStatus','providerMessage','leaseVersion')) {
    if ($attemptResponseText -notmatch [regex]::Escape($required)) {
        throw "Notification attempt visibility marker missing: $required"
    }
}
foreach ($migration in @(
    'cpf-tools\db\vendor\mariadb\migration\flyway\V68__notification_delivery_attempt_history.sql',
    'cpf-tools\db\vendor\postgresql\migration\flyway\cpfDB\V68__notification_delivery_attempt_history.sql',
    'cpf-tools\db\vendor\oracle\migration\flyway\cpfDB\V68__notification_delivery_attempt_history.sql')) {
    $migrationPath = Join-Path $Root $migration
    if (-not (Test-Path -LiteralPath $migrationPath -PathType Leaf)) { throw "missing: $migration" }
    $migrationText = Get-Content -LiteralPath $migrationPath -Raw -Encoding UTF8
    foreach ($required in @('cpf_notification_delivery_attempt','attempt_status','lease_version')) {
        if ($migrationText -notmatch [regex]::Escape($required)) { throw "V68 marker missing: $migration :: $required" }
    }
}
$notificationPageText = Get-Content -LiteralPath $notificationPage -Raw -Encoding UTF8
foreach ($required in @(
    'Durable Outbox 발송 이력','retryNotificationDelivery','cancelNotificationDelivery',
    'expectedVersion','operationId','lastErrorCode','Provider Attempt 이력','attemptStatus')) {
    if ($notificationPageText -notmatch [regex]::Escape($required)) {
        throw "Notification operator UI marker missing: $required"
    }
}
if ($notificationPageText -match '<pre\b') {
    throw 'Notification operator UI must not expose raw JSON <pre> output.'
}

$controllerTestText = Get-Content -LiteralPath $controllerTest -Raw -Encoding UTF8
foreach ($required in @('forwardsAuthenticatedOperatorAndExpectedVersionForRetry','versionConflictIsMappedToHttp409','HttpStatus.CONFLICT')) {
    if ($controllerTestText -notmatch [regex]::Escape($required)) {
        throw "Notification controller regression test marker missing: $required"
    }
}
$outboxTestText = Get-Content -LiteralPath $outboxTest -Raw -Encoding UTF8
foreach ($required in @('expiredProcessingLeaseIsQuarantinedAsUnknownResultInsteadOfAutomaticRetry','providerMessagesAreRedactedBeforePersistence','retryAndCancelRequireNonNegativeExpectedVersion')) {
    if ($outboxTestText -notmatch [regex]::Escape($required)) {
        throw "Notification outbox regression test marker missing: $required"
    }
}

$controllerText = Get-Content -LiteralPath $controller -Raw -Encoding UTF8
foreach ($forbidden in @(
    'com.cpf.core.common.','com.cpf.core.internal.',
    '? "ADM" : fallback','defaultValue = "ADM"')) {
    if ($controllerText -match [regex]::Escape($forbidden)) {
        throw "Notification trust boundary regression: $forbidden"
    }
}
foreach ($required in @(
    'com.cpf.core.api.execution.CpfOnlineTransaction','adm.operatorId','UNAUTHORIZED','FORBIDDEN','expectedVersion','/delivery-logs/{deliveryId}/attempts')) {
    if ($controllerText -notmatch [regex]::Escape($required)) {
        throw "Notification authentication marker missing: $required"
    }
}
Write-Host '[PASS] Notification authentication, durable lease recovery, public boundary, and official-DB portable SQL'
