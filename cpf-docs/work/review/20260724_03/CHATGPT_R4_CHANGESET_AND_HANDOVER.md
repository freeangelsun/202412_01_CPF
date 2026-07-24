# ChatGPT R4 변경집합 및 3-Way 인수인계

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- R4 기준 Commit: `536b229bd46411d6be66a5e4697eaca002c50f1a` (`20260724_02`)
- Package: `CPF_PRODUCT_HARDENING_20260724_R4`
- 작성 주체: ChatGPT + 사용자 제품 정책 확정사항
- 목적: 회사 Codex / 집 Codex / ChatGPT가 동일한 제품 기준으로 이어서 개발·검증하도록 실제 변경사항과 미완료를 한 문서에 고정한다.
- Commit/Push/Branch: 수행하지 않음.

이 문서는 Codex 완료 보고서가 아니다. **직접 구현한 정적 변경, 확인한 결함, 실제 미실행 검증을 구분하는 인수인계 정본**이다.

## 2. 이번 R4에서 사용자가 최종 확정한 정책

### 2.1 거래 식별자

1. CPF 거래 실행 인스턴스 ID는 `transactionId` 하나만 사용한다.
2. 기존 `transactionGlobalId`는 제품 개념과 신규 Source/SQL/API 계약에서 폐기한다.
3. 기존 Global ID의 검증된 생성 규격을 `transactionId`가 승계한다.
   - `yyyyMMddHHmmssSSS` 17자리
   - `SystemCode` 3자리
   - `wasId` 7자리
   - sequence 7자리
   - 총 34자리
4. 외부/선행 거래에서 유효한 transactionId가 들어오면 그대로 승계한다.
5. Scheduler/Batch/Worker/Center-Cut/Agent 등 **독립 내부 기동**은 Core가 새로운 transactionId를 생성한다.
6. 동일 업무 흐름의 Local/Remote/Async/Retry/Replay/Batch 후속 처리에서는 새 transactionId를 만들지 않는다.
7. 세부 호출 계층은 `segmentId`, `parentSegmentId`, `attemptNo`, `callDepth` 등으로 표현한다.
8. `standardExecutionId`는 `OXYZAA0001` 계열의 **실행 정의 ID**이며 transactionId와 의미가 다르다.

### 2.2 Generated Domain

1. Generator는 MBR/ACC/EXS/REF/LNG/ING/PAY 등 특정 업무명을 하드코딩하지 않는다.
2. 신규 고객 Domain은 제한 없이 추가 가능하며 DomainName/SystemCode/Module/Package/Schema/TablePrefix/Port/선택 Capability만 Metadata로 달라진다.
3. 동일 Capability의 기본 Source/DB/Test 구조는 normalize 시 동일해야 한다.
4. 기본 DB Sample은 `${tablePrefix}_sample_item` 하나이며 실제 CRUD/Search/Paging/Validation/Idempotency/Optimistic Lock 예제를 제공한다.
5. Standard Header, transactionId, 공통 오류, 감사/마스킹 등 기술 공통 기능을 Generated Controller Body에 복제하지 않는다. Core Context/Filter/Interceptor가 책임진다.
6. Generator가 DB를 생성할 때도 SQL을 내부에 복제하지 않는다. 중앙 Vendor `domain-template` + `initialize-domain-database.ps1`가 DB SSOT다.
7. 표준 Lifecycle은 `create -> optional DB bootstrap -> CRUD -> verify -> remove -> regenerate -> parity`다.
8. REF는 다양한 기능을 보여주는 선택형 EDU/reference이므로 Golden Generated Domain과 동일 테이블 수를 강제하지 않는다.
9. MBR은 장기적으로 Golden Generated Domain의 checked-in reference instance로 정렬한다.
10. ACC/EXS는 실제 Consumer 이관 확인 전 물리 삭제하지 않는다. 이관 후 고정 Module/Schema 제거 대상이다.

### 2.3 DB/Vendor 변경 순서

모든 DB 변경은 다음 순서를 지켜야 한다.

`Canonical Vendor Source SQL/Metadata -> generated Vendor Pack -> Migration/Rollback -> Mapper/Repository -> Service/API/UI -> Test/Runtime -> Evidence`

- Historical Flyway는 수정하지 않는다.
- Product Seed / Optional Reference Seed / Test Seed를 분리한다.
- 테이블만 만들고 운영에 필요한 초기 코드/권한/정책/메뉴/설정 Metadata를 누락하면 완료가 아니다.
- Fresh Install과 Upgrade 양쪽을 모두 검증한다.

