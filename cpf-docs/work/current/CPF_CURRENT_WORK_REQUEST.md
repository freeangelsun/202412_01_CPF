# CPF Current Work Request — C 개발/QA 관리_2_1

## 1. 성격 / 착수 상태

이 문서는 `C 개발/QA 관리_2_1`의 **현재 개발 착수 요청 정본**이다. 현재는 자료 취합·착수 리뷰 단계이며 제품 Source 수정은 시작하지 않는다. 사용자가 `개발 시작해줘`라고 요청한 뒤 최신 Local Working Tree Source Identity를 다시 계산하고, 본 목록의 동일 ID/순서를 실제 개발·Closure 기준으로 사용한다.

이전 DevGPT의 169행 일괄 `developer_status=완료`는 자동 승계하지 않는다. 기존 169행을 실제 Source/Consumer/Test/Runtime/Evidence와 전수 대조하고, 이번 세션에서 확인된 신규 P0/Finding과 후속 Steering을 병합하여 계획 Inventory는 **187행**으로 현행화한다.

## 2. Source Identity — 현재는 개발용 확정값이 아닌 분석 Baseline

- 분석 입력 ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_201728.zip`
- ZIP 내부 canonical `cpf-source-state.py --scope source` 재계산: `71675d0880399d45247fc34dfdfe46a07c19fdd98918870c1973e7e993ab702b` / **8,361 files / 43,258,931 bytes**
- 기존 C1_8 문서 기준: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af` / 8,340 files
- 사용자 2026-08-27 Full Runtime Evidence Source: `849f1b7f4b3aa65b79e67d543e018af29c7ddd96d2c588d74932deb6d7049850` / 8,444 files
- 세 값이 다르므로 과거 PASS/CLOSED는 자동 승계하지 않는다.
- **개발 착수 직전 최신 Local Working Tree에서 Source/Managed Identity를 다시 확정**하고 이 문서/Inventory/Evidence를 다시 bind한다.

## 3. 최신 Full Runtime 재분류

2026-08-27 통합 Runtime의 실제 최종 결과는 **PASS=135 / FAIL=8 / SKIP_ENV=0 / NOT_EXECUTED=9**이다.

### 3.1 FAIL 8

1. `NXT3_22` — `root-generated-domain-prefix`
2. `FRONTEND_TOOLCHAIN`
3. `GENERATOR_LIFECYCLE` — generated localqa build failure + UTF-8 mojibake 동반
4. `BATCH_TWO_WORKER_CRASH_UNKNOWN` — MariaDB runtime qualification credential/access failure
5. `GATEWAY_BATCH_RUNTIME` — runtime readiness failure + mojibake
6. `LOCAL_ONE_WAS_START` — `127.0.0.1:8080/actuator/health` timeout
7. `SOURCE_STATE_AFTER`
8. `MANAGED_STATE_AFTER`

### 3.2 NOT_EXECUTED 9

- `FRONTEND`
- `LOCAL_FILE_LOG_STANDARD`
- `LOCAL_DB_LOG_POLICY_RUNTIME`
- `LOCAL_INTEGRATED_LOG_CORRELATION`
- `ADM_RUNTIME_OPENAPI_RELEASE`
- `BACKOFFICE_RUNTIME_OPENAPI_RELEASE`
- `BROWSER_E2E`
- `PERFORMANCE_LIVE`
- `OPEN_GIT_ACTUAL_FRESH_RELEASE`

### 3.3 이미 실제 PASS했으므로 무조건 처음부터 반복하지 않을 기반

- Root Gradle assemble/build 흐름 및 SBOM stage는 위 Runtime에서 PASS한 구간이 존재한다.
- Generated `cpf-member` build도 PASS했다.
- Oracle/PostgreSQL/MariaDB DB3 Runtime stage 자체는 PASS했다.
- 다만 DB3는 새 Physical DB canonical set 요구로 DB Source가 변경될 예정이므로 **DB Consolidation 완료 뒤 영향범위를 재검증**한다.

## 4. Canonical Physical Runtime DB — 신규 P0 정본

허용되는 Active Physical Runtime DB는 정확히 다음 4개다.

1. `cpfDB` — CPF Platform 통합 DB
2. `mbwDB` — Backoffice(MBW) DB
3. `mbrDB` — Member(MBR) Generated Domain DB
4. `exsDB` — External(EXS) Generated Domain DB

다음 5개는 Current Active Physical Target으로 **0건**이어야 한다.

`cmnDB / admDB / batDB / bzaDB / refDB`

Released Migration의 immutable byte/checksum 또는 provenance/history metadata는 필요한 경우 보존하되 Current DB 생성/Schema/Connection/DataSource/Migration/Seed/Upgrade/Rollback/Runtime Query 대상으로 사용하지 않는다.

