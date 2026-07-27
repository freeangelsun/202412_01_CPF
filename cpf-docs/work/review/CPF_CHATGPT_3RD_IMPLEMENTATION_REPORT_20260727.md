# CPF ChatGPT 3차 구현 Report — 2026-07-27

## 1. 기준

- 시작 SHA: `00780dc14ef621578f6f7ca61ef1d0c9973c60e6` (`20260727_04`)
- 종료 상태: Working Tree Patch, 미Commit/미Push
- Patch: `CPF_20260727_05_ROOT_PATCH`
- QA: `CPF_NEXT_QA_REQUIREMENTS_CHATGPT_FIRST_CODEX_REVIEW_20260727_05.md`

## 2. 작업 전 Review

QA와 기존 CPF 계획을 병합한 결과 이번 폐쇄 단위는 A-V Source 안전성 재확인과 Change Set B ADM/BZA Data Safety로 확정했다.

보호 대상:
- BZA Directory/Assignment/Approval의 기존 정상 기능
- ADM Identity/Profile Ownership
- V59/V60
- BAT/Generated Domain/Gateway 기존 성공 기능

실제 결함:
- ADM DB 오류 Memory 성공 전환 가능성
- 운영자 Identity/Profile/Role 비원자 생성
- 일반 운영자 자동 Role/default 활성화 위험
- 연락처 기본 원문 Projection/Audit 노출 위험
- Raw 조회 사유 URL 노출 위험
- BZA ACTIVE/EMPLOYED 의미 혼재
- 관리자 계정 상태와 직원 재직 상태 미분리
- BZA Java Vendor SQL/Internal Core 참조
- V59/V60 이후 상태/멱등/낙관적 잠금 lifecycle 누락

## 3. 구현 결과

### ADM
- DATABASE fail-closed 기본화
- MEMORY는 명시 Demo Profile 전용
- DB startup/readiness 상태 표시
- Identity/Profile/Role Transaction + `operationId` 멱등 생성
- 일반 생성 `PENDING_ACTIVATION`, Role 자동 부여 없음
- ACTIVE 전환 Role 필수
- 실패 로그인 상태/LOCKED 동기화
- 연락처 Masked 기본 Projection
- Raw 연락처 POST body reason + 별도 permission + Audit + no-store
- 연락처 optimistic version/explicit clear

### BZA
- 직원 `BzaEmploymentStatus` Catalog
- 관리자 `BzaAdminAccountStatus` Catalog
- 신규 관리자 PENDING/Role 미부여
- Auth login은 ACTIVE/use/lock 동시 확인
- Login failure LOCKED와 version 동기화
- 직원 연락처 normalize/mask/raw/clear
- Audit canonical snapshot PII masking/secret redaction
- `BzaDirectoryService`의 기존 조직·겸직·파견·대행·유효기간 기능은 중복 재구현하지 않고 보호

### SQL/Boundary
- Public `com.cpf.core.api.database.CpfVendorSqlCatalog` 추가
- ADM/BZA `com.cpf.core.common.*` 직접 import 0
- BZA Java inline SQL 0
- Backoffice/Auth/Operation/Audit/Directory/Approval/Support SQL을 MariaDB Query Resource로 외부화

### DB
- MariaDB V61 추가
- ADM account status/version/create operation id
- ADM profile display/version
- BZA admin account status/version
- employee legacy ACTIVE→EMPLOYED 명시 Migration
- Fresh schema/constraint/index/checksum/generated bundle 동기화
- Safe rollback: Role 미부여 계정에 fake role 또는 NOT NULL 강제 없음, EMPLOYED 역변환 없음

## 4. 구현 중 자체 Review로 추가 발견·수정한 결함

1. MEMORY no-op Transaction에서 fallback 후 rollback-only가 남을 수 있는 문제 → 수동 rollback-only 제거
2. MEMORY가 실제 DataSource Bean 생성 단계에서 DB 설정을 요구할 수 있는 문제 → 연결 불가 전용 MemoryDataSource 분리
3. 로그인 실패 Update가 상위 Transaction 예외로 롤백될 수 있는 문제 → 인증 실패 카운트 경계 분리
4. Raw PII 사유를 GET query에 넣으면 access log/browser history에 남는 문제 → POST body
5. Raw API Method 변경 후 ADM Permission Seed가 GET으로 남는 회귀 → POST 정렬
6. generated `00_verify.sql` 직접 수정 위험 → canonical `99_smoke_check.sql`에서 bundle 재생성
7. V61 rollback의 `role_code NOT NULL` 복원은 Role 미부여 PENDING 계정을 깨뜨림 → safe nullable compatibility 유지
8. Fresh/Migration constraint name drift → canonical 이름 정렬
9. 초기 범위 밖 BZA Directory/Approval/Support Repository에도 inline SQL 존재 → BZA 전체 Repository 경계로 확대 제거
10. Approval/Support Query에 과거 `employment_status='ACTIVE'` 잔존 → business-active Catalog 값으로 수정

## 5. 검증

실행한 저비용 검증은 Evidence `CPF_CHATGPT_STATIC_VALIDATION_20260727_05.txt`에 기록한다.

현재 Container 제약:
- Java 21
- Node 22
- `pwsh` 없음
- 전체 Git checkout 없음
- MariaDB 없음
- Browser 없음

따라서 Java25 Full Gradle, 실제 PowerShell Gate, MariaDB lifecycle, Browser는 미검증이다.

## 6. 제품 판정

Change Set B의 **구현은 완료**다.
외부 실행 검증은 `미검증`으로 분리하며 Commercial Release Gate를 통과시키지 않는다.

지원 DB Vendor 전체 완료를 주장하지 않는다. 현재 실제 GA lifecycle 정본은 MariaDB이며 PostgreSQL 등 다른 pack의 `미구현` metadata를 존중한다. 5 Vendor는 별도 DB Change Set에서 실제 구현/검증한다.

## 7. 다음 Change Set

다음 폐쇄 개발 단위는 `S — 최종 지원 Stack Migration`이다. `TRANSITION`을 제품 상태로 남기지 않고 공식 지원 가능한 Spring Boot 4.x + Java25 + Gradle9 조합으로 Root/Generator/BAT/WAR까지 실제 이관한다.
