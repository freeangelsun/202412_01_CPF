# TEST AND EVIDENCE — QA-12F

## 실행 환경

- Python: `3.13.5`
- Java: `OpenJDK 21.0.10`
- Node: `v22.16.0`
- Docker CLI/실제 3 Vendor DB/Broker/Browser: 사용할 수 없음 또는 실제 목표 환경 미제공
- Baseline: `f97655c1299936a1101bc3ec10239265ec3b502e`

## 실제 실행 결과

| 검증 | 결과 | 핵심 |
|---|---|---|
| Full QA ledger build — 수정 전 | FAIL | Requirement split index Part 010/011 stale metadata 재현 |
| Full QA ledger build — 수정 후 | PASS | Requirement 30,558 / Scenario 40,763 생성 |
| Full ledger validator — 수정 전 | FAIL | canonical `CPF-GATE-00~15` 거부 및 대용량 성능 문제 |
| Full ledger validator — 수정 후 | PASS | Gate ID 허용, scenario ID set 1회 생성 |
| Validator regression test | PASS | Gate ID와 scale 회귀 |
| QA-12F range/count/link validation | PASS | Requirement 2,546, Scenario 4,772, 중복/미연결 0 |
| Resilience contract harness | PASS(부분) | revision, bounded retry, UNKNOWN/reconcile, audit fail-closed |
| Dynamic log runtime harness | PASS(부분) | runtime rule 정상/만료/우선순위; CAS/distribution은 미검증 |
| Durable log spool harness | PASS(부분) | claim/lease/retry/poison; fsync/process-kill은 미검증 |
| File retention cap before/after | FAIL→PASS | same-day cap bypass 제거 |
| File permission before/after | FAIL→PASS | POSIX 750/640 및 world access fail-closed |
| Trace sampling/header harness | PASS(부분) | deterministic sampling/correlation; 실제 exporter/provider fault는 미검증 |
| Masking truncate before/after | FAIL→PASS | 음수/과소 경계 fail-closed |
| Bearer masking before/after | FAIL→PASS | 토큰 본문 잔존 제거 |
| Fixed-Length starter javac before/after | FAIL→PASS | 잘못된 import 수정, Bean 계약 Test |
| DB pack static parity | PASS(정적) | Oracle/PostgreSQL/MariaDB pack shape parity; 실제 lifecycle 미실행 |

## 수행하지 못한 목표 Runtime

- Oracle/PostgreSQL/MariaDB 실제 install→upgrade→rollback→reapply→backup/restore
- Kafka/RabbitMQ/JMS/IBM MQ 실제 ACK/NACK/redelivery/rebalance/backpressure
- Browser E2E, ADM/BZA 실제 권한·접근성·오류상태
- 다중 JVM/다중 인스턴스 lease contention, process kill/restart, fencing
- 실제 Gateway/TCP/SFTP 외부 peer fault/half-open/resume
- Spring Batch 실제 DB+Kafka remote chunk/partition process-kill

위 항목은 Source·contract·독립 Harness로 확인 가능한 범위까지만 판정하고, 차이만 `미검증`으로 남겼다.

## Evidence 파일

- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE.md`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/asynchronous-log-writer-source-reproduction.exit`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/asynchronous-log-writer-source-reproduction.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/build-full-qa-ledgers-after-index-fix.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/build-full-qa-ledgers.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/db-pack-static-parity.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/dynamic-log-runtime-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-permission-before-after.exit`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-permission-before-after.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-permission-javac.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-permission-world-access-reject.exit`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-permission-world-access-reject.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-retention-before-after-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-retention-cap-before-after.exit`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/file-log-retention-cap-before-after.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/fixedlength-autoconfig-before-after-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/log-spool-runtime-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/masking-bearer-before-after-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/masking-policy-runtime-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/masking-truncate-before-after-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/resilience-contract-runtime-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/test_validate_gate_ids.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/test_validate_gate_ids_and_scale.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/trace-header-correlation-harness.exit`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/trace-header-correlation-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/trace-sampling-runtime-harness.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-12way-partition-coverage.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-12way-partition-coverage.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-after-fix.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-after-fix.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-before-fix.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-before-fix.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-fixed-validator.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-builder-output-fixed-validator.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-qa12f-requirement-count.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-qa12f-requirement-count.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-qa12f-scenario-count.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/validate-qa12f-scenario-count.log`

- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/final-validator-pycompile.log`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/final-validator-regression.log`

- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/final-package-validation.json`
- `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/final-package-validation.log`
