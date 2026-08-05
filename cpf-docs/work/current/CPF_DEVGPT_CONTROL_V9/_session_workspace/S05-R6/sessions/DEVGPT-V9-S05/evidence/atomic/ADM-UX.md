# ADM-UX atomic adjudication evidence

- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Owner module: `cpf-admin`
- Verification: `ALTERNATIVE_SOURCE_CONSUMER_OPENAPI_TRACE`
- Source status: `CONFIRMED_AT_EXACT_SHA`
- Consumer status: `CONFIRMED`

## Actual Source
cpf-admin/frontend/src/App.vue; cpf-admin/frontend/src/app/router.ts; cpf-admin/frontend/src/app/routes.ts; cpf-admin/frontend/src/features/errors; cpf-admin/frontend/src/generated

## Actual Consumer Call Path
ADM router/menu → feature pages → generated client → backend OpenAPI; global loading/error/401·403·404·409·429·500·503 handling

## Test and Assertion
cpf-admin/frontend/scripts/validate-openapi.mjs; evidence/ADM_OPENAPI_VALIDATION.log; evidence/ADM_OPENAPI_LIFECYCLE.log; evidence/CANONICAL_SOURCE_TRACE.csv

## OpenAPI Operations
112

## Direct/Alternative Evidence
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/CANONICAL_SOURCE_TRACE.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/SOURCE_PATH_EXISTENCE_AUDIT.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/ADM_BZA_OPENAPI_OPERATION_INVENTORY.csv`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/evidence/REV006_FRONTEND_ASSEMBLED_CONSUMER_AND_OPENAPI.log`

## Adjudication
Assigned acceptance is individually recorded in WORK_ITEM_RESULT, DEVELOPMENT_REQUIREMENT_RESULT, and DEVELOPMENT_SCENARIO_RESULT. Runtime-only gaps are identified as integration rerun conditions and are not represented as direct execution.
