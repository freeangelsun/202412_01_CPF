package com.cpf.starter.fixedlength.runtimecontrol;

import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfFixedLengthRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfFixedLengthLayoutRegistry fixedLengthLayoutRegistry() {
        return new CpfFixedLengthLayoutRegistry();
    }

    @Bean(name = "cpfFixedLayoutRuntimeApplier")
    @ConditionalOnBean(CpfFixedLengthLayoutRegistry.class)
    @ConditionalOnMissingBean(name = "cpfFixedLayoutRuntimeApplier")
    CpfRuntimeChangeApplier fixedLayoutRuntimeApplier(
            CpfFixedLengthLayoutRegistry registry) {
        return new CpfFixedLayoutRuntimeApplier(registry);
    }

    @Bean(name = "cpfSchemaRegistryRuntimeApplier")
    @ConditionalOnBean(CpfFixedLengthLayoutRegistry.class)
    @ConditionalOnMissingBean(name = "cpfSchemaRegistryRuntimeApplier")
    CpfRuntimeChangeApplier schemaRegistryRuntimeApplier(
            CpfFixedLengthLayoutRegistry registry) {
        return new CpfSchemaRegistryRuntimeApplier(registry);
    }
}
