# CPF ADM 개발자 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 문서 콘텐츠 기준 Commit: `64049044956924032360fa80be83b5e37c64f828` (`08_03`)
> 기준일: `2026-08-07 Asia/Seoul`
> 기준 Commit: **현재 `master` 구현 기준 문서**. Product Surface·Starter·Tool·EDU 식별자는 아래 기준 Commit의 Source 정본과 대조한다.

| 항목 | 내용 |
|---|---|
| 주 독자 | 고객 업무 개발자·ADM 연동 개발자·Frontend/Backend 통합 개발자 |
| 이 문서로 완료할 일 | 완성된 ADM 제품에 고객 업무 Owner의 Query·Command·승인·감사·복구 계약을 Same-JVM/Remote·OpenAPI·Generated Client·Route·화면·Browser Test로 연결한다. |
| 완료 판정 | 독자가 다른 문서나 Source 역분석 없이 정상 흐름, 오류, 부분 실패, 복구, 감사, 운영 인계를 끝낼 수 있어야 한다. |
| 상태 표현 | 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요만 사용한다. |
| 정합성 원칙 | 실제 Class·API·SQL·Config·화면·Permission·Script·Test를 우선하고 문서와 양방향으로 추적한다. |

## 1. ADM 연동의 경계

ADM은 고객 업무 상태를 대신 소유하지 않는다. ADM Backend는 Owner Query/Command Contract를 호출하고 Frontend는 해당 계약을 사용자 작업으로 표현한다.

금지:

- ADM DB에서 업무 상태 직접 DML.
- Frontend가 Raw URL/임의 DTO로 Owner를 우회.
- Toast만 보고 성공 확정.
- Timeout 뒤 동일 Button Blind Retry.

## 2. 연동 결과물

- Owner Query Port.
- Owner Command Port.
- Same-JVM Adapter.
- Remote Adapter.
- ADM Controller.
- OpenAPI Operation/Schema/Error.
- Generated Client.
- Route/Menu/Risk/Feature Flag/Operation Registry.
- Vue Page/Component.
- Menu/Button/API Permission·Data Scope·Masking.
- Browser Contract/Fault Test.
- 04 운영자 매뉴얼의 화면 카드.

## 3. Owner Query

Query 응답에 최소 포함:

- 기준시각.
- Owner/Source.
- 상태·Version.
- 검색/페이지 정보.
- Masking 여부.
- 운영 추적 ID.

ADM은 Query 결과를 별도 업무 사실로 재해석하지 않는다.

## 4. Owner Command

Command 입력:

- Target ID.
- Expected Version.
- Reason.
- Idempotency Key.
- Approval ID(필요 시).
- Actor/Security Context.

Command 결과:

- Operation ID.
- 접수/진행/확정 상태.
- 새 Version 또는 최신 Snapshot.
- UNKNOWN_RESULT일 때 Status Query 경로.

## 5. Same-JVM Adapter

Local 호출이라고 Permission/Version/Idempotency/Audit를 생략하지 않는다. 네트워크만 없을 뿐 계약 의미는 Remote와 같다.

## 6. Remote Adapter

추가 책임:

- Service Identity/Audience.
- Trace/Transaction Header.
- Serialization.
- Connect/Read/Overall Timeout.
- Error Mapping.
- Target Attempt.
- Response Loss 판정.

Dispatch 뒤 응답 유실은 `UNKNOWN_RESULT`와 Operation 조회 경로로 변환한다.

## 7. Controller

Controller는 HTTP 형식, Authentication Context, 입력 Validation, Error Mapping을 담당한다. 상태 전이는 Owner가 판단한다.

위험 Command에서 다음이 빠지지 않게 한다.

- 403 Permission/Scope.
- 409 Version/Idempotency Conflict.
- 422 정책 Validation.
- 202/Operation 진행 또는 UNKNOWN.
- 5xx Dependency/Error.

## 8. OpenAPI·Generated Client

1. 고유 Operation ID.
2. Request/Response/Error Schema.
3. Required Header/Version/Reason/Approval.
4. Nullable/Enum/Format.
5. Error Code/Retry 의미.
6. Generated Client 생성.
7. TypeScript/Java Compile.
8. Frontend Raw URL 제거.
9. Route Registry Operation ID 대조.

## 9. Route·Menu Registry

Route 한 건은 최소 다음 계약을 가진다.

| 필드 | 의미 |
|---|---|
| routeId | 안정된 화면 식별자 |
| path | Browser Path |
| menuId | Menu Permission 연결 |
| label | 사용자 표시명 |
| group | 메뉴/업무 영역 |
| riskLevel | 변경 조치 위험도 |
| featureFlag | 노출 조건 |
| expectedOperationIds | 화면이 사용할 Backend 계약 |
| component | 실제 Page |

