[CmdletBinding(SupportsShouldProcess=$true)]
param(
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
  [switch]$IncludeBuildArtifacts
)
$ErrorActionPreference='Stop'

# ChatGPT 작업 ZIP에서 과거 잘못 Root에 배치한 적용안내/manifest만 정확한 파일명으로 제거합니다.
# 정식 Root 문서(README.md, CPF_FINAL_TARGET_REQUIREMENTS.md 등)는 절대 pattern 삭제하지 않습니다.
$chatGptRootArtifacts = @(
  'APPLY_README.md',
  'APPLY_CORRECTION.md',
  'APPLY_BOUNDARY_FIX.md',
  'APPLY_OWNER_DB_FIX.md',
  'MANIFEST.txt',
  'MANIFEST.sha256',
  'PATCH_MANIFEST.txt'
)
foreach($name in $chatGptRootArtifacts){
  $path = Join-Path $Root $name
  if((Test-Path $path) -and $PSCmdlet.ShouldProcess($path,'Remove misplaced ChatGPT patch artifact from repository root')){
    Remove-Item $path -Force
  }
}
$legacyRel='cpf-common\src\main\java\com\cpf\common\utils'
$legacy=Join-Path $Root $legacyRel
$imports=@()
Get-ChildItem $Root -Recurse -File -Filter '*.java' | Where-Object { $_.FullName -notlike "$legacy*" } | ForEach-Object {
  $m=Select-String -Path $_.FullName -Pattern 'com\.cpf\.common\.utils\.'
  if($m){ $imports += $m }
}
if($imports.Count){
  Write-Host '[BLOCK] cpf-common.utils still has consumers. Nothing was deleted.' -ForegroundColor Yellow
  $imports | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) $($_.Line.Trim())" }
  throw 'Migrate remaining consumers to com.cpf.core.api.util or an owned cpf-common business helper first.'
}
if(Test-Path $legacy){
  if($PSCmdlet.ShouldProcess($legacy,'Remove obsolete cpf-common.utils after zero-consumer check')){ Remove-Item $legacy -Recurse -Force }
}


# R9 이전 REF 로컬 Center-Cut runner는 BAT ownership을 우회하므로, 새 item adapter가 적용된 뒤 소비자가 없을 때만 제거합니다.
$refLegacyFiles = @(
  'cpf-reference\src\main\java\com\cpf\reference\centercut\application\ReferenceCenterCutEducationService.java',
  'cpf-reference\src\main\java\com\cpf\reference\centercut\dto\ReferenceCenterCutExecutionResponse.java'
)
foreach($rel in $refLegacyFiles){
  $path = Join-Path $Root $rel
  if(!(Test-Path $path)){ continue }
  $className = [System.IO.Path]::GetFileNameWithoutExtension($path)
  $consumers = Get-ChildItem $Root -Recurse -File -Filter '*.java' |
    Where-Object { $_.FullName -ne $path } |
    Select-String -Pattern ("\\b" + [regex]::Escape($className) + "\\b")
  if($consumers){
    Write-Host "[KEEP] $rel still has consumers:" -ForegroundColor Yellow
    $consumers | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber)" }
  } elseif($PSCmdlet.ShouldProcess($path,'Remove obsolete REF local Center-Cut runner artifact')){
    Remove-Item $path -Force
  }
}


# BAT가 Center-Cut Runtime owner가 된 뒤 Core의 legacy auto-configuration은 더 이상 등록하지 않습니다.
$legacyCenterCutAutoConfig = Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\config\CpfCenterCutAutoConfiguration.java'
$autoConfigImports = Join-Path $Root 'cpf-core\src\main\resources\META-INF\spring\org.springframework.boot.autoconfigure.AutoConfiguration.imports'
if(Test-Path $legacyCenterCutAutoConfig){
  $registered = (Test-Path $autoConfigImports) -and
    (Select-String -Path $autoConfigImports -Pattern 'com\.cpf\.core\.config\.CpfCenterCutAutoConfiguration' -Quiet)
  if($registered){
    throw 'Legacy CpfCenterCutAutoConfiguration is still registered. Remove the registration before deleting the old Core runtime.'
  }
  if($PSCmdlet.ShouldProcess($legacyCenterCutAutoConfig,'Remove obsolete Core Center-Cut auto-configuration after BAT ownership migration')){
    Remove-Item $legacyCenterCutAutoConfig -Force
  }
}

