param(
    [Parameter(Mandatory = $false)]
    [string]$RepositoryRoot = ".",
    [Parameter(Mandatory = $false)]
    [string]$ManifestPath = "cpf-docs/work/CPF_DELETE_MANIFEST.csv"
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$manifest = Join-Path $root $ManifestPath
if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
    throw "DELETE_MANIFEST_NOT_FOUND: $manifest"
}

$protectedPrefixes = @(
    "cpf-docs/deliverables/",
    "cpf-docs/guides/",
    "cpf-docs/assets/manuals/",
    "cpf-docs/assets/readme/",
    "cpf-docs/environment/docker/",
    "cpf-tools/environment/docker-development-test/"
)
$protectedExact = @(
    "cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md"
)

$rows = Import-Csv -LiteralPath $manifest
$seen = @{}
$deleted = 0
$alreadyAbsent = 0
foreach ($row in $rows) {
    $rel = ([string]$row.path).Replace("\", "/").TrimStart("./")
    $replacementRel = ([string]$row.replacement_path).Replace("\", "/").TrimStart("./")
    if ([string]::IsNullOrWhiteSpace($rel) -or [string]::IsNullOrWhiteSpace($replacementRel)) {
        throw "DELETE_MANIFEST_INVALID_ROW: path/replacement_path required"
    }
    if ($seen.ContainsKey($rel)) {
        throw "DELETE_MANIFEST_DUPLICATE_PATH: $rel"
    }
    $seen[$rel] = $true
    if ([System.IO.Path]::IsPathRooted($rel) -or $rel -match '(^|/)\.\.(/|$)' -or $rel.IndexOfAny([char[]]'*?[]') -ge 0) {
        throw "DELETE_MANIFEST_UNSAFE_PATH: $rel"
    }
    foreach ($prefix in $protectedPrefixes) {
        if ($rel.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"
        }
    }
    foreach ($exact in $protectedExact) {
        if ($rel.Equals($exact, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"
        }
    }
    if ([string]$row.protected_check -ne "PASS" -or [string]$row.delete_status -ne "PENDING_USER_APPROVAL") {
        throw "DELETE_MANIFEST_STATUS_INVALID: $rel"
    }

    if ([System.IO.Path]::IsPathRooted($replacementRel) -or $replacementRel -match '(^|/)\.\.(/|$)' -or $replacementRel.IndexOfAny([char[]]'*?[]') -ge 0) {
        throw "DELETE_MANIFEST_UNSAFE_REPLACEMENT_PATH: $replacementRel"
    }
    $replacement = Join-Path $root ($replacementRel.Replace("/", [System.IO.Path]::DirectorySeparatorChar))
    $replacementFull = [System.IO.Path]::GetFullPath($replacement)

    $target = Join-Path $root ($rel.Replace("/", [System.IO.Path]::DirectorySeparatorChar))
    $targetFull = [System.IO.Path]::GetFullPath($target)
    $rootPrefix = $root.TrimEnd([System.IO.Path]::DirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar
    if (-not $targetFull.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "DELETE_MANIFEST_PATH_ESCAPE: $rel"
    }
    if (-not $replacementFull.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "DELETE_MANIFEST_REPLACEMENT_PATH_ESCAPE: $replacementRel"
    }
    if (-not (Test-Path -LiteralPath $replacementFull -PathType Leaf)) {
        throw "DELETE_MANIFEST_REPLACEMENT_MISSING: $replacementRel"
    }
    if (Test-Path -LiteralPath $targetFull -PathType Container) {
        throw "DELETE_MANIFEST_DIRECTORY_DELETE_FORBIDDEN: $rel"
    }
    if (Test-Path -LiteralPath $targetFull -PathType Leaf) {
        Remove-Item -LiteralPath $targetFull -Force
        $deleted++
    } else {
        $alreadyAbsent++
    }
}

Write-Host ("CPF_DELETE_MANIFEST_APPLIED rows={0} deleted={1} alreadyAbsent={2}" -f $rows.Count, $deleted, $alreadyAbsent)
git status --short
