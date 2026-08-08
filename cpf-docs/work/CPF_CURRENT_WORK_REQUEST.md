# CPF QA 전 최종 Core Hardening 개발 요청서

> Current canonical path: `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`  
> Package source master: `a570b366ef85b23863e41173c991025c072a2427` (`07_12`)  
> Canonical requirement target after this currentization: **180**  
> 목적: 직전 QA A/B 재개발 결과를 successor에서 다시 검산하고, 신규 Core Transaction/Starter DX 요구와 기존 미흡점을 한 사이클에서 구현 완료한 뒤 강화 QA A/B로 넘긴다.

## 1. 최우선 실행 원칙

이 요청은 **신규 XA/JTA/TCC 기능만 추가하는 작업이 아니다**.

개발 시작 시 반드시 최신 `origin/master`, exact SHA, Working Tree를 다시 확인한다. 이 패키지는 `a570b366ef85b23863e41173c991025c072a2427`를 검토하여 작성되었지만 사용자가 Overlay를 적용·commit·push하면 Developer GPT의 실제 기준은 그 successor SHA다.

직전 Developer GPT의 다음 문구를 자동 승계하지 않는다.

`개발GPT 추가 구현 작업 없음 / Remaining = Runtime-only verification`

그 판정은 신규 Requirement가 정본화되기 전 `a570b366ef85b23863e41173c991025c072a2427` 기준 판정이다. 이번 현행화 후에는 **다시 직접 Source를 검토하여 구현 가능한 Gap이 없을 때만** 같은 결론을 낼 수 있다.

QA가 아직 재검수하지 않았더라도 다음이 보이면 즉시 같은 개발 사이클에서 보완한다.

- 불완전 Source
- Consumer 없는 API/SPI
- Interface/DTO/Sample만 존재
- Wrapper-only Starter
- Test/Harness 부재
- Config/SQL/OpenAPI/Generator/Frontend 누락
- 잘못된 Owner/Package
- Dead/duplicate/stale Source
- False-green verifier
- 오류/UNKNOWN/복구 누락
- 개발자 사용성이 OSS 직접 적용보다 나쁨
- 문서와 실제 Source 불일치

**QA Finding이 없다는 것은 구현 완성의 증거가 아니다.**

## 2. 이번 작업 시작 시 먼저 재검토할 successor 변경

`a570b366ef85b23863e41173c991025c072a2427`에서 직전 개발 결과는 실제로 다음 영역에 들어왔다.

- authenticated first-hop transaction identity
- FileLog HOL recovery
- Timeline QUERY_FAILED/PARTIAL
- release qualification root-of-trust
- persistence-mybatis / messaging-reliability-jdbc / session-jdbc package relocation
- ADM/BZA OpenAPI error + typed high-risk client
- Online A→B→C(/D) Spring/JDBC reference
- Batch durable checkpoint / OS process kill / multi-process lease
- DB3 runtime lock lifecycle
- Public API/SPI Korean JavaDoc gate
- JDBC `CpfLockManager`
- messaging reliability Outbox/Router/Reconcile

위 목록은 PASS 선언이 아니다. Requirement→Source→Symbol→Consumer→Call Path→Failure→Recovery→Test/Harness를 다시 확인한다.

특히 successor에는 직전 `DELETE_MANIFEST.csv`의 구 package 파일이 함께 남은 정황이 확인되었다. 새 canonical replacement가 존재하는 것을 확인한 뒤 `cpf-docs/work/CPF_DELETE_MANIFEST.csv`의 exact allowlist만 정리하고 stale package/import/reference가 0인지 재검증한다.

## 3. P0 Core Transaction & Integration Reliability

### 3.1 공식 전략

다음을 CPF 공식 Transaction Capability Matrix로 구현한다.

`LOCAL / XA_JTA / OUTBOX / INBOX_DEDUP / SAGA / TCC`

전략은 상호 대체가 아니라 업무 일관성 요구에 따라 선택·혼합 가능해야 한다.

권장 기본:

| 업무 | 기본 전략 |
|---|---|
| 단일 DB | LOCAL |
| DB+DB 강한 원자성 | XA/JTA |
| DB+JMS 강한 원자성 | XA/JTA |
| DB+Kafka/RabbitMQ/Event | OUTBOX 우선 |
| MSA A→B→C | SAGA |
| 잔액/한도/재고 Hold | TCC |
| 외부 결과 불명 | UNKNOWN+RECONCILE |

### 3.2 LOCAL

실제 Domain Consumer에서 commit/rollback, propagation, isolation, timeout, read-only, exception translation을 검증한다. 기본 사용에 XA/JTA Provider가 필요하면 안 된다.

### 3.3 XA/JTA

