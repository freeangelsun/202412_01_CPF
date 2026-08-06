# CPF BZA 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 기준 Commit: `a8be27a34bdac0b7c075e06d6e86571244c96421` (`06_08`)
> 기준일: `2026-08-06 Asia/Seoul`

| 항목 | 내용 |
|---|---|
| 주 독자 | 조직·직원·사용자·권한·결재 담당자와 업무 연동 개발자, 업무관리 시스템을 처음 운영하는 담당자 |
| 이 문서로 완료할 일 | 조직 기준일에서 사용자·Role·Permission·Data Scope·결재·위임을 만들고 실제 업무 Consumer의 판정과 Audit를 확인한다. |
| 읽는 방식 | 처음 접하는 독자는 앞에서부터 실습 순서로 읽고, 숙련자는 장별 판단표와 참조 경로를 사용한다. |
| 설명 원칙 | 제품 기능은 사용할 수 있는 상태로 설명한다. 실제 Source·SQL·API·Config·Frontend·Script·Test의 이름과 경계를 유지한다. |


![BZA 업무 적용 여정](../assets/manuals/cpf-bza-book-journey.svg)

## 1. BZA의 역할

BZA는 고객 업무에 필요한 조직·직원·사용자·Role·Permission·Data Scope·결재·위임·대결·첨부·알림을 제공한다. 업무 주문·계약·결제 같은 최종 상태는 각 업무 Domain이 소유한다.

처음 도입할 때 다음 순서로 진행한다.

1. 조직 기준일과 Root 조직.
2. 직원과 발령.
3. 사용자와 직원 연결.
4. Role과 Permission.
5. Data Scope와 Masking.
6. 결재 Policy와 참여자 Snapshot.
7. 위임·대결.
8. 실제 업무 Consumer Simulation.
9. Audit·Backup·운영 인계.

## 2. 초기 설치

- Backend·Frontend Artifact와 Checksum 확인.
- DB Vendor Pack 적용.
- Menu·Permission Seed 적용.
- 초기 관리자 계정 생성.
- MFA·Password·Session 정책 적용.
- Root 조직·대표 직원·사용자 연결.
- 관리자 Role Simulation.
- Backup과 Restore·Login Smoke.
- 초기 Credential 교체와 Audit 확인.

## 3. Route 지도

| Route | 목적 | 주요 조치 |
|---|---|---|
| `/` | 조직·권한·결재 현황 | 이상 카드 이동 |
| `/organizations` | 조직 계층 | 저장·폐기 |
| `/employees` | 직원 Profile | 저장·상태 변경 |
| `/positions` | 직급 | Version 관리 |
| `/jobTitles` | 직책 | Version 관리 |
| `/assignments` | 발령·겸직·파견 | 적용·종료 |
| `/organizationResponsibilities` | 조직 책임 | 책임자 지정 |
| `/users` | 사용자 | 생성·잠금·연결 |
| `/roles` | Role | 생성·상태 변경 |
| `/userRoles` | 사용자 Role | 유효기간 부여 |
| `/menus` | Menu Registry | Menu Version |
| `/permissions` | Permission·Data Scope | 저장·상태 변경 |
| `/permissionTools` | Role 비교·Simulation | Effective 판정 |
| `/approvalInbox` | 결재 처리 | 승인·반려 |
| `/approvalSubmissions` | 상신·Lifecycle | 상신·회수 |
| `/approvalPolicies` | Versioned Policy | Draft·Approve·Retire |
| `/approvalSimulation` | 경로 Simulation | 참여자 계산 |
| `/approvalDelegations` | 위임·대결 | 기간·범위 등록 |
| `/sessions` | Session | 조회·폐기 |
| `/audits` | 업무 Audit | 조회·Export |
| `/notifications` | 업무 알림 | Rule·Delivery |
| `/attachments` | 첨부 | Upload·Scan·Download |
| `/savedSearches` | 저장 검색 | 저장·공유 |
| `/settings` | 업무 설정 | Version 저장 |
| `/downloads` | Download Policy | Token·행 제한 |
| `/downloadAudits` | Download Audit | 조회·대사 |

## 4. 조직 계층

### 4.1 입력

- 조직 Code와 이름.
- 상위 조직.
- 유효 시작·종료일.
- 조직 유형.
- 상태.
- Expected Version.
- Reason.

### 4.2 검증

- 자기 자신 또는 하위 조직을 상위로 지정하지 않는다.
- 같은 기준일에 조직 Code가 중복되지 않는다.
- 상위 조직의 유효기간 안에 하위 조직이 존재한다.
- 사용 중인 조직 폐기는 직원·Role·결재 영향 확인 뒤 수행한다.

