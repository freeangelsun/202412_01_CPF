# CPF BZA 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 문서 콘텐츠 기준 Commit: `64049044956924032360fa80be83b5e37c64f828` (`08_03`)
> 기준일: `2026-08-07 Asia/Seoul`
> 기준 Commit: **현재 `master` 구현 기준 문서**. Product Surface·Starter·Tool·EDU 식별자는 아래 기준 Commit의 Source 정본과 대조한다.

| 항목 | 내용 |
|---|---|
| 주 독자 | 조직·직원·사용자·권한·결재 담당자, 업무 연동 개발자, 보안/감사 담당자 |
| 이 문서로 완료할 일 | BZA를 설치하고 조직·직원·사용자·Role·Permission·Data Scope·결재·위임·Attachment·Notification·Session·Masking·Audit·Export를 업무에 적용·운영한다. |
| 완료 판정 | 독자가 다른 문서나 Source 역분석 없이 정상 흐름, 오류, 부분 실패, 복구, 감사, 운영 인계를 끝낼 수 있어야 한다. |
| 상태 표현 | 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요만 사용한다. |
| 정합성 원칙 | 실제 Class·API·SQL·Config·화면·Permission·Script·Test를 우선하고 문서와 양방향으로 추적한다. |

## 1. BZA 도입 판단

BZA를 선택하는 경우:

- 조직 기준일과 발령을 업무 권한에 사용.
- 사용자와 직원/조직을 연결.
- Role·Permission·Data Scope·Masking 필요.
- 결재 Policy·승인자 Snapshot·위임/대결 필요.
- 업무 Attachment/Notification/Audit/Export 정책 필요.

BZA가 하지 않는 일: 주문·계약·결제 같은 업무 최종 상태 소유.

## 2. 설치와 초기 관리자

1. Artifact/Checksum.
2. DB Vendor Pack.
3. Menu/Permission Seed.
4. 초기 관리자 계정.
5. MFA/Password/Session 정책.
6. Root 조직/대표 직원/사용자 연결.
7. 관리자 Role Simulation.
8. Backup/Restore/Login Smoke.
9. 초기 Credential 교체/Audit.

## 3. 조직

입력: code, name, parent, type, validFrom/To, status, expectedVersion, reason.
검증: cycle 방지, 기준일 중복 방지, 상위 유효기간, 폐기 영향 분석.
정상 판정: 기준일 조직 Path/Level과 History 일치.

## 4. 직원·발령

- 직원 Profile과 Login User 분리.
- 입사/이동/겸직/파견/휴직/퇴직을 기준일로 관리.
- 대표 Assignment와 추가 Assignment를 구분.
- 과거 Snapshot을 현재 조직 변경으로 덮어쓰지 않는다.

## 5. 사용자·Session

사용자: loginId, employeeId, status, MFA, valid period, password policy.
Session: issueAt, lastAccess, IP/device, expiry, revoke reason.
고위험 Role 변경/비밀번호 초기화/퇴직 시 Session 폐기 정책을 적용한다.

## 6. Role·Permission·Data Scope·Masking

세 층을 구분한다.

1. Menu 진입.
2. Action/API 실행.
3. Data Scope.

Masking/Unmask는 별도 Permission이며 원문 조회 자체를 감사한다.

Effective Permission은 Allow/Deny 우선순위, Role 유효기간, 조직 기준일, Data Scope, Resource/Action, Masking을 함께 계산한다.

## 7. Permission Simulation

입력: user, asOf, resource, action, target.
출력: allow/deny, matched role/policy, data scope, masking, reason.
실제 업무 API의 Decision과 Simulation 결과를 비교한다.

## 8. 결재 Policy

- Action/위험 조건.
- Policy Version.
- 단계/승인자 Role.
- 요청자-승인자 분리.
- 순차/병렬.
- 만료/Escalation.
- 위임/대결 범위.
- Snapshot/Checksum.

`DRAFT -> APPROVED -> RETIRED` 같은 Policy Lifecycle을 사용하고 진행 중 문서는 생성 시점 Policy/Snapshot을 보존한다.

## 9. 결재 Lifecycle

`DRAFT -> SUBMITTED -> IN_REVIEW -> APPROVED | REJECTED | WITHDRAWN | EXPIRED -> EXECUTED`

실행 전 Approval Snapshot과 현재 Target Version/Hash를 비교한다. Drift가 있으면 재승인을 요구한다.

응답 유실은 결재 완료와 실제 업무 실행 결과를 별도로 대사한다.

## 10. 위임·대결

