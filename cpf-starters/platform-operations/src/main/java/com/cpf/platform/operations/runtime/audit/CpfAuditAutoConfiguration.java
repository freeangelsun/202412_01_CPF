package com.cpf.platform.operations.runtime.audit;

import com.cpf.platform.operations.api.audit.CpfAuditSink;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CpfAuditProperties.class)
public class CpfAuditAutoConfiguration {
    @Bean @ConditionalOnBean(CpfAuditSink.class)
    CpfAuditAspect cpfAuditAspect(CpfAuditSink sink,CpfAuditProperties properties){return new CpfAuditAspect(sink,properties,Clock.systemUTC());}
}
