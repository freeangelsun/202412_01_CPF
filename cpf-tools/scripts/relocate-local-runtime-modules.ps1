param(
    [string] $ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $AllowDirtyLegacySource
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$runtimeRoot = Join-Path $ProjectRoot 'cpf-tools\runtime'
New-Item -ItemType Directory -Path $runtimeRoot -Force | Out-Null

function Get-FileSha256([string] $Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Assert-LegacySourceClean([string] $ModuleName) {
    if ($AllowDirtyLegacySource -or -not (Test-Path -LiteralPath (Join-Path $ProjectRoot '.git'))) { return }
    $changes = @(& git -C $ProjectRoot status --porcelain -- $ModuleName)
    if ($LASTEXITCODE -ne 0) { throw "Unable to inspect legacy runtime source status: $ModuleName" }
    if ($changes.Count -gt 0) {
        throw "Legacy runtime source contains local changes. Relocation aborted to prevent data loss: $ModuleName`n$($changes -join [Environment]::NewLine)"
    }
}

function Merge-MoveModule([string] $ModuleName) {
    $source = Join-Path $ProjectRoot $ModuleName
    $target = Join-Path $runtimeRoot $ModuleName

    if (-not (Test-Path -LiteralPath $source)) {
        if (-not (Test-Path -LiteralPath $target)) {
            throw "Local runtime module is missing from both source and target: $ModuleName"
        }
        Write-Host "[SKIP] already relocated: $ModuleName"
        return
    }

    Assert-LegacySourceClean $ModuleName
    New-Item -ItemType Directory -Path $target -Force | Out-Null
    $targetWins = [System.Collections.Generic.List[string]]::new()

    Get-ChildItem -LiteralPath $source -Recurse -Force | Sort-Object FullName | ForEach-Object {
        $relative = $_.FullName.Substring($source.Length).TrimStart('\\','/')
        $destination = Join-Path $target $relative
        if ($_.PSIsContainer) {
            New-Item -ItemType Directory -Path $destination -Force | Out-Null
            return
        }

        New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
        if (-not (Test-Path -LiteralPath $destination -PathType Leaf)) {
            Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
        } elseif ((Get-FileSha256 $_.FullName) -ne (Get-FileSha256 $destination)) {
            # The overlay target intentionally owns changed files. This is safe only because
            # the tracked legacy source was confirmed clean above.
            $targetWins.Add($relative)
        }
    }

    foreach ($required in @('build.gradle','src')) {
        if (-not (Test-Path -LiteralPath (Join-Path $target $required))) {
            throw "Relocation verification failed: $ModuleName/$required"
        }
    }

    Remove-Item -LiteralPath $source -Recurse -Force
    Write-Host "[MOVED] $ModuleName -> cpf-tools/runtime/$ModuleName (overlayOwnedDifferences=$($targetWins.Count))"
}

Merge-MoveModule 'cpf-local-runtime'
Merge-MoveModule 'cpf-local-batch-runtime'

$settings = Join-Path $ProjectRoot 'settings.gradle'
if (-not (Test-Path -LiteralPath $settings -PathType Leaf)) {
    throw "settings.gradle is missing: $settings"
}
$settingsText = Get-Content -LiteralPath $settings -Raw -Encoding UTF8
foreach ($path in @('cpf-tools/runtime/cpf-local-runtime','cpf-tools/runtime/cpf-local-batch-runtime')) {
    if ($settingsText -notmatch [regex]::Escape($path)) {
        throw "settings.gradle does not reference relocated module path: $path"
    }
}
foreach ($legacy in @('cpf-local-runtime','cpf-local-batch-runtime')) {
    if (Test-Path -LiteralPath (Join-Path $ProjectRoot $legacy)) {
        throw "Legacy runtime root still exists after relocation: $legacy"
    }
}
Write-Host '[PASS] CPF local runtime physical modules relocated under cpf-tools/runtime.'
