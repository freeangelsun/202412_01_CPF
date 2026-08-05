param([Parameter(Mandatory=$true)][string]$CampaignId,[int]$MaxItemsPerSession=8,[string]$BaselineSha="")
$ErrorActionPreference="Stop"
if (-not $BaselineSha) { $BaselineSha=(git rev-parse HEAD).Trim() }
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/initialize-devgpt-control-v9.ps1 -BaselineSha $BaselineSha
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/generate-development-requests.ps1 -CampaignId $CampaignId -MaxItemsPerSession $MaxItemsPerSession -BaselineSha $BaselineSha
