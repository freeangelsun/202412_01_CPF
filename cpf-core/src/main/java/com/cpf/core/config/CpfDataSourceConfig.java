package com.cpf.core.config;

import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.common.database.CpfDataSourceResolver;
import com.cpf.core.common.database.CpfJdbcReplicaHealthMonitor;
import com.cpf.core.common.database.CpfReadRoutingRuntimePolicy;
import com.cpf.core.common.database.CpfReadWriteRoutingDataSource;
import com.cpf.core.common.database.CpfReplicaHealthMonitor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/** CPF 메타 DB와 선택적 Primary/Replica routing을 구성합니다. */
@Configuration
public class CpfDataSourceConfig {

    @Bean(name = "cpfJndiTemplate")
    public JndiTemplate cpfJndiTemplate() { return new JndiTemplate(); }

    @Bean(name = "cpfPrimaryDataSource")
    public DataSource cpfPrimaryDataSource(
            Environment environment,
            @Qualifier("cpfJndiTemplate") JndiTemplate jndiTemplate) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cpf", jndiTemplate);
    }

    @Bean(name = "cpfReplicaDataSource")
    @ConditionalOnProperty(prefix = "cpf.db.read-routing", name = "enabled", havingValue = "true")
    public DataSource cpfReplicaDataSource(
            Environment environment,
            @Qualifier("cpfJndiTemplate") JndiTemplate jndiTemplate) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cpf.replica", jndiTemplate);
    }

    @Bean
    public CpfReadRoutingRuntimePolicy cpfReadRoutingRuntimePolicy(Environment environment) {
        CpfReadRoutingRuntimePolicy policy = new CpfReadRoutingRuntimePolicy();
        policy.replace(
                environment.getProperty("cpf.db.read-routing.enabled", Boolean.class, false),
                environment.getProperty("cpf.db.read-routing.max-replica-lag-ms", Long.class, 5000L),
                environment.getProperty("cpf.db.read-routing.read-after-write-ms", Long.class, 3000L));
        return policy;
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.db.read-routing", name = "enabled", havingValue = "true")
    public CpfReplicaHealthMonitor cpfReplicaHealthMonitor(
            @Qualifier("cpfReplicaDataSource") DataSource replicaDataSource) {
        return new CpfJdbcReplicaHealthMonitor(replicaDataSource);
    }

    @Bean(name = "cpfDataSource")
    public DataSource cpfDataSource(
            @Qualifier("cpfPrimaryDataSource") DataSource primary,
            @Qualifier("cpfReplicaDataSource") ObjectProvider<DataSource> replicaProvider,
            CpfReadRoutingRuntimePolicy policy,
            ObjectProvider<CpfReplicaHealthMonitor> monitorProvider) {
        DataSource replica = replicaProvider.getIfAvailable();
        CpfReplicaHealthMonitor monitor = monitorProvider.getIfAvailable();
        if (replica == null || monitor == null || !policy.current().enabled()) return primary;
        CpfReadWriteRoutingDataSource routing = new CpfReadWriteRoutingDataSource(policy, monitor);
        Map<Object, Object> targets = new LinkedHashMap<>();
        targets.put("WRITE", primary);
        targets.put("READ", replica);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();
        return routing;
    }

    @Bean
    @ConditionalOnMissingBean(CpfVendorSqlCatalogProvider.class)
    public CpfVendorSqlCatalogProvider cpfVendorSqlCatalogProvider(Environment environment) {
        return moduleCode -> com.cpf.core.common.database.CpfVendorSqlCatalog.create(environment, moduleCode);
    }

    @Bean(name = "cpfTransactionManager")
    public PlatformTransactionManager cpfTransactionManager(@Qualifier("cpfDataSource") DataSource cpfDataSource) {
        return new DataSourceTransactionManager(cpfDataSource);
    }

    @Bean(name = "cpfJdbcTemplate")
    public JdbcTemplate cpfJdbcTemplate(@Qualifier("cpfDataSource") DataSource cpfDataSource) {
        return new JdbcTemplate(cpfDataSource);
    }
}
