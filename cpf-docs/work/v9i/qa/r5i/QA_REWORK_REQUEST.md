# CPF REV-004 QA R5I 재개발 요청

- Target SHA reviewed: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- QA result: **미통과**
- Target role: 개발GPT → 자체검수 → Codex 독립검수 → QA R6
- Findings: 29 (P0 12, P1 13, P2 4)

## 공통 제출 조건

모든 finding은 동일 Requirement ID로 수정하고 exact result SHA, clean working tree, 실행 명령, 환경/도구 버전, 시작·종료 시각, Exit Code, sanitized stdout/stderr, 산출물 SHA-256, negative/concurrency/runtime Evidence를 제출한다. `READY`, `PLANNED`, `NOT_EXECUTED`는 PASS가 아니다.

## P0

### QA-R5I-001 — Management/Baseline

- Requirement: `FDEV-001,FDEV-021,FDEV-022,FDEV-024`
- 확인: `PACKAGE_AND_REPOSITORY_CONFIRMED`
- 결함: 최신 master는 e7cc9ada(06_10)인데 REVIEW_INDEX, FINAL_MANAGEMENT_STATE, FINAL_INTEGRITY, fdr/r4 BASELINE/REQUIREMENT_STATUS/PACKAGE_MANIFEST가 a8be27/2929163/2a0136/cb3b2a를 현재 기준처럼 혼용한다.
- 영향: QA/개발/Codex가 서로 다른 Source를 기준으로 판정하여 완료·회귀·Evidence 연결이 무효화된다.
- 대상: `cpf-docs/work/v9i/REVIEW_INDEX.md; cpf-docs/work/v9i/evidence/FINAL_*.json; cpf-docs/work/v9i/fdr/r4/**`
- 필수 수정: 최신 master에서 instruction_basis_sha, work_start_sha, documentation_delta_sha, result_commit_sha, evidence_source_sha를 분리하고 모든 현재 상태·Manifest·Evidence를 재생성한다.
- 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기준: 모든 current/result 문서의 result_commit_sha=e7cc9ada, Evidence별 실제 SHA 일치, 과거 SHA는 historical 필드에만 존재.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: QA/개발/Codex가 서로 다른 Source를 기준으로 판정하여 완료·회귀·Evidence 연결이 무효화된다.

### QA-R5I-002 — Runtime/CI

