# SELF:SELF-QA-002 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-002`
- Severity: `P0`
- Category: `Batch / Retention / Concurrency`
- Title: Run Pause expectedVersion 검증과 상태 변경이 분리된 TOCTOU
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

policy version N에서 pause 요청 시작 후 N+1 변경 interleaving 시 pause 실패/409, run pause_requested_yn은 변경되지 않고 audit도 실패 상태를 남김. 정상 N 요청은 성공. 동시 두 pause/resume 명령 중 허용된 하나만 성공.

## Current source / consumer scope

cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatRetentionExecutionService.java:66-72,131-136; cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatRetentionExecutionRepository.java:85-88

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
