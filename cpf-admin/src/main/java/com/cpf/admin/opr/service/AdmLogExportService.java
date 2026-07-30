package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmLogExportRequest;
import com.cpf.admin.opr.dto.AdmLogExportResponse;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.security.CpfMasking;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 거래 로그 상세를 서버에서 마스킹하고 감사 가능한 만료 Artifact로 생성합니다.
 * 브라우저가 화면에 보인 객체를 임의로 Blob으로 내려받는 경로를 허용하지 않습니다.
 */
@Service
public class AdmLogExportService {
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final long MAX_ARTIFACT_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> SECRET_KEYS = Set.of(
            "password", "passwd", "authorization", "cookie", "set-cookie", "token", "secret",
            "privatekey", "private_key", "clientsecret", "client_secret", "credential");
    private static final Set<String> PII_KEYS = Set.of(
            "memberno", "member_no", "customerno", "customer_no", "mobile", "mobileno", "mobile_no",
            "email", "residentno", "resident_no", "accountno", "account_no", "cardno", "card_no");

    private final AdmLogQueryService logQueryService;
    private final AdmAuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Path root;
    private final ConcurrentHashMap<String, Artifact> artifacts = new ConcurrentHashMap<>();

    public AdmLogExportService(
            AdmLogQueryService logQueryService,
            AdmAuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this(logQueryService, auditLogService, objectMapper, Clock.systemUTC(),
                Path.of(System.getProperty("java.io.tmpdir"), "cpf", "adm", "log-exports"));
    }

    AdmLogExportService(
            AdmLogQueryService logQueryService,
            AdmAuditLogService auditLogService,
            ObjectMapper objectMapper,
            Clock clock,
            Path root) {
        this.logQueryService = logQueryService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.root = root.toAbsolutePath().normalize();
    }

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
        envelope.put("schemaVersion", 1);
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
            Path artifactPath = persist(exportId, bytes);
            artifacts.put(exportId, new Artifact(exportId, operator, artifactPath, fileName, expiresAt));
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

    public DownloadArtifact read(String exportId, String actor, String clientIp) {
        String operator = required(actor, "인증 운영자 정보가 없습니다.");
        Artifact artifact = artifacts.get(required(exportId, "Export ID가 필요합니다."));
        if (artifact == null) throw new CpfValidationException("Export Artifact가 없거나 이미 만료되었습니다.");
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        if (now.isAfter(artifact.expiresAt())) {
            remove(artifact);
            throw new CpfValidationException("Export Artifact가 만료되었습니다.");
        }
        if (!artifact.actor().equals(operator)) {
            auditLogService.record(null, operator, "LOG_DETAIL_EXPORT_DENIED", "LOG_EXPORT", exportId,
                    "다른 운영자의 Export Artifact 접근", null, "ownerMismatch", "LOG_EXPORT", clientIp);
            throw new CpfValidationException("다른 운영자의 Export Artifact에는 접근할 수 없습니다.");
        }
        try {
            byte[] content = Files.readAllBytes(artifact.path());
            auditLogService.record(null, operator, "LOG_DETAIL_EXPORT_DOWNLOAD", "LOG_EXPORT", exportId,
                    "감사된 로그 Export 다운로드", null, "bytes=" + content.length,
                    "LOG_EXPORT", clientIp);
            return new DownloadArtifact(artifact.fileName(), content);
        } catch (IOException ex) {
            throw new IllegalStateException("Export Artifact를 읽지 못했습니다.", ex);
        }
    }

    @Scheduled(fixedDelayString = "${cpf.admin.log-export.cleanup-delay-ms:60000}")
    public void cleanupExpired() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        new ArrayList<>(artifacts.values()).stream()
                .filter(value -> now.isAfter(value.expiresAt()))
                .forEach(this::remove);
    }

    private Path persist(String exportId, byte[] content) {
        try {
            Files.createDirectories(root);
            Path target = root.resolve(exportId + ".json").normalize();
            if (!target.startsWith(root)) throw new SecurityException("잘못된 Export 경로입니다.");
            Path temporary = root.resolve(exportId + ".tmp").normalize();
            Files.write(temporary, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            try {
                return Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnsupported) {
                return Files.move(temporary, target);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Export Artifact를 생성하지 못했습니다.", ex);
        }
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
        if (value instanceof CharSequence text) return CpfMasking.truncate(text.toString(), 65536);
        return value;
    }

    private Object protectField(String key, Object value) {
        String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace(".", "");
        if (SECRET_KEYS.contains(normalized)) return "***MASKED***";
        if (PII_KEYS.contains(normalized) && value != null) return CpfMasking.mask(String.valueOf(value));
        return protect(value);
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

    private void remove(Artifact artifact) {
        artifacts.remove(artifact.exportId(), artifact);
        try { Files.deleteIfExists(artifact.path()); } catch (IOException ignored) { /* 다음 정리 주기 재확인 */ }
    }

    private record Artifact(String exportId, String actor, Path path, String fileName, LocalDateTime expiresAt) {}
    public record DownloadArtifact(String fileName, byte[] content) {
        public DownloadArtifact { content = content == null ? new byte[0] : content.clone(); }
        @Override public byte[] content() { return content.clone(); }
    }
}
