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

# PFW core evidence.
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfGlobalExceptionHandler.java" "PFW_GLOBAL_EXCEPTION_HANDLER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfErrorResponse.java" "PFW_ERROR_RESPONSE"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/filter/TransactionContextFilter.java" "PFW_TRANSACTION_CONTEXT_FILTER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" "PFW_TRANSACTION_ID_GENERATOR"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "PFW_LOGGING_ASPECT"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLauncher.java" "PFW_BATCH_LAUNCHER"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java" "PFW_BATCH_OPERATION_REPOSITORY"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java" "PFW_OPENAPI_CONFIG"

# CMN project common evidence.
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceService.java" "CMN_SEQUENCE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java" "CMN_NOTIFICATION_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogService.java" "CMN_BUSINESS_LOG_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java" "CMN_FILE_EXCHANGE_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/tlm/service/CmnTelegramService.java" "CMN_TELEGRAM_SERVICE"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java" "CMN_MESSAGE_BRIDGE_SERVICE"
Test-RequiredFile "cmn/src/test/java/cpf/cmn/biz/sequence/CmnSequenceServiceConcurrencyTest.java" "CMN_SEQUENCE_CONCURRENCY_TEST"

# ADM operation evidence.
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "ADM_PERMISSION_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMemberController.java" "ADM_MEMBER_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmLogController.java" "ADM_LOG_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java" "ADM_BATCH_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCacheController.java" "ADM_CACHE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMessageController.java" "ADM_MESSAGE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCodeController.java" "ADM_CODE_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmConfigController.java" "ADM_CONFIG_API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmDownloadService.java" "ADM_DOWNLOAD_SERVICE"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmDownloadServiceTest.java" "ADM_DOWNLOAD_TEST"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/batch" "ADM_BATCH_UI_ROUTE"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/logs" "ADM_LOG_UI_ROUTE"

# Business module evidence.
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

# EDU evidence.
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java" "EDU_CRUD"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java" "EDU_CMN_BUSINESS"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzBatchEducationController.java" "EDU_BATCH"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java" "EDU_SERVICE_CALL"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzTelegramEducationController.java" "EDU_TELEGRAM"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzFileExchangeEducationController.java" "EDU_FILE_EXCHANGE"

# SQL, migration, docs, and smoke scripts.
Test-RequiredFile "specs/sql/00_all_install.sql" "SQL_ALL_INSTALL"
Test-RequiredFile "specs/sql/00_all_install_and_smoke.sql" "SQL_ALL_INSTALL_AND_SMOKE"
Test-RequiredFile "specs/sql/migration/flyway/V1__cpf_baseline_install.sql" "FLYWAY_BASELINE"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "bizadm_customer" "SQL_BIZADM_TABLE"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "exs_transaction_log" "SQL_EXS_TABLE"
Test-RequiredText "specs/sql/40_business_modules_schema.sql" "mbr_member" "SQL_MBR_TABLE"
Test-RequiredFile "README.md" "README"
Test-RequiredFile "specs/index.html" "DOC_INDEX"
Test-SpecHtmlDocuments
Test-RequiredFile "scripts/check-sql-standard.ps1" "CHECK_SQL_STANDARD"
Test-RequiredFile "scripts/smoke-openapi.ps1" "SMOKE_OPENAPI"
Test-RequiredFile "scripts/smoke-adm-ui.ps1" "SMOKE_ADM_UI"

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Feature evidence check passed."
