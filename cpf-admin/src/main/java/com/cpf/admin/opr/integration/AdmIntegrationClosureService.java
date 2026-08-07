package com.cpf.admin.opr.integration;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.security.crypto.CpfCryptoOperations;
import com.cpf.core.api.time.CpfTimeOperations;
import com.cpf.core.api.time.CpfTimeSnapshot;
import com.cpf.core.api.webhook.CpfWebhookDelivery;
import com.cpf.core.api.webhook.CpfWebhookOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** ADM operational consumer for time, data-quality and webhook closure paths. */
public final class AdmIntegrationClosureService {
    public static final String DATA_QUALITY_ACTION = "DATA_QUALITY_CORRECTION";
    public static final String DATA_QUALITY_OWNER = "CMN";
    public static final String DATA_QUALITY_COMMAND = "correctQuarantine";
    public static final String DATA_QUALITY_TARGET = "DATA_QUALITY_QUARANTINE";

    private final CpfCryptoOperations crypto;
    private final CpfDataQualityOperations quality;
    private final CpfTimeOperations time;
    private final CpfWebhookOperations webhook;
    private final AdmApprovalService approvals;
    private final ObjectMapper objectMapper;
    private final Duration correctionApprovalTtl;

    public AdmIntegrationClosureService(
            CpfCryptoOperations crypto,
            CpfDataQualityOperations quality,
            CpfTimeOperations time,
            CpfWebhookOperations webhook,
            AdmApprovalService approvals,
            ObjectMapper objectMapper,
            Duration correctionApprovalTtl) {
        this.crypto = crypto;
        this.quality = Objects.requireNonNull(quality, "quality");
        this.time = Objects.requireNonNull(time, "time");
        this.webhook = Objects.requireNonNull(webhook, "webhook");
        this.approvals = Objects.requireNonNull(approvals, "approvals");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        if (correctionApprovalTtl == null || correctionApprovalTtl.isZero() || correctionApprovalTtl.isNegative()) {
            throw new IllegalArgumentException("correctionApprovalTtl must be positive");
        }
        this.correctionApprovalTtl = correctionApprovalTtl;
    }


