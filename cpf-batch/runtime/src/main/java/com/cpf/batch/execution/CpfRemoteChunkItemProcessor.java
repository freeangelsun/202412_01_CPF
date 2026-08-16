package com.cpf.batch.execution;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Remote Chunk Processor는 입력 계약 검증과 정규화만 담당합니다.
 * 업무 Side Effect는 {@link CpfRemoteChunkItemWriter}의 Writer Transaction에서 실행합니다.
 */
public final class CpfRemoteChunkItemProcessor
        implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> process(Map<String, Object> item) {
        if (item == null) throw new IllegalArgumentException("BATCH_REMOTE_CHUNK_ITEM_REQUIRED");
        BatchStepDefinition definition = definition(item);
        Map<String, Object> normalized = new LinkedHashMap<>(item);
        normalized.put("stepId", definition.stepId());
        normalized.put("executorType", definition.executorType().name());
        normalized.put("executorReference", definition.executorReference());
        normalized.put("parameters", definition.parameters());
        normalized.put("partitionCount", definition.partitionCount());
        normalized.put("restartable", definition.restartable());
        return Map.copyOf(normalized);
    }

    @SuppressWarnings("unchecked")
    static BatchStepDefinition definition(Map<String, Object> item) {
        Object raw = item.get("parameters");
        Map<String, Object> stepParameters = raw instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> right,
                        LinkedHashMap::new))
                : Map.of();
        return new BatchStepDefinition(
                required(String.valueOf(item.get("stepId")), "stepId"),
                BatchJobDefinition.ExecutorType.valueOf(
                        required(String.valueOf(item.get("executorType")), "executorType")),
                required(String.valueOf(item.get("executorReference")), "executorReference"),
                stepParameters,
                requiredPositiveInt(item.get("partitionCount"), "partitionCount"),
                "",
                "",
                !Boolean.FALSE.equals(item.get("restartable")));
    }

    static String required(String value, String name) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            throw new IllegalStateException(name + " is missing");
        }
        return value;
    }

    static long required(Long value, String name) {
        if (value == null || value <= 0) throw new IllegalStateException(name + " is missing");
        return value;
    }

    private static int requiredPositiveInt(Object value, String name) {
        if (!(value instanceof Number number) || number.intValue() <= 0) {
            throw new IllegalStateException(name + " must be a positive number");
        }
        return number.intValue();
    }
}
