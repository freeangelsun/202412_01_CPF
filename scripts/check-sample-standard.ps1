param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

# 범용 EDU는 REF와 BAT만 소유합니다. 다른 모듈은 엔진, 포트, reference adapter와 테스트 fixture만 둡니다.
$operationRoots = @(
    "cpf-core/src/main/java",
    "cpf-common/src/main/java",
    "cpf-admin/src/main/java",
    "cpf-biz-admin/src/main/java",
    "cpf-member/src/main/java"
)
$educationRoots = @(
    "cpf-batch/src/main/java/com/cpf/batch/edu",
    "cpf-reference/src/main/java/com/cpf/reference"
)
$failures = New-Object System.Collections.Generic.List[string]

function Test-EduSourcePath {
    param([string] $RelativePath)
    $normalized = ($RelativePath -replace '\\', '/').TrimStart('/')
    foreach ($educationRoot in $educationRoots) {
        if ($normalized.StartsWith($educationRoot.Replace('\', '/'))) {
            return $true
        }
    }
    return $false
}

foreach ($rootPath in $operationRoots) {
    $absoluteRoot = Join-Path $Root $rootPath
    if (-not (Test-Path -LiteralPath $absoluteRoot)) {
        continue
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -Directory | ForEach-Object {
        $relativeDirectory = $_.FullName.Substring($Root.Length)
        if ($_.Name -match '(?i)^sample$' -and -not (Test-EduSourcePath $relativeDirectory)) {
            $failures.Add("운영 모듈 sample 패키지 잔존: $($_.FullName.Substring($Root.Length))")
        }
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length)
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        $isEduSource = Test-EduSourcePath $relative

        if (-not $isEduSource -and ($_.Name -match 'EducationSample' -or $text -match '(?m)^\s*package\s+com\.cpf\.(core|common|admin|bizadmin|member)\.edu(\.|;)')) {
            $failures.Add("REF/BAT 외 모듈의 범용 EDU 소유권 위반: $relative")
        }

        if (-not $isEduSource -and $_.Name -match '(?i)Sample(Controller|Service)\.java$') {
            $failures.Add("운영 모듈 SampleController/SampleService 잔존: $relative")
        }
        if (-not $isEduSource -and $text -match '(?m)^\s*package\s+.*\.sample(\.|;)' ) {
            $failures.Add("운영 모듈 sample package 선언 잔존: $relative")
        }
        if (-not $isEduSource -and $text -match 'List\.of\s*\(\s*Map\.of\s*\(') {
            $failures.Add("운영 모듈 하드코딩 데이터(List.of(Map.of)) 사용: $relative")
        }
    }
}

$educationRoots | ForEach-Object {
    $absoluteEducationRoot = Join-Path $Root $_
    if (-not (Test-Path -LiteralPath $absoluteEducationRoot)) {
        return
    }
    Get-ChildItem -LiteralPath $absoluteEducationRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length)
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        if ($_.Name -match '(?i)Sample(Controller|Service|Request|Response)\.java$' -and $_.Name -notmatch 'EducationSample') {
            $failures.Add("EDU 공개 클래스명 Sample 잔존: $relative")
        }
        if ($text -match '(?m)^\s*public\s+(class|record|interface)\s+(?!\S*EducationSample)\S*Sample\S*') {
            $failures.Add("EDU 공개 타입명 Sample 잔존: $relative")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Sample and hardcoding standard check passed."
