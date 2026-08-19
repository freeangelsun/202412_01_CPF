# CPF 최종 개발 패키지 인수인계

## 1. Current basis

- Baseline source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260819_003152.zip`
- Baseline source ZIP SHA-256: `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850`
- Baseline files: `8,440`
- Exact Git SHA: unavailable because the supplied ZIP has no `.git`; never substitute a historical SHA.
- Git write operations: none performed. No commit/push/branch/tag/reset/restore/stash/clean/history rewrite was performed.
- Physical source deletion: none performed. `cpf-docs/deliverables/DELETE_MANIFEST.csv` contains root-relative candidates with `approved=false`.

## 2. Superseding architecture

### Transaction / context

Canonical Business Domain transaction transport is System-based 6 headers:

1. `X-Transaction-Id`
2. `X-Original-System-Code`
3. `X-System-Code`
4. `X-Caller-System-Code`
5. `X-Target-System-Code`
6. `X-Target-Operation-Id`

Channel identity is a separate optional security/policy/ingress context. `X-System-Code` is receiver-owned trusted runtime identity. Same-JVM and Remote calls preserve one logical System/Operation contract; same-JVM does not create self-HTTP.

### BZA

- `cpf-biz-admin`: CPF-internal **Optional Prebuilt Business Administration Domain**. Not Generator-created, but follows generated-domain public contracts.
- BZA-owned data only: Backoffice approval state, BZA business permission, Backoffice settings, etc. Member/Customer/Account masters remain exactly-one owned by their Business Domain. No cross-domain DB direct access.
- `cpf-biz-channel`: external **DB-less Pure Spring Boot** BFF, CPF Java/BOM/Starter/Internal dependency 0, HTTP/HTTPS only.
- `cpf-biz-frontend`: external optional Reference Frontend; browser calls Channel only. Current reference routes are four representative workflows, not a fixed full customer backoffice.
- Direct Public HTTP is not a security bypass. It must preserve authN/authZ/Channel Policy/Audit/Canonical Header/Operation enforcement.
- BZA source/DB/channel/frontend can be omitted for customers not using BZA without breaking mandatory CPF Build/Runtime.

### Optionality

All canonical optional/user-selectable modules/capabilities follow physical-removal semantics: absence must not break root settings/build/test/publication/installer/verifier; presence joins aggregate regression. Optional DB/deploy/listener/scheduler/external connection surfaces activate only when selected/present.

### Source quality

CPF completion includes architecture/ownership/package/naming/dependency direction/discoverability/change isolation/operability. Do not split files mechanically by size; use feature-first + meaningful role boundaries. Frontend follows feature pages/components/api/model/composables where needed.

## 3. Implemented/closed source-static areas

- QA3 runtime-control/OpenAPI/header blockers repaired and related stale gates currentized.
- Canonical System 6 header/context flow currentized through Core/Web/HTTP/Observability.
- Runtime Instance identity consolidated to the Foundation provider with invalid fallback fail-close.
- Subject late-enrichment search uses original transaction start time; firstSeen remains provenance.
- Pre-controller header rejection is routed to sanitized durable Observability evidence.
- Common active runtime converged to `cpfDB` + canonical Common Management/Catalog API; legacy CMN runtime has no active main-source consumer.
- ADM Server Management separated by feature ownership and changed from client-side hard-cap paging to typed server-side paging.
- ADM route registry split by meaningful operations domains and its generator/verifier currentized.
- Education Online 20 / Batch 15 restructured to feature-first role packages; 29 superseded flat entries are Delete Manifest candidates.
- BZA external Channel/Frontend implemented; internal Domain OpenAPI is canonical owner; legacy embedded Frontend is no longer active.
- Optional Surface policy/gate covers source-removable applications and selectable Starters.
- Public Distribution adds default-deny allowlist/classification staging and a fail-closed publish driver under existing release ownership.
- Canonical/Current Markdown requirements currentized to the latest Architecture.

## 4. Delete / garbage management

- Delete Manifest candidate count: 272 at this handover point.
- Categories: 29 superseded Education flat sources, 239 legacy embedded BZA Frontend files, 4 stale BZA UI fixtures.
- `approved=false` for every entry; no physical delete has been executed.
- Protected delete count: 0 for `cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`.
- Desired-state verification excludes these candidates to prove the post-delete source surface before user approval.

## 5. Current verified static/independent evidence

Latest desired-state rerun includes PASS for requirement projection, BZA boundary, Frontend consumer/syntax/golden path, BZA/ADM workflow harness, Education active/executable coverage, Common DX, Optional Surface, Operator Trust, NXT3 ADM/BZA/Gateway, Publication Starter closure, ADM route-operation contract, Generated Client contract and Java source syntax. Public staging is default-deny. Final static rerun also records NXT3 23/23 and focused release/BZA/frontend/evidence Python tests 22/22. Exact counts are recorded in `TEST_AND_EVIDENCE.md`.

## 6. Must remain unverified until user-local runtime

- Java 25 Root Gradle full configuration/compile/test/build/publication/SBOM.
- Standalone `cpf-biz-channel` Gradle build/test.
- Official Node >=22.18 clean ADM/BZA frontend install/build/test.
- Live Oracle/PostgreSQL/MariaDB fresh/upgrade/runtime/rollback.
- Redis/Valkey reconnect/failover.
- Multi-WAS policy/identity/lease/concurrency.
- process-kill/restart/redeploy recovery.
- latest external BZA Channel+Frontend and ADM real-browser E2E.
- Windows PowerShell validation scripts in actual user environment.
- real Public Git remote clone/commit/push.

These are `미검증`, not PASS. Any failure reopens the same Requirement by root cause.

## 7. Public Git release

Canonical user-local entrypoint: `cpf-tools/release/public/publish-cpf-public-repository.ps1`. It requires a clean private Git worktree, runs private gates/build/publication, creates empty default-deny staging, verifies leakage/classification/clean public workspace, checks staged diff, and only reaches push when all gates PASS and the user explicitly supplies `-Push`. No gate-bypass push option exists.

## 8. Continuation / QA rules

1. Do not inherit prior PASS/Evidence or historical Git SHA.
2. Final verifier must keep both real Git checkout and ZIP/fallback execution modes. `git ls-files -z` must split actual `b'\0'`.
3. New QA findings must be re-reproduced against this exact source; solved/stale/environment items are not re-developed blindly.
4. Do not alter QA/Codex-owned ledger judgments from Developer GPT.
5. `WORK_RESULT_REVIEW` is **not generated automatically**. When the user explicitly asks after final packaging, produce a detailed 1:1 Requirement/QA/Steering → implementation/path/consumer/validation/remaining-condition report.

## 9. Canonical evidence

- `cpf-docs/work/TEST_AND_EVIDENCE.md`
- `cpf-docs/work/REQUIREMENT_STATUS.csv`
- `cpf-docs/work/CHANGE_MANIFEST.csv`
- `cpf-docs/work/OPEN_ISSUES.md`
- `cpf-docs/work/PACKAGE_MANIFEST.json`
- `cpf-docs/deliverables/DELETE_MANIFEST.csv`
- `cpf-docs/work/current/CODEX_REVALIDATION_REQUEST.md`
