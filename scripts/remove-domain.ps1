param(
    [Parameter(Mandatory = $true)]
    [string] $ModuleCode,
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$module = $ModuleCode.Trim().ToLowerInvariant()
if ($module -notmatch '^[a-z][a-z0-9]{1,9}$') {
    throw "ModuleCode는 영문으로 시작하는 2~10자리 영문·숫자여야 합니다."
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/remove-domain/$module"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "remove-domain-result.json"
$moduleDir = Join-Path $Root $module
$ownershipPath = Join-Path $moduleDir "manifest/generator-ownership.json"

function Write-Result {
    param([object] $Value)
    [System.IO.File]::WriteAllText(
            $resultPath,
            ($Value | ConvertTo-Json -Depth 30),
            $Utf8NoBom)
}

function Get-RelativePath {
    param([string] $Path)
    return $Path.Substring($Root.Length + 1).Replace('\', '/')
}

if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
    $result = [ordered]@{
        generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
        status = "FAILED"
        dryRun = [bool] $DryRun
        moduleCode = $module.ToUpperInvariant()
        reason = "생성기 소유권 manifest가 없어 안전한 자동 제거가 불가능합니다."
        manifest = Get-RelativePath $ownershipPath
    }
    Write-Result $result
    throw $result.reason
}

$ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($ownership.moduleCode -ne $module.ToUpperInvariant()) {
    throw "요청 모듈과 소유권 manifest의 moduleCode가 다릅니다."
}

$ownedPaths = New-Object System.Collections.Generic.HashSet[string]([System.StringComparer]::OrdinalIgnoreCase)
$changedFiles = New-Object System.Collections.Generic.List[object]
$missingFiles = New-Object System.Collections.Generic.List[string]
foreach ($ownedFile in @($ownership.createdFiles)) {
    $relative = ([string] $ownedFile.path).Replace('\', '/')
    [void] $ownedPaths.Add($relative)
    $absolute = Join-Path $moduleDir $relative
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        $missingFiles.Add("$module/$relative")
        continue
    }
    $currentHash = (Get-FileHash -LiteralPath $absolute -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($currentHash -ne ([string] $ownedFile.sha256).ToLowerInvariant()) {
        $changedFiles.Add([ordered]@{
            path = "$module/$relative"
            generatedSha256 = $ownedFile.sha256
            currentSha256 = $currentHash
        })
    }
}

$userOwnedFiles = @(Get-ChildItem -LiteralPath $moduleDir -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\build\\' -and
        $_.FullName -ne $ownershipPath -and
        -not $ownedPaths.Contains($_.FullName.Substring($moduleDir.Length + 1).Replace('\', '/'))
    } | ForEach-Object { Get-RelativePath $_.FullName })

# 다른 모듈·배포·SQL이 제거 대상 모듈을 참조하면 자동 제거를 차단합니다.
$escapedReferenceModule = [regex]::Escape($module)
$referencePatterns = @(
    "cpf\.$escapedReferenceModule\.",
    ('project\([''"]:' + $escapedReferenceModule + '[''"]\)'),
    ('include\s+[''"]' + $escapedReferenceModule + '[''"]'),
    ('(?i)\bmodule\b\s*[:=]\s*[''"]?' + $escapedReferenceModule + '\b')
)
$references = New-Object System.Collections.Generic.List[object]
$scanExtensions = @('.java', '.xml', '.yml', '.yaml', '.json', '.sql', '.gradle', '.ps1')
Get-ChildItem -LiteralPath $Root -Recurse -File | Where-Object {
    $_.FullName -notlike "$moduleDir*" -and
    $_.FullName -notmatch '\\.git\\|\\build\\|\\specs\\evidence\\' -and
    $scanExtensions -contains $_.Extension.ToLowerInvariant()
} | ForEach-Object {
    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    foreach ($pattern in $referencePatterns) {
        if ($text -notmatch $pattern) {
            continue
        }
        $relative = Get-RelativePath $_.FullName
        if ($relative -eq 'settings.gradle') {
            continue
        }
        $references.Add([ordered]@{ path = $relative; pattern = $pattern })
        break
    }
}

$blockReasons = New-Object System.Collections.Generic.List[string]
if ($changedFiles.Count -gt 0) {
    $blockReasons.Add("생성 후 사용자가 수정한 파일이 있습니다.")
}
if ($userOwnedFiles.Count -gt 0) {
    $blockReasons.Add("생성기 소유가 아닌 사용자 파일이 모듈에 있습니다.")
}
if ($references.Count -gt 0) {
    $blockReasons.Add("다른 source·SQL·배포 설정이 모듈을 참조합니다.")
}

$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = if ($blockReasons.Count -eq 0) { "READY" } else { "BLOCKED" }
    dryRun = [bool] $DryRun
    moduleCode = $module.ToUpperInvariant()
    moduleDirectory = $module
    manifest = Get-RelativePath $ownershipPath
    generatedFileCount = $ownedPaths.Count
    changedGeneratedFiles = @($changedFiles.ToArray())
    missingGeneratedFiles = @($missingFiles.ToArray())
    userOwnedFiles = $userOwnedFiles
    externalReferences = @($references.ToArray())
    blockReasons = @($blockReasons.ToArray())
    databaseObjectsRemoved = $false
    databasePolicy = "운영 DB 객체는 자동 DROP하지 않습니다. 백업과 승인 migration을 별도로 수행해야 합니다."
}

if ($DryRun) {
    Write-Result $result
    Write-Host "remove-domain dry-run status=$($result.status) result=$resultPath"
    return
}

if ($blockReasons.Count -gt 0) {
    Write-Result $result
    throw "사용자 변경 또는 외부 참조가 있어 모듈 제거를 중단했습니다. 먼저 dry-run 결과를 확인하세요."
}

foreach ($ownedFile in @($ownership.createdFiles)) {
    $absolute = Join-Path $moduleDir ([string] $ownedFile.path)
    if (Test-Path -LiteralPath $absolute -PathType Leaf) {
        Remove-Item -LiteralPath $absolute -Force
    }
}
Remove-Item -LiteralPath $ownershipPath -Force

# 생성 모듈의 compile/test/package 산출물은 사용자 소스가 아니므로 실제 제거 시 함께 정리합니다.
$moduleBuildDir = Join-Path $moduleDir 'build'
if (Test-Path -LiteralPath $moduleBuildDir -PathType Container) {
    $resolvedModuleBuildDir = [IO.Path]::GetFullPath($moduleBuildDir)
    $resolvedModuleDir = [IO.Path]::GetFullPath($moduleDir).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolvedModuleBuildDir.StartsWith($resolvedModuleDir, [StringComparison]::OrdinalIgnoreCase)) {
        throw "모듈 build 경로가 제거 허용 범위를 벗어났습니다. path=$resolvedModuleBuildDir"
    }
    Remove-Item -LiteralPath $resolvedModuleBuildDir -Recurse -Force
}

$settingsPath = Join-Path $Root 'settings.gradle'
$settingsText = [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
$escapedModule = [regex]::Escape($module)
$settingsText = [regex]::Replace($settingsText, "(?m)^\s*include\s+'$escapedModule'\s*\r?\n?", '')
$settingsText = [regex]::Replace(
        $settingsText,
        "(?m)^\s*project\(':$escapedModule'\)\.projectDir\s*=\s*file\('$escapedModule'\)\s*\r?\n?",
        '')
[System.IO.File]::WriteAllText($settingsPath, $settingsText, $Utf8NoBom)

# 파일 제거 후 비어 있는 생성 디렉터리만 아래에서 위 순서로 정리합니다.
Get-ChildItem -LiteralPath $moduleDir -Recurse -Directory |
    Sort-Object { $_.FullName.Length } -Descending |
    Where-Object { @(Get-ChildItem -LiteralPath $_.FullName -Force).Count -eq 0 } |
    Remove-Item -Force
if ((Test-Path -LiteralPath $moduleDir) -and @(Get-ChildItem -LiteralPath $moduleDir -Force).Count -eq 0) {
    Remove-Item -LiteralPath $moduleDir -Force
}

$result.status = "DONE"
$result.removedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
Write-Result $result
Write-Host "remove-domain completed. module=$module result=$resultPath"
