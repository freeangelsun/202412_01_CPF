# 2026-07-20 Domain identity 보정

- 현재 행 수: 51개 (`XYZ 26`, `BAT 25`)
- `EDU`는 sample/reference 분류이며 XYZ/BAT의 domain, module, artifact, runtime identity가 아니다.
- `cpf-xyz-edu-*` artifact와 `XYZ online EDU` topology는 금지한다.
- 기존 `cpf.xyz.*`, `/api/xyz/reference/**`, `*-EDU-*`는 공개 호환성 조사 후 alias·deprecation·migration과 함께 변경한다.
- 모든 행의 20260716 evidence는 22818f0 최신 source/test와 freshness를 재검증해야 한다.
- source 존재와 unit test만으로 runtime 완료를 판정하지 않는다.

---

# CPF EDU 샘플 검증 매트릭스

> 범용 EDU 소유자는 XYZ와 BAT입니다. PFW는 엔진·포트·계약 테스트, CMN은 공통 helper·단위 테스트, MBR·ADM·BZA는 업무 또는 운영 코드를 소유합니다.

| sampleId | module | package | featureArea | sampleName | sourcePath | testPath | evidencePath | validationLevel | runtimeRequired | runtimeExecuted | status | notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| XYZ Reference-AI-001 | XYZ | cpf.xyz.ai | AI | XyzAiEducationSample | xyz/src/main/java/cpf/xyz/ai/XyzAiEducationSample.java | xyz/src/test/java/cpf/xyz/ai/XyzAiEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-ARCHIVE-001 | XYZ | cpf.xyz.archive | ARCHIVE | XyzArchiveEducationSample | xyz/src/main/java/cpf/xyz/archive/XyzArchiveEducationSample.java | xyz/src/test/java/cpf/xyz/archive/XyzArchiveEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-ATTACHMENT-001 | XYZ | cpf.xyz.attachment | ATTACHMENT | XyzAttachmentEducationSample | xyz/src/main/java/cpf/xyz/attachment/XyzAttachmentEducationSample.java | xyz/src/test/java/cpf/xyz/attachment/XyzAttachmentEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-AUDIT-001 | XYZ | cpf.xyz.audit | AUDIT | XyzAuditEducationSample | xyz/src/main/java/cpf/xyz/audit/XyzAuditEducationSample.java | xyz/src/test/java/cpf/xyz/audit/XyzAuditEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-CRUD-001 | XYZ | cpf.xyz.crud | CRUD | XyzCrudEducationSample | xyz/src/main/java/cpf/xyz/crud/XyzCrudEducationSample.java | xyz/src/test/java/cpf/xyz/crud/XyzCrudEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-DETAIL-001 | XYZ | cpf.xyz.detail | DETAIL | XyzDetailEducationSample | xyz/src/main/java/cpf/xyz/detail/XyzDetailEducationSample.java | xyz/src/test/java/cpf/xyz/detail/XyzDetailEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-EXTERNAL-001 | XYZ | cpf.xyz.external | EXTERNAL | XyzExternalIntegrationEducationSample | xyz/src/main/java/cpf/xyz/external/XyzExternalIntegrationEducationSample.java | xyz/src/test/java/cpf/xyz/external/XyzExternalIntegrationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-FACADE-001 | XYZ | cpf.xyz.servicecall | FACADE | XyzLocalFacadeEducationSample | xyz/src/main/java/cpf/xyz/servicecall/XyzLocalFacadeEducationSample.java | xyz/src/test/java/cpf/xyz/servicecall/XyzLocalFacadeEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-FAILURE-001 | XYZ | cpf.xyz.failure | FAILURE | XyzFailureEducationSample | xyz/src/main/java/cpf/xyz/failure/XyzFailureEducationSample.java | xyz/src/test/java/cpf/xyz/failure/XyzFailureEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-FILETRANSFER-001 | XYZ | cpf.xyz.filetransfer | FILETRANSFER | XyzFileChecksumEducationSample | xyz/src/main/java/cpf/xyz/filetransfer/XyzFileChecksumEducationSample.java | xyz/src/test/java/cpf/xyz/filetransfer/XyzFileChecksumEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-FILETRANSFER-002 | XYZ | cpf.xyz.filetransfer | FILETRANSFER | XyzSftpTransferPlanEducationSample | xyz/src/main/java/cpf/xyz/filetransfer/XyzSftpTransferPlanEducationSample.java | xyz/src/test/java/cpf/xyz/filetransfer/XyzSftpTransferPlanEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-HEADER-001 | XYZ | cpf.xyz.header | HEADER | XyzHeaderPropagationEducationSample | xyz/src/main/java/cpf/xyz/header/XyzHeaderPropagationEducationSample.java | xyz/src/test/java/cpf/xyz/header/XyzHeaderPropagationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-IDEMPOTENCY-001 | XYZ | cpf.xyz.idempotency | IDEMPOTENCY | XyzIdempotencyEducationSample | xyz/src/main/java/cpf/xyz/idempotency/XyzIdempotencyEducationSample.java | xyz/src/test/java/cpf/xyz/idempotency/XyzIdempotencyEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-LOGGING-001 | XYZ | cpf.xyz.logging | LOGGING | XyzFileLogEducationSample | xyz/src/main/java/cpf/xyz/logging/XyzFileLogEducationSample.java | xyz/src/test/java/cpf/xyz/logging/XyzFileLogEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-MESSAGING-001 | XYZ | cpf.xyz.messaging | MESSAGING | XyzBrokerPublishEducationSample | xyz/src/main/java/cpf/xyz/messaging/XyzBrokerPublishEducationSample.java | xyz/src/test/java/cpf/xyz/messaging/XyzBrokerPublishEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-MESSAGING-002 | XYZ | cpf.xyz.messaging | MESSAGING | XyzOutboxInboxEducationSample | xyz/src/main/java/cpf/xyz/messaging/XyzOutboxInboxEducationSample.java | xyz/src/test/java/cpf/xyz/messaging/XyzOutboxInboxEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-OPERATION-001 | XYZ | cpf.xyz.batch | OPERATION | XyzAdmBatchLogQueryEducationSample | xyz/src/main/java/cpf/xyz/batch/XyzAdmBatchLogQueryEducationSample.java | xyz/src/test/java/cpf/xyz/batch/XyzAdmBatchLogQueryEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-OPERATION-002 | XYZ | cpf.xyz.logging | OPERATION | XyzOperationTraceEducationSample | xyz/src/main/java/cpf/xyz/logging/XyzOperationTraceEducationSample.java | xyz/src/test/java/cpf/xyz/logging/XyzOperationTraceEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-PAGINATION-001 | XYZ | cpf.xyz.pagination | PAGINATION | XyzPaginationEducationSample | xyz/src/main/java/cpf/xyz/pagination/XyzPaginationEducationSample.java | xyz/src/test/java/cpf/xyz/pagination/XyzPaginationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-QUERY-001 | XYZ | cpf.xyz.query | QUERY | XyzQueryEducationSample | xyz/src/main/java/cpf/xyz/query/XyzQueryEducationSample.java | xyz/src/test/java/cpf/xyz/query/XyzQueryEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-SECURITY-001 | XYZ | cpf.xyz.security | SECURITY | XyzApiPermissionEducationSample | xyz/src/main/java/cpf/xyz/security/XyzApiPermissionEducationSample.java | xyz/src/test/java/cpf/xyz/security/XyzApiPermissionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-SECURITY-002 | XYZ | cpf.xyz.security | SECURITY | XyzDetailMaskingEducationSample | xyz/src/main/java/cpf/xyz/security/XyzDetailMaskingEducationSample.java | xyz/src/test/java/cpf/xyz/security/XyzDetailMaskingEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-SERVICECALL-001 | XYZ | cpf.xyz.servicecall | SERVICECALL | XyzServiceCallEngineEducationSample | xyz/src/main/java/cpf/xyz/servicecall/XyzServiceCallEngineEducationSample.java | xyz/src/test/java/cpf/xyz/servicecall/XyzServiceCallEngineEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-TELEGRAM-001 | XYZ | cpf.xyz.telegram | TELEGRAM | XyzFixedLengthBusinessUseEducationSample | xyz/src/main/java/cpf/xyz/telegram/XyzFixedLengthBusinessUseEducationSample.java | xyz/src/test/java/cpf/xyz/telegram/XyzFixedLengthBusinessUseEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-TRANSACTION-001 | XYZ | cpf.xyz.transaction | TRANSACTION | XyzTransactionEducationSample | xyz/src/main/java/cpf/xyz/transaction/XyzTransactionEducationSample.java | xyz/src/test/java/cpf/xyz/transaction/XyzTransactionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| XYZ Reference-VALIDATION-001 | XYZ | cpf.xyz.validation | VALIDATION | XyzValidationEducationSample | xyz/src/main/java/cpf/xyz/validation/XyzValidationEducationSample.java | xyz/src/test/java/cpf/xyz/validation/XyzValidationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-ARCHIVE-001 | BAT | cpf.bat.edu.archive | ARCHIVE | BatArchiveCompressionEducationSample | bat/src/main/java/cpf/bat/edu/archive/BatArchiveCompressionEducationSample.java | bat/src/test/java/cpf/bat/edu/archive/BatArchiveCompressionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-GENERAL-001 | BAT | cpf.bat.edu.tasklet.basic | GENERAL | BatTaskletEducationSample | bat/src/main/java/cpf/bat/edu/tasklet/basic/BatTaskletEducationSample.java | bat/src/test/java/cpf/bat/edu/tasklet/basic/BatTaskletEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CENTERCUT-001 | BAT | cpf.bat.edu.centercut | CENTERCUT | BatCenterCutExecutionEducationSample | bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutExecutionEducationSample.java | bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutExecutionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CENTERCUT-002 | BAT | cpf.bat.edu.centercut | CENTERCUT | BatCenterCutItemProcessingEducationSample | bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutItemProcessingEducationSample.java | bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutItemProcessingEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CENTERCUT-003 | BAT | cpf.bat.edu.centercut | CENTERCUT | BatCenterCutResultSummaryEducationSample | bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutResultSummaryEducationSample.java | bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutResultSummaryEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CENTERCUT-004 | BAT | cpf.bat.edu.centercut | CENTERCUT | BatCenterCutRetryEducationSample | bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSample.java | bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CENTERCUT-005 | BAT | cpf.bat.edu.centercut | CENTERCUT | BatCenterCutTargetEducationSample | bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutTargetEducationSample.java | bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutTargetEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-CHUNK-001 | BAT | cpf.bat.edu.chunk | CHUNK | BatChunkJobEducationSample | bat/src/main/java/cpf/bat/edu/chunk/BatChunkJobEducationSample.java | bat/src/test/java/cpf/bat/edu/chunk/BatChunkJobEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-IDEMPOTENCY-001 | BAT | cpf.bat.edu.idempotency | IDEMPOTENCY | BatDuplicateExecutionGuardEducationSample | bat/src/main/java/cpf/bat/edu/idempotency/BatDuplicateExecutionGuardEducationSample.java | bat/src/test/java/cpf/bat/edu/idempotency/BatDuplicateExecutionGuardEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-IDEMPOTENCY-002 | BAT | cpf.bat.edu.idempotency | IDEMPOTENCY | BatIdempotencyEducationSample | bat/src/main/java/cpf/bat/edu/idempotency/BatIdempotencyEducationSample.java | bat/src/test/java/cpf/bat/edu/idempotency/BatIdempotencyEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-JOB-001 | BAT | cpf.bat.edu.tasklet.basic | JOB | BatJobParameterValidationEducationSample | bat/src/main/java/cpf/bat/edu/tasklet/basic/BatJobParameterValidationEducationSample.java | bat/src/test/java/cpf/bat/edu/tasklet/basic/BatJobParameterValidationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-JOB-002 | BAT | cpf.bat.edu.restart.checkpoint | JOB | BatJobStatusTransitionEducationSample | bat/src/main/java/cpf/bat/edu/restart/checkpoint/BatJobStatusTransitionEducationSample.java | bat/src/test/java/cpf/bat/edu/restart/checkpoint/BatJobStatusTransitionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-LOGGING-001 | BAT | cpf.bat.edu.logging | LOGGING | BatJobLogEducationSample | bat/src/main/java/cpf/bat/edu/logging/BatJobLogEducationSample.java | bat/src/test/java/cpf/bat/edu/logging/BatJobLogEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-ONDEMAND-001 | BAT | cpf.bat.edu.ondemand | ONDEMAND | BatOnDemandEducationSample | bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandEducationSample.java | bat/src/test/java/cpf/bat/edu/ondemand/BatOnDemandEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-RECONCILIATION-001 | BAT | cpf.bat.edu.reconciliation | RECONCILIATION | BatReconciliationEducationSample | bat/src/main/java/cpf/bat/edu/reconciliation/BatReconciliationEducationSample.java | bat/src/test/java/cpf/bat/edu/reconciliation/BatReconciliationEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-RECONCILIATION-002 | BAT | cpf.bat.edu.reconciliation | RECONCILIATION | BatUnknownResultEducationSample | bat/src/main/java/cpf/bat/edu/reconciliation/BatUnknownResultEducationSample.java | bat/src/test/java/cpf/bat/edu/reconciliation/BatUnknownResultEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-RESTART-001 | BAT | cpf.bat.edu.restart | RESTART | BatRestartEducationSample | bat/src/main/java/cpf/bat/edu/restart/BatRestartEducationSample.java | bat/src/test/java/cpf/bat/edu/restart/BatRestartEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-RETRY-001 | BAT | cpf.bat.edu.retry | RETRY | BatRetryEducationSample | bat/src/main/java/cpf/bat/edu/retry/BatRetryEducationSample.java | bat/src/test/java/cpf/bat/edu/retry/BatRetryEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-SERVICECALL-001 | BAT | cpf.bat.edu.servicecall | SERVICECALL | BatFacadeCallEducationSample | bat/src/main/java/cpf/bat/edu/servicecall/BatFacadeCallEducationSample.java | bat/src/test/java/cpf/bat/edu/servicecall/BatFacadeCallEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-SERVICECALL-002 | BAT | cpf.bat.edu.servicecall | SERVICECALL | BatOnlineServiceCallEducationSample | bat/src/main/java/cpf/bat/edu/servicecall/BatOnlineServiceCallEducationSample.java | bat/src/test/java/cpf/bat/edu/servicecall/BatOnlineServiceCallEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-SERVICECALL-003 | BAT | cpf.bat.edu.servicecall | SERVICECALL | BatServiceCallEngineEducationSample | bat/src/main/java/cpf/bat/edu/servicecall/BatServiceCallEngineEducationSample.java | bat/src/test/java/cpf/bat/edu/servicecall/BatServiceCallEngineEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-TRANSACTION-001 | BAT | cpf.bat.edu.transaction | TRANSACTION | BatItemTransactionEducationSample | bat/src/main/java/cpf/bat/edu/transaction/BatItemTransactionEducationSample.java | bat/src/test/java/cpf/bat/edu/transaction/BatItemTransactionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-TRANSACTION-002 | BAT | cpf.bat.edu.transaction | TRANSACTION | BatPartialRollbackEducationSample | bat/src/main/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSample.java | bat/src/test/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |
| BAT-EDU-TRANSACTION-003 | BAT | cpf.bat.edu.transaction | TRANSACTION | BatTransactionEducationSample | bat/src/main/java/cpf/bat/edu/transaction/BatTransactionEducationSample.java | bat/src/test/java/cpf/bat/edu/transaction/BatTransactionEducationSampleTest.java | specs/evidence/20260716_01/full-test.sanitized.log | unit-test | false | false | 완료 | XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다. |

