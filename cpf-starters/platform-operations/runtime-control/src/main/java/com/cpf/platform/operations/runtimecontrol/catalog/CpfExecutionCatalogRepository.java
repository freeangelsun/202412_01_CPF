package com.cpf.platform.operations.runtimecontrol.catalog;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfExecutionCatalogPort;
import com.cpf.foundation.execution.api.CpfExecutionDefinition;
import com.cpf.foundation.execution.api.CpfExecutionType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** DB3-backed canonical standard-execution catalog provider. */
public final class CpfExecutionCatalogRepository implements CpfExecutionCatalogPort {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final CpfVendorSqlCatalog sql;

    public CpfExecutionCatalogRepository(
            JdbcTemplate jdbc,
            TransactionTemplate tx,
            CpfVendorSqlCatalogProvider catalogs) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.tx = Objects.requireNonNull(tx, "tx");
        this.sql = Objects.requireNonNull(catalogs, "catalogs").forModule("cpf");
    }

    @Override
    public void upsertAll(Collection<CpfExecutionDefinition> definitions) {
        List<CpfExecutionDefinition> owned = List.copyOf(
                Objects.requireNonNull(definitions, "definitions"));
        if (owned.isEmpty()) {
            return;
        }
        List<Object[]> parameters = owned.stream().map(this::parameters).toList();
        tx.executeWithoutResult(status -> jdbc.batchUpdate(
                sql.required("execution-catalog-upsert"), parameters));
    }

    @Override
    public List<CpfExecutionDefinition> findAll() {
        return jdbc.query(sql.required("execution-catalog-find-all"), this::map);
    }

    @Override
    public Optional<CpfExecutionDefinition> findById(String standardExecutionId) {
        String id = required(standardExecutionId, "standardExecutionId");
        return jdbc.query(
                sql.required("execution-catalog-find-by-id"), this::map, id).stream().findFirst();
    }

    @Override
    public Optional<CpfExecutionDefinition> resolve(String executionId) {
        String id = required(executionId, "executionId");
        Optional<CpfExecutionDefinition> current = findById(id);
        if (current.isPresent()) {
            return current;
        }
        return jdbc.query(
                        sql.required("execution-catalog-resolve-alias"),
                        (rs, rowNum) -> rs.getString(1).trim(),
                        id)
                .stream()
                .findFirst()
                .flatMap(this::findById);
    }

    private Object[] parameters(CpfExecutionDefinition definition) {
        return new Object[] {
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
                yn(definition.auditReasonRequired()),
                definition.visibility(),
                yn(definition.directAllowed()),
                yn(definition.gatewayAllowed()),
                definition.sourceVersion(),
                Timestamp.from(definition.discoveredAt())
        };
    }

    private CpfExecutionDefinition map(ResultSet rs, int rowNum) throws SQLException {
        Timestamp discoveredAt = rs.getTimestamp("last_discovered_at");
        return new CpfExecutionDefinition(
                rs.getString("standard_execution_id").trim(),
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
                yes(rs.getString("audit_reason_required_yn")),
                rs.getString("visibility"),
                yes(rs.getString("direct_allowed_yn")),
                yes(rs.getString("gateway_allowed_yn")),
                rs.getString("source_version"),
                discoveredAt == null ? null : discoveredAt.toInstant());
    }

    private static String yn(boolean value) {
        return value ? "Y" : "N";
    }

    private static boolean yes(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
