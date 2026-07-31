package com.cpf.batch.spi;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import java.util.Map;

/** File/Shell/API 업무 로직을 Spring Batch Tasklet/Chunk 안에서 실행하는 확장 SPI입니다. */
public interface BatchStepHandler {
    boolean supports(BatchJobDefinition.ExecutorType executorType, String executorReference);
    BatchStepResult execute(BatchStepCommand command) throws Exception;

    record BatchStepCommand(
            String cpfExecutionId,
            long jobExecutionId,
            long stepExecutionId,
            long fencingToken,
            BatchStepDefinition step,
            Map<String, Object> jobParameters,
            Map<String, Object> executionContext) {
        public BatchStepCommand {
            jobParameters = jobParameters == null ? Map.of() : Map.copyOf(jobParameters);
            executionContext = executionContext == null ? Map.of() : Map.copyOf(executionContext);
        }
    }

    record BatchStepResult(Status status, String code, String message, long readCount, long writeCount, long skipCount, Map<String,Object> checkpoint) {
        public BatchStepResult {
            if (status == null) throw new IllegalArgumentException("status is required.");
            code = code == null ? "" : code;
            message = message == null ? "" : message;
            checkpoint = checkpoint == null ? Map.of() : Map.copyOf(checkpoint);
        }
        public static BatchStepResult completed(String message, long read, long write, Map<String,Object> checkpoint) {
            return new BatchStepResult(Status.COMPLETED, "", message, read, write, 0, checkpoint);
        }
    }
    enum Status { COMPLETED, FAILED, RETRYABLE_FAILURE, UNKNOWN_RESULT, STOPPED }
}
