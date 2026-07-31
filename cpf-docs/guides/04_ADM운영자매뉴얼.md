# CPF ADM 운영자 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF` · **기준 Branch** `master` · **기준 SHA** `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)  
> **문서 기준일** `2026-07-31` · **Runtime 검증 상태** QA32 전체 Runtime·3DB·Browser·다중 인스턴스 Evidence는 `미검증`  
> **문서 목적** 실제 ADM Route Registry와 권한 경계를 기준으로 모든 메뉴의 목적·진입·조회·판단·조치·오류·복구·감사 절차를 제공한다.  
> **주요 독자** 플랫폼 조회 운영자, 조치 운영자, 승인자, 감사자, 보안 운영자, 장애 대응 담당자  
> **완료 결과** 운영자가 자신의 권한으로 제공되는 모든 ADM 메뉴를 찾아 안전하게 조회·조치·승인·복구하고 Evidence를 남긴다.

> [!IMPORTANT]
> 최신 `master`의 Requirement·Architecture·Source·SQL·Config·Frontend·Test·Script를 교차 확인해 작성한 역할별 정본이다.
> Source에서 확인한 사실과 제품 계약을 구분하며, 실행하지 않은 Build·DB·Kafka·Browser·Failure/Recovery 검증을 성공으로 기록하지 않는다.
> 실제 환경 수행 시 기준 SHA, Profile, 명령, 시작·종료 시각, Exit Code, Expected/Actual, 민감정보 제거 여부를 Evidence에 남긴다.

## 문서 사용 계약

| 상태 | 의미 |
|---|---|
| `완료` | 최신 exact SHA에서 Source·Consumer·Failure/Recovery·필수 Runtime Evidence가 모두 확인됨 |
| `부분 구현` | 일부 계층만 연결되었거나 Legacy/대체 경로가 Primary로 남음 |
| `미구현` | 제품 Source 또는 필수 수직 연결이 없음 |
| `미검증` | Source는 있으나 필요한 실제 환경 검증이 실행되지 않음 |
| `실패` | 필수 Gate·Scenario가 실패함 |
| `재확인 필요` | Source·Evidence·SHA·환경이 상충하거나 불명확함 |

- **Source-confirmed**: 기준 SHA의 Source·Config·SQL·Route·Script에서 직접 확인했다.
- **Product contract**: CPF 정본이 요구하는 동작이다. 실제 Runtime Evidence가 없으면 `미검증`으로 표시한다.
- **Operator procedure**: 운영자가 수행해야 하는 절차다. 화면·권한·환경 차이는 실제 배포 Catalog를 우선한다.
- **Prohibited**: 성공처럼 보여도 제품 안정성·감사·복구를 깨뜨려 금지하는 방식이다.

```text
Requirement → Owner → Public API/SPI → Application/Policy → Adapter/State
→ Runtime Consumer → Security/Approval/Audit → Failure/Recovery
→ Test/Evidence → Guide/EDU → Legacy 제거
```


## 1. 로그인·공통 화면

실제 로그인:

```http
POST /adm/api/auth/login
Content-Type: application/json

{"operatorId":"...","password":"..."}
```

성공 후 Frontend는 `operator`, `menus`, `buttonIds`를 사용한다. 메뉴가 보이더라도 Read/Write/Delete·Button·API Permission은 별개다.

### 1.1 항상 확인

- Environment·Region
- Operator ID·Role
- SystemCode·Service
- 데이터 수집 시각
- Current·Expected Version
- Stale·Partial·Drift
- Pending Approval
- transactionId·operationId

### 1.2 검색

- 기간 최소화
- ID 우선
- Page Size·Sort
- Masking
- Export 최소 범위
- 0건과 수집 실패 구분

### 1.3 위험 조치

```text
대상·Environment
→ 최신 상태·Version
→ 영향·대체 Capacity
→ Permission
→ Reason·Ticket
→ Approval
→ 실행
→ operationId
→ Owner 상태
→ UNKNOWN_RESULT Reconcile
→ Audit·Evidence
```

## 2. 권한 레벨

| 레벨 | 가능 | 금지 |
|---|---|---|
| 조회 운영자 | List·Detail·Metric·Masked Log | Command·원문·Export |
| 조치 운영자 | 승인된 Start/Stop/Drain/Restart | Permission 외 조치 |
| 승인자 | 검토·승인·반려 | 자기 요청 자기 승인 |
| 감사자 | Audit·Evidence·Download Audit | Runtime 변경 |
| 보안 운영자 | Session·MFA·IP·Secret 상태 | Secret 원문 일반 조회 |
| Break-glass | 승인된 기간·대상 | 상시 Role 대체 |

## 3. 전체 Route 색인

