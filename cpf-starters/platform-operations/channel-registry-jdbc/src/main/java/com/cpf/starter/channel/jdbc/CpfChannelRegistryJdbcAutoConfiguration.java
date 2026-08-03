package com.cpf.starter.channel.jdbc;
import com.cpf.core.channel.adapter.JdbcCpfChannelRegistryAdapter;
import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
@AutoConfiguration
public class CpfChannelRegistryJdbcAutoConfiguration {
 @Bean @ConditionalOnMissingBean CpfChannelRegistryPort cpfChannelRegistryPort(@Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbc, Environment env){return new JdbcCpfChannelRegistryAdapter(jdbc,env);}
 @Bean @ConditionalOnMissingBean CpfChannelPolicyService cpfChannelPolicyService(CpfChannelRegistryPort port,Environment env){return new CpfChannelPolicyService(port,env.getProperty("cpf.channel-policy.startup-load-enabled",Boolean.class,true));}
}
