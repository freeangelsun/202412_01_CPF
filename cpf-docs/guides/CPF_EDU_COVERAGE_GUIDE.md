# CPF 교육·예제 범위 가이드

## 1. 목적

CPF 교육 자료는 별도 장난감 규격을 만들지 않는다. 실제 제품 Public API, SPI, Error, Paging, 보안과 운영 계약을 그대로 사용한다.

## 2. 교육 원칙

- 실제 Product API 사용
- 내부 Package Import 금지
- 정상뿐 아니라 오류·경계·복구
- Local/Remote 비교
- 권한과 감사
- DB와 Runtime
- 운영 화면 연결
- 실행 가능한 Test

## 3. 교육 구조

```text
cpf-reference/
├─ foundation
├─ online
├─ remote-call
├─ messaging
├─ file
├─ telegram
├─ batch
├─ center-cut
├─ security
├─ operations
└─ failure-scenarios
```

## 4. Foundation

주제:

- Strings/Numbers/Decimals
- Date/Time/Clock
- Collection
- Page/Slice/Cursor
- Header
- transactionId
- Validation
- Masking
- Secret Reference

각 예제는 입력, 결과, 오류를 제공한다.

## 5. 온라인 거래

- 표준 Header
- 실행 Annotation
- Transaction Context
- Validation
- Error Mapping
- Audit
- Trace

## 6. Local/Remote

같은 Facade를 Local과 Remote로 실행한다.

시나리오:

- 정상
- 4xx
- 5xx
- Timeout
- Target Down
- Retry
- Circuit
- Commit 후 응답 유실
- Failover

## 7. Paging

- Offset Page
- Slice
- HMAC Cursor
- Sort Allowlist
- 대량 검색
- 잘못된 Cursor
- Count 비용

## 8. Messaging

- Outbox
- Publisher
- Inbox
- 중복
- Retry
- DLQ
- Replay
- Schema Version
- Poison

## 9. File

- 안전한 Path
- Checksum
- Upload
- Scanner
- Quarantine
- Download
- Duplicate
- Credential Reference
- 결과 불명

## 10. 전문

- Fixed Length Layout
- Encoding
- Padding
- Validation
- 전문→DTO
- DTO→전문
- 오류 위치
- 민감 필드 마스킹

## 11. Batch

- Job Pack
- Definition
- Parameter
- Schedule
- Worker
- Restart
- Reprocess
- Unknown
- Agent
- Signature

## 12. Center-Cut

- Target Provider
- Partition
- Claim
- Fencing
- Handler
- Failed
- Unknown
- Reprocess

## 13. 보안

- 인증
- 권한
- Secret
- Masking
- Audit
- Approval
- File Scan
- Download

## 14. 운영

- Registry
- Health
- Transaction
- Log
- Incident
- Gateway Apply
- Batch Control
- Config Policy
- Reconcile

## 15. Generated Domain

Generator로 두 임의 Domain을 생성해 교육한다.

- 구조
- Public API
- DB
- OpenAPI
- Test
- Registry
- Remove

## 16. 오류 예제

- Null
- 빈 값
- 최대 길이
- Duplicate
- Optimistic Lock
- Timeout
- Stale Fencing
- Unauthorized
- Forbidden
- Rate Limit
- Unknown
- Poison
- Scanner Down

## 17. 교육 문서 형식

각 주제:

1. 목표
2. 선행지식
3. 구조
4. 코드
5. 실행
6. 정상 결과
7. 오류 결과
8. 운영 조회
9. Test
10. 확장 과제

## 18. 실행

교육 예제는 Repository에서 Build/Test 가능해야 한다.

```powershell
.\gradlew.bat :cpf-reference:clean :cpf-reference:test :cpf-reference:assemble
```

## 19. Browser

ADM/BZA 교육은 실제 Route와 권한 계정을 사용한다.

- 조회
- 변경
- 403
- 409
- 위험 조치
- Audit
- 접근성

## 20. Evidence

- Source Commit
- Command
- Profile
- 결과
- Screenshot 보조
- Log/Query
- 관련 API
- 민감정보 제거

## 21. Coverage Matrix

| Capability | API | Example | Error | Test | Operations |
|---|---|---|---|---|---|
| Foundation | O | O | O | O | - |
| Remote Call | O | O | O | O | O |
| Messaging | O | O | O | O | O |
| File | O | O | O | O | O |
| Batch | O | O | O | O | O |
| Gateway | O | O | O | O | O |
| Security | O | O | O | O | O |

## 22. 체크리스트

- [ ] 실제 Public API를 사용한다.
- [ ] 내부 구현 Import가 없다.
- [ ] 오류와 복구 예제가 있다.
- [ ] 운영 화면에서 추적할 수 있다.
- [ ] Generator 산출물과 규격이 같다.
- [ ] 예제가 Build/Test 된다.
