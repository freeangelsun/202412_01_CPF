# CPF ADM Commercial Control Plane UI·IA 상세 개발요건

> 성격: Developer GPT 다음 체크포인트용 **상세 실행 Requirement / 자체검수 기준**  
> Canonical Requirement ID 신규 추가: **0건**  
> 강화 대상: `ADM-AUTH`, `ADM-RBAC`, `ADM-AUDIT`, `ADM-TX`, `ADM-TIMELINE`, `ADM-SERVICE`, `ADM-LOG`, `ADM-BATCH`, `ADM-CENTER`, `ADM-AGENT`, `ADM-EXS`, `ADM-RECOVERY`, `ADM-INCIDENT`, `ADM-UX`, `ADM-APPROVAL`  
> Currentization source/basis: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`)  
> 실행 시: 최신 `origin/master` exact SHA를 다시 확인한다.

---

## 0A. 18_19 ADM Source Audit — 확정 Gap

Target CSV 80 route 대비 최신 `cpf-admin/frontend/src/app/routes.ts`에는 64 route가 존재한다. 다음 16 route는 미구현이다.

```text
auth-session
external-institutions
incident-postmortem
audit-diff
ops-metrics
ops-slo
ops-alerts
ops-runbooks
ops-self-healing
ops-topology
ops-maintenance
ops-config
ops-drift
ops-capacity
ops-dr
security-cross-cut
```

기존 64 route의 `expectedOperationIds`는 Requirement CSV와 대조되지만, Route 존재만으로 완료가 아니다.

또한 최신 Navigation Group은 6개(`home`, `online`, `batch`, `integration`, `monitoring`, `framework`)로 남아 있어 Target 8개 IA와 불일치한다. 다음 8개 Top Menu Group으로 재구성하고 모든 80 route를 배치한다.

1. 운영현황
2. 거래·실행 추적
3. 서비스·연계·Gateway
4. Batch·대량처리
5. 로그·감사·Incident
6. 복구·변경·배포
7. 기준정보·플랫폼설정
8. 보안·권한·승인

### 운영자 관리 강화

운영자 목록/등록/상세·수정은 최소 다음 필드를 실제 Backend/DB/OpenAPI/Generated Client/UI까지 연결한다.

- operator ID, 이름, 부서, 직위/직책/담당
- email, mobile, office phone(선택), emergency/ops contact(필요 시)
- active/inactive/locked, 사용 시작/종료/계정 만료, 설명
- 상세 Tab: Overview / Role-Permission / Authentication(MFA/OIDC/SSO/password/IP allowlist) / Session / Audit
- 목록의 email/mobile은 masking; 원문 조회는 별도 권한 + 사유 + Audit
- 연락처는 Incident/Security/Approval/Break-glass/Owner/Batch/Gateway/Service notification의 실제 Consumer와 연결
- Super Admin/critical permission, deactivate, lock/unlock, MFA 제거, all-session revoke, break-glass assignment는 Impact/Diff→Reason→Approval/SoD→Confirm→Result→Audit→Recovery
- 자기 자신 잠금/마지막 관리자 제거 방지

Browser/E2E는 80 route 전수, 실제 Generated Client/Backend API, 401/403/404/409/429/500/503, Desktop/Narrow, accessibility, screenshot/evidence를 최신 exact SHA에서 수행한다.

## 1. 제품 목표

ADM은 CPF의 부가 화면이 아니라 **Core Platform Framework의 간판 운영 Control Plane**이다.

기능이 Backend에 존재하는 것만으로 상용 Framework가 되지 않는다. 운영자가 장애를 찾고, 거래를 추적하고, Batch를 제어하고, 설정 변경의 영향을 확인하고, 승인된 복구를 실행하고, 결과와 Audit을 남길 수 있어야 한다.

ADM 완료는 다음 흐름이 실제로 연결되는 상태다.

```text
알림/이상징후
→ Dashboard/Incident
→ Transaction/Execution/Instance 식별
→ Timeline/Log/Audit 분석
→ 원인 Service/Gateway/Batch/External/Config 확인
→ 영향 Preview
→ 승인/SoD/Reason
→ Owner Command 실행
→ 결과/UNKNOWN/Reconcile/Recovery
→ Audit/Evidence
```

**UI가 복잡하거나 메뉴가 기술구현 단위로 난립하면 기능이 많아도 실패로 판정한다.**

---

## 2. 이번 상세화의 근거

### 2.1 Canonical ADM 15축

최신 정본의 ADM 요구는 다음 15축이다.

1. `ADM-AUTH` — 운영자 Identity, Password/MFA/OIDC, JDBC Session, fixation/concurrency/revocation/force logout, fail-closed
2. `ADM-RBAC` — Menu/Button/API/Command Permission, Role Mapping, 유효기간, Organization Context, Server Enforcement, Cache Invalidation
3. `ADM-AUDIT` — Actor/Target, Before/After Masked Snapshot, Reason, Approval, Result, TransactionId, Immutable/Tamper-evident
4. `ADM-TX` — Online/Async/Batch/External Transaction 검색, Header, Masking, Segment/Attempt
5. `ADM-TIMELINE` — Local/Remote/Event/Batch/Gateway/File/Agent Timeline
6. `ADM-SERVICE` — Service/Endpoint/Instance/Health/Version/Zone/Routing/Maintenance/Drain
7. `ADM-LOG` — File/DB Log, Saved Search, Trace Boost, Dynamic Level, Retention, Download Guard
8. `ADM-BATCH` — Job Definition/Execution/Step/Checkpoint/Restart/Stop/Recover/Spring Batch ID
9. `ADM-CENTER` — Center-Cut Job/Item/Attempt/Progress/Reprocess/UNKNOWN/Compensation
10. `ADM-AGENT` — Agent/Runner/Worker Registry, Capability, Heartbeat, Artifact, Process, Drain/Takeover
11. `ADM-EXS` — 외부기관 Endpoint/Health/Credential·Certificate/Request·Response Timeline/UNKNOWN/Reconcile
12. `ADM-RECOVERY` — Unknown/DLQ/Saga/Deployment/File/Batch Runbook, Recover/Compensate/Reconcile
13. `ADM-INCIDENT` — Alert→Incident→Severity/Owner→Runbook/Action→Postmortem/Closure
14. `ADM-UX` — 대량검색/Paging/Sort/Filter/Saved Condition/상태/Responsive/Keyboard/A11y/Safe Download
15. `ADM-APPROVAL` — Versioned Policy, ALL/ANY/N_OF_M, SoD, Expiry, Break-glass, Immutable Command Hash, Owner Command

### 2.2 현재 CPF Source

`10_15`의 `cpf-admin/frontend/src/app/routes.ts`에는 **64개 실제 ADM Route**가 있으며 각 Route가 `expectedOperationIds`로 OpenAPI Operation과 연결된다.

현재 Group은 `home / online / batch / integration / monitoring / framework` 6개다.  
다음 체크포인트에서는 64 Route를 지우거나 단순 이동하는 것이 아니라 **운영업무 관점 IA로 재분류하고 중복·Dead Route·기술명 노출을 제거**한다.

### 2.3 사용자 제공 기존 운영화면 참고자료 44장

실제 확인한 압축 구성:

- Batch 화면 15장
- Online/System/Common/Batch/Analysis 화면 29장
- 합계 44장

Reference에서 확인된 운영 패턴:

**System**
- 사용자 관리
- 권한 관리
- 메뉴 관리
- 공지사항
- 로그인 이력
- 사용 이력
- 시스템 로그
- DB Schema

**Common**
- Framework Node 관리
- Parameter
- Log Level/Pattern
- Message
- Code Group/Code
- 통지 설정/수신자
- Framework Node/DataSource/Cache/Log/TCP Monitoring
- Log File Download
- Event Log

**Online**
- 거래 목록
- 거래 통제
- 거래 전/후처리
- 거래 Profile/속성
- Deferred 실행규칙
- DBIO
- Component/XIO 배포 현황
- 서비스 처리량/Thread/Deferred Monitoring
- 서비스 처리 흐름
- 거래/Error/Deferred/전문/통합 Log
- 거래 통계/전문 처리시간 통계
- Log 조회 설정

**Batch**
- Job 등록정보
- Job Instance
- Job Group
- Job 등록요청/승인 상태
- Server/Agent
- Schedule/Calendar
- 전역 Parameter
- 통지
- Job Flow Diagram
- 실행상태/강제·재실행
- Report/Log/File Log

**Deployment/Center-Cut**
- Batch Parameter/Profile
- Center-Cut Profile
- 실행 Monitoring
- 실행 Log

**Analysis**
- Component 분석
- SQL 분석
- XIO/FIO 분석
- 영향도 분석

이 Reference는 **기능 누락 방지용 Benchmark**다. 디자인을 복제하지 않는다. 기존 화면보다 메뉴를 명확하게 하고, Transaction Timeline, 권한, Audit, Recovery, 오류 UX, Responsive, Accessibility를 강화한다.

---



---

## 2.4 ADM은 `ADM-*`만이 아니라 `OPS-*` Control Plane까지 제품 범위다

CPF 정본에서 `cpf-admin control plane + runtime owner`가 Owner인 아래 운영 Requirement는 ADM의 별도 필수 Coverage다.

1. `OPS-METRIC` — Transaction/Service/Instance/DB/Broker/Batch/Gateway/Agent bounded-cardinality Metric, Dashboard/Export
2. `OPS-SLO` — Availability/Latency/Error/Freshness/Backlog/Recovery SLI·SLO, Error Budget, Burn-rate
3. `OPS-ALERT` — Dedup/Grouping/Inhibition/Severity/Routing/Escalation/Maintenance Suppression/Acknowledgement
4. `OPS-INCIDENT` — Incident Lifecycle, Commander/Owner, Communication, Timeline, Evidence, Action Item, Problem Linkage
5. `OPS-RUNBOOK` — 탐지조건·영향·진단·안전조치·Rollback·Escalation·검증·종결기준을 실행 가능한 Runbook으로 제공
6. `OPS-SELF` — 자동진단/자동복구 Allowlist, Rate/Attempt Limit, Circuit Stop, Approval Boundary, Rollback, Immutable Audit
7. `OPS-TOPOLOGY` — Service/Instance/Dependency/Domain/Owner/Database/Broker/Endpoint의 Versioned Topology/Service Catalog
8. `OPS-MAINT` — Maintenance, Admission Block, Drain/Quiesce, In-flight Deadline, Health/Routing 반영, Resume, Audit
9. `OPS-CONFIG` — Runtime Config Catalog, Schema, Encryption, Version, Staged Rollout, Approval, Dynamic Apply, Rollback, Drift Detection
10. `OPS-DRIFT` — Source/Artifact/Config/DB/Route/Permission/Runtime Version의 Desired-Actual Drift 탐지·차단·복구
11. `OPS-CAPACITY` — CPU/Memory/Thread/Connection/Queue/Storage/DB/Broker Capacity, Threshold, Trend, Forecast, Load-test 기준
12. `OPS-DR` — RTO/RPO, Multi-zone/Site, Backup/Restore, Failover/Failback, Data Consistency, Runbook, DR Drill

따라서 다음 개발의 ADM 완료 기준은:

```text
ADM Canonical 15축
+ OPS Control Plane 12축
+ ADM에서 노출되는 Security Cross-cut
+ 현재 실제 Route/API
+ Legacy Enterprise 운영기능 Benchmark
```

이다.

### OPS 화면 배치 원칙

기존 8개 Top-Level IA를 유지하되 다음 Submenu/Tab을 명시적으로 제공하거나 기존 화면에서 완전 Coverage됨을 증명한다.

```text
01 운영현황
├─ 통합 Dashboard
├─ Metrics
├─ SLO / Error Budget
├─ Capacity / Trend / Forecast
└─ Versioned Topology / Dependency

