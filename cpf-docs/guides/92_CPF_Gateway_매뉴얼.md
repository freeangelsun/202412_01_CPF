# CPF Gateway 매뉴얼 — API 등록·보안·게시·적용·정상화

> **주 독자**: API 개발자, Gateway 설정 담당자, 보안 담당자, 승인자, 게시 담당자, 운영 담당자
> **완료 결과**: CPF Gateway를 선택·설치하고 Route·보안·복원력 정책을 등록·검증·승인·게시하며, Target별 적용 결과·Drift·응답 유실·부분 적용·Rollback을 운영한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
- Owner Module: `cpf-gateway`
- 최상위 요구 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 활성 개발 요구: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- 실제 Controller·Service·Config·Frontend·DB·Test가 문서보다 우선한다.
- 기준 Commit에서 Runtime·Browser·다중 인스턴스·Fault Scenario를 직접 실행하지 않았으므로 해당 결과는 `미검증`이다.

## 1. CPF Gateway를 선택하는 기준

다음 조건이면 Gateway 사용을 검토한다.

- 여러 업무 API에 공통 인증·인가·라우팅·제한 정책이 필요하다.
- Route 변경을 Draft·Validation·Approval·Publish 절차로 통제해야 한다.
- 다중 Target의 적용 Version·Checksum·ACK/NACK·Drift를 추적해야 한다.
- Timeout·Retry·Circuit Breaker·Bulkhead를 API 진입점에서 공통 적용해야 한다.
- HMAC·Audience·Nonce·Body Hash·SSRF·TLS 정책을 공통으로 검증해야 한다.

다음 경우에는 불필요한 Hop과 운영 복잡도를 검토한다.

- 단일 내부 API만 존재한다.
- 업무 서비스가 자체 인증·Route를 소유하고 공통 게시 절차가 필요하지 않다.
- Gateway가 업무 원장이나 업무 승인 규칙을 대신 소유하게 되는 설계다.

## 2. Ownership과 의존 방향

### 2.1 Gateway가 소유하는 것

```text
Route·Predicate·Filter·Rewrite
Target·Discovery·Load Balancing 정책
Security·TLS·SSRF 정책 참조
Timeout·Retry·Circuit Breaker·Bulkhead 정책
Route Version·Checksum·Validation 결과
Approval·Publish Operation
Target ACK·NACK·Partial Apply
Attempt Ledger·UNKNOWN_RESULT 대사 정보
Last Known Good(LKG)
Probe·Health·Drift·Reconciliation
Gateway Audit
```

### 2.2 Gateway가 소유하지 않는 것

```text
업무 엔터티·업무 상태·업무 원장
업무 승인·취소·보상 규칙
BZA 조직·사용자·권한 정본
Batch Job Execution 원장
외부기관 실제 처리 결과 정본
```

### 2.3 의존 방향

```text
Channel / Client
        ↓
CPF Gateway Public Endpoint
        ↓
Route·Security·Resilience Policy
        ↓
Owner Service Public API
        ↓
Owner Domain·DB·External Provider
```

Gateway가 Owner DB를 직접 수정하거나 내부 Repository를 호출하지 않는다.

