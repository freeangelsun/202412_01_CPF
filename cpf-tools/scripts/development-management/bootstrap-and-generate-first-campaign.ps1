param([Parameter(Mandatory=$true)][string]$CampaignId,[int]$MaxItemsPerSession=8,[string]$BaselineSha="")
$ErrorActionPreference="Stop"
if (-not $BaselineSha) { $BaselineSha=(git rev-parse HEAD).Trim() }
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1 -BaselineSha $BaselineSha
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/generate-development-requests.ps1 -CampaignId $CampaignId -MaxItemsPerSession $MaxItemsPerSession -BaselineSha $BaselineSha
