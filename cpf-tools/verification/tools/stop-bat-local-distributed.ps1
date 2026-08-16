param([string]$RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference = "Stop"
$registryPath = Join-Path $RepoRoot "build\bat-local-runtime\process-registry.json"
if (-not (Test-Path $registryPath)) { Write-Host "No BAT process registry."; exit 0 }
$items = Get-Content $registryPath -Raw | ConvertFrom-Json
foreach ($item in $items) {
    $p = Get-Process -Id $item.pid -ErrorAction SilentlyContinue
    if ($p) {
        Stop-Process -Id $item.pid
        Write-Host "Stopped $($item.instanceId) pid=$($item.pid)"
    }
}
Remove-Item $registryPath -Force
