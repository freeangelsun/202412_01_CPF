# CPF ADM 운영자 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF`
> **기준 Branch** `master`
> **기준 Commit** `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
> **문서 목적** ADM Route별 조회·판단·승인·제어·대사·복구와 Permission·Reason·Expected Version·Audit 절차를 설명한다.
> **주요 독자** 플랫폼 운영관리자, 승인자, 감사자, 보안 운영자, 장애 대응 담당자
> **문서 사용 결과** 운영자가 화면의 상태를 실제 Owner 결과와 대조하고 위험 조치·부분 적용·결과 불명을 처리한다.

## 0. 문서 사용 계약

이 문서는 제품 목표, 기준 Commit의 구현, 실제 실행 검증을 분리한다.

- 목표는 구현·검증 여부와 무관한 제품 계약이다.
- 기능 설명은 최신 Source·SQL·API·Config·Frontend·Script·Test의 exact path를 기준으로 한다.
- 적용 환경에서는 Build·DB·Kafka·Browser·다중 인스턴스·장애 시나리오의 실행 결과를 환경 기록에 남긴다.
- Source에 없는 Class·API·Property·Route·Permission·상태를 만들지 않는다.
- 기능 상태와 운영 상태는 Owner가 정의한 실제 상태값과 Terminal 조건을 사용한다.
- 명령 실행 전 Local Working Tree를 확인하고 기존 변경을 보호한다.


## 1. 운영 원칙

- 화면의 `HTTP 200/202`만으로 업무 결과를 확정하지 않는다.
- Owner 상태, Operation ID, Instance ACK, Version·Checksum을 확인한다.
- 위험 조치는 Permission·Reason·Approval·Expected Version·Impact Preview를 확인한다.
- 응답 유실 후 동일 Button을 반복 클릭하지 않고 상태 조회·Reconcile을 사용한다.
- ADM DB를 직접 수정하거나 Browser 개발자 도구로 권한을 우회하지 않는다.

### 1.1 ADM UI 배포 전 확인 절차

운영에 사용할 ADM Bundle은 다음 정보를 함께 제공한다.

1. exact Source SHA, Backend Artifact SHA, OpenAPI Hash, Generated Client Hash, Bundle Hash.
2. `npm ci → verify:lock → verify:installed → lint → typecheck → test → build` 결과.
3. 59개 Router Registry Import·Deep Link·404 결과.
4. Chromium·Firefox·WebKit의 로그인·권한·조회·위험조치·오류 결과.
5. Privileged API의 미인증 401·무권한 403·CSRF·Origin 결과.

운영자는 배포된 Bundle의 Manifest와 Backend·DB·Config Version을 대조한 뒤 이 매뉴얼의 Route별 절차를 수행한다.

## 2. Route Inventory

Source: `cpf-admin/frontend/src/app/routes.ts`. 아래 Field·Column·Button·Permission·API를 Route별 Browser Scenario와 함께 운영한다.

| 그룹 | Menu ID | Route | Component | 운영 참조 |
|---|---|---|---|---|
| 홈 | `dashboard` | `/` | `DashboardPage.vue` | 제3부 기능 카드 |
| 홈 | `topology` | `/topology` | `TopologyPage.vue` | 제3부 기능 카드 |
| 홈 | `capacity` | `/capacity` | `CapacityPage.vue` | 제3부 기능 카드 |
| 온라인 | `transactionGroups` | `/transactionGroups` | `TransactionGroupsPage.vue` | 제3부 기능 카드 |
| 온라인 | `transactions` | `/transactions` | `TransactionsPage.vue` | 제3부 기능 카드 |
| 온라인 | `standardExecutions` | `/standardExecutions` | `StandardExecutionsPage.vue` | 제3부 기능 카드 |
| 온라인 | `channelPolicy` | `/channelPolicy` | `ChannelPolicyPage.vue` | 제3부 기능 카드 |
| 온라인 | `serviceRegistry` | `/serviceRegistry` | `ServiceRegistryPage.vue` | 제3부 기능 카드 |
| 온라인 | `runtimeControl` | `/runtimeControl` | `RuntimeControlPage.vue` | 제3부 기능 카드 |
| 배치 | `batch` | `/batch` | `BatchPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-overview` | `/batch-overview` | `BatchOverviewPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-runtime` | `/batch-runtime` | `RuntimeTopologyPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-instances` | `/batch-instances` | `BatchInstancesPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-scheduler` | `/batch-scheduler` | `BatchSchedulerPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-worker-pools` | `/batch-worker-pools` | `BatchWorkerPoolsPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-center-cut` | `/batch-center-cut` | `BatchCenterCutPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-agents` | `/batch-agents` | `BatchAgentsPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-job-packs` | `/batch-job-packs` | `BatchJobPacksPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-executions` | `/batch-executions` | `BatchExecutionsPage.vue` | 제3부 기능 카드 |
| 배치 | `batch-deployment` | `/batch-deployment` | `BatchDeploymentPage.vue` | 제3부 기능 카드 |
| 배치 | `workers` | `/workers` | `WorkersPage.vue` | 제3부 기능 카드 |
| 배치 | `file-jobs` | `/file-jobs` | `FileJobsPage.vue` | 제3부 기능 카드 |
| 연계 | `notifications` | `/notifications` | `NotificationsPage.vue` | 제3부 기능 카드 |
| 연계 | `downloads` | `/downloads` | `DownloadsPage.vue` | 제3부 기능 카드 |
| 연계 | `messages` | `/messages` | `MessagesPage.vue` | 제3부 기능 카드 |
| 관제 | `logs` | `/logs` | `LogsPage.vue` | 제3부 기능 카드 |
| 관제 | `remoteLogs` | `/remoteLogs` | `RemoteLogsPage.vue` | 제3부 기능 카드 |
| 관제 | `auditLogs` | `/auditLogs` | `AuditLogsPage.vue` | 제3부 기능 카드 |
| 관제 | `logLevel` | `/logLevel` | `LogLevelPage.vue` | 제3부 기능 카드 |
| 관제 | `logPolicies` | `/logPolicies` | `LogPoliciesPage.vue` | 제3부 기능 카드 |
| 관제 | `recoveryCenter` | `/recoveryCenter` | `RecoveryCenterPage.vue` | 제3부 기능 카드 |
| 관제 | `incidents` | `/incidents` | `IncidentsPage.vue` | 제3부 기능 카드 |
| 관제 | `reliability` | `/reliability` | `ReliabilityPage.vue` | 제3부 기능 카드 |
| 관제 | `batch-recovery` | `/batch-recovery` | `BatchRecoveryPage.vue` | 제3부 기능 카드 |
| 관제 | `batch-leases` | `/batch-leases` | `BatchLeasesPage.vue` | 제3부 기능 카드 |
| 관제 | `batch-alerts` | `/batch-alerts` | `BatchAlertsPage.vue` | 제3부 기능 카드 |
| 관제 | `batch-audit` | `/batch-audit` | `BatchAuditEvidencePage.vue` | 제3부 기능 카드 |
| 프레임워크 | `maintenance` | `/maintenance` | `MaintenancePage.vue` | 제3부 기능 카드 |
| 프레임워크 | `cache` | `/cache` | `CachePage.vue` | 제3부 기능 카드 |
| 프레임워크 | `configs` | `/configs` | `ConfigsPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `responseCodes` | `/responseCodes` | `ResponseCodesPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `businessCalendar` | `/businessCalendar` | `BusinessCalendarPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `codes` | `/codes` | `CodesPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `permissions` | `/permissions` | `PermissionsPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `password` | `/password` | `PasswordPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `security` | `/security` | `SecurityPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `operators` | `/operators` | `OperatorsPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `secrets` | `/secrets` | `SecretsPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `approvals` | `/approvals` | `ApprovalsPage.vue` | 제3부 기능 카드 |
| 프레임워크 | `breakGlass` | `/breakGlass` | `BreakGlassPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-dashboard` | `/gateway-dashboard` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-servers` | `/gateway-servers` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-groups` | `/gateway-groups` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-routes` | `/gateway-routes` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-security` | `/gateway-security` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-health` | `/gateway-health` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-transactions` | `/gateway-transactions` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-log-policies` | `/gateway-log-policies` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |
| Gateway | `gateway-apply-status` | `/gateway-apply-status` | `GatewayOperationsPage.vue` | 제3부 기능 카드 |

## 3. 모든 화면의 공통 확인표

| 항목 | 운영 확인 |
|---|---|
| Permission | 메뉴·Route·조회·조치·Raw·Export 권한 분리 |
| 검색 | Field, Default, Timezone, Paging, Sort, Reset |
| 결과 | Column, Masking, Empty, Loading, Partial, Stale 시각 |
| 상세 | ID, Owner, Version, Desired/Actual, Error, Audit Link |
| 조치 | Button 활성 조건, Input Validation, Reason, Approval, Expected Version |
| 결과 불명 | Operation ID, Attempt, 상태 조회, Reconcile 기한 |
| 부분 적용 | Instance별 ACK/NACK, Version·Checksum, Traffic 제외, Retry 대상 |
| 복구 | Retry, Restart, Reprocess, Reconcile, Compensation, Rollback |
| 감사 | Operator, Permission, Reason, Approval, Before/After, 결과 |

## 4. 조회 절차

1. Environment·Service·Instance·시간 범위를 선택한다.
2. 기본 Timezone과 포함/제외 상태를 확인한다.
3. Paging·Sort를 고정하고 Search를 실행한다.
4. 결과 생성 시각과 Stale 여부를 확인한다.
5. 상세에서 Owner·Version·Trace·Transaction·Attempt를 연결한다.
6. Raw Data는 별도 Permission·Reason이 있을 때만 조회한다.

## 5. 변경 조치 절차

1. Target 상태·Version·Owner를 재조회한다.
2. 영향 Instance·사용자·거래·대체 경로를 Preview한다.
3. Reason을 구체적으로 입력한다.
4. Approval 요구 여부와 유효기간·자기승인 금지를 확인한다.
5. Expected Version과 Request Hash를 확인한다.
6. Command 전송 후 Operation ID를 기록한다.
7. 최종 상태 또는 `UNKNOWN_RESULT`를 확인한다.
8. Partial이면 Failed Instance만 Retry하거나 Rollback한다.
9. Audit와 Metric이 연결됐는지 확인한다.

## 6. 온라인 운영

### 거래·표준 실행

- transactionId·traceId·segment·attempt
- Caller·Principal·Channel·Environment
- 시작·종료·상태·오류 단계
- Remote Side Effect와 Unknown 여부
- Reconcile·Compensation 결과

### Service Registry·Runtime Control

- Instance ID·Version·Build SHA·Zone
- Liveness·Readiness·Maintenance·Draining
- Lease 만료·Last heartbeat
- Start/Stop/Restart 권한·Reason·Approval
- Stale Instance·Split brain·Partial response

## 7. Batch 운영

실행 전: Definition Version·Artifact Checksum·Schedule·Parameter·Dry Run·Approval·Idempotency·Fencing.

실행 중: CPF Execution ID, Spring Job/Step ID, 처리·Skip·Error 건수, Checkpoint, Worker·Partition, Lease·Heartbeat.

조치:

- Stop: 실제 Step 종료와 Resource 정리 확인.
- Restart: Restart 가능 상태·Checkpoint·최신 Fencing 확인.
- Abandon: 재실행 금지 영향과 승인 확인.
- Reprocess: Failed-only 대상·중복 안전성·대사 기준 확인.
- Reconcile: CPF Control·Spring Metadata·업무 Item을 대조.

## 8. Gateway 운영

Gateway 9개 Route는 현재 같은 Page Component를 사용한다. 메뉴 선택 후 화면 Mode와 API가 Route ID에 맞는지 확인한다.

### Route 게시

1. Ingress Host·Method·Path·Version 확인.
2. Target Service·Rewrite·Timeout·Retry 정책 확인.
3. Collision·Connection·Application Probe 실행.
4. Security·SSRF·Header·CORS 검증 결과 확인.
5. Approval과 Version·Checksum 확인.
6. Publish 후 Instance별 ACK/NACK 확인.
7. Smoke 요청과 Attempt Ledger 확인.

### Partial Apply

- 일부 ACK는 Success가 아니다.
- NACK Instance를 Traffic에서 제외했는지 확인한다.
- Candidate와 Last Known Good를 비교한다.
- Retry 또는 Rollback 후 모든 Instance Checksum을 다시 확인한다.

## 9. Config·Secret·Log Policy

- Config 변경은 Scope·Type·Default·Restart 필요·Secret 여부를 확인한다.
- Partial Apply와 Instance Drift를 확인한다.
- Secret 원문을 화면·Audit·Export에 남기지 않는다.
- Log Level 상향은 기간·대상·Cardinality·PII 영향과 자동 원복 대신 명시적 만료·확인을 사용한다.

## 10. Recovery Center·Incident

1. Alert의 Service·Instance·Transaction·Runbook을 확인한다.
2. 최근 배포·Config·DB Migration·Kafka Lag를 대조한다.
3. 영향 범위와 데이터 불일치 가능성을 분류한다.
4. Mitigation과 Recovery를 구분한다.
5. Unknown·Pending·Orphan을 대사한다.
6. 정상화 지표와 업무 대사를 확인한다.
7. Incident Timeline·조치·승인·결과를 남긴다.

## 11. Security·Approval·Break-glass

- Operator·Role·Permission·Data Scope를 확인한다.
- Session 만료·권한 회수 후 재인증을 확인한다.
- Break-glass는 별도 권한·TTL·긴급 사유·승인 또는 사후 검토가 필요하다.
- Password·Secret·Raw Data·Export 조치는 이중 확인과 감사 링크를 요구한다.

## 12. 응답 유실 처리

1. Browser 재전송을 중지한다.
2. Network 오류 시각·Operation ID·Target을 기록한다.
3. Owner 상태 조회 API를 실행한다.
4. Side Effect 전 실패인지, 처리 후 응답 유실인지 분류한다.
5. `UNKNOWN_RESULT`면 Reconcile 담당·기한을 지정한다.
6. 결과 확정 후 Retry·Compensation·운영 확정을 수행한다.

## 13. Rollback 판정

Rollback 전 확인:

- 현재 Version과 Last Known Good
- DB·Message·Config Compatibility
- 진행 중 거래·Batch·Session
- 일부 Instance만 적용됐는지
- Rollback 자체 Idempotency·Unknown 처리

Rollback 후 Health·Smoke·Metric·Data Reconcile·Audit를 확인한다.

## 14. Browser 검증 체크

- Chromium·Firefox·WebKit
- 권한별 Menu·Route·Button
- 검색·Paging·Sort·Timezone
- Loading·Empty·Error·Retry
- Duplicate click·Timeout·Response loss
- Version conflict·Approval expiry
- Partial Apply·Reconcile·Rollback
- Keyboard·Focus·Label·Contrast

## 15. 적용 환경 확인 항목

- 59개 Route의 Registry와 Component Source, 화면 Field·Column·Button·Permission 조건을 정적 대조했다.
- `/logs`는 표준 Log 조회, 검색, 상세, 연관 Transaction 이동을 제공한다.
- 화면 Source 대조는 Browser 동작 증거를 대신하지 않는다. Chromium·Firefox·WebKit, Owner API, Audit, 다중 인스턴스·Fault는 환경별로 실행해 확인한다.

## 부록 A. 권한 판정 방식

ADM Frontend는 로그인 응답의 `menus`와 `buttonIds`를 사용한다.

- Menu 권한: `readAllowed`, `writeAllowed`, `deleteAllowed`
- Button 권한: 서버가 전달한 Button ID 목록
- API 권한: 별도 API Permission Matrix
- 원문 개인정보: 일반 조회와 분리된 Permission·Reason

Menu가 보이지 않는다고 API가 차단됐다고 가정하지 않고, 반대로 Menu가 보여도 Command 권한이 있다고 가정하지 않는다. 권한 변경 후 기존 Session에 반영되는 시점과 강제 재로그인 정책을 확인한다.

## 부록 B. Permission 화면 운영 절차

### B.1 조회

1. `/permissions` 진입 후 Role·Menu·Button·API Permission 목록을 모두 조회한다.
2. Role ID와 대상 Menu/Button/API ID를 확인한다.
3. 현재 Matrix와 적용 Environment를 기록한다.
4. 변경 대상 사용자의 다른 Role과 Data Scope를 함께 확인한다.

### B.2 변경

| 대상 | 입력 | API |
|---|---|---|
| Menu | roleId, menuId, readYn, writeYn, deleteYn, reason | `PUT /adm/api/permissions/roles/{roleId}/menus/{menuId}` |
| Button | roleId, buttonId, allowYn, reason | `PUT /adm/api/permissions/roles/{roleId}/buttons/{buttonId}` |
| API | roleId, apiPermissionId, allowYn, reason | `PUT /adm/api/permissions/roles/{roleId}/api-permissions/{apiPermissionId}` |

변경 후 같은 Role의 Test Operator로 재인증하고 Menu·Direct URL·Button·API를 각각 확인한다. 권한 축소는 기존 Session Cache·Token·Server Session에 남지 않는지 확인한다.

## 부록 C. 운영자·Password·Session 화면

### C.1 운영자 등록

필수 입력: Operator ID, 이름, 초기 Password, Reason. 연락처는 입력 여부와 Masking 정책을 확인한다. 화면이 만든 `operationId`를 기록한다.

1. 등록 요청 후 `operatorId`가 반환되면 목록을 재조회한다.
2. 응답이 없으면 Button을 다시 누르지 않는다.
3. `/adm/api/operators/operations/{operationId}`로 결과를 조회한다.
4. Role을 부여하고 Expected Version을 사용해 `ACTIVE`로 변경한다.
5. 최초 Login·Password 변경 요구·Audit를 확인한다.

### C.2 상태 변경

`ACTIVE` 전환은 `expectedVersion`과 Reason을 사용한다. `409`가 발생하면 최신 목록을 조회하고 기존 Version으로 반복 전송하지 않는다.

### C.3 원문 연락처

- 일반 목록은 Masking 결과만 사용한다.
- 원문 조회는 대상 Operator와 Reason을 다시 확인한다.
- `403`: 원문 권한 없음
- `409`: 대상 Version 변경, 목록 재조회
- `503`: Permission·Audit·DB 저장소 장애
- 원문 값은 Ticket·Chat·Screenshot·Export에 복사하지 않는다.

### C.4 Password·Session

- Password 초기화 후 `forceChange` 정책을 확인한다.
- 잠금 해제 전에 실패 횟수·공격 여부·MFA 상태를 확인한다.
- Session 폐기는 대상 Operator·Session ID·최근 활동·Reason을 확인한다.
- 만료 Session 일괄 정리는 실행 건수와 오류 건수를 Audit로 확인한다.

## 부록 D. Security 화면

| 기능 | 운영 입력 | 확인 결과 |
|---|---|---|
| IP Allowlist | Network/CIDR, Environment, Reason | 현재 접속 경로가 차단되지 않는지 Preview |
| MFA 등록 | operatorId, secretRef, reason | Secret 원문 미노출, 등록 상태 |
| MFA 검증 | operatorId, otpCode, reason | 검증 결과·실패 횟수·Audit |
| Break-glass | 대상, TTL, 긴급 사유, 승인 | 만료·사후 Review·권한 회수 |
| Secret | Reference, Version, Scope | 값 원문 대신 Metadata·Rotation 상태 |

MFA Secret은 화면 입력 문자열이 아니라 승인된 Secret Reference를 사용한다. OTP 값은 Log·Audit Detail에 남지 않아야 한다.

## 부록 E. 화면 상태별 운영 행동

| 화면 상태 | 의미 | 운영 행동 |
|---|---|---|
| Loading | 요청 진행 중 | 중복 Click 금지 |
| Empty | 조회 결과 없음 | Filter·권한·Environment·시간 범위 확인 |
| Stale | 갱신 시각 경과 | 변경 조치 전 Refresh |
| Partial | 일부 Source/Instance만 응답 | 누락 Instance 식별, 전체 성공 처리 금지 |
| Conflict | Version·상태 변경 | 최신 상세 조회 |
| Unknown | Side Effect 결과 불명 | Operation 상태 조회·Reconcile |
| Rejected | 권한·검증·승인 거부 | 입력·권한·Approval 수정 후 새 요청 |
| Failed | 결과가 실패로 확정 | Retry 가능성·보상·Rollback 판단 |

## 부록 F. Batch 화면 공통 Column과 판정

Batch 관련 Route에서 최소한 다음 값을 서로 연결한다.

- Job ID·Definition Version·Checksum
- CPF Execution ID
- Spring JobInstance ID·JobExecution ID·StepExecution ID
- Schedule ID·Scheduled Time·Business Date
- Worker·Partition·Lease·Fencing Token
- Read·Write·Skip·Error Count
- Start·Last Update·End Time
- Status·Exit Code·Failure Stage·Unknown Reason

Stop·Restart·Abandon·Reprocess Button 활성 조건이 Spring Batch 상태와 일치하지 않으면 조치를 중지한다.

## 부록 G. Gateway 화면 공통 판정

9개 Gateway Route가 같은 Component를 사용하므로 Route 이름만 보고 기능이 분리됐다고 판단하지 않는다. 화면 Mode가 다음 데이터와 API를 바꾸는지 확인한다.

- Dashboard: Traffic·Error·Latency·Instance 적용 상태
- Servers/Groups: Instance·Zone·Weight·Maintenance·Draining
- Routes: Host·Method·Path·Version·Target·Rewrite·Policy
- Security: Auth·Permission·Header·CORS·SSRF 정책
- Health: DNS·TCP·TLS·HTTP·Application Probe
- Transactions: Transaction·Attempt·Unknown·Reconcile
- Log Policies: Masking·Sampling·Retention
- Apply Status: Candidate·Version·Checksum·ACK/NACK·LKG

## 부록 H. 교대 인계 체크

- 미확정 `UNKNOWN_RESULT`와 담당자·대사 기한
- Partial Apply Instance와 Traffic 제외 여부
- 진행 중 Batch·Deployment·Rollback
- 만료 예정 Approval·Break-glass·Secret·Certificate
- 최근 Config·Log Level·Route 변경과 원복 기준
- Incident 상태·업무 영향·정상화 판정 Metric
- 다음 교대가 재현할 Query·Operation ID·Audit Link

---

## 기준 Source와 역할별 활용 범위

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- 문서 표준: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 제품 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 사실 우선순위: 실제 Source·SQL·API·Config·Frontend·Script·Test → Architecture·Specification → 이 매뉴얼

이 매뉴얼의 대상 역할은 다음 흐름을 다른 사람의 구두 설명이나 Source 역분석 없이 수행할 수 있어야 한다.

```text
업무 목적 파악
→ 선행 조건과 권한 준비
→ 실제 기능 위치 탐색
→ 등록·설정·개발·실행
→ 상태와 결과 확인
→ 오류·동시성·부분 실패 판단
→ Retry·Restart·Reprocess·Reconcile·Compensation·Rollback
→ Log·Metric·Trace·Audit·Evidence 확인
→ 정상화와 완료 판정
```

기능 설명은 Source의 실제 계약과 사용 절차를 기준으로 하며, 적용 전 확인 조건·오류·복구 경로를 함께 제공한다.

---

## 제2부 실무편: ADM 운영자가 모든 화면과 기능을 사용하는 절차

## 16. 로그인·Session·권한 선행 절차

1. `/adm/api/auth/login`으로 로그인한다.
2. 응답의 운영자 ID, Menu Permission, Button ID가 로드됐는지 확인한다.
3. `passwordChangeRequired`이면 다른 화면을 조작하지 않고 본인 Password 변경을 수행한 뒤 새 Password로 다시 로그인한다.
4. Menu Read Permission이 없으면 Route를 직접 입력해 우회하지 않는다.
5. Button이 보이지 않더라도 API가 403을 반환하는지 배포 전 Negative Test로 확인한다.
6. Session Cookie·CSRF·Trusted Origin이 정상인지 확인한다.


## 17. 모든 화면 공통 조작 규칙

모든 검색 Form과 조치 Form은 **기본값**을 먼저 확인한다. 기본 기간·환경·상태·Paging·Sort가 업무 범위를 축소할 수 있으므로 조회 전 화면 표시값과 API 요청값을 대조하고, 초기화 후 다시 조회해 차이를 확인한다.

| 상황 | 운영 행동 |
|---|---|
| Loading | 중복 조작 금지. 이전 결과를 최신 결과로 해석하지 않는다. |
| Empty | HTTP 성공·Partial/Stale Flag 없음·Filter 확인 뒤 0건으로 판정한다. |
| 401 | Session 만료/CSRF/Origin 확인 후 로그인. 동일 Command 자동 Replay 금지. |
| 403 | Menu·Button·API·Owner Permission과 Data Scope를 확인한다. |
| 409 | `expectedVersion` 충돌. 최신 상태 재조회 후 변경 의도를 재확인한다. |
| 429 | Rate Limit과 Retry-After 확인. 비멱등 조치 자동 Retry 금지. |
| 503 Partial/Stale | 정상 Empty로 해석하지 않는다. 실패 Owner와 조회 범위를 표시한다. |
| Timeout/응답 유실 | 기존 Operation/Transaction ID로 결과 조회. 신규 ID 재실행 금지. |
| PARTIAL_FAILED | Target별 성공·실패·미응답을 분리하고 Reconcile/Rollback한다. |
| UNKNOWN_RESULT | Owner DB·상대 시스템·Ledger·Audit를 대사해 결과 확정 전 Retry 금지. |

## 18. ADM Route·화면 전수 Inventory

아래 표는 기준 Commit의 `cpf-admin/frontend/src/app/routes.ts`와 실제 Component를 대조한 운영 기준이다.
표의 개별 상태는 Component·API 기능의 구현 상태이며, 위 Frontend Build·Generated Client·Router·Browser 검사를 통과한 Bundle을 배포한다.

| Route ID | URL | Group | 목적 | 검색·입력과 Default | Column·상세 | Button·조치 | Permission | 오류·복구 핵심 | Source | 상태 |
|---|---|---|---|---|---|---|---|---|---|---|
| dashboard | / | 홈 | 운영 대시보드 | 초기 데이터 자동 조회 | 등록 인스턴스·정상 수, 비정상 Health, 결과 미확정, DLQ, 서비스 상태, 최근 Service Call | 새로고침 | 조회 권한 | Loading/Empty/Error | `cpf-admin/frontend/src/features/dashboard/DashboardPage.vue` | 제3부 기능 카드 |
| topology | /topology | 홈 | 서비스 토폴로지 | 없음 | Service ID·명, Instance ID·명, Endpoint, Weight, Status | 새로고침 | 조회 권한 | Registry 0건 Empty | `.../features/topology/TopologyPage.vue` | 제3부 기능 카드 |
| capacity | /capacity | 홈 | 용량·SLO 기본 Signal | 없음 | 최근 호출, 평균 지연, 실패율, 인스턴스; Service/Endpoint/Status/Latency/Transaction | 새로고침 | 조회 권한 | 장기 Percentile·Forecast는 Metrics Backend와 함께 확인 | `.../features/capacity/CapacityPage.vue` | 제3부 기능 카드 |
| logs | /logs | 통합 관제 | 로그 조회 | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 표준 로그 조회 화면 | `.../features/logs/LogsPage.vue` | 사용 절차 참조 |
| transactionGroups | /transactionGroups | 온라인 운영 | 거래 그룹·구간 추적 | 기간, Transaction/Segment, Status, 실패, Module/Source/Target/Role/Direction, 고객·회원·사용자·운영자, Channel, 외부기관/거래, API/거래명/오류, Duration, Header 검색 | 거래/모듈 흐름/시간/소요/상태/실패/Masked 고객·회원/Channel/외부 연계 | 조회·초기화·정렬·Paging·상세 Tab | 거래 조회 Permission·Data Scope | Authorization/API Key/Token 등 원문 미표시 | `.../features/transaction-groups/TransactionGroupsPage.vue` | 제3부 기능 카드 |
| transactions | /transactions | 온라인 운영 | 거래 Metadata | Module 기본 ADM, Active Y, Transaction ID, 선택 ID, Reason | Pretty Result | 조회·재스캔·비활성화 | `TRANSACTION_META` Write for mutation | 재스캔/비활성화 응답 유실 시 Transaction ID 대사 | `.../features/transactions/TransactionsPage.vue` | 제3부 기능 카드 |
| standardExecutions | /standardExecutions | 온라인 운영 | 표준 실행 Catalog | 유형 ONLINE/BATCH, Owner Domain, Keyword | ID, 유형, 실행명, Owner, Source Module, Endpoint | 조회·상세 | 조회 권한 | Catalog/Source 불일치 조사 | `.../features/standard-executions/StandardExecutionsPage.vue` | 제3부 기능 카드 |
| channelPolicy | /channelPolicy | 온라인 운영 | Channel·거래 정책 Snapshot | Channel/Policy Form; Package JSON; Import Dry Run | Channel 인증·서명·신뢰·Version; 정책 허용·TPS·Version | 조회·Snapshot 갱신·Package 반출/반입·Channel/Policy 저장 | `CHANNEL_POLICY` Write | Snapshot Version·Import Dry Run·부분 적용 확인 | `.../features/channel-policy/ChannelPolicyPage.vue` | 제3부 기능 카드 |
| serviceRegistry | /serviceRegistry | 온라인 운영 | Service·Endpoint·Instance·Health·Routing | Service ID, Endpoint, Instance Status; 각 등록 Form | Service/Endpoint/Instance/Health/Routing/Circuit/Call | 등록·수정·Drain·Resume·Disable·새로고침 | `SERVICE_REGISTRY` Write | Version·Heartbeat·Draining·Maintenance·Health 분리 | `.../features/service-registry/ServiceRegistryPage.vue` | 제3부 기능 카드 |
| runtimeControl | /runtimeControl | 온라인 운영 | Runtime 변경 Control Plane | Operation/Change/Target/Expected Version/Rollout/Approval/Payload/Reason | Readiness, Pending, Poison, Drift; ACK/Failed/Drift/Hash | Target/Diff Preview·생성·조회·Audit 검증·Cancel·Exact Rollback·Group CRUD | Runtime Control Permission + Approval/Break-glass | UNKNOWN/PARTIAL/Drift를 성공으로 처리 금지 | `.../features/runtime-control/RuntimeControlPage.vue` | 제3부 기능 카드 |
| maintenance | /maintenance | 프레임워크 | 점검·Drain 제어 | Service, Endpoint, Instance, DRAIN/DISABLE/RESUME, Reason | 시간, Service, Instance, Action, Result, Reason | 명령 실행·조회 | Owner Command Permission | Routing 제외 영향·Audit 확인 | `.../features/maintenance/MaintenancePage.vue` | 제3부 기능 카드 |
| cache | /cache | 프레임워크 | Cache 조회·Evict·Reconcile | Tenant, Namespace, Key, Version, Reason | Cache Summary/Result | Target 갱신·Key/Namespace Evict·Durable Reconcile | Button Permission `CACHE_*` | Cache는 정본 아님; Reconcile 뒤 Owner 확인 | `.../features/cache/CachePage.vue` | 제3부 기능 카드 |
| configs | /configs | 프레임워크 | 설정 관리 | Config ID/Key/Value/Type/Encrypted YN/Reason | Pretty Result | 조회·등록·수정 | `CONFIG` Write | Secret 원문을 일반 Config에 저장 금지 | `.../features/configs/ConfigsPage.vue` | 제3부 기능 카드 |
| responseCodes | /responseCodes | 프레임워크 | 응답코드 관리 | Response/Message Code, S/E, Module, Group, Sequence, HTTP, Reason | Pretty Result | 조회·등록·수정·삭제 | `RESPONSE_CODE` Write/Delete | Consumer·Message Mapping 영향 확인 | `.../features/response-codes/ResponseCodesPage.vue` | 제3부 기능 카드 |
| businessCalendar | /businessCalendar | 프레임워크 | 영업일·휴일 Override | Calendar DEFAULT, Date, Business/Holiday, Day Type, Institution, Business/Audit Reason | Date, Type, Institution, Reason, Version | 조회·저장·삭제 | Menu Write/Delete + Writable Provider | Expected Version 409 충돌 재조회 | `.../features/business-calendar/BusinessCalendarPage.vue` | 제3부 기능 카드 |
| codes | /codes | 프레임워크 | 공통 코드 | Code ID, Parent ID, Key, Value, Description, Reason | Pretty Result | 조회·등록·수정 | `CODE` Write | Parent 순환·Consumer Cache 갱신 확인 | `.../features/codes/CodesPage.vue` | 제3부 기능 카드 |
| messages | /messages | 연계 관리 | 다국어 Message | Message ID/Code/Locale/External/Internal/Reason | Pretty Result | 조회·등록·수정 | `MESSAGE` Write | External/Internal 노출 범위 분리 | `.../features/messages/MessagesPage.vue` | 제3부 기능 카드 |
| remoteLogs | /remoteLogs | 통합 관제 | 원격 Log Artifact | 환경/Module/Service/Instance/Type/File/표준 ID/Transaction/Batch IDs/기간/Size/압축/활성/Lines/Keyword/Reason | Artifact Metadata·Preview·Bundle Job·Diagnostics | 조회·단건/선택/비동기 ZIP·상태·Download·진단 | `REMOTE_LOG` Write for download | Retention·Size·Masking·Download Audit | `.../features/remote-logs/RemoteLogsPage.vue` | 제3부 기능 카드 |
| auditLogs | /auditLogs | 통합 관제 | Audit 조회·Delivery 복구 | Operator, Action, Target Type/ID; Delivery Status, Retry Reason | Audit Result; Delivery ID/Status/Attempt/Error | 조회·Delivery 조회·재처리 | `AUDIT_LOG` Write for retry | 업무 결과와 Audit Delivery 분리 | `.../features/audit-logs/AuditLogsPage.vue` | 제3부 기능 카드 |
| logLevel | /logLevel | 통합 관제 | Dynamic Log Level | Business Transaction ID, Transaction ID, DEBUG/INFO/TRACE, TTL, Reason | Rule Result | 조회·등록 | `DYNAMIC_LOG` Write | TTL 만료·민감정보 Capture 정책 확인 | `.../features/log-level/LogLevelPage.vue` | 제3부 기능 카드 |
| logPolicies | /logPolicies | 통합 관제 | Log Capture·Retention·Trace Boost | Target/Level/DB/File/Stack/Retention/Sampling/Capture Mode/Allowlist/Masking/Byte Cap/Reason/Trace Boost | Policy·Distribution Event/Gateway/Version/Status/Attempt/Fencing/Error/ACK | 조회·저장·중지·Override·Trace Boost·Cache Refresh/Clear·적용 상태 | `LOG_POLICY` Write | Raw Authorization/Cookie/Token·FULL RAW 금지; ACK 실패 대사 | `.../features/log-policies/LogPoliciesPage.vue` | 제3부 기능 카드 |
| recoveryCenter | /recoveryCenter | 통합 관제 | Unknown·DLQ·Outbox·File Transfer 통합 조회 | 없음 | Unknown/DLQ/Outbox/File Transfer KPI·후보 | 새로고침 | 조회 권한 | 실제 조치는 Reliability 화면 Gate 사용 | `.../features/recovery-center/RecoveryCenterPage.vue` | 제3부 기능 카드 |
| incidents | /incidents | 통합 관제 | Incident Lifecycle | Severity SEV1~4, Title, Summary, Source, Reason | ID, Severity, Title, Status, Detected | 생성·ACKNOWLEDGED·MITIGATED·RESOLVED | Incident Write | 각 전이에 구체적 Reason | `.../features/incidents/IncidentsPage.vue` | 제3부 기능 카드 |
| reliability | /reliability | 통합 관제 | DLQ·Unknown·Batch Log 대사 | Scope/Status/Key/Transaction/Topic/Endpoint/Type/Business Date/Job/Instance/Limit; Message/Unknown ID/Target Status/Reason | 통합 Result | 조회·BAT 상세·DLQ Replay·Unknown 수동 확정 | `RELIABILITY` Write | 실제 Side Effect 근거 없이 수동 성공 확정 금지 | `.../features/reliability/ReliabilityPage.vue` | 제3부 기능 카드 |
| notifications | /notifications | 연계 관리 | 알림 Rule·Durable Delivery | Rule/Event/Channel/Severity/Receiver/Reason; Delivery Expected Version/Operation/Reason | Rule; Delivery/Hash/Status/Attempt/Lease/Version; Provider Attempt | 저장·중지·Test·CSV·Retry·Cancel | `NOTIFICATION_*` Button Permission | Expected Version·Lease·Attempt 확인 | `.../features/notifications/NotificationsPage.vue` | 제3부 기능 카드 |
| downloads | /downloads | 연계 관리 | CSV Download·Audit | Type, Target, Date Range, Transaction/Trace/Job, Limit, Reason | Download Result | 정책 조회·CSV | Download Permission·Reason | Data Scope·Masking·건수 상한 | `.../features/downloads/DownloadsPage.vue` | 제3부 기능 카드 |
| file-jobs | /file-jobs | 배치 운영 | 대량 File Job | Operation, Template/Version, CSV/XLSX, Dry Run, File, Reason; Control Approval/Reason; Unknown Resolution | Job/State/Rows/Checksum; Row State/Business Key/Error | Upload·Detail·Apply·Retry·Cancel·Rollback·Unknown Resolve·Artifact | `FILE_JOB_*` Button Permission | 상태별 Button 활성; Side Effect 대사·Rollback Token | `.../features/file-jobs/FileJobsPage.vue` | 제3부 기능 카드 |
| batch | /batch | 배치 운영 | Batch·Center-Cut 종합 통제 | Job/Execution/Schedule/Parameter/Calendar/Date/Simulation/Dispatch/Heartbeat/Lock/Ghost/Reason | Execution Trace; Center-Cut Job/Target/Result | 등록·실행·재수행·중지·Scheduler 1회·Lock/Ghost·조회·CSV | `BATCH` Write | Unknown/Lock/Ghost 조치 전 원장·Heartbeat 대사 | `.../features/batch/BatchPage.vue` | 제3부 기능 카드 |
| batch-overview | /batch-overview | 배치/통합 관제 | Batch Overview | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`overview` | 제3부 기능 카드 |
| batch-runtime | /batch-runtime | 배치/통합 관제 | Runtime Topology | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`runtime` | 제3부 기능 카드 |
| batch-instances | /batch-instances | 배치/통합 관제 | Batch Instances | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`instances` | 제3부 기능 카드 |
| batch-scheduler | /batch-scheduler | 배치/통합 관제 | Scheduler | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`scheduler` | 제3부 기능 카드 |
| batch-worker-pools | /batch-worker-pools | 배치/통합 관제 | Worker Pools | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`worker-pools` | 제3부 기능 카드 |
| batch-center-cut | /batch-center-cut | 배치/통합 관제 | Center-Cut | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`center-cut` | 제3부 기능 카드 |
| batch-agents | /batch-agents | 배치/통합 관제 | Agents | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`agents` | 제3부 기능 카드 |
| batch-job-packs | /batch-job-packs | 배치/통합 관제 | Job Packs | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`job-packs` | 제3부 기능 카드 |
| batch-executions | /batch-executions | 배치/통합 관제 | Executions | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`executions` | 제3부 기능 카드 |
| batch-recovery | /batch-recovery | 배치/통합 관제 | Recovery/Unknown | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`recovery` | 제3부 기능 카드 |
| batch-leases | /batch-leases | 배치/통합 관제 | Leases | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`leases` | 제3부 기능 카드 |
| batch-alerts | /batch-alerts | 배치/통합 관제 | Alerts | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`alerts` | 제3부 기능 카드 |
| batch-audit | /batch-audit | 배치/통합 관제 | Audit Evidence | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`audit` | 제3부 기능 카드 |
| workers | /workers | 배치/통합 관제 | Workers | View 고정; 별도 검색 UI 없음 | Control Server가 반환한 최대 18개 동적 Column | 새로고침 | 조회 권한 | `stale`/`partial` 경고를 정상·Empty로 해석 금지 | `BatchViewPage.vue`, view=`workers` | 제3부 기능 카드 |
| batch-deployment | /batch-deployment | 배치 운영 | Deployment History·Plan | Manifest JSON, Reason | Cell별 Deployment/Rollback·Failure Stage; 생성 Plan | 새로고침·Plan 생성 후 Approval | 배포 Plan 권한 + BAT Approval | Plan 생성은 실행 완료 아님; Partial/Reconcile 필요 | `BatchDeploymentPage.vue`, `DeploymentPage.vue` | 제3부 기능 카드 |
| gateway-dashboard | /gateway-dashboard | 온라인 운영 | Gateway Dashboard | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-servers | /gateway-servers | 온라인 운영 | Gateway Servers | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-groups | /gateway-groups | 온라인 운영 | Gateway Groups | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-routes | /gateway-routes | 온라인 운영 | Gateway Routes | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-security | /gateway-security | 온라인 운영 | Gateway Security | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-health | /gateway-health | 온라인 운영 | Gateway Health | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-transactions | /gateway-transactions | 온라인 운영 | Gateway Transactions | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-log-policies | /gateway-log-policies | 온라인 운영 | Gateway Log Policies | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| gateway-apply-status | /gateway-apply-status | 온라인 운영 | Gateway Apply Status | Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력 | TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK | 조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치 | Gateway Menu/Action Permission + Approval | Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리 | `.../features/gateway-operations/GatewayOperationsPage.vue` | 제3부 기능 카드 |
| permissions | /permissions | 프레임워크 | Role·Menu·Button·API Permission | Role/Menu/Button/API ID, Read/Write/Delete/Allow, Reason; Registry Fields | Matrix/Registry Result | 조회·각 Permission 저장·Role/Menu/Button/API 등록/수정 | `PERMISSION` Write | Frontend 숨김과 Backend 403 모두 검증 | `.../features/permissions/PermissionsPage.vue` | 제3부 기능 카드 |
| operators | /operators | 프레임워크 | 운영자 | ID/Name/Mobile/Office/Initial Password/Reason; Raw Reason | ID/Name/Status/Masked Contact/Roles/Lock | 등록·원문 보기·Role 보유 후 활성화 | `OPERATOR` Write, Raw 별도 | Operation ID 대사; Raw Dialog 종료 시 Clear | `.../features/operators/OperatorsPage.vue` | 제3부 기능 카드 |
| password | /password | 프레임워크 | Password·Session | Operator, New Password, Force Change, Session ID, Reason | Policy/Session/Action Result | 정책 조회·Reset·Unlock·Session 조회/강제 종료/만료 정리 | `PASSWORD` 또는 `OPERATOR` Write | Reset 뒤 강제 변경·Session 폐기 확인 | `.../features/password/PasswordPage.vue` | 제3부 기능 카드 |
| security | /security | 프레임워크 | IP Allowlist·MFA | IP/CIDR, Description, Operator, Secret Ref, OTP, Reason | Security Result | 조회·IP 저장·MFA 등록/검증 | `SECURITY` Write | Secret 원문 금지; BFF 401/403 재검증 | `.../features/security/SecurityPage.vue` | 제3부 기능 카드 |
| secrets | /secrets | 프레임워크 | Secret Metadata·Rotation | Provider, Key, Rotation Reason | Reference/Version/Created/Expires/Rotatable/Attributes | Provider 조회·Metadata 조회·Rotation | Secret Permission | Provider와 Secret 모두 Rotatable일 때만 | `.../features/secrets/SecretsPage.vue` | 제3부 기능 카드 |
| approvals | /approvals | 프레임워크 | 위험조치 승인 | Action/Policy/Owner/Target/Request Key/Expire/Reason/Masked Snapshot; Decision/Idempotency | Request/Execution/Policy | 요청·결정·승인 Command 실행 | Approval Role | UNKNOWN은 recoveryRequiredYn으로 대사 | `.../features/approvals/ApprovalsPage.vue` | 제3부 기능 카드 |
| breakGlass | /breakGlass | 프레임워크 | 비상 권한 | Scope SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY, Target, TTL 1~30, Reason | Session/Status/Expiry/Post Review | 발급·종료·사후 승인/문제 기록 | Break-glass Permission | Owner Command가 Scope를 명시적으로 소비 | `.../features/break-glass/BreakGlassPage.vue` | 제3부 기능 카드 |

## 19. Dashboard·Topology·Capacity

### Dashboard

- 등록 인스턴스 수와 정상 상태 수를 비교한다.
- 비정상 Health가 1개 이상이면 Service Registry의 Instance·Health Tab에서 대상과 마지막 Heartbeat를 확인한다.
- 결과 미확정은 Reliability에서 `unknownId`, Transaction ID, Side Effect 근거를 확인한다.
- DLQ는 Message ID·Topic·Attempt와 Consumer Side Effect를 확인한 뒤 Replay한다.
- 최근 Service Call은 Status·Duration·Transaction ID로 Transaction Group과 연결한다.

### Topology

Service→Instance 계층과 Endpoint·Weight·Status를 확인한다. 표시되지 않는 Instance가 있으면 Registry Filter, Active/Drain/Maintenance, Data Scope를 확인한다. 화면 Node 수와 Registry 원장 건수를 비교한다.

### Capacity

현재 화면은 최근 Call 평균과 실패율을 계산한다. P95/P99·장기 추세·Forecast가 아니므로 Capacity 증설 근거로 단독 사용하지 않는다. Metrics Backend에서 CPU·Heap·GC·Pool·Queue·DB·Kafka Lag와 함께 판정한다.

## 20. Transaction·표준 실행

### 거래 그룹 조회

1. 기간을 먼저 제한한다.
2. Transaction ID가 있으면 단일 ID로 조회한다.
3. 실패 분석은 `failureYn=Y`, 실패 Code, Module/Segment를 사용한다.
4. 고객·회원 검색 결과는 Masked 값과 Data Scope를 확인한다.
5. Row를 선택해 Segment Timeline·표준/확장 Header Snapshot·외부 호출을 확인한다.
6. Authorization, API Key, Token, Secret, Password, Credential, Signature 원문은 표시되지 않는 것이 정상이다.

### 거래 Metadata 재스캔·비활성화

- 재스캔 전 Module과 현재 Active 상태를 기록한다.
- 변경 Reason을 입력한다.
- 응답 유실 시 선택 Transaction ID와 Audit를 조회한다.
- 비활성화 뒤 신규 호출 차단과 기존 실행 영향 범위를 확인한다.

### 표준 실행 Catalog

Online/Batch 유형, Owner Domain, Source Module, Endpoint를 확인한다. Catalog 항목과 실제 Controller/Job Definition이 다르면 실행 등록을 진행하지 않고 Source Owner에게 정합화를 요청한다.

## 21. Channel Policy

### Channel 등록·변경

필수 입력: Code, Name, Type, Trust Level, Client/Internal Flag, Authentication/Signature Required, Active, Description, Reason.

- `EXTERNAL` Channel을 `INTERNAL`로 변경하면 인증·서명·Gateway Header Trust 영향 검토가 필요하다.
- Snapshot 갱신 전 현재 Version과 정책 수를 기록한다.
- Package Import는 Dry Run으로 Validation·Diff를 확인한 뒤 적용한다.
- 반입 응답 유실 시 새 Package를 만들지 않고 Snapshot Version과 Audit를 확인한다.

### 실행 정책

Policy Key, Standard Execution ID, Original/Caller Channel, Request Type, Allowed, Authentication/Signature, Max TPS, Active를 설정한다. `maxTps=0` 또는 UI의 “제한 없음” 의미가 Backend 계약과 일치하는지 확인한다.

## 22. Service Registry

### Service

ID는 최초 저장 뒤 변경하지 않는다. Name, Type, Owner Module, Use, Description, Reason을 관리한다. Consumer Source에는 Host/IP 대신 Service ID를 사용한다.

### Endpoint

HTTP/HTTPS/GRPC/LOCAL/TCP/SFTP 유형, Base URL·Context Path, Timeout, Retry, Use를 관리한다. 비멱등 Command Endpoint의 Retry를 0보다 크게 설정하지 않는다.

### Instance

- Host/Port/Base URL, Environment/Zone/Cell, Weight/Priority, Active를 등록한다.
- Drain: 신규 Routing에서 제외하고 In-flight 완료를 기다린다.
- Disable: Health와 무관하게 사용 중지한다.
- Resume: Health·Artifact·Config가 정상인지 확인한 뒤 편입한다.
- Maintenance와 Draining을 Status 하나로 합치지 않는다.

### Health·Routing·Circuit·Call

Protocol Status·Latency·Failure Message·Checked Time, Routing Policy, Circuit State/Failure Count, 최근 Call Retry/Failure를 확인한다. Instance 상태를 수동으로 UP으로 바꾸기 전에 Probe와 Heartbeat 원인을 해결한다.

## 23. Runtime Control

### 변경 생성

1. Environment·Service/Group/Instance로 대상을 제한한다. `allowAll`은 승인 정책을 확인한다.
2. Target Preview에서 Drain/Maintenance 제외와 대상 수를 확인한다.
3. Diff Preview에서 Current/Desired와 민감정보 Masking을 확인한다.
4. Expected Version, Rollout, Wave Size, Quorum, Payload Schema/Key/Type, Approval/Break-glass, Reason을 입력한다.
5. 생성 후 Change ID와 Operation ID를 기록한다.

### 결과 확인

- ACK/Failed/Drift 수와 Target 수가 일치하는지 확인한다.
- 일부 실패면 실패 Instance의 Error와 Actual Version을 확인한다.
- Audit Hash Chain을 검증한다.
- Cancel은 예약/진행 가능 상태에서만, Exact Rollback은 이전 Version Artifact/Policy가 존재할 때만 수행한다.
- Rollback 뒤 Desired/Actual/Drift 0과 Owner 기능 Probe를 확인한다.

## 24. Config·Cache·Code·Message·Calendar

### Config

Encrypted YN을 설정해도 Value가 Secret Store 원문인지 확인한다. Secret은 Secret 화면의 Reference를 사용한다. 변경 뒤 Consumer Cache·재기동 영향과 Rollback 값을 기록한다.

### Cache

Key/Namespace Evict 전 영향 Consumer와 Stampede 위험을 확인한다. Durable Reconcile은 Owner 정본과 Cache를 비교해 정상화한다. Cache 결과만으로 업무 상태를 확정하지 않는다.

### Calendar

Save/Delete 모두 Expected Version과 Audit Reason을 사용한다. 409면 최신 Version을 다시 선택한다. Batch/Scheduler에서 같은 Calendar ID와 Business Date가 반영됐는지 확인한다.

### Response Code·Code·Message

등록 전 중복 Key·Parent·Locale·HTTP Mapping을 확인한다. External Message에는 내부 Stack·Table·Secret을 넣지 않는다. 변경 뒤 Consumer Cache Refresh와 API Error 응답을 확인한다.

## 25. Log·Audit·Download

### `/logs` 차단

Log Route는 `LogsPage.vue`와 Generated Client를 연결하고, 검색·상세·Transaction 이동·Masking을 Route Test로 확인한다.

### Remote Log

기간·환경·Service·Instance·Transaction/Batch ID로 범위를 좁힌다. Download Reason이 필요하다. Bundle Job은 `COMPLETED` 뒤에만 다운로드한다. Retention 만료·Size·압축 상태와 Download Audit를 확인한다.

### Log Policy

Body Capture는 기본 NONE/METADATA_ONLY를 사용한다. MASKED/ALLOWLIST는 Schema·Masking Preview를 확인하고 ENCRYPTED_BODY는 제한 Route·Approval·TTL이 필요하다. Authorization/Cookie/Token 원문과 일반 Full Raw Body는 저장하지 않는다. Gateway Distribution ACK/NACK와 Fencing을 확인한다.

### Audit Delivery

업무 Command가 성공했어도 Audit Delivery가 FAILED일 수 있다. Delivery ID, Operation Status, Attempt/Max, Last Error를 확인하고 Reason을 입력해 재처리한다. 업무 Command를 다시 실행하지 않는다.

## 26. Incident·Reliability·Notification

### Incident

SEV와 Title/Summary를 입력해 등록하고, 인지·완화·복구 전이마다 Reason을 남긴다. `RESOLVED`는 Alert가 사라졌다는 뜻이 아니라 원인·영향·복구 확인이 완료됐을 때 사용한다.

### DLQ Replay

Message ID, Topic, Idempotency Key, Consumer Side Effect, Inbox 상태를 확인한다. 이미 Side Effect가 있으면 Replay하지 않고 Reconcile한다.

### Unknown 수동 확정

Target Status를 선택하기 전에 Transaction ID, Owner DB, 외부 응답, Attempt, Audit 근거를 확보한다. `CONFIRMED_SUCCESS`를 추정으로 선택하지 않는다.

### Notification

Rule과 Delivery를 분리한다. Delivery Retry/Cancel은 Expected Version, Operation ID, Lease, Attempt, Error를 확인한다. Provider 응답 유실은 Delivery를 곧바로 실패로 확정하지 않고 Provider 조회 가능 여부를 확인한다.

## 27. File Job

```text
Upload(Dry Run 권장)
→ RECEIVED
→ VALIDATED / READY_TO_APPLY
→ Approval + Apply
→ COMPLETED / FAILED / PARTIAL_FAILED / UNKNOWN_RESULT
→ Row별 Reconcile
→ Retry 또는 Rollback
```

- Apply와 Rollback은 Approval ID가 필요하다.
- UNKNOWN Row는 외부 시스템·DB 결과를 확인하고 Resolution을 선택한다.
- Side Effect Applied인데 Compensation되지 않았다면 Rollback Token이 필요할 수 있다.
- Row별 Business Key와 오류를 확인한 뒤 선택 재처리한다.

## 28. Batch 운영

### 실행

Job ID·Parameter·Calendar·Business Date·Reason을 확인한다. 수동 실행은 Scheduler Trigger와 중복되지 않는지 Idempotency Key/Execution 원장을 확인한다.

### Stop·Retry

Stop 요청 뒤 Spring Batch Status와 Worker 상태를 재조회한다. 실패 재수행은 Restartable, Checkpoint, Side Effect 중복 여부를 확인한다.

### Lock·Ghost

Heartbeat Timeout과 Lock Owner/Fencing을 확인한다. Ghost Action `FAIL`, `ABANDON`, `RELEASE_LOCK`의 영향이 다르므로 실행 상태·Worker Process·DB Commit을 대사한다.

### Batch Runtime Views

`stale`/`partial` 경고가 있으면 Control Server 실패로 판정하고 빈 결과로 인계하지 않는다. Dynamic Column의 이름과 값은 Backend View Contract를 기준으로 해석한다.

### Deployment

Manifest와 Reason으로 Plan을 생성한 뒤 Approval을 받아야 한다. Instance 일부 실패는 Partial 상태이며 Deployment History·Agent·Health·Actual Artifact를 Reconcile한다.

## 29. Gateway 운영

1. Capability Available과 Source Instance를 확인한다.
2. Server Group과 Service Registry Member를 연결한다.
3. Binding Draft를 저장한다.
4. Host/Path/Method/API Version/Target Rewrite/Timeout/Retry/Security Policy를 검증한다.
5. Connection Test를 Target Direct와 Gateway E2E로 수행한다.
6. Approval 뒤 Publish한다.
7. Gateway Instance Expected/Applied Version과 ACK/NACK를 확인한다.
8. Drift·Spool Backlog·Open Circuit·Certificate를 확인한다.
9. 차단·Rollback은 CAS Version·Approval·Reason과 LKG를 사용한다.

## 30. Permission·Operator·Security

### Permission

Role→Menu→Button→API Permission을 순서대로 등록한다. Menu Read가 있어도 Button/API가 없으면 조치할 수 없다. 변경 Reason과 실제 403 Negative Test를 남긴다.

### Operator

신규 운영자는 등록 뒤 Role을 부여하고 ACTIVE로 전환한다. 원문 연락처는 별도 Reason·Audit로 조회하며 일반 목록은 Masked 상태를 유지한다.

### Password·Session

Reset 뒤 Force Change, Unlock, Session Revoke를 구분한다. Session Revoke 응답 유실 시 Session ID를 다시 조회하고 같은 조치를 무조건 반복하지 않는다.

### Secret

원문이 아니라 Provider Reference와 Metadata만 조회한다. Rotation은 Provider·Secret 모두 Rotatable이고 Reason이 있을 때만 실행한다. Consumer Reload/재기동과 이전 Version Rollback 가능성을 확인한다.

### Approval·Break-glass

Approval은 Masked Snapshot Hash를 고정하고 Owner Command를 실행한다. Break-glass는 Scope·Target·TTL을 최소화하고 종료 뒤 사후검토한다. 전역 권한 우회로 사용하지 않는다.

## 31. 교대·Evidence 양식

```text
기준 Commit:
운영자·Role:
Menu/Route:
Target:
시작 상태·Version:
입력·Reason·Approval:
Operation/Transaction/Change/Delivery ID:
정상/오류 응답:
부분 실패 Target:
Reconciliation 근거:
Rollback 결과:
Log·Metric·Trace·Audit:
환경 확인 대상·후속 조치:
최종 판정:
```

## 32. ADM 운영 완료 Checklist

- [ ] 모든 Route의 Component 존재와 Build 성공
- [ ] Menu·Button·API·Owner Permission
- [ ] Search Default·Column·Detail·Paging·Sort
- [ ] 상태와 Button 활성 조건
- [ ] Reason·Approval·Expected Version·Operation ID
- [ ] Loading·Empty·Error·Stale·Partial·Unknown
- [ ] Retry·Reprocess·Reconcile·Rollback
- [ ] Masking·Raw 조회·Download Audit
- [ ] Log·Metric·Trace·Audit·Evidence
- [ ] Browser 3종과 401/403/409/503 Fault Test

---
## 부록 I. ADM 화면 Source 진입점

| 화면군 | 기준 Frontend Source |
|---|---|
| 전체 Route | `cpf-admin/frontend/src/app/routes.ts` |
| Dashboard | `cpf-admin/frontend/src/features/dashboard/DashboardPage.vue` |
| Topology | `cpf-admin/frontend/src/features/topology/TopologyPage.vue` |
| Capacity | `cpf-admin/frontend/src/features/capacity/CapacityPage.vue` |
| 거래 그룹 | `cpf-admin/frontend/src/features/transaction-groups/TransactionGroupsPage.vue` |
| 거래 Metadata | `cpf-admin/frontend/src/features/transactions/TransactionsPage.vue` |
| Channel Policy | `cpf-admin/frontend/src/features/channel-policy/ChannelPolicyPage.vue` |
| Service Registry | `cpf-admin/frontend/src/features/service-registry/ServiceRegistryPage.vue` |
| Runtime Control | `cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue` |
| Business Calendar | `cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue` |
| Remote Log | `cpf-admin/frontend/src/features/remote-logs/RemoteLogsPage.vue` |
| Audit Log | `cpf-admin/frontend/src/features/audit-logs/AuditLogsPage.vue` |
| Recovery Center | `cpf-admin/frontend/src/features/recovery-center/RecoveryCenterPage.vue` |
| Reliability | `cpf-admin/frontend/src/features/reliability/ReliabilityPage.vue` |
| Notification | `cpf-admin/frontend/src/features/notifications/NotificationsPage.vue` |
| File Job | `cpf-admin/frontend/src/features/file-jobs/FileJobsPage.vue` |
| Batch Main | `cpf-admin/frontend/src/features/batch/BatchPage.vue` |
| Batch Runtime View | `cpf-admin/frontend/src/features/batch-runtime-control/BatchViewPage.vue` |
| Batch Deployment | `cpf-admin/frontend/src/features/batch-deployment/BatchDeploymentPage.vue` |
| Gateway Operations | `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue` |
| Permission | `cpf-admin/frontend/src/features/permissions/PermissionsPage.vue` |
| Operator | `cpf-admin/frontend/src/features/operators/OperatorsPage.vue` |
| Security | `cpf-admin/frontend/src/features/security/SecurityPage.vue` |
| Approval | `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue` |

---

## 제3부. ADM 59개 화면별 상세 운영 절차

> 이 부는 Route 목록이 아니라 화면마다 실제로 무엇을 준비하고, 입력하고, 확인하고, 실패 시 어떻게 복구할지를 설명한다. 공통 규칙과 각 화면 고유 Field·Column·Action을 함께 적용한다.

## 33. 화면별 기능 카드 사용법

1. Route와 Menu ID가 현재 로그인 권한에 포함되는지 확인한다.
2. 기능 카드의 선행 조건과 입력을 준비한다.
3. 정상 결과의 상태·Version·Audit·Metric을 모두 확인한다.
4. 오류 표에서 해당 상황을 찾아 신규 실행·Retry·Reconcile·Rollback 여부를 결정한다.
5. 교대 시 카드 마지막의 인계 항목을 기록한다.

### dashboard — 운영 대시보드

**접근 위치**: `/`
**메뉴 그룹**: 홈
**화면 Source**: ``cpf-admin/frontend/src/features/dashboard/DashboardPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **운영 대시보드** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

초기 데이터 자동 조회

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

등록 인스턴스·정상 수, 비정상 Health, 결과 미확정, DLQ, 서비스 상태, 최근 Service Call

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Loading/Empty/Error

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### topology — 서비스 토폴로지

**접근 위치**: `/topology`
**메뉴 그룹**: 홈
**화면 Source**: ``.../features/topology/TopologyPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **서비스 토폴로지** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Service ID·명, Instance ID·명, Endpoint, Weight, Status

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Registry 0건 Empty

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### capacity — 용량·SLO 기본 Signal

**접근 위치**: `/capacity`
**메뉴 그룹**: 홈
**화면 Source**: ``.../features/capacity/CapacityPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **용량·SLO 기본 Signal** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

최근 호출, 평균 지연, 실패율, 인스턴스; Service/Endpoint/Status/Latency/Transaction

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 장기 Percentile·Forecast는 Metrics Backend와 함께 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### logs — 로그 조회

**접근 위치**: `/logs`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/logs/LogsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **로그 조회** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `해당 없음`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

해당 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

해당 없음

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **해당 없음**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 표준 로그 조회 화면

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### transactionGroups — 거래 그룹·구간 추적

**접근 위치**: `/transactionGroups`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/transaction-groups/TransactionGroupsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **거래 그룹·구간 추적** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `거래 조회 Permission·Data Scope`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

기간, Transaction/Segment, Status, 실패, Module/Source/Target/Role/Direction, 고객·회원·사용자·운영자, Channel, 외부기관/거래, API/거래명/오류, Duration, Header 검색

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

거래/모듈 흐름/시간/소요/상태/실패/Masked 고객·회원/Channel/외부 연계

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·초기화·정렬·Paging·상세 Tab**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Authorization/API Key/Token 등 원문 미표시

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### transactions — 거래 Metadata

**접근 위치**: `/transactions`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/transactions/TransactionsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **거래 Metadata** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``TRANSACTION_META` Write for mutation`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Module 기본 ADM, Active Y, Transaction ID, 선택 ID, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Pretty Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·재스캔·비활성화**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 재스캔/비활성화 응답 유실 시 Transaction ID 대사

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### standardExecutions — 표준 실행 Catalog

**접근 위치**: `/standardExecutions`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/standard-executions/StandardExecutionsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **표준 실행 Catalog** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

유형 ONLINE/BATCH, Owner Domain, Keyword

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

ID, 유형, 실행명, Owner, Source Module, Endpoint

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·상세**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Catalog/Source 불일치 조사

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### channelPolicy — Channel·거래 정책 Snapshot

**접근 위치**: `/channelPolicy`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/channel-policy/ChannelPolicyPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Channel·거래 정책 Snapshot** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``CHANNEL_POLICY` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Channel/Policy Form; Package JSON; Import Dry Run

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Channel 인증·서명·신뢰·Version; 정책 허용·TPS·Version

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Snapshot 갱신·Package 반출/반입·Channel/Policy 저장**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Snapshot Version·Import Dry Run·부분 적용 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### serviceRegistry — Service·Endpoint·Instance·Health·Routing

