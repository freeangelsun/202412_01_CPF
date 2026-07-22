package com.cpf.batch.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/** CPF 실행 메타의 jobId와 파라미터를 Spring Batch JobOperator에 전달합니다. */
@Component
public class SpringBatchWorkerJobDispatcher implements BatWorkerJobDispatcher {
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final ObjectMapper objectMapper;

    public SpringBatchWorkerJobDispatcher(
            JobOperator jobOperator,
            JobExplorer jobExplorer,
            ObjectMapper objectMapper) {
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
        this.objectMapper = objectMapper;
    }

    @Override
    public DispatchResult dispatch(BatWorkerLease lease) {
        Long springExecutionId = null;
        try {
            Properties parameters = parameters(lease);
            springExecutionId = jobOperator.start(lease.jobId(), parameters);
            JobExecution execution = jobExplorer.getJobExecution(springExecutionId);
            if (execution == null) {
                return DispatchResult.failed(springExecutionId, "Spring Batch 실행 메타를 찾지 못했습니다.");
            }
            String status = execution.getStatus().name();
            return "COMPLETED".equals(status)
                    ? DispatchResult.completed(springExecutionId)
                    : DispatchResult.failed(springExecutionId, "Spring Batch 종료 상태=" + status);
        } catch (Exception ex) {
            return DispatchResult.failed(springExecutionId, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private Properties parameters(BatWorkerLease lease) throws Exception {
        Properties properties = new Properties();
        if (lease.jobParameters() != null && !lease.jobParameters().isBlank()) {
            Map<String, Object> values = objectMapper.readValue(
                    lease.jobParameters(), new TypeReference<Map<String, Object>>() { });
            values.forEach((key, value) -> {
                if (key != null && value != null) {
                    properties.setProperty(key, String.valueOf(value));
                }
            });
        }
        properties.setProperty("cpfCpfExecutionId", lease.executionId() + ",java.lang.Long,true");
        properties.setProperty("cpfWorkerLeaseToken", lease.leaseToken());
        properties.setProperty("serverInstanceId", lease.workerId());
        properties.putIfAbsent("run.id", System.currentTimeMillis() + ",java.lang.Long,true");
        return properties;
    }
}
