# CPF Transaction / Domain Call / External / Standard Result / Logging DX 상세 Requirement

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실제 실행 시 최신 Git HEAD와 Runtime Evidence를 사용한다.
> 문서 역할: 최상위 정본 8, 8.1, 16.3.4~16.3.17을 실제 개발 가능한 계약으로 상세화  
> 우선순위: Developer Convenience + Commercial Control + Failure/Recovery

## 1. DX 최상위 원칙

CPF는 개발자가 Framework History를 몰라도 쓸 수 있어야 한다.

좋은 Golden Path:

```java
Member member = memberService.find(memberId);
CpfResult<VerifyResponse> result = exsClient.verify(request);
```

나쁜 Golden Path:

```java
Object bean = applicationContext.getBean("exsService");
Map<String,Object> result = callRemote("http://10.0.0.1:8080", "verify", request);
```

Public API는 짧고 Type-safe하며 이름만 보고 용도를 예측할 수 있어야 한다.

## 2. 호출 Boundary 분류

| Boundary | 개발자 기본 호출 | 표준 반환 | DB Tx 의미 |
|---|---|---|---|
| Controller→Service | `call(() -> service.method())` | 자연 Type 또는 Web Response mapping | `call` 자체는 DB Tx 아님 |
| Service→Service same JVM | `service.method()` | 자연 Type | callee Tx policy |
| Service→Repository | `repository.find...()` | persistence Type | caller/callee local Tx |
| MBR→EXS/ACC Domain | `exsClient.method()` | `CpfResult<T>` | caller/callee DB Tx 분리 |
| Explicit Remote Advanced | `callRemote(...)` | `CpfResult<T>` | distributed boundary |
| Domain→External | `bankClient.method()` | `CpfResult<T>` | external side effect boundary |
| Messaging | `publish/send` | `CpfResult<Receipt>` | Outbox/ack 의미 |
| Async | `callAsync/...Async` | `CompletionStage<CpfResult<T>>` at boundary | thread/context boundary |
| Batch/Operation | typed operations | `CpfResult<Receipt>` | chunk/operation boundary |

## 3. Standard Result

Target:

```java
public record CpfResult<T>(
    CpfCallOutcome outcome,
    T data,
    CpfErrorInfo error,
    CpfCallMeta meta,
    CpfRecoveryInfo recovery
) {}
```

### Outcome

- `SUCCESS`
- `BUSINESS_FAILURE`
- `TECHNICAL_FAILURE`
- `UNKNOWN`

Expected remote business/technical/unknown result는 Result로 처리한다.
Programming/configuration/contract misuse는 fail-fast Exception을 허용한다.

## 4. 자료형

| 업무 반환 | Boundary Return |
|---|---|
| DTO 1건 | `CpfResult<MemberDto>` |
| List | `CpfResult<List<MemberDto>>` |
| Paging | `CpfResult<CpfPage<MemberDto>>` |
| Cursor | `CpfResult<CpfCursorPage<MemberDto>>` |
| Map | `CpfResult<Map<String,MemberDto>>` |
| String | `CpfResult<String>` |
| int | `CpfResult<Integer>` |
| long/count | `CpfResult<Long>` |
| boolean | `CpfResult<Boolean>` |
| BigDecimal | `CpfResult<BigDecimal>` |
| data 없음 | `CpfResult<Void>` |
| side-effect acknowledgement | `CpfResult<CpfAck>` |
| operation | `CpfResult<CpfOperationReceipt>` |
| message | `CpfResult<CpfMessageReceipt>` |
| transfer | `CpfResult<CpfTransferReceipt>` |
| async | `CompletionStage<CpfResult<T>>` |
| stream | item stream + terminal `CpfResult<CpfStreamSummary>` |

`false`, `0`, `[]`, `{}`는 SUCCESS data일 수 있다.

## 5. Result Convenience

필수 또는 동등 API:
- `isSuccess()`
- `isBusinessFailure()`
- `isTechnicalFailure()`
- `isUnknown()`
- `data()`
- `error()`
- `meta()`
- `recovery()`
- `map(...)`
- `fold(success,businessFailure,technicalFailure,unknown)`
- side-effect 없는 안전 Contract에 한해 `requireData()`

UNKNOWN 가능 side-effect 호출에서 무조건 `dataOrThrow()`로 결과를 지우는 Golden Path 금지.

## 6. Typed Domain Client

Primary:

```java
@CpfInject
private ExsClient exsClient;

CpfResult<VerifyResponse> result = exsClient.verify(request);
```

Runtime:

```text
EXS logical domain
→ Local/Remote Binding
→ Local managed adapter OR Registry/Router/Transport
→ EXS entry
```

같은 Source로 Local/Remote 동작.

Generic escape:

```java
CpfResult<List<MemberDto>> result =
    callDomain("EXS", "searchMembers", request,
        new CpfTypeRef<List<MemberDto>>() {},
        options);
```

실제 canonical signature는 naming consistency를 검토해 하나로 확정한다.
Raw `Class<List>` 금지.

## 7. `callRemote` 위치

기존 Requirement의 `callRemote/callRemoteAsync`는 제거하지 않는다.
단 다음 용도로 한정한다.

- Remote transport를 명시적으로 강제하는 advanced case.
- Test/Fault harness.
- Native escape/compatibility.

MBR→EXS/ACC 업무 Golden Path는 `callRemote`가 아니라 Typed Domain Client / topology-independent Domain Operation이다.

