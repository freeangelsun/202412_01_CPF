# CPF BZA 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF` · **기준 Branch** `master` · **기준 SHA** `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)  
> **문서 기준일** `2026-07-31` · **Runtime 검증 상태** QA32 전체 Runtime·3DB·Browser·다중 인스턴스 Evidence는 `미검증`  
> **문서 목적** 선택형 BZA의 도입 판단, 설치·Bootstrap, 조직·사용자·권한·결재·첨부·감사·다운로드와 실제 Route 사용·개발·운영 절차를 제공한다.  
> **주요 독자** BZA 도입 Architect, BZA 개발자, 업무 관리자, 권한·결재·감사 운영자  
> **완료 결과** BZA 선택 프로젝트가 문서만으로 활성화·초기 구성·전체 메뉴 사용·기능 확장·백업·복구를 수행한다.

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


## 1. 선택 기준

BZA는 조직·사용자·Role·Permission·결재·첨부·감사 같은 업무 관리자 기능이 필요한 프로젝트가 선택한다.

비선택:

- 고객 기존 IAM/HR/Workflow가 책임
- BZA Runtime·DB·Frontend가 필수 Dependency로 따라오지 않음
- Domain은 Public Contract로 외부 시스템과 연계

BZA를 단순 CRUD 화면으로 도입하지 않는다. Permission·Data Scope·Approval Snapshot·Audit·Masking·Export·Recovery가 제품 단위다.

## 2. 설치·Bootstrap

### 2.1 DB

V85/R85 `bza_bootstrap_approval` Migration이 MariaDB·PostgreSQL `bzaDB`·Oracle `bzaDB`에 존재한다.

### 2.2 Bootstrap 계약

- 1회용 승인 Hash
- 만료
- Environment Fingerprint
- 원자 Claim
- Secret File 파기
- 초기 관리자 생성
- 재실행 차단
- 감사

### 2.3 순서

1. Artifact·Source SHA·Signature
2. BZA DB Migration
3. Bootstrap Approval
4. Secret 전달·File Permission
5. 1회 기동
6. 초기 관리자·Role
7. 상태 `CLAIMED/COMPLETED`
8. Secret File 파기
9. 일반 Profile 재기동
10. 로그인·권한·감사 Probe

실패 후 승인 Row를 임의 수정하지 않는다.

## 3. 보안·Session

- Spring Security + Spring Session JDBC
- HttpOnly Cookie
- CSRF
- Secure Cookie Production 필수
- SameSite Strict/Lax
- Timeout 30분 기본, 최대 12시간
- Browser Storage Token 영구 저장 금지
- Role 변경·퇴직·강제 종료 Session 재검증
- 모든 변경 API Permission·Data Scope

## 4. 전체 Route

실제 Source: `cpf-biz-admin/frontend/src/app/routes.ts`.

| Path | 메뉴 | menuCode | Group | 설명 |
|---|---|---|---|---|
| `/` | 대시보드 | `DASHBOARD` | `overview` | 업무 운영 현황 |
| `/organizations` | 조직 | `ORGANIZATION` | `people` | 조직 계층 |
| `/employees` | 직원 | `EMPLOYEE` | `people` | 직원 Profile |
| `/positions` | 직급 | `EMPLOYEE` | `people` | 직급 기준정보 |
| `/jobTitles` | 직책 | `EMPLOYEE` | `people` | 직책 기준정보 |
| `/assignments` | 발령·겸직 | `EMPLOYEE` | `people` | 다중 소속·겸직·파견·대행 |
| `/organizationResponsibilities` | 조직 책임 | `ORGANIZATION` | `people` | 조직장·대행·승인 Owner |
| `/users` | 사용자 | `AUTHORIZATION` | `access` | BZA 인증 사용자 |
| `/roles` | 역할 | `AUTHORIZATION` | `access` | 업무 역할 |
| `/userRoles` | 사용자 Role | `AUTHORIZATION` | `access` | 다중 Role 유효기간 |
| `/menus` | 메뉴 | `AUTHORIZATION` | `access` | 화면 메뉴 Registry |
| `/permissions` | 권한 | `AUTHORIZATION` | `access` | 화면·행위·API·Data Scope 권한 |
| `/permissionTools` | 권한 분석 | `AUTHORIZATION` | `access` | Role 비교와 권한 Simulation |
| `/approvalInbox` | 결재 처리 | `APPROVAL` | `approval` | Snapshot 참여자 Inbox |
| `/approvalSubmissions` | 결재 상신 | `APPROVAL` | `approval` | 정책 기반 멱등 상신 |
| `/approvalPolicies` | 결재 정책 | `APPROVAL` | `approval` | Versioned Policy/ALL·ANY·N_OF_M |
| `/approvalSimulation` | 경로 Simulation | `APPROVAL` | `approval` | 조직/Role/위임 사전 해석 |
| `/approvalDelegations` | 결재 위임 | `APPROVAL` | `approval` | 유효기간 위임·대결 |
| `/sessions` | 내 세션 | `AUTHORIZATION` | `support` | Refresh session 관리 |
| `/audits` | 업무 감사 | `AUDIT` | `support` | Immutable 업무 감사 |
| `/notifications` | 알림 | `SETTING` | `support` | 업무 알림 |
| `/attachments` | 첨부파일 | `ATTACHMENT` | `support` | 첨부 업로드·검증 |
| `/savedSearches` | 저장 검색 | `SETTING` | `support` | 사용자 검색 조건 |
| `/settings` | 업무 설정 | `SETTING` | `support` | BZA 업무 설정 |
| `/downloads` | 다운로드 정책 | `SETTING` | `support` | 다운로드 정책 |
| `/downloadAudits` | 다운로드 감사 | `AUDIT` | `support` | 다운로드 감사 |

