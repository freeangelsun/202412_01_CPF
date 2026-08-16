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

# Legacy split/source SQL is not the current authority.
# Canonical JSON -> renderer -> generated/current Vendor3 is the product contract.
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-schema-governance.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-development-contract.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/render_vendor_pack.py' @('--root',$Root,'--check')
[ordered]@{
    status='PASS'
    canonicalSchema='cpf-tools/db/canonical/platform-schema.json'
    authority='CANONICAL_JSON_RENDERER'
    officialVendors=@('mariadb','postgresql','oracle')
    checks=@('schema-governance','development-contract','semantic-parity','renderer-check')
} | ConvertTo-Json -Depth 8
