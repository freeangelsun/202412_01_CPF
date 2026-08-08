package com.cpf.starter.messaging.reliability.jdbc;

import com.cpf.core.common.broker.CpfBrokerReplayPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Broker reliability 운영 facade입니다.
 *
 * <p>과거 operator/reason만으로 실행하던 replay API는 승인 정책을 우회하므로 fail-closed합니다.
 * 실제 실행은 ADM Approval Engine의 BrokerReliabilityApprovalOwnerCommandAdapter가 승인 Snapshot을
 * 검증한 뒤 CPF public reliability port를 호출합니다.</p>
 */
public class CpfBrokerReliabilityOperations {
    private static final int MAX_REPLAY_RANGE = 5_000;

    @SuppressWarnings("unused")
    private final CpfBrokerReplayPort replay;

    public CpfBrokerReliabilityOperations(CpfBrokerReplayPort replay) {
        this.replay = Objects.requireNonNull(replay, "replay must not be null");
    }

    /** @deprecated 승인 완료 Owner Command 없이 단건 replay를 실행할 수 없습니다. */
    @Deprecated(forRemoval = false)
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerResult replay(String messageId, String operatorId, String reason) {
        requireAudit(operatorId, reason);
        requireText(messageId, "messageId");
        throw new SecurityException("DLQ replay requires an approved owner command");
    }

    /** @deprecated 범위 replay도 개별 대상 Snapshot 승인 없이는 실행할 수 없습니다. */
    @Deprecated(forRemoval = false)
    @Transactional(transactionManager = "cpfTransactionManager")
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
        throw new SecurityException("DLQ range replay requires per-target approval snapshots");
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
