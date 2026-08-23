# CPF Batch Runtime Deep-Dive Work Package

## Authority and status

- Steering: `CPF_CODEX_BATCH_RUNTIME_AGENT_DISTRIBUTED_RECOVERY_DEEP_DIVE_STEERING_FINAL_20260823.md`
- Primary Source: current VS Code Working Tree
- Runtime owner to verify: `cpf-batch/**`
- Status: `IN_PROGRESS`
- Rule: existing current-source Evidence is reused only when its Requirement and impact scope still match; unexecuted runtime is never PASS.

## Thirty execution units

| # | Unit | Initial state |
|---:|---|---|
| 1 | Existing Findings/Evidence/current Source impact inventory | COMPLETED |
| 2 | `cpf-batch` owner and Public/Internal boundary | PENDING |
| 3 | Tasklet/Chunk/LOCAL_PARTITION/REMOTE_PARTITION/REMOTE_CHUNK/REMOTE_STEP inventory | PENDING |
| 4 | Developer/operator CLI and Windows/Linux shell inventory | PENDING |
| 5 | Config/profile/identity/health contract | PENDING |
| 6 | Java 25 Batch modules compile/test baseline | PENDING |
| 7 | Tasklet success/failure/persist/restart runtime | PENDING |
| 8 | Chunk checkpoint/rollback/retry/skip/duplicate runtime | PENDING |
| 9 | Local partition assignment/partial failure/restart | PENDING |
| 10 | Scheduler due/overlap/missed-fire/multi-instance lease | PENDING |
| 11 | On-demand auth/identity/duplicate/invalid invocation | PENDING |
| 12 | Center-Cut trigger/cut/partial/restart/reconcile | PENDING |
| 13 | File batch identity/malformed/restart/charset/large-file | PENDING |
| 14 | Synthetic scale/memory/streaming/backpressure baseline | PENDING |
| 15 | Agent start/registration/identity/capability/runtime | PENDING |
| 16 | Agent heartbeat/lease/disconnect/reconnect/stale cleanup | PENDING |
| 17 | Worker assignment/race/duplicate prevention | PENDING |
| 18 | Remote Partition two-worker E2E | PENDING |
| 19 | Remote Chunk manager/worker/ack-loss/idempotency E2E | PENDING |
| 20 | Remote Step canonical-support and E2E | PENDING |
| 21 | Scheduler/Worker/Agent/Manager process-kill and restart | PENDING |
| 22 | UNKNOWN state and reconciliation | PENDING |
| 23 | Retry/Restart/Recovery/Reconcile/Reprocess separation | PENDING |
| 24 | Oracle/PostgreSQL/MariaDB schema/query parity | PENDING |
| 25 | DB install/seed/execution/upgrade/rollback preservation | PENDING |
| 26 | Impacted ADM Batch monitoring/control/risk actions | PENDING |
| 27 | Impacted EDU Batch 15 public-consumer verification | PENDING |
| 28 | Generated optional Batch and Open Git Public Binary/CLI | PENDING |
| 29 | Canonical Requirement/ledger/inventory/spec currentization | PENDING |
| 30 | Broader regression, Source Identity, Evidence and final Batch judgment | PENDING |

## Initial overlap classification

| Area | Classification | Basis / required follow-up |
|---|---|---|
| Center-Cut compile/context/validation | ALREADY_CLOSED_AND_REUSABLE | F029/F032/F033/F034; 23/23 Java 25 tests at the recorded Source. Revalidate only if Batch owner changes. |
| Runtime instance identity | ALREADY_CLOSED_AND_REUSABLE | F030 full runtime-support suite; multi-process identity runtime remains separate. |
| ADM Batch typed/risk/security contracts | ALREADY_CLOSED_AND_REUSABLE | F077/F078/F083/F085 and complete ADM 266/266; Browser/runtime integration still pending. |
| EDU Batch 15 | ALREADY_CLOSED_AND_REUSABLE | Current Private gate confirms active/executable catalog 15/15; rerun impacted examples only after Batch source change. |
| Generated optional Batch | ALREADY_CLOSED_AND_REUSABLE | Fresh composite and current MBR actual Java 25 Batch compilation; Open Git/public artifact impact remains. |
| Public Binary/Open Git Batch consumption | IMPACTED_REVALIDATION_REQUIRED | Fresh Publication and Open Git developer flow are pending and precede public Batch runtime acceptance. |
| Actual Agent/Worker/Scheduler multi-process and process-kill | PARTIAL | Source/test/smoke harnesses exist; actual current-source runtime evidence must be inventoried and executed where feasible. |
| DB3 live migration/recovery | ENVIRONMENT_BLOCKED | Static DB3 work exists; Docker daemon and native DB clients were unavailable at last bootstrap. Recheck before final classification. |
| Linux runtime shell | ENVIRONMENT_BLOCKED | Windows host has no working POSIX runtime; static/syntax evidence is not runtime PASS. |

Inventory evidence: `cpf-docs/work/evidence/codex/current/CX-04-BATCH-EXISTING-OVERLAP-INVENTORY.json` (9 modules, 251 main files, 77 Java test files, five executable applications). File existence did not produce a PASS or a Finding by itself.

## Finding lifecycle

Every defect is persisted before Product Source edit as `OPEN`/`IN_PROGRESS`, then `SOURCE_FIXED`, `VERIFICATION_PENDING`, and only `CLOSED` after the entire affected Requirement scope passes. Duplicate symptoms sharing one Batch root cause are one work package/finding.
