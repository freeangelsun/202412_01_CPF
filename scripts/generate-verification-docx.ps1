param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [ValidateSet("all", "matrix", "report")]
    [string] $DocumentKind = "all",
    [switch] $MetadataOnly
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

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

function Update-DocumentCommitMetadata {
    param(
        [string] $TargetPath,
        [string] $Commit
    )

    $archive = [System.IO.Compression.ZipFile]::Open($TargetPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $coreXml = Read-ZipEntryText -Archive $archive -Name "docProps/core.xml"
        $commitText = "Commit $Commit"
        if ($coreXml -match '(?i)Commit\s+[0-9a-f]{40}') {
            $coreXml = [regex]::Replace($coreXml, '(?i)Commit\s+[0-9a-f]{40}', $commitText)
        } elseif ($coreXml -match '</cp:coreProperties>') {
            $coreXml = [regex]::Replace(
                    $coreXml,
                    '</cp:coreProperties>',
                    '<dc:subject>' + (ConvertTo-XmlText $commitText) + '</dc:subject></cp:coreProperties>')
        } else {
            throw "DOCX core metadata root를 찾을 수 없습니다. path=$TargetPath"
        }
        Write-ZipEntryText -Archive $archive -Name "docProps/core.xml" -Text $coreXml
    } finally {
        $archive.Dispose()
    }
}

