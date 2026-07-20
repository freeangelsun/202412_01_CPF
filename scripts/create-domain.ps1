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
    [string] $BzaMenu = "N",
    [ValidateSet("Y", "N")]
    [string] $ProductionProfile = "N",
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
$ProductionProfileEnabled = $ProductionProfile -eq "Y"
if ([string]::IsNullOrWhiteSpace($ModuleName)) {
    $ModuleName = $module
}
if ([string]::IsNullOrWhiteSpace($BasePackage)) {
    $BasePackage = "cpf.$module"
}
$ModuleClassName = $module.Substring(0, 1).ToUpperInvariant() + $module.Substring(1).ToLowerInvariant()
$FeaturePackage = "$BasePackage.reference"
$FeatureClassPrefix = "${ModuleName}Reference"
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
$featurePackagePath = $FeaturePackage.Replace('.', '/')
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

import cpf.pfw.common.base.CpfBaseController;

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

import cpf.pfw.common.base.CpfBaseService;

/**
 * $ModuleUpper Service가 공유하는 주제영역 확장점입니다.
 */
public abstract class ${ModuleClassName}BaseService extends CpfBaseService {
}
"@

$moduleFacadeContract = @"
package $BasePackage.common.contract;

import cpf.pfw.common.base.CpfApplicationFacade;

/** $ModuleUpper Application Facade의 주제영역 계약입니다. */
public interface ${ModuleClassName}ApplicationFacade extends CpfApplicationFacade {
}
"@

$moduleRepositoryContract = @"
package $BasePackage.common.contract;

import cpf.pfw.common.base.CpfRepositoryPort;

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

import cpf.pfw.common.base.CpfRequest;

/** $ModuleUpper 요청 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Request extends CpfRequest {
}
"@

$moduleResponseContract = @"
package $BasePackage.common.contract;

import cpf.pfw.common.base.CpfResponse;

/** $ModuleUpper 응답 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Response extends CpfResponse {
}
"@

$controller = @"
package $FeaturePackage.controller;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.facade.${FeatureClassPrefix}Facade;
import $FeaturePackage.validation.${FeatureClassPrefix}SearchValidator;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * ${ModuleName} 조회 API를 제공합니다.
 *
 * <p>업무 Controller는 요청 검증과 Swagger 계약을 담당하고,
 * 실제 업무 처리는 Facade와 Service에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/$module/reference")
@Tag(name = "$ModuleUpper 생성 참조", description = "생성기 기본 골격과 대표 업무 기능의 경계를 확인하는 참조 API")
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
    @Operation(
            operationId = "search${ModuleName}Reference",
            summary = "${ModuleName} 생성 참조 목록 조회",
            description = "검색어, 페이징, 정렬 whitelist를 적용해 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> search(${FeatureClassPrefix}SearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }
}
"@

$facade = @"
package $FeaturePackage.facade;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'ApplicationFacade;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.service.${FeatureClassPrefix}Service;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Controller와 업무 서비스를 분리하는 진입 Facade입니다.
 *
 * <p>여러 서비스 조합, 외부 호출, 거래 단위 조정이 필요하면 Facade에서 처리합니다.</p>
 */
@Component
public class ${FeatureClassPrefix}Facade implements ${ModuleClassName}ApplicationFacade {
    private final ${FeatureClassPrefix}Service service;

    public ${FeatureClassPrefix}Facade(${FeatureClassPrefix}Service service) {
        this.service = Objects.requireNonNull(service, "service는 필수입니다.");
    }

    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
        return service.search(request);
    }
}
"@

$queryPortSource = @"
package $FeaturePackage.port;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'RepositoryPort;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;

import java.util.Map;

/**
 * ${ModuleName} 조회 구현을 local 또는 remote adapter로 교체하기 위한 업무 포트입니다.
 */
public interface ${FeatureClassPrefix}QueryPort extends ${ModuleClassName}RepositoryPort<Map<String, Object>, String> {
    Map<String, Object> search(${FeatureClassPrefix}SearchRequest request);
}
"@

