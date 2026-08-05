param([string]$BaselineSha = "")
$ErrorActionPreference = "Stop"
if (-not $BaselineSha) { $BaselineSha = (git rev-parse HEAD).Trim() }
python cpf-tools/scripts/development-management/build_full_assignment.py --repo-root . --baseline-sha $BaselineSha
python cpf-tools/scripts/development-management/validate_development_management.py --repo-root . --require-full-assignment
