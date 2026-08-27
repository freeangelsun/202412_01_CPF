param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $ApplyGeneratedDomains
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-CpfDatabaseArtifactStep {
    param(
        [Parameter(Mandatory = $true)][string] $ScriptPath,
        [Parameter(Mandatory = $true)][string] $FailureMessage,
        [string[]] $ExtraArgs = @()
    )

    $absoluteScriptPath = Join-Path $Root $ScriptPath
    if (-not (Test-Path -LiteralPath $absoluteScriptPath -PathType Leaf)) {
        throw "DB artifact step script is missing: $absoluteScriptPath"
    }

    # .ps1을 현재 PowerShell scope에서 & 로 호출한 뒤 $LASTEXITCODE를 읽으면
    # 하위 script 내부에서 마지막으로 실행한 native command의 과거 exit code를
    # 부모 script의 성공/실패로 오판할 수 있다. 각 gate를 별도 pwsh process로
    # 실행하여 $LASTEXITCODE가 해당 gate process의 실제 종료 코드만 나타내게 한다.
    & pwsh -NoProfile -File $absoluteScriptPath -Root $Root @ExtraArgs
    if ($LASTEXITCODE -ne 0) {
        throw "$FailureMessage exitCode=$LASTEXITCODE script=$ScriptPath"
    }
}

function Invoke-CpfPythonArtifactStep {
    param(
        [Parameter(Mandatory = $true)][string] $ScriptPath,
        [Parameter(Mandatory = $true)][string] $FailureMessage,
        [string[]] $ExtraArgs = @()
    )

    $absoluteScriptPath = Join-Path $Root $ScriptPath
    if (-not (Test-Path -LiteralPath $absoluteScriptPath -PathType Leaf)) {
        throw "DB artifact Python step script is missing: $absoluteScriptPath"
    }

    # Artifact synchronization must not materialize import bytecode inside the
    # developer-facing Source tree (for example render_vendor_pack.__pycache__).
    & python -B $absoluteScriptPath --root $Root @ExtraArgs
    if ($LASTEXITCODE -ne 0) {
        throw "$FailureMessage exitCode=$LASTEXITCODE script=$ScriptPath"
    }
}

# Canonical JSON must be projected before any aggregate/lifecycle bundle is
# assembled. The canonical seed synchronizer owns numbered seed inputs and
# lifecycle bundles, while the current renderer owns generated/current. Omitting
# either projection can leave an apparently successful FreshInstall input one
# generation behind its canonical JSON.
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/generator/generate-official-db-vendor-source.ps1" "Official DB vendor canonical source generation failed."
Invoke-CpfPythonArtifactStep "cpf-tools/db/tools/sync-canonical-seed-bundles.py" "Canonical DB seed source/bundle synchronization failed."
Invoke-CpfPythonArtifactStep "cpf-tools/db/render_vendor_pack.py" "Canonical DB current snapshot rendering failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/build-official-db-vendor-packs.ps1" "Official DB vendor lifecycle pack generation failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/build-all-install-sql.ps1" "DB bundle generation failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/verification/tools/check-spring-batch-sequence-contract.ps1" "Spring Batch sequence contract validation failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/runtime/tools/sync-platform-runtime-query-packs.ps1" "Platform Runtime Query Pack synchronization failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/runtime/tools/sync-bat-runtime-query-pack.ps1" "BAT Runtime Query Pack synchronization failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/verification/check-query-contract-integrity.ps1" "Runtime Query Contract integrity failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/verification/tools/sync-platform-nullable-empty-string-repair.ps1" "Platform nullable empty-string repair synchronization failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/sync-platform-seed-currentization.ps1" "Platform seed currentization generation failed." @("-Apply")
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/generate-migration-checksums.ps1" "DB migration checksum generation failed." @("-Apply")
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/sync-platform-seed-currentization.ps1" "Platform seed currentization checksum/parity check failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/tools/generate-database-schema-manifest.ps1" "DB schema manifest generation failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/verification/check-database-schema-drift.ps1" "DB schema drift check failed."
Invoke-CpfDatabaseArtifactStep "cpf-tools/db/verification/check-database-profile-standard.ps1" "DB profile/generated-domain standard check failed."

# 중앙 domain-template/Runtime SQL 변경이 기존 Generated Domain에 반영되지 않은 채
# Platform bundle만 최신화되는 것을 금지합니다. 기본은 drift 검출, 명시적 switch에서만 적용합니다.
$generatedSync = Join-Path $Root "cpf-tools/generator/tools/sync-generated-domain-artifacts.ps1"
if (-not (Test-Path -LiteralPath $generatedSync -PathType Leaf)) {
    throw "Generated Domain DB artifact synchronizer is missing: $generatedSync"
}
$args = @("-NoProfile","-File",$generatedSync,"-Root",$Root,"-Scope","Database")
if ($ApplyGeneratedDomains) { $args += "-Apply" }
& pwsh @args
if ($LASTEXITCODE -ne 0) {
    $mode = if ($ApplyGeneratedDomains) { "apply" } else { "check" }
    throw "Generated Domain DB artifact $mode failed. exitCode=$LASTEXITCODE"
}

Write-Host "CPF DB artifacts synchronized. Canonical JSON -> official 3-vendor source -> lifecycle pack -> schema manifest parity PASS."
