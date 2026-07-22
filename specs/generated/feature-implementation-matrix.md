# 기능 구현 매트릭스

## 기준

- SHA: `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`
- 정본 domain: 133개
- inventory file: 1,118개
- 경로 존재는 완료 근거가 아니다.

## 상태 합계

- 재확인 필요: 77
- 부분 구현: 30
- 미구현: 19
- 미검증: 5
- 실패: 2

## 도메인별 상태

### `ARCH-MISSION` — CPF 최종 목표/상용 솔루션 원칙

- 상태: `재확인 필요`
- inventory: source 0, test 0, SQL 0, script 2
- 확인 시작 파일:
  - `cpf-core/build.gradle`
  - `cpf-common/build.gradle`
  - `cpf-admin/build.gradle`

### `ARCH-MSA` — MSA-first 및 Modular Monolith 호환

- 상태: `재확인 필요`
- inventory: source 80, test 20, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/LocalCpfArchiveService.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/LocalCpfAttachmentStorageAdapter.java`

### `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context

- 상태: `재확인 필요`
- inventory: source 76, test 15, SQL 2, script 8
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/reliability/CpfReliabilityOperationsPort.java`

### `ARCH-LAYER` — 계층/패키지/의존성 규칙

- 상태: `재확인 필요`
- inventory: source 64, test 16, SQL 1, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/BaseController.java`

### `FACADE-LOCAL` — Local Facade 표준

- 상태: `재확인 필요`
- inventory: source 19, test 7, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/LocalCpfArchiveService.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfApplicationFacade.java`

### `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter

- 상태: `재확인 필요`
- inventory: source 114, test 19, SQL 1, script 7
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/reliability/CpfReliabilityOperationsPort.java`

### `CPF-CALL` — CpfWebClient/CpfRestClient Service Call Engine

- 상태: `재확인 필요`
- inventory: source 42, test 8, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfHttpClientProperties.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfLocalServiceIdentity.java`

### `CPF-REGISTRY` — Service/Endpoint/Instance Registry

- 상태: `재확인 필요`
- inventory: source 17, test 1, SQL 4, script 4
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/channel/api/CpfChannelRegistryPort.java`

### `CPF-ROUTING` — LB mode/direct instance/discovery routing

- 상태: `재확인 필요`
- inventory: source 10, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferEndpoint.java`
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRoute.java`
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java`

### `CPF-HEALTH` — Liveness/Readiness/Dependency Health

- 상태: `재확인 필요`
- inventory: source 10, test 2, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfRuntimeHealthStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerHealthPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferHealthPort.java`

### `CPF-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure

- 상태: `재확인 필요`
- inventory: source 3, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferRetryPolicy.java`
  - `cpf-batch/src/main/java/com/cpf/batch/edu/centercut/BatCenterCutRetryEducationSample.java`
  - `cpf-batch/src/main/java/com/cpf/batch/edu/retry/BatRetryEducationSample.java`

### `CPF-DEADLINE` — Request Deadline/Timeout Budget

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `CPF-HEADER` — 표준/확장 헤더

- 상태: `재확인 필요`
- inventory: source 22, test 8, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfExtensionHeaderPolicy.java`
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderAuditLogger.java`
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderCategory.java`

### `CPF-CONTEXT` — TransactionContext/MDC/Thread Context

- 상태: `재확인 필요`
- inventory: source 5, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/CpfTransactionContextAnomalyMonitor.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/TransactionContext.java`

### `CPF-TXID` — transactionGlobalId/segment/timeline

- 상태: `재확인 필요`
- inventory: source 18, test 2, SQL 3, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `CPF-ROLE` — transactionRole/direction/source-target

- 상태: `재확인 필요`
- inventory: source 17, test 6, SQL 0, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutTargetProvider.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CpfCenterCutTarget.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/policy/LogPolicyTargetType.java`

### `CPF-OPSDB` — CPF 운영 DB 공유/장애모드

