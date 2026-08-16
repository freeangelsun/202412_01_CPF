param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest

$schemaPath=Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$schema=Get-Content $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50

$errors=[Collections.Generic.List[string]]::new()
foreach($table in $schema.tables){
 foreach($column in $table.columns){
  if($null -ne $column.default -and ([string]$column.default).Trim() -eq "''"){
   $errors.Add("empty-string default: $($table.logicalDatabase).$($table.name).$($column.name)")
  }
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
 $errors | ForEach-Object {Write-Error $_}
 throw "Canonical DDL safety gate failed: $($errors.Count) issue(s)"
}
Write-Host "Canonical DDL safety gate PASS: no empty-string defaults, missing FK parents, or FK cycles."
