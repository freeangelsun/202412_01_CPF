# CPF Backoffice(MBW) Domain · Web BFF · Reference Frontend 상세 개발요건

> Canonical product identity: `cpf-backoffice` / `cpf-backoffice-web` / SystemCode `MBW` / logical DB `mbwDB`.  
> Revision: `2026-08-20 Current-Only`; Runtime PASS는 별도 Evidence로만 판정한다.  
> 신규 Canonical Requirement ID 추가: 0건  
> 강화 대상: `MBW-BUSINESS`, `MBW-ORG`, `MBW-APPROVAL` 및 관련 Security/Audit/Download 계약


## 0. 2026-08-19 Superseding BZA Target

이 절이 본 문서의 최우선 Current Target이다. 아래 과거 27-route embedded UI 상세는 **내부 `cpf-backoffice/online` Domain capability가 어떤 업무를 지원해야 하는지 추적하기 위한 historical capability mapping**으로만 보존하며, 외부 Reference Frontend의 활성 Route/IA 목표로 사용하지 않는다.

### 0.1 최종 구성

```text
Browser
  → cpf-backoffice-web/frontend            # 선택형 외부 Reference UI
  → cpf-backoffice-web             # DB-less Pure Spring Boot, CPF Java dependency 0
  → Gateway 또는 허용된 Direct Public HTTP
  → cpf-backoffice/online               # CPF 내부 Optional Prebuilt Business Administration Domain
  → 필요한 Business Domain Public/Invocation Contract
  → 각 Domain-owned DB
```

- `cpf-backoffice/online`은 MBW 업무규칙, 승인 상태, MBW 업무권한, Backoffice 설정 등 MBW-owned data의 Owner다. Member/Customer/Account 등의 Business Master를 복제하거나 해당 Domain DB를 직접 읽고 쓰지 않는다.
- `cpf-backoffice/online`은 Generator 생성 대상이 아니지만 Generated Business Domain과 동일한 Public Starter/API, Canonical System transaction/context, Domain Invocation, Security, Logging/Audit/Trace, DB3/Test 계약을 사용한다.
- BZA를 사용하지 않는 경우 `cpf-backoffice/online`과 MBW DB bundle을 물리적으로 제외해도 Root Build/Publication/Installer/Verifier와 필수 CPF Runtime이 정상이어야 한다.
- `cpf-backoffice-web`은 DB/JDBC/JPA/MyBatis와 CPF BOM/Starter/Java/Internal dependency를 두지 않는다. Session을 이유로 업무 상태/권한 원장을 영속 저장하지 않는다.
- `cpf-backoffice-web/frontend`는 Channel만 호출하고 대표 업무 흐름을 보여주는 소수 Reference Page를 제공한다. 현재 Canonical Reference Route는 Dashboard, Employee Search/Change, Approval Inbox/Decision, Role/Permission의 4개다.
- Direct Public HTTP는 Gateway 보안 우회가 아니다. authN/authZ/Channel Policy/Audit/Canonical System Header/Operation 계약을 동일하게 충족한다. 자동 Gateway→Direct fallback을 금지한다.

### 0.2 UI/Source 구조 품질

Backoffice Web BFF과 Frontend는 feature-first 구조를 사용한다. Channel은 기능별 API/controller가 공통 HTTP transport/protocol/routing을 재사용하고, Frontend는 기능별 `pages/components/api/model/composables`를 필요한 수준으로 분리한다. Controller/API/상태/모델을 한 통파일에 몰거나 모든 Controller를 하나의 공용 package에 집중시키지 않는다. 반대로 파일 크기만으로 의미 없이 쪼개지도 않는다.

### 0.3 완료 판정

