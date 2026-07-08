param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

$failures = New-Object System.Collections.Generic.List[string]

function Test-RequiredFile {
    param(
        [string] $Path,
        [string] $Name
    )

    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        $failures.Add("missing feature evidence file [$Name]: $Path")
    }
}

function Test-RequiredText {
    param(
        [string] $Path,
        [string] $Text,
        [string] $Name
    )

    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        $failures.Add("missing feature evidence file [$Name]: $Path")
        return
    }

    $content = [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
    if ($content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        $failures.Add("missing feature evidence text [$Name]: $Path :: $Text")
    }
}

function Test-ForbiddenFile {
    param(
        [string] $Path,
        [string] $Name
    )

    $fullPath = Join-Path $Root $Path
    if (Test-Path -LiteralPath $fullPath) {
        $failures.Add("forbidden feature evidence file remains [$Name]: $Path")
    }
}

function Test-ForbiddenText {
    param(
        [string] $Path,
        [string] $Text,
        [string] $Name
    )

    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        $failures.Add("missing feature evidence file [$Name]: $Path")
        return
    }

    $content = [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
    if ($content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
        $failures.Add("forbidden feature evidence text remains [$Name]: $Path :: $Text")
    }
}

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

function Test-RequiredTextInSpecs {
    param(
        [string] $Text,
        [string] $Name
    )

    $specsRoot = Join-Path $Root "specs"
    if (-not (Test-Path -LiteralPath $specsRoot)) {
        $failures.Add("missing specs directory: specs")
        return
    }

    $found = $false
    Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.html" | ForEach-Object {
        if ($found) {
            return
        }
        $content = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
        if ($content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
            $found = $true
        }
    }
    if (-not $found) {
        $failures.Add("missing specs feature evidence text [$Name]: $Text")
    }
}

function Test-SpecHtmlDocuments {
    $specsRoot = Join-Path $Root "specs"
    if (-not (Test-Path -LiteralPath $specsRoot)) {
        $failures.Add("missing specs directory: specs")
        return
    }

    $htmlFiles = @(Get-ChildItem -LiteralPath $specsRoot -File -Filter "*.html")
    if ($htmlFiles.Count -lt 6) {
        $failures.Add("not enough specs html documents: expected at least 6, actual $($htmlFiles.Count)")
    }
}

Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfGlobalExceptionHandler.java" "PFW_GLOBAL_EXCEPTION_HANDLER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfErrorResponse.java" "PFW_ERROR_RESPONSE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/filter/TransactionContextFilter.java" "PFW_TRANSACTION_CONTEXT_FILTER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" "PFW_TRANSACTION_ID_GENERATOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "PFW_LOGGING_ASPECT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/CpfTransaction.java" "PFW_CPF_TRANSACTION_ANNOTATION"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderNames.java" "PFW_HEADER_NAMES"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderSpecs.java" "PFW_HEADER_SPECS"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfTrustedProxyPolicy.java" "PFW_TRUSTED_PROXY_POLICY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfInboundHeaderValidator.java" "PFW_HEADER_VALIDATOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderExtractor.java" "PFW_HEADER_EXTRACTOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMasker.java" "PFW_HEADER_MASKER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderPropagator.java" "PFW_HEADER_PROPAGATOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMutator.java" "PFW_HEADER_MUTATOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/TransactionContext.java" "PFW_TRANSACTION_CONTEXT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/TransactionHeader.java" "PFW_TRANSACTION_HEADER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/http/CpfRestClientInterceptor.java" "PFW_REST_CLIENT_HEADER_INTERCEPTOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/http/CpfWebClient.java" "PFW_WEB_CLIENT_FACADE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/http/CpfWebClientConfig.java" "PFW_WEB_CLIENT_HEADER_FILTER"
Test-RequiredFile "pfw/src/test/java/cpf/pfw/common/header/CpfHeaderPropagatorTest.java" "PFW_HEADER_PROPAGATOR_TEST"
Test-RequiredFile "pfw/src/test/java/cpf/pfw/common/header/CpfHeaderStandardCoverageTest.java" "PFW_HEADER_STANDARD_COVERAGE_TEST"
Test-RequiredFile "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "PFW_STANDARD_HEADER_E2E_TEST"
Test-RequiredFile "pfw/src/test/java/cpf/pfw/common/http/CpfRestClientInterceptorTest.java" "PFW_REST_CLIENT_HEADER_TEST"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "AUTHORIZATION" "PFW_STANDARD_HEADER_E2E_SENSITIVE_AUTH"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "ORIGINAL_CHANNEL_CODE" "PFW_STANDARD_HEADER_E2E_ORIGINAL_CHANNEL"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "CUSTOMER_NO" "PFW_STANDARD_HEADER_E2E_CUSTOMER"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "MEMBER_NO" "PFW_STANDARD_HEADER_E2E_MEMBER"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java" "CpfTrustedProxyPolicy" "PFW_STANDARD_HEADER_E2E_TRUSTED_PROXY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/policy/LogPolicyResolver.java" "PFW_LOG_POLICY_RESOLVER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/policy/LogPolicyCache.java" "PFW_LOG_POLICY_CACHE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/policy/LogPolicyTargetType.java" "PFW_LOG_POLICY_TARGET_TYPE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/config/CpfLogPolicyAutoConfiguration.java" "PFW_LOG_POLICY_AUTO_CONFIG"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/transaction/CpfTransactionMetaScanner.java" "PFW_TRANSACTION_META_SCANNER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/transaction/CpfTransactionMetaRepository.java" "PFW_TRANSACTION_META_REPOSITORY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLauncher.java" "PFW_BATCH_LAUNCHER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java" "PFW_BATCH_OPERATION_REPOSITORY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java" "PFW_OPENAPI_CONFIG"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallEngine.java" "PFW_SERVICE_CALL_ENGINE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceRegistry.java" "PFW_SERVICE_REGISTRY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfEndpointRegistry.java" "PFW_ENDPOINT_REGISTRY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceInstanceRegistry.java" "PFW_INSTANCE_REGISTRY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfEndpointResolver.java" "PFW_ENDPOINT_RESOLVER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfRoutingPolicyResolver.java" "PFW_ROUTING_POLICY_RESOLVER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfHealthAwareInstanceSelector.java" "PFW_HEALTH_AWARE_INSTANCE_SELECTOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfRemoteFacadeProxySupport.java" "PFW_REMOTE_FACADE_PROXY_SUPPORT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallLogWriter.java" "PFW_SERVICE_CALL_LOG_WRITER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceHealthChecker.java" "PFW_SERVICE_HEALTH_CHECKER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/config/CpfServiceCallAutoConfiguration.java" "PFW_SERVICE_CALL_AUTO_CONFIG"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmServiceRegistryController.java" "ADM_SERVICE_REGISTRY_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmServiceRegistryService.java" "ADM_SERVICE_REGISTRY_SERVICE"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "/adm/api/service-registry" "ADM_SERVICE_REGISTRY_PERMISSION_ROUTE"
Test-RequiredText "specs/sql/10_pfw_schema.sql" "pfw_service_call_history" "SQL_PFW_SERVICE_CALL_HISTORY"
Test-RequiredFile "specs/sql/migration/flyway/V21__pfw_service_call_registry.sql" "FLYWAY_PFW_SERVICE_CALL_REGISTRY"
Test-RequiredFile "scripts/smoke-service-registry-runtime.ps1" "SMOKE_SERVICE_REGISTRY"
Test-RequiredFile "scripts/smoke-service-call-engine-runtime.ps1" "SMOKE_SERVICE_CALL_ENGINE_RUNTIME"
Test-RequiredFile "scripts/smoke-service-registry-health-runtime.ps1" "SMOKE_SERVICE_REGISTRY_HEALTH_RUNTIME"
Test-RequiredFile "scripts/smoke-service-call-engine-circuit-runtime.ps1" "SMOKE_SERVICE_CALL_ENGINE_CIRCUIT_RUNTIME"
Test-RequiredFile "scripts/smoke-service-call-engine-failover-runtime.ps1" "SMOKE_SERVICE_CALL_ENGINE_FAILOVER_RUNTIME"
Test-RequiredFile "scripts/smoke-adm-service-registry-runtime.ps1" "SMOKE_ADM_SERVICE_REGISTRY"
Test-RequiredFile "scripts/smoke-adm-service-registry-ui-static.ps1" "SMOKE_ADM_SERVICE_REGISTRY_UI_STATIC"
Test-RequiredFile "scripts/check-service-call-boundary.ps1" "CHECK_SERVICE_CALL_BOUNDARY"
Test-RequiredText "README.md" "scripts/smoke-service-registry-runtime.ps1" "README_SERVICE_REGISTRY_SMOKE"