### 4.3 정상 판정

조직 Path와 Level이 재계산되고 순환·고아가 없으며 기준일 조회 결과가 History와 일치한다.

## 5. 직원·발령

직원 Profile과 사용자 계정을 구분한다. 한 직원이 여러 조직에 겸직할 수 있으므로 대표 발령과 추가 발령을 기준일로 계산한다.

| 상황 | 판정 |
|---|---|
| 신규 입사 | 직원 생성→발령→사용자 연결 |
| 조직 이동 | 기존 발령 종료→새 발령 시작 |
| 겸직 | 추가 Assignment와 Scope 명시 |
| 휴직 | 재직 상태와 Login 정책 분리 |
| 퇴직 | 발령 종료·Role 만료·Session 폐기 |

## 6. 사용자와 Session

사용자 생성 시 직원 ID, Login ID, 상태, MFA, 유효기간을 연결한다. 직원 상태와 사용자 Login 상태는 같은 값이 아니므로 정책으로 매핑한다.

Session 화면에서는 발급 시각, 마지막 접근, IP·Device, 만료, 폐기 사유를 확인한다. 비밀번호 초기화나 고위험 Role 변경 뒤 기존 Session 폐기 정책을 적용한다.

## 7. Role·Permission·Data Scope

### 7.1 세 층

1. Menu: 화면에 진입할 수 있는가.
2. Action/API: Button과 Backend Operation을 실행할 수 있는가.
3. Data Scope: 어느 조직·고객·업무 Row를 볼 수 있는가.

Masking은 별도다. 조회 권한이 있어도 원문 Field 권한이 없으면 Masked 값만 본다.

### 7.2 Permission 설계 예

| Permission | Resource | Action | Scope |
|---|---|---|---|
| `PAY_METHOD_READ` | PAY Method | READ | 자기 조직 고객 |
| `PAY_METHOD_SUSPEND` | PAY Method | SUSPEND | 담당 기관 |
| `PAY_METHOD_RAW_ACCOUNT` | Account Field | UNMASK | 승인된 조사 건 |
| `PAY_METHOD_EXPORT` | PAY Method | EXPORT | 최대 5,000행·Reason 필수 |

### 7.3 Simulation

Role을 적용하기 전에 사용자·기준일·Resource·Action·Target을 넣어 Effective 결과를 확인한다. Allow뿐 아니라 Deny, Data Scope, Masking 근거를 함께 보여 준다.

## 8. 결재 Policy

Policy는 Draft·Approved·Retired Version으로 관리한다.

입력:

- 업무 Action.
- 금액·위험도·조직 조건.
- 단계와 승인자 Role.
- 요청자와 승인자 분리.
- 병렬·순차 방식.
- 만료와 Escalation.
- 대결·위임 허용 범위.

실행 시 참여자와 대상 Snapshot을 고정한다. 조직이나 Role이 바뀌어도 진행 중 문서의 과거 Snapshot을 수정하지 않는다.

## 9. 결재 Lifecycle

```text
DRAFT → SUBMITTED → IN_REVIEW → APPROVED | REJECTED | WITHDRAWN | EXPIRED
```

- 상신 전 Validation.
- 상신 시 Policy Version과 참여자 Snapshot.
- 승인·반려 시 Actor·Reason.
- 실행은 승인 문서와 Target Snapshot 일치 확인.
- 응답 유실은 Approval 완료와 업무 실행 결과를 별도로 대사.

## 10. 위임·대결

| 항목 | 입력 |
|---|---|
| 위임자·대결자 | 사용자 ID |
| 기간 | 시작·종료 시각 |
| 범위 | 조직·업무·Action |
| 제외 | 자기 승인·고위험 Action |
| Reason·Approval | 정책에 따라 필요 |

만료 뒤 Effective Permission에서 제거됐는지 확인한다. 원 요청자와 실제 결정자를 Audit에 모두 남긴다.

## 11. Attachment·Notification

Attachment는 Upload·Checksum·Scan·Quarantine·Download Permission을 사용한다. Notification은 Template Version·Channel·Delivery Attempt·Provider Receipt·DLQ를 관리한다.

결재 알림이 실패해도 결재 상태를 임의 변경하지 않는다. Delivery 상태와 결재 상태를 분리한다.

## 12. 업무 Domain 연동

업무 Service는 다음 입력으로 BZA를 호출한다.

```json
{
  "actorId": "USR-1004",
  "businessDate": "2026-08-06",
  "resource": "PAY_METHOD",
  "action": "SUSPEND",
  "targetOrganizationId": "ORG-SEOUL-01",
  "targetId": "PM-000123"
}
```

