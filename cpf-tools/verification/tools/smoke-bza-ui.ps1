param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = "",
    [string] $FrontendRoot = "",
    [string] $BzaBaseUrl = "http://127.0.0.1:8080",
    [string] $BzaUsername = $(if ([string]::IsNullOrWhiteSpace($env:CPF_BZA_SMOKE_USERNAME)) { "bza-admin" } else { $env:CPF_BZA_SMOKE_USERNAME }),
    [string] $BzaPassword = $env:CPF_BZA_SMOKE_PASSWORD,
    [switch] $BrowserClick,
    [switch] $RequireBrowserClick
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot "..\..\runtime\tools\runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

if ([string]::IsNullOrWhiteSpace($FrontendRoot)) {
    $FrontendRoot = Join-Path $Root "cpf-biz-admin/frontend"
} elseif (-not [IO.Path]::IsPathRooted($FrontendRoot)) {
    $FrontendRoot = Join-Path $Root $FrontendRoot
}
$FrontendRoot = [IO.Path]::GetFullPath($FrontendRoot)

$appPath = Join-Path $Root "cpf-biz-admin/frontend/src/App.vue"
$sessionPath = Join-Path $Root "cpf-biz-admin/frontend/src/features/auth/session.ts"
$routePath = Join-Path $Root "cpf-biz-admin/frontend/src/app/routes.ts"
$apiPath = Join-Path $Root "cpf-biz-admin/frontend/src/generated/orval/cpf-api.ts"
$mutatorPath = Join-Path $Root "cpf-biz-admin/frontend/src/shared/orval-mutator.ts"
$openApiPath = Join-Path $Root "cpf-biz-admin/frontend/openapi/cpf-openapi.json"
$packageLockPath = Join-Path $Root "cpf-biz-admin/frontend/package-lock.json"
$resultPath = Join-Path $ResultDir "bza-ui-runtime-result.sanitized.json"

$requiredFiles = @($appPath,$sessionPath,$routePath,$apiPath,$mutatorPath,$openApiPath,$packageLockPath)
$missingFiles = @($requiredFiles | Where-Object { -not (Test-Path -LiteralPath $_ -PathType Leaf) })
if ($missingFiles.Count -gt 0) {
    throw "BZA UI canonical source is incomplete: $($missingFiles -join ', ')"
}

$app = [IO.File]::ReadAllText($appPath,[Text.Encoding]::UTF8)
$session = [IO.File]::ReadAllText($sessionPath,[Text.Encoding]::UTF8)
$routes = [IO.File]::ReadAllText($routePath,[Text.Encoding]::UTF8)
$api = [IO.File]::ReadAllText($apiPath,[Text.Encoding]::UTF8)
$mutator = [IO.File]::ReadAllText($mutatorPath,[Text.Encoding]::UTF8)
$openApi = Get-Content -LiteralPath $openApiPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100

$markers = [ordered]@{
    vuePrimarySurface = $app.Contains('<script setup lang="ts">') -and $app.Contains('BZA Backoffice')
    loginAccessibility = $app.Contains('aria-labelledby="loginTitle"') -and $app.Contains('aria-live="polite"')
    passwordAutocomplete = $app.Contains('autocomplete="current-password"')
    secureBffSession = $session.Contains('bzaAuthLogin') -and $session.Contains('bzaAuthRefresh') -and $session.Contains('bzaAuthLogout') -and $session.Contains('bzaAuthMe')
    serverSessionRecovery = $session.Contains('configureCpfBffSessionRecovery') -and $session.Contains('sessionEpoch')
    noBrowserTokenPersistence = -not (($session + "`n" + $mutator) -match '(?i)(localStorage|sessionStorage).*(access.?token|refresh.?token|authorization|bearer)')
    permissionAwareNavigation = $app.Contains('hasBzaMenu') -and $routes.Contains('menuCode:')
    routeOperationContract = $routes.Contains('expectedOperationIds') -and $routes.Contains('bzaSupportDashboard')
    generatedLoginClient = $api.Contains('getBzaAuthLoginUrl') -and $api.Contains('/api/bza/auth/login')
    generatedLogoutClient = $api.Contains('/api/bza/auth/logout')
    generatedRefreshClient = $api.Contains('/api/bza/auth/refresh')
    generatedMeClient = $api.Contains('/api/bza/auth/me')
    openApiOperationCount = ([int]$openApi.'x-cpf-openapi-operation-count' -eq 96)
    packageLock = Test-Path -LiteralPath $packageLockPath -PathType Leaf
    legacyConsoleRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/frontend/src/features/console.ts') -PathType Leaf)
    legacyGlobalVueRemoved = -not (($app + "`n" + $session) -match '(?i)(window\.Vue|vue\.global)')
}

