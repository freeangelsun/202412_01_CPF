param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)
$ErrorActionPreference = "Stop"
$tracked = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
if (-not (Test-Path -LiteralPath $tracked -PathType Leaf)) {
    throw "Tracked DB schema manifest가 없습니다: $tracked"
}
$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-db-schema-" + [Guid]::NewGuid().ToString("N") + ".json")
try {
    $generator = Join-Path $PSScriptRoot "generate-database-schema-manifest.ps1"
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $generator -Root $Root -OutputPath $temp
    if ($LASTEXITCODE -ne 0) { throw "DB schema manifest 생성 실패 exitCode=$LASTEXITCODE" }

    $expected = Get-Content -LiteralPath $tracked -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50 | ConvertTo-Json -Depth 50 -Compress
    $actual = Get-Content -LiteralPath $temp -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50 | ConvertTo-Json -Depth 50 -Compress
    if ($expected -ne $actual) {
        throw "Canonical SQL/metadata와 generated DB schema manifest가 다릅니다. sync-database-artifacts.ps1을 실행하세요."
    }
    Write-Host "Database schema artifact drift check passed."
} finally {
    Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
}