## 10. 화면 구성

모든 화면에서 정의할 것:

- 검색 Field/기본값/범위.
- Table Column/Sort/Paging.
- 상세 Field/Masking.
- Button/활성 조건.
- Permission·Data Scope.
- Reason/Approval/Expected Version.
- 실행 중/성공/실패/Unknown/Partial 상태 표현.
- Timeout/응답 유실 후 Status Query.
- Audit/Trace 이동.

## 11. 위험 Button 활성 조건

Button은 단순 Permission만으로 활성화하지 않는다.

1. 현재 상태가 허용 전이인지.
2. 사용자가 Menu/Button/API Permission을 가지는지.
3. Target이 Data Scope 안인지.
4. 최신 Version인지.
5. Reason이 유효한지.
6. 필요한 Approval이 Approved이고 Snapshot이 같은지.
7. 기존 미종결 Operation이 없는지.
8. Feature Flag/환경 정책이 허용하는지.

## 12. Expected Version Conflict

409가 오면:

1. 화면의 편집 값 보존.
2. 최신 상세 재조회.
3. Changed Field 비교.
4. 사용자에게 재판단 요청.
5. 새 Version으로 새 Intent를 제출.

Version만 교체해 자동 재호출하지 않는다.

## 13. Idempotency와 Browser Ledger

Double Click, Network Retry, Browser Refresh에서도 같은 의도를 중복 실행하지 않게 한다. 화면은 Command Intent마다 Key/Hash/Operation을 관리하고 완료·미종결 상태를 재조회할 수 있어야 한다.

## 14. UNKNOWN_RESULT·Reconciliation

화면은 `요청 실패`와 `결과 불명`을 같은 오류 Toast로 처리하지 않는다.

- Dispatch 전 실패: 재입력/재호출 가능 여부 제시.
- Dispatch 후 결과 불명: Operation 조회 Button 제공.
- Owner/Target 대사 결과를 Timeline으로 표시.
- 재처리/보상/종결은 별도 Permission/Reason/Approval.

## 15. Permission·Masking·Reason·Approval·Audit

Frontend에서 숨겼다고 Backend 권한을 생략하지 않는다. Backend Decision을 정본으로 하고 Frontend는 사용성을 위해 같은 정책을 표현한다.

원문 조회/Export/Secret/Break-glass는 조회 자체를 Audit한다.

## 16. 부분 적용과 Rollback

다중 Target 조치는 Target별 결과를 보존한다.

- 성공 Target을 전체 실패로 되돌리지 않는다.
- 실패 Target만 재적용할지 전체 LKG로 Rollback할지 정책을 보여 준다.
- Desired/Observed Version과 Checksum을 표시한다.

## 17. Browser Test

필수 Scenario:

- Route/Menu 노출.
- Permission 403.
- Data Scope 제한.
- Masking/Unmask.
- Validation Error.
- 409 Version Conflict.
- Double Click/Idempotency.
- Read Timeout.
- Command Response Loss/UNKNOWN.
- Approval Snapshot Drift.
- Partial Apply.
- Rollback/Reconcile.
- Audit/Trace 이동.

## 18. 고객 업무 화면 연결 통합 실습

1. Owner Query/Command Contract 확정.
2. Local Adapter 연결.
3. Remote Adapter 연결.
4. Controller/OpenAPI 작성.
5. Generated Client 생성.
6. Route/Menu/Risk/Feature Flag 등록.
7. 검색/상세/Command 화면 작성.
8. Permission/Scope/Masking 적용.
9. 409/UNKNOWN/Partial을 재현.
10. Operation/Audit로 복구.
11. 04 운영자 카드 작성.

## 19. 운영 인계

Route, Menu, Operation ID, Permission, Scope, Masking, 위험 Button, Approval, 상태, 오류, Retry/Reconcile/Rollback, Audit, Browser Test를 04와 운영팀에 인계한다.

## 20. 완료 체크리스트

- [ ] Owner Contract를 우회하지 않는다.
- [ ] Local/Remote 의미가 같다.
- [ ] OpenAPI/Generated Client/Route가 일치한다.
- [ ] 모든 위험 Button 조건이 명확하다.
- [ ] 409/UNKNOWN/PARTIAL UI가 있다.
- [ ] Permission/Scope/Masking/Approval/Audit가 Backend와 일치한다.
- [ ] Browser Fault Test가 있다.
- [ ] 04 운영 매뉴얼과 양방향 연결된다.

## 21. Frontend 기술 경계

