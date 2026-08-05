# Canonical Evidence Matrix

- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Session: `DEVGPT-V9-S03`
- 각 Canonical은 Source, 실제 Consumer, 전체 호출 경로, 정상·오류·복구, 직접/대체검증을 연결한다.

## API-LIMIT

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/runtime/InMemoryCpfGatewayRateLimitCounterAdapter.java; JdbcCpfGatewayRateLimitCounterAdapter.java; CpfGatewayRuntimePolicy.java; cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java`
- Default implementation: policy-driven rate limit with local/JDBC counters and standardized response headers
- Actual consumer: `gateway entry handler and GatewayOperationsPage.vue`
- Full call path: request identity/route → rate-limit policy → atomic counter → allow/429 headers → audit/metrics
- Normal: within-limit request consumes one bucket unit and continues
- Error/partial/UNKNOWN: exhausted/invalid policy returns deterministic 429/fail-closed response without downstream call
- Recovery/reconcile: window rollover or distributed counter recovery restores availability without over-admission
- Evidence group: `gateway`
- Remaining target environment: actual multi-instance JDBC counter/load/browser runtime

## CORE-FILE

- Source: `cpf-core/src/main/java/com/cpf/core/api/filetransfer/**; cpf-starters/file/sftp/src/main/java/com/cpf/starter/sftp/CpfSftpClient.java; CpfSftpPathPolicy.java; JdbcCpfSftpTransferLedger.java`
- Default implementation: safe path policy, resumable upload/download, SHA-256 verification, temporary remote/local target and atomic promotion, durable transfer ledger
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/filetransfer/ReferenceFileChecksumEducationSample.java; ReferenceSftpTransferPlanEducationSample.java`
- Full call path: reference/business file job → CpfSftpClient upload/download → path policy → remote channel → checksum/atomic rename → transfer ledger/result
- Normal: bytes, size and SHA-256 agree before final name becomes visible
- Error/partial/UNKNOWN: path escape, checksum mismatch and pre-command failure are deterministic; failure after remote mutation command starts remains UNKNOWN
- Recovery/reconcile: resume from verified offset or reconcile remote temporary/final path and ledger before retry
- Evidence group: `sftp_policy`
- Remaining target environment: actual SFTP server, DB ledger and process-kill runtime

## CORE-FIXED

- Source: `cpf-starters/integration/fixedlength-core/src/main/java/com/cpf/starter/integration/fixedlength/CpfFixedLengthCodec.java; CpfFixedLengthField.java; CpfBinaryFieldCodec.java; cpf-starters/integration/fixedlength/src/main/java/com/cpf/starter/fixedlength/CpfFixedLengthAutoConfiguration.java`
- Default implementation: byte-length aware layout/field codec, alignment/order validation, invalid BCD nibble rejection and starter auto-configuration
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/telegram/ReferenceFixedLengthBusinessUseEducationSample.java; generated/business telegram consumers`
- Full call path: ReferenceFixedLengthBusinessUseEducationSample → CpfFixedLengthCodec.encode/decode → CpfFixedLengthLayout/CpfFixedLengthField → byte payload/result
- Normal: valid layout and values encode to exact byte length and decode to the same logical values
- Error/partial/UNKNOWN: null alignment/order, byte overflow, invalid BCD nibble, malformed input fail before downstream I/O with field-level error
- Recovery/reconcile: codec is deterministic and side-effect free; caller corrects layout/input and safely retries
- Evidence group: `fixedlength`
- Remaining target environment: Java25/generated-domain compatibility runtime

## CORE-MESSAGE

- Source: `cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: broker envelope/result contracts, idempotency/outbox/inbox ports, header policy, provider routing and reliability worker
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider;kafka;fencing`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-AMQP

- Source: `cpf-starters/messaging/rabbitmq/src/main/java/com/cpf/starter/rabbitmq/CpfRabbitMqBrokerClient.java; cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: RabbitMQ AMQP provider adapter plus common reliability/header/fencing contracts
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-BROKER

- Source: `cpf-starters/messaging/kafka/src/main/java/com/cpf/starter/kafka/KafkaCpfBrokerClient.java; JMS/IBM MQ/RabbitMQ adapters; cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: broker-neutral router with provider-specific outcome normalization
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider;kafka`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-CORE

- Source: `cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: core event/broker contracts, immutable envelope, idempotency and reliability orchestration
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider;fencing`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-DLQ

- Source: `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqPort.java; CpfBrokerDlqReplayRequest.java; CpfBrokerDlqReplayResult.java; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: DLQ history/replay contract integrated with reliable broker state and audit
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `fencing;provider`
- Remaining target environment: actual DLQ broker/DB/replay authorization runtime

## EVENT-IBM-MQ

- Source: `cpf-starters/messaging/ibm-mq/src/main/java/com/cpf/starter/ibmmq/CpfIbmMqBrokerClient.java; cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: IBM MQ/JMS provider adapter plus common reliability/header/fencing contracts
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-JMS

- Source: `cpf-starters/messaging/jms/src/main/java/com/cpf/starter/jms/CpfJmsBrokerClient.java; cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: JMS provider adapter plus common reliability/header/fencing contracts
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-MQ

- Source: `cpf-starters/messaging/jms/**; cpf-starters/messaging/ibm-mq/**; cpf-core/src/main/java/com/cpf/core/common/broker/**; cpf-core/src/main/java/com/cpf/core/api/broker/**; cpf-starters/messaging/reliability-jdbc/**`
- Default implementation: MQ-neutral routing with JMS/IBM MQ provider adapters
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `provider_header;provider`
- Remaining target environment: actual broker, DB and multi-instance worker runtime

## EVENT-OUTBOX

- Source: `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerOutboxPort.java; cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/core/common/broker/CpfBrokerPublisherWorker.java; JdbcCpfBrokerReliabilityRepository.java`
- Default implementation: fenced outbox claim/publish/finalize worker and durable JDBC reliability repository
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/messaging/controller/ReferenceMessagingEducationController.java; ReferenceBrokerPublishEducationSample.java`
- Full call path: reference/business publisher → reliable client/router → provider adapter → broker; worker/reconciler → outbox/UNKNOWN repositories
- Normal: immutable validated envelope is delivered or durably queued with tracking ID
- Error/partial/UNKNOWN: invalid/reserved headers fail pre-I/O; post-provider ambiguity is UNKNOWN; duplicate consume is idempotent
- Recovery/reconcile: outbox retry, fenced UNKNOWN reconcile and DLQ/replay with audit
- Evidence group: `fencing;provider;kafka`
- Remaining target environment: MariaDB/PostgreSQL/Oracle multi-instance outbox runtime

## EXS-FILE

- Source: `cpf-starters/file/sftp/src/main/java/com/cpf/starter/sftp/CpfSftpClient.java; CpfSftpUploadOutcomePolicy.java; JdbcCpfSftpTransferLedger.java`
- Default implementation: external SFTP transfer plan with mutation-boundary UNKNOWN semantics, checksum and atomic publish
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/filetransfer/ReferenceSftpTransferPlanEducationSample.java`
- Full call path: external transfer request → path/credential policy → temp upload/resume → checksum → atomic rename → ledger/audit response
- Normal: non-resume upload is invisible until checksum-verified atomic rename; resume continues from agreed offset
- Error/partial/UNKNOWN: remote OPEN/CREATE/TRUNCATE/write/rename ambiguity is UNKNOWN, never false FAILED
- Recovery/reconcile: remote stat/checksum plus ledger reconciliation determines retry/resume/complete action
- Evidence group: `sftp_policy`
- Remaining target environment: actual SFTP provider and network fault injection

## EXS-FIXED

- Source: `cpf-starters/integration/fixedlength-core/src/main/java/com/cpf/starter/integration/fixedlength/CpfFixedLengthCodec.java; cpf-starters/integration/tcp/src/main/java/com/cpf/starter/tcp/CpfTcpFrameCodec.java`
- Default implementation: fixed-length telegram validation combined with framed TCP transport boundary
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/telegram/ReferenceFixedLengthBusinessUseEducationSample.java; external TCP adapter consumers`
- Full call path: business DTO → fixed-length encode → TCP frame encode/write → peer → frame decode → fixed-length decode → response DTO
- Normal: exact field bytes and frame length are preserved end-to-end
- Error/partial/UNKNOWN: invalid field length/framing fails before dispatch; post-dispatch transport loss is not collapsed to deterministic failure
- Recovery/reconcile: pre-dispatch correction/retry; post-dispatch UNKNOWN record and owner reconciliation
- Evidence group: `fixedlength;tcp`
- Remaining target environment: actual external fixed-length peer/TCP fault runtime

## EXS-INST

- Source: `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryCatalog.java; CpfServiceRegistryControlPort.java; CpfServiceRegistryQueryPort.java; cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayTargetSelector.java`
- Default implementation: service instance catalog, enabled/health/version metadata and deterministic target selection
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoRemoteClient.java; cpf-gateway runtime target resolver`
- Full call path: service caller/gateway → registry query/catalog → target selector → pinned instance → transport
- Normal: only eligible healthy instance with matching contract/version is selected
- Error/partial/UNKNOWN: empty/stale/disabled instance set fails closed before transport; concurrent changes use snapshot/version rules
- Recovery/reconcile: registry refresh, health reevaluation and versioned snapshot replacement restore routing
- Evidence group: `http;gateway`
- Remaining target environment: multi-instance registry discovery/runtime

## EXS-RECON

- Source: `cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfBrokerUnknownResultReconciler.java; CpfTcpUnknownResultStore.java; CpfSftpClient.java; core service-call outcome/probe contracts`
- Default implementation: fenced claim/lease, owner probe, compare-and-set finalization and retry-safe reconciliation
- Actual consumer: `broker reliability worker/operator flow, TCP/SFTP external integration operation ledger and ADM recovery surfaces`
- Full call path: UNKNOWN ledger query → fenced claim → owner/provider probe → observed state → CAS finalize/retry/release → history/audit
- Normal: single claimant resolves UNKNOWN to evidence-backed final state
- Error/partial/UNKNOWN: probe unavailable/ambiguous keeps UNKNOWN and releases/renews lease; stale fence cannot mutate state
- Recovery/reconcile: lease expiry/reclaim and repeated probe converge without duplicate external side effect
- Evidence group: `fencing;tcp;sftp_policy;provider`
- Remaining target environment: actual DB multi-instance reconcile and provider probe runtime

## EXS-REST

- Source: `cpf-starters/integration/http/src/main/java/com/cpf/starter/integration/http/CpfTypedHttpClient.java; cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceCallExecutor.java`
- Default implementation: typed HTTP request, timeout/deadline, bounded response, deterministic transport classification and service-call outcome contract
- Actual consumer: `cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoRemoteClient.java; ReferenceServiceCallEngineEducationSample.java; cpf-reference/src/main/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorController.java`
- Full call path: reference/business caller → CpfServiceCallExecutor/CpfTypedHttpClient → resolved target → HTTP provider → typed response/outcome → caller
- Normal: validated request receives bounded typed response with metadata and trace context
- Error/partial/UNKNOWN: DNS/connect/pre-dispatch errors are deterministic; response loss/deadline after dispatch is UNKNOWN according to operation semantics
- Recovery/reconcile: idempotent read retry or idempotency-key protected write reconciliation before retry
- Evidence group: `http`
- Remaining target environment: actual HTTPS/mTLS provider and network fault runtime

## EXS-SEC

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfApiClientSecurityPolicy.java; CpfApiClientSecurityRuntimeApplier.java; cpf-starters/integration/http/src/main/java/com/cpf/starter/integration/http/CpfTypedHttpClient.java; cpf-starters/file/sftp/src/main/java/com/cpf/starter/sftp/CpfSftpPathPolicy.java`
- Default implementation: endpoint trust, TLS/mTLS policy, header/path validation, secret-safe failure detail and fail-closed runtime application
- Actual consumer: `gateway/external HTTP, TCP and SFTP transport consumers`
- Full call path: request/config → security policy validation → credential/TLS context → transport → sanitized audit/response
- Normal: authorized trusted target is called without exposing credentials
- Error/partial/UNKNOWN: untrusted target, path escape, invalid certificate/config and reserved metadata fail before I/O
- Recovery/reconcile: rotate/reload approved secret/trust material through versioned config and audit; never log raw secret
- Evidence group: `http;gateway;sftp_policy`
- Remaining target environment: actual mTLS/secret rotation and certificate-failure runtime

## EXS-TCP

- Source: `cpf-starters/integration/tcp/src/main/java/com/cpf/starter/tcp/CpfTcpClient.java; CpfTcpFrameCodec.java; CpfTcpUnknownResultStore.java; com/cpf/starter/integration/tcp/internal/CpfResilientTcpClient.java`
- Default implementation: framed request/response TCP client, correlation, TLS/reconnect policy and durable UNKNOWN storage
- Actual consumer: `external TCP adapter/business caller through CpfTcpOperations and reference external integration flow`
- Full call path: business request → CpfTcpClient.exchange → frame encode → socket write → peer response/frame decode → correlation/result; ambiguity → CpfTcpUnknownResultStore
- Normal: complete frame write and correlated response return success exactly once
- Error/partial/UNKNOWN: connect/pre-write error is deterministic; partial write, response loss and half-open after dispatch are UNKNOWN
- Recovery/reconcile: durable UNKNOWN record is reconciled by owner lookup/correlation before idempotent resend
- Evidence group: `tcp;tcp_primary_clock`
- Remaining target environment: actual TLS TCP peer, half-open/process-kill fault runtime

## EXS-UNKNOWN

- Source: `CpfTypedHttpClient; cpf-starters/integration/tcp/src/main/java/com/cpf/starter/tcp/CpfTcpClient.java; cpf-starters/file/sftp/src/main/java/com/cpf/starter/sftp/CpfSftpClient.java; cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfProviderBrokerPublisher.java`
- Default implementation: transport-specific dispatch boundary and UNKNOWN preservation after possible external side effect
- Actual consumer: `reference HTTP/TCP/SFTP/Messaging flows and operation ledgers`
- Full call path: consumer → transport dispatch → possible remote side effect → lost/ambiguous response → UNKNOWN ledger/result → reconciliation
- Normal: confirmed response/ACK is recorded as final success/failure only with proof
- Error/partial/UNKNOWN: partial write, provider runtime exception after invocation, response/ACK loss and rename ambiguity remain UNKNOWN
- Recovery/reconcile: owner probe/callback/remote stat/checksum/broker probe resolves actual state before retry
- Evidence group: `http;tcp;sftp_policy;provider;kafka`
- Remaining target environment: actual provider response-loss and process-kill runtime

## GWY-ENTRY

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java; CpfScgPrimaryRouteConfiguration.java; cpf-gateway/src/main/java/com/cpf/gateway/runtime/DefaultCpfGatewayEntryPolicy.java`
- Default implementation: primary SCG entry policy, authentication/authorization/rate-limit/audit preconditions and downstream exchange
- Actual consumer: `Spring Cloud Gateway route → CpfScgPrimaryHandler; cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue`
- Full call path: HTTP request → SCG primary route → entry policy/security/rate limit → target resolution → downstream → completion ledger/audit
- Normal: accepted request is traced, policy-checked and forwarded once
- Error/partial/UNKNOWN: invalid/auth/rate/target errors fail with controlled status and no unsafe downstream call
- Recovery/reconcile: durable audit/ledger recovery spool and policy snapshot refresh
- Evidence group: `gateway`
- Remaining target environment: Java25 SCG/browser/downstream runtime

## GWY-RESILIENCE

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/internal/resilience/CpfGatewayResilientInvoker.java; cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayHealthEvaluator.java`
- Default implementation: explicit READ/WRITE/UNKNOWN operation semantics, deadline-aware retry and idempotency-protected write policy
- Actual consumer: `gateway downstream invocation path/CpfScgPrimaryHandler`
- Full call path: gateway request → operation classification → resilient invoker → downstream attempt(s) → confirmed result or UNKNOWN
- Normal: READ may retry within deadline; WRITE retries only with idempotency key and safe failure phase
- Error/partial/UNKNOWN: post-dispatch timeout/response loss becomes UNKNOWN; deadline exhaustion stops retries
- Recovery/reconcile: owner reconciliation determines write result; health/circuit state controls later attempts
- Evidence group: `gateway`
- Remaining target environment: actual SCG downstream timeout/circuit/runtime

## GWY-ROUTING

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayRouteSnapshot.java; CpfGatewayPathRewriter.java; cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayRouteSynchronizer.java; CpfGatewayTargetSelector.java; cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java`
- Default implementation: versioned immutable route snapshot, validated path rewrite, healthy target selection and runtime synchronization
- Actual consumer: `CpfScgPrimaryHandler and gateway operations ADM page`
- Full call path: request → route snapshot match → path rewrite → target selector/resolver → pinned downstream → response
- Normal: one immutable route version and target are used for the full request
- Error/partial/UNKNOWN: missing/conflicting route, traversal rewrite, unhealthy target and stale update fail closed
- Recovery/reconcile: versioned refresh/CAS replacement and last-known-good snapshot preserve service
- Evidence group: `gateway`
- Remaining target environment: multi-instance route distribution/browser runtime

## GWY-TRUST

- Source: `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfApiClientSecurityPolicy.java; CpfApiClientSecurityRuntimeApplier.java; cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedAddressContext.java; CpfGatewayPinnedHttpClientConfiguration.java`
- Default implementation: trusted endpoint/address pinning, TLS policy and request-scoped immutable target context
- Actual consumer: `CpfScgPrimaryHandler/CpfScgTargetResolver downstream client path`
- Full call path: route target → trust policy/DNS resolution → pinned address context → configured HTTP client → downstream
- Normal: validated host and pinned resolved address remain consistent through connect
- Error/partial/UNKNOWN: SSRF/private-range/host mismatch/rebinding and invalid TLS policy fail before downstream I/O
- Recovery/reconcile: approved policy/target snapshot refresh with audit; no implicit trust downgrade
- Evidence group: `gateway;http`
- Remaining target environment: actual DNS rebinding/mTLS/network runtime

