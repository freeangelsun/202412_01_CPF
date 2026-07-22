param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 90,
    [string] $LogDir = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $BuildBeforeRun
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$permissionResultPath = Join-Path $LogDir "adm-permission-runtime-result.json"
$v15ResultPath = Join-Path $LogDir "v15-adm-api-permission-result.json"

& (Join-Path $Root "scripts/apply-v15-adm-api-permission-management.ps1") `
    -Root $Root `
    -ResultPath $v15ResultPath

& (Join-Path $Root "scripts/smoke-adm-runtime.ps1") `
    -Root $Root `
    -AdmBaseUrl $AdmBaseUrl `
    -StartupTimeoutSeconds $StartupTimeoutSeconds `
    -ShutdownTimeoutSeconds $ShutdownTimeoutSeconds `
    -LogDir $LogDir `
    -AdmUsername $AdmUsername `
    -AdmPassword $AdmPassword `
    -IncludePermissionWriteSmoke `
    -PermissionResultPath $permissionResultPath `
    -BuildBeforeRun:$BuildBeforeRun

Write-Host "ADM permission runtime smoke completed. Result: $permissionResultPath"
