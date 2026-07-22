param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$htmlPath = Join-Path $Root "cpf-biz-admin/frontend/src/App.vue"
$scriptPath = Join-Path $Root "cpf-biz-admin/frontend/src/features/console.ts"
$helperPath = Join-Path $Root "cpf-biz-admin/frontend/src/shared/html.ts"
$packageLockPath = Join-Path $Root "cpf-biz-admin/frontend/package-lock.json"
$generatedIndexPath = Join-Path $Root "cpf-biz-admin/build/generated/frontend/static/bza/index.html"
$resultPath = Join-Path $ResultDir "bza-ui-static-result.sanitized.json"
$result = [ordered]@{
    checkedAt = [DateTimeOffset]::Now.ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    markers = [ordered]@{}
    nodeSyntax = [ordered]@{ available = $false; passed = $false }
}

try {
    $html = [System.IO.File]::ReadAllText($htmlPath, [System.Text.Encoding]::UTF8)
    $script = [System.IO.File]::ReadAllText($scriptPath, [System.Text.Encoding]::UTF8) `
        + [System.IO.File]::ReadAllText($helperPath, [System.Text.Encoding]::UTF8)
    $markers = [ordered]@{
        loginAccessibility = $html.Contains('aria-labelledby="loginTitle"') -and $html.Contains('aria-live="polite"')
        passwordAutocomplete = $html.Contains('autocomplete="current-password"')
        sessionOnlyTokenStorage = -not $script.Contains("localStorage") -and $script.Contains("sessionStorage")
        serverPermissionAwareMenu = $script.Contains("function hasMenu") -and $script.Contains("function hasPermission")
        refreshRotationClient = $script.Contains("/api/bza/auth/refresh") -and $script.Contains("setTokens(refreshed)")
        passwordChange = $script.Contains("/api/bza/auth/password/change") -and $script.Contains("newPasswordConfirm")
        sessionManagement = $script.Contains("/api/bza/auth/sessions") `
            -and $script.Contains("function loadSessions")
        organization = $script.Contains("/api/bza/backoffice/organizations")
        employee = $script.Contains("/api/bza/backoffice/employees")
        approval = $script.Contains("/api/bza/backoffice/approvals") -and $script.Contains("idempotencyKey")
        role = $script.Contains("/api/bza/roles")
        menu = $script.Contains("/api/bza/menus")
        permission = $script.Contains("/api/bza/permissions")
        writeEditors = $html.Contains('id="entityDialog"') `
            -and $script.Contains("function loadManagedTable") `
            -and $script.Contains("function saveEntity") `
            -and $script.Contains("method:'POST'") `
            -and -not $script.Contains("payload.requestUser")
        authenticatedAuditActor = -not $script.Contains("actorEmployeeNo:actor") `
            -and -not $script.Contains("처리 직원 번호를 입력하세요")
        notification = $script.Contains("/api/bza/notifications") `
            -and $script.Contains("function loadNotifications")
        attachment = $script.Contains("/api/bza/attachments") `
            -and $script.Contains("function loadAttachments")
        savedSearch = $script.Contains("/api/bza/saved-searches")
        permissionAnalysis = $script.Contains("/api/bza/permissions/compare") `
            -and $script.Contains("/api/bza/permissions/simulate")
        customer = $script.Contains("/api/bza/customers")
        product = $script.Contains("/api/bza/products")
        order = $script.Contains("/api/bza/orders")
        setting = $script.Contains("/api/bza/settings")
        downloadAudit = $script.Contains("/api/bza/downloads") `
            -and $script.Contains("/api/bza/download-audits")
        businessAudit = $script.Contains("/api/bza/backoffice/audits")
        outputEscaping = $script.Contains("function escapeHtml")
        legacyNameRemoved = -not $script.Contains("BIZADM") -and -not $html.Contains("BIZADM")
        vueSfcTypeScript = $html.Contains('<script setup lang="ts">')
        packageLock = Test-Path -LiteralPath $packageLockPath
        productionBuild = Test-Path -LiteralPath $generatedIndexPath
        noGlobalVue = -not $script.Contains("window.Vue") -and -not $script.Contains("vue.global")
    }
    foreach ($key in $markers.Keys) {
        $result.markers[$key] = [bool] $markers[$key]
    }

    $npm = Get-Command $(if ($env:OS -eq "Windows_NT") { "npm.cmd" } else { "npm" }) -ErrorAction SilentlyContinue
    if ($npm) {
        Push-Location (Join-Path $Root "cpf-biz-admin/frontend")
        try {
            $nodeOutput = @(& $npm.Source run typecheck 2>&1 | ForEach-Object { $_.ToString() })
        } finally {
            Pop-Location
        }
        $result.nodeSyntax.available = $true
        $result.nodeSyntax.passed = ($LASTEXITCODE -eq 0)
        $result.nodeSyntax.output = ($nodeOutput -join "`n")
    }

    $missing = @($result.markers.GetEnumerator() | Where-Object { -not $_.Value })
    $syntaxFailed = $result.nodeSyntax.available -and -not $result.nodeSyntax.passed
    $result.status = $(if ($missing.Count -eq 0 -and -not $syntaxFailed) {
        Get-CpfRuntimeStatusText "Done"
    } else {
        Get-CpfRuntimeStatusText "Failed"
    })
    $result.missingMarkers = @($missing | ForEach-Object Key)
    $result.finishedAt = [DateTimeOffset]::Now.ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.error = $_.Exception.Message
    $result.finishedAt = [DateTimeOffset]::Now.ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    throw
}

Write-Host "BZA UI 정적 스모크 통과: $resultPath"
