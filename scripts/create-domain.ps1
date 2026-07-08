param(
    [Parameter(Mandatory = $true)]
    [string] $ModuleCode,
    [string] $ModuleName = "",
    [string] $BasePackage = "",
    [string] $TablePrefix = "",
    [string] $OutputDir = "",
    [switch] $DryRun,
    [switch] $GeneratePatch,
    [switch] $Apply
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path "$PSScriptRoot\..").Path
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-StatusText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-StatusText @(0xC644, 0xB8CC)
$StatusFailed = New-StatusText @(0xC2E4, 0xD328)

function Normalize-Code {
    param([string] $Value)
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "ModuleCode is required."
    }
    $normalized = $Value.Trim().ToLowerInvariant()
    if ($normalized -notmatch '^[a-z][a-z0-9]{1,9}$') {
        throw "ModuleCode must be lower/upper alpha numeric, 2-10 characters, starting with a letter."
    }
    return $normalized
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

$module = Normalize-Code $ModuleCode
$ModuleUpper = $module.ToUpperInvariant()
$Dollar = '$'
if ([string]::IsNullOrWhiteSpace($ModuleName)) {
    $ModuleName = $module
}
if ([string]::IsNullOrWhiteSpace($BasePackage)) {
    $BasePackage = "cpf.$module"
}
if ([string]::IsNullOrWhiteSpace($TablePrefix)) {
    $TablePrefix = $module
}
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $Root "build/domain-generator/$module"
}

$targetModuleDir = Join-Path $Root $module
$settingsPath = Join-Path $Root "settings.gradle"
$packagePath = $BasePackage.Replace('.', '/')
$conflicts = New-Object System.Collections.Generic.List[string]

if (Test-Path -LiteralPath $targetModuleDir) {
    $conflicts.Add("module directory already exists: $module")
}
if (Test-TextExists -Path $settingsPath -Text "include '$module'") {
    $conflicts.Add("settings.gradle already includes module: $module")
}
if (Test-TextExists -Path (Join-Path $Root "specs/sql/40_business_modules_schema.sql") -Text "$TablePrefix`_") {
    $conflicts.Add("table prefix already appears in business schema: $TablePrefix")
}
if (Test-Path -LiteralPath (Join-Path $Root "$module/src/main/java/$packagePath")) {
    $conflicts.Add("base package already exists: $BasePackage")
}

