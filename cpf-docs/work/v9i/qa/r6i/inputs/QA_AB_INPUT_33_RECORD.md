# CPF QA A+B R6S12 통합 상세 검수 결과

> QA A 결과와 이번 QA B 독립 검수를 한 문서에 취합한 통합 QA 입력자료다. Repository Merge·Commit·Push·최종 개발지침 작성은 수행하지 않았다.

## 0. 식별정보

- 생성 시각(KST): `2026-08-07T11:13:36+09:00`
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 exact SHA: `77db10ad9aff44ee422795080fb2e96b364c9d65` (`08_01`)
- Parent SHA: `28f823a18eca859cebdbceb382029f595cdf490c`
- QA A 역할: Core Architecture·Source·Approval·Security·Concurrency·DB/Runtime·Evidence
- QA B 역할: ADM·Frontend·OpenAPI·Generator·Starter·EDU·Documentation·Release/Evidence
- 사용자 추가 집중 요청: ADM 63개 메뉴별 CRUD·실시간·실제 Consumer, EDU 135건 수량·Source·ADM 연동
- Git Write/Merge/Commit/Push/Branch/Tag/PR/Delete: **수행하지 않음**

## 1. 최종 판정

**미통과 — Release Blocked**

- QA A Finding: **19건** — P0 6 / P1 12 / P2 1
- QA B Finding: **14건** — P0 6 / P1 8 / P2 0
- 통합 QA Record: **33건** — 중복·상호보강 관계는 아래 Crosswalk에 표시

### 핵심 결론

- **ADM Route 수량은 63개로 매뉴얼과 Registry가 일치한다.**
- Backend OpenAPI/Generated Client에는 Registry가 선언한 329개 unique Operation ID가 존재한다.
- 그러나 Component Consumer 계약은 274개 unique Operation만 사용하며, **12개 메뉴에서 55개 기능이 화면 호출 경로와 끊겨 있다.**
- Browser Interaction 원장은 59개 기준이라 신규 Critical 4개 메뉴가 제외되고 59개 전부 검증상태가 미검증이다.
- WebSocket/SSE/주기 Polling 및 실제 Browser 실시간 Evidence가 없어 운영 실시간 기능 완료를 인정할 수 없다.
- **EDU는 정확히 135건**이며 Catalog·Package Index 수량 요건은 충족한다.
- 그러나 **135건 전부 미검증**이고, 675개 per-ID Test는 실제 Consumer가 아닌 deterministic double을 사용하는 공통 Wrapper다.
- EDU-ADM 17건은 실제 cpf-admin API/UI와 연동되지 않고 generic REF DB record를 사용하며 매뉴얼 제목과 catalog 제목도 17/17 불일치한다.

## 2. QA 방법과 검수 경계

- 최신 master를 GitHub Connector로 재확인하고 exact SHA를 고정했다.
- ADM 운영자 매뉴얼의 63 Route와 `routes.ts`의 63 Route를 exact path로 비교했다.
- Route Registry expected operations와 Component Consumer에서 생성된 `adm-route-operation-contract.ts`를 전수 비교했다.
- OpenAPI operation contract와 generated `cpf-api.ts`에 모든 Registry operation ID가 존재하는지 정적 검산했다.
- 대표 CRUD Source를 직접 확인했다: Business Calendar, Config, Code, Service Registry, Notification, Feature Flag, Approval/Integration Closure.
- Browser Test source와 Release workflow를 확인하고 Mock/Release mode 차이를 검수했다.
- EDU catalog 135건을 JSON으로 전수 파싱해 ID·sourcePath·resourceContract·test path·consumer·status를 검산했다.
- EDU Runtime Controller/Service/Registry/Consumer, 대표 Handler, Batch Job, Gateway Simulator, Process Script, 공통 Test 기반을 Source 단위로 확인했다.
- Java25·Gradle9.1·DB3·실제 Browser·Broker 환경은 없어 Runtime PASS로 기록하지 않았다.

## 3. QA A+B Crosswalk

| QA B FindingQA A 연계관계 |                        |                                              |
| --------------------- | ---------------------- | -------------------------------------------- |
| QA-B-R6S12-001        | QA-A-R6S12-014,015,016 | OpenAPI/Consumer/HTTP 경계 확장                  |
| QA-B-R6S12-002        | QA-A-R6S12-003,007     | Browser Gate 구조 결함을 63 Route 기준으로 확장         |
| QA-B-R6S12-003        | QA-A-R6S12-003,007     | Browser Runtime 미검증과 별개로 실시간 freshness 결함 추가 |
| QA-B-R6S12-004\~013   | 별도                     | EDU 135·ADM 연동·실제 Consumer 신규 검수             |
| QA-B-R6S12-014        | QA-A-R6S12-005,006     | Fresh build/publication/Evidence 미검증 상호 보강   |

## 4. QA B Finding 상세

### QA-B-R6S12-001 [P0] ADM Route Registry와 실제 Component Consumer 사이 12개 메뉴·55개 Operation Drift

- 연결 Requirement: `FDEV-014,FDEV-016,FDEV-017,FDEV-020`
- Source: `cpf-admin/frontend/src/app/routes.ts; cpf-admin/frontend/src/generated/adm-route-operation-contract.ts`
- 확인 Evidence: 63개 Route가 414개 operation reference(329 unique)를 선언하지만 Component Consumer 계약은 359개(274 unique)만 관측한다. 12개 메뉴에서 총 55개 선언 Operation이 실제 화면 Consumer에서 빠진다.
- 영향: 메뉴와 OpenAPI/Generated Client가 존재해도 상세·CRUD·복구·다운로드·권한 기능이 화면에서 호출되지 않는 false-complete가 된다.
- 필수 보완: Registry expectedOperationIds를 실제 Component 호출과 일치시키고, 빠진 기능은 메뉴별 UI/handler/권한/오류/감사까지 연결한다. 단순 Registry 축소로 해결하지 않는다.
- 필수 검증: 63개 Route 각각 모든 required query/mutation operation을 실제 Browser interaction으로 발생시키고 X-Cpf-Operation-Id와 Backend 상태 변화를 검증한다.
- QA B 판정: **미통과**

### QA-B-R6S12-002 [P0] ADM Browser Interaction 원장이 59개 기준으로 고정되어 신규 Critical 4개 Route를 검수하지 않음

- 연결 Requirement: `FDEV-014,FDEV-017,FDEV-022`
- Source: `cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv; cpf-admin/frontend/e2e/adm-route-contract.spec.ts; routes.ts`
- 확인 Evidence: Router/운영자 매뉴얼은 63개 Route인데 E2E는 `routes.length !== 59`를 강제한다. `/featureFlags`, `/integrationClosure`, `/openApiOperations`, `/resiliencePolicies`가 Interaction Matrix에서 제외된다. Matrix의 59개 행도 verification\_status가 전부 미검증이다.
- 영향: 가장 최근 추가된 HIGH/CRITICAL 운영 메뉴가 Route Browser Gate 밖에 있어 UI가 깨져도 기존 E2E baseline이 통과할 수 있다.
- 필수 보완: Interaction Matrix를 canonical 63 Route에서 생성하고 신규 Route 4개를 포함한다. 고정 숫자 대신 canonical registry exact set을 비교한다.
- 필수 검증: 63/63 deep link, menu, query/mutation interaction, permission, error matrix, a11y, responsive, actual backend state.
- QA B 판정: **미통과**

### QA-B-R6S12-003 [P1] ADM 실시간 기능이 자동 갱신이 아니라 수동 조회 중심이며 Runtime 증명이 없음

- 연결 Requirement: `FDEV-014,FDEV-017`
- Source: `cpf-admin/frontend/src/**; Dashboard/Topology/Capacity/Batch/Gateway/Notification pages`
- 확인 Evidence: 정확 검색에서 WebSocket·EventSource·setInterval 사용이 발견되지 않았고 대표 운영 화면은 버튼/초기 load 기반이다. Dashboard·Batch Runtime·Gateway Health·Notification과 같은 실시간성 요구 메뉴에 실제 Push/SSE/Polling 및 reconnect/backoff 증거가 없다.
- 영향: 운영 화면이 stale 상태를 표시해 장애·Lease·Health·DLQ·실행 상태를 늦게 판단할 수 있다.
- 필수 보완: 메뉴별 freshness SLA를 정의하고 SSE/WebSocket 또는 bounded polling, visibility pause, reconnect/backoff, stale 표시를 구현한다.
- 필수 검증: 상태 변경 자동 반영, 연결 단절·재연결, stale age, tab background/foreground, 다중 인스턴스.
- QA B 판정: **미통과**

### QA-B-R6S12-004 [P0] EDU 예제 수는 정확히 135건이나 QA 검증 완료는 0/135

- 연결 Requirement: `FDEV-018,SAMPLE-REF,SAMPLE-EDU`
- Source: `cpf-reference/src/main/resources/edu/manual-135-catalog.json; PACKAGE_INDEX.md`
- 확인 Evidence: featureCount=135, Requirement ID·sourcePath·resourceContract는 모두 unique다. 분포는 ONLINE 45/BATCH 30/ADM 17/OPS 15/BACKOFFICE 14/GATEWAY 14다. 그러나 135건 전부 verificationStatus=미검증이고 동일 Evidence 문구로 Java25/Gradle/Runtime pending을 기록한다.
- 영향: 수량 요건은 충족하지만 실제 실행·오류·복구·권한·운영 품질은 하나도 QA 통과하지 않았다.
- 필수 보완: 135개 ID별로 개발 상태와 검증 상태를 분리하고 실제 Consumer/Runtime Evidence를 생성한다.
- 필수 검증: 135/135 exact ID runtime matrix, 정상·Negative·Concurrency·Recovery·Security, official DB/vendor/infra.
- QA B 판정: **미통과**

### QA-B-R6S12-005 [P0] EDU 675개 Test 파일이 개별 업무 Consumer를 실행하지 않는 공통 Double 기반 Wrapper

- 연결 Requirement: `FDEV-018,FDEV-022,SAMPLE-EDU`
- Source: `AbstractManualEdu*Test.java; AbstractManualEduTestSupport.java; TestEduBusinessConsumers.java; per-ID *Test.java`
- 확인 Evidence: 각 ID의 5개 Test는 handler()만 교체하는 1\~5줄 subclass다. 공통 TestSupport는 FileEduOperationRepository와 `TestEduBusinessConsumers.registry()`를 사용하고 모든 ConsumerType을 DeterministicConsumer로 대체한다. 실제 JDBC/HTTP/PROCESS/SPRING\_BATCH/REFERENCE\_GATEWAY를 호출하지 않는다.
- 영향: 135개 테스트가 존재해도 실제 SQL, HTTP, Script, Batch Job, Gateway, 보안 경계를 검증하지 않는 synthetic false-green이다.
- 필수 보완: 공통 상태머신 Test는 유지하되 ID별 실제 Consumer Integration/Runtime Test를 별도로 작성한다.
- 필수 검증: real JdbcTemplate+DB3, actual HTTP simulator, allowlisted process, actual 30 Spring Batch jobs, Gateway simulator/actual contract, failure injection.
- QA B 판정: **미통과**

### QA-B-R6S12-006 [P0] EDU-ADM 17건이 실제 cpf-admin 연동 예제가 아니라 공통 REF DB 기록으로 동작

- 연결 Requirement: `FDEV-014,FDEV-018,SAMPLE-EDU`
- Source: `manual-135-catalog.json; EduAdm01~17Handler; JdbcEduBusinessConsumer; ADM 개발자 매뉴얼 부록 B`
- 확인 Evidence: EDU-ADM 17건 모두 owner=cpf-reference이며 15건 JDBC\_COMMAND, 2건 JDBC\_QUERY로 동일 `CPF_EDU_BUSINESS_RECORD`를 사용한다. cpf-admin Controller/OpenAPI/Generated Client/Route/Browser를 실제 호출하지 않는다. 개발자 매뉴얼의 EDU-ADM-01\~17 제목과 catalog 제목도 17/17 불일치한다.
- 영향: Owner Port→Controller→OpenAPI→Generated Client→Route→Browser 연동 교육이라는 문서 요건을 generic record state 변경으로 대체한다.
- 필수 보완: 17개 EDU-ADM ID를 매뉴얼 정본과 재정렬하고 실제 cpf-admin API/Generated Client/UI Consumer를 사용하는 end-to-end 예제로 구현한다.
- 필수 검증: Same-JVM/Remote adapter, 401/403/409/202/UNKNOWN, masking/export, approval, partial rollback, actual Playwright.
- QA B 판정: **미통과**

### QA-B-R6S12-007 [P0] EDU Runtime 권한이 인증 주체가 아닌 호출자 제공 Header를 신뢰

