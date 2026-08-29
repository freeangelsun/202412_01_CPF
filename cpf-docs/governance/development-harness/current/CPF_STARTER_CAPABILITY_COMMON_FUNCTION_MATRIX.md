# CPF Starter Capability / Common Function Completeness Matrix

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실제 실행 시 최신 Git HEAD와 Runtime Evidence를 사용한다.
> catalog schemaVersion: `2.0`  
> Catalog baseline policy: `GIT_HEAD_RUNTIME / RUNTIME_GIT_HEAD` — frozen 과거 SHA를 Current 기준으로 사용하지 않는다.
> module count: **64**  
> public modules: **24**  
> internal modules: **40**

## 사용 원칙

- 이 표의 64 Module은 하나도 생략하지 않는다.
- Public Starter/Provider는 개발자용 Operations/Facade와 activation/conflict/consumer를 제공한다.
- Internal Module은 Public Base helper에 직접 노출되지 않고 자신이 구현하는 Public Capability를 명시한다.
- Starter 추가/제거로 Business class의 상속 Class를 바꾸지 않는다.
- `cpf-starters/common`은 Product Common Service이며 기술 Operations의 잡동사니 Owner가 아니다.
- 실제 method signature는 기존 API/Provider 구조를 검토해 결정하며 아래는 **필수 의미론 최소선**이다.

