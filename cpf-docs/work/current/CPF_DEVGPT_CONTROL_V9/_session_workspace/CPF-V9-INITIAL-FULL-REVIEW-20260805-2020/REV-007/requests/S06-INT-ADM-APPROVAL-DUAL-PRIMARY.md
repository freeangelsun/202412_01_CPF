# Integration Request S06-INT-ADM-APPROVAL-DUAL-PRIMARY

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S05`
- Priority: `P0`
- Exact path: `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java;cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmApprovalController.java`
- Related IDs: `GATE-02-CONSUMER;GATE-03-HTTP-API;GAP-OPENAPI-PROFILE;STAB-028`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/SPRING_MAPPING_DUPLICATE.json`

## Required implementation and acceptance
Seven duplicate Spring mappings and OpenAPI operationIds; one canonical Approval controller must remain and consumers/tests/OpenAPI must be regressed.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
