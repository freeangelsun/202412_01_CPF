# QA A Opinion — R6J Full Review

## WORKER_OPINION
**미통과 / RELEASE_BLOCKED**

This is not a file-existence review. QA A re-opened all 40 R6I findings against current `3ed676061246c9db3e44f29e254c0393ecca3929`, inspected the relevant product consumers and failure paths, executed current-source Frontend/EDU gates where possible, performed semantic mutation/adversarial tests, reviewed DB3 source parity and traced transaction/logging runtime wiring.

### Regression disposition
- R6I findings reviewed: **40/40**
- Source-resolved but target runtime still unverified: **21**
- Still FAIL: **14**
- PARTIAL: **5**
- Fully auto-closed from developer claim: **0**

### New R6J findings
- New findings: **10**
- P0: **9**
- P1: **1**

## DISAGREEMENT
1. `77/77 development_status=완료` cannot equal current canonical completeness because 07_02 added release-critical transaction/logging and EDU architecture requirements after that ledger.
2. `Frontend Contract PASS` is not permission-safety proof; an unconditional permission bypass mutation survives.
3. `Observability PASS` is not trustworthy while a fabricated boolean probe passes unchanged.
4. `EDU Consumer PASS` proves 135/8 wiring shape only; it does not prove authorization, architecture correctness or actual target runtimes.
5. Source parity for DB3 is not live DB3 lifecycle PASS.

## ARCHITECTURE_DECISION_REQUIRED
Adopt the 17-row classification in `QA_A_EDU_ARCH_CLASSIFICATION.csv`. Product ADM functions should move to PRODUCT_ADM validation; only genuine adopter-facing Public API/SPI/Extension/Integration examples should remain EDU.

## ADDITIONAL_QA_REQUIRED
QA B should independently challenge:
- the release workflow variable mismatch;
- permission semantic mutation survivor;
- observability fake-proof survivor;
- EDU actor/role/scope spoofing;
- EDU QA37 architecture conflict;
- 17/17 EDU-ADM role drift;
- exact transactionId one-shot multi-source timeline;
- Approval DB-outage durable UNKNOWN;
- runtime OpenAPI 422 parity;
- full authenticated browser and 332-operation runtime closure.

## ADDITIONAL_DEVELOPMENT_REQUIRED
Legacy exact IDs still FAIL/PARTIAL:
- `AB-R6-001` — FAIL — Current result SHA와 R6S12 Evidence provenance 미결속
- `AB-R6-002` — FAIL — Current master Push에 Release Workflow 실행 없음
- `AB-R6-010` — PARTIAL — Backend validation과 committed ADM OpenAPI/Generated artifact drift
- `AB-R6-012` — PARTIAL — Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음
- `AB-R6-014` — PARTIAL — R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology
- `AB-R6-025` — FAIL — Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족
- `AB-R6-027` — PARTIAL — InMemory DQ replay는 multi-instance/process-kill idempotency 증거가 아님
- `AB-R6-028` — FAIL — EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치
- `AB-R6-029` — FAIL — EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음
- `AB-R6-030` — FAIL — EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심
- `AB-R6-032` — FAIL — EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음
- `AB-R6-033` — FAIL — EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음
- `AB-R6-034` — FAIL — EDU-ADM-09 version conflict/browser flow 의미 불일치
- `AB-R6-035` — PARTIAL — EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체
- `AB-R6-036` — FAIL — EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침
- `AB-R6-037` — FAIL — EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체
- `AB-R6-038` — FAIL — QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함
- `AB-R6-039` — FAIL — Current SHA에서 EDU 135 target runtime 증거 없음
- `AB-R6-040` — FAIL — QA 진입 전 Codex/필수 Target Runtime 미완료

New exact IDs:
- `QA-A-R6J-NEW-001` — Required release workflow exports CPF_FRONTEND_URL from CPF_ADM_FRONTEND_URL, but preflight requires CPF_ADM_FRONTEND_URL itself. The variable is not exported into the step environment.
- `QA-A-R6J-NEW-002` — Frontend contract gate does not detect semantic removal of risky-operation permission enforcement.
- `QA-A-R6J-NEW-003` — Observability qualification accepts self-attested boolean proof and can false-green without authoritative telemetry checks.
- `QA-A-R6J-NEW-004` — 07_02 added release-critical transaction/file/DB logging requirements after the 77-row R6I developer ledger; 77/77 implementation claim no longer represents the current canonical target.
- `QA-A-R6J-NEW-005` — ADM transactionId one-shot view is only partial: current service aggregates transaction segment/header/external candidates but not all required message, DLQ, batch, file, trace/audit and source freshness states.
- `QA-A-R6J-NEW-006` — Canonical CPF_TRANSACTION_SEGMENT schema/query surface alone does not carry or expose the full new standard identifier set or multi-source linkage.
- `QA-A-R6J-NEW-007` — EDU runtime authorization trusts caller-provided actor, roles and data-scope headers.
- `QA-A-R6J-NEW-008` — PROCESS EDU consumer inherits parent process environment and writes full command payload to an OS temp JSON file.
- `QA-A-R6J-NEW-009` — QA37 EDU gate encodes the old self-contained cpf-reference duplication model and conflicts with latest §16.1 product/EDU boundary.
- `QA-A-R6J-NEW-010` — Backend Approval validation returns HTTP 422, but committed critical OpenAPI response profiles and Integration Closure frontend error taxonomy omit 422.

## NEXT_ACTION
Fix all FAIL/PARTIAL items, bind evidence to a successor exact master SHA, execute the remaining target runtimes, then run QA A and QA B independently again. Source-resolved regression rows are not Release PASS until their required runtime/evidence acceptance is satisfied.
