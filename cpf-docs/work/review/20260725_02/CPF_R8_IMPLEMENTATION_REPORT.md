# CPF R8 Requirement-by-Requirement Implementation Report — 2026-07-25

- 기준 master: `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement: 162개 (Legacy Alias 8개 중복 제외)
- Commit / Push / Branch: ChatGPT가 수행하지 않음
- 완료 판정 원칙: 실제 Runtime/Evidence 없는 구현은 `완료`로 승격하지 않음

## 1~23 전체 검수 및 개발 결과

| # | 확인한 내용 | 확인된 미흡/위험 | R8에서 실제 개발·교정 | 현재 판정 |
|---:|---|---|---|---|
| 1 | Final Target, Current Request, R7/R8 Review/Gap/Handover 정본 관계 | 과거 고정 EXS 정책과 Generated EXS 정책이 섞이면 작업자별 반대 구현 위험 | Current Request를 Generated EXS 기준으로 유지하고 R8 162 matrix/Report/Handover 정본 생성 | 부분 구현 |
| 2 | MSA/Modular Monolith, Local/Remote, 복구·보안·운영·Generator 제품 정의 | 선언 대비 Owner Runtime 연결과 Evidence가 부족 | Batch/Center-Cut Owner Port + Local/Remote Adapter, 통합 Verification/Evidence Runner 보강 | 부분 구현 |
| 3 | Embedded/동일 JVM/분리 WAS/Admin/Agent topology | ADM이 BAT/REF DB를 직접 읽어 topology가 DB 결합에 묶여 있었음 | ADM→BAT Batch/Center-Cut를 Port로 전환. 동일 JVM은 Owner Bean, 분리 WAS는 ServiceCall Remote Adapter 사용 | 부분 구현 |
| 4 | Module/SystemCode/Generated Domain | fixed `cpf-external`, ADM의 MBR 업무 CRUD 잔재가 Generated Domain 정책과 충돌 | fixed EXS residue 제거 후 Generator `external/EXS`; ADM MEMBER 메뉴/API/Service/Seed 제거 Migration 추가 | 부분 구현 |
| 5 | Architecture/Dependency/Ownership | Core에 Batch Runtime이 집중되고 ADM이 Owner DB를 침범 | BAT Runtime/Scheduler/CenterCut Runner를 `cpf-batch`에 추가하고 BAT 소비 import를 전환. ADM direct owner DB 경계 제거 | 부분 구현 |
| 6 | `cpf-core` Public API/SPI/Internal과 기술 공통 범위 | `com.cpf.core.common.batch` legacy 실행 구현이 아직 호환 코드로 남아 있음 | 신규 BAT 실행은 BAT Owner로 이동, Core legacy Batch/CenterCut AutoConfiguration default OFF. Saga 기술 공통 Runtime 추가 | **부분 구현** — legacy Core Runtime class 물리 제거는 전체 compile 후 후속 |
| 7 | `cpf-common`/cmnDB 최소 공통 정책 | R8 직접 기능 변경 필요성이 확인된 항목 없음. 이전 검수의 부분 상태와 Runtime Evidence 부족 유지 | 보호 대상 유지, 새 업무 기능을 CMN/Core에 임시 적치하지 않음 | 부분 구현 |
| 8 | DB canonical/source/vendor/install/migration/rollback/drift | R7 push 후 `cpf-tools/db/source/mariadb`가 실제 Git에 잔존, V39 checksum ledger 누락, historical V6/V29 integrity unresolved | APPLY가 legacy source를 SHA 충돌 검사 후 `vendor/mariadb/source`로 이동. V39/V40/V41 checksum, V40 Saga, V41 ADM Member ownership migration 추가 | **실패/부분 구현** — V6/V29 원인 미확정 |
| 9 | Fixed/File/External/Messaging ownership | REF EDU Center-Cut 조회를 ADM이 refDB 직접 조회 | REF가 자기 DB를 읽는 Extension SPI 구현, BAT 표준 Center-Cut는 BAT가 소유 | 부분 구현 — REF 분리 WAS remote extension registry Evidence 필요 |
| 10 | 업무 채번 ownership | 공통 Runtime에 업무 채번을 넣으면 업무 규칙 결합 | BZA 기본 OFF 선택형 Sequence Sample + optional SQL pack + row lock + issue history + UI | 부분 구현 |
| 11 | Gateway/ServiceCall 경계 | 이번 R8의 주요 오류는 Gateway 자체보다 ADM→BAT 직접 DB 결합 | BAT Remote Adapter가 기존 ServiceCallEngine/Registry 경계를 사용. Gateway 기존 성공 기반은 보호 | 부분 구현 |
| 12 | Batch/Scheduler/Center-Cut/Agent | Core Batch runtime ownership, ADM Scheduler ownership, 독립 CenterCutRunner 미흡 | BAT Launcher/Heartbeat/Ghost/Lock/RuntimeListener/FileLog/Scheduler/CenterCut Runner/Registry/Stop/Rate 운영 구현을 BAT Owner로 추가 | 부분 구현 — multi-instance fault Evidence 필요 |
| 13 | ADM/BZA Backend 기능 | ADM Approval은 API/SPI/DDL만 있고 Runtime 부재. BZA Approval은 ALL-only legacy 로직, 조직/Role Snapshot 미연결 | ADM Approval Engine/API/UI. BZA 직급·직책·다중소속·조직책임·다중 Role, Versioned Approval/Delegation/Simulation/Snapshot/ALL·ANY·N_OF_M/optimistic lock 구현 | 부분 구현 |
| 14 | ADM/BZA Vue package | R7은 ADM 5개 coarse panel, BZA coarse feature file 수준. ADM operators 회귀 발견 | ADM 24 메뉴 각각 독립 lazy feature directory. BZA 27 route 각각 독립 feature directory. coarse panel/console 삭제 Gate. 외부 runtime asset 0 | 부분 구현 — npm/browser 미검증 |
| 15 | API Contract/OpenAPI | Owner API가 DB 직접 공유보다 명확한 Contract 필요 | `CpfBatchOperationsPort`, `CpfCenterCutOperationsPort`, Shared internal API, Approval/Directory API 추가. 기존 공개 API 호환 유지 | 부분 구현 — OpenAPI runtime 미검증 |
| 16 | Security/AuthZ/Audit/Dual Control | ADM Approval mutation에서 actor 누락 시 `SYSTEM` fallback 위험, BZA 다중 Role DDL과 인증 Runtime 단절 | ADM Approval actor required fail-closed, self-approval 방지, payload hash/idempotency/audit. BZA effective multi-role 권한 합집합 연결 | 부분 구현 — break-glass/MFA 등 잔여 |
| 17 | Generator/DevEx | EXS를 고정 모듈로 복구할 위험, R7 cleanup이 복사만으로 미반영 | APPLY가 fixed residue만 제거 후 Golden Generator로 `external/EXS -Apply` 생성/verify. Full Runner에 lifecycle smoke 옵션 | 부분 구현 — full lifecycle Evidence 필요 |
| 18 | Reliability/Observability | 결과불명을 일반 실패로 단정할 위험, Saga compensation/manual recovery 미구현 | BAT remote `UNKNOWN` 별도 Exception, durable Saga 역순 compensation/manual retry/manual resolve/audit 추가 | 부분 구현 |
| 19 | Install/Deploy/Compatibility | Build/DB/Browser 검증이 분산되어 누락 가능 | `verify-full-product.ps1`: static→DB sync→Gradle→npm→Generator→DB→Browser→post-hygiene, PASS/FAIL/SKIPPED 분리 | 미검증 |
| 20 | Repository/Document hygiene | Root compose, old db/source, R7 coarse UI, legacy ADM member/scheduler 잔재 | APPLY 삭제/이동 manifest, untracked root logs 조건부 제거, cleanup/post-cleanup Gate | 부분 구현 |
| 21 | Evidence/완료 판정 | 정적 PASS를 Runtime PASS로 오판 가능 | 162 상태를 허용 6개 상태로만 기록. Full Runner가 기준 SHA/명령/시각/Profile/출력/결과를 sanitized Evidence로 자동 저장 | 미검증 |
| 22 | Canonical 162 Requirement | 일부만 작업하고 전체 목록 상태가 누락되는 문제 | Legacy Alias 8개 제외 정확히 162개/unique 162개 matrix 생성 + Gate 추가. 이전 full review 상태와 최신 변경을 각 ID에 연결 | 부분 구현 |
| 23 | 최종 Product Gate | 검증을 사람이 여러 명령으로 나눠 수행하면 누락 가능 | 한 번에 실행하는 Full Verification Runner와 `-RequireAll` 강제 모드, 적용/검증 Handover 제공 | 미검증 |

## 이번 R8에서 새로 발견하고 실제로 고친 핵심 결함

1. R7의 이동/삭제가 Git에 실제 반영되지 않아 구 MariaDB source, Root compose, coarse UI가 잔존.
2. ADM `operators` 메뉴는 존재하지만 R7 coarse panel에 화면이 없어 기능 회귀 가능.
3. BZA DB 메뉴코드 `BZA_*`와 Frontend route 권한코드가 불일치하여 메뉴가 숨겨질 수 있음.
4. `bza_user_role` 다중 Role 테이블이 있어도 로그인 권한 계산은 단일 legacy role만 사용.
5. ADM Approval은 DDL/API/SPI만 있고 Runtime Engine이 사실상 부재.
6. BZA Approval 정책 테이블은 있었지만 Runtime은 ALL 중심 legacy line 로직에 묶여 있음.
7. Core Batch Runtime + ADM Batch Scheduler/direct batDB 접근이 Module Ownership을 위반.
8. ADM Center-Cut가 batDB/refDB를 직접 조회.
9. ADM이 Generated MBR 업무 데이터를 직접 CRUD하는 잘못된 Owner 기능 보유.
10. Saga compensation/manual recovery가 명시적 미구현.
11. 기존 R6.1 DB sync stale exit-code 문제는 최신 master에서 별도 `pwsh` process 방식으로 교정됨을 재확인.
12. 신규 REF Center-Cut query에서 DB 오류를 빈 목록으로 숨기지 않도록 fail-closed 처리.
13. 신규 Saga JDBC Store attempt counter가 상태 전이마다 증가할 수 있던 오류를 RUNNING/COMPENSATING 진입 때만 증가하도록 수정.

## 정적 검증 결과

- Java parser-level syntax: **0 error** / 69 files
- Pure Saga runtime: **`javac PASS`** / 12 files
- ADM TS/Vue script parse: **0 error** / 30 files
- BZA TS/Vue script parse: **0 error** / 30 files
- ADM route/menu: **24 / 24 / diff 0 / missing page 0**
- BZA routes: **27 / unique 27 / missing page 0**
- External frontend runtime asset: **0**
- Canonical Requirement: **162 / unique 162 / invalid status 0**
- Heuristic credential/secret hit: **0**

## 완료로 선언하지 않는 잔여 핵심 Gap

- `DB-MIGRATION`: historical V6/V29 checksum/integrity 원인 미확정 — **실패 유지**
- `DB-MULTI-VENDOR`: MariaDB 외 Platform Pack 실제 parity 미구현 — **부분 구현**
- Core legacy Batch Runtime class 물리 제거: BAT 전환 후 전체 compile/regression을 거쳐 제거 필요 — **부분 구현**
- REF/Generated Domain Center-Cut extension의 분리 WAS generic remote registration — **부분 구현**
- ADM/BZA Approval: break-glass, expiry/escalation, withdraw/cancel/resubmit, 조직개편·부재 시나리오 — **부분 구현**
- `PROD-MULTITENANT` — **미구현**
- Gradle/MariaDB/npm/Browser/Multi-instance/Fault/Evidence — 실제 사용자 환경 실행 전 **미검증**

세부 162개 판정은 `CPF_R8_REQUIREMENT_REVIEW.md`를 정본으로 사용한다.
