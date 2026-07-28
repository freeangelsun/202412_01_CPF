param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$service = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationService.java'
$controller = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmNotificationController.java'
foreach ($path in @($service,$controller)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "missing: $path" }
}
$serviceText = Get-Content -LiteralPath $service -Raw -Encoding UTF8
foreach ($forbidden in @(
    'ON DUPLICATE KEY','LAST_INSERT_ID','LIMIT ?','CURRENT_TIMESTAMP(3)',
    'com.cpf.core.common.','com.cpf.core.internal.')) {
    if ($serviceText -match [regex]::Escape($forbidden)) {
        throw "Notification service forbidden dependency/SQL: $forbidden"
    }
}
foreach ($required in @(
    'com.cpf.core.api.error.CpfValidationException',
    'com.cpf.core.api.logging.CpfTransactionContext',
    'GeneratedKeyHolder','new String[] {"rule_id"}','new String[] {"delivery_id"}',
    'setMaxRows','DuplicateKeyException')) {
    if ($serviceText -notmatch [regex]::Escape($required)) {
        throw "Portable notification marker missing: $required"
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
    'com.cpf.core.api.execution.CpfOnlineTransaction','adm.operatorId','UNAUTHORIZED','FORBIDDEN')) {
    if ($controllerText -notmatch [regex]::Escape($required)) {
        throw "Notification authentication marker missing: $required"
    }
}
Write-Host '[PASS] Notification authentication, public boundary, and official-DB portable SQL'
