package com.cpf.starter.internal.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.spi.context.CpfContextRuntimeProvider;
import java.util.ArrayDeque;

/**
 * Public Base Starter가 소유하는 기본 lexical Context 저장 Provider입니다.
 * Thread 재사용에서도 마지막 scope close 후 ThreadLocal을 제거하여 Context leak을 방지합니다.
 */
public final class CpfStarterContextRuntime implements CpfContextRuntimeProvider {
    private final ThreadLocal<ArrayDeque<CpfContext>> stack = ThreadLocal.withInitial(ArrayDeque::new);

    public CpfStarterContextRuntime() { }

    @Override public int priority() { return 100; }

    @Override public CpfContext current() {
        ArrayDeque<CpfContext> values = stack.get();
        return values.isEmpty() ? null : values.peek();
    }

    @Override public AutoCloseable bind(CpfContext context) {
        if (context == null) throw new IllegalArgumentException("context");
        ArrayDeque<CpfContext> values = stack.get();
        values.push(context);
        return new Scope(values, context);
    }

    private final class Scope implements AutoCloseable {
        private final ArrayDeque<CpfContext> values;
        private final CpfContext expected;
        private boolean closed;
        private Scope(ArrayDeque<CpfContext> values, CpfContext expected) {
            this.values = values;
            this.expected = expected;
        }
        @Override public void close() {
            if (closed) return;
            closed = true;
            if (values.isEmpty() || values.pop() != expected) {
                stack.remove();
                throw new IllegalStateException("CPF context scope close order violated");
            }
            if (values.isEmpty()) stack.remove();
        }
    }
}
