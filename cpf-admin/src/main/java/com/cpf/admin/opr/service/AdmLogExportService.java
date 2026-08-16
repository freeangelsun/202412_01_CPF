package com.cpf.admin.opr.service;

import com.cpf.data.persistence.api.annotation.CpfTx;
import com.cpf.admin.opr.dto.AdmLogExportRequest;
import com.cpf.admin.opr.dto.AdmLogExportResponse;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.CpfMasking;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import com.cpf.foundation.annotation.CpfService;

/**
 * 거래 로그 상세를 서버에서 마스킹하고 ADM DB의 다중 인스턴스 공용 만료 Artifact로 생성합니다.
 * Artifact 본문과 소유자·만료시각은 한 행에 원자 저장되며 브라우저 임의 Blob 생성 경로를 허용하지 않습니다.
 */
@CpfService
public class AdmLogExportService {
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final long MAX_ARTIFACT_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> SECRET_KEYS = Set.of(
            "password", "passwd", "authorization", "cookie", "setcookie", "token", "secret",
            "privatekey", "clientsecret", "credential", "accesstoken", "refreshtoken");
    private static final Set<String> PII_KEYS = Set.of(
            "memberno", "customerno", "mobile", "mobileno", "email", "residentno",
            "accountno", "cardno");

    private final AdmLogQueryService logQueryService;
    private final AdmAuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public AdmLogExportService(
            AdmLogQueryService logQueryService,
            AdmAuditLogService auditLogService,
            ObjectMapper objectMapper,
            JdbcTemplate jdbc) {
        this(logQueryService, auditLogService, objectMapper, jdbc, Clock.systemUTC());
    }

    AdmLogExportService(
            AdmLogQueryService logQueryService,
            AdmAuditLogService auditLogService,
            ObjectMapper objectMapper,
            JdbcTemplate jdbc,
            Clock clock) {
        this.logQueryService = logQueryService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @CpfTx(id="ADM_ADMLOGEXPORTSERVICE_CREATE", name="ADM_ADMLOGEXPORTSERVICE_CREATE", ownerDomain="ADM")
    public AdmLogExportResponse create(AdmLogExportRequest request, String actor, String clientIp) {
        String operator = required(actor, "인증 운영자 정보가 없습니다.");
        String reason = auditLogService.requireReason(request == null ? null : request.reason());
        String action = normalizeAction(request == null ? null : request.action());
        long logId = parseLogId(request == null ? null : request.logId());
        Map<String, Object> detail = logQueryService.getLogDetail(logId);
        String exportId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        LocalDateTime expiresAt = createdAt.plus(TTL);
        String watermark = "CPF LOG EXPORT | actor=" + operator + " | exportId=" + exportId
                + " | createdAt=" + createdAt + "Z | expiresAt=" + expiresAt + "Z";

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("schemaVersion", 2);
        envelope.put("watermark", watermark);
        envelope.put("logId", logId);
        envelope.put("masked", true);
        envelope.put("detail", protect(detail));
        String content = toJson(envelope);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_ARTIFACT_BYTES) {
            throw new CpfValidationException("로그 Export 결과가 허용 크기를 초과했습니다.");
        }

        String fileName = "cpf-log-export-" + exportId + ".json";
        String downloadUrl = null;
        String clipboardContent = null;
        if ("DOWNLOAD".equals(action)) {
            int inserted = jdbc.update("""
                    INSERT INTO adm_log_export_artifact
                        (export_id, owner_operator_id, file_name, content_type, artifact_content,
                         content_length, created_at, expires_at, status_code)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'READY')
                    """, exportId, operator, fileName, "application/json", bytes, bytes.length,
                    Timestamp.valueOf(createdAt), Timestamp.valueOf(expiresAt));
            if (inserted != 1) throw new IllegalStateException("로그 Export Artifact 저장에 실패했습니다.");
            downloadUrl = "/adm/api/log-exports/" + exportId + "/artifact";
        } else {
            clipboardContent = content;
        }
        auditLogService.record(
                null, operator, "LOG_DETAIL_EXPORT_" + action, "TRANSACTION_LOG", String.valueOf(logId), reason,
                null, "exportId=" + exportId + ",masked=true,expiresAt=" + expiresAt,
                "LOG_EXPORT", clientIp);
        return new AdmLogExportResponse(
                exportId, "READY", fileName, downloadUrl, clipboardContent, expiresAt, watermark);
    }

