param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$utf8 = [System.Text.Encoding]::UTF8
$sqlRoot = Join-Path $Root "specs\sql"
$sqlFiles = Get-ChildItem -LiteralPath $sqlRoot -Recurse -File -Include *.sql

foreach ($file in $sqlFiles) {
    $lines = [System.IO.File]::ReadAllLines($file.FullName, $utf8)
    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]
        $lineNo = $i + 1

        if ($line -match '(?i)\b(refresh_token|access_token|client_secret|api_key)\s+(varchar|text|longtext)\b' `
                -and $line -notmatch '(?i)(refresh_token_hash|token_hash|secret_ref|api_key_hash)') {
            $failures.Add("$($file.FullName):$lineNo raw token/secret column name must use hash or secret_ref")
        }

        if ($line -match '(?i)\b(password|refresh_token|access_token|client_secret|api_key)\b' `
                -and $line -match "'[^']*(Adm!n|BizAdm!|Member!|Bearer\s+|eyJ|secret-change-me)[^']*'") {
            $failures.Add("$($file.FullName):$lineNo raw credential-like literal is not allowed in SQL seed")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Security seed standard check passed."
