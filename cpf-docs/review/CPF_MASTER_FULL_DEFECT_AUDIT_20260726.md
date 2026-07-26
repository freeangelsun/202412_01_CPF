# CPF 최신 master 전수 결함·결함 의심 및 필수 제품 기능 Gap 조사

- **Repository**: `freeangelsun/202412_01_CPF`
- **Branch**: `master`
- **기준 Commit**: `56b165513f73f0548d41d2d52197abcdf69a0d14`
- **검수일**: `2026-07-26`
- **결함·결함 의심·부분 구현·미검증 등록**: **289건**
- **별도 필수 제품 기능 Gap**: **12건**

> 최신 master의 실제 Source, Frontend, API, Service, SQL/Migration, Build/Release Script를 GitHub에서 직접 읽어 대조한 정적 전수검수다. 전체 Gradle, npm, DB, Browser, Multi-instance Runtime은 이 환경에서 직접 실행하지 못했으므로 실행이 필요한 내용은 `미검증` 또는 `재확인 필요`로 표시했다. Source만으로 확정 가능한 항목만 `실패`로 판정했다.

## 1. 집계

### 상태

| 상태 | 건수 |
|---|---:|
| 실패 | 63 |
| 부분 구현 | 169 |
| 미구현 | 21 |
| 미검증 | 5 |
| 재확인 필요 | 31 |

### 우선도

| 우선도 | 건수 |
|---|---:|
| P0 | 92 |
| P1 | 162 |
| P2 | 35 |

### 영역

| 영역 | 건수 |
|---|---:|
| A. ADM/BZA Frontend·UI | 62 |
| B. Build·Release·Deploy·Supply Chain | 31 |
| C. Contract·Architecture·Ownership | 22 |
| D. Security·Authentication·Header | 33 |
| E. Observability·Feature Flag·Fault Injection | 29 |
| F. Cache·다중 인스턴스 일관성 | 24 |
| G. MBR·ADM·Audit·DB | 52 |
| H. Batch·Calendar·Service Call·File | 36 |

## 2. 즉시 먼저 닫아야 할 P0 후보

1. **ADM LogsPage Route 대상 누락** — `cpf-admin/frontend/src/app/routes.ts` / 재확인 필요: Vite build 또는 거래로그 메뉴 진입 실패 가능
2. **Vue SFC 종료 태그 중복** — `cpf-admin/frontend/src/features/log-policies/LogPoliciesPage.vue` / 실패: Vue compiler parse/build 실패
3. **Vue SFC 종료 태그 중복** — `cpf-admin/frontend/src/features/response-codes/ResponseCodesPage.vue` / 실패: 응답코드 화면 또는 전체 build 실패
4. **Vue SFC 종료 태그 중복** — `cpf-admin/frontend/src/features/codes/CodesPage.vue` / 실패: 코드 화면 또는 전체 build 실패
5. **Vue SFC 종료 태그 중복** — `cpf-admin/frontend/src/features/security/SecurityPage.vue` / 실패: 보안 화면 또는 전체 build 실패
6. **최신 SHA Frontend CI Evidence 부재** — `GitHub CI / ADM·BZA frontend` / 미검증: 문법 오류가 master에 남아도 Push가 성공
7. **ADM API False Success** — `cpf-admin/frontend/src/app/methods/referenceMethods.ts` / 실패: 403/409/500 뒤 성공 Toast가 표시될 수 있음
8. **부분 실패 결과 타입 불일치** — `cpf-admin/frontend/src/features/core/methods.ts` / 실패: 렌더링 TypeError 또는 장애 0건 위장
9. **로그아웃 후 민감 상태 잔존** — `cpf-admin/frontend/src/state/admSharedState.ts` / 실패: 다른 운영자에게 이전 조회 데이터 노출 가능
10. **ADM token localStorage 저장** — `cpf-admin/frontend/src/state/createAdmState.ts` / 재확인 필요: XSS와 Browser 종료 후 세션 잔존 위험
11. **Clipboard/local Blob 반출이 서버 audit를 거치지 않는다.** — `ADM 로그 상세 Copy/JSON` / 부분 구현: Copy/JSON도 권한·사유·audit 적용
12. **Drain/Disable/Resume에 권한·대상 Preview·재확인 부족** — `ADM MaintenancePage` / 실패: 오조작 가능
13. **위험 Command 대상 Preview·멱등·재확인 부족** — `ADM BatchPage` / 부분 구현: 중복·오조작 위험
14. **encrypted 값 일반 Text/Raw JSON** — `ADM ConfigsPage` / 부분 구현: Secret 원문 노출 가능
15. **다운로드 전용 권한·민감 승인 부족** — `ADM DownloadsPage` / 부분 구현: 대량/민감 반출 통제 부족
16. **상태·권한 기반 버튼 통제 부족** — `ADM ApprovalsPage` / 부분 구현: 승인 흐름 오조작
17. **동시 401 refresh rotation 경합** — `BZA auth/session.ts` / 재확인 필요: 첫 요청 외 실패·세션 불안정
18. **Refresh 실패 후 세션 미정리** — `BZA auth/session.ts` / 부분 구현: stale 인증 상태 잔존
19. **등록·수정 모두 POST** — `BZA CrudTable.vue` / 재확인 필요: 신규 생성/덮어쓰기/권한 오판
20. **전체 결재 목록을 Inbox로 사용** — `BZA ApprovalInboxPage` / 재확인 필요: 자신의 결재가 아닌 건도 처리 가능해 보임
21. **결정 전 상세 부족** — `BZA ApprovalInboxPage` / 부분 구현: 근거 없이 승인/반려
22. **Dependency locking/verification 부재** — `Gradle dependency resolution` / 미구현: 재현성·공급망 무결성 약화
23. **dirty/branch/tag/승인 Guard 부재** — `prepareRelease tasks` / 부분 구현: clean tree/protected branch/signed tag 조건
24. **NOASSERTION을 채우고 존재만 확인** — `SBOM license` / 실패: allow/deny/review와 승인 예외
25. **ADM/BZA npm dependency 미포함** — `Frontend SBOM` / 미구현: package-lock 기반 SBOM 통합
26. **SBOM 후 CVE Gate 없음** — `Vulnerability scan` / 미구현: OSV/Dependency-Track/Grype gate
27. **일부 Build 파일만 material로 기록** — `provenance inputs` / 부분 구현: source tree/material digest와 builder identity
28. **NOT_SIGNED도 validation 통과** — `Release signature` / 미구현: keyless/key signing과 attestation 필수
29. **dirty를 기록만 하고 차단하지 않음** — `dirty build` / 부분 구현: RC/final에서 금지
30. **prod inventory 없으면 template fallback** — `cpfValidateDeployInventory` / 실패: prod는 승인 inventory 없으면 실패
31. **method/path/status/media/header/query/security 미모델링** — `REST contract` / 부분 구현: OpenAPI operation diff
32. **partition/order/delivery/schema evolution 부족** — `EVENT contract` / 부분 구현: Event envelope/key/delivery model
33. **전체 byte length/overlap/gap/padding/charset 부족** — `FIXED_LENGTH contract` / 부분 구현: byte layout validator
34. **delimiter/header/quote/compression/checksum 부족** — `FILE contract` / 부분 구현: File profile contract
35. **required/default/identity/restart semantics 부족** — `BATCH contract` / 부분 구현: Batch parameter/restart model
36. **환경별 Consumer/Provider Registry와 can-deploy 없음** — `Contract Registry` / 미구현: Registry+environment matrix+CI API
37. **Legacy backoffice 상태전이와 신규 approval engine 공존** — `BZA approval services` / 재확인 필요: 정본 Owner 하나로 통합
38. **강제 비밀번호 변경 Backend 차단 없음** — `BzaAuthService` / 실패: UI 우회 API 사용 가능
39. **비밀번호 만료 Backend 집행 없음** — `BzaAuthService` / 실패: 만료 계정 업무 수행 가능
40. **Refresh만 폐기하고 access token은 TTL까지 유효** — `BZA logout/password change` / 부분 구현: jti/session version/introspection

