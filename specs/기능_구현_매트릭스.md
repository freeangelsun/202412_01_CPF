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
  - `pfw/build.gradle`
  - `cmn/build.gradle`
  - `adm/build.gradle`

### `ARCH-MSA` — MSA-first 및 Modular Monolith 호환

- 상태: `재확인 필요`
- inventory: source 80, test 20, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`

### `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context

- 상태: `재확인 필요`
- inventory: source 76, test 15, SQL 2, script 8
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`

### `ARCH-LAYER` — 계층/패키지/의존성 규칙

- 상태: `재확인 필요`
- inventory: source 64, test 16, SQL 1, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/base/BaseController.java`

### `FACADE-LOCAL` — Local Facade 표준

- 상태: `재확인 필요`
- inventory: source 19, test 7, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`

### `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter

- 상태: `재확인 필요`
- inventory: source 114, test 19, SQL 1, script 7
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`

### `PFW-CALL` — CpfWebClient/CpfRestClient Service Call Engine

- 상태: `재확인 필요`
- inventory: source 42, test 8, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfHttpClientProperties.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfLocalServiceIdentity.java`

### `PFW-REGISTRY` — Service/Endpoint/Instance Registry

- 상태: `재확인 필요`
- inventory: source 17, test 1, SQL 4, script 4
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `pfw/src/main/java/cpf/pfw/channel/api/CpfChannelRegistryPort.java`

### `PFW-ROUTING` — LB mode/direct instance/discovery routing

- 상태: `재확인 필요`
- inventory: source 10, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRoute.java`
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalog.java`

### `PFW-HEALTH` — Liveness/Readiness/Dependency Health

- 상태: `재확인 필요`
- inventory: source 10, test 2, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerHealthPort.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferHealthPort.java`

### `PFW-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure

- 상태: `재확인 필요`
- inventory: source 3, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferRetryPolicy.java`
  - `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSample.java`
  - `bat/src/main/java/cpf/bat/edu/retry/BatRetryEducationSample.java`

### `PFW-DEADLINE` — Request Deadline/Timeout Budget

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `PFW-HEADER` — 표준/확장 헤더

- 상태: `재확인 필요`
- inventory: source 22, test 8, SQL 1, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/header/CpfExtensionHeaderPolicy.java`
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderAuditLogger.java`
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderCategory.java`

### `PFW-CONTEXT` — TransactionContext/MDC/Thread Context

- 상태: `재확인 필요`
- inventory: source 5, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filter/TransactionContextFilter.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/CpfTransactionContextAnomalyMonitor.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/TransactionContext.java`

### `PFW-TXID` — transactionGlobalId/segment/timeline

- 상태: `재확인 필요`
- inventory: source 18, test 2, SQL 3, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `PFW-ROLE` — transactionRole/direction/source-target

- 상태: `재확인 필요`
- inventory: source 17, test 6, SQL 0, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutTarget.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/policy/LogPolicyTargetType.java`

### `PFW-OPSDB` — PFW 운영 DB 공유/장애모드

- 상태: `재확인 필요`
- inventory: source 26, test 9, SQL 4, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationType.java`

### `PFW-LOGDB` — DB 로그/Segment 로그

- 상태: `부분 구현`
- inventory: source 51, test 14, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`

### `PFW-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그

- 상태: `미검증`
- inventory: source 52, test 15, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchFileLogWriter.java`

### `PFW-LOGFAIL` — 로그 실패/fail-open/local spool

- 상태: `부분 구현`
- inventory: source 52, test 14, SQL 2, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`

### `PFW-TRACE` — Trace Boost/동적 로그 레벨

- 상태: `재확인 필요`
- inventory: source 13, test 1, SQL 2, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRequest.java`

### `PFW-MASK` — 마스킹/민감정보 보호

- 상태: `재확인 필요`
- inventory: source 5, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMasker.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/SensitiveDataMasker.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMaskingRule.java`

### `PFW-ERROR` — 오류/예외/응답 표준

- 상태: `재확인 필요`
- inventory: source 49, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfResponse.java`
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfBusinessException.java`
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfDynamicErrorCode.java`

### `PFW-VALID` — Validation Framework

- 상태: `재확인 필요`
- inventory: source 11, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfValidationException.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumValidationResult.java`
  - `pfw/src/main/java/cpf/pfw/common/header/CpfInboundHeaderValidator.java`

