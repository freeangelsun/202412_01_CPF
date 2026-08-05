# OPEN ISSUES — CHECKPOINT, NOT FINAL

- Integration-owner requests remain in `results/INTEGRATION_REQUEST_STATUS.csv`; Cache is resolved in the S06 overlay.
- User approval is required before deleting `cpf-starters/openapi-webmvc`; no deletion performed.
- Authoritative Java 25/Gradle 9.1, three DB vendors, IdP, browser engines, signed release and QA environments remain unavailable.
- Atomic IDs affected by these blockers remain `미검증` or `실패`; no false PASS is recorded.


## R5 frozen-baseline update
- Baseline `af12a0c8851a2e8d20e9e42964d8dacc0266af03`.
- Telemetry and Notification/Incident tests: 13 PASS.
- Notification/Incident three-vendor canonical fresh-install parity: baseline FAIL; S04 proposed patch probe PASS.
- Regression aggregate: 29 files / 135 tests / 0 failures.
- Package remains checkpoint (`final_completion=false`).
