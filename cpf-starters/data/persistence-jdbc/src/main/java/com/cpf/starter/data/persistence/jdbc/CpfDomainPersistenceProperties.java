package com.cpf.starter.data.persistence.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.domain.persistence")
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