다음을 실제 Source로 구현한다.

- topology-neutral transaction contract
- Optional JTA integration
- Tomcat-compatible standalone Transaction Manager Adapter
- JTA-capable WAS managed Transaction Manager Adapter 경계
- `XADataSource`
- `XAConnectionFactory` / `XAResource`
- DB+DB
- DB+JMS
- 2PC prepare/commit/rollback
- transaction timeout
- heuristic outcome
- in-doubt state
- recovery log
- startup recovery scan
- duplicate recovery/fencing

Narayana/Atomikos 등 특정 구현을 `cpf-core`에 강제하지 않는다. Reference Provider를 하나 선택할 수 있으나 Public Contract는 Provider-neutral이어야 한다.

공식 DB Vendor는 Oracle/PostgreSQL/MariaDB만 사용한다.

### 3.4 XA Crash Recovery

반드시 다음 Harness를 작성한다.

1. XA begin
2. 두 Resource enlist
3. prepare 완료
4. Process Kill
5. restart
6. recovery scan
7. 최종 commit/rollback 판정
8. duplicate side effect 0
9. ADM/Log/transactionId 확인

실행 환경이 없으면 Source/Harness/Script/Config까지 구현하고 `미검증`으로 남긴다. Harness 자체가 없으면 FAIL이다.

### 3.5 Outbox / Inbox-Dedup

기존 `messaging-reliability-jdbc`를 새로 갈아엎지 말고 직접 검산·고도화한다.

Outbox:

- Business update + Outbox INSERT = 동일 Local TX
- stable eventId/messageId
- transactionId
- PENDING/CLAIMED/PUBLISHED/CONFIRMED/FAILED/UNKNOWN/RETRYING/DEAD/RECONCILED
- lease/fencing
- ACK loss
- Publisher kill/restart
- multi-instance
- cleanup/retention

Inbox/Dedup:

- event/message ID + consumer identity
- idempotency
- concurrent duplicate
- partial processing
- Consumer kill/restart
- replay
- retention
- duplicate business side effect 0

Outbox를 Transaction/Audit Log와 동일 개념으로 합치지 않는다.

### 3.6 Saga

기존 `SAGA-*` Requirement를 실제 구현/Consumer까지 확인한다.

상태 최소:

`STARTED / RUNNING / COMPLETED / FAILED / COMPENSATING / COMPENSATED / UNKNOWN / MANUAL_REVIEW`

A→B→C(/D), C 실패, 역순 compensation, compensation 자체 실패/retry, process kill, duplicate, UNKNOWN/Reconcile을 실행 가능한 Reference로 검증한다.

### 3.7 TCC

Optional TCC Contract와 실제 Reference Consumer를 구현한다.

- Try idempotency
- Confirm idempotency
- Cancel idempotency
- Empty Rollback
- Hanging
- Duplicate Confirm/Cancel
- Timeout
- Process Kill
- Recovery
- UNKNOWN/Reconcile
- Manual Review

Framework가 업무 Hold/Reservation 의미를 임의로 구현하지 않고 Business Consumer가 명확한 Contract를 제공한다.

## 4. P0 E2E Transaction Lineage

다음을 각각 PASS하고 끝내지 않는다.

`Transaction + Domain Call + External Call + Messaging + Batch + Logging + Audit + Trace + Recovery + ADM`

동일 Reference Transaction에서 확인한다.

```text
Authenticated Channel
 → transactionId
 → Domain A
 → Domain B
 → DB
 → External API
 → Outbox/Broker
 → Domain C
 → Retry/UNKNOWN/Reconcile
 → Transaction/Integration/Audit Log
 → ADM Timeline
```

Retry 시 transactionId 재발급 금지. attempt/segment/span/execution은 별도다.

Local/Remote, REST/SOAP/TCP/File, JMS/Kafka/RabbitMQ, Batch, Saga/TCC/XA recovery에서도 동일 lineage를 유지한다.

## 5. Log / Audit / Timeline

목적을 분리한다.

- Transaction Log: 거래 진행/상태
- Integration Log: 외부 요청/응답/Message
- Audit Log: 누가 무엇을 했는지
- Outbox: 아직 외부 전달이 완료되지 않은 durable message state
- Inbox/Dedup: 수신 처리/중복 제어

ADM transaction detail에서 이들을 transactionId/eventId/correlationId로 연결해 하나의 Timeline으로 재구성한다.

Source query 실패를 `NOT_APPLICABLE`로 숨기지 않는다. Partial/QUERY_FAILED/UNKNOWN을 명시한다.

## 6. Starter 전체 Developer Experience 전수 고도화

`CPF_STARTER_VALUE_CATALOG.csv`와 Canonical Starter Catalog의 **활성 Starter 전부**를 직접 연다.

