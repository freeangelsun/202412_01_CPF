param(
  [Parameter(Mandatory=$true)][string]$Results,
  [Parameter(Mandatory=$true)][string]$SessionId,
  [Parameter(Mandatory=$true)][string]$DevelopmentRequestId
)
$ErrorActionPreference="Stop"
python cpf-tools/scripts/devgpt-control-v9/merge_session_results.py --repo-root . --results $Results --session-id $SessionId --development-request-id $DevelopmentRequestId
python cpf-tools/scripts/devgpt-control-v9/validate_development_management.py --repo-root .
