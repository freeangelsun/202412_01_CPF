param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ProfilePath = "",
    [string] $ResultDir = "",
    [switch] $All,
    [string[]] $DomainName = @(),
    [string[]] $SystemCode = @(),
    [string[]] $ModuleName = @(),
    [ValidateSet("profile", "product", "none", "all")]
    [string] $SeedMode = "profile",
    [switch] $ProvisionOnly,
    [switch] $RequireRun
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF DB 초기화는 pwsh 7 이상이 필요합니다."
}
if ($ProvisionOnly -and -not $RequireRun) {
    throw "-ProvisionOnly는 실제 Service User/Grant를 반영하므로 -RequireRun이 필요합니다."
}

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

. (Join-Path $PSScriptRoot "database-profile-common.ps1")

if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"
} elseif (-not [System.IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
$ProfilePath = [System.IO.Path]::GetFullPath($ProfilePath)
$profile = Get-CpfDatabaseProfile $ProfilePath

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/db-install"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$profileModuleProperties = @($profile.modules.PSObject.Properties)
if ($profileModuleProperties.Count -eq 0) {
    throw "DB Profile modules가 비어 있습니다."
}
# 설치 순서는 Tool source의 고정 Domain 목록이 아니라 Profile의 선언 순서를 사용합니다.
$moduleOrder = @($profileModuleProperties | ForEach-Object { [string]$_.Name })
$moduleProfiles = @{}
foreach ($key in $moduleOrder) {
    $moduleProfiles[$key] = ConvertTo-CpfModuleProfile $profile $key
}

$platformKeys = @($moduleOrder | Where-Object {
        $moduleProfiles[$_].databaseLifecycle -eq "platform-pack"
    })
$generatedProfileKeys = @($moduleOrder | Where-Object {
        $moduleProfiles[$_].databaseLifecycle -eq "generated-domain"
    })
$enabledKeys = @($platformKeys | Where-Object {
        $m = $moduleProfiles[$_]
        if (-not $m.enabled) { return $false }
        if (-not $m.sourceOptional) { return $true }
        if ([string]::IsNullOrWhiteSpace($m.ownerPath)) {
            throw "sourceOptional Module은 ownerPath가 필요합니다: $_"
        }
        $owner = Join-Path $Root $m.ownerPath
        if (-not (Test-Path -LiteralPath $owner -PathType Container)) {
            Write-Host "CPF DB optional module skipped because source is ABSENT: $_ owner=$($m.ownerPath)"
            return $false
        }
        return $true
    })
foreach ($requiredKey in @($platformKeys | Where-Object { $moduleProfiles[$_].required })) {
    if (-not $moduleProfiles[$requiredKey].enabled) {
        throw "필수 Module DB를 disabled로 설정할 수 없습니다: $requiredKey"
    }
}
if ($enabledKeys.Count -eq 0) { throw "설치할 Module DB가 하나도 없습니다." }
$coreCandidates = @($platformKeys | Where-Object {
        $moduleProfiles[$_].required -and $moduleProfiles[$_].systemCode -eq "CPF"
    })
if ($coreCandidates.Count -ne 1) {
    throw "Profile에는 required=true/systemCode=CPF인 Core DB가 정확히 하나 있어야 합니다."
}
$coreKey = $coreCandidates[0]

$hasSelector = $All -or $DomainName.Count -gt 0 -or $SystemCode.Count -gt 0 -or $ModuleName.Count -gt 0
if ($All -and ($DomainName.Count -gt 0 -or $SystemCode.Count -gt 0 -or $ModuleName.Count -gt 0)) {
    throw "-All과 DomainName/SystemCode/ModuleName 선택자는 동시에 사용할 수 없습니다."
}

if (-not $hasSelector -or $All) {
    $selectedKeys = @($enabledKeys)
} else {
    $selected = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

    foreach ($name in $DomainName) {
        $matched = @($platformKeys | Where-Object {
            $moduleProfiles[$_].domainName -eq $name
        })
        if ($matched.Count -eq 0) {
            $generatedMatch = @($generatedProfileKeys | Where-Object {
                    $moduleProfiles[$_].domainName -eq $name
                })
            if ($generatedMatch.Count -gt 0) {
                throw "Generated Domain은 Platform Pack으로 설치하지 않습니다. manifest 기반 initialize-generated-domain-databases.ps1를 사용하세요: $name"
            }
            throw "알 수 없는 Platform DomainName입니다: $name"
        }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    foreach ($code in $SystemCode) {
        $normalized = $code.Trim().ToUpperInvariant()
        $matched = @($platformKeys | Where-Object {
            $moduleProfiles[$_].systemCode -eq $normalized
        })
        if ($matched.Count -eq 0) {
            $generatedMatch = @($generatedProfileKeys | Where-Object {
                    $moduleProfiles[$_].systemCode -eq $normalized
                })
            if ($generatedMatch.Count -gt 0) {
                throw "Generated Domain은 Platform Pack으로 설치하지 않습니다. manifest 기반 initialize-generated-domain-databases.ps1를 사용하세요: $code"
            }
            throw "알 수 없는 Platform SystemCode입니다: $code"
        }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    foreach ($name in $ModuleName) {
        $matched = @($platformKeys | Where-Object {
            $moduleProfiles[$_].moduleName -eq $name
        })
        if ($matched.Count -eq 0) {
            $generatedMatch = @($generatedProfileKeys | Where-Object {
                    $moduleProfiles[$_].moduleName -eq $name
                })
            if ($generatedMatch.Count -gt 0) {
                throw "Generated Domain은 Platform Pack으로 설치하지 않습니다. manifest 기반 initialize-generated-domain-databases.ps1를 사용하세요: $name"
            }
            throw "알 수 없는 Platform ModuleName입니다: $name"
        }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    $selectedKeys = @($platformKeys | Where-Object { $selected.Contains($_) })
}

$disabledSelected = @($selectedKeys | Where-Object { -not $moduleProfiles[$_].enabled })
if ($disabledSelected.Count -gt 0) {
    throw "Profile에서 disabled인 Module은 설치할 수 없습니다: $($disabledSelected -join ', ')"
}
if ($selectedKeys.Count -eq 0) { throw "선택된 Module DB가 하나도 없습니다." }
$fullPlatformSelection = $selectedKeys.Count -eq $enabledKeys.Count -and
    @($enabledKeys | Where-Object { $_ -notin $selectedKeys }).Count -eq 0 -and
    $generatedProfileKeys.Count -eq 0

Write-Host "CPF DB selected modules: $($selectedKeys -join ', ')"

# 한 번의 플랫폼 설치는 단일 DB Vendor를 사용합니다. 서로 다른 Vendor를 한 Platform Pack에 혼합하면
# migration/backup/rollback/운영 Runbook의 원자성이 깨지므로 명시적으로 금지합니다.
$selectedVendors = @($selectedKeys | ForEach-Object { $moduleProfiles[$_].vendor } | Sort-Object -Unique)
if ($selectedVendors.Count -ne 1) {
    throw "한 번의 CPF Platform DB 설치에는 하나의 Vendor만 사용할 수 있습니다: $($selectedVendors -join ', ')"
}
$selectedVendor = $selectedVendors[0]
if ($selectedVendor -in @('postgresql','oracle')) {
    $runner = Join-Path $PSScriptRoot 'invoke-official-db-vendor-sql.ps1'
    if (-not (Test-Path -LiteralPath $runner -PathType Leaf)) { throw "Official DB vendor runner가 없습니다: $runner" }
    $manifest = Get-Content (Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
    $entry = $manifest.vendors.$selectedVendor
    if ($null -eq $entry -or $selectedVendor -notin @($manifest.supportedVendors)) { throw "공식 Vendor Pack이 readiness 상태가 아닙니다: $selectedVendor" }
    foreach ($k in @('provision','emptyInstall','productSeed','optionalSampleSeed','testSeed','verify')) {
        $f = Join-Path $Root ([string]$entry.lifecycle.$k)
        if (-not (Test-Path -LiteralPath $f -PathType Leaf)) { throw "Vendor lifecycle artifact가 없습니다: vendor=$selectedVendor key=$k path=$f" }
    }
    if ($RequireRun) {
        $moduleArgs = @($selectedKeys)
        & $runner -Vendor $selectedVendor -Mode provision -ProfilePath $ProfilePath -Modules $moduleArgs
        if ($LASTEXITCODE -ne 0) { throw "$selectedVendor provision 실패" }
        if (-not $ProvisionOnly) {
            & $runner -Vendor $selectedVendor -Mode install -ProfilePath $ProfilePath -Modules $moduleArgs
            if ($LASTEXITCODE -ne 0) { throw "$selectedVendor install 실패" }
            $effectiveSeedMode = $SeedMode
            if ($effectiveSeedMode -eq 'profile') { $effectiveSeedMode = 'product' }
            if ($effectiveSeedMode -in @('product','all')) { & $runner -Vendor $selectedVendor -Mode productSeed -ProfilePath $ProfilePath -Modules $moduleArgs; if ($LASTEXITCODE -ne 0) { throw "$selectedVendor productSeed 실패" } }
            if ($effectiveSeedMode -eq 'all') {
                foreach ($mode in @('optionalSampleSeed','testSeed')) { & $runner -Vendor $selectedVendor -Mode $mode -ProfilePath $ProfilePath -Modules $moduleArgs; if ($LASTEXITCODE -ne 0) { throw "$selectedVendor $mode 실패" } }
            }
            & $runner -Vendor $selectedVendor -Mode verify -ProfilePath $ProfilePath -Modules $moduleArgs
            if ($LASTEXITCODE -ne 0) { throw "$selectedVendor verify 실패" }
        }
    }
    $summary = [ordered]@{ baselineCommit = ''; vendor = $selectedVendor; modules = $selectedKeys; operationMode = if ($ProvisionOnly) { 'provision-only' } else { 'install' }; requireRun = [bool]$RequireRun; status = if ($RequireRun) { '완료' } else { '미검증' }; profile = [IO.Path]::GetFileName($ProfilePath); generatedAt = (Get-Date).ToString('o') }
    $summary | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $ResultDir 'database-profile-install-result.sanitized.json') -Encoding UTF8
    if (-not $RequireRun) { Write-Host "CPF $selectedVendor lifecycle static validation PASS. 실제 DB 실행은 -RequireRun에서 수행합니다." }
    return
}

$logicalToKey = @{}
foreach ($profileKey in $moduleOrder) {
    $target = $moduleProfiles[$profileKey]
    $logicalDatabase = [string]$target.logicalDatabase
    if ([string]::IsNullOrWhiteSpace($logicalDatabase)) {
        throw "Profile logicalDatabase가 비어 있습니다: module=$profileKey"
    }
    if (-not $logicalToKey.ContainsKey($logicalDatabase)) {
        $logicalToKey[$logicalDatabase] = $profileKey
        continue
    }

    # Multiple CPF owners may intentionally share one physical platform database (for example core/common/admin).
    # This is valid only when the profile explicitly declares sharesDatabaseWith and the physical DB contract is exact.
    $ownerKey = [string]$logicalToKey[$logicalDatabase]
    $owner = $moduleProfiles[$ownerKey]
    $declaredOwner = [string]$target.sharesDatabaseWith
    if ([string]::IsNullOrWhiteSpace($declaredOwner)) {
        throw "Profile logicalDatabase가 중복되었지만 sharesDatabaseWith가 없습니다: module=$profileKey logicalDatabase=$logicalDatabase owner=$ownerKey"
    }
    if ($declaredOwner -ne $ownerKey) {
        throw "sharesDatabaseWith가 logicalDatabase 정본 Owner와 다릅니다: module=$profileKey sharesDatabaseWith=$declaredOwner owner=$ownerKey logicalDatabase=$logicalDatabase"
    }
    foreach ($property in @('vendor','host','port','databaseName','schemaName','migrationUsername','runtimeUsername')) {
        if ([string]$target.$property -ne [string]$owner.$property) {
            throw "공유 DB Profile 물리 계약이 다릅니다: module=$profileKey owner=$ownerKey property=$property"
        }
    }
}

$installFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/install/00_empty_install.sql"
$productSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql"
$optionalSampleSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_optional_sample_seed.sql"
$testSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_test_seed.sql"
$verifyFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/verify/00_verify.sql"
$schemaManifestFile = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
foreach ($requiredFile in @(
    $installFile,
    $productSeedFile,
    $optionalSampleSeedFile,
    $testSeedFile,
    $verifyFile,
    $schemaManifestFile
)) {
    if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "MariaDB Vendor Pack 파일이 없습니다: $requiredFile"
    }
}

function Find-MariaClient {
    param($Target)
    if (-not [string]::IsNullOrWhiteSpace($Target.clientPath) -and
        (Test-Path -LiteralPath $Target.clientPath -PathType Leaf)) {
        return (Resolve-Path -LiteralPath $Target.clientPath).Path
    }
    foreach ($name in @("mariadb")) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $cmd) { return $cmd.Source }
    }
    throw "MariaDB CLI를 찾을 수 없습니다. module=$($Target.moduleKey)"
}

function Protect-CpfSecretText {
    param(
        [string] $Text,
        [string[]] $Secrets
    )
    if ($null -eq $Text) { return "" }
    $safe = $Text
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $safe = $safe.Replace($secret, "****")
        }
    }
    return $safe
}

