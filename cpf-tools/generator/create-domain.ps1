param(
    [string] $DomainName = "",
    [string] $SystemCode = "",
    [string] $ModuleCode = "",
    [string] $Root = "",
    [string] $ModuleName = "",
    [string] $DomainIdCode = "",
    [string] $PackageName = "",
    [string] $BasePackage = "",
    [string] $SchemaName = "",
    [string] $TablePrefix = "",
    [ValidateRange(1024, 65535)]
    [int] $Port = 8080,
    [ValidateSet("Y", "N")]
    [string] $Online = "Y",
    [ValidateSet("Y", "N")]
    [string] $Database = "Y",
    [Alias("DbVendor")]
    [ValidateSet("mariadb", "mysql", "postgresql", "oracle", "sqlserver")]
    [string] $DatabaseVendor = "mariadb",
    [string] $DatabaseHost = "127.0.0.1",
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $DatabaseSchema = "",
    [string] $DatabaseAdminUsername = "",
    [string] $DatabaseMigrationUsername = "",
    [string] $DatabaseRuntimeUsername = "",
    [string] $DatabaseClientPath = "",
    [string] $Capabilities = "",
    [ValidateSet("Y", "N")]
    [string] $Batch = "N",
    [ValidateSet("Y", "N")]
    [string] $CenterCut = "N",
    [ValidateSet("Y", "N")]
    [string] $External = "N",
    [ValidateSet("Y", "N")]
    [string] $Messaging = "N",
    [ValidateSet("Y", "N")]
    [string] $File = "N",
    [ValidateSet("Y", "N")]
    [string] $SecurityAudit = "N",
    [ValidateSet("Y", "N")]
    [string] $Ui = "N",
    [ValidateSet("Y", "N")]
    [string] $BzaMenu = "N",
    [ValidateSet("Y", "N")]
    [string] $ProductionProfile = "N",
    [switch] $ProvisionDatabase,
    [string] $OutputDir = "",
    [switch] $DryRun,
    [switch] $GeneratePatch,
    [switch] $Apply,
    [switch] $AllowReserved
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
} else {
    $Root = (Resolve-Path -LiteralPath $Root).Path
}
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-StatusText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-StatusText @(0xC644, 0xB8CC)
$StatusFailed = New-StatusText @(0xC2E4, 0xD328)

function Normalize-DomainName {
    param([string] $Value)
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "DomainName은 필수입니다. 이전 호환 입력인 ModuleCode도 사용할 수 있습니다."
    }
    $normalized = $Value.Trim().ToLowerInvariant()
    if ($normalized -notmatch '^[a-z][a-z0-9]{1,29}$') {
        throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
    }
    return $normalized
}

function ConvertTo-ClassName {
    param([string] $Value)
    $segments = @($Value -split '[-_]' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    return -join ($segments | ForEach-Object {
            $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1).ToLowerInvariant()
        })
}

function Write-Utf8 {
    param(
        [string] $Path,
        [string] $Content
    )
    $parent = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    [System.IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Test-TextExists {
    param(
        [string] $Path,
        [string] $Text
    )
    if (-not (Test-Path -LiteralPath $Path)) {
        return $false
    }
    $content = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    return $content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
}

$requestedDomainName = if ([string]::IsNullOrWhiteSpace($DomainName)) { $ModuleCode } else { $DomainName }
$module = Normalize-DomainName $requestedDomainName
$projectName = "cpf-$module"
$Dollar = '$'
if ([string]::IsNullOrWhiteSpace($SystemCode)) {
    $SystemCode = $DomainIdCode
}
if ([string]::IsNullOrWhiteSpace($SystemCode)) {
    $SystemCode = if ($module.Length -ge 3) {
        $module.Substring(0, 3).ToUpperInvariant()
    } else {
        $module.ToUpperInvariant().PadRight(3, 'X')
    }
}
$SystemCode = $SystemCode.Trim().ToUpperInvariant()
if ($SystemCode -notmatch '^[A-Z][A-Z0-9]{2}$') {
    throw "SystemCode는 영문자로 시작하는 정확히 3자리 영문 대문자·숫자여야 합니다."
}
$DomainIdCode = $SystemCode
$ModuleUpper = $SystemCode

if ([string]::IsNullOrWhiteSpace($ModuleName)) {
    $ModuleName = ConvertTo-ClassName $module
}
if ($ModuleName -notmatch '^[A-Z][A-Za-z0-9]{1,49}$') {
    throw "ModuleName은 영문 대문자로 시작하는 2~50자리 Java class 이름이어야 합니다."
}
if (-not [string]::IsNullOrWhiteSpace($PackageName) -and
        -not [string]::IsNullOrWhiteSpace($BasePackage) -and
        $PackageName.Trim() -ne $BasePackage.Trim()) {
    throw "PackageName과 이전 호환 입력 BasePackage가 서로 다릅니다."
}
if ([string]::IsNullOrWhiteSpace($PackageName)) {
    $PackageName = $BasePackage
}
if ([string]::IsNullOrWhiteSpace($PackageName)) {
    $PackageName = "com.cpf.$module"
}
$PackageName = $PackageName.Trim()
if ($PackageName -notmatch '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$') {
    throw "PackageName은 com.cpf 하위의 유효한 Java package여야 합니다."
}
# BasePackage는 이전 호출자와 생성 source template을 위한 호환 변수입니다.
$BasePackage = $PackageName

if ([string]::IsNullOrWhiteSpace($TablePrefix)) {
    $TablePrefix = $SystemCode.ToLowerInvariant()
}
$TablePrefix = $TablePrefix.Trim().ToLowerInvariant()
if ($TablePrefix -notmatch '^[a-z][a-z0-9_]{1,19}$') {
    throw "TablePrefix는 영문 소문자로 시작하는 2~20자리 영문 소문자·숫자·밑줄이어야 합니다."
}
if ([string]::IsNullOrWhiteSpace($SchemaName)) {
    $SchemaName = "${TablePrefix}DB"
}
$SchemaName = $SchemaName.Trim()
if ($SchemaName -notmatch '^[A-Za-z][A-Za-z0-9_]{1,29}$') {
    throw "SchemaName은 영문자로 시작하는 2~30자리 영문·숫자·밑줄이어야 합니다."
}
$OnlineEnabled = $Online -eq "Y"
$DatabaseEnabled = $Database -eq "Y"
$DatabaseVendor = $DatabaseVendor.ToLowerInvariant()
$vendorPortDefaults = @{ mariadb = 3306; mysql = 3306; postgresql = 5432; oracle = 1521; sqlserver = 1433 }
if ($DatabasePort -le 0) { $DatabasePort = [int]$vendorPortDefaults[$DatabaseVendor] }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $DatabaseName = if ($DatabaseVendor -eq "oracle") { "FREEPDB1" } else { $SchemaName }
}
if ([string]::IsNullOrWhiteSpace($DatabaseSchema)) { $DatabaseSchema = $SchemaName }
if ([string]::IsNullOrWhiteSpace($DatabaseAdminUsername)) {
    $DatabaseAdminUsername = if ($DatabaseVendor -eq "postgresql") { "postgres" } elseif ($DatabaseVendor -eq "oracle") { "SYSTEM" } elseif ($DatabaseVendor -eq "sqlserver") { "sa" } else { "root" }
}
if ([string]::IsNullOrWhiteSpace($DatabaseMigrationUsername)) { $DatabaseMigrationUsername = "cpf_${module}_migration" }
if ([string]::IsNullOrWhiteSpace($DatabaseRuntimeUsername)) { $DatabaseRuntimeUsername = "cpf_${module}_app" }
$BatchEnabled = $Batch -eq "Y"
$CenterCutEnabled = $CenterCut -eq "Y"
$ExternalEnabled = $External -eq "Y"
$MessagingEnabled = $Messaging -eq "Y"
$FileEnabled = $File -eq "Y"
$SecurityAuditEnabled = $SecurityAudit -eq "Y"
$UiEnabled = $Ui -eq "Y"
$BzaMenuEnabled = $BzaMenu -eq "Y"
$ProductionProfileEnabled = $ProductionProfile -eq "Y"

if ($ProvisionDatabase -and -not $Apply) {
    throw "DB 실제 생성은 Repository에 생성 결과를 반영하는 -Apply와 함께 사용해야 합니다."
}
if ($ProvisionDatabase -and -not $DatabaseEnabled) {
    throw "ProvisionDatabase는 Database capability가 활성화된 Domain에서만 사용할 수 있습니다."
}

# Golden Generated Domain 기본값은 최소 업무 골격입니다.
# Online + Database + Local Call만 기본으로 두고 External/Batch/Messaging/File/UI/전용 Security Guard는
# 명시 capability로 요청할 때만 생성합니다. 표준 Header/transactionId/공통 Security/Audit는
# cpf-core/cpf-common의 공통 Filter/AOP를 사용하며 Domain마다 중복 Source를 만들지 않습니다.
# Capabilities를 명시하면 선택형 기능은 목록에 포함된 항목만 생성합니다.
# online과 local-call은 기본 골격이므로 기존 Online 입력과 DB/in-memory adapter 정책을 유지합니다.
if (-not [string]::IsNullOrWhiteSpace($Capabilities)) {
    $requestedCapabilities = @($Capabilities -split '[,; ]+' |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            ForEach-Object { $_.Trim().ToLowerInvariant() } |
            Sort-Object -Unique)
    $supportedCapabilities = @(
        'database', 'batch', 'center-cut', 'external', 'messaging', 'file', 'ui',
        'security', 'audit', 'local-call', 'remote-call'
    )
    $unknownCapabilities = @($requestedCapabilities | Where-Object { $_ -notin $supportedCapabilities })
    if ($unknownCapabilities.Count -gt 0) {
        throw "지원하지 않는 capability가 있습니다: $($unknownCapabilities -join ', ')"
    }
    $DatabaseEnabled = 'database' -in $requestedCapabilities
    $BatchEnabled = 'batch' -in $requestedCapabilities
    $CenterCutEnabled = 'center-cut' -in $requestedCapabilities
    $ExternalEnabled = ('external' -in $requestedCapabilities) -or ('remote-call' -in $requestedCapabilities)
    $MessagingEnabled = 'messaging' -in $requestedCapabilities
    $FileEnabled = 'file' -in $requestedCapabilities
    $UiEnabled = 'ui' -in $requestedCapabilities
    $SecurityAuditEnabled = ('security' -in $requestedCapabilities) -or ('audit' -in $requestedCapabilities)
}
$DataSourceJndiName = "java:comp/env/jdbc/cpf${ModuleName}DataSource"
$ModuleClassName = $ModuleName
$FeaturePackage = "$BasePackage.reference"
$FeatureClassPrefix = "${ModuleName}Reference"
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = if ($Apply) {
        Join-Path $Root $projectName
    } else {
        Join-Path $Root "build/domain-generator/$projectName"
    }
}

$targetModuleDir = Join-Path $Root $projectName
$settingsPath = Join-Path $Root "settings.gradle"
$packagePath = $BasePackage.Replace('.', '/')
$featurePackagePath = $FeaturePackage.Replace('.', '/')
$conflicts = New-Object System.Collections.Generic.List[string]

if (Test-Path -LiteralPath $targetModuleDir) {
    $conflicts.Add("module directory already exists: $module")
}
if (Test-TextExists -Path $settingsPath -Text "include '$projectName'") {
    $conflicts.Add("settings.gradle에 같은 모듈이 이미 등록되어 있습니다: $projectName")
}
# Platform/Generated Domain table-prefix collision은 특정 Vendor SQL 경로를 직접 읽지 않는다.
# generated database-schema-manifest.json이 현재 구현된 Platform schema의 vendor-neutral collision gate다.
$databaseSchemaManifestPath = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
if ($DatabaseEnabled -and (Test-Path -LiteralPath $databaseSchemaManifestPath -PathType Leaf)) {
    try {
        $databaseSchemaManifest = Get-Content -LiteralPath $databaseSchemaManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
        $prefixCollision = @($databaseSchemaManifest.tables | Where-Object {
            ([string]$_.tableName).ToLowerInvariant().StartsWith($TablePrefix + "_")
        })
        if ($prefixCollision.Count -gt 0) {
            $conflicts.Add("table prefix already appears in canonical schema manifest: $TablePrefix")
        }
    } catch {
        $conflicts.Add("database-schema-manifest.json을 해석할 수 없습니다: $databaseSchemaManifestPath")
    }
}
if (Test-Path -LiteralPath (Join-Path $Root "$projectName/src/main/java/$packagePath")) {
    $conflicts.Add("base package already exists: $BasePackage")
}

