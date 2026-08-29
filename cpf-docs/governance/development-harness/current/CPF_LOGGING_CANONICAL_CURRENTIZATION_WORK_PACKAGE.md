# CPF Logging Canonical Currentization — Current Work Package

## Governing result

The final 2026-08-23 Logging Steering supersedes conflicting earlier Logging directions. This package is execution tracking, not inherited PASS.

The 2026-08-24 all-executable Runtime Config/Profile/Logging/Launcher Steering is an acceptance overlay on this package, the Batch deep-dive, Generated Domain and Open Git work. It adds no duplicate work package and does not restart completed source analysis. Every real executable Main must expose a consumed local/dev/test/stg/prod configuration and logging contract; standalone processes additionally require equivalent Windows/Linux lifecycle launchers. Empty profiles, comments without consumers, library launchers and file-presence-only checks are not PASS. Intake evidence: `cpf-docs/governance/development-harness/evidence/independent-reviewer/current/CX-00-STEERING-ALL-EXECUTABLE-RUNTIME.json`.

## Canonical ownership

- `cpf-common`: shared SLF4J/context/masking/property-validation and any common delayed archive/retention implementation.
- `cpf-common` is a default product dependency, not a developer-selectable capability. Stale optional-Common source comments and canonical text must be removed.
- Each executable application: visible `application.yml` plus applicable local/dev/stg/prod and test profile configuration.
- Generated Customer Domains: the Generator is the sole root owner. MBR/EXS are synchronized only after Scratch fresh generation proves the template.
- Fixed runtimes: ADM, current Backoffice, Gateway, Batch executables, EDU and local runtime owners maintain the same schema in their own resources.
- Existing transaction Evidence logging remains separate from ordinary application runtime files.

## 24 execution units

1. Steering intake and reusable-Evidence classification.
2. Spring Boot application/resource/logging consumer inventory.
3. Existing transaction Evidence versus ordinary runtime-log ownership boundary.
4. Canonical `cpf.logging.files.*` binding/schema/default design.
5. Validation and operator-error contract.
6. Multi-file runtime/error and extensible logical-file support.
7. Safe application/instance path policy.
8. Daily rolling ownership/integration.
9. Five-day delayed compression with testable clock.
10. 365-day deletion and active-file protection.
11. Traversal/symlink/collision/concurrency safety tests.
12. Context/MDC/masking and async propagation impact.
13. Generator template/profile implementation with practical Korean comments.
14. Scratch fresh generate and exact output inventory.
15. Scratch bind/compile/test/runtime and real files.
16. MBR/EXS canonical regenerate/sync parity and compile/test/runtime.
17. Batch executable configuration/shell/log-path integration.
18. ADM/runtime-control integration.
19. Gateway and current Backoffice integration.
20. EDU/local-runtime and remaining executable inventory closure.
21. Windows plus Linux/container contract verification.
22. Open Git fresh/public consumer impact.
23. Canonical requirement/ledger/specification currentization.
24. Broader regression, root hygiene and final acceptance.

## Intake inventory

- 14 maintained `@SpringBootApplication` owners were found.
- 70 maintained application/profile/logback resources were found.
- 25 Java consumers relate to existing Logging/Evidence behavior.
- Generated MBR/EXS profiles contain ports and generated-domain settings but no final Logging policy.
- Generator renders application/profile files from `cpf_domain_generator.py`, but no Generator Logging template/consumer contract is present.
- Existing `cpf.logging.file.*` implementation belongs primarily to CPF transaction Evidence lifecycle and must not be relabeled as ordinary `runtime.log`.
- Batch has five repeated `logback-spring.xml` resources; reuse and actual differences must be assessed before any consolidation.
- ADM/EDU contain `logging.config` references that require exact resource-consumer validation; fallback boot is not PASS.

## Current acceptance state

`VERIFICATION_PENDING` for the completed Common/base-Starter sub-scope only. The complete package remains not accepted. File existence, YML-only properties, mock-only tests and generated-domain manual edits cannot close this package.

The remaining fixed-runtime units now explicitly include profile activation/default/environment/CLI precedence, invalid values, actual logging level/file/rolling/retention/MDC/masking behavior, and the standalone launcher lifecycle matrix. The existing 24-unit denominator already owns this scope; no duplicate units were added.

## Review trace — Common and default Starter foundation

### Defect and root cause

- The final Logging contract had neither a reusable ordinary-runtime policy consumer nor Generator-owned visible output. Existing `cpf.logging.file.*` consumers primarily owned transaction Evidence and could not be relabeled without breaking lifecycle and audit boundaries.
- The existing base Starter still described Common as optional, conflicting with the user-final decision that `cpf-common` is a default product dependency.
- Generated and fixed applications had no single executable consumer that bound an extensible per-file schema to actual runtime files. YML-only projection would therefore have been a false implementation.

### Source changes made

- Added `cpf-common` policy, validation, safe path resolution and clock-driven maintenance for daily archives, compression strictly after five days, deletion strictly after 365 days, active-file protection, containment, symlink rejection and lock-protected maintenance.
- Made the existing base Starter depend on `cpf-common`; no separate Logging Starter was added.
- Added Spring Boot auto-configuration/property binding and an internal default Logback adapter that preserves Console output and writes separate runtime/error files beneath application/instance paths.
- Kept transaction Evidence ownership and lifecycle distinct. Alternative SLF4J backends remain possible only through explicit CPF file-wiring disable plus an equivalent application-owned policy.

### Executed verification and exact result

| Scope | Command / evidence | Actual result | Acceptance |
|---|---|---|---|
| Common policy/maintenance | `:cpf-common:test --tests 'com.cpf.common.logging.*'`, `CX-05-F126-COMMON-LOGGING-TEST-RERUN2.json` | 4 tests, 0 failures/errors/skips | Targeted PASS |
| Base Starter runtime wiring | `:starters:base:test --tests 'com.cpf.starter.logging.*'`, `CX-05-F126-BASE-STARTER-LOGGING-TEST-RERUN1.json` | 2 tests, 0 failures/errors/skips; actual `runtime.log` and `error.log` writes asserted | Targeted PASS |
| Complete Finding F126 | Finding ledger and this package | Generator/Fresh/MBR/EXS/fixed runtime/MDC/masking/Open Git scopes remain | NOT CLOSED |

### Remaining mandatory review scope

- Modify the canonical Generator before touching generated MBR/EXS output; verify Fresh output inventory, compile/test, runtime files and regenerate parity.
- Wire and run every fixed executable owner, including Batch, Admin/current Backoffice, Gateway, EDU and local runtime.
- Prove actual context/MDC lineage and sensitive-data masking on real output, not only configuration binding.
- Regenerate the Open Git release and execute the Fresh public consumer workflow.
- Currentize canonical requirements and close F126 only after the whole affected surface passes.
