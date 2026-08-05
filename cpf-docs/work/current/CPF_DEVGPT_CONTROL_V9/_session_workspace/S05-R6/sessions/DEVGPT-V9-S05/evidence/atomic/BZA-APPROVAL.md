# BZA-APPROVAL atomic adjudication evidence

- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Owner module: `cpf-biz-admin`
- Verification: `DIRECT_HARNESS_PLUS_SOURCE_TRACE`
- Source status: `CONFIRMED_AT_EXACT_SHA`
- Consumer status: `CONFIRMED`

## Actual Source
cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/controller; cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/service/BzaApprovalPolicyService.java; cpf-biz-admin/frontend/src/features/approval-inbox

## Actual Consumer Call Path
BZA approval inbox/policy/submission/delegation pages → generated client → BZA approval controllers → BzaApprovalPolicyService/repository/audit

## Test and Assertion
cpf-biz-admin/src/test/java/com/cpf/bizadmin/approval/service/BzaApprovalPolicyServiceIdempotencyTest.java; evidence/BZA_APPROVAL_IDEMPOTENCY_JAVA21_HARNESS.log; evidence/BZA_OPENAPI_VALIDATION.log

## OpenAPI Operations
19

## Direct/Alternative Evidence
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/CANONICAL_SOURCE_TRACE.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/SOURCE_PATH_EXISTENCE_AUDIT.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/ADM_BZA_OPENAPI_OPERATION_INVENTORY.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_BZA_APPROVAL_JAVA21.log`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_FRONTEND_ASSEMBLED_CONSUMER_AND_OPENAPI.log`

## Adjudication
Assigned acceptance is individually recorded in WORK_ITEM_RESULT, DEVELOPMENT_REQUIREMENT_RESULT, and DEVELOPMENT_SCENARIO_RESULT. Runtime-only gaps are identified as integration rerun conditions and are not represented as direct execution.