- 내부 Domain OpenAPI/권한/감사/DB/Domain-call 계약과 외부 Channel Route가 일치한다.
- Channel routes와 Backend public operations가 drift하지 않는다.
- 외부 Reference UI는 Generated/OpenAPI-derived client의 실제 Consumer다.
- Channel `DB-less=1`, CPF Java dependency 0, receiver-owned `X-System-Code` 작성 0을 Gate로 검증한다.
- 외부 Frontend/Channel 및 내부 Backoffice(MBW) Domain 각각을 제거/미선택한 지원 구성에서 Optionality 계약을 검증한다.
- 실제 Browser/Gradle/DB Runtime을 실행하지 못하면 `미검증`으로 기록한다.

## 0A. Historical Embedded UI Audit — Capability Absorption Only

과거 Target CSV의 27 route와 embedded `cpf-backoffice/online/frontend`는 더 이상 active external UI target이 아니다. 해당 목록의 업무 의미는 내부 `cpf-backoffice/online` Domain capability/API/권한/감사 요구로 1:1 흡수하고, 외부 UI는 4개 대표 Reference 흐름만 제공한다. **과거 Route/Operation 존재를 현재 완료 근거로 사용하지 않는다.**

과거 embedded Navigation의 group/route 배치는 historical mapping이며 현재 외부 Reference Navigation의 Acceptance가 아니다.

1. Dashboard
2. 조직·인사
3. 사용자·권한
4. 승인·결재
5. 업무지원·공통
6. 감사·다운로드
7. 설정

27-route 업무 의미는 내부 Domain capability/API에 보존하고, 외부 Reference UI는 선택된 대표 흐름에서 Backend enforcement/Generated Client/Permission/Audit/Error UX를 검증한다.

Approval은 policy와 immutable execution snapshot을 분리하고 sequential/parallel, person/role/org, ALL/ANY/N_OF_M, delegation/proxy, withdraw/resubmit/expiry/concurrency를 실제 Backend + UI + E2E로 증명한다. 조직/인사/Role은 유효기간·겸직·임시발령·대행·위임·snapshot semantics를 포함한다.

현재 `route-quality.spec.ts`의 release-mode matrix 요구는 유지하되, 최신 exact SHA에서 real backend route/failure matrix + Desktop/Narrow + screenshot Evidence가 없으면 완료가 아니다.

## 1. 제품 역할

Backoffice(MBW)는 CPF Platform 자체를 운전하는 ADM이 아니라 **고객 업무 관리자용 Business Administration Console**이다.

ADM과 Backoffice(MBW)를 섞지 않는다.

- ADM: Runtime, Service, Transaction, Batch, Gateway, Recovery, Platform Security/Approval.
- Backoffice(MBW): 고객 조직, 직원/사용자, Role/Permission, 메뉴, 승인·결재, 업무공통, 첨부/다운로드, 업무감사.

BZA도 단순 CRUD 샘플이 아니라 고객 프로젝트에서 바로 확장·상용화 가능한 관리제품이어야 한다.

## 2. Canonical 핵심

### MBW-BUSINESS
고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·Download·Approval을 업무 Domain Public Contract로 수행한다.

### MBW-ORG
조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 Assignment, 겸직/파견/대행, Masked Profile, 결재 Snapshot을 제공한다.

### MBW-APPROVAL
순차/병렬/개인/Role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 Policy/Instance 분리를 제공한다.

## 3. Historical Embedded UI Source 기준

과거 embedded legacy BZA UI에는 27개 Route가 있었다. 현재 active external UI는 `cpf-backoffice-web/frontend`의 4개 Reference Route이며, 내부 `cpf-backoffice/online` Domain은 기존 업무 capability/API를 계속 소유한다. 아래 27-route 정보는 capability traceability를 위한 historical mapping이다.

## 4. 내부 Backoffice(MBW) Domain Capability IA (External Reference Route 수와 독립)

