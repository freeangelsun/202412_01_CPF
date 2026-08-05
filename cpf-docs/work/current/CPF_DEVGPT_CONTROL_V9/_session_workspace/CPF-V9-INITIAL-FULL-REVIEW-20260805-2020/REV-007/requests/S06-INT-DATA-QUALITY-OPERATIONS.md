# Integration Request S06-INT-DATA-QUALITY-OPERATIONS

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S04`
- Priority: `P1`
- Exact path: `cpf-core/src/main/java/com/cpf/core/api/data;cpf-common/src/main/java/com/cpf/common;cpf-admin/src/main/java/com/cpf/admin/opr;cpf-tools/db/vendor/oracle;cpf-tools/db/vendor/postgresql;cpf-tools/db/vendor/mariadb`
- Related IDs: `GAP-DATA-QUALITY`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/ATOMIC_WORK_ITEM_EVIDENCE.jsonl#GAP-DATA-QUALITY`

## Required implementation and acceptance
Implement validation rule lifecycle, quarantine ledger, correction approval/reason/audit, replay/reconcile and ADM operational consumer across all three vendors.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
