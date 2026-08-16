package com.cpf.core.api.domain;

/** 논리 systemCode와 물리 serviceId의 Runtime Binding입니다. */
public record CpfDomainBinding(CpfDomainBindingMode mode, String serviceId) {
    public CpfDomainBinding {
        if (mode == null) mode = CpfDomainBindingMode.AUTO;
        serviceId = serviceId == null || serviceId.isBlank() ? null : serviceId.trim();
        if (mode == CpfDomainBindingMode.REMOTE && serviceId == null) {
            throw new IllegalArgumentException("REMOTE Domain Binding은 serviceId가 필수입니다.");
        }
    }

    /** Local operation 우선 AUTO Binding을 생성합니다. */
    public static CpfDomainBinding auto(String serviceId) {
        return new CpfDomainBinding(CpfDomainBindingMode.AUTO, serviceId);
    }
}
