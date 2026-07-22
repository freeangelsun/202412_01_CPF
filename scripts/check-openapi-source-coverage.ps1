param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
& "$PSScriptRoot/ensure-explicit-openapi-operation-ids.ps1" -Root $Root -ResultDir $ResultDir
exit $LASTEXITCODE
