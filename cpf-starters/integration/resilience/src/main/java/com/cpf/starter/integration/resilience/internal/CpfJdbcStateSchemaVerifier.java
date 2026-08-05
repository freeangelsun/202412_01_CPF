package com.cpf.starter.integration.resilience.internal;

import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/** Fail-fast startup verification for the shared CPF state schema. */
final class CpfJdbcStateSchemaVerifier {
    private CpfJdbcStateSchemaVerifier() {}

    static void verify(JdbcTemplate jdbc) {
        Objects.requireNonNull(jdbc, "jdbc");
        try {
            long shards = count(jdbc, "SELECT COUNT(*) FROM cpf_state_shard");
            if (shards != JdbcCpfStateStore.REQUIRED_SHARD_ROWS) {
                throw new IllegalStateException(
                        "cpf_state_shard must contain exactly "
                                + JdbcCpfStateStore.REQUIRED_SHARD_ROWS + " rows");
            }
            verifyTable(jdbc, "cpf_operation_state");
            verifyTable(jdbc, "cpf_state_command");
            verifyTable(jdbc, "cpf_state_audit");
        } catch (RuntimeException failure) {
            if (failure instanceof IllegalStateException stateFailure
                    && stateFailure.getMessage() != null
                    && stateFailure.getMessage().startsWith("cpf_state_shard")) {
                throw stateFailure;
            }
            throw new IllegalStateException("CPF JDBC state schema verification failed", failure);
        }
    }

    private static void verifyTable(JdbcTemplate jdbc, String table) {
        count(jdbc, "SELECT COUNT(*) FROM " + table + " WHERE 1 = 0");
    }

    private static long count(JdbcTemplate jdbc, String sql) {
        Long found = jdbc.query(
                sql,
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null);
        if (found == null || found < 0L) {
            throw new IllegalStateException("invalid JDBC state schema count result");
        }
        return found;
    }
}
