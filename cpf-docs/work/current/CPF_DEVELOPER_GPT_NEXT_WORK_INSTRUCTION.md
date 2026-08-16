# 통합 개발·검수 추가 강제 지침

이번 마무리 작업은 Source 수정과 1차 개발검수, 개발요건/Architecture/Current 문서 현행화를 분리하지 않는다. 발견한 결함은 동일 Root Cause 전체 영향까지 검색해 Source·Consumer·Config·Generator·Test·문서를 같은 변경 단위에서 닫는다.

횡단 Acceptance는 Golden Path, Execution Lifecycle, Common Product Service, Operational Journey, Generator-first DX, Open Extension/Native Escape 여섯 관점이다. 기능 존재만으로 PASS하지 않고 실제 Consumer·실패/UNKNOWN·복구·운영 추적까지 확인한다.

문서 Current 영역에는 동일 역할의 세션별/버전별 복제본을 남기지 않는다. Git History 보존 여부에 의존하지 말고, 살아 있어야 할 요구·결정·재검증 범위를 canonical 파일에 먼저 병합한 뒤 stale 문서는 Delete Manifest로 제거한다.

# CPF Developer GPT — Integrated Full Redevelopment Mandatory Development Instruction

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실행 시 최신 `origin/master` exact SHA를 재확인한다.
> Evidence rule: 이전 실행 결과를 successor PASS로 승계하지 않고 현재 execution SHA에서 재생성한다.
> Delivery model: **단일 Full-Scope / Phase 분할 전달 금지**
> 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

# 0. 실행 선언

이번 회차는 “일부 blocker 수정”이나 “문서/Interface/Sample 추가”가 목적이 아니다.
최신 master의 실제 Source Gap을 닫고 CPF를 상용 Framework Release Candidate에 수렴시키는 회차다.

Developer GPT는 다음 네 범위를 모두 한 번에 유지한다.

1. 최신 독립검수 F-001~F-016 — 최우선.
2. Configuration/Invocation 독립검수 CFG-001~CFG-024 — 동일 회차 P0/P1 closure.
3. Commercial Hardening 40대 전체.
4. Repository one concept + Annotation/Golden Path + Starter Common Functions + Batch DX + Logging/Transaction/Cache 등 이번 사용자 요구.

중간 Checkpoint는 진행 보존일 뿐 Scope 축소가 아니다.
P0/P1/P2는 실행 우선순위이며 별도 개발 패키지로 쪼개지 않는다.


# 0A. 진행률 / 연속 실행 강제

- 사용자가 별도 중단을 요청하지 않는 한 **중간보고 후 멈추지 않는다**.
- 최소 5분 간격으로 실제 완료량 기반 진행률 `%`를 표시한다.
- 진행률 보고 직후 사용자 응답을 기다리지 않고 다음 구현/검증을 계속한다.
- `분석 완료`, `P0 완료`, `오류 발견`, `Gate 실패`, `중간 ZIP`, `미구현/부분구현 판정`은 종료 지점이 아니다.
- 구현 가능한 `미구현/부분구현`은 선언만 남기지 말고 같은 회차에서 Source/Test/Consumer/Evidence까지 수정한다.
- 자체 검수 중 새 결함을 발견하면 기존 Requirement/Commercial Hardening 40의 owner 축에 병합하고 즉시 수정한다.
- 진행률 100%는 verifiable failure 0, 자체검수, final overlay/hash/manifest까지 끝난 뒤에만 사용한다.

진행보고 예:

```text
[CPF 최종개발 진행률 XX%]
완료:
현재 작업:
새 결함/원인:
같은 회차 보정:
다음 Gate:
전체 완료까지 남은 핵심:
```

