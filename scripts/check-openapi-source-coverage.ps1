param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

$ErrorActionPreference = "Stop"
& "$PSScriptRoot/ensure-explicit-openapi-operation-ids.ps1" -Root $Root -ResultDir $ResultDir
exit $LASTEXITCODE
