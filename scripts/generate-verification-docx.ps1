param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [ValidateSet("all", "matrix", "report")]
    [string] $DocumentKind = "all"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function ConvertTo-XmlText {
    param([string] $Value)

    if ($null -eq $Value) {
        return ""
    }
    return [System.Security.SecurityElement]::Escape($Value)
}

function New-WmlParagraph {
    param(
        [string] $Text,
        [string] $Style = "Normal",
        [switch] $PageBreak
    )

    $styleXml = if ([string]::IsNullOrWhiteSpace($Style) -or $Style -eq "Normal") {
        ""
    } else {
        '<w:pPr><w:pStyle w:val="' + (ConvertTo-XmlText $Style) + '"/></w:pPr>'
    }
    $breakXml = if ($PageBreak) { '<w:br w:type="page"/>' } else { "" }
    $textXml = ConvertTo-XmlText $Text
    return '<w:p>' + $styleXml + '<w:r>' + $breakXml + '<w:t xml:space="preserve">' + $textXml + '</w:t></w:r></w:p>'
}

function New-WmlTable {
    param([object[]] $Rows)

    if ($Rows.Count -eq 0) {
        return ""
    }
    $columnCount = @($Rows | ForEach-Object { $_.Count } | Measure-Object -Maximum).Maximum
    $builder = New-Object System.Text.StringBuilder
    [void] $builder.Append('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders>')
    foreach ($edge in @("top", "left", "bottom", "right", "insideH", "insideV")) {
        [void] $builder.Append('<w:' + $edge + ' w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>')
    }
    [void] $builder.Append('</w:tblBorders></w:tblPr>')
    for ($rowIndex = 0; $rowIndex -lt $Rows.Count; $rowIndex++) {
        [void] $builder.Append('<w:tr>')
        for ($columnIndex = 0; $columnIndex -lt $columnCount; $columnIndex++) {
            $value = if ($columnIndex -lt $Rows[$rowIndex].Count) { [string] $Rows[$rowIndex][$columnIndex] } else { "" }
            $bold = if ($rowIndex -eq 0) { '<w:rPr><w:b/></w:rPr>' } else { "" }
            [void] $builder.Append('<w:tc><w:tcPr><w:tcW w:w="0" w:type="auto"/></w:tcPr><w:p><w:r>')
            [void] $builder.Append($bold)
            [void] $builder.Append('<w:t xml:space="preserve">' + (ConvertTo-XmlText $value) + '</w:t></w:r></w:p></w:tc>')
        }
        [void] $builder.Append('</w:tr>')
    }
    [void] $builder.Append('</w:tbl>')
    return $builder.ToString()
}

function Convert-MarkdownToWml {
    param([string] $Markdown)

    $output = New-Object System.Collections.Generic.List[string]
    $lines = @($Markdown.Replace("`r`n", "`n").Split("`n"))
    $inCode = $false
    $index = 0
    while ($index -lt $lines.Count) {
        $line = $lines[$index]
        if ($line -match '^```') {
            $inCode = -not $inCode
            $index++
            continue
        }
        if ($inCode) {
            $output.Add((New-WmlParagraph -Text $line -Style "Code")) | Out-Null
            $index++
            continue
        }
        if ($line -match '^\|.+\|\s*$') {
            $rows = New-Object System.Collections.Generic.List[object]
            while ($index -lt $lines.Count -and $lines[$index] -match '^\|.+\|\s*$') {
                $cells = @($lines[$index].Trim().Trim('|').Split('|') | ForEach-Object { $_.Trim().Replace('\|', '|') })
                if (($cells | Where-Object { $_ -notmatch '^:?-{3,}:?$' }).Count -gt 0) {
                    $rows.Add($cells) | Out-Null
                }
                $index++
            }
            $output.Add((New-WmlTable -Rows $rows.ToArray())) | Out-Null
            continue
        }
        if ($line -match '^(#{1,3})\s+(.+)$') {
            $style = switch ($Matches[1].Length) { 1 { "Heading1" } 2 { "Heading2" } default { "Heading3" } }
            $output.Add((New-WmlParagraph -Text $Matches[2] -Style $style)) | Out-Null
        } elseif ($line -match '^>\s*(.*)$') {
            $output.Add((New-WmlParagraph -Text $Matches[1] -Style "Note")) | Out-Null
        } elseif ($line -match '^[-*]\s+(.+)$') {
            $output.Add((New-WmlParagraph -Text ("- " + $Matches[1]))) | Out-Null
        } elseif (-not [string]::IsNullOrWhiteSpace($line)) {
            $output.Add((New-WmlParagraph -Text $line)) | Out-Null
        }
        $index++
    }
    return ($output -join "")
}

