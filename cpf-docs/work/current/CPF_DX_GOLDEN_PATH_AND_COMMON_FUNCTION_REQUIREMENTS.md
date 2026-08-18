# CPF Golden Path / Common Function / Starter Composition 상세 개발 Requirement

## 1. 목적

본 문서는 `DEVEX-LAYER`, `DEVEX-ANNOTATION`, `DEVEX-LOGGING`, `DEVEX-TESTKIT`,
`TX-DX`, `STARTER-DX`, `SAMPLE-MBR`, `SAMPLE-EDU`, `BAT-*`의 Acceptance를
실제 고객 개발 코드 수준으로 상세화한다. 신규 Canonical ID를 만들지 않는다.

목표는 “기능이 있다”가 아니라 **업무 개발자가 안전한 상용 기본값을 짧은 코드로 사용**하는 것이다.

## 2. Target Golden Path

```java
@CpfController
public class MemberController extends MemberBaseController {
    private final MemberService memberService;

    public MemberResponse detail(String memberId) {
        return call(() -> memberService.detail(memberId));
    }
}
```

```java
@CpfService
public class MemberService extends MemberBaseService {
    private final MemberRepository memberRepository;

    public MemberResponse detail(String id) {
        return cacheGet("member:" + id,
                () -> memberRepository.findById(id));
    }
}
```

```java
@CpfRepository
public class MemberRepository extends MemberBaseRepository<Member, String> {
    // provider-neutral common operations를 재사용하고
    // 특수 query만 명시적으로 확장한다.
}
```

Starter를 추가해도 위 `extends MemberBase*`는 바뀌지 않는다.

## 3. 금지 예

```java
// 금지: Starter별 상속 교체
class MemberService extends CpfRedisMessagingHttpService { }
```

```java
// Golden Path 금지: Controller가 Repository를 직접 소비
class MemberController {
    MemberRepository repository;
}
```

```java
// 금지: String service locator
applicationContext.getBean("memberService");
```

```java
// 금지: Base가 수백 capability method를 직접 구현하는 God Base
class CpfBaseService {
   // redis/kafka/http/jpa/sftp/... provider code 직접 소유
}
```

## 4. Annotation

### Controller / Service / Repository
- 실제 Spring bean 등록/정책 consumer.
- optional explicit name + canonical default name.
- duplicate/conflicting role fail-fast.
- type-based constructor injection.
- exact 3-tier or provider-appropriate Repository contract validation.
- Annotation runtime behavior Test.

### DTO
`@CpfDto`는 singleton bean이 아니다.
Validation/serialization/mapping/masking/metadata를 제공한다.
Mutable request state를 singleton으로 올리는 구현은 금지한다.

## 4A. Common Function Bean Registration

공통 Function은 Base method만 추가하고 끝내지 않는다.

- 선택 Starter의 Public `*Operations`/Facade가 실제 Spring Bean으로 등록되어야 한다.
- Base helper와 direct constructor injection이 동일 Bean/Runtime Consumer를 사용한다.
- stateless singleton 기본, mutable request/transaction state singleton field 저장 금지.
- 미선택 Capability Bean/Listener/Thread/Endpoint/SQL/mandatory Config 0.
- Provider conflict/duplicate role/name fail-fast.
- Customer override/backoff contract.
- bounded Async Executor Bean + Context propagation + rejection/backpressure + graceful drain.
- Messaging Listener/Realtime/Integration Client/Batch Job·Step의 실제 Bean registration/lifecycle 검증.
- Context Test로 selected/unselected/conflict/missing config/custom override/shutdown을 검증한다.


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



## 4B. Developer Custom Bean 자동 주입 / 메서드 호출

`@CpfInject` 또는 동등한 CPF Public Annotation을 제공하여 업무 개발자가 만든 Bean을 이름 검색 없이 자동 주입한다.

### Target DX

```java
@CpfService
public class MemberPolicyService extends MemberBaseService {
    public boolean canJoin(String memberId) {
        return true;
    }
}
```

```java
@CpfService
public class MemberService extends MemberBaseService {

    @CpfInject
    private MemberPolicyService memberPolicyService;

    public boolean canJoin(String memberId) {
        return memberPolicyService.canJoin(memberId);
    }
}
```