Test-RequiredText "settings.gradle" "include 'pfw', 'cmn', 'acc', 'mbr', 'xyz', 'adm', 'bizadm', 'exs', 'bat'" "BAT_SETTINGS_INCLUDE"
Test-RequiredFile "bat/build.gradle" "BAT_BUILD"
Test-RequiredFile "bat/src/main/java/cpf/bat/BatApplication.java" "BAT_APPLICATION"
Test-RequiredFile "bat/src/main/java/cpf/bat/config/BatBatchRepositoryConfig.java" "BAT_BATCH_REPOSITORY_CONFIG"
Test-RequiredFile "bat/src/main/java/cpf/bat/job/BatSmokeJobConfig.java" "BAT_SMOKE_JOB_CONFIG"
Test-RequiredFile "bat/src/main/java/cpf/bat/operation/BatSmokeOperationService.java" "BAT_SMOKE_OPERATION_SERVICE"
Test-RequiredFile "bat/src/main/java/cpf/bat/operation/BatHealthController.java" "BAT_HEALTH_CONTROLLER"
Test-RequiredFile "bat/src/test/java/cpf/bat/operation/BatSmokeOperationServiceTest.java" "BAT_OPERATION_TEST"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutService.java" "PFW_CENTER_CUT_SERVICE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java" "PFW_CENTER_CUT_TARGET_PROVIDER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java" "PFW_CENTER_CUT_HANDLER"
Test-RequiredFile "pfw/src/test/java/cpf/pfw/common/batch/centercut/CpfCenterCutServiceTest.java" "PFW_CENTER_CUT_TEST"
Test-RequiredFile "bat/src/main/java/cpf/bat/centercut/BatCenterCutSmokeTasklet.java" "BAT_CENTER_CUT_TASKLET"
Test-RequiredFile "bat/src/main/java/cpf/bat/centercut/BatCenterCutSampleTargetProvider.java" "BAT_CENTER_CUT_PROVIDER"
Test-RequiredFile "bat/src/main/java/cpf/bat/centercut/BatCenterCutSampleHandler.java" "BAT_CENTER_CUT_HANDLER"
Test-RequiredFile "bat/src/test/java/cpf/bat/centercut/BatCenterCutSampleTargetProviderTest.java" "BAT_CENTER_CUT_TEST"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/centercut/XyzCenterCutTargetRepository.java" "XYZ_CENTER_CUT_DB_PROVIDER"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/centercut/XyzCenterCutHandler.java" "XYZ_CENTER_CUT_HANDLER"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/service/XyzCenterCutEducationService.java" "XYZ_CENTER_CUT_SERVICE"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCenterCutEducationController.java" "XYZ_CENTER_CUT_API"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/centercut/XyzCenterCutAdapterTest.java" "XYZ_CENTER_CUT_DB_TEST"
Test-RequiredFile "scripts/smoke-center-cut-adapter.ps1" "SMOKE_CENTER_CUT_ADAPTER"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCenterCutController.java" "ADM_CENTER_CUT_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmCenterCutOperationService.java" "ADM_CENTER_CUT_SERVICE"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/controller/AdmCenterCutControllerTest.java" "ADM_CENTER_CUT_CONTROLLER_TEST"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmCenterCutOperationServiceTest.java" "ADM_CENTER_CUT_SERVICE_TEST"
Test-RequiredFile "scripts/smoke-adm-center-cut-runtime.ps1" "SMOKE_ADM_CENTER_CUT_RUNTIME"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "/adm/api/center-cut/jobs/CPF_XYZ_CENTER_CUT_SAMPLE_JOB/summary" "SMOKE_ADM_RUNTIME_CENTER_CUT_SUMMARY"
Test-RequiredText "scripts/smoke-adm-center-cut-runtime.ps1" "resultPayload raw field must not be exposed" "SMOKE_ADM_CENTER_CUT_PAYLOAD_MASK"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/center-cut/jobs" "ADM_CENTER_CUT_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/index.html" "Center-Cut Job ID" "ADM_CENTER_CUT_UI_SECTION"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "xyz_center_cut_sample_target" "SQL_XYZ_CENTER_CUT_TARGET"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "xyz_center_cut_sample_result" "SQL_XYZ_CENTER_CUT_RESULT"
Test-RequiredText "specs/sql/70_test_data.sql" "CPF_XYZ_CENTER_CUT_SAMPLE_JOB" "SQL_XYZ_CENTER_CUT_SEED"
Test-RequiredText "specs/sql/99_smoke_check.sql" "xyz_center_cut_sample_target" "SQL_XYZ_CENTER_CUT_SMOKE"
Test-RequiredFile "specs/sql/migration/flyway/V16__batch_center_cut_standard.sql" "FLYWAY_CENTER_CUT_STANDARD"
Test-RequiredFile "specs/sql/migration/flyway/V17__batch_center_cut_ownership_repair.sql" "FLYWAY_CENTER_CUT_OWNERSHIP_REPAIR"
Test-RequiredFile "specs/sql/migration/flyway/V18__xyz_center_cut_sample.sql" "FLYWAY_XYZ_CENTER_CUT_SAMPLE"
Test-RequiredText "specs/sql/35_bat_schema.sql" "bat_center_cut_job" "SQL_BAT_CENTER_CUT_TABLE"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "CPF_BAT_CENTER_CUT_JOB" "BAT_SMOKE_CENTER_CUT_JOB"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "centerCutRequested=" "BAT_SMOKE_CENTER_CUT_EVIDENCE"
Test-RequiredFile "scripts/smoke-bat-runtime.ps1" "SMOKE_BAT_RUNTIME"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "AllowLegacyFallback" "BAT_SMOKE_STRICT_V10_OPTION"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "V10 extendedExecution.processed_count is missing" "BAT_SMOKE_STRICT_V10_MESSAGE"
Test-RequiredText "README.md" "scripts/smoke-bat-runtime.ps1" "BAT_DOC_README_SMOKE"
Test-RequiredText "README.md" "BatApplication" "BAT_DOC_README_MODULE"
Test-RequiredText "README.md" "CPF_FINAL_TARGET_REQUIREMENTS.md" "CPF_FINAL_TARGET_README"
Test-RequiredText "CPF_FINAL_TARGET_REQUIREMENTS.md" "X-Cpf-Ext-*" "CPF_FINAL_TARGET_EXTENSION_HEADER"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderNames.java" "X-Cpf-Ext-1" "PFW_EXTENSION_RESERVED_HEADER"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/header/CpfExtensionHeaderPolicy.java" "X-Cpf-Ext-" "PFW_EXTENSION_HEADER_POLICY"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfExtensionHeaderPolicyTest.java" "X-Cpf-Ext-Campaign-Id" "PFW_EXTENSION_HEADER_TEST"
Test-RequiredFile "scripts/smoke-mariadb-full-install.ps1" "SMOKE_MARIADB_FULL_INSTALL"
Test-RequiredText "scripts/smoke-mariadb-full-install.ps1" "CPF_DB_ROOT_PASSWORD" "SMOKE_MARIADB_PASSWORD_ENV"
Test-RequiredText "scripts/smoke-mariadb-full-install.ps1" "build/sql-smoke" "SMOKE_MARIADB_RESULT_DIR"
Test-RequiredText "scripts/smoke-mariadb-full-install.ps1" "CPF_BAT_CENTER_CUT_JOB" "SMOKE_MARIADB_CENTER_CUT_SEED"
Test-RequiredText "scripts/smoke-mariadb-full-install.ps1" "bat_center_cut_job" "SMOKE_MARIADB_CENTER_CUT_TABLE"
Test-RequiredFile "scripts/smoke-standard-header-e2e.ps1" "SMOKE_STANDARD_HEADER_E2E"
Test-RequiredText "scripts/smoke-standard-header-e2e.ps1" "X-Cpf-Ext-Campaign-Id" "SMOKE_STANDARD_HEADER_ALLOWED_EXTENSION"
Test-RequiredText "scripts/smoke-standard-header-e2e.ps1" "X-Cpf-Ext-Token" "SMOKE_STANDARD_HEADER_BLOCKED_EXTENSION"
Test-RequiredText "scripts/smoke-standard-header-e2e.ps1" "Authorization" "SMOKE_STANDARD_HEADER_SENSITIVE"
Test-RequiredText "scripts/smoke-standard-header-e2e.ps1" "standard-header-e2e-result.json" "SMOKE_STANDARD_HEADER_RESULT_JSON"
Test-RequiredText "README.md" "scripts/smoke-mariadb-full-install.ps1" "README_MARIADB_FULL_INSTALL_SMOKE"
Test-RequiredText "README.md" "scripts/smoke-standard-header-e2e.ps1" "README_STANDARD_HEADER_E2E_SMOKE"
Test-RequiredText "README.md" "scripts/smoke-composite-transaction-runtime.ps1" "README_COMPOSITE_TRANSACTION_SMOKE"
Test-RequiredText "README.md" "scripts/smoke-adm-transaction-group-runtime.ps1" "README_ADM_TRANSACTION_GROUP_SMOKE"

Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceService.java" "CMN_SEQUENCE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java" "CMN_NOTIFICATION_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogService.java" "CMN_BUSINESS_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java" "CMN_FILE_EXCHANGE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/tlm/service/CmnTelegramService.java" "CMN_TELEGRAM_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthLayoutSpec.java" "CMN_FIXED_LENGTH_LAYOUT_SPEC"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldSpec.java" "CMN_FIXED_LENGTH_FIELD_SPEC"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMessageParser.java" "CMN_FIXED_LENGTH_PARSER"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMessageFormatter.java" "CMN_FIXED_LENGTH_FORMATTER"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMaskingRule.java" "CMN_FIXED_LENGTH_MASKING"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthLayoutRegistry.java" "CMN_FIXED_LENGTH_LAYOUT_REGISTRY"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthGroupSpec.java" "CMN_FIXED_LENGTH_GROUP_SPEC"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMessageError.java" "CMN_FIXED_LENGTH_ERROR"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMessageException.java" "CMN_FIXED_LENGTH_EXCEPTION"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthTypeConverter.java" "CMN_FIXED_LENGTH_TYPE_CONVERTER"
Test-RequiredFile "cmn/src/test/java/cpf/cmn/message/fixedlength/FixedLengthMessageParserFormatterTest.java" "CMN_FIXED_LENGTH_ROUND_TRIP_TEST"
Test-RequiredText "cmn/src/test/java/cpf/cmn/message/fixedlength/FixedLengthMessageParserFormatterTest.java" "registryAndTypeValidationExposeFieldErrors" "CMN_FIXED_LENGTH_TYPE_VALIDATION_TEST"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java" "CMN_MESSAGE_BRIDGE_SERVICE"
Test-RequiredFile "cmn/src/test/java/cpf/cmn/biz/sequence/CmnSequenceServiceConcurrencyTest.java" "CMN_SEQUENCE_CONCURRENCY_TEST"

Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "ADM_PERMISSION_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java" "ADM_API_PERMISSION_DTO"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmPermissionServiceTest.java" "ADM_PERMISSION_SERVICE_TEST"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "/api-permissions" "ADM_API_PERMISSION_ENDPOINT"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "adm_api_permission" "ADM_API_PERMISSION_FILTER"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "/adm/api/permissions/api-permissions" "ADM_PERMISSION_RUNTIME_SMOKE_API"
Test-RequiredFile "scripts/smoke-adm-permission-runtime.ps1" "ADM_PERMISSION_WRITE_RUNTIME_SMOKE"
Test-RequiredFile "scripts/apply-v15-adm-api-permission-management.ps1" "ADM_V15_API_PERMISSION_JDBC_APPLY"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "permissionWriteApi" "ADM_PERMISSION_WRITE_RUNTIME_RESULT"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "adm-permission-runtime-result.json" "ADM_PERMISSION_WRITE_RUNTIME_JSON"
Test-RequiredText "scripts/smoke-adm-permission-runtime.ps1" "v15-adm-api-permission-result.json" "ADM_V15_API_PERMISSION_RESULT"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "BuildBeforeRun" "ADM_RUNTIME_BOOTJAR_BUILD_BEFORE_RUN"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "viewerCenterCutReadAllowed" "ADM_PERMISSION_VIEWER_CENTER_CUT_READ"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMemberController.java" "ADM_MEMBER_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmLogController.java" "ADM_LOG_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmObservabilityController.java" "ADM_OBSERVABILITY_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmObservabilityService.java" "ADM_OBSERVABILITY_SERVICE"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyAuditController.java" "ADM_LOG_POLICY_AUDIT_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmTransactionMetaController.java" "ADM_TRANSACTION_META_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyController.java" "ADM_LOG_POLICY_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java" "ADM_BATCH_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCacheController.java" "ADM_CACHE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMessageController.java" "ADM_MESSAGE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCodeController.java" "ADM_CODE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmConfigController.java" "ADM_CONFIG_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmDownloadService.java" "ADM_DOWNLOAD_SERVICE"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmDownloadServiceTest.java" "ADM_DOWNLOAD_TEST"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmObservabilityServiceTest.java" "ADM_OBSERVABILITY_TEST"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/batch" "ADM_BATCH_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/logs" "ADM_LOG_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/transaction-groups" "ADM_TRANSACTION_GROUP_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "loadTransactionGroupDetail" "ADM_TRANSACTION_GROUP_UI_DETAIL"
Test-RequiredText "adm/src/main/resources/static/adm/index.html" "activeMenu === 'transactionGroups'" "ADM_TRANSACTION_GROUP_UI_PANEL"
Test-RequiredText "adm/src/main/resources/static/adm/index.html" "External Logs" "ADM_TRANSACTION_GROUP_UI_EXTERNAL_LOGS_TAB"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/transactions" "ADM_TRANSACTION_META_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/log-policies" "ADM_LOG_POLICY_UI_ROUTE"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmTransactionGroupController.java" "ADM_TRANSACTION_GROUP_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmTransactionGroupService.java" "ADM_TRANSACTION_GROUP_SERVICE"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmTransactionGroupController.java" "/adm/api/transaction-groups" "ADM_TRANSACTION_GROUP_ENDPOINT"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/service/AdmTransactionGroupService.java" "timelineFromSegments" "ADM_TRANSACTION_GROUP_TIMELINE"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/service/AdmTransactionGroupService.java" "headerSnapshots" "ADM_TRANSACTION_GROUP_HEADERS"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "/adm/api/transaction-groups" "ADM_TRANSACTION_GROUP_AUTH"

Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/auth/controller/BizAdmAuthController.java" "BIZADM_AUTH_API"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/auth/service/BizAdmAuthService.java" "BIZADM_AUTH_SERVICE"
Test-RequiredFile "bizadm/src/test/java/cpf/bizadm/auth/service/BizAdmAuthServiceTest.java" "BIZADM_AUTH_TEST"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/controller/BizAdmOperationController.java" "BIZADM_OPERATION_API"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/service/BizAdmOperationService.java" "BIZADM_OPERATION_SERVICE"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/repository/BizAdmOperationRepository.java" "BIZADM_OPERATION_REPOSITORY"
Test-RequiredFile "bizadm/src/test/java/cpf/bizadm/operation/service/BizAdmOperationServiceTest.java" "BIZADM_OPERATION_TEST"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/controller/MbrController.java" "MBR_MEMBER_API"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/controller/MbrAuthController.java" "MBR_AUTH_API"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrService.java" "MBR_MEMBER_SERVICE"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrAuthService.java" "MBR_AUTH_SERVICE"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/mapper/MemberMapper.java" "MBR_MEMBER_MAPPER"
Test-RequiredFile "mbr/src/test/java/cpf/mbr/bse/controller/MbrControllerValidationTest.java" "MBR_CONTROLLER_TEST"
Test-RequiredFile "mbr/src/test/java/cpf/mbr/bse/service/MbrAuthServiceTest.java" "MBR_AUTH_TEST"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/controller/ExsOperationController.java" "EXS_OPERATION_API"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/controller/ExsAdminOperationController.java" "EXS_ADMIN_API"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/service/ExsOperationService.java" "EXS_OPERATION_SERVICE"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/repository/ExsOperationRepository.java" "EXS_OPERATION_REPOSITORY"
Test-RequiredFile "exs/src/main/java/cpf/exs/flow/service/ExsFlowService.java" "EXS_FLOW_SERVICE"
Test-RequiredFile "exs/src/test/java/cpf/exs/operation/service/ExsOperationServiceTest.java" "EXS_OPERATION_TEST"

Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java" "EDU_CRUD"
Test-RequiredText "xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java" "/items/{educationItemId}/status" "EDU_CRUD_STATUS_API"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/dto/XyzCrudEducationStatusRequest.java" "EDU_CRUD_STATUS_DTO"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/service/XyzCrudEducationService.java" "EDU_CRUD_SERVICE"
Test-RequiredText "xyz/src/main/java/cpf/xyz/edu/service/XyzCrudEducationService.java" "XyzQueryEducationRepository" "EDU_CRUD_REPOSITORY_LINK"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzQueryEducationController.java" "EDU_QUERY"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/service/XyzQueryEducationService.java" "EDU_QUERY_SERVICE"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/repository/XyzQueryEducationRepository.java" "EDU_QUERY_REPOSITORY"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/mapper/XyzQueryEducationMapper.java" "EDU_QUERY_MAPPER"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationCriteria.java" "EDU_QUERY_CRITERIA_DTO"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationItem.java" "EDU_QUERY_ITEM_DTO"
Test-RequiredFile "xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml" "EDU_QUERY_MAPPER_XML"
Test-RequiredText "xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml" "insertCrudItem" "EDU_CRUD_MAPPER_INSERT"
Test-RequiredText "xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml" "logicalDeleteCrudItem" "EDU_CRUD_MAPPER_LOGICAL_DELETE"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationRepositoryTest.java" "EDU_QUERY_REPOSITORY_TEST"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java" "EDU_QUERY_MAPPER_SLICE_TEST"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/service/XyzCrudEducationServiceTest.java" "EDU_CRUD_SERVICE_TEST"
Test-RequiredText "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java" "insertCrudItem" "EDU_CRUD_MAPPER_SLICE_TEST"
Test-RequiredFile "xyz/src/test/resources/sql/xyz_edu_query_fixture.sql" "EDU_QUERY_MAPPER_FIXTURE"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/service/XyzQueryEducationServiceTest.java" "EDU_QUERY_SERVICE_TEST"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java" "EDU_CMN_BUSINESS"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzBatchEducationController.java" "EDU_BATCH"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java" "EDU_SERVICE_CALL"
Test-RequiredText "xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java" "CpfWebClient" "EDU_OUTBOUND_CPF_WEBCLIENT"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzTelegramEducationController.java" "EDU_TELEGRAM"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzFileExchangeEducationController.java" "EDU_FILE_EXCHANGE"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCompositeTransactionEducationController.java" "EDU_COMPOSITE_TRANSACTION"
Test-RequiredText "xyz/src/main/java/cpf/xyz/edu/controller/XyzCompositeTransactionEducationController.java" "/adm/api/transaction-groups" "EDU_COMPOSITE_ADM_GROUP_GUIDE"
Test-RequiredFile "acc/src/main/java/cpf/acc/bse/controller/AccCompositeEducationController.java" "ACC_COMPOSITE_API"
Test-RequiredFile "acc/src/main/java/cpf/acc/bse/service/AccCompositeTransactionService.java" "ACC_COMPOSITE_SERVICE"
Test-RequiredFile "acc/src/main/java/cpf/acc/bse/service/ExsExchangeClientService.java" "ACC_EXS_CLIENT"
Test-RequiredText "acc/src/main/java/cpf/acc/bse/service/AccCompositeTransactionService.java" "TransactionSegmentService" "ACC_COMPOSITE_SEGMENT_SERVICE"
Test-RequiredText "acc/src/main/java/cpf/acc/bse/service/MbrMemberClientService.java" "callMemberWithExternal" "ACC_MBR_COMPOSITE_CLIENT"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/controller/MbrCompositeEducationController.java" "MBR_COMPOSITE_API"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrCompositeTransactionService.java" "MBR_COMPOSITE_SERVICE"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrExsClientService.java" "MBR_EXS_CLIENT"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/controller/ExsCompositeEducationController.java" "EXS_COMPOSITE_API"

