param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $Quiet
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
$syncScript = Join-Path $Root "cpf-tools\scripts\sync-bat-runtime-query-pack.ps1"
& $syncScript -Root $Root -Check | Out-Null

$contractPath = Join-Path $Root "cpf-tools\db\metadata\bat-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$vendors = @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")
$keys = @($contract.statements | ForEach-Object { [string] $_.key } | Sort-Object)
$failures = [System.Collections.Generic.List[string]]::new()
$inlineSqlPattern = '(?is)(?:"""|")\s*(?:SELECT|INSERT|UPDATE|DELETE|MERGE)\b'

foreach ($vendor in $vendors) {
    $repositoryRoot = Join-Path $Root "cpf-tools\db\vendor\$vendor\runtime\bat\repository"
    $actualKeys = @(
        Get-ChildItem -LiteralPath $repositoryRoot -File -Filter "*.sql" |
            ForEach-Object { $_.BaseName } |
            Sort-Object
    )
    if (($actualKeys -join "`n") -cne ($keys -join "`n")) {
        $failures.Add("Vendor statement key parity mismatch: $vendor")
    }
    foreach ($key in $keys) {
        $path = Join-Path $repositoryRoot "$key.sql"
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        $sql = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        if ($sql -match "@[A-Z0-9_]+@") {
            $failures.Add("Unresolved token: vendor=$vendor key=$key")
        }
        if ($vendor -notin @("mariadb", "mysql") -and $sql -match "(?i)\bON\s+DUPLICATE\s+KEY\b") {
            $failures.Add("MySQL-family UPSERT leaked: vendor=$vendor key=$key")
        }
        if ($vendor -eq "sqlserver" -and $sql -match "(?i)\bCURRENT_TIMESTAMP\s*\(") {
            $failures.Add("SQL Server CURRENT_TIMESTAMP precision syntax leaked: key=$key")
        }
        if ($vendor -in @("oracle", "sqlserver") -and $sql -match "(?i)\bLIMIT\s+\d+\b") {
            $failures.Add("LIMIT syntax leaked: vendor=$vendor key=$key")
        }
        if ($vendor -eq "oracle" -and $sql -match "(?i)\bON\s+CONFLICT\b") {
            $failures.Add("PostgreSQL UPSERT syntax leaked: vendor=$vendor key=$key")
        }
    }
}

foreach ($scope in @($contract.migrationScope)) {
    $sourceRoot = Join-Path $Root ($scope -replace "/", "\")
    foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java") {
        $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        if ($text -match $inlineSqlPattern) {
            $relative = $file.FullName.Substring($Root.Length + 1).Replace("\", "/")
            $failures.Add("BAT Java inline SQL remains: $relative")
        }
    }
}

$remainingInlineFiles = [System.Collections.Generic.List[object]]::new()
$remainingInlineStatements = 0
foreach ($scope in @($contract.remainingScope)) {
    $sourceRoot = Join-Path $Root ($scope -replace "/", "\")
    foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java") {
        $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $count = ([regex]::Matches($text, $inlineSqlPattern)).Count
        if ($count -eq 0) { continue }
        $relative = $file.FullName.Substring($Root.Length + 1).Replace("\", "/")
        $remainingInlineFiles.Add([ordered]@{ path = $relative; statements = $count })
        $remainingInlineStatements += $count
    }
}

foreach ($vendor in $vendors) {
    $packPath = Join-Path $Root "cpf-tools\db\vendor\$vendor\pack.json"
    $pack = Get-Content -Raw -Encoding UTF8 -LiteralPath $packPath | ConvertFrom-Json
    $descriptor = $pack.runtimeModules.bat
    if ($null -eq $descriptor -or [string] $descriptor.ownerArtifact -cne "cpf-batch") {
        $failures.Add("pack.json BAT Runtime ownership is missing: vendor=$vendor")
    }
}

$result = [ordered]@{
    status = if ($failures.Count -eq 0) { "PASS" } else { "FAIL" }
    contractStatus = [string] $contract.status
    migratedScopes = @($contract.migrationScope)
    remainingScopes = @($contract.remainingScope)
    statements = $keys.Count
    vendors = $vendors.Count
    remainingInlineSqlStatements = $remainingInlineStatements
    remainingInlineSqlFiles = @($remainingInlineFiles)
    failures = @($failures)
}
if (-not $Quiet) {
    $result | ConvertTo-Json -Depth 10
}
if ($failures.Count -gt 0) {
    throw "BAT Runtime Query Pack validation failed ($($failures.Count)): $($failures -join ' | ')"
}