> Source-confirmed: `cpf-admin/frontend/src/app/routes.ts`. Dashboard는 `/`, 나머지는 `/<routeId>`. 실제 표시 이름과 접근 가능 여부는 로그인 응답 Menu Catalog와 Server Permission이 최종 결정한다.

| Route | 가이드 표기 | Group | Component | 주 권한 |
|---|---|---|---|---|
| `/` | 대시보드 | `home` | `DashboardPage.vue` | 조회 운영자 |
| `/topology` | 토폴로지 | `home` | `TopologyPage.vue` | 조회 운영자 |
| `/capacity` | 용량 현황 | `home` | `CapacityPage.vue` | 조회 운영자 |
| `/logs` | 통합 로그 | `monitoring` | `LogsPage.vue` | 관제/감사 운영자 |
| `/transactionGroups` | 거래 그룹 | `online` | `TransactionGroupsPage.vue` | 조회/조치 운영자 |
| `/transactions` | 거래 조회 | `online` | `TransactionsPage.vue` | 조회/조치 운영자 |
| `/remoteLogs` | 원격 로그 | `monitoring` | `RemoteLogsPage.vue` | 관제/감사 운영자 |
| `/auditLogs` | 감사 로그 | `monitoring` | `AuditLogsPage.vue` | 관제/감사 운영자 |
| `/logLevel` | 로그 레벨 | `monitoring` | `LogLevelPage.vue` | 관제/감사 운영자 |
| `/logPolicies` | 로그 정책 | `monitoring` | `LogPoliciesPage.vue` | 관제/감사 운영자 |
| `/standardExecutions` | 표준 실행 | `online` | `StandardExecutionsPage.vue` | 조회/조치 운영자 |
| `/channelPolicy` | 채널 정책 | `online` | `ChannelPolicyPage.vue` | 조회/조치 운영자 |
| `/serviceRegistry` | 서비스 레지스트리 | `online` | `ServiceRegistryPage.vue` | 조회/조치 운영자 |
| `/runtimeControl` | Runtime 제어 | `online` | `RuntimeControlPage.vue` | 조회/조치 운영자 |
| `/maintenance` | 유지보수 | `framework` | `MaintenancePage.vue` | 플랫폼/보안 관리자 |
| `/cache` | 캐시 | `framework` | `CachePage.vue` | 플랫폼/보안 관리자 |
| `/configs` | 설정 | `framework` | `ConfigsPage.vue` | 플랫폼/보안 관리자 |
| `/responseCodes` | 응답 코드 | `framework` | `ResponseCodesPage.vue` | 플랫폼/보안 관리자 |
| `/businessCalendar` | 영업일 달력 | `framework` | `BusinessCalendarPage.vue` | 플랫폼/보안 관리자 |
| `/recoveryCenter` | 복구 센터 | `monitoring` | `RecoveryCenterPage.vue` | 관제/감사 운영자 |
| `/incidents` | 장애 관리 | `monitoring` | `IncidentsPage.vue` | 관제/감사 운영자 |
| `/reliability` | 신뢰성 | `monitoring` | `ReliabilityPage.vue` | 관제/감사 운영자 |
| `/notifications` | 알림 | `integration` | `NotificationsPage.vue` | 연계 운영자 |
| `/batch` | 배치 | `batch` | `BatchPage.vue` | 배치 조회/조치 운영자 |
| `/batch-overview` | 배치 개요 | `batch` | `BatchOverviewPage.vue` | 배치 조회/조치 운영자 |
| `/batch-runtime` | 배치 Runtime | `batch` | `RuntimeTopologyPage.vue` | 배치 조회/조치 운영자 |
| `/batch-instances` | 배치 인스턴스 | `batch` | `BatchInstancesPage.vue` | 배치 조회/조치 운영자 |
| `/batch-scheduler` | 배치 Scheduler | `batch` | `BatchSchedulerPage.vue` | 배치 조회/조치 운영자 |
| `/batch-worker-pools` | Worker Pool | `batch` | `BatchWorkerPoolsPage.vue` | 배치 조회/조치 운영자 |
| `/batch-center-cut` | Center-Cut | `batch` | `BatchCenterCutPage.vue` | 배치 조회/조치 운영자 |
| `/batch-agents` | 배치 Agent | `batch` | `BatchAgentsPage.vue` | 배치 조회/조치 운영자 |
| `/batch-job-packs` | Job Pack | `batch` | `BatchJobPacksPage.vue` | 배치 조회/조치 운영자 |
| `/batch-executions` | 배치 실행 | `batch` | `BatchExecutionsPage.vue` | 배치 조회/조치 운영자 |
| `/batch-deployment` | 배치 배포 | `batch` | `BatchDeploymentPage.vue` | 배치 조회/조치 운영자 |
| `/batch-recovery` | 배치 복구 | `monitoring` | `BatchRecoveryPage.vue` | 관제/감사 운영자 |
| `/batch-leases` | 배치 Lease | `monitoring` | `BatchLeasesPage.vue` | 관제/감사 운영자 |
| `/batch-alerts` | 배치 알림 | `monitoring` | `BatchAlertsPage.vue` | 관제/감사 운영자 |
| `/batch-audit` | 배치 감사 | `monitoring` | `BatchAuditEvidencePage.vue` | 관제/감사 운영자 |
| `/workers` | Worker | `batch` | `WorkersPage.vue` | 배치 조회/조치 운영자 |
| `/downloads` | 다운로드 | `integration` | `DownloadsPage.vue` | 연계 운영자 |
| `/file-jobs` | 파일 Job | `batch` | `FileJobsPage.vue` | 배치 조회/조치 운영자 |
| `/messages` | 메시지 | `integration` | `MessagesPage.vue` | 연계 운영자 |
| `/codes` | 코드 | `framework` | `CodesPage.vue` | 플랫폼/보안 관리자 |
| `/gateway-dashboard` | Gateway 대시보드 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-servers` | Gateway 서버 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-groups` | Gateway 그룹 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-routes` | Gateway Route | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-security` | Gateway 보안 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-health` | Gateway Health | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-transactions` | Gateway 거래 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-log-policies` | Gateway 로그 정책 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/gateway-apply-status` | Gateway 적용 상태 | `online` | `GatewayOperationsPage.vue` | 조회/조치 운영자 |
| `/permissions` | 권한 | `framework` | `PermissionsPage.vue` | 플랫폼/보안 관리자 |
| `/password` | 비밀번호·세션 | `framework` | `PasswordPage.vue` | 플랫폼/보안 관리자 |
| `/security` | 보안 | `framework` | `SecurityPage.vue` | 플랫폼/보안 관리자 |
| `/operators` | 운영자 | `framework` | `OperatorsPage.vue` | 플랫폼/보안 관리자 |
| `/secrets` | Secret | `framework` | `SecretsPage.vue` | 플랫폼/보안 관리자 |
| `/approvals` | 승인 | `framework` | `ApprovalsPage.vue` | 플랫폼/보안 관리자 |
| `/breakGlass` | 비상 권한 | `framework` | `BreakGlassPage.vue` | 플랫폼/보안 관리자 |