```text
Backoffice(MBW)
├─ 01 Dashboard
├─ 02 조직·인사
│  ├─ 조직
│  ├─ 직원
│  ├─ 직급
│  ├─ 직책
│  ├─ 발령·겸직
│  └─ 조직 책임
├─ 03 사용자·권한
│  ├─ 사용자
│  ├─ Role
│  ├─ 사용자 Role
│  ├─ 메뉴
│  ├─ Permission
│  ├─ 권한 분석
│  └─ Session
├─ 04 승인·결재
│  ├─ 결재 처리
│  ├─ 결재 상신
│  ├─ 결재 정책
│  ├─ 경로 Simulation
│  └─ 결재 위임
├─ 05 업무지원·공통
│  ├─ 알림
│  ├─ 첨부파일
│  ├─ 저장 검색
│  └─ Common Catalog
├─ 06 감사·다운로드
│  ├─ 업무 감사
│  ├─ 다운로드 정책
│  └─ 다운로드 감사
└─ 07 설정
```

이 명칭을 기계적으로 강제하는 것이 아니라 다음 원칙을 강제한다.

- 기술 Package/Feature 이름을 그대로 노출하지 않는다.
- 메뉴는 고객 업무관리자의 업무 흐름을 기준으로 배치한다.
- 유사 기능의 중복 메뉴를 제거한다.
- 조직→직원→Assignment→Role/Permission→승인 Owner의 연계가 끊기지 않는다.
- 권한관리 화면은 Role/Permission/User Role/Menu 권한을 한 업무영역으로 이해할 수 있어야 한다.
- 결재는 Policy→Simulation→Submission→Inbox→Decision→Audit 흐름이 이어져야 한다.
- 메뉴 노출권한과 Backend 권한을 분리하지 않는다.

## 5. 화면 설명 Requirement

모든 주요 화면 상단:

- 화면명
- 이 화면이 무엇을 관리하는지 1~2문장 설명
- 관리 Scope
- 데이터 기준시각
- 필요한 권한
- Help
- 관련 화면 Link

Help Panel:

- 주요 검색조건
- 상태 의미
- 유효기간 의미
- 변경 시 영향
- 승인/감사 여부
- Masking/Raw Access 정책
- 오류/충돌 해결방법

## 6. 조직·인사 특별 기준

### 조직
- Tree + List/Search를 함께 검토한다.
- 상위/하위 관계, 조직코드/명, 유효기간, 상태, 책임자.
- 조직 이동/폐쇄 시 하위조직·직원·권한·결재 Owner 영향 Preview.
- 대량 조직에서 Lazy Load/Virtualization/Paging 전략.
- Cycle/중복 Parent 방지.

### 직원
- 사번, 이름, 조직, 직급, 직책, 상태, 유효기간.
- 연락처/개인정보 Masking.
- Raw Contact Access는 별도 권한·사유·Audit.
- 직원 상세에서 Assignment, Role, 결재 참여정보로 Drill-down.

### 발령·겸직·파견·대행
- 시작/종료일.
- Primary/Secondary 또는 우선순위.
- 겹치는 기간 Conflict.
- 대행 범위.
- 조직 책임과 결재 Snapshot 영향.

### 조직 책임
- 조직장/대행/승인 Owner.
- 유효기간.
- 책임 중복/공백 검증.
- 결재 Snapshot과 실제 Policy Resolution 연결.

## 7. 사용자·권한 특별 기준

- User와 Employee가 동일 개념인지 분리 개념인지 UI에서 명확화.
- Role, Permission, Menu, Data Scope를 각각 구분한다.
- Effective Permission을 계산해 보여준다.
- Role 변경 전/후 Diff.
- Permission Simulation.
- 즉시 권한 회수와 Cache Invalidation 상태.
- 자기 자신의 최종 Admin 권한 제거/Lockout 방지.
- URL 직접 접근도 Server-side Enforcement.
- 유효기간 만료 Role 표현.

## 8. 승인·결재 특별 기준

결재는 단순 승인/반려 버튼이 아니다.

```text
Policy Version
→ Participant Resolution
→ Simulation
→ Submission
→ Snapshot
→ Sequential/Parallel Decision
→ ALL/ANY/N_OF_M
→ Delegation/Substitute
→ Withdraw/Cancel/Expire/Resubmit
→ Concurrent Decision
→ Result/Audit
```

필수:

