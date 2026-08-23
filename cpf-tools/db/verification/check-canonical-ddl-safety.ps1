param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest

$schemaPath=Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$schema=Get-Content $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50

$errors=[Collections.Generic.List[string]]::new()

function Get-MariaDbIndexColumnBytes {
 param($Table,[string]$Expression)
 $match=[regex]::Match($Expression,'^([A-Za-z][A-Za-z0-9_$#]*)(?:\(([0-9]+)\))?$')
 if(-not $match.Success){throw "unsupported MariaDB index column expression: $Expression"}
 $columnName=$match.Groups[1].Value
 $prefix=if($match.Groups[2].Success){[int]$match.Groups[2].Value}else{$null}
 $column=@($Table.columns|Where-Object{[string]$_.name -ieq $columnName})
 if($column.Count -ne 1){throw "MariaDB index column must resolve exactly once: $($Table.name).$columnName"}
 $type=([string]$column[0].type).ToUpperInvariant()
 $sized=[regex]::Match($type,'^(VAR)?CHAR\(([0-9]+)\)$')
 if($sized.Success){
  $declared=[int]$sized.Groups[2].Value
  $characters=if($null -eq $prefix){$declared}else{$prefix}
  if($characters -lt 1 -or $characters -gt $declared){throw "invalid MariaDB character index prefix: $($Table.name).$Expression type=$type"}
  return $characters*4
 }
 $binary=[regex]::Match($type,'^(VAR)?BINARY\(([0-9]+)\)$')
 if($binary.Success){
  $declared=[int]$binary.Groups[2].Value
  $bytes=if($null -eq $prefix){$declared}else{$prefix}
  if($bytes -lt 1 -or $bytes -gt $declared){throw "invalid MariaDB binary index prefix: $($Table.name).$Expression type=$type"}
  return $bytes
 }
 if($type -match '^(TEXT|BLOB|LONGBLOB)$'){
  if($null -eq $prefix -or $prefix -lt 1){throw "MariaDB large-value index requires an explicit prefix: $($Table.name).$Expression type=$type"}
  return $(if($type -eq 'TEXT'){$prefix*4}else{$prefix})
 }
 if($null -ne $prefix){throw "MariaDB fixed-width index column must not declare a prefix: $($Table.name).$Expression type=$type"}
 switch -Regex ($type) {
  '^BIGINT$' { return 8 }
  '^(INT|INTEGER)$' { return 4 }
  '^SMALLINT$' { return 2 }
  '^TINYINT' { return 1 }
  '^(TIMESTAMP|DATETIME)' { return 8 }
  '^DATE$' { return 3 }
  '^DECIMAL\(' { return 16 }
  '^BOOLEAN$' { return 1 }
  default { throw "unsupported MariaDB indexed type: $($Table.name).$columnName type=$type" }
 }
}

foreach($table in $schema.tables){
 foreach($column in $table.columns){
  if($null -ne $column.default -and ([string]$column.default).Trim() -eq "''"){
   $errors.Add("empty-string default: $($table.logicalDatabase).$($table.name).$($column.name)")
  }
 }
 foreach($index in @($table.uniqueKeys)+@($table.indexes)){
  $indexColumns=@($index.columns)
  if($null -ne $index.PSObject.Properties['vendorColumns'] -and
      $null -ne $index.vendorColumns.PSObject.Properties['mariadb']){
   $indexColumns=@($index.vendorColumns.mariadb)
  }
  try{
   $encodedBytes=0
   foreach($expression in $indexColumns){$encodedBytes+=Get-MariaDbIndexColumnBytes $table ([string]$expression)}
   if($encodedBytes -gt 3072){
    $errors.Add("MariaDB utf8mb4 index exceeds 3072 bytes: $($table.logicalDatabase).$($table.name).$($index.name) bytes=$encodedBytes columns=$($indexColumns -join ',')")
   }
  }catch{$errors.Add($_.Exception.Message)}
 }
}

foreach($group in ($schema.tables | Group-Object logicalDatabase)){
 $tables=@($group.Group)
 $names=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
 foreach($table in $tables){[void]$names.Add([string]$table.name)}
 foreach($table in $tables){
  foreach($foreignKey in @($table.foreignKeys)){
   if(-not $names.Contains([string]$foreignKey.refTable)){
    $errors.Add("missing FK parent: $($group.Name).$($table.name).$($foreignKey.name) -> $($foreignKey.refTable)")
   }
  }
 }

 $incoming=@{}
 $dependents=@{}
 foreach($table in $tables){
  $incoming[[string]$table.name]=0
  $dependents[[string]$table.name]=[Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
 }
 foreach($table in $tables){
  $child=[string]$table.name
  foreach($foreignKey in @($table.foreignKeys)){
   $parent=[string]$foreignKey.refTable
   if($parent -eq $child -or -not $incoming.ContainsKey($parent)){continue}
   if($dependents[$parent].Add($child)){$incoming[$child]=[int]$incoming[$child]+1}
  }
 }
 $ready=[Collections.Generic.Queue[string]]::new()
 foreach($name in @($incoming.Keys | Where-Object {[int]$incoming[$_] -eq 0} | Sort-Object)){ $ready.Enqueue($name) }
 $visited=0
 while($ready.Count -gt 0){
  $name=$ready.Dequeue()
  $visited++
  foreach($child in @($dependents[$name] | Sort-Object)){
   $incoming[$child]=[int]$incoming[$child]-1
   if([int]$incoming[$child] -eq 0){$ready.Enqueue($child)}
  }
 }
 if($visited -ne $tables.Count){
  $cycle=@($incoming.Keys | Where-Object {[int]$incoming[$_] -gt 0} | Sort-Object)
  $errors.Add("FK cycle: $($group.Name): $($cycle -join ', ')")
 }
}

if($errors.Count -gt 0){
 $errors | ForEach-Object {Write-Error $_ -ErrorAction Continue}
 throw "Canonical DDL safety gate failed: $($errors.Count) issue(s)"
}
Write-Host "Canonical DDL safety gate PASS: no empty-string defaults, oversized MariaDB indexes, missing FK parents, or FK cycles."
