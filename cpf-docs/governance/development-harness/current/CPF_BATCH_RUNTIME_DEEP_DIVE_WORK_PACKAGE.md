# CPF Batch Runtime Deep-Dive Work Package

## Authority and status

- Steering: `CPF_CODEX_BATCH_RUNTIME_AGENT_DISTRIBUTED_RECOVERY_DEEP_DIVE_STEERING_FINAL_20260823.md` plus current Center-Cut Domain Invocation overlay `CPF_CENTER_CUT_BUSINESS_DOMAIN_INVOCATION_STEERING_20260824.md`
- Primary Source: current VS Code Working Tree
- Runtime owner to verify: `cpf-batch/**`
- Status: `IN_PROGRESS`
- Rule: existing current-source Evidence is reused only when its Requirement and impact scope still match; unexecuted runtime is never PASS.

## Forty-two execution units

| # | Unit | Initial state |
|---:|---|---|
| 1 | Existing Findings/Evidence/current Source impact inventory | COMPLETED |
| 2 | `cpf-batch` owner and Public/Internal boundary | PENDING |
| 3 | Tasklet/Chunk/LOCAL_PARTITION 보존 + Kafka 기반 Batch Remote Execution Surface 제거(`BAT-NO-REMOTE-KAFKA`) | IN_PROGRESS |
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
| 31 | Center-Cut Core Dispatch/Result Port versus optional Kafka Adapter boundary | PENDING |
| 32 | CEC runtime System/Channel/Role identity and Header6 Context contract | PENDING |
| 33 | Actual Generated Domain canonical operation target and business result contract | PENDING |
| 34 | Worker official CPF Domain/Transaction Invocation integration without duplicate client stack | PENDING |
| 35 | CEC → Worker → Domain → Result → Aggregate normal physical E2E and trace linkage | PENDING |
| 36 | Business partial failure policy and final aggregate consistency | PENDING |
| 37 | Domain timeout/response-unknown probe, reconcile and no-blind-retry contract | PENDING |
| 38 | Worker kill/lease/reassignment with no duplicate Domain business processing | PENDING |
| 39 | Reply-loss recovery without duplicate Domain Invocation | PENDING |
| 40 | Dispatch provider/broker/topic/group/concurrency/timeout Profile and configuration ownership | PENDING |
| 41 | Execution→Work Item→Worker→Domain Transaction→Operation→Result ADM/trace/timeline linkage | PENDING |
| 42 | Center-Cut Requirement/Open Git/Generated projection and overall two-stage judgment | PENDING |

F274 is a finding closure unit inside units 31–35, so it does not duplicate those planned units. It owns the missing concrete `CenterCutTargetProvider` and `CenterCutHandler` consumers, their canonical Seed/Config wiring, and the actual official Domain Invocation handoff. F265 and F274 are one ordered package: F274 establishes the DB Work → Domain contract; F265 removes Center-Cut Kafka coupling and closes only on the later no-Kafka physical flow.

F265 Source correction removed the six Center-Cut Kafka direct couplings. The optional Kafka Remote Partition diagnostic remains executable but is now owned by Control Plane; the maintained R44 harness starts Control Plane as Remote Manager and starts Center-Cut with Remote Transport disabled. This preserves the independent transport proof without making Kafka a Center-Cut dependency. Runtime classpath/boot and no-Kafka physical verification remain required before closure.

The first classpath check exposed a deeper transitive coupling in common `cpf-batch/runtime`. Kafka implementation, codec/envelope, durable Remote Message ledger, configuration and owner tests are now in `cpf-batch/remote-kafka` (`:runtime:batch:remote-kafka`). Worker, Control Plane and Scheduler select that published optional Adapter; common Runtime and Center-Cut do not. Compile/test, resolved classpath and bootJar inspection remain fail-closed gates.

The corrected Center-Cut runtimeClasspath and actual bootJar now contain zero Kafka/Remote Kafka entries. Fresh official DB3 preparation passes 202 Platform tables, 29 Backoffice tables and 20 MariaDB checks. The first no-Kafka boot reaches a valid Hikari connection but exposes F275 before health: the verified external Vendor SQL Pack provider is not active. F275 is part of the shared executable Boot Gate and must be corrected at the JDBC Starter/launcher owner; no mock, embedded SQL or Kafka fallback is allowed.

F276 owns the Header6 root cause: canonical transaction and item/parent segment values are already persisted in DB Work Items, but the current Step handler binds an unrelated Spring Batch parent transaction and a newly generated segment before official Domain Invocation. Control Plane launch also permits malformed manual transaction values. The fix must resolve/validate IDs through CPF generators/current Context, load all stored lineage, bind it around the official Router and remove transaction metadata assembly from EDU consumers.