# 1. 시작 시 정본 읽기 순서

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. Architecture/Specification/ADR
3. `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
4. `cpf-docs/work/current/CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md`
5. `cpf-docs/work/current/CPF_DX_GOLDEN_PATH_AND_COMMON_FUNCTION_REQUIREMENTS.md`
6. `cpf-docs/work/current/CPF_CONFIGURATION_AND_INVOCATION_REQUIREMENTS.md`
7. `cpf-docs/work/current/CPF_STARTER_CAPABILITY_COMMON_FUNCTION_MATRIX.md`
8. `cpf-docs/work/current/CPF_CONFIGURATION_SETUP_AND_INVOCATION_MATRIX.md`
9. `cpf-docs/work/current/CPF_WHOLE_FRAMEWORK_CONFIGURATION_DEFAULTS_KO_COMMENTS_OVERRIDE_REQUIREMENTS.md`
10. Requirement/Gap/Defect Matrix
11. 실제 Source/SQL/API/Test/Config/Frontend/Script
12. 현재 execution SHA Evidence

문서와 Source가 충돌하면 QA Requirement/최상위 정본을 우선하되,
정본 자체의 stale 사실은 임의 해석으로 덮지 말고 영향/대안과 함께 current management에 기록한다.


# 1A. 통합 재개발 기준 — Developer Convenience 우선 / Steering 최소화

이 문서는 지금까지 전달된 Configuration/Invocation/Domain Call/Logging/Transaction/Generator/DX Steering을 **전부 흡수한 통합 개발지침**이다.
부분 Steering만 읽고 구현하지 않는다.

이번 회차는 기존 구현을 완료로 승계하지 않고 latest master에서 처음부터 다시 검수한다.
단 이미 올바른 Source를 무조건 다시 쓰지 않고 보존·재검증한다.

최상위 DX 원칙:

> History를 몰라도 된다.
> 이름만 보고 용도가 보여야 한다.
> IDE 자동완성으로 대부분 개발할 수 있어야 한다.
> 개발자는 Framework plumbing보다 업무코드를 더 많이 작성해야 한다.
> 짧고 자연스럽지만 관리·보안·복구가 빠지면 안 된다.

## 1A.1 추가 필수 읽기

- `cpf-docs/work/current/CPF_TRANSACTION_CALL_RESULT_LOG_DX_REQUIREMENTS.md`
- `cpf-docs/work/current/CPF_TRANSACTION_CALL_COMMON_FUNCTION_DEVELOPER_MANUAL.md`
- `cpf-docs/work/current/CPF_COMMON_FUNCTION_COMMAND_CATALOG.csv`

이 세 문서의 의미와 실제 Public Source가 다르면 Source/Generator/EDU/Manual을 같은 회차에서 currentize한다.

## 1A.2 CALLDX-001~018 동일 회차 수정

`CPF_CURRENT_WORK_REQUEST.md`의 CALLDX-001~018을 F-001~016, CFG-001~024, Hardening 40, Starter 64와 같은 Full-Scope에 병합한다.

특히 현재 직접 확인된:
- stale `CpfServiceClient` Core base type 참조.
- String `ServiceCallResult.status`.
- 일반 Call Outcome vs XA/TCC `CpfTransactionOutcome` 역할 혼동.
- Core `CpfErrorResponse`의 platform-operations internal 의존.
- direct system time.
- Logging annotation package drift.
를 단일 파일만 고치지 말고 같은 Root Cause 전체를 Repository에서 검색·수정한다.

## 1A.3 Standard Result 구현

Core Public contract로 `CpfResult<T>` 또는 이름상 동등한 하나의 표준을 확정한다.

Outcome:
- SUCCESS
- BUSINESS_FAILURE
- TECHNICAL_FAILURE
- UNKNOWN

단:
- 모든 Service/Repository Java method를 Result로 감싸지 않는다.
- Network/Distributed/Async/Messaging/External Side Effect/Runtime Operation Boundary에 Result를 강제한다.
- Annotation/Role Architecture Test로 임의 반환계약을 막는다.

자료형:
DTO/List/Page/Cursor/Map/String/int/long/boolean/BigDecimal/Void/Ack/Receipt/Async/Stream 전부 Test한다.

Generic List/Page/Map에는 TypeRef/Parameterized Type 계약을 제공한다.
Generated Typed Client를 Primary로 한다.

## 1A.4 Domain / Remote / External Function naming

Golden Path를 다음 의미로 명확히 한다.

- same JVM service: direct typed call.
- topology-independent Domain: Generated Typed Domain Client.
- generic Domain helper: `callDomain/callDomainAsync` 또는 동등한 직관적 이름.
- explicit remote advanced: 기존 `callRemote/callRemoteAsync`.
- External: Generated Typed External Client.
- generic External helper: `callExternal/callExternalAsync` 또는 동등 이름.

`callRemote` 하나에 Domain/External을 전부 넣지 않는다.

## 1A.5 Result 처리 Boilerplate 최소화

`CpfResult<T>`는 최소:
`isSuccess/isBusinessFailure/isTechnicalFailure/isUnknown/data/error/meta/recovery/map/fold`
또는 동등 Convenience를 제공한다.

Side-effect 없는 안전한 query contract에서 `requireData()`류 편의 API를 허용한다.
UNKNOWN 가능한 side-effect를 무조건 unwrap하는 API는 금지한다.

## 1A.6 MSA 처리

Boundary Call은:
SUCCESS/BUSINESS_FAILURE/TECHNICAL_FAILURE/UNKNOWN을 개발자가 즉시 판단 가능하게 한다.

TECHNICAL_FAILURE retry는 retryable + idempotency + configured policy 모두 만족할 때만.
UNKNOWN은 reconcile/probe/manual-review를 쉽게 연결하는 `RecoveryInfo`/Operations를 제공한다.

## 1A.7 Transaction

- Controller call = execution envelope, DB Tx 아님.
- same-JVM Service direct call.
- required/requiresNew/readOnly/timeout/isolation/rollback/hooks.
- MBR DB Tx와 EXS DB Tx 분리.
- Local Domain binding도 remote와 semantics parity.
- DB→Message Outbox.
- Message→DB Inbox.
- long workflow Saga/Reconcile.
- XA/JTA/TCC optional only.
- active local write tx에서 remote side effect 호출 정책을 명시/검증.

## 1A.8 Logging

최상위 정본 8.1을 그대로 구현한다.

자동 로그 필드:
timestamp/level/system/environment/instance/TxId/ExecutionId/Trace/Span/Segment/ParentSegment/Attempt/
Request/Idempotency/Actor/Tenant/Channel/Batch IDs/Operation/Endpoint/RemoteSystem/Result/Error/
FailureStage/Retryable/Unknown/Elapsed/message-file identifiers.

Public:
businessLog/operationLog/securityLog/audit/errorLog 또는 동등 naming.

현재 LoggingAspect의 좁은 필드만으로 완료 금지.

## 1A.9 Common Function Manual을 Source와 함께 개발

`CPF_COMMON_FUNCTION_COMMAND_CATALOG.csv`의 각 행을 실제:
Public API → Owner Starter → Config → AutoConfiguration → Bean → Consumer → Test → Evidence
에 연결한다.

Manual을 나중에 사람이 추측해서 쓰게 하지 않는다.
Public API 변경 시 Manual/Catalog/Generator/EDU도 같은 commit scope로 수정한다.

## 1A.10 새 결함 확산검사

작업 중 한 기능에서:
- return type 불일치
- poor naming
- result handling 불편
- config 설명 누락
- log field 누락
- hidden transaction
- raw URL
- string lookup
- false-green
을 발견하면 동일 패턴을 CPF 전체에서 검색해 같은 회차에 수정한다.

“추후”, “다른 모듈도 유사”, “다음 Phase”로 종료하지 않는다.

# 2. Git / 삭제 / 적용 절대 금지

## 2.1 Git write
사용자 승인 없이 Commit/Push/Branch/Tag/PR/Release/Reset/Restore/Stash/Clean/History 변경 금지.

## 2.2 사용자 Repository Source 변환 금지
다음은 **절대 금지**:
- currentizer
- migration/source transformation
- source move/rename
- package rewrite
- dynamic source generation
- repository-wide 장시간 mutation
- 중간 실패 시 partial conversion 상태가 되는 script

## 2.3 허용 Apply
사용자용 Installer/Verifier는 정확히:
`exact target SHA 확인 → 완성 Overlay 파일 복사 → 승인된 Root-relative Delete Manifest 삭제 → 검증`

뿐이다.

Education/Batch/Tools/Deploy도 완성된 final source를 ZIP에 직접 넣는다.
내부 currentizer가 필요하면 Developer GPT 자기 격리 작업공간에서만 사용한다.
최종 ZIP/Repository/사용자 명령에 currentizer를 넣지 않는다.
적용 후 새 Source/Package 생성이 필요하면 Package FAIL.

# 3. Baseline / Current Review — 먼저 실행하고 후판정

최신 `origin/master` exact SHA와 Working Tree를 기록한다.
착수 기준 SHA 이후 commit이 있으면 변경을 다시 review한다.

현재 정적 확정 defect:

## F-001 [P0] Root Gradle convention path broken
**근거:** root build.gradle이 `cpf-tools/build/cpf-root-conventions.gradle`을 apply하지만 최신 `cpf-tools/build/`에는 해당 파일이 없고 `gradle-plugin/`, `platform-bom/`만 존재
**Owner Requirement:** `REL-BUILD`, `RULE-ARCH`
**Closure:** fresh clone root help/settings/configuration이 exit 0, missing apply-from 0

## F-002 [P0] Batch retired source residue
**근거:** settings는 새 IA만 포함하지만 `cpf-batch/agent/.../HostAgentRuntimeStateProvider.java`와 `cpf-batch/runtime-support/.../application-bat-runtime.yml`이 최신 master에 실제 tracked file로 잔존
**Owner Requirement:** `BAT-CORE`, `BAT-SHARED`, `RULE-ARCH`
**Closure:** 새 agent/runtime-support로 기능 승계 증명 후 approved Delete Manifest로 residue 0

## F-003 [P0] Tools Canonical IA incomplete
**근거:** 최신 `cpf-tools/` top-level에 target에서 제거/이관해야 할 `analysis/`, `scripts/`가 남고 contracts/governance/security/supply-chain target mapping이 미완결
**Owner Requirement:** `ARCH-LAYER`, `RULE-ARCH`
**Closure:** 각 파일 owner 재분류 + consumer 이관 + stale path 0

## F-004 [P0] Deploy Canonical IA incomplete
**근거:** 최신 `deploy/`가 `batch/cells/env/inventory/local/schema` 구 구조를 유지
**Owner Requirement:** `REL-DEPLOY`, `RULE-ARCH`
**Closure:** target `environments/runtimes/ci/schemas` 의미로 완성 source 이동 및 old tree 0

## F-005 [P0] Evidence baseline stale
**근거:** Evidence는 실행 대상 exact SHA와 일치해야 하며 과거 세션/버전 Evidence를 Current 정본으로 승계하지 않는다.
**Owner Requirement:** `TEST-EVIDENCE`, `REQ-REVIEW`
**Closure:** 최신 execution SHA에서 PASS/FAIL/미검증을 재생성하고 Current Evidence 하나로 관리한다.

## F-006 [P0] Generator still emits DAO Golden Path
**근거:** generator가 `CpfBaseDao`, `@CpfDao`, `SampleTransactionDao`를 생성하고 member/external 회귀 산출물에도 `*BaseDao` 존재
**Owner Requirement:** `DEVEX-CODEGEN`, `DEVEX-LAYER`, `STARTER-DX`
**Closure:** Repository one-concept target으로 generator/member/external/EDU 동시 currentize

## F-007 [P0] Generator transaction template stale
**근거:** generator sample service는 `@CpfOnlineTransaction`을 생성하지만 최신 persistence interceptor는 `@CpfTx`와 legacy duplicate 의미를 별도 처리
**Owner Requirement:** `TX-DX`, `DEVEX-CODEGEN`
**Closure:** Canonical transaction DX 하나로 generated source currentize + duplicate/legacy active usage 0

## F-008 [P0] EDU capability integration false-green risk
**근거:** 공통 `AbstractManualEduIntegrationTest`가 모든 EduConsumerType을 deterministic test double로 치환; cache/batch/gateway 등 named integration tests가 실제 해당 provider runtime을 증명하지 못함
**Owner Requirement:** `SAMPLE-EDU`, `DEVEX-TESTKIT`, `TEST-RUNTIME`, `TEST-FAULT`
**Closure:** shared harness는 unit/contract로 유지, capability integration/recovery는 actual CPF starter/provider consumer 사용

## F-009 [P0] EDU Base/common function too thin
**근거:** EducationBaseController/Service/Dao는 page size/txn id/key/offset 수준으로 얇아 합의된 실사용 DX skeleton 미충족
**Owner Requirement:** `DEVEX-LAYER`, `DEVEX-LOGGING`, `TX-DX`
**Closure:** Controller/Service/Repository/Batch common operations + starter composition을 실제 EDU에서 사용

## F-010 [P0] Canonical DAO wording conflicts with target
**근거:** 최상위 §16.3이 Controller/Service/DAO exact 3-tier를 강제하면서 같은 절이 @CpfRepository/interface composition을 인정
**Owner Requirement:** `DEVEX-LAYER`, `DEVEX-ANNOTATION`
**Closure:** 고객 Public Persistence concept=Repository로 정본 현행화, provider별 class/interface mode 명시

## F-011 [P1] ADM 16 requirements missing in source registry
**근거:** Requirement 80, source route 64; missing 16 exact route ids
**Owner Requirement:** `ADM-*`
**Closure:** 누락 16을 포함해 80행 전체 Menu→Route→Page→Client→Backend→Permission→E2E

## F-012 [P1] BZA route count aligned but menu grouping requires revalidation
**근거:** Requirement route 27과 source bzaRoutes 27은 1:1 일치. Source high-level group enum은 5, requirement target_menu_group은 7
**Owner Requirement:** `BZA-*`
**Closure:** 실제 menu/subgroup rendering을 대조해 7 target IA 의미 보존 여부 판정

## F-013 [P1] DB3 physical basis exists; runtime evidence stale
**근거:** canonical + vendor/{oracle,postgresql,mariadb} 물리 구조는 존재하나 latest exact-SHA install/upgrade/rollback runtime evidence 없음
**Owner Requirement:** `DB-MULTI-VENDOR`, `DB-INSTALL`, `DB-MIGRATION`, `DB-ROLLBACK`
**Closure:** 3 vendor runtime matrix latest SHA 재검증

## F-014 [P1] Starter catalog metadata stale
**근거:** Starter Catalog baseline metadata는 frozen 과거 SHA를 Current 값으로 사용하지 않는다.
**Owner Requirement:** `ARCH-STARTER`, `STARTER-DX`, `TEST-EVIDENCE`
**Closure:** physical/settings/generator/BOM/publication과 catalog baseline/current metadata 정합화

## F-015 [P1] Current base helpers are not yet commercial DX surface
**근거:** CpfBaseController는 context/validation/response 중심, CpfBaseService는 requireText 수준. 기존 Annotation runtime consumer는 존재
**Owner Requirement:** `DEVEX-LAYER`, `DEVEX-ANNOTATION`, `DEVEX-LOGGING`, `TX-DX`
**Closure:** 기존 runtime consumer 장점 유지 + starter operations composition/common helper 확장

## F-016 [P1] Repository API surface duplicated by implementation mode
**근거:** CpfBaseDao/@CpfDao와 @CpfRepository/RepositoryContract/RepositoryPort가 public API에 동시 존재
**Owner Requirement:** `DEVEX-LAYER`, `ARCH-BOUNDARY`
**Closure:** customer concept Repository 하나로 수렴; JDBC/MyBatis class mode와 JPA/interface mode의 내부 차이 숨김

# 4. Root Build / Convention — F-001

현재 확인된 missing `cpf-root-conventions.gradle`을 임시 파일 하나 만들어 통과시키는 방식으로 끝내지 않는다.

확인:
- root `settings.gradle` include graph
- convention plugin physical owner
- root `build.gradle` apply/plugin path
- subproject common conventions
- generated domain build participation
- publication/BOM/catalog
- Gradle configuration cache/Java25 compatibility where applicable

Acceptance:
- fresh clone equivalent에서 `gradlew help` / settings / configuration 성공
- missing apply-from 0
- dual convention path 0
- `git diff --check`
- changed-module compile/test
- publication gate까지 연결

# 5. Batch IA / Runtime — F-002

Target active projects:
`api, runtime-support, runtime, control-plane, scheduler, worker, center-cut, agent, testkit`

현재 tracked old residue:
- `cpf-batch/agent/.../HostAgentRuntimeStateProvider.java`
- `cpf-batch/runtime-support/.../application-bat-runtime.yml`

먼저 새 `agent/AgentRuntimeStateProvider`, `runtime-support` config와 기능/consumer parity를 대조한다.
필요 content를 잃지 않고 successor에 통합한 뒤에만 old path를 Delete Manifest로 제거한다.

`check-legacy-batch-migration.ps1`는:
- directory
- settings
- all build.gradle
- source/config/script/docs active reference
를 검사한다.
history/negative fixture는 allowlist한다.

## Batch workload Common Operations

Control Plane의 `CpfBatchOperationsPort`를 새로 복제하지 않는다.
업무 Job/Step용 DX를 별도 workload operations로 만든다.

필수 의미:
- context/job/step/execution/businessDate/parameters
- chunk/page/cursor/partition
- checkpoint/resume/watermark
- retry/skip/idempotency
- transaction/commit
- lock/lease/fencing
- scheduler/agent/runner/worker propagation
- stop/cancel/drain
- progress/metric/log/audit
- failure/UNKNOWN/reconcile
- restart/rerun/duplicate protection

Generator Batch sample과 EDU actual Spring Batch consumer가 사용한다.

# 6. Tools / Deploy IA — F-003/F-004

`cpf-tools`의 모든 file을 owner 기준으로 inventory한다.
`analysis/`, `scripts/`를 이름만 지우지 말고 실제 기능을 build/contracts/db/environment/generator/governance/release/runtime/security/supply-chain/testing/verification owner로 이관한다.

`deploy` old tree:
`batch/cells/env/inventory/local/schema`
를 target:
`environments/*`, `runtimes/platform`, `runtimes/batch`, `ci`, `schemas`
의 실제 의미로 currentize한다.

사용자 PC에서 move script 금지.
완성 tree가 Overlay에 직접 들어가야 한다.

# 7. Evidence — F-005

과거 execution Evidence를 Current 완료 판정에 승계하지 않는다. History 정리 전에도 현재 QA/Codex 재검증에 필요한 최소 Evidence만 안정된 current 경로에 유지한다.
아래를 successor execution SHA에서 다시 실행:

- static gates
- root build
- targeted/full compile-test
- generator lifecycle
- DB3
- cache provider
- broker/integration
- online/batch runtime
- browser
- fault injection
- publication
- package/hash

READY/PLANNED/NOT_EXECUTED는 PASS 아님.

# 8. Repository One Concept — F-006/F-010/F-016

## 8.1 Customer Public concept
`Repository` 하나.

## 8.2 JDBC/MyBatis
Class Golden Path:
`CpfBaseRepository → DomainBaseRepository → BusinessRepository`

## 8.3 JPA/Spring Data
Interface mode:
`@CpfRepository + CpfRepositoryContract/Port + composition`

## 8.4 기존 자산
`CpfCrudRepositoryPort`, `CpfSearchRepositoryPort`, `CpfBulkRepositoryPort`, `CpfLockingRepositoryPort`를 재사용.

## 8.5 제거/Compatibility
`CpfBaseDao/@CpfDao/*BaseDao`는 새 API/Generator/Consumer가 완전히 전환된 뒤 Legacy Compatibility 필요성을 판정.
Public 문서/EDU/Generated Golden Path에는 DAO를 남기지 않는다.
단, Mapper/JDBC low-level implementation은 Internal에서 기술적으로 사용할 수 있다.

## 8.6 Annotation
`@CpfRepository` 하나가 class/interface mode의 고객 의미를 일관되게 표현하도록 runtime verifier를 설계한다.
blind meta-annotation 변경으로 JPA proxy를 깨지 않는다.

# 9. Annotation / Bean / Naming

## Controller
`@CpfController`는 실제 bean + web policy consumer.
optional value/name 또는 동등 명시 이름 지원을 검토.
default bean naming은 Spring/Cpf canonical naming과 일치.

## Service
기존 `@Service` meta 및 post-processor 기반을 유지·확장.
duplicate name/role/base/context mismatch fail-fast.

## Repository
§8 mode에 따라 class/interface를 모두 안전하게 처리.

## DTO
`@CpfDto`는 Singleton bean 등록 금지.
validation/serialization/mapping/masking/metadata consumer 연결.

## Injection
type-based constructor injection 기본.
string lookup 금지.

# 10. Stable 3-Level Golden Path / Multi-Starter Composition

Starter 선택이 바뀌어도:
`BusinessController extends DomainBaseController`
`BusinessService extends DomainBaseService`
`BusinessRepository extends DomainBaseRepository`
는 유지.

여러 capability를 Java multiple inheritance로 해결하려 하지 않는다.

Architecture:
`Base ergonomic helper → Public Capability Operations/Facade → AutoConfiguration → Provider`

God Base 금지.
Internal leaf direct dependency 금지.
Native Escape 제공.

미선택 capability:
- dependency 0
- bean 0
- config 0
- sql 0
- 가능하면 helper compile surface 0

provider conflict/exactly-one은 generator preflight/startup fail-fast.

# 11. Starter 64-module Function Catalog — 하나도 누락 금지

`CPF_STARTER_CAPABILITY_COMMON_FUNCTION_MATRIX.md`의 64 module을 전부 처리한다.

각 Module 실행행:
- artifact/project path/visibility/owner
- config prefix
- public operation 또는 구현 대상 public owner
- AutoConfiguration
- Base helper 노출 여부
- activation
- provider slot/conflict
- actual Consumer
- normal/error/partial failure
- recovery/reconcile
- Native Escape
- Generator/EDU/Sample
- Test/Evidence

“Logging/Cache 등 대표 Starter 몇 개만 구현” 금지.
Catalog에 존재하면 전부 판정한다.

# 11A. Common Function Bean Registration / Lifecycle — 필수

공통 Function 구현은 `Base method 존재`로 완료하지 않는다.

- selected Capability → Canonical Public `*Operations`/Facade Bean 등록.
- Domain Base helper와 Customer direct constructor injection은 동일 Operations Bean 사용.
- unselected Capability → Bean/Provider/Listener/Thread/Endpoint/SQL/mandatory Config 0.
- Provider conflict/duplicate role/name → fail-fast.
- documented Customer override/backoff.
- stateless singleton 기본; mutable Request/User/Tx/Execution state singleton field 저장 금지.
- `callAsync` → bounded Executor Bean, common pool 금지, Context snapshot/restore, rejection/backpressure/timeout/cancel/drain.
- Messaging/Realtime/Integration → Listener/Consumer/Client Bean registration + collision/lifecycle/drain.
- Batch → Job/Step/Reader/Processor/Writer/Tasklet Adapter registration + duplicate name/scope/lifecycle.
- selected/unselected/missing-config/conflict/override/shutdown Context Test.


## Common Function Master Catalog — 전체 Starter/Capability Fan-out

아래는 위에서 합의한 공통 Function의 **의미론 Master Catalog**다.
실제 API 명칭은 기존 Public API와 naming consistency를 검토해 확정하되,
각 Function Family의 기능/오류/복구/Bean/Consumer 중 하나라도 빠지면 완료가 아니다.

### A. Base / Context / Execution
- context, transactionId, executionId, correlationId, requestId, attempt, deadline.
- current actor/user/service identity/tenant/system/instance.
- Context snapshot/restore/propagate/clear.
- call, callAsync, safe callback/execution wrapper.
- standard result/error/classification.
- Clock/currentTime/businessDate/timezone.
- cancellation/interruption/deadline check.
- typed capability availability; String service locator 금지.

### B. Web / Controller
- request/path/query/header/body validation.
- page/sort/cursor/search normalization.
- standard success/created/accepted/no-content/error response.
- request metadata/idempotency/deadline.
- upload/download/stream helper when File Capability exists.
- conditional/optimistic version helper where API requires.
- Controller → Service/다른 Service 호출 허용.
- Controller → Repository 직접 접근 Golden Path 금지.

### C. Service / Application
- sync/async call/orchestration.
- required/requiresNew/readOnly 및 명시적 no-tx/suspend 필요성.
- timeout/isolation/rollback.
- afterCommit/afterRollback.
- retry/backoff/jitter/deadline.
- idempotent/deduplicate/reconcile/compensate.
- cache/messaging/integration/security/audit/common product shortcut.
- structured log/trace/metric.
- bounded parallel fan-out/fan-in.
- SUCCESS/FAIL/UNKNOWN 구분.

### D. Repository / Persistence
- findById/findOne/exists.
- insert/save/update/delete.
- search/page/cursor/count.
- bulk insert/update/delete.
- optimistic/pessimistic lock.
- timeout/row limit/page limit/sort allow-list.
- streaming/large-result resource-safe close.
- duplicate/constraint/deadlock/timeout/connection error mapping.
- retryability classification.
- vendor-neutral ID/sequence.
- native JDBC/MyBatis/JPA escape hatch.

### E. Transaction / Consistency
- required/requiresNew/readOnly.
- timeout/isolation/rollback.
- afterCommit/afterRollback.
- current tx status/rollback-only.
- idempotency/outbox/inbox.
- Saga/TCC/XA/JTA는 선택 Capability로 recovery contract 제공.
- commit response loss/UNKNOWN probe/reconcile.
- local DB tx와 remote HTTP/Message 경계 혼동 금지.

### F. Cache / Distributed Lock
- get/put/putIfAbsent/evict/evictAll where safe/getOrLoad.
- TTL/negative cache/stale/version/fence/invalidation.
- multi-instance refresh.
- single-flight/stampede protection.
- serializer/schema/version.
- outage/reconnect/fail-open-or-closed.
- hit/miss/load/failure/eviction metrics.
- lock acquire/try/renew/release/fencing/lease.
- stale owner/process-kill/network partition recovery.
- Caffeine/Redis/Valkey parity/conflict.

### G. Messaging / Event
- publish/send/request-reply where supported.
- consume/listener registration.
- key/header/correlation/context.
- schema/content-type/version.
- ack/nack/commit.
- retry/backoff/jitter.
- DLQ/quarantine/replay.
- duplicate/idempotency/inbox.
- ordering/partition/rebalance.
- outbox publish.
- publish result/probe/UNKNOWN.
- pause/resume/drain.
- Kafka/JMS/IBM MQ/RabbitMQ provider-neutral semantics + native escape.

### H. Integration / External Call
- sync/async call.
- timeout/deadline.
- retry eligibility/backoff.
- circuit breaker.
- bulkhead/concurrency/rate limit.
- credential/service identity.
- request/response masking.
- error taxonomy/status mapping.
- correlation/context propagation.
- remote UNKNOWN/probe/reconcile.
- HTTP/TCP/SOAP/Fixed Length/ISO8583/Webhook.
- AI request/result/error/timeout/resource governance.
- GraphQL query/mutation/paging/error/context/security.
- Realtime/SSE subscribe/publish/progress/reconnect/heartbeat/backpressure/drain.

### I. File / Object Storage / Tabular
- upload/download/read/write/stream.
- metadata/content-type/checksum/version.
- atomic temp-write/finalize.
- partial failure/orphan/quarantine.
- retry/resume where supported.
- archive create/extract with path traversal/zip-bomb protection.
- attachment validation/size/type/malware hook.
- SFTP connect/upload/download/rename/ack.
- S3/object storage put/get/delete/list/presign where policy permits.
- tabular read/write/stream/row validation.
- retention/encryption/access/audit.

### J. Notification
- dispatch/send.
- email/SMS channel selection.
- template/render/locale.
- recipient normalization/validation.
- provider result/correlation.
- retry/dedup/rate limit.
- provider outage/fallback only when explicitly configured.
- PII masking/audit.
- async delivery result/UNKNOWN.

### K. Security / Identity / Session / Secret
- currentPrincipal/currentServiceIdentity/currentTenant.
- authenticate/authorize/hasPermission by role.
- reason/approval/SoD/break-glass.
- session/token issue/read/revoke/expire/refresh where owned.
- secret resolve/rotate metadata without raw logging.
- masking/redaction.
- security/audit event.
- session JDBC/Valkey parity/multi-instance.
- OIDC/resource-server/service-identity boundary.

### L. Observability / Platform Operations
- structured business/operation/security/audit/error log.
- trace/span/correlation.
- metric counter/timer/gauge/distribution.
- liveness/readiness/startup/degraded.
- runtime registration/status.
- graceful drain/shutdown.
- runtime control command/result.
- typed feature flag evaluation.
- channel/provider registry.
- config drift/runtime health/alert.
- operator query/control auth/reason/audit.

### M. Common Product Service
- code lookup/list/refresh.
- message lookup/render/locale.
- parameter typed get/default/version.
- calendar/businessDate/holiday/working-day.
- template resolve/render/version.
- management/common catalog owner/consumer.
- Cache/Transaction/Logging 등 기술 helper를 Common에 몰아넣지 않는다.

### N. Batch Workload
- jobContext/stepContext/executionContext.
- businessDate/parameters.
- chunk/page/cursor/partition.
- checkpoint/watermark/resume.
- retry/skip/idempotency.
- tx/commit boundary.
- lock/lease/fencing.
- scheduler/agent/runner/worker correlation.
- stop/cancel/drain.
- progress/metric/log/audit.
- output/result.
- restart/rerun/duplicate protection.
- failure/UNKNOWN/reconcile.

### O. Developer / Test Support
- deterministic Clock/ID/Sequence.
- context fixture.
- transaction/cache/message/integration fault injection.
- provider contract test fixture.
- fake/test-double은 Unit/Contract에서만 사용하고 Runtime Evidence와 분리.
- member/external generated parity.
- misuse negative test.
- Native Escape compatibility test.



# 11B. Developer Custom Bean Injection / Method Invocation — 필수 DX

현재 지침의 type-based Constructor Injection만으로 완료하지 않는다.
업무 개발자가 직접 만든 Bean을 다른 CPF Bean에서 간단하게 자동 주입하고 메서드를 호출하는 `@CpfInject` 또는 동등 Public DX를 구현한다.

## Owner / Boundary
- Spring Runtime 의존이므로 `cpf-core`에 두지 않는다.
- Base/Application Runtime의 Public Annotation/AutoConfiguration/BeanPostProcessor가 Owner.
- Customer Source가 Internal package를 참조하지 않게 한다.

## Usage
```java
@CpfService
public class MemberPolicyService extends MemberBaseService {
    public boolean canJoin(String memberId) {
        return true;
    }
}

@CpfService
public class MemberService extends MemberBaseService {

    @CpfInject
    private MemberPolicyService memberPolicyService;

    public boolean canJoin(String memberId) {
        return memberPolicyService.canJoin(memberId);
    }
}
```

관리 실행 경계:
```java
return call(() -> memberPolicyService.canJoin(memberId));
```

비동기:
```java
return callAsync(() -> memberPolicyService.canJoin(memberId));
```

## Resolution
- default: by type.
- Customer `@CpfService/@CpfRepository`, Spring `@Component/@Service/@Repository`, `@Bean`, Public Starter Operations 모두 주입 가능.
- 0 candidate(required) → fail-fast.
- 2+ candidates → qualifier 없으면 fail-fast.
- `@CpfQualifier("beanName")` 또는 동등 Canonical Qualifier로 명시 선택.
- Bean name은 qualifier에서만 보조적으로 사용.
- `ApplicationContext.getBean()` / global static resolver / string service locator를 Golden Path로 만들지 않는다.

## Proxy semantics
- injected managed Proxy를 통해 `@CpfTx`, Retry, Logging, Security, Audit 등 Runtime Consumer가 적용되어야 한다.
- `this.someMethod()` self-invocation 우회 문제를 EDU/Guide에 설명하고 Negative Test를 둔다.
- `call("bean", "method")` Reflection/string invocation API 금지.
- `call(() -> bean.method())` compile-time safe invocation 사용.

## Constructor vs Field
- Framework/Generator Base 내부는 Constructor Injection 기본.
- Customer Business Source에는 `@CpfInject` Field/Constructor/Parameter를 공식 간편 DX로 허용.
- 필수 불변 의존성/테스트성 요구가 높은 Component는 Constructor/Parameter 사용을 권장.
- Field Injection을 허용한다고 Circular Dependency를 숨기지 않는다.

## Fan-out
- Annotation Source/AutoConfiguration/PostProcessor.
- Bean naming/Qualifier contract.
- Generator template.
- cpf-member/cpf-external.
- cpf-education Online/Batch 실제 sample.
- misuse/negative/context/runtime test.
- Korean JavaDoc/Guide.

## Acceptance Test
1. custom CPF Service injection PASS.
2. plain Spring Component/@Bean injection PASS.
3. Public Starter Operations injection PASS.
4. missing bean FAIL.
5. duplicate type without qualifier FAIL.
6. qualifier selects exact bean PASS.
7. Tx/Logging/Security proxy behavior PASS.
8. self-invocation negative.
9. circular dependency negative.
10. generated/EDU actual method invocation evidence.


# 12. Controller Common Functions

최소 semantic:
- call/callAsync
- context/transactionId/executionId
- currentUser/principal/permission
- validate
- page/sort/cursor
- standard response
- request metadata
- idempotency key
- request/operation/error logging

Controller→다른 Service 가능.
Controller→Repository Golden Path 금지.

`call()`이 아무 의미 없는 method wrapper이면 만들지 않는다.
Context/Trace/Execution/Error semantics를 실제 적용한다.

# 13. Service Common Functions

최소 semantic:
- call/callAsync
- required/requiresNew/readOnly
- timeout/isolation/rollback
- afterCommit/afterRollback
- retry/backoff
- idempotent/reconcile
- cache
- messaging/event
- integration
- security/authorization
- audit
- code/message/parameter/calendar/template
- structured logging / trace / metric

Helper count를 채우는 것이 목표가 아니다.
Operations grouping이 더 명확하면 grouping한다.

# 14. Transaction DX — F-007

기존 `CpfTxMethodInterceptor + TransactionTemplate` 기반을 활용한다.

현재 한계:
- propagation required 고정
- readOnly/timeout 중심
- generated sample은 legacy `@CpfOnlineTransaction`

Target:
- required
- limited requiresNew
- readOnly
- timeout
- isolation
- rollback rule
- afterCommit/afterRollback
- operation/context/audit semantics
- UNKNOWN classification
- outbox/inbox/saga/reconcile

Spring self-invocation 함정에 기대지 않는다.

Test:
- outer/inner commit/rollback matrix
- requiresNew 독립 결과
- timeout/deadlock
- commit response loss/process kill
- DB→message outbox
- message→DB inbox/duplicate
- remote side effect unknown

Generator/EDU에서 old transaction annotation Golden Path 제거.

# 15. Logging DX

기존 `CpfLoggingAspect`의 Context/Masking/Error 연계를 보존.

Public developer operations:
- businessLog
- operationLog
- securityLog
- auditLog/audit
- errorLog
또는 naming-consistent 동등 API.

자동 fields:
System/Instance/TransactionId/ExecutionId/Actor/User/ServiceIdentity/Tenant/Job/Step/Trace/Error.

민감정보 raw log 금지.

동일 event를 log/trace/metric/audit에서 correlation 가능해야 한다.

Log sink failure policy와 Audit durability 경계를 테스트한다.

# 16. Cache DX

기존 `CpfCacheAsideService`의 TTL/negative/single-flight/fail-open을 재사용.

Public Operations:
get/put/evict/getOrLoad + policy.

보강:
- invalidation
- multi-instance refresh
- stale
- outage/reconnect
- stampede
- serialization/version
- metrics
- Redis/Valkey/Caffeine parity
- provider conflict

실제 Redis/Valkey outage/reconnect/process kill test 필요.

# 17. Persistence Common Functions

CRUD/search/page/cursor/count/bulk/lock/timeout/limit/sort/error/id sequence.

DB exception:
duplicate/constraint/deadlock/timeout/connection/unknown을 CPF error taxonomy와 retryability로 map.

Lock:
optimistic/pessimistic 및 distributed fencing 의미를 구분.

DB3:
Oracle/PostgreSQL/MariaDB public semantics 동일.

# 18. Messaging Common Functions

기존 Broker API/Unknown Result Port를 재사용하고 ergonomic Operations를 제공.

- publish/send/consume
- headers/key/correlation/context
- retry/backoff
- DLQ
- duplicate/idempotency
- ordering/rebalance
- schema/version
- outbox/inbox
- result probe/UNKNOWN

Kafka/JMS/IBM MQ/RabbitMQ provider matrix 실제 runtime test.

# 19. Integration Common Functions

- local/remote call abstraction
- call/callAsync
- timeout/deadline
- retry eligibility
- circuit breaker
- bulkhead/rate-limit
- credential/security
- error mapping
- context/correlation
- remote UNKNOWN/reconcile

HTTP/TCP/SOAP/fixed-length/ISO8583/Webhook 등 Catalog capability를 누락하지 않는다.

# 20. Security Common Functions

- current principal / service identity / tenant
- authorize/hasPermission
- masking
- reason/approval
- SoD/break-glass
- session/revoke/expiry
- audit
- batch/message/integration propagation

# 21. Common Product Services

`cpf-starters/common`:
Code / Message / Parameter / Calendar / Template를 actual runtime product service로 유지.

Service Base shortcut은 이 Public Service를 composition.
Cache/Transaction/Logging 같은 기술 helper를 Common에 이동 금지.

# 22. Generator / Generated Domain — F-006/F-007

Generator를 Source of Truth로 수정하고 그 뒤 member/external을 **실제 동일 generator**로 재생성한다.

필수:
- DAO→Repository
- old transaction sample→new TX DX
- selected starter composition
- Online/Batch common operations
- Korean comments
- no internal leaf direct dependency
- domain-neutral
- no `.cpf/**`, README/verification/vendor DB tree in generated customer project
- validate/preflight/generate/regenerate/upgrade/restore

`cpf-member`와 `cpf-external` normalized parity + hardcoding scan.

# 23. EDU — F-008/F-009

EDU는 고객이 복사할 실사용 source다.

새 common operations를 실제로 사용:
- Controller call/page/validation/log
- Service tx/cache/message/integration/security/log
- Repository
- Batch workload ops

기존 generic execution/state-machine harness:
- Unit/Contract로 유지 가능
- actual provider가 아니면 Integration/Runtime/Recovery PASS에 집계 금지

Capability별 actual test:
Cache→Redis/Valkey/Caffeine actual provider
Batch→Spring Batch/runtime/worker/checkpoint/process kill
Messaging→actual broker where supported
Integration→actual reference server/fault
DB→DB3
Security→actual auth/session policy
ADM/BZA→browser/generated client/backend

# 24. ADM — F-011

Source route에 없는 exact 16:
`audit-diff`, `auth-session`, `external-institutions`, `incident-postmortem`,
`ops-alerts`, `ops-capacity`, `ops-config`, `ops-dr`, `ops-drift`, `ops-maintenance`,
`ops-metrics`, `ops-runbooks`, `ops-self-healing`, `ops-slo`, `ops-topology`, `security-cross-cut`.

16개만 skeleton 추가하고 끝내지 않는다.
ADMUI-001~080 전체를:
Menu→Route→Page→Generated Client→Backend API→Permission→Error UX→Browser/E2E로 검증.

401/403/404/409/429/500/503.
위험 운영조치 reason/approval/audit.
a11y/keyboard/responsive/deep-link.

# 25. BZA — F-012

27 route ID는 latest Source와 Requirement가 일치하므로 그대로 보존.

검증:
- target 7 menu-group semantic
- menu/subgroup rendering
- backend enforcement
- org→user→role→permission→approval
- download/audit/settings
- generated client
- error/a11y/responsive

숫자 27만으로 완료 금지.

# 26. DB3 — F-013

Canonical + Oracle/PostgreSQL/MariaDB physical basis는 보존.

latest execution SHA에서:
- canonical render
- install
- seed
- upgrade
- rollback 또는 forward recovery
- runtime query
- checksum/drift
- generator parity
- member/external/EDU
를 다시 검증.

# 27. Starter Catalog — F-014

Catalog baseline은 고정 과거 SHA를 정본으로 사용하지 않는다. 현재 계약은 실행 시점 `RUNTIME_GIT_HEAD` 또는 successor exact SHA를 기준으로 검증하고 Catalog/Source/Generator/BOM parity를 유지한다.

Catalog와:
- settings
- physical paths
- generator
- profiles
- BOM/publication
- public/internal visibility
- provider slots
- generated dependencies
를 동기화.

Map 변환 전에 duplicate detection.
Internal leaf public BOM 노출 금지.

# 28. Base Helpers / Existing Runtime Assets — F-015

`CpfBaseController`, `CpfBaseService`, Cache Aside, Tx Interceptor, Logging Aspect, Repository Port를 폐기하고 새 평행 API를 만들지 않는다.

기존 API를 inventory:
- keep
- extend
- merge
- deprecate
- internalize
판정 후 canonical public surface 하나로 수렴.

# 29. Commercial Hardening 40 — Developer Instruction 내장 전체 목록

아래 40개는 별도 문서 참조만으로 생략하지 않는다. Developer GPT는 **정확히 1~40 전부**를 이번 Full-Scope 상태표에 판정하고,
`CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md`의 Canonical 연결/Acceptance를 실행한다.

| # | Priority | Axis | 이번 회차 판정 의무 |
|---:|---|---|---|
| 1 | P0 | Runtime 장애·복구·UNKNOWN Hardening | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 2 | P0 | 다중 인스턴스 / 분리 WAS / MSA 일관성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 3 | P0 | Transaction / Outbox / Inbox / Idempotency 통합 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 4 | P0 | Security / Identity 통합 모델 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 5 | P0 | 위험 운영조치 승인 / SoD / Break-glass | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 6 | P0 | Starter/API Developer Experience 전수 Audit | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 7 | P0 | Public API / SPI / Internal 경계 최종 정리 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 8 | P0 | Starter Canonical Catalog 단일화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 9 | P0 | 고객 실제 개발 표준 흐름 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 10 | P0 | Generator Stateless Lifecycle / DX 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 11 | P0 | Root Build / Convention / Publication 경로 단일화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 12 | P0 | Education / Sample 실행체계 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 13 | P0 | Batch Runtime / Scheduler / Worker / Center-Cut 구조 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 14 | P0 | cpf-tools / deploy Canonical IA 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 15 | P0 | Repository Garbage / Dead Source / False-Green 제거 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 16 | P0 | Current Evidence / Exact SHA 신뢰성 체계 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 17 | P1 | Persistence 상용 기본기 강화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 18 | P1 | Oracle/PostgreSQL/MariaDB DB3 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 19 | P1 | Observability E2E 추적 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 20 | P1 | Runtime Health / Readiness / Graceful Drain | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 21 | P1 | Cache / Redis / Valkey Hardening | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 22 | P1 | Messaging 장애대응 표준화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 23 | P1 | Integration 장애대응 표준화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 24 | P1 | ADM Commercial Control Plane 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 25 | P1 | BZA Business Admin 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 26 | P1 | Common Code / Message / Parameter Runtime화 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 27 | P1 | Config / Profile / Secret Governance | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 28 | P1 | API / Event / DB Schema Versioning & Compatibility | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 29 | P1 | Event Schema / Contract Governance | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 30 | P1 | Testkit / Contract Test / Fault Injection Harness | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 31 | P1 | 성능·확장성 Engineering | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 32 | P1 | Upgrade / Rollback / Publication / Supply Chain | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 33 | P2 | Time / Clock / Timezone / Sequence 표준 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 34 | P2 | Resource Exhaustion / Backpressure / Overload Protection | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 35 | P2 | Backup / Restore / DR / Rebuildability | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 36 | P2 | Data Privacy / Retention / Masking / Audit Lifecycle | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 37 | P2 | Extension / Plugin / Native Escape Hatch 정책 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 38 | P2 | Cross-platform CLI / Developer Tooling 완성 | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 39 | P2 | Commercial Education / Onboarding / Troubleshooting | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |
| 40 | P2 | Release Readiness / Commercial Acceptance Closure | 구현/부분구현/미구현 + verification 상태 + Source/Evidence를 기록 |

**금지:** P0만 개발하고 P1/P2를 다음 패키지로 넘기지 않는다. 우선순위는 실행순서일 뿐 Scope 분할이 아니다.
이미 구현된 축도 successor exact SHA regression evidence가 없으면 자동 PASS로 승계하지 않는다.

`CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md` 1~40을 정확히 전부 판정한다.
“현재 개발과 무관”이라는 이유로 행을 생략하지 않는다.
이미 구현된 항목은 regression evidence,
부분 구현은 gap closure,
미구현은 source implementation.

40개 외 새 top-level axis 자동 생성 금지.


# 29A. 기존 31개 Detailed Requirement Contract — 최신 사실로 보정하여 전부 승계

이 절은 이전 `CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`의 **31개 상세 구현 계약을 버리지 않고 승계**한다.
다만 아래에 남아 있는 과거 baseline 표현은 Architecture Target의 역사적 문맥이며,
**현재 Source 사실은 본 문서 §3의 F-001~F-016과 latest execution SHA가 우선**한다.

추가 강제 해석:
- Persistence 용어는 고객 Public `Repository` one-concept로 currentize한다. old DAO naming을 새 Source에 재도입하지 않는다.
- Education 물리 rename은 이미 진행됐으므로 이름 변경 자체가 아니라 active reference 0 + actual consumer quality를 닫는다.
- Batch active IA rename도 대부분 진행됐으므로 `host-agent/runtime-common` tracked residue와 consumer/config parity를 닫는다.
- 31개 Target을 “40대 Hardening과 별개인 과거 작업”으로 보지 않는다. 40대는 이 Target의 commercial closure 축이다.
- 아래 상세 Requirement 중 최신 Final Target/§16.3과 충돌하는 표현은 최신 Final Target을 우선하고 같은 회차에서 Source/Generator/EDU를 함께 currentize한다.

### 3. P0 — historical-baseline Current-State Source Closure

#### NXT2-CORE-001 Core 최종 Freeze와 stale tree 제거

Core는 **Topology-independent Kernel Contract/SPI/Value/Semantics**만 소유한다. 경로 이름만 보고 삭제하지 말고 각 Type의 실제 Owner/Consumer를 검증한다.

`historical-baseline` 실제 Public surface에는 다음 semantic package가 존재한다.

```text
cpf-core/src/main/java/com/cpf/core/
├─ api/config              # topology-independent config descriptor/policy contract만
├─ api/context             # 표준 Context contract/value
├─ api/data/encryption     # data-protection semantic contract/value만
├─ api/error               # error taxonomy/fallback semantics
├─ api/security/crypto     # crypto/security semantic contract/value만
├─ api/transaction         # transaction semantic contract/value만
├─ api/version             # minimal version value/contract
└─ spi/context             # genuine interchangeable context provider SPI
```

위 package 존재 자체를 결함으로 보지 않는다. 대신 각 Type마다 `Public API/SPI인가 → topology independent인가 → 실제 Consumer가 있는가 → runtime/provider/AutoConfiguration 구현을 품고 있지 않은가`를 검사한다. Admin/Batch/Telemetry/Filter/Persistence/Provider/AutoConfiguration/선택 OSS 구현은 올바른 Starter/Capability owner로 이동한다.

- `historical-baseline` 기준 `cpf-starters/foundation/**` 물리 Root는 없다. 다시 이관 작업을 만들지 말고 **재도입 0**을 검증한다.
- Base/Foundation semantics의 물리 Owner는 `cpf-starters/base`다. Capability-specific Source는 해당 Owner에만 존재해야 한다.
- Core semantic → 정말 Kernel인 경우에만 `cpf-core`.
- `settings.gradle`, Catalog, Generator, Docs가 제거된 `foundation` ownerGroup을 active owner처럼 허용하면 currentize한다.
- 다른 `cpf-starters/**/core`, `*-core` predecessor path/FQCN 잔존을 Repository 전체에서 찾고 0으로 만든다.
- 빈 Directory는 Git 여부와 무관하게 local hygiene one-line이 정리할 수 있게 한다.

#### NXT2-ERROR-001 Error/Exception + DB Catalog 완성

Core 최소 exception taxonomy를 다음으로 고정한다.

```text
CpfException            CPF 공통 base RuntimeException
CpfBusinessException    예상 가능한 업무 규칙/업무 코드
CpfValidationException  입력/계약 검증 실패
CpfSystemException      CPF/framework/system/infrastructure unexpected fault
```

`CpfRuntimeException`은 추가하지 않는다. Java RuntimeException 여부는 기술 구현 특성이지 오류 의미 분류가 아니다. Integration/Security/Persistence/Batch에 별도 handling 가치가 있는 Exception은 각 Capability Owner에 두고 Core 파일 수를 늘리지 않는다.

`CpfErrorCode`는 Framework reserved fallback code만 가진다. 업무/기관 Error Code를 enum에 계속 추가하지 않는다.

현재 `CpfBusinessException(String responseCode, ...)`처럼 responseCode로 messageCode를 문자열 합성하는 구현을 제거한다. Target throw/resolve 경로:

```text
Business Source
  throw CpfBusinessException(errorCode/reference, arguments)
        ↓
