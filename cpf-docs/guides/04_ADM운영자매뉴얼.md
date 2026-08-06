# CPF ADM 운영자 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 기준 Commit: `ee977cf66c251081df78ea5e9675b81c3dfafa59` (`06_07`)
> 기준일: `2026-08-06 Asia/Seoul`

| 항목 | 내용 |
|---|---|
| 주 독자 | 조회자·운영자·승인자·보안담당자·운영관리자, 시스템 운영을 처음 맡는 담당자 |
| 이 문서로 완료할 일 | ADM 화면에서 상태를 읽고 권한에 맞게 조치하며 UNKNOWN_RESULT·PARTIAL·NACK·DRIFT를 대사·복구한다. |
| 읽는 방식 | 처음 접하는 독자는 앞에서부터 실습 순서로 읽고, 숙련자는 장별 판단표와 참조 경로를 사용한다. |
| 설명 원칙 | 제품 기능은 사용할 수 있는 상태로 설명한다. 실제 Source·SQL·API·Config·Frontend·Script·Test의 이름과 경계를 유지한다. |


![ADM 장애 판정 Tree](../assets/manuals/cpf-adm-incident-decision.svg)

## 1. 운영자가 먼저 알아야 할 원칙

1. 화면 Toast는 업무 상태 정본이 아니다.
2. 목록 Row는 상세를 다시 조회하기 전까지 오래된 정보일 수 있다.
3. 조회와 상태 변경 권한은 다르다.
4. 상태 변경은 Reason·Expected Version·Idempotency·Approval을 확인한다.
5. Timeout 뒤 같은 Button을 다시 누르기 전에 Operation을 조회한다.
6. `PARTIAL`은 성공 Target을 보존한다.
7. `UNKNOWN_RESULT`는 Target·Owner 원장을 대사한다.
8. Download와 원문 조회는 별도 Permission·Reason·Audit를 사용한다.

## 2. 역할과 금지 행동

| 역할 | 할 수 있는 일 | 하지 말아야 할 일 |
|---|---|---|
| 조회자 | 검색·상세·Masked Field 확인 | 상태 변경·원문 Download |
| 운영자 | 허용된 Command 요청·복구 실행 | 자기 승인·Owner DB 직접 수정 |
| 승인자 | Snapshot·Reason·Policy 검토 | 요청 Payload 수정·결과 추정 |
| 보안담당자 | Permission·MFA·Secret·Break-glass | Secret 평문 조회·감사 삭제 |
| 운영관리자 | Incident·SLA·교대·정책 | 미대사 Unknown 강제 종결 |

## 3. 공통 화면 읽는 순서

1. 상단의 환경·기준시각·Feature Flag를 확인한다.
2. 검색 범위를 필요한 최소 기간·조직·서비스로 제한한다.
3. 목록에서 상태·Version·Owner·Updated At를 읽는다.
4. 상세에서 Transaction·Operation·Attempt·Target·Error·Audit를 연결한다.
5. 변경 전 Target Snapshot과 Permission을 확인한다.
6. 실행 후 같은 조건으로 재조회한다.
7. Owner 원장과 Audit가 일치할 때 종결한다.

## 4. 상태별 행동

| 상태 | 판정 | 운영 행동 |
|---|---|---|
| `REQUESTED` | 요청 수락, 실행 전 | 중복 요청 금지 |
| `RUNNING` | 진행 Evidence 존재 | Heartbeat·Deadline 확인 |
| `SUCCEEDED` | 결과 확정 | 재실행 금지 |
| `FAILED` | 결정적 실패 | 원인 제거 후 새 Operation |
| `UNKNOWN_RESULT` | 결과 Evidence 부족 | Target·Owner 대사, Blind Retry 금지 |
| `PARTIAL` | Target 결과 혼합 | 성공 Target 보존 |
| `NACK` | Target이 적용 거부 | 실패 Target Error 확인 |
| `DRIFT` | Desired와 Observed 불일치 | Reconcile 또는 LKG Rollback |

## 5. 교대 인계 최소 항목

- 환경·기준시각.
- Incident·Operation·Target ID.
- 현재 상태와 마지막 Evidence.
- 실행한 조치와 Reason·Approval.
- 성공 Target과 미종결 Target.
- 다음 판정 시각과 담당자.
- Rollback Point와 금지할 반복 조치.

## 6. Route 전체 지도

