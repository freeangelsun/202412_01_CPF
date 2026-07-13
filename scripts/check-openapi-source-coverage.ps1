param(
    [string]$Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string]$ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260713_02")
)

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$moduleCodes = @("acc", "adm", "bat", "bizadm", "exs", "mbr", "xyz")
$controllerFiles = New-Object System.Collections.Generic.List[System.IO.FileInfo]

foreach ($moduleCode in $moduleCodes) {
    $sourceRoot = Join-Path $Root "$moduleCode/src/main/java"
    if (Test-Path -LiteralPath $sourceRoot) {
        Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*Controller.java" |
            ForEach-Object { $controllerFiles.Add($_) | Out-Null }
    }
}

$mappingCount = 0
$explicitIds = New-Object System.Collections.Generic.List[object]
foreach ($file in $controllerFiles) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $mappingCount += [regex]::Matches($content, '@(?:Get|Post|Put|Delete|Patch|Request)Mapping\s*\(').Count
    foreach ($match in [regex]::Matches($content, 'operationId\s*=\s*"([^"]+)"')) {
        $explicitIds.Add([pscustomobject]@{
            operationId = $match.Groups[1].Value
            file = $file.FullName.Substring($Root.Length + 1).Replace("\", "/")
        }) | Out-Null
    }
}

if ($controllerFiles.Count -eq 0 -or $mappingCount -eq 0) {
    $failures.Add("Controller 또는 API mapping을 찾지 못했습니다.") | Out-Null
}

$duplicates = @($explicitIds | Group-Object operationId | Where-Object { $_.Count -gt 1 })
foreach ($duplicate in $duplicates) {
    $failures.Add("중복 operationId: $($duplicate.Name)") | Out-Null
}

$moduleDependency = [ordered]@{}
foreach ($moduleCode in $moduleCodes) {
    $buildPath = Join-Path $Root "$moduleCode/build.gradle"
    $hasPfw = (Test-Path -LiteralPath $buildPath -PathType Leaf) -and
        ([System.IO.File]::ReadAllText($buildPath, [System.Text.Encoding]::UTF8).Contains("project(':pfw')"))
    $moduleDependency[$moduleCode] = $hasPfw
    if (-not $hasPfw) {
        $failures.Add("$moduleCode 모듈이 PFW OpenAPI 자동 설정을 의존하지 않습니다.") | Out-Null
    }
}

$configurationPath = Join-Path $Root "pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java"
$configuration = [System.IO.File]::ReadAllText($configurationPath, [System.Text.Encoding]::UTF8)
foreach ($marker in @(
    "cpfOperationIdCustomizer",
    "generatedOperationId",
    "X-Transaction-Id",
    "X-Request-Type",
    "X-Original-Channel-Code",
    "X-Channel-Code"
)) {
    if (-not $configuration.Contains($marker)) {
        $failures.Add("PFW OpenAPI 자동 설정 누락: $marker") | Out-Null
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    coverageMode = "명시 operationId 또는 PFW 결정적 자동 생성"
    controllerCount = $controllerFiles.Count
    mappingCount = $mappingCount
    explicitOperationIdCount = $explicitIds.Count
    duplicateOperationIdCount = $duplicates.Count
    modulePfwDependency = $moduleDependency
    runtimeOpenApiJsonVerified = $false
    failures = $failures
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "openapi-source-coverage.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    (New-Object System.Text.UTF8Encoding($false)))

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "OpenAPI source coverage check passed."
