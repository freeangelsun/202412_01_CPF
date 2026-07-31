# CPF QA34 Post-Push Independent Source Review

## 1. Final review judgment

Latest reviewed `master` is `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a` (`20260801_01`). The push contains meaningful improvements in build canonicalization, BFF authentication boundaries, Kafka synchronous worker handling, pinned outbound transports, frontend verification scripts, and QA34 runner scaffolding.

However, the repository is **not yet project-complete and must not be handed to Codex as verification-only work**. The current source has fail-always verifier paths, stale generated frontend artifacts, incomplete tracked OpenAPI, non-specific evidence fan-out, and no current exact-SHA runtime evidence. The next work must be treated as a final integrated development and closure task, not as a documentation-only correction.

## 2. Confirmed P0 findings

### 2.1 Post-push state is stale

The completion report still records `c2e1680...`, while latest master is `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`. `CPF_CURRENT_WORK_REQUEST.md` states only one independent verification remains even though the pushed source and generated artifacts are not internally closed.

### 2.2 The advertised one-pass verifier cannot complete

- `verify-cpf-qa34-all.ps1` references missing `reclassify-cpf-qa33-exact-sha.py`.
- `verify-cpf-qa34-finalize.ps1` references missing `verify-cpf-qa34-evidence-closure.py`.

These are fail-always defects and must be repaired before any expensive Codex run.

### 2.3 QA33 completion can be falsely fanned out

The runtime matrix can place every QA33 requirement/scenario/result ID in one evidence object. The reclassifier completes rows based on ID membership, not on a scenario-specific command result. A small set of runtime steps can therefore promote 552 rows without proving them individually.

### 2.4 QA34 matrices are templates, not executable traceability

The 60 scenarios are mechanically generated normal/error/recovery sentences. They reuse broad acceptance text and the shared evidence ID `QA34-FULL-RUNTIME-MATRIX`. This cannot prove which command validated which behavior.

### 2.5 Frontend canonical artifacts are not closed

At the reviewed SHA:

- ADM tracked OpenAPI: two authentication operations, zero schemas.
- BZA tracked OpenAPI: four authentication operations, zero schemas.
- ADM generated directory lacks the new operation-contract and Orval output expected by current scripts.
- tracked marker remains schema v2 and points to historical `c1f273...`.
- legacy `source-sha.json` remains even though the new verifier rejects it.

A fresh generation is therefore expected to alter tracked files or fail. The current push cannot support a clean frontend release verification.

### 2.6 No exact-SHA QA34 runtime evidence exists

The push adds evidence schema/template and runners, but no Java 25, browser, three-vendor DB, Kafka/process-kill, deployment, or supply-chain success evidence bound to `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`.

## 3. Confirmed partial implementations

- BFF authentication/CSRF/session defaults improved, but real ADM/BZA endpoint-permission coverage is missing.
- Kafka request processing has a synchronous channel and ACK-after-handler flow, but real broker/rebalance/process-kill evidence is missing.
- Gateway DNS pinning design improved, but production TLS/allowed-port/lifecycle contracts and live TLS tests are incomplete.
- Batch and Host Agent transports are connected to real consumers, but duplicate CIDR parsers permit DNS lookup in CIDR configuration and need one owned literal parser.
- DB runner has dry-run/apply/rollback scaffolding, but does not prove an exact QA32 baseline and sequential upgrade state per vendor.

## 4. Why repeated QA rounds continue

The repeated work is not primarily caused by QA being overly strict. It is caused by a completion model that promotes implementation scaffolding to completion before executable closure:

1. Source/test/runner existence was treated as feature completion.
2. Generated matrices were created from requirement prose instead of actual test cases.
3. One evidence object claimed many unrelated IDs.
4. Post-push SHA and generated outputs were not synchronized.
5. Verifier wrappers were not self-tested before delivery.
6. Compile/runtime work was delegated to Codex while development was already marked complete.
7. Source and separately owned guides were mixed into one large review scope.
8. Network-security primitives were duplicated between modules.
9. A working final evidence closure path was never completed.

The QA35 request prevents recurrence by making wrapper preflight, per-row evidence, post-push synchronization, generated-output cleanliness, actual consumer coverage, and final external evidence closure mandatory gates.

## 5. Protected successful functions

The next work must preserve:

- canonical plugin ID `com.cpf.platform-conventions`
- plugin publication group `com.cpf.gradle`
- BOM `com.cpf:cpf-platform-bom`
- `pluginManagement.includeBuild` convention plugin supply
- official DB vendors Oracle/PostgreSQL/MariaDB only
- BFF fail-closed authentication, CSRF, session fixation, concurrent-session rules
- Kafka ledger completion before ACK
- DNS pinning with original hostname TLS/SNI validation
- Batch/Agent request size, response size, content type, digest, redirect, header injection protections
- `cpf-tools/build` as protected source, not disposable build output
- no unauthorized Git write operations

## 6. Required next action

Use `cpf-docs/work/current/CPF_20260801_QA35_FINAL_COMPLETION_DEVELOPMENT_REQUEST.md` as the sole current development request. Do not run the previous QA34 Codex request until QA35 P0 source defects and verifier contracts are closed. After QA35 development is pushed, use the new QA35 Codex request exactly once from a fresh clone.