### `PFW-IDEMP` — Idempotency 표준

- 상태: `재확인 필요`
- inventory: source 14, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerIdempotencyPort.java`
  - `pfw/src/main/java/cpf/pfw/common/idempotency/CpfIdempotencyCommand.java`
  - `pfw/src/main/java/cpf/pfw/common/idempotency/CpfIdempotencyEngine.java`

### `PFW-STATE` — 상태 전이 State Machine

- 상태: `재확인 필요`
- inventory: source 8, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflow.java`
  - `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowContext.java`
  - `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowFailurePolicy.java`

### `PFW-LOCK` — Optimistic/Distributed Lock

- 상태: `재확인 필요`
- inventory: source 6, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLockManager.java`
  - `pfw/src/main/java/cpf/pfw/common/runtime/CpfDistributedLockPort.java`
  - `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireRequest.java`

### `PFW-SCHED` — Scheduler 표준

- 상태: `부분 구현`
- inventory: source 3, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `adm/src/main/java/cpf/adm/opr/dto/PfwBatchScheduleCandidate.java`
  - `adm/src/main/java/cpf/adm/opr/service/PfwBatchScheduler.java`
  - `adm/src/main/java/cpf/adm/opr/service/PfwBatchScheduleService.java`

### `CMN-CODE` — 공통코드/참조데이터

- 상태: `재확인 필요`
- inventory: source 26, test 3, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfDynamicErrorCode.java`
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfErrorCode.java`
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfFrameworkErrorCode.java`

### `CMN-MSG` — 공통 메시지/다국어/오류 메시지

- 상태: `재확인 필요`
- inventory: source 34, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeMessage.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerMessage.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerMessageHandler.java`

### `CMN-ID` — 채번/분산 ID

- 상태: `재확인 필요`
- inventory: source 4, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
  - `cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceIssueRequest.java`
  - `cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceIssueResult.java`

### `CMN-FILE` — 파일/다운로드/업로드/Object Storage

- 상태: `재확인 필요`
- inventory: source 54, test 14, SQL 1, script 3
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfFileTransferStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/CpfAttachmentContent.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/CpfAttachmentStoragePort.java`

### `CMN-FIXED` — 고정길이 전문 parser/formatter/layout

- 상태: `재확인 필요`
- inventory: source 24, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfMessageFormatter.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthAlignment.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldSpec.java`

### `CMN-CALENDAR` — 영업일/휴일/기관 캘린더

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `adm/src/main/java/cpf/adm/opr/dto/AdmBusinessDayRequest.java`

### `CMN-TEMPLATE` — 템플릿/알림

- 상태: `부분 구현`
- inventory: source 12, test 0, SQL 1, script 0
- 확인 시작 파일:
  - `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogRequest.java`
  - `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogResult.java`
  - `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java`

### `ADM-AUTH` — ADM 인증/세션/계정 생명주기

- 상태: `재확인 필요`
- inventory: source 23, test 6, SQL 2, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cmn/src/main/java/cpf/cmn/sec/token/CmnOAuthBearerTokenService.java`
  - `cmn/src/main/java/cpf/cmn/sec/token/CmnOAuthTokenIntrospectionResult.java`

### `ADM-RBAC` — RBAC/ABAC/권한 메타

- 상태: `재확인 필요`
- inventory: source 17, test 3, SQL 2, script 3
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentRole.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java`
  - `adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java`

### `ADM-AUDIT` — 감사/보안 이벤트/부인방지

- 상태: `재확인 필요`
- inventory: source 7, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderAuditLogger.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmAuditLogController.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyAuditController.java`

### `ADM-TX` — 거래 그룹 목록/상세

- 상태: `재확인 필요`
- inventory: source 129, test 34, SQL 10, script 7
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchFileLogWriter.java`

### `ADM-TIMELINE` — 통합 Timeline

- 상태: `재확인 필요`
- inventory: source 14, test 1, SQL 3, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentFallbackStore.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`

### `ADM-SERVICE` — Service Instance/Routing/Health 관제

- 상태: `재확인 필요`
- inventory: source 136, test 42, SQL 5, script 10
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/channel/application/CpfChannelPolicyService.java`
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`

### `ADM-LOG` — 로그 정책/Trace Boost 화면

- 상태: `재확인 필요`
- inventory: source 43, test 6, SQL 2, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRequest.java`

### `ADM-BATCH` — Batch/Worker/Heartbeat/Ghost 관제

- 상태: `부분 구현`
- inventory: source 57, test 10, SQL 8, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`

