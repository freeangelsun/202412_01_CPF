package cpf.adm.opr.service;

import cpf.adm.config.AdmPasswordPolicyProperties;
import cpf.adm.opr.dto.AdmLoginRequest;
import cpf.adm.opr.dto.AdmPasswordChangeRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ADM 본인 비밀번호 변경의 확인, 재사용 금지, fallback 계약을 검증합니다.
 */
class AdmOperatorPasswordChangeTest {
    private static final String OPERATOR_ID = "admin";
    private static final String INITIAL_PASSWORD = "Initial!234Aa";
    private static final String SECOND_PASSWORD = "Changed!234Aa";
    private static final String THIRD_PASSWORD = "Another!234Aa";

    private AdmOperatorService operatorService;

    @BeforeEach
    void setUp() {
        AdmPasswordPolicyProperties properties = new AdmPasswordPolicyProperties();
        properties.setMinLength(10);
        properties.setRequiredCategoryCount(3);
        properties.setHistoryCount(3);
        operatorService = new AdmOperatorService(
                new AdmPasswordPolicyService(properties),
                new OfflineJdbcTemplate());
        assertThat(operatorService.bootstrapOperator(OPERATOR_ID, "CPF 관리자", INITIAL_PASSWORD)).isTrue();
    }

    @Test
    void currentPasswordAndConfirmationAreRequired() {
        assertThatThrownBy(() -> operatorService.changePassword(
                OPERATOR_ID,
                request("Wrong!234Aa", SECOND_PASSWORD, SECOND_PASSWORD)))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("현재 비밀번호");

        assertThatThrownBy(() -> operatorService.changePassword(
                OPERATOR_ID,
                request(INITIAL_PASSWORD, SECOND_PASSWORD, THIRD_PASSWORD)))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("확인값");
    }

    @Test
    void successfulChangeReplacesCredentialAndClearsForceFlag() {
        var changed = operatorService.changePassword(
                OPERATOR_ID,
                request(INITIAL_PASSWORD, SECOND_PASSWORD, SECOND_PASSWORD));

        assertThat(changed.passwordChangeRequired()).isFalse();
        assertThat(operatorService.authenticate(new AdmLoginRequest(OPERATOR_ID, SECOND_PASSWORD)).operatorId())
                .isEqualTo(OPERATOR_ID);
        assertThatThrownBy(() -> operatorService.authenticate(new AdmLoginRequest(OPERATOR_ID, INITIAL_PASSWORD)))
                .isInstanceOf(CpfValidationException.class);
    }

    @Test
    void recentlyUsedPasswordCannotBeReused() {
        operatorService.changePassword(
                OPERATOR_ID,
                request(INITIAL_PASSWORD, SECOND_PASSWORD, SECOND_PASSWORD));
        operatorService.changePassword(
                OPERATOR_ID,
                request(SECOND_PASSWORD, THIRD_PASSWORD, THIRD_PASSWORD));

        assertThatThrownBy(() -> operatorService.changePassword(
                OPERATOR_ID,
                request(THIRD_PASSWORD, INITIAL_PASSWORD, INITIAL_PASSWORD)))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다시 사용할 수 없습니다");
    }

    private AdmPasswordChangeRequest request(String currentPassword, String newPassword, String confirm) {
        return new AdmPasswordChangeRequest(
                currentPassword,
                newPassword,
                confirm,
                OPERATOR_ID,
                "운영자 본인 비밀번호 변경");
    }

    /**
     * 실제 DB가 준비되지 않은 local 기동 조건을 재현합니다.
     */
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