### 2.4 Repository 정본 위치

- Root 문서 최종 허용: `README.md`만.
- Tool/Shell: `cpf-tools/scripts/`
- 사람이 수정하는 MariaDB SQL Source: `cpf-tools/db/source/mariadb/`
- 생성/배포 Vendor Pack: `cpf-tools/db/vendor/mariadb/`
- 최종 목표: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 상세 작업 요청: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- Continuity/Decision: `cpf-docs/work/state/`
- Evidence: `cpf-docs/evidence/`

## 3. ChatGPT가 R4에서 직접 구현/보강한 항목

### 3.1 transactionId 단일화 기반

- Core Transaction Header/Context/Generator/Filter/Header extractor/propagator를 transactionId 중심으로 보강.
- Broker Envelope와 Segment Context를 transactionId 계약으로 정렬.
- 파일 거래로그 그룹키를 업무 정의 ID가 아니라 transactionId로 교정.
- 파일 경로는 transactionId + Business Date를 기준으로 그룹화하도록 정리.
- ADM frontend transaction helper를 34자리 transactionId 생성/검증 규격으로 교정.
- `check-transaction-id-standard.ps1`를 새 정책 Quality Gate로 보강.
- active R4 Source/SQL/Guide에서 구 Global 거래 ID 명칭이 잔존하지 않도록 정적 Gate 추가.
- Historical Flyway에서 과거 컬럼명이 남는 것은 이력 보존을 위해 허용하고, V38에서 forward migration한다.

### 3.2 Generated Domain Golden 계약

- Generator 기본 Capability를 최소 업무 Domain 중심으로 정리.
- Generated Controller/DTO가 transactionId/idempotency/actor/sequence를 Body에서 다시 받는 중복을 제거하고 Core `TransactionContext`를 사용하도록 Template 보강.
- `-ProvisionDatabase` orchestration을 추가하여 DB bootstrap을 중앙 `initialize-domain-database.ps1`로 위임.
- `domain-template`의 단일 Sample Table을 `transaction_id CHAR(34)` 계약으로 정렬.
- MyBatis Template에 누락됐던 `findBySampleKey`, `findByIdempotencyKey`를 추가.
- Result Map/Map 사용 시 camelCase alias를 보강.
- deterministic generated-domain parity 검사 Script 추가.
- 생성/DB/CRUD/삭제/재생성 Lifecycle Smoke Script 추가.

### 3.3 MariaDB SSOT / Migration / Seed

- Root `cpf-tools/db/source/mariadb`을 대체할 `cpf-tools/db/source/mariadb` 정본을 구성.
- Split SQL과 generated `00_*` bundle을 재구성하고 Central Vendor lifecycle과 byte parity를 맞춤.
- 정적 Split DDL: 124개 Table.
- V38 `transactionId` 통합 + BZA Governance forward migration 추가.
- V38은 optional/부분 설치 환경에서 없는 Table 때문에 바로 실패하지 않도록 table/column existence helper를 사용하도록 보강.
- R38은 복구 불가능한 의미 축소를 거짓 rollback하지 않고 Backup/Forward Recovery 필요를 명시하는 Guard로 구성.
- Product Seed에서 localhost/127.0.0.1 개발 fixture를 제거.
- Local/REF fixture는 Optional Reference Seed로 이동.
- BZA 기본 Role/Menu/Permission/Project Setting은 Product Metadata로 분리.

### 3.4 BZA / ADM 데이터 모델

BZA DDL에서 다음을 직접 보강했다.

- Organization -> Position -> JobTitle -> Employee -> Assignment FK 생성 순서 보정.
- `bza_organization_responsibility` 추가: 책임자/대행/결재책임과 유효기간 관리.
- Approval Document에 policy snapshot, payload hash, request idempotency, transactionId 조회 계약 보강.
- Approval Participant에 approver snapshot / resolution source 보강.
- Business Audit에 hash-chain 필드 보강.
- Attachment에 scan/data-classification/retention/quarantine Governance 필드 보강.
- BZA 기본 Role/Menu/API Permission/Approval 관련 Project Setting Product Seed 보강.

ADM/BZA Engine/API/UI 전체 완료를 의미하지 않는다. Runtime 연결은 아래 미완료에서 별도 관리한다.

### 3.5 Root/Path/Hygiene Operator

다음 Operator를 R4에 추가/보강했다.

