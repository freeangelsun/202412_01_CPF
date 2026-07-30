param(
    [string]$RootPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
)

$ErrorActionPreference = "Stop"

$expectedCommit = "b7c6146e952c10b885952fa2bc6b6786f4611d86"
$baseCommitPath = Join-Path $RootPath "cpf-docs\work\overlay\20260730-readme-guides\BASE_COMMIT.txt"
if (-not (Test-Path -LiteralPath $baseCommitPath)) { throw "BASE_COMMIT.txt가 없습니다." }
$actualCommit = ([System.IO.File]::ReadAllText($baseCommitPath)).Trim()
if ($actualCommit -ne $expectedCommit) { throw "기준 Commit이 다릅니다: $actualCommit" }

$required = @(
    "README.md",
    "cpf-docs\guides\CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md",
    "cpf-docs\guides\CPF_ADMIN_OPERATOR_GUIDE.md",
    "cpf-docs\guides\CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md",
    "cpf-docs\guides\CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md",
    "cpf-docs\guides\CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md",
    "cpf-docs\guides\CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md",
    "cpf-docs\guides\CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md",
    "cpf-docs\guides\CPF_BIZ_ADMIN_GUIDE.md",
    "cpf-docs\guides\CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md",
    "cpf-docs\guides\CPF_DATABASE_TOOL_GUIDE.md",
    "cpf-docs\guides\CPF_DEVELOPER_GUIDE.md",
    "cpf-docs\guides\CPF_EDU_COVERAGE_GUIDE.md",
    "cpf-docs\guides\CPF_FOUNDATION_API_GUIDE.md",
    "cpf-docs\guides\CPF_GATEWAY_OPERATIONS_GUIDE.md",
    "cpf-docs\guides\CPF_GENERATOR_TOOL_GUIDE.md",
    "cpf-docs\guides\CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md",
    "cpf-docs\guides\CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md",
    "cpf-docs\guides\CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md",
    "cpf-docs\guides\CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md",
    "cpf-docs\guides\CPF_SECURITY_DR_RETENTION_GUIDE.md",
    "cpf-docs\guides\CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md",
    "cpf-docs\guides\CPF_TEST_AND_EVIDENCE_GUIDE.md",
    "cpf-docs\guides\CPF_TOOLS_GUIDE.md",
    "cpf-docs\guides\CPF_TOOL_REFERENCE.md",
    "cpf-docs\guides\DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md",
    "cpf-docs\guides\README.md",
    "cpf-docs\assets\readme\cpf-architecture-overview-desktop.png",
    "cpf-docs\assets\readme\cpf-architecture-overview-desktop.svg",
    "cpf-docs\assets\readme\cpf-architecture-overview-mobile.png",
    "cpf-docs\assets\readme\cpf-architecture-overview-mobile.svg",
    "cpf-docs\assets\readme\cpf-domain-journey-desktop.png",
    "cpf-docs\assets\readme\cpf-domain-journey-desktop.svg",
    "cpf-docs\assets\readme\cpf-domain-journey-mobile.png",
    "cpf-docs\assets\readme\cpf-domain-journey-mobile.svg",
    "cpf-docs\assets\readme\cpf-execution-desktop.png",
    "cpf-docs\assets\readme\cpf-execution-desktop.svg",
    "cpf-docs\assets\readme\cpf-execution-mobile.png",
    "cpf-docs\assets\readme\cpf-execution-mobile.svg",
    "cpf-docs\assets\readme\cpf-guide-map-desktop.png",
    "cpf-docs\assets\readme\cpf-guide-map-desktop.svg",
    "cpf-docs\assets\readme\cpf-guide-map-mobile.png",
    "cpf-docs\assets\readme\cpf-guide-map-mobile.svg",
    "cpf-docs\assets\readme\cpf-hero-desktop.png",
    "cpf-docs\assets\readme\cpf-hero-desktop.svg",
    "cpf-docs\assets\readme\cpf-hero-mobile.png",
    "cpf-docs\assets\readme\cpf-hero-mobile.svg",
    "cpf-docs\assets\readme\cpf-operations-desktop.png",
    "cpf-docs\assets\readme\cpf-operations-desktop.svg",
    "cpf-docs\assets\readme\cpf-operations-mobile.png",
    "cpf-docs\assets\readme\cpf-operations-mobile.svg",
    "cpf-docs\assets\readme\cpf-product-map-desktop.png",
    "cpf-docs\assets\readme\cpf-product-map-desktop.svg",
    "cpf-docs\assets\readme\cpf-product-map-mobile.png",
    "cpf-docs\assets\readme\cpf-product-map-mobile.svg",
    "cpf-docs\assets\readme\cpf-topology-desktop.png",
    "cpf-docs\assets\readme\cpf-topology-desktop.svg",
    "cpf-docs\assets\readme\cpf-topology-mobile.png",
    "cpf-docs\assets\readme\cpf-topology-mobile.svg",
    "cpf-docs\assets\readme\cpf-value-pillars-desktop.png",
    "cpf-docs\assets\readme\cpf-value-pillars-desktop.svg",
    "cpf-docs\assets\readme\cpf-value-pillars-mobile.png",
    "cpf-docs\assets\readme\cpf-value-pillars-mobile.svg",
    "cpf-docs\assets\readme\cpf-lifecycle-desktop.png",
    "cpf-docs\assets\readme\cpf-lifecycle-desktop.svg",
    "cpf-docs\assets\readme\cpf-lifecycle-mobile.png",
    "cpf-docs\assets\readme\cpf-lifecycle-mobile.svg"
)

