# Integration Request S06-INT-ADM-ROUTE-RAW-URL-OWNERSHIP

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S05`
- Priority: `P0`
- Exact path: `cpf-admin/frontend/src/app/methods/routeClosureMethods.ts;cpf-admin/frontend/src/app/methods;cpf-admin/frontend/src/generated`
- Related IDs: `STAB-001;STAB-002`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/STAB002_INTEGRATION_PATCH.diff`

## Required implementation and acceptance
Apply owner-canonical generated-client actions, remove raw URL duplicate methods, preserve detail/delete/create/update consumers and execute route/source/browser regression.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