# Core의 과거 Center-Cut Runtime 계약은 BAT/Domain이 public api/spi로 완전히 이관된 경우에만 제거합니다.
$legacyCenterCut = Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\common\batch\centercut'
if(Test-Path $legacyCenterCut){
  $legacyConsumers = Get-ChildItem $Root -Recurse -File -Filter '*.java' |
    Where-Object { $_.FullName -notlike "$legacyCenterCut*" } |
    Select-String -Pattern '^\s*import\s+com\.cpf\.core\.common\.batch\.centercut\.'
  if($legacyConsumers){
    Write-Host '[KEEP] legacy core Center-Cut package still has consumers; package was not removed.' -ForegroundColor Yellow
    $legacyConsumers | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) $($_.Line.Trim())" }
  } elseif($PSCmdlet.ShouldProcess($legacyCenterCut,'Remove obsolete core Center-Cut package after zero-consumer check')){
    Remove-Item $legacyCenterCut -Recurse -Force
  }
}


# BAT Runtime owner 구현이 존재할 때만 ADM에 남은 과거 BAT DB/Scheduler 소유 코드를 제거합니다.
$batOwnerRequired = @(
  'cpf-batch\scheduler\src\main\java\com\cpf\batch\scheduler\BatchSchedulerApplication.java',
  'cpf-batch\scheduler\src\main\java\com\cpf\batch\scheduler\SchedulerCoordinator.java',
  'cpf-batch\scheduler\src\main\java\com\cpf\batch\scheduler\SchedulerDispatchService.java',
  'cpf-batch\control-server\src\main\java\com\cpf\batch\control\compat\BatchOperationsCompatibilityService.java',
  'cpf-admin\src\main\java\com\cpf\admin\opr\batch\RemoteCpfBatchOperationsAdapter.java'
)
$missingBatOwner = @($batOwnerRequired | Where-Object { !(Test-Path (Join-Path $Root $_)) })
if($missingBatOwner.Count){
  throw "BAT Owner replacement is incomplete; ADM legacy BAT runtime was not removed:`n$($missingBatOwner -join "`n")"
}
$admLegacyBat = @(
  'cpf-admin\src\main\java\com\cpf\admin\config\AdmBatchRepositoryConfig.java',
  'cpf-admin\src\main\java\com\cpf\admin\opr\service\CpfBatchScheduleService.java',
  'cpf-admin\src\main\java\com\cpf\admin\opr\service\CpfBatchExecutionTargetService.java',
  'cpf-admin\src\main\java\com\cpf\admin\opr\service\CpfBatchScheduler.java'
)
foreach($rel in $admLegacyBat){
  $path = Join-Path $Root $rel
  if((Test-Path $path) -and $PSCmdlet.ShouldProcess($path,'Remove obsolete ADM-owned BAT runtime after BAT Owner migration')){
    Remove-Item $path -Force
  }
}
$legacyCandidate = Join-Path $Root 'cpf-admin\src\main\java\com\cpf\admin\opr\dto\CpfBatchScheduleCandidate.java'
if(Test-Path $legacyCandidate){
  $candidateConsumers = Get-ChildItem $Root -Recurse -File -Filter '*.java' |
    Where-Object { $_.FullName -ne $legacyCandidate } |
    Select-String -Pattern '\bCpfBatchScheduleCandidate\b'
  if(!$candidateConsumers -and $PSCmdlet.ShouldProcess($legacyCandidate,'Remove obsolete ADM batch schedule DTO')){
    Remove-Item $legacyCandidate -Force
  }
}



# R10 정본 cleanup gate가 삭제 대상으로 선언한 잔존 경로를 한 번에 정리합니다.
# 이 목록은 cpf-tools/scripts/check-r10-cleanup.ps1와 동일한 정책을 사용합니다.
$r10Obsolete = @(
  'cpf-external',
  'cpf-tools\db\source',
  'docker-compose.local.yml',
  'cpf-core\src\main\java\com\cpf\core\common\batch',
  'cpf-core\src\test\java\com\cpf\core\common\batch',
  'cpf-core\src\main\java\com\cpf\core\config\CpfBatchAutoConfiguration.java',
  'cpf-core\src\main\java\com\cpf\core\config\CpfCenterCutAutoConfiguration.java',
  'cpf-biz-admin\frontend\src\features\console.ts',
  'cpf-biz-admin\frontend\src\features\directory',
  'cpf-biz-admin\frontend\src\features\access',
  'cpf-biz-admin\frontend\src\features\approval',
  'cpf-biz-admin\frontend\src\features\support',
  'cpf-admin\frontend\src\features\observability',
  'cpf-admin\frontend\src\features\platform',
  'cpf-admin\frontend\src\features\business',
  'cpf-admin\frontend\src\features\access',
  'cpf-admin\frontend\src\features\reference',
  'cpf-admin\src\main\java\com\cpf\admin\opr\dto\AdmBusinessDayRequest.java'
)

# cpf-core/common/batch 물리 삭제 전에 남은 Consumer를 Owner 기준으로 자동 이관합니다.
# - 범용 실행 계약/이벤트 모델: cpf-core.api.batch
# - BAT Runtime 구현: cpf-batch.runtime
# - BAT 밖에서 Runtime 구현을 직접 참조하면 자동 숨기지 않고 BLOCK합니다.
$legacyBatchRoot = Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\common\batch'
$legacyCoreBatchTestRoot = Join-Path $Root 'cpf-core\src\test\java\com\cpf\core\common\batch'
$batRoot = Join-Path $Root 'cpf-batch'
$plannedLegacyBatchRemovalFiles = @(
  (Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\config\CpfBatchAutoConfiguration.java'),
  (Join-Path $Root 'cpf-core\src\main\java\com\cpf\core\config\CpfCenterCutAutoConfiguration.java')
)

$legacyBatchMigrationRequired =
  (Test-Path -LiteralPath $legacyBatchRoot) -or
  @($plannedLegacyBatchRemovalFiles | Where-Object { Test-Path -LiteralPath $_ }).Count -gt 0
if($legacyBatchMigrationRequired){
$requiredPublicBatchContracts = @(
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchEvent.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchEventPublisher.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchEventType.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchExecutionRequest.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchExecutionResult.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchOperationType.java',
  'cpf-core\src\main\java\com\cpf\core\api\batch\CpfBatchLogPaths.java'
)
$requiredBatRuntime = @(
  'cpf-batch\runtime-common\src\main\java\com\cpf\batch\runtime\RuntimeReporter.java',
  'cpf-batch\runtime-common\src\main\java\com\cpf\batch\runtime\RuntimeStateProvider.java',
  'cpf-batch\control-server\src\main\java\com\cpf\batch\control\internal\JdbcRuntimeRegistry.java',
  'cpf-batch\worker\src\main\java\com\cpf\batch\worker\WorkerRuntime.java',
  'cpf-batch\worker\src\main\java\com\cpf\batch\worker\JobPackDispatcher.java',
  'cpf-batch\scheduler\src\main\java\com\cpf\batch\scheduler\SchedulerCoordinator.java',
  'cpf-batch\center-cut-runner\src\main\java\com\cpf\batch\centercut\runner\CenterCutRuntime.java'
)
$missingBatchBoundary = @(($requiredPublicBatchContracts + $requiredBatRuntime) | Where-Object { !(Test-Path (Join-Path $Root $_)) })
if($missingBatchBoundary.Count){
  throw "Batch physical migration prerequisites are missing; no legacy batch deletion was attempted:`n$($missingBatchBoundary -join "`n")"
}

