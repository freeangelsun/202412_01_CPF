# Test and Evidence — Current Developer Rework

Basis checkout SHA: `f6d7080c5a14b7dd7595093f9497470169e18d80`.

## Direct/mutation gates executed

| Gate | Result |
|---|---|
| Central direct ledger | PASS — Source/Consumer/Test 36/36 |
| Canonical direct ledger | PASS — 169/169, 124 unique files opened by gate |
| Previous QA findings direct ledger | PASS — 56/56, 47 unique files opened by gate |
| Special Review direct ledger | PASS — 1000/1000 Source/Consumer/Test, exact basis SHA |
| Runtime readiness | PASS — 13/13 repository-owned runner/script/config/pass criteria |
| EDU runtime consumer contract | PASS — 135 features, 8 concrete consumer types, mutation 8/8 |
| EDU ADM architecture | PASS — 13 non-executable redirects and retained executable 02/03/04/07 |
| Javadoc contract | PASS — 43 files, 44 public types, 244 methods, 8 constructors, missing 0; self-test PASS |
| Starter package ownership | PASS — persistence 10 Java, messaging 25, session-jdbc 17; stale refs 0; mutation PASS |
| DB3 transaction lineage/lifecycle | PASS — Oracle/PostgreSQL/MariaDB static parity + mutation PASS |
| `CpfLockManager` provider contract | PASS — JDBC provider, lease/fencing/epoch/reconcile, DB3 assets + mutation PASS |
| Online/Batch integrated source contract | PASS |
| Integrated Java runtime harness | PASS |
| Batch actual OS process kill/restart | PASS — checkpoint retained, competing lease blocked, restart completed, duplicate remote effects 0 |
| Transaction identity | PASS — authenticated first-hop identity producer + filter/consumer mutation |
| FileLog recovery/fairness | PASS — failed target no longer blocks healthy tail |
| Timeline/ADM batch failure semantics | PASS — `QUERY_FAILED`, `partial=true`, failed source preserved |
| ADM/BZA OpenAPI + typed high-risk clients | PASS — applicable standard errors and generated concrete requests; mutation PASS |
| Release qualification trust self-tests | PASS — Resource/Batch/Broker/Security/DR/Observability; fake localhost rejected 6/6 |
| Package/current-SHA provenance | PASS — pre-push composite provenance and manifest basis bound to current SHA; self-test mutation PASS |

## Environment/runtime still unverified

- Java 25 + Gradle 9.1 clean build/test/publication: current container does not provide the required runtime; repository runner is ready.
- Oracle/PostgreSQL/MariaDB live lifecycle: SQL/migrations/rollback/runtime query/harness ready; live endpoints not available.
- Authenticated ADM/BZA 3-browser matrix: source/generated client/gates ready; browser/auth environment unavailable.
- Real authorized release target, broker/network fault, DR and remaining real multi-instance axes: runners and fail criteria ready; authorized infrastructure unavailable.
- Successor exact-SHA post-apply evidence: cannot exist before user-controlled apply/commit/push.
- Local fresh clone was not available because this execution environment could not resolve GitHub through git; exact-SHA source reads used the GitHub connector instead.

Unexecuted runtime checks are **not PASS**.
