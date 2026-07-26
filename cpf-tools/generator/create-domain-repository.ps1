param(
    [Parameter(Mandatory=$true)][string] $DomainName,
    [Parameter(Mandatory=$true)][ValidatePattern('^[A-Z]{3}$')][string] $SystemCode,
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [switch] $Batch,
    [switch] $CenterCut,
    [switch] $SkipBuild
)
$ErrorActionPreference = "Stop"
$repo = (Resolve-Path "$PSScriptRoot\..\..").Path
$module = "cpf-$($DomainName.ToLowerInvariant())"
$stage = Join-Path $repo "build\domain-generator\$module"
$temporary = Join-Path $repo $module
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
if (Test-Path $temporary) { throw "Root module already exists: $module" }

$batchFlag = if ($Batch) { "Y" } else { "N" }
$centerCutFlag = if ($CenterCut) { "Y" } else { "N" }

& (Join-Path $PSScriptRoot "create-domain.ps1") `
    -DomainName $DomainName `
    -SystemCode $SystemCode `
    -OutputDir $stage `
    -DatabaseVendor mariadb `
    -Batch $batchFlag `
    -CenterCut $centerCutFlag
if ($LASTEXITCODE -ne 0) { throw "Domain template generation failed." }

Move-Item $stage $temporary
try {
    & (Join-Path $PSScriptRoot "export-domain-repository.ps1") `
        -DomainModule $module `
        -SystemCode $SystemCode `
        -PlatformVersion $PlatformVersion `
        -SkipBuild
    $target = Join-Path (Join-Path $repo 'build\domain-repositories') "cpf-domain-$($DomainName.ToLowerInvariant())"
    if ($Batch -or $CenterCut) {
        & (Join-Path $PSScriptRoot 'create-domain-jobpack.ps1') -RepositoryRoot $target -DomainName $DomainName -SystemCode $SystemCode -PlatformVersion $PlatformVersion
    }
    if (-not $SkipBuild) {
        Push-Location $target
        try {
            if ($IsWindows) { & .\gradlew.bat clean test --no-daemon }
            else { & ./gradlew clean test --no-daemon }
            if ($LASTEXITCODE -ne 0) { throw 'Standalone domain repository build failed.' }
        } finally { Pop-Location }
    }
} finally {
    if (Test-Path $temporary) { Remove-Item $temporary -Recurse -Force }
}
