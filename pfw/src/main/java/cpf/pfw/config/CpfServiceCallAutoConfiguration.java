package cpf.pfw.config;

import cpf.pfw.common.servicecall.CpfEndpointRegistry;
import cpf.pfw.common.servicecall.CpfEndpointResolver;
import cpf.pfw.common.servicecall.CpfHealthAwareInstanceSelector;
import cpf.pfw.common.servicecall.CpfRemoteFacadeProxySupport;
import cpf.pfw.common.servicecall.CpfRoutingPolicyResolver;
import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.CpfServiceCallLogWriter;
import cpf.pfw.common.servicecall.CpfServiceCallProperties;
import cpf.pfw.common.servicecall.CpfServiceHealthChecker;
import cpf.pfw.common.servicecall.CpfServiceInstanceRegistry;
import cpf.pfw.common.servicecall.CpfServiceRegistry;
import cpf.pfw.common.servicecall.CpfServiceRegistryRepository;
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
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new CpfServiceRegistryRepository(jdbcTemplateProvider, dataSourceProvider);
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
            CpfServiceCallProperties properties) {
        return new CpfServiceCallEngine(endpointResolver, logWriter, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteFacadeProxySupport cpfRemoteFacadeProxySupport(CpfServiceCallEngine serviceCallEngine) {
        return new CpfRemoteFacadeProxySupport(serviceCallEngine);
    }
}