05 로그·감사·Incident
├─ Alert Center
├─ Incident
├─ Runbook
└─ Evidence / Action Item / Problem Linkage

06 복구·변경·배포
├─ Recovery Center
├─ Automated Diagnosis / Self-healing
├─ Desired-Actual Drift
├─ Maintenance / Drain
└─ DR / Backup / Failover / Drill

07 기준정보·플랫폼설정
└─ Runtime Config
   ├─ Schema / Version
   ├─ Staged Rollout
   ├─ Dynamic Apply
   ├─ Rollback
   └─ Drift
```

전용 Page가 반드시 필요한 것은 아니다. 그러나 **해당 Requirement의 정보·Action·권한·오류·Audit·E2E가 기존 화면에서 완전 Coverage됨을 Matrix로 증명하지 못하면 전용 Page/Tab을 구현한다.**

### `OPS-METRIC` UI

- Transaction / Service / Instance / DB / Broker / Batch / Gateway / Agent 단위 Metric
- bounded-cardinality 정책
- 기간 선택
- Auto Refresh
- Rate / Count / Gauge / Histogram 의미 구분
- p50 / p95 / p99 등 Latency
- Success / Error / Timeout
- Queue / Backlog
- DB Connection / Pool / Slow Query 관련 운영 Metric
- Broker Lag / Queue depth
- Batch Throughput / Failure
- Gateway latency/error
- Agent heartbeat/capacity
- Dashboard→대상 상세 Drill-down
- Export
- Metric label 폭증이나 민감 label을 UI에서도 경고

### `OPS-SLO` UI

- Service/Capability별 SLI/SLO
- Availability
- Latency
- Error
- Freshness
- Backlog
- Recovery
- 목표값 / 현재값
- Error Budget
- Burn Rate
- 기간
- 위반 구간
- 관련 Incident/Transaction Drill-down
- SLO 변경은 Version/Reason/Approval/Audit

### `OPS-ALERT` UI

- Alert 목록/상세
- Severity
- Source
- First/Last seen
- Count
- Dedup Group
- Inhibition 상태
- Routing
- Escalation
- Maintenance Suppression
- Acknowledge
- Incident 생성/연결
- Related Runbook
- Alert Storm에서 UI가 무너지지 않는 Paging/Grouping

### `OPS-RUNBOOK` UI

Runbook을 단순 Markdown 링크로 완료하지 않는다.

- Trigger 조건
- 영향
- 진단 단계
- 안전조치
- 위험 Action
- Rollback
- Escalation
- Verification
- Closure criteria
- Owner
- Version
- Required Permission
- Approval
- 실행 결과 기록

가능한 Step은 Owner Command API와 연결하고, 자동화 불가 Step은 체크/증적을 남긴다.

### `OPS-SELF` UI

자동복구는 반드시 제한적으로 제공한다.

- Allowlist
- 대상 Scope
- Trigger
- Max Attempt
- Rate Limit
- Circuit Stop
- Approval Boundary
- Dry-run/Preview
- Rollback
- 현재 자동복구 상태
- 최근 실행
- 성공/실패/UNKNOWN
- Audit
- Kill Switch

### `OPS-TOPOLOGY` UI

Topology는 단순 Instance 점 목록이 아니다.

- Domain
- Service
- Endpoint
- Instance
- Owner
- Version
- Zone
- Database
- Broker
- External endpoint
- Dependency
- Health
- Current traffic/routing
- Versioned Snapshot
- Dependency Click→관련 Health/Metric/Transaction/Incident

### `OPS-MAINT` UI

- Maintenance Window
- Target
- Admission Block
- Drain / Quiesce
- In-flight count
- Deadline
- Routing 반영
- Health 변화
- Resume
- Reason
- Approval
- 결과
- Audit

### `OPS-CONFIG` UI

Config CRUD만으로 완료하지 않는다.

- Config Schema
- Type / Range / Secret 여부
- Environment/Service/Instance Scope
- Current desired value
- Actual runtime value
- Version
- 변경 Diff
- Staged rollout
- Canary/Batch 적용 단위
- Dynamic Apply
- 적용 Instance 결과
- Partial Failure
- Rollback
- Drift
- Approval/Audit

### `OPS-DRIFT` UI

Desired / Actual을 다음 축으로 비교한다.

- Source SHA
- Artifact Version/Checksum
- Config Version
- DB Schema/Migration Version
- Route/Gateway
- Permission Version
- Runtime Version

기능:
- Drift Severity
- 영향
- 발견 시각
- 대상 Instance
- Expected / Actual Diff
- 차단 여부
- Reconcile/Recover
- Audit

Legacy의 DB Schema 관리 화면은 **운영자가 임의 DDL을 편집하는 화면으로 복제하지 않는다.**
대신 Canonical Schema Version / Migration / Drift / Readiness를 읽기·진단 중심으로 노출하고 Schema 변경은 공식 Migration 경로를 사용한다.

### `OPS-CAPACITY` UI

- CPU
- Memory
- Heap
- Thread
- Connection
- Queue
- Storage
- DB
- Broker
- Batch worker
- Gateway
- Agent

각각:
- Current
- Threshold
- Trend
- Forecast
- Peak
- Saturation
- 관련 Alert
- Capacity Risk
- Load-test baseline

### `OPS-DR` UI

- RTO / RPO
- Site / Zone
- Replication state
- Backup status
- Last successful restore validation
- Failover readiness
- Failback readiness
- Data consistency check
- DR Runbook
- 정기 Drill 일정/결과
- 실제 Failover/Restore 같은 위험조치는 강한 승인·사유·Evidence를 요구

---

## 2.5 Security Cross-cut을 ADM UI 완료조건에 명시

ADM이 보안 계약의 Owner를 침범하지 않되, 운영자가 사용하는 Projection/Command UI는 관련 Security Requirement를 끝까지 반영해야 한다.

### `SEC-DOWNLOAD`

민감/대량 Download UI:
- Permission
- Reason
- Approval
- Watermark
- Row/Size Limit
- Expiry
- Encryption
- One-time Link
- Audit

### `SEC-PRIVACY`

- Masked View 기본
- Raw Access 별도 Permission
- 목적/사유
- Retention/Deletion 상태
- Export 정책
- Raw Access Audit

### `SEC-CERT` / `SEC-SECRET`

- Provider Health
- Key/Certificate ID·Version Metadata
- Expiry
- Rotation
- Revocation
- mTLS 상태
- Private Key/Secret 원문 절대 비노출
- 실패/Timeout 상태
- Audit

### `SEC-AUDIT`

ADM Audit 화면은 Hash-chain/Tamper detection 결과를 이해 가능하게 표시하고:
- Mutation
- Delete
- Reorder
- Signature Verification(사용 시)
- Multi-instance consistency
를 Evidence로 검증한다.

### `SEC-APPROVAL`

- Dual Control
- 자기승인 금지
- Immutable Target/Command Hash
- Expiry
- Break-glass
- 사후 Review
를 `ADM-APPROVAL` UI와 일관되게 제공한다.


## 3. ADM 최종 권장 Top-Level IA

Top-Level 메뉴는 **8개 전후**를 기본 목표로 한다. 기능 64개를 1Depth에 나열하지 않는다.

```text
ADM
├─ 01 운영현황
│  ├─ 통합 운영 Dashboard
│  ├─ 서비스 토폴로지
│  ├─ Runtime Instance Health
│  └─ Online Runtime Diagnostics
│
├─ 02 거래·실행 추적
│  ├─ 통합 Transaction Timeline
│  ├─ 온라인 거래 정의
│  ├─ 표준 실행
│  └─ 거래 로그
│
├─ 03 서비스·연계·Gateway
│  ├─ 서비스 레지스트리
│  ├─ 채널 정책
│  ├─ Gateway
│  │  ├─ Dashboard
│  │  ├─ 연동 서버/그룹
│  │  ├─ Route/Binding
│  │  ├─ 보안·제한
│  │  ├─ Health·연결시험
│  │  ├─ 거래
│  │  ├─ 로그 정책
│  │  └─ 적용 상태·이력
│  ├─ 외부기관 관제
│  ├─ 전문·Protocol Message
│  ├─ 알림
│  └─ 다운로드
│
├─ 04 Batch·대량처리
│  ├─ Batch Overview
│  ├─ Job Definition / Job Pack
│  ├─ Scheduler
│  ├─ Execution / Step / Checkpoint
│  ├─ Runtime Instance
│  ├─ Worker Pool / Agent
│  ├─ Center-Cut
│  ├─ 대량파일 Job
│  ├─ Lease / Fencing
│  ├─ Alert
│  └─ Audit / Evidence
│
├─ 05 로그·감사·Incident
│  ├─ 원격 로그
│  ├─ 감사 로그
│  ├─ 동적 로그
│  ├─ 로그 정책
│  ├─ Incident
│  ├─ Reliability Analysis
│  └─ 변경 전·후 Audit Diff
│
├─ 06 복구·변경·배포
│  ├─ 복구 센터
│  ├─ Batch Recovery / Unknown
│  ├─ Deployment·Promotion·Rollback
│  ├─ Batch Deployment
│  ├─ 점검·Drain
│  └─ 통합 운영 정정 승인
│
├─ 07 기준정보·플랫폼설정
│  ├─ Config / Parameter
│  ├─ Code
│  ├─ Response Code
│  ├─ 영업일·휴일
│  ├─ Cache
│  ├─ Feature Flag
│  ├─ Resilience Policy
│  └─ OpenAPI 운영
│
└─ 08 보안·권한·승인
   ├─ 운영자
   ├─ 인증·세션
   ├─ Permission / Role
   ├─ Password
   ├─ MFA / IP Allowlist
   ├─ Secret / Key
   ├─ 위험조치 승인
   └─ Break-glass
