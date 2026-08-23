package com.cpf.batch.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("cpf.batch.execution")
public record CpfBatchExecutionProperties(
        int defaultChunkSize,
        int maxPartitionCount,
        long remotePollIntervalMs,
        long remoteTimeoutMs,
        int remoteChunkMaxWaitTimeouts,
        long remoteChunkThrottleLimit,
        int maxMaterializedJobs,
        int executorCoreSize,
        int executorMaxSize,
        int executorQueueCapacity) {
    @ConstructorBinding
    public CpfBatchExecutionProperties {
        defaultChunkSize = bounded(defaultChunkSize, 100, 1, 10_000, "defaultChunkSize");
        maxPartitionCount = bounded(maxPartitionCount, 256, 1, 10_000, "maxPartitionCount");
        remotePollIntervalMs = bounded(remotePollIntervalMs, 1_000L, 10L, 60_000L, "remotePollIntervalMs");
        remoteTimeoutMs = bounded(remoteTimeoutMs, 3_600_000L, 1_000L, 86_400_000L, "remoteTimeoutMs");
        remoteChunkMaxWaitTimeouts = bounded(remoteChunkMaxWaitTimeouts, 40, 1, 10_000, "remoteChunkMaxWaitTimeouts");
        remoteChunkThrottleLimit = bounded(remoteChunkThrottleLimit, 256L, 1L, 10_000L, "remoteChunkThrottleLimit");
        maxMaterializedJobs = bounded(maxMaterializedJobs, 1_024, 16, 100_000, "maxMaterializedJobs");
        executorCoreSize = bounded(executorCoreSize, 4, 1, 512, "executorCoreSize");
        executorMaxSize = bounded(executorMaxSize, 32, executorCoreSize, 2_048, "executorMaxSize");
        executorQueueCapacity = bounded(executorQueueCapacity, 1_024, 1, 1_000_000, "executorQueueCapacity");
    }

    /** 이전 생성자 Source 호환용입니다. */
    public CpfBatchExecutionProperties(
            int defaultChunkSize,
            int maxPartitionCount,
            long remotePollIntervalMs,
            long remoteTimeoutMs,
            int remoteChunkMaxWaitTimeouts,
            long remoteChunkThrottleLimit) {
        this(defaultChunkSize, maxPartitionCount, remotePollIntervalMs, remoteTimeoutMs,
                remoteChunkMaxWaitTimeouts, remoteChunkThrottleLimit, 1_024, 4, 32, 1_024);
    }

    private static int bounded(int value, int defaultValue, int minimum, int maximum, String field) {
        int result = value == 0 ? defaultValue : value;
        if (result < minimum || result > maximum) {
            throw new IllegalArgumentException(field + " must be between " + minimum + " and " + maximum);
        }
        return result;
    }

    private static long bounded(long value, long defaultValue, long minimum, long maximum, String field) {
        long result = value == 0 ? defaultValue : value;
        if (result < minimum || result > maximum) {
            throw new IllegalArgumentException(field + " must be between " + minimum + " and " + maximum);
        }
        return result;
    }
}
