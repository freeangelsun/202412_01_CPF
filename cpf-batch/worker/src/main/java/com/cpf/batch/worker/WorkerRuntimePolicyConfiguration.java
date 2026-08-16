package com.cpf.batch.worker;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/** Applies worker capacity policy to both Kafka admission and Spring Batch handler execution. */
@Configuration
public class WorkerRuntimePolicyConfiguration {
    private static final String WORKER_CONTAINER_FACTORY = "cpfBatchKafkaManualAckContainerFactory";

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

    /**
     * Configures the worker Kafka consumer pool before listener containers are created. Dynamic
     * policy reductions are additionally enforced by {@link WorkerExecutionTracker}.
     */
    @Bean
    static BeanPostProcessor cpfBatchWorkerKafkaConcurrencyCustomizer(Environment environment) {
        int configured = environment.getProperty(
                "cpf.batch.worker.max-concurrency", Integer.class, 1);
        if (configured < 1 || configured > BatchRuntimePolicy.MAX_CONCURRENCY) {
            throw new IllegalArgumentException("Worker max concurrency is out of range");
        }
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (WORKER_CONTAINER_FACTORY.equals(beanName)
                        && bean instanceof ConcurrentKafkaListenerContainerFactory<?, ?> factory) {
                    factory.setConcurrency(configured);
                }
                return bean;
            }
        };
    }
}