## 5. 메뉴별 사용

### 5.1 대시보드 — `/`

**목적**  
업무 운영 현황

**접근 계약**

- Route ID: `dashboard`
- menuCode: `DASHBOARD`
- Group: `overview`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 조직·권한·결재·감사 요약 확인
2. 미처리·오류·만료 위험 상세로 이동

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.2 조직 — `/organizations`

**목적**  
조직 계층

**접근 계약**

- Route ID: `organizations`
- menuCode: `ORGANIZATION`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 조직 검색·계층 조회
2. 신규/변경 요청
3. 유효기간·상위 조직 검증
4. 영향 사용자·승인 경로 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.3 직원 — `/employees`

**목적**  
직원 Profile

**접근 계약**

- Route ID: `employees`
- menuCode: `EMPLOYEE`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 직원 검색
2. Profile 상세
3. 마스킹 원문 승인 조회
4. 퇴직·상태 전이

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.4 직급 — `/positions`

**목적**  
직급 기준정보

**접근 계약**

- Route ID: `positions`
- menuCode: `EMPLOYEE`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 직급 Version 조회
2. 등록·변경
3. 사용 중 값 영향 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.5 직책 — `/jobTitles`

**목적**  
직책 기준정보

**접근 계약**

- Route ID: `jobTitles`
- menuCode: `EMPLOYEE`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 직책 Version 조회
2. 등록·변경
3. 조직 책임과 정합성 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.6 발령·겸직 — `/assignments`

**목적**  
다중 소속·겸직·파견·대행

**접근 계약**

- Route ID: `assignments`
- menuCode: `EMPLOYEE`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 발령·겸직 등록
2. 유효기간 충돌 검증
3. 현재/미래 소속 조회
4. 권한 재계산

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.7 조직 책임 — `/organizationResponsibilities`

**목적**  
조직장·대행·승인 Owner

**접근 계약**

- Route ID: `organizationResponsibilities`
- menuCode: `ORGANIZATION`
- Group: `people`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 조직장·대행 지정
2. 유효기간
3. 승인 Owner 재해석

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.8 사용자 — `/users`

**목적**  
BZA 인증 사용자

**접근 계약**

- Route ID: `users`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 사용자 생성/연결
2. 상태·잠금
3. 직원 매핑
4. Session 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.9 역할 — `/roles`

**목적**  
업무 역할

**접근 계약**

- Route ID: `roles`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Role 생성·Version
2. Permission 연결
3. 사용자 영향 Simulation

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.10 사용자 Role — `/userRoles`

**목적**  
다중 Role 유효기간

**접근 계약**

- Route ID: `userRoles`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Role 부여·회수
2. 유효기간
3. 중복·충돌
4. Session 재검증

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.11 메뉴 — `/menus`

**목적**  
화면 메뉴 Registry

**접근 계약**

- Route ID: `menus`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Menu Registry 조회
2. Route·menuCode 정합성
3. 활성/비활성

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.12 권한 — `/permissions`

