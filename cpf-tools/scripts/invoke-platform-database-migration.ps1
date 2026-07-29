[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path,
    [string] $ProfilePath = "",
    [ValidateSet("upgrade", "rollback")]
    [string] $Direction = "upgrade",
    [int] $FromVersion = -1,
    [int] $ToVersion = -1,
    [int[]] $MigrationVersion = @(),
    [string[]] $Modules = @(),
    [string] $ResultPath = "",
    [switch] $DryRun,
    [switch] $Apply,
    [switch] $ConfirmApply,
    [switch] $ConfirmApplicationsStopped,
    [switch] $ConfirmRollbackReady,
    [string] $ExpectedPlanSha256 = "",
    [string[]] $BackupManifestPath = @()
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF Platform DB migration tool은 pwsh 7 이상이 필요합니다."
}

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$Modules = @(
    $Modules |
        ForEach-Object { $_ -split "," } |
        ForEach-Object { $_.Trim() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
)

. (Join-Path $PSScriptRoot "database-profile-common.ps1")

function Get-CpfRelativePath {
    param([Parameter(Mandatory = $true)][string] $Path)
    return ([IO.Path]::GetRelativePath($Root, [IO.Path]::GetFullPath($Path))).Replace("\", "/")
}

function Get-CpfSha256 {
    param([Parameter(Mandatory = $true)][string] $Text)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($algorithm.ComputeHash($Utf8NoBom.GetBytes($Text)))).
            Replace("-", "").
            ToLowerInvariant()
    } finally {
        $algorithm.Dispose()
    }
}

function Get-CpfFileSha256 {
    param([Parameter(Mandatory = $true)][string] $Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-CpfMigrationVersion {
    param([Parameter(Mandatory = $true)][string] $Name)
    if ($Name -notmatch "^V([0-9]+)__.+\.sql$") {
        throw "올바르지 않은 migration 파일명입니다: $Name"
    }
    return [int]$Matches[1]
}

function Get-CpfMigrationChecksumMap {
    param([Parameter(Mandatory = $true)][string] $Directory)

    $manifestPath = Join-Path $Directory "checksums.sha256"
    if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
        throw "Migration checksum manifest가 없습니다: $(Get-CpfRelativePath $manifestPath)"
    }

    $entries = @{}
    $versions = @{}
    foreach ($line in Get-Content -LiteralPath $manifestPath -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line -notmatch "^([0-9a-fA-F]{64}) \*(V([0-9]+)__.+\.sql)$") {
            throw "Migration checksum manifest 형식이 올바르지 않습니다: $(Get-CpfRelativePath $manifestPath)"
        }
        $fileName = $Matches[2]
        $version = [int]$Matches[3]
        if ($entries.ContainsKey($fileName) -or $versions.ContainsKey($version)) {
            throw "Migration checksum manifest에 중복 version/file이 있습니다: $(Get-CpfRelativePath $manifestPath)"
        }
        $entries[$fileName] = $Matches[1].ToLowerInvariant()
        $versions[$version] = $fileName
    }
    if ($entries.Count -eq 0) {
        throw "Migration checksum manifest가 비어 있습니다: $(Get-CpfRelativePath $manifestPath)"
    }
    return $entries
}

function Get-CpfVersionedMigrationFile {
    param(
        [Parameter(Mandatory = $true)][string] $Directory,
        [Parameter(Mandatory = $true)][int] $Version,
        [Parameter(Mandatory = $true)] $ChecksumMap
    )

    $matches = @(Get-ChildItem -LiteralPath $Directory -File -Filter "V${Version}__*.sql")
    if ($matches.Count -ne 1) {
        throw "Migration V$Version 파일은 정확히 하나여야 합니다: directory=$(Get-CpfRelativePath $Directory) count=$($matches.Count)"
    }
    $file = $matches[0]
    if (-not $ChecksumMap.ContainsKey($file.Name)) {
        throw "Migration V$Version checksum 항목이 없습니다: $(Get-CpfRelativePath $file.FullName)"
    }
    $actualHash = Get-CpfFileSha256 $file.FullName
    if ($actualHash -ne [string]$ChecksumMap[$file.Name]) {
        throw "Migration V$Version checksum이 일치하지 않습니다: $(Get-CpfRelativePath $file.FullName)"
    }
    return $file
}

function Get-CpfVersionedRollbackFile {
    param(
        [Parameter(Mandatory = $true)][string] $Directory,
        [Parameter(Mandatory = $true)][int] $Version
    )

    if (-not (Test-Path -LiteralPath $Directory -PathType Container)) {
        throw "Rollback directory가 없습니다: $(Get-CpfRelativePath $Directory)"
    }
    $matches = @(
        Get-ChildItem -LiteralPath $Directory -File |
            Where-Object {
                $_.Name -match ("^R{0}__.+\.sql$" -f $Version) -or
                $_.Name -match ("^V{0}__.+_rollback\.sql$" -f $Version)
            }
    )
    if ($matches.Count -ne 1) {
        throw "Rollback V$Version 파일은 정확히 하나여야 합니다: directory=$(Get-CpfRelativePath $Directory) count=$($matches.Count)"
    }
    return $matches[0]
}

