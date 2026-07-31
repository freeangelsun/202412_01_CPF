# CPF 자체 개발 요청 목록

- 기준 SHA: `95e592c05fc457301efdb13ee50e0d7453325806`
- 자체 개발 Requirement: `30`건
- ID Namespace: `CPF-SELF-DEV-*`
- 범위: 개발 주체가 실제 Source 자체 검토로 발견한 누락·구조 부채·운영 완성도 결함

## 정본 분리 원칙

- 이 문서는 자체 개발 전용 정본이다.
- 외부 검수 Requirement·Defect·회차명·상태를 포함하거나 수정하지 않는다.
- 별도 검수 목록이 나중에 전달되더라도 원본 정본과 ID를 유지한다.
- 공통 원인은 한 번만 구현할 수 있으나 추적 관계는 출처별로 별도 유지한다.

## 개발 순서

1. False Green·Fail-Always·Evidence 상태 모델
2. ADM Canonical Registry·Route·Permission·OpenAPI
3. Batch 운영 Workbench
4. Online 운영 Workbench
5. Batch·Online 통합 Trace와 Dashboard
6. Network·DB·Fresh Clone 최종 Gate

## CPF-SELF-DEV-001 [P0] 완료 판정 모델 재정의

- Owner: `공통/검증`
- 현재 Gap: File·Class·Script·Matrix 존재를 기능 완료로 승격하는 관행이 반복 미완료를 만든다.
- 필수 구현: Requirement별 Source·API·SQL·Consumer·Test·Runtime·Evidence 상태를 분리하고 실제 Consumer와 검증 근거가 없으면 완료를 금지한다.
- 완료 검증: 상태 전이 Negative Test, Matrix·Evidence 정합성 Gate, False Green Fixture
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-002 [P0] Fail-Always 통합 Wrapper 제거

- Owner: `cpf-tools`
- 현재 Gap: 존재하지 않는 하위 Script를 호출하거나 파라미터 불일치로 시작 단계에서 실패할 수 있다.
- 필수 구현: 모든 하위 Script 존재·호출 인자·종료코드를 사전 검증하고 단일 통합 실행이 Fresh Clone에서 시작·종료 가능하게 한다.
- 완료 검증: Missing Script·Invalid Parameter Negative Test와 통합 Wrapper 1회 실행
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-003 [P0] Push SHA 정본 동기화

- Owner: `문서/Evidence`
- 현재 Gap: 완료 보고·Current Request·Matrix·Evidence가 서로 다른 Commit SHA를 가리킬 수 있다.
- 필수 구현: 최종 Push 이후 Source를 다시 변경하지 않는 two-phase Evidence 구조로 정본·Evidence를 최신 SHA에 동기화한다.
- 완료 검증: sourceSha=resultSha, clean tree, read-only Fresh Clone
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-004 [P0] Bulk-ID Evidence 금지

- Owner: `Evidence`
- 현재 Gap: 하나의 Evidence에 다수 Requirement·Scenario·Result ID를 일괄 기록해 미실행 항목까지 승격할 위험이 있다.
- 필수 구현: 각 Result를 실행 단계·명령·환경·종료코드·Artifact와 직접 매핑하고 검증된 N:1 관계만 허용한다.
- 완료 검증: 미실행 ID 완료 승격 방지 Negative Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-005 [P0] 실행 가능한 Scenario 계약

- Owner: `cpf-tools`
- 현재 Gap: 정상·오류·복구 문구만 반복된 Scenario는 실제 검증을 보장하지 않는다.
- 필수 구현: Fixture, precondition, action, expected result, recovery, evidence path를 명시한 실행 가능한 Scenario 계약을 만든다.
- 완료 검증: Scenario lint와 executable-reference Gate
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-006 [P0] Canonical ADM Capability Registry

- Owner: `cpf-admin`
- 현재 Gap: Menu·Route·Component·Permission·Backend API·Evidence가 분리돼 Drift가 발생한다.
- 필수 구현: 단일 Registry에서 Menu, Route, Tab, Permission, 위험도, operationId, feature flag, Consumer를 생성 또는 전수 검증한다.
- 완료 검증: 전체 ADM Menu·Route parity test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-007 [P0] Silent Dashboard Fallback 제거