**목적**  
화면·행위·API·Data Scope 권한

**접근 계약**

- Route ID: `permissions`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Menu·Action·API·Data Scope
2. Role Matrix
3. Fail-closed 검증

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.13 권한 분석 — `/permissionTools`

**목적**  
Role 비교와 권한 Simulation

**접근 계약**

- Route ID: `permissionTools`
- menuCode: `AUTHORIZATION`
- Group: `access`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Role 비교
2. 사용자 Effective Permission
3. What-if Simulation

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.14 결재 처리 — `/approvalInbox`

**목적**  
Snapshot 참여자 Inbox

**접근 계약**

- Route ID: `approvalInbox`
- menuCode: `APPROVAL`
- Group: `approval`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 내 승인 건 조회
2. Snapshot 검토
3. 승인/반려
4. 실행 결과

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.15 결재 상신 — `/approvalSubmissions`

**목적**  
정책 기반 멱등 상신

**접근 계약**

- Route ID: `approvalSubmissions`
- menuCode: `APPROVAL`
- Group: `approval`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 정책 선택
2. 멱등 상신
3. 현재 상태·Version
4. 상신 결과

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.16 결재 정책 — `/approvalPolicies`

**목적**  
Versioned Policy/ALL·ANY·N_OF_M

**접근 계약**

- Route ID: `approvalPolicies`
- menuCode: `APPROVAL`
- Group: `approval`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Versioned Policy
2. ALL/ANY/N_OF_M
3. 참여자 규칙
4. 게시·Rollback

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.17 경로 Simulation — `/approvalSimulation`

**목적**  
조직/Role/위임 사전 해석

**접근 계약**

- Route ID: `approvalSimulation`
- menuCode: `APPROVAL`
- Group: `approval`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 조직·Role·위임 입력
2. 예상 승인 경로
3. 누락·순환 검증

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.18 결재 위임 — `/approvalDelegations`

**목적**  
유효기간 위임·대결

**접근 계약**

- Route ID: `approvalDelegations`
- menuCode: `APPROVAL`
- Group: `approval`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 위임 등록
2. 유효기간·범위
3. 자기 승인 방지
4. 회수

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.19 내 세션 — `/sessions`

**목적**  
Refresh session 관리

**접근 계약**

- Route ID: `sessions`
- menuCode: `AUTHORIZATION`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 내 Session 조회
2. 다른 장치 확인
3. Session 종료

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.20 업무 감사 — `/audits`

**목적**  
Immutable 업무 감사

**접근 계약**

- Route ID: `audits`
- menuCode: `AUDIT`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. Actor·Action·Target 검색
2. 승인·Reason 연결
3. Hash/Chain 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.21 알림 — `/notifications`

**목적**  
업무 알림

**접근 계약**

- Route ID: `notifications`
- menuCode: `SETTING`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 알림 조회
2. 읽음·재전송
3. 전송 실패 확인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.22 첨부파일 — `/attachments`

**목적**  
첨부 업로드·검증

**접근 계약**

- Route ID: `attachments`
- menuCode: `ATTACHMENT`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 업로드
2. Checksum/Scan
3. 다운로드 Permission
4. 보존·파기

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.23 저장 검색 — `/savedSearches`

**목적**  
사용자 검색 조건

**접근 계약**

- Route ID: `savedSearches`
- menuCode: `SETTING`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 검색 조건 저장
2. 공유 범위
3. 민감 Filter 제거

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.24 업무 설정 — `/settings`

**목적**  
BZA 업무 설정

**접근 계약**

- Route ID: `settings`
- menuCode: `SETTING`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 설정 Version
2. Preview
3. 승인·게시
4. Consumer 적용

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.25 다운로드 정책 — `/downloads`

**목적**  
다운로드 정책

**접근 계약**

- Route ID: `downloads`
- menuCode: `SETTING`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 다운로드 정책
2. 마스킹·Column·Row 제한
3. 만료·승인

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증

### 5.26 다운로드 감사 — `/downloadAudits`

**목적**  
다운로드 감사

**접근 계약**

- Route ID: `downloadAudits`
- menuCode: `AUDIT`
- Group: `support`
- 최종 접근: Server Permission·Data Scope
- Browser Runtime 검증: `미검증`

**사용 절차**

