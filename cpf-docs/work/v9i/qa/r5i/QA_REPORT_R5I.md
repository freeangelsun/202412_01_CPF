# CPF 통합 QA 전수검수 보고서 — REV-004 / QA R5I

## 1. Executive Verdict

| 항목 | 결과 |
|---|---|
| Repository / Branch | `freeangelsun/202412_01_CPF` / `master` |
| 최종 검수 기준 Commit | `e7cc9ada86c871214a20862779f2433bc46fea1b` (`06_10`) |
| 이전 Commit | `11093fe26b4e94d9066f2d9edcc1d06c879d868e` (`06_09`) |
| 문서 Delta Commit | `a8be27a34bdac0b7c075e06d6e86571244c96421` (`06_08`) |
| QA 회차 | `R5I` — 자체 QA와 동료 QA R5 통합 재검증 |
| 전체 QA 결과 | **미통과** |
| 개발GPT 재개발 요청 | **필요** |
| FDEV-001~FDEV-025 | **25건 전부 미통과** |
| 통합 결함 | **29건 — P0 12 / P1 13 / P2 4** |
| Java25/Gradle9.1 full build | **미실행 / PASS 불인정** |
| Oracle/PostgreSQL/MariaDB live lifecycle | **미실행 / PASS 불인정** |
| Playwright/Pester/Broker/Multi-process | **미실행 / PASS 불인정** |
| 출시·QA 통과 주장 | **불가** |

현재 master에는 개발 변경이 존재하지만, 보안 경계·승인 정책 결속·민감정보 노출·Bean wiring·OpenAPI/Frontend 계약·데이터 동시성·DB Runner·Evidence 무결성·Target Runtime 검증 결함이 남아 있다. 따라서 `development_status=완료`, `verification_status=완료`, CPF 최종 완료를 인정하지 않는다.

## 2. 통합 검토 방식

1. 자체 QA 16건과 동료 QA R5 24건을 finding 단위로 교차 대조했다.
2. 중복·부분 중복 11개 묶음을 병합하고 자체 QA에서만 확인된 5개 결함을 추가해 최종 29건으로 정리했다.
3. GitHub Connector로 최신 master와 06_08→06_09→06_10 Commit 계보를 재확인했다.
4. Exact-SHA로 내려받은 Source, OpenAPI, Frontend, Verification Script와 제출 Package/Evidence를 재검토했다.
5. 동료 QA ZIP의 압축 무결성, SHA-256 목록, Manifest 수량, 단일 보고서 동일성을 독립 검산했다.
6. 실행하지 못한 Java25·DB3·Browser·Broker·Multi-process 검증은 PASS로 전환하지 않았다.

## 3. 동료 QA Package 독립 검산

- ZIP 압축 검사: PASS
- `SHA256SUMS.txt` 검산: 전체 PASS
- Manifest `payloadFileCount=16`, files array 16: 일치
- ZIP 총 엔트리 18개: 16 payload + Manifest + SHA 목록으로 내부 정합
- 업로드된 단일 상세 보고서와 ZIP 내부 보고서: SHA-256 동일
- 결론: **동료 QA 산출물 컨테이너 무결성은 정상**이나, 그 내용이 판정한 제품 상태는 미통과다.

## 4. 추가 검증으로 확정한 핵심 결함

### 4.1 승인 Capability 타입 경계 우회
`CpfDataQualityCorrectionPort.ApprovedCorrection`은 public nested record이며 외부 코드가 생성할 수 있다. 자체 compile/run probe에서도 외부 package 생성이 성공했다. 서버 Ledger reservation을 거친 ADM 내부만 생성 가능한 타입 경계가 아니다.

### 4.2 A→B→A 교차 Draft 멱등성 실패
Integration Closure의 `sessionStorage`는 단일 slot이다. A pending 상태가 B로 덮인 뒤 A를 재시도하면 원 A key가 유지되지 않는 재현 결과가 남아 있다.

### 4.3 DB Runner의 Secret·Timeout 경계
Password stdin 전달 자체는 확인됐지만 URL/Username argv 전달, URL 내 credential 금지/마스킹 부재, child environment secret 상속 방지 부재, 무제한 `WaitForExit()`가 동시에 존재한다.

