package com.cpf.admin.opr.incident;

import java.time.LocalDateTime;
import java.util.List;

/** 알림·Incident·Maintenance 운영 계약입니다. */
public final class AdmIncidentContracts {
    private AdmIncidentContracts() {}

    public record PolicySaveRequest(
            String policyCode, String eventType, String eventSubType, String severity,
            int thresholdCount, int windowSeconds, int escalationMinutes,
            String receiverGroup, String useYn, long expectedVersion,
            String reason, String approvalRequestId, String idempotencyKey) {}

    public record PolicyResponse(
            long policyId, String policyCode, String eventType, String eventSubType,
            String severity, int thresholdCount, int windowSeconds, int escalationMinutes,
            String receiverGroup, String useYn, long version,
            String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {}

    public record SignalRequest(
            String policyCode, String sourceType, String sourceId, String correlationId,
            String transactionId, String title, String summary, LocalDateTime occurredAt,
            String idempotencyKey) {}

    public record SignalResult(
            long signalId, Long incidentId, String result, boolean suppressedByMaintenance,
            int observedCount, int thresholdCount) {}

    public record IncidentActionRequest(
            long expectedVersion, String reason, String approvalRequestId, String idempotencyKey) {}

    public record IncidentResponse(
            long incidentId, long policyId, String policyCode, String severity, String status,
            String title, String summary, String sourceType, String sourceId,
            String correlationId, String transactionId, int occurrenceCount,
            int escalationLevel, LocalDateTime firstOccurredAt, LocalDateTime lastOccurredAt,
            LocalDateTime acknowledgedAt, LocalDateTime resolvedAt,
            String ownerId, long version, String createdBy, LocalDateTime createdAt,
            String updatedBy, LocalDateTime updatedAt) {}

    public record TimelineResponse(
            long timelineId, long incidentId, String actionType, String beforeStatus,
            String afterStatus, String reason, String approvalRequestId,
            String actorId, LocalDateTime createdAt) {}

    public record MaintenanceSaveRequest(
            String maintenanceCode, String targetType, String targetId,
            LocalDateTime startsAt, LocalDateTime endsAt, String useYn,
            long expectedVersion, String reason, String approvalRequestId, String idempotencyKey) {}

    public record MaintenanceResponse(
            long maintenanceId, String maintenanceCode, String targetType, String targetId,
            LocalDateTime startsAt, LocalDateTime endsAt, String useYn, long version,
            String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {}

    public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
