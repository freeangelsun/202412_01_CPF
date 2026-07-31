param(
    [Parameter(Mandatory=$false)][string]$Root = ".",
    [switch]$RequireLegacyRemoved
)
$ErrorActionPreference = "Stop"
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$expected = @(
 "README.md",
 "cpf-tools/README.md",
 "cpf-docs/guides/README.md",
 "cpf-docs/guides/00_프레임워크안내.md",
 "cpf-docs/guides/01_개발자매뉴얼.md",
 "cpf-docs/guides/02_배치개발매뉴얼.md",
 "cpf-docs/guides/03_ADM개발자매뉴얼.md",
 "cpf-docs/guides/04_ADM운영자매뉴얼.md",
 "cpf-docs/guides/05_플랫폼운영매뉴얼.md",
 "cpf-docs/guides/90_BZA매뉴얼.md",
 "cpf-docs/guides/91_게이트웨이매뉴얼.md"
)
$errors = New-Object System.Collections.Generic.List[string]
foreach ($item in $expected) {
    $path = Join-Path $rootPath ($item.Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $path)) { $errors.Add("필수 파일 누락: $item") }
}
$scanFiles = $expected | ForEach-Object { Join-Path $rootPath ($_.Replace('/', [IO.Path]::DirectorySeparatorChar)) } | Where-Object { Test-Path $_ }
$forbidden = @("업무 시스템을 위한", "실패를 숨기지 않고 복구 가능한 상태로 남깁니다", "CPF_DEVELOPER_GUIDE.md", "CPF_ADMIN_OPERATOR_GUIDE.md")
foreach ($file in $scanFiles) {
    $text = Get-Content -LiteralPath $file -Raw -Encoding UTF8
    foreach ($phrase in $forbidden) {
        if ($text.Contains($phrase)) { $errors.Add("금지/구형 문구: $phrase @ $file") }
    }
}
$manifest = Join-Path $rootPath "cpf-docs/work/manifest/CPF_GUIDE_REBUILD_DELETE_MANIFEST.txt"
$legacyLeafNames = New-Object System.Collections.Generic.List[string]
if (-not (Test-Path -LiteralPath $manifest)) {
    $errors.Add("삭제 Manifest 누락")
} else {
    Get-Content -LiteralPath $manifest -Encoding UTF8 |
        Where-Object { $_ -and -not $_.Trim().StartsWith('#') } |
        ForEach-Object {
            $relative = $_.Trim()
            $legacyLeafNames.Add([IO.Path]::GetFileName($relative))
            if ($RequireLegacyRemoved) {
                $p = Join-Path $rootPath ($relative.Replace('/', [IO.Path]::DirectorySeparatorChar))
                if (Test-Path -LiteralPath $p) { $errors.Add("Legacy Guide 잔존: $relative") }
            }
        }
}
# 정본과 cpf-tools README가 삭제된 Guide를 다시 링크하지 않는지 조기 검사한다.
foreach ($file in $scanFiles) {
    $text = Get-Content -LiteralPath $file -Raw -Encoding UTF8
    foreach ($leaf in $legacyLeafNames) {
        if ($text.Contains($leaf)) { $errors.Add("삭제된 Guide 참조 잔존: $leaf @ $file") }
    }
}
$assetNames = @(
 "cpf-product-contract.svg","cpf-development-journey.svg","cpf-transaction-flow.svg","cpf-messaging-flow.svg",
 "cpf-batch-ownership.svg","cpf-batch-runtime.svg","cpf-adm-development-stack.svg","cpf-adm-operation-flow.svg",
 "cpf-platform-operation.svg","cpf-bza-optional.svg","cpf-gateway-lifecycle.svg"
)
foreach ($asset in $assetNames) {
    if (-not (Test-Path (Join-Path $rootPath "cpf-docs/assets/guides/$asset"))) { $errors.Add("Guide 이미지 누락: $asset") }
}
if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "CPF Guide 정본 검증 통과: $($expected.Count) 문서, $($assetNames.Count) Guide 이미지"
exit 0
