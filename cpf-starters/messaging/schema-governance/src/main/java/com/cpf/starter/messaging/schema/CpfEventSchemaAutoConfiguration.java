package com.cpf.starter.messaging.schema;
import com.cpf.core.api.reliability.CpfEventSchemaRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@ConditionalOnProperty(prefix="cpf.messaging.schema", name="enabled", havingValue="true")
public class CpfEventSchemaAutoConfiguration {
    @Bean @ConditionalOnMissingBean(CpfEventSchemaRegistry.class)
    CpfEventSchemaRegistry cpfEventSchemaRegistry() { return new CpfInMemoryEventSchemaRegistry(); }
}
