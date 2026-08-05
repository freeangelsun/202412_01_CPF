# No repository deletion targets. Remove only this session workspace after integration and QA archive.
$target = 'cpf-docs\work\current\CPF_DEVGPT_CONTROL_V9\_session_workspace\S01-20260805\REV-001\sessions\DEVGPT-V9-S01'
if (Test-Path -LiteralPath $target) { Write-Host "Cleanup target retained until QA: $target" }
