param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9]{1,29}$')]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $RepositoryRoot = "",
    [string] $ResultDir = "",
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$cpfRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$domain = $DomainName.Trim().ToLowerInvariant()
$normalizedSystemCode = $SystemCode.Trim().ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($normalizedSystemCode) -and
        $normalizedSystemCode -notmatch '^[A-Z][A-Z0-9]{2}$') {
    throw "SystemCode는 영문자로 시작하는 정확히 3자리 영문 대문자·숫자여야 합니다."
}
if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = Join-Path $cpfRoot "build/domain-repositories"
}
$repositoryRootResolved = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$target = Join-Path $repositoryRootResolved "cpf-domain-$domain"
$targetResolved = if (Test-Path -LiteralPath $target) {
    (Resolve-Path -LiteralPath $target).Path
} else {
    [IO.Path]::GetFullPath($target)
}
$allowedPrefix = [IO.Path]::GetFullPath($repositoryRootResolved).TrimEnd('\', '/') +
        [IO.Path]::DirectorySeparatorChar
if (-not $targetResolved.StartsWith($allowedPrefix, [StringComparison]::OrdinalIgnoreCase) -or
        (Split-Path -Leaf $targetResolved) -ne "cpf-domain-$domain") {
    throw "Standalone Repository 제거 경로가 허용 범위를 벗어났습니다: $targetResolved"
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $cpfRoot "build/reports/remove-domain-repository/$domain"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $cpfRoot $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "remove-domain-repository-result.json"

function Save-Result([object] $Result) {
    [IO.File]::WriteAllText(
            $resultPath,
            (($Result | ConvertTo-Json -Depth 30) + [Environment]::NewLine),
            $Utf8NoBom)
}

$ownershipPath = Join-Path $target "cpf-domain-ownership.json"
if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
    $failure = [ordered]@{
        status = "BLOCKED"
        dryRun = [bool]$DryRun
        domainName = $domain
        target = $target
        reason = "독립 Repository 소유권 manifest가 없어 안전한 자동 제거가 불가능합니다."
    }
    Save-Result $failure
    throw $failure.reason
}

$ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ([string]$ownership.domainName -ne $domain) {
    throw "요청 DomainName과 Repository ownership manifest가 다릅니다."
}
if (-not [string]::IsNullOrWhiteSpace($normalizedSystemCode) -and
        [string]$ownership.systemCode -ne $normalizedSystemCode) {
    throw "요청 SystemCode와 Repository ownership manifest가 다릅니다."
}

$ownedPaths = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$changedFiles = [System.Collections.Generic.List[object]]::new()
$missingFiles = [System.Collections.Generic.List[string]]::new()
foreach ($owned in @($ownership.generatedFiles)) {
    $relative = ([string]$owned.path).Replace('\', '/')
    [void]$ownedPaths.Add($relative)
    $absolute = Join-Path $target $relative
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        $missingFiles.Add($relative)
        continue
    }
    $currentHash = (Get-FileHash -LiteralPath $absolute -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($currentHash -ne ([string]$owned.sha256).ToLowerInvariant()) {
        $changedFiles.Add([ordered]@{
            path = $relative
            generatedSha256 = [string]$owned.sha256
            currentSha256 = $currentHash
        })
    }
}

$userOwnedFiles = @(Get-ChildItem -LiteralPath $target -Recurse -File |
        Where-Object {
            $relative = $_.FullName.Substring($target.Length + 1).Replace('\', '/')
            $_.FullName -ne $ownershipPath -and
            $relative -notmatch '^(?:build|\.gradle|logs?)(?:/|$)' -and
            -not $ownedPaths.Contains($relative)
        } |
        ForEach-Object { $_.FullName.Substring($target.Length + 1).Replace('\', '/') })
$blockReasons = [System.Collections.Generic.List[string]]::new()
if ($changedFiles.Count -gt 0) {
    $blockReasons.Add("Generator 소유 파일이 변경되었습니다.")
}
if ($userOwnedFiles.Count -gt 0) {
    $blockReasons.Add("Generator 소유가 아닌 사용자 파일이 있습니다.")
}

$result = [ordered]@{
    status = if ($blockReasons.Count -eq 0) { "READY" } else { "BLOCKED" }
    dryRun = [bool]$DryRun
    domainName = $domain
    systemCode = [string]$ownership.systemCode
    target = $target
    generatedFileCount = $ownedPaths.Count
    changedGeneratedFiles = @($changedFiles)
    missingGeneratedFiles = @($missingFiles)
    userOwnedFiles = $userOwnedFiles
    blockReasons = @($blockReasons)
    databaseObjectsRemoved = $false
}

if ($DryRun) {
    Save-Result $result
    Write-Host "remove-domain-repository dry-run status=$($result.status) result=$resultPath"
    return
}
if ($blockReasons.Count -gt 0) {
    Save-Result $result
    throw "사용자 변경이 있어 Standalone Repository 제거를 중단했습니다."
}

Remove-Item -LiteralPath $targetResolved -Recurse -Force
$result.status = "DONE"
$result.removedAt = (Get-Date).ToString("o")
Save-Result $result
Write-Host "remove-domain-repository completed. target=$targetResolved result=$resultPath"
