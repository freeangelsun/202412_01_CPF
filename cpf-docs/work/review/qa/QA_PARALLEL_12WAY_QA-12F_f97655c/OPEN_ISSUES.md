# OPEN ISSUES — QA-12F

## 요약

- Total findings: `43`
- QA direct fixes: `7` — 모두 교차검토·독립 QA 재검수 대기
- Development rework requests: `24`
- Runtime/environment validation findings: `12`
- Requirement 미검증: `1547`
- Scenario 미검증: `2891`

### QA12F-INFRA-001 — CANONICAL_SPLIT_INDEX_STALE
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv`
- Root cause: Part 010/011 metadata did not match exact-SHA split blobs and blocked full ledger build.
- 영향: Requirement 0 / Scenario 0
- 수정/대상 파일: `cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-INFRA-002 — FULL_LEDGER_VALIDATOR_GATE_AND_SCALE
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `cpf-tools/scripts/validate-cpf-full-qa-ledgers.py`
- Root cause: Validator rejected canonical CPF-GATE IDs and rebuilt scenario ID sets per requirement, causing false failure and O(R×S) runtime.
- 영향: Requirement 0 / Scenario 0
- 수정/대상 파일: `cpf-tools/scripts/validate-cpf-full-qa-ledgers.py;cpf-tools/scripts/tests/test_validate_cpf_full_qa_ledgers_gate_ids.py`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-RES-001 — RECOVERY_PROBE_END_TO_END_MISSING
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-core/api/resilience; cpf-starters/integration/resilience`
- Root cause: Recovery Probe-specific query/command, durable probe state, worker claim/lease and Domain/Batch/ADM consumer path were not found.
- 영향: Requirement 7 / Scenario 7
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-RES-002 — POLICY_VERSION_CONSUMER_AND_MIXED_VERSION_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `resilience policy store/command/engine`
- Root cause: Revisioned policy store exists, but actual Domain/Batch consumer convergence, approval race, restart and mixed-version runtime were not proven.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-LOG-001 — FILE_RETENTION_SAME_DAY_CAP_BYPASS
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `CpfFileLogWriter.java`
- Root cause: Once-per-day retention gate skipped later same-day total-size enforcement.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: `cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java;cpf-core/src/test/java/com/cpf/core/common/logging/file/CpfFileLogWriterTest.java;cpf-core/src/main/resources/application-cpf.yml`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-LOG-002 — FILE_PERMISSION_UMASK_DEPENDENCY
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `CpfFileLogWriter.java`
- Root cause: File/directory mode inherited process umask and did not fail closed on world access.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: `cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java;cpf-core/src/test/java/com/cpf/core/common/logging/file/CpfFileLogWriterTest.java;cpf-core/src/main/resources/application-cpf.yml`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-LOG-003 — STRUCTURED_LOG_SCHEMA_VERSION_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `TransactionLogRecord.java; log policy API`
- Root cause: Structured log record exposes apiVersion but no explicit immutable schema/version compatibility contract for all producers/consumers.
- 영향: Requirement 16 / Scenario 30
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-LOG-004 — ASYNCHRONOUS_WRITER_INLINE_DB_PATH
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `LoggingAspect; TransactionLogListener; TransactionLogService`
- Root cause: Event listener performs DB save/fallback inline without bounded async queue, rejection/backpressure and shutdown drain contract.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-LOG-005 — SPOOL_FSYNC_AND_DOUBLE_FAILURE_DURABILITY_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `TransactionLogFallbackStore.java; TransactionLogService.java`
- Root cause: Durable spool has claim/lease/retry, but explicit fsync and durable signal for simultaneous DB+spool failure were not demonstrated.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-LOG-006 — DYNAMIC_LOG_LEVEL_NO_CAS_PARTIAL_DIVERGENCE
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `DynamicTransactionLogLevelService; AdmDynamicLogLevelRuleStore/BroadcastService`
- Root cause: Rule revision/CAS is absent and runtime→DB→broadcast→audit partial failure can leave instances divergent without durable reconcile.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-LOG-007 — LOG_POLICY_NO_CAS_DISTRIBUTION_CONVERGENCE
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `LogPolicy service/cache/controller`
- Root cause: Version/checksum exists, but expected-version CAS, durable multi-instance ACK/NACK and rollback convergence were not proven.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-LOG-008 — GENERAL_RETENTION_PROVIDER_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `api/retention; file logging retention`
- Root cause: File-specific retention exists, but one canonical retention provider/consumer path for all required logs/artifacts was not identified.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-TRACE-001 — OPERATION_SPAN_CONSUMER_COMPLETENESS_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `CpfTelemetry; CpfOpenTelemetryAdapter; operation consumers`
- Root cause: Generic OTel adapter exists, but dedicated Local/Remote/Message/Batch/File span lifecycle consumers were not completely connected and evidenced.
- 영향: Requirement 72 / Scenario 135
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-TRACE-002 — SAMPLER_POLICY_EXPORTER_CONVERGENCE_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `CpfTraceSamplingPolicy; OTel SDK sampler`
- Root cause: CPF sampling/boost decisions and exporter sampler convergence/persistence across instances were not proven.
- 영향: Requirement 16 / Scenario 30
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-TRACE-003 — BAGGAGE_ALLOWLIST_CARDINALITY_BUDGET_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `CpfOpenTelemetryAdapter`
- Root cause: Sensitive-key filtering exists, but explicit baggage allowlist and bounded attribute cardinality/value length policy are absent.
- 영향: Requirement 16 / Scenario 30
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-MASK-001 — TRUNCATE_NEGATIVE_BOUNDARY_EXCEPTION
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `SensitiveDataMasker.truncate`
- Root cause: Negative or tiny max length could reach substring with invalid bounds instead of using the fail-closed minimum.
- 영향: Requirement 48 / Scenario 90
- 수정/대상 파일: `cpf-core/src/main/java/com/cpf/core/common/logging/SensitiveDataMasker.java;cpf-core/src/test/java/com/cpf/core/common/logging/SensitiveDataMaskerBoundaryTest.java`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-MASK-002 — BEARER_TOKEN_REDACTION_ORDER_LEAK
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `SensitiveDataMasker.mask`
- Root cause: Authorization key masking ran before bearer-token masking and left the token body visible.
- 영향: Requirement 48 / Scenario 90
- 수정/대상 파일: `cpf-core/src/main/java/com/cpf/core/common/logging/SensitiveDataMasker.java;cpf-core/src/test/java/com/cpf/core/common/logging/SensitiveDataMaskerBoundaryTest.java`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-MASK-003 — MASKING_POLICY_VERSION_CAS_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `SensitiveDataMasker.MaskingPolicy`
- Root cause: Immutable snapshot replacement exists, but explicit revision/CAS/persistence/distribution convergence is absent.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-MASK-004 — RAW_VIEW_BROWSER_ZEROIZATION_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `ADM raw-view/browser paths`
- Root cause: Mandatory approval/short-lived raw view and browser memory zeroization were not proven by an actual frontend workflow/runtime test.
- 영향: Requirement 16 / Scenario 30
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-FILE-001 — FSYNC_ATOMIC_PROCESS_KILL_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `LocalCpfArchiveService.java; file transfer adapters`
- Root cause: Temporary/rollback/checksum guards exist, but explicit fsync and strict atomic publish are absent; unsupported atomic move falls back to non-atomic move.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-FILE-002 — RESUME_RANGE_CANCEL_EXTERNAL_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `CpfFileTransferEngine; SFTP adapters`
- Root cause: Resume/range/cancellation contracts were not proven against a real remote endpoint under disconnect, retry and process kill.
- 영향: Requirement 32 / Scenario 60
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-FIXED-001 — FIXED_LENGTH_STARTER_WRONG_IMPORT
- Action: `DIRECT_FIX` / Status: `QA_DIRECT_FIX_CROSS_REVIEW_REQUIRED`
- Source: `CpfFixedLengthAutoConfiguration.java`
- Root cause: Starter imported nonexistent com.cpf.integration.fixedlength package instead of the actual dependency package.
- 영향: Requirement 120 / Scenario 225
- 수정/대상 파일: `cpf-starters/integration/fixedlength/src/main/java/com/cpf/starter/fixedlength/CpfFixedLengthAutoConfiguration.java;cpf-starters/integration/fixedlength/src/test/java/com/cpf/starter/fixedlength/CpfFixedLengthAutoConfigurationTest.java`
- 미조치 위험: 정본 원장 검증 실패 또는 제품 로그/마스킹/Fixed-Length 기능의 보안·가용성 저하.

### QA12F-FIXED-002 — FIXED_LENGTH_FULL_CORPUS_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `fixedlength API/core/starter`
- Root cause: After wiring repair, full known-vector, multibyte byte-length, streaming and generated-consumer runtime remains unverified.
- 영향: Requirement 120 / Scenario 225
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-DB-001 — THREE_VENDOR_LIFECYCLE_RUNTIME_MISSING
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-tools/db/vendor/{oracle,postgresql,mariadb}`
- Root cause: Static pack parity passes, but actual install→upgrade→rollback→reapply→backup→restore was not executed on the three supported DBs.
- 영향: Requirement 176 / Scenario 330
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-QUERY-001 — CANONICAL_QUERY_CONTRACT_PROVIDER_MISSING
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-core database/data API; persistence-jdbc`
- Root cause: No versioned Query ID/parameter/result/sort/filter/paging/mutation contract with a universal provider and all-DB-consumer path was identified.
- 영향: Requirement 136 / Scenario 255
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-QUERY-002 — QUERY_PLAN_AND_ORPHAN_OPERATIONS_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `query operations path`
- Root cause: Plan baseline, slow-query and orphan-query detection lack one canonical operations store/query/control/evidence chain.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-GW-001 — GATEWAY_MULTI_INSTANCE_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-gateway route/control/registry/runtime`
- Root cause: Route/control implementation exists, but TLS/client identity and multi-gateway ACK/NACK/LKG/drift/rejoin runtime were not executed.
- 영향: Requirement 296 / Scenario 555
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-GW-002 — GATEWAY_FAILURE_LEDGER_RECOVERY_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-gateway ledger/runtime/scg`
- Root cause: Connect/read/client-disconnect/streaming/response-loss/attempt/recovery behavior lacks exact-SHA fault runtime across multiple targets.
- 영향: Requirement 72 / Scenario 135
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-EVT-001 — EVENT_ENVELOPE_VERSION_ALLOWLIST_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `CpfBrokerMessage; broker envelope/binding`
- Root cause: Message ID/topic/key/payload exist, but canonical envelope version, TTL, attribute/serializer allowlist and schema compatibility enforcement are incomplete.
- 영향: Requirement 48 / Scenario 90
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-EVT-002 — EVENT_CONSUMER_CONCURRENCY_RUNTIME_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `broker consumer/worker/provider bindings`
- Root cause: Consumer ACK/redelivery/backpressure/concurrency/multi-instance correlation require actual provider runtime and were not fully implemented uniformly.
- 영향: Requirement 56 / Scenario 105
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-KAFKA-001 — KAFKA_CONSUMER_OPERATIONS_RUNTIME_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-starters/messaging/kafka`
- Root cause: Producer and UNKNOWN mapping exist; complete consumer transaction/ACK/redelivery/ordering/backpressure/health/reconnect/metrics runtime is not proven.
- 영향: Requirement 128 / Scenario 240
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-RABBIT-001 — RABBIT_CONSUMER_OPERATIONS_RUNTIME_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-starters/messaging/rabbitmq`
- Root cause: Publisher confirms exist; complete consumer ACK/redelivery/ordering/backpressure/reconnect/metrics runtime is not proven.
- 영향: Requirement 128 / Scenario 240
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-JMS-001 — JMS_CONSUMER_OPERATIONS_RUNTIME_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-starters/messaging/jms`
- Root cause: Provider-neutral producer exists; provider transaction/consumer ACK/redelivery/backpressure/reconnect/metrics path is not proven.
- 영향: Requirement 128 / Scenario 240
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-IBMMQ-001 — IBM_MQ_CONSUMER_OPERATIONS_RUNTIME_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `cpf-starters/messaging/ibm-mq`
- Root cause: Reason mapping and producer exist; queue-manager consumer/transaction/reconnect/metrics path is not proven.
- 영향: Requirement 128 / Scenario 240
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-OUTBOX-001 — REPLAY_APPROVAL_BYPASSABLE
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `JdbcCpfBrokerReliabilityRepository.replay/replayRange`
- Root cause: Repository replay changes state without an approval token or mandatory two-person approval contract.
- 영향: Requirement 8 / Scenario 15
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-OUTBOX-002 — OUTBOX_CLEANUP_TIMELINE_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `JdbcCpfBrokerReliabilityRepository`
- Root cause: Cleanup retention and complete operations timeline APIs were not identified in the reference repository.
- 영향: Requirement 16 / Scenario 30
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-OUTBOX-003 — OUTBOX_DB_CONCURRENCY_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `JdbcCpfBrokerReliabilityRepository`
- Root cause: Claim/lease SQL exists, but three-vendor concurrent claim/fencing/ACK-loss/process-kill runtime was not executed.
- 영향: Requirement 144 / Scenario 270
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-EXT-001 — EXTERNAL_CALLBACK_RECONCILE_CONFIRM_GAP
- Action: `REDEVELOPMENT_REQUEST` / Status: `OPEN_REDEVELOPMENT`
- Source: `REST/TCP/SFTP external integration`
- Root cause: Institution callback, durable external reconciliation and manual confirmation do not have one mandatory state/audit/approval path.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: ``
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

