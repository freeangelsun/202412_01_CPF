# CPF QA35 ADM 최소 기능선 상세 검수

## 1. 검수 입력

- 사용자 제공 ZIP: `메뉴캡쳐이미지(1).zip`
- Screenshot: 44장
  - Batch: 15장
  - Online/System/Common/Batch/Analysis: 29장
- 비교 대상 CPF ADM Route: 59개
- 기준 Source SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`

이 자료는 구형 제품의 화면을 그대로 복제하기 위한 UI 시안이 아니다.  
**CPF ADM이 반드시 포함해야 하는 최소 운영 Capability**이며 CPF는 보안·감사·복구·다중 인스턴스·결과불명·배포·Evidence 품질에서 이를 상회해야 한다.

## 2. 캡처에서 확인된 최소 기능군

### 시스템 관리
사용자, Role, Menu, 메뉴별 CRUD/Server 권한 Matrix, 공지, 로그인 이력, 사용 이력, 시스템 로그 파일 탐색, DB Schema/Datasource Inventory.

### 공통 플랫폼 운영
Framework Node, Profile/환경, Logger level/effective state, Cache, Datasource Pool, Message/Code, Notification Rule/Receiver, Event Log, Log tail/download.

### 온라인 설계·운영
거래 Test, 거래 Metadata, 거래 Profile과 Property Schema, 전후처리 순서, Deferred Rule, DBIO/Query Registry, Component/XIO/FIO 배포현황, 서비스 처리량, Thread/Executor, Deferred/지연비동기 관제, 서비스 처리 흐름, 거래/Error/Debug/전문 Log, 거래·전문 처리시간 통계, Log 정책.

### 배치 설계·운영
Job Definition/Parameter/Program/Trigger/Schedule, clone/version, Job Group, Dependency Flow Diagram, 긴급 실행 승인, Job Instance 검색/상세/상태 제어, Log tail/full/download, Report Artifact, Agent/Host/JVM/OS/Disk, 환경변수, Calendar, Receiver, Scheduler HA, Center-Cut Profile/실행통계, Job Pack/Deployment/Rollback.

### 분석
Report, SQL/DBIO, XIO/외부연계, FIO/File, 변경 영향도 분석.

## 3. CPF가 이미 앞서는 기능 후보

현재 Route만으로도 다음은 비교 대상보다 발전된 방향이다.

- Recovery Center와 Reliability 상세
- Unknown Result/DLQ/Reconcile
- Incident와 Batch Alert
- Approval과 Break-glass
- Lease/Fencing
- Batch Deployment/Rollback
- Gateway Security/Apply Status
- Secret/Key 운영
- Audit/Evidence
- SLO/Capacity와 Topology

그러나 **고급 Route 존재만으로 제품 완성은 아니다**. 해당 기능이 일상 운영기능과 실제 Workflow로 연결되고 exact-SHA Runtime에서 검증돼야 한다.

## 4. 현재 확인된 핵심 부족·재확인 항목

1. Menu Registry·공지·로그인/사용 이력·DB Schema/Datasource 전용 기능.
2. 거래 Test·Profile·Profile Schema·DBIO·전후처리 Pipeline.
3. Online Executor/Thread·Deferred·지연비동기·거래/전문 특화 통계.
4. Online Component와 XIO/FIO Adapter의 통합 배포/Version Drift.
5. Batch Job Definition 상세·clone/version·Job Group Lifecycle.
6. Batch Dependency DAG/Flow Diagram·cycle·critical path.
7. 긴급/수동 실행과 Instance 제어의 공통 승인·Idempotency·Unknown Result.
8. Batch Agent/Host의 JVM·OS·Disk·quarantine와 실행 Command 결과.
9. Batch scope 환경변수·Receiver·Report Artifact.
10. Center-Cut Profile/Simulation/Execution statistics.
11. Global Search와 Online–Gateway–Batch Cross-domain Timeline.
12. Report·SQL·XIO/FIO·Impact Analysis.
13. 전체 화면의 검색·Server Paging·상세·Empty/Error·Freshness·Export 품질.
14. OpenAPI/Generated Client 실패 상태로 인한 Typed Consumer 부재.

## 5. 목표 정보구조

CPF ADM은 단순히 Menu 수를 늘리지 않는다.

```text
통합 홈
 ├─ Global Search / Topology / 승인·복구 대기
온라인 설계
 ├─ 거래 Metadata / Profile / Pipeline / DBIO / Test
온라인 운영
 ├─ 거래 Timeline / Logs / Runtime / Async / Deployment
배치 설계
 ├─ Job Definition / Group / DAG / Schedule / Center-Cut
배치 운영
 ├─ Execution / Emergency / Recovery / Worker·Agent / Deployment
연계 관리
 ├─ Gateway / REST·전문 / File / Notification
통합 관제
 ├─ Event·Incident / Recovery / Audit·Evidence
분석
 ├─ 거래·전문 / Batch SLA / SQL / XIO·FIO / 영향도
플랫폼 관리
 ├─ Node / Config·Secret·Cache·Log / Code·Message·Calendar
보안·권한
 ├─ Operator / Role / Menu / Permission / Approval / Session
```

## 6. 완료 판정

다음이 모두 충족돼야 ADM 완료다.

- 87개 최소 Capability가 `완료` 또는 근거 있는 `고급 대체 완료`.
- 59개 기존 Route와 신규 Target Menu의 Owner/API/Permission/Test가 연결.
- 검색·Paging·상세·Loading/Empty/Error·권한·감사·Freshness가 적용.
- 위험조치가 Reason·Approval·Expected Version·Idempotency·Unknown Result를 처리.
- Online/Batch/Gateway/File/Notification이 하나의 correlation timeline으로 연결.
- Chromium/Firefox/WebKit 실제 Backend E2E.
- Source SHA와 Runtime/Evidence SHA 일치.
- CPF 고급 Recovery/Approval/Fencing 기능을 축소하지 않고 최소 기능과 통합.
