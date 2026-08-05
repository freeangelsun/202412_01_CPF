param([Parameter(Mandatory=$true)][string]$Feed)
$ErrorActionPreference="Stop"
python cpf-tools/scripts/development-management/apply_qa_reopen_feed.py --repo-root . --feed $Feed
python cpf-tools/scripts/development-management/validate_development_management.py --repo-root .
