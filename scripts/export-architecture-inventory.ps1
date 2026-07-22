param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$modules = @(
    'cpf-core', 'cpf-common', 'cpf-member', 'cpf-admin', 'cpf-biz-admin',
    'cpf-batch', 'cpf-account', 'cpf-reference', 'cpf-external', 'cpf-gateway'
)
$trackedExtensions = @('.java', '.kt', '.groovy', '.xml', '.yml', '.yaml', '.properties', '.sql', '.json', '.js', '.css', '.html', '.md', '.ps1', '.gradle')
$items = [System.Collections.Generic.List[object]]::new()
$failures = [System.Collections.Generic.List[object]]::new()

function Get-RelativePath([string] $Path) {
    if (-not $Path.StartsWith($Root, [StringComparison]::OrdinalIgnoreCase)) {
        throw "저장소 외부 경로는 인벤토리에 포함할 수 없습니다. path=$Path"
    }
    $Path.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
}

function Resolve-ArtifactType([string] $RelativePath) {
    if ($RelativePath -match '/src/test/') { return 'TEST' }
    if ($RelativePath -match '/src/main/java/') { return 'SOURCE' }
    if ($RelativePath -match '/src/main/resources/') { return 'RESOURCE' }
    if ($RelativePath -match '(^|/)sql/') { return 'SQL' }
    if ($RelativePath -match '(^|/)deploy/') { return 'DEPLOY' }
    if ($RelativePath -match '(^|/)manifest/') { return 'MANIFEST' }
    if ($RelativePath -match '(^|/)scripts/') { return 'SCRIPT' }
    if ($RelativePath -match '(^|/)specs/') { return 'SPEC' }
    return 'BUILD_OR_DOCUMENT'
}

function Resolve-Capability([string] $RelativePath, [string] $Module) {
    $marker = if ($RelativePath -match '/src/(main|test)/java/') { '/java/' } else { '' }
    if ($marker) {
        $after = $RelativePath.Substring($RelativePath.IndexOf($marker) + $marker.Length)
        $segments = $after.Split('/')
        if ($segments.Count -gt 3) { return $segments[3] }
    }
    if ($RelativePath -match '/mybatis/mapper/[^/]+/([^/]+)/') { return $Matches[1] }
    return $Module
}

function Find-TestPath([string] $RelativePath, [string] $Module) {
    if ($RelativePath -notmatch '/src/main/java/(.+)\.java$') { return '' }
    $candidate = "$Module/src/test/java/$($Matches[1])Test.java"
    if (Test-Path -LiteralPath (Join-Path $Root $candidate) -PathType Leaf) { return $candidate }
    return ''
}

function Add-InventoryFile([IO.FileInfo] $File, [string] $Module) {
    $relative = Get-RelativePath $File.FullName
    $packageName = ''
    $visibility = 'INTERNAL'
    $className = $File.BaseName
    if ($File.Extension -eq '.java') {
        $text = [IO.File]::ReadAllText($File.FullName, [Text.Encoding]::UTF8)
        if ($text -match '(?m)^package\s+([a-zA-Z0-9_.]+);') { $packageName = $Matches[1] }
        if ($text -match '(?m)^public\s+(?:final\s+|sealed\s+|abstract\s+)?(?:class|interface|record|enum|@interface)\s+') {
            $visibility = 'PUBLIC'
        }
        if ($relative -match '/src/(?:main|test)/java/(.+)/[^/]+\.java$') {
            $expected = $Matches[1].Replace('/', '.')
            if ($packageName -ne $expected) {
                $failures.Add([ordered]@{ path = $relative; reason = "package 선언과 경로가 일치하지 않습니다."; package = $packageName; expected = $expected }) | Out-Null
            }
        }
    }
    $items.Add([ordered]@{
            module = $Module
            path = $relative
            package = $packageName
            file = $File.Name
            artifactType = Resolve-ArtifactType $relative
            capability = Resolve-Capability $relative $Module
            owner = $Module.ToUpperInvariant()
            visibility = $visibility
            consumer = if ($visibility -eq 'PUBLIC') { '모듈 API 소비자' } else { '모듈 내부' }
            test = Find-TestPath $relative $Module
            sqlOrConfig = if ($relative -match '\.(sql|xml|yml|yaml|properties)$') { $relative } else { '' }
            disposition = '유지'
            reason = '현재 모듈 소유권과 경로 기준에 포함됨'
        }) | Out-Null
}

foreach ($module in $modules) {
    $modulePath = Join-Path $Root $module
    if (-not (Test-Path -LiteralPath $modulePath -PathType Container)) {
        $failures.Add([ordered]@{ path = $module; reason = '필수 모듈 디렉터리가 없습니다.' }) | Out-Null
        continue
    }
    Get-ChildItem -LiteralPath $modulePath -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\build\\|\\bin\\|\\out\\|\\target\\|\\logs\\' -and
        $trackedExtensions -contains $_.Extension.ToLowerInvariant()
    } | ForEach-Object { Add-InventoryFile $_ $module }
}

foreach ($rootArea in @('scripts', 'specs/sql', 'deploy')) {
    $areaPath = Join-Path $Root $rootArea
    if (-not (Test-Path -LiteralPath $areaPath -PathType Container)) { continue }
    Get-ChildItem -LiteralPath $areaPath -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\evidence\\' -and $trackedExtensions -contains $_.Extension.ToLowerInvariant()
    } | ForEach-Object { Add-InventoryFile $_ 'ROOT' }
}

$moduleSummary = @($items | Group-Object module | Sort-Object Name | ForEach-Object {
        [ordered]@{ module = $_.Name; fileCount = $_.Count }
    })
$result = [ordered]@{
    generatedAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss.fffK')
    status = if ($failures.Count -eq 0) { 'DONE' } else { 'FAILED' }
    root = '.'
    moduleCount = $modules.Count
    fileCount = $items.Count
    failureCount = $failures.Count
    moduleSummary = $moduleSummary
    failures = @($failures)
    items = @($items)
}

$jsonPath = Join-Path $ResultDir 'architecture-inventory.sanitized.json'
[IO.File]::WriteAllText($jsonPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)
$csvPath = Join-Path $ResultDir 'architecture-inventory.sanitized.csv'
$csvText = (($items | ConvertTo-Csv -NoTypeInformation) -join [Environment]::NewLine) + [Environment]::NewLine
[IO.File]::WriteAllText($csvPath, $csvText, $Utf8NoBom)

if ($failures.Count -gt 0) {
    throw "아키텍처 인벤토리 검사가 실패했습니다. failureCount=$($failures.Count)"
}
Write-Host "CPF 아키텍처 인벤토리 생성 완료: $($items.Count)개 파일"
