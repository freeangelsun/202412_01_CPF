# R4 Test and Evidence

## 실행 기준

- Branch: `master`
- 기준 Source SHA: `cb305fc5363263c9607e990ba640233c28668f01`
- Java/Javac: Java 21
- Node: Node 22
- Python: Python 3
- 현재 환경에 없는 항목: 전체 Git Working Tree, Java 25 Toolchain/Gradle dependency environment, 실제 Oracle/PostgreSQL/MariaDB Server, Chromium/Playwright 실제 BFF, 실제 ADM Spring 2-instance DB Runtime

## 최종 실행 결과

`cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R4/R4_RUNTIME_EXIT_SUMMARY_FINAL.csv`의 **22개 Task가 모두 Exit Code 0**이다.

| 검증 | 실제 결과 |
|---|---|
| Python Gate Positive·Negative | 67 tests PASS |
| Python compileall | PASS |
| ADM/BZA 전체 TypeScript Compile | ADM 41개, BZA 19개 PASS |
| ADM/BZA API Runtime | 2 Surface, 52개 이상 검사 PASS |
| Frontend Workflow Runtime | 19 checks, 4 pages, 12개 이상 operation contract PASS |
| Java 21 Controller | 61 assertions PASS |
| Java 21 DNS/Network | 18 assertions PASS |
| Java 21 Transaction | 11 assertions PASS |
| Java 21 Persistence | 9 assertions PASS |
| Java 21 DB-less | 7 assertions PASS |
| Java 21 Runtime Command | 37 assertions PASS |
| Java 21 Batch Abandon | 20 assertions PASS |
| Audit 2-JVM Kill/Restart | 220건, 중복 0, 유실 0, HEAD 불일치 0 PASS |
| 3 Vendor SQL Semantic/Lifecycle Parser | MariaDB·PostgreSQL·Oracle, Canonical 200 Table PASS |
| Frontend Consumer Closure | PASS |
| Network Policy Consumer Closure | Consumer 5개, Test 2개 PASS |
| Operator Trust Boundary | Frontend 154개 파일, Controller 2개 Snapshot 파일 PASS |
| Transaction ID Standard | Source 314개, Route 38개, Annotation 25개 PASS |
| Work Package Source Review | 291/291 연결, Inventory 512개, Required Aspect 미연결 0 |
| Requirement Traceability Build | Requirement 10,558개, Scenario 14,014개 연결 |
| Requirement Traceability Closure | PASS — Traceability만 증명하며 구현 완료/QA 통과 의미 아님 |

## 실제 구현·재현 결과

### Batch Abandon

- `ABANDONING` 상태를 먼저 선점한 Process만 외부 `JobOperator.abandon`을 호출한다.
- 동시 호출에서 외부 Side Effect는 정확히 1회였다.
- 외부 호출 응답 유실과 최종 Ledger 기록 실패를 `UNKNOWN_RESULT`로 보존한다.
- MariaDB·PostgreSQL·Oracle Source/Install/V99 Migration/R99 Rollback/V99 Verify를 함께 보완했다.

### Runtime Command·위험명령 Ledger

- Snapshot/CAS 사전 실패는 `FAILED`, 외부 호출 후 응답 유실·Evidence 저장 실패는 `UNKNOWN`으로 분리했다.
- 동일 Target 중복을 차단하고 대상별 부분 성공을 보존한다.
- Approval ALL/ANY/N_OF_M, Request Fingerprint, Expected Version, 재실행 차단을 Java 21에서 검증했다.
- `bat_operation_request` Canonical·3 Vendor V100/R100 Lifecycle을 연결했다.

### Frontend

- ADM/BZA 전체 TypeScript Compile과 API Runtime을 실행했다.
- Generated Mutator가 Actor Alias, 비인가 `operatorId`, Browser Bearer, Cross-Origin, Raw privileged body를 fail-closed 처리한다.
- BZA에서 잘못 허용되던 ADM Login 경로 예외를 제거했다.
- Break-glass, Attachment, Session, Maintenance 위험조치 Workflow를 실제 TypeScript로 실행했다.

### Audit

- JVM 두 개를 동시에 실행하고 한 JVM을 강제 종료한 뒤 다른 JVM의 저장 지속과 종료 JVM 재기동을 검증했다.
- Record 220건, unique 220건, 중복·유실·Secret 원문 누출 0건이다.
- 저장 실패 성공 은폐와 조회 실패 빈 목록 은폐를 차단했다.

## Requirement·Work Package 결과

- Requirement 개별 Traceability: 10,558/10,558
- Work Package Source 연결: 291/291
- Work Package Required Aspect 미연결: 0
- 공통 구현·대체 Runtime Evidence 연결 Requirement: 6,972
- Traceability-only Requirement: 3,586
- Requirement별 전체 Acceptance 개발 완료: 0

6,972건은 연결된 공통 구현·대체 Runtime 범위만 인정하며 Requirement 고유 Acceptance 전체 완료로 승격하지 않았다. 3,586건은 Source·Scenario·Consumer Traceability만 연결돼 `미검증`을 유지한다.

## 외부 환경 실행 Wrapper

새 Commit exact HEAD에서 다음을 한 번에 fail-closed 실행한다.

`cpf-tools/scripts/run-cpf-r4-exact-head-validation.ps1`

이 Wrapper는 clean HEAD, Java 25, Gradle Module Test, 3 Vendor DB lifecycle, ADM/BZA Browser E2E, 실제 Audit Spring 2-instance Runtime이 모두 성공해야 PASS한다. 현재 환경에서는 실행하지 않았으며 성공으로 기록하지 않았다.
