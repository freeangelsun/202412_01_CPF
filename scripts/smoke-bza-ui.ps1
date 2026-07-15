param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$htmlPath = Join-Path $Root "bza/src/main/resources/static/bza/index.html"
$scriptPath = Join-Path $Root "bza/src/main/resources/static/bza/bza.js"
$resultPath = Join-Path $ResultDir "bza-ui-static-result.sanitized.json"
$result = [ordered]@{
    checkedAt = [DateTimeOffset]::Now.ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    markers = [ordered]@{}
    nodeSyntax = [ordered]@{ available = $false; passed = $false }
}

try {
    $html = [System.IO.File]::ReadAllText($htmlPath, [System.Text.Encoding]::UTF8)
    $script = [System.IO.File]::ReadAllText($scriptPath, [System.Text.Encoding]::UTF8)
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
    }
    foreach ($key in $markers.Keys) {
        $result.markers[$key] = [bool] $markers[$key]
    }

    $node = Get-Command node -ErrorAction SilentlyContinue
    if ($node) {
        $nodeOutput = @(& $node.Source --check $scriptPath 2>&1 | ForEach-Object { $_.ToString() })
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
