# CPF ADM 운영자 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 문서 콘텐츠 기준 Commit: `64049044956924032360fa80be83b5e37c64f828` (`08_03`)
> 기준일: `2026-08-07 Asia/Seoul`
> 기준 Commit: **현재 `master` 구현 기준 문서**. Product Surface·Starter·Tool·EDU 식별자는 아래 기준 Commit의 Source 정본과 대조한다.

| 항목 | 내용 |
|---|---|
| 주 독자 | 조회자·운영자·승인자·보안담당자·운영관리자 |
| 이 문서로 완료할 일 | ADM 실제 Route에서 상태를 읽고 권한에 맞게 조치하며 Conflict·UNKNOWN_RESULT·PARTIAL·NACK·DRIFT를 대사·복구·감사한다. |
| 완료 판정 | 독자가 다른 문서나 Source 역분석 없이 정상 흐름, 오류, 부분 실패, 복구, 감사, 운영 인계를 끝낼 수 있어야 한다. |
| 상태 표현 | 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요만 사용한다. |
| 정합성 원칙 | 실제 Class·API·SQL·Config·화면·Permission·Script·Test를 우선하고 문서와 양방향으로 추적한다. |

## 1. 운영 원칙

1. 화면 Toast는 업무 상태 정본이 아니다.
2. 목록 Row는 상세 재조회 전까지 오래된 Snapshot일 수 있다.
3. 조회·변경·원문·Export 권한은 분리한다.
4. 위험 조치는 Reason·Expected Version·Idempotency·Approval을 확인한다.
5. Timeout 뒤 같은 Button을 다시 누르기 전에 Operation을 조회한다.
6. `PARTIAL`은 성공 Target을 보존한다.
7. `UNKNOWN_RESULT`는 Target·Owner 원장을 대사한다.
8. Download/Unmask/Secret/Break-glass는 행위 자체를 감사한다.

## 2. 역할

| 역할 | 허용 | 금지 |
|---|---|---|
| 조회자 | 검색·상세·Masked Field | 상태 변경·원문 Export |
| 운영자 | 허용 Command·복구 | 자기 승인·Owner DB 직접 수정 |
| 승인자 | Snapshot·Reason·Policy 검토 | 요청 Payload 수정 |
| 보안담당자 | Permission·MFA·Secret·Break-glass | Secret 평문 상시 조회 |
| 운영관리자 | Incident·SLA·교대·정책 | 미대사 Unknown 강제 종결 |

## 3. 공통 화면 읽는 순서

1. 환경·기준시각·Feature Flag·Version을 확인한다.
2. 검색 기간·조직·서비스를 필요한 최소 범위로 제한한다.
3. 목록에서 상태·Version·Owner·Updated At를 읽는다.
4. 상세에서 Transaction·Operation·Attempt·Target·Error·Audit를 연결한다.
5. 변경 전 Snapshot·Permission·Reason·Approval을 확인한다.
6. 실행 후 같은 조건으로 재조회한다.
7. Owner 원장과 Audit가 일치할 때 종결한다.

## 4. 상태별 행동

| 상태 | 의미 | 운영 행동 |
|---|---|---|
| REQUESTED | 접수됨 | 중복 요청 금지 |
| RUNNING | 진행 Evidence 존재 | Heartbeat/Deadline 확인 |
| SUCCEEDED | 결과 확정 | 동일 의도 재실행 금지 |
| FAILED | 결정적 실패 | 원인 제거 후 새 Operation |
| UNKNOWN_RESULT | 결과 Evidence 부족 | Target/Owner 대사 |
| PARTIAL | Target 결과 혼합 | 성공 Target 보존 |
| NACK | Target 적용 거부 | Error/환경 보정 |
| DRIFT | Desired/Observed 불일치 | Reconcile/LKG Rollback |

## 5. 화면 공통 필드 계약

모든 화면은 아래 15개 항목을 `cpf-admin/frontend/src/app/routes.ts`의 Route 계약과 실제 Vue Component의 표시·입력·조치 계약에 연결한다. Route Registry의 `expectedOperationIds`는 **해당 화면이 소비해야 하는 API Operation 범위**이며, Operation ID가 존재한다는 이유만으로 화면 Button이 있다고 간주하지 않는다. 실제 Button/Field는 Component에 존재하는 것만 기술한다.

1. Menu/Route/Feature Flag.
2. Menu·Button·API Permission.
3. 검색 Field와 기본값.
4. Paging/Sort.
5. Table Column.
6. 상세 Field.
7. Masking/Unmask.
8. Button/활성 조건.
9. 입력값/Reason/Approval/Expected Version.
10. 실행 상태/Progress.
11. Timeout/응답 유실.
12. Partial Apply/Retry/Reprocess/Reconcile/Rollback.
13. Audit/Trace.
14. 정상 판정.
15. 교대 인계.

### 5.1 Route 권한·Feature Flag의 실제 판정

현재 ADM Router는 `cpf-admin/frontend/src/app/router.ts`에서 다음 순서로 판정한다.

1. `findCapabilityByRouteName()`으로 `admCapabilityRegistry`의 Route를 찾는다. 없으면 임의 Dashboard로 보내지 않고 `not-found`로 처리한다.
2. Query의 transaction/trace 같은 causal identifier는 탐색 문맥일 뿐 Permission 근거로 신뢰하지 않는다.
3. Server Session Projection이 로드되면 `canAccessRoute(routeId, menuId, path)`로 Menu/Route 접근권한을 다시 계산한다. 실패하면 403으로 이동한다.
4. `isFeatureEnabled(routeId, menuId, path)`로 해당 `featureFlag`를 판정한다. 비활성 기능은 별도 `feature-disabled` 상태로 이동한다.
5. 화면 Lazy Load 실패는 503 성격의 `lazy-load-failure`로 분리하고 재시도를 허용한다.
6. **Action/API Permission은 Route 접근권한과 별도다.** 변경 Button은 Component가 Server Session의 Button/Action grant, 현재 상태, Version, Reason/Approval 조건을 확인한 경우에만 노출·활성화한다.

`cpf-admin/frontend/src/app/routes.ts`는 63개 Route 각각에 `routeId`, `path`, `menuId`, `label`, `group`, `component`, `riskLevel`, `featureFlag`, `expectedOperationIds`를 한 정본으로 둔다. 04의 Route 지도와 63개 운영 카드는 이 Registry의 경로·메뉴·위험도 기준을 사용한다.

## 6. Route 전체 지도

| No | Route | Menu | 화면 | Group | Risk |
|---:|---|---|---|---|---|
| 1 | `/` | `DASHBOARD` | 통합 운영 Dashboard | home | MEDIUM |
| 2 | `/topology` | `TOPOLOGY` | 서비스 토폴로지 | home | MEDIUM |
| 3 | `/capacity` | `CAPACITY` | Online Runtime Diagnostics | home | MEDIUM |
| 4 | `/logs` | `LOG_LIST` | 거래 로그 | monitoring | MEDIUM |
| 5 | `/transactionGroups` | `LOG_LIST` | Online·Batch 통합 Trace | online | MEDIUM |
| 6 | `/transactions` | `TRANSACTION_META` | 온라인 거래 정의 | online | HIGH |
| 7 | `/remoteLogs` | `REMOTE_LOG` | 원격 로그 | monitoring | MEDIUM |
| 8 | `/auditLogs` | `AUDIT_LOG` | 감사 로그 | monitoring | MEDIUM |
| 9 | `/logLevel` | `DYNAMIC_LOG` | 동적 로그 | monitoring | HIGH |
| 10 | `/logPolicies` | `LOG_POLICY` | 로그 정책 | monitoring | MEDIUM |
| 11 | `/standardExecutions` | `STANDARD_EXECUTION` | 표준 실행 | online | MEDIUM |
| 12 | `/channelPolicy` | `CHANNEL_POLICY` | 채널 정책 | online | HIGH |
| 13 | `/serviceRegistry` | `SERVICE_REGISTRY` | 서비스 레지스트리 | online | MEDIUM |
| 14 | `/runtimeControl` | `RUNTIME_CONTROL` | Deployment·Promotion·Rollback | online | HIGH |
| 15 | `/maintenance` | `MAINTENANCE` | 점검·Drain | framework | HIGH |
| 16 | `/cache` | `CACHE` | 캐시 | framework | HIGH |
| 17 | `/configs` | `CONFIG` | 설정 | framework | HIGH |
| 18 | `/responseCodes` | `RESPONSE_CODE` | 응답코드 | framework | MEDIUM |
| 19 | `/businessCalendar` | `BUSINESS_CALENDAR` | 영업일·휴일 | framework | MEDIUM |
| 20 | `/recoveryCenter` | `RECOVERY_CENTER` | 복구 센터 | monitoring | MEDIUM |
| 21 | `/incidents` | `INCIDENT` | Error·Unknown Result | monitoring | HIGH |
| 22 | `/reliability` | `RELIABILITY` | Analysis Center | monitoring | MEDIUM |
| 23 | `/notifications` | `NOTIFICATION` | 알림 | integration | MEDIUM |
| 24 | `/batch` | `BATCH` | Batch / Center-Cut | batch | MEDIUM |
| 25 | `/batch-overview` | `BATCH_OVERVIEW` | Batch Overview | batch | MEDIUM |
| 26 | `/batch-runtime` | `BATCH_RUNTIME` | Runtime Topology | batch | HIGH |
| 27 | `/batch-instances` | `BATCH_INSTANCES` | Runtime Instances | batch | MEDIUM |
| 28 | `/batch-scheduler` | `BATCH_SCHEDULER` | Scheduler HA | batch | MEDIUM |
| 29 | `/batch-worker-pools` | `BATCH_WORKER_POOLS` | Worker Pools | batch | MEDIUM |
| 30 | `/batch-center-cut` | `BATCH_CENTER_CUT` | Center-Cut | batch | MEDIUM |
| 31 | `/batch-agents` | `BATCH_AGENTS` | Host Agents | batch | MEDIUM |
| 32 | `/batch-job-packs` | `BATCH_JOB_PACKS` | Job Packs | batch | MEDIUM |
| 33 | `/batch-executions` | `BATCH_EXECUTIONS` | Executions | batch | MEDIUM |
| 34 | `/batch-deployment` | `BATCH_DEPLOYMENT` | Deployment / Rollback | batch | HIGH |
| 35 | `/batch-recovery` | `BATCH_RECOVERY` | Recovery / Unknown | monitoring | MEDIUM |
| 36 | `/batch-leases` | `BATCH_LEASES` | Lease / Fencing | monitoring | MEDIUM |
| 37 | `/batch-alerts` | `BATCH_ALERTS` | Batch Alerts | monitoring | MEDIUM |
| 38 | `/batch-audit` | `BATCH_AUDIT` | Audit / Evidence | monitoring | MEDIUM |
| 39 | `/workers` | `WORKER` | Agent / Worker | batch | MEDIUM |
| 40 | `/downloads` | `DOWNLOAD` | 다운로드 | integration | MEDIUM |
| 41 | `/file-jobs` | `FILE_JOB` | 대량파일 Job | batch | MEDIUM |
| 42 | `/messages` | `MESSAGE` | 전문·Protocol Message | integration | MEDIUM |
| 43 | `/codes` | `CODE` | 코드 | framework | MEDIUM |
| 44 | `/gateway-dashboard` | `GATEWAY_DASHBOARD` | Gateway 대시보드 | online | MEDIUM |
| 45 | `/gateway-servers` | `GATEWAY_SERVERS` | Gateway 연동 서버 | online | MEDIUM |
| 46 | `/gateway-groups` | `GATEWAY_GROUPS` | Gateway 서버 그룹 | online | MEDIUM |
| 47 | `/gateway-routes` | `GATEWAY_ROUTES` | Gateway 경로·라우팅 | online | MEDIUM |
| 48 | `/gateway-security` | `GATEWAY_SECURITY` | Gateway 보안·제한 | online | HIGH |
| 49 | `/gateway-health` | `GATEWAY_HEALTH` | Gateway Health·연결시험 | online | MEDIUM |
| 50 | `/gateway-transactions` | `GATEWAY_TRANSACTIONS` | Gateway 거래 조회 | online | MEDIUM |
| 51 | `/gateway-log-policies` | `GATEWAY_LOG_POLICY` | Gateway 로그 정책 | online | MEDIUM |
| 52 | `/gateway-apply-status` | `GATEWAY_APPLY_STATUS` | Gateway 적용 상태·이력 | online | MEDIUM |
| 53 | `/permissions` | `PERMISSION` | 권한 | framework | MEDIUM |
| 54 | `/password` | `PASSWORD` | 비밀번호 | framework | HIGH |
| 55 | `/security` | `SECURITY` | 보안 | framework | HIGH |
| 56 | `/operators` | `OPERATOR` | 운영자 | framework | HIGH |
| 57 | `/secrets` | `SECRET` | Secret / Key | framework | HIGH |
| 58 | `/approvals` | `APPROVAL` | 위험조치 승인 | framework | HIGH |
| 59 | `/breakGlass` | `BREAK_GLASS` | Break-glass | framework | HIGH |
| 60 | `/featureFlags` | `FEATURE_FLAG` | Feature Flag | framework | CRITICAL |
| 61 | `/integrationClosure` | `INTEGRATION_CLOSURE` | 통합 운영 정정 승인 | integration | CRITICAL |
| 62 | `/openApiOperations` | `OPENAPI_OPERATIONS` | OpenAPI 운영 | framework | HIGH |
| 63 | `/resiliencePolicies` | `RESILIENCE_POLICY` | Resilience 정책 | framework | CRITICAL |

## 7. 화면별 운영 카드

### 7.1. 통합 운영 Dashboard — `/`

| 항목 | 내용 |
|---|---|
| Menu | `DASHBOARD` |
| Group / Risk | `home` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/UnifiedOperationsDashboardPage.vue` / `adm.route.dashboard.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 통합 운영 Dashboard의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 검색 입력 없음. 새로고침으로 6개 Owner 조회를 동시에 수행한다. |
| 검색 기본값 | 최초 진입 시 자동 조회. Unknown/DLQ/Outbox/Batch 조회 한도 100, 우선순위 표 최대 20건. |
| 주요 표시값 | Runtime Control, Unknown Result, Broker DLQ, Batch Execution, Outbox, Causal Trace 카드; 우선순위/영역/상태/식별자/Transaction 표. |
| 주요 조치 | 새로고침; Runtime Control·Incident·Batch Execution·Reliability·Causal Trace로 Deep Link. |
| Button 활성 조건 | 변경 Button 없음. Route 접근권한과 Feature Flag만 적용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 6개 Owner 중 일부 실패 시 성공 응답은 보존하고 `일부 Owner 조회 실패 n/6`를 표시한다. Freshness 시각을 기준으로 원인 화면에서 재조회한다. |
| Partial/NACK/Drift | 6개 Owner 중 일부 실패 시 성공 응답은 보존하고 `일부 Owner 조회 실패 n/6`를 표시한다. Freshness 시각을 기준으로 원인 화면에서 재조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/UnifiedOperationsDashboardPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 검색 입력 없음. 새로고침으로 6개 Owner 조회를 동시에 수행한다. / 최초 진입 시 자동 조회. Unknown/DLQ/Outbox/Batch 조회 한도 100, 우선순위 표 최대 20건.
- **실제 표시·컬럼**: Runtime Control, Unknown Result, Broker DLQ, Batch Execution, Outbox, Causal Trace 카드; 우선순위/영역/상태/식별자/Transaction 표.
- **실제 조치**: 새로고침; Runtime Control·Incident·Batch Execution·Reliability·Causal Trace로 Deep Link.
- **권한·활성 조건**: 변경 Button 없음. Route 접근권한과 Feature Flag만 적용.
- **Version·Approval·Idempotency**: 조회 화면. Version/Approval/Idempotency 입력 없음.
- **응답 유실·부분 실패·복구**: 6개 Owner 중 일부 실패 시 성공 응답은 보존하고 `일부 Owner 조회 실패 n/6`를 표시한다. Freshness 시각을 기준으로 원인 화면에서 재조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/` 진입 시 Menu `DASHBOARD`의 Server Session 권한과 Feature Flag `adm.route.dashboard.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 검색 입력 없음. 새로고침으로 6개 Owner 조회를 동시에 수행한다. / 최초 진입 시 자동 조회. Unknown/DLQ/Outbox/Batch 조회 한도 100, 우선순위 표 최대 20건.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Runtime Control, Unknown Result, Broker DLQ, Batch Execution, Outbox, Causal Trace 카드; 우선순위/영역/상태/식별자/Transaction 표.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침; Runtime Control·Incident·Batch Execution·Reliability·Causal Trace로 Deep Link.
5. 실행 전 활성 조건을 다시 검사한다: 변경 Button 없음. Route 접근권한과 Feature Flag만 적용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회 화면. Version/Approval/Idempotency 입력 없음.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 6개 Owner 중 일부 실패 시 성공 응답은 보존하고 `일부 Owner 조회 실패 n/6`를 표시한다. Freshness 시각을 기준으로 원인 화면에서 재조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.2. 서비스 토폴로지 — `/topology`

| 항목 | 내용 |
|---|---|
| Menu | `TOPOLOGY` |
| Group / Risk | `home` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/topology/TopologyPage.vue` / `adm.route.topology.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 서비스 토폴로지의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 검색 입력 없음. |
| 검색 기본값 | Service Registry 현재 Snapshot을 조회한다. |
| 주요 표시값 | Service별 Node, serviceName/description, Instance name/id, endpointCode, weight, instanceStatus. |
| 주요 조치 | 새로고침. 변경 조치 없음. |
| Button 활성 조건 | 읽기 전용. Route/Feature Flag 접근 조건만 적용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Service Registry 데이터가 없으면 빈 상태를 표시한다. Instance 상태는 UP/ACTIVE/READY와 그 외를 구분한다. |
| Partial/NACK/Drift | Service Registry 데이터가 없으면 빈 상태를 표시한다. Instance 상태는 UP/ACTIVE/READY와 그 외를 구분한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/topology/TopologyPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 검색 입력 없음. / Service Registry 현재 Snapshot을 조회한다.
- **실제 표시·컬럼**: Service별 Node, serviceName/description, Instance name/id, endpointCode, weight, instanceStatus.
- **실제 조치**: 새로고침. 변경 조치 없음.
- **권한·활성 조건**: 읽기 전용. Route/Feature Flag 접근 조건만 적용.
- **Version·Approval·Idempotency**: 조회 화면.
- **응답 유실·부분 실패·복구**: Service Registry 데이터가 없으면 빈 상태를 표시한다. Instance 상태는 UP/ACTIVE/READY와 그 외를 구분한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/topology` 진입 시 Menu `TOPOLOGY`의 Server Session 권한과 Feature Flag `adm.route.topology.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 검색 입력 없음. / Service Registry 현재 Snapshot을 조회한다.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Service별 Node, serviceName/description, Instance name/id, endpointCode, weight, instanceStatus.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침. 변경 조치 없음.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 전용. Route/Feature Flag 접근 조건만 적용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회 화면.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Service Registry 데이터가 없으면 빈 상태를 표시한다. Instance 상태는 UP/ACTIVE/READY와 그 외를 구분한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.3. Online Runtime Diagnostics — `/capacity`

| 항목 | 내용 |
|---|---|
| Menu | `CAPACITY` |
| Group / Risk | `home` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/OnlineDiagnosticsPage.vue` / `adm.route.capacity.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Online Runtime Diagnostics의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Environment, Service ID, Transaction ID. |
| 검색 기본값 | 모두 공백=전체 범위. Outbox/Inbox/Idempotency/File I/O 조회 한도 100. 기본 Tab runtime. |
| 주요 표시값 | Control Health, Runtime State, Outbox, Inbox, Idempotency, File I/O 카드; ID/Key, Status, Transaction, Service/Topic, Updated 표. |
| 주요 조치 | 진단/새로고침, runtime/outbox/inbox/idempotency/files Tab. |
| Button 활성 조건 | 조회 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 6개 소스 PromiseAllSettled. 일부 실패는 `부분 실패 n/6`로 표시하고 성공 데이터는 보존한다. |
| Partial/NACK/Drift | 6개 소스 PromiseAllSettled. 일부 실패는 `부분 실패 n/6`로 표시하고 성공 데이터는 보존한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/OnlineDiagnosticsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Environment, Service ID, Transaction ID. / 모두 공백=전체 범위. Outbox/Inbox/Idempotency/File I/O 조회 한도 100. 기본 Tab runtime.
- **실제 표시·컬럼**: Control Health, Runtime State, Outbox, Inbox, Idempotency, File I/O 카드; ID/Key, Status, Transaction, Service/Topic, Updated 표.
- **실제 조치**: 진단/새로고침, runtime/outbox/inbox/idempotency/files Tab.
- **권한·활성 조건**: 조회 전용.
- **Version·Approval·Idempotency**: 조회 화면.
- **응답 유실·부분 실패·복구**: 6개 소스 PromiseAllSettled. 일부 실패는 `부분 실패 n/6`로 표시하고 성공 데이터는 보존한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/capacity` 진입 시 Menu `CAPACITY`의 Server Session 권한과 Feature Flag `adm.route.capacity.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Environment, Service ID, Transaction ID. / 모두 공백=전체 범위. Outbox/Inbox/Idempotency/File I/O 조회 한도 100. 기본 Tab runtime.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Control Health, Runtime State, Outbox, Inbox, Idempotency, File I/O 카드; ID/Key, Status, Transaction, Service/Topic, Updated 표.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 진단/새로고침, runtime/outbox/inbox/idempotency/files Tab.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회 화면.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 6개 소스 PromiseAllSettled. 일부 실패는 `부분 실패 n/6`로 표시하고 성공 데이터는 보존한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.4. 거래 로그 — `/logs`

| 항목 | 내용 |
|---|---|
| Menu | `LOG_LIST` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/logs/LogsPage.vue` / `adm.route.logs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 거래 로그의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | transactionId, traceId, 업무 거래 ID, URI, 응답 코드, HTTP 상태, 회원번호 보호검색, 고객번호 보호검색, 채널, 로그 유형; Export ID. |
| 검색 기본값 | 쪽 크기 10/20/50 선택. 상세 미선택 시 복사/저장 비활성. |
| 주요 표시값 | IDX, transactionId, 거래명/URI, Module/WAS, Instance/Host, 채널, HTTP/응답, 시작, 소요(ms); IN/GATEWAY/OUT/RESULT Timeline; Retry/Failover Attempt 8열; 10개 구조화 상세 Tab. |
| 주요 조치 | 조회, 마스킹 상세 복사, 감사 상세 저장, Export Artifact 다운로드. |
| Button 활성 조건 | 상세 복사/저장은 `logDetail.item` 존재 시. Artifact는 Export ID 사용. 화면은 서버 마스킹 결과만 표시. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Timeline 실패 단계와 Attempt를 먼저 확인한다. Export/상세 응답 유실은 동일 로그 식별자로 재조회하며 원문 민감정보를 우회 조회하지 않는다. |
| Partial/NACK/Drift | Timeline 실패 단계와 Attempt를 먼저 확인한다. Export/상세 응답 유실은 동일 로그 식별자로 재조회하며 원문 민감정보를 우회 조회하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/logs/LogsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: transactionId, traceId, 업무 거래 ID, URI, 응답 코드, HTTP 상태, 회원번호 보호검색, 고객번호 보호검색, 채널, 로그 유형; Export ID. / 쪽 크기 10/20/50 선택. 상세 미선택 시 복사/저장 비활성.
- **실제 표시·컬럼**: IDX, transactionId, 거래명/URI, Module/WAS, Instance/Host, 채널, HTTP/응답, 시작, 소요(ms); IN/GATEWAY/OUT/RESULT Timeline; Retry/Failover Attempt 8열; 10개 구조화 상세 Tab.
- **실제 조치**: 조회, 마스킹 상세 복사, 감사 상세 저장, Export Artifact 다운로드.
- **권한·활성 조건**: 상세 복사/저장은 `logDetail.item` 존재 시. Artifact는 Export ID 사용. 화면은 서버 마스킹 결과만 표시.
- **Version·Approval·Idempotency**: 조회/Artifact 화면. 변경 Version 없음.
- **응답 유실·부분 실패·복구**: Timeline 실패 단계와 Attempt를 먼저 확인한다. Export/상세 응답 유실은 동일 로그 식별자로 재조회하며 원문 민감정보를 우회 조회하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/logs` 진입 시 Menu `LOG_LIST`의 Server Session 권한과 Feature Flag `adm.route.logs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: transactionId, traceId, 업무 거래 ID, URI, 응답 코드, HTTP 상태, 회원번호 보호검색, 고객번호 보호검색, 채널, 로그 유형; Export ID. / 쪽 크기 10/20/50 선택. 상세 미선택 시 복사/저장 비활성.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: IDX, transactionId, 거래명/URI, Module/WAS, Instance/Host, 채널, HTTP/응답, 시작, 소요(ms); IN/GATEWAY/OUT/RESULT Timeline; Retry/Failover Attempt 8열; 10개 구조화 상세 Tab.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 마스킹 상세 복사, 감사 상세 저장, Export Artifact 다운로드.
5. 실행 전 활성 조건을 다시 검사한다: 상세 복사/저장은 `logDetail.item` 존재 시. Artifact는 Export ID 사용. 화면은 서버 마스킹 결과만 표시.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회/Artifact 화면. 변경 Version 없음.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Timeline 실패 단계와 Attempt를 먼저 확인한다. Export/상세 응답 유실은 동일 로그 식별자로 재조회하며 원문 민감정보를 우회 조회하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.5. Online·Batch 통합 Trace — `/transactionGroups`