각 Starter마다 다음을 기록하고 필요한 Source를 고친다.

1. 실제 convenience API
2. OSS 직접 사용 대비 제거되는 boilerplate
3. actual Consumer
4. AutoConfiguration
5. 최소/직관적 Config
6. safe default
7. Fail-Fast
8. 표준 Error
9. transactionId/context
10. Security/Authorization
11. Masking
12. Audit/Logging/Observability
13. Timeout/Retry/Circuit Breaker
14. Idempotency/UNKNOWN/Reconcile
15. Provider 확장성
16. Native API Escape Hatch
17. 미사용 0-footprint
18. Sample/EDU
19. 정상/실패/timeout/partial/recovery
20. 실제 OSS-direct 대비 사용성

Wrapper-only Starter는 완료 처리하지 않는다.

Consumer가 하나뿐이고 Provider 확장 가치가 없으면 형식적 SPI를 만들지 않는다.

## 7. 신규/보강 Capability

### 7.1 AI Optional

- Provider-neutral API/SPI
- model/provider routing
- timeout/retry/circuit breaker/fallback
- masking/sensitive-data policy
- token/usage/cost metering
- audit/observability
- transactionId
- authorization/policy
- high-risk human approval
- provider failure/timeout/UNKNOWN
- 실제 Consumer/EDU

자체 LLM, 자체 Vector DB, 대형 Agent Framework는 기본범위가 아니다.

### 7.2 OAuth2/JWT Developer API

기존 Resource Server를 유지하고 반복 코드를 줄인다.

`currentUserId()`, `currentTenantId()`, `currentPrincipal()`, `hasRole()`, `hasScope()`, safe claim access, issuer/audience/expiry, role/scope mapping, token propagation, 401/403를 검토·보강한다.

### 7.3 SSO/OIDC

OIDC/OAuth2 Login, Keycloak/Entra ID/Okta, user/role/group/claim mapping, CPF User/Tenant/Authority Context, login/logout/session/token 만료·갱신, Frontend/BFF를 최소 설정으로 연결한다. SAML2는 Optional.

### 7.4 KMS/HSM / Digital Signature

기존 Secret/Crypto/Certificate를 재사용한다.

- KMS/HSM Provider
- PKCS#11 Optional
- key version/rotation/revocation/health/timeout
- sign/verify
- algorithm/keyId/certificate/signature metadata
- Audit
- Private Key/Secret 원문 노출 금지

Hash/HMAC/AES-GCM은 중복 Starter를 만들지 않는다.

### 7.5 Tamper-evident Audit

기존 `SEC-AUDIT`를 검산·고도화한다.

append-only, canonical payload, previousHash/currentHash, 수정/삭제/순서변경 탐지, signature, concurrency, multi-instance, masking/evidence integrity.

Blockchain/DLT Starter는 이번 범위에 추가하지 않는다.

## 8. EDU/Reference 보완

기존 EDU 135를 삭제하거나 임의 숫자 증가로 해결하지 않는다. 기존 Track과 중복 여부를 먼저 확인하고, 필요한 Feature/Scenario를 기존 EDU에 통합하거나 신규 executable feature로 추가한다.

최소 실행형 Reference:

- LOCAL commit/rollback
- XA DB+DB
- XA DB+JMS
- XA prepare kill/recovery
- Outbox 동일 TX
- ACK loss/Publisher kill/duplicate
- Inbox/Dedup
- Saga compensation/retry
- TCC confirm/cancel/empty rollback/hanging
- External timeout/UNKNOWN/Reconcile
- Domain A→B→C local/remote
- Batch→A→B→C transaction boundary
- 동일 transactionId + ADM Timeline

Generator/Generated Domain도 같은 Public API를 실제 Consumer로 사용해야 한다.

## 9. Catalog / Settings / BOM / Generator 규칙

신규 모듈 이름만 Catalog에 먼저 넣지 않는다.

실제 Source 폴더와 `build.gradle`, AutoConfiguration, Owner Group/Internal Role, Consumer, Test가 준비된 뒤 같은 변경 단위에서:

- settings.gradle
- Canonical Starter Catalog
- BOM/publication
- capability profiles
- generator template/schema/lock
- generated domain
- reference/EDU
- docs

를 동기화한다.

Public BOM에 Internal Leaf를 무분별하게 노출하지 않는다.

## 10. 직전 push 후 Repository Hygiene

현재 successor에서 구 package + 신규 package가 동시에 남은 정황이 있다.

`cpf-docs/work/CPF_DELETE_MANIFEST.csv`는 직전 Developer의 66개 allowlist를 Current-State 형태로 승계한다.