| # | Artifact | Visibility / Kind | Owner path | Capability | 개발자 Common Function 목표 | Runtime/Recovery/경계 |
|---:|---|---|---|---|---|---|
| 1 | `cpf-starter` | public / starter-base | `cpf-starters/base` | Base composition | context/validation/call/log/error 최소 DX; 선택 capability 직접 소유 금지 | 선택 Public Operations 조합; 내부 leaf 직접 노출 금지 |
| 2 | `cpf-starter-cache-caffeine` | public / starter-provider | `cpf-starters/data/cache/caffeine` | Cache | get/put/evict/getOrLoad, TTL, invalidation, stale, single-flight | provider outage/reconnect/version/multi-instance; cache provider slot 충돌 fail-fast |
| 3 | `cpf-cache-spring-data-redis` | internal / internal-starter | `cpf-starters/data/cache/spring-data-redis` | Cache | get/put/evict/getOrLoad, TTL, invalidation, stale, single-flight | provider outage/reconnect/version/multi-instance; cache provider slot 충돌 fail-fast |
| 4 | `cpf-starter-cache-redis` | public / starter-provider | `cpf-starters/data/cache/redis` | Cache | get/put/evict/getOrLoad, TTL, invalidation, stale, single-flight | provider outage/reconnect/version/multi-instance; cache provider slot 충돌 fail-fast |
| 5 | `cpf-starter-cache-valkey` | public / starter-provider | `cpf-starters/data/cache/valkey` | Cache | get/put/evict/getOrLoad, TTL, invalidation, stale, single-flight | provider outage/reconnect/version/multi-instance; cache provider slot 충돌 fail-fast |
| 6 | `cpf-starter-data-jdbc` | public / starter-provider | `cpf-starters/data/persistence/jdbc` | Persistence/Repository | CRUD/search/page/cursor/bulk/lock/timeout/error/id | JDBC/MyBatis class Repository, JPA interface Repository; DB3 semantics 동일 |
| 7 | `cpf-starter-data-mybatis` | public / starter-provider | `cpf-starters/data/persistence/mybatis` | Persistence/Repository | CRUD/search/page/cursor/bulk/lock/timeout/error/id | JDBC/MyBatis class Repository, JPA interface Repository; DB3 semantics 동일 |
| 8 | `cpf-starter-data-jpa` | public / starter-provider | `cpf-starters/data/persistence/jpa` | Persistence/Repository | CRUD/search/page/cursor/bulk/lock/timeout/error/id | JDBC/MyBatis class Repository, JPA interface Repository; DB3 semantics 동일 |
| 9 | `cpf-starter-file-archive` | internal / internal-starter | `cpf-starters/file/archive` | File/Object Storage | read/write/upload/download/stream/checksum | partial/orphan/retry/quarantine/retention/security |
| 10 | `cpf-starter-file-attachment` | internal / internal-starter | `cpf-starters/file/attachment` | File/Object Storage | read/write/upload/download/stream/checksum | partial/orphan/retry/quarantine/retention/security |
| 11 | `cpf-starter-file-sftp` | internal / internal-starter | `cpf-starters/file/sftp` | File/Object Storage | read/write/upload/download/stream/checksum | partial/orphan/retry/quarantine/retention/security |
| 12 | `cpf-starter-file-tabular-poi` | internal / internal-starter | `cpf-starters/file/tabular/poi` | File/Object Storage | read/write/upload/download/stream/checksum | partial/orphan/retry/quarantine/retention/security |
| 13 | `cpf-starter-integration-ai` | internal / internal-starter | `cpf-starters/integration/ai` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 14 | `cpf-starter-integration-fixed-length` | internal / internal-starter | `cpf-starters/integration/fixed-length` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 15 | `cpf-starter-integration-http` | internal / internal-starter | `cpf-starters/integration/http` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 16 | `cpf-starter-integration-iso8583` | internal / internal-starter | `cpf-starters/integration/iso8583` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 17 | `cpf-starter-integration-resilience` | internal / internal-starter | `cpf-starters/integration/resilience` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 18 | `cpf-starter-integration-soap` | internal / internal-starter | `cpf-starters/integration/soap` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 19 | `cpf-starter-integration-tcp` | internal / internal-starter | `cpf-starters/integration/tcp` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 20 | `cpf-starter-integration-webhook` | internal / internal-starter | `cpf-starters/integration/webhook` | Integration | call/callAsync/timeout/retry/error/correlation | circuit-breaker/bulkhead/rate/auth/remote UNKNOWN/reconcile |
| 21 | `cpf-starter-messaging-ibm-mq` | public / starter-provider | `cpf-starters/messaging/ibm-mq` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 22 | `cpf-starter-messaging-jms` | public / starter-provider | `cpf-starters/messaging/jms` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 23 | `cpf-starter-messaging-kafka` | public / starter-provider | `cpf-starters/messaging/kafka` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 24 | `cpf-starter-messaging-rabbitmq` | public / starter-provider | `cpf-starters/messaging/rabbitmq` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 25 | `cpf-starter-messaging-reliability-jdbc` | internal / internal-starter | `cpf-starters/messaging/reliability/jdbc` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 26 | `cpf-starter-notification-dispatch` | internal / internal-starter | `cpf-starters/notification/dispatch` | Notification | send/template/channel/result | retry/duplicate/provider outage/audit |
| 27 | `cpf-starter-notification-email` | internal / internal-starter | `cpf-starters/notification/email` | Notification | send/template/channel/result | retry/duplicate/provider outage/audit |
| 28 | `cpf-starter-notification-sms` | internal / internal-starter | `cpf-starters/notification/sms` | Notification | send/template/channel/result | retry/duplicate/provider outage/audit |
| 29 | `cpf-starter-platform-operations-channel-registry-jdbc` | internal / internal-starter | `cpf-starters/platform-operations/channel-registry/jdbc` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 30 | `cpf-starter-platform-operations-feature-flag-openfeature` | internal / internal-starter | `cpf-starters/platform-operations/feature-flag/openfeature` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 31 | `cpf-starter-platform-operations-observability-otlp` | internal / internal-starter | `cpf-starters/platform-operations/observability/otlp` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 32 | `cpf-starter-platform-operations-runtime-control` | internal / internal-starter | `cpf-starters/platform-operations/runtime-control` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 33 | `cpf-starter-batch` | public / starter-profile | `cpf-starters/profiles/batch-service` | Batch Profile | job/step/context/checkpoint/retry/progress workload facade | exactly-one profile; batch runtime composition/control-plane separation |
| 34 | `cpf-starter-bff` | public / starter-profile | `cpf-starters/profiles/browser-bff` | Application Profile | selected capability composition | exactly-one profile; zero-footprint unselected |
| 35 | `cpf-starter-event` | public / starter-profile | `cpf-starters/profiles/event-service` | Application Profile | selected capability composition | exactly-one profile; zero-footprint unselected |
| 36 | `cpf-starter-secure-api` | public / starter-profile | `cpf-starters/profiles/secure-api` | Application Profile | selected capability composition | exactly-one profile; zero-footprint unselected |
| 37 | `cpf-starter-web-api` | public / starter-profile | `cpf-starters/profiles/web-api` | Application Profile | selected capability composition | exactly-one profile; zero-footprint unselected |
| 38 | `cpf-starter-security-audit-jdbc` | internal / internal-starter | `cpf-starters/security/audit/jdbc` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 39 | `cpf-starter-oidc` | public / starter-provider | `cpf-starters/security/oidc` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 40 | `cpf-starter-security-resource-server` | internal / internal-starter | `cpf-starters/security/resource-server` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 41 | `cpf-starter-security-secret` | internal / internal-starter | `cpf-starters/security/secret` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 42 | `cpf-starter-security-service-identity` | internal / internal-starter | `cpf-starters/security/service-identity` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 43 | `cpf-starter-session-jdbc` | public / starter-provider | `cpf-starters/security/session/jdbc` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 44 | `cpf-starter-lock-valkey` | public / starter-provider | `cpf-starters/data/lock/valkey` | Distributed Lock | lock/tryLock/renew/release/fencing/lease | stale-writer/process-kill/network partition recovery |
| 45 | `cpf-starter-messaging-schema` | internal / internal-starter | `cpf-starters/messaging/schema` | Messaging | publish/send/consume/correlation/result | retry/DLQ/duplicate/order/outbox/inbox/schema/UNKNOWN |
| 46 | `cpf-starter-graphql` | public / starter-provider | `cpf-starters/integration/graphql` | GraphQL | query/mutation/paging/context/security | complexity/N+1/error/native spring graphql |
| 47 | `cpf-starter-realtime` | public / starter-provider | `cpf-starters/integration/realtime` | Realtime | SSE subscribe/progress/reconnect | backpressure/multi-instance/drain/fallback |
| 48 | `cpf-starter-object-storage-s3` | public / starter-provider | `cpf-starters/file/object-storage/s3` | File/Object Storage | read/write/upload/download/stream/checksum | partial/orphan/retry/quarantine/retention/security |
| 49 | `cpf-starter-session-valkey` | public / starter-provider | `cpf-starters/security/session/valkey` | Security | currentPrincipal/authorize/hasPermission/masking/audit | identity/session/secret/cert/SoD/failure/expiry/revoke |
| 50 | `cpf-starter-platform-operations-health` | internal / internal-starter | `cpf-starters/platform-operations/health` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 51 | `cpf-starter-platform-operations-runtime-health-jdbc` | internal / internal-starter | `cpf-starters/platform-operations/runtime-health/jdbc` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 52 | `cpf-base-runtime` | internal / internal-starter | `cpf-starters/base/runtime` | Base Runtime Internal | public base operations implementation | no direct generated-domain dependency |
| 53 | `cpf-starter-common` | public / starter-common | `cpf-starters/common` | Common Product Service | code/message/parameter/calendar/template shortcut | DB-backed product service; technical helper dumping 금지 |
| 54 | `cpf-web-runtime` | internal / internal-starter | `cpf-starters/web` | Web | call/callAsync/validate/page/sort/cursor/response/request metadata | controller→repository 금지; security/context/error/log integration |
| 55 | `cpf-data-runtime` | internal / internal-starter | `cpf-starters/data` | data | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 56 | `cpf-data-persistence-runtime` | internal / internal-starter | `cpf-starters/data/persistence` | data | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 57 | `cpf-messaging-runtime` | internal / internal-starter | `cpf-starters/messaging` | messaging | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 58 | `cpf-integration-runtime` | internal / internal-starter | `cpf-starters/integration` | integration | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 59 | `cpf-file-runtime` | internal / internal-starter | `cpf-starters/file` | file | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 60 | `cpf-notification-runtime` | internal / internal-starter | `cpf-starters/notification` | notification | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 61 | `cpf-security-runtime` | internal / internal-starter | `cpf-starters/security` | security | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 62 | `cpf-platform-operations-runtime` | internal / internal-starter | `cpf-starters/platform-operations` | platform-operations | Owner capability가 정의한 Public Operations 또는 internal implementation | Public owner/consumer/failure/recovery/native escape 연결 필수 |
| 63 | `cpf-platform-operations-observability-runtime` | internal / internal-starter | `cpf-starters/platform-operations/observability` | Platform Operations | health/log/metric/trace/config/runtime-control operations | instance topology/drain/degraded/alert/recovery/operator audit |
| 64 | `cpf-data-transaction-jta` | internal / internal-starter | `cpf-starters/data/transaction/jta` | Transaction | required/requiresNew/readOnly/timeout/isolation/hooks | outbox/inbox/saga/reconcile/UNKNOWN; JTA optional |

