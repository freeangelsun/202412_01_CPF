# Codex Collaboration Handoff — Active

This is a live coordination record for a stopped or resumed Claude/Codex session. It is
provenance only: it does not promote any work item to `CLOSED` or `PASS`.

## Current ownership boundary

- Baseline Git branch/HEAD observed read-only: `master` / `f9669afcfe5b7d41a534d46d2eff0bcdb1ba271c`.
- No Git write was performed by Codex.
- Codex currently owns only these uncommitted source changes. Do not overwrite them without
  reconciling the Root Cause and regression below:
  - `cpf-tools/build/cpf-root-conventions.gradle`
  - `cpf-tools/verification/tests/test_cpf_gradle_task_group_readability.py`
  - `cpf-tools/verification/tests/test_cpf_developer_shell_contract.py`
  - `cpf-tools/db/generator/generate-official-db-vendor-source.ps1`
  - `cpf-tools/db/tests/test_mariadb_profile_verify_contract.py`
  - generated current DB Provision/Verify projections under `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/`.

## Active WP — dynamic Domain task projection

- Observed failure: root `cpfBuildMember` first failed because `GradleBuild.tasks` received a
  Groovy `GString`; after string normalization it failed again because the nested Gradle build
  made the Domain `settings.gradle` attempt to `includeBuild` its parent root.
- Root Cause: `GradleBuild` is an in-process nested build and Gradle correctly forbids a child
  composite from including that parent build.
- Source correction in progress: every generated/optional Domain build, verify, standalone run,
  deploy, and internal target task now invokes the root Gradle wrapper as an isolated OS process
  with `--project-dir <domain>`, current `-PcpfProductCompositeRoot`, and the selected DB vendor.
  This preserves current-source consumption without stale publication fallback.
- Actual consumer evidence: `cpfBuildMember` completed successfully after the correction:
  root task → isolated Domain process → current-source composite → `:online:build` and
  `:batch:build`; 102 Domain actions, `BUILD SUCCESSFUL`.
- Regression source test added: it rejects a return to `GradleBuild` and requires the isolated
  wrapper/current-source contract for every Domain axis.
- Actual complete-consumer evidence: `cpfBuildAll` passed (596 actionable tasks) and includes
  `cpfInternalBuildBackofficeOnline`, `cpfInternalBuildExternalOnline`, and
  `cpfInternalBuildMemberBatch`; individual `cpfBuildBackoffice` also passed (89 actions).

## Active WP — DB Verify Pack physical-owner projection

- Root Cause: the official DB source generator iterated every enabled application module. The
  shared `cpfDB` owner and its enabled education alias therefore emitted duplicate
  `table_count`, `table_engine_collation`, and `runtime_transaction_id_contract` check names.
  `initialize-cpf-database.ps1` correctly failed closed on that duplicate; this was not a
  runner-only false failure and has not been suppressed.
- Source correction: the generator now resolves exactly one explicit physical owner for every
  enabled logical database and uses that collection for Provision, service-account/Grant, and
  Verify projections. A profile with zero or multiple owners fails during generation.
- Canonical generated outputs were regenerated through the official generator and both vendor
  bundle builders. The affected MariaDB, PostgreSQL, and Oracle Provision/Verify projections are
  intentionally part of the same uncommitted source set.
- Regression: isolated pytest of DB + Gradle contracts passed `48 passed, 3 subtests passed`.
  Direct generated SQL correlation confirmed unique check names: MariaDB 20, PostgreSQL 6,
  Oracle 6; duplicate count is zero for each vendor.
- Physical runtime evidence on this current source identity:
  `contentSha1=7ce6806a000d39ec85eebcb4cc57fdd2774f17da`,
  `contentSha256=0afd0f969b8b62bfc1302a60d1229332ee4a943de56fc1139f5ce496dbd0053c`.
  Verifier-owned Docker MariaDB lifecycle is PASS at
  `C:/Users/fly10/Downloads/CPF_CODEX_DB3_MARIADB_20260831_221126`:
  FreshInstall, pre-current fixture, Upgrade, Rollback/Reapply, and cleanup all passed; the
  installer reported `MariaDB canonical verify=PASS checks=20`.

## VS Code / classpath observation

- User-reported missing class folders for `web-api`, `secure-api`, `browser-bff`, and
  `batch-service` were physically absent/stale JDT references, not missing declared dependency
  edges.
- Current-source remediation command passed:
  `gradlew cpfPrepareIdeClasspath cpfVerifyIdeClasspathReady cpfVerifyIdeClasspathModel` →
  `javaProjects=86`, all canonical outputs present, model gate PASS.
- Fresh VS Code Problems JSON is still required before any `0 Error / 0 Warning` claim. No
  suppression, severity exclusion, or fabricated Problems export was used.

## RUN13 continuation finding (prior-source provenance)

- RUN13 result directory: `C:/Users/fly10/Downloads/CPF_LOCAL_VALIDATION_20260831_204607`.
- It is stopped, not running: `PASS=147`, `FAIL=4`, `SKIP_ENV=2`, `NOT_EXECUTED=7`.
- Prior result source identity: `08b7d1615ff36d81001976ac93ad91743e0a5a29ee42aedfbeba5b2e86e1a471`.
  It is not the current source after the active Gradle changes.
- Single upstream Root Cause observed in stages 125, 140, and 145: MariaDB initialization rejected
  duplicate `check_name` `<verification database>.table_count`. It has a current-source physical
  MariaDB lifecycle PASS above, but the affected Batch/One-WAS consumers still need their own
  reruns. The prior failure blocked DB3 MariaDB,
  Batch runtime DB preparation, and One-WAS runtime DB preparation; the remaining One-WAS logging,
  ADM/Backoffice OpenAPI, Browser, and Performance stages were not executed only as a consequence.

## Next safe sequence

1. Rerun the affected Batch and One-WAS DB-preparation consumers against the current DB projection;
   then run the required File/DB transaction-lineage runtime stages.
2. Run PostgreSQL and Oracle physical runtime matrices separately; never inherit MariaDB PASS.
3. Recalculate current source identity and run fresh VS Code Problems collection after the final
   source change; then run the canonical final runtime/fresh replay.
