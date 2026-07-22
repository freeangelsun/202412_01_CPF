# CPF API·Security Guide

## 1. Header

- Transaction
- Trace
- Segment
- Channel
- Caller
- User
- External
- `X-Cpf-Ext-*`

Trust Boundary와 Allowlist를 적용한다.

## 2. Transaction ID

34자리 정본과 업무 실행 ID 정책을 혼동하지 않는다. Product Contract 하나를 확정하고 Source·UI·SQL·Guide를 일치시킨다.

## 3. Response

```json
{
  "transactionGlobalId": "...",
  "code": "SUCCESS",
  "message": "정상",
  "data": {},
  "meta": {}
}
```

## 4. Error

- Code
- Retryable
- Unknown
- Compensation
- Field Error
- User/Operator Message
- Masked Detail
- Cause ID

## 5. Paging

- List
- Offset
- Slice
- Cursor
- Limit
- Sort Allowlist
- Signed Cursor

## 6. Async

- operationId
- statusUri
- acceptedAt
- callback
- polling
- event

## 7. File

- Multipart
- Streaming
- Size
- Count
- Content Type
- Checksum
- Permission
- Audit

## 8. Security

- AuthN
- AuthZ
- RBAC
- MFA
- IP
- mTLS
- OAuth/JWT/API Key
- CSRF
- CSP
- SSRF
- PII
- Masking
- Secret
- Audit
- Rate Limit

## 9. JavaDoc/OpenAPI

- Purpose
- Permission
- Header
- Request/Response
- Error
- Idempotency
- Limits
- Thread Safety
- Security
- Since
- Deprecated
