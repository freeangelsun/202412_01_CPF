# Integration Request S06-INT-CORE-TIME-STANDARD

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S01`
- Priority: `P1`
- Exact path: `cpf-core/src/main/java/com/cpf/core/api/time;cpf-core/src/main/java/com/cpf/core/spi/time;cpf-admin/src/main/java/com/cpf/admin/opr/health`
- Related IDs: `GAP-TIME`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/ATOMIC_WORK_ITEM_EVIDENCE.jsonl#GAP-TIME`

## Required implementation and acceptance
Implement UTC/business timezone, monotonic deadline clock, skew/NTP health, serialization, lease/audit semantics and deterministic TestClock/DST regression matrix.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
