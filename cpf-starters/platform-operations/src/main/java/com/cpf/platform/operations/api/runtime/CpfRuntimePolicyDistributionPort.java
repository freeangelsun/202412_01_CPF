package com.cpf.platform.operations.api.runtime;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * ADM 정책 변경을 다중 Runtime Instance에 내구성 있게 전달하는 공개 Port입니다.
 *
 * <p>Event 원장과 Consumer별 ACK를 분리하여 부분 적용, 재시도, Drift를 조회할 수 있습니다.</p>
 */
public interface CpfRuntimePolicyDistributionPort {
    DistributionEvent publish(PublishCommand command);
    List<DistributionEvent> claimPending(String consumerId, List<String> eventTypes, int limit, int leaseSeconds);
    DeliveryStatus acknowledge(AcknowledgeCommand command);
    List<DeliveryStatus> findDeliveryStatus(String aggregateType, String aggregateId, int limit);

    record PublishCommand(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String action,
            String payloadChecksum,
            Map<String, String> metadata,
            String reason,
            String requestedBy,
            OffsetDateTime occurredAt) {
        public PublishCommand {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
            occurredAt = occurredAt == null ? OffsetDateTime.now() : occurredAt;
        }
    }

    record DistributionEvent(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String action,
            String payloadChecksum,
            Map<String, String> metadata,
            String reason,
            String requestedBy,
            OffsetDateTime occurredAt,
            long fencingToken,
            int deliveryAttempt) {}

    record AcknowledgeCommand(
            String eventId,
            String consumerId,
            long fencingToken,
            String status,
            String errorCode,
            String errorMessage,
            OffsetDateTime acknowledgedAt) {}

    record DeliveryStatus(
            String eventId,
            String consumerId,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String status,
            int attemptCount,
            long fencingToken,
            String errorCode,
            String errorMessage,
            OffsetDateTime leasedUntil,
            OffsetDateTime acknowledgedAt,
            OffsetDateTime updatedAt) {}
}
