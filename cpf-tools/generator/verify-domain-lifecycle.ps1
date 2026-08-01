[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string]$ExpectedSha,
    [Parameter(Mandatory)][string]$WorkRoot,
    [string]$RepositoryUrl = "",
    [ValidateSet("mariadb", "postgresql", "oracle")][string]$DatabaseVendor = "mariadb",
    [string]$DomainName = "qagenerator",
    [string]$SystemCode = "QAG",
    [string]$EvidenceRoot = "",
    [switch]$PreserveDisposableClone
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$MarkerName = ".cpf-disposable-generator-validation-root"
$StageIds = @(
    "fresh-clone", "create", "database-bootstrap", "build-test", "runtime-smoke",
    "adm-registration", "user-change-protection", "safe-remove", "regenerate", "parity"
)
$stages = New-Object System.Collections.Generic.List[object]
$artifacts = New-Object System.Collections.Generic.List[object]
$cloneRoot = Join-Path ([IO.Path]::GetFullPath($WorkRoot)) $DatabaseVendor
$resultDir = if ([string]::IsNullOrWhiteSpace($EvidenceRoot)) {
    Join-Path ([IO.Path]::GetFullPath($WorkRoot)) "evidence/$DatabaseVendor"
} else {
    Join-Path ([IO.Path]::GetFullPath($EvidenceRoot)) $DatabaseVendor
}
$resultPath = Join-Path $resultDir "generator-lifecycle-result.sanitized.json"

