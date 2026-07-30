# CPF Gateway 운영 가이드

## 1. 목적

CPF Gateway는 외부 Channel의 공통 진입 정책이 필요한 환경에서 사용하는 선택형 Edge 제품이다. 내부 업무 호출을 무조건 Gateway로 재경유시키지 않으며, 외부 공개와 Routing을 안전하게 통제한다.

## 2. 구성 개념

```text
Service Registry
→ Server Group
→ Route
→ Environment Binding
→ Policy
→ Publish
→ Gateway Instance Apply
→ ACK
```

## 3. Registry

Registry 서비스 정보:

- serviceId
- systemCode
- moduleId
- endpoint
- protocol
- visibility
- health
- zone/cell
- version
- metadata

내부 전용 Endpoint는 외부 공개 후보에서 제외한다.

## 4. Server Group

Server Group은 Routing 대상 Instance 집합이다.

필드:

- groupId
- environment
- protocol
- selectionPolicy
- healthPolicy
- drainPolicy
- members
- version

선택 정책:

- Round Robin
- Weighted
- Rendezvous Hash
- Priority Failover
- Least Load

## 5. Route

Route는 다음을 정의한다.

- Host
- Path
- Method
- Protocol
- Target Service
- Header Policy
- Request/Response 제한
- Timeout
- Rate Limit
- Authentication
- Authorization
- Idempotency
- Retry
- Error Mapping

중복 또는 모호한 우선순위를 Publish 전에 차단한다.

## 6. Binding

Binding은 Route, Server Group과 환경 정책의 Versioned 조합이다.

상태:

```text
DRAFT
→ VALIDATED
→ APPROVAL_REQUESTED
→ APPROVED
→ PUBLISHED
→ RETIRED
```

Published Binding은 직접 수정하지 않는다.

## 7. 외부 공개 기본 거부

다음이 없으면 Publish하지 않는다.

- 인증
- 권한
- TLS
- Rate Limit
- Header Allowlist
- Timeout
- Payload 크기 제한
- Audit
- Connection Test

## 8. 작성자·승인자 분리

- 작성자와 승인자 동일 금지
- Publish/Retire/Rollback 별도 Permission
- Reason
- expectedVersion
- Policy Snapshot
- Audit

## 9. Header Policy

Inbound:

- 표준 Header 생성/검증
- 허용 Header만 승계
- Spoofing 방지
- 인증 문맥 재작성
- Trace 연결
- Client IP 신뢰 경계
- Content Type와 길이

Outbound:

- Hop-by-hop Header 제거
- 내부 Credential 노출 금지
- Deadline 전달
- Target Header 생성

## 10. 인증과 권한

지원 Adapter:

- OAuth2/OIDC
- JWT
- mTLS
- API Key Reference
- 기관 전용 인증 SPI

Credential 원문은 Config나 Log에 저장하지 않는다.

## 11. Rate Limit과 Quota

정책 축:

- Client
- Channel
- Route
- User
- Tenant
- 시간 창
- Burst
- 동시 요청

초과 시 표준 429와 Retry 정보를 반환한다. 분산 환경에서 일관된 Counter Store를 사용한다.

## 12. Timeout과 Retry

- Connect Timeout
- TLS Timeout
- Response Timeout
- 전체 Deadline
- Retryable 오류
- 최대 Attempt
- Backoff
- 비멱등 Command 보호

Gateway Retry와 업무 Service Retry가 중첩되어 폭증하지 않도록 Budget을 공유한다.

## 13. Health와 Routing

Instance 상태:

- UP
- DEGRADED
- DRAINING
- MAINTENANCE
- DOWN
- UNKNOWN

Active/Passive Health와 Hysteresis를 사용한다. 짧은 순간 오류로 Instance를 반복 탈락·복귀시키지 않는다.

## 14. Drain과 Maintenance

- 신규 요청 배정 중단
- In-flight 종료 대기
- 최대 대기시간
- 강제 종료 정책
- 복귀 확인
- Audit

