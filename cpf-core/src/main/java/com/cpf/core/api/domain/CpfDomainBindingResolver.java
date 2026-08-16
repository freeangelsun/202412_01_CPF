package com.cpf.core.api.domain;

/** Runtime topology를 업무 Source 밖에서 해석하는 공개 SPI입니다. */
@FunctionalInterface
public interface CpfDomainBindingResolver {
    CpfDomainBinding resolve(String systemCode);
}
