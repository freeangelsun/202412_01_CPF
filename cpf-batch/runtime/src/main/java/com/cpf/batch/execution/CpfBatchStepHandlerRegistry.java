package com.cpf.batch.execution;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.List;

/** Spring Batch Step 안에서 실행할 유일한 Product Consumer Registry입니다. */
public final class CpfBatchStepHandlerRegistry {
    private final List<BatchStepHandler> handlers;

    public CpfBatchStepHandlerRegistry(List<BatchStepHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public BatchStepHandler required(BatchJobDefinition.ExecutorType type, String reference) {
        return handlers.stream()
                .filter(handler -> handler.supports(type, reference))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "BATCH_STEP_HANDLER_UNAVAILABLE:" + type + ":" + reference));
    }
}
