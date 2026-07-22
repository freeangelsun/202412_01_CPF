package com.cpf.core.common.execution;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final ConcurrentMap<String, CpfExecutionDefinition> localCatalog = new ConcurrentHashMap<>();

    public CpfExecutionCatalogRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Override
    public void upsertAll(Collection<CpfExecutionDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        definitions.forEach(definition -> localCatalog.put(definition.standardExecutionId(), definition));
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return;
        }
        try {
            for (CpfExecutionDefinition definition : definitions) {
                jdbcTemplate.update("""
                        INSERT INTO cpf_standard_execution (
                            standard_execution_id, execution_name, execution_type, owner_domain,
                            source_module, source_class, source_method, http_method, endpoint, operation_id,
                            description, required_permission, audit_reason_required_yn, visibility,
                            direct_allowed_yn, gateway_allowed_yn, source_version,
                            registration_status, first_registered_at, last_discovered_at,
                            created_by, updated_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTERED', CURRENT_TIMESTAMP, ?, 'CPF_STARTUP', 'CPF_STARTUP')
                        ON DUPLICATE KEY UPDATE
                            execution_name = VALUES(execution_name),
                            execution_type = VALUES(execution_type),
                            owner_domain = VALUES(owner_domain),
                            source_module = VALUES(source_module),
                            source_class = VALUES(source_class),
                            source_method = VALUES(source_method),
                            http_method = VALUES(http_method),
                            endpoint = VALUES(endpoint),
                            operation_id = VALUES(operation_id),
                            description = VALUES(description),
                            required_permission = VALUES(required_permission),
                            audit_reason_required_yn = VALUES(audit_reason_required_yn),
                            visibility = VALUES(visibility),
                            direct_allowed_yn = VALUES(direct_allowed_yn),
                            gateway_allowed_yn = VALUES(gateway_allowed_yn),
                            source_version = VALUES(source_version),
                            registration_status = 'REGISTERED',
                            last_discovered_at = VALUES(last_discovered_at),
                            updated_by = 'CPF_STARTUP',
                            updated_at = CURRENT_TIMESTAMP
                        """,
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
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            try {
                return jdbcTemplate.query("""
                        SELECT standard_execution_id, execution_name, execution_type, owner_domain,
                               source_module, source_class, source_method, http_method, endpoint, operation_id,
                               description, required_permission, audit_reason_required_yn, visibility,
                               direct_allowed_yn, gateway_allowed_yn, source_version, last_discovered_at
                        FROM cpf_standard_execution
                        WHERE registration_status <> 'RETIRED'
                        ORDER BY standard_execution_id
                        """, (rs, rowNum) -> new CpfExecutionDefinition(
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
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return Optional.empty();
        }
        try {
            List<String> currentIds = jdbcTemplate.queryForList("""
                    SELECT standard_execution_id
                    FROM cpf_standard_execution_alias
                    WHERE legacy_execution_id = ?
                    """, String.class, executionId);
            return currentIds.isEmpty() ? Optional.empty() : findById(currentIds.getFirst());
        } catch (DataAccessException ex) {
            log.warn("레거시 실행 ID alias 조회에 실패했습니다. legacyExecutionId={}", executionId, ex);
            return Optional.empty();
        }
    }
}
