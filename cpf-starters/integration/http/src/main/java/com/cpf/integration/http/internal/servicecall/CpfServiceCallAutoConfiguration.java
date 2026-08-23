package com.cpf.integration.http.internal.servicecall;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.integration.api.servicecall.CpfServiceCallExecutor;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.platform.operations.observability.api.lineage.CpfLineageRecorder;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import com.cpf.platform.operations.reconciliation.CpfReconciliationPort;
import java.time.Clock;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Service Call의 Registry→Routing→Engine→Public Port를 실제 Runtime Bean으로 연결합니다.
 *
 * <p>선언만 존재하는 추상화가 되지 않도록 Public Caller/Executor와 운영 Registry Port가 같은
 * Repository/Engine을 소비합니다. 선택 Observability/Recovery Provider가 없으면 호출 자체는 동작하되
 * 해당 기능은 명시적으로 비활성 상태로 남습니다.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfServiceCallProperties.class)
public class CpfServiceCallAutoConfiguration {

    /** cpfDB Service Registry 저장소를 구성합니다. */
    @Bean
    @ConditionalOnMissingBean
    CpfServiceRegistryRepository cpfServiceRegistryRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplates,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSources) {
        return new CpfServiceRegistryRepository(jdbcTemplates, dataSources);
    }

    /** Service Registry 조회기를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfServiceRegistry cpfServiceRegistry(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistry(repository);
    }

    /** Endpoint Registry 조회기를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfEndpointRegistry cpfEndpointRegistry(CpfServiceRegistryRepository repository) {
        return new CpfEndpointRegistry(repository);
    }

    /** Instance Registry 조회기를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfServiceInstanceRegistry cpfServiceInstanceRegistry(CpfServiceRegistryRepository repository) {
        return new CpfServiceInstanceRegistry(repository);
    }

    /** Routing 정책 해석기를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfRoutingPolicyResolver cpfRoutingPolicyResolver(CpfServiceRegistryRepository repository) {
        return new CpfRoutingPolicyResolver(repository);
    }

    /** Health/Drain/Weight를 반영하는 Instance 선택기를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfHealthAwareInstanceSelector cpfHealthAwareInstanceSelector() {
        return new CpfHealthAwareInstanceSelector();
    }

    /** 논리 Service/Endpoint를 실제 Instance로 해석하는 Resolver를 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfEndpointResolver cpfEndpointResolver(
            CpfServiceRegistry services,
            CpfEndpointRegistry endpoints,
            CpfServiceInstanceRegistry instances,
            CpfRoutingPolicyResolver policies,
            CpfHealthAwareInstanceSelector selector) {
        return new CpfEndpointResolver(services, endpoints, instances, policies, selector);
    }

    /** 호출이력·Health·Circuit 기록기를 Framework Clock과 연결합니다. */
    @Bean @ConditionalOnMissingBean
    CpfServiceCallLogWriter cpfServiceCallLogWriter(
            CpfServiceRegistryRepository repository,
            ObjectProvider<Clock> clocks) {
        return new CpfServiceCallLogWriter(repository, clocks.getIfUnique(Clock::systemUTC));
    }

    /** 표준 4상태 결과와 UNKNOWN Recovery를 처리하는 Service Call Engine을 구성합니다. */
    @Bean @ConditionalOnMissingBean
    CpfServiceCallEngine cpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            ObjectProvider<CpfTransactionSegmentPort> segments,
            ObjectProvider<CpfReconciliationPort> reconciliations,
            ObjectProvider<CpfLineageRecorder> lineageRecorders,
            ObjectProvider<Clock> clocks,
            ObjectProvider<CpfExecutionIdGenerator> executionIds) {
        properties.validate();
        return new CpfServiceCallEngine(
                endpointResolver,
                logWriter,
                properties,
                segments.getIfAvailable(),
                reconciliations.getIfAvailable(),
                lineageRecorders.getIfAvailable(),
                clocks.getIfUnique(Clock::systemUTC),
                () -> {
                    CpfExecutionIdGenerator generator = executionIds.getIfAvailable();
                    return generator == null ? UUID.randomUUID().toString() : generator.newExecutionId();
                });
    }

    /** 호환 Public Caller를 표준 Engine에 연결합니다. */
    @Bean @ConditionalOnMissingBean(CpfServiceCaller.class)
    CpfServiceCaller cpfServiceCaller(CpfServiceCallEngine engine) {
        return new CpfServiceCallerAdapter(engine);
    }

    /** 신규 Public Executor를 표준 Engine에 연결합니다. */
    @Bean @ConditionalOnMissingBean(CpfServiceCallExecutor.class)
    CpfServiceCallExecutor cpfServiceCallExecutor(CpfServiceCallEngine engine) {
        return new CpfServiceCallExecutorAdapter(engine);
    }

    /** ADM/운영 조회가 같은 Registry 저장소를 사용하도록 Query Port를 연결합니다. */
    @Bean @ConditionalOnMissingBean(CpfServiceRegistryQueryPort.class)
    CpfServiceRegistryQueryPort cpfServiceRegistryQueryPort(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryQueryFacade(repository);
    }

    /** ADM/운영 변경이 같은 Registry 저장소를 사용하도록 Control Port를 연결합니다. */
    @Bean @ConditionalOnMissingBean(CpfServiceRegistryControlPort.class)
    CpfServiceRegistryControlPort cpfServiceRegistryControlPort(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryControlFacade(repository);
    }
}
