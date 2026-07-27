param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9]{1,29}$')]
    [string] $DomainName,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode,
    [ValidatePattern('^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [ValidateSet("mariadb", "mysql", "postgresql", "oracle", "sqlserver")]
    [string] $DatabaseVendor = "mariadb",
    [string] $ModuleName = "",
    [string] $PackageName = "",
    [string] $SchemaName = "",
    [string] $TablePrefix = "",
    [ValidateRange(1024, 65535)]
    [int] $Port = 8080,
    [string] $Capabilities = "database,local-call",
    [string] $OutputRoot = "build/domain-repositories",
    [switch] $Batch,
    [switch] $CenterCut,
    [switch] $SkipBuild,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$repositoryRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
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
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $Script @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "CPF script failed. script=$Script exitCode=$LASTEXITCODE"
    }
}

function Invoke-CpfLocalArtifactPublish {
    if (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
        return
    }
    $localRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        [IO.Path]::GetFullPath($env:CPF_LOCAL_ARTIFACT_REPOSITORY)
    } else {
        [IO.Path]::GetFullPath((Join-Path $HOME ".cpf/repository"))
    }
    $env:CPF_LOCAL_ARTIFACT_REPOSITORY = $localRepository
    $gradle = if ($IsWindows) {
        Join-Path $repositoryRoot "gradlew.bat"
    } else {
        Join-Path $repositoryRoot "gradlew"
    }
    if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
        throw "CPF Gradle Wrapper가 없습니다: $gradle"
    }
    Push-Location $repositoryRoot
    try {
        & $gradle publishCpfLocalPlatformArtifacts --no-daemon --max-workers=1 --console=plain
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
        artifactRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
            $env:CPF_ARTIFACT_REPOSITORY_URL
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
