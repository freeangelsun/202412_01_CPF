package com.cpf.integration.fixedlength;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.util.Map;
import java.util.Objects;

/**
 * Fixed-length codec 호출을 CPF child execution으로 감싸는 Integration Owner helper입니다.
 * Payload/레이아웃 의미는 순수 {@link CpfFixedLengthCodec}가 소유하고 이 클래스는 Context lifecycle만 담당합니다.
 */
public final class CpfContextFixedLengthCodec {
    private final CpfFixedLengthCodec delegate;
    private final CpfContextExecutionFactory contextFactory;

    public CpfContextFixedLengthCodec(CpfFixedLengthCodec delegate, CpfContextExecutionFactory contextFactory) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
    }

    public byte[] encode(Map<String, String> values) {
        return inChild("fixed-length.encode", () -> delegate.encode(values));
    }

    public Map<String, String> decode(byte[] bytes) {
        return inChild("fixed-length.decode", () -> delegate.decode(bytes));
    }

    private <T> T inChild(String standardExecutionId, java.util.concurrent.Callable<T> action) {
        CpfContextSnapshot parent = CpfContexts.requireSnapshot();
        CpfContextSnapshot child = contextFactory.childSnapshot(parent,
                new CpfContextExecutionFactory.ChildSpec(
                        standardExecutionId,
                        CpfContext.CpfExecutionType.INTEGRATION,
                        parent.context().execution().attempt(),
                        parent.context().execution().deadline(),
                        parent.context().operation()));
        try {
            return CpfContexts.call(child, action);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("fixed-length context execution failed", e);
        }
    }
}
