param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

$ErrorActionPreference = "Stop"

$modules = @("pfw", "cmn", "acc", "mbr", "xyz", "bizadm", "exs", "bat", "adm")
$businessModules = @("acc", "mbr", "xyz", "bizadm", "exs", "bat")
$implementationModules = @("acc", "mbr", "xyz", "bizadm", "exs", "bat", "adm")

$failures = New-Object System.Collections.Generic.List[object]
$warnings = New-Object System.Collections.Generic.List[object]
$checkedFiles = 0

function Get-RelativePath {
    param([string] $Path)
    return $Path.Substring($Root.Length + 1).Replace("\", "/")
}

function Add-Finding {
    param(
        [System.Collections.Generic.List[object]] $Target,
        [string] $Rule,
        [string] $Path,
        [string] $Detail,
        [string] $Recommendation
    )

    $Target.Add([pscustomobject]@{
        rule = $Rule
        path = $Path
        detail = $Detail
        recommendation = $Recommendation
    })
}

function Test-Text {
    param(
        [string] $Text,
        [string] $Pattern
    )
    return [System.Text.RegularExpressions.Regex]::IsMatch($Text, $Pattern)
}

function Get-JavaFiles {
    param([string] $Module)
    $sourceRoot = Join-Path $Root "$Module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        return @()
    }
    return @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java")
}

foreach ($module in $modules) {
    foreach ($file in (Get-JavaFiles $module)) {
        $checkedFiles++
        $relativePath = Get-RelativePath $file.FullName
        $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

        if ($module -eq "pfw") {
            foreach ($targetModule in $implementationModules) {
                if (Test-Text $text "import\s+cpf\.$targetModule\.") {
                    Add-Finding $failures "PFW_NO_BUSINESS_DEPENDENCY" $relativePath "PFW imports cpf.$targetModule implementation." "Keep PFW as port/interface owner and move implementation dependency to business adapter."
                }
            }
        }

        if ($module -eq "cmn") {
            foreach ($targetModule in $implementationModules) {
                if (Test-Text $text "import\s+cpf\.$targetModule\.") {
                    Add-Finding $failures "CMN_NO_BUSINESS_DEPENDENCY" $relativePath "CMN imports cpf.$targetModule implementation." "Keep CMN as project common helper/rule owner and remove business implementation dependency."
                }
            }
            if (Test-Text $text "KafkaTemplate|JmsTemplate|RabbitTemplate|Sftp|SFTP|FTP|FTPS|SSH") {
                if ($text -match "CPF-OWNERSHIP:CMN_PROJECT_HELPER") {
                    continue
                }
                if ($text -match "CPF-OWNERSHIP:PFW_PORT_MIGRATION_CANDIDATE") {
                    Add-Finding $warnings "CMN_PFW_PORT_MIGRATION_CANDIDATE" $relativePath "CMN has technology engine code kept for compatibility." "Move broker/file-transfer execution to PFW port adapter and keep CMN as envelope/rule/helper."
                } else {
                    Add-Finding $warnings "CMN_TECH_ENGINE_REVIEW" $relativePath "CMN has broker/file-transfer technical implementation candidate." "Move technical engine to PFW port if needed; keep CMN as project common rule/helper."
                }
            }
        }

        if ($businessModules -contains $module) {
            foreach ($targetModule in $businessModules) {
                if ($targetModule -eq $module) {
                    continue
                }
                if (Test-Text $text "import\s+cpf\.$targetModule\..*(controller|repository|mapper).*;") {
                    Add-Finding $failures "BUSINESS_NO_CROSS_DOMAIN_INTERNAL_IMPORT" $relativePath "Business module imports cpf.$targetModule Controller/Repository/Mapper directly." "Use PFW Service Call Engine, CMN helper, public facade, or DB boundary separation."
                }
            }
            if ($module -ne "exs" -and (Test-Text $text "import\s+cpf\.exs\.")) {
                Add-Finding $failures "BUSINESS_NO_EXS_TECH_REUSE" $relativePath "Business module imports EXS internal class as reusable technology." "Move shared technology candidate to PFW/CMN capability and keep EXS as adapter."
            }
            if (Test-Text $text "WebClient\s*\.\s*builder\s*\(|new\s+RestTemplate\s*\(|RestClient\s*\.\s*create\s*\(") {
                Add-Finding $failures "BUSINESS_NO_RAW_HTTP_CLIENT" $relativePath "Business module creates raw HTTP client." "Use CpfWebClient or PFW Service Call Engine."
            }
            if (Test-Text $text "https?://") {
                if ($relativePath -match "^xyz/src/main/java/cpf/xyz/edu/" -and $text -match "CPF-ARCH-ALLOW-DIRECT-URL:\s*EDU_ONLY") {
                    continue
                }
                Add-Finding $warnings "BUSINESS_DIRECT_URL_REVIEW" $relativePath "Business source has URL literal." "Check whether this is education-only or should use registry/service-call."
            }
            if (Test-Text $text "KafkaTemplate|JmsTemplate|RabbitTemplate|RedisTemplate") {
                Add-Finding $warnings "BUSINESS_BROKER_ADAPTER_REVIEW" $relativePath "Business module has broker adapter candidate." "Keep common broker port in PFW and verify this is only a business adapter."
            }
        }
    }
}

