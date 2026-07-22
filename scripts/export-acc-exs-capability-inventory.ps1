param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

# Inventory는 과거 Evidence를 재사용하지 않고 현재 Source·Test·SQL을 직접 검사해 생성합니다.
$capabilities = @(
    [ordered]@{
        id = "ACC-ACCOUNT-CRUD"
        owner = "ACC"
        description = "계좌 등록·조회·수정과 업무 경계를 제공"
        paths = @(
            "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountController.java",
            "cpf-account/src/main/java/com/cpf/account/account/service/AccAccountService.java",
            "cpf-account/src/test/java/com/cpf/account/account/service/AccAccountServiceTest.java",
            "specs/sql/migration/flyway/V1__cpf_baseline_install.sql"
        )
    },
    [ordered]@{
        id = "ACC-SHARED-CONSUMER"
        owner = "ACC/MBR"
        description = "ACC 공유 계약과 MBR 원격 소비자를 연결"
        paths = @(
            "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountSharedController.java",
            "cpf-account/src/main/java/com/cpf/account/account/facade/AccAccountSummaryFacade.java",
            "cpf-member/src/main/java/com/cpf/member/integration/account/MbrAccountSummaryRemoteProxy.java",
            "cpf-member/src/test/java/com/cpf/member/integration/account/MbrAccountSummaryRemoteProxyTest.java"
        )
    },
    [ordered]@{
        id = "REF-EXTERNAL-EDU"
        owner = "REF/EXS"
        description = "대외 호출 정상·오류·결과 불명 교육 흐름을 제공"
        paths = @(
            "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSample.java",
            "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorController.java",
            "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSampleTest.java",
            "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorControllerTest.java",
            "specs/sql/migration/flyway/V37__official_ref_external_expansion.sql"
        )
    },
    [ordered]@{
        id = "CPF-SERVICE-CALL"
        owner = "CPF"
        description = "Local·Remote 호출의 공통 실행·복원력 계약을 제공"
        paths = @(
            "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java",
            "cpf-core/src/test/java/com/cpf/core/common/servicecall/CpfServiceCallEngineTest.java"
        )
    },
    [ordered]@{
        id = "CPF-TRANSACTION-SEGMENT"
        owner = "CPF"
        description = "부모·자식 거래 구간과 호출 타임라인을 기록"
        paths = @(
            "cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentService.java",
            "cpf-core/src/test/java/com/cpf/core/common/logging/segment/TransactionSegmentServiceTest.java",
            "specs/sql/migration/flyway/V19__transaction_segment_trace.sql"
        )
    }
)

$items = @($capabilities | ForEach-Object {
        $capability = $_
        $checks = @($capability.paths | ForEach-Object {
                $relativePath = [string] $_
                $absolutePath = Join-Path $Root $relativePath
                $exists = Test-Path -LiteralPath $absolutePath -PathType Leaf
                [ordered]@{
                    path = $relativePath
                    exists = $exists
                    sha256 = if ($exists) {
                        (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
                    } else {
                        $null
                    }
                }
            })
        [ordered]@{
            id = $capability.id
            owner = $capability.owner
            description = $capability.description
            status = if (@($checks | Where-Object { -not $_.exists }).Count -eq 0) { "DONE" } else { "FAILED" }
            artifacts = $checks
        }
    })
$missingTargets = @($items | ForEach-Object { $_.artifacts } | Where-Object { -not $_.exists })
$currentCommit = (git -C $Root rev-parse HEAD).Trim()
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = if ($missingTargets.Count -eq 0) { "DONE" } else { "FAILED" }
    currentCommit = $currentCommit
    inventorySource = "current-source"
    capabilityCount = $items.Count
    items = $items
    missingTargetCount = $missingTargets.Count
    note = "과거 Evidence를 입력으로 사용하지 않고 현재 Source·Test·SQL에서 직접 생성했습니다."
}
$resultPath = Join-Path $ResultDir "acc-exs-capability-inventory.sanitized.json"
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)
if ($missingTargets.Count -gt 0) {
    throw "ACC/EXS 기능 inventory 검증이 실패했습니다. missing=$($missingTargets.Count)"
}
Write-Host "ACC/EXS capability inventory export passed. capabilities=$($items.Count)"
