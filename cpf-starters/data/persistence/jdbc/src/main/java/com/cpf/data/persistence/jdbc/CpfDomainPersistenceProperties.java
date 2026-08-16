package com.cpf.data.persistence.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.domain.persistence")
/** CpfDomainPersistenceProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfDomainPersistenceProperties(
        boolean enabled, boolean required, String provider, String dataSourcePrefix) {
    public CpfDomainPersistenceProperties(boolean enabled, boolean required, String dataSourcePrefix) {
        this(enabled, required, enabled ? "mybatis" : "", dataSourcePrefix);
    }

    public CpfDomainPersistenceProperties {
        provider = provider == null ? "" : provider.trim().toLowerCase(java.util.Locale.ROOT);
        dataSourcePrefix = dataSourcePrefix == null ? "" : dataSourcePrefix.trim();
        if (enabled && !java.util.Set.of("jdbc", "mybatis").contains(provider)) {
            throw new IllegalArgumentException("cpf.domain.persistence.provider must be jdbc or mybatis when enabled");
        }
        if (enabled && dataSourcePrefix.isBlank()) {
            throw new IllegalArgumentException("cpf.domain.persistence.data-source-prefix is required when enabled");
        }
    }
}
