package com.cpf.batch.control.centercut;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.api.CpfCenterCutOperations;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** CENTER_CUT Job Definition을 Control Plane의 공식 Center-Cut 실행 생성 API에 연결합니다. */
@Component
public final class CenterCutBatchStepHandler implements BatchStepHandler {
    private static final String PREFIX = "CENTER_CUT:";
    private final CpfCenterCutOperations operations;

    public CenterCutBatchStepHandler(CpfCenterCutOperations operations) {
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public boolean supports(BatchJobDefinition.ExecutorType type, String reference) {
        return type == BatchJobDefinition.ExecutorType.CENTER_CUT
                && reference != null && reference.startsWith(PREFIX);
    }

    @Override
    public BatchStepResult execute(BatchStepCommand command) throws Exception {
        String centerCutJobId = command.step().executorReference().substring(PREFIX.length()).trim();
        if (centerCutJobId.isEmpty()) throw new IllegalArgumentException("CENTER_CUT_JOB_ID_REQUIRED");
        Map<String,Object> parameters = businessParameters(command);
        String operator = text(parameters.remove("operatorId"), "cpf-batch");
        String reason = text(parameters.remove("reason"), "Batch Job Definition Center-Cut 실행");
        int tps = integer(parameters.remove("tpsLimit"), 0, 0);
        int concurrency = integer(parameters.remove("concurrencyLimit"), 1, 1);
        String schemaVersion = text(parameters.remove("parameterSchemaVersion"), "1");
        CenterCutExecutionRequest request = new CenterCutExecutionRequest(
                centerCutJobId, command.cpfExecutionId() + ":" + command.stepExecutionId(),
                parameters, schemaVersion, tps, concurrency, operator, reason);
        Map<String,Object> result = operations.launch(request);
        String executionId = Objects.toString(result.getOrDefault("execution_id", result.get("executionId")), "");
        return BatchStepResult.completed("CENTER_CUT_EXECUTION_CREATED", 1, 1,
                Map.of("centerCutJobId", centerCutJobId, "centerCutExecutionId", executionId));
    }

    private static Map<String,Object> businessParameters(BatchStepCommand command) {
        LinkedHashMap<String,Object> values = new LinkedHashMap<>();
        command.jobParameters().forEach((key,value) -> { if (key.startsWith("arg.")) values.put(key.substring(4), value); });
        values.putAll(command.step().parameters());
        return values;
    }

    private static String text(Object value, String fallback) {
        String text = Objects.toString(value, "").trim();
        return text.isEmpty() ? fallback : text;
    }

    private static int integer(Object value, int fallback, int minimum) {
        if (value == null || Objects.toString(value, "").isBlank()) return fallback;
        try {
            int parsed = Integer.parseInt(Objects.toString(value).trim());
            if (parsed < minimum) throw new IllegalArgumentException("CENTER_CUT_POLICY_INVALID");
            return parsed;
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("CENTER_CUT_POLICY_INVALID", failure);
        }
    }
}
