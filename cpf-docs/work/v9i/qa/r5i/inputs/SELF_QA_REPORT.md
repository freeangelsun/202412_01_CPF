# CPF REV-004 통합 QA 전수검수 상세 리포트

## 1. 최종 판정

- QA 회차: `QA-REV004-S1`
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 exact SHA: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- 검수 시각: `2026-08-06T20:12:00+09:00`
- 전체 Requirement: `FDEV-001~FDEV-025` 25건
- QA 통과: **0건**
- QA 미통과: **25건**
- Finding: **16건 (P0 12, P1 4)**
- 전체 Release 판정: **미통과 / 재개발 요청**

최신 master의 정적 Source에는 Approval dual hash, UNKNOWN reconcile, 8개 OpenAPI-Route-Vue Consumer, Starter 39=6+33 및 retained inactive root와 같은 개선이 확인됐다. 그러나 최신 SHA 기준 Evidence가 재생성되지 않았고, 실제 승인 우회 가능 SPI, 교차 Draft 멱등키 유실, DB Runner secret transport 결함, Evidence 경로/0-byte 결함이 확인됐다. Java25 전체 Build, DB3, Browser, Broker, Multi-instance 등 필수 Runtime은 실행되지 않았으므로 통과 처리하지 않았다.

## 2. 기준선 및 독립성

GitHub Connector로 2026-08-06 최신 master가 `e7cc9ada86c871214a20862779f2433bc46fea1b`임을 재확인했다. 제출 R4 Package는 `a8be27a34bdac0b7c075e06d6e86571244c96421`를 baseline으로 하므로 최신 master QA 증거로 승계하지 않았다. QA는 제품 Source를 수정하지 않았고 Commit/Push/Branch/Tag/Reset/Restore/Stash/Clean/Delete를 수행하지 않았다.

## 3. 실행 및 검수 요약

| 구분 | 결과 | 핵심 내용 |
|---|---|---|
| 최신 master/변경 파일/Source 조회 | 수행 | commit 06_10, +6356/-1685, 핵심 Source/Test/Config/Frontend/Script/Manifest 직접 검수 |
| Approval capability 외부 생성 재현 | 실패 재현 | 외부 package에서 `ApprovedCorrection` 생성 성공 |
| Browser A→B→A 멱등성 재현 | 실패 재현 | 원 A key 회전 및 original confirm 실패 |
| OpenAPI/Route/API/Vue 정적 parity | 정적 통과 | 8/8, 전체 operationId 332 unique |
| Starter catalog 정적 parity | 정적 통과 | active 39=public 6+internal 33, retained 1, duplicate 0 |
| Evidence/Manifest provenance | 실패 | 과거 SHA, `.log` 참조/`.txt` 실제 파일, 0-byte runtime evidence |
| DB Runner secret boundary | 실패 | child env 미정리, URL argument embedded-secret guard 없음 |
| Java25/Gradle9.1 전체 Build | 미실행 | 현재 Java21, Gradle 없음 |
| PowerShell/Pester/Live DB3 | 미실행 | pwsh 및 DB3 없음 |
| Frontend full build/Playwright | 미실행 | isolated TS reproduction만 수행 |
| Broker/Multi-instance/Process Kill | 미실행 | runtime resource 없음 |

상세 실행 내역은 `QA_TEST_EXECUTION_LEDGER.csv`에 기록했다.

## 4. 주요 긍정 확인 사항

1. `AdmApprovalService`는 실행 전 hash 확인, 단회 reservation, reservation 후 재검증, UNKNOWN 보존과 observation-only reconcile 경로를 구현했다.
2. `DataQualityCorrectionApprovalOwnerCommandAdapter`는 reserved command 재조회, Snapshot hash, SoD, 승인 참여자, 만료, target/version 검증과 before/after hash를 구현했다.
3. Backend OpenAPI의 integration operation 8개가 route registry, API facade, Vue 실제 호출과 정적으로 일치한다. 전체 operationId 332개는 중복이 없다.
4. Starter catalog는 정적 데이터상 39개 active module, public 6, internal 33, retained inactive `openapi-webmvc` 1개이며 duplicate/active-retained overlap이 없다.
5. `settings.gradle`과 root `build.gradle`에는 missing/extra/duplicate/internal leak을 차단하는 strict closure가 구현돼 있다.

