package com.cpf.starter.platform.operations.observability;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.context.CpfContextProjection;
import com.cpf.foundation.context.CpfContextProjectionRegistry;
import org.slf4j.MDC;

/** Downstream-only projection: CPF Context never imports MDC or OpenTelemetry. */
public final class CpfObservabilityContextProjection implements CpfContextProjection, AutoCloseable {
    private final CpfMdcContextProjection mdc;
    private final CpfTraceContextProjection trace;
    private final AutoCloseable registration;

    public CpfObservabilityContextProjection(CpfMdcContextProjection mdc, CpfTraceContextProjection trace) {
        this(mdc, trace, null);
    }

    public CpfObservabilityContextProjection(CpfMdcContextProjection mdc, CpfTraceContextProjection trace,
            CpfContextProjectionRegistry projections) {
        this.mdc = mdc;
        this.trace = trace;
        this.registration = projections == null ? () -> { } : projections.register(this);
    }

    @Override public void project(CpfContextSnapshot snapshot) {
        mdc.bind(snapshot);
        var ids = trace.current();
        if (ids.traceId() != null) MDC.put("trace_id", ids.traceId());
        if (ids.spanId() != null) MDC.put("span_id", ids.spanId());
    }

    @Override public void clear() {
        mdc.clear();
        MDC.remove("trace_id");
        MDC.remove("span_id");
    }

    @Override public void close() throws Exception {
        clear();
        registration.close();
    }
}