### `ADM-CENTER` — Center-Cut 관제

- 상태: `재확인 필요`
- inventory: source 25, test 11, SQL 3, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`

### `ADM-EXS` — 대외연계 관제

- 상태: `재확인 필요`
- inventory: source 10, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfExternalServiceException.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointProperties.java`

### `ADM-COMP` — Compensation/Manual Recovery 관제

- 상태: `부분 구현`
- inventory: source 6, test 0, SQL 2, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`

### `ADM-INCIDENT` — Incident/Alert/Runbook

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `ADM-UX` — ADM UX/검색/다운로드/대시보드

- 상태: `미구현`
- inventory: source 7, test 1, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`
  - `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`

### `BAT-CORE` — BAT standalone worker/application

- 상태: `미구현`
- inventory: source 14, test 1, SQL 2, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchHeartbeatService.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerConsumerWorker.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerPublisherWorker.java`

### `BAT-JOB` — Job/Step/Parameter/Dependency

- 상태: `재확인 필요`
- inventory: source 25, test 8, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfJobSupport.java`
  - `pfw/src/main/java/cpf/pfw/common/base/CpfStepSupport.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchJobLogPath.java`

### `BAT-ITEM` — Batch item claim/retry/skip/rerun

- 상태: `재확인 필요`
- inventory: source 6, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferRetryPolicy.java`
  - `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutItemProcessingEducationSample.java`
  - `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSample.java`

### `BAT-CALL-SYNC` — BAT → 주제영역 동기 호출

- 상태: `재확인 필요`
- inventory: source 43, test 9, SQL 0, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `BAT-CALL-ASYNC` — BAT → Event/Outbox 비동기 호출

- 상태: `재확인 필요`
- inventory: source 11, test 1, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`

### `BAT-SHARED` — BAT → SHARED/온라인 Facade 재사용

- 상태: `재확인 필요`
- inventory: source 13, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`
  - `pfw/src/main/java/cpf/pfw/common/execution/CpfSharedApi.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`

### `CENTER-CORE` — Center-Cut 기본 구현

- 상태: `재확인 필요`
- inventory: source 25, test 11, SQL 3, script 2
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`

### `CENTER-ADV` — Center-Cut 고급 패턴

- 상태: `재확인 필요`
- inventory: source 26, test 11, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`

### `EXS-INST` — 기관/Endpoint/Profile

- 상태: `부분 구현`
- inventory: source 5, test 0, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointProperties.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointRegistry.java`

### `EXS-REST` — EXS REST 송수신

- 상태: `재확인 필요`
- inventory: source 12, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/CpfExternalServiceException.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfHttpClientProperties.java`
  - `pfw/src/main/java/cpf/pfw/common/http/CpfLocalServiceIdentity.java`

### `EXS-FIXED` — EXS 고정길이 전문 송수신

- 상태: `재확인 필요`
- inventory: source 23, test 2, SQL 1, script 1
- 확인 시작 파일:
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthAlignment.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldSpec.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldType.java`

### `EXS-SEC` — EXS OAuth/JWT/mTLS/인증서

- 상태: `부분 구현`
- inventory: source 12, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfCredentialStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogServiceCredentialPort.java`
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`

### `EXS-UNKNOWN` — Unknown Result 처리

- 상태: `부분 구현`
- inventory: source 35, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveResult.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchExecutionResult.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`

### `EXS-RECON` — 대외 Reconciliation

- 상태: `부분 구현`
- inventory: source 5, test 3, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfReconciliationPort.java`
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfUnknownResultRecord.java`
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepository.java`

### `EXS-FILE` — SFTP/File Transfer 연계 후보

- 상태: `부분 구현`
- inventory: source 30, test 6, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfFileTransferStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfDuplicatePreventionPort.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumPolicy.java`

### `EVENT-CORE` — Event/Message Envelope

- 상태: `재확인 필요`
- inventory: source 46, test 1, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`

### `EVENT-OUTBOX` — Outbox/Inbox

- 상태: `재확인 필요`
- inventory: source 3, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerInboxPort.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerOutboxPort.java`
  - `xyz/src/main/java/cpf/xyz/messaging/XyzOutboxInboxEducationSample.java`

### `EVENT-BROKER` — Kafka/MQ/Redis real broker

- 상태: `미검증`
- inventory: source 30, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfBrokerStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeHandler.java`