**접근 위치**: `/serviceRegistry`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/service-registry/ServiceRegistryPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Service·Endpoint·Instance·Health·Routing** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``SERVICE_REGISTRY` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Service ID, Endpoint, Instance Status; 각 등록 Form

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Service/Endpoint/Instance/Health/Routing/Circuit/Call

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **등록·수정·Drain·Resume·Disable·새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Version·Heartbeat·Draining·Maintenance·Health 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### runtimeControl — Runtime 변경 Control Plane

**접근 위치**: `/runtimeControl`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/runtime-control/RuntimeControlPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Runtime 변경 Control Plane** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Runtime Control Permission + Approval/Break-glass`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Operation/Change/Target/Expected Version/Rollout/Approval/Payload/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Readiness, Pending, Poison, Drift; ACK/Failed/Drift/Hash

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **Target/Diff Preview·생성·조회·Audit 검증·Cancel·Exact Rollback·Group CRUD**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: UNKNOWN/PARTIAL/Drift를 성공으로 처리 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### maintenance — 점검·Drain 제어

**접근 위치**: `/maintenance`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/maintenance/MaintenancePage.vue``

#### 사용 목적과 사용 시점

이 화면은 **점검·Drain 제어** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Owner Command Permission`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Service, Endpoint, Instance, DRAIN/DISABLE/RESUME, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

시간, Service, Instance, Action, Result, Reason

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **명령 실행·조회**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Routing 제외 영향·Audit 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### cache — Cache 조회·Evict·Reconcile

**접근 위치**: `/cache`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/cache/CachePage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Cache 조회·Evict·Reconcile** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Button Permission `CACHE_*``이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Tenant, Namespace, Key, Version, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Cache Summary/Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **Target 갱신·Key/Namespace Evict·Durable Reconcile**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Cache는 정본 아님; Reconcile 뒤 Owner 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### configs — 설정 관리