function Resolve-CpfLifecyclePath {
    param(
        [Parameter(Mandatory = $true)][string] $Pattern,
        [Parameter(Mandatory = $true)][AllowEmptyString()][string] $LogicalDatabase
    )

    if ($Pattern.Contains("{logicalDatabase}")) {
        $resolved = $Pattern.Replace("{logicalDatabase}", $LogicalDatabase)
    } else {
        $resolved = $Pattern
    }
    return [IO.Path]::GetFullPath((Join-Path $Root $resolved))
}

function Convert-CpfLogicalIdentifier {
    param(
        [Parameter(Mandatory = $true)][string] $Sql,
        [Parameter(Mandatory = $true)][string] $LogicalDatabase,
        [Parameter(Mandatory = $true)][string] $PhysicalIdentifier
    )

    $pattern = "(?i)(?<![A-Za-z0-9_`$#])" + [regex]::Escape($LogicalDatabase) + "(?![A-Za-z0-9_`$#])"
    return [regex]::Replace($Sql, $pattern, [Text.RegularExpressions.MatchEvaluator] {
            param($match)
            return $PhysicalIdentifier
        })
}

function Get-CpfMariaSections {
    param(
        [Parameter(Mandatory = $true)][string] $Sql,
        [Parameter(Mandatory = $true)][hashtable] $TargetByLogicalDatabase,
        [Parameter(Mandatory = $true)][string] $DisplayPath,
        [AllowNull()] $ExplicitRouting
    )

    $usePattern = "(?im)^\s*USE\s+`?([A-Za-z][A-Za-z0-9_`$#]{0,62})`?\s*;\s*$"
    $matches = [regex]::Matches($Sql, $usePattern)
    if ($matches.Count -eq 0) {
        if ($null -eq $ExplicitRouting) {
            throw "MariaDB migration에는 명시적 USE logicalDatabase routing이 필요합니다: $DisplayPath"
        }
        $routingSections = @($ExplicitRouting.sections)
        if ($routingSections.Count -eq 0) {
            throw "MariaDB explicit routing section이 비어 있습니다: $DisplayPath"
        }
        $sections = [System.Collections.Generic.List[object]]::new()
        $cursor = 0
        for ($index = 0; $index -lt $routingSections.Count; $index++) {
            $routing = $routingSections[$index]
            $logicalDatabase = [string]$routing.logicalDatabase
            $lookupKey = $logicalDatabase.ToLowerInvariant()
            if ([string]::IsNullOrWhiteSpace($lookupKey) -or
                -not $TargetByLogicalDatabase.ContainsKey($lookupKey)) {
                throw "Explicit routing이 요구하는 logical DB Module을 모두 선택해야 합니다: file=$DisplayPath logicalDatabase=$logicalDatabase"
            }

            $startMarkerProperty = $routing.PSObject.Properties["startMarker"]
            $endMarkerProperty = $routing.PSObject.Properties["endMarker"]
            $startMarker = if ($null -eq $startMarkerProperty) { "" } else { [string]$startMarkerProperty.Value }
            $endMarker = if ($null -eq $endMarkerProperty) { "" } else { [string]$endMarkerProperty.Value }
            $start = if ([string]::IsNullOrWhiteSpace($startMarker)) {
                $cursor
            } else {
                $first = $Sql.IndexOf($startMarker, [StringComparison]::Ordinal)
                $last = $Sql.LastIndexOf($startMarker, [StringComparison]::Ordinal)
                if ($first -lt 0 -or $first -ne $last) {
                    throw "MariaDB explicit routing start marker는 정확히 하나여야 합니다: file=$DisplayPath marker=$startMarker"
                }
                $first
            }
            $end = if ([string]::IsNullOrWhiteSpace($endMarker)) {
                if ($index -eq $routingSections.Count - 1) {
                    $Sql.Length
                } else {
                    throw "마지막이 아닌 MariaDB routing section에는 endMarker가 필요합니다: file=$DisplayPath logicalDatabase=$logicalDatabase"
                }
            } else {
                $first = $Sql.IndexOf($endMarker, [StringComparison]::Ordinal)
                $last = $Sql.LastIndexOf($endMarker, [StringComparison]::Ordinal)
                if ($first -lt 0 -or $first -ne $last) {
                    throw "MariaDB explicit routing end marker는 정확히 하나여야 합니다: file=$DisplayPath marker=$endMarker"
                }
                $first
            }
            if ($start -ne $cursor -or $end -le $start) {
                throw "MariaDB explicit routing section이 겹치거나 SQL을 누락합니다: file=$DisplayPath logicalDatabase=$logicalDatabase"
            }

            $sectionSql = $Sql.Substring($start, $end - $start)
            foreach ($otherKey in $TargetByLogicalDatabase.Keys) {
                if ($otherKey -eq $lookupKey) { continue }
                $otherLogical = [string]$TargetByLogicalDatabase[$otherKey].logicalDatabase
                $otherPattern = "(?i)(?<![A-Za-z0-9_`$#])" + [regex]::Escape($otherLogical) + "(?![A-Za-z0-9_`$#])"
                if ([regex]::IsMatch($sectionSql, $otherPattern)) {
                    throw "MariaDB explicit routing section의 cross-database 참조는 허용하지 않습니다: file=$DisplayPath current=$logicalDatabase referenced=$otherLogical"
                }
            }
            $target = $TargetByLogicalDatabase[$lookupKey]
            $sections.Add([pscustomobject]@{
                    logicalDatabase = $logicalDatabase
                    target = $target
                    sql = Convert-CpfLogicalIdentifier $sectionSql $logicalDatabase $target.databaseName
                })
            $cursor = $end
        }
        if ($cursor -ne $Sql.Length) {
            throw "MariaDB explicit routing contract가 전체 SQL을 소유하지 않습니다: file=$DisplayPath"
        }
        return @($sections)
    }

    $preamble = $Sql.Substring(0, $matches[0].Index)
    $preambleWithoutComments = [regex]::Replace($preamble, "(?ms)/\*.*?\*/|^\s*--.*?$", "").Trim()
    if (-not [string]::IsNullOrWhiteSpace($preambleWithoutComments)) {
        throw "첫 USE 앞에 실행 SQL이 있어 routing할 수 없습니다: $DisplayPath"
    }

    $sections = [System.Collections.Generic.List[object]]::new()
    for ($index = 0; $index -lt $matches.Count; $index++) {
        $match = $matches[$index]
        $logicalDatabase = [string]$match.Groups[1].Value
        $lookupKey = $logicalDatabase.ToLowerInvariant()
        if (-not $TargetByLogicalDatabase.ContainsKey($lookupKey)) {
            throw "Migration이 선택되지 않았거나 Generated Domain인 logical DB를 참조합니다: file=$DisplayPath logicalDatabase=$logicalDatabase"
        }

        $end = if ($index + 1 -lt $matches.Count) { $matches[$index + 1].Index } else { $Sql.Length }
        $sectionSql = $Sql.Substring($match.Index, $end - $match.Index)
        foreach ($otherKey in $TargetByLogicalDatabase.Keys) {
            if ($otherKey -eq $lookupKey) { continue }
            $otherLogical = [string]$TargetByLogicalDatabase[$otherKey].logicalDatabase
            $otherPattern = "(?i)(?<![A-Za-z0-9_`$#])" + [regex]::Escape($otherLogical) + "(?![A-Za-z0-9_`$#])"
            if ([regex]::IsMatch($sectionSql, $otherPattern)) {
                throw "MariaDB migration section의 cross-database 참조는 자동 추정하지 않습니다: file=$DisplayPath current=$logicalDatabase referenced=$otherLogical"
            }
        }

        $target = $TargetByLogicalDatabase[$lookupKey]
        $rendered = Convert-CpfLogicalIdentifier $sectionSql $logicalDatabase $target.databaseName
        $sections.Add([pscustomobject]@{
                logicalDatabase = $logicalDatabase
                target = $target
                sql = $rendered
            })
    }

    $groups = [System.Collections.Generic.List[object]]::new()
    foreach ($logicalDatabase in @($sections.logicalDatabase | Select-Object -Unique)) {
        $owned = @($sections | Where-Object {
                $_.logicalDatabase.Equals($logicalDatabase, [StringComparison]::OrdinalIgnoreCase)
            })
        $groups.Add([pscustomobject]@{
                logicalDatabase = $logicalDatabase
                target = $owned[0].target
                sql = ($owned.sql -join "`n")
            })
    }
    return @($groups)
}