Core exception carries reference + safe fallback semantics
        ↓
Common Error Catalog Resolver
        ↓
CMN_RESPONSE_CODE (error/response metadata)
        ↓ messageCode
CMN_MESSAGE (locale/version/template)
        ↓
Capability mapper (Web/Message/Batch/Integration)
        ↓
external safe response + internal audit/log
```

필수 구현:
- DB에 신규 Error/Response Code와 Message를 등록하면 Java enum/source 변경 없이 즉시 조회 가능
- framework reserved code도 DB에서 다국어 message를 override할 수 있으나 category/exposure/retry semantic을 안전하지 않게 약화할 수 없음
- 없는/disabled/not-effective code는 Framework fallback으로 처리하고 audit/metric을 남김
- locale fallback, parameter schema, escaping, masking
- Cache preload/refresh/invalidation, commit-after invalidation, multi-instance version fence
- DB/cache failure 시 raw SQL/exception/secret 외부 노출 금지
- `CmnCpfMessageResolver`, `CmnCpfResponseCodeResolver`의 stale `com.cpf.core.error.*` import 제거
- Web transport HTTP status는 Web owner가 매핑. Common DB Catalog가 HTTP 기술 mapping의 유일 Owner가 되지 않게 분리
- BZA 관리 Consumer에서 Error/Message CRUD/search/paging/version/effective period/audit 연결

필수 Test:
- 등록→조회→throw→resolve→Web response E2E
- 신규 arbitrary business code source change 0
- message locale/fallback/parameter
- duplicate/invalid format/disabled/expired
- DB outage/cache hit, cache outage, refresh race, rollback, multi-instance invalidation
- SystemException masking/500 generic response
- BusinessException safe message
- Validation field error

### 4. P0 — Common을 Starter Capability로 전환

#### NXT2-COMMON-001

`10_07` 기준 독립 Root `cpf-common`은 이미 존재하지 않는다. 아래 Common 기능은 `cpf-starters/common`에 있어야 하며, 이제는 이관 예정 문구가 아니라 **Owner/Consumer/DB Runtime 정합성과 구 Root 재등장 0**을 검증한다.

```text
cpf-starters/common
├─ code
├─ parameter
├─ message
├─ calendar
└─ template
```

현재 `cde/cfg/msg` 같은 축약 package는 개발자에게 직관적인 `code/parameter/message`로 currentize한다. `ref` 중 code/reference-data 의미는 code로 병합한다. `cache/data/mqe/sec/security/sql/tabular/time/validation/sample/common`은 기능을 전수 분류해 Data/Messaging/Security/File/Base/Reference 등 실제 Owner로 이동한다. 단순 삭제로 기능을 잃지 않는다.

`cpf-starter-common` Public Entry를 생성한다. Common은 CPF 소유 Product Service이며 고객 Application이 직접 사용한다. 고객별 공통 업무는 `<customer>-common`이 소유한다.

Common Full Runtime은 CPF Data JDBC 기반을 필수로 조립하고 `cpfDB`의 `CMN_*`를 사용한다. DB Provider 부재/DB 연결 실패를 Memory 성공으로 숨기지 않는다. 명시적 DB-less Calendar fallback처럼 정본에 지정된 예외만 허용한다.

#### NXT2-WEB-001

`10_07` 기준 `cpf-starters/web`이 존재한다. `CpfBaseController`, `CpfController`, Web Context/Error/Validation runtime이 Web Owner에 실제 귀속됐는지 Consumer/FQCN까지 검증하고, Profile이 구현 Owner로 되돌아간 residue만 제거한다.

Controller / Service / Repository의 CPF Golden Path는 **정확한 3단 Class 상속구조**로 통일한다.

```text
CpfBaseController (abstract, Web)
→ DomainBaseController (abstract, Domain Common)
→ BusinessController (concrete)