**접근 위치**: `/configs`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/configs/ConfigsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **설정 관리** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``CONFIG` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Config ID/Key/Value/Type/Encrypted YN/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Pretty Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·등록·수정**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Secret 원문을 일반 Config에 저장 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### responseCodes — 응답코드 관리

**접근 위치**: `/responseCodes`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/response-codes/ResponseCodesPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **응답코드 관리** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``RESPONSE_CODE` Write/Delete`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Response/Message Code, S/E, Module, Group, Sequence, HTTP, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Pretty Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·등록·수정·삭제**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Consumer·Message Mapping 영향 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### businessCalendar — 영업일·휴일 Override

**접근 위치**: `/businessCalendar`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/business-calendar/BusinessCalendarPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **영업일·휴일 Override** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Menu Write/Delete + Writable Provider`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Calendar DEFAULT, Date, Business/Holiday, Day Type, Institution, Business/Audit Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Date, Type, Institution, Reason, Version

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·저장·삭제**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Expected Version 409 충돌 재조회

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### codes — 공통 코드

**접근 위치**: `/codes`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/codes/CodesPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **공통 코드** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``CODE` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Code ID, Parent ID, Key, Value, Description, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Pretty Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·등록·수정**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Parent 순환·Consumer Cache 갱신 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### messages — 다국어 Message

**접근 위치**: `/messages`
**메뉴 그룹**: 연계 관리
**화면 Source**: ``.../features/messages/MessagesPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **다국어 Message** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``MESSAGE` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Message ID/Code/Locale/External/Internal/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Pretty Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·등록·수정**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: External/Internal 노출 범위 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### remoteLogs — 원격 Log Artifact

