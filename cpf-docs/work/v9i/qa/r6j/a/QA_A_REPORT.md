# CPF QA A R6J — Full Independent Source/Consumer/Runtime Review

## 0. Final verdict
**미통과 / RELEASE_BLOCKED**

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Current exact SHA: `3ed676061246c9db3e44f29e254c0393ecca3929` (`07_02`)
- R6J instruction-known SHA: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- Developer baseline: `64049044956924032360fa80be83b5e37c64f828`
- R6I regression findings reviewed: **40/40**
- Regression current disposition:
  - Source resolved, runtime/evidence revalidation required: **21**
  - FAIL: **14**
  - PARTIAL: **5**
- New R6J findings: **10** (P0 9 / P1 1)
- Developer ledger: 77 rows / dev-complete 77 / verified 26 / unverified 51
- EDU: 135 total / 135 runtime-unverified / 8 consumer types / EDU-ADM 17
- Product Source changes by QA: **0**
- Git writes/deletes/history changes by QA: **0**

## 1. Why this release is blocked
1. Exact-SHA execution Evidence is not bound: all 77 developer result SHA fields are pending and 14 referenced local logs are absent.
2. Required release workflow has a deterministic ADM frontend environment-name mismatch.
3. 51 developer requirements remain runtime-unverified.
4. Current 07_02 canonical transaction/logging requirements were added after the 77-row developer ledger.
5. ADM transactionId one-shot view is structurally partial, not the required cross-source timeline/tree.
6. Approval owner-side-effect + DB outage still lacks an independently durable UNKNOWN channel.
7. EDU-ADM has 17/17 role drift and product/education architecture remains wrong for many examples.
8. EDU runtime trusts caller-provided actor/roles/scope and PROCESS consumer has environment/temp-payload exposure.
9. Frontend verification and Observability verification both have independently reproduced false-green survivors.
10. Runtime OpenAPI/frontend omit 422 while backend validation actually returns 422.

## 2. Independently executed checks
- Frontend contract current-source projection: **PASS** — `[CPF][R6I][FRONTEND][PASS] admRoutes=63 routeBindings=414 uniqueOps=329 approvalGeneratedConsumers=8 workbenches=GET_ONLY`
- Frontend permission semantic mutation: **SURVIVED / FAIL** — verifier still returned PASS
- EDU consumer wiring current-source projection: **PASS** — `[CPF][R6I][EDU-CONSUMER][PASS] features=135 types=8 mutations=8`
- Observability fake proof: **SURVIVED / FAIL** — qualifier returned PASS
- Supply-chain built-in self-test: **PASS**
- Artifact consumer built-in self-test: **PASS**
- DB3 V105 source semantic parity: **3/3 vendors PASS**
- DB3 V106 source semantic parity: **3/3 vendors PASS**
- Live DB3 / Java25 / Browser / Multi-process / DR / performance / security corpus: **UNVERIFIED**

## 3. R6I 40-finding regression — current SHA

### AB-R6-001 — P0 — FAIL
**Prior issue:** Current result SHA와 R6S12 Evidence provenance 미결속