### `EVENT-DLQ` — DLQ/Replay/Poison Message

- 상태: `부분 구현`
- inventory: source 4, test 0, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqPort.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayRequest.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayResult.java`

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
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`

### `SEC-AUTHN` — 인증/AuthN

- 상태: `재확인 필요`
- inventory: source 18, test 5, SQL 2, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
  - `cmn/src/main/java/cpf/cmn/sec/token/CmnJwtCreateRequest.java`
  - `cmn/src/main/java/cpf/cmn/sec/token/CmnJwtService.java`

### `SEC-AUTHZ` — 권한/AuthZ/RBAC/ABAC

- 상태: `재확인 필요`
- inventory: source 18, test 3, SQL 2, script 3
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentRole.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java`

### `SEC-SECRET` — Secret/Vault/Key Rotation

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/security/CpfSecretProviderPort.java`

### `SEC-CERT` — Certificate/mTLS 관리

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`

### `SEC-PRIVACY` — 개인정보/Privacy/Data Classification

- 상태: `재확인 필요`
- inventory: source 5, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMasker.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/SensitiveDataMasker.java`
  - `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMaskingRule.java`

### `SEC-DOWNLOAD` — Download Governance

- 상태: `재확인 필요`
- inventory: source 7, test 1, SQL 1, script 5
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`
  - `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`

### `SEC-APP` — Application Security 기본 통제

- 상태: `재확인 필요`
- inventory: source 23, test 3, SQL 1, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java`
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialRef.java`

### `SEC-APPROVAL` — Approval/Dual Control/Break-glass

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `OPS-METRIC` — Metrics/Observability

- 상태: `재확인 필요`
- inventory: source 3, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `adm/src/main/java/cpf/adm/opr/controller/AdmObservabilityController.java`
  - `adm/src/main/java/cpf/adm/opr/service/AdmObservabilityService.java`
  - `adm/src/test/java/cpf/adm/opr/service/AdmObservabilityServiceTest.java`

### `OPS-SLO` — SLI/SLO/Error Budget

- 상태: `미구현`
- inventory: source 4, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfZipSlipGuard.java`
  - `pfw/src/test/java/cpf/pfw/common/archive/CpfZipSlipGuardTest.java`
  - `cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogRequest.java`

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
  - `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
  - `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`

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
  - `pfw/src/main/java/cpf/pfw/channel/application/CpfChannelPolicyService.java`
  - `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelExecutionPolicy.java`
  - `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyDecision.java`

### `OPS-DRIFT` — Config Drift/Policy Versioning

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/version/CpfPlatformVersion.java`

### `OPS-CAPACITY` — Performance/Capacity/Resource Governance

- 상태: `재확인 필요`
- inventory: source 0, test 2, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/resources/application-pfw-dev.yml`
  - `pfw/src/main/resources/application-pfw-local.yml`
  - `pfw/src/main/resources/application-pfw-prod.yml`

### `OPS-DR` — DR/Backup/Restore/Archive

- 상태: `부분 구현`
- inventory: source 11, test 5, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveEntry.java`
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveFormat.java`

### `REL-BUILD` — Build/Artifact/Provenance

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/build.gradle`
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumPolicy.java`

### `REL-DEPLOY` — Deployment/Rollback/Feature Flag

- 상태: `부분 구현`
- inventory: source 1, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `bat/src/main/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSample.java`
  - `bat/src/test/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSampleTest.java`
  - `deploy/inventory/dev-services.json`

### `REL-MIG` — DB Migration/Flyway/Expand-Contract

- 상태: `부분 구현`
- inventory: source 1, test 3, SQL 35, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
  - `pfw/src/test/java/cpf/pfw/common/base/CpfBaseContractTest.java`
  - `pfw/src/test/java/cpf/pfw/common/capability/CpfCapabilityContractTest.java`

### `REL-COMPAT` — API/Event/전문 호환성

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/version/CpfPlatformVersion.java`

### `DB-SQL` — SQL 표준/MyBatis/Index

- 상태: `재확인 필요`
- inventory: source 15, test 4, SQL 51, script 3
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
  - `pfw/src/main/java/cpf/pfw/config/PfwMyBatisConfig.java`
  - `pfw/src/main/java/cpf/pfw/mapper/common/logging/TransactionLogMapper.java`