CPF 실행 Envelope가 필요하면:

```java
return call(() -> memberPolicyService.canJoin(memberId));
```

### Resolution Rules

- 기본은 Type 기반.
- `@CpfService/@CpfRepository`, 일반 Spring Component/`@Bean`, Starter Public Operations Bean 모두 대상.
- 후보 0 → 필수 주입 FAIL.
- 후보 2+ → Qualifier 없으면 FAIL.
- 다중 구현체는 `@CpfQualifier` 또는 동등 Canonical Qualifier.
- Bean name/String Service Locator/ApplicationContext 직접 조회를 일반 업무 Golden Path로 사용하지 않는다.
- Inject된 Proxy를 통해 Transaction/Retry/Logging/Security Runtime Consumer가 동작해야 한다.
- self-invocation/circular dependency Negative Test.
- Generator/member/external/education에 실제 Custom Bean 선언→자동 주입→method 호출 예제를 제공한다.



## 4C. Configuration / Domain Call / External Client Golden Path

개발자 DX는 `API를 어떻게 호출하는가`뿐 아니라 `어디에 무엇을 설정하는가`까지 포함한다.

### CPF Domain Call

CPF 관리 Domain은 배포 위치와 무관한 Typed Client를 사용한다.

```java
@CpfInject
private ExsClient exsClient;

public VerifyResponse verify(VerifyRequest request) {
    return exsClient.verify(request);
}
```

`EXS`가 같은 JVM이면 Local Adapter, 별도 WAS/IP이면 Remote Adapter가 선택되며 Business Source는 동일하다.
`CpfRestClient(serviceId, ...)` 같은 raw serviceId API가 필요하면 Advanced/Native Escape로 유지할 수 있지만 Golden Path가 아니다.

Domain Binding은 최소:
`domain/systemCode → topology/local-or-remote → service registry/static endpoint → instances → health → routing → transport`
를 가진다.

### External Client

외부기관은 Domain Client와 다른 Named Client/Channel로 선언한다.

```java
@CpfInject
private BankHostClient bankHostClient;

public BankResponse inquire(BankRequest request) {
    return bankHostClient.inquire(request);
}
```

HTTP/TCP/SOAP/ISO8583/SFTP 등의 구현은 Starter가 소유하고,
업무 Source는 endpoint/credential/codec plumbing을 반복하지 않는다.

### Setup 원칙

- Generator에는 논리 dependency/client 이름을 둔다.
- 물리 endpoint/credential/secret은 environment/deployment binding에 둔다.
- selected capability는 typed Properties/Schema/validation과 실제 Bean을 제공한다.
- unselected capability는 관련 config requirement/bean/thread/listener/endpoint가 0.
- `REFRESHABLE`/`RESTART_REQUIRED`를 구분한다.
- 동일 transport 여러 외부기관은 named binding으로 확장 가능해야 한다.
- Provider의 native config를 중복 복제하지 않고 CPF semantic config와 bridge한다.
- Config/Binding 오류는 개발자가 이해할 수 있는 fail-fast message를 제공한다.
- IDE metadata/한글 JavaDoc/EDU 예제로 설정 경로를 찾을 수 있어야 한다.

### 사용성 완료 기준

개발자가 Public 문서와 IDE metadata만 보고:
1. Starter 선택,
2. 설정 파일/env/secret 작성,
3. Typed Bean 주입,
4. 업무 Method 호출,
5. 오류 원인 확인,
6. 다중 Provider/Named Binding 선택,
7. 운영 상태 확인
을 수행할 수 있어야 한다.

## 5. Base / Operations 설계 규칙

### Mandatory Base
Base에는 모든 Application이 사용하는 안정된 의미만 둔다:
- context/execution
- validation/error
- standard result/response
- safe call primitives
- logging facade access

### Optional Capability
Cache/Messaging/Integration/Security/Persistence/Batch 등은 각 Owner의 Public Operations가 기능을 소유한다.
DomainBase는 Generator가 선택 Starter에 따라 composition/wiring을 제공한다.