| No | Route | Menu | 화면 | Group | Risk |
|---|---|---|---|---|---|
| 1 | / | DASHBOARD | 통합 운영 Dashboard | home | MEDIUM |
| 2 | /topology | TOPOLOGY | 서비스 토폴로지 | home | MEDIUM |
| 3 | /capacity | CAPACITY | Online Runtime Diagnostics | home | MEDIUM |
| 4 | /logs | LOG_LIST | 거래 로그 | monitoring | MEDIUM |
| 5 | /transactionGroups | LOG_LIST | Online·Batch 통합 Trace | online | MEDIUM |
| 6 | /transactions | TRANSACTION_META | 온라인 거래 정의 | online | HIGH |
| 7 | /remoteLogs | REMOTE_LOG | 원격 로그 | monitoring | MEDIUM |
| 8 | /auditLogs | AUDIT_LOG | 감사 로그 | monitoring | MEDIUM |
| 9 | /logLevel | DYNAMIC_LOG | 동적 로그 | monitoring | HIGH |
| 10 | /logPolicies | LOG_POLICY | 로그 정책 | monitoring | MEDIUM |
| 11 | /standardExecutions | STANDARD_EXECUTION | 표준 실행 | online | MEDIUM |
| 12 | /channelPolicy | CHANNEL_POLICY | 채널 정책 | online | HIGH |
| 13 | /serviceRegistry | SERVICE_REGISTRY | 서비스 레지스트리 | online | MEDIUM |
| 14 | /runtimeControl | RUNTIME_CONTROL | Deployment·Promotion·Rollback | online | HIGH |
| 15 | /maintenance | MAINTENANCE | 점검·Drain | framework | HIGH |
| 16 | /cache | CACHE | 캐시 | framework | HIGH |
| 17 | /configs | CONFIG | 설정 | framework | HIGH |
| 18 | /responseCodes | RESPONSE_CODE | 응답코드 | framework | MEDIUM |
| 19 | /businessCalendar | BUSINESS_CALENDAR | 영업일 · 휴일 | framework | MEDIUM |
| 20 | /recoveryCenter | RECOVERY_CENTER | 복구 센터 | monitoring | MEDIUM |
| 21 | /incidents | INCIDENT | Error·Unknown Result | monitoring | HIGH |
| 22 | /reliability | RELIABILITY | Analysis Center | monitoring | MEDIUM |
| 23 | /notifications | NOTIFICATION | 알림 | integration | MEDIUM |
| 24 | /batch | BATCH | Batch / Center-Cut | batch | MEDIUM |
| 25 | /batch-overview | BATCH_OVERVIEW | Batch Overview | batch | MEDIUM |
| 26 | /batch-runtime | BATCH_RUNTIME | Runtime Topology | batch | HIGH |
| 27 | /batch-instances | BATCH_INSTANCES | Runtime Instances | batch | MEDIUM |
| 28 | /batch-scheduler | BATCH_SCHEDULER | Scheduler HA | batch | MEDIUM |
| 29 | /batch-worker-pools | BATCH_WORKER_POOLS | Worker Pools | batch | MEDIUM |
| 30 | /batch-center-cut | BATCH_CENTER_CUT | Center-Cut | batch | MEDIUM |
| 31 | /batch-agents | BATCH_AGENTS | Host Agents | batch | MEDIUM |
| 32 | /batch-job-packs | BATCH_JOB_PACKS | Job Packs | batch | MEDIUM |
| 33 | /batch-executions | BATCH_EXECUTIONS | Executions | batch | MEDIUM |
| 34 | /batch-deployment | BATCH_DEPLOYMENT | Deployment / Rollback | batch | HIGH |
| 35 | /batch-recovery | BATCH_RECOVERY | Recovery / Unknown | monitoring | MEDIUM |
| 36 | /batch-leases | BATCH_LEASES | Lease / Fencing | monitoring | MEDIUM |
| 37 | /batch-alerts | BATCH_ALERTS | Batch Alerts | monitoring | MEDIUM |
| 38 | /batch-audit | BATCH_AUDIT | Audit / Evidence | monitoring | MEDIUM |
| 39 | /workers | WORKER | Agent / Worker | batch | MEDIUM |
| 40 | /downloads | DOWNLOAD | 다운로드 | integration | MEDIUM |
| 41 | /file-jobs | FILE_JOB | 대량파일 Job | batch | MEDIUM |
| 42 | /messages | MESSAGE | 전문·Protocol Message | integration | MEDIUM |
| 43 | /codes | CODE | 코드 | framework | MEDIUM |
| 44 | /gateway-dashboard | GATEWAY_DASHBOARD | Gateway 대시보드 | online | MEDIUM |
| 45 | /gateway-servers | GATEWAY_SERVERS | Gateway 연동 서버 | online | MEDIUM |
| 46 | /gateway-groups | GATEWAY_GROUPS | Gateway 서버 그룹 | online | MEDIUM |
| 47 | /gateway-routes | GATEWAY_ROUTES | Gateway 경로·라우팅 | online | MEDIUM |
| 48 | /gateway-security | GATEWAY_SECURITY | Gateway 보안·제한 | online | HIGH |
| 49 | /gateway-health | GATEWAY_HEALTH | Gateway Health·연결시험 | online | MEDIUM |
| 50 | /gateway-transactions | GATEWAY_TRANSACTIONS | Gateway 거래 조회 | online | MEDIUM |
| 51 | /gateway-log-policies | GATEWAY_LOG_POLICY | Gateway 로그 정책 | online | MEDIUM |
| 52 | /gateway-apply-status | GATEWAY_APPLY_STATUS | Gateway 적용 상태·이력 | online | MEDIUM |
| 53 | /permissions | PERMISSION | 권한 | framework | MEDIUM |
| 54 | /password | PASSWORD | 비밀번호 | framework | HIGH |
| 55 | /security | SECURITY | 보안 | framework | HIGH |
| 56 | /operators | OPERATOR | 운영자 | framework | HIGH |
| 57 | /secrets | SECRET | Secret / Key | framework | HIGH |
| 58 | /approvals | APPROVAL | 위험조치 승인 | framework | HIGH |
| 59 | /breakGlass | BREAK_GLASS | Break-glass | framework | HIGH |
| 60 | /featureFlags | FEATURE_FLAG | Feature Flag | framework | CRITICAL |
| 61 | /integrationClosure | INTEGRATION_CLOSURE | 통합 운영 정정 승인 | integration | CRITICAL |
| 62 | /openApiOperations | OPENAPI_OPERATIONS | OpenAPI 운영 | framework | HIGH |
| 63 | /resiliencePolicies | RESILIENCE_POLICY | Resilience 정책 | framework | CRITICAL |


## 7. 화면별 운영 카드

각 카드는 화면에서 완료할 일과 오류 시 다음 행동을 먼저 보여 준다. 전체 Operation ID는 `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`와 OpenAPI Runtime Inventory를 따른다.

### 7.1. 통합 운영 Dashboard — `/` {#dashboard}

| 항목 | 내용 |
|---|---|
| Route ID | `dashboard` |
| Menu ID | `DASHBOARD` |
| Group·Risk | `home` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 서비스·Batch·Broker의 이상을 첫 화면에서 분류 |
| 기본 검색 | 환경, 서비스, 기간, 심각도 |
| 핵심 표시값 | Readiness, Liveness, Version, UNKNOWN, DLQ, Outbox 적체 |
| 주요 조치 | 이상 카드 이동 |
| 정상 판정 | 집계 기준시각과 상세 화면 건수가 일치 |
| 대표 장애 | 오래된 Snapshot·일부 Owner Timeout |
| 복구 기준 | 기준시각을 맞춰 재조회하고 Owner Health 확인 |


### 7.2. 서비스 토폴로지 — `/topology` {#topology}

| 항목 | 내용 |
|---|---|
| Route ID | `topology` |
| Menu ID | `TOPOLOGY` |
| Group·Risk | `home` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Service·Instance·Endpoint와 Health 관계 확인 |
| 기본 검색 | 환경, Service ID, Zone |
| 핵심 표시값 | Instance, Version, Endpoint, Health, Last Seen |
| 주요 조치 | 상세 이동 |
| 정상 판정 | Registry와 Runtime Heartbeat가 일치 |
| 대표 장애 | Ghost Instance·중복 Endpoint |
| 복구 기준 | Last Seen과 Runtime 원장을 대사해 비활성화 |


### 7.3. Online Runtime Diagnostics — `/capacity` {#capacity}

| 항목 | 내용 |
|---|---|
| Route ID | `capacity` |
| Menu ID | `CAPACITY` |
| Group·Risk | `home` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Thread·Connection·Queue·Lag의 병목 식별 |
| 기본 검색 | 서비스, Instance, 기간 |
| 핵심 표시값 | Pool 사용률, Queue, Outbox, Inbox, File 전송 |
| 주요 조치 | 진단 상세 |
| 정상 판정 | 관측값과 Health·업무 지연이 같은 시각대 |
| 대표 장애 | Metric 누락·고카디널리티 |
| 복구 기준 | Collector와 Instance 상태를 확인해 범위를 좁힘 |


### 7.4. 거래 로그 — `/logs` {#logs}

| 항목 | 내용 |
|---|---|
| Route ID | `logs` |
| Menu ID | `LOG_LIST` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Transaction과 Error 로그 조회·Export |
| 기본 검색 | Transaction ID, Trace ID, 기간, Level |
| 핵심 표시값 | 시각, Service, Operation, Error Code, Masking |
| 주요 조치 | 상세, Export 요청 |
| 정상 판정 | 검색 조건과 Download Audit가 연결 |
| 대표 장애 | 원문 노출·Export 만료 |
| 복구 기준 | Masking 권한과 Download Token을 재확인 |


### 7.5. Online·Batch 통합 Trace — `/transactionGroups` {#transactionGroups}

| 항목 | 내용 |
|---|---|
| Route ID | `transactionGroups` |
| Menu ID | `LOG_LIST` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 온라인·Batch·외부 Attempt Timeline 연결 |
| 기본 검색 | Transaction ID, Trace ID, Business ID |
| 핵심 표시값 | Segment, Attempt, Target, Duration, Status |
| 주요 조치 | Timeline 상세 |
| 정상 판정 | 동일 식별자로 모든 Segment가 연결 |
| 대표 장애 | Trace 단절·Clock 차이 |
| 복구 기준 | Header·UTC·업무시각을 대사해 단절 지점 확인 |


