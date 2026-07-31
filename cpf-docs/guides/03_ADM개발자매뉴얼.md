# CPF ADM 개발자 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF` · **기준 Branch** `master` · **기준 SHA** `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)  
> **문서 기준일** `2026-07-31` · **Runtime 검증 상태** QA32 전체 Runtime·3DB·Browser·다중 인스턴스 Evidence는 `미검증`  
> **문서 목적** 플랫폼 운영 메뉴를 Backend Owner Port·OpenAPI·Orval·Vue Router·Pinia·TanStack Query·Element Plus·권한·승인·감사·Playwright까지 수직 연결해 개발하는 방법을 제공한다.  
> **주요 독자** ADM Backend·Frontend 개발자, UX 개발자, 플랫폼 Control Plane 개발자, QA  
> **완료 결과** 개발자가 실제 ADM 메뉴 하나를 조회·상세·위험 조치·오류·감사·Browser Test까지 완성한다.

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


## 1. ADM 책임

ADM은 다른 Owner의 DB를 직접 수정하는 만능 관리 Module이 아니다.

```text
ADM Page
→ Generated Client
→ ADM Controller
→ Owner Public Command/Query Port
→ Same-JVM 또는 Remote Adapter
→ Owner Runtime
→ Owner State
→ Result/Audit
```

ADM이 소유:

- 운영 UX
- 검색·Paging·상태 표현
- Permission·Approval·Reason UX
- Command Tracking·UNKNOWN_RESULT UX
- Audit·Evidence 조회
- Control Plane 조정

ADM이 소유하지 않음:

- 업무 상태 정본
- Batch JobRepository
- Gateway Data Plane 처리
- 다른 Module DB 직접 갱신
- OS Service 직접 조작

## 2. 실제 Frontend Stack

| 영역 | 실제 버전 |
|---|---|
| Vue | 3.5.40 |
| Vue Router | 5.2.0 |
| Pinia | 4.0.2 |
| TanStack Vue Query | 5.101.4 |
| TanStack Vue Table | 8.21.3 |
| Element Plus | 2.14.3 |
| Zod | 4.4.3 |
| Orval | 8.23.0 |
| Playwright | 1.62.0 |
| Vite | 8.1.5 |
| TypeScript | 6.0.3 |
| Vitest | 4.1.10 |
| Node/npm | 22.16.0 / 10.9.2 |

```powershell
Push-Location .\cpf-admin\frontend
npm ci
npm run verify:primary
npm run lint
npm run typecheck
npm test
npm run build
npm run test:e2e
npm run test:a11y
Pop-Location
```

## 3. Route Registry

실제 Source:

- `cpf-admin/frontend/src/app/routes.ts`
- `cpf-admin/frontend/src/app/router.ts`

규칙:

- `createWebHistory(import.meta.env.BASE_URL)`
- dashboard path `/`
- 나머지 `/<routeId>`
- 알 수 없는 Path는 dashboard redirect
- 실제 메뉴 노출은 로그인 응답의 Authorized Menu Catalog

### 3.1 전체 Route Inventory