$result = [ordered]@{
    schemaVersion = 2
    checkedAt = [DateTimeOffset]::Now.ToString('o')
    status = Get-CpfRuntimeStatusText 'Partial'
    frontendRoot = $FrontendRoot
    markers = $markers
    nodeTypecheck = [ordered]@{ available = $false; executed = $false; passed = $false; reason = '' }
    browserClick = [ordered]@{ requested = [bool]$BrowserClick; required = [bool]$RequireBrowserClick; status = 'NOT_REQUESTED' }
    sanitized = $true
}

$missingMarkers = @($markers.GetEnumerator() | Where-Object { -not [bool]$_.Value } | ForEach-Object Key)
if ($missingMarkers.Count -gt 0) {
    $result.status = Get-CpfRuntimeStatusText 'Failed'
    $result.missingMarkers = $missingMarkers
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    throw "BZA UI static contract failed: $($missingMarkers -join ', ')"
}

$npmName = if ($env:OS -eq 'Windows_NT') { 'npm.cmd' } else { 'npm' }
$npm = Get-Command $npmName -ErrorAction SilentlyContinue
$installedMarker = Join-Path $FrontendRoot 'node_modules/typescript/package.json'
if ($npm -and (Test-Path -LiteralPath $installedMarker -PathType Leaf)) {
    Push-Location $FrontendRoot
    try {
        $typecheckOutput = @(& $npm.Source run typecheck 2>&1 | ForEach-Object { $_.ToString() })
        $typecheckExit = $LASTEXITCODE
    } finally { Pop-Location }
    $result.nodeTypecheck.available = $true
    $result.nodeTypecheck.executed = $true
    $result.nodeTypecheck.passed = ($typecheckExit -eq 0)
    $result.nodeTypecheck.output = ($typecheckOutput -join "`n")
    if ($typecheckExit -ne 0) {
        $result.status = Get-CpfRuntimeStatusText 'Failed'
        Write-CpfRuntimeJson -Path $resultPath -Value $result
        throw 'BZA UI typecheck failed in prepared frontend sandbox.'
    }
} else {
    $result.nodeTypecheck.reason = 'Prepared frontend sandbox/node_modules unavailable; FullLocal frontend verify owns npm ci/typecheck.'
}

if (-not $BrowserClick) {
    $result.status = Get-CpfRuntimeStatusText 'Done'
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    Write-Host "BZA UI canonical source smoke passed: $resultPath"
    return
}

if ([string]::IsNullOrWhiteSpace($BzaPassword)) {
    $result.browserClick.status = 'FAILED'
    $result.browserClick.reason = 'CPF_BZA_SMOKE_PASSWORD / -BzaPassword is required for actual BZA login.'
    $result.status = Get-CpfRuntimeStatusText 'Failed'
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    if ($RequireBrowserClick) { exit 1 }
    return
}
if (-not $npm) {
    $result.browserClick.status = 'SKIPPED'
    $result.browserClick.reason = 'npm is unavailable.'
    $result.status = if ($RequireBrowserClick) { Get-CpfRuntimeStatusText 'Failed' } else { Get-CpfRuntimeStatusText 'Done' }
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    if ($RequireBrowserClick) { exit 1 }
    return
}

$runnerDir = Join-Path $Root 'build/runtime-smoke/playwright-runner'
New-Item -ItemType Directory -Force -Path $runnerDir | Out-Null
$specPath = Join-Path $runnerDir 'bza-ui-click-smoke.spec.js'
$screenshotPath = Join-Path $ResultDir 'bza-ui-click-smoke.png'
$playwrightLogPath = Join-Path $ResultDir 'bza-ui-click-smoke.log'
$spec = @'
const { test, expect } = require("@playwright/test");
test.use({ channel: "msedge" });
test("BZA authenticated basic click flow", async ({ page }) => {
  const baseUrl = process.env.BZA_BASE_URL || "http://127.0.0.1:8080";
  const username = process.env.BZA_UI_SMOKE_USERNAME || "bza-admin";
  const password = process.env.BZA_UI_SMOKE_PASSWORD;
  const screenshot = process.env.BZA_UI_SMOKE_SCREENSHOT || "bza-ui-click-smoke.png";
  if (!password) throw new Error("BZA_UI_SMOKE_PASSWORD is required");
  await page.goto(`${baseUrl}/bza`, { waitUntil: "domcontentloaded" });
  await expect(page.getByRole("heading", { name: "BZA Backoffice" })).toBeVisible({ timeout: 15000 });
  await page.getByLabel("로그인 ID").fill(username);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "로그인" }).click();
  await expect(page.getByRole("navigation", { name: "업무 백오피스 메뉴" })).toBeVisible({ timeout: 15000 });
  const passwordDialog = page.getByRole("dialog");
  if (await passwordDialog.isVisible().catch(() => false)) {
    const cancel = passwordDialog.getByRole("button", { name: "취소" });
    if (await cancel.isVisible().catch(() => false)) await cancel.click();
  }
  for (const label of ["조직", "역할", "결재 상신", "업무 감사"]) {
    const menu = page.getByRole("button", { name: label });
    if (await menu.isVisible().catch(() => false)) {
      await menu.click();
      await expect(page.getByRole("heading", { name: label })).toBeVisible({ timeout: 15000 });
    }
  }
  await page.screenshot({ path: screenshot, fullPage: true });
});
'@
[IO.File]::WriteAllText($specPath,$spec,[Text.UTF8Encoding]::new($false))

