param([string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$registryPath = Join-Path $RepoRoot 'build\cpf-local-runtime\process-registry.json'
if (-not (Test-Path -LiteralPath $registryPath)) {
    Write-Host 'CPF local runtime is stopped.'
    exit 0
}
$registry = @(Get-Content -LiteralPath $registryPath -Raw -Encoding UTF8 | ConvertFrom-Json)
$result = foreach ($entry in $registry) {
    $process = Get-Process -Id ([int]$entry.pid) -ErrorAction SilentlyContinue
    [pscustomobject]@{
        role = $entry.role
        pid = $entry.pid
        port = $entry.port
        running = $null -ne $process
        startedAt = $entry.startedAt
        stderr = $entry.stderr
    }
}
$result | Format-Table -AutoSize
if (@($result | Where-Object { -not $_.running }).Count -gt 0) { exit 1 }