# Platform Module의 SystemCode는 고정 배열로 관리하지 않습니다. 현재 source 설정에서
# 동적으로 발견하여 Generated Domain metadata와 충돌하는 경우만 차단합니다.
if (-not $AllowReserved) {
    $platformConfigurationFiles = @(Get-ChildItem -LiteralPath $Root -Directory -Filter 'cpf-*' -ErrorAction SilentlyContinue |
            ForEach-Object {
                $resourceDirectory = Join-Path $_.FullName 'src/main/resources'
                if (Test-Path -LiteralPath $resourceDirectory -PathType Container) {
                    Get-ChildItem -LiteralPath $resourceDirectory -Recurse -File -Include '*.yml', '*.yaml' -ErrorAction SilentlyContinue
                }
            })
    foreach ($configurationFile in $platformConfigurationFiles) {
        $configurationText = [System.IO.File]::ReadAllText(
                $configurationFile.FullName,
                [System.Text.Encoding]::UTF8)
        $matches = [regex]::Matches(
                $configurationText,
                '(?im)^\s*module-id\s*:\s*(?:\$\{[^:}\r\n]+:)?([A-Z][A-Z0-9]{2})(?:\})?\s*$')
        if (@($matches | Where-Object { $_.Groups[1].Value -eq $SystemCode }).Count -gt 0) {
            $conflicts.Add(
                    "SystemCode가 현재 Platform Module 설정과 중복됩니다: $SystemCode ($($configurationFile.FullName))")
            break
        }
    }
}

$manifestFiles = @(Get-ChildItem -LiteralPath $Root -Filter 'domain-manifest.json' -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notmatch '\\build\\|\\.git\\' })
foreach ($manifestFile in $manifestFiles) {
    try {
        $manifest = Get-Content -LiteralPath $manifestFile.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
        if ([string]$manifest.systemCode -eq $SystemCode -or [string]$manifest.moduleCode -eq $SystemCode) {
            $conflicts.Add("SystemCode가 기존 manifest와 중복됩니다: $SystemCode ($($manifestFile.FullName))")
        }
        if ([string]$manifest.domainName -eq $module -or [string]$manifest.projectName -eq $projectName) {
            $conflicts.Add("DomainName 또는 projectName이 기존 manifest와 중복됩니다: $module")
        }
    } catch {
        $conflicts.Add("기존 domain manifest를 해석할 수 없습니다: $($manifestFile.FullName)")
    }
}

$sourceRoots = @(Get-ChildItem -LiteralPath $Root -Directory -Filter 'cpf-*' -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'src' } | Where-Object { Test-Path -LiteralPath $_ })
foreach ($sourceRoot in $sourceRoots) {
    $candidateFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Include *.java,*.yml,*.yaml,*.xml -ErrorAction SilentlyContinue)
    foreach ($candidateFile in $candidateFiles) {
        $candidateText = [System.IO.File]::ReadAllText($candidateFile.FullName, [System.Text.Encoding]::UTF8)
        if ($candidateText.Contains("/api/v1/$module") -or $candidateText.Contains("package $BasePackage")) {
            $conflicts.Add("API route 또는 package가 기존 source와 충돌합니다: $($candidateFile.FullName)")
            break
        }
    }
}

$plan = [ordered]@{
    status = $StatusDone
    dryRun = [bool] $DryRun
    moduleCode = $ModuleUpper
    domainName = $module
    projectName = $projectName
    systemCode = $SystemCode
    moduleName = $ModuleName
    domainIdCode = $DomainIdCode
    packageName = $PackageName
    basePackage = $BasePackage
    schemaName = $SchemaName
    tablePrefix = $TablePrefix
    port = $Port
    online = $OnlineEnabled
    database = $DatabaseEnabled
    databaseVendor = $DatabaseVendor
    batch = $BatchEnabled
    external = $ExternalEnabled
    messaging = $MessagingEnabled
    file = $FileEnabled
    securityAudit = $SecurityAuditEnabled
    ui = $UiEnabled
    bzaMenu = $BzaMenuEnabled
    productionProfile = $ProductionProfileEnabled
    outputDir = $OutputDir
    generatePatch = $false
    applyMode = [bool] $Apply
    conflicts = @($conflicts)
    generatedFiles = @()
    patchFiles = @()
    patchCandidates = @()
}

if ($conflicts.Count -gt 0) {
    $plan.status = $StatusFailed
}

if ($DryRun -or $conflicts.Count -gt 0) {
    $plan | ConvertTo-Json -Depth 20
    if ($conflicts.Count -gt 0) {
        exit 1
    }
    exit 0
}

$javaRoot = Join-Path $OutputDir "src/main/java/$featurePackagePath"
$resourceRoot = Join-Path $OutputDir "src/main/resources"
$testRoot = Join-Path $OutputDir "src/test/java/$featurePackagePath"

$moduleBaseController = @"
package $BasePackage.common.base;

import com.cpf.core.api.base.CpfBaseController;

/**
 * $ModuleUpper Controller가 공유하는 주제영역 확장점입니다.
 *
 * <p>route와 Spring stereotype을 두지 않으며 주제영역 공통 정책만 제한적으로 추가합니다.</p>
 */
public abstract class ${ModuleClassName}BaseController extends CpfBaseController {
}
"@

$moduleBaseService = @"
package $BasePackage.common.base;

import com.cpf.core.api.base.CpfBaseService;

/**
 * $ModuleUpper Service가 공유하는 주제영역 확장점입니다.
 */
public abstract class ${ModuleClassName}BaseService extends CpfBaseService {
}
"@

$moduleFacadeContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfApplicationFacade;

/** $ModuleUpper Application Facade의 주제영역 계약입니다. */
public interface ${ModuleClassName}ApplicationFacade extends CpfApplicationFacade {
}
"@

$moduleRepositoryContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfRepositoryPort;

/**
 * $ModuleUpper Repository Port가 공통으로 확장하는 주제영역 계약입니다.
 *
 * @param <T> model 형식
 * @param <ID> 식별자 형식
 */
public interface ${ModuleClassName}RepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
}
"@

$moduleRequestContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfRequest;

/** $ModuleUpper 요청 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Request extends CpfRequest {
}
"@

$moduleResponseContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfResponse;

/** $ModuleUpper 응답 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Response extends CpfResponse {
}
"@

$controller = @"
package $FeaturePackage.controller;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.facade.${FeatureClassPrefix}Facade;
import $FeaturePackage.validation.${FeatureClassPrefix}SearchValidator;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * ${ModuleName} Golden Generated Domain 참조 API입니다.
 *
 * <p>transactionId, idempotencyKey, 표준 채널/사용자 Header, tracing과 공통 오류 변환은
 * cpf-core Filter/Context/Exception 계층이 처리합니다. 업무 Controller는 이를 Body에 중복으로
 * 받거나 직접 생성하지 않고 업무 입력 검증과 Facade 위임만 수행합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/$module/reference")
@Tag(name = "$ModuleUpper 생성 참조", description = "Generated Domain 공통 CRUD/Search/Paging/Validation/Transaction 참조 API")
public class ${FeatureClassPrefix}Controller extends ${ModuleClassName}BaseController {
    private final ${FeatureClassPrefix}Facade facade;
    private final ${FeatureClassPrefix}SearchValidator validator;

    public ${FeatureClassPrefix}Controller(
            ${FeatureClassPrefix}Facade facade,
            ${FeatureClassPrefix}SearchValidator validator) {
        this.facade = Objects.requireNonNull(facade, "facade는 필수입니다.");
        this.validator = Objects.requireNonNull(validator, "validator는 필수입니다.");
    }

    @GetMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0001", name = "${ModuleName}Search", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "search${ModuleName}Reference", summary = "생성 참조 목록 조회",
            description = "검색어, 페이징, 정렬 whitelist를 적용해 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> search(${FeatureClassPrefix}SearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }

    @PostMapping("/sample-items")
    @CpfOnlineTransaction(id = "O${DomainIdCode}IN0001", name = "${ModuleName}Create", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "create${ModuleName}SampleItem", summary = "Generated Domain Sample 등록",
            description = "transactionId/idempotencyKey는 표준 Header에서 Core가 수집합니다.")
    public ResponseEntity<Map<String, Object>> create(@RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.create(command));
    }

    @GetMapping("/sample-items/{sampleKey}")
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0002", name = "${ModuleName}Find", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "find${ModuleName}SampleItem", summary = "Generated Domain Sample 단건 조회")
    public ResponseEntity<Map<String, Object>> findBySampleKey(@PathVariable String sampleKey) {
        return ok(facade.findBySampleKey(sampleKey)
                .orElseThrow(() -> new CpfValidationException("Sample Item을 찾을 수 없습니다.")));
    }

    @PostMapping("/sample-items/{sampleItemId}/update")
    @CpfOnlineTransaction(id = "O${DomainIdCode}UP0001", name = "${ModuleName}Update", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "update${ModuleName}SampleItem", summary = "낙관적 잠금 Sample 수정")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable long sampleItemId,
            @RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.update(sampleItemId, command));
    }

    @PostMapping("/sample-items/{sampleItemId}/delete")
    @CpfOnlineTransaction(id = "O${DomainIdCode}DL0001", name = "${ModuleName}Delete", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "delete${ModuleName}SampleItem", summary = "낙관적 잠금 Sample 논리 삭제")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable long sampleItemId,
            @RequestBody Map<String, Object> request) {
        Object rawVersion = request.get("expectedVersion");
        if (!(rawVersion instanceof Number version)) {
            throw new CpfValidationException("expectedVersion은 필수입니다.");
        }
        facade.delete(sampleItemId, version.longValue());
        return ok(Map.of("deleted", true, "sampleItemId", sampleItemId));
    }

    @GetMapping("/sample-items/cursor")
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0003", name = "${ModuleName}Cursor", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "cursor${ModuleName}SampleItems", summary = "Cursor/Slice 조회")
    public ResponseEntity<CpfSlice<Map<String,Object>>> cursor(
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "20") int size) {
        return ok(facade.cursor(afterId, size));
    }

    @PostMapping("/sample-items/rollback-verify")
    @CpfOnlineTransaction(id = "O${DomainIdCode}TX0001", name = "${ModuleName}Rollback", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "rollback${ModuleName}SampleItem", summary = "Transaction rollback 검증")
    public ResponseEntity<Boolean> verifyRollback(@RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.verifyRollback(command));
    }
}
"@

$facade = @"
package $FeaturePackage.facade;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'ApplicationFacade;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.service.${FeatureClassPrefix}Service;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Controller와 업무 서비스를 분리하는 생성형 Domain 진입 Facade입니다. */
@Component
public class ${FeatureClassPrefix}Facade implements ${ModuleClassName}ApplicationFacade {
    private final ${FeatureClassPrefix}Service service;

    public ${FeatureClassPrefix}Facade(${FeatureClassPrefix}Service service) {
        this.service = Objects.requireNonNull(service, "service는 필수입니다.");
    }

    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) { return service.search(request); }
    public Map<String, Object> create(${FeatureClassPrefix}SampleCommand command) { return service.create(command); }
    public Optional<Map<String, Object>> findBySampleKey(String sampleKey) { return service.findBySampleKey(sampleKey); }
    public Map<String, Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command) { return service.update(sampleItemId, command); }
    public void delete(long sampleItemId, long expectedVersion) { service.delete(sampleItemId, expectedVersion); }
    public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) { return service.cursor(afterId, size); }
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command) { return service.verifyRollback(command); }
}
"@

