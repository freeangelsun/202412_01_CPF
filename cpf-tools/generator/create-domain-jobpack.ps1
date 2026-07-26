param(
    [Parameter(Mandatory = $true)]
    [string] $RepositoryRoot,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9]{1,29}$')]
    [string] $DomainName,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode,
    [ValidatePattern('^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = "1.0.0-SNAPSHOT"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$repositoryRootResolved = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$domain = $DomainName.Trim().ToLowerInvariant()
$module = "cpf-$domain-batch-jobpack"
$moduleRoot = Join-Path $repositoryRootResolved $module
if (Test-Path -LiteralPath $moduleRoot) {
    throw "Job Pack already exists: $moduleRoot"
}
$stagingRoot = Join-Path $repositoryRootResolved (".$module.staging-" + [guid]::NewGuid().ToString("N"))

$repositoryManifestPath = Join-Path $repositoryRootResolved "cpf-domain-manifest.json"
if (-not (Test-Path -LiteralPath $repositoryManifestPath -PathType Leaf)) {
    throw "CPF Generated Domain Repository manifest가 없습니다: $repositoryManifestPath"
}
$repositoryManifest = Get-Content -LiteralPath $repositoryManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ([string]$repositoryManifest.domainName -ne $domain -or
        [string]$repositoryManifest.systemCode -ne $SystemCode) {
    throw "Job Pack metadata가 Domain Repository manifest와 일치하지 않습니다."
}

function Write-Utf8 {
    param([string] $Path, [string] $Content)
    $parent = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

$package = "com.cpf.$domain.batch"
$packagePath = $package.Replace('.', '\')
$className = "${SystemCode}JobPackProvider"
$Dollar = '$'

$buildGradle = @"
plugins { id 'java-library' }
java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
def cpfPlatformVersion = providers.gradleProperty('cpfPlatformVersion').orElse('$PlatformVersion').get()
dependencies {
    implementation platform("com.cpf:cpf-bom:${Dollar}{cpfPlatformVersion}")
    implementation "com.cpf.core:cpf-core:${Dollar}{cpfPlatformVersion}"
    implementation "com.cpf.batch:cpf-batch-contract:${Dollar}{cpfPlatformVersion}"
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.4'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}
dependencyLocking { lockAllConfigurations() }
tasks.named('test') { useJUnitPlatform() }
"@
Write-Utf8 -Path (Join-Path $stagingRoot "build.gradle") -Content $buildGradle

$providerSource = @"
package $package;

import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.spi.BusinessJobProvider;
import java.util.List;
import java.util.Map;

/** Generated Job Pack entry. 고객 업무 Job/Step/Center-Cut Provider는 이 독립 Repository에서 확장합니다. */
public final class $className implements BusinessJobProvider {
    @Override
    public JobPackManifest manifest() {
        return new JobPackManifest(
                "$SystemCode-JOBPACK",
                "$SystemCode",
                "${package}:$module",
                "0.1.0-SNAPSHOT",
                "CALCULATED_BY_RELEASE_PIPELINE",
                null,
                "[$PlatformVersion,2.0.0)",
                List.of("GENERAL"),
                List.of(),
                Map.of("generatedDomain", "$domain"));
    }

    @Override
    public Object resolveJob(String jobId) {
        throw new IllegalArgumentException("Unknown jobId: " + jobId);
    }
}
"@
Write-Utf8 -Path (Join-Path $stagingRoot "src/main/java/$packagePath/$className.java") -Content $providerSource
Write-Utf8 -Path (Join-Path $stagingRoot "src/main/resources/META-INF/services/com.cpf.batch.spi.BusinessJobProvider") `
    -Content "$package.$className`n"

$providerTest = @"
package $package;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {
    @Test
    void exposesGeneratedDomainOwnership() {
        var manifest = new $className().manifest();
        assertThat(manifest.jobPackId()).isEqualTo("$SystemCode-JOBPACK");
        assertThat(manifest.ownerDomain()).isEqualTo("$SystemCode");
        assertThat(manifest.metadata()).containsEntry("generatedDomain", "$domain");
    }
}
"@
Write-Utf8 -Path (Join-Path $stagingRoot "src/test/java/$packagePath/${className}Test.java") -Content $providerTest

$jobPackMetadata = [ordered]@{
    manifestVersion = "1.0"
    domainName = $domain
    systemCode = $SystemCode
    moduleName = $module
    artifactCoordinate = "$package`:$module`:0.1.0-SNAPSHOT"
    checksumPolicy = "release-pipeline-sha256"
    signaturePolicy = "release-pipeline-ed25519"
    platformVersionRange = "[$PlatformVersion,2.0.0)"
}
Write-Utf8 -Path (Join-Path $stagingRoot "manifest/job-pack.json") `
    -Content (($jobPackMetadata | ConvertTo-Json -Depth 10) + [Environment]::NewLine)

$settingsPath = Join-Path $repositoryRootResolved "settings.gradle"
$settingsText = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
$originalSettingsText = $settingsText
if ($settingsText -match "(?m)^\s*include\s+['""]$([regex]::Escape($module))['""]\s*$") {
    throw "settings.gradle에 Job Pack module이 이미 등록되어 있습니다: $module"
}
$settingsText = $settingsText.TrimEnd() + [Environment]::NewLine +
        "include '$module'" + [Environment]::NewLine

# Job Pack도 Generator 소유 산출물이므로 독립 Repository 소유권 manifest를 원자적으로 갱신합니다.
$ownershipPath = Join-Path $repositoryRootResolved "cpf-domain-ownership.json"
if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
    throw "Domain Repository ownership manifest가 없습니다: $ownershipPath"
}
$ownershipText = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8
try {
    Move-Item -LiteralPath $stagingRoot -Destination $moduleRoot
    Write-Utf8 -Path $settingsPath -Content $settingsText

    $ownership = $ownershipText | ConvertFrom-Json
    $ownedFiles = @(Get-ChildItem -LiteralPath $repositoryRootResolved -Recurse -File |
            Where-Object {
                $relative = $_.FullName.Substring($repositoryRootResolved.Length + 1).Replace('\', '/')
                $_.FullName -ne $ownershipPath -and
                $relative -notmatch '^(?:build|\.gradle|logs?)(?:/|$)'
            } |
            Sort-Object FullName |
            ForEach-Object {
                [ordered]@{
                    path = $_.FullName.Substring($repositoryRootResolved.Length + 1).Replace('\', '/')
                    sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
                }
            })
    $ownership.generatedFiles = $ownedFiles
    $ownership | Add-Member -MemberType NoteProperty -Name "jobPackModule" -Value $module -Force
    Write-Utf8 -Path $ownershipPath -Content (($ownership | ConvertTo-Json -Depth 30) + [Environment]::NewLine)
} catch {
    Write-Utf8 -Path $settingsPath -Content $originalSettingsText
    Write-Utf8 -Path $ownershipPath -Content $ownershipText
    if (Test-Path -LiteralPath $moduleRoot -PathType Container) {
        Remove-Item -LiteralPath $moduleRoot -Recurse -Force
    }
    throw
} finally {
    if (Test-Path -LiteralPath $stagingRoot -PathType Container) {
        Remove-Item -LiteralPath $stagingRoot -Recurse -Force
    }
}

Write-Host "Generated Job Pack: $moduleRoot"
