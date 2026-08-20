param(
  [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
  [string] $ResultDir = "",
  [string] $FrontendRoot = "",
  [string] $BackofficeFrontendUrl = $(if ([string]::IsNullOrWhiteSpace($env:CPF_BACKOFFICE_FRONTEND_URL)) { 'http://127.0.0.1:5173/' } else { $env:CPF_BACKOFFICE_FRONTEND_URL }),
  [switch] $BrowserClick,
  [switch] $RequireBrowserClick
)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Utf8=[Text.UTF8Encoding]::new($false); [Console]::OutputEncoding=$Utf8; $OutputEncoding=$Utf8
$Root=(Resolve-Path -LiteralPath $Root).Path
if([string]::IsNullOrWhiteSpace($ResultDir)){$ResultDir=Join-Path $Root 'build/runtime-evidence/browser'}
New-Item -ItemType Directory -Force -Path $ResultDir|Out-Null
if([string]::IsNullOrWhiteSpace($FrontendRoot)){$FrontendRoot=Join-Path $Root 'cpf-backoffice-web/frontend'}elseif(-not[IO.Path]::IsPathRooted($FrontendRoot)){$FrontendRoot=Join-Path $Root $FrontendRoot}
$required=@(
  'src/App.vue','src/router/index.ts','src/shared/api/channelHttpClient.ts','src/generated/backoffice-api.ts',
  'src/features/employees/pages/EmployeesPage.vue','src/features/approvals/pages/ApprovalInboxPage.vue',
  'src/features/authorization/pages/AuthorizationPage.vue','src/features/dashboard/pages/DashboardPage.vue',
  'openapi/cpf-openapi.json','package-lock.json'
)|%{Join-Path $FrontendRoot $_}
$missing=@($required|?{-not(Test-Path $_ -PathType Leaf)}); if($missing){throw "BACKOFFICE reference UI source incomplete: $($missing -join ', ')"}
$app=Get-Content (Join-Path $FrontendRoot 'src/App.vue') -Raw -Encoding UTF8
$router=Get-Content (Join-Path $FrontendRoot 'src/router/index.ts') -Raw -Encoding UTF8
$transport=Get-Content (Join-Path $FrontendRoot 'src/shared/api/channelHttpClient.ts') -Raw -Encoding UTF8
$generated=Get-Content (Join-Path $FrontendRoot 'src/generated/backoffice-api.ts') -Raw -Encoding UTF8
$markers=[ordered]@{
  referenceSurface=$app.Contains('CPF Backoffice') -and $app.Contains('Reference Backoffice')
  fourFeatureRoutes=(@("path: '/'","path: '/employees'","path: '/approvals'","path: '/authorization'")|?{$router -notlike "*$_*"}).Count -eq 0
  channelOnly=$transport.Contains('VITE_MBW_WEB_BASE_URL') -and $transport.Contains("credentials: 'include'")
  browserOwnsNoCpfHeader=($transport -notmatch 'X-(?:Transaction|Original-System|System-Code|Caller-System|Target-System|Target-Operation)')
  generatedReferenceClient=$generated.Contains('supportDashboard') -and $generated.Contains('backofficeFindEmployeesPage') -and $generated.Contains('approvalInbox')
  noNativePrompt=-not ((Get-ChildItem (Join-Path $FrontendRoot 'src') -Recurse -File -Include *.ts,*.vue|Get-Content -Raw -ErrorAction SilentlyContinue) -match 'window\.prompt|window\.confirm|\bprompt\(')
}
$failed=@($markers.GetEnumerator()|?{-not[bool]$_.Value}|% Key)
$result=[ordered]@{schemaVersion=3;checkedAt=[DateTimeOffset]::Now.ToString('o');status=$(if($failed.Count){'FAILED'}else{'PASS'});frontendRoot=$FrontendRoot;frontendUrl=$BackofficeFrontendUrl;markers=$markers;browser='NOT_REQUESTED';sanitized=$true}
$resultPath=Join-Path $ResultDir 'backoffice-ui-runtime-result.sanitized.json'
if($failed.Count){$result.failed=$failed;[IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 10),$Utf8);throw "BACKOFFICE reference UI static contract failed: $($failed -join ', ')"}
if($BrowserClick){
  $npm=Get-Command npm -ErrorAction SilentlyContinue
  if(-not $npm){$result.browser='SKIPPED_NPM_UNAVAILABLE';if($RequireBrowserClick){$result.status='FAILED'}}
  else{
    $runner=Join-Path $Root 'build/runtime-smoke/backoffice-reference-playwright';New-Item -ItemType Directory -Force -Path $runner|Out-Null
    $spec=Join-Path $runner 'backoffice-reference.spec.js'
    @'
const { test, expect } = require('@playwright/test');
test('BACKOFFICE reference navigation', async ({page}) => {
 const base=process.env.CPF_BACKOFFICE_FRONTEND_URL || 'http://127.0.0.1:5173/';
 await page.goto(base,{waitUntil:'domcontentloaded'});
 await expect(page.getByRole('heading',{name:'CPF BACKOFFICE'})).toBeVisible();
 for (const label of ['직원 관리','결재','권한']) { await page.getByRole('link',{name:label}).click(); await expect(page.locator('main h2')).toBeVisible(); }
});
'@ | Set-Content -LiteralPath $spec -Encoding UTF8
    if(-not(Test-Path (Join-Path $runner 'node_modules/@playwright/test/package.json'))){& $npm.Source install --prefix $runner --no-save --package-lock=false --no-audit --no-fund '@playwright/test@1.62.0'|Out-Null;if($LASTEXITCODE -ne 0){throw 'Playwright install failed'}}
    $env:CPF_BACKOFFICE_FRONTEND_URL=$BackofficeFrontendUrl
    & (Join-Path $runner 'node_modules/.bin/playwright.cmd') test $spec --reporter=line
    if($LASTEXITCODE -ne 0){$result.browser='FAILED';$result.status='FAILED'}else{$result.browser='PASS'}
  }
}
[IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 10),$Utf8)
if($result.status -ne 'PASS'){throw "BACKOFFICE reference UI smoke failed: $resultPath"}
Write-Host "BACKOFFICE reference UI smoke PASS: $resultPath"
