

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
        return (Resolve-Path "$PSScriptRoot\..\..").Path
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
            wasId = "admAP01"
            port = 8090
            portEnv = "ADM_SERVER_PORT"
            healthPath = "/adm/api/health/readiness"
            jarDir = "cpf-admin/build/libs"
            jarPattern = "cpf-admin-*.jar"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "BAT"
            moduleLower = "bat"
            projectName = "cpf-batch:control-server"
            wasId = "batCT01"
            port = 8180
            portEnv = "CPF_PORT"
            healthPath = "/actuator/health/readiness"
            jarDir = "cpf-batch/control-server/build/libs"
            jarPattern = "cpf-batch-control-server-*.jar"
            openApi = $false
            generatedDomain = $false
        },
        [ordered]@{
            module = "BZA"
            moduleLower = "bza"
            projectName = "cpf-biz-admin"
            wasId = "bzaAP01"
            port = 8091
            portEnv = "BZA_SERVER_PORT"
            healthPath = "/actuator/health/readiness"
            jarDir = "cpf-biz-admin/build/libs"
            jarPattern = "cpf-biz-admin-*.jar"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "REF"
            moduleLower = "ref"
            projectName = "cpf-reference"
            wasId = "refAP01"
            port = 8099
            portEnv = "REF_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "cpf-reference/build/libs"
            jarPattern = "cpf-reference-*.jar"
            openApi = $true
            generatedDomain = $false
        },
        [ordered]@{
            module = "GWY"
            moduleLower = "gwy"
            projectName = "cpf-gateway"
            wasId = "gwLC001"
            port = 8070
            portEnv = "GWY_SERVER_PORT"
            healthPath = "/actuator/health"
            jarDir = "cpf-gateway/build/libs"
            jarPattern = "cpf-gateway-*.jar"
            openApi = $true
            generatedDomain = $false
        }
    )
}

