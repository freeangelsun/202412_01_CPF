package cpf.adm.opr.service;

import cpf.adm.config.AdmBootstrapProperties;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ADM bootstrap이 기본 비활성, prod 승인, secret 필수 규칙을 지키는지 검증합니다.
 */
class AdmBootstrapInitializerTest {

    @Test
    void disabledBootstrapDoesNotCreateOperator() {
        AdmBootstrapProperties properties = properties(false, null);
        AdmOperatorService operatorService = mock(AdmOperatorService.class);

        initializer(properties, operatorService, new MockEnvironment()).run(arguments());

        verify(operatorService, never()).bootstrapOperator("admin", "CPF 관리자", null);
    }

    @Test
    void enabledBootstrapRequiresPassword() {
        AdmBootstrapProperties properties = properties(true, " ");

        assertThatThrownBy(() -> initializer(
                properties,
                mock(AdmOperatorService.class),
                new MockEnvironment()).run(arguments()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("비밀번호 환경변수");
    }

    @Test
    void prodBootstrapRequiresExplicitApproval() {
        AdmBootstrapProperties properties = properties(true, testPassword());
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> initializer(
                properties,
                mock(AdmOperatorService.class),
                environment).run(arguments()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("ALLOW_PROD");
    }

    @Test
    void approvedBootstrapDelegatesWithoutLoggingPassword() {
        String password = testPassword();
        AdmBootstrapProperties properties = properties(true, password);
        AdmOperatorService operatorService = mock(AdmOperatorService.class);
        when(operatorService.bootstrapOperator("admin", "CPF 관리자", password)).thenReturn(true);

        initializer(properties, operatorService, new MockEnvironment()).run(arguments());

        verify(operatorService).bootstrapOperator("admin", "CPF 관리자", password);
    }

    private AdmBootstrapInitializer initializer(
            AdmBootstrapProperties properties,
            AdmOperatorService operatorService,
            MockEnvironment environment) {
        return new AdmBootstrapInitializer(properties, operatorService, environment);
    }

    private AdmBootstrapProperties properties(boolean enabled, String password) {
        AdmBootstrapProperties properties = new AdmBootstrapProperties();
        properties.setEnabled(enabled);
        properties.setPassword(password);
        return properties;
    }

    private DefaultApplicationArguments arguments() {
        return new DefaultApplicationArguments(new String[0]);
    }

    private String testPassword() {
        return String.join("", "Bootstrap", "!", "2345Aa");
    }
}