CpfBaseService (abstract, Base)
→ DomainBaseService (abstract, Domain Common)
→ BusinessService (concrete)

CpfBaseRepository (abstract, Data/Persistence)
→ DomainBaseRepository (abstract, Domain Common)
→ BusinessRepository (concrete)
```

강제 규칙:

- 1단 CPF Framework Base는 `abstract class`이며 직접 생성하지 않는다.
- 2단 Domain Common Base도 `abstract class`이며 직접 생성하지 않는다.
- 3단 Business Class만 concrete class로 구현한다.
- Domain Base를 생략하여 2단 구조로 축소하지 않는다.
- Domain Base를 단순 계층 수 맞추기용 빈 Class로 만들지 않는다. Domain 공통 Context/Validation/Error/Logging·Audit Helper, 공통 정책, Template Method 또는 protected Hook 등 실제 재사용 책임을 가져야 한다.
- 기존 `CpfControllerExtension` / `CpfServiceExtension` / `CpfRepositoryExtension`이 동일 역할이면 기능을 보존하여 Canonical `CpfBase*`로 통합한다. 같은 의미의 Base와 Extension을 병존시키지 않는다.
- 장기 Compatibility Wrapper나 4단 이상 상속 구조를 만들지 않는다.
- `@CpfController` / `@CpfService` / `@CpfRepository` Annotation은 상속구조를 대체하지 않고 Runtime 정책을 보완한다.
- Generator / Generated Domain / `cpf-education` / Sample / Testkit은 이 3단 구조를 설명만 하지 말고 실제 Source로 사용한다.

### 5. P0 — Starter 구조와 개발자 공개명

#### NXT2-STARTER-001 Physical hierarchy

Target root는 정확히 `base/common/web/data/messaging/integration/file/notification/security/platform-operations/profiles`를 사용한다. `cpf-core` 외 `core` 그룹은 만들지 않는다.

Catalog, settings.gradle, BOM, publication, Generator, profile, final artifact catalog가 하나의 Canonical Catalog를 사용한다. Module exactly-one owner group/internal role을 강제한다.

#### NXT2-STARTER-002 개발자 공개 Artifact one-shot rename

Repository에 Release/Tag가 없음을 확인했으므로 구 Public Coordinate를 병존시키지 말고 전체 Consumer를 한 번에 전환한다.

```text
cpf-starter                         유지
cpf-starter-common                  신규
cpf-starter-profile-web-api      -> cpf-starter-web-api
cpf-starter-profile-secure-api   -> cpf-starter-secure-api
cpf-starter-profile-browser-bff  -> cpf-starter-bff
cpf-starter-profile-event-service-> cpf-starter-event
cpf-starter-profile-batch-service-> cpf-starter-batch
```

개발자 선택 Provider 공개명:

```text
cpf-starter-data-jdbc
cpf-starter-data-mybatis
cpf-starter-data-jpa
cpf-starter-cache-caffeine
cpf-starter-cache-redis
cpf-starter-cache-valkey
cpf-starter-lock-valkey
cpf-starter-session-jdbc
cpf-starter-session-valkey
cpf-starter-messaging-kafka
cpf-starter-messaging-rabbitmq
cpf-starter-messaging-jms
cpf-starter-messaging-ibm-mq
cpf-starter-object-storage-s3
cpf-starter-graphql
cpf-starter-realtime
cpf-starter-oidc
```

Internal shared runtime은 Public BOM/README 선택 목록에 노출하지 않는다. 예: `data/cache/spring-data-redis`.

#### NXT2-STARTER-003 Composition

- `cpf-starter` = Base + Common + Common DB 구동에 필요한 Data JDBC foundation.
- Deployable당 Top-level Profile exactly-one.
- `secure-api`는 Web Capability를 내부 조립하고 Application이 `web-api + secure-api`를 동시에 참조하지 않는다.
- Generated Domain은 Internal Leaf project/package를 직접 참조하지 않는다.
- Provider collision/duplicate AutoConfiguration은 startup Fail-Fast.

Gateway의 현재 dependency를 최신 Source에서 다시 확인한다. 과거 stale `:cpf-foundation`, `:internal:security:runtime`, `:internal:integration:runtime`, `:internal:platform-operations:runtime`, 중복 Profile이 이미 제거됐다면 다시 추가하지 않는다. 남아 있다면 제거한다. 최종적으로 Gateway는 Public Contract/Starter만 소비하고 exactly-one Top-level Profile 규칙을 지킨다.

### 6. P0 — CPF Platform DB Consolidation

#### NXT2-DB-001

Canonical relation DB target:

```text
CPF_PLATFORM_DB (default physical name: cpfDB)
  CPF_*  CMN_*  ADM_*  BAT_*  GW_*  SEC_*  OPS_*

