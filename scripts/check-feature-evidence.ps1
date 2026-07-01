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

Test-RequiredText "settings.gradle" "include 'pfw', 'cmn', 'acc', 'mbr', 'xyz', 'adm', 'bizadm', 'exs', 'bat'" "BAT_SETTINGS_INCLUDE"
Test-RequiredFile "bat/build.gradle" "BAT_BUILD"
Test-RequiredFile "bat/src/main/java/cpf/bat/BatApplication.java" "BAT_APPLICATION"
Test-RequiredFile "bat/src/main/java/cpf/bat/config/BatBatchRepositoryConfig.java" "BAT_BATCH_REPOSITORY_CONFIG"
Test-RequiredFile "bat/src/main/java/cpf/bat/job/BatSmokeJobConfig.java" "BAT_SMOKE_JOB_CONFIG"
Test-RequiredFile "bat/src/main/java/cpf/bat/operation/BatSmokeOperationService.java" "BAT_SMOKE_OPERATION_SERVICE"
Test-RequiredFile "bat/src/main/java/cpf/bat/operation/BatHealthController.java" "BAT_HEALTH_CONTROLLER"
Test-RequiredFile "bat/src/test/java/cpf/bat/operation/BatSmokeOperationServiceTest.java" "BAT_OPERATION_TEST"
Test-RequiredFile "scripts/smoke-bat-runtime.ps1" "SMOKE_BAT_RUNTIME"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "AllowLegacyFallback" "BAT_SMOKE_STRICT_V10_OPTION"
Test-RequiredText "scripts/smoke-bat-runtime.ps1" "V10 extendedExecution.processed_count is missing" "BAT_SMOKE_STRICT_V10_MESSAGE"
Test-RequiredText "README.md" "scripts/smoke-bat-runtime.ps1" "BAT_DOC_README_SMOKE"
Test-RequiredText "README.md" "BatApplication" "BAT_DOC_README_MODULE"

Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceService.java" "CMN_SEQUENCE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java" "CMN_NOTIFICATION_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogService.java" "CMN_BUSINESS_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java" "CMN_FILE_EXCHANGE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/tlm/service/CmnTelegramService.java" "CMN_TELEGRAM_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java" "CMN_MESSAGE_BRIDGE_SERVICE"
Test-RequiredFile "cmn/src/test/java/cpf/cmn/biz/sequence/CmnSequenceServiceConcurrencyTest.java" "CMN_SEQUENCE_CONCURRENCY_TEST"

Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "ADM_PERMISSION_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java" "ADM_API_PERMISSION_DTO"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmPermissionServiceTest.java" "ADM_PERMISSION_SERVICE_TEST"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "/api-permissions" "ADM_API_PERMISSION_ENDPOINT"
Test-RequiredText "adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java" "adm_api_permission" "ADM_API_PERMISSION_FILTER"
Test-RequiredText "scripts/smoke-adm-runtime.ps1" "/adm/api/permissions/api-permissions" "ADM_PERMISSION_RUNTIME_SMOKE_API"
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
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/transactions" "ADM_TRANSACTION_META_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/log-policies" "ADM_LOG_POLICY_UI_ROUTE"

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
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzQueryEducationController.java" "EDU_QUERY"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/service/XyzQueryEducationService.java" "EDU_QUERY_SERVICE"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/repository/XyzQueryEducationRepository.java" "EDU_QUERY_REPOSITORY"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/mapper/XyzQueryEducationMapper.java" "EDU_QUERY_MAPPER"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationCriteria.java" "EDU_QUERY_CRITERIA_DTO"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationItem.java" "EDU_QUERY_ITEM_DTO"
Test-RequiredFile "xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml" "EDU_QUERY_MAPPER_XML"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationRepositoryTest.java" "EDU_QUERY_REPOSITORY_TEST"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java" "EDU_QUERY_MAPPER_SLICE_TEST"
Test-RequiredFile "xyz/src/test/resources/sql/xyz_edu_query_fixture.sql" "EDU_QUERY_MAPPER_FIXTURE"
Test-RequiredFile "xyz/src/test/java/cpf/xyz/edu/service/XyzQueryEducationServiceTest.java" "EDU_QUERY_SERVICE_TEST"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java" "EDU_CMN_BUSINESS"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzBatchEducationController.java" "EDU_BATCH"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java" "EDU_SERVICE_CALL"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzTelegramEducationController.java" "EDU_TELEGRAM"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzFileExchangeEducationController.java" "EDU_FILE_EXCHANGE"

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
Test-RequiredText "scripts/smoke-adm-ui.ps1" "BrowserClick" "SMOKE_ADM_UI_BROWSER_CLICK_OPTION"
Test-RequiredText "scripts/smoke-adm-ui.ps1" "adm-ui-browser-smoke-result.json" "SMOKE_ADM_UI_BROWSER_CLICK_RESULT"
Test-RequiredText "scripts/smoke-adm-ui.ps1" "ADM_UI_BROWSER_CLICK_FLOW" "SMOKE_ADM_UI_PLAYWRIGHT_FLOW"

$canonicalFixtureName = "xyz_edu_query_fixture.sql"
$canonicalFixturePath = "xyz/src/test/resources/sql/$canonicalFixtureName"
$legacyFixtureName = "xyz_edu_query_" + "mapper_fixture.sql"
$legacyFixturePath = "xyz/src/test/resources/fixture/$legacyFixtureName"
$devGuideFileName = (New-UnicodeText @(0xAC1C, 0xBC1C, 0x5F, 0xAC00, 0xC774, 0xB4DC)) + ".html"
$featureMatrixFileName = (New-UnicodeText @(0xAE30, 0xB2A5, 0x5F, 0xAD6C, 0xD604, 0x5F, 0xB9E4, 0xD2B8, 0xB9AD, 0xC2A4)) + ".html"
$fixtureEvidenceFiles = @(
    "xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java",
    "scripts/check-feature-evidence.ps1",
    "specs/$devGuideFileName",
    "specs/$featureMatrixFileName",
    "CPF_STABILIZATION_REPORT.html"
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
    "CPF_STABILIZATION_REPORT.html"
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
