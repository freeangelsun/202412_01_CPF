# Integration Request S06-INT-WEBHOOK-CALLBACK-LIFECYCLE

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S03`
- Priority: `P1`
- Exact path: `cpf-core/src/main/java/com/cpf/core/api/webhook;cpf-starters/integration/webhook;cpf-tools/generator;cpf-admin/src/main/java/com/cpf/admin/opr`
- Related IDs: `GAP-WEBHOOK`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/ATOMIC_WORK_ITEM_EVIDENCE.jsonl#GAP-WEBHOOK`

## Required implementation and acceptance
Implement signed callback contract, replay/SSRF protection, endpoint validation, ordered retry/DLQ delivery ledger, reconciliation, generated customer adapter and operational consumer.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