- Owner: `cpf-admin`
- 현재 Gap: 알 수 없는 Route와 Component 누락이 Dashboard로 조용히 대체된다.
- 필수 구현: 403, 404, feature-disabled, lazy-load failure를 구분하고 원인·correlationId를 표시한다.
- 완료 검증: Route Negative E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-008 [P0] Gateway Menu·Tab 정합성

- Owner: `cpf-admin/cpf-gateway`
- 현재 Gap: 여러 Gateway 메뉴가 같은 Component와 기본 Tab으로 연결될 수 있다.
- 필수 구현: Menu별 고유 Tab·Deep Link·Permission·Filter State를 보장하거나 명확한 단일 Workspace로 통합한다.
- 완료 검증: Gateway Route별 active tab·권한·URL 복원 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-009 [P0] Batch Execution Workbench

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Execution 화면이 조회 전용 Dynamic Table 수준이다.
- 필수 구현: 검색·Server Paging·실행 상세·Step Timeline·Parameter·Worker/Server·Log·Artifact·Report·실패 원인·재실행·중지·강제완료를 제공한다.
- 완료 검증: 정상·실패·중단·재실행·Unknown Result E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-010 [P0] Batch Scheduler HA Workbench

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Scheduler HA 메뉴가 전용 운영 제어와 Schedule 분석을 제공하지 않는다.
- 필수 구현: Schedule Preview, Misfire, Calendar, Leader/Lease, Duplicate Suppression, Pause/Resume, Failover, Next-fire 영향도를 제공한다.
- 완료 검증: 다중 인스턴스 Leader Kill·Misfire·Duplicate Fixture
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-011 [P0] Worker Pool 운영 제어

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Worker Pool 화면이 조회 전용이다.
- 필수 구현: Capacity, Queue Depth, Active Task, Drain, Quarantine, Restart, Backpressure, Ownership, Audit를 제공한다.
- 완료 검증: 권한·승인·부분 실패·재시도 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-012 [P0] Host Agent 운영 제어

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Host Agent 조회와 Deployment·Artifact Trust·Process Control이 분리돼 있다.
- 필수 구현: Health, Version, Heartbeat, Deployment, Artifact Hash, Process Start/Stop/Restart, Log Tail, Rollback, Approval을 통합한다.
- 완료 검증: Offline·Partial Deploy·Hash Mismatch·Process Kill Fixture
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-013 [P0] Recovery·Unknown Result Center

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Unknown Result와 Recovery가 일반 목록 수준이다.
- 필수 구현: Lease·Fencing·Idempotency·Reconcile·Replay·Compensation·Manual Decision·Approval·Evidence를 제공한다.
- 완료 검증: Duplicate·Late Reply·ACK Failure·Process Kill·Network Partition Fixture
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-014 [P1] Batch Job 상세·의존관계·리포트 강화

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: Job Pack 거버넌스는 강하지만 실행 상세·Dependency Diagram·Report 연결이 분산돼 있다.
- 필수 구현: Definition Version, Dependency Graph, Schedule Simulation, Execution History, Impact Analysis, Artifact/Report를 하나의 상세 Workspace로 연결한다.
- 완료 검증: Graph Cycle·Disabled Dependency·Version Rollback Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-015 [P0] Batch 위험조치 Permission Matrix

- Owner: `cpf-admin/cpf-batch`
- 현재 Gap: 재실행·강제실행·중지·Drain·Restart 등 위험조치 권한과 승인이 균일하지 않다.
- 필수 구현: READ·EXECUTE·RETRY·STOP·FORCE·DEPLOY·ROLLBACK·RECOVER 권한, 사유, 승인, CAS, Audit, Result Tracking을 표준화한다.
- 완료 검증: 각 Action의 401·403·409·429·503 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-016 [P0] Online Transaction Definition Workbench