Frontend는 Route Registry와 Generated Client를 단일 API 경계로 사용한다. Raw `fetch` 또는 임의 URL 호출은 승인된 bootstrap/mutator 경계를 제외하고 사용하지 않는다.

필수 UX:

- loading/empty/error/retry.
- search/paging/sort/detail.
- deep link/404.
- 403.
- session expiry.
- browser history.
- responsive/keyboard/accessibility.

## 22. BFF·Session 주의

Browser Storage, URL, DOM, response body, console/log에 Access Token/Refresh Token/Session ID를 노출하지 않는다. Session fixation, rotation, timeout, concurrent session, privilege revocation, force logout을 테스트한다.

## 23. 변경 영향 체크

Route 변경 시 Backend Controller/OpenAPI/Generated Client/Permission/Frontend/Browser Test/04 매뉴얼을 한 변경 단위로 대조한다. Operation ID가 바뀌면 Consumer와 Audit 조회도 함께 확인한다.

## 24. ADM 연동 Product Surface

ADM은 Owner DB를 직접 수정하지 않는다. 조회는 Owner Query, 변경은 Owner Command, 비동기/응답 유실은 Owner Status/Reconcile을 통해 닫는다. Same-JVM과 Remote Adapter는 DTO·Validation·Error·Expected Version·Idempotency·Audit 의미가 같아야 한다.

화면을 추가하기 전에 기존 Route/Component가 같은 운영 질문을 이미 해결하는지 확인한다. 고객 전용 Route는 마지막 선택이다.

## 25. EDU-ADM 17개 전수 지도

| ID | 예제 | 핵심 확인 | 장애·복구 관점 |
|---|---|---|---|
| `EDU-ADM-01` | 기존 ADM 기능 재사용 판단 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-02` | 고객 업무 조회 연동 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-03` | 안전한 운영 조치 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-04` | 승인 필요한 위험 조치 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-05` | 비동기 작업·응답 유실 | operation/attempt 원장과 Owner 결과 대사 | 확인 전 재실행 금지 → reconcile → 필요한 경우 compensation/확정 |
| `EDU-ADM-06` | 부분 성공·대상별 복구 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-07` | 고객 전용 화면 추가의 마지막 선택 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-08` | 권한·데이터 범위·Masking·사유 입력 연동 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-ADM-09` | Expected Version 충돌 화면·재조회·재적용 | version/idempotency/lease 소유권과 경쟁 요청 결과 | stale writer 차단, 최신 상태 재조회 후 재판단 |
| `EDU-ADM-10` | 대상 일괄 조치·부분 성공·결과 파일 | temp/final·checksum·bounded streaming·row/result 상태 | partial target 격리/삭제, 재개 또는 failed-only 재처리 |
| `EDU-ADM-11` | 설정·기능전환·유지보수 창 운영 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-12` | Incident·Recovery Center 종단간 복구 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-13` | 감사 증적·다운로드·승인 반출 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |
| `EDU-ADM-14` | Topology·Health·Capacity Drill-down | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-15` | Log·Trace·Transaction 상관 검색 | 입력·상태 전이·정상 결과·Log/Metric/Trace/Audit | 실패 단계 분류 → retry/reconcile/rollback 중 허용 경로 선택 |
| `EDU-ADM-16` | 알림 Acknowledge·Escalation·교대 인계 | messageId/key/order/ACK·consumer idempotency·DLQ | 중복 소비 차단, replay 승인, 미종결 원장 대사 |
| `EDU-ADM-17` | Browser 세션 만료·재로그인·위험 조치 안전성 | actor/permission/data scope/masking/reason/audit | fail-closed, credential 회수/세션 폐기, 감사로 정상화 확인 |

공통 실행 역할: `CPF_EDU_ADM_DEVELOPER`. 각 ID의 **정확한 requiredFields·businessStates·exceptionScenarios·requiredVerification·handler/source/test/timeout**은 기준 Commit의 `cpf-reference/src/main/resources/edu/manual-135-catalog.json`과 동일하게 유지한다. 매뉴얼에서는 그 계약을 업무 절차에 연결하며, 임의 필드를 추가하지 않는다.

### 25.1 안전한 위험 조치 UI 계약

위험 Button은 단순 Permission 하나로 활성화하지 않는다. 최소 `Permission + Data Scope + 현재 상태 + 최신 Expected Version + Reason + 필요한 Approval + 미종결 Operation 부재`를 평가한다. 클릭 후 응답 유실 시 Button을 다시 누르지 않고 Operation/Owner 상태를 조회한다.

### 25.2 Generated Client와 Drift

