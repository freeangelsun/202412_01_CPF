param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $Force
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$registryPath = Join-Path $RepoRoot 'build\cpf-local-runtime\process-registry.json'
if (-not (Test-Path -LiteralPath $registryPath)) {
    Write-Host "CPF local runtime registry가 없습니다: $registryPath"
    exit 0
}

$registry = @(Get-Content -LiteralPath $registryPath -Raw -Encoding UTF8 | ConvertFrom-Json)
foreach ($entry in $registry) {
    $process = Get-Process -Id ([int]$entry.pid) -ErrorAction SilentlyContinue
    if ($null -ne $process) {
        Stop-Process -Id $process.Id -Force:$Force -ErrorAction Stop
        Write-Host "Stopped $($entry.role) pid=$($entry.pid)"
    }
}
Remove-Item -LiteralPath $registryPath -Force
