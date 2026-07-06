param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $EvidenceDir = "",
    [string] $RuntimeDir = "",
    [string] $SqlSmokeDir = "",
    [switch] $AllowMissing
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
    $EvidenceDir = Join-Path $Root "specs/evidence/20260706_02"
}
if ([string]::IsNullOrWhiteSpace($RuntimeDir)) {
    $RuntimeDir = Join-Path $Root "build/runtime-smoke"
}
if ([string]::IsNullOrWhiteSpace($SqlSmokeDir)) {
    $SqlSmokeDir = Join-Path $Root "build/sql-smoke"
}

New-Item -ItemType Directory -Force -Path $EvidenceDir | Out-Null
$manifestPath = Join-Path $EvidenceDir "evidence-manifest.sanitized.json"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$manifest = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    evidenceDir = $EvidenceDir
    items = @()
}

function Protect-Text {
    param([string] $Text)

    if ($null -eq $Text) {
        return ""
    }

    $masked = $Text
    $masked = [System.Text.RegularExpressions.Regex]::Replace($masked, '(?i)(Bearer\s+)[A-Za-z0-9._~+/=-]+', '$1***')
    $masked = [System.Text.RegularExpressions.Regex]::Replace(
        $masked,
        '(?i)((password|passwd|pwd|secret|credential|signature|api[-_]?key|accessToken|refreshToken|token)\s*["'']?\s*[:=]\s*)("[^"]*"|''[^'']*''|[^,}\]\r\n]+)',
        '$1"***"')
    $masked = [System.Text.RegularExpressions.Regex]::Replace($masked, '(?i)(MYSQL_PWD|MARIADB_PWD)\s*=\s*[^\r\n]+', '$1=***')
    return $masked
}

function Export-One {
    param(
        [string[]] $Sources,
        [string] $TargetName,
        [string] $Kind
    )

    $sourcePath = $null
    foreach ($source in $Sources) {
        $candidate = Join-Path $Root $source
        if (Test-Path -LiteralPath $candidate) {
            $sourcePath = (Resolve-Path -LiteralPath $candidate).Path
            break
        }
    }

    $targetPath = Join-Path $EvidenceDir $TargetName
    if ([string]::IsNullOrWhiteSpace($sourcePath)) {
        $manifest.items += [ordered]@{
            target = $TargetName
            status = "MISSING"
            sources = $Sources
            kind = $Kind
        }
        if (-not $AllowMissing) {
            throw "Required evidence source is missing for $TargetName. sources=$($Sources -join ', ')"
        }
        return
    }

    $raw = [System.IO.File]::ReadAllText($sourcePath, [System.Text.Encoding]::UTF8)
    $safe = Protect-Text -Text $raw
    [System.IO.File]::WriteAllText($targetPath, $safe, $Utf8NoBom)
    $manifest.items += [ordered]@{
        target = $TargetName
        status = "EXPORTED"
        source = $sourcePath.Substring($Root.Length).TrimStart("\", "/")
        bytes = (Get-Item -LiteralPath $targetPath).Length
        kind = $Kind
    }
}

Export-One -Sources @("build/runtime-smoke/standard-header-e2e-result.json") -TargetName "standard-header-e2e-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/composite-transaction-runtime-result.json") -TargetName "composite-transaction-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/composite-transaction-failure-runtime-result.json") -TargetName "composite-transaction-failure-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/adm-transaction-group-runtime-result.json") -TargetName "adm-transaction-group-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/adm-transaction-group-failure-runtime-result.json") -TargetName "adm-transaction-group-failure-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/openapi-runtime-result.json") -TargetName "openapi-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/adm-ui-browser-smoke-result.json") -TargetName "adm-ui-browser-smoke-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/runtime-smoke-summary.json") -TargetName "runtime-smoke-summary.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/runtime-closure-result.json") -TargetName "runtime-closure-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/file-log-standard-result.json") -TargetName "file-log-standard-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/file-log-grep-summary.log") -TargetName "file-log-grep-summary.sanitized.log" -Kind "log"
Export-One -Sources @("build/runtime-smoke/trace-boost-runtime-result.json") -TargetName "trace-boost-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/bat-trace-boost-runtime-result.json") -TargetName "bat-trace-boost-runtime-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/runtime-smoke/create-domain-result.json") -TargetName "create-domain-result.sanitized.json" -Kind "json"
Export-One -Sources @("build/sql-smoke/mariadb-full-install-result.json") -TargetName "mariadb-full-install-result.sanitized.json" -Kind "json"
Export-One -Sources @(
    "build/runtime-smoke/run-local-services-composite-rerun.sanitized.log",
    "build/runtime-smoke/run-local-services-composite-rerun.job.log",
    "build/runtime-smoke/run-local-services-closure.job.log",
    "build/runtime-smoke/run-local-services-20260706-02.out.log",
    "build/runtime-smoke/run-local-services.job.log"
) -TargetName "run-local-services-composite-rerun.sanitized.log" -Kind "log"

[System.IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 20), $Utf8NoBom)
Write-Host "Sanitized evidence exported. manifest=$manifestPath"
