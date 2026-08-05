package com.cpf.common.sample;

import com.cpf.common.sql.CmnSqlResourceLoader;
import java.util.Locale;

/** Official CPF database-vendor SQL resource routing for the CMN golden sample. */
enum CmnSampleSqlDialect {
    MARIADB("sample/offset-mariadb.sql", "sample/cursor-mariadb.sql"),
    POSTGRESQL("sample/offset-postgresql.sql", "sample/cursor-postgresql.sql"),
    ORACLE("sample/offset-oracle.sql", "sample/cursor-oracle.sql");

    private final String offsetQueryId;
    private final String cursorQueryId;

    CmnSampleSqlDialect(String offsetQueryId, String cursorQueryId) {
        this.offsetQueryId = offsetQueryId;
        this.cursorQueryId = cursorQueryId;
    }

    static CmnSampleSqlDialect fromDatabaseProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalStateException("Database product name is required for CMN Sample SQL routing.");
        }
        String normalized = productName.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("mariadb")) return MARIADB;
        if (normalized.contains("postgresql")) return POSTGRESQL;
        if (normalized.contains("oracle")) return ORACLE;
        throw new IllegalStateException("Unsupported CMN Sample database vendor: " + productName);
    }

    String offsetPageSql() {
        return CmnSqlResourceLoader.load(offsetQueryId);
    }

    Object[] offsetPageParameters(String statusCode, String keyword, int offset, int limit) {
        if (this == ORACLE) {
            return new Object[] {statusCode, statusCode, keyword, keyword, offset, limit};
        }
        return new Object[] {statusCode, statusCode, keyword, keyword, limit, offset};
    }

    String cursorPageSql() {
        return CmnSqlResourceLoader.load(cursorQueryId);
    }
}
