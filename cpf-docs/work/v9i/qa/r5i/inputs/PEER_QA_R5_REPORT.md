# CPF 통합 QA 전수검수 보고서 — REV-004 / QA R5

## 1. Executive Verdict

| 항목 | 결과 |
|---|---|
| Repository / Branch | `freeangelsun/202412_01_CPF` / `master` |
| 최신 확인 Commit | `e7cc9ada86c871214a20862779f2433bc46fea1b` (`06_10`) |
| instruction basis | `ee977cf66c251081df78ea5e9675b81c3dfafa59` (`06_07`) |
| documentation delta | `a8be27a34bdac0b7c075e06d6e86571244c96421` (`06_08`) |
| QA 회차 | R5 |
| 전체 QA 결과 | **미통과** |
| QA 통과 주장 | **아님** |
| P0 / P1 / P2 | **9 / 11 / 4** |
| Commit 연결 CI/Workflow | 없음 |
| Java25/Gradle9.1 full build | 미실행 |
| DB3 live lifecycle | 미실행 |
| Broker/Multi-process/Process Kill | 미실행 |
| Playwright Browser Matrix | 미실행 |
| QA 진입/출시 가능 여부 | 재개발 및 exact-SHA 재검수 전 **불가** |

개발GPT가 제출한 5개 재개발 행은 Source가 실제 master에 반영됐지만, 관리 정본과 Evidence는 여전히 적용 전 baseline을 가리키며, 보안·계약·Bean wiring·데이터 품질·검증 Gate에 Release 차단 결함이 남아 있다. 따라서 `development_status=완료`, `verification_status=완료` 또는 CPF 최종 완료를 인정하지 않는다.

## 2. 검수 방법과 범위

1. GitHub Connector로 최신 master, commit sequence, commit diff, changed files와 exact blob을 확인했다.
2. REV-004 직접 대상 `FDEV-003/005/014/016/025`의 Controller, Service, Repository, SPI, Configuration, Frontend, OpenAPI generator, Build, QA Gate, Tests, Evidence를 소스 단위로 검토했다.
3. 상위 정본 `CPF_FINAL_TARGET_REQUIREMENTS.md`, v9i 관리 원장/Integrity/Review/Evidence Matrix를 교차검증했다.
4. GitHub commit status와 workflow run을 조회했다. 결과는 0건이다.
5. clean clone과 Runtime 실행을 시도했으나 실행 환경 DNS가 `github.com`을 해석하지 못해 clone은 exit 128로 실패했다. 이 실패를 PASS로 간주하지 않았다.
6. 로컬 Node probe로 duplicate JSON key와 JavaScript safe-integer 손실을 재현했다.

### 검수 한계

현재 QA 실행 환경에는 Repository checkout, Java25, PowerShell, DB3, Broker, Browser가 없었다. 따라서 해당 Runtime은 `미검증`으로 유지한다. 다만 GitHub exact blob에서 확인된 정적 모순과 계약 위반은 실제 결함으로 판정했다.

## 3. Release Blocker Summary

### 3.1 관리 정본과 Evidence가 최신 master를 나타내지 않음

`REVIEW_INDEX`, `FINAL_MANAGEMENT_STATE`, `FINAL_INTEGRITY`, R4 baseline/status/manifest가 `cb3b2a`, `2a0136`, `2929163`, `a8be27`을 혼용한다. R4 상태는 `OVERLAY_NOT_APPLIED`인데 실제 Source는 `e7cc9ada86c871214a20862779f2433bc46fea1b` master에 반영됐다. 최신 Source와 Evidence 기준이 분리되지 않아 FDEV-001/021/022/024는 실패다.

### 3.2 Evidence 자체 모순

- `idempotency_runtime.txt`: 0 byte
- Requirement/Evidence 참조: `.log`; 실제 파일: `.txt`
- OpenAPI idempotent evidence: `935f...`; 실제 OpenAPI/manifest: `22d22...`
- 여러 Evidence는 command/time/environment/exit/source SHA 없는 한 줄 PASS

이는 단순 문서 오타가 아니라 QA 증거 무결성 실패다.

### 3.3 승인 엔진의 서버 정책 결속 부족

Client가 explicit policy code/version을 선택할 때 enabled/effective window 검증이 없고, 정책과 ownerModule/ownerCommand/targetType의 서버 결속 Registry가 없다. 저위험·구버전 정책을 다른 Owner Command에 적용할 여지가 있다.

### 3.4 승인 상세의 민감 Payload 노출

일반 Approval API는 raw `payloadSnapshot`을 반환하고 Approval 화면은 StructuredData로 표시한다. Integration Closure 전용 sanitize만으로 일반 승인 API 노출을 막지 못한다.

### 3.5 운영 Fail-closed 기본값 위반