### `DB-INSTALL` — MariaDB 신규 빈 DB Full Install

- 상태: `재확인 필요`
- inventory: source 7, test 2, SQL 4, script 34
- 확인 시작 파일:
  - `acc/smoke/smoke-acc.ps1`
  - `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutSmokeTasklet.java`
  - `bat/src/main/java/cpf/bat/job/heartbeat/BatHeartbeatSmokeTasklet.java`

### `DB-PERF` — DB 성능/Partition/Retention

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0
- 확인 시작 파일:
  - `adm/src/main/resources/static/adm/index.html`
  - `bza/src/main/resources/static/bza/index.html`

### `DB-MULTI` — Multi Datasource/Read Replica

- 상태: `부분 구현`
- inventory: source 7, test 2, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/config/PfwDataSourceConfig.java`
  - `pfw/src/test/java/cpf/pfw/config/PfwDataSourceConfigTest.java`
  - `cmn/src/main/java/cpf/cmn/config/CmnBusinessDataSourceConfig.java`

### `DATA-LINEAGE` — Data Lineage/Quality/Reconciliation

- 상태: `재확인 필요`
- inventory: source 5, test 3, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfReconciliationPort.java`
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfUnknownResultRecord.java`
  - `pfw/src/main/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepository.java`

### `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive

- 상태: `재확인 필요`
- inventory: source 11, test 5, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveEntry.java`
  - `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveFormat.java`

### `API-CONTRACT` — API Contract/OpenAPI/Developer Portal

- 상태: `재확인 필요`
- inventory: source 147, test 11, SQL 0, script 3
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/BaseController.java`
  - `pfw/src/main/java/cpf/pfw/common/base/CpfBaseController.java`
  - `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`

### `API-GATEWAY` — Gateway/L4/WAF/Service Mesh 연계

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileExchangeGateway.java`
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
  - `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRoute.java`

### `API-LIMIT` — Rate Limit/Quota/Abuse Detection

- 상태: `미구현`
- inventory: source 0, test 0, SQL 0, script 0

### `API-PAGING` — Pagination/Search/Sort/Download Guard

- 상태: `재확인 필요`
- inventory: source 12, test 2, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogArtifactSearch.java`
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
  - `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`

### `SAMPLE-ACC` — ACC 실전형 샘플

- 상태: `재확인 필요`
- inventory: source 26, test 6, SQL 0, script 0
- 확인 시작 파일:
  - `acc/build.gradle`
  - `acc/README.md`
  - `acc/manifest/domain-manifest.json`

### `SAMPLE-MBR` — MBR 실전형 샘플

- 상태: `재확인 필요`
- inventory: source 20, test 9, SQL 0, script 0
- 확인 시작 파일:
  - `mbr/build.gradle`
  - `mbr/src/main/java/cpf/mbr/MbrApplication.java`
  - `mbr/src/main/java/cpf/mbr/bse/controller/MbrAuthController.java`

### `SAMPLE-BIZADM` — BIZADM 업무관리 샘플

- 상태: `재확인 필요`
- inventory: source 19, test 5, SQL 0, script 0
- 확인 시작 파일:
  - `bza/build.gradle`
  - `bza/src/main/java/cpf/bza/BzaApplication.java`
  - `bza/src/main/java/cpf/bza/auth/controller/BzaAuthController.java`

### `SAMPLE-EDU` — EDU 교육 샘플

- 상태: `재확인 필요`
- inventory: source 101, test 61, SQL 4, script 4
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/edu/PfwEducationCoverageCatalog.java`
  - `pfw/src/test/java/cpf/pfw/common/edu/PfwEducationCoverageCatalogTest.java`
  - `adm/src/main/java/cpf/adm/opr/dto/PfwBatchScheduleCandidate.java`

### `SAMPLE-XYZ` — XYZ 신규 주제영역 샘플

- 상태: `재확인 필요`
- inventory: source 77, test 36, SQL 0, script 0
- 확인 시작 파일:
  - `xyz/build.gradle`
  - `xyz/src/main/java/cpf/xyz/XyzApplication.java`
  - `xyz/src/main/java/cpf/xyz/ai/XyzAiEducationSample.java`

### `ONBOARD-DOMAIN` — 신규 주제영역 온보딩

- 상태: `실패`
- inventory: source 1, test 1, SQL 0, script 5
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
  - `mbr/src/test/java/cpf/mbr/common/logging/TransactionIdGeneratorTest.java`
  - `acc/manifest/domain-manifest.json`

### `DEVEX-QUICK` — Developer Quickstart/Local Dev

- 상태: `재확인 필요`
- inventory: source 9, test 4, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/filetransfer/LocalCpfFileTransferAdapter.java`

