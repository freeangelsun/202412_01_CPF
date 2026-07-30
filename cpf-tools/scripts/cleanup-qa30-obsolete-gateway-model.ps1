param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,
    [switch] $WhatIf
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$obsolete = @(
    'cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRoute.java',
    'cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java',
    'cpf-core/src/test/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalogTest.java'
)

$sourceRoot = Join-Path $Root 'cpf-core/src'
if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
    throw "CPF Core source root not found: $sourceRoot"
}

$targetPaths = @($obsolete | ForEach-Object { [IO.Path]::GetFullPath((Join-Path $Root $_)) })
$remainingReferences = [Collections.Generic.List[string]]::new()
$scanExtensions = @('.java', '.kt', '.groovy', '.xml', '.gradle')

Get-ChildItem -LiteralPath $sourceRoot -Recurse -File | Where-Object {
    $scanExtensions -contains $_.Extension.ToLowerInvariant() -and
    $targetPaths -notcontains $_.FullName
} | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
    $isCommonGatewayPackage = $_.FullName.Replace('\\', '/') -match '/com/cpf/core/common/gateway/'
    $legacyRouteReference = $text -match 'com\.cpf\.core\.common\.gateway\.CpfGatewayRoute\b' -or
        ($isCommonGatewayPackage -and $text -match '\bCpfGatewayRoute\b')
    $legacyCatalogReference = $text -match '\bCpfGatewayRouteCatalog\b'
    if ($legacyRouteReference -or $legacyCatalogReference) {
        $relative = [IO.Path]::GetRelativePath($Root, $_.FullName).Replace('\\', '/')
        $remainingReferences.Add($relative)
    }
}

if ($remainingReferences.Count -gt 0) {
    throw "Legacy Gateway model still has consumers; cleanup aborted: $($remainingReferences -join ', ')"
}

foreach ($relative in $obsolete) {
    $path = Join-Path $Root $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Write-Host "Already absent: $relative"
        continue
    }
    if ($WhatIf) {
        Write-Host "[WhatIf] Remove: $relative"
    } else {
        Remove-Item -LiteralPath $path -Force
        Write-Host "Removed: $relative"
    }
}

if (-not $WhatIf) {
    foreach ($relative in $obsolete) {
        if (Test-Path -LiteralPath (Join-Path $Root $relative)) {
            throw "Obsolete Gateway file remains after cleanup: $relative"
        }
    }
}

Write-Host 'CPF QA30 obsolete Gateway model cleanup PASS'
