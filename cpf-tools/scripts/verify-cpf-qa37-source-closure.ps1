[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot "../..")), [switch]$OverlayPackage, [switch]$SkipJavaCompile)
$ErrorActionPreference = 'Stop'
$argsList = @((Join-Path $Root 'cpf-tools/scripts/verify-cpf-qa37-source-closure.py'), '--root', $Root)
if ($OverlayPackage) { $argsList += '--overlay-package' }
if ($SkipJavaCompile) { $argsList += '--skip-java-compile' }
& python @argsList
if ($LASTEXITCODE -ne 0) { throw "QA37 source closure failed (exit=$LASTEXITCODE)" }
