[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$VerifierRunId,
    [Parameter(Mandatory=$true)][string]$RuntimeDbResultPath,
    [Parameter(Mandatory=$true)][string]$SecretDirectory,
    [Parameter(Mandatory=$true)][string]$ResultPath,
    [string]$LoginId='backoffice-full-local',
    [string]$OperatorName='Backoffice FullLocal Operator',
    [string]$RoleCode='MBW_MANAGER',
    [string]$EnvironmentCode='local',
    [string]$ActiveProfiles='local,local-integrated'
)
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if($VerifierRunId -notmatch '^[a-f0-9]{8,24}$'){throw 'Invalid verifier run id.'}
if($LoginId -notmatch '^[A-Za-z0-9._-]{3,50}$'){throw 'Invalid Backoffice verifier login id.'}
$password=[Environment]::GetEnvironmentVariable('CPF_BACKOFFICE_SMOKE_PASSWORD','Process')
$adminPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process')
if([string]::IsNullOrWhiteSpace($password)){throw 'CPF_BACKOFFICE_SMOKE_PASSWORD is required in process environment.'}
if([string]::IsNullOrWhiteSpace($adminPassword)){throw 'CPF_ADMIN_PASSWORD is required in process environment.'}
if($password.Length -lt 14 -or $password -match [regex]::Escape($LoginId)){throw 'Backoffice verifier password does not satisfy bootstrap policy.'}
$dbResult=Get-Content -LiteralPath (Resolve-Path -LiteralPath $RuntimeDbResultPath).Path -Raw -Encoding UTF8|ConvertFrom-Json
$backofficeDatabase=[string]$dbResult.backofficeDatabase
if($backofficeDatabase -notmatch "^cpf_verify_${VerifierRunId}_mbw$"){throw "Refusing Backoffice bootstrap fixture for database=$backofficeDatabase"}
$secretDir=[IO.Path]::GetFullPath($SecretDirectory);[IO.Directory]::CreateDirectory($secretDir)|Out-Null
$resultFull=[IO.Path]::GetFullPath($ResultPath);[IO.Directory]::CreateDirectory((Split-Path -Parent $resultFull))|Out-Null
$token="cpf-mbw-$VerifierRunId-$([guid]::NewGuid().ToString('N'))"
$scope="CPF-FULLLOCAL-$VerifierRunId"
$operationId="MBW-BOOTSTRAP-$VerifierRunId"
$instanceId="cpf-local-$VerifierRunId"
function Get-Sha256([string]$Value){
    $sha=[Security.Cryptography.SHA256]::Create();try{return [Convert]::ToHexString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($Value))).ToLowerInvariant()}finally{$sha.Dispose()}
}
$tokenHash=Get-Sha256 $token
$fingerprint=Get-Sha256 ("$EnvironmentCode|$scope|$ActiveProfiles")
$tokenPath=Join-Path $secretDir "backoffice-bootstrap-token-$VerifierRunId.txt"
$passwordPath=Join-Path $secretDir "backoffice-bootstrap-password-$VerifierRunId.txt"
[IO.File]::WriteAllText($tokenPath,$token,[Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText($passwordPath,$password,[Text.UTF8Encoding]::new($false))
function Protect-SecretFile([string]$Path){
    if($IsWindows){
        $acl=Get-Acl -LiteralPath $Path
        $owner=[Security.Principal.NTAccount]::new($acl.Owner)
        $acl.SetAccessRuleProtection($true,$false)
        foreach($rule in @($acl.Access)){[void]$acl.RemoveAccessRuleAll($rule)}
        $rights=[Security.AccessControl.FileSystemRights]::FullControl
        $inherit=[Security.AccessControl.InheritanceFlags]::None
        $prop=[Security.AccessControl.PropagationFlags]::None
        $allow=[Security.AccessControl.AccessControlType]::Allow
        $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new($owner,$rights,$inherit,$prop,$allow))
        Set-Acl -LiteralPath $Path -AclObject $acl
    }else{
        & chmod 600 -- $Path
        if($LASTEXITCODE -ne 0){throw "chmod failed for secret file: $Path"}
    }
}
Protect-SecretFile $tokenPath;Protect-SecretFile $passwordPath
$old=$env:MYSQL_PWD;$env:MYSQL_PWD=$adminPassword
try{
    $sql=@"
DELETE FROM MBW_BOOTSTRAP_APPROVAL WHERE TOKEN_HASH='$tokenHash' OR OPERATION_ID='$operationId';
INSERT INTO MBW_BOOTSTRAP_APPROVAL (
 TOKEN_HASH, ENV_FINGERPRINT, STATUS, OPERATION_ID, EXPIRES_AT,
 CLEANUP_STATUS, REQUESTED_BY, APPROVED_BY, APPROVAL_REASON, CREATED_AT, UPDATED_AT
) VALUES (
 '$tokenHash', '$fingerprint', 'APPROVED', NULL, DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 15 MINUTE),
 'PENDING', 'CPF_FULLLOCAL_REQUESTER', 'CPF_FULLLOCAL_APPROVER', 'Verifier-owned isolated Backoffice browser runtime', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);
"@
    $sqlFile=Join-Path $secretDir "backoffice-bootstrap-approval-$VerifierRunId.sql"
    [IO.File]::WriteAllText($sqlFile,$sql,[Text.UTF8Encoding]::new($false))
    try{
        # Root secret is already present inside the verifier-owned MariaDB container as MARIADB_ROOT_PASSWORD.
        # Do not place it in the host process command line or Evidence.
        $sql | & docker exec -i cpf-mariadb sh -lc 'MYSQL_PWD="$MARIADB_ROOT_PASSWORD" exec mariadb --protocol=tcp -h 127.0.0.1 -P 3306 -u root "$1"' sh $backofficeDatabase
        if($LASTEXITCODE -ne 0){throw "Backoffice bootstrap approval insert failed exit=$LASTEXITCODE"}
    }finally{
        if(Test-Path -LiteralPath $sqlFile){Remove-Item -LiteralPath $sqlFile -Force}
    }
}finally{$env:MYSQL_PWD=$old}
$result=[ordered]@{
 schemaVersion=1;status='PASS';sanitized=$true;runId=$VerifierRunId;loginId=$LoginId;operatorName=$OperatorName;roleCode=$RoleCode;
 environmentCode=$EnvironmentCode;activeProfiles=$ActiveProfiles;approvalScope=$scope;operationId=$operationId;instanceId=$instanceId;
 tokenFile=$tokenPath;passwordFile=$passwordPath;database=$backofficeDatabase
}
[IO.File]::WriteAllText($resultFull,($result|ConvertTo-Json -Depth 10)+"`n",[Text.UTF8Encoding]::new($false))
Write-Host "CPF local Backoffice bootstrap fixture PASS runId=$VerifierRunId database=$backofficeDatabase loginId=$LoginId"