function New-MariaProcessStartInfo {
    param(
        $Target,
        [string] $Username,
        [string] $Password,
        [string] $Database = "",
        [switch] $RedirectInput
    )

    $client = Find-MariaClient $Target
    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = [bool]$RedirectInput
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    if ($RedirectInput) {
        $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    }
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8

    foreach ($arg in @(
        "--protocol=TCP",
        "--host=$($Target.host)",
        "--port=$($Target.port)",
        "--user=$Username",
        "--connect-timeout=5",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names"
    )) { [void]$psi.ArgumentList.Add($arg) }

    switch ([string]$Target.sslMode) {
        "disabled" {
            [void]$psi.ArgumentList.Add("--ssl=0")
        }
        "required" {
            [void]$psi.ArgumentList.Add("--ssl=1")
        }
        "verify-full" {
            [void]$psi.ArgumentList.Add("--ssl=1")
            [void]$psi.ArgumentList.Add("--ssl-verify-server-cert")
            if (-not [string]::IsNullOrWhiteSpace([string]$Target.sslCaPath)) {
                if (-not (Test-Path -LiteralPath $Target.sslCaPath -PathType Leaf)) {
                    throw "MariaDB TLS CA 파일이 없습니다: module=$($Target.moduleKey) path=$($Target.sslCaPath)"
                }
                [void]$psi.ArgumentList.Add("--ssl-ca=$($Target.sslCaPath)")
            }
        }
        "preferred" {
            # MariaDB Client의 기본 TLS negotiation을 사용합니다.
        }
        default {
            throw "지원하지 않는 MariaDB sslMode입니다: module=$($Target.moduleKey) sslMode=$($Target.sslMode)"
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($Database)) {
        [void]$psi.ArgumentList.Add("--database=$Database")
    }

    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        $psi.Environment["MYSQL_PWD"] = $Password
        $psi.Environment["MARIADB_PWD"] = $Password
    }
    return $psi
}

