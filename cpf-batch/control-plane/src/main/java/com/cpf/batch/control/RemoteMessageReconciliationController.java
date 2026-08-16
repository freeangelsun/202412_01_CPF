package com.cpf.batch.control;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * ACK/response 손실로 UNKNOWN이 된 Remote Batch Message를 승인된 운영 판단 뒤에만 재시도 가능 상태로 전환합니다.
 */
@RestController
@RequestMapping("/api/v1/batch/remote-messages")
public class RemoteMessageReconciliationController {
    private static final String RECONCILED_PREFIX = "RECONCILED_RETRY_";

    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final BatVerifiedActorResolver actorResolver;

    public RemoteMessageReconciliationController(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            BatVerifiedActorResolver actorResolver) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.actorResolver = actorResolver;
    }

    @PostMapping("/{direction}/{messageId}/reconcile-unknown/retry")
    @Transactional
    public ResponseEntity<Map<String, Object>> retryUnknown(
            @PathVariable String direction,
            @PathVariable String messageId,
            @RequestBody RetryUnknownRequest request,
            HttpServletRequest http) {
        String normalizedDirection = direction(direction);
        validateMessageId(messageId);
        validate(request);
        var actors = actorResolver.approved(
                http,
                request.requestedBy(),
                request.approvedBy(),
                request.approvalRequestId());
        String reconciliationCode = reconciliationCode(
                normalizedDirection, messageId, request, actors);
        Map<String, Object> before = message(normalizedDirection, messageId);

        if (isIdempotentReplay(before, request, reconciliationCode)) {
            return accepted(normalizedDirection, messageId, request, actors, true);
        }
        assertRetryableUnknown(before, request);

        int changed = jdbc.update(
                sql.required("execution-remote-message-reconcile-unknown-retry"),
                reconciliationCode,
                normalizedDirection,
                messageId,
                request.payloadSha256().toLowerCase(Locale.ROOT),
                request.expectedAttemptNo(),
                request.expectedVersion());
        if (changed != 1) {
            Map<String, Object> afterRace = message(normalizedDirection, messageId);
            if (isIdempotentReplay(afterRace, request, reconciliationCode)) {
                return accepted(normalizedDirection, messageId, request, actors, true);
            }
            throw conflict("BATCH_REMOTE_MESSAGE_RECONCILE_CONFLICT");
        }

        audit(normalizedDirection, messageId, before, request, actors, reconciliationCode);
        return accepted(normalizedDirection, messageId, request, actors, false);
    }

    private Map<String, Object> message(String direction, String messageId) {
        try {
            return jdbc.queryForMap(
                    sql.required("execution-remote-message-reconcile-load"),
                    direction,
                    messageId);
        } catch (EmptyResultDataAccessException notFound) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "BATCH_REMOTE_MESSAGE_NOT_FOUND",
                    notFound);
        }
    }

    private static void assertRetryableUnknown(
            Map<String, Object> before,
            RetryUnknownRequest request) {
        if (!"UNKNOWN".equals(Objects.toString(before.get("status_cd"), ""))) {
            throw conflict("BATCH_REMOTE_MESSAGE_NOT_UNKNOWN");
        }
        if (!request.payloadSha256().equalsIgnoreCase(
                Objects.toString(before.get("payload_sha256"), ""))) {
            throw conflict("BATCH_REMOTE_MESSAGE_PAYLOAD_CONFLICT");
        }
        if (number(before.get("attempt_no")) != request.expectedAttemptNo()) {
            throw conflict("BATCH_REMOTE_MESSAGE_ATTEMPT_CONFLICT");
        }
        if (number(before.get("version_no")) != request.expectedVersion()) {
            throw conflict("BATCH_REMOTE_MESSAGE_VERSION_CONFLICT");
        }
    }

    private static boolean isIdempotentReplay(
            Map<String, Object> row,
            RetryUnknownRequest request,
            String reconciliationCode) {
        return "FAILED".equals(Objects.toString(row.get("status_cd"), ""))
                && reconciliationCode.equals(Objects.toString(row.get("last_error_cd"), ""))
                && request.payloadSha256().equalsIgnoreCase(
                        Objects.toString(row.get("payload_sha256"), ""))
                && number(row.get("attempt_no")) == request.expectedAttemptNo()
                && number(row.get("version_no")) == request.expectedVersion() + 1;
    }

    private void audit(
            String direction,
            String messageId,
            Map<String, Object> before,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors,
            String reconciliationCode) {
        String jobId = "REMOTE:" + direction + ":" + messageId;
        String beforeData = SensitiveTextSanitizer.sanitize(
                "direction=" + direction
                        + ",messageId=" + messageId
                        + ",status=" + before.get("status_cd")
                        + ",attemptNo=" + before.get("attempt_no")
                        + ",version=" + before.get("version_no")
                        + ",lastErrorCode=" + before.get("last_error_cd"));
        String afterData = SensitiveTextSanitizer.sanitize(
                "direction=" + direction
                        + ",messageId=" + messageId
                        + ",status=FAILED"
                        + ",reconciliationCode=" + reconciliationCode
                        + ",approvalRequestId=" + actors.approvalRequestId()
                        + ",approvedBy=" + actors.approvedBy());
        int audited = jdbc.update(
                sql.required("execution-remote-message-reconcile-audit"),
                jobId,
                actors.requestedBy(),
                SensitiveTextSanitizer.sanitize(request.reason()),
                beforeData,
                afterData,
                "UNKNOWN retry approved",
                actors.approvedBy(),
                actors.approvedBy());
        if (audited != 1) {
            throw new IllegalStateException("BATCH_REMOTE_MESSAGE_RECONCILE_AUDIT_REJECTED");
        }
    }

    private static ResponseEntity<Map<String, Object>> accepted(
            String direction,
            String messageId,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors,
            boolean replayed) {
        return ResponseEntity.accepted().body(Map.of(
                "direction", direction,
                "messageId", messageId,
                "previousStatus", "UNKNOWN",
                "status", "FAILED",
                "expectedAttemptNo", request.expectedAttemptNo(),
                "expectedVersion", request.expectedVersion(),
                "resultVersion", request.expectedVersion() + 1,
                "approvalRequestId", actors.approvalRequestId(),
                "replayed", replayed));
    }

    private static void validate(RetryUnknownRequest request) {
        if (request == null
                || request.payloadSha256() == null
                || !request.payloadSha256().matches("(?i)[0-9a-f]{64}")
                || request.expectedAttemptNo() <= 0
                || request.expectedVersion() <= 0
                || request.idempotencyKey() == null
                || request.idempotencyKey().isBlank()
                || request.idempotencyKey().length() > 160
                || request.reason() == null
                || request.reason().isBlank()) {
            throw new IllegalArgumentException(
                    "payloadSha256, positive expectedAttemptNo/expectedVersion, idempotencyKey and reason are required");
        }
    }

    private static String direction(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("REQUEST") && !normalized.equals("REPLY")) {
            throw new IllegalArgumentException("direction must be REQUEST or REPLY");
        }
        return normalized;
    }

    private static void validateMessageId(String messageId) {
        if (messageId == null || !messageId.matches("[A-Za-z0-9._:-]{1,64}")) {
            throw new IllegalArgumentException("messageId is invalid");
        }
    }

    private static String reconciliationCode(
            String direction,
            String messageId,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors) {
        String canonicalCommand = String.join("\n",
                direction,
                messageId,
                request.payloadSha256().toLowerCase(Locale.ROOT),
                Long.toString(request.expectedAttemptNo()),
                Long.toString(request.expectedVersion()),
                request.idempotencyKey().trim(),
                actors.requestedBy(),
                actors.approvedBy(),
                actors.approvalRequestId(),
                request.reason().trim());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalCommand.getBytes(StandardCharsets.UTF_8));
            return RECONCILED_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static long number(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(Objects.toString(value, ""));
        } catch (NumberFormatException invalid) {
            throw conflict("BATCH_REMOTE_MESSAGE_VERSION_INVALID");
        }
    }

    private static ResponseStatusException conflict(String code) {
        return new ResponseStatusException(HttpStatus.CONFLICT, code);
    }

    public record RetryUnknownRequest(
            String payloadSha256,
            long expectedAttemptNo,
            long expectedVersion,
            String idempotencyKey,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
    }
}
