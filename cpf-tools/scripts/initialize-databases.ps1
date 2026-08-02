param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [ValidateSet("platform", "generated", "all")]
    [string] $Scope = "all",
    [switch] $All,
    [string[]] $DomainName = @(),
    [string[]] $SystemCode = @(),
    [string[]] $ModuleName = @(),
    [ValidateSet("profile", "product", "none", "all")]
    [string] $SeedMode = "profile",
    [ValidateSet("bootstrap", "migration", "verify")]
    [string] $GeneratedOperation = "bootstrap",
    [switch] $Apply
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path

$platform = Join-Path $Root "cpf-tools/scripts/initialize-cpf-database.ps1"
$generated = Join-Path $Root "cpf-tools/scripts/initialize-generated-domain-databases.ps1"

if ($Scope -in @("platform", "all")) {
    $args = @("-NoProfile", "-File", $platform, "-Root", $Root, "-SeedMode", $SeedMode)
    if ($All -or ($Scope -eq "all" -and $DomainName.Count -eq 0 -and $SystemCode.Count -eq 0 -and $ModuleName.Count -eq 0)) {
        $args += "-All"
    } else {
        if ($DomainName.Count -gt 0) { $args += "-DomainName"; $args += $DomainName }
        if ($SystemCode.Count -gt 0) { $args += "-SystemCode"; $args += $SystemCode }
        if ($ModuleName.Count -gt 0) { $args += "-ModuleName"; $args += $ModuleName }
    }
    if ($Apply) { $args += "-RequireRun" }

    Write-Host "=== CPF Platform DB ==="
    & pwsh @args
    if ($LASTEXITCODE -ne 0) { throw "CPF Platform DB 작업이 실패했습니다." }
}

if ($Scope -in @("generated", "all")) {
    $args = @(
        "-NoProfile",
        "-File", $generated,
        "-Root", $Root,
        "-Operation", $GeneratedOperation
    )
    if ($All -or ($Scope -eq "all" -and $DomainName.Count -eq 0 -and $SystemCode.Count -eq 0)) {
        $args += "-All"
    } else {
        if ($DomainName.Count -gt 0) { $args += "-DomainName"; $args += $DomainName }
        if ($SystemCode.Count -gt 0) { $args += "-SystemCode"; $args += $SystemCode }
    }
    if ($Apply) { $args += "-Apply" }

    Write-Host "=== Generated Domain DB ==="
    & pwsh @args
    if ($LASTEXITCODE -ne 0) { throw "Generated Domain DB 작업이 실패했습니다." }
}