function Test-MariaConnection {
    param(
        $Target,
        [string] $Username,
        [string] $Password
    )

    $psi = New-MariaProcessStartInfo `
        -Target $Target `
        -Username $Username `
        -Password $Password
    [void]$psi.ArgumentList.Add("--execute=SELECT 1;")

    $p = [System.Diagnostics.Process]::new()
    $p.StartInfo = $psi
    [void]$p.Start()
    $stdout = $p.StandardOutput.ReadToEnd()
    $stderr = $p.StandardError.ReadToEnd()
    $p.WaitForExit()

    if ($p.ExitCode -ne 0) {
        $safeError = Protect-CpfSecretText $stderr @($Password)
        throw "MariaDB 접속/인증 사전검증 실패 module=$($Target.moduleKey) host=$($Target.host):$($Target.port) user=$Username exit=$($p.ExitCode) error=$safeError"
    }
}

function Invoke-MariaText {
    param(
        $Target,
        [string] $Username,
        [string] $Password,
        [string] $SqlText,
        [string] $Database = ""
    )

    $psi = New-MariaProcessStartInfo `
        -Target $Target `
        -Username $Username `
        -Password $Password `
        -Database $Database `
        -RedirectInput

    $p = [System.Diagnostics.Process]::new()
    $p.StartInfo = $psi
    [void]$p.Start()

    $stdoutTask = $p.StandardOutput.ReadToEndAsync()
    $stderrTask = $p.StandardError.ReadToEndAsync()

    $inputError = $null
    try {
        $p.StandardInput.Write($SqlText)
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try {
            $p.StandardInput.Close()
        } catch {
            if ([string]::IsNullOrWhiteSpace($inputError)) {
                $inputError = $_.Exception.Message
            }
        }
    }

    $p.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()

    if ($p.ExitCode -ne 0 -or -not [string]::IsNullOrWhiteSpace($inputError)) {
        $combined = if (-not [string]::IsNullOrWhiteSpace($stderr)) {
            $stderr.Trim()
        } else {
            [string]$inputError
        }
        $safeError = Protect-CpfSecretText $combined @($Password)
        throw "MariaDB 실행 실패 module=$($Target.moduleKey) host=$($Target.host):$($Target.port) user=$Username database=$Database exit=$($p.ExitCode) error=$safeError"
    }

    return $stdout
}

function Sql-HexLiteral {
    param([string] $Value)
    $hex = [Convert]::ToHexString([Text.Encoding]::UTF8.GetBytes($Value))
    return "CONVERT(0x$hex USING utf8mb4)"
}

function Quote-Db {
    param([string] $Identifier)
    return "``$Identifier``"
}

function Quote-UserHost {
    param([string] $Username, [string] $HostPart)
    $u = $Username.Replace("'", "''")
    $h = $HostPart.Replace("'", "''")
    return "'$u'@'$h'"
}

