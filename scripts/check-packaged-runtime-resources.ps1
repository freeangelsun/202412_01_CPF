param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string[]] $Modules = @("MBR", "ADM", "BZA", "XYZ", "BAT"),
    [string] $ResultDir = "",
    [switch] $NoExitOnFailure
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

# bootJar 내부 리소스가 소스 기준과 다르게 남아 있으면 런타임에서만 오류가 난다.
# 그래서 jar와 nested jar를 직접 열어 포트 환경변수, 로그 경로, 금지 절대경로를 확인한다.
$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "packaged-runtime-resource-check.sanitized.json"
$selectedModules = Resolve-CpfRuntimeModules -Modules $Modules
$textExtensions = @(".xml", ".yml", ".yaml", ".properties")
$forbiddenPatterns = @("D:/logs", "D:\logs", "C:/logs", "C:\logs")
$result = [ordered]@{
    checkedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @()
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Read-ZipEntryText {
    param([System.IO.Compression.ZipArchiveEntry] $Entry)

    $stream = $Entry.Open()
    try {
        $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true)
        try {
            return $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    } finally {
        $stream.Dispose()
    }
}

function Add-ArchiveTextEntries {
    param(
        [System.IO.Compression.ZipArchive] $Archive,
        [string] $ArchiveName,
        [int] $NestedDepth,
        [System.Collections.Generic.List[object]] $Entries
    )

    foreach ($entry in $Archive.Entries) {
        $extension = [System.IO.Path]::GetExtension($entry.FullName).ToLowerInvariant()
        if ($textExtensions -contains $extension) {
            $Entries.Add([ordered]@{
                archive = $ArchiveName
                entry = $entry.FullName
                text = Read-ZipEntryText -Entry $entry
            })
            continue
        }

        if ($NestedDepth -lt 1 -and $entry.FullName -match 'BOOT-INF/lib/(pfw|cmn|mbr|adm|bza|xyz|bat)-.*\.jar$') {
            $memory = [System.IO.MemoryStream]::new()
            try {
                $entryStream = $entry.Open()
                try {
                    $entryStream.CopyTo($memory)
                } finally {
                    $entryStream.Dispose()
                }
                $memory.Position = 0
                $nestedArchive = [System.IO.Compression.ZipArchive]::new($memory, [System.IO.Compression.ZipArchiveMode]::Read, $true)
                try {
                    Add-ArchiveTextEntries `
                        -Archive $nestedArchive `
                        -ArchiveName ($ArchiveName + "!" + $entry.FullName) `
                        -NestedDepth ($NestedDepth + 1) `
                        -Entries $Entries
                } finally {
                    $nestedArchive.Dispose()
                }
            } finally {
                $memory.Dispose()
            }
        }
    }
}

function Get-PackagedTextEntries {
    param([string] $JarPath)

    $entries = [System.Collections.Generic.List[object]]::new()
    $archive = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        Add-ArchiveTextEntries -Archive $archive -ArchiveName (Split-Path -Leaf $JarPath) -NestedDepth 0 -Entries $entries
    } finally {
        $archive.Dispose()
    }
    return @($entries)
}

foreach ($module in $selectedModules) {
    $jarPath = Find-CpfRuntimeBootJar -Root $Root -Module $module
    $moduleResult = [ordered]@{
        module = $module.module
        jarExists = ($jarPath -ne $null)
        jar = $(if ($jarPath -ne $null) { Get-CpfRelativePath -Root $Root -Path $jarPath } else { $null })
        status = Get-CpfRuntimeStatusText "NotVerified"
        forbiddenMatches = @()
        markerChecks = @()
        scannedTextResourceCount = 0
        scannedResourceSamples = @()
    }

    if ($jarPath -eq $null) {
        $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
        $moduleResult.failureClassification = "implementation"
        $moduleResult.failureRootCause = "bootJar file was not found."
        $result.modules += $moduleResult
        continue
    }

    $entries = @(Get-PackagedTextEntries -JarPath $jarPath)
    $moduleResult.scannedTextResourceCount = $entries.Count
    $moduleResult.scannedResourceSamples = @($entries | Select-Object -First 20 | ForEach-Object { $_.archive + "!" + $_.entry })
    $combinedText = [string]::Join("`n", @($entries | ForEach-Object { [string] $_.text }))

    foreach ($pattern in $forbiddenPatterns) {
        foreach ($matchEntry in @($entries | Where-Object { ([string] $_.text).IndexOf($pattern, [System.StringComparison]::OrdinalIgnoreCase) -ge 0 })) {
            $moduleResult.forbiddenMatches += [ordered]@{
                pattern = $pattern
                archive = $matchEntry.archive
                entry = $matchEntry.entry
            }
        }
    }

    $requiredMarkers = @(
        [ordered]@{ name = "module-port-env"; text = $module.portEnv },
        [ordered]@{ name = "logging-root-contract"; text = "CPF_LOG_ROOT" },
        [ordered]@{ name = "logging-module-path"; text = "cpf.logging.runtime-module-path" }
    )

    foreach ($marker in $requiredMarkers) {
        $found = $combinedText.IndexOf([string] $marker.text, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        $moduleResult.markerChecks += [ordered]@{
            name = $marker.name
            text = $marker.text
            found = $found
        }
    }

    $missingMarkers = @($moduleResult.markerChecks | Where-Object { $_.found -ne $true })
    if ($moduleResult.forbiddenMatches.Count -eq 0 -and $missingMarkers.Count -eq 0) {
        $moduleResult.status = Get-CpfRuntimeStatusText "Done"
    } else {
        $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
        $moduleResult.failureClassification = "implementation"
        $moduleResult.failureRootCause = "packaged runtime resources contain forbidden path or missing required marker."
    }
    $result.modules += $moduleResult
}

$failed = @($result.modules | Where-Object { $_.status -ne (Get-CpfRuntimeStatusText "Done") })
$result.status = $(if ($failed.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
$result.finishedAt = (Get-Date).ToString("o")
Write-CpfRuntimeJson -Path $resultPath -Value $result

Write-Host "packaged runtime resource check finished. status=$($result.status) result=$resultPath"
if (-not $NoExitOnFailure -and $result.status -ne (Get-CpfRuntimeStatusText "Done")) {
    exit 1
}
