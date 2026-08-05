# Open Issues

## Cross-session code gaps

- **DEVGPT-6A:** OpenAPI/generated clients and ADM/BZA routes for Gateway, external integration, Event/Messaging, approvals, 429 and UNKNOWN states.
- **DEVGPT-6C:** Reliability public-port direct replay fail-close and atomic `replay_count`/`updated_at` database CAS/fencing for approved DLQ replay; multi-instance execution claim and UNKNOWN-safe result audit.
- **DEVGPT-6E:** Oracle/PostgreSQL/MariaDB schema and lifecycle. `cpf_gateway_rate_limit_request` must persist deterministic SHA-256 `request_hash`, reject requestId/payload drift, and atomically coordinate journal and multi-scope counters.
- **DEVGPT-6F:** Java 25/Gradle 9.1 full build/test/publication, BOM/catalog and generator/template/lock parity.
- **SAGA V7.1:** 14 items are outside DEVGPT-6D exclusive ownership and require coordinator/QA assignment.

## Environment-only runtime gaps

- Real Kafka/JMS/IBM MQ/RabbitMQ, SFTP/TCP institution and multi-process SCG runtimes.
- Oracle/PostgreSQL/MariaDB install→upgrade→rollback→reapply runtime.
- Browser/generated-client/Playwright/accessibility E2E.

No independent Functional Slice executable with the current tools remains intentionally unperformed.