- 연결 Requirement: `FDEV-013,FDEV-018,SAMPLE-EDU`
- Source: `EduCapabilityController.java; cpf-reference/build.gradle; ReferenceApplication.java`
- 확인 Evidence: execute는 X-Cpf-Actor-Id/Roles/Data-Scope 값을 그대로 EduExecutionCommand에 넣는다. retry/reconcile/compensate/cancel은 actor header와 reason만 받는다. cpf-reference build에는 Spring Security starter가 없고 ReferenceApplication에도 인증 Filter 설정이 확인되지 않는다.
- 영향: 호출자가 관리자 Role/Scope/Actor를 위조해 교육 Runtime의 상태 변경·복구·보상을 실행할 수 있다.
- 필수 보완: 인증된 Principal/Authority/Scope에서 서버가 Actor·Role·Scope를 생성하고 외부 입력 Header는 무시하거나 신뢰 경계 Filter에서 서명 검증한다.
- 필수 검증: forged role/scope/actor 거부, retry/reconcile/compensate/cancel 권한, tenant isolation, audit principal.
- QA B 판정: **미통과**

### QA-B-R6S12-008 [P1] EDU PROCESS 예제가 실제 제품 실행보다 Sandbox·파일 존재 확인에 머묾

- 연결 Requirement: `FDEV-018,SAMPLE-REF,SAMPLE-EDU`
- Source: `invoke-reference-edu.ps1; EduDev01Handler; EduOps01~15Handler`
- 확인 Evidence: DEV-01은 빈 src 폴더와 manifest를 만들고 `generatedDomainLinkedToEdu=false`를 기록한다. OPS-04/DEV-14는 DB3 lifecycle 파일 존재와 Hash만 확인하며 DB를 실행하지 않는다. 배포·DR·Kafka·보안 예제 다수는 sequence/state JSON을 만드는 SANDBOX다.
- 영향: 실제 Generator→build→runtime→remove/regenerate, DB3 migration/rollback, broker, DR, deployment를 교육한다는 최상위 요건과 다르다.
- 필수 보완: Sandbox임을 명확히 분류하고 제품 완료 Evidence로 사용하지 않는다. 필수 EDU는 실제 제품 tool/runtime을 호출해 성공·실패·복구를 재현한다.
- 필수 검증: fresh clone generator lifecycle, live DB3, broker, deployment rollback, process kill, artifact hash.
- QA B 판정: **미통과**

### QA-B-R6S12-009 [P1] EDU Process Consumer가 부모 환경변수를 유지하고 Payload 임시파일에 민감정보를 기록

- 연결 Requirement: `FDEV-013,FDEV-018`
- Source: `ProcessEduBusinessConsumer.java`
- 확인 Evidence: ProcessBuilder.environment()을 clear하지 않고 EDU 변수만 putAll한다. 전체 payload를 OS temp JSON에 기록한다. cleanup 실패 주석은 'payload contains no secrets'라고 가정하지만 EDU 입력에는 Secret/Certificate/Password 성격 필드가 존재할 수 있다.
- 영향: 부모 Process Secret이 child script로 전파되고 임시파일 cleanup 실패 시 민감정보가 남을 수 있다.
- 필수 보완: child environment allowlist/clear, secret field redaction 또는 stdin 전달, restrictive file permission, guaranteed cleanup/audit를 적용한다.
- 필수 검증: parent secret non-inheritance, temp permission, kill/exception cleanup, secret scan.
- QA B 판정: **미통과**

### QA-B-R6S12-010 [P1] WAITING\_EXTERNAL 완료를 위한 acknowledgeExternal Service는 있으나 Public API가 없음

- 연결 Requirement: `FDEV-018,API-ASYNC`
- Source: `EduExecutionService.java; EduCapabilityController.java`
- 확인 Evidence: Service에는 acknowledgeExternal(operationId, actor, reason)가 있고 Integration Test는 직접 호출한다. Controller에는 execute/get/find/audit/targets/outbox/retry/reconcile/compensate/cancel만 있고 acknowledge endpoint가 없다.
- 영향: autoAcknowledge=false 또는 202/외부 ACK 흐름에서 API 사용자가 상태를 완료시킬 수 없으며 Test와 실제 API 사용 경로가 다르다.
- 필수 보완: 권한·expectedVersion·idempotency를 갖춘 external acknowledgement endpoint 또는 실제 callback/event consumer를 제공한다.
- 필수 검증: 202→ACK→SUCCEEDED, duplicate ACK, stale ACK, unauthorized ACK, lost response reconcile.
- QA B 판정: **미통과**

### QA-B-R6S12-011 [P1] EDU Gateway 14건은 실제 Gateway 계약이 아닌 공통 REF DB Simulator

- 연결 Requirement: `FDEV-018,SAMPLE-EDU`
- Source: `ReferenceGatewayBusinessConsumer.java; EduGw01~14Handler`
- 확인 Evidence: 14개 Gateway Handler는 REFERENCE\_GATEWAY를 사용하지만 Consumer는 JdbcEduBusinessConsumer로 `CPF_EDU_BUSINESS_RECORD`를 갱신하고 simulator metadata만 추가한다. 실제 routing/health/rate-limit/security/publish runtime을 호출하지 않는다.
- 영향: Gateway 적용·부분 NACK·Health·Drift·Rollback·다중 인스턴스 교육이 단일 DB row state 변경으로 대체된다.
- 필수 보완: 실제 reference gateway simulator endpoint와 runtime state/health/routing/publish/reconcile를 구현하고 제품 Gateway 계약과 parity를 검증한다.
- 필수 검증: route publish, member health, partial NACK, rate limit, LKG rollback, multi-instance drift.
- QA B 판정: **미통과**

### QA-B-R6S12-012 [P1] EDU Batch 30건은 Job Bean은 존재하지만 동일 Generic Worker에 수렴하고 실제 시나리오 검증이 없음

- 연결 Requirement: `FDEV-010,FDEV-018,SAMPLE-EDU`
- Source: `EduBat01~30Handler/JobConfiguration; SpringBatchEduBusinessConsumer; EduBatchScenarioWorker`
- 확인 Evidence: 30개 Handler와 per-ID JobConfiguration 구조는 존재한다. 그러나 각 Job은 공통 EduBatchScenarioWorker를 호출해 CPF\_REF\_BAT\_\*와 CPF\_EDU\_BUSINESS\_RECORD에 generic 성공 상태를 기록한다. catalog 30건 모두 미검증이고 per-ID Test는 deterministic double을 사용한다.
- 영향: Tasklet/Chunk/Partition/Remote Worker/Misfire/Backpressure 등 30개 시나리오의 실제 Spring Batch 의미가 독립적으로 검증되지 않는다.
- 필수 보완: 각 시나리오별 reader/processor/writer/partition/scheduler/restart semantics와 failure injection을 실제 JobRepository/DB에서 검증한다.
- 필수 검증: 30/30 actual Job bean start, checkpoint/restart, kill, lease/fencing, remote reassignment, backpressure, DB3.
- QA B 판정: **미통과**

### QA-B-R6S12-013 [P1] EDU Online·Backoffice 다수가 실제 제품 API 대신 Generic CPF\_EDU\_BUSINESS\_RECORD를 사용

- 연결 Requirement: `FDEV-018,SAMPLE-REF,SAMPLE-EDU`
- Source: `manual-135-catalog.json; JdbcEduBusinessConsumer.java; handlers`
- 확인 Evidence: JDBC\_COMMAND 51건과 JDBC\_QUERY 6건이 requirementId/businessKey/dataScope/payload를 한 generic table에 저장·조회한다. 최상위 요건은 EDU가 장난감 계약이 아니라 실제 Header/API/DB/Event/Batch/Security 표준을 사용하도록 요구한다.
- 영향: 제목별 업무 Validation/Schema/Transaction/Owner API 차이를 generic JSON row로 덮어 실제 제품 사용법 교육이 약화된다.
- 필수 보완: 공통 실행 원장은 추적용으로만 사용하고 각 예제는 실제 CPF Public API/SPI와 해당 Reference Domain Schema/Consumer를 호출한다.
- 필수 검증: scenario-specific schema/API, transaction rollback, header/auth, event/outbox, generated client.
- QA B 판정: **미통과**

### QA-B-R6S12-014 [P1] Starter/Catalog 정적 구조는 양호하지만 Fresh Build·Publication·Generated parity는 미검증

- 연결 Requirement: `FDEV-018,FDEV-023,FDEV-025`
- Source: `cpf-starter-catalog.json; settings.gradle; QA39`
- 확인 Evidence: 39개 module의 projectPath/ownerPath/artifactId/packageBase/configPrefix 중복은 0이고 6 public profile+33 internal starter 구조가 확인된다. local-domains는 명시 opt-in으로 개선됐다. 하지만 Java25/Gradle9.1 fresh build, publication, BOM visibility, generated domain runtime parity는 실행되지 않았다.
- 영향: Catalog JSON의 정합성만으로 실제 물리 module/build/public artifact 완료를 확정할 수 없다.
- 필수 보완: exact SHA fresh clone에서 settings/configuration, all modules, publication, BOM exposure, generator/remove/regenerate parity를 실행한다.
- 필수 검증: QA39 full, Gradle publication, artifact consumer build, public BOM internal exclusion, duplicate mutation.
- QA B 판정: **미통과**

## 5. ADM 63개 메뉴 전수 정적 검수 Matrix

- Route: `63`
- Registry operation reference: `414` / unique `329`
- Component consumed operation reference: `359` / unique `274`
- Operation drift Route: `12`
- Missing component operation reference: `55`
- Browser Interaction Matrix: `59` / Router `63`
- Interaction Matrix 제외 Route: `/featureFlags, /integrationClosure, /openApiOperations, /resiliencePolicies`

