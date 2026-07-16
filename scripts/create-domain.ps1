param(
    [Parameter(Mandatory = $true)]
    [string] $ModuleCode,
    [string] $Root = "",
    [string] $ModuleName = "",
    [string] $DomainIdCode = "",
    [string] $BasePackage = "",
    [string] $TablePrefix = "",
    [ValidateRange(1024, 65535)]
    [int] $Port = 8080,
    [ValidateSet("Y", "N")]
    [string] $Online = "Y",
    [ValidateSet("Y", "N")]
    [string] $Batch = "N",
    [ValidateSet("Y", "N")]
    [string] $BzaMenu = "Y",
    [string] $OutputDir = "",
    [switch] $DryRun,
    [switch] $GeneratePatch,
    [switch] $Apply
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path "$PSScriptRoot\..").Path
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
if ([string]::IsNullOrWhiteSpace($DomainIdCode)) {
    $DomainIdCode = if ($ModuleUpper.Length -ge 3) {
        $ModuleUpper.Substring(0, 3)
    } else {
        $ModuleUpper.PadRight(3, 'X')
    }
}
$DomainIdCode = $DomainIdCode.Trim().ToUpperInvariant()
if ($DomainIdCode -notmatch '^[A-Z0-9]{3}$') {
    throw "DomainIdCode must be exactly three upper alpha numeric characters."
}
$OnlineEnabled = $Online -eq "Y"
$BatchEnabled = $Batch -eq "Y"
$BzaMenuEnabled = $BzaMenu -eq "Y"
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
    $OutputDir = if ($Apply) {
        Join-Path $Root $module
    } else {
        Join-Path $Root "build/domain-generator/$module"
    }
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
    domainIdCode = $DomainIdCode
    basePackage = $BasePackage
    tablePrefix = $TablePrefix
    port = $Port
    online = $OnlineEnabled
    batch = $BatchEnabled
    bzaMenu = $BzaMenuEnabled
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
        "standard execution catalog manifest",
        "service registry manifest",
        "BZA menu registration candidate",
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
import cpf.pfw.common.base.BaseController;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ${ModuleName} 조회 API를 제공합니다.
 *
 * <p>업무 Controller는 요청 검증과 Swagger 계약을 담당하고,
 * 실제 업무 처리는 Facade와 Service에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/$module")
@RequiredArgsConstructor
@Tag(name = "$ModuleUpper 업무", description = "${ModuleName} 주제영역 조회 API")
public class ${ModuleName}Controller extends BaseController {
    private final ${ModuleName}Facade facade;
    private final ${ModuleName}SearchValidator validator;

    @GetMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0001", name = "${ModuleName}Search", ownerDomain = "$DomainIdCode")
    @Operation(
            operationId = "search${ModuleName}",
            summary = "${ModuleName} 목록 조회",
            description = "검색어, 페이징, 정렬 whitelist를 적용해 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> search(${ModuleName}SearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
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
 * Controller와 업무 서비스를 분리하는 진입 Facade입니다.
 *
 * <p>여러 서비스 조합, 외부 호출, 거래 단위 조정이 필요하면 Facade에서 처리합니다.</p>
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

$queryPortSource = @"
package $BasePackage.port;

import $BasePackage.dto.${ModuleName}SearchRequest;

import java.util.Map;

/**
 * ${ModuleName} 조회 구현을 local 또는 remote adapter로 교체하기 위한 업무 포트입니다.
 */
public interface ${ModuleName}QueryPort {
    Map<String, Object> search(${ModuleName}SearchRequest request);
}
"@

$localAdapter = @"
package $BasePackage.adapter.local;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.port.${ModuleName}QueryPort;
import $BasePackage.repository.${ModuleName}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 같은 주제영역 DB를 사용하는 기본 local adapter입니다.
 */
@Primary
@Component
@RequiredArgsConstructor
public class Local${ModuleName}QueryAdapter implements ${ModuleName}QueryPort {
    private final ${ModuleName}Repository repository;

    @Override
    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return repository.search(request);
    }
}
"@

$remoteProxy = @"
package $BasePackage.adapter.remote;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.port.${ModuleName}QueryPort;
import cpf.pfw.common.http.CpfWebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

/**
 * 분리 배포된 ${ModuleName} 서비스에 PFW 표준 서비스 호출 경계로 접근하는 remote proxy입니다.
 *
 * <p>프로젝트 설정에서 remote 모드를 선택할 때만 Bean으로 등록합니다.</p>
 */
public class Remote${ModuleName}QueryProxy implements ${ModuleName}QueryPort {
    private final CpfWebClient webClient;

