param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke"),
    [switch] $Fix
)

$ErrorActionPreference = "Stop"
$moduleCodes = @("acc", "adm", "bat", "bizadm", "exs", "mbr", "xyz")
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Get-AnnotationRanges {
    param([string] $Text, [string] $Pattern)
    $ranges = New-Object System.Collections.Generic.List[object]
    foreach ($match in [regex]::Matches($Text, $Pattern)) {
        $open = $Text.IndexOf('(', $match.Index)
        if ($open -lt 0) {
            continue
        }
        $depth = 0
        $quote = [char]0
        $escaped = $false
        for ($index = $open; $index -lt $Text.Length; $index++) {
            $character = $Text[$index]
            if ($quote -ne [char]0) {
                if ($escaped) {
                    $escaped = $false
                } elseif ($character -eq '\') {
                    $escaped = $true
                } elseif ($character -eq $quote) {
                    $quote = [char]0
                }
                continue
            }
            if ($character -eq '"' -or $character -eq "'") {
                $quote = $character
                continue
            }
            if ($character -eq '(') {
                $depth++
            } elseif ($character -eq ')') {
                $depth--
                if ($depth -eq 0) {
                    $ranges.Add([pscustomobject]@{
                        start = $match.Index
                        open = $open
                        end = $index + 1
                        text = $Text.Substring($match.Index, $index + 1 - $match.Index)
                    }) | Out-Null
                    break
                }
            }
        }
    }
    return $ranges.ToArray()
}

function Find-NextPublicMethod {
    param([string] $Text, [int] $Start)
    if ($Start -ge $Text.Length) {
        return $null
    }
    $tail = $Text.Substring($Start, [Math]::Min(12000, $Text.Length - $Start))
    $match = [regex]::Match(
        $tail,
        '(?m)^[ \t]*public[ \t]+(?:<[^>]+>[ \t]+)?[A-Za-z0-9_$.?<>,\[\] ]+[ \t]+(?<name>[A-Za-z_][A-Za-z0-9_]*)[ \t]*\(')
    if (-not $match.Success) {
        return $null
    }
    return [pscustomobject]@{
        index = $Start + $match.Index
        name = $match.Groups['name'].Value
    }
}

function Get-OperationIdBase {
    param([string] $ModuleCode, [string] $ClassName, [string] $MethodName)
    $classPart = $ClassName -replace 'Controller$', ''
    $moduleTitle = (Get-Culture).TextInfo.ToTitleCase($ModuleCode.ToLowerInvariant())
    if ($classPart.StartsWith($moduleTitle, [StringComparison]::OrdinalIgnoreCase)) {
        $classPart = $classPart.Substring($moduleTitle.Length)
    }
    if ([string]::IsNullOrWhiteSpace($classPart)) {
        $classPart = "Api"
    }
    $methodPart = $MethodName.Substring(0, 1).ToUpperInvariant() + $MethodName.Substring(1)
    return $ModuleCode.ToLowerInvariant() + $classPart + $methodPart
}

function New-UniqueOperationId {
    param([string] $Base, [System.Collections.Generic.HashSet[string]] $Used)
    $candidate = $Base
    $suffix = 2
    while (-not $Used.Add($candidate)) {
        $candidate = $Base + $suffix
        $suffix++
    }
    return $candidate
}

function Get-MethodMappings {
    param([string] $Text)
    $mappingRanges = Get-AnnotationRanges $Text '@(?:Get|Post|Put|Delete|Patch|Request)Mapping\s*\('
    $result = New-Object System.Collections.Generic.List[object]
    foreach ($mapping in $mappingRanges) {
        $method = Find-NextPublicMethod $Text $mapping.end
        if ($null -eq $method) {
            continue
        }
        $between = $Text.Substring($mapping.end, $method.index - $mapping.end)
        if ($between -match '(?m)\b(class|interface|record)\s+[A-Za-z_][A-Za-z0-9_]*') {
            continue
        }
        $result.Add([pscustomobject]@{
            mapping = $mapping
            method = $method
        }) | Out-Null
    }
    return $result.ToArray()
}