| 항목 | 내용 |
|---|---|
| Menu | `LOG_LIST` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/IntegratedTracePage.vue` / `adm.route.transactionGroups.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Online·Batch 통합 Trace의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 식별자 유형(Transaction/Trace/Business Transaction), 식별자, 최대 건수. |
| 검색 기본값 | 유형 transaction, limit 100; 선택 20/50/100/200. Route query의 transactionId/traceId/businessTransactionId를 초기값으로 사용. |
| 주요 표시값 | Transaction/Timeline Event/Failure/Batch 카드; 시각, 유형, 상태, Module/Service, Execution/Segment, 요약 표; Event 구조화 상세. |
| 주요 조치 | 통합 조회; Batch Execution/Log/Audit/Incident Deep Link. |
| Button 활성 조건 | 식별자 없거나 loading이면 조회 비활성. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | transaction 유형은 Observability와 Group Timeline을 함께 조회한다. 오류 시 동일 식별자로 재조회한다. |
| Partial/NACK/Drift | transaction 유형은 Observability와 Group Timeline을 함께 조회한다. 오류 시 동일 식별자로 재조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/IntegratedTracePage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 식별자 유형(Transaction/Trace/Business Transaction), 식별자, 최대 건수. / 유형 transaction, limit 100; 선택 20/50/100/200. Route query의 transactionId/traceId/businessTransactionId를 초기값으로 사용.
- **실제 표시·컬럼**: Transaction/Timeline Event/Failure/Batch 카드; 시각, 유형, 상태, Module/Service, Execution/Segment, 요약 표; Event 구조화 상세.
- **실제 조치**: 통합 조회; Batch Execution/Log/Audit/Incident Deep Link.
- **권한·활성 조건**: 식별자 없거나 loading이면 조회 비활성.
- **Version·Approval·Idempotency**: 조회 화면.
- **응답 유실·부분 실패·복구**: transaction 유형은 Observability와 Group Timeline을 함께 조회한다. 오류 시 동일 식별자로 재조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/transactionGroups` 진입 시 Menu `LOG_LIST`의 Server Session 권한과 Feature Flag `adm.route.transactionGroups.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 식별자 유형(Transaction/Trace/Business Transaction), 식별자, 최대 건수. / 유형 transaction, limit 100; 선택 20/50/100/200. Route query의 transactionId/traceId/businessTransactionId를 초기값으로 사용.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Transaction/Timeline Event/Failure/Batch 카드; 시각, 유형, 상태, Module/Service, Execution/Segment, 요약 표; Event 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 통합 조회; Batch Execution/Log/Audit/Incident Deep Link.
5. 실행 전 활성 조건을 다시 검사한다: 식별자 없거나 loading이면 조회 비활성.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회 화면.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: transaction 유형은 Observability와 Group Timeline을 함께 조회한다. 오류 시 동일 식별자로 재조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.6. 온라인 거래 정의 — `/transactions`

| 항목 | 내용 |
|---|---|
| Menu | `TRANSACTION_META` |
| Group / Risk | `online` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/transactions/TransactionsPage.vue` / `adm.route.transactions.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 온라인 거래 정의의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 모듈, 활성 상태, 거래 ID, 쪽 크기; Runtime 재스캔/비활성화 사유. |
| 검색 기본값 | activeYn=Y, pageSize=20(10/20/50/100), page=0. 사유 10~500자. |
| 주요 표시값 | 거래 ID, 거래명, Module/Domain, HTTP Mapping, Operation ID, Log·Masking, 상태, 최종 Scan; Controller#Method, Mapping, OpenAPI, Log 정책, Masking, Updated by/at 상세. |
| 주요 조치 | 조회/새로고침, Runtime 재스캔, 상세, 활성 거래 비활성화, Timeline/Audit Deep Link. |
| Button 활성 조건 | 재스캔/비활성화는 `session.canWrite(...)`; 비활성화는 현재 active=Y. 위험조치는 사유>=10 + 영향/Audit 확인 체크. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 실행 후 목록/상세 재조회. Metadata 테이블 unavailable이면 Migration 상태를 먼저 복구한다. 동일 조치를 추정 재실행하지 않는다. |
| Partial/NACK/Drift | 실행 후 목록/상세 재조회. Metadata 테이블 unavailable이면 Migration 상태를 먼저 복구한다. 동일 조치를 추정 재실행하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/transactions/TransactionsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 모듈, 활성 상태, 거래 ID, 쪽 크기; Runtime 재스캔/비활성화 사유. / activeYn=Y, pageSize=20(10/20/50/100), page=0. 사유 10~500자.
- **실제 표시·컬럼**: 거래 ID, 거래명, Module/Domain, HTTP Mapping, Operation ID, Log·Masking, 상태, 최종 Scan; Controller#Method, Mapping, OpenAPI, Log 정책, Masking, Updated by/at 상세.
- **실제 조치**: 조회/새로고침, Runtime 재스캔, 상세, 활성 거래 비활성화, Timeline/Audit Deep Link.
- **권한·활성 조건**: 재스캔/비활성화는 `session.canWrite(...)`; 비활성화는 현재 active=Y. 위험조치는 사유>=10 + 영향/Audit 확인 체크.
- **Version·Approval·Idempotency**: 화면 입력 Version은 없고 Runtime meta Owner가 상태를 관리한다. DB meta 미사용 시 명시적 warning.
- **응답 유실·부분 실패·복구**: 실행 후 목록/상세 재조회. Metadata 테이블 unavailable이면 Migration 상태를 먼저 복구한다. 동일 조치를 추정 재실행하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/transactions` 진입 시 Menu `TRANSACTION_META`의 Server Session 권한과 Feature Flag `adm.route.transactions.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 모듈, 활성 상태, 거래 ID, 쪽 크기; Runtime 재스캔/비활성화 사유. / activeYn=Y, pageSize=20(10/20/50/100), page=0. 사유 10~500자.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 거래 ID, 거래명, Module/Domain, HTTP Mapping, Operation ID, Log·Masking, 상태, 최종 Scan; Controller#Method, Mapping, OpenAPI, Log 정책, Masking, Updated by/at 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/새로고침, Runtime 재스캔, 상세, 활성 거래 비활성화, Timeline/Audit Deep Link.
5. 실행 전 활성 조건을 다시 검사한다: 재스캔/비활성화는 `session.canWrite(...)`; 비활성화는 현재 active=Y. 위험조치는 사유>=10 + 영향/Audit 확인 체크.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 화면 입력 Version은 없고 Runtime meta Owner가 상태를 관리한다. DB meta 미사용 시 명시적 warning.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 실행 후 목록/상세 재조회. Metadata 테이블 unavailable이면 Migration 상태를 먼저 복구한다. 동일 조치를 추정 재실행하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.7. 원격 로그 — `/remoteLogs`

| 항목 | 내용 |
|---|---|
| Menu | `REMOTE_LOG` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/remote-logs/RemoteLogsPage.vue` / `adm.route.remoteLogs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 원격 로그의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 환경, 모듈, 서비스, 인스턴스, 로그유형, 파일명, 표준 온라인/배치 ID, 거래/구간 ID, Job Instance/Execution/Step Execution, Scheduler ID, 수정 시작/종료, 최소/최대 byte, 압축, 활성/보관, 마지막 행(1~1000), 본문 검색, 다운로드 사유. |
| 검색 기본값 | 환경 초기 예시값 `local`, 모듈/서비스 초기 예시값 `ADM`, 로그유형 초기 예시값 `transaction`. 압축/활성은 전체가 기본. |
| 주요 표시값 | 선택, 환경, 모듈, 서비스, 인스턴스, 유형, 파일명, 크기, 수정시각, 보존만료, 상태; Preview/Bundle Job/Diagnostics 상세. |
| 주요 조치 | 조회, 단일 다운로드, 선택 ZIP, 비동기 ZIP, 작업상태, 완료 ZIP 다운로드, 진단. |
| Button 활성 조건 | 다운로드/ZIP은 REMOTE_LOG write. 선택 ZIP/비동기 ZIP은 선택건>0, 작업상태는 jobId, 완료 ZIP은 status=COMPLETED. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 대용량은 비동기 ZIP Job으로 전환하고 Job 상태를 재조회한다. 완료 전 다운로드를 시도하지 않는다. |
| Partial/NACK/Drift | 대용량은 비동기 ZIP Job으로 전환하고 Job 상태를 재조회한다. 완료 전 다운로드를 시도하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/remote-logs/RemoteLogsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 환경, 모듈, 서비스, 인스턴스, 로그유형, 파일명, 표준 온라인/배치 ID, 거래/구간 ID, Job Instance/Execution/Step Execution, Scheduler ID, 수정 시작/종료, 최소/최대 byte, 압축, 활성/보관, 마지막 행(1~1000), 본문 검색, 다운로드 사유. / 환경 초기 예시값 `local`, 모듈/서비스 초기 예시값 `ADM`, 로그유형 초기 예시값 `transaction`. 압축/활성은 전체가 기본.
- **실제 표시·컬럼**: 선택, 환경, 모듈, 서비스, 인스턴스, 유형, 파일명, 크기, 수정시각, 보존만료, 상태; Preview/Bundle Job/Diagnostics 상세.
- **실제 조치**: 조회, 단일 다운로드, 선택 ZIP, 비동기 ZIP, 작업상태, 완료 ZIP 다운로드, 진단.
- **권한·활성 조건**: 다운로드/ZIP은 REMOTE_LOG write. 선택 ZIP/비동기 ZIP은 선택건>0, 작업상태는 jobId, 완료 ZIP은 status=COMPLETED.
- **Version·Approval·Idempotency**: Artifact 조회 작업. 비동기 Bundle은 Job 상태로 추적.
- **응답 유실·부분 실패·복구**: 대용량은 비동기 ZIP Job으로 전환하고 Job 상태를 재조회한다. 완료 전 다운로드를 시도하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/remoteLogs` 진입 시 Menu `REMOTE_LOG`의 Server Session 권한과 Feature Flag `adm.route.remoteLogs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 환경, 모듈, 서비스, 인스턴스, 로그유형, 파일명, 표준 온라인/배치 ID, 거래/구간 ID, Job Instance/Execution/Step Execution, Scheduler ID, 수정 시작/종료, 최소/최대 byte, 압축, 활성/보관, 마지막 행(1~1000), 본문 검색, 다운로드 사유. / 환경 초기 예시값 local, 모듈/서비스 ADM, 로그유형 transaction. 압축/활성은 전체가 기본.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 선택, 환경, 모듈, 서비스, 인스턴스, 유형, 파일명, 크기, 수정시각, 보존만료, 상태; Preview/Bundle Job/Diagnostics 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 단일 다운로드, 선택 ZIP, 비동기 ZIP, 작업상태, 완료 ZIP 다운로드, 진단.
5. 실행 전 활성 조건을 다시 검사한다: 다운로드/ZIP은 REMOTE_LOG write. 선택 ZIP/비동기 ZIP은 선택건>0, 작업상태는 jobId, 완료 ZIP은 status=COMPLETED.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Artifact 조회 작업. 비동기 Bundle은 Job 상태로 추적.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 대용량은 비동기 ZIP Job으로 전환하고 Job 상태를 재조회한다. 완료 전 다운로드를 시도하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.8. 감사 로그 — `/auditLogs`

| 항목 | 내용 |
|---|---|
| Menu | `AUDIT_LOG` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/audit-logs/AuditLogsPage.vue` / `adm.route.auditLogs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 감사 로그의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 운영자 ID, 행위, 대상 유형, 대상 ID; 전달 상태(전체/PENDING/RETRY/FAILED/DELIVERED), 재처리 사유. |
| 검색 기본값 | 전달 상태 전체. |
| 주요 표시값 | 감사 조회 결과; 전달 ID, 행위, 운영자, 작업, 전달, 시도, 오류, 조치. |
| 주요 조치 | 감사 조회, 전달 상태 조회, 감사 전달 재처리. |
| Button 활성 조건 | 재처리는 deliveryStatus가 PENDING/RETRY/FAILED이고 AUDIT_LOG writeAllowed일 때. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 이미 DELIVERED인 건은 재처리하지 않는다. 실패/재시도 상태에서 전달 원장과 attempt count를 확인 후 조치한다. |
| Partial/NACK/Drift | 이미 DELIVERED인 건은 재처리하지 않는다. 실패/재시도 상태에서 전달 원장과 attempt count를 확인 후 조치한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/audit-logs/AuditLogsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 운영자 ID, 행위, 대상 유형, 대상 ID; 전달 상태(전체/PENDING/RETRY/FAILED/DELIVERED), 재처리 사유. / 전달 상태 전체.
- **실제 표시·컬럼**: 감사 조회 결과; 전달 ID, 행위, 운영자, 작업, 전달, 시도, 오류, 조치.
- **실제 조치**: 감사 조회, 전달 상태 조회, 감사 전달 재처리.
- **권한·활성 조건**: 재처리는 deliveryStatus가 PENDING/RETRY/FAILED이고 AUDIT_LOG writeAllowed일 때.
- **Version·Approval·Idempotency**: 전달 원장의 현재 상태/attempt를 기준으로 재처리.
- **응답 유실·부분 실패·복구**: 이미 DELIVERED인 건은 재처리하지 않는다. 실패/재시도 상태에서 전달 원장과 attempt count를 확인 후 조치한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/auditLogs` 진입 시 Menu `AUDIT_LOG`의 Server Session 권한과 Feature Flag `adm.route.auditLogs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 운영자 ID, 행위, 대상 유형, 대상 ID; 전달 상태(전체/PENDING/RETRY/FAILED/DELIVERED), 재처리 사유. / 전달 상태 전체.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 감사 조회 결과; 전달 ID, 행위, 운영자, 작업, 전달, 시도, 오류, 조치.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 감사 조회, 전달 상태 조회, 감사 전달 재처리.
5. 실행 전 활성 조건을 다시 검사한다: 재처리는 deliveryStatus가 PENDING/RETRY/FAILED이고 AUDIT_LOG writeAllowed일 때.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 전달 원장의 현재 상태/attempt를 기준으로 재처리.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 이미 DELIVERED인 건은 재처리하지 않는다. 실패/재시도 상태에서 전달 원장과 attempt count를 확인 후 조치한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.9. 동적 로그 — `/logLevel`

| 항목 | 내용 |
|---|---|
| Menu | `DYNAMIC_LOG` |
| Group / Risk | `monitoring` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/log-level/LogLevelPage.vue` / `adm.route.logLevel.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 동적 로그의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 업무 거래 ID, 거래 ID, 레벨(DEBUG/INFO/TRACE), TTL 초, 사유; 제거용 Rule ID/사유. |
| 검색 기본값 | 업무 거래 ID 초기 예시값 `REF01EDU0001`. 레벨 선택은 Component 값 사용. |
| 주요 표시값 | 동적 로그 규칙/처리 결과 구조화 상세. |
| 주요 조치 | 조회, 규칙 등록, Rule ID 기준 규칙 제거. |
| Button 활성 조건 | 등록/제거는 DYNAMIC_LOG write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 등록 후 규칙 조회로 TTL·대상·레벨을 확인한다. 응답 유실 시 동일 Rule 존재 여부를 조회한다. |
| Partial/NACK/Drift | 등록 후 규칙 조회로 TTL·대상·레벨을 확인한다. 응답 유실 시 동일 Rule 존재 여부를 조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/log-level/LogLevelPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 업무 거래 ID, 거래 ID, 레벨(DEBUG/INFO/TRACE), TTL 초, 사유; 제거용 Rule ID/사유. / 업무 거래 ID 초기 예시값 `REF01EDU0001`. 레벨 선택은 Component 값 사용.
- **실제 표시·컬럼**: 동적 로그 규칙/처리 결과 구조화 상세.
- **실제 조치**: 조회, 규칙 등록, Rule ID 기준 규칙 제거.
- **권한·활성 조건**: 등록/제거는 DYNAMIC_LOG write.
- **Version·Approval·Idempotency**: TTL 기반 임시 규칙. Rule ID로 제거.
- **응답 유실·부분 실패·복구**: 등록 후 규칙 조회로 TTL·대상·레벨을 확인한다. 응답 유실 시 동일 Rule 존재 여부를 조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/logLevel` 진입 시 Menu `DYNAMIC_LOG`의 Server Session 권한과 Feature Flag `adm.route.logLevel.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 업무 거래 ID, 거래 ID, 레벨(DEBUG/INFO/TRACE), TTL 초, 사유; 제거용 Rule ID/사유. / 업무 거래 ID 초기 예시값 REF01EDU0001. 레벨 선택은 Component 값 사용.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 동적 로그 규칙/처리 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 규칙 등록, Rule ID 기준 규칙 제거.
5. 실행 전 활성 조건을 다시 검사한다: 등록/제거는 DYNAMIC_LOG write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: TTL 기반 임시 규칙. Rule ID로 제거.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 등록 후 규칙 조회로 TTL·대상·레벨을 확인한다. 응답 유실 시 동일 Rule 존재 여부를 조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.10. 로그 정책 — `/logPolicies`

| 항목 | 내용 |
|---|---|
| Menu | `LOG_POLICY` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/log-policies/LogPoliciesPage.vue` / `adm.route.logPolicies.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 로그 정책의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Policy ID/Key/Name, 대상유형·ID, Level, DB/File 저장, Error Stack Capture, 보존일(1~3650), Sampling(0~100), 우선순위; Query/Header/Body Capture Mode, 각 Allowlist, Masking Policy, byte 상한, 사유; Trace Boost 거래/업무거래/API/상태/실패코드/지연/TTL/기간. |
| 검색 기본값 | Body/Query는 안전 모드 중심. byte 상한 UI: query 65536, header 131072, request/response body 1048576, stack 262144. |
| 주요 표시값 | 정책 ID/정책/대상/레벨/DB·File/Body/Sampling/상태/갱신; Gateway 배포 Event/Gateway/Version/상태/시도/Fencing/오류/ACK. |
| 주요 조치 | 조회, 적용상태, 저장, Override 등록/중지, 정책중지, Trace Boost 등록/상태/이력, Cache refresh/clear, 신규/상세/수정. |
| Button 활성 조건 | Mutation은 LOG_POLICY write. Authorization/Cookie/Token 원문과 일반 운영 FULL_RAW_BODY는 저장 대상으로 허용하지 않음. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | FAILED/PENDING Gateway만 원인·ACK·fencing을 확인한다. 성공 Instance를 되돌리지 않고 실패 대상만 재대사한다. |
| Partial/NACK/Drift | FAILED/PENDING Gateway만 원인·ACK·fencing을 확인한다. 성공 Instance를 되돌리지 않고 실패 대상만 재대사한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/log-policies/LogPoliciesPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Policy ID/Key/Name, 대상유형·ID, Level, DB/File 저장, Error Stack Capture, 보존일(1~3650), Sampling(0~100), 우선순위; Query/Header/Body Capture Mode, 각 Allowlist, Masking Policy, byte 상한, 사유; Trace Boost 거래/업무거래/API/상태/실패코드/지연/TTL/기간. / Body/Query는 안전 모드 중심. byte 상한 UI: query 65536, header 131072, request/response body 1048576, stack 262144.
- **실제 표시·컬럼**: 정책 ID/정책/대상/레벨/DB·File/Body/Sampling/상태/갱신; Gateway 배포 Event/Gateway/Version/상태/시도/Fencing/오류/ACK.
- **실제 조치**: 조회, 적용상태, 저장, Override 등록/중지, 정책중지, Trace Boost 등록/상태/이력, Cache refresh/clear, 신규/상세/수정.
- **권한·활성 조건**: Mutation은 LOG_POLICY write. Authorization/Cookie/Token 원문과 일반 운영 FULL_RAW_BODY는 저장 대상으로 허용하지 않음.
- **Version·Approval·Idempotency**: Gateway 배포는 aggregate version/ACK/fencing을 표시. 정책 변경 후 적용상태로 배포 수렴 확인.
- **응답 유실·부분 실패·복구**: FAILED/PENDING Gateway만 원인·ACK·fencing을 확인한다. 성공 Instance를 되돌리지 않고 실패 대상만 재대사한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/logPolicies` 진입 시 Menu `LOG_POLICY`의 Server Session 권한과 Feature Flag `adm.route.logPolicies.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Policy ID/Key/Name, 대상유형·ID, Level, DB/File 저장, Error Stack Capture, 보존일(1~3650), Sampling(0~100), 우선순위; Query/Header/Body Capture Mode, 각 Allowlist, Masking Policy, byte 상한, 사유; Trace Boost 거래/업무거래/API/상태/실패코드/지연/TTL/기간. / Body/Query는 안전 모드 중심. byte 상한 UI: query 65536, header 131072, request/response body 1048576, stack 262144.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 정책 ID/정책/대상/레벨/DB·File/Body/Sampling/상태/갱신; Gateway 배포 Event/Gateway/Version/상태/시도/Fencing/오류/ACK.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 적용상태, 저장, Override 등록/중지, 정책중지, Trace Boost 등록/상태/이력, Cache refresh/clear, 신규/상세/수정.
5. 실행 전 활성 조건을 다시 검사한다: Mutation은 LOG_POLICY write. Authorization/Cookie/Token 원문과 일반 운영 FULL_RAW_BODY는 저장 대상으로 허용하지 않음.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Gateway 배포는 aggregate version/ACK/fencing을 표시. 정책 변경 후 적용상태로 배포 수렴 확인.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: FAILED/PENDING Gateway만 원인·ACK·fencing을 확인한다. 성공 Instance를 되돌리지 않고 실패 대상만 재대사한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.11. 표준 실행 — `/standardExecutions`

| 항목 | 내용 |
|---|---|
| Menu | `STANDARD_EXECUTION` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/standard-executions/StandardExecutionsPage.vue` / `adm.route.standardExecutions.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 표준 실행의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 유형(전체/ONLINE/BATCH), 소유 업무, 검색어(ID/실행명/source/endpoint). |
| 검색 기본값 | 유형 전체. |
| 주요 표시값 | 표준 실행 ID, 유형, 실행명, 소유 업무, Source 모듈, Endpoint; 선택 상세. |
| 주요 조치 | 조회, 행 선택 상세. |
| Button 활성 조건 | 읽기 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Source/Endpoint가 비어 있으면 상세에서 Owner 정의를 확인한다. |
| Partial/NACK/Drift | Source/Endpoint가 비어 있으면 상세에서 Owner 정의를 확인한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/standard-executions/StandardExecutionsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 유형(전체/ONLINE/BATCH), 소유 업무, 검색어(ID/실행명/source/endpoint). / 유형 전체.
- **실제 표시·컬럼**: 표준 실행 ID, 유형, 실행명, 소유 업무, Source 모듈, Endpoint; 선택 상세.
- **실제 조치**: 조회, 행 선택 상세.
- **권한·활성 조건**: 읽기 전용.
- **Version·Approval·Idempotency**: Catalog 조회 화면.
- **응답 유실·부분 실패·복구**: Source/Endpoint가 비어 있으면 상세에서 Owner 정의를 확인한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/standardExecutions` 진입 시 Menu `STANDARD_EXECUTION`의 Server Session 권한과 Feature Flag `adm.route.standardExecutions.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 유형(전체/ONLINE/BATCH), 소유 업무, 검색어(ID/실행명/source/endpoint). / 유형 전체.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 표준 실행 ID, 유형, 실행명, 소유 업무, Source 모듈, Endpoint; 선택 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 행 선택 상세.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Catalog 조회 화면.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Source/Endpoint가 비어 있으면 상세에서 Owner 정의를 확인한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.12. 채널 정책 — `/channelPolicy`

| 항목 | 내용 |
|---|---|
| Menu | `CHANNEL_POLICY` |
| Group / Risk | `online` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/channel-policy/ChannelPolicyPage.vue` / `adm.route.channelPolicy.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 채널 정책의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 채널 코드(<=30), 채널명(<=100), 유형 CLIENT/OPERATOR/SYSTEM, 신뢰 EXTERNAL/INTERNAL, 최초/내부/인증/서명/사용, 설명·감사사유(<=500); 정책키(<=100), 표준실행ID(<=10), 최초/호출채널(<=30), 요청유형(<=30), maxTPS>=0, 허용/인증/서명/사용, 사유; Package JSON, Dry-run. |
| 검색 기본값 | 현재 Snapshot version 표시. Package Import Dry-run 체크 가능. |
| 주요 표시값 | 채널/명칭/유형/신뢰/인증/서명/사용/버전; 정책키/표준실행/최초채널/호출채널/요청유형/허용/maxTPS/버전. |
| 주요 조치 | 조회, Snapshot 갱신, Package 반출/반입, 채널 저장, 거래정책 저장. |
| Button 활성 조건 | 갱신/반입/저장은 CHANNEL_POLICY write. 반출은 조회 가능. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 반입은 Dry-run으로 먼저 검증하고 Snapshot 갱신 후 Gateway/온라인 거래에서 적용 Version을 확인한다. |
| Partial/NACK/Drift | 반입은 Dry-run으로 먼저 검증하고 Snapshot 갱신 후 Gateway/온라인 거래에서 적용 Version을 확인한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/channel-policy/ChannelPolicyPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 채널 코드(<=30), 채널명(<=100), 유형 CLIENT/OPERATOR/SYSTEM, 신뢰 EXTERNAL/INTERNAL, 최초/내부/인증/서명/사용, 설명·감사사유(<=500); 정책키(<=100), 표준실행ID(<=10), 최초/호출채널(<=30), 요청유형(<=30), maxTPS>=0, 허용/인증/서명/사용, 사유; Package JSON, Dry-run. / 현재 Snapshot version 표시. Package Import Dry-run 체크 가능.
- **실제 표시·컬럼**: 채널/명칭/유형/신뢰/인증/서명/사용/버전; 정책키/표준실행/최초채널/호출채널/요청유형/허용/maxTPS/버전.
- **실제 조치**: 조회, Snapshot 갱신, Package 반출/반입, 채널 저장, 거래정책 저장.
- **권한·활성 조건**: 갱신/반입/저장은 CHANNEL_POLICY write. 반출은 조회 가능.
- **Version·Approval·Idempotency**: 불변 Snapshot version 및 각 Row version을 표시.
- **응답 유실·부분 실패·복구**: 반입은 Dry-run으로 먼저 검증하고 Snapshot 갱신 후 Gateway/온라인 거래에서 적용 Version을 확인한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/channelPolicy` 진입 시 Menu `CHANNEL_POLICY`의 Server Session 권한과 Feature Flag `adm.route.channelPolicy.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 채널 코드(<=30), 채널명(<=100), 유형 CLIENT/OPERATOR/SYSTEM, 신뢰 EXTERNAL/INTERNAL, 최초/내부/인증/서명/사용, 설명·감사사유(<=500); 정책키(<=100), 표준실행ID(<=10), 최초/호출채널(<=30), 요청유형(<=30), maxTPS>=0, 허용/인증/서명/사용, 사유; Package JSON, Dry-run. / 현재 Snapshot version 표시. Package Import Dry-run 체크 가능.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 채널/명칭/유형/신뢰/인증/서명/사용/버전; 정책키/표준실행/최초채널/호출채널/요청유형/허용/maxTPS/버전.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, Snapshot 갱신, Package 반출/반입, 채널 저장, 거래정책 저장.
5. 실행 전 활성 조건을 다시 검사한다: 갱신/반입/저장은 CHANNEL_POLICY write. 반출은 조회 가능.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 불변 Snapshot version 및 각 Row version을 표시.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 반입은 Dry-run으로 먼저 검증하고 Snapshot 갱신 후 Gateway/온라인 거래에서 적용 Version을 확인한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.13. 서비스 레지스트리 — `/serviceRegistry`

