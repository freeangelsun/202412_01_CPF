package com.cpf.core.config;

import com.cpf.core.common.logging.policy.JdbcLogPolicyRepository;
import com.cpf.core.common.logging.policy.LogPolicyCache;
import com.cpf.core.common.logging.policy.LogPolicyRepository;
import com.cpf.core.common.logging.policy.LogPolicyResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * CPF 로그 정책 resolver/cache 자동 구성입니다.
 */
@Configuration(proxyBeanMethods = false)
public class CpfLogPolicyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogPolicyRepository logPolicyRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new JdbcLogPolicyRepository(jdbcTemplateProvider, dataSourceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogPolicyCache logPolicyCache(LogPolicyRepository repository, Environment environment) {
        return new LogPolicyCache(repository, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogPolicyResolver logPolicyResolver(LogPolicyCache cache) {
        return new LogPolicyResolver(cache);
    }
}
