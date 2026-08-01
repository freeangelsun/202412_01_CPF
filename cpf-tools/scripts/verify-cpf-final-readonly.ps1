[CmdletBinding()]
param(
    [Parameter()][string]$ProjectRoot = ".",
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string]$ExpectedSha,
    [Parameter()][switch]$Release
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path

function Resolve-RequiredFile([string]$Name,[string]$RelativePath) {
    $path = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "$Name missing: $RelativePath" }
    return $path
}
function Invoke-Required([string]$Name,[string]$RelativePath,[string[]]$Arguments=@()) {
    $path = Resolve-RequiredFile $Name $RelativePath
    $extension = [IO.Path]::GetExtension($path).ToLowerInvariant()
    Write-Host "[CPF][RUN] $Name :: $RelativePath $($Arguments -join ' ')"
    switch ($extension) {
        '.ps1' { & pwsh -NoProfile -ExecutionPolicy Bypass -File $path @Arguments }
        '.py'  { & python $path @Arguments }
        '.mjs' { & node $path @Arguments }
        '.js'  { & node $path @Arguments }
        default { throw "$Name unsupported executable extension: $extension ($RelativePath)" }
    }
    if ($LASTEXITCODE -ne 0) { throw "$Name failed(exit=$LASTEXITCODE)" }
}
function Assert-RequiredCommands {
    foreach ($command in @('git','python','node','pwsh')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) { throw "Required command missing: $command" }
    }
}
function Assert-GateInventory {
    $required = @(
        'cpf-tools/scripts/verify-cpf-requirement-traceability.py',
        'cpf-tools/scripts/verify-cpf-adm-capability-registry.py',
        'cpf-tools/scripts/verify-cpf-adm-commercial-page-contract.py',
        'cpf-tools/scripts/verify-cpf-controller-permission-contract.py',
        'cpf-tools/scripts/check-repository-hygiene.ps1',
        'cpf-tools/scripts/check-db-vendor-pack-parity.ps1',
        'cpf-tools/scripts/check-migration-checksums.ps1',
        'cpf-tools/scripts/check-enterprise-source-closure.ps1',
        'cpf-tools/scripts/check-direct-client-boundary.ps1',
        'cpf-tools/scripts/check-semantic-consumer-graph.ps1',
        'cpf-tools/scripts/check-evidence-contract.ps1'
    )
    foreach ($relative in $required) { [void](Resolve-RequiredFile 'Required gate' $relative) }
}

Push-Location $root
try {
    Assert-RequiredCommands
    Assert-GateInventory
    $head = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) { throw "git rev-parse HEAD failed(exit=$LASTEXITCODE)" }
    if ($head -ne $ExpectedSha) { throw "exact SHA mismatch expected=$ExpectedSha actual=$head" }
    $before = @(& git status --porcelain=v1 --untracked-files=all)
    if ($LASTEXITCODE -ne 0) { throw "git status(before) failed(exit=$LASTEXITCODE)" }
    if ($before.Count -gt 0) { throw "Working Tree must be clean before read-only validation.`n$($before -join [Environment]::NewLine)" }

    $traceArgs = @('--root',$root,'--expected-sha',$ExpectedSha,'--require-clean')
    if ($Release) { $traceArgs += '--release' }
    Invoke-Required 'Requirement traceability' 'cpf-tools/scripts/verify-cpf-requirement-traceability.py' $traceArgs
    Invoke-Required 'ADM capability registry' 'cpf-tools/scripts/verify-cpf-adm-capability-registry.py' @('--root',$root)
    Invoke-Required 'ADM commercial page contract' 'cpf-tools/scripts/verify-cpf-adm-commercial-page-contract.py' @('--root',$root)
    Invoke-Required 'Controller permission contract' 'cpf-tools/scripts/verify-cpf-controller-permission-contract.py' @('--root',$root,'--strict')
    Invoke-Required 'Repository hygiene' 'cpf-tools/scripts/check-repository-hygiene.ps1' @('-Root',$root)
    Invoke-Required 'DB vendor parity' 'cpf-tools/scripts/check-db-vendor-pack-parity.ps1' @('-Root',$root)
    Invoke-Required 'Migration checksums' 'cpf-tools/scripts/check-migration-checksums.ps1' @('-Root',$root)
    Invoke-Required 'Ownership boundary' 'cpf-tools/scripts/check-enterprise-source-closure.ps1' @('-Root',$root)
    Invoke-Required 'Direct client boundary' 'cpf-tools/scripts/check-direct-client-boundary.ps1' @('-Root',$root)
    Invoke-Required 'Semantic consumer graph' 'cpf-tools/scripts/check-semantic-consumer-graph.ps1' @('-Root',$root)
    Invoke-Required 'Evidence contract' 'cpf-tools/scripts/check-evidence-contract.ps1' @('-Root',$root)

    $after = @(& git status --porcelain=v1 --untracked-files=all)
    if ($LASTEXITCODE -ne 0) { throw "git status(after) failed(exit=$LASTEXITCODE)" }
    if ($after.Count -gt 0) { throw "Read-only validation changed the Working Tree.`n$($after -join [Environment]::NewLine)" }
    Write-Host "[CPF][PASS] exact-SHA read-only validation SHA=$head release=$Release"
}
finally { Pop-Location }
