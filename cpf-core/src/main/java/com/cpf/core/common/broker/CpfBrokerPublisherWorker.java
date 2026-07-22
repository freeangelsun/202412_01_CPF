package com.cpf.core.common.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 영속 outbox를 broker publisher port에 전달하는 CPF 표준 worker입니다.
 *
 * <p>스케줄러는 runOnce를 반복 호출하며, 실제 다중 worker lock의 최종 책임은
 * CpfBrokerOutboxPort 구현체가 담당합니다.</p>
 */
public class CpfBrokerPublisherWorker {
    private final CpfBrokerOutboxPort outboxPort;
    private final CpfBrokerPublisher publisher;

    public CpfBrokerPublisherWorker(CpfBrokerOutboxPort outboxPort, CpfBrokerPublisher publisher) {
        this.outboxPort = Objects.requireNonNull(outboxPort, "outboxPort는 필수입니다.");
        this.publisher = Objects.requireNonNull(publisher, "publisher는 필수입니다.");
    }

    public RunResult runOnce(String workerId, int limit) {
        if (workerId == null || workerId.isBlank()) {
            throw new IllegalArgumentException("workerId는 필수입니다.");
        }
        List<CpfBrokerEnvelope> claimed = outboxPort.claimPending(workerId, limit);
        List<CpfBrokerResult> results = new ArrayList<>(claimed.size());
        for (CpfBrokerEnvelope envelope : claimed) {
            CpfBrokerResult result;
            try {
                result = publisher.publish(envelope);
                if (result == null) {
                    result = CpfBrokerResult.failed(
                            envelope.message().messageId(),
                            "UNKNOWN_ADAPTER",
                            "publisher가 결과를 반환하지 않았습니다.");
                }
            } catch (RuntimeException ex) {
                result = CpfBrokerResult.failed(
                        envelope.message().messageId(),
                        "PUBLISHER_EXCEPTION",
                        safeMessage(ex));
            }
            outboxPort.markPublished(envelope.message().messageId(), result);
            results.add(result);
        }
        long successCount = results.stream().filter(this::isPublished).count();
        return new RunResult(workerId, claimed.size(), successCount, claimed.size() - successCount, results);
    }

    private boolean isPublished(CpfBrokerResult result) {
        return "PUBLISHED".equalsIgnoreCase(result.status())
                || "SUCCESS".equalsIgnoreCase(result.status())
                || "ACCEPTED".equalsIgnoreCase(result.status());
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    public record RunResult(
            String workerId,
            int claimedCount,
            long successCount,
            long failureCount,
            List<CpfBrokerResult> results) {

        public RunResult {
            results = results == null ? List.of() : List.copyOf(results);
        }
    }
}