위 항목은 **정적 후보 충족**이며, 최신 SHA Runtime PASS를 의미하지 않는다.

## 5. 결함 상세

### QAF-REV004-001 [P0] 최신 master와 제출 Package/Evidence 기준 SHA 불일치

- Requirement: `FDEV-001,FDEV-022,FDEV-024`
- 근거: 최신 master=e7cc9ada86c871214a20862779f2433bc46fea1b; R4 BASELINE.md:5 및 PACKAGE_MANIFEST.json baselineSha는 a8be27a34bdac0b7c075e06d6e86571244c96421; REVIEW_INDEX.md:53도 a8be27a34bdac0b7c075e06d6e86571244c96421.
- 영향: 최신 master에서 생성되지 않은 증적을 현재 구현의 PASS 근거로 승계할 수 없으며 Package provenance와 QA 판정이 무효화된다.
- 수정 대상: cpf-docs/work/v9i/REVIEW_INDEX.md; cpf-docs/work/v9i/fdr/r4/BASELINE.md; PACKAGE_MANIFEST.json; SHA256SUMS.txt; PRODUCT_TREE_SHA256.txt; 전체 Evidence
- 수정 요구: exact SHA clean checkout에서 전체 Gate를 재실행하고 모든 Manifest/Hash/Evidence/원장/Review Index를 동일 SHA로 재생성한다.
- 재실행: `git rev-parse HEAD; git status --short; .\gradlew.bat --no-daemon clean check; 이후 Evidence/Manifest 생성기 실행 및 모든 baselineSha=e7cc9ada86c871214a20862779f2433bc46fea1b 확인`
- 성공 기준: 모든 현재 QA 문서와 Evidence가 e7cc9ada86c871214a20862779f2433bc46fea1b를 기록하고 Source/Manifest/Hash가 일치한다.
- 실패 기준: 과거 SHA, OVERLAY_NOT_APPLIED, PENDING 또는 현재 SHA와 다른 Hash가 하나라도 남음
- 미조치 위험: False Green, 잘못된 Release 승인, 재현 불가

### QAF-REV004-002 [P0] Evidence 원장 경로가 실제 파일과 불일치하고 핵심 Runtime Evidence가 0바이트

