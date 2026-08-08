# Codex Independent Review Request

Review the Session 18 Overlay independently against `master@4c4248a12e699c07f9f5fb11fbb33b97ca04077d`. Do not inherit Developer GPT PASS decisions.

Required review focus:

- all 47 `NXT-*` exact IDs and Acceptance Criteria;
- full applied-repository Core class ownership and external/internal dependency direction;
- Core POM/build boundary after authorized relocation deletes;
- 17 Utility relocation/absorption decisions and public API compatibility;
- Health dependency timeout/concurrency/UNKNOWN, multi-instance JDBC registry, ADM consumer and drain ordering;
- JPA/JDBC/MyBatis parity; Oracle/PostgreSQL/MariaDB source/install/migration/rollback/verify/runtime SQL;
- Valkey Session and Lock fencing/audit/multi-instance behavior;
- S3-compatible Object Storage, schema governance, GraphQL, SSE/realtime security and resource limits;
- Testkit and process-kill/recovery harnesses;
- Generator/golden Reference consumer/catalog/BOM/public visibility;
- ADM OpenAPI -> generated client -> actual Vue consumer and error handling;
- exact delete manifest, protected paths, Windows path length, secret leakage, stale/dead artifacts;
- every NOT_EXECUTED runtime item in `RUNTIME_ONLY_VERIFICATION.csv`.

Record Codex findings only in Codex-owned evidence/status fields. Any source defect must reopen the same exact NXT ID rather than weakening the requirement.
