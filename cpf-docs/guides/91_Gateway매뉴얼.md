# CPF Gateway 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF`
> **기준 Branch** `master`
> **기준 Commit** `c2e1680fcf42467d445df97f1a3a0c36dab783ef`
> **문서 목적** Gateway 선택 기준, SCG MVC Data Plane, Route·Predicate·Filter·Rewrite, Target·보안·Resilience, Validate·Approval·Publish·ACK/NACK·LKG·Rollback과 장애 복구를 설명한다.
> **주요 독자** Gateway 개발자·운영자, 네트워크·보안 담당자, ADM Gateway 개발자
> **문서 사용 결과** Gateway Route를 등록·검증·게시·관측·대사·Rollback하고 기준 Commit의 제한을 구분한다.

## 0. 문서 사용 계약

이 문서는 제품 목표, 기준 Commit의 구현, 실제 실행 검증을 분리한다.

- 목표는 구현·검증 여부와 무관한 제품 계약이다.
- 현재 구현은 Source·SQL·API·Config·Frontend·Script·Test의 exact path로 판정한다.
- 실행하지 않은 Build·DB·Kafka·Browser·다중 인스턴스·장애 시나리오는 `미검증`이다.
- Source에 없는 Class·API·Property·Route·Permission·상태를 만들지 않는다.
- 허용 상태는 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`뿐이다.
- 명령 실행 전 Local Working Tree를 확인하고 기존 변경을 보호한다.


## 1. 선택 기준

Gateway가 필요한 경우: 공통 외부 진입, 인증·인가·Header·CORS·Route 정책, 중앙 게시·ACK·감사, 여러 Service Target.

직접 진입을 검토할 경우: 내부 단일 Service, 공통 정책 없음, 추가 Failure Point가 불필요한 구성.

Gateway 사용 여부와 무관하게 업무 Public API와 Idempotency·Error 계약은 같아야 한다.

## 2. 현재 구현 상태

| 항목 | 상태 | Source·판정 |
|---|---|---|
| Artifact | `부분 구현` | `cpf-gateway/build.gradle`: SCG Server Web MVC, BootJar, Core·Resilience·Observability |
| Data Plane | `부분 구현` | `CpfScgPrimaryRouteConfiguration`이 업무 `/**`를 Handler에 연결하고 Actuator/Internal/Control 제외 |
| Route Snapshot | `부분 구현` | ACK Route Load, Empty Route fail, 30초 Refresh, 오류 시 Last Normal Snapshot 유지 |
| Target | `부분 구현` | Fresh Heartbeat·Zone·Priority·Weight 선택, Canonical URI·DNS/CIDR 검사 구현; Connection Address Pinning 재확인 필요 |
| Auth·Audit·Ledger | `부분 구현` | Authentication·Authorization·Reason·Attempt·Completion Source 확인 |
| Transaction ID | `부분 구현` | 내부 Ledger ID는 UUID, 별도 신뢰 Transaction ID를 검증·전달. 두 식별자의 화면·대사 구분 필요 |
| Trust Boundary | `부분 구현` | `x-cpf-*`/`x-forwarded-*` Client Header 거부, 설치 Allowlist와 Header Budget, Server Principal/Instance 재구성 구현 |
| SSRF | `재확인 필요` | Scheme·Authority·Path/Query Canonicalization, Private/CIDR DNS 검사 구현; 검증 DNS와 실제 연결 Address Pinning은 미구현 |
| Retry·Body | `부분 구현` | Idempotent+Idempotency-Key+Replay-safe Body만 Buffer Retry, Streaming은 단일 Attempt; Runtime 검증 미수행 |
| Failure Stage | `부분 구현` | Connect/DNS는 결과 미확정에서 제외하고 Timeout/EOF/Socket·Client/Stream을 Unknown 처리; Side Effect 대사는 필요 |
| Multi-instance | `미검증` | Target cursor는 JVM Local. ACK·Drift·Load 분산 Runtime 미실행 |

## 3. Data Plane·Control Plane

### Data Plane

- SCG MVC Router/Handler
- Route Match·Path Rewrite
- Authentication·Authorization
- Target Resolve·HTTP Proxy
- Circuit Breaker·Retry
- Attempt·Transaction Ledger

### Control Plane

- Route·Binding·Server Group
- Version·Checksum·Validation
- Approval·Publish
- Candidate·ACK/NACK·Active·Last Known Good
- Drift·Reconcile·Rollback

Control Plane DB를 Data Plane 요청마다 조회하지 않고 ACK Snapshot을 사용한다.

## 4. Route Match

현재 Match 입력:

- 설치 Property `cpf.gateway.environment-code` (`Handler`가 Client Environment Header를 신뢰하지 않음)
- Host
- Raw Path
- HTTP Method
- `X-Api-Version` 또는 `v1`

Enabled·Environment·Host·Path·Method·Version을 필터하고 Host/Path specificity와 routeId로 정렬한다. 일치 Route가 없으면 거부한다.

확인할 Route 필드:

- routeId·routeKey·standardExecutionId
- environment·hostPattern·pathPattern·httpMethod·apiVersion
- target service·serverGroup·targetPath
- routeVersion·expectedVersion·checksum
- timeout·retry·idempotent·auditReasonRequired
- auth·permission·CORS·Header Policy

## 5. Route Snapshot

Property:

- `cpf.gateway.allow-empty-routes=false`
- `cpf.gateway.route-refresh-millis=30000`

기동 시 ACK Route 0건이고 allow-empty가 false면 중단한다. Refresh 오류 시 기존 Snapshot을 유지한다. Candidate 준비와 ACK 기록 사이의 Atomicity·Instance별 ACK·Checksum은 Control Plane Runtime으로 검증해야 한다.

## 6. Path Rewrite

Ingress Pattern과 Target Path를 분리한다.

```text
Ingress /api/orders/{id}
Target /orders/{id}
```

계약 Test:

- Path variable·encoded slash·query·trailing slash
- duplicate slash·dot segment·Unicode normalization
- target base path
- empty/invalid rewrite
- traversal·open redirect

## 7. Target·Discovery·Load Balancing

현재 Resolver:

1. Service Registry에서 `UP` 후보를 조회한다.
2. active, not maintenance/draining, `staleAfter` 이내 Heartbeat를 필터한다.
3. 같은 Zone 후보가 있으면 우선하고 가장 낮은 Priority를 선택한다.
4. Weight와 Server Group별 JVM-local Cursor로 Target을 선택한다.
5. HTTP/HTTPS, Authority, Control Character, 단일·이중 Decode Traversal, Query Size를 검증한다.
6. DNS 결과의 Loopback/Link-local/Multicast/Public 여부를 검사한다.

남은 검증:

- 검증 DNS와 실제 HTTP 연결 Address Pinning·Rebinding.
- Redirect 후 Authority/CIDR 재검증.
- 다중 Gateway Instance의 분산·Drift.
- Registry Stale·Empty Target·DNS 변경 Runtime Fixture.

## 8. Authentication·Authorization·Header

- 외부 Authorization은 Authentication Port에만 전달되는지 확인한다.
- Client가 보낸 내부 Principal·Environment·Instance·Approval Header를 정본으로 신뢰하지 않는다.
- Trusted Proxy Chain과 Forwarded Header Allowlist를 적용한다.
- Downstream Header는 Allowlist로 재구성하고 Hop-by-hop·Credential을 제거한다.
- Audience·Service Identity·Route Permission·Data Scope를 검증한다.

현재 Handler의 Header 복사 범위는 재검토가 필요하다.

## 9. HMAC Control Channel

Canonical Request 최소 요소:

- Method·Request Target
- Content-Type·Body SHA-256
- Caller·Operator
- Timestamp·Nonce
- Audience·Key ID

Nonce Store는 Gateway 인스턴스 간 공유되어야 한다. Body 변조·Clock Skew·Replay·Cross-environment를 Negative Test한다.

## 10. Timeout·Retry·Circuit·Bulkhead

- Connect·Send·Response Header·Read·Overall Deadline을 구분한다.
- Retry는 남은 Budget 안에서만 수행한다.
- Mutation은 Idempotency와 Body replay 조건이 모두 성립할 때만 Retry한다.
- Circuit 이름·Threshold·Open Duration·Fallback 결과를 운영에 노출한다.
- Bulkhead·Connection Pool·Rate Limit·Queue 상한을 설정한다.
- Downstream Client의 자체 Retry와 중복되지 않게 한다.

현재 Handler는 idempotent flag에 따라 Retry Count를 사용하고 IOException·TimeoutException을 대상으로 한다. Streaming/one-shot body 재시도는 `재확인 필요`다.

## 11. Attempt Ledger·`UNKNOWN_RESULT`

Transaction Start에는 Route·Version·Target Group·Method·Path·Request bytes를 기록한다. 각 Attempt에는 Target·Duration·Protocol Status·Failure·Unknown을 기록한다.

현재 구현 한계:

- 모든 upstream exception Attempt를 unknown으로 기록한다.
- Filter 예외를 `CLIENT_OR_STREAM` Unknown으로 묶는다.
- pre-connect, request partial send, side effect, response read 실패를 세분화해야 한다.
- Client disconnect와 Async completion의 실제 종료 시점을 Runtime Test해야 한다.

대사:

1. transaction/attempt 조회.
2. Target Service Idempotency·Status API 조회.
3. Side Effect 확인.
4. Success·Failed·Unknown 확정.
5. Retry·Compensation·Operator decision.
6. Audit와 Evidence 연결.

## 12. Route 생명주기

```text
DRAFT → VALIDATED → APPROVED → CANDIDATE
→ INSTANCE ACK/NACK → ACTIVE
ACTIVE → BLOCKED/RETIRED/ROLLED_BACK
```

- Validate: Schema, Collision, Capability, Rewrite, Target, TLS, Security, Timeout.
- Approval: Request Hash, Version, Checksum, requester/approver 분리.
- Publish: Candidate 배포, Instance별 ACK/NACK.
- Activate: 정책이 요구한 ACK Quorum 충족 후.
- LKG: 직전 정상 Version·Checksum 보존.

## 13. ACK/NACK·Partial Apply

각 Instance 보고:

- instanceId·artifact version
- route version·checksum
- appliedAt
- ACK/NACK·error code·unsupported capability
- probe result

일부 ACK 상태는 `PARTIAL`이다. NACK Instance의 Traffic 제외·재동기화·Artifact Version·Cache/DB를 확인한다. Retry는 실패 Instance만 대상으로 한다.

## 14. Connection Test·Probe

구분:

- DNS
- TCP
- TLS
- HTTP
- Application

결과에는 resolved IP, Duration, Certificate, HTTP Status, Response Contract, Failure Stage를 포함한다. TCP Connect만으로 Application Ready를 확정하지 않는다.

## 15. ADM 절차

### 등록·게시

1. Environment·Route ID·Owner 확인.
2. Host·Method·Ingress·Target Rewrite 입력.
3. Server Group·Timeout·Security·CORS 선택.
4. Collision Preview.
5. Connection/Application Probe.
6. Validate 결과 확인.
7. Reason·Approval·Expected Version.
8. Publish·Operation ID 기록.
9. ACK/NACK·Checksum 확인.
10. Smoke·Ledger·Metric 확인.

### 차단

영향 Route·진행 거래·대체 경로·Retry·Unknown을 확인하고 Approval 후 차단한다. 모든 Instance 적용을 확인한다.

### Rollback

LKG, 현재 Candidate, DB·Policy Compatibility, 진행 요청을 확인한다. Rollback 후 ACK·Smoke·Metric·Ledger·Audit를 확인한다.

## 16. 설치·기동

- `cpf-gateway.jar` Source SHA·Checksum
- JDK·Port·TLS·Service Account
- Registry·Route Provider·Ledger·Audit DB
- Instance ID·Environment
- Empty Route 정책·Refresh 주기
- Security·Resilience·Connection Pool
- Health·Readiness·Build SHA

## 17. Log·Metric·Capacity

Log: routeId, routeVersion, transaction, attempt, target instance, failure stage, result, masked reason.
Metric: RPS, latency, status, target failure, circuit, retry, active connection, queue, snapshot refresh, ACK/NACK.
Cardinality가 큰 transaction/URI 원문을 Metric Label로 쓰지 않는다.

## 18. 장애 Runbook

### ACK Route 0건

allow-empty, Route Provider, Approval·ACK, DB 연결 확인. Default deny 유지. 승인 없는 임시 Route 주입 금지.

### Snapshot 갱신 실패

Last Normal Snapshot Version·LoadedAt, Provider DB, Schema, Checksum 확인. 반복 오류 Alert. Stale 허용 기한 초과 정책 확인.

### Target 없음

Registry TTL·Health·Maintenance·Draining·Service ID 확인. 임의 Host 우회 금지.

### Upstream Timeout·5xx

Attempt별 Failure Stage·Target·Circuit·Retry를 확인. Mutation Side Effect 가능성은 상태 조회 후 재시도.

### SSRF 의심

Route Target·DNS·Resolved IP·Redirect·Proxy Header·Audit 보존. Route 차단과 Credential rotate 검토.

### Partial Apply

NACK Instance Traffic 제외, Version·Checksum·Capability 비교, Failed-only Retry 또는 LKG Rollback.

## 19. Test

- Route Match·Specificity·Default deny
- Rewrite·Encoding·Traversal
- Header spoof·Auth·Permission·Reason
- HMAC Body change·Replay·Audience
- SSRF CIDR·DNS Rebinding·Redirect
- Connect·Send·Response·Read Timeout
- GET/Mutation/Streaming Body Retry
- Circuit·Bulkhead·Rate Limit
- ACK/NACK·Partial·Drift·LKG
- Client disconnect·Response loss·Unknown Reconcile
- Multi-instance Registry·Target·Snapshot
- Load·Large body·Slow consumer

## 20. EDU

1. 주문 Route와 Target Rewrite 정의.
2. Security·Timeout·Idempotency 정책 작성.
3. Validate·Collision·Connection Probe.
4. Approval·Publish.
5. 두 Gateway Instance ACK 확인.
6. 한 Instance NACK 주입.
7. Target Timeout·Response loss·Client disconnect 주입.
8. Attempt·Unknown·Target 상태 대사.
9. LKG Rollback.
10. Metric·Audit·Evidence 확인.

## 21. 현재 제한사항

- SCG MVC Source 경로는 확인했다.
- Standard transactionId, Header Trust, SSRF, Body Retry, Failure Stage가 목표와 완전히 일치하지 않는다.
- Control Plane API·DB·ACK Runtime과 Browser·Multi-instance·Load·Fault는 `미검증`이다.

## 부록 A. 기준 Commit Source 지도

| 책임 | Source | 현재 확인 |
|---|---|---|
| SCG Business Route | `CpfScgPrimaryRouteConfiguration` | `/**`에서 Actuator·Internal·Control 경로 제외 |
| Data Plane Handler | `CpfScgPrimaryHandler` | Route Match, AuthN/AuthZ, Retry·Circuit, Ledger Attempt |
| 응답 완료 원장 | `CpfGatewayLedgerCompletionFilter` | Servlet 응답 종료 시 Transaction Completion |
| Route Snapshot | `CpfGatewayRouteSnapshot` | ACK Public Route, Candidate, LKG Memory Snapshot |
| Target 선택 | `CpfScgTargetResolver` | Service Registry `UP` Instance Round-Robin |
| Path Rewrite | `CpfGatewayPathRewriter` | Pattern→Target Path |
| Build | `cpf-gateway/build.gradle` | SCG Server Web MVC, Core·Resilience·Observability |

## 부록 B. 실제 Request 처리 순서

```text
Business Route Match
→ 설치 Environment·Host·Path·Method·API Version으로 ACK Route 선택
→ Header 정리
→ Authentication
→ Authorization
→ 위험 Route Reason 확인
→ Transaction/Attempt 원장 시작
→ Target Path Rewrite
→ Service Registry UP Instance 선택
→ Circuit Breaker·조건부 Retry
→ SCG HTTP Forward
→ Attempt 저장
→ Servlet Response 종료 시 Transaction 완료
```

### B.1 Match 입력

- 설치 Property `cpf.gateway.environment-code`가 Route Environment 입력이다. Client의 Environment Header는 Route 선택 정본으로 사용하지 않는다.
- `Host`
- Request Raw Path
- HTTP Method
- `X-Api-Version`: 미입력 시 `v1`

Route는 enabled, Environment, Host Pattern, Path Pattern, Method, API Version 순으로 Filter하고 Host·Path Specificity와 Route ID로 정렬한다. 일치 Route가 없으면 Default Deny다.

## 부록 C. Snapshot 기동·갱신

| Property | Default | 동작 |
|---|---:|---|
| `cpf.gateway.allow-empty-routes` | `false` | `false`에서 ACK Route 0건이면 기동 실패 |
| `cpf.gateway.route-refresh-millis` | `30000` | Public Route Snapshot 갱신 주기 |

기동 시 `loadPublicRoutes()`가 null이면 실패한다. 빈 Route를 허용하지 않으면 실패한다. 주기 갱신 중 예외가 발생하면 마지막 정상 Snapshot을 유지한다.

주의: `refreshNow()`는 Provider가 반환한 Map을 새 Snapshot으로 교체한다. ACK·Version·Checksum 검증이 Provider에서 수행되는지 Owner Source·DB Query와 함께 확인한다.

## 부록 D. Target 선택과 SSRF 경계

현재 Target Resolver는 Service ID, `UP`, active, maintenance/draining, Heartbeat Freshness를 확인하고 Same Zone·최저 Priority·Weight로 Target을 선택한다. URI Authority와 Path/Query를 Canonicalize하고 Loopback·Link-local·Multicast와 Public Address 정책을 검사한다.

남은 항목:

- DNS 검증 결과와 실제 HTTP Connection Address 고정.
- Redirect 재검증과 명시 Port 정책.
- Multi-instance Cursor 공유 또는 전체 분배 Acceptance.

따라서 Target 정책은 `부분 구현`, DNS Rebinding 방지는 `재확인 필요`, 다중 인스턴스 분산은 `미검증`이다.

## 부록 E. Retry·Circuit 동작

현재 Handler는 Route가 `idempotent=true`일 때만 `maxRetryCount + 1` Attempt를 허용하고, 그 외에는 1회만 실행한다. Wait Duration은 50ms이며 `IOException`, `TimeoutException`을 Retry 대상으로 설정한다. Circuit Breaker 이름은 `gateway-<routeId>`다.

운영 확인:

- POST·PATCH·DELETE가 idempotent로 잘못 등록되지 않았는가
- Request Body가 Replay 가능한가
- Downstream Idempotency Key·Request Hash가 있는가
- 전체 Deadline 안에서 Retry가 종료되는가
- Gateway와 Client/Downstream의 중복 Retry가 없는가
- Attempt별 Target·Failure Stage·Unknown이 남는가

현재 코드의 Timeout 값이 Route Policy에서 SCG HTTP Client에 실제 적용되는지 Runtime Test가 필요하다.

## 부록 F. Header·Transaction ID 현재 주의사항

Handler는 Client의 `x-cpf-*`와 `x-forwarded-*` Header를 거부하고, Authorization과 설치 Allowlist의 Context Header만 Header Count/Byte Budget 안에서 수용한다. Downstream에는 Server가 Principal, Transaction, Gateway Transaction, Route, Instance Header를 재구성한다. Header Negative Test와 Proxy Chain은 Runtime에서 검증한다.

현재 Gateway Transaction ID는 UUID로 생성된다. CPF 목표의 34자리 transactionId와 일치하지 않으므로 다음을 구분한다.

- Gateway 내부 Transaction Ledger ID
- CPF 표준 transactionId
- Trace ID
- Downstream Operation/Idempotency ID

문서나 화면에서 UUID를 표준 transactionId로 표시하지 않는다. Header Spoof Negative Test 전에는 이 항목을 `부분 구현`으로 둔다.

## 부록 G. Attempt·Completion 상태

### G.1 Attempt

성공/최종 4xx·5xx와 Retryable Status를 구분해 Attempt를 기록한다. Connect/DNS 실패는 Side Effect 전으로 보고 `unknown=false`가 될 수 있고, Timeout·EOF·Socket은 `unknown=true`로 기록해 대상 Side Effect를 대사한다.

### G.2 Transaction Completion

Servlet Response 종료 시 다음을 기록한다.

- HTTP Status
- Target Instance Attribute
- Duration
- Response Byte Count
- Failure Stage
- Unknown 여부

현재 Filter는 Filter Chain에서 예외가 발생하면 `UNKNOWN_RESULT`, 5xx면 `FAILED`, 4xx면 `REJECTED`, 나머지는 `SUCCESS`로 분류한다. 모든 예외가 실제 Side Effect 결과 불명인 것은 아니므로 Connect 전 실패·Send 전후·Response Read·Client Disconnect를 더 세분화하는 검증이 필요하다.

## 부록 H. Route 게시 운영 절차

1. Route ID·Environment·Host·Method·Path·API Version을 입력한다.
2. Target Service·Server Group·Target Path Rewrite를 입력한다.
3. AuthN/AuthZ·CORS·Header·Rate·Timeout·Retry 정책을 선택한다.
4. Collision Preview와 Path Rewrite Contract를 확인한다.
5. DNS→TCP→TLS→HTTP→Application Probe를 구분해 실행한다.
6. SSRF Allowlist와 Certificate·SNI를 확인한다.
7. Version·Checksum·Request Hash를 고정하고 Approval을 받는다.
8. Candidate를 게시하고 Instance별 ACK/NACK를 확인한다.
9. 일부 NACK이면 해당 Instance를 Traffic에서 제외하고 전체 성공으로 표시하지 않는다.
10. Smoke Transaction과 Attempt Ledger를 확인한다.
11. Error·Latency·Unknown·Downstream 상태를 관찰한다.
12. 문제가 있으면 LKG Version으로 Rollback하고 모든 Instance Checksum을 다시 확인한다.

## 부록 I. 장애별 판정

| 증상 | 우선 확인 | 복구 |
|---|---|---|
| 기동 실패·ACK Route 0 | `allow-empty-routes`, Public Route Query | Route ACK 복구 또는 승인된 Default Deny 기동 |
| Snapshot 갱신 실패 | Provider·DB·Checksum·최근 Candidate | LKG 유지, 원인 수정 후 재갱신 |
| Target 없음 | Registry Status·Maintenance·Draining·Service ID | 정상 Instance 복구, Route Target 검토 |
| 반복 Timeout | Deadline·Retry 중복·Circuit·Downstream Capacity | Retry 축소, Target 격리, 대사 |
| 응답 유실 | Attempt Stage·Downstream Operation 상태 | Blind Retry 금지, Reconcile |
| 일부 Instance NACK | Capability·Artifact Version·Checksum | Traffic 제외, 재적용 또는 LKG Rollback |
| SSRF 의심 | Resolved IP·Redirect·DNS·Target Config | Route 차단, Credential Rotation, Incident 조사 |
| Ledger 저장 실패 | 업무 응답과 Ledger Transaction 경계 | Spool/재처리·Alert, 업무 결과 오염 여부 확인 |

## 부록 J. Gateway 검증 Matrix

- Host·Path·Method·Version 우선순위
- Wildcard Host와 Path Boundary
- Rewrite Parameter·Query 보존
- Authorization Header·Trusted Header Spoof
- AuthN 실패·AuthZ 실패·Reason 누락
- GET Retry와 Mutation Retry 차단
- Request Body Replay·Streaming·Large Upload
- Connect·Send·Response·Read Timeout
- Client Disconnect·Response Write 실패
- Circuit Open·Half-open·Recovery
- Registry 0건·Maintenance·Draining
- Route Refresh 실패와 LKG 유지
- Candidate ACK/NACK·Partial Apply·Rollback
- Multi-instance Route Version Drift
- SSRF CIDR·DNS Rebinding·Redirect
- Ledger DB 장애·중복·Masking

## 부록 K. HMAC Body Hash·Scale-out·Reconciliation

Control Channel Signature의 Canonical 입력에는 Method, Request Target, Content-Type, **Body Hash(SHA-256)**, Caller, Operator, Timestamp, Nonce, Audience, Key ID를 포함한다. Body Hash 계산 전 Canonicalization 규칙과 Empty Body 표현을 고정한다.

Scale-out 시 Route Snapshot Version·Checksum, Nonce Store, ACK/NACK, Attempt Ledger는 인스턴스 로컬 메모리에만 두지 않는다. Instance별 적용 Drift를 조회하고, 응답 유실·일부 적용·Ledger 누락은 Reconciliation으로 Route Version·Runtime Snapshot·DB 원장·실제 Traffic 상태를 대조한다.

---

## 기준 Source와 역할 완결성 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `c2e1680fcf42467d445df97f1a3a0c36dab783ef`
- 문서 표준: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 제품 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 사실 우선순위: 실제 Source·SQL·API·Config·Frontend·Script·Test → Architecture·Specification → 이 매뉴얼

이 매뉴얼의 대상 역할은 다음 흐름을 다른 사람의 구두 설명이나 Source 역분석 없이 수행할 수 있어야 한다.

```text
업무 목적 파악
→ 선행 조건과 권한 준비
→ 실제 기능 위치 탐색
→ 등록·설정·개발·실행
→ 상태와 결과 확인
→ 오류·동시성·부분 실패 판단
→ Retry·Restart·Reprocess·Reconcile·Compensation·Rollback
→ Log·Metric·Trace·Audit·Evidence 확인
→ 정상화와 완료 판정
```

Source에 구현되지 않았거나 현재 Gate가 실패한 기능은 사용 가능한 기능처럼 설명하지 않는다. 해당 단락의 상태를 `미구현`, `미검증`, `실패`, `재확인 필요`로 표시하고 실행 중단 조건과 확인 경로를 함께 제공한다.

---

## 제2부 실무편: Gateway 설치·개발·Control Plane·Data Plane 전수 절차

## 22. Gateway 선택과 책임 경계

Gateway는 공통 Ingress Route·Authentication/Authorization·Header·Rate Limit·Timeout/Retry·Circuit·Audit·Attempt Ledger·배포 ACK가 필요할 때 선택한다. 내부 Service Call만 있고 공통 Ingress 정책이 필요 없으면 선택하지 않는다.

- Data Plane Owner: `cpf-gateway`, Spring Cloud Gateway Server Web MVC.
- Route/Policy Control Plane: ADM Gateway 기능 + Gateway Provider/Apply Ledger.
- Service/Instance 정본: Service Registry.
- 업무 상태 정본: Target Domain.
- Gateway는 ADM/BAT/Actuator/Internal Control API를 외부 Business Route로 노출하지 않는다.

## 23. 설치와 Safety Cap

`cpf.gateway.*` Safety Property는 ADM Runtime Policy가 확대할 수 없는 설치 상한이다.

| 항목 | Default·제약 |
|---|---|
| Route/Policy Refresh | 30s / 15s, 양수 |
| Health Probe/Stale | 30s / 90s |
| Connect/Response/Overall Cap | 10s / 60s / 90s; Overall은 양쪽 이상 |
| Retry Cap | 2, 0~10 |
| Request/Response Body | 각 10 MiB |
| Header Count/Bytes | 100 / 64 KiB |
| Raw Body Capture | false |
| Log Spool | 2 GiB, `./data/gateway-log-spool` |
| Bootstrap Mode | `FAIL_CLOSED` 또는 `LAST_KNOWN_GOOD` |
| Environment/Instance | `local` / `gateway`; 운영에서 명시 |
| Public Target | false |
| Trusted Context Header | Source Allowlist만 |

기동 전 Spool Directory ACL·Disk, Environment/Instance 고유성, Certificate/Trust, DB/Kafka/Service Registry, ACK Route/LKG를 확인한다.

## 24. Request 처리 순서

```text
Business Route Match
→ 승인·ACK Snapshot에서 Binding 결정
→ Authentication
→ Trusted Context 재구성
→ Authorization
→ 위험 호출 Reason 검증
→ Transaction/Attempt Ledger Begin
→ Target 선택·URI Rewrite·SSRF 검증
→ Timeout·Circuit·조건부 Retry
→ Response Stream
→ Sync/Async/Streaming 실제 종료
→ Ledger Completion + AFTER Audit
→ 저장 실패 시 Recovery Spool
```

Route Configuration은 `/actuator/**`, `/internal/**`, `/api/gateway/control/**`를 Business Data Plane에서 제외한다.

## 25. Route Match·Predicate·Rewrite

### Match 입력

- Environment: Header가 없으면 계약 Default를 확인한다.
- Host Pattern: Exact 또는 `*.` Wildcard.
- Path Pattern.
- HTTP Method 또는 `*`.
- API Version 또는 `*`.
- Enabled/ACK/Apply 상태.

여러 Route가 일치하면 Host/Path Specificity와 Route ID 정렬로 결정한다. 의도하지 않은 중복 Route를 Validation에서 차단한다.

### Rewrite

- Target Path Template의 Variable/Wildcard 치환을 Preview한다.
- `//`, Backslash, Control Character, 단일/이중 Decode `.`/`..` Traversal을 거부한다.
- Base Authority와 Path Prefix를 벗어나지 않는지 Canonical URI로 검증한다.
- Query는 Control Character·Fragment·8 KiB 초과를 거부한다.

## 26. Service Registry·Target·Load Balancing

Target 후보 조건:

- Service ID 일치.
- Active, `UP`, Maintenance/Draining 아님.
- Heartbeat가 `staleAfter`보다 최신.
- Gateway Zone 후보가 있으면 Same Zone 우선.
- 가장 낮은 Priority Group.
- Weight 기반 선택.

현재 Cursor는 JVM Local이므로 Instance 간 선택 위치를 공유하지 않는다. 장기 분배는 전체 Gateway Traffic 기준으로 검증한다.

### SSRF

Base URI는 HTTP/HTTPS, Host 필수, UserInfo/Query/Fragment 금지다. DNS로 해석된 모든 Address에 Loopback/Any/Link-local/Multicast를 거부하고 Public Target이 false면 Private/CGNAT/ULA만 허용한다.

다만 QA34는 검증 DNS 조회와 실제 HTTP Client 연결 DNS 조회가 분리돼 Address Pinning이 없음을 지적한다. DNS Rebinding·Mixed A/AAAA·Redirect·Metadata Endpoint Fixture가 통과하기 전 외부 변경 가능한 DNS Target을 허용하지 않는다.

## 27. Authentication·Authorization·Header·HMAC

### Header Trust

Client가 보낸 Authorization을 내부 Trusted Context Map에 복사하지 않는다. 설치 Allowlist에 있는 Context Header만 사용하고 Authentication 결과의 Principal을 Server 측 Attribute로 추가한다.

`X-Transaction-Id`, `X-Channel-Id`, `traceparent`, `X-Api-Version`, Operation Reason 등은 Header별 신뢰 주체와 Validation을 명시한다. Client Header와 Server-generated ID가 충돌하면 Server 규칙을 따른다.

### Authentication/Authorization

Binding에 연결된 Policy ID를 사용한다. 인증 성공 뒤 Route·Principal·Channel·Scope로 Authorization한다. 위험 Route는 `X-Operation-Reason`을 요구한다.

### HMAC Control Channel

Control API는 Key ID, Timestamp/Expiry, Nonce, Audience, Method, Canonical Path/Query, Body SHA-256, Signature를 검증한다. Nonce는 공유 Store에서 중복 차단하고 Clock Skew를 제한한다. Key Rotation은 Active/Previous Window와 Audit를 포함한다.

## 28. Timeout·Retry·Circuit·Bulkhead

| 항목 | 운영 원칙 |
|---|---|
| Connect Timeout | TCP/TLS 연결 예산 |
| Response Timeout | Upstream 응답 예산 |
| Overall Timeout | Queue·Retry 포함 전체 예산 |
| Retry | `idempotent=true` Route만, Cap 이하 |
| Retry Exception | I/O·Timeout 등 명시 목록 |
| Status Retry | 실제 Policy에 정의된 경우만 |
| Circuit | Route/Target 기준 이름·Threshold·Open/Half-open |
| Bulkhead | Thread/Connection/Queue 상한과 거부 상태 |

현재 Handler는 Route ID 기준 Retry/Circuit을 생성한다. Attempt별 Target·URI·Status·Protocol Status·Failure Code/Message·Unknown을 Ledger에 기록한다. 비멱등 POST를 “일시 오류”라는 이유로 Retry하지 않는다.

## 29. Ledger·Completion·Recovery Spool

### Begin

Transaction ID, Trace, Channel, Client Address, Gateway Instance, Route/Version/Expected Version/Key, Server Group, Method, Path, Request Size, 시작 시각을 기록한다.

### Attempt

Attempt ID/No, Target Instance, Host/Port/Scheme, Latency, Status, Protocol Status, Failure Stage, Unknown, 시작/종료를 기록한다.

### Completion

Sync/Async/Streaming 실제 종료 시 Atomic Guard로 한 번만 완료한다.

- 2xx/3xx: `SUCCESS`.
- 4xx: `REJECTED`.
- 5xx: `FAILED`.
- Async Error/Timeout/Client·Stream Exception: `UNKNOWN_RESULT`.
- Response Body가 Safety Cap을 넘으면 I/O 오류와 Unknown/Reconciliation 대상이 될 수 있다.

Ledger/Audit 저장 실패는 요청 Thread에서 성공으로 숨기지 않고 Disk Recovery Spool에 격리한다. Spool Count/Bytes, Disk Cap, Replay Attempt, Poison Record를 ADM KPI와 Log로 확인한다.

## 30. Route·Server Group·Binding Lifecycle

### Server Group

입력: ID/Name/Environment/Service/Endpoint/Target Protocol/Load Balance/Hash Source/Health/Failover/Member Instance·Weight·Priority·Canary·Enabled/Fencing/Reason.

Service Registry Instance를 복제 저장하지 않고 Member로 참조한다. Member 제거 전 In-flight와 Failover Capacity를 확인한다.

### Binding

입력: Binding/Route/Environment/Host/Path/Target Path/Method/API Version/Route Version/Service/Group/Protocol/Timeout/Retry/Idempotent/Gateway/Direct/TLS/Auth/Authz/Header/Rate/Health/Reason.

Lifecycle:

```text
DRAFT
→ Validate/Preview
→ Approval Request
→ APPROVED
→ Publish Event
→ Gateway Instance Apply
→ ACK / NACK / PARTIAL_APPLY
→ ACTIVE
→ Block/Retire/Rollback
```

Service Registry 등록만으로 외부 공개하지 않는다. Active Binding과 Gateway ACK가 모두 필요하다.

## 31. Publish·ACK/NACK·LKG·Rollback

1. Candidate Snapshot의 모든 Binding과 Policy Reference를 검증한다.
2. Checksum·Version·Expected Version·Approval을 고정한다.
3. Publish Event를 생성한다.
4. Gateway Instance별 ACK/NACK와 Applied Version을 수집한다.
5. Quorum/All 정책을 판정한다.
6. Partial이면 신규 Traffic 범위를 제한하고 실패 Instance를 Reconcile한다.
7. Rollback은 Last Known Good Version과 Checksum으로 수행한다.
8. Rollback 뒤 모든 Instance Applied Version·Route Probe·Drift 0을 확인한다.

Snapshot Refresh 실패 시 현재 Last Normal Snapshot을 유지한다. `FAIL_CLOSED` 기동에서 ACK Route 0건이면 기동을 중단하거나 명시적으로 Empty Route 허용을 결정한다. `LAST_KNOWN_GOOD`는 LKG Artifact·Checksum·Age·Environment 일치를 확인한다.

## 32. ADM Gateway 화면

### 공통

Capability Available/Source Instance/Generated At, Environment/Service/Route Filter, Auto Refresh `LIVE SSE`/`POLL 15s`/`PAUSED`를 확인한다.

KPI: TPS 60s, Success/Error, P95/P99, Drift, Open Circuit, Certificate ≤30d, Spool Backlog, Connection Test Failure.

### Group Tab

Member Diff를 확인하고 저장한다. Rendezvous Hash는 Hash Key Source가 필요하다. Canary Percent와 Priority/Weight를 함께 검토한다.

### Binding Tab

Default Deny Draft를 저장하고 Apply Preview를 확인한다. Timeout Cap과 Retry/Idempotent 조합, Gateway/Direct 허용, 모든 Security Policy Reference를 검증한다.

### Apply/Connection Test

Instance Expected/Applied/Status/Last Seen을 확인한다. Target Direct와 Gateway E2E Test를 비동기 Operation으로 실행하고 Test Type·Reason·Failure Stage·Trace를 기록한다.

### Security/Transaction/Log Policy

Default Deny, Retry Safety, Admin/Internal 제외, Approval/CAS/Audit를 확인한다. Transaction은 Attempt/Completion/Unknown을 대사하고 Log Policy는 Gateway ACK/NACK와 Capture 상한을 확인한다.

## 33. Scale-out·Drift·Reconciliation

- Instance ID와 Zone을 고유하게 설정한다.
- Snapshot Version·Checksum·Policy Version을 Instance별로 보고한다.
- Route 선택 Cursor가 JVM Local임을 감안해 전체 분배를 측정한다.
- Drift는 Desired Version과 Applied Version, Route Count/Checksum 차이를 포함한다.
- Reconcile은 DB/Control Plane 값만 덮어쓰지 않고 Gateway Runtime Snapshot을 다시 읽는다.
- Stale Instance는 Traffic에서 제외하고 재기동/재적용 뒤 편입한다.

## 34. Probe·Health·Capacity

- Process/Readiness.
- Route Snapshot Loaded/LKG Age.
- Service Registry/DB/Policy Provider.
- Target Probe와 Circuit.
- Thread/Connection/Queue/Heap/GC.
- Request/Response Body Size Reject.
- Spool Disk/Backlog/Poison.
- Certificate Expiry.
- TPS/Error/P95/P99와 Retry 증폭.

## 35. Gateway 장애 Runbook

### Route 404/Default Deny

Host/Path/Method/API Version/Environment, Enabled/ACK/Applied Snapshot을 확인한다. 임의 Wildcard Route를 추가하지 않는다.

### Target 없음

Service Registry Fresh Heartbeat, UP/Active, Drain/Maintenance, Priority/Zone, Base URL/DNS/CIDR를 확인한다.

### Timeout/5xx

Attempt별 Target/Latency/Failure Stage, Circuit, Retry Count, Target Log/Trace를 확인한다. 비멱등 Retry 금지.

### Client Disconnect/Streaming

Completion의 `CLIENT_OR_STREAM`, Unknown Flag, Response Bytes, Target Side Effect를 대사한다.

### Spool Backlog

Disk/Permission/Cap, Ledger/Audit DB, Poison Record를 확인한다. Spool File을 직접 삭제하지 않고 Replay/격리 Evidence를 남긴다.

### Partial Apply/Drift

Expected/Applied Version, ACK/NACK Error, Instance Health를 확인한다. 실패 Instance 격리→Reapply/Reconcile 또는 LKG Rollback.

### SSRF/DNS

Target을 우선 차단하고 Registry/Binding/Approval/Audit, DNS Answer, 실제 Connection Address, Proxy/Redirect를 보전한다. Public Target 허용을 임시 해제해 우회하지 않는다.

## 36. Gateway Test Matrix

| Test | 정상 판정 |
|---|---|
| Match 충돌 | Specific Route 한 개, Ambiguous Validation 차단 |
| Rewrite Traversal | 단일/이중 Encoding 모두 거부 |
| Header Spoof | Client Authorization/Principal 내부 Context 미주입 |
| Retry | 멱등 Route만, Attempt Ledger 일치 |
| Response Cap | 초과 응답 중단·Unknown/Reconcile |
| Async/Streaming | Completion 1회 |
| Ledger DB 실패 | Recovery Spool 기록·Replay |
| DNS Rebinding | 검증/연결 Identity 변경 거부 |
| Multi-instance Apply | ACK/NACK/Drift 정확 |
| Rollback | LKG Version/Checksum 전 Instance 일치 |
| Client Disconnect | Target Side Effect 대사 |
| 3DB | Route/Apply/Ledger Query 실제 실행 |

## 37. Gateway EDU

- EDU-GW-01 Group/Member→Binding Draft→Validation→Approval→Publish→ACK.
- EDU-GW-02 Host/Path Rewrite와 Traversal Negative Test.
- EDU-GW-03 Target Drain·Zone·Priority·Weight·Failover.
- EDU-GW-04 Timeout·Retry·Circuit·Unknown 대사.
- EDU-GW-05 Partial Apply→Reconcile→LKG Rollback.
- EDU-GW-06 Ledger/Audit DB 장애→Spool→Replay.
- EDU-GW-07 DNS Rebinding·Public/Private CIDR Test.

## 38. Gateway 완료 Checklist

- [ ] 설치 Safety Cap·Secret·Certificate·Spool
- [ ] Route Match/Predicate/Rewrite
- [ ] Service Registry/Target/LB/Failover
- [ ] Authentication/Authorization/Header/HMAC
- [ ] SSRF/TLS/DNS/Redirect
- [ ] Timeout/Retry/Circuit/Bulkhead
- [ ] Ledger/Attempt/Completion/Unknown
- [ ] Audit/Ledger Recovery Spool
- [ ] Group/Binding Version/Checksum/Approval
- [ ] Publish/ACK/NACK/Partial/LKG/Rollback
- [ ] Scale-out/Drift/Reconcile
- [ ] Probe/Health/Capacity/Runbook
- [ ] Browser/3DB/Multi-instance/Fault Evidence

---
## 부록 L. Gateway Source·SQL 진입점

| 기능 | 기준 Source |
|---|---|
| Build/Dependency | `cpf-gateway/build.gradle` |
| Safety Cap | `cpf-gateway/src/main/java/com/cpf/gateway/config/CpfGatewaySafetyProperties.java` |
| SCG Route | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryRouteConfiguration.java` |
| Data Plane Handler | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java` |
| Target Resolver | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java` |
| Response Completion | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerCompletionFilter.java` |
| Ledger Recovery | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerRecoverySpool.java` |
| Audit Recovery | `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayAuditRecoverySpool.java` |
| Route Snapshot | `cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayRouteSnapshot.java` |
| Path Rewrite | `cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayPathRewriter.java` |
| Gateway Core Contract | `cpf-core/src/main/java/com/cpf/core/api/gateway` |
| Service Registry Contract | `cpf-core/src/main/java/com/cpf/core/api/servicecall` |
| ADM Gateway UI | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue` |
| ADM Route Registry | `cpf-admin/frontend/src/app/routes.ts` |
| Recovery Test | `cpf-gateway/src/test/java/com/cpf/gateway/scg/CpfGatewayRecoverySpoolTest.java` |
| Target Security Test | `cpf-gateway/src/test/java/com/cpf/gateway/scg/CpfScgTargetResolverTest.java` |
| Canonical DB | `cpf-tools/db/canonical/platform-schema.json` |
| Gateway Runtime SQL | `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/runtime/gat` |
