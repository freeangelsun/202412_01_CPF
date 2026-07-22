param(
    [string] $Root = "",
    [Parameter(Mandatory = $true)]
    [string] $ResultDir
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Root = if ([string]::IsNullOrWhiteSpace($Root)) {
    (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    (Resolve-Path $Root).Path
}
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$TargetPath = Join-Path $Root "CPF_FINAL_TARGET_REQUIREMENTS.md"
$RequestPath = Join-Path $Root "CPF_CURRENT_WORK_REQUEST.md"
$MatrixPath = Join-Path $Root "specs/generated/feature-implementation-matrix.json"
$InventoryPath = Join-Path $ResultDir "cpf-inventory.sanitized.json"

foreach ($requiredPath in @($TargetPath, $RequestPath, $MatrixPath, $InventoryPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath -PathType Leaf)) {
        throw "요구사항 추적성 입력 파일이 없습니다: $requiredPath"
    }
}

function Get-DomainCapabilities {
    param([string] $DomainId)

    switch -Regex ($DomainId) {
        '^(ARCH|FACADE)-' { return @("application-core", "service-call", "documentation") }
        '^CPF-(CALL|REGISTRY|ROUTING|HEALTH|RESILIENCE|DEADLINE)' { return @("service-call") }
        '^CPF-(HEADER|CONTEXT|TXID|ROLE)' { return @("transaction-header") }
        '^CPF-(OPSDB|LOGDB|FILELOG|LOGFAIL|TRACE)' { return @("logging-recovery", "database") }
        '^CPF-MASK' { return @("security", "logging-recovery") }
        '^CPF-(ERROR|VALID|STATE|LOCK|SCHED)' { return @("application-core", "batch-center-cut") }
        '^CPF-IDEMP' { return @("idempotency") }
        '^CMN-(CODE|MSG|TEMPLATE)' { return @("code-message-config") }
        '^CMN-ID' { return @("idempotency", "application-core") }
        '^CMN-FILE' { return @("file-transfer", "archive") }
        '^CMN-FIXED' { return @("fixed-length") }
        '^CMN-CALENDAR' { return @("batch-center-cut", "code-message-config") }
        '^ADM-(AUTH|RBAC|AUDIT)' { return @("security", "application-core") }
        '^ADM-(BATCH|CENTER)' { return @("batch-center-cut") }
        '^ADM-(TX|TIMELINE|LOG)' { return @("transaction-header", "logging-recovery") }
        '^ADM-(SERVICE|EXS)' { return @("service-call", "unknown-reconciliation") }
        '^ADM-(COMP|INCIDENT|UX)' { return @("operations", "application-core") }
        '^BAT-' { return @("batch-center-cut", "service-call", "broker-event") }
        '^CENTER-' { return @("batch-center-cut") }
        '^EXS-(REST|INST)' { return @("service-call") }
        '^EXS-FIXED' { return @("fixed-length") }
        '^EXS-SEC' { return @("security") }
        '^EXS-(UNKNOWN|RECON)' { return @("unknown-reconciliation") }
        '^EXS-FILE' { return @("file-transfer") }
        '^EVENT-' { return @("broker-event") }
        '^SAGA-' { return @("unknown-reconciliation", "broker-event") }
        '^SEC-' { return @("security") }
        '^OPS-' { return @("operations", "logging-recovery", "service-call") }
        '^REL-' { return @("documentation", "database", "application-core") }
        '^DB-' { return @("database") }
        '^DATA-' { return @("database", "unknown-reconciliation") }
        '^API-(CONTRACT|GATEWAY)' { return @("openapi", "service-call") }
        '^API-LIMIT' { return @("api-governance", "security") }
        '^API-PAGING' { return @("api-governance", "application-core") }
        '^SAMPLE-' { return @("education") }
        '^ONBOARD-' { return @("domain-generator", "education") }
        '^DEVEX-' { return @("domain-generator", "documentation", "education") }
        '^RULE-' { return @("documentation", "security", "application-core") }
        '^TEST-' { return @("documentation", "education", "application-core") }
        '^DOC-' { return @("documentation") }
        '^PROD-' { return @("product-governance", "documentation") }
        '^REQ-' { return @("documentation") }
        default { return @("application-core") }
    }
}

function Convert-ModuleNames {
    param([string] $Value)

    $result = New-Object System.Collections.Generic.List[string]
    foreach ($moduleName in @($Value -split ',' | ForEach-Object { $_.Trim().ToUpperInvariant() })) {
        if ([string]::IsNullOrWhiteSpace($moduleName)) { continue }
        $normalized = switch ($moduleName) {
            "BIZADM" { "BZA" }
            "EDU" { "REF" }
            "EXS" { "ACC" }
            "전체" { "ROOT" }
            "전체 모듈" { "ROOT" }
            default { $moduleName }
        }
        if (-not $result.Contains($normalized)) { [void]$result.Add($normalized) }
    }
    return @($result)
}

function Get-UniquePaths {
    param(
        [object[]] $Assets,
        [string[]] $Types,
        [int] $Limit = 16
    )

    return @($Assets |
        Where-Object { $Types -contains $_.type } |
        Sort-Object @{ Expression = "score"; Descending = $true }, path |
        Select-Object -ExpandProperty path -Unique |
        Select-Object -First $Limit)
}

# 활성 요청서의 133개 도메인 상태와 검토 시작 경로를 구조화합니다.
$requestLines = [System.IO.File]::ReadAllLines($RequestPath, [System.Text.Encoding]::UTF8)
$requestDomains = [ordered]@{}
$currentDomain = $null
$collectExamples = $false
foreach ($line in $requestLines) {
    if ($line -match '^## 8\.\d{3}\. `(?<id>[^`]+)` — (?<title>.+)$') {
        $currentDomain = [ordered]@{
            domainId = $Matches.id
            title = $Matches.title.Trim()
            reviewStatus = "재확인 필요"
            startPaths = New-Object System.Collections.Generic.List[string]
        }
        $requestDomains[$Matches.id] = $currentDomain
        $collectExamples = $false
        continue
    }
    if ($null -eq $currentDomain) { continue }
    if ($line -match '^\*\*현재 판정:\*\* `(?<status>[^`]+)`') {
        $currentDomain.reviewStatus = $Matches.status
        continue
    }
    if ($line -eq '확인 시작 파일:') {
        $collectExamples = $true
        continue
    }
    if ($collectExamples -and $line -match '^- `(?<path>[^`]+)`') {
        [void]$currentDomain.startPaths.Add($Matches.path.Replace('\', '/'))
        continue
    }
    if ($collectExamples -and $line -notmatch '^-' -and -not [string]::IsNullOrWhiteSpace($line)) {
        $collectExamples = $false
    }
}

# 최종 목표의 모든 상세 카드를 REQ-ID 단위로 추출합니다.
$targetLines = [System.IO.File]::ReadAllLines($TargetPath, [System.Text.Encoding]::UTF8)
$requirements = New-Object System.Collections.Generic.List[object]
$active = $null
foreach ($line in $targetLines) {
    if ($line -match '^## (?<id>[A-Z][A-Z0-9-]+-\d{3})\.\s*(?<title>.+)$') {
        if ($null -ne $active) { [void]$requirements.Add([pscustomobject]$active) }
        $requirementId = $Matches.id
        $active = [ordered]@{
            requirementId = $requirementId
            domainId = ($requirementId -replace '-\d{3}$', '')
            title = $Matches.title.Trim()
            priority = ""
            classification = ""
            checkpointName = ""
            purpose = ""
            modules = ""
            design = ""
            developmentLink = ""
            configuration = ""
            logging = ""
            verificationLevel = ""
            evidenceCriteria = ""
            completionCriteria = ""
            rejectionCriteria = ""
            requestInstruction = ""
        }
        continue
    }
    if ($null -eq $active) { continue }
    if ($line -match '^- (?<name>[^:]+):\s*(?<value>.*)$') {
        $name = $Matches.name.Trim()
        $value = $Matches.value.Trim()
        switch ($name) {
            "Priority" { $active.priority = $value }
            "분류" { $active.classification = $value }
            "기능/체크포인트명" { $active.checkpointName = $value }
            "목적" { $active.purpose = $value }
            "적용 모듈" { $active.modules = $value }
            "설계/구현 기준" { $active.design = $value }
            "개발 연결" { $active.developmentLink = $value }
            "설정/DB/API/ADM 기준" { $active.configuration = $value }
            "로그/감사/마스킹 기준" { $active.logging = $value }
            "검증 수준" { $active.verificationLevel = $value }
            "Evidence 기준" { $active.evidenceCriteria = $value }
            "완료 기준" { $active.completionCriteria = $value }
            "완료 불인정" { $active.rejectionCriteria = $value }
            "Codex 요청서 반영" { $active.requestInstruction = $value }
        }
    }
}
if ($null -ne $active) { [void]$requirements.Add([pscustomobject]$active) }

$inventory = Get-Content -LiteralPath $InventoryPath -Encoding UTF8 -Raw | ConvertFrom-Json
$matrix = Get-Content -LiteralPath $MatrixPath -Encoding UTF8 -Raw | ConvertFrom-Json
$assets = @($inventory.assets)
$requirementsByDomain = @{}
foreach ($requirement in $requirements) {
    if (-not $requirementsByDomain.ContainsKey($requirement.domainId)) {
        $requirementsByDomain[$requirement.domainId] = New-Object System.Collections.Generic.List[object]
    }
    [void]$requirementsByDomain[$requirement.domainId].Add($requirement)
}
$domainTraces = New-Object System.Collections.Generic.List[object]
$reverseIndex = @{}

foreach ($domainEntry in $requestDomains.GetEnumerator()) {
    $domain = $domainEntry.Value
    $domainRequirements = if ($requirementsByDomain.ContainsKey($domain.domainId)) {
        @($requirementsByDomain[$domain.domainId].ToArray())
    } else { @() }
    $modules = if ($domainRequirements.Count -gt 0) {
        Convert-ModuleNames $domainRequirements[0].modules
    } else { @() }
    $capabilities = @(Get-DomainCapabilities $domain.domainId)
    $tokens = New-Object System.Collections.Generic.List[string]
    foreach ($token in @($domain.domainId -split '-')) {
        $normalized = $token.ToLowerInvariant()
        if ($normalized.Length -ge 3 -and $normalized -notin @('cpf', 'cmn', 'adm', 'bat', 'ops', 'rel', 'req', 'sec', 'api', 'db')) {
            [void]$tokens.Add($normalized)
        }
    }
    foreach ($match in [regex]::Matches($domain.title, '[A-Za-z][A-Za-z0-9]+')) {
        $normalized = $match.Value.ToLowerInvariant()
        if ($normalized.Length -ge 4 -and $normalized -notin @('core', 'standard', 'management', 'framework')) {
            if (-not $tokens.Contains($normalized)) { [void]$tokens.Add($normalized) }
        }
    }

    $ranked = New-Object System.Collections.Generic.List[object]
    foreach ($asset in $assets) {
        $score = 0
        $path = [string]$asset.path
        $searchText = ($path + ' ' + [string]$asset.name + ' ' + [string]$asset.detail).ToLowerInvariant()
        if ($domain.startPaths -contains $path) { $score += 1000 }
        if ($capabilities -contains [string]$asset.capability) { $score += 120 }
        if ($modules -contains [string]$asset.owner) { $score += 25 }
        foreach ($token in $tokens) {
            if ($searchText.Contains($token)) { $score += 55 }
        }
        if ($score -ge 120 -or ($score -ge 55 -and ($modules.Count -eq 0 -or $modules -contains [string]$asset.owner))) {
            [void]$ranked.Add([pscustomobject]@{
                type = [string]$asset.type
                owner = [string]$asset.owner
                capability = [string]$asset.capability
                path = $path
                name = [string]$asset.name
                consumer = if ([string]$asset.owner -in @("CPF", "ROOT")) { "프레임워크 또는 정본" } else { "업무 주제영역" }
                score = $score
            })
        }
    }

    foreach ($startPath in $domain.startPaths) {
        if (-not ($ranked.path -contains $startPath) -and (Test-Path -LiteralPath (Join-Path $Root $startPath))) {
            [void]$ranked.Add([pscustomobject]@{
                type = "request-start-path"
                owner = (($startPath -split '/')[0]).ToUpperInvariant()
                capability = "request-signal"
                path = $startPath
                name = [System.IO.Path]::GetFileName($startPath)
                consumer = "요청서 검토 시작점"
                score = 1000
            })
        }
    }

    $sourcePaths = Get-UniquePaths $ranked @("java-public-type", "public-port", "controller", "service", "repository", "mapper", "engine-facade-adapter", "education-source", "java-package", "request-start-path") 24
    $testPaths = Get-UniquePaths $ranked @("test-source") 20
    $sqlPaths = Get-UniquePaths $ranked @("sql-table", "sql-index", "sql-file", "flyway-migration") 16
    $apiPaths = Get-UniquePaths $ranked @("api-mapping") 16
    $configPaths = Get-UniquePaths $ranked @("runtime-configuration") 12
    $scriptPaths = Get-UniquePaths $ranked @("script") 12
    $documentPaths = Get-UniquePaths $ranked @("document") 8
    $matrixItems = @($matrix.items | Where-Object { $_.requirementDomain -eq $domain.domainId })
    $evidencePaths = @($matrixItems.evidence -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ -and $_ -ne "없음" } | Sort-Object -Unique)

    $allPaths = @($sourcePaths + $testPaths + $sqlPaths + $apiPaths + $configPaths + $scriptPaths + $documentPaths | Sort-Object -Unique)
    foreach ($path in $allPaths) {
        if (-not $reverseIndex.ContainsKey($path)) {
            $matchedAsset = $ranked | Where-Object { $_.path -eq $path } | Select-Object -First 1
            $reverseIndex[$path] = [ordered]@{
                path = $path
                owner = if ($matchedAsset) { $matchedAsset.owner } else { "ROOT" }
                type = if ($matchedAsset) { $matchedAsset.type } else { "request-start-path" }
                consumer = if ($matchedAsset) { $matchedAsset.consumer } else { "요청서 검토 시작점" }
                requirementDomains = New-Object System.Collections.Generic.List[string]
            }
        }
        if (-not $reverseIndex[$path].requirementDomains.Contains($domain.domainId)) {
            [void]$reverseIndex[$path].requirementDomains.Add($domain.domainId)
        }
    }

    $missingSignals = New-Object System.Collections.Generic.List[string]
    if ($sourcePaths.Count -eq 0) { [void]$missingSignals.Add("source") }
    if ($domain.reviewStatus -in @("완료", "재확인 필요") -and $testPaths.Count -eq 0) { [void]$missingSignals.Add("test") }
    if ($domain.reviewStatus -eq "완료" -and $evidencePaths.Count -eq 0) { [void]$missingSignals.Add("evidence") }

    [void]$domainTraces.Add([pscustomobject]@{
        domainId = $domain.domainId
        title = $domain.title
        reviewStatus = $domain.reviewStatus
        requirementCount = $domainRequirements.Count
        requirementIds = @($domainRequirements.requirementId)
        modules = $modules
        capabilities = $capabilities
        sourcePaths = $sourcePaths
        testPaths = $testPaths
        sqlPaths = $sqlPaths
        apiPaths = $apiPaths
        configPaths = $configPaths
        scriptPaths = $scriptPaths
        documentPaths = $documentPaths
        runtimeEvidencePaths = $evidencePaths
        consumerOwners = @($ranked | Where-Object { $_.owner -notin @("CPF", "ROOT") } | Select-Object -ExpandProperty owner -Unique | Sort-Object)
        missingSignals = @($missingSignals.ToArray())
    })
}

$duplicateRequirements = @($requirements | Group-Object requirementId | Where-Object { $_.Count -gt 1 })
$missingFieldCount = 0
foreach ($requirement in $requirements) {
    foreach ($field in @("priority", "classification", "purpose", "modules", "design", "developmentLink",
            "verificationLevel", "evidenceCriteria", "completionCriteria", "rejectionCriteria")) {
        if ([string]::IsNullOrWhiteSpace([string]$requirement.$field)) { $missingFieldCount++ }
    }
}
$untracedDomains = @($domainTraces | Where-Object {
    ($_.sourcePaths.Count + $_.testPaths.Count + $_.sqlPaths.Count + $_.apiPaths.Count +
        $_.configPaths.Count + $_.scriptPaths.Count + $_.documentPaths.Count) -eq 0
})
$status = if ($requestDomains.Count -eq 133 -and $requirements.Count -eq 13300 -and
        $duplicateRequirements.Count -eq 0 -and $missingFieldCount -eq 0 -and $untracedDomains.Count -eq 0) {
    "DONE"
} else { "FAILED" }

$head = (& git -C $Root rev-parse HEAD).Trim()
$requestHash = (Get-FileHash -LiteralPath $RequestPath -Algorithm SHA256).Hash.ToLowerInvariant()
$targetHash = (Get-FileHash -LiteralPath $TargetPath -Algorithm SHA256).Hash.ToLowerInvariant()
$requirementInventory = [ordered]@{
    schemaVersion = 1
    generatedAt = (Get-Date).ToString("o")
    status = $status
    basisSha = $head
    requestSha256 = $requestHash
    targetSha256 = $targetHash
    domainCount = $requestDomains.Count
    requirementCount = $requirements.Count
    duplicateRequirementCount = $duplicateRequirements.Count
    missingFieldCount = $missingFieldCount
    countsByPriority = [ordered]@{}
    countsByClassification = [ordered]@{}
    requirements = @($requirements.ToArray())
}
foreach ($group in ($requirements | Group-Object priority | Sort-Object Name)) {
    $requirementInventory.countsByPriority[$group.Name] = $group.Count
}
foreach ($group in ($requirements | Group-Object classification | Sort-Object Name)) {
    $requirementInventory.countsByClassification[$group.Name] = $group.Count
}

$traceability = [ordered]@{
    schemaVersion = 1
    generatedAt = (Get-Date).ToString("o")
    status = $status
    basisSha = $head
    requestSha256 = $requestHash
    targetSha256 = $targetHash
    domainCount = $domainTraces.Count
    requirementCount = $requirements.Count
    implementationAssetCount = $reverseIndex.Count
    untracedDomainCount = $untracedDomains.Count
    domains = @($domainTraces.ToArray())
    implementationToRequirements = @($reverseIndex.Values | ForEach-Object {
        [pscustomobject]@{
            path = $_.path
            owner = $_.owner
            type = $_.type
            consumer = $_.consumer
            requirementDomains = @($_.requirementDomains | Sort-Object)
        }
    } | Sort-Object path)
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
[System.IO.File]::WriteAllText(
    (Join-Path $ResultDir "requirement-inventory.sanitized.json"),
    (($requirementInventory | ConvertTo-Json -Depth 12) + "`n"),
    $Utf8NoBom)
[System.IO.File]::WriteAllText(
    (Join-Path $ResultDir "requirement-traceability.sanitized.json"),
    (($traceability | ConvertTo-Json -Depth 12) + "`n"),
    $Utf8NoBom)

$result = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    status = $status
    basisSha = $head
    domainCount = $domainTraces.Count
    requirementCount = $requirements.Count
    implementationAssetCount = $reverseIndex.Count
    duplicateRequirementCount = $duplicateRequirements.Count
    missingFieldCount = $missingFieldCount
    untracedDomainCount = $untracedDomains.Count
    inventoryPath = "$($ResultDir.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/'))/requirement-inventory.sanitized.json"
    traceabilityPath = "$($ResultDir.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/'))/requirement-traceability.sanitized.json"
}
[System.IO.File]::WriteAllText(
    (Join-Path $ResultDir "requirement-traceability-result.sanitized.json"),
    (($result | ConvertTo-Json -Depth 6) + "`n"),
    $Utf8NoBom)

if ($status -ne "DONE") {
    throw "요구사항 추적성 생성 실패: domains=$($requestDomains.Count), requirements=$($requirements.Count), duplicates=$($duplicateRequirements.Count), missingFields=$missingFieldCount, untracedDomains=$($untracedDomains.Count)"
}

Write-Host "요구사항 추적성 생성 완료: domains=$($domainTraces.Count), requirements=$($requirements.Count), assets=$($reverseIndex.Count)"