| Route                   | 메뉴                            |     Risk | Expected | Consumed | Gap | 실제 소비 유형            | Browser 원장 | 정적 판정    | Runtime |
| ----------------------- | ----------------------------- | -------: | -------: | -------: | --: | ------------------- | ---------- | -------- | ------- |
| `/`                     | 통합 운영 Dashboard               |   MEDIUM |        9 |        9 |   0 | C/A:2/R:7           | 포함         | 정적 계약 일치 | 미검증     |
| `/topology`             | 서비스 토폴로지                      |   MEDIUM |        4 |        4 |   0 | R:4                 | 포함         | 정적 계약 일치 | 미검증     |
| `/capacity`             | Online Runtime Diagnostics    |   MEDIUM |        6 |        6 |   0 | C/A:2/R:4           | 포함         | 정적 계약 일치 | 미검증     |
| `/logs`                 | 거래 로그                         |   MEDIUM |        4 |        4 |   0 | C/A:1/R:3           | 포함         | 정적 계약 일치 | 미검증     |
| `/transactionGroups`    | Online·Batch 통합 Trace         |   MEDIUM |        9 |        4 |   5 | R:4                 | 포함         | 미통과      | 미검증     |
| `/transactions`         | 온라인 거래 정의                     |     HIGH |        5 |        4 |   1 | R:3/U/A:1           | 포함         | 미통과      | 미검증     |
| `/remoteLogs`           | 원격 로그                         |   MEDIUM |        9 |        5 |   4 | C/A:1/R:3/U/A:1     | 포함         | 미통과      | 미검증     |
| `/auditLogs`            | 감사 로그                         |   MEDIUM |        4 |        4 |   0 | R:3/U/A:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/logLevel`             | 동적 로그                         |     HIGH |        3 |        3 |   0 | C/A:1/D:1/R:1       | 포함         | 정적 계약 일치 | 미검증     |
| `/logPolicies`          | 로그 정책                         |   MEDIUM |       13 |        6 |   7 | C/A:1/R:3/U/A:2     | 포함         | 미통과      | 미검증     |
| `/standardExecutions`   | 표준 실행                         |   MEDIUM |        2 |        2 |   0 | R:2                 | 포함         | 정적 계약 일치 | 미검증     |
| `/channelPolicy`        | 채널 정책                         |     HIGH |        6 |        6 |   0 | C/A:3/R:2/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/serviceRegistry`      | 서비스 레지스트리                     |   MEDIUM |       15 |       11 |   4 | C/A:3/D:3/R:4/U/A:1 | 포함         | 미통과      | 미검증     |
| `/runtimeControl`       | Deployment·Promotion·Rollback |     HIGH |       16 |       16 |   0 | C/A:15/D:1          | 포함         | 정적 계약 일치 | 미검증     |
| `/maintenance`          | 점검·Drain                      |     HIGH |        2 |        2 |   0 | C/A:1/R:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/cache`                | 캐시                            |     HIGH |        5 |        5 |   0 | D:2/R:1/U/A:2       | 포함         | 정적 계약 일치 | 미검증     |
| `/configs`              | 설정                            |     HIGH |        6 |        5 |   1 | C/A:1/D:1/R:2/U/A:1 | 포함         | 미통과      | 미검증     |
| `/responseCodes`        | 응답코드                          |   MEDIUM |        5 |        5 |   0 | C/A:1/D:1/R:2/U/A:1 | 포함         | 정적 계약 일치 | 미검증     |
| `/businessCalendar`     | 영업일 · 휴일                      |   MEDIUM |        4 |        4 |   0 | C/A:1/D:1/R:1/U/A:1 | 포함         | 정적 계약 일치 | 미검증     |
| `/recoveryCenter`       | 복구 센터                         |   MEDIUM |        5 |        5 |   0 | C/A:2/R:1/U/A:2     | 포함         | 정적 계약 일치 | 미검증     |
| `/incidents`            | Error·Unknown Result          |     HIGH |       22 |       12 |  10 | C/A:3/R:8/U/A:1     | 포함         | 미통과      | 미검증     |
| `/reliability`          | Analysis Center               |   MEDIUM |        8 |        7 |   1 | R:7                 | 포함         | 미통과      | 미검증     |
| `/notifications`        | 알림                            |   MEDIUM |       11 |        9 |   2 | C/A:2/R:4/U/A:3     | 포함         | 미통과      | 미검증     |
| `/batch`                | Batch / Center-Cut            |   MEDIUM |       12 |        6 |   6 | R:6                 | 포함         | 미통과      | 미검증     |
| `/batch-overview`       | Batch Overview                |   MEDIUM |        7 |        7 |   0 | R:7                 | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-runtime`        | Runtime Topology              |     HIGH |        4 |        4 |   0 | C/A:4               | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-instances`      | Runtime Instances             |   MEDIUM |        4 |        4 |   0 | C/A:2/R:2           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-scheduler`      | Scheduler HA                  |   MEDIUM |        6 |        6 |   0 | C/A:1/R:3/U/A:2     | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-worker-pools`   | Worker Pools                  |   MEDIUM |        5 |        5 |   0 | C/A:3/R:2           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-center-cut`     | Center-Cut                    |   MEDIUM |        9 |        9 |   0 | R:8/U/A:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-agents`         | Host Agents                   |   MEDIUM |        5 |        5 |   0 | C/A:4/R:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-job-packs`      | Job Packs                     |   MEDIUM |        8 |        8 |   0 | C/A:1/R:6/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-executions`     | Executions                    |   MEDIUM |        7 |        7 |   0 | R:5/U/A:2           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-deployment`     | Deployment / Rollback         |     HIGH |        3 |        3 |   0 | C/A:3               | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-recovery`       | Recovery / Unknown            |   MEDIUM |        6 |        6 |   0 | C/A:1/R:4/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-leases`         | Lease / Fencing               |   MEDIUM |        4 |        4 |   0 | C/A:1/R:3           | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-alerts`         | Batch Alerts                  |   MEDIUM |        4 |        4 |   0 | R:4                 | 포함         | 정적 계약 일치 | 미검증     |
| `/batch-audit`          | Audit / Evidence              |   MEDIUM |        5 |        5 |   0 | R:5                 | 포함         | 정적 계약 일치 | 미검증     |
| `/workers`              | Agent / Worker                |   MEDIUM |        3 |        3 |   0 | C/A:1/R:2           | 포함         | 정적 계약 일치 | 미검증     |
| `/downloads`            | 다운로드                          |   MEDIUM |        3 |        3 |   0 | R:3                 | 포함         | 정적 계약 일치 | 미검증     |
| `/file-jobs`            | 대량파일 Job                      |   MEDIUM |       10 |       10 |   0 | C/A:1/R:5/U/A:4     | 포함         | 정적 계약 일치 | 미검증     |
| `/messages`             | 전문·Protocol Message           |   MEDIUM |        6 |        6 |   0 | C/A:1/D:1/R:3/U/A:1 | 포함         | 정적 계약 일치 | 미검증     |
| `/codes`                | 코드                            |   MEDIUM |        5 |        5 |   0 | C/A:1/D:1/R:2/U/A:1 | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-dashboard`    | Gateway 대시보드                  |   MEDIUM |        4 |        4 |   0 | R:4                 | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-servers`      | Gateway 연동 서버                 |   MEDIUM |        4 |        4 |   0 | C/A:1/D:1/R:2       | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-groups`       | Gateway 서버 그룹                 |   MEDIUM |        4 |        4 |   0 | C/A:1/D:1/R:2       | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-routes`       | Gateway 경로·라우팅                |   MEDIUM |        4 |        4 |   0 | C/A:1/D:1/R:1/U/A:1 | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-security`     | Gateway 보안·제한                 |     HIGH |        3 |        3 |   0 | C/A:1/R:1/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-health`       | Gateway Health·연결시험           |   MEDIUM |        7 |        7 |   0 | C/A:2/R:2/U/A:3     | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-transactions` | Gateway 거래 조회                 |   MEDIUM |        3 |        3 |   0 | R:3                 | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-log-policies` | Gateway 로그 정책                 |   MEDIUM |        3 |        3 |   0 | R:3                 | 포함         | 정적 계약 일치 | 미검증     |
| `/gateway-apply-status` | Gateway 적용 상태·이력              |   MEDIUM |        3 |        3 |   0 | C/A:1/R:1/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/permissions`          | 권한                            |   MEDIUM |       26 |       15 |  11 | R:11/U/A:4          | 포함         | 미통과      | 미검증     |
| `/password`             | 비밀번호                          |     HIGH |        5 |        5 |   0 | D:1/R:1/U/A:3       | 포함         | 정적 계약 일치 | 미검증     |
| `/security`             | 보안                            |     HIGH |        6 |        6 |   0 | C/A:2/R:3/U/A:1     | 포함         | 정적 계약 일치 | 미검증     |
| `/operators`            | 운영자                           |     HIGH |       12 |        9 |   3 | C/A:2/R:4/U/A:3     | 포함         | 미통과      | 미검증     |
| `/secrets`              | Secret / Key                  |     HIGH |        3 |        3 |   0 | R:2/U/A:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/approvals`            | 위험조치 승인                       |     HIGH |       12 |       12 |   0 | C/A:6/R:4/U/A:2     | 포함         | 정적 계약 일치 | 미검증     |
| `/breakGlass`           | Break-glass                   |     HIGH |        4 |        4 |   0 | R:3/U/A:1           | 포함         | 정적 계약 일치 | 미검증     |
| `/featureFlags`         | Feature Flag                  | CRITICAL |        7 |        7 |   0 | C/A:1/D:1/R:3/U/A:2 | 누락         | 미통과      | 미검증     |
| `/integrationClosure`   | 통합 운영 정정 승인                   | CRITICAL |        8 |        8 |   0 | C/A:4/R:3/U/A:1     | 누락         | 미통과      | 미검증     |
| `/openApiOperations`    | OpenAPI 운영                    |     HIGH |        2 |        2 |   0 | R:1/U/A:1           | 누락         | 미통과      | 미검증     |
| `/resiliencePolicies`   | Resilience 정책                 | CRITICAL |        5 |        5 |   0 | C/A:1/R:2/U/A:2     | 누락         | 미통과      | 미검증     |

### 5.1 Component에서 빠진 12개 메뉴·55개 Operation

- `/transactionGroups` Online·Batch 통합 Trace: `admTransactionGroupFindDetail`, `admTransactionGroupFindExternalLogs`, `admTransactionGroupFindGroups`, `admTransactionGroupFindHeaders`, `admTransactionGroupFindSegments`
- `/transactions` 온라인 거래 정의: `admTransactionMetaFindTransactions`
- `/remoteLogs` 원격 로그: `admRemoteLogBundleDownload`, `admRemoteLogBundleDownloadTokenIssue`, `admRemoteLogDiagnostics`, `admRemoteLogDownload`
- `/logPolicies` 로그 정책: `admLogPolicyClearCache`, `admLogPolicyCreateOverride`, `admLogPolicyCreateTraceBoost`, `admLogPolicyDisableOverride`, `admLogPolicyFindTraceBoostHistory`, `admLogPolicyFindTraceBoostRuntimeState`, `admLogPolicyRefreshCache`
- `/serviceRegistry` 서비스 레지스트리: `admServiceRegistryCapabilities`, `admServiceRegistryFindCallHistory`, `admServiceRegistryFindCircuitStates`, `admServiceRegistryFindRoutingPolicies`
- `/configs` 설정: `admParameterReferenceSearch`
- `/incidents` Error·Unknown Result: `admIncidentAcknowledge`, `admIncidentCreateMaintenance`, `admIncidentCreatePolicy`, `admIncidentEscalate`, `admIncidentFindIncident`, `admIncidentFindTimeline`, `admIncidentReopen`, `admIncidentResolve`, `admIncidentUpdateMaintenance`, `admIncidentUpdatePolicy`
- `/reliability` Analysis Center: `findAdmBatchJobInstanceLogs`
- `/notifications` 알림: `admNotificationDisableRule`, `admNotificationFindDlq`
- `/batch` Batch / Center-Cut: `admBatchFindExecutionTargets`, `admBatchFindExecutions`, `admBatchFindJobDetail`, `admBatchFindRelations`, `admBatchRegisterJob`, `admBatchRunJob`
- `/permissions` 권한: `admPermissionCreateApiPermission`, `admPermissionCreateButton`, `admPermissionCreateMenu`, `admPermissionCreateRole`, `admPermissionUpdateApiPermission`, `admPermissionUpdateButton`, `admPermissionUpdateButtonPermission`, `admPermissionUpdateMenu`, `admPermissionUpdateMenuPermission`, `admPermissionUpdateRole`, `admPermissionUpdateRoleApiPermission`
- `/operators` 운영자: `admOperatorCleanupExpiredSessions`, `admOperatorFindCreateResult`, `admOperatorRawContact`

### 5.2 ADM Source 판정

- Business Calendar는 실제 GET/PUT/DELETE, expectedVersion, 감사사유, auth/me 메뉴 권한을 직접 호출한다.
- Service Registry는 Service/Endpoint/Instance 저장·삭제·상태변경과 expectedVersion/operationId를 실제 generated operation으로 호출한다.
- Feature Flag는 검색·평가·Override 요청·승인·회수·Kill Switch API를 실제 호출한다.
- Config·Code·Notification 등은 공통 Pinia Store/Method Registry를 통해 실제 API에 연결된다.
- 따라서 ADM 전체가 단순 정적 화면만 있는 것은 아니다.
- 하지만 Registry 선언 기능 전부가 각 메뉴에서 소비되지 않고 Browser/실시간 Runtime Evidence가 없으므로 메뉴별 완료 판정은 불가하다.

## 6. EDU 135 전수 Catalog 검산

- schemaVersion: `2`
- featureCount: `135`
- 실제 parsed count: `135`
- unique Requirement ID: `135`
- unique sourcePath: `135`
- unique resourceContract: `135`
- Test path reference: `675` / unique `675`
- Development status: `{'완료': 135}`
- Verification status: `{'미검증': 135}`

### 6.1 분류별 수량

| Kind       | 수량 | Consumer 분포                                                          |
| ---------- | -: | -------------------------------------------------------------------- |
| ONLINE     | 45 | FILE:6, HTTP:5, JDBC\_COMMAND:23, JDBC\_QUERY:3, OUTBOX:6, PROCESS:2 |
| BATCH      | 30 | SPRING\_BATCH:30                                                     |
| ADM        | 17 | JDBC\_COMMAND:15, JDBC\_QUERY:2                                      |
| OPS        | 15 | PROCESS:15                                                           |
| BACKOFFICE | 14 | JDBC\_COMMAND:13, JDBC\_QUERY:1                                      |
| GATEWAY    | 14 | REFERENCE\_GATEWAY:14                                                |

### 6.2 EDU-ADM 매뉴얼·Catalog 제목 Drift 17/17

| ID           | ADM 개발자 매뉴얼           | 135 Catalog                         |
| ------------ | --------------------- | ----------------------------------- |
| `EDU-ADM-01` | Owner Query Port      | 기존 ADM 기능 재사용 판단                    |
| `EDU-ADM-02` | Owner Command Port    | 고객 업무 조회 연동                         |
| `EDU-ADM-03` | Same-JVM Adapter      | 안전한 운영 조치                           |
| `EDU-ADM-04` | Remote Adapter        | 승인 필요한 위험 조치                        |
| `EDU-ADM-05` | ADM Controller        | 비동기 작업·응답 유실                        |
| `EDU-ADM-06` | OpenAPI Operation     | 부분 성공·대상별 복구                        |
| `EDU-ADM-07` | Generated Client      | 고객 전용 화면 추가의 마지막 선택                 |
| `EDU-ADM-08` | Route Registry        | 권한·데이터 범위·Masking·사유 입력 연동          |
| `EDU-ADM-09` | 검색 Form               | Expected Version 충돌 화면·재조회·재적용      |
| `EDU-ADM-10` | Table·Detail          | 대상 일괄 조치·부분 성공·결과 파일                |
| `EDU-ADM-11` | Command Dialog        | 설정·기능전환·유지보수 창 운영                   |
| `EDU-ADM-12` | 409 Conflict UX       | Incident·Recovery Center 종단간 복구     |
| `EDU-ADM-13` | Timeout·UNKNOWN UX    | 감사 증적·다운로드·승인 반출                    |
| `EDU-ADM-14` | Masking·Export        | Topology·Health·Capacity Drill-down |
| `EDU-ADM-15` | Browser Negative Test | Log·Trace·Transaction 상관 검색         |
| `EDU-ADM-16` | 통합 정정 승인              | 알림 Acknowledge·Escalation·교대 인계     |
| `EDU-ADM-17` | 부분 적용 Rollback        | Browser 세션 만료·재로그인·위험 조치 안전성        |

### 6.3 EDU 135건 개별 원장

| ID           | Title                                      | Kind       | Consumer           | Entry Point                                                   | Dev | Verify | QA B 판정                      |
| ------------ | ------------------------------------------ | ---------- | ------------------ | ------------------------------------------------------------- | --- | ------ | ---------------------------- |
| `EDU-DEV-01` | Generator 기반 신규 업무 영역 생성                   | ONLINE     | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-02` | 권한·범위가 적용된 목록·상세 조회                        | ONLINE     | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-03` | 등록·수정·상태 변경과 감사                            | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-04` | 동시 수정과 예상 Version 충돌                       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-05` | 지급 등록 멱등성·응답 유실·결과 대사                      | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-06` | 같은 애플리케이션·분리 서비스 호출 동등성                    | ONLINE     | HTTP               | `/external/06`                                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-07` | Kafka Outbox·Inbox·중복 소비·재처리               | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-08` | 파일 업로드·검사·첨부·다운로드                          | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-09` | 외부 REST 신용조회와 결과 미확정                       | ONLINE     | HTTP               | `/external/09`                                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-10` | 고정길이 전문 기관 이체                              | ONLINE     | HTTP               | `/external/10`                                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-11` | 권한·데이터 범위·개인정보 가림·감사                       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-12` | Cache·기능 전환·Secret 교체                      | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-13` | 알림·비동기 내보내기·다운로드 감사                        | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-14` | Oracle·PostgreSQL·MariaDB 동일 의미 Migration  | ONLINE     | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-15` | 지급 업무 장애 주입·복구·운영 인계                       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-16` | 대용량 목록 검색·정렬·Cursor Paging                 | ONLINE     | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-17` | 대량 등록 사전검증·부분 오류 보고·재업로드                   | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-18` | 논리 삭제·복원·보존기간 만료                           | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-19` | 기준일·유효기간이 있는 기준정보                          | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-20` | 다단계 고객 업무 상태기계와 취소·재개                      | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-21` | Transactional Outbox 게시 지연·재시작             | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-22` | 서비스 간 Saga 보상·수동 확정                        | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-23` | 공통 입력검증·오류 계약·OpenAPI 일치                   | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-24` | 장시간 비동기 Operation 조회·취소                    | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-25` | Webhook Callback 서명·재전송·Replay 방지          | ONLINE     | HTTP               | `/external/25`                                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-26` | SFTP 수신·송신·완료 파일 원자 처리                     | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-27` | SOAP·XML 외부기관 연계와 Fault 처리                 | ONLINE     | HTTP               | `/external/27`                                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-28` | 대용량 Multipart 업로드·중단 재개                    | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-29` | 악성코드 검사·격리·승인 해제                           | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-30` | Object Storage 보존·버전·법적 보류                 | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-31` | 다중 채널 알림 선호·재시도·대체 채널                      | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-32` | 개인정보 암호화·Tokenization·Key Rotation         | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-33` | 인증 Token 만료·갱신·폐기·세션 강제 종료                 | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-34` | API 사용량 제한·고객별 Quota·초과 처리                 | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-35` | 기능 전환 Canary·Kill Switch·사용자 Segment       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-36` | Cache Stampede·Negative Cache·원본 정합성       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-37` | 온라인 분산 Lease·Fencing·소유권 상실                | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-38` | 다중 Tenant 격리·설정·데이터 범위                     | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-39` | 업무일자·시간대·휴일 Calendar                       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-40` | 금액·통화·반올림·환율 Version                       | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-41` | 감사 증적 Export·무결성 Hash·검증                   | ONLINE     | FILE               | `cpf-reference EDU file store`                                | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-42` | 로그·Metric·Trace 상관관계와 Sampling             | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-43` | API Version 전환·하위 호환·폐기                    | ONLINE     | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-44` | Event Schema 진화·호환성·Dead Letter            | ONLINE     | OUTBOX             | `CPF_EDU_OUTBOX`                                              | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-DEV-45` | 조회 모델·검색색인 Eventual Consistency            | ONLINE     | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BAT-01` | 업무일 마감 Tasklet                             | BATCH      | SPRING\_BATCH      | `eduBat01Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-02` | 회원 등급 10,000건 Chunk                        | BATCH      | SPRING\_BATCH      | `eduBat02Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-03` | CSV 입출력 배치                                 | BATCH      | SPRING\_BATCH      | `eduBat03Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-04` | 8개 범위 Partition                            | BATCH      | SPRING\_BATCH      | `eduBat04Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-05` | Manager·Worker·Lease·Fencing               | BATCH      | SPRING\_BATCH      | `eduBat05Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-06` | 센터컷 Preview·승인·실행                          | BATCH      | SPRING\_BATCH      | `eduBat06Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-07` | 영업일 23시 Scheduler                          | BATCH      | SPRING\_BATCH      | `eduBat07Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-08` | Job Pack Version·Artifact 배포               | BATCH      | SPRING\_BATCH      | `eduBat08Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-09` | 중지·재시작·실패건 재처리                             | BATCH      | SPRING\_BATCH      | `eduBat09Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-10` | 실행 요청 응답 유실·결과 대사                          | BATCH      | SPRING\_BATCH      | `eduBat10Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-11` | 조건 분기·다단계 Job Flow                         | BATCH      | SPRING\_BATCH      | `eduBat11Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-12` | Retry·Skip·No-Skip 예외 분류                   | BATCH      | SPRING\_BATCH      | `eduBat12Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-13` | Writer Commit 장애 후 Checkpoint 재시작          | BATCH      | SPRING\_BATCH      | `eduBat13Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-14` | JobParameter 식별·중복 실행·새 Instance           | BATCH      | SPRING\_BATCH      | `eduBat14Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-15` | 지연 도착 데이터·Backfill·재산출                     | BATCH      | SPRING\_BATCH      | `eduBat15Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-16` | Watermark 기반 증분 수집·재시작                     | BATCH      | SPRING\_BATCH      | `eduBat16Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-17` | 암호화·압축·Checksum 파일 산출                      | BATCH      | SPRING\_BATCH      | `eduBat17Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-18` | 수신 파일 Header·Detail·Trailer 대사             | BATCH      | SPRING\_BATCH      | `eduBat18Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-19` | 다중 파일 Fan-in·Fan-out                       | BATCH      | SPRING\_BATCH      | `eduBat19Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-20` | Scheduler Misfire·Catch-up·건너뛰기            | BATCH      | SPRING\_BATCH      | `eduBat20Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-21` | 중복 실행 방지·동시 실행 허용 범위                       | BATCH      | SPRING\_BATCH      | `eduBat21Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-22` | 휴일 Calendar·영업일 순번 JobParameter            | BATCH      | SPRING\_BATCH      | `eduBat22Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-23` | Stop·Abandon·Restart 의미 분리                 | BATCH      | SPRING\_BATCH      | `eduBat23Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-24` | Remote Worker 유실·재할당·중복 결과 차단              | BATCH      | SPRING\_BATCH      | `eduBat24Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-25` | Partition 편향 감지·재분할                        | BATCH      | SPRING\_BATCH      | `eduBat25Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-26` | 센터컷 결과 대사·차이 보정·재실행                        | BATCH      | SPRING\_BATCH      | `eduBat26Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-27` | Job Pack Checksum·호환성·이전 Version 복구        | BATCH      | SPRING\_BATCH      | `eduBat27Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-28` | Host Agent Offline·명령 ACK 유실               | BATCH      | SPRING\_BATCH      | `eduBat28Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-29` | Dry Run·건수 Preview·표본 확인                   | BATCH      | SPRING\_BATCH      | `eduBat29Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-BAT-30` | 대용량 처리 성능·용량·Backpressure                  | BATCH      | SPRING\_BATCH      | `eduBat30Job`                                                 | 완료  | 미검증    | Job Source·개별 Runtime 미검증    |
| `EDU-ADM-01` | 기존 ADM 기능 재사용 판단                           | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-02` | 고객 업무 조회 연동                                | ADM        | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-03` | 안전한 운영 조치                                  | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-04` | 승인 필요한 위험 조치                               | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-05` | 비동기 작업·응답 유실                               | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-06` | 부분 성공·대상별 복구                               | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-07` | 고객 전용 화면 추가의 마지막 선택                        | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-08` | 권한·데이터 범위·Masking·사유 입력 연동                 | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-09` | Expected Version 충돌 화면·재조회·재적용             | ADM        | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-10` | 대상 일괄 조치·부분 성공·결과 파일                       | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-11` | 설정·기능전환·유지보수 창 운영                          | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-12` | Incident·Recovery Center 종단간 복구            | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-13` | 감사 증적·다운로드·승인 반출                           | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-14` | Topology·Health·Capacity Drill-down        | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-15` | Log·Trace·Transaction 상관 검색                | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-16` | 알림 Acknowledge·Escalation·교대 인계            | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-ADM-17` | Browser 세션 만료·재로그인·위험 조치 안전성               | ADM        | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | 부분 구현·실제 ADM 미연동             |
| `EDU-BZA-01` | 조직·직원·발령·기준일                               | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-02` | 사용자·역할·권한·실효 권한                            | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-03` | 결재정책 Version·경로 사전 계산                      | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-04` | 상신·승인·반려·철회·취소                             | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-05` | 위임·대결·대행 책임                                | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-06` | 첨부·알림·감사·다운로드                              | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-07` | 초기 관리자 Bootstrap·첫 로그인·권한 인계               | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-08` | 조직 개편·기준일·과거 이력 유지                         | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-09` | 입사·이동·휴직·퇴사 Joiner-Mover-Leaver            | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-10` | 역할 충돌·직무분리·실효 권한 Simulation                | BACKOFFICE | JDBC\_QUERY        | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-11` | 위임 중첩·기간 만료·결재 경로 재계산                      | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-12` | 계정 잠금·비밀번호 초기화·세션 강제 종료                    | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-13` | 개인정보 Masking·감사 조회·승인 Export               | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-BZA-14` | 고객 업무 승인 결과 반영·실패 Rollback                 | BACKOFFICE | JDBC\_COMMAND      | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Source 존재·실제 Consumer 미검증    |
| `EDU-GW-01`  | Server Group·Health·Load Balancing         | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-02`  | Route·Predicate·Path Rewrite               | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-03`  | 인증·권한·TLS·HMAC·Nonce                       | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-04`  | Timeout·Retry·Circuit Breaker·Bulkhead     | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-05`  | Draft·검증·승인·게시·부분 적용                       | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-06`  | Attempt Ledger·UNKNOWN\_RESULT·LKG 복구      | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-07`  | Service Discovery·Target Failover·복귀       | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-08`  | SSRF Allowlist·DNS Rebinding·내부망 차단        | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-09`  | Header 정리·경로·요청·응답 변환                      | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-10`  | Body 크기·Content-Type·Schema Validation     | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-11`  | Command 멱등성·Attempt Ledger·응답 유실           | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-12`  | 다중 인스턴스 설정 Drift·Reconcile                 | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-13`  | Canary·가중치 Routing·Version Rollback        | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-GW-14`  | Gateway 관측·개인정보 가림·감사                      | GATEWAY    | REFERENCE\_GATEWAY | `CPF_EDU_BUSINESS_RECORD`                                     | 완료  | 미검증    | Simulator Source·Runtime 미검증 |
| `EDU-OPS-01` | 신규 환경 설치·Artifact·Checksum 검증              | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-02` | Profile·환경변수·설정값 전체 검증                     | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-03` | Secret·Certificate 배포·교체·만료 대응             | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-04` | DB 3종 신규 설치·Migration·Drift·Rollback       | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-05` | Kafka Topic·ACL·Consumer Group Lifecycle   | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-06` | 기동·종료·Health·Dependency 순서                 | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-07` | Rolling 배포·Session·Connection Drain        | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-08` | Blue-Green·Canary 전환·되돌리기                  | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-09` | 설정 변경 Partial Apply·Reconcile              | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-10` | Log·Metric·Trace 수집 장애·보존·용량               | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-11` | Backup·Restore·시점 복구·대사                    | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-12` | 재해복구 전환·복귀·Split-Brain 방지                  | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-13` | Disk·Memory·Network·DB 장애 Runbook          | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-14` | 보안 사고·계정·키·세션 긴급 차단                        | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |
| `EDU-OPS-15` | Version Upgrade·DB 호환·Application Rollback | OPS        | PROCESS            | `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1` | 완료  | 미검증    | Sandbox/정적 확인·Runtime 미검증    |

## 7. Starter·Generator·Publication B 검수

- Canonical Starter module: `39`
- Public Profile: `6`
- Internal Starter: `33`
- projectPath/ownerPath/artifactId/packageBase/configPrefix Duplicate: `0`
- ownerGroup/internalRole 누락: `0`
- `settings.gradle`의 local-domains 자동 include는 명시 opt-in 및 manifest/hash 확인 구조로 개선됨
- 정적 Catalog 구조는 양호하나 Java25·Gradle9.1 fresh build, Publication, BOM consumer, Generator remove/regenerate는 미검증

## 8. QA A Finding 요약

| ID               | Severity | 제목                                                                  |
| ---------------- | -------: | ------------------------------------------------------------------- |
| `QA-A-R6S12-001` |       P0 | 승인 정책 4차원 결속 완료 주장이 Source와 불일치                                     |
| `QA-A-R6S12-002` |       P0 | Integration Closure UI 권한 Projection이 실제 로그인 Session과 단절            |
| `QA-A-R6S12-003` |       P0 | GitHub Browser Release Gate가 구조적으로 실행 불가                            |
| `QA-A-R6S12-004` |       P0 | R6 Behavior/Mutation Gate가 행위 검증을 흉내낸 false-green                   |
| `QA-A-R6S12-005` |       P0 | 현재 Result SHA와 Package/Evidence Provenance 불일치                      |
| `QA-A-R6S12-006` |       P0 | Target Runtime·DB3·Multi-process가 필수 Release Gate가 아님               |
| `QA-A-R6S12-007` |       P1 | UI E2E가 실제 인증·권한·Backend 상태변경을 검증하지 않음                              |
| `QA-A-R6S12-008` |       P1 | 멱등성 Ledger가 사용자·Tenant·Session 간 격리되지 않음                            |
| `QA-A-R6S12-009` |       P1 | 멱등성 Unit Test가 저장소 불일치로 false-green                                 |
| `QA-A-R6S12-010` |       P1 | 상태 조회가 두 권한을 묶으면서 버튼은 한 권한만 확인                                      |
| `QA-A-R6S12-011` |       P1 | Approval 화면 기본 Owner Command가 실제 Adapter에서 지원되지 않음                  |
| `QA-A-R6S12-012` |       P1 | Approval 위험조치 UI에 권한·In-flight 중복제어가 없음                             |
| `QA-A-R6S12-013` |       P1 | Approval Policy Steps가 Strict JSON 계약을 우회                           |
| `QA-A-R6S12-014` |       P1 | Stale Generated API가 두 번째 계약 정본으로 잔존                                |
| `QA-A-R6S12-015` |       P1 | Correction Approval Consumer 경로의 201/200/409/422 계약 미완성             |
| `QA-A-R6S12-016` |       P1 | Approval Exception Handler가 Integration Closure Controller를 포함하지 않음 |
| `QA-A-R6S12-017` |       P1 | Release Runner가 첫 실패에서 중단되어 전체 실패 집계 불가                             |
| `QA-A-R6S12-018` |       P1 | HMAC 승인 Proof Canonical Message 경계가 모호함                             |
| `QA-A-R6S12-019` |       P2 | UI 오류 계약에 422가 누락                                                   |

## 9. QA A 상세 원문

# CPF QA A — R6S12 독립 상세 검수 결과

- 생성 시각(KST): `2026-08-07T10:28:37+09:00`
- QA 역할: **QA A / QA 1 주담당**
- 주담당: Core Architecture·Source·Consumer·Approval·Security·Concurrency·Recovery·DB/Runtime·Evidence
- 특별 강화 동료검토: **ADM UI·Frontend 실구현 여부**
- 통합 여부: **통합하지 않음** — 이 문서는 별도 통합 QA 세션의 입력 파일이다.
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 exact SHA: `77db10ad9aff44ee422795080fb2e96b364c9d65` (`08_01`)
- 직전 기준 SHA: `28f823a18eca859cebdbceb382029f595cdf490c`
- Git Write/Merge/Push/Branch/Tag/PR/Delete: **수행하지 않음**

## 1. 최종 판정

**미통과 — Release Blocked**

- 신규·재개방 QA A Finding: **19건**
- Severity: **P0 6 / P1 12 / P2 1**
- 기존 R5I 29건 QA A 재판정: 통과 확정 0건, 부분 통과/부분 검증 일부, 미통과·미검증·미완료 다수
- 개발 원장 자체 상태: Finding `완료 25 / 부분 구현 3 / 미완료 1`, Verification `29/29 미검증`
- Requirement 원장 자체 상태: `완료 15 / 부분 구현 5 / 재확인 필요 5`, Verification `25/25 미검증 또는 부분 검증`

개발 변경은 이전 Checkpoint보다 훨씬 광범위하고 실제 Source·Test·SQL·Frontend 보완이 존재한다. 그러나 완료 주장과 달리 정책 결속, UI 권한 연결, Browser CI, 행위 Gate, current SHA Evidence에서 P0 결함이 확인됐다. 따라서 QA 최종 통과나 개발 완료로 인정할 수 없다.

## 2. QA 수행 원칙과 범위

개발GPT의 완료 보고를 승계하지 않고 exact SHA Source를 GitHub Connector로 다시 읽었다. 다음 경로를 연속 추적했다.

- Approval: Controller → Service → Repository → Owner Port/Adapter → Data Quality Correction Port
- UI: Route Registry → Page → Permission Projection → Operation Client → Runtime Controller
- Concurrency: Browser idempotency ledger → request body → server idempotency/CAS
- Runtime Gate: GitHub Workflow → PowerShell Runner → Gradle/npm/Playwright/DB3/Multi-process
- Evidence: Finding/Requirement 원장 → package basis → result commit SHA

실행 환경은 Java 21.0.10, Node 22.16.0, npm 10.9.2이며 Target Java25/Node22.18+/DB/Browser 서버 환경은 없었다. 따라서 Target Runtime은 PASS로 기록하지 않았다. 대신 TypeScript strict compile과 독립 Node/Python harness를 실행했다.

## 3. 독립 실행·재현 결과

| 검증                          | 실제 결과                                      | 판정                  |
| --------------------------- | ------------------------------------------ | ------------------- |
| latest master 조회            | `77db10ad9aff44ee422795080fb2e96b364c9d65` | 기준 확정               |
| TypeScript strict compile   | Exit 0                                     | 제한적 PASS            |
| Strict JSON duplicate key   | 거부 재현                                      | PASS                |
| Strict JSON unsafe integer  | 거부 재현                                      | PASS                |
| Nullable object             | `{name:null}` 수용                           | PASS                |
| Cross-session idempotency   | 다른 사용자 시뮬레이션에서 동일 key 재사용                  | FAIL                |
| Unit Test storage isolation | sessionStorage 상태가 beforeEach 이후 잔존        | FAIL                |
| 민감 payload Test assertion   | sessionStorage 기록 후 localStorage 검사        | 무의미한 PASS 재현        |
| HMAC canonical message      | 서로 다른 tuple의 동일 문자열 구성 가능                  | FAIL/설계 보완          |
| Generated API 비교            | stale direct-fetch/old replay signature 잔존 | FAIL                |
| Java25/Gradle9.1 full build | 환경 미보유                                     | NOT\_EXECUTED       |
| Playwright actual server    | workflow/server/auth 설정 부재                 | NOT\_EXECUTED·구조 결함 |
| DB3 live lifecycle          | DB 환경 미보유                                  | NOT\_EXECUTED       |
| Multi-process/Broker        | 환경/실행 없음                                   | NOT\_EXECUTED       |

독립 실행 로그 SHA-256:

- `QA1_EXECUTION_LOG.txt`: `21b9f10e3e785d8bfe85ff32e77f51c726a3fb8bd6094163786f4d1f191905ee`

## 4. 확인된 긍정적 개선

- `application.yml`의 기본 local profile 제거와 profile guard Source가 존재한다.
- Strict JSON parser는 duplicate key와 unsafe integer를 실제로 거부한다.
- Data Quality null/CAS/replay 관련 Source·Test가 추가됐다.
- DB runner는 child environment clear, stdin secret 전달, timeout/kill-tree 방향으로 개선됐다.
- Oracle/PostgreSQL/MariaDB R6 SQL lifecycle 파일과 static parity gate가 존재한다.
- 보호 SVG가 historical blob 기반으로 복원됐다.
- Feature Integration Closure API는 canonical operation client를 사용한다.

이 항목들은 Source 수준 개선으로 인정하지만 Target Runtime PASS를 의미하지 않는다.

## 5. 기존 QA R5I 29건 — QA A 항목별 재판정

| ID           | 우선순위 | QA A 판정    | 근거 요약                                                                                                     |
| ------------ | ---: | ---------- | --------------------------------------------------------------------------------------------------------- |
| `QA-R5I-001` |   P0 | **미통과**    | 현재 master 77db10...과 R6S12 package basis 28f823... 사이 result SHA/Evidence 결속이 없다.                         |
| `QA-R5I-002` |   P0 | **미검증**    | Java 25·Gradle 9.1·Node 22.18+·Browser·DB3·Multi-process Target Runtime이 실행되지 않았다.                        |
| `QA-R5I-003` |   P0 | **미통과**    | Evidence 구조는 존재하지만 current result SHA와 runtime evidence가 결속되지 않았다.                                        |
| `QA-R5I-004` |   P0 | **부분 통과**  | 보호 SVG 복원은 Source상 확인했으나 fresh clone/package 적용 후 hash·clean-tree 검증은 미실행이다.                              |
| `QA-R5I-005` |   P0 | **미통과**    | 정책은 actionType으로만 조회된다. Owner·Command·Target은 adapter supports에서 별도 검사되어 정책 자체의 4차원 결속이 아니다.              |
| `QA-R5I-006` |   P0 | **부분 검증**  | 기본 local profile 제거와 guard Source는 확인했으나 ApplicationContext·운영 profile runtime은 미실행이다.                    |
| `QA-R5I-007` |   P0 | **부분 검증**  | Bean pair 보완 Source/Test는 있으나 Java 25 full context runtime은 미실행이다.                                        |
| `QA-R5I-008` |   P0 | **미통과**    | 실제 UI 권한 projection이 server session buttonIds와 연결되지 않고 stale generated API도 잔존한다.                         |
| `QA-R5I-009` |   P0 | **부분 검증**  | detail sanitize Source는 개선됐으나 실제 API/browser/log 민감정보 비노출 runtime evidence가 없다.                           |
| `QA-R5I-010` |   P1 | **미통과**    | Strict parser는 개선됐지만 Approval policy steps는 JSON.parse를 사용해 동일 strict contract를 우회한다.                     |
| `QA-R5I-011` |   P1 | **부분 검증**  | null 처리 Source/Test는 존재하나 target Java runtime은 미실행이다.                                                     |
| `QA-R5I-012` |   P1 | **부분 검증**  | Replay lock/CAS Source와 제한적 harness는 있으나 DB·multi-instance 동시성은 미실행이다.                                    |
| `QA-R5I-013` |   P1 | **재확인 필요** | Policy lifecycle·break-glass 변경은 전체 DB/runtime 감사 경로까지 독립 검증되지 않았다.                                       |
| `QA-R5I-014` |   P1 | **미검증**    | 실제 DB3 unique conflict/CAS 수렴은 실행되지 않았다.                                                                  |
| `QA-R5I-015` |   P1 | **부분 검증**  | Runner가 child environment clear·stdin secret·timeout을 구현한 것은 확인했으나 DB3 live execution은 없다.                |
| `QA-R5I-016` |   P1 | **미통과**    | UI permission metadata가 실제 session store와 단절되고 E2E가 data attribute를 인위 주입해 false-green을 만든다.              |
| `QA-R5I-017` |   P1 | **미검증**    | Frontend Gradle input 보완 주장은 full Gradle/npm clean rebuild로 검증되지 않았다.                                     |
| `QA-R5I-018` |   P1 | **부분 검증**  | OpenAPI route 합성 금지 Source는 있으나 stale generated API와 full generated-client verify 미실행이 남는다.               |
| `QA-R5I-019` |   P1 | **미통과**    | R6 behavior/mutation gate가 token 존재와 문자열 삭제만 확인해 실제 행위 mutation이 아니다.                                     |
| `QA-R5I-020` |   P1 | **미통과**    | 개발 원장 자체가 modified scope만 검토했다고 기록했으며 전체 Repository dead/garbage source 검수가 없다.                           |
| `QA-R5I-021` |   P2 | **부분 통과**  | 문서는 작성됐으나 일부 완료 주장이 실제 검증·Source와 불일치한다.                                                                  |
| `QA-R5I-022` |   P2 | **미통과**    | Manifest/Hash는 package 기준이며 현재 result commit SHA·clean tree와 결속되지 않았다.                                    |
| `QA-R5I-023` |   P2 | **미통과**    | Release workflow의 Browser 환경이 구성되지 않았고 DB3/Multi-process가 PR에서 사실상 optional이다.                            |
| `QA-R5I-024` |   P2 | **미통과**    | Overlay와 실제 result commit 77db10... 사이 post-apply provenance 검증이 없다.                                      |
| `QA-R5I-025` |   P0 | **미통과**    | HMAC proof는 추가됐으나 canonical message delimiter가 모호하고 정책 4차원 결속도 성립하지 않는다.                                  |
| `QA-R5I-026` |   P0 | **미통과**    | A→B→A ledger는 개선됐지만 global localStorage key와 fingerprint에 operator/tenant/session이 없어 세션 간 key 재사용이 가능하다. |
| `QA-R5I-027` |   P1 | **미통과**    | 행위·Mutation Gate는 실제 소스 변형 후 Test를 실행하지 않는 tautological 검사다.                                              |
| `QA-R5I-028` |   P1 | **미통과**    | Generic approval는 201/200/409/422를 구분하지만 Integration Closure correction request는 항상 200이고 Advice 범위 밖이다.  |
| `QA-R5I-029` |   P0 | **미완료**    | Codex 요청서만 존재하고 독립 Codex 검수는 실행되지 않았다.                                                                    |

## 6. QA A 신규·재개방 Finding 상세

### QA-A-R6S12-001 [P0] 승인 정책 4차원 결속 완료 주장이 Source와 불일치

- 연결 Requirement: `FDEV-003,FDEV-013`
- Source·근거: `AdmApprovalService.java:136-180,502-515,648-661; AdmApprovalRepository.java:29-69`
- 확인 내용: PolicyRequest에는 policyCode/version/name/actionType만 있고 ownerModule·ownerCommand·targetType이 없다. canonical active policy도 findActivePolicy(actionType)로 조회한다. Owner tuple은 resolveOwnerPort(...supports(...))에서 별도 검증된다.
- 영향: 동일 actionType을 지원하는 다른 Owner/Command/Target tuple에 같은 정책이 재사용될 수 있다. 정책 자체가 위험조치 대상에 결속됐다는 보장이 없다.
- 필수 수정: Policy 정본/DB/API에 ownerModule·ownerCommand·targetType 또는 명시적 tuple binding set을 추가하고 canonical active lookup을 tuple 전체로 수행한다. 같은 actionType의 다른 tuple 재사용을 거부한다.
- 필수 검증: 동일 actionType·서로 다른 tuple 정책 분리, wrong-owner/wrong-command/wrong-target 거부, 중복 active tuple DB constraint, migration/rollback DB3.
- QA 판정: **미통과**

### QA-A-R6S12-002 [P0] Integration Closure UI 권한 Projection이 실제 로그인 Session과 단절

- 연결 Requirement: `FDEV-014,FDEV-017`
- Source·근거: `IntegrationClosurePage.vue:39-45,155-210; admSessionStore.ts:16-19,50-54,110-112; admConsoleStore.ts:17-26; integration-closure-r6.spec.ts:4-9`
- 확인 내용: 실제 서버 권한은 Pinia session.buttonIds에 저장된다. 새 화면은 document.documentElement.dataset.admPermissions를 한 번 읽는다. App/main/index/session 경로에는 dataset 설정 코드가 없고, E2E만 addInitScript로 값을 주입한다.
- 영향: 실제 권한을 가진 운영자도 모든 위험 버튼이 비활성화될 수 있다. 반대로 DOM dataset 조작에 UI 표시가 의존한다. Backend 권한과 UI 권한 모델이 분리된다.
- 필수 수정: useAdmSessionStore().hasButton 또는 reactive computed를 단일 권한 정본으로 사용한다. DOM dataset 기반 권한을 제거한다.
- 필수 검증: 실제 /adm/api/auth/me buttonIds projection, 로그인/로그아웃/권한변경 반응성, 권한별 버튼, DOM 조작 무영향, 실제 backend 403.
- QA 판정: **미통과**

### QA-A-R6S12-003 [P0] GitHub Browser Release Gate가 구조적으로 실행 불가

- 연결 Requirement: `FDEV-004,FDEV-017,FDEV-023`
- Source·근거: `playwright.config.ts:5-24,50-54; .github/workflows/cpf-r6-release-gates.yml:27-60; run-r6-release-gates.ps1:27-39`
- 확인 내용: Playwright는 CPF\_FRONTEND\_URL을 필수로 요구하지만 workflow가 설정하지 않고 Web server도 기동하지 않는다. Workflow는 chromium만 설치하지만 config는 chromium/firefox/webkit 전부 실행한다.
- 영향: CI에서 Browser Gate가 앱 검증 전에 실패하거나 브라우저 executable 부재로 실패한다. 작성된 E2E가 release evidence로 생성될 수 없다.
- 필수 수정: Frontend/Backend test server를 기동하고 health wait, CPF\_FRONTEND\_URL, release auth state/matrices를 설정한다. 모든 project browser를 설치하거나 release project를 명시한다.
- 필수 검증: clean GitHub runner에서 workflow 전체 PASS, 실제 authenticated server session, browser artifacts, exact SHA ledger.
- QA 판정: **미통과**

### QA-A-R6S12-004 [P0] R6 Behavior/Mutation Gate가 행위 검증을 흉내낸 false-green

- 연결 Requirement: `FDEV-022,FDEV-025`
- Source·근거: `verify-r6-behavior-contracts.py:6-24,42-62`
- 확인 내용: 검사는 Source token 존재/금지만 본다. Mutation은 original.replace(token,'') 후 token not in mutated를 확인할 뿐, 변형 Source에 대해 compile/test/runtime을 실행하지 않는다.
- 영향: 기능이 고장나도 문자열만 남으면 PASS한다. '18 behavior / 9 mutation PASS'를 제품 동작 증거로 사용할 수 없다.
- 필수 수정: 격리 worktree/temporary source에서 실제 mutation을 적용하고 targeted compile/test를 실행해 반드시 실패하는지 검증한다. Runtime route·permission·idempotency·secret inheritance를 행위 assertion으로 검증한다.
- 필수 검증: 각 mutation이 해당 Test를 실패시키고 원복 후 PASS, mutation survivor 0, exit code/log/evidence.
- QA 판정: **미통과**

### QA-A-R6S12-005 [P0] 현재 Result SHA와 Package/Evidence Provenance 불일치

- 연결 Requirement: `FDEV-001,FDEV-020,FDEV-022,FDEV-024`
- Source·근거: `FINDING_STATUS.csv; REQUIREMENT_STATUS.csv; cpf-docs/work/v9i/dev/r6s12/*`
- 확인 내용: 개발 Package 기준은 28f823...이지만 적용 후 master는 77db10...이다. 원장도 QA-R5I-001/003/020 부분 구현, QA-R5I-029 미완료, 모든 verification 미검증을 기록한다.
- 영향: 현재 master에서 재현되지 않은 package evidence를 완료 근거로 오인할 수 있다.
- 필수 수정: 77db10... 또는 후속 exact result SHA의 fresh clone에서 전체 gate를 재실행하고 manifest/evidence/result commit/clean tree를 다시 결속한다.
- 필수 검증: sourceSha=resultSha=evidenceSha, clean tree, manifest hash 0 mismatch, artifact provenance.
- QA 판정: **미통과**

### QA-A-R6S12-006 [P0] Target Runtime·DB3·Multi-process가 필수 Release Gate가 아님

- 연결 Requirement: `FDEV-004,FDEV-005,FDEV-006,FDEV-019,FDEV-023,FDEV-024`
- Source·근거: `.github/workflows/cpf-r6-release-gates.yml:3-16,56-60`
- 확인 내용: run\_db3/run\_multiprocess 입력 기본값은 false이고 pull\_request에서는 workflow\_dispatch inputs를 제공하지 않는다. Java25/Gradle9.1, DB3 live, broker/split-WAS/process-kill evidence도 현재 없다.
- 영향: 핵심 Runtime이 한 번도 실행되지 않은 상태로 PR Gate가 통과 가능한 구조다.
- 필수 수정: Release/merge 보호 workflow에서 DB3 및 multiprocess를 필수 job으로 분리하고 secrets 부재 시 SKIP이 아니라 BLOCKED/FAIL 처리한다.
- 필수 검증: Oracle/PostgreSQL/MariaDB install→migration→seed→upgrade→runtime→rollback, 2+ instance race, broker duplicate/DLQ, process kill/reconcile.
- QA 판정: **미통과**

### QA-A-R6S12-007 [P1] UI E2E가 실제 인증·권한·Backend 상태변경을 검증하지 않음

- 연결 Requirement: `FDEV-014,FDEV-017`
- Source·근거: `integration-closure-r6.spec.ts:4-44`
- 확인 내용: 권한은 addInitScript로 임의 주입하고 403은 page.route().fulfill()로 합성한다. 실제 auth/me, backend mutation, persistence, audit, server 409/422/429/503를 검증하지 않는다.
- 영향: 화면이 실제 시스템과 연결되지 않아도 E2E가 PASS할 수 있다.
- 필수 수정: Authenticated storageState와 실제 server를 사용한 E2E를 추가하고 Mock test와 Runtime E2E를 명확히 분리한다.
- 필수 검증: 실제 승인 요청→조회→결정→실행→감사, refresh persistence, 401/403/409/422/429/500/503.
- QA 판정: **미통과**

### QA-A-R6S12-008 [P1] 멱등성 Ledger가 사용자·Tenant·Session 간 격리되지 않음

- 연결 Requirement: `FDEV-012,FDEV-014`
- Source·근거: `integrationClosureIdempotency.ts:19-24,46-55,92-124; IntegrationClosurePage.vue:171`
- 확인 내용: 기본 저장소는 global localStorage이고 storage key도 전역 단일이다. fingerprint에는 operator/tenant/session/environment가 없다. 독립 harness에서 두 사용자 시뮬레이션이 같은 key를 재사용했다.
- 영향: 공용 브라우저·계정 전환·Tenant 전환에서 이전 운영자의 pending/confirmed key를 재사용할 수 있다.
- 필수 수정: server session identifier·tenant·operator·environment를 namespace/fingerprint에 포함하거나 sessionStorage와 server-side idempotency ledger를 사용한다. 로그아웃 시 정리한다.
- 필수 검증: operator A→logout→B, tenant A→B, release/environment 전환, confirmed TTL, shared workstation.
- QA 판정: **미통과**

### QA-A-R6S12-009 [P1] 멱등성 Unit Test가 저장소 불일치로 false-green

- 연결 Requirement: `FDEV-012,FDEV-025`
- Source·근거: `integrationClosureIdempotency.test.ts:7-14,24-31,57-61`
- 확인 내용: beforeEach는 기본 localStorage만 지우지만 Test는 sessionStorage를 사용한다. markApprovalConfirmed는 storage 인자를 생략해 localStorage를 사용한다. 민감 payload Test는 sessionStorage에 기록하고 localStorage를 검사한다. Harness에서 test contamination과 vacuous assertion을 재현했다.
- 영향: 테스트 순서 의존·오염과 무의미한 assertion으로 회귀를 놓친다.
- 필수 수정: 각 Test에서 동일 explicit Storage를 사용하고 beforeEach에 local/session 모두 clear한다. mark/resolve/clear storage를 일관되게 전달한다.
- 필수 검증: 랜덤 순서·병렬·반복 실행, Test isolation, 실제 production localStorage 경로.
- QA 판정: **미통과**

### QA-A-R6S12-010 [P1] 상태 조회가 두 권한을 묶으면서 버튼은 한 권한만 확인

- 연결 Requirement: `FDEV-014,FDEV-017`
- Source·근거: `IntegrationClosurePage.vue:70-75,155-156`
- 확인 내용: loadOperationalStatus는 cryptoStatus와 timeHealth를 Promise.all로 호출하지만 버튼은 admIntegrationCryptoStatus만 확인한다.
- 영향: Crypto 권한만 가진 사용자는 timeHealth 403 때문에 두 상태 모두 실패한다. 최소권한 모델과 UI 동작이 불일치한다.
- 필수 수정: 두 작업을 별도 버튼/결과로 분리하거나 두 권한 모두 요구하고 부분 성공을 개별 표시한다.
- 필수 검증: crypto-only, time-only, both, neither, one API 503/other 200.
- QA 판정: **미통과**

### QA-A-R6S12-011 [P1] Approval 화면 기본 Owner Command가 실제 Adapter에서 지원되지 않음

- 연결 Requirement: `FDEV-003,FDEV-014`
- Source·근거: `ApprovalsPage.vue:87-92; GatewayApprovalOwnerCommandAdapter.java:21-33,51-58`
- 확인 내용: UI 기본값은 GATEWAY\_BINDING\_CHANGE지만 Adapter는 APPROVE/ACTIVATE/BLOCK/RETIRE만 지원하고 actionType=ownerCommand를 요구한다.
- 영향: 초기 화면에서 기본 승인 요청이 즉시 validation 실패한다. 운영자는 샘플을 정상 경로로 오인한다.
- 필수 수정: 지원 Command를 server registry에서 선택하도록 하고 기본값을 유효 tuple로 바꾼다.
- 필수 검증: 각 지원 command dropdown, unsupported value 입력 불가, registry drift contract.
- QA 판정: **미통과**

### QA-A-R6S12-012 [P1] Approval 위험조치 UI에 권한·In-flight 중복제어가 없음

- 연결 Requirement: `FDEV-013,FDEV-014,FDEV-017`
- Source·근거: `ApprovalsPage.vue:97-105 및 template action buttons`
- 확인 내용: requestPending/decisionPending을 설정하지만 오류 시 finally reset이 없고 버튼 disabled 조건에 연결되지 않는다. Policy save/request/decision/execute/reconcile도 session.hasButton 기반 제어가 보이지 않는다.
- 영향: Double-click 중복 요청, 실패 후 영구 pending state, 권한 없는 버튼 노출, 위험조치 UX 오작동이 발생한다.
- 필수 수정: 공통 operation permission과 reactive pending guard를 모든 위험조치에 적용하고 finally에서 해제한다.
- 필수 검증: double click, network timeout, response lost, retry same key, 403, logout during request.
- QA 판정: **미통과**

### QA-A-R6S12-013 [P1] Approval Policy Steps가 Strict JSON 계약을 우회

- 연결 Requirement: `FDEV-003,FDEV-013,FDEV-016`
- Source·근거: `ApprovalsPage.vue:100; strictJsonObject.ts`
- 확인 내용: Payload는 parseStrictJsonObject를 쓰지만 policy stepsJson은 JSON.parse를 사용한다.
- 영향: 중복 key·Unicode normalized duplicate·unsafe integer가 브라우저에서 조용히 정규화돼 Backend/Hash 계약과 달라질 수 있다.
- 필수 수정: 정책 배열용 strict parser를 제공하고 각 object의 duplicate/precision/schema를 검증한다.
- 필수 검증: duplicate stepNo/key, NFC collision, unsafe number, null/unknown field, exact canonical hash.
- QA 판정: **미통과**

### QA-A-R6S12-014 [P1] Stale Generated API가 두 번째 계약 정본으로 잔존

- 연결 Requirement: `FDEV-016,FDEV-018,FDEV-020`
- Source·근거: `src/generated/integrationClosureApi.ts:10-38; features/integration-closure/integrationClosureApi.ts:1-48; verify-generated-client.mjs:45-68; .cpf-openapi-source.json`
- 확인 내용: stale generated file은 direct fetch와 구 replay query 계약을 유지한다. 실제 feature facade는 admInvokeOperation과 body contract를 사용한다. Marker generatedFiles에 stale file이 없고 verifier는 예상 외 generated file을 거부하지 않는다.
- 영향: 향후 Consumer가 잘못된 API를 import해 expectedVersion/idempotency/context를 누락할 수 있다.
- 필수 수정: stale file 삭제 승인을 요청하거나 canonical generated client로 대체하고 unexpected generated file gate를 추가한다.
- 필수 검증: Repository-wide import scan, exact generated set allowlist, stale signature compile-negative.
- QA 판정: **미통과**

### QA-A-R6S12-015 [P1] Correction Approval Consumer 경로의 201/200/409/422 계약 미완성

- 연결 Requirement: `FDEV-003,FDEV-014,FDEV-016`
- Source·근거: `AdmIntegrationClosureController.java:67-79; AdmApprovalController.java; AdmApprovalExceptionHandler.java:15-19`
- 확인 내용: requestCorrection은 항상 ResponseEntity.ok를 반환한다. Generic approval Controller의 created/replay 상태 구분이 Integration Closure consumer path로 전달되지 않는다.
- 영향: 신규 생성과 replay를 UI/Client/Audit가 구분할 수 없고 문서화된 HTTP 계약이 경로별로 달라진다.
- 필수 수정: Service가 created/replayed metadata를 반환하고 Integration Closure Controller도 201/200를 구분한다. conflict/validation advice도 동일 경로에 적용한다.
- 필수 검증: new=201, replay=200, collision/stale=409, invalid=422, generated client handling.
- QA 판정: **미통과**

### QA-A-R6S12-016 [P1] Approval Exception Handler가 Integration Closure Controller를 포함하지 않음

- 연결 Requirement: `FDEV-003,FDEV-014`
- Source·근거: `AdmApprovalExceptionHandler.java:15-22; AdmIntegrationClosureController.java`
- 확인 내용: @RestControllerAdvice(assignableTypes = AdmApprovalController.class)로 제한돼 있다. Integration Closure는 같은 Approval Service를 사용하지만 범위 밖이다.
- 영향: 동일 예외가 API 경로별로 다른 status/body로 반환될 수 있다.
- 필수 수정: 공통 package/annotation 기반 Advice로 확대하거나 Integration Closure 전용 handler를 추가한다.
- 필수 검증: 두 Controller에서 동일 conflict/validation exception의 status/body parity.
- QA 판정: **미통과**

### QA-A-R6S12-017 [P1] Release Runner가 첫 실패에서 중단되어 전체 실패 집계 불가

- 연결 Requirement: `FDEV-001,FDEV-023,FDEV-025`
- Source·근거: `run-r6-release-gates.ps1:18-44`
- 확인 내용: Invoke-Gate는 nonzero 즉시 throw한다. 독립 gate의 나머지는 실행되지 않고 finally summary만 작성된다.
- 영향: 첫 실패만 고치고 다음 실패를 반복 발견하며, 지침의 공통 원인별 전체 실패 집계와 상충한다.
- 필수 수정: 각 gate 실패를 ledger에 누적하고 독립 gate를 계속 실행한 뒤 마지막에 aggregate nonzero를 반환한다. 의존 gate만 명시적으로 skip한다.
- 필수 검증: 복수 의도적 실패에서 모든 독립 gate ledger 생성, final exit nonzero.
- QA 판정: **미통과**

### QA-A-R6S12-018 [P1] HMAC 승인 Proof Canonical Message 경계가 모호함

- 연결 Requirement: `FDEV-013`
- Source·근거: `AdmDataQualityApprovalProofService.java:20-38; CpfDataQualityCorrectionPort.java:20-45`
- 확인 내용: 필드를 escape 없는 newline으로 연결한다. quarantineId/ref/nonce에 newline·control char 금지가 없다. 독립 harness에서 서로 다른 field tuple이 같은 canonical message를 만드는 예를 구성했다.
- 영향: 현재 adapter 입력 제약에 따라 직접 exploit 가능성은 추가 확인이 필요하지만, 승인 proof 정본의 필드 경계가 암호학적으로 명확하지 않다.
- 필수 수정: 길이-prefix binary 또는 canonical JSON/CBOR로 서명하고 모든 식별자 control char를 거부한다.
- 필수 검증: delimiter/control-char corpus, distinct tuple non-collision, cross-language canonical vector.
- QA 판정: **미통과**

### QA-A-R6S12-019 [P2] UI 오류 계약에 422가 누락

- 연결 Requirement: `FDEV-014,FDEV-017`
- Source·근거: `IntegrationClosurePage.vue:51-61`
- 확인 내용: 오류 메시지 map은 400/401/403/404/409/429/500/503만 처리한다. 개발 주장은 validation=422다.
- 영향: 422가 raw Error message로 노출되고 사용자 안내·접근성 메시지가 계약과 달라진다.
- 필수 수정: 422와 Backend problem code를 명시 처리하고 field error focus를 제공한다.
- 필수 검증: 422 field validation, aria-live/role=alert, 첫 오류 focus.
- QA 판정: **미통과**

## 7. UI 실구현 여부 집중 판정

| 기능                        | Source 존재                   | 실제 Consumer       | 실제 Runtime 증명            | QA 판정   |
| ------------------------- | --------------------------- | ----------------- | ------------------------ | ------- |
| Integration Closure Route | 있음                          | Route registry 연결 | Browser 미실행              | 부분 구현   |
| Session 권한                | auth/me→Pinia 있음            | 새 화면이 Pinia 미사용   | E2E가 DOM 값 주입            | **미통과** |
| 상태 조회                     | API 두 개 연결                  | Promise.all       | partial permission 미처리   | **미통과** |
| Data Quality Validate     | canonical client 연결         | 있음                | actual backend E2E 없음    | 미검증     |
| Correction Approval       | API 연결                      | 있음                | 201/200 구분 없음            | **미통과** |
| Approval Execute          | API 연결                      | 있음                | actual single-use E2E 없음 | 미검증     |
| Replay                    | body contract 개선            | 있음                | DB/multi-instance 미실행    | 미검증     |
| Webhook DLQ/Replay        | API 연결                      | 있음                | actual broker/DLQ 미실행    | 미검증     |
| Approvals Policy UI       | Operation client 연결         | 있음                | default tuple invalid    | **미통과** |
| 접근성                       | aria/focus 일부               | shallow E2E       | axe/실제 browser 없음        | 미검증     |
| 반응형                       | CSS와 viewport test          | 있음                | Browser 미실행              | 미검증     |
| 오류 처리                     | 401/403/404/409/429/500/503 | 422 누락            | actual server matrix 없음  | 부분 구현   |

UI는 단순 버튼만 있는 수준은 아니며 실제 API facade 호출이 존재한다. 그러나 권한 Projection 단절과 synthetic E2E 때문에 **운영 기능이 실제 사용자 세션에서 정상 동작한다는 증명은 실패**했다. 따라서 '실기능 완료'가 아니라 'Source 구현은 있으나 운영 연결 미완성'으로 판정한다.

## 8. FDEV-001\~025 QA A 판정

| Requirement | QA A 판정    | 이유                                                     |
| ----------- | ---------- | ------------------------------------------------------ |
| `FDEV-001`  | **미통과**    | 현재 SHA provenance와 전체 실패 집계 불충족                        |
| `FDEV-002`  | **미검증**    | Java25 전체 Context/runtime 미실행                          |
| `FDEV-003`  | **미통과**    | 정책 tuple 결속·HTTP 경로·UI 기본 tuple 결함                     |
| `FDEV-004`  | **미검증**    | Java25/Gradle9.1 full build/publication 미실행            |
| `FDEV-005`  | **미검증**    | DB3 live lifecycle 미실행                                 |
| `FDEV-006`  | **미검증**    | Broker·multi-instance·split-WAS·process-kill 미실행       |
| `FDEV-007`  | **재확인 필요** | 전체 Repository API/SPI/Owner 회귀 미실행                     |
| `FDEV-008`  | **부분 구현**  | Approval/Data Quality 호출 경로는 있으나 정책·UI 계약 결함           |
| `FDEV-009`  | **재확인 필요** | Outbox/Notification 전체 범위 미검수                          |
| `FDEV-010`  | **미검증**    | Batch 전체 범위 미검수                                        |
| `FDEV-011`  | **미검증**    | Cache 전체 범위 미검수                                        |
| `FDEV-012`  | **부분 구현**  | 멱등성 ledger 개선됐으나 세션 격리·Test false-green                |
| `FDEV-013`  | **미통과**    | 정책 binding·HMAC canonicalization·UI 위험조치 권한            |
| `FDEV-014`  | **미통과**    | UI permission projection·HTTP/error·실제 E2E 불충족         |
| `FDEV-015`  | **미검증**    | BZA 전체는 QA A 동료검토 범위, runtime 근거 없음                    |
| `FDEV-016`  | **미통과**    | stale generated API·경로별 HTTP 계약 불일치                    |
| `FDEV-017`  | **미통과**    | Browser workflow 비실행·synthetic E2E                     |
| `FDEV-018`  | **재확인 필요** | Generator/EDU 전체와 unexpected generated artifact 검증 미완료 |
| `FDEV-019`  | **부분 검증**  | DB3 static parity만 확인, live vendor 미실행                 |
| `FDEV-020`  | **부분 구현**  | 전체 dead/garbage source scan과 post-apply hygiene 미완료    |
| `FDEV-021`  | **재확인 필요** | 문서 완료 주장과 실제 검증 불일치                                    |
| `FDEV-022`  | **미통과**    | false-green gate와 current SHA evidence 불결속             |
| `FDEV-023`  | **미통과**    | release workflow browser/DB3/multiprocess 필수성 결함       |
| `FDEV-024`  | **미통과**    | result commit/Codex/target runtime 미완료                 |
| `FDEV-025`  | **미통과**    | 행위 mutation·canonical catalog full gate 불충족            |

## 9. 필수 재검증 조건

다음은 통합 QA가 개발 최종 요건에 병합해야 할 QA A 조건이다. 이 문서 자체는 최종 통합 개발지침이 아니다.

1. Policy DB/API/Service를 Owner·Command·Action·Target tuple에 실제 결속한다.
2. UI 권한 정본을 Pinia server session buttonIds로 통합하고 DOM dataset 방식을 제거한다.
3. GitHub Browser workflow에서 실제 Backend/Frontend 서버, auth state, URL, browser 설치를 구성한다.
4. Synthetic E2E와 실제 Runtime E2E를 분리하고 실제 상태 변경·refresh·audit를 검증한다.
5. Token Gate를 실제 compile/test/runtime mutation으로 교체한다.
6. Idempotency namespace에 operator/tenant/session/environment를 포함하고 Test storage를 일치시킨다.
7. Approval UI 기본 tuple·권한·pending/finally·strict policy JSON을 수정한다.
8. stale generated API를 제거하거나 canonical 생성물로 통합하고 unexpected file gate를 추가한다.
9. Integration Closure correction approval도 201/200/409/422를 동일하게 구현한다.
10. HMAC message를 length-prefix 또는 canonical JSON/CBOR로 교체한다.
11. Release runner가 독립 실패를 끝까지 집계하도록 변경한다.
12. 후속 exact SHA fresh clone에서 Java25/Gradle9.1, Node22.18+, npm verify, Playwright, DB3, Multi-process, QA38/39/REV004, Codex를 실행한다.
13. 모든 Evidence와 Manifest를 후속 result SHA·clean working tree에 다시 결속한다.

## 10. 통합 QA 인계

- 이 파일은 QA A의 독립 결과이며 QA B 결과와 아직 병합하지 않았다.
- 통합 QA는 QA-A-R6S12-001\~019와 QA B Finding을 exact SHA 기준으로 중복/부분중복 교차표로 병합해야 한다.
- P0 6건 중 하나라도 남으면 통합 판정은 미통과다.
- 개발GPT 원장의 `완료`를 그대로 승계하지 말고 본 문서의 재판정과 Source 근거를 우선 대조한다.
- 최종 개발요건·최종 원장·Overlay·Merge 작업은 별도 통합 QA 세션에서만 수행한다.

## 11. Source Snapshot Hash

| 파일                                        | SHA-256                                                            |
| ----------------------------------------- | ------------------------------------------------------------------ |
| `IntegrationClosurePage.vue`              | `6cb4f1e078a92cb644e27c668d5f500b5f09eb15ffc173df0710cdadde61f495` |
| `integration-closure-r6.spec.ts`          | `b14f6c1d8857da1105a430762df4a2ee21ffaedec0bd38c5c951e75fca417add` |
| `integrationClosureIdempotency.ts`        | `2dfce2049965e437e88b999806e69b7706f800a94b54b1b85328ea1b5e09633a` |
| `integrationClosureIdempotency.test.ts`   | `4dcbd77e071b368e00f7eff7c3871b62344730901a75dd6cfbd448cb6bb71447` |
| `ApprovalsPage.vue`                       | `9ce184471f2430101b2d81cd09588daef22ef8f4d8aa673ed66feedaf17cb12d` |
| `AdmApprovalService.java`                 | `293e0210f27fb477293a337150f91e9f5d1a20ee0c5c5065eb785110ffa86937` |
| `AdmApprovalRepository.java`              | `eb6dcd9d33a7b71f21a88ad4d8c93a7f73f1728fc47a05f080aaf331afd5f22f` |
| `AdmDataQualityApprovalProofService.java` | `267929e2781ca0572ab03476af468acb69054d081c6ebc12eeedf0c2e33693ef` |
| `CpfDataQualityCorrectionPort.java`       | `8283bccd5b9647695e501dd3bc5db9e58bb50d95f13924f5e2ebe0b3b5a4f48f` |
| `playwright.config.ts`                    | `ded53cf344d711086db4b13154b024bb6fb85c655df20e00a186b69a308404e2` |
| `cpf-r6-release-gates.yml`                | `aaf13df8ba0bf47ed45b3c356d287207af01484d7a3852f77052c4f8fd2c3425` |
| `run-r6-release-gates.ps1`                | `ed416d683c250966e546764b748c1cc339f15615bca3be7973f77bf04d4fd70f` |
| `verify-r6-behavior-contracts.py`         | `b4f90a6f00ee2789b1ac304646e954915c927ecffb60bde65b538d3ea874c777` |
| `FINDING_STATUS.csv`                      | `ef63ede6ab6bdd38adf343d4fc9617a03a08b4d91b64bf006bc701b2d53f4f4b` |
| `REQUIREMENT_STATUS.csv`                  | `0e7f67a3031e1c46c1f75e28ab9f505378e1b27d3375878dbdfc411b4308eead` |
| `QA1_EXECUTION_LOG.txt`                   | `21b9f10e3e785d8bfe85ff32e77f51c726a3fb8bd6094163786f4d1f191905ee` |

## 12. QA 결론

`77db10ad9aff44ee422795080fb2e96b364c9d65`는 광범위한 보정이 반영된 개발본이지만 QA A 기준으로 최종 완료가 아니다. 정책 결속, UI 실사용 권한, Browser CI, 행위 Gate, result SHA Evidence에 P0가 존재한다. 따라서 **QA A 최종 판정은 미통과**이며 통합 QA는 본 단일 문서를 입력으로 최종 재개발 요건을 작성해야 한다.

## 10. 통합 QA가 개발 최종요건에 반영할 조건

아래는 통합 QA가 QA A·QA B 결과를 최종 개발지침으로 재구성할 때 빠뜨리면 안 되는 조건이다.

1. QA A P0 6건과 QA B P0 7건을 우선 Release Blocker로 유지한다.
2. ADM 63개 Route를 단일 canonical set으로 관리하고 Route Registry·Interaction Matrix·Component Consumer·OpenAPI·Generated Client를 exact set으로 맞춘다.
3. 12개 메뉴의 55개 빠진 기능을 실제 UI Consumer에 연결하거나 Requirement/매뉴얼에서 공식 제거 결정한다. 단순 expectedOperationIds 삭제로 false-green을 만들지 않는다.
4. ADM 메뉴별 검색·Paging·상세·CRUD/조치·권한·사유·승인·CAS·감사·401/403/404/409/422/429/500/503를 실제 Browser에서 검증한다.
5. Dashboard·Topology·Batch·Gateway·Incident·Notification의 freshness SLA와 실시간/갱신 방식을 구현하고 stale/reconnect를 검증한다.
6. EDU는 135건 수량만 PASS로 분리하고, 135건 Runtime Verification은 0/135에서 다시 시작한다.
7. EDU per-ID Test가 실제 Consumer를 호출하도록 synthetic state-machine Test와 product/runtime Test를 분리한다.
8. EDU-ADM 17건을 개발자 매뉴얼 정본과 일치시키고 실제 cpf-admin Owner/API/OpenAPI/Generated Client/Route/Browser 흐름으로 재구현한다.
9. EDU Runtime 인증은 caller header가 아니라 authenticated principal 기반으로 변경한다.
10. EDU Process child environment·temp payload Secret 안전성을 보완한다.
11. External ACK API/callback을 구현하고 202/UNKNOWN/reconcile를 검증한다.
12. Gateway 14와 Batch 30을 generic 성공 레코드가 아니라 시나리오별 실제 Runtime semantics로 검증한다.
13. exact 후속 SHA에서 Java25·Gradle9.1·Node22.18+·npm verify·Playwright·DB3·Broker·Multi-instance·Publication·Generator parity를 실행한다.
14. 모든 Evidence/Manifest를 후속 result SHA와 clean Working Tree에 결속한다.
15. Codex 독립 검수를 실행하고 결과를 동일 exact SHA에 결속한다.

## 11. Source·Evidence Hash

| Local QA Input                                        | SHA-256                                                            |
| ----------------------------------------------------- | ------------------------------------------------------------------ |
| `CPF_QA_A_R6S12_DETAILED_REVIEW_77db10ad_20260807.md` | `1bcf602f7347827b9079479d0bd452ef96c0e48ffb3006c5ed8e1d9ca154588f` |
| `routes.ts`                                           | `2bfc735b336cc2f0bc9e0c409e4fa328ef831c7c3ce0a239164aa74f3b2c840e` |
| `adm-route-operation-contract.ts`                     | `f48688fab8838a7936dbc9038250e5c7286494ff2c1e704e4953da531aaf7e17` |
| `cpf-operation-contract.ts`                           | `af20b8308c942b4a964d5afd34a924fdf8dc23103c59ca27e7ac97401dbfaa30` |
| `cpf-api.ts`                                          | `990bb6a1dee5f2b9f24a818f79c96da73d0e561c8cac0af0a07c088a0da589e0` |
| `CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv`       | `04230dd306298cbf14e01ae8c95caed9b0d61593ee8d7b1f28bc143a11e0eb6d` |
| `route-quality.spec.ts`                               | `23bf27eaaf654a4b1ca9a0b3d67f4da2891b2251c88667bc12e9f51e0b6ea04d` |
| `adm-route-contract.spec.ts`                          | `63ccb5120de9cb854e840ddfd18d8bf1d6c22e32a34a1ac2de89db930ea8cc37` |
| `manual-135-catalog.json`                             | `62d3c12f9ce7c3a4f203f6b15fdba9942c9acb4b8fd9df8eae890c709c78cb11` |
| `PACKAGE_INDEX.md`                                    | `a0d550907a415e2ef69a87a27b5212f4fce860b9a4bcbd07d955bcf4d83fc282` |
| `package-layout.json`                                 | `bdf9ca435262df6f8e20ccfc0839f064a9fadc0b8c6be8860336f18a674a009b` |
| `EduCapabilityController.java`                        | `7b28be4bdd91c1e7daf5f0e06eb966c48526c824de9bc0c7588ae7af1e5cd360` |
| `EduExecutionService.java`                            | `99c5a71d313bd279b118667c9b245a9e6754d68b2883014fcb28fc5963f2a5b1` |
| `AbstractManualEduTestSupport.java`                   | `bd57d074c18b7eae2423496821a1a16f952efcd96a60b32559c1e7cce00428fd` |
| `TestEduBusinessConsumers.java`                       | `156cdd74aa8230af772ef6c641e35fa1228ffd285638f0408ac9e9eae764895d` |
| `JdbcEduBusinessConsumer.java`                        | `b94abaf1062f54a161fcdf1e7dcfa5a7aad538be00f52511c7156af3791b2115` |
| `ProcessEduBusinessConsumer.java`                     | `3fce5daac6aa548aeab050745731dd9b04a9a6e883885968361fa387fad5fc49` |
| `invoke-reference-edu.ps1`                            | `761643b04c03c467074212ec261186bcaee6160c750d4f88c4ab75a32124a8d9` |
| `ReferenceGatewayBusinessConsumer.java`               | `df8235f62a4b4b3370986e0e4a2134520f98692620f810330ad2f40e52c80967` |
| `SpringBatchEduBusinessConsumer.java`                 | `72eb3d286e0ed62946ba5dc48efdb5c54f59bd727ea16da1d814d82d9d69f105` |
| `EduBatchScenarioWorker.java`                         | `551f9abfd05daadd6f62d545348813200e08c1d84bb6828cae17c29a8caa87eb` |
| `cpf-starter-catalog.json`                            | `35c67b9e95b1c343e54563e02d69b0f9da85d93e1c6bd335b713709ed0ec40d2` |
| `settings.gradle`                                     | `8ace4c59691ecceebf9fb03b62ddbffd3098ea4c8140443de7108c745a7d0f39` |

## 12. 최종 인계

- 본 문서는 QA A·QA B의 취합 결과다.
- Repository 파일 병합, 최종 Requirement 원장 수정, 최종 개발GPT 지침 생성은 별도 통합 QA 세션에서 수행한다.
- 현재 exact SHA의 최종 판정은 **미통과**다.
- ADM 63개와 EDU 135개를 완료로 승계하지 말고 본 문서의 정적·Runtime 분리 판정을 사용한다.
- 다음 검수는 후속 exact SHA에서 과거 Evidence를 자동 승계하지 않고 재실행한다.