package cpf.pfw.common.execution;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PFW DB를 우선 사용하고 DB가 없는 로컬 환경에서는 현재 프로세스 catalog를 제공하는 저장소입니다.
 */
public final class CpfExecutionCatalogRepository implements CpfExecutionCatalogPort {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ConcurrentMap<String, CpfExecutionDefinition> localCatalog = new ConcurrentHashMap<>();

    public CpfExecutionCatalogRepository(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
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
                        INSERT INTO pfw_standard_execution (
                            standard_execution_id, execution_name, execution_type, owner_domain,
                            source_module, source_class, source_method, endpoint, operation_id,
                            source_version, registration_status, first_registered_at, last_discovered_at,
                            created_by, updated_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTERED', CURRENT_TIMESTAMP, ?, 'PFW_STARTUP', 'PFW_STARTUP')
                        ON DUPLICATE KEY UPDATE
                            execution_name = VALUES(execution_name),
                            execution_type = VALUES(execution_type),
                            owner_domain = VALUES(owner_domain),
                            source_module = VALUES(source_module),
                            source_class = VALUES(source_class),
                            source_method = VALUES(source_method),
                            endpoint = VALUES(endpoint),
                            operation_id = VALUES(operation_id),
                            source_version = VALUES(source_version),
                            registration_status = 'REGISTERED',
                            last_discovered_at = VALUES(last_discovered_at),
                            updated_by = 'PFW_STARTUP',
                            updated_at = CURRENT_TIMESTAMP
                        """,
                        definition.standardExecutionId(),
                        definition.executionName(),
                        definition.executionType().name(),
                        definition.ownerDomain(),
                        definition.sourceModule(),
                        definition.sourceClass(),
                        definition.sourceMethod(),
                        definition.endpoint(),
                        definition.operationId(),
                        definition.sourceVersion(),
                        Timestamp.from(definition.discoveredAt()));
            }
        } catch (DataAccessException ignored) {
            // DB가 아직 설치되지 않은 로컬 기동에서는 첫 연결 실패 후 메모리 카탈로그를 유지합니다.
        }
    }

    @Override
    public List<CpfExecutionDefinition> findAll() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            try {
                return jdbcTemplate.query("""
                        SELECT standard_execution_id, execution_name, execution_type, owner_domain,
                               source_module, source_class, source_method, endpoint, operation_id,
                               source_version, last_discovered_at
                        FROM pfw_standard_execution
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
                        rs.getString("endpoint"),
                        rs.getString("operation_id"),
                        rs.getString("source_version"),
                        rs.getTimestamp("last_discovered_at").toInstant()));
            } catch (DataAccessException ignored) {
                // 설치 전 로컬 기동에서는 현재 프로세스에서 발견한 정보로 조회를 계속합니다.
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
}
