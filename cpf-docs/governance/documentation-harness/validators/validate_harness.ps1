$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path

function Fail([string]$Message){
    Write-Host ("HARNESS=FAIL " + $Message)
    exit 1
}
function Load-Json([string]$Relative){
    $p=Join-Path $Root ($Relative -replace '/','\\')
    if(-not (Test-Path -LiteralPath $p -PathType Leaf)){ Fail("missing " + $Relative) }
    try { return (Get-Content -LiteralPath $p -Raw -Encoding UTF8 | ConvertFrom-Json) }
    catch { Fail("json " + $Relative + ": " + $_.Exception.Message) }
}
function As-Array($Value){ return @($Value) }

$h=Load-Json 'harness.json'
if($h.version -ne '1.1.3'){ Fail 'version' }
if($h.locked -ne $true -or $h.changeAuthority -ne 'USER_EXPLICIT_REQUEST_ONLY'){ Fail 'change authority' }
if($h.changePolicy.autoModify -ne $false){ Fail 'auto modify' }

foreach($f in @('design-tokens.json','writing-style.json','content-density.json','visual-system.json','document-output-rules.json','readme-value-inventory.json')){
    $d=Load-Json $f
    if($d.harnessVersion -ne '1.1.3'){ Fail("version " + $f) }
}

$D=Load-Json 'design-tokens.json'
if($D.toc.readme_toc -ne 'forbidden'){ Fail 'README TOC must be forbidden' }
if($D.tables.body_default_alignment -ne 'left'){ Fail 'table body left' }
if($D.tables.equal_width_default -ne 'forbidden'){ Fail 'equal width' }
if([int]$D.tables.max_columns_portrait -ne 4 -or [int]$D.tables.max_columns_landscape -ne 5){ Fail 'table columns' }
if([int]$D.tables.max_cell_korean_chars_review -gt 70){ Fail 'cell prose limit' }

$W=Load-Json 'writing-style.json'
function Get-Utf8Sha256Text([string]$Text){
    $sha=[System.Security.Cryptography.SHA256]::Create()
    try {
        $enc=New-Object System.Text.UTF8Encoding($false)
        $bytes=$enc.GetBytes($Text)
        return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-','').ToLowerInvariant()
    } finally { $sha.Dispose() }
}
$LicenseExpectedSha='187a77beb5372e34ea4c95eff4a9281365d8bf429e6805627e270b946a7107dd'
function Require-LicenseSha($Value,[string]$Label){
    $actual=Get-Utf8Sha256Text ([string]$Value)
    if($actual -ne $LicenseExpectedSha){ Fail($Label + ' expected=' + $LicenseExpectedSha + ' actual=' + $actual) }
}
Require-LicenseSha $W.license.exact_user_facing_sentence 'license writing-style'
$O=Load-Json 'document-output-rules.json'
Require-LicenseSha $O.README.licenseExactSentence 'license output-rules'

$TObj=(Load-Json 'table-presets.json').presets
$tableNames=@($TObj.PSObject.Properties | ForEach-Object { $_.Name })
foreach($prop in $TObj.PSObject.Properties){
    $name=$prop.Name; $t=$prop.Value
    $widths=@($t.widthPct | ForEach-Object { [int]$_ })
    $columns=@($t.columns)
    $sum=0; foreach($w in $widths){$sum+=$w}
    if($sum -ne 100){ Fail("width sum " + $name) }
    if($columns.Count -ne $widths.Count){ Fail("columns " + $name) }
    if($widths.Count -gt 5){ Fail("too many cols " + $name) }
    if($widths.Count -gt 2){
        $unique=@($widths | Select-Object -Unique)
        if($unique.Count -eq 1){ Fail("equal widths " + $name) }
    }
}

$FObj=(Load-Json 'figure-presets.json').presets
$figureNames=@($FObj.PSObject.Properties | ForEach-Object { $_.Name })
if($figureNames -notcontains 'README_ARCHITECTURE_MAP'){ Fail 'architecture visual' }

$scope=Load-Json 'scope.json'; $arts=@($scope.officialArtifacts)
if($arts.Count -ne 12 -or [int]$scope.officialDocxCount -ne 11 -or [int]$scope.officialPdfCount -ne 11){ Fail 'scope count' }
$modelsObj=(Load-Json 'content-models.json').models
$modelNames=@($modelsObj.PSObject.Properties | ForEach-Object { $_.Name })

