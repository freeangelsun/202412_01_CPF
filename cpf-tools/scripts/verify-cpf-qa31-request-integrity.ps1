param(
    [string] $ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$manifestPath = Join-Path $ProjectRoot "cpf-docs/quality/CPF_20260730_QA31_REQUEST_INTEGRITY.json"
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "QA31 integrity manifest is missing: $manifestPath"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$failures = [System.Collections.Generic.List[string]]::new()
foreach ($entry in $manifest.files) {
    $path = Join-Path $ProjectRoot $entry.path
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        $failures.Add("missing: $($entry.path)") | Out-Null
        continue
    }
    $actual = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne ([string]$entry.sha256).ToLowerInvariant()) {
        $failures.Add("hash mismatch: $($entry.path) expected=$($entry.sha256) actual=$actual") | Out-Null
    }
}
$resultDir = Join-Path $ProjectRoot "build/quality-gate"
New-Item -ItemType Directory -Path $resultDir -Force | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    failureCount = $failures.Count
    failures = @($failures)
}
$result | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $resultDir "qa31-request-integrity.json") -Encoding UTF8
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "[PASS] QA31 immutable request files match the integrity manifest."
