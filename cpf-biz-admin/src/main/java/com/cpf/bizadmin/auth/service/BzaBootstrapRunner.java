package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.core.common.security.password.CpfPasswordHashingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/** 명시적 환경변수가 모두 제공된 경우에만 최초 BZA 운영자를 한 번 생성합니다. */
@Component
public class BzaBootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BzaBootstrapRunner.class);

    private final Environment environment;
    private final CpfPasswordHashingPort passwordHashingPort;
    private final BzaAuthRepository authRepository;

    public BzaBootstrapRunner(
            Environment environment,
            CpfPasswordHashingPort passwordHashingPort,
            BzaAuthRepository authRepository) {
        this.environment = environment;
        this.passwordHashingPort = passwordHashingPort;
        this.authRepository = authRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.getProperty("cpf.bza.bootstrap.enabled", Boolean.class, false)) {
            return;
        }
        String loginId = require("cpf.bza.bootstrap.login-id");
        String operatorName = require("cpf.bza.bootstrap.operator-name");
        String password = require("cpf.bza.bootstrap.password");
        String roleCode = environment.getProperty("cpf.bza.bootstrap.role-code", "BZA_MANAGER");
        requireStrongPassword(loginId, password);
        char[] chars = password.toCharArray();
        try {
            int inserted = authRepository.bootstrapOperator(
                    loginId, operatorName, passwordHashingPort.hash(chars), roleCode);
            log.info("BZA 최초 운영자 bootstrap 결과를 기록했습니다. loginId={}, created={}", loginId, inserted == 1);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private String require(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("BZA bootstrap 활성화 시 설정이 필요합니다. key=" + key);
        }
        return value.trim();
    }

    private void requireStrongPassword(String loginId, String password) {
        long categories = java.util.stream.Stream.of(
                password.matches(".*[A-Z].*"), password.matches(".*[a-z].*"),
                password.matches(".*[0-9].*"), password.matches(".*[^A-Za-z0-9].*"))
                .filter(Boolean::booleanValue)
                .count();
        if (password.length() < 12 || categories < 3 || password.toLowerCase().contains(loginId.toLowerCase())) {
            throw new IllegalStateException("BZA bootstrap 비밀번호가 보안 정책을 충족하지 않습니다.");
        }
    }
}