**Current QA evidence:** 77/77 result_sha=PENDING_USER_APPLY_COMMIT; 55 ledger rows reference 14 distinct evidence/*.log while pushed r6i-dev/evidence contains only environment.txt. Selected source artifact hashes 7/7 matched package SHA sums, so source integrity is partially preserved but execution provenance is not.

**Target paths:** cpf-docs/work/v9i/dev/r6s12/\*\*

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-002 — P0 — FAIL
**Prior issue:** Current master Push에 Release Workflow 실행 없음

**Current QA evidence:** Current master has no current-SHA release execution evidence. Required workflow also has CPF_FRONTEND_URL export vs CPF_ADM_FRONTEND_URL preflight name mismatch.

**Target paths:** .github/workflows/cpf-r6-release-gates.yml

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-003 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Release Runner가 clean exact-SHA qualification을 증명하지 못함

**Current QA evidence:** run-r6-release-gates.ps1 now enforces ExpectedHead, clean start/end, release DB3/browser/multiprocess and aggregates failures with stdout/stderr hashes. Current-SHA end-to-end run is still unavailable.

**Target paths:** cpf-tools/verification/final-dev/run-r6-release-gates.ps1

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-004 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Playwright Release Gate 실제 Product wiring 미완성

**Current QA evidence:** Workflow installs Chromium/Firefox/WebKit and Release runner wires ADM/BZA browser inputs fail-closed; no authenticated current-SHA browser run evidence.

**Target paths:** cpf-admin/frontend/playwright.config.ts;.github/workflows/cpf-r6-release-gates.yml

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-005 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Integration Closure UI operation permission이 실제 ADM Session과 연결되지 않음

**Current QA evidence:** IntegrationClosurePage uses actual ADM session store hasButton(operationId); E2E checks real /adm/api/auth/me role states. Runtime browser still unverified.

**Target paths:** cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue;cpf-admin/frontend/e2e/integration-closure-r6.spec.ts

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-006 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** ADM generic RouteOperationWorkbench가 전용 화면의 permission/Strict JSON을 우회

**Current QA evidence:** ADM generic RouteOperationWorkbench is GET-only, rejects mutation and requires session grant; dedicated mutation screens required. Runtime unverified.

**Target paths:** cpf-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-admin/frontend/src/App.vue

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-007 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Approval 위험 Operation UI에 action-level permission 미적용

**Current QA evidence:** ApprovalsPage binds risky controls to canAction(operationId)=admSession.hasButton(operationId) and generated client. Source fixed, but frontend verifier semantic permission mutation survives.

**Target paths:** cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-008 — P1 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** ADM 63 Route 중 4개가 59개 sidebar canonical menu에서 누락

**Current QA evidence:** routes.ts has 63 canonical entries and createAdmState projects menus from Object.values(admCapabilityRegistry). Source route/menu count fixed.

**Target paths:** cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/state/createAdmState.ts

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-009 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** ADM generated route-operation contract가 12 Route에서 stale

**Current QA evidence:** Current frontend contract direct execution reproduced 63 routes / 414 bindings / 329 unique operations with route-generated key parity.

**Target paths:** cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/generated/adm-route-operation-contract.ts

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-010 — P0 — PARTIAL
**Prior issue:** Backend validation과 committed ADM OpenAPI/Generated artifact drift

**Current QA evidence:** Committed OpenAPI has 332 operations and is marked CONTROLLER_SOURCE_PRE_RUNTIME; Release requires external runtime parity. Backend validation uses 422 but committed critical response profile omits 422.

**Target paths:** cpf-admin/frontend/openapi/cpf-openapi.json;cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-011 — P1 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Frontend enrich script가 validation/security response 계약의 제2 정본 역할

**Current QA evidence:** enrich-adm-openapi-contract.mjs is validation-only and verifies pre/post hash rather than generating a second canonical artifact.

**Target paths:** cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-012 — P1 — PARTIAL
**Prior issue:** Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음

**Current QA evidence:** Approval high-risk page directly consumes eight generated cpf-api functions; full compile/runtime binding across all high-risk consumers not independently executed.

**Target paths:** cpf-admin/frontend/src/shared/cpfApi.ts;integrationClosureApi.ts;orval-mutator.ts

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-013 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Operation consumer Gate False Green

**Current QA evidence:** ADM/BZA operation consumer scripts now distinguish generated consumers, GET-only workbench and high-risk mutations. Current full npm verify not executed; separate frontend contract semantic permission mutation still survives.

**Target paths:** cpf-admin/frontend/scripts/verify-operation-consumer.mjs;cpf-biz-admin/frontend/scripts/verify-operation-consumer.mjs

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-014 — P0 — PARTIAL
**Prior issue:** R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology

**Current QA evidence:** verify-r6-behavior-contracts.py now runs 17 child verifier token-deletion mutations, but base assertions remain heavily required/forbidden-token based; semantic mutations preserving tokens are not proven killed.

**Target paths:** cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-015 — P1 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** DB Runner Security Test가 process behavior가 아니라 문자열 검사 중심

**Current QA evidence:** run-db3-lifecycle.Tests.ps1 is process-behavioral: child stdin credential envelope, child env secret count, timeout grandchild kill and URL credential rejection. pwsh/live DB runtime unavailable.

**Target paths:** cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1;verify-db3-runner-contract.py

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-016 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** BZA Workbench permission code가 canonical manifest와 불일치

**Current QA evidence:** BZA workbench is GET-only and resolves permissions from canonical bza-permission-manifest action rules. Runtime/browser unverified.

**Target paths:** cpf-biz-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-tools/db/metadata/bza-permission-manifest.json

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-017 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** BZA Release Integrity 미완성

**Current QA evidence:** Release runner includes BZA npm/openapi/browser checks and BZA URL fail-closed source logic; current-SHA BZA runtime evidence absent.

**Target paths:** cpf-biz-admin/build.gradle;cpf-biz-admin/frontend/\*\*

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-018 — P2 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Frontend idempotency storage 정책/표현 불일치

**Current QA evidence:** Integration Closure idempotency code/tests consistently use localStorage v3 with pending 24h, confirmed 7d, generation rotation and no raw payload persistence.

**Target paths:** integrationClosureIdempotency.ts/.test.ts

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-019 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Approval Owner Registry 4D binding이 exact tuple이 아니라 fuzzy matching

**Current QA evidence:** BatchRuntimeApprovalOwnerCommandAdapter and CenterCut adapter use exact 4D tuples ownerModule/ownerCommand/actionType/targetType; service resolves exact tuple.

**Target paths:** BatchRuntimeApprovalOwnerCommandAdapter.java;CenterCutApprovalOwnerCommandAdapter.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-020 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Process Kill 후 Approval EXECUTING/RUNNING 고착 경로

**Current QA evidence:** Approval repository has execution lease/fence and stale RUNNING/EXECUTING sweep to UNKNOWN; process-kill live runtime not independently executed.

**Target paths:** AdmApprovalService.java;AdmApprovalRepository.java;AdmApprovalController.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-021 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Public DQ capability 안전성이 Provider 구현 규율에 의존

**Current QA evidence:** Production DQ correction path is wrapped by final AdmDataQualityCorrectionGateway which verifyAndConsume()s proof before delegate.

**Target paths:** cpf-core/.../CpfDataQualityCorrectionPort.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-022 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** HMAC capability 자체에 expiry/nonce-consumption single-use 없음

**Current QA evidence:** Approval proof has TTL+nonce; persistent nonce repository performs conditional single-use consume with expiry/reference.

**Target paths:** AdmDataQualityApprovalProofService.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-023 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Integration Closure secret가 raw ConfigurationProperties 문자열로 주입 가능

**Current QA evidence:** Production/staging secret resolution rejects raw secret and requires SecretRef/provider; local/dev compatibility remains.

**Target paths:** AdmIntegrationClosureProperties.java;AdmIntegrationClosureConfiguration.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-024 — P1 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** Approval Policy immutability/active overlap이 DB까지 닫히지 않음

**Current QA evidence:** V105 PostgreSQL/Oracle/MariaDB all contain immutable policy enforcement, lock bucket, overlap trigger, execution lease/fence and nonce ledger. Live DB3 migration race not executed.

**Target paths:** AdmApprovalService.java;AdmApprovalRepository.java;DB schema/V104

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-025 — P0 — FAIL
**Prior issue:** Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족

**Current QA evidence:** Owner call occurs outside DB tx; if finishExecutionAndRequest fails, catch immediately calls markExecutionUnknown/history through the same DB repository. A real DB outage can prevent the durable UNKNOWN record too.

**Target paths:** AdmApprovalService.java;AdmApprovalRepository.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-026 — P1 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** BAT Approved Remote Port가 localhost/local instance default로 fail-open

**Current QA evidence:** BAT remote approval port requires explicit remote base URL and rejects localhost/127.0.0.1/::1/0.0.0.0 and local/default/unknown caller instance. Runtime topology unverified.

**Target paths:** cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-027 — P1 — PARTIAL
**Prior issue:** InMemory DQ replay는 multi-instance/process-kill idempotency 증거가 아님

**Current QA evidence:** InMemory DQ implementation explicitly declares process-local/non-production semantics; persistent provider is required for prod/stg. Actual multi-instance/process-kill replay on persistent provider remains unverified.

**Target paths:** cpf-common/.../InMemoryCpfDataQualityOperations.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-028 — P0 — FAIL
**Prior issue:** EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치

**Current QA evidence:** EDU-ADM Catalog requiredRole=CPF_ADM_OPERATOR for 17/17, while all 17 current handlers define CPF_REFERENCE_PLATFORM_OPERATOR. QA37 compile parity expects CPF_ADM_OPERATOR, so current source has 17/17 exact role drift.

**Target paths:** cpf-reference/src/main/resources/edu/manual-135-catalog.json;EduAdm01\~17Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-029 — P0 — FAIL
**Prior issue:** EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음

**Current QA evidence:** EDU consumer contract direct run passes features=135/types=8/mutations=8, proving wiring shape. Catalog still marks 135/135 verificationStatus=미검증; no target runtime proof for all 8 types.

**Target paths:** cpf-reference/src/test/java/com/cpf/reference/edu/runtime/\*\*

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-030 — P0 — FAIL
**Prior issue:** EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심

**Current QA evidence:** EDU-ADM 01~17 remain cpf-reference-owned and bind mostly to generic CPF_EDU_BUSINESS_RECORD rather than actual product Public API/SPI/Extension contracts; product semantics are simulated.

**Target paths:** cpf-reference/src/main/java/com/cpf/reference/optional/operations/\*\*/EduAdm\*Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-031 — P0 — SOURCE_RESOLVED_RUNTIME_UNVERIFIED
**Prior issue:** EDU-ADM readOnly 정본 불일치

