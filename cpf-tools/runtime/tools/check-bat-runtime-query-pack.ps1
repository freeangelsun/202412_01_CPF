param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $Quiet
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
$syncScript = Join-Path $Root "cpf-tools\runtime\tools\sync-bat-runtime-query-pack.ps1"
& $syncScript -Root $Root -Check | Out-Null

$contractPath = Join-Path $Root "cpf-tools\db\metadata\bat-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$vendors = @("mariadb", "postgresql", "oracle")
$keys = @($contract.statements | ForEach-Object { [string] $_.key } | Sort-Object)
$failures = [System.Collections.Generic.List[string]]::new()
$inlineSqlPattern = '(?is)(?:"""|")\s*(?:(?:SELECT|INSERT|UPDATE|MERGE)\b|DELETE\s+FROM\b)'
$expectedMigrationScopes = @(
    "cpf-batch/worker/src/main/java",
    "cpf-batch/scheduler/src/main/java",
    "cpf-batch/center-cut/src/main/java",
    "cpf-batch/control-plane/src/main/java",
    "cpf-batch/runtime/src/main/java"
)

function ConvertTo-BatScopeArray {
    param([object] $Value)

    $scopes = [System.Collections.Generic.List[string]]::new()
    if ($null -eq $Value) {
        return @()
    }
    foreach ($item in @($Value)) {
        $scope = ([string] $item).Trim().Replace("\", "/").TrimEnd("/")
        if (-not [string]::IsNullOrWhiteSpace($scope)) {
            $scopes.Add($scope)
        }
    }
    return @($scopes)
}

$migrationScopes = @(ConvertTo-BatScopeArray -Value $contract.migrationScope)
$remainingScopes = @(ConvertTo-BatScopeArray -Value $contract.remainingScope)
if (($migrationScopes -join "`n") -cne ($expectedMigrationScopes -join "`n")) {
    $failures.Add("BAT migrationScope must contain only the five owned main Java roots.")
}
foreach ($scope in $remainingScopes) {
    if ($expectedMigrationScopes -cnotcontains $scope) {
        $failures.Add("BAT remainingScope is outside BAT-owned main Java roots: $scope")
    }
    if ($migrationScopes -ccontains $scope) {
        $failures.Add("BAT scope cannot be both migrated and remaining: $scope")
    }
}

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
        if ($vendor -ne "mariadb" -and $sql -match "(?i)\bON\s+DUPLICATE\s+KEY\b") {
            $failures.Add("MariaDB UPSERT leaked: vendor=$vendor key=$key")
        }
        if ($vendor -eq "oracle" -and $sql -match "(?i)\bLIMIT\s+\d+\b") {
            $failures.Add("LIMIT syntax leaked: vendor=$vendor key=$key")
        }
        if ($vendor -eq "oracle" -and $sql -match "(?i)\bON\s+CONFLICT\b") {
            $failures.Add("PostgreSQL UPSERT syntax leaked: vendor=$vendor key=$key")
        }
    }
}

foreach ($scope in $migrationScopes) {
    if ($expectedMigrationScopes -cnotcontains $scope) { continue }
    $sourceRoot = Join-Path $Root ($scope -replace "/", "\")
    if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
        $failures.Add("BAT migration scope is missing: $scope")
        continue
    }
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
foreach ($scope in $remainingScopes) {
    if ($expectedMigrationScopes -cnotcontains $scope) { continue }
    $sourceRoot = Join-Path $Root ($scope -replace "/", "\")
    if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
        $failures.Add("BAT remaining scope is missing: $scope")
        continue
    }
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
    migratedScopes = $migrationScopes
    remainingScopes = $remainingScopes
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
