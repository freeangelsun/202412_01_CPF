# USER APPROVAL REQUIRED.
# Run only after V9 validation and all active V8-based Development GPT work is integrated and handed to QA.
$paths=@("cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8","cpf-tools/scripts/development-management","cpf-tools/scripts/tests/development_management","cpf-docs/work/current/development-session-results"); $paths | ForEach-Object { if (Test-Path -LiteralPath $_) { Remove-Item -LiteralPath $_ -Recurse -Force } }
