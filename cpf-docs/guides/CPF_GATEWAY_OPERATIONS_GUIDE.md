# CPF 게이트웨이 운영 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 게이트웨이 운영자, 경로 설계자, 보안 관리자
> **목적**: 외부 경로와 대상군을 검증·승인·배포하고 일부 적용·정본 불일치·되돌리기를 운영한다.
> **관련 문서**: [상태 점검과 서비스 등록부](CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md) · [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md)

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

Inbound:

- 표준 헤더 생성/검증
- 허용 헤더만 승계
- Spoofing 방지
- 인증 문맥 재작성
- 추적 연결
- Client IP 신뢰 경계
- Content Type와 길이

Outbound:

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

인증정보 원문은 설정나 로그에 저장하지 않는다.

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
- Non-idempotent Unknown
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