- 위임자/대리자.
- 기간.
- 조직/업무/Action 범위.
- 금지 권한.
- 자기 승인 방지.
- 결재 Snapshot에 실제 Actor와 Delegator 모두 기록.

## 11. Attachment·Notification

Attachment는 Upload/Scan/Checksum/Quarantine/Download Permission/Expiry/Audit.
Notification은 Template/Channel/Recipient/Attempt/Retry/Cancel/DLQ와 원문 민감정보 정책을 가진다.

## 12. Session·Masking·Audit·Export

Export는 별도 Permission, Reason, Query Snapshot, Row Limit, Masking Policy, 만료 Token, Download Audit를 사용한다.

## 13. 업무 Domain 연계

업무 Service는 BZA의 Effective Permission/Approval 결과를 **Consumer**로 사용하지만 최종 업무 상태를 스스로 소유한다.

연계 시 고정할 것:

- Resource/Action Naming.
- Target Key.
- As-of Date.
- Required Permission.
- Data Scope Mapping.
- Masking Field.
- Approval Policy/Version.
- Audit Correlation ID.

## 14. 확장

고객 전용 조직 속성/Permission/Approval 조건을 추가할 때 기존 Public Contract와 기준일/Version 의미를 유지한다. 내부 BZA Table 직접 의존 대신 공개 API/SPI를 사용한다.

## 15. Backup·Restore

조직 History, Assignment, User/Role, Permission, Policy/Approval, Session/Audit/Attachment Metadata의 일관성을 보존한다. Restore 뒤 Effective Permission Simulation과 진행 중 Approval Snapshot을 검증한다.

## 16. Upgrade·Rollback

Schema/API/Permission Seed/Frontend를 함께 Versioning한다. Mixed Version에서 기존 Approval/Permission Decision이 해석 가능한지 확인한다.

## 17. 운영 장애

- 잘못된 조직 기준일 → 과거/현재 Snapshot 비교.
- Permission 과다/부족 → Effective Simulation과 실제 API Decision 대조.
- Approval stuck → participant/expiry/delegation/operation 확인.
- Session 과다 → revoke 정책과 Audit.
- Attachment scan 실패 → Quarantine.
- Export 응답 유실 → Export Job/Artifact 상태 조회.

## 18. 조직·권한·결재 통합 실습

Root 조직→부서→직원→사용자→Role→Permission→Data Scope→Simulation→Approval Policy→상신/승인→업무 실행→Audit까지 수행한다. 중간에 조직 이동과 Approval Snapshot Drift를 재현해 재승인 절차를 확인한다.

## 19. 완료 체크리스트

- [ ] 조직/직원/발령의 기준일이 있다.
- [ ] User/Employee/Session이 분리된다.
- [ ] Role/Permission/Scope/Masking이 분리된다.
- [ ] Simulation으로 Effective 결과를 설명할 수 있다.
- [ ] Approval Snapshot/Version/Expiry가 있다.
- [ ] 위임/대결/자기 승인 방지가 있다.
- [ ] Attachment/Notification/Audit/Export가 있다.
- [ ] 업무 Domain이 최종 상태 Owner다.
- [ ] Backup/Restore/Upgrade/Rollback이 있다.

## 20. 기준일·History의 업무 적용

조직·발령·Role·Permission을 현재값만 조회하지 않는다. 거래 발생시점, 결재 상신시점, 감사 조사시점에 맞는 `asOf` 기준으로 Effective 결과를 재현할 수 있어야 한다.

## 21. Approval 무결성

승인 요청은 Target ID뿐 아니라 Action, Policy Version, Target Version/Hash, 주요 변경값, Requester, Expiry를 Snapshot으로 고정한다. 실행 시 동일 Snapshot인지 비교한다.

## 22. 개인정보와 Retention

직원/사용자/첨부/감사/Export 데이터의 Retention, Archive, Legal Hold, 삭제 요청, Backup 예외를 분리한다. 삭제 후 복구된 Backup에서 재삭제가 필요한지 절차를 정한다.

## 23. BZA 운영 화면 기준

현재 Router는 인증 Session을 복원한 뒤 각 Route의 `menuCode`를 `hasBzaMenu(menuCode)`로 검사한다. 인증되지 않은 사용자는 Dashboard 로그인 흐름으로 이동하고, Menu 권한이 없으면 `/forbidden`, 비활성 기능은 `/feature-disabled`, lazy-load 실패는 `/lazy-load-failure`, 미등록 URL은 404 상태 화면으로 이동한다.

