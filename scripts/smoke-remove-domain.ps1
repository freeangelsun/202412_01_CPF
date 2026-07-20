param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$sandbox = Join-Path $Root "build/remove-domain-verification"
$resultPath = Join-Path $ResultDir "remove-domain-smoke.sanitized.json"

function Invoke-Tool {
    param([string] $Script, [string[]] $Arguments)
    $output = & powershell -NoProfile -ExecutionPolicy Bypass -File $Script @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "$Script 실행에 실패했습니다. exitCode=$LASTEXITCODE output=$($output -join ' ')"
    }
    return $output
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = "FAILED"
    moduleCode = "LNG"
    cleanDryRun = $null
    changedFileDryRun = $null
    actualRemove = $null
}

try {
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $sandbox 'scripts') | Out-Null
    Copy-Item -LiteralPath (Join-Path $Root 'scripts/create-domain.ps1') -Destination (Join-Path $sandbox 'scripts/create-domain.ps1')
    Copy-Item -LiteralPath (Join-Path $Root 'scripts/remove-domain.ps1') -Destination (Join-Path $sandbox 'scripts/remove-domain.ps1')
    [System.IO.File]::WriteAllText(
            (Join-Path $sandbox 'settings.gradle'),
            "rootProject.name = 'cpf-remove-domain-smoke'`r`n",
            $Utf8NoBom)

    $createArgs = @(
        '-Root', $sandbox,
        '-ModuleCode', 'lng',
        '-ModuleName', 'Lending',
        '-DomainIdCode', 'LNG',
        '-BasePackage', 'cpf.lng',
        '-TablePrefix', 'lng',
        '-Port', '8188',
        '-Online', 'Y',
        '-Batch', 'Y',
        '-Apply'
    )
    [void] (Invoke-Tool -Script (Join-Path $sandbox 'scripts/create-domain.ps1') -Arguments $createArgs)

    $cleanDir = Join-Path $sandbox 'build/remove-domain-clean'
    [void] (Invoke-Tool -Script (Join-Path $sandbox 'scripts/remove-domain.ps1') -Arguments @(
            '-Root', $sandbox, '-ModuleCode', 'lng', '-DryRun', '-ResultDir', $cleanDir))
    $cleanResult = Get-Content -LiteralPath (Join-Path $cleanDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($cleanResult.status -ne 'READY') {
        throw "변경 없는 생성 모듈의 제거 사전 점검이 READY가 아닙니다."
    }
    $result.cleanDryRun = $cleanResult

    $ownedFile = Join-Path $sandbox 'lng/src/main/java/cpf/lng/LngApplication.java'
    Add-Content -LiteralPath $ownedFile -Value "// 사용자 변경 검증" -Encoding UTF8
    $changedDir = Join-Path $sandbox 'build/remove-domain-changed'
    [void] (Invoke-Tool -Script (Join-Path $sandbox 'scripts/remove-domain.ps1') -Arguments @(
            '-Root', $sandbox, '-ModuleCode', 'lng', '-DryRun', '-ResultDir', $changedDir))
    $changedResult = Get-Content -LiteralPath (Join-Path $changedDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($changedResult.status -ne 'BLOCKED' -or @($changedResult.changedGeneratedFiles).Count -lt 1) {
        throw "사용자 변경 파일을 제거 차단 사유로 탐지하지 못했습니다."
    }
    $result.changedFileDryRun = $changedResult

    # 원래 checksum으로 되돌린 뒤 실제 제거와 settings 정리를 검증합니다.
    [void] (Invoke-Tool -Script (Join-Path $sandbox 'scripts/create-domain.ps1') -Arguments @(
            '-Root', $sandbox, '-ModuleCode', 'tmp', '-ModuleName', 'Temporary', '-DomainIdCode', 'TMP',
            '-BasePackage', 'cpf.tmp', '-TablePrefix', 'tmp', '-Port', '8189', '-Online', 'Y', '-Batch', 'N', '-Apply'))
    $actualDir = Join-Path $sandbox 'build/remove-domain-actual'
    [void] (Invoke-Tool -Script (Join-Path $sandbox 'scripts/remove-domain.ps1') -Arguments @(
            '-Root', $sandbox, '-ModuleCode', 'tmp', '-ResultDir', $actualDir))
    $actualResult = Get-Content -LiteralPath (Join-Path $actualDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    $settingsText = Get-Content -LiteralPath (Join-Path $sandbox 'settings.gradle') -Raw -Encoding UTF8
    if ($actualResult.status -ne 'DONE' -or (Test-Path -LiteralPath (Join-Path $sandbox 'tmp')) -or $settingsText -match "include 'tmp'") {
        throw "실제 제거 후 모듈 또는 settings 참조가 남았습니다."
    }
    $result.actualRemove = $actualResult
    $result.status = "DONE"
} finally {
    $result.endedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}

Write-Host "remove-domain smoke passed. evidence=$resultPath"
