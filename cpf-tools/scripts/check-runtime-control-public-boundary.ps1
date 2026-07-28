param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path

function Assert-NoCoreInternalImport([string]$Module) {
    $javaRoot = Join-Path $Root "$Module\src"
    if (-not (Test-Path -LiteralPath $javaRoot -PathType Container)) { throw "missing source root: $javaRoot" }
    $violations = @(
        Get-ChildItem -LiteralPath $javaRoot -Recurse -Filter '*.java' -File |
            Select-String -Pattern '^\s*import\s+com[.]cpf[.]core[.](common|internal)[.]' |
            ForEach-Object { "$($_.Path):$($_.LineNumber):$($_.Line.Trim())" }
    )
    if ($violations.Count -gt 0) {
        throw "$Module -> cpf-core internal/common 직접 의존이 남아 있습니다.`n$($violations -join "`n")"
    }
}

Assert-NoCoreInternalImport 'cpf-admin'
Assert-NoCoreInternalImport 'cpf-gateway'

$controller = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmRuntimeControlController.java'
if (-not (Test-Path -LiteralPath $controller -PathType Leaf)) { throw "missing: $controller" }
$text = Get-Content -LiteralPath $controller -Raw -Encoding UTF8
foreach ($required in @(
    'com.cpf.core.api.runtimecontrol',
    'CpfRuntimeRateLimitException',
    'CpfRuntimeVersionConflictException',
    'CpfRuntimeFenceException',
    'CpfRuntimeCapabilityCatalog',
    '/adm/api/runtime-control/capabilities')) {
    if ($text -notmatch [regex]::Escape($required)) {
        throw "Runtime Control public boundary marker missing: $required"
    }
}
foreach ($exception in @(
    'CpfRuntimeRateLimitException.java',
    'CpfRuntimeVersionConflictException.java',
    'CpfRuntimeFenceException.java')) {
    $path = Join-Path $Root "cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\$exception"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Runtime Control public API exception missing: $path"
    }
}
Write-Host '[PASS] ADM/Gateway -> CPF Core public API boundary'
