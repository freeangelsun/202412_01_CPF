param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $Vendor = "mariadb"
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$vendorRoot = Join-Path $Root "cpf-tools/db/vendor/$Vendor/source/migration/flyway"
if (-not (Test-Path -LiteralPath $vendorRoot -PathType Container)) {
    throw "Canonical migration directory가 없습니다: $vendorRoot"
}

$files = @(Get-ChildItem -LiteralPath $vendorRoot -File -Filter "V*.sql" |
    Sort-Object @{Expression={
        if ($_.BaseName -match '^V(\d+)__') { [int]$Matches[1] } else { [int]::MaxValue }
    }}, Name)
if ($files.Count -eq 0) { throw "Migration SQL이 없습니다: $vendorRoot" }

$lines = foreach ($file in $files) {
    $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash *$($file.Name)"
}
$target = Join-Path $vendorRoot "checksums.sha256"
[IO.File]::WriteAllLines($target, $lines, [Text.UTF8Encoding]::new($false))
Write-Host "CPF migration checksum synchronized. vendor=$Vendor migrations=$($files.Count) target=$target"
