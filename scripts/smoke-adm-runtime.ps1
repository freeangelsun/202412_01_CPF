param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 90,
    [string] $LogDir = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $IncludePermissionWriteSmoke,
    [string] $PermissionResultPath = "",
    [switch] $BuildBeforeRun,
    [switch] $RequireBrowserClick
)

$ErrorActionPreference = "Stop"
    $result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    process = [ordered]@{}
    health = [ordered]@{}
    openapi = [ordered]@{}
    admOperationApi = [ordered]@{}
    permissionWriteApi = [ordered]@{}
    batchApi = [ordered]@{}
    centerCutApi = [ordered]@{}
    transactionMetaApi = [ordered]@{}
    staticUi = [ordered]@{}
    browserClick = [ordered]@{}
    cleanup = [ordered]@{}
}
$smokeSequence = 0

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$stdoutLog = Join-Path $LogDir "adm-bootRun.out.log"
$stderrLog = Join-Path $LogDir "adm-bootRun.err.log"
$resultPath = Join-Path $LogDir "adm-runtime-smoke-result.json"
if ([string]::IsNullOrWhiteSpace($PermissionResultPath)) {
    $PermissionResultPath = Join-Path $LogDir "adm-permission-runtime-result.json"
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function ConvertFrom-Utf8JsonResponse {
    param([object] $Response)
    $content = $Response.Content
    if ($null -ne $Response.RawContentStream) {
        $stream = $Response.RawContentStream
        if ($stream.CanSeek) {
            $stream.Position = 0
        }
        $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true, 1024, $true)
        try {
            $content = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    }
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }
    return $content | ConvertFrom-Json
}

function Invoke-SmokeJson {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null,
        [int] $TimeoutSec = 15
    )

    $mergedHeaders = New-SmokeHeaders
    foreach ($key in $Headers.Keys) {
        $mergedHeaders[$key] = $Headers[$key]
    }

    $invokeParams = @{
        Method = $Method
        Uri = $Uri
        TimeoutSec = $TimeoutSec
        Headers = $mergedHeaders
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    ConvertFrom-Utf8JsonResponse (Invoke-WebRequest @invokeParams)
}

function Invoke-SmokeForStatus {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null,
        [int] $TimeoutSec = 15
    )

    $mergedHeaders = New-SmokeHeaders
    foreach ($key in $Headers.Keys) {
        $mergedHeaders[$key] = $Headers[$key]
    }

    $invokeParams = @{
        Method = $Method
        Uri = $Uri
        TimeoutSec = $TimeoutSec
        Headers = $mergedHeaders
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }

    try {
        $response = Invoke-WebRequest @invokeParams
        return [ordered]@{
            statusCode = [int] $response.StatusCode
            body = ConvertFrom-Utf8JsonResponse $response
        }
    } catch {
        $statusCode = 0
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int] $_.Exception.Response.StatusCode
        }
        return [ordered]@{
            statusCode = $statusCode
            error = $_.Exception.Message
        }
    }
}

function Get-SmokeJsonCount {
    param([object] $Value)

    if ($null -eq $Value) {
        return 0
    }
    if ($Value -is [System.Array]) {
        return $Value.Count
    }
    return 1
}

function Save-PermissionSmokeResult {
    param([object] $PermissionResult)

    $PermissionResult.finishedAt = (Get-Date).ToString("o")
    $PermissionResult | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $PermissionResultPath -Encoding UTF8
}