## 4. 메뉴별 상세

### 4.1 대시보드 — `/`

**목적**  
플랫폼 전체 운영 신호, 미처리 승인, 장애·복구 대기 건을 한 화면에서 요약한다.

**Source 근거**

- Route ID: `dashboard`
- Group: `home`
- Component: `cpf-admin/frontend/src/features/.../DashboardPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 이상 지표와 수집 시각 확인
2. Pending Approval·Incident·Reconcile 대기 상세로 이동

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.2 토폴로지 — `/topology`

**목적**  
Environment·Service·Instance·연결 관계와 배포 구성을 조회한다.

**Source 근거**

- Route ID: `topology`
- Group: `home`
- Component: `cpf-admin/frontend/src/features/.../TopologyPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. Service·Instance 연결 확인
2. Version·Config 불일치 대상 분리
3. 장애 영향 경로 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.3 용량 현황 — `/capacity`

**목적**  
CPU·Memory·Thread·Pool·Queue·Disk 등 운영 용량과 임계치를 확인한다.

**Source 근거**

- Route ID: `capacity`
- Group: `home`
- Component: `cpf-admin/frontend/src/features/.../CapacityPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 임계치 초과 Resource 확인
2. Active Workload와 Capacity 상관 분석
3. Scale·Drain 검토 요청

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.4 통합 로그 — `/logs`

**목적**  
서비스·인스턴스·거래 식별자로 마스킹된 로그를 검색한다.

**Source 근거**

- Route ID: `logs`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../LogsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. 거래 ID 우선 검색
2. Trace·Attempt 연결
3. 승인된 Masked Export

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.5 거래 그룹 — `/transactionGroups`

**목적**  
연관 거래를 그룹 단위로 묶어 시작·종료·부분 실패를 분석한다.

**Source 근거**

- Route ID: `transactionGroups`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../TransactionGroupsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. 연관 거래 묶음 확인
2. 부분 실패를 거래별로 분리
3. 대사 후보 생성

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.6 거래 조회 — `/transactions`

**목적**  
transactionId·traceId·operationId 기준으로 거래와 Attempt를 조회한다.

**Source 근거**

- Route ID: `transactions`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../TransactionsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. Attempt·Target·Duration 조회
2. Owner Status 재조회
3. UNKNOWN_RESULT Reconciliation 이동

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.7 원격 로그 — `/remoteLogs`

**목적**  
원격 인스턴스 로그 수집·조회 상태와 누락·지연을 확인한다.

**Source 근거**

- Route ID: `remoteLogs`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../RemoteLogsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. 수집 Agent·Source 상태 재조회
2. 누락 기간과 마지막 수집 시각 확인
3. 승인된 재수집

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.8 감사 로그 — `/auditLogs`

**목적**  
Actor·Action·Target·Reason·Approval·Result 감사 이력을 조회한다.

**Source 근거**

- Route ID: `auditLogs`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../AuditLogsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. Actor·Action·Target 검색
2. Reason·Approval·Result 연결
3. Hash/Chain·Evidence Export

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.9 로그 레벨 — `/logLevel`

**목적**  
승인된 범위에서 Runtime 로그 레벨 변경과 적용 결과를 확인한다.

**Source 근거**

- Route ID: `logLevel`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../LogLevelPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. 현재/목표 Level Preview
2. 승인 요청
3. 적용·ACK/NACK·Rollback

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.10 로그 정책 — `/logPolicies`

**목적**  
마스킹·보존·수집·반출 정책의 Version과 적용 상태를 관리한다.

**Source 근거**

- Route ID: `logPolicies`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../LogPoliciesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. Version Diff
2. 게시
3. Consumer ACK/NACK
4. Rollback·Reconcile

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.11 표준 실행 — `/standardExecutions`

**목적**  
표준 실행 계약과 실행 이력·상태·오류를 조회한다.

**Source 근거**

- Route ID: `standardExecutions`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../StandardExecutionsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 실행 정의·Version 조회
2. Parameter 검증
3. 결과·Error·Attempt 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.12 채널 정책 — `/channelPolicy`

**목적**  
채널별 허용 정책·Header·보안·제한을 조회하고 Version을 관리한다.

**Source 근거**

- Route ID: `channelPolicy`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../ChannelPolicyPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. 채널·Audience·Header 정책 조회
2. Version Diff
3. 승인·게시·적용 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.13 서비스 레지스트리 — `/serviceRegistry`

**목적**  
Service·Instance의 등록, 상태, Version, Drain·Maintenance 정보를 조회한다.

**Source 근거**

- Route ID: `serviceRegistry`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../ServiceRegistryPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. Service·Instance 상세
2. Drain/Resume 요청
3. Maintenance·Version 상태 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.14 Runtime 제어 — `/runtimeControl`

**목적**  
Drain·Resume·재기동 등 승인된 Runtime 제어 요청과 결과를 확인한다.

**Source 근거**

- Route ID: `runtimeControl`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../RuntimeControlPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. Drain
2. Resume
3. Stop/Restart 요청
4. operationId 결과 조회

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.15 유지보수 — `/maintenance`

**목적**  
점검 창, 대상, 작업 상태, 영향 범위와 복귀 조건을 관리한다.

**Source 근거**

- Route ID: `maintenance`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../MaintenancePage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 점검 대상·창 등록
2. Traffic/Trigger 통제
3. 복귀 조건·완료 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.16 캐시 — `/cache`

**목적**  
Cache Provider·Namespace·Hit/Miss·용량·무효화 이력을 조회한다.

**Source 근거**

- Route ID: `cache`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../CachePage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. Provider·Namespace 조회
2. Hit/Miss·용량 판단
3. 승인된 무효화·결과 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.17 설정 — `/configs`

**목적**  
설정 Key·Scope·Version·Consumer ACK/NACK·Drift를 조회한다.

**Source 근거**

- Route ID: `configs`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../ConfigsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. Key·Scope·Version 비교
2. Preview·승인
3. 게시·Consumer ACK/NACK
4. Rollback

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.18 응답 코드 — `/responseCodes`

**목적**  
표준 응답 코드, HTTP 매핑, 사용자 메시지와 운영 조치를 관리한다.

**Source 근거**

- Route ID: `responseCodes`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../ResponseCodesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. Code·HTTP Mapping 조회
2. 사용처 영향 분석
3. Version 변경·게시

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.19 영업일 달력 — `/businessCalendar`

**목적**  
영업일·휴일·Cut-off 기준과 Version을 관리한다.

**Source 근거**

- Route ID: `businessCalendar`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../BusinessCalendarPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. 영업일·휴일 조회
2. Cut-off 영향 확인
3. Version 변경·승인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.20 복구 센터 — `/recoveryCenter`

**목적**  
UNKNOWN_RESULT·재처리·보상·대사 후보를 통합 조회하고 조치한다.

**Source 근거**

- Route ID: `recoveryCenter`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../RecoveryCenterPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. UNKNOWN_RESULT 대사
2. 재처리
3. 보상
4. 운영 결과 확정

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.21 장애 관리 — `/incidents`

**목적**  
Incident의 영향, Timeline, 통제, 복구, 후속조치를 기록한다.

**Source 근거**

- Route ID: `incidents`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../IncidentsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. Incident 생성·영향 기록
2. 즉시 통제
3. Timeline·복구·종료

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.22 신뢰성 — `/reliability`

**목적**  
오류율·지연·재시도·Circuit·SLO 지표와 위험 신호를 확인한다.

**Source 근거**

- Route ID: `reliability`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../ReliabilityPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. SLO·Error·Latency 분석
2. Retry·Circuit 상태 확인
3. 개선 Action 연결

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.23 알림 — `/notifications`

**목적**  
운영 알림 채널·전송 결과·재전송·실패 원인을 조회한다.

**Source 근거**

- Route ID: `notifications`
- Group: `integration`
- Component: `cpf-admin/frontend/src/features/.../NotificationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 채널·수신자·전송 결과 확인
2. 실패 원인 분류
3. 승인된 재전송

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.24 배치 — `/batch`

