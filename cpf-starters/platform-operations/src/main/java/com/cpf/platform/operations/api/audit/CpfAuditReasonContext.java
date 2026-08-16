package com.cpf.platform.operations.api.audit;

import java.util.ArrayDeque;

/**
 * 승인/위험 조치의 사유를 lexical scope로 전달합니다.
 * try-with-resources로 사용하며 nested scope 종료 시 이전 사유를 복원합니다.
 */
public final class CpfAuditReasonContext {
    private static final ThreadLocal<ArrayDeque<String>> REASONS = ThreadLocal.withInitial(ArrayDeque::new);
    private CpfAuditReasonContext() {}
    public static String current() { return REASONS.get().peek(); }
    public static AutoCloseable bind(String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("audit reason is required");
        String normalized = reason.trim();
        var stack = REASONS.get(); stack.push(normalized);
        return () -> {
            if (stack.isEmpty() || !normalized.equals(stack.peek())) throw new IllegalStateException("CPF audit reason scope order violated");
            stack.pop(); if (stack.isEmpty()) REASONS.remove();
        };
    }
}