1. 요청자·대상·Hash·만료
2. 다운로드·파기 이력
3. 이상 탐지

**조회·판단**

- 조직·사용자·Role의 현재·미래 유효기간
- 상태·Current Version
- Data Scope
- PII Masking
- 승인 Snapshot
- operationId·Audit
- 변경 영향 사용자·조직·결재 경로

**변경 전**

- 최신 Version 재조회
- 기간 중복·조직 순환·자기 승인 검증
- Reason·Ticket
- 필요 Approval
- 영향받는 Effective Permission Preview
- Session 재검증 계획

**정상 결과**

- List·Detail 상태와 Version 일치
- Effective Permission·결재 경로 재계산
- Audit Actor·Reason·Approval·Result
- Partial 대상 분리
- 민감정보 마스킹

**오류·복구**

- 401: Session 확인·재로그인
- 403: menuCode·Action·API·Data Scope 확인
- 409: 최신 Version·기간 충돌·동시 변경 확인
- 422: 필수값·기간·조직 순환·정책 문법 확인
- UNKNOWN_RESULT: 같은 operationId로 결과 조회
- Partial: 사용자·조직·Role별 적용 결과 분리
- 권한 변경 후 기존 Session 재검증


## 6. 권한·결재 상세

### 6.1 Permission

- Menu
- Action/Button
- API
- Data Scope
- 유효기간
- Deny/Allow 우선순위
- Role Version
- 사용자 Effective Permission

Frontend 표시만으로 권한을 확정하지 않는다.

### 6.2 Approval

- 정책 Version Snapshot
- ALL·ANY·N_OF_M
- 조직·Role·위임 해석
- 요청자·승인자 분리
- 만료
- Target Version 변경 시 재승인
- 멱등 상신
- 실행 결과 연결

### 6.3 위임

- 위임자·수임자
- 대상 정책·업무 범위
- 시작·종료
- 대결 여부
- 자기 승인·순환 방지
- 즉시 회수
- Audit

## 7. 조직·인사 정합성

- 조직 Parent 순환 차단
- 조직 폐쇄 전 하위 조직·재직자 확인
- 직원과 User 분리
- 다중 Assignment·주 소속
- 직급·직책 유효기간
- 조직 책임자·대행
- 미래 발령
- 권한·승인 경로 재계산
- 퇴직 후 Session·Role 회수

## 8. 첨부·다운로드

### 8.1 Attachment

- Stream Upload
- 크기·확장자·Content 검증
- Checksum
- Malware/Policy Scan
- 격리 상태
- Download Permission
- 보존·파기
- Audit

### 8.2 Download

- 정책 Version
- 대상 Column·Row·Data Scope
- Masking
- Reason·Approval
- Server-side Artifact
- SHA-256
- 만료
- 다운로드·파기 Audit

원문 Data를 Browser에서 조립해 Export하지 않는다.

## 9. 개발 확장

```text
BZA Page
→ Generated Client
→ BZA API
→ BZA Owner 또는 Domain Public Port
→ Operation Status
→ Audit
```

Route 추가:

1. Capability·Owner
2. menuCode·Permission·Data Scope
3. Backend Query/Command
4. OpenAPI
5. Orval
6. Route Registry
7. Page·Form·Table·Detail
8. Approval·Audit·Masking
9. Browser Test
10. Seed·Migration·Guide

BZA가 Domain DB를 직접 수정하지 않는다.

## 10. 운영·복구

Backup:

- 조직·직원·발령
- 사용자·Role·Permission
- Menu Registry
- Approval Policy·Instance·Action
- Session 정책 범위
- Audit
- Attachment Metadata
- Download Policy·Audit
- Config Manifest

장애:

- Bootstrap 재실행: 차단·Audit
- 조직 순환: 변경 Rollback
- 권한 과다: Session Revoke·Role 회수
- 결재 적체: 참여자·위임·Snapshot
- Attachment Scan 실패: 다운로드 차단
- Download 유출 의심: Artifact 파기·Security Incident
- DB 장애: 변경 차단·복구 후 대사

## 11. 완료 Gate

- 26개 Route가 Menu Catalog·Permission과 일치
- Bootstrap 1회성·만료·원자 Claim 검증
- 조직·권한·결재가 유효기간·Version·Audit 보유
- PII·Attachment·Download 정책
- Browser 3종·권한별 E2E
- Backup·Restore·Upgrade 실제 DB