응답에는 Effective Permission, Data Scope, Masking Decision, Policy Version, 기준시각을 포함한다. 업무 Service는 최종 업무 상태를 자기 Domain 규칙과 함께 판단한다.

## 13. 종합 실습

1. `ORG-HQ`와 `ORG-PAY` 조직 생성.
2. 직원 A·B 생성, PAY 조직 발령.
3. 사용자 A·B 연결과 MFA 적용.
4. `PAY_OPERATOR`, `PAY_APPROVER` Role 생성.
5. 조회·정지·원문·Export Permission 설정.
6. 사용자 A에게 운영 Role, B에게 승인 Role 부여.
7. 정지 Action 결재 Policy 생성·승인.
8. A가 PAY 정지 요청 상신.
9. B가 승인.
10. 업무 Service가 승인 Snapshot을 확인해 실행.
11. Permission Simulation과 업무 API 결과 비교.
12. Audit에서 요청자·승인자·실행 Operation 연결.

## 14. 오류·복구

| 오류 | 복구 |
|---|---|
| 조직 순환 | 상위 관계를 이전 Version으로 복원 |
| 사용 중 조직 폐기 | Assignment·Role·Policy 참조를 정리 후 새 요청 |
| Role 과다 권한 | Simulation·영향 Matrix 후 Version 교정 |
| 자기 승인 | Policy 위반으로 차단, 다른 승인자 재배정 |
| 위임 만료 | Effective 결과 재계산, 진행 문서 Snapshot 확인 |
| 결재 응답 유실 | Approval 문서 ID로 결과 조회 |
| 업무 실행 응답 유실 | 업무 Operation과 Approval을 각각 대사 |
| Attachment Scan 실패 | Quarantine 유지·Download 차단 |

## 15. Backup·Upgrade·Rollback

- 조직·Assignment·Role·Permission·Policy·Approval·Audit를 같은 복구점으로 관리한다.
- Upgrade 전 기준일 Query와 Simulation Fixture를 저장한다.
- Rollback 뒤 진행 중 Approval Snapshot을 잃지 않는다.
- Session·Notification·Attachment Metadata와 Object를 대사한다.

## 16. 운영 인계

- 조직 Root와 기준일.
- 직원·사용자 연결 규칙.
- Role·Permission·Data Scope·Masking Matrix.
- 결재 Policy Version과 참여자 계산.
- 위임·대결 제한.
- Attachment·Notification Provider.
- Session·Password·MFA 정책.
- Backup·Restore·Upgrade·Rollback.
- 업무 Consumer와 Simulation Test.

## 17. BZA 자체 검수

1. 조직 기준일과 History가 일치하는가?
2. 순환·고아 조직이 없는가?
3. 직원과 사용자 상태를 구분했는가?
4. Menu·Action·Data Scope·Masking을 분리했는가?
5. Role 적용 전 Simulation했는가?
6. 요청자와 승인자가 분리됐는가?
7. Policy·참여자·Target Snapshot을 보존하는가?
8. 위임·대결의 기간·범위·감사가 있는가?
9. 업무 Service의 실제 판정과 BZA 결과가 일치하는가?
10. Restore 뒤 진행 문서와 Audit를 대사할 수 있는가?

<!-- CPF_R10_QUALITY_EXPANSION -->

## 부록 A. BZA 26개 기능 Route 작업 카드

### A.1 `dashboard` — 업무관리 현황

| 항목 | 내용 |
|---|---|
| 업무 결과 | 조직·사용자·결재·알림 이상을 분류 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.2 `organizations` — 조직

| 항목 | 내용 |
|---|---|
| 업무 결과 | 기준일 조직 계층 생성·변경 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.3 `employees` — 직원

| 항목 | 내용 |
|---|---|
| 업무 결과 | 발령·소속·재직 상태 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.4 `users` — 사용자

| 항목 | 내용 |
|---|---|
| 업무 결과 | 직원과 로그인 계정 연결 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.5 `roles` — 역할

| 항목 | 내용 |
|---|---|
| 업무 결과 | 업무 역할 Version 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.6 `permissions` — 권한

| 항목 | 내용 |
|---|---|
| 업무 결과 | Menu·Button·API Permission 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.7 `dataScopes` — 데이터 범위

| 항목 | 내용 |
|---|---|
| 업무 결과 | 조직·고객·업무 Scope 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.8 `approvalPolicies` — 결재 정책

| 항목 | 내용 |
|---|---|
| 업무 결과 | 조건별 결재선 정책 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.9 `approvalRequests` — 결재 요청

| 항목 | 내용 |
|---|---|
| 업무 결과 | 제출·승인·반려·회수 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.10 `delegations` — 위임·대결

