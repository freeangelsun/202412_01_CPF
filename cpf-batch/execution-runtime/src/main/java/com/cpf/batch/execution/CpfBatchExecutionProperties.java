package com.cpf.batch.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.batch.execution")
public record CpfBatchExecutionProperties(
        int defaultChunkSize,
        int maxPartitionCount,
        long remotePollIntervalMs,
        long remoteTimeoutMs,
        int remoteChunkMaxWaitTimeouts,
        long remoteChunkThrottleLimit) {
    public CpfBatchExecutionProperties {
        defaultChunkSize = defaultChunkSize <= 0 ? 100 : defaultChunkSize;
        maxPartitionCount = maxPartitionCount <= 0 ? 256 : maxPartitionCount;
        remotePollIntervalMs = remotePollIntervalMs <= 0 ? 1_000L : remotePollIntervalMs;
        remoteTimeoutMs = remoteTimeoutMs <= 0 ? 3_600_000L : remoteTimeoutMs;
        remoteChunkMaxWaitTimeouts = remoteChunkMaxWaitTimeouts <= 0 ? 40 : remoteChunkMaxWaitTimeouts;
        remoteChunkThrottleLimit = remoteChunkThrottleLimit <= 0 ? 256 : remoteChunkThrottleLimit;
    }
}
