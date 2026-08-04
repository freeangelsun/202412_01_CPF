# QA-12F Connected Functional Slice Source Trace

- Baseline SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- Scope: `CPF-FR-012734`–`CPF-FR-015279`
- Requirements: 2,546
- Linked Scenarios: 4,772
- Connected Groups: 319

이 문서는 구조 확인만으로 판정하지 않고 각 Connected Group의 Public 계약, 구현 Provider, 저장소, 실제 Consumer, Test와 남은 Runtime 차이를 기록한다.

## Resilience
- Group: 2, Requirement: 15, Scenario: 22
- Group result: 통과 0, 미통과 1, 미검증 1
- Public/API: cpf-core/src/main/java/com/cpf/core/api/resilience/*; cpf-core/src/main/java/com/cpf/core/spi/resilience/*
- Implementation/Provider: cpf-starters/integration/resilience/src/main/java/com/cpf/starter/integration/resilience/internal/CpfResilienceEngine.java; CpfResiliencePolicyCommandService.java; JdbcCpfResiliencePolicyStore.java
- Consumer: cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmResiliencePolicyController.java; runtime CpfResilienceExecutor consumers
- Tests/Evidence: cpf-starters/integration/resilience/src/test/java/.../CpfResilienceEngineTest.java; resilience-contract-runtime-harness.log
- Remaining difference: Recovery Probe 전용 durable state/worker/domain·batch consumer 및 실제 3-vendor/multi-instance runtime 없음

## Logging
- Group: 15, Requirement: 120, Scenario: 225
- Group result: 통과 0, 미통과 11, 미검증 4
- Public/API: cpf-core/src/main/java/com/cpf/core/api/logging/*; api/logging/policy/*
- Implementation/Provider: LoggingAspect; TransactionLogListener; TransactionLogService; CpfFileLogWriter; TransactionLogFallbackStore/RecoveryWorker; DynamicTransactionLogLevelService
- Consumer: 모든 AOP transaction consumer; cpf-admin AdmLog*/AdmDynamicLogLevel* controllers
- Tests/Evidence: cpf-core logging tests; dynamic-log-runtime-harness.log; log-spool-runtime-harness.log; file-log before/after harnesses
- Remaining difference: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증

## Tracing
- Group: 13, Requirement: 104, Scenario: 195
- Group result: 통과 0, 미통과 7, 미검증 6
- Public/API: cpf-core/src/main/java/com/cpf/core/api/observability/CpfTelemetry.java; api/logging/CpfTransactionContext.java
- Implementation/Provider: cpf-core/src/main/java/com/cpf/core/service/observability/CpfOpenTelemetryAdapter.java; LoggingAspect; TransactionSegmentService; CpfTraceSamplingPolicy
- Consumer: HTTP interceptor, LoggingAspect, segment services; generic telemetry caller
- Tests/Evidence: trace-sampling-runtime-harness.log; trace-header-correlation-harness.log; segment/header tests
- Remaining difference: 특정 Local/Remote/Message/Batch/File span consumer 완결성, baggage allowlist, cardinality budget, 실제 OTLP exporter runtime 미검증

## Masking
- Group: 12, Requirement: 96, Scenario: 180
- Group result: 통과 0, 미통과 9, 미검증 3
- Public/API: cpf-core/src/main/java/com/cpf/core/api/security/CpfMasking.java; CpfSensitiveData.java
- Implementation/Provider: cpf-core/src/main/java/com/cpf/core/common/logging/SensitiveDataMasker.java; ADM security/download/log consumers
- Consumer: LoggingAspect; log export; evidence sanitization; admin detail/download consumers
- Tests/Evidence: masking-policy-runtime-harness.log; masking-truncate-before-after-harness.log; masking-bearer-before-after-harness.log
- Remaining difference: Raw View Approval·Browser zeroization·policy CAS/distribution/actual browser E2E 미검증

