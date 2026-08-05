# Integration Request S06-DELETE-OPENAPI-WEBMVC-STALE-ROOT

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S06`
- Priority: `P1`
- Exact path: `cpf-starters/openapi-webmvc`
- Related IDs: `STAB-028;CPF-WP-ARCH-STARTER-03-VERIFICATION_EVIDENCE;CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE`
- Current status: `PENDING_USER_DELETE_APPROVAL`
- Reproduction/Evidence: `evidence/STARTER_CATALOG_TRUTH_LIMITED.log`

## Required implementation and acceptance
Catalog removed root remains physically present; project rules prohibit deletion without user approval.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
