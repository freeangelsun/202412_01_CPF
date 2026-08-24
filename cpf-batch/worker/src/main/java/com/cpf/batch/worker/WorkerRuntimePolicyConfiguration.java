package com.cpf.batch.worker;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Applies worker capacity policy to Spring Batch handler execution. */
@Configuration
public class WorkerRuntimePolicyConfiguration {
    @Bean(name = "batchConcurrencyRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchConcurrencyRuntimeApplier")
    CpfRuntimeChangeApplier batchConcurrencyRuntimeApplier(
            BatchRuntimePolicy policy,
            SpringBatchWorkerRuntimeState runtime) {
        BatchRuntimePolicyApplier delegate = new BatchRuntimePolicyApplier(
                BatchRuntimePolicyApplier.CONCURRENCY, policy);
        return new CpfRuntimeChangeApplier() {
            @Override
            public String changeType() {
                return delegate.changeType();
            }

            @Override
            public boolean supportsIdempotentReplay() {
                return delegate.supportsIdempotentReplay();
            }

            @Override
            public boolean snapshotCapable() {
                return delegate.snapshotCapable();
            }

            @Override
            public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
                CpfRuntimeApplyResult result = delegate.apply(delivery);
                if (result.applied()) {
                    runtime.reconcile();
                }
                return result;
            }
        };
    }

}