### 4.4 승인 정책과 실제 명령 결속 부족
명시 policyCode/version 경로는 활성·유효기간 검사 없이 정책을 선택하며 ownerModule/ownerCommand/targetType과의 서버 Registry 결속도 부족하다.

### 4.5 Approval 상세 Payload 원문 노출
`detail()`은 Repository 요청 row를 그대로 복사하고 participants/execution만 추가한다. row에 포함된 payloadSnapshot이 API·화면으로 전달될 수 있다.

### 4.6 정적 Gate False-Green
패턴 존재형 Gate가 위 Capability 우회, 단일-slot idempotency, 환경변수 상속, timeout 부재를 잡지 못했다. PASS 문자열은 Runtime/보안 PASS가 아니다.

## 5. Finding Summary

### P0 — 12건

| ID | 영역 | Requirement | 확인 수준 | 요약 |
|---|---|---|---|---|
| QA-R5I-001 | Management/Baseline | FDEV-001,FDEV-021,FDEV-022,FDEV-024 | PACKAGE_AND_REPOSITORY_CONFIRMED | 최신 master는 e7cc9ada(06_10)인데 REVIEW_INDEX, FINAL_MANAGEMENT_STATE, FINAL_INTEGRITY, fdr/r4 BASELINE/REQUIREMENT_STATUS/PACKAGE_MANIFEST가 a8be27/2929163/2a0136/cb3b2a를 현재 기준처럼 혼용한다. |
| QA-R5I-002 | Runtime/CI | FDEV-004,FDEV-005,FDEV-006,FDEV-017,FDEV-020,FDEV-024,FDEV-010,FDEV-011,FDEV-015,FDEV-018 | PEER_CONNECTOR_CONFIRMED_AND_LOCAL_ENVIRONMENT_BLOCKED | e7cc9ada에 연결된 GitHub status와 workflow run이 0건이며 Java25/Gradle9.1, full npm verify, Playwright, Pester, DB3, broker/multi-process가 실행되지 않았다. 추가 독립 환경 probe에서도 GitHub DNS 실패, JDK 21, Node 22.16.0, PowerShell 부재가 확인되어 Target Runtime  |
| QA-R5I-003 | Evidence Integrity | FDEV-001,FDEV-014,FDEV-016,FDEV-020,FDEV-022,FDEV-024 | PACKAGE_EVIDENCE_CONFIRMED | idempotency_runtime.txt는 0 byte인데 Ledger는 PASS이며, Requirement CSV는 .log를 참조하지만 저장 파일은 .txt이다. openapi_idempotent_1/2의 935f... 해시는 실제 OpenAPI 22d22...와 불일치한다. |
| QA-R5I-004 | Governance/Delete Safety | FDEV-020,FDEV-021,FDEV-024 | COMMIT_DIFF_CONFIRMED | 06_08 기준에 존재하던 cpf-document-quality-r9.svg가 최신 master에서 삭제되었고 Documentation Delete Manifest는 삭제를 지시하지만 사용자 승인 Evidence가 없다. |
| QA-R5I-005 | Approval Policy Binding | FDEV-003,FDEV-008,FDEV-013 | DIRECT_SOURCE_CONFIRMED | requestApproval은 client가 policyCode/version을 주면 enabled/effective 기간을 검증하지 않고, 해당 정책을 ownerModule/ownerCommand/targetType과 서버 Registry로 결속하지 않는다. |
| QA-R5I-006 | Secure Default/Profile | FDEV-002,FDEV-003,FDEV-013,FDEV-020 | PEER_EXACT_BLOB_CONFIRMED | application.yml이 spring.profiles.active=local을 기본값으로 고정하고 local profile은 integration-closure와 ephemeral providers를 기본 true로 한다. |
| QA-R5I-007 | Spring Bean Wiring | FDEV-002,FDEV-003 | DIRECT_SOURCE_GRAPH_CONTRADICTION;RUNTIME_REQUIRED | customerOverridesWinOverDefaultProviders Test는 CpfDataQualityOperations만 공급하지만 Configuration의 Owner Adapter는 별도 CpfDataQualityCorrectionPort를 필수 주입한다. ephemeral bean은 MissingBean(query) 조건으로 생성되지 않아 Context 성공 기대와 Bean graph가 모순된다 |
| QA-R5I-008 | API Contract Parity | FDEV-014,FDEV-016 | DIRECT_SOURCE_AND_OPENAPI_CONFIRMED | Webhook replay expectedVersion은 OpenAPI/UI minimum 0이나 Service는 1 이상을 요구한다. reason/idempotencyKey의 OpenAPI max/min 제약은 Controller/Service에서 동일하게 강제되지 않는다. |
| QA-R5I-009 | Sensitive Data Exposure | FDEV-003,FDEV-013,FDEV-014 | DIRECT_SOURCE_CONFIRMED | AdmApprovalService.detail은 COMMAND_PAYLOAD_SNAPSHOT을 포함한 raw request Map을 반환하고 AdmApprovalController detail/create/decision/execute/reconcile 및 ApprovalsPage StructuredData가 이를 그대로 노출한다. |
| QA-R5I-025 | Approval Capability Boundary | FDEV-003,FDEV-007,FDEV-008,FDEV-013 | DIRECT_SOURCE_AND_EXTERNAL_COMPILE_REPRODUCTION_CONFIRMED | CpfDataQualityCorrectionPort가 public SPI에 public nested ApprovedCorrection record를 노출한다. 외부 package가 승인 엔진을 거치지 않고 ApprovedCorrection을 직접 생성해 correctApproved를 호출할 수 있어 “caller authorization API가 아니다”라는 주석이 타입 경계로 강제되지 않는다. |
| QA-R5I-026 | Frontend Idempotency | FDEV-003,FDEV-012,FDEV-014,FDEV-016 | LOCAL_REPRODUCTION_CONFIRMED | Integration Closure approval idempotency state가 sessionStorage 단일 key에 저장된다. Draft A pending 후 B를 열고 다시 A를 재시도하면 A의 원 key가 B 상태에 의해 대체되어 새 key가 생성되고 timeout/응답 유실 재시도의 동일 요청 정체성이 깨진다. |
| QA-R5I-029 | Independent Review | FDEV-024 | PROCESS_EVIDENCE_ABSENT | REV-004 개발 결과에 대한 Codex 독립 검수·보완 완료 Evidence가 없으며 개발GPT 자체검수 직후 QA로 진입했다. |

