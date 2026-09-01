param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$schemaPath=Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$seedPath=Join-Path $Root 'cpf-tools/db/canonical/seed-model.json'
$profilePath=Join-Path $Root 'cpf-tools/db/config/database-install.default.json'
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
# A logical database is one physical lifecycle/verification target even when
# several enabled application modules deliberately share it.  Pick the one
# explicit owner for every logical DB here, rather than rendering identical
# provision/grant/verify SQL once for each sharing module.  In particular,
# generating the same check_name twice is a real Verify Pack contract breach:
# the runtime installer correctly rejects it rather than silently counting it.
$platformDatabaseOwners=@(
 $platformModules |
 Group-Object { [string]$_.logicalDatabase } |
  ForEach-Object {
   $logicalDatabase=[string]$_.Name
   $owners=@($_.Group | Where-Object {
    $shareProperty=$_.PSObject.Properties['sharesDatabaseWith']
    $shareDatabaseWith=if($null -eq $shareProperty){''}else{[string]$shareProperty.Value}
    [string]::IsNullOrWhiteSpace($shareDatabaseWith)
   })
   if($owners.Count -ne 1){
    throw "Enabled profile logicalDatabase must have exactly one physical owner: logicalDatabase=$logicalDatabase owners=$($owners.Count)"
   }
   $owners[0]
  }
)
$profileLogicalDatabases=@($platformModules | ForEach-Object { [string]$_.logicalDatabase } | Sort-Object -Unique)
if(($logicalDatabases -join "`n") -cne ($profileLogicalDatabases -join "`n")){
 throw "Canonical schema/profile logical database drift. schema=$($logicalDatabases -join ',') profile=$($profileLogicalDatabases -join ',')"
}
$fileByDb=@{cpfDB='10_cpf_schema.sql';mbwDB='40_business_modules_schema.sql'}
$productionLogicalDatabases=@($schema.tables | Where-Object { [bool]$_.productionDefault } | ForEach-Object { [string]$_.logicalDatabase } | Sort-Object -Unique)
$retiredSplitSchemaFiles=@('20_cmn_schema.sql','30_adm_schema.sql')
foreach($vendor in $vendors){
 $vendorSourceRoot=[IO.Path]::GetFullPath((Join-Path $Root "cpf-tools/db/vendor/$vendor/source"))
 foreach($retiredFile in $retiredSplitSchemaFiles){
  $retiredPath=[IO.Path]::GetFullPath((Join-Path $vendorSourceRoot $retiredFile))
  if(-not $retiredPath.StartsWith($vendorSourceRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){
   throw "Retired vendor Source cleanup escaped its owner root: $retiredPath"
  }
  if(Test-Path -LiteralPath $retiredPath -PathType Leaf){Remove-Item -LiteralPath $retiredPath -Force}
 }
}
function W([string]$p,[string]$s){New-Item -ItemType Directory -Force -Path (Split-Path -Parent $p)|Out-Null;[IO.File]::WriteAllText($p,$s.TrimEnd()+"`n",[Text.UTF8Encoding]::new($false))}
function Assert-DbIdentifier([string]$value,[string]$name){
 if($value -cnotmatch '^[A-Za-z][A-Za-z0-9_$#]{1,62}$'){throw "Invalid $name in canonical DB profile: $value"}
}
function Sql-Quote([string]$value){return $value.Replace("'","''")}
function Get-CanonicalTableOrder([object[]]$tables,[string]$logicalDatabase){
 $byName=@{}
 $dependencies=@{}
 foreach($table in $tables){
  $name=[string]$table.name
  if([string]::IsNullOrWhiteSpace($name)){throw "Canonical table without name in logicalDatabase=$logicalDatabase"}
  if($byName.ContainsKey($name)){throw "Duplicate canonical table name: $name"}
  $byName[$name]=$table
  $dependencies[$name]=[Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
 }
 foreach($table in $tables){
  $child=[string]$table.name
  foreach($foreignKey in @($table.foreignKeys)){
   $parent=[string]$foreignKey.refTable
   if([string]::IsNullOrWhiteSpace($parent)){throw "Foreign key without refTable: table=$child constraint=$($foreignKey.name)"}
   if($parent -eq $child){continue}
   if(-not $byName.ContainsKey($parent)){
    throw "Foreign key parent is missing or belongs to another logical database: child=$child parent=$parent logicalDatabase=$logicalDatabase"
   }
   [void]$dependencies[$child].Add($parent)
  }
 }
 $remaining=@{}
 foreach($name in $byName.Keys){$remaining[$name]=$byName[$name]}
 $ordered=[Collections.Generic.List[object]]::new()
 while($remaining.Count -gt 0){
  $ready=[Collections.Generic.List[object]]::new()
  foreach($name in @($remaining.Keys)){
   $blocked=$false
   foreach($parent in $dependencies[$name]){
    if($remaining.ContainsKey($parent)){$blocked=$true;break}
   }
   if(-not $blocked){$ready.Add($remaining[$name])}
  }
  if($ready.Count -eq 0){
   $cycle=@($remaining.Keys | Sort-Object) -join ','
   throw "Canonical foreign key cycle detected: logicalDatabase=$logicalDatabase tables=$cycle"
  }
  foreach($table in @($ready | Sort-Object name)){
   $ordered.Add($table)
   $remaining.Remove([string]$table.name)
  }
 }
 return @($ordered)
}
function Assert-CanonicalSchemaContract($canonicalSchema){
 $globalTableByName=@{}
 foreach($table in @($canonicalSchema.tables)){
  $tableName=[string]$table.name
  if($globalTableByName.ContainsKey($tableName)){throw "Duplicate canonical table name across logical databases: $tableName"}
  $globalTableByName[$tableName]=$table
  $columnByName=@{}
  foreach($column in @($table.columns)){
   $columnName=[string]$column.name
   if($columnByName.ContainsKey($columnName)){throw "Duplicate canonical column: $tableName.$columnName"}
   $columnByName[$columnName]=$column
   if([string]$column.default -eq "''"){
    throw "Empty-string DDL defaults are not portable to Oracle. Use nullable=true/default=null: $tableName.$columnName"
   }
   if([bool]$column.autoIncrement -and ([string]$column.type -notmatch '^(?i:BIGINT|INT|TINYINT)$')){
    throw "autoIncrement requires an integer canonical type: $tableName.$columnName type=$($column.type)"
   }
  }
  foreach($primaryKeyColumn in @($table.primaryKey)){
   if(-not $columnByName.ContainsKey([string]$primaryKeyColumn)){
    throw "Primary key references missing column: table=$tableName column=$primaryKeyColumn"
   }
  }
 }
 foreach($table in @($canonicalSchema.tables)){
  $child=[string]$table.name
  foreach($foreignKey in @($table.foreignKeys)){
   $parent=[string]$foreignKey.refTable
   if(-not $globalTableByName.ContainsKey($parent)){throw "Foreign key references missing table: child=$child parent=$parent"}
   if([string]$globalTableByName[$parent].logicalDatabase -ne [string]$table.logicalDatabase){
    throw "Cross logical-database foreign key is forbidden: child=$child parent=$parent"
   }
  }
 }
 foreach($logicalDatabase in @($canonicalSchema.tables | ForEach-Object { [string]$_.logicalDatabase } | Sort-Object -Unique)){
  [void](Get-CanonicalTableOrder @($canonicalSchema.tables | Where-Object { [string]$_.logicalDatabase -eq $logicalDatabase }) $logicalDatabase)
 }
}
Assert-CanonicalSchemaContract $schema

foreach($module in $platformModules){
 Assert-DbIdentifier ([string]$module.logicalDatabase) 'logicalDatabase'
 Assert-DbIdentifier ([string]$module.migration.username) 'migration username'
 Assert-DbIdentifier ([string]$module.runtime.username) 'runtime username'
}

# Provision contracts are projections of the platform install profile. Generated
# domains are provisioned from their manifest/template and never enter this list.
$mariaDatabaseSource=@(
 '-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json'
 '-- vendor=mariadb; platform provision databases'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
foreach($module in $platformDatabaseOwners){
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
 '-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json'
 '-- vendor=mariadb; platform service users and least-privilege grants'
 '-- The caller must set @cpf_migration_password and @cpf_app_password in-memory.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
$createdMigrationAccounts=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$createdRuntimeAccounts=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach($module in $platformDatabaseOwners){
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
foreach($module in $platformDatabaseOwners){
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
 '-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json'
 '-- vendor=postgresql; physical database/roles are created by the profile-aware executor.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
$oracleProvision=@(
 '-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json'
 '-- vendor=oracle; password tokens are rendered only in-memory by the executor.'
 '-- DO NOT EDIT generated provision SQL directly.'
 ''
)
foreach($module in $platformDatabaseOwners){
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

# Current product-seed verification is projected from the canonical seed model.
# Arbitrary row-count minimums can both reject a correct Fresh install and pass
# when a required business key is absent but an unrelated row happens to exist.
function Split-CanonicalSeedProjection([string]$source,[char]$separator=','){
 $parts=@();$start=0;$depth=0;$quote=[char]0
 for($i=0;$i -lt $source.Length;$i++){
  $character=$source[$i]
  if($quote -ne [char]0){
   if($character -eq $quote){if($i+1 -lt $source.Length -and $source[$i+1] -eq $quote){$i++}else{$quote=[char]0}}
   continue
  }
  if($character -eq "'" -or $character -eq '"'){$quote=$character;continue}
  if($character -eq '('){$depth++}
  elseif($character -eq ')'){$depth--}
  elseif($character -eq $separator -and $depth -eq 0){$parts+=$source.Substring($start,$i-$start).Trim();$start=$i+1}
 }
 $parts+=$source.Substring($start).Trim()
 return $parts
}
function Get-CanonicalSeedValueRows([object]$statement){
 $source=[string]$statement.source
 if([string]$statement.sourceKind -eq 'select'){
  $match=[regex]::Match($source,'(?is)^\s*SELECT\s+(?<projection>.*?)\s+WHERE\s+NOT\s+EXISTS\s*\(')
  if(-not $match.Success){throw "Unsupported canonical product-seed SELECT projection: table=$($statement.currentTableName)"}
  return @($match.Groups['projection'].Value.Trim())
 }
 if([string]$statement.sourceKind -cne 'values'){
  throw "Unsupported canonical product-seed source kind: table=$($statement.currentTableName) kind=$($statement.sourceKind)"
 }
 $rows=@();$start=-1;$depth=0;$quote=[char]0
 for($i=0;$i -lt $source.Length;$i++){
  $character=$source[$i]
  if($quote -ne [char]0){
   if($character -eq $quote){if($i+1 -lt $source.Length -and $source[$i+1] -eq $quote){$i++}else{$quote=[char]0}}
   continue
  }
  if($character -eq "'" -or $character -eq '"'){$quote=$character;continue}
  if($character -eq '('){if($depth -eq 0){$start=$i+1};$depth++}
  elseif($character -eq ')'){$depth--;if($depth -eq 0 -and $start -ge 0){$rows+=$source.Substring($start,$i-$start);$start=-1}}
 }
 if($depth -ne 0 -or $quote -ne [char]0){throw "Unbalanced canonical product-seed VALUES projection: table=$($statement.currentTableName)"}
 return $rows
}
function Get-CanonicalProductSeedKeyContract([string]$tableName,[string[]]$keyColumns){
 $seen=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
 $predicates=[Collections.Generic.List[string]]::new()
 $statements=@($seed.statements | Where-Object {
  [bool]$_.productionDefault -and [string]$_.statementKind -ceq 'insert' -and [string]$_.currentTableName -ceq $tableName
 })
 if($statements.Count -eq 0){throw "Canonical production seed has no insert contract for $tableName"}
 foreach($statement in $statements){
  $columns=@($statement.columns)
  foreach($row in @(Get-CanonicalSeedValueRows $statement)){
   $values=@(Split-CanonicalSeedProjection $row)
   if($values.Count -ne $columns.Count){throw "Canonical product-seed projection width mismatch: table=$tableName columns=$($columns.Count) values=$($values.Count)"}
   $keyValues=@();$clauses=@()
   foreach($keyColumn in $keyColumns){
    if($keyColumn -cnotmatch '^[A-Za-z][A-Za-z0-9_]*$'){throw "Unsafe canonical product-seed key identifier: $tableName.$keyColumn"}
    $columnIndex=[array]::IndexOf($columns,$keyColumn)
    if($columnIndex -lt 0){throw "Canonical product-seed key is absent from projection: $tableName.$keyColumn"}
    $keyValue=[string]$values[$columnIndex]
    if($keyValue -cnotmatch "^'(?:''|[^'])*'$" -and $keyValue -cnotmatch '^-?[0-9]+$'){
     throw "Canonical product-seed key must be a deterministic SQL literal: $tableName.$keyColumn=$keyValue"
    }
    $keyValues+=$keyValue
    $clauses+="$keyColumn = $keyValue"
   }
   $signature=$keyValues -join ([char]0x1f)
   if($seen.Add($signature)){$predicates.Add('('+($clauses -join ' AND ')+')')}
  }
 }
 if($predicates.Count -eq 0){throw "Canonical product-seed business-key contract is empty: $tableName"}
 # SQLPlus rejects any physical input line over 4,999 characters. Keep each
 # deterministic business-key predicate on its own line for every Vendor so
 # the Canonical Verify projection remains portable as seed inventories grow.
 return [pscustomobject]@{table=$tableName;count=$predicates.Count;predicate=($predicates -join " OR`n               ")}
}
$productSeedKeyColumns=[ordered]@{
 CMN_CODE=@('code_key','code_value')
 CMN_MESSAGE=@('message_code','locale')
 CMN_RESPONSE_CODE=@('response_code')
 CMN_PARAMETER=@('config_key')
}
$productSeedContracts=@{}
foreach($tableName in $productSeedKeyColumns.Keys){
 $productSeedContracts[$tableName]=Get-CanonicalProductSeedKeyContract $tableName @($productSeedKeyColumns[$tableName])
}
$mariaVerify=@(
 '-- AUTO-GENERATED from CPF canonical schema/profile contracts'
 '-- vendor=mariadb; each logical section executes in its profile-selected physical database.'
 '-- DO NOT EDIT generated verify SQL directly.'
 ''
)
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
foreach($module in $platformDatabaseOwners){
 $db=[string]$module.logicalDatabase
 $systemCode=([string]$module.systemCode).ToUpperInvariant()
 $expected=[int]$tableCounts[$db]
 $mariaVerify+=@(
  "-- CPF_LOGICAL_DATABASE=$db"
  "SELECT '$db.table_count' AS check_name,"
  "       IF(COUNT(*) = $expected, 1, 0) AS passed"
  'FROM information_schema.tables'
  "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';"
  ''
  "SELECT '$db.table_engine_collation' AS check_name,"
  '       IF(COUNT(*) = 0, 1, 0) AS passed'
  'FROM information_schema.tables'
  "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'"
  "  AND (UPPER(COALESCE(engine, '')) <> 'INNODB'"
  "       OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci');"
  ''
 )
 $transactionColumns=@()
 foreach($table in @($schema.tables|Where-Object{[string]$_.logicalDatabase -eq $db})){
  foreach($column in @($table.columns|Where-Object{[string]$_.name -ieq 'transaction_id'})){
   $type=[string]$column.type
   if($type -cnotmatch '^(CHAR|VARCHAR)\((\d+)\)$'){throw "Unsupported canonical transaction_id type: $($table.name).$type"}
   $transactionColumns+= [pscustomobject]@{table=([string]$table.name).ToUpperInvariant();dataType=$Matches[1].ToLowerInvariant();length=[int]$Matches[2]}
  }
 }
 $mariaVerify+=@(
  "SELECT '$db.runtime_transaction_id_contract' AS check_name,"
  "       IF(COUNT(*) = $($transactionColumns.Count) AND COALESCE(SUM(CASE"
 )
 foreach($transactionColumn in $transactionColumns){
  $mariaVerify+="           WHEN UPPER(table_name) = '$($transactionColumn.table)' AND LOWER(data_type) = '$($transactionColumn.dataType)' AND character_maximum_length = $($transactionColumn.length) THEN 1"
 }
 $mariaVerify+=@(
  "           ELSE 0 END), 0) = $($transactionColumns.Count), 1, 0) AS passed"
  'FROM information_schema.columns'
  "WHERE table_schema = DATABASE() AND LOWER(column_name) = 'transaction_id';"
  ''
 )
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
 $mariaVerify+=@(
   "SELECT 'cpfDB.product_seed' AS check_name,"
   '       IF('
   "           (SELECT COUNT(*) FROM CMN_CODE WHERE $($productSeedContracts['CMN_CODE'].predicate)) = $($productSeedContracts['CMN_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_MESSAGE WHERE $($productSeedContracts['CMN_MESSAGE'].predicate)) = $($productSeedContracts['CMN_MESSAGE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_PARAMETER WHERE $($productSeedContracts['CMN_PARAMETER'].predicate)) = $($productSeedContracts['CMN_PARAMETER'].count),"
   '           1, 0' 
   '       ) AS passed;'
   ''
   "SELECT 'cpfDB.response_code_http_status' AS check_name,"
   "       IF((SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   '          AND NOT EXISTS (SELECT 1 FROM CMN_RESPONSE_CODE WHERE http_status NOT BETWEEN 100 AND 599), 1, 0) AS passed;'
   ''
   "SELECT 'cpfDB.admin_product_seed' AS check_name,"
   '       IF('
   "           (SELECT COUNT(*) FROM ADM_ROLE WHERE USE_YN = 'Y') >= 5"
   "           AND (SELECT COUNT(*) FROM ADM_MENU WHERE USE_YN = 'Y') >= 30"
   "           AND (SELECT COUNT(*) FROM ADM_API_PERMISSION WHERE USE_YN = 'Y') >= 10,"
   '           1, 0'
   '       ) AS passed;'
   ''
   "SELECT 'cpfDB.removed_stale_tables_absent' AS check_name,"
   '       IF(COUNT(*) = 0, 1, 0) AS passed'
   'FROM information_schema.tables'
   "WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('cpf_file_exchange_log','adm_operation_log');"
   ''
   "SELECT 'cpfDB.adm_operator_account_safety_columns' AS check_name,"
   '       IF(COUNT(*) = 3, 1, 0) AS passed'
   'FROM information_schema.columns'
   "WHERE table_schema = DATABASE() AND UPPER(table_name) = 'ADM_OPERATOR'"
   "  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');"
   ''
   "SELECT 'cpfDB.adm_contact_ownership' AS check_name,"
   '       IF('
   "         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 0"
   '         AND'
   "         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR_PROFILE' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 2,"
   '         1, 0'
   '       ) AS passed;'
   ''
   "SELECT 'cpfDB.adm_operator_status_constraint' AS check_name,"
   '       IF(COUNT(*) = 1, 1, 0) AS passed'
   'FROM information_schema.table_constraints'
   "WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND constraint_name='ck_adm_operator_status';"
   ''
  )
  $postgresVerify+=@(
   "SELECT 'cpfDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM CMN_CODE WHERE $($productSeedContracts['CMN_CODE'].predicate)) = $($productSeedContracts['CMN_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_MESSAGE WHERE $($productSeedContracts['CMN_MESSAGE'].predicate)) = $($productSeedContracts['CMN_MESSAGE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_PARAMETER WHERE $($productSeedContracts['CMN_PARAMETER'].predicate)) = $($productSeedContracts['CMN_PARAMETER'].count)"
   '       THEN 1 ELSE 0 END AS passed;'
   ''
   "SELECT 'cpfDB.response_code_http_status' AS check_name,"
   "       CASE WHEN (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   '                 AND NOT EXISTS (SELECT 1 FROM CMN_RESPONSE_CODE WHERE http_status NOT BETWEEN 100 AND 599)'
   '       THEN 1 ELSE 0 END AS passed;'
   ''
  )
  $oracleVerify+=@(
   "SELECT 'cpfDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM CMN_CODE WHERE $($productSeedContracts['CMN_CODE'].predicate)) = $($productSeedContracts['CMN_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_MESSAGE WHERE $($productSeedContracts['CMN_MESSAGE'].predicate)) = $($productSeedContracts['CMN_MESSAGE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   "           AND (SELECT COUNT(*) FROM CMN_PARAMETER WHERE $($productSeedContracts['CMN_PARAMETER'].predicate)) = $($productSeedContracts['CMN_PARAMETER'].count)"
   '       THEN 1 ELSE 0 END AS passed FROM dual;'
   ''
   "SELECT 'cpfDB.response_code_http_status' AS check_name,"
   "       CASE WHEN (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE $($productSeedContracts['CMN_RESPONSE_CODE'].predicate)) = $($productSeedContracts['CMN_RESPONSE_CODE'].count)"
   '                 AND NOT EXISTS (SELECT 1 FROM CMN_RESPONSE_CODE WHERE http_status NOT BETWEEN 100 AND 599)'
   '       THEN 1 ELSE 0 END AS passed FROM dual;'
   ''
  )
 }
 if($systemCode -eq 'MBW'){
  $mariaVerify+=@(
   "SELECT 'mbwDB.product_seed' AS check_name,"
   '       IF('
   "           (SELECT COUNT(*) FROM MBW_ROLE WHERE use_yn = 'Y') >= 4"
   "           AND (SELECT COUNT(*) FROM MBW_MENU WHERE use_yn = 'Y') >= 8"
   "           AND (SELECT COUNT(*) FROM MBW_PERMISSION WHERE role_code = 'MBW_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8,"
   '           1, 0'
   '       ) AS passed;'
   ''
   "SELECT 'mbwDB.removed_stale_tables_absent' AS check_name,"
   '       IF(COUNT(*) = 0, 1, 0) AS passed'
   'FROM information_schema.tables'
   "WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('mbw_customer','mbw_product','mbw_order','mbw_masking_audit');"
   ''
   "SELECT 'mbwDB.admin_user_account_safety_columns' AS check_name,"
   '       IF(COUNT(*) = 3, 1, 0) AS passed'
   'FROM information_schema.columns'
   "WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_ADMIN_USER'"
   "  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');"
   ''
   "SELECT 'mbwDB.employee_status_default' AS check_name,"
   "       IF(MAX(UPPER(REPLACE(COALESCE(column_default,''), CHAR(39), ''))) = 'EMPLOYED', 1, 0) AS passed"
   'FROM information_schema.columns'
   "WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_EMPLOYEE' AND LOWER(column_name)='employment_status';"
   ''
   "SELECT 'mbwDB.status_constraints' AS check_name,"
   '       IF(COUNT(*) = 2, 1, 0) AS passed'
   'FROM information_schema.table_constraints'
   "WHERE table_schema=DATABASE() AND ((UPPER(table_name)='MBW_ADMIN_USER' AND constraint_name='ck_mbw_admin_user_status')"
   "   OR (UPPER(table_name)='MBW_EMPLOYEE' AND constraint_name='ck_mbw_employee_status'));"
   ''
   "SELECT 'mbwDB.login_operation_contract' AS check_name,"
   '       IF('
   "         (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_LOGIN_OPERATION' AND table_type='BASE TABLE') = 1"
   '         AND'
   "         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_REFRESH_TOKEN' AND LOWER(column_name)='login_operation_id') = 1,"
   '         1, 0'
   '       ) AS passed;'
   ''
  )
  $postgresVerify+=@(
   "SELECT 'mbwDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM MBW_ROLE WHERE use_yn = 'Y') >= 4"
   "           AND (SELECT COUNT(*) FROM MBW_MENU WHERE use_yn = 'Y') >= 8"
   "           AND (SELECT COUNT(*) FROM mbw_permission WHERE role_code = 'MBW_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8"
   '       THEN 1 ELSE 0 END AS passed;'
   ''
  )
  $oracleVerify+=@(
   "SELECT 'mbwDB.product_seed' AS check_name,"
   '       CASE WHEN'
   "           (SELECT COUNT(*) FROM MBW_ROLE WHERE use_yn = 'Y') >= 4"
   "           AND (SELECT COUNT(*) FROM MBW_MENU WHERE use_yn = 'Y') >= 8"
   "           AND (SELECT COUNT(*) FROM mbw_permission WHERE role_code = 'MBW_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8"
   '       THEN 1 ELSE 0 END AS passed FROM dual;'
   ''
  )
 }
}
W (Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql') ($mariaVerify -join "`n")
W (Join-Path $Root 'cpf-tools/db/vendor/postgresql/source/00_verify.sql') ($postgresVerify -join "`n")
W (Join-Path $Root 'cpf-tools/db/vendor/oracle/source/00_verify.sql') ($oracleVerify -join "`n")
function Type-For([string]$v,[string]$t){
 $u=$t.ToUpperInvariant()
 if($v -eq 'mariadb'){$u=$u -replace '^CLOB$','MEDIUMTEXT' -replace '^BYTEA$','LONGBLOB';return $u}
 if($v -eq 'postgresql'){
  if($u -match '^VARBINARY\(\d+\)$'){return 'BYTEA'}
  $u=$u -replace '^DATETIME','TIMESTAMP' -replace '^TINYINT','SMALLINT' -replace '^INT$','INTEGER' -replace '^LONGTEXT$','TEXT' -replace '^MEDIUMTEXT$','TEXT' -replace '^LONGBLOB$','BYTEA' -replace '^BLOB$','BYTEA' -replace '^CLOB$','TEXT'
  return $u
 }
 if($u -match '^VARBINARY\((\d+)\)$'){return "RAW($($Matches[1]))"}
 if($u -match '^VARCHAR\((\d+)\)$'){return "VARCHAR2($($Matches[1]) CHAR)"}
 if($u -match '^CHAR\((\d+)\)$'){return "CHAR($($Matches[1]) CHAR)"}
 if($u -match '^BIGINT$'){return 'NUMBER(19)'}; if($u -match '^INT$'){return 'NUMBER(10)'}; if($u -match '^TINYINT$'){return 'NUMBER(3)'}
 if($u -eq 'TIME'){return 'VARCHAR2(15 CHAR)'}
 if($u -match '^DATETIME(\(\d+\))?$'){return 'TIMESTAMP'+$Matches[1]}; if($u -eq 'LONGBLOB' -or $u -eq 'BLOB'){return 'BLOB'}; if($u -eq 'MEDIUMTEXT' -or $u -eq 'LONGTEXT' -or $u -eq 'TEXT'){return 'CLOB'}
 return $u
}
function Default-For([string]$v,$d){ if($null -eq $d){return $null};$x=[string]$d;if($v -eq 'postgresql'){$x=[regex]::Replace($x,'(?i)CURRENT_TIMESTAMP\(\d+\)','CURRENT_TIMESTAMP')};return $x }
function Check-For([string]$v,[string]$expression){
 $x=$expression
 $regexpPattern='(?i)(?<operand>\b[A-Za-z_][A-Za-z0-9_]*\b)\s+REGEXP\s+(?<pattern>''[^'']*'')'
 if($v -eq 'postgresql'){
  $x=[regex]::Replace($x,$regexpPattern,'${operand} ~ ${pattern}')
 }elseif($v -eq 'oracle'){
  $x=[regex]::Replace($x,$regexpPattern,'REGEXP_LIKE(${operand}, ${pattern})')
  $x=[regex]::Replace($x,'(?i)RIGHT\(\s*(?<operand>[A-Za-z_][A-Za-z0-9_]*)\s*,\s*(?<count>\d+)\s*\)','SUBSTR(${operand}, -${count})')
 }
 return $x
}
function Q([string]$s){return $s.Replace("'","''")}
function Render-Table([string]$v,$t){
 $lines=@()
 foreach($c in $t.columns){
  $type=Type-For $v ([string]$c.type)
  $line='    '+$c.name+' '+$type
  if([bool]$c.autoIncrement){
   $line+=if($v -eq 'postgresql'){' GENERATED BY DEFAULT AS IDENTITY'}else{' GENERATED BY DEFAULT ON NULL AS IDENTITY'}
  }
  $d=Default-For $v $c.default
  $hasDefault=($null-ne$d-and$d-ne'')
  if($v -eq 'oracle'){
   # Oracle column grammar requires DEFAULT before the inline NOT NULL
   # constraint. PostgreSQL accepts the existing NOT NULL DEFAULT order.
   if($hasDefault){$line+=' DEFAULT '+$d}
   if(-not [bool]$c.nullable){$line+=' NOT NULL'}
  }else{
   if(-not [bool]$c.nullable){$line+=' NOT NULL'}
   if($hasDefault){$line+=' DEFAULT '+$d}
  }
  $lines+=$line
 }
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
 foreach($c in $t.checks){
  $checkExpression=[string]$c.expression
  if($null -ne $c.PSObject.Properties['vendorExpressions']){
   $vendorExpression=$c.vendorExpressions.PSObject.Properties[$v]
   if($null -ne $vendorExpression){$checkExpression=[string]$vendorExpression.Value}
  }
  $lines+='    CONSTRAINT '+$c.name+' CHECK ('+(Check-For $v $checkExpression)+')'
 }
 foreach($f in $t.foreignKeys){
  $line='    CONSTRAINT '+$f.name+' FOREIGN KEY ('+($f.columns -join ', ')+') REFERENCES '+$f.refTable+' ('+($f.refColumns -join ', ')+')'
  $onDelete=([string]$f.onDelete).Trim().ToUpperInvariant()
  # Oracle's default is RESTRICT/NO ACTION and its grammar accepts neither
  # keyword explicitly. CASCADE and SET NULL remain explicit and unchanged.
  if($onDelete-and-not($v-eq'oracle'-and$onDelete-in@('RESTRICT','NO ACTION'))){$line+=' ON DELETE '+$onDelete}
  $lines+=$line
 }
 if($v -eq 'mariadb'){foreach($i in $t.indexes){$isUnique=$null -ne $i.PSObject.Properties['unique'] -and [bool]$i.unique;$indexColumns=@($i.columns);if($null -ne $i.PSObject.Properties['vendorColumns'] -and $null -ne $i.vendorColumns.PSObject.Properties[$v]){$indexColumns=@($i.vendorColumns.PSObject.Properties[$v].Value)};$lines+='    '+$(if($isUnique){'UNIQUE '}else{''})+'INDEX '+$i.name+' ('+($indexColumns -join ', ')+')'}}
 $createPrefix=if($v -eq 'mariadb'){'CREATE TABLE IF NOT EXISTS '}else{'CREATE TABLE '}
 $createSuffix=if($v -eq 'mariadb'){"`n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"+$(if($t.comment){" COMMENT='$(Q ([string]$t.comment))'"}else{''})+";`n"}else{"`n);`n"}
 $out=$createPrefix+$t.name+" (`n"+($lines -join ",`n")+$createSuffix
 if($v -ne 'mariadb'){foreach($i in $t.indexes){$isUnique=$null -ne $i.PSObject.Properties['unique'] -and [bool]$i.unique;$indexColumns=@($i.columns);if($null -ne $i.PSObject.Properties['vendorColumns'] -and $null -ne $i.vendorColumns.PSObject.Properties[$v]){$indexColumns=@($i.vendorColumns.PSObject.Properties[$v].Value)};$out+='CREATE '+($(if($isUnique){'UNIQUE '}else{''}))+'INDEX '+$i.name+' ON '+$t.name+' ('+($indexColumns -join ', ')+");`n"}}
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
$sourceSchemaFiles=@($fileByDb.Values | Sort-Object -Unique)
foreach($v in $vendors){
 $bucket=[ordered]@{}; foreach($sourceFile in $sourceSchemaFiles){$bucket[$sourceFile]=@()}
 foreach($db in $productionLogicalDatabases){if(-not $fileByDb.ContainsKey($db)){throw "Production logical database has no vendor source file mapping: $db"};$ts=@(Get-CanonicalTableOrder @($schema.tables|Where-Object{$_.logicalDatabase -eq $db -and [bool]$_.productionDefault}) $db);if($ts.Count -eq 0){continue};$s="-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json`n-- vendor=$v`n-- DO NOT EDIT generated DDL directly.`n`n-- CPF_LOGICAL_DATABASE=$db`n";if($v -eq 'mariadb'){$s+="USE $db;`n"};foreach($t in $ts){$s+=(Render-Table $v $t)+"`n"};$bucket[$fileByDb[$db]]+=$s}
 foreach($sourceFile in $sourceSchemaFiles){if($bucket[$sourceFile].Count -gt 0){W (Join-Path $Root "cpf-tools/db/vendor/$v/source/$sourceFile") ($bucket[$sourceFile] -join "`n")}}
}
# Vendor seed source 의 단일 writer 는 canonical renderer 다. 이 생성기는 schema/provision/
# verify 만 소유하고, seed 는 자체 규칙으로 다시 만들지 않는다. 과거에는 두 도구가 같은 파일을
# 다른 규칙으로 덮어써서 실행 순서에 따라 결과가 달라졌다.
$seedSync = Join-Path $Root "cpf-tools/db/tools/sync-canonical-seed-bundles.py"
$python = if ($env:CPF_PYTHON) { $env:CPF_PYTHON } else { "python" }
& $python -B $seedSync --root $Root
if ($LASTEXITCODE -ne 0) {
    throw "Canonical seed source synchronization failed. exitCode=$LASTEXITCODE"
}

# Table rendering does not own sequences and other non-table objects. Restore
# those artifacts from their dedicated canonical contract after PostgreSQL and
# Oracle source files have been regenerated.
$nonTableSync = Join-Path $Root "cpf-tools/verification/tools/sync-platform-non-table-objects.ps1"
& pwsh -NoProfile -File $nonTableSync -Root $Root
if ($LASTEXITCODE -ne 0) {
    throw "Canonical non-table DB object synchronization failed. exitCode=$LASTEXITCODE"
}
Write-Host 'Canonical schema/seed -> MariaDB/PostgreSQL/Oracle vendor source generation complete.'