    @CpfTx(id="ADM_ADMLOGEXPORTSERVICE_READ", name="ADM_ADMLOGEXPORTSERVICE_READ", ownerDomain="ADM")
    public DownloadArtifact read(String exportId, String actor, String clientIp) {
        String operator = required(actor, "인증 운영자 정보가 없습니다.");
        String id = required(exportId, "Export ID가 필요합니다.");
        Artifact artifact = jdbc.query("""
                SELECT export_id, owner_operator_id, file_name, artifact_content, expires_at, status_code
                  FROM adm_log_export_artifact
                 WHERE export_id = ?
                """, rs -> rs.next()
                ? new Artifact(rs.getString("export_id"), rs.getString("owner_operator_id"),
                        rs.getString("file_name"), rs.getBytes("artifact_content"),
                        rs.getTimestamp("expires_at").toLocalDateTime(), rs.getString("status_code"))
                : null, id);
        if (artifact == null || !"READY".equals(artifact.status())) {
            throw new CpfValidationException("Export Artifact가 없거나 사용할 수 없습니다.");
        }
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        if (!now.isBefore(artifact.expiresAt())) {
            jdbc.update("DELETE FROM adm_log_export_artifact WHERE export_id = ?", id);
            throw new CpfValidationException("Export Artifact가 만료되었습니다.");
        }
        if (!artifact.actor().equals(operator)) {
            auditLogService.record(null, operator, "LOG_DETAIL_EXPORT_DENIED", "LOG_EXPORT", id,
                    "다른 운영자의 Export Artifact 접근", null, "ownerMismatch", "LOG_EXPORT", clientIp);
            throw new CpfValidationException("다른 운영자의 Export Artifact에는 접근할 수 없습니다.");
        }
        int consumed = jdbc.update("""
                UPDATE adm_log_export_artifact
                   SET downloaded_at = CURRENT_TIMESTAMP, download_count = download_count + 1
                 WHERE export_id = ? AND status_code = 'READY' AND expires_at > CURRENT_TIMESTAMP
                """, id);
        if (consumed != 1) throw new CpfValidationException("Export Artifact 상태가 변경되어 다시 조회해야 합니다.");
        auditLogService.record(null, operator, "LOG_DETAIL_EXPORT_DOWNLOAD", "LOG_EXPORT", id,
                "감사된 로그 Export 다운로드", null, "bytes=" + artifact.content().length,
                "LOG_EXPORT", clientIp);
        return new DownloadArtifact(artifact.fileName(), artifact.content());
    }

    @Scheduled(fixedDelayString = "${cpf.admin.log-export.cleanup-delay-ms:60000}")
    public void cleanupExpired() {
        jdbc.update("DELETE FROM adm_log_export_artifact WHERE expires_at <= CURRENT_TIMESTAMP");
    }

    private Object protect(Object value) {
        if (value == null) return null;
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> masked = new LinkedHashMap<>();
            map.forEach((key, item) -> masked.put(String.valueOf(key), protectField(String.valueOf(key), item)));
            return masked;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> masked = new ArrayList<>();
            iterable.forEach(item -> masked.add(protect(item)));
            return masked;
        }
        if (value.getClass().isArray()) {
            return objectMapper.convertValue(value, List.class).stream().map(this::protect).toList();
        }
        if (value instanceof CharSequence text) {
            return sanitizeFreeText(CpfMasking.truncate(text.toString(), 65536));
        }
        return value;
    }

    private Object protectField(String key, Object value) {
        String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "").replace(".", "");
        if (SECRET_KEYS.contains(normalized)) return "***MASKED***";
        if (PII_KEYS.contains(normalized) && value != null) return CpfMasking.mask(String.valueOf(value));
        return protect(value);
    }

    private static String sanitizeFreeText(String value) {
        if (value == null || value.isBlank()) return value;
        return value
                .replaceAll("(?i)(authorization|password|passwd|token|secret|cookie|client_secret)\\s*[:=]\\s*[^,;\\s]+", "$1=***MASKED***")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/-]+=*", "Bearer ***MASKED***");
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("로그 Export JSON을 생성하지 못했습니다.", ex);
        }
    }

    private long parseLogId(String value) {
        try {
            long id = Long.parseLong(required(value, "로그 ID가 필요합니다."));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new CpfValidationException("유효한 숫자 로그 ID가 필요합니다.");
        }
    }

    private String normalizeAction(String value) {
        String action = required(value, "Export 동작이 필요합니다.").toUpperCase(Locale.ROOT);
        if (!Set.of("CLIPBOARD", "DOWNLOAD").contains(action)) {
            throw new CpfValidationException("지원하지 않는 로그 Export 동작입니다.");
        }
        return action;
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new CpfValidationException(message);
        return value.trim();
    }

    private record Artifact(String exportId, String actor, String fileName, byte[] content,
            LocalDateTime expiresAt, String status) {
        private Artifact { content = content == null ? new byte[0] : content.clone(); }
        @Override public byte[] content() { return content.clone(); }
    }

    public record DownloadArtifact(String fileName, byte[] content) {
        public DownloadArtifact { content = content == null ? new byte[0] : content.clone(); }
        @Override public byte[] content() { return content.clone(); }
    }
}
