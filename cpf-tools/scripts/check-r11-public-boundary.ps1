[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path)
$ErrorActionPreference='Stop'
$failures = [System.Collections.Generic.List[string]]::new()
function Add-Failure([string]$m){ $failures.Add($m) | Out-Null; Write-Host "[FAIL] $m" -ForegroundColor Red }
function Pass([string]$m){ Write-Host "[PASS] $m" -ForegroundColor Green }

$generator = Join-Path $Root 'cpf-tools\generator\create-domain.ps1'
if(!(Test-Path $generator)){ Add-Failure "Generator not found: $generator" }
else {
    $bad = Select-String -Path $generator -Pattern 'com\.cpf\.core\.common\.'
    if($bad){ Add-Failure ("Golden Generator imports internal core packages: " + (($bad | ForEach-Object {$_.LineNumber}) -join ',')) }
    else { Pass 'Golden Generator has zero com.cpf.core.common.* references' }
}

$required = @(
 'cpf-core/src/main/java/com/cpf/core/api/base/CpfBaseController.java',
 'cpf-core/src/main/java/com/cpf/core/api/execution/CpfOnlineTransaction.java',
 'cpf-core/src/main/java/com/cpf/core/api/http/CpfHttpClient.java',
 'cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionContext.java',
 'cpf-core/src/main/java/com/cpf/core/api/database/CpfSqlResources.java',
 'cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerClient.java',
 'cpf-core/src/main/java/com/cpf/core/api/filetransfer/CpfFileTransferClient.java',
 'cpf-core/src/main/java/com/cpf/core/api/centercut/CpfCenterCutTarget.java',
 'cpf-core/src/main/java/com/cpf/core/spi/centercut/CenterCutHandler.java',
 'cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceCaller.java'
)
foreach($rel in $required){ if(!(Test-Path (Join-Path $Root $rel))){ Add-Failure "Missing public boundary: $rel" } }
if($failures.Count -eq 0){ Pass 'Required public boundary contracts exist' }

# Generated business modules must not consume cpf-core internal implementations.
$platform = @('cpf-core','cpf-common','cpf-admin','cpf-biz-admin','cpf-batch','cpf-gateway','cpf-reference','cpf-tools','cpf-docs','deploy')
$modules = Get-ChildItem $Root -Directory -Filter 'cpf-*' | Where-Object { $platform -notcontains $_.Name }
foreach($module in $modules){
    $src = Join-Path $module.FullName 'src\main\java'
    if(!(Test-Path $src)){ continue }
    $bad = Get-ChildItem $src -Recurse -File -Filter '*.java' | Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.'
    if($bad){
        Add-Failure ("$($module.Name) imports cpf-core internal classes:`n" + (($bad | ForEach-Object {"  $($_.Path):$($_.LineNumber) $($_.Line.Trim())"}) -join "`n"))
    } else { Pass "$($module.Name) generated-domain boundary" }
}

# Reference EDU may demonstrate framework internals only in explicitly internal packages; public EDU controllers must not.
$refEdu = Join-Path $Root 'cpf-reference\src\main\java\com\cpf\reference\utility\controller'
if(Test-Path $refEdu){
    $bad = Get-ChildItem $refEdu -Recurse -File -Filter '*.java' | Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.'
    if($bad){ Add-Failure ('Reference public EDU leaks core internals: ' + (($bad | ForEach-Object {"$($_.Path):$($_.LineNumber)"}) -join ', ')) }
    else { Pass 'Reference public EDU uses public API only' }
}



# BAT Runtime도 Core 내부 구현에 직접 의존하면 안 됩니다. Runtime 간 연결은 api/spi만 사용합니다.
$batCenterCut = Join-Path $Root 'cpf-batch\src\main\java\com\cpf\batch\runtime\centercut'
if(Test-Path $batCenterCut){
  $bad = Get-ChildItem $batCenterCut -Recurse -File -Filter '*.java' | Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.'
  if($bad){ Add-Failure ('BAT Center-Cut leaks core internals: ' + (($bad | ForEach-Object {"$($_.Path):$($_.LineNumber)"}) -join ', ')) }
  else { Pass 'BAT Center-Cut consumes public api/spi only' }
}

# REF Center-Cut Domain adapter 역시 public api/spi를 사용해야 합니다.
$refCenterCut = Join-Path $Root 'cpf-reference\src\main\java\com\cpf\reference\centercut'
if(Test-Path $refCenterCut){
  $bad = Get-ChildItem $refCenterCut -Recurse -File -Filter '*.java' | Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.(batch\.centercut|execution)\.'
  if($bad){ Add-Failure ('Reference Center-Cut still uses legacy/internal contracts: ' + (($bad | ForEach-Object {"$($_.Path):$($_.LineNumber)"}) -join ', ')) }
  else { Pass 'Reference Center-Cut uses public API/SPI contracts' }
}



# REF Batch EDU도 실제 Public Operations/API만 사용해야 합니다.
$refBatch = Join-Path $Root 'cpf-reference\src\main\java\com\cpf\reference\batch'
if(Test-Path $refBatch){
  $bad = Get-ChildItem $refBatch -Recurse -File -Filter '*.java' | Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.(batch|execution)\.'
  if($bad){ Add-Failure ('Reference Batch EDU still uses core internal runtime: ' + (($bad | ForEach-Object {"$($_.Path):$($_.LineNumber)"}) -join ', ')) }
  else { Pass 'Reference Batch EDU uses public API/operations port' }
}


# BAT/REF test도 public Center-Cut 계약만 사용해야 legacy Core runtime을 안전하게 제거할 수 있습니다.
$centerCutTestRoots = @(
  (Join-Path $Root 'cpf-batch\src\test\java'),
  (Join-Path $Root 'cpf-reference\src\test\java')
)
foreach($testRoot in $centerCutTestRoots){
  if(!(Test-Path $testRoot)){ continue }
  $bad = Get-ChildItem $testRoot -Recurse -File -Filter '*.java' |
    Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.batch\.centercut\.'
  if($bad){ Add-Failure ('Center-Cut test leaks legacy Core runtime: ' + (($bad | ForEach-Object {"$($_.Path):$($_.LineNumber)"}) -join ', ')) }
}
if(-not ($failures | Where-Object { $_ -like 'Center-Cut test leaks*' })){ Pass 'Center-Cut tests use public api/spi contracts' }

$legacyCenterCut = Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\common\batch\centercut'
if(Test-Path $legacyCenterCut){
  Add-Failure 'Legacy Core Center-Cut runtime package still exists after cleanup; BAT must be the runtime owner.'
} else { Pass 'Legacy Core Center-Cut runtime package removed' }

if($failures.Count){ throw "CPF R11 public-boundary gate failed ($($failures.Count))." }
Pass 'CPF R11 public-boundary gate completed'