### 7.6. 온라인 거래 정의 — `/transactions` {#transactions}

| 항목 | 내용 |
|---|---|
| Route ID | `transactions` |
| Menu ID | `TRANSACTION_META` |
| Group·Risk | `online` · `HIGH` |
| 이 화면으로 완료하는 일 | 거래 정의와 상태·정책을 조회·변경 |
| 기본 검색 | 거래 Code, 상태, Owner |
| 핵심 표시값 | Code, Version, Timeout, Idempotency, 상태 |
| 주요 조치 | Scan, 비활성화 |
| 정상 판정 | 정의 Version과 Runtime 적용 상태 일치 |
| 대표 장애 | 사용 중 비활성·Version 충돌 |
| 복구 기준 | Consumer 영향 확인 뒤 새 Version으로 변경 |


### 7.7. 원격 로그 — `/remoteLogs` {#remoteLogs}

| 항목 | 내용 |
|---|---|
| Route ID | `remoteLogs` |
| Menu ID | `REMOTE_LOG` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 원격 Instance 로그 Preview와 Support Bundle |
| 기본 검색 | 서비스, Instance, 기간, Pattern |
| 핵심 표시값 | 파일, Size, Modified, Masking 상태 |
| 주요 조치 | Preview, Bundle 생성, Download |
| 정상 판정 | Bundle Manifest와 Hash·Masking Report 존재 |
| 대표 장애 | 파일 읽기 실패·Size 초과 |
| 복구 기준 | 범위를 축소하고 실패 파일을 Manifest에 기록 |


### 7.8. 감사 로그 — `/auditLogs` {#auditLogs}

| 항목 | 내용 |
|---|---|
| Route ID | `auditLogs` |
| Menu ID | `AUDIT_LOG` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Actor·Reason·Before/After·Delivery 확인 |
| 기본 검색 | Actor, Resource, Operation, 기간 |
| 핵심 표시값 | Audit ID, Action, Result, Delivery |
| 주요 조치 | 상세, Delivery Retry |
| 정상 판정 | 업무 Operation과 Audit가 같은 식별자 |
| 대표 장애 | Delivery 실패·Hash 불일치 |
| 복구 기준 | 원본 Audit 보존 후 Delivery만 재시도 |


### 7.9. 동적 로그 — `/logLevel` {#logLevel}

| 항목 | 내용 |
|---|---|
| Route ID | `logLevel` |
| Menu ID | `DYNAMIC_LOG` |
| Group·Risk | `monitoring` · `HIGH` |
| 이 화면으로 완료하는 일 | 기간이 제한된 Log Level 규칙 운영 |
| 기본 검색 | 서비스, Logger, Level, 만료 |
| 핵심 표시값 | Rule, 대상, Level, 시작·만료, 상태 |
| 주요 조치 | 등록, 제거 |
| 정상 판정 | 만료 후 원래 Level로 복귀 |
| 대표 장애 | 광범위 DEBUG·만료 누락 |
| 복구 기준 | 해당 규칙을 제거하고 Log Volume 확인 |


### 7.10. 로그 정책 — `/logPolicies` {#logPolicies}

| 항목 | 내용 |
|---|---|
| Route ID | `logPolicies` |
| Menu ID | `LOG_POLICY` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 수집·Masking·보존·Trace Boost 정책 운영 |
| 기본 검색 | 정책 Code, 상태, 대상 |
| 핵심 표시값 | Version, Masking, Retention, 배포 상태 |
| 주요 조치 | 생성, 수정, 비활성, Refresh |
| 정상 판정 | Target별 Applied Version 일치 |
| 대표 장애 | 부분 배포·Cache stale |
| 복구 기준 | Distribution Status에서 실패 Target만 Refresh |


### 7.11. 표준 실행 — `/standardExecutions` {#standardExecutions}

| 항목 | 내용 |
|---|---|
| Route ID | `standardExecutions` |
| Menu ID | `STANDARD_EXECUTION` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 표준 Transaction 실행 결과 조회 |
| 기본 검색 | Execution ID, 거래 Code, 기간 |
| 핵심 표시값 | 상태, 시작·종료, Error, Operation |
| 주요 조치 | 상세 |
| 정상 판정 | 요청·응답·Audit 식별자 일치 |
| 대표 장애 | 결과 누락·Timeout |
| 복구 기준 | Transaction Group과 Owner 원장 대사 |


### 7.12. 채널 정책 — `/channelPolicy` {#channelPolicy}

| 항목 | 내용 |
|---|---|
| Route ID | `channelPolicy` |
| Menu ID | `CHANNEL_POLICY` |
| Group·Risk | `online` · `HIGH` |
| 이 화면으로 완료하는 일 | 채널별 Header·Timeout·실행 정책 관리 |
| 기본 검색 | Channel ID, 상태, Version |
| 핵심 표시값 | Header, Timeout, Policy, Applied Version |
| 주요 조치 | 저장, Export, Import |
| 정상 판정 | Snapshot과 Runtime 정책 일치 |
| 대표 장애 | 잘못된 Import·부분 적용 |
| 복구 기준 | Import Preview와 Target ACK를 확인해 Rollback |


### 7.13. 서비스 레지스트리 — `/serviceRegistry` {#serviceRegistry}

| 항목 | 내용 |
|---|---|
| Route ID | `serviceRegistry` |
| Menu ID | `SERVICE_REGISTRY` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Service·Instance·Endpoint 등록과 호출 상태 운영 |
| 기본 검색 | Service ID, Instance, 상태 |
| 핵심 표시값 | Endpoint, Capability, Circuit, Routing |
| 주요 조치 | 저장, 상태 변경, 삭제 |
| 정상 판정 | 활성 Endpoint와 Health가 일치 |
| 대표 장애 | 참조 중 삭제·Ghost |
| 복구 기준 | Call History와 Consumer 참조를 확인해 정리 |


### 7.14. Deployment·Promotion·Rollback — `/runtimeControl` {#runtimeControl}

| 항목 | 내용 |
|---|---|
| Route ID | `runtimeControl` |
| Menu ID | `RUNTIME_CONTROL` |
| Group·Risk | `online` · `HIGH` |
| 이 화면으로 완료하는 일 | Artifact·Config 변경의 Preview·Canary·Promotion·Rollback |
| 기본 검색 | 환경, Group, Version, Operation |
| 핵심 표시값 | Desired, Observed, ACK, NACK, Drift |
| 주요 조치 | Preview, 생성, 취소, Rollback |
| 정상 판정 | 모든 Target의 Observed Version과 Audit 일치 |
| 대표 장애 | NACK·Timeout·PARTIAL |
| 복구 기준 | 성공 Target 보존 후 실패 Target 대사·LKG 복원 |



**부분 적용 판정**

Target별 `Desired Version`, `Observed Version`, `ACK/NACK`, Health를 비교한다. 일부 Target만 성공하면 전체 재실행하지 않고 실패 Target만 복구하거나 승인된 LKG로 되돌린다.

### 7.15. 점검·Drain — `/maintenance` {#maintenance}

