# TEST AND EVIDENCE — DEVGPT-V9-S02 REV-005

Baseline: `fc207ac5560da59f352ee0c5f83199177f2987b4`  
Request: `DEVREQ-V9-S02-LOCAL-20260805-2140-R005`

## Exact equality

```text
Work Item assigned = judged = 141
CPF-FR assigned = unique result = 3136
CPF-SC assigned = unique result = 4564
Gate assigned = judged = 19
Unreviewed Work Item/CPF-FR/CPF-SC = 0/0/0
Missing / duplicate Primary / unassigned = 0/0/0
Evidence reference/anchor errors = 0
Actual Consumer blank = 0
Manual mapping pending = 0 (80/80 resolved)
```

## New REV-005 verification

| Check | Exit | Actual |
|---|---:|---|
| Scheduler unsafe UNKNOWN SQL guard isolated javac | 0 | compile PASS; test-stub serial warning only |
| Scheduler guard behavior | 0 | PASS 11 assertions |
| Manual semantic mapping review | 0 | 80/80 resolved; all targets exist in exact Work Item scope |

Unsafe vendor SQL containing `'UNKNOWN'` in normal dispatch/claim is rejected before DB query, claim, or external Spring Batch start. This prevents blind retry and duplicate side effects while S04 integration is pending.

## Previously successful isolated Slices retained

Center-Cut lifecycle/controller 25 assertions; Claim 14; Async UNKNOWN 11; Runtime Command 6; Host Agent 18; Spring Batch stale-link 6; Remote UNKNOWN reconcile 39; Scheduler leader DB-time 30 Java + 52 SQL guards.

## Not claimed PASS

Java 25 Gradle full/module tests, MariaDB/PostgreSQL/Oracle transaction/process-kill, broker, browser/generated-client, publication/load, and latest-master regression after integration remain `재확인 필요`.

## REV-005 final consistency validation

| Check | Exit | Actual |
|---|---:|---|
| Requirement/Scenario resolved-mapping synchronization | 0 | 80/80 parent Requirement and linked Scenario flags/basis synchronized |
| Atomic per-ID Evidence validation | 0 | 141 Work Items, 3,136 CPF-FR, 4,564 CPF-SC, 19 Gates; 7,700 unique anchors; missing/duplicate/unassigned/consumer blank 0 |
| Scheduler find/claim dual-SQL fail-closed regression | 0 | Both automatic-dispatch SQL keys reject literal UNKNOWN; isolated behavior harness remains PASS 11 assertions |
| Product source hygiene | 0 | 24 changed product files; trailing-whitespace and local secret-like scan PASS |

These checks complete the assigned atomic **judgment ledger**, not target-environment or QA completion. S04/S05/MGMT integration and latest-master target regression remain open.

## Checkpoint blockers and rerun

- Last judged Work Item / CPF-FR / CPF-SC: `CPF-WP-OPS-TOPOLOGY-07-GENERATION_COMPATIBILITY` / `CPF-FR-019410` / `CPF-SC-029825`
- Next unreviewed exact ID: `NONE_UNREVIEWED`
- Pending Integration Requests: `ICR-V9-S02-S05-0001, ICR-V9-S02-MGMT-0002, ICR-V9-S02-S04-0002`
- Failed direct environment checks: clone Exit `128` (`Could not resolve host: github.com`); Gradle/PowerShell/Docker Exit `127` (`command not found`).
- Corrected alternative verification: GitHub exact-SHA reads, per-ID source/consumer/call-path evidence, isolated Java 21 compile/behavior harnesses, CSV/anchor/hash/hygiene checks. These do not replace Java 25 Gradle, three-vendor DB, broker, browser, process-kill, publication or load.
- After owners integrate and push: `.\gradlew.bat :cpf-batch:contract:test :cpf-batch:control-server:test :cpf-batch:execution-runtime:test :cpf-batch:host-agent:test :cpf-batch:scheduler:test :cpf-batch:center-cut-runner:test`; then execute MariaDB/PostgreSQL/Oracle transaction/process-kill/fencing, broker ACK/duplicate/UNKNOWN and generated-client/browser E2E.