`application.yml` 기본 active profile이 `local`이고 local profile은 ephemeral provider를 기본 활성화한다. Profile 지정 누락 시 개발용 in-memory 운영 기능이 켜질 수 있다.

### 3.6 Bean Wiring Test와 실제 Configuration 모순

고객 query provider만 Override하는 Test는 성공을 기대하지만 Owner Adapter는 correction port를 별도로 요구한다. Full Gradle Test가 실행되지 않아 이 모순이 제출물에 남았다.

### 3.7 OpenAPI/Runtime/UI Parity 실패

Webhook replay expectedVersion은 Spec/UI `0 이상`, Service `1 이상`이다. reason/idempotency 길이도 Spec과 서버 검증이 다르다. 더구나 enrich script는 Runtime route가 없어도 `ensureOperation`으로 새 operation을 만들 수 있어 OpenAPI가 검증 Gate가 아니라 제2 정본이 된다.

### 3.8 데이터 품질 기능 경계 결함

- `Map.copyOf`로 null field를 처리하지 못함
- replay에 expectedVersion/CAS 없음
- failed replay가 `validate()`를 통해 신규 quarantine을 생성하여 중복/lineage 단절 가능
- Browser JSON parse에서 duplicate key와 64-bit 숫자 손실

## 4. Findings

상세 24건은 `QA_FINDINGS.csv`에 기록했다.

| ID | 심각도 | 요약 |
|---|---|---|
| QA-R5-001 | P0 | 최신 master는 e7cc9ada(06_10)인데 REVIEW_INDEX, FINAL_MANAGEMENT_STATE, FINAL_INTEGRITY, fdr/r4 BASELINE/REQUIREMENT_STATUS/PACKAGE_MANIFEST가 a8be27/2929163/2a0136/cb3b2a를 현재 기준처럼 혼용한다. |
| QA-R5-002 | P0 | e7cc9ada에 연결된 GitHub status와 workflow run이 0건이며 Java25/Gradle9.1, full npm verify, Playwright, Pester, DB3, broker/multi-process가 실행되지 않았다. |
| QA-R5-003 | P0 | idempotency_runtime.txt는 0 byte인데 Ledger는 PASS이며, Requirement CSV는 .log를 참조하지만 저장 파일은 .txt이다. openapi_idempotent_1/2의 935f... 해시는 실제 OpenAPI 22d22...와 불일치한다. |
| QA-R5-004 | P0 | 06_08 기준에 존재하던 cpf-document-quality-r9.svg가 최신 master에서 삭제되었고 Documentation Delete Manifest는 삭제를 지시하지만 사용자 승인 Evidence가 없다. |
| QA-R5-005 | P0 | requestApproval은 client가 policyCode/version을 주면 enabled/effective 기간을 검증하지 않고, 해당 정책을 ownerModule/ownerCommand/targetType과 서버 Registry로 결속하지 않는다. |
| QA-R5-006 | P0 | application.yml이 spring.profiles.active=local을 기본값으로 고정하고 local profile은 integration-closure와 ephemeral providers를 기본 true로 한다. |
| QA-R5-007 | P0 | customerOverridesWinOverDefaultProviders Test는 CpfDataQualityOperations만 공급하지만 Configuration의 Owner Adapter는 별도 CpfDataQualityCorrectionPort를 필수 주입한다. ephemeral bean은 MissingBean(query) 조건으로 생성되지 않아 Context 성공 기대와 Bean graph가 모순된다. |
| QA-R5-008 | P0 | Webhook replay expectedVersion은 OpenAPI/UI minimum 0이나 Service는 1 이상을 요구한다. reason/idempotencyKey의 OpenAPI max/min 제약은 Controller/Service에서 동일하게 강제되지 않는다. |
| QA-R5-009 | P0 | AdmApprovalService.detail은 COMMAND_PAYLOAD_SNAPSHOT을 포함한 raw request Map을 반환하고 AdmApprovalController detail/create/decision/execute/reconcile 및 ApprovalsPage StructuredData가 이를 그대로 노출한다. |
| QA-R5-010 | P1 | Jackson/JSON.parse 기본 파서는 exact duplicate key를 마지막 값으로 덮어쓰고, JS Number는 2^53 초과 정수와 고정소수 정밀도를 손실한다. 현재 canonical hash는 strict duplicate/BigDecimal 설정이 없다. |
| QA-R5-011 | P1 | Map.copyOf를 record/corrected payload에 사용하여 null field가 포함된 데이터 품질 검증·정정 요청이 NPE로 실패한다. |
| QA-R5-012 | P1 | replay는 expectedVersion 없이 동작하고 validate를 재호출하여 실패 시 새 quarantineId를 생성한다. 동시 correction/replay에서 stale update·중복 quarantine이 가능하다. |
| QA-R5-013 | P1 | Versioned 정책을 same policyCode/version으로 UPDATE하고 steps를 DELETE/INSERT한다. PolicyRequest.reason과 breakGlassAllowedYn은 실행/감사에 반영되지 않는다. |
| QA-R5-014 | P1 | requestKey/decision idempotency는 선조회 후 INSERT/UPDATE하며 실제 DB unique conflict 재조회/수렴 테스트가 없다. 변경된 테스트는 대부분 Mockito이며 3 Vendor transaction/CAS를 검증하지 않는다. |
| QA-R5-015 | P1 | DB runner는 JDBC URL/username을 argv로 전달하고 URL 내 credential/query secret을 금지·redact하지 않는다. WaitForExit에 timeout/cancellation이 없다. |
| QA-R5-016 | P1 | Integration Closure 화면은 CRITICAL route이나 query/approval/execute/replay 버튼별 권한 상태를 반영하지 않고 audit link도 없다. Playwright/role/accessibility 검증도 미실행이다. |
| QA-R5-017 | P1 | Gradle frontendBuild inputs에 openapi/, scripts/, orval.config.ts 등 generator 입력이 빠져 있어 변경 후 stale output을 up-to-date로 재사용할 수 있다. install marker도 Node/npm version을 입력으로 추적하지 않는다. |
| QA-R5-018 | P1 | enrich script의 ensureOperation은 Controller/Runtime에 없는 route도 정적 spec에 생성하고 security/error를 수동 삽입한다. |
| QA-R5-019 | P1 | QA38 DB parity loop는 cpf-starters 1단계 directory만 검사하여 data/... 등 중첩 module SQL을 놓친다. AutoConfiguration target 일부는 SHA에 묶이지 않은 hard-coded allowlist로 존재 검사를 우회한다. |
| QA-R5-020 | P1 | CPF_SOURCE_EVIDENCE_MATRIX는 모두 a8be27 기준이며 integrationClosureApi/Test를 존재하지 않는 generated 경로로 기록하고 일부 OpenAPI 경로도 실제 Repository와 불일치한다. |
| QA-R5-021 | P2 | CpfDataQualityOperations에 client boolean approved 시그니처가 Deprecated default method로 남아 있다. |
| QA-R5-022 | P2 | settings.gradle이 local-domains 아래 임의 included build를 별도 opt-in 없이 자동 포함한다. |
| QA-R5-023 | P2 | ApprovalsPage의 requestKey/decision idempotencyKey는 초기 1회 생성 후 성공·새 요청 시 자동 회전되지 않아 다른 요청에 재사용되기 쉽다. |
| QA-R5-024 | P2 | R4 Package는 baseline overlay 관점의 117개 payload를 기록하지만 result_commit_sha와 commit 적용 후 상태가 없고 current master 기준 Package index가 없다. |

