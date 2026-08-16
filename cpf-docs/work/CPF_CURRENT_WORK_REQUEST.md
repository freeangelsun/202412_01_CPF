# CPF Current Work Request — Integrated Full Redevelopment / Commercial Closure

> Current static-source baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12` (`Apply CPF development updates and cleanup`; 현재 통합 개발검수 시작 기준, runtime PASS 아님)
> Work mode: **전체범위 단일 Full-Scope / 분할 전달 금지**
> Canonical Requirement Count: 기존 186개 유지 — 40대 목표와 DX는 기존 ID Acceptance로 병합
> Development/static closure: **완료** / Environment-dependent Runtime verification: **미검증**

## 0. 요청 목적

현재 통합 Source를 최신 기준으로 재검수하고, 남아 있는 Root Build·Architecture·Generator/Generated Domain·EDU·ADM·Evidence Gap을 같은 회차에서 닫는다.

현재 통합 closure는 아래 세 입력을 **한 번에 전부** 처리하고 개발·정적 검수까지 닫는다.

1. 본 Current Work Request에 통합된 F-001~F-016 — 최우선. 과거 세션 리뷰 파일을 현행 정본으로 참조하지 않는다.
2. `CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md`의 40대 전체.
3. `CPF_DX_GOLDEN_PATH_AND_COMMON_FUNCTION_REQUIREMENTS.md`와 Starter 64-module Function Matrix.

P0/P1/P2는 내부 수행순서일 뿐 작업물을 분할하지 않는다.

## 0A. 통합 1차 개발검수 추가 Acceptance

기존 F/CFG/CALLDX/Hardening/Starter Requirement를 약화하거나 새 축으로 중복 집계하지 않고 다음 횡단 관점으로 실제 Source를 다시 검수·보완한다.

- Golden Path: 권장 개발경로 1개 + 명시적 Native Escape, 중복 Public 경로/내부 Registry 직접 접근 제거
- Execution Lifecycle: Context/Tx/Result/Log/Trace/Retry/UNKNOWN/Recovery가 단일 실행 lineage로 연결
- Common Product Service: Code/Message/Parameter/Calendar/Template의 실제 업무·운영 완결성
- Operational Journey: transaction/execution 기준 전체 호출·실패·Retry·UNKNOWN·Reconcile·Audit 추적
- Generator-first DX: 생성 결과만으로 Online/Batch/DB/Domain/External/Config/Recovery 개발 가능
- Open Extension: CPF 정책 통제와 Spring/JDK/Provider native bridge/escape의 균형

추가로 Repository 문서는 역할별 active canonical 파일 하나를 유지한다. 과거 세션 Final/Revision/Checkpoint, stale SHA 완료보고, 깨진 파일명, 동일 목적 중복본은 current 정본에 필요한 내용을 승계한 뒤 Delete Manifest로 제거한다.


## 0B. 현재 Checkpoint Closure 상태

- Commercial Hardening #1~#40: **40/40 development_status=완료, static_source_gate=PASS**.
- 통합로그/거래추적: transactionId/executionId/segment/attempt/result/UNKNOWN/recovery lineage와 ADM 조회 경로를 Source/DB3/API/UI까지 currentize.
- ADM: 전체 관리·운영 Capability를 메뉴 난립 없이 업무 Workbench로 coverage하고 실제 Generated Client consumer를 검증.
- BZA: 고객업무 관리자 범위를 27 route/84 operation으로 검증.
- Gateway: Data/Control Plane, 보안, timeout/retry/idempotency, rate-limit/circuit/bulkhead, Context, audit/recovery, health/drain/canary, ADM Gateway 운영을 24개 계약으로 검증.
- Common Product Service: `CpfCodeService`, `CpfMessageService`, `CpfParameterService`, `CpfCalendarService`, `CpfTemplateService` 5개 Public Golden Path로 수렴.
- Naming/Architecture: main Java package↔directory 불일치 0, duplicate FQCN 0, stale retired package 0.
- Generator/Tools: Generator full/public contract, canonical profile/DB3/logical binding/Golden Path 및 active tooling 정적 Gate PASS.
- Document Governance: 비교 제품명/retired identity/버전·세션 경쟁 정본을 active surface에서 제거하고 역할별 canonical 문서를 유지.

실제 외부 Runtime이 필요한 항목은 구현 보류가 아니라 **환경 미제공 검증 상태**로만 `미검증` 처리하며, 미실행 결과를 PASS로 기록하지 않는다.

## 1. 시작 시 Mandatory Consistency Gate

- 최신 `origin/master` exact SHA를 다시 확인한다. 문서에 기록된 과거/검토 기준 SHA를 execution SHA로 고정하지 않는다.
- Working Tree 상태를 기록한다.
- Final Target → Current Work Request → Independent Review → 40 Cross-map → DX → Starter Matrix → 실제 Source 순으로 읽는다.
- latest master가 본 문서 기준보다 앞서 있으면 Source가 우선이며 변경사항을 다시 cross-map한다.
- 과거 완료/Evidence를 자동 승계하지 않는다.
- QA/Codex role-owned 상태를 Developer GPT가 덮어쓰지 않는다.

## 2. 현재 1차 개발검수 판정 (baseline `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`)

이 절은 과거 세션의 결함 선언을 누적하지 않고 **현재 Source와 재실행 Gate 기준**으로만 유지한다.
과거 F-001~F-016에서 현재도 필요한 Acceptance는 본 Current Request와 Canonical Requirement에 병합되어 있어야 한다. Repository는 Git History 보존을 전제로 하지 않으므로 삭제 전 현재 정본 승계를 확인한다.

### CLOSED — 이번 currentization에서 직접 보정·재검증
- **Root Build convention**: `cpf-tools/build/cpf-root-conventions.gradle` 실물화, Root static orchestration 경로 복구.
- **Final verifier command resolution**: Linux/Windows 실행파일 판정 오류를 수정하여 shell command를 파일경로로 오인하지 않게 함.
- **Service Call Runtime materialization**: Registry/Router/Engine/Public Caller·Executor/운영 Port를 실제 AutoConfiguration Bean으로 연결.
- **Boundary Result 4-state**: `SUCCESS/BUSINESS_FAILURE/TECHNICAL_FAILURE/UNKNOWN` typed helper와 UNKNOWN recovery id/action을 Public Outcome에 연결.
- **Generated Domain time policy**: Generated Online/Batch Sample의 `Instant.now()` 직접 호출을 `Clock` 주입으로 치환.
- **Generated profile/default**: `application-local/test/dev/stg/prod.yml`, local stable port, collision preflight, dev/stg/prod explicit binding fail-fast를 Generator와 MBR/EXS 생성물에 동기화.
- **Capability binding cardinality**: capability별 `SINGLE_DEFAULT_REQUIRED/NAMED_MULTI_OPTIONAL_DEFAULT/EXPLICIT_ONLY/INTERNAL_NO_PUBLIC_BINDING` Runtime 정책 구현.
- **Cross-owner Internal dependency**: Integration Service Call의 Observability segment/file-log/server-identity 직접 internal import를 Public Port/Facade로 교체.
- **Current document hygiene**: 세션/날짜/Checkpoint/중복 Current 문서를 active tree에서 제거 대상으로 확정하고 Canonical 문서로 내용 승계.

### CLOSED — 추가 횡단검수에서 Source/Consumer까지 보정

#### Typed Domain Client / Local-Remote Binding
- Core에 topology-independent `CpfDomainClient`, `CpfResult` 4-state/Recovery 계약을 두고 Integration에 `CpfDomainClientRouter`, Local Operation Registry, Binding Resolver를 구현했다.
- HTTP Adapter는 기존 Service Call/Registry/Routing 자산을 재사용하며 별도 평행 Remote Framework를 만들지 않는다.
- Generator는 `domainDependencies`를 domain-specific typed client/adapter/actual consumer로 materialize하고 `cpf-member`/`cpf-external` Online 회귀 생성물에 연결한다. Batch는 Generated Domain 산출물이 아니라 초기 프로젝트 구성에서 `cpf-starter-batch`로 별도 선택한다.
- Local/Remote 동일 typed contract와 LOCAL missing fail-closed 정적/순수 Java smoke를 확인했다. Java25/Spring full Runtime parity는 아래 Runtime 미검증으로 유지한다.

#### Generator logical `domainDependencies` / `externalClients`
- `cpf-domain.schema.json`과 canonical Python Engine에 logical dependency/client 선언을 추가하고 실제 URL/IP/credential은 definition에서 금지한다.
- Generated profile은 local/test/dev/stg/prod를 분리하고 local stable port/collision preflight, dev/stg/prod explicit binding fail-fast를 적용한다.
- `cpf-member`/`cpf-external`은 canonical Engine으로 재생성하여 `verify_generated`/preset/genericity Gate를 통과했다.

#### Framework Clock propagation
- Generated MBR/EXS Sample과 Common Product Service production source의 직접 system time을 injected `Clock`/Business Time 정책으로 수렴했다.
- `OffsetDateTime.now(clock)`처럼 주입 Clock을 명시 사용하는 코드는 허용하며 무주입 `now()`/`System.currentTimeMillis()`은 금지한다.

#### Test architecture boundary
- Integration main의 Observability internal import는 Public Port로 교체했다.
- Resilience test harness의 cross-owner internal masking/sampling import도 제거하고 해당 회귀는 Observability Owner-local harness로 이동했다.

#### Stateless Generator lifecycle / Tool currentization
- Generated Project 내부 영구 lifecycle manifest/ownership/lock을 정본으로 사용하지 않는다. Framework `cpf-domain.yaml` + canonical Python Engine이 Source of Truth이며 transient 검증 상태만 `build/domain-generator/verification/**`에 둔다.
- lifecycle verifier는 fresh/idempotent/upgrade/user-owned protection/remove/restore를 실제 임시 생성물로 검증한다. 과거 manifest 기반 Tool/Guide/Consumer는 current stable Gate로 이관 후 Delete Manifest 대상으로 정리한다.

### Runtime 미검증
- 이 검수 환경은 Java 21이며 CPF 기준 Java 25 Runtime이 아니다. Root Gradle full build/test/publication은 PASS로 기록하지 않는다.
- Oracle/PostgreSQL/MariaDB, Redis/Valkey, multi-instance/process-kill, Browser E2E와 외부기관 fault runtime은 별도 실제 환경 Evidence가 필요하다.

## 3. 보존해야 하는 구현

다음은 없애지 말고 확장한다.

- exact 3-tier 검증 `CpfThreeTierStructurePolicy`.
- Controller/Service/Repository annotation runtime verifier/post-processor.
- Repository CRUD/Search/Bulk/Lock Port.
- provider-neutral Cache Aside/Single-flight/TTL 기반.
- programmatic TransactionTemplate interceptor 기반.
- Context/Masking 연계 Logging Aspect.
- Batch control-plane operations/UNKNOWN/recovery contract.
- DB Canonical + DB3 renderer/verification 기반.
- BZA 27 route exact alignment.
- Education physical root와 대규모 adopter scenario corpus.

## 4. Architecture 변경 — Repository one concept

최상위 정본 §16.3을 따른다.
JDBC/MyBatis class mode와 JPA/interface mode를 모두 Repository로 수렴한다.
blind rename 금지. 실제 Provider consumer를 먼저 전환한다.

## 5. Common Function / Starter Composition

`CPF_STARTER_CAPABILITY_COMMON_FUNCTION_MATRIX.md`의 64 modules를 하나도 누락하지 않는다.
Starter를 여러 개 선택해도 Business class inheritance는 바뀌지 않는다.
Public Operations와 ergonomic Base helper를 조합한다.
God Base/Service Locator/Internal leaf 직접 참조를 금지한다.

## 5A. 공통 Function Bean 등록/Lifecycle 보강

여기서 공통 Function은 위에서 합의한 Controller/Service/Repository/Batch와 Starter별 Function 전체다.
각 Function family는 `Public Operations Bean → AutoConfiguration → Base Helper → Actual Consumer`까지 연결한다.

추가 Acceptance:
- selected capability bean exists / unselected capability bean-listener-thread-endpoint 0.
- provider conflict/duplicate naming fail-fast.
- custom override/backoff.
- stateless singleton.
- bounded async executor/context propagation/backpressure/drain.
- messaging listener/integration client/realtime/batch job-step registration/lifecycle.


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



## 5B. Developer Custom Bean 자동주입 / Method Invocation

기존 type-based Constructor Injection 요구에 더해, 고객 Business Source에서 개발자가 만든 Bean을 자동 주입하는 `@CpfInject` 또는 동등 Public DX를 구현한다.

Acceptance:
- Customer CPF Bean/일반 Spring Bean/Starter Public Operations Bean 모두 타입 기반 자동 주입.
- missing/duplicate candidate fail-fast.
- qualifier/explicit bean selection.
- `ApplicationContext.getBean()` 직접 사용 없이 method 호출.
- injected Proxy를 통한 Transaction/Retry/Logging/Security 적용.
- self-invocation/circular dependency negative test.
- Generator/member/external/education actual sample.

Golden Path:
```java
@CpfInject
private MemberPolicyService memberPolicyService;

return call(() -> memberPolicyService.canJoin(memberId));
```


## 6. Generator / Education

- Generator에서 DAO/legacy transaction sample을 currentize.
- member/external 동일 generator 결과로 재생성·parity 검증.
- EDU는 new common functions를 실제 사용.
- generic deterministic harness는 Unit/Contract로 재분류.
- Integration/Runtime/Recovery는 actual provider consumer를 사용.

## 7. Batch

새 active IA를 기준으로 stale `host-agent/runtime-common` content를 실제 successor와 대조한다.
대체가 완료된 파일만 Delete Manifest 대상으로 한다.
Batch workload common operations와 control-plane operations를 역할 분리한다.

## 8. Tools / Deploy

현재 old physical tree를 Target IA로 실제 완성한다.
Source move는 Developer GPT 격리 작업공간에서 완성하고 최종 Overlay에는 완성 tree를 직접 포함한다.
사용자 Apply에서 move/rename/rewrite하지 않는다.

## 9. ADM / BZA

ADM은 exact missing 16을 우선 보완하되 80행 전체 chain을 재검수한다.
BZA는 27 route는 일치하므로 재작성하지 말고 7 target IA semantics/backend enforcement를 검증한다.

## 10. DB3

Physical DB3 foundation을 재사용한다.
latest exact SHA에서 install/seed/upgrade/rollback-or-forward-recovery/runtime query/checksum/drift를 재검증한다.
MySQL/MSSQL/H2는 공식 Evidence 대상에 넣지 않는다.

## 11. Safe Overlay — P0

사용자 Repository에서 currentizer/migration/source conversion/source move/rename/package rewrite/dynamic generation 금지.

허용 Apply:
`SHA 확인 → Overlay 복사 → 사용자 승인 Delete Manifest 삭제 → 검증`

적용 중단 시 partial conversion 상태가 생길 수 있으면 Package FAIL.
currentizer는 내부 격리 작업공간에서만 가능하고 최종 ZIP/명령에 포함 금지.
Overlay 적용 후 Source를 새로 만들어야 정상 상태가 되면 FAIL.

## 6. Canonical 31 Requirement Inventory

### Core / Error / Common / Web

- `NXT2-CORE-001` — Core 최종 Freeze와 stale tree 제거
- `NXT2-ERROR-001` — Error/Exception + DB Catalog 완성
- `NXT2-COMMON-001` — Common을 Starter Capability로 전환
- `NXT2-WEB-001` — Web Capability + 3단 Base Class Golden Path

### Starter

- `NXT2-STARTER-001` — Starter Physical hierarchy
- `NXT2-STARTER-002` — 개발자 공개 Artifact one-shot rename
- `NXT2-STARTER-003` — Starter Composition

### DB / Vendor

- `NXT2-DB-001` — CPF Platform DB Consolidation
- `NXT2-DB-002` — Customer Business DB
- `NXT2-DBVENDOR-001` — Canonical One Source / 3 Vendor Generated Pack

### Generated Domain / Education / Redis

- `NXT2-GEN-001` — Generic Root Generated Domains: member(MBR)+external(EXS) 동시 실생성·실거래·회귀검증
- `NXT3-EDU-001` — cpf-education 전환 완결 / old reference Active Surface 0 / actual Education consumer 품질
- `NXT3-EDU-002` — Education Package Information Architecture
- `NXT3-EDU-003` — Education 예제 품질 / CPF Golden Path
- `NXT2-REDIS-001` — Redis/Valkey 선구현 Closure

### ADM / BZA

- `NXT3-ADM-BZA-001` — ADM/BZA CPF Framework 사용 경계
- `NXT3-ADM-BZA-002` — Common Product Service 실시간 운영
- `NXT3-ADM-BZA-003` — Frontend/OpenAPI 실제 Consumer

### Config / Repository IA / Hygiene

- `NXT3-CONFIG-001` — Hardcoding 전수검사
- `NXT3-CONFIG-002` — Property-only Framework 금지
- `NXT3-CONFIG-003` — Build/Compatibility
- `NXT3-TOOLS-001` — cpf-tools 최종 Canonical Layout
- `NXT3-DEPLOY-001` — deploy 최종 Canonical Layout
- `NXT3-BATCH-STRUCTURE-001` — Batch 하위 Module exact rename
- `NXT3-HYGIENE-001` — 실제 Garbage 제거

### Generator / Annotation / Query / Comment

- `NXT3-GENERATOR-001` — Generator Canonical 입력 파일
- `NXT3-GENERATOR-002` — Generator Preset
- `NXT3-GENERATOR-003` — Generator 전체/개별 생성
- `NXT3-ANNOTATION-001` — Annotation / Golden Path 실제 Runtime 소비
- `NXT3-QUERY-001` — 모든 SQL/Query 3-Vendor Neutral
- `NXT3-KOREAN-COMMENT-001` — 한글 주석 영구 강제 규칙



## 11A. Configuration / Invocation 전체 재검수 — CFG-001~CFG-016

Configuration과 실제 사용 흐름은 기존 F-001~F-016/40대/64 Starter Scope에 **통합 병합**하며 별도 Phase로 미루지 않는다.

상세 근거:
- `cpf-docs/work/current/CPF_CONFIGURATION_AND_INVOCATION_REQUIREMENTS.md`
- `cpf-docs/work/current/CPF_CONFIGURATION_SETUP_AND_INVOCATION_MATRIX.md`
- `cpf-docs/work/current/CPF_WHOLE_FRAMEWORK_CONFIGURATION_DEFAULTS_KO_COMMENTS_OVERRIDE_REQUIREMENTS.md`

필수 closure:

- `CFG-001` — `CpfConfigPolicy/CpfConfigCatalog`와 실제 64 Starter Configuration Properties coverage/metadata 연결 완결.
- `CFG-002` — Config 계약의 `com.cpf.core` vs stale `com.cpf.foundation` import/package drift 정리 및 compile gate.
- `CFG-003` — MBR→EXS/ACC 등 CPF 내부 Domain Call의 논리 Domain/SystemCode + Local/Remote topology binding 완결.
- `CFG-004` — Service Call/Registry/Routing의 topology-independent Public API/SPI Owner 정리; HTTP internal 구현에 계약을 가두지 않음.
- `CFG-005` — CPF 내부 Domain Registry와 External Integration Client/Channel Registry/namespace를 분리.
- `CFG-006` — Capability Binding cardinality를 per-capability로 정의; 모든 capability exactly-one default 강제 제거.
- `CFG-007` — Generator에 logical domainDependencies/externalClients/binding skeleton 추가; 물리 IP/Secret 저장 금지.
- `CFG-008` — CPF semantic config와 Spring/Kafka/Redis/OIDC/AWS/DataSource 등 Native Provider config의 bridge/preflight/doc 완결.
- `CFG-009` — Config schema/type/range/enum/required/default/unknown-prefix validation 통일.
- `CFG-010` — environment/profile/secret/mutability/atomic refresh/restart-required/runtime override/rollback/drift lifecycle 완결.
- `CFG-011` — ADM `ops-config`, `ops-topology`, `external-institutions`, `ops-drift`를 실제 Backend/Generated Client/Permission/Audit와 연결.
- `CFG-012` — 개발자 Typed Domain Client/External Client/Operations + Generator/member/external/education 실제 사용 예제.
- `CFG-013` — TCP/SFTP/S3/Notification/HTTP 등 다중 외부 대상이 자연스러운 capability의 single-binding 가정 전수 Audit.
- `CFG-014` — preflight/doctor/diagnostics에서 active capability/binding/effective config/missing/unknown/restart-required를 확인 가능하게 함.
- `CFG-015` — config key/schema deprecation/alias/mixed-version/upgrade/rollback compatibility.
- `CFG-016` — 64/64 Config coverage, Local/Remote parity, multi-binding, refresh/drift/negative/fault exact-SHA Evidence.

완료는 Config 파일/Properties class 존재가 아니라:

`Definition → Binding → Config → Validation → Secret → AutoConfiguration → Bean/Registry/Client → Actual Consumer → Health/ADM → Recovery → Generator/EDU → Test/Evidence`

전체가 닫힌 경우만 인정한다.


### 11A.1 Configuration Usability 추가 결함 — CFG-017~CFG-024

전체 CPF 설정 활용성 재검수에서 다음을 기존 Requirement/40대에 병합한다.

- `CFG-017` — local/test loopback과 dev/stg/prod fail-fast 기본정책 불명확.
- `CFG-018` — Generated Domain/Gateway/Runtime local port stable allocation/collision 정책 부족.
- `CFG-019` — 주요 option의 한글 역할/기본값/허용값/단위/보안/운영주의 설명 불균일.
- `CFG-020` — `application-local/test/dev/stg/prod.yml` 환경별 skeleton/commented option 계약 부족.
- `CFG-021` — Source Typed Customizer/Builder/Per-call Bounded Override와 Deployment Lock precedence 부족.
- `CFG-022` — Gateway 설정값 대비 한글 필드설명/profile/override/운영 setup closure 부족.
- `CFG-023` — Integration/Gateway를 넘어 CPF 전체 config/env/hardcoded value Inventory Gate 부족.
- `CFG-024` — YAML comment/default/IDE metadata/Java code default drift 검증 부족.

새 상위 Canonical Requirement를 추가하지 않고 기존 `CORE-CONFIG/OPS-CONFIG/STARTER-DX/DEVEX/GATEWAY/TEST-EVIDENCE` Acceptance에 병합한다.


## 11B. Developer-First Transaction / Call / Result / Logging 재개발 — CALLDX-001~CALLDX-018

이번 재개발은 단순 문서 보완이 아니다. 최신 Source와 정본을 대조한 결과 호출/응답/로그 DX의 구조적 Gap을 확인했다.
아래 Finding은 기존 Canonical Requirement/Commercial Hardening 40에 병합하고 같은 Full-Scope에서 구현한다.

| ID | 우선 | Finding | 완료 조건 |
|---|---|---|---|
| CALLDX-001 | P0 | Public `CpfServiceClient`가 현재 존재하지 않는 `com.cpf.core.api.base.CpfRequest/CpfResponse`를 참조 | current Core Public contract와 compile/runtime 정합 |
| CALLDX-002 | P0 | 내부 `ServiceCallResult<T>`가 String `SUCCESS/FAILED/UNKNOWN`이며 business vs technical failure 구분이 없음 | typed `CpfCallOutcome`/Public Result mapping |
| CALLDX-003 | P0 | 일반 Call Result와 XA/TCC용 `CpfTransactionOutcome` 역할 혼용 위험 | Call Outcome과 Transaction Recovery Status 분리 |
| CALLDX-004 | P0 | `CpfErrorResponse`가 core에서 platform-operations internal `TransactionContext`를 참조 | Core ownership/topology independence 복원 |
| CALLDX-005 | P1 | `CpfErrorResponse`가 `LocalDateTime.now()` 직접 사용 | CPF Clock/time policy currentization |
| CALLDX-006 | P0 | `CpfLoggingAspect`가 `com.cpf.foundation.annotation.*`를 참조 | Canonical annotation owner와 Repository-wide currentization |
| CALLDX-007 | P0 | Common Function 전체 명령/반환/Tx/Log/Failure 사용매뉴얼 부재 | source-verified command catalog + manual |
| CALLDX-008 | P0 | `callRemote`가 Domain Call Golden Path와 혼동 가능 | topology-independent Typed Domain Client + `callDomain`; `callRemote` advanced-only |
| CALLDX-009 | P0 | 단건/List/Page/Cursor/Map/scalar/boolean/count/void/async/stream Result 계약 미완결 | 표준 type-safe result family |
| CALLDX-010 | P0 | Generic parameterized response를 raw `Class<T>`로 처리할 위험 | Generated Typed Client + TypeRef |
| CALLDX-011 | P0 | Local/Remote Domain Transaction semantics parity와 active local Tx 내 remote call 정책 미완결 | topology parity + transaction boundary test |
| CALLDX-012 | P0 | MSA business/technical/UNKNOWN 결과처리와 exception/result 경계가 개발자에게 불명확 | 4-state outcome + fail-fast boundary |
| CALLDX-013 | P0 | 8.1 표준 로그 필드가 current Runtime log에 전부 연결되지 않음 | E2E structured timeline |
| CALLDX-014 | P0 | UNKNOWN 결과를 개발자가 쉽게 Reconcile/Manual Review로 연결하는 공통 Result/Operations 부족 | RecoveryInfo + reconcile API |
| CALLDX-015 | P0 | Generator/EDU가 Result/Domain/External/UNKNOWN/자료형별 예제를 생성·실행하지 않음 | actual generated consumers |
| CALLDX-016 | P1 | Messaging/File/Object/Notification/Batch operation의 Ack/Receipt 결과 의미가 Capability별로 분산 | standard receipt/result semantics |
| CALLDX-017 | P0 | Annotation/Role별 허용 return contract Architecture Gate 부족 | Service/Repository/Boundary return gate |
| CALLDX-018 | P0 | Manual/Requirement/Public API/Config/Log Field drift를 release gate가 검증하지 않음 | exact-SHA documentation/API parity gate |

신규 상위 Requirement count를 늘리지 않고 `ARCH-MSA`, `CPF-CALL`, `DEVEX-*`, `CORE-ERR`, `TX-*`,
`OPS-OBS`, `STARTER-DX`, `SAMPLE-*`, `TEST-*`, Commercial Hardening #1/#2/#6/#9/#18/#19/#23/#27/#29/#30/#39/#40에 병합한다.

## 12. 완료

최신 execution SHA에서:
- Current review defects closure
- 40 hardening full cross-check
- DX/Starter Function completeness
- Generator/member/external/EDU actual usage
- Root Build
- DB3
- ADM/BZA
- Batch/Tools/Deploy
- failure/UNKNOWN/recovery
- Test/Evidence
- safe overlay
를 모두 만족하기 전 전체 완료 금지.

## 11C. CFG-018 / CALLDX-012·014 환경·Header·전문 강화 현행화

기존 Requirement를 축소하지 않고 다음 Acceptance를 추가한다.

- **CFG-018 강화**: Local 기본은 `local-integrated` 1 JVM/1 HTTP Port/Gateway OFF, Generated Domain descriptor 자동편입. 분산 Local에서만 stable port를 사용하고 충돌은 fail-fast한다.
- **CFG-018/020 강화**: dev/stg/prod는 `single-node/split-online/split-batch/full-distributed/custom` 선택형 topology를 동일 inventory contract로 지원한다. Batch 분리는 선택이다.
- **CFG-020 강화**: 배포 JAR은 distribution 단계에서 `artifacts/ + deployment-manifest.json`으로 집계하며 Windows/Linux/Jenkins/수동 설치가 같은 manifest를 사용한다.
- **CALLDX-012/014 강화**: `CpfResult.fold`로 SUCCESS/BUSINESS_FAILURE/TECHNICAL_FAILURE/UNKNOWN을 명시 분기하고 UNKNOWN은 RecoveryInfo 기반 reconcile 경로를 강제한다.
- **CALLDX-018 강화**: 기존 Header wire name은 호환성을 위해 유지하고 업무 Source는 literal 대신 `CpfHeaders`의 짧은 builder/get/require API를 사용한다. 내부 서비스 hop의 Tx/Exec/Caller/Target은 필수이며 누락 또는 인증된 Caller 불일치는 fail-fast한다. traceparent는 W3C 선택 계약으로 유지하되 존재하면 엄격 검증한다.
- **Integration DX 강화**: Fixed-Length Starter는 실제 기본 Parser/Writer/Operations Bean을 제공하고 ADM/log에는 Layout ID/version 기반 masked structured view만 노출한다.
- **Generator lifecycle 강화**: 생성 시 Local/Deploy integration point를 자동 연결하고 remove/purge 시 Generator 소유 integration point까지 stale entry 없이 제거한다. 사용자 소유 Source/DB는 자동삭제하지 않는다.


## FINAL-COMMERCIAL-QA-20-50

- 기준 입력 SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`
- 실행 우선순위는 `current/CPF_EXECUTION_SEQUENCE.csv`, `OPEN_ISSUES.md`, `QA_FINDING_REVALIDATION.csv`를 사용한다. 별도 20/50 상태 Matrix를 만들지 않는다.
- 완료 기준은 파일/Interface/Route 존재가 아니라 실제 Consumer, UI 역할, DB3, 실패/UNKNOWN/복구, 승인/감사, Generator/배포, Test/Evidence 연결이다.
- 개발 GPT는 정적으로 확인 가능한 결함을 Codex 전달 전 보정한다.
- Java25/DB3 Live/Process Kill/Browser/분산환경처럼 현재 환경에서 실행하지 못한 항목은 `미검증`이며 Codex가 `current/CODEX_FINAL_RUNTIME_VALIDATION_INSTRUCTION.md` 순서대로 실행한다.
- Runtime 미실행을 PASS로 기록하지 않는다.

## 13. History-less Repository Currentization

- Push 후 Git history를 current baseline 하나로 재작성하더라도 Requirement/Decision/Codex continuity/Evidence가 유실되지 않게 current canonical 문서에 먼저 병합한다.
- 세션/날짜/REV/checkpoint/campaign 전용 문서·Evidence·Tool·Test는 successor가 확인되면 제거한다.
- Active Source/Workflow/Verifier가 삭제 경로를 참조하면 먼저 current successor로 전환한 뒤 삭제한다.
- 보호경로와 `cpf-tools/build/**` 제품 Source는 cleanup에서 보존한다.
- Delete Manifest/Garbage Decision은 현재 baseline과 이전 적용 Overlay까지 고려해 1:1로 생성하고, 파일 삭제 후 비보호 빈 폴더도 제거한다.
- History rewrite 이후 새 root commit SHA에서 Runtime/Codex/QA Evidence를 이어간다.