## Provider Slot — exactly-one / compatibility 검증

- **messaging:** `kafka`, `rabbitmq`, `jms`, `ibm-mq`
- **data:** `jdbc`, `mybatis`, `jpa`
- **cache:** `caffeine`, `valkey`, `redis`
- **integration-transport:** `http`, `tcp`, `soap`
- **integration-codec:** `fixed-length`, `iso8583`
- **file:** `sftp`
- **notification:** `email`, `sms`
- **observability:** `otlp`
- **security-mode:** `resource-server`, `browser-session`, `service-identity`, `browser-session-valkey`
- **locking:** `valkey`
- **object-storage:** `s3-compatible`
- **graphql:** `spring-graphql`
- **realtime:** `sse`
- **health-registry:** `jdbc`

## 각 Module별 필수 검증 컬럼

Developer GPT는 실제 구현 시 위 표를 다음 실행 Matrix로 확장한다.

`artifact → config prefix → AutoConfiguration → Public Operations → Base helper → activation condition → provider/conflict → actual consumer → normal/error/partial failure → recovery/reconcile → Native Escape → Generator → member/external → EDU → Unit/Contract/Runtime/Fault Test → Evidence`

어느 열이 비어 있어도 “Starter Common Function 완료”로 판정하지 않는다.


## Public Operations Bean Registration Gate