| 항목 | 내용 |
|---|---|
| Route ID | `maintenance` |
| Menu ID | `MAINTENANCE` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | 점검 Mode와 Traffic Drain 실행 |
| 기본 검색 | Service, Instance, Window |
| 핵심 표시값 | 현재 상태, Active 요청, Drain 진행 |
| 주요 조치 | 점검 시작·종료 |
| 정상 판정 | 신규 유입 차단과 기존 처리 종료 |
| 대표 장애 | Drain Timeout·세션 잔존 |
| 복구 기준 | 활성 요청을 확인하고 단계별 Rollback |


### 7.16. 캐시 — `/cache` {#cache}

| 항목 | 내용 |
|---|---|
| Route ID | `cache` |
| Menu ID | `CACHE` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | Cache 상태·Key·Namespace·Durable invalidation 운영 |
| 기본 검색 | Provider, Tenant, Namespace, Key |
| 핵심 표시값 | Entry, TTL, Event, Checkpoint, Lag |
| 주요 조치 | Refresh, Key/Namespace Evict, Reconcile |
| 정상 판정 | Owner 데이터와 Cache가 일치하고 Lag 0 |
| 대표 장애 | Signal 유실·Valkey Down |
| 복구 기준 | Durable Ledger의 Checkpoint 이후 Event Reconcile |


### 7.17. 설정 — `/configs` {#configs}

| 항목 | 내용 |
|---|---|
| Route ID | `configs` |
| Menu ID | `CONFIG` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | Config Key·Source·Version·Target 적용 운영 |
| 기본 검색 | Key, Profile, Service, 상태 |
| 핵심 표시값 | Type, Value Source, Secret, Restart, Version |
| 주요 조치 | 생성, 수정, 삭제 |
| 정상 판정 | Desired·Observed와 Consumer Health 일치 |
| 대표 장애 | 범위 오류·부분 적용 |
| 복구 기준 | 이전 값/LKG 복원 뒤 Drift 0 확인 |


### 7.18. 응답코드 — `/responseCodes` {#responseCodes}

| 항목 | 내용 |
|---|---|
| Route ID | `responseCodes` |
| Menu ID | `RESPONSE_CODE` |
| Group·Risk | `framework` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 업무·기술 Error Code와 HTTP Mapping 관리 |
| 기본 검색 | Code, HTTP, 상태 |
| 핵심 표시값 | Message, Retryable, Owner, Version |
| 주요 조치 | 생성, 수정, 삭제 |
| 정상 판정 | API 응답과 Catalog Mapping 일치 |
| 대표 장애 | 사용 중 Code 삭제 |
| 복구 기준 | Consumer·OpenAPI 영향 확인 후 새 Version 적용 |


### 7.19. 영업일 · 휴일 — `/businessCalendar` {#businessCalendar}

| 항목 | 내용 |
|---|---|
| Route ID | `businessCalendar` |
| Menu ID | `BUSINESS_CALENDAR` |
| Group·Risk | `framework` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 기관별 영업일·휴일과 기준일 계산 운영 |
| 기본 검색 | Calendar ID, 기관, 기간 |
| 핵심 표시값 | Date, Day Type, Version, Applied |
| 주요 조치 | 저장, 삭제, 기준일 계산 |
| 정상 판정 | Row·Audit·Refresh Event와 Consumer 결과 일치 |
| 대표 장애 | 날짜 충돌·Refresh Lag |
| 복구 기준 | Calendar Row와 Consumer Checkpoint 대사 |


### 7.20. 복구 센터 — `/recoveryCenter` {#recoveryCenter}

| 항목 | 내용 |
|---|---|
| Route ID | `recoveryCenter` |
| Menu ID | `RECOVERY_CENTER` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Trace Poison·Unknown·Broker DLQ 복구 실행 |
| 기본 검색 | Operation ID, 상태, Owner |
| 핵심 표시값 | 대상, 원인, Evidence, 다음 행동 |
| 주요 조치 | Recovery, Replay 요청, Resolve |
| 정상 판정 | Owner 결과와 복구 Audit 일치 |
| 대표 장애 | Blind Retry·중복 Replay |
| 복구 기준 | 원본 Attempt 조회 후 승인된 후속 Operation 생성 |


### 7.21. Error·Unknown Result — `/incidents` {#incidents}

| 항목 | 내용 |
|---|---|
| Route ID | `incidents` |
| Menu ID | `INCIDENT` |
| Group·Risk | `monitoring` · `HIGH` |
| 이 화면으로 완료하는 일 | Incident 등록·전이·Escalation·종결 |
| 기본 검색 | 심각도, 상태, 서비스, 기간 |
| 핵심 표시값 | Incident, Owner, SLA, Timeline, Unknown |
| 주요 조치 | Acknowledge, Escalate, Resolve, Reopen |
| 정상 판정 | 종결 근거와 Owner 정상 상태 일치 |
| 대표 장애 | 근거 없는 Resolve·SLA 초과 |
| 복구 기준 | Signal·Operation·Owner 원장을 다시 대사 |


### 7.22. Analysis Center — `/reliability` {#reliability}

| 항목 | 내용 |
|---|---|
| Route ID | `reliability` |
| Menu ID | `RELIABILITY` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Outbox·Inbox·Idempotency·DLQ·Unknown 통합 분석 |
| 기본 검색 | 업무 Key, Operation, Event, 상태 |
| 핵심 표시값 | Ledger, Attempt, Checkpoint, Age |
| 주요 조치 | 상세 이동 |
| 정상 판정 | 각 원장의 식별자와 상태 전이 일치 |
| 대표 장애 | Ledger 불일치·장기 Unknown |
| 복구 기준 | 가장 이른 불일치 지점부터 Reconcile |


### 7.23. 알림 — `/notifications` {#notifications}

| 항목 | 내용 |
|---|---|
| Route ID | `notifications` |
| Menu ID | `NOTIFICATION` |
| Group·Risk | `integration` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Rule·Template·Delivery·Receipt·DLQ 운영 |
| 기본 검색 | Channel, Rule, 수신자, 상태 |
| 핵심 표시값 | Delivery, Attempt, Provider ID, Receipt |
| 주요 조치 | 저장, Test, Retry, Cancel |
| 정상 판정 | Provider Receipt와 Delivery 상태 일치 |
| 대표 장애 | Provider 미설정·응답 유실 |
| 복구 기준 | Provider ID 대사 후 실패 확정 Delivery만 Retry |


### 7.24. Batch / Center-Cut — `/batch` {#batch}

| 항목 | 내용 |
|---|---|
| Route ID | `batch` |
| Menu ID | `BATCH` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Job·Schedule·Execution 통합 Workbench |
| 기본 검색 | Job, 업무일, 상태 |
| 핵심 표시값 | Job, Schedule, Execution, Worker |
| 주요 조치 | 등록, 실행, 상세 |
| 정상 판정 | Metadata와 업무 대사 결과 일치 |
| 대표 장애 | 완료 상태지만 합계 불일치 |
| 복구 기준 | Execution 상세에서 업무 Count·Amount 대사 |


### 7.25. Batch Overview — `/batch-overview` {#batch-overview}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-overview` |
| Menu ID | `BATCH_OVERVIEW` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 배치 전체 상태와 병목 요약 |
| 기본 검색 | 업무일, Job Group, 상태 |
| 핵심 표시값 | 실행, 실패, Worker, Lock, Lag |
| 주요 조치 | 상세 이동 |
| 정상 판정 | 요약 건수와 각 Workbench 일치 |
| 대표 장애 | 오래된 집계·Owner Timeout |
| 복구 기준 | 기준시각을 맞추고 개별 화면 재조회 |


