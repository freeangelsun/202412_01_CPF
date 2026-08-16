# CPF Public Function TOP 100

> 현재 Source와 Canonical Starter Catalog에서 자동 검증되는 개발자 탐색용 목록입니다. Internal 구현은 포함하지 않습니다.

## TOP 20 — Golden Path

| No | Function / Annotation | Starter | 언제 사용 | Source |
|---:|---|---|---|---|
| 1 | `@CpfController` | `cpf-web-runtime` | HTTP Controller 표준 경계 | `cpf-starters/web/src/main/java/com/cpf/web/api/CpfController.java` |
| 2 | `@CpfService` | `cpf-base-runtime` | 업무 Service 표준 경계 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfService.java` |
| 3 | `@CpfRepository` | `cpf-data-persistence-runtime` | Repository/DAO 표준 경계 | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfRepository.java` |
| 4 | `@CpfTx` | `cpf-data-persistence-runtime` | Local DB transaction | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/annotation/CpfTx.java` |
| 5 | `CpfDomainClient.execute` | `framework/runtime` | CPF Domain 간 호출 | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainClient.java` |
| 6 | `@CpfClient` | `cpf-integration-runtime` | 외부기관/외부시스템 typed client | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfClient.java` |
| 7 | `@CpfTimeout` | `cpf-integration-runtime` | 외부 연계 deadline/timeout | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfTimeout.java` |
| 8 | `@CpfRetry` | `cpf-integration-runtime` | 정책 기반 bounded retry | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfRetry.java` |
| 9 | `CpfContexts.transactionId` | `framework/runtime` | 현재 CPF 거래 Context 조회 | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java` |
| 10 | `CpfCodeService.required` | `cpf-starter-common` | 공통 코드 필수 조회 | `cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCodeService.java` |
| 11 | `CpfMessageService.resolve` | `cpf-starter-common` | 표준 메시지 조회 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageService.java` |
| 12 | `CpfParameterService.requiredValue` | `cpf-starter-common` | 운영 파라미터 조회 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java` |
| 13 | `CpfCalendarService.nextBusinessDay` | `cpf-starter-common` | 영업일 계산 | `cpf-starters/common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java` |
| 14 | `@CpfIdempotent` | `cpf-base-runtime` | 중복 side effect 방지 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfIdempotent.java` |
| 15 | `@CpfLogging` | `cpf-base-runtime` | 업무/운영 로그 표준화 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfLogging.java` |
| 16 | `@CpfPermission` | `cpf-security-runtime` | 권한 검증 | `cpf-starters/security/src/main/java/com/cpf/security/api/annotation/CpfPermission.java` |
| 17 | `@CpfApprovalRequired` | `cpf-security-runtime` | 위험 조치 승인 | `cpf-starters/security/src/main/java/com/cpf/security/api/annotation/CpfApprovalRequired.java` |
| 18 | `@CpfAudit` | `cpf-platform-operations-runtime` | 감사 이벤트 | `cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/annotation/CpfAudit.java` |
| 19 | `@CpfMessageListener` | `cpf-messaging-runtime` | 표준 메시지 Consumer | `cpf-starters/messaging/src/main/java/com/cpf/messaging/api/CpfMessageListener.java` |
| 20 | `@CpfBatchJob / @CpfBatchStep` | `framework/runtime` | Batch Job/Step 개발 | `cpf-batch/api/src/main/java/com/cpf/batch/api/annotation/CpfBatchJob.java` |

## TOP 100 — 기능 탐색

