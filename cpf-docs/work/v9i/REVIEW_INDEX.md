# CPF Review Index

## Current QA Baseline

- Repository/branch: `freeangelsun/202412_01_CPF` / `master`
- REV-004 instruction basis: `ee977cf66c251081df78ea5e9675b81c3dfafa59`
- Documentation delta basis: `a8be27a34bdac0b7c075e06d6e86571244c96421` (`06_08`)
- Documentation follow-up: `11093fe26b4e94d9066f2d9edcc1d06c879d868e` (`06_09`)
- Current QA exact master: `e7cc9ada86c871214a20862779f2433bc46fea1b` (`06_10`)
- QA result: **FAILED / REWORK REQUIRED**
- Target runtime verification: **PENDING / NOT PASSED**

## Canonical Dataset

- Exact Requirement dataset: `results/REQUIREMENT_STATUS_INDEX.csv` + status part files
- Canonical exact ID count preserved by prior package: `47,745`
- Integration request count preserved by prior package: `32`

## REV-004 QA R5I

- Path: `qa/r5i`
- Report: `qa/r5i/QA_REPORT_R5I.md`
- Findings: `qa/r5i/QA_FINDINGS.csv`
- Requirement status: `qa/r5i/QA_REQUIREMENT_STATUS.csv`
- Rework: `qa/r5i/QA_REWORK_REQUEST.md`
- Result: FDEV-001~FDEV-025 all `미통과`
- Counts: P0 12 / P1 13 / P2 4
- Inputs: self QA report + peer QA R5 report

## Review Order

1. `qa/QA_LATEST.md`
2. `qa/r5i/QA_REPORT_R5I.md`
3. `qa/r5i/QA_FINDINGS.csv`
4. `qa/r5i/QA_REQUIREMENT_STATUS.csv`
5. `qa/r5i/QA_REWORK_REQUEST.md`
6. `qa/r5i/QA_TEST_EXECUTION_LEDGER.csv`
7. `qa/r5i/QA_SOURCE_VALIDATION_INDEX.csv`
8. `results/REVIEW_FINDINGS.csv`

## Delete Safety

- QA-approved deletion: 0
- `cpf-document-quality-r9.svg`: restore or exact-path explicit approval required
- `cpf-starters/openapi-webmvc`: no delete pending explicit approval
- QA performed no commit, push, branch, history rewrite, reset, restore, stash, clean, or deletion.
