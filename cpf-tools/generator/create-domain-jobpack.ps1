param(
 [Parameter(Mandatory=$true)][string]$RepositoryRoot,
 [Parameter(Mandatory=$true)][string]$DomainName,
 [Parameter(Mandatory=$true)][ValidatePattern('^[A-Z]{3}$')][string]$SystemCode,
 [string]$PlatformVersion='1.0.0-SNAPSHOT'
)
$ErrorActionPreference='Stop'
$module="cpf-$($DomainName.ToLowerInvariant())-batch-jobpack";$root=Join-Path $RepositoryRoot $module
if(Test-Path $root){throw "Job Pack already exists: $root"}
$package="com.cpf.$($DomainName.ToLowerInvariant()).batch";$pkgPath=$package.Replace('.','\')
New-Item -ItemType Directory -Force -Path (Join-Path $root "src\main\java\$pkgPath")|Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $root 'src\main\resources\META-INF\services')|Out-Null
@"
plugins { id 'java-library' }
java { toolchain { languageVersion=JavaLanguageVersion.of(25) } }
def cpfPlatformVersion=providers.gradleProperty('cpfPlatformVersion').orElse('$PlatformVersion').get()
dependencies {
 implementation platform("com.cpf:cpf-bom:${cpfPlatformVersion}")
 implementation "com.cpf.core:cpf-core:${cpfPlatformVersion}"
 implementation "com.cpf.batch:cpf-batch-contract:${cpfPlatformVersion}"
 implementation 'org.springframework.batch:spring-batch-core'
}
dependencyLocking { lockAllConfigurations() }
"@|Set-Content -Encoding UTF8 (Join-Path $root 'build.gradle')
$className="${SystemCode}JobPackProvider"
@"
package $package;
import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.spi.BusinessJobProvider;
/** Generated Job Pack entry. Add approved Job/Step/Center-Cut providers inside this Domain repository. */
public final class $className implements BusinessJobProvider {
 public JobPackManifest manifest(){return new JobPackManifest("$SystemCode-JOBPACK","$SystemCode","$package:$module","0.1.0-SNAPSHOT","GENERATED_AT_BUILD",null,"[$PlatformVersion,2.0.0)",java.util.List.of("GENERAL"),java.util.List.of(),java.util.Map.of());}
 public Object resolveJob(String jobId){throw new IllegalArgumentException("Unknown jobId: "+jobId);}
}
"@|Set-Content -Encoding UTF8 (Join-Path $root "src\main\java\$pkgPath\$className.java")
"$package.$className"|Set-Content -Encoding ASCII (Join-Path $root 'src\main\resources\META-INF\services\com.cpf.batch.spi.BusinessJobProvider')
$settings=Join-Path $RepositoryRoot 'settings.gradle';Add-Content -Encoding UTF8 -Path $settings -Value "`ninclude '$module'"
Write-Host "Generated Job Pack: $root"