| 항목 | 내용 |
|---|---|
| 업무 결과 | 기간·Scope 제한 위임 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.11 `attachments` — 첨부

| 항목 | 내용 |
|---|---|
| 업무 결과 | 업무/결재 첨부 권한 관리 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.12 `notifications` — 알림

| 항목 | 내용 |
|---|---|
| 업무 결과 | Rule·Delivery·Receipt 운영 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.13 `sessions` — 세션

| 항목 | 내용 |
|---|---|
| 업무 결과 | 활성 Session·강제 종료 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.14 `auditLogs` — 감사

| 항목 | 내용 |
|---|---|
| 업무 결과 | Before/After·Actor·Reason 조회 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.15 `exports` — 내보내기

| 항목 | 내용 |
|---|---|
| 업무 결과 | 권한·Masking·Download Audit |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.16 `referenceCatalogs` — 기준정보

| 항목 | 내용 |
|---|---|
| 업무 결과 | 업무 Code·Reference 연결 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.17 `organizationChanges` — 조직 변경

| 항목 | 내용 |
|---|---|
| 업무 결과 | 미래 조직 개편 Preview |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.18 `employeeAppointments` — 발령

| 항목 | 내용 |
|---|---|
| 업무 결과 | 겹침·취소·미래 발령 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.19 `userRoles` — 사용자 역할

| 항목 | 내용 |
|---|---|
| 업무 결과 | 기준일 Role 할당 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.20 `rolePermissions` — 역할 권한

| 항목 | 내용 |
|---|---|
| 업무 결과 | Role별 Permission Matrix |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.21 `scopeSimulations` — 범위 시뮬레이션

| 항목 | 내용 |
|---|---|
| 업무 결과 | Effective Data Scope 검증 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.22 `approvalSimulations` — 결재선 시뮬레이션

| 항목 | 내용 |
|---|---|
| 업무 결과 | 조건별 Participant 계산 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.23 `approvalHistory` — 결재 이력

| 항목 | 내용 |
|---|---|
| 업무 결과 | Snapshot·Decision Timeline |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.24 `notificationDlq` — 알림 DLQ

| 항목 | 내용 |
|---|---|
| 업무 결과 | 실패 Delivery 격리·Replay |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.25 `security` — BZA 보안

| 항목 | 내용 |
|---|---|
| 업무 결과 | MFA·IP·Session 정책 |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

### A.26 `systemSettings` — BZA 설정

| 항목 | 내용 |
|---|---|
| 업무 결과 | 업무관리 제품 설정·Version |
| 주 입력 | 기준일·업무 식별자·expectedVersion·reason·actor |
| 정상 결과 | 현재 Row·History·Effective 결과·Audit가 같은 Version으로 연결 |
| 경계 조건 | 유효기간 겹침·기준일 경계·권한 상실·Session stale |
| 실패 처리 | 409·403·Timeout·부분 적용을 구분하고 원본 요청을 덮어쓰지 않음 |
| 복구 | 최신 Snapshot 재조회→영향 Simulation→새 Operation 또는 승인 요청 |

## 부록 B. BZA EDU 14개

BZA EDU는 기준일 조직·직원·사용자·Role·Data Scope·결재 Snapshot을 준비하고 정상·경계일·권한 회수·응답 유실을 검증한다. 업무 Owner 반영과 Session·Audit까지 확인한다.

