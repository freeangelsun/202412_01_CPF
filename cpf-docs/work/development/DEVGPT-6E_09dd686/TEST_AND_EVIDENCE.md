# DEVGPT-6E Test and Evidence

Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`

## Executed verification

| Gate | Exit | Result | Evidence |
|---|---:|---|---|
| Python DB regression | 0 | 126 tests, OK | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/all-db-development-tests-09dd686.log` |
| DB development static contract | 0 | PASS, 17 PowerShell files, 9 lifecycle stages | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/db-development-static-contract-09dd686.json` |
| DB lifecycle contract | 0 | PASS | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/db-lifecycle-contract-09dd686.log` |
| Schema governance | 0 | PASS, 201 tables, 147 foreign keys | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/db-schema-governance-09dd686.json` |
| Vendor manifest | 0 | PASS, 3 vendors, 36 paths | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/vendor-manifest-09dd686.json` |
| Vendor semantic parity | 0 | PASS, findings 0 | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/vendor-semantic-parity-09dd686.json` |
| Vendor token parity | 0 | PASS, V83 and V86-V91 across 3 vendors | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/vendor-static-token-parity-09dd686.json` |
| Seed dialect | 0 | PASS | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/vendor-seed-dialect-09dd686.json` |
| OpenAPI regeneration | 0 | PASS | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/openapi-regeneration-09dd686.log` |
| OpenAPI reproducibility | 0 | byte-identical | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/openapi-reproducibility-09dd686.log` |
| Python compile | 0 | PASS | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/python-compileall-09dd686.log` |
| JSON/CSV parse | 0 | PASS | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/json-csv-parse-09dd686.log` |
| Source hygiene/secret patterns | 0 | findings 0 | `cpf-docs/evidence/development/DEVGPT-6E_09dd686/source-hygiene-secret-scan-09dd686.log` |

## Scope verification

- V8 Work Item index and both DB ledgers reconcile to 60 in-scope Work Items and 33 excluded `cpf-common` Work Items.
- 1,044 CPF-FR and 1,449 CPF-SC are individually reviewed; representative-ID blanket PASS was not used.
- Missing, duplicate, unassigned, unreviewed and Evidence-missing counts are zero.

## Target runtime boundary

Java 21 was observed, but Java 25, PowerShell, SQLPlus, PostgreSQL/MariaDB clients, Docker and the three native DB servers are unavailable. Native install→upgrade→rollback→reapply, backup→restore→PITR, process-kill/multi-instance, browser and representative-scale performance validation were not executed and are not recorded as PASS.