- Policy와 Instance/Snapshot 분리.
- 조직/Role 변경 후 기존 결재건 Snapshot 불변.
- 자기승인 금지/SoD가 필요한 Policy는 명확히 표시.
- 중복 Click/동시 승인 멱등성.
- 만료/회수/재상신 상태.
- 위임/대결 유효기간 충돌.
- 승인/반려 사유.
- History Timeline.

## 9. Common Catalog

MBW Common Catalog는 **고객 업무 공통**을 관리한다.
Platform Runtime Config는 ADM으로 보낸다.

- Response Code
- Message / Locale
- 고객이 실제 확장 가능한 Common Catalog

필수:
- Search/Paging/Detail.
- Create/Update/Disable.
- Version/Effective state.
- Duplicate Validation.
- 사용처/Consumer.
- Runtime Refresh 결과.
- Audit.
- 409 Conflict.

## 10. Attachment/Download

- 파일명/MIME/Size/Hash/Scan state.
- Upload validation.
- Archive bomb/path traversal 방어.
- Download 권한.
- 민감정보 Masking/Encryption.
- Reason/Approval가 필요한 다운로드 구분.
- Row/Size Limit.
- Expiry/One-time link가 적용되는 기능 확인.
- Download Audit.

## 11. 공통 UI

List:
- Search, Advanced Filter, Reset, Saved Search.
- Server-side Paging/Sort.
- Total Count.
- Loading/Empty/Error/Partial.
- Column Show/Hide.
- 긴 ID Copy.
- Status Badge.

Form:
- Required.
- Inline Validation.
- Backend Validation mapping.
- Dirty state.
- 중복 Submit 방지.
- Conflict/Version 처리.
- 저장 전 Diff.

Detail:
- Summary.
- History.
- Related entities.
- Audit.
- Actions.

## 12. Historical Route별 Domain Capability Mapping

### `dashboard` — 대시보드

**목표 Menu Group:** 01 Dashboard  
**Canonical:** `MBW-BUSINESS`  
**현재 Menu Code:** `DASHBOARD`  
**기능 설명:** 고객 업무관리자가 조직·권한·승인·감사·업무지원 현황을 한눈에 보고 필요한 관리업무로 Drill-down하는 시작화면.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaSupportDashboard`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `organizations` — 조직

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `ORGANIZATION`  
**기능 설명:** 조직 계층을 Tree/List로 조회·검색하고 상하위 관계, 유효기간, 상태, 책임자, 변경이력을 관리.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaBackofficeFindOrganizations, bzaBackofficeSaveOrganization, bzaBackofficeFindOrganizationsPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `employees` — 직원

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `EMPLOYEE`  
**기능 설명:** 직원 Profile과 사번·소속·직급·직책·유효기간·연락처 Masking을 조회하고 조직·Role·Assignment와 연결.

**현재 OpenAPI 연결**
- Expected Operation: 4개
- `bzaBackofficeFindEmployees, bzaBackofficeSaveEmployee, bzaBackofficeFindEmployeesPage, bzaBackofficeEmployeeRawContact`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `positions` — 직급

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `EMPLOYEE`  
**기능 설명:** 직급 기준정보를 검색·등록·수정하고 유효기간·사용여부·정렬·참조영향을 관리.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaDirectoryFindPositions, bzaDirectorySavePosition, bzaDirectoryFindPositionsPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `jobTitles` — 직책

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `EMPLOYEE`  
**기능 설명:** 직책 기준정보를 검색·등록·수정하고 조직 책임·결재 Owner와의 연결영향을 확인.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaDirectoryFindJobTitles, bzaDirectorySaveJobTitle, bzaDirectoryFindJobTitlesPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `assignments` — 발령·겸직

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `EMPLOYEE`  
**기능 설명:** 겸직·파견·대행·다중소속 Assignment의 기간·우선순위·상태를 관리하고 충돌·중복을 검증.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaDirectoryFindAssignments, bzaDirectorySaveAssignment, bzaDirectoryFindAssignmentsPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `organizationResponsibilities` — 조직 책임

**목표 Menu Group:** 02 조직·인사  
**Canonical:** `MBW-ORG`  
**현재 Menu Code:** `ORGANIZATION`  
**기능 설명:** 조직장·대행·승인 Owner 등 조직 책임을 유효기간과 함께 관리하고 결재 Snapshot에 반영되는 경로를 검증.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaDirectoryFindResponsibilities, bzaDirectorySaveResponsibility, bzaDirectoryFindResponsibilitiesPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `users` — 사용자

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** BZA 인증 사용자를 검색·등록·상태관리하고 직원/조직/Role/Session과 연결.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaOperationFindAdminUsers, bzaOperationSaveAdminUser, bzaOperationFindAdminUsersPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `roles` — 역할

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** 업무 Role을 관리하고 Permission, User-Role, Menu 접근, Data Scope 영향도를 확인.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaOperationFindRoles, bzaOperationSaveRole, bzaOperationFindRolesPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `userRoles` — 사용자 Role

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** 사용자별 다중 Role과 유효기간을 관리하고 즉시권한반영/회수와 충돌을 검증.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaDirectoryFindUserRoles, bzaDirectorySaveUserRole, bzaDirectoryFindUserRolesPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `menus` — 메뉴

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** BZA 메뉴 Registry와 계층·표시순서·권한연결을 관리하되 내부 구현 Feature를 사용자 메뉴로 그대로 노출하지 않음.

**현재 OpenAPI 연결**
- Expected Operation: 5개
- `bzaOperationFindMenus, bzaOperationSaveMenu, bzaOperationFindMenusPage, bzaOperationDeleteMenu, bzaOperationFindMenuImpact`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `permissions` — 권한

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** 화면·행위·API·Data Scope Permission을 관리하고 Server-side Enforcement와 Frontend 표시를 일치시킴.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaOperationFindPermissions, bzaOperationSavePermission, bzaOperationFindPermissionsPage`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `permissionTools` — 권한 분석

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHZ`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** Role 비교, Effective Permission, 권한 Simulation으로 변경 전 실제 접근효과를 분석.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaBackofficeFindEffectivePermissions, bzaSupportCompareRolePermissions, bzaSupportSimulatePermission`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `approvalInbox` — 결재 처리