## File Contract
- Group: 20, Requirement: 160, Scenario: 300
- Group result: 통과 0, 미통과 3, 미검증 17
- Public/API: cpf-core/src/main/java/com/cpf/core/api/archive/*; api/attachment/*; api/filetransfer/*
- Implementation/Provider: cpf-starters/file/archive/.../LocalCpfArchiveService.java; cpf-starters/file/sftp/.../CpfFileTransferEngine.java; LocalCpfFileTransferAdapter.java
- Consumer: archive/attachment/SFTP gateway and admin/file consumers
- Tests/Evidence: CpfArchiveServiceTest; LocalCpfArchiveServiceStreamingTest; SFTP/local adapter tests (source inspected)
- Remaining difference: fsync, atomic-move unsupported fallback, real SFTP resume/range/cancel, disk-full/process-kill runtime 미검증

## Fixed-Length
- Group: 15, Requirement: 120, Scenario: 225
- Group result: 통과 0, 미통과 15, 미검증 0
- Public/API: cpf-core/src/main/java/com/cpf/core/api/fixedlength/*
- Implementation/Provider: cpf-starters/integration/fixedlength-core/.../CpfFixedLengthCodec.java; parser/writer/layout registry; fixedlength starter auto-config
- Consumer: fixed-length starter and generated/integration consumers
- Tests/Evidence: fixedlength-autoconfig-before-after-harness.log; fixed-length module tests/source
- Remaining difference: starter import baseline compile blocker patched; full known-vector/streaming/generated-domain runtime pending

## Database Lifecycle
- Group: 22, Requirement: 176, Scenario: 330
- Group result: 통과 0, 미통과 0, 미검증 22
- Public/API: cpf-core/src/main/java/com/cpf/core/api/database/*; cpf-tools/db vendor pack schema
- Implementation/Provider: cpf-tools/db/vendor/{oracle,postgresql,mariadb}/{install,migration,rollback,verify,runtime,seed}; pack.json
- Consumer: installer/upgrader/runtime SQL consumers and generated domains
- Tests/Evidence: db-pack-static-parity.log; vendor verification scripts/source
- Remaining difference: Oracle/PostgreSQL/MariaDB actual install→upgrade→rollback→reapply→restore runtime not executed

## Query Contract
- Group: 17, Requirement: 136, Scenario: 255
- Group result: 통과 0, 미통과 17, 미검증 0
- Public/API: cpf-core api/database/data inspected; no canonical versioned Query ID/parameter/result contract identified
- Implementation/Provider: persistence-jdbc routing/pool/health implementation inspected; no feature-complete Query Registry provider found
- Consumer: all DB consumers required by Acceptance; concrete universal consumer path not identified
- Tests/Evidence: query contract-specific runtime/fault suite not identified
- Remaining difference: 17 query feature contracts lack one canonical registry/provider/consumer/evidence chain

## Gateway
- Group: 37, Requirement: 296, Scenario: 555
- Group result: 통과 0, 미통과 0, 미검증 37
- Public/API: cpf-core/src/main/java/com/cpf/core/api/gateway/*
- Implementation/Provider: cpf-gateway/src/main/java/com/cpf/gateway/{control,registry,route,runtime,scg,ledger}/*
- Consumer: Spring Cloud Gateway runtime, control plane, route registry and ledger
- Tests/Evidence: cpf-gateway/src/test source and route/control tests (paths inspected)
- Remaining difference: actual TLS/client-cert, multi-gateway ACK/NACK/LKG/drift/rejoin, streaming loss and target recovery runtime not executed

## Event Contract
- Group: 20, Requirement: 160, Scenario: 300
- Group result: 통과 0, 미통과 13, 미검증 7
- Public/API: cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerMessage/Envelope/Result and api/broker/*
- Implementation/Provider: broker bridge, publisher/consumer workers, provider adapters
- Consumer: Kafka/Rabbit/JMS/IBM MQ bindings and Outbox/Inbox worker
- Tests/Evidence: broker core/provider/reliability-jdbc tests source inspected
- Remaining difference: envelope version/TTL/attribute+serializer allowlist/schema compatibility and actual consumer concurrency/multi-instance runtime incomplete

## Kafka
- Group: 16, Requirement: 128, Scenario: 240
- Group result: 통과 0, 미통과 11, 미검증 5
- Public/API: cpf-core api/common broker contracts
- Implementation/Provider: cpf-starters/messaging/kafka CpfKafkaAutoConfiguration, KafkaCpfBrokerClient/BridgeAdapter, consumer control
- Consumer: broker bridge/worker and Kafka bindings
- Tests/Evidence: CpfKafkaPropertiesTest; KafkaCpfBrokerClientTest
- Remaining difference: actual broker TLS/topic provisioning/transaction/consumer ACK-rebalance/backpressure/health/metrics runtime 미검증

## RabbitMQ AMQP
- Group: 16, Requirement: 128, Scenario: 240
- Group result: 통과 0, 미통과 11, 미검증 5
- Public/API: cpf-core broker contracts
- Implementation/Provider: CpfRabbitMqAutoConfiguration/Properties/BrokerClient
- Consumer: broker publisher/bridge
- Tests/Evidence: RabbitMQ starter tests source inspected
- Remaining difference: actual exchange/queue, TLS, consumer ACK/redelivery/backpressure/reconnect/metrics runtime 미검증

## Jakarta JMS
- Group: 16, Requirement: 128, Scenario: 240
- Group result: 통과 0, 미통과 11, 미검증 5
- Public/API: cpf-core broker contracts
- Implementation/Provider: CpfJmsAutoConfiguration/Properties/BrokerClient
- Consumer: provider-neutral broker publisher
- Tests/Evidence: JMS starter tests source inspected
- Remaining difference: actual provider destination/transaction/consumer ACK/redelivery/reconnect/metrics runtime 미검증

## IBM MQ
- Group: 16, Requirement: 128, Scenario: 240
- Group result: 통과 0, 미통과 11, 미검증 5
- Public/API: cpf-core broker contracts
- Implementation/Provider: CpfIbmMqAutoConfiguration/Properties/BrokerClient/ReasonCodeMapper
- Consumer: broker publisher
- Tests/Evidence: IBM MQ starter tests source inspected
- Remaining difference: actual queue manager TLS/transaction/consumer/reconnect/metrics runtime 미검증

## Outbox
- Group: 18, Requirement: 144, Scenario: 270
- Group result: 통과 0, 미통과 3, 미검증 15
- Public/API: CpfBrokerOutbox/Inbox/Dlq/Replay/Unknown ports
- Implementation/Provider: JdbcCpfBrokerReliabilityRepository; CpfBrokerPublisherWorker; CpfBrokerConsumerWorker; bridge adapter
- Consumer: all provider adapters and business broker handlers
- Tests/Evidence: JdbcCpfBrokerReliabilityRepositoryTest/Qa39Test; CpfBrokerWorkerTest; UnknownResultTest
- Remaining difference: actual 3-vendor concurrent claim/fencing, mandatory replay approval boundary, cleanup/timeline runtime incomplete

## REST
- Group: 25, Requirement: 200, Scenario: 375
- Group result: 통과 0, 미통과 3, 미검증 22
- Public/API: cpf-core api/http, api/filetransfer and TCP/file contracts
- Implementation/Provider: http-client CpfWebClientConfig/RestClientInterceptor; tcp client/server/frame/correlation; SFTP CpfFileExchangeGateway/Engine/Jdbc repository
- Consumer: external institution REST/TCP/SFTP consumers
- Tests/Evidence: HTTP interceptor, TCP simulator/frame, file transfer adapter tests source inspected
- Remaining difference: real institution endpoints, TLS/host key, half-open, response-loss, resume/checksum, callback/reconcile/manual-confirm runtime 미검증

## Spring Batch
- Group: 28, Requirement: 224, Scenario: 420
- Group result: 통과 0, 미통과 0, 미검증 28
- Public/API: cpf-core api/batch and cpf-batch/contract
- Implementation/Provider: cpf-batch/execution-runtime CpfBatchJobFactory/Tasklet/ExecutionControl/Partitioner/RemoteChunk/RemotePartition/JDBC adapters
- Consumer: cpf-batch worker/control-server/Kafka remote manager-worker
- Tests/Evidence: execution-runtime/testkit tests source inspected
- Remaining difference: actual Spring Batch repository DB, Kafka remote chunk/partition, process-kill/restart runtime 미검증

## Scheduler
- Group: 11, Requirement: 83, Scenario: 160
- Group result: 통과 0, 미통과 0, 미검증 11
- Public/API: cpf-batch contract/runtime policies
- Implementation/Provider: cpf-batch/scheduler SchedulerCoordinator/DispatchService/DbSchedulerPrimaryConfiguration/JdbcSchedulerLeaderRepository; host-agent AgentController/Ledger
- Consumer: scheduler, worker, host-agent
- Tests/Evidence: scheduler/host-agent tests source inspected
- Remaining difference: actual multi-instance lease contention, misfire/calendar/pause/resume, duplicate-fire prevention and heartbeat expiry runtime 미검증