### EDU-BZA-01 — 초기 관리자 Bootstrap

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 초기 관리자 Bootstrap의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Bootstrap Token·Admin ID |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 1회성 생성과 Audit |
| 장애 재현 | Token 재사용 |
| 복구 판정 | 폐기 후 새 Bootstrap 절차 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-02 — 조직 계층

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 조직 계층의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Org ID·Parent·Effective Date |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 기준일 계층 계산 |
| 장애 재현 | Cycle·겹침 |
| 복구 판정 | 변경 차단·유효기간 보정 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-03 — 직원 발령

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 직원 발령의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Employee·Org·기간 |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 현재/미래 발령 분리 |
| 장애 재현 | 겹치는 발령 |
| 복구 판정 | Version Conflict 처리 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-04 — 사용자 연결

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 사용자 연결의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | User·Employee·Status |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 계정과 인사 상태 연결 |
| 장애 재현 | 퇴직자 활성 |
| 복구 판정 | Session 폐기·상태 변경 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-05 — Role 정의

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | Role 정의의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Role Code·Version |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | Permission Set 고정 |
| 장애 재현 | 사용 중 삭제 |
| 복구 판정 | 새 Version과 영향 분석 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-06 — Permission Matrix

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | Permission Matrix의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Menu·Button·API |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | Effective Permission 일치 |
| 장애 재현 | Self lockout |
| 복구 판정 | Break-glass 경로 확인 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-07 — Data Scope

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | Data Scope의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Org/Customer Scope |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 조회·Export 범위 일치 |
| 장애 재현 | Scope 확대 |
| 복구 판정 | Simulation·Audit로 검출 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-08 — 결재 정책

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 결재 정책의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Rule·Amount·Risk |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 조건별 결재선 생성 |
| 장애 재현 | Rule 중첩 |
| 복구 판정 | 우선순위·Version 보정 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-09 — 결재 Snapshot

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 결재 Snapshot의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Request·Participants |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 요청 시 결재선 고정 |
| 장애 재현 | 조직 변경 후 Drift |
| 복구 판정 | 기존 Snapshot 유지 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-10 — 승인·반려·회수

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 승인·반려·회수의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Decision·Reason |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 허용 상태전이와 Audit |
| 장애 재현 | 자기 승인·중복 결정 |
| 복구 판정 | 서버 차단·현재 상태 반환 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-11 — 위임·대결

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 위임·대결의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Delegator·Delegatee·기간 |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 기간·Scope 내에서만 적용 |
| 장애 재현 | 순환 위임 |
| 복구 판정 | 등록 차단 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-12 — Attachment·Notification

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | Attachment·Notification의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Attachment ID·Template |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 결재와 파일·알림 연결 |
| 장애 재현 | 파일 권한·발송 중복 |
| 복구 판정 | 권한 재검증·Receipt 대사 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-13 — 업무 Domain 연동

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | 업무 Domain 연동의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `업무 Domain + BZA` 및 실제 Consumer·Test·Config |
| 주요 입력 | Approval Result·Policy Version |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 업무 Owner가 결과 소비 |
| 장애 재현 | BZA가 업무 DB 직접 수정 |
| 복구 판정 | Owner Command로 교체 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

### EDU-BZA-14 — Backup·Upgrade

| 항목 | 수행 내용 |
|---|---|
| 학습 결과 | Backup·Upgrade의 선택 기준과 정상·실패·복구 의미를 설명하고 직접 판정한다. |
| Repository 확인 위치 | `cpf-biz-admin` 및 실제 Consumer·Test·Config |
| 주요 입력 | Schema·Policy Export |
| 실행 순서 | Fixture 준비 → 정상 실행 → 원장·로그·Trace·Audit 확인 → 장애 주입 → 복구 실행 → 재검증 |
| 정상 판정 | 조직·권한·결재 이력 복구 |
| 장애 재현 | Policy 누락 |
| 복구 판정 | Restore 후 Effective Permission 대사 |
| 운영 확인 | - |
| 고객 업무 전환 | 예제 ID·상태·Permission·SLA만 고객 값으로 바꾸고 Idempotency·Version·Audit·복구 계약은 유지 |

## 부록 C. 조직·권한·결재 종합 예제

1. `2026-09-01` 시행 조직 개편을 미래 Version으로 등록한다.
2. 직원 `E1001`의 미래 발령을 등록하고 기존 발령과 겹치지 않는지 검증한다.
3. `PAY_REVIEWER` Role과 `PAY_METHOD_READ`, `PAY_METHOD_SUSPEND` Permission을 연결한다.
4. Data Scope를 `본부 A 하위 조직`으로 제한하고 기준일 Simulation을 실행한다.
5. 금액 1천만원 이상 또는 위험등급 HIGH인 정지 요청에 2인 결재 정책을 적용한다.
6. 요청 시 Participant와 Policy Version을 Snapshot으로 저장한다.
7. 조직 개편이 시행돼도 진행 중 결재는 기존 Snapshot을 유지한다.
8. 승인 결과는 PAY Owner Command가 소비하고 BZA가 PAY Table을 직접 수정하지 않는다.
9. 사용자 Role을 회수한 뒤 기존 Session에서 메뉴·API가 차단되는지 확인한다.
10. Audit·Export·Notification Receipt를 같은 Approval ID로 조회한다.

<!-- CPF_R10_BOOK_EXPANSION -->

## 부록 D. 신규 고객사의 조직·권한·결재를 구성하는 책형 예제

예제 회사는 본사 아래 영업본부와 운영본부가 있고, 영업본부의 결제수단 정지 요청을 운영본부 승인자가 승인한다. 조회 범위와 Export 범위는 분리한다.

### D.1 기준정보 설계