### 7.26. Runtime Topology — `/batch-runtime` {#batch-runtime}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-runtime` |
| Menu ID | `BATCH_RUNTIME` |
| Group·Risk | `batch` · `HIGH` |
| 이 화면으로 완료하는 일 | Runner·Worker·Agent Runtime Command 운영 |
| 기본 검색 | Runtime ID, Host, 상태 |
| 핵심 표시값 | Capability, Process, Heartbeat, Command |
| 주요 조치 | Start, Stop, Restart |
| 정상 판정 | Process 상태와 Command Evidence 일치 |
| 대표 장애 | 응답 유실·비종료 Process |
| 복구 기준 | Command ID와 Service Manager 상태 대사 |


### 7.27. Runtime Instances — `/batch-instances` {#batch-instances}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-instances` |
| Menu ID | `BATCH_INSTANCES` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 배치 Runtime Instance와 Version 확인 |
| 기본 검색 | Service, Instance, Zone |
| 핵심 표시값 | Version, Health, Last Seen, Capability |
| 주요 조치 | 상세 |
| 정상 판정 | Registry·Heartbeat·Process 상태 일치 |
| 대표 장애 | Ghost·Version skew |
| 복구 기준 | Last Seen과 Agent Evidence를 대사 |


### 7.28. Scheduler HA — `/batch-scheduler` {#batch-scheduler}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-scheduler` |
| Menu ID | `BATCH_SCHEDULER` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Schedule·Calendar·Misfire·Run-once 운영 |
| 기본 검색 | Schedule ID, Job, 상태 |
| 핵심 표시값 | Cron, Zone, Calendar, Next Fire, Owner |
| 주요 조치 | Simulation, Enable, Disable, Run once |
| 정상 판정 | 단일 Lease Owner와 다음 실행시각 일치 |
| 대표 장애 | 중복 Dispatch·Clock Skew |
| 복구 기준 | Schedule Disable 후 Trigger·Lease 대사 |


### 7.29. Worker Pools — `/batch-worker-pools` {#batch-worker-pools}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-worker-pools` |
| Menu ID | `BATCH_WORKER_POOLS` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Worker Capability·Pool·할당 상태 운영 |
| 기본 검색 | Pool, Capability, Zone |
| 핵심 표시값 | Worker, Capacity, Active, Queue |
| 주요 조치 | Runtime Command |
| 정상 판정 | 할당량과 실제 Worker 상태 일치 |
| 대표 장애 | 과부하·Capability 불일치 |
| 복구 기준 | 새 작업 유입을 줄이고 Worker 상태 재등록 |


### 7.30. Center-Cut — `/batch-center-cut` {#batch-center-cut}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-center-cut` |
| Menu ID | `BATCH_CENTER_CUT` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 대상 Preview·Execution·Item 결과·대사 |
| 기본 검색 | Job, Execution, 업무일, 상태 |
| 핵심 표시값 | Preview Hash, 대상, 성공, 실패, Unknown |
| 주요 조치 | FAILED 재처리, UNKNOWN 대사 |
| 정상 판정 | Preview·Result·업무 합계 일치 |
| 대표 장애 | Criteria Drift·부분 결과 |
| 복구 기준 | Execution별 실패·불명 Item만 조치 |



**Button 조건**

- FAILED Execution: 실패 재처리만 허용.
- UNKNOWN Execution: UNKNOWN 대사만 허용.
- 성공 Execution: 재처리 Button 비활성.
- Reason·Approval·Idempotency Key와 대상 Snapshot을 확인.

### 7.31. Host Agents — `/batch-agents` {#batch-agents}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-agents` |
| Menu ID | `BATCH_AGENTS` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Host Agent와 Process Command Evidence 확인 |
| 기본 검색 | Host, Agent, 상태 |
| 핵심 표시값 | Version, Last Seen, Command, Process |
| 주요 조치 | Runtime Command |
| 정상 판정 | Agent와 Service Manager 상태 일치 |
| 대표 장애 | Agent Offline·권한 오류 |
| 복구 기준 | Host 접근과 Agent Token·Process를 확인 |


### 7.32. Job Packs — `/batch-job-packs` {#batch-job-packs}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-job-packs` |
| Menu ID | `BATCH_JOB_PACKS` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Job Definition·Artifact·Checksum·Lifecycle 운영 |
| 기본 검색 | Pack ID, Job, Version |
| 핵심 표시값 | Definition, Artifact, Checksum, 상태 |
| 주요 조치 | Validate, Save, Transition |
| 정상 판정 | Definition과 Artifact Manifest 일치 |
| 대표 장애 | Checksum·Schema 오류 |
| 복구 기준 | LKG 유지 후 새 Version 검증 |


### 7.33. Executions — `/batch-executions` {#batch-executions}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-executions` |
| Menu ID | `BATCH_EXECUTIONS` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Job·Step Execution과 Stop·Retry 운영 |
| 기본 검색 | Execution ID, Job, 상태 |
| 핵심 표시값 | Parameter, Step Count, Exit, Checkpoint |
| 주요 조치 | Stop, Retry |
| 정상 판정 | Metadata와 업무 원장 일치 |
| 대표 장애 | Stale 상태·Checkpoint 불일치 |
| 복구 기준 | 마지막 Commit과 업무 Row 대사 후 Restart |


### 7.34. Deployment / Rollback — `/batch-deployment` {#batch-deployment}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-deployment` |
| Menu ID | `BATCH_DEPLOYMENT` |
| Group·Risk | `batch` · `HIGH` |
| 이 화면으로 완료하는 일 | Batch Runtime Artifact 배포·Rollback |
| 기본 검색 | Plan, Version, Target |
| 핵심 표시값 | Artifact, Checksum, Target ACK, 상태 |
| 주요 조치 | Plan 생성, Rollback |
| 정상 판정 | Target별 Version과 Health 일치 |
| 대표 장애 | PARTIAL·NACK |
| 복구 기준 | 성공 Target 보존 후 LKG로 실패 Target 복원 |


### 7.35. Recovery / Unknown — `/batch-recovery` {#batch-recovery}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-recovery` |
| Menu ID | `BATCH_RECOVERY` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Ghost Execution과 Unknown 결과 정상화 |
| 기본 검색 | Execution, Operation, 상태 |
| 핵심 표시값 | Ghost 후보, Evidence, Owner 상태 |
| 주요 조치 | Ghost 조치, Unknown Resolve |
| 정상 판정 | 종결 상태와 Metadata·업무 원장 일치 |
| 대표 장애 | 살아 있는 실행 오판 |
| 복구 기준 | Heartbeat·Process·Lease를 모두 확인 |


### 7.36. Lease / Fencing — `/batch-leases` {#batch-leases}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-leases` |
| Menu ID | `BATCH_LEASES` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Lease Holder·Token·만료와 stale writer 확인 |
| 기본 검색 | Resource, Holder, 상태 |
| 핵심 표시값 | Token, Acquired, Expires, Heartbeat |
| 주요 조치 | Release |
| 정상 판정 | 새 Holder Token이 이전보다 큼 |
| 대표 장애 | 강제 Release 뒤 이전 Writer 갱신 |
| 복구 기준 | Process 종료와 Token 차단 확인 후 Release |


