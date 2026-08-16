Set-StrictMode -Version Latest

function Assert-CpfBackupScalar {
    param([Parameter(Mandatory)][string]$Value,[Parameter(Mandatory)][string]$DisplayName)
    if ([string]::IsNullOrWhiteSpace($Value)) { throw "$DisplayName 값이 비어 있습니다." }
    if ($Value -match '[\x00-\x1F\x7F]') { throw "$DisplayName 값에 제어문자를 사용할 수 없습니다." }
    return $Value.Trim()
}

function Assert-CpfBackupIdentifier {
    param([Parameter(Mandatory)][string]$Value,[Parameter(Mandatory)][string]$DisplayName)
    $Value=Assert-CpfBackupScalar $Value $DisplayName
    if($Value -notmatch '^[A-Za-z][A-Za-z0-9_$#.-]{0,126}$'){throw "$DisplayName 값이 안전한 식별자 규칙에 맞지 않습니다: $Value"}
    return $Value
}

function Get-CpfPythonCommand {
    foreach($candidate in @('python','python3')){
        $tool=Get-Command $candidate -ErrorAction SilentlyContinue
        if($tool){return $tool.Source}
    }
    throw 'CPF backup encryption을 실행할 Python 3를 찾을 수 없습니다.'
}

function Get-CpfGitHeadOrUnknown {
    param([Parameter(Mandatory)][string]$RootPath)
    $head=(git -C $RootPath rev-parse HEAD 2>$null)
    if([string]::IsNullOrWhiteSpace([string]$head)){return 'UNKNOWN'}
    return ([string]$head).Trim()
}

function Write-CpfJsonAtomic {
    param([Parameter(Mandatory)]$Value,[Parameter(Mandatory)][string]$Path)
    $directory=Split-Path -Parent $Path
    if(-not [string]::IsNullOrWhiteSpace($directory)){New-Item -ItemType Directory -Force -Path $directory|Out-Null}
    $temporary="$Path.tmp-$PID-$([guid]::NewGuid().ToString('N'))"
    try{
        $Value|ConvertTo-Json -Depth 20|Set-Content -LiteralPath $temporary -Encoding UTF8
        Move-Item -LiteralPath $temporary -Destination $Path -Force
    } finally {
        if(Test-Path -LiteralPath $temporary){Remove-Item -LiteralPath $temporary -Force -ErrorAction SilentlyContinue}
    }
}