function Get-UseSections {
    param([string] $Text)
    # Current lifecycle bundles are database-neutral and identify ownership with an explicit marker.
    # Older MariaDB packs used USE <logicalDb>; support both, but never silently return an empty pack.
    $markerPattern = '(?im)^[ \t]*--[ \t]*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)[ \t]*$'
    $matches = [regex]::Matches($Text, $markerPattern)
    if ($matches.Count -eq 0) {
        $matches = [regex]::Matches(
            $Text,
            '(?im)^[ \t]*USE[ \t]+`?([A-Za-z][A-Za-z0-9_$#]*)`?[ \t]*;[ \t]*$'
        )
    }
    $list = New-Object System.Collections.Generic.List[object]
    for ($i = 0; $i -lt $matches.Count; $i++) {
        $start = $matches[$i].Index
        $end = if ($i + 1 -lt $matches.Count) { $matches[$i + 1].Index } else { $Text.Length }
        $list.Add([pscustomobject]@{
            logicalDatabase = $matches[$i].Groups[1].Value
            text = $Text.Substring($start, $end - $start)
        })
    }
    return $list.ToArray()
}

function Render-LogicalDatabaseNames {
    param([string] $Sql)
    $rendered = $Sql
    foreach ($logical in $logicalToKey.Keys) {
        $key = $logicalToKey[$logical]
        $target = $moduleProfiles[$key]
        if ($target.enabled) {
            $physical = $target.databaseName
            $rendered = [regex]::Replace(
                $rendered,
                "(?<![A-Za-z0-9_])" + [regex]::Escape($logical) + "(?![A-Za-z0-9_])",
                [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $physical }
            )
        }
    }
    return $rendered
}

function Get-ModuleSql {
    param([string] $File, [string] $LogicalDatabase, [switch] $StripBaseline)
    $text = [IO.File]::ReadAllText($File, [Text.Encoding]::UTF8)
    if ($StripBaseline) {
        $marker = "-- Product Seed의 마지막 단계까지 성공한 CPF 소유 Schema만 공식 Baseline으로 기록합니다."
        $idx = $text.IndexOf($marker, [StringComparison]::Ordinal)
        if ($idx -ge 0) { $text = $text.Substring(0, $idx) }
    }
    $sections = @(Get-UseSections $text | Where-Object { $_.logicalDatabase -eq $LogicalDatabase })
    if ($sections.Count -eq 0) { return "" }
    $joined = ($sections | ForEach-Object { $_.text }) -join "`n"
    return Render-LogicalDatabaseNames $joined
}

function Split-CpfSqlDefinitions {
    param([string] $Body)

    $definitions = [System.Collections.Generic.List[string]]::new()
    $current = [System.Text.StringBuilder]::new()
    $depth = 0
    [char] $quote = [char]0
    for ($index = 0; $index -lt $Body.Length; $index++) {
        $character = $Body[$index]
        if ($quote -ne [char]0) {
            [void] $current.Append($character)
            if ($character -eq $quote) {
                if ($index + 1 -lt $Body.Length -and $Body[$index + 1] -eq $quote) {
                    [void] $current.Append($Body[++$index])
                } elseif ($index -eq 0 -or $Body[$index - 1] -ne '\') {
                    $quote = [char]0
                }
            }
            continue
        }
        if ($character -in @("'", '"', '`')) {
            $quote = $character
            [void] $current.Append($character)
            continue
        }
        if ($character -eq '(') {
            $depth++
            [void] $current.Append($character)
            continue
        }
        if ($character -eq ')') {
            $depth--
            if ($depth -lt 0) { throw "Table DDL 괄호 깊이가 올바르지 않습니다." }
            [void] $current.Append($character)
            continue
        }
        if ($character -eq ',' -and $depth -eq 0) {
            $definition = $current.ToString().Trim()
            if (-not [string]::IsNullOrWhiteSpace($definition)) {
                $definitions.Add($definition)
            }
            [void] $current.Clear()
            continue
        }
        [void] $current.Append($character)
    }
    if ($quote -ne [char]0 -or $depth -ne 0) {
        throw "Table DDL 문자열 또는 괄호가 닫히지 않았습니다."
    }
    $last = $current.ToString().Trim()
    if (-not [string]::IsNullOrWhiteSpace($last)) {
        $definitions.Add($last)
    }
    return $definitions.ToArray()
}

