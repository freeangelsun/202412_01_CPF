# Open Issues

There are no known implementable Source TODOs left from the 34 direct R6J rework rows after the local Source/Contract/Mutation gates. The remaining open conditions are external qualification only and must not be promoted to PASS without execution.

1. Java 25 + Gradle 9.1 exact-SHA clean build, test, publication, regeneration zero-diff.
2. Oracle/PostgreSQL/MariaDB live lifecycle including V107/V108 apply/verify/rollback/reapply.
3. ADM and BZA authenticated Chromium/Firefox/WebKit E2E, including unauthorized high-risk actions and 401/403/404/409/422/429/500/503 behavior.
4. 2+ instance Approval process-kill, response-loss, owner-success/DB-finalization-failure, UNKNOWN observation reconcile, duplicate side-effect negative proof.
5. Broker/network/DB failure injection, performance/backpressure, security negative corpus, DR/backup/restore.
6. Generator create → runtime → remove → regenerate across DB3 and generated consumer artifacts.
7. TransactionId end-to-end lineage across nested transaction/external/message-DLQ/batch/file/trace/audit with stale/partial stores.
8. Independent Codex review bound to `cd5baccb02245a980e5998aa0dc9bac579fc019f` and subsequent QA re-verification.

See `RUNTIME_QUALIFICATION_MATRIX.csv` for exact commands and required evidence.
