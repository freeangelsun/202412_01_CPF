package com.cpf.batch.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("cpf.batch.execution")
public record CpfBatchExecutionProperties(
        int defaultChunkSize,
        int maxPartitionCount,
        int maxMaterializedJobs,
        int executorCoreSize,
        int executorMaxSize,
        int executorQueueCapacity) {
    @ConstructorBinding
    public CpfBatchExecutionProperties {
        defaultChunkSize = bounded(defaultChunkSize, 100, 1, 10_000, "defaultChunkSize");
        maxPartitionCount = bounded(maxPartitionCount, 256, 1, 10_000, "maxPartitionCount");
        maxMaterializedJobs = bounded(maxMaterializedJobs, 1_024, 16, 100_000, "maxMaterializedJobs");
        executorCoreSize = bounded(executorCoreSize, 4, 1, 512, "executorCoreSize");
        executorMaxSize = bounded(executorMaxSize, 32, executorCoreSize, 2_048, "executorMaxSize");
        executorQueueCapacity = bounded(executorQueueCapacity, 1_024, 1, 1_000_000, "executorQueueCapacity");
    }

    private static int bounded(int value, int defaultValue, int minimum, int maximum, String field) {
        int result = value == 0 ? defaultValue : value;
        if (result < minimum || result > maximum) {
            throw new IllegalArgumentException(field + " must be between " + minimum + " and " + maximum);
        }
        return result;
    }
}