- 상태: `재확인 필요`
- inventory: source 26, test 9, SQL 4, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/reliability/CpfReliabilityOperationsPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchOperationRepository.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchOperationType.java`

### `CPF-LOGDB` — DB 로그/Segment 로그

- 상태: `부분 구현`
- inventory: source 51, test 14, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchLoggingEventPublisher.java`

### `CPF-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그

- 상태: `미검증`
- inventory: source 52, test 15, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchFileLogWriter.java`

### `CPF-LOGFAIL` — 로그 실패/fail-open/local spool

- 상태: `부분 구현`
- inventory: source 52, test 14, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchLoggingEventPublisher.java`

### `CPF-TRACE` — Trace Boost/동적 로그 레벨

- 상태: `재확인 필요`
- inventory: source 13, test 1, SQL 2, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/CpfLogLevel.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/DynamicLogLevelRequest.java`

### `CPF-MASK` — 마스킹/민감정보 보호

- 상태: `재확인 필요`
- inventory: source 5, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderMasker.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/SensitiveDataMasker.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthMaskingRule.java`

### `CPF-ERROR` — 오류/예외/응답 표준

- 상태: `재확인 필요`
- inventory: source 49, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfResponse.java`
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfBusinessException.java`
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfDynamicErrorCode.java`

### `CPF-VALID` — Validation Framework

- 상태: `재확인 필요`
- inventory: source 11, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfValidationException.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileChecksumValidationResult.java`
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java`

### `CPF-IDEMP` — Idempotency 표준

- 상태: `재확인 필요`
- inventory: source 14, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerIdempotencyPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/idempotency/CpfIdempotencyCommand.java`
  - `cpf-core/src/main/java/com/cpf/core/common/idempotency/CpfIdempotencyEngine.java`

### `CPF-STATE` — 상태 전이 State Machine

- 상태: `재확인 필요`
- inventory: source 8, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/workflow/CpfWorkflow.java`
  - `cpf-core/src/main/java/com/cpf/core/common/workflow/CpfWorkflowContext.java`
  - `cpf-core/src/main/java/com/cpf/core/common/workflow/CpfWorkflowFailurePolicy.java`

### `CPF-LOCK` — Optimistic/Distributed Lock

- 상태: `재확인 필요`
- inventory: source 6, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchLockManager.java`
  - `cpf-core/src/main/java/com/cpf/core/common/runtime/CpfDistributedLockPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/runtime/CpfLockAcquireRequest.java`

### `CPF-SCHED` — Scheduler 표준

- 상태: `부분 구현`
- inventory: source 3, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/CpfBatchScheduleCandidate.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduler.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduleService.java`

### `CMN-CODE` — 공통코드/참조데이터

- 상태: `재확인 필요`
- inventory: source 26, test 3, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfDynamicErrorCode.java`
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfErrorCode.java`
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfFrameworkErrorCode.java`

### `CMN-MSG` — 공통 메시지/다국어/오류 메시지

- 상태: `재확인 필요`
- inventory: source 34, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeMessage.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerMessage.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerMessageHandler.java`

### `CMN-ID` — 채번/분산 ID

- 상태: `재확인 필요`
- inventory: source 4, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java`
  - `cpf-common/src/main/java/com/cpf/common/biz/sequence/CmnSequenceIssueRequest.java`
  - `cpf-common/src/main/java/com/cpf/common/biz/sequence/CmnSequenceIssueResult.java`

### `CMN-FILE` — 파일/다운로드/업로드/Object Storage

- 상태: `재확인 필요`
- inventory: source 54, test 14, SQL 1, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfFileTransferStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/CpfAttachmentContent.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/CpfAttachmentStoragePort.java`

### `CMN-FIXED` — 고정길이 전문 parser/formatter/layout

- 상태: `재확인 필요`
- inventory: source 24, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfMessageFormatter.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthAlignment.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthFieldSpec.java`

