package com.cpf.starter.platform.health.jdbc;
import com.cpf.core.api.health.CpfRuntimeHealthRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
@AutoConfiguration
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix="cpf.platform.health.registry.jdbc",name="enabled",havingValue="true",matchIfMissing=true)
public class CpfRuntimeHealthJdbcAutoConfiguration {
 @Bean @ConditionalOnMissingBean(CpfRuntimeHealthRegistry.class)
 CpfRuntimeHealthRegistry cpfRuntimeHealthRegistry(DataSource ds,ObjectMapper mapper){return new JdbcCpfRuntimeHealthRegistry(new JdbcTemplate(ds),mapper);}
}
