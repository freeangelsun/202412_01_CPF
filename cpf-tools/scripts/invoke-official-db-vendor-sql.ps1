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
    if ($Mode -eq 'verify') { return $Sql }
    $pattern='(?im)^\s*--\s*CPF_LOGICAL_DATABASE='+[regex]::Escape($LogicalDb)+'\s*$'
    $m=[regex]::Match($Sql,$pattern)
    if (-not $m.Success) { return '' }
    $next=[regex]::Match($Sql.Substring($m.Index+$m.Length),'(?im)^\s*--\s*CPF_LOGICAL_DATABASE=[A-Za-z][A-Za-z0-9_$#]*\s*$')
    $end=if($next.Success){$m.Index+$m.Length+$next.Index}else{$Sql.Length}
    $section=$Sql.Substring($m.Index,$end-$m.Index)
    return [regex]::Replace($section,'(?im)^\s*--\s*CPF_(?:USE_)?LOGICAL_DATABASE=.*$','')
}
function Invoke-Psql($t,[string]$Sql) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'psql'}else{$t.clientPath}
    $tmp=[IO.Path]::GetTempFileName()+'.sql'; try {
        $prefix="\\set ON_ERROR_STOP on`n"
        if(-not [string]::IsNullOrWhiteSpace($t.schemaName)){ $prefix += 'SET search_path TO "'+$t.schemaName.Replace('"','""')+'";'+"`n" }
        [IO.File]::WriteAllText($tmp,$prefix+$Sql,[Text.UTF8Encoding]::new($false))
        $old=$env:PGPASSWORD; $env:PGPASSWORD=$t.migrationPassword
        & $client -h $t.host -p $t.port -U $t.migrationUsername -d $t.databaseName -f $tmp
        if($LASTEXITCODE -ne 0){throw "psql failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
    } finally { $env:PGPASSWORD=$old; Remove-Item $tmp -Force -ErrorAction SilentlyContinue }
}
function Invoke-SqlPlus($t,[string]$Sql) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'sqlplus'}else{$t.clientPath}
    $tmp=[IO.Path]::GetTempFileName()+'.sql'; try {
        $schema=""; if(-not [string]::IsNullOrWhiteSpace($t.schemaName)){ $schema="ALTER SESSION SET CURRENT_SCHEMA = $($t.schemaName);`n" }
        [IO.File]::WriteAllText($tmp,"WHENEVER SQLERROR EXIT SQL.SQLCODE`n"+$schema+$Sql+"`nEXIT`n",[Text.UTF8Encoding]::new($false))
        $connect=$t.migrationUsername+'/"'+$t.migrationPassword.Replace('"','""')+'"@//'+$t.host+':'+$t.port+'/'+$t.databaseName
        & $client -L -S $connect "@$tmp"
        if($LASTEXITCODE -ne 0){throw "sqlplus failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
    } finally { Remove-Item $tmp -Force -ErrorAction SilentlyContinue }
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
        $script="\set ON_ERROR_STOP on`nSELECT format('CREATE ROLE %I LOGIN PASSWORD %L', '$mig', '$migPwd') WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='$mig') \gexec`nSELECT format('CREATE ROLE %I LOGIN PASSWORD %L', '$run', '$runPwd') WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='$run') \gexec`nSELECT format('CREATE DATABASE %I OWNER %I', '$db', '$mig') WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='$db') \gexec`n\connect "$db"`nCREATE SCHEMA IF NOT EXISTS "$schema" AUTHORIZATION "$mig";`nGRANT CONNECT ON DATABASE "$db" TO "$run";`nGRANT USAGE ON SCHEMA "$schema" TO "$run";`n"
        [IO.File]::WriteAllText($tmp,$script,[Text.UTF8Encoding]::new($false)); $old=$env:PGPASSWORD; $env:PGPASSWORD=$t.adminPassword
        & $client -h $t.host -p $t.port -U $t.adminUsername -d postgres -f $tmp
        if($LASTEXITCODE -ne 0){throw "PostgreSQL provision failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
    } finally {$env:PGPASSWORD=$old;Remove-Item $tmp -Force -ErrorAction SilentlyContinue}
}
function Provision-Oracle($t) {
    $client=if([string]::IsNullOrWhiteSpace($t.clientPath)){'sqlplus'}else{$t.clientPath};$tmp=[IO.Path]::GetTempFileName()+'.sql'
    try {
        $mig=$t.migrationUsername.ToUpperInvariant();$run=$t.runtimeUsername.ToUpperInvariant();$mp=$t.migrationPassword.Replace('"','""');$rp=$t.runtimePassword.Replace('"','""')
        $script="WHENEVER SQLERROR EXIT SQL.SQLCODE`nDECLARE c NUMBER; BEGIN SELECT COUNT(*) INTO c FROM dba_users WHERE username='$mig'; IF c=0 THEN EXECUTE IMMEDIATE 'CREATE USER $mig IDENTIFIED BY "$mp"'; END IF; END;`n/`nGRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE, CREATE VIEW TO $mig;`nALTER USER $mig QUOTA UNLIMITED ON USERS;`nDECLARE c NUMBER; BEGIN SELECT COUNT(*) INTO c FROM dba_users WHERE username='$run'; IF c=0 THEN EXECUTE IMMEDIATE 'CREATE USER $run IDENTIFIED BY "$rp"'; END IF; END;`n/`nGRANT CREATE SESSION TO $run;`nEXIT`n"
        [IO.File]::WriteAllText($tmp,$script,[Text.UTF8Encoding]::new($false));$connect=$t.adminUsername+'/"'+$t.adminPassword.Replace('"','""')+'"@//'+$t.host+':'+$t.port+'/'+$t.databaseName
        & $client -L -S $connect "@$tmp";if($LASTEXITCODE -ne 0){throw "Oracle provision failed module=$($t.moduleKey) exit=$LASTEXITCODE"}
    } finally {Remove-Item $tmp -Force -ErrorAction SilentlyContinue}
}
function Grant-Runtime($t) {
    if($Vendor -eq 'postgresql'){
        $schema=if([string]::IsNullOrWhiteSpace($t.schemaName)){'public'}else{$t.schemaName};$run=$t.runtimeUsername
        Invoke-Psql $t ("GRANT USAGE ON SCHEMA "$schema" TO "$run"; GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA "$schema" TO "$run"; GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA "$schema" TO "$run"; ALTER DEFAULT PRIVILEGES IN SCHEMA "$schema" GRANT SELECT,INSERT,UPDATE,DELETE ON TABLES TO "$run";")
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