- Owner: `cpf-admin/cpf-core/cpf-common`
- 현재 Gap: 거래 정의는 있으나 전처리·후처리·Pipeline·Dependency·DBIO를 통합 관리하지 못한다.
- 필수 구현: Transaction, Pre/Post Processor, Pipeline Stage, Dependency, Timeout/Retry, DBIO/Query, Version/Promotion/Rollback을 제공한다.
- 완료 검증: 정의 변경 영향도·승인·Rollback Integration Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-017 [P0] Online Runtime Diagnostics

- Owner: `cpf-admin`
- 현재 Gap: 서비스 처리량·Thread/Connection Pool·Dependency Latency·Delayed Async·Backpressure 진단이 부족하다.
- 필수 구현: Instance/Service/Route별 TPS, Latency Percentile, Pool Saturation, Queue Lag, Retry, Circuit State, Dependency Map을 제공한다.
- 완료 검증: 부하·고갈·지연·부분 실패 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-018 [P0] Error Workbench

- Owner: `cpf-admin`
- 현재 Gap: Error 조회가 운영 조치·거래 추적·재처리와 충분히 연결되지 않는다.
- 필수 구현: Error Classification, 영향 Transaction/Batch, Stack Masking, Retryability, Owner, Incident, Replay/Compensation, Approval을 제공한다.
- 완료 검증: 민감정보 Masking·재처리 권한·중복 방지 Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-019 [P1] 전문·Protocol Message Workbench

- Owner: `cpf-admin`
- 현재 Gap: 전문·Message 추적과 Field-level Masking·Validation·Replay가 First-class 기능이 아니다.
- 필수 구현: Request/Response Header, Field Schema, Validation, Correlation, Masking, Compare, Replay Guard를 제공한다.
- 완료 검증: 대용량·Malformed·Masked Export Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-020 [P1] Online Deployment·Promotion·Rollback

- Owner: `cpf-admin`
- 현재 Gap: Online Component 배포 상태와 환경 승격·Rollback이 운영화면에서 완결되지 않는다.
- 필수 구현: Artifact Trust, Environment Promotion, Rollout, Health Gate, Rollback, Approval, Audit를 제공한다.
- 완료 검증: Canary Failure·Rollback·Hash Mismatch Fixture
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-021 [P0] Batch·Online 통합 Trace

- Owner: `cpf-admin`
- 현재 Gap: Backend 식별자는 존재하지만 화면 간 Causal Navigation이 완결되지 않았다.
- 필수 구현: Online Transaction→Segment→Gateway/External→Batch Execution/Step→Incident→Approval→Audit를 하나의 Timeline과 Deep Link로 제공한다.
- 완료 검증: Filter·기간·Masking·Permission Context 유지 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-022 [P0] 통합 운영 Dashboard

- Owner: `cpf-admin`
- 현재 Gap: Batch와 Online이 동일 ADM에 있으나 공통 SLO·Incident·Capacity 관점 Dashboard가 부족하다.
- 필수 구현: Online TPS/Latency/Error, Batch Backlog/Failure/ETA, Gateway, Agent/Worker, Incident, Approval Queue를 역할별로 제공한다.
- 완료 검증: 부분 장애·데이터 지연·권한별 Card 노출 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-023 [P1] System/Common 운영 기능 보강

- Owner: `cpf-admin`
- 현재 Gap: Menu 관리·공지·Session·Login 실패·사용이력·DB Schema/Drift·다국어·Datasource Pool 기능이 불명확하다.
- 필수 구현: 각 Capability를 실제 Backend API, Paging, Permission, Audit, Export와 연결한다.
- 완료 검증: Capability Registry Parity와 E2E
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-024 [P1] Analysis Center

- Owner: `cpf-admin`
- 현재 Gap: Component·SQL·External I/O·File I/O·Throughput 분석 기능이 분산돼 있다.
- 필수 구현: 기간 비교, Percentile, Top-N, Causal Drilldown, Export, Saved Query를 제공한다.
- 완료 검증: 대량 데이터 Paging·Timeout·Masking Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-025 [P0] 전체 OpenAPI·Generated Client 정본화