### P1 — 13건

| ID | 영역 | Requirement | 확인 수준 | 요약 |
|---|---|---|---|---|
| QA-R5I-010 | Approval JSON Integrity | FDEV-003,FDEV-012,FDEV-013 | LOCAL_REPRODUCTION_CONFIRMED | Jackson/JSON.parse 기본 파서는 exact duplicate key를 마지막 값으로 덮어쓰고, JS Number는 2^53 초과 정수와 고정소수 정밀도를 손실한다. 현재 canonical hash는 strict duplicate/BigDecimal 설정이 없다. |
| QA-R5I-011 | Data Quality Null Handling | FDEV-003,FDEV-009,FDEV-014 | DIRECT_SOURCE_CONFIRMED | Map.copyOf를 record/corrected payload에 사용하여 null field가 포함된 데이터 품질 검증·정정 요청이 NPE로 실패한다. |
| QA-R5I-012 | Data Quality Replay/Concurrency | FDEV-003,FDEV-009,FDEV-012 | DIRECT_SOURCE_CONFIRMED | replay는 expectedVersion 없이 동작하고 validate를 재호출하여 실패 시 새 quarantineId를 생성한다. 동시 correction/replay에서 stale update·중복 quarantine이 가능하다. |
| QA-R5I-013 | Approval Policy Lifecycle | FDEV-003,FDEV-013,FDEV-014 | PEER_EXACT_BLOB_CONFIRMED | Versioned 정책을 same policyCode/version으로 UPDATE하고 steps를 DELETE/INSERT한다. PolicyRequest.reason과 breakGlassAllowedYn은 실행/감사에 반영되지 않는다. |
| QA-R5I-014 | Approval Idempotency/DB Integration | FDEV-003,FDEV-012 | SOURCE_PATTERN_CONFIRMED;DB3_RUNTIME_REQUIRED | requestKey/decision idempotency는 선조회 후 INSERT/UPDATE하며 실제 DB unique conflict 재조회/수렴 테스트가 없다. 변경된 테스트는 대부분 Mockito이며 3 Vendor transaction/CAS를 검증하지 않는다. |
| QA-R5I-015 | DB3 Runner Safety | FDEV-005,FDEV-020,FDEV-023 | DIRECT_SOURCE_CONFIRMED_AND_ENRICHED | DB runner는 JDBC URL/username을 argv로 전달하고 URL 내 credential/query secret을 금지·redact하지 않는다. WaitForExit에 timeout/cancellation이 없다. 또한 ProcessStartInfo.Environment를 정리하지 않아 부모 프로세스의 CPF_RUNTIME_*_PASSWORD 및 기타 Secret 환경변수가 child로 상속될  |
| QA-R5I-016 | ADM UX/Permission | FDEV-014,FDEV-017 | PEER_SOURCE_REVIEW;BROWSER_RUNTIME_REQUIRED | Integration Closure 화면은 CRITICAL route이나 query/approval/execute/replay 버튼별 권한 상태를 반영하지 않고 audit link도 없다. Playwright/role/accessibility 검증도 미실행이다. |
| QA-R5I-017 | Frontend Build Reproducibility | FDEV-016,FDEV-020 | PEER_SOURCE_REVIEW;CLEAN_INCREMENTAL_RUNTIME_REQUIRED | Gradle frontendBuild inputs에 openapi/, scripts/, orval.config.ts 등 generator 입력이 빠져 있어 변경 후 stale output을 up-to-date로 재사용할 수 있다. install marker도 Node/npm version을 입력으로 추적하지 않는다. |
| QA-R5I-018 | OpenAPI Source of Truth | FDEV-016,FDEV-021 | DIRECT_SOURCE_CONFIRMED | enrich script의 ensureOperation은 Controller/Runtime에 없는 route도 정적 spec에 생성하고 security/error를 수동 삽입한다. |
| QA-R5I-019 | QA Gate Coverage | FDEV-007,FDEV-019,FDEV-020,FDEV-025 | PEER_SOURCE_REVIEW;MUTATION_TEST_REQUIRED | QA38 DB parity loop는 cpf-starters 1단계 directory만 검사하여 data/... 등 중첩 module SQL을 놓친다. AutoConfiguration target 일부는 SHA에 묶이지 않은 hard-coded allowlist로 존재 검사를 우회한다. |
| QA-R5I-020 | Documentation Evidence Matrix | FDEV-001,FDEV-021,FDEV-022 | PACKAGE_AND_PATH_REVIEW_CONFIRMED | CPF_SOURCE_EVIDENCE_MATRIX는 모두 a8be27 기준이며 integrationClosureApi/Test를 존재하지 않는 generated 경로로 기록하고 일부 OpenAPI 경로도 실제 Repository와 불일치한다. |
| QA-R5I-027 | QA Gate False Green | FDEV-003,FDEV-005,FDEV-014,FDEV-016,FDEV-025 | DIRECT_VERIFIER_SOURCE_REVIEW_CONFIRMED | REV-004/DB3 정적 Gate는 문자열 패턴 존재를 PASS로 판정하며 public capability 위조, 단일-slot idempotency, child environment secret inheritance, WaitForExit timeout 부재 같은 실제 결함을 검출하지 못한다. |
| QA-R5I-028 | Approval HTTP Contract | FDEV-003,FDEV-012,FDEV-014,FDEV-016 | DIRECT_CONTROLLER_SOURCE_CONFIRMED | AdmApprovalController의 create/detail/decision/reconcile/execute가 모두 ResponseEntity.ok를 반환한다. 기존 requestKey 재생, concurrent preemption/conflict, stale version 등 상태 충돌을 HTTP 409로 안정적으로 표현하는 계약이 부족하다. |

