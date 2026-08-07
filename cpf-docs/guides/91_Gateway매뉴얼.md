# CPF Gateway 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 문서 콘텐츠 기준 Commit: `64049044956924032360fa80be83b5e37c64f828` (`08_03`)
> 기준일: `2026-08-07 Asia/Seoul`
> 기준 Commit: **현재 `master` 구현 기준 문서**. Product Surface·Starter·Tool·EDU 식별자는 아래 기준 Commit의 Source 정본과 대조한다.

| 항목 | 내용 |
|---|---|
| 주 독자 | API 개발자·보안담당자·Gateway 운영자·외부 연계 운영자 |
| 이 문서로 완료할 일 | Gateway를 설치하고 Route·Predicate·Filter·Rewrite·Target·Discovery·보안·Timeout·Retry·Attempt Ledger·Validate·Approve·Publish·ACK/NACK·LKG·Scale-out·Drift·Reconciliation을 운영한다. |
| 완료 판정 | 독자가 다른 문서나 Source 역분석 없이 정상 흐름, 오류, 부분 실패, 복구, 감사, 운영 인계를 끝낼 수 있어야 한다. |
| 상태 표현 | 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요만 사용한다. |
| 정합성 원칙 | 실제 Class·API·SQL·Config·화면·Permission·Script·Test를 우선하고 문서와 양방향으로 추적한다. |

## 1. Gateway 선택 기준

외부 Trust Boundary, 중앙 Route 정책, HMAC/Nonce/SSRF/TLS, Target Load Balancing, 외부 Write Attempt, Publish/ACK/NACK/LKG가 필요할 때 선택한다. 내부 Same-JVM 호출을 모두 Gateway로 우회하지 않는다.

## 2. 설치

1. Artifact/Manifest/SHA/Checksum.
2. Policy Store/Secret Provider/Certificate.
3. Public Listener와 Admin/Probe Listener 분리.
4. Network Zone/Firewall.
5. Route 없는 상태로 기동.
6. Health/Capability/Version.
7. LKG 저장.

## 3. Server Group·Target·Discovery

필드: groupId, memberId, target URI, weight, zone, health path, TLS profile, enabled, version.
검증: scheme/host/port/path allowlist, member 0 금지, DNS/IP policy, duplicate/version CAS.

Discovery를 쓰더라도 최종 Target 후보는 SSRF 정책과 TLS policy를 통과해야 한다.

## 4. Route·Predicate·Filter·Rewrite

Route는 listener, method, host/path/header/query predicates, priority, rewrite, header filter, target group, security, resilience, idempotency를 하나의 Versioned Snapshot으로 관리한다.

Golden Request로 실제 Target URI/Header/Body Hash를 계산해 검증한다.

## 5. Authentication·Authorization

외부 Client Identity와 내부 Service Identity를 분리한다. Issuer/Audience/Subject/Scope/Permission/Expiry/Clock Skew/Key Version을 검증한다. Gateway 1차 제어가 업무 Service의 Data Scope를 대신하지 않는다.


### 5.1 Default Deny 공개 원칙

Gateway는 Service Registry에 Endpoint가 존재한다는 이유만으로 외부 공개하지 않는다. **Default Deny**가 기본이며, 환경에 맞는 Route/Binding이 검증·승인되어 ACTIVE이고 대상 Gateway Instance가 해당 Version을 ACK한 경우에만 공개 가능한 상태로 판정한다. ADM·BAT·Actuator·Internal Endpoint는 명시적 예외 근거 없이 외부 Route 대상으로 등록하지 않는다. Retry는 멱등 Route에서만 허용하며 비멱등 호출의 응답 유실은 Attempt Ledger와 Target 원장을 대사한다.

## 6. HMAC·Audience·Body Hash·Nonce

Canonical String에는 method, normalized path, canonical query, content SHA-256, timestamp, nonce, audience를 포함한다.

검증 순서:

1. Key ID/Version.
2. Timestamp/Skew.
3. Nonce replay.
4. Body hash.
5. Canonical string.
6. Constant-time signature compare.
7. 성공 Nonce/Attempt 기록.

Negative: body/path/audience 변경, expired/future timestamp, nonce replay, retired key, client cert mismatch.

## 7. SSRF·TLS

- 승인 Scheme.
- 등록 Group/Allowlist Host.
- Loopback/link-local/metadata 차단.
- DNS resolve 결과 재검증.
- Redirect Target 재검증.
- 사용자 입력 전체 URL 금지.

