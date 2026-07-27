# CPF Current Work Request

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 이번 ChatGPT 작업 시작 SHA: `00780dc14ef621578f6f7ca61ef1d0c9973c60e6` (`20260727_04`)
- Working Tree: `CPF_20260727_05_ROOT_PATCH` — 아직 Commit/Push 하지 않음
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 통합 QA 입력 정본: `cpf-docs/work/review/CPF_NEXT_QA_REQUIREMENTS_CHATGPT_FIRST_CODEX_REVIEW_20260727_05.md`

## 2. 이번 작업 종료 판정

이번 Change Set B — ADM/BZA Data Safety는 **구현 완료**로 닫는다.

완료된 구현 범위:

- ADM 운영자 Identity/Profile/Role 생성 Transaction 원자성
- `operationId` 기반 운영자 생성 멱등성
- 신규 ADM 운영자 `PENDING_ACTIVATION`, Role 자동 부여 금지
- Product ADM DB fail-closed, MEMORY는 명시적 local/test/demo/library 전용
- ADM DB startup/readiness 상태 가시성
- ADM/BZA 기본 연락처 Masking, Raw 조회 별도 권한·사유·Audit·`no-store`
- Raw 조회 사유의 URL/query-string 노출 금지 — POST body 사용
- 연락처 정규화, Blank/NULL/명시적 Clear 계약
- BZA 직원 재직상태와 관리자 계정상태 Catalog 분리
- BZA 신규 관리자 `PENDING_ACTIVATION`, Role 자동 부여 금지
- BZA Audit Snapshot PII Masking/Secret Redaction
- BZA Java inline SQL 제거 및 Vendor Query Resource 이관
- ADM/BZA의 `com.cpf.core.common.*` 직접 import 제거
- Public `CpfVendorSqlCatalog` API 경계 제공
- MariaDB V61 Fresh/Upgrade/Rollback 정본 및 generated lifecycle parity
- Data Safety 정적 Gate `check-admin-data-safety.ps1`

다음 항목은 구현 부족이 아니라 현재 실행환경 부재로 **미검증**이다.

- Java 25 + Gradle 9.1 전체 clean/test/assemble
- PowerShell `check-admin-data-safety.ps1` 실제 실행
- MariaDB V59→V60→V61 upgrade/rollback/reapply/fresh/runtime
- ADM/BZA Browser E2E
- DB fault/동시성 실제 Runtime

미검증 항목은 Commercial Release Gate를 통과시키지 않는다.

## 3. A-V Stack/Artifact 상태

`20260727_04`에서 구현한 Stack/Artifact 안전장치는 Source 계약 기준 완료 상태를 유지한다.
다만 Java25/Gradle9, Local staging/promotion/rollback, Offline standalone, bootJar/bootWar hash, Remote Registry는 실제 환경 검증이 필요하다.

이를 다시 `부분 구현`으로 낮추지 않는다. **구현 상태=완료 / 실행 검증 상태=미검증**으로 분리한다.

## 4. 다음 실제 개발 Change Set

다음 작업은 **CHANGE SET S — 최종 지원 Stack Migration**이다.

목표:

1. `TRANSITION` 상태를 제거한다.
2. 작업 시점 공식 지원 Matrix를 다시 확인하여 Java 25 + Gradle 9.1과 공식 호환되는 Spring Boot 4.x Target을 확정한다.
3. Root/Module/BOM/Convention Plugin/Generator/Exported Domain/BAT/ADM/BZA/Gateway/REF를 한 번에 이관한다.
4. Spring Framework/Security/Batch, MyBatis, Flyway, Actuator, Testcontainers, springdoc, Servlet/Jakarta, Embedded/External WAS를 함께 이관한다.
5. bootJar/bootWar/External WAR/Generated Domain까지 검증 가능한 상태로 닫는다.
6. 구현 가능한 항목을 `부분 구현/미구현/재확인 필요`로 넘기지 않는다.

그 다음 순서는 `C Generated Domain → D BAT → E Gateway → U ADM/BZA UX → DB → T Gate/Tool/CI/Release → 최종 Full Regression`이다.

## 5. 보호할 성공 기능

- BAT standalone 역할 분리와 Lease/Fencing
- BAT Query Contract와 V58 구조
- ADM Generated Domain 직접 종속 제거
- REF Generated Domain 중립화
- BZA Directory/Assignment/Approval Engine의 기존 정상 기능
- V59/V60 연락처/기본값 lifecycle
- LOCAL_DEV/REMOTE/OFFLINE Artifact 공급 정책
- `transactionId` 단일 제품 용어

후속 변경이 위 기능의 영향권에 들어가면 targeted regression 대상으로 다시 연다.

## 6. Git 정책

사용자 명시 승인 없이 Commit, Push, Branch, Tag, Release를 생성하지 않는다.
