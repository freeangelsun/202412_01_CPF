package com.cpf.security.audit.jdbc;
import com.cpf.security.api.audit.CpfTamperAuditStore;import org.springframework.boot.autoconfigure.AutoConfiguration;import org.springframework.boot.autoconfigure.condition.*;import org.springframework.context.annotation.Bean;import org.springframework.jdbc.core.JdbcTemplate;
@AutoConfiguration @ConditionalOnClass(JdbcTemplate.class) @ConditionalOnProperty(prefix="cpf.security.audit",name="tamper-evident-enabled",havingValue="true")
public class CpfTamperAuditJdbcAutoConfiguration { @Bean @ConditionalOnMissingBean(CpfTamperAuditStore.class) CpfTamperAuditStore cpfTamperAuditStore(JdbcTemplate jdbc){return new JdbcCpfTamperAuditStore(jdbc);} }