---

## XYZ/BAT sample runtime 정책 보정

- XYZ는 reference business module이며 고객 production 기본 배포 대상이 아니다.
- XYZ sample은 local/dev/test와 명시적 reference runtime에서 검증한다.
- production 기본 inventory, DB, secret, Gateway route, ADM/BZA menu seed에는 넣지 않는다.
- XYZ controller/service도 canonical ModuleBaseController/ModuleBaseService 계층을 따른다.
- BAT sample은 BAT runtime module 내부 sample classification이며 공통 PFW logging·transaction·service-call engine을 사용한다.
- sample class 존재와 unit test만으로 production capability 완료를 판정하지 않는다.

---

## 공통 API sample 품질 기준

모든 sample을 다음 두 종류로 분류한다.

### Basic sample

- 업무 입력과 typed request만 사용
- 거래/서비스 ID 또는 generated typed client 사용
- 1~3줄 핵심 호출
- central default policy 사용
- raw timeout/retry/URI/header/attribute/adapter 금지
- 정상·표준 오류 예제
- JavaDoc와 OpenAPI link

### Advanced sample

- 승인된 named policy override
- timeout/retry/circuit/bulkhead 조정
- local/remote adapter extension
- custom codec/masking/log field
- 운영 권한·상한·감사와 함께 설명

