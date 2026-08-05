# USER APPROVAL REQUIRED. Run only after V8 validator PASS and reference scan confirms zero active references.
if (Test-Path -LiteralPath "cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1") { Remove-Item -LiteralPath "cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1" -Recurse -Force }