BZA_DB (bzaDB)
  BZA_*

CUSTOMER_BUSINESS_DB
  MBR_*  ACC_*  PRD_* ...
```

`cmnDB`, `admDB`, `batDB`를 별도 target physical DB로 유지하지 않는다. Gateway DB를 신규 생성하지 않는다. `refDB`는 Production 기본 DB에서 제거하고 Reference/Test Fixture로 한정한다.

현재 canonical 215 table을 전수 mapping한다. 각각 `currentLogicalDatabase, currentName, logicalOwner, targetDatabaseRole, targetTableName/prefix, migrationOwner, runtimeConsumer, atomicityClass`를 확인한다.

- Common: 기존 `cpf_code/cpf_config/cpf_message/cpf_response_code`와 cmnDB Calendar/Template를 `CMN_*` ownership으로 일관되게 currentize
- Admin: `ADM_*`
- Batch: `BAT_*` (Spring Batch 표준 table도 Batch ownership metadata 유지)
- Gateway: `GW_*`
- Security: `SEC_*`
- Platform Ops: `OPS_*`
- 순수 CPF 공통 control metadata만 `CPF_*`

Physical DB 통합은 cross-owner direct update를 허용하지 않는다. Owner별 Migration/API/credential/pool/timeout/health를 유지한다. Cross-owner FK/Join은 기본 금지한다.

Oracle/PostgreSQL/MariaDB 모두 Fresh Install→Upgrade→Rollback/Forward Recovery→Reapply→Runtime Query→Drift/Checksum을 검증한다.

#### NXT2-DB-002 Customer Business DB

Generator에서 `CPF_SCHEMA_NAME=mbrDB`, `CPF_DATABASE_NAME=mbrDB` 같은 Domain DB 생성을 제거한다. Manifest는 `businessDatabaseRole`, `tablePrefix`, `ownerDomain`, `databaseVendor`를 사용한다.

Business transaction과 원자성이 필요한 Outbox/Inbox/Idempotency/Recovery state는 Customer Business DB에 둔다. cpfDB로 이동시켜 XA를 강제하지 않는다.

Bulk Log/Trace/Metrics/File payload는 cpfDB에 무제한 적재하지 않는다. Policy/index/metadata만 cpfDB에 두고 payload store를 분리한다.


#### NXT2-DBVENDOR-001 — Canonical One Source / 3 Vendor Generated Pack 강제

Oracle/PostgreSQL/MariaDB 3개 공식 지원은 유지한다. **개발자가 DDL/DML/Migration 3벌을 직접 병행 유지하는 현재/잠복 구조를 제거한다.** MySQL/MSSQL/H2는 공식 Vendor/Generator option/QA Evidence에 추가하지 않는다.

구현 Target:

```text
cpf-tools/db/canonical/**
  platform-schema.json
  seed-model.json
  platform-non-table-objects.json
  + logical datatype semantics
  + migration intent/catalog
             ↓
     CPF DB Vendor Renderer
      ┌──────┼───────┐
      ↓      ↓       ↓
   oracle postgresql mariadb
      ↓      ↓       ↓
 generated pack / explicit override
```

강제 구현 규칙:

1. Canonical Model이 Table/Column/Key/Constraint/Index/Seed/Non-table Object/Owner/Prefix의 의미론을 소유한다.
2. 하나의 Canonical Change에서 3 Vendor Pack을 생성할 수 있는 Renderer/Generator를 `cpf-tools/db`의 정식 Owner로 구현한다. 기존 Vendor Pack 기능을 삭제해서 맞추지 말고 Canonical 재현 구조로 currentize한다.
3. Vendor Pack은 generated와 override를 식별 가능하게 만든다. Override는 Canonical로 표현 불가능한 Vendor 고유 차이에만 허용하며 `canonicalId/vendor/owner/reason/testId`를 Manifest에 기록한다.
4. Business/Common/Admin/Batch/Gateway/Generated Domain에서 Vendor명 문자열 분기와 3벌 SQL 복사를 금지한다. Pagination/Upsert/Generated Key/Sequence/Lock/Time/JSON/LOB/Identifier 차이는 Data Capability의 제한된 Dialect/Strategy Owner로 이동한다.
5. Current Schema Snapshot 재생성과 Migration History를 분리한다. Release/Cut-over Baseline으로 고정된 Migration은 immutable/checksum protected이며 Renderer 변경으로 과거 Migration을 다시 생성하지 않는다.
6. 현재 pre-GA DB Consolidation은 Canonical Model에서 한 번 수행하고 cpfDB 통합 결과를 Oracle/PostgreSQL/MariaDB에 Render한다. `cmnDB/admDB/batDB` 통합을 3 Vendor SQL에서 각각 독립 구현하지 않는다.
7. Generator는 `businessDatabaseRole + tablePrefix + ownerDomain + databaseVendor`를 사용하되 `databaseVendor`는 Runtime Pack 선택값이다. Generated Source/SQL을 Vendor별로 fork하지 않는다.
8. DB Static Gate는 canonical schema/duplicate/owner/prefix/type/migration ordering/checksum/unsupported-vendor/raw-vendor-branch를 검사한다.
9. DB Render Gate는 Canonical object/change ID의 Oracle/PostgreSQL/MariaDB mapping 누락 0, generated drift 0, orphan override 0을 검증한다.
10. DB-impact 변경의 Runtime Gate는 3 Vendor 각각 Fresh Install→Seed→Upgrade→Rollback 또는 Forward-Recovery→Reapply→Representative Query/Transaction→Restart/Reconcile→Checksum/Drift를 같은 Scenario ID로 실행한다.
11. 개발 Fast Gate는 Reference Runtime 하나로 빠르게 수행할 수 있으나 한 Vendor PASS를 Vendor3 PASS로 기록하지 않는다. CI 기본 Reference Runtime은 PostgreSQL로 둔다.
12. Docker/Oracle/Network 환경이 없어 일부 Runtime을 못 돌려도 Canonical/Renderer/Test Harness/Static/Render 개발을 끝까지 진행한다. 미실행 Vendor만 `미검증`이며 작업 중단 사유가 아니다.

Acceptance:

- manual triple-maintenance source 0
- Business/Generated raw vendor branch 0
- Canonical → 3 Vendor reproducible render
- explicit override manifest/test coverage 100%
- immutable migration/checksum gate
- Oracle/PostgreSQL/MariaDB lifecycle scenario 존재 및 실행 가능한 Harness
- MySQL/MSSQL/H2 official support/evidence 0
- DB3 Runtime 미실행 항목을 PASS로 기록 0

### 7. P0 — Generated Domain / Education 최종 구조

#### NXT2-GEN-001 Generated Customer Projects — cpf-member(MBR)+cpf-external(EXS) 동시 실생성·실거래 검증

CPF Generator는 특정 member 전용 Generator가 아니다. **모든 고객 업무 Domain을 설정만 바꿔 동일 Engine/Template으로 Root에 생성하는 상용 Domain-neutral Generator**로 완성한다.

##### Canonical 생성 위치

```text
<customer-project-root>/
├─ cpf-member/
│  ├─ online/       # selected
│  ├─ batch/        # selected
│  └─ domain/       # actual multi-consumer shared code only
├─ cpf-external/
│  ├─ online/
│  ├─ batch/
│  └─ domain/       # actual multi-consumer shared code only
└─ cpf-<domain>/    # 일반 고객 Project naming
```

CPF 개발 Repository에서는 Root `cpf-member/`와 `cpf-external/`만 retained regression으로 생성·유지한다. 이 둘은 CPF Product Module/Public Artifact가 아니라 Generated Customer Project다. 제3 임의 Domain은 Repository Root에 생성하지 않는다.

**Generated Project 내부 최소 Surface 강제:** `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 3벌, `<domain>-api`, `<domain>-common`, Domain명 반복 하위 폴더, 선택하지 않은 capability/빈 Directory를 생성하지 않는다. Online Runtime은 `online/`, Batch는 `batch/`, `domain/`은 실제 다중 Consumer 공유 시만, `contract/`는 독립 Public Contract Consumer가 실제 있을 때만 생성한다. Verification/DB3 render는 CPF Tooling/build owner가 관리한다.

##### 현재 없는 Domain은 즉시 실제 Generator로 생성

현재 retained `cpf-member/` 또는 `cpf-external/`이 없다면 정상 상태로 보지 않는다. Generator 작업 초기에 Canonical Schema를 확인하고 **둘 다 fresh generation**한다. 기존 Source 복사나 수동 Skeleton 작성으로 대신하지 않는다.

##### 공식 member 입력

```yaml
domain:
  name: member
  moduleName: member
  systemCode: MBR
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: MBR
preset: standard-enterprise
modules:
  online: true
  batch: true
generation:
  sampleTransaction: true
```

##### 공식 external 입력

```yaml
domain:
  name: external
  moduleName: external
  systemCode: EXS
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: EXS
preset: standard-enterprise
modules:
  online: true
  batch: true
generation:
  sampleTransaction: true
```

##### Domain-neutral 강제

Generator/Template/Script/Build Logic에 `member/MBR/external/EXS` 특수분기를 두지 않는다. 모든 Domain 차이는 Canonical Metadata/Naming Strategy로만 결정한다. 이후 account/product/loan/order 등도 같은 Schema로 생성 가능해야 한다.

##### 기본 Public Starter 자동 구성

Generated Domain 직접 dependency는 최신 Starter Catalog의 `visibility=public` Artifact만 사용한다.

API:

```text
cpf-starter-secure-api
cpf-starter-data-mybatis
+ 필요한 Capability의 Public Composition/Profile/Provider
```

Batch:

```text
cpf-starter-batch
cpf-starter-data-mybatis
+ 필요한 Capability의 Public Composition/Profile/Provider
```

`currentization_source_sha` Catalog에서 `cpf-starter-integration-http`, `cpf-starter-integration-resilience`는 Internal-only leaf다. Generated Domain이 직접 참조하지 않는다. HTTP/Resilience가 필요하면 Public Profile/Composition 내부에서 소비하도록 Framework Public Surface를 구현한다. Public Composition이 없으면 Generated Domain의 Internal 직접 참조로 우회하지 않는다.

Internal leaf/project/package direct dependency, legacy `cpf-common`, legacy profile coordinate를 생성하지 않는다. 선택하지 않은 Provider도 강제하지 않는다.


##### 기본 실제 Sample Transaction

Skeleton/Hello World만 생성하면 FAIL이다. 각 Domain은 생성 직후 개발자가 실제 DB 거래를 수행할 수 있는 `SampleTransaction`을 제공한다.

```text
member   → MBR_SAMPLE_TX
external → EXS_SAMPLE_TX
```

최소 Column:

```text
ID / BUSINESS_KEY / STATUS / REQUEST_VALUE / RESULT_VALUE
CREATED_AT / CREATED_BY / UPDATED_AT / UPDATED_BY / VERSION
```

최소 API:

```text
POST Create
GET Detail
GET Search/Page
PUT Update
```

실제 호출경로:

```text
HTTP → CPF Security/Web → Context/transactionId → Validation
→ Controller → Service Transaction → Repository/Mapper → CUSTOMER_BUSINESS_DB
→ CPF Error/Response → Logging/Audit
```

##### 3단 Base 실제 생성

Controller:

```text
CpfBaseController → MemberBaseController/ExternalBaseController → SampleTransactionController
```

Service:

```text
CpfBaseService → MemberBaseService/ExternalBaseService → SampleTransactionService
```

Repository:

```text
CpfBaseRepository → MemberBaseRepository/ExternalBaseRepository → SampleTransactionRepository
```

CPF/Domain Base는 abstract, Business는 concrete다. Domain Base는 실제 reusable behavior를 갖고 빈 ceremonial class를 금지한다. 동일 역할 `Cpf*Extension` 병존과 4단 wrapper도 금지한다.

##### DB / DB3

두 Domain 모두 `CUSTOMER_BUSINESS_DB`를 사용한다. `memberDB/mbrDB/externalDB/exsDB`를 만들지 않는다.

동일 Generated Application Source로 Oracle/PostgreSQL/MariaDB를 검증하고 Vendor별 업무 Source fork/if-else를 금지한다.

##### Test / Runtime-ready

각 Domain에 Controller/Service/Repository/Validation/Error/Context/Transaction/Integration/Batch Test를 생성한다. 가능한 환경에서 Create→DB Insert→Read→Update→DB Select E2E를 실제 실행한다. OpenAPI와 Sample Batch도 실행 가능해야 한다.

##### Root Build / DX

Generator는 실제 선택된 `online/`, `batch/`와 실제 Consumer가 있는 `domain/`/`contract/`만 Build Graph에 연결하고 Convention/BOM/Test 설정을 맞춘다. CPF 검증 Repository에서는 Build/Test Graph에 포함하되 Public BOM/Publication/Product Artifact Catalog에는 넣지 않는다.

Generated Project 내부 README를 만들지 않는다. Run/Test/API/OpenAPI/DB/Batch/Regenerate 안내는 CPF CLI help, Canonical Guide, EDU에서 제공한다. 개발자가 대량 수동 수정 없이 바로 테스트할 수 있어야 한다.

##### 두 Domain 동시 회귀

Framework/Generator/Starter/Base/Annotation/Data/DB/Build 변경 시:

```text
member regenerate/compile/test/runtime
+
external regenerate/compile/test/runtime
```

을 같이 수행한다. 한 Domain만 성공해서 완료 처리하지 않는다.

##### Normalized parity / Hardcoding scan

member↔external에서 DomainName/SystemCode/Package/TablePrefix/Port만 normalize하고 구조/Starter/Base/Sample Transaction/Test 품질을 비교한다. Generator/Template 전체에서 member/MBR/external/EXS 특수분기 0을 확인한다.

필요하면 제3 임의 Domain을 `build/domain-generator/verification/<scenario>/`에 fresh 생성해 genericity를 추가 검증하고 cleanup한다. 이 cleanup은 retained cpf-member/cpf-external에 적용하지 않는다.

##### Lifecycle

반드시 검증:

```text
create
add domain
dry-run
diff
regenerate
idempotent rerun
upgrade
remove
restore/regenerate
user-owned modification protection
```

`add external`이 기존 member를 훼손하거나 이후 다른 Domain 추가가 기존 Root를 덮어쓰면 FAIL이다.

##### 보존 / 삭제

cpf-member/cpf-external은 개발기간 동안 유지하고 최종 Repository/ZIP에도 포함한다. 일반 Cleanup/Hygiene로 삭제하지 않는다. Lifecycle 검증에서 일시 삭제하면 같은 작업 단위에서 Canonical Generator로 다시 생성·검증한다. Source 복사/수동 작성 복구는 금지한다.

##### Final Evidence

각 Domain의 input/lock/source hash, generated file count, Starter composition, compile/test/runtime, Sample DB Transaction, DB3, dry-run/diff/regenerate/idempotency/upgrade/remove/restore, user-owned 보호를 기록한다. 공통으로 normalized parity, hardcoding scan, Public/Internal dependency, 3단 Base, Annotation runtime consumer, Korean comment gate를 기록한다.


#### NXT3-EDU-001 `cpf-education` Canonical Surface / Active Alias 0 — 선택권 없음

`cpf-education` Root, `com.cpf.education.*`, `EducationApplication`, System Code `EDU`를 유일한 Canonical Education Surface로 유지한다.

완료 시 다음을 보장한다.

- 다른 Education Product Root/Package/Application alias의 Active Surface 0
- README/Guide/Generator/Settings/Catalog/Test가 `cpf-education`만 현재 Module명으로 사용
- Education Source가 실제 Public Function/Config/API를 실행하는 Consumer를 제공
- Education 자체 아래에 중복 의미의 최상위 `edu` 기능 그룹을 만들지 않음
- 과거 명칭이나 회차별 migration 지침을 current 개발 정본에 유지하지 않음

#### NXT3-EDU-002 Education Package Information Architecture — 아래 구조로 통일

`cpf-education`은 아래 Target IA를 기준으로 기능 package를 분류하고 **Canonical Education Surface만 유지**한다.

이 구조를 그대로 이름만 `education`으로 바꾸는 것을 금지한다.

최종 Education Source의 기능 Root는 아래 범주를 사용한다. **다른 임의의 최상위 기능 폴더를 추가하지 않는다.**

```text
com.cpf.education
├─ base
├─ common
├─ web
├─ data
│  ├─ persistence
│  ├─ cache
│  └─ lock
├─ transaction
├─ messaging
├─ integration
├─ file
├─ security
├─ batch
├─ operations
├─ generator
├─ scenarios
└─ verification
```

기존 예제는 실제 기능 Owner를 보고 재분류한다.

예시 규칙:

- `crud/query/pagination/detail/header/validation` → 실제 Web/Data 사용 성격에 따라 `web/**` 또는 `data/**`
- `archive/attachment/filetransfer` → `file/**`
- `servicecall/external/telegram/soap/graphql/http/fixed-length/tcp` → `integration/**`
- broker/outbox/inbox → `messaging/**`
- cache → `data/cache/**`
- persistence/mapper/repository → `data/persistence/**`
- idempotency/recovery/transaction → `transaction/**`
- audit/logging/health/gateway/platform-control` → `operations/**`
- batch/center-cut → `batch/**`
- 여러 Capability가 함께 동작하는 E2E 업무 예제 → `scenarios/**`
- Gate/verification-only fixture → `verification/**`

단순 Folder move가 아니라 Package, import, Test, resource, OpenAPI, README/Index, 호출 URL, Generator reference를 모두 함께 변경한다.

#### NXT3-EDU-003 Education 예제 품질 강제

모든 Education/Sample은 **CPF Framework가 지원하는 기능을 우선 사용한다.**

CPF가 제공하는 Context, Error, Validation, Base Controller/Service/Repository, Transaction, Persistence, Cache, Lock, Messaging, Integration, Security, File, Batch, Observability, Audit, Paging, OpenAPI Client 기능이 있는데 Spring/OSS Native API를 직접 사용하여 Framework Golden Path를 우회하면 결함이다.

Native API 직접 사용은 다음 두 조건을 모두 만족할 때만 허용한다.

1. CPF가 해당 기능을 의도적으로 Wrapper/추상화하지 않는다는 정본 근거가 있다.
2. 예제에 “Advanced Native Extension”임을 명확히 표시하고 일반 Golden Path 예제와 분리한다.

각 주요 Capability에는 최소한 다음 예제를 둔다.

- 정상 처리
- 입력/업무 오류
- Infrastructure/System 오류
- Retry/Recovery 또는 해당 기능의 복구 흐름
- 권한/보안 실패가 있는 기능은 401/403
- 충돌/동시성 기능은 409
- 제한 기능은 429
- 외부/Runtime unavailable은 503
- 필요한 경우 UNKNOWN/Reconcile

예제는 compile되지 않는 설명용 Source, Consumer 없는 Annotation/DTO, Mock-only Example로 완료 처리하지 않는다.

### 8. Redis/Valkey 선구현 Closure

#### NXT2-REDIS-001

현재 Canonical Catalog의 다음 Redis/Valkey 구조는 자산으로 유지한다.

```text
data/cache/spring-data-redis
data/cache/redis
data/cache/valkey
```

다시 복제 개발하지 않는다. Public 이름만 `cpf-starter-cache-redis`, `cpf-starter-cache-valkey`로 currentize하고 Common/Generator/Profile Consumer를 새 Architecture에 연결한다.

필수 검증:
- Redis 연결/PUT/GET/EVICT/TTL/serialization
- server unavailable/startup policy
- runtime disconnect/reconnect
- timeout/partial failure
- multi-instance invalidation/version fence
- Redis+Valkey 동시 classpath collision Fail-Fast
- 명시 Redis 선택이 Valkey로 fallback하지 않음
- Valkey 기존 기능 전체 regression
- live runtime 미실행을 PASS로 기록하지 않음

### 9. P0 — ADM / BZA / Gateway Framework 사용 경계

#### NXT3-ADM-BZA-001 ADM/BZA도 CPF Framework를 사용한다

ADM과 BZA는 CPF Framework를 검증하는 First-party Application이다. 따라서 Framework가 제공하는 다음 기능을 우회하지 않는다.

- Context/Transaction ID
- Error/Exception/Validation
- Security/Authorization
- Audit
- Logging/Masking
- Web Base/Request/Response contract
- Paging/Search
- Persistence/Transaction
- Cache
- Integration/Resilience
- OpenAPI/Generated Client
- Runtime health/metrics

단, **ADM은 Control Plane이므로 자신이 관리하는 Runtime과 동일 장애 도메인에 묶여 같이 죽는 구조를 금지한다.**

예를 들어 ADM이 관리 대상 Service의 Cache/Message Broker/Runtime Registry가 장애났을 때 ADM의 로그인·감사·조회·복구명령 자체까지 사용할 수 없게 만드는 순환 의존을 금지한다.

ADM은 CPF Public API/Starter를 사용하되 다음을 분리한다.

- Control Plane bootstrap path
- Management credential/pool/timeout
- 관리 대상 Runtime 호출 channel
- Audit/log destination
- 위험 조치 승인/사유/결과 추적

Framework Logging을 사용하므로 ADM/BZA 로그도 CPF 표준 Context/Masking/Correlation을 따른다. 다만 `systemCode`, `application`, `instance`, `transactionId`, `operationType`으로 구분하고, 관리 대상 Runtime 로그와 Control Plane Audit를 논리적으로 식별 가능하게 한다. ADM의 로그 수집/조회 기능이 자신의 로그 sink 장애와 재귀적으로 결합되지 않게 한다.

#### NXT3-ADM-BZA-002 Common Product Service 실시간 운영

Code / Parameter / Message / Calendar / Template는 Common Product Service가 소유한다.

BZA/ADM에서 해당 데이터를 변경한 뒤 재기동을 요구하면 미완료다.

정확한 Runtime 흐름:

```text
ADM/BZA 화면 또는 API
→ 권한/승인/변경사유
→ Common Management Command API
→ Owner DB Commit
→ Catalog Version 증가
→ Commit-after invalidation event
→ 모든 Instance local cache/Redis/Valkey invalidate 또는 refresh
→ 신규 Business 요청이 변경값 사용
→ 누락 event는 version checkpoint/reconcile로 복구
→ 변경/전파/실패/복구 Audit
```

필수 Test:

- Instance A에서 수정 후 Instance B Business Consumer가 최신 Code/Message/Parameter 사용
- event loss 후 reconcile
- cache outage
- broker outage
- DB rollback 시 잘못된 invalidation 금지
- duplicate event idempotency
- concurrent update/version fence
- disabled/expired/effective-date
- 권한 없는 변경 403
- 승인 필요한 위험 변경 승인 전 미반영
- Audit에 Secret/민감정보 원문 0

단순 “cache clear button”만 구현하면 완료가 아니다.

#### NXT3-ADM-BZA-003 Frontend/OpenAPI 실제 Consumer

ADM/BZA 화면은 Backend OpenAPI 기반 Generated Client를 실제 사용한다.

각 메뉴는 최소 다음을 지원한다.

- Search/filter
- Paging
- Detail
- Create/Update/Delete 또는 기능상 필요한 Command
- 상태 표시
- 권한별 버튼/메뉴 제어
- 위험조치 사유/승인
- 성공/실패 Audit 결과
- 401/403/404/409/429/500/503
- Loading/Empty/Error state
- 접근성/Keyboard
- Responsive
- 외부 Runtime CDN/Font/Script 의존 0

Backend API만 있고 메뉴/화면 Consumer가 없거나, 화면 Mock만 있으면 완료가 아니다.

Gateway는 독립 Runtime이고 DB는 cpfDB의 `GW_*` ownership을 사용한다. ADM은 Gateway Operations Public API를 통해 제어하며 Gateway 내부 Package/DB Table을 직접 조작하지 않는다.

### 10. Context Architecture Closure

Core Context는 Immutable minimal execution semantics로 유지한다. HTTP/Message/Batch/Gateway/Security Context는 각 Adapter가 Core Context를 compose/translate한다. Context 상속/클래스 수를 늘리지 않는다. transactionId는 최초 정식 inbound에서 1회 생성 또는 규격 외부 ID를 수용하고 Retry에서 새 ID를 만들지 않는다.

### 11. P0 — Build / Config / Hardcoding / Extensibility

#### NXT3-CONFIG-001 Hardcoding 전수검사

Repository 전체에서 다음 값을 전수 검색한다.

- URL/Host/Port/Path
- DB vendor/schema/database/user
- timeout
- retry/backoff/jitter
- pool size
- batch chunk/parallelism
- cache TTL
- header name/value
- locale/date/time format
- topic/queue/group
- storage bucket/path
- security policy
- rate limit
- feature flag
- error/message/status code
- runtime control endpoint
- page size/max size
- file size
- scheduler interval
- reconciliation interval

각 값은 정확히 세 종류로 분류한다.

1. **Immutable Framework Contract** — 코드 상수 유지 가능
2. **Framework Default Policy** — 기본값은 코드에 둘 수 있으나 Property/Programmatic override 제공
3. **Environment/Operation Value** — 반드시 외부화

무조건 Property로 빼는 것도 금지하고, 환경값을 Source에 고정하는 것도 금지한다. 분류 근거를 Source JavaDoc/Property comment/Guide에 남긴다.

#### NXT3-CONFIG-002 Property-only Framework 금지

설정 가능한 기능은 Property만 제공하고 Java 코드에서 확장할 수 없게 만들지 않는다.

적용 가능한 기능은 다음 중 적절한 Public 확장 경로를 제공한다.

- Builder
- Customizer
- Strategy
- Provider/SPI
- Bean override
- Programmatic Configuration API
- protected Template Hook

우선순위는 명확해야 한다.

```text
safe framework default
→ configuration property
→ application programmatic override/customizer
→ explicit runtime control (허용된 기능만)
```

부분 옵션 노출도 결함이다. 예를 들어 timeout만 외부화하고 retry/backoff/jitter가 hardcoded라면 완료가 아니다.

Advanced Developer가 Internal package 접근이나 Framework fork 없이 확장 가능해야 한다.

#### NXT3-CONFIG-003 Build/Compatibility

- root `build.gradle`/`settings.gradle`은 선언 중심으로 유지
- 재사용 검증 로직은 `cpf-tools/build` 또는 `cpf-tools/verification`의 명확한 Owner로 둔다
- `cpf.*` canonical config namespace
- Provider explicit selection
- Provider collision Fail-Fast
- Secret raw value를 metadata/evidence/log에 기록하지 않음
- Public/API/SPI/Internal visibility catalog/build gate
- 새 Public 이름으로 settings/BOM/publication/generator/education/README impact 동시 수정
- compatibility shim/duplicate public artifact 장기 병존 금지


#### NXT3-CONFIG-004 Configuration → Binding → Invocation 상용 Closure

이번 작업에서는 `Property가 있다`, `configPrefix가 있다`, `AutoConfiguration이 있다`를 Configuration 완료로 판정하지 않는다.
64 Starter/Capability 전체에 아래 연결을 적용한다.

```text
Logical Definition
→ Runtime Binding
→ Typed Config / Native Config Bridge
→ Schema / Validation / Secret Ref
→ Profile / Environment
→ AutoConfiguration
→ Binding Registry / Bean / Typed Client
→ Business Consumer
→ Health / Diagnostics / ADM
→ Audit / Drift / Rollback
→ Generator / EDU
→ Runtime / Fault Evidence
```

##### A. 기존 자산 우선 재사용

현재 Source에는 이미 다음 기반이 있다.

- `CpfConfigPolicy`, `CpfConfigCatalog`, `CpfConfigDescriptor`.
- `CpfConfigurationPolicyCatalog`.
- `CpfCapabilityBindingRegistry`.
- `CpfServiceClient`, `CpfTypedServiceClient`.
- `CpfServiceCallEngine`, `CpfServiceRegistry`, `CpfServiceInstanceRegistry`.
- endpoint resolver/registry, health-aware instance selector, routing policy, WebClient transport.

따라서 별도 `callRemote`/별도 Config Framework/별도 Registry를 평행 생성하지 않는다.
기존 자산을 Canonical Owner/Public API/SPI/Runtime 경계에 맞게 currentize하고 실제 Consumer를 연결한다.

현재 확인된 `CpfConfigurationPolicyCatalog`의 stale `com.cpf.foundation.api.config.*` import와
실제 `com.cpf.core.api.config.*` 계약 drift는 compile/source currentization 대상으로 처리한다.

##### B. CPF Domain Call

MBR→EXS, MBR→ACC 등 CPF 관리 Domain 호출은 같은 JVM/별도 WAS/별도 IP/MSA와 무관하게 동일 Typed Contract를 사용한다.

업무 Source 목표:

```java
@CpfInject
private ExsClient exsClient;

return exsClient.verify(request);
```

또는 existing Public API와 정합된 동일 의미의 Typed Client.

금지:

```java
callRemote("http://10.0.0.10:8080", request);
applicationContext.getBean("exsClient");
```

Runtime은 logical Domain/SystemCode/ServiceId에서 Local/Remote Binding을 결정한다.

- LOCAL → managed local proxy/adapter.
- REMOTE → registry → healthy instances → routing → transport.
- AUTO가 있다면 Local/Remote ambiguity/fallback 규칙을 명확히 하고 fail-fast한다.
- Local/Remote 모두 Context/TxId/Security/Timeout/Error/Trace/Idempotency 의미를 동일하게 유지한다.
- Local이라고 cross-domain hidden transaction을 새로 만들지 않는다.
- Remote internal call은 Gateway를 재경유하지 않는다.
- static VIP/DNS와 runtime registry fallback/priority를 명시한다.
- zone/version/weight/maintenance/drain/lease/TTL/stale instance를 실제 Routing에 사용한다.

topology-independent Domain Contract/Registry/Router API/SPI는 core가 소유하고,
HTTP/WebClient 등 Transport 구현은 Integration Runtime가 소유한다.

##### C. External Integration은 별도 Setup

내부 Domain Registry와 외부기관 Binding을 하나의 `serviceId/baseUrl` map으로 뭉개지 않는다.

External Client 예:
- `bank-host`
- `credit-agency`
- `card-company`

각 Named Binding은 transport에 따라 endpoint, host/port/destination, codec/layout/schema,
TLS/mTLS, secret/cert ref, timeout/pool, retry, CB, bulkhead/rate,
idempotency/correlation/masking, UNKNOWN/probe/reconcile, health/drain을 가진다.

EXS에서:

```java
@CpfInject
private BankHostClient bankHostClient;

return bankHostClient.inquire(request);
```

처럼 Typed Client를 사용하고 TCP/HTTP/SOAP/ISO8583 details는 내부 Adapter가 처리한다.

EXS를 모든 외부호출의 Framework 강제 Gateway로 만들지 않는다.
Framework는 직접 External Integration도 지원하되 고객 Architecture Policy가 EXS 집중화를 선택할 수 있어야 한다.

##### D. Binding Cardinality

현재 Binding Registry처럼 모든 capability에 정확히 하나의 default를 일괄 요구하지 않는다.

Capability별로:
- SINGLE_DEFAULT_REQUIRED
- NAMED_MULTI_OPTIONAL_DEFAULT
- EXPLICIT_ONLY
- INTERNAL_NO_PUBLIC_BINDING

또는 동등한 Canonical policy를 선언한다.

Provider slot(cache/persistence/security mode 등)의 exactly-one과,
bank-a/bank-b 같은 named client 다중 binding을 구분한다.

TCP/SFTP/S3/HTTP external client/Notification 등 다중 대상이 자연스러운 기능은
단일 global bean/config 전제를 전수검사한다.

##### E. Native Provider Config Bridge

Spring Boot/Provider 설정을 CPF prefix로 무의미하게 복제하지 않는다.

CPF Config Catalog에는:
- CPF semantic prefix/owner.
- 필요한 native property prefix/bean.
- secret owner/ref.
- preflight condition.
- Native Escape.

를 연결한다.

Redis/Kafka/OIDC/DataSource/AWS 등 표준 설정을 사용할 경우 실제 native property가 무엇인지 Guide/Diagnostics/Test가 보여야 하고,
선택 Capability인데 필수 native bean/config가 없으면 명확한 startup/preflight 오류를 낸다.

##### F. Config Catalog / Lifecycle

64/64 module `configPrefix`에 대해 최소 다음 Metadata를 작성·검증한다.

`prefix/id | owner | capability | scope | property/schema | required/default |
constraints | secretSeparated | mutability | runtimeOverrideAllowed |
maskedDisplay | nativeDependency | version/deprecation/alias | effectiveSource | healthImpact`

- duplicate/orphan/unknown/stale CPF prefix detect.
- `REFRESHABLE`과 `RESTART_REQUIRED` 실제 처리 분리.
- runtime refresh는 atomic immutable snapshot으로 성공하거나 previous-good 유지.
- runtime override는 permission/reason/version/audit/rollback.
- secret 원문 UI/log/evidence 0.
- multi-instance config version/hash drift detection.

##### G. Generator

Generator의 `cpf-domain.yaml`에는 논리 구조를 추가할 수 있다.

- Domain/SystemCode.
- `domainDependencies` 또는 동등 계약.
- `externalClients` 또는 동등 named client/channel definition.
- selected Starter/provider/binding names.

그러나 실제 IP/URL/password/token/certificate secret을 generator definition에 저장하지 않는다.

Generated config는 선택 Capability에 필요한 환경 binding skeleton/env reference를 생성한다.
미선택 Capability의 config/bean/thread/endpoint는 0이어야 한다.

반드시 실제 생성/검증:
- MBR→EXS same JVM.
- 동일 MBR Source → EXS separate WAS/IP.
- MBR→ACC 또는 ephemeral third domain.
- EXS→HTTPS external.
- EXS→TCP fixed-length external.
- 같은 transport external named clients 2+.
- missing/duplicate/invalid binding negative test.

##### H. Setup Family 64-module 전수검사

`CPF_CONFIGURATION_SETUP_AND_INVOCATION_MATRIX.md`를 실행 Matrix로 사용한다.
각 Starter/Runtime은 configPrefix만 확인하지 말고 Setup owner/cardinality/native dependency/actual consumer를 채운다.

특히:
Base/Web/Persistence/Transaction/Cache/Lock/Session/Messaging/Domain Call/Integration/
File/SFTP/Object/Notification/Security/Observability/Common/Batch/Generator를 빠뜨리지 않는다.

##### I. ADM / Operations

다음 route는 단순 화면 Skeleton이 아니라 Configuration Runtime Consumer다.

- `ops-config`
- `ops-topology`
- `external-institutions`
- `ops-drift`

`Config Catalog → Backend API → Generated Client → Page → Permission → Reason/Approval → Audit`
까지 연결한다.

Raw Secret 표시/수정 금지.
Endpoint/Certificate/Binding Health와 config source/version/mutability/restart-required/drift를 운영자가 볼 수 있게 한다.

##### J. Diagnostics / Preflight

기존 Tooling/Diagnostics를 확장하여 최소:
- active profile/capability/provider.
- Domain/External named binding.
- effective config source/version/hash.
- masked endpoint/secret reference.
- missing/unused/unknown config.
- provider/native dependency.
- restart-required change.
- unhealthy/draining instance.
를 확인 가능하게 한다.

새 CLI를 무조건 만들지 않고 기존 `cpf`/preflight/diagnostics owner를 우선 재사용한다.


##### L. CPF 전체 Default / 한글 설정주석 / Source Override — 추가 강제

이 요구는 Gateway/Integration에만 적용하지 않는다. CPF 전체 설정을 Repository-wide inventory하고 currentize한다.

1. local/test host는 필요 시 `127.0.0.1`; dev/stg/prod silent localhost fallback 금지.
2. 신규 Generated Domain은 stable local port + collision preflight. 기존 Canonical Port는 호환성 검토 없이 변경 금지.
3. 실행 Application은 `application.yml`, `application-local.yml`, `application-test.yml`,
   `application-dev.yml`, `application-stg.yml`, `application-prod.yml`의 역할을 구분한다.
4. 선택 Capability 주요 option은 사용하지 않더라도 commented example로 노출하고
   역할/기본값/허용값/단위/적용범위/우선순위/REFRESHABLE·RESTART_REQUIRED/Secret/운영주의/실패조건을 한글로 설명한다.
5. `@ConfigurationProperties`/Config object의 public property에 한글 JavaDoc을 붙이고 IDE metadata/YAML/default/validation을 일치시킨다.
6. property별 `CONFIG_LOCKED / SOURCE_DEFAULT / SOURCE_CUSTOMIZABLE / PER_CALL_BOUNDED / RUNTIME_MANAGED` 정책을 둔다.
7. Timeout/Retry/TTL 등 허용값은 Typed Customizer/Builder/CallOptions로 조정 가능하게 하되
   production endpoint/credential/TLS/auth/secret은 Source에서 우회할 수 없게 한다.
8. core/starters/common/data/integration/messaging/file/notification/security/platform-operations/web/profiles/
   gateway/batch/admin/biz-admin/member/external/education/generator/tools/environment/deploy를 전수 확인한다.
9. URL/IP/port/path/timeout/retry/TTL/pool/thread/concurrency/env/JVM property를 전수 검색하고 환경 종속 hardcoding을 Config/Binding으로 이동한다.
10. Gateway `application.yml`, `CpfGatewaySafetyProperties`, Route/Registry/Runtime/SCG/Control Plane을 전수 대조한다.
11. YAML 주석 default와 Java default 불일치, undocumented major property, prod localhost/sample fallback,
    consumer 없는 property, security lock을 우회 가능한 Source override는 Gate FAIL.

##### K. 완료 Test

- 64/64 Config metadata coverage.
- selected/unselected zero-footprint.
- Local/Remote Domain Call source parity.
- multi-instance failover/drain/version/weight.
- external named multi-client same transport.
- duplicate/default cardinality fail.
- missing endpoint/native bean/secret fail.
- hot refresh success / invalid refresh rollback.
- restart-required live apply 차단.
- multi-instance drift.
- secret masking.
- Generator/member/external/EDU actual setup.
- current exact-SHA Runtime/Fault Evidence.

위 중 필요한 항목이 빠지면 `Config/Profile/Integration/Starter DX 완료`로 판정하지 않는다.

### 12. P0 — Repository Information Architecture / Garbage / 직관적 이름

과거 currentization 기준에서 `cpf-tools`에는 `config`, `performance`, `product-governance`, `promotion`, `runtime-alternatives`, `scripts` 등 역할이 겹치거나 이름만으로 목적이 불명확한 Top-level Directory가 남아 있다. `deploy`에도 `batch/cells/env/inventory/local/schema`가 서로 다른 기준으로 펼쳐져 있다.

**이번 Requirement에서는 “검토만” 하지 않는다. 아래 Canonical Layout으로 실제 정리한다. 다른 구조를 선택하지 않는다.**

#### NXT3-TOOLS-001 `cpf-tools` 최종 Canonical Layout

```text
cpf-tools/
├─ build/
├─ contracts/
├─ db/
├─ environment/
├─ generator/
├─ governance/
├─ release/
├─ runtime/
├─ security/
├─ supply-chain/
├─ testing/
├─ verification/
└─ README.md
```

정확한 Migration 규칙:

```text
cpf-tools/config/**                → 실제 기능 Owner로 이동 후 top-level config 제거
cpf-tools/performance/**           → cpf-tools/verification/performance/**
cpf-tools/product-governance/**    → cpf-tools/governance/product/**
cpf-tools/promotion/**             → cpf-tools/release/promotion/**
cpf-tools/runtime-alternatives/**  → cpf-tools/runtime/profiles/**
cpf-tools/scripts/**               → 각 실제 Owner(db/generator/release/runtime/verification/...)로 이동 후 scripts 제거
```

`cpf-tools/environment/docker-development-test/**`는 보호 경로다. 삭제/임의 이동 금지.

`config`, `scripts`, `misc`, `temp`, `old`, `legacy`, `qa31`, `qa39`, `r8`, `r13`, 날짜/Session/REV 이름 같은 Dumping Directory를 새로 만들지 않는다.

일회성 Migration/QA/Apply Script는 다음 중 하나만 허용한다.

- 현재 Canonical Gate로 통합
- Audit Evidence로만 보존해야 할 경우 `cpf-docs/work/evidence`에서 Source가 아닌 Evidence로 보존
- 더 이상 Consumer가 없으면 `CPF_DELETE_MANIFEST.csv`에 등록

Top-level Tool Directory를 새로 만들려면 반드시 **명확한 단일 Owner + 독립 Lifecycle + 기존 Canonical Owner로 수용 불가한 근거**가 있어야 한다. 그렇지 않으면 생성 금지.

#### NXT3-DEPLOY-001 `deploy` 최종 Canonical Layout

```text
deploy/
├─ environments/
│  ├─ local/
│  ├─ dev/
│  ├─ stg/
│  └─ prod/
│     ├─ services/
│     ├─ inventory/
│     └─ topology/
├─ runtimes/
│  ├─ platform/
│  └─ batch/
├─ ci/
└─ schemas/
```

정확한 규칙:

```text
deploy/env/**       → deploy/environments/**
deploy/local/**     → deploy/environments/local/**
deploy/cells/**     → 해당 environment의 topology/**
deploy/inventory/** → 해당 environment의 inventory/**
deploy/runtimes/batch/**     → deploy/runtimes/batch/**
deploy/schemas/**    → deploy/schemas/**
deploy/ci/**        → 유지
```

Environment별 차이가 있는 값을 top-level shared 파일로 계속 두지 않는다. 정말 공용 Schema/Template이면 `schemas` 또는 명확한 runtime owner에 둔다.

`cells`처럼 조직 내부 은어에 가까운 이름은 사용하지 않는다. 개발자가 이름만 보고 역할을 알 수 있는 `topology`, `inventory`, `environments`, `runtimes`를 사용한다.

#### NXT3-BATCH-STRUCTURE-001 Batch 하위 Module 이름 정확히 변경

Batch IA 구 이름은 Current Target이 아니다. active settings와 실제 Source를 대조해 tracked residue가 남아 있으면 successor parity 후 완전 제거한다.

```text
center-cut-runner
contract
control-server
execution-runtime
host-agent
runtime-common
scheduler
testkit
worker
```

다음 이름으로 **정확히** 변경한다. 다른 대체 이름 선택 금지.

```text
contract           → api
execution-runtime  → runtime
control-server     → control-plane
center-cut-runner  → center-cut
host-agent         → agent
runtime-common     → runtime-support
scheduler          → scheduler
worker             → worker
testkit            → testkit
```

최종:

```text
cpf-batch/
├─ api/
├─ runtime/
├─ scheduler/
├─ worker/
├─ control-plane/
├─ center-cut/
├─ agent/
├─ runtime-support/
└─ testkit/
```

Folder만 rename하지 않는다. `settings.gradle`, project path, Gradle dependency, publication coordinate, package/import, ADM client, Starter Catalog, Generator, OpenAPI, Test, Docs, Runtime Script, Evidence를 전부 같은 변경 단위로 currentize한다.

#### NXT3-HYGIENE-001 실제 Garbage 제거

삭제는 이름만 보고 판단하지 않는다.

순서:

```text
Consumer search
→ replacement/canonical owner 확인
→ 필요한 기능 통합
→ stale reference 0
→ targeted test
→ CPF_DELETE_MANIFEST.csv READY_TO_DELETE
```

Developer GPT는 사용자 승인 없이 실제 삭제하지 않는다. 그러나 **삭제 대상 식별·Manifest 등록을 보류하면 안 된다.**

Protected path:

- `cpf-docs/deliverables/**`
- `cpf-docs/guides/**`
- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`

완료 시 stale one-shot script, duplicate current doc, Session/REV/Date garbage, 사용되지 않는 Tool, old path/FQCN이 0이어야 한다.

### 12A. P0 — Generator Commercial DX 최종화


##### Generated Customer Domain 보호

Root `cpf-member/**`, `cpf-external/**`은 Garbage/Temp가 아니라 공식 Generator 회귀 Domain이다. 일반 Hygiene/Delete Manifest로 제거하지 않는다. lifecycle remove 검증으로 일시 삭제하면 같은 작업 단위에서 Generator로 복구한다. 최종 상태에서 둘 중 하나라도 없으면 `NXT2-GEN-001`, `NXT3-GENERATOR-001~003`, `NXT3-HYGIENE-001` 완료가 아니다.


#### NXT3-GENERATOR-001 Canonical 입력 파일

Generator의 정본 입력은 긴 Interactive Prompt가 아니라 선언형 파일이다.

```yaml
domain:
  name: member
  moduleName: member
  systemCode: MBR

database:
  role: CUSTOMER_BUSINESS_DB
  vendor: postgresql
  tablePrefix: MBR

preset: standard-enterprise

modules:
  online: true
  batch: true

features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
```

Fresh input 파일명은 `cpf-domain.yaml`을 사용한다. Framework definition 또는 명시 `--file`을 source of generation으로 사용하고 Generated Project에는 resolved metadata/lock/ownership을 영구 저장하지 않는다.

Secret/password를 YAML에 저장하지 않는다. Secret reference/env/secret-manager reference만 저장한다.

공식 fresh regression 입력은 `cpf-tools/generator/definitions/member/cpf-domain.yaml`과 `cpf-tools/generator/definitions/external/cpf-domain.yaml` 두 개다. Generated Project에는 lifecycle metadata를 영구 저장하지 않는다. 둘 다 동일 Schema를 사용하며 Developer GPT는 예시 YAML만 작성하고 완료 처리하지 않고 실제 Generator로 Online/Batch/Sample Transaction까지 생성해 Root에 유지한다. Schema는 특정 Domain명/SystemCode를 예약값으로 하드코딩하지 않는다.

필수 input/preflight validation:

- domain logical name / `cpf-` prefixed logical name 금지
- `projectRoot=cpf-<domain>` derivation/path collision
- systemCode/package/tablePrefix format·uniqueness
- `moduleName` 등 derivable 값은 별도 Consumer 의미가 없으면 Public 입력에서 제거하고 derive
- Generated DB role은 `CUSTOMER_BUSINESS_DB`; DB Vendor는 Domain YAML이 아니라 DB3 render 실행값으로 Oracle/PostgreSQL/MariaDB만 허용
- preset/feature/provider exactly-one 및 incompatible combination
- latest Catalog `visibility=public` composition 가능 여부; Internal leaf 직접선택 금지
- plaintext Secret/credential 0
- canonical definitions + explicit `--file` collision
- invalid input에서 partial generation 0

#### NXT3-GENERATOR-002 Preset

다음 Preset을 제공한다.

```text
minimal
standard-enterprise   # 기본값
full-enterprise
custom
```

`standard-enterprise` 회귀 시나리오는 **Online + Batch를 함께 검증**하되 Generated Project는 필요한 Surface만 만든다. Online과 Batch가 실제 별도 Deployable이면 각각 `online/`, `batch/`를 사용하고, 공유 코드가 실제 존재할 때만 `domain/`을 둔다.

```text
cpf-<domain>/
├─ online/
├─ batch/
└─ domain/   # actual shared consumer only
```

Online 기본 직접 dependency:

```text
cpf-starter-secure-api
cpf-starter-data-mybatis
+ 최신 Canonical Starter Catalog에서 visibility=public인 필요한 Integration Composition/Profile/Provider
```

Batch 기본 직접 dependency:

```text
cpf-starter-batch
cpf-starter-data-mybatis
+ 최신 Canonical Starter Catalog에서 visibility=public인 필요한 Integration Composition/Profile/Provider
```

`cpf-starter-integration-http`, `cpf-starter-integration-resilience`가 최신 Catalog에서 Internal-only leaf라면 Generated Domain이 직접 의존하지 않는다. 필요한 기능의 Public Composition이 없으면 Framework Public Surface Gap으로 먼저 보완한다.

`cpf-starter-data-mybatis`가 API와 Batch 예시에 각각 나타나는 것은 **두 Deployable이 각자 Persistence Provider를 소비하기 때문**이다. 동일 Module build 파일에 같은 dependency를 중복 선언하지 않는다.

Batch가 필요 없는 고객은 `modules.batch=false`로 제거할 수 있다.

Redis/Valkey/Kafka/IBM MQ/S3 같은 외부 Infra Provider는 기본으로 강제하지 않는다. feature 선택 시 정확히 하나의 Provider를 조립한다.

#### NXT3-GENERATOR-003 전체/개별 생성

동일 Generator Engine으로 다음을 모두 지원한다.

- 모든 정의 Domain 일괄 생성
- 특정 Domain 하나 생성
- 신규 Domain 추가 생성
- dry-run
- diff
- regenerate
- upgrade
- remove
- idempotent rerun
- conflict/user modification 보호
- retained verification fixture regenerate/update
- fresh arbitrary-domain transient generation

Root `cpf-member/`와 `cpf-external/` 재생성, 이후 arbitrary Domain 생성 모두 동일 Generator Engine/Template을 사용해야 한다. 별도 구현된 all/single/add/verification Generator가 서로 다른 Template 경로를 사용하면 결함이다.

### 12B. P0 — Annotation / Golden Path 실제 Runtime 소비

#### NXT3-ANNOTATION-001

Framework Annotation은 존재만 하면 완료가 아니다.

각 Annotation별로 다음을 확인한다.

```text
Annotation
→ scanner/interceptor/aspect/argument resolver/config consumer
→ runtime behavior
→ invalid usage fail-fast
→ Education Sample
→ Test
```

Meta-annotation, inheritance, precedence, multiple annotation collision이 적용되는 Annotation은 반드시 경계 Test를 둔다.

Consumer 없는 Annotation/Marker는 제거하거나 실제 Consumer를 구현한다.

Education/Generated Domain은 가능한 곳에서 CPF Annotation을 실제 사용한다.

### 12C. P0 — 모든 SQL/Query 3-Vendor Neutral

#### NXT3-QUERY-001

3 Vendor 정책은 DDL만이 아니다.

다음 모든 Query를 전수 대상에 포함한다.

- Common
- ADM
- BZA
- Batch
- Gateway
- Security
- Platform Operations
- Education
- Generated Domain
- Sample transaction
- Mapper/Repository
- Dynamic Query
- DDL/DML
- Operational Query

각 SQL은 정확히 하나의 Owner를 가져야 한다.

```text
portable canonical SQL
or
CPF data dialect/renderer
or
explicit vendor override + reason + testId
```

Application/Generated/Education Source에 다음과 같은 임의 Vendor branch를 금지한다.

```text
if oracle ...
else if postgres ...
else if mariadb ...
```

세 Vendor SQL 3벌을 Business Source에 복사하여 관리하는 것도 금지한다.

Generated Sample Transaction은 동일 Source로 Oracle/PostgreSQL/MariaDB에서 작동해야 한다.

### 12D. P0 — 한글 주석 영구 강제 규칙

#### NXT3-KOREAN-COMMENT-001

이번 Session에서 한 번 적용하고 끝나는 요구가 아니다. **앞으로 모든 Developer GPT Session/Handover/Continuity/Work Instruction에 자동 승계되는 영구 규칙이다.**

신규/수정 Source는 다음 주석을 반드시 제공한다.

##### Java/Kotlin

- Class/Interface/Enum/Annotation: 역할, 책임, 사용 위치
- public/protected 주요 Method: 입력, 반환, 예외, Side Effect
- 복잡한 private Method: 왜 필요한지/알고리즘 의도
- 동시성/Retry/Transaction/Lock/Cache/Recovery: 설계 이유
- Security/Masking/Audit: 보호 대상과 주의점
- Vendor/Dialect logic: 분리 이유
- Template Method/protected Hook: 하위 구현 계약

Getter/setter, 단순 대입, 자명한 한 줄까지 의미 없는 주석을 기계적으로 붙이지 않는다.

##### `@ConfigurationProperties`

각 Field/Record Component에 **한글로** 다음을 최대한 남긴다.

- 의미
- 기본값
- 단위(ms/sec/bytes/count 등)
- 허용 범위
- 운영 영향
- 보안 주의
- 재시작 필요 여부
- Provider별 차이가 있으면 그 차이

##### YAML / properties / env example

의미를 설명할 수 있는 설정 라인에는 **가능한 한 라인별 한글 주석**을 붙인다.

예:

```yaml
cpf:
  integration:
    http:
      # 외부 HTTP 연결 제한 시간(ms). 운영망 특성에 따라 조정한다.
      connect-timeout-ms: 3000
      # 응답을 기다리는 최대 시간(ms). 재시도 총시간보다 작게 설계한다.
      read-timeout-ms: 5000
```

Secret 실제 값은 예제/주석에 넣지 않는다.

##### 주석 Gate

신규/수정된 Public Source/Configuration에서 필요한 한글 설명이 빠지면 Quality Gate 실패로 처리한다.

모든 Handover에 아래 문구를 그대로 승계한다.

> 신규·수정 Source와 설정은 CPF 한글 주석 정책을 반드시 지키며, Configuration Property와 YAML/properties는 의미를 남길 수 있는 각 설정 항목에 상세 한글 설명을 유지한다. 이후 Session에서 이 기준을 약화하지 않는다.

### 12E. P0 — 오늘 요구사항 영구 Steering

다음 항목은 이번 Session 선택 사항이 아니라 CPF Architecture의 강제 정책이다.

- Folder/Module 이름은 개발자가 이름만 보고 목적을 이해할 수 있어야 한다.
- 동일 목적 Directory를 여러 Top-level Root로 분산하지 않는다.
- 불필요한 Directory/Wrapper/ceremonial layer를 만들지 않는다.
- Framework가 지원하는 기능은 Education/ADM/BZA/Generated Domain에서 Framework를 사용한다.
- Normal Developer는 Starter/Property로 쉽게 사용하고 Advanced Developer는 Public Programmatic API/SPI로 확장 가능해야 한다.
- Internal package 직접 접근이 필요한 확장 구조는 결함이다.
- Current 문서가 실제 Source보다 오래되어 제거된 구조를 다시 생성하게 만들면 P0 정본 결함이다.
- 완료된 Migration은 “다시 Migration” Requirement로 남기지 않고 “재도입 금지 Invariant”로 currentize한다.

# 29B. 31개 Detailed Contract와 신규 DX의 결합 규칙

위 31개에서 어떤 Framework Contract를 수정하든 다음 Fan-out을 자동 적용한다.

`Canonical Requirement → Owner Source → Public API/SPI → Config/Schema/Binding/Secret → AutoConfiguration → Starter Catalog → Typed Client/Operations → Generator → cpf-member/cpf-external → cpf-education → Online/Batch Sample → ADM/BZA/OpenAPI where applicable → Unit/Contract/Runtime/Fault Test → Evidence`

예:
- `NXT3-ANNOTATION-001` 변경은 Annotation class만 수정하고 끝낼 수 없다. Bean naming/conflict/runtime verifier/DTO semantics/EDU misuse test를 함께 닫는다.
- `NXT2-REDIS-001`은 Provider 존재만으로 끝나지 않고 Cache Common Operations, Redis/Valkey parity, multi-instance/outage/reconnect/EDU actual provider를 포함한다.
- `NXT3-BATCH-STRUCTURE-001`은 folder rename만으로 끝나지 않고 workload Base/Common Operations, scheduler/worker/control-plane/agent actual runtime과 old residue 0을 포함한다.
- `NXT2-GEN-001`은 generated file count가 아니라 Repository Golden Path, Transaction DX, selected Starter composition, DB3, member/external parity와 actual transaction을 포함한다.
- `NXT3-KOREAN-COMMENT-001`은 주석 수가 아니라 Public API의 정책/실패/복구/확장성 이해 가능성을 검증한다.

# 30. Fault / Recovery Acceptance

최소 Failure corpus:
- before side effect
- after side effect before ack/response
- DB commit response loss
- process kill
- remote timeout
- duplicate retry
- broker rebalance/outage
- cache outage
- lock lease expiry
- stale writer
- disk/log sink failure
- graceful drain mid-request/job
- multi-instance race

각 case:
expected state / actual state / audit / operator visibility / retry / reconcile / final convergence.

# 31. 한글 JavaDoc

모든 신규/변경 Public Base/Operations/Extension Point:
목적/정책/context/tx/failure/recovery/thread/async/starter/extension/native escape/forbidden usage 설명.

한 줄 번역 주석 금지.

# 32. 검증 실행 순서 — Gate 실패해도 전체 집계

1. SHA/WT/Root Freeze
2. stale path/catalog/secret/hygiene
3. root Gradle settings/help/config
4. ownership/dependency/public-internal
5. annotation/naming/base architecture
6. 64 Starter Function Matrix
7. 64 Starter Configuration/Binding/Invocation Matrix
8. targeted compile/test
9. generator lifecycle + member/external
10. DB3
11. Online Golden Flow
12. Batch Golden Flow
13. Cache/Messaging/Integration/Security actual provider
14. partial failure/process kill/UNKNOWN
15. ADM/BZA/OpenAPI/browser
16. publication/BOM
17. package/hash/manifest/diff check

한 Gate 실패로 중단 금지.
독립 검사를 계속하고 전체 실패를 root cause별로 일괄 보정.

# 33. 역할별 원장 수정 권한

Developer GPT는 개발GPT 소유 컬럼과 자기 Source/Evidence만 수정한다.
QA/Codex 판정 컬럼을 완료로 덮지 않는다.
Source 변경으로 재검수 필요를 기록하되 최종 QA 완료는 QA만 판정한다.

# 34. Evidence

각 Evidence:
- exact SHA
- command
- environment
- timestamp
- exit code
- actual result
- report/artifact hash
- sanitized 여부
- failure/retry/recovery detail

미실행 = 미검증.

# 35. 최종 Package

Root-relative Overlay:
- Source/SQL/API/Test/Config/Frontend/Script
- currentized Developer-owned docs/evidence
- CHANGE_MANIFEST
- TEST_AND_EVIDENCE
- OPEN_ISSUES
- REQUIREMENT_STATUS Developer-owned fields
- PACKAGE_MANIFEST
- SHA256
- Delete Manifest
- Handover/Continuity. 별도 독립 Review 요청서는 사용자가 해당 검수 역할을 명시적으로 운영할 때만 canonical 단일 파일로 생성

**currentizer/source rewrite tool 없음.**
적용 즉시 완성 Source Tree.

# 36. 완료 정의

다음 중 하나라도 남으면 전체 완료 금지:
- F-001~F-016 unresolved
- 40 hardening axis 미판정
- 64 starter matrix 누락
- Generator/EDU/member/external old Golden Path
- Root Gradle failure
- Batch/Tools/Deploy stale IA
- ADM 80 chain incomplete
- BZA semantic unverified
- DB3 unverified
- actual-provider runtime missing
- process-kill/UNKNOWN/recovery missing
- stale successor evidence
- unsafe apply packaging

Developer GPT는 위 전체를 같은 회차 Scope로 유지하고, 완료 후 중앙관리/QA의 successor SHA 독립검수를 받는다.
