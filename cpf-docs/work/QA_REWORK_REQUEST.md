# CPF QA Rework / Revalidation Request — Current Development Package

Developer-GPT has repaired the reproducible Source/Static/Independent-Gate defects found in the supplied QA3 and local validation package. QA must revalidate the **current desired-state source**, not historical evidence.

Priority runtime/revalidation scope:

1. Java25 Root Gradle configuration/compile/test/build/publication/SBOM.
2. Canonical System 6 Header + receiver-owned `X-System-Code`, controller-before reject and durable failure evidence.
3. Same-JVM/Remote transaction-context parity and Operation policy.
4. Optional `cpf-biz-admin` physical removal; external DB-less `cpf-biz-channel` and `cpf-biz-frontend` build/runtime; Direct HTTP same security contract.
5. Common canonical `cpfDB` runtime and actual transaction execution.
6. Runtime Instance identity, central Registry, server-side paging and multi-instance behavior.
7. Subject late enrichment/concurrent bind/time-range search/timeline lineage.
8. Retention scheduled/manual/pause/resume/lease/process-kill/recovery.
9. DB3 Oracle/PostgreSQL/MariaDB fresh/upgrade/runtime query/rollback.
10. Redis/Valkey reconnect/failover and other provider runtime regression.
11. Education 20+15 build/runtime/consumer regression after Delete Manifest application.
12. ADM + external BZA Reference actual Browser E2E and 401/403/404/409/429/500/503.
13. Public Git release command on a clean private Git checkout; any failed gate must prevent push.
14. Delete Manifest application must remove only approved root-relative candidates and protected delete count must remain 0.

Runtime Evidence absent = `미검증`, never PASS. A failure reopens the same Requirement and should be repaired by common root cause rather than adding duplicate APIs/engines.
