package com.cpf.integration.resilience.api;

/** 검증 Profile에서만 사용할 수 있는 통제형 장애 주입 계약입니다. */
public interface CpfFaultInjector {
    void before(String targetId);
}