### `CMN-CALENDAR` — 영업일/휴일/기관 캘린더

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmBusinessDayRequest.java`

### `CMN-TEMPLATE` — 템플릿/알림

- 상태: `부분 구현`
- inventory: source 12, test 0, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-common/src/main/java/com/cpf/common/biz/notification/CmnNotificationLogRequest.java`
  - `cpf-common/src/main/java/com/cpf/common/biz/notification/CmnNotificationLogResult.java`
  - `cpf-common/src/main/java/com/cpf/common/biz/notification/CmnNotificationLogService.java`

### `ADM-AUTH` — ADM 인증/세션/계정 생명주기

- 상태: `재확인 필요`
- inventory: source 23, test 6, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cpf-common/src/main/java/com/cpf/common/sec/token/CmnOAuthBearerTokenService.java`
  - `cpf-common/src/main/java/com/cpf/common/sec/token/CmnOAuthTokenIntrospectionResult.java`

### `ADM-RBAC` — RBAC/ABAC/권한 메타

- 상태: `재확인 필요`
- inventory: source 17, test 3, SQL 2, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentRole.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmPermissionController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmApiPermission.java`

### `ADM-AUDIT` — 감사/보안 이벤트/부인방지

- 상태: `재확인 필요`
- inventory: source 7, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderAuditLogger.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmAuditLogController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmLogPolicyAuditController.java`

### `ADM-TX` — 거래 그룹 목록/상세

- 상태: `재확인 필요`
- inventory: source 129, test 34, SQL 10, script 7
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchFileLogWriter.java`

### `ADM-TIMELINE` — 통합 Timeline

- 상태: `재확인 필요`
- inventory: source 14, test 1, SQL 3, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTransactionTimelineQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/TransactionSegmentFallbackStore.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`

### `ADM-SERVICE` — Service Instance/Routing/Health 관제

- 상태: `재확인 필요`
- inventory: source 136, test 42, SQL 5, script 10
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/channel/application/CpfChannelPolicyService.java`
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfRuntimeHealthStatusQuery.java`

### `ADM-LOG` — 로그 정책/Trace Boost 화면

- 상태: `재확인 필요`
- inventory: source 43, test 6, SQL 2, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/CpfLogLevel.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/DynamicLogLevelRequest.java`

### `ADM-BATCH` — Batch/Worker/Heartbeat/Ghost 관제

- 상태: `부분 구현`
- inventory: source 57, test 10, SQL 8, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEvent.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventPublisher.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventType.java`

### `ADM-CENTER` — Center-Cut 관제

- 상태: `재확인 필요`
- inventory: source 25, test 11, SQL 3, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutHandler.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutTargetProvider.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CpfCenterCutResult.java`

### `ADM-EXS` — 대외연계 관제

- 상태: `재확인 필요`
- inventory: source 10, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfExternalServiceException.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferEndpoint.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfServiceEndpointProperties.java`

### `ADM-COMP` — Compensation/Manual Recovery 관제

- 상태: `부분 구현`
- inventory: source 6, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/TransactionLogRecoveryWorker.java`

### `ADM-INCIDENT` — Incident/Alert/Runbook

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `ADM-UX` — ADM UX/검색/다운로드/대시보드

- 상태: `미구현`
- inventory: source 7, test 1, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmDownloadController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/DownloadAuditLog.java`

### `BAT-CORE` — BAT standalone worker/application

- 상태: `미구현`
- inventory: source 14, test 1, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchHeartbeatService.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerConsumerWorker.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerPublisherWorker.java`

### `BAT-JOB` — Job/Step/Parameter/Dependency

- 상태: `재확인 필요`
- inventory: source 25, test 8, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfJobSupport.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfStepSupport.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchJobLogPath.java`

### `BAT-ITEM` — Batch item claim/retry/skip/rerun

- 상태: `재확인 필요`
- inventory: source 6, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferRetryPolicy.java`
  - `cpf-batch/src/main/java/com/cpf/batch/edu/centercut/BatCenterCutItemProcessingEducationSample.java`
  - `cpf-batch/src/main/java/com/cpf/batch/edu/centercut/BatCenterCutRetryEducationSample.java`