**목표 Menu Group:** 04 승인·결재  
**Canonical:** `MBW-APPROVAL`  
**현재 Menu Code:** `APPROVAL`  
**기능 설명:** 결재 참여자가 처리할 Inbox를 조회하고 승인/반려/대결/만료 상태와 Snapshot을 확인.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaApprovalInbox`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `approvalSubmissions` — 결재 상신

**목표 Menu Group:** 04 승인·결재  
**Canonical:** `MBW-APPROVAL`  
**현재 Menu Code:** `APPROVAL`  
**기능 설명:** 정책 기반 결재상신·취소·회수·재상신·만료·동시승인 처리를 멱등하게 수행.

**현재 OpenAPI 연결**
- Expected Operation: 8개
- `bzaApprovalSubmissions, bzaApprovalPolicySubmit, bzaApprovalExpireDue, bzaApprovalSubmissionDetail, bzaApprovalCancel, bzaApprovalResubmit, bzaApprovalWithdraw, bzaApprovalParticipantDecision`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `approvalPolicies` — 결재 정책

**목표 Menu Group:** 04 승인·결재  
**Canonical:** `MBW-APPROVAL`  
**현재 Menu Code:** `APPROVAL`  
**기능 설명:** Versioned 결재정책, 순차/병렬, 개인/Role/조직, ALL/ANY/N_OF_M, 유효기간, SoD를 관리.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaApprovalPolicies, bzaApprovalPolicySave, bzaApprovalPolicyDetail`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `approvalSimulation` — 경로 Simulation

