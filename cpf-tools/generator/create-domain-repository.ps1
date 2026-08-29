<#
.SYNOPSIS
Canonical cpf-domain.yaml에서 독립 Generated Domain Repository를 원자적으로 생성합니다.
.DESCRIPTION
Generated Project 내부에 lifecycle manifest/lock/ownership을 만들지 않습니다. 입력은
cpf-domain.schema.json을 따르는 Framework definition 또는 explicit -DefinitionFile 하나이며,
DB Vendor는 입력 metadata가 아니라 독립 Repository의 외부 Resource pack 선택값입니다.
#>
[CmdletBinding()]
param(
    [string] $DefinitionFile = '',
    [ValidatePattern('^$|^[a-zA-Z][a-zA-Z0-9-]{1,49}$')]
    [string] $DomainName = '',
    [ValidatePattern('^$|^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode = '',
    [ValidatePattern('^$|^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = '',
    [string] $DatabaseVendor = $env:CPF_DOMAIN_DB_VENDOR,
    [string] $ModuleName = '',
    [string] $PackageName = '',
    [ValidateSet('CUSTOMER_BUSINESS_DB')]
    [string] $BusinessDatabaseRole = 'CUSTOMER_BUSINESS_DB',
    [string] $TablePrefix = '',
    [ValidateRange(18080, 18998)]
    [int] $Port = 18080,
    [string] $Capabilities = 'database,local-call',
    [string] $OutputRoot = 'build/domain-repositories',
    [ValidateSet('AUTO', 'LOCAL_DEV', 'REMOTE', 'OFFLINE')]
    [string] $ArtifactMode = 'AUTO',
    [string] $LocalArtifactRepository = '',
    [string] $RemoteArtifactRepository = '',
    [string] $OfflineArtifactRepository = '',
    [switch] $CenterCut,
    [switch] $SkipBuild,
    [switch] $DryRun
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$frameworkRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
. (Join-Path $frameworkRoot 'cpf-tools/generator/tools/generated-domain-common.ps1')
. (Join-Path $frameworkRoot 'cpf-tools/db/tools/database-profile-common.ps1')
if ([string]::IsNullOrWhiteSpace($DatabaseVendor)) { throw 'DatabaseVendor가 필요합니다. -DatabaseVendor 또는 CPF_DOMAIN_DB_VENDOR를 설정하세요.' }
$DatabaseVendor = Assert-CpfSupportedDatabaseVendor $DatabaseVendor

function Get-EffectiveArtifactMode {
    if ($ArtifactMode -ne 'AUTO') { return $ArtifactMode }
    if (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_MODE)) {
        return $env:CPF_ARTIFACT_MODE.Trim().ToUpperInvariant()
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteArtifactRepository) -or
            -not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
        return 'REMOTE'
    }
    return 'LOCAL_DEV'
}

function Invoke-CpfVerifiedLocalArtifactPublish {
    param([Parameter(Mandatory = $true)][string] $EffectivePlatformVersion)
    if ((Get-EffectiveArtifactMode) -ne 'LOCAL_DEV' -or $SkipBuild) { return }
    $userProfile = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
    $localRepository = if (-not [string]::IsNullOrWhiteSpace($LocalArtifactRepository)) {
        [IO.Path]::GetFullPath($LocalArtifactRepository)
    } elseif (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        [IO.Path]::GetFullPath($env:CPF_LOCAL_ARTIFACT_REPOSITORY)
    } else {
        [IO.Path]::GetFullPath((Join-Path $userProfile '.cpf/repository'))
    }
    $env:CPF_LOCAL_ARTIFACT_REPOSITORY = $localRepository
    $verifier = Join-Path $frameworkRoot 'cpf-tools/verification/tools/verify-local-artifact-propagation.ps1'
    & pwsh -NoProfile -File $verifier `
        -Root $frameworkRoot `
        -LocalRepository $localRepository `
        -RequireManifest *> $null
    if ($LASTEXITCODE -eq 0) { return }

    $gradle = if ($IsWindows) { Join-Path $frameworkRoot 'gradlew.bat' } else { Join-Path $frameworkRoot 'gradlew' }
    Push-Location $frameworkRoot
    try {
        & $gradle publishCpfVerifiedLocalPlatformArtifacts `
            --no-daemon --max-workers=1 --console=plain `
            -PcpfArtifactMode=LOCAL_DEV `
            "-PcpfLocalArtifactRepository=$localRepository" `
            "-PcpfPlatformVersion=$EffectivePlatformVersion"
        if ($LASTEXITCODE -ne 0) {
            throw "현재 Source와 일치하는 CPF local artifact publish 실패: exit=$LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
}

if ($CenterCut) {
    throw 'Generated Domain canonical input에 CenterCut option이 없습니다. Batch/Center-Cut은 초기 프로젝트 구성에서 별도 Framework Capability로 선택해야 합니다.'
}
if (-not [string]::IsNullOrWhiteSpace($ModuleName)) {
    Write-Warning 'ModuleName은 canonical Generated Domain 입력에서 제거되었습니다. domain.name이 물리 Root를 결정합니다.'
}
$normalizedCapabilities = @($Capabilities -split '[,; ]+' |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        ForEach-Object { $_.Trim().ToLowerInvariant() } |
        Sort-Object -Unique)
if (($normalizedCapabilities -join ',') -notin @('', 'database,local-call')) {
    throw 'Capabilities 문자열 입력은 제거되었습니다. 실제 Consumer가 있는 features/modules를 -DefinitionFile에 선언하십시오.'
}

$temporaryDefinitionRoot = $null
$generatedSourceRoot = $null
$definitionPath = $null
try {
    if (-not [string]::IsNullOrWhiteSpace($DefinitionFile)) {
        $definitionPath = if ([IO.Path]::IsPathRooted($DefinitionFile)) {
            (Resolve-Path -LiteralPath $DefinitionFile).Path
        } else {
            (Resolve-Path -LiteralPath (Join-Path $frameworkRoot $DefinitionFile)).Path
        }
    } else {
        if ([string]::IsNullOrWhiteSpace($DomainName) -or
                [string]::IsNullOrWhiteSpace($SystemCode)) {
            throw '-DefinitionFile 또는 DomainName/SystemCode가 필요합니다.'
        }
        $domain = $DomainName.Trim().ToLowerInvariant()
        $system = $SystemCode.Trim().ToUpperInvariant()
        $package = if ([string]::IsNullOrWhiteSpace($PackageName)) {
            $domain
        } else {
            $PackageName.Trim()
        }
        $prefix = if ([string]::IsNullOrWhiteSpace($TablePrefix)) {
            $system
        } else {
            $TablePrefix.Trim().ToUpperInvariant()
        }
        $temporaryDefinitionRoot = Join-Path $frameworkRoot (
            'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/federation-input/' + [guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Force -Path $temporaryDefinitionRoot | Out-Null
        $definitionPath = Join-Path $temporaryDefinitionRoot 'cpf-domain.yaml'
        $definitionText = @"
# Standalone Repository 생성용 일회성 canonical input입니다. Generated Project에는 복사하지 않습니다.
domain:
  name: $domain
  systemCode: $system
  packageName: $package
database:
  role: $BusinessDatabaseRole
  tablePrefix: $prefix
preset: standard-enterprise
modules:
  online: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
runtime:
  localOnlinePort: $Port
generation:
  sampleTransaction: true
"@
        [IO.File]::WriteAllText($definitionPath, $definitionText, $Utf8NoBom)
    }

    $requestedDomain = if ([string]::IsNullOrWhiteSpace($DomainName)) {
        ''
    } else {
        $DomainName.Trim().ToLowerInvariant()
    }
    $definition = Get-CpfGeneratedDomainDefinition `
            -Root $frameworkRoot `
            -DomainName $(if ($requestedDomain) { $requestedDomain } else {
                # explicit file의 DomainName은 inventory 결과에서 확정합니다.
                $inventory = @(Get-CpfGeneratedDomainInventory -Root $frameworkRoot `
                        -DefinitionPath $definitionPath -IncludeMissing)
                if ($inventory.Count -ne 1) { throw 'Explicit canonical definition을 정확히 하나 해석하지 못했습니다.' }
                [string]$inventory[0].domainName
            }) `
            -DefinitionPath $definitionPath `
            -IncludeMissing
    $domain = [string]$definition.domainName
    if (-not [string]::IsNullOrWhiteSpace($DomainName) -and
            $domain -cne $DomainName.Trim().ToLowerInvariant()) {
        throw "요청 DomainName과 canonical definition이 다릅니다: requested=$DomainName canonical=$domain"
    }
    if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
            [string]$definition.systemCode -cne $SystemCode.Trim().ToUpperInvariant()) {
        throw "요청 SystemCode와 canonical definition이 다릅니다: requested=$SystemCode canonical=$($definition.systemCode)"
    }
    $outputRootAbsolute = if ([IO.Path]::IsPathRooted($OutputRoot)) {
        [IO.Path]::GetFullPath($OutputRoot)
    } else {
        [IO.Path]::GetFullPath((Join-Path $frameworkRoot $OutputRoot))
    }
    $target = Join-Path $outputRootAbsolute ([string]$definition.projectName)
    if (Test-Path -LiteralPath $target) {
        throw "Standalone Repository가 이미 있습니다. 사용자 Source를 덮어쓰지 않습니다: $target"
    }
    $generatedSourceParent = Join-Path $frameworkRoot (
        'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/federation-staging/' + [guid]::NewGuid().ToString('N'))
    $generatedSourceRoot = Join-Path $generatedSourceParent ([string]$definition.projectName)

    if ($DryRun) {
        $dryRunResult = Invoke-CpfCanonicalCli -Root $frameworkRoot -Arguments @(
            'domain', 'dry-run', '--file', $definitionPath, '--output', $generatedSourceRoot)
        [ordered]@{
            status = 'READY'
            dryRun = $true
            domainName = $domain
            systemCode = [string]$definition.systemCode
            target = $target
            canonicalSchema = 'cpf-tools/generator/contracts/cpf-domain.schema.json'
            contractSha256 = [string]$definition.contractSha256
            generatedProjectMetadata = 'ABSENT'
            databaseVendor = if ([bool]$definition.databaseEnabled) { $DatabaseVendor } else { $null }
            artifactMode = $ArtifactMode
            batchCapabilitySelection = 'PROJECT_SETUP'
            canonicalDryRun = $dryRunResult
        } | ConvertTo-Json -Depth 30
        return
    }

    $generated = Invoke-CpfCanonicalCli -Root $frameworkRoot -Arguments @(
        'domain', 'generate', '--file', $definitionPath, '--output', $generatedSourceRoot)
    if ([string]$generated.status -notin @('GENERATED', 'IDEMPOTENT')) {
        throw "Canonical Generated Domain 생성 상태가 유효하지 않습니다: $($generated.status)"
    }

    $effectivePlatformVersion = $PlatformVersion
    if ([string]::IsNullOrWhiteSpace($effectivePlatformVersion)) {
        $platformPropertiesPath = Join-Path $frameworkRoot 'gradle/cpf-platform.properties'
        $platformLine = Get-Content -LiteralPath $platformPropertiesPath -Encoding UTF8 |
                Where-Object { $_ -match '^\s*platformVersion\s*=' } |
                Select-Object -First 1
        if ($null -eq $platformLine) { throw "platformVersion 정본이 없습니다: $platformPropertiesPath" }
        $effectivePlatformVersion = ($platformLine -split '=', 2)[1].Trim()
    }
    Invoke-CpfVerifiedLocalArtifactPublish -EffectivePlatformVersion $effectivePlatformVersion

    $exportArguments = @(
        '-DomainModule', ([string]$definition.projectName),
        '-SourceRoot', $generatedSourceRoot,
        '-DefinitionFile', $definitionPath,
        '-SystemCode', ([string]$definition.systemCode),
        '-DatabaseVendor', $DatabaseVendor,
        '-OutputRoot', $outputRootAbsolute,
        '-ArtifactMode', $ArtifactMode)
    if (-not [string]::IsNullOrWhiteSpace($PlatformVersion)) {
        $exportArguments += @('-PlatformVersion', $PlatformVersion)
    }
    if (-not [string]::IsNullOrWhiteSpace($LocalArtifactRepository)) {
        $exportArguments += @('-LocalArtifactRepository', $LocalArtifactRepository)
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteArtifactRepository)) {
        $exportArguments += @('-RemoteArtifactRepository', $RemoteArtifactRepository)
    }
    if (-not [string]::IsNullOrWhiteSpace($OfflineArtifactRepository)) {
        $exportArguments += @('-OfflineArtifactRepository', $OfflineArtifactRepository)
    }
    if ($SkipBuild) { $exportArguments += '-SkipBuild' }
    & pwsh -NoProfile -File (Join-Path $PSScriptRoot 'export-domain-repository.ps1') @exportArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Standalone Repository export 실패: exit=$LASTEXITCODE"
    }
} finally {
    if ($null -ne $generatedSourceRoot -and
            (Test-Path -LiteralPath $generatedSourceRoot -PathType Container)) {
        try {
            $remove = Invoke-CpfCanonicalCli -Root $frameworkRoot -Arguments @(
                'domain', 'remove', (Split-Path -Leaf $generatedSourceRoot).Substring(4),
                '--file', $definitionPath, '--output', $generatedSourceRoot, '--apply')
            if ([string]$remove.status -cne 'REMOVED') {
                throw "Federation staging Generated Source 제거 상태가 유효하지 않습니다: $($remove.status)"
            }
        } catch {
            Write-Error "Federation staging Generated Source 공식 제거 실패: $($_.Exception.Message)"
        }
    }
    if ($null -ne $temporaryDefinitionRoot -and
            $null -ne $definitionPath -and
            (Test-Path -LiteralPath $definitionPath -PathType Leaf)) {
        $resolvedDefinition = [IO.Path]::GetFullPath($definitionPath)
        $temporaryPrefix = [IO.Path]::GetFullPath($temporaryDefinitionRoot).TrimEnd('\', '/') +
                [IO.Path]::DirectorySeparatorChar
        if (-not $resolvedDefinition.StartsWith($temporaryPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                (Split-Path -Leaf $resolvedDefinition) -cne 'cpf-domain.yaml') {
            throw "Temporary canonical definition cleanup 경로가 허용 범위를 벗어났습니다: $resolvedDefinition"
        }
        Remove-Item -LiteralPath $resolvedDefinition -Force
    }
    foreach ($cleanupRoot in @(
            $(if ($null -ne $generatedSourceRoot) { Split-Path -Parent $generatedSourceRoot } else { $null }),
            $temporaryDefinitionRoot)) {
        if ($null -eq $cleanupRoot -or -not (Test-Path -LiteralPath $cleanupRoot -PathType Container)) {
            continue
        }
        if (@(Get-ChildItem -LiteralPath $cleanupRoot -Force).Count -eq 0) {
            Remove-Item -LiteralPath $cleanupRoot -Force
        }
    }
}