Test-RequiredFile "specs/sql/00_all_install.sql" "SQL_ALL_INSTALL"
Test-RequiredFile "specs/sql/00_all_install_and_smoke.sql" "SQL_ALL_INSTALL_AND_SMOKE"
Test-RequiredFile "specs/sql/migration/flyway/V1__cpf_baseline_install.sql" "FLYWAY_BASELINE"
Test-RequiredFile "specs/sql/migration/flyway/V11__transaction_meta_log_policy.sql" "FLYWAY_TRANSACTION_META_LOG_POLICY"
Test-RequiredText "specs/sql/10_pfw_schema.sql" "pfw_transaction_meta" "SQL_TRANSACTION_META_TABLE"
Test-RequiredText "specs/sql/10_pfw_schema.sql" "pfw_log_policy" "SQL_LOG_POLICY_TABLE"
Test-RequiredText "specs/sql/50_framework_seed_data.sql" "ONLINE_TRANSACTION" "SQL_ONLINE_TRANSACTION_POLICY"
Test-RequiredFile "specs/sql/migration/flyway/V12__log_policy_runtime_standard.sql" "FLYWAY_LOG_POLICY_RUNTIME_STANDARD"
Test-RequiredFile "specs/sql/migration/flyway/V13__adm_runtime_policy_permission_seed.sql" "FLYWAY_ADM_RUNTIME_PERMISSION_SEED"
Test-RequiredText "specs/sql/migration/flyway/V13__adm_runtime_policy_permission_seed.sql" "adm_role_menu" "FLYWAY_ADM_RUNTIME_MENU_GRANT"
Test-RequiredText "specs/sql/60_adm_seed_data.sql" "LOG_POLICY_CACHE_REFRESH" "SQL_LOG_POLICY_CACHE_REFRESH_BUTTON"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "LOG_POLICY_CACHE_REFRESH" "ADM_AUTH_LOG_POLICY_CACHE_REFRESH"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "bizadm_customer" "SQL_BIZADM_TABLE"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "exs_transaction_log" "SQL_EXS_TABLE"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "mbr_member" "SQL_MBR_TABLE"
Test-RequiredText "specs/sql/20_cmn_schema.sql" "cmn_edu_query_item" "SQL_CMN_EDU_QUERY_TABLE"
Test-RequiredFile "specs/sql/migration/flyway/V14__edu_query_sample.sql" "FLYWAY_EDU_QUERY_SAMPLE"
Test-RequiredText "specs/sql/10_pfw_schema.sql" "pfw_transaction_segment" "SQL_TRANSACTION_SEGMENT_TABLE"
Test-RequiredText "specs/sql/99_smoke_check.sql" "pfw_transaction_segment" "SQL_TRANSACTION_SEGMENT_SMOKE"
Test-RequiredFile "specs/sql/migration/flyway/V19__transaction_segment_trace.sql" "FLYWAY_TRANSACTION_SEGMENT_TRACE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentService.java" "PFW_TRANSACTION_SEGMENT_SERVICE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentContext.java" "PFW_TRANSACTION_SEGMENT_CONTEXT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentScope.java" "PFW_TRANSACTION_SEGMENT_SCOPE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentRecord.java" "PFW_TRANSACTION_SEGMENT_RECORD"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/mapper/common/logging/TransactionSegmentMapper.java" "PFW_TRANSACTION_SEGMENT_MAPPER"
Test-RequiredFile "pfw/src/main/resources/mybatis/mapper/pfw/logging/TransactionSegmentMapper.xml" "PFW_TRANSACTION_SEGMENT_MAPPER_XML"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderNames.java" "X-Transaction-Segment-Id" "PFW_TRANSACTION_SEGMENT_HEADER"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/header/CpfHeaderPropagator.java" "appendSegmentHeaders" "PFW_TRANSACTION_SEGMENT_HEADER_PROPAGATION"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/header/CpfHeaderPropagatorTest.java" "outboundHeadersContainCurrentTransactionSegment" "PFW_TRANSACTION_SEGMENT_HEADER_TEST"
Test-RequiredFile "README.md" "README"
Test-RequiredFile "specs/index.html" "DOC_INDEX"
Test-RequiredTextInSpecs "cpf-local-dev-guide" "DOC_LOCAL_DEV_GUIDE"
Test-RequiredTextInSpecs "cpf-standard-header-guide" "DOC_STANDARD_HEADER_GUIDE"
Test-RequiredTextInSpecs "cpf-db-standard-guide" "DOC_DB_STANDARD_GUIDE"
Test-RequiredTextInSpecs "cpf-architecture-guide" "DOC_ARCHITECTURE_GUIDE"
Test-RequiredTextInSpecs "cpf-transaction-guide" "DOC_TRANSACTION_GUIDE"
Test-RequiredTextInSpecs "inboundHeaders" "DOC_HEADER_INBOUND"
Test-RequiredTextInSpecs "CpfRestClientInterceptor" "DOC_HEADER_REST_INTERCEPTOR"
Test-RequiredTextInSpecs "cpf-pfw-cmn-boundary" "DOC_PFW_CMN_BOUNDARY"
Test-RequiredTextInSpecs "/adm/api/transactions" "DOC_ADM_REQUIRED_MENU_MATRIX"
Test-RequiredTextInSpecs "/adm/api/cache" "DOC_ADM_CACHE_REQUIRED_MENU"
Test-RequiredTextInSpecs "AdmTransactionMetaController" "DOC_ADMIN_TRANSACTION_META"
Test-RequiredTextInSpecs "AdmLogPolicyController" "DOC_ADMIN_LOG_POLICY"
Test-RequiredTextInSpecs "pfw_transaction_segment" "DOC_TRANSACTION_SEGMENT_TABLE"
Test-RequiredTextInSpecs "/adm/api/transaction-groups" "DOC_ADM_TRANSACTION_GROUP_API"
Test-RequiredTextInSpecs "scripts/smoke-composite-transaction-runtime.ps1" "DOC_COMPOSITE_TRANSACTION_SMOKE"
Test-RequiredTextInSpecs "FixedLengthLayoutRegistry" "DOC_FIXED_LENGTH_REGISTRY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "resolveOnlineLogPolicy" "LOGGING_ASPECT_POLICY_RESOLVE"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "dbLogEnabled" "LOGGING_ASPECT_DB_LOG_POLICY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "requestBodySaveYn" "LOGGING_ASPECT_REQUEST_POLICY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/service/common/logging/TransactionLogService.java" "dbLogEnabled" "TRANSACTION_LOG_SERVICE_DB_POLICY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/service/common/logging/TransactionLogService.java" "requestBodySave" "TRANSACTION_LOG_SERVICE_BODY_POLICY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeListener.java" "resolveBatchJob" "BATCH_JOB_LOG_POLICY_HOOK"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeListener.java" "resolveBatchStep" "BATCH_STEP_LOG_POLICY_HOOK"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/service/AdmLogPolicyService.java" "evictPolicyCache" "ADM_LOG_POLICY_CACHE_EVICT"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyController.java" "/cache/refresh" "ADM_LOG_POLICY_CACHE_REFRESH_API"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "/adm/api/transactions/scan" "SMOKE_TRANSACTION_META_SCAN"
Test-RequiredFile "scripts/smoke-transaction-meta-runtime.ps1" "SMOKE_TRANSACTION_META_RUNTIME"
Test-RequiredFile "scripts/apply-pfw-runtime-migrations.ps1" "APPLY_PFW_RUNTIME_MIGRATIONS"
Test-RequiredFile "scripts/apply-v13-adm-permission-seed.ps1" "APPLY_V13_ADM_PERMISSION_SEED"
Test-RequiredFile "scripts/smoke-log-policy-runtime.ps1" "SMOKE_LOG_POLICY_RUNTIME"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "dbLogDisabled" "SMOKE_LOG_POLICY_DB_LOG_DISABLED"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "dbLogEnabled" "SMOKE_LOG_POLICY_DB_LOG_ENABLED"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "requestBodyPolicy" "SMOKE_LOG_POLICY_REQUEST_BODY_DISABLED"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "responseBodyPolicy" "SMOKE_LOG_POLICY_RESPONSE_BODY_DISABLED"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "errorStackPolicy" "SMOKE_LOG_POLICY_ERROR_STACK_DISABLED"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "overrideFallback" "SMOKE_LOG_POLICY_OVERRIDE_FALLBACK"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "admObservability" "SMOKE_ADM_OBSERVABILITY"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "businessTransactionSearch" "SMOKE_ADM_OBSERVABILITY_BUSINESS_SEARCH"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "policyAuditQuery" "SMOKE_ADM_OBSERVABILITY_POLICY_AUDIT"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "/adm/api/observability" "SMOKE_ADM_OBSERVABILITY_WRAPPER_API"
Test-RequiredText "scripts/smoke-log-policy-runtime.ps1" "/adm/api/log-policy-audits" "SMOKE_ADM_LOG_POLICY_AUDIT_API"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmLogController.java" "transactionGlobalId" "ADM_LOG_TRANSACTION_GLOBAL_ALIAS"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "/adm/api/observability" "ADM_AUTH_OBSERVABILITY_API"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "/adm/api/log-policy-audits" "ADM_AUTH_LOG_POLICY_AUDIT_API"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/logging/policy/LogPolicyCacheTest.java" "expiredOverrideFallsBackToDbPolicy" "LOG_POLICY_EXPIRED_OVERRIDE_TEST"
Test-RequiredText "pfw/src/test/java/cpf/pfw/common/logging/policy/LogPolicyCacheTest.java" "cpfDefaultIsUsedWhenDbPolicyAndApplicationDefaultAreMissing" "LOG_POLICY_CPF_DEFAULT_TEST"
Test-RequiredText "specs/index.html" "batch-development-guide" "DOC_BATCH_GUIDE_LINK"
Test-RequiredText "specs/index.html" "operation-runbook" "DOC_OPERATION_RUNBOOK_LINK"
Test-RequiredText "README.md" "batch-development-guide" "README_BATCH_GUIDE_LINK"
Test-RequiredText "README.md" "operation-runbook" "README_OPERATION_MANUAL_LINK"
Test-RequiredText "README.md" "scripts/smoke-log-policy-runtime.ps1" "README_LOG_POLICY_RUNTIME_SMOKE"
Test-RequiredTextInSpecs "scripts/smoke-log-policy-runtime.ps1" "DOC_LOG_POLICY_RUNTIME_SMOKE"
Test-SpecHtmlDocuments
Test-RequiredFile "scripts/check-sql-standard.ps1" "CHECK_SQL_STANDARD"
Test-RequiredFile "scripts/smoke-openapi.ps1" "SMOKE_OPENAPI"
Test-RequiredFile "scripts/smoke-adm-ui.ps1" "SMOKE_ADM_UI"
Test-RequiredFile "scripts/smoke-composite-transaction-runtime.ps1" "SMOKE_COMPOSITE_TRANSACTION_RUNTIME"
Test-RequiredFile "scripts/smoke-adm-transaction-group-runtime.ps1" "SMOKE_ADM_TRANSACTION_GROUP_RUNTIME"
Test-RequiredFile "scripts/smoke-composite-transaction-failure-runtime.ps1" "SMOKE_COMPOSITE_TRANSACTION_FAILURE_RUNTIME"
Test-RequiredFile "scripts/smoke-adm-transaction-group-failure-runtime.ps1" "SMOKE_ADM_TRANSACTION_GROUP_FAILURE_RUNTIME"
Test-RequiredFile "scripts/export-sanitized-evidence.ps1" "SMOKE_SANITIZED_EVIDENCE_EXPORT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/file/CpfFileLogWriter.java" "PFW_FILE_LOG_WRITER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/file/TransactionFileLogListener.java" "PFW_TRANSACTION_FILE_LOG_LISTENER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchFileLogWriter.java" "PFW_BATCH_FILE_LOG_WRITER"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/dto/AdmTraceBoostRequest.java" "ADM_TRACE_BOOST_REQUEST"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyController.java" "/trace-boost" "ADM_TRACE_BOOST_API"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/service/AdmLogPolicyService.java" "TRACE_BOOST_SAFE_MASKING" "ADM_TRACE_BOOST_MASKING_POLICY"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/http/CpfWebClientConfig.java" "integrationFileLogFilter" "PFW_INTEGRATION_FILE_LOG_FILTER"
Test-RequiredText "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "publishTransactionLog" "PFW_TRANSACTION_EVENT_PUBLISH"
Test-RequiredText "pfw/src/main/resources/application-pfw.yml" "cpf-{moduleCode}-{logType}.log" "PFW_FILE_LOG_PATTERN"
Test-RequiredFile "scripts/smoke-file-log-standard-runtime.ps1" "SMOKE_FILE_LOG_STANDARD_RUNTIME"
Test-RequiredFile "scripts/smoke-trace-boost-runtime.ps1" "SMOKE_TRACE_BOOST_RUNTIME"
Test-RequiredFile "scripts/smoke-bat-trace-boost-runtime.ps1" "SMOKE_BAT_TRACE_BOOST_RUNTIME"
Test-RequiredFile "scripts/create-domain.ps1" "CREATE_DOMAIN_SCRIPT"
Test-RequiredFile "scripts/smoke-create-domain.ps1" "SMOKE_CREATE_DOMAIN"
Test-RequiredFile "scripts/sync-runtime-smoke-summary.ps1" "SYNC_RUNTIME_SMOKE_SUMMARY"
Test-RequiredFile "scripts/runtime-common.ps1" "RUNTIME_COMMON"
Test-RequiredFile "scripts/runtime-start-services.ps1" "RUNTIME_START_SERVICES"
Test-RequiredFile "scripts/runtime-stop-services.ps1" "RUNTIME_STOP_SERVICES"
Test-RequiredFile "scripts/runtime-status.ps1" "RUNTIME_STATUS"
Test-RequiredFile "scripts/runtime-diagnostics.ps1" "RUNTIME_DIAGNOSTICS"
Test-RequiredFile "scripts/check-packaged-runtime-resources.ps1" "CHECK_PACKAGED_RUNTIME_RESOURCES"
Test-RequiredFile "scripts/smoke-runtime-closure.ps1" "SMOKE_RUNTIME_CLOSURE"
Test-RequiredFile "scripts/smoke-bat-log-bean-runtime.ps1" "SMOKE_BAT_LOG_BEAN_RUNTIME"
Test-RequiredFile "scripts/smoke-adm-operation-console-runtime.ps1" "SMOKE_ADM_OPERATION_CONSOLE_RUNTIME"
Test-RequiredFile "scripts/smoke-adm-log-policy-ui-static.ps1" "SMOKE_ADM_LOG_POLICY_UI_STATIC"
Test-RequiredFile "scripts/smoke-exs-timeout-retry-runtime.ps1" "SMOKE_EXS_TIMEOUT_RETRY_RUNTIME"
Test-RequiredFile "scripts/smoke-cmn-fixed-length-advanced.ps1" "SMOKE_CMN_FIXED_LENGTH_ADVANCED"
Test-RequiredText "scripts/runtime-start-services.ps1" "ACC_SERVER_PORT" "RUNTIME_START_ACC_PORT_ENV"
Test-RequiredText "scripts/runtime-start-services.ps1" "bootJarBuildStatus" "RUNTIME_START_BOOTJAR_BUILD_STATUS"
Test-RequiredText "scripts/runtime-start-services.ps1" "finalRuntimeUsable" "RUNTIME_START_FINAL_USABLE_FIELD"
Test-RequiredText "scripts/runtime-start-services.ps1" "jarFallbackUsed" "RUNTIME_START_JAR_FALLBACK_FIELD"
Test-RequiredText "scripts/runtime-start-services.ps1" "healthCheckPassed" "RUNTIME_START_HEALTH_FIELD"
Test-RequiredText "scripts/runtime-status.ps1" "runtime-status-result.json" "RUNTIME_STATUS_RESULT_JSON"
Test-RequiredText "scripts/runtime-status.ps1" "finalRuntimeUsable" "RUNTIME_STATUS_FINAL_USABLE_FIELD"
Test-RequiredText "scripts/runtime-diagnostics.ps1" "runtime-diagnostics-result.json" "RUNTIME_DIAGNOSTICS_RESULT_JSON"
Test-RequiredText "scripts/runtime-diagnostics.ps1" "finalRuntimeUsable" "RUNTIME_DIAGNOSTICS_FINAL_USABLE_FIELD"
Test-RequiredText "scripts/check-packaged-runtime-resources.ps1" "D:/logs" "PACKAGED_CHECK_FORBIDDEN_D_LOGS"
Test-RequiredText "scripts/check-packaged-runtime-resources.ps1" "CPF_LOGGING_FILE_BASE_PATH:logs" "PACKAGED_CHECK_LOG_BASE_MARKER"
Test-RequiredText "scripts/smoke-runtime-closure.ps1" "runtime-start-services-result.json" "SMOKE_RUNTIME_CLOSURE_START_RESULT"
Test-RequiredText "scripts/smoke-bat-log-bean-runtime.ps1" "/bat/api/diagnostics/logging" "SMOKE_BAT_LOG_BEAN_DIAGNOSTIC_API"
Test-RequiredText "bat/src/main/java/cpf/bat/operation/BatHealthController.java" "/bat/api/diagnostics/logging" "BAT_LOGGING_DIAGNOSTIC_API"
Test-RequiredText "scripts/smoke-adm-log-policy-ui-static.ps1" "/adm/api/log-policies/trace-boost" "SMOKE_ADM_LOG_POLICY_UI_TRACE_BOOST"
Test-RequiredText "scripts/smoke-adm-operation-console-runtime.ps1" "/adm/api/batch/relations" "SMOKE_ADM_OPERATION_BATCH_RELATIONS"
Test-RequiredText "scripts/smoke-exs-timeout-retry-runtime.ps1" "EXS_TIMEOUT" "SMOKE_EXS_TIMEOUT_RETRY_ASSERT"
Test-RequiredText "scripts/smoke-cmn-fixed-length-advanced.ps1" "FixedLengthMessageParserFormatterTest" "SMOKE_CMN_FIXED_LENGTH_TEST"
Test-RequiredText "scripts/smoke-file-log-standard-runtime.ps1" "New-CpfRuntimeDiagnostic" "SMOKE_FILE_LOG_RUNTIME_DIAGNOSTIC"
Test-RequiredText "scripts/smoke-trace-boost-runtime.ps1" "New-CpfRuntimeDiagnostic" "SMOKE_TRACE_BOOST_RUNTIME_DIAGNOSTIC"
Test-RequiredText "scripts/smoke-bat-trace-boost-runtime.ps1" "New-CpfRuntimeDiagnostic" "SMOKE_BAT_TRACE_BOOST_RUNTIME_DIAGNOSTIC"
Test-RequiredText "scripts/create-domain.ps1" "conflicts" "CREATE_DOMAIN_CONFLICT_CHECK"
Test-RequiredText "scripts/create-domain.ps1" "GeneratePatch" "CREATE_DOMAIN_GENERATE_PATCH"
Test-RequiredText "scripts/create-domain.ps1" "patch-candidates/apply-order.md" "CREATE_DOMAIN_APPLY_ORDER"
Test-RequiredText "scripts/create-domain.ps1" "50_framework_seed" "CREATE_DOMAIN_PFW_SEED_CANDIDATE"
Test-RequiredText "scripts/create-domain.ps1" "60_adm_seed" "CREATE_DOMAIN_ADM_SEED_CANDIDATE"
Test-RequiredText "scripts/create-domain.ps1" "cpf-{moduleCode}-{logType}.log" "CREATE_DOMAIN_FILE_LOG_GUIDE"
Test-RequiredText "README.md" "scripts/smoke-file-log-standard-runtime.ps1" "README_FILE_LOG_STANDARD_SMOKE"
Test-RequiredText "README.md" "scripts/smoke-trace-boost-runtime.ps1" "README_TRACE_BOOST_SMOKE"
Test-RequiredText "README.md" "scripts/smoke-create-domain.ps1" "README_CREATE_DOMAIN_SMOKE"
Test-RequiredTextInSpecs "file-log-standard" "DOC_FILE_LOG_STANDARD_MATRIX"
Test-RequiredTextInSpecs "trace-boost-runtime" "DOC_TRACE_BOOST_MATRIX"
Test-RequiredTextInSpecs "create-domain-smoke" "DOC_CREATE_DOMAIN_MATRIX"
Test-RequiredFile "specs/evidence/20260706_02/file-log-standard-result.sanitized.json" "SANITIZED_0602_FILE_LOG_STANDARD"
Test-RequiredFile "specs/evidence/20260706_02/file-log-grep-summary.sanitized.log" "SANITIZED_0602_FILE_LOG_GREP"
Test-RequiredFile "specs/evidence/20260706_02/trace-boost-runtime-result.sanitized.json" "SANITIZED_0602_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_02/bat-trace-boost-runtime-result.sanitized.json" "SANITIZED_0602_BAT_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_02/create-domain-result.sanitized.json" "SANITIZED_0602_CREATE_DOMAIN"
Test-RequiredFile "specs/evidence/20260706_02/runtime-smoke-summary.sanitized.json" "SANITIZED_0602_RUNTIME_SUMMARY"
Test-RequiredFile "specs/evidence/20260706_02/runtime-closure-result.sanitized.json" "SANITIZED_0602_RUNTIME_CLOSURE"
Test-RequiredFile "specs/evidence/20260706_03/runtime-start-services-result.sanitized.json" "SANITIZED_0603_RUNTIME_START"
Test-RequiredFile "specs/evidence/20260706_03/runtime-stop-services-result.sanitized.json" "SANITIZED_0603_RUNTIME_STOP"
Test-RequiredFile "specs/evidence/20260706_03/runtime-status-result.sanitized.json" "SANITIZED_0603_RUNTIME_STATUS"
Test-RequiredFile "specs/evidence/20260706_03/runtime-diagnostics-result.sanitized.json" "SANITIZED_0603_RUNTIME_DIAGNOSTICS"
Test-RequiredFile "specs/evidence/20260706_03/runtime-closure-result.sanitized.json" "SANITIZED_0603_RUNTIME_CLOSURE"
Test-RequiredFile "specs/evidence/20260706_03/file-log-standard-result.sanitized.json" "SANITIZED_0603_FILE_LOG_STANDARD"
Test-RequiredFile "specs/evidence/20260706_03/file-log-grep-summary.sanitized.log" "SANITIZED_0603_FILE_LOG_GREP"
Test-RequiredFile "specs/evidence/20260706_03/trace-boost-runtime-result.sanitized.json" "SANITIZED_0603_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_03/bat-trace-boost-runtime-result.sanitized.json" "SANITIZED_0603_BAT_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_03/bat-log-bean-runtime-result.sanitized.json" "SANITIZED_0603_BAT_LOG_BEAN"
Test-RequiredFile "specs/evidence/20260706_03/adm-operation-console-runtime-result.sanitized.json" "SANITIZED_0603_ADM_OPERATION_CONSOLE"
Test-RequiredFile "specs/evidence/20260706_03/adm-log-policy-ui-static-result.sanitized.json" "SANITIZED_0603_ADM_LOG_POLICY_UI_STATIC"
Test-RequiredFile "specs/evidence/20260706_03/exs-timeout-retry-runtime-result.sanitized.json" "SANITIZED_0603_EXS_TIMEOUT_RETRY"
Test-RequiredFile "specs/evidence/20260706_03/cmn-fixed-length-advanced-result.sanitized.json" "SANITIZED_0603_CMN_FIXED_LENGTH"
Test-RequiredFile "specs/evidence/20260706_03/create-domain-result.sanitized.json" "SANITIZED_0603_CREATE_DOMAIN"
Test-RequiredFile "specs/evidence/20260706_04/packaged-runtime-resource-check.sanitized.json" "SANITIZED_0604_PACKAGED_RESOURCE_CHECK"
Test-RequiredFile "specs/evidence/20260706_04/runtime-start-services-result.sanitized.json" "SANITIZED_0604_RUNTIME_START"
Test-RequiredFile "specs/evidence/20260706_04/runtime-status-result.sanitized.json" "SANITIZED_0604_RUNTIME_STATUS"
Test-RequiredFile "specs/evidence/20260706_04/runtime-diagnostics-result.sanitized.json" "SANITIZED_0604_RUNTIME_DIAGNOSTICS"
Test-RequiredFile "specs/evidence/20260706_04/runtime-closure-result.sanitized.json" "SANITIZED_0604_RUNTIME_CLOSURE"
Test-RequiredFile "specs/evidence/20260706_04/file-log-standard-result.sanitized.json" "SANITIZED_0604_FILE_LOG_STANDARD"
Test-RequiredFile "specs/evidence/20260706_04/trace-boost-runtime-result.sanitized.json" "SANITIZED_0604_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_04/bat-trace-boost-runtime-result.sanitized.json" "SANITIZED_0604_BAT_TRACE_BOOST"
Test-RequiredFile "specs/evidence/20260706_04/bat-log-bean-runtime-result.sanitized.json" "SANITIZED_0604_BAT_LOG_BEAN"
Test-RequiredFile "specs/evidence/20260706_04/adm-operation-console-runtime-result.sanitized.json" "SANITIZED_0604_ADM_OPERATION_CONSOLE"
Test-RequiredFile "specs/evidence/20260706_04/exs-timeout-retry-runtime-result.sanitized.json" "SANITIZED_0604_EXS_TIMEOUT_RETRY"
Test-RequiredText "scripts/smoke-composite-transaction-runtime.ps1" "segmentCount must be at least 3" "SMOKE_COMPOSITE_SEGMENT_COUNT_ASSERT"
Test-RequiredText "scripts/smoke-composite-transaction-failure-runtime.ps1" "failedSegmentId" "SMOKE_COMPOSITE_FAILURE_SEGMENT_ASSERT"
Test-RequiredText "scripts/smoke-composite-transaction-failure-runtime.ps1" "EXS_TIMEOUT" "SMOKE_COMPOSITE_FAILURE_CODE_ASSERT"
Test-RequiredText "scripts/smoke-adm-transaction-group-runtime.ps1" "/adm/api/transaction-groups" "SMOKE_ADM_TRANSACTION_GROUP_ENDPOINT"
Test-RequiredText "scripts/smoke-adm-transaction-group-runtime.ps1" "/external-logs" "SMOKE_ADM_TRANSACTION_GROUP_EXTERNAL_LOGS"
Test-RequiredText "scripts/smoke-adm-transaction-group-runtime.ps1" "dbSegmentRows" "SMOKE_ADM_TRANSACTION_GROUP_DB_SEGMENT_ROWS"
Test-RequiredText "scripts/smoke-adm-transaction-group-failure-runtime.ps1" "failureYn=Y" "SMOKE_ADM_TRANSACTION_GROUP_FAILURE_QUERY"
Test-RequiredText "scripts/smoke-adm-transaction-group-failure-runtime.ps1" "EXS_TRANSACTION_LOG" "SMOKE_ADM_TRANSACTION_GROUP_EXS_LEDGER"
Test-RequiredText "scripts/smoke-adm-ui.ps1" "BrowserClick" "SMOKE_ADM_UI_BROWSER_CLICK_OPTION"
Test-RequiredText "scripts/smoke-adm-ui.ps1" "adm-ui-browser-smoke-result.json" "SMOKE_ADM_UI_BROWSER_CLICK_RESULT"
Test-RequiredText "scripts/smoke-adm-ui.ps1" "ADM_UI_BROWSER_CLICK_FLOW" "SMOKE_ADM_UI_PLAYWRIGHT_FLOW"
Test-RequiredFile "specs/evidence/20260703_04/standard-header-e2e-result.sanitized.json" "SANITIZED_STANDARD_HEADER_EVIDENCE"
Test-RequiredFile "specs/evidence/20260703_04/composite-transaction-runtime-result.sanitized.json" "SANITIZED_COMPOSITE_EVIDENCE"
Test-RequiredFile "specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json" "SANITIZED_ADM_TRANSACTION_GROUP_EVIDENCE"
Test-RequiredFile "specs/evidence/20260703_04/mariadb-full-install-result.sanitized.json" "SANITIZED_MARIADB_EVIDENCE"
Test-RequiredFile "specs/evidence/20260703_04/run-local-services-composite-rerun.sanitized.log" "SANITIZED_RUNTIME_LOG_EVIDENCE"