OpenAPI exact-SHA → Generated Client → Route/Component 사용이 하나의 Chain이어야 한다. Server Contract가 바뀌었는데 수동 DTO/raw fetch가 남으면 Gate 실패다. 403/409/422/5xx/Timeout을 같은 Toast로 뭉개지 않는다.

### 25.3 Browser Fault Test

세션 만료, 403, Expected Version 409, Command response loss, 부분 성공, Approval expiry, Target NACK를 Browser에서 재현한다. 재로그인 후 이전 Command를 자동 재전송하지 않고 Status/Reconcile을 먼저 확인한다.








<!-- R17-EDU-ADM-DETAIL-BEGIN -->
## 25A. EDU-ADM 전수 실행 카드 — 17개

아래 카드는 `manual-135-catalog.json`의 ID를 잃지 않고 매뉴얼 업무 절차로 연결한다. **정확한 필드명·상태·Handler·Source·Test는 같은 ID의 정본 값을 사용하며 문서가 별도 제2 정본을 만들지 않는다.** 대신 독자는 각 ID에서 무엇을 준비하고 무엇을 실패시켜 어떻게 정상화를 판정하는지 이 절만으로 이해할 수 있어야 한다.

### EDU-ADM-01 — 기존 ADM 기능 재사용 판단

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-01/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-01` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-02 — 고객 업무 조회 연동

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-02/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-02` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-03 — 안전한 운영 조치

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-03/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-03` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-04 — 승인 필요한 위험 조치

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-04/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-04` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-05 — 비동기 작업·응답 유실

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-05/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-05` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-06 — 부분 성공·대상별 복구

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-06/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-06` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-07 — 고객 전용 화면 추가의 마지막 선택

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-07/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-07` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-08 — 권한·데이터 범위·Masking·사유 입력 연동

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-08/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-08` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-09 — Expected Version 충돌 화면·재조회·재적용

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; expected version; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-09/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; stale version/경쟁 갱신.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-09` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-10 — 대상 일괄 조치·부분 성공·결과 파일

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; file/checksum/size; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-10/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; partial file·checksum mismatch·disk full·중단.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-10` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-11 — 설정·기능전환·유지보수 창 운영

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-11/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-11` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-12 — Incident·Recovery Center 종단간 복구

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-12/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-12` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-13 — 감사 증적·다운로드·승인 반출

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-13/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-13` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-14 — Topology·Health·Capacity Drill-down

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-14/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-14` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-15 — Log·Trace·Transaction 상관 검색

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-15/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-15` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-16 — 알림 Acknowledge·Escalation·교대 인계

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; messageId/key/schema version; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-16/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; duplicate/late ACK/broker outage/DLT.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-16` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.

### EDU-ADM-17 — Browser 세션 만료·재로그인·위험 조치 안전성

- **역할:** `CPF_EDU_ADM_DEVELOPER`.
- **준비/입력:** 정본 `requiredFields` + business key + trace/correlation 식별자; actor/role/data scope/reason; route/operationId/expected version/reason.
- **실행:** `POST /api/reference/edu-capabilities/EDU-ADM-17/executions`; 실행 전 같은 ID의 `requiredFields`를 채우고 고객 업무 전환 시 임의 필드를 추가하지 않는다.
- **정상 상태:** 정본 `businessStates`의 시작→처리→종결 상태.
- **정상 판정:** 정본 `requiredVerification` + Log/Metric/Trace/Audit 상관관계; 버튼 권한·실제 API status. HTTP 2xx 하나만으로 종결하지 않는다.
- **Fault:** 정본 `exceptionScenarios`의 negative/failure case; 401/403/권한 회수/secret expiry.
- **복구:** 화면 재시도 전에 operation/attempt/audit로 실제 결과를 먼저 확인.
- **Source/Test Trace:** `cpf-reference/src/main/resources/edu/manual-135-catalog.json`의 `EDU-ADM-17` 항목에 기록된 `handlerClass`, `sourcePath`, `resourceContract`, `tests`, `consumerBinding`, `timeoutSeconds`를 한 세트로 검증한다.
- **실무 전환:** Reference 전용 Sandbox/Seed/이름은 고객 Owner로 바꾸되 idempotency/version/lease/attempt/audit/recovery 의미는 삭제하지 않는다.
<!-- R17-EDU-ADM-DETAIL-END -->

## 26. ADM 개발 인계 Gate

- ADM 17/17 EDU가 03 매뉴얼 Anchor에 존재.
- Query/Command/Status/Reconcile Owner가 명확.
- Same-JVM/Remote 동등성.
- Generated Client/OpenAPI exact-SHA.
- Route·Permission·Button 활성 조건.
- 응답 유실·부분 성공·승인·감사 Browser Test.