function Get-CpfGeneratedRuntimeModuleMap {
    param([string] $Root = "")

    $resolvedRoot = Get-CpfRuntimeRoot -Root $Root
    $settingsPath = Join-Path $resolvedRoot "settings.gradle"
    if (-not (Test-Path -LiteralPath $settingsPath -PathType Leaf)) {
        throw "CPF root settings.gradle을 찾을 수 없습니다: $settingsPath"
    }
    $settingsText = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
    $generated = @()

    foreach ($projectDir in @(Get-ChildItem -LiteralPath $resolvedRoot -Directory |
            Where-Object { $_.Name -like "cpf-*" } |
            Sort-Object Name)) {
        $manifestPath = Join-Path $projectDir.FullName "manifest/domain-manifest.json"
        if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
            continue
        }
        $ownershipPath = Join-Path $projectDir.FullName "manifest/generator-ownership.json"
        if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
            throw "Generated Domain ownership manifest가 없습니다: $($projectDir.Name)"
        }

        try {
            $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
            $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
        } catch {
            throw "Generated Domain manifest JSON을 읽을 수 없습니다: project=$($projectDir.Name) error=$($_.Exception.Message)"
        }

        $projectName = [string] $manifest.projectName
        $systemCode = ([string] $manifest.systemCode).Trim().ToUpperInvariant()
        $domainName = ([string] $manifest.domainName).Trim().ToLowerInvariant()
        $moduleName = [string] $manifest.moduleName
        $dependencyModel = [string] $manifest.dependencyModel
        if ([string] $manifest.domainType -cne "GENERATED_DOMAIN" -or
                $projectName -cne $projectDir.Name -or
                $projectName -notmatch '^cpf-[a-z][a-z0-9-]{1,40}$' -or
                $systemCode -notmatch '^[A-Z][A-Z0-9]{2}$' -or
                $domainName -notmatch '^[a-z][a-z0-9]{1,29}$' -or
                [string]::IsNullOrWhiteSpace($moduleName) -or
                $dependencyModel -cne "root-project") {
            throw "Generated Domain runtime identity가 유효하지 않습니다: project=$($projectDir.Name)"
        }
        foreach ($identityCheck in @(
                [ordered]@{ name = "projectName"; actual = [string] $ownership.projectName; expected = $projectName },
                [ordered]@{ name = "moduleDirectory"; actual = [string] $ownership.moduleDirectory; expected = $projectName },
                [ordered]@{ name = "systemCode"; actual = ([string] $ownership.systemCode).ToUpperInvariant(); expected = $systemCode },
                [ordered]@{ name = "domainName"; actual = ([string] $ownership.domainName).ToLowerInvariant(); expected = $domainName })) {
            if ($identityCheck.actual -cne $identityCheck.expected) {
                throw (
                    "Generated Domain manifest/ownership identity가 다릅니다. " +
                    "project=$projectName field=$($identityCheck.name)"
                )
            }
        }
        if ($settingsText.IndexOf("include '$projectName'", [StringComparison]::Ordinal) -lt 0 -or
                $settingsText.IndexOf("project(':$projectName').projectDir", [StringComparison]::Ordinal) -lt 0) {
            throw "Generated Domain이 settings.gradle에 root project로 등록되지 않았습니다: $projectName"
        }

        $onlineEnabled = [bool] $manifest.onlineEnabled
        if ($null -ne $manifest.capabilities -and
                $null -ne $manifest.capabilities.PSObject.Properties["online"]) {
            $onlineEnabled = [bool] $manifest.capabilities.online
        }
        if (-not $onlineEnabled) {
            continue
        }
        $databaseEnabled = [bool] $manifest.databaseEnabled
        if ($null -ne $manifest.capabilities -and
                $null -ne $manifest.capabilities.PSObject.Properties["database"]) {
            $databaseEnabled = [bool] $manifest.capabilities.database
        }

        $port = [int] $manifest.port
        if ($port -lt 1 -or $port -gt 65535) {
            throw "Generated Domain runtime port가 유효하지 않습니다: project=$projectName port=$port"
        }
        $moduleLower = $systemCode.ToLowerInvariant()
        $generated += [ordered]@{
            module = $systemCode
            moduleLower = $moduleLower
            projectName = $projectName
            wasId = $moduleLower + "AP01"
            port = $port
            portEnv = "${systemCode}_SERVER_PORT"
            healthPath = "/v3/api-docs"
            jarDir = "$projectName/build/libs"
            jarPattern = "$projectName-*.jar"
            openApi = $true
            generatedDomain = $true
            domainName = $domainName
            moduleName = $moduleName
            databaseEnabled = $databaseEnabled
            databaseProfilePath = [string] $manifest.databaseProfilePath
            manifestPath = Get-CpfRelativePath -Root $resolvedRoot -Path $manifestPath
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

function New-CpfRuntimeTransactionHeaders {
    param(
        [string] $Module = "CPF",
        [string] $WasId = "smoke01",
        [string] $RequestType = "SMOKE",
        [string] $ClientAppId = "cpf-runtime-smoke",
        [string] $ClientVersion = "1.0.0",
        [string] $UserId = "runtime-smoke",
        [string] $ChannelCode = ""
    )

    $normalizedModule = if ([string]::IsNullOrWhiteSpace($Module)) { "CPF" } else { $Module.Trim().ToUpperInvariant() }
    $normalizedWasId = if ([string]::IsNullOrWhiteSpace($WasId)) { "smoke01" } else { $WasId.Trim() }
    if ($normalizedWasId.Length -lt 7) {
        $normalizedWasId = $normalizedWasId.PadRight(7, "0")
    } elseif ($normalizedWasId.Length -gt 7) {
        $normalizedWasId = $normalizedWasId.Substring(0, 7)
    }

    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $transactionId = "$timestamp$normalizedModule$normalizedWasId" + "0000001"
    $resolvedChannel = if ([string]::IsNullOrWhiteSpace($ChannelCode)) { $normalizedModule } else { $ChannelCode.Trim().ToUpperInvariant() }

    return @{
        "X-Transaction-Id" = $transactionId
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = $RequestType
        "X-Original-Channel-Code" = $resolvedChannel
        "X-Channel-Code" = $resolvedChannel
        "X-Client-App-Id" = $ClientAppId
        "X-Client-Version" = $ClientVersion
        "X-User-Id" = $UserId
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
    $instanceId = if ([string]::IsNullOrWhiteSpace($env:CPF_INSTANCE_ID)) {
        $Module.moduleLower + "-" + $environmentCode + "-01"
    } else {
        $env:CPF_INSTANCE_ID.Trim()
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
