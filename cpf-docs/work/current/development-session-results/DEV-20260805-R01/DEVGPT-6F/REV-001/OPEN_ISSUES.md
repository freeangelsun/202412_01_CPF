# Open Issues and Runtime Differences

Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`

1. **Java 25 / Gradle 9.1.0 target build** — Java 21 exists, Java 25 and Gradle cache are absent. Wrapper download failed with `UnknownHostException: services.gradle.org`. Root configuration, full compile/test/publication and Fresh Clone remain target-environment verification.
2. **Official DB runtime** — Oracle/PostgreSQL/MariaDB instances are unavailable. Scheduler DB-clock/lease reclaim and Center-Cut concurrency require 3-vendor runtime. See `CROSS_SESSION_CHANGE_REQUEST.csv` request 001.
3. **External Broker runtime** — Kafka/JMS/RabbitMQ/IBM MQ actual redelivery/fault tests require provider environments. See request 003.
4. **Browser runtime** — Playwright/accessibility and ADM Batch control consumer are owned by 6A and not executable here. See request 002.
5. **Process fault/load** — multi-process kill/restart, clock skew, load/backpressure soak and failover require external process/DB environment.
6. **Product governance** — edition/multi-tenant/plugin/package policy is deliberately `PROTOTYPE_ONLY_NOT_GA`; commercial license/support policy and actual opt-in runtime are unresolved.
7. **QA/Codex final status** — all 224/5,658/7,878 rows are reviewed, but QA/Codex independent validation and latest-Git runtime results are still required.
