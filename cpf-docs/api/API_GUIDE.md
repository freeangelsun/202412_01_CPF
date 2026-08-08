# CPF API Guide

## 1. API Principles

- Resource와 업무 행위를 명확히 표현
- 표준 Header 사용
- 안정적인 오류 Contract
- 멱등성
- Paging·sorting 표준
- 호환성
- 권한과 Audit
- OpenAPI와 Runtime 일치

## 2. Base Path

```text
/api/<version>/<domain>/<resource>
```

예시:

```text
/api/v1/members
/api/v1/accounts
/api/v1/accounts/{accountId}/transfers
```

업무 기능 ID 호출을 함께 제공할 수 있습니다.

```text
/api/v1/transactions/OACC-TR-0001
```

## 3. Standard Headers

| Header | Description |
|---|---|
| `X-Transaction-Id` | 전역 거래 식별자. 기본 규격은 34자리이며 모든 내부 호출에 전달 |
| `X-Trace-Id` | Trace 식별자 |
| `X-Transaction-Segment-Id` | 호출 구간 식별자 |
| `X-Channel-Code` | 인증된 Channel |
| `X-Caller-System-Code` | 호출 시스템 |
| `X-User-Id` | 인증된 사용자 |
| `Idempotency-Key` | 멱등 요청 식별자 |

Client가 임의 생성할 수 없는 Header는 Gateway에서 재작성합니다.

`X-Transaction-Id`의 기본 구성은 `yyyyMMddHHmmssSSS`(17) + 모듈 ID(3) +
WAS ID(7) + 일일 순번(7)입니다. 인바운드 값이 34자리 표준에 맞지 않으면
신뢰 경계에서 새 ID를 생성하며, 정상 값은 하위 Local/Remote 호출과 로그에
그대로 전달합니다. 업무 실행 ID(`OACC...`)는 API 기능을 식별하는 별도 값으로
거래 ID 대신 사용하지 않습니다.

`cpf-core`의 예약 3자리 SystemCode는 `CPF`입니다. 따라서 코어가 직접 발급하는
거래 ID의 모듈 구간은 `CPF`, 공통 성공·오류 코드는 `SCPF...`·`ECPF...`, 메시지
코드는 `MCPF...`를 사용합니다. `CPF`는 Framework Core 전용 예약 코드이므로
Generator가 신규 업무 Domain의 SystemCode로 재발급하거나 충돌을 허용하면 안 됩니다.

## 4. Response Envelope

성공:

```json
{
  "transactionId": "20260722103045123ACCwas00010000001",
  "data": {
    "transferId": "TR202607210001",
    "status": "COMPLETED"
  }
}
```

실패:

```json
{
  "transactionId": "20260722103045123ACCwas00010000001",
  "error": {
    "code": "ACC-TRANSFER-40901",
    "message": "출금 가능 잔액이 부족합니다.",
    "retryable": false
  }
}
```

## 5. HTTP Status

- `200`: 조회·처리 성공
- `201`: 생성
- `202`: 비동기 접수
- `204`: 응답 본문 없는 성공
- `400`: 형식·Validation
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 대상 없음
- `409`: 상태 충돌·중복
- `422`: 업무 처리 불가
- `429`: Rate limit
- `500`: 내부 오류
- `502`: 외부 응답 오류
- `503`: 일시적 서비스 불가
- `504`: Timeout

## 6. Idempotency

멱등 API는 `Idempotency-Key`를 요구합니다.

동일 Key와 동일 요청:

- 처리 중: 현재 상태 응답
- 완료: 저장된 결과 재응답
- 실패: 정책에 따라 재시도 가능
- 다른 요청 Body: `409 Conflict`

## 7. Paging

Offset:

```text
?page=0&size=20&sort=createdAt,desc
```

Keyset:

```text
?after=eyJpZCI6MTAwfQ&size=20
```

Response:

```json
{
  "items": [],
  "page": {
    "size": 20,
    "hasNext": true,
    "nextCursor": "..."
  }
}
```

정렬 Field는 allowlist로 제한합니다.

## 8. Date, Time and Number

- Date: ISO-8601
- Timestamp: timezone 포함
- 통화 금액: decimal 또는 minor unit 계약 명시
- Boolean: `true`/`false`
- Enum: 문서화된 대문자 값

## 9. Versioning

- 호환 변경: 동일 Major
- 비호환 변경: 새 Major
- Deprecated 기간 제공
- replacement 링크
- 제거 Release 명시

Field 추가는 Consumer가 unknown field를 허용하는지 확인합니다.

## 10. Async API

접수:

```http
HTTP/1.1 202 Accepted
Location: /api/v1/jobs/JOB-001
```

상태:

```json
{
  "jobId": "JOB-001",
  "status": "RUNNING",
  "progress": 43
}
```

상태 전이는 문서화합니다.

## 11. File API

- metadata와 binary 분리
- size limit
- checksum
- content type
- expiry
- download authorization
- range support
- audit

## 12. OpenAPI

OpenAPI에는 다음을 포함합니다.

- Summary와 description
- 권한
- Header
- Request·response schema
- Example
- 오류 코드
- Idempotency
- Paging
- Deprecated
- operationId와 업무 기능 ID

Runtime smoke에서 OpenAPI endpoint와 실제 API를 함께 검증합니다.

## 13. Approval API 계약

ADM/BZA Approval API는 생성, Inbox/조회, Simulation, Approve/Agree/Review/Reject, Delegate, Cancel/Withdraw, History와 실행 결과를 명시적 operationId로 제공한다. 결정 Command는 idempotency key와 optimistic version을 받아 중복·동시 요청을 안전하게 처리한다.

