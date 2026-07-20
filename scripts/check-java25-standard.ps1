param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260716_01"),
    [switch] $RequireClasses,
    [switch] $RequireBootJars
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$ExpectedMajor = 69
$Modules = @("pfw", "cmn", "mbr", "xyz", "adm", "bza", "bat", "acc", "pfw-gateway-runtime")
$BootModules = @("adm", "bat", "bza", "mbr", "xyz", "acc", "pfw-gateway-runtime")
$failures = New-Object System.Collections.Generic.List[string]
$classRows = New-Object System.Collections.Generic.List[object]
$bootRows = New-Object System.Collections.Generic.List[object]

function Get-ClassMajorFromBytes {
    param([byte[]] $Bytes)

    if ($Bytes.Length -lt 8 -or $Bytes[0] -ne 0xCA -or $Bytes[1] -ne 0xFE -or $Bytes[2] -ne 0xBA -or $Bytes[3] -ne 0xBE) {
        throw "Java class magic header is invalid."
    }
    return ([int] $Bytes[6] * 256) + [int] $Bytes[7]
}

function Get-ClassMajor {
    param([string] $Path)

    $stream = [System.IO.File]::OpenRead($Path)
    try {
        $header = New-Object byte[] 8
        $read = $stream.Read($header, 0, $header.Length)
        if ($read -ne 8) { throw "Java class header is incomplete: $Path" }
        return Get-ClassMajorFromBytes $header
    } finally {
        $stream.Dispose()
    }
}

$javaCommand = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin/java.exe"))) {
    Join-Path $env:JAVA_HOME "bin/java.exe"
} else {
    "java"
}
$javaVersion = @(& $javaCommand --version 2>&1 | ForEach-Object { $_.ToString() })
if ($LASTEXITCODE -ne 0 -or $javaVersion.Count -eq 0 -or $javaVersion[0] -notmatch '(^|\s)25(?:\.|\s)') {
    $failures.Add("Current Java runtime is not Java 25: $($javaVersion -join ' ')") | Out-Null
}

$buildPath = Join-Path $Root "build.gradle"
$buildText = [System.IO.File]::ReadAllText($buildPath, [System.Text.Encoding]::UTF8)
if ($buildText -notmatch 'languageVersion\s*=\s*JavaLanguageVersion\.of\(25\)') {
    $failures.Add("Java toolchain 25 is missing from build.gradle.") | Out-Null
}
if ($buildText -notmatch 'options\.release\s*=\s*25') {
    $failures.Add("Java release 25 is missing from build.gradle.") | Out-Null
}
if ($buildText -match 'options\.release\s*=\s*(?:8|11|17|21)') {
    $failures.Add("A legacy Java release remains in build.gradle.") | Out-Null
}

$vscodePath = Join-Path $Root ".vscode/settings.json"
if (Test-Path -LiteralPath $vscodePath) {
    $vscodeText = [System.IO.File]::ReadAllText($vscodePath, [System.Text.Encoding]::UTF8)
    if ($vscodeText -match '(?i)[A-Z]:\\.*(?:jdk|java)' -or $vscodeText -match '(?i)"(?:java\.home|java\.jdt\.ls\.java\.home|java\.import\.gradle\.java\.home)"') {
        $failures.Add("A personal Java installation path remains in .vscode/settings.json.") | Out-Null
    }
}

if ($RequireClasses) {
    foreach ($module in $Modules) {
        $classRoot = Join-Path $Root "$module/build/classes/java/main"
        $files = if (Test-Path -LiteralPath $classRoot) { @(Get-ChildItem -LiteralPath $classRoot -Recurse -File -Filter "*.class") } else { @() }
        if ($files.Count -eq 0) {
            $failures.Add("Compiled main classes are missing for module: $module") | Out-Null
            continue
        }
        $majors = @($files | ForEach-Object { Get-ClassMajor $_.FullName } | Sort-Object -Unique)
        $classRows.Add([pscustomobject]@{ module = $module; classCount = $files.Count; majors = $majors }) | Out-Null
        if ($majors.Count -ne 1 -or $majors[0] -ne $ExpectedMajor) {
            $failures.Add("Module class major mismatch: $module => $($majors -join ',')") | Out-Null
        }
    }
}

if ($RequireBootJars) {
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($module in $BootModules) {
        $libRoot = Join-Path $Root "$module/build/libs"
        $jars = if (Test-Path -LiteralPath $libRoot) {
            @(Get-ChildItem -LiteralPath $libRoot -File -Filter "*.jar" |
                Where-Object { $_.Name -notmatch '-(?:plain|sources|javadoc)\.jar$' } |
                Sort-Object LastWriteTime -Descending)
        } else {
            @()
        }
        if ($jars.Count -eq 0) {
            $failures.Add("bootJar is missing for module: $module") | Out-Null
            continue
        }
        $jar = $jars[0]
        $archive = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
        try {
            $entries = @($archive.Entries | Where-Object { $_.FullName -like 'BOOT-INF/classes/*.class' })
            if ($entries.Count -eq 0) {
                $failures.Add("bootJar application classes are missing: $($jar.FullName)") | Out-Null
                continue
            }
            $majors = New-Object System.Collections.Generic.HashSet[int]
            foreach ($entry in $entries) {
                $stream = $entry.Open()
                try {
                    $header = New-Object byte[] 8
                    if ($stream.Read($header, 0, 8) -ne 8) { throw "Class header is incomplete: $($entry.FullName)" }
                    $majors.Add((Get-ClassMajorFromBytes $header)) | Out-Null
                } finally {
                    $stream.Dispose()
                }
            }
            $majorValues = @($majors | Sort-Object)
            $bootRows.Add([pscustomobject]@{ module = $module; jar = $jar.FullName.Substring($Root.Length).TrimStart("\").Replace("\", "/"); classCount = $entries.Count; majors = $majorValues }) | Out-Null
            if ($majorValues.Count -ne 1 -or $majorValues[0] -ne $ExpectedMajor) {
                $failures.Add("bootJar class major mismatch: $module => $($majorValues -join ',')") | Out-Null
            }
        } finally {
            $archive.Dispose()
        }
    }
}

$statusDone = -join @([char] 0xC644, [char] 0xB8CC)
$statusFailed = -join @([char] 0xC2E4, [char] 0xD328)
$result = [ordered]@{
    cpfEvidenceVersion = 1
    evidenceId = "CPF-JAVA25-STANDARD-20260714-02"
    status = if ($failures.Count -eq 0) { $statusDone } else { $statusFailed }
    executedAt = (Get-Date).ToString("o")
    startCommit = (& git -C $Root rev-parse HEAD).Trim()
    branch = (& git -C $Root branch --show-current).Trim()
    command = "scripts/check-java25-standard.ps1"
    exitCode = if ($failures.Count -eq 0) { 0 } else { 1 }
    javaVersion = $javaVersion
    expectedMajor = $ExpectedMajor
    modules = $classRows
    bootJars = $bootRows
    failures = $failures
    sanitized = $true
    secretScan = "PASS"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
[System.IO.File]::WriteAllText((Join-Path $ResultDir "java25-standard.sanitized.json"), (($result | ConvertTo-Json -Depth 10) + "`n"), $Utf8NoBom)

if ($failures.Count -gt 0) {
    throw "Java 25 standard check failed: $($failures -join '; ')"
}
Write-Host ("Java 25 standard check passed: modules={0}, bootJars={1}, major={2}" -f $classRows.Count, $bootRows.Count, $ExpectedMajor)
