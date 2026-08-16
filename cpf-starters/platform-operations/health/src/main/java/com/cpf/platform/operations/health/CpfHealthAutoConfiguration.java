package com.cpf.platform.operations.health;
import com.cpf.platform.operations.api.health.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import com.cpf.starter.runtime.CpfRuntimeCapabilityInventory;
import org.springframework.core.env.Environment;
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
    @Bean @ConditionalOnMissingBean CpfRuntimeHealthService cpfRuntimeHealthService(CpfHealthProperties properties, CpfDrainControl drain,
            List<CpfDependencyHealthCheck> checks, CpfRuntimeCapabilityInventory inventory, Environment environment){
        properties.applyRuntimeIdentity(environment);
        Map<String,String> identity=new LinkedHashMap<>();
        put(identity,"environment",first(environment,"cpf.environment","spring.profiles.active","CPF_ENVIRONMENT"));
        put(identity,"systemCode",first(environment,"cpf.system-code","cpf.system.id",properties.getSystemId()));
        put(identity,"systemId",properties.getSystemId());
        put(identity,"domainCode",first(environment,"cpf.domain-code","cpf.domain.code","cpf.generated-domain.system-code"));
        put(identity,"domainId",first(environment,"cpf.domain-id","cpf.domain.id"));
        put(identity,"application",first(environment,"spring.application.name",properties.getSystemId()));
        put(identity,"module",first(environment,"cpf.module","cpf.runtime.role","spring.application.name"));
        put(identity,"host",first(environment,"cpf.host","HOSTNAME","COMPUTERNAME"));
        put(identity,"instanceId",properties.getInstanceId());
        return new CpfRuntimeHealthService(properties.toConfig(),drain,checks,inventory,identity);
    }
    private static String first(Environment env,String... keysOrFallback){
        for(String key:keysOrFallback){String v=key.contains(".")||key.contains("-")?env.getProperty(key):key;if(v!=null&&!v.isBlank())return v.trim();}return null;
    }
    private static void put(Map<String,String> target,String key,String value){if(value!=null&&!value.isBlank())target.put(key,value);}

    @Bean @ConditionalOnMissingBean CpfHealthEndpoint cpfHealthEndpoint(CpfHealthSnapshotProvider provider){return new CpfHealthEndpoint(provider);}
    @Bean @ConditionalOnMissingBean CpfDrainWebFilter cpfDrainWebFilter(CpfDrainControl drain){return new CpfDrainWebFilter(drain);}
    @Bean(destroyMethod="close")
    @ConditionalOnClass(name="org.springframework.web.client.RestClient")
    @ConditionalOnProperty(prefix="cpf.platform-operations.health",name={"report-url","report-token"})
    CpfHealthReporter cpfHealthReporter(CpfHealthSnapshotProvider provider,CpfHealthProperties properties){return new CpfHealthReporter(provider,properties);}
}
