package com.cpf.starter.data.persistence.jdbc.quality;

import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Production JDBC provider for the platform data-quality capability. */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
public class CpfDataQualityJdbcAutoConfiguration {
    @Bean
    @ConditionalOnBean({DataSource.class, PlatformTransactionManager.class, ObjectMapper.class})
    @ConditionalOnMissingBean(value = {CpfDataQualityOperations.class, CpfDataQualityCorrectionPort.class})
    JdbcCpfDataQualityOperations cpfJdbcDataQualityOperations(
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
            ObjectMapper objectMapper) {
        return new JdbcCpfDataQualityOperations(
                new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager), objectMapper);
    }
}
