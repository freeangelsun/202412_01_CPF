package com.cpf.admin.opr.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ADM JDBC query primitives that intentionally avoid database-vendor pagination and metadata SQL.
 */
final class AdmJdbcQueries {
    private static final String[] TABLE_TYPES = {"TABLE"};

    private AdmJdbcQueries() {
    }

    static List<Map<String, Object>> queryForList(
            JdbcTemplate jdbcTemplate,
            String sql,
            List<?> parameters,
            int maxRows) {
        return query(jdbcTemplate, sql, parameters, maxRows, new ColumnMapRowMapper());
    }

    static <T> List<T> query(
            JdbcTemplate jdbcTemplate,
            String sql,
            List<?> parameters,
            int maxRows,
            RowMapper<T> rowMapper) {
        int boundedMaxRows = Math.max(1, maxRows);
        List<?> safeParameters = parameters == null ? List.of() : parameters;
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int index = 0; index < safeParameters.size(); index++) {
                statement.setObject(index + 1, safeParameters.get(index));
            }
            statement.setMaxRows(boundedMaxRows);
            return statement;
        }, rowMapper);
    }

    static boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        String normalizedTableName = requireIdentifier(tableName);
        try {
            Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                    tableExists(
                            connection.getMetaData(),
                            connection.getCatalog(),
                            connection.getSchema(),
                            normalizedTableName));
            return Boolean.TRUE.equals(exists);
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private static boolean tableExists(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String tableName) throws SQLException {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(tableName);
        candidates.add(tableName.toUpperCase(Locale.ROOT));
        candidates.add(tableName.toLowerCase(Locale.ROOT));
        for (String candidate : candidates) {
            try (ResultSet tables = metadata.getTables(catalog, schema, candidate, TABLE_TYPES)) {
                while (tables.next()) {
                    String discovered = tables.getString("TABLE_NAME");
                    if (tableName.equalsIgnoreCase(discovered)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String requireIdentifier(String value) {
        if (value == null || !value.matches("[A-Za-z][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("tableName must be a simple SQL identifier");
        }
        return value;
    }
}
