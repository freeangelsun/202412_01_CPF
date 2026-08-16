package com.cpf.platform.operations.health;
import com.cpf.platform.operations.api.health.*;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Runtime Health/Drain의 canonical AutoConfiguration입니다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfHealthProperties.class)
@ConditionalOnProperty(prefix="cpf.platform-operations.health",name="enabled",havingValue="true",matchIfMissing=true)
public class CpfHealthAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfDrainControl cpfDrainControl(){return new CpfDrainManager();}
    @Bean @ConditionalOnMissingBean CpfRuntimeHealthRegistry cpfRuntimeHealthRegistry(){return new CpfRuntimeHealthRegistryMemory();}
    @Bean @ConditionalOnMissingBean CpfRuntimeHealthService cpfRuntimeHealthService(CpfHealthProperties properties, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks){return new CpfRuntimeHealthService(properties.toConfig(),drain,checks);}
    @Bean @ConditionalOnMissingBean CpfHealthEndpoint cpfHealthEndpoint(CpfHealthSnapshotProvider provider){return new CpfHealthEndpoint(provider);}
    @Bean @ConditionalOnMissingBean CpfDrainWebFilter cpfDrainWebFilter(CpfDrainControl drain){return new CpfDrainWebFilter(drain);}
    @Bean(destroyMethod="close")
    @ConditionalOnClass(name="org.springframework.web.client.RestClient")
    @ConditionalOnProperty(prefix="cpf.platform-operations.health",name={"report-url","report-token"})
    CpfHealthReporter cpfHealthReporter(CpfHealthSnapshotProvider provider,CpfHealthProperties properties){return new CpfHealthReporter(provider,properties);}
}
