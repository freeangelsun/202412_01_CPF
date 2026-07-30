package com.cpf.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/** 기동 시 설치 안전 상한의 모순을 fail-fast 합니다. */
@Component
final class CpfGatewaySafetyStartupValidator {
    private final CpfGatewaySafetyProperties properties;
    CpfGatewaySafetyStartupValidator(CpfGatewaySafetyProperties properties){this.properties=properties;}
    @PostConstruct void validate(){properties.validate();}
}
