# ADM-RBAC atomic adjudication evidence

- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Owner module: `cpf-admin`
- Verification: `ALTERNATIVE_SOURCE_CONSUMER_OPENAPI_TRACE`
- Source status: `CONFIRMED_AT_EXACT_SHA`
- Consumer status: `CONFIRMED`

## Actual Source
cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmPermissionController.java; cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmPermissionService.java

## Actual Consumer Call Path
ADM menu/button/API permission UI → permission HTTP operations → AdmPermissionController → AdmPermissionService → permission repository/server-side enforcement

## Test and Assertion
cpf-admin/src/test/java/com/cpf/admin/opr/service/AdmPermissionServiceTest.java; evidence/ADM_BZA_OPENAPI_OPERATION_INVENTORY.csv; evidence/CANONICAL_SOURCE_TRACE.csv

## OpenAPI Operations
29

## Direct/Alternative Evidence
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/CANONICAL_SOURCE_TRACE.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/SOURCE_PATH_EXISTENCE_AUDIT.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/ADM_BZA_OPENAPI_OPERATION_INVENTORY.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_FRONTEND_ASSEMBLED_CONSUMER_AND_OPENAPI.log`

## Adjudication
Assigned acceptance is individually recorded in WORK_ITEM_RESULT, DEVELOPMENT_REQUIREMENT_RESULT, and DEVELOPMENT_SCENARIO_RESULT. Runtime-only gaps are identified as integration rerun conditions and are not represented as direct execution.
