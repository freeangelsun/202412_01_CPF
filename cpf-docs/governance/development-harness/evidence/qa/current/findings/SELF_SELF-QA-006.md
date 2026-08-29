# SELF:SELF-QA-006 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-006`
- Severity: `P0`
- Category: `Final Verification Workflow`
- Title: Overlay-before-commit 정책과 clean-tree/exact-HEAD Final Plan 요구가 충돌하며 Final Plan static PASS가 내부 고장을 놓침
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

Overlay 적용 dirty tree에서 precommit final validation PASS 가능, 예상 외 파일 변경은 FAIL. commit 후 release final plan도 exact SHA에서 PASS. 동일 Source file hash가 두 모드 사이에서 일치.

## Current source / consumer scope

cpf-tools/verification/tools/invoke-cpf-final-verification-plan.py:140-171,236-263; cpf-tools/verification/tools/verify-cpf-final-completion.ps1:36-47

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