## 5. 신규/재개방 P0 Work Item

| ID | 우선 | 내용 | 완료 핵심 |
|---|---|---|---|
| WP-R00.15 | P0 | 최신 Source Identity 재기준화 | current docs/evidence/source-state 단일 identity |
| WP-R00.16 | P0 | Inventory/Work Request count 정합 | 187 unique rows, mirror mismatch 0 |
| WP-R00.17 | P0 | Current-only 정본/스크립트/가비지 History 정리 | 역할별 현행본 exactly-one, stale history delete manifest |
| WP-R02.12 | P0 | VSCode JDT nullness Warning 8건 + 잠복 동일패턴 | Fresh VSCode Error 0 / Warning 0 |
| WP-R10.12 | P0 | Physical DB exact 4-set Governance/Binding | active set exactly cpfDB/mbwDB/mbrDB/exsDB |
| WP-R10.13 | P0 | `cmnDB` active 제거 → `cpfDB` | active cmnDB 0 |
| WP-R10.14 | P0 | `admDB` active 제거 → `cpfDB` | active admDB 0 |
| WP-R10.15 | P0 | `batDB` active 제거 → `cpfDB` | active batDB 0 |
| WP-R10.16 | P0 | `bzaDB` active 제거/current MBW owner | active bzaDB 0, MBW→mbwDB |
| WP-R10.17 | P0 | `refDB` product physical 제거 | active refDB 0 |
| WP-R10.18 | P0 | Consolidation 이후 DB3 전체 Lifecycle | Vendor3 canonical target lifecycle PASS |
| WP-R14.08 | P0 | Runtime process boundary UTF-8 closure | Child→Parent→Tee→File/JSON/CSV 한글 roundtrip |
| WP-R14.09 | P0 | mojibake fail-closed Final Gate | Full Runtime/Fresh Replay mojibake 0 |


### 5.1 후속 Steering 추가 4개

| ID | 우선 | 내용 | 완료 핵심 |
|---|---|---|---|
| WP-R12.09 | P0 | 거래 DB Log 독립 Transaction/오류응답 Closure | 업무 rollback/handled 4xx·5xx/exception에도 REQUIRES_NEW DB log + request/response/error + fallback/recovery + One-WAS correlation |
| WP-R07.11 | P0 | Open Git Binary Default / Optional Source Profile | binary sources/javadoc 0, source allowlist-only, current-only, same sourceIdentity fresh release |
| WP-R07.12 | P0 | Open Git Bootstrap/Developer CLI Lifecycle | fresh binary bootstrap→domain-new→domain-sync→build→test→run/READY→stop→reset→replay |
| WP-R07.13 | P0 | Cross-Platform Java CLI | `cpf-cli.jar` single implementation + `bin/cpf|cpf.cmd|cpf.ps1` thin wrappers + Windows/Linux parity |
| WP-R07.14 | P0 | CPF Unified CLI Canonical Tooling Architecture | exactly-one `cpf-tools/runtime/cli` Java CLI + PUBLIC/INTERNAL Command Catalog + Generator/Initializer/Build/DB/Runtime/Verify/Publish/Release Engine consumers + duplicate entrypoint 0 |

## 6. VSCode 신규 8건 — 동일 Root Cause, 영향영역별 확인

`code=67109822` null type safety 경고를 단순 suppression하지 않는다.

1. `cpf-batch/worker/.../BatchOutboundHttpPolicy.java:84` — `Map.Entry::getValue`
2. `cpf-starters/base/.../CpfRuntimeLoggingLifecycle.java:83` — `Map.Entry::getKey`
3. `.../TransactionLogFallbackStore.java:176` — `EligibleFile::firstFailedAt`
4. `.../InMemoryCpfLogPolicyVersionStore.java:86`
5. `.../InMemoryCpfLogPolicyVersionStore.java:157`
6. `.../JdbcCpfLogPolicyVersionStoreHarness.java:256`
7. `.../JdbcCpfMaskingPolicyStoreHarness.java:230`
8. `.../InMemoryCpfMaskingPolicyStore.java:72`

Repository 전체에서 같은 method-reference/nullness 조합을 검색하고 명시 타입 lambda/comparator 등 JDT가 실제 null contract를 증명할 수 있는 형태로 보정한다. 정렬/필터/값 선택 의미를 바꾸지 않는다.

## 7. 재작업 최소화 실행 순서

### Stage 0 — 정본 / Identity / Inventory Freeze

- WP-R00 전체 재기준화
- 최신 Target Requirement + QA/보정 인수인계 + Inventory 187행 확정
- current-only canonical filename/owner 정책 확정
- Delete Manifest는 **아직 삭제하지 않고** replacement/precondition부터 확정

**이 단계 이후 구조/요건 ID를 흔들지 않는다.**