```

### IA 강제 원칙

- `batch-worker-pools`, `batch-agents`, `workers`처럼 Source Feature가 나뉘어 있어도 **사용자 메뉴는 중복을 최소화**한다.
- Gateway 관련 8개 Route는 별도 Top-Level 8개가 아니라 `서비스·연계·Gateway > Gateway` 아래에 둔다.
- Recovery/Incident/Audit는 서로 다른 책임이므로 한 페이지에 전부 섞지 않는다.
- 위험 Command는 조회 화면의 기본 CTA처럼 보이지 않게 별도 Action 영역/Drawer로 분리한다.
- 장애 대응 경로는 3클릭 이내를 목표로 한다.
- Breadcrumb와 Context Link를 유지한다.
- URL 직접 접근도 Permission을 통과해야 한다.

---

## 4. 모든 ADM 화면의 기능 설명 UX

사용자가 요청한 **“기능 설명이 있는 UI”**를 제품 Requirement로 둔다.

### 4.1 Page Header

모든 주요 Page 상단에 다음을 제공한다.

- 화면명
- 1~2문장 기능 설명
- 현재 Scope: System / Environment / Instance / Tenant 등
- 데이터 기준시각 또는 마지막 Refresh
- Auto Refresh 여부
- Help 버튼
- 관련 Canonical Capability 또는 사용자 친화적인 Capability 이름

예:

> **통합 Transaction Timeline**  
> TransactionId 하나로 온라인, 외부연계, Event, Batch, Gateway, File 실행을 시간순으로 추적합니다. 실패 Segment를 선택하면 관련 로그와 복구상태로 이동할 수 있습니다.

### 4.2 Help Panel

Help에는 다음을 포함한다.

- 이 화면에서 할 수 있는 일
- 주요 검색 키
- 상태값 의미
- 데이터가 갱신되는 방식
- 필요한 권한
- 위험조치가 있는 경우 영향 범위
- 관련 화면
- 오류/UNKNOWN 발생 시 권장 운영경로
- 민감정보 Masking 정책

### 4.3 Field/Column Help

기술 약어가 있는 Column은 Tooltip 또는 Help Dictionary를 제공한다.

예:
- TransactionId
- Business TransactionId
- TraceId
- Attempt
- Fencing Token
- Checkpoint
- UNKNOWN
- Reconcile
- Drain
- SoD

단, 모든 Cell에 Tooltip을 남발하지 않는다.

### 4.4 위험조치 설명

HIGH/CRITICAL Action은 클릭 시 최소 다음을 보여준다.

```text
무엇을 변경/실행하는가
대상은 무엇인가
예상 영향은 무엇인가
즉시 반영인가
Rollback/Recovery 가능한가
필요 Permission
Approval 정책
Reason 입력
Command Hash / Request ID
```

---

## 5. 공통 List UI 상세기준

Reference 화면의 장점인 **조건검색→고밀도 목록→상세**는 유지하되 현대화한다.

### Search Area

- 기본조건 3~6개
- 고급검색 접기/펼치기
- 기간 Preset
- 상태 Multi-select
- System/Instance/Channel
- TransactionId/ExecutionId 직접검색
- 검색조건 초기화
- Saved Search
- URL Query/Bookmark 가능
- Enter 검색
- 최근검색 선택 옵션

### Table

- Server-side Paging
- Server-side Sort
- Total Count
- Column Resize
- 중요 Column 고정
- Column Show/Hide
- 긴 ID Copy
- Status Badge
- Severity
- Duration formatting
- 날짜/시간 timezone 명확화
- Empty
- Skeleton/Loading
- Partial Failure
- Selection
- Bulk Action은 안전정책 있는 경우만 제공

### Download

- 현재 검색조건 그대로 Export
- 예상 건수/용량
- 최대건수
- Masking
- Reason/Permission
- Async Export Job
- Download Audit
- 만료 Token
- 재다운로드 정책

---

## 6. 상세/Drill-down 표준

상세화면은 Legacy의 긴 단일 Form보다 다음 구조를 우선한다.

```text
Summary
├─ 상태/핵심 ID/시간/Owner
├─ 핵심 Metadata
├─ Timeline
├─ Related Resources
├─ Log/Audit
└─ Actions
```

Tab 예:

- Overview
- Timeline
- Header/Context
- Request/Response
- Steps/Attempts
- Logs
- Audit
- Recovery

관련 ID는 Copy와 Navigate를 제공한다.

---

## 7. ADM Route별 상세 개발 Requirement

### 01 운영현황

#### `capacity` — Online Runtime Diagnostics (MEDIUM)

**화면 목적**  
온라인 Runtime의 Health/Status와 Outbox/Inbox/Idempotency/File 처리 현황을 함께 보고 병목·적체 징후를 진단.

**현재 계약 연결**  
- Path: `/capacity`
- Canonical: `ADM-SERVICE, ADM-UX`
- Expected OpenAPI Operation: 6개
- Operation 예: `admRuntimeControlFindHealth, admRuntimeControlFindStatus, findAdmBrokerOutbox, findAdmBrokerInbox, findAdmIdempotencyRecords, findAdmFileTransferHistory`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `health-instances` — Runtime Instance Health (MEDIUM)

**화면 목적**  
Runtime Instance별 Readiness/Liveness, 최근 상태변경, 기본 진단정보를 조회하여 개별 인스턴스 장애를 빠르게 식별.

**현재 계약 연결**  
- Path: `/health-instances`
- Canonical: `ADM-SERVICE`
- Expected OpenAPI Operation: 2개
- Operation 예: `admHealthInstanceList, admHealthInstanceDetail`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `topology` — 서비스 토폴로지 (MEDIUM)

**화면 목적**  
서비스→Endpoint→Instance의 배치 위치, 버전, Zone, Health, Routing 관계를 시각화하고 장애 Instance와 영향 서비스를 즉시 식별.

**현재 계약 연결**  
- Path: `/topology`
- Canonical: `ADM-SERVICE`
- Expected OpenAPI Operation: 4개
- Operation 예: `admServiceRegistryFindServices, admServiceRegistryFindInstances, admServiceRegistryFindEndpoints, admServiceRegistryFindHealth`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `dashboard` — 통합 운영 Dashboard (MEDIUM)

**화면 목적**  
플랫폼 전체 상태를 한 화면에서 요약하고 장애·Unknown·DLQ·Batch 실패·Readiness/Liveness를 업무 흐름별 상세 화면으로 Drill-down하는 운영 시작점.

**현재 계약 연결**  
- Path: `/`
- Canonical: `ADM-UX, ADM-SERVICE, ADM-INCIDENT`
- Expected OpenAPI Operation: 9개
- Operation 예: `admRuntimeControlFindHealth, admRuntimeControlFindStatus, findAdmUnknownResults, findAdmBrokerDlq, findAdmBrokerOutbox, admBatchFindExecutionPage` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 02 거래·실행 추적

#### `transactionGroups` — Online·Batch 통합 Trace (MEDIUM)

**화면 목적**  
TransactionId/TraceId/BusinessTransactionId 하나로 Online·Remote·Event·Batch·Gateway·File Segment와 Attempt를 시간순 Timeline으로 재구성.

**현재 계약 연결**  
- Path: `/transactionGroups`
- Canonical: `ADM-TX, ADM-TIMELINE`
- Expected OpenAPI Operation: 9개
- Operation 예: `traceAdmByTransactionId, traceAdmByTraceId, traceAdmByBusinessTransactionId, admTransactionGroupFindTimeline, admTransactionGroupFindGroups, admTransactionGroupFindDetail` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `logs` — 거래 로그 (MEDIUM)

**화면 목적**  
거래 로그를 조건검색하고 상세·Export·Download를 제공하되 TransactionId 연계와 Masking·Download Guard를 적용.

**현재 계약 연결**  
- Path: `/logs`
- Canonical: `ADM-LOG, ADM-TX`
- Expected OpenAPI Operation: 4개
- Operation 예: `admLogFindLogs, admLogGetLogDetail, admLogExportCreate, admLogExportDownload`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `transactions` — 온라인 거래 정의 (HIGH) **위험조치 화면**

**화면 목적**  
온라인 거래 Metadata/정의와 활성상태를 검색·조회하고 변경 영향과 안전한 비활성화를 관리.

**현재 계약 연결**  
- Path: `/transactions`
- Canonical: `ADM-TX`
- Expected OpenAPI Operation: 5개
- Operation 예: `admTransactionMetaFindPage, admTransactionMetaFindTransaction, admTransactionMetaInactivate, admTransactionMetaFindTransactions`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `standardExecutions` — 표준 실행 (MEDIUM)

**화면 목적**  
표준 실행 단위의 상태와 상세를 조회하여 Transaction/Batch/연계 실행과 연결.

**현재 계약 연결**  
- Path: `/standardExecutions`
- Canonical: `ADM-TX, ADM-TIMELINE`
- Expected OpenAPI Operation: 2개
- Operation 예: `admStandardExecutionFindAll, admStandardExecutionFindOne`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 03 서비스·연계·Gateway

#### `gateway-health` — Gateway Health·연결시험 (MEDIUM)

**화면 목적**  
연결시험 요청/취소/재검증과 결과 이력을 조회하여 Endpoint 연결성·인증·Timeout 문제를 진단.

**현재 계약 연결**  
- Path: `/gateway-health`
- Canonical: `ADM-SERVICE, ADM-EXS`
- Expected OpenAPI Operation: 7개
- Operation 예: `admGatewayCapability, admGatewayFindApplyStatus, admGatewayFindConnectionTests, admGatewayRequestConnectionTest, admGatewayFindConnectionTestOperation, admGatewayCancelConnectionTest` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-transactions` — Gateway 거래 조회 (MEDIUM)