### `BAT-CALL-SYNC` — BAT → 주제영역 동기 호출

- 상태: `재확인 필요`
- inventory: source 43, test 9, SQL 0, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfApplicationFacade.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `BAT-CALL-ASYNC` — BAT → Event/Outbox 비동기 호출

- 상태: `재확인 필요`
- inventory: source 11, test 1, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEvent.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventPublisher.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventType.java`

### `BAT-SHARED` — BAT → SHARED/온라인 Facade 재사용

- 상태: `재확인 필요`
- inventory: source 13, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfApplicationFacade.java`
  - `cpf-core/src/main/java/com/cpf/core/common/execution/CpfSharedApi.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `CENTER-CORE` — Center-Cut 기본 구현

- 상태: `재확인 필요`
- inventory: source 25, test 11, SQL 3, script 2
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutHandler.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutTargetProvider.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CpfCenterCutResult.java`

### `CENTER-ADV` — Center-Cut 고급 패턴

- 상태: `재확인 필요`
- inventory: source 26, test 11, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutHandler.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CenterCutTargetProvider.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CpfCenterCutResult.java`

### `EXS-INST` — 기관/Endpoint/Profile

- 상태: `부분 구현`
- inventory: source 5, test 0, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferEndpoint.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfServiceEndpointProperties.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java`

### `EXS-REST` — EXS REST 송수신

- 상태: `재확인 필요`
- inventory: source 12, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/CpfExternalServiceException.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfHttpClientProperties.java`
  - `cpf-core/src/main/java/com/cpf/core/common/http/CpfLocalServiceIdentity.java`

### `EXS-FIXED` — EXS 고정길이 전문 송수신

- 상태: `재확인 필요`
- inventory: source 23, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthAlignment.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthFieldSpec.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthFieldType.java`

### `EXS-SEC` — EXS OAuth/JWT/mTLS/인증서

- 상태: `부분 구현`
- inventory: source 12, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfCredentialStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogServiceCredentialPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCertificateProviderPort.java`

### `EXS-UNKNOWN` — Unknown Result 처리

- 상태: `부분 구현`
- inventory: source 35, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveResult.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchExecutionResult.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/centercut/CpfCenterCutResult.java`

### `EXS-RECON` — 대외 Reconciliation

- 상태: `부분 구현`
- inventory: source 5, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/CpfReconciliationPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/CpfUnknownResultRecord.java`
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/JdbcCpfReconciliationRepository.java`

### `EXS-FILE` — SFTP/File Transfer 연계 후보

- 상태: `부분 구현`
- inventory: source 30, test 6, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfFileTransferStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfDuplicatePreventionPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileChecksumPolicy.java`

### `EVENT-CORE` — Event/Message Envelope

- 상태: `재확인 필요`
- inventory: source 46, test 1, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEvent.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventPublisher.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchEventType.java`

### `EVENT-OUTBOX` — Outbox/Inbox

- 상태: `재확인 필요`
- inventory: source 3, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerInboxPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerOutboxPort.java`
  - `cpf-reference/src/main/java/com/cpf/reference/messaging/ReferenceOutboxInboxEducationSample.java`

### `EVENT-BROKER` — Kafka/MQ/Redis real broker

- 상태: `미검증`
- inventory: source 30, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfBrokerStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeHandler.java`

### `EVENT-DLQ` — DLQ/Replay/Poison Message

- 상태: `부분 구현`
- inventory: source 4, test 0, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqReplayRequest.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqReplayResult.java`

### `SAGA-CORE` — Saga/분산 거래 표준

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 0

### `SAGA-COMP` — Compensation

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 0

### `SAGA-MANUAL` — Manual Recovery/Adjustment

- 상태: `부분 구현`
- inventory: source 6, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/TransactionLogRecoveryWorker.java`

