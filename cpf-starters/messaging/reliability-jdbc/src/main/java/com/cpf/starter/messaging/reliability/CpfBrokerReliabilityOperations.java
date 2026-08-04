package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.CpfBrokerReplayPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Auditable broker reliability operations facade.
 *
 * <p>Replay requests require an authenticated operator and a non-empty reason. The transaction
 * boundary covers all JDBC replay mutations so a missing outbox or another provider failure cannot
 * leave the DLQ and inbox in a partially changed state.</p>
 */
public final class CpfBrokerReliabilityOperations {
    private static final int MAX_REPLAY_RANGE = 5_000;

    private final CpfBrokerReplayPort replay;

    public CpfBrokerReliabilityOperations(CpfBrokerReplayPort replay) {
        this.replay = Objects.requireNonNull(replay, "replay must not be null");
    }

    @Transactional
    public CpfBrokerResult replay(String messageId, String operatorId, String reason) {
        requireAudit(operatorId, reason);
        return replay.replay(requireText(messageId, "messageId"));
    }

    @Transactional
    public List<CpfBrokerResult> replayRange(
            String topic,
            Instant from,
            Instant to,
            int limit,
            String operatorId,
            String reason) {
        requireAudit(operatorId, reason);
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
        if (limit < 1 || limit > MAX_REPLAY_RANGE) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_REPLAY_RANGE);
        }
        String normalizedTopic = topic == null || topic.isBlank() ? null : topic.trim();
        return replay.replayRange(normalizedTopic, from, to, limit);
    }

    private static void requireAudit(String operatorId, String reason) {
        requireText(operatorId, "operatorId");
        requireText(reason, "replay reason");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            if ("operatorId".equals(name)) {
                throw new SecurityException("operatorId is required");
            }
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
