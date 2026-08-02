# CPF BZA 매뉴얼 — 조직·사용자·권한·결재·감사 운영

> **주 독자**: 조직 관리자, 인사 연계 담당자, 사용자 관리자, 권한 관리자, 결재 정책 담당자, 보안·감사 담당자, BZA 연동 개발자
> **완료 결과**: CPF BZA를 선택·설치하고 조직·직원·사용자·Role·Permission·Data Scope·결재·위임·대결·Session·Masking·Audit를 업무 시스템에 연결하여 운영한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
- Owner Module: `cpf-biz-admin`
- 최상위 요구 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 활성 개발 요구: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- 실제 Backend·Frontend·SQL·Permission Manifest·Test가 문서보다 우선한다.
- 기준 Commit에서 Browser·다중 인스턴스·장애·Backup/Restore를 직접 실행하지 않았으므로 해당 결과는 `미검증`이다.

## 1. BZA 도입 판단

### 1.1 선택 조건

- 여러 업무 시스템이 조직·직원·사용자 정보를 공통으로 사용한다.
- Role·Permission·Data Scope를 공통 정본으로 운영해야 한다.
- 결재선·위임·대결·유효기간·감사 정책을 여러 업무에서 재사용한다.
- Session·Masking·Export 통제를 공통 제품에서 관리해야 한다.

### 1.2 비선택 조건

- 단일 업무가 자체 사용자·권한을 소유하고 공통 정본이 필요하지 않다.
- BZA가 업무 엔터티·업무 상태·업무 승인 결과 원장을 직접 소유하게 된다.
- 조직·권한 정본 Owner가 확정되지 않았다.

## 2. Ownership과 업무 Domain 경계

### BZA가 소유하는 것

```text
조직·조직 계층·유효기간
직원·사용자 연결과 사용자 상태
Role·Permission Catalog
Role Assignment·Data Scope
결재 정책·결재선·승인 이력
위임·대결·유효기간
Session·Masking Policy·Export Audit
BZA 자체 운영 Audit
```

### 업무 Domain이 소유하는 것

```text
업무 엔터티·업무 상태·업무 원장
업무 금액·건수·위험 계산
업무 요청·취소·보상·대사
업무 결과의 최종 판정
업무별 승인 대상 Hash
```

업무 Domain은 BZA의 사용자·권한·승인 결과를 참조하지만 자신의 Transaction과 Audit에 필요한 식별자를 저장한다.

