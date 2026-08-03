param(
    [string]$ProjectRoot=(Resolve-Path "$PSScriptRoot/../..").Path,
    [string]$BaseSha="dafe5c0e5260ea8149234e8ab2e75347e75338c1",
    [switch]$SkipHarness
)
$ErrorActionPreference='Stop'
$ProjectRoot=(Resolve-Path $ProjectRoot).Path
Set-Location $ProjectRoot

# 최신 Starter 정본은 단일 Canonical Catalog다. 기존 QA38 구조·SQL·중복·Runtime
# 검사를 제거하지 않고, 그보다 먼저 Catalog/Settings/Profile/Public BOM 파생 정합성을
# fail-closed로 검증한다.
& pwsh -NoProfile -File '.\cpf-tools\scripts\verify-cpf-starter-catalog-truth.ps1' `
    -Root $ProjectRoot `
    -JsonOutput '.\build\reports\cpf\starter-catalog-truth.json'
if($LASTEXITCODE-ne 0){throw 'Canonical starter catalog truth gate failed'}

$protected=@('cpf-docs/deliverables','cpf-docs/guides','cpf-docs/environment/docker','cpf-tools/environment/docker-development-test')
# 보호 경로의 기존 Working Tree 변경은 다른 작업자의 산출물일 수 있으므로 실패 조건으로 사용하지 않는다.
# QA38 Change/Delete Manifest가 보호 경로를 포함하지 않는지는 적용 스크립트와 구조 Gate에서 별도로 검증한다.

$requiredModules=@(
'cpf-starters/foundation/base','cpf-starters/data/persistence-jdbc','cpf-starters/data/persistence-mybatis','cpf-starters/aop-service-access',
'cpf-starters/openapi-webmvc','cpf-starters/security/resource-server','cpf-starters/security/session-jdbc','cpf-starters/security/service-identity',
'cpf-starters/messaging/reliability-jdbc','cpf-starters/messaging/kafka','cpf-starters/messaging/rabbitmq','cpf-starters/messaging/jms',
'cpf-starters/messaging/ibm-mq','cpf-starters/integration/tcp','cpf-starters/integration/fixedlength-core',
'cpf-starters/integration/iso8583','cpf-starters/file/sftp','cpf-starters/notification/dispatch','cpf-starters/notification/email',
'cpf-starters/notification/sms-spi','cpf-tools/generator/contracts/capability-profiles.json','cpf-tools/verification/core-only-consumer'
)
foreach($path in $requiredModules){if(-not(Test-Path -LiteralPath $path)){throw "QA38 필수 구현이 없습니다: $path"}}

$deleteManifest='cpf-docs/work/manifest/CPF_QA38_DELETE_MANIFEST.txt'
foreach($path in Get-Content -LiteralPath $deleteManifest -Encoding UTF8){
    $relative=$path.Trim();if(-not $relative){continue}
    if(Test-Path -LiteralPath $relative){throw "Starter 대체 후 Legacy 경로가 남아 있습니다: $relative"}
}

$core=Get-Content -LiteralPath 'cpf-core/build.gradle' -Raw -Encoding UTF8
foreach($forbidden in @('mybatis-spring-boot-starter','spring-boot-starter-aspectj','aspectjweaver','springdoc-openapi','opentelemetry-sdk','opentelemetry-exporter-otlp','commons-compress')){
    if($core.Contains($forbidden)){throw "cpf-core에 선택 Runtime Dependency가 남아 있습니다: $forbidden"}
}
$common=Get-Content -LiteralPath 'cpf-common/build.gradle' -Raw -Encoding UTF8
foreach($forbidden in @('mybatis-spring-boot-starter','spring-data-redis','lettuce-core','caffeine','poi-ooxml','hibernate-validator')){
    if($common -match "(?m)^\s*(api|implementation|runtimeOnly)\s+.*$([regex]::Escape($forbidden))"){throw "cpf-common에 기술 Runtime Dependency가 남아 있습니다: $forbidden"}
}

$coreImports=Get-Content -LiteralPath 'cpf-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports' -Raw -Encoding UTF8
foreach($forbidden in @('CpfDataSourceConfig','CpfMyBatisConfig','CpfAopConfig','CpfOpenApiAutoConfiguration','CpfSecurityAutoConfiguration','CpfAttachmentAutoConfiguration','CpfLogPolicyAutoConfiguration','CpfServiceCallAutoConfiguration','CpfRuntimeControlAutoConfiguration')){
    if($coreImports.Contains($forbidden)){throw "cpf-core AutoConfiguration에 선택 Runtime이 남아 있습니다: $forbidden"}
}

$settings=Get-Content -LiteralPath 'settings.gradle' -Raw -Encoding UTF8
$projectMatches=[regex]::Matches($settings,"project\(':(?<name>[^']+)'\)\.projectDir\s*=\s*file\('(?<path>[^']+)'\)")
if($projectMatches.Count -lt 50){throw "Starter project mapping 수가 비정상입니다: $($projectMatches.Count)"}
foreach($match in $projectMatches){$path=$match.Groups['path'].Value;if(-not(Test-Path -LiteralPath $path -PathType Container)){throw "settings.gradle projectDir이 없습니다: $path"}}

