param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $ApplyGeneratedDomains
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

Invoke-CpfDatabaseArtifactStep "generate-migration-checksums.ps1" "DB migration checksum generation failed."
Invoke-CpfDatabaseArtifactStep "build-all-install-sql.ps1" "DB bundle generation failed."
Invoke-CpfDatabaseArtifactStep "generate-database-schema-manifest.ps1" "DB schema manifest generation failed."
Invoke-CpfDatabaseArtifactStep "check-database-schema-drift.ps1" "DB schema drift check failed."
Invoke-CpfDatabaseArtifactStep "check-database-profile-standard.ps1" "DB profile/generated-domain standard check failed."

# 중앙 domain-template/Runtime SQL 변경이 기존 Generated Domain에 반영되지 않은 채
# Platform bundle만 최신화되는 것을 금지합니다. 기본은 drift 검출, 명시적 switch에서만 적용합니다.
$generatedSync = Join-Path $PSScriptRoot "sync-generated-domain-artifacts.ps1"
if (Test-Path -LiteralPath $generatedSync -PathType Leaf) {
    $args = @("-NoProfile","-ExecutionPolicy","Bypass","-File",$generatedSync,"-Root",$Root,"-Scope","Database")
    if ($ApplyGeneratedDomains) { $args += "-Apply" }
    & pwsh @args
    if ($LASTEXITCODE -ne 0) {
        $mode = if ($ApplyGeneratedDomains) { "apply" } else { "check" }
        throw "Generated Domain DB artifact $mode failed. exitCode=$LASTEXITCODE"
    }
}

Write-Host "CPF DB artifacts synchronized. Canonical vendor source -> lifecycle pack -> schema manifest parity PASS."
