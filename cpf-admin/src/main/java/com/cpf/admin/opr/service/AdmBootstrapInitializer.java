package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.core.common.exception.CpfValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 명시적으로 승인된 경우에만 ADM 최초 운영자를 생성합니다.
 *
 * <p>기존 계정의 비밀번호는 재설정하지 않으므로 재기동에도 idempotent합니다. 비밀번호 원문은
 * 어떤 로그에도 남기지 않으며 운영 profile에서는 별도 승인 플래그를 요구합니다.</p>
 */
@Component
public class AdmBootstrapInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdmBootstrapInitializer.class);

    private final AdmBootstrapProperties properties;
    private final AdmOperatorService operatorService;
    private final Environment environment;

    public AdmBootstrapInitializer(
            AdmBootstrapProperties properties,
            AdmOperatorService operatorService,
            Environment environment) {
        this.properties = properties;
        this.operatorService = operatorService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        boolean prod = Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
        if (prod && !properties.isAllowProd()) {
            throw new CpfValidationException("prod ADM bootstrap은 CPF_ADM_BOOTSTRAP_ALLOW_PROD=true 승인이 필요합니다.");
        }
        if (properties.getPassword() == null || properties.getPassword().isBlank()) {
            throw new CpfValidationException("ADM bootstrap 비밀번호 환경변수가 필요합니다.");
        }
        boolean created = operatorService.bootstrapOperator(
                properties.getOperatorId(),
                properties.getOperatorName(),
                properties.getPassword());
        log.info("ADM bootstrap 처리 결과입니다. operatorId={}, created={}", properties.getOperatorId(), created);
    }
}
