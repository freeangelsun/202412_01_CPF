# CPF QA35 Codex Final Independent Verification Request

## Use timing

Use this request only after QA35 development has been completed, reviewed, committed, and pushed. Do not use the old QA34 request.

## Inputs the developer must fill before Codex starts

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Expected exact SHA: `<QA35_FINAL_PUSH_40_CHAR_SHA>`
- External evidence root: `<ABSOLUTE_EXTERNAL_EVIDENCE_DIRECTORY>`
- approved npm registry and credentials source
- Java 25, Node/npm exact versions
- Oracle/PostgreSQL/MariaDB baseline and runtime profiles
- Kafka/Redis/process-control environment
- browser fixture root and ADM/BZA runtime URLs
- ORT/Syft/Grype executable paths

## Credit-saving execution order

### 1. One preflight only

Before any build, validate:

- HEAD equals Expected SHA and tree is clean
- every wrapper dependency, Gradle task, script parameter, fixture, profile, tool, and evidence output path exists
- QA35 request integrity and current-document uniqueness
- generated OpenAPI/client/marker clean-drift contract
- QA35 ADM capability/menu-route matrix counts and every required graph edge

If preflight fails, stop without running Java, npm, DB, browser, or supply-chain work. Report all preflight defects in one result.

### 2. One low-cost source closure

Run hygiene, secret, ownership/dependency, build coordinates, OpenAPI semantic coverage, operation-consumer graph, real endpoint permission inventory, SQL/generator parity, matrix/evidence schema, ADM menu-route-capability graph, no-silent-fallback, Gateway route-tab mapping, generic-wrapper prohibition, and negative fixtures once.

### 3. Large stages once each

1. Java 25 empty-cache build/publication/consumer
2. ADM and BZA clean frontend generation/build and three browsers, including all 59 ADM routes and the Batch-Online integrated workflows
3. Oracle, PostgreSQL, MariaDB exact-baseline upgrade/rollback/reapply
4. Kafka/Batch/Scheduler/Gateway/Deployment/Agent process-failure matrix
5. supply-chain and final artifact hashes

Do not repeat a successful large stage. If a later defect affects only one module, rerun that module and its direct consumer/gate, not the entire repository.

### 4. Final row-specific closure

Reclassify QA33 and QA35 only from row-specific evidence. Reject bulk ID arrays without per-row command/result/artifact mapping. Produce the final evidence index outside the source tree and verify hashes again.

## Codex modification boundary

Codex is an independent verifier, not the primary developer. Do not redesign or broadly repair source. On failure:

- classify `SOURCE_DEFECT`, `CONTRACT_DEFECT`, or `ENVIRONMENT_BLOCKER`
- record exact command, exit code, first relevant error, affected requirement/scenario/result IDs, and minimum revalidation scope
- do not alter source unless the user explicitly authorizes Codex remediation

## PASS conditions

- sourceSha=resultSha=Expected SHA
- sourceDirty=false
- all verifier dependencies resolved
- no P0/P1 source defect
- required QA33/QA35 rows have row-specific evidence
- generated artifacts produce no diff
- official three DB vendors and three browsers complete
- multi-instance/process-kill and supply-chain required scenarios complete
- external evidence bundle hashes and released artifact hashes match
- final verification itself does not modify source


## ADM credit-saving review scope

Do not manually click every screen before preflight. First generate the 59-route graph and fail once with the full list of missing component/operation/backend/permission/test edges. After source closure, execute one Playwright matrix that covers direct links, route-specific state, permission denial, standard error codes, accessibility, responsive layout, and the critical Batch/Online workflows. Do not treat screenshot similarity or menu count as PASS.
