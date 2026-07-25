package com.cpf.member.operation;

import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MbrOwnerAdminOperationsServiceErrorTest {
    @Test
    void missingMemberAndDatabaseFailureAreNotCollapsedIntoSameError() {
        var missing = new MbrOwnerAdminOperationsService(new FailingJdbcTemplate(new EmptyResultDataAccessException(1)));
        assertThrows(CpfNotFoundException.class, () -> missing.query(
                new CpfOwnerAdminQuery("member", "findMemberDetail", "1", Map.of())));

        var databaseDown = new MbrOwnerAdminOperationsService(
                new FailingJdbcTemplate(new DataAccessResourceFailureException("db down")));
        assertThrows(CpfBusinessException.class, () -> databaseDown.query(
                new CpfOwnerAdminQuery("member", "findMemberDetail", "1", Map.of())));
    }

    private static final class FailingJdbcTemplate extends JdbcTemplate {
        private final RuntimeException failure;

        private FailingJdbcTemplate(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public Map<String, Object> queryForMap(String sql, Object... args) {
            throw failure;
        }
    }
}
