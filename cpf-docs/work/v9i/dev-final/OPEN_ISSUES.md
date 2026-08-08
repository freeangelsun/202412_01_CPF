# Open Issues / Verification Pending

No known developer-remediable Source/Consumer/Test/Harness gap remains after the current direct-review cycle. Remaining blockers are verification-only:

1. User-controlled apply/commit/push must create the successor exact SHA; post-apply provenance is therefore still unverified.
2. Java 25 + Gradle 9.1 clean configuration/build/test/publication must run on that successor.
3. Oracle/PostgreSQL/MariaDB live install/upgrade/rollback/reapply and runtime queries must run.
4. Authenticated ADM/BZA 3-browser flows must run.
5. Authorized release targets and remaining real multi-instance/broker/DR fault scenarios must run.
6. Codex and QA must independently re-run and may reopen the same Requirement IDs.
7. `DELETE_MANIFEST.csv` lists canonical package relocation files. Developer GPT did not delete repository files; user-controlled application must remove only those allowlisted paths after replacement presence is confirmed.

Status: `UNVERIFIED / RELEASE_BLOCKED`.
