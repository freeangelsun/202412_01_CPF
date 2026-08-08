# CPF QA A/B 전수 교차검수 인수인계 표준

> Current path: `cpf-docs/work/handover/CPF_QA_SESSION_HANDOVER_STANDARD.md`  
> Currentization basis: `b2da6bd720d1a8506db6bddf5d2e35feb9dca964` (`07_15`)  
> 목적: Developer 자체완료를 QA PASS로 오인하지 않고, QA A/B가 동일 전체 Scope를 각각 독립 전수검수한 뒤 교차대조한다.

## 1. QA 역할

QA는 Developer 보고서를 검토하는 역할이 아니라 최신 `origin/master`의 실제 제품을 독립 판정하는 역할이다.

검수 시작 시 반드시 확인한다.

- latest origin/master
- exact SHA
- Working Tree
- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `CPF_CURRENT_WORK_REQUEST.md`
- `REQUIREMENT_STATUS.csv`
- `CPF_REQUIREMENT_MATRIX.csv`
- `CPF_SCENARIO_MATRIX.csv`
- Source/SQL/API/Test/Config/Frontend/Script/Generator
- Evidence provenance

과거 PASS, Developer 자체검수, 상대 QA의 PASS를 자동 승계하지 않는다.

## 2. QA A — 전체 100% 전수검수

순서:

`Canonical → Architecture → Core/Foundation → Starter/Provider → Source → Consumer → Generator/Generated Domain → DB3 → Transaction/Security → Health/Operations → ADM/BZA/OpenAPI → Test/Harness → Runtime/Evidence → Repository Hygiene`

각 Requirement는 다음 Trace를 가져야 한다.

`Requirement ID → Source Path/Symbol → Consumer → Call Path → Failure/Boundary → Test/Harness → Execution/Evidence → QA A Judgement`

Source를 직접 열지 않은 항목은 Deep Review 완료로 계산하지 않는다.

## 3. QA B — 동일 전체 100% 독립 전수검수

QA B는 QA A와 같은 전체 Scope를 검수하되 역순으로 진행한다.

`Repository Hygiene → Runtime/Evidence → Test/Harness → ADM/BZA/OpenAPI → Health/Operations → Transaction/Security → DB3 → Generator/Generated Domain → Consumer → Source → Starter/Provider → Core/Foundation → Architecture → Canonical`

QA A의 Evidence를 탐색 참고로 볼 수는 있으나 판정 근거를 상속하지 않는다.

## 4. Cross Validation

A/B 완료 후 exact Requirement ID 단위로 다음을 교차대조한다.

- A 판정
- B 판정
- A Evidence
- B Evidence
- Source/Consumer/Test mapping 일치 여부
- 불일치 원인
- 재개발 필요 여부
- 재검수 필요 여부
- Runtime-only 여부

다음 중 하나라도 있으면 완료가 아니다.

- A PASS / B FAIL
- A FAIL / B PASS
- 한쪽 미검수
- 한쪽 Source 미확인
- generic evidence 반복
- 최신 SHA가 다름
- Runtime 미실행을 PASS로 기록
- Developer 보고만으로 PASS

## 5. Framework Fundamentals 독립 Sweep

Canonical Requirement 외에도 QA A/B 각각 다음 기본 기능을 별도 Sweep한다.

### Web/API
Request/Response, Validation, Error, Paging, Sort/Search, Header, File, Idempotency, Rate Limit, OpenAPI, Generated Client, 400/401/403/404/409/429/500/503.

### Persistence
CRUD, Exists/Count, Page/Slice/Cursor, JDBC, MyBatis, Spring Data JPA Optional, Transaction, Lock, DB3, Timeout, Bulk, Multi-datasource.

### Core/Foundation
Core→Starter 0, compileOnly Ownership, Utility Wrapper, Default/Adapter/Configuration/Filter/Repository/Provider의 Owner, Foundation purity.

### Security
Current User/Tenant/Role/Scope, OAuth2/JWT/OIDC/SSO, Distributed Session, Secret/KMS/HSM, Signature, Masking, Audit.

### Reliability
Timeout, Retry, Circuit Breaker, Idempotency, UNKNOWN, Reconcile, Multi-instance, Process Kill, Lease/Fencing.

### Integration
Domain Call, REST, SOAP, TCP, File, Object Storage, JMS, IBM MQ, Kafka, RabbitMQ, Batch.

### Operations
Health, Liveness, Readiness, Startup, Drain, Registry, Runtime Control, ADM Instance View.

### Observability
transactionId, Log, Integration Log, Audit, Metrics, Trace, Timeline.

### Developer Experience
Quick Start, minimal config, safe default, fail-fast, actionable error, Native Escape, JavaDoc, EDU, 0-footprint.

### Repository Hygiene
old/new relocation duplicate, stale import/config/test/doc, historical workspaces, obsolete verification helper, secret, long Windows path.

## 6. 신규 Modernization 집중검수

이번 신규 작업에서는 특히 다음을 별도 분모로 추적한다.

- Core Slimming
- Unified Utility/Foundation
- Transaction ID Contract/Implementation 분리
- Runtime Health/Instance Operations
- JPA/JDBC/MyBatis parity
- Valkey Session
- S3-compatible Object Storage
- Event Schema Governance
- GraphQL Optional
- Realtime SSE/WebSocket
- Verification Tool/Gate Currentization
- Documentation consolidation
- Garbage cleanup

## 7. 미검증 판정

외부 Runtime이 없을 때만 `미검증`을 허용한다. 그 전에 Source, Consumer, Call Path, Test/Harness가 완성됐는지 직접 확인한다.

미검증 기록에는:

- 환경
- 실행 명령
- 기대 결과
- 실패 기준
- 필요한 Secret/권한/서비스
- Evidence 경로
- 재실행 조건

을 포함한다.

## 8. 문서 생성 제한

QA A/B는 세션별 `QA_FINAL`, `QA_SESSION`, `REV`, 날짜별 결과 문서를 Repository에 추가하지 않는다.

판정은 기존 Current 문서에 반영한다.

- `REQUIREMENT_STATUS.csv`
- `TEST_AND_EVIDENCE.md`
- `HANDOVER.md`
- `REVIEW_INDEX.md`

새 문서 종류가 반드시 필요하면 문서 정렬 승인을 먼저 받는다.

## 9. QA 완료 조건

- QA A 전체 분모 검수 완료
- QA B 동일 전체 분모 검수 완료
- Cross Validation 미대조 0
- A/B disagreement 0 또는 동일 ID 재개발/재검수로 해소
- False Green 0
- latest exact SHA 동일
- Developer-remediable FAIL 0
- Runtime-only는 미검증으로 분리
- Repository/Documentation Hygiene PASS

QA만 최종 완료 여부를 판정한다.