- Requirement: `FDEV-004,FDEV-005,FDEV-006,FDEV-017,FDEV-020,FDEV-024,FDEV-010,FDEV-011,FDEV-015,FDEV-018`
- 확인: `PEER_CONNECTOR_CONFIRMED_AND_LOCAL_ENVIRONMENT_BLOCKED`
- 결함: e7cc9ada에 연결된 GitHub status와 workflow run이 0건이며 Java25/Gradle9.1, full npm verify, Playwright, Pester, DB3, broker/multi-process가 실행되지 않았다. 추가 독립 환경 probe에서도 GitHub DNS 실패, JDK 21, Node 22.16.0, PowerShell 부재가 확인되어 Target Runtime PASS를 만들 수 없었다.
- 영향: fresh clone 재현성·컴파일·테스트·배포·다중 인스턴스·DB Vendor·브라우저 품질을 판정할 근거가 없다.
- 대상: `GitHub commit status/workflow lookup; fdr/r4/TEST_AND_EVIDENCE.md; TEST_EXECUTION_LEDGER.csv`
- 필수 수정: clean checkout exact SHA에서 지정 Target Runtime 명령을 실행하고 URL, tool version, command, exit code, stdout/stderr hash, artifact hash를 제출한다.
- 재실행: `.\gradlew.bat --no-daemon clean aggregateQualityBuild publicationGate; npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기준: 모든 필수 Gate PASS, git status clean, 실행 SHA와 Evidence SHA 동일. 하나라도 미실행/실패면 QA 미통과.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: fresh clone 재현성·컴파일·테스트·배포·다중 인스턴스·DB Vendor·브라우저 품질을 판정할 근거가 없다.

### QA-R5I-003 — Evidence Integrity

- Requirement: `FDEV-001,FDEV-014,FDEV-016,FDEV-020,FDEV-022,FDEV-024`
- 확인: `PACKAGE_EVIDENCE_CONFIRMED`
- 결함: idempotency_runtime.txt는 0 byte인데 Ledger는 PASS이며, Requirement CSV는 .log를 참조하지만 저장 파일은 .txt이다. openapi_idempotent_1/2의 935f... 해시는 실제 OpenAPI 22d22...와 불일치한다.
- 영향: 존재하지 않거나 다른 산출물의 Evidence로 PASS를 주장하여 감사 추적성과 재현성이 붕괴한다.
- 대상: `cpf-docs/work/v9i/fdr/r4/e/**; REQUIREMENT_STATUS.csv; TEST_EXECUTION_LEDGER.csv; PACKAGE_MANIFEST.json`
- 필수 수정: Evidence를 실제 명령 출력으로 재생성하고 경로·확장자·SHA를 원장/Manifest와 exact match한다. 빈 Evidence를 금지한다.
- 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기준: orphan/missing/empty/hash mismatch 0, 모든 Evidence에 command/environment/time/exit/source SHA 포함.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 존재하지 않거나 다른 산출물의 Evidence로 PASS를 주장하여 감사 추적성과 재현성이 붕괴한다.

### QA-R5I-004 — Governance/Delete Safety

- Requirement: `FDEV-020,FDEV-021,FDEV-024`
- 확인: `COMMIT_DIFF_CONFIRMED`
- 결함: 06_08 기준에 존재하던 cpf-document-quality-r9.svg가 최신 master에서 삭제되었고 Documentation Delete Manifest는 삭제를 지시하지만 사용자 승인 Evidence가 없다.
- 영향: 보호 문서/SVG 무단 삭제는 문서 정본 보존·삭제 승인 규칙을 위반한다.
- 대상: `compare a8be27..e7cc9ada; cpf-docs/deliverables/evidence/CPF_DOCUMENTATION_DELETE_MANIFEST.txt`
- 필수 수정: 명시 승인 전 해당 SVG를 복원하거나 승인 ID/일시/범위를 원장에 기록한다. 다른 보호 문서 삭제 여부도 재검사한다.
- 재실행: `git diff --name-status a8be27a34bdac0b7c075e06d6e86571244c96421..HEAD -- cpf-docs/deliverables cpf-docs/guides cpf-docs/assets/manuals`
- 성공 기준: 승인 없는 삭제 0; 삭제 시 exact path 승인과 before/after manifest 존재.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 보호 문서/SVG 무단 삭제는 문서 정본 보존·삭제 승인 규칙을 위반한다.

### QA-R5I-005 — Approval Policy Binding

- Requirement: `FDEV-003,FDEV-008,FDEV-013`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: requestApproval은 client가 policyCode/version을 주면 enabled/effective 기간을 검증하지 않고, 해당 정책을 ownerModule/ownerCommand/targetType과 서버 Registry로 결속하지 않는다.
- 영향: 비활성·구버전·저위험 정책을 임의 Owner Command에 적용하여 승인 강도를 낮출 수 있다.
- 대상: `AdmApprovalService.requestApproval; AdmApprovalRepository.findPolicy/findActivePolicy; Approval Controller/UI`
- 필수 수정: 정책 선택은 서버 action/owner/command/target Registry로 제한하고 explicit version도 enabled/effective/authorized override를 검증한다. negative integration test를 추가한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: expired/disabled/future/wrong-action/wrong-owner/wrong-target policy 요청 모두 4xx; Owner mutation 0; 감사 기록 존재.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 비활성·구버전·저위험 정책을 임의 Owner Command에 적용하여 승인 강도를 낮출 수 있다.

### QA-R5I-006 — Secure Default/Profile

- Requirement: `FDEV-002,FDEV-003,FDEV-013,FDEV-020`
- 확인: `PEER_EXACT_BLOB_CONFIRMED`
- 결함: application.yml이 spring.profiles.active=local을 기본값으로 고정하고 local profile은 integration-closure와 ephemeral providers를 기본 true로 한다.
- 영향: 운영 배포에서 profile 누락 시 in-memory Data Quality/Webhook 운영 기능이 활성화되는 fail-open 구성이다.
- 대상: `cpf-admin/src/main/resources/application.yml; application-adm-local.yml; application-adm-prod.yml`
- 필수 수정: active profile 기본값을 제거하고 prod/unknown 환경에서 ephemeral provider를 절대 활성화하지 않는 fail-fast guard와 배포 테스트를 추가한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "*AdmIntegrationClosureConfigurationTest"`
- 성공 기준: profile 미지정/잘못된 profile에서 운영기능 비활성 또는 startup fail; prod에서 ephemeral bean 0.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 운영 배포에서 profile 누락 시 in-memory Data Quality/Webhook 운영 기능이 활성화되는 fail-open 구성이다.

### QA-R5I-007 — Spring Bean Wiring

- Requirement: `FDEV-002,FDEV-003`
- 확인: `DIRECT_SOURCE_GRAPH_CONTRADICTION;RUNTIME_REQUIRED`
- 결함: customerOverridesWinOverDefaultProviders Test는 CpfDataQualityOperations만 공급하지만 Configuration의 Owner Adapter는 별도 CpfDataQualityCorrectionPort를 필수 주입한다. ephemeral bean은 MissingBean(query) 조건으로 생성되지 않아 Context 성공 기대와 Bean graph가 모순된다.
- 영향: 고객 Override 구성에서 ApplicationContext가 실패할 가능성이 높고 FDEV-002 완료 주장이 깨진다.
- 대상: `AdmIntegrationClosureConfiguration.java; AdmIntegrationClosureConfigurationTest.java`
- 필수 수정: Query/Correction capability를 명시적으로 pair 검증하거나 고객 Owner Adapter override를 허용하고 실제 Gradle Context Test를 실행한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "*AdmIntegrationClosureConfigurationTest"`
- 성공 기준: disabled/default/customer override/duplicate/missing provider Context Test 모두 실제 PASS; Bean 목록 Evidence 제출.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 고객 Override 구성에서 ApplicationContext가 실패할 가능성이 높고 FDEV-002 완료 주장이 깨진다.

### QA-R5I-008 — API Contract Parity

- Requirement: `FDEV-014,FDEV-016`
- 확인: `DIRECT_SOURCE_AND_OPENAPI_CONFIRMED`
- 결함: Webhook replay expectedVersion은 OpenAPI/UI minimum 0이나 Service는 1 이상을 요구한다. reason/idempotencyKey의 OpenAPI max/min 제약은 Controller/Service에서 동일하게 강제되지 않는다.
- 영향: Generated Client가 유효하다고 판단한 요청이 Runtime에서 거부되거나, 계약상 거부해야 할 oversized 입력이 서버에서 수용된다.
- 대상: `enrich-adm-openapi-contract.mjs; AdmIntegrationClosureController.java; AdmIntegrationClosureService.java; IntegrationClosurePage.vue`
- 필수 수정: 단일 Validation DTO/Bean Validation에서 제약을 정의하고 runtime-generated OpenAPI와 generated client를 재생성한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기준: 0/1 경계, 7/8/128/129 idempotency, reason 500/501 contract test가 Controller+OpenAPI+client에서 동일 결과.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Generated Client가 유효하다고 판단한 요청이 Runtime에서 거부되거나, 계약상 거부해야 할 oversized 입력이 서버에서 수용된다.

### QA-R5I-009 — Sensitive Data Exposure

- Requirement: `FDEV-003,FDEV-013,FDEV-014`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: AdmApprovalService.detail은 COMMAND_PAYLOAD_SNAPSHOT을 포함한 raw request Map을 반환하고 AdmApprovalController detail/create/decision/execute/reconcile 및 ApprovalsPage StructuredData가 이를 그대로 노출한다.
- 영향: 정정 값, 계정/개인정보/Secret이 API와 브라우저 화면에 원문 노출될 수 있다.
- 대상: `AdmApprovalService.detail; AdmApprovalController; ApprovalsPage.vue`
- 필수 수정: 외부 응답 전용 DTO를 도입해 payloadSnapshot을 제거/마스킹하고 민감 필드 접근 권한·감사·negative snapshot test를 적용한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: API/UI/로그/Evidence에서 원문 corrected/secret/PII 0; 승인 엔진 내부 hash/target/audit reference만 노출.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 정정 값, 계정/개인정보/Secret이 API와 브라우저 화면에 원문 노출될 수 있다.

### QA-R5I-025 — Approval Capability Boundary

- Requirement: `FDEV-003,FDEV-007,FDEV-008,FDEV-013`
- 확인: `DIRECT_SOURCE_AND_EXTERNAL_COMPILE_REPRODUCTION_CONFIRMED`
- 결함: CpfDataQualityCorrectionPort가 public SPI에 public nested ApprovedCorrection record를 노출한다. 외부 package가 승인 엔진을 거치지 않고 ApprovedCorrection을 직접 생성해 correctApproved를 호출할 수 있어 “caller authorization API가 아니다”라는 주석이 타입 경계로 강제되지 않는다.
- 영향: Correction Port를 주입받거나 구현한 Consumer가 승인 Ledger·single-use reservation·snapshot hash 검증 없이 Owner mutation을 실행할 수 있다.
- 대상: `cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java; cpf-admin/.../DataQualityCorrectionApprovalOwnerCommandAdapter.java; consumers`
- 필수 수정: 승인 증명 객체의 생성자를 외부에 노출하지 말고 ADM 내부 sealed/package-private capability 또는 nonce 검증형 execution token으로 이동한다. Public SPI는 raw correction command를 받지 않도록 재설계하고 외부 compile-negative test를 추가한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: 외부 package에서 승인 capability 생성/Owner mutation 호출이 compile 또는 runtime authorization 단계에서 거부되고, 유일한 Consumer가 서버 Ledger reservation을 검증한 ADM adapter로 제한된다.
- 실패 기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: Correction Port를 주입받거나 구현한 Consumer가 승인 Ledger·single-use reservation·snapshot hash 검증 없이 Owner mutation을 실행할 수 있다.

### QA-R5I-026 — Frontend Idempotency

- Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- 확인: `LOCAL_REPRODUCTION_CONFIRMED`
- 결함: Integration Closure approval idempotency state가 sessionStorage 단일 key에 저장된다. Draft A pending 후 B를 열고 다시 A를 재시도하면 A의 원 key가 B 상태에 의해 대체되어 새 key가 생성되고 timeout/응답 유실 재시도의 동일 요청 정체성이 깨진다.
- 영향: 동일 correction 요청이 복수 Approval Request로 생성되거나 응답 유실 뒤 중복 side effect 및 운영자 혼란이 발생할 수 있다.
- 대상: `cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts; IntegrationClosurePage.vue; tests`
- 필수 수정: fingerprint별 multi-entry pending ledger를 사용하고 confirmed/expired 상태를 명시 관리한다. A→B→A, refresh, multi-tab, timeout, duplicate click corpus를 추가한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기준: 동일 fingerprint의 pending retry는 항상 동일 key, 다른 draft는 독립 key, 성공/명시 취소 후에만 회전하며 A→B→A 재현에서 original A key가 유지된다.
- 실패 기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 동일 correction 요청이 복수 Approval Request로 생성되거나 응답 유실 뒤 중복 side effect 및 운영자 혼란이 발생할 수 있다.

### QA-R5I-029 — Independent Review

- Requirement: `FDEV-024`
- 확인: `PROCESS_EVIDENCE_ABSENT`
- 결함: REV-004 개발 결과에 대한 Codex 독립 검수·보완 완료 Evidence가 없으며 개발GPT 자체검수 직후 QA로 진입했다.
- 영향: 개발자와 독립된 2차 Source/Runtime 검증 단계가 생략되어 동일 가정과 false-green이 QA 패키지까지 전파된다.
- 대상: `cpf-docs/work/v9i/fdr/r4/CODEX_REVIEW_REQUEST.md; Codex result/evidence; requirement ledger`
- 필수 수정: 개발 수정 후 Codex가 exact result SHA에서 29개 finding과 FDEV-001~025를 독립 검수하고 Codex 영역 원장·Evidence를 제출한다.
- 재실행: `git rev-parse HEAD; git status --short --branch`
- 성공 기준: Codex 검수 결과, 수정 내역, 명령/Exit/Evidence, remaining findings가 exact SHA에 결속되고 QA R6 진입 전에 미완료가 명시된다.
- 실패 기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 개발자와 독립된 2차 Source/Runtime 검증 단계가 생략되어 동일 가정과 false-green이 QA 패키지까지 전파된다.

## P1

### QA-R5I-010 — Approval JSON Integrity

- Requirement: `FDEV-003,FDEV-012,FDEV-013`
- 확인: `LOCAL_REPRODUCTION_CONFIRMED`
- 결함: Jackson/JSON.parse 기본 파서는 exact duplicate key를 마지막 값으로 덮어쓰고, JS Number는 2^53 초과 정수와 고정소수 정밀도를 손실한다. 현재 canonical hash는 strict duplicate/BigDecimal 설정이 없다.
- 영향: 승인 화면에 표시된 원문과 서버가 해석·hash·실행하는 값이 달라질 수 있다.
- 대상: `AdmApprovalSnapshotIntegrity.java; integrationClosureIdempotency.ts; IntegrationClosurePage.vue`
- 필수 수정: 서버 strict duplicate detection + BigInteger/BigDecimal parsing, 프론트 raw JSON strict parser 또는 typed form을 적용한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: duplicate key, Unicode collision, 64-bit integer, high-scale decimal corpus가 browser/server 동일 canonical hash로 PASS.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 승인 화면에 표시된 원문과 서버가 해석·hash·실행하는 값이 달라질 수 있다.

### QA-R5I-011 — Data Quality Null Handling

- Requirement: `FDEV-003,FDEV-009,FDEV-014`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: Map.copyOf를 record/corrected payload에 사용하여 null field가 포함된 데이터 품질 검증·정정 요청이 NPE로 실패한다.
- 영향: 누락/NULL 검증이 핵심인 Data Quality 기능이 정상적으로 입력을 검사하지 못한다.
- 대상: `AdmIntegrationClosureService; InMemoryCpfDataQualityOperations; CpfDataQualityCorrectionPort`
- 필수 수정: null을 허용하는 immutable copy 또는 명시 schema validation을 적용하고 null/empty/oversize 경계 테스트를 추가한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-common:test :cpf-admin:test --tests "*DataQuality*"`
- 성공 기준: null field는 규칙 위반 결과로 처리되고 500/NPE가 발생하지 않는다.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 누락/NULL 검증이 핵심인 Data Quality 기능이 정상적으로 입력을 검사하지 못한다.

### QA-R5I-012 — Data Quality Replay/Concurrency

- Requirement: `FDEV-003,FDEV-009,FDEV-012`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: replay는 expectedVersion 없이 동작하고 validate를 재호출하여 실패 시 새 quarantineId를 생성한다. 동시 correction/replay에서 stale update·중복 quarantine이 가능하다.
- 영향: 중복 Side Effect, 상태 분기, 대사 불일치와 무한 격리 증가가 발생할 수 있다.
- 대상: `CpfDataQualityOperations; InMemoryCpfDataQualityOperations; replay API/OpenAPI/UI`
- 필수 수정: Replay CAS/operation id를 추가하고 validation-only 경로와 quarantine creation을 분리하며 parent/replay lineage를 저장한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-common:test :cpf-admin:test --tests "*DataQuality*"`
- 성공 기준: stale replay 409, concurrent replay 1회, failed replay 신규 orphan 0, reconcile 수렴.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 중복 Side Effect, 상태 분기, 대사 불일치와 무한 격리 증가가 발생할 수 있다.

### QA-R5I-013 — Approval Policy Lifecycle

- Requirement: `FDEV-003,FDEV-013,FDEV-014`
- 확인: `PEER_EXACT_BLOB_CONFIRMED`
- 결함: Versioned 정책을 same policyCode/version으로 UPDATE하고 steps를 DELETE/INSERT한다. PolicyRequest.reason과 breakGlassAllowedYn은 실행/감사에 반영되지 않는다.
- 영향: 과거 승인 정책 재현성·감사성이 깨지고 정책 변경 충돌 및 break-glass 의미가 형식 필드에 그친다.
- 대상: `AdmApprovalService.savePolicy; AdmApprovalRepository.replacePolicy; ApprovalsPage.vue`
- 필수 수정: 사용 중/활성 정책 Version immutable, 새 Version 생성, optimistic lock, 변경 reason/audit, break-glass 정책 실제 enforcement를 구현한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: same version overwrite 거부, concurrent save 409, policy audit before/after hash, break-glass negative test PASS.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 과거 승인 정책 재현성·감사성이 깨지고 정책 변경 충돌 및 break-glass 의미가 형식 필드에 그친다.

### QA-R5I-014 — Approval Idempotency/DB Integration

- Requirement: `FDEV-003,FDEV-012`
- 확인: `SOURCE_PATTERN_CONFIRMED;DB3_RUNTIME_REQUIRED`
- 결함: requestKey/decision idempotency는 선조회 후 INSERT/UPDATE하며 실제 DB unique conflict 재조회/수렴 테스트가 없다. 변경된 테스트는 대부분 Mockito이며 3 Vendor transaction/CAS를 검증하지 않는다.
- 영향: 다중 인스턴스 동시 요청에서 duplicate key 예외가 500이 되거나 동일 결과 보장이 깨질 수 있다.
- 대상: `AdmApprovalService; AdmApprovalRepository; approval tests; DB vendor schema`
- 필수 수정: DB unique constraint 기반 insert-or-read와 duplicate exception replay를 구현하고 DB3 동시성 통합 테스트를 수행한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: 2+ process same key에서 mutation/request/decision 1건, 모든 호출 동일 result, 500 0.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 다중 인스턴스 동시 요청에서 duplicate key 예외가 500이 되거나 동일 결과 보장이 깨질 수 있다.

### QA-R5I-015 — DB3 Runner Safety

- Requirement: `FDEV-005,FDEV-020,FDEV-023`
- 확인: `DIRECT_SOURCE_CONFIRMED_AND_ENRICHED`
- 결함: DB runner는 JDBC URL/username을 argv로 전달하고 URL 내 credential/query secret을 금지·redact하지 않는다. WaitForExit에 timeout/cancellation이 없다. 또한 ProcessStartInfo.Environment를 정리하지 않아 부모 프로세스의 CPF_RUNTIME_*_PASSWORD 및 기타 Secret 환경변수가 child로 상속될 수 있다.
- 영향: process list·Evidence에 secret이 노출되거나 hung DB/runner가 QA/운영 작업을 무기한 점유한다.
- 대상: `run-db3-lifecycle.ps1; Pester tests; verify-db3-runner-contract.py`
- 필수 수정: URL credential validation/redaction, config/stdin/file descriptor 방식, vendor별 timeout·kill·timeout Evidence를 추가한다. child environment를 clear 후 명시 allowlist만 주입하고 Password/Token inheritance negative test를 추가한다.
- 재실행: `python cpf-tools/verification/final-dev/verify-db3-runner-contract.py; pwsh -NoProfile -Command "Invoke-Pester -Path 'cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1' -CI"`
- 성공 기준: secret corpus argv/log/audit 0, timeout 시 child tree 종료 및 명확한 exit code/UNKNOWN result. child environment secret 0, inherited secret canary 0.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: process list·Evidence에 secret이 노출되거나 hung DB/runner가 QA/운영 작업을 무기한 점유한다.

### QA-R5I-016 — ADM UX/Permission

- Requirement: `FDEV-014,FDEV-017`
- 확인: `PEER_SOURCE_REVIEW;BROWSER_RUNTIME_REQUIRED`
- 결함: Integration Closure 화면은 CRITICAL route이나 query/approval/execute/replay 버튼별 권한 상태를 반영하지 않고 audit link도 없다. Playwright/role/accessibility 검증도 미실행이다.
- 영향: 조회 권한만 있는 운영자에게 위험 버튼이 노출되고 운영자가 감사·복구 근거를 연결하기 어렵다.
- 대상: `routes.ts; IntegrationClosurePage.vue; approval page; Playwright suite`
- 필수 수정: operation permission model로 버튼 hide/disable/reason 제공, audit/operation link, 401/403/409 focus flow 및 browser matrix를 구현한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기준: role별 버튼 matrix, keyboard/focus/aria/viewport, error injection Playwright PASS.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 조회 권한만 있는 운영자에게 위험 버튼이 노출되고 운영자가 감사·복구 근거를 연결하기 어렵다.

### QA-R5I-017 — Frontend Build Reproducibility

- Requirement: `FDEV-016,FDEV-020`
- 확인: `PEER_SOURCE_REVIEW;CLEAN_INCREMENTAL_RUNTIME_REQUIRED`
- 결함: Gradle frontendBuild inputs에 openapi/, scripts/, orval.config.ts 등 generator 입력이 빠져 있어 변경 후 stale output을 up-to-date로 재사용할 수 있다. install marker도 Node/npm version을 입력으로 추적하지 않는다.
- 영향: Source/OpenAPI가 변경돼도 WAR/static artifact가 이전 generated client를 포함할 수 있다.
- 대상: `cpf-admin/build.gradle; frontend package/generator files`
- 필수 수정: 모든 generator/build input과 tool version을 Gradle inputs로 선언하고 clean/incremental 두 경로 artifact hash를 비교한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기준: OpenAPI/script/tool version 1 byte 변경 시 task 재실행; clean과 incremental artifact hash 동일.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Source/OpenAPI가 변경돼도 WAR/static artifact가 이전 generated client를 포함할 수 있다.

### QA-R5I-018 — OpenAPI Source of Truth

- Requirement: `FDEV-016,FDEV-021`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: enrich script의 ensureOperation은 Controller/Runtime에 없는 route도 정적 spec에 생성하고 security/error를 수동 삽입한다.
- 영향: OpenAPI가 실제 Runtime을 검증하지 않고 결함을 가리는 별도 정본이 된다.
- 대상: `enrich-adm-openapi-contract.mjs; OpenAPI lifecycle scripts`
- 필수 수정: runtime/controller-generated document를 입력으로 사용하고 missing route/operation이면 생성이 아니라 실패하도록 변경한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기준: Controller 제거/rename/security 변경 mutation test에서 OpenAPI Gate FAIL; runtime snapshot과 release spec exact parity.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: OpenAPI가 실제 Runtime을 검증하지 않고 결함을 가리는 별도 정본이 된다.

### QA-R5I-019 — QA Gate Coverage

- Requirement: `FDEV-007,FDEV-019,FDEV-020,FDEV-025`
- 확인: `PEER_SOURCE_REVIEW;MUTATION_TEST_REQUIRED`
- 결함: QA38 DB parity loop는 cpf-starters 1단계 directory만 검사하여 data/... 등 중첩 module SQL을 놓친다. AutoConfiguration target 일부는 SHA에 묶이지 않은 hard-coded allowlist로 존재 검사를 우회한다.
- 영향: Starter 물리 재편 이후 주요 DB/Bean 결함이 Gate PASS로 누락될 수 있다.
- 대상: `cpf-tools/verification/qa38/verify-qa38-structure.py`
- 필수 수정: canonical catalog modules를 순회해 DB/Source를 검사하고 allowlist를 제거하거나 exact path+hash catalog로 검증한다.
- 재실행: `python cpf-tools/verification/qa38/verify-qa38-structure.py .; python cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py`
- 성공 기준: 중첩 module SQL 누락/AutoConfiguration 삭제 mutation test가 반드시 FAIL.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Starter 물리 재편 이후 주요 DB/Bean 결함이 Gate PASS로 누락될 수 있다.

### QA-R5I-020 — Documentation Evidence Matrix

- Requirement: `FDEV-001,FDEV-021,FDEV-022`
- 확인: `PACKAGE_AND_PATH_REVIEW_CONFIRMED`
- 결함: CPF_SOURCE_EVIDENCE_MATRIX는 모두 a8be27 기준이며 integrationClosureApi/Test를 존재하지 않는 generated 경로로 기록하고 일부 OpenAPI 경로도 실제 Repository와 불일치한다.
- 영향: Guide/산출물에서 Source 추적 링크가 끊기고 최신 구현과 문서가 양방향 불일치한다.
- 대상: `cpf-docs/deliverables/evidence/CPF_SOURCE_EVIDENCE_MATRIX.csv`
- 필수 수정: e7cc9ada source tree로 path existence/hash를 재생성하고 미검증 항목을 완료로 표기하지 않는다.
- 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기준: matrix 경로 100% 존재, blob/hash 일치, runtime 미실행 status=미검증.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Guide/산출물에서 Source 추적 링크가 끊기고 최신 구현과 문서가 양방향 불일치한다.

### QA-R5I-027 — QA Gate False Green

- Requirement: `FDEV-003,FDEV-005,FDEV-014,FDEV-016,FDEV-025`
- 확인: `DIRECT_VERIFIER_SOURCE_REVIEW_CONFIRMED`
- 결함: REV-004/DB3 정적 Gate는 문자열 패턴 존재를 PASS로 판정하며 public capability 위조, 단일-slot idempotency, child environment secret inheritance, WaitForExit timeout 부재 같은 실제 결함을 검출하지 못한다.
- 영향: 검증 Script PASS가 상용 동작·보안 PASS로 오인되어 동일 유형의 회귀가 반복된다.
- 대상: `cpf-tools/verification/final-dev/verify-rev004-overlay.py; verify-db3-runner-contract.py; verify_db3_runner_protocol.py; tests`
- 필수 수정: negative compile/runtime/mutation test를 Gate에 연결하고 문자열 존재 검사를 행위 검증으로 보강한다. 미실행 Runtime은 PASS가 아니라 NOT_EXECUTED로 구조화한다.
- 재실행: `python cpf-tools/verification/final-dev/verify-rev004-overlay.py; python cpf-tools/verification/final-dev/verify-db3-runner-contract.py`
- 성공 기준: capability public 노출, single-slot storage, inherited secret, timeout 제거, runtime route 삭제 mutation이 각각 Gate FAIL을 발생시킨다.
- 실패 기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 검증 Script PASS가 상용 동작·보안 PASS로 오인되어 동일 유형의 회귀가 반복된다.

### QA-R5I-028 — Approval HTTP Contract

- Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- 확인: `DIRECT_CONTROLLER_SOURCE_CONFIRMED`
- 결함: AdmApprovalController의 create/detail/decision/reconcile/execute가 모두 ResponseEntity.ok를 반환한다. 기존 requestKey 재생, concurrent preemption/conflict, stale version 등 상태 충돌을 HTTP 409로 안정적으로 표현하는 계약이 부족하다.
- 영향: Generated Client와 운영 UI가 성공·멱등 replay·충돌·동시 변경을 구분하지 못하고 재시도/오류 처리를 잘못 수행할 수 있다.
- 대상: `AdmApprovalController.java; AdmApprovalService.java; exception handler; OpenAPI; generated client; frontend consumers`
- 필수 수정: created/replayed/conflict/stale 상태를 명시 DTO와 201/200/409 계약으로 정의하고 ControllerAdvice/OpenAPI/client/UI contract test를 추가한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기준: 신규 201, 동일 payload replay 200 same result, key collision/stale CAS 409, validation 400/422가 Backend/OpenAPI/generated client/UI에서 동일하다.
- 실패 기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: Generated Client와 운영 UI가 성공·멱등 replay·충돌·동시 변경을 구분하지 못하고 재시도/오류 처리를 잘못 수행할 수 있다.

## P2

### QA-R5I-021 — Public API Migration

- Requirement: `FDEV-003,FDEV-007,FDEV-008`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: CpfDataQualityOperations에 client boolean approved 시그니처가 Deprecated default method로 남아 있다.
- 영향: 금지된 사용법이 public surface와 자동완성에 계속 노출되고 Consumer 제거 여부를 판정하기 어렵다.
- 대상: `CpfDataQualityOperations.java; consumer scan`
- 필수 수정: 호환 기간/제거 버전/사용처 0 증거를 문서화하고 다음 major에서 제거하거나 별도 compatibility adapter로 격리한다.
- 재실행: `.\gradlew.bat --no-daemon :cpf-core:test :cpf-common:test :cpf-admin:test`
- 성공 기준: production consumer 0, deprecation migration test/document, major removal plan 존재.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 금지된 사용법이 public surface와 자동완성에 계속 노출되고 Consumer 제거 여부를 판정하기 어렵다.

### QA-R5I-022 — Build Graph Reproducibility

- Requirement: `FDEV-004,FDEV-020,FDEV-025`
- 확인: `DIRECT_SOURCE_CONFIRMED`
- 결함: settings.gradle이 local-domains 아래 임의 included build를 별도 opt-in 없이 자동 포함한다.
- 영향: 동일 Git SHA라도 로컬 directory 존재 여부에 따라 build graph와 dependency resolution이 달라진다.
- 대상: `settings.gradle`
- 필수 수정: 명시 property/allowlist와 manifest hash가 있을 때만 include하고 Evidence에 mounted domains를 기록한다.
- 재실행: `.\gradlew.bat --no-daemon projects; .\gradlew.bat --no-daemon clean assemble`
- 성공 기준: fresh clone/로컬 폴더 존재 환경의 기본 build graph 동일; opt-in 시 manifest 검증.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 동일 Git SHA라도 로컬 directory 존재 여부에 따라 build graph와 dependency resolution이 달라진다.

### QA-R5I-023 — Frontend Approval Key UX

- Requirement: `FDEV-003,FDEV-014`
- 확인: `PEER_SOURCE_REVIEW;UX_TEST_REQUIRED`
- 결함: ApprovalsPage의 requestKey/decision idempotencyKey는 초기 1회 생성 후 성공·새 요청 시 자동 회전되지 않아 다른 요청에 재사용되기 쉽다.
- 영향: 운영자가 정상 다음 작업에서 cross-request idempotency 오류를 반복하거나 수동 키를 잘못 수정한다.
- 대상: `ApprovalsPage.vue`
- 필수 수정: 성공 확정/새 작업 시 key rotation, pending retry 시만 동일 key 유지, 상태 표시와 테스트를 추가한다.
- 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기준: timeout retry same key, success/new draft new key, cross-request reuse UI 차단.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 운영자가 정상 다음 작업에서 cross-request idempotency 오류를 반복하거나 수동 키를 잘못 수정한다.

### QA-R5I-024 — Evidence Retention/Path

- Requirement: `FDEV-022,FDEV-024`
- 확인: `PACKAGE_PROVENANCE_CONFIRMED`
- 결함: R4 Package는 baseline overlay 관점의 117개 payload를 기록하지만 result_commit_sha와 commit 적용 후 상태가 없고 current master 기준 Package index가 없다.
- 영향: 사용자가 merge한 Commit과 제출 ZIP의 관계를 독립적으로 증명할 수 없다.
- 대상: `fdr/r4/PACKAGE_MANIFEST.json; PRODUCT_TREE_SHA256.txt; CHANGE_MANIFEST.csv`
- 필수 수정: result commit 기준 tree manifest와 changed-file hash를 재생성하고 overlay baseline manifest는 historical로 분리한다.
- 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기준: baseline overlay hash + result tree hash + commit SHA + file count가 모두 교차 검산됨.
- 실패 기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 사용자가 merge한 Commit과 제출 ZIP의 관계를 독립적으로 증명할 수 없다.

