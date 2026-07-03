param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $LogDir = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $BrowserClick,
    [switch] $RequireBrowserClick
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}

$indexPath = Join-Path $Root "adm/src/main/resources/static/adm/index.html"
$scriptPath = Join-Path $Root "adm/src/main/resources/static/adm/adm.js"
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

function New-Status {
    param(
        [bool] $Available,
        [string] $Path = "",
        [string] $Output = "",
        [int] $ExitCode = -1
    )

    return [ordered]@{
        available = $Available
        path = $Path
        output = $Output
        exitCode = $ExitCode
    }
}

function Invoke-Tool {
    param(
        [string] $Path,
        [string[]] $Arguments = @()
    )

    try {
        $output = & $Path @Arguments 2>&1
        $exitCode = $LASTEXITCODE
        return New-Status -Available ($exitCode -eq 0) -Path $Path -Output (($output | Out-String).Trim()) -ExitCode $exitCode
    } catch {
        return New-Status -Available $false -Path $Path -Output $_.Exception.Message -ExitCode -1
    }
}

function Get-ToolStatus {
    param(
        [string] $Name,
        [string[]] $VersionArguments = @()
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        return New-Status -Available $false
    }

    if ($VersionArguments.Count -eq 0) {
        return New-Status -Available $true -Path $command.Source
    }

    return Invoke-Tool -Path $command.Source -Arguments $VersionArguments
}

function Save-BrowserResult {
    param([object] $Result)

    $Result.finishedAt = (Get-Date).ToString("o")
    New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
    $resultPath = Join-Path $LogDir "adm-ui-browser-smoke-result.json"
    $Result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
    return $resultPath
}

if (-not (Test-Path -LiteralPath $indexPath)) {
    throw "ADM index.html not found: $indexPath"
}
if (-not (Test-Path -LiteralPath $scriptPath)) {
    throw "ADM adm.js not found: $scriptPath"
}

$index = [System.IO.File]::ReadAllText($indexPath, [System.Text.Encoding]::UTF8)
$script = [System.IO.File]::ReadAllText($scriptPath, [System.Text.Encoding]::UTF8)
$combined = "$index`n$script"

# Static smoke checks core ADM UI menu, button, and API route markers.
$requiredMarkers = @(
    "batch",
    "logs",
    "transactionGroups",
    "auditLogs",
    "notifications",
    "downloads",
    "downloadLogDetail",
    "bizadm",
    "exs",
    "/api/bizadm",
    "/api/exs",
    "/adm/api/batch",
    "/adm/api/batch/jobs",
    "/adm/api/batch/schedules",
    "/adm/api/batch/instances",
    "/adm/api/batch/executions",
    "/adm/api/batch/steps",
    "/adm/api/batch/workers",
    "/adm/api/batch/locks",
    "/adm/api/batch/ghost-candidates",
    "/adm/api/batch/operations",
    "/adm/api/batch/relations",
    "/adm/api/batch/execution-targets",
    "/adm/api/batch/calendar",
    "/adm/api/batch/scheduler/run-once",
    "Center-Cut Job ID",
    "CPF_XYZ_CENTER_CUT_SAMPLE_JOB",
    "/adm/api/center-cut",
    "/adm/api/center-cut/jobs",
    "/adm/api/center-cut/results",
    "parentTransactionGlobalId",
    "childTransactionGlobalId",
    "lastErrorMessage",
    "/adm/api/logs",
    "/adm/api/notifications",
    "/adm/api/downloads",
    "/adm/api/transaction-groups",
    "transactionGlobalId",
    "transactionSegmentId",
    "segment timeline",
    "External Logs",
    "standardHeaderValue",
    "extensionHeaderValue",
    "X-Cpf-Ext-*",
    "Authorization"
)