| 대상 | 예제 | 필수 계약 |
|---|---|---|
| 조직 | ROOT > SALES, OPS | 유효기간·Version·Cycle 금지 |
| 직원 | E100 영업 담당, E200 운영 승인자 | 현재/미래 발령과 겹침 금지 |
| 사용자 | u-sales, u-approver | 직원 상태와 Session 연결 |
| Role | PAY_OPERATOR, PAY_APPROVER | Versioned Permission Set |
| Permission | PAY_READ, PAY_SUSPEND_REQUEST, PAY_SUSPEND_APPROVE, PAY_EXPORT | Menu·Button·API 구분 |
| Data Scope | SALES는 자기 조직 고객, OPS 승인자는 요청 대상 | 기준일 계산과 Snapshot |
| 결재 정책 | 금액 무관 1단계 운영 승인 | 정책 Version과 Participant Snapshot |
| 위임 | E200 부재 시 E201, 3일 | 기간·Scope·순환 금지 |

### D.2 적용 순서

1. `/organizations`에서 ROOT·SALES·OPS를 유효 시작일과 함께 등록한다.
2. `/employees`에서 직원 발령을 등록하고 기준일별 소속을 Preview한다.
3. `/users`에서 사용자와 직원을 연결하고 초기 Session 정책을 확인한다.
4. `/roles`에서 역할을 Version 1로 만들고 사용 중 직접 삭제하지 않는다.
5. `/permissions`에서 Menu·Button·API Permission을 각각 연결한다.
6. `/data-scopes`에서 조직 범위를 Simulation하고 확대·축소 결과를 비교한다.
7. `/approval-policies`에서 정책 조건과 승인자를 고정한다.
8. `/delegations`에서 기간과 업무 Scope를 제한한 위임을 등록한다.
9. 업무 신청을 생성해 결재선 Snapshot과 Policy Version을 확인한다.
10. 승인·반려·회수·만료·위임 시나리오를 수행하고 Audit를 대사한다.

### D.3 조직 발령 JSON 예시

```json
{
  "employeeId": "E100",
  "organizationId": "SALES",
  "positionCode": "STAFF",
  "effectiveFrom": "2026-08-10",
  "effectiveTo": null,
  "expectedVersion": 3,
  "reason": "영업본부 정기 발령"
}
```

### D.4 Permission Matrix

| 역할 | 메뉴 | 조회 | 요청 | 승인 | Export |
|---|---|---|---|---|---|
| PAY_VIEWER | PAY_METHOD | 허용 | 금지 | 금지 | 금지 |
| PAY_OPERATOR | PAY_METHOD | 허용 | 허용 | 금지 | 별도 승인 |
| PAY_APPROVER | PAY_APPROVAL | 요청 대상 조회 | 금지 | 허용 | 금지 |
| PAY_AUDITOR | PAY_AUDIT | Masking 조회 | 금지 | 금지 | 감사 Export |
| BZA_SECURITY_ADMIN | BZA_SECURITY | 정책 조회 | Role 변경 | 정책 승인 | 권한 보고서 |

### D.5 Effective Permission 계산 예

```text
사용자 u-sales
  ├─ 직원 E100
  ├─ 기준일 2026-08-11 소속 SALES
  ├─ Role PAY_OPERATOR version 2
  ├─ Permission PAY_READ, PAY_SUSPEND_REQUEST
  ├─ Data Scope SALES subtree
  └─ 제외 정책: 퇴직·잠금·만료·위험 세션

결과
  PAY_READ              = ALLOW
  PAY_SUSPEND_REQUEST   = ALLOW
  PAY_SUSPEND_APPROVE   = DENY
  PAY_EXPORT            = DENY
```

### D.6 결재 Snapshot

```json
{
  "approvalRequestId": "APR-10001",
  "businessType": "PAY_METHOD_SUSPEND",
  "businessId": "PM-1001",
  "policyId": "POL-PAY-SUSPEND",
  "policyVersion": 4,
  "requester": { "userId": "u-sales", "organizationId": "SALES" },
  "participants": [
    { "step": 1, "actorId": "E200", "role": "PAY_APPROVER", "delegatedFrom": null }
  ],
  "targetSnapshot": {
    "methodId": "PM-1001",
    "status": "ACTIVE",
    "version": 7
  },
  "expiresAt": "2026-08-12T09:00:00+09:00"
}
```

### D.7 승인 실행 판정

| 상황 | 서버 판정 | 운영자 메시지 | 다음 행동 |
|---|---|---|---|
| 정상 승인 | APPROVED | 승인 완료, 업무 실행 대기 | Owner Operation 조회 |
| 자기 승인 | FORBIDDEN | 요청자와 승인자 분리 필요 | 다른 승인자 지정 |
| 만료 | EXPIRED | 승인 유효시간 경과 | 새 요청 생성 |
| Version Drift | CONFLICT | 대상 상태가 승인 Snapshot과 다름 | 최신 상태로 새 승인 |
| 위임 기간 밖 | FORBIDDEN | 위임 효력 없음 | 원 승인자 또는 새 위임 |
| 응답 유실 | UNKNOWN_RESULT | 결재 상태 확인 필요 | Approval ID 조회, 재결정 금지 |