**접근 위치**: `/remoteLogs`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/remote-logs/RemoteLogsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **원격 Log Artifact** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``REMOTE_LOG` Write for download`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

환경/Module/Service/Instance/Type/File/표준 ID/Transaction/Batch IDs/기간/Size/압축/활성/Lines/Keyword/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Artifact Metadata·Preview·Bundle Job·Diagnostics

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·단건/선택/비동기 ZIP·상태·Download·진단**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Retention·Size·Masking·Download Audit

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### auditLogs — Audit 조회·Delivery 복구

**접근 위치**: `/auditLogs`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/audit-logs/AuditLogsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Audit 조회·Delivery 복구** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``AUDIT_LOG` Write for retry`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Operator, Action, Target Type/ID; Delivery Status, Retry Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Audit Result; Delivery ID/Status/Attempt/Error

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Delivery 조회·재처리**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 업무 결과와 Audit Delivery 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### logLevel — Dynamic Log Level

**접근 위치**: `/logLevel`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/log-level/LogLevelPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Dynamic Log Level** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``DYNAMIC_LOG` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Business Transaction ID, Transaction ID, DEBUG/INFO/TRACE, TTL, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Rule Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·등록**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: TTL 만료·민감정보 Capture 정책 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### logPolicies — Log Capture·Retention·Trace Boost

**접근 위치**: `/logPolicies`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/log-policies/LogPoliciesPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Log Capture·Retention·Trace Boost** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``LOG_POLICY` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Target/Level/DB/File/Stack/Retention/Sampling/Capture Mode/Allowlist/Masking/Byte Cap/Reason/Trace Boost

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Policy·Distribution Event/Gateway/Version/Status/Attempt/Fencing/Error/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·저장·중지·Override·Trace Boost·Cache Refresh/Clear·적용 상태**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Raw Authorization/Cookie/Token·FULL RAW 금지; ACK 실패 대사

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### recoveryCenter — Unknown·DLQ·Outbox·File Transfer 통합 조회

