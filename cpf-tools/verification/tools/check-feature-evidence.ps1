param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/quality-gate")
)
$Utf8=[Text.UTF8Encoding]::new($false);[Console]::InputEncoding=$Utf8;[Console]::OutputEncoding=$Utf8;$OutputEncoding=$Utf8
$ErrorActionPreference='Stop';$Root=(Resolve-Path -LiteralPath $Root).Path
if(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir};New-Item -ItemType Directory -Force -Path $ResultDir|Out-Null
$checks=[Collections.Generic.List[object]]::new()
function AddCheck([string]$id,[string[]]$paths,[string]$meaning){$missing=@($paths|Where-Object{-not(Test-Path -LiteralPath (Join-Path $Root $_))});$checks.Add([ordered]@{id=$id;passed=$missing.Count-eq 0;paths=$paths;missing=$missing;meaning=$meaning;runtimeEvidence=$false})|Out-Null}
AddCheck 'ROOT_BUILD_CONVENTION' @('build.gradle','settings.gradle','cpf-tools/build/cpf-root-conventions.gradle','cpf-tools/build/gradle-plugin/build.gradle') 'Root 선언과 단일 Build Convention Owner 실물을 검증합니다.'
AddCheck 'EDUCATION_CANONICAL' @('cpf-education/build.gradle','cpf-education/src/main/java/com/cpf/education/EducationApplication.java','cpf-education/src/main/resources/verification/manual-135-catalog.json') 'EDU Application과 135 실행 Catalog의 실물을 검증합니다.'
AddCheck 'BATCH_CANONICAL' @('cpf-batch/api/build.gradle','cpf-batch/runtime-support/build.gradle','cpf-batch/runtime/build.gradle','cpf-batch/control-plane/build.gradle','cpf-batch/center-cut/build.gradle','cpf-batch/agent/build.gradle') 'Batch canonical module 실물을 검증합니다.'
AddCheck 'GENERATOR_CANONICAL' @('cpf-tools/generator/config/application-starters.yml','cpf-tools/generator/engine/cpf_domain_generator.py','cpf-tools/generator/tools/create-domain.ps1','cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1') 'Generator catalog/engine/lifecycle 검증기를 확인합니다.'
AddCheck 'DB3_CANONICAL' @('cpf-tools/db/config/database-vendor-coverage.json','cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py','cpf-tools/db/verification/verify-cpf-db-lifecycle-contract.py') 'Oracle/PostgreSQL/MariaDB DB3 검증 Owner 실물을 확인합니다.'
AddCheck 'TOOLS_IA' @('cpf-tools/analysis','cpf-tools/build','cpf-tools/db','cpf-tools/generator','cpf-tools/release','cpf-tools/runtime','cpf-tools/testing','cpf-tools/verification') '허용된 Tools Owner Root를 확인합니다.'
$failed=@($checks|Where-Object{-not$_.passed})
$result=[ordered]@{generatedAt=[DateTimeOffset]::Now.ToString('o');status=if($failed.Count-eq 0){'PASS'}else{'FAIL'};checks=@($checks);failedCount=$failed.Count}
[IO.File]::WriteAllText((Join-Path $ResultDir 'feature-evidence-result.sanitized.json'),($result|ConvertTo-Json -Depth 8),$Utf8)
if($failed.Count){$failed|ForEach-Object{Write-Error ("Feature evidence missing: {0} -> {1}" -f $_.id,($_.missing -join ','))};throw "Feature evidence gate failed: $($failed.Count)"}
Write-Host "CPF_FEATURE_EVIDENCE_GATE=PASS checks=$($checks.Count)"
