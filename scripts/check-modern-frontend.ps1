param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

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
    admVueSfc = Test-Path -LiteralPath (Join-Path $Root 'cpf-admin/frontend/src/App.vue')
    admTypeScriptFeatures = @(Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-admin/frontend/src/features') -Recurse -File -Filter '*.ts').Count -ge 6
    admPackageLock = Test-Path -LiteralPath (Join-Path $Root 'cpf-admin/frontend/package-lock.json')
    bzaVueSfc = Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/frontend/src/App.vue')
    bzaTypeScriptFeatures = Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/frontend/src/features/console.ts')
    bzaPackageLock = Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/frontend/package-lock.json')
    legacyAdmScriptRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'cpf-admin/src/main/resources/static/adm/adm.js'))
    legacyBzaScriptRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/src/main/resources/static/bza/bza.js'))
    globalVueRemoved = -not (Test-Path -LiteralPath (Join-Path $Root 'cpf-admin/src/main/resources/static/adm/vendor/vue.global.prod.js'))
    admProductionBuild = Test-Path -LiteralPath (Join-Path $Root 'cpf-admin/build/generated/frontend/static/adm/index.html')
    bzaProductionBuild = Test-Path -LiteralPath (Join-Path $Root 'cpf-biz-admin/build/generated/frontend/static/bza/index.html')
}

$archives = [ordered]@{}
foreach ($moduleInfo in @(
        [ordered]@{ project = 'cpf-admin'; staticPath = 'adm' },
        [ordered]@{ project = 'cpf-biz-admin'; staticPath = 'bza' }
    )) {
    $module = $moduleInfo.staticPath
    foreach ($extension in @('jar', 'war')) {
        $archive = Get-ChildItem -LiteralPath (Join-Path $Root "$($moduleInfo.project)/build/libs") -File -Filter "*.$extension" |
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
