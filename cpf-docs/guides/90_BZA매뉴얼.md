# CPF BZA 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF`
> **기준 Branch** `master`
> **기준 Commit** `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
> **문서 목적** BZA 도입 판단, 설치·초기 관리자, 조직·직원·사용자, Role·Permission·Data Scope, 결재·위임, Attachment·Notification, Session·Audit·Export, Domain 연계·복구를 설명한다.
> **주요 독자** BZA 도입 책임자, 개발자, 업무 관리자, 권한·조직·결재 운영자
> **문서 사용 결과** BZA를 선택 설치하고 업무 관리 기능을 권한·승인·감사·복구 계약과 함께 운영한다.

## 0. 문서 사용 계약

이 문서는 제품 목표, 기준 Commit의 구현, 실제 실행 검증을 분리한다.

- 목표는 구현·검증 여부와 무관한 제품 계약이다.
- 기능 설명은 최신 Source·SQL·API·Config·Frontend·Script·Test의 exact path를 기준으로 한다.
- 적용 환경에서는 Build·DB·Kafka·Browser·다중 인스턴스·장애 시나리오의 실행 결과를 환경 기록에 남긴다.
- Source에 없는 Class·API·Property·Route·Permission·상태를 만들지 않는다.
- 기능 상태와 운영 상태는 Owner가 정의한 실제 상태값과 Terminal 조건을 사용한다.
- 명령 실행 전 Local Working Tree를 확인하고 기존 변경을 보호한다.


## 1. 선택 기준

BZA 적용 후보:

- 조직·직원·사용자와 다중 소속
- Role·Permission·Data Scope
- 업무 결재·위임·대결
- Attachment·Notification·Saved Search
- 업무 감사·다운로드 통제

BZA를 사용하지 않는 시스템은 Dependency·DB·기동 조건으로 강제하지 않는다. 플랫폼 Runtime 제어는 ADM 소유다.

## 2. 기능 구성과 Source 지도

| 기능군 | Source | 사용 업무 |
|---|---|---|
| Frontend Route | `cpf-biz-admin/frontend/src/app/routes.ts` | 26개 업무 화면과 Menu Code를 제공한다. |
| 인증·Session | Security Starter·BZA Auth API | 로그인·Refresh Session·폐기·동시 Session 정책을 사용한다. |
| Bootstrap | `BzaBootstrapRunner`, V85·V91 | 승인 Token과 Secret File로 최초 관리자를 생성하고 Operation을 대사한다. |
| 조직·인사 | Organization·Employee·Position·JobTitle·Assignment·Responsibility | 조직 계층·직원·다중 소속·책임을 기준일로 관리한다. |
| 권한 | User·Role·UserRole·Menu·Permission | Menu·행위·API·Data Scope와 실효 권한을 관리한다. |
| 결재 | Policy·Submission·Inbox·Simulation·Delegation | Versioned 정책·Snapshot·상신·결정·위임·대결을 처리한다. |
| 지원 기능 | Attachment·Notification·Saved Search·Setting | 파일·알림·사용자 조건·업무 설정을 관리한다. |
| 감사·Download | Audit·Download Policy·Download Audit | Masking·Export·다운로드 사유와 이력을 관리한다. |
| OpenAPI·Frontend | Backend OpenAPI·Orval Client·Bundle Manifest | API·DTO·Error·Page Consumer를 같은 Source SHA로 연결한다. |
| DB Lifecycle | Oracle·PostgreSQL·MariaDB Migration·Rollback | 신규 설치·Upgrade·Backup·Restore를 수행한다. |

## 3. Route 지도

| 그룹 | Route ID | Path | Menu Code | 역할 |
|---|---|---|---|---|
| overview | `dashboard` | `/` | `DASHBOARD` | 업무 운영 현황 |
| people | `organizations` | `/organizations` | `ORGANIZATION` | 조직 계층 |
| people | `employees` | `/employees` | `EMPLOYEE` | 직원 Profile |
| people | `positions` | `/positions` | `EMPLOYEE` | 직급 |
| people | `jobTitles` | `/jobTitles` | `EMPLOYEE` | 직책 |
| people | `assignments` | `/assignments` | `EMPLOYEE` | 발령·겸직 |
| people | `organizationResponsibilities` | `/organizationResponsibilities` | `ORGANIZATION` | 조직 책임 |
| access | `users` | `/users` | `AUTHORIZATION` | 사용자 |
| access | `roles` | `/roles` | `AUTHORIZATION` | Role |
| access | `userRoles` | `/userRoles` | `AUTHORIZATION` | 사용자 Role |
| access | `menus` | `/menus` | `AUTHORIZATION` | 메뉴 |
| access | `permissions` | `/permissions` | `AUTHORIZATION` | 권한·Data Scope |
| access | `permissionTools` | `/permissionTools` | `AUTHORIZATION` | 권한 Simulation |
| approval | `approvalInbox` | `/approvalInbox` | `APPROVAL` | 결재 처리 |
| approval | `approvalSubmissions` | `/approvalSubmissions` | `APPROVAL` | 결재 상신 |
| approval | `approvalPolicies` | `/approvalPolicies` | `APPROVAL` | 결재 정책 |
| approval | `approvalSimulation` | `/approvalSimulation` | `APPROVAL` | 경로 Simulation |
| approval | `approvalDelegations` | `/approvalDelegations` | `APPROVAL` | 위임·대결 |
| support | `sessions` | `/sessions` | `AUTHORIZATION` | 세션 |
| support | `audits` | `/audits` | `AUDIT` | 업무 감사 |
| support | `notifications` | `/notifications` | `SETTING` | 알림 |
| support | `attachments` | `/attachments` | `ATTACHMENT` | 첨부 |
| support | `savedSearches` | `/savedSearches` | `SETTING` | 저장 검색 |
| support | `settings` | `/settings` | `SETTING` | 업무 설정 |
| support | `downloads` | `/downloads` | `SETTING` | 다운로드 정책 |
| support | `downloadAudits` | `/downloadAudits` | `AUDIT` | 다운로드 감사 |

Route 존재는 CRUD·권한·결재 상태 기계의 실행 확인을 뜻하지 않는다.

## 4. 설치 전 준비

- BZA 선택 결정과 Owner 승인
- Artifact SHA·Source SHA
- BZA DB Schema·Runtime/Migration 계정
- Session JDBC Schema
- Secret/Approval Token 발급 절차
- 조직·사용자 Identifier 정책
- Role·Permission·Data Scope Model
- 결재 정책과 감사 Retention
- Attachment Storage·검사·Retention

## 5. 최초 관리자 Bootstrap

필수 Property는 05 매뉴얼의 BZA Bootstrap 표를 따른다.

Source 동작:

1. Approval Token File이 없으면 Bootstrap을 수행하지 않는다.
2. legacy enable 또는 plaintext password Property가 있으면 중단한다.
3. Token·Password File의 Regular File·Symlink·Size를 검증한다.
4. Environment Fingerprint·Operation ID를 생성한다.
5. Approval Token을 Claim한다.
6. Password 정책을 검증하고 Operator·Role을 생성한다.
7. Approval을 Complete/Fail로 기록한다.
8. Secret char array를 지우고 File 덮어쓰기·삭제를 시도한다.

운영 확인:

- Token은 Environment·Scope·Expiry에 묶는다.
- Password File Permission과 OS Secret Store를 확인한다.
- Bootstrap 후 Token 재사용이 거부되는지 확인한다.
- 실패 시 부분 생성·Approval 상태를 대사한다.
- 생성된 관리자 ID·Role·Audit을 확인한다.

## 6. 조직·직원·발령

### 조직

- 조직 ID·상위 조직·유효기간·상태·Version
- 순환 계층 차단
- 조직장·대행·승인 Owner
- 폐쇄 시 사용자·결재·Data Scope 영향 Preview

### 직원·발령

- 직원 ID·사용자 연결·재직 상태
- 직급·직책·다중 소속·겸직·파견
- Effective Date·이력·Expected Version
- 퇴직·휴직 시 Session·Role·Approval 위임 처리

## 7. 사용자·Role·Permission·Data Scope

- User와 Employee를 동일 개념으로 강제하지 않는다.
- Role 부여는 유효기간·Requester·Approver·Reason·Audit을 가진다.
- Permission은 Menu·Route·Action·API·Data Scope·Raw·Export를 분리한다.
- 권한 Simulation은 실제 Backend 판정과 같은 Engine을 사용해야 한다.
- 권한 회수 후 기존 Session·Cache·Token이 남지 않는지 확인한다.

## 8. 결재·위임·대결

결재 정책:

- Version·Effective Period
- ALL·ANY·N_OF_M
- 조직·Role·사용자 Participant Snapshot
- 자기승인 금지·Requester/Approver 분리
- Expiry·Cancel·Reject·Withdraw
- Delegation·Acting 기간과 Scope
- Immutable Command Hash

상신은 Idempotency Key+Request Hash를 사용한다. 응답 유실 시 Submission 상태와 Participant Snapshot을 조회한다.

## 9. Attachment·Notification

Attachment:

- Size·Type·Checksum·Malware·Storage Reference
- Permission·Data Scope·Masking
- Upload partial·cancel·cleanup
- Download Reason·Audit·Retention·Deletion

Notification:

- Template Version·Variable Schema·Channel
- Retry·Duplicate·Failure·Opt-out
- 민감정보 최소화
- 업무 상태와 발송 상태 분리

## 10. Session·Masking·Audit·Export

Session:

- Cookie `CPFSESSION`, 기본 30분, SameSite Strict
- TLS 환경 Secure Cookie
- Fixation 보호·Rotation·Concurrent Session·Forced Logout
- JDBC Store Readiness와 Schema 확인

Audit:

- 조직·권한·결재·Raw·Export·Bootstrap 변경 전후
- Operator·Reason·Approval·Request Hash·Result
- Credential·PII Redaction

Export:

- Data Scope·Column Masking·Row Limit
- Reason·Approval·Watermark·Expiry
- Download Audit·Storage Cleanup

## 11. 업무 Domain 연계

BZA는 업무 상태를 직접 소유하지 않는다. Domain은 Public Query/Command를 제공하고 BZA는 조직·권한·결재 결과를 전달한다.

- User/Org Reference는 Version과 유효기간을 포함한다.
- Remote Timeout·Unknown·Reconcile을 제공한다.
- BZA 장애가 핵심 업무를 차단하는 정책인지 명시한다.
- Cache 사용 시 권한 회수 지연 상한을 기록한다.

## 12. Backup·Restore·Upgrade

Backup: BZA DB, Session DB, Attachment Metadata/Data, Config, Approval/Audit, Key Metadata.

Restore 후:

- 조직 계층·User·Role·Permission Count
- Pending Approval·Delegation 기간
- Session 강제 종료 여부
- Attachment Checksum
- Audit Sequence·Download 기록
- Domain Reference 대사

Upgrade는 DB Migration·Frontend/API Compatibility·Session Cookie·Policy Version을 확인한다.

## 13. 장애 Runbook

### Bootstrap 실패

Approval 상태, Token Scope·Expiry, File Permission·Symlink·Size, Password 정책, 부분 생성 여부 확인. Token을 재사용하지 않고 실패 원장과 DB를 대사한다.

### 권한 오작동

Backend Permission·Data Scope, Role 유효기간, Cache, Session, 조직 변경 시각을 확인. Frontend Menu만 수정하지 않는다.

### 결재 정지

Policy Version, Participant Snapshot, Delegation, Expiry, Lock, Unknown Submission을 확인. 승인자 임의 DB Update 금지.

### Session 장애

DB 연결·Schema·Index·Cookie·Clock·Load Balancer 확인. 제품 Profile에서 Memory Session으로 우회하지 않는다.

### Attachment 장애

Storage·Disk·Checksum·Temp·Malware 검사·Permission 확인. Metadata와 실제 Object를 대사한다.

## 14. EDU

1. 빈 BZA DB와 Session DB 준비.
2. Approval Token·Password Secret File 준비.
3. Bootstrap 실행과 재사용 거부 확인.
4. 조직·직원·사용자·Role·Permission 생성.
5. Data Scope로 조회 범위 확인.
6. ALL/ANY/N_OF_M 결재와 위임 생성.
7. Attachment·Notification·Export 실행.
8. 권한 회수·Session 만료·Response loss 주입.
9. Backup·Restore 후 대사.
10. Browser·Audit·Evidence 확인.

## 15. 적용 환경 확인 항목

- Route와 Bootstrap Source는 확인했다.
- 조직·권한·결재·Session·Attachment의 DB·API·Browser Scenario를 Release마다 실행한다.
- Permission·Field·Button 전수 Inventory는 미실행이다.

## 부록 A. BZA Route 전수 지도

Source: `cpf-biz-admin/frontend/src/app/routes.ts`.

| 그룹 | Route ID·Path | Menu Code | 화면 목적 |
|---|---|---|---|
| 개요 | `dashboard` `/` | `DASHBOARD` | 업무 운영 현황 |
| 조직 | `organizations` `/organizations` | `ORGANIZATION` | 조직 계층 |
| 조직 | `employees` `/employees` | `EMPLOYEE` | 직원 Profile |
| 조직 | `positions` `/positions` | `EMPLOYEE` | 직급 기준정보 |
| 조직 | `jobTitles` `/jobTitles` | `EMPLOYEE` | 직책 기준정보 |
| 조직 | `assignments` `/assignments` | `EMPLOYEE` | 다중 소속·겸직·파견·대행 |
| 조직 | `organizationResponsibilities` `/organizationResponsibilities` | `ORGANIZATION` | 조직장·대행·승인 Owner |
| 권한 | `users` `/users` | `AUTHORIZATION` | 인증 사용자 |
| 권한 | `roles` `/roles` | `AUTHORIZATION` | 업무 Role |
| 권한 | `userRoles` `/userRoles` | `AUTHORIZATION` | Role 유효기간 |
| 권한 | `menus` `/menus` | `AUTHORIZATION` | Menu Registry |
| 권한 | `permissions` `/permissions` | `AUTHORIZATION` | 화면·행위·API·Data Scope |
| 권한 | `permissionTools` `/permissionTools` | `AUTHORIZATION` | Role 비교·Simulation |
| 결재 | `approvalInbox` `/approvalInbox` | `APPROVAL` | 결재 Inbox |
| 결재 | `approvalSubmissions` `/approvalSubmissions` | `APPROVAL` | 멱등 상신 |
| 결재 | `approvalPolicies` `/approvalPolicies` | `APPROVAL` | Versioned ALL·ANY·N_OF_M 정책 |
| 결재 | `approvalSimulation` `/approvalSimulation` | `APPROVAL` | 조직·Role·위임 경로 해석 |
| 결재 | `approvalDelegations` `/approvalDelegations` | `APPROVAL` | 위임·대결 유효기간 |
| 지원 | `sessions` `/sessions` | `AUTHORIZATION` | Session 관리 |
| 지원 | `audits` `/audits` | `AUDIT` | 업무 감사 |
| 지원 | `notifications` `/notifications` | `SETTING` | 업무 알림 |
| 지원 | `attachments` `/attachments` | `ATTACHMENT` | 첨부 업로드·검증 |
| 지원 | `savedSearches` `/savedSearches` | `SETTING` | 사용자 검색 조건 |
| 지원 | `settings` `/settings` | `SETTING` | BZA 업무 설정 |
| 지원 | `downloads` `/downloads` | `SETTING` | Download 정책 |
| 지원 | `downloadAudits` `/downloadAudits` | `AUDIT` | Download 감사 |

Route 존재는 Backend API·Permission·DB 상태·Browser 동작 검증을 뜻하지 않는다. 각 화면은 Menu Code와 Backend Authorization을 함께 확인한다.

## 부록 B. 최초 관리자 Bootstrap Property와 절차

### B.1 Property

| Key | 설명 | 기본·제약 |
|---|---|---|
| `cpf.bza.bootstrap.approval-token-file` | 사전 승인 1회 Token 파일 | 설정 시 Bootstrap 시작 |
| `cpf.bza.bootstrap.password-file` | 초기 Password 파일 | 필수, Symlink 금지, 4KiB 이하 |
| `cpf.bza.bootstrap.login-id` | 초기 Login ID | 필수 |
| `cpf.bza.bootstrap.operator-name` | 운영자 이름 | 필수 |
| `cpf.bza.bootstrap.role-code` | 초기 Role | 기본 `BZA_MANAGER` |
| `cpf.bza.bootstrap.approval-scope` | 승인 Scope | 필수 |
| `cpf.bza.bootstrap.operation-id` | 멱등 Operation ID | 선택, 형식 제한 |
| `cpf.environment.code` | 환경 코드 | Fingerprint 구성에 필수 |

Legacy `cpf.bza.bootstrap.enabled` 또는 평문 `cpf.bza.bootstrap.password`가 있으면 실행을 거부한다.

### B.2 사전 준비

1. Approval Repository에 Token Hash·Environment Fingerprint·Scope·만료·상태를 등록한다.
2. Token과 Password를 서로 다른 제한 Directory의 일반 파일로 생성한다.
3. Service Account만 읽도록 Permission을 제한한다.
4. 초기 Password는 14자 이상이고 대문자·소문자·숫자·특수문자 중 3종 이상을 포함하며 Login ID를 포함하지 않게 한다.
5. 기존 관리자·Role·조직 Seed와 충돌 여부를 확인한다.

### B.3 실행 결과

- Approval Claim 실패: 상태·만료·Scope·중복 Operation을 확인한다.
- Operator 생성 성공: Approval을 완료 상태로 바꾸고 Admin User ID를 연결한다.
- 생성 실패: Approval 실패 사유를 기록한다.
- finally 단계에서 Token·Password 배열을 지우고 파일 덮어쓰기·삭제를 시도한다.

파일 덮어쓰기·삭제만으로 저장장치 원문 제거를 단정하지 않는다. 운영 환경에서는 tmpfs·Secret Volume·OS Secret Store와 Backup 제외 정책을 적용한다.

## 부록 C. 조직·직원·발령 운영 순서

1. 조직 Code·상위 조직·유효기간·상태를 등록한다.
2. 조직 책임자와 대행자를 유효기간으로 연결한다.
3. 직원 Profile을 등록하고 개인정보 Masking·원문 권한을 확인한다.
4. 발령·겸직·파견·대행은 시작·종료일과 Primary 여부를 명시한다.
5. 변경 후 User·Role·Approval Path·Data Scope Simulation을 실행한다.
6. 종료일 경계와 겹치는 Assignment를 검증한다.
7. Audit Before/After와 영향을 받는 결재 진행 건을 확인한다.

## 부록 D. Role·Permission·Data Scope

- User와 Employee를 같은 식별자로 가정하지 않는다.
- Role은 유효기간과 상태를 가진다.
- Menu·Button·API Permission과 Data Scope를 분리한다.
- 권한 축소는 기존 Session·Cache·결재 위임에 반영되는지 확인한다.
- Simulation 결과와 실제 Backend 판정을 같은 사용자·시각·조직 Version으로 비교한다.

권한 변경 전후 Test:

| Test | 확인 |
|---|---|
| Menu | Route 표시·Direct URL |
| Button | 조회 가능하지만 변경 불가한 상태 |
| API | 직접 호출 403 |
| Data Scope | 다른 조직·자기 조직·위임 범위 |
| Raw | Masking 조회와 원문 조회 분리 |
| Session | 기존 Session 권한 회수 |

## 부록 E. 결재·위임·대결

### E.1 정책

- Policy Version과 유효기간을 고정한다.
- `ALL`, `ANY`, `N_OF_M` 참여자 해석을 기록한다.
- Requester와 Approver 분리, 자기승인 금지를 확인한다.
- 상신 시 Participant Snapshot과 Command Hash를 저장한다.

### E.2 위임

- 위임자·수임자·업무 Scope·시작·종료·사유를 입력한다.
- 순환 위임·중첩 위임·만료 경계를 검증한다.
- 기존 진행 건 적용 여부를 정책으로 구분한다.
- 취소 후 Inbox·권한·Audit를 재확인한다.

### E.3 응답 유실

상신·승인·반려 요청의 응답이 없으면 같은 Button을 반복하지 않는다. Operation ID·Request Hash·Approval Instance 상태를 조회하고, 실제 반영 여부를 확정한 뒤 후속 조치를 수행한다.

## 부록 F. Attachment·Download·Audit

- Attachment Upload는 Size·Type·Checksum·Virus/Malware 정책·Owner Scope를 확인한다.
- Download는 Permission·Data Scope·Reason·Watermark·건수·파일 Hash를 기록한다.
- 대량 Export는 승인·만료·재다운로드 정책을 확인한다.
- Download Audit에는 Operator·대상·Query 조건·Column·Masking·시각·결과를 남긴다.
- Partial File·취소·Timeout·Disk Full 후 Temp Cleanup과 Metadata 대사를 수행한다.

## 부록 G. BZA 운영 검증

- Bootstrap Token 재사용 거부
- 같은 Operation ID 재시도
- 조직 유효기간 겹침
- 겸직·파견·대행과 Data Scope
- Role 만료·권한 회수·기존 Session
- ALL·ANY·N_OF_M 정책
- 위임 순환·만료·취소
- Attachment Oversize·취소·악성 파일
- Download 승인·Masking·Audit
- Backup·Restore 후 User·Role·Approval·Attachment 대사

## 부록 H. 업무 Domain 연계와 확장 경계

BZA 확장은 조직·사용자·Role·Permission·결재 같은 업무 관리 Contract를 통해 수행한다. 업무 Domain Table을 BZA가 직접 갱신하지 않고, Domain Public API·Event·Approved Command를 사용한다. 고객 확장 Field는 Version·Validation·Masking·Data Scope·Migration·Export 영향을 함께 정의하고, BZA 미사용 시스템에 Dependency나 기동 조건을 전파하지 않는다.

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

## 제2부 실무편: BZA 도입·설치·전체 기능 운영·확장

## 16. BZA 사용 여부 결정

BZA는 조직·직원·사용자·Role·Permission·결재·첨부·업무 감사가 필요한 시스템에서 선택한다. 단순 ADM 운영자 관리만 필요하면 BZA를 강제 도입하지 않는다.

선택 질문:

- 조직 계층·직원·다중 발령이 업무 권한에 영향을 주는가.
- 사용자별 다중 Role과 유효기간이 필요한가.
- Menu·Button·API·Data Scope를 업무 관리자 제품에서 관리해야 하는가.
- 결재 정책, 위임·대결, Snapshot 참여자가 필요한가.
- Attachment, Notification, Saved Search, Download Audit가 필요한가.

선택하지 않은 경우 `cpf-biz-admin` Runtime·DB Migration·Frontend를 배포하지 않는다.

## 17. 설치와 최초 관리자

### 17.1 선행 조건

- BZA DB Schema와 Vendor Migration V91 포함.
- Security Starter·Password Service·Session Store.
- BZA Artifact와 Frontend Bundle의 exact SHA 일치.
- Bootstrap Approval Row와 1회용 Token.
- Secret File ACL과 BZA Instance ID.

### 17.2 Bootstrap Property

| Property | 제약 |
|---|---|
| `cpf.bza.bootstrap.approval-token-file` | 설정 시 Bootstrap 시작; Regular/No Symlink/4 KiB 이하 |
| `password-file` | 필수 Secret File |
| `login-id`, `operator-name` | 필수 |
| `role-code` | 기본 `BZA_MANAGER` |
| `approval-scope`, `cpf.environment.code` | Environment Fingerprint 구성 |
| `operation-id` | 미입력 시 Fingerprint+Login; 100자 이하 |
| `claim-lease-seconds` | 기본 300, 30~1,800 |
| `cpf.instance.id`/`CPF_INSTANCE_ID` | Claim Owner; 100자 이하 식별자 |

Legacy Enable과 Plaintext Password Property는 입력 단계에서 거부된다.

### 17.3 실행·대사

```text
Token/Password File ACL 검증
→ Secret Read·Hash
→ Environment Fingerprint·Operation ID
→ 기존 Operation 조회
→ Approval Claim + Lease
→ 사용자·Role Bootstrap
→ Approval Complete
→ Secret Memory Clear·File 삭제
→ Cleanup Ledger
```

응답/Terminal Update가 불명확하면 Operation ID로 기존 Admin을 조회하고 Approval을 Reconcile한다. 같은 Operation에 다른 Login ID가 연결되면 중단한다. Secret 삭제 실패는 업무 성공과 별개로 숨기지 않고 Bootstrap Cleanup 실패로 처리한다.

## 18. BZA Route·기능 전수 Inventory

| Route | URL | Menu Code | 목적 | 입력·검색·Column | 조치 | Permission | 상태 |
|---|---|---|---|---|---|---|---|
| dashboard | / | DASHBOARD | 업무 운영 현황 | 통계·최근 상태 | 새로고침 | 조회 | 기능 제공 |
| organizations | /organizations | ORGANIZATION | 조직 계층 | 조직명·코드 검색, 중지 포함; Tree/상세/고아·순환 경고 | 조회·상세 선택 | Read; 변경 안내는 Write | 기능 제공 |
| employees | /employees | EMPLOYEE | 직원 Profile | 직원번호, 대표조직, 이름, 직급, 직책, 재직상태, Email/Mobile/Office, Clear Flag, Use | 등록·수정·PII Raw | Write, PII_RAW | 기능 제공 |
| positions | /positions | EMPLOYEE | 직급 기준 | Code, Name, Rank Order, Use | 등록·수정 | Write | 기능 제공 |
| jobTitles | /jobTitles | EMPLOYEE | 직책 기준 | Code, Name, Manager YN, Use | 등록·수정 | Write | 기능 제공 |
| assignments | /assignments | EMPLOYEE | 발령·겸직·파견 | Assignment ID, Employee, Organization, Position, Job Title, Type, Primary, From/To | 등록·수정 | Write | 기능 제공 |
| organizationResponsibilities | /organizationResponsibilities | ORGANIZATION | 조직장·대행·승인 Owner | Responsibility ID, Organization, Type, Employee, From/To | 등록·수정 | Write | 기능 제공 |
| users | /users | AUTHORIZATION | BZA 인증 사용자 | Login ID, Name, Password, Account Status, Use, Lock, Force Change, Expected Version, Reason | 등록·수정·Paging | Write | 기능 제공 |
| roles | /roles | AUTHORIZATION | 업무 Role | Role Code/Name, Write Allowed, Data Scope, Use | 등록·수정 | Write | 기능 제공 |
| userRoles | /userRoles | AUTHORIZATION | 사용자 Role 유효기간 | Operation ID, Login ID, Role, Valid From/To, Primary | 등록·수정·Paging | Write | 기능 제공 |
| menus | /menus | AUTHORIZATION | Menu Registry | Code, Parent, Name, Route, Sort, Use, Reason; Tree 검색 | 등록·수정 | Write | 기능 제공 |
| permissions | /permissions | AUTHORIZATION | Menu·Button·API·Data Scope Permission | Permission ID, Role, Menu, Button, Type, HTTP, API Pattern, Domain/Env, Data Scope, Allow/Use | Assignment 등록/수정·실효 권한 Simulation | WRITE, SIMULATE | 기능 제공 |
| permissionTools | /permissionTools | AUTHORIZATION | Role 비교·권한 분석 | 비교 Role/User·Simulation 입력 | 조회·비교 | SIMULATE | 기능 제공 |
| approvalInbox | /approvalInbox | APPROVAL | 결재 처리 | 처리대기/완료/기타 Lane; Decision Reason | APPROVE·AGREE·REJECT | 결재 참여자 | 기능 제공 |
| approvalSubmissions | /approvalSubmissions | APPROVAL | 상신·철회·취소·재상신 | Policy/Version/Domain/Type/Requester/Title/Mode/Due/Payload/Attachment/Key/Reason | 상신·철회·취소·재상신 | 요청자/상신 권한 | 기능 제공 |
| approvalPolicies | /approvalPolicies | APPROVAL | Versioned 결재 정책 | Policy/Version/Name/Domain/Type/From/To/Enabled/Self Approval/Description/Steps JSON/Reason | 저장·조회 | 정책 Write | 기능 제공 |
| approvalSimulation | /approvalSimulation | APPROVAL | 결재 경로 사전 해석 | 조직·Role·위임·정책 Context | Simulation | 조회/Simulation | 기능 제공 |
| approvalDelegations | /approvalDelegations | APPROVAL | 결재 위임·대결 | 위임자/수임자/범위/From/To/Reason | 등록·수정·중지 | Write | 기능 제공 |
| sessions | /sessions | AUTHORIZATION | 본인 Refresh Session | Session 목록·Device/Expiry | 조회·폐기 | 본인/관리 권한 | 기능 제공 |
| audits | /audits | AUDIT | Immutable 업무 감사 | Actor/Action/Target/기간/Operation | 조회·상세 | Audit Read | 기능 제공 |
| notifications | /notifications | SETTING | 업무 알림 | 알림 상태·채널·사용자 Filter | 조회·읽음/설정 | 본인/Setting | 기능 제공 |
| attachments | /attachments | ATTACHMENT | 첨부 업로드·검사·격리 | Group ID, File, Reason; Scan/Data Classification/Quarantine/Retention | Upload·재검사·CLEAN·QUARANTINED | Write | 기능 제공 |
| savedSearches | /savedSearches | SETTING | 저장 검색 | Menu/Name/Condition/Use | 등록·수정·삭제 | 본인/Setting | 기능 제공 |
| settings | /settings | SETTING | BZA 업무 설정 | Key/Value/Type/Scope/Version/Reason | 조회·저장 | Write | 기능 제공 |
| downloads | /downloads | SETTING | Download 정책 | 유형/건수/Data Scope/Masking/Approval/Reason | 조회·정책 변경 | Write | 기능 제공 |
| downloadAudits | /downloadAudits | AUDIT | Download 감사 | 사용자/유형/대상/기간/Reason | 조회·상세 | Audit Read | 기능 제공 |

모든 CRUD 공통 규칙:

- 기본 Paging Size는 20이다.
- 등록/수정 Dialog에는 감사 사유가 필수다.
- 기존 Row의 `versionNo`가 있으면 `expectedVersion`으로 전송한다.
- Operation ID Field가 있고 비어 있으면 UUID를 만든다.
- PII Field는 수정 시 빈 값이면 기존 값을 유지하고, 삭제는 Clear Flag로 명시한다.
- Raw 조회는 별도 `PII_RAW` Permission과 Reason을 사용한다.

## 19. 조직·직원·직급·직책·발령

### 조직

Tree는 Parent Code로 구성하며 고아 조직과 순환을 경고한다. 조직 변경은 Parent 순환, 하위 조직, 유효 사용자·결재 Owner 영향을 Preview한 뒤 수행한다. 조직 삭제 대신 Use N과 유효기간 정책을 사용한다.

### 직원

- 직원번호·대표 조직·이름·재직 상태는 필수다.
- 재직 상태: `EMPLOYED`, `ON_LEAVE`, `SECONDMENT`, `DISPATCHED`, `RETIRED`, `TERMINATED`.
- Email/Mobile/Office는 Masked List와 Raw 조회를 분리한다.
- 빈 수정값은 기존 값을 유지하며 명시 Clear Flag로 삭제한다.

### 직급·직책

직급 Rank Order와 직책 Manager YN이 결재/권한 해석에 영향을 주는지 Consumer를 확인한다. Code를 변경해 과거 발령 참조를 끊지 않는다.

### 발령·책임

Assignment는 Employee, Organization, Type, Primary, 유효기간을 관리한다. 조직 책임은 조직장·대행·승인 Owner를 유효기간으로 관리한다. 동일 시간대 Primary 중복, 위임/대결과 책임 충돌, 퇴직자 유효 책임을 검사한다.

## 20. 사용자·Role·Permission·Data Scope

### 사용자

신규 사용자는 Role 없이 `PENDING_ACTIVATION`으로 생성한다. User Role에서 Role을 부여한 뒤 `ACTIVE`로 바꾼다. Password는 Log/Audit/Payload Snapshot에 저장하지 않는다.

Account Status와 Use/Lock을 구분한다.

- `PENDING_ACTIVATION`: Role/초기 준비 전.
- `ACTIVE`: 로그인 가능.
- `LOCKED`: 인증 실패 등 잠금.
- `SUSPENDED`: 운영 중지.
- `DISABLED`: 사용 종료.

### Role

Role Code/Name, Write Allowed, Data Scope, Use를 관리한다. `writeAllowedYn=Y`만으로 API 권한이 생긴다고 가정하지 않는다.

### User Role

Operation ID, Login ID, Role Code, Valid From/To, Primary YN을 관리한다. 유효기간 중첩과 대표 Role 중복을 검사한다. 응답 유실 시 Operation ID로 기존 이력을 확인한다.

### Menu·Permission

Menu Tree의 Code·Parent·Route·Sort·Use를 Frontend Route Registry와 대조한다. Permission Assignment는 Role, Menu, Button, Type, HTTP Method, API Pattern, Domain/Environment, Data Scope, Allow/Use를 관리한다.

실효 권한 Simulation은 Login ID 기준으로 Menu별 Allow/Deny, API Pattern, Data Scope와 판정 근거를 표시한다. UI 노출만 보지 말고 실제 API 403 Negative Test를 수행한다.

## 21. 결재 정책·상신·Inbox·위임

### 정책

Policy Code+Version을 불변 식별자로 사용한다. 업무 Domain, Approval Type, 유효기간, Enabled, Self Approval, Steps를 관리한다. Step JSON에는 Step No/Type, Target Type/Code, Decision Rule(`ALL`, `ANY`, `N_OF_M` 등 실제 계약), Required, Sort를 명시한다.

### 상신

- 선택 Policy/Version 또는 해석 가능한 Context.
- Business Domain·Approval Type.
- Requester Employee No·Title.
- Sequential/Parallel.
- Due At, Payload JSON, Attachment Group.
- Request Idempotency Key, Reason.

상신 시 참여자와 정책을 Snapshot으로 고정한다. 진행 중 조직/Role/위임 변경이 기존 문서 참여자를 임의로 바꾸지 않는다.

### Inbox

처리 대기 건에서 `APPROVE`, `AGREE`, `REJECT`를 선택하고 구체적 Reason을 입력한다. 같은 문서를 두 Browser에서 처리하면 Version/현재 Step을 재조회한다.

### Lifecycle

진행 중 문서는 Requester 정책에 따라 WITHDRAW/CANCEL한다. REJECTED/WITHDRAWN/CANCELED/EXPIRED는 새 Idempotency Key와 Snapshot으로 재상신한다. 과거 문서를 상태만 되돌려 재사용하지 않는다.

### 위임·대결

Delegator, Delegate, Scope, Effective From/To, Reason을 관리한다. 자기 위임, 순환, 기간 중첩, 권한 확대, 종료된 직원, 결재 Snapshot 적용 여부를 검증한다.

## 22. Attachment

```text
Upload + Group ID + Reason
→ Temporary/Stored
→ Scan Pending
→ CLEAN 또는 QUARANTINED
→ 업무 연결
→ Retention/Expire
```

화면은 Original Filename, Scan Status, Data Classification, Quarantine, Retention을 표시한다. 재검사/CLEAN/격리 조치마다 Reason을 남긴다. CLEAN 전 다운로드·업무 연결을 허용하지 않는다. File Size, MIME, Extension, Checksum, Virus Scanner 오류와 응답 유실을 구분한다.

## 23. Session·Notification·Saved Search·Setting

### Session

Refresh Session별 Device/발급/만료/폐기 상태를 확인한다. Password Reset, User Disable, Security Incident 시 관련 Session을 폐기한다. BFF Credential Vault와 Session DB Migration을 함께 확인한다.

### Notification

업무 알림은 Delivery/읽음 상태와 수신자 Data Scope를 적용한다. Provider 발송과 UI Notification을 같은 성공 상태로 합치지 않는다.

### Saved Search

Menu/사용자/검색 조건 JSON/Use를 관리한다. 저장된 조건을 API Parameter Allowlist에 다시 검증하고 다른 사용자의 Data Scope를 포함하지 않는다.

### Setting

Key·Value·Type·Scope·Version·Reason을 관리한다. Secret 원문은 Setting에 저장하지 않는다. 변경 뒤 Consumer Cache/재기동과 Rollback 값을 확인한다.

## 24. Audit·Download

Audit는 Actor, Action, Target, Operation, Before/After, Reason, Approval, Result를 불변 이력으로 조회한다. 업무 Table 직접 UPDATE로 Audit와 상태를 분리하지 않는다.

Download 정책은 유형, 건수 상한, Masking, Data Scope, Approval/Reason을 관리한다. Download Audit에서 사용자, 유형, 대상, 건수, Filter, Reason, 결과를 확인한다. CSV/Excel Formula Injection과 PII 원문을 방지한다.

## 25. 업무 Domain 연계

Domain은 BZA DB를 직접 Join하지 않고 BZA Public Query/Authorization/Approval Contract를 사용한다.

- 사용자/Role: Login ID와 실효 Permission/Data Scope.
- 조직/직원: Employee No, Organization Code, Effective Time.
- 결재: Approval ID/Status/Snapshot Hash.
- Attachment: Attachment Group/ID와 Security Status.
- Audit: Domain Operation ID와 BZA Actor Context.

Same-JVM·Remote Adapter는 같은 결과·오류·Timeout·Masking을 제공해야 한다.

## 26. Backup·Restore·Upgrade·Rollback

- BZA Schema와 Session/Credential Vault, Bootstrap Approval/Cleanup, Organization/User/Permission/Approval/Audit/Attachment Metadata를 Backup한다.
- File Binary Store는 DB Metadata와 Checksum을 함께 복구한다.
- Restore 뒤 Login, Permission Simulation, 결재 Snapshot, Attachment Download, Audit Hash를 검증한다.
- V91 Bootstrap Claim Recovery Upgrade를 3 Vendor에서 실행하고 기존 Approval Row Backfill/Index를 확인한다.
- Rollback 시 새 Credential/Session/Bootstrap Column과 Application Version 호환을 확인한다.

## 27. BZA 장애 Runbook

### Login 불가

User Status/Use/Lock/Password Change, Role 유효기간, Session Store, BFF Credential Vault, FilterChain 401/403를 확인한다.

### 권한 오류

User Role 유효기간, Menu/Button/API Permission, Data Scope, Cache, Simulation 결과, Backend/Owner 403를 비교한다.

### 결재 정지

Policy Version, Snapshot Participant, Current Step, Delegation, Due/Expired, Idempotency Key, Audit를 확인한다. Row 직접 상태 변경 금지.

### Bootstrap 불명

Operation ID로 Admin 생성 여부를 조회하고 Approval Claim/Lease/Cleanup 상태를 Reconcile한다. 새 Token으로 중복 Bootstrap하지 않는다.

### Attachment 정지

Scanner/Storage/Checksum/Quarantine/Retention을 확인하고 CLEAN을 추정으로 설정하지 않는다.

## 28. BZA EDU

- EDU-BZA-01 Bootstrap→Role→ACTIVE→Login.
- EDU-BZA-02 조직·직원·발령·책임과 기간 충돌.
- EDU-BZA-03 Role/Menu/Button/API/Data Scope와 Allow/Deny Simulation.
- EDU-BZA-04 결재 Policy→상신→승인/반려→위임→재상신.
- EDU-BZA-05 Attachment Upload→Quarantine→Recheck→CLEAN→Download Audit.
- EDU-BZA-06 Session 폐기·Password 변경·Audit 확인.

## 29. BZA 완료 Checklist

- [ ] 설치/Bootstrap/Secret Cleanup
- [ ] 28개 Route 접근·Field·Button·Permission
- [ ] 조직·직원·발령·책임 유효기간
- [ ] 사용자 Status·Role·Permission·Data Scope
- [ ] 결재 Policy·Snapshot·Inbox·Lifecycle·Delegation
- [ ] Attachment Scan·Quarantine·Retention
- [ ] Session·Credential Vault·Notification·Saved Search
- [ ] Masking·PII Raw·Audit·Download
- [ ] Domain 연계 Contract
- [ ] Backup/Restore/Upgrade/Rollback
- [ ] Browser·3DB·Concurrency·Fault Evidence

---
## 부록 I. BZA Source·Route 진입점

| 기능 | 기준 Source |
|---|---|
| 전체 Route | `cpf-biz-admin/frontend/src/app/routes.ts` |
| Router/404 | `cpf-biz-admin/frontend/src/app/router.ts` |
| 공통 CRUD | `cpf-biz-admin/frontend/src/components/CrudTable.vue` |
| 조직 | `cpf-biz-admin/frontend/src/features/organizations/OrganizationsPage.vue` |
| 직원 | `cpf-biz-admin/frontend/src/features/employees/EmployeesPage.vue` |
| 직급 | `cpf-biz-admin/frontend/src/features/positions/PositionsPage.vue` |
| 직책 | `cpf-biz-admin/frontend/src/features/job-titles/JobTitlesPage.vue` |
| 발령 | `cpf-biz-admin/frontend/src/features/assignments/AssignmentsPage.vue` |
| 조직 책임 | `cpf-biz-admin/frontend/src/features/organization-responsibilities/OrganizationResponsibilitiesPage.vue` |
| 사용자 | `cpf-biz-admin/frontend/src/features/users/UsersPage.vue` |
| Role | `cpf-biz-admin/frontend/src/features/roles/RolesPage.vue` |
| User Role | `cpf-biz-admin/frontend/src/features/user-roles/UserRolesPage.vue` |
| Menu | `cpf-biz-admin/frontend/src/features/authorization/MenusPage.vue` |
| Permission | `cpf-biz-admin/frontend/src/features/authorization/PermissionsPage.vue` |
| 결재 정책 | `cpf-biz-admin/frontend/src/features/approval-policies/ApprovalPoliciesPage.vue` |
| 결재 상신 | `cpf-biz-admin/frontend/src/features/approval-submissions/ApprovalSubmissionsPage.vue` |
| 결재 Inbox | `cpf-biz-admin/frontend/src/features/approval-inbox/ApprovalInboxPage.vue` |
| Attachment | `cpf-biz-admin/frontend/src/features/attachments/AttachmentsPage.vue` |
| Bootstrap | `cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/service/BzaBootstrapRunner.java` |
| Bootstrap Approval | `cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/service/BzaBootstrapApprovalRepository.java` |
| DB Migration | `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/migration/**/V85__bza_bootstrap_approval.sql`, `V91__bza_bootstrap_claim_recovery.sql` |

---

## 제3부. BZA 26개 화면별 상세 사용 절차

## 30. BZA 기능 카드 사용법

조직·직원·사용자·Role·결재는 서로 영향을 주므로 화면 하나의 저장 결과만 확인하지 않는다. 변경 후 연관 Projection, Permission Evaluation, Approval Simulation, Audit를 순서대로 확인한다.



## 31. 신규 조직·직원·사용자 Onboarding 전체 절차

1. Organization Code·Name·Parent·유효기간을 등록한다.
2. Position·Job Title 기준정보를 등록한다.
3. Employee Profile을 Masking 정책과 함께 등록한다.
4. Assignment로 주 조직·겸직·파견·대행과 기간을 지정한다.
5. Organization Responsibility로 조직장·승인 Owner를 지정한다.
6. User를 Employee와 연결하고 인증 상태를 설정한다.
7. Role과 User Role 유효기간을 등록한다.
8. Menu·Action·API·Data Scope Permission을 확인한다.
9. Approval Simulation으로 조직·Role·위임 경로를 확인한다.
10. 로그인·메뉴·업무 조회·상신·첨부·Audit를 확인한다.

## 32. 퇴직·이동·권한 회수 절차

1. 진행 중 결재와 위임·대결을 조회한다.
2. 업무 Owner와 조직 책임을 새 담당자에게 이관한다.
3. User Role 종료일과 Assignment 종료일을 적용한다.
4. Session을 폐기한다.
5. Download·Raw Data·Break-glass 권한을 회수한다.
6. 미처리 결재 Inbox를 재배정하거나 정책에 따라 처리한다.
7. 변경 전후 Permission Simulation과 Audit를 확인한다.

## 33. 결재 정책 변경 절차

1. 기존 Policy Version과 사용 중인 Approval Instance를 조회한다.
2. ALL·ANY·N_OF_M, 참여자 해석, 조직·Role·책임·위임 규칙을 작성한다.
3. Simulation으로 대표 조직·직원·대행·위임 Scenario를 실행한다.
4. 자기승인·순환·빈 참여자·만료·중복 위임을 검사한다.
5. 새 Version을 게시하고 적용 시작 시각을 기록한다.
6. 기존 상신은 기존 Snapshot을 유지하는지 확인한다.
7. 실제 상신·Inbox·승인·반려·회수·만료를 시험한다.

## 34. BZA 업무 Domain 연계 계약

업무 Domain은 BZA DB를 직접 조회하지 않고 조직·사용자·Permission·Approval Public API를 사용한다. 응답에는 Version·기준 시각·Data Scope·Masking 정보를 포함하고, Remote Timeout 시 Operation 또는 Approval ID로 상태를 조회한다.

---

## 제4부. BZA 화면별 업무 완결 절차

> 아래 26개 기능 카드는 화면 진입부터 입력, 정상 결과, 오류, 복구, 감사 확인까지 한 단위로 설명한다.

### dashboard — 업무 운영 현황

**Route**: `/`
**Menu Code**: `DASHBOARD`
**Frontend**: `cpf-biz-admin/frontend/src/features/dashboard/DashboardPage.vue`

#### 기능 목적

업무 운영 현황 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `조회` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

통계·최근 상태

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **새로고침**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### organizations — 조직 계층

**Route**: `/organizations`
**Menu Code**: `ORGANIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/organizations/OrganizationsPage.vue`

#### 기능 목적

조직 계층 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Read; 변경 안내는 Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

조직명·코드 검색, 중지 포함; Tree/상세/고아·순환 경고

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·상세 선택**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### employees — 직원 Profile

**Route**: `/employees`
**Menu Code**: `EMPLOYEE`
**Frontend**: `cpf-biz-admin/frontend/src/features/employees/EmployeesPage.vue`

#### 기능 목적

직원 Profile 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write, PII_RAW` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

