package com.cpf.data.persistence.jdbc.async;
import com.cpf.starter.async.operation.CpfAsyncOperationStore; import org.springframework.boot.autoconfigure.AutoConfiguration; import org.springframework.boot.autoconfigure.condition.*; import org.springframework.context.annotation.Bean; import org.springframework.jdbc.core.JdbcTemplate;
/**
 * CPF 비동기 작업의 JDBC 영속 저장소를 Public Persistence Starter에 연결합니다.
 * <p>비동기 capability와 JDBC가 함께 활성화된 경우에만 사용하며, 재시작/복구 가능한 저장 계약을 제공합니다.
 */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(JdbcTemplate.class)
public class CpfAsyncJdbcAutoConfiguration { @Bean @ConditionalOnMissingBean(CpfAsyncOperationStore.class) CpfAsyncOperationStore cpfAsyncOperationStore(JdbcTemplate jdbc){return new CpfJdbcAsyncOperationStore(jdbc);} }
