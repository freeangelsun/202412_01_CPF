package com.cpf.integration.fixedlength.runtimecontrol;

import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Fixed-length integration capability를 CPF Runtime 운영 제어/상태 관리 체계에 등록합니다.
 * <p>전문 codec의 업무 사용법과 분리된 운영 자동구성이며, capability가 선택되지 않으면 활성화되지 않습니다.
 */
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
