package com.cpf.core.common.broker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Durable outbox publisher with explicit FAILED versus UNKNOWN_RESULT semantics. */
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
            CpfBrokerUnknownResultPort unknownPort,
            CpfBrokerPublisher publisher,
            Clock clock,
            Duration unknownReconcileDelay) {
        this.outboxPort = Objects.requireNonNull(outboxPort, "outboxPort");
        this.unknownPort = unknownPort;
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.unknownReconcileDelay = requirePositive(unknownReconcileDelay, "unknownReconcileDelay");
    }

    public RunResult runOnce(String workerId, int limit) {
        if (workerId == null || workerId.isBlank()) throw new IllegalArgumentException("workerId는 필수입니다.");
        List<CpfBrokerEnvelope> claimed = outboxPort.claimPending(workerId, limit);
        List<CpfBrokerResult> results = new ArrayList<>(claimed.size());
        long unknownCount = 0;
        for (CpfBrokerEnvelope envelope : claimed) {
            CpfBrokerResult result;
            try {
                result = publisher.publish(envelope);
                if (result == null) result = unknown(envelope, "publisher가 결과를 반환하지 않았습니다.");
            } catch (RuntimeException ex) {
                result = unknown(envelope, safeMessage(ex));
            }
            if (isUnknown(result)) {
                unknownCount++;
                if (unknownPort == null) {
                    throw new IllegalStateException("UNKNOWN broker result requires CpfBrokerUnknownResultPort");
                }
                unknownPort.markUnknown(envelope.message().messageId(), result,
                        clock.instant().plus(unknownReconcileDelay));
            } else {
                outboxPort.markPublished(envelope.message().messageId(), result);
            }
            results.add(result);
        }
        long successCount = results.stream().filter(this::isPublished).count();
        long failureCount = claimed.size() - successCount - unknownCount;
        return new RunResult(workerId, claimed.size(), successCount, failureCount, unknownCount, results);
    }

    private CpfBrokerResult unknown(CpfBrokerEnvelope envelope, String detail) {
        return new CpfBrokerResult("UNKNOWN", envelope.message().messageId(), "UNKNOWN_ADAPTER", null,
                clock.instant(), detail);
    }
    private boolean isUnknown(CpfBrokerResult r){return "UNKNOWN".equalsIgnoreCase(r.status())||"RESULT_UNKNOWN".equalsIgnoreCase(r.status());}
    private boolean isPublished(CpfBrokerResult r){return "PUBLISHED".equalsIgnoreCase(r.status())||"SUCCESS".equalsIgnoreCase(r.status())||"ACCEPTED".equalsIgnoreCase(r.status());}
    private String safeMessage(RuntimeException ex){String m=ex.getMessage();return m==null||m.isBlank()?ex.getClass().getSimpleName():m;}
    private static Duration requirePositive(Duration value,String name){if(value==null||value.isZero()||value.isNegative())throw new IllegalArgumentException(name+" must be positive");return value;}

    public record RunResult(String workerId,int claimedCount,long successCount,long failureCount,long unknownCount,List<CpfBrokerResult> results){
        public RunResult { results=results==null?List.of():List.copyOf(results); }
    }
}