function Get-CpfMariaRoutingEntry {
    param(
        [AllowNull()] $Manifest,
        [Parameter(Mandatory = $true)][string] $FileName
    )
    if ($null -eq $Manifest) { return $null }
    $matches = @($Manifest.files.PSObject.Properties | Where-Object { $_.Name -ceq $FileName })
    if ($matches.Count -eq 0) { return $null }
    if ($matches.Count -ne 1) {
        throw "MariaDB explicit routing file entry가 중복되었습니다: $FileName"
    }
    return $matches[0].Value
}

function Get-CpfPhysicalQualifier {
    param(
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)] $Target
    )
    if ($Vendor -eq "mariadb") { return [string]$Target.databaseName }
    if (-not [string]::IsNullOrWhiteSpace([string]$Target.schemaName)) {
        return [string]$Target.schemaName
    }
    if ($Vendor -eq "postgresql") { return "public" }
    return [string]$Target.migrationUsername
}

function Get-CpfPlanPayload {
    param(
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)][object[]] $Operations,
        [Parameter(Mandatory = $true)][string] $SelectionMode,
        [Parameter(Mandatory = $true)][int[]] $Versions
    )

    return [ordered]@{
        schemaVersion = 1
        direction = $Direction
        vendor = $Vendor
        selectionMode = $SelectionMode
        fromVersion = if ($SelectionMode -eq "range") { $FromVersion } else { $null }
        toVersion = if ($SelectionMode -eq "range") { $ToVersion } else { $null }
        versions = @($Versions)
        operations = @($Operations | ForEach-Object {
                [ordered]@{
                    order = $_.order
                    version = $_.version
                    moduleKey = $_.target.moduleKey
                    moduleName = $_.target.moduleName
                    systemCode = $_.target.systemCode
                    logicalDatabase = $_.target.logicalDatabase
                    physicalDatabase = $_.target.databaseName
                    physicalSchema = $_.target.schemaName
                    migrationPath = $_.migrationPath
                    migrationSha256 = $_.migrationSha256
                    rollbackPath = $_.rollbackPath
                    rollbackSha256 = $_.rollbackSha256
                    selectedPath = $_.selectedPath
                    selectedSha256 = $_.selectedSha256
                    renderedSha256 = $_.renderedSha256
                }
            })
    }
}