**접근 위치**: `/recoveryCenter`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/recovery-center/RecoveryCenterPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Unknown·DLQ·Outbox·File Transfer 통합 조회** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Unknown/DLQ/Outbox/File Transfer KPI·후보

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 실제 조치는 Reliability 화면 Gate 사용

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### incidents — Incident Lifecycle

**접근 위치**: `/incidents`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/incidents/IncidentsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Incident Lifecycle** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Incident Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Severity SEV1~4, Title, Summary, Source, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

ID, Severity, Title, Status, Detected

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **생성·ACKNOWLEDGED·MITIGATED·RESOLVED**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 각 전이에 구체적 Reason

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### reliability — DLQ·Unknown·Batch Log 대사

**접근 위치**: `/reliability`
**메뉴 그룹**: 통합 관제
**화면 Source**: ``.../features/reliability/ReliabilityPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **DLQ·Unknown·Batch Log 대사** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``RELIABILITY` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Scope/Status/Key/Transaction/Topic/Endpoint/Type/Business Date/Job/Instance/Limit; Message/Unknown ID/Target Status/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

통합 Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·BAT 상세·DLQ Replay·Unknown 수동 확정**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 실제 Side Effect 근거 없이 수동 성공 확정 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### notifications — 알림 Rule·Durable Delivery

**접근 위치**: `/notifications`
**메뉴 그룹**: 연계 관리
**화면 Source**: ``.../features/notifications/NotificationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **알림 Rule·Durable Delivery** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``NOTIFICATION_*` Button Permission`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Rule/Event/Channel/Severity/Receiver/Reason; Delivery Expected Version/Operation/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Rule; Delivery/Hash/Status/Attempt/Lease/Version; Provider Attempt

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **저장·중지·Test·CSV·Retry·Cancel**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Expected Version·Lease·Attempt 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### downloads — CSV Download·Audit

**접근 위치**: `/downloads`
**메뉴 그룹**: 연계 관리
**화면 Source**: ``.../features/downloads/DownloadsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **CSV Download·Audit** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Download Permission·Reason`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Type, Target, Date Range, Transaction/Trace/Job, Limit, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Download Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **정책 조회·CSV**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Data Scope·Masking·건수 상한

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### file-jobs — 대량 File Job