$catalog=Get-Content -LiteralPath 'cpf-tools/generator/contracts/capability-profiles.json' -Raw -Encoding UTF8|ConvertFrom-Json
if(@($catalog.profiles).Count -lt 13){throw "Capability Profile이 부족합니다: $(@($catalog.profiles).Count)"}
$defaultByCapability=@{}
foreach($profile in $catalog.profiles){
    if([string]::IsNullOrWhiteSpace($profile.profileId)){throw 'profileId가 비어 있습니다.'}
    if(@($profile.resolvedStarters).Count -eq 0){throw "resolvedStarters가 비어 있습니다: $($profile.profileId)"}
    foreach($starter in $profile.resolvedStarters){if(-not($settings.Contains("'$starter'"))){throw "Profile Starter가 settings.gradle에 없습니다: $starter"}}
}

$vendors=@('mariadb','postgresql','oracle')
$dbModules=@('messaging-reliability-jdbc','integration-sftp','notification','integration-webhook')
foreach($module in $dbModules){
    foreach($vendor in $vendors){
        $root="cpf-starters/$module/src/main/resources/db/$vendor"
        if(-not(Test-Path -LiteralPath "$root/migration")){throw "DB migration 경로가 없습니다: $root/migration"}
        if(-not(Test-Path -LiteralPath "$root/rollback")){throw "DB rollback 경로가 없습니다: $root/rollback"}
        if(@(Get-ChildItem -LiteralPath "$root/migration" -File -Filter '*.sql').Count -eq 0){throw "DB migration SQL이 없습니다: $root"}
        if(@(Get-ChildItem -LiteralPath "$root/rollback" -File -Filter '*.sql').Count -eq 0){throw "DB rollback SQL이 없습니다: $root"}
    }
}

$sourceFiles=@(Get-ChildItem -Path 'cpf-starters','cpf-tools/generator' -Recurse -File -Include *.java,*.ps1,*.py,*.gradle,*.json,*.sql)
$badMarkerPattern=(('TO'+'DO')+'|'+('FIX'+'ME')+'|'+('PLAN'+'NED')+'|not\s+implemented|'+('place'+'holder')+'|'+('dum'+'my'))
$bad=@($sourceFiles|Select-String -Pattern $badMarkerPattern)
$unsupported=@($sourceFiles|Where-Object{$_.FullName -match '[\/]src[\/]main[\/]'}|Select-String -Pattern ('throw\s+new\s+'+('Unsupported'+'OperationException')+'\s*\('))
if(($bad.Count+$unsupported.Count)-gt 0){@($bad+$unsupported)|ForEach-Object{"$($_.Path):$($_.LineNumber): $($_.Line.Trim())"};throw '미구현 Marker가 남아 있습니다.'}

$python=(Get-Command python -ErrorAction SilentlyContinue)
if(-not $python){$python=(Get-Command py -ErrorAction SilentlyContinue)}
if(-not $python){throw 'Python이 없어 QA38 구조/SQL/중복 Gate를 실행할 수 없습니다.'}
foreach($script in @('verify-qa38-structure.py','verify-qa38-sql-parity.py','verify-qa38-java-duplicates.py')){
    & $python.Source (Join-Path $ProjectRoot "cpf-tools/verification/qa38/$script") $ProjectRoot
    if($LASTEXITCODE-ne 0){throw "QA38 Python Gate 실패: $script"}
}

$hashFile='cpf-docs/work/manifest/CPF_QA38_FILES.sha256'
if(Test-Path -LiteralPath $hashFile){
    foreach($line in Get-Content -LiteralPath $hashFile -Encoding UTF8){
        if($line -notmatch '^(?<hash>[0-9a-f]{64})  (?<path>.+)$'){throw "Hash Manifest 형식 오류: $line"}
        $path=$Matches['path'];$expected=$Matches['hash']
        if(-not(Test-Path -LiteralPath $path -PathType Leaf)){throw "Hash 대상 파일이 없습니다: $path"}
        $actual=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
        if($actual -ne $expected){throw "File hash 불일치: $path"}
    }
}

if(-not $SkipHarness){
    if(-not(Get-Command javac -ErrorAction SilentlyContinue)){throw 'javac가 없어 QA38 순수 Runtime Harness를 실행할 수 없습니다.'}
    & pwsh -NoProfile -File '.\cpf-tools\verification\qa38\run-qa38-pure-runtime-harness.ps1' -ProjectRoot $ProjectRoot
    if($LASTEXITCODE-ne 0){throw 'QA38 pure runtime harness failed'}
    & pwsh -NoProfile -File '.\cpf-tools\verification\qa38\run-qa38-messaging-identity-harness.ps1' -ProjectRoot $ProjectRoot
    if($LASTEXITCODE-ne 0){throw 'QA38 messaging/identity harness failed'}
}

git diff --check
if($LASTEXITCODE-ne 0){throw 'git diff --check 실패'}
Write-Host 'QA38 STARTER CLOSURE VERIFICATION PASS'
