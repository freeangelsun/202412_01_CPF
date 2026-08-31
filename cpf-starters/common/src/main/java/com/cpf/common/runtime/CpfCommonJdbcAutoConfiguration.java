package com.cpf.common.runtime;

import com.cpf.common.management.CpfCommonManagementAuditSink;
import com.cpf.common.calendar.CmnCalendarChangePublisher;
import com.cpf.common.calendar.CmnDurableCalendarChangePublisher;
import com.cpf.common.calendar.CmnJdbcCalendarStore;
import com.cpf.common.calendar.CmnCalendarStore;
import com.cpf.common.spi.CpfCommonCacheChangePublisher;
import com.cpf.common.template.CmnJdbcTemplateStore;
import com.cpf.common.template.CmnTemplateManagementService;
import com.cpf.common.template.CmnTemplateProvider;
import com.cpf.common.template.CmnTemplateRenderer;
import com.cpf.common.template.CmnTemplateService;
import com.cpf.common.template.CmnTemplateStore;
import com.cpf.common.spi.CpfCommonPersistenceNames;
import com.cpf.common.message.service.CmnLoggingCommonManagementAuditSink;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.Clock;
import java.sql.Connection;

/**
 * CPF Common Product Service의 cpfDB JDBC runtime 조립입니다.
 *
 * <p>Common은 Memory 성공으로 DB 장애를 숨기지 않습니다. product runtime에서
 * {@code cpf.common.datasource.url} 또는 {@code cpf.common.datasource.jndi-name}이 없거나
 * 연결 검증이 실패하면 Application startup을 실패시킵니다.</p>
 */
@AutoConfiguration
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = {"com.cpf.common.code", "com.cpf.common.parameter", "com.cpf.common.message", "com.cpf.common.management", "com.cpf.common.calendar", "com.cpf.common.template", "com.cpf.common.runtime.cache"})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
@ConditionalOnProperty(prefix = "cpf.common", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CpfCommonJdbcAutoConfiguration {
    public static final String DATA_SOURCE_BEAN = CpfCommonPersistenceNames.DATA_SOURCE_BEAN;
    public static final String TX_MANAGER_BEAN = CpfCommonPersistenceNames.TX_MANAGER_BEAN;

    @Bean(name = {DATA_SOURCE_BEAN, CpfCommonPersistenceNames.PLATFORM_DATA_SOURCE_BEAN})
    DataSource cpfCommonDataSource(CpfDataSourceRegistry dataSources, Environment environment) {
        DataSource dataSource;
        try {
            dataSource = dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
        } catch (RuntimeException resolution) {
            // Role 해석 실패는 접속 정보가 아니라 설정 결함이다. 어떤 설정이 빠졌는지 알 수 없으면
            // 기동 실패 원인을 추적할 수 없으므로, secret을 담지 않는 이 원인은 그대로 보존한다.
            throw new IllegalStateException(
                    "CPF Common requires the CPF_PLATFORM_DB role to resolve a DataSource", resolution);
        }
        try {
            int timeout = Math.max(1, environment.getProperty("cpf.common.datasource.validation-timeout-seconds", Integer.class, 3));
            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isValid(timeout)) {
                    throw new IllegalStateException("CPF Common cpfDB validation returned false");
                }
            }
            return dataSource;
        } catch (Exception failure) {
            // URL, SQL and secret values are deliberately excluded from the externalized exception text.
            throw new IllegalStateException("CPF Common requires a reachable cpfDB DataSource", null);
        }
    }

    @Bean(name = {CpfCommonPersistenceNames.JDBC_TEMPLATE_BEAN, CpfCommonPersistenceNames.PLATFORM_JDBC_TEMPLATE_BEAN})
    JdbcTemplate cpfCommonJdbcTemplate(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new JdbcTemplate(cpfCommonDataSource);
    }

    @Bean(name = "cpfCommonNamedJdbcTemplate")
    NamedParameterJdbcTemplate cpfCommonNamedJdbcTemplate(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new NamedParameterJdbcTemplate(cpfCommonDataSource);
    }

    @Bean(name = {TX_MANAGER_BEAN, CpfCommonPersistenceNames.PLATFORM_TX_MANAGER_BEAN})
    PlatformTransactionManager cpfCommonTransactionManager(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new DataSourceTransactionManager(cpfCommonDataSource);
    }


    @Bean
    @ConditionalOnMissingBean(CmnCalendarChangePublisher.class)
    CmnCalendarChangePublisher cmnCalendarChangePublisher(CpfCommonCacheChangePublisher publisher) {
        return new CmnDurableCalendarChangePublisher(publisher);
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.common.calendar.jdbc.enabled", havingValue = "true", matchIfMissing = true)
    CmnCalendarStore cmnCalendarStore(@Qualifier(DATA_SOURCE_BEAN) DataSource dataSource) {
        return new CmnJdbcCalendarStore(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.common.template.jdbc.enabled", havingValue = "true", matchIfMissing = true)
    CmnTemplateStore cmnTemplateStore(@Qualifier(DATA_SOURCE_BEAN) DataSource dataSource) {
        return new CmnJdbcTemplateStore(dataSource);
    }

    @Bean
    @ConditionalOnBean(CmnTemplateProvider.class)
    @ConditionalOnMissingBean(CmnTemplateService.class)
    CmnTemplateService cmnTemplateService(CmnTemplateProvider provider, CmnTemplateRenderer renderer) {
        return new CmnTemplateService(provider, renderer);
    }

    @Bean
    @ConditionalOnBean(CmnTemplateStore.class)
    @ConditionalOnMissingBean(CmnTemplateManagementService.class)
    CmnTemplateManagementService cmnTemplateManagementService(
            CmnTemplateStore store, CpfCommonCacheChangePublisher publisher) {
        return new CmnTemplateManagementService(store, publisher);
    }

    /** Common Product Service의 effective-time/audit/cache 시간을 하나의 override 가능한 Clock으로 통일합니다. */
    @Bean(name = CpfCommonPersistenceNames.CLOCK_BEAN)
    @ConditionalOnMissingBean(name = CpfCommonPersistenceNames.CLOCK_BEAN)
    Clock cpfCommonClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(CpfCommonManagementAuditSink.class)
    CpfCommonManagementAuditSink cpfCommonManagementAuditSink() {
        return new CmnLoggingCommonManagementAuditSink();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cpfCommonDefaultCacheManager() {
        return new ConcurrentMapCacheManager("codeCache", "configCache", "messageCache", "responseCodeCache");
    }
}