직원번호, 대표조직, 이름, 직급, 직책, 재직상태, Email/Mobile/Office, Clear Flag, Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정·PII Raw**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### positions — 직급 기준

**Route**: `/positions`
**Menu Code**: `EMPLOYEE`
**Frontend**: `cpf-biz-admin/frontend/src/features/positions/PositionsPage.vue`

#### 기능 목적

직급 기준 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Code, Name, Rank Order, Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### jobTitles — 직책 기준

**Route**: `/jobTitles`
**Menu Code**: `EMPLOYEE`
**Frontend**: `cpf-biz-admin/frontend/src/features/job-titles/JobTitlesPage.vue`

#### 기능 목적

직책 기준 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Code, Name, Manager YN, Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### assignments — 발령·겸직·파견

**Route**: `/assignments`
**Menu Code**: `EMPLOYEE`
**Frontend**: `cpf-biz-admin/frontend/src/features/assignments/AssignmentsPage.vue`

#### 기능 목적

발령·겸직·파견 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Assignment ID, Employee, Organization, Position, Job Title, Type, Primary, From/To

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### organizationResponsibilities — 조직장·대행·승인 Owner

**Route**: `/organizationResponsibilities`
**Menu Code**: `ORGANIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/organization-responsibilities/OrganizationResponsibilitiesPage.vue`