### `SEC-AUTHN` — 인증/AuthN

- 상태: `재확인 필요`
- inventory: source 18, test 5, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cpf-common/src/main/java/com/cpf/common/sec/token/CmnJwtCreateRequest.java`
  - `cpf-common/src/main/java/com/cpf/common/sec/token/CmnJwtService.java`

### `SEC-AUTHZ` — 권한/AuthZ/RBAC/ABAC

- 상태: `재확인 필요`
- inventory: source 18, test 3, SQL 2, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentRole.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmPermissionController.java`

### `SEC-SECRET` — Secret/Vault/Key Rotation

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfSecretProviderPort.java`

### `SEC-CERT` — Certificate/mTLS 관리

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCertificateProviderPort.java`

### `SEC-PRIVACY` — 개인정보/Privacy/Data Classification

- 상태: `재확인 필요`
- inventory: source 5, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderMasker.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/SensitiveDataMasker.java`
  - `cpf-common/src/main/java/com/cpf/common/message/fixedlength/FixedLengthMaskingRule.java`

### `SEC-DOWNLOAD` — Download Governance

- 상태: `재확인 필요`
- inventory: source 7, test 1, SQL 1, script 5
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmDownloadController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/DownloadAuditLog.java`

### `SEC-APP` — Application Security 기본 통제

- 상태: `재확인 필요`
- inventory: source 23, test 3, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCertificateProviderPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialProviderPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialRef.java`

### `SEC-APPROVAL` — Approval/Dual Control/Break-glass

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-METRIC` — Metrics/Observability

- 상태: `재확인 필요`
- inventory: source 3, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmObservabilityController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java`
  - `cpf-admin/src/test/java/com/cpf/admin/opr/service/AdmObservabilityServiceTest.java`

### `OPS-SLO` — SLI/SLO/Error Budget

- 상태: `미구현`
- inventory: source 4, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfZipSlipGuard.java`
  - `cpf-core/src/test/java/com/cpf/core/common/archive/CpfZipSlipGuardTest.java`
  - `cpf-common/src/main/java/com/cpf/common/biz/log/CmnBusinessLogRequest.java`

### `OPS-ALERT` — Alert Rule Engine

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-INCIDENT` — Incident Management

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-RUNBOOK` — Runbook/자동 진단

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 1
- 확인 시작 파일:
  - `scripts/runtime-diagnostics.ps1`

### `OPS-SELF` — Self-healing

- 상태: `미구현`
- inventory: source 6, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/logging/CpfTraceRecoveryPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `cpf-core/src/main/java/com/cpf/core/common/logging/fallback/TransactionLogRecoveryWorker.java`

### `OPS-TOPOLOGY` — Topology/Dependency Map/Service Catalog

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-MAINT` — Maintenance Mode/Drain

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-CONFIG` — Config/Policy/Runtime Override

- 상태: `재확인 필요`
- inventory: source 82, test 14, SQL 4, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/channel/application/CpfChannelPolicyService.java`
  - `cpf-core/src/main/java/com/cpf/core/channel/model/CpfChannelExecutionPolicy.java`
  - `cpf-core/src/main/java/com/cpf/core/channel/model/CpfChannelPolicyDecision.java`

### `OPS-DRIFT` — Config Drift/Policy Versioning

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/version/CpfPlatformVersion.java`

### `OPS-CAPACITY` — Performance/Capacity/Resource Governance

- 상태: `재확인 필요`
- inventory: source 0, test 2, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/resources/application-cpf-dev.yml`
  - `cpf-core/src/main/resources/application-cpf-local.yml`
  - `cpf-core/src/main/resources/application-cpf-prod.yml`

### `OPS-DR` — DR/Backup/Restore/Archive

- 상태: `부분 구현`
- inventory: source 11, test 5, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveChecksum.java`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveEntry.java`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveFormat.java`

### `REL-BUILD` — Build/Artifact/Provenance

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/build.gradle`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveChecksum.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileChecksumPolicy.java`