F276 is now `VERIFICATION_PENDING`: Java 25 passes 11/11 across canonical launch resolution, exact DB Work Context restore, official Router handling and the real HTTP outbound adapter. All Header6 fields are asserted without developer assembly, and the MariaDB/PostgreSQL/Oracle Work queries are byte-identical with `parent_segment_id`. Closure still requires the physical no-Kafka Generated Domain invocation to observe the same DB-owned transaction lineage.

F277 is `CLOSED`: optional Adapter extraction had left one direct Spring Kafka listener-factory policy in generic Worker Source. The Kafka-only policy and its regression now belong to `remote-kafka`, generic Worker direct Spring Kafka references are zero, the exact policy tests pass 2/2 and all five actual executable JARs rebuild successfully. This removes a repeated common-Boot failure before physical F275 execution.

F275 and F279 are `CLOSED`: Fresh run f275b014 prepares the canonical DB contract (202 Platform tables, 29 Backoffice tables and 20 MariaDB checks), then boots the exact Control Plane, Scheduler, Worker, Center-Cut and Agent JARs without Kafka. All five return liveness 200 and register `UP` 5/5. The Agent consumes the shared BAT Runtime/Platform DB role, owns the JDBC Starter and packages the managed DB3 drivers. Processes, ports and verifier DB ownership are cleaned afterward.

F280 is `CLOSED`: the Agent application requires its cryptographic artifact-state MAC key and command-ledger root through explicit environment placeholders; the implicit workspace-relative Java default is gone. Fresh f275b014 uses an environment-only generated 256-bit key and an evidence-owned ledger path, reaches liveness 200/registry UP, records no secret value and cleans the state path with the evidence workspace.

F281 is `CLOSED`: the role-named `AgentRuntimeStateProvider` is the only provider; the duplicate class/test were deleted immediately. Inventory passes 4/4, Java behavior 2/2, and the exact rebuilt Agent resolves the common Runtime consumer and reaches liveness 200/registry UP in Fresh f275b014.

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

Inventory evidence: `cpf-docs/governance/development-harness/evidence/independent-reviewer/current/CX-04-BATCH-EXISTING-OVERLAP-INVENTORY.json` (9 modules, 251 main files, 77 Java test files, five executable applications). File existence did not produce a PASS or a Finding by itself.

The latest canonical decision supersedes the earlier two-stage interpretation for Center-Cut itself. R44 remains reusable proof of the independently owned Kafka Batch Remote Transport (two real Workers, six requests/replies, completed Manager and durable metadata), but it is not the canonical Center-Cut flow. Center-Cut units 12 and 31–42 are now one Kafka-free DB-backed flow: Control Plane → CEC → DB Work/Claim/Lease/Fencing → two Workers → official CPF Domain Invocation → actual business Domain operation → Result/Aggregate → Drain/Recovery/Reconcile. No Center-Cut Kafka Broker Control, Topic, Listener, Consumer Group, Reply or DLT implementation/repair is permitted. Intake evidence: `cpf-docs/governance/development-harness/evidence/independent-reviewer/current/CX-00-STEERING-CENTER-CUT-DOMAIN.json` and `CX-00-STEERING-CENTER-CUT-NO-KAFKA.json`.

Before the next broad Fresh Batch runtime, units 2–6 are one convergence gate across every actual executable: executable-JAR dependency closure, Starter/Provider presence, ConfigurationProperties binding, unique Bean/AOP ownership, Runtime identity and Profile boot. This is an execution-order refinement, not a new denominator. Kafka may remain only behind its independent Messaging/Batch Remote Transport owner and must not be required by Center-Cut unit 31 or 40. Historical `refDB`/`referenceFixture` remains immutable Migration/Rollback provenance only and must not reappear as a current product surface; Current education remains `education/EDU`. Intake evidence: `cpf-docs/governance/development-harness/evidence/independent-reviewer/current/CX-00-STEERING-F273-AFTER-PLAN.json`.

## Finding lifecycle

Every defect is persisted before Product Source edit as `OPEN`/`IN_PROGRESS`, then `SOURCE_FIXED`, `VERIFICATION_PENDING`, and only `CLOSED` after the entire affected Requirement scope passes. Duplicate symptoms sharing one Batch root cause are one work package/finding.


## 2026-08-26 BAT-NO-REMOTE-KAFKA Current Steering

이 문서의 과거 `remote-kafka` / `REMOTE_PARTITION` / `REMOTE_CHUNK` / Provider-neutral Remote Adapter 관련 계획·보고는 **historical context**이며 현재 요구가 아니다. 사용자 직접 Steering `BAT-NO-REMOTE-KAFKA`가 이를 supersede한다. 일반 Batch·Worker·Scheduler·Center-Cut은 Kafka 없이 보존하고 Kafka 기반 Batch Remote Execution 전체 Surface를 실제 Consumer/Bean/Runtime/DB/Publication/Harness 호출경로 기준으로 제거한다. 새 Remote Transport/Broker를 만들지 않는다. 공용 Messaging Kafka는 별도 Owner/Consumer로만 판단한다.