## 3. 설치 전 준비

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
if(-not(Test-Path -LiteralPath (Join-Path $repo 'cpf-biz-admin'))){throw 'cpf-biz-admin 모듈이 없습니다.'}
git -C $repo rev-parse HEAD
git -C $repo status --short
& (Join-Path $repo 'gradlew.bat') :cpf-biz-admin:tasks --all
```

준비 항목:

1. Artifact·Commit·SHA-256을 확인한다.
2. 대상 DB Vendor와 Migration Pack을 확인한다.
3. 초기 관리자 생성 방식과 Secret 전달 경로를 정한다.
4. 조직·직원·사용자 정본 Owner와 동기화 주기를 정한다.
5. Role·Permission Catalog Owner를 지정한다.
6. 운영자·승인자·보안 관리자·감사 담당자를 분리한다.
7. Backup·Restore·Rollback 기준을 준비한다.

## 4. 초기 관리자와 최초 기동

### 4.1 초기 관리자 원칙

- 초기 Password·Token 원문을 Git·문서·명령 이력에 남기지 않는다.
- 최초 로그인 후 Password 변경 또는 Credential Rotation을 요구한다.
- 초기 관리자는 운영·승인·보안·감사 역할을 장기간 함께 보유하지 않는다.
- 초기 설정 완료 후 Bootstrap 권한을 축소하거나 비활성화한다.

### 4.2 최초 기동 완료 기준

- DB Migration 성공
- 초기 관리자 인증 성공
- Session 생성·만료 확인
- 조직 Root 등록 가능
- Permission Catalog 조회 가능
- Audit 기록 생성
- Health와 실제 기능 Probe 성공

## 5. 조직 모델

### 5.1 필수 속성

```text
organizationId / organizationCode / name
parentOrganizationId
validFrom / validTo
status / version
owner / reason / audit
```

### 5.2 검증 규칙

- Root는 승인된 개수만 허용한다.
- Parent는 같은 유효 시점에 존재해야 한다.
- 자기 자신과 순환 관계를 금지한다.
- Parent 종료일 이후 Child가 활성 상태로 남는 경우를 검사한다.
- 조직 이동은 과거 결재·Audit 해석을 위해 시점 정보를 보존한다.

### 5.3 상태 변화

```text
DRAFT → ACTIVE → INACTIVE → CLOSED
```

실제 Source 상태값이 다르면 Source를 따른다. 종료 조직의 사용자·Role·위임·결재 대기 건을 함께 확인한다.

## 6. 직원과 사용자

직원과 로그인 사용자를 분리한다.

| 개념 | 의미 |
|---|---|
| 직원 | 조직·재직 상태·인사 식별자 |
| 사용자 | 로그인 ID·인증 주체·Session 상태 |
| 연결 | 직원과 사용자 간 유효기간 관계 |

운영 절차:

1. 직원 정본을 등록·동기화한다.
2. 사용자 ID 중복과 인증 Provider 연결을 확인한다.
3. 직원·사용자 연결 유효기간을 설정한다.
4. 휴직·퇴직·이동 시 Session·Role·위임·대기 결재를 확인한다.
5. 상태 변경 결과와 Audit를 확인한다.

## 7. Role·Permission Catalog

### 7.1 개념

| 개념 | 예 |
|---|---|
| Role | 지급 운영자, 승인자, 보안 관리자 |
| Permission | 지급 조회, 재처리 요청, 원문 조회, Export |
| Assignment | 사용자·조직에 Role 부여 |
| Effective Permission | Role·직접 부여·기간·정책을 반영한 결과 |

### 7.2 Permission 분리

- 메뉴 접근
- 목록 조회
- 상세 조회
- 변경·조치
- 승인·반려
- 원문 조회
- Export·Download
- 권한 Simulation
- 보안 정책 변경

화면 Button 숨김만으로 권한을 통제하지 않는다. Backend Filter·Controller·Owner Service에서 같은 Permission을 검증한다.

## 8. Data Scope

Data Scope 예:

```text
SELF
OWN_ORGANIZATION
DESCENDANT_ORGANIZATIONS
SPECIFIED_ORGANIZATIONS
ALL_AUTHORIZED
```

실제 값은 Source Manifest를 따른다.

평가 순서:

1. Actor와 유효 Session을 확인한다.
2. Role·Permission의 유효기간을 확인한다.
3. 조직 시점과 사용자 소속을 확인한다.
4. Data Scope를 업무 대상 조직에 적용한다.
5. 업무 Owner Service가 최종 범위를 검증한다.
6. 결과와 Scope Snapshot을 Audit에 기록한다.

## 9. Masking

Masking은 목록·상세·Export·Log에 각각 적용한다.

| 영역 | 기본 원칙 |
|---|---|
| 목록 | 업무 수행에 필요한 최소 필드만 표시 |
| 상세 | Permission·Data Scope·Reason 검사 |
| 원문 조회 | 별도 Permission·Reason·필요 시 Approval |
| Export | 별도 Permission·범위·Watermark·Audit |
| Log·Trace | 개인정보·Token·Secret 원문 금지 |

Masking된 값을 업무 처리 입력으로 재사용하지 않는다.

## 10. 결재 정책

### 10.1 정책 구성

```text
policyId / policyVersion
businessType / action
amount·count·risk condition
approval line / quorum
requester-approver separation
validity / expiry
reject / cancel / recall
substitution / delegation
reason / evidence
```

### 10.2 승인 대상 고정

Approval 요청에는 다음을 저장한다.

- 업무 식별자
- Action
- 대상 Snapshot 또는 Hash
- Policy ID·Version
- 요청자·조직·Data Scope
- 요청 Reason
- 만료 시각

대상 Hash나 Policy Version이 달라지면 기존 승인을 재사용하지 않는다.

### 10.3 상태

```text
REQUESTED
WAITING_APPROVAL
APPROVED
REJECTED
CANCELLED
EXPIRED
EXECUTED
UNKNOWN_RESULT
```

실제 Source 상태값과 전이를 전수 대조한다.

## 11. 결재 요청·승인·반려 절차

### 요청자

1. 대상 업무 상태와 Version을 확인한다.
2. Action·Reason·대상 Hash를 입력한다.
3. 적용 정책·결재선·만료 시각을 확인한다.
4. 요청 후 Approval ID를 기록한다.

### 승인자

1. 자신의 Permission·Data Scope·대결 여부를 확인한다.
2. 대상 Snapshot과 현재 업무 상태를 비교한다.
3. 정책 Version·요청 Reason·이전 승인 이력을 확인한다.
4. 승인 또는 반려 Reason을 입력한다.
5. 결과와 Audit를 확인한다.

### 실행자

승인 성공만으로 업무 결과를 성공으로 표시하지 않는다. 업무 Owner Service에 Command를 보내고 Operation 결과를 대사한다.

## 12. 위임과 대결

| 구분 | 의미 |
|---|---|
| 위임 | 권한 또는 업무 처리 권한을 기간·대상 범위로 이전 |
| 대결 | 결재자를 대신해 승인·반려 수행 |

필수 통제:

- 시작·종료 시각
- 대상 Role·Permission·업무 유형
- 원 권한자의 범위 초과 금지
- 순환 위임·다단계 우회 차단
- 원 권한자·대행자 동시 Audit
- 휴직·퇴직·조직 이동 시 재평가
- 만료 후 Session·Cache 반영 확인

## 13. Attachment

- 허용 확장자·MIME·Size
- Checksum
- 악성코드 검사·격리
- 접근 Permission·Data Scope
- 보존기간·법적 보류
- Download Audit
- 삭제·복원 정책

Attachment 저장소가 BZA 외부 Provider이면 Provider Receipt와 업무 Reference를 함께 저장한다.

## 14. Notification

알림은 결재·권한 변경·위임 만료·보안 사건의 전달 수단이며 업무 결과 정본이 아니다.

필수 항목:

```text
channel / template / locale
recipient / preference / quiet hours
attempt / timeout / retry
provider receipt / failure class
idempotency / audit
```

기준 Commit에서 Notification Provider 전체 Starter는 미구현 또는 재확인 필요 상태다. Provider가 없으면 알림 성공을 문서만으로 가정하지 않는다.

## 15. Session

운영 항목:

- 로그인·로그아웃
- Session ID Rotation
- Idle·Absolute Timeout
- 동시 Session 정책
- 잠금·휴면·퇴직 사용자 차단
- Password 변경·Credential Rotation 후 Session 처리
- 강제 종료
- CSRF·Cookie·SameSite·Secure 속성
- Session Audit

Frontend 로그인 성공과 Backend Permission 성공을 분리해 시험한다.

## 16. Audit

최소 필드:

```text
actorId / employeeId / organizationId
actingFor / delegationId
role / permission / dataScope
reason / approvalId / policyVersion
action / target / before / after
requestId / traceId / operationId
result / failureClass / timestamp
```

Audit에는 Secret·Token·Password·불필요한 개인정보 원문을 남기지 않는다.

## 17. Export

Export는 조회의 단순 확장이 아니다.

1. Export Permission을 확인한다.
2. Data Scope·Masking·필드 목록을 확정한다.
3. Reason과 필요 시 Approval을 받는다.
4. 비동기 작업 ID를 생성한다.
5. 파일 Checksum·보존시간·Download 횟수를 기록한다.
6. Download 시 다시 Permission·만료를 확인한다.
7. Export·Download·삭제 Audit를 남긴다.

응답 유실 후 같은 Export를 중복 생성하지 않고 Operation을 조회한다.

## 18. 업무 Domain 연계

### 18.1 인증·권한

업무 Service는 BZA가 전달한 Header만 신뢰하지 않고 검증된 Security Context와 Permission·Data Scope를 사용한다.

### 18.2 승인

업무 Command에는 Approval ID·Policy Version·대상 Hash·Expected Version을 포함한다. 업무 Owner가 현재 상태와 승인 대상을 다시 확인한다.

### 18.3 Timeout·결과 불명

Approval 요청 또는 승인 후 응답을 받지 못하면 Approval ID로 조회한다. 새 승인 요청을 만들지 않는다. 업무 실행 응답이 유실되면 Owner Operation과 업무 원장을 대사한다.

## 19. 확장 방법

조직 속성·Permission·결재 유형을 추가할 때:

1. Owner와 Consumer를 확인한다.
2. 기존 Public API·DB·Frontend 영향 범위를 작성한다.
3. 새로운 Permission과 Data Scope가 필요한지 결정한다.
4. 3 Vendor Migration과 Rollback/Forward Recovery를 준비한다.
5. Generated Client와 Frontend Form·Table을 갱신한다.
6. 기존 Role·Policy와 하위 호환을 검증한다.
7. Audit·Export·Backup 범위를 갱신한다.
8. EDU와 Browser Test를 추가한다.

업무 전용 필드를 BZA 공통 테이블에 임의로 추가하지 않는다.

## 20. 실제 BZA Menu 26개

아래 26개 Menu 식별자는 기준 Commit의 `cpf-biz-admin/frontend/src/generated/bza-route-operation-contract.ts` Key와 대조한 정적 화면 기준이다. Browser Runtime을 실행하지 않았으므로 실제 Route Path, Component Rendering, Backend Permission 문자열과 Button 활성 조건은 `미검증`이다. 배포 전에는 Generated Client, Frontend Router와 Backend Permission Manifest를 전수 대조한다.

| 구분 | Menu 식별자·업무 | 역할·권한 범주 | 검색·기본값 | 주요 Column·상세 | Button·활성 조건 | 완료 판정 |
|---|---|---|---|---|---|---|
| 조직·인사 | `dashboard`<br>**운영 요약** | 조회자·운영 관리자 | 운영 기준일·조직·상태 | 조직·계정·권한·결재 요약·집계 시각 | 상세 이동·담당자 지정; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 기준 시각 최신, 위험 항목 담당자·기한 기록 |
| 조직·인사 | `organizations`<br>**조직** | 조직 관리자 | ID·명칭·상위 조직·적용일 | 계층 순환·폐쇄·유효기간·하위 영향 | 등록·변경·폐쇄·이력; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 기준일별 계층 단일, 순환·기간 중복 0 |
| 조직·인사 | `organizationResponsibilities`<br>**조직 책임** | 조직 관리자 | 조직·책임 유형·기간 | 책임 범위·중복 책임자·위임 | 등록·변경·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 같은 기간·범위 충돌 0, Before/After Audit |
| 조직·인사 | `positions`<br>**직위** | 인사 관리자 | 코드·명칭·적용일 | 사용 조직·직원·권한 영향·유효기간 | 등록·변경·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 실효 직위와 Assignment 기준일 일치 |
| 조직·인사 | `jobTitles`<br>**직책** | 인사 관리자 | 코드·명칭·적용일 | 결재·권한 정책 참조·유효기간 | 등록·변경·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 실효 권한·결재 경로 재계산 |
| 조직·인사 | `employees`<br>**직원** | 인사 관리자 | 직원 ID·조직·재직 상태·적용일 | 입사·이동·휴직·퇴사·중복 Assignment | 등록·발령·휴직·복직·퇴사; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 재직·소속 단일, 계정·권한 회수 연결 |
| 조직·인사 | `assignments`<br>**발령·배정** | 인사 관리자 | 직원·조직·직위·기간 | 기간 중복·주 소속·과거 이력 | 발령 등록·변경·취소; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 적용일 전후 결과 일치, 과거 이력 보존 |
| 계정·권한 | `users`<br>**사용자 계정** | 계정 관리자 | Login ID·직원·상태 | 직원 연결·Lock·만료·Session·마지막 Login | 등록·활성·Lock 해제·비활성; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 개인 계정, 퇴직·이동 Session/권한 회수 |
| 계정·권한 | `roles`<br>**Role** | 권한 관리자·승인자 | Role ID·명칭·Version·기간 | Permission·직무분리·사용 사용자 | Draft·Validate·Approval·Publish·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 승인 Version만 실효 계산, 충돌 0 |
| 계정·권한 | `permissions`<br>**Permission** | 권한 관리자 | ID·Resource·Action | API·Menu·Button·Data Scope·원문·Export | 등록·변경·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | Backend Permission과 화면 가시성 의미 일치 |
| 계정·권한 | `userRoles`<br>**사용자 Role** | 권한 관리자·승인자 | 사용자·Role·유효기간·Reason | 직접/상속·Approval·기간·충돌 | 부여·회수·기간 변경; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 실효 권한=승인 내용, 회수 후 Session 반영 |
| 계정·권한 | `menus`<br>**Menu 권한** | 권한 관리자 | Menu ID·Route·상위 Menu·Permission | Route 중복·표시 순서·활성 조건·접근 권한 | 등록·변경·숨김·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | Menu와 API Permission 일치, 직접 접근 거부 |
| 계정·권한 | `permissionTools`<br>**실효 권한 도구** | 권한 관리자·감사 담당자 | 사용자·Role·기준일·업무 대상 | 실효 Permission·직무분리·Data Scope·Diff | Simulation·충돌 분석; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 적용 전 예상과 적용 후 실효 차이 0 |
| 결재 | `approvalPolicies`<br>**결재 정책** | 결재 관리자·승인자 | Policy ID·업무 유형·조건·Version·기간 | 단계·금액/조직 조건·자기 승인·위임 | Draft·Simulation·Approval·Publish·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 상신 시 Version 고정, 진행 경로 불변 |
| 결재 | `approvalSimulation`<br>**결재 경로 사전 계산** | 요청자·결재 관리자 | 업무 유형·요청자·금액·기준일 | 예상 승인자·위임/대결·충돌·경로 없음 | 경로 Simulation; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 실제 상신 경로와 일치, 경로 없음 사전 차단 |
| 결재 | `approvalSubmissions`<br>**상신** | 업무 요청자 | 업무 ID·Policy Version·Reason | Request Hash·Attachment·결재 경로·멱등 Key | 상신·철회·취소·상태 조회; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 중복 0, 업무 ID·Approval ID·Policy Version 대사 |
| 결재 | `approvalInbox`<br>**승인함** | 승인자 | 승인자·상태·기한·업무 유형 | 자기 승인·Request Version·Attachment·Reason·만료 | Approve·Reject·Hold·상세; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 단계별 단일 결정, 업무 반영·Audit 연결 |
| 결재 | `approvalDelegations`<br>**위임·대결** | 결재 관리자·승인자 | 위임자·대리자·Scope·기간 | 중첩·순환·만료·책임 주체 | 등록·변경·조기 종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 유효 위임만 적용, 원/대리 책임 Audit |
| 첨부·알림 | `attachments`<br>**Attachment** | 업무 사용자·보안 담당자 | Attachment ID·업무 ID·상태 | 이름·크기·Content Type·Checksum·검사·격리 | Upload·Download·격리 해제 요청·만료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 검사 통과만 연결, 원문·Download Audit |
| 첨부·알림 | `notifications`<br>**Notification** | 운영자 | 수신자·Channel·상태·기간 | Masking·Attempt·ACK·대체 Channel·Escalation | 재전송·확인·종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 중복 정책 준수, 실패 원인·다음 Attempt 기록 |
| 첨부·알림 | `downloads`<br>**반출 File** | 반출 권한자·승인자 | Operation ID·생성자·상태 | Permission·Approval·Masking·Hash·만료 | 생성·Download·만료·재생성; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | Hash 일치, 승인 범위·기간 내 Download |
| 첨부·알림 | `downloadAudits`<br>**Download Audit** | 감사 담당자 | Actor·File·업무·기간 | 시각·IP·Session·Reason·Approval ID | 조회·Evidence 반출; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | File과 Audit의 Hash·Actor·시각 일치 |
| 개인화·운영 | `savedSearches`<br>**저장 검색** | 사용자 | 사용자·화면·공유 범위 | 민감 조건·개인/공유·기본 기간 | 저장·변경·공유·삭제; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 권한 밖 조건·개인정보 원문 저장 0 |
| 개인화·운영 | `sessions`<br>**Session** | 보안 담당자·계정 관리자 | 사용자·Session·상태·기간 | 발급·마지막 사용·만료·IP·Device·권한 변경 | 개별/전체 Session 종료; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | Lock·권한 회수 후 이전 Session API 차단 |
| 개인화·운영 | `settings`<br>**BZA 설정** | 운영자·승인자 | Key·환경·Version·Target | 현재값·Default·Secret·재기동·적용 상태 | Preview·Approval·Apply·Reconcile·Rollback; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | Target Version·Checksum 일치, Secret 원문 0 |
| 개인화·운영 | `audits`<br>**Audit** | 감사 담당자·반출 권한자 | Actor·Action·Target·기간·Trace | Before/After·Reason·Approval·업무/Operation | 조회·Evidence 반출; 변경 조치는 Reason·필요 Approval·Expected Version 확인 | 조직·권한·결재·반출 변경 누락 0 |

대량 조직·Role·Permission 변경은 Target별 결과를 저장한다. 응답 유실 시 같은 Button을 반복하지 않고 업무 ID·Approval ID·Idempotency Key·Operation ID로 기존 상태와 Audit을 조회한다.

## 21. 화면 운영 표준

실제 Route·Component·Permission은 최신 Frontend Source와 Backend Manifest를 전수 대조해야 한다. Source에 없는 메뉴나 Button을 만들지 않는다.

각 화면에 다음을 기록한다.

| 항목 | 예시 범주 |
|---|---|
| 메뉴·Route | 조직, 사용자, Role, Permission, 결재, 위임, Audit |
| Permission | 조회·변경·승인·Export·원문 조회 |
| 검색 Field | 코드·이름·상태·기간·조직 |
| 기본값 | 기준일·활성 상태·Page Size |
| Column | 상태·Version·유효기간·Owner |
| 상세 Field | 관계·Assignment·Policy·Audit |
| Button | 등록·변경·비활성·승인·반려·위임·Export |
| 활성 조건 | 상태·Permission·Expected Version |
| 입력 | Reason·Approval·유효기간 |
| 부분 실패 | 대상별 결과·재처리 |
| 응답 유실 | Operation·Audit 조회 |

기준 Commit의 실제 Route·Component·Button을 Browser에서 전수 실행한 결과는 `미검증`이다.

## 22. 운영 Runbook

### 22.1 조직 고아·순환

- 기준일의 Parent 관계를 조회한다.
- 조직 이동·종료 이력을 확인한다.
- 임의 DB 수정 대신 승인된 Command를 사용한다.
- 수정 후 조직 Tree·권한 Scope·결재선을 재계산한다.

### 22.2 퇴직·잠금 사용자 접근

- 사용자·직원 상태와 Session을 확인한다.
- Role·위임·대결·Token을 종료한다.
- 강제 Session 종료와 Audit를 확인한다.

### 22.3 권한 Drift

- Desired Role·Permission Catalog와 Actual Assignment를 비교한다.
- Cache·Session 반영 지연을 확인한다.
- 과다 권한을 먼저 차단하고 영향 사용자를 확인한다.
- Drift 0과 대표 API 접근을 검증한다.

### 22.4 결재 응답 유실

- Approval ID·Policy Version으로 조회한다.
- 재요청 전에 기존 상태를 확정한다.
- `UNKNOWN_RESULT`이면 업무 Owner와 실행 결과를 대사한다.

### 22.5 부분 적용

대량 Role 부여나 조직 변경에서 일부 대상만 성공하면 성공 대상을 다시 실행하지 않는다. 대상별 결과를 저장하고 실패 대상만 재처리한다.

## 23. Backup·Restore

### 범위

- 조직·직원·사용자·Assignment
- Role·Permission Catalog
- Data Scope·Masking Policy
- 결재 정책·승인 이력
- 위임·대결
- Session Metadata·Audit
- Config·Artifact·Migration History

Restore 후 대표 사용자가 로그인하고 목록·상세·Command·승인·Audit를 실행한다. DB Restore 성공만으로 BZA 정상화를 판정하지 않는다.

## 24. Upgrade·Rollback

1. Compatibility Matrix를 확인한다.
2. Backup과 격리 Restore 결과를 확인한다.
3. DB Migration Pre-check를 실행한다.
4. 제한 사용자·조직으로 Canary를 수행한다.
5. 조직 Tree·Permission·Data Scope·결재·과거 Audit를 대사한다.
6. 오류 시 Application·Config를 Rollback한다.
7. DB 역변경이 불가하면 Forward Recovery를 수행한다.
8. Session·Cache·Generated Client 호환을 확인한다.

## 25. Test Matrix

```text
조직 Tree·고아·순환·유효기간
직원·사용자 연결·휴직·퇴직·이동
Role·Permission·Action Permission
Data Scope·하위 조직·지정 조직
Masking·원문 조회·Export
결재선·Quorum·Policy Version·대상 Hash
승인·반려·취소·만료
위임·대결·순환·기간
Session·잠금·강제 종료
Attachment·악성파일·Download Audit
Notification Timeout·Receipt·중복
응답 유실·UNKNOWN_RESULT·대사
대량 부분 실패·재처리
Backup·Restore·Upgrade·Rollback
Browser·API 우회·Audit
```

## 26. EDU — 조직·권한·결재 종단간 실습

1. 조직 Root와 2단계 하위 조직을 등록한다.
2. 직원과 로그인 사용자를 연결한다.
3. Role·Permission·Data Scope를 부여한다.
4. 조직 범위 밖 데이터가 차단되는지 확인한다.
5. Masked 목록·상세·원문 조회 Permission을 비교한다.
6. 2단계 결재 정책과 대상 Hash를 등록한다.
7. 승인·반려·만료를 실행한다.
8. 위임·대결·순환 차단·만료를 확인한다.
9. 승인 후 업무 Owner Command를 호출한다.
10. Timeout을 주입하고 Approval·Operation을 대사한다.
11. Export·Download·Audit를 확인한다.
12. Backup·Restore 후 대표 시나리오를 재실행한다.

### 26.1 BZA EDU 14개 선택표

기준 기능 카탈로그에는 다음 BZA EDU가 정의돼 있다. `cpf.reference.features.backoffice.enabled` 활성 조건, Handler·Consumer·DB·Browser·Fault 결과를 함께 확인하며, 목록 존재만으로 실행 성공을 판정하지 않는다.

| EDU | 확인 기능 | 역할 | 활성 조건 | 검증 상태 |
|---|---|---|---|---|
| `EDU-BZA-01` | 조직·직원·Assignment·기준일 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-02` | 사용자·Role·Permission·실효 권한 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-03` | 결재 Policy Version·경로 Simulation | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-04` | 상신·승인·반려·철회·취소 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-05` | 위임·대결·책임 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-06` | Attachment·Notification·Audit·Download | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-07` | 초기 관리자·첫 Login·권한 인계 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-08` | 조직 개편·기준일·과거 이력 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-09` | 입사·이동·휴직·복직·퇴직 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-10` | Role 충돌·직무분리·Permission Simulation | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-11` | 위임 중첩·만료·결재 경로 재계산 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-12` | 계정 Lock·비밀번호 초기화·Session 종료 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-13` | Masking·Audit·승인 반출 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |
| `EDU-BZA-14` | 업무 승인 결과 반영·실패 정상화 | `CPF_BZA_OPERATOR` | `cpf.reference.features.backoffice.enabled` | Runtime 미검증 |

## 27. 현재 상태와 Owner 작업 요청

| ID | 항목 | 판정 | 요청 |
|---|---|---|---|
| `BZA-UI-001` | 실제 Route·Component·Permission·Button 전수표 | 미검증 | Frontend·Backend Manifest·Generated Client 대조 |
| `BZA-PERM-001` | Action Permission과 API 우회 차단 | 재확인 필요 | Browser·Direct API Negative Test |
| `BZA-APPROVAL-001` | 응답 유실·대상 Hash·Policy Version | 미검증 | Runtime·DB·Audit 대사 Scenario |
| `BZA-NOTIFY-001` | Notification Provider·Receipt | 미구현 또는 재확인 필요 | Starter·Consumer·Fault Test 연결 |
| `BZA-DR-001` | Backup·Restore·Upgrade·Rollback | 미검증 | 3 Vendor 실행 Evidence |

## 28. 완료 점검표

- [ ] 도입·비도입 판단과 Owner가 확정됐다.
- [ ] 초기 관리자 Credential과 권한 분리가 확인됐다.
- [ ] 조직·직원·사용자 상태와 유효기간이 일치한다.
- [ ] Role·Permission·Data Scope·Masking이 화면과 API에서 같다.
- [ ] 결재 정책 Version·대상 Hash·승인 이력이 연결된다.
- [ ] 위임·대결·만료·순환 차단이 확인됐다.
- [ ] Attachment·Notification·Session·Export·Audit가 확인됐다.
- [ ] 업무 Domain 연동에서 Timeout·응답 유실·대사가 가능하다.
- [ ] Backup·Restore·Upgrade·Rollback 결과가 기록됐다.
- [ ] 직접 실행하지 않은 항목은 `미검증`으로 남았다.
