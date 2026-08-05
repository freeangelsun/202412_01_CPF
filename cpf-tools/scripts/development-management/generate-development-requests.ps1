param([Parameter(Mandatory=$true)][string]$CampaignId,[int]$MaxItemsPerSession=8,[string]$BaselineSha="")
$ErrorActionPreference="Stop"
if (-not $BaselineSha) { $BaselineSha=(git rev-parse HEAD).Trim() }
python cpf-tools/scripts/development-management/generate_development_requests.py --repo-root . --campaign-id $CampaignId --baseline-sha $BaselineSha --max-items-per-session $MaxItemsPerSession
