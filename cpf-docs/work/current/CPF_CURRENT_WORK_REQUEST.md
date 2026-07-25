# CPF 차기 Codex 통합 작업 요청서

## 0. 기준선과 작업 성격

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 Commit: `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482`
- 최상위 목표: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Requirement 연속성: `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
- 장기 결정: `cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md`
- PC/계정 인수인계: `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`

이 작업은 Greenfield 개발이 아니다. 현재 Source에는 Service Call, Header/Trace, Broker Reliability, Fixed-Length Core, DB Vendor Pack, Generator, Batch/Center-Cut, ADM/BZA 등의 기존 구현과 WIP가 함께 있다. 각 기능을 다시 만들기 전에 반드시 `유지 / 보완 / 확장 / 교체 필요 / 제거 필요` 중 하나로 판정하고 기존 Consumer와 Evidence를 확인한다.

## 1. 절대 가드레일

1. 사용자 명시 승인 전 commit/push/branch 생성 금지.
2. 현재 Worktree를 reset/clean/revert/checkout으로 폐기하지 않는다.
3. 삭제된 Module-local Vendor SQL/MyBatis pack 또는 Stale Evidence를 오류 회피 목적으로 복구하지 않는다.
4. Historical Flyway Migration은 checksum 원인 확정과 명시 승인 없이 수정·재작성·삭제하지 않는다.
5. 실행하지 않은 검증을 완료로 기록하지 않는다.
6. Source/Class/Table/Swagger/정적 검색만으로 완료 처리하지 않는다.
7. Secret/Password/Token/Private Key/개인정보 원문을 Source, Log, Evidence에 남기지 않는다.
8. 다른 Module Owner DB를 직접 수정하는 신규 코드를 만들지 않는다.
9. ADM/BZA Approval을 하나의 공통 업무 Engine/Table로 합치지 않는다.
10. 현재 오류를 줄이기 위해 최종 Architecture와 충돌하는 호환 fallback을 다시 추가하지 않는다.

## 2. Cross-PC / Cross-Account Handover Gate — 매 작업 시작·중단·종료 필수

CPF는 회사 PC와 집 PC, 여러 Codex 계정/세션이 번갈아 작업할 수 있다. Continuity 파일에 없다는 이유만으로 과거 작업이 없었다고 판단하지 않는다.

### 시작 시

1. `git status`, HEAD, `origin/master`, ahead/behind를 확인한다.
2. Final Target → Continuity Ledger → Current Request → Decision Log → Continuity State → 실제 Git Diff/Evidence 순으로 대조한다.
3. Continuity State의 과거 회사 PC/집 PC 작업 중 Git에서 확인되는 누락을 즉시 보강한다.
4. Local DB/Runtime 상태는 PC별 상태로 구분한다. 한 PC의 Runtime 성공을 다른 PC의 성공으로 승계하지 않는다.
5. 이번 세션에서 이어받는 Requirement ID와 정확한 중단점을 기록한 뒤 구현을 시작한다.

### 의미 있는 Checkpoint마다

- 시작/종료한 Requirement ID
- 유지/보완/확장/교체/제거 판정
- 실제 변경 파일/Module
- 선택한 구현 Architecture와 이유
- 검토한 대안과 선택하지 않은 이유
- 실제 실행 명령과 결과
- DB/Runtime 상태
- 미검증/실패/Blocker
- 다음 PC/계정이 첫 번째로 할 작업
- 다시 하지 말아야 할 완료 작업

을 `CPF_CODEX_CONTINUITY_STATE.md`에 갱신한다. 장기 Architecture 결정만 Decision Log에 기록한다.

### 중단/크레딧 종료 전

새 작업을 확장하지 말고 안전한 중단점까지 마무리한 후 Continuity State를 반드시 갱신한다. 미커밋 변경을 삭제하지 않는다.

## 3. 구현 보고 형식 — Requirement별 필수

최종 보고에서 각 주요 Requirement마다 다음을 빠짐없이 작성한다.

1. Requirement ID
2. 기존 구현 상태와 실제 Consumer
3. `유지/보완/확장/교체/제거` 판정
4. 발견한 문제와 제품 위험
5. 실제 적용한 Architecture
6. Owner Module/Package
7. Public API / SPI / Internal 경계
8. SQL/Config/Migration 영향
9. 실제 Consumer와 연결 방식
10. 구현 방식을 선택한 이유
11. 검토한 대안과 채택하지 않은 이유
12. 호환성/Migration/Rollback
13. 실제 실행한 Unit/Integration/Runtime/Browser/DB 검증
14. Evidence 경로와 기준 Commit
15. 미검증/잔여 Gap
16. 변경 파일
17. 현재 구현이 CPF 장기 제품 구조에서 최선이라고 판단한 근거

Codex 주장보다 실제 Git/Runtime을 우선한다.

## 4. P0 — DB 정본 단일화와 Empty Install 재구축

### 현재 문제

검수 기준 Commit에서 최신 split DDL 107 Table과 generated `00_empty_install.sql`/중앙 MariaDB pack 115 Table이 불일치했다. 본 Overlay는 실제 누락된 ADM/BZA 요구 DDL을 추가하여 split/generated를 정적 123 Table로 재생성했지만 Runtime은 아직 미검증이다. MBR/BZA 구 테이블이 generated bundle에서 다시 생성될 수 있었고 최적화 전 127-table Runtime Evidence를 최적화 후 구조의 성공 근거로 사용할 수 없다.

### 목표

`cpf-tools/db/vendor/mariadb/source/10_*~45_*` split DDL을 MariaDB Platform Schema 설계 정본으로 사용하고 `cpf-tools/scripts/build-all-install-sql.ps1`이 같은 Vendor 경계의 install/seed/migration/verify lifecycle artifact를 결정적으로 재생성한다. 다른 Vendor도 동일 `cpf-tools/db/vendor/<vendor>/source` 계약을 사용하며 미구현 Vendor는 fail-closed한다.

### 필수 작업

- Split DDL → `00_empty_install.sql` → Central MariaDB install exact parity.
- `99_smoke_check.sql`과 `00_verify.sql`을 현재 Schema와 일치시킨다.
- Product Seed/Optional Sample/Test Seed 분리를 유지한다.
- Fresh MariaDB에서 Reset 없이 첫 Empty Install 성공.
- 이후 allowlist Reset dry-run/apply → Reinstall까지 검증한다.
- 실제 Schema/Table/PK/FK/UK/Check/Index/Object 수를 Evidence로 남긴다.
- `mbrDB`는 `mbr_sample_item` 최소 Reference 구조와 실제 Java/Mapper가 일치해야 한다.
- ACC는 기존 계좌 업무 원장 2개를 계속 유지할지 Minimal Transaction `acc_sample_item`로 전환할지 Final Target 기준으로 교정하고 Generator Reference와 동일 논리 Template을 증명한다.

### 금지

- generated bundle을 손으로 따로 고쳐 split DDL과 이중 정본화
- 과거 127-table Evidence 재사용
- DB가 비어 있다는 이유로 Historical Flyway checksum 파일 수정

## 5. P0 — Central Multi-Vendor Pack 단일 Runtime 경로

### 목표 Architecture

`cpf-tools/db/vendor/<vendor>`가 Physical Vendor SQL의 유일한 제품 정본이다. Java Controller/Service/Domain/일반 Repository 계약은 Vendor-neutral이다.

```text
cpf.db.vendor + cpf.db.resource-root
        ↓
