[CmdletBinding()]
param(
 [string[]]$Modules=@(),
 [switch]$Full,
 [ValidateSet('test','assemble','build')][string]$Goal='assemble',
 [switch]$NoDaemon
)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$settings=Get-Content (Join-Path $Root 'settings.gradle') -Raw
$known=[regex]::Matches($settings,"include\s+([^\r\n]+)") | ForEach-Object {
  [regex]::Matches($_.Groups[1].Value,"'([^']+)'") | ForEach-Object {$_.Groups[1].Value}
} | Sort-Object -Unique
if($Full){ $selected=@($known) }
else {
  $selected=@($Modules | ForEach-Object { $_ -split ',' } | ForEach-Object {$_.Trim()} | Where-Object {$_})
  if(!$selected.Count){ throw 'Use -Modules cpf-member,cpf-account or -Full.' }
}
foreach($m in $selected){ if($known -notcontains $m){ throw "Unknown/unregistered Gradle module: $m" } }
$wrapper=if($IsWindows){Join-Path $Root 'gradlew.bat'}else{Join-Path $Root 'gradlew'}
if(!(Test-Path $wrapper)){ throw "Gradle wrapper not found: $wrapper" }
$tasks=@()
foreach($m in $selected){ $tasks += ":${m}:$Goal" }
$args=@('--stacktrace')
if($NoDaemon){$args+='--no-daemon'}
Write-Host "CPF modules: $($selected -join ', ')"
Write-Host "Tasks: $($tasks -join ' ')"
& $wrapper @tasks @args
if($LASTEXITCODE -ne 0){ throw "Gradle module-set build failed ($LASTEXITCODE)." }