| 항목 | 내용 |
|---|---|
| Menu | `SERVICE_REGISTRY` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/service-registry/ServiceRegistryPage.vue` / `adm.route.serviceRegistry.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 서비스 레지스트리의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Toolbar Service ID/Endpoint/Instance 상태. Service: ID/명/유형/Owner/use/설명/reason>=5. Endpoint: code/service/name/type HTTP·HTTPS·GRPC·LOCAL·TCP·SFTP/baseURL/context/timeout>=1/retry>=0/use/reason>=5. Instance: ID/service/endpoint/name/baseURL/host/port1~65535/env/zone/cell/weight>=1/priority>=0/active/reason>=5. |
| 검색 기본값 | 기존 Service/Endpoint/Instance의 식별자는 version>0이면 잠금. |
| 주요 표시값 | Service 원장, Endpoint, Instance·Health, Health History, Routing·Circuit, 최근 Service Call(Transaction/Trace/Retry/Failure) Tab. |
| 주요 조치 | 조회, Service/Endpoint/Instance 등록·수정·삭제, Instance DRAIN/RESUME/DISABLE. |
| Button 활성 조건 | 쓰기/삭제 권한 분리. DRAIN은 미Drain, RESUME은 draining, DISABLE은 active인 Instance에서만 노출. 상태조치 reason>=5. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Health·Routing·Circuit·Call History를 교차 확인한다. 상태변경 응답 유실 시 Instance 현재 상태와 호출 이력부터 재조회한다. |
| Partial/NACK/Drift | Health·Routing·Circuit·Call History를 교차 확인한다. 상태변경 응답 유실 시 Instance 현재 상태와 호출 이력부터 재조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/service-registry/ServiceRegistryPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Toolbar Service ID/Endpoint/Instance 상태. Service: ID/명/유형/Owner/use/설명/reason>=5. Endpoint: code/service/name/type HTTP·HTTPS·GRPC·LOCAL·TCP·SFTP/baseURL/context/timeout>=1/retry>=0/use/reason>=5. Instance: ID/service/endpoint/name/baseURL/host/port1~65535/env/zone/cell/weight>=1/priority>=0/active/reason>=5. / 기존 Service/Endpoint/Instance의 식별자는 version>0이면 잠금.
- **실제 표시·컬럼**: Service 원장, Endpoint, Instance·Health, Health History, Routing·Circuit, 최근 Service Call(Transaction/Trace/Retry/Failure) Tab.
- **실제 조치**: 조회, Service/Endpoint/Instance 등록·수정·삭제, Instance DRAIN/RESUME/DISABLE.
- **권한·활성 조건**: 쓰기/삭제 권한 분리. DRAIN은 미Drain, RESUME은 draining, DISABLE은 active인 Instance에서만 노출. 상태조치 reason>=5.
- **Version·Approval·Idempotency**: 각 Service/Endpoint/Instance version 표시. 삭제 Dialog에도 version을 사용해 최신 대상을 확인.
- **응답 유실·부분 실패·복구**: Health·Routing·Circuit·Call History를 교차 확인한다. 상태변경 응답 유실 시 Instance 현재 상태와 호출 이력부터 재조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/serviceRegistry` 진입 시 Menu `SERVICE_REGISTRY`의 Server Session 권한과 Feature Flag `adm.route.serviceRegistry.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Toolbar Service ID/Endpoint/Instance 상태. Service: ID/명/유형/Owner/use/설명/reason>=5. Endpoint: code/service/name/type HTTP·HTTPS·GRPC·LOCAL·TCP·SFTP/baseURL/context/timeout>=1/retry>=0/use/reason>=5. Instance: ID/service/endpoint/name/baseURL/host/port1~65535/env/zone/cell/weight>=1/priority>=0/active/reason>=5. / 기존 Service/Endpoint/Instance의 식별자는 version>0이면 잠금.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Service 원장, Endpoint, Instance·Health, Health History, Routing·Circuit, 최근 Service Call(Transaction/Trace/Retry/Failure) Tab.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, Service/Endpoint/Instance 등록·수정·삭제, Instance DRAIN/RESUME/DISABLE.
5. 실행 전 활성 조건을 다시 검사한다: 쓰기/삭제 권한 분리. DRAIN은 미Drain, RESUME은 draining, DISABLE은 active인 Instance에서만 노출. 상태조치 reason>=5.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 각 Service/Endpoint/Instance version 표시. 삭제 Dialog에도 version을 사용해 최신 대상을 확인.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Health·Routing·Circuit·Call History를 교차 확인한다. 상태변경 응답 유실 시 Instance 현재 상태와 호출 이력부터 재조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.14. Deployment·Promotion·Rollback — `/runtimeControl`

| 항목 | 내용 |
|---|---|
| Menu | `RUNTIME_CONTROL` |
| Group / Risk | `online` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/OnlineDeploymentWorkbenchPage.vue` / `adm.route.runtimeControl.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Deployment·Promotion·Rollback의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Environment, Service ID, Change ID, Operation ID; Change Type, Expected Version>=0, Approval ID, Break-glass ID, Target JSON, Payload JSON, 사유>=10; Cancel/Rollback Operation ID·사유>=10. |
| 검색 기본값 | Expected Version=0, payload schema version=1, Target JSON 예시 DEV/serviceIds, Payload {}. Operation ID는 UUID 생성. |
| 주요 표시값 | Health, Capabilities, Runtime State 카드; Preview/Change/Operation 구조화 결과. |
| 주요 조치 | 상태 새로고침, 결과 조회, Preview, 변경 실행, Cancel, Rollback. |
| Button 활성 조건 | 변경 실행은 Preview 영향 확인 checkbox + reason>=10. Cancel/Rollback은 resultChangeId 필요하며 별도 확인 checkbox+reason>=10. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 결과 불명은 동일 Operation ID로 조회. Cancel/Rollback 응답 유실도 재실행하지 말고 Operation 상태를 확인한다. |
| Partial/NACK/Drift | 결과 불명은 동일 Operation ID로 조회. Cancel/Rollback 응답 유실도 재실행하지 말고 Operation 상태를 확인한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/OnlineDeploymentWorkbenchPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Environment, Service ID, Change ID, Operation ID; Change Type, Expected Version>=0, Approval ID, Break-glass ID, Target JSON, Payload JSON, 사유>=10; Cancel/Rollback Operation ID·사유>=10. / Expected Version=0, payload schema version=1, Target JSON 예시 DEV/serviceIds, Payload {}. Operation ID는 UUID 생성.
- **실제 표시·컬럼**: Health, Capabilities, Runtime State 카드; Preview/Change/Operation 구조화 결과.
- **실제 조치**: 상태 새로고침, 결과 조회, Preview, 변경 실행, Cancel, Rollback.
- **권한·활성 조건**: 변경 실행은 Preview 영향 확인 checkbox + reason>=10. Cancel/Rollback은 resultChangeId 필요하며 별도 확인 checkbox+reason>=10.
- **Version·Approval·Idempotency**: Expected Version CAS, Approval/Break-glass, Operation ID를 함께 사용.
- **응답 유실·부분 실패·복구**: 결과 불명은 동일 Operation ID로 조회. Cancel/Rollback 응답 유실도 재실행하지 말고 Operation 상태를 확인한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/runtimeControl` 진입 시 Menu `RUNTIME_CONTROL`의 Server Session 권한과 Feature Flag `adm.route.runtimeControl.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Environment, Service ID, Change ID, Operation ID; Change Type, Expected Version>=0, Approval ID, Break-glass ID, Target JSON, Payload JSON, 사유>=10; Cancel/Rollback Operation ID·사유>=10. / Expected Version=0, payload schema version=1, Target JSON 예시 DEV/serviceIds, Payload {}. Operation ID는 UUID 생성.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Health, Capabilities, Runtime State 카드; Preview/Change/Operation 구조화 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 상태 새로고침, 결과 조회, Preview, 변경 실행, Cancel, Rollback.
5. 실행 전 활성 조건을 다시 검사한다: 변경 실행은 Preview 영향 확인 checkbox + reason>=10. Cancel/Rollback은 resultChangeId 필요하며 별도 확인 checkbox+reason>=10.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Expected Version CAS, Approval/Break-glass, Operation ID를 함께 사용.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 결과 불명은 동일 Operation ID로 조회. Cancel/Rollback 응답 유실도 재실행하지 말고 Operation 상태를 확인한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.15. 점검·Drain — `/maintenance`

| 항목 | 내용 |
|---|---|
| Menu | `MAINTENANCE` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/maintenance/MaintenancePage.vue` / `adm.route.maintenance.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 점검·Drain의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Service, Endpoint, Instance, Action(DRAIN/DISABLE/RESUME), 감사 사유>=5. |
| 검색 기본값 | Action DRAIN. 조회 limit 100. |
| 주요 표시값 | 시간, Service, Endpoint, Instance, Action, 결과, 사유, Operation. |
| 주요 조치 | 새로고침, 운영 명령. |
| Button 활성 조건 | `session.canWrite(maintenance)` 필요. 실행 중 입력/닫기 비활성. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 실패 또는 결과 불명확 메시지에서 **재시도하지 말고 실행 이력을 조회해 대사**하도록 명시되어 있다. |
| Partial/NACK/Drift | 실패 또는 결과 불명확 메시지에서 **재시도하지 말고 실행 이력을 조회해 대사**하도록 명시되어 있다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/maintenance/MaintenancePage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Service, Endpoint, Instance, Action(DRAIN/DISABLE/RESUME), 감사 사유>=5. / Action DRAIN. 조회 limit 100.
- **실제 표시·컬럼**: 시간, Service, Endpoint, Instance, Action, 결과, 사유, Operation.
- **실제 조치**: 새로고침, 운영 명령.
- **권한·활성 조건**: `session.canWrite(maintenance)` 필요. 실행 중 입력/닫기 비활성.
- **Version·Approval·Idempotency**: Server가 인증 Operator·Transaction ID·Operation ID를 감사 기록.
- **응답 유실·부분 실패·복구**: 실패 또는 결과 불명확 메시지에서 **재시도하지 말고 실행 이력을 조회해 대사**하도록 명시되어 있다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/maintenance` 진입 시 Menu `MAINTENANCE`의 Server Session 권한과 Feature Flag `adm.route.maintenance.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Service, Endpoint, Instance, Action(DRAIN/DISABLE/RESUME), 감사 사유>=5. / Action DRAIN. 조회 limit 100.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 시간, Service, Endpoint, Instance, Action, 결과, 사유, Operation.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, 운영 명령.
5. 실행 전 활성 조건을 다시 검사한다: `session.canWrite(maintenance)` 필요. 실행 중 입력/닫기 비활성.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Server가 인증 Operator·Transaction ID·Operation ID를 감사 기록.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 실패 또는 결과 불명확 메시지에서 **재시도하지 말고 실행 이력을 조회해 대사**하도록 명시되어 있다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.16. 캐시 — `/cache`

| 항목 | 내용 |
|---|---|
| Menu | `CACHE` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/cache/CachePage.vue` / `adm.route.cache.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 캐시의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Tenant, Namespace, Key, Version>=1, 감사 사유(required). |
| 검색 기본값 | Version 최소 1. |
| 주요 표시값 | Cache summary/operation 구조화 결과. |
| 주요 조치 | 조회, Target별 갱신, 단일 Key 제거, Namespace 제거, Durable 재조정. |
| Button 활성 조건 | CACHE_REFRESH/CACHE_EVICT_KEY/CACHE_EVICT_NAMESPACE/CACHE_RECONCILE 각 Button permission으로 분리. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Evict/refresh 응답 유실 시 대상 Key/Namespace와 durable state를 재조회한 뒤 reconcile한다. |
| Partial/NACK/Drift | Evict/refresh 응답 유실 시 대상 Key/Namespace와 durable state를 재조회한 뒤 reconcile한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/cache/CachePage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Tenant, Namespace, Key, Version>=1, 감사 사유(required). / Version 최소 1.
- **실제 표시·컬럼**: Cache summary/operation 구조화 결과.
- **실제 조치**: 조회, Target별 갱신, 단일 Key 제거, Namespace 제거, Durable 재조정.
- **권한·활성 조건**: CACHE_REFRESH/CACHE_EVICT_KEY/CACHE_EVICT_NAMESPACE/CACHE_RECONCILE 각 Button permission으로 분리.
- **Version·Approval·Idempotency**: Control Version을 입력하고 durable reconcile에서 현재 원장과 맞춘다.
- **응답 유실·부분 실패·복구**: Evict/refresh 응답 유실 시 대상 Key/Namespace와 durable state를 재조회한 뒤 reconcile한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/cache` 진입 시 Menu `CACHE`의 Server Session 권한과 Feature Flag `adm.route.cache.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Tenant, Namespace, Key, Version>=1, 감사 사유(required). / Version 최소 1.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Cache summary/operation 구조화 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, Target별 갱신, 단일 Key 제거, Namespace 제거, Durable 재조정.
5. 실행 전 활성 조건을 다시 검사한다: CACHE_REFRESH/CACHE_EVICT_KEY/CACHE_EVICT_NAMESPACE/CACHE_RECONCILE 각 Button permission으로 분리.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Control Version을 입력하고 durable reconcile에서 현재 원장과 맞춘다.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Evict/refresh 응답 유실 시 대상 Key/Namespace와 durable state를 재조회한 뒤 reconcile한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.17. 설정 — `/configs`

| 항목 | 내용 |
|---|---|
| Menu | `CONFIG` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/configs/ConfigsPage.vue` / `adm.route.configs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 설정의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Config ID, Config Key, Config Value, 유형, 암호화(Y/N), 사유. |
| 검색 기본값 | 암호화 select Y/N. |
| 주요 표시값 | 설정 조회/상세/처리 결과. |
| 주요 조치 | 조회, 등록, 수정, 상세, 삭제. |
| Button 활성 조건 | 등록/수정/삭제는 CONFIG write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 암호화 설정은 평문 노출 여부를 확인하고 적용 결과를 재조회한다. |
| Partial/NACK/Drift | 암호화 설정은 평문 노출 여부를 확인하고 적용 결과를 재조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/configs/ConfigsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Config ID, Config Key, Config Value, 유형, 암호화(Y/N), 사유. / 암호화 select Y/N.
- **실제 표시·컬럼**: 설정 조회/상세/처리 결과.
- **실제 조치**: 조회, 등록, 수정, 상세, 삭제.
- **권한·활성 조건**: 등록/수정/삭제는 CONFIG write.
- **Version·Approval·Idempotency**: Config Owner의 상세 결과를 기준으로 변경 후 재조회.
- **응답 유실·부분 실패·복구**: 암호화 설정은 평문 노출 여부를 확인하고 적용 결과를 재조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/configs` 진입 시 Menu `CONFIG`의 Server Session 권한과 Feature Flag `adm.route.configs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Config ID, Config Key, Config Value, 유형, 암호화(Y/N), 사유. / 암호화 select Y/N.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 설정 조회/상세/처리 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 등록, 수정, 상세, 삭제.
5. 실행 전 활성 조건을 다시 검사한다: 등록/수정/삭제는 CONFIG write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Config Owner의 상세 결과를 기준으로 변경 후 재조회.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 암호화 설정은 평문 노출 여부를 확인하고 적용 결과를 재조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.18. 응답코드 — `/responseCodes`

| 항목 | 내용 |
|---|---|
| Menu | `RESPONSE_CODE` |
| Group / Risk | `framework` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/response-codes/ResponseCodesPage.vue` / `adm.route.responseCodes.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 응답코드의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Response Code, Message Code, 결과(S/E), 모듈, 그룹, 일련번호, HTTP 상태, 사유. |
| 검색 기본값 | 결과 선택 S/E. |
| 주요 표시값 | 응답코드 조회/상세/처리 결과. |
| 주요 조치 | 조회, 등록, 수정, 삭제, 상세. |
| Button 활성 조건 | 등록/수정은 write; 삭제는 delete permission. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | HTTP/업무 응답 매핑 변경 후 실제 조회 결과와 Consumer 계약을 확인한다. |
| Partial/NACK/Drift | HTTP/업무 응답 매핑 변경 후 실제 조회 결과와 Consumer 계약을 확인한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/response-codes/ResponseCodesPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Response Code, Message Code, 결과(S/E), 모듈, 그룹, 일련번호, HTTP 상태, 사유. / 결과 선택 S/E.
- **실제 표시·컬럼**: 응답코드 조회/상세/처리 결과.
- **실제 조치**: 조회, 등록, 수정, 삭제, 상세.
- **권한·활성 조건**: 등록/수정은 write; 삭제는 delete permission.
- **Version·Approval·Idempotency**: Owner 상세를 재조회해 변경 상태 확인.
- **응답 유실·부분 실패·복구**: HTTP/업무 응답 매핑 변경 후 실제 조회 결과와 Consumer 계약을 확인한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/responseCodes` 진입 시 Menu `RESPONSE_CODE`의 Server Session 권한과 Feature Flag `adm.route.responseCodes.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Response Code, Message Code, 결과(S/E), 모듈, 그룹, 일련번호, HTTP 상태, 사유. / 결과 선택 S/E.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 응답코드 조회/상세/처리 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 등록, 수정, 삭제, 상세.
5. 실행 전 활성 조건을 다시 검사한다: 등록/수정은 write; 삭제는 delete permission.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Owner 상세를 재조회해 변경 상태 확인.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: HTTP/업무 응답 매핑 변경 후 실제 조회 결과와 Consumer 계약을 확인한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.19. 영업일·휴일 — `/businessCalendar`

| 항목 | 내용 |
|---|---|
| Menu | `BUSINESS_CALENDAR` |
| Group / Risk | `framework` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue` / `adm.route.businessCalendar.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 영업일·휴일의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Calendar ID, 기준일, 영업일/휴일, Day Type, 기관코드, 업무 사유, 감사 사유; resolve용 Calendar ID/date/offset. |
| 검색 기본값 | Calendar ID DEFAULT, 기준일 오늘, businessDay=false, Day Type HOLIDAY, resolve offset=1. |
| 주요 표시값 | Calendar Provider WRITE/READ ONLY, 권한, 일자/구분/Type/기관/사유/Version. |
| 주요 조치 | 조회, 저장, 삭제, 영업일 계산. |
| Button 활성 조건 | Backend writable=true + BUSINESS_CALENDAR write/delete permission. 삭제는 version>0. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 409이면 최신 Calendar row/version을 다시 읽고 재판단한다. 같은 expectedVersion으로 무조건 반복하지 않는다. |
| Partial/NACK/Drift | 409이면 최신 Calendar row/version을 다시 읽고 재판단한다. 같은 expectedVersion으로 무조건 반복하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Calendar ID, 기준일, 영업일/휴일, Day Type, 기관코드, 업무 사유, 감사 사유; resolve용 Calendar ID/date/offset. / Calendar ID DEFAULT, 기준일 오늘, businessDay=false, Day Type HOLIDAY, resolve offset=1.
- **실제 표시·컬럼**: Calendar Provider WRITE/READ ONLY, 권한, 일자/구분/Type/기관/사유/Version.
- **실제 조치**: 조회, 저장, 삭제, 영업일 계산.
- **권한·활성 조건**: Backend writable=true + BUSINESS_CALENDAR write/delete permission. 삭제는 version>0.
- **Version·Approval·Idempotency**: PUT/DELETE에 expectedVersion을 전달. 409를 `동시 변경 충돌`로 표시.
- **응답 유실·부분 실패·복구**: 409이면 최신 Calendar row/version을 다시 읽고 재판단한다. 같은 expectedVersion으로 무조건 반복하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/businessCalendar` 진입 시 Menu `BUSINESS_CALENDAR`의 Server Session 권한과 Feature Flag `adm.route.businessCalendar.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Calendar ID, 기준일, 영업일/휴일, Day Type, 기관코드, 업무 사유, 감사 사유; resolve용 Calendar ID/date/offset. / Calendar ID DEFAULT, 기준일 오늘, businessDay=false, Day Type HOLIDAY, resolve offset=1.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Calendar Provider WRITE/READ ONLY, 권한, 일자/구분/Type/기관/사유/Version.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 저장, 삭제, 영업일 계산.
5. 실행 전 활성 조건을 다시 검사한다: Backend writable=true + BUSINESS_CALENDAR write/delete permission. 삭제는 version>0.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: PUT/DELETE에 expectedVersion을 전달. 409를 `동시 변경 충돌`로 표시.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 409이면 최신 Calendar row/version을 다시 읽고 재판단한다. 같은 expectedVersion으로 무조건 반복하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.20. 복구 센터 — `/recoveryCenter`

| 항목 | 내용 |
|---|---|
| Menu | `RECOVERY_CENTER` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/recovery-center/RecoveryCenterPage.vue` / `adm.route.recoveryCenter.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 복구 센터의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | DLQ Message ID, Unknown ID, Recovery Target, Recovery Event ID, 사유. |
| 검색 기본값 | 표시 목록 각 최대 20건. |
| 주요 표시값 | Unknown/DLQ/Outbox/File Transfer KPI; Unknown ID/유형/상태/거래ID; DLQ Message/Topic/상태. |
| 주요 조치 | 복구 상태, DLQ Replay, 결과 확정, Poison Retry, 복구 실행. |
| Button 활성 조건 | 실제 변경은 Reliability 화면의 권한·감사사유·멱등 Gate를 재사용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Blind retry가 아니라 대상 원장 상태를 확인해 Replay/Reconcile/Poison Recovery를 분리한다. |
| Partial/NACK/Drift | Blind retry가 아니라 대상 원장 상태를 확인해 Replay/Reconcile/Poison Recovery를 분리한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/recovery-center/RecoveryCenterPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: DLQ Message ID, Unknown ID, Recovery Target, Recovery Event ID, 사유. / 표시 목록 각 최대 20건.
- **실제 표시·컬럼**: Unknown/DLQ/Outbox/File Transfer KPI; Unknown ID/유형/상태/거래ID; DLQ Message/Topic/상태.
- **실제 조치**: 복구 상태, DLQ Replay, 결과 확정, Poison Retry, 복구 실행.
- **권한·활성 조건**: 실제 변경은 Reliability 화면의 권한·감사사유·멱등 Gate를 재사용.
- **Version·Approval·Idempotency**: Unknown/DLQ/Recovery 원장의 식별자와 상태를 기준으로 조치.
- **응답 유실·부분 실패·복구**: Blind retry가 아니라 대상 원장 상태를 확인해 Replay/Reconcile/Poison Recovery를 분리한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/recoveryCenter` 진입 시 Menu `RECOVERY_CENTER`의 Server Session 권한과 Feature Flag `adm.route.recoveryCenter.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: DLQ Message ID, Unknown ID, Recovery Target, Recovery Event ID, 사유. / 표시 목록 각 최대 20건.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Unknown/DLQ/Outbox/File Transfer KPI; Unknown ID/유형/상태/거래ID; DLQ Message/Topic/상태.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 복구 상태, DLQ Replay, 결과 확정, Poison Retry, 복구 실행.
5. 실행 전 활성 조건을 다시 검사한다: 실제 변경은 Reliability 화면의 권한·감사사유·멱등 Gate를 재사용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Unknown/DLQ/Recovery 원장의 식별자와 상태를 기준으로 조치.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Blind retry가 아니라 대상 원장 상태를 확인해 Replay/Reconcile/Poison Recovery를 분리한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.21. Error·Unknown Result — `/incidents`

