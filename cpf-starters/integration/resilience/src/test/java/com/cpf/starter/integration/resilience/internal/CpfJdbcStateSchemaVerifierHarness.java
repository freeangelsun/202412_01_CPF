package com.cpf.starter.integration.resilience.internal;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public final class CpfJdbcStateSchemaVerifierHarness {
    private CpfJdbcStateSchemaVerifierHarness() {}

    public static void main(String[] args) {
        RecordingJdbc valid = new RecordingJdbc(258L);
        CpfJdbcStateSchemaVerifier.verify(valid);
        require(valid.sql.size() == 4, "schema verification must inspect shard and three tables");
        require(valid.sql.stream().anyMatch(sql -> sql.contains("cpf_operation_state")),
                "operation state table must be verified");

        try {
            CpfJdbcStateSchemaVerifier.verify(new RecordingJdbc(257L));
            throw new AssertionError("missing shard row must fail startup");
        } catch (IllegalStateException expected) {
            require(expected.getMessage().contains("exactly 258"),
                    "shard failure must describe the invariant without raw SQL errors");
        }

        RecordingJdbc missingTable = new RecordingJdbc(258L);
        missingTable.failAfterFirst = true;
        try {
            CpfJdbcStateSchemaVerifier.verify(missingTable);
            throw new AssertionError("missing state table must fail startup");
        } catch (IllegalStateException expected) {
            require("CPF JDBC state schema verification failed".equals(expected.getMessage()),
                    "provider errors must be wrapped in a stable startup error");
        }
        System.out.println("CPF_JDBC_STATE_SCHEMA_VERIFIER_HARNESS_PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class RecordingJdbc extends JdbcTemplate {
        private final long shards;
        private final List<String> sql = new ArrayList<>();
        private boolean failAfterFirst;

        private RecordingJdbc(long shards) {
            this.shards = shards;
        }

        @Override
        public <T> T query(String statement, ResultSetExtractor<T> extractor, Object... args) {
            sql.add(statement);
            if (failAfterFirst && sql.size() > 1) {
                throw new IllegalArgumentException("database-specific raw failure");
            }
            long value = sql.size() == 1 ? shards : 0L;
            ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[] {ResultSet.class},
                    (proxy, method, methodArgs) -> switch (method.getName()) {
                        case "next" -> true;
                        case "getLong" -> value;
                        case "close" -> null;
                        case "isClosed" -> false;
                        case "unwrap" -> null;
                        case "isWrapperFor" -> false;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
            try {
                return extractor.extractData(resultSet);
            } catch (java.sql.SQLException failure) {
                throw new IllegalStateException(failure);
            }
        }
    }
}
