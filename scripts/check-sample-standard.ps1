param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# 운영 모듈에는 sample 패키지와 SampleController/SampleService가 남으면 안 됩니다.
# 교육 코드는 xyz/edu에 둘 수 있지만, 공개 클래스명은 Education 도메인 용어를 사용해야 합니다.
$operationRoots = @(
    "pfw/src/main/java",
    "cmn/src/main/java",
    "adm/src/main/java",
    "bizadm/src/main/java",
    "mbr/src/main/java",
    "exs/src/main/java",
    "acc/src/main/java"
)
$educationRoot = "xyz/src/main/java/cpf/xyz/edu"
$failures = New-Object System.Collections.Generic.List[string]

foreach ($rootPath in $operationRoots) {
    $absoluteRoot = Join-Path $Root $rootPath
    if (-not (Test-Path -LiteralPath $absoluteRoot)) {
        continue
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -Directory | ForEach-Object {
        if ($_.Name -match '(?i)^sample$') {
            $failures.Add("운영 모듈 sample 패키지 잔존: $($_.FullName.Substring($Root.Length))")
        }
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length)
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        if ($_.Name -match '(?i)Sample(Controller|Service)\.java$') {
            $failures.Add("운영 모듈 SampleController/SampleService 잔존: $relative")
        }
        if ($text -match '(?m)^\s*package\s+.*\.sample(\.|;)' ) {
            $failures.Add("운영 모듈 sample package 선언 잔존: $relative")
        }
        if ($text -match 'List\.of\s*\(\s*Map\.of\s*\(') {
            $failures.Add("운영 모듈 하드코딩 데이터(List.of(Map.of)) 사용: $relative")
        }
    }
}

$absoluteEducationRoot = Join-Path $Root $educationRoot
if (Test-Path -LiteralPath $absoluteEducationRoot) {
    Get-ChildItem -LiteralPath $absoluteEducationRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length)
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        if ($_.Name -match '(?i)Sample(Controller|Service|Request|Response)\.java$') {
            $failures.Add("EDU 공개 클래스명 Sample 잔존: $relative")
        }
        if ($text -match '(?m)^\s*public\s+(class|record|interface)\s+\S*Sample\S*') {
            $failures.Add("EDU 공개 타입명 Sample 잔존: $relative")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Sample and hardcoding standard check passed."