- Owner: `cpf-admin/frontend`
- 현재 Gap: ADM/BZA OpenAPI가 일부 인증 Operation에 한정되고 Generated Artifact가 Stale할 수 있다.
- 필수 구현: Runtime 전체 OpenAPI Export, operationId·Schema·Error Response 검증, Orval 생성, 실제 화면 Consumer 이관, Marker 최신화를 수행한다.
- 완료 검증: Tracked Drift 0, Operation Consumer Coverage 100%
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-026 [P0] 전체 ADM Route 상용 Page Contract

- Owner: `cpf-admin/frontend`
- 현재 Gap: 다수 Menu가 공통 조회 Wrapper에 의존하고 전용 운영 Workflow가 없다.
- 필수 구현: 모든 Route에 Search·Paging·Detail·Loading·Empty·Error·Permission·Responsive·Accessibility·Action Confirmation 계약을 적용한다.
- 완료 검증: Chromium·Firefox·WebKit 전체 Route Matrix
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-027 [P0] 실제 Controller Permission 전수 검증

- Owner: `cpf-admin/backend`
- 현재 Gap: Security Test가 일부 Test Controller 중심이면 실제 ADM/BZA 권한 공백을 잡지 못한다.
- 필수 구현: Controller Method↔Permission Code↔HTTP Method↔Menu Action Matrix를 생성하고 누락 시 Build를 실패시킨다.
- 완료 검증: 실제 Endpoint 401·403·CSRF·Origin Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-028 [P0] Literal IP/CIDR 정책 공통화

- Owner: `공통 네트워크`
- 현재 Gap: Gateway·Batch Outbound·Host Agent에 CIDR Parser가 중복되고 설정에 Hostname이 들어갈 수 있다.
- 필수 구현: 공용 Literal-IP/CIDR Parser, Startup Validation, Port/TLS Policy, Lifecycle Management를 구현한다.
- 완료 검증: IPv4·IPv6·Mixed DNS·Rebinding·Private·Metadata Negative Test
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-029 [P0] 실제 Baseline Upgrade Chain

- Owner: `DB`
- 현재 Gap: DB Matrix가 승인된 이전 Release Baseline에서 시작했음을 증명하지 않는다.
- 필수 구현: Oracle·PostgreSQL·MariaDB 각각 이전 Baseline Install→Sequential Upgrade→Runtime Query→Reverse Rollback→Reapply를 수행한다.
- 완료 검증: Baseline Artifact Hash와 Migration Checksum Evidence
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## CPF-SELF-DEV-030 [P0] Fresh Clone 독립 완료 Gate

- Owner: `최종 검증`
- 현재 Gap: Source 수정과 Evidence 생성·정본 갱신이 서로 SHA를 바꿔 반복 검증을 유발한다.
- 필수 구현: 최종 Push SHA의 Read-only Fresh Clone에서 검증하고 Source Defect와 Environment Blocker를 분리한다.
- 완료 검증: Codex 1회 통합 검증·Clean Tree·Exact-SHA Evidence
- 완료 금지: File·Menu·Class·Script·Matrix만 존재하거나 실제 Consumer·Runtime 근거가 없는 경우.

## 최종 완료 조건

- 30개 자체 개발 Requirement가 실제 Source·Consumer·Test·Evidence로 연결된다.
- Batch 세부 Menu가 조회 전용 Wrapper가 아니라 운영 Workflow를 제공한다.
- Online 정의·진단·Error·Message·Deployment가 First-class 기능으로 연결된다.
- Batch와 Online이 동일 식별자와 Causal Timeline으로 양방향 추적된다.
- ADM/BZA 전체 OpenAPI와 Generated Client가 실제 화면에서 사용된다.
- 전체 ADM Route가 3개 Browser에서 상용 Page Contract를 통과한다.
- Oracle·PostgreSQL·MariaDB Upgrade·Rollback·Reapply가 Exact-SHA Evidence로 남는다.
- 최종 Push SHA Fresh Clone에서 Source 수정 없이 독립 검증을 통과한다.