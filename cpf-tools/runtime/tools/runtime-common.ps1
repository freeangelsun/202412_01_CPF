

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

Set-StrictMode -Version Latest

$script:CpfRuntimeUtf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-CpfUnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

function Get-CpfRuntimeStatusText {
    param([string] $Name)

    switch ($Name) {
        "Done" { return (New-CpfUnicodeText @(0xC644, 0xB8CC)) }
        "Partial" { return (New-CpfUnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)) }
        "NotImplemented" { return (New-CpfUnicodeText @(0xBBF8, 0xAD6C, 0xD604)) }
        "NotVerified" { return (New-CpfUnicodeText @(0xBBF8, 0xAC80, 0xC99D)) }
        "Failed" { return (New-CpfUnicodeText @(0xC2E4, 0xD328)) }
        default { return (New-CpfUnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)) }
    }
}

function Get-CpfRuntimeRoot {
    param([string] $Root)

    if ([string]::IsNullOrWhiteSpace($Root)) {
        return (Resolve-Path "$PSScriptRoot\..\..\..").Path
    }
    return (Resolve-Path -LiteralPath $Root).Path
}

function Get-CpfRuntimeResultDir {
    param(
        [string] $Root,
        [string] $ResultDir
    )

    if ([string]::IsNullOrWhiteSpace($ResultDir)) {
        return Join-Path $Root "build/runtime-smoke"
    }
    return $ResultDir
}

function Get-CpfRuntimePlatformModuleMap {
    return @(
        [ordered]@{
            module = "ADM"
            moduleLower = "adm"
            projectName = "cpf-admin"
            instanceId = $null
            port = 8090
            portEnv = "ADM_SERVER_PORT"
            healthPath = "/adm/api/health/readiness"
            jarDir = "cpf-admin/build/libs"
            jarPattern = "admin-*.war"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "BAT"
            moduleLower = "bat"
            projectName = "cpf-batch:control-plane"
            instanceId = $null
            port = 8180
            portEnv = "CPF_PORT"
            healthPath = "/actuator/health/readiness"
            jarDir = "cpf-batch/control-plane/build/libs"
            jarPattern = "cpf-batch-control-plane-*.jar"
            openApi = $false
            generatedDomain = $false
        },
        [ordered]@{
            module = "MBW"
            moduleLower = "mbw"
            projectName = "cpf-backoffice"
            instanceId = $null
            port = 8091
            portEnv = "MBW_ONLINE_PORT"
            healthPath = "/actuator/health/readiness"
            jarDir = "cpf-backoffice/online/build/libs"
            jarPattern = "cpf-backoffice-online-*.jar"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "EDU"
            moduleLower = "edu"
            projectName = "cpf-education"
            instanceId = $null
            port = 8099
            portEnv = "EDU_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "cpf-education/build/libs"
            jarPattern = "education-*.jar"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "GWY"
            moduleLower = "gwy"
            projectName = "cpf-gateway"
            instanceId = $null
            port = 8070
            portEnv = "GWY_SERVER_PORT"
            healthPath = "/actuator/health"
            jarDir = "cpf-gateway/build/libs"
            jarPattern = "gateway-*.jar"
            openApi = $true
            generatedDomain = $false
        }
    )
}

function Get-CpfGeneratedRuntimeModuleMap {
    param([string] $Root = "")

    $resolvedRoot = Get-CpfRuntimeRoot -Root $Root
    . (Join-Path $resolvedRoot "cpf-tools/generator/tools/generated-domain-common.ps1")
    $generated = @()
    foreach ($domain in @(Get-CpfGeneratedDomainInventory -Root $resolvedRoot)) {
        if (-not [bool]$domain.exists -or -not [bool]$domain.onlineEnabled) { continue }
        # Prebuilt Domain(cpf-backoffice/MBW)은 Get-CpfRuntimePlatformModuleMap의 static entry가 이미
        # 소유한다. 동일 cpf.domain.* 계약을 공유한다는 이유로 여기서 다시 추가하면 identity가 중복된다.
        if ([string]$domain.generationMode -eq 'prebuilt') { continue }
        if (@($domain.forbiddenPermanentMetadata).Count -gt 0) {
            throw "Generated Domain에 영구 lifecycle metadata가 남아 있습니다: $($domain.projectName)"
        }
        $port = [int]$domain.localOnlinePort
        if ($port -lt 1 -or $port -gt 65535) {
            throw "Generated Domain local online port가 유효하지 않습니다: project=$($domain.projectName) port=$port"
        }
        $moduleLower = ([string]$domain.systemCode).ToLowerInvariant()
        $projectName = [string]$domain.projectName
        $generated += [ordered]@{
            module = [string]$domain.systemCode
            moduleLower = $moduleLower
            projectName = $projectName
            instanceId = $null
            port = $port
            portEnv = "$([string]$domain.systemCode)_ONLINE_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "$projectName/online/build/libs"
            jarPattern = "$projectName-online-*.jar"
            openApi = $true
            generatedDomain = $true
            domainName = [string]$domain.domainName
            moduleName = [string]$domain.moduleName
            databaseEnabled = [bool]$domain.databaseEnabled
            databaseRole = [string]$domain.databaseRole
            contractPath = [string]$domain.contractPath
        }
    }
    return @($generated)
}

