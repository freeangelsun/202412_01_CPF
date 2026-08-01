[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [switch]$Apply
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) {
    throw "Repository root not found: $RepoRoot"
}

Set-Location -LiteralPath $RepoRoot

$initialStatus = (& git -c core.quotepath=false status --porcelain=v1 --untracked-files=all 2>&1)
if ($LASTEXITCODE -ne 0) {
    throw "Unable to read Git status."
}

if ($Apply -and -not [string]::IsNullOrWhiteSpace(($initialStatus -join [Environment]::NewLine))) {
    throw "Working Tree must be clean before -Apply. This prevents deleting another worker's files."
}

$directoryNames = @(
    ".gradle",
    "node_modules",
    "dist",
    "coverage",
    "test-results",
    "playwright-report",
    "__pycache__",
    "build"
)

$filePatterns = @(
    "*.log",
    "*.tmp",
    "*.bak",
    "*.orig",
    "*.rej",
    "*.pyc",
    "npm-debug*",
    "yarn-error*",
    "hs_err_pid*"
)

function Get-RepoRelativePath {
    param([string]$AbsolutePath)
    return ([IO.Path]::GetRelativePath($RepoRoot, $AbsolutePath)).Replace("\", "/")
}

function Test-ContainsTrackedFile {
    param([string]$RelativePath)
    $result = & git ls-files -- "$RelativePath/**" "$RelativePath/*" 2>$null
    return ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace(($result -join [Environment]::NewLine)))
}

$candidates = @()
$skippedTracked = @()

Get-ChildItem -LiteralPath $RepoRoot -Recurse -Directory -Force -ErrorAction SilentlyContinue |
    Where-Object {
        $_.FullName -notlike "$RepoRoot\.git*" -and
        $_.Name -in $directoryNames
    } |
    Sort-Object FullName -Descending |
    ForEach-Object {
        $relative = Get-RepoRelativePath $_.FullName
        if (Test-ContainsTrackedFile $relative) {
            $skippedTracked += $relative
        } else {
            $candidates += [pscustomobject]@{
                type = "directory"
                relativePath = $relative
                fullPath = $_.FullName
            }
        }
    }

foreach ($pattern in $filePatterns) {
    Get-ChildItem -LiteralPath $RepoRoot -Recurse -File -Force -Filter $pattern -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notlike "$RepoRoot\.git*" } |
        ForEach-Object {
            $relative = Get-RepoRelativePath $_.FullName
            $tracked = & git ls-files --error-unmatch -- "$relative" 2>$null
            if ($LASTEXITCODE -eq 0) {
                $skippedTracked += $relative
            } else {
                $candidates += [pscustomobject]@{
                    type = "file"
                    relativePath = $relative
                    fullPath = $_.FullName
                }
            }
        }
}

$candidates = @($candidates | Sort-Object type, relativePath -Unique)
$skippedTracked = @($skippedTracked | Sort-Object -Unique)

Write-Host "Safe cleanup candidates: $($candidates.Count)"
$candidates | ForEach-Object { Write-Host "  [$($_.type)] $($_.relativePath)" }

if ($skippedTracked.Count -gt 0) {
    Write-Host "Protected because Git-tracked:"
    $skippedTracked | ForEach-Object { Write-Host "  $_" }
}

if (-not $Apply) {
    Write-Host "Preview only. Re-run with -Apply after reviewing the list."
    exit 0
}

foreach ($candidate in $candidates) {
    if ($PSCmdlet.ShouldProcess($candidate.relativePath, "Remove generated artifact")) {
        if ($candidate.type -eq "directory") {
            Remove-Item -LiteralPath $candidate.fullPath -Recurse -Force
        } else {
            Remove-Item -LiteralPath $candidate.fullPath -Force
        }
    }
}

$deletedTracked = & git -c core.quotepath=false diff --name-only --diff-filter=D 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Unable to verify tracked deletions after cleanup."
}

if (-not [string]::IsNullOrWhiteSpace(($deletedTracked -join [Environment]::NewLine))) {
    throw "Safety violation: cleanup produced tracked deletions.`n$($deletedTracked -join [Environment]::NewLine)"
}

Write-Host "Safe cleanup completed. No Git-tracked file was deleted."
