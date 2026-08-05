# ADM-CENTER atomic adjudication evidence

- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Owner module: `cpf-admin`
- Verification: `DIRECT_HARNESS_PLUS_GENERATED_CONSUMER_TRACE`
- Source status: `CONFIRMED_REV005_OVERLAY`
- Consumer status: `CONFIRMED`

## Actual Source
cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCenterCutController.java; cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCenterCutOperationService.java; cpf-admin/src/main/java/com/cpf/admin/opr/centercut/RemoteAdmCenterCutCommandClient.java; cpf-admin/frontend/openapi/cpf-openapi.json; cpf-admin/frontend/src/generated/cpf-operation-contract.ts; cpf-admin/frontend/src/generated/orval/cpf-api.ts

## Actual Consumer Call Path
ADM batch-center-cut route/page → generated operation contract → approval request/detail → execution-scoped ADM API → approval ledger → BAT /api/v1/batch/center-cut/executions/{executionId}/{reprocess-failed|reconcile-unknown}

## Test and Assertion
Java21 exact-source behavior/remote-header harness; execution-scope Controller test; FAILED/UNKNOWN UI guards; OpenAPI 323 operation validation; generated facade/Orval/route contract closure; job-scope mutation forbidden

## OpenAPI Operations
9

## Direct/Alternative Evidence
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/CANONICAL_SOURCE_TRACE.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/SOURCE_PATH_EXISTENCE_AUDIT.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/ADM_BZA_OPENAPI_OPERATION_INVENTORY.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_CENTER_CUT_SERVICE_COMPILE.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_CENTER_CUT_SERVICE_RUN.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_CENTER_CUT_REMOTE_JAVA21.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_CENTER_CUT_APPROVAL_JAVA21.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_BATCH_OWNER_ADAPTER_JAVA21.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_FRONTEND_ASSEMBLED_CONSUMER_AND_OPENAPI.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_CENTER_CUT_TEST_API_DRIFT_CORRECTION.log`

## Adjudication
Assigned acceptance is individually recorded in WORK_ITEM_RESULT, DEVELOPMENT_REQUIREMENT_RESULT, and DEVELOPMENT_SCENARIO_RESULT. Runtime-only gaps are identified as integration rerun conditions and are not represented as direct execution.
