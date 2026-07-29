param([string]$ExpectedSha='b8941577b99535ff3e64a4fad99b74bafa544227')
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path

python (Join-Path $root 'cpf-tools\verification\20260729_04\check_final_source_closure.py') $root
if ($LASTEXITCODE -ne 0) { throw "Final source closure failed. exit=$LASTEXITCODE" }
python (Join-Path $root 'cpf-tools\verification\20260729_04\check_generator_idempotency_templates.py') $root
if ($LASTEXITCODE -ne 0) { throw "Generator idempotency template failed. exit=$LASTEXITCODE" }
python (Join-Path $root 'cpf-tools\verification\20260729_04\check_generator_java_template_compile.py') $root
if ($LASTEXITCODE -ne 0) { throw "Generator Java template compile failed. exit=$LASTEXITCODE" }
node (Join-Path $root 'cpf-tools\verification\20260729_04\check_frontend_syntax.cjs') $root
if ($LASTEXITCODE -ne 0) { throw "Frontend syntax failed. exit=$LASTEXITCODE" }

& (Join-Path $PSScriptRoot 'check-enterprise-source-closure.ps1') -ProjectRoot $root
& (Join-Path $PSScriptRoot 'check-direct-client-boundary.ps1') -ProjectRoot $root
& (Join-Path $PSScriptRoot 'check-semantic-consumer-graph.ps1') -ProjectRoot $root
& (Join-Path $PSScriptRoot 'check-evidence-contract.ps1') -ProjectRoot $root
& (Join-Path $PSScriptRoot 'check-work-context-sha.ps1') -ProjectRoot $root -ExpectedSha $ExpectedSha
& (Join-Path $PSScriptRoot 'check-local-runtime-topology.ps1') -Root $root
Write-Host '[PASS] CPF 20260729 final overlay static verification'