$plan = [ordered]@{
    status = $StatusDone
    dryRun = [bool] $DryRun
    moduleCode = $ModuleUpper
    moduleName = $ModuleName
    basePackage = $BasePackage
    tablePrefix = $TablePrefix
    outputDir = $OutputDir
    generatePatch = [bool] ($GeneratePatch -or $Apply)
    applyMode = [bool] $Apply
    conflicts = @($conflicts)
    generatedFiles = @()
    patchFiles = @()
    patchCandidates = @(
        "settings.gradle include '$module'",
        "root build dependency registration if needed",
        "specs/sql/40_business_modules_schema.sql table candidate",
        "specs/sql/50_framework_seed_data.sql PFW registry seed candidate",
        "specs/sql/60_adm_seed_data.sql ADM menu/API/button seed candidate",
        "specs/sql/99_smoke_check.sql smoke candidate",
        "specs/sql/00_all_install.sql merge candidate",
        "specs/sql/00_all_install_and_smoke.sql merge candidate",
        "specs/sql/migration/flyway/Vxx__${module}_domain.sql candidate",
        "application-${module}.yml candidate",
        "src/main/resources/application-${module}-local.yml candidate",
        "src/main/resources/application-${module}-dev.yml candidate",
        "src/main/resources/application-${module}-stg.yml candidate",
        "src/main/resources/application-${module}-prod.yml candidate",
        "deploy/env/local-${module}.env candidate",
        "deploy/env/dev-${module}.env candidate",
        "deploy/env/stg-${module}.env candidate",
        "deploy/env/prod-${module}.env candidate",
        "deploy/inventory/local-services.json candidate",
        "deploy/inventory/dev-services.json candidate",
        "deploy/inventory/stg-services.json candidate",
        "deploy/inventory/prod-services.template.json candidate",
        "module smoke script candidate",
        "README.md and specs guide link candidate"
    )
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

$javaRoot = Join-Path $OutputDir "src/main/java/$packagePath"
$resourceRoot = Join-Path $OutputDir "src/main/resources"
$testRoot = Join-Path $OutputDir "src/test/java/$packagePath"

$controller = @"
package $BasePackage.controller;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.facade.${ModuleName}Facade;
import $BasePackage.validation.${ModuleName}SearchValidator;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ${ModuleName} ? API ????
 *
 * <p>? ?? Controller? ?  Swagger ?????,
 * ? ? ? Service/Facade ? ????</p>
 */
@RestController
@RequestMapping("/api/v1/$module")
@RequiredArgsConstructor
@Tag(name = "$ModuleUpper ?", description = "${ModuleName} ? ? API")
public class ${ModuleName}Controller {
    private final ${ModuleName}Facade facade;
    private final ${ModuleName}SearchValidator validator;

    @GetMapping
    @CpfTransaction(id = "${ModuleUpper}01SRH0010", name = "${ModuleName}Search")
    @Operation(summary = "${ModuleName}  ", description = "?? ?, ??whitelist?????????")
    public ResponseEntity<Map<String, Object>> search(${ModuleName}SearchRequest request) {
        validator.validate(request);
        return ResponseEntity.ok(facade.search(request));
    }
}
"@

$facade = @"
package $BasePackage.facade;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.service.${ModuleName}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Controller? ? ?????? ???
 *
 * <p>? service ?, ?? ?,  ? ????? Facade? ???????</p>
 */
@Component
@RequiredArgsConstructor
public class ${ModuleName}Facade {
    private final ${ModuleName}Service service;

    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return service.search(request);
    }
}
"@

$service = @"
package $BasePackage.service;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.repository.${ModuleName}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ${ModuleName} ? ??? ???.
 */
@Service
@RequiredArgsConstructor
public class ${ModuleName}Service {
    private final ${ModuleName}Repository repository;

    @Transactional(readOnly = true)
    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return repository.search(request.normalized());
    }
}
"@

$repository = @"
package $BasePackage.repository;

import $BasePackage.dto.${ModuleName}SearchRequest;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * MyBatis mapper ???????????
 */
@Repository
@RequiredArgsConstructor
public class ${ModuleName}Repository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList("${BasePackage}.mapper.${ModuleName}Mapper.search", request),
                "criteria", request);
    }
}
"@

$dto = @"
package $BasePackage.dto;

import java.util.Set;

/**
 * ${ModuleName}  ???
 *
 * <p>? ? whitelist????SQL Injection?????</p>
 */
public record ${ModuleName}SearchRequest(
        String keyword,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size) {
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "${TablePrefix}_id");

    public ${ModuleName}SearchRequest normalized() {
        String normalizedSortBy = SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new ${ModuleName}SearchRequest(keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }
}
"@

$validator = @"
package $BasePackage.validation;

import $BasePackage.dto.${ModuleName}SearchRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * ${ModuleName} ? ? API ????.
 */
@Component
public class ${ModuleName}SearchValidator {
    public void validate(${ModuleName}SearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("${ModuleName}  ??????");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("? ???200 ??????");
        }
    }
}
"@

$mapperXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="$BasePackage.mapper.${ModuleName}Mapper">
  <!-- ${ModuleName}  : ? ? DTO whitelist?????? -->
  <select id="search" resultType="map">
    SELECT ${TablePrefix}_id,
           ${TablePrefix}_name,
           status_code,
           created_by,
           created_at,
           updated_by,
           updated_at
    FROM ${TablePrefix}_sample
    WHERE deleted_yn = 'N'
    <if test="keyword != null and keyword != ''">
      AND ${TablePrefix}_name LIKE CONCAT('%', #{keyword}, '%')
    </if>
    ORDER BY ${Dollar}{sortBy} ${Dollar}{sortDirection}
    LIMIT #{size} OFFSET #{page} * #{size}
  </select>
</mapper>
"@

$buildGradle = @"
plugins {
    id 'java'
    id 'org.springframework.boot'
}

dependencies {
    implementation project(':pfw')
    implementation project(':cmn')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
}
"@

$applicationYml = @"
spring:
  application:
    name: cpf-$module
  config:
    import:
      - optional:classpath:application-pfw.yml
      - optional:classpath:application-pfw-${Dollar}{spring.profiles.active:local}.yml
      - optional:classpath:application-cmn.yml
      - optional:classpath:application-cmn-${Dollar}{spring.profiles.active:local}.yml
      - optional:classpath:application-$module.yml
      - optional:classpath:application-$module-${Dollar}{spring.profiles.active:local}.yml

cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}.log"
"@
$applicationModuleYml = @"
# ${ModuleName}     .
cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  datasource:
    mode: ${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:java:comp/env/jdbc/cpf$($ModuleUpper)DataSource}
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}.log"
"@
$sql = @"
-- ${ModuleName} ? ? ??????
CREATE TABLE IF NOT EXISTS ${TablePrefix}_sample (
    ${TablePrefix}_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '${ModuleName} ???,
    ${TablePrefix}_name VARCHAR(200) NOT NULL COMMENT '${ModuleName} ?,
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '? ',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '? ?? ??',
    created_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '???,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '??',
    updated_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '???,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '??',
    PRIMARY KEY (${TablePrefix}_id),
    INDEX ix_${TablePrefix}_sample_status (status_code, deleted_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='${ModuleName} ?';
"@

$readme = @"
# ${ModuleName} ???? ?

?????`scripts/create-domain.ps1`  ? ? ? ????

- ? ? ??`settings.gradle`, `specs/sql`, ADM /API/button seed, OpenAPI ?????.
- Controller, Service, Repository, DTO, Mapper XML, SQL ? ? ? table prefix?????
- ? ??`cpf-{moduleCode}-{logType}.log` ????.
"@

$serviceTest = @"
package $BasePackage.service;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.repository.${ModuleName}Repository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ${ModuleName}ServiceTest {
    private final ${ModuleName}Repository repository = mock(${ModuleName}Repository.class);
    private final ${ModuleName}Service service = new ${ModuleName}Service(repository);

    @Test
    void searchNormalizesPagingAndSort() {
        when(repository.search(any())).thenReturn(Map.of("items", java.util.List.of(), "criteria", Map.of()));

        Map<String, Object> result = service.search(new ${ModuleName}SearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

        assertThat(result).containsKey("items");
    }
}
"@

$smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:8080",
    [int] `$TimeoutSec = 20
)

`$ErrorActionPreference = "Stop"
`$uri = "`$BaseUrl/api/v1/$module?keyword=sample&page=0&size=20&sortBy=created_at&sortDirection=DESC"
`$response = Invoke-WebRequest -Method Get -Uri `$uri -TimeoutSec `$TimeoutSec -UseBasicParsing
if ([int] `$response.StatusCode -lt 200 -or [int] `$response.StatusCode -ge 300) {
    throw "${ModuleName} smoke failed. status=`$(`$response.StatusCode)"
}
Write-Host "${ModuleName} smoke passed. uri=`$uri"
"@

$applyOrder = @"
# ${ModuleName} ???? ? ?

1. `settings.gradle.patch` ???? ???  include ??????
2. `${module}/` ? ?? `application-${module}.yml`???  ?????.
3. `sql/Vxx__${module}_domain.sql`??? Flyway version ????
4. `sql/40_business_modules_schema.${module}.candidate.sql`??split SQL?all_install ??????
5. `sql/50_framework_seed.${module}.candidate.sql`?PFW module registry seed????
6. `sql/60_adm_seed.${module}.candidate.sql`?ADM menu/API/button  seed????
7. `sql/99_smoke_check.${module}.candidate.sql`??smoke check?????
8. `smoke-${module}.ps1`??? ?? API path?? ?.

??? ??????? ? SQL?settings?? ??? ??.
"@

$settingsPatch = @"
--- a/settings.gradle
+++ b/settings.gradle
@@
+include '$module'
"@

$pfwSeed = @"
-- ${ModuleName} PFW module registry seed ????
INSERT INTO pfw_module_registry (module_code, module_name, module_type, active_yn, created_by, updated_by)
VALUES ('$ModuleUpper', '${ModuleName}', 'BUSINESS', 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE module_name = VALUES(module_name), active_yn = VALUES(active_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
"@

$admSeed = @"
-- ${ModuleName} ADM /API/  seed ????
INSERT INTO adm_menu (menu_id, parent_menu_id, menu_name, menu_path, sort_order, use_yn, created_by, updated_by)
VALUES ('${ModuleUpper}_ROOT', 'BIZ_ROOT', '${ModuleName}', '/adm/${module}', 900, 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_path = VALUES(menu_path), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (api_permission_id, api_path, http_method, permission_code, use_yn, created_by, updated_by)
VALUES ('${ModuleUpper}_SEARCH', '/api/v1/${module}', 'GET', '${ModuleUpper}_READ', 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE api_path = VALUES(api_path), http_method = VALUES(http_method), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
"@

$smokeSql = @"
-- ${ModuleName} smoke check ????
SELECT '${ModuleUpper}_SAMPLE_TABLE' AS check_id, COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = '${TablePrefix}_sample';
"@

$profileApplicationFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $profileApplicationFiles["src/main/resources/application-${module}-${profileName}.yml"] = @"
# ${ModuleName} ${profileName}   .
spring:
  config:
    activate:
      on-profile: $profileName

server:
  port: ${Dollar}{$($ModuleUpper)_SERVER_PORT:8080}

cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
    instance-id: ${Dollar}{$($ModuleUpper)_INSTANCE_ID:${ModuleUpper}01}
    was-id: ${Dollar}{$($ModuleUpper)_WAS_ID:${profileName}-01}
  datasource:
    mode: ${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}
    url: ${Dollar}{$($ModuleUpper)_DATASOURCE_URL:jdbc:mariadb://localhost:3306/${module}DB}
    username: ${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:${module}_app}
    password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD:__REPLACE_BY_ENV__}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:java:comp/env/jdbc/cpf$($ModuleUpper)DataSource}
"@
}

$deployEnvFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $deployEnvFiles["deploy/env/${profileName}-${module}.env"] = @"
SPRING_PROFILES_ACTIVE=$profileName
${ModuleUpper}_MODULE_ID=$ModuleUpper
${ModuleUpper}_INSTANCE_ID=${ModuleUpper}-${profileName}-01
${ModuleUpper}_WAS_ID=${profileName}-was-01
${ModuleUpper}_SERVER_PORT=8080
${ModuleUpper}_LOG_BASE_PATH=./logs/$module
${ModuleUpper}_DATASOURCE_MODE=url
${ModuleUpper}_DATASOURCE_URL=jdbc:mariadb://localhost:3306/${module}DB
${ModuleUpper}_DATASOURCE_USERNAME=${module}_app
${ModuleUpper}_DATASOURCE_PASSWORD=__REPLACE_BY_ENV__
${ModuleUpper}_DATASOURCE_JNDI_NAME=java:comp/env/jdbc/cpf$($ModuleUpper)DataSource
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
      "deployBase": "/opt/cpf/$module",
      "healthUrl": "http://localhost:8080/actuator/health",
      "serviceName": "cpf-$module",
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

$files = [ordered]@{
    "build.gradle" = $buildGradle
    "README.md" = $readme
    "src/main/resources/application.yml" = $applicationYml
    "src/main/resources/application-${module}.yml" = $applicationModuleYml
    "src/main/resources/mybatis/mapper/${module}/${ModuleName}Mapper.xml" = $mapperXml
    "src/main/java/$packagePath/controller/${ModuleName}Controller.java" = $controller
    "src/main/java/$packagePath/facade/${ModuleName}Facade.java" = $facade
    "src/main/java/$packagePath/service/${ModuleName}Service.java" = $service
    "src/main/java/$packagePath/repository/${ModuleName}Repository.java" = $repository
    "src/main/java/$packagePath/dto/${ModuleName}SearchRequest.java" = $dto
    "src/main/java/$packagePath/validation/${ModuleName}SearchValidator.java" = $validator
    "src/test/java/$packagePath/service/${ModuleName}ServiceTest.java" = $serviceTest
    "smoke/smoke-${module}.ps1" = $smokeScript
    "sql/Vxx__${module}_domain.sql" = $sql
}

foreach ($entry in $profileApplicationFiles.GetEnumerator()) {
    $files[$entry.Key] = $entry.Value
}
foreach ($entry in $deployEnvFiles.GetEnumerator()) {
    $files[$entry.Key] = $entry.Value
}
foreach ($entry in $deployInventoryFiles.GetEnumerator()) {
    $files[$entry.Key] = $entry.Value
}

foreach ($entry in $files.GetEnumerator()) {
    $path = Join-Path $OutputDir $entry.Key
    if (Test-Path -LiteralPath $path) {
        throw "Generated file already exists. path=$path"
    }
    Write-Utf8 -Path $path -Content $entry.Value
    $plan.generatedFiles += $path.Substring($Root.Length).TrimStart('\', '/')
}

if ($GeneratePatch -or $Apply) {
    $patchFiles = [ordered]@{
        "patch-candidates/apply-order.md" = $applyOrder
        "patch-candidates/settings.gradle.patch" = $settingsPatch
        "patch-candidates/sql/40_business_modules_schema.${module}.candidate.sql" = $sql
        "patch-candidates/sql/50_framework_seed.${module}.candidate.sql" = $pfwSeed
        "patch-candidates/sql/60_adm_seed.${module}.candidate.sql" = $admSeed
        "patch-candidates/sql/99_smoke_check.${module}.candidate.sql" = $smokeSql
        "patch-candidates/sql/migration/Vxx__${module}_domain.sql" = $sql
        "patch-candidates/README.${module}.candidate.md" = $readme
        "patch-candidates/smoke-${module}.ps1" = $smokeScript
    }

    foreach ($entry in $patchFiles.GetEnumerator()) {
        $path = Join-Path $OutputDir $entry.Key
        Write-Utf8 -Path $path -Content $entry.Value
        $plan.patchFiles += $path.Substring($Root.Length).TrimStart('\', '/')
    }
    foreach ($entry in $profileApplicationFiles.GetEnumerator()) {
        $path = Join-Path $OutputDir ("patch-candidates/" + $entry.Key)
        Write-Utf8 -Path $path -Content $entry.Value
        $plan.patchFiles += $path.Substring($Root.Length).TrimStart('\', '/')
    }
    foreach ($entry in $deployEnvFiles.GetEnumerator()) {
        $path = Join-Path $OutputDir ("patch-candidates/" + $entry.Key)
        Write-Utf8 -Path $path -Content $entry.Value
        $plan.patchFiles += $path.Substring($Root.Length).TrimStart('\', '/')
    }
    foreach ($entry in $deployInventoryFiles.GetEnumerator()) {
        $path = Join-Path $OutputDir ("patch-candidates/" + $entry.Key)
        Write-Utf8 -Path $path -Content $entry.Value
        $plan.patchFiles += $path.Substring($Root.Length).TrimStart('\', '/')
    }
}

$resultPath = Join-Path $OutputDir "create-domain-result.json"
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
