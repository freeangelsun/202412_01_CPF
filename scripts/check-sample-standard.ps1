param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# 운영 모듈에는 일반 sample 패키지와 SampleController/SampleService가 남으면 안 됩니다.
# 단, CPF 공식 EDU 샘플은 각 모듈의 edu 하위 기능 패키지에 EducationSample 이름으로 둘 수 있습니다.
$operationRoots = @(
    "pfw/src/main/java",
    "cmn/src/main/java",
    "adm/src/main/java",
    "bizadm/src/main/java",
    "mbr/src/main/java",
    "exs/src/main/java",
    "acc/src/main/java"
)
$educationRoots = @(
    "pfw/src/main/java/cpf/pfw/common",
    "cmn/src/main/java/cpf/cmn/edu",
    "adm/src/main/java/cpf/adm/edu",
    "bizadm/src/main/java/cpf/bizadm/edu",
    "exs/src/main/java/cpf/exs/edu",
    "bat/src/main/java/cpf/bat/edu",
    "xyz/src/main/java/cpf/xyz/edu"
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