#### 기능 목적

조직장·대행·승인 Owner 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Responsibility ID, Organization, Type, Employee, From/To

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### users — BZA 인증 사용자

**Route**: `/users`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/users/UsersPage.vue`

#### 기능 목적

BZA 인증 사용자 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Login ID, Name, Password, Account Status, Use, Lock, Force Change, Expected Version, Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정·Paging**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### roles — 업무 Role

**Route**: `/roles`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/roles/RolesPage.vue`

#### 기능 목적

업무 Role 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Role Code/Name, Write Allowed, Data Scope, Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### userRoles — 사용자 Role 유효기간

**Route**: `/userRoles`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/user-roles/UserRolesPage.vue`

#### 기능 목적

사용자 Role 유효기간 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Operation ID, Login ID, Role, Valid From/To, Primary

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정·Paging**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### menus — Menu Registry

**Route**: `/menus`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/authorization/MenusPage.vue`

#### 기능 목적

Menu Registry 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Code, Parent, Name, Route, Sort, Use, Reason; Tree 검색

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### permissions — Menu·Button·API·Data Scope Permission

**Route**: `/permissions`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/authorization/PermissionsPage.vue`

#### 기능 목적

Menu·Button·API·Data Scope Permission 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `WRITE, SIMULATE` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Permission ID, Role, Menu, Button, Type, HTTP, API Pattern, Domain/Env, Data Scope, Allow/Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **Assignment 등록/수정·실효 권한 Simulation**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### permissionTools — Role 비교·권한 분석

**Route**: `/permissionTools`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/permission-tools/PermissionToolsPage.vue`

