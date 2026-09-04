package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 명시적으로 승인된 경우에만 ADM 최초 운영자를 생성합니다.
 *
 * <p>기존 계정의 비밀번호는 재설정하지 않으므로 재기동에도 idempotent합니다. 비밀번호 원문은
 * 어떤 로그·감사·evidence에도 남기지 않습니다. local/dev/stg/test/prod의 보안 의미를 분기하지 않습니다.</p>
 */
@Component
public class AdmBootstrapInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdmBootstrapInitializer.class);
    private static final String PASSWORD_ENV = "CPF_ADM_BOOTSTRAP_PASSWORD";

    private final AdmBootstrapProperties properties;
    private final AdmOperatorService operatorService;
    private final AdmAuditLogService auditLogService;
    private final Supplier<String> bootstrapPassword;

    public AdmBootstrapInitializer(
            AdmBootstrapProperties properties,
            AdmOperatorService operatorService,
            AdmAuditLogService auditLogService) {
        this(properties, operatorService, auditLogService, () -> System.getenv(PASSWORD_ENV));
    }

    AdmBootstrapInitializer(
            AdmBootstrapProperties properties,
            AdmOperatorService operatorService,
            AdmAuditLogService auditLogService,
            Supplier<String> bootstrapPassword) {
        this.properties = properties;
        this.operatorService = operatorService;
        this.auditLogService = auditLogService;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    @CpfTransactional(transactionManager = "admTransactionManager")
    public void run(ApplicationArguments args) {
        String password = bootstrapPassword.get();
        if (operatorService.hasAnyOperator()) {
            log.info("ADM initial operator bootstrap result. status=ALREADY_BOOTSTRAPPED, bootstrapSecretProvided={}",
                    password != null && !password.isBlank());
            return;
        }
        if (password == null || password.isBlank()) {
            throw new CpfValidationException("ADM_INITIAL_OPERATOR_BOOTSTRAP_SECRET_REQUIRED");
        }
        String operatorId = require(properties.getOperatorId(), "operator-id");
        String operatorName = require(properties.getOperatorName(), "operator-name");
        boolean created = operatorService.bootstrapOperator(
                operatorId,
                operatorName,
                password);
        if (created) {
            auditLogService.record(
                    null,
                    "CPF_BOOTSTRAP",
                    "INITIAL_OPERATOR_BOOTSTRAP",
                    "ADM_OPERATOR",
                    operatorId,
                    "initial operator bootstrap",
                    null,
                    "{\"result\":\"CREATED\",\"source\":\"CPF_BOOTSTRAP\",\"bootstrapSecretProvided\":true}",
                    null,
                    "CPF_BOOTSTRAP");
        } else if (!operatorService.hasAnyOperator()) {
            throw new CpfValidationException("ADM_INITIAL_OPERATOR_BOOTSTRAP_RESULT_UNKNOWN");
        }
        log.info("ADM initial operator bootstrap result. status={}, operatorId={}, bootstrapSecretProvided={}",
                created ? "CREATED" : "ALREADY_BOOTSTRAPPED", operatorId, true);
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException("ADM_INITIAL_OPERATOR_PROPERTY_REQUIRED:" + name);
        }
        return value.trim();
    }
}