### Native Escape
복잡한 케이스는 `CpfCacheOperations`, `CpfMessagingOperations`, `CpfTransactionOperations` 같은
동등 Public API를 직접 injection해 provider-neutral 상세 제어를 한다.
Provider native API가 필요한 경우에도 내부 package 직접 참조가 아니라 공식 escape boundary를 사용한다.

## 6. Controller Function Catalog

| Function 의미 | 자동 적용 | 오류/복구 |
|---|---|---|
| `call` | Context/transactionId/execution/trace/error mapping | Service error를 CPF 표준 결과/로그에 연결 |
| `callAsync` | async context snapshot/restore/completion tracking | timeout/cancel/error/UNKNOWN |
| `context/currentUser/permission` | trusted entry context | missing trust fail-closed |
| `validate` | CPF/Jakarta validation | field/global error standardization |
| `page/sort/cursor` | max limit/sort allow-list | invalid cursor/sort fail-fast |
| response helper | standard status/body/header | arbitrary error-code 합성 금지 |
| request metadata | idempotency/correlation/deadline | invalid/replayed request policy |
| request/error log | masking/correlation | raw body/secret logging 금지 |

## 7. Service Function Catalog

- `call`, `callAsync`
- transaction `required`, `requiresNew`, `readOnly`
- transaction timeout/isolation/rollback/afterCommit/afterRollback
- `retry`, `timeout`, `idempotent`, `reconcile`
- `cacheGet/Put/Evict/GetOrLoad`
- `publish/send`
- integration `callRemote/callRemoteAsync`
- `authorize/hasPermission/currentPrincipal`
- `audit`
- `businessLog/operationLog/securityLog/errorLog`
- `code/message/parameter/calendar/template`

Helper 명칭은 실제 Public API naming consistency를 검토해 확정한다.
이 표를 그대로 method count 목표로 오해하지 않는다. 중복/모호 helper는 Operations object로 grouping할 수 있다.


## 7A. Canonical Call / Result Function Naming — Developer-First

아래 이름은 Target Public Naming이다.
실제 기존 API와 충돌이 있으면 하나의 더 직관적인 이름으로 currentize하되 Manual/Catalog/Generator/EDU를 동시에 바꾼다.

| 용도 | Golden Path | 표준 결과 |
|---|---|---|
| Controller execution | `call`, `callAsync` | 자연 Type/Web mapping |
| same-JVM Service | 직접 typed method | 자연 Type |
| Domain | typed Domain Client / `callDomain` | `CpfResult<T>` |
| explicit remote | `callRemote` | `CpfResult<T>` |
| External | typed External Client / `callExternal` | `CpfResult<T>` |
| Transaction | `required/requiresNew/readOnly` | `T` |
| Cache | `cacheGet/Put/Evict/GetOrLoad` | `T`/Optional semantics |
| Messaging | `publish/send` | `CpfResult<Receipt>` |
| Logging | `businessLog/operationLog/securityLog/audit/errorLog` | void/receipt where durable |
| Common | `code/message/parameter/calendar/template` | typed business value |

### Standard Result Rule

모든 메서드 Wrapper 강제는 금지한다.
Boundary Function만 `CpfResult<T>`를 강제하고 same-JVM Service/Repository는 자연스러운 Type을 유지한다.

### Developer usability gate

처음 보는 개발자가:
1. IDE에서 함수명을 찾고,
2. 한글 JavaDoc으로 옵션/Tx/Result/Log를 이해하고,
3. Sample copy-paste 없이도 기본 호출을 작성할 수 있어야 한다.

## 8. Transaction 상세

### `required`
기존 transaction 참여 또는 새 local transaction.
Context/tx metadata와 rollback 결과를 기록.

### `requiresNew`
기존 transaction을 suspend하고 독립 commit이 업무적으로 필요한 use case만.
Audit/Logging 편의를 위해 무분별하게 사용 금지.
outer rollback / inner commit 및 inner failure / outer success를 각각 테스트.

### `readOnly`
read-only routing/hint/timeout과 write attempt policy를 명확히 함.