function Invoke-AdmPermissionWriteSmoke {
    param(
        [hashtable] $AdminHeaders,
        [string] $AdminPassword
    )

    $permissionResult = [ordered]@{
        startedAt = (Get-Date).ToString("o")
        admBaseUrl = $AdmBaseUrl
        operatorId = $AdmUsername
        writes = [ordered]@{}
        audit = [ordered]@{}
        filter = [ordered]@{}
        cleanup = [ordered]@{}
    }

    $reasonStamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $reason = "ADM permission runtime smoke $reasonStamp"
    $viewerOperatorId = "smoke_viewer_runtime"
    $viewerPassword = "SmokeRuntime!20260701"
    $apiPermissionId = "API_PERMISSION_WRITE_PUT"

    try {
        $operators = @(Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/operators" -Headers $AdminHeaders)
        $viewerExists = @($operators | Where-Object { $_.operatorId -eq $viewerOperatorId }).Count -gt 0
        if ($viewerExists) {
            $resetViewer = Invoke-SmokeForStatus -Method Post -Uri "$AdmBaseUrl/adm/api/operators/$viewerOperatorId/password/reset" -Headers $AdminHeaders -Body @{
                newPassword = $viewerPassword
                forceChange = $false
                requestUser = "runtime-smoke"
                reason = "$reason - reset reusable viewer operator"
            }
            $roleViewer = Invoke-SmokeForStatus -Method Put -Uri "$AdmBaseUrl/adm/api/operators/$viewerOperatorId/roles" -Headers $AdminHeaders -Body @{
                roleIds = @("ADM_VIEWER")
                requestUser = "runtime-smoke"
                reason = "$reason - ensure viewer role"
            }
            $permissionResult.writes.viewerOperatorReady = [ordered]@{
                mode = "REUSED"
                resetStatusCode = $resetViewer.statusCode
                roleStatusCode = $roleViewer.statusCode
                operatorId = $viewerOperatorId
            }
            if ($resetViewer.statusCode -lt 200 -or $resetViewer.statusCode -ge 300) {
                throw "viewer operator password reset failed. status=$($resetViewer.statusCode)"
            }
            if ($roleViewer.statusCode -lt 200 -or $roleViewer.statusCode -ge 300) {
                throw "viewer operator role update failed. status=$($roleViewer.statusCode)"
            }
        } else {
            $createViewer = Invoke-SmokeForStatus -Method Post -Uri "$AdmBaseUrl/adm/api/operators" -Headers $AdminHeaders -Body @{
                operatorId = $viewerOperatorId
                operatorName = "Runtime Smoke Viewer"
                password = $viewerPassword
                roleIds = @("ADM_VIEWER")
                requestUser = "runtime-smoke"
                reason = "$reason - create viewer operator"
            }
            $permissionResult.writes.viewerOperatorReady = [ordered]@{
                mode = "CREATED"
                statusCode = $createViewer.statusCode
                operatorId = $viewerOperatorId
            }
            if ($createViewer.statusCode -lt 200 -or $createViewer.statusCode -ge 300) {
                throw "viewer operator create failed. status=$($createViewer.statusCode)"
            }
        }

        $menuWrite = Invoke-SmokeForStatus -Method Put -Uri "$AdmBaseUrl/adm/api/permissions/roles/ADM_VIEWER/menus/PERMISSION" -Headers $AdminHeaders -Body @{
            readYn = "Y"
            writeYn = "N"
            deleteYn = "N"
            requestUser = "runtime-smoke"
            reason = "$reason - save menu permission"
        }
        $permissionResult.writes.menuPermission = [ordered]@{
            method = "PUT"
            path = "/adm/api/permissions/roles/ADM_VIEWER/menus/PERMISSION"
            statusCode = $menuWrite.statusCode
        }
        if ($menuWrite.statusCode -lt 200 -or $menuWrite.statusCode -ge 300) {
            throw "menu permission update failed. status=$($menuWrite.statusCode)"
        }

        $apiWrite = Invoke-SmokeForStatus -Method Put -Uri "$AdmBaseUrl/adm/api/permissions/roles/ADM_VIEWER/api-permissions/$apiPermissionId" -Headers $AdminHeaders -Body @{
            allowYn = "N"
            requestUser = "runtime-smoke"
            reason = "$reason - save API permission"
        }
        $permissionResult.writes.roleApiPermission = [ordered]@{
            method = "PUT"
            path = "/adm/api/permissions/roles/ADM_VIEWER/api-permissions/$apiPermissionId"
            statusCode = $apiWrite.statusCode
        }
        if ($apiWrite.statusCode -lt 200 -or $apiWrite.statusCode -ge 300) {
            throw "role API permission update failed. status=$($apiWrite.statusCode)"
        }

        $matrix = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/permissions/api-matrix" -Headers $AdminHeaders
        $matrixRows = @($matrix | Where-Object {
                $_.ROLE_ID -eq "ADM_VIEWER" -and $_.API_PERMISSION_ID -eq $apiPermissionId
            })
        $permissionResult.writes.roleApiPermissionReadBack = [ordered]@{
            matchedRows = Get-SmokeJsonCount $matrixRows
            allowYn = if ($matrixRows.Count -gt 0) { [string] $matrixRows[0].ALLOW_YN } else { $null }
        }
        if ($matrixRows.Count -eq 0 -or [string] $matrixRows[0].ALLOW_YN -ne "N") {
            throw "role API permission read-back mismatch."
        }

        $auditTargetId = [uri]::EscapeDataString("ADM_VIEWER:$apiPermissionId")
        $audit = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/audit-logs?actionType=ROLE_API_PERMISSION_UPDATE&targetType=adm_role_api_permission&targetId=$auditTargetId&limit=5" -Headers $AdminHeaders
        $auditItems = @($audit.items)
        $permissionResult.audit.roleApiPermission = [ordered]@{
            path = "/adm/api/audit-logs"
            matchedRows = Get-SmokeJsonCount $auditItems
            hasReason = ($auditItems.Count -gt 0 -and -not [string]::IsNullOrWhiteSpace([string] $auditItems[0].REASON))
            hasBeforeAfterOrDiff = ($auditItems.Count -gt 0 -and (
                    -not [string]::IsNullOrWhiteSpace([string] $auditItems[0].BEFORE_DATA) -or
                    -not [string]::IsNullOrWhiteSpace([string] $auditItems[0].AFTER_DATA) -or
                    -not [string]::IsNullOrWhiteSpace([string] $auditItems[0].DIFF_DATA)
                ))
        }
        if ($auditItems.Count -eq 0 -or -not $permissionResult.audit.roleApiPermission.hasReason) {
            throw "role API permission audit log was not found."
        }

        $viewerLogin = Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
            operatorId = $viewerOperatorId
            password = $viewerPassword
        }
        if ([string]::IsNullOrWhiteSpace($viewerLogin.accessToken)) {
            throw "viewer login response does not contain accessToken."
        }
        $viewerHeaders = @{ Authorization = "Bearer $($viewerLogin.accessToken)" }
        $viewerCenterCutRead = Invoke-SmokeForStatus -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs" -Headers $viewerHeaders
        $viewerBlocked = Invoke-SmokeForStatus -Method Put -Uri "$AdmBaseUrl/adm/api/permissions/roles/ADM_VIEWER/api-permissions/$apiPermissionId" -Headers $viewerHeaders -Body @{
            allowYn = "Y"
            requestUser = $viewerOperatorId
            reason = "$reason - verify denied viewer write"
        }
        $adminAllowed = Invoke-SmokeForStatus -Method Get -Uri "$AdmBaseUrl/adm/api/permissions/api-permissions" -Headers $AdminHeaders
        $permissionResult.filter.viewerWriteDenied = [ordered]@{
            method = "PUT"
            path = "/adm/api/permissions/roles/ADM_VIEWER/api-permissions/$apiPermissionId"
            statusCode = $viewerBlocked.statusCode
            expected = 403
        }
        $permissionResult.filter.viewerCenterCutReadAllowed = [ordered]@{
            method = "GET"
            path = "/adm/api/center-cut/jobs"
            statusCode = $viewerCenterCutRead.statusCode
            expected = "2xx"
        }
        $permissionResult.filter.adminReadAllowed = [ordered]@{
            method = "GET"
            path = "/adm/api/permissions/api-permissions"
            statusCode = $adminAllowed.statusCode
            expected = "2xx"
        }
        if ($viewerBlocked.statusCode -ne 403) {
            throw "viewer write was not denied with 403. status=$($viewerBlocked.statusCode)"
        }
        if ($viewerCenterCutRead.statusCode -lt 200 -or $viewerCenterCutRead.statusCode -ge 300) {
            throw "viewer center-cut read was not allowed. status=$($viewerCenterCutRead.statusCode)"
        }
        if ($adminAllowed.statusCode -lt 200 -or $adminAllowed.statusCode -ge 300) {
            throw "admin read was not allowed. status=$($adminAllowed.statusCode)"
        }

        $permissionResult.status = "PASSED"
    } catch {
        $permissionResult.status = "FAILED"
        $permissionResult.error = $_.Exception.Message
        Save-PermissionSmokeResult $permissionResult
        throw
    }

    Save-PermissionSmokeResult $permissionResult
    return $permissionResult
}

