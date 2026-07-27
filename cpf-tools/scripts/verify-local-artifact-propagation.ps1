param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $LocalRepository = ""
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($LocalRepository)) {
    $LocalRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        $env:CPF_LOCAL_ARTIFACT_REPOSITORY
    } else {
        Join-Path $HOME ".cpf/repository"
    }
}
$LocalRepository = [IO.Path]::GetFullPath($LocalRepository)

$platformProperties = Join-Path $Root "gradle/cpf-platform.properties"
if (-not (Test-Path -LiteralPath $platformProperties -PathType Leaf)) {
    throw "CPF platform properties가 없습니다: $platformProperties"
}
$versionLine = Get-Content -LiteralPath $platformProperties -Encoding UTF8 |
        Where-Object { $_ -match '^\s*platformVersion\s*=' } |
        Select-Object -First 1
if ($null -eq $versionLine) {
    throw "platformVersion을 찾을 수 없습니다: $platformProperties"
}
$version = ($versionLine -split '=', 2)[1].Trim()

$expected = @(
    "com/cpf/core/cpf-core/$version/cpf-core-$version.jar",
    "com/cpf/common/cpf-common/$version/cpf-common-$version.jar",
    "com/cpf/batch/cpf-batch-contract/$version/cpf-batch-contract-$version.jar",
    "com/cpf/cpf-bom/$version/cpf-bom-$version.pom",
    "com/cpf/build/cpf-gradle-plugin/$version/cpf-gradle-plugin-$version.jar"
)

$missing = @()
foreach ($relative in $expected) {
    $candidate = Join-Path $LocalRepository ($relative.Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
        $missing += $relative
    }
}

$result = [ordered]@{
    status = if ($missing.Count -eq 0) { "DONE" } else { "FAILED" }
    repository = $LocalRepository
    platformVersion = $version
    expected = $expected
    missing = $missing
}
$result | ConvertTo-Json -Depth 10

if ($missing.Count -gt 0) {
    throw "CPF local artifact repository가 불완전합니다. 먼저 gradlew publishCpfLocalPlatformArtifacts 를 실행하십시오. missing=$($missing -join ', ')"
}