**Current QA evidence:** Direct 17-handler comparison shows readOnly parity 17/17 with catalog. Source issue resolved; runtime semantics still unverified.

**Target paths:** manual-135-catalog.json;EduAdm01/09/14/15Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-032 — P0 — FAIL
**Prior issue:** EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음

**Current QA evidence:** EDU-ADM-04 adds local requester/approver/expiry/scope/version checks but persists generic REF DB and does not consume actual Approval product/public integration contract.

**Target paths:** EduAdm04Handler.java;tests/resource contract

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-033 — P0 — FAIL
**Prior issue:** EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음

**Current QA evidence:** EDU-ADM-08 uses payload-provided permission and caller headers, local masking simulation, generic JDBC; no actual authenticated IDOR/raw-role browser matrix.

**Target paths:** EduAdm08Handler.java;scenario-contract.json

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-034 — P0 — FAIL
**Prior issue:** EDU-ADM-09 version conflict/browser flow 의미 불일치

**Current QA evidence:** EDU-ADM-09 constructs conflict state locally against generic JDBC query; does not prove actual HTTP 409 re-read/reapply browser flow.

**Target paths:** EduAdm09Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-035 — P0 — PARTIAL
**Prior issue:** EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체

**Current QA evidence:** EDU-ADM-10 now models concrete targetIds/expectedVersions/per-target outcomes, improving pseudo-partition issue, but still does not call actual product bulk operation; partial.