## A. ADM/BZA Frontend·UI

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | 재확인 필요 | P0 | 결함 의심 | `cpf-admin/frontend/src/app/routes.ts` | **ADM LogsPage Route 대상 누락** | Route가 ../features/logs/LogsPage.vue를 import하나 기준 Commit에서 해당 경로 조회가 404였다. | Vite build 또는 거래로그 메뉴 진입 실패 가능 | 실제 파일 정본을 복원하거나 Route를 수정하고 Route resolve gate 추가 | npm run build와 모든 Route import |
| 2 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/features/log-policies/LogPoliciesPage.vue` | **Vue SFC 종료 태그 중복** | 닫는 </template>가 중복된다. | Vue compiler parse/build 실패 | 중복 태그 제거와 전체 .vue parser gate | frontendTypecheck/frontendBuild |
| 3 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/features/response-codes/ResponseCodesPage.vue` | **Vue SFC 종료 태그 중복** | 닫는 </template>가 중복된다. | 응답코드 화면 또는 전체 build 실패 | SFC 문법 수정 | frontendBuild |
| 4 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/features/codes/CodesPage.vue` | **Vue SFC 종료 태그 중복** | 닫는 </template>가 중복된다. | 코드 화면 또는 전체 build 실패 | SFC 문법 수정 | frontendBuild |
| 5 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/features/security/SecurityPage.vue` | **Vue SFC 종료 태그 중복** | 닫는 </template>가 중복된다. | 보안 화면 또는 전체 build 실패 | SFC 문법 수정 | frontendBuild |
| 6 | 미검증 | P0 | 검증 Gap | `GitHub CI / ADM·BZA frontend` | **최신 SHA Frontend CI Evidence 부재** | package.json/Gradle task는 있으나 최신 Commit Workflow run/check가 확인되지 않았다. | 문법 오류가 master에 남아도 Push가 성공 | 두 Frontend verify를 필수 CI/Release Gate로 연결 | latest SHA lint/typecheck/test/build 로그 |
| 7 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/app/methods/referenceMethods.ts` | **ADM API False Success** | parseResponse가 비정상 HTTP를 메시지만 남기고 예외를 던지지 않는다. | 403/409/500 뒤 성공 Toast가 표시될 수 있음 | 비정상 HTTP typed exception과 성공 계약 검증 | mutation별 400/401/403/409/500 E2E |
| 8 | 실패 | P0 | 확정 결함 | `cpf-admin/frontend/src/features/core/methods.ts` | **부분 실패 결과 타입 불일치** | settledValue 실패값은 객체인데 화면은 배열로 간주한다. | 렌더링 TypeError 또는 장애 0건 위장 | SectionResult 공통 타입 도입 | Section별 timeout/500 |
| 9 | 실패 | P1 | 확정 결함 | `cpf-admin/frontend/src/App.vue` | **Header 상태 ONLINE 고정** | 실제 readiness와 무관하게 ONLINE을 표시한다. | 장애 중 정상처럼 보임 | readiness/liveness/dependency 상태와 마지막 성공시각 표시 | DB/Owner down Browser |
| 10 | 실패 | P0 | 강한 결함 의심 | `cpf-admin/frontend/src/state/admSharedState.ts` | **로그아웃 후 민감 상태 잔존** | clearToken은 token/operator/menu만 지우고 로그·감사·보안 결과는 공유 상태에 남는다. | 다른 운영자에게 이전 조회 데이터 노출 가능 | logout 시 전체 상태 reset과 polling/secret 제거 | 사용자 A→logout→B E2E |
| 11 | 부분 구현 | P1 | 결함 의심 | `cpf-admin/frontend/src/app/methods/accessMethods.ts` | **Logout API 실패 시 local token 잔존** | 서버 요청 성공 뒤에만 clearToken을 호출한다. | 네트워크 장애 시 Browser 세션이 남음 | local 정리는 finally에서 수행 | logout timeout/500 |
| 12 | 재확인 필요 | P0 | 보안 결함 의심 | `cpf-admin/frontend/src/state/createAdmState.ts` | **ADM token localStorage 저장** | access token을 localStorage에 저장한다. | XSS와 Browser 종료 후 세션 잔존 위험 | HttpOnly cookie 또는 memory/session token과 timeout 적용 | Threat model/XSS/session test |
| 13 | 실패 | P1 | 확정 결함 | `cpf-admin/frontend/src/shared/cpfApi.ts` | **Transaction ID 규격 분열** | 신규 admApi는 OADM-UI-epoch, 기존 client는 34자리 CPF ID를 사용한다. | 추적·Header 검증·감사 상관관계 불일치 | 공통 Browser transaction client 통합 | 모든 UI API ID 형식 |
| 14 | 부분 구현 | P1 | 구조 결함 | `cpf-admin/frontend/src/features/core/methods.ts` | **로그인 직후 전체 API 과호출** | 선택 메뉴·권한과 무관하게 다수 API를 동시에 호출한다. | 초기 부하·403·민감 데이터 메모리 적재 | Dashboard summary와 메뉴 lazy load 분리 | Role별 Network waterfall |
| 15 | 부분 구현 | P2 | 기능 불일치 | `ADM 메뉴 Registry/Frontend Route` | **DB 메뉴 등록과 Source component route가 연결되지 않는다.** | 등록한 메뉴를 실제 사용할 수 없음 | 관리 범위를 제한하거나 Plugin Manifest 제공 | 메뉴 등록 후 Route E2E | Source 수정 후 Unit/Integration/Runtime Evidence |
| 16 | 부분 구현 | P1 | 권한 결함 의심 | `ADM logs/transactionGroups` | **서로 다른 민감 기능이 동일 LOG_LIST 권한을 공유한다.** | 원문 로그와 거래 추적을 별도 통제 못함 | Menu/Button/API 권한 분리 | Role 권한 Matrix | Source 수정 후 Unit/Integration/Runtime Evidence |
| 17 | 부분 구현 | P2 | 사용성 결함 | `ADM uiMessage` | **모든 비동기 작업이 하나의 전역 메시지를 덮어쓴다.** | 중요 오류가 뒤의 성공 메시지에 가려짐 | Section error와 command toast 분리 | 동시 성공/실패 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 18 | 부분 구현 | P2 | 동시성 결함 의심 | `ADM/BZA fetch` | **AbortController/request sequence가 없다.** | 늦은 이전 응답이 최신 검색 결과를 덮음 | 취소·query key·stale discard 적용 | 응답 순서 역전 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 19 | 실패 | P1 | 확정 결함 | `ADM Dashboard` | **조회 실패와 정상 0건 미구분** | 실패 Section도 length 기반 KPI로 계산한다. | 장애가 0건으로 위장 | AVAILABLE/EMPTY/FAILED/STALE 표시 | dependency별 장애 |
| 20 | 부분 구현 | P2 | 기능 미흡 | `ADM TopologyPage` | **Health 데이터 미표시** | health를 가져오지만 instance status 중심으로만 표시한다. | readiness 원인 파악 불가 | health reason/heartbeat/dependency 표시 | degraded/down |
| 21 | 부분 구현 | P1 | 기능 오표기 | `ADM CapacityPage` | **SLO 화면이 최근 평균만 제공** | P95/P99·기간·error budget 없이 SLO로 명명한다. | 운영자 오판 | Snapshot으로 명칭 변경 또는 실제 SLO 구현 | 기간/P95/P99 |
| 22 | 실패 | P1 | 확정 결함 | `ADM TransactionGroupsPage` | **가짜 Client Pagination** | 서버 limit 결과를 다시 slice한다. | limit 이후 데이터 접근 불가 | server cursor/page 계약 | 100건 이상 paging |
| 23 | 부분 구현 | P1 | 복구 결함 | `ADM 거래그룹 상세` | **5개 API를 Promise.all로 묶는다.** | 한 Section 장애로 전체 상세 실패 | Tab별 partial success/retry | Section별 500 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 24 | 부분 구현 | P0 | 보안 결함 의심 | `ADM 로그 상세 Copy/JSON` | **Clipboard/local Blob 반출이 서버 audit를 거치지 않는다.** | 민감 로그 반출 통제 우회 | Copy/JSON도 권한·사유·audit 적용 | 민감 필드 반출 감사 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 25 | 부분 구현 | P2 | 자원 누수 | `ADM 원격 로그 ZIP` | **60초 polling이 route/logout에서 취소되지 않는다.** | 불필요 요청과 logout 후 상태 갱신 | poll controller/backoff/unmount 취소 | route change/logout | Source 수정 후 Unit/Integration/Runtime Evidence |
| 26 | 실패 | P0 | 운영 안전 | `ADM MaintenancePage` | **Drain/Disable/Resume에 권한·대상 Preview·재확인 부족** | 자유 입력 대상에 위험 명령을 즉시 실행한다. | 오조작 가능 | Registry 선택·영향 Preview·재확인·승인 | 권한/오조작 E2E |
| 27 | 실패 | P1 | 상태전이 | `ADM IncidentsPage` | **상태와 무관한 전이 버튼** | 인지·완화·복구가 항상 동시에 노출된다. | 불가능 전이와 혼란 | 서버 availableActions 사용 | 상태전이 Matrix |
| 28 | 부분 구현 | P1 | 기능 미완성 | `ADM RecoveryCenterPage` | **조회와 실제 복구 조치 분리** | Unknown/DLQ 목록에서 상세·대사·replay로 이동할 수 없다. | 복구 시간이 길고 ID 재입력 오류 | 상세 drawer와 deep link | Unknown/DLQ E2E |
| 29 | 부분 구현 | P1 | 오조작 | `ADM ReliabilityPage` | **복구 대상 ID 수기 입력** | row 선택 없이 messageId/unknownId를 입력한다. | 잘못된 거래 조치 가능 | row 선택·최신 상태·승인 gate | 잘못된 ID/상태 |
| 30 | 부분 구현 | P1 | 부분 장애 | `ADM Batch loadBatch` | **12개 API 전체 성공 요구** | 하나의 Promise.all로 모든 영역을 묶는다. | 관련 없는 장애가 전체 화면 실패 | Section별 독립 상태 | API별 단독 장애 |
| 31 | 실패 | P1 | 확정 결함 | `ADM batchMethods` | **부분 조회가 batchResult 전체 교체** | Worker/Lock/Step 조회가 다른 영역을 삭제한다. | 화면 데이터 소실 | Section merge | 조회 순서 조합 |
| 32 | 실패 | P1 | 확정 결함 | `ADM Center-Cut methods` | **부분 조회가 centerCutResult 전체 교체** | Target/Result/Detail이 Summary/Jobs를 덮어쓴다. | 화면 정보 소실 | Section merge | Target→Result→Detail |
| 33 | 부분 구현 | P2 | 과도한 결합 | `ADM WorkersPage` | **Worker 새로고침이 Batch 전체 호출** | loadBatch 전체를 실행한다. | Calendar 장애로 Worker 화면 실패 | Worker 전용 query | Calendar down |
| 34 | 부분 구현 | P0 | 운영 안전 | `ADM BatchPage` | **위험 Command 대상 Preview·멱등·재확인 부족** | Run/Retry/Stop/Lock/Ghost가 자유 입력과 단일 버튼이다. | 중복·오조작 위험 | 공통 위험 Command Dialog | double click/상태 |
| 35 | 부분 구현 | P0 | 보안 결함 의심 | `ADM ConfigsPage` | **encrypted 값 일반 Text/Raw JSON** | encryptedYn이 있어도 configValue가 일반 input/result에 표시된다. | Secret 원문 노출 가능 | write-only secret reference와 masked read | 암호화 설정 조회 |
| 36 | 부분 구현 | P0 | 보안/권한 | `ADM DownloadsPage` | **다운로드 전용 권한·민감 승인 부족** | 예상 건수·민감 mode·비동기 전환 없이 다운로드한다. | 대량/민감 반출 통제 부족 | 권한·preview·approval·masking | 권한/대량/민감 |
| 37 | 부분 구현 | P0 | 운영 안전 | `ADM ApprovalsPage` | **상태·권한 기반 버튼 통제 부족** | 요청/결정/실행이 available action 없이 노출된다. | 승인 흐름 오조작 | 서버 actions·payload diff/hash·역할 분리 | 요청자/승인자/실행자 |
| 38 | 부분 구현 | P2 | 오조작 | `ADM LogPoliciesPage` | **Override ID prompt 입력** | 목록 선택 없이 ID를 입력한다. | 다른 override 중지 가능 | row 선택 상세 Dialog | 잘못된 ID |
| 39 | 재확인 필요 | P1 | 동시성 결함 의심 | `ADM BusinessCalendarPage` | **Key 변경 후 이전 Version 잔존** | row 선택 뒤 calendar/date 변경 시 version reset이 없다. | 다른 key에 이전 expectedVersion 전송 | 선택 key/form key 분리 | 선택→date 변경→save |
| 40 | 부분 구현 | P1 | 오류 처리 | `BZA App navigateFromHash` | **동적 import 실패 catch 없음** | Chunk 404 시 빈 화면 | route error boundary/retry | chunk 404 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 41 | 재확인 필요 | P0 | 동시성 결함 의심 | `BZA auth/session.ts` | **동시 401 refresh rotation 경합** | 각 요청이 같은 refresh token을 독립 회전한다. | 첫 요청 외 실패·세션 불안정 | single-flight refresh queue | 10개 동시 401 |
| 42 | 부분 구현 | P0 | 인증 결함 | `BZA auth/session.ts` | **Refresh 실패 후 세션 미정리** | 최종 refresh 오류에도 clearBzaSession이 없다. | stale 인증 상태 잔존 | 401 final 시 clear/redirect | revoked refresh |
| 43 | 부분 구현 | P2 | 사용성/보안 | `BZA 비밀번호 UI` | **성공 안내와 비밀번호 clear 불완전** | 성공 메시지가 닫힌 modal state에 남는다. | 사용자 혼란과 값 메모리 잔존 | login message 전달/즉시 reset | 성공/실패 |
| 44 | 부분 구현 | P1 | 부분 장애 | `BZA DashboardPage` | **한 Section 장애가 전체 실패** | summary/approval/org/employee를 Promise.all로 묶는다. | 권한/한 API 장애로 전체 dashboard 실패 | Section별 partial state | 각 API 403/500 |
| 45 | 부분 구현 | P2 | 기능 미흡 | `BZA OrganizationsPage` | **조직 Tree 2단계 제한** | root와 직접 child만 렌더링한다. | 다단계 조직 탐색 불가 | recursive tree/search/breadcrumb | 4단계 조직 |
| 46 | 실패 | P1 | 확정 결함 | `BZA CrudTable.vue` | **조회 오류 미표시** | load에 catch/error state가 없다. | 이전/빈 데이터가 정상처럼 보임 | typed error/stale 상태 | GET 500 |
| 47 | 재확인 필요 | P0 | 계약 결함 의심 | `BZA CrudTable.vue` | **등록·수정 모두 POST** | 기존 row 수정도 같은 endpoint POST다. | 신규 생성/덮어쓰기/권한 오판 | create/update method·ID·version 분리 | CRUD contract |
| 48 | 재확인 필요 | P0 | 권한 결함 의심 | `BZA ApprovalInboxPage` | **전체 결재 목록을 Inbox로 사용** | 진행 건 모두에 처리 버튼을 노출한다. | 자신의 결재가 아닌 건도 처리 가능해 보임 | my-inbox API와 availableActions | 타 결재자 계정 |
| 49 | 부분 구현 | P0 | 업무 안전 | `BZA ApprovalInboxPage` | **결정 전 상세 부족** | payload·policy·결재선·history·첨부를 보지 못한다. | 근거 없이 승인/반려 | 상세 drawer/snapshot/hash | 첨부·정책 |
| 50 | 부분 구현 | P1 | 중복 처리 | `BZA ApprovalInboxPage` | **Button busy와 idempotency 고정 부재** | 클릭마다 새 UUID를 만든다. | 더블클릭이 다른 요청으로 전송 | submit disable/한 action 한 key | slow double click |
| 51 | 부분 구현 | P2 | 표시 결함 | `BZA EndpointList.vue` | **첫 Row Key만 Column 사용** | Object.keys(rows[0])로 열을 결정한다. | 뒤 row 필드 누락 | 명시 schema/union columns | heterogeneous rows |
| 52 | 부분 구현 | P1 | 기능 미완성 | `BZA Audits/Settings/Downloads` | **Raw API Viewer 수준** | 검색·paging·상세·마스킹·변경 기능이 없다. | 실제 운영 화면으로 사용 곤란 | 기능별 전용 화면 | 업무 운영 E2E |
| 53 | 부분 구현 | P1 | 업무 안전 | `BZA ApprovalPoliciesPage` | **Step Raw JSON 편집** | JSON.parse 외 업무 검증이 없다. | 잘못된 결재 정책 생성 | Visual builder/schema/simulation/diff | ALL/ANY/N_OF_M |
| 54 | 부분 구현 | P1 | 시간 계약 | `BZA 정책/Simulation/위임` | **Text 시각과 UTC/local 혼용** | 운영자 의도와 다른 유효기간 가능 | timezone·local input·UTC preview | Asia/Seoul | Source 수정 후 Unit/Integration/Runtime Evidence |
| 55 | 부분 구현 | P1 | 기능 미완성 | `BZA ApprovalDelegationsPage` | **위임 종료·취소·수정 없음** | 잘못된 위임을 즉시 중지 못함 | disable/shorten/replace/overlap preview | 겹침·취소 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 56 | 부분 구현 | P2 | 오류 처리 | `BZA SessionsPage` | **prompt와 uncaught revoke 오류** | 실패가 명확히 표시되지 않는다. | 세션 상세 Dialog/typed error | 404/409/500 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 57 | 재확인 필요 | P1 | 권한 정합성 의심 | `BZA Route/Button code` | **Route와 화면 Button이 다른 menu code를 사용한다.** | 화면/버튼 권한 불일치 가능 | 단일 Permission Manifest | Seed×Role×Route | Source 수정 후 Unit/Integration/Runtime Evidence |
| 58 | 부분 구현 | P2 | 기능 불일치 | `BZA DB menu/Source Route` | **DB route 변경과 component가 연결되지 않는다.** | 관리 기능이 실제 동작에 반영되지 않음 | 관리 범위 제한/Manifest | menu update | Source 수정 후 Unit/Integration/Runtime Evidence |
| 59 | 부분 구현 | P2 | 접근성 | `ADM/BZA dialog` | **showModal/focus trap 대신 :open 사용** | 배경 조작·focus restore 불안정 | 공통 accessible modal | keyboard/screen reader | Source 수정 후 Unit/Integration/Runtime Evidence |
| 60 | 부분 구현 | P2 | 접근성 | `ADM/BZA clickable rows` | **tr @click에 keyboard 접근 없음** | 키보드로 상세 열기 불가 | 명시 button/link | keyboard-only | Source 수정 후 Unit/Integration/Runtime Evidence |
| 61 | 재확인 필요 | P2 | 표시 결함 의심 | `BZA SequenceSamplePage` | **객체 직접 interpolation** | {{ result }}로 표시한다. | [object Object] 가능 | pretty JSON/구조 table | 규칙/이력 |
| 62 | 부분 구현 | P2 | 기능 미완성 | `BZA SavedSearchesPage` | **Raw JSON과 실제 화면 필터 미연결** | 수동 criteriaJson 저장만 보인다. | 저장 검색 실사용 불가 | 화면 schema 기반 capture/apply | 저장→복원 |
## B. Build·Release·Deploy·Supply Chain

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 63 | 부분 구현 | P1 | 구조 결함 | `root build.gradle` | **모든 Subproject에 5개 DB Driver 일괄 주입** | 비DB/Library Module에도 모든 Vendor driver가 runtime에 들어간다. | Artifact 비대화·CVE·License·classpath 위험 | 필요 실행 Module/Vendor profile에만 포함 | Artifact lib/SBOM |
| 64 | 미구현 | P0 | 공급망 Gap | `Gradle dependency resolution` | **Dependency locking/verification 부재** | transitive drift와 coordinate 변조를 차단하는 정본이 확인되지 않는다. | 재현성·공급망 무결성 약화 | locking과 checksum verification 정본화 | offline rebuild/checksum mismatch |
| 65 | 재확인 필요 | P1 | 증분 Build 의심 | `root processResources` | **생성 metadata input 추적 불충분** | root version 파일 변경이 task input으로 명시되지 않는다. | incremental/cache에서 이전 version 잔존 가능 | inputs.file/property 선언 | version 변경 incremental |
| 66 | 부분 구현 | P1 | Release 결함 | `Jar manifest Git-Commit` | **git 실패를 ignore하고 빈 값 허용** | source identity 없는 Artifact 생성 가능 | Release에서 commit 필수 검증 | non-git build | Source 수정 후 Unit/Integration/Runtime Evidence |
| 67 | 부분 구현 | P0 | Release 안전 | `prepareRelease tasks` | **dirty/branch/tag/승인 Guard 부재** | 수정 중 또는 잘못된 branch에서 release 가능 | clean tree/protected branch/signed tag 조건 | dirty/non-master | Source 수정 후 Unit/Integration/Runtime Evidence |
| 68 | 재확인 필요 | P1 | Version 정합성 | `Platform/Component version` | **final platform과 component SNAPSHOT 공존 가능** | 최종 Release에 snapshot component 혼입 가능 | 모든 component final/compatible 검사 | override fixture | Source 수정 후 Unit/Integration/Runtime Evidence |
| 69 | 부분 구현 | P2 | Gate 취약성 | `Generator launcher gate` | **크기와 문자열 포함만 검사** | 중복 구현/부작용 launcher 통과 가능 | 허용 invocation hash/AST gate | malicious launcher | Source 수정 후 Unit/Integration/Runtime Evidence |
| 70 | 부분 구현 | P1 | Stale Artifact | `generateReleaseMetadata` | **output directory clean 부재** | 이전 Artifact/checksum 혼입 가능 | 생성 전 clean/allowlist | artifact 제거 후 재생성 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 71 | 부분 구현 | P1 | Provenance 결함 | `Release Git source` | **git SHA/branch blank 허용** | provenance source identity 불완전 | commit/branch/tag 필수 | detached/unavailable | Source 수정 후 Unit/Integration/Runtime Evidence |
| 72 | 부분 구현 | P1 | Release Gate | `validateReleaseMetadata` | **Artifact 0건 실패 조건 없음** | 빈 Release metadata가 통과 가능 | 필수 Module/최소 개수 검증 | assemble output 제거 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 73 | 재확인 필요 | P2 | Artifact 식별 의심 | `Release validator` | **동일 파일명 Artifact를 경로 없이 식별** | Module 간 같은 archive name 충돌/오인 | module path+classifier identity | duplicate name fixture | Source 수정 후 Unit/Integration/Runtime Evidence |
| 74 | 부분 구현 | P1 | SBOM 불완전 | `SBOM scope` | **runtimeClasspath Maven component만 중심** | frontend/plugin/container/build tool 누락 | CycloneDX/SPDX 전체 공급망 | 표준 BOM 비교 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 75 | 부분 구현 | P2 | SBOM 규격 | `PURL 생성` | **group/name/version URL encoding 미확인** | 특수문자 PURL 파손 | 공식 package-url library | special coordinate | Source 수정 후 Unit/Integration/Runtime Evidence |
| 76 | 부분 구현 | P2 | SBOM graph | `dependency roots` | **cpf-module root가 components에 선언되지 않음** | graph consumer 해석 불완전 | application component root 선언 | SBOM validator | Source 수정 후 Unit/Integration/Runtime Evidence |
| 77 | 실패 | P0 | License Gate | `SBOM license` | **NOASSERTION을 채우고 존재만 확인** | License 미확정 Release 통과 | allow/deny/review와 승인 예외 | 금지 License fixture | Source 수정 후 Unit/Integration/Runtime Evidence |
| 78 | 미구현 | P0 | 공급망 Gap | `Frontend SBOM` | **ADM/BZA npm dependency 미포함** | Frontend CVE/License 누락 | package-lock 기반 SBOM 통합 | npm transitive | Source 수정 후 Unit/Integration/Runtime Evidence |
| 79 | 미구현 | P1 | 공급망 Gap | `Build/Runtime environment` | **Gradle plugin/JDK/base image 미포함** | 빌더 공급망 추적 불가 | builder/runtime BOM | image/JDK hash | Source 수정 후 Unit/Integration/Runtime Evidence |
| 80 | 미구현 | P0 | 보안 Gap | `Vulnerability scan` | **SBOM 후 CVE Gate 없음** | Critical 취약점 포함 Release 가능 | OSV/Dependency-Track/Grype gate | Critical fixture | Source 수정 후 Unit/Integration/Runtime Evidence |
| 81 | 부분 구현 | P1 | 상호운용성 | `CPF custom SBOM` | **표준 CycloneDX/SPDX가 아님** | 고객 보안 도구 연계 어려움 | 표준 SBOM+CPF extensions | 표준 import | Source 수정 후 Unit/Integration/Runtime Evidence |
| 82 | 부분 구현 | P0 | Provenance 불완전 | `provenance inputs` | **일부 Build 파일만 material로 기록** | Source/wrapper/lock/script 변조 범위 확인 불가 | source tree/material digest와 builder identity | SLSA verifier | Source 수정 후 Unit/Integration/Runtime Evidence |
| 83 | 재확인 필요 | P2 | Provenance 신뢰 | `CPF_BUILD_COMMAND` | **환경 문자열을 actual command로 신뢰** | 실제와 다른 command 기록 가능 | CI task graph에서 생성 | spoofed env | Source 수정 후 Unit/Integration/Runtime Evidence |
| 84 | 미구현 | P0 | 서명 Gap | `Release signature` | **NOT_SIGNED도 validation 통과** | Artifact 출처·무결성 보장 부족 | keyless/key signing과 attestation 필수 | missing/bad signature | Source 수정 후 Unit/Integration/Runtime Evidence |
| 85 | 부분 구현 | P0 | Release 안전 | `dirty build` | **dirty를 기록만 하고 차단하지 않음** | 미Commit Source 상용 배포 가능 | RC/final에서 금지 | dirty negative | Source 수정 후 Unit/Integration/Runtime Evidence |
| 86 | 부분 구현 | P1 | Schema 검증 | `Release JSON validator` | **top-level required key만 수동 확인** | nested type/shape 오류 통과 | 정식 JSON Schema validator | negative corpus | Source 수정 후 Unit/Integration/Runtime Evidence |
| 87 | 부분 구현 | P2 | Schema 엄격성 | `Release schemas` | **additionalProperties/nested item schema 부족** | 오타·미정 field 수용 | strict nested schema | unknown field | Source 수정 후 Unit/Integration/Runtime Evidence |
| 88 | 미구현 | P1 | 재현성 Gap | `Release Artifact` | **독립 재빌드 hash 비교 없음** | reproducibility 미증명 | clean container 2회 hash gate | double build | Source 수정 후 Unit/Integration/Runtime Evidence |
| 89 | 실패 | P1 | Deploy 설정 | `cpfValidateDeployEnv` | **선택 mode와 무관한 URL/JNDI 값 요구** | 불필요 Secret/잘못된 config 강제 가능 | mode별 conditional keys | url/jndi minimum | Source 수정 후 Unit/Integration/Runtime Evidence |
| 90 | 실패 | P0 | Deploy 안전 | `cpfValidateDeployInventory` | **prod inventory 없으면 template fallback** | 실제 운영 target 없이 검증 통과 가능 | prod는 승인 inventory 없으면 실패 | missing inventory | Source 수정 후 Unit/Integration/Runtime Evidence |
| 91 | 부분 구현 | P1 | Config parser | `cpfReadEnvFile` | **quote/escape/multiline/duplicate 처리 부족** | 특수문자 secret과 중복 key 오해석 | 표준 dotenv parser/duplicate reject | special secrets | Source 수정 후 Unit/Integration/Runtime Evidence |
| 92 | 재확인 필요 | P1 | Evidence 노출 의심 | `deploy sanitized inventory` | **target 객체 전체를 Evidence에 기록** | host/path 등 인프라 정보 과다 노출 가능 | 필드 allowlist/민감도 분류 | Evidence scanner | Source 수정 후 Unit/Integration/Runtime Evidence |
| 93 | 부분 구현 | P1 | Package 검증 부족 | `cpfCheckPackagedDependencies` | **core/common 존재만 중심** | version mismatch·duplicate·forbidden jar 누락 | 정확 version/BOM/duplicate gate | bad version fixture | Source 수정 후 Unit/Integration/Runtime Evidence |
## C. Contract·Architecture·Ownership

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 94 | 부분 구현 | P1 | 계약 결함 | `check-contract-compatibility.ps1` | **Flat field 비교만 지원** | 중첩 object/array 변경을 검출하지 못한다. | Consumer 파손 누락 | kind별 recursive AST diff | nested DTO |
| 95 | 실패 | P1 | Gate 결함 | `Contract script` | **입력 JSON Schema 선검증 없음** | 잘못된 타입/오타가 diff를 왜곡한다. | Baseline/Candidate schema validation | invalid contract | Source 수정 후 Unit/Integration/Runtime Evidence |
| 96 | 부분 구현 | P2 | Schema 결함 | `compatibility-contract.schema.json` | **additionalProperties:false 부재** | 오타 field가 조용히 수용된다. | 엄격 schema | unknown property | Source 수정 후 Unit/Integration/Runtime Evidence |
| 97 | 부분 구현 | P1 | Version 결함 | `Contract version` | **SemVer·증가 방향·major policy 없음** | breaking인데 동일 version 가능 | SemVer와 compatibility policy | downgrade/same version | Source 수정 후 Unit/Integration/Runtime Evidence |
| 98 | 부분 구현 | P1 | Type 결함 | `Contract field.type` | **임의 문자열 허용** | serializer와 연결되지 않는 타입 저장 | kind별 type vocabulary | unknown type | Source 수정 후 Unit/Integration/Runtime Evidence |
| 99 | 부분 구현 | P1 | 호환성 결함 | `Optional field removal` | **제거를 compatible로 볼 수 있음** | Consumer가 읽는 optional field 파손 | deprecation/consumer usage policy | optional removal | Source 수정 후 Unit/Integration/Runtime Evidence |
| 100 | 부분 구현 | P1 | 호환성 결함 | `Field semantics` | **enum/null/default/range/pattern 미검사** | 유효값 축소·default 변경 누락 | JSON Schema/OpenAPI semantic diff | enum/default | Source 수정 후 Unit/Integration/Runtime Evidence |
| 101 | 부분 구현 | P0 | REST Gap | `REST contract` | **method/path/status/media/header/query/security 미모델링** | Route/인증 변경이 Gate 통과 | OpenAPI operation diff | REST breaking | Source 수정 후 Unit/Integration/Runtime Evidence |
| 102 | 부분 구현 | P0 | Event Gap | `EVENT contract` | **partition/order/delivery/schema evolution 부족** | Consumer ordering/dedup 파손 | Event envelope/key/delivery model | event changes | Source 수정 후 Unit/Integration/Runtime Evidence |
| 103 | 부분 구현 | P0 | 전문 Gap | `FIXED_LENGTH contract` | **전체 byte length/overlap/gap/padding/charset 부족** | 전문 위치·길이 오류 누락 | byte layout validator | multibyte/overlap | Source 수정 후 Unit/Integration/Runtime Evidence |
| 104 | 부분 구현 | P0 | 파일 Gap | `FILE contract` | **delimiter/header/quote/compression/checksum 부족** | 상대 시스템 parser 파손 | File profile contract | format changes | Source 수정 후 Unit/Integration/Runtime Evidence |
| 105 | 부분 구현 | P0 | Batch Gap | `BATCH contract` | **required/default/identity/restart semantics 부족** | 재실행/JobInstance 계약 파손 | Batch parameter/restart model | identity changes | Source 수정 후 Unit/Integration/Runtime Evidence |
| 106 | 미구현 | P0 | 제품 기능 Gap | `Contract Registry` | **환경별 Consumer/Provider Registry와 can-deploy 없음** | 배포된 Consumer 기준 안전성 판정 불가 | Registry+environment matrix+CI API | mixed versions | Source 수정 후 Unit/Integration/Runtime Evidence |
| 107 | 부분 구현 | P2 | 검증 부족 | `Contract self-test` | **소수 정상/오류 case만 존재** | 다양한 kind 회귀 미방지 | kind별 negative corpus | mutation test | Source 수정 후 Unit/Integration/Runtime Evidence |
| 108 | 미구현 | P1 | 정본 Gap | `Contract baseline lifecycle` | **승인·서명·폐기·deprecation 절차 없음** | 임의 baseline 조작 가능 | signed snapshot workflow | tamper test | Source 수정 후 Unit/Integration/Runtime Evidence |
| 109 | 부분 구현 | P2 | 도구 이식성 | `PowerShell Gate` | **PowerShell 없는 고객 CI 사용 어려움** | 플랫폼 편향 | portable JVM/CLI 정본 | Linux/Windows parity | Source 수정 후 Unit/Integration/Runtime Evidence |
| 110 | 실패 | P1 | Boundary 위반 | `BzaBackofficeService` | **cpf-core.common.exception 직접 import** | 업무 Module이 Internal 구현에 결합 | Public api.error 사용+ArchUnit | dependency gate | Source 수정 후 Unit/Integration/Runtime Evidence |
| 111 | 재확인 필요 | P0 | 중복 구현 의심 | `BZA approval services` | **Legacy backoffice 상태전이와 신규 approval engine 공존** | 같은 테이블에 다른 규칙 적용 가능 | 정본 Owner 하나로 통합 | endpoint/consumer map | Source 수정 후 Unit/Integration/Runtime Evidence |
| 112 | 재확인 필요 | P1 | 제품 정책 Gap | `cpf-account` | **Generator 전환 대상인데 고정 Module/Deploy 목록 유지** | 확장 모델이 고정 Domain으로 회귀 | Consumer 이관·제거 Gate | ACC inventory | Source 수정 후 Unit/Integration/Runtime Evidence |
| 113 | 실패 | P1 | Generator 확장 | `root cpfServiceModules` | **Deploy 대상이 고정 Map** | 새 Domain이 표준 deploy에 자동 참여 못함 | Generator Manifest 기반 inventory | 새 Domain deploy | Source 수정 후 Unit/Integration/Runtime Evidence |
| 114 | 미검증 | P1 | 계약 정합성 | `Local/Remote topology` | **동일 Header/Error/Permission 동등성 Runtime Evidence 부족** | Topology별 다른 결과 가능 | 동일 suite 양쪽 실행 | parity Evidence | Source 수정 후 Unit/Integration/Runtime Evidence |
| 115 | 미검증 | P1 | Boundary Gate | `Public API/SPI/Internal` | **전체 ArchUnit/jdeps 최신 실행 Evidence 없음** | internal import 회귀 가능 | Release 필수 Gate | latest SHA logs | Source 수정 후 Unit/Integration/Runtime Evidence |
## D. Security·Authentication·Header

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 116 | 실패 | P0 | 인증 우회 | `BzaAuthService` | **강제 비밀번호 변경 Backend 차단 없음** | requireActiveOperator는 use/lock만 검사한다. | UI 우회 API 사용 가능 | password change 외 API 서버 차단 | 직접 REST |
| 117 | 실패 | P0 | 인증 우회 | `BzaAuthService` | **비밀번호 만료 Backend 집행 없음** | passwordExpireAt은 반환되지만 login/authorize 차단이 보이지 않는다. | 만료 계정 업무 수행 가능 | login/refresh/authorize 집행 | expired account |
| 118 | 부분 구현 | P0 | 세션 결함 | `BZA logout/password change` | **Refresh만 폐기하고 access token은 TTL까지 유효** | logout 후 탈취 token 사용 가능 | jti/session version/introspection | old access token | Source 수정 후 Unit/Integration/Runtime Evidence |
| 119 | 재확인 필요 | P1 | 원자성 의심 | `BzaAuthService login` | **성공 갱신·이력·refresh 저장의 명시 transaction 없음** | 중간 실패로 상태 불일치 | transaction/state machine | write failure injection | Source 수정 후 Unit/Integration/Runtime Evidence |
| 120 | 미구현 | P0 | 보안 Gap | `BZA login protection` | **분산 rate limit/lock threshold 미확인** | Brute force 방어 부족 | rate limit·delay·lock·alert | multi-node brute force | Source 수정 후 Unit/Integration/Runtime Evidence |
| 121 | 부분 구현 | P1 | 세션 정책 | `BZA refresh` | **rotation마다 TTL 재설정, absolute max 없음** | 세션 무기한 연장 가능 | absolute+idle expiry | long rotation | Source 수정 후 Unit/Integration/Runtime Evidence |
| 122 | 미구현 | P0 | 토큰 Gap | `BZA refresh family` | **family/reuse detection 없음** | 탈취 old token 재사용 탐지 불가 | family id/parent/reuse detection | old token replay | Source 수정 후 Unit/Integration/Runtime Evidence |
| 123 | 부분 구현 | P0 | 키 관리 | `JWT HS256` | **단일 secret, kid/rotation 없음** | 키 교체·노출 대응 취약 | KMS key ring/kid/grace | two-key rotation | Source 수정 후 Unit/Integration/Runtime Evidence |
| 124 | 부분 구현 | P1 | 키 품질 | `requireJwtSecret` | **길이만 확인** | 저entropy 32자 문자열 통과 | encoding/entropy/secret manager ref | weak key | Source 수정 후 Unit/Integration/Runtime Evidence |
| 125 | 부분 구현 | P2 | 정보 노출 | `JWT validation reason` | **구체 실패 reason 외부 노출 가능** | 공격자에게 구조 정보 제공 | 외부 일반화/내부 상세 audit | invalid token | Source 수정 후 Unit/Integration/Runtime Evidence |
| 126 | 미구현 | P1 | 비밀번호 Gap | `Password history` | **현재 비밀번호만 재사용 차단** | 과거 비밀번호 재사용 가능 | history/breach list | 최근 N개 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 127 | 부분 구현 | P1 | 권한 Staleness | `JWT menus/buttons` | **권한 목록을 token에 포함** | 회수 후 UI claim stale | permission version/DB recheck | 권한 회수 | Source 수정 후 Unit/Integration/Runtime Evidence |
| 128 | 실패 | P0 | 권한 결함 | `findEffectiveRoleCodes` | **legacy role 활성 여부 무시** | 중지 Role도 권한 부여 가능 | role use/effective 검사 | disabled role | Source 수정 후 Unit/Integration/Runtime Evidence |
| 129 | 실패 | P0 | 권한 결함 | `findMenus/findButtons` | **Role/Menu use 상태 join 없음** | 중지 항목 권한 계산 가능 | role/menu join 조건 | disable test | Source 수정 후 Unit/Integration/Runtime Evidence |
| 130 | 부분 구현 | P0 | 권한 범위 | `BZA permission evaluator` | **environment/domain/dataScope/http/apiPattern 무시** | 화면·API·데이터 권한 불일치 | 통합 evaluator | scope matrix | Source 수정 후 Unit/Integration/Runtime Evidence |
| 131 | 부분 구현 | P0 | 권한 모델 | `BZA permission` | **Deny precedence 없음** | 다중 Role allow가 제한을 덮음 | deny conflict policy | allow+deny | Source 수정 후 Unit/Integration/Runtime Evidence |
| 132 | 미구현 | P1 | 세션 운영 Gap | `bza_refresh_token` | **device/ip/user-agent/last-used 없음** | 의심 세션 식별 곤란 | device session metadata | session UI | Source 수정 후 Unit/Integration/Runtime Evidence |
| 133 | 재확인 필요 | P1 | 입력 길이 의심 | `Login history` | **긴 userAgent/IP safe handling 미확인** | DB 오류로 로그인 처리 실패 가능 | normalize/truncate/hash | oversized header | Source 수정 후 Unit/Integration/Runtime Evidence |
| 134 | 재확인 필요 | P2 | 식별 정합성 | `BZA login ID` | **대소문자 canonical 정책 불명확** | 계정 중복/정책 차이 | canonical normalization | case variants | Source 수정 후 Unit/Integration/Runtime Evidence |
| 135 | 실패 | P0 | Header 검증 | `HeaderValidator` | **필수값 존재만 검사** | 임의 ID/channel/timestamp 통과 | 표준 형식/enum/time validator | invalid corpus | Source 수정 후 Unit/Integration/Runtime Evidence |
| 136 | 실패 | P1 | Header 검증 | `HeaderValidator` | **공백 문자열 허용** | 공백 ID/channel 통과 | hasText/trim | whitespace | Source 수정 후 Unit/Integration/Runtime Evidence |
| 137 | 실패 | P0 | 추적 결함 | `transactionId validation` | **34자리 규격 미검사** | DB/검색/상관관계 파손 | 공통 parser | length/char/time | Source 수정 후 Unit/Integration/Runtime Evidence |
| 138 | 부분 구현 | P0 | Replay Gap | `Header timestamp` | **freshness/clock skew 미검사** | 오래된 서명 요청 replay 가능 | skew/nonce/idempotency | old/future | Source 수정 후 Unit/Integration/Runtime Evidence |
| 139 | 부분 구현 | P0 | Channel 보안 | `Header/Channel` | **Registry 관계와 signature requirement 미검사** | 내부 채널 사칭 가능 | Channel snapshot evaluator | spoof test | Source 수정 후 Unit/Integration/Runtime Evidence |
| 140 | 미구현 | P1 | Header 확장 Gap | `Extension Header` | **size/count/key/PII policy 불명확** | Header bombing·민감 전파 | limit/namespace/redaction | oversized | Source 수정 후 Unit/Integration/Runtime Evidence |
| 141 | 부분 구현 | P1 | JWT 표준 | `CmnJwtService` | **nbf/jti/maxTTL/skew 미지원** | Replay·미래 token 통제 부족 | standard claims policy | nbf/jti | Source 수정 후 Unit/Integration/Runtime Evidence |
| 142 | 부분 구현 | P2 | JWT 경계 | `exp 비교` | **isBefore만 사용** | exp==now 경계 유효 가능 | now>=exp/Clock | boundary | Source 수정 후 Unit/Integration/Runtime Evidence |
| 143 | 미구현 | P1 | DoS Gap | `JWT decode` | **Token 길이/JSON depth/count 제한 없음** | 과대 token 자원 소모 | byte/depth/claim limits | oversized JWT | Source 수정 후 Unit/Integration/Runtime Evidence |
| 144 | 부분 구현 | P1 | 오류 분류 | `JWT malformed claims` | **non-numeric exp 등이 500으로 갈 수 있음** | client 오류가 서버 장애로 기록 | malformed는 invalid 결과 | bad exp | Source 수정 후 Unit/Integration/Runtime Evidence |
| 145 | 재확인 필요 | P1 | API 오용 | `readClaimsWithoutVerification` | **일반 Service 공개** | 인증 판단 오용 위험 | internal diagnostic로 제한 | usage gate | Source 수정 후 Unit/Integration/Runtime Evidence |
| 146 | 부분 구현 | P1 | Token 크기 | `BZA JWT` | **전체 menus/buttons 포함** | Header limit·구조 노출 | compact scopes/version ref | large permissions | Source 수정 후 Unit/Integration/Runtime Evidence |
| 147 | 부분 구현 | P1 | 감사 Gap | `Mandatory audit` | **login/logout 제외** | 세션 보안 행위가 durable audit와 분리 | 전용 security audit | login/logout delivery | Source 수정 후 Unit/Integration/Runtime Evidence |
| 148 | 재확인 필요 | P0 | 내부 API 보안 | `MbrAdminOperationsController` | **allowedCallers annotation 외 runtime 신원 집행 미확인** | ADM 사칭 내부 호출 위험 | mTLS/service token/signature | non-ADM caller | Source 수정 후 Unit/Integration/Runtime Evidence |
## E. Observability·Feature Flag·Fault Injection

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 149 | 부분 구현 | P1 | 기능 결함 | `CpfPropertyFeatureFlagProvider` | **disabled가 caller safeDefault 반환** | safeDefault=true면 kill switch가 실제로 true를 반환한다. | 긴급 차단 실패 가능 | disabled/kill switch를 명시 false와 분리 | safeDefault=true |
| 150 | 부분 구현 | P1 | 평가 결함 | `Feature boolean parse` | **오타 문자열을 false로 조용히 변환** | 설정 오류가 정상 평가로 위장 | 엄격 parser/startup validation | invalid boolean | Source 수정 후 Unit/Integration/Runtime Evidence |
| 151 | 부분 구현 | P2 | 타게팅 결함 | `Feature targets` | **case-sensitive CSV exact match** | 대소문자/Unicode 차이로 target 누락 | canonical normalization | case/unicode | Source 수정 후 Unit/Integration/Runtime Evidence |
| 152 | 부분 구현 | P1 | Rollout 결함 | `stableTargetKey` | **식별자 없는 요청이 모두 anonymous** | 익명 트래픽이 한 bucket으로 몰림 | stable request/device key 또는 익명 rollout 금지 | distribution | Source 수정 후 Unit/Integration/Runtime Evidence |
| 153 | 부분 구현 | P2 | 설정 경계 | `flagKey` | **key regex/reserved namespace 없음** | property 충돌·오타 가능 | flag registry/key validation | invalid key | Source 수정 후 Unit/Integration/Runtime Evidence |
| 154 | 부분 구현 | P1 | 관측 Gap | `Feature provider` | **예외를 safe default로 삼키고 metric/log 부족** | provider 장애 장기 은폐 | error metric/health/audit | provider exception | Source 수정 후 Unit/Integration/Runtime Evidence |
| 155 | 미구현 | P0 | 제품 기능 Gap | `Feature Flag control plane` | **영속 Registry·Version·승인·Rollback 없음** | 안전한 운영 변경 불가 | OpenFeature 호환 control plane | multi-node change | Source 수정 후 Unit/Integration/Runtime Evidence |
| 156 | 미구현 | P1 | Lifecycle Gap | `Feature Flag` | **owner/expiry/dead flag 관리 없음** | 임시 Flag 영구화 | owner/expiry/removal gate | dead flag report | Source 수정 후 Unit/Integration/Runtime Evidence |
| 157 | 부분 구현 | P1 | 타입 Gap | `Feature values` | **integer/JSON/schema variant 부족** | consumer별 임의 parsing | typed variants/schema | typed tests | Source 수정 후 Unit/Integration/Runtime Evidence |
| 158 | 부분 구현 | P1 | 데이터 보호 Gap | `Feature context` | **attribute PII allowlist 없음** | 외부 provider로 민감정보 전파 가능 | context schema/redaction | PII attributes | Source 수정 후 Unit/Integration/Runtime Evidence |
| 159 | 부분 구현 | P1 | Trace 전파 Gap | `CpfTelemetryAspect` | **W3C context extraction/injection 없음** | 분산 trace 단절 | traceparent/baggage bridge | remote continuity | Source 수정 후 Unit/Integration/Runtime Evidence |
| 160 | 부분 구현 | P1 | Span kind | `Telemetry aspect` | **모든 Online을 SERVER로 기록** | local/shared/batch 의미 왜곡 | kind별 instrumentation | span kind tests | Source 수정 후 Unit/Integration/Runtime Evidence |
| 161 | 부분 구현 | P1 | 상관관계 Gap | `Telemetry` | **CPF transactionId↔trace mapping 운영 조회 없음** | 로그/trace 이동 곤란 | 표준 attributes와 ADM deep link | cross search | Source 수정 후 Unit/Integration/Runtime Evidence |
| 162 | 미구현 | P0 | Observability Gap | `Remote/Message/Batch/File` | **전 transport propagation 미완료** | MSA/비동기 trace 단절 | transport inject/extract | E2E traces | Source 수정 후 Unit/Integration/Runtime Evidence |
| 163 | 미구현 | P1 | Observability Gap | `Metrics/log correlation` | **OTel metrics/log linkage 없음** | SLO·capacity 운영 부족 | RED/USE metrics/exemplars | collector metrics | Source 수정 후 Unit/Integration/Runtime Evidence |
| 164 | 부분 구현 | P1 | Resource Gap | `Telemetry config` | **service/module/version/environment resource 부족** | Collector 분류 불완전 | OTel Resource metadata | collector inspect | Source 수정 후 Unit/Integration/Runtime Evidence |
| 165 | 부분 구현 | P1 | Sampling Gap | `Telemetry` | **sampling/trace boost 연계 없음** | 비용 폭증 또는 오류 trace 유실 | parent/tail sampling policy | load sampling | Source 수정 후 Unit/Integration/Runtime Evidence |
| 166 | 부분 구현 | P1 | Exporter Gap | `OTLP` | **TLS/auth/queue/retry/health 부족** | 운영 보안·장애 진단 어려움 | secure profile/drop metrics | exporter down | Source 수정 후 Unit/Integration/Runtime Evidence |
| 167 | 부분 구현 | P0 | 민감정보 의심 | `Telemetry attributes` | **key blacklist 중심** | 일반 key의 민감 값 유출 가능 | allowlist/classifier/value scan | PII trace | Source 수정 후 Unit/Integration/Runtime Evidence |
| 168 | 부분 구현 | P0 | 민감정보 의심 | `recordException` | **exception message/stack 원문 가능** | Collector에 Secret/PII 유출 | exception sanitizer | secret exception | Source 수정 후 Unit/Integration/Runtime Evidence |
| 169 | 부분 구현 | P1 | 자원 보호 Gap | `Telemetry attributes` | **count/length/cardinality 제한 부족** | 고비용 span 폭증 | budgets/truncation metrics | large attributes | Source 수정 후 Unit/Integration/Runtime Evidence |
| 170 | 미검증 | P0 | 검증 Gap | `OpenTelemetry` | **실제 Collector Evidence 없음** | Source만으로 export/비오염 미확정 | local/remote/async/exporter down 검증 | Collector logs | Source 수정 후 Unit/Integration/Runtime Evidence |
| 171 | 부분 구현 | P1 | Fault model 부족 | `CpfControlledFaultInjector` | **sleep/예외 1종** | DB/Broker/Unknown 복구 검증 불가 | fault type SPI | fault matrix | Source 수정 후 Unit/Integration/Runtime Evidence |
| 172 | 부분 구현 | P1 | 현실성 결함 | `Fault Thread.sleep` | **request thread 단순 점유** | 실제 network/async 장애와 다름 | adapter-level nonblocking faults | pool load | Source 수정 후 Unit/Integration/Runtime Evidence |
| 173 | 부분 구현 | P1 | 설정 결함 | `Fault Injection` | **모든 target 공통 delay/throw** | 대상별 시나리오 불가 | per-target scenario | multi target | Source 수정 후 Unit/Integration/Runtime Evidence |
| 174 | 재확인 필요 | P0 | 오활성화 위험 | `chaos profile` | **production hard deny 미확인** | 운영 장애 주입 가능 | prod deny/signed token/TTL/approval | prod negative | Source 수정 후 Unit/Integration/Runtime Evidence |
| 175 | 미구현 | P0 | 통제 Gap | `Fault Injection` | **Audit/TTL/자동해제/상태 조회 없음** | 누가 무엇을 주입했는지 추적 불가 | signed scenario/audit/expiry | lifecycle | Source 수정 후 Unit/Integration/Runtime Evidence |
| 176 | 미구현 | P1 | Coverage Gap | `Fault Injection` | **partial response/reorder/lease loss 없음** | UNKNOWN_RESULT 핵심 검증 불가 | DB/Broker/File/Worker adapters | recovery suite | Source 수정 후 Unit/Integration/Runtime Evidence |
| 177 | 미검증 | P1 | Aspect 순서 | `Telemetry/Logging/Fault` | **Aspect order Integration Evidence 없음** | 예외가 span/log/audit에서 불일치 가능 | 명시 order/test | aspect matrix | Source 수정 후 Unit/Integration/Runtime Evidence |
## F. Cache·다중 인스턴스 일관성

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 178 | 실패 | P1 | Cache Key | `ResponseCodeCacheService` | **raw @Cacheable key와 normalized DB key 불일치** | case variant가 별도 cache entry가 된다. | stale/중복 cache | normalized key generator | case hit |
| 179 | 실패 | P1 | 원자성 | `replaceSnapshot` | **clear 후 순차 put** | 동시 reader가 빈/부분 snapshot을 본다. | 일시적 오류 응답 | atomic generation swap | concurrent read |
| 180 | 재확인 필요 | P1 | 결과 불명 의심 | `afterCommit cache callback` | **DB commit 후 cache 실패 처리 경계 불명확** | client 실패/DB 성공 가능 | 업무 응답과 cache failure 분리 | cache failure after commit | Source 수정 후 Unit/Integration/Runtime Evidence |
| 181 | 부분 구현 | P1 | 불변성 | `Cache values` | **mutable List<Map> 반환** | caller가 공유 cache를 오염시킬 수 있다. | 노드 내 비결정성 | immutable DTO | mutation test |
| 182 | 실패 | P1 | DB 결과 검증 | `ResponseCode CRUD` | **affected row 확인 부족** | 대상 없음이 성공처럼 보일 수 있다. | False success | row count/CAS | 0 row |
| 183 | 부분 구현 | P1 | Actor | `Cache events` | **SYSTEM fallback/hardcode** | 실제 운영자 추적 단절 | 감사 부정확 | verified actor 전달 | actor check |
| 184 | 재확인 필요 | P1 | 계약 의심 | `ResponseCode update` | **path/body code 불일치 허용** | rename/update 의미 불명 | 잘못된 row 변경 | 불일치 금지/rename command | mismatch |
| 185 | 부분 구현 | P2 | 성능 | `Common cache mutations` | **매 변경 전체 snapshot 조회** | write latency/lock 증가 | 대규모 운영 저하 | delta/version 또는 commit 후 build | 100k rows |
| 186 | 부분 구현 | P0 | Readiness | `cache preload` | **fail-fast 기본 false** | 필수 cache 없이 ready 가능 | 오류코드 처리 불능 | 필수 cache readiness | DB down startup |
| 187 | 부분 구현 | P1 | 운영 상태 | `cache status` | **process-local version만** | cluster stale node 식별 불가 | 부분 장애 장기화 | desired/source version matrix | multi-node |
| 188 | 부분 구현 | P2 | 진단 | `cache failure state` | **실패 시각/event/last success 부족** | 현재 장애 판단 곤란 | 운영 대응 지연 | staleSince/lag/retry | status API |
| 189 | 실패 | P0 | 내구성 | `Publisher retryQueue` | **메모리 queue** | restart 시 미저장 event 유실 | cluster stale | durable outbox/spool | DB down restart |
| 190 | 부분 구현 | P1 | 재시도 | `Publisher attempt` | **max/backoff/DLQ 부족** | 독성 event 무한 반복 | 처리 정체 | bounded retry/DLQ | persistent fail |
| 191 | 실패 | P1 | Queue 손실 | `Publisher capacity` | **초과 시 oldest drop** | 중요 변경 영구 유실 | stale cache | drop 금지/durable DLQ | overflow |
| 192 | 실패 | P1 | Head-of-line | `Publisher retry` | **첫 실패에서 return** | 뒤 healthy event도 막힘 | 전체 cache 지연 | event별 schedule | poison event |
| 193 | 재확인 필요 | P1 | 멱등성 의심 | `Cache event insert` | **event UUID/unique key 부족** | 재시도 중복 event 가능 | refresh storm | idempotency key | lost response retry |
| 194 | 부분 구현 | P2 | Instance identity | `sourceWasId` | **default local 충돌** | 다중 노드 구분 불가 | 운영 진단 오류 | unique serverInstanceId 필수 | two nodes |
| 195 | 실패 | P0 | Cursor | `Listener initialize` | **maxEventId부터 시작** | 중단 중 event 건너뜀 | 영구 stale | persistent offset+snapshot reconciliation | node restart |
| 196 | 실패 | P0 | Cursor 내구성 | `lastEventId` | **메모리 only** | 재기동 처리 보장 없음 | event 유실 | consumer offset table | restart |
| 197 | 부분 구현 | P1 | 확장성 | `findEventsAfter` | **bounded batch/retention 부족** | backlog 메모리·DB 폭증 | 장애 복구 실패 | paged fetch/retention | million backlog |
| 198 | 실패 | P1 | Event 유실 | `unknown cacheName` | **warning 후 cursor 전진** | 오타 event 폐기 | 변경 미반영 | DLQ/FAILED policy | unknown event |
| 199 | 실패 | P1 | Head-of-line | `Listener` | **한 refresh 실패가 뒤 event 지연** | 모든 cache 동기화 지연 | cluster stale | partition/skip+DLQ | broken one cache |
| 200 | 부분 구현 | P1 | Refresh storm | `event consumer` | **event마다 전체 refresh** | burst 시 DB 부하 폭증 | 연쇄 장애 | coalesce/debounce/version | 1000 updates |
| 201 | 미구현 | P1 | 제품 기능 Gap | `Cache convergence` | **노드별 desired version/reconcile command 없음** | stale node 복구 어려움 | 운영 복구 지연 | ADM node matrix/reconcile | convergence |
## G. MBR·ADM·Audit·DB

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 202 | 실패 | P1 | 성능 | `MbrOwnerAdminOperationsService.findMembers` | **SQL LIMIT 없이 전체 결과 후 subList** | 대규모 회원에서 DB/Heap 폭증 | 운영 조회 장애 | keyset/cursor SQL paging | million rows |
| 203 | 실패 | P1 | 성능 | `회원번호 발급 이력` | **전체 결과 후 Java limit** | 이력 증가 시 확장성 저하 | 운영 화면 timeout | indexed cursor | large history |
| 204 | 부분 구현 | P1 | 검색 | `회원 LIKE` | **%term%과 wildcard 미escape** | index 미사용/의도치 않은 전체 검색 | DB 부하 | prefix/exact/search index | query plan |
| 205 | 부분 구현 | P1 | 부분 장애 | `MBR detail` | **roles/history/login all-or-nothing** | 한 이력 장애로 기본정보 실패 | 조회 불가 | section status/partial model | table failure |
| 206 | 실패 | P0 | 입력 검증 | `MBR create/update` | **핵심 필드 길이·형식·상태 검증 부족** | DB 오류·잘못된 데이터 저장 | 원장 오염 | Bean/domain validation | fuzz/boundary |
| 207 | 실패 | P1 | 입력 검증 | `MBR yn helper` | **Y 외 값을 모두 N으로 변환** | 오타가 정상 N으로 저장 | 상태 오염 | Y/N 엄격 검증 | invalid yn |
| 208 | 재확인 필요 | P1 | 업무 식별 의심 | `customerNo default` | **미입력 시 memberNo 사용** | 서로 다른 식별 의미 혼재 | downstream identity 오류 | 발급/nullable 정책 명시 | lifecycle |
| 209 | 부분 구현 | P1 | 오류 분류 | `DuplicateKeyException` | **모든 unique 충돌을 회원번호/loginId로 안내** | 실제 constraint 원인 은폐 | 잘못된 대응 | constraint name mapping | each conflict |
| 210 | 재확인 필요 | P0 | 식별자 변경 의심 | `updateMember` | **memberNo/customerNo/loginId 일반 수정** | 외부 참조·발급이력 불일치 | 추적 파손 | 불변 또는 승인 rename command | identifier change |
| 211 | 실패 | P1 | Patch 의미 | `updateMember` | **email/mobile clear 불가, description clear 가능** | 필드 의미 불일치 | 운영 수정 오류 | PUT/PATCH nullable contract | clear tests |
| 212 | 실패 | P0 | 상태 조합 | `member status` | **ACTIVE+withdraw Y 등 상충 허용** | 인증/업무 해석 분열 | 원장 불일치 | lifecycle state machine/CHECK | state matrix |
| 213 | 부분 구현 | P0 | 멱등성 Gap | `MBR create/update/status` | **Role 외 idempotency 없음** | timeout 재시도 중복/반복 변경 | 중복 원장 | 공통 command idempotency | timeout replay |
| 214 | 부분 구현 | P1 | 감사 Gap | `MBR member mutation` | **Owner DB에 reason history 부족** | ADM audit 장애 시 사유 유실 | 감사 불완전 | Owner operation history | audit down |
| 215 | 재확인 필요 | P1 | 시간 계약 | `role expireAt` | **String→DATETIME** | timezone/vendor 차이 | 잘못된 만료 | typed Instant/UTC | timezone vendors |
| 216 | 부분 구현 | P1 | Code 정합성 | `role/service code` | **normalize/Registry 검증 부족** | case variant/미등록 권한 | 권한 오염 | canonical code/FK | unknown code |
| 217 | 재확인 필요 | P1 | 멱등 충돌 분류 | `reserveRoleOperation` | **모든 DuplicateKey를 idempotency 중복으로 가정** | 다른 DB 오류가 replay 경로 | 오판 | constraint mapping | FK/PK failure |
| 218 | 실패 | P0 | 멱등성 | `role operation` | **payload hash/expectedVersion/reason/expiry 비교 없음** | 같은 key의 다른 요청을 replay | 잘못된 권한 결과 | canonical request hash | same key diff payload |
| 219 | 실패 | P1 | 멱등 결과 | `idempotentRoleResult` | **최초 response 아닌 현재 role 반환** | 후속 변경 뒤 replay 결과 달라짐 | 멱등 계약 위반 | original response snapshot | change then replay |
| 220 | 부분 구현 | P1 | Audit format | `role history` | **Map.toString 저장** | JSON diff/schema 불가 | 감사 도구 사용 불가 | canonical masked JSON | parse/diff |
| 221 | 부분 구현 | P2 | 조회 누락 | `role history query` | **serviceCode/operatorId 누락** | 행위 범위/주체 확인 어려움 | 조사 지연 | 핵심 column 반환 | API contract |
| 222 | 실패 | P1 | 성능 | `MBR histories` | **전체 조회 후 subList** | DB/Heap 낭비 | 대량 이력 timeout | SQL limit/cursor | large history |
| 223 | 실패 | P1 | 입력 검증 | `requiredLong` | **음수 ID 허용** | 불필요 DB/오류 혼란 | 잘못된 요청 | positive validation | 0/-1 |
| 224 | 부분 구현 | P0 | PII 노출 | `MBR Owner response` | **email/mobile/customer/member 원문 Map** | ADM 권한과 무관한 노출 | 개인정보 유출 | 민감 DTO/masking privilege | Viewer response |
| 225 | 실패 | P0 | Audit 보존 | `MBR FK cascade` | **회원 삭제 시 role operation/history/login history 삭제** | 감사·보안 증거 소멸 | 규제 위험 | 독립 보존/set null/archive | delete retention |
| 226 | 부분 구현 | P1 | 계약 은폐 | `AdmMemberOperationService` | **잘못된 Owner response를 빈 list/map으로 변환** | 계약 장애가 0건으로 위장 | 오판 | schema/protocol error | malformed response |
| 227 | 실패 | P0 | PII 감사 노출 | `AdmMemberController` | **before/after Map 원문 감사** | email/mobile 원문 저장 가능 | 개인정보 유출 | typed recursive sanitizer | audit DB inspect |
| 228 | 부분 구현 | P1 | 과도한 결합 | `ADM member mutation` | **변경 전 전체 detail 조회** | read-model 장애가 mutation 차단 | 운영 변경 불가 | Owner basic snapshot만 사용 | aux DB down |
| 229 | 부분 구현 | P1 | 정보 노출 | `role revoke query` | **reason/idempotency를 query parameter로 전달** | access/proxy log 노출 | 사유·key 노출 | command body/header | log inspect |
| 230 | 부분 구현 | P1 | Paging | `ADM member list` | **raw list, cursor/total/truncated 없음** | 다음 페이지/잘림 판단 불가 | 운영 사용성 저하 | Page/Slice 표준 | 500+ |
| 231 | 부분 구현 | P0 | 감사 노출 | `AdmAuditLogService` | **BEFORE/AFTER/DIFF 원문 반환** | 감사 조회자가 민감 원문 열람 | PII 유출 | field masking/privileged reveal | Viewer audit |
| 232 | 부분 구현 | P1 | 감사 완결성 | `enrichReservation` | **보강 실패 후 mutation 계속** | 실제 action/target 누락 가능 | 감사 품질 저하 | block 또는 UNKNOWN review | DB failure |
| 233 | 부분 구현 | P0 | 감사 결과 유실 | `mandatory completion` | **완료 기록 실패가 log 후 stale UNKNOWN** | 정확 after/result 영구 유실 가능 | 복구 불가 | durable result sidecar/reconcile | post-owner failure |
| 234 | 부분 구현 | P1 | Sanitizer | `Audit delivery` | **regex+16KB truncate** | JSON 파손/PII 미마스킹/증적 잘림 | 감사 신뢰 저하 | typed sanitizer+hash+archive | large nested PII |
| 235 | 부분 구현 | P0 | 무결성 Gap | `adm_audit_log` | **IMMUTABLE_YN만 있고 hash chain/WORM 없음** | DB 수정 탐지 불가 | 감사 변조 | cryptographic ledger | tamper test |
| 236 | 부분 구현 | P1 | 보존 정책 | `Audit retention` | **5년 SQL hardcode** | 고객/법규/legal hold 미지원 | 정책 위반 | versioned retention/hold | policy tests |
| 237 | 부분 구현 | P1 | 오류 노출 | `Audit LAST_ERROR` | **raw exception message 저장** | SQL/host/path 노출 | 내부 정보 유출 | external code/secure detail | DB error UI |
| 238 | 부분 구현 | P1 | 다중 인스턴스 | `Audit relay` | **SKIP LOCKED claim 부재** | 노드 경합/처리량 저하 | relay 지연 | lease/SKIP LOCKED | 2 nodes |
| 239 | 부분 구현 | P1 | Reconciliation Gap | `stale REQUESTED` | **UNKNOWN 승격 후 Owner 대사 없음** | 최종 결과 미확정 | 감사 불완전 | command key 기반 reconcile | crash point |
| 240 | 부분 구현 | P1 | 감사 멱등성 | `Audit reservation` | **request unique key 없음** | retry로 중복 audit | 중복 증적 | transaction/action/request key | same retry |
| 241 | 부분 구현 | P0 | IP 신뢰 | `Audit clientIp` | **remoteAddr 직접 사용** | proxy IP/Forwarded 정책 불명 | 행위자 위치 오판 | trusted proxy validation | proxy topology |
| 242 | 실패 | P0 | Migration drift | `V52 IF NOT EXISTS` | **기존 column shape 검증 없음** | 잘못된 schema도 성공 | Runtime SQL 실패 | information_schema precondition | wrong type upgrade |
| 243 | 부분 구현 | P1 | Constraint Gap | `V52 tables` | **operation/status/YN CHECK 부족** | 임의 상태 저장 | state machine 파손 | CHECK/FK | invalid state |
| 244 | 부분 구현 | P2 | 용량 | `member_no_sequence` | **발급마다 row 영구 증가** | 장기 table 성장 | 용량 증가 | DB sequence/purge-safe allocator | large issuance |
| 245 | 실패 | P1 | 이력 참조 | `issue history` | **member_id 없이 memberNo만** | 변경/삭제 후 주체 상실 | 감사 연결 파손 | immutable identity/memberId snapshot | rename/delete |
| 246 | 재확인 필요 | P1 | 이력 모델 | `issue history unique memberNo` | **번호당 1 event만 허용** | 취소/재발급/정정 기록 불가 | lifecycle 누락 | event model+active uniqueness | reissue |
| 247 | 부분 구현 | P0 | 멱등 상태 Gap | `role operation table` | **FAILED/UNKNOWN/EXPIRED/request hash 없음** | PENDING 영구·payload 충돌 | 복구 불가 | state machine/hash/error | crash |
| 248 | 실패 | P0 | Rollback 손실 | `R52` | **이력/멱등/sequence table DROP** | 운영 데이터 영구 소실 | 복구 불가 | archive/precondition/non-destructive | populated rollback |
| 249 | 부분 구현 | P1 | Rollback 비대칭 | `R52` | **추가 columns 유지** | 이전 schema로 미복귀 | 구버전 app 불일치 | forward-only/compat definition | V51 app |
| 250 | 실패 | P1 | Audit 손실 | `R52 CSV policy` | **policy version column DROP** | 과거 방어정책 증거 소실 | 감사 불완전 | 보존/archive | rollback audit |
| 251 | 부분 구현 | P0 | PII 보호 Gap | `mbr_member schema` | **원문 PII 중심** | DB/backup 유출 시 직접 식별 | 개인정보 위험 | encryption/tokenization/lookup hash | DB role/backup |
| 252 | 부분 구현 | P0 | 동시성 Gap | `BZA 관리 tables` | **다수 version_no 없음** | 동시 관리자 변경 덮어쓰기 | lost update | CAS/history | two admins |
| 253 | 부분 구현 | P1 | 제품/샘플 혼재 | `business schema source` | **운영·REF·sample이 한 install source에 혼재** | prod에 불필요 sample 잔존 | 제품 정본 오염 | product/EDU pack 분리 | fresh install inventory |
## H. Batch·Calendar·Service Call·File

| ID | 판정 | 우선도 | 유형 | 대상 | 항목 | Source 근거 | 실제 위험 | 권장 수정 | 필수 검증 |
|---:|---|---|---|---|---|---|---|---|---|
| 254 | 부분 구현 | P1 | DB Vendor | `BatOperationFacade SQL` | **TIMESTAMPDIFF/TIMESTAMPADD를 Java Source에 직접 사용** | 다중 Vendor에서 SQL 파손 | Vendor 지원 실패 | Vendor SQL catalog/mapper | 5 Vendor |
| 255 | 부분 구현 | P1 | 시간 입력 | `Batch execution search` | **from/to String을 JDBC 전달** | 형식/timezone별 변환 오류 | 조회 오판 | typed time contract | invalid/timezone |
| 256 | 부분 구현 | P1 | 민감정보 | `Batch queries` | **job_parameters/error/step_log 원문 반환** | Secret·업무 데이터 노출 | 정보 유출 | schema masking/privileged reveal | secret params |
| 257 | 부분 구현 | P1 | Paging Gap | `Batch lists` | **jobs/schedules/instances/locks 등에 paging 부족** | 대규모 응답 폭증 | 운영 timeout | cursor paging | large tables |
| 258 | 재확인 필요 | P1 | Read side effect | `findGhostCandidates` | **조회가 detectGhostCandidates 실행** | GET 반복이 DB event 생성 가능 | audit 없는 변경 | detect command/scheduler 분리 | repeated GET |
| 259 | 부분 구현 | P1 | Ghost 판정 | `Ghost query` | **worker heartbeat 중심** | 정상 장기 Job 오판/실제 ghost 누락 | 오조치 | lease+execution+Spring 상태 | slow/restart |
| 260 | 부분 구현 | P0 | 운영 안전 | `releaseLock` | **active/만료/실행 상태와 무관한 강제 삭제 가능** | 정상 실행 lock 해제 | 중복 실행 | expired/ghost/approval 조건 | live lock |
| 261 | 부분 구현 | P0 | Fencing Gap | `Batch lock` | **monotonic fencing token 없음** | 과거 owner가 계속 write 가능 | split brain | generation token 검증 | old owner resume |
| 262 | 부분 구현 | P1 | Audit format | `Ghost/operation log` | **Map.toString before/after** | 구조 diff/마스킹 불가 | 감사 분석 불가 | canonical masked JSON | parse/diff |
| 263 | 실패 | P1 | Update 검증 | `updateScheduleEnabled` | **affected row/CAS 부족** | 경쟁 변경을 성공처럼 처리 가능 | lost update | version CAS/row count | concurrent edit |
| 264 | 실패 | P1 | 입력 은폐 | `parseDateOrToday` | **invalid date를 오늘로 대체** | 다른 날짜 시뮬레이션 오인 | 운영 판단 오류 | validation error | bad date |
| 265 | 실패 | P1 | Simulation 정확성 | `simulateSchedule` | **cron/timezone/window/pattern/holiday 실제 계산 미흡** | 실제 scheduler와 결과 불일치 | 잘못된 운영 계획 | 동일 evaluator 재사용 | simulation parity |
| 266 | 부분 구현 | P1 | 장애 은폐 | `findSpringBatchExecution` | **예외를 debug 후 null** | 미설정/장애 구분 불가 | 오판 | 상태형 response | JobExplorer fail |
| 267 | 부분 구현 | P1 | 동시성 | `registerJob upsert` | **version 없이 기존 정의 덮어쓰기** | 두 운영자 변경 손실 | lost update | versioned definition | concurrent edit |
| 268 | 재확인 필요 | P0 | Retry 안전 의심 | `Remote MBR command` | **POST command가 ServiceCall retry 대상 가능** | 비멱등 create/update/status 중복 가능 | 원장 중복 | operation idempotency 기반 retry | commit then timeout |
| 269 | 실패 | P0 | HTTP 결과 | `ServiceCallEngine` | **정상 반환을 항상 HTTP 200 기록** | 201/204 등 실제 metadata 유실 | 추적/정책 오판 | transport result status 전달 | 201/204 |
| 270 | 부분 구현 | P0 | Retry 정책 | `ServiceCallEngine` | **4xx/validation도 일반 예외로 retry 가능** | 불필요 반복·side effect | 중복/부하 | retryable classifier | 4xx/5xx |
| 271 | 부분 구현 | P0 | UNKNOWN 분류 | `isUnknownResult` | **class name timeout만 인식** | reset/partial write/read timeout 누락 | 결과 불명 미등록 | transport phase classifier | fault matrix |
| 272 | 실패 | P0 | UNKNOWN 내구성 | `registerUnknown` | **reconciliationPort null/실패여도 unknown 반환** | 복구 record 없는 결과 불명 | 영구 미복구 | product mode store 필수 | store down |
| 273 | 부분 구현 | P1 | Retry 자원 | `backoff Thread.sleep` | **caller thread 점유** | downstream 장애 시 pool 고갈 | cascading failure | nonblocking retry/bulkhead | load outage |
| 274 | 부분 구현 | P1 | Circuit/failover | `ServiceCallEngine` | **OPEN 시 다른 instance failover 정책 불명확/즉시 반환 가능** | 한 instance 장애가 service 실패 | 가용성 저하 | instance circuit-aware resolver | one open one healthy |
| 275 | 부분 구현 | P1 | 오류 정보 | `safeMessage` | **exception message 원문 저장** | URL/query/credential/PII 노출 | 로그 유출 | structured sanitizer | secret error |
| 276 | 부분 구현 | P1 | Lineage Header | `recordLineage` | **특정 Header 문자열 직접 사용** | 표준 명칭 불일치 시 transaction null | 추적 단절 | CpfHeaderNames 정본 사용 | propagation |
| 277 | 부분 구현 | P1 | Fallback 정책 | `CpfWebClient` | **Registry 장애 시 raw configured endpoint fallback** | 운영 policy/circuit/audit 우회 | 통제 우회 | fallback dev-only/상태 audit | prod registry down |
| 278 | 실패 | P0 | Timeout | `CpfWebClient fallback` | **raw .block() timeout 없음** | 무제한 blocking 가능 | thread 고갈 | 동일 timeout budget | hung endpoint |
| 279 | 부분 구현 | P1 | Header 전파 | `CpfWebClient overloads` | **일부 legacy 호출이 표준 headers 없이 수행** | transaction/channel/auth 누락 | 추적/권한 불일치 | 모든 overload 표준 request로 수렴 | overload parity |
| 280 | 부분 구현 | P1 | File 멱등성 | `duplicateKey` | **businessKey 없으면 operation\|remotePath** | 다른 파일 lifecycle 오판 가능 | 중복/누락 | endpoint+business identity+digest | same path files |
| 281 | 부분 구현 | P0 | File UNKNOWN | `CpfFileTransferEngine` | **reconciliationPort null이면 기록 없음** | 결과 불명 복구 대상 소실 | 영구 미확정 | product mode 필수 store | unknown null port |
| 282 | 부분 구현 | P0 | File 원자성 | `persistResult` | **전송 성공 후 history 실패 정책 부족** | 재시도 중복 전송 | 대외 중복 | durable state/reconciliation | history fail |
| 283 | 부분 구현 | P0 | File 경로 보안 | `File transfer request` | **path traversal/symlink sandbox 검증 근거 부족** | 임의 로컬 파일 접근 가능 | 보안 침해 | canonical root/allowlist | ../ symlink |
| 284 | 부분 구현 | P1 | Calendar fallback | `isBusinessDay` | **Override 없으면 주말만 기준** | 공휴일 누락이 영업일로 처리 | 배치 오실행 | coverage/completeness policy | missing holiday |
| 285 | 실패 | P1 | Calendar validation | `findRange` | **from>to/과도 범위 검증 부족** | store별 불일치/과도 조회 | 성능/오류 | service range validation | reversed range |
| 286 | 부분 구현 | P1 | Calendar propagation | `safePublish` | **event 실패 warning 후 계속** | 다른 노드 stale Calendar | 배치 날짜 불일치 | transactional outbox/convergence | publisher fail |
| 287 | 부분 구현 | P1 | Calendar SQL | `findRange setMaxRows` | **SQL LIMIT 없이 driver max rows** | 넓은 범위 DB 읽기 가능 | DB 부하 | Vendor paging SQL | query plan |
| 288 | 실패 | P0 | Calendar 감사 | `delete` | **actor를 검증하지만 Owner delete history 없음** | 삭제 주체/사유 Owner DB 미보존 | 감사 손실 | soft delete/change history | delete audit |
| 289 | 부분 구현 | P1 | Calendar Version | `delete event` | **expectedVersion+1 tombstone만 publish** | delete/recreate 순서 수렴 어려움 | stale resurrection | tombstone sequence/change log | delete/recreate |

## I. 별도 필수 제품 기능·품질 개선

> 아래 항목은 결함 수를 늘리기 위해 기존 문제를 다시 쪼갠 것이 아니다. 현재 Source에 상용 제품 수준으로 존재하지 않거나 골격만 있는 독립 제품 기능이다.

| GAP | 우선도 | 기능 | 필요한 이유 | 목표 상태 | Owner |
|---:|---|---|---|---|---|
| G01 | P0 | **통합 위험 Command Framework** | Drain, Lock 해제, Ghost, 재처리, 승인 실행, Break-glass가 각자 다른 안전장치를 사용한다. | Target snapshot, expectedVersion, idempotency, 영향 Preview, 승인, Audit, execute/reconcile 상태를 하나의 Operations Contract와 ADM UI로 제공한다. | cpf-core API + cpf-admin |
| G02 | P0 | **영속 Feature Flag/OpenFeature Control Plane** | 현재 property provider 골격만 있고 운영 변경 제품이 없다. | OpenFeature SPI, versioned registry, tenant/domain targeting, kill switch, 승인·감사·rollback·multi-instance sync·provider health를 구현한다. | cpf-core SPI + cpf-admin |
| G03 | P0 | **End-to-End Observability Plane** | Online annotation trace 수준이며 transport 전파와 운영 조회가 불완전하다. | W3C context, CPF transaction identity bridge, remote/message/file/batch propagation, metrics/log correlation, sampling, exporter health와 Trace Explorer를 제공한다. | cpf-core + transport Owners + cpf-admin |
| G04 | P0 | **Contract Registry와 Can-Deploy** | Flat 파일 diff만 존재한다. | OpenAPI/Event/File/Fixed/Batch snapshot을 서명·등록하고 환경별 Consumer/Provider Matrix로 can-deploy를 판정한다. | cpf-tools + cpf-admin |
| G05 | P0 | **Durable Multi-instance Cache/Config Convergence** | event cursor와 retry 일부가 process memory에 의존한다. | persistent offset, outbox, coalescing, desired version, node lag/readiness와 reconcile command를 제공한다. | cpf-common + cpf-admin |
| G06 | P0 | **관리자 Session·Key Security Center** | ADM/BZA token, rotation, device/session, 강제변경 정책이 분산되어 있다. | KMS key ring/kid rotation, token family reuse detection, idle/absolute timeout, revoke-all, privileged re-auth, security audit UI를 구현한다. | cpf-common security + ADM/BZA |
| G07 | P0 | **Cryptographic Audit Ledger와 Legal Hold** | DB row와 IMMUTABLE_YN만으로 변조 탐지와 법적 보존이 부족하다. | Hash chain/signature, append-only privilege, WORM archive, legal hold, retention version과 verification report를 제공한다. | cpf-admin audit |
| G08 | P1 | **Schema Lifecycle·Capacity Analyzer** | 대형 table의 version/partition/retention/index 검증이 분산되어 있다. | Canonical data dictionary, owner/consumer map, online migration precheck, index/query plan, partition/retention/capacity forecast와 rollback safety 분석을 자동화한다. | cpf-tools DB + DB Owners |
| G09 | P1 | **Typed Admin Frontend SDK와 Feature Manifest** | API 문자열·any/Map과 정적 Route가 많다. | OpenAPI generated TS client, typed errors/pages, feature/menu/button/API manifest, deep link와 Browser contract gate를 제공한다. | ADM/BZA frontend + build tools |
| G10 | P1 | **Role 기반 Browser/E2E·Accessibility·Visual Gate** | 최신 Commit의 실제 Frontend Runtime Evidence가 없다. | ADM/BZA 역할별 Route·Button·401/403/409/500·keyboard·WCAG·responsive·visual regression·console error 0 Gate를 CI에 추가한다. | cpf-tools verification + ADM/BZA |
| G11 | P1 | **Controlled Failure Verification Harness** | 현재 단순 sleep/throw injector만 있다. | DB deadlock/disconnect, broker duplicate/delay, HTTP partial/timeout, file unknown, worker lease loss, cache/event failure를 선언형 scenario로 실행하고 Evidence를 만든다. | cpf-tools + runtime SPIs |
| G12 | P1 | **통합 운영 Work Queue와 Deep Link** | Dashboard/Incident/Notification/Recovery가 실제 처리 화면과 분리돼 있다. | 권한 기반 pending queue, SLA/priority, owner, deep link, acknowledge/assign/escalate와 처리 Evidence를 제공한다. | cpf-admin/BZA |

## 3. 다음 작업 요청서 권장 분할

### R1 — Build·보안·False Success 즉시 차단
- Vue SFC/Route/Frontend CI, ADM API False Success, BZA 강제 비밀번호 변경·만료 집행
- Audit/Log/Config/Download 민감정보, Service Call 비멱등 Retry와 UNKNOWN 내구성

### R2 — 다중 인스턴스·동시성·복구
- Cache persistent offset/outbox/convergence, MBR idempotency/history
- Batch lock fencing/Ghost, Calendar propagation, Admin optimistic locking

### R3 — Release·계약·상용화
- 표준 SBOM/License/CVE/Signature/SLSA, Contract Registry/Can-deploy
- 전체 OTel propagation, Schema lifecycle/capacity, Role 기반 Browser E2E

## 4. 완료 금지 조건

- 전체 Gradle `clean test assemble qualityGate`와 ADM/BZA Frontend `verify`를 실행하지 않았다.
- MariaDB fresh install, upgrade, rollback/re-apply, checksum/drift를 실행하지 않았다.
- P0 항목의 정상·오류·부분 실패·동시성 Runtime Evidence가 없다.
- ADM/BZA Role별 Browser·Button·API 권한 Matrix가 없다.
- 민감정보가 API, Audit, Log, Trace, CSV, 다운로드와 Evidence에서 제거됐다는 검증이 없다.
- Local/Remote, Single/Multi-instance 결과가 동일 계약으로 검증되지 않았다.
- Release License/CVE/Signature/Provenance가 실제 실패 Gate로 동작하지 않는다.
- Codex 완료 보고와 최신 Commit Source/Test/SQL/Evidence가 일치하지 않는다.

## 5. 검수 방법과 한계

- 최신 master 파일을 GitHub connector로 직접 조회했다.
- 문서의 완료 표기를 완료 근거로 사용하지 않았다.
- 직접 실행하지 않은 Build/DB/Browser/Multi-instance 검증을 성공으로 기록하지 않았다.
- 동일 원인은 하나의 수정 단위로 묶었으며 Owner·위험·수정 방식이 다른 경우에만 독립 항목으로 분리했다.
- 다음 Push 후 이 ID별 상태를 다시 판정해야 한다.