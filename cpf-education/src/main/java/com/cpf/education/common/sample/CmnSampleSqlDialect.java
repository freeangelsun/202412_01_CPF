package com.cpf.education.common.sample;
import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import org.springframework.jdbc.core.JdbcTemplate;

/** Official CPF database-vendor SQL resource routing for the CMN golden sample. */
enum CmnSampleSqlDialect {
    MARIADB("sample/offset-mariadb.sql", "sample/cursor-mariadb.sql", false),
    POSTGRESQL("sample/offset-postgresql.sql", "sample/cursor-postgresql.sql", false),
    ORACLE("sample/offset-oracle.sql", "sample/cursor-oracle.sql", true);

    private final String offsetQueryId;
    private final String cursorQueryId;
    private final boolean offsetBeforeLimit;

    CmnSampleSqlDialect(String offsetQueryId, String cursorQueryId, boolean offsetBeforeLimit) {
        this.offsetQueryId = offsetQueryId;
        this.cursorQueryId = cursorQueryId;
        this.offsetBeforeLimit = offsetBeforeLimit;
    }


    /**
     * 샘플 인프라 경계에서 JDBC metadata를 공식 CPF DB Vendor 계약으로 변환합니다.
     * 업무 Service는 Vendor 이름이나 DatabaseMetaData를 직접 분기하지 않습니다.
     */
    static CmnSampleSqlDialect detect(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate.getDataSource() == null) {
            throw new IllegalStateException("CMN Sample DataSource is not configured.");
        }
        try (java.sql.Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            return fromDatabaseProductName(connection.getMetaData().getDatabaseProductName());
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException("CMN Sample database dialect detection failed.", ex);
        }
    }

    static CmnSampleSqlDialect fromDatabaseProductName(String productName) {
        return valueOf(CpfDatabaseVendor.fromDatabaseProductName(productName).name());
    }

    String offsetPageSql() {
        return CmnEducationSqlResourceLoader.load(offsetQueryId);
    }

    Object[] offsetPageParameters(String statusCode, String keyword, int offset, int limit) {
        return offsetBeforeLimit
                ? new Object[] {statusCode, statusCode, keyword, keyword, offset, limit}
                : new Object[] {statusCode, statusCode, keyword, keyword, limit, offset};
    }

    String cursorPageSql() {
        return CmnEducationSqlResourceLoader.load(cursorQueryId);
    }
}