**목적**  
배치 정의·실행·상태·조치의 통합 진입점이다.

**Source 근거**

- Route ID: `batch`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. 정의·실행 통합 검색
2. 실행 상세 이동
3. Start/Stop/Restart 승인 흐름

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.25 배치 개요 — `/batch-overview`

**목적**  
Job·Execution·Worker·Scheduler·Alert의 핵심 현황을 요약한다.

**Source 근거**

- Route ID: `batch-overview`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchOverviewPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. 실패·지연·Unknown 요약
2. Worker·Scheduler 영향 확인
3. 상세 Route 이동

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.26 배치 Runtime — `/batch-runtime`

**목적**  
Control·Scheduler·Worker·Center-Cut·Agent Runtime 토폴로지를 조회한다.

**Source 근거**

- Route ID: `batch-runtime`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../RuntimeTopologyPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Control·Scheduler·Worker·Agent Topology
2. Version·Heartbeat
3. 격리·복구 대상 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.27 배치 인스턴스 — `/batch-instances`

**목적**  
배치 Runtime Instance의 상태, Version, Heartbeat, Drain을 조회한다.

**Source 근거**

- Route ID: `batch-instances`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchInstancesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Instance 상태·Heartbeat
2. Drain/Resume
3. Version·Capacity 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.28 배치 Scheduler — `/batch-scheduler`

