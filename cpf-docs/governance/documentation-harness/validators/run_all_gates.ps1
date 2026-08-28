param([Parameter(Mandatory=$true)][string]$Manifest)
$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $here 'validate_harness.ps1'); if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL harness'}
& (Join-Path $here 'validate_source_alignment.ps1'); if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL source alignment'}
& (Join-Path $here 'validate_quality_fixtures.ps1'); if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL fixtures'}
& (Join-Path $here 'validate_readme.ps1') -Path 'README.md'; if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL readme'}
& (Join-Path $here 'validate_docx_artifacts.ps1'); if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL docx'}
$py=Get-Command python -ErrorAction SilentlyContinue;if(-not $py){$py=Get-Command py -ErrorAction SilentlyContinue}
if(-not $py){throw 'ALL_GATES=FAIL reader-task exact validator requires Python'}
$reader=Join-Path $here 'validate_reader_task_coverage.py'
if($py.Name-eq 'py'){& $py.Source -3 $reader}else{& $py.Source $reader};if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL reader tasks'}
& (Join-Path $here 'validate_readability_actionability.ps1'); if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL readability/actionability'}
& (Join-Path $here 'validate_final_acceptance.ps1') -Manifest $Manifest; if($LASTEXITCODE-ne 0){throw 'ALL_GATES=FAIL final acceptance'}
Write-Host 'ALL_GATES=PASS'