**화면 목적**  
Gateway를 통과한 거래 Snapshot/Event를 조회하고 TransactionId Timeline으로 연결.

**현재 계약 연결**  
- Path: `/gateway-transactions`
- Canonical: `ADM-TX, ADM-TIMELINE, ADM-EXS`
- Expected OpenAPI Operation: 3개
- Operation 예: `admGatewayOperationsSnapshot, admGatewayOperationsEvents, traceAdmByTransactionId`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-routes` — Gateway 경로·라우팅 (MEDIUM)

**화면 목적**  
Binding/Route를 조회·등록·상태변경·삭제하며 적용 전 영향 Preview와 적용 결과를 추적.

**현재 계약 연결**  
- Path: `/gateway-routes`
- Canonical: `ADM-SERVICE, ADM-EXS`
- Expected OpenAPI Operation: 4개
- Operation 예: `admGatewayFindBindings, admGatewaySaveBinding, admGatewayChangeBindingState, admGatewayDeleteBinding`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-dashboard` — Gateway 대시보드 (MEDIUM)

**화면 목적**  
Gateway 상태·최근 Event·운영 Snapshot을 요약하고 연결시험/Route/거래로 Drill-down.

**현재 계약 연결**  
- Path: `/gateway-dashboard`
- Canonical: `ADM-SERVICE, ADM-EXS`
- Expected OpenAPI Operation: 4개
- Operation 예: `admGatewayCapability, admGatewayOperationsSnapshot, admGatewayOperationsEvents, admGatewayOperationsStream`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-log-policies` — Gateway 로그 정책 (MEDIUM)

**화면 목적**  
Gateway 로그 정책과 배포/분산 상태를 조회하여 정책 미반영 Instance를 식별.

**현재 계약 연결**  
- Path: `/gateway-log-policies`
- Canonical: `ADM-LOG, ADM-EXS`
- Expected OpenAPI Operation: 3개
- Operation 예: `admGatewayOperationsSnapshot, admLogPolicyFindPolicies, admLogPolicyDistributionStatus`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-security` — Gateway 보안·제한 (HIGH) **위험조치 화면**

**화면 목적**  
Gateway Binding의 보안·제한 정책을 관리하고 위험 변경은 승인·Audit·Rollback 가능성을 보장.

**현재 계약 연결**  
- Path: `/gateway-security`
- Canonical: `ADM-RBAC, ADM-EXS`
- Expected OpenAPI Operation: 3개
- Operation 예: `admGatewayFindBindings, admGatewaySaveBinding, admGatewayChangeBindingState`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-groups` — Gateway 서버 그룹 (MEDIUM)

**화면 목적**  
Gateway Server Group 구성과 Member를 관리하고 Routing 적용 영향과 상태를 확인.

**현재 계약 연결**  
- Path: `/gateway-groups`
- Canonical: `ADM-SERVICE, ADM-EXS`
- Expected OpenAPI Operation: 4개
- Operation 예: `admGatewayFindServerGroups, admGatewayFindGroupMembers, admGatewaySaveServerGroup, admGatewayDeleteServerGroup`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-servers` — Gateway 연동 서버 (MEDIUM)

**화면 목적**  
연동 서버/Server Group과 Member를 조회·편집하고 Target 상태·가용성을 관리.

**현재 계약 연결**  
- Path: `/gateway-servers`
- Canonical: `ADM-SERVICE, ADM-EXS`
- Expected OpenAPI Operation: 4개
- Operation 예: `admGatewayFindServerGroups, admGatewayFindGroupMembers, admGatewaySaveServerGroup, admGatewayDeleteServerGroup`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `gateway-apply-status` — Gateway 적용 상태·이력 (MEDIUM)

**화면 목적**  
Gateway 설정 적용상태·이력·연결시험 Operation을 모아 변경 결과와 실패 대상을 추적.

**현재 계약 연결**  
- Path: `/gateway-apply-status`
- Canonical: `ADM-AUDIT, ADM-EXS`
- Expected OpenAPI Operation: 3개
- Operation 예: `admGatewayFindApplyStatus, admGatewayOperationsEvents, admGatewayFindConnectionTestOperation`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `downloads` — 다운로드 (MEDIUM)

**화면 목적**  
Download Policy와 Download Audit를 조회하고 안전한 Export/CSV Download를 제공.

**현재 계약 연결**  
- Path: `/downloads`
- Canonical: `ADM-LOG, ADM-UX`
- Expected OpenAPI Operation: 3개
- Operation 예: `admDownloadFindPolicies, admDownloadFindDownloadAuditLogs, admDownloadDownloadCsv`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `serviceRegistry` — 서비스 레지스트리 (MEDIUM)

**화면 목적**  
서비스·Instance·Endpoint·Capability·Circuit·Routing Policy를 조회·관리하고 상태변경은 Owner Command와 Audit를 통해 수행.

**현재 계약 연결**  
- Path: `/serviceRegistry`
- Canonical: `ADM-SERVICE`
- Expected OpenAPI Operation: 15개
- Operation 예: `admServiceRegistryFindServices, admServiceRegistryFindInstances, admServiceRegistryFindEndpoints, admServiceRegistryFindHealth, admServiceRegistrySaveService, admServiceRegistrySaveInstance` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `notifications` — 알림 (MEDIUM)

**화면 목적**  
운영 알림 Rule, Delivery, Attempt, DLQ, Retry/Cancel/Test를 관리하여 장애 통지의 전달결과까지 추적.

**현재 계약 연결**  
- Path: `/notifications`
- Canonical: `ADM-INCIDENT`
- Expected OpenAPI Operation: 11개
- Operation 예: `admNotificationFindRules, admNotificationFindRule, admNotificationFindDeliveryLogs, admNotificationFindDeliveryAttempts, admNotificationSaveRule, admNotificationUpdateRule` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `messages` — 전문·Protocol Message (MEDIUM)

**화면 목적**  
전문/Protocol Message 정의를 조회·등록·수정·삭제하고 실제 Transaction Trace와 연결.

**현재 계약 연결**  
- Path: `/messages`
- Canonical: `ADM-TX, ADM-EXS`
- Expected OpenAPI Operation: 6개
- Operation 예: `admMessageFindMessages, admMessageFindMessage, admMessageCreateMessage, admMessageUpdateMessage, admMessageDeleteMessage, traceAdmByTransactionId`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `channelPolicy` — 채널 정책 (HIGH) **위험조치 화면**

**화면 목적**  
채널별 실행/허용 정책 Snapshot을 조회·편집·Import/Export하고 Runtime 반영상태와 충돌을 확인.

**현재 계약 연결**  
- Path: `/channelPolicy`
- Canonical: `ADM-SERVICE, ADM-AUDIT`
- Expected OpenAPI Operation: 6개
- Operation 예: `admChannelFindSnapshot, admChannelRefreshSnapshot, admChannelSave, admChannelSaveExecutionPolicy, admChannelExportPackage, admChannelImportPackage`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 04 Batch·대량처리

#### `workers` — Agent / Worker (MEDIUM)

**화면 목적**  
Agent/Worker/Instance 관계와 가용상태를 단순 운영뷰로 제공.

