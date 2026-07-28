package com.cpf.batch.worker;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Worker instance가 실제 소비하는 concurrency Runtime capability만 등록합니다. */
@Configuration
public class WorkerRuntimePolicyConfiguration {
    @Bean(name = "batchConcurrencyRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchConcurrencyRuntimeApplier")
    CpfRuntimeChangeApplier batchConcurrencyRuntimeApplier(BatchRuntimePolicy policy) {
        return new BatchRuntimePolicyApplier(BatchRuntimePolicyApplier.CONCURRENCY, policy);
    }
}