| Route ID | Path | Group | Component |
|---|---|---|---|
| `dashboard` | `/` | `home` | `DashboardPage.vue` |
| `topology` | `/topology` | `home` | `TopologyPage.vue` |
| `capacity` | `/capacity` | `home` | `CapacityPage.vue` |
| `logs` | `/logs` | `monitoring` | `LogsPage.vue` |
| `transactionGroups` | `/transactionGroups` | `online` | `TransactionGroupsPage.vue` |
| `transactions` | `/transactions` | `online` | `TransactionsPage.vue` |
| `remoteLogs` | `/remoteLogs` | `monitoring` | `RemoteLogsPage.vue` |
| `auditLogs` | `/auditLogs` | `monitoring` | `AuditLogsPage.vue` |
| `logLevel` | `/logLevel` | `monitoring` | `LogLevelPage.vue` |
| `logPolicies` | `/logPolicies` | `monitoring` | `LogPoliciesPage.vue` |
| `standardExecutions` | `/standardExecutions` | `online` | `StandardExecutionsPage.vue` |
| `channelPolicy` | `/channelPolicy` | `online` | `ChannelPolicyPage.vue` |
| `serviceRegistry` | `/serviceRegistry` | `online` | `ServiceRegistryPage.vue` |
| `runtimeControl` | `/runtimeControl` | `online` | `RuntimeControlPage.vue` |
| `maintenance` | `/maintenance` | `framework` | `MaintenancePage.vue` |
| `cache` | `/cache` | `framework` | `CachePage.vue` |
| `configs` | `/configs` | `framework` | `ConfigsPage.vue` |
| `responseCodes` | `/responseCodes` | `framework` | `ResponseCodesPage.vue` |
| `businessCalendar` | `/businessCalendar` | `framework` | `BusinessCalendarPage.vue` |
| `recoveryCenter` | `/recoveryCenter` | `monitoring` | `RecoveryCenterPage.vue` |
| `incidents` | `/incidents` | `monitoring` | `IncidentsPage.vue` |
| `reliability` | `/reliability` | `monitoring` | `ReliabilityPage.vue` |
| `notifications` | `/notifications` | `integration` | `NotificationsPage.vue` |
| `batch` | `/batch` | `batch` | `BatchPage.vue` |
| `batch-overview` | `/batch-overview` | `batch` | `BatchOverviewPage.vue` |
| `batch-runtime` | `/batch-runtime` | `batch` | `RuntimeTopologyPage.vue` |
| `batch-instances` | `/batch-instances` | `batch` | `BatchInstancesPage.vue` |
| `batch-scheduler` | `/batch-scheduler` | `batch` | `BatchSchedulerPage.vue` |
| `batch-worker-pools` | `/batch-worker-pools` | `batch` | `BatchWorkerPoolsPage.vue` |
| `batch-center-cut` | `/batch-center-cut` | `batch` | `BatchCenterCutPage.vue` |
| `batch-agents` | `/batch-agents` | `batch` | `BatchAgentsPage.vue` |
| `batch-job-packs` | `/batch-job-packs` | `batch` | `BatchJobPacksPage.vue` |
| `batch-executions` | `/batch-executions` | `batch` | `BatchExecutionsPage.vue` |
| `batch-deployment` | `/batch-deployment` | `batch` | `BatchDeploymentPage.vue` |
| `batch-recovery` | `/batch-recovery` | `monitoring` | `BatchRecoveryPage.vue` |
| `batch-leases` | `/batch-leases` | `monitoring` | `BatchLeasesPage.vue` |
| `batch-alerts` | `/batch-alerts` | `monitoring` | `BatchAlertsPage.vue` |
| `batch-audit` | `/batch-audit` | `monitoring` | `BatchAuditEvidencePage.vue` |
| `workers` | `/workers` | `batch` | `WorkersPage.vue` |
| `downloads` | `/downloads` | `integration` | `DownloadsPage.vue` |
| `file-jobs` | `/file-jobs` | `batch` | `FileJobsPage.vue` |
| `messages` | `/messages` | `integration` | `MessagesPage.vue` |
| `codes` | `/codes` | `framework` | `CodesPage.vue` |
| `gateway-dashboard` | `/gateway-dashboard` | `online` | `GatewayOperationsPage.vue` |
| `gateway-servers` | `/gateway-servers` | `online` | `GatewayOperationsPage.vue` |
| `gateway-groups` | `/gateway-groups` | `online` | `GatewayOperationsPage.vue` |
| `gateway-routes` | `/gateway-routes` | `online` | `GatewayOperationsPage.vue` |
| `gateway-security` | `/gateway-security` | `online` | `GatewayOperationsPage.vue` |
| `gateway-health` | `/gateway-health` | `online` | `GatewayOperationsPage.vue` |
| `gateway-transactions` | `/gateway-transactions` | `online` | `GatewayOperationsPage.vue` |
| `gateway-log-policies` | `/gateway-log-policies` | `online` | `GatewayOperationsPage.vue` |
| `gateway-apply-status` | `/gateway-apply-status` | `online` | `GatewayOperationsPage.vue` |
| `permissions` | `/permissions` | `framework` | `PermissionsPage.vue` |
| `password` | `/password` | `framework` | `PasswordPage.vue` |
| `security` | `/security` | `framework` | `SecurityPage.vue` |
| `operators` | `/operators` | `framework` | `OperatorsPage.vue` |
| `secrets` | `/secrets` | `framework` | `SecretsPage.vue` |
| `approvals` | `/approvals` | `framework` | `ApprovalsPage.vue` |
| `breakGlass` | `/breakGlass` | `framework` | `BreakGlassPage.vue` |

### 3.2 Route 추가

1. Route ID·Owner Capability 확정
2. Menu·Button·API Permission 설계
3. 기능 Directory와 Page 생성
4. `admFeatureRoutes` 등록
5. Generated Client·Query Key 연결
6. Deep Link·Refresh·Back/Forward
7. 권한 없는 직접 URL 차단
8. Browser 3종·접근성

## 4. 권한 모델

### 4.1 로그인

```http
POST /adm/api/auth/login
```

성공 응답 핵심:

- `operator`
- `menus`
- `buttonIds`

Menu 표시와 Command 권한은 다르다. Server가 API Permission·Method Security·Data Scope를 재검증한다.

### 4.2 실제 Permission API

