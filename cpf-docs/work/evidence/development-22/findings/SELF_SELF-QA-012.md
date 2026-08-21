# SELF:SELF-QA-012 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-012`
- Severity: `P1`
- Category: `Generated Domain / Verification Contract`
- Title: Generator 관련 verifier/최종 completion이 서로 다른 metadata 정본 정책을 검사
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

동일 fixture에 대해 모든 generator verifier verdict 일치. batch/dependency/externalClients feature 선택만 normalized allowed delta.

## Current source / consumer scope

cpf-tools/verification/tools/verify-cpf-final-completion.ps1:68-130; cpf-tools/generator/verification/check-generated-domain-parity.ps1; cpf-tools/verification/nxt3/verify_generator_cross_platform.py

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
