# OPEN ISSUES — DEVGPT-6C V8

| ID | Severity | Issue | Owner / next action |
|---|---|---|---|
| DEV6C-OI-001 | P0 | 48 capability FRs have no actual Runtime ChangeApplier; 4 have only generic primitives. | Capability Starter owners; see `CROSS_SESSION_CHANGE_REQUEST.csv` |
| DEV6C-OI-002 | P0 | PostgreSQL and Oracle `R64__runtime_control_plane.sql` are missing. | DEVGPT-6E DB owner |
| DEV6C-OI-003 | P0 | `payload_json`/rollback payload are plaintext; no approved encryption storage SPI. | Security/Crypto integration owner |
| DEV6C-OI-004 | P1 | OpenAPI snapshot missing 7 request bodies and 5 query params; 10 raw URL consumers. | DEVGPT-6A |
| DEV6C-OI-005 | P1 | Java25 targeted/full build, publication and binary compatibility are unverified. | DEVGPT-6F |
| DEV6C-OI-006 | P1 | Browser E2E and authorization/data-scope negative runtime are unverified. | DEVGPT-6A + Security/ADM |
| DEV6C-OI-007 | P1 | Two JVM + actual 3-vendor DB contention/reclaim/fault matrix is unverified. | DEVGPT-6E runtime DB test |