### 7.37. Batch Alerts — `/batch-alerts` {#batch-alerts}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-alerts` |
| Menu ID | `BATCH_ALERTS` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Batch Unknown·DLQ·Outbox·실패 Alert 확인 |
| 기본 검색 | 심각도, Job, 기간 |
| 핵심 표시값 | Alert, Age, Owner, Operation |
| 주요 조치 | 상세 이동 |
| 정상 판정 | Alert 원인과 실제 상태 일치 |
| 대표 장애 | 중복 Alert·미종결 |
| 복구 기준 | 같은 업무 Key로 그룹화하고 Owner 이관 |


### 7.38. Audit / Evidence — `/batch-audit` {#batch-audit}

| 항목 | 내용 |
|---|---|
| Route ID | `batch-audit` |
| Menu ID | `BATCH_AUDIT` |
| Group·Risk | `monitoring` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 배치 실행·조치·배포 Audit 조회 |
| 기본 검색 | Execution, Actor, Action, 기간 |
| 핵심 표시값 | Operation, Before/After, Result, Delivery |
| 주요 조치 | 상세, Delivery Retry |
| 정상 판정 | Execution과 Audit·Artifact Hash 일치 |
| 대표 장애 | Audit Delivery 실패 |
| 복구 기준 | 원본 Audit 보존 후 Delivery만 재시도 |


### 7.39. Agent / Worker — `/workers` {#workers}

| 항목 | 내용 |
|---|---|
| Route ID | `workers` |
| Menu ID | `WORKER` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Worker·Instance·Capability 상태 조회 |
| 기본 검색 | Worker ID, Instance, Capability |
| 핵심 표시값 | Status, Active Job, Heartbeat, Version |
| 주요 조치 | 상세 |
| 정상 판정 | Worker Registry와 Runtime 상태 일치 |
| 대표 장애 | 중복 Worker ID·stale heartbeat |
| 복구 기준 | Instance 재등록과 Lease 상태 확인 |


### 7.40. 다운로드 — `/downloads` {#downloads}

| 항목 | 내용 |
|---|---|
| Route ID | `downloads` |
| Menu ID | `DOWNLOAD` |
| Group·Risk | `integration` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Download Policy·Audit·CSV 수령 운영 |
| 기본 검색 | 사용자, Resource, 기간 |
| 핵심 표시값 | Policy, Token, Expires, Rows, Audit |
| 주요 조치 | CSV Download |
| 정상 판정 | Token·행 제한·Audit 일치 |
| 대표 장애 | 만료 Token·권한 확대 |
| 복구 기준 | 새 Reason과 조건으로 Token 재발급 |


### 7.41. 대량파일 Job — `/file-jobs` {#file-jobs}

| 항목 | 내용 |
|---|---|
| Route ID | `file-jobs` |
| Menu ID | `FILE_JOB` |
| Group·Risk | `batch` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Upload·Preview·Apply·Retry·Rollback 운영 |
| 기본 검색 | Job, File ID, 상태 |
| 핵심 표시값 | Checksum, Row Count, 오류, Applied |
| 주요 조치 | Upload, Apply, Retry, Cancel, Rollback |
| 정상 판정 | File·Row·업무 원장 합계 일치 |
| 대표 장애 | Bad Row·PARTIAL·UNKNOWN |
| 복구 기준 | 성공 Row 보존 후 실패 Row만 재처리 |


### 7.42. 전문·Protocol Message — `/messages` {#messages}

| 항목 | 내용 |
|---|---|
| Route ID | `messages` |
| Menu ID | `MESSAGE` |
| Group·Risk | `integration` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 전문 정의·Message·Trace 조회 |
| 기본 검색 | Message Code, Version, Transaction |
| 핵심 표시값 | Layout, Charset, Status, Trace |
| 주요 조치 | 생성, 수정, 삭제 |
| 정상 판정 | Golden Bytes와 Decode 결과 일치 |
| 대표 장애 | 길이·Charset·MAC 오류 |
| 복구 기준 | 원문 보존 후 Layout Version으로 재파싱 |


### 7.43. 코드 — `/codes` {#codes}

| 항목 | 내용 |
|---|---|
| Route ID | `codes` |
| Menu ID | `CODE` |
| Group·Risk | `framework` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 업무 Code·Version·유효기간 관리 |
| 기본 검색 | Code Group, Code, 기준일 |
| 핵심 표시값 | Name, Value, Effective, Version |
| 주요 조치 | 생성, 수정, 삭제 |
| 정상 판정 | Consumer의 기준일 해석과 Row 일치 |
| 대표 장애 | 사용 중 폐기·Refresh Lag |
| 복구 기준 | 새 Version 적용 후 Consumer Checkpoint 대사 |


### 7.44. Gateway 대시보드 — `/gateway-dashboard` {#gateway-dashboard}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-dashboard` |
| Menu ID | `GATEWAY_DASHBOARD` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Gateway Capability·Traffic·Error·Apply 요약 |
| 기본 검색 | Route, Target, 기간 |
| 핵심 표시값 | RPS, Error, Circuit, Apply, Unknown |
| 주요 조치 | 상세 이동 |
| 정상 판정 | 요약과 Route·Target 상세 일치 |
| 대표 장애 | 집계 지연·Stream 단절 |
| 복구 기준 | Snapshot 기준시각과 Event Stream 확인 |


### 7.45. Gateway 연동 서버 — `/gateway-servers` {#gateway-servers}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-servers` |
| Menu ID | `GATEWAY_SERVERS` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Target Server Group Member 관리 |
| 기본 검색 | Group, Host, 상태 |
| 핵심 표시값 | URI, Zone, Weight, TLS, Health |
| 주요 조치 | 저장, 삭제 |
| 정상 판정 | 등록 Member와 Probe 결과 일치 |
| 대표 장애 | SSRF 대상·사용 중 삭제 |
| 복구 기준 | Binding 참조와 DNS·Allowlist 확인 |


### 7.46. Gateway 서버 그룹 — `/gateway-groups` {#gateway-groups}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-groups` |
| Menu ID | `GATEWAY_GROUPS` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 논리 Target Group과 Member 조합 관리 |
| 기본 검색 | Group ID, 상태 |
| 핵심 표시값 | Member 수, Weight, Zone, Version |
| 주요 조치 | 저장, 삭제 |
| 정상 판정 | Weight·가용 Member가 정책 충족 |
| 대표 장애 | Member 0·중복 Target |
| 복구 기준 | Member와 Binding 참조를 보정 |


### 7.47. Gateway 경로·라우팅 — `/gateway-routes` {#gateway-routes}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-routes` |
| Menu ID | `GATEWAY_ROUTES` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Predicate·Rewrite·Target Binding 운영 |
| 기본 검색 | Route ID, Path, Method, 상태 |
| 핵심 표시값 | Predicate, Rewrite, Group, Version |
| 주요 조치 | 저장, 상태 변경, 삭제 |
| 정상 판정 | Golden Request가 기대 Target으로 연결 |
| 대표 장애 | Route 충돌·Shadow |
| 복구 기준 | Validate 결과와 우선순위를 보정 |


### 7.48. Gateway 보안·제한 — `/gateway-security` {#gateway-security}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-security` |
| Menu ID | `GATEWAY_SECURITY` |
| Group·Risk | `online` · `HIGH` |
| 이 화면으로 완료하는 일 | Auth·Audience·HMAC·Nonce·Rate Limit 운영 |
| 기본 검색 | Route, Client, Policy |
| 핵심 표시값 | Auth, Audience, Key Version, Limit |
| 주요 조치 | 저장, 상태 변경 |
| 정상 판정 | Negative Test와 정책 적용 상태 일치 |
| 대표 장애 | Wrong Audience·Replay·Key 만료 |
| 복구 기준 | Key Rotation·Nonce Ledger·Target 상태 확인 |


