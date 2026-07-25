param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path

function Fail([string]$Message) { throw "Source documentation gate FAIL: $Message" }

# 현재 작업에서 변경된 Java를 우선 검사한다. Commit 직후처럼 diff가 없으면 CPF Public API와
# 이번 제품 표준 영역을 검사하여 Gate 자체가 무의미해지지 않게 한다.
$changed = @()
$changed += @(& git -C $Root diff --name-only --diff-filter=ACMR 2>$null)
$changed += @(& git -C $Root diff --cached --name-only --diff-filter=ACMR 2>$null)
$changed = @($changed | Where-Object { $_ -like '*.java' } | Sort-Object -Unique)

if ($changed.Count -eq 0) {
    foreach ($relativeRoot in @(
        'cpf-core/src/main/java/com/cpf/core/api',
        'cpf-core/src/main/java/com/cpf/core/spi',
        'cpf-common/src/main/java/com/cpf/common/calendar'
    )) {
        $path = Join-Path $Root $relativeRoot
        if (Test-Path $path) {
            $changed += @(Get-ChildItem $path -Recurse -File -Filter *.java |
                ForEach-Object { $_.FullName.Substring($Root.Length).TrimStart('\','/') })
        }
    }
}

$errors = [System.Collections.Generic.List[string]]::new()
foreach ($relative in @($changed | Sort-Object -Unique)) {
    $path = Join-Path $Root $relative
    if (-not (Test-Path $path)) { continue }
    $text = Get-Content $path -Raw

    # @RestControllerAdvice를 @RestController로 오인하지 않는다.
    $isRestController = $text -match '@RestController\b'

    # Public API/SPI와 Controller/Service 같은 중요 변경 Source는 최소 class-level JavaDoc을 가진다.
    $important = $relative -match '[/\\]com[/\\]cpf[/\\]core[/\\](api|spi)[/\\]' -or
                 $relative -match '[/\\]calendar[/\\]' -or
                 $isRestController -or
                 $text -match '@Service'
    if ($important -and $text -notmatch '(?s)/\*\*.*?\*/\s*(?:@\w+(?:\([^;]*?\))?\s*)*(?:public\s+)?(?:final\s+|abstract\s+)?(?:class|interface|record|enum)\s+') {
        $errors.Add("$relative : 중요 Class/Interface JavaDoc 누락")
    }

    if ($isRestController) {
        if ($text -notmatch '@Tag\s*\(') { $errors.Add("$relative : Controller @Tag 누락") }
        $mappingCount = ([regex]::Matches($text, '@(?:Get|Post|Put|Delete|Patch)Mapping\s*\(')).Count
        $operationCount = ([regex]::Matches($text, '@Operation\s*\(')).Count
        if ($mappingCount -gt $operationCount) {
            $errors.Add("$relative : API mapping=$mappingCount, @Operation=$operationCount")
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Host " - $_" }
    Fail "$($errors.Count)건"
}
Write-Host "Source documentation/OpenAPI standard PASS. checked=$($changed.Count)"
