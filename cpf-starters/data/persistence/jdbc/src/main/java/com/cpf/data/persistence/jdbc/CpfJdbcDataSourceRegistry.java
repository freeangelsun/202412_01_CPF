package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/** JDBC Provider의 논리 Database role 해석기입니다. */
final class CpfJdbcDataSourceRegistry implements CpfDataSourceRegistry {
    private final ListableBeanFactory beanFactory;
    private final Environment environment;

    CpfJdbcDataSourceRegistry(ListableBeanFactory beanFactory, Environment environment) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    @Override
    public DataSource require(CpfDatabaseRole role) {
        Objects.requireNonNull(role, "role");
        String key = "cpf.data.persistence.jdbc.roles." + role.name().toLowerCase().replace('_', '-');
        String beanName = environment.getProperty(key);
        if (beanName != null && !beanName.isBlank()) {
            if (!beanFactory.containsBean(beanName) || !beanFactory.isTypeMatch(beanName, DataSource.class)) {
                throw new IllegalStateException("Configured CPF DataSource role bean is missing: " + role);
            }
            return beanFactory.getBean(beanName, DataSource.class);
        }
        Map<String, DataSource> candidates = beanFactory.getBeansOfType(DataSource.class, false, false);
        if (candidates.size() == 1) return candidates.values().iterator().next();
        if (candidates.isEmpty()) throw new IllegalStateException("CPF DataSource is required for role: " + role);
        throw new IllegalStateException("CPF DataSource role is ambiguous; configure " + key);
    }
}