#### 기능 목적

Role 비교·권한 분석 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `SIMULATE` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

비교 Role/User·Simulation 입력

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·비교**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### approvalInbox — 결재 처리

**Route**: `/approvalInbox`
**Menu Code**: `APPROVAL`
**Frontend**: `cpf-biz-admin/frontend/src/features/approval-inbox/ApprovalInboxPage.vue`

#### 기능 목적

결재 처리 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `결재 참여자` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

처리대기/완료/기타 Lane; Decision Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **APPROVE·AGREE·REJECT**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### approvalSubmissions — 상신·철회·취소·재상신

**Route**: `/approvalSubmissions`
**Menu Code**: `APPROVAL`
**Frontend**: `cpf-biz-admin/frontend/src/features/approval-submissions/ApprovalSubmissionsPage.vue`

#### 기능 목적

상신·철회·취소·재상신 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `요청자/상신 권한` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Policy/Version/Domain/Type/Requester/Title/Mode/Due/Payload/Attachment/Key/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **상신·철회·취소·재상신**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### approvalPolicies — Versioned 결재 정책

**Route**: `/approvalPolicies`
**Menu Code**: `APPROVAL`
**Frontend**: `cpf-biz-admin/frontend/src/features/approval-policies/ApprovalPoliciesPage.vue`