운영 화면은 아래 26개 Route를 정본으로 사용한다. `expectedOperationIds`는 해당 화면에서 허용된 API/Operation 표면이며, Component에 없는 Mutation을 문서나 운영 절차에서 추가하지 않는다.


### 23.1 실제 BZA Route 26개

| ID | Path | 화면 | Menu | Group | 목적 | Expected Operation IDs |
|---|---|---|---|---|---|---|
| `dashboard` | `/` | 대시보드 | `DASHBOARD` | `overview` | 업무 운영 현황 | `bzaSupportDashboard` |
| `organizations` | `/organizations` | 조직 | `ORGANIZATION` | `people` | 조직 계층 | `bzaBackofficeFindOrganizations`, `bzaBackofficeSaveOrganization`, `bzaBackofficeFindOrganizationsPage` |
| `employees` | `/employees` | 직원 | `EMPLOYEE` | `people` | 직원 Profile | `bzaBackofficeFindEmployees`, `bzaBackofficeSaveEmployee`, `bzaBackofficeFindEmployeesPage`, `bzaBackofficeEmployeeRawContact` |
| `positions` | `/positions` | 직급 | `EMPLOYEE` | `people` | 직급 기준정보 | `bzaDirectoryFindPositions`, `bzaDirectorySavePosition`, `bzaDirectoryFindPositionsPage` |
| `jobTitles` | `/jobTitles` | 직책 | `EMPLOYEE` | `people` | 직책 기준정보 | `bzaDirectoryFindJobTitles`, `bzaDirectorySaveJobTitle`, `bzaDirectoryFindJobTitlesPage` |
| `assignments` | `/assignments` | 발령·겸직 | `EMPLOYEE` | `people` | 다중 소속·겸직·파견·대행 | `bzaDirectoryFindAssignments`, `bzaDirectorySaveAssignment`, `bzaDirectoryFindAssignmentsPage` |
| `organizationResponsibilities` | `/organizationResponsibilities` | 조직 책임 | `ORGANIZATION` | `people` | 조직장·대행·승인 Owner | `bzaDirectoryFindResponsibilities`, `bzaDirectorySaveResponsibility`, `bzaDirectoryFindResponsibilitiesPage` |
| `users` | `/users` | 사용자 | `AUTHORIZATION` | `access` | BZA 인증 사용자 | `bzaOperationFindAdminUsers`, `bzaOperationSaveAdminUser`, `bzaOperationFindAdminUsersPage` |
| `roles` | `/roles` | 역할 | `AUTHORIZATION` | `access` | 업무 역할 | `bzaOperationFindRoles`, `bzaOperationSaveRole`, `bzaOperationFindRolesPage` |
| `userRoles` | `/userRoles` | 사용자 Role | `AUTHORIZATION` | `access` | 다중 Role 유효기간 | `bzaDirectoryFindUserRoles`, `bzaDirectorySaveUserRole`, `bzaDirectoryFindUserRolesPage` |
| `menus` | `/menus` | 메뉴 | `AUTHORIZATION` | `access` | 화면 메뉴 Registry | `bzaOperationFindMenus`, `bzaOperationSaveMenu`, `bzaOperationFindMenusPage`, `bzaOperationDeleteMenu`, `bzaOperationFindMenuImpact` |
| `permissions` | `/permissions` | 권한 | `AUTHORIZATION` | `access` | 화면·행위·API·Data Scope 권한 | `bzaOperationFindPermissions`, `bzaOperationSavePermission`, `bzaOperationFindPermissionsPage` |
| `permissionTools` | `/permissionTools` | 권한 분석 | `AUTHORIZATION` | `access` | Role 비교와 권한 Simulation | `bzaBackofficeFindEffectivePermissions`, `bzaSupportCompareRolePermissions`, `bzaSupportSimulatePermission` |
| `approvalInbox` | `/approvalInbox` | 결재 처리 | `APPROVAL` | `approval` | Snapshot 참여자 Inbox | `bzaApprovalInbox`, `bzaBackofficeFindApprovals`, `bzaBackofficeCreateApproval`, `bzaBackofficeFindApproval`, `bzaBackofficeActApproval` |
| `approvalSubmissions` | `/approvalSubmissions` | 결재 상신 | `APPROVAL` | `approval` | 정책 기반 멱등 상신 | `bzaApprovalSubmissions`, `bzaApprovalPolicySubmit`, `bzaApprovalExpireDue`, `bzaApprovalSubmissionDetail`, `bzaApprovalCancel`, `bzaApprovalResubmit`, `bzaApprovalWithdraw`, `bzaApprovalParticipantDecision` |
| `approvalPolicies` | `/approvalPolicies` | 결재 정책 | `APPROVAL` | `approval` | Versioned Policy/ALL·ANY·N_OF_M | `bzaApprovalPolicies`, `bzaApprovalPolicySave`, `bzaApprovalPolicyDetail` |
| `approvalSimulation` | `/approvalSimulation` | 경로 Simulation | `APPROVAL` | `approval` | 조직/Role/위임 사전 해석 | `bzaApprovalPolicySimulate` |
| `approvalDelegations` | `/approvalDelegations` | 결재 위임 | `APPROVAL` | `approval` | 유효기간 위임·대결 | `bzaApprovalDelegations`, `bzaApprovalDelegationSave` |
| `sessions` | `/sessions` | 내 세션 | `AUTHORIZATION` | `support` | Refresh session 관리 | `bzaAuthSessions`, `bzaAuthRevokeSession` |
| `audits` | `/audits` | 업무 감사 | `AUDIT` | `support` | Immutable 업무 감사 | `bzaBusinessAuditVerify`, `bzaBackofficeFindBusinessAudits` |
| `notifications` | `/notifications` | 알림 | `SETTING` | `support` | 업무 알림 | `bzaSupportFindNotifications`, `bzaSupportCreateNotification`, `bzaSupportReadAllNotifications`, `bzaSupportReadNotification` |
| `attachments` | `/attachments` | 첨부파일 | `ATTACHMENT` | `support` | 첨부 업로드·검증 | `bzaSupportFindAttachments`, `bzaSupportUploadAttachment`, `bzaSupportDownloadAttachment`, `bzaSupportRecheckAttachment`, `bzaSupportUpdateAttachmentSecurity` |
| `savedSearches` | `/savedSearches` | 저장 검색 | `SETTING` | `support` | 사용자 검색 조건 | `bzaSupportFindSavedSearches`, `bzaSupportSaveSavedSearch`, `bzaSupportDisableSavedSearch` |
| `settings` | `/settings` | 업무 설정 | `SETTING` | `support` | BZA 업무 설정 | `bzaOperationFindSettings` |
| `downloads` | `/downloads` | 다운로드 정책 | `SETTING` | `support` | 다운로드 정책 | `bzaOperationFindDownloadPolicies` |
| `downloadAudits` | `/downloadAudits` | 다운로드 감사 | `AUDIT` | `support` | 다운로드 감사 | `bzaSupportFindDownloadAudits` |

