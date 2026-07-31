# CPF QA35 Final Completion Development Request

## 1. Baseline and objective

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Review baseline SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a` (`20260801_01`)
- Canonical target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Input registers: QA35 Defect 36, Requirement 43, Root Cause 15, ADM Capability 68, ADM Menu/Route 59

This is a **final integrated development and release-closure request**. Do not treat it as a verification-only request. Do not leave partial implementation or unimplemented source. Runtime steps that cannot run due to environment remain `미검증`, but all implementable source, tests, gates, fixtures, scripts, matrices, and evidence contracts must be completed.

## 2. Mandatory reading order

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
3. `cpf-docs/work/review/CPF_20260801_QA34_POST_PUSH_REVIEW_PLAN.md`
4. `cpf-docs/work/review/CPF_20260801_QA34_POST_PUSH_INDEPENDENT_SOURCE_REVIEW.md`
5. `cpf-docs/quality/CPF_20260801_QA35_ROOT_CAUSE_MATRIX.csv`
6. `cpf-docs/quality/CPF_20260801_QA35_DEFECT_REGISTER.csv`
7. `cpf-docs/quality/CPF_20260801_QA35_REQUIREMENT_MATRIX.csv`
8. `cpf-docs/work/review/CPF_20260801_QA35_ADM_REFERENCE_INVENTORY.md`
9. `cpf-docs/work/review/CPF_20260801_QA35_ADM_BATCH_ONLINE_BENCHMARK_REVIEW.md`
10. `cpf-docs/quality/CPF_20260801_QA35_ADM_CAPABILITY_MATRIX.csv`
11. `cpf-docs/quality/CPF_20260801_QA35_ADM_MENU_ROUTE_MATRIX.csv`
12. `cpf-docs/work/current/CPF_20260801_QA35_SELF_DEVELOPMENT_REQUIREMENTS.md`
9. latest actual Source, SQL, tests, build, frontend, scripts, and evidence

## 3. Work order designed to prevent repetition

### Phase 0 — Baseline and ownership

- fetch latest `master`, record HEAD/origin/master/working tree
- distinguish other-worker changes
- create a pre-development impact review
- protect README/guides unless their owner and scope are explicitly approved

### Phase 1 — Repair the verification foundation first

Complete QA35-REQ-001 through QA35-REQ-005 before any expensive runtime work:

- fix missing wrapper dependencies
- implement verifier-of-verifiers preflight and negative fixtures
- implement final evidence closure
- remove bulk QA33 ID fan-out
- replace artifact-presence completion semantics

No Java/Frontend/DB full run may begin until the verifier preflight passes.

### Phase 2 — Close OpenAPI and frontend consumers

Complete QA35-REQ-006 through QA35-REQ-009:

- start actual ADM/BZA backend profiles and export full OpenAPI
- include request/response schemas, validation, paging, auth, and standard errors
- regenerate Orval clients, operation contracts, and schema v3 markers
- delete legacy generated SHA artifacts through the Delete Manifest when tracked
- migrate real screens to typed generated consumers
- prove zero generated drift
- run clean npm and three-browser matrix once

### Phase 3 — Close actual authorization

Complete QA35-REQ-010:

- inventory every real ADM/BZA route and HTTP method
- map owner, operation ID, required permission, risk classification
- reject authenticated-only privileged endpoints
- execute real-controller 401/403/CSRF/session/permission tests


### Phase 3A — Complete commercial integrated ADM before runtime sign-off

Complete QA35-REQ-024 through QA35-REQ-043 as one architecture-aware ADM backlog:

- preserve CPF strengths; do not clone legacy screens
- implement canonical nested menu/capability registry and remove silent fallback
- fix Gateway route-to-tab mapping
- replace generic Batch runtime wrappers with feature-specific typed workbenches
- complete Batch Job/Schedule/Execution/Agent/Worker/HA/Recovery/Risk Governance
- complete Online Definition/Deployment/Diagnostics/Observability/Analysis
- implement Batch-Online-Gateway-Incident-Audit unified trace and deep links
- complete System/Common minimum administration capabilities
- enforce the full ADM capability graph and commercial page contract across all routes

Do not start browser sign-off until ADM Capability and Menu/Route matrices have actual Source/API/Permission/Test mappings.

### Phase 4 — Build and database closure

Complete QA35-REQ-011 and QA35-REQ-012:

- Java 25 empty-cache full build/publication/consumer verification
- exact QA32 DB baseline per Oracle/PostgreSQL/MariaDB
- sequential V83/V86-V91 upgrade with state assertions
- reverse rollback, reapply, drift, runtime query, index/FK/check verification

### Phase 5 — Distributed runtime and outbound security

Complete QA35-REQ-013 through QA35-REQ-016:

- real Kafka and multi-instance/process-kill scenarios
- scheduler lease/fencing/recovery
- gateway TLS/port/lifecycle and live network tests
- common literal IP/CIDR policy shared by Gateway/Batch/Agent
- artifact activation/rollback/compensation/reconcile

### Phase 6 — Supply chain and row-by-row reclassification

Complete QA35-REQ-017 through QA35-REQ-021:

- actual released artifact SBOM/vulnerability/license/hash evidence
- QA33 138/414/552 row-by-row reclassification
- QA34 20/60/80 scenarios replaced with executable rows
- current docs/handover/continuity synchronized to pushed SHA
- superseded current documents archived and garbage removed

### Phase 7 — One-pass independent review

Complete QA35-REQ-022 and QA35-REQ-023:

- create one exact copyable Codex command
- preflight once, then low-cost → Java → Frontend → DB → Runtime → Supply-chain once each
- no source changes during independent review
- produce source-clean external evidence bundle and final index
- declare complete only when matrices, artifacts, evidence, current docs, and exact SHA agree

## 4. Completion prohibitions

Do not mark complete when any of the following is true:

- a wrapper references a missing file/task/parameter
- OpenAPI contains only bootstrap/auth operations
- generated output changes after a fresh generation
- a UI wrapper references operations not present in the contract
- synthetic security controllers are the only authorization evidence
- a DB upgrade did not start from a verified baseline
- one evidence object claims unrelated rows without per-row results
- runtime was not executed but development is complete only because a runner exists
- current docs or markers contain stale SHA
- a menu is counted complete only because its route or component exists
- multiple advertised menus open one default page without route-specific state
- a Batch operational menu is only a generic dynamic table
- Batch and Online share IDs but have no executable cross-navigation scenario
- a required ADM capability is absent from the capability graph
- independent verification modifies source
- README/guides scope ownership is unresolved

## 5. Required deliverables

Produce one project-root-relative ZIP overlay containing:

- all source/test/script/config/SQL changes
- pre-development review and post-development review
- updated self-development requirements and completion report
- QA35 defect/requirement/scenario/result matrices
- QA33 exact-SHA reclassified matrices
- sanitized external-evidence index references
- handover and continuity state
- Codex final independent review request
- changed-file manifest, file SHA-256, delete manifest

Do not commit or push without explicit user approval.