#### 기능 목적

Versioned 결재 정책 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `정책 Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Policy/Version/Name/Domain/Type/From/To/Enabled/Self Approval/Description/Steps JSON/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **저장·조회**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### approvalSimulation — 결재 경로 사전 해석

**Route**: `/approvalSimulation`
**Menu Code**: `APPROVAL`
**Frontend**: `cpf-biz-admin/frontend/src/features/approval-simulation/ApprovalSimulationPage.vue`

#### 기능 목적

결재 경로 사전 해석 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `조회/Simulation` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

조직·Role·위임·정책 Context

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **Simulation**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### approvalDelegations — 결재 위임·대결

**Route**: `/approvalDelegations`
**Menu Code**: `APPROVAL`
**Frontend**: `cpf-biz-admin/frontend/src/features/approval-delegations/ApprovalDelegationsPage.vue`

#### 기능 목적

결재 위임·대결 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

위임자/수임자/범위/From/To/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정·중지**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### sessions — 본인 Refresh Session

**Route**: `/sessions`
**Menu Code**: `AUTHORIZATION`
**Frontend**: `cpf-biz-admin/frontend/src/features/sessions/SessionsPage.vue`

#### 기능 목적

본인 Refresh Session 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `본인/관리 권한` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Session 목록·Device/Expiry

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·폐기**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### audits — Immutable 업무 감사

**Route**: `/audits`
**Menu Code**: `AUDIT`
**Frontend**: `cpf-biz-admin/frontend/src/features/audits/AuditsPage.vue`

#### 기능 목적

Immutable 업무 감사 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Audit Read` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Actor/Action/Target/기간/Operation

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·상세**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### notifications — 업무 알림

