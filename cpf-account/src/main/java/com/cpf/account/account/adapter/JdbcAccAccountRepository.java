package com.cpf.account.account.adapter;

import com.cpf.account.account.dto.AccAccountCreateRequest;
import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.dto.AccAccountSearchCriteria;
import com.cpf.account.account.dto.AccAccountUpdateRequest;
import com.cpf.account.account.port.AccAccountRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** MariaDB의 ACC 소유 테이블에만 접근하는 계정 저장소 adapter입니다. */
@Repository
public class JdbcAccAccountRepository implements AccAccountRepository {
    private static final String SELECT_COLUMNS = """
            account_id, account_no, account_name, email, status_code, row_version,
            created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccAccountRepository(@Qualifier("accJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long create(AccAccountCreateRequest request, String actor) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO acc_account (
                        account_no, account_name, email, status_code, row_version,
                        deleted_yn, created_by, updated_by
                    ) VALUES (?, ?, ?, 'ACTIVE', 0, 'N', ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.accountNo().trim());
            statement.setString(2, request.accountName().trim());
            statement.setString(3, trimToNull(request.email()));
            statement.setString(4, actor);
            statement.setString(5, actor);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("ACC 계정 등록 키를 확인할 수 없습니다.");
        }
        return key.longValue();
    }

    @Override
    public Optional<AccAccountResponse> find(long accountId) {
        List<AccAccountResponse> rows = jdbcTemplate.query("""
                SELECT %s
                FROM acc_account
                WHERE account_id = ? AND deleted_yn = 'N'
                """.formatted(SELECT_COLUMNS), (rs, rowNum) -> map(rs), accountId);
        return rows.stream().findFirst();
    }

    @Override
    public List<AccAccountResponse> search(AccAccountSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT ").append(SELECT_COLUMNS)
                .append(" FROM acc_account WHERE deleted_yn = 'N'");
        List<Object> arguments = new ArrayList<>();
        appendLike(sql, arguments, "account_no", criteria.accountNo());
        appendLike(sql, arguments, "account_name", criteria.accountName());
        if (criteria.statusCode() != null) {
            sql.append(" AND status_code = ?");
            arguments.add(criteria.statusCode());
        }
        if (criteria.cursorId() != null) {
            sql.append(" AND account_id ")
                    .append("ASC".equals(criteria.sortDirection()) ? ">" : "<")
                    .append(" ?");
            arguments.add(criteria.cursorId());
        }
        sql.append(" ORDER BY ").append(criteria.sortColumn()).append(' ').append(criteria.sortDirection());
        sql.append(" LIMIT ?");
        arguments.add(criteria.size());
        if (criteria.cursorId() == null) {
            sql.append(" OFFSET ?");
            arguments.add(criteria.offset());
        }
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> map(rs), arguments.toArray());
    }

    @Override
    public boolean update(long accountId, AccAccountUpdateRequest request, String actor) {
        return jdbcTemplate.update("""
                UPDATE acc_account
                SET account_name = ?, email = ?, status_code = ?, row_version = row_version + 1,
                    updated_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE account_id = ? AND row_version = ? AND deleted_yn = 'N'
                """, request.accountName().trim(), trimToNull(request.email()),
                request.statusCode().trim().toUpperCase(), actor, accountId, request.version()) == 1;
    }

    @Override
    public boolean logicalDelete(long accountId, long version, String actor) {
        return jdbcTemplate.update("""
                UPDATE acc_account
                SET deleted_yn = 'Y', status_code = 'DELETED', row_version = row_version + 1,
                    updated_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE account_id = ? AND row_version = ? AND deleted_yn = 'N'
                """, actor, accountId, version) == 1;
    }

    @Override
    public void recordChange(long accountId, String actionCode, Object beforeValue, Object afterValue,
                             String actor, String auditReason) {
        jdbcTemplate.update("""
                INSERT INTO acc_account_change_log (
                    account_id, action_code, before_value, after_value, audit_reason,
                    created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, accountId, actionCode, stringValue(beforeValue), stringValue(afterValue),
                auditReason, actor, actor);
    }

    private AccAccountResponse map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new AccAccountResponse(
                resultSet.getLong("account_id"),
                resultSet.getString("account_no"),
                resultSet.getString("account_name"),
                maskEmail(resultSet.getString("email")),
                resultSet.getString("status_code"),
                resultSet.getLong("row_version"),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")));
    }

    private void appendLike(StringBuilder sql, List<Object> arguments, String column, String value) {
        if (value != null) {
            sql.append(" AND ").append(column).append(" LIKE CONCAT('%', ?, '%')");
            arguments.add(value);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        return email.substring(0, Math.min(2, at)) + "***" + email.substring(at);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime localDateTime(java.sql.Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