**목표 Menu Group:** 04 승인·결재  
**Canonical:** `MBW-APPROVAL`  
**현재 Menu Code:** `APPROVAL`  
**기능 설명:** 정책 적용 전 조직·Role·위임·유효기간을 해석하여 예상 결재경로와 참여자를 시뮬레이션.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaApprovalPolicySimulate`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `approvalDelegations` — 결재 위임

**목표 Menu Group:** 04 승인·결재  
**Canonical:** `MBW-APPROVAL`  
**현재 Menu Code:** `APPROVAL`  
**기능 설명:** 결재 위임·대결의 유효기간·범위·위임자/수임자·충돌을 관리하고 Audit를 남김.

**현재 OpenAPI 연결**
- Expected Operation: 2개
- `bzaApprovalDelegations, bzaApprovalDelegationSave`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `sessions` — 내 세션

**목표 Menu Group:** 03 사용자·권한  
**Canonical:** `MBW-BUSINESS, SEC-AUTHN`  
**현재 Menu Code:** `AUTHORIZATION`  
**기능 설명:** 현재 사용자 Session을 조회하고 개별 Session Revoke를 제공하며 인증상태와 만료를 명확히 표시.

**현재 OpenAPI 연결**
- Expected Operation: 2개
- `bzaAuthSessions, bzaAuthRevokeSession`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `audits` — 업무 감사

**목표 Menu Group:** 06 감사·다운로드  
**Canonical:** `MBW-BUSINESS, SEC-AUDIT`  
**현재 Menu Code:** `AUDIT`  
**기능 설명:** MBW 업무 변경·승인·다운로드의 Immutable Audit를 조회하고 Actor/Target/Reason/Result/TransactionId를 추적.

**현재 OpenAPI 연결**
- Expected Operation: 2개
- `bzaBusinessAuditVerify, bzaBackofficeFindBusinessAudits`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `notifications` — 알림

**목표 Menu Group:** 05 업무지원·공통  
**Canonical:** `MBW-BUSINESS`  
**현재 Menu Code:** `SETTING`  
**기능 설명:** 업무 알림 목록·생성·읽음처리와 관련 업무대상 Drill-down을 제공.

**현재 OpenAPI 연결**
- Expected Operation: 4개
- `bzaSupportFindNotifications, bzaSupportCreateNotification, bzaSupportReadAllNotifications, bzaSupportReadNotification`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `attachments` — 첨부파일

**목표 Menu Group:** 05 업무지원·공통  
**Canonical:** `MBW-BUSINESS, SEC-APP`  
**현재 Menu Code:** `ATTACHMENT`  
**기능 설명:** 첨부 업로드/다운로드/재검사/보안상태를 관리하고 MIME·크기·악성파일·권한을 검증.

**현재 OpenAPI 연결**
- Expected Operation: 5개
- `bzaSupportFindAttachments, bzaSupportUploadAttachment, bzaSupportDownloadAttachment, bzaSupportRecheckAttachment, bzaSupportUpdateAttachmentSecurity`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `savedSearches` — 저장 검색

**목표 Menu Group:** 05 업무지원·공통  
**Canonical:** `MBW-BUSINESS`  
**현재 Menu Code:** `SETTING`  
**기능 설명:** 자주 쓰는 검색조건을 사용자별 저장·비활성화하고 각 목록화면에서 재사용.

**현재 OpenAPI 연결**
- Expected Operation: 3개
- `bzaSupportFindSavedSearches, bzaSupportSaveSavedSearch, bzaSupportDisableSavedSearch`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `settings` — 업무 설정

**목표 Menu Group:** 07 설정  
**Canonical:** `MBW-BUSINESS`  
**현재 Menu Code:** `SETTING`  
**기능 설명:** MBW 업무설정의 Scope·현재값·변경가능여부를 조회하고 설정 Owner와 연결.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaOperationFindSettings`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `commonCatalog` — 공통 코드·메시지

**목표 Menu Group:** 05 업무지원·공통  
**Canonical:** `MBW-BUSINESS`  
**현재 Menu Code:** `SETTING`  
**기능 설명:** 고객 업무에서 사용하는 Common 응답코드·다국어 메시지를 검색·등록·수정·비활성화하고 Runtime Refresh를 추적.

