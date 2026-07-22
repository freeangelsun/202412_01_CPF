param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$moduleRules = [ordered]@{
    "cpf-account" = [ordered]@{ controller = "AccBaseController"; service = "AccBaseService" }
    "cpf-admin" = [ordered]@{ controller = "AdmBaseController"; service = "AdmBaseService" }
    "cpf-batch" = [ordered]@{ controller = "BatBaseController"; service = "BatBaseService" }
    "cpf-biz-admin" = [ordered]@{ controller = "BzaBaseController"; service = "BzaBaseService" }
    "cpf-common" = [ordered]@{ controller = $null; service = "CmnBaseService" }
    "cpf-member" = [ordered]@{ controller = "MbrBaseController"; service = "MbrBaseService" }
    "cpf-reference" = [ordered]@{ controller = "ReferenceBaseController"; service = "ReferenceBaseService" }
    "cpf-external" = [ordered]@{ controller = "ExternalBaseController"; service = "ExternalBaseService" }
}

$checked = New-Object System.Collections.Generic.List[object]
$failures = New-Object System.Collections.Generic.List[object]

foreach ($module in $moduleRules.Keys) {
    $sourceRoot = Join-Path $Root "$module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        $failures.Add([ordered]@{ module = $module; path = $sourceRoot; reason = "main Java source 경로가 없습니다." })
        continue
    }

    foreach ($file in (Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java")) {
        $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        if ($text -match '(?m)^\s*(public\s+)?abstract\s+class\s+') {
            continue
        }

        $relativePath = $file.FullName.Substring($Root.Length + 1).Replace("\", "/")
        foreach ($kind in @("controller", "service")) {
            $suffix = if ($kind -eq "controller") { "Controller" } else { "Service" }
            $baseClass = $moduleRules[$module][$kind]
            if ($null -eq $baseClass) {
                continue
            }
            $declarationPattern = '(?m)^\s*(?:public\s+)?(?:final\s+)?class\s+(\w+' + [regex]::Escape($suffix) + ')\b([^\r\n{]*)'
            $declaration = [regex]::Match($text, $declarationPattern)
            if (-not $declaration.Success) {
                continue
            }

            $className = $declaration.Groups[1].Value
            $tail = $declaration.Groups[2].Value
            $basePattern = '\bextends\s+(?:[a-zA-Z0-9_.]+\.)?' + [regex]::Escape($baseClass) + '\b'
            $passed = $tail -match $basePattern
            $checked.Add([ordered]@{
                module = $module
                kind = $kind
                path = $relativePath
                className = $className
                expectedBase = $baseClass
                passed = $passed
            })
            if (-not $passed) {
                $failures.Add([ordered]@{
                    module = $module
                    path = $relativePath
                    className = $className
                    reason = "$className 클래스가 $baseClass 를 상속하지 않습니다."
                })
            }
        }
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    checkedCount = $checked.Count
    failureCount = $failures.Count
    checked = $checked
    failures = $failures
}
$jsonPath = Join-Path $ResultDir "base-hierarchy.sanitized.json"
[System.IO.File]::WriteAllText(
    $jsonPath,
    ($result | ConvertTo-Json -Depth 10),
    [System.Text.UTF8Encoding]::new($false)
)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host ("{0}: {1}" -f $_.path, $_.reason) }
    exit 1
}

Write-Host "Base hierarchy check passed: $($checked.Count) classes."