TLS는 protocol/cipher/trust/client cert/hostname/SAN/expiry/revocation을 관리한다.

## 8. Timeout Budget

`Client Deadline > Gateway Overall > Queue + Connect + TLS + Response + Transform + Ledger`

Retry Backoff도 Overall Budget 안에 포함한다. Write response loss는 Blind Retry하지 않는다.

## 9. Retry·Circuit Breaker·Bulkhead·Rate Limit

- Validation/Auth 오류는 재시도하지 않는다.
- Connect 전 일시 오류만 정책상 Retry.
- 명시 5xx는 Idempotency/Budget 확인.
- Circuit window/failure/open/half-open probe.
- Bulkhead concurrency/queue.
- Rate limit key/limit/window/response header.

## 10. Idempotency·Attempt Ledger

저장: route/version, client, idempotency key, request hash, target group/member, attempt no, dispatch time/deadline, target tracking ID, result.

같은 Key+같은 Hash는 기존 결과, 같은 Key+다른 Hash는 Conflict.

## 11. UNKNOWN_RESULT

Dispatch 이후 응답을 잃으면 Attempt를 UNKNOWN으로 두고 Target Status Query/업무 Owner 대사로 확정한다.

## 12. Validate

- 중복 Route/우선순위.
- Predicate/Rewrite 결과.
- Target 존재/Health.
- SSRF/TLS/Secret.
- Timeout 관계.
- Retry/Idempotency.
- Schema/Checksum.
- Golden/Negative Request.

## 13. Approval·Publish

Approval Snapshot: route version, target, security, timeout, checksum, target list, reason, expiry.

Publish:

1. Operation 생성.
2. Target별 version/checksum dispatch.
3. ACK/NACK/Timeout 수집.
4. Observed 확인.
5. PARTIAL/DRIFT 분리.
6. 성공 Target 보존.
7. 실패 Target Reapply 또는 LKG Rollback.

## 14. ACK·NACK·Partial Apply

| 상태 | 의미 | 행동 |
|---|---|---|
| ACK | 적용 성공 응답 | Observed/Health 확인 |
| NACK | 검증/적용 거부 | Error/환경 보정 |
| Timeout | 결과 미수신 | Applied Version 조회 |
| PARTIAL | 혼합 결과 | 성공 Target 보존 |
| DRIFT | Desired≠Observed | Reconcile/Rollback |

## 15. LKG·Rollback

LKG에는 Route/Target/Security/Timeout/Checksum/Secret Reference Version을 저장한다. Rollback도 Target별 Operation이며 부분 Rollback을 별도 상태로 기록한다.

## 16. Scale-out·Drift·Reconciliation

새 Instance는 현재 Desired Version을 받기 전 Traffic 대상이 되지 않는다. Scale-in 시 Drain 후 Registry/Lease를 제거한다. 주기적으로 Desired/Observed/Checksum/Health를 비교해 Drift를 탐지한다.

## 17. Probe·Health

DNS→TCP→TLS→HTTP→Authentication→Application 단계로 Probe를 분리한다. Connection Test 자체도 외부 Write 부작용을 만들지 않게 별도 안전 Endpoint를 사용한다.

## 18. ADM 운영

Dashboard, Server/Group, Route, Security, Health, Transaction, Log Policy, Apply Status 화면에서 같은 Route/Operation/Target 식별자를 사용한다.

## 19. 장애 Runbook

### Route NACK

Validation Error→Target Config→Secret/Cert→Version/Checksum 순으로 확인하고 수정된 새 Snapshot으로 재게시한다.

### Response Loss

Attempt 조회→Target 상태→Owner 대사→성공/실패/보상 확정.

### Partial Apply

성공 Target 보존→실패 Target 원인→Reapply 또는 전체 LKG 정책→최종 Drift 0 확인.

### SSRF/TLS 차단

정책을 끄지 말고 Host/IP/Certificate/Trust 설정을 보정한다.

## 20. Route 게시·응답 유실 통합 실습

Route Draft→Golden Validate→Negative Security Test→Approval→Publish→Target 하나 NACK→PARTIAL 확인→실패 Target 수정→Reapply→Observed 일치→LKG Rollback Drill까지 수행한다.

## 21. 완료 체크리스트

