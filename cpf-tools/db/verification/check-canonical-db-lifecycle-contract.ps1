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

function Get-CpfSourceSha {
    $base=Join-Path $Root 'cpf-docs/work/BASE_SHA.txt'
    if(Test-Path -LiteralPath $base -PathType Leaf){
        $value=(Get-Content -LiteralPath $base -Raw -Encoding UTF8).Trim()
        if($value -match '^[0-9a-fA-F]{40}$'){ return $value.ToLowerInvariant() }
    }
    $manifestPath=Join-Path $Root 'cpf-docs/work/PACKAGE_MANIFEST.json'
    if(Test-Path -LiteralPath $manifestPath -PathType Leaf){
        $manifest=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
        foreach($candidate in @($manifest.resultGitSha,$manifest.baselineSha)){
            if($candidate -and ([string]$candidate) -match '^[0-9a-fA-F]{40}$'){ return ([string]$candidate).ToLowerInvariant() }
        }
    }
    throw 'Exact package source identity is required for DB lifecycle verification.'
}
$sourceSha=Get-CpfSourceSha
Invoke-CpfPythonGate 'cpf-tools/db/verify_migration_lifecycle.py' @('--root',$Root,'--source-sha',$sourceSha)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-development-contract.py' @('--root',$Root)
Invoke-CpfPythonGate 'cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py' @('--root',$Root)
[ordered]@{
    status='PASS'
    sourceSha=$sourceSha
    authority='CANONICAL_JSON_RENDERER'
    checks=@('migration-lifecycle','development-contract','semantic-parity')
} | ConvertTo-Json -Depth 8
