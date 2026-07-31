param([string]$Root = '.')

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$original = Get-Location
$deletedFiles = 0
$deletedDirectories = 0

function Test-CpfProtectedDirectory([string]$Path, [string]$RootPath) {
    $normalized = [System.IO.Path]::GetFullPath($Path).TrimEnd([System.IO.Path]::DirectorySeparatorChar)
    $toolingSource = [System.IO.Path]::GetFullPath((Join-Path $RootPath 'cpf-tools/build')).TrimEnd([System.IO.Path]::DirectorySeparatorChar)
    if ($normalized -eq $toolingSource) { return $true }
    $relative = [System.IO.Path]::GetRelativePath($RootPath, $normalized).Replace('\','/')
    return $relative -match '(^|/)src/(main|test)/.*/build$'
}

try {
    $rootPath = (Resolve-Path $Root).Path
    Set-Location $rootPath

    $deleteManifest = Join-Path $rootPath 'cpf-docs/work/manifest/CPF_20260731_QA33_DELETE_PATHS.txt'
    if (Test-Path -LiteralPath $deleteManifest) {
        foreach ($line in Get-Content -LiteralPath $deleteManifest -Encoding UTF8) {
            $relative = $line.Trim()
            if (-not $relative -or $relative.StartsWith('#')) { continue }
            $target = [System.IO.Path]::GetFullPath((Join-Path $rootPath $relative))
            if (-not $target.StartsWith($rootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
                throw "Delete manifest path escapes project root: $relative"
            }
            if (Test-Path -LiteralPath $target) {
                $item = Get-Item -Force -LiteralPath $target
                Remove-Item -Recurse -Force -LiteralPath $target
                if ($item.PSIsContainer) { $deletedDirectories++ } else { $deletedFiles++ }
            }
        }
    }

    $garbageNames = @(
        'node_modules','dist','coverage','playwright-report','test-results','.gradle','.vite',
        '__pycache__','logs','tmp','temp','.cache','.pytest_cache','.mypy_cache'
    )
    $directories = Get-ChildItem -LiteralPath $rootPath -Recurse -Force -Directory -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notmatch '[\\/]\.git([\\/]|$)' -and
            ($_.Name -eq 'build' -or $garbageNames -contains $_.Name) -and
            -not (Test-CpfProtectedDirectory $_.FullName $rootPath)
        } |
        Sort-Object { $_.FullName.Length } -Descending
    foreach ($directory in $directories) {
        if (Test-Path -LiteralPath $directory.FullName) {
            Remove-Item -Recurse -Force -LiteralPath $directory.FullName -ErrorAction SilentlyContinue
            $deletedDirectories++
        }
    }

    $files = Get-ChildItem -LiteralPath $rootPath -Recurse -Force -File -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notmatch '[\\/]\.git([\\/]|$)' -and (
                $_.Name -match '\.(log|tmp|bak|orig|rej|patch|pyc|hprof|stackdump)$' -or
                $_.Name -match '^(hs_err_pid|replay_pid|npm-debug|yarn-debug|yarn-error)' -or
                $_.Name -match '^CPF_.*_(INTERMEDIATE|TEMP|WORKING).*\.zip$'
            )
        }
    foreach ($file in $files) {
        Remove-Item -Force -LiteralPath $file.FullName -ErrorAction SilentlyContinue
        $deletedFiles++
    }

    $emptyDirectories = Get-ChildItem -LiteralPath $rootPath -Recurse -Force -Directory -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notmatch '[\\/]\.git([\\/]|$)' -and
            -not (Test-CpfProtectedDirectory $_.FullName $rootPath) -and
            -not (Get-ChildItem -Force -LiteralPath $_.FullName -ErrorAction SilentlyContinue)
        } |
        Sort-Object { $_.FullName.Length } -Descending
    foreach ($directory in $emptyDirectories) {
        if (Test-Path -LiteralPath $directory.FullName) {
            Remove-Item -Force -LiteralPath $directory.FullName -ErrorAction SilentlyContinue
            $deletedDirectories++
        }
    }

    Write-Host "[CPF][QA33][CLEANUP] deletedFiles=$deletedFiles deletedDirectories=$deletedDirectories"
}
finally {
    Set-Location $original
}