### P2 — 4건

| ID | 영역 | Requirement | 확인 수준 | 요약 |
|---|---|---|---|---|
| QA-R5I-021 | Public API Migration | FDEV-003,FDEV-007,FDEV-008 | DIRECT_SOURCE_CONFIRMED | CpfDataQualityOperations에 client boolean approved 시그니처가 Deprecated default method로 남아 있다. |
| QA-R5I-022 | Build Graph Reproducibility | FDEV-004,FDEV-020,FDEV-025 | DIRECT_SOURCE_CONFIRMED | settings.gradle이 local-domains 아래 임의 included build를 별도 opt-in 없이 자동 포함한다. |
| QA-R5I-023 | Frontend Approval Key UX | FDEV-003,FDEV-014 | PEER_SOURCE_REVIEW;UX_TEST_REQUIRED | ApprovalsPage의 requestKey/decision idempotencyKey는 초기 1회 생성 후 성공·새 요청 시 자동 회전되지 않아 다른 요청에 재사용되기 쉽다. |
| QA-R5I-024 | Evidence Retention/Path | FDEV-022,FDEV-024 | PACKAGE_PROVENANCE_CONFIRMED | R4 Package는 baseline overlay 관점의 117개 payload를 기록하지만 result_commit_sha와 commit 적용 후 상태가 없고 current master 기준 Package index가 없다. |