function Test-CpfBackupCoverage {
    param(
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)][object[]] $Operations,
        [Parameter(Mandatory = $true)][string[]] $ManifestPaths
    )

    if ($ManifestPaths.Count -eq 0) {
        throw "Apply에는 선택된 모든 physical DB의 -BackupManifestPath가 필요합니다."
    }

    $covered = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($requestedPath in $ManifestPaths) {
        $absoluteManifest = if ([IO.Path]::IsPathRooted($requestedPath)) {
            [IO.Path]::GetFullPath($requestedPath)
        } else {
            [IO.Path]::GetFullPath((Join-Path $Root $requestedPath))
        }
        if (-not (Test-Path -LiteralPath $absoluteManifest -PathType Leaf)) {
            throw "Backup manifest가 없습니다: $requestedPath"
        }
        $manifest = Get-Content -LiteralPath $absoluteManifest -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
        if (([string]$manifest.vendor).ToLowerInvariant() -ne $Vendor) {
            throw "Backup manifest vendor가 plan과 다릅니다: $requestedPath"
        }
        $database = [string]$manifest.database
        if ([string]::IsNullOrWhiteSpace($database)) {
            throw "Backup manifest database가 비어 있습니다: $requestedPath"
        }
        $backupFileName = [string]$manifest.backupFile
        $expectedHash = ([string]$manifest.sha256).ToLowerInvariant()
        if ([string]::IsNullOrWhiteSpace($backupFileName) -or $expectedHash -notmatch "^[0-9a-f]{64}$") {
            throw "Backup manifest file/hash 계약이 올바르지 않습니다: $requestedPath"
        }
        $backupPath = if ([IO.Path]::IsPathRooted($backupFileName)) {
            [IO.Path]::GetFullPath($backupFileName)
        } else {
            [IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $absoluteManifest) $backupFileName))
        }
        if (-not (Test-Path -LiteralPath $backupPath -PathType Leaf)) {
            throw "Backup artifact가 없습니다: manifest=$requestedPath"
        }
        if ((Get-CpfFileSha256 $backupPath) -ne $expectedHash) {
            throw "Backup artifact checksum이 일치하지 않습니다: manifest=$requestedPath"
        }
        [void]$covered.Add($database)
    }

    $requiredDatabases = @($Operations.target.databaseName | Select-Object -Unique)
    $missing = @($requiredDatabases | Where-Object { -not $covered.Contains([string]$_) })
    if ($missing.Count -gt 0) {
        throw "Backup manifest가 없는 physical DB가 있습니다: $($missing -join ', ')"
    }
}

function Protect-CpfOutput {
    param([string] $Text, [string[]] $Secrets)
    $safe = if ($null -eq $Text) { "" } else { $Text }
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $safe = $safe.Replace($secret, "****")
        }
    }
    return $safe
}

