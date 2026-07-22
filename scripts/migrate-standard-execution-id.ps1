param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $Apply
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$LegacyPattern = [regex]'([OB])([A-Z]{3})-([A-Z0-9]{3})-([A-Z0-9]{2})-([0-9]{4})'
$CurrentPattern = [regex]'^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/verification/standard-execution-id-migration'
}
[System.IO.Directory]::CreateDirectory($ResultDir) | Out-Null

function Resolve-FeatureCode {
    param(
        [string] $Domain,
        [string] $Business
    )

    $semanticCodes = @{
        'REF:EDU' = 'AA'
        'REF:QRY' = 'QR'
        'MBR:BSE' = 'MB'
        'MBR:AUT' = 'AU'
        'BZA:USR' = 'US'
        'BZA:AUD' = 'UD'
        'BAT:OPS' = 'OP'
        'BAT:CUT' = 'CU'
    }
    $key = "$Domain`:$Business"
    if ($semanticCodes.ContainsKey($key)) {
        return $semanticCodes[$key]
    }
    return $Business.Substring(0, 2)
}

function Get-TargetFiles {
    param([string] $BasePath)

    $allowedExtensions = @('.java', '.xml', '.yml', '.yaml', '.sql', '.json', '.ps1', '.gradle')
    $roots = @('cpf', 'cmn', 'mbr', 'ref', 'adm', 'bza', 'bat', 'scripts', 'specs/sql', 'deploy')
    $files = foreach ($relativeRoot in $roots) {
        $path = Join-Path $BasePath $relativeRoot
        if (Test-Path -LiteralPath $path) {
            Get-ChildItem -LiteralPath $path -Recurse -File | Where-Object {
                $allowedExtensions -contains $_.Extension.ToLowerInvariant() -and
                $_.FullName -notmatch '[\\/]build[\\/]' -and
                $_.FullName -notmatch '[\\/]evidence[\\/]'
            }
        }
    }
    foreach ($name in @('build.gradle', 'settings.gradle')) {
        $path = Join-Path $BasePath $name
        if (Test-Path -LiteralPath $path) {
            Get-Item -LiteralPath $path
        }
    }
    return @($files | Sort-Object FullName -Unique)
}

$targetFiles = Get-TargetFiles -BasePath $Root
$legacyIds = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
foreach ($file in $targetFiles) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    foreach ($match in $LegacyPattern.Matches($content)) {
        [void] $legacyIds.Add($match.Value)
    }
}

# 동일 기능 코드에서 순번이 겹치면 정렬 순서에 따라 다음 빈 순번을 배정합니다.
# 생성한 매핑을 evidence로 고정하므로 이후 재실행에서도 동일 입력은 동일 결과를 냅니다.
$usedIds = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
$mapping = [ordered]@{}
foreach ($legacyId in @($legacyIds | Sort-Object)) {
    $match = $LegacyPattern.Match($legacyId)
    $type = $match.Groups[1].Value
    $domain = $match.Groups[2].Value
    $business = $match.Groups[3].Value
    $feature = Resolve-FeatureCode -Domain $domain -Business $business
    $requestedSequence = [int] $match.Groups[5].Value
    $sequence = if ($requestedSequence -lt 1) { 1 } else { $requestedSequence }
    $candidate = '{0}{1}{2}{3:D4}' -f $type, $domain, $feature, $sequence
    while ($usedIds.Contains($candidate)) {
        $sequence++
        if ($sequence -gt 9999) {
            throw "표준 실행 ID 순번 공간을 초과했습니다. legacyId=$legacyId"
        }
        $candidate = '{0}{1}{2}{3:D4}' -f $type, $domain, $feature, $sequence
    }
    if (-not $CurrentPattern.IsMatch($candidate)) {
        throw "신규 표준 실행 ID 형식이 올바르지 않습니다. legacyId=$legacyId currentId=$candidate"
    }
    [void] $usedIds.Add($candidate)
    $mapping[$legacyId] = $candidate
}

$changedFiles = [System.Collections.Generic.List[string]]::new()
if ($Apply) {
    foreach ($file in $targetFiles) {
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $updated = $content
        foreach ($entry in $mapping.GetEnumerator()) {
            $updated = $updated.Replace($entry.Key, $entry.Value)
        }
        if ($updated -cne $content) {
            [System.IO.File]::WriteAllText($file.FullName, $updated, $Utf8NoBom)
            $changedFiles.Add($file.FullName.Substring($Root.Length).TrimStart('\', '/')) | Out-Null
        }
    }
}

$remainingLegacyIds = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
if ($Apply) {
    foreach ($file in $targetFiles) {
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        foreach ($match in $LegacyPattern.Matches($content)) {
            [void] $remainingLegacyIds.Add($match.Value)
        }
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    mode = if ($Apply) { 'APPLY' } else { 'DRY_RUN' }
    status = if ($remainingLegacyIds.Count -eq 0) { '완료' } else { '실패' }
    legacyIdCount = $mapping.Count
    currentIdCount = $usedIds.Count
    changedFileCount = $changedFiles.Count
    changedFiles = @($changedFiles)
    remainingLegacyIds = @($remainingLegacyIds | Sort-Object)
    mappings = @($mapping.GetEnumerator() | ForEach-Object {
            [ordered]@{ legacyId = $_.Key; currentId = $_.Value }
        })
}
$resultFile = if ($Apply) { 'standard-execution-id-migration-apply.sanitized.json' } else { 'standard-execution-id-migration-dry-run.sanitized.json' }
$resultPath = Join-Path $ResultDir $resultFile
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10), $Utf8NoBom)

if ($remainingLegacyIds.Count -gt 0) {
    throw "구형 표준 실행 ID가 남아 있습니다. count=$($remainingLegacyIds.Count)"
}
Write-Host "표준 실행 ID 마이그레이션 완료: mode=$($result.mode) mappings=$($mapping.Count) changedFiles=$($changedFiles.Count) result=$resultPath"
