package com.cpf.starter.platform.operations.health;

import com.cpf.core.api.health.CpfDrainControl;
import com.cpf.core.api.health.CpfHealthSnapshotProvider;
import java.util.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(CpfHealthProperties.class)
@ConditionalOnProperty(prefix="cpf.platform.health",name="enabled",havingValue="true",matchIfMissing=true)
public class CpfHealthAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfDrainControl cpfDrainControl(){return new CpfDrainManager();}
    @Bean @ConditionalOnMissingBean CpfHealthSnapshotProvider cpfRuntimeHealthService(CpfHealthProperties p,CpfDrainControl d,List<CpfDependencyHealthCheck> checks){return new CpfRuntimeHealthService(p,d,checks);}
    @Bean @ConditionalOnMissingBean CpfDrainAuditSink cpfDrainAuditSink(){return new Slf4jCpfDrainAuditSink();}
    @Bean @ConditionalOnClass(Endpoint.class) CpfHealthEndpoint cpfHealthEndpoint(CpfHealthSnapshotProvider p,CpfDrainControl d,List<CpfDrainAuditSink> sinks){return new CpfHealthEndpoint(p,d,sinks);}
    @Bean @ConditionalOnClass(name="jakarta.servlet.Filter") @ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET) CpfDrainWebFilter cpfDrainWebFilter(CpfDrainControl d){return new CpfDrainWebFilter(d);}
    @Bean(destroyMethod="close") @ConditionalOnClass(RestClient.class)
    @ConditionalOnProperty(prefix="cpf.platform.health",name={"report-url","report-token"})
    CpfHealthReporter cpfHealthReporter(CpfHealthSnapshotProvider p,CpfHealthProperties props){return new CpfHealthReporter(p,props);}
}
