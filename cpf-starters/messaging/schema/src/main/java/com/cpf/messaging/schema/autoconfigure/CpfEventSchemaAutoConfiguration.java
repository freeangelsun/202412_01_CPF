package com.cpf.messaging.schema.autoconfigure;

import com.cpf.messaging.schema.api.CpfEventSchemaRegistry;
import com.cpf.messaging.schema.runtime.CpfInMemoryEventSchemaRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** CPF 이벤트 스키마 Registry를 선택적으로 활성화한다. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cpf.messaging.schema", name = "enabled", havingValue = "true")
public class CpfEventSchemaAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfEventSchemaRegistry.class)
    CpfEventSchemaRegistry cpfEventSchemaRegistry() {
        return new CpfInMemoryEventSchemaRegistry();
    }
}
