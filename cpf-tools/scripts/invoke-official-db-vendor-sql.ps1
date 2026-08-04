param(
    [Parameter(Mandatory=$true)][ValidateSet('postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory=$true)][ValidateSet('provision','install','productSeed','optionalSampleSeed','testSeed','verify')][string]$Mode,
    [string]$ProfilePath,
    [string[]]$Modules = @()
)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
. (Join-Path $PSScriptRoot 'database-profile-common.ps1')
if ([string]::IsNullOrWhiteSpace($ProfilePath)) { $ProfilePath=Join-Path $Root 'cpf-tools/config/database-install.default.json' }
$profile=Get-CpfDatabaseProfile $ProfilePath
$manifest=Get-Content (Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$entry=$manifest.vendors.$Vendor
if ($null -eq $entry) { throw "Vendor manifest missing: $Vendor" }
$pathKey = @{provision='provision';install='emptyInstall';productSeed='productSeed';optionalSampleSeed='optionalSampleSeed';testSeed='testSeed';verify='verify'}[$Mode]
$file=Join-Path $Root ([string]$entry.lifecycle.$pathKey)
if (-not (Test-Path $file)) { throw "Lifecycle file missing: $file" }
$text=Get-Content $file -Raw -Encoding UTF8

function Get-Section([string]$Sql,[string]$LogicalDb) {
    $markerPattern='(?im)^\s*--\s*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)\s*$'
    $markers=[regex]::Matches($Sql,$markerPattern)
    $selected=[Collections.Generic.List[string]]::new()
    for($index=0;$index -lt $markers.Count;$index++){
        $marker=$markers[$index]
        if($marker.Groups[1].Value -ine $LogicalDb){continue}
        $end=if($index+1 -lt $markers.Count){$markers[$index+1].Index}else{$Sql.Length}
        $section=$Sql.Substring($marker.Index,$end-$marker.Index)
        $selected.Add([regex]::Replace(
            $section,
            '(?im)^\s*--\s*CPF_(?:USE_)?LOGICAL_DATABASE=.*$',
            ''
        ))
    }
    return ($selected -join "`n").Trim()
}
function Assert-VerifyOutput([object[]]$Output,[string]$ModuleKey) {
    $rows=[Collections.Generic.List[object]]::new()
    foreach($item in @($Output)){
        foreach($line in ([string]$item -split '\r?\n')){
            $match=[regex]::Match($line,'^\s*(?<name>[^|]+?)\s*\|\s*(?<passed>[01])\s*$')
            if($match.Success){
                $rows.Add([pscustomobject]@{
                    name=$match.Groups['name'].Value.Trim()
                    passed=$match.Groups['passed'].Value
                })
            }
        }
    }
    if($rows.Count -eq 0){throw "Verify output contract violation: vendor=$Vendor module=$ModuleKey rows=0"}
    $failed=@($rows | Where-Object {$_.passed -ne '1'} | ForEach-Object {$_.name})
    if($failed.Count -gt 0){
        throw "Verify failed: vendor=$Vendor module=$ModuleKey checks=$($failed -join ',')"
    }
    Write-Host "[$ModuleKey] vendor=$Vendor verify checks=$($rows.Count) PASS"
}
function Invoke-Psql($t,[string]$Sql) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'psql'}else{$t.clientPath}
    $tmp=[IO.Path]::GetTempFileName()+'.sql'; try {
        $prefix="\set ON_ERROR_STOP on`n"
        if(-not [string]::IsNullOrWhiteSpace($t.schemaName)){ $prefix += 'SET search_path TO "'+$t.schemaName.Replace('"','""')+'";'+"`n" }
        [IO.File]::WriteAllText($tmp,$prefix+$Sql,[Text.UTF8Encoding]::new($false))
        $old=$env:PGPASSWORD; $env:PGPASSWORD=$t.migrationPassword
        if($Mode -eq 'verify'){
            $verifyOutput=@(& $client -X -q -A -t -F '|' -h $t.host -p $t.port -U $t.migrationUsername -d $t.databaseName -f $tmp 2>&1)
            if($LASTEXITCODE -ne 0){throw "psql failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
            Assert-VerifyOutput $verifyOutput $t.moduleKey
        }else{
            & $client -h $t.host -p $t.port -U $t.migrationUsername -d $t.databaseName -f $tmp
            if($LASTEXITCODE -ne 0){throw "psql failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
        }
    } finally { $env:PGPASSWORD=$old; Remove-Item $tmp -Force -ErrorAction SilentlyContinue }
}
function Protect-CpfSecretText([string]$Text,[string[]]$Secrets) {
    if($null -eq $Text){return ''}
    $safe=$Text
    foreach($secret in @($Secrets)){
        if(-not [string]::IsNullOrWhiteSpace($secret)){$safe=$safe.Replace($secret,'****')}
    }
    return $safe
}
function Invoke-SqlPlusText($t,[string]$Username,[string]$Password,[string]$Sql,[switch]$Verify) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'sqlplus'}else{$t.clientPath}
    $passwordLiteral=$Password.Replace('"','""')
    $connect='CONNECT '+$Username+'/"'+$passwordLiteral+'"@//'+$t.host+':'+$t.port+'/'+$t.databaseName
    $script="WHENEVER SQLERROR EXIT SQL.SQLCODE`n"+$connect+"`n"+$Sql+"`nEXIT`n"

    $psi=[Diagnostics.ProcessStartInfo]::new()
    $psi.FileName=$client
    $psi.UseShellExecute=$false
    $psi.RedirectStandardInput=$true
    $psi.RedirectStandardOutput=$true
    $psi.RedirectStandardError=$true
    $psi.CreateNoWindow=$true
    $psi.StandardInputEncoding=[Text.Encoding]::UTF8
    $psi.StandardOutputEncoding=[Text.Encoding]::UTF8
    $psi.StandardErrorEncoding=[Text.Encoding]::UTF8
    foreach($argument in @('-L','-S','/nolog')){[void]$psi.ArgumentList.Add($argument)}

    $process=[Diagnostics.Process]::new()
    $process.StartInfo=$psi
    [void]$process.Start()
    $stdoutTask=$process.StandardOutput.ReadToEndAsync()
    $stderrTask=$process.StandardError.ReadToEndAsync()
    try{$process.StandardInput.Write($script)}finally{$process.StandardInput.Close()}
    $process.WaitForExit()
    $stdout=$stdoutTask.GetAwaiter().GetResult()
    $stderr=$stderrTask.GetAwaiter().GetResult()
    if($process.ExitCode -ne 0){
        $safe=Protect-CpfSecretText (($stderr+"`n"+$stdout).Trim()) @($Password)
        throw "sqlplus failed module=$($t.moduleKey) exit=$($process.ExitCode) error=$safe"
    }
    if($Verify){
        Assert-VerifyOutput @($stdout -split '\r?\n') $t.moduleKey
    } elseif(-not [string]::IsNullOrWhiteSpace($stdout)) {
        Write-Host (Protect-CpfSecretText $stdout @($Password))
    }
}
function Invoke-SqlPlus($t,[string]$Sql) {
    $schema=""; if(-not [string]::IsNullOrWhiteSpace($t.schemaName)){ $schema="ALTER SESSION SET CURRENT_SCHEMA = $($t.schemaName);`n" }
    $outputContract=if($Mode -eq 'verify'){
        "SET HEADING OFF`nSET FEEDBACK OFF`nSET PAGESIZE 0`nSET TRIMSPOOL ON`nSET COLSEP |`n"
    }else{''}
    Invoke-SqlPlusText $t $t.migrationUsername $t.migrationPassword ($outputContract+$schema+$Sql) -Verify:($Mode -eq 'verify')
}
$targets=@($profile.modules.PSObject.Properties | ForEach-Object { ConvertTo-CpfModuleProfile $profile $_.Name } | Where-Object { $_.enabled -and $_.vendor -eq $Vendor })
if($Modules.Count -gt 0){$targets=@($targets | Where-Object {$_.moduleKey -in $Modules})}
function Q-Sql([string]$value) { return $value.Replace("'", "''") }
function Provision-Postgresql($t) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'psql'}else{$t.clientPath}
    $tmp=[IO.Path]::GetTempFileName()+'.sql'; try {
        $db=$t.databaseName; $mig=$t.migrationUsername; $run=$t.runtimeUsername
        $migPwd=Q-Sql $t.migrationPassword; $runPwd=Q-Sql $t.runtimePassword
        $schema=if([string]::IsNullOrWhiteSpace($t.schemaName)){'public'}else{$t.schemaName}
        $dbLiteral=Q-Sql $db
        $migrationLiteral=Q-Sql $mig
        $runtimeLiteral=Q-Sql $run
        $script=@"
\set ON_ERROR_STOP on
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', '$migrationLiteral', '$migPwd')
 WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='$migrationLiteral') \gexec
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', '$runtimeLiteral', '$runPwd')
 WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='$runtimeLiteral') \gexec