### `REL-DEPLOY` — Deployment/Rollback/Feature Flag

- 상태: `부분 구현`
- inventory: source 1, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-batch/src/main/java/com/cpf/batch/edu/transaction/BatPartialRollbackEducationSample.java`
  - `cpf-batch/src/test/java/com/cpf/batch/edu/transaction/BatPartialRollbackEducationSampleTest.java`
  - `deploy/inventory/dev-services.json`

### `REL-MIG` — DB Migration/Flyway/Expand-Contract

- 상태: `부분 구현`
- inventory: source 1, test 3, SQL 35, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfMapperContract.java`
  - `cpf-core/src/test/java/com/cpf/core/common/base/CpfBaseContractTest.java`
  - `cpf-core/src/test/java/com/cpf/core/common/capability/CpfCapabilityContractTest.java`

### `REL-COMPAT` — API/Event/전문 호환성

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/version/CpfPlatformVersion.java`

### `DB-SQL` — SQL 표준/MyBatis/Index

- 상태: `재확인 필요`
- inventory: source 15, test 4, SQL 51, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfMapperContract.java`
  - `cpf-core/src/main/java/com/cpf/core/config/CpfMyBatisConfig.java`
  - `cpf-core/src/main/java/com/cpf/core/mapper/common/logging/TransactionLogMapper.java`

### `DB-INSTALL` — MariaDB 신규 빈 DB Full Install

- 상태: `재확인 필요`
- inventory: source 7, test 2, SQL 4, script 34
- 확인 시작 파일:
  - `acc/smoke/smoke-acc.ps1`
  - `cpf-batch/src/main/java/com/cpf/batch/job/centercut/BatCenterCutSmokeTasklet.java`
  - `cpf-batch/src/main/java/com/cpf/batch/job/heartbeat/BatHeartbeatSmokeTasklet.java`

### `DB-PERF` — DB 성능/Partition/Retention

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-admin/src/main/resources/static/adm/index.html`
  - `cpf-biz-admin/src/main/resources/static/bza/index.html`

### `DB-MULTI` — Multi Datasource/Read Replica

- 상태: `부분 구현`
- inventory: source 7, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/config/CpfDataSourceConfig.java`
  - `cpf-core/src/test/java/com/cpf/core/config/CpfDataSourceConfigTest.java`
  - `cpf-common/src/main/java/com/cpf/common/config/CmnBusinessDataSourceConfig.java`

### `DATA-LINEAGE` — Data Lineage/Quality/Reconciliation

- 상태: `재확인 필요`
- inventory: source 5, test 3, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/CpfReconciliationPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/CpfUnknownResultRecord.java`
  - `cpf-core/src/main/java/com/cpf/core/common/reconciliation/JdbcCpfReconciliationRepository.java`

### `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive

- 상태: `재확인 필요`
- inventory: source 11, test 5, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveChecksum.java`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveEntry.java`
  - `cpf-core/src/main/java/com/cpf/core/common/archive/CpfArchiveFormat.java`

### `API-CONTRACT` — API Contract/OpenAPI/Developer Portal

- 상태: `재확인 필요`
- inventory: source 147, test 11, SQL 0, script 3
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/BaseController.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfBaseController.java`
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfMapperContract.java`

### `API-GATEWAY` — Gateway/L4/WAF/Service Mesh 연계

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileExchangeGateway.java`
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRoute.java`

### `API-LIMIT` — Rate Limit/Quota/Abuse Detection

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `API-PAGING` — Pagination/Search/Sort/Download Guard

- 상태: `재확인 필요`
- inventory: source 12, test 2, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogArtifactSearch.java`
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmDownloadController.java`

### `SAMPLE-ACC` — ACC 실전형 샘플