| 항목 | 내용 |
|---|---|
| Menu | `INCIDENT` |
| Group / Risk | `monitoring` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/ErrorWorkbenchPage.vue` / `adm.route.incidents.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Error·Unknown Result의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Tab unknown/dlq/recovery, Status, Transaction ID, Topic(DLQ만); 위험조치 Expected Version>=0, 사유10~500, Unknown action RECONCILE/REPLAY/COMPENSATE/MARK_FAILED. |
| 검색 기본값 | Tab unknown, expectedVersion은 선택 Row version에서 채움. |
| 주요 표시값 | ID, 상태, Transaction, Topic/Target, Version, 발생시각; 대상 구조화 상세. |
| 주요 조치 | 결과 확정, DLQ Replay, Recovery 실행. |
| Button 활성 조건 | RELIABILITY_RESOLVE/RELIABILITY_REPLAY/RELIABILITY_RECOVERY_RUN Button permission. 확인 checkbox+reason>=10. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | UNKNOWN을 실패로 단정하지 않는다. action 의미에 따라 Reconcile/Replay/Compensate/Mark Failed를 선택하고 실행 후 재조회한다. |
| Partial/NACK/Drift | UNKNOWN을 실패로 단정하지 않는다. action 의미에 따라 Reconcile/Replay/Compensate/Mark Failed를 선택하고 실행 후 재조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/ErrorWorkbenchPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Tab unknown/dlq/recovery, Status, Transaction ID, Topic(DLQ만); 위험조치 Expected Version>=0, 사유10~500, Unknown action RECONCILE/REPLAY/COMPENSATE/MARK_FAILED. / Tab unknown, expectedVersion은 선택 Row version에서 채움.
- **실제 표시·컬럼**: ID, 상태, Transaction, Topic/Target, Version, 발생시각; 대상 구조화 상세.
- **실제 조치**: 결과 확정, DLQ Replay, Recovery 실행.
- **권한·활성 조건**: RELIABILITY_RESOLVE/RELIABILITY_REPLAY/RELIABILITY_RECOVERY_RUN Button permission. 확인 checkbox+reason>=10.
- **Version·Approval·Idempotency**: 선택 Row의 version/expectedVersion 사용.
- **응답 유실·부분 실패·복구**: UNKNOWN을 실패로 단정하지 않는다. action 의미에 따라 Reconcile/Replay/Compensate/Mark Failed를 선택하고 실행 후 재조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/incidents` 진입 시 Menu `INCIDENT`의 Server Session 권한과 Feature Flag `adm.route.incidents.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Tab unknown/dlq/recovery, Status, Transaction ID, Topic(DLQ만); 위험조치 Expected Version>=0, 사유10~500, Unknown action RECONCILE/REPLAY/COMPENSATE/MARK_FAILED. / Tab unknown, expectedVersion은 선택 Row version에서 채움.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: ID, 상태, Transaction, Topic/Target, Version, 발생시각; 대상 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 결과 확정, DLQ Replay, Recovery 실행.
5. 실행 전 활성 조건을 다시 검사한다: RELIABILITY_RESOLVE/RELIABILITY_REPLAY/RELIABILITY_RECOVERY_RUN Button permission. 확인 checkbox+reason>=10.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 선택 Row의 version/expectedVersion 사용.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: UNKNOWN을 실패로 단정하지 않는다. action 의미에 따라 Reconcile/Replay/Compensate/Mark Failed를 선택하고 실행 후 재조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.22. Analysis Center — `/reliability`

| 항목 | 내용 |
|---|---|
| Menu | `RELIABILITY` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/AnalysisCenterPage.vue` / `adm.route.reliability.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Analysis Center의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Transaction ID, Topic. |
| 검색 기본값 | 각 Unknown/DLQ/Outbox/Idempotency/File I/O 최대 200. |
| 주요 표시값 | 영역별 count/risky 카드; 영역, 상태, 건수, 대표 Transaction 표; Freshness/filters 상세. |
| 주요 조치 | 분석/재분석. |
| Button 활성 조건 | 읽기 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 5개 데이터 원천은 PromiseAllSettled. 일부 실패 시 `부분 실패 n/5`를 표시하고 성공 데이터는 유지한다. |
| Partial/NACK/Drift | 5개 데이터 원천은 PromiseAllSettled. 일부 실패 시 `부분 실패 n/5`를 표시하고 성공 데이터는 유지한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/AnalysisCenterPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Transaction ID, Topic. / 각 Unknown/DLQ/Outbox/Idempotency/File I/O 최대 200.
- **실제 표시·컬럼**: 영역별 count/risky 카드; 영역, 상태, 건수, 대표 Transaction 표; Freshness/filters 상세.
- **실제 조치**: 분석/재분석.
- **권한·활성 조건**: 읽기 전용.
- **Version·Approval·Idempotency**: 조회 화면.
- **응답 유실·부분 실패·복구**: 5개 데이터 원천은 PromiseAllSettled. 일부 실패 시 `부분 실패 n/5`를 표시하고 성공 데이터는 유지한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/reliability` 진입 시 Menu `RELIABILITY`의 Server Session 권한과 Feature Flag `adm.route.reliability.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Transaction ID, Topic. / 각 Unknown/DLQ/Outbox/Idempotency/File I/O 최대 200.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 영역별 count/risky 카드; 영역, 상태, 건수, 대표 Transaction 표; Freshness/filters 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 분석/재분석.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 조회 화면.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 5개 데이터 원천은 PromiseAllSettled. 일부 실패 시 `부분 실패 n/5`를 표시하고 성공 데이터는 유지한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.23. 알림 — `/notifications`

