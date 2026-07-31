# CPF QA35 최종 제품 마감 개발·검증 요청

## 1. 기준
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 기준 SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA35 Requirement: **35건**
- QA35 Defect: **28건**
- ADM Route Baseline: **59건**
- EDU Feature Baseline: **32건**

작업 시작 시 최신 `origin/master`를 다시 확인한다. 위 SHA가 최신이 아니면 모든 문서의 기준 SHA를 갱신하고 실제 최신 Source로 재검토한다.

## 2. 최종 목표
이번 작업은 중간 보강이 아니라 CPF 제품 완료를 위한 마지막 개발 요청으로 수행한다.

- QA34 거짓/조기 완료 판정을 수정한다.
- Frontend 정본을 fresh clone에서 실제 Build 가능하게 만든다.
- ADM 59개 메뉴를 실제 상용 Control Plane 기능으로 닫는다.
- CPF의 모든 Framework 기능과 162 Requirement를 EDU 예제로 전수 연결한다.
- Java·Frontend·3 Browser·3DB·Kafka·Multi-instance·Process Kill·Recovery·Supply Chain을 exact SHA에서 검증한다.
- 미구현·부분 구현·미검증·실패·재확인 필요를 0으로 만들거나 외부 차단 근거를 명확히 남긴다.

## 3. 필수 작업 순서
1. `QA34_POST_PUSH_INDEPENDENT_SOURCE_REVIEW.md`를 읽는다.
2. QA35 Defect/Requirement/ADM/EDU Matrix를 기준선으로 고정한다.
3. 작업 전 리뷰를 최신 Source로 보정한다.
4. Phase 0 Truth Reset을 먼저 반영한다.
5. Deterministic Source Gate를 통과하기 전 Runtime 작업으로 넘어가지 않는다.
6. ADM Product Closure를 59개 Route 전수로 수행한다.
7. EDU Product Closure를 162 Requirement/Public API 전수로 수행한다.
8. Runtime·Fault·Supply Chain을 수행한다.
9. 작업 후 독립 리뷰와 Codex Package를 작성한다.
10. 결과는 Root Overlay ZIP으로 제공한다.

## 4. P0 Source Closure
### 4.1 Frontend
- ADM/BZA Runtime 전체 OpenAPI를 Canonical Snapshot으로 Export한다.
- Tracked Snapshot은 SHA-free이고 Release SHA는 Evidence만 소유한다.
- Orval vue-query client, compatibility client, operation contract, marker schema 3을 생성한다.
- clean npm ci → generate → git diff 0 → lint/typecheck/test/build를 수행한다.
- stale SHA, schema2 marker, missing generated file, manual snapshot negative fixture를 추가한다.
- Source Closure가 위 실제 명령을 실행해야 한다.

### 4.2 CI
- Push/PR exact SHA마다 Java 25 empty-cache와 Frontend deterministic preflight를 실행한다.
- Required Status가 없으면 Release 완료 금지다.
- Runtime 환경이 없어도 Source 결함은 CI에서 먼저 탐지돼야 한다.

## 5. ADM 최우선 개발요건
### 5.1 Menu/Route 정본
59개 Route마다 다음을 모두 기록한다.

```text
routeId, path, menuId, group, label, component,
ownerModule, queryOperations, commandOperations,
menuPermission, buttonPermission, dataScope,
reasonRequired, approvalRequired, expectedVersion,
maskingPolicy, auditEvent, metric/trace,
normal/empty/error/recovery scenario, evidence
```

### 5.2 실제 기능
- 검색·Paging·정렬·상세·상태·오류·권한·위험조치 확인을 기능별로 제공한다.
- Route마다 고유 URL·active menu·heading·component marker·기대 API Operation을 검증한다.
- Gateway 9개 메뉴는 독립 Component 또는 명시적 Route Mode로 실제 기능을 분리한다.
- 동일 Permission 공유는 ADR·Data Scope·Negative Test로 정당화한다.
- 모든 위험조치는 Reason·Approval/Break-glass·Expected Version·Idempotency·결과 추적·Audit를 갖는다.
- Frontend 숨김이 아니라 Backend가 권한과 범위를 재검증한다.
- Route/Failure/Security Fixture는 Repository 정본으로 관리하고 Route별 Interaction을 필수화한다.
- Chromium·Firefox·WebKit에서 실제 Backend E2E를 실행한다.

