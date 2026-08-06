# Exact-SHA Source Confirmation
- Basis SHA: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- Direct source statements below are findings, not Runtime PASS.

## cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java
- Local capture SHA-256: `79f897fbab3869347237b389ccad07d1fe07701b721c5a1425848b4600487b93`
- Reviewed lines: 137-143;388-390
- Findings: QA-R5I-005;QA-R5I-009
- Result: **explicit policy bypasses active-window lookup; detail returns repository record including payloadSnapshot**

## cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java
- Local capture SHA-256: `2e8909babd4a36d53bda3dba580775eb9411865c2578c758e052bc5c796e8443`
- Reviewed lines: 15-37
- Findings: QA-R5I-025
- Result: **public nested approval command constructible outside ADM**

## cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java
- Local capture SHA-256: `a12ece0c8e270d24933e976c39c703a9bef0dc92fa488555c7f1e13b33be8d20`
- Reviewed lines: 49-84
- Findings: QA-R5I-028
- Result: **all approval operations return HTTP 200**

## cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureConfiguration.java
- Local capture SHA-256: `5a4f4e425db6d1514946b5b3ade1c0bf3068561554a75953ec58d7676e13518e`
- Reviewed lines: 44;99
- Findings: QA-R5I-007
- Result: **query MissingBean suppresses default while owner adapter requires correction port**

## cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmIntegrationClosureService.java
- Local capture SHA-256: `aa611c36b40b765758b77396eb120fd628906113fc1318f6689b2ca19109e63d`
- Reviewed lines: 75-96;145-156
- Findings: QA-R5I-008;QA-R5I-011;QA-R5I-012
- Result: **Map.copyOf null exposure and expectedVersion >=1 service boundary**

## cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java
- Local capture SHA-256: `e28baa2a958c3c4c45cc018ef132f26b2440c8d531fc7d13dbb2010db890b8f3`
- Reviewed lines: 52-73;104-119
- Findings: QA-R5I-011;QA-R5I-012
- Result: **Map.copyOf null failure; replay calls validate and may create new quarantine**

## cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts
- Local capture SHA-256: `ec7184c539affb13700bfb9a5aebf2b4317ebdad07b66a04ccb3f60ef8096087`
- Reviewed lines: 46-78
- Findings: QA-R5I-026
- Result: **single sessionStorage slot keyed by one global storage key**

## cpf-tools/verification/final-dev/run-db3-lifecycle.ps1
- Local capture SHA-256: `d4a2f36407454e62180da76d76006b3b9f95cdd782df700c78a4bec144fd9e0b`
- Reviewed lines: 52-81
- Findings: QA-R5I-015
- Result: **argv use, environment inheritance not cleared, unbounded WaitForExit**

## settings.gradle
- Local capture SHA-256: `8eb384a4419c2986aac505650f4e073b0fb005b7eb06ab1ce879a3ccc1b9502b`
- Reviewed lines: 198-204
- Findings: QA-R5I-022
- Result: **local-domains included automatically when folder exists**

## cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs
- Local capture SHA-256: `e50d62413bbcf87e4be1c964b8b77ee79cc9685e77a71bd25e641c1fca67a562`
- Reviewed lines: 24-38
- Findings: QA-R5I-018
- Result: **ensureOperation synthesizes absent path/operation**

## cpf-tools/verification/final-dev/verify-db3-runner-contract.py
- Local capture SHA-256: `b61e590dea1a24a9fa847f7d3388361ca6b0f2b494a7288b9d9ee674164b5525`
- Reviewed lines: required/forbidden regex blocks
- Findings: QA-R5I-027
- Result: **static pattern gate lacks timeout/environment-isolation behavioral checks**

## cpf-admin/frontend/openapi/cpf-openapi.json
- Local capture SHA-256: `22d22c4f6a74c436b2ac9f5eb663341e2b66027e2cf2ae92d4e8da6828cf27b8`
- Reviewed lines: webhook expectedVersion contract
- Findings: QA-R5I-008
- Result: **OpenAPI boundary differs from service boundary**
