package com.cpf.web.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/** Default failure sink: emits a masked operational event and publishes it for DB/ADM observability adapters. */
public final class CpfDefaultHeaderFailureRecorder implements CpfHeaderFailureRecorder {
    private static final Logger log = LoggerFactory.getLogger(CpfDefaultHeaderFailureRecorder.class);
    private final ApplicationEventPublisher publisher;

    public CpfDefaultHeaderFailureRecorder(ApplicationEventPublisher publisher) { this.publisher = publisher; }

    @Override
    public void record(Failure failure) {
        if (failure == null) return;
        log.warn("CPF_HEADER_FAILURE transactionId={} system={} application={} instance={} header={} category={} errorCode={} httpStatus={} method={} uri={}",
                safe(failure.transactionId()), safe(failure.systemCode()), safe(failure.application()), safe(failure.instance()),
                safe(failure.headerName()), safe(failure.category()), safe(failure.errorCode()), failure.httpStatus(),
                safe(failure.method()), safe(failure.uri()));
        if (publisher != null) publisher.publishEvent(failure);
    }

    private static String safe(String value) {
        if (value == null) return "";
        String normalized = value.replaceAll("[\\r\\n\\t]", "_");
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }
}