### QA12F-EXT-002 — TCP_SFTP_FAULT_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `tcp and sftp starters`
- Root cause: Framing/correlation/reconnect and SFTP ledger source exists, but half-open/backpressure/resume/checksum/host-key fault runtime was not executed.
- 영향: Requirement 200 / Scenario 375
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-BATCH-001 — SPRING_BATCH_END_TO_END_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-batch/execution-runtime`
- Root cause: Execution/control/remote partition/chunk source exists, but actual repository DB, Kafka and process-kill restart runtime were not executed.
- 영향: Requirement 224 / Scenario 420
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-BATCH-002 — REMOTE_BATCH_DUPLICATE_UNKNOWN_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `remote message ledger/Kafka channels`
- Root cause: Remote partition/chunk duplicate, UNKNOWN, rebalance and recovery semantics require actual broker multi-instance runtime.
- 영향: Requirement 24 / Scenario 45
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-SCHED-001 — SCHEDULER_LEASE_MISFIRE_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-batch/scheduler`
- Root cause: Scheduler/leader repository exists, but lease contention, misfire/calendar/pause/resume and duplicate-fire prevention were not executed multi-instance.
- 영향: Requirement 83 / Scenario 160
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

### QA12F-AGENT-001 — AGENT_REGISTRATION_HEARTBEAT_RUNTIME_GAP
- Action: `RUNTIME_VALIDATION_REQUIRED` / Status: `OPEN_RUNTIME_VALIDATION`
- Source: `cpf-batch/host-agent`
- Root cause: mTLS filter, command ledger and runtime state exist, but registration/capability/heartbeat expiry and reconnect were not executed with real agents.
- 영향: Requirement 19 / Scenario 40
- 수정/대상 파일: ``
- 미조치 위험: 운영 환경에서만 드러나는 데이터 불일치·중복·유실·복구 실패.

