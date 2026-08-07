# Codex Independent Review Request

Review this R6J development overlay independently against master `cd5baccb02245a980e5998aa0dc9bac579fc019f`. Do not inherit developer PASS.

Priority review targets:
- 34 direct rework requirements and all linked 56 Findings / 93 central rows.
- Release workflow canonical URL/preflight and mandatory gate behavior.
- Frontend risky-action generated-contract consumer and permission-bypass mutation detection.
- Approval owner UNKNOWN observation reconcile; prove reconcile never replays mutation.
- EDU 135: total 135, executable 122, ADM architecture 9 PRODUCT_ADM / 4 EXTENSION_SAMPLE / 4 MERGE_EDU.
- PROCESS env allowlist/stdin IPC and spoofable authority-header removal.
- DB3 V107 transaction lineage + V108 risky action grants, migration/source/install/flyway/rollback/runtime/verify parity.
- FileLog spool durability, boundedness, dedup, structured JSON replay, masking, quarantine, terminal loss diagnostics.
- Run the local gates in `TEST_AND_EVIDENCE.md`, then mandatory runtime rows when environment exists.

Do not edit QA columns. Record Codex findings in Codex-owned fields/evidence only.
