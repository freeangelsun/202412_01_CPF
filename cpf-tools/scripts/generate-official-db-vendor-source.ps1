param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$schemaPath=Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$seedPath=Join-Path $Root 'cpf-tools/db/canonical/seed-model.json'
$profilePath=Join-Path $Root 'cpf-tools/config/database-install.default.json'
$schema=Get-Content $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$seed=Get-Content $seedPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$profile=Get-Content $profilePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$vendors=@('mariadb','postgresql','oracle')
$logicalDatabases=@($schema.tables | ForEach-Object { [string]$_.logicalDatabase } | Sort-Object -Unique)
$platformModules=@(
 $profile.modules.PSObject.Properties |
  Where-Object { [bool]$_.Value.enabled } |
  ForEach-Object { $_.Value }
)
$profileLogicalDatabases=@($platformModules | ForEach-Object { [string]$_.logicalDatabase } | Sort-Object -Unique)
if(($logicalDatabases -join "`n") -cne ($profileLogicalDatabases -join "`n")){
 throw "Canonical schema/profile logical database drift. schema=$($logicalDatabases -join ',') profile=$($profileLogicalDatabases -join ',')"
}
$fileByDb=@{cpfDB='10_cpf_schema.sql';cmnDB='20_cmn_schema.sql';admDB='30_adm_schema.sql';batDB='35_bat_schema.sql';bzaDB='40_business_modules_schema.sql';refDB='40_business_modules_schema.sql'}
function W([string]$p,[string]$s){New-Item -ItemType Directory -Force -Path (Split-Path -Parent $p)|Out-Null;[IO.File]::WriteAllText($p,$s.TrimEnd()+"`n",[Text.UTF8Encoding]::new($false))}
function Assert-DbIdentifier([string]$value,[string]$name){
 if($value -cnotmatch '^[A-Za-z][A-Za-z0-9_$#]{1,62}$'){throw "Invalid $name in canonical DB profile: $value"}
}
function Sql-Quote([string]$value){return $value.Replace("'","''")}
foreach($module in $platformModules){
 Assert-DbIdentifier ([string]$module.logicalDatabase) 'logicalDatabase'
 Assert-DbIdentifier ([string]$module.migration.username) 'migration username'
 Assert-DbIdentifier ([string]$module.runtime.username) 'runtime username'
}