foreach($a in $arts){
    $profilePath=Join-Path (Join-Path $Root 'profiles') ([string]$a.profile)
    if(-not (Test-Path -LiteralPath $profilePath -PathType Leaf)){ Fail("profile missing " + [string]$a.profile) }
    try { $pr=Get-Content -LiteralPath $profilePath -Raw -Encoding UTF8 | ConvertFrom-Json }
    catch { Fail("profile json " + [string]$a.profile + ': ' + $_.Exception.Message) }
    if($pr.documentId -ne $a.id -or $pr.changeAuthority -ne 'USER_EXPLICIT_REQUEST_ONLY'){ Fail("profile " + [string]$a.profile) }
    if($pr.additionalH1 -ne $false){ Fail("extra h1 " + [string]$a.profile) }
    if($a.id -eq 'README'){
        if($pr.tocRequired -ne $false){ Fail 'README profile TOC' }
        $nums=@()
        foreach($s in @($pr.sections)){
            $m=[regex]::Match([string]$s.title,'^(\d+)\. ')
            if(-not $m.Success){ Fail("README unnumbered " + [string]$s.title) }
            $nums += [int]$m.Groups[1].Value
        }
        for($i=0;$i -lt $nums.Count;$i++){ if($nums[$i] -ne ($i+1)){ Fail 'README numbering' } }
        if($pr.specialRules.architectureMap.required -ne $true){ Fail 'README architecture' }
        Require-LicenseSha $pr.specialRules.license.exactSentence 'license README profile'
        $licenseSections=@($pr.sections | Where-Object { @($_.requiredH2).Count -eq 1 -and (Get-Utf8Sha256Text ([string]$_.requiredH2[0])) -eq $LicenseExpectedSha })
        if($licenseSections.Count -ne 1){ Fail('license README H2 expected=' + $LicenseExpectedSha + ' matches=' + $licenseSections.Count) }
    } else {
        if($pr.tocRequired -ne $true){ Fail("DOCX toc " + [string]$a.profile) }
    }
    foreach($s in @($pr.sections)){
        if($s.additionalH2 -ne $false -or $s.additionalH3 -ne $false){ Fail("extra heading " + [string]$a.profile) }
        if($modelNames -notcontains [string]$s.model){ Fail("model " + [string]$a.profile + ':' + [string]$s.model) }
        foreach($tn in @($s.tables)){ if($tableNames -notcontains [string]$tn){ Fail("table " + [string]$a.profile + ':' + [string]$tn) } }
        foreach($fn in @($s.figures)){ if($figureNames -notcontains [string]$fn){ Fail("figure " + [string]$a.profile + ':' + [string]$fn) } }
    }
}

$C=Load-Json 'product-coverage.json'; $items=@($C.items)
if($items.Count -lt 55){ Fail 'coverage' }

$deletePath=Join-Path $Root 'DELETE_MANIFEST.txt'
foreach($raw in (Get-Content -LiteralPath $deletePath -Encoding UTF8)){
    $s=$raw.Trim(); if(-not $s -or $s.StartsWith('#')){continue}
    if($s.Contains('*') -or $s.Contains('?') -or $s.StartsWith('/') -or (($s -split '[/\\]') -contains '..')){ Fail("unsafe delete " + $s) }
}

$lock=Load-Json 'HARNESS_LOCK.json'
foreach($prop in $lock.files.PSObject.Properties){
    $rel=$prop.Name; $expected=([string]$prop.Value).ToLowerInvariant()
    $p=Join-Path $Root ($rel -replace '/','\\')
    if(-not (Test-Path -LiteralPath $p -PathType Leaf)){ Fail("lock missing " + $rel) }
    $actual=(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant()
    if($actual -ne $expected){ Fail("lock mismatch " + $rel + ' expected=' + $expected + ' actual=' + $actual) }
}

$profiles=(Get-ChildItem -LiteralPath (Join-Path $Root 'profiles') -Filter '*.json' -File).Count
Write-Host 'HARNESS=PASS'
Write-Host ('VERSION=' + [string]$h.version)
Write-Host ('ARTIFACTS=' + $arts.Count)
Write-Host ('COVERAGE_ITEMS=' + $items.Count)
Write-Host ('PROFILES=' + $profiles)
Write-Host ('TABLE_PRESETS=' + $tableNames.Count)
Write-Host ('FIGURE_PRESETS=' + $figureNames.Count)
exit 0
