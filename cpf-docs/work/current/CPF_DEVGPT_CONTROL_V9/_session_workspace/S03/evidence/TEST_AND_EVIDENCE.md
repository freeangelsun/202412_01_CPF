# TEST AND EVIDENCE

- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- request_id: `CPF-V9-S03-REQ-20260805-001`
- Environment: Linux; Java/Javac 21.0.10; Node 22.16.0; npm 10.9.2; Python 3.13.5
- Unavailable: Java25, Gradle command, Docker, PowerShell, actual external providers/brokers/3 DB vendors/browser process topology

## Repository and assignment

- `git ls-remote origin master` → Exit `128`, `Could not resolve host: github.com`.
- GitHub MCP read at exact SHA succeeded and was used as source of truth.
- Unpatched `build_full_assignment.py` → Exit `1` due canonical/scenario column alias mismatch. See `INTEGRATION_REQUEST_CPF-V9-INT-S03-001.md`.
- Independent S03 reconstruction → Exit `0`; 111 Work Items, 24 Canonical, 3,523 CPF-FR, 5,479 CPF-SC, 18 Gates; missing/duplicate/unassigned 0.

## Independent Java harnesses

- `fencing`: javac Exit `0`, run Exit `0`, result `S03_BROKER_FENCING_HARNESS PASS cases=6`
- `fixedlength`: javac Exit `0`, run Exit `0`, result `S03_FIXED_LENGTH_VALIDATION_HARNESS PASS cases=9`
- `gateway`: javac Exit `0`, run Exit `0`, result `S03_GATEWAY_RESILIENCE_HARNESS PASS cases=6`
- `http`: javac Exit `0`, run Exit `0`, result `S03_HTTP_CLIENT_HARNESS PASS cases=8`
- `kafka`: javac Exit `0`, run Exit `0`, result `S03_KAFKA_OUTCOME_HARNESS PASS cases=5`
- `provider`: javac Exit `0`, run Exit `0`, result `S03_PROVIDER_OUTCOME_HARNESS PASS cases=5`
- `provider_header`: javac Exit `0`, run Exit `0`, result `S03_JMS_HEADER_HARNESS PASS cases=4 S03_IBM_HEADER_HARNESS PASS cases=4 S03_RABBIT_HEADER_HARNESS PASS cases=4`
- `sftp_policy`: javac Exit `0`, run Exit `0`, result `S03_SFTP_OUTCOME_HARNESS PASS cases=7`
- `tcp`: javac Exit `0`, run Exit `0`, result `S03_TCP_OUTCOME_HARNESS PASS cases=6`
- `tcp_primary_clock`: javac Exit `0`, run Exit `0`, result `S03_TCP_CLOCK_HARNESS PASS cases=2`

## Failed attempt and correction

- Initial TCP direct compile: Exit `1` because Spring `@ConfigurationProperties` and TLS/reconnect support types were absent from isolated classpath. This was an environment harness assembly failure, not treated as product PASS.
- Corrective alternative: minimal signature-compatible annotation/support sources were added only to the isolated harness; the unchanged product source was compiled and executed. Final TCP javac/run exits are 0/0 with 6 assertions.

## Static quality gates

- Broad `javac -proc:none` overlay scan: Exit `1` because framework/provider dependencies are not available; `58` files scanned, syntax-like errors `0`, unresolved dependency errors are retained in `evidence/static/javac_overlay.stderr`.
- Text hygiene issues: `1` before final evidence refresh; final validation reruns after artifact generation.
- Package/path issues: `0`.
- V8 reference issues: `0`.
- Local secret scan issues: `0`.
- No delete manifest entries and no protected path deletion/move/replacement.

## Atomic result counts

- Work Item: 111 judged; verification {'완료': 71, '재확인 필요': 40}
- CPF-FR: 3523 judged; verification {'완료': 1635, '재확인 필요': 1888}
- CPF-SC: 5479 judged; verification {'완료': 1676, '재확인 필요': 3803}
- Gates: 18 judged; verification {'완료': 7, '재확인 필요': 11}
- Every atomic row has a distinct ID-scoped assertion, actual result and evidence reference in `results/ATOMIC_ID_EVIDENCE_INDEX.csv`.

## Target-environment re-execution

- Java25/Gradle: `./gradlew --no-daemon :cpf-core:test :cpf-gateway:test :cpf-starters:integration:tcp:test :cpf-starters:integration:fixedlength-core:test :cpf-starters:file:sftp:test :cpf-starters:messaging:reliability-jdbc:test :cpf-starters:messaging:kafka:test :cpf-starters:messaging:jms:test :cpf-starters:messaging:ibm-mq:test :cpf-starters:messaging:rabbitmq:test`
- Provider runtime: execute Kafka/JMS/IBM MQ/RabbitMQ publish, ACK loss, duplicate, DLQ/outbox/UNKNOWN reconcile and process-kill scenarios with pre/post broker and DB state.
- DB runtime: execute install/upgrade/rollback and locking/query tests on Oracle, PostgreSQL and MariaDB.
- Gateway/browser runtime: execute SCG routes and ADM generated-client pages including 401/403/404/409/429/500/503, accessibility, responsive and deep-link scenarios.
- TCP/SFTP runtime: execute TLS/half-open/partial write/response loss/resume/checksum/atomic rename/process-kill scenarios against actual peer/server.

