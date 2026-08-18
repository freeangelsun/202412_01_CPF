# CPF TEST AND EVIDENCE

- Evidence time: `2026-08-18T23:19:00+09:00`
- Baseline full-source ZIP SHA-256: `a62e1abfa134d3124f2ab6743821610fa225ed5cc3e8c21e201e7a20785a25f4`
- Baseline file count: `8,383`
- Git exact SHA: `UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT`
- Development status: **SOURCE/STATIC DEVELOPMENT COMPLETE**
- Verification status: **RUNTIME REVERIFY REQUIRED**

## 1. Final development scope closed

This development pass currentized and revalidated the integrated Channel/Header/Context, Same-JVM/Remote invocation boundary, DB3, Subject Tracking, Central Managed Server Registry, File/DB Retention execution, ADM/OpenAPI/Frontend, Generator/Generated Domain, EDU, stale verifier/harness, and repository hygiene surfaces.

The current development source intentionally does **not** claim QA final completion for runtime checks that were not executed on the latest modified source.

## 2. Latest-source PASS evidence

| Gate | Result | Evidence |
|---|---|---|
| Final static verifier | PASS | online=20, batch=15, operationPairs=27, uniqueOperationIds=27, failures=0 |
| Subject Tracking closure | PASS | types=4, DB3=3, rawPersistence=0, ADM consumer=1 |
| Central Registry / Retention closure | PASS | centralRegistryUiConsumers=6, canonicalTables=233 |
| DB3 semantic parity | PASS | MariaDB/PostgreSQL/Oracle, canonicalTableCount=233, findings=0 |
| Java source syntax | PASS | 2,745 source files, errors=0 on final snapshot run |
| Repository hygiene | PASS | protectedDelete=0, directoryDelete=0 |
| Frontend TypeScript | PASS before final packaging | vue-tsc --noEmit exit code 0 after final ADM UI patch; a later duplicate rerun exceeded this environment execution window and is not substituted for that PASS |
| Generated client | PASS | 337 operations; generated/request boundary contract passed |
| Frontend consumer closure | PASS | 337/337 consumers |
| Verification tests | PASS | 17/17 test files exit code 0 |
| DB verification tests | PASS | 15/15 test files exit code 0 |
| Runtime tool tests | PASS | 17/17 test files exit code 0 |
| Generator verification tests | PASS | 11/11 test files exit code 0 |
| Testing-tools regression | PASS | 83/83 test files, actual FAIL 0; one parallel resource-contention flake was rerun standalone and passed 7/7 |
| Changed-impact NXT3 gates | PASS | 11/11 |
| Cache standalone compile closure | PASS | main=29, test=11, warnings=0 |

## 3. Important defect closures

- Final verifier `git ls-files -z` now parses actual NUL (`b'\0'`) and retains ZIP/fallback mode; wrong escaped-NUL parsing is fail-closed by regression checks.
- Same-JVM domain invocation no longer bypasses the transport-independent operation access policy boundary.
- External Business Domain contract is five required wire headers; `X-Current-Channel` remains receiver-owned and non-required.
- Channel identity validation is 1..16 with no silent uppercase normalization; Generated Domain `systemCode` is used as current Channel value without mapping.
- Channel DB policy is `operation_id + caller_channel`; DB3 fresh/source/install/migration/runtime surfaces converge on the canonical schema.
- PostgreSQL/Oracle vendor dialect generation no longer inherits incompatible MariaDB expressions.
- Subject Tracking uses role/type/source/trust metadata, protected deterministic search token, transaction binding and existing ADM transaction timeline; raw identifier persistence is not the search model.
- Central server management uses stable Managed Server + existing Runtime Instance/State/Capability ownership; feature screens consume shared Runtime Inventory instead of creating feature-specific server masters.
- File/DB Retention is wired to real execution paths. Scheduled/Manual/Resume share one engine; pause occurs between safe chunks; lease/fencing, maxRows/maxRuntime/throttle and history are enforced. Manual run re-previews server-side before execution.
- EDU 20 online + 15 batch keeps scenario count while removing compressed nested golden-path responsibilities; generated customer-domain hardcoding and ObjectProvider repository fallback were removed from EDU golden paths.
- ADM frontend server paging/lazy load/runtime status/mobile/modal/shared table/form/authorization-context defects were currentized; generated client and actual consumers were regenerated/currentized.

## 4. Latest-source unverified runtime items — NOT PASS

The following are **not** reported as PASS because they were not completed on the latest modified source in this assistant environment:

- Java 25 root Gradle configuration/compile/test/build/publication/SBOM
- Live Oracle Fresh/Upgrade/Runtime/Rollback
- Live PostgreSQL Fresh/Upgrade/Runtime/Rollback
- Live MariaDB Fresh/Upgrade/Runtime/Rollback
- Redis/Valkey live reconnect/failover
- Multi-WAS policy propagation, Subject concurrent bind, retention lease contention with real multiple JVMs
- Process kill/restart/redeploy recovery
- Final real-browser Playwright viewport matrix on the latest source

These are environmental/runtime revalidation requirements, not permission to revert the implemented source.

## 5. Final local validation

Canonical entry point:

`cpf-tools/verification/tools/run-cpf-final-local-validation.ps1`

The next QA run must use the latest local Working Tree and must validate both actual Git checkout mode and ZIP/fallback mode for the final verifier.