**접근 위치**: `/file-jobs`
**메뉴 그룹**: 배치 운영
**화면 Source**: ``.../features/file-jobs/FileJobsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **대량 File Job** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``FILE_JOB_*` Button Permission`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Operation, Template/Version, CSV/XLSX, Dry Run, File, Reason; Control Approval/Reason; Unknown Resolution

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Job/State/Rows/Checksum; Row State/Business Key/Error

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **Upload·Detail·Apply·Retry·Cancel·Rollback·Unknown Resolve·Artifact**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: 상태별 Button 활성; Side Effect 대사·Rollback Token

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch — Batch·Center-Cut 종합 통제

**접근 위치**: `/batch`
**메뉴 그룹**: 배치 운영
**화면 Source**: ``.../features/batch/BatchPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Batch·Center-Cut 종합 통제** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``BATCH` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Job/Execution/Schedule/Parameter/Calendar/Date/Simulation/Dispatch/Heartbeat/Lock/Ghost/Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Execution Trace; Center-Cut Job/Target/Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **등록·실행·재수행·중지·Scheduler 1회·Lock/Ghost·조회·CSV**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Unknown/Lock/Ghost 조치 전 원장·Heartbeat 대사

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-overview — Batch Overview

**접근 위치**: `/batch-overview`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`overview``

