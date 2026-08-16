package com.cpf.core.api.domain;

/** 논리 Domain을 동일 JVM 또는 원격 Runtime에 연결하는 Binding 모드입니다. */
public enum CpfDomainBindingMode {
    AUTO,
    LOCAL,
    REMOTE
}