$queryPortSource = @"
package $FeaturePackage.port;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'RepositoryPort;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;

import java.util.Optional;
import java.util.Map;

/** Local DB / Remote adapter 교체가 가능한 Generated Domain 업무 Port입니다. */
public interface ${FeatureClassPrefix}QueryPort extends ${ModuleClassName}RepositoryPort<Map<String, Object>, String> {
    Map<String, Object> search(${FeatureClassPrefix}SearchRequest request);
    Map<String, Object> create(${FeatureClassPrefix}SampleCommand command,
                               String transactionId, String idempotencyKey, long transactionSequence, String actor);
    Optional<Map<String, Object>> findBySampleKey(String sampleKey);
    Map<String, Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command,
                               String transactionId, String idempotencyKey, long transactionSequence, String actor);
    void delete(long sampleItemId, long expectedVersion, String transactionId, long transactionSequence, String actor);
    CpfSlice<Map<String,Object>> cursor(Long afterId, int size);
    boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,
                           String transactionId, String idempotencyKey, long transactionSequence, String actor);
}
"@

$localAdapter = @"
package $FeaturePackage.adapter.local;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import $FeaturePackage.repository.${FeatureClassPrefix}Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 같은 주제영역 DB를 사용하는 기본 local adapter입니다. */
@Component
@ConditionalOnProperty(name = "cpf.$module.reference.mode", havingValue = "local", matchIfMissing = true)
public class Local${FeatureClassPrefix}QueryAdapter implements ${FeatureClassPrefix}QueryPort {
    private final ${FeatureClassPrefix}Repository repository;
    public Local${FeatureClassPrefix}QueryAdapter(${FeatureClassPrefix}Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository는 필수입니다.");
    }
    @Override public Map<String,Object> search(${FeatureClassPrefix}SearchRequest request) { return repository.search(request); }
    @Override public Map<String,Object> create(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return repository.create(command, transactionId, idempotencyKey, transactionSequence, actor);
    }
    @Override public Optional<Map<String,Object>> findBySampleKey(String sampleKey) { return repository.findBySampleKey(sampleKey); }
    @Override public Map<String,Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return repository.update(sampleItemId, command, transactionId, idempotencyKey, transactionSequence, actor);
    }
    @Override public void delete(long sampleItemId, long expectedVersion, String transactionId, long transactionSequence, String actor) {
        repository.delete(sampleItemId, expectedVersion, transactionId, transactionSequence, actor);
    }
    @Override public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) { return repository.cursor(afterId, size); }
    @Override public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return repository.verifyRollback(command, transactionId, idempotencyKey, transactionSequence, actor);
    }
}
"@

$remoteMatchIfMissing = if (-not $DatabaseEnabled) { ", matchIfMissing = true" } else { "" }
$remoteProxy = @"
package $FeaturePackage.adapter.remote;

import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import com.cpf.core.api.http.CpfHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 분리 배포된 Generated Domain에 CPF 표준 호출 경계로 접근하는 remote proxy입니다.
 * transactionId/idempotencyKey 등은 CpfHttpClient 구현이 현재 거래 Context를 자동 전파합니다.
 */
@Component
@ConditionalOnProperty(name = "cpf.$module.reference.mode", havingValue = "remote"$remoteMatchIfMissing)
public class Remote${FeatureClassPrefix}QueryProxy implements ${FeatureClassPrefix}QueryPort {
    private final CpfHttpClient webClient;
    public Remote${FeatureClassPrefix}QueryProxy(CpfHttpClient webClient) { this.webClient = Objects.requireNonNull(webClient, "webClient는 필수입니다."); }
    @Override public Map<String,Object> search(${FeatureClassPrefix}SearchRequest request) {
        return webClient.get("$ModuleUpper", uriBuilder -> {
            uriBuilder.path("/api/v1/$module/reference")
                    .queryParam("sortBy", request.sortBy()).queryParam("sortDirection", request.sortDirection())
                    .queryParam("page", request.page()).queryParam("size", request.size());
            if (request.keyword() != null && !request.keyword().isBlank()) uriBuilder.queryParam("keyword", request.keyword());
            return uriBuilder.build();
        }, new ParameterizedTypeReference<Map<String,Object>>() {});
    }
    @Override public Map<String,Object> create(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return webClient.post("$ModuleUpper", "/api/v1/$module/reference/sample-items", command, new ParameterizedTypeReference<Map<String,Object>>() {});
    }
    @Override public Optional<Map<String,Object>> findBySampleKey(String sampleKey) {
        Map<String,Object> response = webClient.get("$ModuleUpper", uriBuilder -> uriBuilder.path("/api/v1/$module/reference/sample-items/{sampleKey}").build(sampleKey), new ParameterizedTypeReference<Map<String,Object>>() {});
        return Optional.ofNullable(response);
    }
    @Override public Map<String,Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return webClient.post("$ModuleUpper", "/api/v1/$module/reference/sample-items/"+sampleItemId+"/update", command, new ParameterizedTypeReference<Map<String,Object>>() {});
    }
    @Override public void delete(long sampleItemId, long expectedVersion, String transactionId, long transactionSequence, String actor) {
        webClient.post("$ModuleUpper", "/api/v1/$module/reference/sample-items/"+sampleItemId+"/delete", Map.of("expectedVersion", expectedVersion), Void.class);
    }
    @Override public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) {
        return webClient.get("$ModuleUpper", uriBuilder -> uriBuilder.path("/api/v1/$module/reference/sample-items/cursor").queryParam("afterId", afterId == null ? 0 : afterId).queryParam("size", size).build(), new ParameterizedTypeReference<CpfSlice<Map<String,Object>>>() {});
    }
    @Override public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        Boolean response = webClient.post("$ModuleUpper", "/api/v1/$module/reference/sample-items/rollback-verify", command, Boolean.class);
        return Boolean.TRUE.equals(response);
    }
}
"@

$inMemoryAdapter = @"
package $FeaturePackage.adapter.memory;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DB capability를 선택하지 않은 경우 계약 검증에 사용하는 메모리 adapter입니다. */
@Component
public class InMemory${FeatureClassPrefix}QueryAdapter implements ${FeatureClassPrefix}QueryPort {
    @Override public Map<String,Object> search(${FeatureClassPrefix}SearchRequest request) { return Map.of("items", List.of(), "criteria", request); }
    @Override public Map<String,Object> create(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return Map.of("sampleKey", command.sampleKey(), "itemName", command.itemName(), "versionNo", command.expectedVersion(), "transactionId", transactionId, "idempotencyKey", idempotencyKey);
    }
    @Override public Optional<Map<String,Object>> findBySampleKey(String sampleKey) { return Optional.empty(); }
    @Override public Map<String,Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        return create(command, transactionId, idempotencyKey, transactionSequence, actor);
    }
    @Override public void delete(long sampleItemId, long expectedVersion, String transactionId, long transactionSequence, String actor) { }
    @Override public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) { return new CpfSlice<>(List.of(), 0, Math.max(1, size), false); }
    @Override public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) { return true; }
}
"@

$service = @"
package $FeaturePackage.service;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseService;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Generated Domain Sample 업무 서비스입니다. Transport 식별자는 Core Context에서만 취득합니다. */
@Service
public class ${FeatureClassPrefix}Service extends ${ModuleClassName}BaseService {
    private static final Logger log = LoggerFactory.getLogger(${FeatureClassPrefix}Service.class);
    private final ${FeatureClassPrefix}QueryPort queryPort;
    public ${FeatureClassPrefix}Service(${FeatureClassPrefix}QueryPort queryPort) { this.queryPort = Objects.requireNonNull(queryPort, "queryPort는 필수입니다."); }

    @Transactional(readOnly = true)
    public Map<String,Object> search(${FeatureClassPrefix}SearchRequest request) { return queryPort.search(request.normalized()); }

    @Transactional
    public Map<String,Object> create(${FeatureClassPrefix}SampleCommand command) {
        requireCommand(command); MutationContext context = mutationContext();
        log.info("$ModuleUpper Sample create auditKey={}, transactionId={}", command.maskedAuditKey(), context.transactionId());
        return queryPort.create(command, context.transactionId(), context.idempotencyKey(), context.sequence(), context.actor());
    }

    @Transactional(readOnly = true)
    public Optional<Map<String,Object>> findBySampleKey(String sampleKey) {
        if (sampleKey == null || sampleKey.isBlank()) throw new CpfValidationException("sampleKey는 필수입니다.");
        return queryPort.findBySampleKey(sampleKey.trim());
    }

    @Transactional
    public Map<String,Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command) {
        requirePositiveId(sampleItemId); requireCommand(command); MutationContext context = mutationContext();
        return queryPort.update(sampleItemId, command, context.transactionId(), context.idempotencyKey(), context.sequence(), context.actor());
    }

    @Transactional
    public void delete(long sampleItemId, long expectedVersion) {
        requirePositiveId(sampleItemId);
        if (expectedVersion < 0) throw new CpfValidationException("expectedVersion은 0 이상이어야 합니다.");
        MutationContext context = mutationContext();
        queryPort.delete(sampleItemId, expectedVersion, context.transactionId(), context.sequence(), context.actor());
    }

    @Transactional(readOnly = true)
    public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) { return queryPort.cursor(afterId, size); }

    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command) {
        requireCommand(command); MutationContext context = mutationContext();
        return queryPort.verifyRollback(command, context.transactionId(), context.idempotencyKey(), context.sequence(), context.actor());
    }

    private MutationContext mutationContext() {
        String transactionId = CpfTransactionContext.transactionId();
        String idempotencyKey = CpfTransactionContext.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CpfValidationException("변경 거래에는 표준 Header idempotencyKey가 필수입니다.");
        }
        String actor = firstText(CpfTransactionContext.operatorId(), CpfTransactionContext.userId(), "$ModuleUpper");
        return new MutationContext(transactionId, idempotencyKey.trim(), CpfTransactionContext.nextSequence(), actor);
    }

    private void requireCommand(${FeatureClassPrefix}SampleCommand command) {
        if (command == null) throw new CpfValidationException("Sample Command는 필수입니다.");
    }
    private void requirePositiveId(long sampleItemId) {
        if (sampleItemId < 1) throw new CpfValidationException("sampleItemId는 1 이상이어야 합니다.");
    }
    private String firstText(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return "$ModuleUpper";
    }
    private record MutationContext(String transactionId, String idempotencyKey, long sequence, String actor) { }
}
"@

$repository = @"
package $FeaturePackage.repository;