$controllerFiles = New-Object System.Collections.Generic.List[object]
foreach ($moduleCode in $moduleCodes) {
    $sourceRoot = Join-Path $Root "$moduleCode/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        continue
    }
    Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*Controller.java" | ForEach-Object {
        $controllerFiles.Add([pscustomobject]@{ module = $moduleCode; file = $_ }) | Out-Null
    }
}

$usedIds = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
foreach ($entry in $controllerFiles) {
    $text = [System.IO.File]::ReadAllText($entry.file.FullName, [System.Text.Encoding]::UTF8)
    foreach ($match in [regex]::Matches($text, 'operationId\s*=\s*"([^"]+)"')) {
        $null = $usedIds.Add($match.Groups[1].Value)
    }
}

$modifiedFiles = New-Object System.Collections.Generic.List[string]
if ($Fix) {
    foreach ($entry in $controllerFiles) {
        $path = $entry.file.FullName
        $className = $entry.file.BaseName
        $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        $changes = New-Object System.Collections.Generic.List[object]
        foreach ($operation in (Get-AnnotationRanges $text '@Operation\s*\(')) {
            if ($operation.text -match 'operationId\s*=') {
                continue
            }
            $method = Find-NextPublicMethod $text $operation.end
            if ($null -eq $method) {
                continue
            }
            $base = Get-OperationIdBase $entry.module $className $method.name
            $operationId = New-UniqueOperationId $base $usedIds
            $changes.Add([pscustomobject]@{
                index = $operation.open + 1
                value = "operationId = `"$operationId`", "
            }) | Out-Null
        }
        foreach ($change in @($changes | Sort-Object index -Descending)) {
            $text = $text.Insert($change.index, $change.value)
        }
        if ($changes.Count -gt 0) {
            [System.IO.File]::WriteAllText($path, $text, $utf8NoBom)
            $modifiedFiles.Add($path) | Out-Null
        }
    }

    foreach ($entry in $controllerFiles) {
        $path = $entry.file.FullName
        $className = $entry.file.BaseName
        $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        $operationsByMethod = @{}
        foreach ($operation in (Get-AnnotationRanges $text '@Operation\s*\(')) {
            $method = Find-NextPublicMethod $text $operation.end
            if ($null -ne $method) {
                $operationsByMethod[[string]$method.index] = $true
            }
        }
        $changes = New-Object System.Collections.Generic.List[object]
        foreach ($pair in (Get-MethodMappings $text)) {
            if ($operationsByMethod.ContainsKey([string]$pair.method.index)) {
                continue
            }
            $base = Get-OperationIdBase $entry.module $className $pair.method.name
            $operationId = New-UniqueOperationId $base $usedIds
            $mappingName = [regex]::Match($pair.mapping.text, '@(?<name>Get|Post|Put|Delete|Patch|Request)Mapping').Groups['name'].Value
            $summary = switch ($mappingName) {
                'Get' { '정보 조회' }
                'Post' { '요청 처리' }
                'Put' { '정보 변경' }
                'Patch' { '일부 정보 변경' }
                'Delete' { '정보 삭제' }
                default { 'API 요청 처리' }
            }
            $lineStart = $text.LastIndexOf("`n", [Math]::Max(0, $pair.method.index - 1)) + 1
            $indent = [regex]::Match($text.Substring($lineStart, $pair.method.index - $lineStart), '^\s*').Value
            $newline = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
            $changes.Add([pscustomobject]@{
                index = $lineStart
                value = "$indent@Operation(operationId = `"$operationId`", summary = `"$summary`")$newline"
            }) | Out-Null
        }
        foreach ($change in @($changes | Sort-Object index -Descending)) {
            $text = $text.Insert($change.index, $change.value)
        }
        if ($changes.Count -gt 0) {
            if ($text -notmatch 'import\s+io\.swagger\.v3\.oas\.annotations\.Operation\s*;') {
                $packageMatch = [regex]::Match($text, '(?m)^package\s+[^;]+;\s*\r?\n')
                if (-not $packageMatch.Success) {
                    throw "Operation import를 추가할 package 선언을 찾지 못했습니다: $path"
                }
                $newline = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
                $text = $text.Insert($packageMatch.Index + $packageMatch.Length, "${newline}import io.swagger.v3.oas.annotations.Operation;${newline}")
            }
            [System.IO.File]::WriteAllText($path, $text, $utf8NoBom)
            if (-not $modifiedFiles.Contains($path)) {
                $modifiedFiles.Add($path) | Out-Null
            }
        }
    }

    # 이전 자동 보강이나 수동 편집으로 annotation만 남은 파일도 전수 복구합니다.
    foreach ($entry in $controllerFiles) {
        $path = $entry.file.FullName
        $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        if ($text -notmatch '@Operation\s*\(' -or
            $text -match 'import\s+io\.swagger\.v3\.oas\.annotations\.Operation\s*;') {
            continue
        }
        $packageMatch = [regex]::Match($text, '(?m)^package\s+[^;]+;\s*\r?\n')
        if (-not $packageMatch.Success) {
            throw "Operation import를 추가할 package 선언을 찾지 못했습니다: $path"
        }
        $newline = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
        $text = $text.Insert($packageMatch.Index + $packageMatch.Length, "${newline}import io.swagger.v3.oas.annotations.Operation;${newline}")
        [System.IO.File]::WriteAllText($path, $text, $utf8NoBom)
        if (-not $modifiedFiles.Contains($path)) {
            $modifiedFiles.Add($path) | Out-Null
        }
    }
}

$failures = New-Object System.Collections.Generic.List[string]
$allIds = New-Object System.Collections.Generic.List[object]
$mappingCount = 0
$explicitCount = 0
foreach ($entry in $controllerFiles) {
    $text = [System.IO.File]::ReadAllText($entry.file.FullName, [System.Text.Encoding]::UTF8)
    $operationsByMethod = @{}
    foreach ($operation in (Get-AnnotationRanges $text '@Operation\s*\(')) {
        $method = Find-NextPublicMethod $text $operation.end
        if ($null -eq $method) {
            continue
        }
        $idMatch = [regex]::Match($operation.text, 'operationId\s*=\s*"([^"]+)"')
        if ($idMatch.Success) {
            $explicitCount++
            $operationsByMethod[[string]$method.index] = $idMatch.Groups[1].Value
            $allIds.Add([pscustomobject]@{
                operationId = $idMatch.Groups[1].Value
                path = $entry.file.FullName.Substring($Root.Length + 1).Replace('\', '/')
                method = $method.name
            }) | Out-Null
        }
    }
    foreach ($pair in (Get-MethodMappings $text)) {
        $mappingCount++
        if (-not $operationsByMethod.ContainsKey([string]$pair.method.index)) {
            $relative = $entry.file.FullName.Substring($Root.Length + 1).Replace('\', '/')
            $failures.Add("명시 operationId 누락: $relative#$($pair.method.name)") | Out-Null
        }
    }
}

foreach ($duplicate in @($allIds | Group-Object operationId | Where-Object Count -gt 1)) {
    $locations = ($duplicate.Group | ForEach-Object { "$($_.path)#$($_.method)" }) -join ', '
    $failures.Add("중복 operationId: $($duplicate.Name) [$locations]") | Out-Null
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = if ($failures.Count -eq 0) { 'DONE' } else { 'FAILED' }
    fixMode = [bool]$Fix
    controllerCount = $controllerFiles.Count
    methodMappingCount = $mappingCount
    explicitOperationIdCount = $explicitCount
    duplicateOperationIdCount = @($allIds | Group-Object operationId | Where-Object Count -gt 1).Count
    missingOperationIdCount = @($failures | Where-Object { $_ -like '명시 operationId 누락:*' }).Count
    modifiedFileCount = $modifiedFiles.Count
    failures = @($failures)
}
$resultPath = Join-Path $ResultDir 'openapi-source-coverage.sanitized.json'
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 8), $utf8NoBom)

Write-Host "OpenAPI explicit operationId status=$($result.status) mappings=$mappingCount explicit=$explicitCount modified=$($modifiedFiles.Count) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