function Get-CpfRuntimeModuleMap {
    param([string] $Root = "")

    $map = @(
        @(Get-CpfRuntimePlatformModuleMap)
        @(Get-CpfGeneratedRuntimeModuleMap -Root $Root)
    )
    foreach ($propertyName in @("module", "projectName", "port")) {
        $duplicates = @($map |
                Group-Object -Property { $_[$propertyName] } |
                Where-Object { $_.Count -gt 1 } |
                ForEach-Object { [string] $_.Name })
        if ($duplicates.Count -gt 0) {
            throw "Runtime module identity가 중복되었습니다. field=$propertyName values=$($duplicates -join ',')"
        }
    }
    return @($map)
}

function Resolve-CpfRuntimeModules {
    param(
        [string[]] $Modules,
        [string] $Root = ""
    )

    $map = Get-CpfRuntimeModuleMap -Root $Root
    if ($null -eq $Modules -or $Modules.Count -eq 0) {
        return $map
    }

    $selected = @()
    $expandedModules = @()
    foreach ($moduleName in $Modules) {
        if ([string]::IsNullOrWhiteSpace($moduleName)) {
            continue
        }
        $expandedModules += @($moduleName -split "," | ForEach-Object { $_.Trim() } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    }

    foreach ($moduleName in $expandedModules) {
        if ([string]::IsNullOrWhiteSpace($moduleName)) {
            continue
        }
        $normalized = $moduleName.Trim().ToUpperInvariant()
        $module = $map | Where-Object { $_.module -eq $normalized } | Select-Object -First 1
        if ($null -eq $module) {
            throw "Unsupported runtime module. module=$moduleName"
        }
        $selected += $module
    }
    return @($selected)
}

function Get-CpfRelativePath {
    param(
        [string] $Root,
        [string] $Path
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $Path
    }
    $resolvedRoot = (Resolve-Path -LiteralPath $Root).Path
    $fullPath = $Path
    if (Test-Path -LiteralPath $Path) {
        $fullPath = (Resolve-Path -LiteralPath $Path).Path
    }
    if ($fullPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $fullPath.Substring($resolvedRoot.Length).TrimStart("\", "/")
    }
    return $Path
}

function Write-CpfRuntimeJson {
    param(
        [string] $Path,
        [object] $Value
    )

    $parent = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [System.IO.File]::WriteAllText($Path, ($Value | ConvertTo-Json -Depth 80), $script:CpfRuntimeUtf8NoBom)
}

function New-CpfRuntimeClientHeaders {
    param(
        [string] $ClientId = "cpf-runtime-smoke",
        [string] $ClientVersion = "1.0.0",
        [string] $RequestType = "SMOKE"
    )

    return @{
        "X-Client-Id" = $ClientId
        "X-Client-Version" = $ClientVersion
        "X-Request-Type" = $RequestType
    }
}

function Test-CpfRuntimeTcpPort {
    param(
        [int] $Port,
        [int] $TimeoutMilliseconds = 700
    )

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync("127.0.0.1", $Port)
        return ($task.Wait($TimeoutMilliseconds) -and $client.Connected)
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Invoke-CpfRuntimeHttpProbe {
    param(
        [int] $Port,
        [string] $Path,
        [int] $TimeoutSeconds = 3
    )

    $uri = "http://127.0.0.1:$Port$Path"
    try {
        $response = Invoke-WebRequest -Uri $uri -Method Get -TimeoutSec $TimeoutSeconds -UseBasicParsing
        return [ordered]@{
            uri = $uri
            statusCode = [int] $response.StatusCode
            success = $true
        }
    } catch {
        $statusCode = $null
        if ($_.Exception.Response -ne $null) {
            try {
                $statusCode = [int] $_.Exception.Response.StatusCode
            } catch {
                $statusCode = $null
            }
        }
        return [ordered]@{
            uri = $uri
            statusCode = $statusCode
            success = $false
            error = $_.Exception.GetType().Name
            message = $_.Exception.Message
        }
    }
}

function Get-CpfRuntimeTail {
    param(
        [string] $Path,
        [int] $LineCount = 80
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return @()
    }
    try {
        return @(Get-Content -LiteralPath $Path -Encoding UTF8 -Tail $LineCount -ErrorAction Stop)
    } catch {
        return @("log tail read failed: $($_.Exception.Message)")
    }
}

function Get-CpfRuntimeTailPreview {
    param(
        [string] $Path,
        [int] $LineCount = 40,
        [int] $MaxLineLength = 1000
    )

    return @(Get-CpfRuntimeTail -Path $Path -LineCount $LineCount | ForEach-Object {
            $line = [string] $_
            if ($line.Length -gt $MaxLineLength) {
                $line.Substring(0, $MaxLineLength) + "...truncated"
            } else {
                $line
            }
        })
}

function Find-CpfRuntimeBootJar {
    param(
        [string] $Root,
        [object] $Module
    )

    $jarDir = Join-Path $Root $Module.jarDir
    if (-not (Test-Path -LiteralPath $jarDir)) {
        return $null
    }
    $jars = @(Get-ChildItem -LiteralPath $jarDir -File -Filter $Module.jarPattern |
        Where-Object { $_.Name -notmatch '(?i)-plain\.jar$' } |
        Sort-Object LastWriteTime -Descending)
    if ($jars.Count -eq 0) {
        return $null
    }
    return $jars[0].FullName
}

function Read-CpfRuntimeState {
    param(
        [string] $ResultDir
    )

    $statePath = Join-Path $ResultDir "runtime-services.json"
    if (-not (Test-Path -LiteralPath $statePath)) {
        return $null
    }
    try {
        return Get-Content -LiteralPath $statePath -Encoding UTF8 -Raw | ConvertFrom-Json
    } catch {
        return $null
    }
}

function Get-CpfRuntimePidInfo {
    param(
        [string] $ResultDir,
        [object] $Module
    )

    $pidPath = Join-Path $ResultDir ("runtime-" + $Module.moduleLower + ".pid")
    $pidValue = $null
    if (Test-Path -LiteralPath $pidPath) {
        $raw = (Get-Content -LiteralPath $pidPath -Encoding UTF8 -Raw).Trim()
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            $pidValue = [int] $raw
        }
    }

    $processAlive = $false
    $processName = $null
    if ($pidValue -ne $null) {
        $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
        if ($null -ne $process) {
            $processAlive = $true
            $processName = $process.ProcessName
        }
    }

    return [ordered]@{
        pid = $pidValue
        processAlive = $processAlive
        processName = $processName
        pidFile = $pidPath
    }
}

function Get-CpfRuntimeLogFiles {
    param(
        [string] $Root,
        [object] $Module,
        [switch] $IncludeTail
    )

    $environmentCode = if ([string]::IsNullOrWhiteSpace($env:CPF_ENV)) { "local" } else { $env:CPF_ENV.Trim().ToLowerInvariant() }
    $instanceId = if (-not [string]::IsNullOrWhiteSpace($env:CPF_RUNTIME_INSTANCE_ID)) {
        $env:CPF_RUNTIME_INSTANCE_ID.Trim()
    } else {
        $hostIdentity = [System.Net.Dns]::GetHostName()
        if ([string]::IsNullOrWhiteSpace($hostIdentity) -or $hostIdentity.Trim().ToLowerInvariant() -in @('local','localhost','unknown','127.0.0.1','::1')) {
            throw 'CPF Runtime instanceId를 확정할 수 없습니다. CPF_RUNTIME_INSTANCE_ID 또는 실제 Runtime hostname이 필요합니다.'
        }
        $hostIdentity.Trim()
    }
    $logDir = Join-Path $Root ("logs/{0}/{1}/{2}" -f $environmentCode, $Module.moduleLower, $instanceId)
    if (-not (Test-Path -LiteralPath $logDir)) {
        return @()
    }
    return @(Get-ChildItem -LiteralPath $logDir -File -Recurse -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 8 |
        ForEach-Object {
            $tailPreview = @()
            if ($IncludeTail -and $_.Length -le 5242880) {
                $tailPreview = Get-CpfRuntimeTailPreview -Path $_.FullName -LineCount 10
            } elseif ($IncludeTail) {
                $tailPreview = @("tail skipped because log file is larger than 5MB")
            }
            [ordered]@{
                path = Get-CpfRelativePath -Root $Root -Path $_.FullName
                bytes = $_.Length
                lastWriteTime = $_.LastWriteTime.ToString("o")
                tail = [string[]] $tailPreview
            }
        })
}

function Get-CpfRuntimeFailureClassification {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) {
        return "needs-review"
    }
    if ($Message -match "(?i)401|403|unauthorized|forbidden|permission|denied") {
        return "permission"
    }
    if ($Message -match "(?i)SQL|JDBC|Hikari|MariaDB|database|Connection refused") {
        return "environment"
    }
    if ($Message -match "(?i)BeanCreation|NoSuchBean|ClassNotFound|NoSuchMethod|NullPointer|compile") {
        return "implementation"
    }
    if ($Message -match "(?i)scope|excluded|not implemented") {
        return "request-scope"
    }
    return "environment"
}
