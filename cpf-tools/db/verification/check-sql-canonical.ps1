param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8 = [Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $Utf8
[Console]::OutputEncoding = $Utf8
$OutputEncoding = $Utf8
$Root = (Resolve-Path -LiteralPath $Root).Path
$python = (Get-Command python -ErrorAction Stop).Source

function Invoke-CpfPythonGate([string]$Script,[string[]]$Arguments=@()) {
    $path = Join-Path $Root $Script
    if(-not(Test-Path -LiteralPath $path -PathType Leaf)){ throw "CPF DB gate missing: $Script" }
    & $python $path @Arguments
    if($LASTEXITCODE -ne 0){ throw "CPF DB gate failed: $Script exit=$LASTEXITCODE" }
}

Invoke-CpfPythonGate 'cpf-tools/db/render_vendor_pack.py' @('--root',$Root,'--check')
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-vendor-manifest.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-schema-governance.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py' @('--root',$Root)
[ordered]@{
    status='PASS'
    canonicalSchema='cpf-tools/db/canonical/platform-schema.json'
    renderer='cpf-tools/db/render_vendor_pack.py'
    generatedCurrent='cpf-tools/db/generated/current/{vendor}'
    officialVendors=@('mariadb','postgresql','oracle')
    checks=@('renderer-check','vendor-manifest','schema-governance','semantic-parity')
} | ConvertTo-Json -Depth 8