$canonicalFixtureName = "xyz_edu_query_fixture.sql"
$canonicalFixturePath = "xyz/src/test/resources/sql/$canonicalFixtureName"
$legacyFixtureName = "xyz_edu_query_" + "mapper_fixture.sql"
$legacyFixturePath = "xyz/src/test/resources/fixture/$legacyFixtureName"
$devGuideFileName = (New-UnicodeText @(0xAC1C, 0xBC1C, 0x5F, 0xAC00, 0xC774, 0xB4DC)) + ".html"
$featureMatrixFileName = (New-UnicodeText @(0xAE30, 0xB2A5, 0x5F, 0xAD6C, 0xD604, 0x5F, 0xB9E4, 0xD2B8, 0xB9AD, 0xC2A4)) + ".html"
Test-RequiredText "specs/$featureMatrixFileName" "adm-permission-runtime" "ADM_PERMISSION_RUNTIME_MATRIX"
Test-RequiredText "specs/$featureMatrixFileName" "scripts/smoke-mariadb-full-install.ps1" "MATRIX_MARIADB_FULL_INSTALL_SMOKE"
Test-RequiredText "specs/$featureMatrixFileName" "scripts/smoke-standard-header-e2e.ps1" "MATRIX_STANDARD_HEADER_E2E_SMOKE"
Test-RequiredText "CPF_STABILIZATION_REPORT.md" "scripts/smoke-mariadb-full-install.ps1" "REPORT_MARIADB_FULL_INSTALL_SMOKE"
Test-RequiredText "CPF_STABILIZATION_REPORT.md" "scripts/smoke-standard-header-e2e.ps1" "REPORT_STANDARD_HEADER_E2E_SMOKE"
$fixtureEvidenceFiles = @(
    "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java",
    "scripts/check-feature-evidence.ps1",
    "specs/$devGuideFileName",
    "specs/$featureMatrixFileName",
    "CPF_STABILIZATION_REPORT.md"
)

Test-ForbiddenFile $legacyFixturePath "EDU_LEGACY_MAPPER_FIXTURE"
foreach ($file in $fixtureEvidenceFiles) {
    Test-RequiredText $file $canonicalFixtureName "EDU_CANONICAL_FIXTURE_TEXT"
    Test-ForbiddenText $file $legacyFixtureName "EDU_LEGACY_FIXTURE_TEXT"
}
Test-RequiredFile $canonicalFixturePath "EDU_CANONICAL_FIXTURE_FILE"

$dbUsernameEnv = "CPF_XYZ_EDU_MAPPER_DB_USERNAME"
$legacyDbUserEnv = "CPF_XYZ_EDU_MAPPER_DB_USER"
$dbEnvEvidenceFiles = @(
    "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java",
    "specs/$devGuideFileName",
    "CPF_STABILIZATION_REPORT.md"
)
foreach ($file in $dbEnvEvidenceFiles) {
    Test-RequiredText $file $dbUsernameEnv "EDU_DB_USERNAME_ENV"
    Test-RequiredText $file $legacyDbUserEnv "EDU_DB_USER_LEGACY_ENV"
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Feature evidence check passed."