**현재 계약 연결**  
- Path: `/workers`
- Canonical: `ADM-AGENT`
- Expected OpenAPI Operation: 3개
- Operation 예: `admBatchFindWorkers, admBatchFindInstances, admBatchRuntimeInstances`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-audit` — Audit / Evidence (MEDIUM)

**화면 목적**  
Batch Operation Log와 Audit/Evidence를 조회하여 누가 어떤 위험조치를 언제 수행했는지 추적.

**현재 계약 연결**  
- Path: `/batch-audit`
- Canonical: `ADM-AUDIT, ADM-BATCH`
- Expected OpenAPI Operation: 5개
- Operation 예: `admBatchWorkbenchRecovery, admBatchFindOperationLogs, admAuditLogFindAuditLogs, admAuditDeliveryList, getAdmBatchJobInstanceLog`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch` — Batch / Center-Cut (MEDIUM)

**화면 목적**  
Job/Schedule/Execution/Instance/Worker/Relation을 한 업무 Workbench에서 연결하고 Job 등록과 즉시 실행까지 지원.

**현재 계약 연결**  
- Path: `/batch`
- Canonical: `ADM-BATCH, ADM-CENTER`
- Expected OpenAPI Operation: 12개
- Operation 예: `admBatchFindJobs, admBatchFindSchedules, admBatchFindExecutionPage, admBatchFindInstances, admBatchFindWorkers, admBatchWorkbenchJobDetail` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-alerts` — Batch Alerts (MEDIUM)

**화면 목적**  
Batch Unknown/DLQ/Outbox/Operation Log에서 운영 Alert를 모아 장애 대응으로 연결.

**현재 계약 연결**  
- Path: `/batch-alerts`
- Canonical: `ADM-INCIDENT, ADM-BATCH`
- Expected OpenAPI Operation: 4개
- Operation 예: `findAdmUnknownResults, findAdmBrokerDlq, findAdmBrokerOutbox, admBatchFindOperationLogs`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-overview` — Batch Overview (MEDIUM)

**화면 목적**  
Job·Schedule·Execution·Instance·Worker·Lock 핵심 상태를 요약하는 Batch 운영 Dashboard.

**현재 계약 연결**  
- Path: `/batch-overview`
- Canonical: `ADM-BATCH`
- Expected OpenAPI Operation: 7개
- Operation 예: `admBatchWorkbenchOverview, admBatchFindJobs, admBatchFindSchedules, admBatchFindExecutionPage, admBatchFindInstances, admBatchFindWorkers` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-center-cut` — Center-Cut (MEDIUM)

**화면 목적**  
Center-Cut Job/Result/Target/Parameter/Attempt를 조회하고 Failed Reprocess와 UNKNOWN Reconcile을 수행.

**현재 계약 연결**  
- Path: `/batch-center-cut`
- Canonical: `ADM-CENTER`
- Expected OpenAPI Operation: 9개
- Operation 예: `admCenterCutFindSummary, admCenterCutFindJobs, admCenterCutFindJobDetail, admCenterCutFindResults, admCenterCutFindResultDetail, admCenterCutFindTargets` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-executions` — Executions (MEDIUM)

**화면 목적**  
Execution/Step 상세, Progress, 시작·종료·소요시간, Retry/Stop과 실패 위치를 제공.

**현재 계약 연결**  
- Path: `/batch-executions`
- Canonical: `ADM-BATCH`
- Expected OpenAPI Operation: 7개
- Operation 예: `admBatchWorkbenchExecutionDetail, admBatchWorkbenchExecutions, admBatchFindExecutionPage, admBatchFindExecutionDetail, admBatchFindStepExecutions, admBatchRetryExecution` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-agents` — Host Agents (MEDIUM)

**화면 목적**  
Host Agent Registry, Heartbeat, Capability, Runtime 상태를 조회하고 Drain/Takeover 관련 명령을 관리.

**현재 계약 연결**  
- Path: `/batch-agents`
- Canonical: `ADM-AGENT`
- Expected OpenAPI Operation: 5개
- Operation 예: `admBatchWorkbenchInfrastructure, admBatchRuntimeInstances, admBatchRuntimeView, admBatchRuntimeCommand, admBatchRuntimeCommandState`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-job-packs` — Job Packs (MEDIUM)

**화면 목적**  
Job Definition/Pack의 목록·상세·Validate·Save·State Transition을 관리하고 실행 Job과 연결.

**현재 계약 연결**  
- Path: `/batch-job-packs`
- Canonical: `ADM-BATCH`
- Expected OpenAPI Operation: 8개
- Operation 예: `admBatchWorkbenchJobDetail, admBatchWorkbenchJobs, admBatchJobDefinitions, admBatchJobDefinitionDetail, admBatchJobDefinitionValidate, admBatchJobDefinitionSave` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-leases` — Lease / Fencing (MEDIUM)

**화면 목적**  
Lease/Lock/Fencing 상태와 Ghost 위험을 조회하고 안전한 Lock Release를 지원.

**현재 계약 연결**  
- Path: `/batch-leases`
- Canonical: `ADM-BATCH, ADM-RECOVERY`
- Expected OpenAPI Operation: 4개
- Operation 예: `admBatchWorkbenchRecovery, admBatchRuntimeView, admBatchFindLocks, admBatchReleaseLock`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-instances` — Runtime Instances (MEDIUM)

**화면 목적**  
Batch Instance 목록·상세와 Runtime View를 제공하고 Host/Version/Heartbeat/상태를 확인.

**현재 계약 연결**  
- Path: `/batch-instances`
- Canonical: `ADM-BATCH, ADM-AGENT`
- Expected OpenAPI Operation: 4개
- Operation 예: `admBatchWorkbenchInfrastructure, admBatchFindInstances, admBatchRuntimeInstances, admBatchRuntimeView`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-runtime` — Runtime Topology (HIGH) **위험조치 화면**

**화면 목적**  
Batch Runtime Topology와 Instance 상태를 보고 Drain/Command 등 Runtime 제어를 승인된 절차로 수행.

**현재 계약 연결**  
- Path: `/batch-runtime`
- Canonical: `ADM-BATCH, ADM-AGENT`
- Expected OpenAPI Operation: 4개
- Operation 예: `admBatchRuntimeInstances, admBatchRuntimeView, admBatchRuntimeCommand, admBatchRuntimeCommandState`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-scheduler` — Scheduler HA (MEDIUM)

**화면 목적**  
Schedule 조회, 시뮬레이션, Enable/Disable, Run Once와 Scheduler HA 상태를 관리.

