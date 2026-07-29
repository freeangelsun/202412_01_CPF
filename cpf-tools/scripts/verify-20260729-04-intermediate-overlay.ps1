param(
    [Parameter(Mandatory=$false)][string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
)
$ErrorActionPreference = 'Stop'
function Assert-True([bool]$Condition,[string]$Message){ if(-not $Condition){ throw $Message } }
$required = @(
 'cpf-docs\quality\qa-20260729\CPF_QA_387_SCENARIO_CLOSURE_MATRIX_20260729_04.csv',
 'cpf-docs\work\current\CPF_CHATGPT_NEXT_SESSION_DEVELOPMENT_HANDOVER_20260729_04.md',
 'cpf-docs\work\current\CPF_CODEX_QA_387_VALIDATION_REQUEST_20260729_04.md',
 'cpf-core\src\main\java\com\cpf\core\api\cache\CpfCachePort.java',
 'cpf-core\src\main\java\com\cpf\core\api\tabular\CpfTabularReader.java',
 'cpf-common\src\main\java\com\cpf\common\cache\CpfRedisCacheProvider.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\filejob\AdmFileJobService.java',
 'cpf-biz-admin\frontend\src\components\CpfTreeNode.vue',
 'cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeSafetyGuard.java'
)
foreach($rel in $required){ Assert-True (Test-Path (Join-Path $ProjectRoot $rel)) "Missing required artifact: $rel" }
$matrix = Import-Csv (Join-Path $ProjectRoot 'cpf-docs\quality\qa-20260729\CPF_QA_387_SCENARIO_CLOSURE_MATRIX_20260729_04.csv')
Assert-True ($matrix.Count -eq 387) "Scenario matrix count must be 387, actual=$($matrix.Count)"
Assert-True (($matrix.id | Sort-Object -Unique).Count -eq 387) 'Scenario IDs must be unique.'
$invalid = $matrix | Where-Object { $_.development_preparation_status -ne '완료' -or $_.execution_status -ne '미검증' }
Assert-True ($invalid.Count -eq 0) 'Checkpoint contract mismatch: definition must be 완료 and unexecuted result must remain 미검증.'
$garbage = Get-ChildItem $ProjectRoot -Recurse -File | Where-Object { $_.Extension -in '.class','.log','.tmp','.bak' -and $_.FullName -notmatch '\\build\\' }
Assert-True ($garbage.Count -eq 0) "Repository garbage detected: $($garbage.FullName -join ', ')"
Write-Host "CPF 20260729_04 intermediate overlay verification PASS (scenarioCount=387)"
