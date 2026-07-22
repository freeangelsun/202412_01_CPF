package com.cpf.core.config;

import com.cpf.core.common.servicecall.CpfEndpointRegistry;
import com.cpf.core.common.servicecall.CpfEndpointResolver;
import com.cpf.core.common.servicecall.CpfHealthAwareInstanceSelector;
import com.cpf.core.common.servicecall.CpfRemoteFacadeProxySupport;
import com.cpf.core.common.servicecall.CpfRoutingPolicyResolver;
import com.cpf.core.common.servicecall.CpfServiceCallEngine;
import com.cpf.core.common.servicecall.CpfServiceCallLogWriter;
import com.cpf.core.common.servicecall.CpfServiceCallProperties;
import com.cpf.core.common.servicecall.CpfServiceHealthChecker;
import com.cpf.core.common.servicecall.CpfServiceInstanceRegistry;
import com.cpf.core.common.servicecall.CpfServiceRegistry;
import com.cpf.core.common.servicecall.CpfServiceRegistryRepository;
import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.core.common.servicecall.CpfServiceRegistryQueryFacade;
import com.cpf.core.common.logging.segment.TransactionSegmentService;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * CPF 서비스 호출 엔진 자동 구성입니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfServiceCallProperties.class)
public class CpfServiceCallAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceRegistryRepository cpfServiceRegistryRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new CpfServiceRegistryRepository(jdbcTemplateProvider, dataSourceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceRegistryQueryPort cpfServiceRegistryQueryPort(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryQueryFacade(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceRegistry cpfServiceRegistry(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistry(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfEndpointRegistry cpfEndpointRegistry(CpfServiceRegistryRepository repository) {
        return new CpfEndpointRegistry(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceInstanceRegistry cpfServiceInstanceRegistry(CpfServiceRegistryRepository repository) {
        return new CpfServiceInstanceRegistry(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRoutingPolicyResolver cpfRoutingPolicyResolver(CpfServiceRegistryRepository repository) {
        return new CpfRoutingPolicyResolver(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfHealthAwareInstanceSelector cpfHealthAwareInstanceSelector() {
        return new CpfHealthAwareInstanceSelector();
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfEndpointResolver cpfEndpointResolver(
            CpfServiceRegistry serviceRegistry,
            CpfEndpointRegistry endpointRegistry,
            CpfServiceInstanceRegistry instanceRegistry,
            CpfRoutingPolicyResolver routingPolicyResolver,
            CpfHealthAwareInstanceSelector instanceSelector) {
        return new CpfEndpointResolver(
                serviceRegistry,
                endpointRegistry,
                instanceRegistry,
                routingPolicyResolver,
                instanceSelector);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceCallLogWriter cpfServiceCallLogWriter(CpfServiceRegistryRepository repository) {
        return new CpfServiceCallLogWriter(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceHealthChecker cpfServiceHealthChecker() {
        return new CpfServiceHealthChecker();
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfServiceCallEngine cpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            ObjectProvider<TransactionSegmentService> segmentServiceProvider,
            ObjectProvider<CpfReconciliationPort> reconciliationPortProvider) {
        return new CpfServiceCallEngine(
                endpointResolver,
                logWriter,
                properties,
                segmentServiceProvider.getIfAvailable(),
                reconciliationPortProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteFacadeProxySupport cpfRemoteFacadeProxySupport(CpfServiceCallEngine serviceCallEngine) {
        return new CpfRemoteFacadeProxySupport(serviceCallEngine);
    }
}