### D.8 업무 Domain 연동

BZA는 PAY DB를 직접 수정하지 않는다. 승인 결과에는 Approval ID, Policy Version, Target Snapshot Hash, Approver, Decision, Reason이 포함된다. PAY Owner는 이 결과를 검증하고 자신의 Command와 Idempotency·Version·Audit 계약으로 상태를 바꾼다.

### D.9 권한 변경 뒤 Session Test

1. `u-sales`로 로그인해 PAY 메뉴와 요청 Button을 확인한다.
2. BZA에서 `PAY_SUSPEND_REQUEST`를 Role Version 3에서 제거한다.
3. 기존 Session으로 메뉴 재진입과 API 호출을 수행한다.
4. 정책에 따라 Session 폐기 또는 Permission 재평가가 일어나고 API는 403을 반환해야 한다.
5. Audit에서 Role 변경자·영향 사용자·Session 처리 결과를 확인한다.

## 부록 E. BZA 운영 결함 10개 판정표

### E.1 미래 발령이 오늘 권한에 반영됨

| 구분 | 내용 |
|---|---|
| 원인 후보 | 기준일 Filter 누락 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Effective Permission Snapshot`를 조회한다. |
| 보정 | 현재일과 미래일 Effective View를 분리 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Effective Permission Snapshot |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.2 조직 Cycle 생성

| 구분 | 내용 |
|---|---|
| 원인 후보 | Parent 검증 누락 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `조직 Graph와 Error Audit`를 조회한다. |
| 보정 | 등록 전 Ancestor Cycle 검증 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | 조직 Graph와 Error Audit |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.3 퇴직 사용자 Session 유지

| 구분 | 내용 |
|---|---|
| 원인 후보 | 인사 상태와 Session 미연결 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Session Row·403 Test`를 조회한다. |
| 보정 | 상태 변경 Event로 Session 폐기 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Session Row·403 Test |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.4 Role 삭제로 업무 중단

| 구분 | 내용 |
|---|---|
| 원인 후보 | 사용 중 영향 분석 없음 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `영향 사용자·Menu·API 목록`를 조회한다. |
| 보정 | 새 Version·Simulation·승인 후 전환 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | 영향 사용자·Menu·API 목록 |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.5 Data Scope가 상위 조직까지 확대

| 구분 | 내용 |
|---|---|
| 원인 후보 | Scope Rule 오류 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `표본 사용자 Query 결과`를 조회한다. |
| 보정 | 기준일·조직 Tree·Exclude Rule 재계산 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | 표본 사용자 Query 결과 |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.6 요청자가 자기 승인

| 구분 | 내용 |
|---|---|
| 원인 후보 | Requester/Approver 분리 없음 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Approval Audit`를 조회한다. |
| 보정 | 서버 정책에서 차단 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Approval Audit |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.7 위임이 무기한 적용

| 구분 | 내용 |
|---|---|
| 원인 후보 | 기간·Scope 없음 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Effective Delegation`를 조회한다. |
| 보정 | 시작/종료·업무유형 제한 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Effective Delegation |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.8 결재선이 조직 변경에 따라 바뀜

| 구분 | 내용 |
|---|---|
| 원인 후보 | Snapshot 미저장 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Policy Version·Participants`를 조회한다. |
| 보정 | 요청 시 Participant Snapshot 고정 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Policy Version·Participants |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.9 Attachment가 다른 요청에 노출

| 구분 | 내용 |
|---|---|
| 원인 후보 | Business Scope 검증 누락 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Download Audit`를 조회한다. |
| 보정 | Download 시 Permission·Business ID 재검증 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Download Audit |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

### E.10 알림이 두 번 발송

| 구분 | 내용 |
|---|---|
| 원인 후보 | Receipt·Idempotency 없음 |
| 확인 | 기준일·Policy Version·사용자·조직·Session·Approval ID를 고정하고 `Delivery Count·Receipt`를 조회한다. |
| 보정 | Notification Attempt와 Receipt Key 사용 |
| 회귀 Test | 정상·경계일·중복·동시성·권한 회수·응답 유실을 재현한다. |
| 완료 근거 | Delivery Count·Receipt |
| 금지 | Owner DB 직접 수정이나 Audit 삭제로 상태를 맞추지 않는다. |

<!-- CPF_R10_REFERENCE_EXPANSION -->

## 부록 F. BZA Route별 입력·권한·복구 참조

