param(
    [string]$RootPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
)

$ErrorActionPreference = "Stop"

$required = @(
    "README.md",
    "cpf-docs\guides\CPF_DEVELOPER_GUIDE.md",
    "cpf-docs\guides\CPF_FOUNDATION_API_GUIDE.md",
    "cpf-docs\guides\CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md",
    "cpf-docs\guides\CPF_GENERATOR_TOOL_GUIDE.md",
    "cpf-docs\guides\CPF_ADMIN_OPERATOR_GUIDE.md",
    "cpf-docs\guides\CPF_BIZ_ADMIN_GUIDE.md",
    "cpf-docs\guides\CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md",
    "cpf-docs\guides\CPF_GATEWAY_OPERATIONS_GUIDE.md",
    "cpf-docs\guides\CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md",
    "cpf-docs\guides\CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md",
    "cpf-docs\guides\CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md",
    "cpf-docs\guides\CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md",
    "cpf-docs\guides\CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md",
    "cpf-docs\guides\CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md",
    "cpf-docs\guides\CPF_DATABASE_TOOL_GUIDE.md",
    "cpf-docs\guides\DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md",
    "cpf-docs\guides\CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md",
    "cpf-docs\guides\CPF_SECURITY_DR_RETENTION_GUIDE.md",
    "cpf-docs\guides\CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md",
    "cpf-docs\guides\CPF_TOOLS_GUIDE.md",
    "cpf-docs\guides\CPF_TOOL_REFERENCE.md",
    "cpf-docs\guides\CPF_EDU_COVERAGE_GUIDE.md",
    "cpf-docs\guides\CPF_TEST_AND_EVIDENCE_GUIDE.md"
)

$missing = @()
foreach ($relative in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $RootPath $relative))) {
        $missing += $relative
    }
}
if ($missing.Count -gt 0) {
    throw "필수 문서가 없습니다:`n$($missing -join "`n")"
}

$utf8 = New-Object System.Text.UTF8Encoding($false, $true)
foreach ($relative in $required) {
    $path = Join-Path $RootPath $relative
    $bytes = [System.IO.File]::ReadAllBytes($path)
    $null = $utf8.GetString($bytes)

    $content = [System.IO.File]::ReadAllText($path)
    if (-not $content.StartsWith("#") -and -not $content.StartsWith("<div")) {
        throw "문서 제목이 없습니다: $relative"
    }

    $fences = ([regex]::Matches($content, '```')).Count
    if (($fences % 2) -ne 0) {
        throw "코드 블록 구분자가 맞지 않습니다: $relative"
    }
}

$readme = [System.IO.File]::ReadAllText((Join-Path $RootPath "README.md"))
$linkMatches = [regex]::Matches($readme, '\]\((cpf-docs/[^)#]+\.md)(?:#[^)]+)?\)')
$broken = @()
foreach ($match in $linkMatches) {
    $relative = $match.Groups[1].Value.Replace("/", [System.IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath (Join-Path $RootPath $relative))) {
        $broken += $match.Groups[1].Value
    }
}
if ($broken.Count -gt 0) {
    throw "README 문서 Link 대상이 없습니다:`n$($broken -join "`n")"
}

Write-Host "README와 가이드 문서 정적 검증 PASS"
Write-Host "검증 파일 수: $($required.Count)"