### Stage 1 — Java25 Compile / VSCode 0·0 Blocking Closure

- 기존 WP-R01 Build/Dependency cascade
- WP-R02 전체 + 신규 WP-R02.12
- 저비용 compile/targeted test로 API/Generic/JDT/nullness blocker를 먼저 제거
- Full Root Build는 매 Finding마다 반복하지 않고 blocker 묶음 수정 후 1회 실행

**이유:** 뒤의 DB/Generator/Runtime 개발 중 compile blocker로 반복 중단되는 것을 방지한다.

### Stage 2 — UTF-8 Process Foundation

- WP-R14.08/09를 먼저 구현
- nested pwsh/Java/Python/Node/Docker/DB client/native process의 encoding helper/contract 통합
- Child→Parent→Tee→File/JSON/CSV roundtrip + mojibake scanner

**이유:** 이후 모든 DB/Batch/One-WAS Runtime Evidence를 깨진 로그로 다시 생성하는 재작업을 방지한다.

### Stage 3 — Physical DB Canonical Model / Consolidation Source

- WP-R10.12~17
- `cmnDB/admDB/batDB/bzaDB/refDB` 전수 분류
- Canonical Source/Renderer/Migration Routing/Seed/Initializer/DataSource/Runtime Target/Repository Query를 owner별 canonical DB로 이전
- Released migration immutability 보존

**이 단계에서는 아직 최종 DB3 Full Replay를 하지 않는다.** Generator가 같은 DB Contract를 소비하도록 다음 Stage에서 먼저 맞춘다.

### Stage 4 — Generator / Generated Domain / Optional Domain Contract

- WP-R04 전체
- `mbrDB/exsDB` generated binding, fresh generation, idempotency, add/remove/rename/upgrade, user-owned protection
- WP-R08 optional capability/domain mutation도 Generator 구조와 함께 currentize

**이유:** DB3를 먼저 최종 검증한 뒤 Generator가 SQL/binding을 다시 바꾸는 재실행을 방지한다.

### Stage 5 — DB3 Physical Lifecycle Final for DB Changes

- WP-R10.18
- Oracle/PostgreSQL/MariaDB 각각 canonical 4-set 기준
- Fresh→Initializer→Migration→Seed→Runtime→Upgrade→Rollback/Recovery→Reapply→Fault/UNKNOWN→Cleanup→Replay
- active legacy DB 5종=0

### Stage 6 — Batch Runtime Closure

- WP-R05/R06/R11
- 5-role, Worker×2, claim/lease/fencing, kill/takeover, UNKNOWN/reconcile, Center-Cut, Gateway/ADM management path
- Batch DB owner는 canonical platform DB 모델을 소비

### Stage 7 — Security / Context / One-WAS / Logging

- WP-R09 + WP-R12 backend/runtime 영역
- Header6/SystemCode/session/auth/approval/audit/masking
- One-WAS actual transaction
- File↔DB↔Transaction/Segment/Timeline correlation
- Runtime OpenAPI

### Stage 8 — Frontend / Browser

- WP-R13
- Node/npm exact toolchain, lint/typecheck/test/build
- Backend Runtime OpenAPI generated client 재생성/consumer
- Browser E2E/a11y/responsive/401·403·404·409·429·500·503

**Backend OpenAPI가 안정된 다음 실행하여 generated client 재생성 반복을 최소화한다.**

### Stage 9 — Performance + Open Git Actual Fresh Release

- signed Performance Live/load/soak
- WP-R07 Open Git은 기본 `binary`, 명시적 `source` Profile을 같은 Current Source Identity에서 Fresh 생성한다. Binary에는 Framework 구현 Source/sources.jar/javadoc.jar 0건, Source Profile은 Canonical Public Source Allowlist만 허용한다.
- `cpf-cli.jar`가 단일 Cross-Platform CLI 구현을 소유하고 Linux `bin/cpf`, Windows `bin/cpf.cmd`/`bin/cpf.ps1`은 Java 실행 Thin Wrapper만 담당한다.
- Fresh Binary에서 `cpf bootstrap → domain-new → domain-sync → build → test → run/READY → stop → reset → fresh replay`, Source Profile에서 allowlist exact/leakage 0/source build/identity parity까지 실행한다.
- 최종 Source 구조가 안정된 뒤 Fresh Release를 만들어 재패키징을 최소화한다.
- `cpf-release/`는 Open Git 전달 전용 local-generated staging이며 Private CPF master Git/Source Identity에서 제외한다. Release Tool은 Private/Open Git 어디에서도 사용자 승인 전 `git add`/index staging/commit/push를 실행하지 않고 `VERIFIED`까지만 만든다. 모든 Gate PASS 후 사용자가 `cpf-release/open-git`을 검토하여 Open Git에 직접 Commit/Push한다. Private master Source Commit/Push와 Open Git Release Commit/Push는 분리한다.