- [ ] Route/Predicate/Filter/Rewrite가 Versioned Snapshot이다.
- [ ] Target/Discovery/Load Balancing이 있다.
- [ ] Authentication/Authorization/HMAC/Audience/Nonce/SSRF/TLS가 있다.
- [ ] Timeout/Retry/Circuit/Bulkhead/RateLimit 관계가 있다.
- [ ] Idempotency/Attempt/UNKNOWN_RESULT가 있다.
- [ ] Validate/Approval/Publish/ACK/NACK/PARTIAL가 있다.
- [ ] LKG/Rollback/Drift/Reconcile가 있다.
- [ ] Scale-out/Probe/ADM/Runbook/EDU가 있다.

## 22. Trusted Proxy·Header Policy

외부에서 들어온 내부용 Header를 그대로 신뢰하지 않는다. Trusted Proxy Chain과 Client Header Allowlist를 적용하고 내부 Identity/Trace Header는 승인 경계에서 재생성하거나 덮어쓴다.

## 23. Streaming·Client Disconnect

Request/Response Body를 스트리밍하는 Route는 전체 Body 재적재를 피하고 Retry 가능 조건을 제한한다. Client Disconnect, Target Disconnect, Partial Response, Response Body read failure를 서로 다른 Attempt 단계로 기록한다.

## 24. Rate Limit·Quota

Client/Channel/API/Tenant별 rate, quota, burst, distributed counter를 정의한다. 429와 Retry-After 의미를 일관되게 제공하고 운영 Override는 Permission/Reason/Expiry/Audit를 사용한다.

## 25. Multi-instance Correlation

비동기/Streaming 응답이나 Target callback이 인스턴스 로컬 Memory Queue에만 의존하지 않게 Durable Correlation을 사용한다. Instance loss/rebalance/late reply를 재현한다.

## 26. Gateway Artifact Trust

Route/Policy Artifact는 version/checksum/signature/keyId를 가지며 Target 적용 전에 검증한다. 잘못된 Snapshot은 fail-closed하고 마지막 승인 LKG를 유지한다.

## 27. Gateway Reference EDU 14개 전수 지도

| ID | 예제 | 핵심 확인 | 장애·복구 관점 |
|---|---|---|---|
| `EDU-GW-01` | Server Group·Health·Load Balancing | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |
| `EDU-GW-02` | Route·Predicate·Path Rewrite | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |
| `EDU-GW-03` | 인증·권한·TLS·HMAC·Nonce | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |
| `EDU-GW-04` | Timeout·Retry·Circuit Breaker·Bulkhead | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-GW-05` | Draft·검증·승인·게시·부분 적용 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-GW-06` | Attempt Ledger·UNKNOWN_RESULT·LKG 복구 | operation/attempt 원장과 Owner 결과 대사 | 확인 전 재실행 금지 → reconcile → 필요한 경우 compensation/확정 |
| `EDU-GW-07` | Service Discovery·Target Failover·복귀 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-GW-08` | SSRF Allowlist·DNS Rebinding·내부망 차단 | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |
| `EDU-GW-09` | Header 정리·경로·요청·응답 변환 | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |
| `EDU-GW-10` | Body 크기·Content-Type·Schema Validation | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-GW-11` | Command 멱등성·Attempt Ledger·응답 유실 | operation/attempt 원장과 Owner 결과 대사 | 확인 전 재실행 금지 → reconcile → 필요한 경우 compensation/확정 |
| `EDU-GW-12` | 다중 인스턴스 설정 Drift·Reconcile | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-GW-13` | Canary·가중치 Routing·Version Rollback | version/idempotency/lease 소유권과 경쟁 요청 결과 | stale writer 차단, 최신 상태 재조회 후 재판단 |
| `EDU-GW-14` | Gateway 관측·개인정보 가림·감사 | route version/checksum/target/attempt/보안 정책 | NACK·partial apply는 성공 대상 보존, LKG/reconcile |

공통 실행 역할: `CPF_EDU_GATEWAY_OPERATOR`. 각 ID의 **정확한 requiredFields·businessStates·exceptionScenarios·requiredVerification·handler/source/test/timeout**은 기준 Commit의 `cpf-reference/src/main/resources/edu/manual-135-catalog.json`과 동일하게 유지한다. 매뉴얼에서는 그 계약을 업무 절차에 연결하며, 임의 필드를 추가하지 않는다.


<!-- R17-EDU-GW-DETAIL-BEGIN -->
## 27A. EDU-GW 전수 실행 카드 — 14개

아래 카드는 `manual-135-catalog.json`의 ID를 잃지 않고 매뉴얼 업무 절차로 연결한다. **정확한 필드명·상태·Handler·Source·Test는 같은 ID의 정본 값을 사용하며 문서가 별도 제2 정본을 만들지 않는다.** 대신 독자는 각 ID에서 무엇을 준비하고 무엇을 실패시켜 어떻게 정상화를 판정하는지 이 절만으로 이해할 수 있어야 한다.

### EDU-GW-01 — Server Group·Health·Load Balancing

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-01/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-01` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-02 — Route·Predicate·Path Rewrite

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-02/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-02` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-03 — 인증·권한·TLS·HMAC·Nonce

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-03/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-03` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-04 — Timeout·Retry·Circuit Breaker·Bulkhead

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-04/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-04` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-05 — Draft·검증·승인·게시·부분 적용

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-05/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-05` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-06 — Attempt Ledger·UNKNOWN_RESULT·LKG 복구

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-06/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-06` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-07 — Service Discovery·Target Failover·복귀

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-07/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-07` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-08 — SSRF Allowlist·DNS Rebinding·내부망 차단

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-08/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-08` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-09 — Header 정리·경로·요청·응답 변환

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-09/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-09` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-10 — Body 크기·Content-Type·Schema Validation

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-10/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-10` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-11 — Command 멱등성·Attempt Ledger·응답 유실

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; idempotency/operation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-11/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 동시 중복·응답 유실.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-11` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-12 — 다중 인스턴스 설정 Drift·Reconcile

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-12/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-12` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-13 — Canary·가중치 Routing·Version Rollback

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; expected version; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-13/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; stale version/경쟁 갱신.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-13` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-GW-14 — Gateway 관측·개인정보 가림·감사