    public Remote${ModuleName}QueryProxy(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return webClient.get(
                "$ModuleUpper",
                uriBuilder -> {
                    uriBuilder.path("/api/v1/$module")
                            .queryParam("sortBy", request.sortBy())
                            .queryParam("sortDirection", request.sortDirection())
                            .queryParam("page", request.page())
                            .queryParam("size", request.size());
                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        uriBuilder.queryParam("keyword", request.keyword());
                    }
                    return uriBuilder.build();
                },
                new ParameterizedTypeReference<Map<String, Object>>() { });
    }
}
"@

$service = @"
package $BasePackage.service;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.port.${ModuleName}QueryPort;
import cpf.pfw.common.base.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ${ModuleName} 조회 업무를 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class ${ModuleName}Service extends BaseService {
    private final ${ModuleName}QueryPort queryPort;

    @Transactional(readOnly = true)
    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return queryPort.search(request.normalized());
    }
}
"@

$repository = @"
package $BasePackage.repository;

import $BasePackage.dto.${ModuleName}SearchRequest;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * MyBatis mapper 호출을 캡슐화하는 업무 저장소입니다.
 */
@Repository
public class ${ModuleName}Repository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public ${ModuleName}Repository(
            @Qualifier("${module}SqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public Map<String, Object> search(${ModuleName}SearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList("${BasePackage}.mapper.${ModuleName}Mapper.search", request),
                "criteria", request);
    }
}
"@

$myBatisConfig = @"
package $BasePackage.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/** ${ModuleName} mapper를 해당 주제영역 소유 DataSource와 명시적으로 연결합니다. */
@Configuration(proxyBeanMethods = false)
public class ${ModuleName}MyBatisConfig {

    @Bean(name = "${module}SqlSessionFactory")
    public SqlSessionFactory ${module}SqlSessionFactory(
            @Qualifier("${module}DataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/${module}/**/*.xml"));
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
 * ${ModuleName} 배치 원천 메타를 PFW DB의 Spring Batch 표준 저장소에 기록합니다.
 *
 * <p>업무 데이터 트랜잭션과 배치 메타 트랜잭션을 분리해 업무 DB에
 * Spring Batch 내부 테이블이 생성되거나 조회되는 것을 방지합니다.</p>
 */
@Configuration
public class ${ModuleName}BatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource pfwDataSource;
    private final PlatformTransactionManager pfwTransactionManager;

    public ${ModuleName}BatchRepositoryConfig(
            @Qualifier("pfwDataSource") DataSource pfwDataSource,
            @Qualifier("pfwTransactionManager") PlatformTransactionManager pfwTransactionManager) {
        this.pfwDataSource = pfwDataSource;
        this.pfwTransactionManager = pfwTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return pfwDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return pfwTransactionManager;
    }

    @Override
    protected String getDatabaseType() {
        return "MYSQL";
    }
}
"@

$dto = @"
package $BasePackage.dto;

import java.util.Set;

/**
 * ${ModuleName} 조회 조건입니다.
 *
 * <p>정렬 컬럼은 whitelist로 제한해 SQL Injection을 차단합니다.</p>
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

    public int offset() {
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return normalizedPage * normalizedSize;
    }
}
"@

$validator = @"
package $BasePackage.validation;

import $BasePackage.dto.${ModuleName}SearchRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * ${ModuleName} 조회 API 입력값을 검증합니다.
 */
@Component
public class ${ModuleName}SearchValidator {
    public void validate(${ModuleName}SearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("${ModuleName} 조회 조건은 필수입니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하여야 합니다.");
        }
    }
}
"@

$mapperXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="$BasePackage.mapper.${ModuleName}Mapper">
  <!-- ${ModuleName} 조회: 정렬 컬럼은 DTO whitelist를 통과한 값만 사용합니다. -->
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
    LIMIT #{size} OFFSET #{offset}
  </select>
</mapper>
"@

$batchDependency = if ($BatchEnabled) {
    "    implementation 'org.springframework.boot:spring-boot-starter-batch'"
} else {
    ""
}

$batchConfig = @"
package $BasePackage.batch;

import cpf.pfw.common.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ${ModuleName} 주제영역의 표준 Tasklet 배치 골격입니다.
 */
@Configuration
public class ${ModuleName}BatchConfig {

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
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(rootProject.ext.cpfJavaVersion)
    }
}

