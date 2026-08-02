param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultPath = ""
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$root = (Resolve-Path -LiteralPath $RepoRoot).Path
$failures = [System.Collections.Generic.List[string]]::new()
$checkedRepositories = [System.Collections.Generic.List[object]]::new()

function Add-Failure([string] $Message) {
    $failures.Add($Message)
}

function Find-GeneratedModules([string] $SearchRoot) {
    $modules = [System.Collections.Generic.List[object]]::new()
    foreach ($directory in Get-ChildItem -LiteralPath $SearchRoot -Directory -ErrorAction SilentlyContinue) {
        $manifestPath = Join-Path $directory.FullName "manifest/domain-manifest.json"
        if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
            continue
        }
        try {
            $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
            if ([string]$manifest.domainType -eq "GENERATED_DOMAIN") {
                $modules.Add([ordered]@{
                    path = $directory.FullName
                    manifestPath = $manifestPath
                    manifest = $manifest
                })
            }
        } catch {
            Add-Failure "Generated Domain metadata parse 실패: $manifestPath"
        }
    }
    return @($modules)
}

function Test-ForbiddenSource(
    [string] $Path,
    [string] $Label,
    [bool] $DisallowRootProjectDependency
) {
    $files = @(Get-ChildItem -LiteralPath $Path -Recurse -File -Include *.java,*.gradle,*.kts -ErrorAction SilentlyContinue |
            Where-Object {
                $relative = $_.FullName.Substring($Path.Length + 1).Replace('\', '/')
                $relative -notmatch '^(?:build|\.gradle)(?:/|$)'
            })
    $patterns = [ordered]@{
        "cpf-core internal import" = 'com\.cpf\.core\.common\.'
    }
    if ($DisallowRootProjectDependency) {
        $patterns["CPF Root project dependency"] =
                'project\s*\(\s*[''"]:cpf-(?:core|common|batch)(?::|[''"])'
    }
    foreach ($entry in $patterns.GetEnumerator()) {
        $hits = @($files | Select-String -Pattern $entry.Value)
        foreach ($hit in $hits) {
            Add-Failure "$Label - $($entry.Key): $($hit.Path):$($hit.LineNumber)"
        }
    }
}

function Test-StandaloneRepository([string] $RepositoryPath) {
    $repositoryManifestPath = Join-Path $RepositoryPath "cpf-domain-manifest.json"
    if (-not (Test-Path -LiteralPath $repositoryManifestPath -PathType Leaf)) {
        Add-Failure "Standalone repository manifest 누락: $repositoryManifestPath"
        return
    }
    try {
        $repositoryManifest = Get-Content -LiteralPath $repositoryManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        Add-Failure "Standalone repository manifest parse 실패: $repositoryManifestPath"
        return
    }

    $domainModule = [string]$repositoryManifest.domainModule
    $domainModulePath = Join-Path $RepositoryPath $domainModule
    $domainManifestPath = Join-Path $domainModulePath "manifest/domain-manifest.json"
    if (-not (Test-Path -LiteralPath $domainManifestPath -PathType Leaf)) {
        Add-Failure "Standalone Generated Domain metadata 누락: $domainManifestPath"
        return
    }
    $domainManifest = Get-Content -LiteralPath $domainManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]$domainManifest.domainType -ne "GENERATED_DOMAIN") {
        Add-Failure "Standalone module domainType이 GENERATED_DOMAIN이 아닙니다: $domainManifestPath"
    }
    if ([string]$repositoryManifest.systemCode -ne [string]$domainManifest.systemCode -or
            [string]$repositoryManifest.domainName -ne [string]$domainManifest.domainName) {
        Add-Failure "Standalone repository/module metadata가 일치하지 않습니다: $RepositoryPath"
    }
    if ([string]$repositoryManifest.dependencyModel -ne "published-artifact" -or
            [string]$domainManifest.dependencyModel -ne "published-artifact") {
        Add-Failure "Standalone dependencyModel은 published-artifact여야 합니다: $RepositoryPath"
    }

    foreach ($required in @(
            "settings.gradle",
            "build.gradle",
            "gradlew",
            "gradlew.bat",
            "gradle/wrapper/gradle-wrapper.jar",
            "gradle/wrapper/gradle-wrapper.properties",
            "$domainModule/build.gradle",
            "cpf-domain-ownership.json")) {
        if (-not (Test-Path -LiteralPath (Join-Path $RepositoryPath $required) -PathType Leaf)) {
            Add-Failure "Standalone 필수 파일 누락: $required ($RepositoryPath)"
        }
    }

    $moduleBuildPath = Join-Path $domainModulePath "build.gradle"
    if (Test-Path -LiteralPath $moduleBuildPath -PathType Leaf) {
        $moduleBuild = Get-Content -LiteralPath $moduleBuildPath -Raw -Encoding UTF8
        foreach ($requiredText in @(
                "implementation platform('com.cpf:cpf-platform-bom:",
                "implementation 'com.cpf.core:cpf-core:",
                "implementation 'com.cpf.common:cpf-common:",
                "dependencyLocking",
                "cpf-db/vendor")) {
            if (-not $moduleBuild.Contains($requiredText)) {
                Add-Failure "Standalone build 계약 누락 '$requiredText': $moduleBuildPath"
            }
        }
    }

    if ([bool]$domainManifest.databaseEnabled) {
        $vendor = ([string]$domainManifest.databaseVendor).ToLowerInvariant()
        $selectedTemplate = Join-Path $RepositoryPath "cpf-db/vendor/$vendor/domain-template"
        $snapshotPath = Join-Path $RepositoryPath "cpf-db/domain-template-snapshot.json"
        if (-not (Test-Path -LiteralPath $selectedTemplate -PathType Container)) {
            Add-Failure "선택 Vendor Domain Template 누락: $selectedTemplate"
        }
        if (-not (Test-Path -LiteralPath $snapshotPath -PathType Leaf)) {
            Add-Failure "Domain Template snapshot metadata 누락: $snapshotPath"
        }
        foreach ($dbToolFile in @(
                "cpf-db/initialize-domain-database.ps1",
                "cpf-db/tools/initialize-domain-database.ps1",
                "cpf-db/tools/database-profile-common.ps1")) {
            if (-not (Test-Path -LiteralPath (Join-Path $RepositoryPath $dbToolFile) -PathType Leaf)) {
                Add-Failure "Standalone Domain DB Tool 누락: $dbToolFile"
            }
        }
        if ([string]$domainManifest.databaseLifecycle.bootstrapScript -ne
                "cpf-db/initialize-domain-database.ps1") {
            Add-Failure "Standalone DB bootstrapScript 경로가 독립 Repository 기준이 아닙니다."
        }
        $vendorDirectories = @(Get-ChildItem -LiteralPath (Join-Path $RepositoryPath "cpf-db/vendor") `
                -Directory -ErrorAction SilentlyContinue)
        if ($vendorDirectories.Count -ne 1 -or $vendorDirectories[0].Name -ne $vendor) {
            Add-Failure "Standalone repository에는 선택 Vendor 하나만 있어야 합니다: $RepositoryPath"
        }
    }

    $jobPackModules = @(Get-ChildItem -LiteralPath $RepositoryPath -Directory -Filter "cpf-*-batch-jobpack")
    foreach ($jobPackModule in $jobPackModules) {
        $jobBuild = Join-Path $jobPackModule.FullName "build.gradle"
        $jobManifest = Join-Path $jobPackModule.FullName "manifest/job-pack.json"
        if (-not (Test-Path -LiteralPath $jobManifest -PathType Leaf)) {
            Add-Failure "Generated Job Pack manifest 누락: $jobManifest"
        }
        if (-not (Test-Path -LiteralPath $jobBuild -PathType Leaf) -or
                -not (Get-Content -LiteralPath $jobBuild -Raw -Encoding UTF8).Contains(
                        "com.cpf.batch:cpf-batch-contract")) {
            Add-Failure "Generated Job Pack public contract dependency 누락: $jobBuild"
        }
    }

    Test-ForbiddenSource `
            -Path $RepositoryPath `
            -Label ([string]$repositoryManifest.repositoryName) `
            -DisallowRootProjectDependency $true
    $checkedRepositories.Add([ordered]@{
        path = $RepositoryPath
        domainName = [string]$repositoryManifest.domainName
        systemCode = [string]$repositoryManifest.systemCode
        databaseVendor = [string]$repositoryManifest.databaseVendor
        jobPackCount = $jobPackModules.Count
    })
}

$standaloneManifestAtRoot = Join-Path $root "cpf-domain-manifest.json"
if (Test-Path -LiteralPath $standaloneManifestAtRoot -PathType Leaf) {
    Test-StandaloneRepository -RepositoryPath $root
} else {
    foreach ($generatedModule in @(Find-GeneratedModules -SearchRoot $root)) {
        Test-ForbiddenSource `
                -Path $generatedModule.path `
                -Label ([string]$generatedModule.manifest.projectName) `
                -DisallowRootProjectDependency (
                    [string]$generatedModule.manifest.dependencyModel -eq "published-artifact")
        $checkedRepositories.Add([ordered]@{
            path = $generatedModule.path
            domainName = [string]$generatedModule.manifest.domainName
            systemCode = [string]$generatedModule.manifest.systemCode
            dependencyModel = [string]$generatedModule.manifest.dependencyModel
        })
    }
}

$result = [ordered]@{
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    root = $root
    checked = @($checkedRepositories)
    failures = @($failures)
}
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    $resultAbsolute = if ([IO.Path]::IsPathRooted($ResultPath)) {
        $ResultPath
    } else {
        Join-Path $root $ResultPath
    }
    $resultParent = Split-Path -Parent $resultAbsolute
    New-Item -ItemType Directory -Force -Path $resultParent | Out-Null
    [IO.File]::WriteAllText(
            $resultAbsolute,
            (($result | ConvertTo-Json -Depth 30) + [Environment]::NewLine),
            $Utf8NoBom)
}
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "Domain federation boundary check: PASS checked=$($checkedRepositories.Count)"