- 상태: `재확인 필요`
- inventory: source 26, test 6, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-account/build.gradle`
  - `acc/README.md`
  - `acc/manifest/domain-manifest.json`

### `SAMPLE-MBR` — MBR 실전형 샘플

- 상태: `재확인 필요`
- inventory: source 20, test 9, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-member/build.gradle`
  - `cpf-member/src/main/java/com/cpf/member/MbrApplication.java`
  - `cpf-member/src/main/java/com/cpf/member/bse/controller/MbrAuthController.java`

### `SAMPLE-BIZADM` — BIZADM 업무관리 샘플

- 상태: `재확인 필요`
- inventory: source 19, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-biz-admin/build.gradle`
  - `cpf-biz-admin/src/main/java/com/cpf/bizadmin/BzaApplication.java`
  - `cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/controller/BzaAuthController.java`

### `SAMPLE-EDU` — EDU 교육 샘플

- 상태: `재확인 필요`
- inventory: source 101, test 61, SQL 4, script 4
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/edu/CpfEducationCoverageCatalog.java`
  - `cpf-core/src/test/java/com/cpf/core/common/edu/CpfEducationCoverageCatalogTest.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/CpfBatchScheduleCandidate.java`

### `SAMPLE-REF` — REF 신규 주제영역 샘플

- 상태: `재확인 필요`
- inventory: source 77, test 36, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-reference/build.gradle`
  - `cpf-reference/src/main/java/com/cpf/reference/ReferenceApplication.java`
  - `cpf-reference/src/main/java/com/cpf/reference/ai/ReferenceAiEducationSample.java`

### `ONBOARD-DOMAIN` — 신규 주제영역 온보딩

- 상태: `실패`
- inventory: source 1, test 1, SQL 0, script 5
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java`
  - `cpf-member/src/test/java/com/cpf/member/common/logging/TransactionIdGeneratorTest.java`
  - `acc/manifest/domain-manifest.json`

### `DEVEX-QUICK` — Developer Quickstart/Local Dev

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/archive/LocalCpfArchiveService.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/filetransfer/LocalCpfFileTransferAdapter.java`

### `DEVEX-CODEGEN` — Scaffold/Code Generation Governance

- 상태: `실패`
- inventory: source 1, test 1, SQL 0, script 4
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java`
  - `cpf-member/src/test/java/com/cpf/member/common/logging/TransactionIdGeneratorTest.java`
  - `scripts/create-domain.ps1`

### `DEVEX-COMMENT` — 한글 주석/설정 주석 표준

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `RULE-ARCH` — Architecture Rule Check

- 상태: `재확인 필요`
- inventory: source 0, test 0, SQL 0, script 3
- 확인 시작 파일:
  - `scripts/check-architecture-ownership.ps1`
  - `scripts/check-base-hierarchy.ps1`
  - `scripts/export-architecture-inventory.ps1`

### `RULE-SEC` — Security/Secret/URL Scan

- 상태: `재확인 필요`
- inventory: source 26, test 3, SQL 1, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/execution/CpfExecutionCatalogScanner.java`
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCertificateProviderPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialProviderPort.java`

### `RULE-QUALITY` — Static Analysis/Dependency/License

- 상태: `재확인 필요`
- inventory: source 0, test 0, SQL 1, script 2
- 확인 시작 파일:
  - `cpf-admin/src/main/resources/static/adm/adm.css`
  - `cpf-admin/src/main/resources/static/adm/adm.js`
  - `cpf-admin/src/main/resources/static/adm/index.html`

### `TEST-UNIT` — Unit/Slice/Integration Test

- 상태: `재확인 필요`
- inventory: source 1, test 158, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/test/java/com/cpf/core/channel/application/CpfChannelPolicyServiceTest.java`
  - `cpf-core/src/test/java/com/cpf/core/common/archive/CpfArchiveChecksumTest.java`
  - `cpf-core/src/test/java/com/cpf/core/common/archive/CpfArchiveServiceTest.java`

### `TEST-CONTRACT` — Contract/Compatibility Test

- 상태: `재확인 필요`
- inventory: source 2, test 158, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/base/CpfMapperContract.java`
  - `cpf-core/src/test/java/com/cpf/core/channel/application/CpfChannelPolicyServiceTest.java`
  - `cpf-core/src/test/java/com/cpf/core/common/archive/CpfArchiveChecksumTest.java`

