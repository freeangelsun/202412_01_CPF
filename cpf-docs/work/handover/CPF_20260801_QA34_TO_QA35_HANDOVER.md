# CPF QA34 to QA35 Handover

## Baseline

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Reviewed SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- Latest commit message: `20260801_01`
- Git writes by reviewer: none

## What should be preserved

Build canonical coordinates, pluginManagement included build, official three DB vendors, BFF fail-closed defaults, Kafka ledger-before-ACK direction, pinned network connection identity, and protected `cpf-tools/build` source.

## Immediate P0 start points

1. repair missing reclassification and evidence-closure scripts or canonicalize wrappers
2. implement verifier preflight and negative fixtures
3. remove QA33 bulk-ID evidence fan-out
4. regenerate complete ADM/BZA OpenAPI and schema v3 generated artifacts
5. create actual controller permission matrix
6. establish exact DB baseline upgrade chains
7. implement source-clean final evidence closure

## Do not do

- do not run the old QA34 Codex request as-is
- do not inherit QA34 development complete statuses
- do not modify README/guides without confirmed artifact-session ownership
- do not commit/push without explicit user approval

## Canonical next files

- `cpf-docs/work/current/CPF_20260801_QA35_FINAL_COMPLETION_DEVELOPMENT_REQUEST.md`
- `cpf-docs/work/current/CPF_20260801_QA35_SELF_DEVELOPMENT_REQUIREMENTS.md`
- `cpf-docs/quality/CPF_20260801_QA35_DEFECT_REGISTER.csv`
- `cpf-docs/quality/CPF_20260801_QA35_REQUIREMENT_MATRIX.csv`
- `cpf-docs/work/current/CPF_20260801_QA35_CODEX_FINAL_VERIFICATION_REQUEST.md`


## ADM benchmark addendum

- User reference reviewed: 44 screenshots (Batch 15, Online/Common/System/Analysis 29).
- Minimum benchmark was converted into 68 CPF capability rows.
- Current ADM inventory: 59 menu IDs and 59 route IDs.
- Strong areas to protect: Job Pack governance, Transaction Group trace, Gateway security, approval/break-glass.
- P0 gaps: silent route fallback, shared Gateway page state, generic Batch runtime views, incomplete Online definition/deployment/diagnostics, missing integrated cross-navigation and capability graph.
- Updated register counts: Defect 36, Requirement 43, Root Cause 15.