**목적**  
Schedule·Trigger·다음 실행·지연·중복 방지 상태를 관리한다.

**Source 근거**

- Route ID: `batch-scheduler`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchSchedulerPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Schedule 활성/비활성
2. Next Fire·Misfire 확인
3. 중복 Trigger 대사

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.29 Worker Pool — `/batch-worker-pools`

**목적**  
Worker Pool의 용량·Concurrency·Lease·할당 상태를 조회한다.

**Source 근거**

- Route ID: `batch-worker-pools`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchWorkerPoolsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Pool 용량·Concurrency 확인
2. Lease·할당 조회
3. Scale·Drain 검토

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.30 Center-Cut — `/batch-center-cut`

**목적**  
Center-Cut 요청의 승인·실행·중단·재시작·대사를 관리한다.

**Source 근거**

- Route ID: `batch-center-cut`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchCenterCutPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. 승인된 Start
2. Stop
3. Restart
4. Abandon
5. Reconcile

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.31 배치 Agent — `/batch-agents`

**목적**  
Host Agent의 연결·Artifact·Service 제어·로그 수집 상태를 조회한다.

**Source 근거**

- Route ID: `batch-agents`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchAgentsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Agent 연결·mTLS 확인
2. Artifact/Service 상태
3. Log Archive·실패 복구

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.32 Job Pack — `/batch-job-packs`

**목적**  
배포 가능한 Job Pack·Version·Checksum·호환성을 조회한다.

**Source 근거**

- Route ID: `batch-job-packs`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchJobPacksPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Job Pack Version·Checksum
2. 호환성·승인
3. 배포 대상 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.33 배치 실행 — `/batch-executions`

**목적**  
Spring Batch JobInstance·JobExecution·StepExecution을 조회하고 조치한다.

**Source 근거**

- Route ID: `batch-executions`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchExecutionsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. JobInstance·Execution·Step 조회
2. Start/Stop/Restart/Abandon
3. Reconcile

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.34 배치 배포 — `/batch-deployment`

**목적**  
Job Pack과 Runtime Artifact의 Wave 배포·검증·Rollback 상태를 관리한다.

**Source 근거**

