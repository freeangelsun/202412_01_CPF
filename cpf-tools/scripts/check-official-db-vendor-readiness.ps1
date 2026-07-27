param([string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$official = @('mariadb','postgresql','oracle')
$manifestPath = Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json'
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $manifestPath | ConvertFrom-Json
$failures = [System.Collections.Generic.List[string]]::new()

if ((@($manifest.officialVendors) -join ',') -cne ($official -join ',')) {
    $failures.Add("officialVendors must be exactly: $($official -join ',')")
}
if (@($manifest.vendors.PSObject.Properties.Name | Where-Object { $_ -notin $official }).Count -gt 0) {
    $failures.Add('Unsupported DB vendor metadata remains in vendor-pack-manifest.json')
}

$requiredLifecycle = @('provision','emptyInstall','productSeed','optionalSampleSeed','testSeed','migration','verify','rollback')
foreach ($vendor in $official) {
    $entry = $manifest.vendors.$vendor
    if ($null -eq $entry) { $failures.Add("Official vendor entry missing: $vendor"); continue }
    $isSelectable = @($manifest.supportedVendors) -contains $vendor
    $ready = $true
    foreach ($name in $requiredLifecycle) {
        $prop = $entry.lifecycle.PSObject.Properties[$name]
        if ($null -eq $prop) { $ready = $false; continue }
        $relative = [string]$prop.Value
        if ($relative -match '\{logicalDatabase\}') {
            $rootRelative = $relative -replace '/\{logicalDatabase\}.*$',''
            $path = Join-Path $Root ($rootRelative -replace '/', [System.IO.Path]::DirectorySeparatorChar)
            if (-not (Test-Path -LiteralPath $path -PathType Container)) { $ready = $false }
            else {
                foreach ($db in @('cpfDB','cmnDB','admDB','bzaDB','batDB','mbrDB','accDB','refDB')) {
                    $dbDir = Join-Path $path $db
                    if (-not (Test-Path -LiteralPath $dbDir -PathType Container) -or @(Get-ChildItem -LiteralPath $dbDir -File -Filter '*.sql').Count -eq 0) { $ready = $false }
                }
            }
        } else {
            $path = Join-Path $Root ($relative -replace '/', [System.IO.Path]::DirectorySeparatorChar)
            if ($name -in @('migration','rollback')) {
                if (-not (Test-Path -LiteralPath $path -PathType Container) -or @(Get-ChildItem -LiteralPath $path -Recurse -File | Where-Object { $_.Name -ne '.gitkeep' }).Count -eq 0) { $ready = $false }
            } else {
                if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-Item $path).Length -le 1) { $ready = $false }
            }
        }
    }
    if ($isSelectable -and -not $ready) {
        $failures.Add("Selectable vendor has incomplete lifecycle: $vendor")
    }
    if (-not $isSelectable -and $ready) {
        $failures.Add("Lifecycle-ready official vendor is not promoted to supportedVendors: $vendor")
    }
}
foreach ($vendor in @('postgresql','oracle')) {
    $vendorRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor"
    foreach ($sql in Get-ChildItem -LiteralPath $vendorRoot -Recurse -File -Filter '*.sql') {
        $body = Get-Content -LiteralPath $sql.FullName -Raw -Encoding UTF8
        if ($body -match '(?im)^\s*USE\s+') { $failures.Add("$vendor contains USE directive: $($sql.FullName)") }
        if ($body -match '(?i)\b(cpfDB|cmnDB|admDB|bzaDB|batDB|mbrDB|accDB|refDB)\.') { $failures.Add("$vendor contains logical DB qualifier: $($sql.FullName)") }
    }
}
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "Official DB vendor readiness failed: $($failures.Count) issue(s)."
}
Write-Host "Official DB vendor readiness passed. official=$($official -join ',') selectable=$(@($manifest.supportedVendors) -join ',')"