function Get-ExpectedTableColumns {
    param([string] $Sql)

    $rows = New-Object System.Collections.Generic.List[object]
    $rowsByTable = @{}
    $matches = [regex]::Matches(
        $Sql,
        '(?is)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\((.*?)\)\s*ENGINE='
    )
    foreach ($match in $matches) {
        $tableName = $match.Groups[1].Value
        $body = $match.Groups[2].Value
        $columns = New-Object System.Collections.Generic.List[string]
        foreach ($lineRaw in (Split-CpfSqlDefinitions $body)) {
            $line = [regex]::Replace($lineRaw.Trim(), '\s+', ' ')
            $columnMatch = [regex]::Match(
                $line,
                '^`?([A-Za-z][A-Za-z0-9_]*)`?\s+(BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|TIME|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($columnMatch.Success) {
                $columns.Add($columnMatch.Groups[1].Value)
            }
        }
        $row = [pscustomobject]@{
            tableName = $tableName
            columns = @($columns.ToArray())
        }
        $rows.Add($row)
        $rowsByTable[$tableName.ToLowerInvariant()] = $row
    }

    $alterColumnRegex = [regex]::new(
        'ALTER\s+TABLE\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s+ADD\s+COLUMN\s+(?:IF\s+NOT\s+EXISTS\s+)?`?([A-Za-z][A-Za-z0-9_]*)`?\s+(BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|TIME|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
    foreach ($alterMatch in $alterColumnRegex.Matches($Sql)) {
        $tableKey = $alterMatch.Groups[1].Value.ToLowerInvariant()
        if (-not $rowsByTable.ContainsKey($tableKey)) {
            throw "ALTER TABLE이 설치 Bundle에 없는 Table을 참조합니다: $($alterMatch.Groups[1].Value)"
        }
        $row = $rowsByTable[$tableKey]
        $columnName = $alterMatch.Groups[2].Value
        if ($columnName -notin @($row.columns)) {
            $row.columns = @($row.columns) + $columnName
        }
    }
    return @($rows.ToArray())
}

function Assert-MariaSchemaColumns {
    param(
        $Target,
        [string] $DatabaseName,
        [object[]] $ExpectedTableColumns
    )

    foreach ($table in $ExpectedTableColumns) {
        $dbEscaped = $DatabaseName.Replace("'", "''")
        $tableEscaped = ([string]$table.tableName).Replace("'", "''")
        $actualText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT column_name
FROM information_schema.columns
WHERE table_schema = '$dbEscaped'
  AND table_name = '$tableEscaped'
ORDER BY ordinal_position;
"@
        $actual = @($actualText -split '\r?\n' | Where-Object { $_ })
        $expected = @($table.columns)
        $missing = @($expected | Where-Object { $_ -notin $actual })
        $unexpected = @($actual | Where-Object { $_ -notin $expected })
        if ($missing.Count -gt 0 -or $unexpected.Count -gt 0) {
            throw "Schema drift 감지. Fresh DDL로 덮어쓰지 않고 migration이 필요합니다. module=$($Target.moduleKey) table=$($table.tableName) missing=$($missing -join ',') unexpected=$($unexpected -join ',')"
        }
    }
}

function Assert-MariaSchemaManifest {
    param(
        $Target,
        [string] $ManifestPath
    )

    $manifest = Get-Content -LiteralPath $ManifestPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 100
    if ([int]$manifest.schemaVersion -lt 2) {
        throw "지원하지 않는 Database Schema Manifest입니다: $ManifestPath"
    }

    $expectedTables = @(
        $manifest.tables |
            Where-Object {
                [string]$_.vendor -eq "mariadb" -and
                [string]$_.logicalDatabase -eq [string]$Target.logicalDatabase
            }
    )
    if ($expectedTables.Count -eq 0) {
        throw "Schema Manifest에 Module Table이 없습니다: module=$($Target.moduleKey) logicalDatabase=$($Target.logicalDatabase)"
    }

    $dbEscaped = ([string]$Target.databaseName).Replace("'", "''")
    $actualTableText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = '$dbEscaped'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
"@
    $actualTableNames = @(
        $actualTableText -split '\r?\n' |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            ForEach-Object { $_.Trim().ToLowerInvariant() }
    )
    $expectedTableNames = @(
        $expectedTables |
            ForEach-Object { ([string]$_.tableName).ToLowerInvariant() }
    )
    $missingTables = @($expectedTableNames | Where-Object { $_ -notin $actualTableNames })
    $unexpectedTables = @($actualTableNames | Where-Object { $_ -notin $expectedTableNames })
    if ($missingTables.Count -gt 0 -or $unexpectedTables.Count -gt 0) {
        throw "Schema Manifest Table drift. module=$($Target.moduleKey) missing=$($missingTables -join ',') unexpected=$($unexpectedTables -join ',')"
    }

    $actualColumnText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = '$dbEscaped'
ORDER BY table_name, ordinal_position;
"@
    $actualColumns = @{}
    foreach ($line in @($actualColumnText -split '\r?\n' | Where-Object { $_ })) {
        $fields = $line.Split("`t", [System.StringSplitOptions]::None)
        if ($fields.Count -ne 2) {
            throw "Schema Manifest Column inventory 형식 오류: module=$($Target.moduleKey) line=$line"
        }
        $tableKey = $fields[0].ToLowerInvariant()
        if (-not $actualColumns.ContainsKey($tableKey)) {
            $actualColumns[$tableKey] = [System.Collections.Generic.List[string]]::new()
        }
        $actualColumns[$tableKey].Add($fields[1].ToLowerInvariant())
    }
    foreach ($table in $expectedTables) {
        $tableKey = ([string]$table.tableName).ToLowerInvariant()
        $expected = @($table.columns | ForEach-Object { ([string]$_).ToLowerInvariant() })
        $actual = if ($actualColumns.ContainsKey($tableKey)) {
            @($actualColumns[$tableKey].ToArray())
        } else {
            @()
        }
        if (($expected -join ",") -ne ($actual -join ",")) {
            throw "Schema Manifest Column drift. module=$($Target.moduleKey) table=$($table.tableName) expected=$($expected -join ',') actual=$($actual -join ',')"
        }
    }

    $actualIndexText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT
    table_name,
    index_name,
    non_unique,
    GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',')
FROM information_schema.statistics
WHERE table_schema = '$dbEscaped'
  AND index_name <> 'PRIMARY'
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;
"@
    $actualIndexes = @{}
    foreach ($line in @($actualIndexText -split '\r?\n' | Where-Object { $_ })) {
        $fields = $line.Split("`t", [System.StringSplitOptions]::None)
        if ($fields.Count -ne 4) {
            throw "Schema Manifest Index inventory 형식 오류: module=$($Target.moduleKey) line=$line"
        }
        $key = "$($fields[0].ToLowerInvariant())|$($fields[1].ToLowerInvariant())"
        $actualIndexes[$key] = [pscustomobject]@{
            unique = $fields[2] -eq "0"
            columns = $fields[3].ToLowerInvariant()
        }
    }

    $declaredIndexKeys = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase
    )
    $allowedImplicitIndexKeys = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase
    )
    foreach ($table in $expectedTables) {
        $tableKey = ([string]$table.tableName).ToLowerInvariant()
        foreach ($index in @($table.indexes)) {
            $key = "$tableKey|$(([string]$index.name).ToLowerInvariant())"
            [void]$declaredIndexKeys.Add($key)
            if (-not $actualIndexes.ContainsKey($key)) {
                throw "Schema Manifest Index 누락. module=$($Target.moduleKey) table=$($table.tableName) index=$($index.name)"
            }
            $actual = $actualIndexes[$key]
            $expectedColumns = @($index.columns | ForEach-Object { ([string]$_).ToLowerInvariant() }) -join ","
            if ([bool]$index.unique -ne [bool]$actual.unique -or $expectedColumns -ne $actual.columns) {
                throw "Schema Manifest Index drift. module=$($Target.moduleKey) table=$($table.tableName) index=$($index.name) expectedUnique=$($index.unique) actualUnique=$($actual.unique) expectedColumns=$expectedColumns actualColumns=$($actual.columns)"
            }
        }
        foreach ($foreignKey in @($table.foreignKeys)) {
            [void]$allowedImplicitIndexKeys.Add(
                "$tableKey|$(([string]$foreignKey.name).ToLowerInvariant())"
            )
        }
    }
    $unexpectedIndexes = @(
        $actualIndexes.Keys |
            Where-Object {
                -not $declaredIndexKeys.Contains($_) -and
                -not $allowedImplicitIndexKeys.Contains($_)
            }
    )
    if ($unexpectedIndexes.Count -gt 0) {
        throw "Schema Manifest에 없는 Index를 감지했습니다. module=$($Target.moduleKey) indexes=$($unexpectedIndexes -join ',')"
    }

    $actualForeignKeyText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT
    table_name,
    constraint_name,
    GROUP_CONCAT(column_name ORDER BY ordinal_position SEPARATOR ','),
    MIN(referenced_table_name),
    GROUP_CONCAT(referenced_column_name ORDER BY ordinal_position SEPARATOR ',')