**Target paths:** EduAdm10Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-036 — P1 — FAIL
**Prior issue:** EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침

**Current QA evidence:** EDU-ADM-11 derives APPLIED/PARTIAL/ROLLED_BACK from payload flags over generic REF DB; no real maintenance/LKG rollback lifecycle.

**Target paths:** EduAdm11Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-037 — P0 — FAIL
**Prior issue:** EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체

**Current QA evidence:** EDU-ADM-12~17 handlers implement incident/evidence/topology/correlation/notification/session as cpf-reference local state/result simulations over generic record, not delivered product contracts.

**Target paths:** EduAdm12\~17Handler.java

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-038 — P0 — FAIL
**Prior issue:** QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함

**Current QA evidence:** QA37 checks catalog/source parity and generated self-test mutations, but its semantic selftest mutates expected JSON, not real handler semantics; it also explicitly forbids cpf-admin/cpf-biz-admin/cpf-gateway dependencies, conflicting with latest product-vs-EDU boundary when a real public product integration should be consumed.

**Target paths:** cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-039 — P0 — FAIL
**Prior issue:** Current SHA에서 EDU 135 target runtime 증거 없음

**Current QA evidence:** Current catalog has 135/135 verificationStatus=미검증. No exact-current-SHA full runtime evidence exists for all examples.