function New-SmokeHeaders {
    $script:smokeSequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $sequence = $script:smokeSequence.ToString("0000000")
    return @{
        "X-Transaction-Id" = "$timestamp" + "ADM" + "smoke01" + $sequence
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
    }
}

function Test-HealthReady {
    try {
        $health = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/health" -TimeoutSec 5
        return $health
    } catch {
        return $null
    }
}

function Stop-ProcessTree {
    param([int] $ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId ([int] $child.ProcessId)
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
    if (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue) {
        & "$env:WINDIR\System32\taskkill.exe" /PID $ProcessId /T /F | Out-Null
    }
}

function Resolve-AdmPort {
    $uri = [Uri] $AdmBaseUrl
    if ($uri.IsDefaultPort) {
        if ($uri.Scheme -eq "https") {
            return 443
        }
        return 80
    }
    return $uri.Port
}

function Stop-AdmPortOwner {
    $port = Resolve-AdmPort
    $owners = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)
    foreach ($owner in $owners) {
        if ($owner -and $owner -gt 0) {
            Stop-ProcessTree -ProcessId ([int] $owner)
        }
    }
}

function Test-BrowserAutomationAvailable {
    $commands = @("node", "npx", "chrome", "chrome.exe", "msedge", "msedge.exe", "chromium", "chromium.exe")
    foreach ($command in $commands) {
        if (Get-Command $command -ErrorAction SilentlyContinue) {
            return $command
        }
    }
    return $null
}

