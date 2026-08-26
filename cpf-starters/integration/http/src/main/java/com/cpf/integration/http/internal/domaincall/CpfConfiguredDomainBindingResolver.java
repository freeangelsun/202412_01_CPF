package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.domain.CpfDomainBindingResolver;
import java.util.Locale;
import java.util.Objects;

/** application/profile 설정을 topology-independent Binding으로 변환합니다. */
public final class CpfConfiguredDomainBindingResolver implements CpfDomainBindingResolver {
    private final CpfDomainCallProperties properties;
    public CpfConfiguredDomainBindingResolver(CpfDomainCallProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }
    @Override
    public CpfDomainBinding resolve(String systemCode) {
        if (systemCode == null || systemCode.isBlank()) throw new IllegalArgumentException("systemCode는 필수입니다.");
        String normalized = systemCode.trim().toUpperCase(Locale.ROOT);
        CpfDomainCallProperties.Binding configured = properties.getBindings().entrySet().stream()
                .filter(e -> normalized.equals(e.getKey().trim().toUpperCase(Locale.ROOT)))
                .map(value -> java.util.Map.Entry.getValue(value)).findFirst().orElse(null);
        if (configured == null) return CpfDomainBinding.auto(null);
        return new CpfDomainBinding(configured.getMode(), configured.getServiceId());
    }
}