import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 중앙 Vendor Pack의 MyBatis statement를 호출하는 Generated Domain DB-neutral 저장소입니다. */
@Repository
public class ${FeatureClassPrefix}Repository {
    private static final int MAX_PAGE_SIZE = 200;
    private final SqlSessionTemplate sqlSessionTemplate;
    private final TransactionTemplate transactionTemplate;
    public ${FeatureClassPrefix}Repository(@Qualifier("${module}SqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate,
            @Qualifier("${module}TransactionManager") PlatformTransactionManager transactionManager) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate은 필수입니다.");
        this.transactionTemplate = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager는 필수입니다."));
    }
    public Map<String,Object> search(${FeatureClassPrefix}SearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList("${FeaturePackage}.mapper.${FeatureClassPrefix}Mapper.search", request),
                "criteria", request,
                "pageRequest", request.pageRequest());
    }
    public Map<String,Object> create(${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor) {
        Optional<Map<String,Object>> replay=findByIdempotencyKey(idempotencyKey);
        if(replay.isPresent()){ Map<String,Object> response=new LinkedHashMap<>(replay.get()); response.put("idempotentReplay",true); return Map.copyOf(response); }
        Map<String,Object> parameters=parameters(command,transactionId,idempotencyKey,transactionSequence,actor); sqlSessionTemplate.insert(statement("insert"),parameters);
        return findBySampleKey(command.sampleKey()).orElseThrow(() -> new IllegalStateException("등록한 Sample Item을 다시 조회할 수 없습니다."));
    }
    public Optional<Map<String,Object>> findBySampleKey(String sampleKey){ return Optional.ofNullable(sqlSessionTemplate.selectOne(statement("findBySampleKey"),sampleKey)); }
    public Map<String,Object> update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey, long transactionSequence, String actor){
        Map<String,Object> parameters=parameters(command,transactionId,idempotencyKey,transactionSequence,actor); parameters.put("sampleItemId",sampleItemId);
        int updated=sqlSessionTemplate.update(statement("updateWithVersion"),parameters); if(updated!=1) throw new OptimisticLockingFailureException("Sample Item이 없거나 version이 변경되었습니다. sampleItemId="+sampleItemId);
        return findBySampleKey(command.sampleKey()).orElseThrow(() -> new IllegalStateException("수정한 Sample Item을 다시 조회할 수 없습니다."));
    }
    public void delete(long sampleItemId,long expectedVersion,String transactionId,long transactionSequence,String actor){
        Map<String,Object> parameters=new HashMap<>(); parameters.put("sampleItemId",sampleItemId); parameters.put("versionNo",expectedVersion); parameters.put("transactionId",transactionId); parameters.put("transactionSequence",transactionSequence); parameters.put("updatedBy",actor);
        int updated=sqlSessionTemplate.update(statement("logicalDeleteWithVersion"),parameters); if(updated!=1) throw new OptimisticLockingFailureException("Sample Item이 없거나 version이 변경되었습니다. sampleItemId="+sampleItemId);
    }
    public CpfSlice<Map<String,Object>> cursor(Long afterId,int size){
        int safeSize=Math.max(1,Math.min(size,MAX_PAGE_SIZE)); Map<String,Object> parameters=Map.of("cursor",afterId==null?0L:Math.max(afterId,0L),"size",safeSize+1);
        List<Map<String,Object>> rows=sqlSessionTemplate.selectList(statement("cursorSlice"),parameters); boolean hasNext=rows.size()>safeSize; List<Map<String,Object>> items=hasNext?List.copyOf(rows.subList(0,safeSize)):List.copyOf(rows);
        return new CpfSlice<>(items, 0, safeSize, hasNext);
    }
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,String transactionId,String idempotencyKey,long transactionSequence,String actor){
        boolean existedBefore=findBySampleKey(command.sampleKey()).isPresent(); transactionTemplate.executeWithoutResult(status->{ sqlSessionTemplate.insert(statement("insert"),parameters(command,transactionId,idempotencyKey,transactionSequence,actor)); status.setRollbackOnly(); });
        return existedBefore==findBySampleKey(command.sampleKey()).isPresent();
    }
    private Optional<Map<String,Object>> findByIdempotencyKey(String idempotencyKey){ return Optional.ofNullable(sqlSessionTemplate.selectOne(statement("findByIdempotencyKey"),idempotencyKey)); }
    private Map<String,Object> parameters(${FeatureClassPrefix}SampleCommand command,String transactionId,String idempotencyKey,long transactionSequence,String actor){
        Map<String,Object> parameters=new HashMap<>(); parameters.put("sampleKey",command.sampleKey()); parameters.put("itemName",command.itemName()); parameters.put("statusCode",command.statusCode()); parameters.put("versionNo",command.expectedVersion()); parameters.put("idempotencyKey",idempotencyKey); parameters.put("transactionId",transactionId); parameters.put("transactionSequence",transactionSequence); parameters.put("createdBy",actor); parameters.put("updatedBy",actor); return parameters;
    }
    private String statement(String id){ return "${FeaturePackage}.mapper.${FeatureClassPrefix}Mapper."+id; }
}
"@

$myBatisConfig = @"
package $BasePackage.config;

import com.cpf.core.api.database.CpfSqlResources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/** ${ModuleName} mapper를 해당 주제영역 소유 DataSource와 명시적으로 연결합니다. */
@Configuration(proxyBeanMethods = false)
public class ${ModuleName}MyBatisConfig {

    @Bean(name = "${module}SqlSessionFactory")
    public SqlSessionFactory ${module}SqlSessionFactory(
            @Qualifier("${module}DataSource") DataSource dataSource,
            Environment environment) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(CpfSqlResources.mapperResources(environment, "$module"));
        return factoryBean.getObject();
    }

    @Bean(name = "${module}SqlSessionTemplate")
    public SqlSessionTemplate ${module}SqlSessionTemplate(
            @Qualifier("${module}SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
"@

$batchRepositoryConfig = @"
package $BasePackage.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ${ModuleName} 배치 원천 메타를 CPF DB의 Spring Batch 표준 저장소에 기록합니다.
 *
 * <p>업무 데이터 트랜잭션과 배치 메타 트랜잭션을 분리해 업무 DB에
 * Spring Batch 내부 테이블이 생성되거나 조회되는 것을 방지합니다.</p>
 */
@Configuration
public class ${ModuleName}BatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource cpfDataSource;
    private final PlatformTransactionManager cpfTransactionManager;

    public ${ModuleName}BatchRepositoryConfig(
            @Qualifier("cpfDataSource") DataSource cpfDataSource,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager cpfTransactionManager) {
        this.cpfDataSource = cpfDataSource;
        this.cpfTransactionManager = cpfTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return cpfDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return cpfTransactionManager;
    }

}
"@

$dto = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Request;'))
import com.cpf.core.api.base.CpfQuery;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSort;
import com.cpf.core.api.page.CpfSortDirection;
import java.util.List;
import java.util.Set;

/**
 * ${ModuleName} 조회 조건입니다.
 *
 * <p>정렬 컬럼은 whitelist로 제한해 SQL Injection을 차단합니다.</p>
 */
public record ${FeatureClassPrefix}SearchRequest(
        String keyword,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size) implements ${ModuleClassName}Request, CpfQuery {
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "sample_item_id", "item_name");

    public ${FeatureClassPrefix}SearchRequest normalized() {
        String normalizedSortBy = sortBy != null && SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new ${FeatureClassPrefix}SearchRequest(
                keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }

    /** CPF 표준 Page 요청으로 변환합니다. Repository/EDU가 별도 Paging DTO를 만들지 않습니다. */
    public CpfPageRequest pageRequest() {
        ${FeatureClassPrefix}SearchRequest n = normalized();
        CpfSortDirection direction = CpfSortDirection.from(n.sortDirection());
        return new CpfPageRequest(n.page(), n.size(), List.of(new CpfSort(n.sortBy(), direction)));
    }

    public int offset() {
        return Math.toIntExact(pageRequest().offset());
    }
}
"@

$sampleCommand = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Request;'))
import com.cpf.core.api.security.CpfMasking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Generated Domain Minimal CRUD의 업무 입력 계약입니다.
 * transactionId/idempotencyKey/actor/sequence는 Body에 중복하지 않고 cpf-core TransactionContext에서 공급합니다.
 */
public record ${FeatureClassPrefix}SampleCommand(
        @NotBlank @Size(max = 100) String sampleKey,
        @NotBlank @Size(max = 200) String itemName,
        @Pattern(regexp = "ACTIVE|INACTIVE") String statusCode,
        @PositiveOrZero long expectedVersion) implements ${ModuleClassName}Request {
    public ${FeatureClassPrefix}SampleCommand {
        sampleKey = requireText(sampleKey,"sampleKey"); itemName=requireText(itemName,"itemName");
        statusCode=defaultText(statusCode,"ACTIVE").toUpperCase(Locale.ROOT);
        if(!statusCode.equals("ACTIVE")&&!statusCode.equals("INACTIVE")) throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        if(expectedVersion<0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
    }
    public String maskedAuditKey(){ return CpfMasking.mask(sampleKey); }
    private static String requireText(String value,String field){ if(value==null||value.isBlank()) throw new IllegalArgumentException(field+"는 필수입니다."); return value.trim(); }
    private static String defaultText(String value,String fallback){ return value==null||value.isBlank()?fallback:value.trim(); }
}
"@

$sampleItem = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Response;'))
import java.time.Instant;

/** Vendor와 무관한 Generated Domain Sample Item 논리 모델입니다. */
public record ${FeatureClassPrefix}SampleItem(
        long sampleItemId,
        String sampleKey,
        String itemName,
        String statusCode,
        long versionNo,
        String idempotencyKey,
        String transactionId,
        long transactionSequence,
        Instant transactionAt,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt) implements ${ModuleClassName}Response {
}
"@

$validator = @"
package $FeaturePackage.validation;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * ${ModuleName} 조회 API 입력값을 검증합니다.
 */
@Component
public class ${FeatureClassPrefix}SearchValidator {
    public void validate(${FeatureClassPrefix}SearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("${ModuleName} 조회 조건은 필수입니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하여야 합니다.");
        }
    }
}
"@

$batchDependency = if ($BatchEnabled) {
    "    implementation 'org.springframework.boot:spring-boot-starter-batch'"
} else {
    ""
}

$databaseDependencies = if ($DatabaseEnabled) {
@"
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
    implementation 'org.flywaydb:flyway-database-postgresql'
    implementation 'org.flywaydb:flyway-database-oracle'
    implementation 'org.flywaydb:flyway-sqlserver'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'org.postgresql:postgresql'
    runtimeOnly 'com.oracle.database.jdbc:ojdbc11'
    runtimeOnly 'com.microsoft.sqlserver:mssql-jdbc'
"@
} else {
    ""
}

$databaseResourceAssembly = if ($DatabaseEnabled) {
@"
def cpfDbVendor = (findProperty('cpfDbVendor') ?: System.getenv('${ModuleUpper}_DATABASE_VENDOR') ?: '$DatabaseVendor')
        .toString()
        .toLowerCase(Locale.ROOT)
def cpfSupportedDbVendors = ['mariadb', 'mysql', 'postgresql', 'oracle', 'sqlserver'] as Set
if (!cpfSupportedDbVendors.contains(cpfDbVendor)) {
    throw new GradleException("Unsupported cpfDbVendor: `${cpfDbVendor}")
}
def cpfCentralDbPackRoot = file(
        findProperty('cpfCentralDbPackRoot')
                ?: "${Dollar}{rootProject.projectDir}/cpf-tools/db/vendor")
def cpfSelectedDomainTemplate = new File(cpfCentralDbPackRoot, "`${cpfDbVendor}/domain-template")
def cpfGeneratedVendorResources = layout.buildDirectory.dir('generated-resources/cpf-vendor')

tasks.register('prepareCpfVendorResources', Sync) {
    def tokenValues = [
            CPF_VENDOR: cpfDbVendor,
            CPF_DOMAIN: '$module',
            CPF_SYSTEM_CODE: '$ModuleUpper',
            CPF_DISPLAY_NAME: '$ModuleName',
            CPF_SCHEMA_NAME: '$SchemaName',
            CPF_MODULE_NAME: '$ModuleName',
            CPF_PACKAGE_NAME: '$PackageName',
            CPF_TABLE_PREFIX: '$TablePrefix',
            CPF_MAPPER_NAMESPACE: '$FeaturePackage.mapper.${FeatureClassPrefix}Mapper',
            CPF_MAPPER_NAME: '${FeatureClassPrefix}Mapper'
    ]
    into cpfGeneratedVendorResources
    from(cpfSelectedDomainTemplate) {
        include 'provision/**', 'install/**', 'seed/**', 'migration/**', 'verify/**', 'rollback/**'
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName ->
            fileName
                    .replace('.template', '')
                    .replace('__DOMAIN__', '$module')
        }
        into "db/vendor/`${cpfDbVendor}"
        includeEmptyDirs = false
    }
    from(new File(cpfSelectedDomainTemplate, 'runtime/mybatis')) {
        include '**/*.template'
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName ->
            fileName
                    .replace('.template', '')
                    .replace('__DOMAIN__', '$module')
                    .replace('__MAPPER__', '${FeatureClassPrefix}Mapper')
        }
        into "mybatis/vendor/`${cpfDbVendor}/mapper/$module/reference"
        includeEmptyDirs = false
    }
    from(new File(cpfSelectedDomainTemplate, 'runtime/repository')) {
        include '**/*.template'
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName ->
            fileName
                    .replace('.template', '')
                    .replace('__DOMAIN__', '$module')
        }
        into "sql/vendor/`${cpfDbVendor}/$module"
        includeEmptyDirs = false
    }
    doFirst {
        if (!cpfSelectedDomainTemplate.isDirectory()) {
            throw new GradleException(
                    "CPF central domain template is missing: `${cpfSelectedDomainTemplate}")
        }
        if (!new File(cpfSelectedDomainTemplate, 'runtime/mybatis').isDirectory()) {
            throw new GradleException(
                    "CPF central domain runtime MyBatis template is missing: `${cpfSelectedDomainTemplate}")
        }
    }
}