function Set-CpfJavaText {
  param([string]$Path,[string]$Text,[string]$Reason)
  $current = Get-Content -LiteralPath $Path -Raw
  if($current -ceq $Text){ return $false }
  if($PSCmdlet.ShouldProcess($Path,$Reason)){
    Set-Content -LiteralPath $Path -Value $Text -Encoding utf8 -NoNewline
    return $true
  }
  return $false
}

$publicSameName = @(
  'CpfBatchEvent',
  'CpfBatchEventPublisher',
  'CpfBatchEventType',
  'CpfBatchExecutionRequest',
  'CpfBatchExecutionResult',
  'CpfBatchOperationType'
)
# 단일 cpf-batch/src Runtime Class로의 자동 치환은 더 이상 허용하지 않습니다.
# 남은 내부 Runtime Consumer는 아래 zero-consumer 검사에서 차단하고 역할별 Owner로 수동 이관합니다.
$batRuntimeMap = [ordered]@{}

$migratedFiles = [System.Collections.Generic.List[string]]::new()
$illegalExternalRuntimeConsumers = [System.Collections.Generic.List[string]]::new()
$javaFiles = @(Get-ChildItem $Root -Recurse -File -Filter '*.java' -ErrorAction SilentlyContinue | Where-Object {
  $_.FullName -notlike "$legacyBatchRoot*" -and
  $_.FullName -notlike "$legacyCoreBatchTestRoot*" -and
  $plannedLegacyBatchRemovalFiles -notcontains $_.FullName
})
foreach($file in $javaFiles){
  $content = Get-Content -LiteralPath $file.FullName -Raw
  $updated = $content
  foreach($name in $publicSameName){
    $updated = $updated.Replace("com.cpf.core.common.batch.$name", "com.cpf.core.api.batch.$name")
  }

  $isBat = $file.FullName -like "$batRoot*"
  foreach($entry in $batRuntimeMap.GetEnumerator()){
    $oldName = [string]$entry.Key
    $newName = [string]$entry.Value
    $oldFqcn = "com.cpf.core.common.batch.$oldName"
    if($updated.Contains($oldFqcn)){
      if(!$isBat){
        if($oldName -eq 'CpfBatchJobLogPath'){
          $updated = $updated.Replace($oldFqcn, 'com.cpf.core.api.batch.CpfBatchLogPaths')
          $updated = [regex]::Replace($updated, "\bCpfBatchJobLogPath\b", 'CpfBatchLogPaths')
        } else {
          $illegalExternalRuntimeConsumers.Add("$($file.FullName): $oldFqcn")
          continue
        }
      } else {
        $updated = $updated.Replace($oldFqcn, "com.cpf.batch.runtime.$newName")
        $updated = [regex]::Replace($updated, "\b$([regex]::Escape($oldName))\b", $newName)
      }
    }
  }
  if($updated -cne $content){
    if(Set-CpfJavaText -Path $file.FullName -Text $updated -Reason 'Migrate legacy Core Batch consumer to public API/BAT owner runtime'){
      $migratedFiles.Add($file.FullName.Substring($Root.Length).TrimStart([char[]]@('\','/')))
    }
  }
}
if($illegalExternalRuntimeConsumers.Count){
  Write-Host '[BLOCK] Non-BAT modules still depend on BAT runtime implementation classes.' -ForegroundColor Yellow
  $illegalExternalRuntimeConsumers | Sort-Object -Unique | ForEach-Object { Write-Host "  $_" }
  throw 'Move non-BAT runtime consumers behind CpfBatchOperationsPort before deleting legacy Core Batch runtime.'
}

# 현재 구조에서는 Legacy Core Runtime Test를 삭제된 aggregate Source로 자동 생성하지 않습니다.
# 남아 있다면 역할별 독립 Runtime 또는 REF Handler Test로 명시적으로 매핑해야 합니다.
$legacyCoreBatchTests = @(
  Get-ChildItem -LiteralPath $legacyCoreBatchTestRoot -Recurse -File -Filter '*.java' -ErrorAction SilentlyContinue
)
if($legacyCoreBatchTests.Count){
  throw "Legacy Core Batch test remains; map it explicitly before cleanup:`n$($legacyCoreBatchTests.FullName -join "`n")"
}

if(Test-Path $legacyCoreBatchTestRoot){
  $remainingTestFiles = @(Get-ChildItem $legacyCoreBatchTestRoot -Recurse -File -ErrorAction SilentlyContinue)
  if(!$remainingTestFiles.Count -and $PSCmdlet.ShouldProcess($legacyCoreBatchTestRoot,'Remove empty legacy Core Batch test directory')){
    Remove-Item $legacyCoreBatchTestRoot -Recurse -Force
  }
}

# 자동 이관 뒤에는 import뿐 아니라 FQCN 직접 사용까지 전수 0건이어야 합니다.
$legacyBatchConsumers = @(Get-ChildItem $Root -Recurse -File -Filter '*.java' -ErrorAction SilentlyContinue |
  Where-Object {
    $_.FullName -notlike "$legacyBatchRoot*" -and
    $_.FullName -notlike "$legacyCoreBatchTestRoot*" -and
    $plannedLegacyBatchRemovalFiles -notcontains $_.FullName
  } |
  Select-String -Pattern 'com\.cpf\.core\.common\.batch\.' -ErrorAction SilentlyContinue)
if($legacyBatchConsumers.Count){
  Write-Host '[BLOCK] legacy cpf-core.common.batch still has consumers after automatic ownership migration.' -ForegroundColor Yellow
  $legacyBatchConsumers | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) $($_.Line.Trim())" }
  throw 'Batch consumer migration is incomplete; legacy Core Batch runtime was not deleted.'
}
Write-Host "[PASS] Legacy Core Batch consumers migrated: $($migratedFiles.Count) file(s)." -ForegroundColor Green
$migratedFiles | Sort-Object -Unique | ForEach-Object { Write-Host "  [MIGRATE] $_" }
Write-Host '[PASS] Legacy Core Batch consumer/FQCN count = 0.' -ForegroundColor Green
} else {
  Write-Host '[PASS] Legacy Core Batch migration is already complete; obsolete source prerequisites are not required.' -ForegroundColor Green
}

# 삭제된 AutoConfiguration class를 import 목록이 계속 참조하지 않도록 동기화합니다.
$autoConfigImports = Join-Path $Root 'cpf-core\src\main\resources\META-INF\spring\org.springframework.boot.autoconfigure.AutoConfiguration.imports'
if(Test-Path $autoConfigImports){
  $before = @(Get-Content -LiteralPath $autoConfigImports)
  $after = @($before | Where-Object {
    $_ -notmatch '^com\.cpf\.core\.config\.CpfBatchAutoConfiguration\s*$' -and
    $_ -notmatch '^com\.cpf\.core\.config\.CpfCenterCutAutoConfiguration\s*$'
  })
  if($after.Count -ne $before.Count -and $PSCmdlet.ShouldProcess($autoConfigImports,'Remove obsolete Batch/CenterCut auto-configuration registrations')){
    Set-Content -LiteralPath $autoConfigImports -Value $after -Encoding utf8
  }
}

foreach($rel in $r10Obsolete){
  $path = Join-Path $Root $rel
  if(Test-Path $path){
    if($PSCmdlet.ShouldProcess($path,'Remove R10 canonical obsolete artifact')){
      Remove-Item -LiteralPath $path -Recurse -Force
      Write-Host "[REMOVE] $rel"
    }
  }
}

# Root runtime 디렉터리는 Git 추적 파일이 전혀 없을 때만 제거합니다.
foreach($dir in @('logs','tmp','temp')){
  $path = Join-Path $Root $dir
  if(!(Test-Path $path)){ continue }
  $tracked = @(& git -C $Root ls-files -- $dir 2>$null)
  if($tracked.Count){
    Write-Host "[KEEP] tracked root runtime directory: $dir" -ForegroundColor Yellow
    continue
  }
  if($PSCmdlet.ShouldProcess($path,'Remove untracked root runtime directory')){
    Remove-Item -LiteralPath $path -Recurse -Force
    Write-Host "[REMOVE] root runtime directory: $dir"
  }
}

# check-r10-cleanup.ps1가 금지하는 Root garbage를 같은 기준으로 제거합니다.
$rootGarbage = @(Get-ChildItem $Root -File -Force -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -match '\.(zip|log|tmp|bak)$' -or $_.Name -match '^(APPLY|PATCH|EVIDENCE)_' } |
  Where-Object { $_.Name -ne 'README.md' })
foreach($file in $rootGarbage){
  if($PSCmdlet.ShouldProcess($file.FullName,'Remove R10 root garbage file')){
    Remove-Item -LiteralPath $file.FullName -Force
    Write-Host "[REMOVE] root garbage: $($file.Name)"
  }
}

$garbagePatterns=@('*.bak','*.orig','*.rej','*.tmp','*.swp','*~')
foreach($pattern in $garbagePatterns){
  Get-ChildItem $Root -Recurse -File -Filter $pattern -ErrorAction SilentlyContinue | ForEach-Object {
    if($_.FullName -match '[\\/]cpf-docs[\\/]evidence[\\/]'){ return }
    if($PSCmdlet.ShouldProcess($_.FullName,'Remove development residue')){ Remove-Item $_.FullName -Force }
  }
}
if($IncludeBuildArtifacts){
  $canonicalBuildToolRoot = [IO.Path]::GetFullPath((Join-Path $Root 'cpf-tools/build'))
  $repositoryRootPrefix = [IO.Path]::GetFullPath($Root).TrimEnd(
    [IO.Path]::DirectorySeparatorChar,
    [IO.Path]::AltDirectorySeparatorChar
  ) + [IO.Path]::DirectorySeparatorChar
  $dirs = Get-ChildItem $Root -Recurse -Directory | Where-Object {
    if($_.Name -in @('.gradle','node_modules','dist')){
      return $true
    }
    if($_.Name -notin @('build','bin')){
      return $false
    }
    # com/cpf/build 같은 Source package를 산출물로 오인하지 않고, Gradle/JDT가
    # Project Root에 만든 build/bin만 재생성 가능 산출물로 정리합니다.
    $projectDirectory = $_.Parent.FullName
    return (
      (Test-Path -LiteralPath (Join-Path $projectDirectory 'build.gradle') -PathType Leaf) -or
      (Test-Path -LiteralPath (Join-Path $projectDirectory 'build.gradle.kts') -PathType Leaf)
    )
  }
  foreach($d in ($dirs | Sort-Object FullName -Descending)){
    $resolvedDirectory = [IO.Path]::GetFullPath($d.FullName)
    if(-not $resolvedDirectory.StartsWith($repositoryRootPrefix,[StringComparison]::OrdinalIgnoreCase)){
      throw "Repository 외부 Build Artifact 삭제를 거부합니다: $resolvedDirectory"
    }
    if($resolvedDirectory.Equals($canonicalBuildToolRoot,[StringComparison]::OrdinalIgnoreCase)){
      Write-Host '[KEEP] canonical build tooling source owner: cpf-tools/build' -ForegroundColor Yellow
      continue
    }
    if($resolvedDirectory -match '[\\/]cpf-docs[\\/]evidence[\\/]'){ continue }
    if($PSCmdlet.ShouldProcess($resolvedDirectory,'Remove regenerable build artifact')){ Remove-Item -LiteralPath $resolvedDirectory -Recurse -Force -ErrorAction SilentlyContinue }
  }

  # Build Support Unit의 정본 Source는 cpf-tools/build로 이동했습니다. 과거 Root에는
  # Git에서 무시되는 Gradle/JDT 산출물과 빈 디렉터리만 남을 수 있으므로, 새 정본 Source와
  # 추적 파일 삭제 상태를 확인한 뒤 정확한 두 경로만 제거합니다.
  $relocatedBuildUnits = [ordered]@{
    'cpf-gradle-plugin' = @(
      'cpf-tools/build/gradle-plugin/build.gradle',
      'cpf-tools/build/gradle-plugin/settings.gradle',
      'cpf-tools/build/gradle-plugin/src/main/groovy/com/cpf/build/CpfDomainConventionPlugin.groovy'
    )
    'cpf-platform-bom' = @(
      'cpf-tools/build/platform-bom/build.gradle',
      'cpf-tools/build/platform-bom/settings.gradle'
    )
  }
  foreach($legacyRelativePath in $relocatedBuildUnits.Keys){
    $legacyRoot = [IO.Path]::GetFullPath((Join-Path $Root $legacyRelativePath))
    if(-not $legacyRoot.StartsWith($repositoryRootPrefix,[StringComparison]::OrdinalIgnoreCase)){
      throw "Repository 외부 Legacy Build Unit 삭제를 거부합니다: $legacyRoot"
    }
    foreach($canonicalRelativePath in $relocatedBuildUnits[$legacyRelativePath]){
      $canonicalPath = Join-Path $Root $canonicalRelativePath
      if(-not (Test-Path -LiteralPath $canonicalPath -PathType Leaf)){
        throw "이관된 Build Unit 정본 Source가 없습니다: $canonicalRelativePath"
      }
    }
    $legacyTrackedFiles = @(& git -C $Root ls-files -- $legacyRelativePath 2>$null)
    $legacyTrackedFilesStillPresent = @(
      $legacyTrackedFiles | Where-Object { Test-Path -LiteralPath (Join-Path $Root $_) -PathType Leaf }
    )
    if($legacyTrackedFilesStillPresent.Count){
      throw (
        "Legacy Build Unit의 추적 Source가 아직 물리적으로 남아 있어 삭제하지 않습니다: " +
        ($legacyTrackedFilesStillPresent -join ', ')
      )
    }
    $legacyUntrackedFiles = @(
      & git -C $Root ls-files --others --exclude-standard -- $legacyRelativePath 2>$null
    )
    if($legacyUntrackedFiles.Count){
      throw (
        "Legacy Build Unit 아래 보존 여부를 알 수 없는 Untracked 파일이 있어 삭제하지 않습니다: " +
        ($legacyUntrackedFiles -join ', ')
      )
    }
    if(
      (Test-Path -LiteralPath $legacyRoot -PathType Container) -and
      $PSCmdlet.ShouldProcess($legacyRoot,'Remove relocated legacy Build Unit cache and empty directories')
    ){
      Remove-Item -LiteralPath $legacyRoot -Recurse -Force
      Write-Host "[REMOVE] relocated legacy Build Unit root: $legacyRelativePath"
    }
  }
}
Write-Host '[PASS] Cleanup completed. Evidence and product source were not pattern-deleted.' -ForegroundColor Green