$localAdapter = @"
package $FeaturePackage.adapter.local;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import $FeaturePackage.repository.${FeatureClassPrefix}Repository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 같은 주제영역 DB를 사용하는 기본 local adapter입니다.
 */
@Component
public class Local${FeatureClassPrefix}QueryAdapter implements ${FeatureClassPrefix}QueryPort {
    private final ${FeatureClassPrefix}Repository repository;

    public Local${FeatureClassPrefix}QueryAdapter(${FeatureClassPrefix}Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository는 필수입니다.");
    }

    @Override
    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
        return repository.search(request);
    }
}
"@

$remoteProxy = @"
package $FeaturePackage.adapter.remote;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import cpf.pfw.common.http.CpfWebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;
import java.util.Objects;

/**
 * 분리 배포된 ${ModuleName} 서비스에 PFW 표준 서비스 호출 경계로 접근하는 remote proxy입니다.
 *
 * <p>프로젝트 설정에서 remote 모드를 선택할 때만 Bean으로 등록합니다.</p>
 */
public class Remote${FeatureClassPrefix}QueryProxy implements ${FeatureClassPrefix}QueryPort {
    private final CpfWebClient webClient;

    public Remote${FeatureClassPrefix}QueryProxy(CpfWebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient는 필수입니다.");
    }

    @Override
    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
        return webClient.get(
                "$ModuleUpper",
                uriBuilder -> {
                    uriBuilder.path("/api/v1/$module/reference")
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
package $FeaturePackage.service;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseService;'))
import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/**
 * ${ModuleName} 조회 업무를 처리합니다.
 */
@Service
public class ${FeatureClassPrefix}Service extends ${ModuleClassName}BaseService {
    private final ${FeatureClassPrefix}QueryPort queryPort;

    public ${FeatureClassPrefix}Service(${FeatureClassPrefix}QueryPort queryPort) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort는 필수입니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
        return queryPort.search(request.normalized());
    }
}
"@

$repository = @"
package $FeaturePackage.repository;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;

/**
 * MyBatis mapper 호출을 캡슐화하는 업무 저장소입니다.
 */
@Repository
public class ${FeatureClassPrefix}Repository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public ${FeatureClassPrefix}Repository(
            @Qualifier("${module}SqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate은 필수입니다.");
    }

    public Map<String, Object> search(${FeatureClassPrefix}SearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList(
                        "${FeaturePackage}.mapper.${FeatureClassPrefix}Mapper.search", request),
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

}
"@

$dto = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Request;'))
import cpf.pfw.common.base.CpfQuery;
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
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "${TablePrefix}_id");