dependencies {
    implementation project(':pfw')
    implementation project(':cmn')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
$batchDependency
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
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
 * ${ModuleName} 주제영역 실행 애플리케이션입니다.
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "$BasePackage"})
public class ${ModuleName}Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(${ModuleName}Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(${ModuleName}Application.class);
    }
}
"@

$dataSourceConfig = @"
package $BasePackage.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * embedded URL 연결과 external Tomcat JNDI 연결을 동일한 모듈에서 선택합니다.
 */
@Configuration
public class ${ModuleName}DataSourceConfig {

    @Bean
    @Primary
    public DataSource ${module}DataSource(Environment environment) throws NamingException {
        String mode = environment.getProperty("cpf.datasource.mode", "url");
        if ("jndi".equalsIgnoreCase(mode)) {
            String jndiName = environment.getRequiredProperty("cpf.datasource.jndi-name");
            return new JndiTemplate().lookup(jndiName, DataSource.class);
        }
        return DataSourceBuilder.create()
                .url(environment.getRequiredProperty("cpf.datasource.url"))
                .username(environment.getRequiredProperty("cpf.datasource.username"))
                .password(environment.getRequiredProperty("cpf.datasource.password"))
                .driverClassName("org.mariadb.jdbc.Driver")
                .build();
    }

    /**
     * ${ModuleName} 저장소가 다른 주제영역 DB를 선택하지 않도록 전용 JDBC 접근 객체를 제공합니다.
     */
    @Bean(name = "${module}JdbcTemplate")
    @Primary
    public JdbcTemplate ${module}JdbcTemplate(
            @Qualifier("${module}DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * ${ModuleName} 서비스와 배치가 동일한 업무 트랜잭션 경계를 사용하도록 합니다.
     */
    @Bean(name = "${module}TransactionManager")
    @Primary
    public PlatformTransactionManager ${module}TransactionManager(
            @Qualifier("${module}DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
"@

$applicationYml = @"
spring:
  application:
    name: $module
  flyway:
    # DDL migration은 app 계정이 아니라 별도 migration 절차에서 실행합니다.
    enabled: ${Dollar}{$($ModuleUpper)_FLYWAY_ENABLED:false}
  batch:
    # 배치 Job은 운영 실행 요청으로만 시작하고 애플리케이션 기동 시 자동 실행하지 않습니다.
    job:
      enabled: false
    # Spring Batch 메타 스키마는 pfwDB 설치 SQL이 관리합니다.
    jdbc:
      initialize-schema: never
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
$applicationModuleYml = @"
# ${ModuleName} 주제영역 공통 설정입니다.
cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  datasource:
    mode: ${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:java:comp/env/jdbc/cpf$($ModuleUpper)DataSource}
    url: ${Dollar}{$($ModuleUpper)_DATASOURCE_URL:jdbc:mariadb://localhost:3306/${module}DB}
    username: ${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}
    password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD:}
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log"
"@
$sql = @"
-- ${ModuleName} 업무 샘플 테이블입니다.
CREATE TABLE IF NOT EXISTS ${TablePrefix}_sample (
    ${TablePrefix}_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '${ModuleName} 식별자',
    ${TablePrefix}_name VARCHAR(200) NOT NULL COMMENT '${ModuleName} 명칭',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT '$ModuleUpper' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (${TablePrefix}_id),
    INDEX ix_${TablePrefix}_sample_status (status_code, deleted_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='${ModuleName} 업무 샘플';
"@

$readme = @"
# ${ModuleName} 주제영역 골격

이 디렉터리는 `scripts/create-domain.ps1`로 생성한 신규 업무 모듈 후보입니다.

- 실제 반영 전 `settings.gradle`, `specs/sql`, ADM 메뉴/API/버튼 seed, OpenAPI 문서를 함께 검토합니다.
- Controller, Facade, Service, Repository, DTO, Mapper XML, SQL의 모듈 코드와 테이블 prefix를 일치시킵니다.
- 운영 로그는 `${Dollar}{CPF_LOG_ROOT}/{environment}/{moduleCode}/{instanceId}/{category}/cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log` 규칙을 사용합니다.
"@

$serviceTest = @"
package $BasePackage.service;

import $BasePackage.dto.${ModuleName}SearchRequest;
import $BasePackage.adapter.local.Local${ModuleName}QueryAdapter;
import $BasePackage.repository.${ModuleName}Repository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ${ModuleName}ServiceTest {
    private final AtomicReference<${ModuleName}SearchRequest> capturedRequest = new AtomicReference<>();
    private final ${ModuleName}Repository repository = new ${ModuleName}Repository(null) {
        @Override
        public Map<String, Object> search(${ModuleName}SearchRequest request) {
            // DB 없이도 Service가 정규화한 최종 조건을 검증할 수 있도록 요청을 보관합니다.
            capturedRequest.set(request);
            return Map.of("items", java.util.List.of(), "criteria", request);
        }
    };
    private final ${ModuleName}Service service = new ${ModuleName}Service(new Local${ModuleName}QueryAdapter(repository));

    @Test
    void searchNormalizesPagingAndSort() {
        Map<String, Object> result = service.search(new ${ModuleName}SearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

        assertThat(result).containsKey("items");
        assertThat(capturedRequest.get().sortBy()).isEqualTo("created_at");
        assertThat(capturedRequest.get().sortDirection()).isEqualTo("ASC");
        assertThat(capturedRequest.get().page()).isZero();
        assertThat(capturedRequest.get().size()).isEqualTo(200);
    }
}
"@

$smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:$Port",
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
2. `${module}/` 모듈과 `application-${module}.yml` 설정을 반영합니다.
3. `sql/Vxx__${module}_domain.sql`의 Flyway version을 확정합니다.
4. `sql/40_business_modules_schema.${module}.candidate.sql`을 split SQL과 all_install에 반영합니다.
5. `sql/50_framework_seed.${module}.candidate.sql`의 PFW module registry seed를 반영합니다.
6. `sql/60_adm_seed.${module}.candidate.sql`의 ADM 메뉴/API/버튼 권한 seed를 반영합니다.
7. `sql/99_smoke_check.${module}.candidate.sql`을 smoke check에 반영합니다.
8. `smoke-${module}.ps1`의 포트와 API path를 확인합니다.

후보 파일은 자동 적용하지 않으며 검토 후 정본 SQL과 설정에 반영합니다.
"@

$settingsPatch = @"
--- a/settings.gradle
+++ b/settings.gradle
@@
+include '$module'
"@

$pfwSeed = @"
-- ${ModuleName} PFW 서비스 레지스트리 seed 후보입니다.
INSERT INTO pfw_service (
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

INSERT INTO pfw_service_endpoint (
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
$batchJson = $BatchEnabled.ToString().ToLowerInvariant()
$bzaMenuJson = $BzaMenuEnabled.ToString().ToLowerInvariant()
$domainManifest = @"
{
  "moduleCode": "$ModuleUpper",
  "domainName": "$ModuleName",
  "domainIdCode": "$DomainIdCode",
  "basePackage": "$BasePackage",
  "port": $Port,
  "tablePrefix": "$TablePrefix",
  "onlineEnabled": $onlineJson,
  "batchEnabled": $batchJson,
  "bzaMenuEnabled": $bzaMenuJson,
  "serviceId": "$ModuleUpper",
  "onlineStandardId": "O${DomainIdCode}QY0001",
  "batchStandardId": "B${DomainIdCode}TS0001"
}
"@

$executionCatalogManifest = @"
[
  {
    "standardExecutionId": "O${DomainIdCode}QY0001",
    "executionType": "ONLINE",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$BasePackage.controller.${ModuleName}Controller",
    "enabled": $onlineJson
  },
  {
    "standardExecutionId": "B${DomainIdCode}TS0001",
    "executionType": "BATCH",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$BasePackage.batch.${ModuleName}BatchConfig",
    "enabled": $batchJson
  }
]
"@

$ownershipManifest = @"
{
  "moduleCode": "$ModuleUpper",
  "ownerDomain": "$DomainIdCode",
  "ownedPackages": ["$BasePackage"],
  "ownedTablePrefixes": ["${TablePrefix}_"],
  "forbiddenDependencies": ["other-domain-repository", "other-domain-mapper"],
  "crossDomainContract": "PFW Service Call Engine 또는 CMN Facade Contract"
}
"@

$smokeSql = @"
-- ${ModuleName} smoke check 후보입니다.
SELECT '${ModuleUpper}_SAMPLE_TABLE' AS check_id, COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = '${TablePrefix}_sample';
"@

$profileApplicationFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $profileWasId = "${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001"
    $profileDataSourceMode = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:jndi}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}"
    }
    $profileDataSourceUrl = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_URL}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_URL:jdbc:mariadb://localhost:3306/${module}DB}"
    }
    $profileDataSourceUsername = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}"
    }
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
  datasource:
    mode: $profileDataSourceMode
    url: $profileDataSourceUrl
    username: $profileDataSourceUsername
    password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD}
    jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:java:comp/env/jdbc/cpf$($ModuleUpper)DataSource}
"@
}

$deployEnvFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $deployDataSourceMode = if ($profileName -eq "prod") { "jndi" } else { "url" }
    $deployDataSourceUrl = if ($profileName -eq "prod") { "__SET_BY_SECRET_PROVIDER__" } else { "jdbc:mariadb://localhost:3306/${module}DB" }
    $deployEnvFiles["deploy/env/${profileName}-${module}.env"] = @"
SPRING_PROFILES_ACTIVE=$profileName
${ModuleUpper}_MODULE_ID=$ModuleUpper
${ModuleUpper}_INSTANCE_ID=${ModuleUpper}-${profileName}-01
${ModuleUpper}_WAS_ID=${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001
${ModuleUpper}_SERVER_PORT=$Port
CPF_LOG_ROOT=C:/cpf/runtime/logs
${ModuleUpper}_DATASOURCE_MODE=$deployDataSourceMode
${ModuleUpper}_DATASOURCE_URL=$deployDataSourceUrl
${ModuleUpper}_DATASOURCE_USERNAME=cpf_${module}_app
${ModuleUpper}_DATASOURCE_PASSWORD=__SET_BY_SECRET_PROVIDER__
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
      "healthUrl": "http://localhost:$Port/actuator/health",
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
    "manifest/domain-manifest.json" = $domainManifest
    "manifest/ownership.json" = $ownershipManifest
    "manifest/standard-execution-catalog.json" = $executionCatalogManifest
    "src/main/resources/application.yml" = $applicationYml
    "src/main/resources/application-${module}.yml" = $applicationModuleYml
    "src/main/resources/mybatis/mapper/${module}/${ModuleName}Mapper.xml" = $mapperXml
    "src/main/java/$packagePath/${ModuleName}Application.java" = $applicationJava
    "src/main/java/$packagePath/config/${ModuleName}DataSourceConfig.java" = $dataSourceConfig
    "src/main/java/$packagePath/config/${ModuleName}MyBatisConfig.java" = $myBatisConfig
    "src/main/java/$packagePath/facade/${ModuleName}Facade.java" = $facade
    "src/main/java/$packagePath/port/${ModuleName}QueryPort.java" = $queryPortSource
    "src/main/java/$packagePath/adapter/local/Local${ModuleName}QueryAdapter.java" = $localAdapter
    "src/main/java/$packagePath/adapter/remote/Remote${ModuleName}QueryProxy.java" = $remoteProxy
    "src/main/java/$packagePath/service/${ModuleName}Service.java" = $service
    "src/main/java/$packagePath/repository/${ModuleName}Repository.java" = $repository
    "src/main/java/$packagePath/dto/${ModuleName}SearchRequest.java" = $dto
    "src/main/java/$packagePath/validation/${ModuleName}SearchValidator.java" = $validator
    "src/test/java/$packagePath/service/${ModuleName}ServiceTest.java" = $serviceTest
    "smoke/smoke-${module}.ps1" = $smokeScript
    "sql/Vxx__${module}_domain.sql" = $sql
}

