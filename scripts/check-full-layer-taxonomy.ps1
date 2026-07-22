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
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$modules = @(
    'cpf-core', 'cpf-gateway', 'cpf-common', 'cpf-admin', 'cpf-biz-admin',
    'cpf-member', 'cpf-account', 'cpf-batch', 'cpf-reference', 'cpf-external'
)
$items = [System.Collections.Generic.List[object]]::new()
$failures = [System.Collections.Generic.List[object]]::new()

function Get-RelativePath([string] $Path) {
    $Path.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
}

function Resolve-Layer([string] $Path, [string] $Name, [string] $Kind, [string] $Text) {
    $value = "$Path/$Name".ToLowerInvariant()
    if ($Name -match 'Application$' -and $Text -match '@SpringBootApplication') { return 'application-entry' }
    if ($value -match '/controller/' -or $Name -match 'Controller$') { return 'controller' }
    if ($value -match '/facade/' -or $Name -match 'Facade$') { return 'facade' }
    if ($value -match '/application/' -or $Name -match '(UseCase|ApplicationService)$') { return 'application' }
    if ($value -match '/service/' -or $Name -match 'Service$') { return 'service' }
    if ($value -match '/repository/' -or $Name -match 'Repository$') { return 'repository' }
    if ($value -match '/dao/' -or $Name -match 'Dao$') { return 'dao' }
    if ($value -match '/mapper/' -or $Name -match 'Mapper$') { return 'mapper' }
    if ($value -match '/assembler/' -or $Name -match '(Assembler|Converter)$') { return 'assembler' }
    if ($value -match '/client/' -or $Name -match 'Client$') { return 'client' }
    if ($value -match '/adapter/' -or $Name -match 'Adapter$') { return 'adapter' }
    if ($value -match '/batch/' -or $value -match '/job/' -or $Name -match '(Batch|Job|Step|Tasklet|Worker|Scheduler|Launcher|Listener)$') { return 'batch-worker' }
    if ($value -match '/config/' -or $Name -match '(Configuration|Config|AutoConfiguration)$') { return 'configuration' }
    if ($Name -match '(Properties|Property)$') { return 'properties' }
    if ($value -match '/spi/' -or $Name -match '(Spi|Port|Provider|Strategy|Policy|Resolver|Factory)$') { return 'spi' }
    if ($value -match '/security/' -or $Name -match '(Security|Authentication|Authorization|Filter|Interceptor)$') { return 'security' }
    if ($value -match '/dto/' -or $Name -match 'Dto$') { return 'dto' }
    if ($value -match '/request/' -or $Name -match 'Request$') { return 'request' }
    if ($value -match '/response/' -or $Name -match '(Response|Result)$') { return 'response' }
    if ($value -match '/entity/' -or $Name -match 'Entity$') { return 'entity' }
    if ($value -match '/model/' -or $Name -match '(Model|Command|Query|Criteria|Condition|Page)$') { return 'model' }
    if ($value -match '/event/' -or $Name -match 'Event$') { return 'event' }
    if ($value -match '/exception/' -or $Name -match '(Exception|Error)$') { return 'exception' }
    if ($value -match '/constant/' -or $Kind -eq 'enum') { return 'constant-enum' }
    if ($value -match '/util/' -or $Name -match '(Utils|Util|Helper|Codec|Parser|Validator|Generator|Masker|Sanitizer)$') { return 'utility' }
    if ($value -match '/base/' -or $Name -match '^\w+Base\w*$') { return 'base-contract' }
    if ($value -match '/edu/' -or $Name -match '(Education|Sample|Catalog)') { return 'education' }
    if ($Kind -eq '@interface') { return 'annotation' }
    return 'domain-component'
}

