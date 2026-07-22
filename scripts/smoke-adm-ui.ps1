param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $LogDir = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $BrowserClick,
    [switch] $RequireBrowserClick
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}

$indexPath = Join-Path $Root "cpf-admin/frontend/index.html"
$sourceRoot = Join-Path $Root "cpf-admin/frontend/src"
$packageLockPath = Join-Path $Root "cpf-admin/frontend/package-lock.json"
$generatedIndexPath = Join-Path $Root "cpf-admin/build/generated/frontend/static/adm/index.html"
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

    $commandName = if ($env:OS -eq "Windows_NT" -and $Name -in @("npm", "npx")) {
        "$Name.cmd"
    } else {
        $Name
    }
    $command = Get-Command $commandName -ErrorAction SilentlyContinue
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
    [System.IO.File]::WriteAllText(
        $resultPath,
        ($Result | ConvertTo-Json -Depth 20),
        [System.Text.UTF8Encoding]::new($false))
    return $resultPath
}

if (-not (Test-Path -LiteralPath $indexPath)) {
    throw "ADM index.html not found: $indexPath"
}
if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
    throw "ADM frontend 정본 소스 경로를 찾지 못했습니다: $sourceRoot"
}
if (-not (Test-Path -LiteralPath $packageLockPath)) {
    throw "ADM package-lock.json을 찾지 못했습니다: $packageLockPath"
}

$index = [System.IO.File]::ReadAllText($indexPath, [System.Text.Encoding]::UTF8)
$sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File |
    Where-Object { $_.Extension -in @(".ts", ".vue") } |
    Sort-Object FullName)
