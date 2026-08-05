# TEST AND EVIDENCE — DEVGPT-V9-S03

## Baseline

- request_id: `CPF-V9-S03-REQ-20260805-001`
- baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- latest master read method: GitHub MCP exact SHA
- local direct Git attempt: failed (`Could not resolve host`, exit 128/6)
- Working Tree: remote exact-SHA read; local product repository unavailable
- Git write/delete: not performed

## Exact assignment and judgement

- Work Item: assigned **111**, judged **111**
- CPF-FR: assigned **3523**, judged **3523**, unique **3523**
- CPF-SC: assigned **5479**, judged **5479**, unique **5479**
- Engineering Gate: assigned **18**, judged **18**
- Missing: 0; duplicate primary: 0; unassigned: 0; evidence-empty: 0; actual Consumer unconfirmed: 0
- Requirement status: `{'완료': 2909, '재확인 필요': 614}`
- Scenario status: `{'완료': 4678, '재확인 필요': 801}`
- Work Item status: `{'완료': 71, '재확인 필요': 40}`
- Gate status: `{'완료': 13, '재확인 필요': 5}`

Atomic evidence is not represented by aggregate counts alone. Every ID has a distinct assertion row in:

- `evidence/REQUIREMENT_EVIDENCE_INDEX.csv`
- `evidence/SCENARIO_EVIDENCE_INDEX.csv`
- `results/DEVELOPMENT_WORK_ITEM_RESULT.csv`

## Direct target attempt and alternative verification

Target Java25/Gradle/DB/Broker/Browser/Process environment was attempted where possible but unavailable. The following standalone Java21 harnesses compile product Source with minimal provider stubs and execute negative/positive boundary assertions. They do not claim Java25, real DB/Broker, Browser or distributed Process success.

| Harness | javac | run | actual output | evidence |
|---|---:|---:|---|---|
| fencing | 0 | 0 | S03_BROKER_FENCING_HARNESS PASS cases=6 | `evidence/harnesses/fencing/` |
| fixedlength | 0 | 0 | S03_FIXED_LENGTH_VALIDATION_HARNESS PASS cases=9 | `evidence/harnesses/fixedlength/` |
| gateway | 0 | 0 | S03_GATEWAY_RESILIENCE_HARNESS PASS cases=6 | `evidence/harnesses/gateway/` |
| http | 0 | 0 | S03_HTTP_CLIENT_HARNESS PASS cases=8 | `evidence/harnesses/http/` |
| kafka | 0 | 0 | S03_KAFKA_OUTCOME_HARNESS PASS cases=5 | `evidence/harnesses/kafka/` |
| provider | 0 | 0 | S03_PROVIDER_OUTCOME_HARNESS PASS cases=5 | `evidence/harnesses/provider/` |
| provider_header | 0 | 0 | S03_JMS_HEADER_HARNESS PASS cases=4 S03_IBM_HEADER_HARNESS PASS cases=4 S03_RABBIT_HEADER_HARNESS PASS cases=4 | `evidence/harnesses/provider_header/` |
| sftp_policy | 0 | 0 | S03_SFTP_OUTCOME_HARNESS PASS cases=7 | `evidence/harnesses/sftp_policy/` |
| tcp | 0 | 0 | S03_TCP_OUTCOME_HARNESS PASS cases=6 | `evidence/harnesses/tcp/` |
| tcp_primary_clock | 0 | 0 | S03_TCP_CLOCK_HARNESS PASS cases=2 | `evidence/harnesses/tcp_primary_clock/` |

## Implemented Connected Functional Slices

1. Provider-neutral Header validation and immutable snapshot before Outbox/provider routing. Reserved names, whitespace and provider projection collisions are rejected.
2. Kafka/provider pre-write FAILED versus post-invocation/ACK-loss UNKNOWN classification and unsupported status fail-closed handling.
3. Outbox/UNKNOWN process-incarnation fencing; legacy unfenced adapters rejected by Worker/Reconciler.
4. Gateway READ/WRITE/UNKNOWN retry semantics, idempotency guard, Clock and trace attributes.
5. TCP deterministic frame validation before I/O, pre-dispatch FAILED versus post-dispatch UNKNOWN, idempotency and Clock.
6. Fixed-length null/order/BCD/unsigned validation.
7. SFTP work-path publication, size/checksum verification, FAILED/UNKNOWN split and deterministic Clock/transfer ID.

## Commands and exit codes

Each harness directory contains the exact `javac.command.txt`, `javac.exitcode`, `run.command.txt`, `run.exitcode`, stdout and stderr. Overlay hygiene check: `git diff --no-index --check` per changed baseline file / trailing whitespace scan for additions, exit **0**.

## Engineering Gates

All gates were judged. `GATE-05`, `GATE-07`, `GATE-13`, `GATE-15`, `GATE-18` remain `재확인 필요` because DB/multi-process/performance/generator/full Java25 evidence is target-owned or environment-specific. No gate is `미검수`.

## Remaining target-only verification

- First Requirement requiring target re-verification: `CPF-FR-001855`
- First Scenario requiring target re-verification: `CPF-SC-000369`
- Full lists: `results/UNVERIFIED_RUNTIME_REQUIREMENTS.csv`, `results/UNVERIFIED_RUNTIME_SCENARIOS.csv`
- Integration requests: `results/INTEGRATION_REQUEST_STATUS.csv` and campaign `requests/S03-ICR-*.md`

## Completion judgement

All assigned atomic IDs are individually judged and have Source, Consumer, call path, assertion and Evidence references. Development within S03-owned paths is implemented. Target Java25/DB/Broker/Browser/multi-process and cross-owner integration remain, so `verification_status=재확인 필요` and `final_completion=false`. This is not CPF QA final completion.