BZA 화면은 기준일과 Policy Version이 핵심이다. 조직·사용자·권한·결재를 수정할 때는 현재값뿐 아니라 적용 시작일, 영향 사용자, 기존 Session, 진행 중 결재를 함께 확인한다.

### F.1 `dashboard` — 업무관리 현황

| 항목 | 내용 |
|---|---|
| 목적 | 조직·사용자·결재·알림 이상을 분류 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.2 `organizations` — 조직

| 항목 | 내용 |
|---|---|
| 목적 | 기준일 조직 계층 생성·변경 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.3 `employees` — 직원

| 항목 | 내용 |
|---|---|
| 목적 | 발령·소속·재직 상태 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.4 `users` — 사용자

| 항목 | 내용 |
|---|---|
| 목적 | 직원과 로그인 계정 연결 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.5 `roles` — 역할

| 항목 | 내용 |
|---|---|
| 목적 | 업무 역할 Version 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.6 `permissions` — 권한

| 항목 | 내용 |
|---|---|
| 목적 | Menu·Button·API Permission 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.7 `dataScopes` — 데이터 범위

| 항목 | 내용 |
|---|---|
| 목적 | 조직·고객·업무 Scope 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.8 `approvalPolicies` — 결재 정책

| 항목 | 내용 |
|---|---|
| 목적 | 조건별 결재선 정책 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.9 `approvalRequests` — 결재 요청

| 항목 | 내용 |
|---|---|
| 목적 | 제출·승인·반려·회수 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.10 `delegations` — 위임·대결

| 항목 | 내용 |
|---|---|
| 목적 | 기간·Scope 제한 위임 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.11 `attachments` — 첨부

| 항목 | 내용 |
|---|---|
| 목적 | 업무/결재 첨부 권한 관리 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.12 `notifications` — 알림

| 항목 | 내용 |
|---|---|
| 목적 | Rule·Delivery·Receipt 운영 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.13 `sessions` — 세션

| 항목 | 내용 |
|---|---|
| 목적 | 활성 Session·강제 종료 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.14 `auditLogs` — 감사

| 항목 | 내용 |
|---|---|
| 목적 | Before/After·Actor·Reason 조회 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.15 `exports` — 내보내기

| 항목 | 내용 |
|---|---|
| 목적 | 권한·Masking·Download Audit |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.16 `referenceCatalogs` — 기준정보

| 항목 | 내용 |
|---|---|
| 목적 | 업무 Code·Reference 연결 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.17 `organizationChanges` — 조직 변경

| 항목 | 내용 |
|---|---|
| 목적 | 미래 조직 개편 Preview |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.18 `employeeAppointments` — 발령

| 항목 | 내용 |
|---|---|
| 목적 | 겹침·취소·미래 발령 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.19 `userRoles` — 사용자 역할

| 항목 | 내용 |
|---|---|
| 목적 | 기준일 Role 할당 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.20 `rolePermissions` — 역할 권한

| 항목 | 내용 |
|---|---|
| 목적 | Role별 Permission Matrix |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.21 `scopeSimulations` — 범위 시뮬레이션

| 항목 | 내용 |
|---|---|
| 목적 | Effective Data Scope 검증 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.22 `approvalSimulations` — 결재선 시뮬레이션

| 항목 | 내용 |
|---|---|
| 목적 | 조건별 Participant 계산 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.23 `approvalHistory` — 결재 이력

| 항목 | 내용 |
|---|---|
| 목적 | Snapshot·Decision Timeline |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.24 `notificationDlq` — 알림 DLQ

| 항목 | 내용 |
|---|---|
| 목적 | 실패 Delivery 격리·Replay |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.25 `security` — BZA 보안

| 항목 | 내용 |
|---|---|
| 목적 | MFA·IP·Session 정책 |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |

### F.26 `systemSettings` — BZA 설정

| 항목 | 내용 |
|---|---|
| 목적 | 업무관리 제품 설정·Version |
| 검색 입력 | 기준일·ID·상태·Version |
| 주요 Column | 식별자·상태·Version·Effective·Audit |
| 조치 | 조회/생성/변경/상태전이 (권한별) |
| Permission | BZA role + data scope |
| Version | 상태 변경은 현재 Version 또는 Policy Version을 읽고 제출한다. |
| Reason | 변경·정지·삭제·승인 조치에 업무 사유를 기록한다. |
| 응답 유실 | 같은 변경을 반복하기 전에 Operation·Approval 상태를 조회한다. |
| 대표 실패 | 403·409·Timeout·유효기간 충돌 |
| 복구 | 최신 Snapshot·Simulation·새 Operation |
| Audit | Actor·기준일·Before/After·Reason·Operation ID를 조회한다. |
| 문서 상태 | 완료 |
