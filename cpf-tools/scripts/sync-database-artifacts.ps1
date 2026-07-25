param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-CpfDatabaseArtifactStep {
    param(
        [Parameter(Mandatory = $true)][string] $ScriptName,
        [Parameter(Mandatory = $true)][string] $FailureMessage
    )

    $scriptPath = Join-Path $PSScriptRoot $ScriptName
    if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) {
        throw "DB artifact step script is missing: $scriptPath"
    }

    # .ps1을 현재 PowerShell scope에서 & 로 호출한 뒤 $LASTEXITCODE를 읽으면
    # 하위 script 내부에서 마지막으로 실행한 native command의 과거 exit code를
    # 부모 script의 성공/실패로 오판할 수 있다. 각 gate를 별도 pwsh process로
    # 실행하여 $LASTEXITCODE가 해당 gate process의 실제 종료 코드만 나타내게 한다.
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $scriptPath -Root $Root
    if ($LASTEXITCODE -ne 0) {
        throw "$FailureMessage exitCode=$LASTEXITCODE script=$ScriptName"
    }
}

Invoke-CpfDatabaseArtifactStep "build-all-install-sql.ps1" "DB bundle generation failed."
Invoke-CpfDatabaseArtifactStep "generate-database-schema-manifest.ps1" "DB schema manifest generation failed."
Invoke-CpfDatabaseArtifactStep "check-database-schema-drift.ps1" "DB schema drift check failed."
Invoke-CpfDatabaseArtifactStep "check-database-profile-standard.ps1" "DB profile/generated-domain standard check failed."

Write-Host "CPF DB artifacts synchronized. Canonical vendor source -> lifecycle pack -> schema manifest parity PASS."