- **역할:** `CPF_EDU_GATEWAY_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route version/checksum/target/attempt.
- **실행:** `POST /api/reference/edu-capabilities/EDU-GW-14/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** DRAFT/VALIDATED/APPROVED/PUBLISHED 및 Target ACK/NACK.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** Partial Apply는 Target별 reconcile, 필요 시 LKG/rollback; UNKNOWN_RESULT는 attempt ledger 대사.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-GW-14` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.
<!-- R17-EDU-GW-DETAIL-END -->

## 28. Gateway Route 생명주기

```text
DRAFT → VALIDATED → APPROVAL_REQUIRED/APPROVED → PUBLISHED
      → instance ACK/NACK collection → APPLIED or PARTIAL_APPLY
      → RECONCILING → LKG/ROLLBACK when required
```

Route Version과 Checksum은 승인 대상과 게시 대상이 같은지 확인하는 계약이다. Approval 이후 Payload가 바뀌면 같은 승인으로 게시하지 않는다.

## 29. Attempt Ledger와 응답 유실

외부 호출은 connect/send/response/read 단계와 Target Instance를 Attempt에 기록한다. `send` 뒤 응답을 잃은 요청은 Target Side Effect 가능성이 있으므로 무조건 Retry하지 않는다. Request Hash, Idempotency Key, Target Tracking ID, Attempt 상태를 대사한다.

## 30. SSRF·TLS·Trusted Proxy

- scheme/host/port/CIDR/service allowlist.
- localhost/metadata IP/private network 차단 정책.
- URI canonicalization, redirect 정책, DNS rebinding/CNAME/IPv6 검증.
- trusted proxy chain과 forwarded/header allowlist.
- 내부 보안 Header는 Client 값으로 신뢰하지 않고 경계에서 재생성/덮어쓴다.
- HMAC은 canonical request, audience, body hash, nonce/expiry/replay ledger와 함께 검증한다.

## 31. Scale-out·Canary·Drift

Instance별 Desired/Observed Route Version/Checksum을 비교한다. Canary는 weight/segment/version을 명시하고 Error/Latency/Business KPI가 Gate를 벗어나면 확대를 중단한다. Partial Apply에서 정상 Instance까지 일괄 Rollback하지 않고 실제 Side Effect가 있는 대상만 판단한다.







## 32. Gateway 최종 Gate

GW 14/14, Server Group/Discovery/LB, Route/Predicate/Filter/Rewrite, AuthN/AuthZ/HMAC/TLS/SSRF, Timeout/Retry/Circuit/Bulkhead/Rate Limit, Idempotency/Attempt/UNKNOWN_RESULT, Validation/Approval/Publish, ACK/NACK/Partial, LKG/Rollback, multi-instance drift/reconcile, Probe/Health/Observability/Audit가 하나의 운영 절차로 이어져야 한다.
