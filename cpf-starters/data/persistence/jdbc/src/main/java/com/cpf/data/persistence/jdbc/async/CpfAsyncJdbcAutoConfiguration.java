package com.cpf.data.persistence.jdbc.async;
import com.cpf.starter.async.operation.CpfAsyncOperationStore; import org.springframework.boot.autoconfigure.AutoConfiguration; import org.springframework.boot.autoconfigure.condition.*; import org.springframework.context.annotation.Bean; import org.springframework.jdbc.core.JdbcTemplate;
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(JdbcTemplate.class)
public class CpfAsyncJdbcAutoConfiguration { @Bean @ConditionalOnMissingBean(CpfAsyncOperationStore.class) CpfAsyncOperationStore cpfAsyncOperationStore(JdbcTemplate jdbc){return new CpfJdbcAsyncOperationStore(jdbc);} }
