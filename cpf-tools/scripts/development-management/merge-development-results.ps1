param([Parameter(Mandatory=$true)][string]$Results)
$ErrorActionPreference="Stop"
python cpf-tools/scripts/development-management/merge_session_results.py --repo-root . --results $Results
python cpf-tools/scripts/development-management/validate_development_management.py --repo-root .
