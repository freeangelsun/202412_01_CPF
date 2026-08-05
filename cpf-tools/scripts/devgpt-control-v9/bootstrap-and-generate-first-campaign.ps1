param(
  [Parameter(Mandatory=$true)][string]$CampaignId,
  [int]$AssignmentRevision=1,
  [int]$MaxItemsPerSession=8,
  [string]$BaselineSha=""
)
$ErrorActionPreference="Stop"
if (-not $BaselineSha) { $BaselineSha=(git rev-parse HEAD).Trim() }
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/initialize-development-management.ps1 -BaselineSha $BaselineSha
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/generate-development-requests.ps1 -CampaignId $CampaignId -AssignmentRevision $AssignmentRevision -MaxItemsPerSession $MaxItemsPerSession -BaselineSha $BaselineSha
