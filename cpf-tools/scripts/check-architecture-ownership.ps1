param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$fixedModules = @(
    "cpf-core", "cpf-common", "cpf-reference", "cpf-biz-admin",
    "cpf-batch", "cpf-admin", "cpf-gateway",
    "cpf-local-runtime", "cpf-local-batch-runtime"
)
$fixedBusinessModules = @("cpf-reference", "cpf-biz-admin")
$fixedModulePackages = [ordered]@{
    "cpf-reference" = "com.cpf.reference"
    "cpf-biz-admin" = "com.cpf.bizadmin"
    "cpf-batch" = "com.cpf.batch"
    "cpf-admin" = "com.cpf.admin"
}

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

function Get-GeneratedRootProjects {
    $result = [System.Collections.Generic.List[object]]::new()
    $candidateDirectories = @(Get-ChildItem -LiteralPath $Root -Directory | Where-Object {
        (Test-Path -LiteralPath (Join-Path $_.FullName "manifest/domain-manifest.json") -PathType Leaf) -or
        (Test-Path -LiteralPath (Join-Path $_.FullName "manifest/generator-ownership.json") -PathType Leaf)
    })

    foreach ($directory in $candidateDirectories) {
        $domainManifestPath = Join-Path $directory.FullName "manifest/domain-manifest.json"
        $ownershipManifestPath = Join-Path $directory.FullName "manifest/generator-ownership.json"
        if (-not (Test-Path -LiteralPath $domainManifestPath -PathType Leaf) -or
                -not (Test-Path -LiteralPath $ownershipManifestPath -PathType Leaf)) {
            Add-Finding $failures "GENERATED_DOMAIN_MANIFEST_PAIR" "$($directory.Name)/manifest" `
                    "Generated Domain manifest pair가 완전하지 않습니다." `
                    "domain-manifest.json과 generator-ownership.json을 함께 생성하세요."
            continue
        }

        try {
            $domain = [System.IO.File]::ReadAllText($domainManifestPath, [System.Text.Encoding]::UTF8) |
                ConvertFrom-Json -ErrorAction Stop
            $ownership = [System.IO.File]::ReadAllText($ownershipManifestPath, [System.Text.Encoding]::UTF8) |
                ConvertFrom-Json -ErrorAction Stop
        } catch {
            Add-Finding $failures "GENERATED_DOMAIN_MANIFEST_JSON" "$($directory.Name)/manifest" `
                    "Generated Domain manifest JSON을 읽을 수 없습니다: $($_.Exception.Message)" `
                    "Generator 정본에서 두 manifest를 다시 생성하세요."
            continue
        }

        $identityValid =
            [string]$domain.domainType -ceq "GENERATED_DOMAIN" -and
            [string]$domain.dependencyModel -ceq "root-project" -and
            [string]$ownership.dependencyModel -ceq "root-project" -and
            [string]$domain.projectName -ceq $directory.Name -and
            [string]$ownership.projectName -ceq $directory.Name -and
            -not [string]::IsNullOrWhiteSpace([string]$domain.moduleName) -and
            [string]$domain.moduleName -ceq [string]$ownership.moduleName -and
            -not [string]::IsNullOrWhiteSpace([string]$domain.domainName) -and
            [string]$domain.domainName -ceq [string]$ownership.domainName -and
            -not [string]::IsNullOrWhiteSpace([string]$domain.systemCode) -and
            [string]$domain.systemCode -ceq [string]$ownership.systemCode -and
            [string]$domain.packageName -ceq [string]$ownership.packageName -and
            [string]$domain.packageName -match '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$'
        if (-not $identityValid) {
            Add-Finding $failures "GENERATED_DOMAIN_IDENTITY" "$($directory.Name)/manifest" `
                    "Generated Domain root-project identity가 두 manifest에서 일치하지 않습니다." `
                    "Generator canonical metadata에서 project/module/domain/SystemCode/package identity를 다시 생성하세요."
            continue
        }

        $result.Add([pscustomobject]@{
            projectName = $directory.Name
            moduleName = [string]$domain.moduleName
            domainName = [string]$domain.domainName
            systemCode = [string]$domain.systemCode
            packageName = [string]$domain.packageName
            packagePath = ([string]$domain.packageName).Replace('.', '/')
        }) | Out-Null
    }

    $duplicatePackages = @($result.ToArray() | Group-Object packageName | Where-Object { $_.Count -gt 1 })
    foreach ($duplicate in $duplicatePackages) {
        Add-Finding $failures "GENERATED_DOMAIN_PACKAGE_DUPLICATE" "manifest/generator-ownership.json" `
                "복수 Generated Domain이 packageName=$($duplicate.Name)을 공유합니다." `
                "신규 Domain마다 고유 PackageName을 사용하세요."
    }
    return @($result.ToArray() | Sort-Object projectName)
}

