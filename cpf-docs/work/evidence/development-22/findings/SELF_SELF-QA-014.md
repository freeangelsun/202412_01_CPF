# SELF:SELF-QA-014 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-014`
- Severity: `P1`
- Category: `Windows / PowerShell Compatibility`
- Title: Windows PowerShell 5.1 fallback 정책과 active script의 .NET/PowerShell 7 전용 API 사용이 광범위하게 충돌
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

지원 Windows shell에서 모든 public/local/final/db/generator entrypoint smoke PASS. forbidden API scan 0 또는 explicit pwsh7-only allowlist.

## Current source / consumer scope

cpf-tools/verification/tools/run-cpf-final-local-validation.ps1:44-45 및 active *.ps1 다수

## Current verification evidence

- `cpf-docs/work/evidence/development-22/logs/CANONICAL_STATIC_VERIFIERS.json`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_DB.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_GENERATOR.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_RELEASE.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_RUNTIME_SECURITY_VERIFICATION.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_TESTING_TOOLS.log`
- `cpf-docs/work/evidence/development-22/logs/FRONTEND_CONTRACTS.log`
- `cpf-docs/work/evidence/development-22/logs/FRESH_REPLAY_GATES.log`

## QA-source evidence reference

QA package matrix/report 참조
