[CmdletBinding()]
param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $FrameworkRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $DefinitionFile = '',
    [string] $DomainName = '',
    [string] $DatabaseVendor = $env:CPF_DOMAIN_DB_VENDOR,
    [ValidateSet('LOCAL_DEV', 'REMOTE', 'OFFLINE')]
    [string] $ArtifactMode = 'LOCAL_DEV',
    [string] $ResultPath = ''
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$root = (Resolve-Path -LiteralPath $RepoRoot).Path
$frameworkRootResolved = (Resolve-Path -LiteralPath $FrameworkRoot).Path
. (Join-Path $frameworkRootResolved 'cpf-tools/generator/tools/generated-domain-common.ps1')
. (Join-Path $frameworkRootResolved 'cpf-tools/db/tools/database-profile-common.ps1')
if (-not [string]::IsNullOrWhiteSpace($DatabaseVendor)) {
    $DatabaseVendor = Assert-CpfSupportedDatabaseVendor $DatabaseVendor
}

$failures = [System.Collections.Generic.List[string]]::new()
$checked = [System.Collections.Generic.List[object]]::new()

function Add-Failure([string] $Message) {
    $failures.Add($Message)
}

function Test-ForbiddenSource {
    param(
        [Parameter(Mandatory = $true)][string] $Path,
        [Parameter(Mandatory = $true)][string] $Label,
        [switch] $DisallowProductComposite
    )
    $files = @(Get-ChildItem -LiteralPath $Path -Recurse -File -Include *.java,*.gradle,*.kts `
            -ErrorAction SilentlyContinue |
            Where-Object {
                $relative = $_.FullName.Substring($Path.Length + 1).Replace('\', '/')
                $relative -notmatch '^(?:build|\.gradle)(?:/|$)'
            })
    $patterns = [ordered]@{
        'cpf-core internal import' = 'com\.cpf\.core\.common\.'
        'CPF product project dependency' = 'project\s*\(\s*[''\"]:cpf-(?:core|common|batch)(?::|[''\"])'
        'Generated lifecycle manifest consumer' = '(?:domain-manifest|generator-ownership)\.json'
    }
    if ($DisallowProductComposite) {
        $patterns['CPF Product source composite activation'] = 'includeBuild\s*\([^\r\n]*cpfProductCompositeRoot'
    }
    foreach ($entry in $patterns.GetEnumerator()) {
        foreach ($hit in @($files | Select-String -Pattern $entry.Value)) {
            Add-Failure "$Label - $($entry.Key): $($hit.Path):$($hit.LineNumber)"
        }
    }
}

function Test-PermanentMetadata {
    param([Parameter(Mandatory = $true)][string] $ProjectRoot)
    foreach ($relative in @(
            '.cpf',
            'cpf-domain.yaml',
            'cpf-domain-manifest.json',
            'cpf-domain-ownership.json',
            'manifest/domain-manifest.json',
            'manifest/generator-ownership.json')) {
        if (Test-Path -LiteralPath (Join-Path $ProjectRoot $relative)) {
            Add-Failure "Generated Project 영구 lifecycle metadata 금지: $ProjectRoot/$relative"
        }
    }
}

function Test-CanonicalGeneratedProject {
    param([Parameter(Mandatory = $true)][object] $Definition)
    $projectPath = Join-Path $frameworkRootResolved ([string]$Definition.projectName)
    if (-not [bool]$Definition.exists -or -not (Test-Path -LiteralPath $projectPath -PathType Container)) {
        Add-Failure "Canonical definition의 Generated Project가 없습니다: $($Definition.projectName)"
        return
    }
    if ([string]$Definition.generatedProjectMetadata -cne 'NONE' -or
            @($Definition.forbiddenPermanentMetadata).Count -ne 0) {
        Add-Failure "Generated Project metadata 계약 위반: $($Definition.projectName)"
    }
    Test-PermanentMetadata -ProjectRoot $projectPath
    Test-ForbiddenSource -Path $projectPath -Label ([string]$Definition.projectName)
    try {
        $verify = Invoke-CpfCanonicalCli -Root $frameworkRootResolved -Arguments @(
            'verify', 'domain', '--file', ([string]$Definition.definitionPath), '--output', $projectPath)
        if ([string]$verify.status -cne 'PASS') {
            Add-Failure "Canonical Generated Domain verify status가 PASS가 아닙니다: $($Definition.projectName)"
        }
    } catch {
        Add-Failure "Canonical Generated Domain verify 실패: $($Definition.projectName) :: $($_.Exception.Message)"
    }
    $checked.Add([ordered]@{
        mode = 'framework-canonical'
        path = $projectPath
        domainName = [string]$Definition.domainName
        systemCode = [string]$Definition.systemCode
        definitionSha256 = [string]$Definition.definitionSha256
        generatedProjectMetadata = 'NONE'
    }) | Out-Null
}

function Test-StandaloneRepository {
    param([Parameter(Mandatory = $true)][object] $Definition)
    if ([bool]$Definition.databaseEnabled -and [string]::IsNullOrWhiteSpace($DatabaseVendor)) {
        throw 'DatabaseVendor가 필요합니다. -DatabaseVendor 또는 CPF_DOMAIN_DB_VENDOR를 설정하세요.'
    }
    $expectedRootName = [string]$Definition.projectName
    if ((Split-Path -Leaf $root) -cne $expectedRootName) {
        Add-Failure "Standalone root 이름은 canonical cpf-<domain>이어야 합니다: expected=$expectedRootName actual=$(Split-Path -Leaf $root)"
    }
    Test-PermanentMetadata -ProjectRoot $root

    $expectedModules = [System.Collections.Generic.List[string]]::new()
    if ([bool]$Definition.onlineEnabled) { $expectedModules.Add('online') }
    foreach ($forbiddenGeneratedModule in @('batch', 'domain', 'jobpack')) {
        if (Test-Path -LiteralPath (Join-Path $root $forbiddenGeneratedModule)) {
            Add-Failure "Generated Domain에는 $forbiddenGeneratedModule module을 생성할 수 없습니다. Batch는 초기 프로젝트 구성에서 cpf-starter-batch로 별도 선택합니다."
        }
    }
    foreach ($required in @(
            'settings.gradle',
            'build.gradle',
            'gradle.properties',
            'gradlew',
            'gradlew.bat',
            'gradle/wrapper/gradle-wrapper.jar',
            'gradle/wrapper/gradle-wrapper.properties',
            'gradle/cpf-federation-repositories.gradle')) {
        if (-not (Test-Path -LiteralPath (Join-Path $root $required) -PathType Leaf)) {
            Add-Failure "Standalone 필수 파일 누락: $required"
        }
    }
    $settingsPath = Join-Path $root 'settings.gradle'
    if (Test-Path -LiteralPath $settingsPath -PathType Leaf) {
        $settings = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
        foreach ($module in $expectedModules) {
            if ($settings -notmatch "(?m)^\s*include\s+['`"]$([regex]::Escape($module))['`"]\s*$") {
                Add-Failure "Standalone settings.gradle module 누락: $module"
            }
        }
        if ($settings -match 'cpfProductCompositeRoot\s*=\s*[^\r\n]+\.orElse') {
            Add-Failure 'Standalone Repository가 CPF Product source composite를 기본 활성화합니다.'
        }
    }
    $rootBuildPath = Join-Path $root 'build.gradle'
    if (Test-Path -LiteralPath $rootBuildPath -PathType Leaf) {
        $rootBuild = Get-Content -LiteralPath $rootBuildPath -Raw -Encoding UTF8
        if (-not $rootBuild.Contains("apply from: rootProject.file('gradle/cpf-federation-repositories.gradle')")) {
            Add-Failure 'Standalone root build에 Artifact federation repository 설정이 연결되지 않았습니다.'
        }
    }
    $repositoryScriptPath = Join-Path $root 'gradle/cpf-federation-repositories.gradle'
    if (Test-Path -LiteralPath $repositoryScriptPath -PathType Leaf) {
        $repositoryScript = Get-Content -LiteralPath $repositoryScriptPath -Raw -Encoding UTF8
        foreach ($token in @(
                'CPF_ARTIFACT_MODE',
                'CPF_LOCAL_ARTIFACT_REPOSITORY',
                'CPF_ARTIFACT_REPOSITORY_URL',
                'CPF_OFFLINE_ARTIFACT_REPOSITORY',
                "artifactMode == '$ArtifactMode'",
                "excludeGroupByRegex 'com\\.cpf")) {
            if (-not $repositoryScript.Contains($token)) {
                Add-Failure "Standalone Artifact federation 설정 token 누락: $token"
            }
        }
    }

    $dbRoot = Join-Path $root 'cpf-db/generated/domain-template'
    if ([bool]$Definition.databaseEnabled) {
        $selectedPack = Join-Path $dbRoot $DatabaseVendor
        if (-not (Test-Path -LiteralPath $selectedPack -PathType Container)) {
            Add-Failure "Standalone 선택 Vendor pack 누락: $selectedPack"
        } else {
            $canonicalPack = Join-Path $frameworkRootResolved "cpf-tools/db/generated/domain-template/$DatabaseVendor"
            $expectedFiles = @(Get-ChildItem -LiteralPath $canonicalPack -Recurse -File | ForEach-Object {
                $_.FullName.Substring($canonicalPack.Length + 1).Replace('\', '/')
            } | Sort-Object)
            $actualFiles = @(Get-ChildItem -LiteralPath $selectedPack -Recurse -File | ForEach-Object {
                $_.FullName.Substring($selectedPack.Length + 1).Replace('\', '/')
            } | Sort-Object)
            if (($expectedFiles -join "`n") -cne ($actualFiles -join "`n")) {
                Add-Failure "Standalone 선택 Vendor pack 파일 집합이 canonical과 다릅니다: $DatabaseVendor"
            } else {
                foreach ($relative in $expectedFiles) {
                    $expectedHash = (Get-FileHash -LiteralPath (Join-Path $canonicalPack $relative) -Algorithm SHA256).Hash
                    $actualHash = (Get-FileHash -LiteralPath (Join-Path $selectedPack $relative) -Algorithm SHA256).Hash
                    if ($expectedHash -cne $actualHash) {
                        Add-Failure "Standalone 선택 Vendor pack hash 불일치: $DatabaseVendor/$relative"
                    }
                }
            }
        }
        $vendors = @(Get-ChildItem -LiteralPath $dbRoot -Directory -ErrorAction SilentlyContinue)
        if ($vendors.Count -ne 1 -or $vendors[0].Name -cne $DatabaseVendor) {
            Add-Failure "Standalone Repository에는 선택 Vendor pack 하나만 있어야 합니다: expected=$DatabaseVendor actual=$($vendors.Name -join ',')"
        }
    } elseif (Test-Path -LiteralPath $dbRoot) {
        Add-Failure 'DB capability가 없는 Generated Domain에 Vendor pack이 포함되었습니다.'
    }

    Test-ForbiddenSource -Path $root -Label $expectedRootName
    $checked.Add([ordered]@{
        mode = 'standalone-federated'
        path = $root
        domainName = [string]$Definition.domainName
        systemCode = [string]$Definition.systemCode
        definitionSha256 = [string]$Definition.definitionSha256
        databaseVendor = if ([bool]$Definition.databaseEnabled) { $DatabaseVendor } else { $null }
        artifactMode = $ArtifactMode
        batchCapabilitySelection = 'PROJECT_SETUP'
        generatedProjectMetadata = 'NONE'
    }) | Out-Null
}

if ($root -eq $frameworkRootResolved) {
    $definitions = @(Get-CpfGeneratedDomainInventory -Root $frameworkRootResolved)
    if ($definitions.Count -eq 0) { Add-Failure 'Framework canonical Generated Domain definition/output이 없습니다.' }
    foreach ($definition in $definitions) {
        Test-CanonicalGeneratedProject -Definition $definition
    }
} else {
    if ([string]::IsNullOrWhiteSpace($DomainName)) {
        $leaf = Split-Path -Leaf $root
        if ($leaf -notmatch '^cpf-([a-z][a-z0-9-]{1,49})$') {
            throw "Standalone root에서 DomainName을 추론할 수 없습니다: $leaf"
        }
        $DomainName = $Matches[1]
    }
    if ([string]::IsNullOrWhiteSpace($DefinitionFile)) {
        $DefinitionFile = Join-Path $frameworkRootResolved "cpf-tools/generator/definitions/$DomainName/cpf-domain.yaml"
    }
    $definition = Get-CpfGeneratedDomainDefinition `
            -Root $frameworkRootResolved `
            -DomainName $DomainName `
            -DefinitionPath $DefinitionFile `
            -IncludeMissing
    Test-StandaloneRepository -Definition $definition
}

$result = [ordered]@{
    status = if ($failures.Count -eq 0) { 'PASS' } else { 'FAIL' }
    root = $root
    frameworkRoot = $frameworkRootResolved
    checked = @($checked)
    failures = @($failures)
}
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    $resultAbsolute = if ([IO.Path]::IsPathRooted($ResultPath)) {
        [IO.Path]::GetFullPath($ResultPath)
    } else {
        [IO.Path]::GetFullPath((Join-Path $root $ResultPath))
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
$result | ConvertTo-Json -Depth 30
