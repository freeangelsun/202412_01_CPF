param(
    [string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string]$ResultDir = "build/quality-gate/full-qa-closure"
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) { $ResultDir = Join-Path $Root $ResultDir }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

function Relative([string]$Path) {
    return $Path.Substring($Root.Length).TrimStart('\','/').Replace('\','/')
}
function Write-Csv([string]$Name, [object[]]$Rows) {
    $path = Join-Path $ResultDir $Name
    $text = if ($Rows.Count -eq 0) { '' } else { (($Rows | ConvertTo-Csv -NoTypeInformation) -join [Environment]::NewLine) + [Environment]::NewLine }
    [IO.File]::WriteAllText($path, $text, $Utf8NoBom)
}
function Sha256Text([string]$Value) {
    $bytes = [Text.Encoding]::UTF8.GetBytes($Value)
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
}
function Get-JsonValue([object]$Object, [string[]]$Names, $DefaultValue = '') {
    foreach ($name in $Names) {
        $property = $Object.PSObject.Properties[$name]
        if ($null -ne $property -and $null -ne $property.Value) {
            if ($property.Value -is [string] -and [string]::IsNullOrWhiteSpace([string]$property.Value)) {
                continue
            }
            return $property.Value
        }
    }
    return $DefaultValue
}

$tracked = @(& git -C $Root ls-files)
if ($LASTEXITCODE -ne 0) { throw 'git ls-files 실패' }
$head = (& git -C $Root rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) { throw 'git rev-parse HEAD 실패' }

# Java Source를 한 번만 읽어 Symbol 참조 횟수를 계산합니다.
$javaFiles = @($tracked | Where-Object { $_ -match '^cpf-[^/]+/(?:[^/]+/)*src/(main|test)/java/.+[.]java$' })
$javaText = @{}
$symbolCount = @{}
foreach ($rel in $javaFiles) {
    $path = Join-Path $Root $rel
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
    $text = [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
    $javaText[$rel] = $text
    foreach ($match in [regex]::Matches($text, '\b[A-Z][A-Za-z0-9_]{2,}\b')) {
        $symbol = $match.Value
        $symbolCount[$symbol] = 1 + [int]($symbolCount[$symbol] ?? 0)
    }
}

$modulePackage = [Collections.Generic.List[object]]::new()
foreach ($rel in $javaFiles | Where-Object { $_ -match '/src/main/java/' }) {
    $text = $javaText[$rel]
    $module = $rel.Split('/')[0]
    $package = if ($text -match '(?m)^package\s+([A-Za-z0-9_.]+);') { $Matches[1] } else { '' }
    $typeName = [IO.Path]::GetFileNameWithoutExtension($rel)
    $segments = @($package.Split('.') | Where-Object { $_ })
    $feature = if ($segments.Count -ge 4) { $segments[3] } else { $module }
    $isApi = $package -match '(^|[.])api([.]|$)'
    $isSpi = $package -match '(^|[.])spi([.]|$)'
    $isInternal = $package -match '(^|[.])(internal|common)([.]|$)' -and -not $isApi -and -not $isSpi
    $consumerCount = [Math]::Max(0, [int]($symbolCount[$typeName] ?? 0) - 1)
    $testPath = $rel.Replace('/src/main/java/','/src/test/java/').Replace('.java','Test.java')
    $suspicious = $package -match '(^|[.])(util|helper|manager)([.]|$)'
    $modulePackage.Add([ordered]@{
        module=$module; domain=($segments | Select-Object -Skip 2 -First 1); feature=$feature
        currentPackage=$package; targetPackage=if($suspicious){'업무 기능 Owner 기준 재확인'}else{$package}
        owner=$module; publicApi=$isApi; spi=$isSpi; internal=$isInternal
        consumerCount=$consumerCount; dbOwner='재확인 필요'; uiOwner='재확인 필요'
        test=if($tracked -contains $testPath){$testPath}else{''}
        generatorManaged=($rel -match 'generated|template' -or $module -in @('cpf-account','cpf-member'))
        drift=if($suspicious){'common/util/helper/manager 오남용 후보'}else{''}
        status=if([string]::IsNullOrWhiteSpace($package)){'실패'}elseif($consumerCount -eq 0 -and ($isApi -or $isSpi)){'재확인 필요'}else{'미검증'}
        requiredAction=if($consumerCount -eq 0 -and ($isApi -or $isSpi)){'Reflection·AutoConfiguration·ServiceLoader 포함 실제 Consumer 확인'}elseif($suspicious){'업무 규칙 은닉 여부 확인'}else{'최신 SHA Build·ArchUnit Evidence 연결'}
        path=$rel
    }) | Out-Null
}

$generatedParity = [Collections.Generic.List[object]]::new()
$manifestPaths = @($tracked | Where-Object { $_ -match '^cpf-[^/]+/manifest/domain-manifest[.]json$' })
foreach ($manifestRel in $manifestPaths) {
    $manifest = Get-Content -LiteralPath (Join-Path $Root $manifestRel) -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    $module = $manifestRel.Split('/')[0]
    $domain = [string](Get-JsonValue $manifest @('domainName') $module)
    $systemCode = [string](Get-JsonValue $manifest @('systemCode','domainIdCode','moduleCode') '')
    $packageRoot = [string](Get-JsonValue $manifest @('packageName','basePackage') '')
    $capabilities = Get-JsonValue $manifest @('capabilities') @()
    $generatorVersion = [string](Get-JsonValue $manifest @('generatorVersion') 'manifest ownership 확인')
    $files = @($tracked | Where-Object { $_.StartsWith("$module/") -and $_ -notmatch '/build/' })
    $normalizers = @(
        $domain, $systemCode, $packageRoot,
        [string](Get-JsonValue $manifest @('projectName','artifactId') ''),
        [string](Get-JsonValue $manifest @('schemaName','schema') ''),
        [string](Get-JsonValue $manifest @('tablePrefix') ''),
        [string](Get-JsonValue $manifest @('port') '')
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object Length -Descending
    $contract = [Text.StringBuilder]::new()
    foreach ($rel in $files | Sort-Object) {
        $path = Join-Path $Root $rel
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        $value = $rel + "`n" + [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
        foreach ($token in $normalizers) { $value = $value.Replace($token, '<DOMAIN_METADATA>') }
        [void]$contract.AppendLine($value)
    }
    $generatedParity.Add([ordered]@{
        domain=$domain; systemCode=$systemCode
        capability=($capabilities | ConvertTo-Json -Compress -Depth 10)
        generatorVersion=$generatorVersion
        normalizedTreeHash=Sha256Text $contract.ToString(); fileCount=$files.Count
        build='미검증'; runtime='미검증'; db='미검증'; local='미검증'; remote='미검증'
        test='미검증'; guide='미검증'; drift='pairwise parity gate에서 판정'; status='미검증'
    }) | Out-Null
}
foreach ($expected in @(
    [ordered]@{domain='account';systemCode='ACC'},
    [ordered]@{domain='member';systemCode='MBR'},
    [ordered]@{domain='external';systemCode='EXS'})) {
    if (-not ($generatedParity.domain -contains $expected.domain)) {
        $generatedParity.Add([ordered]@{
            domain=$expected.domain; systemCode=$expected.systemCode; capability='Generator lifecycle에서 생성 검증'
            generatorVersion=''; normalizedTreeHash=''; fileCount=0; build='미검증'; runtime='미검증'; db='미검증'
            local='미검증'; remote='미검증'; test='미검증'; guide='미검증'
            drift='Repository 상시 Module이 아니면 임시 생성 결과 Evidence 필요'; status='미검증'
        }) | Out-Null
    }
}

$menuUi = [Collections.Generic.List[object]]::new()
foreach ($frontend in @('cpf-admin/frontend','cpf-biz-admin/frontend')) {
    $owner = if ($frontend.StartsWith('cpf-admin')) { 'ADM' } else { 'BZA' }
    $routeFiles = @($tracked | Where-Object { $_.StartsWith("$frontend/") -and $_ -match '(routes|router).*[.](ts|js)$' })
    $routeText = ($routeFiles | ForEach-Object { [IO.File]::ReadAllText((Join-Path $Root $_), [Text.Encoding]::UTF8) }) -join "`n"
    $pages = @($tracked | Where-Object { $_.StartsWith("$frontend/src/") -and $_ -match '(Page|View)[.]vue$' })
    foreach ($rel in $pages) {
        $text = [IO.File]::ReadAllText((Join-Path $Root $rel), [Text.Encoding]::UTF8)
        $page = [IO.Path]::GetFileNameWithoutExtension($rel)
        $buttonCount = ([regex]::Matches($text,'<button\b')).Count
        $rawPre = $text -match '(?is)<pre\b[^>]*>.*?(JSON[.]stringify|\{\{\s*[A-Za-z0-9_.]*(Result|result|raw)[A-Za-z0-9_.]*\s*\}\}).*?</pre>'
        $routeFound = $routeText -match [regex]::Escape($page)
        $menuUi.Add([ordered]@{
            console=$owner; menuGroup='route/menu seed 대조 필요'; menu=$page; route=if($routeFound){'연결됨'}else{'고아 후보'}
            page=$rel; purpose='Source JavaDoc·화면 제목 기준 재확인'; requiredFunction='검색·Paging·상세·오류·권한·감사'
            buttons=$buttonCount; permission=if($text -match 'can(Read|Write|Delete)|permission|allowed'){'표식 있음'}elseif($buttonCount -gt 0){'재확인 필요'}else{'해당 없음'}
            api=if($text -match '/(adm|bza)/api/'){'직접 표식 있음'}else{'Mixin/Store 연결 재확인'}
            owner=$owner; upload=($text -match 'upload|업로드'); download=($text -match 'download|다운로드|CSV|XLSX')
            failureUx=if($text -match 'error|실패|uiMessage|role="alert"'){'표식 있음'}else{'재확인 필요'}
            audit=if($text -match 'reason|사유|audit|감사'){'표식 있음'}else{'재확인 필요'}
            e2e='미검증'; status=if(-not $routeFound -or $rawPre){'재확인 필요'}else{'미검증'}
            issue=if($rawPre){'Raw JSON <pre> 운영화면 후보'}elseif(-not $routeFound){'Page 없는 Route 또는 고아 Page 후보'}else{''}
        }) | Out-Null
    }
}

$garbage = [Collections.Generic.List[object]]::new()
$patterns = @(
    '(?i)(^|/)(tmp|temp|backup|old|copy)(/|$)', '(?i)[.](zip|bak|tmp|log)$',
    '(?i)(^|/)(build|target|out|node_modules|dist)/', '(?i)(Tmp|Old|Copy|Backup)[A-Za-z0-9_]*[.](java|ts|vue)$'
)
foreach ($rel in $tracked) {
    $matched = $false
    foreach ($pattern in $patterns) { if ($rel -match $pattern) { $matched=$true; break } }
    if (-not $matched) { continue }
    $garbage.Add([ordered]@{
        path=$rel; type='Tracked garbage/legacy candidate'; currentRole='재확인 필요'; consumer='미확인'
        candidateReason='Repository Hygiene pattern match'; reflectionConfig='미검증'; generatorImpact='미검증'
        replacement='미확인'; deletion='재확인 필요'; regressionTest='미검증'; evidence='최신 SHA 필요'
    }) | Out-Null
}

Write-Csv 'MODULE_PACKAGE_MATRIX.csv' @($modulePackage)
Write-Csv 'GENERATED_DOMAIN_PARITY_MATRIX.csv' @($generatedParity)
Write-Csv 'MENU_UI_MATRIX.csv' @($menuUi)
Write-Csv 'GARBAGE_REMOVAL_MATRIX.csv' @($garbage)
$manifest = [ordered]@{
    generatedAt=[DateTimeOffset]::Now.ToString('o'); exactSha=$head
    modulePackageCount=$modulePackage.Count; generatedDomainCount=$generatedParity.Count
    menuUiCount=$menuUi.Count; garbageCandidateCount=$garbage.Count
    status='미검증'; note='Matrix는 후보 탐지 결과이며 Build·DB·Browser·Multi-instance Evidence로 재판정해야 합니다.'
}
[IO.File]::WriteAllText((Join-Path $ResultDir 'MATRIX_MANIFEST.json'), ($manifest | ConvertTo-Json -Depth 10) + [Environment]::NewLine, $Utf8NoBom)
Write-Host "[PASS] Full QA closure matrices exported: $ResultDir"
