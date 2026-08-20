# CPF Developer GPT Next Work Instruction — Post DEV21 Runtime Revalidation

## Current basis

Use the **final applied DEV21 overlay snapshot** as the next execution basis. Do not inherit historical PASS or the pre-fix Gradle result as current success.

- Development baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_122758(1).zip`
- Baseline SHA-256: `f73988097aef77a1bcc795ba66394326dd5a9f875a2d1b530e2c99e315cf5ceb`
- GitHub `master` observed at development start: `9922ca8c3c7dceeb18a9b41b2b923f564bbf29de`
- Canonical Requirements: `205`
- Developer Requirement review: `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.md`
- Evidence: `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`
- Open runtime acceptance: `cpf-docs/deliverables/OPEN_ISSUES.md`

## Architecture invariants to preserve

- Business transaction Context is canonical System6; Channel remains a separate optional ingress/policy/security context.
- `instanceId` is explicit config first, otherwise runtime hostname; same `{systemCode, instanceId}` active different process fails closed.
- `operationId` is the stable business operation contract; `executionId` is a per-execution identity.
- `cpf-common` owns customer business-common Product contracts/services/SQL/tests; `cpf-starter-common` owns Runtime/AutoConfiguration composition.
- Generated Business Domain owns root source-controlled `cpf-domain.yaml`; environment DB binding is separate and official DB vendors are Oracle/PostgreSQL/MariaDB only.
- `cpf-backoffice` / MBW is the current prebuilt Business Administration Domain; retired BZA product roots must not be restored.
- ADM management APIs remain general Spring Web management paths, not Generated Business Online Transaction paths.
- Public/optional/provider boundaries must not be bypassed by direct internal leaf consumption.
- Source quality includes ownership/package/naming/discoverability/maintainability/operability and IDE/static-warning hygiene, not only functional execution.

## Next execution order

1. Apply the final overlay and `DELETE_MANIFEST.csv` to the user's current Git Working Tree; verify protected active delete `0`.
2. Execute the Java25 full Gradle command from `CPF_CURRENT_WORK_REQUEST.md` using `Tee-Object`.
3. If it fails, ingest the single `gradle-problems.txt`, group all failures by Root Cause, and redevelop the same current source without resurrecting retired architecture.
4. If Java25 passes, execute DB3 live lifecycle, Multi-instance/recovery, Public Binary consumer, Windows PowerShell, and Browser E2E acceptance.
5. Runtime not executed stays `미검증`; do not substitute static PASS.
6. QA/Codex role columns remain owned by QA/Codex. Developer GPT updates only Developer-owned status/evidence unless QA explicitly reopens the requirement.
7. Do not commit/push/branch/tag/reset/restore/stash/clean or physically delete additional product files without explicit user approval. Use root-relative Delete Manifest for any new stale candidate.

## Failure handoff

For the Java25 full build, the only file normally required from the user is:
`%USERPROFILE%\Downloads\gradle-problems.txt`.
