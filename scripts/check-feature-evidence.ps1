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
        $failures.Add("필수 기능 증거 파일 없음 [$Name]: $Path")
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
        $failures.Add("필수 기능 증거 파일 없음 [$Name]: $Path")
        return
    }

    $content = [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
    if ($content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        $failures.Add("필수 기능 증거 문구 없음 [$Name]: $Path :: $Text")
    }
}

# PFW 코어: 표준 오류, 거래 헤더, 거래 로그, 배치 공통 API, OpenAPI 기본 설정.
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfGlobalExceptionHandler.java" "PFW 표준 예외 처리"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/exception/CpfErrorResponse.java" "PFW 표준 오류 응답"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/filter/TransactionContextFilter.java" "PFW 거래 헤더 필터"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" "PFW 거래 ID 생성"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/logging/LoggingAspect.java" "PFW 거래 로그 AOP"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLauncher.java" "PFW 배치 실행 API"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java" "PFW 배치 운영 저장소"
Test-RequiredFile "pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java" "PFW OpenAPI 설정"

# CMN 프로젝트 공통: 채번, 업무 알림, 업무 로그, 파일/전문/MQ 보조 기능과 테스트.
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceService.java" "CMN 채번 서비스"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java" "CMN 업무 알림 로그"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogService.java" "CMN 업무 로그"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java" "CMN 파일 교환"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/tlm/service/CmnTelegramService.java" "CMN 고정길이 전문"
Test-RequiredFile "cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java" "CMN 메시지 브리지"
Test-RequiredFile "cmn/src/test/java/cpf/cmn/biz/sequence/CmnSequenceServiceConcurrencyTest.java" "CMN 채번 동시성 테스트"

# ADM 운영자 기능: 권한, 회원, 로그, 배치, 캐시, 메시지, 코드, 설정, 다운로드.
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java" "ADM 권한 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMemberController.java" "ADM 회원 관리 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmLogController.java" "ADM 로그 관제 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java" "ADM 배치 관제 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCacheController.java" "ADM 캐시 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmMessageController.java" "ADM 메시지 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmCodeController.java" "ADM 코드 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/controller/AdmConfigController.java" "ADM 설정 API"
Test-RequiredFile "adm/src/main/java/cpf/adm/opr/service/AdmDownloadService.java" "ADM 다운로드 감사"
Test-RequiredFile "adm/src/test/java/cpf/adm/opr/service/AdmDownloadServiceTest.java" "ADM 다운로드 테스트"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/batch" "ADM 배치 화면 API 연결"
Test-RequiredText "adm/src/main/resources/static/adm/adm.js" "/adm/api/logs" "ADM 로그 화면 API 연결"

# BIZADM, MBR, EXS 실제 구현체와 테스트 증거.
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/auth/controller/BizAdmAuthController.java" "BIZADM 인증 API"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/controller/BizAdmOperationController.java" "BIZADM 운영 API"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/service/BizAdmOperationService.java" "BIZADM 운영 서비스"
Test-RequiredFile "bizadm/src/main/java/cpf/bizadm/operation/repository/BizAdmOperationRepository.java" "BIZADM 운영 저장소"
Test-RequiredFile "bizadm/src/test/java/cpf/bizadm/operation/service/BizAdmOperationServiceTest.java" "BIZADM 운영 테스트"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/controller/MbrController.java" "MBR 회원 API"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/controller/MbrAuthController.java" "MBR 인증 API"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrService.java" "MBR 회원 서비스"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/service/MbrAuthService.java" "MBR 인증 서비스"
Test-RequiredFile "mbr/src/main/java/cpf/mbr/bse/mapper/MemberMapper.java" "MBR 회원 Mapper"
Test-RequiredFile "mbr/src/test/java/cpf/mbr/bse/controller/MbrControllerValidationTest.java" "MBR API 테스트"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/controller/ExsOperationController.java" "EXS 업무 API"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/controller/ExsAdminOperationController.java" "EXS 운영 API"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/service/ExsOperationService.java" "EXS 운영 서비스"
Test-RequiredFile "exs/src/main/java/cpf/exs/operation/repository/ExsOperationRepository.java" "EXS 운영 저장소"
Test-RequiredFile "exs/src/main/java/cpf/exs/flow/service/ExsFlowService.java" "EXS 연계 흐름 서비스"
Test-RequiredFile "exs/src/test/java/cpf/exs/operation/service/ExsOperationServiceTest.java" "EXS 운영 테스트"

# EDU 샘플: 개발자가 기능 유형별로 참고할 수 있는 교육 API.
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java" "EDU CRUD 샘플"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java" "EDU CMN 업무 공통 샘플"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzBatchEducationController.java" "EDU 배치 샘플"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java" "EDU 외부 호출 샘플"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzTelegramEducationController.java" "EDU 전문 샘플"
Test-RequiredFile "xyz/src/main/java/cpf/xyz/edu/controller/XyzFileExchangeEducationController.java" "EDU 파일 교환 샘플"

# SQL, Flyway, 가이드 문서, 검증 스크립트가 함께 있어야 기능 증거가 닫힙니다.
Test-RequiredFile "specs/sql/00_all_install.sql" "전체 설치 SQL"
Test-RequiredFile "specs/sql/00_all_install_and_smoke.sql" "전체 설치 및 smoke SQL"
Test-RequiredFile "specs/sql/migration/flyway/V1__cpf_baseline_install.sql" "Flyway 기준 migration"
Test-RequiredText "specs/sql/40_business_sample_schema.sql" "bizadm_customer" "BIZADM SQL 테이블"
Test-RequiredText "specs/sql/40_business_sample_schema.sql" "exs_transaction_log" "EXS SQL 테이블"
Test-RequiredText "specs/sql/40_business_sample_schema.sql" "mbr_member" "MBR SQL 테이블"
Test-RequiredFile "README.md" "README 진입 문서"
Test-RequiredFile "specs/index.html" "문서 인덱스"
Test-RequiredFile "specs/개발_가이드.html" "개발 가이드"
Test-RequiredFile "specs/관리자_가이드.html" "관리자 가이드"
Test-RequiredFile "specs/프레임워크_구성_가이드.html" "프레임워크 구성 가이드"
Test-RequiredFile "specs/SQL_가이드.html" "SQL 가이드"
Test-RequiredFile "specs/기능_구현_매트릭스.html" "기능 구현 매트릭스"
Test-RequiredFile "scripts/check-sql-standard.ps1" "SQL 표준 검사"
Test-RequiredFile "scripts/smoke-openapi.ps1" "OpenAPI smoke"
Test-RequiredFile "scripts/smoke-adm-ui.ps1" "ADM UI smoke"

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Feature evidence check passed."
