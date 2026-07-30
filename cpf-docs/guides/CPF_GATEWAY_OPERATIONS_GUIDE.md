# CPF 게이트웨이 운영 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 게이트웨이 운영자, 경로 설계자, 보안 관리자
> **목적**: 외부 경로와 대상군을 검증·승인·배포하고 일부 적용·정본 불일치·되돌리기를 운영한다.
> **관련 문서**: [상태 점검과 서비스 등록부](CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md) · [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 실행 Owner는 `cpf-gateway`; 운영 진입점은 `cpf-admin` |
| 이 문서로 완료하는 일 | Server Group·Binding을 검증·승인·적용하고 Connection Test·ACK·Partial Apply·Drift·Rollback·Ledger를 운영한다. |
| 적용 범위 | ADM Gateway API, Gateway Internal Control, Registry, Route Runtime, Ledger, Health |
| 주요 독자 | Gateway 운영자, 경로 설계자, 승인자, 보안 관리자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF 게이트웨이는 외부 채널의 공통 진입 정책이 필요한 환경에서 사용하는 선택형 Edge 제품이다. 내부 업무 호출을 무조건 게이트웨이로 재경유시키지 않으며, 외부 공개와 경로 선택을 안전하게 통제한다.

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

## 3. 등록부

등록부 서비스 정보:

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

내부 전용 엔드포인트는 외부 공개 후보에서 제외한다.

## 4. 서버 그룹

서버 그룹은 경로 선택 대상 인스턴스 집합이다.

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

- 순차 순환
- Weighted
- Rendezvous 해시
- 우선순위 장애 전환
- 최소 부하

## 5. 경로

경로는 다음을 정의한다.

- 호스트
- Path
- Method
- Protocol
- 대상 서비스
- 헤더 정책
- 요청/응답 제한
- 시간 제한
- 호출량 제한
- Authentication
- Authorization
- Idempotency
- 재시도
- 오류 매핑

중복 또는 모호한 우선순위를 게시 전에 차단한다.

## 6. 바인딩

바인딩은 경로, 서버 그룹과 환경 정책의 버전이 부여된 조합이다.

상태:

```text
DRAFT
→ VALIDATED
→ APPROVAL_REQUESTED
→ APPROVED
→ PUBLISHED
→ RETIRED
```

게시된 바인딩은 직접 수정하지 않는다.

## 7. 외부 공개 기본 거부

다음이 없으면 게시하지 않는다.

- 인증
- 권한
- TLS
- 호출량 제한
- 헤더 허용 목록
- 시간 제한
- 본문 크기 제한
- 감사
- 연결시험

## 8. 작성자·승인자 분리

- 작성자와 승인자 동일 금지
- 게시/Retire/되돌리기 별도 권한
- 사유
- expectedVersion
- 정책 스냅샷
- 감사

## 9. 헤더 정책

수신:

- 표준 헤더 생성/검증
- 허용 헤더만 승계
- Spoofing 방지
- 인증 문맥 재작성
- 추적 연결
- Client IP 신뢰 경계
- Content Type와 길이

발신:

- Hop-by-hop 헤더 제거
- 내부 인증정보 노출 금지
- Deadline 전달
- 대상 헤더 생성

## 10. 인증과 권한

### 소유 제어 채널 인증

ADM에서 게이트웨이 내부 제어 API를 호출할 때는 일반 사용자 인증과 별도로 소유 제어 채널을 보호한다.

- 요청 시각과 허용 시간 오차
- 재사용할 수 없는 Nonce
- HTTP Method, 경로와 Body Hash
- 공유 비밀값 또는 기관 보안 모듈을 이용한 서명
- 서명 불일치, 만료 요청과 Nonce 재사용 거부
- 요청자·대상·명령·결과 감사

서명 Key 원문은 설정 파일이나 로그에 기록하지 않고 비밀값 참조로 주입한다.


지원 어댑터:

- OAuth2/OIDC
- JWT
- mTLS
- API Key 참조
- 기관 전용 인증 SPI

인증정보 원문은 설정이나 로그에 저장하지 않는다.

## 11. 호출량 제한과 Quota

정책 축:

- Client
- 채널
- 경로
- User
- Tenant
- 시간 창
- Burst
- 동시 요청

초과 시 표준 429와 재시도 정보를 반환한다. 분산 환경에서 일관된 Counter Store를 사용한다.

## 12. 시간 제한과 재시도

- Connect 시간 제한
- TLS 시간 제한
- 응답 시간 제한
- 전체 Deadline
- 재시도 가능 오류
- 최대 시도
- Backoff
- 비멱등 명령 보호

게이트웨이 재시도와 업무 서비스 재시도가 중첩되어 폭증하지 않도록 Budget을 공유한다.

## 13. 상태 점검과 경로 선택

인스턴스 상태:

- UP
- DEGRADED
- DRAINING
- MAINTENANCE
- DOWN
- UNKNOWN

능동·수동 상태 점검과 히스테리시스를 사용한다. 짧은 순간 오류로 인스턴스를 반복 탈락·복귀시키지 않는다.

## 14. 배수와 점검 모드

- 신규 요청 배정 중단
- In-flight 종료 대기
- 최대 대기시간
- 강제 종료 정책
- 복귀 확인
- 감사

## 15. 연결시험

연결시험은 실제 네트워크 단계를 수행한다.

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

대상 인스턴스와 서버 그룹 단위 시험을 지원한다.

## 16. 게시와 적용

게시된 경로는 실행 인스턴스가 주기 동기화 또는 배포 사건으로 가져온다. 적용기는 설정을 임시 스냅샷으로 구성한 뒤 안전 검사를 통과한 경우에만 원자적으로 교체한다. 게이트웨이 시작 시 안전에 필요한 인증·서명·외부 공개 제한이 누락되면 안전 차단한다.

실행 인스턴스는 다음을 보고한다.

- 기대 바인딩 버전과 실제 적용 버전
- 경로 스냅샷 체크섬
- 적용 시각과 소요시간
- 세대 토큰
- 성공·실패와 정제된 오류 코드


```text
게시된 Binding
→ 배포 Event
→ Gateway Instance Claim
→ Version/Fencing 확인
→ 설정 검증
→ Atomic Apply
→ ACK
```

각 인스턴스에 Expected 버전과 Applied 버전을 저장한다.

## 17. 부분 적용

상태:

- PENDING
- APPLYING
- APPLIED
- FAILED
- STALE
- PARTIAL
- ROLLED_BACK

운영자는 실패 인스턴스, 원인, 재시도 가능 여부를 확인한다.

## 18. 확인 응답과 오래된 응답 차단

ACK는 다음을 포함한다.

- bindingVersion
- instanceId
- fencingToken
- checksum
- appliedAt
- result

낡은 버전 또는 세대 토큰의 ACK는 거부한다.

## 19. 구성 불일치

Expected와 실행 환경 실제 구성이 다르면 정본 불일치로 표시한다.

```text
Expected Version
≠ Applied Version
또는 Checksum 불일치
→ Drift
→ Reconcile
```

## 20. 상태 대사

1. 실제 상태 조회
2. Expected와 비교
3. 원인 분류
4. 재적용 또는 되돌리기
5. 결과 확인
6. 감사

## 21. 되돌리기

되돌리기 대상은 검증된 과거 버전이다.

- 대상 버전
- 호환성
- 인스턴스 순서
- 최대 불가용
- 상태 점검 Gate
- 실패 시 중단
- 결과

## 22. 거래 원장

거래 원장은 메모리 임시 기록이 아니라 재시작 뒤에도 조회 가능한 저장소에 남긴다. 요청 본문 원문 대신 식별자, Hash, 크기, 정책 결과와 마스킹된 요약을 기록한다. 여러 대상 시도가 발생하면 최종 결과와 각 시도를 분리해 보존한다.


게이트웨이 호출 흐름:

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

대상에 요청 전송 후 응답 유실 시 UNKNOWN_RESULT로 분류한다. 비멱등 요청은 상태 조회나 업무 대사 없이 재시도하지 않는다.

## 24. 배포 구성

### 동일 JVM 대상

게이트웨이가 로컬 어댑터를 호출할 수 있다.

### 분리 실행 환경

등록부 엔드포인트를 통해 원격 호출한다.

두 방식의 헤더, 오류와 거래 원장 의미를 통일한다.

## 25. ADM 화면

ADM 게이트웨이 화면은 조회 API 외에 운영 상태 스트림을 구독한다. 게시·적용·연결시험·상태 점검·구성 불일치·되돌리기 사건을 수신하면 해당 행과 상세 상태를 갱신한다. 스트림이 끊기면 재연결하되 마지막 사건 이후 누락 가능성을 조회 API로 보정한다.

대상 선택은 순차 순환, Weight, Priority, 최소 부하와 랑데부 해시 외에 승인된 소규모 선행 배포 비율을 지원한다. 선행 대상 선택은 같은 키에 대해 안정적으로 재현돼야 한다.


분리 메뉴:

- 등록부
- 서버 그룹
- 경로
- 바인딩
- 승인
- 적용 상태
- 상태 점검
- 연결시험
- 트랜잭션
- 시도
- 정본 불일치/상태 대사

## 26. 보안

- 외부 공개 기본 거부
- TLS/mTLS
- 헤더 Spoofing 방지
- SSRF 방지
- 대상 허용 목록
- 요청 크기
- Protocol Parser 제한
- 비밀값 참조
- 로그 마스킹
- 관리자 권한

## 27. 테스트

- 정상 경로 선택
- 각 Selection 정책
- 대상 중단
- 시간 제한
- 재시도
- Failover
- 비멱등 요청의 결과 불명
- TLS 실패
- Auth 실패
- 호출량 제한
- 일부 적용
- 오래된 확인 응답
- 정본 불일치
- 되돌리기
- 다중 인스턴스

## 28. 체크리스트

- [ ] 외부 공개 기본 거부다.
- [ ] 바인딩 상태 전이를 서버가 강제한다.
- [ ] 연결시험이 실제 단계를 수행한다.
- [ ] 게시가 인스턴스 적용과 ACK로 연결된다.
- [ ] 버전과 Fencing이 있다.
- [ ] 일부 적용과 정본 불일치를 운영할 수 있다.
- [ ] 거래와 시도가 실행 환경 호출에 연결된다.
- [ ] 결과 불명과 대사가 있다.

## 29. 실제 운영 API 지도

ADM은 Gateway 데이터베이스를 직접 수정하지 않고 `CpfGatewayRegistryPort`를 통해 Gateway 소유 실행 환경을 제어한다. 운영 절차의 시작점은 다음 API다.

| 목적 | Method와 경로 | 핵심 입력·판정 |
|---|---|---|
| 설치·연결 확인 | `GET /adm/api/gateway-registry/capability` | `installed`, `available`, `status`, 지원 Protocol·상태 코드 |
| 운영 요약 | `GET /adm/api/gateway-registry/operations/snapshot` | 거래·오류·결과 불명·정본 불일치·Spool·연결시험 상태 |
| 증분 운영 사건 | `GET /adm/api/gateway-registry/operations/events` | `afterEventId`, `limit` |
| 서버 그룹 조회·저장 | `GET/POST /adm/api/gateway-registry/server-groups` | 환경·서비스·Protocol·선택 정책·Member·Version |
| 바인딩 조회·저장 | `GET/POST /adm/api/gateway-registry/bindings` | Route·Server Group·정책·유효기간·Version |
| 상태 전이 | `POST /adm/api/gateway-registry/bindings/{id}/state` | 제한된 Draft 전이만 허용. `APPROVED`·`ACTIVE`·`BLOCKED`·`RETIRED`는 Approval Owner 실행 API 사용 |
| 인스턴스별 적용 | `GET /adm/api/gateway-registry/bindings/{id}/apply-status` | 기대·적용 Version, 상태, 오류 코드, 마지막 확인 시각 |
| 연결시험 요청 | `POST /adm/api/gateway-registry/bindings/{id}/connection-tests` | 시험 유형·사유·만료 시각·Operation ID |
| 연결시험 추적 | `GET /adm/api/gateway-registry/connection-test-operations/{operationId}` | 대기·실행·완료·취소·만료 상태 |
| 재검증·취소 | `POST .../{operationId}/revalidate`, `POST .../{operationId}/cancel` | 새 Operation ID 또는 기대 Version과 사유 |
| 폐기 | `DELETE /server-groups/{id}`, `DELETE /bindings/{id}` | 일반 API는 `409`로 거부. Approval Owner 실행 API에서만 수행 |

`requestedBy`는 Request Body 값을 신뢰하지 않고 검증된 ADM 운영자 문맥으로 다시 설정한다. 운영 API를 직접 호출할 때도 사용자 ID를 임의로 넣어 권한 검사를 우회할 수 없다.

## 30. 서버 그룹 등록 예제

다음은 운영 환경의 HTTPS 대상 두 개를 순차 순환 방식으로 묶는 예다. 신규 생성은 `expectedVersion`을 `null`로 두고, 수정은 조회한 현재 Version을 전달한다.

```json
{
  "operationId": "gw-sg-pay-20260730-001",
  "serverGroupId": "PAY-API-PROD",
  "groupName": "결제 API 운영 대상군",
  "environmentCode": "PROD",
  "serviceId": "PAY",
  "endpointCode": "PAY-API",
  "targetProtocol": "HTTPS",
  "loadBalancePolicy": "ROUND_ROBIN",
  "hashKeySource": "",
  "healthPolicyId": "HP-PAY-API",
  "failoverGroupId": "PAY-API-DR",
  "directAllowed": false,
  "members": [
    {"instanceId": "pay-api-01", "weight": 100, "priority": 1, "canaryPercent": 0, "enabled": true},
    {"instanceId": "pay-api-02", "weight": 100, "priority": 1, "canaryPercent": 0, "enabled": true}
  ],
  "expectedVersion": null,
  "reason": "운영 결제 API 대상군 최초 등록"
}
```

정상 응답은 `resourceType`, `resourceId`, `status`, `version`, `changedAt`을 가진다. 응답 Version을 다음 수정의 `expectedVersion`으로 사용한다. 같은 Operation ID를 다시 보냈을 때의 의미는 Owner 구현의 멱등성 계약과 감사 이력으로 확인한다.

### 30.1 등록 전 확인

1. `serviceId`와 `endpointCode`가 서비스 등록부에 존재하는지 확인한다.
2. Member 인스턴스가 운영 환경에 속하고 점검·배수 상태가 아닌지 확인한다.
3. `WEIGHTED_ROUND_ROBIN`을 사용하면 모든 Weight가 양수인지 확인한다.
4. `RENDEZVOUS_HASH`를 사용하면 `hashKeySource`가 실제 표준 Header 또는 요청 Key를 가리키는지 확인한다.
5. 우선순위 장애 전환을 사용하면 주 대상군과 장애 전환 대상군의 순환 참조를 차단한다.

## 31. 바인딩 Draft 예제와 게시 Gate

```json
{
  "operationId": "gw-binding-pay-v1-001",
  "bindingId": "PAY-API-V1-PROD",
  "route": {
    "standardExecutionId": "OPAY000101",
    "serviceId": "PAY",
    "httpMethod": "POST",
    "endpoint": "/internal/payments/v1",
    "operationId": "createPayment",
    "requiredPermission": "PAYMENT_CREATE",
    "auditReasonRequired": false,
    "routeVersion": "1",
    "routeId": "PAY-CREATE-V1",
    "environmentCode": "PROD",
    "hostPattern": "api.example.internal",
    "pathPattern": "/partner/v1/payments",
    "apiVersion": "v1",
    "serverGroupId": "PAY-API-PROD",
    "ingressProtocol": "HTTPS",
    "targetProtocol": "HTTPS",
    "tlsPolicyId": "TLS-PROD-INTERNAL",
    "authenticationPolicyId": "AUTH-OIDC-PARTNER",
    "authorizationPolicyId": "AUTHZ-PAY-CREATE",
    "headerPolicyId": "HDR-CPF-STANDARD",
    "rateLimitPolicyId": "RATE-PAY-PARTNER",
    "healthPolicyId": "HP-PAY-API",
    "connectTimeoutMs": 3000,
    "responseTimeoutMs": 10000,
    "overallTimeoutMs": 15000,
    "maxRetryCount": 0,
    "idempotent": false,
    "failoverGroupId": "PAY-API-DR",
    "enabled": true,
    "expectedVersion": 0
  },
  "serverGroupId": "PAY-API-PROD",
  "gatewayAllowed": true,
  "directAllowed": false,
  "approvalId": null,
  "effectiveFrom": "2026-07-31T00:00:00+09:00",
  "effectiveTo": null,
  "expectedVersion": null,
  "reason": "결제 생성 API 운영 경로 Draft 등록"
}
```

Route 계약은 다음을 서버에서 강제한다.

- 연결·응답·전체 시간 제한은 모두 양수여야 한다.
- 전체 시간 제한은 연결·응답 시간 제한보다 작을 수 없다.
- 재시도 횟수가 1 이상이면 `idempotent=true`여야 한다.
- HTTPS 또는 gRPC Ingress에는 `tlsPolicyId`가 필요하다.
- 활성 Route에는 `serverGroupId`가 필요하다.

Draft 저장 뒤에는 `VALIDATED → APPROVAL_PENDING → APPROVED → ACTIVE` 흐름을 따른다. 일반 상태 전이 API는 `APPROVED`, `ACTIVE`, `BLOCKED`, `RETIRED`를 거부하고 Server Group·Binding 폐기도 직접 수행하지 않는다. 이 조치는 작성자·승인자 분리, 승인 Snapshot과 Payload Hash를 검증하는 ADM Approval Owner 실행 API에서만 수행한다.

## 32. 일부 적용 복구 Runbook

1. `GET /bindings/{id}/apply-status`로 기대 Version, 적용 Version, 상태, 오류 코드를 인스턴스별로 수집한다.
2. `operations/snapshot`에서 정본 불일치 수, 실패 연결시험 수와 Spool 적체를 함께 확인한다.
3. 적용 실패가 정책 검증 오류라면 확대 적용을 즉시 중단하고 Draft를 수정하거나 검증된 이전 Version으로 되돌린다.
4. 특정 인스턴스의 파일·Secret·TLS·네트워크 오류라면 그 인스턴스를 배수하고 원인을 복구한다.
5. 오래된 ACK는 Version과 Fencing Token 불일치로 제외하고 현재 소유 인스턴스의 재적용 결과만 수용한다.
6. 재적용 후 Checksum, 실제 경로 동작과 연결시험 결과를 확인한다.
7. 일부 적용 기간에 처리된 거래는 `bindingVersion`, 대상 Instance와 Attempt 단위로 조회해 대사한다.
8. 모든 인스턴스가 기대 Version을 적용하고 거래 이상이 없을 때만 사고를 종료한다.

### 32.1 완료 증적

- Binding ID와 기대 Version
- 승인 ID와 승인 Payload Hash
- Instance별 적용 전·후 상태
- 연결시험 Operation과 결과
- 재적용 또는 되돌리기 명령
- 일부 적용 기간의 거래 조회 결과
- 행위자·사유·승인·감사 ID

## 33. 실패 응답 해석

| 상황 | 대표 HTTP 의미 | 운영 판단 |
|---|---|---|
| Path와 Body ID 불일치 | `400` | 입력 오류. 재시도 전에 요청 수정 |
| 일반 API로 승인·활성·차단·폐기 요청 | `409` | Approval Owner 실행 경로를 사용 |
| Gateway Provider 미구성 | `503` | 설치·연결 상태 확인. 임의 Local 대체 금지 |
| 운영자 문맥 없음 | `401` | 인증 Session과 제어 채널 확인 |
| 기대 Version 불일치 | `409` | 최신 상태를 다시 읽고 충돌 해결 |
| 연결시험 만료 | 상태 코드로 표현 | 새 Operation ID로 재검증 |
| 요청 전달 후 응답 유실 | `UNKNOWN_RESULT` | 자동 재시도하지 않고 대상 업무 결과 대사 |

## 34. 수신 경로와 대상 경로 분리

Gateway Route는 외부에서 수신하는 `pathPattern`과 소유 시스템에 전달하는 대상 경로를 분리한다. 현재 공개 계약에서 `CpfGatewayRoute.endpoint`는 Source 호환용 이름이며 `targetPath()`가 같은 값을 대상 경로로 제공한다.

```text
수신:  /partner/v1/payments/{paymentId}
대상:  /internal/payments/v1/{paymentId}
```

경로 재작성기는 수신 Pattern에서 변수 또는 Wildcard를 추출해 대상 Template의 같은 Token에만 주입한다. 다음 입력은 소유 시스템 호출 전에 차단한다.

- `%2e`, `%2f`, `%5c`, Null·CR·LF 같은 인코딩 우회
- Backslash, 제어 문자, `.`·`..` Segment
- 대상 Template에 남은 미해석 `{token}` 또는 `*`
- `/`로 시작하지 않는 Pattern·대상 경로

`pathPattern`과 대상 경로를 같게 두는 것은 허용되지만, 외부 공개 경로와 내부 API 경로가 다르면 반드시 둘을 명시적으로 분리한다. 경로 변경은 연결시험의 `GATEWAY_E2E` 유형으로 실제 Gateway 수신부터 대상 응답 계약까지 검증한다.

## 35. 소유 제어 요청 서명 정본

ADM과 Gateway Owner 사이 제어 요청은 다음 값을 줄바꿈으로 연결한 Canonical 문자열을 HMAC-SHA256으로 서명한다.

```text
HTTP method
request target
normalized content-type
request body SHA-256
caller service
verified operator ID
timestamp epoch millis
nonce
audience
key ID
```

정본 Header는 `CpfGatewayControlHeaders`가 소유한다.

| Header | 의미 |
|---|---|
| `X-CPF-Caller-Service` | 허용된 호출 서비스 |
| `X-CPF-Operator-Id` | 검증된 운영자 |
| `X-CPF-Gateway-Control-Timestamp` | 요청 시각 |
| `X-CPF-Gateway-Control-Nonce` | 재생 방지 값 |
| `X-CPF-Gateway-Control-Content-SHA256` | Body Hash |
| `X-CPF-Gateway-Control-Audience` | 대상 Gateway 환경·제품 |
| `X-CPF-Gateway-Control-Key-Id` | 검증 Key 선택자 |
| `X-CPF-Gateway-Control-Signature` | HMAC-SHA256 서명 |

수신 측은 서명 비교만으로 끝내지 않는다. 허용 시간 오차를 확인하고 `(audience, keyId, callerId, nonce)`를 다중 인스턴스 공용 저장소에서 단 한 번만 Claim한다. Nonce 저장소 또는 보안 감사 저장소 장애를 성공으로 바꾸지 않고 안전 차단한다.

## 36. 승인 Owner 경계

다음 조치는 일반 Gateway Registry 편집 API의 책임이 아니다.

- Binding `APPROVED`, `ACTIVE`, `BLOCKED`, `RETIRED` 전환
- Server Group 폐기
- Binding 폐기

일반 API가 이 요청을 받으면 `409 Conflict`로 거부한다. 운영 화면은 오류를 우회하지 않고 Approval Owner 흐름으로 이동시켜 다음을 다시 확인한다.

1. 현재 Version과 변경 Snapshot
2. 요청자·승인자 분리
3. 승인 ID와 Payload Hash
4. 영향 경로·대상·인스턴스
5. 중단·되돌리기 조건
6. 실행 결과와 Audit ID

## 부록 A. 경로 게시 전 검토표

| 검토 영역 | 필수 확인 |
|---|---|
| 노출 | 외부 공개 여부, 호스트·경로 중복, 우선순위 |
| 인증 | 인증 방식, 토큰 수신자, 키·인증서 참조 |
| 권한 | 채널·사용자·역할·기관 범위 |
| 헤더 | 허용 목록, 위조 방지, 내부 헤더 제거 |
| 대상 | 서비스·대상군, 상태 점검, 영역·센터 우선순위 |
| 안정성 | 전체 시간 예산, 재시도, 회로 차단, 동시 요청 상한 |
| 데이터 | 요청·응답 크기, 형식, 민감정보 마스킹 |
| 운영 | 연결시험, 단계 배포, 확인 응답, 되돌리기 |

## 부록 B. 부분 적용 대응 절차

1. 게시 버전과 각 인스턴스의 기대·적용 버전을 비교한다.
2. 실패 단계를 다운로드, 검증, 준비, 원자적 전환, 확인 응답으로 분류한다.
3. 오래된 확인 응답과 세대 토큰 불일치를 제외한다.
4. 설정 자체 오류면 전체 확대를 중단하고 검증된 과거 버전으로 되돌린다.
5. 특정 인스턴스 환경 오류면 해당 인스턴스를 배수 상태로 전환하고 복구한다.
6. 재적용 뒤 실제 체크섬과 경로 동작을 연결시험으로 확인한다.
7. 일부 적용 기간의 거래를 버전별로 조회하고 이상을 대사한다.

## 부록 C. 연결시험 실패 코드 예

| 단계 | 코드 예 | 운영 조치 |
|---|---|---|
| DNS | `GW_DNS_RESOLUTION_FAILED` | 이름·검색 도메인·센터별 DNS 확인 |
| 연결 | `GW_CONNECT_TIMEOUT` | 방화벽·포트·대상 상태 확인 |
| TLS | `GW_TLS_TRUST_FAILED` | 체인·호스트명·만료·신뢰 저장소 확인 |
| 인증 | `GW_AUTH_REJECTED` | 비밀값 참조·토큰 대상·권한 확인 |
| 프로토콜 | `GW_PROTOCOL_MISMATCH` | HTTP/gRPC/TCP 설정과 경로 확인 |
| 응답 | `GW_RESPONSE_CONTRACT_FAILED` | 상태 코드·헤더·본문 검증 규칙 확인 |

## 부록 D. 비멱등 요청

게이트웨이가 요청을 대상에 전달한 뒤 응답을 잃으면 자동 재시도하지 않는다. `operationId`와 대상 업무의 결과 조회 계약으로 실제 처리 상태를 확인하고 `UNKNOWN_RESULT`를 해소한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Public Port | `CpfGatewayRegistryPort.java`, `CpfGatewayRoute.java`, `CpfGatewayLedgerPort.java` | ADM·Runtime 간 Typed Contract와 수신/대상 경로 |
| ADM API | `AdmGatewayRegistryController.java` — `/adm/api/gateway-registry` | 조회·Draft·상태·연결시험 |
| Owner Control | `CpfGatewayRegistryInternalController.java`, `CpfGatewayControlSigner.java`, `CpfGatewayControlNoncePort.java` | Canonical 서명·Audience·다중 인스턴스 Nonce Claim |
| Runtime Sync | `CpfGatewayRouteSynchronizer.java`, `CpfGatewayRouteRuntimeApplier.java`, `CpfGatewayPathRewriter.java` | Version·Checksum·Atomic Apply·안전한 대상 경로 재작성 |
| Health/Test | `CpfGatewayProbeExecutor.java`, `CpfGatewayHealthWorker.java`, `CpfGatewayConnectionTestWorker.java` | 실제 Probe·`GATEWAY_E2E` 연결시험 |
| Ledger | `DurableCpfGatewayLedgerAdapter.java` | 거래·Attempt 영속 원장 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
