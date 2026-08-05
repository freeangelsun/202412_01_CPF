# TEST AND EVIDENCE — DEVGPT-6C V8 Checkpoint

## Baseline and environment

- Exact baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- Latest branch observed: `master`
- Java: OpenJDK/Javac 21.0.10
- Java 25: unavailable
- Direct clone/clean snapshot: unavailable because container DNS could not resolve GitHub; exact-SHA files were read through GitHub Connector.
- Formal target environment: Java25, Gradle full repository, Oracle/PostgreSQL/MariaDB, Browser, multi-JVM.

## Scope integrity

- Work Item 14, Canonical Requirement 2, CPF-FR 338, CPF-SC 950, Gate 16.
- Duplicate, unassigned, missing Requirement result, missing Scenario result: 0.
- Capability consumer result: actual applier 2, generic primitive only 4, no actual applier 48.
- Evidence: `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_SCOPE_VALIDATION.json`.

## Executed validation matrix

| Validation | Command in session environment | Exit | Actual result | Classification | Evidence |
|---|---|---:|---|---|---|
| Main source compile | `python /mnt/data/build_dev6c_stub_compile_v2.py` | 0 | 97 files, errors 0, warnings 0 | Java21 Substitute Compile | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_MAIN_COMPILE.txt` |
| Test source compile | `python /mnt/data/compile_dev6c_tests_stub.py` | 0 | errors 0; 16 warnings limited to Mockito stub generic varargs | Java21 Substitute Compile | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_TEST_COMPILE.txt` |
| Feature Flag compile | `python /mnt/data/build_dev6c_feature_compile.py` | 0 | 44 files, errors 0, warnings 0 | Java21 Substitute Compile | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_FEATURE_COMPILE.txt` |
| Contract harness | `javac/java Dev6cContractHarness` | 0 | ACK attempt, ApplyGuard, Inbox, DTO invariants PASS | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_CONTRACT_HARNESS.txt` |
| State transition harness | `java RuntimeStateTransitionHarness` | 0 | 45 checks PASS | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_STATE_TRANSITION_HARNESS.txt` |
| AutoConfiguration harness | `java RuntimeAutoConfigurationHarness` | 0 | local/remote topology PASS | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_AUTOCONFIG_HARNESS.txt` |
| ACK response-loss harness | `java AgentAckRetryHarness` | 0 | applies=1, acks=3, blocked/recovered claim PASS | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_AGENT_ACK_HARNESS.txt` |
| Feature Flag applier harness | `python /mnt/data/run_dev6c_feature_harness.py` | 0 | apply/replay/invalid/UNKNOWN PASS | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_FEATURE_FLAG_HARNESS.txt` |
| Process-kill recovery | `python /mnt/data/run_dev6c_process_kill_harness.py` | 0 | exit 23, PREPARED→APPLIED, ACK cleanup EMPTY | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_PROCESS_KILL_HARNESS.txt` |
| Fencing/reclaim | rebuilt `AgentFenceRecoveryHarness` | 0 | stale=7 rejected, deferred active lease, reclaimed=9 | Substitute Runtime | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_FENCE_RECOVERY_HARNESS.txt` |
| Vendor forward parity | `python validate_dev6c_vendor_parity.py` | 0 | Oracle/PostgreSQL/MariaDB V64 parity findings 0 | Static/Semantic | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_VENDOR_FORWARD_PARITY.txt` |
| SQL schema/reference | `python validate_dev6c_sql_schema.py` | 0 | 75 SQL statements; unknown table 0 | Static/Semantic | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_SQL_SCHEMA.txt` |
| SQL state contracts | `python check_dev6c_sql_contracts.py` | 0 | 14 checked, findings 0 | Static/Semantic | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_SQL_CONTRACTS.txt` |
| JDBC placeholders | `python check_jdbc_placeholders.py` | 0 | 143 calls, findings 0 | Static/Semantic | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_JDBC_PLACEHOLDERS.txt` |
| OpenAPI negative gate | Node/Python snapshot inspection | 1 expected defect | requestBody 7, query 5, raw URL 10 | Negative Defect Evidence | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_OPENAPI_NEGATIVE_GATE.json` |
| Assignment-aware hygiene | V8 package validator | 0 | UTF-8/NUL/trailing/secret/protected/collision findings 0 | Static | `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_HYGIENE_VALIDATION.json` |

## DB lifecycle result

- Forward migration: Oracle/PostgreSQL/MariaDB V64 static parity PASS.
- Runtime tables: 11; repository SQL unknown tables: 0.
- MariaDB `R64__runtime_control_plane.sql`: exists, 11 tables and dependency order PASS.
- PostgreSQL R64: missing.
- Oracle R64: missing.
- Actual database execution: not executed.
- Evidence: `cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001/evidence/V8_DB_STATIC_VALIDATION.json`.

## Known failing or unverified acceptance

1. 52 of 54 capability Requirements lack an actual capability-owned runtime applier or have only generic primitives.
2. Runtime Control OpenAPI/Generated Client/Raw URL consumer contract is not complete.
3. Runtime payload persistence is plaintext; approved encryption/key-rotation SPI is absent.
4. PostgreSQL and Oracle R64 rollback artifacts are absent.
5. Java25 Gradle full build/test/publication/binary compatibility not executed.
6. Browser E2E and server authorization negative tests not executed.
7. Two JVM + real DB multi-instance contention and all 3 DB process-kill/reclaim not executed.

READY, PLANNED and NOT_EXECUTED are not recorded as PASS.
