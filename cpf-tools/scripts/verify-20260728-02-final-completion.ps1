param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
)
$ErrorActionPreference = "Stop"
$verify = Join-Path $ProjectRoot "cpf-tools\verification\20260728_02\verify-final-overlay.ps1"
& pwsh -ExecutionPolicy Bypass -File $verify -OverlayRoot $ProjectRoot
if ($LASTEXITCODE -ne 0) { throw "CPF 20260728_02 static verification failed (exit=$LASTEXITCODE)" }

$required = @(
    "cpf-admin\frontend\src\features\runtime-control\RuntimeControlPage.vue",
    "cpf-gateway\src\main\java\com\cpf\gateway\transport\CpfGatewayReplayableBody.java",
    "cpf-batch\runtime-common\src\main\java\com\cpf\batch\runtime\BatchRuntimePolicy.java",
    "cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeStateCatalog.java"
)
foreach ($relative in $required) {
    if (-not (Test-Path (Join-Path $ProjectRoot $relative))) { throw "Required file missing: $relative" }
}
Write-Host "CPF 20260728_02 final completion static verification PASS" -ForegroundColor Green
