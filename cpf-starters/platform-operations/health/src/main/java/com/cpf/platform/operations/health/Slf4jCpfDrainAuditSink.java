package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.CpfDrainState;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Slf4jCpfDrainAuditSink implements CpfDrainAuditSink {
    private static final Logger log = LoggerFactory.getLogger("cpf.operations.drain.audit");
    @Override public void record(String action, String reason, CpfDrainState result, long inFlight, Instant occurredAt) {
        log.info("cpfDrain action={} result={} inFlight={} occurredAt={} reason={}", action, result, inFlight, occurredAt, sanitize(reason));
    }
    private static String sanitize(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\r\\n\\t]", " ").trim().substring(0, Math.min(256, value.trim().length()));
    }
}