- `preflight.ps1`
- `relocate-dry-run.ps1`, `relocate-apply.ps1`
- `transaction-id-refactor-dry-run.ps1`, `transaction-id-refactor-apply.ps1`
- `apply-overlay.ps1`
- `cleanup-dry-run.ps1`, `cleanup-apply.ps1`
- `workspace-garbage-dry-run.ps1`, `workspace-garbage-apply.ps1`
- `verify-overlay.ps1`

Relocation은 단순 Move가 아니다. 이동 후 PowerShell RepoRoot 계산, `cpf-tools/scripts/`, `cpf-tools/db/source/mariadb/`, Root 작업문서 참조를 canonical path로 같이 보정한다.

## 4. 이번 R4에서 발견했지만 즉시 삭제/완료 처리하지 않은 항목

### 4.1 MBR — 부분 구현

- DB Sample은 Golden 방향으로 정렬했으나 `cpf-member/bse`의 과거 회원/Auth Source가 남아 있다.
- 실제 Consumer inventory 후 Golden Generated Domain Source와 동일 계약으로 재생성/이관하고 과거 업무 전용 잔재를 정리해야 한다.
- 현재 상태를 Generator parity 완료로 판정하지 않는다.

### 4.2 ACC — 부분 구현

- 현재 `JdbcAccAccountRepository` 등 `acc_account` 실제 Consumer가 존재한다.
- DB를 즉시 `acc_sample_item`만 남기면 기존 성공 기능 회귀 가능성이 있으므로 R4에서 강제 삭제하지 않았다.
- Consumer 기능을 적정 Owner/Generated Domain/REF로 이관한 후 고정 ACC Module/Schema 제거를 완료해야 한다.

### 4.3 EXS/cpf-external — 부분 구현

- `cpf-external`에 external execution adapter/api/application/domain/port 구현이 실제 존재한다.
- 사용자의 최종 정책은 고정 EXS Domain 제거 + 필요 시 Generator 생성이다.
- 그러나 Consumer/기능 Owner 이관 확인 없이 Module/DB를 삭제하면 회귀하므로, 이관 후 삭제하도록 Current Request에 P0로 고정했다.
- 현재 Fresh Install의 exsDB 7개 Table은 **전환기 잔존**이며 최종 제품 목표 상태가 아니다.

### 4.4 cpf-core Package Ownership — 부분 구현

현재 `cpf-core/common` 아래 `admin`, `batch`, `edu`, `gateway`, `attachment`, `workflow` 등 다수 Package가 섞여 있다.

- R4에서는 transaction/header/logging/broker/database 경계의 명백한 문제부터 직접 교정했다.
- 대량 Package Move는 실제 Consumer/의존성/Compile 확인 없이 수행하면 회귀 위험이 크므로 하지 않았다.
- Codex는 각 Class를 Public API / SPI / Internal / Wrong Owner / Dead로 inventory하고 실제 Consumer를 추적해 `cpf-admin`, `cpf-batch`, `cpf-reference`, `cpf-gateway`, `cpf-common` 등으로 이관한다.
- 단순 Package rename만으로 완료 처리 금지.

### 4.5 ADM/BZA Runtime — 부분 구현

- DDL/API contract baseline은 보강됐지만 Approval Engine/API/UI/권한/Audit/Unknown-result recovery 전체 Runtime 연결은 아직 닫히지 않았다.
- ADM의 cross-owner direct JDBC/query 경계도 Owner Port 기반으로 교정할 부분이 남아 있다.

## 5. 직접 수행한 R4 정적검증

Package 생성 환경에서 직접 확인:

- UTF-8/NUL/zero-byte text 검사.
- JSON parse.
- Generated MyBatis XML template parse.
- Split DDL Table inventory: **124**.
- `00_empty_install.sql` Table 순서/개수: **124**, split과 일치.
- Source lifecycle ↔ MariaDB Vendor lifecycle byte parity.
- V38 checksum 및 Source ↔ Vendor migration parity.
- Product Seed에 `localhost`, `127.0.0.1`, `::1` 부재.
- Optional Reference Seed에 local fixture가 별도 존재함을 확인.
- active Source/SQL에서 legacy Global transaction identity 명칭 **0건**.
- Generated Domain SQL/Mapper 필수 계약 존재.
- Generator의 transport identity Body 중복 getter 잔존 **0건**.
- BZA 핵심 Governance DDL/Product Seed 존재.
- Operator 필수 Script 존재.

## 6. 직접 실행하지 못한 검증 — 반드시 미검증으로 유지

이 Package 생성 환경에는 PowerShell 7 및 실제 CPF 전체 Worktree/DB Runtime이 없으므로 다음은 직접 성공으로 판정하지 않는다.

