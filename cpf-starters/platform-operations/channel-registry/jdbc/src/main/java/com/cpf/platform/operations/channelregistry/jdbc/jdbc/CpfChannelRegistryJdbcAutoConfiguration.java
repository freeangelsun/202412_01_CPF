package com.cpf.platform.operations.channelregistry.jdbc.jdbc;
import com.cpf.platform.operations.channelregistry.adapter.JdbcCpfChannelRegistryAdapter;
import com.cpf.platform.operations.channelregistry.api.CpfChannelRegistryPort;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
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
