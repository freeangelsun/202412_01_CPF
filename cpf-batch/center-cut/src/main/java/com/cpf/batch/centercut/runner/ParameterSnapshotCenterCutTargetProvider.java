package com.cpf.batch.centercut.runner;

import com.cpf.batch.spi.CenterCutTargetProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * DB에 암호화·고정된 Center-Cut 실행 Parameter Snapshot에서 업무 대상을 페이지 단위로 생성합니다.
 *
 * <p>각 대상은 공통 SystemCode/OperationId와 대상별 실제 업무 요청을 결합합니다. 생성된 JSON은
 * 이후 DB Work Item에 영속화되며, Generator 내부 예제나 메모리 전용 가짜 대상이 아닙니다.</p>
 */
@Component
public final class ParameterSnapshotCenterCutTargetProvider implements CenterCutTargetProvider {
    public static final String PROVIDER_KEY = "cpfParameterSnapshotCenterCutTargetProvider";
    private static final int MAX_TARGETS = 100_000;
    private static final String CURSOR_PREFIX = "offset:";

    private final ObjectMapper mapper;

    public ParameterSnapshotCenterCutTargetProvider(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public String providerKey() {
        return PROVIDER_KEY;
    }

    @Override
    public List<Target> next(
            String jobId,
            String snapshotId,
            String cursor,
            int limit,
            Map<String, Object> parameters) {
        if (limit < 1) throw new IllegalArgumentException("Center-Cut target limit must be positive");
        Map<String, Object> source = parameters == null ? Map.of() : parameters;
        String systemCode = requiredCode(source.get("systemCode"), "systemCode");
        String operationId = requiredCode(source.get("operationId"), "operationId");
        List<?> targets = requiredTargets(source.get("targets"));
        int offset = parseCursor(cursor, targets.size());
        int end = Math.min(targets.size(), Math.addExact(offset, limit));
        List<Target> page = new ArrayList<>(end - offset);
        for (int index = offset; index < end; index++) {
            Map<String, Object> target = objectMap(targets.get(index), "targets[" + index + "]");
            String businessKey = requiredText(target.get("businessKey"), "businessKey");
            Map<String, Object> request = objectMap(target.get("request"), "request");
            Map<String, Object> invocation = new LinkedHashMap<>();
            invocation.put("systemCode", systemCode);
            invocation.put("operationId", operationId);
            invocation.put("request", request);
            String payload;
            try {
                payload = mapper.writeValueAsString(invocation);
            } catch (Exception failure) {
                throw new IllegalArgumentException("Center-Cut target request is not JSON serializable", failure);
            }
            int next = index + 1;
            page.add(new Target(businessKey, CURSOR_PREFIX + next, payload, next == targets.size()));
        }
        return List.copyOf(page);
    }

    private static List<?> requiredTargets(Object value) {
        if (!(value instanceof List<?> targets)) {
            throw new IllegalArgumentException("Center-Cut targets must be a JSON array");
        }
        if (targets.isEmpty()) throw new IllegalArgumentException("Center-Cut targets must not be empty");
        if (targets.size() > MAX_TARGETS) throw new IllegalArgumentException("Center-Cut targets exceed the maximum");
        return targets;
    }

    private static int parseCursor(String cursor, int size) {
        if (cursor == null || cursor.isBlank()) return 0;
        if (!cursor.startsWith(CURSOR_PREFIX)) throw new IllegalArgumentException("Invalid Center-Cut target cursor");
        try {
            int offset = Integer.parseInt(cursor.substring(CURSOR_PREFIX.length()));
            if (offset < 0 || offset > size) throw new IllegalArgumentException("Invalid Center-Cut target cursor");
            return offset;
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("Invalid Center-Cut target cursor", failure);
        }
    }

    private static Map<String, Object> objectMap(Object value, String name) {
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException(name + " must be a JSON object");
        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
        map.forEach((key, item) -> {
            if (!(key instanceof String text) || text.isBlank()) {
                throw new IllegalArgumentException(name + " contains an invalid key");
            }
            normalized.put(text, item);
        });
        return Map.copyOf(normalized);
    }

    private static String requiredCode(Object value, String name) {
        String text = requiredText(value, name);
        if (!text.matches("[A-Za-z][A-Za-z0-9._:-]{0,119}")) {
            throw new IllegalArgumentException("Invalid Center-Cut " + name);
        }
        return text;
    }

    private static String requiredText(Object value, String name) {
        String text = Objects.toString(value, "").trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Center-Cut " + name + " is required");
        return text;
    }
}