FROM information_schema.key_column_usage
WHERE constraint_schema = '$dbEscaped'
  AND referenced_table_name IS NOT NULL
GROUP BY table_name, constraint_name
ORDER BY table_name, constraint_name;
"@
    $actualForeignKeys = @{}
    foreach ($line in @($actualForeignKeyText -split '\r?\n' | Where-Object { $_ })) {
        $fields = $line.Split("`t", [System.StringSplitOptions]::None)
        if ($fields.Count -ne 5) {
            throw "Schema Manifest FK inventory 형식 오류: module=$($Target.moduleKey) line=$line"
        }
        $key = "$($fields[0].ToLowerInvariant())|$($fields[1].ToLowerInvariant())"
        $actualForeignKeys[$key] = [pscustomobject]@{
            columns = $fields[2].ToLowerInvariant()
            referencedTable = $fields[3].ToLowerInvariant()
            referencedColumns = $fields[4].ToLowerInvariant()
        }
    }

    $declaredForeignKeyKeys = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase
    )
    foreach ($table in $expectedTables) {
        $tableKey = ([string]$table.tableName).ToLowerInvariant()
        foreach ($foreignKey in @($table.foreignKeys)) {
            $key = "$tableKey|$(([string]$foreignKey.name).ToLowerInvariant())"
            [void]$declaredForeignKeyKeys.Add($key)
            if (-not $actualForeignKeys.ContainsKey($key)) {
                throw "Schema Manifest FK 누락. module=$($Target.moduleKey) table=$($table.tableName) fk=$($foreignKey.name)"
            }
            $actual = $actualForeignKeys[$key]
            $expectedColumns = @($foreignKey.columns | ForEach-Object { ([string]$_).ToLowerInvariant() }) -join ","
            $expectedReferencedColumns = @(
                $foreignKey.referencedColumns |
                    ForEach-Object { ([string]$_).ToLowerInvariant() }
            ) -join ","
            if (
                $expectedColumns -ne $actual.columns -or
                ([string]$foreignKey.referencedTable).ToLowerInvariant() -ne $actual.referencedTable -or
                $expectedReferencedColumns -ne $actual.referencedColumns
            ) {
                throw "Schema Manifest FK drift. module=$($Target.moduleKey) table=$($table.tableName) fk=$($foreignKey.name)"
            }
        }
    }
    $unexpectedForeignKeys = @(
        $actualForeignKeys.Keys |
            Where-Object { -not $declaredForeignKeyKeys.Contains($_) }
    )
    if ($unexpectedForeignKeys.Count -gt 0) {
        throw "Schema Manifest에 없는 FK를 감지했습니다. module=$($Target.moduleKey) foreignKeys=$($unexpectedForeignKeys -join ',')"
    }

    return [pscustomobject]@{
        tableCount = $expectedTables.Count
        indexCount = $declaredIndexKeys.Count
        foreignKeyCount = $declaredForeignKeyKeys.Count
    }
}