function Update-ManagedDocumentSection {
    param(
        [string] $TargetPath,
        [string] $SectionId,
        [string[]] $Content
    )

    if (-not (Test-Path -LiteralPath $TargetPath -PathType Leaf)) {
        throw "관리 구간을 갱신할 DOCX가 없습니다. path=$TargetPath"
    }
    $begin = "<!-- CPF_MANAGED_SECTION_BEGIN:$SectionId -->"
    $end = "<!-- CPF_MANAGED_SECTION_END:$SectionId -->"
    $managed = $begin + ($Content -join "") + $end
    $archive = [System.IO.Compression.ZipFile]::Open($TargetPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $documentXml = Read-ZipEntryText -Archive $archive -Name "word/document.xml"
        $pattern = [regex]::Escape($begin) + '[\s\S]*?' + [regex]::Escape($end)
        if ([regex]::IsMatch($documentXml, $pattern)) {
            $documentXml = [regex]::Replace($documentXml, $pattern, $managed)
        } elseif ($documentXml -match '<w:sectPr\b') {
            $documentXml = [regex]::Replace($documentXml, '<w:sectPr\b', $managed + '<w:sectPr', 1)
        } else {
            throw "DOCX 관리 구간을 삽입할 section 설정을 찾지 못했습니다. path=$TargetPath"
        }
        Write-ZipEntryText -Archive $archive -Name "word/document.xml" -Text $documentXml
    } finally {
        $archive.Dispose()
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
if ($MetadataOnly) {
    Get-ChildItem -LiteralPath $specs -File -Filter "CPF_*.docx" | ForEach-Object {
        Update-DocumentCommitMetadata -TargetPath $_.FullName -Commit $commit
        Write-Host "DOCX commit metadata 갱신 완료: $($_.FullName)"
    }
    exit 0
}

$managedGuideDefinitions = @(
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_개발자_가이드.docx"
        SectionId = "BZA_SUPPORT_AND_ATTACHMENT_20260715"
        Content = @(
            (New-WmlParagraph -Text "CPF 첨부 저장과 BZA 운영 지원" -Style "Heading1"),
            (New-WmlParagraph -Text "CPF는 CpfAttachmentStoragePort와 LocalCpfAttachmentStorageAdapter로 저장소 기술 계약을 제공하고 BZA는 알림, 첨부 메타, 저장 검색, 다운로드 감사, 역할 비교와 권한 시뮬레이션 업무를 소유한다."),
            (New-WmlParagraph -Text "prod profile에서는 CPF_ATTACHMENT_ROOT가 필수이며 object storage 또는 보안 파일 서버 adapter는 CpfAttachmentStoragePort 구현으로 교체한다."),
            (New-WmlParagraph -Text "쓰기·결재·다운로드 감사 주체는 요청 JSON이 아니라 BzaApiAuthFilter가 설정한 bza.operatorId를 사용한다."),
            (New-WmlTable -Rows @(
                @("구분", "정본 경로"),
                @("CPF port", "cpf-core/src/main/java/com/cpf/core/common/attachment"),
                @("BZA API", "cpf-biz-admin/src/main/java/com/cpf/bizadmin/support"),
                @("EDU", "cpf-reference/src/main/java/com/cpf/reference/attachment"),
                @("단위 테스트", "LocalCpfAttachmentStorageAdapterTest, BzaSupportServiceTest, ReferenceAttachmentEducationSampleTest"),
                @("DB 변경", "specs/sql/migration/flyway/V31__bza_operation_support.sql")
            ))
        )
    },
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_운영자_ADM_가이드.docx"
        SectionId = "BZA_SUPPORT_OPERATIONS_20260715"
        Content = @(
            (New-WmlParagraph -Text "BZA 운영 지원 기능" -Style "Heading1"),
            (New-WmlParagraph -Text "BZA 운영자는 대시보드, 내 알림, 첨부파일, 저장 검색, 다운로드 감사, 역할 권한 비교와 권한 시뮬레이션을 사용한다."),
            (New-WmlParagraph -Text "알림 등록·읽음, 첨부 업로드·다운로드, 저장 검색 변경과 권한 시뮬레이션은 서버 권한과 감사 사유를 요구한다."),
            (New-WmlParagraph -Text "첨부 다운로드는 checksum과 scan_status를 확인하며 PASSED_LOCAL_POLICY는 로컬 확장자·경로 정책 통과를 뜻하고 외부 악성코드 검사 완료를 뜻하지 않는다."),
            (New-WmlParagraph -Text "실 object storage, 악성코드 검사와 보존 정책은 운영 adapter와 배포 secret 설정으로 연결하고 미연동 상태를 완료로 보고하지 않는다." -Style "Note")
        )
    },
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_운영자_ADM_가이드.docx"
        SectionId = "REMOTE_LOG_ASYNC_20260715"
        Content = @(
            (New-WmlParagraph -Text "원격 로그 검색과 비동기 ZIP 운영" -Style "Heading1"),
            (New-WmlParagraph -Text "ADM 원격 로그는 환경, 모듈, 서비스, 인스턴스, 표준 온라인·배치 ID, 거래·구간 ID, Spring Batch 실행 ID, scheduler ID, 수정 시각, 크기, 압축과 활성 상태로 검색한다."),
            (New-WmlParagraph -Text "선택 ZIP은 동기 다운로드와 비동기 작업을 구분한다. 비동기 작업은 등록 운영자에게만 노출되며 완료 후 짧은 유효기간의 1회성 token을 발급한다. 같은 파일을 다시 받으려면 새 token을 발급해야 한다."),
            (New-WmlParagraph -Text "모든 생성, token 발급과 다운로드에는 서버 권한과 감사 사유가 필요하다. 조회 전용 권한보다 구체적인 다운로드 거부 권한이 우선한다."),
            (New-WmlParagraph -Text "기본 InMemoryCpfRemoteLogBundleJobAdapter는 단일 ADM 인스턴스용이다. 운영 cluster에서는 CpfRemoteLogBundleJobPort를 공유 queue·저장소·분산 rate limit 구현으로 교체한다." -Style "Note")
        )
    },
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_설치_DB_SQL_Flyway_가이드.docx"
        SectionId = "BZA_SUPPORT_SQL_20260715"
        Content = @(
            (New-WmlParagraph -Text "BZA 운영 지원 DB 변경" -Style "Heading1"),
            (New-WmlParagraph -Text "V31__bza_operation_support.sql은 bza_notification, bza_attachment, bza_saved_search, bza_download_audit를 추가한다."),
            (New-WmlParagraph -Text "신규 설치는 40_business_modules_schema.sql과 V1 baseline에 포함되고 기존 설치 업그레이드는 V31을 적용한다. 00_all_install.sql과 00_all_install_and_smoke.sql은 build-all-install-sql.ps1로 재생성한다."),
            (New-WmlParagraph -Text "모든 신규 테이블은 bza_ prefix, created_by, created_at, updated_by, updated_at과 한글 COMMENT를 가진다."),
            (New-WmlParagraph -Text "실 MariaDB 검증은 접속 자격정보가 제공된 환경에서 smoke-mariadb-full-install.ps1 -RequireRun으로 별도 수행한다." -Style "Note")
        )
    },
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_프레임워크_소개_및_아키텍처.docx"
        SectionId = "ATTACHMENT_OWNERSHIP_20260715"
        Content = @(
            (New-WmlParagraph -Text "첨부파일 capability 소유권" -Style "Heading1"),
            (New-WmlParagraph -Text "CPF는 경로, 확장자, 크기, checksum과 저장 adapter 교체 지점을 소유한다. BZA는 업무 첨부 그룹, 메타, 권한, 감사와 다운로드 정책을 소유한다."),
            (New-WmlParagraph -Text "BZA는 CPF port만 호출하며 CPF는 bzaDB repository를 참조하지 않는다. 이 경계는 타 주제영역 DB·Repository·Mapper 직접 접근 금지 원칙과 같다.")
        )
    },
    [pscustomobject]@{
        Path = Join-Path $specs "CPF_EDU_샘플_카탈로그_및_실습가이드.docx"
        SectionId = "ATTACHMENT_EDU_20260715"
        Content = @(
            (New-WmlParagraph -Text "REF 첨부파일 EDU" -Style "Heading1"),
            (New-WmlTable -Rows @(
                @("sample ID", "API", "학습 목적"),
                @("REF Reference-ATTACH-001", "POST /api/reference/attachments/text", "CPF 저장 port, 파일명·확장자·크기·경로 검증과 SHA-256"),
                @("REF Reference-ATTACH-002", "POST /api/reference/attachments/verify", "저장 key 재조회와 checksum 무결성 검증")
            )),
            (New-WmlParagraph -Text "소스는 cpf-reference/src/main/java/com/cpf/reference/attachment, 테스트는 cpf-reference/src/test/java/com/cpf/reference/attachment에 있다."),
            (New-WmlParagraph -Text "실습에서는 txt 저장 성공, exe 확장자 거부, checksum 일치와 경로 이탈 거부를 확인한다.")
        )
    }
)
foreach ($guide in $managedGuideDefinitions) {
    Update-ManagedDocumentSection -TargetPath $guide.Path -SectionId $guide.SectionId -Content $guide.Content
    Update-DocumentCommitMetadata -TargetPath $guide.Path -Commit $commit
    Write-Host "공식 DOCX 관리 구간 갱신 완료: $($guide.Path)"
}
$templatePath = Join-Path $specs "CPF_개발자_가이드.docx"
$definitions = @(
    [pscustomobject]@{
        Kind = "matrix"
        Path = Join-Path $specs "CPF_기능_구현_검증_매트릭스.docx"
        Title = "CPF 기능 구현 검증 매트릭스"
        Sources = @("specs/generated/feature-implementation-matrix.md")
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