**Target paths:** manual-135-catalog.json;R6S12 evidence

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

### AB-R6-040 — P0 — FAIL
**Prior issue:** QA 진입 전 Codex/필수 Target Runtime 미완료

**Current QA evidence:** Java25/Gradle9.1, DB3 live, authenticated browser, multi-process/process-kill, performance/observability/security/DR, artifact repository/generator lifecycle and Codex remain incomplete or not reproducible at current SHA.

**Target paths:** CODEX\_REVIEW\_REQUEST.md;OPEN\_ISSUES.md

**Acceptance:** 실제 Source·Consumer·호출 경로가 연결되고 Negative/Failure/Concurrency/Recovery 검증이 exact result SHA에서 PASS하며 generated diff 0, Evidence hash 일치

## 4. New R6J findings

### QA-A-R6J-NEW-001 — P0 — FAIL
**Issue:** Required release workflow exports CPF_FRONTEND_URL from CPF_ADM_FRONTEND_URL, but preflight requires CPF_ADM_FRONTEND_URL itself. The variable is not exported into the step environment.

**Evidence:** cpf-r6-release-gates.yml line-equivalent: CPF_FRONTEND_URL=${{ vars.CPF_ADM_FRONTEND_URL }}; required[] contains CPF_ADM_FRONTEND_URL. run-r6-release-gates.ps1 reads CPF_FRONTEND_URL.

**Required development:** Use one canonical environment variable through workflow/preflight/runner and add a clean-runner workflow self-test proving required-check entry.

**Acceptance:** Required workflow reaches qualification with only documented repository variables/secrets; no alias mismatch; missing value fails on the same canonical name.

### QA-A-R6J-NEW-002 — P0 — FAIL
**Issue:** Frontend contract gate does not detect semantic removal of risky-operation permission enforcement.

**Evidence:** QA mutation changed ApprovalsPage.canAction() from admSession.hasButton(operationId) to unconditional true. verify-r6-frontend-contract.py still exited 0 and printed PASS.

**Required development:** Add executable/AST/behavior mutation that proves every risky action is denied without the exact session button grant, and kill unconditional-true/bypass mutations.

**Acceptance:** Permission bypass mutation makes frontend contract/release gate fail; authenticated browser negative role matrix also fails if button/action becomes reachable.

### QA-A-R6J-NEW-003 — P0 — FAIL
**Issue:** Observability qualification accepts self-attested boolean proof and can false-green without authoritative telemetry checks.

**Evidence:** A fake localhost probe returned seven hard-coded true booleans and arbitrary trace/audit strings; run-r6-observability-qualification.py exited 0/PASS.

**Required development:** Qualification must generate known traffic/failures and independently query metrics/logs/traces/timeline/alert/audit stores; verify values, linkage, alert lifecycle and tamper rejection.