## 8. External Client

```java
CpfResult<BankResponse> result = bankHostClient.inquire(request);
```

External Client는 Named Binding에서:
endpoint/protocol/codec/TLS/credential/timeout/retry/circuit/bulkhead/rate/idempotency/reconcile를 가져온다.

Source에서 URL/IP/credential 직접 지정 금지.

## 9. MSA Result 처리

### SUCCESS

```java
if (result.isSuccess()) {
    return result.data();
}
```

### BUSINESS_FAILURE

업무거절/검증실패. 기본 retry 금지.
CPF error code/message/field/remote code를 표준 응답으로 전환한다.

### TECHNICAL_FAILURE

pre-send failure, confirmed no-side-effect failure 등.
`retryable=true` + idempotency/operation policy가 모두 허용할 때만 bounded retry.

### UNKNOWN

send 후 timeout/response loss/process kill/ambiguous ack.
blind retry 금지.
`CpfRecoveryInfo`로:
- result probe
- reconcile
- compensate
- manual review
를 연결한다.

## 10. Transaction

### Controller `call`
execution envelope이며 DB transaction이 아니다.

### `required`
현재 local DB transaction 참여/없으면 시작.

### `requiresNew`
독립 commit이 업무적으로 필요한 경우만.

### `readOnly`
query 의미/route/hint.

### Domain call
MBR DB Tx와 EXS DB Tx는 분리한다.
같은 JVM 배치도 remote와 같은 consistency semantics를 유지한다.

### DB + Message
Outbox.

### Message + DB
Inbox/dedup.

### 장기 Workflow
Saga/Compensation/Reconcile.

### XA/JTA/TCC
선택 Capability. 일반 Domain Call에 hidden XA 금지.

## 11. Remote call inside local write transaction

Framework는 다음을 검수한다.

- default Golden Path는 long local write transaction 중 remote side-effect 호출을 피한다.
- 필요하면 명시 Policy/Option을 사용.
- lock duration/deadline/retry/UNKNOWN 영향을 Test.
- Local/Remote parity.

## 12. TransactionId / Execution / Segment

정본 8/8.1을 따른다.

- TxId: E2E 1회.
- ExecutionId: 의미 있는 실행.
- SegmentId/ParentSegmentId: hop lineage.
- Attempt: retry.
- Trace/Span: telemetry.

Retry에서 새 TxId 금지.

## 13. Logging

### Automatic Technical Log

Framework가 Boundary start/end/failure/unknown/reconcile을 자동 구조화한다.

### Business Log

```java
businessLog("MEMBER_JOINED", safeFields);
```

### Operation Log

```java
operationLog("INSTANCE_DRAIN", safeFields);
```

### Security Log

```java
securityLog("ACCESS_DENIED", safeFields);
```

### Audit

```java
audit("PARAMETER_CHANGED", auditContext);
```

### Error

```java
errorLog(error, safeContext);
```

정본 8.1 필드를 사용하고 raw payload/secret/PII를 기록하지 않는다.

## 14. One Transaction Timeline

```text
Tx=T10001
 ├ GW  exec=E001 seg=S001 REQUEST
 ├ MBR exec=E010 seg=S010 MemberService START
 ├ MBR exec=E010 seg=S011 DB COMMIT
 ├ EXS exec=E020 seg=S020 DOMAIN_CALL START
 ├ EXS exec=E021 seg=S021 BANK_CALL SENT
 ├ EXS exec=E021 seg=S021 UNKNOWN response-loss
 ├ EXS exec=E022 seg=S022 RECONCILE CONFIRMED
 └ MBR exec=E010 seg=S010 SUCCESS
```

ADM은 TxId 하나로 timeline/tree를 조회한다.

## 15. Architecture Gate

- Service method에 `CpfResult` 무조건 강제 금지.
- Repository가 `CpfResult`를 기본 persistence return으로 사용하는 것 금지.
- Boundary Client는 standard Result 강제.
- External/Domain client가 raw Object/Map return 금지.
- parameterized generic raw Class 금지.
- raw URL/IP caller 금지.
- string bean/method locator 금지.
- retry new TxId 금지.
- timeout→FAIL 단정 금지.
- raw sensitive logging 금지.

## 16. Current Source Findings

현재 basis에서 직접 확인:
- `CpfServiceClient` stale `com.cpf.core.api.base.CpfRequest/CpfResponse`.
- `ServiceCallResult.status` String.
- `CpfTransactionOutcome`은 XA/TCC recovery state라 일반 call outcome으로 부적합.
- `CpfErrorResponse`가 Core→platform-operations internal `TransactionContext` 참조.
- `CpfErrorResponse` direct system time.
- `CpfLoggingAspect` foundation annotation reference.
- current logging fields는 정본 8.1 전체보다 좁음.

동일 Root Cause를 Repository 전체 검색해 일괄 currentize한다.

## 17. Developer Completion Test

- 처음 보는 개발자가 IDE autocomplete로 Golden Path를 작성 가능.
- Domain local→remote 변경 시 업무 Source diff 0.
- DTO/List/Page/Cursor/Map/scalar/boolean/count/no-data/async/stream type tests.
- business/technical/unknown fault tests.
- Local/Remote transaction parity.
- TxId lineage.
- Log timeline.
- Generator actual output.
- EDU copy-paste sample.
- Manual/API exact-SHA parity.
