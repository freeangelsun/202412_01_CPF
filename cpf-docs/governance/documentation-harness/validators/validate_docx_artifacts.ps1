$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
$Docs=@(
 'cpf-docs/guides/02_프레임워크_개발자_가이드.docx','cpf-docs/guides/03_배치_개발자_가이드.docx',
 'cpf-docs/guides/04_운영자_매뉴얼.docx','cpf-docs/guides/05_배치_운영_가이드.docx',
 'cpf-docs/guides/06_Gateway_개발_사용_가이드.docx','cpf-docs/guides/07_Specification_기술_명세.docx',
 'cpf-docs/deliverables/아키텍처설계서.docx','cpf-docs/deliverables/기술사양서.docx',
 'cpf-docs/deliverables/기술표준서.docx','cpf-docs/deliverables/데이터베이스표준서.docx','cpf-docs/deliverables/산출물목록.docx')
Add-Type -AssemblyName System.IO.Compression.FileSystem
$errors=@(); $prov='(?i)(Harness\s*(?:v)?\d+(?:\.\d+)+|Source\s*(?:SHA\s*)?[0-9A-F]{16,}|CPF_FULL_SOURCE_FOR_NEXT_QA_\d+)'
foreach($rel in $Docs){
 $p=Join-Path $Root $rel
 if(!(Test-Path -LiteralPath $p -PathType Leaf)){$errors+="$rel: missing";continue}
 $z=$null
 try{
  $z=[IO.Compression.ZipFile]::OpenRead($p);$e=$z.GetEntry('word/document.xml');if($null-eq$e){$errors+="$rel: word/document.xml missing";continue}
  $sr=New-Object IO.StreamReader($e.Open(),[Text.Encoding]::UTF8);$xml=$sr.ReadToEnd();$sr.Dispose()
  $plain=[regex]::Replace($xml,'<[^>]+>',' ');$plain=[Net.WebUtility]::HtmlDecode($plain)
  $pg=[regex]::Match($xml,'<w:pgSz[^>]*w:w="(\d+)"[^>]*/?>');$mar=[regex]::Match($xml,'<w:pgMar[^>]*w:left="(\d+)"[^>]*w:right="(\d+)"[^>]*/?>')
  if($pg.Success-and$mar.Success){$writable=[int]$pg.Groups[1].Value-[int]$mar.Groups[1].Value-[int]$mar.Groups[2].Value;$toc=[regex]::Matches($xml,'(?s)<w:p[^>]*>.*?<w:pStyle w:val="CPFTOCEntry".*?</w:p>');foreach($tp in $toc){$tm=[regex]::Match($tp.Value,'<w:tab[^>]*w:val="right"[^>]*w:pos="(\d+)"');if(!$tm.Success){$errors+="$rel: TOC entry missing right tab stop"}elseif([int]$tm.Groups[1].Value -gt $writable){$errors+="$rel: TOC tab stop outside writable width"}}}
  if($plain -match $prov){$errors+="$rel: user-facing production provenance $($Matches[0])"}
  $tables=[regex]::Matches($xml,'(?s)<w:tbl[ >].*?</w:tbl>')
  for($i=0;$i-lt$tables.Count;$i++){
    $tx=$tables[$i].Value; $rows=[regex]::Matches($tx,'<w:tr[ >]').Count
    if($rows-eq 1){$errors+="$rel: single-row layout table #$($i+1)"}
    if($tx -notmatch '<w:tblHeader(?:\s|/|>)'){$errors+="$rel: table #$($i+1) header row not marked repeat header"}
    if($i-lt 2){$tt=[Net.WebUtility]::HtmlDecode([regex]::Replace($tx,'<[^>]+>',' '));$hits=0;foreach($m in @('누가 보는가','이 문서로 끝낼 일','기준')){if($tt.Contains($m)){$hits++}};if($hits-ge2){$errors+="$rel: opening reader/purpose/basis metadata table"}}
  }
 } catch {$errors+="$rel: $($_.Exception.Message)"} finally {if($null-ne$z){$z.Dispose()}}
}
if($errors.Count){Write-Host "DOCX_STRUCTURE=FAIL COUNT=$($errors.Count)";$errors|ForEach-Object{Write-Host "- $_"};throw 'DOCX_STRUCTURE_FAIL'}
Write-Host "DOCX_STRUCTURE=PASS DOCX=$($Docs.Count)"
