param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

function Test-ArchiveEntry([string] $ArchivePath, [string] $EntryPattern) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead($ArchivePath)
    try {
        return @($archive.Entries | Where-Object { $_.FullName -match $EntryPattern }).Count -gt 0
    } finally {
        $archive.Dispose()
    }
}

$checks = [ordered]@{
    admVueSfc = Test-Path -LiteralPath (Join-Path $Root 'adm/frontend/src/App.vue')
    admTypeScriptFeatures = @(Get-ChildItem -LiteralPath (Join-Path $Root 'adm/frontend/src/features') -Recurse -File -Filter '*.ts').Count -ge 6
    admPackageLock = Test-Path -LiteralPath (Join-Path $Root 'adm/frontend/package-lock.json')
    bzaVueSfc = Test-Path -LiteralPath (Join-Path $Root 'bza/frontend/src/App.vue')
    bzaTypeScriptFeatures = Test-Path -LiteralPath (Join-Path $Root 'bza/frontend/src/features/console.ts')
    bzaPackageLock = Test-Path -LiteralPath (Join-Path $Root 'bza/frontend/package-lock.json')
    legacyAdmScriptRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'adm/src/main/resources/static/adm/adm.js'))
    legacyBzaScriptRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'bza/src/main/resources/static/bza/bza.js'))
    globalVueRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'adm/src/main/resources/static/adm/vendor/vue.global.prod.js'))
    admProductionBuild = Test-Path -LiteralPath (Join-Path $Root 'adm/build/generated/frontend/static/adm/index.html')
    bzaProductionBuild = Test-Path -LiteralPath (Join-Path $Root 'bza/build/generated/frontend/static/bza/index.html')
}

$archives = [ordered]@{}
foreach ($module in @('adm', 'bza')) {
    foreach ($extension in @('jar', 'war')) {
        $archive = Get-ChildItem -LiteralPath (Join-Path $Root "$module/build/libs") -File -Filter "*.$extension" |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        $key = "$module-$extension"
        if ($null -eq $archive) {
            $archives[$key] = [ordered]@{ exists = $false; frontendIndex = $false; hashedAsset = $false }
            continue
        }
        $prefix = $(if ($extension -eq 'jar') { 'BOOT-INF/classes' } else { 'WEB-INF/classes' })
        $archives[$key] = [ordered]@{
            exists = $true
            frontendIndex = Test-ArchiveEntry $archive.FullName "^$prefix/static/$module/index\.html$"
            hashedAsset = Test-ArchiveEntry $archive.FullName "^$prefix/static/$module/assets/index-.+\.(js|css)$"
        }
    }
}

$failedChecks = @($checks.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
$failedArchives = @($archives.GetEnumerator() | Where-Object {
    -not $_.Value.exists -or -not $_.Value.frontendIndex -or -not $_.Value.hashedAsset
} | ForEach-Object Key)
$result = [ordered]@{
    checkedAt = [DateTimeOffset]::Now.ToString('o')
    status = $(if ($failedChecks.Count -eq 0 -and $failedArchives.Count -eq 0) { 'DONE' } else { 'FAILED' })
    checks = $checks
    archives = $archives
    failedChecks = $failedChecks
    failedArchives = $failedArchives
}
$resultPath = Join-Path $ResultDir 'modern-frontend-result.sanitized.json'
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10), $Utf8NoBom)
if ($result.status -ne 'DONE') {
    throw "현대화 frontend 검증 실패: $($failedChecks + $failedArchives -join ', ')"
}
Write-Host "현대화 frontend 검증 통과: $resultPath"
