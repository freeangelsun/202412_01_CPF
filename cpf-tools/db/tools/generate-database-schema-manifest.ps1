param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $OutputPath = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
}

$generator = Join-Path $Root "cpf-tools/db/tools/generate-database-schema-manifest.py"
if (-not (Test-Path -LiteralPath $generator -PathType Leaf)) {
    throw "Database Schema Manifest Python generator가 없습니다: $generator"
}

$python = Get-Command python -ErrorAction SilentlyContinue
$arguments = @($generator, "--root", $Root, "--output", $OutputPath)
if ($null -eq $python) {
    $python = Get-Command python3 -ErrorAction SilentlyContinue
}
if ($null -eq $python) {
    $python = Get-Command py -ErrorAction SilentlyContinue
    if ($null -ne $python) {
        $arguments = @("-3") + $arguments
    }
}
if ($null -eq $python) {
    throw "Python 3 실행 파일을 찾을 수 없습니다. python/python3/py 중 하나가 필요합니다."
}

& $python.Source @arguments
if ($LASTEXITCODE -ne 0) {
    throw "Database Schema Manifest 생성 실패: exit=$LASTEXITCODE"
}