검증된 pack.json
        ↓
runtime/<owner>/mybatis + runtime/<owner>/repository
install / seed / migration / verify / rollback
```

### 필수

- 동봉 cleanup으로 제거된 Module-local `src/main/resources/sql/vendor/**`, `mybatis/vendor/**`를 복구하지 않는다.
- `CpfSqlResourceResolver`, `CpfVendorSqlCatalog`의 Production 경로는 중앙 pack이 없으면 fail-fast 한다.
- `CpfDatabaseVendor.flywayLocation()` 같은 legacy classpath 경로는 신규 Consumer가 사용하지 않는다. 기존 Consumer를 inventory한 뒤 `CpfSqlResourceResolver.flywayLocation(Environment)` 또는 동등한 중앙 Pack selector로 이관하고 호환 API 제거 시점을 보고한다.
- 배포/테스트/Generator가 `cpf.db.resource-root`를 명시적으로 제공한다.
- 동봉된 `cpf-tools/scripts/runtime-start-services.ps1`는 로컬 통합검증 Harness에서 `CPF_DB_VENDOR`/`CPF_DB_RESOURCE_ROOT`를 Java child process에 명시적으로 주입한다. 제품 배포는 배포환경이 외부 Vendor Pack 경로를 명시적으로 제공해야 하며 Source-tree fallback을 만들지 않는다.
- Source tree를 Vendor 선택 때문에 덮어쓰거나 Git Diff를 발생시키지 않는다.
- MariaDB는 실제 실행 검증. MySQL/PostgreSQL/Oracle/SQL Server는 실제 환경 없으면 미검증.
- 다른 Vendor SQL을 MariaDB 파일 rename/copy만 해서 완료 처리하지 않는다.

## 6. P0 — ADM Owner Boundary 교정

현재 ADM이 `batDB`/`refDB` 등 Owner DB를 직접 조회·수정하는 구현이 있다. 이는 Modular Monolith에서는 동작할 수 있어도 분리 WAS/Microservice에서 Owner credential과 DB coupling을 만든다.

목표:

```text
ADM UI/API
  ↓
ADM Operations Application Service
  ↓
Owner Query/Command Contract
  ↓
Local Facade 또는 Remote Facade
  ↓
cpf-batch / cpf-reference / Generated Domain(external/EXS 포함) / 해당 Owner
```

- ADM에서 Owner DB 직접 UPDATE/DELETE/INSERT 제거.
- Query도 운영 성능상 별도 Read Model이 필요한 경우를 제외하면 Owner Query API 사용. Read Model을 도입하면 명시적 projection/event ownership과 freshness를 정의한다.
- 위험 Command는 ADM Approval 완료 후 Owner Command API를 호출한다.
- Local/Remote Contract parity와 표준 Header/transactionId/Audit를 유지한다.

## 7. P0 — ADM 운영자 Directory와 플랫폼 위험조치 Approval

동봉 DDL은 최종 Engine을 대신하지 않는 Architecture Baseline이다. 실제 Java/API/UI/Runtime을 완결한다.

### Identity/Directory

- `adm_operator`는 Authentication Identity.
- 조직/사번/직급/직책/외부 Directory Subject는 Profile 경계.
- 기본 DB Adapter + LDAP/AD/IAM/HR 확장 Port.
- 조직정보 변경 이력/유효기간 또는 Approval Snapshot으로 감사 재현성을 보장한다.

### Approval

지원 대상: Service 차단/재개, Routing 강제변경, Batch 강제 종료/재시작, Center-Cut Failed-only 재처리, Unknown Result 수동 확정, Compensation, DLQ Replay, 대량 Download/Unmask, Runtime Config, Credential/Certificate Rotation, Break-glass.

필수:

- Policy Version/effective dates
- Operator/Role/Organization Target
- Sequential/Parallel + ALL/ANY/N_OF_M
- requester != approver 기본
- self approval block / SoD
- TTL/expiry/cancel/reject
- immutable command payload hash/snapshot
- idempotency/version concurrency
- Owner Command execution result와 Unknown 상태
- immutable history/audit
- Break-glass TTL + post review
- OpenAPI/JavaDoc/권한 Seed/UI

## 8. P0 — BZA 조직/직원/다중 Role/업무결재 (`BZA-ORG`, `BZA-APPROVAL`, `SEC-APPROVAL`)

동봉 DDL에서 조직/직원/직급/직책/Assignment와 Approval Policy/Participant 기반을 보강했다. 기존 `approval_mode`, `decision_rule`, `delegated_approver_no`가 컬럼만 존재하고 Engine에서 무시되는 상태를 완료로 보지 않는다.

Overlay는 기존 사람별 direct-line INSERT를 새 `target_type/target_code` 계약에 맞추고 현재 실제 구현 가능한 `ALL`만 fail-closed로 허용하도록 최소 보정했다. 이것은 ANY/N_OF_M/부서합의 Engine 구현이 아니며, 기존 API를 임시로 녹색화하기 위해 다른 규칙을 ALL처럼 처리해서는 안 된다.

### 조직/직원

- Organization hierarchy
- Position / Job Title
- Employee Assignment with effective date
- 복수 조직/겸직/파견
- `bza_user_role` 복원 후 다중 Role + valid-from/to
- 조직개편 후 과거 Approval Snapshot 불변

### Approval Engine

- APPROVAL / AGREEMENT / REVIEW step type 구분
- EMPLOYEE / ROLE / ORGANIZATION / ORG_MANAGER / POSITION target
- SEQUENTIAL / PARALLEL
- ALL / ANY / N_OF_M
- 필수/선택 부서
- 부서 전원 합의 / 1명 이상 / M-of-N
- Delegation/Acting/Absence with validity
- policy version/effective dates
- participant resolution snapshot
- optimistic locking/concurrency/idempotency
- reject/withdraw/cancel/resubmit/expire/escalation
- policy/route simulation API + UI
- OpenAPI/JavaDoc/Audit/Evidence

정책 테이블만 만들고 기존 `BzaBackofficeService`의 사람별 line 로직을 그대로 유지하면 완료가 아니다.

## 9. P0 — Batch/Center-Cut Ownership

- Runtime owner는 `cpf-batch`.
- `cpf-core`에는 배포 topology와 무관한 작은 Contract/SPI만 남긴다.
- 독립 `CenterCutRunner` Runtime을 제공한다.
- immutable parameter snapshot, Job/Item/Attempt, claim/lease/fencing, global TPS/backpressure, primary/secondary agent, failed-only reprocess, unknown result resolution, compensation, Java/Shell/File executor를 실제 Runtime으로 연결한다.
- Multi-instance 장애 주입 Evidence가 없으면 완료 금지.

## 10. P0 — Requirement 연속성 복구

이번 Overlay에서 Canonical Count를 162개로 정본화했다. `CPF_REQUIREMENT_CONTINUITY_LEDGER.md`를 삭제하거나 다시 Count를 축소하지 않는다. Legacy Alias는 중복 집계하지 않는다.

특히 복구된 `DB-MULTI`는 Multi Datasource/Read Replica이며 `DB-MULTI-VENDOR`와 다른 요구다. `CPF-LOGFAIL`, `ADM-SERVICE`, `ADM-LOG`, `OPS-MAINT`, `DATA-RETENTION`, `API-LIMIT`, `RULE-*`, `REQ-GAP` 등 복구 Requirement도 Final Target에서 계속 추적한다.

## 11. P0 — EXS Generated Domain 정본화

최상위 목표의 확정 정책은 `EXS`를 고정 Platform Module/SystemCode/DB로 제공하지 않는 것이다. `cpf-external`, `exsDB`, `45_external_schema.sql`, `57_external_seed_data.sql`을 기본 제품 정본으로 복구하지 않는다.

### 필수

- EXS가 필요한 프로젝트에서는 다른 Generated Domain과 동일하게 `create-domain.ps1 -DomainName external -SystemCode EXS -Apply`로 생성한다.
- EXS 이름 전용 switch/if/template을 Generator에 추가하지 않는다.
- Platform default install/product seed/optional seed/schema manifest에는 EXS Object 0건을 유지한다.
- 대외연계 공통 기술 Contract/SPI는 `cpf-core`, 고객 공통 정책은 `cpf-common`, EDU는 `cpf-reference`, 기관별 Adapter/업무 데이터는 생성된 EXS 또는 해당 업무 Domain이 소유한다.
- Generator의 collision 검사는 특정 MariaDB source 파일을 하드코딩하지 않고 schema/domain metadata를 사용한다.
- create → verify → DB bootstrap → build/test → remove → regenerate parity를 Evidence로 남긴다.

### 완료 금지

- 옛 `cpf-external` Source를 수동 복구
- EXS 전용 Platform Table을 중앙 install에 재추가
- EXS를 Generated Domain이라 부르면서 Generator와 다른 구조를 유지
- External EDU seed를 EXS product seed로 되돌림

## 12. P1 — Migration/Upgrade/Rollback

- V6/V29 checksum 불일치 원인을 파일/manifest/Git history 기준으로 확정.
- 임의 checksum 맞추기 금지.
- Pre-release rebaseline이 필요하면 적용 이력 없음과 모든 개발 DB 폐기 가능성을 증명하고 사용자 승인 요청.
- Empty Install과 Upgrade path를 별개로 검증.
- Rollback 불가능 변경은 forward-recovery 전략과 backup/restore를 명시.

## 13. P1 — Protected Baseline 회귀

다음 기존 기반은 Greenfield 재작성하지 않는다. 현재 구현을 먼저 검증하고 Gap만 보완한다.

- Service Call Engine/Registry/Health/Routing/Failover/Circuit
- Standard Header/transactionId/segment/trace
- Broker Outbox/Inbox/DLQ/Replay 기반
- Idempotency/Reconciliation 기반
- Core Fixed-Length API/SPI
- File/Attachment/Logging/Masking 기반
- Generator Metadata + Vendor Domain Template 방향
- cmnDB minimal sample 정책

## 14. P1 — Generator Lifecycle

임의 `${DomainName}` + `${SystemCode}`가 고정 Domain 목록 수정 없이 생성되어야 한다.

- Metadata collision/예약 Code 검증
- central vendor domain-template
- 최소 `sample_item` 논리 계약
- CRUD/search/offset/slice/cursor/validation/duplicate/optimistic lock/commit/rollback
- Local/Remote Call/Header/transactionId/Error/Idempotency/Audit EDU
- create → verify → db-init → build/test/runtime → remove → regenerate → parity

MBR/ACC/REF/PAY/INS 이름 switch/if 추가가 필요하면 설계 실패다.

## 15. P1 — Security/Operations/Product Requirements

Final Target 162개 Catalog 전체를 Source/API/SQL/Test/Evidence와 다시 대조한다. 복구 Requirement를 신규로 재명명하지 않는다. 특히 `SEC-APP`, `OPS-SELF`, `OPS-TOPOLOGY`, `OPS-MAINT`, `DB-PERF`, `DATA-LINEAGE`, `DATA-RETENTION`, `API-LIMIT`, `RULE-ARCH/SEC/QUALITY`를 누락하지 않는다.

## 16. 검증 순서

1. `git diff --check` / secret scan / UTF-8 / repository hygiene
2. central vendor pack parity 및 module-local vendor resource 0건
3. SQL bundle 재생성 후 split/generated/central exact parity
4. Clean compile + unit/integration tests
5. MariaDB allowlist reset dry-run
6. Fresh Empty Install + Product Seed + Verify
7. Runtime module startup
8. Local/Remote Service Call
9. Generator arbitrary domain lifecycle
10. Batch/Center-Cut multi-instance/failure/recovery
11. ADM/BZA API + Production frontend build + Browser E2E
12. Approval concurrent/idempotent/SoD/delegation/department agreement scenarios
13. Upgrade/Rollback/Recovery
14. OpenAPI/JavaDoc/Guide/Evidence consistency
15. Protected Baseline regression

## 17. 완료 금지 조건

- 중앙 pack이 있는데 Module-local vendor fallback이 남아 있음
- generated SQL과 split SQL이 다른 Schema를 만듦
- MBR/ACC sample DDL과 Java Mapper가 다른 Table 사용
- ADM이 Owner DB를 직접 갱신
- `approval_mode`, `decision_rule`, delegation 컬럼만 존재하고 Engine 미사용
- Department Agreement/ALL/ANY/N_OF_M Runtime Test 없음
- CenterCutRunner 없이 Batch Job만으로 Center-Cut 완료 주장
- MariaDB 성공을 다른 Vendor 완료로 확대
- 이전 PC Evidence를 현재 PC 실행으로 기록
- 존재하지 않는 Evidence 경로를 Index가 참조
- Stale Evidence 또는 삭제한 Vendor resource를 복원
- Requirement ID를 Mapping 없이 삭제/rename
- 실행하지 않은 Browser/DB/Broker/Failure 검증을 성공 표기

## 18. 제출 Evidence

Evidence는 기준 Commit, PC/환경, Profile, DB Vendor, 시작/종료시각, 실행 명령, Requirement ID, 결과, 민감정보 제거 여부, Stale 여부를 포함한다. 원시 Secret/PII를 저장하지 않는다.

최종 보고는 본 요청서 3장의 Requirement별 구현 보고 형식을 따른다.

## 19. 2026-07-25 사용자 추가 필수 범위

1. MariaDB만 별도 `cpf-tools/db/source/mariadb`를 갖는 비대칭을 제거하고 Vendor별 동일 `cpf-tools/db/vendor/<vendor>` ownership으로 정리한다.
2. Root `docker-compose.local.yml`을 `deploy/local`로 이동하고 Root hygiene를 재검증한다.
3. `cpf-tools` 사용/책임/DB/Generator/검증/안전 규칙을 설명하는 공식 Guide를 완성한다.
4. 본 요청서 일부만 구현하고 종료하지 않는다. Final Target 전체 Catalog와 본 요청서를 Requirement 단위로 순회하며 상태를 갱신한다.
5. ADM/BZA Vue 화면을 App Shell + feature package + route registry + state/API boundary + code splitting 구조로 완료한다. 외부 CDN/remote CSS/font/icon Runtime 의존 금지.
6. ChatGPT/Codex가 서로 교차 검수할 수 있도록 Requirement별 구현 Report와 실제 잔여 Gap을 남긴다.
7. 작업 종료 시 Current Request, Continuity State, Decision Log(장기 결정만), 검수 Report, 다음 첫 작업을 갱신하고 Root/log/build/temp garbage를 제거한다.

## 20. 현재 우선 중단점

`20260725_01`에서 R6.1 DB Artifact sync가 하위 PowerShell script 성공에도 stale `$LASTEXITCODE`를 읽어 실패했다. DB Runtime/BAT 재설치는 아직 미완료다. 다음 구현은 이 gate를 먼저 수정하고 sync PASS를 확인한 뒤 fresh/partial DB 상태를 다시 검증해야 한다.

## 21. R7 정적 구현 Checkpoint (2026-07-25)

본 절은 완료 선언이 아니라 다음 작업자가 반복 구현하지 않도록 남기는 현재 상태다.

| Requirement | 상태 | R7 적용 | 다음 완료 조건 |
|---|---|---|---|
| DB canonical/vendor ownership | 부분 구현 | MariaDB source를 `vendor/mariadb/source`로 이동, 모든 Vendor 동일 source root 계약 | MariaDB sync/runtime PASS + 타 Vendor Platform pack 실제 구현/검증 |
| DB artifact sync | 부분 구현 | child gate separate pwsh process로 stale exit code 문제 교정 | 사용자 Windows에서 sync 실제 PASS |
| DB schema metadata gate | 부분 구현 | multiline FK 96건 포함 local/referenced table/column 검증 보강 | 실제 PowerShell manifest 재생성 및 drift PASS |
| EXS Generated Domain | 부분 구현 | fixed residue 금지 + Generated `external/EXS` 허용/검증 | Generator create/build/test/db/remove-regenerate Evidence |
| Root hygiene | 부분 구현 | compose deploy 이동, untracked root logs 조건부 제거, .gitignore 주석 정리 | `check-repository-hygiene.ps1` 실제 PASS |
| cpf-tools 문서 | 부분 구현 | Tool README + 상세 Guide | 링크/명령 실제 실행 재검증 |
| ADM frontend packaging | 부분 구현 | App Shell + 5 lazy feature package + shared state/API methods | npm verify + Browser E2E + 권한/위험조치 검증 |
| BZA frontend packaging | 부분 구현 | console.ts 제거 + lazy route registry + auth/API + reusable components | npm verify + Browser E2E + CRUD/approval/session 검증 |
| DB Runtime/BAT V39 | 미검증 | Source/V39은 기존 master에 존재 | partial batDB 정리 후 `-All -RequireRun` Evidence |
| P0 ADM/BZA/Batch 전체 기능 | 부분 구현 | 기존 master 구현 보호 + frontend/DB policy 보강 | 본 요청서 6~9장 Runtime/Evidence closure |
| P1 migration/security/regression/generator | 부분 구현 | guardrail 유지 | 본 요청서 12~15장 순차 실행 |

다음 작업자는 위 표의 `부분 구현`을 `완료`로 임의 승격하지 않는다.


## R8 이후 다음 작업/검수 필수 범위

1. `CPF_R8_REQUIREMENT_REVIEW.md` 162개 항목을 정본으로 사용해 상태를 하나씩 실제 Evidence로 승격한다.
2. R8 APPLY/Full Verify 실패 항목은 최우선으로 수정하며, 실패 검증을 삭제/우회하지 않는다.
3. Historical V6/V29 migration integrity를 원인 확정 없이 수정하지 않는다.
4. ADM/BZA Approval의 break-glass, expiry/escalation, withdraw/cancel/resubmit, 조직개편·부재 Snapshot 시나리오를 완성한다.
5. non-MariaDB Vendor parity와 PROD-MULTITENANT는 명시 잔여 Gap이며 완료로 오판하지 않는다.
6. 작업 후 Source/API/SQL/Test/Runtime/Browser/Evidence를 다시 162 Requirement에 양방향 연결하고 다음 Handover를 갱신한다.

## 22. R8 구현 후 검증·잔여 Gap 고정 (2026-07-25)

R8는 다음 P0/P1 구현을 추가했으나 **실제 Windows/Java25/MariaDB/Browser Evidence 전에는 완료가 아니다.**

- ADM 24개/BZA 27개 기능별 lazy frontend package
- ADM Approval runtime/API/UI, BZA 조직/다중 Role/Approval runtime/API/UI
- BAT Batch runtime/Scheduler/CenterCut Runner Owner 이동 및 ADM Owner Port 경계
- ADM legacy MBR 업무 CRUD 제거
- Saga compensation/manual recovery
- 선택형 BZA 업무 채번 Sample
- R7 cleanup/move 강제 APPLY
- Canonical 162 Requirement Gate
- 통합 Full Verification + sanitized Evidence 자동 보존

### 다음 작업자가 반드시 먼저 할 일

1. `APPLY_R8_20260725.ps1` 적용 후 `git status --short`, `git diff --check`를 저장한다.
2. Static Runner를 실행하고 실패 Gate를 우회하지 않는다.
3. Gradle/npm build 전에 `cpf-tools/db/source`/Root compose/R7 coarse UI/ADM direct owner DB residue가 0인지 확인한다.
4. DB Artifact sync를 실제 실행한다.
5. 직전 부분 생성 가능성이 있는 `batDB`는 원인 확인 후 **batDB만** 정리하고 `-All -RequireRun`한다.
6. `external/EXS`가 Generator ownership manifest를 가진 Generated Domain인지 확인한다.
7. Full Runner `-WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll`을 실행하고 generated Evidence를 보존한다.
8. `CPF_R8_REQUIREMENT_REVIEW.md` 162개 상태를 Evidence로만 승격한다.

### 완료로 오판하면 안 되는 현재 잔여

- historical V6/V29 migration integrity
- non-MariaDB Platform vendor parity
- Core legacy Batch compatibility class 물리 제거
- REF/Generated Center-Cut extension의 generic 분리 WAS remote routing
- Approval break-glass/expiry/escalation/withdraw/cancel/resubmit/조직개편·부재 시나리오
- PROD-MULTITENANT
- 실제 multi-instance/fault/browser/runtime Evidence