## 15. 연결시험

연결시험은 실제 Network 단계를 수행한다.

```text
DNS
→ Connect
→ TLS
→ Authentication
→ Authorization
→ Protocol
→ Request
→ Response Validation
```

결과:

- testId
- bindingId
- instanceId
- traceId
- duration
- failureStage
- failureCode
- sanitizedMessage

대상 Instance와 Server Group 단위 시험을 지원한다.

## 16. Publish와 Apply

```text
Published Binding
→ 배포 Event
→ Gateway Instance Claim
→ Version/Fencing 확인
→ 설정 검증
→ Atomic Apply
→ ACK
```

각 Instance에 Expected Version과 Applied Version을 저장한다.

## 17. 부분 적용

상태:

- PENDING
- APPLYING
- APPLIED
- FAILED
- STALE
- PARTIAL
- ROLLED_BACK

운영자는 실패 Instance, 원인, Retry 가능 여부를 확인한다.

## 18. ACK와 Stale ACK

ACK는 다음을 포함한다.

- bindingVersion
- instanceId
- fencingToken
- checksum
- appliedAt
- result

낡은 Version 또는 Fencing Token의 ACK는 거부한다.

## 19. 구성 불일치

Expected와 Runtime 실제 구성이 다르면 Drift로 표시한다.

```text
Expected Version
≠ Applied Version
또는 Checksum 불일치
→ Drift
→ Reconcile
```

## 20. Reconcile

1. 실제 상태 조회
2. Expected와 비교
3. 원인 분류
4. 재적용 또는 Rollback
5. 결과 확인
6. Audit

## 21. Rollback

Rollback 대상은 검증된 과거 Version이다.

- 대상 Version
- 호환성
- Instance 순서
- 최대 불가용
- Health Gate
- 실패 시 중단
- 결과

## 22. 거래 원장

Gateway 호출 흐름:

```text
IN
→ AUTH
→ ROUTE
→ TARGET SELECT
→ OUT ATTEMPT
→ RESPONSE
→ RESULT
```

기록:

- transactionId
- traceId
- routeId
- bindingVersion
- target
- attempt
- duration
- protocolStatus
- failureCode
- finalState

## 23. 결과 불명

Target에 요청 전송 후 응답 유실 시 UNKNOWN_RESULT로 분류한다. 비멱등 요청은 상태 조회나 업무 대사 없이 재시도하지 않는다.

## 24. 배포 구성

### 동일 JVM Target

Gateway가 Local Adapter를 호출할 수 있다.

### 분리 Runtime

Registry Endpoint를 통해 Remote 호출한다.

두 방식의 Header, 오류와 거래 원장 의미를 통일한다.

## 25. ADM 화면

분리 메뉴:

- Registry
- Server Group
- Route
- Binding
- Approval
- Apply Status
- Health
- Connection Test
- Transaction
- Attempt
- Drift/Reconcile

## 26. 보안

- 외부 공개 기본 거부
- TLS/mTLS
- Header Spoofing 방지
- SSRF 방지
- Target Allowlist
- Request 크기
- Protocol Parser 제한
- Secret Reference
- Log 마스킹
- 관리자 권한

## 27. Test

- 정상 Routing
- 각 Selection Policy
- Target Down
- Timeout
- Retry
- Failover
- Non-idempotent Unknown
- TLS 실패
- Auth 실패
- Rate Limit
- Partial Apply
- Stale ACK
- Drift
- Rollback
- 다중 Instance

## 28. 체크리스트

- [ ] 외부 공개 기본 거부다.
- [ ] Binding 상태 전이를 서버가 강제한다.
- [ ] 연결시험이 실제 단계를 수행한다.
- [ ] Publish가 Instance Apply와 ACK로 연결된다.
- [ ] Version과 Fencing이 있다.
- [ ] Partial Apply와 Drift를 운영할 수 있다.
- [ ] 거래와 Attempt가 Runtime 호출에 연결된다.
- [ ] 결과 불명과 대사가 있다.