ADM 위험조치 Approval 완료 응답과 실제 Owner Command 실행 결과를 하나의 성공으로 뭉개지 않는다. 실행 결과가 불명하면 `UNKNOWN`/Recovery 가능한 상태 계약을 노출한다.

## Unified Context / Standard Header Architecture — Canonical Currentization

기존 CPF Standard Header 이름은 Wire Contract 정본으로 유지한다.
단, **Header는 Context 자체가 아니라 HTTP Transport의 Wire Representation**이다.

### Context ↔ Header 책임 분리

```text
CPF Core Context Semantics
        ↓
Transport-neutral Context Envelope/Snapshot
        ↓
HTTP/Web Adapter
        ↓
Existing CPF Standard Headers
```

Core는 HTTP Header 문자열, Servlet, Trusted Proxy, Header Extractor/Mutator/Propagator Runtime을 소유하지 않는다.

### Header 정책 차원

Header Catalog/Spec은 단순 propagation boolean 대신 다음 의미를 코드/문서/Test에서 일치시킨다.

- semantic owner
- transport scope
- propagation scope
- trust level
- source
- mutation policy
- log/mask policy
- max length
- aliases
- inbound/outbound direction
- compatibility status

대표 정책:
- END_TO_END
- PER_HOP
- EDGE_ONLY
- LOCAL_ONLY
- DO_NOT_PROPAGATE
- OPERATION_SCOPED
- TRUSTED_ONLY
- DERIVED_ONLY
- PRESERVE / REGENERATE / OVERWRITE / DROP

### 기존 Header 주요 의미

- `X-Transaction-Id`: Trusted internal E2E logical transaction. Untrusted external 값은 직접 신뢰하지 않고 Ingress가 검증/생성한다.
- `X-Correlation-Id`: END_TO_END correlation. Security Identity로 사용 금지.
- `X-Request-Id`: 현재 Transport Request/Hop. PER_HOP이며 outbound마다 재생성 가능.
- `Idempotency-Key`: OPERATION_SCOPED. 다른 Command로 blind propagation 금지.
- `X-Idempotency-Key`: compatibility alias.
- `traceparent` / `tracestate`: W3C Trace canonical.
- `X-Trace-Id` / `X-Span-Id` / `X-Parent-Span-Id`: compatibility/deprecation 대상. 신규 outbound 중복 생성 금지.
- `X-Tenant-Id`: TRUSTED_ONLY/DERIVED_ONLY.
- `X-User-Id` / `X-Operator-Id`: untrusted client 값 자체를 Identity로 사용 금지. Authentication 결과에서 derive.
- `X-Api-Version`: PER_HOP. downstream blind copy 금지.
- `X-Caller-Service` / `X-Caller-Instance-Id`: PER_HOP/OVERWRITE.
- `Forwarded` / `X-Forwarded-For` / `X-Real-IP`: EDGE_ONLY. Trusted Proxy에서 resolved client IP만 derive.
- Authorization/API Key/Signature/Nonce/Cookie: DO_NOT_PROPAGATE, raw log 금지.
- Gateway/Approval/Batch/Session/Message/File metadata: OWNER_SPECIFIC.

### Required Policy

“CPF 모든 실행에 Required HTTP Header”라는 개념을 사용하지 않는다.
Ingress Profile별 Required Policy를 사용한다.

- External HTTP: Ingress가 CPF transactionId 생성 가능
- Internal CPF HTTP: trusted propagation 요구 가능
- Batch/Scheduler: HTTP Header 없음
- JMS/Kafka: message metadata에서 restore
- File: file execution boundary에서 생성

### Extension

번호형 Reserved/Ext Header 신규 사용 금지.
기존 Consumer는 compatibility migration 후 제거한다.
신규 확장은 namespace + size/key/value/entry/sensitive/transport allowlist 정책을 사용한다.

## Unified Context Header Mapping — Final Freeze

기존 Header 이름은 Wire compatibility 정본으로 유지하되 다음 semantic owner를 강제한다.

- Transaction: X-Transaction-Id, X-Correlation-Id
- Execution: X-Cpf-Standard-Execution-Id, X-Transaction-Segment-Id, X-Parent-Transaction-Segment-Id, X-Transaction-Call-Depth
- Operation: Idempotency-Key canonical, X-Idempotency-Key alias
- Interaction: X-Request-Id, X-External-Request-Id, X-Api-Version, X-Request-Type, Channel, Client metadata, resolved client network metadata
- Identity/Tenant: User/Operator/Tenant는 trusted/derived only
- ServiceCall: Caller/Caller-Instance/Target-Service는 per-hop overwrite
- Gateway: Gateway Instance/Route/Ingress는 owner-specific
- Observability: traceparent/tracestate canonical; X-Trace/X-Span/X-Parent-Span compatibility/deprecation
- Approval: X-Cpf-Approval-* trusted-only
- Security carrier input: Authorization/API-Key/Signature/Timestamp/Nonce는 Context 저장/일반 propagation 금지
- Customer/Member: global Context/Header에서 Business owner로 migration
- Forwarded/X-Forwarded-For/X-Real-IP: edge input only; internal raw propagation 금지
- Reserved/Ext numbered header: 신규 사용 금지, namespace extension으로 migration

Header Spec는 semantic owner/transport scope/propagation/trust/source/mutation/log/mask/maxLength/aliases/direction/compatibility를 표현해야 한다.
