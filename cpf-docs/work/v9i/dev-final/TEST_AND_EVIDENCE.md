# Test and Evidence

## Provenance
- Target basis: `08d8beb4a664039904c30aeac07115a04707924a` (`master`)
- Prior development SHA is provenance only and is not promoted as current PASS.
- Central currentization changed Governance/Control surfaces but no Product Source files.

## Executed in this environment
A consolidated final-target rerun produced **21 PASS / 0 FAIL** with explicit Exit Code 0 for: canonical 169+8, Core persistence boundary, TransactionId trust/E2E, FileLog recovery, DB3 lineage, DB3 runner, BZA OpenAPI, BZA Simulation, ADM CSP, ADM high-risk client, ADM Reliability CAS, transaction freshness applicability, EDU ADM architecture, Observability, Security Negative, Resource, Batch, Broker, and DR semantic/mutation gates.

Additional gates: `verify-adm-batch-runtime-approval.py --self-test` PASS Exit 0; `verify-adm-incident-lifecycle.py --self-test` PASS Exit 0. ADM operation consumer current changed-scope gate passed `operations=332 consumed=267 waived=0`; a generated→raw HIGH mutation failed Exit 1 as required and restore returned Exit 0.

Static format/hygiene: Python AST 23 / JSON 5 / MJS node-check 2, errors 0. Deleted-currentization path references (`V7/V9`, QA38/39 old, requirement-rebase, REV004) in overlay: 0.

## Environment and not-executed live qualification
- Java: OpenJDK 21.0.11 (required release toolchain Java 25)
- Gradle CLI: unavailable; clean exact-SHA repository checkout unavailable in container
- Node: 22.16.0; npm 10.9.2; Python 3.13.5; Git 2.47.3
- PowerShell: unavailable
- Docker: unavailable
- Oracle/PostgreSQL/MariaDB live endpoints/credentials: unavailable
- ADM/BZA deployed browser/auth targets: unavailable
- 2+ instance/process-kill/broker/network/DR target: unavailable

These live axes are recorded as `미검증`, not PASS. Commands, rerun conditions, expected results and failure criteria are in `RUNTIME_QUALIFICATION_MATRIX.csv`.

## Exact-SHA clean release rerun
On a clean checkout of `08d8beb4a664039904c30aeac07115a04707924a`, run the current `cpf-tools/verification/final-dev/run-r6-release-gates.ps1` with the required exact-head and live inputs. The runner no longer references deleted QA38/QA39/REV004 historical gates and includes the current TXID/ADM/EDU gates.
