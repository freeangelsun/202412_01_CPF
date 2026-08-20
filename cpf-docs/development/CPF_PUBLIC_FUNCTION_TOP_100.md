# CPF Public Function TOP 100

> 현재 Source와 Canonical Starter Catalog에서 자동 검증되는 개발자 탐색용 목록입니다. Internal 구현은 포함하지 않습니다.

## TOP 20 — Golden Path

| No | Function / Annotation | Starter | 언제 사용 | Source |
|---:|---|---|---|---|
| 1 | `@CpfRestController` | `cpf-web-runtime` | HTTP Controller 표준 경계 | `cpf-starters/web/src/main/java/com/cpf/web/api/CpfRestController.java` |
| 2 | `@CpfService` | `cpf-base-runtime` | 업무 Service 표준 경계 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfService.java` |
| 3 | `@CpfRepository` | `cpf-data-persistence-runtime` | Repository/DAO 표준 경계 | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfRepository.java` |
| 4 | `@CpfTransactional` | `cpf-data-persistence-runtime` | Local DB transaction | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/annotation/CpfTransactional.java` |
| 5 | `CpfDomainClient.execute` | `framework/runtime` | CPF Domain 간 호출 | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainClient.java` |
| 6 | `@CpfClient` | `cpf-integration-runtime` | 외부기관/외부시스템 typed client | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfClient.java` |
| 7 | `@CpfTimeLimiter` | `cpf-integration-runtime` | 외부 연계 deadline/timeout | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfTimeLimiter.java` |
| 8 | `@CpfRetry` | `cpf-integration-runtime` | 정책 기반 bounded retry | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfRetry.java` |
| 9 | `CpfContexts.transactionId` | `framework/runtime` | 현재 CPF 거래 Context 조회 | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java` |
| 10 | `CpfCodeService.required` | `cpf-starter-common` | 공통 코드 필수 조회 | `cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCodeService.java` |
| 11 | `CpfMessageSource.resolve` | `cpf-starter-common` | 표준 메시지 조회 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageSource.java` |
| 12 | `CpfParameterService.requiredValue` | `cpf-starter-common` | 운영 파라미터 조회 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java` |
| 13 | `CpfCalendarService.nextBusinessDay` | `cpf-starter-common` | 영업일 계산 | `cpf-common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java` |
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
| 1 | golden | `@CpfRestController` | `cpf-web-runtime` | HTTP Controller 표준 경계 | `cpf-starters/web/src/main/java/com/cpf/web/api/CpfRestController.java` |
| 2 | golden | `@CpfService` | `cpf-base-runtime` | 업무 Service 표준 경계 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfService.java` |
| 3 | golden | `@CpfRepository` | `cpf-data-persistence-runtime` | Repository/DAO 표준 경계 | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfRepository.java` |
| 4 | golden | `@CpfTransactional` | `cpf-data-persistence-runtime` | Local DB transaction | `cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/annotation/CpfTransactional.java` |
| 5 | golden | `CpfDomainClient.execute` | `framework/runtime` | CPF Domain 간 호출 | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainClient.java` |
| 6 | golden | `@CpfClient` | `cpf-integration-runtime` | 외부기관/외부시스템 typed client | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfClient.java` |
| 7 | golden | `@CpfTimeLimiter` | `cpf-integration-runtime` | 외부 연계 deadline/timeout | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfTimeLimiter.java` |
| 8 | golden | `@CpfRetry` | `cpf-integration-runtime` | 정책 기반 bounded retry | `cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfRetry.java` |
| 9 | golden | `CpfContexts.transactionId` | `framework/runtime` | 현재 CPF 거래 Context 조회 | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java` |
| 10 | golden | `CpfCodeService.required` | `cpf-starter-common` | 공통 코드 필수 조회 | `cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCodeService.java` |
| 11 | golden | `CpfMessageSource.resolve` | `cpf-starter-common` | 표준 메시지 조회 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageSource.java` |
| 12 | golden | `CpfParameterService.requiredValue` | `cpf-starter-common` | 운영 파라미터 조회 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java` |
| 13 | golden | `CpfCalendarService.nextBusinessDay` | `cpf-starter-common` | 영업일 계산 | `cpf-common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java` |
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
| 24 | capability | `CpfFiles` | `cpf-starter-file-attachment` | 업무 첨부파일 업로드·다운로드·검사·보관 정책을 사용하는 Public Provider Starter | `cpf-starters/file/attachment/src/main/java/com/cpf/file/api/util/CpfFiles.java` |
| 25 | capability | `CpfPages` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfPages.java` |
| 26 | capability | `CpfQuery` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfQuery.java` |
| 27 | capability | `CpfSlice` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfSlice.java` |
| 28 | capability | `CpfResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfResult.java` |
| 29 | capability | `CpfCommand` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfCommand.java` |
| 30 | capability | `CpfContext` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java` |
| 31 | capability | `CpfRequest` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/base/CpfRequest.java` |
| 32 | capability | `CpfRequest` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfRequest.java` |
| 33 | capability | `CpfResponse` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/base/CpfResponse.java` |
| 34 | capability | `CpfResponse` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfResponse.java` |
| 35 | capability | `CpfTccPhase` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccPhase.java` |
| 36 | capability | `CpfErrorCode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorCode.java` |
| 37 | capability | `CpfException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfException.java` |
| 38 | capability | `CpfParameter` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameter.java` |
| 39 | capability | `CpfSharedApi` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfSharedApi.java` |
| 40 | capability | `CpfTccResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccResult.java` |
| 41 | capability | `CpfXaContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfXaContext.java` |
| 42 | capability | `CpfAsyncState` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/async/CpfAsyncState.java` |
| 43 | capability | `CpfTccContext` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccContext.java` |
| 44 | capability | `CpfTccContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfTccContext.java` |
| 45 | capability | `CpfBaseService` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/CpfBaseService.java` |
| 46 | capability | `CpfCatalogPage` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfCatalogPage.java` |
| 47 | capability | `CpfPageRequest` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfPageRequest.java` |
| 48 | capability | `CpfRekeyResult` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfRekeyResult.java` |
| 49 | capability | `CpfSagaContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/transaction/api/context/CpfSagaContext.java` |
| 50 | capability | `CpfSubjectRole` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/tracking/CpfSubjectRole.java` |
| 51 | capability | `CpfSubjectType` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/tracking/CpfSubjectType.java` |
| 52 | capability | `CpfAsyncHandler` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/async/CpfAsyncHandler.java` |
| 53 | capability | `CpfConfigPolicy` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigPolicy.java` |
| 54 | capability | `CpfCryptoPolicy` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfCryptoPolicy.java` |
| 55 | capability | `CpfRecoveryInfo` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfRecoveryInfo.java` |
| 56 | capability | `CpfResultStatus` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/result/CpfResultStatus.java` |
| 57 | capability | `CpfConfigCatalog` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigCatalog.java` |
| 58 | capability | `CpfDomainBinding` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainBinding.java` |
| 59 | capability | `CpfExecutionType` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfExecutionType.java` |
| 60 | capability | `CpfMessageRecord` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageRecord.java` |
| 61 | capability | `CpfResolvedError` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfResolvedError.java` |
| 62 | capability | `CpfSortDirection` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/page/CpfSortDirection.java` |
| 63 | capability | `CpfXaTransaction` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaTransaction.java` |
| 64 | capability | `CpfAsyncExecution` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/async/CpfAsyncExecution.java` |
| 65 | capability | `CpfEncryptedField` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/data/encryption/CpfEncryptedField.java` |
| 66 | capability | `CpfErrorReference` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorReference.java` |
| 67 | capability | `CpfRepositoryPort` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfRepositoryPort.java` |
| 68 | capability | `CpfTccParticipant` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTccParticipant.java` |
| 69 | capability | `CpfTransactionIds` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIds.java` |
| 70 | capability | `CpfAsyncOperations` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/async/CpfAsyncOperations.java` |
| 71 | capability | `CpfAsyncSubmission` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/async/CpfAsyncSubmission.java` |
| 72 | capability | `CpfContextSnapshot` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java` |
| 73 | capability | `CpfErrorDefinition` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfErrorDefinition.java` |
| 74 | capability | `CpfMessageResolver` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfMessageResolver.java` |
| 75 | capability | `CpfParameterSchema` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterSchema.java` |
| 76 | capability | `CpfPlatformVersion` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/version/CpfPlatformVersion.java` |
| 77 | capability | `CpfRecoveryContext` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfRecoveryContext.java` |
| 78 | capability | `CpfResolvedMessage` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfResolvedMessage.java` |
| 79 | capability | `CpfSystemException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfSystemException.java` |
| 80 | capability | `CpfTemplateService` | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | `cpf-common/src/main/java/com/cpf/common/template/api/CpfTemplateService.java` |
| 81 | capability | `CpfConfigDescriptor` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigDescriptor.java` |
| 82 | capability | `CpfConfigMutability` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/config/CpfConfigMutability.java` |
| 83 | capability | `CpfCryptoOperations` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfCryptoOperations.java` |
| 84 | capability | `CpfDynamicErrorCode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfDynamicErrorCode.java` |
| 85 | capability | `CpfFixedLengthError` | `cpf-starter-integration-fixed-length` | 고정길이 전문 송수신/파싱을 사용하는 업무에서 선택합니다. | `cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthError.java` |
| 86 | capability | `CpfFixedLengthField` | `cpf-starter-integration-fixed-length` | 고정길이 전문 송수신/파싱을 사용하는 업무에서 선택합니다. | `cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthField.java` |
| 87 | capability | `CpfIdempotencyStore` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/reliability/api/CpfIdempotencyStore.java` |
| 88 | capability | `CpfMessageFormatter` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfMessageFormatter.java` |
| 89 | capability | `CpfResolvedResponse` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfResolvedResponse.java` |
| 90 | capability | `CpfSubjectCandidate` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/tracking/CpfSubjectCandidate.java` |
| 91 | capability | `CpfXaRecoveryRecord` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaRecoveryRecord.java` |
| 92 | capability | `CpfXaResourceHandle` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/transaction/CpfXaResourceHandle.java` |
| 93 | capability | `CpfApplicationFacade` | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | `cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/contract/CpfApplicationFacade.java` |
| 94 | capability | `CpfBusinessException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfBusinessException.java` |
| 95 | capability | `CpfDomainBindingMode` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainBindingMode.java` |
| 96 | capability | `CpfDomainPingRequest` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainPingRequest.java` |
| 97 | capability | `CpfFixedLengthLayout` | `cpf-starter-integration-fixed-length` | 고정길이 전문 송수신/파싱을 사용하는 업무에서 선택합니다. | `cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthLayout.java` |
| 98 | capability | `CpfFixedLengthParser` | `cpf-starter-integration-fixed-length` | 고정길이 전문 송수신/파싱을 사용하는 업무에서 선택합니다. | `cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthParser.java` |
| 99 | capability | `CpfFixedLengthWriter` | `cpf-starter-integration-fixed-length` | 고정길이 전문 송수신/파싱을 사용하는 업무에서 선택합니다. | `cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthWriter.java` |
| 100 | capability | `CpfNotFoundException` | `cpf-core` | Topology-independent CPF Public Contract | `cpf-core/src/main/java/com/cpf/core/api/error/CpfNotFoundException.java` |

## 선택 원칙

- **golden**: 일반 업무개발자가 먼저 사용하는 표준 경로입니다.
- **capability**: 해당 기능을 선택했을 때 사용하는 Public API입니다.
- **advanced**: Adapter/Framework 개발용이며 일반 Golden Path와 분리합니다.
- **internal**: 이 문서와 Public Starter 선택 화면에 노출하지 않습니다.

Starter 선택은 `cpf-docs/development/CPF_STARTER_QUICK_SELECT.md`를 먼저 봅니다.