function Get-ExpectedTables {
    param([string] $Sql)
    return @([regex]::Matches(
        $Sql,
        '(?im)CREATE[ \t]+TABLE[ \t]+IF[ \t]+NOT[ \t]+EXISTS[ \t]+`?([A-Za-z][A-Za-z0-9_]*)`?'
    ) | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "미검증"
    profilePath = $ProfilePath
    profileName = [string]$profile.profileName
    operationMode = if ($ProvisionOnly) { "provision-only" } else { "install" }
    seedMode = $SeedMode
    selectedModules = $selectedKeys
    generatedDomainTransitions = @($generatedProfileKeys | ForEach-Object {
            [ordered]@{
                profileKey = $_
                domainName = $moduleProfiles[$_].domainName
                systemCode = $moduleProfiles[$_].systemCode
                moduleName = $moduleProfiles[$_].moduleName
                enabledInPlatformPack = $moduleProfiles[$_].enabled
                installer = "cpf-tools/generator/tools/initialize-generated-domain-databases.ps1"
            }
        })
    modules = [ordered]@{}
    verify = [ordered]@{
        status = "미검증"
        checkCount = 0
        failedChecks = @()
    }
}

try {
    foreach ($key in $selectedKeys) {
        $t = $moduleProfiles[$key]
        $manifestContract = $null
        Write-Host "[$key] vendor=$($t.vendor) host=$($t.host):$($t.port) database=$($t.databaseName)"

        if ($RequireRun) {
            Test-MariaConnection $t $t.adminUsername $t.adminPassword
            Write-Host "[$key] admin connection preflight=PASS"
        }

        $installSql = Get-ModuleSql $installFile $t.logicalDatabase
        if ([string]::IsNullOrWhiteSpace($installSql)) {
            throw "Module DDL section이 없습니다: module=$key logicalDatabase=$($t.logicalDatabase)"
        }
        $expectedTables = @(Get-ExpectedTables $installSql)
        $expectedTableColumns = @(Get-ExpectedTableColumns $installSql)
        if ($expectedTables.Count -eq 0) {
            throw "Module expected table을 추출할 수 없습니다: module=$key"
        }

        $dbQuoted = Quote-Db $t.databaseName
        $migrationAccount = Quote-UserHost $t.migrationUsername $t.migrationUserHost
        $runtimeAccount = Quote-UserHost $t.runtimeUsername $t.runtimeUserHost
        # CONCAT() 안의 SQL 문자열에 계정 literal을 다시 삽입하므로 작은따옴표를 한 번 더 escape한다.
        # 예: 'cpf_migration'@'%' -> ''cpf_migration''@''%''
        $migrationAccountDynamic = $migrationAccount.Replace("'", "''")
        $runtimeAccountDynamic = $runtimeAccount.Replace("'", "''")
        $migPwd = Sql-HexLiteral $t.migrationPassword
        $runPwd = Sql-HexLiteral $t.runtimePassword

        $provisionSql = @"
CREATE DATABASE IF NOT EXISTS $dbQuoted CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET @cpf_mig_pwd = $migPwd;
SET @cpf_run_pwd = $runPwd;
SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS $migrationAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_mig_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = CONCAT('ALTER USER $migrationAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_mig_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
GRANT ALL PRIVILEGES ON $dbQuoted.* TO $migrationAccount;
SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS $runtimeAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_run_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = CONCAT('ALTER USER $runtimeAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_run_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON $dbQuoted.* TO $runtimeAccount;
SET @cpf_mig_pwd = NULL; SET @cpf_run_pwd = NULL; SET @cpf_sql = NULL;
FLUSH PRIVILEGES;
"@
        if ($RequireRun) {
            [void](Invoke-MariaText $t $t.adminUsername $t.adminPassword $provisionSql)
            if ($ProvisionOnly) {
                Test-MariaConnection $t $t.migrationUsername $t.migrationPassword
                Test-MariaConnection $t $t.runtimeUsername $t.runtimePassword
                Write-Host "[$key] provision-only service-user/grant connection=PASS"
                $result.modules[$key] = [ordered]@{
                    status = "완료"
                    operationMode = "provision-only"
                    vendor = $t.vendor
                    host = $t.host
                    port = $t.port
                    databaseName = $t.databaseName
                    schemaName = $t.schemaName
                    domainName = $t.domainName
                    systemCode = $t.systemCode
                    moduleName = $t.moduleName
                    migrationUsername = $t.migrationUsername
                    runtimeUsername = $t.runtimeUsername
                }
                continue
            }

            $actualBeforeText = Invoke-MariaText $t $t.adminUsername $t.adminPassword @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = '$($t.databaseName.Replace("'","''"))'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
"@
            $actualBefore = @($actualBeforeText -split '\r?\n' | Where-Object { $_ })
            $existingExpected = @($actualBefore | Where-Object { $_ -in $expectedTables })
            if ($existingExpected.Count -gt 0 -and $existingExpected.Count -ne $expectedTables.Count) {
                throw "부분 설치 DB를 감지했습니다. 자동 보완/Reset하지 않습니다: module=$key expected=$($expectedTables.Count) existing=$($existingExpected.Count)"
            }

            if ($existingExpected.Count -eq 0) {
                Write-Host "[$key] ddl=fresh-install expectedTables=$($expectedTables.Count)"
                $renderedInstall = "USE $dbQuoted;`n" + $installSql
                [void](Invoke-MariaText $t $t.migrationUsername $t.migrationPassword $renderedInstall $t.databaseName)
            } else {
                Assert-MariaSchemaColumns $t $t.databaseName $expectedTableColumns
                Write-Host "[$key] ddl=skip-existing-complete schemaDrift=NONE expectedTables=$($expectedTables.Count)"
            }

            $seedPlans = switch ($SeedMode) {
                "none" {
                    @()
                }
                "product" {
                    @([pscustomobject]@{ name = "product"; enabled = $true; file = $productSeedFile })
                }
                "all" {
                    @(
                        [pscustomobject]@{ name = "product"; enabled = $true; file = $productSeedFile },
                        [pscustomobject]@{ name = "optionalSample"; enabled = $true; file = $optionalSampleSeedFile },
                        [pscustomobject]@{ name = "test"; enabled = $true; file = $testSeedFile }
                    )
                }
                default {
                    @(
                        [pscustomobject]@{ name = "product"; enabled = $t.productSeed; file = $productSeedFile },
                        [pscustomobject]@{ name = "optionalSample"; enabled = $t.optionalSampleSeed; file = $optionalSampleSeedFile },
                        [pscustomobject]@{ name = "test"; enabled = $t.testSeed; file = $testSeedFile }
                    )
                }
            }
            foreach ($seedPlan in $seedPlans) {
                if (-not $seedPlan.enabled) { continue }
                $seedSql = Get-ModuleSql $seedPlan.file $t.logicalDatabase
                if ([string]::IsNullOrWhiteSpace($seedSql)) { continue }

                Write-Host "[$key] seed=$($seedPlan.name)"
                $seedSql = "USE $dbQuoted;`n" + $seedSql
                [void](Invoke-MariaText $t $t.migrationUsername $t.migrationPassword $seedSql $t.databaseName)
            }

            $actualText = Invoke-MariaText $t $t.adminUsername $t.adminPassword @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = '$($t.databaseName.Replace("'","''"))'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
"@
            $actual = @($actualText -split '\r?\n' | Where-Object { $_ })
            $missing = @($expectedTables | Where-Object { $_ -notin $actual })
            if ($missing.Count -gt 0) {
                throw "tables 검증 실패. module=$key 누락=$($missing -join ', ')"
            }
            Assert-MariaSchemaColumns $t $t.databaseName $expectedTableColumns
            $manifestContract = Assert-MariaSchemaManifest $t $schemaManifestFile
            Write-Host "[$key] schema manifest=PASS tables=$($manifestContract.tableCount) indexes=$($manifestContract.indexCount) foreignKeys=$($manifestContract.foreignKeyCount)"

            $probeTable = $expectedTables[0]
            [void](Invoke-MariaText $t $t.runtimeUsername $t.runtimePassword "SELECT COUNT(*) FROM ``$probeTable``;" $t.databaseName)
        }

        $result.modules[$key] = [ordered]@{
            status = if ($RequireRun) { "완료" } else { "미검증" }
            vendor = $t.vendor
            host = $t.host
            port = $t.port
            databaseName = $t.databaseName
            schemaName = $t.schemaName
            domainName = $t.domainName
            systemCode = $t.systemCode
            moduleName = $t.moduleName
            expectedTableCount = $expectedTables.Count
            manifestIndexCount = if ($null -ne $manifestContract) { $manifestContract.indexCount } else { $null }
            manifestForeignKeyCount = if ($null -ne $manifestContract) { $manifestContract.foreignKeyCount } else { $null }
            migrationUsername = $t.migrationUsername
            runtimeUsername = $t.runtimeUsername
        }
    }

    if ($RequireRun -and -not $ProvisionOnly -and $moduleProfiles[$coreKey].enabled) {
        $core = $moduleProfiles[$coreKey]
        $baselineTableText = Invoke-MariaText $core $core.adminUsername $core.adminPassword @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = '$($core.databaseName.Replace("'","''"))'
  AND table_name = 'cpf_schema_installation';
"@
        if ([int](($baselineTableText -split '\r?\n' | Where-Object { $_ } | Select-Object -First 1)) -eq 1) {
            $values = @()
            foreach ($key in $selectedKeys) {
                $t = $moduleProfiles[$key]
                $values += "('$($t.databaseName)', '$($t.systemCode)', '$($t.vendor.ToUpperInvariant())', '1.0.0-SNAPSHOT', 'CPF_PROFILE_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER')"
            }
            $baselineSql = @"
INSERT INTO cpf_schema_installation (
    schema_name, system_code, database_vendor, product_version,
    baseline_key, install_state, created_by, updated_by
) VALUES
$($values -join ",`n")
ON DUPLICATE KEY UPDATE
    system_code=VALUES(system_code),
    database_vendor=VALUES(database_vendor),
    product_version=VALUES(product_version),
    baseline_key=VALUES(baseline_key),
    install_state=VALUES(install_state),
    updated_by=VALUES(updated_by),
    updated_at=CURRENT_TIMESTAMP(3);
"@
            [void](Invoke-MariaText $core $core.migrationUsername $core.migrationPassword $baselineSql $core.databaseName)
        } else {
            Write-Host "CPF baseline registry=SKIP (core baseline table not installed yet)"
        }
    }

    if ($RequireRun -and -not $ProvisionOnly -and $fullPlatformSelection) {
        $core = $moduleProfiles[$coreKey]
        $verifySql = Get-Content -LiteralPath $verifyFile -Raw -Encoding UTF8
        if ([string]::IsNullOrWhiteSpace($verifySql)) {
            throw "MariaDB Verify Pack이 비어 있습니다: $verifyFile"
        }
        $verifyText = Invoke-MariaText `
            $core `
            $core.adminUsername `
            $core.adminPassword `
            $verifySql `
            $core.databaseName
        $verifyRows = @($verifyText -split '\r?\n' | Where-Object { $_ })
        if ($verifyRows.Count -eq 0) {
            throw "MariaDB Verify Pack이 검증 결과를 반환하지 않았습니다."
        }
        $failedChecks = [System.Collections.Generic.List[string]]::new()
        $seenChecks = [System.Collections.Generic.HashSet[string]]::new(
            [System.StringComparer]::OrdinalIgnoreCase
        )
        foreach ($line in $verifyRows) {
            $fields = $line.Split("`t", [System.StringSplitOptions]::None)
            if ($fields.Count -ne 2 -or [string]::IsNullOrWhiteSpace($fields[0])) {
                throw "MariaDB Verify 출력 계약 위반입니다. expected=check_name<TAB>passed line=$line"
            }
            if (-not $seenChecks.Add($fields[0])) {
                throw "MariaDB Verify check_name이 중복되었습니다: $($fields[0])"
            }
            if ($fields[1] -ne "1") {
                $failedChecks.Add($fields[0])
            }
        }
        $result.verify.checkCount = $verifyRows.Count
        $result.verify.failedChecks = @($failedChecks.ToArray())
        if ($failedChecks.Count -gt 0) {
            $result.verify.status = "실패"
            throw "MariaDB Verify 실패: $($failedChecks -join ', ')"
        }
        $result.verify.status = "완료"
        Write-Host "MariaDB canonical verify=PASS checks=$($verifyRows.Count)"
    } elseif ($RequireRun -and -not $ProvisionOnly) {
        $result.verify.reason = if ($generatedProfileKeys.Count -gt 0) {
            "Generated Domain legacy section retirement 전에는 Platform Full Verify Pack을 실행하지 않습니다."
        } else {
            "전체 enabled Platform Module 선택이 아니므로 Full Verify Pack은 실행하지 않았습니다."
        }
    }

    $result.status = if ($RequireRun) { "완료" } else { "미검증" }
}
catch {
    $result.status = "실패"
    $result.error = $_.Exception.Message
    throw
}
finally {
    $result.finishedAt = (Get-Date).ToString("o")
    $resultPath = Join-Path $ResultDir "database-profile-install-result.sanitized.json"
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 50) + "`n", $Utf8NoBom)
    Write-Host "Sanitized result: $resultPath"
}

if (-not $RequireRun) {
    Write-Host "CPF DB Profile plan 검증 완료. 실제 DB는 변경하지 않았습니다."
} elseif ($ProvisionOnly) {
    Write-Host "CPF DB Profile 기반 Service User/Grant Provision 완료."
} else {
    Write-Host "CPF DB Profile 기반 설치/검증 완료."
}