function Read-ZipEntryText {
    param(
        [System.IO.Compression.ZipArchive] $Archive,
        [string] $Name
    )

    $entry = $Archive.GetEntry($Name)
    if ($null -eq $entry) {
        throw "DOCX package entry가 없습니다. entry=$Name"
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

function Write-ZipEntryText {
    param(
        [System.IO.Compression.ZipArchive] $Archive,
        [string] $Name,
        [string] $Text
    )

    $existing = $Archive.GetEntry($Name)
    if ($null -ne $existing) {
        $existing.Delete()
    }
    $entry = $Archive.CreateEntry($Name, [System.IO.Compression.CompressionLevel]::Optimal)
    $stream = $entry.Open()
    $writer = New-Object System.IO.StreamWriter($stream, $Utf8NoBom)
    try {
        $writer.Write($Text)
    } finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

function Update-VerificationDocument {
    param(
        [string] $TargetPath,
        [string] $TemplatePath,
        [string] $Title,
        [string[]] $Sources,
        [string] $Commit
    )

    if (-not (Test-Path -LiteralPath $TemplatePath -PathType Leaf)) {
        throw "검증 DOCX의 기준 템플릿이 없습니다. path=$TemplatePath"
    }
    $workRoot = Join-Path $Root "build/docx-generation"
    [System.IO.Directory]::CreateDirectory($workRoot) | Out-Null
    $outputPath = Join-Path $workRoot (([System.IO.Path]::GetFileNameWithoutExtension($TargetPath)) + ".output.docx")
    Copy-Item -LiteralPath $TemplatePath -Destination $outputPath -Force

    $archive = [System.IO.Compression.ZipFile]::Open($outputPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $templateDocument = Read-ZipEntryText -Archive $archive -Name "word/document.xml"
        $documentOpen = [regex]::Match($templateDocument, '^\s*(<\?xml[^>]+>\s*)?(<w:document\b[^>]*>)', 'IgnoreCase').Groups[2].Value
        $section = [regex]::Match($templateDocument, '<w:sectPr\b[\s\S]*?</w:sectPr>', 'IgnoreCase').Value
        if ([string]::IsNullOrWhiteSpace($documentOpen) -or [string]::IsNullOrWhiteSpace($section)) {
            throw "DOCX 템플릿의 document root 또는 section 설정을 찾지 못했습니다."
        }

        $metadata = @(
            @("문서", $Title),
            @("기준", "Java 25 / class major 69"),
            @("Commit", $Commit),
            @("생성일", (Get-Date -Format "yyyy-MM-dd")))
        $body = New-Object System.Collections.Generic.List[string]
        $body.Add((New-WmlParagraph -Text $Title -Style "Title")) | Out-Null
        $body.Add((New-WmlParagraph -Text "CPF Java 25 공식 기준본")) | Out-Null
        $body.Add((New-WmlParagraph -Text ("기준 commit: " + $Commit))) | Out-Null
        $body.Add((New-WmlParagraph -Text "" -PageBreak)) | Out-Null
        $body.Add((New-WmlParagraph -Text "문서 정보" -Style "Heading1")) | Out-Null
        $body.Add((New-WmlTable -Rows $metadata)) | Out-Null
        $body.Add((New-WmlParagraph -Text "목차" -Style "Heading1")) | Out-Null
        $body.Add('<w:p><w:fldSimple w:instr="TOC \o &quot;1-3&quot; \h \z \u"><w:r><w:t>목차는 Word에서 필드 업데이트 시 갱신됩니다.</w:t></w:r></w:fldSimple></w:p>') | Out-Null
        $body.Add((New-WmlParagraph -Text "문서 목적" -Style "Heading1")) | Out-Null
        $body.Add((New-WmlParagraph -Text "이 문서는 CPF 구현 상태와 검증 근거를 저장소 실제 결과에 맞춰 제공한다.")) | Out-Null
        $body.Add((New-WmlParagraph -Text "판정 원칙" -Style "Heading2")) | Out-Null
        $body.Add((New-WmlParagraph -Text "미실행 검증은 완료로 기록하지 않고 민감정보는 원문으로 남기지 않는다." -Style "Note")) | Out-Null
        $body.Add((New-WmlParagraph -Text "상태 해석" -Style "Heading3")) | Out-Null
        $body.Add((New-WmlParagraph -Text "완료, 부분 구현, 미구현, 미검증, 실패, 재확인 필요 상태만 사용한다.")) | Out-Null

        foreach ($source in $Sources) {
            $sourcePath = Join-Path $Root $source
            if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
                throw "DOCX 원본 Markdown이 없습니다. source=$source"
            }
            $markdown = [System.IO.File]::ReadAllText($sourcePath, [System.Text.Encoding]::UTF8)
            $body.Add((Convert-MarkdownToWml -Markdown $markdown)) | Out-Null
        }

        $body.Add((New-WmlParagraph -Text "검증 명령" -Style "Heading1")) | Out-Null
        $body.Add((New-WmlParagraph -Text "powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-docx-standard.ps1" -Style "Code")) | Out-Null
        $body.Add((New-WmlParagraph -Text "관련 정본" -Style "Heading1")) | Out-Null
        $body.Add((New-WmlParagraph -Text "README.md, CPF_STABILIZATION_REPORT.md, CPF_EVIDENCE_INDEX.md")) | Out-Null

        $documentXml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
                $documentOpen + '<w:body>' + ($body -join "") + $section + '</w:body></w:document>'
        Write-ZipEntryText -Archive $archive -Name "word/document.xml" -Text $documentXml

        $coreXml = Read-ZipEntryText -Archive $archive -Name "docProps/core.xml"
        if ($coreXml -notmatch [regex]::Escape($Commit)) {
            $coreXml = [regex]::Replace($coreXml, '</cp:coreProperties>', '<dc:subject>' + (ConvertTo-XmlText ("Commit " + $Commit)) + '</dc:subject></cp:coreProperties>')
            Write-ZipEntryText -Archive $archive -Name "docProps/core.xml" -Text $coreXml
        }
    } finally {
        $archive.Dispose()
    }

    $bytes = [System.IO.File]::ReadAllBytes($outputPath)
    if ($bytes.Length -lt 4 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
        throw "생성 결과가 DOCX ZIP 패키지가 아닙니다. path=$outputPath"
    }
    Copy-Item -LiteralPath $outputPath -Destination $TargetPath -Force
    Remove-Item -LiteralPath $outputPath -Force
}

$commit = (& git -C $Root rev-parse HEAD).Trim()
$specs = Join-Path $Root "specs"
$templatePath = Join-Path $specs "CPF_개발자_가이드.docx"
$definitions = @(
    [pscustomobject]@{
        Kind = "matrix"
        Path = Join-Path $specs "CPF_기능_구현_검증_매트릭스.docx"
        Title = "CPF 기능 구현 검증 매트릭스"
        Sources = @("specs/기능_구현_매트릭스.md")
    },
    [pscustomobject]@{
        Kind = "report"
        Path = Join-Path $specs "CPF_전체_테스트_검증_리포트.docx"
        Title = "CPF 전체 테스트 검증 리포트"
        Sources = @("CPF_STABILIZATION_REPORT.md", "CPF_GAP_MATRIX.md", "CPF_EVIDENCE_INDEX.md")
    }
)
if ($DocumentKind -ne "all") {
    $definitions = @($definitions | Where-Object { $_.Kind -eq $DocumentKind })
}
foreach ($definition in $definitions) {
    Update-VerificationDocument -TargetPath $definition.Path -TemplatePath $templatePath -Title $definition.Title -Sources $definition.Sources -Commit $commit
    Write-Host "DOCX Markdown 정본 갱신 완료: $($definition.Path)"
}