if ($OnlineEnabled) {
    $files["src/main/java/$packagePath/controller/${ModuleName}Controller.java"] = $controller
}
if ($BatchEnabled) {
    $files["src/main/java/$packagePath/batch/${ModuleName}BatchConfig.java"] = $batchConfig
    $files["src/main/java/$packagePath/config/${ModuleName}BatchRepositoryConfig.java"] = $batchRepositoryConfig
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
    if ($BzaMenuEnabled) {
        $patchFiles["patch-candidates/sql/70_bza_menu_seed.${module}.candidate.sql"] = $bzaSeed
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

if ($Apply) {
    # Apply 모드는 생성 모듈을 저장소에 유지하고 settings 연결까지 원자적으로 완료합니다.
    # 정본 SQL 합본은 version 충돌 검토가 필요하므로 생성된 patch candidate를 별도 동기화 단계에서 반영합니다.
    $settingsText = [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
    if ($settingsText -notmatch "(?m)^include '$([regex]::Escape($module))'$" -and
            $settingsText -notmatch "(?m)^include .*'$([regex]::Escape($module))'") {
        $settingsText = $settingsText.TrimEnd() + "`r`n`r`ninclude '$module'`r`nproject(':$module').projectDir = file('$module')`r`n"
        [System.IO.File]::WriteAllText($settingsPath, $settingsText, $Utf8NoBom)
        $plan.patchFiles += 'settings.gradle'
    }
}

$resultPath = Join-Path $OutputDir "create-domain-result.json"
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