function Invoke-CpfSqlProcess {
    param(
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)] $Target,
        [Parameter(Mandatory = $true)][string] $Sql
    )

    $client = if (-not [string]::IsNullOrWhiteSpace([string]$Target.clientPath)) {
        [string]$Target.clientPath
    } else {
        @{ mariadb = "mariadb"; postgresql = "psql"; oracle = "sqlplus" }[$Vendor]
    }
    if (-not [IO.Path]::IsPathRooted($client)) {
        $command = Get-Command $client -ErrorAction SilentlyContinue
        if ($null -eq $command) { throw "DB client를 찾을 수 없습니다: vendor=$Vendor" }
        $client = $command.Source
    } elseif (-not (Test-Path -LiteralPath $client -PathType Leaf)) {
        throw "DB client를 찾을 수 없습니다: vendor=$Vendor"
    }

    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardInputEncoding = [Text.Encoding]::UTF8
    $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [Text.Encoding]::UTF8

    $inputSql = $Sql
    if ($Vendor -eq "mariadb") {
        foreach ($argument in @(
                "--host=$($Target.host)",
                "--port=$($Target.port)",
                "--user=$($Target.migrationUsername)",
                "--default-character-set=utf8mb4",
                "--binary-mode",
                "--database=$($Target.databaseName)"
            )) {
            [void]$psi.ArgumentList.Add($argument)
        }
        $psi.Environment["MYSQL_PWD"] = [string]$Target.migrationPassword
    } elseif ($Vendor -eq "postgresql") {
        foreach ($argument in @(
                "-X", "-q",
                "--set=ON_ERROR_STOP=1",
                "--host=$($Target.host)",
                "--port=$($Target.port)",
                "--username=$($Target.migrationUsername)",
                "--dbname=$($Target.databaseName)"
            )) {
            [void]$psi.ArgumentList.Add($argument)
        }
        $psi.Environment["PGPASSWORD"] = [string]$Target.migrationPassword
        $schema = Get-CpfPhysicalQualifier $Vendor $Target
        $inputSql = 'SET search_path TO "' + $schema.Replace('"', '""') + '";' + "`n" + $Sql
    } else {
        foreach ($argument in @("-L", "-S", "/nolog")) {
            [void]$psi.ArgumentList.Add($argument)
        }
        $schema = Get-CpfPhysicalQualifier $Vendor $Target
        $escapedPassword = ([string]$Target.migrationPassword).Replace('"', '""')
        $connect = 'CONNECT ' + $Target.migrationUsername + '/"' + $escapedPassword +
            '"@//' + $Target.host + ':' + $Target.port + '/' + $Target.databaseName
        $inputSql = "WHENEVER SQLERROR EXIT SQL.SQLCODE`n$connect`nALTER SESSION SET CURRENT_SCHEMA = $schema;`n$Sql`nEXIT`n"
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $psi
    try {
        if (-not $process.Start()) {
            throw "DB client process를 시작할 수 없습니다: vendor=$Vendor"
        }
        $process.StandardInput.Write($inputSql)
        $process.StandardInput.Close()
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            $safeError = Protect-CpfOutput (($stderr + "`n" + $stdout).Trim()) @(
                [string]$Target.migrationPassword
            )
            throw "DB migration 실행 실패: vendor=$Vendor module=$($Target.moduleKey) exit=$($process.ExitCode) output=$safeError"
        }
    } finally {
        $process.Dispose()
    }
}

$result = [ordered]@{
    schemaVersion = 1
    tool = "invoke-platform-database-migration.ps1"
    mode = if ($Apply) { "APPLY" } else { "DRY_RUN" }
    status = "미검증"
    generatedAt = (Get-Date).ToString("o")
    profile = ""
    plan = $null
    planSha256 = ""
    operations = @()
    error = ""
}