### 7.49. Gateway Health·연결시험 — `/gateway-health` {#gateway-health}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-health` |
| Menu ID | `GATEWAY_HEALTH` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Target Connection Test와 Capability 확인 |
| 기본 검색 | Group, Target, Test 상태 |
| 핵심 표시값 | DNS, TLS, HTTP, Duration, Operation |
| 주요 조치 | Test 요청, 취소, 재검증 |
| 정상 판정 | Test Operation과 Target Probe 일치 |
| 대표 장애 | Timeout·TLS·응답 유실 |
| 복구 기준 | Operation 조회 후 결과 확정 또는 새 Test |


### 7.50. Gateway 거래 조회 — `/gateway-transactions` {#gateway-transactions}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-transactions` |
| Menu ID | `GATEWAY_TRANSACTIONS` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 외부 요청·Attempt·Target Trace 조회 |
| 기본 검색 | Transaction, Route, Client, 기간 |
| 핵심 표시값 | Attempt, Target, Status, Duration, Error |
| 주요 조치 | Trace 이동 |
| 정상 판정 | Gateway Attempt와 Owner 결과 연결 |
| 대표 장애 | 응답 유실·Trace 단절 |
| 복구 기준 | Idempotency Key·Target ID로 대사 |


### 7.51. Gateway 로그 정책 — `/gateway-log-policies` {#gateway-log-policies}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-log-policies` |
| Menu ID | `GATEWAY_LOG_POLICY` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Gateway Masking·Sampling·배포 상태 운영 |
| 기본 검색 | 정책, Route, 상태 |
| 핵심 표시값 | Version, Masking, Sampling, Applied |
| 주요 조치 | 상세 이동 |
| 정상 판정 | Target별 정책 Version 일치 |
| 대표 장애 | 민감 Header 노출·부분 배포 |
| 복구 기준 | 정책 Rollback과 Distribution 대사 |


### 7.52. Gateway 적용 상태·이력 — `/gateway-apply-status` {#gateway-apply-status}

| 항목 | 내용 |
|---|---|
| Route ID | `gateway-apply-status` |
| Menu ID | `GATEWAY_APPLY_STATUS` |
| Group·Risk | `online` · `MEDIUM` |
| 이 화면으로 완료하는 일 | 게시 Version의 Target ACK·NACK·Drift 확인 |
| 기본 검색 | Publish ID, Version, Target |
| 핵심 표시값 | Checksum, ACK, NACK, Observed, Drift |
| 주요 조치 | 상세 |
| 정상 판정 | 모든 Target이 승인 Snapshot과 일치 |
| 대표 장애 | PARTIAL·DRIFT |
| 복구 기준 | NACK Target 원인 제거 또는 LKG Rollback |


### 7.53. 권한 — `/permissions` {#permissions}

| 항목 | 내용 |
|---|---|
| Route ID | `permissions` |
| Menu ID | `PERMISSION` |
| Group·Risk | `framework` · `MEDIUM` |
| 이 화면으로 완료하는 일 | Menu·Button·API·Role Permission 운영 |
| 기본 검색 | Resource, Role, 상태 |
| 핵심 표시값 | Menu, Button, API, Effective, Version |
| 주요 조치 | 생성, 수정, 상태 변경 |
| 정상 판정 | Simulation과 실제 API 판정 일치 |
| 대표 장애 | Self lockout·Scope 확대 |
| 복구 기준 | 영향 Matrix와 Break-glass 경로 확인 |


### 7.54. 비밀번호 — `/password` {#password}

| 항목 | 내용 |
|---|---|
| Route ID | `password` |
| Menu ID | `PASSWORD` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | Password Policy·변경·초기화·Session 폐기 |
| 기본 검색 | Operator, 정책, 상태 |
| 핵심 표시값 | Policy, Attempts, Changed, Session |
| 주요 조치 | 변경, 초기화, Session 폐기 |
| 정상 판정 | 정책 적용과 Session 폐기 Audit 일치 |
| 대표 장애 | 본인 확인 실패·활성 Session 잔존 |
| 복구 기준 | MFA·관리자 승인 후 재수행 |


### 7.55. 보안 — `/security` {#security}

| 항목 | 내용 |
|---|---|
| Route ID | `security` |
| Menu ID | `SECURITY` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | MFA·IP Allowlist 상태 운영 |
| 기본 검색 | Operator, MFA 상태, IP |
| 핵심 표시값 | MFA Method, Verified, IP Range, Version |
| 주요 조치 | MFA 등록·검증·비활성, IP 저장 |
| 정상 판정 | Login Negative Test와 정책 일치 |
| 대표 장애 | MFA 우회·IP 차단 |
| 복구 기준 | Break-glass와 승인된 복구 절차 사용 |


### 7.56. 운영자 — `/operators` {#operators}

| 항목 | 내용 |
|---|---|
| Route ID | `operators` |
| Menu ID | `OPERATOR` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | 운영자 계정·Role·Session·연락처 운영 |
| 기본 검색 | ID, 상태, Role, 조직 |
| 핵심 표시값 | Status, Roles, Sessions, MFA, Masking |
| 주요 조치 | 생성, Role 변경, 잠금 해제, 상태 변경 |
| 정상 판정 | 계정·Role·Session·Audit 일치 |
| 대표 장애 | 권한 과다·Raw Contact 노출 |
| 복구 기준 | Role 비교와 Session 폐기 후 재검증 |


### 7.57. Secret / Key — `/secrets` {#secrets}

| 항목 | 내용 |
|---|---|
| Route ID | `secrets` |
| Menu ID | `SECRET` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | Secret Provider Metadata와 Rotation 운영 |
| 기본 검색 | Provider, Secret ID, Version |
| 핵심 표시값 | Metadata, Active Version, Expires, Consumer |
| 주요 조치 | Rotate |
| 정상 판정 | Consumer가 새 Version을 사용하고 Log 비노출 |
| 대표 장애 | 평문 노출·부분 Rotation |
| 복구 기준 | 이전 Version Grace와 실패 Consumer 대사 |


### 7.58. 위험조치 승인 — `/approvals` {#approvals}

| 항목 | 내용 |
|---|---|
| Route ID | `approvals` |
| Menu ID | `APPROVAL` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | 정책·요청·결정·실행 Snapshot 운영 |
| 기본 검색 | Approval ID, 상태, Action, 요청자 |
| 핵심 표시값 | Policy, Snapshot, Requester, Approver, Expiry |
| 주요 조치 | 요청, 결정, 실행 |
| 정상 판정 | 실행 Target이 승인 Snapshot과 일치 |
| 대표 장애 | 자기 승인·만료·Snapshot Drift |
| 복구 기준 | 새 승인 요청 또는 실행 중단 |


### 7.59. Break-glass — `/breakGlass` {#breakGlass}

