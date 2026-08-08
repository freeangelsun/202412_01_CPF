package com.cpf.starter.messaging.reliability.jdbc.internal;

import com.cpf.core.common.broker.*;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Durable outbox publisher with explicit FAILED versus UNKNOWN_RESULT semantics and claim fencing. */
public class CpfBrokerPublisherWorker {
    private final CpfBrokerOutboxPort outboxPort;
    private final CpfBrokerUnknownResultPort unknownPort;
    private final CpfBrokerPublisher publisher;
    private final Clock clock;
    private final Duration unknownReconcileDelay;

    public CpfBrokerPublisherWorker(CpfBrokerOutboxPort outboxPort, CpfBrokerPublisher publisher) {
        this(outboxPort, outboxPort instanceof CpfBrokerUnknownResultPort value ? value : null,
                publisher, Clock.systemUTC(), Duration.ofSeconds(30));
    }

    public CpfBrokerPublisherWorker(CpfBrokerOutboxPort outboxPort,
            CpfBrokerUnknownResultPort unknownPort, CpfBrokerPublisher publisher,
            Clock clock, Duration unknownReconcileDelay) {
        this.outboxPort = requireFencedOutbox(outboxPort);
        this.unknownPort = requireFencedUnknown(unknownPort);
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.unknownReconcileDelay = requirePositive(unknownReconcileDelay, "unknownReconcileDelay");
    }

    public RunResult runOnce(String workerId, int limit) {
        String owner = requireWorker(workerId);
        List<CpfBrokerEnvelope> claimed = outboxPort.claimPending(owner, limit);
        List<CpfBrokerResult> results = new ArrayList<>(claimed.size());
        long unknownCount = 0;
        for (CpfBrokerEnvelope envelope : claimed) {
            CpfBrokerResult result;
            try {
                result = publisher.publish(envelope);
                if (result == null) result = unknown(envelope, "publisher returned no result");
            } catch (RuntimeException ex) {
                result = unknown(envelope, safeMessage(ex));
            }
            try {
                if (isUnknown(result)) {
                    unknownCount++;
                    if (unknownPort == null) {
                        throw new IllegalStateException("UNKNOWN broker result requires CpfBrokerUnknownResultPort");
                    }
                    unknownPort.markUnknown(owner, envelope.message().messageId(), result,
                            clock.instant().plus(unknownReconcileDelay));
                } else {
                    outboxPort.markPublished(owner, envelope.message().messageId(), result);
                }
            } catch (RuntimeException persistenceFailure) {
                if (unknownPort != null && !isUnknown(result)) {
                    try {
                        unknownPort.markUnknown(owner, envelope.message().messageId(),
                                unknown(envelope, "provider result persistence failed: " + safeMessage(persistenceFailure)),
                                clock.instant().plus(unknownReconcileDelay));
                    } catch (RuntimeException fallbackFailure) {
                        persistenceFailure.addSuppressed(fallbackFailure);
                    }
                }
                throw persistenceFailure;
            }
            results.add(result);
        }
        long successCount = results.stream().filter(this::isPublished).count();
        long failureCount = claimed.size() - successCount - unknownCount;
        return new RunResult(owner, claimed.size(), successCount, failureCount, unknownCount, results);
    }

    private CpfBrokerResult unknown(CpfBrokerEnvelope envelope, String detail) {
        return new CpfBrokerResult("UNKNOWN", envelope.message().messageId(), "UNKNOWN_ADAPTER", null,
                clock.instant(), CpfBrokerFailureSanitizer.sanitize(detail));
    }
    private boolean isUnknown(CpfBrokerResult r){return "UNKNOWN".equalsIgnoreCase(r.status())||"RESULT_UNKNOWN".equalsIgnoreCase(r.status());}
    private boolean isPublished(CpfBrokerResult r){return "PUBLISHED".equalsIgnoreCase(r.status())||"SUCCESS".equalsIgnoreCase(r.status())||"ACCEPTED".equalsIgnoreCase(r.status());}
    private String safeMessage(RuntimeException ex) { String m=ex.getMessage(); return CpfBrokerFailureSanitizer.sanitize(m==null||m.isBlank()?ex.getClass().getSimpleName():m); }
    private static CpfBrokerOutboxPort requireFencedOutbox(CpfBrokerOutboxPort value) {
        CpfBrokerOutboxPort port = Objects.requireNonNull(value, "outboxPort");
        if (!port.supportsFencedPublishMutation()) {
            throw new IllegalArgumentException(
                    "Broker outbox adapter must enforce fenced publish mutation");
        }
        return port;
    }
    private static CpfBrokerUnknownResultPort requireFencedUnknown(
            CpfBrokerUnknownResultPort value) {
        if (value == null) return null;
        if (!value.supportsFencedUnknownMutation()) {
            throw new IllegalArgumentException(
                    "Broker UNKNOWN adapter must enforce fenced UNKNOWN mutation");
        }
        return value;
    }
    private static Duration requirePositive(Duration v,String n){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException(n+" must be positive");return v;}
    private static String requireWorker(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("workerId is required");return v.trim();}

    public record RunResult(String workerId,int claimedCount,long successCount,long failureCount,long unknownCount,List<CpfBrokerResult> results){
        public RunResult { results=results==null?List.of():List.copyOf(results); }
    }
}
