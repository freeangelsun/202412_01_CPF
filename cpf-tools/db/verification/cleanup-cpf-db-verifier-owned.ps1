[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory=$true)][string]$ProfilePath,
    [Parameter(Mandatory=$true)][string]$VerifierRunId,
    [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
if($VerifierRunId -notmatch '^[a-f0-9]{8,24}$'){throw 'Invalid verifier run id.'}
$rootPath=(Resolve-Path -LiteralPath $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/database-profile-common.ps1')
$profile=Get-CpfDatabaseProfile (Resolve-Path -LiteralPath $ProfilePath).Path
$environment=([string]$profile.environment).Trim().ToLowerInvariant()
if($environment -notin @('development','dev','local','test')){throw "Refusing verifier cleanup for environment=$environment"}
$targets=@($profile.modules.PSObject.Properties|ForEach-Object{ConvertTo-CpfModuleProfile $profile $_.Name}|Where-Object{$_.enabled -and $_.vendor -eq $Vendor})
if($targets.Count -eq 0){throw 'No verifier-owned DB targets found.'}
$allowedHosts=@('mariadb','cpf-mariadb','postgresql','cpf-postgresql','oracle','cpf-oracle')
foreach($t in $targets){
    if(([string]$t.host).Trim().ToLowerInvariant() -notin $allowedHosts){throw "Refusing verifier cleanup for host=$($t.host)"}
    if($Vendor -in @('mariadb','postgresql')){
        $prefix="cpf_verify_${VerifierRunId}_"
        if(-not ([string]$t.databaseName).ToLowerInvariant().StartsWith($prefix)){throw "Refusing verifier cleanup for database=$($t.databaseName)"}
    }else{
        $prefix="cpfv_${VerifierRunId}_"
        if(-not ([string]$t.schemaName).ToLowerInvariant().StartsWith($prefix)){throw "Refusing verifier cleanup for schema=$($t.schemaName)"}
    }
}
$first=$targets[0]
if($Vendor -eq 'mariadb'){
    $old=$env:MYSQL_PWD; $env:MYSQL_PWD=$first.adminPassword
    try{
        $dbs=@($targets.databaseName|Sort-Object -Unique)
        $users=@($targets|ForEach-Object{$_.migrationUsername;$_.runtimeUsername}|Sort-Object -Unique)
        $sql=($dbs|ForEach-Object{"DROP DATABASE IF EXISTS ``$_``;"}) -join "`n"
        foreach($u in $users){$sql += "`nDROP USER IF EXISTS '$u'@'%';"}
        & mariadb --protocol=tcp -h $first.host -P $first.port -u $first.adminUsername -e $sql
        if($LASTEXITCODE -ne 0){throw "MariaDB verifier cleanup failed exit=$LASTEXITCODE"}
    }finally{$env:MYSQL_PWD=$old}
}elseif($Vendor -eq 'postgresql'){
    $old=$env:PGPASSWORD; $env:PGPASSWORD=$first.adminPassword
    try{
        foreach($db in @($targets.databaseName|Sort-Object -Unique)){
            & psql -X -v ON_ERROR_STOP=1 -h $first.host -p $first.port -U $first.adminUsername -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$db' AND pid<>pg_backend_pid();" *> $null
            $dropDatabaseSql = 'DROP DATABASE IF EXISTS "{0}";' -f $db.Replace('"','""')
            & psql -X -v ON_ERROR_STOP=1 -h $first.host -p $first.port -U $first.adminUsername -d postgres -c $dropDatabaseSql
            if($LASTEXITCODE -ne 0){throw "PostgreSQL verifier database cleanup failed database=$db exit=$LASTEXITCODE"}
        }
        foreach($u in @($targets|ForEach-Object{$_.migrationUsername;$_.runtimeUsername}|Sort-Object -Unique)){
            $dropRoleSql = 'DROP ROLE IF EXISTS "{0}";' -f $u.Replace('"','""')
            & psql -X -v ON_ERROR_STOP=1 -h $first.host -p $first.port -U $first.adminUsername -d postgres -c $dropRoleSql
            if($LASTEXITCODE -ne 0){throw "PostgreSQL verifier role cleanup failed role=$u exit=$LASTEXITCODE"}
        }
    }finally{$env:PGPASSWORD=$old}
}else{
    $users=@($targets|ForEach-Object{$_.migrationUsername;$_.runtimeUsername}|Sort-Object -Unique)
    foreach($u in $users){if($u -notmatch '^[A-Za-z][A-Za-z0-9_$#]{0,62}$'){throw "Unsafe Oracle verifier user=$u"}}
    $pwd=[string]$first.adminPassword
    $connect = '{0}/"{1}"@//{2}:{3}/{4}' -f ([string]$first.adminUsername), ($pwd.Replace('"','""')), ([string]$first.host), ([string]$first.port), ([string]$first.databaseName)
    $sql="WHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT $connect`n"
    foreach($u in $users){$upper=$u.ToUpperInvariant();$sql += "DECLARE c NUMBER; BEGIN SELECT COUNT(*) INTO c FROM dba_users WHERE username='$upper'; IF c>0 THEN EXECUTE IMMEDIATE 'DROP USER $upper CASCADE'; END IF; END;`n/`n"}
    $sql += "EXIT`n"
    $psi=[Diagnostics.ProcessStartInfo]::new('sqlplus')
    $psi.UseShellExecute=$false;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$psi.CreateNoWindow=$true
    [void]$psi.ArgumentList.Add('-L');[void]$psi.ArgumentList.Add('-S');[void]$psi.ArgumentList.Add('/nolog')
    $proc=[Diagnostics.Process]::new();$proc.StartInfo=$psi;[void]$proc.Start();$out=$proc.StandardOutput.ReadToEndAsync();$err=$proc.StandardError.ReadToEndAsync()
    try{$proc.StandardInput.Write($sql)}finally{$proc.StandardInput.Close()};$proc.WaitForExit()
    if($proc.ExitCode -ne 0){throw "Oracle verifier cleanup failed exit=$($proc.ExitCode)"}
}
Write-Host "CPF verifier-owned DB cleanup PASS vendor=$Vendor runId=$VerifierRunId"
