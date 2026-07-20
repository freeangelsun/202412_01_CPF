param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$failures = New-Object System.Collections.Generic.List[object]

function Add-Failure {
    param([string] $Rule, [string] $Path, [string] $Detail)
    $failures.Add([ordered]@{ rule = $Rule; path = $Path; detail = $Detail })
}

$tracked = @(& git -C $Root ls-files)
if ($LASTEXITCODE -ne 0) {
    throw "git 추적 파일 목록을 읽지 못했습니다."
}

foreach ($path in $tracked) {
    $normalized = $path.Replace('\', '/')
    if ($normalized -notmatch '/src/(main|test)/' -and
            $normalized -notmatch '^specs/evidence/' -and
            $normalized -match '(^|/)(build|bin|out|target|logs?|tmp|temp|work)/') {
        Add-Failure 'TRACKED_RUNTIME_ARTIFACT' $normalized 'build·로그·임시 산출물은 제품 source로 추적하지 않습니다.'
    }
    if ($normalized -notmatch '^specs/evidence/' -and
            ($normalized -match '(^|/)patch-candidates/' -or
            $normalized -match '(^|/)create-domain-result[^/]*\.json$')) {
        Add-Failure 'GENERATOR_WORK_ARTIFACT_IN_PRODUCT' $normalized '생성 후보와 결과는 build/reports 또는 정제 evidence에만 둡니다.'
    }
    if ($normalized -match '(?i)\.(old|bak|copy)$') {
        Add-Failure 'BACKUP_COPY_TRACKED' $normalized '백업·사본 파일은 버전관리에서 제거합니다.'
    }
    if ($normalized -match '/deploy/inventory/.*\.candidate\.json$' -or
            $normalized -match '/sql/Vxx__') {
        Add-Failure 'UNRESOLVED_CANDIDATE_TRACKED' $normalized '확정되지 않은 candidate 또는 Vxx SQL을 제품 모듈에 두지 않습니다.'
    }
    if ($normalized.EndsWith('/.gitkeep')) {
        Add-Failure 'UNJUSTIFIED_GITKEEP' $normalized '빈 디렉터리 선점용 .gitkeep은 허용하지 않습니다.'
    }
}

$moduleRoots = @('pfw', 'cmn', 'adm', 'bza', 'mbr', 'acc', 'bat', 'xyz', 'pfw-gateway-runtime')
foreach ($module in $moduleRoots) {
    $modulePath = Join-Path $Root $module
    if (-not (Test-Path -LiteralPath $modulePath -PathType Container)) {
        continue
    }
    $candidateAndFinal = Get-ChildItem -LiteralPath $modulePath -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\build\\' -and $_.Name -match '\.candidate\.'
    }
    foreach ($candidate in $candidateAndFinal) {
        Add-Failure 'CANDIDATE_FINAL_DUPLICATION' `
                $candidate.FullName.Substring($Root.Length + 1).Replace('\', '/') `
                '제품 모듈 안에는 최종 산출물만 유지합니다.'
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir 'repository-hygiene.sanitized.json'
$result = [ordered]@{
    generatedAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss.fffK')
    status = if ($failures.Count -eq 0) { 'DONE' } else { 'FAILED' }
    trackedFileCount = $tracked.Count
    failureCount = $failures.Count
    failures = @($failures.ToArray())
}
[System.IO.File]::WriteAllText(
        $resultPath,
        ($result | ConvertTo-Json -Depth 10),
        [System.Text.UTF8Encoding]::new($false))

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL [$($_.rule)] $($_.path)" }
    exit 1
}
Write-Host "Repository hygiene check passed. tracked=$($tracked.Count) evidence=$resultPath"
