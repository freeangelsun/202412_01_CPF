param([string]$Root='.',[switch]$Release)
$ErrorActionPreference='Stop';Set-Location $Root
python cpf-tools/scripts/verify-cpf-qa32-primary-engines.py --root . --json-report cpf-docs/evidence/current/qa32-static-primary-engines.json; if($LASTEXITCODE){exit $LASTEXITCODE}
python cpf-tools/scripts/verify-cpf-qa32-repository-security.py --root . --json-report cpf-docs/evidence/current/qa32-static-security.json; if($LASTEXITCODE){exit $LASTEXITCODE}
python cpf-tools/scripts/verify-cpf-supply-chain.py --root .; if($LASTEXITCODE){exit $LASTEXITCODE}
python cpf-tools/scripts/verify-cpf-qa32-generator.py --root .; if($LASTEXITCODE){exit $LASTEXITCODE}
python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . $(if($Release){'--release'}else{''}); exit $LASTEXITCODE