| 기능 | Endpoint |
|---|---|
| Role | `GET /adm/api/permissions/roles` |
| Menu | `GET /adm/api/permissions/menus` |
| Button | `GET /adm/api/permissions/buttons` |
| Menu Matrix | `GET /adm/api/permissions/menu-matrix` |
| Button Matrix | `GET /adm/api/permissions/button-matrix` |
| API Permission | `GET /adm/api/permissions/api-permissions` |
| API Matrix | `GET /adm/api/permissions/api-matrix` |
| Role Menu | `PUT /adm/api/permissions/roles/{roleId}/menus/{menuId}` |
| Role Button | `PUT /adm/api/permissions/roles/{roleId}/buttons/{buttonId}` |
| Role API | `PUT /adm/api/permissions/roles/{roleId}/api-permissions/{apiPermissionId}` |

모든 변경은 Reason과 Audit를 포함한다.

### 4.3 Frontend Fail-closed

```ts
permission(menuId) {
  const found = authorizedMenus.find(menu => (menu.menuId || menu.id) === menuId);
  return found || { readAllowed: false, writeAllowed: false, deleteAllowed: false };
}
```

Frontend는 Button을 숨기거나 비활성화하지만 최종 권한 판정은 Server다.

## 5. Backend Query·Command

### 5.1 Query

- Server Paging
- Sort Allowlist
- Filter Type·기간 상한
- Data Scope
- Masking
- Snapshot 수집 시각
- Source Runtime
- Total Count 비용

### 5.2 Command

Command Request:

```text
operationId
targetId
expectedVersion
reason
approvalId
idempotencyKey
requestHash
```

응답은 Boolean이 아니라 Operation 상태와 추적 ID를 제공한다.

### 5.3 Owner Port

{code("java", r"""public interface RuntimeDrainCommand {
    RuntimeOperation requestDrain(
            RuntimeTarget target,
            long expectedVersion,
            String reason,
            String approvalId,
            String operationId,
            CpfExecutionContext context);
}""")}

- Same-JVM: Owner Bean
- Remote: 표준 Header·Timeout·Error·Status API
- ADM Service는 Owner DB를 직접 Update하지 않는다.

### 5.4 Transaction

- ADM Transaction은 ADM의 요청·감사 Metadata만 소유
- Owner 변경 Transaction은 Owner Runtime
- Remote Command 응답 유실 시 ADM이 임의 Rollback하지 않음
- `operationId`로 결과 조회·Reconcile

## 6. OpenAPI·Orval

OpenAPI 필수:

- Permission
- 대상·상태 전이
- Reason·Approval·Expected Version
- Idempotency
- 202 Operation
- 400·401·403·404·409·422·429·500·503
- UNKNOWN_RESULT·Status/Reconcile
- Masked Example

Client:

```powershell
Push-Location .\cpf-adminrontend
npm run generate:api
npm run verify:generated
Pop-Location
```

Raw `fetch`, 수동 URL, 임의 `any` Cast를 Primary로 남기지 않는다. 인증·CSRF·표준 Error는 공통 Orval Mutator에서 처리한다.

## 7. Frontend 구조

```text
src/
  app/router.ts
  app/routes.ts
  stores/
  shared/orval-mutator.ts
  components/ui/
  features/<feature>/
    <Feature>Page.vue
    components/
    api/
    model/
    validation/
    tests/
```

### 7.1 역할

- Vue Router: URL·Navigation
- Pinia: 로그인 Subject·Environment·UI Preference
- TanStack Query: Server State·Cache·Mutation
- Zod: Form 입력·Cross-field
- Element Plus: Widget
- TanStack Table: Server Paging·Sort·Column
- Orval: OpenAPI Client
- Playwright: Browser E2E

### 7.2 목록

- 검색 Form과 Applied Filter 분리
- URL Query 공유
- Server Paging·Sort
- 수집 시각·Stale
- Loading·Empty·Error·Partial
- Keyboard·ARIA
- PII Masking
- Export Permission

### 7.3 Detail

- 대상 ID·Environment
- Current Version
- Source Runtime
- 상태·원인 Code
- 관련 Transaction·Attempt
- 최근 변경·Audit
- 조치 가능 조건

### 7.4 위험 조치 Dialog

1. 대상·Environment·상태
2. 영향
3. Expected Version
4. Reason·Ticket
5. Approval
6. operationId
7. UNKNOWN_RESULT 조회
8. 복구·Rollback
9. 최종 확인

## 8. Reference 메뉴: Runtime Drain

### 8.1 수직 흐름

```text
Instance List
→ Detail
→ Drain Command
→ Owner Port
→ 202 Operation
→ operationId Status
→ Owner Registry 상태
→ Reconcile
→ Audit
```

실제 URL은 Owner Controller 정본을 사용한다. 존재하지 않는 Endpoint를 문서 편의를 위해 추가하지 않는다.

### 8.2 화면 상태