### 5.3 Typed API
- 실제 Public Controller 전체를 OpenAPI에 포함한다.
- 모든 Operation은 Generated Client와 실제 Menu Consumer에 연결한다.
- generic 문자열 URL은 download/stream 등 승인된 예외만 허용한다.
- orphan API, orphan route, consumer 없는 generated operation은 Gate에서 실패한다.

## 6. EDU 최우선 개발요건
### 6.1 전수 Coverage
Canonical 162 Requirement와 Public API/SPI 전부를 다음에 연결한다.

```text
Requirement/Public Contract
→ EDU Feature/Scenario
→ cpf-reference Source
→ Config/SQL/Profile
→ 실행 명령
→ Test/Fault
→ ADM 조회·조치 경로
→ exact-SHA Evidence
```

### 6.2 필수 기능군
Build/BOM/Plugin, Generator, Transaction/Header/Error, Local/Remote, Idempotency, Concurrency, Unknown Result, Outbox/Inbox, Kafka, External REST/전문, File/Archive, Cache, Feature Flag, Secret, Security, Observability, 3DB/Migration, Batch Tasklet/Chunk, Scheduler, Worker, Center-Cut, Agent, Deployment/Rollback, Gateway, ADM/BZA, Supply Chain, Multi-instance/DR를 포함한다.

### 6.3 예제 품질
- Happy path만 제공하지 않는다.
- 정상·오류·경계·권한·동시성·중복·Timeout·응답 유실·결과불명·재시도·복구·마스킹을 적용 가능한 범위에서 실행한다.
- EDU는 Public API/SPI만 소비한다.
- 제품 UI의 기본 State에 EDU ID를 하드코딩하지 않고 EDU Profile/Fixture로 격리한다.
- Framework Contract 변경 시 EDU Row와 Test가 없으면 CI가 실패해야 한다.
- Generator 신규 Domain도 EDU·ADM·OpenAPI·Test를 함께 생성·검증한다.

## 7. Runtime·Evidence
- Java 25 fresh Gradle user home
- ADM/BZA approved Registry clean npm ci
- Chromium/Firefox/WebKit
- MariaDB/PostgreSQL/Oracle install/upgrade/rollback/reapply/drift/query
- Kafka duplicate/rebalance/ACK/reply loss
- Gateway/Batch/Scheduler/Agent/Deployment multi-instance와 process kill
- Unknown Result/Reconcile/Compensation
- BOM/Plugin/POM/Marker, nested Batch artifact, Frontend bundle, DB Pack, SBOM/ORT/Syft/Grype
- sourceSha=resultSha=최종 Commit, dirty=false

## 8. 완료 금지 조건
다음 중 하나라도 있으면 전체 완료가 아니다.

- Frontend fresh clone Build 실패
- ADM Route/Permission/API Matrix 미완성
- EDU 162 Requirement/Public API Coverage 미완성
- 외부 Fixture/Command가 재현 불가
- QA34/QA35 미검증 행 존재
- CI Status/Workflow 부재
- exact-SHA Runtime Evidence 부재
- Artifact Catalog 누락
- 발견 결함을 다음 QA로 단순 이월


## 9. 사용자 제공 ADM 최소 기능선 추가 기준

`CPF_20260801_QA35_ADM_SCREENSHOT_EVIDENCE_INDEX.csv`의 44개 화면과
`CPF_20260801_QA35_ADM_LEGACY_MINIMUM_CAPABILITY_MATRIX.csv`의 87개 Capability를
QA35의 필수 수용 기준으로 추가한다.

- 화면을 그대로 복제하지 않는다.
- 기능·업무 결과·운영 제어·분석 능력은 최소한 모두 제공한다.
- CPF는 Online·Batch 통합, MSA/동일 JVM, 다중 인스턴스, 부분 실패,
  결과불명·Reconcile, Security·Approval·Audit·Masking·Evidence에서 반드시 상회한다.
- 기존 Route 이름이 유사해도 실제 API Consumer, 검색·Paging·상세·오류,
  권한·감사·Runtime Evidence가 없으면 충족으로 판정하지 않는다.
- `QA35-REQ-036`~`QA35-REQ-055`를 별도 추가 작업이 아니라 ADM P0 필수 범위로 수행한다.