- Route ID: `batch-deployment`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../BatchDeploymentPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Wave 배포
2. Probe 확인
3. Selective Rollback
4. Instance별 결과 대사

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.35 배치 복구 — `/batch-recovery`

**목적**  
실패·중단·UNKNOWN_RESULT Execution의 Restart·Recover·Reconcile을 관리한다.

**Source 근거**

- Route ID: `batch-recovery`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../BatchRecoveryPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Restart
2. Recover
3. Reconcile
4. 대체 재처리·보상

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.36 배치 Lease — `/batch-leases`

**목적**  
Lease·Claim·Fencing Token·Takeover·Stale Owner를 조회한다.

**Source 근거**

- Route ID: `batch-leases`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../BatchLeasesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Lease·Token·Owner 조회
2. Stale Owner 확인
3. Takeover 검토

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.37 배치 알림 — `/batch-alerts`

**목적**  
배치 실패·지연·Worker Offline·대사 적체 경보를 처리한다.

**Source 근거**

- Route ID: `batch-alerts`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../BatchAlertsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. 경보 상세·중복 억제
2. Owner·Runbook 연결
3. 해소 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.38 배치 감사 — `/batch-audit`

**목적**  
배치 승인·조치·실행·복구 Evidence를 조회한다.

**Source 근거**

- Route ID: `batch-audit`
- Group: `monitoring`
- Component: `cpf-admin/frontend/src/features/.../BatchAuditEvidencePage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. 승인·Command·Execution 연결
2. Evidence Hash 확인
3. 감사 Export

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.39 Worker — `/workers`

**목적**  
Worker의 상태·처리량·오류·할당·Heartbeat를 조회한다.

**Source 근거**

- Route ID: `workers`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../WorkersPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Worker Heartbeat·Capability
2. 현재 할당·오류
3. Drain·재할당 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.40 다운로드 — `/downloads`

**목적**  
마스킹된 운영 자료 생성·만료·다운로드·감사 상태를 관리한다.

**Source 근거**

- Route ID: `downloads`
- Group: `integration`
- Component: `cpf-admin/frontend/src/features/.../DownloadsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. 생성 요청
2. Hash·마스킹·만료 확인
3. 다운로드
4. 파기·Audit

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.41 파일 Job — `/file-jobs`

**목적**  
입력 파일·Checksum·Archive·처리·재처리 상태를 조회한다.

**Source 근거**

- Route ID: `file-jobs`
- Group: `batch`
- Component: `cpf-admin/frontend/src/features/.../FileJobsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Job/Execution/Step ID
- Definition/Artifact Version
- Worker/Agent
- 진행률·Count
- Fencing/Lease

**운영 절차**

1. Input·Checksum 조회
2. 처리·Output 확인
3. 재처리·Archive 복구

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: JobRepository·CPF Ledger·Worker/Agent 상태를 대사한다.
- Stale Fencing: 과거 Worker 결과를 폐기하고 현재 Lease Owner를 확인한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.42 메시지 — `/messages`

**목적**  
Kafka Message·Offset·Retry·DLT·Inbox/Outbox 상태를 조회한다.

**Source 근거**

- Route ID: `messages`
- Group: `integration`
- Component: `cpf-admin/frontend/src/features/.../MessagesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 검색 조건
- 대상 ID
- 최근 변경
- 오류 Code
- 관련 Detail

**운영 절차**

1. Topic·Partition·Offset 조회
2. Retry/DLT 확인
3. Inbox/Outbox 대사

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.43 코드 — `/codes`

**목적**  
공통 코드·Version·유효기간·사용처를 관리한다.

**Source 근거**

- Route ID: `codes`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../CodesPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Scope
- Current/Target Version
- Consumer ACK/NACK
- Drift
- 변경자/승인

**운영 절차**

1. Code·Version·유효기간 조회
2. 사용처 영향
3. 승인된 변경·게시

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.44 Gateway 대시보드 — `/gateway-dashboard`

**목적**  
선택형 Gateway의 Route·Target·거래·적용 상태를 요약한다.

**Source 근거**

- Route ID: `gateway-dashboard`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. NACK·Unknown·Target Down 확인
2. 관련 Server·Route·Transaction 이동

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.45 Gateway 서버 — `/gateway-servers`

**목적**  
Gateway Instance와 Server Group 상태를 조회한다.

**Source 근거**

- Route ID: `gateway-servers`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. Gateway Instance·Version 조회
2. Drain·Health
3. 적용 상태 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.46 Gateway 그룹 — `/gateway-groups`

**목적**  
Server Group·Member·Load Balance·상태 정책을 관리한다.

**Source 근거**

- Route ID: `gateway-groups`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. Member·Load Balance 조회
2. Drain·Weight 변경 검토
3. Group 상태 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.47 Gateway Route — `/gateway-routes`