### `DEVEX-CODEGEN` — Scaffold/Code Generation Governance

- 상태: `실패`
- inventory: source 1, test 1, SQL 0, script 4
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
  - `mbr/src/test/java/cpf/mbr/common/logging/TransactionIdGeneratorTest.java`
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
  - `pfw/src/main/java/cpf/pfw/common/execution/CpfExecutionCatalogScanner.java`
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`
  - `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java`

### `RULE-QUALITY` — Static Analysis/Dependency/License

- 상태: `재확인 필요`
- inventory: source 0, test 0, SQL 1, script 2
- 확인 시작 파일:
  - `adm/src/main/resources/static/adm/adm.css`
  - `adm/src/main/resources/static/adm/adm.js`
  - `adm/src/main/resources/static/adm/index.html`

### `TEST-UNIT` — Unit/Slice/Integration Test

- 상태: `재확인 필요`
- inventory: source 1, test 158, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/test/java/cpf/pfw/channel/application/CpfChannelPolicyServiceTest.java`
  - `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveChecksumTest.java`
  - `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveServiceTest.java`

### `TEST-CONTRACT` — Contract/Compatibility Test

- 상태: `재확인 필요`
- inventory: source 2, test 158, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
  - `pfw/src/test/java/cpf/pfw/channel/application/CpfChannelPolicyServiceTest.java`
  - `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveChecksumTest.java`

### `TEST-RUNTIME` — Runtime Smoke

- 상태: `재확인 필요`
- inventory: source 30, test 6, SQL 4, script 41
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeListener.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeProgress.java`

### `TEST-BROWSER` — Browser Click E2E

- 상태: `미검증`
- inventory: source 2, test 0, SQL 0, script 8
- 확인 시작 파일:
  - `pfw/build.gradle`
  - `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireRequest.java`
  - `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireResult.java`

### `TEST-BROKER` — Real Broker/Multi-instance Test

- 상태: `미검증`
- inventory: source 30, test 4, SQL 1, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/admin/CpfBrokerStatusQuery.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeHandler.java`

### `TEST-FAULT` — Fault Injection/Chaos Smoke

- 상태: `미검증`
- inventory: source 7, test 1, SQL 0, script 0
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/exception/DefaultCpfMessageResolver.java`
  - `pfw/src/main/java/cpf/pfw/common/exception/DefaultCpfResponseCodeResolver.java`
  - `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowFailurePolicy.java`

### `TEST-EVIDENCE` — Evidence Index/Consistency Gate

- 상태: `재확인 필요`
- inventory: source 0, test 1, SQL 0, script 5
- 확인 시작 파일:
  - `pfw/src/test/java/cpf/pfw/common/logging/file/CpfFileLogRuntimeEvidenceTest.java`
  - `scripts/check-evidence-path-existence.ps1`
  - `scripts/check-feature-evidence.ps1`

### `DOC-GOV` — 문서 최소화/정본화 후순위

- 상태: `부분 구현`
- inventory: source 1, test 0, SQL 51, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderSpecs.java`
  - `acc/README.md`
  - `scripts/check-document-links.ps1`

### `DOC-PRODUCT` — 제품화 문서/설치/운영/개발자 문서

- 상태: `부분 구현`
- inventory: source 19, test 7, SQL 7, script 4
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java`
  - `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationType.java`

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
  - `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
  - `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`

### `PROD-PACKAGE` — 산업군별 패키지 후보

- 상태: `미구현`
- inventory: source 2, test 0, SQL 0, script 1
- 확인 시작 파일:
  - `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyPackage.java`
  - `adm/src/main/java/cpf/adm/opr/dto/AdmChannelPackageImportRequest.java`
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
  - `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogPreview.java`

### `REQ-CODEX` — Codex 요청서 작성 기준

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 0

### `REQ-GAP` — Future Requirement Gap Intake

- 상태: `부분 구현`
- inventory: source 0, test 0, SQL 0, script 2
- 확인 시작 파일:
  - `scripts/build-sample-coverage-matrix.ps1`
  - `scripts/check-report-matrix-evidence-consistency.ps1`