sourceSets.main.resources.srcDir(cpfGeneratedVendorResources)
tasks.named('processResources') {
    dependsOn tasks.named('prepareCpfVendorResources')
}
"@
} else {
    ""
}

$batchTransactionImport = if ($DatabaseEnabled) { "" } else {
    "import org.springframework.batch.support.transaction.ResourcelessTransactionManager;"
}
$batchTransactionBean = if ($DatabaseEnabled) { "" } else {
@"
    /** DB capability가 없는 Tasklet의 chunk 경계를 위한 비영속 트랜잭션 관리자입니다. */
    @Bean(name = "${module}TransactionManager")
    public PlatformTransactionManager ${module}TransactionManager() {
        return new ResourcelessTransactionManager();
    }

"@
}
$batchConfig = @"
package $FeaturePackage.batch;

import com.cpf.core.api.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
$batchTransactionImport

/**
 * ${ModuleName} 주제영역의 표준 Tasklet 배치 골격입니다.
 */
@Configuration
public class ${FeatureClassPrefix}BatchConfig {

$batchTransactionBean
    @Bean
    @CpfBatchJob(id = "B${DomainIdCode}TS0001", name = "${ModuleName}표준배치", ownerDomain = "$DomainIdCode")
    public Job ${module}StandardJob(JobRepository jobRepository, Step ${module}StandardStep) {
        return new JobBuilder("${ModuleUpper}_STANDARD_JOB", jobRepository)
                .start(${module}StandardStep)
                .build();
    }

    @Bean
    public Step ${module}StandardStep(
            JobRepository jobRepository,
            @Qualifier("${module}TransactionManager") PlatformTransactionManager transactionManager) {
        return new StepBuilder("${ModuleUpper}_STANDARD_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실제 업무 로직은 재시작 가능성과 멱등성을 보장하는 서비스에 위임합니다.
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
"@

$buildGradle = @"
plugins {
    id 'java'
    id 'war'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

group = '$BasePackage'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(rootProject.ext.cpfJavaVersion)
    }
}

dependencies {
    implementation project(':cpf-core')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
$databaseDependencies
$batchDependency
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

$databaseResourceAssembly

tasks.withType(AbstractArchiveTask).configureEach {
    archiveBaseName = "$projectName"
    preserveFileTimestamps = false
    reproducibleFileOrder = true
}

tasks.named('bootJar') {
    enabled = true
}

tasks.named('bootWar') {
    enabled = true
}
"@

$applicationJava = @"
package $BasePackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * $ModuleUpper 주제영역 실행 애플리케이션입니다.
 *
 * <p>모듈 부트스트랩만 소유하며 업무 기능은 feature package에 둡니다.</p>
 */
@SpringBootApplication
public class ${ModuleClassName}Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(${ModuleClassName}Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(${ModuleClassName}Application.class);
    }
}
"@

$dataSourceConfig = @"
package $BasePackage.config;

import com.cpf.core.api.database.CpfDataSources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * embedded URL 연결과 external Tomcat JNDI 연결을 동일한 모듈에서 선택합니다.
 */
@Configuration
public class ${ModuleName}DataSourceConfig {

    @Bean
    public DataSource ${module}DataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "cpf.datasource");
    }

