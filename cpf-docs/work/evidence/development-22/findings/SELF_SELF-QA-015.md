# SELF:SELF-QA-015 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-015`
- Severity: `P1`
- Category: `Frontend Toolchain`
- Title: Frontend package 계약과 Final Plan Node/npm prerequisite가 불일치
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

Node 22.18~24.x + npm10.9.2 PASS, Node25/22.17/npm11 FAIL. ADM/Backoffice 동일 verdict.

## Current source / consumer scope

cpf-admin/frontend/package.json; cpf-backoffice-web/frontend/package.json; cpf-tools/verification/tools/invoke-cpf-final-verification-plan.py:78-82

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