**Acceptance:** Fake self-attestation cannot pass; source/artifact identity and authoritative telemetry observations are independently verified.

### QA-A-R6J-NEW-004 — P0 — FAIL
**Issue:** 07_02 added release-critical transaction/file/DB logging requirements after the 77-row R6I developer ledger; 77/77 implementation claim no longer represents the current canonical target.

**Evidence:** CPF_FINAL_TARGET_REQUIREMENTS.md §8.1 added at 07_02. Developer REQUIREMENT_STATUS remains historical 77 rows based on 640490... and result_sha PENDING_USER_APPLY_COMMIT.

**Required development:** Create exact current requirement IDs for transaction lineage, file writer, DB timeline, ADM one-shot lookup, masking/raw audit and runtime evidence; develop/test them before completion claim.

**Acceptance:** Current canonical requirements are represented 1:1 in the active QA/development ledger and verified on successor exact SHA.

### QA-A-R6J-NEW-005 — P0 — FAIL
**Issue:** ADM transactionId one-shot view is only partial: current service aggregates transaction segment/header/external candidates but not all required message, DLQ, batch, file, trace/audit and source freshness states.

**Evidence:** AdmTransactionGroupService uses CpfTransactionTimelineQueryPort.findGroups/findSegments/findExternalCandidates and returns segments/timeline/summary/headers/externalLogs. Port has no message/batch/file/audit source methods. TransactionsPage timeline navigation also uses transactionName rather than selected execution transactionId.

**Required development:** Build a canonical multi-source transaction timeline/tree aggregator with source status, partial/stale warnings and secure links; preserve exact transactionId in all UI navigation.

**Acceptance:** One exact transactionId reconstructs local/remote/retry/message/DLQ/file/batch/worker/UNKNOWN/reconcile/audit timeline and tree in ADM from real runtime data.

### QA-A-R6J-NEW-006 — P0 — FAIL
**Issue:** Canonical CPF_TRANSACTION_SEGMENT schema/query surface alone does not carry or expose the full new standard identifier set or multi-source linkage.

**Evidence:** PostgreSQL canonical cpf_transaction_segment has transaction/parent/attempt/external/instance fields and strong indexes, but lacks traceId/spanId/requestId/idempotencyKey/tenant plus batch jobInstance/jobExecution/step/partition/item/agent/worker and message/file identifiers. Current timeline port reads only this segment source.

**Required development:** Define canonical DB3 schema/index/join model for required standard identifiers and source correlation, with retention/partition/archive/purge and large transactionId lookup tests.

**Acceptance:** Oracle/PostgreSQL/MariaDB parity plus indexed single-ID reconstruction with all conditional identifiers and retention/recovery semantics.

### QA-A-R6J-NEW-007 — P0 — FAIL
**Issue:** EDU runtime authorization trusts caller-provided actor, roles and data-scope headers.

**Evidence:** EduCapabilityController directly reads X-Cpf-Actor-Id, X-Cpf-Roles and X-Cpf-Data-Scope into EduExecutionCommand/recovery operations. cpf-reference has no demonstrated SecurityFilterChain that replaces these values with authenticated Principal claims.

**Required development:** Resolve actor/roles/scope from authenticated framework-owned security context; reject/ignore spoofable client security headers and add negative tests.

**Acceptance:** Forged actor/role/scope headers cannot elevate permission or alter audit identity in real authenticated runtime.

### QA-A-R6J-NEW-008 — P0 — FAIL
**Issue:** PROCESS EDU consumer inherits parent process environment and writes full command payload to an OS temp JSON file.

**Evidence:** ProcessEduBusinessConsumer creates a limited environment map but only putAll()s it into ProcessBuilder.environment() without clear(); writes json(command.payload()) via Files.createTempFile; cleanup comment assumes payload contains no secrets.