| 항목 | 내용 |
|---|---|
| Route ID | `breakGlass` |
| Menu ID | `BREAK_GLASS` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | 비상 권한 Session 개설·검토·종료 |
| 기본 검색 | Session, 사용자, 상태 |
| 핵심 표시값 | Scope, Reason, Started, Expires, Review |
| 주요 조치 | 개설, 검토, 종료 |
| 정상 판정 | 기간·Scope·사후 검토 Audit 존재 |
| 대표 장애 | 만료 후 잔존·Scope 초과 |
| 복구 기준 | Session을 종료하고 전체 Action 검토 |


### 7.60. Feature Flag — `/featureFlags` {#featureFlags}

| 항목 | 내용 |
|---|---|
| Route ID | `featureFlags` |
| Menu ID | `FEATURE_FLAG` |
| Group·Risk | `framework` · `CRITICAL` |
| 이 화면으로 완료하는 일 | Typed Flag·Override·Kill Switch 운영 |
| 기본 검색 | Flag, 환경, 상태 |
| 핵심 표시값 | Type, Default, Rule, Override, Expiry |
| 주요 조치 | 평가, Override 요청·승인·폐기, Kill |
| 정상 판정 | Evaluation Evidence와 Runtime 결과 일치 |
| 대표 장애 | Rule 중첩·Override 만료 누락 |
| 복구 기준 | Override 폐기 또는 이전 Version 복원 |



**Kill Switch 주의**

적용 대상·만료·Rollback 조건이 없는 Kill Switch를 실행하지 않는다. 실행 뒤 Evaluation Evidence와 각 Instance의 Applied Version을 확인한다.

### 7.61. 통합 운영 정정 승인 — `/integrationClosure` {#integrationClosure}

| 항목 | 내용 |
|---|---|
| Route ID | `integrationClosure` |
| Menu ID | `INTEGRATION_CLOSURE` |
| Group·Risk | `integration` · `CRITICAL` |
| 이 화면으로 완료하는 일 | Data Quality 정정 승인·Replay, Webhook DLQ, Crypto·Time 상태 |
| 기본 검색 | Quarantine ID, Approval ID, Delivery ID |
| 핵심 표시값 | Version, Rule 위반, Approval, Webhook Attempt |
| 주요 조치 | 승인 요청, 승인 검증 후 실행, Replay |
| 정상 판정 | Server Snapshot·Owner Version·Audit 일치 |
| 대표 장애 | 승인 우회·Version Conflict·UNKNOWN |
| 복구 기준 | Approval ID로 상태 조회 후 Reconcile |



**승인 Snapshot 계약**

- 승인 요청 입력: Quarantine ID, Expected Version, Idempotency Key, Reason, Corrected JSON.
- Server가 정정 내용을 Snapshot으로 보존한다.
- 실행 입력: Approval ID와 Reason.
- 실행 단계에서 Corrected JSON이나 `approved` Boolean을 다시 받지 않는다.
- 만료·반려·Version Conflict는 새 승인 요청으로 처리한다.

### 7.62. OpenAPI 운영 — `/openApiOperations` {#openApiOperations}

| 항목 | 내용 |
|---|---|
| Route ID | `openApiOperations` |
| Menu ID | `OPENAPI_OPERATIONS` |
| Group·Risk | `framework` · `HIGH` |
| 이 화면으로 완료하는 일 | OpenAPI Snapshot 상태와 Refresh 운영 |
| 기본 검색 | Instance, Version, 상태 |
| 핵심 표시값 | Title, API Version, Hash, Refreshed |
| 주요 조치 | Refresh |
| 정상 판정 | Snapshot Hash와 Runtime Controller 일치 |
| 대표 장애 | 중복 Operation·Refresh 제한 |
| 복구 기준 | Schema 오류 수정 후 승인된 Refresh |


### 7.63. Resilience 정책 — `/resiliencePolicies` {#resiliencePolicies}

| 항목 | 내용 |
|---|---|
| Route ID | `resiliencePolicies` |
| Menu ID | `RESILIENCE_POLICY` |
| Group·Risk | `framework` · `CRITICAL` |
| 이 화면으로 완료하는 일 | Timeout·Retry·Circuit·Bulkhead 정책 Version 운영 |
| 기본 검색 | Operation, 상태, Version |
| 핵심 표시값 | Timeout, Retry, Circuit, Bulkhead, Applied |
| 주요 조치 | 요청, 승인, 반려 |
| 정상 판정 | 상위·하위 Timeout Budget과 Runtime Metric 일치 |
| 대표 장애 | Retry Storm·부분 적용 |
| 복구 기준 | 이전 정책 Version Rollback 또는 새 승인 |



## 8. 증상별 바로가기

| 증상 | 먼저 볼 화면 | 다음 화면 |
|---|---|---|
| 요청 결과를 모름 | `/incidents`, `/reliability` | Owner 상세·`/recoveryCenter` |
| Batch가 멈춤 | `/batch-executions` | `/batch-runtime`, `/batch-leases` |
| 일부 Instance만 설정 적용 | `/runtimeControl` | `/configs`, `/incidents` |
| Gateway Target NACK | `/gateway-apply-status` | `/gateway-health`, `/gateway-routes` |
| 데이터 정정 필요 | `/integrationClosure` | `/approvals`, `/auditLogs` |
| DLQ 적체 | `/reliability`, `/notifications` | `/recoveryCenter` |
| 권한은 있는데 Button이 없음 | `/permissions` | 대상 Route·`/operators` |
| Secret Rotation 뒤 오류 | `/secrets` | `/serviceRegistry`, `/incidents` |

## 9. 장애 시나리오 실습

### 9.1 Command 응답 유실

1. `/incidents`에서 `UNKNOWN_RESULT`를 검색한다.
2. Operation ID와 Target ID를 복사한다.
3. `/transactionGroups`에서 Dispatch 시점을 확인한다.
4. Owner 화면 또는 Target 조회에서 결과를 찾는다.
5. 성공이면 Reconciliation Decision으로 `SUCCEEDED`를 확정한다.
6. 실패가 확정되면 새 Idempotency Key의 후속 Operation을 만든다.

### 9.2 Config 부분 적용

1. `/runtimeControl`에서 Target별 ACK·NACK를 확인한다.
2. 성공 Target의 Version을 기록한다.
3. NACK Target의 Error·Health·Config Source를 확인한다.
4. 정책에 따라 실패 Target만 재적용하거나 전체를 LKG로 Rollback한다.
5. Drift가 0인지 확인한다.

### 9.3 Batch Worker loss

1. `/batch-executions`에서 실행과 마지막 Step Count를 확인한다.
2. `/batch-runtime`과 `/workers`에서 Process·Heartbeat를 확인한다.
3. `/batch-leases`에서 Holder·Token·만료를 확인한다.
4. 이전 Writer가 차단된 뒤 더 큰 Token으로 Reclaim한다.
5. Metadata와 업무 Row를 대사한 후 Restart한다.

## 10. 운영자 자체 검수

1. 화면 기준시각과 환경을 확인했는가?
2. 목록 Row를 상세에서 재조회했는가?
3. Menu·Button·API·Data Scope 권한을 구분했는가?
4. 상태 변경 전에 Reason·Version·Approval을 확인했는가?
5. Timeout 뒤 Operation을 먼저 조회했는가?
6. `UNKNOWN_RESULT`를 실패로 덮지 않았는가?
7. `PARTIAL`의 성공 Target을 보존했는가?
8. Owner 원장과 Audit가 일치하는가?
9. 교대 인계에 다음 행동과 금지 조치를 남겼는가?
10. Rollback 뒤 Drift와 업무 합계를 확인했는가?
