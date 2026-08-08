# CPF Session 18 Review Index

## Entry point

This directory is the current Development/Verification handoff for the NXT Core Slimming / Unified Utility / Modern Capability closure based on `master@4c4248a12e699c07f9f5fb11fbb33b97ca04077d`.

## Status and evidence

- `REQUIREMENT_STATUS.csv` — exact ID current development/verification state.
- `CPF_REQUIREMENT_MATRIX.csv` — current requirement matrix baseline.
- `CPF_SCENARIO_MATRIX.csv` — current scenario matrix baseline.
- `CPF_FUNDAMENTAL_BASELINE_AUDIT.csv` — Session 18 fundamental re-audit.
- `CPF_CORE_SLIMMING_AUDIT.csv` — generated architecture classification evidence for the available Overlay; rerun gate after apply/delete for full repository.
- `TEST_AND_EVIDENCE.md` — executed commands and actual results.
- `RUNTIME_ONLY_VERIFICATION.csv` — not-executed environment/runtime checks with rerun/pass/fail conditions.
- `OPEN_ISSUES.md` — remaining authority/environment conditions.

## Change / safety / package

- `CPF_CHANGE_MANIFEST.csv` — changed/added and delete-pending paths.
- `CPF_DELETE_MANIFEST.csv` — exact delete allowlist; Session 18 relocation rows require explicit user approval.
- `CPF_APPLY_ONE_LINE.ps1.txt` — apply-only one-line helper; no deletion.
- `CPF_DELETE_ONE_LINE.ps1.txt` — explicit delete helper invocation; do not run without user approval.
- `CPF_VERIFY_ONE_LINE.ps1.txt` — low-cost architecture/static/diff verification.
- `CPF_VERIFICATION_TOOL_INVENTORY.csv` — tool role/consumer/currentization inventory.
- `CPF_PACKAGE_MANIFEST.json` — package metadata and file hashes.
- `CPF_FILES.sha256` — root-relative SHA-256 list.

## Handoff

- `HANDOVER.md` — apply/verification/QA transition.
- `CODEX_REVIEW_REQUEST.md` — independent Codex review scope.
- `QA_REWORK_REQUEST.md` — QA verification request without self-certifying QA.