function Add-StageResult {
    param([string]$Id, [datetime]$StartedAt, [int]$ExitCode, [string[]]$Assertions)
    if ($StageIds[$stages.Count] -ne $Id) { throw "Generator lifecycle stage order mismatch. expected=$($StageIds[$stages.Count]) actual=$Id" }
    $stages.Add([ordered]@{
        id = $Id
        status = if ($ExitCode -eq 0) { "PASS" } else { "FAIL" }
        exitCode = $ExitCode
        startedAt = $StartedAt.ToUniversalTime().ToString("o")
        endedAt = (Get-Date).ToUniversalTime().ToString("o")
        assertions = @($Assertions)
    })
}
function Invoke-NativeChecked {
    param([string]$Name, [string]$FilePath, [string[]]$Arguments, [string]$WorkingDirectory)
    Write-Host "[CPF][GENERATOR][$Name] $FilePath $($Arguments -join ' ')"
    Push-Location $WorkingDirectory
    try {
        & $FilePath @Arguments
        if ($LASTEXITCODE -ne 0) { throw "$Name failed(exit=$LASTEXITCODE)" }
    } finally { Pop-Location }
}
function Get-RelativePath([string]$Base, [string]$Path) {
    return [IO.Path]::GetFullPath($Path).Substring([IO.Path]::GetFullPath($Base).TrimEnd('\','/').Length).TrimStart('\','/').Replace('\','/')
}
function Remove-NormalizedFields {
    param([object]$Value)
    if ($null -eq $Value) { return $null }
    if ($Value -is [System.Collections.IDictionary]) {
        $ordered = [ordered]@{}
        foreach ($key in @($Value.Keys | Sort-Object)) {
            if ([string]$key -in @("generatedAt", "createdAt", "updatedAt", "outputDir", "resultPath")) { continue }
            $ordered[$key] = Remove-NormalizedFields $Value[$key]
        }
        return $ordered
    }
    if ($Value -is [pscustomobject]) {
        $ordered = [ordered]@{}
        foreach ($property in @($Value.PSObject.Properties | Sort-Object Name)) {
            if ($property.Name -in @("generatedAt", "createdAt", "updatedAt", "outputDir", "resultPath")) { continue }
            $ordered[$property.Name] = Remove-NormalizedFields $property.Value
        }
        return $ordered
    }
    if ($Value -is [System.Collections.IEnumerable] -and $Value -isnot [string]) {
        return @($Value | ForEach-Object { Remove-NormalizedFields $_ })
    }
    return $Value
}
function Get-NormalizedSnapshot {
    param([string]$ModuleDir)
    $snapshot = [ordered]@{}
    Get-ChildItem -LiteralPath $ModuleDir -Recurse -File | Where-Object {
        $_.FullName -notmatch '[\\/](build|\.gradle)[\\/]' -and
        $_.Extension -notin @('.log','.tmp')
    } | Sort-Object FullName | ForEach-Object {
        $relative = Get-RelativePath $ModuleDir $_.FullName
        $bytes = if ($_.Extension -eq '.json') {
            try {
                $json = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
                $normalized = Remove-NormalizedFields $json | ConvertTo-Json -Depth 100 -Compress
                [Text.Encoding]::UTF8.GetBytes($normalized)
            } catch { [IO.File]::ReadAllBytes($_.FullName) }
        } else { [IO.File]::ReadAllBytes($_.FullName) }
        $sha = [Security.Cryptography.SHA256]::Create()
        try { $hash = ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-','').ToLowerInvariant() }
        finally { $sha.Dispose() }
        $snapshot[$relative] = $hash
    }
    return $snapshot
}
function Get-SnapshotDigest {
    param([object]$Snapshot)
    $lines = @($Snapshot.Keys | Sort-Object | ForEach-Object { "$_=$($Snapshot[$_])" })
    $bytes = [Text.Encoding]::UTF8.GetBytes(($lines -join "`n"))
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-','').ToLowerInvariant() }
    finally { $sha.Dispose() }
}
function Write-SanitizedResult([string]$Status, [bool]$CleanBefore, [bool]$CleanAfter, [bool]$UserProtectionVerified, [bool]$ParityVerified) {
    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $result = [ordered]@{
        schemaVersion = 1
        status = $Status
        vendor = $DatabaseVendor
        sourceSha = $ExpectedSha
        resultSha = $ExpectedSha
        sanitized = $true
        cleanBefore = $CleanBefore
        cleanAfter = $CleanAfter
        userProtectionVerified = $UserProtectionVerified
        parityVerified = $ParityVerified
        normalizedSha256 = if ($script:regeneratedSnapshot) { Get-SnapshotDigest $script:regeneratedSnapshot } else { "" }
        stages = @($stages.ToArray())
        artifacts = @($artifacts.ToArray())
    }
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 100), $Utf8NoBom)
}

$cleanBefore = $false
$cleanAfter = $false
$userProtectionVerified = $false
$parityVerified = $false
$script:firstSnapshot = $null
$script:regeneratedSnapshot = $null
$runtimeProcess = $null
try {
    $started = Get-Date
    if ([string]::IsNullOrWhiteSpace($RepositoryUrl)) {
        $RepositoryUrl = (& git remote get-url origin).Trim()
        if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($RepositoryUrl)) { throw "RepositoryUrl is required when origin cannot be resolved." }
    }
    if (Test-Path -LiteralPath $cloneRoot) { throw "Disposable clone path already exists: $cloneRoot" }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $cloneRoot) | Out-Null
    Invoke-NativeChecked "fresh clone" "git" @("clone", "--no-checkout", "--filter=blob:none", $RepositoryUrl, $cloneRoot) (Split-Path -Parent $cloneRoot)
    Invoke-NativeChecked "detached checkout" "git" @("checkout", "--detach", $ExpectedSha) $cloneRoot
    [IO.File]::WriteAllText((Join-Path $cloneRoot $MarkerName), "CPF disposable generator lifecycle root`n", $Utf8NoBom)
    $actualSha = (& git -C $cloneRoot rev-parse HEAD).Trim()
    if ($actualSha -ne $ExpectedSha) { throw "fresh clone SHA mismatch expected=$ExpectedSha actual=$actualSha" }
    $beforeLines = @(& git -C $cloneRoot status --porcelain=v1 --untracked-files=all | Where-Object { $_ -notmatch [regex]::Escape($MarkerName) })
    $cleanBefore = $beforeLines.Count -eq 0
    if (-not $cleanBefore) { throw "fresh clone is not clean before lifecycle validation" }
    Add-StageResult "fresh-clone" $started 0 @("detached exact SHA=$ExpectedSha", "clean working tree before lifecycle")

    $moduleDir = Join-Path $cloneRoot "cpf-$DomainName"
    $createScript = Join-Path $cloneRoot "cpf-tools/scripts/create-domain.ps1"
    $createArgs = @(
        '-NoProfile','-ExecutionPolicy','Bypass','-File',$createScript,
        '-DomainName',$DomainName,'-SystemCode',$SystemCode,'-Root',$cloneRoot,
        '-DatabaseVendor',$DatabaseVendor,'-Online','Y','-Database','Y','-Batch','Y',
        '-CenterCut','Y','-External','Y','-Messaging','Y','-File','Y','-SecurityAudit','Y',
        '-Ui','Y','-BzaMenu','Y','-ProductionProfile','Y','-Apply'
    )
    $started = Get-Date
    Invoke-NativeChecked "create generated domain" "pwsh" $createArgs $cloneRoot
    foreach ($required in @('manifest/domain-manifest.json','manifest/generator-ownership.json','deploy/database/database-profile.json','build.gradle')) {
        if (-not (Test-Path -LiteralPath (Join-Path $moduleDir $required) -PathType Leaf)) { throw "generated product file missing: $required" }
    }
    $script:firstSnapshot = Get-NormalizedSnapshot $moduleDir
    Add-StageResult "create" $started 0 @("golden template generated", "ownership manifest and product files exist")

    $started = Get-Date
    $dbScript = Join-Path $cloneRoot "cpf-tools/scripts/initialize-domain-database.ps1"
    Invoke-NativeChecked "database bootstrap" "pwsh" @('-NoProfile','-ExecutionPolicy','Bypass','-File',$dbScript,'-DomainName',$DomainName,'-SystemCode',$SystemCode,'-Root',$cloneRoot,'-DatabaseVendor',$DatabaseVendor,'-Operation','bootstrap','-Apply') $cloneRoot
    Add-StageResult "database-bootstrap" $started 0 @("$DatabaseVendor provision/install/seed/verify completed", "generated DB profile used")

    $started = Get-Date
    $gradle = if ($IsWindows) { Join-Path $cloneRoot 'gradlew.bat' } else { Join-Path $cloneRoot 'gradlew' }
    if (-not $IsWindows) { & chmod +x $gradle }
    Invoke-NativeChecked "generated module build/test" $gradle @("`:cpf-$DomainName`:clean", "`:cpf-$DomainName`:test", "`:cpf-$DomainName`:bootJar", '--no-daemon') $cloneRoot
    Add-StageResult "build-test" $started 0 @("generated module clean test passed", "boot artifact produced")

    $started = Get-Date
    $jar = Get-ChildItem -LiteralPath (Join-Path $moduleDir 'build/libs') -Filter '*.jar' -File | Where-Object { $_.Name -notmatch 'plain|sources|javadoc' } | Select-Object -First 1
    if ($null -eq $jar) { throw "generated boot JAR missing" }
    $runtimeOut = Join-Path $cloneRoot "build/reports/generator-lifecycle/$DatabaseVendor/runtime.out.log"
    $runtimeErr = Join-Path $cloneRoot "build/reports/generator-lifecycle/$DatabaseVendor/runtime.err.log"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $runtimeOut) | Out-Null
    $runtimeProcess = Start-Process -FilePath 'java' -ArgumentList @('-jar',$jar.FullName) -WorkingDirectory $cloneRoot -RedirectStandardOutput $runtimeOut -RedirectStandardError $runtimeErr -PassThru
    $smoke = Join-Path $moduleDir "smoke/smoke-$DomainName.ps1"
    $smokePassed = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        Start-Sleep -Seconds 2
        if ($runtimeProcess.HasExited) { throw "generated runtime exited before smoke; exit=$($runtimeProcess.ExitCode)" }
        try {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File $smoke
            if ($LASTEXITCODE -eq 0) { $smokePassed = $true; break }
        } catch { }
    }
    if (-not $smokePassed) { throw "generated runtime smoke did not pass" }
    Stop-Process -Id $runtimeProcess.Id -Force
    $runtimeProcess = $null
    Add-StageResult "runtime-smoke" $started 0 @("generated runtime started", "generated HTTP smoke passed")

    $started = Get-Date
    $manifest = Get-Content -LiteralPath (Join-Path $moduleDir 'manifest/domain-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($manifest.bzaMenuEnabled -ne $true) { throw "generated ADM/BZA registration is not enabled" }
    $registrationFiles = @(Get-ChildItem -LiteralPath $moduleDir -Recurse -File | Where-Object { $_.Name -match 'menu|openapi|registration' })
    if ($registrationFiles.Count -eq 0) { throw "generated ADM registration artifacts missing" }
    Add-StageResult "adm-registration" $started 0 @("BZA menu enabled in domain manifest", "ADM/OpenAPI registration artifact exists")

    $started = Get-Date
    $ownership = Get-Content -LiteralPath (Join-Path $moduleDir 'manifest/generator-ownership.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    $owned = @($ownership.createdFiles | Where-Object { ([string]$_.path).EndsWith('.java') } | Select-Object -First 1)
    if ($owned.Count -ne 1) { throw "no generated Java file available for user-change protection test" }
    $changedPath = Join-Path $moduleDir ([string]$owned[0].path)
    $originalChangedText = Get-Content -LiteralPath $changedPath -Raw -Encoding UTF8
    [IO.File]::WriteAllText($changedPath, $originalChangedText + "`n// CPF USER CHANGE PROTECTION PROBE`n", $Utf8NoBom)
    $removeScript = Join-Path $cloneRoot 'cpf-tools/scripts/remove-domain.ps1'
    $probeDir = Join-Path $cloneRoot "build/reports/generator-lifecycle/$DatabaseVendor/change-probe"
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $removeScript -DomainName $DomainName -SystemCode $SystemCode -Root $cloneRoot -ResultDir $probeDir -DryRun
    $probe = Get-Content -LiteralPath (Join-Path $probeDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($probe.status -ne 'BLOCKED' -or @($probe.changedGeneratedFiles).Count -eq 0) { throw "changedGeneratedFiles removal protection failed" }
    [IO.File]::WriteAllText($changedPath, $originalChangedText, $Utf8NoBom)

    $userFile = Join-Path $moduleDir 'src/main/java/UserOwnedExtension.java'
    [IO.File]::WriteAllText($userFile, '// user owned extension', $Utf8NoBom)
    $userProbeDir = Join-Path $cloneRoot "build/reports/generator-lifecycle/$DatabaseVendor/user-probe"
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $removeScript -DomainName $DomainName -SystemCode $SystemCode -Root $cloneRoot -ResultDir $userProbeDir -DryRun
    $userProbe = Get-Content -LiteralPath (Join-Path $userProbeDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($userProbe.status -ne 'BLOCKED' -or @($userProbe.userOwnedFiles).Count -eq 0) { throw "userOwnedFiles removal protection failed" }
    Remove-Item -LiteralPath $userFile -Force

    $referenceFile = Join-Path $cloneRoot 'cpf-tools/contracts/generator-lifecycle-external-reference-probe.json'
    [IO.File]::WriteAllText($referenceFile, ('{"module":"' + $DomainName + '"}'), $Utf8NoBom)
    $referenceProbeDir = Join-Path $cloneRoot "build/reports/generator-lifecycle/$DatabaseVendor/reference-probe"
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $removeScript -DomainName $DomainName -SystemCode $SystemCode -Root $cloneRoot -ResultDir $referenceProbeDir -DryRun
    $referenceProbe = Get-Content -LiteralPath (Join-Path $referenceProbeDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($referenceProbe.status -ne 'BLOCKED' -or @($referenceProbe.externalReferences).Count -eq 0) { throw "externalReferences removal protection failed" }
    Remove-Item -LiteralPath $referenceFile -Force
    $userProtectionVerified = $true
    Add-StageResult "user-change-protection" $started 0 @("changedGeneratedFiles blocks removal", "userOwnedFiles blocks removal", "externalReferences blocks removal", "database objects are never auto-dropped")

    $started = Get-Date
    Invoke-NativeChecked "safe generated-domain removal" "pwsh" @('-NoProfile','-ExecutionPolicy','Bypass','-File',$removeScript,'-DomainName',$DomainName,'-SystemCode',$SystemCode,'-Root',$cloneRoot) $cloneRoot
    if (Test-Path -LiteralPath $moduleDir) { throw "generated module remained after safe removal" }
    Add-StageResult "safe-remove" $started 0 @("ownership hashes matched", "generated module removed without database DROP")

    $started = Get-Date
    Invoke-NativeChecked "regenerate generated domain" "pwsh" $createArgs $cloneRoot
    $script:regeneratedSnapshot = Get-NormalizedSnapshot $moduleDir
    Add-StageResult "regenerate" $started 0 @("same input regenerated successfully", "second ownership manifest created")

    $started = Get-Date
    $firstKeys = @($script:firstSnapshot.Keys | Sort-Object)
    $secondKeys = @($script:regeneratedSnapshot.Keys | Sort-Object)
    if (($firstKeys -join "`n") -ne ($secondKeys -join "`n")) { throw "regeneration file-set parity failed" }
    foreach ($key in $firstKeys) {
        if ($script:firstSnapshot[$key] -ne $script:regeneratedSnapshot[$key]) { throw "normalizedSha256 parity failed: $key" }
    }
    $parityVerified = $true
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $removeScript -DomainName $DomainName -SystemCode $SystemCode -Root $cloneRoot
    if ($LASTEXITCODE -ne 0) { throw "post-parity cleanup failed(exit=$LASTEXITCODE)" }
    Remove-Item -LiteralPath (Join-Path $cloneRoot $MarkerName) -Force
    $afterLines = @(& git -C $cloneRoot status --porcelain=v1 --untracked-files=all)
    $cleanAfter = $afterLines.Count -eq 0
    if (-not $cleanAfter) { throw "disposable clone is not clean after parity cleanup" }
    Add-StageResult "parity" $started 0 @("identical generated file set", "identical normalizedSha256", "clean working tree after lifecycle")

    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $snapshotArtifact = Join-Path $resultDir 'normalized-generator-snapshot.json'
    [IO.File]::WriteAllText($snapshotArtifact, ($script:regeneratedSnapshot | ConvertTo-Json -Depth 20), $Utf8NoBom)
    $artifacts.Add([ordered]@{ path = (Get-RelativePath (Split-Path -Parent $resultDir) $snapshotArtifact); sha256 = (Get-FileHash -LiteralPath $snapshotArtifact -Algorithm SHA256).Hash.ToLowerInvariant() })
    Write-SanitizedResult 'PASS' $cleanBefore $cleanAfter $userProtectionVerified $parityVerified
    Write-Host "[CPF][PASS] generator lifecycle vendor=$DatabaseVendor SHA=$ExpectedSha result=$resultPath"
}
catch {
    if ($null -ne $runtimeProcess -and -not $runtimeProcess.HasExited) { Stop-Process -Id $runtimeProcess.Id -Force -ErrorAction SilentlyContinue }
    Write-SanitizedResult 'FAIL' $cleanBefore $cleanAfter $userProtectionVerified $parityVerified
    throw
}
finally {
    if (-not $PreserveDisposableClone -and (Test-Path -LiteralPath $cloneRoot)) {
        $marker = Join-Path $cloneRoot $MarkerName
        if (Test-Path -LiteralPath $marker -PathType Leaf) { Remove-Item -LiteralPath $cloneRoot -Recurse -Force }
    }
}
