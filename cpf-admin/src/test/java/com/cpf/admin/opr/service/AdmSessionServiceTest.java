package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.common.sec.crypto.CmnCryptoService;
import com.cpf.core.api.error.CpfBusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** ADM Session 정본과 fail-closed 동작을 검증합니다. */
class AdmSessionServiceTest {

    @Test
    void explicitMemoryModeWorksOnlyAsLocalTestFallback() {
        AdmSessionService service = service("MEMORY", new OfflineJdbcTemplate());
        AdmOperator operator = new AdmOperator(
                "admin", "CPF 관리자", List.of("ADM_ADMIN"), false, false, true, null, null);

        var login = service.issue(operator, List.of(), List.of());
        var issued = service.findValidSession(login.accessToken()).orElseThrow();

        assertThat(issued.passwordChangeRequired()).isTrue();
        assertThat(service.revokeOperatorSessions("admin")).isEqualTo(1);
        assertThat(service.findValidSession(login.accessToken())).isEmpty();
    }

    @Test
    void databaseModeNeverFallsBackToMemoryWhenSessionStoreIsUnavailable() {
        AdmSessionService service = service("DATABASE", new OfflineJdbcTemplate());

        assertThatThrownBy(() -> service.findValidSession("bearer-token"))
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("Session Store");
    }

    @Test
    void databaseModeDoesNotIssueTokenWhenPersistenceFails() {
        AdmSessionService service = service("DATABASE", new OfflineJdbcTemplate());
        AdmOperator operator = new AdmOperator(
                "admin", "CPF 관리자", List.of("ADM_ADMIN"), false, false, false, null, null);

        assertThatThrownBy(() -> service.issue(operator, List.of(), List.of()))
                .isInstanceOf(CpfBusinessException.class);
    }

    private AdmSessionService service(String mode, JdbcTemplate jdbcTemplate) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.adm.persistence.mode", mode)
                .withProperty("spring.profiles.active", "test");
        environment.setActiveProfiles("test");
        return new AdmSessionService(
                new AdmSecurityProperties(),
                jdbcTemplate,
                new CmnCryptoService(),
                new AdmPersistencePolicy(environment));
    }

    private static final class OfflineJdbcTemplate extends JdbcTemplate {
        @Override
        public int update(String sql, Object... args) {
            throw new DataAccessResourceFailureException("테스트용 DB 미연결");
        }

        @Override
        public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, Object... args) {
            throw new DataAccessResourceFailureException("테스트용 DB 미연결");
        }
    }
}