function Write-CpfManifestHash {
    param([Parameter(Mandatory)][string]$ManifestPath)
    $hash=(Get-FileHash -LiteralPath $ManifestPath -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath "$ManifestPath.sha256" -Encoding ascii -Value "$hash  $(Split-Path -Leaf $ManifestPath)"
    return $hash
}

function Assert-CpfManifestHash {
    param([Parameter(Mandatory)][string]$ManifestPath)
    $sidecar="$ManifestPath.sha256"
    if(-not (Test-Path -LiteralPath $sidecar -PathType Leaf)){throw "backup manifest hash sidecar가 없습니다: $sidecar"}
    $expected=((Get-Content -LiteralPath $sidecar -Raw -Encoding ascii).Trim() -split '\s+')[0].ToLowerInvariant()
    if($expected -notmatch '^[0-9a-f]{64}$'){throw 'backup manifest hash sidecar 형식이 올바르지 않습니다.'}
    $actual=(Get-FileHash -LiteralPath $ManifestPath -Algorithm SHA256).Hash.ToLowerInvariant()
    if($actual -ne $expected){throw 'backup manifest SHA-256 mismatch'}
    return $actual
}

function Assert-CpfBackupManifest {
    param([Parameter(Mandatory)]$Manifest)
    $required=@('schemaVersion','backupId','status','vendor','database','primaryRegion','artifactFile','artifactSha256','sourceSha256','encrypted','encryptionAlgorithm','startedAt','finishedAt','retentionUntil','legalHold','baseCommit','credentialEmbedded','sanitized')
    foreach($field in $required){if($null -eq $Manifest.PSObject.Properties[$field]){throw "backup manifest required field가 없습니다: $field"}}
    if([int]$Manifest.schemaVersion -ne 1){throw "지원하지 않는 backup manifest version입니다: $($Manifest.schemaVersion)"}
    if([string]$Manifest.status -ne 'COMPLETE'){throw "backup status가 COMPLETE가 아닙니다: $($Manifest.status)"}
    if(-not [bool]$Manifest.encrypted){throw '암호화되지 않은 backup artifact는 복원할 수 없습니다.'}
    if([string]$Manifest.encryptionAlgorithm -ne 'AES-256-GCM-CHUNKED'){throw "지원하지 않는 backup encryption입니다: $($Manifest.encryptionAlgorithm)"}
    if([bool]$Manifest.credentialEmbedded){throw 'credentialEmbedded=true backup은 사용할 수 없습니다.'}
    if(-not [bool]$Manifest.sanitized){throw 'sanitized=false backup manifest는 사용할 수 없습니다.'}
}

function Invoke-CpfBackupCrypto {
    param(
        [Parameter(Mandatory)][ValidateSet('encrypt','decrypt','inspect')][string]$Mode,
        [Parameter(Mandatory)][string]$RootPath,
        [Parameter(Mandatory)][string]$InputPath,
        [string]$OutputPath,
        [string]$KeyEnvironmentVariable='CPF_BACKUP_ENCRYPTION_KEY_B64'
    )
    $python=Get-CpfPythonCommand
    $script=Join-Path $RootPath 'cpf-tools/db/tools/cpf-backup-crypto.py'
    if(-not (Test-Path -LiteralPath $script -PathType Leaf)){throw "CPF backup crypto helper가 없습니다: $script"}
    $args=@($script,$Mode,'--input',$InputPath)
    if($Mode -ne 'inspect'){$args+=@('--output',$OutputPath,'--key-env',$KeyEnvironmentVariable)}
    $json=& $python @args 2>&1
    $exit=$LASTEXITCODE
    if($exit -ne 0){throw "CPF backup crypto failed: exit=$exit"}
    try{return (($json -join "`n")|ConvertFrom-Json -Depth 20)}catch{throw 'CPF backup crypto 결과 JSON을 해석할 수 없습니다.'}
}


function Get-CpfObjectSha256 {
    param([Parameter(Mandatory)]$Value)
    $json=$Value|ConvertTo-Json -Depth 30 -Compress
    $bytes=[Text.Encoding]::UTF8.GetBytes($json)
    $algorithm=[Security.Cryptography.SHA256]::Create()
    try{return ([BitConverter]::ToString($algorithm.ComputeHash($bytes))).Replace('-','').ToLowerInvariant()}finally{$algorithm.Dispose()}
}

function New-CpfBackupAuditEvidence {
    param(
        [Parameter(Mandatory)][string]$RootPath,
        [Parameter(Mandatory)][string]$EvidenceDirectory,
        [Parameter(Mandatory)][string]$Operation,
        [Parameter(Mandatory)][string]$Status,
        [Parameter(Mandatory)][string]$BackupId,
        [Parameter(Mandatory)][string]$Vendor,
        [Parameter(Mandatory)][string]$Database,
        [Parameter(Mandatory)][string]$Operator,
        [Parameter(Mandatory)][string]$Reason,
        [string]$ApprovalReference='',
        [int]$ExitCode=0,
        [string]$FailureCode='',
        [bool]$ReconcileRequired=$false,
        [string]$PlanSha256='',
        [string[]]$AffectedFiles=@()
    )
    $out=Join-Path $RootPath $EvidenceDirectory
    New-Item -ItemType Directory -Force -Path $out|Out-Null
    $now=(Get-Date).ToUniversalTime()
    $record=[ordered]@{
        schemaVersion=1; operation=$Operation; status=$Status; backupId=$BackupId; vendor=$Vendor; database=$Database
        operator=$Operator; reason=$Reason; approvalReference=$ApprovalReference; occurredAt=$now.ToString('o')
        sourceSha=(Get-CpfGitHeadOrUnknown $RootPath); exitCode=$ExitCode; failureCode=$FailureCode
        reconcileRequired=$ReconcileRequired; planSha256=$PlanSha256; affectedFiles=@($AffectedFiles); sanitized=$true
    }
    $path=Join-Path $out ("backup-audit-{0}-{1}.json" -f $now.ToString('yyyyMMddTHHmmssfffZ'),([guid]::NewGuid().ToString('N').Substring(0,8)))
    Write-CpfJsonAtomic $record $path
    return $path
}