각 Starter/Capability 행은 Function 의미뿐 아니라 실제 Bean Graph를 검증한다.

필수 열:
`Public Operations Bean | Provider Bean | AutoConfiguration | Scope | Conditional Activation | Override/Backoff | Conflict Policy | Listener/Thread/Endpoint | Shutdown/Drain | Context Test`

Acceptance:
- selected → Canonical Public Operations Bean exists.
- unselected → related Bean/Thread/Listener/Endpoint 0.
- custom override → documented result.
- provider conflict → fail-fast.
- missing mandatory config → fail-fast.
- singleton operations에 mutable request/transaction state 0.
- async/listener/batch workload는 bounded executor/lifecycle/drain을 가진다.


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



## Developer Custom Bean Injection Gate

Base/Application Runtime의 Public DX에는 `@CpfInject` 또는 동등한 타입 안전 자동주입 기능을 포함한다.

검증 Matrix:
`Annotation → Candidate Resolution → Qualifier → Managed Proxy → Target Bean Method → CPF Runtime Aspect → Error/Fail-fast → Generator/EDU Test`

- Customer-defined CPF Bean 주입.
- Plain Spring Component/@Bean 주입.
- Starter Public Operations 주입.
- zero/multiple candidate behavior.
- qualifier behavior.
- self-invocation/circular-dependency negative behavior.
- unselected capability zero-footprint를 깨뜨리는 optional injection 금지.


## Universal Configuration → Invocation Gate

위 64개 Module의 `configPrefix` 존재만으로 Config 완료 처리하지 않는다.
각 행에 다음 연결을 추가 검수한다.

`Config Prefix → Typed Properties/Schema → Config Policy/Mutability → Secret Separation →
Native Provider Dependency → Binding Cardinality → AutoConfiguration →
Public Operations/Typed Client → Actual Consumer → Health/Diagnostics → Generator/EDU → Evidence`

Binding 정책은 capability 특성에 따라:
- single provider/default,
- named multi binding,
- explicit-only,
- internal no-public-binding
중 하나로 명확히 선언한다.

공통 설정 Framework의 Owner는 Base/Core/Platform Operations 계약으로 두되,
각 기술 설정의 실제 Property/Validation/AutoConfiguration은 해당 Starter Owner가 유지한다.
`cpf-starters/common`에 기술 Capability 설정을 몰아넣지 않는다.

상세 실행표:
`CPF_CONFIGURATION_SETUP_AND_INVOCATION_MATRIX.md`


## Universal Call / Result / Transaction / Log Gate — 추가

64 Starter/Runtime Capability는 기능 성격에 따라 다음을 확인한다.

- same-JVM local API인가 Boundary API인가?
- 허용 return contract는 무엇인가?
- `CpfResult<T>`가 필요한가?
- BUSINESS/TECHNICAL/UNKNOWN이 가능한가?
- retry/idempotency/reconcile이 필요한가?
- transaction boundary는 어디인가?
- TxId/Execution/Segment/Attempt 로그가 남는가?
- DTO/List/Page/Cursor/Map/scalar/Receipt/Async/Stream 자료형을 어떻게 처리하는가?
- Developer Public Function/Client 이름이 직관적인가?
- Generator/EDU actual consumer가 있는가?

특히 Integration/Messaging/File/Object/Notification/Batch Control은 Receipt/UNKNOWN 의미를 빠뜨리지 않는다.
