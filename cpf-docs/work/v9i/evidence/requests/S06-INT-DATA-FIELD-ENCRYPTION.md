# Integration Request S06-INT-DATA-FIELD-ENCRYPTION

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S04`
- Priority: `P0`
- Exact path: `cpf-core/src/main/java/com/cpf/core/api/data;cpf-common/src/main/java/com/cpf/common;cpf-tools/db/vendor/oracle;cpf-tools/db/vendor/postgresql;cpf-tools/db/vendor/mariadb`
- Related IDs: `GAP-DATA-ENCRYPTION`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/ATOMIC_WORK_ITEM_EVIDENCE.jsonl#GAP-DATA-ENCRYPTION`

## Required implementation and acceptance
Implement field/file classification, tokenization/searchability, key version/rekey, three-vendor migration/rollback and actual application/query consumers with masking and audit tests.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