if ($sourceFiles.Count -eq 0) {
    throw "ADM Vue/TypeScript 정본 소스를 찾지 못했습니다: $sourceRoot"
}
$source = ($sourceFiles | ForEach-Object {
    [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
}) -join "`n"
$combined = "$index`n$source"
if ($combined.Contains("window.Vue") -or $combined.Contains("vue.global")) {
    Add-Failure "ADM frontend에 Vue Global Build 참조가 남아 있습니다."
}
if (-not (Test-Path -LiteralPath $generatedIndexPath)) {
    Add-Failure "ADM production frontend build 산출물이 없습니다."
}

# 정적 smoke는 분리된 feature module 전체에서 ADM 메뉴, 버튼, API 경로 계약을 확인합니다.
$requiredMarkers = @(
    "batch",
    "logs",
    "transactionGroups",
    "auditLogs",
    "notifications",
    "downloads",
    "downloadLogDetail",
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
    "CPF_REF_CENTER_CUT_SAMPLE_JOB",
    "/adm/api/center-cut",
    "/adm/api/center-cut/jobs",
    "/adm/api/center-cut/results",
    "parentTransactionGlobalId",
    "childTransactionGlobalId",
    "lastErrorMessage",
    "/adm/api/logs",
    "/adm/api/notifications",
    "/adm/api/downloads",
    "/adm/api/remote-logs/bundle-jobs",
    "downloadRemoteLogBundleJob",
    "비동기 ZIP",
    "/adm/api/transaction-groups",
    "transactionGlobalId",
    "transactionSegmentId",
    "Timeline",
    "Segments",
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
if ($source -match $legacyPattern) {
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
            "cpf-admin/frontend/index.html",
            "cpf-admin/frontend/src/**/*.vue",
            "cpf-admin/frontend/src/**/*.ts",
            "cpf-admin/frontend/package-lock.json",
            "cpf-admin/build/generated/frontend/static/adm/index.html"
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
    # 실제 실행 단계가 Playwright 설치·실행 가능 여부를 판정한다.
    # npx 버전 확인 뒤 같은 패키지를 다시 조회하면 Windows npm 임시 캐시 잠금이 간헐적으로 발생한다.
    $result.environment.playwright = New-Status `
        -Available $true `
        -Path $result.environment.npx.path `
        -Output "resolved at execution time" `
        -ExitCode 0
} else {
    $result.environment.playwright = New-Status -Available $false -Output "npx is not available."
}

$browserToolMissing = -not $result.environment.node.available `
    -or -not $result.environment.npm.available

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
$LogDir = (Resolve-Path -LiteralPath $LogDir).Path
$runnerDir = Join-Path $Root "build/runtime-smoke/playwright-runner"
New-Item -ItemType Directory -Force -Path $runnerDir | Out-Null
$specPath = Join-Path $runnerDir "adm-ui-click-smoke.spec.js"
$screenshotPath = Join-Path $LogDir "adm-ui-click-smoke.png"
$playwrightLogPath = Join-Path $LogDir "adm-ui-click-smoke.log"

$spec = @'
const { test, expect } = require("@playwright/test");

// ADM_UI_BROWSER_CLICK_FLOW
test.use({ channel: "msedge" });

test("ADM UI basic click flow", async ({ page }) => {
  const baseUrl = process.env.ADM_BASE_URL || "http://localhost:8090";
  const username = process.env.ADM_UI_SMOKE_USERNAME || "admin";
  const password = process.env.ADM_UI_SMOKE_PASSWORD;
  if (!password) throw new Error("ADM_UI_SMOKE_PASSWORD is required");
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
  const transactionGroupPanel = page.locator('section[v-show="activeMenu === \'transactionGroups\'"]');
  await transactionGroupPanel.getByLabel("transactionGlobalId").fill(process.env.ADM_UI_SMOKE_TRANSACTION_ID || "");
  await transactionGroupPanel.getByLabel("\uBAA8\uB4C8").fill(process.env.ADM_UI_SMOKE_MODULE_CODE || "REF");
  await transactionGroupPanel.getByLabel("\uC2E4\uD328 \uC5EC\uBD80").selectOption(process.env.ADM_UI_SMOKE_FAILURE_YN || "");
  await transactionGroupPanel.getByLabel("\uD45C\uC900 \uD5E4\uB354 \uAC80\uC0C9").fill("X-Channel-Code");
  await transactionGroupPanel.getByRole("button", { name: "\uC870\uD68C" }).click();
  await expect(transactionGroupPanel.getByText("External Logs")).toBeVisible({ timeout: 15000 });
  const rows = transactionGroupPanel.locator("tbody tr");
  if (await rows.count() > 0) {
    await rows.first().click();
  }
  for (const tabName of ["\uC694\uC57D", "Timeline", "Segments", "\uD45C\uC900 \uD5E4\uB354", "\uD655\uC7A5 \uD5E4\uB354", "External Logs", "\uC6D0\uBCF8 JSON"]) {
    await transactionGroupPanel.getByRole("button", { name: tabName }).click();
  }

  await page.getByRole("button", { name: "\uBC30\uCE58" }).click();
  await expect(page.getByRole("heading", { name: "\uBC30\uCE58 \uAD00\uC81C" })).toBeVisible();

  await page.getByRole("button", { name: "\uAC10\uC0AC \uB85C\uADF8" }).click();
  await expect(page.getByRole("heading", { name: "\uAC10\uC0AC \uB85C\uADF8" })).toBeVisible();

  await page.screenshot({ path: screenshotPath, fullPage: true });
});
'@

$spec | Set-Content -LiteralPath $specPath -Encoding UTF8

if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
    throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
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

    $playwrightPath = Join-Path $runnerDir "node_modules/.bin/playwright.cmd"
    if (-not (Test-Path -LiteralPath $playwrightPath)) {
        $installOutput = & $result.environment.npm.path `
            "install" `
            "--prefix" $runnerDir `
            "--no-save" `
            "--package-lock=false" `
            "--no-audit" `
            "--no-fund" `
            "@playwright/test@1.61.1" 2>&1
        if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $playwrightPath)) {
            throw "ADM UI Playwright runner installation failed: $(($installOutput | Out-String).Trim())"
        }
    }
    $playwrightArguments = @(
        "test",
        (Split-Path -Leaf $specPath),
        "--reporter=list",
        "--trace=on"
    )
    if (Test-Path -LiteralPath $playwrightLogPath) {
        Remove-Item -LiteralPath $playwrightLogPath -Force
    }
    Push-Location $runnerDir
    try {
        $playwrightOutput = & $playwrightPath @playwrightArguments 2>&1
        $playwrightExitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
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
            "transaction group filters filled",
            "transaction group detail tabs clicked",
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