**Route**: `/notifications`
**Menu Code**: `SETTING`
**Frontend**: `cpf-biz-admin/frontend/src/features/notifications/NotificationsPage.vue`

#### 기능 목적

업무 알림 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `본인/Setting` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

알림 상태·채널·사용자 Filter

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·읽음/설정**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### attachments — 첨부 업로드·검사·격리

**Route**: `/attachments`
**Menu Code**: `ATTACHMENT`
**Frontend**: `cpf-biz-admin/frontend/src/features/attachments/AttachmentsPage.vue`

#### 기능 목적

첨부 업로드·검사·격리 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Group ID, File, Reason; Scan/Data Classification/Quarantine/Retention

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **Upload·재검사·CLEAN·QUARANTINED**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### savedSearches — 저장 검색

**Route**: `/savedSearches`
**Menu Code**: `SETTING`
**Frontend**: `cpf-biz-admin/frontend/src/features/saved-searches/SavedSearchesPage.vue`

#### 기능 목적

저장 검색 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `본인/Setting` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Menu/Name/Condition/Use

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **등록·수정·삭제**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### settings — BZA 업무 설정

**Route**: `/settings`
**Menu Code**: `SETTING`
**Frontend**: `cpf-biz-admin/frontend/src/features/settings/SettingsPage.vue`