### Stage 10 — Current-only Garbage / History Closure

- 모든 canonical replacement/consumer link가 확정된 뒤 WP-R00.17 실행
- 날짜형/중간/중복 Work Request, Handover, Codex instruction/result, Runtime command, stale Evidence index 및 obsolete script를 분류
- 필요한 현행 정보는 canonical current 파일에 병합
- 삭제 대상은 Root-relative `DELETE_MANIFEST.csv`에만 등록
- protected path/immutable migration/history metadata를 무작정 삭제하지 않음

### Stage 11 — Codex Credit Continuation — Source-changing Review를 Final Heavy Replay 전에

- 크레딧 강제중단 당시 마지막 `IN_PROGRESS/SOURCE_FIXED/VERIFICATION_PENDING` WP부터 continuation
- 이미 CLOSED + 동일 Source PASS Evidence는 반복 금지
- Codex가 Source를 수정하면 즉시 해당 영향 Targeted Gate를 실행
- 신규 WP를 무리하게 열지 않고 현재 WP 완결 우선

**목적:** Codex가 Source를 수정한 뒤 전체 Runtime을 또 처음부터 돌리는 횟수를 최소화한다.

### Stage 12 — Canonical Final Full Runtime / Fresh Replay

모든 Source-changing 작업 종료 후 한 번의 최종 최대강도 실행으로:

- Fresh VSCode 0/0
- Root build/test/publication/SBOM
- DB3 canonical 4-set
- Generator/Generated Domain
- Batch max runtime
- One-WAS/Logging/OpenAPI
- Frontend/Browser
- Performance/Open Git
- mojibake 0
- legacy active physical DB 0
- source/managed drift 0
- same-source Fresh Replay

을 닫는다.

## 8. Work Package 행 수 — 현행 계획

- **WP-R00: 17행**
- **WP-R01: 20행**
- **WP-R02: 12행**
- **WP-R03: 14행**
- **WP-R04: 13행**
- **WP-R05: 9행**
- **WP-R06: 7행**
- **WP-R07: 15행**
- **WP-R08: 8행**
- **WP-R09: 6행**
- **WP-R10: 18행**
- **WP-R11: 10행**
- **WP-R12: 9행**
- **WP-R13: 7행**
- **WP-R14: 9행**
- **WP-R15: 14행**

총 **188행**.

기존 169행은 삭제하거나 일괄 완료로 두지 않는다. 각 행의 `current_observation / development_scope / source_consumer_scope / static_high_intensity_acceptance / runtime_high_intensity_acceptance / documentation_currentization / garbage_cleanup / closure_rule`을 실제 Source와 대조한다.

## 9. Garbage / History 최종 원칙

최종 제품 Source에는 역할별 Current 정본을 한 본만 남긴다. 다만 삭제보다 **Current 내용 병합과 Consumer 이전이 먼저**다.

- Current canonical replacement가 없는 파일은 삭제 금지
- `cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`는 보호 경로로 취급
- Released DB migration byte/checksum/provenance는 history라는 이유만으로 삭제 금지
- 제품 Source의 obsolete compatibility wrapper/script는 실제 Consumer 0 + replacement PASS를 증명한 뒤 삭제
- 최종 `DELETE_MANIFEST.csv`는 `approved/user_approved/precondition/replacement_path/lifecycle`을 모두 검증
- 최종 결과물과 함께 **manifest 기반 안전 삭제 PowerShell 한 줄**을 제공
- 삭제 후 Source/Consumer/Build/Test/Runtime/Manifest/Hash를 재검증

## 10. 완료 판정

188행 전수 Completion Review에서 한 행이라도 Source/Consumer/Test/Runtime/Evidence가 미확인되면 완료가 아니다.

Final Hard Gate:

- `FAIL=0`
- mandatory `SKIP_ENV=0`
- mandatory `NOT_EXECUTED=0`
- unresolved `UNKNOWN=0`
- Fresh VSCode `Error=0 / Warning=0`
- `RUNTIME_MOJIBAKE_COUNT=0`
- `ACTIVE_PHYSICAL_CMNDB=0`
- `ACTIVE_PHYSICAL_ADMDB=0`
- `ACTIVE_PHYSICAL_BATDB=0`
- `ACTIVE_PHYSICAL_BZADB=0`
- `ACTIVE_PHYSICAL_REFDB=0`
- active Physical DB set exactly `cpfDB/mbwDB/mbrDB/exsDB`
- Source drift=0 / Managed drift=0
- Actual Open Git Fresh Release PASS
- Same Source Fresh Replay PASS
- Codex continuation/independent verification 상태와 Developer Inventory 정합
- Current-only garbage/history closure PASS

위 조건 전부 충족 전 Overall 완료 선언 금지.