## 6. Requirement 판정

모든 Requirement는 `통과` 조건을 충족하지 못했다. Source 결함이 확인된 Requirement는 `실패`, 필수 Target Runtime Evidence가 없는 Requirement는 `미검증`을 포함한 `미통과`다.

| Requirement | 우선순위 | QA 결과 | 개발 상태 | 검증 상태 | Finding |
|---|---:|---|---|---|---|
| FDEV-001 | P0 | **미통과** | 부분 구현 | 실패 | QA-R5I-001;QA-R5I-003;QA-R5I-020 |
| FDEV-002 | P0 | **미통과** | 부분 구현 | 실패 | QA-R5I-006;QA-R5I-007 |
| FDEV-003 | P0 | **미통과** | 부분 구현 | 실패 | QA-R5I-005;QA-R5I-006;QA-R5I-007;QA-R5I-009;QA-R5I-010;QA-R5I-011;QA-R5I-012;QA-R5I-013;QA-R5I-014;QA-R5I-021;QA-R5I-023;QA-R5I-025;QA-R5I-026;QA-R5I-027;QA-R5I-028 |
| FDEV-004 | P0 | **미통과** | 미검증 | 미검증 | QA-R5I-002;QA-R5I-022 |
| FDEV-005 | P0 | **미통과** | 부분 구현 | 실패 | QA-R5I-002;QA-R5I-015;QA-R5I-027 |
| FDEV-006 | P0 | **미통과** | 미검증 | 미검증 | QA-R5I-002 |
| FDEV-007 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-019;QA-R5I-021;QA-R5I-025 |
| FDEV-008 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-005;QA-R5I-021;QA-R5I-025 |
| FDEV-009 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-011;QA-R5I-012 |
| FDEV-010 | P1 | **미통과** | 재확인 필요 | 미검증 | QA-R5I-002 |
| FDEV-011 | P1 | **미통과** | 재확인 필요 | 미검증 | QA-R5I-002 |
| FDEV-012 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-010;QA-R5I-012;QA-R5I-014;QA-R5I-026;QA-R5I-028 |
| FDEV-013 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-005;QA-R5I-006;QA-R5I-009;QA-R5I-010;QA-R5I-013;QA-R5I-025 |
| FDEV-014 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-003;QA-R5I-008;QA-R5I-009;QA-R5I-011;QA-R5I-013;QA-R5I-016;QA-R5I-023;QA-R5I-026;QA-R5I-027;QA-R5I-028 |
| FDEV-015 | P1 | **미통과** | 재확인 필요 | 미검증 | QA-R5I-002 |
| FDEV-016 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-003;QA-R5I-008;QA-R5I-017;QA-R5I-018;QA-R5I-026;QA-R5I-027;QA-R5I-028 |
| FDEV-017 | P1 | **미통과** | 미검증 | 미검증 | QA-R5I-002;QA-R5I-016 |
| FDEV-018 | P1 | **미통과** | 재확인 필요 | 미검증 | QA-R5I-002 |
| FDEV-019 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-019 |
| FDEV-020 | P1 | **미통과** | 부분 구현 | 실패 | QA-R5I-002;QA-R5I-003;QA-R5I-004;QA-R5I-006;QA-R5I-015;QA-R5I-017;QA-R5I-019;QA-R5I-022 |
| FDEV-021 | P2 | **미통과** | 부분 구현 | 실패 | QA-R5I-001;QA-R5I-004;QA-R5I-018;QA-R5I-020 |
| FDEV-022 | P2 | **미통과** | 부분 구현 | 실패 | QA-R5I-001;QA-R5I-003;QA-R5I-020;QA-R5I-024 |
| FDEV-023 | P2 | **미통과** | 부분 구현 | 실패 | QA-R5I-015 |
| FDEV-024 | P2 | **미통과** | 부분 구현 | 실패 | QA-R5I-001;QA-R5I-002;QA-R5I-003;QA-R5I-004;QA-R5I-024;QA-R5I-029 |
| FDEV-025 | P0 | **미통과** | 부분 구현 | 실패 | QA-R5I-019;QA-R5I-022;QA-R5I-027 |

