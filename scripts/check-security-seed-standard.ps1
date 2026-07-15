param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$utf8 = [System.Text.Encoding]::UTF8
$sqlRoot = Join-Path $Root "specs\sql"
$sqlFiles = Get-ChildItem -LiteralPath $sqlRoot -Recurse -File -Include *.sql
$operatorServicePath = Join-Path $Root "adm\src\main\java\cpf\adm\opr\service\AdmOperatorService.java"
$bootstrapPath = Join-Path $Root "adm\src\main\java\cpf\adm\opr\service\AdmBootstrapInitializer.java"
$admConfigPath = Join-Path $Root "adm\src\main\resources\application.yml"
$admSeedPath = Join-Path $sqlRoot "60_adm_seed_data.sql"

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
                -and $line -match "'[^']*(Adm!n|Bza!|Member!|Bearer\s+|eyJ|secret-change-me)[^']*'") {
            $failures.Add("$($file.FullName):$lineNo raw credential-like literal is not allowed in SQL seed")
        }
    }
}

$operatorSource = [System.IO.File]::ReadAllText($operatorServicePath, $utf8)
$bootstrapSource = [System.IO.File]::ReadAllText($bootstrapPath, $utf8)
$admConfig = [System.IO.File]::ReadAllText($admConfigPath, $utf8)
$admSeed = [System.IO.File]::ReadAllText($admSeedPath, $utf8)

if ($admSeed -match '(?is)INSERT\s+INTO\s+adm_operator\b') {
    $failures.Add("$admSeedPath fixed ADM operator seed is not allowed; use the environment-approved bootstrap")
}
if ($operatorSource -match '(?i)(default|initial|bootstrap).{0,40}password\s*=\s*"[^"$]{6,}"') {
    $failures.Add("$operatorServicePath fixed bootstrap password literal is not allowed")
}
if ($bootstrapSource -notmatch 'properties\.isEnabled\(\)' `
        -or $bootstrapSource -notmatch 'properties\.getPassword\(\)' `
        -or $bootstrapSource -notmatch 'allowProd') {
    $failures.Add("$bootstrapPath bootstrap must require enabled, external password, and production approval")
}
if ($admConfig -notmatch 'enabled:\s*\$\{CPF_ADM_BOOTSTRAP_ENABLED:false\}' `
        -or $admConfig -notmatch 'password:\s*\$\{CPF_ADM_BOOTSTRAP_PASSWORD:\}') {
    $failures.Add("$admConfig ADM bootstrap must be disabled by default and have no password default")
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Security seed standard check passed."