## 3. 설치·기동 전 점검

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
if(-not(Test-Path -LiteralPath (Join-Path $repo 'cpf-gateway'))){throw 'cpf-gateway 모듈이 없습니다.'}
git -C $repo rev-parse HEAD
git -C $repo status --short
& (Join-Path $repo 'gradlew.bat') :cpf-gateway:tasks --all
```

확인 항목:

1. Gateway Artifact와 Commit·Hash를 기록한다.
2. DB·Config Store·Secret·Certificate 의존성을 Source에서 확인한다.
3. Target Service의 OpenAPI·Health·Readiness·Permission을 확보한다.
4. 외부 노출 Host·TLS·Firewall·Proxy·DNS를 확정한다.
5. 운영자·승인자·보안 담당자의 Permission을 분리한다.
6. LKG Route Version과 Rollback 절차를 준비한다.

## 4. Route 등록 데이터

| 필드 | 필수 | 의미 | 검증 |
|---|:---:|---|---|
| `routeId` | 예 | Route 고유 식별자 | 중복 없음 |
| Version | 예 | 낙관적 변경 Version | Expected Version 일치 |
| Path | 예 | 외부 요청 경로 | 충돌·Shadowing 검사 |
| Method | 예 | 허용 HTTP Method | OpenAPI와 일치 |
| Predicate | 선택 | Header·Host·Query 등 조건 | 순서·경계값 검사 |
| Filter | 선택 | 요청·응답 처리 | 금지 Header·Payload 검사 |
| Rewrite | 선택 | Path·Header 변환 | 원본·변환 결과 비교 |
| Target | 예 | 대상 Service·URI·Service ID | Allowlist·TLS·Health |
| Discovery | 선택 | 대상 탐색 방식 | Stale Instance·TTL 검사 |
| Load Balancing | 선택 | Round Robin·Weight 등 | 동일 요청 중복 방지 |
| Timeout | 예 | Connect·Read·Write·Total | 전체 Deadline 안에 포함 |
| Retry | 선택 | 재시도 조건·횟수·Backoff | 멱등성·Attempt Ledger |
| Circuit Breaker | 선택 | 실패율·대기·Probe | Open/Half-open 동작 |
| Bulkhead | 선택 | Concurrency·Queue | Capacity 기준 |
| Security Policy | 예 | AuthN·AuthZ·HMAC·TLS | Policy Version 확인 |
| Idempotency Policy | 변경 API | Key·Hash·Ledger | 중복·충돌 시험 |
| Owner | 예 | 업무·Route 책임자 | 연락·승인 체계 |
| Reason | 변경 시 | 등록·변경 사유 | Audit 저장 |
| Approval | 위험 변경 | 승인 ID·Version·만료 | 요청자·승인자 분리 |

## 5. Predicate·Filter·Rewrite

### 5.1 Predicate

- Path·Method를 기본 조건으로 사용한다.
- Host·Header·Query Predicate는 우회 경로가 생기지 않는지 확인한다.
- 대소문자·Encoding·Trailing Slash·중복 Query·빈 Header 경계를 시험한다.
- Predicate 순서가 중복 Route의 우선순위를 바꾸는지 검사한다.

### 5.2 Filter

- 인증·추적 Header를 호출자 입력 그대로 신뢰하지 않는다.
- Hop-by-hop Header와 내부 전용 Header를 제거한다.
- Payload Logging·Compression·Size Limit가 개인정보와 메모리에 미치는 영향을 확인한다.
- Response Header·CORS·Cache 정책은 Route별로 명시한다.

### 5.3 Rewrite

Rewrite 전·후 값을 Validation Evidence에 남긴다. Path Variable·Encoding·Query 보존이 OpenAPI 계약과 일치해야 한다.

## 6. Target·Discovery·Load Balancing

### 6.1 Static Target

- Scheme·Host·Port Allowlist를 사용한다.
- 운영 Target에 Localhost·Metadata Address·임의 Private Range가 포함되지 않도록 한다.
- DNS 결과가 변경될 때 Rebinding 위험을 검사한다.

### 6.2 Service Discovery

- Instance ID·Version·Zone·Readiness·TTL을 사용한다.
- 만료되거나 Readiness가 닫힌 Instance를 신규 요청에서 제외한다.
- 신규 Instance는 Active Route Version·Checksum 적용 후 Traffic을 받는다.

### 6.3 Load Balancing

- Weight·Zone·Session Affinity가 필요한 업무인지 결정한다.
- 재시도 시 같은 Target과 다른 Target 선택 규칙을 명시한다.
- 변경 요청은 Target 전환으로 중복 처리되지 않도록 Idempotency Key를 유지한다.
- Target별 실패율과 전체 실패율을 함께 관측한다.

## 7. Authentication·Authorization

### 7.1 인증 주체

- 사용자 Token과 Service Identity를 구분한다.
- Issuer·Audience·Subject·Client ID·Scope·Role을 검증한다.
- Token Forwarding 여부와 Downstream Audience를 Route별로 정의한다.
- Clock Skew·Key Rotation·Expired Token·Revoked Session을 시험한다.

### 7.2 인가

- Route 접근 Permission과 Owner Service Command Permission을 분리한다.
- Gateway 통과가 업무 권한 승인을 의미하지 않는다.
- Data Scope·Masking은 Owner Service가 최종 검증한다.
- 위험 조치는 Reason·Approval·Expected Version을 요구한다.

## 8. HMAC·Audience·Body Hash·Nonce

HMAC 사용 시 최소 입력:

```text
keyId / algorithm / timestamp / nonce
method / canonical path / canonical query
selected headers / body hash
signature / allowed clock skew
```

검증 순서:

1. Key ID와 활성 Version을 확인한다.
2. Timestamp 허용 범위를 확인한다.
3. Nonce 중복을 차단한다.
4. Canonical Request와 Body Hash를 계산한다.
5. Constant-time 비교로 Signature를 확인한다.
6. 실패 원인을 Secret 없이 Audit에 기록한다.

응답 유실 후 같은 Nonce를 무조건 재사용하지 않는다. Idempotency 계약과 Provider 규칙을 함께 확인한다.

## 9. SSRF·TLS

### 9.1 SSRF

- Scheme·Host·Port Allowlist
- DNS Rebinding 방어
- Redirect 제한과 재검증
- Link-local·Metadata·Loopback 접근 제한
- User Input으로 Target URI 직접 조립 금지
- Proxy 환경의 실제 Destination 확인

### 9.2 TLS

- Certificate Chain·SAN·Expiry
- Protocol·Cipher
- Hostname Verification
- mTLS Client Identity
- Trust Store·Key Store Version
- Certificate Rotation과 Rollback

TLS 검증을 우회한 시험 설정을 운영 Profile에 사용하지 않는다.

## 10. Timeout Budget

전체 Deadline을 다음처럼 나눈다.

```text
Client Deadline
  ├─ Gateway Queue
  ├─ Authentication·Policy
  ├─ Connection
  ├─ Target Processing
  ├─ Response Transfer
  └─ Safety Margin
