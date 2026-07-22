package cpf.adm.opr.service;

import cpf.adm.config.AdmSecurityProperties;
import cpf.adm.opr.dto.AdmOperator;
import cpf.cmn.sec.crypto.CmnCryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ADM 강제 비밀번호 변경 세션과 운영자 단위 세션 폐기를 검증합니다.
 */
class AdmSessionServiceTest {

    @Test
    void issuedSessionKeepsPasswordChangeRestrictionAndCanBeRevokedByOperator() {
        AdmSecurityProperties properties = new AdmSecurityProperties();
        AdmSessionService service = new AdmSessionService(
                properties,
                new OfflineJdbcTemplate(),
                new CmnCryptoService());
        AdmOperator operator = new AdmOperator(
                "admin",
                "CPF 관리자",
                List.of("ADM_ADMIN"),
                false,
                false,
                true,
                null,
                null);

        var login = service.issue(operator, List.of());
        var issuedSession = service.findValidSession(login.accessToken()).orElseThrow();

        assertThat(issuedSession.passwordChangeRequired()).isTrue();
        assertThat(service.revokeOperatorSessions("admin")).isEqualTo(1);
        assertThat(service.findValidSession(login.accessToken())).isEmpty();
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
