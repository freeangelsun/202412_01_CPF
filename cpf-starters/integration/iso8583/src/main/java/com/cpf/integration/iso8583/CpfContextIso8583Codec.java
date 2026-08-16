package com.cpf.integration.iso8583;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.util.Objects;

/** ISO8583 codec를 동일 transaction lineage의 child execution으로 실행합니다. Network I/O는 TCP Owner가 담당합니다. */
public final class CpfContextIso8583Codec {
    private final CpfIso8583Codec delegate;
    private final CpfContextExecutionFactory contextFactory;

    public CpfContextIso8583Codec(CpfIso8583Codec delegate, CpfContextExecutionFactory contextFactory) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
    }

    public byte[] encode(CpfIso8583Message message) {
        return inChild("iso8583.encode", () -> delegate.encode(message));
    }

    public CpfIso8583Message decode(byte[] bytes) {
        return inChild("iso8583.decode", () -> delegate.decode(bytes));
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
            throw new IllegalStateException("ISO8583 context execution failed", e);
        }
    }
}
