param(
    [string] $ExpectedSha = $env:GITHUB_SHA,
    [switch] $AllowOverlayBaseSha
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if ([string]::IsNullOrWhiteSpace($ExpectedSha)) {
    $ExpectedSha = (& git -C $root rev-parse HEAD).Trim()
}
if ($ExpectedSha -notmatch '^[0-9a-fA-F]{40}$') {
    throw "ExpectedSha must be a full 40-character Git SHA: $ExpectedSha"
}

$targets = @('cpf-docs/work/current', 'cpf-docs/work/handover', 'cpf-docs/evidence') |
    ForEach-Object { Join-Path $root $_ } |
    Where-Object { Test-Path -LiteralPath $_ -PathType Container }

$claimPattern = '(?im)(exact[_ ]?sha|validated[_ ]?sha|verification[_ ]?sha|검증 SHA|검수 SHA|Evidence SHA)\s*[:=]\s*`?([0-9a-f]{40})`?'
$basePattern = '(?im)(overlay[_ ]?base[_ ]?sha|개발 기준 SHA|Overlay 기준 SHA|기준 Commit)\s*[:=]\s*`?([0-9a-f]{40})`?'
$errors = @()

foreach ($dir in $targets) {
    Get-ChildItem -LiteralPath $dir -Recurse -File -Include *.md,*.json,*.csv | ForEach-Object {
        $text = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
        foreach ($match in [regex]::Matches($text, $claimPattern)) {
            $claimed = $match.Groups[2].Value
            if ($claimed -ne $ExpectedSha) {
                $errors += "exact validation SHA mismatch: $($_.FullName) claimed=$claimed expected=$ExpectedSha"
            }
        }
        if (-not $AllowOverlayBaseSha) {
            foreach ($match in [regex]::Matches($text, $basePattern)) {
                $claimed = $match.Groups[2].Value
                if ($claimed -ne $ExpectedSha) {
                    $errors += "base SHA mismatch: $($_.FullName) claimed=$claimed expected=$ExpectedSha"
                }
            }
        }
    }
}

if ($errors) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "[PASS] Work/evidence SHA claims are consistent with $ExpectedSha (allowOverlayBase=$AllowOverlayBaseSha)"