```

Gateway Timeout이 Target Commit 이후 발생할 수 있으므로 변경 요청은 `UNKNOWN_RESULT` 가능성을 전제로 한다.

## 11. Retry·Circuit Breaker·Bulkhead

### 11.1 Retry

- 조회와 변경 요청을 구분한다.
- 변경 요청은 Idempotency Key·Request Hash·Attempt Ledger가 있어야 한다.
- HTTP Status만으로 재시도하지 않고 Failure Class를 사용한다.
- 최대 시도·Backoff·Jitter·전체 Deadline을 함께 계산한다.

### 11.2 Circuit Breaker

- Open 조건·최소 호출 수·대기 시간·Half-open Probe를 정의한다.
- Circuit Open을 성공 응답으로 변환하지 않는다.
- Fallback이 실제 업무 결과를 오인하게 하지 않는다.

### 11.3 Bulkhead

- Route·Target별 동시성·Queue를 분리한다.
- Queue 대기가 Client Deadline을 초과하지 않도록 한다.
- 한 Target 장애가 전체 Route를 고갈시키지 않게 한다.

## 12. Idempotency·Attempt Ledger·UNKNOWN_RESULT

### 12.1 Attempt Ledger 필수 정보

```text
requestId / traceId / operationId / idempotencyKey
requestHash / routeId / routeVersion / target
attempt / sentAt / responseAt / timeout
statusCode / failureClass / providerReceipt
resultState / reconcileKey
```

### 12.2 결과 불명 처리

Target에 요청을 보낸 뒤 응답을 받지 못한 경우:

1. 신규 업무 요청을 만들지 않는다.
2. 같은 Idempotency Key·Request Hash로 Operation을 조회한다.
3. Attempt Ledger와 Target Receipt를 확인한다.
4. Owner Service의 업무 원장을 조회한다.
5. 성공이면 결과를 확정하고, 미처리이면 허용된 재시도를 수행한다.
6. 결과가 끝내 확정되지 않으면 운영 확정·보상·대사 절차로 이동한다.

## 13. Validation·Version·Checksum

게시 전 Validation:

- Route ID·Version 중복
- Path·Method 충돌
- Predicate Shadowing
- Rewrite 결과
- Target Allowlist·TLS·Health
- Timeout 합계
- Retry 멱등성
- Security Policy Version
- Secret·Certificate Reference
- Config Schema
- Stable Checksum

Expected Version이 다르면 현재 Draft를 다시 조회하고 변경 내용을 병합한다. 다른 운영자의 변경을 덮어쓰지 않는다.

## 14. 승인·게시 상태

```mermaid
stateDiagram-v2
  [*] --> DRAFT
  DRAFT --> VALIDATED
  VALIDATED --> WAITING_APPROVAL
  WAITING_APPROVAL --> PUBLISHING
  PUBLISHING --> ACTIVE
  PUBLISHING --> PARTIAL_APPLY
  PUBLISHING --> FAILED
  PARTIAL_APPLY --> RECONCILING
  RECONCILING --> ACTIVE
  RECONCILING --> ROLLED_BACK
  ACTIVE --> RETIRED
