# Integration Request S06-INT-CORE-CRYPTO-AGILITY

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S01`
- Priority: `P0`
- Exact path: `cpf-core/src/main/java/com/cpf/core/api/security;cpf-core/src/main/java/com/cpf/core/spi;cpf-starters/security;cpf-tools/release`
- Related IDs: `GAP-CRYPTO-AGILITY`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/ATOMIC_WORK_ITEM_EVIDENCE.jsonl#GAP-CRYPTO-AGILITY`

## Required implementation and acceptance
Add algorithm/provider allowlist, envelope encryption, key-version/rekey, deprecated algorithm rejection, PQC readiness and release CBOM consumer/tests without placing provider runtime in cpf-core.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
