package com.cpf.backoffice.online.auth.service;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
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
    private final CpfContextExecutionFactory contextFactory;

    public BackofficeInitialOperatorBootstrapRunner(
            Environment environment,
            BackofficeInitialOperatorBootstrapService service,
            CpfContextExecutionFactory contextFactory) {
        this.environment = environment;
        this.service = service;
        this.contextFactory = contextFactory;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        // ApplicationRunner 는 HTTP/Batch 경계가 아니어서 CPF 실행 Context 가 없다. 최초 운영자 생성은
        // 업무 감사를 남기고 감사는 transactionId 를 요구하므로 관리 실행 Context 를 연다. 이 구간이
        // 없으면 Fresh 환경의 첫 기동이 "Managed CPF execution has no bound context" 로 실패한다.
        CpfContext root = contextFactory.newRoot(new CpfContextExecutionFactory.RootSpec(
                null, "mbw.bootstrap.initial-operator",
                CpfContext.CpfExecutionType.INTERNAL, CpfContext.CpfTransactionOriginKind.INTERNAL,
                null, null, null, null, null, null));
        try (AutoCloseable scope = CpfContexts.bind(CpfContextSnapshot.capture(root))) {
            bootstrapInitialOperator();
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IllegalStateException("MBW initial operator bootstrap context scope failed", failure);
        }
    }

    private void bootstrapInitialOperator() {
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
