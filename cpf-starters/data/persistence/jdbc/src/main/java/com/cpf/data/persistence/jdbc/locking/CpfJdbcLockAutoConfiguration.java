package com.cpf.data.persistence.jdbc.locking;

import com.cpf.data.lock.api.CpfLockManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** JDBC 기반 CPF LockManager Provider를 등록합니다. */
@AutoConfiguration(afterName="com.cpf.data.persistence.jdbc.config.CpfDataSourceConfig")
@ConditionalOnBean(name={"cpfJdbcTemplate","cpfTransactionManager"})
public class CpfJdbcLockAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfJdbcLockManager.Store.class)
    CpfJdbcLockManager.Store cpfJdbcLockStore(@Qualifier("cpfJdbcTemplate") JdbcTemplate jdbcTemplate){ return new JdbcCpfLockStore(jdbcTemplate); }
    @Bean
    @ConditionalOnMissingBean(CpfLockManager.class)
    CpfLockManager cpfLockManager(CpfJdbcLockManager.Store store,@Qualifier("cpfTransactionManager") PlatformTransactionManager tx){ return new CpfJdbcLockManager(store,tx); }
}
