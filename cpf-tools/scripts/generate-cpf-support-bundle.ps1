[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$CollectionRoot,
    [Parameter(Mandatory = $true)][string]$Manifest,
    [Parameter(Mandatory = $true)][string]$Output,
    [string]$ResultJson = ""
)

$ErrorActionPreference = "Stop"
$scriptPath = Join-Path $PSScriptRoot "generate-cpf-support-bundle.py"
$argsList = @($scriptPath, "--collection-root", $CollectionRoot, "--manifest", $Manifest, "--output", $Output)
if (-not [string]::IsNullOrWhiteSpace($ResultJson)) {
    $argsList += @("--result-json", $ResultJson)
}
& python @argsList
if ($LASTEXITCODE -ne 0) {
    throw "CPF support bundle generation failed with exit code $LASTEXITCODE"
}