### 23.2 화면 운영 공통 규칙

1. 진입 전 인증 Session과 Menu 권한을 확인한다.
2. 목록/상세 화면은 기준일·Data Scope·Masking을 먼저 확인한다.
3. `Save/Create/Act/Delete/Revoke/Upload/Download` Operation이 있는 화면만 해당 Mutation 절차를 제공한다.
4. 변경 시 expected version/idempotency/reason/approval 계약이 API에 있으면 반드시 함께 전달한다.
5. 409는 최신 Snapshot 재조회, Timeout/응답 유실은 operation/status 대사, 부분 반영은 대상별 reconcile로 처리한다.
6. 원문·Raw Contact·Attachment·Download·Audit는 별도 Permission/Masking/Reason/다운로드 감사를 확인한다.

### 23.3 Source 검수 예 — 조직 화면

`OrganizationsPage.vue`는 조직명·코드 검색, `중지 조직 포함=true`, 고아/순환 조직 탐지, 조직 코드/명/상위/유형/사용 여부/하위 수 상세를 제공한다. 이 화면 Component에는 Save Button이 없으므로 Route Registry에 `bzaBackofficeSaveOrganization` Operation이 있더라도 **현재 조직도 Component에서 직접 저장한다고 쓰지 않는다**. 화면 Capability와 API Registry를 같은 것으로 오인하지 않는다.

## 24. BZA Reference EDU 14개 전수 지도

