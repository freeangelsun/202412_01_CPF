package com.cpf.batch.execution;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/** Remote Worker에 전달되는 Step 정의를 ExecutionContext에 저장·복원합니다. */
final class CpfRemoteStepDefinition {
    private CpfRemoteStepDefinition() { }

    static void write(ExecutionContext context, BatchStepDefinition step) {
        context.putString("cpf.step.id", step.stepId());
        context.putString("cpf.step.executorType", step.executorType().name());
        context.putString("cpf.step.executorReference", step.executorReference());
        context.putInt("cpf.step.partitionCount", step.partitionCount());
        context.putString("cpf.step.nextOnSuccess", step.nextOnSuccess());
        context.putString("cpf.step.nextOnFailure", step.nextOnFailure());
        context.put("cpf.step.parameters", new LinkedHashMap<>(step.parameters()));
        context.put("cpf.step.restartable", step.restartable());
    }

    @SuppressWarnings("unchecked")
    static BatchStepDefinition read(ExecutionContext context) {
        Object raw = context.get("cpf.step.parameters");
        Map<String, Object> parameters = raw instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()), Map.Entry::getValue,
                        (left, right) -> right, LinkedHashMap::new))
                : Map.of();
        return new BatchStepDefinition(
                required(context, "cpf.step.id"),
                BatchJobDefinition.ExecutorType.valueOf(required(context, "cpf.step.executorType")),
                required(context, "cpf.step.executorReference"), parameters,
                context.containsKey("cpf.step.partitionCount") ? context.getInt("cpf.step.partitionCount") : 1,
                value(context, "cpf.step.nextOnSuccess"), value(context, "cpf.step.nextOnFailure"),
                !context.containsKey("cpf.step.restartable") || Boolean.TRUE.equals(context.get("cpf.step.restartable")));
    }

    private static String required(ExecutionContext context, String key) {
        String value = context.getString(key);
        if (value == null || value.isBlank()) throw new IllegalStateException(key + " is missing");
        return value;
    }
    private static String value(ExecutionContext context, String key) {
        String value = context.getString(key);
        return value == null ? "" : value;
    }
}