| 항목 | 내용 |
|---|---|
| Menu | `NOTIFICATION` |
| Group / Risk | `integration` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/notifications/NotificationsPage.vue` / `adm.route.notifications.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 알림의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Rule ID, Event Type/Sub Type, Channel, Severity(INFO/WARN/ERROR), Receiver Group, useYn, 수신자, 테스트 메시지, 규칙 변경 사유; Delivery ID/Expected Version/Operation ID(readonly), 운영조치 사유. |
| 검색 기본값 | Severity 선택값, useYn Y/N. 선택 Delivery의 Version/Operation을 자동 반영. |
| 주요 표시값 | 규칙 ID/Event/채널/심각도/수신그룹/사용; Delivery/Operation+requestHash/상태/대상/수신자/시도/다음시도/오류/Lease/Version/요청·수정; Provider Attempt 이력. |
| 주요 조치 | 전체조회, DLQ조회, 규칙 저장/비활성, 테스트 발송, CSV, Delivery 재시도/취소, 상세/신규/수정. |
| Button 활성 조건 | 각 NOTIFICATION_* Button permission. Retry/Cancel은 선택 Delivery 상태와 action eligibility를 함께 확인. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | DLQ/Retry 상태·Attempt·Lease를 확인하고 재시도한다. 응답 유실 시 Operation/Delivery 상태를 먼저 조회한다. |
| Partial/NACK/Drift | DLQ/Retry 상태·Attempt·Lease를 확인하고 재시도한다. 응답 유실 시 Operation/Delivery 상태를 먼저 조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/notifications/NotificationsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Rule ID, Event Type/Sub Type, Channel, Severity(INFO/WARN/ERROR), Receiver Group, useYn, 수신자, 테스트 메시지, 규칙 변경 사유; Delivery ID/Expected Version/Operation ID(readonly), 운영조치 사유. / Severity 선택값, useYn Y/N. 선택 Delivery의 Version/Operation을 자동 반영.
- **실제 표시·컬럼**: 규칙 ID/Event/채널/심각도/수신그룹/사용; Delivery/Operation+requestHash/상태/대상/수신자/시도/다음시도/오류/Lease/Version/요청·수정; Provider Attempt 이력.
- **실제 조치**: 전체조회, DLQ조회, 규칙 저장/비활성, 테스트 발송, CSV, Delivery 재시도/취소, 상세/신규/수정.
- **권한·활성 조건**: 각 NOTIFICATION_* Button permission. Retry/Cancel은 선택 Delivery 상태와 action eligibility를 함께 확인.
- **Version·Approval·Idempotency**: Delivery Expected Version, Operation ID, Request Hash, Lease Version을 표시.
- **응답 유실·부분 실패·복구**: DLQ/Retry 상태·Attempt·Lease를 확인하고 재시도한다. 응답 유실 시 Operation/Delivery 상태를 먼저 조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/notifications` 진입 시 Menu `NOTIFICATION`의 Server Session 권한과 Feature Flag `adm.route.notifications.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Rule ID, Event Type/Sub Type, Channel, Severity(INFO/WARN/ERROR), Receiver Group, useYn, 수신자, 테스트 메시지, 규칙 변경 사유; Delivery ID/Expected Version/Operation ID(readonly), 운영조치 사유. / Severity 선택값, useYn Y/N. 선택 Delivery의 Version/Operation을 자동 반영.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 규칙 ID/Event/채널/심각도/수신그룹/사용; Delivery/Operation+requestHash/상태/대상/수신자/시도/다음시도/오류/Lease/Version/요청·수정; Provider Attempt 이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 전체조회, DLQ조회, 규칙 저장/비활성, 테스트 발송, CSV, Delivery 재시도/취소, 상세/신규/수정.
5. 실행 전 활성 조건을 다시 검사한다: 각 NOTIFICATION_* Button permission. Retry/Cancel은 선택 Delivery 상태와 action eligibility를 함께 확인.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Delivery Expected Version, Operation ID, Request Hash, Lease Version을 표시.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: DLQ/Retry 상태·Attempt·Lease를 확인하고 재시도한다. 응답 유실 시 Operation/Delivery 상태를 먼저 조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.24. Batch / Center-Cut — `/batch`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch/BatchPage.vue` / `adm.route.batch.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Batch / Center-Cut의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Job ID/명/유형(TASKLET/CHUNK/RETRY), Execution ID, Schedule ID, 파라미터, Calendar, 영업일, 시뮬레이션 1~62일, Dispatch 상태, Heartbeat 30~86400초, Lock Key, Ghost Action(FAIL/ABANDON/RELEASE_LOCK), 사유; 실행추적 Job/Transaction/Spring Instance/Worker/Server/limit1~500; Center-Cut Job/status/result/limit. |
| 검색 기본값 | 실행추적 limit=100. Dispatch 상태 초기 예시값 `WAITING`. |
| 주요 표시값 | Execution trace 9열; Center-Cut 전체/대기/처리중/성공/실패 KPI, Job/Target/Result 상세; 배치 작업 구조화 응답. |
| 주요 조치 | 조회, Job 등록/수동실행/재수행/중지, Scheduler 1회, Job 상세/시뮬레이션/관계/대상/Step/Worker/Lock/Ghost/운영로그/CSV, Lock 해제, Center-Cut 조회. |
| Button 활성 조건 | Mutation은 BATCH write. Lock/Ghost/재실행은 대상 상태·사유를 확인. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Ghost/Lock/UNKNOWN을 별도 조회하고 바로 재실행하지 않는다. Center-Cut은 Target/Result 상태와 거래 Segment를 함께 대사한다. |
| Partial/NACK/Drift | Ghost/Lock/UNKNOWN을 별도 조회하고 바로 재실행하지 않는다. Center-Cut은 Target/Result 상태와 거래 Segment를 함께 대사한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch/BatchPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Job ID/명/유형(TASKLET/CHUNK/RETRY), Execution ID, Schedule ID, 파라미터, Calendar, 영업일, 시뮬레이션 1~62일, Dispatch 상태, Heartbeat 30~86400초, Lock Key, Ghost Action(FAIL/ABANDON/RELEASE_LOCK), 사유; 실행추적 Job/Transaction/Spring Instance/Worker/Server/limit1~500; Center-Cut Job/status/result/limit. / 실행추적 limit=100. Dispatch 상태 초기 예시값 `WAITING`.
- **실제 표시·컬럼**: Execution trace 9열; Center-Cut 전체/대기/처리중/성공/실패 KPI, Job/Target/Result 상세; 배치 작업 구조화 응답.
- **실제 조치**: 조회, Job 등록/수동실행/재수행/중지, Scheduler 1회, Job 상세/시뮬레이션/관계/대상/Step/Worker/Lock/Ghost/운영로그/CSV, Lock 해제, Center-Cut 조회.
- **권한·활성 조건**: Mutation은 BATCH write. Lock/Ghost/재실행은 대상 상태·사유를 확인.
- **Version·Approval·Idempotency**: Execution/Lock/Worker/Center-Cut 식별자와 transaction/segment 연결.
- **응답 유실·부분 실패·복구**: Ghost/Lock/UNKNOWN을 별도 조회하고 바로 재실행하지 않는다. Center-Cut은 Target/Result 상태와 거래 Segment를 함께 대사한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch` 진입 시 Menu `BATCH`의 Server Session 권한과 Feature Flag `adm.route.batch.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Job ID/명/유형(TASKLET/CHUNK/RETRY), Execution ID, Schedule ID, 파라미터, Calendar, 영업일, 시뮬레이션 1~62일, Dispatch 상태, Heartbeat 30~86400초, Lock Key, Ghost Action(FAIL/ABANDON/RELEASE_LOCK), 사유; 실행추적 Job/Transaction/Spring Instance/Worker/Server/limit1~500; Center-Cut Job/status/result/limit. / 실행추적 limit=100. Dispatch 초기 예시값 WAITING.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Execution trace 9열; Center-Cut 전체/대기/처리중/성공/실패 KPI, Job/Target/Result 상세; 배치 작업 구조화 응답.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, Job 등록/수동실행/재수행/중지, Scheduler 1회, Job 상세/시뮬레이션/관계/대상/Step/Worker/Lock/Ghost/운영로그/CSV, Lock 해제, Center-Cut 조회.
5. 실행 전 활성 조건을 다시 검사한다: Mutation은 BATCH write. Lock/Ghost/재실행은 대상 상태·사유를 확인.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Execution/Lock/Worker/Center-Cut 식별자와 transaction/segment 연결.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Ghost/Lock/UNKNOWN을 별도 조회하고 바로 재실행하지 않는다. Center-Cut은 Target/Result 상태와 거래 Segment를 함께 대사한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.25. Batch Overview — `/batch-overview`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_OVERVIEW` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-overview/BatchOverviewPage.vue → BatchOperationsWorkbench.vue(mode=overview)` / `adm.route.batch-overview.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Batch Overview의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 필터 없음. 새로고침. |
| 검색 기본값 | 최근 실행 Snapshot. |
| 주요 표시값 | KPI Job/Schedule/실행/실패/실행중/Worker/Stale Worker/Unknown Result; 최근 실행 Execution/Job/상태/시작. |
| 주요 조치 | 새로고침, 행 상세. |
| Button 활성 조건 | 조회 전용 mode. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | partial/stale/failure Banner를 통해 재조회. 위험조치는 다른 전용 mode로 이동. |
| Partial/NACK/Drift | partial/stale/failure Banner를 통해 재조회. 위험조치는 다른 전용 mode로 이동. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-overview/BatchOverviewPage.vue → BatchOperationsWorkbench.vue(mode=overview)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 필터 없음. 새로고침. / 최근 실행 Snapshot.
- **실제 표시·컬럼**: KPI Job/Schedule/실행/실패/실행중/Worker/Stale Worker/Unknown Result; 최근 실행 Execution/Job/상태/시작.
- **실제 조치**: 새로고침, 행 상세.
- **권한·활성 조건**: 조회 전용 mode.
- **Version·Approval·Idempotency**: 상세에서 운영 상태와 식별자 확인.
- **응답 유실·부분 실패·복구**: partial/stale/failure Banner를 통해 재조회. 위험조치는 다른 전용 mode로 이동.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-overview` 진입 시 Menu `BATCH_OVERVIEW`의 Server Session 권한과 Feature Flag `adm.route.batch-overview.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 필터 없음. 새로고침. / 최근 실행 Snapshot.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: KPI Job/Schedule/실행/실패/실행중/Worker/Stale Worker/Unknown Result; 최근 실행 Execution/Job/상태/시작.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, 행 상세.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용 mode.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 상세에서 운영 상태와 식별자 확인.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: partial/stale/failure Banner를 통해 재조회. 위험조치는 다른 전용 mode로 이동. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.26. Runtime Topology — `/batch-runtime`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_RUNTIME` |
| Group / Risk | `batch` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-runtime-control/RuntimeTopologyPage.vue → BatchOperationsWorkbench.vue(mode=topology)` / `adm.route.batch-runtime.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Runtime Topology의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | 현재 Runtime view. |
| 주요 표시값 | Instance, Role, 상태, Heartbeat; 선택 Node 상세/원본 JSON/운영 이력. |
| 주요 조치 | 새로고침, 상세 열기. |
| Button 활성 조건 | 조회 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | stale/partial 상태를 Banner로 구분하고 Worker/Agent 화면에서 원인 조치. |
| Partial/NACK/Drift | stale/partial 상태를 Banner로 구분하고 Worker/Agent 화면에서 원인 조치. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-runtime-control/RuntimeTopologyPage.vue → BatchOperationsWorkbench.vue(mode=topology)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / 현재 Runtime view.
- **실제 표시·컬럼**: Instance, Role, 상태, Heartbeat; 선택 Node 상세/원본 JSON/운영 이력.
- **실제 조치**: 새로고침, 상세 열기.
- **권한·활성 조건**: 조회 전용.
- **Version·Approval·Idempotency**: Runtime node 식별자/heartbeat.
- **응답 유실·부분 실패·복구**: stale/partial 상태를 Banner로 구분하고 Worker/Agent 화면에서 원인 조치.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-runtime` 진입 시 Menu `BATCH_RUNTIME`의 Server Session 권한과 Feature Flag `adm.route.batch-runtime.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / 현재 Runtime view.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Instance, Role, 상태, Heartbeat; 선택 Node 상세/원본 JSON/운영 이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, 상세 열기.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Runtime node 식별자/heartbeat.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: stale/partial 상태를 Banner로 구분하고 Worker/Agent 화면에서 원인 조치. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.27. Runtime Instances — `/batch-instances`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_INSTANCES` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-instances/BatchInstancesPage.vue → BatchOperationsWorkbench.vue(mode=instances)` / `adm.route.batch-instances.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Runtime Instances의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | 현재 instance view. |
| 주요 표시값 | Instance, Service, 상태, Artifact, Fencing; 상세/운영이력. |
| 주요 조치 | 새로고침, 상세. |
| Button 활성 조건 | 조회 전용 route. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Artifact drift/heartbeat/fencing을 확인하고 Agent/Deployment route로 이동. |
| Partial/NACK/Drift | Artifact drift/heartbeat/fencing을 확인하고 Agent/Deployment route로 이동. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-instances/BatchInstancesPage.vue → BatchOperationsWorkbench.vue(mode=instances)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / 현재 instance view.
- **실제 표시·컬럼**: Instance, Service, 상태, Artifact, Fencing; 상세/운영이력.
- **실제 조치**: 새로고침, 상세.
- **권한·활성 조건**: 조회 전용 route.
- **Version·Approval·Idempotency**: Artifact version + fencing token.
- **응답 유실·부분 실패·복구**: Artifact drift/heartbeat/fencing을 확인하고 Agent/Deployment route로 이동.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-instances` 진입 시 Menu `BATCH_INSTANCES`의 Server Session 권한과 Feature Flag `adm.route.batch-instances.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / 현재 instance view.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Instance, Service, 상태, Artifact, Fencing; 상세/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, 상세.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용 route.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Artifact version + fencing token.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Artifact drift/heartbeat/fencing을 확인하고 Agent/Deployment route로 이동. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.28. Scheduler HA — `/batch-scheduler`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_SCHEDULER` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-scheduler/BatchSchedulerPage.vue → BatchOperationsWorkbench.vue(mode=scheduler)` / `adm.route.batch-scheduler.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Scheduler HA의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 통합 검색. |
| 검색 기본값 | page size 50(20/50/100). |
| 주요 표시값 | Schedule, Job, Cron/Policy, 상태, Next Fire; 상세/원본/운영이력. |
| 주요 조치 | 조회/초기화, Scheduler 1회 실행, Schedule 활성/비활성. |
| Button 활성 조건 | Schedule enable/disable는 HIGH + approval + expectedVersion. Scheduler 1회는 CRITICAL + approval. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Misfire/중복 실행 영향을 확인하고 결과불명은 실행이력·Operation을 조회. |
| Partial/NACK/Drift | Misfire/중복 실행 영향을 확인하고 결과불명은 실행이력·Operation을 조회. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-scheduler/BatchSchedulerPage.vue → BatchOperationsWorkbench.vue(mode=scheduler)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 통합 검색. / page size 50(20/50/100).
- **실제 표시·컬럼**: Schedule, Job, Cron/Policy, 상태, Next Fire; 상세/원본/운영이력.
- **실제 조치**: 조회/초기화, Scheduler 1회 실행, Schedule 활성/비활성.
- **권한·활성 조건**: Schedule enable/disable는 HIGH + approval + expectedVersion. Scheduler 1회는 CRITICAL + approval.
- **Version·Approval·Idempotency**: Schedule expectedVersion을 위험조치 Dialog에 사용.
- **응답 유실·부분 실패·복구**: Misfire/중복 실행 영향을 확인하고 결과불명은 실행이력·Operation을 조회.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-scheduler` 진입 시 Menu `BATCH_SCHEDULER`의 Server Session 권한과 Feature Flag `adm.route.batch-scheduler.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 통합 검색. / page size 50(20/50/100).
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Schedule, Job, Cron/Policy, 상태, Next Fire; 상세/원본/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/초기화, Scheduler 1회 실행, Schedule 활성/비활성.
5. 실행 전 활성 조건을 다시 검사한다: Schedule enable/disable는 HIGH + approval + expectedVersion. Scheduler 1회는 CRITICAL + approval.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Schedule expectedVersion을 위험조치 Dialog에 사용.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Misfire/중복 실행 영향을 확인하고 결과불명은 실행이력·Operation을 조회. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.29. Worker Pools — `/batch-worker-pools`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_WORKER_POOLS` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-worker-pools/BatchWorkerPoolsPage.vue → RuntimeFleetWorkbench.vue(view=worker-pools)` / `adm.route.batch-worker-pools.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Worker Pools의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 자유검색, Command(DRAIN/RESUME/RESTART), 사유>=5, 승인요청 ID, 승인자, 승인정책 Version. |
| 검색 기본값 | approvalPolicyVersion=BAT-RUNTIME-V1. |
| 주요 표시값 | ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 선택 상세/Command 결과. |
| 주요 조치 | DRAIN, RESUME, RESTART, Command 상태 조회. |
| Button 활성 조건 | BATCH_RUNTIME_COMMAND/BAT_RUNTIME_COMMAND/BATCH_{ACTION}; DRAIN은 RUNNING/UP/ACTIVE, RESUME은 DRAINING/DRAINED/STOPPED/DOWN. 요청자와 승인자 분리, 최종확인 필수. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | UNKNOWN_RESULT이면 자동 재실행하지 않고 Command 상태를 조회한다. |
| Partial/NACK/Drift | UNKNOWN_RESULT이면 자동 재실행하지 않고 Command 상태를 조회한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-worker-pools/BatchWorkerPoolsPage.vue → RuntimeFleetWorkbench.vue(view=worker-pools)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 자유검색, Command(DRAIN/RESUME/RESTART), 사유>=5, 승인요청 ID, 승인자, 승인정책 Version. / approvalPolicyVersion=BAT-RUNTIME-V1.
- **실제 표시·컬럼**: ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 선택 상세/Command 결과.
- **실제 조치**: DRAIN, RESUME, RESTART, Command 상태 조회.
- **권한·활성 조건**: BATCH_RUNTIME_COMMAND/BAT_RUNTIME_COMMAND/BATCH_{ACTION}; DRAIN은 RUNNING/UP/ACTIVE, RESUME은 DRAINING/DRAINED/STOPPED/DOWN. 요청자와 승인자 분리, 최종확인 필수.
- **Version·Approval·Idempotency**: idempotencyKey=`view:target:action:version`, target Snapshot SHA-256, expectedVersion, expiresAt=요청+10분.
- **응답 유실·부분 실패·복구**: UNKNOWN_RESULT이면 자동 재실행하지 않고 Command 상태를 조회한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-worker-pools` 진입 시 Menu `BATCH_WORKER_POOLS`의 Server Session 권한과 Feature Flag `adm.route.batch-worker-pools.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 자유검색, Command(DRAIN/RESUME/RESTART), 사유>=5, 승인요청 ID, 승인자, 승인정책 Version. / approvalPolicyVersion=BAT-RUNTIME-V1.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 선택 상세/Command 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: DRAIN, RESUME, RESTART, Command 상태 조회.
5. 실행 전 활성 조건을 다시 검사한다: BATCH_RUNTIME_COMMAND/BAT_RUNTIME_COMMAND/BATCH_{ACTION}; DRAIN은 RUNNING/UP/ACTIVE, RESUME은 DRAINING/DRAINED/STOPPED/DOWN. 요청자와 승인자 분리, 최종확인 필수.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: idempotencyKey=`view:target:action:version`, target Snapshot SHA-256, expectedVersion, expiresAt=요청+10분.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: UNKNOWN_RESULT이면 자동 재실행하지 않고 Command 상태를 조회한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.30. Center-Cut — `/batch-center-cut`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_CENTER_CUT` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-center-cut/BatchCenterCutPage.vue` / `adm.route.batch-center-cut.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Center-Cut의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Job 선택. 위험조치 사유/멱등키/승인 ID는 DangerousActionDialog에서 입력. |
| 검색 기본값 | Job 목록 조회 후 선택 Execution을 대상으로 함. |
| 주요 표시값 | Job/이름/상태; Execution/Result/상태/메시지/운영조치; Summary 최대 12항목. |
| 주요 조치 | FAILED Execution 실패 재처리, UNKNOWN Execution 대사. |
| Button 활성 조건 | FAILED+executionId만 reprocess, UNKNOWN+executionId만 reconcile. CRITICAL. 승인 Ticket이 없으면 먼저 승인 요청 생성 후 실행. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | UNKNOWN은 blind retry 금지. Owner/외부 결과 대사 후 선택 Execution만 reconcile. |
| Partial/NACK/Drift | UNKNOWN은 blind retry 금지. Owner/외부 결과 대사 후 선택 Execution만 reconcile. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-center-cut/BatchCenterCutPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Job 선택. 위험조치 사유/멱등키/승인 ID는 DangerousActionDialog에서 입력. / Job 목록 조회 후 선택 Execution을 대상으로 함.
- **실제 표시·컬럼**: Job/이름/상태; Execution/Result/상태/메시지/운영조치; Summary 최대 12항목.
- **실제 조치**: FAILED Execution 실패 재처리, UNKNOWN Execution 대사.
- **권한·활성 조건**: FAILED+executionId만 reprocess, UNKNOWN+executionId만 reconcile. CRITICAL. 승인 Ticket이 없으면 먼저 승인 요청 생성 후 실행.
- **Version·Approval·Idempotency**: Job 단위 일괄 변경 금지. Execution 단위 승인/멱등키.
- **응답 유실·부분 실패·복구**: UNKNOWN은 blind retry 금지. Owner/외부 결과 대사 후 선택 Execution만 reconcile.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-center-cut` 진입 시 Menu `BATCH_CENTER_CUT`의 Server Session 권한과 Feature Flag `adm.route.batch-center-cut.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Job 선택. 위험조치 사유/멱등키/승인 ID는 DangerousActionDialog에서 입력. / Job 목록 조회 후 선택 Execution을 대상으로 함.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Job/이름/상태; Execution/Result/상태/메시지/운영조치; Summary 최대 12항목.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: FAILED Execution 실패 재처리, UNKNOWN Execution 대사.
5. 실행 전 활성 조건을 다시 검사한다: FAILED+executionId만 reprocess, UNKNOWN+executionId만 reconcile. CRITICAL. 승인 Ticket이 없으면 먼저 승인 요청 생성 후 실행.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Job 단위 일괄 변경 금지. Execution 단위 승인/멱등키.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: UNKNOWN은 blind retry 금지. Owner/외부 결과 대사 후 선택 Execution만 reconcile. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.31. Host Agents — `/batch-agents`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_AGENTS` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-agents/BatchAgentsPage.vue → RuntimeFleetWorkbench.vue(view=agents)` / `adm.route.batch-agents.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Host Agents의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 자유검색, Command DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, 사유>=5, 승인 ID/승인자/정책. |
| 검색 기본값 | approvalPolicyVersion=BAT-RUNTIME-V1. |
| 주요 표시값 | ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 상세/Command 결과. |
| 주요 조치 | DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, Command 상태조회. |
| Button 활성 조건 | Runtime button grant + 상태조건 + 요청자/승인자 분리 + 확인. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | UNKNOWN_RESULT 자동 재실행 금지. 상태조회 후 Rollback/Resume 여부 판단. |
| Partial/NACK/Drift | UNKNOWN_RESULT 자동 재실행 금지. 상태조회 후 Rollback/Resume 여부 판단. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-agents/BatchAgentsPage.vue → RuntimeFleetWorkbench.vue(view=agents)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 자유검색, Command DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, 사유>=5, 승인 ID/승인자/정책. / approvalPolicyVersion=BAT-RUNTIME-V1.
- **실제 표시·컬럼**: ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 상세/Command 결과.
- **실제 조치**: DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, Command 상태조회.
- **권한·활성 조건**: Runtime button grant + 상태조건 + 요청자/승인자 분리 + 확인.
- **Version·Approval·Idempotency**: Snapshot hash, expectedVersion, idempotency key, expiresAt 10분.
- **응답 유실·부분 실패·복구**: UNKNOWN_RESULT 자동 재실행 금지. 상태조회 후 Rollback/Resume 여부 판단.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-agents` 진입 시 Menu `BATCH_AGENTS`의 Server Session 권한과 Feature Flag `adm.route.batch-agents.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 자유검색, Command DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, 사유>=5, 승인 ID/승인자/정책. / approvalPolicyVersion=BAT-RUNTIME-V1.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: ID, Role/Pool, State, Host/Zone, Version, Heartbeat, Fencing; 상세/Command 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: DRAIN/RESUME/RESTART/STOP/START/ROLLBACK, Command 상태조회.
5. 실행 전 활성 조건을 다시 검사한다: Runtime button grant + 상태조건 + 요청자/승인자 분리 + 확인.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Snapshot hash, expectedVersion, idempotency key, expiresAt 10분.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: UNKNOWN_RESULT 자동 재실행 금지. 상태조회 후 Rollback/Resume 여부 판단. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.32. Job Packs — `/batch-job-packs`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_JOB_PACKS` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-job-packs/BatchJobPacksPage.vue` / `adm.route.batch-job-packs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Job Packs의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Job ID, Definition Version>=1, Job명, Owner, 설명; Executor, Trigger, Timezone, Misfire; Typed Parameter; Agent Pool/Zone/Concurrency/Timeout/Attempts/Backoff/Skip/Restartable/Unknown Policy/Compensation; Dependency; Alert/SLA; Checksum, 사유, 시행기간. |
| 검색 기본값 | Version1, Executor SPRING_BATCH, state DRAFT, owner BAT, CRON `0 0 1 * * *`, Asia/Seoul, Misfire FAIL_CLOSED, pool DEFAULT, concurrency1, timeout3600s, restartable=true, attempts1, backoff0, multiplier1, Unknown FAIL_CLOSED, failure/missed notification=true. |
| 주요 표시값 | Job, Version, Executor, State, Trigger, Agent, Concurrency, Unknown, Checksum, Row Version; Validation errors/warnings/preview. |
| 주요 조치 | 검증, Draft 저장, DRAFT→VALIDATED, VALIDATED→APPROVAL, PUBLISHED→RETIRED; Dependency 추가/삭제. |
| Button 활성 조건 | PUBLISHED/RETIRED는 입력 immutable. 자기 자신 Dependency 금지. 상태전환 사유>=5. Publish는 ADM 승인 실행에서만. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Validation 실패 시 저장/전환하지 않는다. UNKNOWN policy와 compensation ref를 Definition에 명시한다. |
| Partial/NACK/Drift | Validation 실패 시 저장/전환하지 않는다. UNKNOWN policy와 compensation ref를 Definition에 명시한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-job-packs/BatchJobPacksPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Job ID, Definition Version>=1, Job명, Owner, 설명; Executor, Trigger, Timezone, Misfire; Typed Parameter; Agent Pool/Zone/Concurrency/Timeout/Attempts/Backoff/Skip/Restartable/Unknown Policy/Compensation; Dependency; Alert/SLA; Checksum, 사유, 시행기간. / Version1, Executor SPRING_BATCH, state DRAFT, owner BAT, CRON `0 0 1 * * *`, Asia/Seoul, Misfire FAIL_CLOSED, pool DEFAULT, concurrency1, timeout3600s, restartable=true, attempts1, backoff0, multiplier1, Unknown FAIL_CLOSED, failure/missed notification=true.
- **실제 표시·컬럼**: Job, Version, Executor, State, Trigger, Agent, Concurrency, Unknown, Checksum, Row Version; Validation errors/warnings/preview.
- **실제 조치**: 검증, Draft 저장, DRAFT→VALIDATED, VALIDATED→APPROVAL, PUBLISHED→RETIRED; Dependency 추가/삭제.
- **권한·활성 조건**: PUBLISHED/RETIRED는 입력 immutable. 자기 자신 Dependency 금지. 상태전환 사유>=5. Publish는 ADM 승인 실행에서만.
- **Version·Approval·Idempotency**: expectedRowVersion + definitionVersion + checksum.
- **응답 유실·부분 실패·복구**: Validation 실패 시 저장/전환하지 않는다. UNKNOWN policy와 compensation ref를 Definition에 명시한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-job-packs` 진입 시 Menu `BATCH_JOB_PACKS`의 Server Session 권한과 Feature Flag `adm.route.batch-job-packs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Job ID, Definition Version>=1, Job명, Owner, 설명; Executor, Trigger, Timezone, Misfire; Typed Parameter; Agent Pool/Zone/Concurrency/Timeout/Attempts/Backoff/Skip/Restartable/Unknown Policy/Compensation; Dependency; Alert/SLA; Checksum, 사유, 시행기간. / Version1, Executor SPRING_BATCH, state DRAFT, owner BAT, CRON `0 0 1 * * *`, Asia/Seoul, Misfire FAIL_CLOSED, pool DEFAULT, concurrency1, timeout3600s, restartable=true, attempts1, backoff0, multiplier1, Unknown FAIL_CLOSED, failure/missed notification=true.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Job, Version, Executor, State, Trigger, Agent, Concurrency, Unknown, Checksum, Row Version; Validation errors/warnings/preview.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 검증, Draft 저장, DRAFT→VALIDATED, VALIDATED→APPROVAL, PUBLISHED→RETIRED; Dependency 추가/삭제.
5. 실행 전 활성 조건을 다시 검사한다: PUBLISHED/RETIRED는 입력 immutable. 자기 자신 Dependency 금지. 상태전환 사유>=5. Publish는 ADM 승인 실행에서만.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: expectedRowVersion + definitionVersion + checksum.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Validation 실패 시 저장/전환하지 않는다. UNKNOWN policy와 compensation ref를 Definition에 명시한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.33. Executions — `/batch-executions`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_EXECUTIONS` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-executions/BatchExecutionsPage.vue → BatchOperationsWorkbench.vue(mode=executions)` / `adm.route.batch-executions.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Executions의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 통합검색, Job ID, 실행상태(RUNNING/COMPLETED/FAILED/STOPPING/UNKNOWN), page size 20/50/100. |
| 검색 기본값 | size=50, page=0. |
| 주요 표시값 | Execution, Job, 상태, Worker/Server, 시작, Duration; 상세 Tab: 요약/Step Timeline/Parameter·Context/운영이력. Step Read/Write/Skip. |
| 주요 조치 | 재실행, 중지, 상세. |
| Button 활성 조건 | 재실행 HIGH approval+expectedVersion; 중지 CRITICAL approval+expectedVersion. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 중지 응답 손실은 UNKNOWN 가능. Recovery Center에서 결과 확인 후 후속조치. |
| Partial/NACK/Drift | 중지 응답 손실은 UNKNOWN 가능. Recovery Center에서 결과 확인 후 후속조치. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-executions/BatchExecutionsPage.vue → BatchOperationsWorkbench.vue(mode=executions)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 통합검색, Job ID, 실행상태(RUNNING/COMPLETED/FAILED/STOPPING/UNKNOWN), page size 20/50/100. / size=50, page=0.
- **실제 표시·컬럼**: Execution, Job, 상태, Worker/Server, 시작, Duration; 상세 Tab: 요약/Step Timeline/Parameter·Context/운영이력. Step Read/Write/Skip.
- **실제 조치**: 재실행, 중지, 상세.
- **권한·활성 조건**: 재실행 HIGH approval+expectedVersion; 중지 CRITICAL approval+expectedVersion.
- **Version·Approval·Idempotency**: selected row/detail의 rowVersion/version/expectedVersion.
- **응답 유실·부분 실패·복구**: 중지 응답 손실은 UNKNOWN 가능. Recovery Center에서 결과 확인 후 후속조치.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-executions` 진입 시 Menu `BATCH_EXECUTIONS`의 Server Session 권한과 Feature Flag `adm.route.batch-executions.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 통합검색, Job ID, 실행상태(RUNNING/COMPLETED/FAILED/STOPPING/UNKNOWN), page size 20/50/100. / size=50, page=0.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Execution, Job, 상태, Worker/Server, 시작, Duration; 상세 Tab: 요약/Step Timeline/Parameter·Context/운영이력. Step Read/Write/Skip.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 재실행, 중지, 상세.
5. 실행 전 활성 조건을 다시 검사한다: 재실행 HIGH approval+expectedVersion; 중지 CRITICAL approval+expectedVersion.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: selected row/detail의 rowVersion/version/expectedVersion.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 중지 응답 손실은 UNKNOWN 가능. Recovery Center에서 결과 확인 후 후속조치. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.34. Deployment / Rollback — `/batch-deployment`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_DEPLOYMENT` |
| Group / Risk | `batch` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-deployment/BatchDeploymentPage.vue → BatchOperationsWorkbench.vue(mode=deployment)` / `adm.route.batch-deployment.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Deployment / Rollback의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | 현재 Deployment workspace. |
| 주요 표시값 | Plan, Artifact, 상태, Target count, 갱신; 상세/원본/운영이력. |
| 주요 조치 | 조회/상세. 실제 배포 조치는 Runtime Command/계획 계약에 연결. |
| Button 활성 조건 | 화면의 shared workbench에서 직접 위험조치 Button은 없음. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 부분 실패/Drift는 성공 대상을 보존하고 실패 Target/Artifact 상태를 확인해 Rollback 계획으로 이동. |
| Partial/NACK/Drift | 부분 실패/Drift는 성공 대상을 보존하고 실패 Target/Artifact 상태를 확인해 Rollback 계획으로 이동. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-deployment/BatchDeploymentPage.vue → BatchOperationsWorkbench.vue(mode=deployment)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / 현재 Deployment workspace.
- **실제 표시·컬럼**: Plan, Artifact, 상태, Target count, 갱신; 상세/원본/운영이력.
- **실제 조치**: 조회/상세. 실제 배포 조치는 Runtime Command/계획 계약에 연결.
- **권한·활성 조건**: 화면의 shared workbench에서 직접 위험조치 Button은 없음.
- **Version·Approval·Idempotency**: Artifact version/hash, plan 상태.
- **응답 유실·부분 실패·복구**: 부분 실패/Drift는 성공 대상을 보존하고 실패 Target/Artifact 상태를 확인해 Rollback 계획으로 이동.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-deployment` 진입 시 Menu `BATCH_DEPLOYMENT`의 Server Session 권한과 Feature Flag `adm.route.batch-deployment.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / 현재 Deployment workspace.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Plan, Artifact, 상태, Target count, 갱신; 상세/원본/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/상세. 실제 배포 조치는 Runtime Command/계획 계약에 연결.
5. 실행 전 활성 조건을 다시 검사한다: 화면의 shared workbench에서 직접 위험조치 Button은 없음.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Artifact version/hash, plan 상태.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 부분 실패/Drift는 성공 대상을 보존하고 실패 Target/Artifact 상태를 확인해 Rollback 계획으로 이동. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.35. Recovery / Unknown — `/batch-recovery`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_RECOVERY` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-recovery/BatchRecoveryPage.vue → BatchOperationsWorkbench.vue(mode=recovery)` / `adm.route.batch-recovery.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Recovery / Unknown의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | Unknown/Ghost candidates workspace. |
| 주요 표시값 | 유형, Execution/Unknown, Job, 상태, 발생/Heartbeat; 상세/운영이력. |
| 주요 조치 | Unknown 실패 확정, Ghost 실패/폐기 확정. |
| Button 활성 조건 | CRITICAL approval. Ghost action은 expectedVersion 필요; Unknown resolve는 승인 필요. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Lease/Fencing/Worker를 재조회한 뒤 수동 판정. Blind retry 금지. |
| Partial/NACK/Drift | Lease/Fencing/Worker를 재조회한 뒤 수동 판정. Blind retry 금지. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-recovery/BatchRecoveryPage.vue → BatchOperationsWorkbench.vue(mode=recovery)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / Unknown/Ghost candidates workspace.
- **실제 표시·컬럼**: 유형, Execution/Unknown, Job, 상태, 발생/Heartbeat; 상세/운영이력.
- **실제 조치**: Unknown 실패 확정, Ghost 실패/폐기 확정.
- **권한·활성 조건**: CRITICAL approval. Ghost action은 expectedVersion 필요; Unknown resolve는 승인 필요.
- **Version·Approval·Idempotency**: Ghost의 current version/heartbeat/fencing을 사용.
- **응답 유실·부분 실패·복구**: Lease/Fencing/Worker를 재조회한 뒤 수동 판정. Blind retry 금지.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-recovery` 진입 시 Menu `BATCH_RECOVERY`의 Server Session 권한과 Feature Flag `adm.route.batch-recovery.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / Unknown/Ghost candidates workspace.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 유형, Execution/Unknown, Job, 상태, 발생/Heartbeat; 상세/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: Unknown 실패 확정, Ghost 실패/폐기 확정.
5. 실행 전 활성 조건을 다시 검사한다: CRITICAL approval. Ghost action은 expectedVersion 필요; Unknown resolve는 승인 필요.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Ghost의 current version/heartbeat/fencing을 사용.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Lease/Fencing/Worker를 재조회한 뒤 수동 판정. Blind retry 금지. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.36. Lease / Fencing — `/batch-leases`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_LEASES` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-leases/BatchLeasesPage.vue → BatchOperationsWorkbench.vue(mode=leases)` / `adm.route.batch-leases.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Lease / Fencing의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | Lock/Lease workspace. |
| 주요 표시값 | Lock Key, Owner, 상태, Expires, Fencing; 상세/운영이력. |
| 주요 조치 | Lock 강제 해제. |
| Button 활성 조건 | CRITICAL approval+expectedVersion. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 실행이 살아 있으면 강제 해제가 중복실행을 만들 수 있으므로 Worker/Heartbeat를 대사 후 실행. |
| Partial/NACK/Drift | 실행이 살아 있으면 강제 해제가 중복실행을 만들 수 있으므로 Worker/Heartbeat를 대사 후 실행. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-leases/BatchLeasesPage.vue → BatchOperationsWorkbench.vue(mode=leases)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / Lock/Lease workspace.
- **실제 표시·컬럼**: Lock Key, Owner, 상태, Expires, Fencing; 상세/운영이력.
- **실제 조치**: Lock 강제 해제.
- **권한·활성 조건**: CRITICAL approval+expectedVersion.
- **Version·Approval·Idempotency**: Lock version/fencing token.
- **응답 유실·부분 실패·복구**: 실행이 살아 있으면 강제 해제가 중복실행을 만들 수 있으므로 Worker/Heartbeat를 대사 후 실행.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-leases` 진입 시 Menu `BATCH_LEASES`의 Server Session 권한과 Feature Flag `adm.route.batch-leases.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / Lock/Lease workspace.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Lock Key, Owner, 상태, Expires, Fencing; 상세/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: Lock 강제 해제.
5. 실행 전 활성 조건을 다시 검사한다: CRITICAL approval+expectedVersion.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Lock version/fencing token.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 실행이 살아 있으면 강제 해제가 중복실행을 만들 수 있으므로 Worker/Heartbeat를 대사 후 실행. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.37. Batch Alerts — `/batch-alerts`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_ALERTS` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-alerts/BatchAlertsPage.vue → BatchOperationsWorkbench.vue(mode=alerts)` / `adm.route.batch-alerts.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Batch Alerts의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | Alert/Broker backlog workspace. |
| 주요 표시값 | 유형, ID, 상태, Target, 발생시각; 상세/원본/운영이력. |
| 주요 조치 | 조회/상세. |
| Button 활성 조건 | 읽기 중심. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Incident/Recovery route로 Deep-dive하여 실제 mutation을 수행. |
| Partial/NACK/Drift | Incident/Recovery route로 Deep-dive하여 실제 mutation을 수행. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-alerts/BatchAlertsPage.vue → BatchOperationsWorkbench.vue(mode=alerts)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / Alert/Broker backlog workspace.
- **실제 표시·컬럼**: 유형, ID, 상태, Target, 발생시각; 상세/원본/운영이력.
- **실제 조치**: 조회/상세.
- **권한·활성 조건**: 읽기 중심.
- **Version·Approval·Idempotency**: Unknown/DLQ/Outbox operation identifiers.
- **응답 유실·부분 실패·복구**: Incident/Recovery route로 Deep-dive하여 실제 mutation을 수행.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-alerts` 진입 시 Menu `BATCH_ALERTS`의 Server Session 권한과 Feature Flag `adm.route.batch-alerts.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / Alert/Broker backlog workspace.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 유형, ID, 상태, Target, 발생시각; 상세/원본/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/상세.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 중심.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Unknown/DLQ/Outbox operation identifiers.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Incident/Recovery route로 Deep-dive하여 실제 mutation을 수행. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.38. Audit / Evidence — `/batch-audit`

| 항목 | 내용 |
|---|---|
| Menu | `BATCH_AUDIT` |
| Group / Risk | `monitoring` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/batch-audit/BatchAuditEvidencePage.vue → BatchOperationsWorkbench.vue(mode=audit)` / `adm.route.batch-audit.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Audit / Evidence의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | Operation audit workspace. |
| 주요 표시값 | 시각, 조치, 대상, 결과, 사유(masked); 상세/운영이력. |
| 주요 조치 | 조회/상세. |
| Button 활성 조건 | 읽기 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 위험조치 Request/Approval/Result와 불변 Audit를 연결해 증적 판정. |
| Partial/NACK/Drift | 위험조치 Request/Approval/Result와 불변 Audit를 연결해 증적 판정. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/batch-audit/BatchAuditEvidencePage.vue → BatchOperationsWorkbench.vue(mode=audit)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / Operation audit workspace.
- **실제 표시·컬럼**: 시각, 조치, 대상, 결과, 사유(masked); 상세/운영이력.
- **실제 조치**: 조회/상세.
- **권한·활성 조건**: 읽기 전용.
- **Version·Approval·Idempotency**: Audit evidence의 operation/target 식별자를 기준으로 조회.
- **응답 유실·부분 실패·복구**: 위험조치 Request/Approval/Result와 불변 Audit를 연결해 증적 판정.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/batch-audit` 진입 시 Menu `BATCH_AUDIT`의 Server Session 권한과 Feature Flag `adm.route.batch-audit.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / Operation audit workspace.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 시각, 조치, 대상, 결과, 사유(masked); 상세/운영이력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/상세.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Audit evidence의 operation/target 식별자를 기준으로 조회.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 위험조치 Request/Approval/Result와 불변 Audit를 연결해 증적 판정. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.39. Agent / Worker — `/workers`

| 항목 | 내용 |
|---|---|
| Menu | `WORKER` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/workers/WorkersPage.vue` / `adm.route.workers.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Agent / Worker의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 검색 없음. |
| 검색 기본값 | Batch load 결과. |
| 주요 표시값 | Workers/Locks/Ghost KPI; Worker, Instance, Status, Heartbeat, Lease. |
| 주요 조치 | 새로고침, Runtime Instance 조회. |
| Button 활성 조건 | 조회 전용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Ghost/Lock 수치가 있으면 Batch Recovery/Lease 전용 화면에서 조치. |
| Partial/NACK/Drift | Ghost/Lock 수치가 있으면 Batch Recovery/Lease 전용 화면에서 조치. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/workers/WorkersPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 검색 없음. / Batch load 결과.
- **실제 표시·컬럼**: Workers/Locks/Ghost KPI; Worker, Instance, Status, Heartbeat, Lease.
- **실제 조치**: 새로고침, Runtime Instance 조회.
- **권한·활성 조건**: 조회 전용.
- **Version·Approval·Idempotency**: Lease/heartbeat를 읽는다.
- **응답 유실·부분 실패·복구**: Ghost/Lock 수치가 있으면 Batch Recovery/Lease 전용 화면에서 조치.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/workers` 진입 시 Menu `WORKER`의 Server Session 권한과 Feature Flag `adm.route.workers.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 검색 없음. / Batch load 결과.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Workers/Locks/Ghost KPI; Worker, Instance, Status, Heartbeat, Lease.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, Runtime Instance 조회.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Lease/heartbeat를 읽는다.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Ghost/Lock 수치가 있으면 Batch Recovery/Lease 전용 화면에서 조치. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.40. 다운로드 — `/downloads`

| 항목 | 내용 |
|---|---|
| Menu | `DOWNLOAD` |
| Group / Risk | `integration` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/downloads/DownloadsPage.vue` / `adm.route.downloads.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 다운로드의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 다운로드 유형(TRANSACTION_LOGS/ERROR_LOGS/BATCH_EXECUTIONS/NOTIFICATION_DELIVERY_LOGS), 대상화면, 시작/종료, 거래ID, TraceID, JobID, 건수, 사유. |
| 검색 기본값 | 다운로드 유형 select 중 하나. |
| 주요 표시값 | Download 정책/감사 결과 구조화 상세. |
| 주요 조치 | 조회, CSV 다운로드. |
| Button 활성 조건 | 다운로드 정책과 서버 권한/감사 사유 적용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 다운로드 실패 시 같은 조건의 감사 이력부터 확인하고 대용량 결과는 제한 조건을 좁힌다. |
| Partial/NACK/Drift | 다운로드 실패 시 같은 조건의 감사 이력부터 확인하고 대용량 결과는 제한 조건을 좁힌다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/downloads/DownloadsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 다운로드 유형(TRANSACTION_LOGS/ERROR_LOGS/BATCH_EXECUTIONS/NOTIFICATION_DELIVERY_LOGS), 대상화면, 시작/종료, 거래ID, TraceID, JobID, 건수, 사유. / 다운로드 유형 select 중 하나.
- **실제 표시·컬럼**: Download 정책/감사 결과 구조화 상세.
- **실제 조치**: 조회, CSV 다운로드.
- **권한·활성 조건**: 다운로드 정책과 서버 권한/감사 사유 적용.
- **Version·Approval·Idempotency**: Artifact 감사 ID/조건으로 추적.
- **응답 유실·부분 실패·복구**: 다운로드 실패 시 같은 조건의 감사 이력부터 확인하고 대용량 결과는 제한 조건을 좁힌다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/downloads` 진입 시 Menu `DOWNLOAD`의 Server Session 권한과 Feature Flag `adm.route.downloads.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 다운로드 유형(TRANSACTION_LOGS/ERROR_LOGS/BATCH_EXECUTIONS/NOTIFICATION_DELIVERY_LOGS), 대상화면, 시작/종료, 거래ID, TraceID, JobID, 건수, 사유. / 다운로드 유형 select 중 하나.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Download 정책/감사 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, CSV 다운로드.
5. 실행 전 활성 조건을 다시 검사한다: 다운로드 정책과 서버 권한/감사 사유 적용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Artifact 감사 ID/조건으로 추적.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 다운로드 실패 시 같은 조건의 감사 이력부터 확인하고 대용량 결과는 제한 조건을 좁힌다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.41. 대량파일 Job — `/file-jobs`

| 항목 | 내용 |
|---|---|
| Menu | `FILE_JOB` |
| Group / Risk | `batch` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/file-jobs/FileJobsPage.vue` / `adm.route.file-jobs.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 대량파일 Job의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Upload: Operation ID, Template, Version>=1, Format CSV/XLSX, Dry-run, 파일, 사유. Control: 승인ID(Apply/Rollback), 사유, 영향확인. Resolve: Row, Resolution, Business Key, 조건부 Rollback Token, 승인ID, 사유, 외부결과 확인. |
| 검색 기본값 | Upload operationId UUID, template ADM_NOTIFICATION_RULE_IMPORT, version1, CSV, dryRun=true, reason 기본 문구. |
| 주요 표시값 | Job, Template, State, Rows(success/total/fail), Checksum, 요청자, 관리; 상세 Operation/RequestHash/DryRun/Retention/Control/Audit/Error; Row/State/BusinessKey/Error. |
| 주요 조치 | Upload, Apply, Retry, Cancel, Rollback, UNKNOWN 결과확정, Artifact, 직접 상세/운영. |
| Button 활성 조건 | 상태별: Apply VALIDATED/READY_TO_APPLY; Retry FAILED/PARTIAL_FAILED이면서 ROLLBACK_* 오류가 아닌 경우; Cancel RECEIVED/VALIDATED/READY_TO_APPLY; Rollback COMPLETED/PARTIAL_FAILED+supported; Resolve UNKNOWN_RESULT; Artifact !EXPIRED. Button별 permission. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | DISPATCHING/UNKNOWN_RESULT/ROLLBACK_DISPATCHING/ROLLBACK_UNKNOWN_RESULT 행을 외부·DB 결과와 대사 후 `SIDE_EFFECT_NOT_APPLIED`, `SIDE_EFFECT_APPLIED`, `SIDE_EFFECT_COMPENSATED` 중 증거에 맞는 값으로 확정. Apply/Rollback은 승인 필요. |
| Partial/NACK/Drift | DISPATCHING/UNKNOWN_RESULT/ROLLBACK_DISPATCHING/ROLLBACK_UNKNOWN_RESULT 행을 외부·DB 결과와 대사 후 `SIDE_EFFECT_NOT_APPLIED`, `SIDE_EFFECT_APPLIED`, `SIDE_EFFECT_COMPENSATED` 중 증거에 맞는 값으로 확정. Apply/Rollback은 승인 필요. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/file-jobs/FileJobsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Upload: Operation ID, Template, Version>=1, Format CSV/XLSX, Dry-run, 파일, 사유. Control: 승인ID(Apply/Rollback), 사유, 영향확인. Resolve: Row, Resolution, Business Key, 조건부 Rollback Token, 승인ID, 사유, 외부결과 확인. / Upload operationId UUID, template ADM_NOTIFICATION_RULE_IMPORT, version1, CSV, dryRun=true, reason 기본 문구.
- **실제 표시·컬럼**: Job, Template, State, Rows(success/total/fail), Checksum, 요청자, 관리; 상세 Operation/RequestHash/DryRun/Retention/Control/Audit/Error; Row/State/BusinessKey/Error.
- **실제 조치**: Upload, Apply, Retry, Cancel, Rollback, UNKNOWN 결과확정, Artifact, 직접 상세/운영.
- **권한·활성 조건**: 상태별: Apply VALIDATED/READY_TO_APPLY; Retry FAILED/PARTIAL_FAILED이면서 ROLLBACK_* 오류가 아닌 경우; Cancel RECEIVED/VALIDATED/READY_TO_APPLY; Rollback COMPLETED/PARTIAL_FAILED+supported; Resolve UNKNOWN_RESULT; Artifact !EXPIRED. Button별 permission.
- **Version·Approval·Idempotency**: Operation ID/Request Hash/Approval/Row 상태를 사용. UNKNOWN resolve는 row 단위.
- **응답 유실·부분 실패·복구**: DISPATCHING/UNKNOWN_RESULT/ROLLBACK_DISPATCHING/ROLLBACK_UNKNOWN_RESULT 행을 외부·DB 결과와 대사 후 `SIDE_EFFECT_NOT_APPLIED`, `SIDE_EFFECT_APPLIED`, `SIDE_EFFECT_COMPENSATED` 중 증거에 맞는 값으로 확정. Apply/Rollback은 승인 필요.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/file-jobs` 진입 시 Menu `FILE_JOB`의 Server Session 권한과 Feature Flag `adm.route.file-jobs.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Upload: Operation ID, Template, Version>=1, Format CSV/XLSX, Dry-run, 파일, 사유. Control: 승인ID(Apply/Rollback), 사유, 영향확인. Resolve: Row, Resolution, Business Key, 조건부 Rollback Token, 승인ID, 사유, 외부결과 확인. / Upload operationId UUID, template ADM_NOTIFICATION_RULE_IMPORT, version1, CSV, dryRun=true, reason 기본 문구.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Job, Template, State, Rows(success/total/fail), Checksum, 요청자, 관리; 상세 Operation/RequestHash/DryRun/Retention/Control/Audit/Error; Row/State/BusinessKey/Error.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: Upload, Apply, Retry, Cancel, Rollback, UNKNOWN 결과확정, Artifact, 직접 상세/운영.
5. 실행 전 활성 조건을 다시 검사한다: 상태별: Apply VALIDATED/READY_TO_APPLY; Retry FAILED/PARTIAL_FAILED이면서 ROLLBACK_* 오류가 아닌 경우; Cancel RECEIVED/VALIDATED/READY_TO_APPLY; Rollback COMPLETED/PARTIAL_FAILED+supported; Resolve UNKNOWN_RESULT; Artifact !EXPIRED. Button별 permission.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Operation ID/Request Hash/Approval/Row 상태를 사용. UNKNOWN resolve는 row 단위.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: DISPATCHING/UNKNOWN_RESULT/ROLLBACK_DISPATCHING/ROLLBACK_UNKNOWN_RESULT 행을 외부·DB 결과와 대사 후 `SIDE_EFFECT_NOT_APPLIED`, `SIDE_EFFECT_APPLIED`, `SIDE_EFFECT_COMPENSATED` 중 증거에 맞는 값으로 확정. Apply/Rollback은 승인 필요. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.42. 전문·Protocol Message — `/messages`

| 항목 | 내용 |
|---|---|
| Menu | `MESSAGE` |
| Group / Risk | `integration` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operations/ProtocolMessageWorkbenchPage.vue` / `adm.route.messages.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 전문·Protocol Message의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Transaction ID, 최대 Event 20/50/100; Message Code<=80, Locale<=20, Format FIXED/INDEXED/NAMED, Use Y/N, External/Internal Message, Parameter Count0~99/Sample, Description, 사유>=5. |
| 검색 기본값 | limit=100, locale ko-KR, format FIXED, useYn=Y, parameterCount=0. |
| 주요 표시값 | Registry ID/Code/Locale/External/Use; Protocol Trace 시각/구간/Protocol·Channel/External/Status/Duration; 마스킹 상세. |
| 주요 조치 | 신규, 새로고침, 전문 흐름 추적, 등록/수정 저장, 비활성. |
| Button 활성 조건 | Trace는 transactionId 필요. 비활성은 기존 messageId + 영향확인 checkbox + valid reason. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 동일 Code/Locale Consumer 영향 확인 후 비활성. Trace는 transactionId로 재조회. |
| Partial/NACK/Drift | 동일 Code/Locale Consumer 영향 확인 후 비활성. Trace는 transactionId로 재조회. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operations/ProtocolMessageWorkbenchPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Transaction ID, 최대 Event 20/50/100; Message Code<=80, Locale<=20, Format FIXED/INDEXED/NAMED, Use Y/N, External/Internal Message, Parameter Count0~99/Sample, Description, 사유>=5. / limit=100, locale ko-KR, format FIXED, useYn=Y, parameterCount=0.
- **실제 표시·컬럼**: Registry ID/Code/Locale/External/Use; Protocol Trace 시각/구간/Protocol·Channel/External/Status/Duration; 마스킹 상세.
- **실제 조치**: 신규, 새로고침, 전문 흐름 추적, 등록/수정 저장, 비활성.
- **권한·활성 조건**: Trace는 transactionId 필요. 비활성은 기존 messageId + 영향확인 checkbox + valid reason.
- **Version·Approval·Idempotency**: Registry row 식별자 기반.
- **응답 유실·부분 실패·복구**: 동일 Code/Locale Consumer 영향 확인 후 비활성. Trace는 transactionId로 재조회.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/messages` 진입 시 Menu `MESSAGE`의 Server Session 권한과 Feature Flag `adm.route.messages.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Transaction ID, 최대 Event 20/50/100; Message Code<=80, Locale<=20, Format FIXED/INDEXED/NAMED, Use Y/N, External/Internal Message, Parameter Count0~99/Sample, Description, 사유>=5. / limit=100, locale ko-KR, format FIXED, useYn=Y, parameterCount=0.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Registry ID/Code/Locale/External/Use; Protocol Trace 시각/구간/Protocol·Channel/External/Status/Duration; 마스킹 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 신규, 새로고침, 전문 흐름 추적, 등록/수정 저장, 비활성.
5. 실행 전 활성 조건을 다시 검사한다: Trace는 transactionId 필요. 비활성은 기존 messageId + 영향확인 checkbox + valid reason.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Registry row 식별자 기반.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 동일 Code/Locale Consumer 영향 확인 후 비활성. Trace는 transactionId로 재조회. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.43. 코드 — `/codes`

| 항목 | 내용 |
|---|---|
| Menu | `CODE` |
| Group / Risk | `framework` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/codes/CodesPage.vue` / `adm.route.codes.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 코드의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Code ID, Parent ID, Code Key, Code Value, 설명, 사유. |
| 검색 기본값 | 공백/0 입력 상태에서 사용자가 대상 지정. |
| 주요 표시값 | Code 처리 결과 구조화 상세. |
| 주요 조치 | 조회, 등록, 수정, 상세, 비활성. |
| Button 활성 조건 | 등록/수정/비활성은 CODE write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 계층 Parent/Key 영향 확인 후 변경하고 Consumer 조회 결과를 확인. |
| Partial/NACK/Drift | 계층 Parent/Key 영향 확인 후 변경하고 Consumer 조회 결과를 확인. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/codes/CodesPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Code ID, Parent ID, Code Key, Code Value, 설명, 사유. / 공백/0 입력 상태에서 사용자가 대상 지정.
- **실제 표시·컬럼**: Code 처리 결과 구조화 상세.
- **실제 조치**: 조회, 등록, 수정, 상세, 비활성.
- **권한·활성 조건**: 등록/수정/비활성은 CODE write.
- **Version·Approval·Idempotency**: Owner 상세 결과 재조회.
- **응답 유실·부분 실패·복구**: 계층 Parent/Key 영향 확인 후 변경하고 Consumer 조회 결과를 확인.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/codes` 진입 시 Menu `CODE`의 Server Session 권한과 Feature Flag `adm.route.codes.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Code ID, Parent ID, Code Key, Code Value, 설명, 사유. / 공백/0 입력 상태에서 사용자가 대상 지정.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Code 처리 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 등록, 수정, 상세, 비활성.
5. 실행 전 활성 조건을 다시 검사한다: 등록/수정/비활성은 CODE write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Owner 상세 결과 재조회.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 계층 Parent/Key 영향 확인 후 변경하고 Consumer 조회 결과를 확인. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.44. Gateway 대시보드 — `/gateway-dashboard`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_DASHBOARD` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=dashboard)` / `adm.route.gateway-dashboard.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 대시보드의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 환경(전체/DEV/TEST/PROD), Service ID, Route ID. |
| 검색 기본값 | Event poll 15s 자동 활성. 전체 환경 기본. |
| 주요 표시값 | Capability 상태; TPS60s, Success/Error, P95/P99, Drift, Open Circuit, Cert<=30d, Spool Backlog, Failed Tests24h KPI; 현재 필터/경고. |
| 주요 조치 | 자동갱신 시작/중지, 새로고침, 조회. |
| Button 활성 조건 | 조회 전용 dashboard mode. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Capability unavailable/운영 warning을 먼저 확인하고 해당 Gateway 전용 Tab으로 이동. |
| Partial/NACK/Drift | Capability unavailable/운영 warning을 먼저 확인하고 해당 Gateway 전용 Tab으로 이동. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=dashboard)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 환경(전체/DEV/TEST/PROD), Service ID, Route ID. / Event poll 15s 자동 활성. 전체 환경 기본.
- **실제 표시·컬럼**: Capability 상태; TPS60s, Success/Error, P95/P99, Drift, Open Circuit, Cert<=30d, Spool Backlog, Failed Tests24h KPI; 현재 필터/경고.
- **실제 조치**: 자동갱신 시작/중지, 새로고침, 조회.
- **권한·활성 조건**: 조회 전용 dashboard mode.
- **Version·Approval·Idempotency**: Source instance/generatedAt 및 drift를 표시.
- **응답 유실·부분 실패·복구**: Capability unavailable/운영 warning을 먼저 확인하고 해당 Gateway 전용 Tab으로 이동.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-dashboard` 진입 시 Menu `GATEWAY_DASHBOARD`의 Server Session 권한과 Feature Flag `adm.route.gateway-dashboard.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 환경(전체/DEV/TEST/PROD), Service ID, Route ID. / Event poll 15s 자동 활성. 전체 환경 기본.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Capability 상태; TPS60s, Success/Error, P95/P99, Drift, Open Circuit, Cert<=30d, Spool Backlog, Failed Tests24h KPI; 현재 필터/경고.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 자동갱신 시작/중지, 새로고침, 조회.
5. 실행 전 활성 조건을 다시 검사한다: 조회 전용 dashboard mode.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Source instance/generatedAt 및 drift를 표시.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Capability unavailable/운영 warning을 먼저 확인하고 해당 Gateway 전용 Tab으로 이동. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.45. Gateway 연동 서버 — `/gateway-servers`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_SERVERS` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=servers)` / `adm.route.gateway-servers.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 연동 서버의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 환경, Service ID, Route ID. |
| 검색 기본값 | 공통 필터. |
| 주요 표시값 | 환경, Service, Endpoint, Protocol, Group, Member, 상태. |
| 주요 조치 | 조회/새로고침. |
| Button 활성 조건 | 읽기 전용 servers mode. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Endpoint/Group 불일치는 Service Registry와 Group 상세에서 대사. |
| Partial/NACK/Drift | Endpoint/Group 불일치는 Service Registry와 Group 상세에서 대사. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=servers)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 환경, Service ID, Route ID. / 공통 필터.
- **실제 표시·컬럼**: 환경, Service, Endpoint, Protocol, Group, Member, 상태.
- **실제 조치**: 조회/새로고침.
- **권한·활성 조건**: 읽기 전용 servers mode.
- **Version·Approval·Idempotency**: Server Group 상태와 Service Registry 연결.
- **응답 유실·부분 실패·복구**: Endpoint/Group 불일치는 Service Registry와 Group 상세에서 대사.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-servers` 진입 시 Menu `GATEWAY_SERVERS`의 Server Session 권한과 Feature Flag `adm.route.gateway-servers.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 환경, Service ID, Route ID. / 공통 필터.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 환경, Service, Endpoint, Protocol, Group, Member, 상태.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/새로고침.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 전용 servers mode.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Server Group 상태와 Service Registry 연결.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Endpoint/Group 불일치는 Service Registry와 Group 상세에서 대사. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.46. Gateway 서버 그룹 — `/gateway-groups`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_GROUPS` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=groups)` / `adm.route.gateway-groups.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 서버 그룹의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Group ID/명, 환경, Service ID, Endpoint, Target Protocol, LB, 조건부 Hash Key, Health/Failover Group; Member instanceId/weight1~10000/priority>=0/canary0~100/enabled; reason>=5. |
| 검색 기본값 | 환경 선택, Member Diff preview. |
| 주요 표시값 | 환경, 그룹, Service/Endpoint, Protocol, LB, 상태, Member, Version; Member Health/Fencing. |
| 주요 조치 | 새 그룹, 저장, Member 추가/제거, 기존 Group 폐기 요청. |
| Button 활성 조건 | 기존 Group은 version>0. 변경사유>=5. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 저장 후 Member health/fencing과 apply status를 확인. 일부 Member 실패 시 성공 Member 보존. |
| Partial/NACK/Drift | 저장 후 Member health/fencing과 apply status를 확인. 일부 Member 실패 시 성공 Member 보존. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=groups)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Group ID/명, 환경, Service ID, Endpoint, Target Protocol, LB, 조건부 Hash Key, Health/Failover Group; Member instanceId/weight1~10000/priority>=0/canary0~100/enabled; reason>=5. / 환경 선택, Member Diff preview.
- **실제 표시·컬럼**: 환경, 그룹, Service/Endpoint, Protocol, LB, 상태, Member, Version; Member Health/Fencing.
- **실제 조치**: 새 그룹, 저장, Member 추가/제거, 기존 Group 폐기 요청.
- **권한·활성 조건**: 기존 Group은 version>0. 변경사유>=5.
- **Version·Approval·Idempotency**: Group row version + Member fencing; diff preview.
- **응답 유실·부분 실패·복구**: 저장 후 Member health/fencing과 apply status를 확인. 일부 Member 실패 시 성공 Member 보존.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-groups` 진입 시 Menu `GATEWAY_GROUPS`의 Server Session 권한과 Feature Flag `adm.route.gateway-groups.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Group ID/명, 환경, Service ID, Endpoint, Target Protocol, LB, 조건부 Hash Key, Health/Failover Group; Member instanceId/weight1~10000/priority>=0/canary0~100/enabled; reason>=5. / 환경 선택, Member Diff preview.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 환경, 그룹, Service/Endpoint, Protocol, LB, 상태, Member, Version; Member Health/Fencing.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새 그룹, 저장, Member 추가/제거, 기존 Group 폐기 요청.
5. 실행 전 활성 조건을 다시 검사한다: 기존 Group은 version>0. 변경사유>=5.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Group row version + Member fencing; diff preview.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 저장 후 Member health/fencing과 apply status를 확인. 일부 Member 실패 시 성공 Member 보존. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.47. Gateway 경로·라우팅 — `/gateway-routes`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_ROUTES` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=bindings)` / `adm.route.gateway-routes.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 경로·라우팅의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Binding ID/Route ID/환경/Host Pattern/Ingress Path/Target Path/Method*/GET/POST/PUT/DELETE/API Version/Route Version/Service/Group/Ingress·Target Protocol/Connect·Response·Overall Timeout>=1/Retry>=0/Idempotent/Gateway·Direct 허용/TLS/AuthN/AuthZ/Header/RateLimit/Health Policy/reason>=5. |
| 검색 기본값 | Default Deny Draft 흐름. |
| 주요 표시값 | 환경, Route, Server Group, Route Version, Gateway, Direct, 상태, Row Version; 적용 Preview. |
| 주요 조치 | 새 Binding, Draft 저장, 검증 전환, 폐기 요청. |
| Button 활성 조건 | 기존 binding version>0일 때 검증/폐기. 변경사유 필수. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Draft→검증 후 ACK/Drift 확인. 실패 Instance만 원인 대사하고 LKG/Rollback 정책과 연결. |
| Partial/NACK/Drift | Draft→검증 후 ACK/Drift 확인. 실패 Instance만 원인 대사하고 LKG/Rollback 정책과 연결. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=bindings)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Binding ID/Route ID/환경/Host Pattern/Ingress Path/Target Path/Method*/GET/POST/PUT/DELETE/API Version/Route Version/Service/Group/Ingress·Target Protocol/Connect·Response·Overall Timeout>=1/Retry>=0/Idempotent/Gateway·Direct 허용/TLS/AuthN/AuthZ/Header/RateLimit/Health Policy/reason>=5. / Default Deny Draft 흐름.
- **실제 표시·컬럼**: 환경, Route, Server Group, Route Version, Gateway, Direct, 상태, Row Version; 적용 Preview.
- **실제 조치**: 새 Binding, Draft 저장, 검증 전환, 폐기 요청.
- **권한·활성 조건**: 기존 binding version>0일 때 검증/폐기. 변경사유 필수.
- **Version·Approval·Idempotency**: Row Version + Route Version. 운영 활성화는 승인/CAS/Audit 경계.
- **응답 유실·부분 실패·복구**: Draft→검증 후 ACK/Drift 확인. 실패 Instance만 원인 대사하고 LKG/Rollback 정책과 연결.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-routes` 진입 시 Menu `GATEWAY_ROUTES`의 Server Session 권한과 Feature Flag `adm.route.gateway-routes.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Binding ID/Route ID/환경/Host Pattern/Ingress Path/Target Path/Method*/GET/POST/PUT/DELETE/API Version/Route Version/Service/Group/Ingress·Target Protocol/Connect·Response·Overall Timeout>=1/Retry>=0/Idempotent/Gateway·Direct 허용/TLS/AuthN/AuthZ/Header/RateLimit/Health Policy/reason>=5. / Default Deny Draft 흐름.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 환경, Route, Server Group, Route Version, Gateway, Direct, 상태, Row Version; 적용 Preview.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새 Binding, Draft 저장, 검증 전환, 폐기 요청.
5. 실행 전 활성 조건을 다시 검사한다: 기존 binding version>0일 때 검증/폐기. 변경사유 필수.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Row Version + Route Version. 운영 활성화는 승인/CAS/Audit 경계.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Draft→검증 후 ACK/Drift 확인. 실패 Instance만 원인 대사하고 LKG/Rollback 정책과 연결. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.48. Gateway 보안·제한 — `/gateway-security`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_SECURITY` |
| Group / Risk | `online` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=security)` / `adm.route.gateway-security.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 보안·제한의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 별도 편집 입력 없음. 현재 Binding/정책을 조회. |
| 검색 기본값 | Default Deny. |
| 주요 표시값 | Default Deny, Retry Safety, 관리 API 보호, 변경 통제 원칙. |
| 주요 조치 | 조회/새로고침. |
| Button 활성 조건 | 관리 API/ADM/BAT/Actuator/Internal은 기본 외부 공개 제외. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Retry는 idempotent Route에만. 비멱등 Route 결과불명은 대사 후 결정. |
| Partial/NACK/Drift | Retry는 idempotent Route에만. 비멱등 Route 결과불명은 대사 후 결정. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=security)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 별도 편집 입력 없음. 현재 Binding/정책을 조회. / Default Deny.
- **실제 표시·컬럼**: Default Deny, Retry Safety, 관리 API 보호, 변경 통제 원칙.
- **실제 조치**: 조회/새로고침.
- **권한·활성 조건**: 관리 API/ADM/BAT/Actuator/Internal은 기본 외부 공개 제외.
- **Version·Approval·Idempotency**: 변경은 사유+승인 ID+CAS Version+Audit 요구.
- **응답 유실·부분 실패·복구**: Retry는 idempotent Route에만. 비멱등 Route 결과불명은 대사 후 결정.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-security` 진입 시 Menu `GATEWAY_SECURITY`의 Server Session 권한과 Feature Flag `adm.route.gateway-security.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 별도 편집 입력 없음. 현재 Binding/정책을 조회. / Default Deny.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Default Deny, Retry Safety, 관리 API 보호, 변경 통제 원칙.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회/새로고침.
5. 실행 전 활성 조건을 다시 검사한다: 관리 API/ADM/BAT/Actuator/Internal은 기본 외부 공개 제외.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 변경은 사유+승인 ID+CAS Version+Audit 요구.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Retry는 idempotent Route에만. 비멱등 Route 결과불명은 대사 후 결정. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.49. Gateway Health·연결시험 — `/gateway-health`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_HEALTH` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=health)` / `adm.route.gateway-health.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway Health·연결시험의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 환경/Service/Route; 선택 Binding, Test Type(capability catalog), 연결시험 사유. |
| 검색 기본값 | testReason=`Gateway 연결 상태 검증`; auto poll15s. |
| 주요 표시값 | Apply ACK/Drift; Connection Test Type/Gateway·Target/Status/Failure Stage/Duration/Trace; Operation status/version. |
| 주요 조치 | 연결시험 실행, Operation 상태 확인, 취소, 재검증. |
| Button 활성 조건 | 실행은 selectedBindingId+not submitting. Cancel은 REQUESTED/PENDING/RUNNING, Revalidate는 COMPLETED/FAILED/CANCELLED/EXPIRED. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 비동기 Operation 상태를 조회한다. Failure Stage/Trace로 원인 분리 후 재검증 조건 충족 시 실행. |
| Partial/NACK/Drift | 비동기 Operation 상태를 조회한다. Failure Stage/Trace로 원인 분리 후 재검증 조건 충족 시 실행. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=health)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 환경/Service/Route; 선택 Binding, Test Type(capability catalog), 연결시험 사유. / testReason=`Gateway 연결 상태 검증`; auto poll15s.
- **실제 표시·컬럼**: Apply ACK/Drift; Connection Test Type/Gateway·Target/Status/Failure Stage/Duration/Trace; Operation status/version.
- **실제 조치**: 연결시험 실행, Operation 상태 확인, 취소, 재검증.
- **권한·활성 조건**: 실행은 selectedBindingId+not submitting. Cancel은 REQUESTED/PENDING/RUNNING, Revalidate는 COMPLETED/FAILED/CANCELLED/EXPIRED.
- **Version·Approval·Idempotency**: Connection Test Operation version 및 Apply expected/applied version.
- **응답 유실·부분 실패·복구**: 비동기 Operation 상태를 조회한다. Failure Stage/Trace로 원인 분리 후 재검증 조건 충족 시 실행.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-health` 진입 시 Menu `GATEWAY_HEALTH`의 Server Session 권한과 Feature Flag `adm.route.gateway-health.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 환경/Service/Route; 선택 Binding, Test Type(capability catalog), 연결시험 사유. / testReason=`Gateway 연결 상태 검증`; auto poll15s.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Apply ACK/Drift; Connection Test Type/Gateway·Target/Status/Failure Stage/Duration/Trace; Operation status/version.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 연결시험 실행, Operation 상태 확인, 취소, 재검증.
5. 실행 전 활성 조건을 다시 검사한다: 실행은 selectedBindingId+not submitting. Cancel은 REQUESTED/PENDING/RUNNING, Revalidate는 COMPLETED/FAILED/CANCELLED/EXPIRED.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Connection Test Operation version 및 Apply expected/applied version.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 비동기 Operation 상태를 조회한다. Failure Stage/Trace로 원인 분리 후 재검증 조건 충족 시 실행. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.50. Gateway 거래 조회 — `/gateway-transactions`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_TRANSACTIONS` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=transactions)` / `adm.route.gateway-transactions.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 거래 조회의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 공통 필터 + Transaction ID. |
| 검색 기본값 | 공통 filters. |
| 주요 표시값 | Gateway 최근 실행/Transaction Trace 구조화 상세. |
| 주요 조치 | 거래 추적. |
| Button 활성 조건 | Transaction ID 필요. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Transaction/Correlation ID로 Gateway→Target 흐름을 재구성. |
| Partial/NACK/Drift | Transaction/Correlation ID로 Gateway→Target 흐름을 재구성. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=transactions)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 공통 필터 + Transaction ID. / 공통 filters.
- **실제 표시·컬럼**: Gateway 최근 실행/Transaction Trace 구조화 상세.
- **실제 조치**: 거래 추적.
- **권한·활성 조건**: Transaction ID 필요.
- **Version·Approval·Idempotency**: Correlation context 유지.
- **응답 유실·부분 실패·복구**: Transaction/Correlation ID로 Gateway→Target 흐름을 재구성.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-transactions` 진입 시 Menu `GATEWAY_TRANSACTIONS`의 Server Session 권한과 Feature Flag `adm.route.gateway-transactions.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 공통 필터 + Transaction ID. / 공통 filters.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Gateway 최근 실행/Transaction Trace 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 거래 추적.
5. 실행 전 활성 조건을 다시 검사한다: Transaction ID 필요.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Correlation context 유지.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Transaction/Correlation ID로 Gateway→Target 흐름을 재구성. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.51. Gateway 로그 정책 — `/gateway-log-policies`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_LOG_POLICY` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=log-policies)` / `adm.route.gateway-log-policies.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 로그 정책의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 공통 필터. |
| 검색 기본값 | 현재 Gateway log policy snapshot. |
| 주요 표시값 | Masking/Sampling/Retention/Payload 정책과 배포 상태. |
| 주요 조치 | 정책 새로고침. |
| Button 활성 조건 | 읽기 중심; 실제 정책 변경은 /logPolicies. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 배포 실패는 /logPolicies의 Gateway ACK/fencing과 대사. |
| Partial/NACK/Drift | 배포 실패는 /logPolicies의 Gateway ACK/fencing과 대사. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=log-policies)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 공통 필터. / 현재 Gateway log policy snapshot.
- **실제 표시·컬럼**: Masking/Sampling/Retention/Payload 정책과 배포 상태.
- **실제 조치**: 정책 새로고침.
- **권한·활성 조건**: 읽기 중심; 실제 정책 변경은 /logPolicies.
- **Version·Approval·Idempotency**: 정책 distribution/ACK 상태.
- **응답 유실·부분 실패·복구**: 배포 실패는 /logPolicies의 Gateway ACK/fencing과 대사.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-log-policies` 진입 시 Menu `GATEWAY_LOG_POLICY`의 Server Session 권한과 Feature Flag `adm.route.gateway-log-policies.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 공통 필터. / 현재 Gateway log policy snapshot.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Masking/Sampling/Retention/Payload 정책과 배포 상태.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 정책 새로고침.
5. 실행 전 활성 조건을 다시 검사한다: 읽기 중심; 실제 정책 변경은 /logPolicies.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: 정책 distribution/ACK 상태.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 배포 실패는 /logPolicies의 Gateway ACK/fencing과 대사. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.52. Gateway 적용 상태·이력 — `/gateway-apply-status`

| 항목 | 내용 |
|---|---|
| Menu | `GATEWAY_APPLY_STATUS` |
| Group / Risk | `online` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=apply)` / `adm.route.gateway-apply-status.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Gateway 적용 상태·이력의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 공통 필터; 연결시험 Test Type/Reason. |
| 검색 기본값 | auto poll15s. |
| 주요 표시값 | Gateway Instance, Expected, Applied, Status, Last Seen; Connection Test 표/Operation. |
| 주요 조치 | 조회, 연결시험 실행/상태/취소/재검증. |
| Button 활성 조건 | Health mode와 동일한 Operation 상태 조건. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Drift는 성공 Instance 보존, stale/failed Instance만 재대사. Connection Test 결과를 함께 사용. |
| Partial/NACK/Drift | Drift는 성공 Instance 보존, stale/failed Instance만 재대사. Connection Test 결과를 함께 사용. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue(initialMode=apply)` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 공통 필터; 연결시험 Test Type/Reason. / auto poll15s.
- **실제 표시·컬럼**: Gateway Instance, Expected, Applied, Status, Last Seen; Connection Test 표/Operation.
- **실제 조치**: 조회, 연결시험 실행/상태/취소/재검증.
- **권한·활성 조건**: Health mode와 동일한 Operation 상태 조건.
- **Version·Approval·Idempotency**: Expected vs Applied Version으로 Drift 판정.
- **응답 유실·부분 실패·복구**: Drift는 성공 Instance 보존, stale/failed Instance만 재대사. Connection Test 결과를 함께 사용.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/gateway-apply-status` 진입 시 Menu `GATEWAY_APPLY_STATUS`의 Server Session 권한과 Feature Flag `adm.route.gateway-apply-status.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 공통 필터; 연결시험 Test Type/Reason. / auto poll15s.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Gateway Instance, Expected, Applied, Status, Last Seen; Connection Test 표/Operation.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 연결시험 실행/상태/취소/재검증.
5. 실행 전 활성 조건을 다시 검사한다: Health mode와 동일한 Operation 상태 조건.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Expected vs Applied Version으로 Drift 판정.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Drift는 성공 Instance 보존, stale/failed Instance만 재대사. Connection Test 결과를 함께 사용. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.53. 권한 — `/permissions`

