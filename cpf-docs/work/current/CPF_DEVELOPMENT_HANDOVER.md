# CPF Development Handover

## Baseline
- Baseline source ZIP SHA-256: `a62e1abfa134d3124f2ab6743821610fa225ed5cc3e8c21e201e7a20785a25f4`
- Baseline source entries: `8,383`
- Exact Git SHA: unavailable because the supplied source ZIP had no `.git`.
- Do not inherit a historical Git SHA as evidence.

## Current development state
- Development: source/static implementation complete for this pass.
- Verification: runtime revalidation required for Java25/live DB3/Multi-WAS/process-kill/final browser E2E.
- Git write operations: none performed.
- No commit/push/branch/reset/restore/stash/clean was performed.

## Mandatory continuation rules
1. Development GPT owns implementation, review, defect repair and revalidation for every defect found in the current working source, whether pre-existing or introduced during the current change.
2. New QA/Steering/logs must first be merged by root cause/owner/dependency order; do not append duplicate V2/V3 APIs or engines.
3. Latest approved development Steering currentizes stale canonical documents; do not revert Channel vocabulary to stale System headers.
4. Final verifier must validate both Git Working Tree and ZIP/fallback modes. `git ls-files -z` must be parsed with actual NUL `b'\0'`, never escaped backslash-zero.
5. Subject Tracking public metadata is minimal/optional. Existing trusted Security/Identity sources have priority; claimed metadata never silently overrides verified identity.
6. Managed Server, Runtime Instance, `instanceId`, `systemCode`, Channel identity remain distinct concepts. Feature menus consume the central Runtime Inventory rather than owning duplicate server masters.
7. Logging/Retention completion means actual execution, not saved configuration. Scheduled/Manual/Pause/Resume must share the same engine and produce run history.
8. `WORK_RESULT_REVIEW` is **not generated automatically**. After all development/validation/packaging is complete, create it only when the user explicitly asks. When requested, map every development requirement, QA finding and Steering item to implementation, path, consumer, validation, remaining condition and QA recheck point without omissions.

## Next action
Run the canonical user-local Java25 final validation against the latest Working Tree. If it fails, repair the same Requirement by root cause and rerun. Do not mark an unexecuted runtime item PASS.

## Canonical evidence
- `cpf-docs/work/TEST_AND_EVIDENCE.md`
- `cpf-docs/work/REQUIREMENT_STATUS.csv`
- `cpf-docs/work/CHANGE_MANIFEST.csv`
- `cpf-docs/work/OPEN_ISSUES.md`
- `cpf-docs/work/PACKAGE_MANIFEST.json`
- `cpf-docs/work/current/CODEX_REVALIDATION_REQUEST.md`