    /** Operational status only; key material is never returned. */
    public Map<String, Object> cryptoStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("configured", crypto != null);
        status.put("activeKeyVersion", crypto == null ? "" : crypto.activeKeyVersion());
        status.put("plaintextKeyExposed", false);
        return Collections.unmodifiableMap(new LinkedHashMap<>(status));
    }

    public CpfTimeSnapshot timeHealth(String zone, long maxSkewMillis) {
        if (maxSkewMillis < 0) throw new IllegalArgumentException("maxSkewMillis must not be negative");
        return time.snapshot(ZoneId.of(zone), Duration.ofMillis(maxSkewMillis));
    }

    public CpfDataQualityDecision validate(String recordId, Map<String, Object> record) {
        return quality.validate(require(recordId, "recordId"), immutableNullable(record));
    }

    /** Creates an immutable approval request; correction data is never accepted again at execution. */
    public Map<String, Object> requestCorrection(
            String quarantineId,
            long expectedVersion,
            Map<String, Object> corrected,
            String idempotencyKey,
            String actor,
            String reason) {
        quarantineId = require(quarantineId, "quarantineId");
        actor = require(actor, "actor");
        reason = bounded(reason, "reason", 8, 500);
        idempotencyKey = bounded(idempotencyKey, "idempotencyKey", 8, 128);
        if (expectedVersion < 1) throw new IllegalArgumentException("expectedVersion must be positive");
        if (corrected == null || corrected.isEmpty()) throw new IllegalArgumentException("corrected payload is required");
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("quarantineId", quarantineId);
        snapshot.put("expectedVersion", expectedVersion);
        snapshot.put("corrected", immutableNullable(corrected));
        String payload;
        try {
            payload = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("corrected payload cannot be serialized", error);
        }
        Map<String, Object> result = approvals.requestApproval(new AdmApprovalService.CreateRequest(
                idempotencyKey,
                null,
                null,
                DATA_QUALITY_ACTION,
                DATA_QUALITY_OWNER,
                DATA_QUALITY_COMMAND,
                DATA_QUALITY_TARGET,
                quarantineId,
                payload,
                time.now().plus(correctionApprovalTtl),
                reason), actor);
        return sanitizeApproval(result);
    }

    /**
     * Executes only an approved immutable snapshot. The approval engine performs atomic single-use
     * reservation and converts owner/finalization ambiguity to UNKNOWN; this preflight additionally
     * prevents cross-action, cross-target and maker/executor substitution.
     */
    public Map<String, Object> executeCorrection(long approvalRequestId, String actor, String reason) {
        if (approvalRequestId < 1) throw new IllegalArgumentException("approvalRequestId must be positive");
        actor = require(actor, "actor");
        reason = bounded(reason, "reason", 8, 500);
        Map<String, Object> detail = approvals.detail(approvalRequestId);
        requireEquals(detail, "actionType", DATA_QUALITY_ACTION);
        requireEquals(detail, "ownerModule", DATA_QUALITY_OWNER);
        requireEquals(detail, "ownerCommand", DATA_QUALITY_COMMAND);
        requireEquals(detail, "targetType", DATA_QUALITY_TARGET);
        requireEquals(detail, "approvalStatus", "APPROVED");
        String maker = text(detail.get("requestedBy"));
        if (actor.equals(maker)) throw new CpfValidationException("요청자와 실행자는 분리되어야 합니다.");
        Instant expiresAt = instant(detail.get("expireAt"));
        if (expiresAt != null && !expiresAt.isAfter(time.now())) {
            throw new CpfValidationException("만료된 승인 요청입니다.");
        }
        if (!isApprovedParticipant(detail.get("participants"), actor)) {
            throw new CpfValidationException("승인 참여자만 정정 실행을 요청할 수 있습니다.");
        }
        return sanitizeApproval(approvals.execute(approvalRequestId, reason, actor));
    }

    public CpfDataQualityDecision replayQuality(String id, long expectedVersion, String operationId,
                                                String actor, String reason) {
        return quality.replay(new CpfDataQualityOperations.ReplayCommand(
                require(id, "id"), expectedVersion, bounded(operationId, "operationId", 8, 128),
                require(actor, "actor"), bounded(reason, "reason", 8, 500)));
    }

    public List<CpfWebhookDelivery> webhookDlq(int limit) {
        if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit must be between 1 and 1000");
        return webhook.dlq(limit);
    }

    public CpfWebhookDelivery replayWebhook(String id, long expected, String actor, String reason) {
        if (expected < 1) throw new IllegalArgumentException("expectedVersion must be positive");
        return webhook.replay(require(id, "id"), expected, require(actor, "actor"), bounded(reason, "reason", 8, 500), time.now());
    }

    private static boolean isApprovedParticipant(Object value, String actor) {
        if (!(value instanceof Iterable<?> participants)) return false;
        for (Object participant : participants) {
            if (participant instanceof Map<?, ?> row
                    && actor.equals(text(row.get("operatorId")))
                    && "APPROVED".equals(text(row.get("decisionStatus")))) {
                return true;
            }
        }
        return false;
    }

    private static void requireEquals(Map<String, Object> detail, String field, String expected) {
        if (!expected.equalsIgnoreCase(text(detail.get(field)))) {
            throw new CpfValidationException("승인 요청의 " + field + "가 정정 명령과 일치하지 않습니다.");
        }
    }

    private static Map<String, Object> sanitizeApproval(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if ("payloadSnapshot".equals(key)) continue;
            if ("participants".equals(key) && entry.getValue() instanceof Iterable<?> participants) {
                sanitized.put(key, sanitizeParticipants(participants));
            } else if ("execution".equals(key) && entry.getValue() instanceof Map<?, ?> execution) {
                Map<String, Object> safe = new LinkedHashMap<>();
                execution.forEach((k, v) -> {
                    String name = String.valueOf(k);
                    if (!name.toLowerCase().contains("payload") && !name.toLowerCase().contains("secret")) safe.put(name, v);
                });
                sanitized.put(key, safe);
            } else {
                sanitized.put(key, entry.getValue());
            }
        }
        return Collections.unmodifiableMap(sanitized);
    }

    private static List<Map<String, Object>> sanitizeParticipants(Iterable<?> participants) {
        java.util.ArrayList<Map<String, Object>> safe = new java.util.ArrayList<>();
        for (Object participant : participants) {
            if (participant instanceof Map<?, ?> row) {
                Map<String, Object> selected = new LinkedHashMap<>();
                for (String field : List.of("stepNo", "targetType", "targetCode", "operatorId", "decisionStatus", "decisionAt")) {
                    if (row.containsKey(field)) selected.put(field, row.get(field));
                }
                safe.add(Collections.unmodifiableMap(selected));
            }
        }
        return List.copyOf(safe);
    }

    private static Instant instant(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        return Instant.parse(String.valueOf(value));
    }

    private static Map<String, Object> immutableNullable(Map<String, Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source == null ? Map.of() : source));
    }

    private static String bounded(String value, String field, int min, int max) {
        String result = require(value, field);
        if (result.length() < min || result.length() > max)
            throw new IllegalArgumentException(field + " length must be between " + min + " and " + max);
        return result;
    }

    private static String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
