<#
.SYNOPSIS
CPF Java/Gradle/Spring Boot 기술 Stack 정본과 현재 상용 Release 가능 상태를 검증합니다.
.DESCRIPTION
gradle/cpf-stack.properties, Gradle Wrapper, Root/Module/Generator의 Version 중복을 검사합니다.
현재 Stack이 TRANSITION이면 일반 검증은 상태를 출력하지만 -RequireSupported는 실패합니다.
.PARAMETER Root
CPF Repository Root. 기본값은 Script 기준 Repository Root입니다.
.PARAMETER RequireSupported
공식 지원 Matrix와 stackState=SUPPORTED_GA를 모두 요구합니다. Commercial Release Gate에서 사용합니다.
.EXAMPLE
pwsh -File .\cpf-tools\verification\tools\check-cpf-stack-support.ps1
.EXAMPLE
pwsh -File .\cpf-tools\verification\tools\check-cpf-stack-support.ps1 -RequireSupported
#>
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $RequireSupported
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Read-Properties([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw "Properties file not found: $Path" }
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
        $index = $trimmed.IndexOf('=')
        if ($index -le 0) { continue }
        $values[$trimmed.Substring(0, $index).Trim()] = $trimmed.Substring($index + 1).Trim()
    }
    return $values
}

$stackPath = Join-Path $Root 'gradle/cpf-stack.properties'
$stack = Read-Properties $stackPath
$required = @('javaVersion','gradleVersion','springBootVersion','springDependencyManagementVersion','springBootTargetVersion','stackState')
foreach ($key in $required) {
    if (-not $stack.ContainsKey($key) -or [string]::IsNullOrWhiteSpace([string]$stack[$key])) {
        throw "CPF stack property is missing: $key"
    }
}

$wrapperPath = Join-Path $Root 'gradle/wrapper/gradle-wrapper.properties'
$wrapper = Read-Properties $wrapperPath
$distributionUrl = [string]$wrapper['distributionUrl']
if ($distributionUrl -notmatch "gradle-$([regex]::Escape([string]$stack['gradleVersion']))-bin\.zip$") {
    throw "Gradle wrapper version mismatch. canonical=$($stack['gradleVersion']) distributionUrl=$distributionUrl"
}

$rootBuild = Get-Content -LiteralPath (Join-Path $Root 'build.gradle') -Raw -Encoding UTF8
$settings = Get-Content -LiteralPath (Join-Path $Root 'settings.gradle') -Raw -Encoding UTF8
$generator = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/generator/engine/cpf_domain_generator.py') -Raw -Encoding UTF8
$exporter = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/generator/export-domain-repository.ps1') -Raw -Encoding UTF8

$forbiddenLiterals = @(
    "id 'org.springframework.boot' version '$($stack['springBootVersion'])'",
    "spring-boot-dependencies:$($stack['springBootVersion'])",
    "id 'io.spring.dependency-management' version '$($stack['springDependencyManagementVersion'])'"
)
foreach ($literal in $forbiddenLiterals) {
    if ($rootBuild.Contains($literal)) { throw "Root build contains duplicated stack version literal: $literal" }
}
$generatedDefinitionRoot = $Root
$generatedRootPrefixes = @(
    Get-ChildItem -LiteralPath $generatedDefinitionRoot -Directory -ErrorAction SilentlyContinue |
        ForEach-Object {
            [IO.Path]::GetFullPath((Join-Path $Root "cpf-$($_.Name)")).TrimEnd('\', '/') +
                    [IO.Path]::DirectorySeparatorChar
        }
)
$buildFiles = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Filter 'build.gradle' | Where-Object {
    $candidate = [IO.Path]::GetFullPath($_.FullName)
    $_.FullName -notmatch '[\\/](build|\.gradle|local-domains)[\\/]' -and
            @($generatedRootPrefixes | Where-Object {
                $candidate.StartsWith($_, [StringComparison]::OrdinalIgnoreCase)
            }).Count -eq 0
})
foreach ($buildFile in $buildFiles) {
    $text = Get-Content -LiteralPath $buildFile.FullName -Raw -Encoding UTF8
    if ($text -match "id\s+'org\.springframework\.boot'\s+version\s+" -or
            $text -match "id\s+'io\.spring\.dependency-management'\s+version\s+" -or
            $text -match 'spring-boot-dependencies:[0-9]') {
        throw "Duplicated Spring stack version literal in build file: $($buildFile.FullName)"
    }
}
if (-not $settings.Contains("cpf-stack.properties")) { throw 'settings.gradle does not resolve plugin versions from cpf-stack.properties.' }
if (-not $generator.Contains('gradle/cpf-stack.properties') -or
        -not $generator.Contains('def read_stack(')) {
    throw 'Canonical Generator Engine does not resolve the CPF stack source.'
}
if (-not $exporter.Contains('Invoke-CpfCanonicalCli') -or
        -not $exporter.Contains('cpfPlatformVersion') -or
        -not $exporter.Contains('gradle.properties')) {
    throw 'Standalone exporter does not preserve canonical Generated Domain stack/version inputs.'
}

$java = [int]$stack['javaVersion']
$maxJava = if ($stack.ContainsKey('springBootMaxSupportedJava')) { [int]$stack['springBootMaxSupportedJava'] } else { $java }
$gradleMajor = [int](([string]$stack['gradleVersion']).Split('.')[0])
$maxGradleMajor = if ($stack.ContainsKey('springBootSupportedGradleMajorMax')) { [int]$stack['springBootSupportedGradleMajorMax'] } else { $gradleMajor }
$officiallySupportedCurrent = ($java -le $maxJava -and $gradleMajor -le $maxGradleMajor)
$state = ([string]$stack['stackState']).ToUpperInvariant()
if ($state -eq 'SUPPORTED_GA' -and -not $officiallySupportedCurrent) {
    throw "stackState=SUPPORTED_GA contradicts declared support matrix. java=$java maxJava=$maxJava gradleMajor=$gradleMajor maxGradleMajor=$maxGradleMajor"
}
if ($RequireSupported -and (-not $officiallySupportedCurrent -or $state -ne 'SUPPORTED_GA')) {
    throw "CPF stack is not commercial-release eligible. state=$state currentBoot=$($stack['springBootVersion']) targetBoot=$($stack['springBootTargetVersion'])"
}

[ordered]@{
    status = if ($officiallySupportedCurrent -and $state -eq 'SUPPORTED_GA') { 'SUPPORTED_GA' } else { 'TRANSITION' }
    stackState = $state
    javaVersion = $java
    gradleVersion = $stack['gradleVersion']
    springBootVersion = $stack['springBootVersion']
    springBootTargetVersion = $stack['springBootTargetVersion']
    officiallySupportedCurrent = $officiallySupportedCurrent
    releaseEligible = ($officiallySupportedCurrent -and $state -eq 'SUPPORTED_GA')
} | ConvertTo-Json -Depth 5
