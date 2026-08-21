# SELF:SELF-QA-001 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-001`
- Severity: `P0`
- Category: `Runtime / Concurrency`
- Title: Runtime Instance 최초 동시 등록에서 active fencing을 우회할 수 있는 race
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `계약/회귀 검증 완료; 전체 실환경은 STD-QA-011에서 별도 관리`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

(1) same-host implicit hostname process A 등록 PASS, (2) lease 살아있는 상태에서 process B 동시/순차 등록 모두 FenceException 및 READY 실패, (3) MBR01/MBR02 explicit ID는 모두 PASS, (4) lease expiry 후 다른 process takeover는 fencing token 증가 후 PASS, (5) state/service row partial failure와 process-kill 복구 검증, (6) MariaDB/PostgreSQL/Oracle 동시성 test PASS.

## Current source / consumer scope

cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfRuntimeControlPlaneRepository.java:482-543,1020-1085

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
