param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,
    [Parameter(Mandatory = $true)]
    [ValidateSet('postgresql','oracle')]
    [string]$Vendor,
    [string]$ProfilePath = '',
    [ValidateSet('cpf','bza','bat')]
    [string[]]$Module = @('cpf','bza','bat'),
    [string]$EvidencePath = ''
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ProfilePath)) { $ProfilePath = Join-Path $Root 'cpf-tools/config/database-install.default.json' }
$ProfilePath = (Resolve-Path -LiteralPath $ProfilePath).Path
. (Join-Path $Root 'cpf-tools/scripts/database-profile-common.ps1')

# Static contract drift must be clean before a real DB parse smoke is attempted.
& (Join-Path $Root 'cpf-tools/scripts/check-query-contract-integrity.ps1') -Root $Root | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Runtime Query Contract integrity failed before DB smoke.' }

$profile = Get-CpfDatabaseProfile -Path $ProfilePath
$moduleKeyMap = @{ cpf = 'core'; bza = 'bizAdmin'; bat = 'batch' }
$startedAt = [DateTimeOffset]::Now
$results = [System.Collections.Generic.List[object]]::new()
$failures = [System.Collections.Generic.List[object]]::new()

function ConvertTo-ParseSql([string]$Sql) {
    $text = $Sql.Trim()
    $text = [regex]::Replace($text, '#\{[A-Za-z][A-Za-z0-9_]*\}', 'NULL')
    $text = [regex]::Replace($text, '(?<!:):[A-Za-z][A-Za-z0-9_]*', 'NULL')
    $text = $text.Replace('?', 'NULL').Replace('%s', 'NULL')
    return $text.Trim()
}

function Read-MyBatisStatements([string]$Path) {
    $settings = [System.Xml.XmlReaderSettings]::new()
    $settings.DtdProcessing = [System.Xml.DtdProcessing]::Ignore
    $settings.XmlResolver = $null
    $reader = [System.Xml.XmlReader]::Create($Path, $settings)
    try {
        $document = [System.Xml.XmlDocument]::new()
        $document.XmlResolver = $null
        $document.Load($reader)
        return @($document.SelectNodes('/mapper/insert|/mapper/update|/mapper/select') | ForEach-Object {
            [pscustomobject]@{ key = "$(Split-Path -Leaf $Path).$($_.GetAttribute('id'))"; sql = ConvertTo-ParseSql ([string]$_.InnerText) }
        })
    } finally { $reader.Dispose() }
}

function Invoke-PostgreSqlParse($Target, [string]$Username, [string]$Password, [string]$Sql) {
    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = [string]$Target.clientPath
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    foreach ($arg in @('-X','-v','ON_ERROR_STOP=1','-h',[string]$Target.host,'-p',[string]$Target.port,'-U',$Username,'-d',[string]$Target.databaseName)) { [void]$psi.ArgumentList.Add($arg) }
    $psi.Environment['PGPASSWORD'] = $Password
    $process = [Diagnostics.Process]::Start($psi)
    try {
        $process.StandardInput.WriteLine('BEGIN;')
        $process.StandardInput.WriteLine('EXPLAIN ' + $Sql.TrimEnd(';') + ';')
        $process.StandardInput.WriteLine('ROLLBACK;')
        $process.StandardInput.WriteLine('\q')
        $process.StandardInput.Close()
        [void]$process.StandardOutput.ReadToEnd()
        $errorText = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        return [pscustomobject]@{ ok = ($process.ExitCode -eq 0); error = $errorText }
    } finally { if (-not $process.HasExited) { $process.Kill($true) }; $process.Dispose() }
}

function Get-OracleQQuote([string]$Sql) {
    foreach ($delimiter in @('~','!','^','|','#')) {
        if (-not $Sql.Contains($delimiter)) { return "q'$delimiter$Sql$delimiter'" }
    }
    throw 'Oracle DBMS_SQL parse delimiter를 선택할 수 없습니다.'
}

