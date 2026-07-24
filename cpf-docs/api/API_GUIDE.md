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
