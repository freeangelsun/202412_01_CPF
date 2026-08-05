param([Parameter(Mandatory=$true)][string]$Feed)
$ErrorActionPreference="Stop"
python cpf-tools/scripts/devgpt-control-v9/apply_qa_reopen_feed.py --repo-root . --feed $Feed
python cpf-tools/scripts/devgpt-control-v9/validate_devgpt_control_v9.py --repo-root .