    /**
     * ${ModuleName} 저장소가 다른 주제영역 DB를 선택하지 않도록 전용 JDBC 접근 객체를 제공합니다.
     */
    @Bean(name = "${module}JdbcTemplate")
    public JdbcTemplate ${module}JdbcTemplate(
            @Qualifier("${module}DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * ${ModuleName} 서비스와 배치가 동일한 업무 트랜잭션 경계를 사용하도록 합니다.
     */
    @Bean(name = "${module}TransactionManager")
    public PlatformTransactionManager ${module}TransactionManager(
            @Qualifier("${module}DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
"@

$databaseSpringYml = if ($DatabaseEnabled) {
@"
  flyway:
    # DDL migration은 app 계정이 아니라 별도 migration 절차에서 실행합니다.
    enabled: ${Dollar}{$($ModuleUpper)_FLYWAY_ENABLED:false}
    locations: classpath:db/vendor/${Dollar}{$($ModuleUpper)_DATABASE_VENDOR:$DatabaseVendor}/migration
"@
} else { "" }
$applicationYml = @"
spring:
  application:
    name: $projectName
$databaseSpringYml
  batch:
    # 배치 Job은 운영 실행 요청으로만 시작하고 애플리케이션 기동 시 자동 실행하지 않습니다.
    job:
      enabled: false
    # Spring Batch 메타 스키마는 cpfDB 설치 SQL이 관리합니다.
    jdbc:
      initialize-schema: never
  config:
    import:
      - optional:classpath:application-cpf.yml
      - optional:classpath:application-cpf-${Dollar}{spring.profiles.active:local}.yml
      - optional:classpath:application-$module.yml
      - optional:classpath:application-$module-${Dollar}{spring.profiles.active:local}.yml

cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  logging:
    file:
      base-path: ${Dollar}{CPF_LOG_ROOT:}
      timezone: ${Dollar}{CPF_LOG_TIMEZONE:Asia/Seoul}
      max-history-days: ${Dollar}{CPF_LOG_MAX_HISTORY_DAYS:30}
      archive-compress-enabled: ${Dollar}{CPF_LOG_ARCHIVE_COMPRESS_ENABLED:true}
      file-pattern: "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log"
logging:
  config: classpath:log/cpf-logback-spring.xml
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
"@
$defaultReferenceMode = if ($DatabaseEnabled) { 'local' } elseif ($ExternalEnabled) { 'remote' } else { 'memory' }
$moduleDataSourceYml = if ($DatabaseEnabled) {
@"
  datasource:
    mode: ${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:$DataSourceJndiName}
    database-name: ${Dollar}{$($ModuleUpper)_DATABASE_NAME:$SchemaName}
    url: '${Dollar}{$($ModuleUpper)_DATASOURCE_URL:}'
    driver-class-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_DRIVER_CLASS_NAME:}
    username: ${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}
    password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD:}
"@
} else { "" }
$applicationModuleYml = @"
# ${ModuleName} 주제영역 공통 설정입니다.
cpf:
  db:
    # Vendor 선택은 Java 업무 Source가 아니라 SQL/Mapper resource 경로만 변경합니다.
    vendor: ${Dollar}{$($ModuleUpper)_DATABASE_VENDOR:$DatabaseVendor}
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
$moduleDataSourceYml
  ${module}:
    reference:
      mode: ${Dollar}{$($ModuleUpper)_REFERENCE_MODE:$defaultReferenceMode}
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log"
"@
$readme = @"
# ${ModuleName} 주제영역 골격

이 디렉터리는 `cpf-tools/scripts/create-domain.ps1`로 생성한 신규 업무 모듈 후보입니다.

- 실제 반영 전 `settings.gradle`, `cpf-tools/db/generated/database-schema-manifest.json`, 선택 Vendor Pack, ADM/BZA 연동과 OpenAPI 문서를 함께 검토합니다.
- Controller, Facade, Service, Repository, DTO, Mapper XML, SQL의 모듈 코드와 테이블 prefix를 일치시킵니다.
- 운영 로그는 `${Dollar}{CPF_LOG_ROOT}/{environment}/{moduleCode}/{instanceId}/{category}/cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log` 규칙을 사용합니다.
"@

$uiComponent = @"
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { search${FeatureClassPrefix} } from './${FeatureClassPrefix}Api'

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])

async function load(): Promise<void> {
  loading.value = true
  try {
    rows.value = await search${FeatureClassPrefix}()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section aria-labelledby="${module}-reference-title">
    <h1 id="${module}-reference-title">${ModuleName} 참조 조회</h1>
    <button type="button" :disabled="loading" @click="load">조회</button>
    <p v-if="loading" role="status">조회 중</p>
    <table v-else>
      <caption>${ModuleName} 참조 결과</caption>
      <tbody>
        <tr v-for="(row, index) in rows" :key="index">
          <td>{{ row }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
"@

$uiApi = @"
/** ${ModuleName} 참조 API를 호출하고 응답의 items만 화면에 전달합니다. */
export async function search${FeatureClassPrefix}(): Promise<Record<string, unknown>[]> {
  const response = await fetch('/api/v1/$module/reference?page=0&size=20&sortBy=created_at&sortDirection=DESC', {
    headers: { Accept: 'application/json' },
    credentials: 'same-origin',
  })
  if (!response.ok) {
    throw new Error('${ModuleName} 참조 조회에 실패했습니다.')
  }
  const body = (await response.json()) as { items?: Record<string, unknown>[] }
  return body.items ?? []
}
"@

$serviceTest = @"
package $FeaturePackage.service;

import $FeaturePackage.dto.${FeatureClassPrefix}SampleCommand;
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import com.cpf.core.api.logging.CpfTransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ${FeatureClassPrefix}ServiceTest {
    private final AtomicReference<${FeatureClassPrefix}SearchRequest> capturedRequest = new AtomicReference<>();
    private final StubQueryPort queryPort = new StubQueryPort();
    private final ${FeatureClassPrefix}Service service = new ${FeatureClassPrefix}Service(queryPort);

    @BeforeEach
    void setUpTransactionContext() {
        CpfTransactionContext.initializeForTest(
                "20260724123456789${DomainIdCode}00000010000001",
                "${ModuleUpper}_IDEMPOTENCY_001",
                "generator-test");
    }

    @AfterEach
    void clearTransactionContext() {
        CpfTransactionContext.clear();
    }

    @Test
    void searchNormalizesPagingAndSort() {
        Map<String, Object> result = service.search(
                new ${FeatureClassPrefix}SearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

        assertThat(result).containsKey("items");
        assertThat(capturedRequest.get().sortBy()).isEqualTo("created_at");
        assertThat(capturedRequest.get().sortDirection()).isEqualTo("ASC");
        assertThat(capturedRequest.get().page()).isZero();
        assertThat(capturedRequest.get().size()).isEqualTo(200);
        assertThat(capturedRequest.get().pageRequest().size()).isEqualTo(200);
    }

    @Test
    void createTakesTransportIdentityFromCoreContext() {
        ${FeatureClassPrefix}SampleCommand command = new ${FeatureClassPrefix}SampleCommand(
                "${ModuleUpper}_TEST_001",
                "Minimal Transaction",
                "ACTIVE",
                0);

        Map<String, Object> result = service.create(command);

        assertThat(result).containsEntry("transactionId", "20260724123456789${DomainIdCode}00000010000001");
        assertThat(result).containsEntry("idempotencyKey", "${ModuleUpper}_IDEMPOTENCY_001");
        assertThat(command.maskedAuditKey()).isNotBlank();
        assertThat(queryPort.created).isSameAs(command);
    }

    private final class StubQueryPort implements ${FeatureClassPrefix}QueryPort {
        private ${FeatureClassPrefix}SampleCommand created;

        @Override
        public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
            capturedRequest.set(request);
            return Map.of("items", List.of(), "criteria", request);
        }

        @Override
        public Map<String, Object> create(${FeatureClassPrefix}SampleCommand command,
                                          String transactionId,
                                          String idempotencyKey,
                                          long transactionSequence,
                                          String actor) {
            created = command;
            return Map.of(
                    "sampleKey", command.sampleKey(),
                    "transactionId", transactionId,
                    "idempotencyKey", idempotencyKey,
                    "actor", actor);
        }

        @Override
        public Optional<Map<String, Object>> findBySampleKey(String sampleKey) { return Optional.empty(); }

        @Override
        public Map<String, Object> update(long sampleItemId,
                                          ${FeatureClassPrefix}SampleCommand command,
                                          String transactionId,
                                          String idempotencyKey,
                                          long transactionSequence,
                                          String actor) {
            return create(command, transactionId, idempotencyKey, transactionSequence, actor);
        }

        @Override
        public void delete(long sampleItemId, long expectedVersion, String transactionId, long transactionSequence, String actor) { }

        @Override
        public CpfSlice<Map<String,Object>> cursor(Long afterId, int size) {
            return new CpfSlice<>(List.of(), 0, Math.max(1, size), false);
        }

        @Override
        public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,
                                      String transactionId,
                                      String idempotencyKey,
                                      long transactionSequence,
                                      String actor) { return true; }
    }
}

"@

$messagingPublisher = @"
package $FeaturePackage.messaging;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * ${ModuleName} 업무 이벤트를 CPF 공개 transactional-outbox API로 등록합니다.
 * 업무 코드는 broker 종류와 Outbox 내부 저장소를 직접 참조하지 않습니다.
 */
@Service
@ConditionalOnBean(CpfBrokerClient.class)
public class ${ModuleName}EventPublisher {
    private final CpfBrokerClient brokerClient;
    public ${ModuleName}EventPublisher(CpfBrokerClient brokerClient) {
        this.brokerClient = Objects.requireNonNull(brokerClient, "brokerClient는 필수입니다.");
    }
    public CpfBrokerPublishResult publish(String messageId,String topic,String transactionId,String payload) {
        return brokerClient.enqueue(new CpfBrokerPublishRequest(
                messageId, topic, messageId,
                payload == null ? new byte[0] : payload.getBytes(StandardCharsets.UTF_8),
                "application/json", transactionId, null, "$SystemCode", null, messageId,
                Map.of("cpf-source-system", "$SystemCode"), Map.of("domainName", "$module")));
    }
}
"@

$messagingController = @"
package $FeaturePackage.messaging;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** ${ModuleName} 이벤트 outbox 적재 API입니다. */
@RestController
@RequestMapping("/api/v1/$module/events")
@ConditionalOnBean(${ModuleName}EventPublisher.class)
public class ${ModuleName}MessagingController extends ${ModuleClassName}BaseController {
    private final ${ModuleName}EventPublisher publisher;
    public ${ModuleName}MessagingController(${ModuleName}EventPublisher publisher) { this.publisher = publisher; }

    @PostMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}EV0001", name = "${ModuleName}EventPublish", ownerDomain = "$DomainIdCode", requiredPermission = "$SystemCode:EVENT:PUBLISH")
    @Operation(operationId = "publish${ModuleName}Event", summary = "${ModuleName} 업무 이벤트 등록")
    public ResponseEntity<CpfBrokerPublishResult> publish(@RequestBody Map<String,String> request) {
        return ok(publisher.publish(request.get("messageId"),request.get("topic"),request.get("transactionId"),request.get("payload")));
    }
}
"@

$messagingTest = @"
package $FeaturePackage.messaging;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ${ModuleName}EventPublisherTest {
    @Test void savesTraceableRequestThroughPublicBrokerBoundary() {
        AtomicReference<CpfBrokerPublishRequest> captured=new AtomicReference<>();
        CpfBrokerClient client=request->{ captured.set(request); return new CpfBrokerPublishResult("ACCEPTED",request.messageId(),"outbox",request.key(),Instant.now(),null); };
        CpfBrokerPublishResult result=new ${ModuleName}EventPublisher(client).publish("MSG-1","${module}.changed","TX-1","{\"id\":1}");
        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(captured.get().producerModule()).isEqualTo("$SystemCode");
        assertThat(captured.get().transactionId()).isEqualTo("TX-1");
    }
    @Test void rejectsMissingMessageIdBeforeRuntimeAccess() {
        CpfBrokerClient unused=request->{ throw new AssertionError(); };
        assertThatThrownBy(()->new ${ModuleName}EventPublisher(unused).publish(" ","topic","TX-1","{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
"@

$fileTransferService = @"
package $FeaturePackage.file;

import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Objects;

/** ${ModuleName} 파일 송수신 요청을 CPF 공개 파일전송 경계에 위임합니다. */
@Service
@ConditionalOnBean(CpfFileTransferClient.class)
public class ${ModuleName}FileTransferService {
    private final CpfFileTransferClient client;
    public ${ModuleName}FileTransferService(CpfFileTransferClient client) { this.client=Objects.requireNonNull(client,"client는 필수입니다."); }
    public CpfFileResult execute(CpfFileEndpoint endpoint,CpfFileRequest request) {
        return client.execute(Objects.requireNonNull(endpoint,"endpoint는 필수입니다."),Objects.requireNonNull(request,"request는 필수입니다."));
    }
}
"@

$fileTransferTest = @"
package $FeaturePackage.file;

import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ${ModuleName}FileTransferServiceTest {
    @Test void delegatesThroughPublicFileTransferBoundary() {
        AtomicBoolean called=new AtomicBoolean();
        CpfFileTransferClient client=(endpoint,request)->{ called.set(true); return new CpfFileResult("SUCCESS",endpoint.endpointCode(),request.localPath(),request.remotePath(),request.checksum(),request.fileSize(),Instant.now(),null); };
        CpfFileEndpoint endpoint=new CpfFileEndpoint("SFTP-1","SFTP","localhost",22,"/upload",null,Duration.ofSeconds(3),Map.of());
        CpfFileRequest request=new CpfFileRequest("TX-1","SEG-1","SFTP-1","UPLOAD","local.dat","remote.dat","sha256",10L,Map.of());
        CpfFileResult actual=new ${ModuleName}FileTransferService(client).execute(endpoint,request);
        assertThat(actual.status()).isEqualTo("SUCCESS");
        assertThat(called).isTrue();
    }
}
"@

$securityAuditGuard = @"
package $FeaturePackage.security;

import org.springframework.stereotype.Component;

/** 운영 변경 API가 감사 사유를 빠뜨리지 않도록 공통 검증합니다. */
@Component
public class ${ModuleName}OperationGuard {
    public String requireAuditReason(String auditReason) {
        if (auditReason == null || auditReason.isBlank()) {
            throw new IllegalArgumentException("감사 사유는 필수입니다.");
        }
        String normalized = auditReason.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("감사 사유는 500자 이하여야 합니다.");
        }
        return normalized;
    }
}
"@

$securityAuditController = @"
package $FeaturePackage.security;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 권한과 감사 사유가 필요한 운영 변경 API의 참조 구현입니다. */
@RestController
@RequestMapping("/api/v1/$module/operations")
public class ${ModuleName}OperationController extends ${ModuleClassName}BaseController {
    private final ${ModuleName}OperationGuard guard;

    public ${ModuleName}OperationController(${ModuleName}OperationGuard guard) {
        this.guard = guard;
    }

    @PostMapping("/validate")
    @CpfOnlineTransaction(
            id = "O${DomainIdCode}OP0001",
            name = "${ModuleName}OperationValidate",
            ownerDomain = "$DomainIdCode",
            requiredPermission = "$SystemCode:OPERATION:EXECUTE",
            auditReasonRequired = true)
    @Operation(operationId = "validate${ModuleName}Operation", summary = "운영 변경 감사 사유 검증")
    public ResponseEntity<Map<String, String>> validate(@RequestBody Map<String, String> request) {
        return ok(Map.of("auditReason", guard.requireAuditReason(request.get("auditReason"))));
    }
}
"@

$securityAuditTest = @"
package $FeaturePackage.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ${ModuleName}OperationGuardTest {
    private final ${ModuleName}OperationGuard guard = new ${ModuleName}OperationGuard();

    @Test
    void normalizesRequiredAuditReason() {
        assertThat(guard.requireAuditReason("  승인된 운영 변경  ")).isEqualTo("승인된 운영 변경");
    }

    @Test
    void rejectsMissingAuditReason() {
        assertThatThrownBy(() -> guard.requireAuditReason(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
"@

$smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:$Port",
    [int] `$TimeoutSec = 20
)

`$ErrorActionPreference = "Stop"
`$uri = "`$BaseUrl/api/v1/$module/reference?keyword=sample&page=0&size=20&sortBy=created_at&sortDirection=DESC"
`$response = Invoke-WebRequest -Method Get -Uri `$uri -TimeoutSec `$TimeoutSec -UseBasicParsing
if ([int] `$response.StatusCode -lt 200 -or [int] `$response.StatusCode -ge 300) {
    throw "${ModuleName} smoke failed. status=`$(`$response.StatusCode)"
}
Write-Host "${ModuleName} smoke passed. uri=`$uri"
"@

if (-not $OnlineEnabled) {
    $smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:$Port",
    [int] `$TimeoutSec = 20
)

`$ErrorActionPreference = "Stop"
`$uri = "`$BaseUrl/actuator/health"
`$response = Invoke-WebRequest -Method Get -Uri `$uri -TimeoutSec `$TimeoutSec -UseBasicParsing
if ([int] `$response.StatusCode -ne 200) {
    throw "${ModuleName} health smoke failed. status=`$(`$response.StatusCode)"
}
Write-Host "${ModuleName} health smoke passed. uri=`$uri"
"@
}

$applyOrder = @"
# ${ModuleName} 주제영역 반영 순서

1. `settings.gradle.patch` 내용을 검토하고 모듈 include를 반영합니다.
2. `${projectName}/` 모듈과 `application-${module}.yml` 설정을 반영합니다.
3. 중앙 `cpf-tools/db/vendor/{vendor}/domain-template`과 `prepareCpfVendorResources` 조립 결과를 검토합니다.
4. `manifest/domain-manifest.json` Metadata로 선택 Vendor의 Provision/Install/Seed/Migration/Runtime/Verify를 수행합니다.
5. `sql/50_framework_seed.${module}.candidate.sql`의 CPF module registry seed를 반영합니다.
6. `sql/60_adm_seed.${module}.candidate.sql`의 ADM 메뉴/API/버튼 권한 seed를 반영합니다.
7. `sql/99_smoke_check.${module}.candidate.sql`을 smoke check에 반영합니다.
8. `smoke-${module}.ps1`의 포트와 API path를 확인합니다.

후보 파일은 자동 적용하지 않으며 검토 후 정본 SQL과 설정에 반영합니다.
"@

$settingsPatch = @"
--- a/settings.gradle
+++ b/settings.gradle
@@
+include '$projectName'
+project(':$projectName').projectDir = file('$projectName')
"@

$cpfSeed = @"
-- ${ModuleName} CPF 서비스 레지스트리 seed 후보입니다.
INSERT INTO cpf_service (
    service_id, service_name, service_type, owner_module_code, description,
    use_yn, created_by, updated_by
) VALUES (
    '$ModuleUpper', '${ModuleName} 서비스', 'INTERNAL', '$ModuleUpper',
    '${ModuleName} 주제영역 서비스 호출 대상', 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    owner_module_code = VALUES(owner_module_code),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES (
    '${ModuleUpper}_API', '$ModuleUpper', '${ModuleName} API Endpoint', 'HTTP',
    'http://localhost:$Port', '/api/v1/$module', 3000, 0, 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_name = VALUES(endpoint_name),
    base_url = VALUES(base_url),
    context_path = VALUES(context_path),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
"@

$admSeed = @"
-- ${ModuleName} 표준 실행 카탈로그 바로가기 메뉴 seed 후보입니다.
INSERT INTO adm_menu (menu_id, parent_menu_id, menu_name, menu_path, sort_order, use_yn, created_by, updated_by)
VALUES ('${ModuleUpper}_STANDARD_EXECUTION', 'STANDARD_EXECUTION', '${ModuleName} 실행 정의', '/adm#standard-executions', 900, 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_path = VALUES(menu_path), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
"@

$bzaSeed = @"
-- ${ModuleName} BZA 업무 메뉴 seed 후보입니다.
INSERT INTO bza_menu (
    menu_code, menu_name, parent_menu_code, module_code, route_path,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    '${ModuleUpper}_ROOT', '${ModuleName}', NULL, '$ModuleUpper', '/bza/domain/$module',
    'ALL', '/api/v1/$module', 900, 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    module_code = VALUES(module_code),
    route_path = VALUES(route_path),
    api_path = VALUES(api_path),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
"@

$onlineJson = $OnlineEnabled.ToString().ToLowerInvariant()
$databaseJson = $DatabaseEnabled.ToString().ToLowerInvariant()
$batchJson = $BatchEnabled.ToString().ToLowerInvariant()
$externalJson = $ExternalEnabled.ToString().ToLowerInvariant()
$uiJson = $UiEnabled.ToString().ToLowerInvariant()
$bzaMenuJson = $BzaMenuEnabled.ToString().ToLowerInvariant()
$domainManifest = @"
{
  "metadataVersion": "1.0",
  "domainType": "GENERATED_DOMAIN",
  "moduleCode": "$ModuleUpper",
  "systemCode": "$SystemCode",
  "domainName": "$module",
  "projectName": "$projectName",
  "moduleName": "$ModuleName",
  "displayName": "$ModuleName",
  "domainIdCode": "$DomainIdCode",
  "packageName": "$PackageName",
  "basePackage": "$BasePackage",
  "schemaName": "$SchemaName",
  "port": $Port,
  "tablePrefix": "$TablePrefix",
  "sampleTable": "${TablePrefix}_sample_item",
  "onlineEnabled": $onlineJson,
  "databaseEnabled": $databaseJson,
  "databaseVendor": "$DatabaseVendor",
  "databaseVendorProperty": "cpf.db.vendor",
  "databaseProfilePath": "deploy/database/database-profile.json",
  "databaseConnectionDefaults": {
    "vendor": "$DatabaseVendor",
    "host": "$DatabaseHost",
    "port": $DatabasePort,
    "databaseName": "$DatabaseName",
    "schemaName": "$DatabaseSchema",
    "adminUsername": "$DatabaseAdminUsername",
    "migrationUsername": "$DatabaseMigrationUsername",
    "runtimeUsername": "$DatabaseRuntimeUsername"
  },
  "supportedDatabaseVendors": ["mariadb", "mysql", "postgresql", "oracle", "sqlserver"],
  "databaseTemplatePack": "cpf-tools/db/vendor/{vendor}/domain-template",
  "databaseTemplateSelector": {
    "vendor": "$DatabaseVendor",
    "metadata": "manifest/domain-manifest.json",
    "sourceTreeMutation": false,
    "selectedVendorOnly": true
  },
  "databaseResourceAssembly": "prepareCpfVendorResources",
  "databaseLifecycle": {
    "sourceOfTruth": "cpf-tools/db/vendor/{vendor}/domain-template",
    "bootstrapScript": "cpf-tools/scripts/initialize-domain-database.ps1",
    "defaultSampleTable": "${TablePrefix}_sample_item",
    "transactionIdentity": "transactionId"
  },
  "generatedResourceRoot": "build/generated-resources/cpf-vendor",
  "dataSourceJndiName": "$DataSourceJndiName",
  "batchEnabled": $batchJson,
  "externalEnabled": $externalJson,
  "messagingEnabled": $($MessagingEnabled.ToString().ToLowerInvariant()),
  "fileEnabled": $($FileEnabled.ToString().ToLowerInvariant()),
  "securityAuditEnabled": $($SecurityAuditEnabled.ToString().ToLowerInvariant()),
  "uiEnabled": $uiJson,
  "bzaMenuEnabled": $bzaMenuJson,
  "serviceId": "$ModuleUpper",
  "onlineStandardId": "O${DomainIdCode}QY0001",
  "batchStandardId": "B${DomainIdCode}TS0001",
  "minimalTransactionContract": {
    "model": "sample-item",
    "logicalTable": "${SchemaName}.${TablePrefix}_sample_item",
    "operations": [
      "create", "read", "update", "delete", "search", "offset-page", "slice", "cursor",
      "validation", "transaction-commit", "transaction-rollback", "optimistic-lock",
      "duplicate", "local-call", "remote-call", "standard-header",
      "transaction-id", "error-mapping", "idempotency", "audit", "masking", "framework-edu"
    ]
  }
}
"@

$executionCatalogManifest = @"
[
  {
    "standardExecutionId": "O${DomainIdCode}QY0001",
    "executionType": "ONLINE",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$FeaturePackage.controller.${FeatureClassPrefix}Controller",
    "enabled": $onlineJson
  },
  {
    "standardExecutionId": "B${DomainIdCode}TS0001",
    "executionType": "BATCH",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$FeaturePackage.batch.${FeatureClassPrefix}BatchConfig",
    "enabled": $batchJson
  }
]
"@

$ownershipManifest = @"
{
  "moduleCode": "$ModuleUpper",
  "ownerDomain": "$DomainIdCode",
  "ownedPackages": ["$BasePackage"],
  "ownedSchemas": ["$SchemaName"],
  "ownedTablePrefixes": ["${TablePrefix}_"],
  "forbiddenDependencies": ["other-domain-repository", "other-domain-mapper"],
  "crossDomainContract": "CPF Service Call Engine 또는 CMN Facade Contract"
}
"@

$smokeSql = @"
-- ${ModuleName} smoke check 후보입니다.
SELECT '${ModuleUpper}_SAMPLE_ITEM_TABLE' AS check_id, COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = '${TablePrefix}_sample_item';
"@

$profileApplicationFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $profileWasId = "${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001"
    $profileDataSourceMode = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:jndi}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}"
    }
    $profileDataSourceUrl = "${Dollar}{$($ModuleUpper)_DATASOURCE_URL:}"
    $profileDataSourceUsername = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}"
    }
    $profileDataSourceYml = if ($DatabaseEnabled) {
@"
  datasource:
    mode: $profileDataSourceMode
    database-name: ${Dollar}{$($ModuleUpper)_DATABASE_NAME:$SchemaName}
    url: $profileDataSourceUrl
    username: $profileDataSourceUsername
    password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:$DataSourceJndiName}
"@
    } else { "" }
    $profileApplicationFiles["src/main/resources/application-${module}-${profileName}.yml"] = @"
# ${ModuleName} ${profileName} profile 설정입니다.
spring:
  config:
    activate:
      on-profile: $profileName

server:
  port: ${Dollar}{$($ModuleUpper)_SERVER_PORT:$Port}

cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
    instance-id: ${Dollar}{$($ModuleUpper)_INSTANCE_ID:${ModuleUpper}01}
    was-id: ${Dollar}{$($ModuleUpper)_WAS_ID:$profileWasId}
$profileDataSourceYml
"@
}

$deployEnvFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $deployDataSourceMode = if ($profileName -eq "prod") { "jndi" } else { "url" }
    $deployDataSourceEnv = if ($DatabaseEnabled) {
@"
${ModuleUpper}_DATASOURCE_MODE=$deployDataSourceMode
${ModuleUpper}_DATABASE_VENDOR=$DatabaseVendor
${ModuleUpper}_DATABASE_NAME=$SchemaName
${ModuleUpper}_DATASOURCE_URL=
${ModuleUpper}_DATASOURCE_USERNAME=cpf_${module}_app
${ModuleUpper}_DATASOURCE_PASSWORD=__SET_BY_SECRET_PROVIDER__
${ModuleUpper}_DATASOURCE_JNDI_NAME=$DataSourceJndiName
"@
    } else { "" }
    $deployEnvFiles["deploy/env/${profileName}-${module}.env"] = @"
SPRING_PROFILES_ACTIVE=$profileName
${ModuleUpper}_MODULE_ID=$ModuleUpper
${ModuleUpper}_INSTANCE_ID=${ModuleUpper}-${profileName}-01
${ModuleUpper}_WAS_ID=${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001
${ModuleUpper}_SERVER_PORT=$Port
CPF_LOG_ROOT=C:/cpf/runtime/logs
$deployDataSourceEnv
"@
}

$deployInventoryFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $inventoryFileName = if ($profileName -eq "prod") { "prod-services.template.json" } else { "$profileName-services.json" }
    $deployInventoryFiles["deploy/inventory/${inventoryFileName}.${module}.candidate.json"] = @"
{
  "profile": "$profileName",
  "services": [
    {
      "module": "$ModuleUpper",
      "hostAlias": "${module}-${profileName}",
      "sshHostEnvKey": "${ModuleUpper}_SSH_HOST",
      "sshUserEnvKey": "${ModuleUpper}_SSH_USER",
      "deployBase": "/opt/cpf/$projectName",
      "healthUrl": "http://localhost:$Port/actuator/health",
      "serviceName": "$projectName",
      "portEnvKey": "${ModuleUpper}_SERVER_PORT",
      "profile": "$profileName",
      "runtimeMode": "embedded-bootjar",
      "approvalRequired": true,
      "rollbackEnabled": true
    }
  ]
}
"@
}

$domainDatabaseProfile = @"
{
  "profileVersion": 1,
  "profileName": "$module-local-dev",
  "environment": "development",
  "domainName": "$module",
  "systemCode": "$SystemCode",
  "database": {
    "vendor": "$DatabaseVendor",
    "host": "$DatabaseHost",
    "port": $DatabasePort,
    "databaseName": "$DatabaseName",
    "schemaName": "$DatabaseSchema",
    "clientPath": "$DatabaseClientPath",
    "admin": {
      "username": "$DatabaseAdminUsername",
      "password": {
        "env": "${ModuleUpper}_DB_ADMIN_PASSWORD",
        "fallbackEnv": "CPF_DB_ROOT_PASSWORD"
      }
    },
    "migration": {
      "username": "$DatabaseMigrationUsername",
      "password": {
        "env": "${ModuleUpper}_DB_MIGRATION_PASSWORD",
        "fallbackEnv": "CPF_DB_MIGRATION_PASSWORD",
        "devDefault": "Cpf${ModuleUpper}Mig#2026"
      }
    },
    "runtime": {
      "username": "$DatabaseRuntimeUsername",
      "password": {
        "env": "${ModuleUpper}_DB_RUNTIME_PASSWORD",
        "fallbackEnv": "CPF_DB_APP_PASSWORD",
        "devDefault": "Cpf${ModuleUpper}App#2026"
      }
    }
  },
  "seed": {
    "product": true,
    "optionalSample": false,
    "test": false
  }
}
"@


$centerCutHandlerSource = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.spi.centercut.CenterCutHandler;
import org.springframework.stereotype.Component;

/**
 * BAT Center-Cut Runner가 local/remote 양쪽에서 호출할 수 있는 $ModuleName 업무 Handler 예제입니다.
 * 실제 업무에서는 businessKey/idempotency를 기준으로 동일 item 중복 실행을 안전하게 처리하십시오.
 */
@Component
public class ${ModuleName}CenterCutHandler implements CenterCutHandler {
    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        if (target == null) throw new IllegalArgumentException("target은 필수입니다.");
        return CpfCenterCutResult.success(target, "$ModuleName center-cut handled", target.payload());
    }
}
"@

$centerCutControllerSource = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 분리 WAS BAT가 Gateway 재경유 없이 호출하는 내부 Center-Cut endpoint입니다. */
@RestController
@RequestMapping("/internal/$module/center-cut")
public class ${ModuleName}CenterCutController {
    private final ${ModuleName}CenterCutHandler handler;
    public ${ModuleName}CenterCutController(${ModuleName}CenterCutHandler handler) { this.handler = handler; }

    @PostMapping("/{jobId}/items")
    @CpfOnlineTransaction(
        id = "S${DomainIdCode}CC0001",
        name = "${ModuleName}CenterCutItem",
        ownerDomain = "$DomainIdCode",
        visibility = "INTERNAL",
        gatewayAllowed = false,
        directAllowed = true)
    public ResponseEntity<CpfCenterCutResult> execute(@PathVariable String jobId, @RequestBody CpfCenterCutTarget target) {
        if (!jobId.equals(target.centerCutJobId())) throw new IllegalArgumentException("jobId와 target.centerCutJobId가 일치해야 합니다.");
        return ResponseEntity.ok(handler.handle(target));
    }
}
"@

$centerCutHandlerTest = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class ${ModuleName}CenterCutHandlerTest {
    @Test
    void handlesTargetThroughPublicCenterCutContract() {
        CpfCenterCutTarget target = new CpfCenterCutTarget(
            "T-1", "$ModuleUpper-CC-1", "BUSINESS-1", LocalDate.of(2026,7,25), "{}",
            "TX-1", "SEG-P", "SEG-C", 0, CpfCenterCutStatus.RUNNING);
        assertThat(new ${ModuleName}CenterCutHandler().handle(target).status()).isEqualTo(CpfCenterCutStatus.SUCCESS);
    }
}
"@

$files = [ordered]@{
    "build.gradle" = $buildGradle
    "README.md" = $readme
    "manifest/domain-manifest.json" = $domainManifest
    "deploy/database/database-profile.json" = $domainDatabaseProfile
    "manifest/ownership.json" = $ownershipManifest
    "manifest/standard-execution-catalog.json" = $executionCatalogManifest
    "src/main/resources/application.yml" = $applicationYml
    "src/main/resources/application-${module}.yml" = $applicationModuleYml
    "src/main/java/$packagePath/${ModuleClassName}Application.java" = $applicationJava
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseController.java" = $moduleBaseController
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseService.java" = $moduleBaseService
    "src/main/java/$packagePath/common/contract/${ModuleClassName}ApplicationFacade.java" = $moduleFacadeContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}RepositoryPort.java" = $moduleRepositoryContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Request.java" = $moduleRequestContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Response.java" = $moduleResponseContract
    "src/main/java/$featurePackagePath/facade/${FeatureClassPrefix}Facade.java" = $facade
    "src/main/java/$featurePackagePath/port/${FeatureClassPrefix}QueryPort.java" = $queryPortSource
    "src/main/java/$featurePackagePath/service/${FeatureClassPrefix}Service.java" = $service
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SearchRequest.java" = $dto
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SampleCommand.java" = $sampleCommand
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SampleItem.java" = $sampleItem
    "src/main/java/$featurePackagePath/validation/${FeatureClassPrefix}SearchValidator.java" = $validator
    "src/test/java/$featurePackagePath/service/${FeatureClassPrefix}ServiceTest.java" = $serviceTest
    "smoke/smoke-${module}.ps1" = $smokeScript
}

if ($DatabaseEnabled) {
    $files["src/main/java/$packagePath/config/${ModuleName}DataSourceConfig.java"] = $dataSourceConfig
    $files["src/main/java/$packagePath/config/${ModuleName}MyBatisConfig.java"] = $myBatisConfig
    $files["src/main/java/$featurePackagePath/adapter/local/Local${FeatureClassPrefix}QueryAdapter.java"] = $localAdapter
    $files["src/main/java/$featurePackagePath/repository/${FeatureClassPrefix}Repository.java"] = $repository
}
if ($ExternalEnabled) {
    $files["src/main/java/$featurePackagePath/adapter/remote/Remote${FeatureClassPrefix}QueryProxy.java"] = $remoteProxy
}
if (-not $DatabaseEnabled -and -not $ExternalEnabled) {
    $files["src/main/java/$featurePackagePath/adapter/memory/InMemory${FeatureClassPrefix}QueryAdapter.java"] = $inMemoryAdapter
}
if ($UiEnabled) {
    $files["ui/src/features/reference/${FeatureClassPrefix}Page.vue"] = $uiComponent
    $files["ui/src/features/reference/${FeatureClassPrefix}Api.ts"] = $uiApi
}

if ($MessagingEnabled) {
    $files["src/main/java/$featurePackagePath/messaging/${ModuleName}EventPublisher.java"] = $messagingPublisher
    $files["src/test/java/$featurePackagePath/messaging/${ModuleName}EventPublisherTest.java"] = $messagingTest
    if ($OnlineEnabled) {
        $files["src/main/java/$featurePackagePath/messaging/${ModuleName}MessagingController.java"] = $messagingController
    }
}
if ($FileEnabled) {
    $files["src/main/java/$featurePackagePath/file/${ModuleName}FileTransferService.java"] = $fileTransferService
    $files["src/test/java/$featurePackagePath/file/${ModuleName}FileTransferServiceTest.java"] = $fileTransferTest
}
if ($CenterCutEnabled) {
    $files["src/main/java/$featurePackagePath/centercut/${ModuleName}CenterCutHandler.java"] = $centerCutHandlerSource
    $files["src/main/java/$featurePackagePath/centercut/${ModuleName}CenterCutController.java"] = $centerCutControllerSource
    $files["src/test/java/$featurePackagePath/centercut/${ModuleName}CenterCutHandlerTest.java"] = $centerCutHandlerTest
}

if ($SecurityAuditEnabled) {
    $files["src/main/java/$featurePackagePath/security/${ModuleName}OperationGuard.java"] = $securityAuditGuard
    $files["src/test/java/$featurePackagePath/security/${ModuleName}OperationGuardTest.java"] = $securityAuditTest
    if ($OnlineEnabled) {
        $files["src/main/java/$featurePackagePath/security/${ModuleName}OperationController.java"] = $securityAuditController
    }
}

if ($OnlineEnabled) {
    $files["src/main/java/$featurePackagePath/controller/${FeatureClassPrefix}Controller.java"] = $controller
}
if ($BatchEnabled) {
    $files["src/main/java/$featurePackagePath/batch/${FeatureClassPrefix}BatchConfig.java"] = $batchConfig
    $files["src/main/java/$packagePath/config/${ModuleName}BatchRepositoryConfig.java"] = $batchRepositoryConfig
}

if ($ProductionProfileEnabled) {
    foreach ($entry in $profileApplicationFiles.GetEnumerator()) {
        $files[$entry.Key] = $entry.Value
    }
}

foreach ($entry in $files.GetEnumerator()) {
    $path = Join-Path $OutputDir $entry.Key
    if (Test-Path -LiteralPath $path) {
        throw "Generated file already exists. path=$path"
    }
    Write-Utf8 -Path $path -Content $entry.Value
    $plan.generatedFiles += $path.Substring($Root.Length).TrimStart('\', '/')
}

if ($GeneratePatch) {
    Write-Warning "GeneratePatch는 1.0.0부터 중간 후보 파일을 생성하지 않습니다. SQL과 배포 설정은 승인된 별도 절차로 반영하세요."
}

if ($Apply) {
    # Apply 모드는 생성 모듈을 저장소에 유지하고 settings 연결까지 원자적으로 완료합니다.
    # 정본 SQL 합본은 version 충돌 검토가 필요하므로 생성된 patch candidate를 별도 동기화 단계에서 반영합니다.
    $settingsText = [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
    if ($settingsText -notmatch "(?m)^include '$([regex]::Escape($projectName))'$" -and
            $settingsText -notmatch "(?m)^include .*'$([regex]::Escape($projectName))'") {
        $settingsText = $settingsText.TrimEnd() + "`r`n`r`ninclude '$projectName'`r`nproject(':$projectName').projectDir = file('$projectName')`r`n"
        [System.IO.File]::WriteAllText($settingsPath, $settingsText, $Utf8NoBom)
        $plan.patchFiles += 'settings.gradle'
    }
}

# 제거 도구는 생성 당시 checksum과 현재 파일을 비교해 사용자 변경을 보호합니다.
$ownedFiles = @()
foreach ($relativePath in @($plan.generatedFiles)) {
    $absolutePath = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        continue
    }
    $moduleRelativePath = $absolutePath.Substring($OutputDir.Length).TrimStart('\', '/')
    $ownedFiles += [ordered]@{
        path = $moduleRelativePath.Replace('\', '/')
        sha256 = (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
$generatorOwnership = [ordered]@{
    generatorVersion = "3.1"
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    moduleCode = $ModuleUpper
    systemCode = $SystemCode
    domainName = $module
    projectName = $projectName
    moduleName = $ModuleName
    packageName = $PackageName
    schemaName = $SchemaName
    tablePrefix = $TablePrefix
    moduleDirectory = $projectName
    outputDirectory = $OutputDir
    capabilities = [ordered]@{
        online = $OnlineEnabled
        database = $DatabaseEnabled
        databaseVendor = $DatabaseVendor
        batch = $BatchEnabled
        centerCut = $CenterCutEnabled
        external = $ExternalEnabled
        messaging = $MessagingEnabled
        file = $FileEnabled
        securityAudit = $SecurityAuditEnabled
        ui = $UiEnabled
        productionProfile = $ProductionProfileEnabled
    }
    createdFiles = $ownedFiles
    modifiedGlobalFiles = @(
        [ordered]@{
            path = "settings.gradle"
            managedLines = @("include '$projectName'", "project(':$projectName').projectDir = file('$projectName')")
        }
    )
    databaseRemovalPolicy = "운영 DB 객체는 자동 삭제하지 않으며 별도 승인 migration으로 처리합니다."
}
$ownershipPath = Join-Path $OutputDir "manifest/generator-ownership.json"
Write-Utf8 -Path $ownershipPath -Content ($generatorOwnership | ConvertTo-Json -Depth 20)
$plan.generatedFiles += $ownershipPath.Substring($Root.Length).TrimStart('\', '/')

if ($ProvisionDatabase) {
    $dbInitializer = Join-Path $Root "cpf-tools/scripts/initialize-domain-database.ps1"
    if (-not (Test-Path -LiteralPath $dbInitializer -PathType Leaf)) {
        throw "공식 Domain DB 초기화 Script가 없습니다: $dbInitializer"
    }

    $generatedProfilePath = Join-Path $Root "$projectName/deploy/database/database-profile.json"
    if (-not (Test-Path -LiteralPath $generatedProfilePath -PathType Leaf)) {
        throw "생성된 Domain DB Profile이 없습니다: $generatedProfilePath"
    }

    # DB 접속/계정/Vendor는 생성된 Git-tracked Domain Profile을 단일 정본으로 사용합니다.
    # Secret은 Profile의 env/fallback/devDefault 정책으로 initialize-domain-database.ps1가 해석합니다.
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $dbInitializer `
        -DomainName $module `
        -SystemCode $SystemCode `
        -Root $Root `
        -ProfilePath $generatedProfilePath `
        -Operation bootstrap `
        -Apply
    if ($LASTEXITCODE -ne 0) {
        throw "Domain Source 생성은 완료됐지만 DB bootstrap이 실패했습니다. project=$projectName"
    }

    $plan.databaseProvisioned = $true
    $plan.databaseProvisioningScript = 'cpf-tools/scripts/initialize-domain-database.ps1'
    $plan.databaseProfile = "$projectName/deploy/database/database-profile.json"
}

$resultPath = if ($Apply) {
    Join-Path $Root "build/reports/create-domain/$module/create-domain-result.json"
} else {
    Join-Path $OutputDir "create-domain-result.json"
}
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
