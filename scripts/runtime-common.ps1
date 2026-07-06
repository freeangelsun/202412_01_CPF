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
        return (Resolve-Path "$PSScriptRoot\..").Path
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

function Get-CpfRuntimeModuleMap {
    return @(
        [ordered]@{
            module = "ACC"
            moduleLower = "acc"
            port = 8080
            portEnv = "ACC_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "acc/build/libs"
            jarPattern = "acc-*.jar"
            logDir = "logs/acc"
        },
        [ordered]@{
            module = "MBR"
            moduleLower = "mbr"
            port = 8081
            portEnv = "MBR_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "mbr/build/libs"
            jarPattern = "mbr-*.jar"
            logDir = "logs/mbr"
        },
        [ordered]@{
            module = "ADM"
            moduleLower = "adm"
            port = 8090
            portEnv = "ADM_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "adm/build/libs"
            jarPattern = "adm-*.jar"
            logDir = "logs/adm"
        },
        [ordered]@{
            module = "EXS"
            moduleLower = "exs"
            port = 8092
            portEnv = "EXS_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "exs/build/libs"
            jarPattern = "exs-*.jar"
            logDir = "logs/exs"
        },
        [ordered]@{
            module = "BAT"
            moduleLower = "bat"
            port = 8093
            portEnv = "BAT_SERVER_PORT"
            healthPath = "/bat/api/health"
            jarDir = "bat/build/libs"
            jarPattern = "bat-*.jar"
            logDir = "logs/bat"
        }
    )
}

function Resolve-CpfRuntimeModules {
    param(
        [string[]] $Modules
    )

    $map = Get-CpfRuntimeModuleMap
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

    $logDir = Join-Path $Root $Module.logDir
    if (-not (Test-Path -LiteralPath $logDir)) {
        return @()
    }
    return @(Get-ChildItem -LiteralPath $logDir -File -ErrorAction SilentlyContinue |
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
