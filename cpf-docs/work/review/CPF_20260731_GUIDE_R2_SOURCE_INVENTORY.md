# CPF Guide R2 Source Inventory

- 기준 SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)
- 작성일: `2026-07-31`
- Git Write: 없음
- 목적: Guide 내용의 근거가 된 실제 Source·Config·Route·Script를 기록한다.

## 기준 정본

- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA32 Architecture·Requirement·Defect·Scenario·Evidence
- `cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md`
- `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER_RESULT.md`

## Build·Module

- `settings.gradle`
- `build.gradle`
- `gradle/cpf-stack.properties`
- `gradle/cpf-platform.properties`

## Generator

- `cpf-tools/generator/create-domain.ps1`
- `cpf-tools/generator/contracts/central-domain-template-contract.json`

## ADM

- `cpf-admin/frontend/package.json`
- `cpf-admin/frontend/src/app/routes.ts`
- `cpf-admin/frontend/src/app/router.ts`
- `cpf-admin/frontend/src/app/methods/accessMethods.ts`
- `cpf-admin/frontend/src/shared/orval-mutator.ts`
- `cpf-admin/frontend/e2e/primary-runtime.spec.ts`

Route 수: 59

## BZA

- `cpf-biz-admin/frontend/src/app/routes.ts`
- `cpf-biz-admin/frontend/src/app/router.ts`
- BZA Bootstrap Runner·Approval Repository
- BZA V85/R85 Vendor Migration

Route 수: 26

## Batch

- `cpf-batch/contract/**`
- `cpf-batch/execution-runtime/**`
- `CpfBatchExecutionProperties`
- `CpfBatchKafkaRemoteProperties`
- `CpfSpringBatchExecutionControl`
- `CenterCutControlController`
- Process별 `application.properties`
- V82/R82·V84/R84

## Security·Starter

- `CpfServerSessionProperties`
- `CpfServerSessionSecurityAutoConfiguration`
- `CpfKafkaProperties`
- `CpfCaffeineCacheProperties`
- `AgentProperties`

Property Reference 수: 41

## Gateway

- `CpfScgPrimaryRouteConfiguration`
- `CpfScgPrimaryHandler`
- `CpfScgTargetResolver`
- `CpfGatewayLedgerCompletionFilter`

## DB

- `cpf-tools/db/vendor/mariadb/**`
- `cpf-tools/db/vendor/postgresql/**`
- `cpf-tools/db/vendor/oracle/**`
- V82~V85와 R82~R85

## 검증 Script

- `cpf-tools/scripts/verify-cpf-guide-system.ps1`
- `cpf-tools/scripts/verify-cpf-qa32-runtime.ps1`
- `cpf-tools/scripts/verify-cpf-qa32-completion.py`
- `cpf-tools/scripts/verify-cpf-guide-content.py`

## 검증 한계

GitHub 기준 Source는 확인했다. Java 25 전체 Build, npm ci·Playwright, 3 Vendor DB, Kafka, 다중 인스턴스, 실제 화면 캡처와 장애 주입은 이 작성 환경에서 실행하지 않았다. 각 Guide에 `미검증`으로 표시했다.
