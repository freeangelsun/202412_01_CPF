param([Parameter(Mandatory=$true)][string]$Results)
$ErrorActionPreference="Stop"
python cpf-tools/scripts/devgpt-control-v9/merge_session_results.py --repo-root . --results $Results
python cpf-tools/scripts/devgpt-control-v9/validate_devgpt_control_v9.py --repo-root .
