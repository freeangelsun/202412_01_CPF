param(
  [Parameter(Mandatory=$true)][string]$CampaignId,
  [int]$AssignmentRevision=1,
  [int]$MaxItemsPerSession=8,
  [string]$BaselineSha=""
)
$ErrorActionPreference="Stop"
if (-not $BaselineSha) { $BaselineSha=(git rev-parse HEAD).Trim() }
python cpf-tools/scripts/devgpt-control-v9/generate_development_requests.py `
  --repo-root . `
  --management-dir cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9 `
  --campaign-id $CampaignId `
  --baseline-sha $BaselineSha `
  --assignment-revision $AssignmentRevision `
  --max-items-per-session $MaxItemsPerSession
