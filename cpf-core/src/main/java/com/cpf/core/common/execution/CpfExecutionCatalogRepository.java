package com.cpf.core.common.execution;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CPF DB를 우선 사용하고 DB가 없는 로컬 환경에서는 현재 프로세스 catalog를 제공하는 저장소입니다.
 */
public final class CpfExecutionCatalogRepository implements CpfExecutionCatalogPort {
    private static final Logger log = LoggerFactory.getLogger(CpfExecutionCatalogRepository.class);

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final boolean dbAccessEnabled;
    private final CpfVendorSqlCatalog sql;
    private final ConcurrentMap<String, CpfExecutionDefinition> localCatalog = new ConcurrentHashMap<>();

    public CpfExecutionCatalogRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this(jdbcTemplateProvider, true, new StandardEnvironment());
    }

    /**
     * DB 없는 단위/컨텍스트 테스트에서는 메모리 catalog만 사용하도록 DB 접근을 명시적으로 끌 수 있습니다.
     * 운영 기본 생성자는 DB 접근을 활성화합니다.
     */
    public CpfExecutionCatalogRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            boolean dbAccessEnabled) {
        this(jdbcTemplateProvider, dbAccessEnabled, new StandardEnvironment());
    }

    public CpfExecutionCatalogRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            boolean dbAccessEnabled,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dbAccessEnabled = dbAccessEnabled;
        this.sql = CpfVendorSqlCatalog.create(environment, "cpf");
    }

    @Override
    public void upsertAll(Collection<CpfExecutionDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        definitions.forEach(definition -> localCatalog.put(definition.standardExecutionId(), definition));
        JdbcTemplate jdbcTemplate = dbAccessEnabled ? jdbcTemplateProvider.getIfAvailable() : null;
        if (jdbcTemplate == null) {
            return;
        }
        try {
            for (CpfExecutionDefinition definition : definitions) {
                jdbcTemplate.update(sql.required("execution-catalog-upsert"),
                        definition.standardExecutionId(),
                        definition.executionName(),
                        definition.executionType().name(),
                        definition.ownerDomain(),
                        definition.sourceModule(),
                        definition.sourceClass(),
                        definition.sourceMethod(),
                        definition.httpMethod(),
                        definition.endpoint(),
                        definition.operationId(),
                        definition.description(),
                        definition.requiredPermission(),
                        definition.auditReasonRequired() ? "Y" : "N",
                        definition.visibility(),
                        definition.directAllowed() ? "Y" : "N",
                        definition.gatewayAllowed() ? "Y" : "N",
                        definition.sourceVersion(),
                        Timestamp.from(definition.discoveredAt()));
            }
        } catch (DataAccessException ex) {
            // DB가 아직 설치되지 않은 로컬 기동에서도 서비스는 계속하되 운영자가 원인을 확인할 수 있게 경고를 남깁니다.
            log.warn("CPF 표준 실행 카탈로그 DB 저장에 실패해 메모리 카탈로그를 유지합니다. count={}",
                    definitions.size(), ex);
        }
    }

    @Override
    public List<CpfExecutionDefinition> findAll() {
        JdbcTemplate jdbcTemplate = dbAccessEnabled ? jdbcTemplateProvider.getIfAvailable() : null;
        if (jdbcTemplate != null) {
            try {
                return jdbcTemplate.query(
                        sql.required("execution-catalog-find-all"),
                        (rs, rowNum) -> new CpfExecutionDefinition(
                        rs.getString("standard_execution_id"),
                        rs.getString("execution_name"),
                        CpfExecutionType.valueOf(rs.getString("execution_type")),
                        rs.getString("owner_domain"),
                        rs.getString("source_module"),
                        rs.getString("source_class"),
                        rs.getString("source_method"),
                        rs.getString("http_method"),
                        rs.getString("endpoint"),
                        rs.getString("operation_id"),
                        rs.getString("description"),
                        rs.getString("required_permission"),
                        "Y".equalsIgnoreCase(rs.getString("audit_reason_required_yn")),
                        rs.getString("visibility"),
                        "Y".equalsIgnoreCase(rs.getString("direct_allowed_yn")),
                        "Y".equalsIgnoreCase(rs.getString("gateway_allowed_yn")),
                        rs.getString("source_version"),
                        rs.getTimestamp("last_discovered_at").toInstant()));
            } catch (DataAccessException ex) {
                // 설치 전 로컬 기동에서는 현재 프로세스에서 발견한 정보로 조회를 계속합니다.
                log.warn("CPF 표준 실행 카탈로그 DB 조회에 실패해 메모리 카탈로그를 사용합니다.", ex);
            }
        }
        return localCatalog.values().stream()
                .sorted(Comparator.comparing(CpfExecutionDefinition::standardExecutionId))
                .toList();
    }

    @Override
    public Optional<CpfExecutionDefinition> findById(String standardExecutionId) {
        return findAll().stream()
                .filter(item -> item.standardExecutionId().equals(standardExecutionId))
                .findFirst();
    }

    @Override
    public Optional<CpfExecutionDefinition> resolve(String executionId) {
        if (CpfStandardExecutionId.isValid(executionId)) {
            return findById(executionId);
        }
        if (!CpfStandardExecutionId.isLegacy(executionId)) {
            return Optional.empty();
        }
        JdbcTemplate jdbcTemplate = dbAccessEnabled ? jdbcTemplateProvider.getIfAvailable() : null;
        if (jdbcTemplate == null) {
            return Optional.empty();
        }
        try {
            List<String> currentIds = jdbcTemplate.queryForList(
                    sql.required("execution-catalog-resolve-alias"),
                    String.class, executionId);
            return currentIds.isEmpty() ? Optional.empty() : findById(currentIds.getFirst());
        } catch (DataAccessException ex) {
            log.warn("레거시 실행 ID alias 조회에 실패했습니다. legacyExecutionId={}", executionId, ex);
            return Optional.empty();
        }
    }
}