SELECT format('CREATE DATABASE %I OWNER %I', '$dbLiteral', '$migrationLiteral')
 WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='$dbLiteral') \gexec
\connect "$db"
CREATE SCHEMA IF NOT EXISTS "$schema" AUTHORIZATION "$mig";
GRANT CONNECT ON DATABASE "$db" TO "$run";
GRANT USAGE ON SCHEMA "$schema" TO "$run";
"@
        [IO.File]::WriteAllText($tmp,$script,[Text.UTF8Encoding]::new($false)); $old=$env:PGPASSWORD; $env:PGPASSWORD=$t.adminPassword
        & $client -h $t.host -p $t.port -U $t.adminUsername -d postgres -f $tmp
        if($LASTEXITCODE -ne 0){throw "PostgreSQL provision failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
    } finally {$env:PGPASSWORD=$old;Remove-Item $tmp -Force -ErrorAction SilentlyContinue}
}
function Provision-Oracle($t) {
    $mig=$t.migrationUsername.ToUpperInvariant();$run=$t.runtimeUsername.ToUpperInvariant()
    $mp=$t.migrationPassword.Replace('"','""').Replace("'","''")
    $rp=$t.runtimePassword.Replace('"','""').Replace("'","''")
    $script=@"
DECLARE
    c NUMBER;
BEGIN
    SELECT COUNT(*) INTO c FROM dba_users WHERE username='$mig';
    IF c=0 THEN
        EXECUTE IMMEDIATE 'CREATE USER $mig IDENTIFIED BY "$mp"';
    END IF;
END;
/
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE, CREATE VIEW TO $mig;
ALTER USER $mig QUOTA UNLIMITED ON USERS;
DECLARE
    c NUMBER;
BEGIN
    SELECT COUNT(*) INTO c FROM dba_users WHERE username='$run';
    IF c=0 THEN
        EXECUTE IMMEDIATE 'CREATE USER $run IDENTIFIED BY "$rp"';
    END IF;
END;
/
GRANT CREATE SESSION TO $run;
"@
    Invoke-SqlPlusText $t $t.adminUsername $t.adminPassword $script
}
function Grant-Runtime($t) {
    if($Vendor -eq 'postgresql'){
        $schema=if([string]::IsNullOrWhiteSpace($t.schemaName)){'public'}else{$t.schemaName};$run=$t.runtimeUsername
        $grantSql=@"
GRANT USAGE ON SCHEMA "$schema" TO "$run";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA "$schema" TO "$run";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA "$schema" TO "$run";
ALTER DEFAULT PRIVILEGES IN SCHEMA "$schema"
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "$run";
"@
        Invoke-Psql $t $grantSql
    } else {
        $run=$t.runtimeUsername.ToUpperInvariant(); Invoke-SqlPlus $t ("BEGIN FOR r IN (SELECT table_name FROM user_tables) LOOP EXECUTE IMMEDIATE 'GRANT SELECT,INSERT,UPDATE,DELETE ON '||r.table_name||' TO $run'; END LOOP; END;`n/")
    }
}
foreach($t in $targets){
    if([string]::IsNullOrWhiteSpace($t.databaseName)){throw "databaseName required for $Vendor module=$($t.moduleKey)"}
    if($Mode -eq 'provision'){if($Vendor -eq 'postgresql'){Provision-Postgresql $t}else{Provision-Oracle $t};Write-Host "[$($t.moduleKey)] vendor=$Vendor mode=provision OK";continue}
    $sql=Get-Section $text $t.logicalDatabase
    if([string]::IsNullOrWhiteSpace($sql)){Write-Host "[$($t.moduleKey)] no section; SKIP";continue}
    if($Vendor -eq 'postgresql'){Invoke-Psql $t $sql}else{Invoke-SqlPlus $t $sql}
    if($Mode -eq 'install'){Grant-Runtime $t}
    Write-Host "[$($t.moduleKey)] vendor=$Vendor mode=$Mode OK"
}