#### 사용 목적과 사용 시점

이 화면은 **Batch Overview** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-runtime — Runtime Topology

**접근 위치**: `/batch-runtime`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`runtime``

#### 사용 목적과 사용 시점

이 화면은 **Runtime Topology** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-instances — Batch Instances

**접근 위치**: `/batch-instances`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`instances``

#### 사용 목적과 사용 시점

이 화면은 **Batch Instances** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-scheduler — Scheduler

**접근 위치**: `/batch-scheduler`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`scheduler``

#### 사용 목적과 사용 시점

이 화면은 **Scheduler** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-worker-pools — Worker Pools

**접근 위치**: `/batch-worker-pools`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`worker-pools``

#### 사용 목적과 사용 시점

이 화면은 **Worker Pools** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-center-cut — Center-Cut

**접근 위치**: `/batch-center-cut`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`center-cut``

#### 사용 목적과 사용 시점

이 화면은 **Center-Cut** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-agents — Agents

**접근 위치**: `/batch-agents`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`agents``

#### 사용 목적과 사용 시점

이 화면은 **Agents** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-job-packs — Job Packs

**접근 위치**: `/batch-job-packs`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`job-packs``

#### 사용 목적과 사용 시점

이 화면은 **Job Packs** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-executions — Executions

**접근 위치**: `/batch-executions`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`executions``

#### 사용 목적과 사용 시점

이 화면은 **Executions** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-recovery — Recovery/Unknown

**접근 위치**: `/batch-recovery`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`recovery``

#### 사용 목적과 사용 시점

이 화면은 **Recovery/Unknown** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-leases — Leases

**접근 위치**: `/batch-leases`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`leases``

#### 사용 목적과 사용 시점

이 화면은 **Leases** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-alerts — Alerts

**접근 위치**: `/batch-alerts`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`alerts``

#### 사용 목적과 사용 시점

이 화면은 **Alerts** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-audit — Audit Evidence

**접근 위치**: `/batch-audit`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`audit``