# 직전 검수에서 확인된 ADM 핵심 경계는 회귀를 허용하지 않습니다.
$admBoundaryTargets = @(
    "adm/src/main/java/cpf/adm/opr/controller/AdmReliabilityController.java",
    "adm/src/main/java/cpf/adm/opr/service/AdmReliabilityService.java",
    "adm/src/main/java/cpf/adm/opr/service/AdmServiceRegistryService.java",
    "adm/src/main/java/cpf/adm/opr/service/AdmTransactionGroupService.java"
)
foreach ($relativePath in $admBoundaryTargets) {
    $absolutePath = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $absolutePath)) {
        Add-Finding $failures "ADM_PFW_BOUNDARY_TARGET_MISSING" $relativePath "경계 검증 대상 파일이 없습니다." "삭제 또는 이동 사유를 gate와 함께 갱신하세요."
        continue
    }
    $text = [System.IO.File]::ReadAllText($absolutePath, [System.Text.Encoding]::UTF8)
    if (Test-Text $text 'TransactionLogRecoveryWorker|TransactionSegmentMapper|@Qualifier\("pfwJdbcTemplate"\)|\bJdbcTemplate\b') {
        Add-Finding $failures "ADM_NO_PFW_INTERNAL_ACCESS" $relativePath "ADM 핵심 운영 코드가 PFW 내부 worker 또는 JDBC에 직접 접근합니다." "PFW public query/command port 또는 facade를 사용하세요."
    }
    if (Test-Text $text '(?i)(FROM|JOIN|UPDATE|INSERT\s+INTO|DELETE\s+FROM)\s+pfw_[a-z0-9_]+') {
        Add-Finding $failures "ADM_NO_PFW_TABLE_SQL" $relativePath "ADM 핵심 운영 코드에 PFW table SQL이 있습니다." "SQL 소유권을 PFW facade로 이동하세요."
    }
}

$requiredCapabilityFiles = @(
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerPublisher.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerConsumer.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerMessage.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerEnvelope.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerResult.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerReplayPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerHealthPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerHistoryPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerOutboxPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerInboxPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerIdempotencyPort.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayRequest.java",
    "pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayResult.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferClient.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferPort.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferRequest.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferResult.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferHistoryPort.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferHealthPort.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferProtocol.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferPolicy.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumPolicy.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfDuplicatePreventionPort.java",
    "pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferHistoryQuery.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfCredentialRef.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfCredentialValidationResult.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfTokenProviderPort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfTokenRequest.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfTokenResult.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfSecretProviderPort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfKeyProviderPort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfSignaturePort.java",
    "pfw/src/main/java/cpf/pfw/common/security/CpfEncryptionPort.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfDistributedLockPort.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfHeartbeatPort.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfHealthCheckPort.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfGhostDetectorPort.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireRequest.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireResult.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfHeartbeatRequest.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfHeartbeatResult.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfGhostDetectionResult.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfWorkerControlRequest.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfWorkerControlResult.java",
    "pfw/src/main/java/cpf/pfw/common/runtime/CpfRuntimeHealthQuery.java",
    "pfw/src/main/java/cpf/pfw/common/admin/CpfBrokerStatusQuery.java",
    "pfw/src/main/java/cpf/pfw/common/admin/CpfFileTransferStatusQuery.java",
    "pfw/src/main/java/cpf/pfw/common/admin/CpfCredentialStatusQuery.java",
    "pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java"
)

foreach ($relativePath in $requiredCapabilityFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $relativePath))) {
        Add-Finding $failures "PFW_CAPABILITY_SKELETON_MISSING" $relativePath "필수 PFW capability skeleton 파일이 없습니다." "요청서 기준의 port/interface/DTO를 PFW에 추가하세요."
    }
}

$fixedLengthFiles = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.java" |
    Where-Object {
        $_.FullName -notmatch "\\build\\" -and
        ([System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8) -match "FixedLength|fixed-length|고정길이")
    })
if ($fixedLengthFiles.Count -gt 0) {
    $nonExsFixedLength = @($fixedLengthFiles | Where-Object { (Get-RelativePath $_.FullName) -notmatch "^exs/" })
    if ($nonExsFixedLength.Count -eq 0) {
        Add-Finding $warnings "FIXED_LENGTH_EXS_ONLY_REVIEW" "exs/src/main/java" "Fixed-length parser/formatter candidate appears to be EXS-only." "Move to CMN helper or PFW/CMN common capability."
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "architecture-ownership-scan.sanitized.json"
$status = "DONE"
$statusCode = "DONE"
if ($failures.Count -gt 0) {
    $status = "FAILED"
    $statusCode = "FAILED"
} elseif ($warnings.Count -gt 0) {
    $status = "NEEDS_REVIEW"
    $statusCode = "NEEDS_REVIEW"
}

$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $status
    checkedFiles = $checkedFiles
    failureCount = $failures.Count
    warningCount = $warnings.Count
    failures = @($failures.ToArray())
    reviewCandidates = @($warnings.ToArray())
    requiredCapabilities = $requiredCapabilityFiles
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Architecture ownership scan statusCode=$statusCode failures=$($failures.Count) warnings=$($warnings.Count) evidence=$resultPath"
if ($warnings.Count -gt 0) {
    $warnings | ForEach-Object { Write-Host "REVIEW [$($_.rule)] $($_.path)" }
}
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL [$($_.rule)] $($_.path)" }
    exit 1
}
