package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.database.CpfJdbcOperations;
import com.cpf.core.api.error.CpfSystemException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** Internal Spring JDBC adapter for the provider-neutral CPF data operation contract. */
final class CpfSpringJdbcOperations implements CpfJdbcOperations {
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    CpfSpringJdbcOperations(
            NamedParameterJdbcTemplate jdbc,
            TransactionTemplate transactions) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    @Override
    public <T> List<T> queryList(
            String sql,
            Map<String, ?> parameters,
            Class<T> resultType) {
        try {
            return List.copyOf(jdbc.query(
                    requireSql(sql),
                    safe(parameters),
                    mapper(resultType)));
        } catch (RuntimeException exception) {
            throw failure("queryList", exception);
        }
    }

    @Override
    public <T> T queryOne(
            String sql,
            Map<String, ?> parameters,
            Class<T> resultType) {
        List<T> rows = queryList(sql, parameters, resultType);
        if (rows.size() > 1) {
            throw failure(
                    "queryOne",
                    new IllegalStateException(
                            "CPF JDBC query expected at most one row but returned " + rows.size()));
        }
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public int update(String sql, Map<String, ?> parameters) {
        try {
            return jdbc.update(requireSql(sql), safe(parameters));
        } catch (RuntimeException exception) {
            throw failure("update", exception);
        }
    }

    @Override
    public <T> T inRollbackOnlyTransaction(Function<CpfJdbcOperations, T> callback) {
        Objects.requireNonNull(callback, "callback");
        return transactions.execute(status -> {
            T result = callback.apply(this);
            status.setRollbackOnly();
            return result;
        });
    }

    private static String requireSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("CPF JDBC SQL is required");
        }
        return sql;
    }

    private static Map<String, ?> safe(Map<String, ?> parameters) {
        return parameters == null ? Map.of() : parameters;
    }

    private static <T> RowMapper<T> mapper(Class<T> resultType) {
        Objects.requireNonNull(resultType, "resultType");
        if (BeanUtils.isSimpleValueType(resultType)) {
            return SingleColumnRowMapper.newInstance(resultType);
        }
        return DataClassRowMapper.newInstance(resultType);
    }

    private static CpfSystemException failure(String operation, RuntimeException cause) {
        if (cause instanceof CpfSystemException cpfException) {
            return cpfException;
        }
        return new CpfSystemException("CPF JDBC operation failed: " + operation, cause);
    }
}
