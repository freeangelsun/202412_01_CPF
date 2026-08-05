param([string]$BaselineSha = "")
$ErrorActionPreference = "Stop"
if (-not $BaselineSha) { $BaselineSha = (git rev-parse HEAD).Trim() }
python cpf-tools/scripts/devgpt-control-v9/build_full_assignment.py --repo-root . --baseline-sha $BaselineSha
python cpf-tools/scripts/devgpt-control-v9/validate_devgpt_control_v9.py --repo-root . --require-full-assignment
