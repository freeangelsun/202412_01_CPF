# CPF Education Canonical Package Index

> 기준 Catalog: `cpf-education/src/main/resources/verification/manual-135-catalog.json`
> System Code: `EDU` / Package Base: `com.cpf.education`

## Canonical 14 Roots

- `base` → `com.cpf.education.base`
- `batch` → `com.cpf.education.batch`
- `common` → `com.cpf.education.common`
- `data` → `com.cpf.education.data`
- `file` → `com.cpf.education.file`
- `generator` → `com.cpf.education.generator`
- `integration` → `com.cpf.education.integration`
- `messaging` → `com.cpf.education.messaging`
- `operations` → `com.cpf.education.operations`
- `scenarios` → `com.cpf.education.scenarios`
- `security` → `com.cpf.education.security`
- `transaction` → `com.cpf.education.transaction`
- `verification` → `com.cpf.education.verification`
- `web` → `com.cpf.education.web`

## Executable Education Coverage

| Requirement | Source | Tests | Contract |
|---|---|---:|---|
| `EDU-DEV-01` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/generator/domain/EduDev01Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/generator/domain/scenario-contract.json` |
| `EDU-DEV-02` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/query/scoped/EduDev02Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/query/scoped/scenario-contract.json` |
| `EDU-DEV-03` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/command/audit/EduDev03Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/command/audit/scenario-contract.json` |
| `EDU-DEV-04` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/concurrency/optimisticlock/EduDev04Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/concurrency/optimisticlock/scenario-contract.json` |
| `EDU-DEV-05` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/idempotency/payment/EduDev05Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/idempotency/payment/scenario-contract.json` |
| `EDU-DEV-06` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/servicecall/topology/EduDev06Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/servicecall/topology/scenario-contract.json` |
| `EDU-DEV-07` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/messaging/outboxinbox/EduDev07Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/messaging/outboxinbox/scenario-contract.json` |
| `EDU-DEV-08` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/attachment/EduDev08Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/attachment/scenario-contract.json` |
| `EDU-DEV-09` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/counterparty/rest/EduDev09Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/counterparty/rest/scenario-contract.json` |
| `EDU-DEV-10` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/counterparty/fixedwidth/EduDev10Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/counterparty/fixedwidth/scenario-contract.json` |
| `EDU-DEV-11` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/security/authorization/EduDev11Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/security/authorization/scenario-contract.json` |
| `EDU-DEV-12` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/runtime/featuremanagement/EduDev12Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/runtime/featuremanagement/scenario-contract.json` |
| `EDU-DEV-13` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/notification/export/EduDev13Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/notification/export/scenario-contract.json` |
| `EDU-DEV-14` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/database/migration/EduDev14Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/database/migration/scenario-contract.json` |
| `EDU-DEV-15` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/resilience/recovery/EduDev15Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/resilience/recovery/scenario-contract.json` |
| `EDU-DEV-16` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/query/cursor/EduDev16Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/query/cursor/scenario-contract.json` |
| `EDU-DEV-17` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/bulkimport/EduDev17Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/bulkimport/scenario-contract.json` |
| `EDU-DEV-18` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/lifecycle/softdelete/EduDev18Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/lifecycle/softdelete/scenario-contract.json` |
| `EDU-DEV-19` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/masterdata/effectiveperiod/EduDev19Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/masterdata/effectiveperiod/scenario-contract.json` |
| `EDU-DEV-20` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/workflow/statemachine/EduDev20Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/workflow/statemachine/scenario-contract.json` |
| `EDU-DEV-21` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/messaging/transactionaloutbox/EduDev21Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/messaging/transactionaloutbox/scenario-contract.json` |
| `EDU-DEV-22` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/workflow/saga/EduDev22Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/workflow/saga/scenario-contract.json` |
| `EDU-DEV-23` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/contract/validation/EduDev23Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/contract/validation/scenario-contract.json` |
| `EDU-DEV-24` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/asyncoperation/lifecycle/EduDev24Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/asyncoperation/lifecycle/scenario-contract.json` |
| `EDU-DEV-25` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/counterparty/webhook/EduDev25Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/counterparty/webhook/scenario-contract.json` |
| `EDU-DEV-26` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/sftp/EduDev26Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/sftp/scenario-contract.json` |
| `EDU-DEV-27` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/counterparty/soap/EduDev27Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/counterparty/soap/scenario-contract.json` |
| `EDU-DEV-28` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/multipart/EduDev28Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/multipart/scenario-contract.json` |
| `EDU-DEV-29` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/quarantine/EduDev29Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/quarantine/scenario-contract.json` |
| `EDU-DEV-30` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/file/objectstorage/EduDev30Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/file/objectstorage/scenario-contract.json` |
| `EDU-DEV-31` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/notification/multichannel/EduDev31Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/notification/multichannel/scenario-contract.json` |
| `EDU-DEV-32` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/security/cryptography/EduDev32Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/security/cryptography/scenario-contract.json` |
| `EDU-DEV-33` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/security/session/EduDev33Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/security/session/scenario-contract.json` |
| `EDU-DEV-34` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/api/quota/EduDev34Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/api/quota/scenario-contract.json` |
| `EDU-DEV-35` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/runtime/featuretoggle/EduDev35Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/runtime/featuretoggle/scenario-contract.json` |
| `EDU-DEV-36` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/cache/consistency/EduDev36Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/cache/consistency/scenario-contract.json` |
| `EDU-DEV-37` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/concurrency/lease/EduDev37Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/concurrency/lease/scenario-contract.json` |
| `EDU-DEV-38` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/security/multitenancy/EduDev38Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/security/multitenancy/scenario-contract.json` |
| `EDU-DEV-39` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/calendar/businessday/EduDev39Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/calendar/businessday/scenario-contract.json` |
| `EDU-DEV-40` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/money/exchange/EduDev40Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/money/exchange/scenario-contract.json` |
| `EDU-DEV-41` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/audit/evidence/EduDev41Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/audit/evidence/scenario-contract.json` |
| `EDU-DEV-42` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/observability/correlation/EduDev42Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/observability/correlation/scenario-contract.json` |
| `EDU-DEV-43` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/api/versioning/EduDev43Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/api/versioning/scenario-contract.json` |
| `EDU-DEV-44` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/messaging/schema/EduDev44Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/messaging/schema/scenario-contract.json` |
| `EDU-DEV-45` | `cpf-education/src/main/java/com/cpf/education/scenarios/online/query/searchindex/EduDev45Handler.java` | 5 | `cpf-education/src/main/resources/scenarios/online/query/searchindex/scenario-contract.json` |
| `EDU-BAT-01` | `cpf-education/src/main/java/com/cpf/education/batch/tasklet/close/EduBat01Handler.java` | 5 | `cpf-education/src/main/resources/batch/tasklet/close/scenario-contract.json` |
| `EDU-BAT-02` | `cpf-education/src/main/java/com/cpf/education/batch/chunk/membergrade/EduBat02Handler.java` | 5 | `cpf-education/src/main/resources/batch/chunk/membergrade/scenario-contract.json` |
| `EDU-BAT-03` | `cpf-education/src/main/java/com/cpf/education/batch/file/csv/EduBat03Handler.java` | 5 | `cpf-education/src/main/resources/batch/file/csv/scenario-contract.json` |
| `EDU-BAT-04` | `cpf-education/src/main/java/com/cpf/education/batch/partition/range/EduBat04Handler.java` | 5 | `cpf-education/src/main/resources/batch/partition/range/scenario-contract.json` |
| `EDU-BAT-05` | `cpf-education/src/main/java/com/cpf/education/batch/remote/worker/EduBat05Handler.java` | 5 | `cpf-education/src/main/resources/batch/remote/worker/scenario-contract.json` |
| `EDU-BAT-06` | `cpf-education/src/main/java/com/cpf/education/batch/centercut/approval/EduBat06Handler.java` | 5 | `cpf-education/src/main/resources/batch/centercut/approval/scenario-contract.json` |
| `EDU-BAT-07` | `cpf-education/src/main/java/com/cpf/education/batch/scheduler/businessday/EduBat07Handler.java` | 5 | `cpf-education/src/main/resources/batch/scheduler/businessday/scenario-contract.json` |
| `EDU-BAT-08` | `cpf-education/src/main/java/com/cpf/education/batch/jobpack/version/EduBat08Handler.java` | 5 | `cpf-education/src/main/resources/batch/jobpack/version/scenario-contract.json` |
| `EDU-BAT-09` | `cpf-education/src/main/java/com/cpf/education/batch/recovery/restart/EduBat09Handler.java` | 5 | `cpf-education/src/main/resources/batch/recovery/restart/scenario-contract.json` |
| `EDU-BAT-10` | `cpf-education/src/main/java/com/cpf/education/batch/reconcile/requestloss/EduBat10Handler.java` | 5 | `cpf-education/src/main/resources/batch/reconcile/requestloss/scenario-contract.json` |
| `EDU-BAT-11` | `cpf-education/src/main/java/com/cpf/education/batch/flow/conditional/EduBat11Handler.java` | 5 | `cpf-education/src/main/resources/batch/flow/conditional/scenario-contract.json` |
| `EDU-BAT-12` | `cpf-education/src/main/java/com/cpf/education/batch/faulttolerance/retryskip/EduBat12Handler.java` | 5 | `cpf-education/src/main/resources/batch/faulttolerance/retryskip/scenario-contract.json` |
| `EDU-BAT-13` | `cpf-education/src/main/java/com/cpf/education/batch/checkpoint/writercommit/EduBat13Handler.java` | 5 | `cpf-education/src/main/resources/batch/checkpoint/writercommit/scenario-contract.json` |
| `EDU-BAT-14` | `cpf-education/src/main/java/com/cpf/education/batch/instance/parameter/EduBat14Handler.java` | 5 | `cpf-education/src/main/resources/batch/instance/parameter/scenario-contract.json` |
| `EDU-BAT-15` | `cpf-education/src/main/java/com/cpf/education/batch/backfill/latearrival/EduBat15Handler.java` | 5 | `cpf-education/src/main/resources/batch/backfill/latearrival/scenario-contract.json` |
| `EDU-BAT-16` | `cpf-education/src/main/java/com/cpf/education/batch/incremental/watermark/EduBat16Handler.java` | 5 | `cpf-education/src/main/resources/batch/incremental/watermark/scenario-contract.json` |
| `EDU-BAT-17` | `cpf-education/src/main/java/com/cpf/education/batch/file/secureoutput/EduBat17Handler.java` | 5 | `cpf-education/src/main/resources/batch/file/secureoutput/scenario-contract.json` |
| `EDU-BAT-18` | `cpf-education/src/main/java/com/cpf/education/batch/file/validation/EduBat18Handler.java` | 5 | `cpf-education/src/main/resources/batch/file/validation/scenario-contract.json` |
| `EDU-BAT-19` | `cpf-education/src/main/java/com/cpf/education/batch/file/faninout/EduBat19Handler.java` | 5 | `cpf-education/src/main/resources/batch/file/faninout/scenario-contract.json` |
| `EDU-BAT-20` | `cpf-education/src/main/java/com/cpf/education/batch/scheduler/misfire/EduBat20Handler.java` | 5 | `cpf-education/src/main/resources/batch/scheduler/misfire/scenario-contract.json` |
| `EDU-BAT-21` | `cpf-education/src/main/java/com/cpf/education/batch/concurrency/execution/EduBat21Handler.java` | 5 | `cpf-education/src/main/resources/batch/concurrency/execution/scenario-contract.json` |
| `EDU-BAT-22` | `cpf-education/src/main/java/com/cpf/education/batch/calendar/businessday/EduBat22Handler.java` | 5 | `cpf-education/src/main/resources/batch/calendar/businessday/scenario-contract.json` |
| `EDU-BAT-23` | `cpf-education/src/main/java/com/cpf/education/batch/lifecycle/stopabandon/EduBat23Handler.java` | 5 | `cpf-education/src/main/resources/batch/lifecycle/stopabandon/scenario-contract.json` |
| `EDU-BAT-24` | `cpf-education/src/main/java/com/cpf/education/batch/remote/reassignment/EduBat24Handler.java` | 5 | `cpf-education/src/main/resources/batch/remote/reassignment/scenario-contract.json` |
| `EDU-BAT-25` | `cpf-education/src/main/java/com/cpf/education/batch/partition/rebalance/EduBat25Handler.java` | 5 | `cpf-education/src/main/resources/batch/partition/rebalance/scenario-contract.json` |
| `EDU-BAT-26` | `cpf-education/src/main/java/com/cpf/education/batch/centercut/reconcile/EduBat26Handler.java` | 5 | `cpf-education/src/main/resources/batch/centercut/reconcile/scenario-contract.json` |
| `EDU-BAT-27` | `cpf-education/src/main/java/com/cpf/education/batch/jobpack/recovery/EduBat27Handler.java` | 5 | `cpf-education/src/main/resources/batch/jobpack/recovery/scenario-contract.json` |
| `EDU-BAT-28` | `cpf-education/src/main/java/com/cpf/education/batch/agent/offline/EduBat28Handler.java` | 5 | `cpf-education/src/main/resources/batch/agent/offline/scenario-contract.json` |
| `EDU-BAT-29` | `cpf-education/src/main/java/com/cpf/education/batch/dryrun/preview/EduBat29Handler.java` | 5 | `cpf-education/src/main/resources/batch/dryrun/preview/scenario-contract.json` |
| `EDU-BAT-30` | `cpf-education/src/main/java/com/cpf/education/batch/performance/backpressure/EduBat30Handler.java` | 5 | `cpf-education/src/main/resources/batch/performance/backpressure/scenario-contract.json` |
| `EDU-ADM-01` | `cpf-education/src/main/java/com/cpf/education/operations/admin/reuse/EduAdm01Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/reuse/scenario-contract.json` |
| `EDU-ADM-02` | `cpf-education/src/main/java/com/cpf/education/operations/admin/query/EduAdm02Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/query/scenario-contract.json` |
| `EDU-ADM-03` | `cpf-education/src/main/java/com/cpf/education/operations/admin/command/EduAdm03Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/command/scenario-contract.json` |
| `EDU-ADM-04` | `cpf-education/src/main/java/com/cpf/education/operations/admin/approval/EduAdm04Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/approval/scenario-contract.json` |
| `EDU-ADM-05` | `cpf-education/src/main/java/com/cpf/education/operations/admin/asyncoperation/EduAdm05Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/asyncoperation/scenario-contract.json` |
| `EDU-ADM-06` | `cpf-education/src/main/java/com/cpf/education/operations/admin/partialrecovery/EduAdm06Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/partialrecovery/scenario-contract.json` |
| `EDU-ADM-07` | `cpf-education/src/main/java/com/cpf/education/operations/admin/customscreen/EduAdm07Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/customscreen/scenario-contract.json` |
| `EDU-ADM-08` | `cpf-education/src/main/java/com/cpf/education/operations/admin/search/EduAdm08Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/search/scenario-contract.json` |
| `EDU-ADM-09` | `cpf-education/src/main/java/com/cpf/education/operations/admin/detail/EduAdm09Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/detail/scenario-contract.json` |
| `EDU-ADM-10` | `cpf-education/src/main/java/com/cpf/education/operations/admin/bulk/EduAdm10Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/bulk/scenario-contract.json` |
| `EDU-ADM-11` | `cpf-education/src/main/java/com/cpf/education/operations/admin/configuration/EduAdm11Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/configuration/scenario-contract.json` |
| `EDU-ADM-12` | `cpf-education/src/main/java/com/cpf/education/operations/admin/incident/EduAdm12Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/incident/scenario-contract.json` |
| `EDU-ADM-13` | `cpf-education/src/main/java/com/cpf/education/operations/admin/evidence/EduAdm13Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/evidence/scenario-contract.json` |
| `EDU-ADM-14` | `cpf-education/src/main/java/com/cpf/education/operations/admin/topology/EduAdm14Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/topology/scenario-contract.json` |
| `EDU-ADM-15` | `cpf-education/src/main/java/com/cpf/education/operations/admin/correlation/EduAdm15Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/correlation/scenario-contract.json` |
| `EDU-ADM-16` | `cpf-education/src/main/java/com/cpf/education/operations/admin/notification/EduAdm16Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/notification/scenario-contract.json` |
| `EDU-ADM-17` | `cpf-education/src/main/java/com/cpf/education/operations/admin/session/EduAdm17Handler.java` | 5 | `cpf-education/src/main/resources/operations/admin/session/scenario-contract.json` |
| `EDU-BZA-01` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/organization/EduBackoffice01Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/organization/scenario-contract.json` |
| `EDU-BZA-02` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/authorization/EduBackoffice02Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/authorization/scenario-contract.json` |
| `EDU-BZA-03` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/policysimulation/EduBackoffice03Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/policysimulation/scenario-contract.json` |
| `EDU-BZA-04` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/approvalflow/EduBackoffice04Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/approvalflow/scenario-contract.json` |
| `EDU-BZA-05` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/delegation/EduBackoffice05Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/delegation/scenario-contract.json` |
| `EDU-BZA-06` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/evidence/EduBackoffice06Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/evidence/scenario-contract.json` |
| `EDU-BZA-07` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/directory/EduBackoffice07Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/directory/scenario-contract.json` |
| `EDU-BZA-08` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/reorganization/EduBackoffice08Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/reorganization/scenario-contract.json` |
| `EDU-BZA-09` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/lifecycle/EduBackoffice09Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/lifecycle/scenario-contract.json` |
| `EDU-BZA-10` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/separationofduties/EduBackoffice10Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/separationofduties/scenario-contract.json` |
| `EDU-BZA-11` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/approvalhistory/EduBackoffice11Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/approvalhistory/scenario-contract.json` |
| `EDU-BZA-12` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/attachment/EduBackoffice12Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/attachment/scenario-contract.json` |
| `EDU-BZA-13` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/privacyexport/EduBackoffice13Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/privacyexport/scenario-contract.json` |
| `EDU-BZA-14` | `cpf-education/src/main/java/com/cpf/education/operations/backoffice/rollback/EduBackoffice14Handler.java` | 5 | `cpf-education/src/main/resources/operations/backoffice/rollback/scenario-contract.json` |
| `EDU-GW-01` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/servergroup/EduGw01Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/servergroup/scenario-contract.json` |
| `EDU-GW-02` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/route/EduGw02Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/route/scenario-contract.json` |
| `EDU-GW-03` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/security/EduGw03Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/security/scenario-contract.json` |
| `EDU-GW-04` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/resilience/EduGw04Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/resilience/scenario-contract.json` |
| `EDU-GW-05` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/publish/EduGw05Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/publish/scenario-contract.json` |
| `EDU-GW-06` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/reconcile/EduGw06Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/reconcile/scenario-contract.json` |
| `EDU-GW-07` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/registry/EduGw07Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/registry/scenario-contract.json` |
| `EDU-GW-08` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/health/EduGw08Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/health/scenario-contract.json` |
| `EDU-GW-09` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/drain/EduGw09Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/drain/scenario-contract.json` |
| `EDU-GW-10` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/rejection/EduGw10Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/rejection/scenario-contract.json` |
| `EDU-GW-11` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/version/EduGw11Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/version/scenario-contract.json` |
| `EDU-GW-12` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/ratecontrol/EduGw12Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/ratecontrol/scenario-contract.json` |
| `EDU-GW-13` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/audit/EduGw13Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/audit/scenario-contract.json` |
| `EDU-GW-14` | `cpf-education/src/main/java/com/cpf/education/operations/gateway/recovery/EduGw14Handler.java` | 5 | `cpf-education/src/main/resources/operations/gateway/recovery/scenario-contract.json` |
| `EDU-OPS-01` | `cpf-education/src/main/java/com/cpf/education/operations/platform/install/artifact/EduOps01Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/install/artifact/scenario-contract.json` |
| `EDU-OPS-02` | `cpf-education/src/main/java/com/cpf/education/operations/platform/configuration/validation/EduOps02Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/configuration/validation/scenario-contract.json` |
| `EDU-OPS-03` | `cpf-education/src/main/java/com/cpf/education/operations/platform/security/secretrotation/EduOps03Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/security/secretrotation/scenario-contract.json` |
| `EDU-OPS-04` | `cpf-education/src/main/java/com/cpf/education/operations/platform/database/lifecycle/EduOps04Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/database/lifecycle/scenario-contract.json` |
| `EDU-OPS-05` | `cpf-education/src/main/java/com/cpf/education/operations/platform/messaging/kafka/EduOps05Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/messaging/kafka/scenario-contract.json` |
| `EDU-OPS-06` | `cpf-education/src/main/java/com/cpf/education/operations/platform/lifecycle/startstop/EduOps06Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/lifecycle/startstop/scenario-contract.json` |
| `EDU-OPS-07` | `cpf-education/src/main/java/com/cpf/education/operations/platform/deployment/rolling/EduOps07Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/deployment/rolling/scenario-contract.json` |
| `EDU-OPS-08` | `cpf-education/src/main/java/com/cpf/education/operations/platform/deployment/bluegreen/EduOps08Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/deployment/bluegreen/scenario-contract.json` |
| `EDU-OPS-09` | `cpf-education/src/main/java/com/cpf/education/operations/platform/configuration/reconcile/EduOps09Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/configuration/reconcile/scenario-contract.json` |
| `EDU-OPS-10` | `cpf-education/src/main/java/com/cpf/education/operations/platform/observability/pipeline/EduOps10Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/observability/pipeline/scenario-contract.json` |
| `EDU-OPS-11` | `cpf-education/src/main/java/com/cpf/education/operations/platform/recovery/backuprestore/EduOps11Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/recovery/backuprestore/scenario-contract.json` |
| `EDU-OPS-12` | `cpf-education/src/main/java/com/cpf/education/operations/platform/recovery/disaster/EduOps12Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/recovery/disaster/scenario-contract.json` |
| `EDU-OPS-13` | `cpf-education/src/main/java/com/cpf/education/operations/platform/runbook/infrastructure/EduOps13Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/runbook/infrastructure/scenario-contract.json` |
| `EDU-OPS-14` | `cpf-education/src/main/java/com/cpf/education/operations/platform/security/incident/EduOps14Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/security/incident/scenario-contract.json` |
| `EDU-OPS-15` | `cpf-education/src/main/java/com/cpf/education/operations/platform/upgrade/compatibility/EduOps15Handler.java` | 5 | `cpf-education/src/main/resources/operations/platform/upgrade/compatibility/scenario-contract.json` |