#### 기능 목적

BZA 업무 설정 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

Key/Value/Type/Scope/Version/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·저장**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### downloads — Download 정책

**Route**: `/downloads`
**Menu Code**: `SETTING`
**Frontend**: `cpf-biz-admin/frontend/src/features/downloads/DownloadsPage.vue`

#### 기능 목적

Download 정책 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Write` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

유형/건수/Data Scope/Masking/Approval/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·정책 변경**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.
### downloadAudits — Download 감사

**Route**: `/downloadAudits`
**Menu Code**: `AUDIT`
**Frontend**: `cpf-biz-admin/frontend/src/features/download-audits/DownloadAuditsPage.vue`

#### 기능 목적

Download 감사 기능을 조회·등록·변경·감사한다. 이 기능의 변경은 조직 기준일, 사용자·Role 유효기간, Data Scope, 결재 Snapshot, 업무 Domain Consumer에 영향을 줄 수 있으므로 단일 화면 저장 결과만으로 종료하지 않는다.

#### 선행 조건

1. 로그인 사용자에게 `Audit Read` 권한이 있는지 확인한다.
2. 상위 기준정보와 대상의 현재 Version·유효기간·Active 상태를 조회한다.
3. 개인정보·첨부·Download 기능은 Raw·Export 권한과 Reason을 별도로 확인한다.
4. 변경 중인 Operation 또는 Approval이 있으면 기존 ID로 결과를 확인한다.

#### 검색·입력·표시 값

사용자/유형/대상/기간/Reason

입력 Field마다 필수 여부, 공백·Null 의미, 최대 길이, 날짜 포함 경계, Code 불변성, Expected Version을 확인한다. 조직·Role·Assignment·Delegation은 기간 중복과 순환을 검사한다.

#### 조치

화면 조치: **조회·상세**

1. 조회 조건을 제한하고 대상을 선택한다.
2. Detail에서 ID·Version·유효기간·최근 변경자를 확인한다.
3. 변경 영향 Preview 또는 Simulation을 실행한다.
4. Reason과 Expected Version을 입력한다.
5. 등록·수정·결정 요청 후 Operation ID·Approval ID를 기록한다.
6. 업무 상태와 Audit가 함께 반영됐는지 확인한다.

#### 정상 결과

- 대상 Version이 증가하고 입력한 유효기간·상태가 반영된다.
- 조직 Tree, User Role, Permission Evaluation, Approval Simulation 등 연관 Projection이 같은 결과를 반환한다.
- Masking 정책과 Data Scope가 로그인 사용자에게 적용된다.
- Audit에 Actor, Action, Target, Reason, Before/After, Version, 결과가 남는다.

#### 오류·동시성·응답 유실

| 상황 | 처리 |
|---|---|
| Validation | Field 오류와 정책을 수정해 새 요청을 만든다. |
| 403 | Menu·Action·API·Data Scope를 확인한다. |
| 409 | 최신 Version·유효기간·변경자를 조회하고 다시 작성한다. |
| 중복 기간 | 겹치는 Assignment·Role·Delegation·Policy를 종료하거나 기간을 조정한다. |
| 응답 유실 | 같은 Operation ID·Idempotency Key로 결과를 조회한다. |
| 부분 반영 | 연관 Projection과 Consumer를 Reconcile하고 Failed Target만 재처리한다. |

#### Rollback·복구

변경 전 Version과 유효기간을 기준으로 새 보정 변경을 수행한다. 물리 삭제 대신 비활성화·종료일 적용이 계약인 기능은 해당 방법을 사용한다. 권한·결재 변경은 현재 Session과 진행 중 Snapshot에 미치는 영향을 확인한다.

#### 운영 확인

Audit, Session, Permission Simulation, Approval Simulation, 업무 Domain 조회를 연결하고 교대 기록에 대상 ID·Version·Operation·미확정 항목을 남긴다.


## 35. BZA 일일 운영

1. Dashboard에서 사용자·결재·Attachment·Notification 상태를 확인한다.
2. 조직·직원·Assignment의 기준일과 고아·순환·중복을 확인한다.
3. User·Role·Permission의 만료·잠금·Data Scope를 확인한다.
4. Approval Inbox·만료·위임·대결 충돌을 확인한다.
5. Quarantine Attachment, 실패 Notification, 만료 Download를 확인한다.
6. Raw·Export·Permission·Approval 변경 Audit를 검토한다.

## 36. BZA 월간 점검

- 퇴직·휴직·파견·겸직 상태와 User·Role·Session을 대조한다.
- 조직 책임과 결재 Owner·위임 기간을 대조한다.
- Role 비교와 Permission Simulation으로 과다 권한을 검토한다.
- Attachment Retention·Quarantine·Download Audit를 검토한다.
- 업무 Domain Consumer와 BZA 기준정보 Version을 대조한다.