## 5. Requirement 판정

- `FDEV-001~FDEV-025`: **전부 미통과**
- Runtime 전용 `FDEV-004/006/017`: Source 결함 여부와 별개로 exact-SHA 실행 Evidence가 없어 `미검증`
- 직접 재개발 `FDEV-003/005/014/016/025`: 실제 결함 또는 검증 Gate 결함이 확인되어 재개발/재검수 요청
- 세부 상태: `QA_REQUIREMENT_STATUS.csv`

## 6. 재개발 우선순위

1. 승인 정책 결속, payload masking, secure default, Bean wiring
2. OpenAPI exact parity와 strict JSON/number handling
3. Data Quality replay/CAS/null handling
4. Evidence/원장/Manifest 최신 SHA 재생성 및 무승인 삭제 복구
5. DB runner timeout/secret 안전, QA38 nested module coverage
6. clean checkout Java25/Gradle/Node/DB3/Broker/Browser 전수 실행

## 7. 통합 원장 갱신

이 QA Overlay는 다음을 제공한다.

- `cpf-docs/work/v9i/qa/r5/QA_REPORT_REV004_R5.md`
- `QA_FINDINGS.csv`
- `QA_REQUIREMENT_STATUS.csv`
- `QA_REWORK_REQUEST.md`
- `QA_TEST_EXECUTION_LEDGER.csv`
- `QA_EVIDENCE_INDEX.csv`
- `QA_DELETE_MANIFEST.csv`
- `cpf-docs/work/v9i/results/REVIEW_FINDINGS.csv` append 갱신
- `cpf-docs/work/v9i/REVIEW_INDEX.md` QA R5 section 갱신
- `cpf-docs/work/v9i/fdr/r4/QA_REWORK_REQUEST.md` 최신 QA 요청으로 갱신

## 8. 최종 결론

현재 master는 개발GPT 변경이 반영된 상태지만 상용 Framework GA 품질과 최종 QA 통과 상태가 아니다. P0 결함을 먼저 수정하고 같은 Requirement ID로 재개발 제출한 뒤, clean checkout Target Runtime과 전체 FDEV-001~025 R6 재검수가 필요하다.