| No | Level | Public Function / Type | Starter | 대표 용도/메소드 | Source |
|---:|---|---|---|---|---|
| 1 | golden | `@CpfController` | `cpf-web-runtime` | HTTP Controller 표준 경계 | `cpf-starters/web/src/main/java/com/cpf/web/api/CpfController.java` |
| 2 | golden | `@CpfService` | `cpf-base-runtime` | 업무 Service 표준 경계 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfService.java` |
| 3 | golden | `@CpfRepository` | `cpf-data-persistence-runtime` | Repository/DAO 표준 경계 | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfRepository.java` |
| 4 | golden | `@CpfTx` | `cpf-data-persistence-runtime` | Local DB transaction | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/annotation/CpfTx.java` |
| 5 | golden | `CpfDomainClient.execute` | `framework/runtime` | CPF Domain 간 호출 | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainClient.java` |
| 6 | golden | `@CpfClient` | `cpf-integration-runtime` | 외부기관/외부시스템 typed client | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfClient.java` |
| 7 | golden | `@CpfTimeout` | `cpf-integration-runtime` | 외부 연계 deadline/timeout | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfTimeout.java` |
| 8 | golden | `@CpfRetry` | `cpf-integration-runtime` | 정책 기반 bounded retry | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfRetry.java` |
| 9 | golden | `CpfContexts.transactionId` | `framework/runtime` | 현재 CPF 거래 Context 조회 | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java` |
| 10 | golden | `CpfCodeService.required` | `cpf-starter-common` | 공통 코드 필수 조회 | `cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCodeService.java` |
| 11 | golden | `CpfMessageService.resolve` | `cpf-starter-common` | 표준 메시지 조회 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageService.java` |
| 12 | golden | `CpfParameterService.requiredValue` | `cpf-starter-common` | 운영 파라미터 조회 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java` |
| 13 | golden | `CpfCalendarService.nextBusinessDay` | `cpf-starter-common` | 영업일 계산 | `cpf-starters/common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java` |
| 14 | golden | `@CpfIdempotent` | `cpf-base-runtime` | 중복 side effect 방지 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfIdempotent.java` |
| 15 | golden | `@CpfLogging` | `cpf-base-runtime` | 업무/운영 로그 표준화 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfLogging.java` |
| 16 | golden | `@CpfPermission` | `cpf-security-runtime` | 권한 검증 | `cpf-starters/security/src/main/java/com/cpf/security/api/annotation/CpfPermission.java` |
| 17 | golden | `@CpfApprovalRequired` | `cpf-security-runtime` | 위험 조치 승인 | `cpf-starters/security/src/main/java/com/cpf/security/api/annotation/CpfApprovalRequired.java` |
| 18 | golden | `@CpfAudit` | `cpf-platform-operations-runtime` | 감사 이벤트 | `cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/annotation/CpfAudit.java` |
| 19 | golden | `@CpfMessageListener` | `cpf-messaging-runtime` | 표준 메시지 Consumer | `cpf-starters/messaging/src/main/java/com/cpf/messaging/api/CpfMessageListener.java` |
| 20 | golden | `@CpfBatchJob / @CpfBatchStep` | `framework/runtime` | Batch Job/Step 개발 | `cpf-batch/api/src/main/java/com/cpf/batch/api/annotation/CpfBatchJob.java` |
| 21 | capability | `CpfCode` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCode.java` |
| 22 | capability | `CpfPage` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfPage.java` |
| 23 | capability | `CpfSort` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfSort.java` |
| 24 | capability | `CpfPages` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfPages.java` |
| 25 | capability | `CpfQuery` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfQuery.java` |
| 26 | capability | `CpfSlice` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfSlice.java` |
| 27 | capability | `CpfResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfResult.java` |
| 28 | capability | `CpfCommand` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfCommand.java` |
| 29 | capability | `CpfContext` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java` |
| 30 | capability | `CpfRequest` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/base/CpfRequest.java` |
| 31 | capability | `CpfRequest` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfRequest.java` |
| 32 | capability | `CpfBatchJob` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfBatchJob.java` |
| 33 | capability | `CpfResponse` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/base/CpfResponse.java` |
| 34 | capability | `CpfResponse` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfResponse.java` |
| 35 | capability | `CpfTccPhase` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccPhase.java` |
| 36 | capability | `CpfWorkflow` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/workflow/api/CpfWorkflow.java` |
| 37 | capability | `CpfErrorCode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorCode.java` |
| 38 | capability | `CpfException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfException.java` |
| 39 | capability | `CpfParameter` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameter.java` |
| 40 | capability | `CpfSharedApi` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfSharedApi.java` |
| 41 | capability | `CpfTccResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccResult.java` |
| 42 | capability | `CpfXaContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfXaContext.java` |
| 43 | capability | `CpfTccContext` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccContext.java` |
| 44 | capability | `CpfTccContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfTccContext.java` |
| 45 | capability | `CpfBaseService` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/CpfBaseService.java` |
| 46 | capability | `CpfCatalogPage` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfCatalogPage.java` |
| 47 | capability | `CpfPageRequest` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfPageRequest.java` |
| 48 | capability | `CpfRekeyResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfRekeyResult.java` |
| 49 | capability | `CpfSagaContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfSagaContext.java` |
| 50 | capability | `CpfConfigPolicy` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigPolicy.java` |
| 51 | capability | `CpfCryptoPolicy` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfCryptoPolicy.java` |
| 52 | capability | `CpfRecoveryInfo` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfRecoveryInfo.java` |
| 53 | capability | `CpfResultStatus` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfResultStatus.java` |
| 54 | capability | `CpfConfigCatalog` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigCatalog.java` |
| 55 | capability | `CpfDomainBinding` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainBinding.java` |
| 56 | capability | `CpfExecutionType` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfExecutionType.java` |
| 57 | capability | `CpfMessageRecord` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageRecord.java` |
| 58 | capability | `CpfResolvedError` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfResolvedError.java` |
| 59 | capability | `CpfSortDirection` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfSortDirection.java` |
| 60 | capability | `CpfXaTransaction` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaTransaction.java` |
| 61 | capability | `CpfEncryptedField` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/data/encryption/CpfEncryptedField.java` |
| 62 | capability | `CpfErrorReference` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorReference.java` |
| 63 | capability | `CpfRepositoryPort` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfRepositoryPort.java` |
| 64 | capability | `CpfTccParticipant` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccParticipant.java` |
| 65 | capability | `CpfTransactionIds` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIds.java` |
| 66 | capability | `CpfContextSnapshot` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java` |
| 67 | capability | `CpfErrorDefinition` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorDefinition.java` |
| 68 | capability | `CpfMessageResolver` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfMessageResolver.java` |
| 69 | capability | `CpfParameterSchema` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterSchema.java` |
| 70 | capability | `CpfPlatformVersion` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/version/CpfPlatformVersion.java` |
| 71 | capability | `CpfRecoveryContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfRecoveryContext.java` |
| 72 | capability | `CpfResolvedMessage` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfResolvedMessage.java` |
| 73 | capability | `CpfSystemException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfSystemException.java` |
| 74 | capability | `CpfTemplateService` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/template/api/CpfTemplateService.java` |
| 75 | capability | `CpfConfigDescriptor` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigDescriptor.java` |
| 76 | capability | `CpfConfigMutability` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigMutability.java` |
| 77 | capability | `CpfCryptoOperations` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfCryptoOperations.java` |
| 78 | capability | `CpfDynamicErrorCode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfDynamicErrorCode.java` |
| 79 | capability | `CpfIdempotencyStore` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfIdempotencyStore.java` |
| 80 | capability | `CpfMessageFormatter` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfMessageFormatter.java` |
| 81 | capability | `CpfResolvedResponse` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfResolvedResponse.java` |
| 82 | capability | `CpfXaRecoveryRecord` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaRecoveryRecord.java` |
| 83 | capability | `CpfXaResourceHandle` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaResourceHandle.java` |
| 84 | capability | `CpfApplicationFacade` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfApplicationFacade.java` |
| 85 | capability | `CpfBusinessException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfBusinessException.java` |
| 86 | capability | `CpfDomainBindingMode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainBindingMode.java` |
| 87 | capability | `CpfDomainPingRequest` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainPingRequest.java` |
| 88 | capability | `CpfNotFoundException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfNotFoundException.java` |
| 89 | capability | `CpfOnlineTransaction` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfOnlineTransaction.java` |
| 90 | capability | `CpfDomainPingResponse` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainPingResponse.java` |
| 91 | capability | `CpfEnvelopeCiphertext` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfEnvelopeCiphertext.java` |
| 92 | capability | `CpfFrameworkErrorCode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfFrameworkErrorCode.java` |
| 93 | capability | `CpfFrameworkException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfFrameworkException.java` |
| 94 | capability | `CpfResponseCodeRecord` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfResponseCodeRecord.java` |
| 95 | capability | `CpfTransactionOutcome` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionOutcome.java` |
| 96 | capability | `CpfExecutionDefinition` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfExecutionDefinition.java` |
| 97 | capability | `CpfFieldClassification` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/data/encryption/CpfFieldClassification.java` |
| 98 | capability | `CpfParameterValueCodec` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterValueCodec.java` |
| 99 | capability | `CpfStandardExecutionId` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfStandardExecutionId.java` |
| 100 | capability | `CpfValidationException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfValidationException.java` |

## 선택 원칙

- **golden**: 일반 업무개발자가 먼저 사용하는 표준 경로입니다.
- **capability**: 해당 기능을 선택했을 때 사용하는 Public API입니다.
- **advanced**: Adapter/Framework 개발용이며 일반 Golden Path와 분리합니다.
- **internal**: 이 문서와 Public Starter 선택 화면에 노출하지 않습니다.

Starter 선택은 `cpf-docs/development/CPF_STARTER_QUICK_SELECT.md`를 먼저 봅니다.