| 상태 | 화면 동작 |
|---|---|
| READY | 대체 Capacity와 Drain 조건 |
| DRAINING | 진행률·Active Request |
| DRAINED | Resume·배포 가능 |
| CONFLICT | 최신 Version 재조회 |
| FORBIDDEN | Permission 안내 |
| UNKNOWN_RESULT | operationId·Reconcile |
| FAILED | Error Code·Runbook |

### 8.3 응답 유실

현재 운영자 생성 Frontend는 응답이 불명확할 때 같은 `operationId`로 `/adm/api/operators/operations/{operationId}`를 조회한다. 모든 위험 Command에 같은 패턴을 적용한다.

새 Operation ID를 생성해 재요청하지 않는다.

### 8.4 원문 개인정보 조회

실제 Endpoint:

```http
POST /adm/api/operators/{operatorId}/contacts/raw
Content-Type: application/json

{"reason":"INC-2041 운영자 본인 확인"}
```

현재 Frontend는 403·409·503을 구분하고 transactionId를 표시한다. 다른 원문 조회도 별도 Permission·Reason·Audit·짧은 표시 시간을 적용한다.

## 9. 인증·Session

실제 Session 계약:

- Cookie 기본 `CPFSESSION`
- Timeout 기본 30분, 최대 12시간
- HttpOnly
- SameSite Strict 기본, Lax 허용
- Secure는 Production true
- JDBC Session Readiness fail-closed 기본 true
- CSRF Filter
- Browser Token 영구 저장 금지

로그아웃 API:

```http
POST /adm/api/auth/logout
```

Server Session 폐기 실패에도 Browser 민감 상태는 제거한다.

## 10. 오류·UNKNOWN_RESULT

| 상태 | UX |
|---|---|
| 401 | Session 만료·로그인 |
| 403 | 필요한 Permission; 반복 금지 |
| 404 | 대상 삭제/Scope |
| 409 | 최신 상태·Version |
| 422 | Field Error Focus |
| 429 | Retry-After |
| 500 | transactionId·Runbook |
| 503 | Dependency·재시도 조건 |
| UNKNOWN_RESULT | operationId 유지·Status/Reconcile |

위험 조치는 낙관적으로 성공 표시하지 않는다. Server 응답과 Owner 상태를 확인한 뒤 Query Cache를 무효화한다.

## 11. 계정·권한 기능 Reference

현재 `accessMethods.ts`에서 확인되는 흐름:

- 운영자 등록 `POST /adm/api/operators`
- 응답 유실 결과 조회 `GET /adm/api/operators/operations/{operationId}`
- 상태 변경 `PUT /adm/api/operators/{operatorId}/status`
- 비밀번호 정책 `GET /adm/api/operators/password-policy`
- 비밀번호 초기화 `POST /adm/api/operators/{operatorId}/password/reset`
- 잠금 해제 `POST /adm/api/operators/{operatorId}/unlock`
- Session 조회 `GET /adm/api/operators/sessions`
- Session 폐기 `POST /adm/api/operators/sessions/{sessionId}/revoke`
- 만료 Session 정리 `POST /adm/api/operators/sessions/cleanup-expired`
- MFA 등록·검증
- IP Allowlist 조회

운영자 생성 후 Role을 부여하고 ACTIVE로 전환해야 로그인 가능하다는 UX를 제공한다.

## 12. Playwright

필수:

- 로그인 성공·실패·비밀번호 변경 필요
- Menu 권한별 노출
- 직접 URL 403
- Button Permission
- Server Paging·Sort·Filter
- Detail Stale Version
- 정상·409·403·422·500·503
- Response Loss·UNKNOWN_RESULT
- Approval 만료
- Audit Timeline
- Keyboard·Focus·ARIA
- Chromium·Firefox·WebKit

{code("typescript", r"""test("응답 유실 후 operationId로 결과를 확정한다", async ({ page }) => {
  await page.goto("/runtimeControl");
  await page.getByRole("row", { name: /instance-a/ })
    .getByRole("button", { name: "Drain" }).click();
  await page.getByLabel("사유").fill("INC-2041 planned drain");
  await page.getByRole("button", { name: "실행" }).click();

  await expect(page.getByText(/결과 확인 중|UNKNOWN_RESULT/)).toBeVisible();
  await page.getByRole("button", { name: "결과 확인" }).click();
  await expect(page.getByText("DRAINED")).toBeVisible();
});""")}

## 13. 완료 Gate

- Route·Menu·Button·API Permission 일치
- Backend가 Owner Port 사용
- OpenAPI와 Orval 재생성
- Search·Paging·Detail·Command·Audit 연결
- 401·403·409·422·500·503·UNKNOWN_RESULT 표현
- Browser 3종·접근성 Test
- 실제 실행하지 않은 Browser 검증은 `미검증`