# Provision contracts are projections of the platform install profile. Generated
# domains are provisioned from their manifest/template and never enter this list.
$mariaDatabaseSource=@(
 '-- AUTO-GENERATED from cpf-tools/config/database-install.default.json'
 '-- vendor=mariadb; platform provision databases'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
foreach($module in $platformModules){
 $db=[string]$module.logicalDatabase
 $mariaDatabaseSource+=@(
  "CREATE DATABASE IF NOT EXISTS $db"
  '  DEFAULT CHARACTER SET utf8mb4'
  '  DEFAULT COLLATE utf8mb4_unicode_ci;'
  ''
 )
}
W (Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/01_create_databases.sql') ($mariaDatabaseSource -join "`n")

$mariaUserSource=@(
 '-- AUTO-GENERATED from cpf-tools/config/database-install.default.json'
 '-- vendor=mariadb; platform service users and least-privilege grants'
 '-- The caller must set @cpf_migration_password and @cpf_app_password in-memory.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
$createdMigrationAccounts=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$createdRuntimeAccounts=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach($module in $platformModules){
 foreach($accountType in @('migration','runtime')){
  $account=$module.$accountType
  $username=[string]$account.username
  $accountHost=[string]$account.userHost
  if([string]::IsNullOrWhiteSpace($accountHost)){$accountHost='%'}
  $accountKey="$username@$accountHost"
  $accountIsNew=if($accountType -eq 'migration'){
   $createdMigrationAccounts.Add($accountKey)
  }else{
   $createdRuntimeAccounts.Add($accountKey)
  }
  if(-not $accountIsNew){continue}
  $quotedAccount="'$(Sql-Quote $username)'@'$(Sql-Quote $accountHost)'"
  $dynamicAccount=$quotedAccount.Replace("'","''")
  $passwordVariable=if($accountType -eq 'migration'){'@cpf_migration_password'}else{'@cpf_app_password'}
  $mariaUserSource+=@(
   "SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS $dynamicAccount IDENTIFIED BY ', QUOTE(NULLIF($passwordVariable, '')));"
   'PREPARE cpf_user_stmt FROM @cpf_sql;'
   'EXECUTE cpf_user_stmt;'
   'DEALLOCATE PREPARE cpf_user_stmt;'
   ''
  )
 }
}
foreach($module in $platformModules){
 $db=[string]$module.logicalDatabase
 $migration=$module.migration
 $runtime=$module.runtime
 $migrationHost=if([string]::IsNullOrWhiteSpace([string]$migration.userHost)){'%'}else{[string]$migration.userHost}
 $runtimeHost=if([string]::IsNullOrWhiteSpace([string]$runtime.userHost)){'%'}else{[string]$runtime.userHost}
 $migrationAccount="'$(Sql-Quote ([string]$migration.username))'@'$(Sql-Quote $migrationHost)'"
 $runtimeAccount="'$(Sql-Quote ([string]$runtime.username))'@'$(Sql-Quote $runtimeHost)'"
 $mariaUserSource+=@(
  "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON $db.* TO $migrationAccount;"
  "GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON $db.* TO $runtimeAccount;"
  ''
 )
}
$mariaUserSource+=@('SET @cpf_sql = NULL;','FLUSH PRIVILEGES;')
W (Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/02_create_service_users.sql') ($mariaUserSource -join "`n")

$postgresProvision=@(
 '-- AUTO-GENERATED from cpf-tools/config/database-install.default.json'
 '-- vendor=postgresql; physical database/roles are created by the profile-aware executor.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
$oracleProvision=@(
 '-- AUTO-GENERATED from cpf-tools/config/database-install.default.json'
 '-- vendor=oracle; password tokens are rendered only in-memory by the executor.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
foreach($module in $platformModules){
 $db=[string]$module.logicalDatabase
 $upper=$db.ToUpperInvariant()
 $postgresProvision+=@("-- CPF_LOGICAL_DATABASE=$db","CREATE SCHEMA IF NOT EXISTS $db;",'')
 $oracleProvision+=@(
  "-- CPF_LOGICAL_DATABASE=$db"
  'BEGIN'
  "    EXECUTE IMMEDIATE 'CREATE USER $upper IDENTIFIED BY `"`${${upper}_PASSWORD}`"';"
  'EXCEPTION'
  '    WHEN OTHERS THEN'
  '        IF SQLCODE != -1920 THEN RAISE; END IF;'
  'END;'
  '/'
  "GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE TO $upper;"
  "ALTER USER $upper QUOTA UNLIMITED ON USERS;"
  ''
 )
}
W (Join-Path $Root 'cpf-tools/db/vendor/postgresql/source/00_provision.sql') ($postgresProvision -join "`n")
W (Join-Path $Root 'cpf-tools/db/vendor/oracle/source/00_provision.sql') ($oracleProvision -join "`n")

$tableCounts=@{}
foreach($group in ($schema.tables | Group-Object logicalDatabase)){$tableCounts[$group.Name]=$group.Count}
$postgresVerify=@(
 '-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json'
 '-- vendor=postgresql; each logical section executes in its profile-selected schema.'
 '-- DO NOT EDIT generated verify SQL directly.'
 ''
)
$oracleVerify=@(
 '-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json'
 '-- vendor=oracle; each logical section executes in its profile-selected schema.'
 '-- DO NOT EDIT generated verify SQL directly.'
 ''
)
foreach($module in $platformModules){
 $db=[string]$module.logicalDatabase
 $systemCode=([string]$module.systemCode).ToUpperInvariant()
 $expected=[int]$tableCounts[$db]
 $postgresVerify+=@(
  "-- CPF_LOGICAL_DATABASE=$db"
  "SELECT '$db.table_count' AS check_name,"
  "       CASE WHEN COUNT(*) = $expected THEN 1 ELSE 0 END AS passed"
  'FROM information_schema.tables'
  "WHERE table_schema = current_schema() AND table_type = 'BASE TABLE';"
  ''
 )
 $oracleVerify+=@(
  "-- CPF_LOGICAL_DATABASE=$db"
  "SELECT '$db.table_count' AS check_name,"
  "       CASE WHEN COUNT(*) = $expected THEN 1 ELSE 0 END AS passed"
  'FROM user_tables;'
  ''
 )
 if($systemCode -eq 'CPF'){
  $postgresVerify+=@(
   "SELECT 'cpfDB.product_seed' AS check_name,"
   '       CASE WHEN'
   '           (SELECT COUNT(*) FROM cpf_code) >= 100'
   '           AND (SELECT COUNT(*) FROM cpf_message) >= 40'
   '           AND (SELECT COUNT(*) FROM cpf_response_code) >= 40'
   '           AND (SELECT COUNT(*) FROM cpf_config) >= 20'
   '       THEN 1 ELSE 0 END AS passed;'
   ''
  )
  $oracleVerify+=@(
   "SELECT 'cpfDB.product_seed' AS check_name,"
   '       CASE WHEN'
   '           (SELECT COUNT(*) FROM cpf_code) >= 100'
   '           AND (SELECT COUNT(*) FROM cpf_message) >= 40'
   '           AND (SELECT COUNT(*) FROM cpf_response_code) >= 40'
   '           AND (SELECT COUNT(*) FROM cpf_config) >= 20'
   '       THEN 1 ELSE 0 END AS passed FROM dual;'
   ''
  )
 }
 if($systemCode -eq 'BZA'){
  $postgresVerify+=@(
   "SELECT 'bzaDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM bza_role WHERE use_yn = 'Y') >= 4"
   "           AND (SELECT COUNT(*) FROM bza_menu WHERE use_yn = 'Y') >= 8"
   "           AND (SELECT COUNT(*) FROM bza_permission WHERE role_code = 'BZA_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8"
   '       THEN 1 ELSE 0 END AS passed;'
   ''
  )
  $oracleVerify+=@(
   "SELECT 'bzaDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM bza_role WHERE use_yn = 'Y') >= 4"
   "           AND (SELECT COUNT(*) FROM bza_menu WHERE use_yn = 'Y') >= 8"
   "           AND (SELECT COUNT(*) FROM bza_permission WHERE role_code = 'BZA_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8"
   '       THEN 1 ELSE 0 END AS passed FROM dual;'
   ''
  )
 }
}
W (Join-Path $Root 'cpf-tools/db/vendor/postgresql/source/00_verify.sql') ($postgresVerify -join "`n")
W (Join-Path $Root 'cpf-tools/db/vendor/oracle/source/00_verify.sql') ($oracleVerify -join "`n")
function Type-For([string]$v,[string]$t){
 $u=$t.ToUpperInvariant()
 if($v -eq 'mariadb'){return $u}
 if($v -eq 'postgresql'){
  $u=$u -replace '^DATETIME','TIMESTAMP' -replace '^TINYINT','SMALLINT' -replace '^INT$','INTEGER' -replace '^LONGTEXT$','TEXT' -replace '^BLOB$','BYTEA'
  return $u
 }
 if($u -match '^VARCHAR\((\d+)\)$'){return "VARCHAR2($($Matches[1]) CHAR)"}
 if($u -match '^CHAR\((\d+)\)$'){return "CHAR($($Matches[1]) CHAR)"}
 if($u -match '^BIGINT$'){return 'NUMBER(19)'}; if($u -match '^INT$'){return 'NUMBER(10)'}; if($u -match '^TINYINT$'){return 'NUMBER(3)'}
 if($u -match '^DATETIME(\(\d+\))?$'){return 'TIMESTAMP'+$Matches[1]}; if($u -eq 'LONGTEXT' -or $u -eq 'TEXT'){return 'CLOB'}
 return $u
}
function Default-For([string]$v,$d){ if($null -eq $d){return $null};$x=[string]$d;if($v -eq 'postgresql'){$x=[regex]::Replace($x,'(?i)CURRENT_TIMESTAMP\(\d+\)','CURRENT_TIMESTAMP')};return $x }
function Q([string]$s){return $s.Replace("'","''")}
function Render-Table([string]$v,$t){
 $lines=@(); foreach($c in $t.columns){$type=Type-For $v ([string]$c.type);$line='    '+$c.name+' '+$type;if([bool]$c.autoIncrement){$line+=if($v -eq 'postgresql'){' GENERATED BY DEFAULT AS IDENTITY'}else{' GENERATED BY DEFAULT ON NULL AS IDENTITY'}};if(-not [bool]$c.nullable){$line+=' NOT NULL'};$d=Default-For $v $c.default;if($null -ne $d -and $d -ne ''){$line+=' DEFAULT '+$d};$lines+=$line}
 if($v -eq 'mariadb'){
  $lines=@()
  foreach($c in $t.columns){
   $line='    '+$c.name+' '+(Type-For $v ([string]$c.type))
   if(-not [bool]$c.nullable){$line+=' NOT NULL'}else{$line+=' NULL'}
   if([bool]$c.autoIncrement){$line+=' AUTO_INCREMENT'}
   $d=Default-For $v $c.default
   if($null -ne $d -and $d -ne ''){$line+=' DEFAULT '+$d}
   if($c.onUpdate){$line+=' ON UPDATE '+[string]$c.onUpdate}
   if($c.comment){$line+=" COMMENT '$(Q ([string]$c.comment))'"}
   $lines+=$line
  }
 }
 if($t.primaryKey.Count -gt 0){$lines+='    CONSTRAINT pk_'+$t.name+' PRIMARY KEY ('+($t.primaryKey -join ', ')+')'}
 foreach($u in $t.uniqueKeys){$lines+='    CONSTRAINT '+$u.name+' UNIQUE ('+($u.columns -join ', ')+')'}
 foreach($c in $t.checks){$lines+='    CONSTRAINT '+$c.name+' CHECK ('+$c.expression+')'}
 foreach($f in $t.foreignKeys){$line='    CONSTRAINT '+$f.name+' FOREIGN KEY ('+($f.columns -join ', ')+') REFERENCES '+$f.refTable+' ('+($f.refColumns -join ', ')+')';if($f.onDelete){$line+=' ON DELETE '+$f.onDelete};$lines+=$line}
 if($v -eq 'mariadb'){foreach($i in $t.indexes){$isUnique=$null -ne $i.PSObject.Properties['unique'] -and [bool]$i.unique;$lines+='    '+$(if($isUnique){'UNIQUE '}else{''})+'INDEX '+$i.name+' ('+($i.columns -join ', ')+')'}}
 $createPrefix=if($v -eq 'mariadb'){'CREATE TABLE IF NOT EXISTS '}else{'CREATE TABLE '}
 $createSuffix=if($v -eq 'mariadb'){"`n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"+$(if($t.comment){" COMMENT='$(Q ([string]$t.comment))'"}else{''})+";`n"}else{"`n);`n"}
 $out=$createPrefix+$t.name+" (`n"+($lines -join ",`n")+$createSuffix
 if($v -ne 'mariadb'){foreach($i in $t.indexes){$isUnique=$null -ne $i.PSObject.Properties['unique'] -and [bool]$i.unique;$portableColumns=@($i.columns | ForEach-Object { [regex]::Replace([string]$_,'\(\d+\)$','') });$out+='CREATE '+($(if($isUnique){'UNIQUE '}else{''}))+'INDEX '+$i.name+' ON '+$t.name+' ('+($portableColumns -join ', ')+");`n"}}
 if($v -ne 'mariadb'){
  if($t.comment){$out+="COMMENT ON TABLE $($t.name) IS '$(Q $t.comment)';`n"}
  foreach($c in $t.columns){if($c.comment){$out+="COMMENT ON COLUMN $($t.name).$($c.name) IS '$(Q $c.comment)';`n"}}
 }
 $touch=@($t.columns|Where-Object{$_.onUpdate}); if($touch.Count -gt 0){
  if($v -eq 'postgresql'){$fn='cpf_touch_'+$t.name;$assign=($touch|ForEach-Object{"NEW.$($_.name) = CURRENT_TIMESTAMP;"}) -join ' ';$out+="CREATE OR REPLACE FUNCTION $fn() RETURNS trigger AS `$`$ BEGIN $assign RETURN NEW; END; `$`$ LANGUAGE plpgsql;`nDROP TRIGGER IF EXISTS trg_$fn ON $($t.name);`nCREATE TRIGGER trg_$fn BEFORE UPDATE ON $($t.name) FOR EACH ROW EXECUTE FUNCTION $fn();`n"}
  elseif($v -eq 'oracle'){$assign=($touch|ForEach-Object{":NEW.$($_.name) := CURRENT_TIMESTAMP;"}) -join ' ';$out+="CREATE OR REPLACE TRIGGER trg_touch_$($t.name) BEFORE UPDATE ON $($t.name) FOR EACH ROW BEGIN $assign END;`n/`n"}
 }
 return $out
}
foreach($v in $vendors){
 $bucket=@{}; foreach($db in $fileByDb.Keys){$bucket[$fileByDb[$db]]=@()}
 foreach($db in $fileByDb.Keys){$ts=@($schema.tables|Where-Object{$_.logicalDatabase -eq $db}|Sort-Object name);if($ts.Count -eq 0){continue};$s="-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json`n-- vendor=$v`n-- DO NOT EDIT generated DDL directly.`n`n-- CPF_LOGICAL_DATABASE=$db`n";if($v -eq 'mariadb'){$s+="USE $db;`n"};foreach($t in $ts){$s+=(Render-Table $v $t)+"`n"};$bucket[$fileByDb[$db]]+=$s}
 foreach($f in $bucket.Keys){if($bucket[$f].Count -gt 0){W (Join-Path $Root "cpf-tools/db/vendor/$v/source/$f") ($bucket[$f] -join "`n")}}
}
# Seed model is canonical too. Rendering is delegated to the dedicated function below so generated SQL never copies another vendor pack.
function Convert-Expr([string]$v,[string]$x){
 $r=$x
 foreach($db in $logicalDatabases){$r=[regex]::Replace($r,'(?i)(?<![A-Za-z0-9_])'+[regex]::Escape($db)+'\.','')}
 if($v -eq 'postgresql'){
  $r=[regex]::Replace($r,'(?i)CURRENT_TIMESTAMP\(\d+\)','CURRENT_TIMESTAMP')
  $r=[regex]::Replace($r,'(?i)\bNOW\(\d*\)','CURRENT_TIMESTAMP')
  $r=[regex]::Replace($r,'(?i)\bIFNULL\s*\(','COALESCE(')
  $r=[regex]::Replace($r,'(?i)\bDATE_SUB\(([^,]+),\s*INTERVAL\s+(\d+)\s+(MINUTE|HOUR|DAY)\)',{param($m)'('+$m.Groups[1].Value+' - INTERVAL '''+$m.Groups[2].Value+' '+$m.Groups[3].Value.ToLowerInvariant()+''')'})
  $r=[regex]::Replace($r,'(?i)\bDATE_ADD\(([^,]+),\s*INTERVAL\s+(\d+)\s+(MINUTE|HOUR|DAY)\)',{param($m)'('+$m.Groups[1].Value+' + INTERVAL '''+$m.Groups[2].Value+' '+$m.Groups[3].Value.ToLowerInvariant()+''')'})
  $r=[regex]::Replace($r,'(?i)\bCONCAT\(([^,()]+),\s*([^()]+)\)','($1 || $2)')
  $r=[regex]::Replace($r,'(?i)\bAS\s+DATETIME\s*\)','AS TIMESTAMP)')
 }
 if($v -eq 'oracle'){
  $r=[regex]::Replace($r,'(?i)CURRENT_TIMESTAMP\(\d+\)','SYSTIMESTAMP')
  $r=[regex]::Replace($r,'(?i)\bNOW\(\d*\)','SYSTIMESTAMP')
  $r=[regex]::Replace($r,'(?i)\bIFNULL\s*\(','COALESCE(')
  $r=[regex]::Replace($r,'(?i)\bDATE_SUB\(([^,]+),\s*INTERVAL\s+(\d+)\s+(MINUTE|HOUR|DAY)\)',{param($m)'('+$m.Groups[1].Value+' - INTERVAL '''+$m.Groups[2].Value+''' '+$m.Groups[3].Value.ToUpperInvariant()+')'})
  $r=[regex]::Replace($r,'(?i)\bDATE_ADD\(([^,]+),\s*INTERVAL\s+(\d+)\s+(MINUTE|HOUR|DAY)\)',{param($m)'('+$m.Groups[1].Value+' + INTERVAL '''+$m.Groups[2].Value+''' '+$m.Groups[3].Value.ToUpperInvariant()+')'})
  $r=[regex]::Replace($r,'(?i)\bCONCAT\(([^,()]+),\s*([^()]+)\)','($1 || $2)')
  $r=[regex]::Replace($r,'(?i)\bAS\s+DATETIME\s*\)','AS TIMESTAMP)')
  $r=[regex]::Replace($r,'(?i)\bDATE\(([^()]+)\)','TRUNC($1)')
  $r=[regex]::Replace($r,'(?im)\bLIMIT\s+(\d+)\s*(?=\)|$)','FETCH FIRST $1 ROWS ONLY')
 }
 return $r
}
function Split-Top([string]$s,[char]$sep=','){$a=@();$start=0;$depth=0;$q=[char]0;for($i=0;$i -lt $s.Length;$i++){$c=$s[$i];if($q -ne [char]0){if($c -eq $q){if($i+1 -lt $s.Length -and $s[$i+1] -eq $q){$i++}else{$q=[char]0}};continue};if($c -eq "'" -or $c -eq '"'){$q=$c;continue};if($c -eq '('){$depth++}elseif($c -eq ')'){$depth--}elseif($c -eq $sep -and $depth -eq 0){$a+=$s.Substring($start,$i-$start).Trim();$start=$i+1}};$a+=$s.Substring($start).Trim();return,$a}
function Rows([string]$s){$r=@();$start=-1;$depth=0;$q=[char]0;for($i=0;$i -lt $s.Length;$i++){$c=$s[$i];if($q -ne [char]0){if($c -eq $q){if($i+1 -lt $s.Length -and $s[$i+1] -eq $q){$i++}else{$q=[char]0}};continue};if($c -eq "'" -or $c -eq '"'){$q=$c;continue};if($c -eq '('){if($depth -eq 0){$start=$i+1};$depth++}elseif($c -eq ')'){$depth--;if($depth -eq 0 -and $start -ge 0){$r+=$s.Substring($start,$i-$start);$start=-1}}};return,$r}
function Alias-Select([string]$v,[string]$src,[object[]]$cols){$x=Convert-Expr $v $src;$m=[regex]::Match($x,'(?is)^\s*SELECT\s+(.*?)\s+FROM\s+(.*)$');if(-not $m.Success){return $x};$parts=Split-Top $m.Groups[1].Value;if($parts.Count -ne $cols.Count){return $x};$sel=@();for($i=0;$i -lt $parts.Count;$i++){$sel+=$parts[$i]+' '+$cols[$i]};return 'SELECT '+($sel-join ', ')+' FROM '+$m.Groups[2].Value}
function Render-Insert([string]$v,$st){$cols=@($st.columns);$table=$st.tableName;$src=Convert-Expr $v ([string]$st.source);$keys=@($st.conflictColumns);$ups=@($st.updates)
 if($v -eq 'mariadb'){$base='INSERT INTO '+$table+' ('+($cols-join ', ')+') '+($(if($st.sourceKind -eq 'values'){'VALUES '+$src}else{$src}));if($keys.Count -gt 0 -and $ups.Count -gt 0){$set=@();foreach($u in $ups){$set+=$u.column+' = '+(Convert-Expr $v ([string]$u.expression))};$base+=' ON DUPLICATE KEY UPDATE '+($set-join ', ')};return $base+';'}
 if($v -eq 'postgresql'){$base='INSERT INTO '+$table+' ('+($cols-join ', ')+') '+($(if($st.sourceKind -eq 'values'){'VALUES '+$src}else{$src}));if($keys.Count -gt 0){$base+=' ON CONFLICT ('+($keys-join ', ')+') ';if($ups.Count -eq 0){$base+='DO NOTHING'}else{$set=@();foreach($u in $ups){$e=[regex]::Replace((Convert-Expr $v $u.expression),'(?i)VALUES\(([A-Za-z0-9_]+)\)','EXCLUDED.$1');$set+=$u.column+' = '+$e};$base+='DO UPDATE SET '+($set-join ', ')}};return $base+';'}
 if($keys.Count -eq 0){if($st.sourceKind -eq 'select'){return 'INSERT INTO '+$table+' ('+($cols-join ', ')+') '+(Convert-Expr $v $src)+';'};$rs=Rows $src;$b='INSERT ALL'+"`n";foreach($row in $rs){$b+='  INTO '+$table+' ('+($cols-join ', ')+') VALUES ('+(Convert-Expr $v $row)+")`n"};return $b+'SELECT 1 FROM dual;'}
 if($st.sourceKind -eq 'values'){$qs=@();foreach($row in (Rows $src)){$vals=Split-Top $row;$pairs=@();for($i=0;$i -lt $cols.Count;$i++){$pairs+=(Convert-Expr $v $vals[$i])+' '+$cols[$i]};$qs+='SELECT '+($pairs-join ', ')+' FROM dual'};$using=($qs-join "`nUNION ALL`n")}else{$using=Alias-Select $v $src $cols}
 $on=($keys|ForEach-Object{"tgt.$_ = src.$_"}) -join ' AND ';$b="MERGE INTO $table tgt USING (`n$using`n) src ON ($on)`n";if($ups.Count -gt 0){$set=@();foreach($u in $ups){$e=[regex]::Replace((Convert-Expr $v $u.expression),'(?i)VALUES\(([A-Za-z0-9_]+)\)','src.$1');$set+='tgt.'+$u.column+' = '+$e};$b+='WHEN MATCHED THEN UPDATE SET '+($set-join ', ')+"`n"};$b+='WHEN NOT MATCHED THEN INSERT ('+($cols-join ', ')+') VALUES ('+(($cols|ForEach-Object{"src.$_"}) -join ', ')+');';return $b}
foreach($v in $vendors){$files=@{};foreach($f in $seed.canonicalPolicy.sourceFiles){$files[$f]="-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json`n-- vendor=$v; source=$f`n-- DO NOT EDIT generated seed directly.`n"};$current=@{};foreach($st in $seed.statements){$f=$st.sourceFile;if(-not $files.ContainsKey($f)){continue};$db=$st.logicalDatabase;if(-not $current.ContainsKey($f) -or $current[$f] -ne $db){$files[$f]+="`n-- CPF_LOGICAL_DATABASE=$db`n";$current[$f]=$db};switch($st.statementKind){'use'{}'insert'{$files[$f]+=(Render-Insert $v $st)+"`n"}'update'{$files[$f]+=(Convert-Expr $v $st.sql)+";`n"}'delete'{$files[$f]+=(Convert-Expr $v $st.sql)+";`n"}'set'{$expr=Convert-Expr $v $st.expression;if($v -eq 'mariadb'){$files[$f]+="SET @$($st.variable) = $expr;`n"}elseif($v -eq 'postgresql'){if($expr.Trim().StartsWith('(')){$files[$f]+="SELECT $expr AS $($st.variable) \\gset`n"}else{$files[$f]+="\\set $($st.variable) $expr`n"}}else{if($expr.Trim().StartsWith('(')){$files[$f]+="COLUMN $($st.variable) NEW_VALUE $($st.variable) NOPRINT`nSELECT $expr AS $($st.variable) FROM dual;`n"}else{$files[$f]+="DEFINE $($st.variable) = $expr`n"}}}}};foreach($f in $files.Keys){$txt=$files[$f];if($v -eq 'postgresql'){$txt=[regex]::Replace($txt,'@([A-Za-z_][A-Za-z0-9_]*)',':$1')}elseif($v -eq 'oracle'){$txt=[regex]::Replace($txt,'@([A-Za-z_][A-Za-z0-9_]*)','&&$1')};W (Join-Path $Root "cpf-tools/db/vendor/$v/source/$f") $txt}}

# Table rendering does not own sequences and other non-table objects. Restore
# those artifacts from their dedicated canonical contract after PostgreSQL and
# Oracle source files have been regenerated.
$nonTableSync = Join-Path $PSScriptRoot "sync-platform-non-table-objects.ps1"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $nonTableSync -Root $Root
if ($LASTEXITCODE -ne 0) {
    throw "Canonical non-table DB object synchronization failed. exitCode=$LASTEXITCODE"
}
Write-Host 'Canonical schema/seed -> MariaDB/PostgreSQL/Oracle vendor source generation complete.'