$playwrightPath = Join-Path $runnerDir 'node_modules/.bin/playwright.cmd'
if (-not (Test-Path -LiteralPath $playwrightPath -PathType Leaf)) {
    $installOutput = @(& $npm.Source install --prefix $runnerDir --no-save --package-lock=false --no-audit --no-fund '@playwright/test@1.61.1' 2>&1 | ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $playwrightPath -PathType Leaf)) {
        $result.browserClick.status = 'FAILED'
        $result.browserClick.reason = 'Playwright runner installation failed.'
        $result.browserClick.installOutput = ($installOutput -join "`n")
        $result.status = Get-CpfRuntimeStatusText 'Failed'
        $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
        Write-CpfRuntimeJson -Path $resultPath -Value $result
        if ($RequireBrowserClick) { exit 1 }
        return
    }
}

$previous = @{
    BZA_BASE_URL = $env:BZA_BASE_URL
    BZA_UI_SMOKE_USERNAME = $env:BZA_UI_SMOKE_USERNAME
    BZA_UI_SMOKE_PASSWORD = $env:BZA_UI_SMOKE_PASSWORD
    BZA_UI_SMOKE_SCREENSHOT = $env:BZA_UI_SMOKE_SCREENSHOT
}
try {
    $env:BZA_BASE_URL = $BzaBaseUrl
    $env:BZA_UI_SMOKE_USERNAME = $BzaUsername
    $env:BZA_UI_SMOKE_PASSWORD = $BzaPassword
    $env:BZA_UI_SMOKE_SCREENSHOT = $screenshotPath
    Push-Location $runnerDir
    try {
        $playwrightOutput = @(& $playwrightPath test (Split-Path -Leaf $specPath) --reporter=list --trace=on 2>&1 | ForEach-Object { $_.ToString() })
        $playwrightExitCode = $LASTEXITCODE
    } finally { Pop-Location }
    [IO.File]::WriteAllLines($playwrightLogPath,$playwrightOutput,[Text.UTF8Encoding]::new($false))
    $result.browserClick.exitCode = $playwrightExitCode
    $result.browserClick.logPath = $playwrightLogPath
    $result.browserClick.screenshotPath = $screenshotPath
    if ($playwrightExitCode -eq 0 -and (Test-Path -LiteralPath $screenshotPath -PathType Leaf)) {
        $result.browserClick.status = 'PASSED'
        $result.browserClick.checkedFlow = @('GET /bza','real login','server session navigation','permission-filtered menu','major route clicks','screenshot')
        $result.status = Get-CpfRuntimeStatusText 'Done'
    } else {
        $result.browserClick.status = 'FAILED'
        $result.browserClick.reason = 'Playwright failed or screenshot was not created.'
        $result.status = Get-CpfRuntimeStatusText 'Failed'
    }
} finally {
    $env:BZA_BASE_URL = $previous.BZA_BASE_URL
    $env:BZA_UI_SMOKE_USERNAME = $previous.BZA_UI_SMOKE_USERNAME
    $env:BZA_UI_SMOKE_PASSWORD = $previous.BZA_UI_SMOKE_PASSWORD
    $env:BZA_UI_SMOKE_SCREENSHOT = $previous.BZA_UI_SMOKE_SCREENSHOT
}
$result.finishedAt = [DateTimeOffset]::Now.ToString('o')
Write-CpfRuntimeJson -Path $resultPath -Value $result
if ($result.browserClick.status -ne 'PASSED' -and $RequireBrowserClick) { exit 1 }
Write-Host "BZA UI browser smoke completed: $resultPath"