**Required development:** Use explicit environment allowlist/clear, secret-safe IPC or protected temp with strict permissions/encryption/minimized payload, deterministic cleanup and crash-recovery scrub.

**Acceptance:** Child sees only allowlisted environment; sensitive payload never appears in plaintext temp or inherited environment; process-kill cleanup/loss tests pass.

### QA-A-R6J-NEW-009 — P0 — FAIL
**Issue:** QA37 EDU gate encodes the old self-contained cpf-reference duplication model and conflicts with latest §16.1 product/EDU boundary.

**Evidence:** verify-cpf-qa37-manual-edu-135.py explicitly rejects source containing cpf-admin/cpf-biz-admin/cpf-gateway and requires consumer owner cpf-reference; EDU-ADM handlers therefore simulate product semantics over CPF_EDU_BUSINESS_RECORD instead of consuming official public integration contracts.

**Required development:** Redesign EDU acceptance around intended adopter + actual Public API/SPI/Extension/Integration contract; reclassify EDU-ADM 17 before rewriting catalog/tests/manual/generator.

**Acceptance:** Retained EDU examples invoke real public contracts without depending on product internals; Product ADM functions are validated in product modules, not duplicated in EDU.

### QA-A-R6J-NEW-010 — P1 — FAIL
**Issue:** Backend Approval validation returns HTTP 422, but committed critical OpenAPI response profiles and Integration Closure frontend error taxonomy omit 422.

**Evidence:** AdmApprovalExceptionHandler maps validation to HttpStatus.UNPROCESSABLE_ENTITY. Current cpf-openapi critical response lists and frontend integrationClosureApi classification include 400/401/403/404/409/429/500/503 but not 422.

**Required development:** Publish 422 in runtime OpenAPI/generated client and handle it explicitly in ADM/BZA UX/tests.

**Acceptance:** Backend runtime OpenAPI, generated client, UI state and browser failure matrix agree on 422 behavior.

## 5. EDU-ADM 17 architecture
The exact 17-row decision is in `QA_A_EDU_ARCH_CLASSIFICATION.csv`.

- Role parity: **0/17 PASS, 17/17 FAIL**
- readOnly parity: **17/17 PASS**
- Current architecture: all 17 remain `cpf-reference` examples and bind through generic reference storage rather than actual product public contracts.
- QA A disposition: keep only genuine adopter-facing extension/integration patterns; product search/security/version conflict/bulk/config/incident/evidence/topology/transaction/notification/session belongs to delivered ADM.

## 6. EDU 135 category review
- DEV: 45
- BAT: 30
- ADM: 17
- OPS: 15
- BZA: 14
- GW: 14
- Consumer types: `{'PROCESS': 17, 'JDBC_QUERY': 6, 'JDBC_COMMAND': 51, 'HTTP': 5, 'OUTBOX': 6, 'FILE': 6, 'SPRING_BATCH': 30, 'REFERENCE_GATEWAY': 14}`
- verificationStatus=미검증: **135/135**

Quantity 135 is not acceptance. Actual target runtime and Public Consumer educational value remain required.

## 7. Transaction / logging
See `QA_A_LOGGING_MATRIX.csv` for each criterion.

Positive source findings:
- 34-char transactionId generation;
- segment parent/attempt model and indexes;
- bounded DB/file queues;
- fallback preservation;
- file path/lock/permission/retention/compression logic;
- ADM transaction group endpoints.

Release blockers:
- trust boundary for incoming transactionId is not clearly topology-independent;
- one-shot multi-source timeline is incomplete;
- canonical DB timeline lacks full required identifier set;
- Message/DLQ/Batch/File/Audit source correlation and partial/stale state missing from current aggregator;
- distributed/failure runtime evidence absent.

## 8. Approval / security
Substantial source hardening is real:
- exact 4D owner tuple;
- action/session permission wiring;
- SoD/snapshot validation;
- execution lease and stale sweep;
- nonce TTL/single-use;
- SecretRef production guard;
- DB3 policy immutability/overlap.

