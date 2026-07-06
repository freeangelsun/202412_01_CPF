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
 * ${ModuleName} 업무 API 예시입니다.
 *
 * <p>신규 주제영역은 Controller에서 요청 검증과 Swagger 설명을 담당하고,
 * 실제 업무 규칙은 Service/Facade 계층으로 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/$module")
@RequiredArgsConstructor
@Tag(name = "$ModuleUpper 업무", description = "${ModuleName} 신규 업무 API")
public class ${ModuleName}Controller {
    private final ${ModuleName}Facade facade;
    private final ${ModuleName}SearchValidator validator;

    @GetMapping
    @CpfTransaction(id = "${ModuleUpper}01SRH0010", name = "${ModuleName}Search")
    @Operation(summary = "${ModuleName} 목록 조회", description = "검색, 정렬, 페이징 whitelist를 적용해 목록을 조회합니다.")
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
 * Controller와 업무 서비스 사이의 조립 계층입니다.
 *
 * <p>여러 service 호출, 외부 연계, 감사 사유 조립이 필요한 경우 Facade에서 흐름을 정리합니다.</p>
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
 * ${ModuleName} 업무 규칙을 처리하는 서비스입니다.
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
 * MyBatis mapper 호출을 캡슐화하는 저장소입니다.
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
 * ${ModuleName} 조회 조건입니다.
 *
 * <p>정렬 컬럼은 whitelist만 허용해 SQL Injection을 차단합니다.</p>
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
 * ${ModuleName} 요청값을 업무 API 진입점에서 검증합니다.
 */
@Component
public class ${ModuleName}SearchValidator {
    public void validate(${ModuleName}SearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("${ModuleName} 조회 조건이 필요합니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하만 허용합니다.");
        }
    }
}
"@

$mapperXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="$BasePackage.mapper.${ModuleName}Mapper">
  <!-- ${ModuleName} 목록 조회: 정렬 컬럼은 DTO whitelist에서만 전달합니다. -->
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

cpf:
  framework:
    module-id: $ModuleUpper
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}.log"
"@

$applicationModuleYml = @"
# ${ModuleName} 업무 모듈 로컬 설정 후보입니다.
spring:
  config:
    activate:
      on-profile: local
  application:
    name: cpf-$module

cpf:
  framework:
    module-id: $ModuleUpper
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}.log"
"@

$sql = @"
-- ${ModuleName} 신규 업무 테이블 후보입니다.
CREATE TABLE IF NOT EXISTS ${TablePrefix}_sample (
    ${TablePrefix}_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '${ModuleName} 식별자',
    ${TablePrefix}_name VARCHAR(200) NOT NULL COMMENT '${ModuleName} 명',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (${TablePrefix}_id),
    INDEX ix_${TablePrefix}_sample_status (status_code, deleted_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='${ModuleName} 샘플';
"@

$readme = @"
# ${ModuleName} 도메인 생성 후보

이 폴더는 `scripts/create-domain.ps1`가 만든 신규 주제영역 적용 후보입니다.

- 실제 적용 전 `settings.gradle`, `specs/sql`, ADM 메뉴/API/button seed, OpenAPI 설명을 검토합니다.
- Controller, Service, Repository, DTO, Mapper XML, SQL 후보가 같은 업무명과 table prefix를 사용합니다.
- 파일 로그는 `cpf-{moduleCode}-{logType}.log` 규격을 따릅니다.
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
# ${ModuleName} 도메인 적용 후보 순서

1. `settings.gradle.patch` 내용을 검토한 뒤 신규 모듈 include 여부를 결정합니다.
2. `${module}/` 후보 소스와 `application-${module}.yml`을 신규 모듈 위치로 옮깁니다.
3. `sql/Vxx__${module}_domain.sql`을 실제 Flyway version 번호로 복사합니다.
4. `sql/40_business_modules_schema.${module}.candidate.sql`을 split SQL과 all_install 합본에 반영합니다.
5. `sql/50_framework_seed.${module}.candidate.sql`로 PFW module registry seed를 반영합니다.
6. `sql/60_adm_seed.${module}.candidate.sql`로 ADM menu/API/button 권한 seed를 반영합니다.
7. `sql/99_smoke_check.${module}.candidate.sql`을 smoke check에 반영합니다.
8. `smoke-${module}.ps1`을 실제 포트와 API path에 맞춰 검증합니다.

이 묶음은 안전한 후보입니다. 운영 SQL과 settings를 자동 수정하지 않습니다.
"@

$settingsPatch = @"
--- a/settings.gradle
+++ b/settings.gradle
@@
+include '$module'
"@

$pfwSeed = @"
-- ${ModuleName} PFW module registry seed 후보입니다.
INSERT INTO pfw_module_registry (module_code, module_name, module_type, active_yn, created_by, updated_by)
VALUES ('$ModuleUpper', '${ModuleName}', 'BUSINESS', 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE module_name = VALUES(module_name), active_yn = VALUES(active_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
"@

$admSeed = @"
-- ${ModuleName} ADM 메뉴/API/버튼 권한 seed 후보입니다.
INSERT INTO adm_menu (menu_id, parent_menu_id, menu_name, menu_path, sort_order, use_yn, created_by, updated_by)
VALUES ('${ModuleUpper}_ROOT', 'BIZ_ROOT', '${ModuleName}', '/adm/${module}', 900, 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_path = VALUES(menu_path), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (api_permission_id, api_path, http_method, permission_code, use_yn, created_by, updated_by)
VALUES ('${ModuleUpper}_SEARCH', '/api/v1/${module}', 'GET', '${ModuleUpper}_READ', 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE api_path = VALUES(api_path), http_method = VALUES(http_method), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
"@

$smokeSql = @"
-- ${ModuleName} smoke check 후보입니다.
SELECT '${ModuleUpper}_SAMPLE_TABLE' AS check_id, COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = '${TablePrefix}_sample';
"@

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
}

$resultPath = Join-Path $OutputDir "create-domain-result.json"
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
