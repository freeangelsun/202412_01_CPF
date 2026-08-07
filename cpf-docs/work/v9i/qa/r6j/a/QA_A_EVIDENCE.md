# QA A R6J Evidence

## Baseline
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Current exact SHA: `3ed676061246c9db3e44f29e254c0393ecca3929` (`07_02`)
- R6J instruction-known SHA: `0427758db041d38eb0f34d88b55bd5366e2d9e47` (`07_01`)
- Developer baseline: `64049044956924032360fa80be83b5e37c64f828`
- Product Source modified by QA: **No**
- Git write/delete/history operations: **None**

## Developer ledger / provenance
- Rows: **77**
- development_status=완료: **77**
- verification_status=완료: **26**
- verification_status=미검증: **51**
- result_sha=PENDING_USER_APPLY_COMMIT: **77**
- Rows referencing local evidence logs: **55**
- Distinct referenced logs: **14**
- Pushed `cpf-docs/work/r6i-dev/evidence/` observed file: `environment.txt`
- Selected source artifact SHA-256 parity: **7/7 matched**

This means the developer overlay source hash list is not wholly untrustworthy; selected source artifacts survived unchanged. But execution Evidence provenance and result-SHA binding are still not valid for current release qualification.

## Directly executed QA gates
### Frontend contract
- Command: `python verify-r6-frontend-contract.py <current-SHA projection>`
- Exit: **0**
- Result: `[CPF][R6I][FRONTEND][PASS] admRoutes=63 routeBindings=414 uniqueOps=329 approvalGeneratedConsumers=8 workbenches=GET_ONLY`
- Projection uses current SHA files for ADM routes/generated contract/state/workbench/approval client, BZA workbench/permission/playwright and Integration Closure E2E.

### Frontend semantic mutation
- Mutation: `ApprovalsPage.canAction()` changed from `admSession.hasButton(operationId)` to unconditional `true`.
- Exit: **0**
- Result: `[CPF][R6I][FRONTEND][PASS] admRoutes=63 routeBindings=414 uniqueOps=329 approvalGeneratedConsumers=8 workbenches=GET_ONLY`
- QA disposition: **false-green survivor**. A permission bypass mutation must not pass a release contract gate.

### EDU consumer contract
- Command: `python verify-r6-edu-consumer-runtime-contract.py --root <current-SHA projection> --self-test`
- Exit: **0**
- Result: `[CPF][R6I][EDU-CONSUMER][PASS] features=135 types=8 mutations=8`
- Catalog: 135 features; consumer types: `{'PROCESS': 17, 'JDBC_QUERY': 6, 'JDBC_COMMAND': 51, 'HTTP': 5, 'OUTBOX': 6, 'FILE': 6, 'SPRING_BATCH': 30, 'REFERENCE_GATEWAY': 14}`
- Catalog verification status: `{'미검증': 135}`
- EDU-ADM role parity: **0/17 PASS, 17/17 FAIL**
- EDU-ADM readOnly parity: **17/17 PASS**

### Observability adversarial probe
- Probe did not query any real Metric/Log/Trace/Audit backend.
- It returned only seven hard-coded `true` flags plus arbitrary trace/audit strings.
- Exit: **0**
- Result: `[CPF][R6I][OBS][PASS] sourceSha=3ed676061246c9db3e44f29e254c0393ecca3929 checks=7`
- QA disposition: **false-green survivor**.

### Supply-chain / Artifact consumer self-tests
See:
- `evidence/supply-chain-selftest.log`
- `evidence/artifact-consumer-selftest.log`

Both built-in self-tests execute successfully, but they are not equivalent to immutable final-artifact qualification against real remote/offline repositories, Syft/Grype/ORT/signature and release browser evidence.

## DB3 source review
V105 and V106 key semantic tokens were compared independently for PostgreSQL/Oracle/MariaDB:
- V105: 3/3 source token parity PASS for execution lease/fence, nonce, policy lock and overlap trigger.
- V106: 3/3 source token parity PASS for DQ rule/quarantine/operation/fingerprint/result.
- Live install/migration/seed/upgrade/runtime/rollback/forward is **UNVERIFIED**, not PASS.

## Approval current source review
Confirmed:
- exact 4D Owner tuple matching;
- request/approver SoD and snapshot/risk checks;
- RUNNING/EXECUTING lease and stale sweep to UNKNOWN;
- nonce TTL + persistent single-use consume;
- prod/stg SecretRef enforcement;
- V105 DB immutability/overlap hardening.

Not closed:
- Owner side effect can succeed and DB finalization can fail. Recovery attempts use the same repository/database, so a true DB outage can prevent durable UNKNOWN/audit recording.

## Transaction / logging current source review
Confirmed source:
- canonical transactionId generation;
- segment parent/attempt persistence and indexed transaction lookup;
- bounded async DB/file queues and shutdown drain;
- durable fallback journal path for DB logging;
- file process lock, path/symlink protection, permissions, compression/retention status;
- ADM `/transaction-groups/{transactionId}` detail surface.

Missing/partial:
- one-shot multi-source aggregation for Message/DLQ/Batch/File/Audit;
- partial/stale source status;
- canonical DB timeline fields for full trace/span/request/idempotency/tenant/batch/message/file identity set;
- current-SHA distributed failure/runtime proof.

## EDU architecture/security
- Catalog feature count: 135.
- Category counts: `{'DEV': 45, 'BAT': 30, 'ADM': 17, 'BZA': 14, 'GW': 14, 'OPS': 15}`
- All 135 catalog records are `verificationStatus=미검증`.
- EDU-ADM 17 all currently bind through cpf-reference generic records; 17/17 Handler roles drift from catalog.
- Controller trusts caller-supplied actor/roles/data-scope headers.
- PROCESS consumer inherits parent environment and writes full payload to OS temp JSON.

## Tool/environment limitations
The QA runtime has:
python: rc=0
stdout=Python 3.13.5
stderr=

node: rc=0
stdout=v22.16.0
stderr=

java: rc=0
stdout=
stderr=openjdk version "21.0.10" 2026-01-20
OpenJDK Runtime Environment (build 21.0.10+7-Debian-1deb13u1)
OpenJDK 64-Bit Server VM (build 21.0.10+7-Debian-1deb13u1, mixed mode, sharing)

git: rc=0
stdout=git version 2.47.3
stderr=

pwsh: NOT_AVAILABLE PermissionError: [Errno 13] Permission denied: 'pwsh'


No unavailable runtime was promoted to PASS.
