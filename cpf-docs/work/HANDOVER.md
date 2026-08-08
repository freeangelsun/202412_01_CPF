# CPF Developer GPT Handover

## Current basis

- Repository: `freeangelsun/202412_01_CPF`
- Source basis: `master@4c4248a12e699c07f9f5fb11fbb33b97ca04077d`
- Session: Development/Verification Session 18
- Developer implementation scope: NXT 47 exact IDs
- Developer status: 43 `완료`, 4 independent-QA IDs `해당 없음`
- Developer-remediable gaps: 0 in final Overlay low-cost gates
- QA final status: **not adjudicated**

## Apply order

1. Start from a clean repository at the intended target SHA and confirm `git status --short`.
2. Apply the Overlay only; it performs no delete.
3. Review `CPF_DELETE_MANIFEST.csv`. Core relocation rows with `PENDING_USER_APPROVAL` require explicit user authorization.
4. If authorized, run the exact-file delete helper.
5. Run low-cost verify helper and `git diff --check`.
6. Run Java 25 full Gradle/Frontend/DB3/Valkey/S3/runtime gates from `RUNTIME_ONLY_VERIFICATION.csv`.
7. Review `git status --short`, `git diff --stat`, and secrets before any commit/push.
8. User performs Git write only after review; QA then uses the resulting central exact SHA.

## High-risk review points

- Core originals must not coexist with relocated implementations after authorized delete.
- `cpf-core` must remain independent from Web/WebFlux/Servlet/Batch/OTel/AWS/Valkey provider APIs.
- Public BOM must not expose internal provider leaves.
- DB scope is Oracle/PostgreSQL/MariaDB only.
- Runtime Health JDBC schema must be installed/migrated/rolled back on all three vendors.
- Lock fencing/force-release audit, Session forced logout/rotation, Object Storage tenant isolation/checksum and Health drain/multi-instance paths require real runtime tests.
- ADM Health OpenAPI/generated client must be regenerated from the running backend and diffed against the checked-in snapshot.

## Key artifacts

- `cpf-docs/work/REQUIREMENT_STATUS.csv`
- `cpf-docs/work/TEST_AND_EVIDENCE.md`
- `cpf-docs/work/RUNTIME_ONLY_VERIFICATION.csv`
- `cpf-docs/work/CPF_FUNDAMENTAL_BASELINE_AUDIT.csv`
- `cpf-docs/work/CPF_CORE_SLIMMING_AUDIT.csv`
- `cpf-docs/work/CPF_VERIFICATION_TOOL_INVENTORY.csv`
- `cpf-docs/work/CPF_DELETE_MANIFEST.csv`
- `cpf-docs/work/OPEN_ISSUES.md`
- `cpf-docs/work/CODEX_REVIEW_REQUEST.md`
