# TEST AND EVIDENCE — REV-007 CHECKPOINT

## Baseline
- Frozen origin/master: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Later Git changes were ignored per user instruction.
- No commit, push, branch, tag, PR, reset, restore, stash, clean or deletion.

## Assignment equations
- Entity 221 = judged 221
- CPF-FR 8,158 = unique judged 8,158
- CPF-SC 9,799 = unique judged 9,799
- Engineering Gate 21 = judged 21
- Unreviewed, missing, duplicate Primary, unowned, evidence-missing and consumer-unconfirmed = 0

## FULL_ASSIGNMENT
- Canonical 169 / Work Packages 775 / CPF-FR 30,558 / CPF-SC 40,763
- Unassigned, duplicate, orphan, unknown owner and dependency cycle = 0

## S06 regression
- Command: file-by-file `python -m pytest -q` over 27 S06 owned/affected files
- Exit Code: 0 for every file
- Actual result: 27/27 files PASS, 122/122 tests PASS
- Evidence: `evidence/S06_FINAL27_REGRESSION_SUMMARY.json`

## Cache Capability
- Direct Java Runtime: local cache, Valkey cache/lock, Feature Flag = PASS
- Durable recovery: append-before-apply, checkpoint-after-success, process-kill replay, fast-signal loss recovery = PASS
- Idempotency: duplicate eventKey with different payload fails closed
- Official DB lifecycle: Oracle/PostgreSQL/MariaDB source/install/migration/rollback/runtime/verify and pack discovery = PASS
- Consumer: ADM → Coordinator → JDBC ledger → Caffeine/Valkey → checkpoint/reconcile
- Evidence: `evidence/CACHE_FEATURE_RUNTIME_R3.json`, `evidence/CACHE_DURABLE_LIFECYCLE.json`

## Not executed as PASS
- Java 25/Gradle 9.1 full Root build/publication/SBOM
- Live Oracle/PostgreSQL/MariaDB servers
- Target IdP/browser/signed release environment

These remain `미검증`; no false PASS is recorded.


## R5 frozen-baseline update
- Baseline `af12a0c8851a2e8d20e9e42964d8dacc0266af03`.
- Telemetry and Notification/Incident tests: 13 PASS.
- Notification/Incident three-vendor canonical fresh-install parity: baseline FAIL; S04 proposed patch probe PASS.
- Regression aggregate: 29 files / 135 tests / 0 failures.
- Package remains checkpoint (`final_completion=false`).

- R6 independent package validation: 70/70 PASS, Exit 0 (`evidence/PACKAGE_R6_INDEPENDENT_VALIDATION.json`).