| 항목 | 내용 |
|---|---|
| Menu | `PERMISSION` |
| Group / Risk | `framework` / `MEDIUM` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/permissions/PermissionsPage.vue` / `adm.route.permissions.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 권한의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Role ID, Menu ID, Button ID, API Permission ID, read/write/delete Y/N, 사유; Role name/type/description; Menu name/parent/path/sort; Button actionCode/name/httpMethod/apiPattern; API group/name/permissionCode/path. |
| 검색 기본값 | 권한 flag는 Y/N 선택. |
| 주요 표시값 | Menu/Button/API/Role 및 Matrix 처리 결과 구조화 상세. |
| 주요 조치 | 조회, 메뉴/버튼/API 권한 저장, Role 등록/수정, Menu 등록/수정, Button 등록/수정, API Permission 등록/수정. |
| Button 활성 조건 | 모든 mutation은 PERMISSION write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Route read와 Action permission을 분리해 검증한다. 화면 표시만으로 API 권한을 추정하지 않는다. |
| Partial/NACK/Drift | Route read와 Action permission을 분리해 검증한다. 화면 표시만으로 API 권한을 추정하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/permissions/PermissionsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Role ID, Menu ID, Button ID, API Permission ID, read/write/delete Y/N, 사유; Role name/type/description; Menu name/parent/path/sort; Button actionCode/name/httpMethod/apiPattern; API group/name/permissionCode/path. / 권한 flag는 Y/N 선택.
- **실제 표시·컬럼**: Menu/Button/API/Role 및 Matrix 처리 결과 구조화 상세.
- **실제 조치**: 조회, 메뉴/버튼/API 권한 저장, Role 등록/수정, Menu 등록/수정, Button 등록/수정, API Permission 등록/수정.
- **권한·활성 조건**: 모든 mutation은 PERMISSION write.
- **Version·Approval·Idempotency**: Server 권한 원장을 변경 후 다시 조회하여 effective matrix 확인.
- **응답 유실·부분 실패·복구**: Route read와 Action permission을 분리해 검증한다. 화면 표시만으로 API 권한을 추정하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/permissions` 진입 시 Menu `PERMISSION`의 Server Session 권한과 Feature Flag `adm.route.permissions.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Role ID, Menu ID, Button ID, API Permission ID, read/write/delete Y/N, 사유; Role name/type/description; Menu name/parent/path/sort; Button actionCode/name/httpMethod/apiPattern; API group/name/permissionCode/path. / 권한 flag는 Y/N 선택.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Menu/Button/API/Role 및 Matrix 처리 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, 메뉴/버튼/API 권한 저장, Role 등록/수정, Menu 등록/수정, Button 등록/수정, API Permission 등록/수정.
5. 실행 전 활성 조건을 다시 검사한다: 모든 mutation은 PERMISSION write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Server 권한 원장을 변경 후 다시 조회하여 effective matrix 확인.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Route read와 Action permission을 분리해 검증한다. 화면 표시만으로 API 권한을 추정하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.54. 비밀번호 — `/password`