### Hook
`afterCommit`, `afterRollback`은 side effect 의미를 명확히 한다.
메시지 발행은 hook만으로 “exactly once”를 주장하지 않고 durable requirement는 Outbox 사용.

### Remote boundary
HTTP/Message remote call을 Local DB transaction에 포함된 것처럼 표현하지 않는다.
response loss 후 side effect가 실행됐는지 모르면 UNKNOWN이다.

## 9. Logging 상세

Logging Public Operations는:
- business
- operation
- security
- audit
- error
의 의미를 구분한다.

자동 field:
- systemCode / instanceId
- transactionId / executionId / segment/attempt
- actor/user/service identity / tenant
- jobId/stepId where batch
- error code/classification
- trace/span/correlation

masking:
- credential/token/password/secret/key
- 주민/계좌/전화/이메일 등 정책상 PII
- Evidence에서도 동일 적용

Log 실패가 업무 transaction을 임의 rollback시키지 않되,
규제상 audit durability가 필수인 경로는 별도 durable policy를 설계한다.

## 10. Cache 상세

기존 `CpfCacheAsideService`의:
- TTL
- negative cache
- single-flight
- fail-open
기반을 살린다.

보강:
- write/update invalidation
- version/fence
- multi-instance refresh
- stale serve policy
- provider outage/reconnect
- serializer schema/version
- cache stampede pressure
- Redis/Valkey/Caffeine parity
- selected provider exactly-one

## 11. Messaging 상세

Public Operations 최소 의미:
- publish/send/consume
- key/header/correlation/context
- schema/content-type/version
- ack/commit boundary
- retry/backoff
- DLQ/quarantine
- duplicate/idempotency
- ordering/rebalance
- Outbox/Inbox
- publish result probe/UNKNOWN

Kafka/Rabbit/JMS/IBM MQ가 동일한 Public semantic을 공유하되 native feature 차이는 explicit capability/escape로 표현한다.

## 12. Integration 상세

- local vs remote topology transparent call contract
- sync/async
- timeout/deadline
- retry eligibility
- circuit breaker
- bulkhead/rate limit
- auth/credential
- request/response masking
- remote error taxonomy
- correlation/context propagation
- remote UNKNOWN/result probe/reconcile
- HTTP/TCP/SOAP/fixed-length/ISO8583/Webhook 등 선택 capability의 실제 Consumer

## 13. Security 상세

- current principal / service identity / tenant
- authorize/permission
- reason/approval context
- SoD/break-glass
- session revoke/expiry
- masking/secret
- audit
- batch/message/integration identity propagation

## 14. Repository 상세

### class mode
JDBC/MyBatis:
`CpfBaseRepository → DomainBaseRepository → BusinessRepository`

### interface mode
JPA/Spring Data:
`@CpfRepository interface ... extends CpfRepositoryContract ...`

두 mode 모두 고객은 Repository 하나로 이해한다.

Common semantics:
CRUD/search/page/cursor/bulk/lock/timeout/error/id.
Provider native query escape는 허용하지만 CPF Context/Tx/Audit 정책을 보존한다.

## 15. Batch 상세

Batch Workload Base는 기존 운영 `CpfBatchOperationsPort`를 대체하지 않는다.

Workload helper:
- currentJob/currentStep/execution
- businessDate/parameters
- chunk/page/cursor
- partition
- checkpoint/watermark/resume
- retry/skip
- idempotency
- transaction/commit
- lock/lease/fencing
- progress/log/audit/metric
- stop/cancel/drain
- UNKNOWN/reconcile
- restart/rerun protection

Control Plane:
- scheduler/worker/agent/runner/center-cut 상태/명령
- 위험조치 approval/audit
- execution/query/recovery

둘은 동일 execution identity와 audit trail을 공유한다.

## 16. Generator / EDU / Regression Fan-out

변경 대상은 Framework API 하나가 아니다.

1. Canonical Starter Catalog
2. AutoConfiguration / Operations
3. Generator definition/template
4. `cpf-member`
5. `cpf-external`
6. `cpf-education`
7. Online Sample
8. Batch Sample
9. Testkit
10. relevant ADM/BZA/OpenAPI/Generated Client
11. JavaDoc/Guide
12. exact-SHA Evidence