$generatedDomains = @(Get-GeneratedRootProjects)
$modules = @($fixedModules + @($generatedDomains | ForEach-Object { $_.projectName }) | Sort-Object -Unique)
$businessModules = @($fixedBusinessModules + @($generatedDomains | ForEach-Object { $_.projectName }) | Sort-Object -Unique)
$businessInternalTargets = @($businessModules + "cpf-batch" | Sort-Object -Unique)
$modulePackages = @{}
foreach ($entry in $fixedModulePackages.GetEnumerator()) {
    $modulePackages[$entry.Key] = $entry.Value
}
foreach ($domain in $generatedDomains) {
    $modulePackages[$domain.projectName] = $domain.packageName
}
$implementationPackages = @($modulePackages.Values | Sort-Object -Unique)
$generatedDomainPathRules = @($generatedDomains | ForEach-Object {
    [pscustomobject]@{
        projectName = $_.projectName
        projectPattern = [regex]::Escape($_.projectName)
        packagePath = $_.packagePath
        packagePathPattern = [regex]::Escape($_.packagePath)
    }
})

function Test-Text {
    param(
        [string] $Text,
        [string] $Pattern
    )
    return [System.Text.RegularExpressions.Regex]::IsMatch($Text, $Pattern)
}

function Get-JavaFiles {
    param([string] $Module)
    if ($Module -eq "cpf-batch") {
        $batchRoot = Join-Path $Root "cpf-batch"
        if (-not (Test-Path -LiteralPath $batchRoot)) {
            return @()
        }
        return @(
            Get-ChildItem -LiteralPath $batchRoot -Directory |
                Where-Object {
                    $_.Name -ne "src" -and
                    (Test-Path -LiteralPath (Join-Path $_.FullName "src/main/java"))
                } |
                ForEach-Object {
                    Get-ChildItem -LiteralPath (Join-Path $_.FullName "src/main/java") `
                            -Recurse -File -Filter "*.java"
                }
        )
    }
    $sourceRoot = Join-Path $Root "$Module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        return @()
    }
    return @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java")
}

function Get-StructuralPathRule {
    param([string] $RelativePath)
    $path = $RelativePath.Replace('\', '/')
    foreach ($domainRule in $generatedDomainPathRules) {
        $generatedRootLayerPattern =
            "^$($domainRule.projectPattern)/src/(main|test)/java/$($domainRule.packagePathPattern)/(controller|service|repository|dto|facade|port|adapter|validation)/"
        if ($path -match $generatedRootLayerPattern) {
            return 'GENERATED_DOMAIN_FEATURE_SLICE_REQUIRED'
        }
    }
    if ($path -match '^cpf-reference/src/(main|test)/java/com/cpf/reference/(controller|dto|service|repository|mapper|facade|operation)/') {
        return 'REF_EDU_CAPABILITY_SLICE_REQUIRED'
    }
    if ($path -match '^cpf-batch/[^/]+/src/(main|test)/java/com/cpf/batch/(?:[^/]+/)*edu(?:/|$)') {
        return 'BAT_RUNTIME_NO_EDU_SOURCE'
    }
    return $null
}

foreach ($module in $modules) {
    foreach ($file in (Get-JavaFiles $module)) {
        $checkedFiles++
        $relativePath = Get-RelativePath $file.FullName
        $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

        $structuralRule = Get-StructuralPathRule $relativePath
        if ($null -ne $structuralRule) {
            Add-Finding $failures $structuralRule $relativePath `
                    "기능 또는 JobDefinition 소유 경계 밖의 계층형 package에 있습니다." `
                    "owner feature 또는 owner job package 안으로 이동하고 source/test/resource를 함께 맞추세요."
        }

        $sourceMarker = if ($relativePath -match '/src/main/java/') { '/src/main/java/' } else { '/src/test/java/' }
        $declaredPackage = [regex]::Match($text, '(?m)^package\s+([a-zA-Z0-9_.]+);')
        if ($declaredPackage.Success) {
            $suffix = $relativePath.Substring($relativePath.IndexOf($sourceMarker) + $sourceMarker.Length)
            $expectedPackage = $suffix.Substring(0, $suffix.LastIndexOf('/')).Replace('/', '.')
            if ($declaredPackage.Groups[1].Value -ne $expectedPackage) {
                Add-Finding $failures "SOURCE_PACKAGE_PATH_MISMATCH" $relativePath `
                        "선언 package가 경로와 다릅니다. declared=$($declaredPackage.Groups[1].Value) expected=$expectedPackage" `
                        "source와 test package 경로를 같은 feature 구조로 이동하세요."
            }
        }

        if ($text -match 'import\s+[^;]*\.repository\.[^;]+;' -and
                $text -match '(?m)^\s*(public\s+)?class\s+\w*Controller\b') {
            Add-Finding $failures "CONTROLLER_NO_REPOSITORY_IMPORT" $relativePath `
                    "Controller가 Repository를 직접 import합니다." `
                    "Controller는 application service 또는 facade를 통해 접근하세요."
        }

        if ($module -eq "cpf-core") {
            foreach ($targetPackage in $implementationPackages) {
                $targetPackagePattern = [regex]::Escape($targetPackage)
                if (Test-Text $text "import\s+$targetPackagePattern\.") {
                    Add-Finding $failures "CPF_NO_BUSINESS_DEPENDENCY" $relativePath "cpf-core가 $targetPackage 구현을 참조합니다." "CPF에는 port와 계약만 두고 업무 구현 의존성은 adapter로 이동하세요."
                }
            }
        }

        if ($module -eq "cpf-common") {
            foreach ($targetPackage in $implementationPackages) {
                $targetPackagePattern = [regex]::Escape($targetPackage)
                if (Test-Text $text "import\s+$targetPackagePattern\.") {
                    Add-Finding $failures "CMN_NO_BUSINESS_DEPENDENCY" $relativePath "cpf-common이 $targetPackage 구현을 참조합니다." "CMN에는 프로젝트 공통 helper와 규칙만 두고 업무 구현 의존성을 제거하세요."
                }
            }
            if (Test-Text $text "KafkaTemplate|JmsTemplate|RabbitTemplate|Sftp|SFTP|FTP|FTPS|SSH") {
                if ($text -match "CPF-OWNERSHIP:CMN_PROJECT_HELPER") {
                    continue
                }
                if ($text -match "CPF-OWNERSHIP:CPF_PORT_MIGRATION_CANDIDATE") {
                    Add-Finding $warnings "CMN_CPF_PORT_MIGRATION_CANDIDATE" $relativePath "CMN has technology engine code kept for compatibility." "Move broker/file-transfer execution to CPF port adapter and keep CMN as envelope/rule/helper."
                } else {
                    Add-Finding $warnings "CMN_TECH_ENGINE_REVIEW" $relativePath "CMN has broker/file-transfer technical implementation candidate." "Move technical engine to CPF port if needed; keep CMN as project common rule/helper."
                }
            }
        }

        if ($module -eq "cpf-gateway") {
            foreach ($targetPackage in $implementationPackages) {
                $targetPackagePattern = [regex]::Escape($targetPackage)
                if (Test-Text $text "import\s+$targetPackagePattern\.") {
                    Add-Finding $failures "GATEWAY_NO_BUSINESS_DEPENDENCY" $relativePath "Gateway가 $targetPackage 구현을 참조합니다." "Gateway는 CPF route·policy port만 사용하고 업무 구현을 직접 참조하지 마세요."
                }
            }
        }

        if ($businessModules -contains $module) {
            if ($text -match '(?m)^\s*@(Aspect|Around|Before|After|AfterReturning|AfterThrowing)\b') {
                Add-Finding $failures "BUSINESS_NO_TECHNICAL_LOGGING_ASPECT" $relativePath "업무 주제영역에 기술 공통 AOP 구현이 있습니다." "거래·성능·오류 로그 Aspect는 CPF가 소유하고 업무 모듈은 annotation 또는 extension port만 사용하세요."
            }
            foreach ($targetModule in $businessInternalTargets) {
                if ($targetModule -eq $module) {
                    continue
                }
                $targetPackage = $modulePackages[$targetModule]
                $targetPackagePattern = [regex]::Escape($targetPackage)
                if (Test-Text $text "import\s+$targetPackagePattern\..*(controller|repository|mapper).*;") {
                    Add-Finding $failures "BUSINESS_NO_CROSS_DOMAIN_INTERNAL_IMPORT" $relativePath "업무 모듈이 $targetPackage Controller/Repository/Mapper를 직접 참조합니다." "CPF Service Call Engine, public facade 또는 분리된 DB 경계를 사용하세요."
                }
            }
            if (Test-Text $text "WebClient\s*\.\s*builder\s*\(|new\s+RestTemplate\s*\(|RestClient\s*\.\s*create\s*\(") {
                Add-Finding $failures "BUSINESS_NO_RAW_HTTP_CLIENT" $relativePath "Business module creates raw HTTP client." "Use CpfWebClient or CPF Service Call Engine."
            }
            if (Test-Text $text "https?://") {
                if ($relativePath -match "^cpf-reference/src/main/java/com/cpf/reference/" -and $text -match "CPF-ARCH-ALLOW-DIRECT-URL:\s*EDU_ONLY") {
                    continue
                }
                Add-Finding $warnings "BUSINESS_DIRECT_URL_REVIEW" $relativePath "Business source has URL literal." "Check whether this is education-only or should use registry/service-call."
            }
            if (Test-Text $text "KafkaTemplate|JmsTemplate|RabbitTemplate|RedisTemplate") {
                Add-Finding $warnings "BUSINESS_BROKER_ADAPTER_REVIEW" $relativePath "Business module has broker adapter candidate." "Keep common broker port in CPF and verify this is only a business adapter."
            }
        }
    }
}

# 과거 잘못된 구조를 fixture로 고정해 gate 규칙이 약화되는 회귀를 막습니다.
$regressionFixtures = @(
    @{ path = 'cpf-reference/src/main/java/com/cpf/reference/controller/ReferenceCrudEducationController.java'; rule = 'REF_EDU_CAPABILITY_SLICE_REQUIRED' },
    @{ path = 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/edu/BadWorkerEducationSample.java'; rule = 'BAT_RUNTIME_NO_EDU_SOURCE' },
    @{ path = 'cpf-batch/control-server/src/test/java/com/cpf/batch/control/edu/BadControlEducationSample.java'; rule = 'BAT_RUNTIME_NO_EDU_SOURCE' }
)
if ($generatedDomainPathRules.Count -gt 0) {
    $generatedFixture = $generatedDomainPathRules[0]
    $regressionFixtures += @{
        path = "$($generatedFixture.projectName)/src/main/java/$($generatedFixture.packagePath)/controller/GeneratedController.java"
        rule = 'GENERATED_DOMAIN_FEATURE_SLICE_REQUIRED'
    }
}
$fixtureResults = @()
foreach ($fixture in $regressionFixtures) {
    $detected = Get-StructuralPathRule $fixture.path
    $passed = $detected -eq $fixture.rule
    $fixtureResults += [ordered]@{
        path = $fixture.path
        expectedRule = $fixture.rule
        detectedRule = $detected
        passed = $passed
    }
    if (-not $passed) {
        Add-Finding $failures "ARCHITECTURE_REGRESSION_FIXTURE_MISSED" $fixture.path `
                "과거 구조 오류 fixture를 탐지하지 못했습니다." `
                "구조 규칙과 fixture를 함께 복구하세요."
    }
}

# 직전 검수에서 확인된 ADM 핵심 경계는 회귀를 허용하지 않습니다.
$admBoundaryTargets = @(
    "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmReliabilityController.java",
    "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmReliabilityService.java",
    "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmServiceRegistryService.java",
    "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmTransactionGroupService.java"
)
foreach ($relativePath in $admBoundaryTargets) {
    $absolutePath = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $absolutePath)) {
        Add-Finding $failures "ADM_CPF_BOUNDARY_TARGET_MISSING" $relativePath "경계 검증 대상 파일이 없습니다." "삭제 또는 이동 사유를 gate와 함께 갱신하세요."
        continue
    }
    $text = [System.IO.File]::ReadAllText($absolutePath, [System.Text.Encoding]::UTF8)
    if (Test-Text $text 'TransactionLogRecoveryWorker|TransactionSegmentMapper|@Qualifier\("cpfJdbcTemplate"\)|\bJdbcTemplate\b') {
        Add-Finding $failures "ADM_NO_CPF_INTERNAL_ACCESS" $relativePath "ADM 핵심 운영 코드가 CPF 내부 worker 또는 JDBC에 직접 접근합니다." "CPF public query/command port 또는 facade를 사용하세요."
    }
    if (Test-Text $text '(?i)(FROM|JOIN|UPDATE|INSERT\s+INTO|DELETE\s+FROM)\s+cpf_[a-z0-9_]+') {
        Add-Finding $failures "ADM_NO_CPF_TABLE_SQL" $relativePath "ADM 핵심 운영 코드에 CPF table SQL이 있습니다." "SQL 소유권을 CPF facade로 이동하세요."
    }
}

$requiredCapabilityFiles = @(
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerPublisher.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerConsumer.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerMessage.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerEnvelope.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerReplayPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerHealthPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerHistoryPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerOutboxPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerInboxPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerIdempotencyPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqReplayRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerDlqReplayResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferClient.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferEndpoint.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferHistoryPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferHealthPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferProtocol.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferPolicy.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileChecksumPolicy.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfDuplicatePreventionPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/filetransfer/CpfFileTransferHistoryQuery.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialRef.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialProviderPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfCredentialValidationResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfTokenProviderPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfTokenRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfTokenResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfSecretProviderPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfKeyProviderPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfCertificateProviderPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfSignaturePort.java",
    "cpf-core/src/main/java/com/cpf/core/common/security/CpfEncryptionPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfDistributedLockPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfHeartbeatPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfHealthCheckPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfGhostDetectorPort.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfLockAcquireRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfLockAcquireResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfHeartbeatRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfHeartbeatResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfGhostDetectionResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfWorkerControlRequest.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfWorkerControlResult.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtime/CpfRuntimeHealthQuery.java",
    "cpf-core/src/main/java/com/cpf/core/common/admin/CpfBrokerStatusQuery.java",
    "cpf-core/src/main/java/com/cpf/core/common/admin/CpfFileTransferStatusQuery.java",
    "cpf-core/src/main/java/com/cpf/core/common/admin/CpfCredentialStatusQuery.java",
    "cpf-core/src/main/java/com/cpf/core/common/admin/CpfRuntimeHealthStatusQuery.java"
)

foreach ($relativePath in $requiredCapabilityFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $relativePath))) {
        Add-Finding $failures "CPF_CAPABILITY_SKELETON_MISSING" $relativePath "필수 CPF capability skeleton 파일이 없습니다." "요청서 기준의 port/interface/DTO를 CPF에 추가하세요."
    }
}

$requiredFixedLengthContracts = @(
    "cpf-core/src/main/java/com/cpf/core/api/fixedlength/CpfFixedLengthLayout.java",
    "cpf-core/src/main/java/com/cpf/core/api/fixedlength/CpfFixedLengthParser.java",
    "cpf-core/src/main/java/com/cpf/core/api/fixedlength/CpfFixedLengthWriter.java"
)
foreach ($relativePath in $requiredFixedLengthContracts) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $relativePath) -PathType Leaf)) {
        Add-Finding $failures "CPF_FIXED_LENGTH_CONTRACT_MISSING" $relativePath `
                "범용 고정길이 전문 Contract가 cpf-core 공개 API에 없습니다." `
                "Layout/Parser/Writer Contract와 기술 Engine은 cpf-core가 소유해야 합니다."
    }
}

$cmnFixedLengthEngines = @(Get-ChildItem -LiteralPath (Join-Path $Root "cpf-common/src/main/java") -Recurse -File -Filter "*.java" |
    Where-Object {
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
        $text -match "(?m)^package\s+[^;]*[.]fixedlength(?:[.;])|class\s+\w*FixedLength(Parser|Writer|Codec|Engine|Formatter)\b"
    })
foreach ($file in $cmnFixedLengthEngines) {
    Add-Finding $failures "CMN_NO_FIXED_LENGTH_ENGINE" (Get-RelativePath $file.FullName) `
            "cpf-common에 범용 고정길이 기술 Engine 후보가 있습니다." `
            "고객 공통 규칙만 CMN에 두고 범용 Layout/Parser/Writer Engine은 cpf-core로 이동하세요."
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
    checkedModules = $modules
    generatedDomains = @($generatedDomains | ForEach-Object { $_.projectName })
    failures = @($failures.ToArray())
    reviewCandidates = @($warnings.ToArray())
    requiredCapabilities = $requiredCapabilityFiles
    regressionFixtures = $fixtureResults
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
