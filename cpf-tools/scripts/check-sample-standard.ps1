param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

# 범용 EDU는 REF만 소유합니다. BAT는 독립 Runtime/Contract만 소유하고
# Batch Job/Step 교육은 REF가 공개 CPF API/SPI를 소비해 제공합니다.
$operationRoots = @(
    "cpf-core/src/main/java",
    "cpf-common/src/main/java",
    "cpf-admin/src/main/java",
    "cpf-biz-admin/src/main/java",
    "cpf-member/src/main/java"
)
$educationRoots = @(
    "cpf-reference/src/main/java/com/cpf/reference"
)
$allowedProductSampleRoots = @(
    # CMN은 설치·Paging·Transaction 검증을 위한 Minimal Sample Table 1개를 제공합니다.
    "cpf-common/src/main/java/com/cpf/common/sample",
    # BZA 채번은 선택형 Customization Sample이며 Platform Runtime 필수 기능이 아닙니다.
    "cpf-biz-admin/src/main/java/com/cpf/bizadmin/sample"
)
$failures = New-Object System.Collections.Generic.List[string]

function Test-PathUnderRoots {
    param(
        [string] $RelativePath,
        [string[]] $Roots
    )
    $normalized = ($RelativePath -replace '\\', '/').TrimStart('/')
    foreach ($root in $Roots) {
        if ($normalized.StartsWith($root.Replace('\', '/'))) {
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
        if ($_.Name -match '(?i)^sample$' -and
                -not (Test-PathUnderRoots $relativeDirectory $educationRoots) -and
                -not (Test-PathUnderRoots $relativeDirectory $allowedProductSampleRoots)) {
            $failures.Add("운영 모듈 sample 패키지 잔존: $($_.FullName.Substring($Root.Length))")
        }
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length)
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        $isEduSource = Test-PathUnderRoots $relative $educationRoots
        $isAllowedProductSample = Test-PathUnderRoots $relative $allowedProductSampleRoots

        if (-not $isEduSource -and -not $isAllowedProductSample -and
                ($_.Name -match 'EducationSample' -or $text -match '(?m)^\s*package\s+com\.cpf\.(core|common|admin|bizadmin|member)\.edu(\.|;)')) {
            $failures.Add("REF 외 모듈의 범용 EDU 소유권 위반: $relative")
        }

        if (-not $isEduSource -and -not $isAllowedProductSample -and
                $_.Name -match '(?i)Sample(Controller|Service)\.java$') {
            $failures.Add("운영 모듈 SampleController/SampleService 잔존: $relative")
        }
        if (-not $isEduSource -and -not $isAllowedProductSample -and
                $text -match '(?m)^\s*package\s+.*\.sample(\.|;)' ) {
            $failures.Add("운영 모듈 sample package 선언 잔존: $relative")
        }
        if (-not $isEduSource -and -not $isAllowedProductSample -and
                $text -match 'List\.of\s*\(\s*Map\.of\s*\(') {
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
        if ($text -match '(?m)^\s*public\s+(class|record|interface)\s+(?!\S*Education)\S*Sample\S*') {
            $failures.Add("EDU 공개 타입명 Sample 잔존: $relative")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Sample and hardcoding standard check passed."