하나라도 예전 DAO/raw transaction/raw cache/raw logging Golden Path를 대표 예제로 유지하면 미완료다.

## 17. Test Strategy

- Unit: pure operations/policy
- Contract: Public API/provider contract
- Architecture: layer/annotation/base/naming/internal visibility
- Integration: actual Starter + actual provider
- Runtime: actual process/DB/broker/cache/browser
- Fault: process kill/network/broker/DB/cache response loss
- Negative: invalid annotation/provider combination/direct repository from controller where enforceable
- Generated: member/external parity
- EDU: actual consumer path
- Native Escape: official escape still works

## 18. 완료 증명

Function 존재만으로 완료하지 않는다.
각 Function family마다:
`source owner + public API + auto config + base helper + consumer + failure/recovery + generated use + EDU use + test + evidence`
를 제출한다.


### Configuration Discoverability / Override DX

개발자가 설정을 위해 Framework Source를 검색해야 하면 DX 완료가 아니다.

- Local에서는 안전한 loopback/stable-port default로 빠르게 실행 가능.
- Dev/Stage/Prod에서는 environment binding 위치가 명확하고 unsafe fallback은 fail-fast.
- 사용하지 않는 주요 option도 commented example + 한글 설명.
- IDE configuration metadata 제공.
- Source 확장은 Typed Customizer/Builder/Options/SPI를 사용.
- Per-call override는 configured bound 내에서만 허용.
- Prod Endpoint/Secret/Security 정책은 Source override로 우회 불가.

## Local Integrated / Optional Topology Developer Golden Path

개발자가 환경 때문에 업무 코드를 바꾸지 않는 것을 최우선 DX로 한다.

1. `local-integrated`는 `cpf-local-runtime` 하나를 8080 기본 Port로 실행한다. Gateway는 선택이며 기본 OFF다.
2. Generated Domain은 descriptor로 자동 발견한다. 같은 JVM에서는 Domain/Common Function을 직접 typed binding으로 사용하며 raw localhost URL을 업무 Source에 넣지 않는다.
3. Batch annotation/runtime API도 같은 JVM에서 사용할 수 있으나 Job은 자동 시작하지 않는다. 운영형 Scheduler/Worker 분리는 별도 topology 선택이다.
4. 서버 환경은 한 서버 통합부터 완전 분산까지 inventory/topology 선택만 바꾸며 Public API는 동일하다.
5. Generated Domain 삭제는 Definition/Local/Deploy/Build integration point 영향도를 함께 처리한다.

### Result 후처리

```java
return result.fold(
    data -> afterSuccess(data),
    failure -> afterBusinessFailure(failure),
    failure -> afterTechnicalFailure(failure),
    unknown -> reconcile(unknown.recoveryInfo())
);
```

### 표준 Header

업무 코드는 Canonical 거래 Header를 직접 조립하지 않는다.

```java
CpfContext context = CpfContexts.requireCurrent();
String transactionId = context.transactionId();
String campaignCode = CpfHttpHeaders.requireCurrent().get("X-Campaign-Code");
```

내부 Domain 호출에서는 CPF Runtime이 `X-Transaction-Id`, `X-Original-Channel`, `X-Current-Channel`,
`X-Caller-Channel`, `X-Target-Channel`, `X-Target-Operation-Id`를 Target Contract와 trusted Runtime Context로
자동 구성한다. Current Channel은 Receiver가 자동 확정하며, Channel Policy는 `operationId + callerChannel`을
사용한다. 일반 Custom Header만 `CpfHttpHeaders.Builder#set/add`로 다루며 Canonical/보호 Header는 업무 코드가
변경할 수 없다. Same-JVM 호출도 HTTP를 만들지 않을 뿐 동일한 Channel/Operation 의미로 Context를 전환한다.

### Fixed-Length

```java
CpfFixedLengthWriteResult outbound = fixedLength.write(values, "BANK01", "3");
CpfFixedLengthParseResult inbound = fixedLength.parse(payload, "BANK01", "3");
```

업무 개발자는 Parser/Writer 구현을 직접 만들지 않고 Layout/Converter만 확장한다.
