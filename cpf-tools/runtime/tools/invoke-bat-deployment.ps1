param(
    [string] $ControlServerBaseUrl = $(if ($env:CPF_BATCH_CONTROL_URL) { $env:CPF_BATCH_CONTROL_URL } else { 'http://127.0.0.1:8180' }),
    [Parameter(Mandatory=$true)][string] $PlanId,
    [Parameter(Mandatory=$true)][long] $ApprovalRequestId,
    [Parameter(Mandatory=$true)][string] $CommandRequestId,
    [Parameter(Mandatory=$true)][string] $RequestedBy,
    [Parameter(Mandatory=$true)][string] $ApprovedBy,
    [Parameter(Mandatory=$true)][string] $Reason,
    [string] $ResultDir = 'build/cpf-evidence/current',
    [switch] $Rollback
)
$ErrorActionPreference='Stop'
if($RequestedBy -eq $ApprovedBy){throw 'Requester and approver must be different.'}
if([string]::IsNullOrWhiteSpace($Reason)){throw 'Reason is required.'}
$uri=[Uri]$ControlServerBaseUrl
$prod=$env:SPRING_PROFILES_ACTIVE -match '(^|,)prod(,|$)'
if($prod -and $uri.Scheme -ne 'https'){throw 'Production remote deployment requires HTTPS Control Server URL.'}
$resultRoot=if([IO.Path]::IsPathRooted($ResultDir)){$ResultDir}else{Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path $ResultDir}
New-Item -ItemType Directory -Force -Path $resultRoot|Out-Null
$operation=if($Rollback){'rollback-approved'}else{'execute-approved'}
$endpoint=($ControlServerBaseUrl.TrimEnd('/') + "/api/v1/batch/deployment-plans/$([Uri]::EscapeDataString($PlanId))/$operation")
$payload=[ordered]@{approvalRequestId=$ApprovalRequestId;commandRequestId=$CommandRequestId;expectedVersion=0;requestedBy=$RequestedBy;approvedBy=$ApprovedBy;reason=$Reason}
$started=(Get-Date)
try{
  $response=Invoke-RestMethod -Method Post -Uri $endpoint -ContentType 'application/json' -Body ($payload|ConvertTo-Json -Depth 10) -TimeoutSec 1800
  $state=[string]$response.state
  $evidence=[ordered]@{startedAt=$started.ToString('o');endedAt=(Get-Date).ToString('o');planId=$PlanId;operation=$operation;controlServer=$uri.GetLeftPart([UriPartial]::Authority);state=$state;failureStage=$response.failureStage;message=$response.message;instances=$response.instances}
  $path=Join-Path $resultRoot (if($Rollback){'rollback-deploy-bat.sanitized.json'}else{'remote-deploy-bat.sanitized.json'})
  $evidence|ConvertTo-Json -Depth 30|Set-Content -Encoding UTF8 $path
  if($state -notin @('SUCCEEDED','ROLLED_BACK')){throw "Deployment did not finish successfully. state=$state"}
}catch{
  $failure=[ordered]@{startedAt=$started.ToString('o');endedAt=(Get-Date).ToString('o');planId=$PlanId;operation=$operation;controlServer=$uri.GetLeftPart([UriPartial]::Authority);state='UNKNOWN_RESULT';message='Control result was not proven successful; reconcile before retry.'}
  $path=Join-Path $resultRoot (if($Rollback){'rollback-deploy-bat.sanitized.json'}else{'remote-deploy-bat.sanitized.json'})
  $failure|ConvertTo-Json -Depth 10|Set-Content -Encoding UTF8 $path
  throw
}
