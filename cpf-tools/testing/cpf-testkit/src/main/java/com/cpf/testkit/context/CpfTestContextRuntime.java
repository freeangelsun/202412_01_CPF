package com.cpf.testkit.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.spi.context.CpfContextRuntimeProvider;
import java.util.ArrayDeque;

/**
 * Testkit Classpath에서 Base Provider보다 우선되는 deterministic Context Provider입니다.
 * install 호출은 legacy test ergonomics를 보존하지만 Core Runtime을 동적으로 교체하지 않습니다.
 */
public final class CpfTestContextRuntime implements CpfContextRuntimeProvider, AutoCloseable {
    private static final ThreadLocal<ArrayDeque<CpfContext>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    public CpfTestContextRuntime() { }
    public static CpfTestContextRuntime install() { return new CpfTestContextRuntime(); }
    @Override public int priority() { return 1000; }
    @Override public CpfContext current() { var values = STACK.get(); return values.isEmpty() ? null : values.peek(); }
    @Override public AutoCloseable bind(CpfContext context) {
        if (context == null) throw new IllegalArgumentException("context");
        var values = STACK.get();
        values.push(context);
        return () -> {
            var current = STACK.get();
            if (current.isEmpty() || current.pop() != context) {
                STACK.remove();
                throw new IllegalStateException("CPF test context close order violated");
            }
            if (current.isEmpty()) STACK.remove();
        };
    }
    @Override public void close() {
        var values = STACK.get();
        if (!values.isEmpty()) throw new IllegalStateException("CPF test context leak detected");
        STACK.remove();
    }
}