```

상태별 필수 기록:

| 상태 | 필수 기록 |
|---|---|
| DRAFT | 작성자·Reason·Expected Version |
| VALIDATED | Validation 결과·Checksum |
| WAITING_APPROVAL | Approval ID·정책 Version·만료 |
| PUBLISHING | Target 목록·시작 시각 |
| ACTIVE | Target ACK·Probe·Drift 0 |
| PARTIAL_APPLY | 성공·실패 Target·NACK 원인 |
| RECONCILING | 재적용 또는 LKG 결정 |
| ROLLED_BACK | 복귀 Version·Checksum·승인 |

## 15. ACK·NACK·Partial Apply

- Target별 Applied Version·Checksum·시각을 저장한다.
- 일부 Target만 성공하면 Traffic 확대와 신규 게시를 중지한다.
- 성공 Target을 다시 게시하여 중복 Side Effect를 만들지 않는다.
- NACK 원인이 Config·Secret·Certificate·Runtime Version 중 무엇인지 분류한다.
- 실패 Target만 Reconcile하거나 전체 Target을 LKG로 되돌린다.

## 16. LKG·Rollback

Rollback 전 확인:

```text
LKG Version·Checksum
DB/API/Message Compatibility
Target 수와 현재 적용 상태
진행 중 Request·Attempt
Secret·Certificate 호환
승인 ID·Reason
```

Rollback 후 판정:

- 모든 Target ACK
- Active Version·Checksum 일치
- Synthetic Probe 성공
- Drift 0
- 오류율·지연 정상 범위
- Owner 업무 요청·Audit 대사

DB나 외부 계약이 이전 Route와 호환되지 않으면 무조건 Rollback하지 않고 Forward Fix를 선택한다.

## 17. Scale-out·Drift·Reconciliation

### 17.1 Scale-out

신규 Instance는 다음 조건을 만족한 후 Readiness를 연다.

- 지원 Runtime Version
- Active Route Version·Checksum 적용
- Secret·Certificate Version 일치
- Target Connectivity·Synthetic Probe 성공

### 17.2 Drift

Drift 예:

- Target의 Route Version 불일치
- Checksum 불일치
- Secret·Certificate Version 불일치
- Predicate·Filter 순서 불일치
- Runtime Agent 미응답

### 17.3 Reconciliation

1. Desired State와 Target Actual State를 수집한다.
2. 차이를 Target별로 분류한다.
3. 진행 중 Publish Operation과 충돌하는지 확인한다.
4. 실패 Target만 재적용한다.
5. ACK와 Probe를 다시 확인한다.
6. Drift 0을 기록한다.

## 18. Probe·Health

Liveness만으로 Route를 판정하지 않는다.

Synthetic Probe는 다음을 포함한다.

- DNS·TLS
- Authentication·Authorization
- Predicate·Rewrite
- Target Selection
- Timeout Budget
- Response Schema
- Trace·Audit 상관관계

변경 API Probe는 실제 업무 Side Effect를 만들지 않는 전용 계약 또는 승인된 Test Data를 사용한다.

## 19. ADM 운영 연계

ADM에서 다음 정보를 조회·조치할 수 있어야 한다.

```text
Route 검색·상세
Draft·Published Version·Checksum
Validation 결과
Approval·Publish Operation
Target ACK·NACK
Attempt Ledger·UNKNOWN_RESULT
Drift·Reconciliation
Synthetic Probe
LKG·Rollback
Audit
```

위험 조치는 Permission·Data Scope·Reason·Approval·Expected Version을 확인한다.

## 20. 실제 ADM Gateway Route 9개

아래 9개 Route는 기준 Commit의 `cpf-admin/frontend/src/generated/adm-route-operation-contract.ts`에서 `gateway-*` Key를 전수 대조한 정적 진입 기준이다. Browser Runtime을 실행하지 않았으므로 Menu 노출·Component Rendering·Permission 문자열·Button 활성 조건은 `미검증`이다. 실제 배포 전에는 ADM Router·Generated Client·Gateway Backend Permission을 전수 대조한다.

| Route·화면 | 역할·권한 범주 | 검색·기본값 | 주요 Column·상세 | Button·활성 조건 | 완료 판정 |
|---|---|---|---|---|---|
| `/gateway-dashboard`<br>**Gateway 대시보드** | Gateway 조회자·운영자·보안 담당자·승인자 | 환경·기간·서버 Group·Route | Health·오류율·지연·Circuit·Partial Apply·Drift | Server·Route·Transaction·Apply 상세 이동; 변경 조치는 Reason·Approval·Expected Version 확인 | 긴급 오류·부분 적용 담당자와 정상화 계획 지정 |
| `/gateway-servers`<br>**Gateway 연동 서버** | Gateway 조회자·운영자·보안 담당자·승인자 | Server ID·환경·상태 | Endpoint·TLS·DNS·Health·Weight·최근 오류 | 등록·중지·연결시험·Certificate 교체; 변경 조치는 Reason·Approval·Expected Version 확인 | 허용 Endpoint만 활성, TLS·DNS·Health 정상 |
| `/gateway-groups`<br>**Gateway 서버 Group** | Gateway 조회자·운영자·보안 담당자·승인자 | Group ID·환경·상태 | Member·Weight·LB·최소 정상 수·Failover | 구성·Weight 변경·비정상 제외·복귀; 변경 조치는 Reason·Approval·Expected Version 확인 | 정상 Member만 수신, Weight·Failover 의도 일치 |
| `/gateway-routes`<br>**Gateway Route·Routing** | Gateway 조회자·운영자·보안 담당자·승인자 | Route ID·Host·Path·Method·Version | Predicate·Rewrite·Target·Priority·Timeout·Approval | Draft·Validate·Approval·Publish·Disable; 변경 조치는 Reason·Approval·Expected Version 확인 | 충돌 0, Probe가 목표 Target·Rewrite와 일치 |
| `/gateway-security`<br>**Gateway 보안·제한** | Gateway 조회자·운영자·보안 담당자·승인자 | Route·Client·정책 Version | Audience·Permission·HMAC·Body Hash·Nonce·Allowlist·TLS | Validate·Approval·Apply·Key Rotate·Block; 변경 조치는 Reason·Approval·Expected Version 확인 | 인증 실패 분류, Replay·SSRF·내부망 우회 차단 |
| `/gateway-health`<br>**Gateway Health·연결시험** | Gateway 조회자·운영자·보안 담당자·승인자 | Server·Group·Route·환경 | DNS·TCP·TLS·Probe·지연·최근 실패 | Probe·재확인·Incident 연결; 변경 조치는 Reason·Approval·Expected Version 확인 | 직접 Target과 경유 결과 일치, 실패 구간 식별 |
| `/gateway-transactions`<br>**Gateway 거래 조회** | Gateway 조회자·운영자·보안 담당자·승인자 | Request ID·Trace ID·Route ID·상태·기간 | Target·Attempt·Circuit·전체 지연·응답 코드·Masking | 거래·Log·Target Trace 이동; 변경 조치는 Reason·Approval·Expected Version 확인 | 같은 Trace로 Gateway와 Target 연결, 원문 노출 0 |
| `/gateway-log-policies`<br>**Gateway 로그 정책** | Gateway 조회자·운영자·보안 담당자·승인자 | Route·데이터 등급·정책 Version | Header/Body 수집·Masking·Sampling·보존·반출 | Preview·Approval·Apply·Rollback; 변경 조치는 Reason·Approval·Expected Version 확인 | 민감정보 원문 0, Instance 정책 Checksum 일치 |
| `/gateway-apply-status`<br>**Gateway 적용 상태·이력** | Gateway 조회자·운영자·보안 담당자·승인자 | Bundle Version·Checksum·환경 | Instance ACK/NACK·현재 Version·Drift·LKG·Attempt | 실패 Instance Reconcile·재적용·LKG; 변경 조치는 Reason·Approval·Expected Version 확인 | 활성 Instance 승인 Version/Checksum 일치 또는 격리 |

응답 유실 시 같은 Publish·Reconcile·Rollback Button을 반복하지 않고 Operation ID와 Attempt Ledger로 기존 결과를 조회한다. 부분 적용은 성공 Instance를 유지하고 NACK·미응답 Instance만 대사한다.

## 21. 화면 사용 표준

실제 Route·Component·Permission은 최신 Frontend Source와 Generated Client를 전수 대조한다. Source에 없는 화면 이름이나 Button을 문서에서 만들지 않는다.

각 화면은 다음 항목을 기록해야 한다.

| 항목 | 내용 |
|---|---|
| 메뉴·Route | 실제 Frontend Route |
| Permission | 화면·조회·변경·승인·Rollback 분리 |
| 검색 Field | Route ID·상태·Owner·Version·Target |
| 기본값 | 기간·상태·Page Size |
| Column | Version·Status·Checksum·Target 결과 |
| 상세 Field | Predicate·Filter·Target·Policy·Audit |
| Button | Validate·Approval·Publish·Reconcile·Rollback |
| 활성 조건 | 상태·Permission·Expected Version |
| 입력 | Reason·Approval·대상·Version |
| 응답 유실 | Operation 조회·대사 절차 |
| 부분 적용 | Target별 결과·재처리 |

기준 Commit의 실제 화면 Route·Button 전수 실행 결과는 `미검증`이다. 이 항목은 `GATEWAY-UI-001` 개발·검수 요청으로 남긴다.

## 22. 장애 Runbook

| 장애 | 최초 확인 | 조치 | 종료 판정 |
|---|---|---|---|
| Target Down | DNS·TLS·Readiness·Pool | Traffic 제외·대체 Target | Probe 성공·오류율 정상 |
| Timeout 증가 | Deadline·Queue·DB·Backlog | Capacity·Bulkhead·원인 제거 | P95/P99·업무 대사 |
| Auth 실패 | Issuer·Audience·Key·Clock | Key·Config Rotation/Rollback | 인증·권한 Probe |
| SSRF 차단 | Target·DNS·Redirect | 정책·Target 수정, 우회 금지 | Allowlist·Probe |
| Partial Apply | ACK/NACK·Checksum | Reconcile 또는 LKG | Drift 0 |
| UNKNOWN_RESULT | Attempt·Receipt·Owner 원장 | 대사·재시도·운영 확정 | 업무 결과 확정 |
| Circuit Open 고착 | 실패율·Probe·Clock | 원인 제거·Half-open Probe | 정상 요청 성공 |

## 23. Test Matrix

```text
Route Conflict·Predicate Shadowing·Rewrite
Discovery TTL·Stale Instance·Load Balancing
AuthN·AuthZ·Audience·Key Rotation
HMAC·Nonce·Timestamp·Body Hash
SSRF·DNS Rebinding·Redirect
TLS·mTLS·Certificate Rotation
Timeout·Retry·Circuit Breaker·Bulkhead
Idempotency·Request Hash·Attempt Ledger
Response Loss·UNKNOWN_RESULT·Reconciliation
Validation·Version Conflict·Checksum
Approval Expiry·Requester/Approver Separation
Publish ACK·NACK·Partial Apply
LKG·Rollback
Scale-out·Drift
Synthetic Probe
Browser Permission·Audit
```

## 24. EDU — Route 게시와 부분 적용 정상화

1. Test Owner API의 OpenAPI와 Health를 준비한다.
2. Route Draft를 등록한다.
3. Predicate·Rewrite·Target·Security·Timeout을 입력한다.
4. Validation과 Checksum을 확인한다.
5. 승인 요청 후 승인자 분리를 확인한다.
6. Target 2개 중 하나가 NACK하도록 Fault를 주입한다.
7. `PARTIAL_APPLY`와 Target별 결과를 확인한다.
8. 실패 Target만 Reconcile한다.
9. Timeout 후 Attempt Ledger와 Owner 원장을 대사한다.
10. LKG Rollback을 실행하고 Drift 0·Probe·Audit를 확인한다.

직접 실행한 명령·환경·Exit Code·Operation ID·Evidence Hash를 기록한다.

### 24.1 Gateway EDU 14개 선택표

기준 기능 카탈로그에는 다음 Gateway EDU가 정의돼 있다. 기능 목록과 Handler 존재만으로 실행 성공을 의미하지 않으며, `cpf.reference.features.gateway.enabled` 조건과 실제 Consumer·DB·Fault 결과를 함께 확인한다.

| EDU | 확인 기능 | 역할 | 활성 조건 | 검증 상태 |
|---|---|---|---|---|
| `EDU-GW-01` | 서버 Group·Health·Load Balancing | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-02` | Route·Predicate·Rewrite | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-03` | Authentication·Authorization·TLS·HMAC·Nonce | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-04` | Timeout·Retry·Circuit Breaker·Bulkhead | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-05` | Draft·Validation·Approval·Publish·Partial Apply | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-06` | Attempt Ledger·UNKNOWN_RESULT·LKG | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-07` | Discovery·Failover·복귀 | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-08` | SSRF Allowlist·DNS Rebinding 차단 | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-09` | Header·Path·Request·Response 변환 | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-10` | Body Size·Content-Type·Schema Validation | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-11` | Command Idempotency·응답 유실 | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-12` | 다중 Instance Drift·Reconciliation | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-13` | Canary·Weighted Routing·Rollback | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |
| `EDU-GW-14` | 관측·Masking·Audit | `CPF_GATEWAY_OPERATOR` | `cpf.reference.features.gateway.enabled` | Runtime 미검증 |

## 25. 현재 상태와 Owner 작업 요청

| ID | 항목 | 판정 | 요청 |
|---|---|---|---|
| `GATEWAY-UI-001` | 실제 Route·Component·Permission·Button 전수표 | 미검증 | Frontend·Generated Client·Backend Permission 대조 |
| `GATEWAY-RUN-001` | Publish ACK/NACK·Partial Apply Runtime | 미검증 | 다중 Target Fault Scenario 실행 |
| `GATEWAY-LEDGER-001` | Attempt Ledger·UNKNOWN_RESULT 전체 경로 | 재확인 필요 | Owner Consumer·DB·Reconcile Test 연결 |
| `GATEWAY-SCALE-001` | Scale-out·Drift·LKG | 미검증 | 다중 Instance Evidence 생성 |

## 26. 완료 점검표

- [ ] Gateway 선택 이유와 비선택 이유가 기록됐다.
- [ ] Route·Predicate·Filter·Rewrite·Target이 OpenAPI와 일치한다.
- [ ] Discovery·Load Balancing·Timeout·Retry 정책이 업무 의미와 맞는다.
- [ ] AuthN·AuthZ·HMAC·SSRF·TLS를 시험했다.
- [ ] Idempotency·Attempt Ledger·UNKNOWN_RESULT 대사가 가능하다.
- [ ] Validation·Version·Checksum·Approval·Publish가 연결된다.
- [ ] Target별 ACK·NACK·Partial Apply·Reconciliation을 확인했다.
- [ ] LKG·Rollback·Scale-out·Drift·Probe를 확인했다.
- [ ] ADM·Browser·Audit가 같은 Route Version을 가리킨다.
- [ ] 직접 실행하지 않은 항목은 `미검증`으로 남았다.