foreach ($marker in $requiredMarkers) {
    if ($combined.IndexOf($marker, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        Add-Failure "ADM UI required marker is missing: $marker"
    }
}

$legacyPattern = ("F" + "PS") + "|" + ("F" + "ps") + "|" + ("f" + "ps")
if ($script -match $legacyPattern) {
    Add-Failure "ADM UI script still contains a legacy project name."
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

if (-not $BrowserClick) {
    Write-Host "ADM UI smoke check passed."
    return
}

# Browser click smoke runs only when browser automation tools are available.
# Missing tools produce SKIPPED evidence instead of a false success.
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    staticUi = [ordered]@{
        status = "PASSED"
        checkedFiles = @(
            "adm/src/main/resources/static/adm/index.html",
            "adm/src/main/resources/static/adm/adm.js"
        )
    }
    environment = [ordered]@{}
    browserClick = [ordered]@{}
    artifacts = [ordered]@{}
}

$result.environment.node = Get-ToolStatus -Name "node" -VersionArguments @("-v")
$result.environment.npm = Get-ToolStatus -Name "npm" -VersionArguments @("-v")
$result.environment.npx = Get-ToolStatus -Name "npx" -VersionArguments @("--version")
$result.environment.chrome = Get-ToolStatus -Name "chrome"
$result.environment.msedge = Get-ToolStatus -Name "msedge"
$result.environment.chromedriver = Get-ToolStatus -Name "chromedriver"
$result.environment.geckodriver = Get-ToolStatus -Name "geckodriver"

if ($result.environment.npx.available) {
    $result.environment.playwright = Invoke-Tool -Path $result.environment.npx.path -Arguments @("playwright", "--version")
} else {
    $result.environment.playwright = New-Status -Available $false -Output "npx is not available."
}

$browserToolMissing = -not $result.environment.node.available `
    -or -not $result.environment.npm.available `
    -or -not $result.environment.npx.available `
    -or -not $result.environment.playwright.available

if ($browserToolMissing) {
    $result.browserClick.status = "SKIPPED"
    $result.browserClick.reason = "Node/npm/npx or Playwright is not available in PATH."
    $resultPath = Save-BrowserResult -Result $result
    Write-Host "ADM UI browser click smoke skipped. Result: $resultPath"
    if ($RequireBrowserClick) {
        exit 1
    }
    return
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$specPath = Join-Path $LogDir "adm-ui-click-smoke.spec.js"
$screenshotPath = Join-Path $LogDir "adm-ui-click-smoke.png"
$playwrightLogPath = Join-Path $LogDir "adm-ui-click-smoke.log"

$spec = @'
const { test, expect } = require("@playwright/test");

// ADM_UI_BROWSER_CLICK_FLOW
test("ADM UI basic click flow", async ({ page }) => {
  const baseUrl = process.env.ADM_BASE_URL || "http://localhost:8090";
  const username = process.env.ADM_UI_SMOKE_USERNAME || "admin";
  const password = process.env.ADM_UI_SMOKE_PASSWORD || "Adm!n12345";
  const screenshotPath = process.env.ADM_UI_SMOKE_SCREENSHOT || "adm-ui-click-smoke.png";

  await page.goto(`${baseUrl}/adm`, { waitUntil: "domcontentloaded" });
  await expect(page.getByText("\uC6B4\uC601\uC790 \uB85C\uADF8\uC778")).toBeVisible({ timeout: 15000 });
  await page.getByLabel("\uC6B4\uC601\uC790 ID").fill(username);
  await page.getByLabel("\uBE44\uBC00\uBC88\uD638").fill(password);
  await page.getByRole("button", { name: "\uB85C\uADF8\uC778" }).click();

  await expect(page.getByText("\uD504\uB808\uC784\uC6CC\uD06C \uC6B4\uC601 \uCF58\uC194")).toBeVisible({ timeout: 15000 });
  await expect(page.getByRole("button", { name: "\uAC70\uB798 \uB85C\uADF8" })).toBeVisible();

  await page.getByRole("button", { name: "\uAC70\uB798 \uB85C\uADF8" }).click();
  await expect(page.getByRole("heading", { name: "\uAC70\uB798 \uB85C\uADF8" })).toBeVisible();

  await page.getByRole("button", { name: "\uAC70\uB798 \uADF8\uB8F9" }).click();
  await expect(page.getByRole("heading", { name: "\uAC70\uB798 \uADF8\uB8F9" })).toBeVisible();

  await page.getByRole("button", { name: "\uBC30\uCE58" }).click();
  await expect(page.getByRole("heading", { name: "\uBC30\uCE58 \uAD00\uC81C" })).toBeVisible();

  await page.getByRole("button", { name: "\uAC10\uC0AC \uB85C\uADF8" }).click();
  await expect(page.getByRole("heading", { name: "\uAC10\uC0AC \uB85C\uADF8" })).toBeVisible();

  await page.screenshot({ path: screenshotPath, fullPage: true });
});
'@

$spec | Set-Content -LiteralPath $specPath -Encoding UTF8

if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
    $AdmPassword = "Adm!n12345"
}

$previousAdmBaseUrl = $env:ADM_BASE_URL
$previousUiUsername = $env:ADM_UI_SMOKE_USERNAME
$previousUiPassword = $env:ADM_UI_SMOKE_PASSWORD
$previousUiScreenshot = $env:ADM_UI_SMOKE_SCREENSHOT

try {
    $env:ADM_BASE_URL = $AdmBaseUrl
    $env:ADM_UI_SMOKE_USERNAME = $AdmUsername
    $env:ADM_UI_SMOKE_PASSWORD = $AdmPassword
    $env:ADM_UI_SMOKE_SCREENSHOT = $screenshotPath

    $npxPath = $result.environment.npx.path
    $playwrightArguments = @(
        "playwright",
        "test",
        $specPath,
        "--reporter=list",
        "--trace=on",
        "--screenshot=on"
    )
    $playwrightOutput = & $npxPath @playwrightArguments 2>&1
    $playwrightExitCode = $LASTEXITCODE
    $playwrightOutput | Set-Content -LiteralPath $playwrightLogPath -Encoding UTF8

    $result.browserClick.exitCode = $playwrightExitCode
    $result.browserClick.logPath = $playwrightLogPath
    $result.artifacts.specPath = $specPath
    $result.artifacts.screenshotPath = $screenshotPath

    if ($playwrightExitCode -eq 0 -and (Test-Path -LiteralPath $screenshotPath)) {
        $result.browserClick.status = "PASSED"
        $result.browserClick.checkedFlow = @(
            "GET /adm",
            "login page visible",
            "login button click",
            "transaction log menu click",
            "transaction group menu click",
            "batch menu click",
            "audit log menu click",
            "screenshot saved"
        )
    } else {
        $result.browserClick.status = "FAILED"
        $result.browserClick.reason = "Playwright failed or screenshot was not created."
    }
} finally {
    $env:ADM_BASE_URL = $previousAdmBaseUrl
    $env:ADM_UI_SMOKE_USERNAME = $previousUiUsername
    $env:ADM_UI_SMOKE_PASSWORD = $previousUiPassword
    $env:ADM_UI_SMOKE_SCREENSHOT = $previousUiScreenshot
}

$resultPath = Save-BrowserResult -Result $result
Write-Host "ADM UI browser click smoke completed. Result: $resultPath"

if ($result.browserClick.status -ne "PASSED" -and $RequireBrowserClick) {
    exit 1
}