- Requirement: `FDEV-001,FDEV-022,FDEV-024`
- 근거: TEST_EXECUTION_LEDGER.csv와 DEVELOPMENT_REQUIREMENT_RESULT.csv는 e/*.log를 참조하지만 Repository의 실제 파일은 e/*.txt이다. idempotency_runtime.txt는 size=0이며 G04 PASS의 실행 출력·명령·Exit Code가 없다.
- 영향: Evidence 링크가 깨지고 PASS 판정을 독립 재검증할 수 없다. 특히 FDEV-014 멱등성 Runtime PASS는 증거가 없다.
- 수정 대상: cpf-docs/work/v9i/fdr/r4/TEST_EXECUTION_LEDGER.csv; DEVELOPMENT_REQUIREMENT_RESULT.csv; REQUIREMENT_STATUS.csv; e/**
- 수정 요구: 원장 참조 경로와 실제 파일명을 exact match로 맞추고, 각 Evidence에 명령·환경·시각·Exit Code·stdout/stderr·Source SHA를 기록한다. 0바이트 Evidence를 PASS로 사용하지 않는다.
- 재실행: `모든 evidence_ref를 Repository Root 기준으로 resolve하여 존재·size>0·SHA256·baselineSha를 검증하는 fail-closed Gate 실행`
- 성공 기준: 참조 파일 100% 존재, 필수 Evidence 0바이트 0건, 각 PASS에 독립 재현 정보 존재
- 실패 기준: 깨진 경로/0바이트/PASS without exit code가 1건이라도 존재
- 미조치 위험: 감사 부적합, 증적 위조 오인, 재검수 불가

### QAF-REV004-003 [P0] Approval 보정 Capability가 public SPI record로 외부에서 위조 가능

- Requirement: `FDEV-003,FDEV-007,FDEV-008,FDEV-013`
- 근거: CpfDataQualityCorrectionPort.java:15-36이 public interface/public record ApprovedCorrection을 노출한다. 외부 package qa.external에서 record 생성이 javac/java로 성공했다(evidence/approved_correction_forgeability.txt). InMemoryCpfDataQualityOperations.java:88-110은 reference 문자열·시각 존재만 검사하고 Framework 발급 Capability를 검증하지 않는다.
- 영향: CpfDataQualityCorrectionPort Bean/구현 참조를 획득한 Caller가 ADM 승인 원장을 거치지 않고 승인 metadata를 임의 생성해 보정을 호출할 수 있다. 문서상 sole consumer는 강제 경계가 아니다.
- 수정 대상: cpf-core/.../CpfDataQualityCorrectionPort.java; cpf-common/.../InMemoryCpfDataQualityOperations.java; AdmIntegrationClosureConfiguration.java; architecture boundary tests
- 수정 요구: Caller가 생성할 수 없는 internal capability 또는 승인 원장 검증 Port를 Mutation 경계에서 강제하고, public exported SPI에서 직접 mutation command 생성/호출을 제거한다. 외부 module compile negative test와 Spring Bean 접근 negative test를 추가한다.
- 재실행: `.\gradlew.bat :cpf-core:test :cpf-common:test :cpf-admin:test checkArchitectureOwnership; 외부 consumer fixture가 ApprovedCorrection 생성/직접 mutation에 compile 또는 runtime 실패해야 함`
- 성공 기준: ADM 예약·Snapshot 검증 없는 direct correction 100% 차단
- 실패 기준: 외부 package에서 command 생성 또는 correctionPort 직접 호출 가능
- 미조치 위험: 승인 우회, SoD 우회, 무단 데이터 변경

### QAF-REV004-004 [P0] Browser 멱등키 저장소가 단일 Slot이라 교차 Draft에서 원 Key를 유실

- Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- 근거: integrationClosureIdempotency.ts:13,44-65는 sessionStorage 단일 key에 한 상태만 저장한다. A→B→A 순서 실행 시 A key가 idem-A-original에서 idem-A-rotated로 변경되고 원 요청 confirm도 실패했다(evidence/idempotency_concurrency_reproduction.txt, exit_code=2). 기존 test는 교차 pending draft를 검증하지 않는다.
- 영향: 응답 유실 후 다른 Draft를 다룬 뒤 원 Draft를 재시도하면 새 Idempotency Key가 발급되어 중복 승인 요청이 생성될 수 있다.
- 수정 대상: integrationClosureIdempotency.ts; integrationClosureIdempotency.test.ts; IntegrationClosurePage.vue
- 수정 요구: fingerprint별 pending/confirmed map 또는 request-scoped durable state를 사용하고 TTL/최대개수/정리 정책을 둔다. A→B→A, 다중 Tab, refresh, 응답 유실, confirm interleave Test를 추가한다.
- 재실행: `npm ci; npm run typecheck; npm test -- --run; 교차 Draft 재현에서 A key가 최초 값과 동일하고 original confirm이 성공해야 함`
- 성공 기준: 같은 fingerprint는 다른 Draft 처리와 무관하게 같은 key 유지
- 실패 기준: A→B→A에서 A key 회전 또는 confirm state changed 발생
- 미조치 위험: 중복 위험조치/중복 승인/감사 원장 분기

### QAF-REV004-005 [P0] DB3 Runner의 child process가 모든 DB Password 환경변수를 상속

- Requirement: `FDEV-005,FDEV-013,FDEV-023`
- 근거: run-db3-lifecycle.ps1:52-59은 ProcessStartInfo 환경을 정리하지 않고, 111-128에서 세 Vendor Password가 현재 process 환경에 존재한다. 기본 child environment inheritance 때문에 각 Runner가 stdin 외에 Oracle/PostgreSQL/MariaDB 비밀번호 환경변수도 모두 받는다. 기존 Test는 argument만 검사하고 child environment를 검사하지 않는다.
- 영향: stdin-only secret transport 주장과 불일치하며 한 Vendor runner compromise가 세 Vendor credential을 모두 노출할 수 있다.
- 수정 대상: cpf-tools/verification/final-dev/run-db3-lifecycle.ps1; tests/run-db3-lifecycle.Tests.ps1; protocol verifier
- 수정 요구: ProcessStartInfo.Environment에서 모든 CPF_RUNTIME_*_PASSWORD를 제거하고 현재 Vendor secret만 stdin으로 전달한다. fake runner가 environment에 secret이 없음을 assert하도록 Test를 강화한다.
- 재실행: `Invoke-Pester .\cpf-tools\verification\final-dev\tests\run-db3-lifecycle.Tests.ps1; live DB3 runner 실행 시 child env dump test는 secret 0건`
- 성공 기준: Password는 child stdin에만 존재하고 args/env/log/audit에는 0건
- 실패 기준: child environment 또는 argument/log/audit에서 Password 발견
- 미조치 위험: DB3 Credential 유출 및 횡적 확산

### QAF-REV004-006 [P1] JDBC URL을 command line에 전달하면서 embedded credential 차단이 없음

- Requirement: `FDEV-005,FDEV-013,FDEV-023`
- 근거: run-db3-lifecycle.ps1:140-145가 --url=$url을 ArgumentList에 추가한다. URL에 password/token/userinfo query가 포함되지 않았는지 검증/마스킹하는 코드와 Test가 없다.
- 영향: 운영자가 credential이 포함된 JDBC URL을 제공하면 process listing과 Evidence에 secret이 노출될 수 있다.
- 수정 대상: run-db3-lifecycle.ps1; DB3 tests; 운영 Runbook
- 수정 요구: Vendor별 JDBC URL parser/allowlist를 적용해 user/password/token/secret parameter와 URI userinfo를 fail-fast하고 Evidence에는 host/service 수준의 masked URL만 기록한다.
- 재실행: `credential 포함 URL 3 Vendor negative test; process argument capture; secret scan`
- 성공 기준: embedded credential URL은 runner 시작 전 거부
- 실패 기준: password/token이 포함된 URL이 child argument로 전달
- 미조치 위험: 명령행·진단도구·감사파일 Secret 노출

### QAF-REV004-007 [P0] Java 25·Gradle 9.1 fresh clone 전체 Build/Test/Publication 미실행

- Requirement: `FDEV-004,FDEV-020,FDEV-022`
- 근거: R4 TEST_AND_EVIDENCE.md와 ENVIRONMENT_CAPABILITY_MATRIX.csv가 full Gradle을 NOT_EXECUTED로 명시한다. 현재 QA runtime도 Java 21이며 Gradle이 없다.
- 영향: 컴파일, Dependency, Spring Context, Test, Publication, BOM Consumer 회귀를 판정할 수 없다.
- 수정 대상: 전체 Repository; build.gradle; settings.gradle; included builds; publication gates
- 수정 요구: clean exact-SHA checkout + Java25/Gradle9.1에서 root clean/check/test/publication/consumer를 실행하고 raw logs와 reports를 보존한다.
- 재실행: `.\gradlew.bat --version; .\gradlew.bat --no-daemon clean check; .\gradlew.bat --no-daemon publicationGate`
- 성공 기준: 모든 명령 exit 0, Java 25 class major/version 및 publication consumer 검증 통과
- 실패 기준: 미실행 또는 하나라도 non-zero/failed/skipped critical gate
- 미조치 위험: 컴파일 불가 Release, 회귀 유입

### QAF-REV004-008 [P0] Spring ApplicationContext 및 실제 Runtime Wiring 증거 없음

- Requirement: `FDEV-002,FDEV-008,FDEV-014`
- 근거: AdmIntegrationClosureConfiguration의 정적 Bean wiring은 존재하지만 기본/override/disabled/invalid 4종 Context와 Controller 호출을 최신 SHA에서 실행한 Evidence가 없다.
- 영향: Missing/Duplicate Bean, property 조건, production ephemeral provider 차단, 실제 Controller 생성 여부를 확인할 수 없다.
- 수정 대상: AdmIntegrationClosureConfiguration.java; AdmApplication.java; properties; Context tests
- 수정 요구: ApplicationContextRunner/@SpringBootTest로 기본/override/disabled/invalid 설정과 분리 WAS profile을 실행한다.
- 재실행: `.\gradlew.bat :cpf-admin:test --tests "*AdmIntegrationClosure*"`
- 성공 기준: 의도된 profile에서만 Bean/Controller 생성, duplicate/missing은 fail-closed
- 실패 기준: Context start failure 또는 잘못된 provider 선택
- 미조치 위험: 운영 기동 실패/부적절한 InMemory provider 사용

### QAF-REV004-009 [P0] Oracle·PostgreSQL·MariaDB 실제 Lifecycle 미실행

- Requirement: `FDEV-005,FDEV-019,FDEV-020,FDEV-023`
- 근거: R4는 Python regex/fake protocol만 PASS이며 PowerShell/Pester와 live DB3 install/migration/upgrade/rollback/query가 NOT_EXECUTED이다.
- 영향: SQL 문법·Vendor 차이·Rollback·Index/FK·Seed·Migration 순서·Driver/권한 결함을 발견하지 못한다.
- 수정 대상: DB3 SQL/Migration/Runner 전체
- 수정 요구: 비운영 DB3에서 install→migration→upgrade→runtime query→rollback→재적용→drift/failure recovery를 실제 실행한다.
- 재실행: `pwsh -NoProfile -File .\cpf-tools\verification\final-dev\run-db3-lifecycle.ps1 -ExpectedHead e7cc9ada86c871214a20862779f2433bc46fea1b`
- 성공 기준: 3 Vendor 각각 SUCCEEDED, actualHead 일치, rollback/reapply/query 증적 존재
- 실패 기준: Vendor 1개라도 미실행/실패/INVALID_AUDIT/MISSING_AUDIT
- 미조치 위험: 운영 배포 실패·Rollback 불가·데이터 손상

### QAF-REV004-010 [P0] Frontend 전체 Build·Vitest·Playwright·접근성·반응형 미실행

- Requirement: `FDEV-014,FDEV-016,FDEV-017`
- 근거: OpenAPI 8개 operation의 정적 parity와 Vue consumer는 확인됐지만 R4가 npm ci/lint/full typecheck/Vitest/build/Playwright를 NOT_EXECUTED로 명시한다.
- 영향: 실제 bundling, generated client type drift, session/CSRF, 401~503 UI, keyboard/a11y, responsive 동작을 보증할 수 없다.
- 수정 대상: cpf-admin/frontend 전체; integration-closure; generated client; routes
- 수정 요구: 지원 Node에서 lockfile clean install 후 lint/typecheck/test/build/Playwright 및 a11y/responsive matrix를 실행한다.
- 재실행: `Set-Location .\cpf-admin\frontend; npm ci; npm run lint; npm run typecheck; npm test -- --run; npm run build; npx playwright test`
- 성공 기준: 전 단계 exit 0, 8 operations 실제 network consumer와 401/403/404/409/429/500/503 화면 검증
- 실패 기준: 미실행/실패/console error/a11y critical violation
- 미조치 위험: 운영 UI 장애·위험조치 오동작

### QAF-REV004-011 [P0] Broker·다중 Instance·분리 WAS·Process Kill·부분 실패·UNKNOWN Runtime 미실행

- Requirement: `FDEV-006,FDEV-009,FDEV-010,FDEV-011,FDEV-012`
- 근거: R1 자체검수와 R4 Open Issues가 Broker/multi-process/process kill/UNKNOWN Runtime을 미검증으로 남긴다. 최신 SHA Evidence가 없다.
- 영향: 동시성, fencing, failover, outbox/DLQ, scheduler/worker, durable invalidation과 recovery 보장이 없다.
- 수정 대상: 비동기/Batch/Cache/Runtime 전체 integration test packages
- 수정 요구: 2+ instance, Broker, forced kill, network partition, duplicate/retry, partial commit, UNKNOWN→reconcile 시나리오를 실제 실행한다.
- 재실행: `Repository의 canonical multi-instance/broker/process-kill runtime harness 전체 실행 및 instance/transaction/execution trace 수집`
- 성공 기준: 중복 side effect 0, 유실 0, fencing 유효, UNKNOWN이 deterministic terminal state로 reconcile
- 실패 기준: 미실행/중복/유실/stuck UNKNOWN/수동 DB 수정 필요
- 미조치 위험: 금융 거래 중복·유실·운영 복구 실패

### QAF-REV004-012 [P0] Starter strict physical closure는 정적 구현만 확인되고 실제 QA38/QA39 미실행

- Requirement: `FDEV-025,FDEV-004,FDEV-020`
- 근거: settings.gradle:106-190과 build.gradle:1848-1931에 strict equality가 존재하고 catalog 39=6+33/retained 1 정적 정합성은 확인됐다. 그러나 제출 Evidence는 synthetic full 또는 overlay partial이고 qa38/qa39는 not_executed이다.
- 영향: 실제 Repository physical tree, settings, BOM publication, generated catalog의 누락/추가/경로 drift를 최종 판정할 수 없다.
- 수정 대상: settings.gradle; build.gradle; canonical/release catalogs; cpf-starters physical tree; BOM builds; QA38/39 gates
- 수정 요구: fresh clone 실제 tree에서 QA38/QA39/Gradle configuration/public+internal BOM consumer를 실행한다. retained openapi-webmvc는 승인 전 삭제하지 않는다.
- 재실행: `.\gradlew.bat checkQa39CanonicalStarterClosure checkQa39FinalCanonical checkQa39EvidenceTruth; python -B cpf-tools/verification/verify_starter_catalog.py --root .`
- 성공 기준: active 39=6+33, retained 1, missing/extra/duplicate/internal leak 0
- 실패 기준: synthetic/overlay-only 또는 실제 tree mismatch 1건 이상
- 미조치 위험: Starter 누락·Internal 공개·Consumer build 실패

### QAF-REV004-013 [P1] REV-004 정적 Gate가 실제 승인 우회·교차 Draft 결함을 검출하지 못하는 False-Green

- Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- 근거: verify-rev004-overlay.py:65-68은 public mutation 제거를 CpfDataQualityOperations Javadoc 문자열로 판정하고 CpfDataQualityCorrectionPort의 public constructor를 검사하지 않는다. 90-96은 sessionStorage/상태 문자열만 검사하고 다중 pending draft를 검증하지 않는다.
- 영향: 실제 취약한 Source가 있어도 Gate가 PASS하여 self-review가 거짓 양성으로 종료된다.
- 수정 대상: cpf-tools/verification/final-dev/verify-rev004-overlay.py; 관련 tests
- 수정 요구: 문자열 존재 검사를 compile-negative/architecture/runtime assertions로 교체하고 이번 두 재현을 regression test로 편입한다.
- 재실행: `변경 전 Gate가 두 재현에서 실패하고 수정 후 성공하는 mutation test 실행`
- 성공 기준: Forgeable capability와 A→B→A key rotation을 Gate가 반드시 FAIL
- 실패 기준: 현재 결함 Source에서 overlay verifier가 PASS
- 미조치 위험: 반복 False Green 및 QA 누락

### QAF-REV004-014 [P1] 동시 실행 선점 충돌의 HTTP 409 계약이 제한적

- Requirement: `FDEV-003,FDEV-012,FDEV-016`
- 근거: AdmApprovalService.java:263-265의 concurrent reservation conflict는 CpfValidationException을 발생시키지만 AdmIntegrationClosureController.java:80-83은 결과 code DQ-VERSION-CONFLICT만 409로 변환한다. 해당 선점 충돌의 409 mapping Test가 없다.
- 영향: Client가 재조회/재시도해야 할 동시성 충돌을 일반 400/500으로 받을 가능성이 있어 계약·UX와 불일치한다.
- 수정 대상: AdmApprovalService.execute; AdmIntegrationClosureController.executeCorrection; global exception mapping; controller tests; OpenAPI
- 수정 요구: typed conflict exception/error code로 표준화하고 reservation/version/state conflict를 모두 409로 매핑하며 Controller integration test를 추가한다.
- 재실행: `.\gradlew.bat :cpf-admin:test --tests "*AdmApproval*Conflict*" --tests "*AdmIntegrationClosureControllerTest*"`
- 성공 기준: 모든 optimistic/reservation/state conflict가 409 + 표준 error body
- 실패 기준: 동시 선점 충돌이 409가 아님
- 미조치 위험: 잘못된 재시도·중복 요청·운영자 오판

### QAF-REV004-015 [P1] 전체 Repository Secret/Hygiene scan 및 exact-SHA clean Working Tree 증거 없음

- Requirement: `FDEV-013,FDEV-020,FDEV-022`
- 근거: R4는 targeted local secret scan만 수행했고 GitHub Advanced Security/full checkout 검사는 NOT_EXECUTED이다. 현재 QA는 Connector read-only라 Working Tree를 검증할 수 없다.
- 영향: 변경 범위 밖 credential, generated secret, ignored artifact, untracked build output를 배제할 수 없다.
- 수정 대상: 전체 repository/ignore/build outputs/evidence
- 수정 요구: fresh clone exact SHA에서 full secret scan, git diff --check, tracked/ignored hygiene, package payload allowlist를 실행한다.
- 재실행: `git status --short --branch; git diff --check; repository canonical secret/hygiene gates; GitHub secret scanning`
- 성공 기준: clean tree, secret 0, ignored/tracked temporary artifact 0
- 실패 기준: secret/hygiene finding 또는 dirty tree
- 미조치 위험: Credential 유출·재현성 상실

### QAF-REV004-016 [P0] Codex 독립 검수·보완이 완료되지 않은 상태에서 QA 진입

- Requirement: `FDEV-024`
- 근거: R4에는 CODEX_REVIEW_REQUEST.md만 있고 독립 Codex 결과/Evidence가 없다. 인수인계도 Codex 미수행을 명시했다.
- 영향: 개발GPT 자체검수와 독립 검수 분리가 충족되지 않아 QA 전 단계의 품질 방어선이 비어 있다.
- 수정 대상: Codex review result/evidence; integrated ledger Codex_* columns
- 수정 요구: 재개발 후 Codex가 독립적으로 source/build/test/evidence를 검수하고 Codex 전용 결과를 기록한 뒤 QA 재검수한다.
- 재실행: `CODEX_REVIEW_REQUEST.md의 exact SHA 기준 명령 전체 수행`
- 성공 기준: Codex 결과가 최신 SHA와 연결되고 미완료 0
- 실패 기준: Codex 결과 없음/과거 SHA/실행 누락
- 미조치 위험: 동일 검수 관점 편향·결함 누락

## 6. Requirement별 QA 판정

| ID | 판정 | 상태 | 주요 Finding |
|---|---|---|---|
| FDEV-001 | 미통과 | 부분 구현 / 실패 | QAF-REV004-001,QAF-REV004-002 |
| FDEV-002 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-008 |
| FDEV-003 | 미통과 | 부분 구현 / 실패 | QAF-REV004-003,QAF-REV004-004,QAF-REV004-013,QAF-REV004-014 |
| FDEV-004 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-007,QAF-REV004-012 |
| FDEV-005 | 미통과 | 부분 구현 / 실패 | QAF-REV004-005,QAF-REV004-006,QAF-REV004-009 |
| FDEV-006 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-011 |
| FDEV-007 | 미통과 | 부분 구현 / 실패 | QAF-REV004-003 |
| FDEV-008 | 미통과 | 부분 구현 / 실패 | QAF-REV004-003,QAF-REV004-008 |
| FDEV-009 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-011 |
| FDEV-010 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-011 |
| FDEV-011 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-011 |
| FDEV-012 | 미통과 | 부분 구현 / 실패 | QAF-REV004-004,QAF-REV004-011,QAF-REV004-013,QAF-REV004-014 |
| FDEV-013 | 미통과 | 부분 구현 / 실패 | QAF-REV004-003,QAF-REV004-005,QAF-REV004-006,QAF-REV004-015 |
| FDEV-014 | 미통과 | 부분 구현 / 실패 | QAF-REV004-004,QAF-REV004-008,QAF-REV004-010,QAF-REV004-013 |
| FDEV-015 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-010 |
| FDEV-016 | 미통과 | 부분 구현 / 실패 | QAF-REV004-004,QAF-REV004-010,QAF-REV004-013,QAF-REV004-014 |
| FDEV-017 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-010 |
| FDEV-018 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-007 |
| FDEV-019 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-009 |
| FDEV-020 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-007,QAF-REV004-009,QAF-REV004-012,QAF-REV004-015 |
| FDEV-021 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-001 |
| FDEV-022 | 미통과 | 부분 구현 / 실패 | QAF-REV004-001,QAF-REV004-002,QAF-REV004-007,QAF-REV004-015 |
| FDEV-023 | 미통과 | 부분 구현 / 실패 | QAF-REV004-005,QAF-REV004-006,QAF-REV004-009 |
| FDEV-024 | 미통과 | 부분 구현 / 실패 | QAF-REV004-001,QAF-REV004-002,QAF-REV004-016 |
| FDEV-025 | 미통과 | 재확인 필요 / 미검증 | QAF-REV004-012 |


## 7. 재개발 우선순위

1. P0 승인 우회 경계(`QAF-REV004-003`)와 Browser 멱등키(`QAF-REV004-004`)를 먼저 수정한다.
2. DB Runner child environment/URL secret 경계(`QAF-REV004-005`, `006`)를 수정하고 Pester negative test를 추가한다.
3. False-Green verifier(`QAF-REV004-013`)를 실제 compile-negative/runtime regression gate로 교체한다.
4. `e7cc9ada86c871214a20862779f2433bc46fea1b` clean checkout에서 Java25 Build, Frontend, DB3, QA38/39, Broker/Multi-instance를 실행한다.
5. Evidence 경로·내용·Hash·원장을 재생성하고 Codex 독립검수 후 QA 재제출한다.

## 8. QA 재검수 진입 조건

- 모든 P0/P1 Finding 조치 완료
- 개발GPT 자체검수 결과가 최신 SHA에 연결
- Codex 독립검수·보완 완료
- Java25/Gradle9.1 root clean/check/publication exit 0
- Frontend lint/typecheck/Vitest/build/Playwright exit 0
- Oracle/PostgreSQL/MariaDB lifecycle exit 0
- QA38/39 actual repository root 통과
- Broker/Multi-instance/Process Kill/UNKNOWN recovery 통과
- Evidence 참조 경로 100% 존재, 0-byte 0건, SHA/Manifest/원장 exact match
- clean Working Tree 증거

## 9. 보호·삭제·Git 안전

- 제품 Source 수정: 없음
- Repository Git write: 없음
- 삭제 수행: 없음
- 보호 경로 변경: 없음
- `cpf-starters/openapi-webmvc`: 사용자 승인 전 삭제 금지, retained inactive 상태 유지

## 10. 산출물

- `CPF_QA_REV004_FULL_REVIEW_20260806.md`: 본 상세 리포트
- `INTEGRATED_REQUIREMENT_STATUS_QA_REV004.csv`: 25건 QA 컬럼 갱신 통합 원장
- `QA_REQUIREMENT_STATUS.csv`: 판정 요약
- `QA_FINDINGS.csv`: 16개 Finding 구조화 원장
- `QA_REWORK_REQUEST.md`: 개발GPT 실행형 재개발 요청
- `QA_TEST_EXECUTION_LEDGER.csv`: 수행/실패/미실행 Gate
- `QA_SOURCE_REVIEW_MATRIX.csv`: Source 검수 범위
- `evidence/**`: QA 독립 재현·정적 검증 근거