**목적**  
Route 정의·Version·Predicate·Target·게시 상태를 관리한다.

**Source 근거**

- Route ID: `gateway-routes`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. Route Version·Diff
2. 승인
3. 게시
4. Rollback

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.48 Gateway 보안 — `/gateway-security`

**목적**  
인증·인가·Header Trust·사유 요구 정책을 조회한다.

**Source 근거**

- Route ID: `gateway-security`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. 인증·인가·Header Trust 확인
2. 사유 요구·Key Rotation 상태
3. 보안 Audit

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.49 Gateway Health — `/gateway-health`

**목적**  
Gateway와 Upstream Target의 Health·Drain·Failure를 확인한다.

**Source 근거**

- Route ID: `gateway-health`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. Gateway·Upstream Probe
2. Down·Draining 분리
3. 복귀 판단

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.50 Gateway 거래 — `/gateway-transactions`

**목적**  
Gateway Transaction·Attempt·Failover·UNKNOWN_RESULT를 조회한다.

**Source 근거**

- Route ID: `gateway-transactions`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- 기간
- Service/Instance
- Level/Result
- 마스킹
- Trace/Attempt

**운영 절차**

1. Attempt·Target·Retry 조회
2. 응답 유실 확인
3. Reconcile

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.51 Gateway 로그 정책 — `/gateway-log-policies`

**목적**  
Gateway 로그·마스킹·보존 정책과 적용 상태를 관리한다.

**Source 근거**

- Route ID: `gateway-log-policies`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. 마스킹·보존 Version
2. 게시·ACK/NACK
3. Rollback

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.52 Gateway 적용 상태 — `/gateway-apply-status`

**목적**  
Route 게시 Version의 Instance별 ACK/NACK·Drift·Reconcile 상태를 조회한다.

**Source 근거**

