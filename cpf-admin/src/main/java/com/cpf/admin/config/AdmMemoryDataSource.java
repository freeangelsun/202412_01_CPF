package com.cpf.admin.config;

import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 명시적 ADM MEMORY 데모 모드에서만 사용하는 연결 불가 DataSource입니다.
 *
 * <p>JdbcTemplate bean 계약은 유지하되 실제 연결 요청은 즉시 실패시켜
 * Service의 명시적 MEMORY fallback 경계에서만 처리되게 합니다.</p>
 */
final class AdmMemoryDataSource extends AbstractDataSource {
    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException("ADM MEMORY mode에는 DB connection이 없습니다.");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }
}