## 7. Target Runtime 검증 상태

| 검증 | 상태 | QA 판정 |
|---|---|---|
| Java 25 / Gradle 9.1 clean build/test/publication | 미실행 | 미통과 |
| Spring ApplicationContext profile/provider matrix | 미실행 | 미통과 |
| Oracle/PostgreSQL/MariaDB install/upgrade/rollback/drift | 미실행 | 미통과 |
| Node full verify / generated client / incremental reproducibility | 미실행 | 미통과 |
| Playwright role/accessibility/responsive/error matrix | 미실행 | 미통과 |
| Pester DB runner timeout/secret/process-tree | PowerShell 부재 | 미통과 |
| Broker/multi-instance/separate WAS/process-kill/UNKNOWN/reconcile | 미실행 | 미통과 |
| GitHub exact-SHA CI/status | 제출 Evidence 없음 | 미통과 |

## 8. 재개발 우선순위

1. **P0 보안·승인 경계**: policy/action/owner binding, capability 생성 차단, payload masking, secure profile, Bean graph.
2. **멱등·계약·동시성**: A→B→A key ledger, HTTP 409, strict JSON/number, Data Quality replay CAS/lineage, DB unique convergence.
3. **DB Runner·Gate**: child env allowlist, argv/URL secret 차단, timeout/process-tree kill, behavioral mutation tests.
4. **정본·Evidence**: result SHA 기반 원장/Matrix/Manifest/Evidence 재생성, 빈 파일/경로/hash 불일치 0, 삭제 승인 복구.
5. **Target Runtime**: clean checkout Java25/Gradle/Node/DB3/Browser/Broker/Multi-process 전수 실행.
6. **독립 검수**: 개발GPT 자체검수 후 Codex 독립 검수·보완을 완료하고 QA R6로 재진입.

## 9. 재검수 진입 조건

- P0 12건 모두 수정 및 negative test 추가
- P1/P2 전건 수정 또는 QA가 수용한 명시적 잔여 위험 결정
- FDEV-001~025 same-ID 원장 갱신
- exact result SHA, clean working tree, command/tool/time/exit/hash가 포함된 Evidence
- Oracle/PostgreSQL/MariaDB, Browser, Broker, multi-process 필수 Runtime 실제 실행
- Codex 독립 검수 결과 및 보완 Evidence
- 승인 없는 보호 파일 삭제 0
- Package Manifest/SHA/Tree와 result commit 교차 검산 100%

## 10. 최종 결론

**QA R5I 최종 판정은 미통과다.** 자체 QA와 동료 QA 결과는 핵심 결함 방향에서 일치했고, 추가 검증에서 승인 Capability 위조 가능성, 교차 Draft 멱등키 유실, 정적 Gate false-green, HTTP 충돌 계약 부족, Codex 독립 검수 누락이 추가 Release Blocker로 유지됐다. 개발GPT가 같은 Requirement ID로 재개발하고 Codex 독립 검수 후 QA R6 전수검수를 수행해야 한다.

제품 Source 수정, Commit, Push, Branch, Reset, Restore, Stash, Clean, 실제 삭제는 수행하지 않았다.