**현재 계약 연결**  
- Path: `/batch-scheduler`
- Canonical: `ADM-BATCH`
- Expected OpenAPI Operation: 6개
- Operation 예: `admBatchWorkbenchSchedules, admBatchFindSchedules, admBatchSimulateSchedule, admBatchEnableSchedule, admBatchDisableSchedule, admBatchRunSchedulerOnce`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-worker-pools` — Worker Pools (MEDIUM)

**화면 목적**  
Worker Pool/Worker 상태와 처리 Capability를 조회하고 Runtime Command 상태를 추적.

**현재 계약 연결**  
- Path: `/batch-worker-pools`
- Canonical: `ADM-BATCH, ADM-AGENT`
- Expected OpenAPI Operation: 5개
- Operation 예: `admBatchWorkbenchInfrastructure, admBatchFindWorkers, admBatchRuntimeView, admBatchRuntimeCommand, admBatchRuntimeCommandState`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `file-jobs` — 대량파일 Job (MEDIUM)

**화면 목적**  
대량파일 Job의 Upload→Apply→Retry/Cancel→Rollback→UNKNOWN Resolve→Artifact 조회 전체 Lifecycle을 제공.

**현재 계약 연결**  
- Path: `/file-jobs`
- Canonical: `ADM-TIMELINE, ADM-RECOVERY`
- Expected OpenAPI Operation: 10개
- Operation 예: `admFileJobList, admFileJobDetail, admFileJobRows, admFileJobUpload, admFileJobApply, admFileJobRetry` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 05 로그·감사·Incident

#### `reliability` — Analysis Center (MEDIUM)

**화면 목적**  
Unknown/DLQ/Outbox/Inbox/Idempotency/File/Batch Recovery 상태를 한 화면에서 분석하는 Reliability Center.

**현재 계약 연결**  
- Path: `/reliability`
- Canonical: `ADM-RECOVERY, ADM-INCIDENT`
- Expected OpenAPI Operation: 8개
- Operation 예: `findAdmUnknownResults, findAdmBrokerDlq, findAdmBrokerOutbox, findAdmBrokerInbox, findAdmIdempotencyRecords, findAdmFileTransferHistory` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `incidents` — Error·Unknown Result (HIGH) **위험조치 화면**

**화면 목적**  
Alert/Signal을 Incident로 연결하고 Acknowledge→Escalate→Runbook→Resolve/Reopen→Timeline까지 운영.

**현재 계약 연결**  
- Path: `/incidents`
- Canonical: `ADM-INCIDENT, ADM-RECOVERY`
- Expected OpenAPI Operation: 20개
- Operation 예: `findAdmUnknownResults, findAdmBrokerDlq, requestAdmBrokerDlqReplay, resolveAdmUnknownResult, getAdmTransactionLogRecoveryStatus, runAdmTransactionLogRecovery` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `auditLogs` — 감사 로그 (MEDIUM)

**화면 목적**  
플랫폼 Audit Log, Delivery, Retry, 정책변경 Audit를 통합 조회.

**현재 계약 연결**  
- Path: `/auditLogs`
- Canonical: `ADM-AUDIT`
- Expected OpenAPI Operation: 4개
- Operation 예: `admAuditLogFindAuditLogs, admAuditDeliveryList, admAuditDeliveryRetry, admLogPolicyAuditFindPolicyAudits`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `logLevel` — 동적 로그 (HIGH) **위험조치 화면**

**화면 목적**  
동적 Log Level Rule을 조회·등록·해제하고 만료·적용대상·Audit를 관리.

**현재 계약 연결**  
- Path: `/logLevel`
- Canonical: `ADM-LOG, ADM-APPROVAL`
- Expected OpenAPI Operation: 3개
- Operation 예: `admDynamicLogLevelFindRules, admDynamicLogLevelRegister, admDynamicLogLevelRemove`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `logPolicies` — 로그 정책 (MEDIUM)

**화면 목적**  
Log Policy, 배포상태, Cache, Trace Boost/Override와 Runtime 적용상태를 관리.

**현재 계약 연결**  
- Path: `/logPolicies`
- Canonical: `ADM-LOG`
- Expected OpenAPI Operation: 13개
- Operation 예: `admLogPolicyFindPolicies, admLogPolicyFindPolicy, admLogPolicyCreatePolicy, admLogPolicyUpdatePolicy, admLogPolicyDisablePolicy, admLogPolicyDistributionStatus` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `remoteLogs` — 원격 로그 (MEDIUM)

**화면 목적**  
다중 Instance 원격 로그 검색·Preview·Bundle 생성·Download·진단을 제공.

**현재 계약 연결**  
- Path: `/remoteLogs`
- Canonical: `ADM-LOG`
- Expected OpenAPI Operation: 9개
- Operation 예: `admRemoteLogSearch, admRemoteLogPreview, admRemoteLogBundleJobCreate, admRemoteLogBundleJobFind, admRemoteLogBundleJobDownload, admRemoteLogBundleDownloadTokenIssue` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 06 복구·변경·배포

#### `batch-deployment` — Deployment / Rollback (HIGH) **위험조치 화면**

**화면 목적**  
Batch Deployment Plan과 Runtime Command 결과를 추적하고 실패 시 Rollback 경로를 제공.

**현재 계약 연결**  
- Path: `/batch-deployment`
- Canonical: `ADM-BATCH, ADM-RECOVERY, ADM-APPROVAL`
- Expected OpenAPI Operation: 3개
- Operation 예: `admBatchRuntimeCreateDeploymentPlan, admBatchRuntimeView, admBatchRuntimeCommandState`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `runtimeControl` — Deployment·Promotion·Rollback (HIGH) **위험조치 화면**

**화면 목적**  
Deployment/Promotion/Rollback Change를 Preview→Create→Execute/Cancel→Verify/Audit로 관리.

**현재 계약 연결**  
- Path: `/runtimeControl`
- Canonical: `ADM-SERVICE, ADM-RECOVERY, ADM-APPROVAL`
- Expected OpenAPI Operation: 16개
- Operation 예: `admRuntimeControlFindHealth, admRuntimeControlFindStatus, admRuntimeControlFindCapabilities, admRuntimeControlPreviewChange, admRuntimeControlCreateChange, admRuntimeControlFindChange` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `batch-recovery` — Recovery / Unknown (MEDIUM)

**화면 목적**  
Batch Ghost/Unknown 후보를 식별하고 Recovery/Reconcile/Action을 실행.

**현재 계약 연결**  
- Path: `/batch-recovery`
- Canonical: `ADM-RECOVERY, ADM-BATCH`
- Expected OpenAPI Operation: 6개
- Operation 예: `admBatchWorkbenchRecovery, admBatchRuntimeView, admBatchFindGhostCandidates, admBatchActGhostExecution, findAdmUnknownResults, resolveAdmUnknownResult`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `recoveryCenter` — 복구 센터 (MEDIUM)

**화면 목적**  
Transaction Log Recovery, Poison Retry, Unknown Resolve, DLQ Replay를 Runbook 중심으로 수행.

**현재 계약 연결**  
- Path: `/recoveryCenter`
- Canonical: `ADM-RECOVERY`
- Expected OpenAPI Operation: 5개
- Operation 예: `getAdmTransactionLogRecoveryStatus, runAdmTransactionLogRecovery, retryAdmTraceRecoveryPoison, resolveAdmUnknownResult, requestAdmBrokerDlqReplay`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `maintenance` — 점검·Drain (HIGH) **위험조치 화면**

**화면 목적**  
점검/Drain 등 운영 Action을 조회하고 대상·영향·사유·승인 후 Owner Command로 실행.

**현재 계약 연결**  
- Path: `/maintenance`
- Canonical: `ADM-SERVICE, ADM-APPROVAL`
- Expected OpenAPI Operation: 2개
- Operation 예: `admMaintenanceFindActions, admMaintenanceExecuteAction`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `integrationClosure` — 통합 운영 정정 승인 (CRITICAL) **위험조치 화면**

**화면 목적**  
Crypto/Time/Data Quality/Webhook DLQ 상태를 검증하고 Correction Approval→Execute→Replay를 수행.

**현재 계약 연결**  
- Path: `/integrationClosure`
- Canonical: `ADM-EXS, ADM-RECOVERY, ADM-APPROVAL`
- Expected OpenAPI Operation: 8개
- Operation 예: `admIntegrationCryptoStatus, admIntegrationTimeHealth, admIntegrationDataQualityValidate, admIntegrationDataQualityCorrectionApprovalRequest, admIntegrationDataQualityCorrectionExecute, admIntegrationDataQualityReplay` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 07 기준정보·플랫폼설정

#### `featureFlags` — Feature Flag (CRITICAL) **위험조치 화면**

**화면 목적**  
Feature Flag 평가·Override 요청/승인/회수와 Kill Switch를 관리하는 CRITICAL 운영화면.

**현재 계약 연결**  
- Path: `/featureFlags`
- Canonical: `ADM-APPROVAL, ADM-AUDIT`
- Expected OpenAPI Operation: 7개
- Operation 예: `admFeatureFlagSearch, admFeatureFlagFind, admFeatureFlagEvaluate, admFeatureFlagRequestOverride, admFeatureFlagApproveOverride, admFeatureFlagRevokeOverride` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `openApiOperations` — OpenAPI 운영 (HIGH) **위험조치 화면**

**화면 목적**  
OpenAPI 상태와 Refresh를 관리하고 Frontend Generated Client 계약과 Runtime API 정합성을 진단.

**현재 계약 연결**  
- Path: `/openApiOperations`
- Canonical: `ADM-SERVICE`
- Expected OpenAPI Operation: 2개
- Operation 예: `admOpenApiStatus, admOpenApiRefresh`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `resiliencePolicies` — Resilience 정책 (CRITICAL) **위험조치 화면**

**화면 목적**  
Retry/Timeout/Circuit 등 Resilience Policy를 조회하고 변경 요청→승인/반려로 관리.

**현재 계약 연결**  
- Path: `/resiliencePolicies`
- Canonical: `ADM-APPROVAL, ADM-EXS`
- Expected OpenAPI Operation: 5개
- Operation 예: `admResiliencePolicySearch, admResiliencePolicyFind, admResiliencePolicyRequest, admResiliencePolicyApprove, admResiliencePolicyReject`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `configs` — 설정 (HIGH) **위험조치 화면**

**화면 목적**  
설정 목록·상세·등록·수정·삭제와 Parameter Reference를 제공하고 Version/Scope/Runtime 반영/Audit를 관리.

**현재 계약 연결**  
- Path: `/configs`
- Canonical: `ADM-AUDIT, ADM-APPROVAL`
- Expected OpenAPI Operation: 6개
- Operation 예: `admConfigFindConfigs, admConfigFindConfig, admConfigCreateConfig, admConfigUpdateConfig, admConfigDeleteConfig, admParameterReferenceSearch`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `businessCalendar` — 영업일 · 휴일 (MEDIUM)

**화면 목적**  
영업일/휴일 조회·날짜해석·등록·삭제를 제공하고 Batch/Schedule 영향도를 확인.

**현재 계약 연결**  
- Path: `/businessCalendar`
- Canonical: `ADM-BATCH, ADM-AUDIT`
- Expected OpenAPI Operation: 4개
- Operation 예: `admCalendarFindDays, admCalendarResolveDate, admCalendarSaveDay, admCalendarDeleteDay`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `responseCodes` — 응답코드 (MEDIUM)

**화면 목적**  
표준 응답코드를 조회·등록·수정·삭제하고 사용처와 Error UX 연결성을 확인.

**현재 계약 연결**  
- Path: `/responseCodes`
- Canonical: `ADM-UX`
- Expected OpenAPI Operation: 5개
- Operation 예: `admResponseCodeFindAll, admResponseCodeFindOne, admResponseCodeCreate, admResponseCodeUpdate, admResponseCodeDelete`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `cache` — 캐시 (HIGH) **위험조치 화면**

**화면 목적**  
Cache Summary를 조회하고 Refresh/Evict/Reconcile 위험조치를 Namespace/Key 범위별로 안전하게 수행.

**현재 계약 연결**  
- Path: `/cache`
- Canonical: `ADM-SERVICE, ADM-APPROVAL`
- Expected OpenAPI Operation: 5개
- Operation 예: `admCacheSummary, admCacheRefresh, admCacheEvictKey, admCacheEvictNamespace, admCacheReconcile`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `codes` — 코드 (MEDIUM)

**화면 목적**  
플랫폼 공통 Code를 조회·등록·수정·삭제하고 사용처·Version·Audit를 연결.

**현재 계약 연결**  
- Path: `/codes`
- Canonical: `ADM-AUDIT`
- Expected OpenAPI Operation: 5개
- Operation 예: `admCodeFindCodes, admCodeFindCode, admCodeCreateCode, admCodeUpdateCode, admCodeDeleteCode`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

### 08 보안·권한·승인

#### `breakGlass` — Break-glass (HIGH) **위험조치 화면**

**화면 목적**  
긴급 Break-glass Session의 Open→Review→Close와 유효시간·사유·Audit를 관리.

**현재 계약 연결**  
- Path: `/breakGlass`
- Canonical: `ADM-APPROVAL, ADM-AUDIT`
- Expected OpenAPI Operation: 4개
- Operation 예: `admBreakGlassFindSessions, admBreakGlassOpenSession, admBreakGlassReviewSession, admBreakGlassCloseSession`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `secrets` — Secret / Key (HIGH) **위험조치 화면**

**화면 목적**  
Secret Metadata/Provider 상태와 Rotation을 제공하되 Secret 원문은 UI·Log·Evidence에 노출하지 않음.

**현재 계약 연결**  
- Path: `/secrets`
- Canonical: `ADM-AUTH, ADM-AUDIT`
- Expected OpenAPI Operation: 3개
- Operation 예: `admSecretFindMetadata, admSecretFindProviders, admSecretRotate`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `permissions` — 권한 (MEDIUM)

**화면 목적**  
Menu/Button/API/Command Permission, Role, Matrix를 조회·편집하며 Server-side Enforcement와 Cache 반영을 검증.

**현재 계약 연결**  
- Path: `/permissions`
- Canonical: `ADM-RBAC`
- Expected OpenAPI Operation: 26개
- Operation 예: `admPermissionFindManagedMenus, admPermissionFindButtons, admPermissionFindApiPermissions, admPermissionFindRoles, admPermissionFindMenuMatrix, admPermissionFindButtonMatrix` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 조회 화면도 Export/Download가 있으면 범위·권한·Masking·Audit·대량 제한을 적용한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `security` — 보안 (HIGH) **위험조치 화면**

**화면 목적**  
MFA 등록/검증/해제와 IP Allowlist를 관리하고 Fail-closed 인증상태를 진단.

**현재 계약 연결**  
- Path: `/security`
- Canonical: `ADM-AUTH, ADM-RBAC`
- Expected OpenAPI Operation: 6개
- Operation 예: `admSecurityFindMfaStates, admSecurityRegisterMfa, admSecurityVerifyMfa, admSecurityDisableMfa, admSecurityFindIpAllowlist, admSecuritySaveIpAllowlist`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `password` — 비밀번호 (HIGH) **위험조치 화면**

**화면 목적**  
Password Policy/검증/변경/Reset/Session Revoke를 관리.

**현재 계약 연결**  
- Path: `/password`
- Canonical: `ADM-AUTH`
- Expected OpenAPI Operation: 5개
- Operation 예: `admOperatorPasswordPolicy, admOperatorValidatePassword, admOperatorChangePassword, admOperatorResetPassword, admOperatorRevokeSession`

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `operators` — 운영자 (HIGH) **위험조치 화면**

**화면 목적**  
운영자 계정·Role·Session·상태·Lock·연락처·접근 메뉴를 관리.

**현재 계약 연결**  
- Path: `/operators`
- Canonical: `ADM-AUTH, ADM-RBAC`
- Expected OpenAPI Operation: 12개
- Operation 예: `admOperatorFindOperators, admOperatorFindRoles, admOperatorFindSessions, admOperatorCreateOperator, admOperatorUpdateRoles, admOperatorUpdateStatus` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.

#### `approvals` — 위험조치 승인 (HIGH) **위험조치 화면**

**화면 목적**  
위험조치 정책/요청/결재/실행/Reconcile을 ALL/ANY/N_OF_M·SoD·Expiry 기준으로 관리.

**현재 계약 연결**  
- Path: `/approvals`
- Canonical: `ADM-APPROVAL`
- Expected OpenAPI Operation: 12개
- Operation 예: `admApprovalPolicies, admApprovalPolicyDetail, admApprovalPolicySave, admApprovalRequest, admApprovalRequestDetail, admApprovalDecision` …

**필수 UI**
- 화면 상단에 1~2문장의 기능 설명과 데이터 범위/최근 갱신시각을 표시한다.
- 검색 조건은 실제 운영 식별자·상태·기간·시스템/인스턴스 등 화면 목적에 맞게 제공한다.
- 목록은 Paging/Sort/Total/Empty/Loading/Error를 가진다.
- 상세는 관련 TransactionId/ExecutionId/InstanceId 등 Context를 유지하여 연관 화면으로 Drill-down한다.
- Permission으로 접근·버튼을 제어하고 Backend Enforcement와 일치시킨다.
- 401/403/404/409/429/500/503을 구분한다.
- 변경/삭제/실행 등 위험조치는 Impact Preview → Reason → Confirm → 필요 시 Approval/SoD → Owner Command → Result → Audit → Recovery/Retry 경로를 제공한다.
- Desktop와 Narrow viewport Browser E2E/Screenshot을 남긴다.

**완료 판정**  
`Menu → Route → Page → Generated Client → Backend → Runtime/DB → Permission/Audit → Normal/Error E2E`가 끊기지 않아야 한다.


---

## 8. Canonical에서 별도 확인해야 할 4개 중요 Coverage

### 8.1 인증·세션 운영 (`ADM-AUTH`)

현재 Route가 여러 Security 화면으로 분리되어 있으므로 다음 전체 Coverage를 반드시 Matrix로 증명한다.

- Password
- MFA
- OIDC/SSO 상태
- Session
- Session Fixation 방어
- 동시 Session 정책
- Session Revoke
- Force Logout
- Account Lock/Unlock
- IP Allowlist
- Fail-closed

메뉴를 하나로 합칠 필요는 없지만 사용자가 `운영자 상세 → 현재 Session → Revoke` 흐름을 끊김 없이 수행할 수 있어야 한다.

### 8.2 외부기관 관제 (`ADM-EXS`)

Gateway 화면만으로 완료라고 가정하지 않는다.

반드시 다음을 대조한다.

- Institution
- Endpoint
- Health
- Credential Metadata
- Certificate 만료/상태
- 최근 Request/Response
- Transaction Timeline
- 오류
- UNKNOWN
- Reconciliation
- Owner API
- Audit

기존 Gateway/Integration 화면으로 전부 제공되면 재사용하고, Coverage가 끊기면 전용 화면/Tab을 추가한다.

### 8.3 Incident Postmortem

단순 Incident 상태변경에서 끝나지 않는다.

```text
Alert
→ Incident 생성/연결
→ Severity/Owner
→ Transaction/Service 영향
→ Runbook
→ Action
→ Resolve
→ Postmortem
→ 재발방지
→ Closure
```

까지 제공한다.

### 8.4 Audit Before/After Diff

Audit 화면은 단순 이벤트 목록이 아니다.

- Before Masked Snapshot
- After Masked Snapshot
- Diff
- Actor
- Target
- Reason
- Approval
- Result
- TransactionId
- Command Hash
- Integrity/Tamper-evident 상태

를 보여준다.

---

## 9. Batch UI 상세화 — Reference 15장 반영

Legacy Batch Reference에서 확인한 주요 기능은 CPF에서 다음과 같이 현대화한다.

### 9.1 Job Definition

Legacy:
- Job 등록정보
- Job Group
- 담당자
- Job Type
- Agent
- Parameter
- 반복/실행조건
- Trigger/선행 Job
- Schedule
- 변경/복제/삭제/Instance 생성/즉시실행

CPF:
- Definition과 Runtime Execution을 분리
- Validate
- Version
- Change Diff
- Job Relation
- Schedule Simulation
- Save/Transition
- Owner
- Required Capability
- Parameter Schema
- 즉시실행은 Permission/Reason/Audit

### 9.2 Job Flow Diagram

Legacy의 Job Dependency Diagram은 가치가 높다.

CPF에서도:
- 선행/후행 Job
- AND/OR
- Trigger
- 실패 Node
- 현재 진행 Node
- Click→Job Detail
- Click→Execution
- Zoom/Pan
- 대형 Graph 성능
을 지원하는 방향을 검토한다.

### 9.3 Job Instance

Legacy가 제공하는:
- 상태 Filter
- Job/Group/Program/Server
- 진행률
- 시작/종료시간
- 처리일
- Log
- Diagram
- 강제실행/재실행
- 상태점검
- Report/Log Download

을 CPF의 Execution/Step/Checkpoint/Recovery 모델에 맞게 구현한다.

### 9.4 Agent/Server

Legacy Server 화면의 장점:
- Agent ID
- Host/IP/Port
- 사용여부
- Directory/Account/OS/Java/Agent Version
- Heap
- Thread
- 실행중 Job

CPF는 여기에:
- Heartbeat
- Capability
- Artifact
- Process
- Drain
- Takeover
- Version skew
- Zone
- Fencing
을 추가한다.

### 9.5 Calendar

Legacy Calendar 화면처럼 운영자가 Schedule을 월 단위로 이해할 수 있어야 한다.

CPF:
- 영업일/휴일
- Batch Schedule
- 실행예정
- 비활성 Schedule
- Override
- Calendar Resolve
를 연결한다.

### 9.6 Notification

Legacy 통지설정/수신자 기능을 CPF에서는 Rule/Recipient/Channel/Delivery/Attempt/DLQ/Test/Audit로 확장한다.

### 9.7 Job Log

Legacy Tail/Full/Download의 실용성을 유지한다.

CPF는:
- Tail
- Full
- Search
- Level
- TransactionId/ExecutionId
- Step
- Multi-instance
- Bundle
- Safe Download
- Masking
을 제공한다.

---

## 10. Online UI 상세화 — Reference 29장 반영

### 10.1 System 관리

Legacy의 사용자/권한/메뉴/로그인·사용이력은 CPF ADM-AUTH/RBAC/AUDIT에 흡수한다.

메뉴 자체를 고객이 마음대로 편집하는 기능은 Product 정책과 충돌하지 않는지 확인하고, Permission Matrix와 연동한다.

### 10.2 Common 관리

Legacy의 Parameter/Message/Code/Notification/Node/DataSource/Cache/Log/TCP Monitoring은 CPF에서:

```text
Config/Parameter
Code
Response Code
Message
Notification
Service Registry
Health
Cache
Log Policy
Gateway/Integration
```

로 Owner를 명확히 분리한다.

### 10.3 거래 통제/Profile

Legacy의 거래통제·전후처리·Profile·속성은 CPF의 Transaction Metadata/Channel Policy/Resilience/Runtime Policy와 대조한다.

임의 Script/Expression이 가능하면:
- Syntax Validation
- Preview
- Version
- Scope
- Dangerous Expression 제한
- Approval
- Audit
가 필수다.

### 10.4 거래 Log/통합 Trace

Legacy 통합로그 화면에서 확인되는 좋은 운영패턴:

- Global ID
- 최초/최종 시간
- 소요시간
- 성공/에러/Inbound/Outbound 건수
- Channel
- 거래코드
- Node
- Request/Response 전문

CPF는 이를 TransactionId 중심 Timeline으로 확장하고 Payload는 Masking한다.

### 10.5 통계/성능

Legacy 거래통계·전문처리시간 통계는 CPF Dashboard/Capacity/Analysis Center에서 최소 다음을 제공하는지 확인한다.

- TPS/처리건수
- Success/Error
- 정상처리율
- p50/p95/p99 또는 적절한 Latency
- Channel/System/Service Breakdown
- 시간대 Trend
- Drill-down to transaction

### 10.6 배포/Center-Cut

Legacy Component/XIO/Center-Cut Profile과 실행 Log의 실무 기능을 CPF Runtime Control/Batch/Center-Cut에서 더 안전하게 제공한다.

### 10.7 Analysis

Legacy Component/SQL/XIO/FIO/Impact 분석 기능은 무조건 동일하게 복제하지 않는다.

상용 CPF에 유효한 부분은 다음 관점으로 흡수한다.

- Service Dependency
- DB/Query Usage
- Integration Dependency
- Change Impact
- Runtime Capability
- OpenAPI Consumer
- Generator/Artifact 영향

실제 Source/Metadata로 신뢰할 수 있게 만들 수 없는 분석화면은 False Precision을 만들지 않는다.

---

## 11. Error/State UX

모든 주요 Page는 다음 상태를 개별 디자인한다.

- Loading
- Empty
- Success
- Stale
- Partial Failure
- Permission Denied
- Conflict
- Rate Limited
- Backend Unavailable
- Unknown Result

HTTP:
- 401: 인증 필요 / 재로그인
- 403: 권한 없음 / 필요한 권한 설명
- 404: 대상 없음 / stale link 가능성
- 409: Version/상태 충돌 / Refresh·Diff
- 429: Retry-after
- 500: 요청 ID와 안전한 오류
- 503: 서비스 불가 / Retry·상태페이지

---

## 12. UI Visual Design Requirement

ADM은 금융권 Enterprise Admin이므로 화려함보다 **정보계층·가독성·상태식별·안전성**을 우선한다.

### Layout

- Desktop 1440~1920 주사용
- 1280에서도 핵심 사용 가능
- Narrow viewport에서 Sidebar collapse
- 좌측 Menu + Header + Content
- 너무 긴 3Depth menu 금지
- 자주 쓰는 메뉴 Favorite/Recent 검토

### Table

Legacy처럼 매우 많은 Column을 한꺼번에 보여주지 않는다.

- 중요 Column 기본 노출
- 나머지 Column 선택
- Sticky critical columns
- Horizontal scroll 최소화
- Detail Drawer/Route 사용

### 상태

- 색상 + Icon + Text를 함께 사용
- 색상만으로 성공/장애를 전달하지 않는다
- UNKNOWN은 ERROR와 별도 표현
- CRITICAL Action은 별도 시각체계

---

## 13. 접근성

- Tab Order
- Visible Focus
- Skip Navigation
- Label / aria
- Modal Focus Trap
- ESC
- Dialog Title
- Table Header
- Button Name
- Keyboard 검색/실행
- Color Contrast
- Reduced Motion 기본 고려

---

## 14. Permission/Risk UI

각 Route에 Risk Level을 명시한다.

- LOW
- MEDIUM
- HIGH
- CRITICAL

현재 Source의 Risk Level을 기준으로 실제 기능 위험도와 재검산한다.

### HIGH/CRITICAL

- Button 강조보다 경고맥락 우선
- 실행 전 Preview
- Reason 필수
- Approval
- SoD
- Expiry
- Command Hash
- 결과 추적
- Reconcile
- Audit
- 재실행 Idempotency

---

## 15. Menu/Route/OpenAPI Coverage Matrix

Developer GPT는 다음 Matrix를 실제로 생성한다.

```text
canonical_requirement
target_menu_group
menu
route
page
feature
risk
permission
expected_operation_id
generated_client_consumer
backend_owner
db/runtime_owner
normal_test
error_test
browser_test
screenshot
status
```

0건 목표:

- Canonical Requirement인데 메뉴/화면/비메뉴 UX Evidence 없음
- Menu인데 Route 없음
- Route인데 Page 없음
- Page인데 Generated Client Consumer 없음
- Operation인데 Consumer 없음
- Dead Page
- Mock-only
- Fixture-only
- Permission 없는 위험조치
- Audit 없는 위험조치
- Help/기능설명 없는 주요 Page
- Screenshot에서 깨진 주요 Page

---

## 16. Browser 검증

현재 CPF가 이미 Playwright 구조를 갖고 있으므로 가능한 범위까지 실제 Browser로 검증한다.

- Chromium
- Firefox
- WebKit

주요 Route:
- Normal
- Empty
- Error interception
- Permission
- HIGH/CRITICAL Action
- Desktop
- Narrow

Screenshot Evidence를 남긴다.

**Screenshot을 남기는 것만으로 PASS하지 않고 실제 UI를 확인하여 잘림/겹침/overflow/가독성/버튼 우선순위를 판정한다.**

---

## 17. 적용·검증 명령 규칙

첫 실패에서 중단하지 않는다.

```text
Route/IA static
→ OpenAPI operation mapping
→ Generated client consumer scan
→ Typecheck
→ Lint
→ Unit
→ Backend targeted test
→ Playwright Chromium
→ Playwright Firefox
→ Playwright WebKit
→ Screenshot inspection
→ 전체 실패 집계
→ Root Cause 분류
→ 일괄 수정
→ 재실행
```

일반 실패는 수집 후 계속한다.

즉시 중단 허용:
- Repository Root 이탈
- 보호경로 침범
- Secret 노출 위험
- 위험한 실제 운영 Command
- 사용자 승인 없는 Git Write/Delete

---

## 18. 완료 정의

ADM은 다음 질문에 모두 YES일 때만 Developer GPT 단계에서 완료 후보가 된다.

1. 운영자가 메뉴를 보고 기능 위치를 이해할 수 있는가?
2. 각 주요 화면에 무엇을 하는 화면인지 설명이 있는가?
3. 장애를 Dashboard에서 실제 원인까지 추적할 수 있는가?
4. TransactionId 하나로 전체 Timeline이 연결되는가?
5. Batch Job에서 Execution/Step/Log/Recovery까지 연결되는가?
6. 외부기관/Gateway 장애와 Credential/Certificate 상태를 볼 수 있는가?
7. 설정 변경의 영향·Diff·승인·Audit가 보이는가?
8. 위험조치가 Owner Command를 통해 안전하게 실행되는가?
9. UNKNOWN을 별도 상태로 보고 Reconcile할 수 있는가?
10. Permission이 Menu/Button/API/Command 전체에서 일치하는가?
11. Before/After Audit와 Reason/Approval/Result가 남는가?
12. UI가 Desktop/Narrow에서 실제 사용할 수 있는가?
13. 401/403/404/409/429/500/503이 의미 있게 처리되는가?
14. Mock-only/Dead UI가 없는가?
15. Canonical ADM 15축에 Evidence가 있는가?

16. Transaction/Service/Instance/DB/Broker/Batch/Gateway/Agent Metric을 운영자가 확인할 수 있는가?
17. SLO/Error Budget/Burn-rate를 보고 관련 Incident까지 Drill-down할 수 있는가?
18. Alert Dedup/Grouping/Suppression/Acknowledge/Escalation이 구현됐는가?
19. 실행 가능한 Runbook과 자동복구 Allowlist/Attempt/Circuit Stop/Rollback이 있는가?
20. Source/Artifact/Config/DB/Route/Permission/Runtime Drift를 Desired-Actual로 확인할 수 있는가?
21. CPU/Memory/Thread/Connection/Queue/Storage/DB/Broker 용량의 Trend/Forecast가 있는가?
22. RTO/RPO, Backup/Restore, Failover/Failback, DR Drill을 안전하게 운영할 수 있는가?
23. 민감 Download에 Watermark/Limit/Expiry/Encryption/One-time Link/Audit가 적용되는가?
24. Certificate/Secret의 Expiry/Rotation/Revocation/Provider Health가 원문 노출 없이 관리되는가?
25. Audit hash-chain/tamper detection과 Approval dual-control가 UI/Evidence까지 연결되는가?


---

## 19. Developer GPT 권한 경계

이 문서는 실행용 상세 Requirement다.

Developer GPT는:
- Source/Frontend/Backend/Test/OpenAPI/Generated Client 구현
- 자체검수
- Developer-owned Evidence
를 수행한다.

Developer GPT는 Canonical Requirement/Architecture/Current Instruction을 직접 수정하지 않는다.

Canonical과 실제 Source 사이 충돌을 발견하면 `OPEN_ISSUES.md`와 Evidence에 기록하고 Source에서 구현 가능한 작업을 계속한다.
