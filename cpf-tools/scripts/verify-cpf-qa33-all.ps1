param(
    [string]$Root = '.',
    [switch]$Release,
    [switch]$SkipExternalTools
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$originalLocation = Get-Location
try {
    Set-Location (Resolve-Path $Root)

    python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
    if ($LASTEXITCODE -ne 0) { throw "QA33 Request Integrity gate failed (exit=$LASTEXITCODE)" }

    python cpf-tools/scripts/verify-cpf-qa33-source-integrity.py --root . --json-report cpf-docs/evidence/current/qa33-source-integrity.sanitized.json
    if ($LASTEXITCODE -ne 0) { throw "QA33 Source Integrity gate failed (exit=$LASTEXITCODE)" }

    python cpf-tools/scripts/verify-cpf-qa33-batch-control-plane.py --root . --json-report cpf-docs/evidence/current/qa33-batch-control-plane.sanitized.json
    if ($LASTEXITCODE -ne 0) { throw "QA33 Batch Control Plane gate failed (exit=$LASTEXITCODE)" }

    python cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py --root . --json-report cpf-docs/evidence/current/qa33-db-vendor-parity.sanitized.json
    if ($LASTEXITCODE -ne 0) { throw "QA33 DB Vendor semantic parity gate failed (exit=$LASTEXITCODE)" }

    python cpf-tools/scripts/verify-cpf-qa33-repository-closure.py --root . --json-report cpf-docs/evidence/current/qa33-repository-closure.sanitized.json
    if ($LASTEXITCODE -ne 0) { throw "QA33 Repository Closure gate failed (exit=$LASTEXITCODE)" }

    python cpf-tools/scripts/verify-cpf-qa33-frontend-closure.py --root . --json-report cpf-docs/evidence/current/qa33-frontend-closure.sanitized.json
    if ($LASTEXITCODE -ne 0) { throw "QA33 Frontend Closure gate failed (exit=$LASTEXITCODE)" }

    & pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa32-all.ps1 -Root . -SkipExternalTools:$SkipExternalTools
    if ($LASTEXITCODE -ne 0) { throw "QA32 regression gate failed (exit=$LASTEXITCODE)" }

    if ($Release) {
        & pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa33-runtime.ps1 -Root . -SkipExternalTools:$SkipExternalTools
        if ($LASTEXITCODE -ne 0) { throw "QA33 runtime gate failed (exit=$LASTEXITCODE)" }
        python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . --release --json-report cpf-docs/evidence/current/qa32-completion-gate.sanitized.json
        if ($LASTEXITCODE -ne 0) { throw "QA32 completion release gate failed (exit=$LASTEXITCODE)" }
        python cpf-tools/scripts/verify-cpf-qa33-result-coverage-v2.py --root . --release --json-report cpf-docs/evidence/current/qa33-result-coverage.sanitized.json
    }
    else {
        python cpf-tools/scripts/verify-cpf-qa33-result-coverage-v2.py --root . --json-report cpf-docs/evidence/current/qa33-result-coverage.sanitized.json
    }
    if ($LASTEXITCODE -ne 0) { throw "QA33 Result Coverage gate failed (exit=$LASTEXITCODE)" }
}
finally {
    Set-Location $originalLocation
}