- PowerShell parser/runtime 실행.
- `gradlew clean test`, assemble, quality/publication gate.
- MariaDB Fresh Provision -> Empty Install -> Product Seed -> Verify.
- V38 실제 Upgrade.
- Generated LNG/ING Source + DB bootstrap + CRUD + Remove + Regenerate.
- Local/Remote/Async/Retry/Timeout/Target-down transactionId E2E.
- ADM/BZA Runtime/OpenAPI/Browser.
- Batch/Worker/Center-Cut multi-instance/lease/fencing.
- PostgreSQL/Oracle/SQL Server/MySQL Vendor Runtime.

Codex는 실행하지 않은 검증을 완료로 올리면 안 된다.

## 7. R4 적용 순서

Package Root에서 다음 순서를 사용한다.

```powershell
Get-ChildItem .\operator\*.ps1 | Unblock-File

pwsh -ExecutionPolicy Bypass -File .\operator\preflight.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"

pwsh -ExecutionPolicy Bypass -File .\operator\relocate-dry-run.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
pwsh -ExecutionPolicy Bypass -File .\operator\relocate-apply.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -ConfirmApply

pwsh -ExecutionPolicy Bypass -File .\operator\transaction-id-refactor-dry-run.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
pwsh -ExecutionPolicy Bypass -File .\operator\transaction-id-refactor-apply.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -ConfirmApply

pwsh -ExecutionPolicy Bypass -File .\operator\apply-overlay.ps1 -RepoRoot "C:\dev\projects\jck\202412_01_CPF" -AllowDirty

pwsh -ExecutionPolicy Bypass -File .\operator\cleanup-dry-run.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
pwsh -ExecutionPolicy Bypass -File .\operator\cleanup-apply.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -ConfirmApply
```

Repository에서:

```powershell
cd C:\dev\projects\jck\202412_01_CPF
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\build-all-install-sql.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-transaction-id-standard.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-sql-canonical.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

Package Root에서:

```powershell
pwsh -ExecutionPolicy Bypass -File .\operator\verify-overlay.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

마지막에:

```powershell
git status
git --no-pager diff --check
git --no-pager diff --stat
git --no-pager diff
```

`workspace-garbage-*`는 Build/Test 후 로컬 산출물 정리가 필요할 때만 dry-run을 먼저 본 뒤 사용한다.

## 8. 사용자와 같이 할 다음 DB / Generator 검증

사용자가 기존 CPF DB를 직접 삭제한 뒤 **새 Reset Tool을 만들지 않고 기존 공식 초기화 Shell**을 사용한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -RequireRun
```

그 다음 임의 Domain `LNG` 같은 이름으로 생성형 Lifecycle을 검증한다. 정확한 순서는:

`cpf-docs/development/GENERATOR_DB_LIFECYCLE_TEST_GUIDE.md`

체크 포인트:

- 임의 DomainName/SystemCode가 하드코딩 없이 생성되는가.
- Domain 전용 DB/`${prefix}_sample_item`이 중앙 Vendor Template으로 생성되는가.
- CRUD/Search/Paging/Validation/Idempotency/Optimistic Lock가 DB까지 연결되는가.
- Gateway Header/URI executionId 호출 모두 가능한가.
- transactionId가 34자리 하나로 File Log / DB Log / ADM Timeline에서 추적되는가.
- Remove 후 Recreate 결과가 normalize parity를 만족하는가.

## 9. 다음 Codex의 첫 읽기 순서

1. `README.md`
2. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
3. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
4. `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
5. `cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md`
6. `cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md`
7. 이 문서
8. 실제 Git diff / Source / SQL / Test / Evidence

`cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`가 이번 실행의 가장 상세한 작업 명세다. 누락을 발견하면 최종 목표와 실제 구현을 근거로 Current Request를 보강한 뒤 진행한다.

## 10. 절대 금지

- 사용자 승인 없는 Commit/Push/Branch.
- dirty Worktree를 reset/clean/revert로 버리기.
- Historical Flyway V1~V37 수정.
- Module-local Vendor SQL/MyBatis fallback 복구.
- `transactionGlobalId` 개념 재도입.
- 같은 업무 흐름의 후속 호출에서 새 transactionId 생성.
- Generated Domain 코드에 MBR/ACC/EXS/LNG/ING 같은 특정 업무명 하드코딩.
- ACC/EXS 실제 Consumer 이관 전 물리 삭제.
- 문서/클래스/테이블 존재만으로 완료 처리.
- Stale Evidence를 현재 SHA의 성공 근거로 사용.
- 실행하지 않은 DB/Runtime/Browser 검증을 완료로 기록.