    public ${FeatureClassPrefix}SearchRequest normalized() {
        String normalizedSortBy = sortBy != null && SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new ${FeatureClassPrefix}SearchRequest(
                keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }

    public int offset() {
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return normalizedPage * normalizedSize;
    }
}
"@

$validator = @"
package $FeaturePackage.validation;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import cpf.pfw.common.exception.CpfValidationException;
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

$mapperXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="$FeaturePackage.mapper.${FeatureClassPrefix}Mapper">
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
package $FeaturePackage.batch;

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
public class ${FeatureClassPrefix}BatchConfig {

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
    implementation project(':pfw')
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

tasks.withType(AbstractArchiveTask).configureEach {
    archiveBaseName = "cpf-$ModuleCode"
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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .driverClassName(environment.getRequiredProperty("cpf.datasource.driver-class-name"))
                .build();
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
    driver-class-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_DRIVER_CLASS_NAME:org.mariadb.jdbc.Driver}
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
package $FeaturePackage.service;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import $FeaturePackage.port.${FeatureClassPrefix}QueryPort;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ${FeatureClassPrefix}ServiceTest {
    private final AtomicReference<${FeatureClassPrefix}SearchRequest> capturedRequest = new AtomicReference<>();
    private final ${FeatureClassPrefix}QueryPort queryPort = request -> {
        // DB adapter를 우회하지 않고 port 계약을 대역으로 사용해 Service 경계만 검증합니다.
        capturedRequest.set(request);
        return Map.of("items", java.util.List.of(), "criteria", request);
    };
    private final ${FeatureClassPrefix}Service service = new ${FeatureClassPrefix}Service(queryPort);

    @Test
    void searchNormalizesPagingAndSort() {
        Map<String, Object> result = service.search(
                new ${FeatureClassPrefix}SearchRequest("keyword", "unsafe_column", "ASC", -1, 999));

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
    "src/main/resources/mybatis/mapper/${module}/reference/${FeatureClassPrefix}Mapper.xml" = $mapperXml
    "src/main/java/$packagePath/${ModuleClassName}Application.java" = $applicationJava
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseController.java" = $moduleBaseController
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseService.java" = $moduleBaseService
    "src/main/java/$packagePath/common/contract/${ModuleClassName}ApplicationFacade.java" = $moduleFacadeContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}RepositoryPort.java" = $moduleRepositoryContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Request.java" = $moduleRequestContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Response.java" = $moduleResponseContract
    "src/main/java/$packagePath/config/${ModuleName}DataSourceConfig.java" = $dataSourceConfig
    "src/main/java/$packagePath/config/${ModuleName}MyBatisConfig.java" = $myBatisConfig
    "src/main/java/$featurePackagePath/facade/${FeatureClassPrefix}Facade.java" = $facade
    "src/main/java/$featurePackagePath/port/${FeatureClassPrefix}QueryPort.java" = $queryPortSource
    "src/main/java/$featurePackagePath/adapter/local/Local${FeatureClassPrefix}QueryAdapter.java" = $localAdapter
    "src/main/java/$featurePackagePath/adapter/remote/Remote${FeatureClassPrefix}QueryProxy.java" = $remoteProxy
    "src/main/java/$featurePackagePath/service/${FeatureClassPrefix}Service.java" = $service
    "src/main/java/$featurePackagePath/repository/${FeatureClassPrefix}Repository.java" = $repository
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SearchRequest.java" = $dto
    "src/main/java/$featurePackagePath/validation/${FeatureClassPrefix}SearchValidator.java" = $validator
    "src/test/java/$featurePackagePath/service/${FeatureClassPrefix}ServiceTest.java" = $serviceTest
    "smoke/smoke-${module}.ps1" = $smokeScript
    "sql/Vxx__${module}_domain.sql" = $sql
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
    if ($settingsText -notmatch "(?m)^include '$([regex]::Escape($module))'$" -and
            $settingsText -notmatch "(?m)^include .*'$([regex]::Escape($module))'") {
        $settingsText = $settingsText.TrimEnd() + "`r`n`r`ninclude '$module'`r`nproject(':$module').projectDir = file('$module')`r`n"
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
    generatorVersion = "2.0"
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    moduleCode = $ModuleUpper
    moduleDirectory = $module
    outputDirectory = $OutputDir
    createdFiles = $ownedFiles
    modifiedGlobalFiles = @(
        [ordered]@{
            path = "settings.gradle"
            managedLines = @("include '$module'", "project(':$module').projectDir = file('$module')")
        }
    )
    databaseRemovalPolicy = "운영 DB 객체는 자동 삭제하지 않으며 별도 승인 migration으로 처리합니다."
}
$ownershipPath = Join-Path $OutputDir "manifest/generator-ownership.json"
Write-Utf8 -Path $ownershipPath -Content ($generatorOwnership | ConvertTo-Json -Depth 20)
$plan.generatedFiles += $ownershipPath.Substring($Root.Length).TrimStart('\', '/')

$resultPath = if ($Apply) {
    Join-Path $Root "build/reports/create-domain/$module/create-domain-result.json"
} else {
    Join-Path $OutputDir "create-domain-result.json"
}
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
