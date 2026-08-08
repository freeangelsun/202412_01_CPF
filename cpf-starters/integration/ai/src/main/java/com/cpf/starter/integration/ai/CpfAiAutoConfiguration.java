package com.cpf.starter.integration.ai;
import com.cpf.core.api.ai.*; import java.util.List; import org.springframework.boot.autoconfigure.AutoConfiguration; import org.springframework.boot.autoconfigure.condition.*; import org.springframework.boot.context.properties.EnableConfigurationProperties; import org.springframework.context.annotation.Bean;
@AutoConfiguration @EnableConfigurationProperties(CpfAiProperties.class) @ConditionalOnProperty(prefix="cpf.integration.ai",name="enabled",havingValue="true")
public class CpfAiAutoConfiguration { @Bean @ConditionalOnMissingBean(CpfAiOperations.class) CpfAiOperations cpfAiOperations(List<CpfAiProvider> providers,CpfAiPolicy policy,CpfAiProperties props){return new CpfAiRouter(providers,policy,props);} }