- Route ID: `gateway-apply-status`
- Group: `online`
- Component: `cpf-admin/frontend/src/features/.../GatewayOperationsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Route/Group/Instance
- 게시 Version
- Target/Health
- ACK/NACK
- Attempt/Unknown

**운영 절차**

1. ACK/NACK 조회
2. Drift 확인
3. Reconcile
4. LKG Rollback

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- UNKNOWN_RESULT: Gateway Transaction·Attempt Ledger와 Upstream 상태를 대사한다.
- Partial Apply: Instance별 ACK/NACK를 확인하고 전체 성공으로 처리하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.53 권한 — `/permissions`

**목적**  
Role·Menu·Button·API 권한 Matrix를 관리한다.

**Source 근거**

- Route ID: `permissions`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../PermissionsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. Role Menu/Button/API Matrix 저장
2. Effective Permission Simulation
3. 감사 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.
- Partial Apply: Consumer별 Version·ACK/NACK를 확인하고 Rollback 또는 Reconcile한다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.54 비밀번호·세션 — `/password`

**목적**  
비밀번호 정책·초기화·잠금 해제·세션 강제 종료를 관리한다.

**Source 근거**

- Route ID: `password`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../PasswordPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. 정책 조회
2. 초기화
3. 잠금 해제
4. Session Revoke

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.55 보안 — `/security`

**목적**  
IP Allowlist·MFA·보안 상태와 조치 이력을 관리한다.

**Source 근거**

- Route ID: `security`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../SecurityPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. IP Allowlist
2. MFA 등록/검증
3. 보안 Event 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.56 운영자 — `/operators`

**목적**  
운영자 계정·Role·상태·연락처 마스킹·활성화를 관리한다.

**Source 근거**

- Route ID: `operators`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../OperatorsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. 등록
2. Role 부여
3. ACTIVE 전환
4. 원문 연락처 승인 조회

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.57 Secret — `/secrets`

**목적**  
Secret Reference·Provider·Rotation·만료 상태를 조회한다.

**Source 근거**

- Route ID: `secrets`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../SecretsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. Reference·Provider 상태 조회
2. Rotation 요청
3. 만료·Provider 장애 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.58 승인 — `/approvals`

**목적**  
위험 조치 요청·검토·승인·반려·만료·실행 결과를 관리한다.

**Source 근거**

- Route ID: `approvals`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../ApprovalsPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. 요청 검토
2. 승인/반려
3. 만료·실행 결과 확인

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)

### 4.59 비상 권한 — `/breakGlass`

**목적**  
비상 권한의 신청·승인·시간 제한·사용·회수·감사를 관리한다.

**Source 근거**

- Route ID: `breakGlass`
- Group: `framework`
- Component: `cpf-admin/frontend/src/features/.../BreakGlassPage.vue`
- Registry: `cpf-admin/frontend/src/app/routes.ts`
- 접근: Menu Catalog + Server Menu/Button/API Permission
- Browser Runtime 검증: `미검증`

**진입 전**

1. Environment·Region을 확인한다.
2. Operator·Role·Permission을 확인한다.
3. 수집 시각·Source Runtime·Stale를 확인한다.
4. 위험 조치에는 Ticket·Reason·Approval을 준비한다.

**조회·판단**

- Environment
- 수집 시각
- 상태
- Version
- Source Runtime
- transactionId/operationId
- Actor/Role
- Permission
- Reason
- Approval
- Audit

**운영 절차**

1. 신청
2. 독립 승인
3. 활성
4. 회수
5. 사용 감사

**조치 전 필수 확인**

- 대상 ID·상태·Version이 Detail과 일치하는가?
- 대체 Capacity·의존 Runtime·진행 중 거래가 있는가?
- Server Permission이 있는가?
- Reason이 Incident/Change와 연결되는가?
- Approval Target·Request Hash·만료·Expected Version이 현재 상태와 일치하는가?
- 결과를 확인할 operationId 또는 transactionId가 있는가?

**정상 결과**

- 접수·진행·완료가 구분된다.
- List와 Detail의 Version·상태가 일치한다.
- Owner Runtime 실제 상태가 일치한다.
- Audit에 Actor·Reason·Approval·Target·Result가 남는다.
- Partial 대상은 Instance·Consumer별로 구분된다.

**오류·부분 실패·복구**

- 401: Session 만료·Environment 주소를 확인하고 재로그인한다.
- 403: 필요한 Menu/Button/API Permission을 확인하고 우회하지 않는다.
- 409: 최신 상태·Version을 다시 읽고 기존 Approval 유효성을 재판정한다.
- 422: Field·Reason·Parameter Schema를 수정한다.
- 500/503: transactionId와 Dependency 상태를 기록하고 Runbook으로 전환한다.
- 응답 유실: 같은 operationId로 Status를 조회하고 새 ID로 Blind Retry하지 않는다.

**Evidence**

- 기준 시각·Environment
- 검색 조건
- 대상 ID·Version
- operationId·transactionId
- Actor·Permission·Reason·Approval
- 실행 전후 상태
- Error Code·Reconcile
- 민감정보 제거 여부

[전체 Route 색인](#3-전체-route-색인)


## 5. 오류별 공통 대응

### 5.1 401

- 공유 계정·자동 재로그인 금지
- Session Timeout·Cookie·CSRF·시간 동기
- 입력 중 Reason·Form 안전 보존
- 재로그인 후 대상 최신 조회

### 5.2 403

- Menu 표시만으로 조치 권한을 가정하지 않는다.
- API Permission·Button ID·Data Scope를 확인한다.
- 승인된 Role 변경을 요청한다.
- Break-glass로 상시 권한 부족을 우회하지 않는다.

### 5.3 409

- Expected/Actual Version
- 다른 운영자·배포·자동 복구
- Approval Snapshot 유효성
- 최신 상태에서 새 요청·재승인

### 5.4 422

- Field Error
- 날짜·Timezone·Enum·Size
- Reason
- 상호 의존 입력

### 5.5 500·503

- transactionId
- 같은 Command 반복 전 Status
- DB·Kafka·Owner·Agent
- Incident 기준
- 복구 후 Reconcile

### 5.6 UNKNOWN_RESULT

```text
새 operationId 생성 금지
→ 기존 operationId Status
→ Owner DB/JobRepository/Ledger/Target
→ 결과 확정
→ 같은 Idempotency Key로 Reconcile
→ Audit
```

## 6. 교대·Evidence

교대:

- 진행 Incident·Change
- Pending Approval
- DRAINING·STOPPING·DEPLOYING
- UNKNOWN_RESULT·Reconcile 대기
- Batch Restart 후보
- Gateway NACK·Drift
- Config Partial Apply
- Download Artifact 만료
- Break-glass 활성

## 7. 역할 Walkthrough

### 조회 운영자

1. Dashboard 이상 신호
2. Topology·Capacity 영향
3. Transactions·Logs·Trace
4. Incident 또는 Escalation
5. Evidence

### 조치 운영자

1. Target 최신 Detail
2. 대체 Capacity·Version
3. Reason·Approval
4. Command·operationId
5. Owner 상태
6. Reconcile
7. Audit

### 승인자

1. 요청자 분리
2. 대상·Environment·Expected Version
3. 영향·Rollback·대사
4. Request Hash·만료
5. 승인 후 실행 결과

### 감사자

1. Actor·Action·Target
2. Reason·Approval·Result
3. 원문 조회·Download Audit
4. Hash/Chain 이상 Security Incident
