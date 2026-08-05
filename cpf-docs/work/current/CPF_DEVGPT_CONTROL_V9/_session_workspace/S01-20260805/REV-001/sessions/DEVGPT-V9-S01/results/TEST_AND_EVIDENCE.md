# TEST_AND_EVIDENCE

Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`

## Environment
- OpenJDK/Javac 21.0.10
- Node 22.16.0 / npm 10.9.2
- Python 3.13.5
- Gradle, PowerShell, Docker unavailable in execution environment
- Repository clone attempt: Exit 128, DNS/network unavailable; GitHub Connector exact-SHA snapshot used

## Direct and substitute validation
### async-log-snapshot
- Result: evidence/async-log-snapshot/compile.exit-code=0; evidence/async-log-snapshot/run.exit-code=0
### audit-file-sink
- Result: evidence/audit-file-sink/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### authz-permission-consumer
- Result: evidence/authz-permission-consumer/compile.exit-code=0; evidence/authz-permission-consumer/run.exit-code=0
### core-safety-java21-substitute
- Result: evidence/core-safety-java21-substitute/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### file-log-async-runtime
- Result: evidence/file-log-async-runtime/compile.exit-code=0; evidence/file-log-async-runtime/run.exit-code=0
### idempotency-safety
- Result: evidence/idempotency-safety/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### lock-token-before
- Result: evidence/lock-token-before/result.txt=COMPILE_EXIT=0 RUN_EXIT=1 SOURCE=/mnt/data/CpfLockManager.java
### lock-token-default
- Result: evidence/lock-token-default/compile.exit-code=0; evidence/lock-token-default/run.exit-code=0
### log-recovery-backoff-substitute
- Result: evidence/log-recovery-backoff-substitute/compile.exit-code=0; evidence/log-recovery-backoff-substitute/run.exit-code=0
### resource-server-clock-skew
- Result: evidence/resource-server-clock-skew/compile.exit-code=0; evidence/resource-server-clock-skew/run.exit-code=0
### secret-registry
- Result: evidence/secret-registry/compile.exit-code=0; evidence/secret-registry/run.exit-code=0
### security-contract
- Result: evidence/security-contract/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### security-policy-access-java21
- Result: evidence/security-policy-access-java21/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### security-replay-gap-before
- Result: evidence/security-replay-gap-before/result.txt=COMPILE_EXIT=0 RUN_EXIT=1 EXPECTED_RUN_EXIT=1 DEFECT_CONFIRMED=true
### service-identity-replay
- Result: evidence/service-identity-replay/compile.exit-code=0; evidence/service-identity-replay/run.exit-code=0
### source-consumer-gate
- Result: evidence/source-consumer-gate/full_assignment_equivalent_run.exit-code=2; evidence/source-consumer-gate/source_consumer_gate.exit-code=0; evidence/source-consumer-gate/full_assignment_run.exit-code=1
### source-harnesses
- Result: logs/source snapshots copied
### state-reconciliation-java21
- Result: evidence/state-reconciliation-java21/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### trace-context-sampling
- Result: evidence/trace-context-sampling/compile.exit-code=0; evidence/trace-context-sampling/run.exit-code=0
### trace-contract
- Result: evidence/trace-contract/result.txt=COMPILE_EXIT=0 RUN_EXIT=0
### transaction-log-async-consumer
- Result: evidence/transaction-log-async-consumer/compile.exit-code=0; evidence/transaction-log-async-consumer/run.exit-code=0
### transaction-log-db-safety
- Result: evidence/transaction-log-db-safety/compile.exit-code=0; evidence/transaction-log-db-safety/run.exit-code=0

## Known target-environment gap
- Root Gradle/Java25 full build was not executable because a full clone and Gradle runtime were unavailable. Java21 standalone compile/run harnesses were executed instead.
- Out-of-owner security starter proposal harnesses pass, but product integration/push/regression remains pending in S06.
- DB/Browser/multi-process target validations remain assigned to S04/S05/S02.

## Assignment integrity
- Requirement master: 30,558 rows and Scenario master: 40,763 rows loaded from all parts.
- Session selection: 2,446 CPF-FR and 3,850 CPF-SC, unique and mapped.
- Upstream `build_full_assignment.py` failed because it accepts `canonical_requirement_id` but the current master uses `canonical_requirement_ids`; integration request IR-S06-BUILD-GENERATOR records this defect.

## Checkpoint direct-owner revalidation
- `javac CpfLockManager + CpfLockManagerDefaultTokenValidationHarness`: Exit 0
- `java CpfLockManagerDefaultTokenValidationHarness`: Exit 0, `CPF_LOCK_DEFAULT_TOKEN_VALIDATION_PASS`
- `javac CpfAsyncLogWriterOperations + Harness`: Exit 0
- `java CpfAsyncLogWriterOperationsHarness`: Exit 0, `CPF_ASYNC_LOG_WRITER_SNAPSHOT_PASS`
- Resource Server proposal compile/run: Exit 0, `CPF_RESOURCE_SERVER_CONFIGURATION_PASS`
- Service Identity replay proposal compile/run: Exit 0, `CPF_SERVICE_IDENTITY_REPLAY_PASS`
- Source/Consumer gate recheck: Exit 0, 20/20 checks PASS
- whitespace/final-newline hygiene: Exit 0, errors 0
- local secret scan: PASS; private key/AWS access-key blocking pattern 0
- exact result equations: 74 / 2,446 / 3,850 / 10; unique and assessed
- Evidence path existence check: missing 0
