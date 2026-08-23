# CPF Developer GPT Next Work Instruction — Post DEV21 Runtime Revalidation

## Current basis

Use the current VS Code **local Working Tree** as the only Primary Source. Do not reapply any baseline/overlay/ZIP and do not inherit historical PASS or the pre-fix Gradle result as current success.

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
- Generated Business Domain owns root source-controlled `gradle.properties` `cpf.domain.*` Developer Contract; `cpf-domain.yaml`, `cpf-generator.lock.json`, renamed/hidden Generator bookkeeping are forbidden in Generated Roots and release output. Environment DB binding is separate and official DB vendors are Oracle/PostgreSQL/MariaDB only.
- `cpf-backoffice` / MBW is the current prebuilt Business Administration Domain; retired BZA product roots must not be restored.
- ADM management APIs remain general Spring Web management paths, not Generated Business Online Transaction paths.
- Public/optional/provider boundaries must not be bypassed by direct internal leaf consumption.
- Source quality includes ownership/package/naming/discoverability/maintainability/operability and IDE/static-warning hygiene, not only functional execution.

## Next execution order

1. Finish the in-progress Generated Domain exact MBR/EXS verification and Java25 compile/test before downstream publication work.
2. Publish Public Binary, create a clean Open Git release, and execute every public command from an isolated Fresh workspace without Private Source or Maven Local false green.
3. Fresh-generate domains and fresh-build the authored EDU, ADM UI/backend, and `cpf-backoffice`/MBW projections; do not treat Backoffice as Generator output.
4. Execute ADM Browser/menu/generated-client/permission/error/responsive/accessibility/screenshots and Starter/Common actual-consumer runtime gates.
5. Trace the current Backoffice UI/Channel/BFF/Gateway optional topology, then run used/unused MBW plus Header6→Domain Invocation→Owner DB E2E without restoring retired `cpf-biz-admin`.
6. Execute Java25 Root, DB3 live lifecycle, Multi-instance/recovery, Windows PowerShell and root no-garbage-regeneration Final Gates.
7. Runtime not executed stays `미검증`; do not substitute static PASS. QA/Codex role columns remain owned by QA/Codex.
8. Do not commit/push/branch/tag/reset/restore/stash/clean. Additional physical deletions require the applicable exact manifest/approval policy.

## Failure handoff

For the Java25 full build, the only file normally required from the user is:
`%USERPROFILE%\Downloads\gradle-problems.txt`.
