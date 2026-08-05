# Integration Request S06-INT-ADM-BROWSER-RELEASE-CONTRACT

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S05`
- Priority: `P0`
- Exact path: `cpf-admin/frontend/playwright.config.ts`
- Related IDs: `GATE-14-FRONTEND;STAB-027;GAP-SESSION-BFF`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Reproduction/Evidence: `evidence/BROWSER_CONTRACT_FAIL.log`

## Required implementation and acceptance
ADM must consume CPF_FRONTEND_URL and canonical CPF_E2E_* release inputs like BZA, then execute Chromium/Firefox/WebKit regression.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
