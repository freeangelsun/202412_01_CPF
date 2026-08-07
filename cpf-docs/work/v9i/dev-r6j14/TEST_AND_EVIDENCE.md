# CPF R6J DEV14 Test and Evidence

- result baseline SHA: `cd5baccb02245a980e5998aa0dc9bac579fc019f`
- current product-source delta after 3ed676: `07_03`, `07_04` are documentation/QA instruction/package metadata only; R6J overlay is rebound to `cd5baccb02245a980e5998aa0dc9bac579fc019f`.
- local environment: Java 21.0.10, Node 22.16.0, npm 10.9.2, Python 3.13.5. `pwsh`, standalone `gradle`, Java 25 and live external services are unavailable.

## Executed gates

| Gate | Result | Evidence |
|---|---|---|
| Overlay hygiene / JSON / YAML / Python / high-confidence secret patterns / protected-path / path length | PASS | `evidence/01_hygiene.txt` |
| Node syntax | PASS | `evidence/02_node_check.txt` |
| R6J rework contract + real mutation self-test | PASS | `evidence/03_r6j_rework.txt` |
| Transaction DB3 static parity + mutation | PASS | `evidence/04_transaction_db3.txt` |
| Approval 422 OpenAPI parity | PASS, 18 approval mutations | `evidence/05_approval_422.txt` |
| Existing R6 behavior regression | PASS, 43 checks + 17 mutations | `evidence/06_behavior_regression.txt` |
| FileLog recovery Java syntax/type with minimal contract stubs | PASS | `evidence/08_javac_recovery_spool.txt` |

The first isolated `javac` attempt without Spring/project classpath failed only because `Environment` and `SensitiveDataMasker` were absent; the contract-stub compile returned 0. Full project compilation was not claimed.

## Final rerun

Final package-content rerun also passed:
- `evidence/09_final_hygiene.txt`
- `evidence/10_final_r6j_rework.txt`
- `evidence/11_final_transaction_db3.txt`
- `evidence/12_final_approval_422.txt`
- `evidence/13_final_behavior_regression.txt`
- `evidence/14_final_gate_summary.txt`

## Not executed / not claimed PASS

All 13 rows in `RUNTIME_QUALIFICATION_MATRIX.csv` remain `미검증`: Java25+Gradle9.1 clean build/publication, DB3 live lifecycle, ADM/BZA authenticated Chromium/Firefox/WebKit, approval process-kill/UNKNOWN, broker/network/DB-finalization faults, performance/resource, security negative corpus, DR, generator lifecycle, full ADM operation runtime closure, Codex review, transaction/logging end-to-end lineage.

## Judgment

Locally verifiable R6J Source/Contract/Gate work has zero remaining gate failures. This does **not** declare CPF GA/QA 100% complete because mandatory external Runtime/Codex/QA qualification remains unexecuted.
