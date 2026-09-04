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
    /** Generated Domain 이 자기 논리 role 을 선언하는 정본 property. */
    private static final String DOMAIN_ROLE_PROPERTY = "cpf.generated-domain.database-role";
    /** CpfDomainDataSourceAutoConfiguration 이 조립하는 Domain DataSource 의 표준 Bean 이름. */
    private static final String DOMAIN_DATA_SOURCE = "cpfDomainDataSource";
    private final ListableBeanFactory beanFactory;
    private final Environment environment;

    CpfJdbcDataSourceRegistry(ListableBeanFactory beanFactory, Environment environment) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    @Override
    public DataSource require(CpfDatabaseRole role) {
        Objects.requireNonNull(role, "role");
        String key = "cpf.data.persistence.jdbc.roles." + role.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
        String beanName = environment.getProperty(key);
        if (beanName != null && !beanName.isBlank()) {
            if (!beanFactory.containsBean(beanName) || !beanFactory.isTypeMatch(beanName, DataSource.class)) {
                throw new IllegalStateException("Configured CPF DataSource role bean is missing: " + role);
            }
            return beanFactory.getBean(beanName, DataSource.class);
        }
        String conventional = switch (role) {
            case CPF_PLATFORM_DB -> "cpfPlatformDataSource";
            case CUSTOMER_BUSINESS_DB -> "cpfCustomerBusinessDataSource";
        };
        if (beanFactory.containsBean(conventional) && beanFactory.isTypeMatch(conventional, DataSource.class)) {
            return beanFactory.getBean(conventional, DataSource.class);
        }
        // Generated Domain 은 자기 논리 role 을 cpf.generated-domain.database-role 로 선언하고
        // DataSource 는 표준 이름 cpfDomainDataSource 로 조립된다. 이 연결이 없으면 Domain Runtime 은
        // 자기 DB 를 갖고도 role 해석에 실패해 기동하지 못한다. 지금까지는 통합 Runtime 이 별도
        // role DataSource 를 켜 주었기 때문에 Domain 단독 기동 경로가 검증되지 않았다.
        String declaredRole = environment.getProperty(DOMAIN_ROLE_PROPERTY, "").trim();
        if (role.name().equalsIgnoreCase(declaredRole)
                && beanFactory.containsBean(DOMAIN_DATA_SOURCE)
                && beanFactory.isTypeMatch(DOMAIN_DATA_SOURCE, DataSource.class)) {
            return beanFactory.getBean(DOMAIN_DATA_SOURCE, DataSource.class);
        }
        Map<String, DataSource> candidates = beanFactory.getBeansOfType(DataSource.class, false, false);
        if (candidates.isEmpty()) throw new IllegalStateException("CPF DataSource is required for role: " + role);
        throw new IllegalStateException("CPF DataSource role is not mapped; configure " + key);
    }
}
