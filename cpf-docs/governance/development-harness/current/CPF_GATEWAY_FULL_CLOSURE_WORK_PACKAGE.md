# CPF Gateway Full Closure Work Package

## Intake basis

- Primary Source: current VS Code Working Tree only.
- Acceptance overlay: `CPF_GATEWAY_FULL_CLOSURE_DEVELOPMENT_QA_REQUEST_FINAL_20260823.md`.
- Attachment identity: SHA-256 `fdc30d3d0098f6a230ae78e0075ba8809966eab76b677b2aaed50bcc219e5d63`; 35,136 bytes; 2,093 lines.
- The attachment's older ZIP snapshot is provenance only. It is not a replacement Source and no ZIP reconstruction is authorized.
- Existing Gateway implementation and Evidence are reusable only after current-impact validation; declarations, ADM storage, help, mocks and unit-only results cannot close a Runtime requirement.
- Current terminology is Channel Code. Retired BZA is not restored; the current Backoffice/MBW topology is used where applicable.

## Priority and dependency order

The active F187 query-consumer gate and Backoffice safety gate are closed first because their DB3 catalog, common registry and current Backoffice ownership are prerequisites reused by Gateway control-plane/runtime validation. PostgreSQL V135 is then replayed before any Gateway DB3 acceptance. Gateway work proceeds from read-only call graph and consumer matrix to targeted repair, then actual runtime/failure recovery, so no new parallel registry, selector or logging abstraction is introduced prematurely.

## Net execution inventory

The following 32 bounded units are added after de-duplicating existing Java 25 build, System6 header, Logging, Open Git and DB3 tracks.

| Unit | Acceptance execution |
|---|---|
| GW-01 | Current Gateway/ADM/Common Source and public-surface inventory |
| GW-02 | Primary data-plane call graph and real handler/target resolver ownership |
| GW-03 | ADM UI → generated client → API → DB → publish/version → snapshot → consumer matrix |
| GW-04 | Explicit/Channel/Hybrid routing mode and Channel Code terminology inventory |
| GW-05 | Common Service/Instance/Endpoint canonical ownership and duplicate-URL check |
| GW-06 | Server Group Member → Common Registry join → effective pool proof |
| GW-07 | Headerless explicit route and management/health header boundary |
| GW-08 | Optional Channel route, explicit-target conflict and mismatch policy |
| GW-09 | Registered/static external endpoint routing and single-target route |
| GW-10 | One primary target-selector flow; duplicate resolver responsibility removal if proven |
| GW-11 | Actual Round Robin selection/exclusion/distribution |
| GW-12 | Actual Weighted Round Robin selection/distribution |
| GW-13 | Rendezvous Hash affinity/stability, constrained key source and missing-key behavior |
| GW-14 | Priority Failover actual consumer and unavailable-pool behavior |
| GW-15 | Least Load real metric ownership and actual selection |
| GW-16 | Canary actual consumer and distribution |
| GW-17 | Common/Gateway effective health composition and actual pool consumption |
| GW-18 | Down/recover threshold, maintenance, stale and drain routing behavior |
| GW-19 | Health endpoint/path separation plus lease/fencing/takeover |
| GW-20 | Local snapshot, config version, invalidation and bounded Last Known Good |
| GW-21 | Retry/idempotency, circuit and rate-limit actual data-plane behavior |
| GW-22 | SSRF, DNS, scheme, TLS, credential/secret and management authorization negatives |
| GW-23 | Structured DB Log ON/OFF and level actual insert behavior |
| GW-24 | File sink independent ON/OFF/level actual write behavior |
| GW-25 | Ledger/audit/body capture separation and success/error sampling |
| GW-26 | Logging backpressure/failure isolation and metrics |
| GW-27 | Common ADM instance/route/LB/health/log live refresh E2E |
| GW-28 | Append-only DB3 schema/install/upgrade/rollback parity |
| GW-29 | OpenAPI/generated client/frontend IA and 401/403/404/409/429/500/503 behavior |
| GW-30 | Actual single-instance Runtime routing/LB/health/log trace scenarios |
| GW-31 | Multi-Gateway config propagation, process/worker kill and control-plane/cache failure recovery |
| GW-32 | Canonical currentization, Open Git projection, Evidence and final acceptance |

## READY prohibition

Gateway is not READY while any advertised policy is storage-only, a configured headerless route cannot complete, Server Group Members are bypassed, effective health is not consumed by the actual pool, DB/File log switches do not reach their sinks, or any mandatory runtime/failure scenario is unexecuted. Environment-blocked items remain `UNVERIFIED_ENVIRONMENT` with the exact replay condition.