But `AB-R6-025` remains P0: the fallback UNKNOWN record is written to the same DB that just failed finalization.

## 9. Verification-tool adversarial review
- `verify-r6-frontend-contract.py`: baseline PASS; permission semantic bypass survivor **FAIL**.
- `verify-r6-edu-consumer-runtime-contract.py`: baseline + 8 mutation self-test PASS; does not cover current security/architecture defects.
- `verify-r6-behavior-contracts.py`: 17 child token-deletion mutations; semantic token-preserving mutation remains partial.
- `verify-r6-sql-parity.py`: strong static DB3 pack/token checks; live DB not covered.
- `run-r6-hardening-qualification.py`: orchestrates H001-H012 but inherits the observability false-green.
- `run-r6-supply-chain-qualification.py`: self-test PASS; real Syft/Grype/ORT/signature final artifact unverified.
- `run-r6-artifact-consumer-qualification.py`: self-test PASS; real REMOTE/OFFLINE repos unverified.
- `run-r6-security-negative-qualification.py`: source requires 11 categories and actual HTTP/command execution; current target corpus run unverified.
- `run-r6-observability-qualification.py`: fabricated proof survivor **FAIL**.
- `run-r6-dr-qualification.ps1`: source is exact-SHA/clean/encrypted-backup fail-closed; live DR unverified.
- `run-r6-release-gates.ps1`: strong exact-SHA/failure aggregation source; required workflow input naming defect remains.

## 10. DB3 / Artifact / DR
DB source parity is not the same as operational completion. Oracle/PostgreSQL/MariaDB live lifecycle, CAS/unique races, backup/restore/RTO/RPO and immutable artifact supply-chain identity must be rerun on successor SHA.

## 11. Required development
### Legacy FAIL/PARTIAL
- `AB-R6-001` — FAIL
- `AB-R6-002` — FAIL
- `AB-R6-010` — PARTIAL
- `AB-R6-012` — PARTIAL
- `AB-R6-014` — PARTIAL
- `AB-R6-025` — FAIL
- `AB-R6-027` — PARTIAL
- `AB-R6-028` — FAIL
- `AB-R6-029` — FAIL
- `AB-R6-030` — FAIL
- `AB-R6-032` — FAIL
- `AB-R6-033` — FAIL
- `AB-R6-034` — FAIL
- `AB-R6-035` — PARTIAL
- `AB-R6-036` — FAIL
- `AB-R6-037` — FAIL
- `AB-R6-038` — FAIL
- `AB-R6-039` — FAIL
- `AB-R6-040` — FAIL

### New
- `QA-A-R6J-NEW-001` — P0
- `QA-A-R6J-NEW-002` — P0
- `QA-A-R6J-NEW-003` — P0
- `QA-A-R6J-NEW-004` — P0
- `QA-A-R6J-NEW-005` — P0
- `QA-A-R6J-NEW-006` — P0
- `QA-A-R6J-NEW-007` — P0
- `QA-A-R6J-NEW-008` — P0
- `QA-A-R6J-NEW-009` — P0
- `QA-A-R6J-NEW-010` — P1

## 12. QA re-entry conditions
A successor master may enter QA only when:
- required workflow variable contract is fixed;
- current result SHA and execution Evidence are immutable and complete;
- legacy FAIL/PARTIAL findings are implemented;
- new R6J findings are implemented;
- 51 runtime-unverified rows are executed where applicable;
- Java25/Gradle9.1 + DB3 + authenticated three-browser + multiprocess/process-kill + performance/observability/security/DR + artifact/generator lifecycle are available;
- transactionId one-shot runtime includes all required sources and partial/stale states;
- QA B cross-review disagreements are resolved centrally.

## 13. Safety / repository integrity
QA A performed no product source edit, commit, push, branch, tag, PR, reset, restore, stash, clean, file delete or history modification.
