# DEVGPT-6B Test and Evidence

## Baseline and environment

- Fixed baseline SHA: `faedf43a7baffdad456bf40f8e46d622db9cfc76`
- Requested target runtime: Java 25
- Executed substitute runtime: OpenJDK 21.0.10
- Newer `origin/master` commits: intentionally ignored by user instruction
- Full Gradle/Java 25/real Oracle·PostgreSQL·MariaDB/Browser runtime: **not executed and not PASS**

## Scope accounting

- Work Items: 43
- Canonical Requirements: 9
- CPF-FR / CPF-SC: 1,109 / 1,698
- Engineering Gates: 16
- Unreviewed Requirement / Scenario: 0 / 0

## Executed commands and results

1. `bash /mnt/data/run_devgpt6b_final_suite_head38.sh` — Exit Code 0, `DEVGPT6B_FINAL_HEAD38_GATES_PASS`
2. `bash /mnt/data/run_devgpt6b_final_suite_tail10.sh` — Exit Code 0, `DEVGPT6B_FINAL_TAIL10_GATES_PASS`
3. `bash /mnt/data/run_devgpt6b_extension_gates.sh` — Exit Code 0, `DEVGT6B_EXTENSION_GATES_PASS`
4. `bash /mnt/data/run_devgpt6b_masking_policy_gates.sh` — Exit Code 0, `DEVGPT6B_MASKING_POLICY_GATES_PASS`
5. `bash /mnt/data/run_devgpt6b_logpolicy_version_gates.sh` — Exit Code 0, `DEVGPT6B_LOG_POLICY_VERSION_GATES_PASS`
6. `bash /mnt/data/run_devgpt6b_jdbc_logpolicy_gates.sh` — Exit Code 0, `DEVGPT6B_JDBC_LOG_POLICY_GATES_PASS`
7. `python /mnt/data/run_devgpt6b_hygiene.py` — Exit Code 0, `DEVGPT6B_HYGIENE_SECRET_SCAN_PASS`

Evidence is under `cpf-docs/evidence/development/DEVGPT-6B_faedf43/logs-reproducible/`. Validation scripts are preserved under `validation-scripts/` as session evidence; they are not product runtime dependencies.

## Consolidated result

- Unique product PASS markers: **63**
- Suite/Hygiene PASS markers: **4**
- Mapped Gate inventory rows: **65**
- Gate rows without PASS marker: **0**
- Hygiene: trailing whitespace 0, merge conflict 0, NUL 0, secret candidate 0, forbidden vendor 0, protected path 0

## Requirement adjudication

- Requirement status: `{'DIRECT_ALTERNATIVE_VERIFIED': 812, 'NOT_APPLICABLE_WITH_RATIONALE': 47, 'DIRECT_SOURCE_SUBSTITUTE_VERIFIED': 17, 'PARTIAL_IMPLEMENTATION': 99, 'CROSS_SESSION_REQUIRED': 134}`
- Scenario status: `{'ALTERNATIVE_VERIFIED': 1380, 'CROSS_SESSION_REQUIRED': 155, 'NOT_APPLICABLE_WITH_RATIONALE': 47, 'DIRECT_SOURCE_SUBSTITUTE_VERIFIED': 17, 'PARTIAL_IMPLEMENTATION': 99}`
- `DIRECT_SOURCE_SUBSTITUTE_VERIFIED` retains Java 25 or real multi-instance runtime as unexecuted.
- `PARTIAL_IMPLEMENTATION` and `CROSS_SESSION_REQUIRED` rows are not counted as complete and contain exact owner-session IDs.

## Runtime differences

- Java 25 repository Gradle configuration/build/test/publication: not executed.
- Real Oracle/PostgreSQL/MariaDB lifecycle: DEVGPT-6E.
- ADM/OpenAPI/Frontend/BZA and Browser Zeroization: DEVGPT-6A.
- Message Span: DEVGPT-6D; Batch Span: DEVGPT-BATCH.
- Generator/Sample/EDU/JavaDoc/Guide/BOM/catalog: DEVGPT-6F.