**현재 OpenAPI 연결**
- Expected Operation: 11개
- `bzaCommonResponseCodeSearch, bzaCommonResponseCodeDetail, bzaCommonResponseCodeCreate, bzaCommonResponseCodeUpdate, bzaCommonResponseCodeDisable, bzaCommonMessageSearch, bzaCommonMessageDetail, bzaCommonMessageCreate …`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `downloads` — 다운로드 정책

**목표 Menu Group:** 06 감사·다운로드  
**Canonical:** `MBW-BUSINESS, SEC-DOWNLOAD`  
**현재 Menu Code:** `SETTING`  
**기능 설명:** 다운로드 정책을 조회하고 민감정보·대량다운로드의 권한·사유·승인·제한을 표현.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaOperationFindDownloadPolicies`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`
### `downloadAudits` — 다운로드 감사

**목표 Menu Group:** 06 감사·다운로드  
**Canonical:** `MBW-BUSINESS, SEC-DOWNLOAD, SEC-AUDIT`  
**현재 Menu Code:** `AUDIT`  
**기능 설명:** 누가 어떤 조건으로 무엇을 다운로드했는지 검색하고 Row/Size/Masking/Result를 Audit.

**현재 OpenAPI 연결**
- Expected Operation: 1개
- `bzaSupportFindDownloadAudits`

**필수 UI/UX**
- 화면 상단에 사용자 관점 기능 설명, 관리 대상 Scope, 마지막 갱신시각, Help를 제공한다.
- 검색/Filter/초기화/Paging/Sort/Total/Loading/Empty/Error를 업무 데이터 특성에 맞게 제공한다.
- 상세 화면은 조직·사용자·Role·Permission·승인·Audit 등 연관 대상 링크를 제공한다.
- 저장 전 변경 Diff와 유효기간·중복·충돌 Validation을 제공한다.
- 권한 없는 기능은 메뉴/버튼/Route에서 정책에 맞게 처리하면서 Backend 403 Enforcement도 반드시 존재해야 한다.
- 401/403/404/409/429/500/503을 구분한다.
- 개인정보/연락처/첨부/다운로드는 Masking과 Raw Access 권한을 구분한다.
- 위험 변경은 Reason/Confirm/필요 시 Approval/Audit를 제공한다.
- Generated OpenAPI Client가 실제 Consumer여야 하며 Mock/Fixture/local-only state로 기능 완료 처리하지 않는다.
- Chromium/Firefox/WebKit 또는 가능한 Browser E2E와 Desktop/Narrow Screenshot Evidence를 남긴다.

**완료 연결**
`Requirement → 업무 Menu → Route → Page → Generated Client → Backend → BZA_DB/Owner Runtime → Permission/Audit → E2E`

## 13. External Reference Frontend Browser/E2E

현재 외부 `cpf-backoffice-web/frontend`의 4개 Reference Route와 선택된 핵심 정상/실패 흐름을 실제 Browser로 검증한다. 내부 `cpf-backoffice/online`의 전체 capability는 Domain/API/권한/DB Test로 별도 검증한다.

- Normal.
- Empty.
- 401/403/404/409/429/500/503.
- Permission 차이.
- Long data.
- Large list.
- Desktop/Narrow.
- Form Validation.
- Approval concurrency.
- Download/Attachment security.

Screenshot은 Evidence일 뿐 자동 PASS가 아니다. 사람이 읽을 수 있는 메뉴/표/폼/오류 상태인지 직접 판정한다.

## 14. 완료조건

0건 목표:

- 내부 Backoffice(MBW) Domain capability에 API/권한/감사/Test Evidence가 없거나, 외부 Reference 대상으로 선택된 기능에 실제 UI Consumer Evidence가 없음.
- Reference Menu→Route→Page→Channel→Domain 연결 끊김.
- Generated Client Consumer 없음.
- Mock-only/Fixture-only.
- 조직/권한/승인 Snapshot 미구현.
- 권한 없는 위험기능.
- Audit 없는 변경.
- 기능 설명/Help 없는 주요 화면.
- Browser 미검증을 PASS 처리.