| 항목 | 내용 |
|---|---|
| Menu | `PASSWORD` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/password/PasswordPage.vue` / `adm.route.password.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 비밀번호의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 운영자 ID, 새 비밀번호(password), 강제변경 true/false, 세션 ID, 사유. |
| 검색 기본값 | 강제변경 select boolean. |
| 주요 표시값 | Password policy/session 처리 결과 구조화 상세. |
| 주요 조치 | 정책 조회, 비밀번호 초기화, 잠금해제, 세션조회, 세션 강제종료, 만료세션정리, 정책검증, 비밀번호 변경. |
| Button 활성 조건 | Mutation은 PASSWORD 또는 OPERATOR write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 비밀번호 원문을 로그/목록에 남기지 않는다. 세션 종료 후 세션 조회로 반영 확인. |
| Partial/NACK/Drift | 비밀번호 원문을 로그/목록에 남기지 않는다. 세션 종료 후 세션 조회로 반영 확인. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/password/PasswordPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 운영자 ID, 새 비밀번호(password), 강제변경 true/false, 세션 ID, 사유. / 강제변경 select boolean.
- **실제 표시·컬럼**: Password policy/session 처리 결과 구조화 상세.
- **실제 조치**: 정책 조회, 비밀번호 초기화, 잠금해제, 세션조회, 세션 강제종료, 만료세션정리, 정책검증, 비밀번호 변경.
- **권한·활성 조건**: Mutation은 PASSWORD 또는 OPERATOR write.
- **Version·Approval·Idempotency**: Server session/password policy 상태를 기준으로 조치.
- **응답 유실·부분 실패·복구**: 비밀번호 원문을 로그/목록에 남기지 않는다. 세션 종료 후 세션 조회로 반영 확인.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/password` 진입 시 Menu `PASSWORD`의 Server Session 권한과 Feature Flag `adm.route.password.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 운영자 ID, 새 비밀번호(password), 강제변경 true/false, 세션 ID, 사유. / 강제변경 select boolean.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Password policy/session 처리 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 정책 조회, 비밀번호 초기화, 잠금해제, 세션조회, 세션 강제종료, 만료세션정리, 정책검증, 비밀번호 변경.
5. 실행 전 활성 조건을 다시 검사한다: Mutation은 PASSWORD 또는 OPERATOR write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Server session/password policy 상태를 기준으로 조치.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 비밀번호 원문을 로그/목록에 남기지 않는다. 세션 종료 후 세션 조회로 반영 확인. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.55. 보안 — `/security`

| 항목 | 내용 |
|---|---|
| Menu | `SECURITY` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/security/SecurityPage.vue` / `adm.route.security.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 보안의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | IP/CIDR, 설명, 운영자 ID, Secret Ref, OTP Code, 사유. |
| 검색 기본값 | 공백 상태에서 대상 지정. |
| 주요 표시값 | IP Allowlist/MFA 상태 처리 결과. |
| 주요 조치 | 조회, IP 저장, MFA 등록, MFA 검증, MFA 해제. |
| Button 활성 조건 | Mutation은 SECURITY write. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Secret 원문이 아니라 Secret Ref를 사용. OTP 실패는 상태를 재조회 후 재등록/검증 판단. |
| Partial/NACK/Drift | Secret 원문이 아니라 Secret Ref를 사용. OTP 실패는 상태를 재조회 후 재등록/검증 판단. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/security/SecurityPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: IP/CIDR, 설명, 운영자 ID, Secret Ref, OTP Code, 사유. / 공백 상태에서 대상 지정.
- **실제 표시·컬럼**: IP Allowlist/MFA 상태 처리 결과.
- **실제 조치**: 조회, IP 저장, MFA 등록, MFA 검증, MFA 해제.
- **권한·활성 조건**: Mutation은 SECURITY write.
- **Version·Approval·Idempotency**: MFA/Allowlist Owner 상태 재조회.
- **응답 유실·부분 실패·복구**: Secret 원문이 아니라 Secret Ref를 사용. OTP 실패는 상태를 재조회 후 재등록/검증 판단.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/security` 진입 시 Menu `SECURITY`의 Server Session 권한과 Feature Flag `adm.route.security.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: IP/CIDR, 설명, 운영자 ID, Secret Ref, OTP Code, 사유. / 공백 상태에서 대상 지정.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: IP Allowlist/MFA 상태 처리 결과.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 조회, IP 저장, MFA 등록, MFA 검증, MFA 해제.
5. 실행 전 활성 조건을 다시 검사한다: Mutation은 SECURITY write.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: MFA/Allowlist Owner 상태 재조회.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Secret 원문이 아니라 Secret Ref를 사용. OTP 실패는 상태를 재조회 후 재등록/검증 판단. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.56. 운영자 — `/operators`

| 항목 | 내용 |
|---|---|
| Menu | `OPERATOR` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/operators/OperatorsPage.vue` / `adm.route.operators.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 운영자의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 운영자 ID/이름, 휴대폰, 내부전화, 초기 비밀번호, 운영사유<=500; 원문조회 사유 5~500; Role IDs/운영사유. |
| 검색 기본값 | 민감정보 원문은 일반 목록/로그에 저장하지 않음. |
| 주요 표시값 | 운영자ID, 이름, 계정상태, 연락처(마스킹 정책), 내부전화, 역할, 잠금, 관리; PII Raw Dialog 휴대폰/내부전화. |
| 주요 조치 | 새로고침, 운영자 등록, 원문보기, 조건부 활성화, 역할/세션 조회, 잠금해제, 연락처/역할 수정. |
| Button 활성 조건 | 관리/원문보기는 OPERATOR write. 활성화는 accountStatus!=ACTIVE + role 존재 + 관리권한. Raw 조회 사유>=5. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 초기비밀번호/PII 원문을 일반 상태에 보존하지 않는다. Raw 조회 시도·결과는 감사 기록을 확인한다. |
| Partial/NACK/Drift | 초기비밀번호/PII 원문을 일반 상태에 보존하지 않는다. Raw 조회 시도·결과는 감사 기록을 확인한다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/operators/OperatorsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 운영자 ID/이름, 휴대폰, 내부전화, 초기 비밀번호, 운영사유<=500; 원문조회 사유 5~500; Role IDs/운영사유. / 민감정보 원문은 일반 목록/로그에 저장하지 않음.
- **실제 표시·컬럼**: 운영자ID, 이름, 계정상태, 연락처(마스킹 정책), 내부전화, 역할, 잠금, 관리; PII Raw Dialog 휴대폰/내부전화.
- **실제 조치**: 새로고침, 운영자 등록, 원문보기, 조건부 활성화, 역할/세션 조회, 잠금해제, 연락처/역할 수정.
- **권한·활성 조건**: 관리/원문보기는 OPERATOR write. 활성화는 accountStatus!=ACTIVE + role 존재 + 관리권한. Raw 조회 사유>=5.
- **Version·Approval·Idempotency**: Server Operator/Session 상태를 기준으로 변경.
- **응답 유실·부분 실패·복구**: 초기비밀번호/PII 원문을 일반 상태에 보존하지 않는다. Raw 조회 시도·결과는 감사 기록을 확인한다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/operators` 진입 시 Menu `OPERATOR`의 Server Session 권한과 Feature Flag `adm.route.operators.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 운영자 ID/이름, 휴대폰, 내부전화, 초기 비밀번호, 운영사유<=500; 원문조회 사유 5~500; Role IDs/운영사유. / 민감정보 원문은 일반 목록/로그에 저장하지 않음.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 운영자ID, 이름, 계정상태, 연락처(마스킹 정책), 내부전화, 역할, 잠금, 관리; PII Raw Dialog 휴대폰/내부전화.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 새로고침, 운영자 등록, 원문보기, 조건부 활성화, 역할/세션 조회, 잠금해제, 연락처/역할 수정.
5. 실행 전 활성 조건을 다시 검사한다: 관리/원문보기는 OPERATOR write. 활성화는 accountStatus!=ACTIVE + role 존재 + 관리권한. Raw 조회 사유>=5.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Server Operator/Session 상태를 기준으로 변경.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 초기비밀번호/PII 원문을 일반 상태에 보존하지 않는다. Raw 조회 시도·결과는 감사 기록을 확인한다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.57. Secret / Key — `/secrets`

| 항목 | 내용 |
|---|---|
| Menu | `SECRET` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/secrets/SecretsPage.vue` / `adm.route.secrets.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Secret / Key의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Provider, Secret key/reference, Rotation 사유. |
| 검색 기본값 | provider 초기 ENV, 실제 Provider 목록에 ENV가 없으면 첫 Provider. |
| 주요 표시값 | Provider, Reference, Version, 생성, 만료, Rotation 지원; metadata attributes. |
| 주요 조치 | Provider 조회, Metadata 조회, Rotation. |
| Button 활성 조건 | Provider.rotatable && metadata.rotatable일 때만 Rotation. 사유 필수. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 지원하지 않는 Provider/Secret은 Rotation 금지. 실패 시 metadata를 재조회하고 원문을 요구하지 않는다. |
| Partial/NACK/Drift | 지원하지 않는 Provider/Secret은 Rotation 금지. 실패 시 metadata를 재조회하고 원문을 요구하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/secrets/SecretsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Provider, Secret key/reference, Rotation 사유. / provider 초기 ENV, 실제 Provider 목록에 ENV가 없으면 첫 Provider.
- **실제 표시·컬럼**: Provider, Reference, Version, 생성, 만료, Rotation 지원; metadata attributes.
- **실제 조치**: Provider 조회, Metadata 조회, Rotation.
- **권한·활성 조건**: Provider.rotatable && metadata.rotatable일 때만 Rotation. 사유 필수.
- **Version·Approval·Idempotency**: Secret Version/metadata만 표시; 원문 Secret은 UI/API에 표시하지 않음.
- **응답 유실·부분 실패·복구**: 지원하지 않는 Provider/Secret은 Rotation 금지. 실패 시 metadata를 재조회하고 원문을 요구하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/secrets` 진입 시 Menu `SECRET`의 Server Session 권한과 Feature Flag `adm.route.secrets.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Provider, Secret key/reference, Rotation 사유. / provider 초기 ENV, 실제 Provider 목록에 ENV가 없으면 첫 Provider.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Provider, Reference, Version, 생성, 만료, Rotation 지원; metadata attributes.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: Provider 조회, Metadata 조회, Rotation.
5. 실행 전 활성 조건을 다시 검사한다: Provider.rotatable && metadata.rotatable일 때만 Rotation. 사유 필수.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Secret Version/metadata만 표시; 원문 Secret은 UI/API에 표시하지 않음.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 지원하지 않는 Provider/Secret은 Rotation 금지. 실패 시 metadata를 재조회하고 원문을 요구하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.58. 위험조치 승인 — `/approvals`

| 항목 | 내용 |
|---|---|
| Menu | `APPROVAL` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue` / `adm.route.approvals.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 위험조치 승인의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 정책: Action/Code/Version>=1/Name/효력기간/enabled/selfApproval/breakGlass/설명/Steps JSON. 요청: Request ID/Key/Owner Module·Command/Target/Payload Snapshot/Expire/Decision/Idempotency Key/reason. Runtime 변경: Operation/ChangeType/SchemaVersion/ExpectedVersion/Rollout/Wave/Quorum/Approval/BreakGlass/Schedule/Expire/Reason/Target JSON/Payload JSON. |
| 검색 기본값 | Gateway Binding 정책 예시, selfApproval=N, breakGlass=N, RequestKey/IdempotencyKey UUID; Runtime Operation 미입력 시 UUID. |
| 주요 표시값 | 승인정책/요청/Runtime 변경/Operation/Audit 결과 구조화 상세. |
| 주요 조치 | 정책 목록/상세/Version 저장, 승인요청/상세/승인·반려/Owner Command 실행/UNKNOWN Reconcile, Runtime 대상/변경 Preview/생성, Change 조회/CANCEL/ROLLBACK/Audit Chain 검증, Break-glass 조회/사후검토/Operation 조회. |
| Button 활성 조건 | Owner Command 실행과 UNKNOWN Reconcile은 사용자 확인. Strict JSON 적용. Self approval 정책과 Server Session Actor를 사용. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | UNKNOWN Reconcile은 Owner 상태만 대조하며 Mutation을 재실행하지 않는다. Runtime 결과불명도 Operation ID + Audit Chain으로 복구. |
| Partial/NACK/Drift | UNKNOWN Reconcile은 Owner 상태만 대조하며 Mutation을 재실행하지 않는다. Runtime 결과불명도 Operation ID + Audit Chain으로 복구. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 정책: Action/Code/Version>=1/Name/효력기간/enabled/selfApproval/breakGlass/설명/Steps JSON. 요청: Request ID/Key/Owner Module·Command/Target/Payload Snapshot/Expire/Decision/Idempotency Key/reason. Runtime 변경: Operation/ChangeType/SchemaVersion/ExpectedVersion/Rollout/Wave/Quorum/Approval/BreakGlass/Schedule/Expire/Reason/Target JSON/Payload JSON. / Gateway Binding 정책 예시, selfApproval=N, breakGlass=N, RequestKey/IdempotencyKey UUID; Runtime Operation 미입력 시 UUID.
- **실제 표시·컬럼**: 승인정책/요청/Runtime 변경/Operation/Audit 결과 구조화 상세.
- **실제 조치**: 정책 목록/상세/Version 저장, 승인요청/상세/승인·반려/Owner Command 실행/UNKNOWN Reconcile, Runtime 대상/변경 Preview/생성, Change 조회/CANCEL/ROLLBACK/Audit Chain 검증, Break-glass 조회/사후검토/Operation 조회.
- **권한·활성 조건**: Owner Command 실행과 UNKNOWN Reconcile은 사용자 확인. Strict JSON 적용. Self approval 정책과 Server Session Actor를 사용.
- **Version·Approval·Idempotency**: Policy Version, Expected Version CAS, Idempotency Key, Approval/Break-glass, Operation ID.
- **응답 유실·부분 실패·복구**: UNKNOWN Reconcile은 Owner 상태만 대조하며 Mutation을 재실행하지 않는다. Runtime 결과불명도 Operation ID + Audit Chain으로 복구.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/approvals` 진입 시 Menu `APPROVAL`의 Server Session 권한과 Feature Flag `adm.route.approvals.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 정책: Action/Code/Version>=1/Name/효력기간/enabled/selfApproval/breakGlass/설명/Steps JSON. 요청: Request ID/Key/Owner Module·Command/Target/Payload Snapshot/Expire/Decision/Idempotency Key/reason. Runtime 변경: Operation/ChangeType/SchemaVersion/ExpectedVersion/Rollout/Wave/Quorum/Approval/BreakGlass/Schedule/Expire/Reason/Target JSON/Payload JSON. / Gateway Binding 정책 예시, selfApproval=N, breakGlass=N, RequestKey/IdempotencyKey UUID; Runtime Operation 미입력 시 UUID.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: 승인정책/요청/Runtime 변경/Operation/Audit 결과 구조화 상세.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 정책 목록/상세/Version 저장, 승인요청/상세/승인·반려/Owner Command 실행/UNKNOWN Reconcile, Runtime 대상/변경 Preview/생성, Change 조회/CANCEL/ROLLBACK/Audit Chain 검증, Break-glass 조회/사후검토/Operation 조회.
5. 실행 전 활성 조건을 다시 검사한다: Owner Command 실행과 UNKNOWN Reconcile은 사용자 확인. Strict JSON 적용. Self approval 정책과 Server Session Actor를 사용.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Policy Version, Expected Version CAS, Idempotency Key, Approval/Break-glass, Operation ID.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: UNKNOWN Reconcile은 Owner 상태만 대조하며 Mutation을 재실행하지 않는다. Runtime 결과불명도 Operation ID + Audit Chain으로 복구. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.59. Break-glass — `/breakGlass`

