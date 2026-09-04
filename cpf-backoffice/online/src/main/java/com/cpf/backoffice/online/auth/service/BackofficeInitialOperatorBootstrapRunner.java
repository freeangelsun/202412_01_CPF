package com.cpf.backoffice.online.auth.service;

import com.cpf.foundation.runtime.CpfInstanceIdentity;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 모든 Profile에서 같은 최초 MBW 운영자 계약을 실행한다.
 *
 * <p>비밀번호는 Spring property/YAML/command line이 아니라 `CPF_MBW_BOOTSTRAP_PASSWORD` 환경변수에서만
 * 읽는다. 로그와 audit에는 값 대신 bootstrapSecretProvided 여부만 남긴다.</p>
 */
@Component
public final class BackofficeInitialOperatorBootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BackofficeInitialOperatorBootstrapRunner.class);
    private static final String PASSWORD_ENV = "CPF_MBW_BOOTSTRAP_PASSWORD";

    private final Environment environment;
    private final BackofficeInitialOperatorBootstrapService service;

    public BackofficeInitialOperatorBootstrapRunner(
            Environment environment,
            BackofficeInitialOperatorBootstrapService service) {
        this.environment = environment;
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        String rawPassword = System.getenv(PASSWORD_ENV);
        char[] password = rawPassword == null ? null : rawPassword.toCharArray();
        try {
            if (environment.getProperty("cpf.backoffice.bootstrap.approval-token-file") != null
                    && !environment.getProperty("cpf.backoffice.bootstrap.approval-token-file").isBlank()
                    && rawPassword != null) {
                throw new IllegalStateException("MBW_INITIAL_AND_APPROVED_BOOTSTRAP_CANNOT_RUN_TOGETHER");
            }
            BackofficeInitialOperatorBootstrapService.Result result = service.bootstrap(
                    new BackofficeInitialOperatorBootstrapService.Command(
                            property("cpf.backoffice.initial-operator.login-id"),
                            property("cpf.backoffice.initial-operator.operator-name"),
                            property("cpf.backoffice.initial-operator.role-code"),
                            passwordExpiryDays(),
                            password,
                            property("cpf.environment.code"),
                            List.of(environment.getActiveProfiles()),
                            CpfInstanceIdentity.instanceId()));
            log.info(
                    "MBW initial operator bootstrap result. status={}, loginId={}, bootstrapSecretProvided={}",
                    result.status(), result.loginId(), rawPassword != null && !rawPassword.isBlank());
        } finally {
            if (password != null) Arrays.fill(password, '\0');
        }
    }

    private String property(String key) {
        String value = environment.getProperty(key);
        return value == null ? null : value.trim();
    }

    private int passwordExpiryDays() {
        String value = property("cpf.backoffice.initial-operator.password-expiry-days");
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException failure) {
            throw new IllegalStateException("MBW_INITIAL_OPERATOR_PASSWORD_EXPIRY_DAYS_INVALID", failure);
        }
    }
}
