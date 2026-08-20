package com.cpf.common.runtime;

import com.cpf.common.management.CpfCommonManagementAuditSink;
import com.cpf.common.spi.CpfCommonPersistenceNames;
import com.cpf.common.message.service.CmnLoggingCommonManagementAuditSink;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    @Bean(name = DATA_SOURCE_BEAN)
    DataSource cpfCommonDataSource(CpfDataSourceRegistry dataSources, Environment environment) {
        try {
            DataSource dataSource = dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
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

    @Bean(name = "cpfCommonJdbcTemplate")
    JdbcTemplate cpfCommonJdbcTemplate(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new JdbcTemplate(cpfCommonDataSource);
    }

    @Bean(name = "cpfCommonNamedJdbcTemplate")
    NamedParameterJdbcTemplate cpfCommonNamedJdbcTemplate(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new NamedParameterJdbcTemplate(cpfCommonDataSource);
    }

    @Bean(name = TX_MANAGER_BEAN)
    PlatformTransactionManager cpfCommonTransactionManager(@Qualifier(DATA_SOURCE_BEAN) DataSource cpfCommonDataSource) {
        return new DataSourceTransactionManager(cpfCommonDataSource);
    }

    /** Common Product Service의 effective-time/audit/cache 시간을 하나의 override 가능한 Clock으로 통일합니다. */
    @Bean
    @ConditionalOnMissingBean(Clock.class)
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
