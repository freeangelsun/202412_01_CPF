package com.cpf.starter.runtime;

import com.cpf.core.api.context.CpfContexts;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;

/**
 * CPF Runtime이 현재 실행 중 실제 사용된 Starter/Capability를 자동 추적하는 lexical context입니다.
 * 업무개발자는 이 API를 호출하지 않습니다. Runtime AOP가 Catalog package metadata로 자동 바인딩합니다.
 *
 * <p>거래 단위 누적값은 SLF4J MDC에도 투영하여 Observability 모듈이 Base Runtime에 역의존하지 않고
 * 구조화 File/DB Log에 자동 포함할 수 있게 합니다. 새로운 transactionId가 관측되면 이전 거래 사용이력은
 * 자동 폐기되므로 Thread Pool 재사용에서도 다른 거래 메타데이터가 섞이지 않습니다.</p>
 */
public final class CpfCapabilityUsageContext {
    public static final String MDC_STARTERS = "cpf.used.starters";
    public static final String MDC_CAPABILITIES = "cpf.used.capabilities";
    public static final String MDC_PROVIDERS = "cpf.used.providers";
    public static final String MDC_OPERATIONS = "cpf.used.operations";

    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);
    private CpfCapabilityUsageContext() { }

    /** Controller/Message/Batch Boundary가 명시적으로 열 수 있는 거래 단위 수집 범위입니다. */
    public static AutoCloseable beginTransactionScope() {
        State state = stateForCurrentTransaction();
        if (state.boundaryDepth++ == 0) {
            state.history.clear();
            publish(state);
        }
        return () -> {
            State current = STATE.get();
            if (current.boundaryDepth > 0) current.boundaryDepth--;
            if (current.boundaryDepth == 0 && current.stack.isEmpty() && current.transactionId == null) clearAll();
        };
    }

    /** Runtime instrumentation 전용입니다. 개발자 수동 등록 용도가 아닙니다. */
    static AutoCloseable bind(CpfRuntimeCapabilityDescriptor descriptor, String operation) {
        if (descriptor == null || !descriptor.operatorVisible()) return () -> { };
        State state = stateForCurrentTransaction();
        Usage usage = new Usage(descriptor.starterArtifactId(), descriptor.capability(), descriptor.provider(), safe(operation));
        state.stack.push(usage);
        state.history.putIfAbsent(usage.key(), usage);
        publish(state);
        return () -> {
            State current = STATE.get();
            if (current.stack.isEmpty() || current.stack.pop() != usage) {
                clearAll();
                throw new IllegalStateException("CPF capability usage scope close order violated");
            }
            // 거래 ID가 존재하는 실행에서는 transaction boundary log가 읽을 때까지 history/MDC를 유지합니다.
            if (current.boundaryDepth == 0 && current.stack.isEmpty() && current.transactionId == null) clearAll();
            else publish(current);
        };
    }

    public static Usage current() { return STATE.get().stack.peek(); }

    /** 현재 거래에서 실제 사용된 Capability 목록입니다. 호출 순서를 유지하고 중복 호출은 압축합니다. */
    public static List<Usage> used() { return List.copyOf(stateForCurrentTransaction().history.values()); }

    public static String starterIdsCsv() { return join(Usage::starterId); }
    public static String capabilityIdsCsv() { return join(Usage::capabilityId); }
    public static String providersCsv() { return join(Usage::provider); }
    public static String operationsCsv() { return join(u -> u.capabilityId() + ":" + u.operation()); }

    private static State stateForCurrentTransaction() {
        State state = STATE.get();
        String transactionId = normalize(CpfContexts.currentTransactionId());
        if (!java.util.Objects.equals(state.transactionId, transactionId)) {
            state.stack.clear();
            state.history.clear();
            state.boundaryDepth = 0;
            state.transactionId = transactionId;
            publish(state);
        }
        return state;
    }

    private static String join(java.util.function.Function<Usage,String> fn) {
        return stateForCurrentTransaction().history.values().stream().map(fn)
                .filter(v -> v != null && !v.isBlank()).distinct()
                .collect(java.util.stream.Collectors.joining(","));
    }

    private static void publish(State state) {
        putOrRemove(MDC_STARTERS, joinState(state, Usage::starterId));
        putOrRemove(MDC_CAPABILITIES, joinState(state, Usage::capabilityId));
        putOrRemove(MDC_PROVIDERS, joinState(state, Usage::provider));
        putOrRemove(MDC_OPERATIONS, joinState(state, u -> u.capabilityId() + ":" + u.operation()));
    }

    private static String joinState(State state, java.util.function.Function<Usage,String> fn) {
        return state.history.values().stream().map(fn).filter(v -> v != null && !v.isBlank()).distinct()
                .collect(java.util.stream.Collectors.joining(","));
    }

    private static void putOrRemove(String key, String value) {
        if (value == null || value.isBlank()) MDC.remove(key); else MDC.put(key, value);
    }

    private static void clearAll() {
        STATE.remove();
        MDC.remove(MDC_STARTERS);
        MDC.remove(MDC_CAPABILITIES);
        MDC.remove(MDC_PROVIDERS);
        MDC.remove(MDC_OPERATIONS);
    }

    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String safe(String value) { return value == null || value.isBlank() ? "unknown" : value.trim(); }

    public record Usage(String starterId, String capabilityId, String provider, String operation) {
        private String key() { return starterId + '|' + capabilityId + '|' + provider + '|' + operation; }
    }
    private static final class State {
        final ArrayDeque<Usage> stack = new ArrayDeque<>();
        final Map<String,Usage> history = new LinkedHashMap<>();
        String transactionId;
        int boundaryDepth;
    }
}