function Resolve-GradleExecutable {
    $cachedGradle = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.11.1-bin" `
        -Recurse `
        -Filter "gradle.bat" `
        -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -ne $cachedGradle) {
        return $cachedGradle.FullName
    }
    return Join-Path $Root "gradlew.bat"
}

function Resolve-AdmBootJar {
    $libsDir = Join-Path $Root "adm/build/libs"
    if (-not (Test-Path -LiteralPath $libsDir)) {
        return $null
    }
    $jar = Get-ChildItem -LiteralPath $libsDir -File -Filter "*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*plain*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jar) {
        return $null
    }
    return $jar.FullName
}

function Invoke-AdmBootJarBuild {
    $gradle = Join-Path $Root "gradlew.bat"
    if (-not (Test-Path -LiteralPath $gradle)) {
        throw "Gradle wrapper was not found: $gradle"
    }

    $buildLog = Join-Path $LogDir "adm-bootJar-before-smoke.log"
    $result.process.buildBeforeRun = [ordered]@{
        requested = $true
        command = ".\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain"
        log = $buildLog
    }

    $buildOutput = & $gradle ":adm:bootJar" "--offline" "--no-daemon" "--console=plain" 2>&1
    $buildOutput | Set-Content -LiteralPath $buildLog -Encoding UTF8
    $result.process.buildBeforeRun.exitCode = $LASTEXITCODE
    if ($LASTEXITCODE -ne 0) {
        throw "ADM bootJar build before runtime smoke failed. Check log: $buildLog"
    }
}

$startedProcess = $null
$startedByScript = $false

