# Test and Evidence

## 기준 및 환경

- 검증 기준 SHA: `d2adc89f344fa1f93a2f9291f6576ce69be05239`
- 최신 SHA 재기준화: PASS, Overlay 중첩 0건
- Java: OpenJDK 21.0.10 / javac 21.0.10
- Node/TypeScript: 22.16.0 / 5.8.3
- Chromium/Playwright: `/usr/bin/chromium` / Python Playwright 1.57.0
- Gradle/pwsh/Docker/Java25/실제 Vendor DB: 현재 개발 환경에 없음

## 실제 실행 결과

| 검증 | 결과 | 실제 범위 | Evidence |
|---|---:|---|---|
| P00 Split Master | PASS | Requirement 30,558 / Scenario 40,763 / Execution 30,558 / scope 10,027 | `P00_SPLIT_MASTER_VALIDATION_R2.json` |
| P01 Traceability | PASS | 전체 Cross-link 오류 0, 미완료 Release fail-closed | `P01_REQUIREMENT_TRACEABILITY_R2.json` |
| Python 회귀 | PASS | 기존 계약 포함 70건 | `P00_P05_MERGED_REGRESSION_TESTS_R2.log` |
| Java 21 대체검증 | PASS | 실제 Controller/Test Compile, Unit 2건, Runtime Harness, Major 65 | `JAVA21_SUBSTITUTE_VALIDATION.json` |
| ADM/BZA 대체검증 | PASS | Strict Type Check, Node Runtime, Chromium Runtime, Actor alias 5종 x 3경로 | `FRONTEND_SUBSTITUTE_VALIDATION.json` |
| P03 DB-less | PASS | exact-SHA ADM/CMN Owner Source | `P03_DB_LESS_FAIL_CLOSED_R2_TARGETED.json` |
| P03 Transaction ID | PASS | 34자리/Header/OpenAPI/Frontend/3 Vendor 대상 계약 | `P03_TRANSACTION_ID_R2_TARGETED.json` |
| P03 Operator Trust | PASS | ADM/BZA Consumer + Batch 재귀 Sanitizer | `P03_OPERATOR_TRUST_R2_TARGETED.json` |
| P03 Network/Audit | PASS | 공통 Policy, 3 Consumer, Durable Audit | `P03_NETWORK_POLICY_R2_TARGETED.json`, `P03_AUDIT_FAIL_CLOSED_R2_TARGETED.json` |
| P04 DB Vendor | PASS | exactly-three Metadata와 exact-SHA 확인 물리 Lifecycle 계약 | `P04_DB_VENDOR_R2_TARGETED.json` |
| P05 Starter | PASS | 38 Module / 6 Profile / 32 Internal / 7 Group / 9 Provider Slot | `P05_STARTER_CATALOG_R2_TARGETED.json` |
| Hygiene/Secret | PASS | JSON·CSV·Path·보호경로·Root·삭제·Cache·Whitespace·Secret Pattern | `STATIC_HYGIENE_AND_SECRET_SCAN.json` |

상세 명령과 Exit Code는 `EXECUTION_LEDGER.csv`에 있다. 모든 검증은 임시 작업공간을 사용해 Repository에 Build/Cache/Log를 생성하지 않았다.

## 검증 이관

다음은 구현 누락이 아니라 외부 Runtime만 미실행된 항목이다.

- Java 25 Toolchain·Bytecode·Publication·JVM Option
- 전체 Gradle Build/Test/Publication/SBOM
- MariaDB/PostgreSQL/Oracle 실제 Provision~Rollback·Runtime Query
- ADM/BZA 전체 의존성 설치·Lint·Unit·Build·전체 E2E
- 전체 Repository P02 Owner 및 P03 Legacy Transaction 전수 스캔
- Docker·Registry·Signing·Multi-instance Runtime

정확한 Requirement/Scenario, 명령, 성공·실패 기준과 Evidence 경로는 `ENVIRONMENT_VALIDATION_HANDOFF.csv`에 있다. 전체 `verification_status`는 해당 검증 전까지 미검증 또는 재확인 필요로 유지한다.