| ID | 예제 | 핵심 확인 | 장애·복구 관점 |
|---|---|---|---|
| `EDU-BZA-01` | 조직·직원·발령·기준일 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-02` | 사용자·역할·권한·실효 권한 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-BZA-03` | 결재정책 Version·경로 사전 계산 | version/idempotency/lease 소유권과 경쟁 요청 결과 | stale writer 차단, 최신 상태 재조회 후 재판단 |
| `EDU-BZA-04` | 상신·승인·반려·철회·취소 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-05` | 위임·대결·대행 책임 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-06` | 첨부·알림·감사·다운로드 | messageId/key/order/ACK·consumer idempotency·DLQ | 중복 소비 차단, replay 승인, 미종결 원장 대사 |
| `EDU-BZA-07` | 초기 관리자 Bootstrap·첫 로그인·권한 인계 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-BZA-08` | 조직 개편·기준일·과거 이력 유지 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-09` | 입사·이동·휴직·퇴사 Joiner-Mover-Leaver | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-10` | 역할 충돌·직무분리·실효 권한 Simulation | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-BZA-11` | 위임 중첩·기간 만료·결재 경로 재계산 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-BZA-12` | 계정 잠금·비밀번호 초기화·세션 강제 종료 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-BZA-13` | 개인정보 Masking·감사 조회·승인 Export | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-BZA-14` | 고객 업무 승인 결과 반영·실패 Rollback | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |

공통 실행 역할: `CPF_EDU_BZA_OPERATOR`. 각 ID의 **정확한 requiredFields·businessStates·exceptionScenarios·requiredVerification·handler/source/test/timeout**은 기준 Commit의 `cpf-reference/src/main/resources/edu/manual-135-catalog.json`과 동일하게 유지한다. 매뉴얼에서는 그 계약을 업무 절차에 연결하며, 임의 필드를 추가하지 않는다.



<!-- R17-EDU-BZA-DETAIL-BEGIN -->
## 24A. EDU-BZA 전수 실행 카드 — 14개

아래 카드는 `manual-135-catalog.json`의 ID를 잃지 않고 매뉴얼 업무 절차로 연결한다. **정확한 필드명·상태·Handler·Source·Test는 같은 ID의 정본 값을 사용하며 문서가 별도 제2 정본을 만들지 않는다.** 대신 독자는 각 ID에서 무엇을 준비하고 무엇을 실패시켜 어떻게 정상화를 판정하는지 이 절만으로 이해할 수 있어야 한다.

### EDU-BZA-01 — 조직·직원·발령·기준일

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-01/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-01` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-02 — 사용자·역할·권한·실효 권한

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-02/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-02` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-03 — 결재정책 Version·경로 사전 계산

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; expected version; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-03/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; stale version/경쟁 갱신.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-03` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-04 — 상신·승인·반려·철회·취소

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-04/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-04` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-05 — 위임·대결·대행 책임

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-05/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-05` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-06 — 첨부·알림·감사·다운로드

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; messageId/key/schema version; file/checksum/size; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-06/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; duplicate/late ACK/broker outage/DLT; partial file·checksum mismatch·disk full·중단.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-06` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-07 — 초기 관리자 Bootstrap·첫 로그인·권한 인계

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-07/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-07` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-08 — 조직 개편·기준일·과거 이력 유지

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-08/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-08` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-09 — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-09/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-09` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-10 — 역할 충돌·직무분리·실효 권한 Simulation

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-10/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-10` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-11 — 위임 중첩·기간 만료·결재 경로 재계산

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-11/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-11` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-12 — 계정 잠금·비밀번호 초기화·세션 강제 종료

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-12/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-12` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-13 — 개인정보 Masking·감사 조회·승인 Export

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-13/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-13` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-BZA-14 — 고객 업무 승인 결과 반영·실패 Rollback

- **역할:** `CPF_EDU_BZA_OPERATOR`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; asOf/policy version/effective permission.
- **실행:** `POST /api/reference/edu-capabilities/EDU-BZA-14/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; effective permission/approval snapshot. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 조직/권한/결재 Snapshot과 Owner 업무 상태를 분리해 복구.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-BZA-14` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.
<!-- R17-EDU-BZA-DETAIL-END -->

## 25. 승인 결과의 업무 반영

`EDU-BZA-14`는 `approvalId`, `businessOperationId`, `approvalPolicyId`를 연결하고 `APPROVED → APPLYING → APPLIED`를 정상 흐름으로 본다. 응답 유실·중복 Callback·승인 후 업무 Version 변경·부분 반영은 `UNKNOWN_RESULT`로 분리하고, Owner 상태를 대사한 뒤 compensation/rollback 또는 수동 확정을 수행한다.







## 26. BZA 운영 최종 Gate

조직/직원/발령 기준일, JML, 사용자/Role/Permission/Data Scope, SoD Simulation, 결재 Policy Version, 상신/승인/반려/철회/취소, 위임/대결, Attachment/Notification, Session/Masking/Audit/Export, 업무 반영/Compensation, Backup/Restore/Upgrade/Rollback과 BZA 14/14 EDU가 모두 연결되어야 한다.