`XyzServiceCallEngineEducationSample`과 유사하게 transport detail을 모두 노출하는 코드는
Basic이 아니라 Advanced로 이동하거나 typed client sample로 대체한다.

각 sample은 다음이 모두 필요하다.

- compile test
- runtime smoke
- current API 사용
- deprecated API 0건 또는 migration 전용 분리
- JavaDoc/OpenAPI link 유효
- sample matrix와 source/test/evidence 동일 commit

---

## 3단 확장·Overload·Property sample 기준

모든 sample은 다음을 보여야 한다.

- PFW Base/Contract → Domain Base/Contract → Feature 구현
- 업무 입력만 사용하는 기본 overload
- 중앙 default policy 사용
- typed options/named policy를 사용하는 advanced overload
- 허용된 override hook
- 우회 불가한 보안·감사·transaction 단계
- 한글 주석이 있는 관련 property
- effective property 확인 방법
- deprecated API migration
- JavaDoc link
- compile/runtime test

sample에서 raw timeout/retry/URI/header/adapter를 기본 코드로 노출하지 않는다.

## 2026-07-20 정본 기반 EDU 완료 기준

모든 PFW public API/SPI는 XYZ 또는 BAT EDU와 1:1 연결한다.

각 capability는 Basic, Advanced, typed API, 정상, 오류, 경계, 복구,
property, typed option/SPI, security·audit·masking, local/remote,
unit·integration·runtime, OpenAPI·JavaDoc, evidence와 migration을 포함한다.

현재 완료로 인정하지 않는 핵심:

- ACC generator lifecycle
- Batch Agent/Worker
- actual file/DB logging
- real broker/multi-instance
- external unknown result/reconciliation
- ADM/BZA Vue product architecture와 browser E2E