| 항목 | 내용 |
|---|---|
| Menu | `BREAK_GLASS` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/break-glass/BreakGlassPage.vue` / `adm.route.breakGlass.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Break-glass의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | Scope(SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY), 대상, TTL 1~30분, 긴급사유>=5; 종료/사후검토 감사사유>=5. |
| 검색 기본값 | scope SERVICE, reason `긴급 장애 복구`, TTL 15분. |
| 주요 표시값 | Active Session, 기본 TTL15m, Post Review KPI; 운영자/Scope/대상/상태/만료/사후검토/조치. |
| 주요 조치 | 발급, ACTIVE 세션 종료, 종료 후 PENDING 사후검토 승인/문제기록. |
| Button 활성 조건 | BREAK_GLASS write. 대상 필수. 운영자별 active 1개 제한은 화면 설명. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 만료/종료 후 사후검토를 완료한다. 비상 권한을 장기 권한으로 전환하지 않는다. |
| Partial/NACK/Drift | 만료/종료 후 사후검토를 완료한다. 비상 권한을 장기 권한으로 전환하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/break-glass/BreakGlassPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: Scope(SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY), 대상, TTL 1~30분, 긴급사유>=5; 종료/사후검토 감사사유>=5. / scope SERVICE, reason `긴급 장애 복구`, TTL 15분.
- **실제 표시·컬럼**: Active Session, 기본 TTL15m, Post Review KPI; 운영자/Scope/대상/상태/만료/사후검토/조치.
- **실제 조치**: 발급, ACTIVE 세션 종료, 종료 후 PENDING 사후검토 승인/문제기록.
- **권한·활성 조건**: BREAK_GLASS write. 대상 필수. 운영자별 active 1개 제한은 화면 설명.
- **Version·Approval·Idempotency**: Session ID/TTL/상태/PostReview 상태.
- **응답 유실·부분 실패·복구**: 만료/종료 후 사후검토를 완료한다. 비상 권한을 장기 권한으로 전환하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/breakGlass` 진입 시 Menu `BREAK_GLASS`의 Server Session 권한과 Feature Flag `adm.route.breakGlass.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: Scope(SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY), 대상, TTL 1~30분, 긴급사유>=5; 종료/사후검토 감사사유>=5. / scope SERVICE, reason `긴급 장애 복구`, TTL 15분.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Active Session, 기본 TTL15m, Post Review KPI; 운영자/Scope/대상/상태/만료/사후검토/조치.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 발급, ACTIVE 세션 종료, 종료 후 PENDING 사후검토 승인/문제기록.
5. 실행 전 활성 조건을 다시 검사한다: BREAK_GLASS write. 대상 필수. 운영자별 active 1개 제한은 화면 설명.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Session ID/TTL/상태/PostReview 상태.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 만료/종료 후 사후검토를 완료한다. 비상 권한을 장기 권한으로 전환하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.60. Feature Flag — `/featureFlags`

| 항목 | 내용 |
|---|---|
| Menu | `FEATURE_FLAG` |
| Group / Risk | `framework` / `CRITICAL` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/feature-flags/FeatureFlagsPage.vue` / `adm.route.featureFlags.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Feature Flag의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 검색 Flag Key; 평가 Flag/Type BOOLEAN·STRING·INTEGER·DECIMAL/Fallback/Targeting Key; Override Flag/Type/Value(password)/ExpiresAt/사유; RequestID/승인·회수사유; Kill Switch Flag/상태/사유. |
| 검색 기본값 | 검색 page0 size100; 평가 type BOOLEAN, fallback false, targetingKey operator-preview; Override type BOOLEAN; Kill enabled=true. |
| 주요 표시값 | Flag, Source, Revision, Reason만 목록에 표시. Typed 평가 결과도 flag/source/revision/reasonCode만 안전 출력. |
| 주요 조치 | 검색, Typed 평가, Override 요청/승인/회수, Kill Switch 반영. |
| Button 활성 조건 | 위험 승인/회수/Kill 요청은 generated API에 `X-CPF-Risk-Confirmed: confirmed` 사용. Value 원문은 목록·감사에 표시하지 않음. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | Override/Kill 반영 후 검색으로 revision/source를 확인. 값 원문을 재노출해 확인하지 않는다. |
| Partial/NACK/Drift | Override/Kill 반영 후 검색으로 revision/source를 확인. 값 원문을 재노출해 확인하지 않는다. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/feature-flags/FeatureFlagsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 검색 Flag Key; 평가 Flag/Type BOOLEAN·STRING·INTEGER·DECIMAL/Fallback/Targeting Key; Override Flag/Type/Value(password)/ExpiresAt/사유; RequestID/승인·회수사유; Kill Switch Flag/상태/사유. / 검색 page0 size100; 평가 type BOOLEAN, fallback false, targetingKey operator-preview; Override type BOOLEAN; Kill enabled=true.
- **실제 표시·컬럼**: Flag, Source, Revision, Reason만 목록에 표시. Typed 평가 결과도 flag/source/revision/reasonCode만 안전 출력.
- **실제 조치**: 검색, Typed 평가, Override 요청/승인/회수, Kill Switch 반영.
- **권한·활성 조건**: 위험 승인/회수/Kill 요청은 generated API에 `X-CPF-Risk-Confirmed: confirmed` 사용. Value 원문은 목록·감사에 표시하지 않음.
- **Version·Approval·Idempotency**: Revision + Request ID. Override request 후 value 입력을 비운다.
- **응답 유실·부분 실패·복구**: Override/Kill 반영 후 검색으로 revision/source를 확인. 값 원문을 재노출해 확인하지 않는다.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/featureFlags` 진입 시 Menu `FEATURE_FLAG`의 Server Session 권한과 Feature Flag `adm.route.featureFlags.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 검색 Flag Key; 평가 Flag/Type BOOLEAN·STRING·INTEGER·DECIMAL/Fallback/Targeting Key; Override Flag/Type/Value(password)/ExpiresAt/사유; RequestID/승인·회수사유; Kill Switch Flag/상태/사유. / 검색 page0 size100; 평가 type BOOLEAN, fallback false, targetingKey operator-preview; Override type BOOLEAN; Kill enabled=true.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Flag, Source, Revision, Reason만 목록에 표시. Typed 평가 결과도 flag/source/revision/reasonCode만 안전 출력.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 검색, Typed 평가, Override 요청/승인/회수, Kill Switch 반영.
5. 실행 전 활성 조건을 다시 검사한다: 위험 승인/회수/Kill 요청은 generated API에 `X-CPF-Risk-Confirmed: confirmed` 사용. Value 원문은 목록·감사에 표시하지 않음.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Revision + Request ID. Override request 후 value 입력을 비운다.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: Override/Kill 반영 후 검색으로 revision/source를 확인. 값 원문을 재노출해 확인하지 않는다. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.61. 통합 운영 정정 승인 — `/integrationClosure`

| 항목 | 내용 |
|---|---|
| Menu | `INTEGRATION_CLOSURE` |
| Group / Risk | `integration` / `CRITICAL` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue` / `adm.route.integrationClosure.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | 통합 운영 정정 승인의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 시간대, 허용 Skew ms; Record ID+Strict JSON; Quarantine ID, Expected Version>=1, 사유>=8, Corrected Strict JSON; DLQ limit1~500; Webhook ID, Expected Version>=1, 사유>=8. |
| 검색 기본값 | zone Asia/Seoul, maxSkewMillis=1000, record/corrected JSON `{}`, expectedVersion=1, DLQ limit100, replay idempotency UUID. |
| 주요 표시값 | Crypto/Time 상태, Data Quality validation, Approval/Replay 결과, Webhook DLQ 식별자/상태/버전. |
| 주요 조치 | 상태조회, 품질검증, 정정 승인요청, 승인 검증 후 단회실행, 새 작업, 재검증 Replay, Webhook DLQ조회/Replay. |
| Button 활성 조건 | 각 Operation ID별 permission을 `canInvokeOperation`으로 직접 검사. 승인요청은 quarantine+reason>=8+version>0+not confirmed. Execute/Replay도 사유>=8. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 409은 최신 상태 재조회. Timeout/응답유실은 동일 Draft 멱등키를 유지. UNKNOWN 승인 Reconcile은 Mutation 재실행 금지. Webhook도 version 확인 후 Replay. |
| Partial/NACK/Drift | 409은 최신 상태 재조회. Timeout/응답유실은 동일 Draft 멱등키를 유지. UNKNOWN 승인 Reconcile은 Mutation 재실행 금지. Webhook도 version 확인 후 Replay. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 시간대, 허용 Skew ms; Record ID+Strict JSON; Quarantine ID, Expected Version>=1, 사유>=8, Corrected Strict JSON; DLQ limit1~500; Webhook ID, Expected Version>=1, 사유>=8. / zone Asia/Seoul, maxSkewMillis=1000, record/corrected JSON `{}`, expectedVersion=1, DLQ limit100, replay idempotency UUID.
- **실제 표시·컬럼**: Crypto/Time 상태, Data Quality validation, Approval/Replay 결과, Webhook DLQ 식별자/상태/버전.
- **실제 조치**: 상태조회, 품질검증, 정정 승인요청, 승인 검증 후 단회실행, 새 작업, 재검증 Replay, Webhook DLQ조회/Replay.
- **권한·활성 조건**: 각 Operation ID별 permission을 `canInvokeOperation`으로 직접 검사. 승인요청은 quarantine+reason>=8+version>0+not confirmed. Execute/Replay도 사유>=8.
- **Version·Approval·Idempotency**: Expected Version + Session-scope 동일 Draft idempotency key. 성공 확정 승인요청은 중복요청 차단.
- **응답 유실·부분 실패·복구**: 409은 최신 상태 재조회. Timeout/응답유실은 동일 Draft 멱등키를 유지. UNKNOWN 승인 Reconcile은 Mutation 재실행 금지. Webhook도 version 확인 후 Replay.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/integrationClosure` 진입 시 Menu `INTEGRATION_CLOSURE`의 Server Session 권한과 Feature Flag `adm.route.integrationClosure.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 시간대, 허용 Skew ms; Record ID+Strict JSON; Quarantine ID, Expected Version>=1, 사유>=8, Corrected Strict JSON; DLQ limit1~500; Webhook ID, Expected Version>=1, 사유>=8. / zone Asia/Seoul, maxSkewMillis=1000, record/corrected JSON `{}`, expectedVersion=1, DLQ limit100, replay idempotency UUID.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Crypto/Time 상태, Data Quality validation, Approval/Replay 결과, Webhook DLQ 식별자/상태/버전.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 상태조회, 품질검증, 정정 승인요청, 승인 검증 후 단회실행, 새 작업, 재검증 Replay, Webhook DLQ조회/Replay.
5. 실행 전 활성 조건을 다시 검사한다: 각 Operation ID별 permission을 `canInvokeOperation`으로 직접 검사. 승인요청은 quarantine+reason>=8+version>0+not confirmed. Execute/Replay도 사유>=8.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Expected Version + Session-scope 동일 Draft idempotency key. 성공 확정 승인요청은 중복요청 차단.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 409은 최신 상태 재조회. Timeout/응답유실은 동일 Draft 멱등키를 유지. UNKNOWN 승인 Reconcile은 Mutation 재실행 금지. Webhook도 version 확인 후 Replay. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.62. OpenAPI 운영 — `/openApiOperations`

| 항목 | 내용 |
|---|---|
| Menu | `OPENAPI_OPERATIONS` |
| Group / Risk | `framework` / `HIGH` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/openapi-operations/OpenApiOperationsPage.vue` / `adm.route.openApiOperations.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | OpenAPI 운영의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 재대사 사유<=500 + 위험조치 확인 checkbox. |
| 검색 기본값 | 상태는 mount 시 자동 조회. |
| 주요 표시값 | Status, Instance, Operation count, API Docs 노출 여부/Path, Refreshed, Failure. |
| 주요 조치 | 상태 새로고침, Route Inventory 재대사. |
| Button 활성 조건 | 재대사는 reason nonblank + confirmed. 요청에 `X-CPF-Risk-Confirmed: confirmed`. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 재대사 실패 시 기존 Snapshot 상태/Failure Code를 유지해 진단하고 반복 실행 전 원인을 확인. |
| Partial/NACK/Drift | 재대사 실패 시 기존 Snapshot 상태/Failure Code를 유지해 진단하고 반복 실행 전 원인을 확인. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/openapi-operations/OpenApiOperationsPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 재대사 사유<=500 + 위험조치 확인 checkbox. / 상태는 mount 시 자동 조회.
- **실제 표시·컬럼**: Status, Instance, Operation count, API Docs 노출 여부/Path, Refreshed, Failure.
- **실제 조치**: 상태 새로고침, Route Inventory 재대사.
- **권한·활성 조건**: 재대사는 reason nonblank + confirmed. 요청에 `X-CPF-Risk-Confirmed: confirmed`.
- **Version·Approval·Idempotency**: Runtime route inventory snapshot 시각/instance.
- **응답 유실·부분 실패·복구**: 재대사 실패 시 기존 Snapshot 상태/Failure Code를 유지해 진단하고 반복 실행 전 원인을 확인.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/openApiOperations` 진입 시 Menu `OPENAPI_OPERATIONS`의 Server Session 권한과 Feature Flag `adm.route.openApiOperations.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 재대사 사유<=500 + 위험조치 확인 checkbox. / 상태는 mount 시 자동 조회.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Status, Instance, Operation count, API Docs 노출 여부/Path, Refreshed, Failure.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 상태 새로고침, Route Inventory 재대사.
5. 실행 전 활성 조건을 다시 검사한다: 재대사는 reason nonblank + confirmed. 요청에 `X-CPF-Risk-Confirmed: confirmed`.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Runtime route inventory snapshot 시각/instance.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 재대사 실패 시 기존 Snapshot 상태/Failure Code를 유지해 진단하고 반복 실행 전 원인을 확인. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

### 7.63. Resilience 정책 — `/resiliencePolicies`

| 항목 | 내용 |
|---|---|
| Menu | `RESILIENCE_POLICY` |
| Group / Risk | `framework` / `CRITICAL` |
| Component / Feature Flag | `cpf-admin/frontend/src/features/resilience-policies/ResiliencePoliciesPage.vue` / `adm.route.resiliencePolicies.enabled` |
| API 소비 경계 | `cpf-admin/frontend/src/app/routes.ts`의 `expectedOperationIds`; **Button 목록과 동일하다고 간주하지 않는다.** |
| 이 화면으로 완료하는 일 | Resilience 정책의 현재 상태를 읽고 필요한 조치·대사·감사까지 종결한다. |
| 검색 Field | 검색 Operation ID; 정책 Operation ID, Timeout ms, Max Attempts, Retry Backoff ms, Circuit Threshold/Open ms, Bulkhead Max Concurrent, Rate Permits/Window ms, Idempotent, Unknown Reconcile, 사유; Request ID/결정 사유. |
| 검색 기본값 | timeout3000ms, attempts3, backoff100ms, circuit threshold5/open30000ms, bulkhead50, permits100/window1000ms, idempotent=true, unknown reconcile=true. |
| 주요 표시값 | Operation, Revision, Timeout, Attempts. |
| 주요 조치 | 검색, 정책 승인요청, 승인, 반려. |
| Button 활성 조건 | 화면은 `2인 승인` 표시. 승인/반려는 `X-CPF-Risk-Confirmed: confirmed` 요청. |
| 정상 판정 | Component 표시 상태와 Owner/API 재조회 결과가 일치하고, 변경 조치는 감사 식별자로 추적 가능해야 한다. |
| Timeout/응답 유실 | 정책 반영 후 검색으로 Revision을 확인. 비멱등 Operation은 retry/unknown reconcile 정책을 별도 판단. |
| Partial/NACK/Drift | 정책 반영 후 검색으로 Revision을 확인. 비멱등 Operation은 retry/unknown reconcile 정책을 별도 판단. |
| Audit | Actor·Reason·Target·Before/After·Operation·Approval·결과를 확인한다. |

#### 실제 Component 계약

- **Source**: `cpf-admin/frontend/src/features/resilience-policies/ResiliencePoliciesPage.vue` @ `64049044956924032360fa80be83b5e37c64f828`
- **실제 입력·검색 / 기본값**: 검색 Operation ID; 정책 Operation ID, Timeout ms, Max Attempts, Retry Backoff ms, Circuit Threshold/Open ms, Bulkhead Max Concurrent, Rate Permits/Window ms, Idempotent, Unknown Reconcile, 사유; Request ID/결정 사유. / timeout3000ms, attempts3, backoff100ms, circuit threshold5/open30000ms, bulkhead50, permits100/window1000ms, idempotent=true, unknown reconcile=true.
- **실제 표시·컬럼**: Operation, Revision, Timeout, Attempts.
- **실제 조치**: 검색, 정책 승인요청, 승인, 반려.
- **권한·활성 조건**: 화면은 `2인 승인` 표시. 승인/반려는 `X-CPF-Risk-Confirmed: confirmed` 요청.
- **Version·Approval·Idempotency**: Policy Revision + Approval Request ID.
- **응답 유실·부분 실패·복구**: 정책 반영 후 검색으로 Revision을 확인. 비멱등 Operation은 retry/unknown reconcile 정책을 별도 판단.
- **검증 상태**: 현재 기준 Commit의 Route Registry와 Vue Component를 직접 대조한 `완료`. Runtime/Browser 실제 실행 검증은 별도 `미검증` 범위다.

**운영 절차**

1. `/resiliencePolicies` 진입 시 Menu `RESILIENCE_POLICY`의 Server Session 권한과 Feature Flag `adm.route.resiliencePolicies.enabled`를 먼저 확인한다.
2. 실제 Component의 입력·기본값을 그대로 사용한다: 검색 Operation ID; 정책 Operation ID, Timeout ms, Max Attempts, Retry Backoff ms, Circuit Threshold/Open ms, Bulkhead Max Concurrent, Rate Permits/Window ms, Idempotent, Unknown Reconcile, 사유; Request ID/결정 사유. / timeout3000ms, attempts3, backoff100ms, circuit threshold5/open30000ms, bulkhead50, permits100/window1000ms, idempotent=true, unknown reconcile=true.
3. 조회 후 다음 표시 계약을 읽어 대상과 현재 상태를 확정한다: Operation, Revision, Timeout, Attempts.
4. 변경이 필요한 경우 실제 Component가 제공하는 조치만 사용한다: 검색, 정책 승인요청, 승인, 반려.
5. 실행 전 활성 조건을 다시 검사한다: 화면은 `2인 승인` 표시. 승인/반려는 `X-CPF-Risk-Confirmed: confirmed` 요청.
6. Version·승인·멱등 계약은 다음 기준을 적용한다: Policy Revision + Approval Request ID.
7. 응답 유실·부분 실패·상태 불일치가 있으면 재실행보다 다음 복구 계약을 우선한다: 정책 반영 후 검색으로 Revision을 확인. 비멱등 Operation은 retry/unknown reconcile 정책을 별도 판단. Owner/API 재조회 결과와 Audit가 일치하면 종결한다.

**경계·오류**

- 401: 세션/인증을 갱신하되 이전 Command 결과를 추정하지 않는다.
- 403: Permission/Data Scope/Masking 정책을 확인한다.
- 409: 최신 Version을 재조회하고 재판단한다.
- 422: 입력/정책 Validation을 보정한다.
- Timeout: Operation/Attempt 조회.
- 5xx: Dependency Health와 Target 상태를 확인한다.

## 8. 응답 유실 공통 Runbook

1. 사용자가 누른 Button과 Target ID를 기록한다.
2. 화면의 Idempotency/Operation ID를 확보한다.
3. 동일 Command를 반복하지 않는다.
4. Operation Status를 조회한다.
5. Attempt가 있으면 Target Tracking/Observed 상태를 조회한다.
6. Owner Current Row와 History/Audit를 비교한다.
7. 성공이면 화면 Snapshot을 갱신한다.
8. 실패면 실패 원인을 보정하고 새 Operation을 만든다.
9. 결과 불명이 계속되면 Incident로 승격한다.

## 9. 부분 적용 공통 Runbook

- Target별 성공/실패/Unknown을 분리한다.
- 성공 Target을 유지할지 전체 LKG로 Rollback할지 정책을 확인한다.
- 실패 Target의 환경/Version/Checksum을 확인한다.
- 재적용에는 새 Reason/Approval/Operation을 사용한다.
- 최종 Desired/Observed를 대사한다.

## 10. 교대 인계

환경, Incident/Operation/Target, 현재 상태, 마지막 Evidence, 실행 조치, Reason/Approval, 성공·미종결 Target, 다음 판정 시각, Rollback Point, 금지 반복 조치를 인계한다.

## 11. Browser/Fault 검수 기준

모든 Route는 최소 다음을 확인한다.

- Route 직접 접근/메뉴 접근.
- 검색 기본값과 빈 결과.
- Permission 403.
- Data Scope.
- Masking/원문 권한.
- 409 Conflict.
- Timeout/응답 유실.
- Partial/NACK/Drift(해당 시).
- Audit/Trace 이동.
- Reload 후 상태 유지/재조회.

## 12. 완료 체크리스트

- [ ] Route 전체가 문서에 있다.
- [ ] 각 Route에 검색/표시/조치/정상판정/오류/복구/Audit가 있다.
- [ ] 위험 조치에 Version/Reason/Approval/Idempotency가 있다.
- [ ] 401/403/409/Timeout/Partial을 구분한다.
- [ ] 운영자는 Owner DB를 직접 수정하지 않는다.
- [ ] UNKNOWN_RESULT는 대사로 종결한다.
- [ ] 교대 인계 항목이 있다.

## 13. Session 만료·403·404 운영 판단

- Session 만료: 재로그인 후 **변경 Operation부터 확인**하고 Command를 반복하지 않는다.
- 403: Menu/Button/API/Data Scope 중 어느 Decision에서 거부됐는지 확인한다.
- 404: Route 미배포, Feature Flag, Resource 미존재를 구분한다.
- Deep Link 실패: Route Registry와 배포 Artifact Version을 확인한다.

## 14. 원문·Export·Support Bundle 통제

원문 조회와 Export에는 별도 Permission, Reason, 필요 시 Approval, Row/Size Limit, Expiry, Download Audit가 필요하다. Support Bundle에는 Secret/Token/Session/Private Key/PII 원문이 들어가지 않게 Sanitization 결과를 확인한다.

## 15. Incident 종결 기준

Incident는 화면 상태가 녹색이 되었다는 이유로 종결하지 않는다. 원인, 영향 범위, Owner 상태, 미종결 Operation, 재발 통제, Audit, 다음 교대 여부를 확인한다.

## 16. Source-verified 화면 계약 보강

### 16.1 거래 로그 `/logs` — 실제 Component 기준

기준 Component: `cpf-admin/frontend/src/features/logs/LogsPage.vue`.

**검색 조건**

| Field | 의미 |
|---|---|
| transactionId | CPF Transaction 식별자 |
| traceId | 분산 Trace 식별자 |
| businessTransactionId | 업무 거래 식별자 |
| URI | 요청 URI |
| responseCode | CPF 응답 코드 |
| httpStatus | HTTP 상태 |
| memberNo | 회원번호 보호검색 |
| customerNo | 고객번호 보호검색 |
| channelCode | 채널 |
| logType | 로그 유형 |

**목록 Column**: IDX, transactionId, 거래명/URI, Module/WAS, Instance/Host, 채널, HTTP/응답, 시작, 소요(ms).

**상세**

- IN / GATEWAY / OUT / RESULT Timeline.
- Retry·Failover Attempt: #, Target Group, Target Instance, Protocol, Connect, Response, 상태, 실패 단계.
- Tab: 요약, 수신 헤더, 해석 헤더, 전파 헤더, 응답 헤더, 요청, 응답, 오류, 상세, 전문.
- Export Artifact: Export ID를 입력한 뒤 실제 Button **`Artifact 다운로드`**로 파일을 받는다.

**Button/활성 조건**

- `조회`: 현재 Filter로 검색.
- `마스킹 상세 복사`: 상세가 로드된 경우에만 활성.
- `감사 상세 저장`: 상세가 로드된 경우에만 활성.
- Paging Size: 10/20/50; 이전/다음은 경계에서 비활성.

**운영 절차**

1. transactionId/traceId/업무 ID 중 아는 식별자부터 조회한다.
2. HTTP/CPF 응답과 Timeline을 연결한다.
3. Retry/Failover가 있었다면 Attempt별 Target/실패 단계를 비교한다.
4. 전문/요청/응답을 볼 때 Masking 정책을 확인한다.
5. Export가 필요하면 승인/감사 정책에 맞는 Export ID로 Artifact를 받는다.
6. 응답 유실은 마지막 RESULT가 아니라 Owner/Attempt/Audit를 함께 대사해 종결한다.

### 16.2 63 Route 카드의 Source 검수 규칙

각 Route는 `Route 존재 → Component 존재 → 실제 검색 Field/Default → Column/Detail → Button/활성 조건 → Permission → API → 오류/Recovery → Audit` 순서로 검수한다. 조회 전용 화면에 변경 Button, Approval, Rollback을 임의로 만들지 않는다. 실제 Component에 없는 Field/Action도 문서에 추가하지 않는다.

### 16.3 공통 HTTP/Browser 장애 판단

- 401: 인증/Session 복구. 이전 Mutation 자동 재전송 금지.
- 403: Permission/Data Scope를 확인하고 권한 없이 우회 조회 금지.
- 409: latest version 재조회 후 Reason/Approval 포함 재판단.
- 422: 입력/정책 Validation 오류를 보정.
- Timeout/Network loss: Operation/Attempt/Owner 조회 전 재실행 금지.
- Partial Apply/NACK: 성공 Target 보존, 실패 Target만 Reconcile/LKG/Rollback.
- Download/Export: Masking·Reason·Approval·Audit·Retention 확인.

## 17. 운영자 관점 최종 검수

63/63 Route가 지도와 운영 카드를 가지며, Route별 실제 Component 검증 결과가 Source Trace에 연결되어야 한다. 특히 위험 조치·승인·Config·Gateway·Batch·Recovery 화면은 Expected Version/Reason/Approval/Operation/Audit를 생략하지 않는다.