#### 사용 목적과 사용 시점

이 화면은 **Audit Evidence** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### workers — Workers

**접근 위치**: `/workers`
**메뉴 그룹**: 배치/통합 관제
**화면 Source**: ``BatchViewPage.vue`, view=`workers``

#### 사용 목적과 사용 시점

이 화면은 **Workers** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `조회 권한`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

View 고정; 별도 검색 UI 없음

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Control Server가 반환한 최대 18개 동적 Column

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: `stale`/`partial` 경고를 정상·Empty로 해석 금지

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### batch-deployment — Deployment History·Plan

**접근 위치**: `/batch-deployment`
**메뉴 그룹**: 배치 운영
**화면 Source**: ``BatchDeploymentPage.vue`, `DeploymentPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Deployment History·Plan** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `배포 Plan 권한 + BAT Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Manifest JSON, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Cell별 Deployment/Rollback·Failure Stage; 생성 Plan

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **새로고침·Plan 생성 후 Approval**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Plan 생성은 실행 완료 아님; Partial/Reconcile 필요

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-dashboard — Gateway Dashboard

**접근 위치**: `/gateway-dashboard`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Dashboard** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-servers — Gateway Servers

**접근 위치**: `/gateway-servers`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Servers** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-groups — Gateway Groups

**접근 위치**: `/gateway-groups`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Groups** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-routes — Gateway Routes

**접근 위치**: `/gateway-routes`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Routes** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-security — Gateway Security

**접근 위치**: `/gateway-security`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Security** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-health — Gateway Health

**접근 위치**: `/gateway-health`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Health** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-transactions — Gateway Transactions

**접근 위치**: `/gateway-transactions`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Transactions** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-log-policies — Gateway Log Policies

**접근 위치**: `/gateway-log-policies`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Log Policies** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### gateway-apply-status — Gateway Apply Status

**접근 위치**: `/gateway-apply-status`
**메뉴 그룹**: 온라인 운영
**화면 Source**: ``.../features/gateway-operations/GatewayOperationsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Gateway Apply Status** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Gateway Menu/Action Permission + Approval`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Environment, Service ID, Route ID; Tab별 Group/Binding/Test 입력

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

TPS/Success/Error/P95/P99/Drift/Circuit/Cert/Spool/Test 및 Group/Binding/ACK

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·Group/Binding Draft·Connection Test·Publish/Block/Rollback 관련 조치**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Capability unavailable·ACK/NACK·Drift·Spool Backlog 분리

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### permissions — Role·Menu·Button·API Permission

**접근 위치**: `/permissions`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/permissions/PermissionsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Role·Menu·Button·API Permission** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``PERMISSION` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Role/Menu/Button/API ID, Read/Write/Delete/Allow, Reason; Registry Fields

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Matrix/Registry Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·각 Permission 저장·Role/Menu/Button/API 등록/수정**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Frontend 숨김과 Backend 403 모두 검증

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### operators — 운영자

**접근 위치**: `/operators`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/operators/OperatorsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **운영자** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``OPERATOR` Write, Raw 별도`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

ID/Name/Mobile/Office/Initial Password/Reason; Raw Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

ID/Name/Status/Masked Contact/Roles/Lock

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **등록·원문 보기·Role 보유 후 활성화**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Operation ID 대사; Raw Dialog 종료 시 Clear

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### password — Password·Session

**접근 위치**: `/password`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/password/PasswordPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Password·Session** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``PASSWORD` 또는 `OPERATOR` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Operator, New Password, Force Change, Session ID, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Policy/Session/Action Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **정책 조회·Reset·Unlock·Session 조회/강제 종료/만료 정리**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Reset 뒤 강제 변경·Session 폐기 확인

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### security — IP Allowlist·MFA

**접근 위치**: `/security`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/security/SecurityPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **IP Allowlist·MFA** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 ``SECURITY` Write`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

IP/CIDR, Description, Operator, Secret Ref, OTP, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Security Result

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **조회·IP 저장·MFA 등록/검증**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Secret 원문 금지; BFF 401/403 재검증

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### secrets — Secret Metadata·Rotation

**접근 위치**: `/secrets`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/secrets/SecretsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **Secret Metadata·Rotation** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Secret Permission`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Provider, Key, Rotation Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Reference/Version/Created/Expires/Rotatable/Attributes

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **Provider 조회·Metadata 조회·Rotation**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Provider와 Secret 모두 Rotatable일 때만

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### approvals — 위험조치 승인

**접근 위치**: `/approvals`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/approvals/ApprovalsPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **위험조치 승인** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Approval Role`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Action/Policy/Owner/Target/Request Key/Expire/Reason/Masked Snapshot; Decision/Idempotency

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Request/Execution/Policy

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **요청·결정·승인 Command 실행**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: UNKNOWN은 recoveryRequiredYn으로 대사

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.
### breakGlass — 비상 권한

**접근 위치**: `/breakGlass`
**메뉴 그룹**: 프레임워크
**화면 Source**: ``.../features/break-glass/BreakGlassPage.vue``

#### 사용 목적과 사용 시점

이 화면은 **비상 권한** 업무를 수행한다. 조회 결과를 다른 화면의 상태와 연결해야 할 때는 Transaction ID, Trace ID, Operation ID, Version, Checksum, Instance ID 중 화면이 제공하는 식별자를 인계 키로 사용한다. 화면의 HTTP 응답만으로 업무 결과를 확정하지 않고 Owner 상태와 Audit를 함께 확인한다.

#### 선행 조건

1. 대상 Environment와 Tenant·Data Scope가 맞는지 확인한다.
2. 메뉴 조회 권한과 조치 권한을 구분한다. 필요한 권한은 `Break-glass Permission`이다.
3. 변경 조치 전 대상의 최신 Version·상태·Owner를 새로 조회한다.
4. Reason·Approval·Expected Version이 필요한 조치는 값을 준비한다.
5. 이전 요청이 `PROCESSING`, `UNKNOWN_RESULT`, `PARTIAL`이면 신규 요청을 만들기 전에 기존 Operation을 대사한다.

#### 검색·입력

Scope SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY, Target, TTL 1~30, Reason

- 기간 입력은 운영 표준 Timezone을 사용하고 종료 시각 포함 여부를 확인한다.
- 빈 문자열과 `null`, 전체 선택의 의미를 구분한다.
- ID 입력은 앞뒤 공백을 제거하되 대소문자를 임의 변경하지 않는다.
- Reason은 작업 목적, 영향 범위, 원복 기준, 관련 Incident·Approval ID를 포함한다.
- 비밀번호·Token·Secret·서명 원문은 일반 입력·Log·Audit에 남기지 않는다.

#### 결과와 상세 확인

Session/Status/Expiry/Post Review

조회 후 다음 순서로 판정한다.

1. 결과 생성 시각과 `stale`·`partial` 표식을 확인한다.
2. Row 식별자와 상세의 Owner·Version이 동일한지 확인한다.
3. Masked 값은 원문 조회 권한 없이 복호화하거나 우회하지 않는다.
4. 상태가 집계값과 상세값에서 다르면 상세 Owner 상태를 기준으로 Reconcile한다.
5. Empty 결과는 권한·Filter·Environment·시간 범위를 확인한 뒤에만 0건으로 판정한다.

#### 조치 절차

사용 가능한 조치는 **발급·종료·사후 승인/문제 기록**이다.

1. 조치 직전 대상 상세를 다시 조회한다.
2. 영향 Preview가 있으면 대상 Instance·거래·사용자·배치·Route 수를 기록한다.
3. Reason, Approval ID, Expected Version, Idempotency Key를 입력한다.
4. 실행 후 반환된 Operation ID·Transaction ID를 기록한다.
5. `ACCEPTED` 또는 HTTP 202는 접수 상태이므로 최종 상태를 조회한다.
6. Success에서는 상태·Version·Checksum·Audit·Metric이 함께 바뀌었는지 확인한다.

#### 정상 결과

- 대상 Owner 상태가 요청한 상태로 전이된다.
- Version 또는 Checksum이 변경 조치와 일치한다.
- Audit에 Operator, Permission, Reason, Approval, Before/After, Result가 남는다.
- 관련 Log·Metric·Trace에서 같은 Operation/Transaction 식별자를 찾을 수 있다.
- 부분 적용 대상이 없거나, 대상별 결과가 명시적으로 구분된다.

#### 오류·응답 유실·복구

핵심 복구 기준: Owner Command가 Scope를 명시적으로 소비

| 상황 | 운영 절차 |
|---|---|
| 400·Validation | Field 오류와 허용 범위를 수정하고 새 요청을 만든다. |
| 401 | Session·CSRF·Origin을 확인하고 재로그인한다. 기존 변경 요청을 자동 재전송하지 않는다. |
| 403 | Menu·Button·API Permission과 Data Scope를 확인한다. UI 우회 호출을 하지 않는다. |
| 409 | 최신 Version과 변경자를 조회하고 변경 의도를 다시 확인한다. Blind Retry하지 않는다. |
| 429 | `Retry-After`와 Rate Limit을 확인한다. 비멱등 조치는 결과 대사 후 재실행한다. |
| Timeout·응답 유실 | Operation ID·Transaction ID·Idempotency Key로 기존 결과를 조회한다. |
| PARTIAL | 성공·실패·미응답 Target을 분리하고 Failed-only Retry 또는 Exact Rollback을 선택한다. |
| UNKNOWN_RESULT | Owner 원장, 상대 시스템, Ledger, Audit를 대사해 결과를 확정한 뒤 다음 조치를 선택한다. |

#### 감사·교대 인계

교대 기록에는 화면 Route, 검색 조건, 대상 ID, Before/After Version, Operation ID, Approval ID, 결과 상태, 미확정 항목, 다음 확인 시각, Rollback 기준을 남긴다.


## 34. ADM 일일 운영 순서

1. Dashboard에서 비정상 Health, Unknown Result, DLQ, 최근 실패를 확인한다.
2. Topology·Capacity에서 Instance·Version·Resource 이상을 확인한다.
3. Runtime·Gateway·Batch의 Desired/Actual·ACK/NACK·Lease·Drift를 확인한다.
4. Audit Delivery·Notification Outbox·Recovery Center의 적체를 확인한다.
5. 만료 예정 Approval·Break-glass·Secret·Certificate·Download를 확인한다.
6. 교대 전 미확정 Operation과 다음 Reconcile 시각을 기록한다.

## 35. 월간 운영 검토

- Permission Matrix와 휴면·퇴직 운영자 계정을 검토한다.
- Raw 조회·Export·Break-glass·Secret Rotation Audit를 검토한다.
- Runtime Config·Route·Log Policy·Business Calendar 변경 이력을 검토한다.
- Incident·DLQ·Unknown Result의 반복 원인과 Runbook 개선을 검토한다.
- Batch Restart·Reprocess·Abandon과 Gateway Rollback 빈도를 검토한다.