function Resolve-ExtensionMode([string] $Kind, [string] $Tail, [string] $Text, [string] $Layer) {
    if ($Tail -match '\bextends\s+[a-zA-Z0-9_.,<>? ]+') { return 'extends' }
    if ($Tail -match '\bimplements\s+[a-zA-Z0-9_.,<>? ]+') { return 'implements' }
    if ($Kind -in @('record', 'enum', '@interface')) { return 'not-applicable' }
    if ($Kind -eq 'interface') { return 'not-applicable' }
    if ($Layer -in @('dto', 'request', 'response', 'model', 'constant-enum', 'annotation')) { return 'not-applicable' }
    if ($Text -match '(?m)^\s*private\s+(?:final\s+)?[a-zA-Z0-9_.$<>, ?\[\]]+\s+\w+\s*(?:[;=])' -or
        $Text -match '@(Component|Service|Repository|Controller|Configuration|RestController|ControllerAdvice|ConfigurationProperties)\b') {
        return 'composition'
    }
    return 'not-applicable'
}

foreach ($module in $modules) {
    $sourceRoot = Join-Path $Root "$module/src"
    if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
        $failures.Add([ordered]@{ module = $module; path = "$module/src"; reason = 'Java source 루트가 없습니다.' }) | Out-Null
        continue
    }

    Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter '*.java' | Where-Object {
        $_.FullName -notmatch '\\build\\|\\generated\\'
    } | ForEach-Object {
        $file = $_
        $relative = Get-RelativePath $file.FullName
        $text = [IO.File]::ReadAllText($file.FullName, [Text.Encoding]::UTF8)
        $packageName = if ($text -match '(?m)^package\s+([a-zA-Z0-9_.]+);') { $Matches[1] } else { '' }
        $declarations = [regex]::Matches(
            $text,
            '(?m)^(?:public\s+|protected\s+|private\s+)?(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+|static\s+)*(class|interface|record|enum|@interface)\s+([A-Za-z_$][A-Za-z0-9_$]*)([^\r\n{;]*)'
        )
        if ($declarations.Count -eq 0) {
            $failures.Add([ordered]@{ module = $module; path = $relative; reason = 'Java 타입 선언을 분류하지 못했습니다.' }) | Out-Null
            return
        }

        foreach ($declaration in $declarations) {
            $kind = $declaration.Groups[1].Value
            $name = $declaration.Groups[2].Value
            $tail = $declaration.Groups[3].Value
            $layer = Resolve-Layer $relative $name $kind $text
            $extensionMode = Resolve-ExtensionMode $kind $tail $text $layer
            $item = [ordered]@{
                module = $module
                sourceSet = if ($relative -match '/src/test/') { 'test' } else { 'main' }
                path = $relative
                package = $packageName
                typeName = $name
                typeKind = $kind
                layer = $layer
                extensionMode = $extensionMode
            }
            $items.Add($item) | Out-Null
            if ([string]::IsNullOrWhiteSpace($layer) -or $extensionMode -notin @('extends', 'implements', 'composition', 'not-applicable')) {
                $failures.Add([ordered]@{ module = $module; path = $relative; typeName = $name; reason = '계층 또는 확장 방식을 분류하지 못했습니다.' }) | Out-Null
            }
        }
    }
}

$summary = @($items | Group-Object layer | Sort-Object Name | ForEach-Object {
    [ordered]@{ layer = $_.Name; count = $_.Count }
})
$modeSummary = @($items | Group-Object extensionMode | Sort-Object Name | ForEach-Object {
    [ordered]@{ extensionMode = $_.Name; count = $_.Count }
})
$result = [ordered]@{
    schemaVersion = '1.0'
    generatedAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss.fffK')
    status = if ($failures.Count -eq 0) { 'DONE' } else { 'FAILED' }
    checkedTypeCount = $items.Count
    unclassifiedCount = $failures.Count
    layerSummary = $summary
    extensionModeSummary = $modeSummary
    failures = @($failures)
    items = @($items)
}
$resultPath = Join-Path $ResultDir 'full-layer-taxonomy.sanitized.json'
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)

if ($failures.Count -gt 0) {
    throw "전체 계층 분류 검사가 실패했습니다. unclassifiedCount=$($failures.Count)"
}
Write-Host "전체 계층 분류 검사 완료: type=$($items.Count), unclassified=0"
