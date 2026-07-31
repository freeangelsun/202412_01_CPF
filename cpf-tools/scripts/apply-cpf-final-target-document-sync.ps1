param([string]$Root='.')
$ErrorActionPreference='Stop'
Push-Location $Root
try {
    python cpf-tools/scripts/sync-cpf-final-target-document-references.py --root . --apply
    if ($LASTEXITCODE -ne 0) { throw "document sync apply failed: $LASTEXITCODE" }
    python cpf-tools/scripts/sync-cpf-final-target-document-references.py --root . --check
    if ($LASTEXITCODE -ne 0) { throw "document sync check failed: $LASTEXITCODE" }
    python cpf-tools/scripts/verify-cpf-final-target-document-consistency.py --root .
    if ($LASTEXITCODE -ne 0) { throw "document consistency failed: $LASTEXITCODE" }
    python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
    if ($LASTEXITCODE -ne 0) { throw "QA33 request integrity failed: $LASTEXITCODE" }
} finally {
    Pop-Location
}
