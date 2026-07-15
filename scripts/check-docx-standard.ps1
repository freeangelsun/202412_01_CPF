param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $AllowLegacyHtml,
    [switch] $OpenWithWord
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function U {
    param([string] $Value)

    return [System.Text.RegularExpressions.Regex]::Unescape($Value)
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'specs/evidence/20260715_01'
}
[System.IO.Directory]::CreateDirectory($ResultDir) | Out-Null

$documentNames = @(
    (U 'CPF_\uD504\uB808\uC784\uC6CC\uD06C_\uC18C\uAC1C_\uBC0F_\uC544\uD0A4\uD14D\uCC98.docx'),
    (U 'CPF_\uAC1C\uBC1C\uC790_\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_\uC6B4\uC601\uC790_ADM_\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_\uC124\uCE58_DB_SQL_Flyway_\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_\uBC30\uCE58_\uC13C\uD130\uCEF7_\uC2A4\uCF00\uC904\uB7EC_\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_\uC678\uBD80\uC5F0\uACC4_\uD30C\uC77C\uC804\uC1A1_\uC804\uBB38_\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_EDU_\uC0D8\uD50C_\uCE74\uD0C8\uB85C\uADF8_\uBC0F_\uC2E4\uC2B5\uAC00\uC774\uB4DC.docx'),
    (U 'CPF_\uAE30\uB2A5_\uAD6C\uD604_\uAC80\uC99D_\uB9E4\uD2B8\uB9AD\uC2A4.docx'),
    (U 'CPF_\uC804\uCCB4_\uD14C\uC2A4\uD2B8_\uAC80\uC99D_\uB9AC\uD3EC\uD2B8.docx'))
$requiredEntries = @(
    '[Content_Types].xml',
    '_rels/.rels',
    'docProps/core.xml',
    'word/document.xml',
    'word/styles.xml',
    'word/settings.xml',
    'word/header1.xml',
    'word/footer1.xml',
    'word/_rels/document.xml.rels')
$failures = New-Object System.Collections.Generic.List[string]
$items = New-Object System.Collections.Generic.List[object]
$commit = (& git -C $Root rev-parse HEAD).Trim()

Add-Type -AssemblyName System.IO.Compression.FileSystem

function Read-ZipEntryText {
    param(
        [System.IO.Compression.ZipArchive] $Archive,
        [string] $Name
    )

    $entry = $Archive.GetEntry($Name)
    if ($null -eq $entry) {
        return $null
    }
    $stream = $entry.Open()
    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8, $true)
    try {
        return $reader.ReadToEnd()
    } finally {
        $reader.Dispose()
        $stream.Dispose()
    }
}