try {
    if ($DryRun -and $Apply) {
        throw "-DryRun과 -Apply를 동시에 지정할 수 없습니다."
    }
    if ($MigrationVersion.Count -gt 0 -and ($FromVersion -ge 0 -or $ToVersion -ge 0)) {
        throw "-MigrationVersion과 -FromVersion/-ToVersion을 동시에 지정할 수 없습니다."
    }

    $selectionMode = if ($MigrationVersion.Count -gt 0) { "explicit" } else { "range" }
    if ($selectionMode -eq "range") {
        if ($FromVersion -lt 0 -or $ToVersion -lt 0) {
            throw "자동 baseline/latest 추정은 금지됩니다. -MigrationVersion 또는 -FromVersion/-ToVersion을 명시하세요."
        }
        if ($Direction -eq "upgrade" -and $FromVersion -ge $ToVersion) {
            throw "Upgrade range는 FromVersion < ToVersion이어야 합니다."
        }
        if ($Direction -eq "rollback" -and $FromVersion -le $ToVersion) {
            throw "Rollback range는 FromVersion > ToVersion이어야 합니다."
        }
    } else {
        $invalidVersions = @($MigrationVersion | Where-Object { $_ -le 0 })
        if ($invalidVersions.Count -gt 0) {
            throw "MigrationVersion은 양의 정수여야 합니다."
        }
        if (@($MigrationVersion | Sort-Object -Unique).Count -ne $MigrationVersion.Count) {
            throw "MigrationVersion을 중복 지정할 수 없습니다."
        }
    }

    if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
        $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
    } elseif (-not [IO.Path]::IsPathRooted($ProfilePath)) {
        $ProfilePath = Join-Path $Root $ProfilePath
    }
    $ProfilePath = [IO.Path]::GetFullPath($ProfilePath)
    $profile = Get-CpfDatabaseProfile $ProfilePath
    $result.profile = [IO.Path]::GetFileName($ProfilePath)

    $moduleOrder = @($profile.modules.PSObject.Properties | ForEach-Object { [string]$_.Name })
    if ($moduleOrder.Count -eq 0) { throw "DB Profile modules가 비어 있습니다." }
    $staticProfiles = @{}
    foreach ($moduleKey in $moduleOrder) {
        $staticProfiles[$moduleKey] = ConvertTo-CpfModuleProfile $profile $moduleKey -SkipSecretResolution
    }
    $platformKeys = @($moduleOrder | Where-Object {
            $staticProfiles[$_].enabled -and
            $staticProfiles[$_].databaseLifecycle -eq "platform-pack"
        })
    if ($Modules.Count -gt 0) {
        $unknownModules = @($Modules | Where-Object { $_ -notin $moduleOrder })
        if ($unknownModules.Count -gt 0) {
            throw "Profile에 없는 Module key입니다: $($unknownModules -join ', ')"
        }
        $nonPlatformModules = @($Modules | Where-Object {
                -not $staticProfiles[$_].enabled -or
                $staticProfiles[$_].databaseLifecycle -ne "platform-pack"
            })
        if ($nonPlatformModules.Count -gt 0) {
            throw "Platform migration 대상으로 선택할 수 없는 Module입니다: $($nonPlatformModules -join ', ')"
        }
        $platformKeys = @($moduleOrder | Where-Object { $_ -in $Modules })
    }
    if ($platformKeys.Count -eq 0) {
        throw "선택된 enabled Platform Module이 없습니다."
    }

    $vendors = @($platformKeys | ForEach-Object { $staticProfiles[$_].vendor } | Sort-Object -Unique)
    if ($vendors.Count -ne 1) {
        throw "한 migration plan에는 단일 Vendor만 사용할 수 있습니다: $($vendors -join ', ')"
    }
    $vendor = Assert-CpfSupportedDatabaseVendor $vendors[0]
    $mariaRoutingManifest = $null
    if ($vendor -eq "mariadb") {
        $routingPath = Join-Path $Root "cpf-tools/db/canonical/mariadb-historical-migration-routing.json"
        if (-not (Test-Path -LiteralPath $routingPath -PathType Leaf)) {
            throw "MariaDB historical migration routing 정본이 없습니다: $(Get-CpfRelativePath $routingPath)"
        }
        $mariaRoutingManifest = Get-Content -LiteralPath $routingPath -Raw -Encoding UTF8 |
            ConvertFrom-Json -Depth 30
        if ([int]$mariaRoutingManifest.schemaVersion -ne 1 -or
            [string]$mariaRoutingManifest.contract -cne "CPF_MARIADB_HISTORICAL_MIGRATION_ROUTING" -or
            -not [bool]$mariaRoutingManifest.policy.historicalSqlImmutable -or
            -not [bool]$mariaRoutingManifest.policy.checksumRequired -or
            -not [bool]$mariaRoutingManifest.policy.implicitPrefixRoutingForbidden) {
            throw "MariaDB historical migration routing 정본 Header/Policy가 유효하지 않습니다."
        }
    }

    $vendorManifestPath = Join-Path $Root "cpf-tools/db/vendor-pack-manifest.json"
    $vendorManifest = Get-Content -LiteralPath $vendorManifestPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    $vendorEntry = $vendorManifest.vendors.$vendor
    if ($null -eq $vendorEntry -or $vendor -notin @($vendorManifest.supportedVendors)) {
        throw "Official Vendor migration lifecycle manifest가 없습니다: vendor=$vendor"
    }
    $migrationPattern = [string]$vendorEntry.lifecycle.migration
    $rollbackPattern = [string]$vendorEntry.lifecycle.rollback
    if ([string]::IsNullOrWhiteSpace($migrationPattern) -or
        [string]::IsNullOrWhiteSpace($rollbackPattern)) {
        throw "Vendor migration/rollback lifecycle path가 비어 있습니다: vendor=$vendor"
    }

    $targetByLogicalDatabase = @{}
    foreach ($moduleKey in $platformKeys) {
        $target = $staticProfiles[$moduleKey]
        $logicalKey = ([string]$target.logicalDatabase).ToLowerInvariant()
        if ([string]::IsNullOrWhiteSpace($logicalKey) -or
            $targetByLogicalDatabase.ContainsKey($logicalKey)) {
            throw "선택된 Module logicalDatabase가 비어 있거나 중복되었습니다: module=$moduleKey"
        }
        $targetByLogicalDatabase[$logicalKey] = $target
    }

    $availableVersions = [Collections.Generic.HashSet[int]]::new()
    if ($vendor -eq "mariadb") {
        $migrationDirectory = Resolve-CpfLifecyclePath $migrationPattern ""
        if (-not (Test-Path -LiteralPath $migrationDirectory -PathType Container)) {
            throw "Migration directory가 없습니다: $(Get-CpfRelativePath $migrationDirectory)"
        }
        foreach ($file in Get-ChildItem -LiteralPath $migrationDirectory -File -Filter "V*.sql") {
            [void]$availableVersions.Add((Get-CpfMigrationVersion $file.Name))
        }
    } else {
        foreach ($moduleKey in $platformKeys) {
            $target = $staticProfiles[$moduleKey]
            $migrationDirectory = Resolve-CpfLifecyclePath $migrationPattern $target.logicalDatabase
            if (-not (Test-Path -LiteralPath $migrationDirectory -PathType Container)) {
                throw "Migration directory가 없습니다: $(Get-CpfRelativePath $migrationDirectory)"
            }
            foreach ($file in Get-ChildItem -LiteralPath $migrationDirectory -File -Filter "V*.sql") {
                [void]$availableVersions.Add((Get-CpfMigrationVersion $file.Name))
            }
        }
    }

    if ($selectionMode -eq "explicit") {
        $selectedVersions = @($MigrationVersion | Sort-Object -Unique)
        $missingVersions = @($selectedVersions | Where-Object { -not $availableVersions.Contains($_) })
        if ($missingVersions.Count -gt 0) {
            throw "선택한 migration version이 Vendor pack에 없습니다: $($missingVersions -join ', ')"
        }
    } elseif ($Direction -eq "upgrade") {
        $selectedVersions = @($availableVersions | Where-Object {
                $_ -gt $FromVersion -and $_ -le $ToVersion
            } | Sort-Object)
    } else {
        $selectedVersions = @($availableVersions | Where-Object {
                $_ -le $FromVersion -and $_ -gt $ToVersion
            } | Sort-Object -Descending)
    }
    if ($selectedVersions.Count -eq 0) {
        throw "명시한 baseline/selection 범위에 migration이 없습니다."
    }
    if ($Direction -eq "rollback") {
        $selectedVersions = @($selectedVersions | Sort-Object -Descending)
    }

    $operations = [System.Collections.Generic.List[object]]::new()
    $order = 0
    if ($vendor -eq "mariadb") {
        $migrationDirectory = Resolve-CpfLifecyclePath $migrationPattern ""
        $rollbackDirectory = Resolve-CpfLifecyclePath $rollbackPattern ""
        $checksumMap = Get-CpfMigrationChecksumMap $migrationDirectory
        foreach ($version in $selectedVersions) {
            $migrationFile = Get-CpfVersionedMigrationFile $migrationDirectory $version $checksumMap
            $rollbackFile = Get-CpfVersionedRollbackFile $rollbackDirectory $version
            $migrationHash = Get-CpfFileSha256 $migrationFile.FullName
            $rollbackHash = Get-CpfFileSha256 $rollbackFile.FullName
            $migrationGroups = Get-CpfMariaSections `
                (Get-Content -LiteralPath $migrationFile.FullName -Raw -Encoding UTF8) `
                $targetByLogicalDatabase `
                (Get-CpfRelativePath $migrationFile.FullName) `
                (Get-CpfMariaRoutingEntry $mariaRoutingManifest $migrationFile.Name)
            $rollbackGroups = Get-CpfMariaSections `
                (Get-Content -LiteralPath $rollbackFile.FullName -Raw -Encoding UTF8) `
                $targetByLogicalDatabase `
                (Get-CpfRelativePath $rollbackFile.FullName) `
                (Get-CpfMariaRoutingEntry $mariaRoutingManifest $rollbackFile.Name)
            $migrationLogical = @($migrationGroups.logicalDatabase | ForEach-Object { $_.ToLowerInvariant() } | Sort-Object -Unique)
            $rollbackLogical = @($rollbackGroups.logicalDatabase | ForEach-Object { $_.ToLowerInvariant() } | Sort-Object -Unique)
            if (($migrationLogical -join ",") -ne ($rollbackLogical -join ",")) {
                throw "Migration/Rollback logical DB ownership이 다릅니다: version=$version migration=$($migrationLogical -join ',') rollback=$($rollbackLogical -join ',')"
            }
            foreach ($moduleKey in $platformKeys) {
                $target = $staticProfiles[$moduleKey]
                $logicalKey = $target.logicalDatabase.ToLowerInvariant()
                $migrationGroup = @($migrationGroups | Where-Object {
                        $_.logicalDatabase.Equals($target.logicalDatabase, [StringComparison]::OrdinalIgnoreCase)
                    })
                if ($migrationGroup.Count -eq 0) { continue }
                $rollbackGroup = @($rollbackGroups | Where-Object {
                        $_.logicalDatabase.Equals($target.logicalDatabase, [StringComparison]::OrdinalIgnoreCase)
                    })
                if ($rollbackGroup.Count -ne 1) {
                    throw "Rollback section ownership이 모호합니다: version=$version logicalDatabase=$logicalKey"
                }
                $selectedFile = if ($Direction -eq "upgrade") { $migrationFile } else { $rollbackFile }
                $selectedSql = if ($Direction -eq "upgrade") { $migrationGroup[0].sql } else { $rollbackGroup[0].sql }
                $order++
                $operations.Add([pscustomobject]@{
                        order = $order
                        version = $version
                        target = $target
                        migrationPath = Get-CpfRelativePath $migrationFile.FullName
                        migrationSha256 = $migrationHash
                        rollbackPath = Get-CpfRelativePath $rollbackFile.FullName
                        rollbackSha256 = $rollbackHash
                        selectedPath = Get-CpfRelativePath $selectedFile.FullName
                        selectedSha256 = if ($Direction -eq "upgrade") { $migrationHash } else { $rollbackHash }
                        renderedSha256 = Get-CpfSha256 $selectedSql
                        sql = $selectedSql
                    })
            }
        }
    } else {
        foreach ($version in $selectedVersions) {
            foreach ($moduleKey in $platformKeys) {
                $target = $staticProfiles[$moduleKey]
                $migrationDirectory = Resolve-CpfLifecyclePath $migrationPattern $target.logicalDatabase
                $rollbackDirectory = Resolve-CpfLifecyclePath $rollbackPattern $target.logicalDatabase
                $matches = @(Get-ChildItem -LiteralPath $migrationDirectory -File -Filter "V${version}__*.sql")
                if ($matches.Count -eq 0) { continue }
                $checksumMap = Get-CpfMigrationChecksumMap $migrationDirectory
                $migrationFile = Get-CpfVersionedMigrationFile $migrationDirectory $version $checksumMap
                $rollbackFile = Get-CpfVersionedRollbackFile $rollbackDirectory $version
                $migrationSql = Get-Content -LiteralPath $migrationFile.FullName -Raw -Encoding UTF8
                $rollbackSql = Get-Content -LiteralPath $rollbackFile.FullName -Raw -Encoding UTF8
                $physicalQualifier = Get-CpfPhysicalQualifier $vendor $target
                $migrationSql = Convert-CpfLogicalIdentifier $migrationSql $target.logicalDatabase $physicalQualifier
                $rollbackSql = Convert-CpfLogicalIdentifier $rollbackSql $target.logicalDatabase $physicalQualifier
                $selectedFile = if ($Direction -eq "upgrade") { $migrationFile } else { $rollbackFile }
                $selectedSql = if ($Direction -eq "upgrade") { $migrationSql } else { $rollbackSql }
                $migrationHash = Get-CpfFileSha256 $migrationFile.FullName
                $rollbackHash = Get-CpfFileSha256 $rollbackFile.FullName
                $order++
                $operations.Add([pscustomobject]@{
                        order = $order
                        version = $version
                        target = $target
                        migrationPath = Get-CpfRelativePath $migrationFile.FullName
                        migrationSha256 = $migrationHash
                        rollbackPath = Get-CpfRelativePath $rollbackFile.FullName
                        rollbackSha256 = $rollbackHash
                        selectedPath = Get-CpfRelativePath $selectedFile.FullName
                        selectedSha256 = if ($Direction -eq "upgrade") { $migrationHash } else { $rollbackHash }
                        renderedSha256 = Get-CpfSha256 $selectedSql
                        sql = $selectedSql
                    })
            }
        }
    }

    if ($operations.Count -eq 0) {
        throw "선택한 Platform Module에 적용할 migration operation이 없습니다."
    }

    $planPayload = Get-CpfPlanPayload $vendor @($operations) $selectionMode @($selectedVersions)
    $planJson = $planPayload | ConvertTo-Json -Depth 30 -Compress
    $planSha256 = Get-CpfSha256 $planJson
    $result.plan = $planPayload
    $result.planSha256 = $planSha256
    $result.operations = @($operations | ForEach-Object {
            [ordered]@{
                order = $_.order
                version = $_.version
                moduleKey = $_.target.moduleKey
                physicalDatabase = $_.target.databaseName
                status = "미검증"
            }
        })

    if ($Apply) {
        if (-not $ConfirmApply) {
            throw "Apply에는 -ConfirmApply가 필요합니다."
        }
        if (-not $ConfirmApplicationsStopped) {
            throw "Apply에는 -ConfirmApplicationsStopped가 필요합니다."
        }
        if (-not $ConfirmRollbackReady) {
            throw "Apply에는 -ConfirmRollbackReady가 필요합니다."
        }
        if ($ExpectedPlanSha256 -notmatch "^[0-9a-fA-F]{64}$" -or
            $ExpectedPlanSha256.ToLowerInvariant() -ne $planSha256) {
            throw "Dry-run에서 검토한 -ExpectedPlanSha256와 현재 plan이 일치해야 합니다. current=$planSha256"
        }
        Test-CpfBackupCoverage $vendor @($operations) $BackupManifestPath

        $runtimeProfiles = @{}
        foreach ($moduleKey in $platformKeys) {
            $runtimeProfiles[$moduleKey] = ConvertTo-CpfModuleProfile $profile $moduleKey
        }

        for ($index = 0; $index -lt $operations.Count; $index++) {
            $operation = $operations[$index]
            $target = $runtimeProfiles[$operation.target.moduleKey]
            Invoke-CpfSqlProcess $vendor $target $operation.sql
            $result.operations[$index].status = "완료"
        }
        $result.status = "완료"
    } else {
        $result.status = "미검증"
    }
} catch {
    $result.status = "실패"
    $result.error = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = (Get-Date).ToString("o")
    if ([string]::IsNullOrWhiteSpace($ResultPath)) {
        $ResultPath = Join-Path $Root "build/db-migration/platform-migration-result.sanitized.json"
    } elseif (-not [IO.Path]::IsPathRooted($ResultPath)) {
        $ResultPath = Join-Path $Root $ResultPath
    }
    $resultDirectory = Split-Path -Parent $ResultPath
    if (-not [string]::IsNullOrWhiteSpace($resultDirectory)) {
        [IO.Directory]::CreateDirectory($resultDirectory) | Out-Null
    }
    [IO.File]::WriteAllText(
        $ResultPath,
        ($result | ConvertTo-Json -Depth 40) + "`n",
        $Utf8NoBom)
    Write-Host "Sanitized migration result: $ResultPath"
}

if (-not $Apply) {
    Write-Host "CPF Platform DB migration dry-run PASS. planSha256=$($result.planSha256)"
    Write-Host "실제 DB는 변경하지 않았습니다."
} else {
    Write-Host "CPF Platform DB migration apply PASS. planSha256=$($result.planSha256)"
}