삭제 조건:

1. exact old path가 실제 존재
2. manifest의 exact replacement path가 존재
3. protected path가 아님
4. replacement가 consumer/build 기준 canonical
5. 삭제 후 stale import/package/reference 0

광범위 wildcard 삭제 금지.

`cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/assets/manuals/**`, `cpf-docs/assets/readme/**`, `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`는 보호한다.

작업 종료 시 날짜/REV/SESSION/checkpoint/debug/tmp/pycache/obsolete zip/hash/duplicate current 문서가 새로 쌓이지 않았는지 검사한다.

## 11. 직접 검수 방법 — 상태값 Shortcut 금지

각 Requirement/Review ID는 최소:

`Requirement → Source Path → Symbol → Consumer → Call Path → Failure/Recovery → Test/Harness → Config/SQL/OpenAPI/Generator/Frontend → Execution → Evidence → Judgement`

를 연결한다.

Source를 직접 열지 않은 항목은 완료로 세지 않는다.

`재확인 필요`를 미검수의 대체값으로 사용하지 않는다.

`미검증`은 Source+Consumer+Call Path+Test/Harness+Script/Config/SQL/Generator가 모두 준비되고 **외부 Runtime 환경만** 없을 때만 허용한다.

Generic Evidence 문구를 수백 행 복사해 개별 검수로 계산하지 않는다.

## 12. 진행률 보고 — 작업 중단점 아님

약 5분마다 화면에 다음을 짧게 표시하고 즉시 작업을 계속한다.

- 현재 Phase
- 전체 진행률 %
- Requirement 직접 Source 확인 x/total
- Consumer 확인 x/total
- Test/Harness 확인 x/total
- 개발 완료 x/total
- FAIL/미구현 수
- Runtime-only 수
- 신규 Finding 수
- 지금 작업 영역
- 다음 작업

`100% review`와 `QA PASS/Release Ready`를 혼동하지 않는다.

## 13. 검증 순서

1. latest master/exact SHA/working tree
2. Canonical/Current Request 정합성
3. prior successor implementation re-review
4. package stale duplicate cleanup plan
5. low-cost static gates
6. Source implementation
7. unit/contract/negative tests
8. Generator/Catalog/DB3/OpenAPI/Frontend
9. integrated reference runtime
10. process-kill/multi-instance
11. available Build/Test
12. self-review
13. matrices/evidence/currentization
14. package/hygiene/hash

단일 Gate 실패 후 멈추지 말고 가능한 독립 Gate를 계속 실행하여 공통 원인별로 일괄 수정한다.

## 14. 완료 판정

다음이 모두 만족될 때만 Developer GPT가 구현 종료를 선언할 수 있다.

- 이번 Current Request의 모든 개발 가능한 Requirement 직접 Source 검토 완료
- developer-remediable FAIL 0
- Consumer 없는 API/SPI 0
- Wrapper-only Starter 0
- 핵심 Harness 누락 0
- Config/SQL/OpenAPI/Generator/Frontend 누락 0
- stale duplicate package/reference 0
- required EDU/Reference 누락 0
- exact current Source와 Evidence 일치
- 남은 항목이 실제 외부 Runtime 실행뿐

그때만:

`현행 요건상 개발GPT 추가 구현 없음 / Remaining = Runtime-only verification`

이라고 기록한다.

그 전에는 QA로 넘기지 않는다.

## 15. Git/삭제 안전

Developer GPT는 사용자 승인 없이 Commit/Push/Pull/Merge/Branch/Tag/PR/Release/Reset/Restore/Stash/Clean/실제 파일 삭제를 수행하지 않는다.

삭제가 필요하면 `CPF_DELETE_MANIFEST.csv`와 한 줄 명령을 갱신하고 사용자 실행 대상으로 남긴다.

## 16. 최종 산출물

기존 Current 파일을 직접 현행화하고 동일 목적의 R/REV/SESSION/날짜 복제 파일을 만들지 않는다.

최종 결과에는 최소:

- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_REQUIREMENT_MATRIX.csv`
- `CPF_SCENARIO_MATRIX.csv`
- `CPF_STARTER_VALUE_CATALOG.csv`
- `CPF_SOURCE_FINDINGS.csv`
- Canonical/Continuity currentization
- Source/SQL/API/Test/Config/Frontend/Generator
- `CHANGE_MANIFEST.csv`
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `REQUIREMENT_STATUS.csv`
- `REVIEW_INDEX.md`
- `PACKAGE_MANIFEST.json`
- SHA-256
- exact Delete Manifest
- Handover/Codex 요청

을 Current-State 방식으로 제공한다.

QA/Codex 상태를 임의 PASS로 바꾸지 않는다.

