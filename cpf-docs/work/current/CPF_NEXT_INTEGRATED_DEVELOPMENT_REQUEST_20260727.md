# CPF 다음 통합 개발 요청 — 2026-07-27

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 기준 SHA: `fb95e15f90856adcff39040a50b128aa40f5ef43` (`20260727_01`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 입력: `CPF 다음 통합 개발·QA 요구사항`
- 원칙: 문서 완료 표시보다 실제 Git Source/API/SQL/Test/Runtime/Evidence를 우선한다.

이 요청서는 QA 요구, Codex 사용량 제한 중단분, 최신 master 재검토 결과와 사용자 추가 요구를 하나의 후속 목록으로 합친다.
이미 검증된 범위를 무조건 다시 실행하지 않는다. 변경 영향이 없고 최신 Evidence가 유효한 항목은 재사용하며,
ChatGPT가 변경한 범위와 Codex가 중단한 범위만 우선 재검증한다.

## 2. 이번 ChatGPT 1차 구현 범위

### CPF-BUILD-LOCAL-001 — 공통 Artifact 자동 전파

목표:

1. `cpf-core`, `cpf-common`, BAT public contract/testkit, CPF BOM, Domain Convention Plugin을 동일 CPF build에서 생성한다.
2. 원격 Artifact Registry가 없는 로컬 개발에서는 `${user.home}/.cpf/repository`를 기본 공유 Maven Repository로 사용한다.
3. `CPF_LOCAL_ARTIFACT_REPOSITORY` 또는 Gradle property `cpfLocalArtifactRepository`로 경로를 변경할 수 있다.
4. 원격 `CPF_ARTIFACT_REPOSITORY_URL`이 있으면 원격을 우선하되 로컬 fallback도 사용할 수 있다.
5. CPF Root의 `build`가 성공한 경우에만 로컬 환경의 public artifact를 자동 동기화하며, 실패한 build는 shared repository에 publish하지 않는다.
6. `-PcpfAutoLocalArtifactSync=false`로 특수 검증/CI에서 자동 동기화를 끌 수 있다.
7. 독립 Generated Domain 생성 시 원격 Repository가 없으면 Generator가 CPF local public artifact를 먼저 publish한다.
8. 생성된 독립 Repository는 shared local Repository를 자동 탐색한다.
9. Generated Domain은 `com.cpf.domain-conventions`를 사용하여 Java 25/reproducible archive/repository 규칙을 공유한다.
10. Generated Domain의 `bootJar`와 `bootWar` 모두 `cpf-core`, `cpf-common` 및 필요한 BAT contract JAR 포함 여부를 검증한다.
11. Root 배포 Gate도 가능한 `bootWar`에 대해 `WEB-INF/lib` 포함 여부를 확인한다.

판정: **이번 ChatGPT 구현 대상**.

### CPF-ADMIN-CONTACT-001 — ADM/BZA 연락처 의미 분리

표시/계약:

- 휴대폰 번호는 UI에서 `연락처(휴대폰)`으로 표시한다.
- `내부 전화번호`를 별도 선택 필드로 제공한다.
- 전화번호는 숫자 DB Type을 사용하지 않는다.
- 미입력은 `NULL`/null로 유지한다.
- ADM 운영자와 BZA 직원 모두 동일 의미를 사용한다.
- ADM 연락처는 인증 Identity인 `adm_operator`가 아니라 Directory/Profile인 `adm_operator_profile`이 소유한다. 운영자 API는 편의를 위해 Profile 연락처를 Projection한다.
- 기존 Java record 생성자 Consumer가 깨지지 않도록 호환 생성자를 유지한다.

DB:

- `adm_operator_profile.MOBILE_NO`
- `adm_operator_profile.OFFICE_PHONE_NO`
- `adm_operator` 인증 Identity에는 연락처 컬럼을 추가하지 않는다.
- `bza_employee.mobile_no`는 연락처(휴대폰) 의미로 유지
- `bza_employee.office_phone_no` 신규
- 신규 Migration `V59__admin_contact_model.sql`
- Rollback `V59__admin_contact_model_rollback.sql`

판정: **이번 ChatGPT 구현 대상**.

### CPF-BZA-DEFAULT-001 — 직원 Safe Default 정렬

QA의 직원 기본 재직 상태는 `EMPLOYED`인데 최신 Source/DDL은 `ACTIVE`였다.
기존 Row를 일괄 변환하지 않고 신규 입력/DDL Default만 `EMPLOYED`로 정렬한다.

- `BzaBackofficeService` 미입력 기본값 `EMPLOYED`
- `bza_employee.employment_status` DDL Default `EMPLOYED`
- `V60__bza_employee_safe_defaults.sql`
- Rollback은 기존 Default `ACTIVE` 복원

판정: **이번 ChatGPT 구현 대상 / 실행 미검증**.

## 3. Codex 중단분 최신 master 재분류

| Requirement | 최신 master 판정 | 후속 |
|---|---|---|
| 고정 `/mbr`/port 기반 Target 추정 제거 | 구현 확인됨 | 관련 Core/GWY 표적 Test만 필요 |
| Generated Domain DB Installer 동적 발견 | 구현 확인됨 | 신규 임시 Domain 2개 설치 경로로 재확인 |
| ADM의 MBR 전용 Controller/Service/UI 결합 제거 | 구현 확인됨 | ADM focused test |
| REF의 MBR Service Call 예제 제거 | 구현 흔적 확인 | REF self-contained Gate 재확인 |
| BAT `cpf-batch/src/**` Legacy 제거 | Source 제거 확인 | 기능/EDU Inventory parity 재확인 |
| Build Tooling `cpf-tools/build/*` 이동 | 구현 확인됨 | 이번 local artifact 변경과 함께 재검증 |
| BAT 158 Query Pack | 이전 실제 MariaDB PREPARE 완료 | 이번 변경과 직접 무관하므로 전체 재실행 금지 |
| MBR/ACC Golden normalized parity | **미완료** | Generator-first 이관 P0 |
| `settings.gradle`의 MBR/ACC Root 고정 Include 정책 | **미완료** | Golden Reference 정책 확정 후 수정 |
| `CpfSystemCodes.inferFromTypeName` type/package 추론 | **잔존/Consumer 재확인 필요** | 실제 Consumer 0 확인 후 제거 또는 Registry 계약으로 치환 |
| Historical Migration 전체 Chain | 미검증 | 최종 통합 검증 시 수행 |
| Browser/Multi-instance/Fault | 미검증 | 관련 기능 안정화 후 통합 검증 |

## 4. 남은 P0

### P0-A Generated Domain Golden 구조

- MBR/ACC를 동일 Capability의 normalized Generator-owned tree로 정규화한다.
- MBR 하나만 Golden Reference로 둘지, ACC를 두 번째 Generated Reference Instance로 둘지 정본화한다.
- 고객 업무 Legacy 구현은 Generator-owned 영역과 분리한다.
- 임시 `payment/PAY`, `insurance/INS`를 생성해 normalized tree/hash, bootJar/bootWar, DB sample, 삭제/재생성을 검증한다.
- Generated Domain 삭제가 Platform build/menu/health/route를 깨지 않도록 한다.
- `CpfSystemCodes` 등 package/type/path/port 추론 잔재를 Consumer 확인 후 제거한다.

### P0-B BAT Legacy 기능/EDU parity

- `CPF_LEGACY_BATCH_MIGRATION_MAP.md`와 삭제된 146개 파일을 Standalone Runtime/REF EDU/Generator Job Pack으로 양방향 대조한다.
- 설명만 있고 실행 불가능한 EDU는 완료 처리하지 않는다.
- BAT 실제 5 Runtime 다중 Process topology는 최종 통합 검증으로 남긴다.

### P0-C Gateway Resilience

- Target A down → B failover
- Outlier/ejection/recovery
- connect/read/overall timeout
- retry budget/idempotency
- `UNKNOWN_RESULT`와 reconciliation
- O/S/B 경계 및 외부→S 우회 차단
- header trust boundary
- 2 Gateway route version/drift

현재 Module Test/단일 Runtime boot 성공만으로 위 항목을 완료 처리하지 않는다.

### P0-D ADM/BZA 운영 완결성

- Generated Domain capability 기반 범용 관리
- 직원/조직 Assignment, Role/Permission, Approval
- 위험조치 승인/Audit
- Server paging/filter/sort
- Browser 접근성/오류/partial failure
- 개인정보 masking/download audit
- 이번 연락처 변경은 위 전체 항목 중 연락처 의미 모델만 닫는다.

## 5. 남은 P1

- Platform Module inline/vendor SQL 중앙화
- 5 Vendor Source/Template/Contract parity
- Historical migration/rollback/reapply
- Runtime SQL 실제 계정 검증
- Config metadata
- Message/Code catalog
- Repository/Source access governance
- Deployment Cell
- Browser E2E
- Multi-instance/Fault/UNKNOWN_RESULT 통합 검증
- SBOM/License/CVE/Signature
- 최종 Repository Hygiene와 최신 SHA Evidence

## 6. DB 변경 절대 순서

`Canonical Source/Metadata → Generator/Template 영향 확인 → Vendor 영향 확인 → Migration → Rollback → Generated lifecycle artifact → Verify/Manifest → Runtime Consumer → Evidence`

이번 V59/V60은 MariaDB 실검증 Vendor의 canonical source/migration까지 준비한다.
`sync-database-artifacts.ps1`와 실제 MariaDB 적용을 실행하지 않은 상태에서 DB 완료로 표시하지 않는다.
다른 4개 Vendor는 현재 Platform Schema source가 실DB 정본 수준으로 존재하지 않으므로 SQL을 임의 복제하지 않는다.
정식 Vendor projection/compiler 또는 검증된 Vendor-specific source를 마련한 뒤 완료 처리한다.

## 7. Codex 크레딧 절약형 검증 원칙

Codex는 다음 순서로 검증한다.

### A. ChatGPT 변경 범위 — 반드시 재검증

1. `publishCpfLocalPlatformArtifacts`
2. `verifyCpfLocalArtifactPropagation`
3. 임시 Generated Domain 1~2개 `create-domain-repository.ps1`
4. 생성 Repository `clean test verifyCpfPackagedDependencies`
5. ADM/BZA 변경 Module compile/test
6. ADM/BZA Frontend typecheck/build
7. `check-admin-contact-model.ps1`
8. DB artifact sync
9. MariaDB V59 Upgrade → 확인 → Rollback → Reapply
10. `check-bza-safe-defaults.ps1` 및 V60 Default Upgrade → Rollback → Reapply

### B. Codex 중단점 — 이어서 검증

- MBR/ACC Golden parity
- REF self-contained dependency 0
- `CpfSystemCodes` inference Consumer
- BAT EDU migration parity

### C. 지금 다시 하지 않는 고비용 검증

다음은 이번 변경과 직접 관련 없는 이상 무조건 반복하지 않는다.

- BAT 158 SQL 전체 PREPARE
- 기존 V58 lifecycle
- 이미 성공한 전체 416 Test의 무조건 재실행
- 전체 Browser E2E
- 전체 Multi-instance/Fault suite

단, A/B 검증에서 공통계약 회귀가 발견되면 영향 범위에 맞춰 확대한다.

## 8. 완료 금지

- ChatGPT가 만든 patch를 Build 없이 완료 처리
- local Maven Repository에 파일이 있다는 이유만으로 bootJar/bootWar 포함을 추정
- Generated output만 수동 수정
- V59 SQL 존재만으로 DB 완료
- 휴대폰/내부 전화번호를 한 Column으로 재통합
- 전화번호를 numeric type으로 변경
- 기존 검증 성공을 변경 후 성공으로 자동 승계
- Codex에게 처음부터 모든 검증을 다시 시켜 크레딧을 소모

## 9. 이번 작업 인계 파일

- `cpf-docs/work/current/CPF_NEXT_INTEGRATED_DEVELOPMENT_REQUEST_20260727.md`
- `cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md`
- `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- `cpf-docs/work/review/CPF_CHATGPT_1ST_IMPLEMENTATION_REPORT_20260727.md`
- `cpf-docs/work/review/CPF_CODEX_2ND_REVIEW_CHECKLIST_20260727.md`
- `cpf-docs/evidence/CPF_NEXT_INTEGRATED_EVIDENCE_INDEX_20260727.md`


## 통합 잔여 Requirement Matrix

- `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md`
- QA 전체 항목을 구현 확인/부분 구현/미완료/통합검증 예정으로 재분류한 정본이며, 다음 검수는 이 Matrix와 Change Ledger를 함께 사용한다.
