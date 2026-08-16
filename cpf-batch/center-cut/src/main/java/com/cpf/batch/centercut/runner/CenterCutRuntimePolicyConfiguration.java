package com.cpf.batch.centercut.runner;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Center-Cut Runner가 실제 소비하는 Runtime capability만 등록합니다. */
@Configuration
public class CenterCutRuntimePolicyConfiguration {
    @Bean(name = "batchCenterCutRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchCenterCutRuntimeApplier")
    CpfRuntimeChangeApplier batchCenterCutRuntimeApplier(BatchRuntimePolicy policy) {
        return new BatchRuntimePolicyApplier(BatchRuntimePolicyApplier.CENTER_CUT, policy);
    }
}
