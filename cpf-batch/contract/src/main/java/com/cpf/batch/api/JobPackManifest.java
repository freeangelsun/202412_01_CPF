package com.cpf.batch.api;

import java.util.List;
import java.util.Map;

/** Domain 소유 Job Pack의 서명/호환성/실행 계약. */
public record JobPackManifest(
        String jobPackId,
        String ownerDomain,
        String artifactCoordinate,
        String version,
        String checksum,
        String signatureBase64,
        String requiredPlatformRange,
        List<String> capabilities,
        List<JobDefinition> jobs,
        Map<String, String> metadata
) {
    public JobPackManifest {
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        jobs = jobs == null ? List.of() : List.copyOf(jobs);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public record JobDefinition(
            String jobId,
            String jobName,
            boolean restartable,
            List<BatchParameterDefinition> parameters,
            String centerCutProviderKey,
            String centerCutHandlerKey,
            BatchExecutorType executorType,
            String executorKey,
            List<String> dependsOn,
            BatchExecutionPolicy executionPolicy
    ) {
        public JobDefinition {
            if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId is required");
            parameters = parameters == null ? List.of() : List.copyOf(parameters);
            executorType = executorType == null ? BatchExecutorType.SPRING_BATCH : executorType;
            dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
            executionPolicy = executionPolicy == null ? BatchExecutionPolicy.defaults() : executionPolicy;
            if (executorType != BatchExecutorType.SPRING_BATCH && (executorKey == null || executorKey.isBlank())) {
                throw new IllegalArgumentException("executorKey is required for " + executorType);
            }
        }

        /** R15 이전 Job Pack Source 호환 생성자. */
        public JobDefinition(String jobId, String jobName, boolean restartable,
                             List<BatchParameterDefinition> parameters,
                             String centerCutProviderKey, String centerCutHandlerKey) {
            this(jobId, jobName, restartable, parameters, centerCutProviderKey, centerCutHandlerKey,
                    BatchExecutorType.SPRING_BATCH, null, List.of(), BatchExecutionPolicy.defaults());
        }
    }
}