try {
    if ($BuildBeforeRun) {
        Invoke-AdmBootJarBuild
    } else {
        $result.process.buildBeforeRun = [ordered]@{
            requested = $false
            reason = "Pass -BuildBeforeRun to build :adm:bootJar before starting ADM smoke."
        }
    }

    $initialHealth = Test-HealthReady
    if ($null -ne $initialHealth) {
        $result.process.status = "REUSED"
        $result.process.message = "Reused an already running ADM application."
    } else {
        if (Test-Path -LiteralPath $stdoutLog) { Remove-Item -LiteralPath $stdoutLog -Force }
        if (Test-Path -LiteralPath $stderrLog) { Remove-Item -LiteralPath $stderrLog -Force }

        $bootJar = Resolve-AdmBootJar
        if ($null -ne $bootJar) {
            $startedProcess = Start-Process `
                -FilePath "java.exe" `
                -ArgumentList @("-jar", $bootJar) `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdoutLog `
                -RedirectStandardError $stderrLog `
                -WindowStyle Hidden `
                -PassThru
            $result.process.launchMode = "BOOT_JAR"
            $result.process.bootJar = $bootJar
        } else {
            $gradle = Resolve-GradleExecutable
            if (-not (Test-Path -LiteralPath $gradle)) {
                throw "Gradle executable was not found: $gradle"
            }
            $startedProcess = Start-Process `
                -FilePath $gradle `
                -ArgumentList @("--offline", ":adm:bootRun") `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdoutLog `
                -RedirectStandardError $stderrLog `
                -WindowStyle Hidden `
                -PassThru
            $result.process.launchMode = "GRADLE_BOOT_RUN"
            $result.process.gradle = $gradle
        }
        $startedByScript = $true
        $result.process.status = "STARTED"
        $result.process.pid = $startedProcess.Id
        $result.process.stdoutLog = $stdoutLog
        $result.process.stderrLog = $stderrLog
    }

    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    $health = $null
    while ((Get-Date) -lt $deadline) {
        $health = Test-HealthReady
        if ($null -ne $health -and $health.status -eq "UP") {
            break
        }
        if ($startedByScript -and $startedProcess.HasExited) {
            throw "ADM application exited before health readiness. Check logs: $stdoutLog, $stderrLog"
        }
        Start-Sleep -Seconds 2
    }

    if ($null -eq $health) {
        throw "ADM health endpoint did not respond before timeout. Check logs: $stdoutLog, $stderrLog"
    }
    if ($health.status -ne "UP") {
        throw "ADM health status is not UP. status=$($health.status)"
    }
    $result.health.status = "PASSED"
    $result.health.response = $health

    & (Join-Path $Root "scripts/smoke-openapi.ps1") -AdmBaseUrl $AdmBaseUrl -SkipMbr -SkipXyz -SkipBza -SkipBat
    $result.openapi.status = "PASSED"
    $result.openapi.path = "$AdmBaseUrl/v3/api-docs"

    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
    }
    $login = Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    if ([string]::IsNullOrWhiteSpace($login.accessToken)) {
        throw "ADM login response does not contain accessToken."
    }
    $headers = @{ Authorization = "Bearer $($login.accessToken)" }
    $admOperationEndpoints = @(
        "/adm/api/operators",
        "/adm/api/operators/roles",
        "/adm/api/operators/menus",
        "/adm/api/permissions/roles",
        "/adm/api/permissions/menus",
        "/adm/api/permissions/buttons",
        "/adm/api/permissions/menu-matrix",
        "/adm/api/permissions/button-matrix",
        "/adm/api/permissions/api-permissions",
        "/adm/api/permissions/api-matrix",
        "/adm/api/audit-logs?limit=5",
        "/adm/api/cache/summary",
        "/adm/api/messages",
        "/adm/api/codes",
        "/adm/api/configs"
    )
    $checkedAdmOperationEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $admOperationEndpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedAdmOperationEndpoints.Add($endpoint)
    }
    $result.admOperationApi.status = "PASSED"
    $result.admOperationApi.checkedEndpoints = $checkedAdmOperationEndpoints

    if ($IncludePermissionWriteSmoke) {
        $permissionResult = Invoke-AdmPermissionWriteSmoke -AdminHeaders $headers -AdminPassword $AdmPassword
        $result.permissionWriteApi.status = $permissionResult.status
        $result.permissionWriteApi.resultPath = $PermissionResultPath
        $result.permissionWriteApi.checked = @(
            "/adm/api/operators",
            "/adm/api/permissions/roles/ADM_VIEWER/menus/PERMISSION",
            "/adm/api/permissions/roles/ADM_VIEWER/api-permissions/API_PERMISSION_WRITE_PUT",
            "/adm/api/audit-logs",
            "403 viewer write deny",
            "200 viewer center-cut read allow",
            "200 admin read allow"
        )
    } else {
        $result.permissionWriteApi.status = "SKIPPED"
        $result.permissionWriteApi.reason = "Run scripts/smoke-adm-permission-runtime.ps1 or pass -IncludePermissionWriteSmoke to execute write/filter checks."
    }

    $batchEndpoints = @(
        "/adm/api/batch/jobs",
        "/adm/api/batch/jobs/CPF_EDU_TASKLET_JOB",
        "/adm/api/batch/schedules",
        "/adm/api/batch/instances",
        "/adm/api/batch/executions?limit=20",
        "/adm/api/batch/steps?limit=20",
        "/adm/api/batch/workers",
        "/adm/api/batch/locks",
        "/adm/api/batch/ghost-candidates",
        "/adm/api/batch/operations?limit=20"
    )
    $checkedEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $batchEndpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedEndpoints.Add($endpoint)
    }
    $result.batchApi.status = "PASSED"
    $result.batchApi.checkedEndpoints = $checkedEndpoints

    $centerCutEndpoints = @(
        "/adm/api/center-cut/jobs",
        "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB",
        "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/parameters",
        "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/summary",
        "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/targets?limit=20",
        "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/results?limit=20"
    )
    $checkedCenterCutEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $centerCutEndpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedCenterCutEndpoints.Add($endpoint)
    }
    $centerCutSummary = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/summary" -Headers $headers
    $centerCutTargets = @(Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/targets?limit=20" -Headers $headers)
    $centerCutResults = @(Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/results?limit=20" -Headers $headers)
    $hasParentTransactionGlobalId = @($centerCutTargets | Where-Object { $_.parentTransactionGlobalId }).Count -gt 0
    $hasChildTransactionGlobalId = @($centerCutTargets | Where-Object { $_.childTransactionGlobalId }).Count -gt 0
    $hasFailureReason = @($centerCutTargets | Where-Object { $_.lastErrorMessage }).Count -gt 0
    $hasRawPayload = (($centerCutResults | ConvertTo-Json -Depth 20) -match "resultPayload`"")
    if ($hasRawPayload) {
        throw "Center-Cut resultPayload raw field must not be exposed."
    }
    $result.centerCutApi.status = "PASSED"
    $result.centerCutApi.checkedEndpoints = $checkedCenterCutEndpoints
    $result.centerCutApi.summary = $centerCutSummary
    $result.centerCutApi.targetCount = $centerCutTargets.Count
    $result.centerCutApi.resultCount = $centerCutResults.Count
    $result.centerCutApi.hasParentTransactionGlobalId = $hasParentTransactionGlobalId
    $result.centerCutApi.hasChildTransactionGlobalId = $hasChildTransactionGlobalId
    $result.centerCutApi.hasFailureReason = $hasFailureReason
    $result.centerCutApi.rawPayloadExposed = $false
    & (Join-Path $Root "scripts/smoke-adm-center-cut-runtime.ps1") `
        -AdmBaseUrl $AdmBaseUrl `
        -AdmUsername $AdmUsername `
        -AdmPassword $AdmPassword `
        -LogDir $LogDir
    $result.centerCutApi.dedicatedSmokeResultPath = Join-Path $LogDir "adm-center-cut-runtime-smoke-result.json"

    $transactionMetaResultPath = Join-Path $LogDir "transaction-meta-runtime-smoke-result.json"
    & (Join-Path $Root "scripts/smoke-transaction-meta-runtime.ps1") `
        -AdmBaseUrl $AdmBaseUrl `
        -AccessToken $login.accessToken `
        -ResultPath $transactionMetaResultPath
    $result.transactionMetaApi.status = "PASSED"
    $result.transactionMetaApi.endpoint = "/adm/api/transactions/scan"
    $result.transactionMetaApi.resultPath = $transactionMetaResultPath

    & (Join-Path $Root "scripts/smoke-adm-ui.ps1") -Root $Root
    $result.staticUi.status = "PASSED"

    $browserCommand = Test-BrowserAutomationAvailable
    if ($null -eq $browserCommand) {
        $result.browserClick.status = "SKIPPED"
        $result.browserClick.reason = "Browser automation runtime was not found in PATH."
        if ($RequireBrowserClick) {
            throw $result.browserClick.reason
        }
    } else {
        $result.browserClick.status = "SKIPPED"
        $result.browserClick.reason = "A browser command was detected, but no Playwright or Selenium click scenario exists in this repository."
        $result.browserClick.detectedCommand = $browserCommand
        if ($RequireBrowserClick) {
            throw $result.browserClick.reason
        }
    }
} catch {
    $result.error = $_.Exception.Message
    Save-SmokeResult
    throw
} finally {
    if ($startedByScript -and $null -ne $startedProcess) {
        Stop-AdmPortOwner
        Stop-ProcessTree -ProcessId $startedProcess.Id
        Wait-Process -Id $startedProcess.Id -Timeout $ShutdownTimeoutSeconds -ErrorAction SilentlyContinue
        $deadline = (Get-Date).AddSeconds($ShutdownTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            Start-Sleep -Seconds 1
            $healthAfterStop = Test-HealthReady
            if ($null -eq $healthAfterStop) {
                $result.cleanup.status = "PASSED"
                $result.cleanup.message = "The ADM process started by this script was stopped and the health endpoint no longer responds."
                break
            }
        }
        if (-not $result.cleanup.status) {
            if (-not (Get-Process -Id $startedProcess.Id -ErrorAction SilentlyContinue)) {
                $result.cleanup.status = "PASSED"
                $result.cleanup.message = "The ADM process started by this script no longer exists."
            } else {
                $result.cleanup.status = "WARNING"
                $result.cleanup.message = "ADM health endpoint still responds after stop request. Check whether another process is using the same port."
            }
        }
    } elseif (-not $result.cleanup.status) {
        $result.cleanup.status = "SKIPPED"
        $result.cleanup.message = "The script reused an existing ADM application and did not stop it."
    }
    Save-SmokeResult
}

Write-Host "ADM runtime smoke completed. Result: $resultPath"