### `TEST-RUNTIME` — Runtime Smoke

- 상태: `재확인 필요`
- inventory: source 30, test 6, SQL 4, script 41
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfRuntimeHealthStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchRuntimeListener.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchRuntimeProgress.java`

### `TEST-BROWSER` — Browser Click E2E

- 상태: `미검증`
- inventory: source 2, test 0, SQL 0, script 8
- 확인 시작 파일:
  - `cpf-core/build.gradle`
  - `cpf-core/src/main/java/com/cpf/core/common/runtime/CpfLockAcquireRequest.java`
  - `cpf-core/src/main/java/com/cpf/core/common/runtime/CpfLockAcquireResult.java`

### `TEST-BROKER` — Real Broker/Multi-instance Test

- 상태: `미검증`
- inventory: source 30, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/admin/CpfBrokerStatusQuery.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeHandler.java`

### `TEST-FAULT` — Fault Injection/Chaos Smoke

- 상태: `미검증`
- inventory: source 7, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/exception/DefaultCpfMessageResolver.java`
  - `cpf-core/src/main/java/com/cpf/core/common/exception/DefaultCpfResponseCodeResolver.java`
  - `cpf-core/src/main/java/com/cpf/core/common/workflow/CpfWorkflowFailurePolicy.java`

### `TEST-EVIDENCE` — Evidence Index/Consistency Gate

- 상태: `재확인 필요`
- inventory: source 0, test 1, SQL 0, script 5
- 확인 시작 파일:
  - `cpf-core/src/test/java/com/cpf/core/common/logging/file/CpfFileLogRuntimeEvidenceTest.java`
  - `scripts/check-evidence-path-existence.ps1`
  - `scripts/check-feature-evidence.ps1`

### `DOC-GOV` — 문서 최소화/정본화 후순위

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 51, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderSpecs.java`
  - `acc/README.md`
  - `scripts/check-document-links.ps1`

### `DOC-PRODUCT` — 제품화 문서/설치/운영/개발자 문서

- 상태: `부분 구현`
- inventory: source 19, test 7, SQL 7, script 4
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/api/reliability/CpfReliabilityOperationsPort.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchOperationRepository.java`
  - `cpf-core/src/main/java/com/cpf/core/common/batch/CpfBatchOperationType.java`

### `PROD-EDITION` — 상용화/Edition/License 후보

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `PROD-MULTITENANT` — Multi-tenant/Multi-customer 후보

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `PROD-PLUGIN` — Plugin/Adapter/Marketplace 후보

- 상태: `미구현`
- inventory: source 13, test 10, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeAdapter.java`

### `PROD-PACKAGE` — 산업군별 패키지 후보

- 상태: `미구현`
- inventory: source 2, test 0, SQL 0, script 1
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/channel/model/CpfChannelPolicyPackage.java`
  - `cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmChannelPackageImportRequest.java`
  - `scripts/check-packaged-runtime-resources.ps1`

### `REQ-GOV` — 요건 ID/우선순위/추적성

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 2
- 확인 시작 파일:
  - `scripts/build-sample-coverage-matrix.ps1`
  - `scripts/check-report-matrix-evidence-consistency.ps1`

### `REQ-REVIEW` — ChatGPT 검수/완료 불인정 기준

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `cpf-core/src/main/java/com/cpf/core/common/remotelog/CpfRemoteLogPreview.java`

### `REQ-CODEX` — Codex 요청서 작성 기준

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 0

### `REQ-GAP` — Future Requirement Gap Intake

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 2
- 확인 시작 파일:
  - `scripts/build-sample-coverage-matrix.ps1`
  - `scripts/check-report-matrix-evidence-consistency.ps1`