function Invoke-OracleParse($Target, [string]$Username, [string]$Password, [string]$Sql) {
    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = [string]$Target.clientPath
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    [void]$psi.ArgumentList.Add('/nolog')
    $process = [Diagnostics.Process]::Start($psi)
    try {
        $safeUser = $Username.Replace('"','""')
        $safePassword = $Password.Replace('"','""')
        $connect = "$safeUser/`"$safePassword`"@//$($Target.host):$($Target.port)/$($Target.databaseName)"
        $parseSql = $Sql.Trim()
        if ($parseSql -notmatch '(?is)^(BEGIN|DECLARE)\b') { $parseSql = $parseSql.TrimEnd(';') }
        $quotedSql = Get-OracleQQuote $parseSql
        $script = @"
SET ECHO OFF FEEDBACK OFF HEADING OFF PAGESIZE 0 VERIFY OFF TERMOUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE
CONNECT $connect
DECLARE
  c INTEGER;
BEGIN
  c := DBMS_SQL.OPEN_CURSOR;
  DBMS_SQL.PARSE(c, $quotedSql, DBMS_SQL.NATIVE);
  DBMS_SQL.CLOSE_CURSOR(c);
END;
/
EXIT
"@
        $process.StandardInput.Write($script)
        $process.StandardInput.Close()
        [void]$process.StandardOutput.ReadToEnd()
        $errorText = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        return [pscustomobject]@{ ok = ($process.ExitCode -eq 0); error = $errorText }
    } finally { if (-not $process.HasExited) { $process.Kill($true) }; $process.Dispose() }
}

foreach ($moduleCode in @($Module | Sort-Object -Unique)) {
    $profileKey = $moduleKeyMap[$moduleCode]
    $rawProperty = $profile.modules.PSObject.Properties[$profileKey]
    if ($null -eq $rawProperty -or -not [bool]$rawProperty.Value.enabled) { throw "DB Profile Module이 활성화되지 않았습니다: $profileKey" }
    $rawModule = $rawProperty.Value
    $target = ConvertTo-CpfModuleProfile -Profile $profile -ModuleKey $profileKey -SkipSecretResolution
    if ([string]$target.vendor -ne $Vendor) { throw "Profile Vendor mismatch: module=$profileKey expected=$Vendor actual=$($target.vendor)" }
    if (-not (Test-Path -LiteralPath $target.clientPath -PathType Leaf)) { throw "DB client가 없습니다: $($target.clientPath)" }
    $allowDevDefault = ([string]$profile.environment).ToLowerInvariant() -in @('development','dev','local') -and [bool]$profile.policy.allowInlineDevDefaults
    $password = Resolve-CpfProfileSecret -SecretSpec $rawModule.runtime.password -DisplayName "$profileKey.runtime.password" -AllowDevDefault $allowDevDefault
    $username = [string]$rawModule.runtime.username
    $statements = [System.Collections.Generic.List[object]]::new()
    $repoRoot = Join-Path $Root "cpf-tools/db/vendor/$Vendor/runtime/$moduleCode/repository"
    if (-not (Test-Path -LiteralPath $repoRoot -PathType Container)) { throw "Runtime Query repository가 없습니다: $repoRoot" }
    foreach ($file in Get-ChildItem -LiteralPath $repoRoot -File -Filter '*.sql' | Sort-Object Name) {
        $statements.Add([pscustomobject]@{ key = $file.BaseName; sql = ConvertTo-ParseSql ([IO.File]::ReadAllText($file.FullName, [Text.Encoding]::UTF8)) })
    }
    if ($moduleCode -eq 'cpf') {
        $mapperRoot = Join-Path $Root "cpf-tools/db/vendor/$Vendor/runtime/cpf/mybatis/logging"
        foreach ($mapper in Get-ChildItem -LiteralPath $mapperRoot -File -Filter '*Mapper.xml' | Sort-Object Name) {
            foreach ($statement in Read-MyBatisStatements $mapper.FullName) { $statements.Add($statement) }
        }
    }
    $passed = 0
    try {
        foreach ($statement in $statements) {
            $result = if ($Vendor -eq 'postgresql') {
                Invoke-PostgreSqlParse -Target $target -Username $username -Password $password -Sql ([string]$statement.sql)
            } else {
                Invoke-OracleParse -Target $target -Username $username -Password $password -Sql ([string]$statement.sql)
            }
            if ($result.ok) { $passed++; continue }
            $safeError = ([string]$result.error -replace '\r?\n',' ').Trim()
            if ($safeError.Length -gt 500) { $safeError = $safeError.Substring(0,500) }
            $failures.Add([pscustomobject]@{ module = $moduleCode; key = [string]$statement.key; error = $safeError })
        }
    } finally { $password = $null }
    $results.Add([pscustomobject]@{ module = $moduleCode; vendor = $Vendor; database = [string]$target.databaseName; statements = $statements.Count; passed = $passed })
}

$finishedAt = [DateTimeOffset]::Now
$evidence = [ordered]@{
    baselineCommit = '2daef3b7d2f82745d42d9b19804dde4bcac60edb'
    vendor = $Vendor
    profile = [IO.Path]::GetFileName($ProfilePath)
    startedAt = $startedAt.ToString('o')
    finishedAt = $finishedAt.ToString('o')
    modules = $results
    failureCount = $failures.Count
    failures = $failures
    sensitiveData = 'SANITIZED: credentials are never written to evidence.'
    status = if ($failures.Count -eq 0) { 'PASS' } else { 'FAIL' }
}
if (-not [string]::IsNullOrWhiteSpace($EvidencePath)) {
    $parent = Split-Path -Parent $EvidencePath
    if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    [IO.File]::WriteAllText($EvidencePath, ($evidence | ConvertTo-Json -Depth 10) + "`n", [Text.UTF8Encoding]::new($false))
}
$evidence | ConvertTo-Json -Depth 10
if ($failures.Count -gt 0) { throw "Official DB Runtime Query compile smoke failed: vendor=$Vendor failures=$($failures.Count)" }
