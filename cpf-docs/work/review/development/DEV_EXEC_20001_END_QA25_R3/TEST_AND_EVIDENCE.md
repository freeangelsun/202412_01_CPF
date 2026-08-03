# Test and Evidence

## 기준

- Source HEAD: `cb305fc5363263c9607e990ba640233c28668f01`
- Working Tree: 현재 실행환경에 Git checkout이 없어 직접 기록 불가. GitHub `master` HEAD는 위 SHA로 재확인했다.

## 최종 실제 실행

| 검증 | 결과 | 주요 수치 | Evidence |
|---|---:|---|---|
| Python Gate Positive/Negative | PASS | 30 tests | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_PYTHON_GATE_ALL.log` |
| Audit Harness Unit | PASS | 1 test | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_PYTHON_AUDIT_UNIT.log` |
| Python compileall | PASS | scripts + verification | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_PYTHON_COMPILEALL.log` |
| ADM/BZA Frontend Runtime | PASS | 2 surfaces, 30+ checks | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_FRONTEND_RUNTIME.log` |
| Java21 Batch Controller | PASS | 61 assertions | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_CONTROLLER.log` |
| Java21 DNS Pinning | PASS | 15 assertions | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_NETWORK.log` |
| Java21 Persistence Runtime | PASS | 9 assertions | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_PERSISTENCE.log` |
| Java21 Transaction Runtime | PASS | 11 assertions | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_TRANSACTION.log` |
| Java21 Audit kill/restart | PASS | 220 records, duplicate/loss 0 | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_AUDIT.log` |
| Java21 Audit stress | PASS | 3/3 runs, 220 records/run | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_JAVA21_AUDIT_STRESS_3X.log` |
| DB Vendor Parser/Object Parity | PASS | 200 tables, 2,914 columns, 337 indexes, 147 FKs/vendor | `cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R3/FINAL_DB_VENDOR_PARITY.log` |

`FINAL_EXECUTION_EXIT_SUMMARY.csv`의 모든 최종 실행 Exit Code는 0이다.

## 발견·보정된 실제 결함

Audit 다중 JVM 최초 동시기동에서 `exists → CREATE_NEW` 경합이 실제 재현되었다. `FileChannel.open(..., CREATE, WRITE)`의 멱등 초기화로 보정하고 기존 Record를 truncate하지 않도록 수정했다. 보정 후 단독 실행과 3회 반복 모두 성공했다.

## 미실행 범위

- Java 25 Toolchain 기반 전체 Root Gradle Build/Test
- 실제 Spring ApplicationContext 전체 Module 기동
- Oracle/PostgreSQL/MariaDB 실제 Install·Migration·Rollback·Reinstall
- Playwright 실제 Browser E2E
- 전체 Repository Working Tree 기반 Owner/Operator/Traceability 전수 Gate

위 항목은 `ENVIRONMENT_VALIDATION_HANDOFF.csv`에 환경·권한·정확한 명령·성공/실패 기준을 기록했다.
