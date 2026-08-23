# Generated Customer Domain의 물리 식별은 Generated Project 내부 manifest가 아니라 Framework canonical definition을 사용합니다.
function Get-CpfPythonCommand {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($null -ne $python) { return @($python.Source) }
    $py = Get-Command py -ErrorAction SilentlyContinue
    if ($null -ne $py) { return @($py.Source, '-3') }
    throw 'Python 3 실행파일을 찾을 수 없습니다.'
}

function Get-CpfGeneratedDomainInventory {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [string] $DomainName = '',
        [string] $DefinitionPath = '',
        [switch] $IncludeMissing
    )
    if (-not [string]::IsNullOrWhiteSpace($DomainName) -and
            -not [string]::IsNullOrWhiteSpace($DefinitionPath)) {
        throw 'DomainName과 DefinitionPath는 동시에 지정할 수 없습니다.'
    }
    $tool = Join-Path $Root 'cpf-tools/generator/tools/generated_domain_inventory.py'
    if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) {
        throw "Generated Domain canonical inventory tool이 없습니다: $tool"
    }
    $python = @(Get-CpfPythonCommand)
    $arguments = @()
    if ($python.Count -gt 1) { $arguments += $python[1..($python.Count - 1)] }
    $arguments += @($tool, '--root', $Root)
    if (-not [string]::IsNullOrWhiteSpace($DomainName)) {
        $arguments += @('--domain', $DomainName)
    }
    if (-not [string]::IsNullOrWhiteSpace($DefinitionPath)) {
        if (-not [IO.Path]::IsPathRooted($DefinitionPath)) {
            $DefinitionPath = Join-Path $Root $DefinitionPath
        }
        $arguments += @('--file', ([IO.Path]::GetFullPath($DefinitionPath)))
    }
    if ($IncludeMissing) { $arguments += '--include-missing' }
    $previousPythonUtf8 = $env:PYTHONUTF8
    try {
        $env:PYTHONUTF8 = '1'
        $json = & $python[0] @arguments
        $exitCode = $LASTEXITCODE
    } finally {
        $env:PYTHONUTF8 = $previousPythonUtf8
    }
    if ($exitCode -ne 0) { throw 'Generated Domain canonical inventory 실행에 실패했습니다.' }
    $parsed = ($json -join [Environment]::NewLine) | ConvertFrom-Json -ErrorAction Stop
    return @($parsed.domains)
}

function Get-CpfGeneratedDomainDefinition {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [Parameter(Mandatory = $true)][string] $DomainName,
        [string] $DefinitionPath = '',
        [switch] $IncludeMissing
    )
    $arguments = @{ Root = $Root; IncludeMissing = $IncludeMissing }
    if ([string]::IsNullOrWhiteSpace($DefinitionPath)) {
        $arguments.DomainName = $DomainName
    } else {
        $arguments.DefinitionPath = $DefinitionPath
    }
    $rows = @(Get-CpfGeneratedDomainInventory @arguments)
    $normalized = $DomainName.Trim().ToLowerInvariant()
    $matches = @($rows | Where-Object { ([string]$_.domainName).ToLowerInvariant() -eq $normalized })
    if ($matches.Count -ne 1) {
        throw "Generated Domain canonical definition을 정확히 하나 찾지 못했습니다: domain=$normalized count=$($matches.Count)"
    }
    return $matches[0]
}

function Invoke-CpfCanonicalCli {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [Parameter(Mandatory = $true)][string[]] $Arguments
    )
    $cli = Join-Path $Root 'cpf-tools/runtime/cli/cpf.py'
    if (-not (Test-Path -LiteralPath $cli -PathType Leaf)) {
        throw "CPF canonical CLI가 없습니다: $cli"
    }
    $python = @(Get-CpfPythonCommand)
    $processArguments = @()
    if ($python.Count -gt 1) { $processArguments += $python[1..($python.Count - 1)] }
    $processArguments += @($cli, '--root', $Root)
    $processArguments += $Arguments
    $oldPreference = $ErrorActionPreference
    $previousPythonUtf8 = $env:PYTHONUTF8
    try {
        $env:PYTHONUTF8 = '1'
        $ErrorActionPreference = 'Continue'
        $output = @(& $python[0] @processArguments 2>&1 | ForEach-Object { $_.ToString() })
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldPreference
        $env:PYTHONUTF8 = $previousPythonUtf8
    }
    if ($exitCode -ne 0) {
        throw "CPF canonical CLI 실패: exitCode=$exitCode output=$($output -join ' ')"
    }
    $jsonStart = -1
    for ($index = 0; $index -lt $output.Count; $index++) {
        $candidate = $output[$index].TrimStart()
        if ($candidate.StartsWith('{')) {
            $jsonStart = $index
            break
        }
    }
    if ($jsonStart -lt 0) {
        throw "CPF canonical CLI 결과에 JSON document가 없습니다: $($output -join ' ')"
    }
    $json = @($output[$jsonStart..($output.Count - 1)]) -join [Environment]::NewLine
    try {
        return $json | ConvertFrom-Json -Depth 100 -ErrorAction Stop
    } catch {
        throw "CPF canonical CLI 결과가 JSON이 아닙니다: $json"
    }
}
