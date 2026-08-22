# CPF Codex Critical Revalidation & Rework Instruction — Final

## 0. Mission

Codex performs **independent execution-focused revalidation and corrective development** against the exact final development Source. This is not a documentation review. Credits are limited, so spend them first on high defect-yield execution paths that the Developer GPT could not fully execute.

### Exact basis
- Baseline: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260821_151542(1).zip`
- Baseline SHA-256: `324f5d8f33bd59925fcfe4cfcb24772a543cfbf9acbafebe0f6b4b88841a8583`
- Development final Source Identity SHA-256: `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`
- Source-scope files: `8,173`
- Developer source-side Closure: `13/13 CLOSED`
- Developer Final Gate: `PASS`
- Developer Fresh Replay: `PASS`
- Overall product status: `NOT COMPLETE` because `EA-01 BLOCKED_EXTERNAL` remains.

Do **not** inherit historical DEV22 PASS, old SHA, old Evidence or old Backoffice/Generated-Domain assumptions.

## 1. Role and write boundaries

Codex must:
1. independently reproduce high-risk execution paths;
2. attack False Green with mutation where valuable;
3. when a Source defect is reproducible and fixable, directly implement the fix in Codex-owned work and re-run the affected gate;
4. update only Codex-owned status/evidence fields and the Codex result documents;
5. never convert NOT_EXECUTED/SKIP_ENV/BLOCKED_EXTERNAL into PASS.

Codex must not weaken Requirements, add skip/allowlist merely to green a failure, reintroduce old/new dual structures, delete USER_OWNED/UNKNOWN Source, or perform commit/push/reset/clean/history changes without explicit user approval.

## 2. Credit priority

### Tier 1 — mandatory, highest value

#### T1-01 Java25 Root Gradle / Backoffice classification
Use Java25 and the final applied Source.
- Run root Gradle configuration/build with `-PcpfIncludeGeneratedDomains=true`.
- Verify MBR/EXS are Generated Customer Domains; MBW Backoffice is prebuilt and included exactly once.
- Reproduce that the historical `cpf-backoffice/build.gradle:16` generated-domain repository/composite failure no longer occurs in Root private-source build.
- If it fails due Source/Gradle logic, fix and re-run. If only external repository/network is missing, record `BLOCKED_EXTERNAL` with exact command/error.

PASS requires `BUILD SUCCESSFUL`, ExitCode 0, failed tasks 0, failed tests 0.

#### T1-02 Generated Domain fresh execution
Do not inspect only existing MBR/EXS.
Generate fresh scratch cases:
- A: online, no dependency
- B: online + `MBR_SAMPLE_TX_DETAIL` dependency
- C: batch=true
- D: two or more Business Features
- E: external HTTP client

Required IA:
`cpf-<domain>/<runtime>/src/main/java/<domain-package>/<business-feature>/<technical-role>`.

Must never generate `<domain>.online.<domain>.*`, `<domain>.<domain>.*`, `<domain>/online/<domain>/`, `<domain>/<domain>/`.

For dependency case verify typed operation/client/service and actual compile. Run create/setup/sync/diff/regenerate/diff/regenerate/diff and verify idempotency. If runtime environment exists, bootstrap and check Spring/Mapper/Entity/Configuration scan.

#### T1-03 Existing MBR/EXS regenerate parity
- MBR/EXS verify PASS.
- diff clean=true, missing/changed/stale/extraUser=0.
- generated main/test/contract compile.
- legacy generated package reference 0.
- user-owned source loss 0.

#### T1-04 Final Gate + Fresh Replay attack
Run:
`python cpf-tools/verification/tools/run-cpf-development-final-gate.py --root . --expected-source-sha256 4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`

Then independently reproduce exact-baseline Fresh Replay. Source Identity must remain `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`. Do not accept a different identity as equivalent.

Mutation at minimum:
- wrong Generated IA package;
- hidden Starter optional dependency edge;
- Backoffice generated method/path drift;
- forbidden runtime instanceId;
- Source `.class`/bytecode injection;
- Evidence artifact SHA mutation.

Each mutation must FAIL the relevant gate and control must PASS after restoration.

### Tier 2 — execute if environment/credits permit

#### T2-01 Public Binary isolated consumer
Use a reachable artifact repository and isolated Gradle cache. No private Source composite and no `mavenLocal` leakage. Verify public artifact/catalog and Fresh consumer.

#### T2-02 Backoffice actual frontend
Current source-side semantic contract is Backend OpenAPI 96 = BFF route 96 = generated client 96. Run Node >=22.18 and npm 10.9.2 full typecheck/test/build and actual BFF/frontend runtime if available.

#### T2-03 Runtime concurrency
Execute same-host two-process identity collision, first-registration fencing, expectedVersion CAS, process kill, lease expiry, restart/reconcile and UNKNOWN recovery if a true multi-process environment is available.

### Tier 3 — only when true live environment exists

- Oracle/PostgreSQL/MariaDB full lifecycle and mixed-vendor binding.
- Chromium/Firefox/WebKit ADM/Backoffice browser E2E.
- Windows PowerShell/VS Code fresh import/index/UI validation.

Do not spend credits simulating these with a fixture and then call them PASS.

## 3. Critical architecture assertions

### Generated Domain IA
Domain Project, Runtime Module, Java Base Package, Business Feature and Technical Role are distinct concepts. `businessFeature=domainName` is forbidden. Unspecified first scaffold uses reserved `sample`. `base/` is generator-owned common/bootstrap only.

### Starter zero-footprint
`persistence=none`, `httpClient=false`, `resilience=false` must leave no corresponding hidden transitive runtime footprint. Do not solve by verifier allowlist; fix dependency architecture.

### Runtime owner
Central Runtime lifecycle/CAS authority remains in Runtime Control Plane; Batch delegates command/version operations and keeps batch-specific telemetry/execution concerns. Do not move SQL CAS ownership back into a stale Batch-local design merely to satisfy an old fixture.

### Backoffice
Compare semantic sets, not counts: OpenAPI operationId/method/path ↔ BFF route ↔ generated client descriptor/function ↔ actual consumer. Function body invocation must match the descriptor.

### Delete lifecycle
`DELETE_MANIFEST.csv` has exact paths. USER_OWNED/UNKNOWN automatic deletion is forbidden. `user_approved=false` is not permission to delete. Protected paths remain fail-closed.

## 4. Source-defect handling

When a defect is found:
1. reproduce;
2. classify same/new Root Cause;
3. search Repository-wide latent occurrences;
4. modify Source + Consumer + Test/Verifier + Generator/OpenAPI/Frontend as affected;
5. run targeted gate;
6. run Development Final Gate;
7. if final Source changes, calculate a **new Source Identity** and repeat Fresh Replay. Do not keep `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886` after a Source modification.

## 5. Codex result documents — mandatory update

Update:
- `cpf-docs/work/current/CODEX_REVALIDATION_RESULT.md`
- `cpf-docs/work/current/CODEX_FINDING_CLOSURE.csv`

Only Codex-owned areas are changed. Do not overwrite DeveloperGPT/QA-owned status columns.

Finding state:
`OPEN / IN_PROGRESS / SOURCE_FIXED / VERIFICATION_PENDING / CLOSED / BLOCKED_EXTERNAL`.
`SOURCE_FIXED`, `VERIFICATION_PENDING`, `BLOCKED_EXTERNAL` are not CLOSED.

Every executed item must record command, environment, exit code, actual result, evidence path and Source Identity. Every blocked item must record missing environment, rerun command, expected PASS and failure criteria.

## 6. Codex final report

Report in this order:
1. exact Source Identity before Codex;
2. exact Source Identity after Codex (if changed);
3. Tier 1 executed results;
4. Tier 2/3 executed results;
5. findings by status;
6. corrective Source changes;
7. mutation/False-Green results;
8. Final Gate;
9. Fresh Replay;
10. remaining BLOCKED_EXTERNAL;
11. overall completion judgment.

Codex may declare overall product COMPLETE only when all mandatory external/live acceptance is actually executed and PASS, no mandatory BLOCKED_EXTERNAL remains, Final Gate PASS, Fresh Replay PASS and Evidence/Source Identity match.
