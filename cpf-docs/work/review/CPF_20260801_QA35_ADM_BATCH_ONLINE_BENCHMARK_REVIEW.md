# CPF QA35 ADM Batch·Online Benchmark Independent Review

## 1. 결론

CPF ADM은 **기능 이름과 영역 수만 보면 사용자 제공 최소 기준보다 넓다.** 현재 Frontend에는 59개 메뉴와 59개 Route가 있고, Immutable Job Pack, 위험조치 승인, Recovery/Unknown Result, Gateway Security, Unified Transaction Group Trace 같은 발전된 개념도 존재한다.

그러나 상용 운영도구 완성도는 균일하지 않다. 여러 Batch 세부 메뉴가 Generic Dynamic Table 한 장으로 구현돼 있고, 9개 Gateway 메뉴가 동일 Component와 기본 Tab을 공유하며, Online 핵심 Definition/Deployment/Runtime Analysis와 System/Common 일부 기능은 First-class Route가 확인되지 않는다. 따라서 **“메뉴가 많다”는 이유로 ADM 완료를 선언하면 안 된다.**

Capability Matrix 68건 판정: 완료 1, 부분 구현 48, 미구현 14, 재확인 필요 3, 실패 2.

## 2. 보호해야 할 강점

- `BatchJobPacksPage.vue`: Immutable Definition Version, validation, approval request, publish/retire, dependency/resource/recovery/unknown-result policy.
- `BatchPage.vue`: 등록, manual run, retry, stop, schedule once, relation/target/step, lock/ghost candidate, Center-Cut 상세.
- `TransactionGroupsPage.vue`: 광범위한 검색, paging/sort, masking, timeline, segment, 표준/확장 Header, external log, raw JSON.
- `GatewayOperationsPage.vue`: server group/member, route binding, default deny, approval lifecycle, timeout/retry/idempotency/security/apply status/connection test.
- ADM Shell: 메뉴 검색, 즐겨찾기, 최근 메뉴, responsive sidebar.

이 기능들은 다음 개발에서 단순화하거나 Legacy 화면 수준으로 후퇴시키지 않는다.

## 3. P0 발견 사항

1. 메뉴/Route 59개가 존재하지만 Frontend hardcoded metadata와 Backend permission ID가 병렬 정본이다.
2. 알 수 없는 Route가 Dashboard로 Silent fallback 된다.
3. Gateway 9개 메뉴가 같은 Page를 열고 route-specific tab이 보장되지 않는다.
4. Batch Instances, Scheduler, Worker Pools, Agents, Executions, Recovery가 Generic `BatchViewPage` Wrapper이다.
5. Batch Execution 전용 Detail Workspace, log/report/artifact/state timeline, operation-specific governance가 불충분하다.
6. Batch-Online-Gateway-Incident-Audit Cross-navigation이 제품 계약으로 닫혀 있지 않다.
7. Online pre/post, pipeline, dependency rule, DBIO, deployment, thread/delayed-async monitor, analysis center가 First-class 운영 기능으로 확인되지 않는다.
8. Error feature Directory는 있으나 Route에 등록되지 않았다.
9. 메뉴 관리, 공지, session/login history, DB schema/drift, label/localization, datasource monitor가 명시적으로 확인되지 않는다.
10. 기존 `featureCoverage.ts`는 Menu/Route ID만 확인하고 실제 Operation·Backend·Permission·Audit·E2E를 증명하지 않는다.

## 4. Batch 기준 대조

기준 화면의 Job 등록·상세·Dependency·Schedule Simulation·Instance·로그·리포트·Server/Agent·Calendar Capability 중 CPF는 Job Definition Governance는 더 발전했다. 반면 실행/스케줄/Agent/Worker/Recovery 세부 메뉴는 전용 운영 Workflow가 부족하다. 특히 Generic View는 typed filter, paging, detail, safe action, error handling, deep link를 제공하지 못한다.

## 5. Online 기준 대조

CPF의 Transaction Group Trace는 기준 통합 로그보다 분산 추적 관점에서 우수하다. 그러나 Definition 관리와 운영 변경, DBIO/SQL, Deployment, Runtime saturation, Error/Protocol workbench, Analysis 기능은 기준 최소 Capability에 못 미치거나 명시적 Route가 없다.

## 6. System/Common 기준 대조

Operator, Permission, Audit, Config, Code, Message, Notification, Cache, Secret, Approval, Break-glass는 좋은 방향이다. Menu Management, Notice, Session/Login History, Schema/Drift, Label/Localization, Datasource/File/Connection Monitoring을 추가하거나 명확히 기존 기능에 매핑해야 한다.

## 7. QA35 변경

- Defect: 18 → 36건
- Requirement: 23 → 43건
- Root Cause: 10 → 15건
- 신규 ADM Capability Matrix: 68건
- 신규 ADM Menu/Route Matrix: 59건

새 Requirement는 기존 QA35의 OpenAPI, Generated Client, BFF Permission, Runtime Evidence가 먼저 정상화된 후 구현한다. UI만 추가하거나 Generic Table을 복제하는 것은 완료가 아니다.
