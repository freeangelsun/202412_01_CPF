# DEVGPT-6D Test and Evidence

- Current master: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- Initial development base: `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06`
- Scope derivation: `WORK_ITEM_INDEX.csv → markdown_file → ledger_part → CPF_REQUIREMENT_MASTER.parts / CPF_SCENARIO_MASTER.parts`
- Working Tree: direct clone/fetch could not be created because container DNS could not resolve GitHub; exact-SHA source was retrieved through GitHub Connector and all writes remained in the temporary overlay.
- Available environment: OpenJDK/Javac 21.0.10, Node 22.16.0, npm 10.9.2.
- Unavailable target environment: Java 25, Gradle 9.1 full clean repository, real external providers, official DB runtimes and Browser/Playwright.

## Scope and individual-ledger coverage

- Repository work-item index: 775 unique rows.
- DEVGPT-6D assigned work items: 96; SAGA excluded: 14.
- Canonical requirements: 21.
- Detailed CPF-FR: 1281 reviewed; missing 0; duplicate 0; unreviewed 0.
- CPF-SC: 1886 unique; 252 core acceptance and 1634 cross-session frontend/related scenarios.
- Development/self-review candidates: 1015.
- Cross-session incomplete: 266.
- Requirements without scenario: 0; actual consumer blank: 0; evidence-less decision: 0.
- Applicable Engineering Gates: 18 exact IDs in `SCOPE_RECONCILIATION.md`.

## Executed low-cost gates

```text
JAVA=openjdk version "21.0.10" 2026-01-20
JAVAC=javac 21.0.10
RESULT=PASS
--- api_client_runtime.run.log
PASS API_CLIENT_RUNTIME_SINGLE_RATE_OWNER_VERSION_EXACT_INT_DUPLICATE
--- consumer.run.log
PASS CONSUMER_NO_REEXECUTION_UNKNOWN_ATOMIC_DLQ_MASKING_DUPLICATE
--- dlq.run.log
PASS DLQ_APPROVAL_SOD_EXECUTOR_BINDING_EXPIRY_HASH_SNAPSHOT_OWNER_COMMAND
--- fixedlength.run.log
PASS FIXED_LENGTH_BYTE_OFFSETS_MULTIBYTE_STRICT_PADDING_LAYOUT
--- gateway_entry.run.log
PASS gateway-entry listener/tls/protocol/maintenance/CAS/telemetry harness
--- gateway_entry_applier.run.log
PASS gateway entry runtime applier harness
--- gateway_entry_reference.run.log
PASS REFERENCE_GATEWAY_ENTRY_STATUS_NO_REQUEST_REEVALUATION_MASKED_TELEMETRY
--- gateway_entry_startup.run.log
PASS gateway startup listener/tls consistency harness
--- gateway_jdbc.run.log
PASS GATEWAY_JDBC_BATCH_VALIDATION_BLOCK_DEADLINE_CROSS_WINDOW
PASS GATEWAY_JDBC_REQUEST_HASH_CONFLICT
--- gateway_rate.run.log
PASS GATEWAY_RATE_LIMIT_ATOMIC_MULTI_SCOPE_GLOBAL_SUBJECT_DEDUPE_HASH_CONFLICT_EXPIRY_CAPACITY_FAIL_CLOSED_ABUSE_CONCURRENCY_HEALTH_STRICT_CONTRACT
--- gateway_security.run.log
PASS GATEWAY_TRUST_API_CLIENT_SINGLE_RATE_OWNER_VERSION_CAS_AUTH_BOUNDARY
--- gateway_semantic.run.log
PASS reject_inbound_x_forwarded
PASS regenerate_forwarded
PASS single_trusted_header_value
PASS stable_rate_dedupe
PASS idempotent_body_replay
PASS connect_failure_not_unknown
PASS rate_429_retry_after
PASS rate_headers_survive_allowlist
PASS atomic_multiscope
PASS no_partial_commit
PASS denied_batch_replay_stable
PASS request_hash_conflict
PASS opaque_subjects
PASS version_conflict
PASS single_rate_owner
PASS distributed_startup_guard
PASS fail_closed_counter
PASS entry_first_consumer
PASS entry_tls_protocol_port
PASS entry_maintenance_cas
PASS entry_public_provider_neutral
PASS control_dedicated_listener
PASS control_data_plane_boundary
PASS production_tls_startup_guard
PASS GATEWAY_ENTRY_TRUST_RESILIENCE_RATE_LIMIT_SEMANTIC_CONTRACT
--- http.run.log
PASS HTTP_PRE_POST_DISPATCH_IDEMPOTENCY_STREAMING_LIMIT_URI_SECURITY_ENDPOINT_ALLOWLIST
--- kafka.run.log
PASS KAFKA_HEADER_TRACKING_RESERVED_ACK_CONTRACT
--- kafka_bridge_compile.run.log
--- outbox.run.log
PASS OUTBOX_FIRST_PUBLIC_CLIENT_PROVIDER_WORKER_MASKING_CORRELATION
--- repository_compile.run.log
--- sftp.run.log
PASS SFTP_PATH_FAIL_CLOSED_RESUME_STATUS_PROCESS_KILL_LEDGER_MASKING
--- tcp_journal.run.log
PASS TCP_DURABLE_JOURNAL_RESTART_ABA_CAS_MASKING
```

Additional source compile: `GATEWAY_JUNIT_SOURCE_COMPILE.txt` PASS for actual rate-limit JUnit sources with minimal JUnit API stubs.

The 19 gate groups include Gateway Entry listener/TLS/protocol/maintenance/CAS/telemetry, Runtime Applier, Startup consistency, Reference consumer, API-LIMIT atomic/JDBC/security paths, HTTP, fixed-length, SFTP, DLQ approval, Outbox, Consumer recovery, Kafka header, TCP durable journal, repository/provider compile and 25 semantic contract assertions.

## Static and integrity gates

```text
structured-data=PASS
hygiene=PASS
java-static=PASS
secret-scan=PASS
official-vendor=PASS
protected-delete=PASS
coverage-integrity=PASS
```

## Not recorded as PASS

- Java 25 / Gradle 9.1 full build, test and publication.
- Real Kafka, JMS, IBM MQ, RabbitMQ, SFTP, TCP institution and multi-process SCG/TLS runtimes.
- Oracle, PostgreSQL and MariaDB install, upgrade, rollback, reapply and runtime-query lifecycle.
- Browser, Playwright, accessibility and clean generated-client flows.
- Cross-session 6C/6E/6F/6A changes listed in `CROSS_SESSION_CHANGE_REQUEST.csv`.

These remain explicit environment or cross-session gaps and are not promoted to PASS.
## V8 개발관리 정본 연속성

- 최신 master: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- V8 `DEVELOPMENT_ITEM_INDEX.csv` Work Package: 775개
- 기존 6D 배정 96개와 V8 `source_work_item_id` exact match: 96/96
- Canonical Requirement: 21개, 누락/중복: 0/0
- V8 중앙 `assigned_session_id`는 새 Campaign 미생성으로 공란이며, 이 세션은 진행 중인 6D Scope만 보존·완료 후보화한다.
- 근거: `V8_SCOPE_CONTINUITY.csv`, `V8_SCOPE_CONTINUITY.md`, `V8_SCOPE_CONTINUITY.txt`

