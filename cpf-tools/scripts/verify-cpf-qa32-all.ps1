param(
    [string]$Root = '.',
    [switch]$Release,
    [switch]$SkipExternalTools
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-CpfCommand {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][scriptblock]$Command
    )
    Write-Host "[CPF][QA32] START $Name"
    & $Command
    $code = if ($null -eq $LASTEXITCODE) { 0 } else { $LASTEXITCODE }
    if ($code -ne 0) {
        throw "[CPF][QA32] $Name failed (exit=$code)"
    }
    Write-Host "[CPF][QA32] PASS  $Name"
}

$originalLocation = Get-Location
try {
    Set-Location (Resolve-Path $Root)

    Invoke-CpfCommand 'primary-engine-source-gate' {
        python cpf-tools/scripts/verify-cpf-qa32-primary-engines.py --root . --json-report cpf-docs/evidence/current/qa32-static-primary-engines.sanitized.json
    }
    Invoke-CpfCommand 'repository-security-source-gate' {
        python cpf-tools/scripts/verify-cpf-qa32-repository-security.py --root . --json-report cpf-docs/evidence/current/qa32-static-security.sanitized.json
    }
    Invoke-CpfCommand 'supply-chain-source-gate' {
        python cpf-tools/scripts/verify-cpf-supply-chain.py --root .
    }
    Invoke-CpfCommand 'generator-source-gate' {
        python cpf-tools/scripts/verify-cpf-qa32-generator.py --root .
    }

    if ($Release) {
        Invoke-CpfCommand 'runtime-release-gate' {
            $arguments = @('-NoProfile', '-File', 'cpf-tools/scripts/verify-cpf-qa32-runtime.ps1', '-Root', '.')
            if ($SkipExternalTools) { $arguments += '-SkipExternalTools' }
            & pwsh @arguments
        }
        Invoke-CpfCommand 'completion-release-gate' {
            python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . --release --json-report cpf-docs/evidence/current/qa32-completion-gate.sanitized.json
        }
    }
    else {
        Invoke-CpfCommand 'completion-development-gate' {
            python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . --json-report cpf-docs/evidence/current/qa32-completion-gate.sanitized.json
        }
    }
}
finally {
    Set-Location $originalLocation
}
