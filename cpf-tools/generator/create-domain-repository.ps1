<#
.SYNOPSIS
Generated Domain을 독립 Repository로 생성하고 선택 Artifact 공급모드로 Build/검증합니다.
.DESCRIPTION
Domain 생성 → Export → Federation 검증 → 선택 시 Job Pack 생성 → standalone test/package를 수행합니다.
LOCAL_DEV는 현재 Source fingerprint와 일치하는 PROMOTED Local CPF Artifact를 재사용하며 stale하면 검증 Publish를 수행합니다.
REMOTE/OFFLINE은 Local fallback 없이 fail-closed합니다.
.PARAMETER ArtifactMode
AUTO, LOCAL_DEV, REMOTE, OFFLINE. AUTO는 명시 환경 설정을 우선하고 없으면 LOCAL_DEV입니다.
.PARAMETER LocalArtifactRepository
LOCAL_DEV Maven Repository. 미지정 시 CPF_LOCAL_ARTIFACT_REPOSITORY 또는 ~/.cpf/repository.
.PARAMETER RemoteArtifactRepository
REMOTE Nexus/Artifactory URL. 미지정 시 CPF_ARTIFACT_REPOSITORY_URL.
.PARAMETER OfflineArtifactRepository
OFFLINE Maven Repository 경로. 미지정 시 CPF_OFFLINE_ARTIFACT_REPOSITORY.
.PARAMETER SkipBuild
생성/Export만 수행하고 standalone Build를 생략합니다.
.PARAMETER DryRun
변경 없이 계산된 Domain/Artifact 설정을 출력합니다.
.EXAMPLE
pwsh -File .\cpf-tools\generator\create-domain-repository.ps1 -DomainName payment -SystemCode PAY -ArtifactMode LOCAL_DEV
.EXAMPLE
pwsh -File .\cpf-tools\generator\create-domain-repository.ps1 -DomainName payment -SystemCode PAY -ArtifactMode REMOTE -RemoteArtifactRepository https://nexus.example/repository/cpf-releases/
#>
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9]{1,29}$')]
    [string] $DomainName,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode,
    [ValidatePattern('^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [string] $DatabaseVendor = "mariadb",
    [string] $ModuleName = "",
    [string] $PackageName = "",
    [string] $SchemaName = "",
    [string] $TablePrefix = "",
    [ValidateRange(1024, 65535)]
    [int] $Port = 8080,
    [string] $Capabilities = "database,local-call",
    [string] $OutputRoot = "build/domain-repositories",
    [ValidateSet("AUTO", "LOCAL_DEV", "REMOTE", "OFFLINE")]
    [string] $ArtifactMode = "AUTO",
    [string] $LocalArtifactRepository = "",
    [string] $RemoteArtifactRepository = "",
    [string] $OfflineArtifactRepository = "",
    [switch] $Batch,
    [switch] $CenterCut,
    [switch] $SkipBuild,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$repositoryRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
. (Join-Path $repositoryRoot "cpf-tools/scripts/database-profile-common.ps1")
$DatabaseVendor = Assert-CpfSupportedDatabaseVendor $DatabaseVendor
if (-not [string]::IsNullOrWhiteSpace($LocalArtifactRepository)) {
    $env:CPF_LOCAL_ARTIFACT_REPOSITORY = [IO.Path]::GetFullPath($LocalArtifactRepository)
}
if (-not [string]::IsNullOrWhiteSpace($RemoteArtifactRepository)) {
    $env:CPF_ARTIFACT_REPOSITORY_URL = $RemoteArtifactRepository.Trim()
}
$effectiveArtifactMode = if (-not [string]::IsNullOrWhiteSpace($ArtifactMode) -and $ArtifactMode -ne 'AUTO') {
    $ArtifactMode.Trim().ToUpperInvariant()
} elseif (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_MODE)) {
    $env:CPF_ARTIFACT_MODE.Trim().ToUpperInvariant()
} elseif (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
    "REMOTE"
} else {
    "LOCAL_DEV"
}
$env:CPF_ARTIFACT_MODE = $effectiveArtifactMode
if ($effectiveArtifactMode -eq "REMOTE" -and [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
    throw "ArtifactMode=REMOTE에는 CPF_ARTIFACT_REPOSITORY_URL이 필요합니다."
}
if ($effectiveArtifactMode -eq "OFFLINE") {
    if (-not [string]::IsNullOrWhiteSpace($OfflineArtifactRepository)) {
        $env:CPF_OFFLINE_ARTIFACT_REPOSITORY = [IO.Path]::GetFullPath($OfflineArtifactRepository)
    }
    if ([string]::IsNullOrWhiteSpace($env:CPF_OFFLINE_ARTIFACT_REPOSITORY)) {
        throw "ArtifactMode=OFFLINE에는 -OfflineArtifactRepository 또는 CPF_OFFLINE_ARTIFACT_REPOSITORY가 필요합니다."
    }
}
$domain = $DomainName.Trim().ToLowerInvariant()
$systemCodeNormalized = $SystemCode.Trim().ToUpperInvariant()
$module = "cpf-$domain"
$temporaryModule = Join-Path $repositoryRoot $module
$outputRootAbsolute = if ([IO.Path]::IsPathRooted($OutputRoot)) {
    [IO.Path]::GetFullPath($OutputRoot)
} else {
    [IO.Path]::GetFullPath((Join-Path $repositoryRoot $OutputRoot))
}
$target = Join-Path $outputRootAbsolute "cpf-domain-$domain"
$orchestrationOutputRoot = Join-Path $outputRootAbsolute (
        ".cpf-domain-$domain.orchestration-" + [guid]::NewGuid().ToString("N"))
$orchestrationTarget = Join-Path $orchestrationOutputRoot "cpf-domain-$domain"

function Invoke-CheckedPowerShell {
    param(
        [Parameter(Mandatory = $true)] [string] $Script,
        [Parameter(Mandatory = $true)] [object[]] $Arguments
    )
    & pwsh -NoProfile -File $Script @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "CPF script failed. script=$Script exitCode=$LASTEXITCODE"
    }
}

function Invoke-CpfLocalArtifactPublish {
    if ($effectiveArtifactMode -ne 'LOCAL_DEV') { return }
    $localRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        [IO.Path]::GetFullPath($env:CPF_LOCAL_ARTIFACT_REPOSITORY)
    } else {
        [IO.Path]::GetFullPath((Join-Path $HOME ".cpf/repository"))
    }
    $env:CPF_LOCAL_ARTIFACT_REPOSITORY = $localRepository

    $platformProperties = @{}
    foreach ($line in Get-Content -LiteralPath (Join-Path $repositoryRoot 'gradle/cpf-platform.properties') -Encoding UTF8) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
        $index = $trimmed.IndexOf('=')
        if ($index -gt 0) { $platformProperties[$trimmed.Substring(0,$index).Trim()] = $trimmed.Substring($index+1).Trim() }
    }
    $sourcePlatformVersion = [string]$platformProperties['platformVersion']
    if ($sourcePlatformVersion -ne $PlatformVersion) {
        throw "LOCAL_DEV standalone generation must use current CPF platformVersion. requested=$PlatformVersion current=$sourcePlatformVersion"
    }

    # 이미 현재 HEAD와 정확히 일치하는 PROMOTED manifest가 있으면 고비용 aggregate build를 반복하지 않습니다.
    $verifier = Join-Path $repositoryRoot 'cpf-tools/scripts/verify-local-artifact-propagation.ps1'
    & pwsh -NoProfile -File $verifier -Root $repositoryRoot -LocalRepository $localRepository -RequireManifest *> $null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "CPF local artifacts already match current source; verified publication is reused: $localRepository"
        return
    }

    $gradle = if ($IsWindows) { Join-Path $repositoryRoot "gradlew.bat" } else { Join-Path $repositoryRoot "gradlew" }
    if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) { throw "CPF Gradle Wrapper가 없습니다: $gradle" }
    Push-Location $repositoryRoot
    try {
        & $gradle publishCpfVerifiedLocalPlatformArtifacts --no-daemon --max-workers=1 --console=plain `
            -PcpfArtifactMode=LOCAL_DEV "-PcpfLocalArtifactRepository=$localRepository"
        if ($LASTEXITCODE -ne 0) {
            throw "CPF local artifact publish failed. exitCode=$LASTEXITCODE repository=$localRepository"
        }
    } finally {
        Pop-Location
    }
}

if (Test-Path -LiteralPath $temporaryModule) {
    throw "Root temporary module already exists: $temporaryModule"
}
if (Test-Path -LiteralPath $target) {
    throw "Standalone repository already exists. 사용자 소유 코드를 보호하기 위해 덮어쓰지 않습니다: $target"
}

$effectiveModuleName = if ([string]::IsNullOrWhiteSpace($ModuleName)) {
    $domain.Substring(0, 1).ToUpperInvariant() + $domain.Substring(1).ToLowerInvariant()
} else {
    $ModuleName.Trim()
}
$effectivePackageName = if ([string]::IsNullOrWhiteSpace($PackageName)) {
    "com.cpf.$domain"
} else {
    $PackageName.Trim()
}
$effectiveTablePrefix = if ([string]::IsNullOrWhiteSpace($TablePrefix)) {
    $systemCodeNormalized.ToLowerInvariant()
} else {
    $TablePrefix.Trim().ToLowerInvariant()
}
$effectiveSchemaName = if ([string]::IsNullOrWhiteSpace($SchemaName)) {
    "${effectiveTablePrefix}DB"
} else {
    $SchemaName.Trim()
}

$federationConflicts = [System.Collections.Generic.List[string]]::new()
if (Test-Path -LiteralPath $outputRootAbsolute -PathType Container) {
    foreach ($existingManifestPath in Get-ChildItem -LiteralPath $outputRootAbsolute `
            -Directory -Filter "cpf-domain-*" -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "cpf-domain-manifest.json" } |
            Where-Object { Test-Path -LiteralPath $_ -PathType Leaf }) {
        try {
            $existingRepositoryManifest = Get-Content -LiteralPath $existingManifestPath `
                -Raw -Encoding UTF8 | ConvertFrom-Json
            $existingModulePath = Join-Path (Split-Path -Parent $existingManifestPath) `
                ([string]$existingRepositoryManifest.domainModule)
            $existingDomainManifestPath = Join-Path $existingModulePath "manifest/domain-manifest.json"
            $existingDomainManifest = Get-Content -LiteralPath $existingDomainManifestPath `
                -Raw -Encoding UTF8 | ConvertFrom-Json
            if ([string]$existingDomainManifest.systemCode -eq $systemCodeNormalized) {
                $federationConflicts.Add(
                        "SystemCode가 기존 독립 Domain과 중복됩니다: $systemCodeNormalized ($existingManifestPath)")
            }
            if ([string]$existingDomainManifest.packageName -eq $effectivePackageName) {
                $federationConflicts.Add(
                        "PackageName이 기존 독립 Domain과 중복됩니다: $effectivePackageName ($existingManifestPath)")
            }
            if ([string]$existingDomainManifest.schemaName -eq $effectiveSchemaName) {
                $federationConflicts.Add(
                        "SchemaName이 기존 독립 Domain과 중복됩니다: $effectiveSchemaName ($existingManifestPath)")
            }
            if ([string]$existingDomainManifest.tablePrefix -eq $effectiveTablePrefix) {
                $federationConflicts.Add(
                        "TablePrefix가 기존 독립 Domain과 중복됩니다: $effectiveTablePrefix ($existingManifestPath)")
            }
            if ([int]$existingDomainManifest.port -eq $Port) {
                $federationConflicts.Add(
                        "Port가 기존 독립 Domain과 중복됩니다: $Port ($existingManifestPath)")
            }
        } catch {
            $federationConflicts.Add("기존 독립 Domain metadata를 해석할 수 없습니다: $existingManifestPath")
        }
    }
}
if ($federationConflicts.Count -gt 0) {
    throw ($federationConflicts -join [Environment]::NewLine)
}

$requestedCapabilities = [System.Collections.Generic.List[string]]::new()
foreach ($capability in @($Capabilities -split '[,; ]+')) {
    if (-not [string]::IsNullOrWhiteSpace($capability) -and
            -not $requestedCapabilities.Contains($capability.Trim().ToLowerInvariant())) {
        $requestedCapabilities.Add($capability.Trim().ToLowerInvariant())
    }
}
if ($Batch -and -not $requestedCapabilities.Contains("batch")) {
    $requestedCapabilities.Add("batch")
}
if ($CenterCut -and -not $requestedCapabilities.Contains("center-cut")) {
    $requestedCapabilities.Add("center-cut")
}
$capabilityText = $requestedCapabilities -join ','

$createScript = Join-Path $PSScriptRoot "create-domain.ps1"
$exportScript = Join-Path $PSScriptRoot "export-domain-repository.ps1"
$jobPackScript = Join-Path $PSScriptRoot "create-domain-jobpack.ps1"
$removeScript = Join-Path $repositoryRoot "cpf-tools/scripts/remove-domain.ps1"
$removeRepositoryScript = Join-Path $PSScriptRoot "remove-domain-repository.ps1"
$verifyFederationScript = Join-Path $PSScriptRoot "verify-domain-federation.ps1"

$generatorArguments = @(
    "-DomainName", $domain,
    "-SystemCode", $systemCodeNormalized,
    "-DatabaseVendor", $DatabaseVendor,
    "-Capabilities", $capabilityText,
    "-DependencyModel", "published-artifact",
    "-PlatformVersion", $PlatformVersion,
    "-ModuleName", $effectiveModuleName,
    "-PackageName", $effectivePackageName,
    "-SchemaName", $effectiveSchemaName,
    "-TablePrefix", $effectiveTablePrefix,
    "-Port", $Port
)
if ($DryRun) {
    Invoke-CheckedPowerShell -Script $createScript -Arguments ($generatorArguments + "-DryRun")
    [ordered]@{
        status = "READY"
        dryRun = $true
        domainName = $domain
        systemCode = $systemCodeNormalized
        target = $target
        dependencyModel = "published-artifact"
        databaseVendor = $DatabaseVendor
        capabilities = @($requestedCapabilities)
        moduleName = $effectiveModuleName
        packageName = $effectivePackageName
        schemaName = $effectiveSchemaName
        tablePrefix = $effectiveTablePrefix
        port = $Port
        artifactMode = $effectiveArtifactMode
        artifactRepository = if ($effectiveArtifactMode -eq 'REMOTE') {
            $env:CPF_ARTIFACT_REPOSITORY_URL
        } elseif ($effectiveArtifactMode -eq 'OFFLINE') {
            $env:CPF_OFFLINE_ARTIFACT_REPOSITORY
        } elseif (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
            $env:CPF_LOCAL_ARTIFACT_REPOSITORY
        } else {
            (Join-Path $HOME ".cpf/repository")
        }
    } | ConvertTo-Json -Depth 10
    return
}

$primaryFailure = $null
try {
    # 독립 Repository가 생성 직후 같은 CPF build의 public JAR/BOM을 사용하도록
    # 원격 Registry가 없는 로컬 환경에서는 공용 local Maven repository를 먼저 동기화합니다.
    if (-not $SkipBuild) {
        Invoke-CpfLocalArtifactPublish
    }

    Invoke-CheckedPowerShell -Script $createScript -Arguments ($generatorArguments + "-Apply")

    Invoke-CheckedPowerShell -Script $exportScript -Arguments @(
        "-DomainModule", $module,
        "-SystemCode", $systemCodeNormalized,
        "-PlatformVersion", $PlatformVersion,
        "-OutputRoot", $orchestrationOutputRoot,
        "-SkipBuild"
    )

    if ($Batch -or $CenterCut) {
        Invoke-CheckedPowerShell -Script $jobPackScript -Arguments @(
            "-RepositoryRoot", $orchestrationTarget,
            "-DomainName", $domain,
            "-SystemCode", $systemCodeNormalized,
            "-PlatformVersion", $PlatformVersion
        )
    }

    Invoke-CheckedPowerShell -Script $verifyFederationScript -Arguments @(
        "-RepoRoot", $orchestrationTarget
    )

    if (-not $SkipBuild) {
        $gradle = if ($IsWindows) {
            Join-Path $orchestrationTarget "gradlew.bat"
        } else {
            Join-Path $orchestrationTarget "gradlew"
        }
        if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
            throw "Standalone Gradle Wrapper가 없습니다: $gradle"
        }
        Push-Location $orchestrationTarget
        try {
            & $gradle clean test verifyCpfPackagedDependencies --refresh-dependencies --no-daemon --max-workers=1 --console=plain
            if ($LASTEXITCODE -ne 0) {
                throw "Standalone domain repository build failed. exitCode=$LASTEXITCODE"
            }
        } finally {
            Pop-Location
        }
    }

    if (Test-Path -LiteralPath $target) {
        throw "Standalone 최종 경로가 orchestration 중 생성되었습니다: $target"
    }
    Move-Item -LiteralPath $orchestrationTarget -Destination $target
    if ((Test-Path -LiteralPath $orchestrationOutputRoot -PathType Container) -and
            @(Get-ChildItem -LiteralPath $orchestrationOutputRoot -Force).Count -eq 0) {
        Remove-Item -LiteralPath $orchestrationOutputRoot -Force
    }
    Write-Host "Standalone domain repository: $target"
} catch {
    $primaryFailure = $_
} finally {
    if (Test-Path -LiteralPath $orchestrationTarget -PathType Container) {
        try {
            $repositoryRemoveResultDir = Join-Path $repositoryRoot `
                "build/reports/remove-domain-repository/$domain-orchestration"
            Invoke-CheckedPowerShell -Script $removeRepositoryScript -Arguments @(
                "-DomainName", $domain,
                "-SystemCode", $systemCodeNormalized,
                "-RepositoryRoot", $orchestrationOutputRoot,
                "-ResultDir", $repositoryRemoveResultDir,
                "-DryRun"
            )
            $repositoryRemoveResultPath = Join-Path $repositoryRemoveResultDir `
                "remove-domain-repository-result.json"
            $repositoryRemoveResult = Get-Content -LiteralPath $repositoryRemoveResultPath `
                -Raw -Encoding UTF8 | ConvertFrom-Json
            if ([string]$repositoryRemoveResult.status -ne "READY") {
                throw "Standalone orchestration Repository 공식 제거가 차단되었습니다. result=$repositoryRemoveResultPath"
            }
            Invoke-CheckedPowerShell -Script $removeRepositoryScript -Arguments @(
                "-DomainName", $domain,
                "-SystemCode", $systemCodeNormalized,
                "-RepositoryRoot", $orchestrationOutputRoot,
                "-ResultDir", $repositoryRemoveResultDir
            )
        } catch {
            if ($null -eq $primaryFailure) {
                $primaryFailure = $_
            } else {
                Write-Error "Primary failure 뒤 standalone orchestration cleanup도 실패했습니다: $($_.Exception.Message)"
            }
        }
    }
    if ((Test-Path -LiteralPath $orchestrationOutputRoot -PathType Container) -and
            @(Get-ChildItem -LiteralPath $orchestrationOutputRoot -Force).Count -eq 0) {
        Remove-Item -LiteralPath $orchestrationOutputRoot -Force
    }
    if (Test-Path -LiteralPath $temporaryModule) {
        try {
            $removeResultDir = Join-Path $repositoryRoot "build/reports/remove-domain/$domain"
            Invoke-CheckedPowerShell -Script $removeScript -Arguments @(
                "-DomainName", $domain,
                "-SystemCode", $systemCodeNormalized,
                "-Root", $repositoryRoot,
                "-ResultDir", $removeResultDir,
                "-DryRun"
            )
            $removeResultPath = Join-Path $removeResultDir "remove-domain-result.json"
            $removeResult = Get-Content -LiteralPath $removeResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
            if ([string]$removeResult.status -ne "READY") {
                throw "Temporary Domain 공식 제거가 차단되었습니다. result=$removeResultPath"
            }
            Invoke-CheckedPowerShell -Script $removeScript -Arguments @(
                "-DomainName", $domain,
                "-SystemCode", $systemCodeNormalized,
                "-Root", $repositoryRoot,
                "-ResultDir", $removeResultDir
            )
        } catch {
            if ($null -eq $primaryFailure) {
                $primaryFailure = $_
            } else {
                Write-Error "Primary failure 뒤 temporary Domain cleanup도 실패했습니다: $($_.Exception.Message)"
            }
        }
    }
}

if ($null -ne $primaryFailure) {
    throw $primaryFailure
}