foreach ($name in $documentNames) {
    $path = Join-Path (Join-Path $Root 'specs') $name
    $itemFailures = New-Object System.Collections.Generic.List[string]
    $metrics = [ordered]@{}
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        $itemFailures.Add('file missing') | Out-Null
    } elseif ((Get-Item -LiteralPath $path).Length -lt 5000) {
        $itemFailures.Add('file is too small') | Out-Null
    } else {
        $archive = $null
        try {
            $archive = [System.IO.Compression.ZipFile]::OpenRead($path)
            foreach ($required in $requiredEntries) {
                if ($null -eq $archive.GetEntry($required)) {
                    $itemFailures.Add("package entry missing: $required") | Out-Null
                }
            }
            $documentXml = Read-ZipEntryText -Archive $archive -Name 'word/document.xml'
            $settingsXml = Read-ZipEntryText -Archive $archive -Name 'word/settings.xml'
            $footerXml = Read-ZipEntryText -Archive $archive -Name 'word/footer1.xml'
            $stylesXml = Read-ZipEntryText -Archive $archive -Name 'word/styles.xml'
            $coreXml = Read-ZipEntryText -Archive $archive -Name 'docProps/core.xml'
            foreach ($xmlText in @($documentXml, $settingsXml, $footerXml, $stylesXml, $coreXml)) {
                if (-not [string]::IsNullOrWhiteSpace($xmlText)) {
                    try { [void][xml] $xmlText } catch { $itemFailures.Add('invalid XML part') | Out-Null }
                }
            }
            if ($null -ne $documentXml) {
                $metrics.heading1 = ([regex]::Matches($documentXml, 'w:val="Heading1"')).Count
                $metrics.heading2 = ([regex]::Matches($documentXml, 'w:val="Heading2"')).Count
                $metrics.heading3 = ([regex]::Matches($documentXml, 'w:val="Heading3"')).Count
                $metrics.tables = ([regex]::Matches($documentXml, '<w:tbl>')).Count
                $metrics.paragraphs = ([regex]::Matches($documentXml, '<w:p(?:>|\s)')).Count
                $metrics.koreanCharacters = ([regex]::Matches($documentXml, '[\uAC00-\uD7A3]')).Count
                if ($metrics.heading1 -lt 5) { $itemFailures.Add('Heading1 count is below 5') | Out-Null }
                if ($metrics.heading2 -lt 1) { $itemFailures.Add('Heading2 is missing') | Out-Null }
                if ($metrics.heading3 -lt 1) { $itemFailures.Add('Heading3 is missing') | Out-Null }
                if ($metrics.tables -lt 2) { $itemFailures.Add('table count is below 2') | Out-Null }
                if ($metrics.paragraphs -lt 20) { $itemFailures.Add('paragraph count is below 20') | Out-Null }
                if ($metrics.koreanCharacters -lt 20) { $itemFailures.Add('Korean content is insufficient') | Out-Null }
                if ($documentXml -notmatch 'TOC \\o &quot;1-3&quot;') { $itemFailures.Add('TOC field is missing') | Out-Null }
                if ($documentXml -notmatch 'w:headerReference') { $itemFailures.Add('header reference is missing') | Out-Null }
                if ($documentXml -notmatch 'w:footerReference') { $itemFailures.Add('footer reference is missing') | Out-Null }
                if ($documentXml -match 'Java 21|release 21|major 65') { $itemFailures.Add('legacy Java standard remains') | Out-Null }
                if ($documentXml -match '(?i)cpf_[a-z0-9_]*local_pw') { $itemFailures.Add('fixed password marker remains') | Out-Null }
            }
            if ($settingsXml -notmatch 'w:updateFields w:val="true"') { $itemFailures.Add('automatic field update is missing') | Out-Null }
            if ($footerXml -notmatch 'w:instr=" PAGE "') { $itemFailures.Add('page number field is missing') | Out-Null }
            if ($stylesXml -notmatch 'w:styleId="Code"' -or $stylesXml -notmatch 'w:styleId="Note"') { $itemFailures.Add('code or note style is missing') | Out-Null }
            if ($coreXml -notmatch [regex]::Escape($commit)) { $itemFailures.Add('current commit metadata is missing') | Out-Null }
        } catch {
            $itemFailures.Add("package open failed: $($_.Exception.Message)") | Out-Null
        } finally {
            if ($null -ne $archive) {
                $archive.Dispose()
            }
        }
    }
    foreach ($failure in $itemFailures) {
        $failures.Add("$name : $failure") | Out-Null
    }
    $items.Add([ordered]@{
        file = "specs/$name"
        bytes = if (Test-Path -LiteralPath $path) { (Get-Item -LiteralPath $path).Length } else { 0 }
        status = if ($itemFailures.Count -eq 0) { U '\uC644\uB8CC' } else { U '\uC2E4\uD328' }
        metrics = $metrics
        failures = @($itemFailures | ForEach-Object { $_ })
    }) | Out-Null
}

$htmlFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root 'specs') -Recurse -File -Filter '*.html')
if (-not $AllowLegacyHtml -and $htmlFiles.Count -gt 0) {
    $failures.Add("legacy specs HTML remains: $($htmlFiles.Count)") | Out-Null
}

$wordOpenResults = @()
if ($OpenWithWord -and $failures.Count -eq 0) {
    $word = $null
    $probeRoot = Join-Path $Root 'build/docx-open-probe'
    if (Test-Path -LiteralPath $probeRoot) {
        Remove-Item -LiteralPath $probeRoot -Recurse -Force
    }
    [System.IO.Directory]::CreateDirectory($probeRoot) | Out-Null
    try {
        $word = New-Object -ComObject Word.Application
        $word.Visible = $false
        $word.DisplayAlerts = 0
        foreach ($name in $documentNames) {
            $sourcePath = Join-Path (Join-Path $Root 'specs') $name
            $path = Join-Path $probeRoot $name
            Copy-Item -LiteralPath $sourcePath -Destination $path -Force
            $document = $null
            try {
                $document = $word.Documents.Open($path, $false, $true, $false)
                $wordOpenResults += [ordered]@{ file = "specs/$name"; status = U '\uC644\uB8CC'; paragraphs = $document.Paragraphs.Count }
            } finally {
                if ($null -ne $document) { $document.Close(0) }
            }
        }
    } finally {
        if ($null -ne $word) {
            $word.Quit()
            [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word)
        }
        [GC]::Collect()
        [GC]::WaitForPendingFinalizers()
        if (Test-Path -LiteralPath $probeRoot) {
            Remove-Item -LiteralPath $probeRoot -Recurse -Force
        }
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    startCommit = $commit
    status = if ($failures.Count -eq 0) { U '\uC644\uB8CC' } else { U '\uC2E4\uD328' }
    documentCount = $documentNames.Count
    htmlCount = $htmlFiles.Count
    wordOpenExecuted = [bool] $OpenWithWord
    documents = @($items | ForEach-Object { $_ })
    wordOpenResults = $wordOpenResults
    failures = @($failures | ForEach-Object { $_ })
}
$resultName = if ($OpenWithWord) { 'docx-word-open.sanitized.json' } else { 'docx-standard.sanitized.json' }
$resultPath = Join-Path $ResultDir $resultName
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "DOCX standard check failed: $($failures.Count)"
}
Write-Host "DOCX standard check passed: documents=$($documentNames.Count) wordOpen=$([bool]$OpenWithWord)"