$missing = @()
foreach ($relative in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $RootPath $relative))) { $missing += $relative }
}
if ($missing.Count -gt 0) { throw "필수 파일이 없습니다:`n$($missing -join "`n")" }

$utf8 = New-Object System.Text.UTF8Encoding($false, $true)
$markdown = $required | Where-Object { $_.EndsWith('.md') }
foreach ($relative in $markdown) {
    $path = Join-Path $RootPath $relative
    $bytes = [System.IO.File]::ReadAllBytes($path)
    $null = $utf8.GetString($bytes)
    $content = [System.IO.File]::ReadAllText($path)
    if (-not $content.StartsWith('#') -and -not $content.StartsWith('<div')) { throw "문서 제목이 없습니다: $relative" }
    $fences = ([regex]::Matches($content, '```')).Count
    if (($fences % 2) -ne 0) { throw "코드 블록 구분자가 맞지 않습니다: $relative" }
    $forbiddenPrefix = ([char]0xC0C1).ToString() + ([char]0xC6A9)
    if ($content -match ($forbiddenPrefix + '\s*(업무|플랫폼|Backoffice)')) { throw "금지 표현이 남아 있습니다: $relative" }
    if ($content -match '(?m)[ \t]+$') { throw "문서에 trailing whitespace가 있습니다: $relative" }
    if ($relative -ne "README.md" -and $relative -ne "cpf-docs\guides\README.md") {
        if (-not $content.Contains('## 0. 문서 계약')) { throw "문서 계약이 없습니다: $relative" }
        if (-not $content.Contains('## 부록 Z. 구현 추적 시작점')) { throw "구현 추적 부록이 없습니다: $relative" }
    }
}

$readme = [System.IO.File]::ReadAllText((Join-Path $RootPath "README.md"))
$heroIndex = $readme.IndexOf('cpf-hero-desktop.png')
$architectureIndex = $readme.IndexOf('cpf-architecture-overview-desktop.png')
$valueIndex = $readme.IndexOf('cpf-value-pillars-desktop.png')
$lifecycleIndex = $readme.IndexOf('cpf-lifecycle-desktop.png')
if ($heroIndex -lt 0 -or $architectureIndex -le $heroIndex) { throw "Hero 다음에 CPF 전체 구조도가 배치되지 않았습니다." }
if ($valueIndex -le $architectureIndex -or $lifecycleIndex -le $valueIndex) { throw "README 브로셔 정보 위계가 맞지 않습니다." }
if ($readme -match '상용\s*(업무|플랫폼|Framework|프레임워크)') { throw "사용자가 제외한 상용 표현이 README에 있습니다." }
if ($readme -match '```text\s*┌') { throw "README에 모바일에서 깨지는 가로 ASCII 구조가 있습니다." }

$linkMatches = [regex]::Matches($readme, '\]\((cpf-docs/[^)#]+)(?:#[^)]+)?\)')
$broken = @()
foreach ($match in $linkMatches) {
    $relative = $match.Groups[1].Value.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath (Join-Path $RootPath $relative))) { $broken += $match.Groups[1].Value }
}
if ($broken.Count -gt 0) { throw "README Link 대상이 없습니다:`n$($broken -join "`n")" }

$pictureCount = ([regex]::Matches($readme, '<picture>')).Count
if ($pictureCount -ne 10) { throw "README 시각 자료 수가 다릅니다: picture=$pictureCount" }
if (-not $readme.Contains('max-width: 720px')) { throw "README에 Mobile 이미지 전환이 없습니다." }

$coverage = @{
    "cpf-docs\guides\CPF_GATEWAY_OPERATIONS_GUIDE.md" = @("GATEWAY_E2E", "CpfGatewayPathRewriter", "X-CPF-Gateway-Control-Audience")
    "cpf-docs\guides\CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md" = @("CpfServiceRegistryCatalog", "CpfServiceCallAttempt", "STG")
    "cpf-docs\guides\CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md" = @("adm_log_export_artifact", "최대 크기는 5MB")
    "cpf-docs\guides\CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md" = @("FileProcessHandler", "JcaScriptArtifactVerifier")
    "cpf-docs\guides\CPF_DATABASE_TOOL_GUIDE.md" = @("## 35. V81", "target_path")
}
foreach ($entry in $coverage.GetEnumerator()) {
    $content = [System.IO.File]::ReadAllText((Join-Path $RootPath $entry.Key))
    foreach ($token in $entry.Value) {
        if (-not $content.Contains($token)) { throw "최신 개발 계약이 문서에 없습니다: $($entry.Key) / $token" }
    }
}

Write-Host "CPF 브로셔형 README와 상세 가이드 정적 검증 PASS"
Write-Host "문서: $($markdown.Count), 시각 자료: $($required.Count - $markdown.Count), README picture: $pictureCount"
