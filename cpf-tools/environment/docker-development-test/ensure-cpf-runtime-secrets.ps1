[CmdletBinding()]
param(
    [string] $SecretFile = (Join-Path $env:SystemDrive 'dev\Docker\Secrets\cpf-runtime.env')
)

<#
.SYNOPSIS
CPF Local Docker Secret env(`cpf-runtime.env`)가 요구하는 필수 Key 중 누락된 것을 자동으로 채운다.

.설명
`CPF_도커_개발테스트환경_전체설치.ps1`은 최초 설치 시 `CPF_ADMIN_PASSWORD` 하나만 기록하지만,
`cpf-tools/db/config/database-install.default.json`의 각 Module Runtime/Migration/Admin 비밀번호는
`CPF_DB_APP_PASSWORD`/`CPF_DB_ROOT_PASSWORD`/`CPF_DB_MIGRATION_PASSWORD`(fallbackEnv)를 필요로 한다.
이 Local 개발 환경은 단일 관리자 공통 비밀번호(`CPF_ADMIN_PASSWORD`)를 모든 Vendor Container의 Root
Credential로 이미 사용하므로(compose.yml `ORACLE_PWD: ${CPF_ADMIN_PASSWORD}` 등과 동일 계약), 누락된
DB fallback Key는 같은 값을 재사용해 채운다. 실제 Secret 값은 이 스크립트 실행 결과에 절대 출력하지
않으며, Key별 "이미 있었는지/새로 추가했는지" 존재 여부만 보고한다.

이 스크립트는 멱등(idempotent)이다 — 이미 있는 Key는 절대 덮어쓰거나 중복 기록하지 않는다.
#>

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

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# fallbackEnv Key -> 재사용할 원천 Key. 원천 Key가 파일에 없으면 해당 fallback Key는 채울 수 없다고 보고한다.
$requiredFallbackKeys = [ordered]@{
    CPF_DB_ROOT_PASSWORD      = 'CPF_ADMIN_PASSWORD'
    CPF_DB_APP_PASSWORD       = 'CPF_ADMIN_PASSWORD'
    CPF_DB_MIGRATION_PASSWORD = 'CPF_ADMIN_PASSWORD'
}

if (-not (Test-Path -LiteralPath $SecretFile -PathType Leaf)) {
    throw "CPF Docker Secret env가 없습니다: $SecretFile (먼저 CPF_도커_개발테스트환경_전체설치.ps1로 최초 설치하세요.)"
}

function Get-CpfSecretKeys {
    param([Parameter(Mandatory)][string] $Path)
    $keys = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($line in [IO.File]::ReadAllLines($Path, [Text.UTF8Encoding]::new($false))) {
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=') { [void]$keys.Add($Matches[1]) }
    }
    return $keys
}

function Get-CpfSecretValue {
    param([Parameter(Mandatory)][string] $Path, [Parameter(Mandatory)][string] $Name)
    foreach ($line in [IO.File]::ReadAllLines($Path, [Text.UTF8Encoding]::new($false))) {
        if ($line -match "^\s*$([regex]::Escape($Name))=(.*)$") { return $Matches[1] }
    }
    return $null
}

$existingKeys = Get-CpfSecretKeys -Path $SecretFile
$report = [ordered]@{}
$toAppend = [Collections.Generic.List[string]]::new()

foreach ($key in $requiredFallbackKeys.Keys) {
    if ($existingKeys.Contains($key)) {
        $report[$key] = 'ALREADY_PRESENT'
        continue
    }
    $sourceKey = $requiredFallbackKeys[$key]
    if (-not $existingKeys.Contains($sourceKey)) {
        $report[$key] = "MISSING_AND_NO_SOURCE($sourceKey)"
        continue
    }
    $sourceValue = Get-CpfSecretValue -Path $SecretFile -Name $sourceKey
    if ([string]::IsNullOrEmpty($sourceValue)) {
        $report[$key] = "MISSING_AND_SOURCE_EMPTY($sourceKey)"
        continue
    }
    $toAppend.Add("$key=$sourceValue")
    $report[$key] = "PROVISIONED_FROM($sourceKey)"
}

if ($toAppend.Count -gt 0) {
    $existingBytes = [IO.File]::ReadAllBytes($SecretFile)
    $needsNewline = $existingBytes.Length -gt 0 -and $existingBytes[$existingBytes.Length - 1] -ne 10
    $suffix = ($(if ($needsNewline) { "`n" } else { '' })) + (($toAppend -join "`n") + "`n")
    [IO.File]::AppendAllText($SecretFile, $suffix, [Text.UTF8Encoding]::new($false))
}

# 실제 값은 출력하지 않는다 — Key별 존재/조치 결과만 보고한다.
[pscustomobject]@{
    secretFile = $SecretFile
    keys       = $report
    appended   = $toAppend.Count
} | ConvertTo-Json -Depth 3
