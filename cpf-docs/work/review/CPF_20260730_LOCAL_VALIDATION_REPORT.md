# CPF 20260730 Local Validation Report

## 1. 검증 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 기준 SHA: `fae7aa9643f646db4bcbcf665d13b8f3b809e8c8`
- 검증 시각: `2026-07-30T10:01:04+09:00`
- 검증 대상: `CPF_20260730_FULL_IMPLEMENTATION_FINAL_OVERLAY.zip` 생성 후보
- 추적 범위: Requirement 405건, Scenario 90건, 총 495행

## 2. 실제 실행 결과

| 검증 | 결과 | 비고 |
|---|---|---|
| Core Public API Java compile | 완료 | JDK 21 `javac --release 21` |
| Batch Contract Java compile | 완료 | Core API 실제 Classpath 사용 |
| Gateway Java compile | 완료 | Core API 실제 Classpath + Spring/JDBC 최소 Stub |
| Gateway Target Selection 동작 | 완료 | Round Robin, Least Load |
| Gateway Health Hysteresis 동작 | 완료 | DEGRADED → DOWN 전환 |
| Batch Worker Java compile | 완료 | 실제 `SensitiveTextSanitizer` + Spring/SLF4J 최소 Stub |
| Batch Control Job Definition Java compile | 완료 | 실제 Batch Contract + Spring/JDBC/Jackson 최소 Stub |
| TypeScript/Vue Script parse | 완료 | 19개 Script, TypeScript `transpileModule` |
| JSON parse | 완료 | Canonical, Manifest, Generator Schema 포함 |
| PowerShell 구조 검증 | 완료 | 11개 Script의 String/Comment 고려 괄호 균형 |
| Canonical 관계 검증 | 완료 | 173개 Table, Index Column, FK Table/Column/Arity |
| 3 Vendor Clean Install Parity | 완료 | MariaDB/PostgreSQL/Oracle 각각 173개 Table |
| V74~V76 Migration Parity | 완료 | 신규 13개 Table 집합 일치 |
| V74~V76 Rollback Parity | 완료 | 신규 13개 Table 역순 제거, 부분 적용 Guard 포함 |
| Runtime SQL Filename Parity | 완료 | BZA Menu Tree 3개 SQL × 3 Vendor |
| MariaDB Index Prefix 격리 | 완료 | `duplicate_key(255)`가 PostgreSQL/Oracle에 유출되지 않음 |
| Matrix 정합성 | 완료 | 405 Requirement + 90 Scenario, ID 중복 없음 |
| UTF-8/Control Character | 완료 | 대상 Text 파일 읽기와 제어문자 검사 |
| High-signal Secret Scan | 완료 | Private Key, AWS Key, 평문 Credential 패턴 없음 |
| Stale/Temporary Artifact | 완료 | `.bak/.tmp/.log/.zip/.class/.pyc` 없음 |

## 3. 검수 중 추가 보완

1. Oracle V75 Migration 검증 오탐은 Dynamic SQL Rollback 구문을 일반 `DROP TABLE` Regex로만 검사한 Validator 문제로 확인했다.
2. V74 Rollback만 부분 적용 상태에 안전하지 않아 다음과 같이 통일했다.
   - MariaDB/PostgreSQL: `DROP TABLE IF EXISTS`
   - Oracle: `SQLCODE = -942` 허용 Guard와 `CASCADE CONSTRAINTS`
3. Canonical FK Key를 `refTable/refColumns`로 통일하고 Generated Manifest는 `referencedTable/referencedColumns`로 정상화했다.
4. Vendor Source Generator에서 MariaDB Index Prefix가 PostgreSQL/Oracle로 유출되지 않도록 생성 로직을 분리했다.
5. Secret Parameter는 외부 검증 응답에서만 Masking하고 내부 Resolve 결과에는 승인된 Secret Reference를 유지하도록 분리했다.
6. File Claim 재취득의 Fencing Token 감소 가능성과 Approved Shell 출력 제한 미연결을 보완했다.

## 4. 이 환경에서 실행하지 못한 검증

다음은 전체 Repository, PowerShell 7 Runtime, 공식 DB Runtime, Redis, Browser가 필요한 검증이므로 성공으로 기록하지 않는다.

- Root Gradle `clean test assemble`
- ADM/BZA Typecheck, ESLint, Vitest, Production Build
- Root `qualityGate`와 `verify-cpf-final-completion.ps1`
- MariaDB 실제 Backup → Upgrade → Verify → Rollback → Reapply → Restore 및 별도 Clean Install
- PostgreSQL·Oracle 실제 Install/Migration/Upgrade/Rollback/Runtime Query
- Redis 장애·복구·다중 인스턴스·Lock/Fencing
- Gateway/Batch Runtime, Browser E2E, 권한 Negative Test
- 495개 Matrix와 2,715개 Verification Ledger의 exact-SHA Evidence 폐쇄

## 5. 판정

- Source·SQL·Frontend·Test·Script·문서 개발 Overlay: `완료`
- 현재 환경의 독립 정적·계약 검증: `완료`
- 최신 Source Commit 기준 전체 통합 검증: `미검증`
- CPF 전체 제품 최종 완료: `재확인 필요`

실행하지 않은 검증 결과를 현재 Overlay의 성공 근거로 사용하지 않는다.
