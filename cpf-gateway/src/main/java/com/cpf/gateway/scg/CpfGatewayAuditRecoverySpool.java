package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayAuditEvent;
import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Audit 저장 장애가 원 거래를 오염시키지 않도록 bounded durable spool로 격리합니다. */
@Component
public final class CpfGatewayAuditRecoverySpool {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayAuditRecoverySpool.class);
    private final CpfGatewayAuditPort audit;
    private final ObjectMapper mapper;
    private final Path directory;
    private final long capBytes;
    private final ReentrantLock spoolLock = new ReentrantLock();

    public CpfGatewayAuditRecoverySpool(
            CpfGatewayAuditPort audit,
            ObjectMapper mapper,
            CpfGatewaySafetyProperties properties) {
        this.audit = audit;
        this.mapper = mapper;
        this.directory = Path.of(properties.getLogSpoolDirectory())
                .toAbsolutePath()
                .normalize()
                .resolve("audit-recovery");
        this.capBytes = properties.getLogSpoolBytesCap();
    }

    public void record(CpfGatewayAuditEvent event) {
        try {
            audit.record(event);
        } catch (RuntimeException failure) {
            spool(event, failure);
        }
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.audit-recovery-interval-ms:5000}")
    public void replay() {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (var files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .limit(100)
                    .forEach(this::replayOne);
        } catch (IOException failure) {
            log.error("CPF Gateway audit recovery scan failed", failure);
        }
    }

    private void replayOne(Path path) {
        try {
            RecoveryEvent event = mapper.readValue(path.toFile(), RecoveryEvent.class);
            audit.record(mapper.readValue(event.payloadJson(), CpfGatewayAuditEvent.class));
            Files.deleteIfExists(path);
        } catch (Exception failure) {
            log.warn("CPF Gateway audit recovery retry failed: {}", path.getFileName(), failure);
        }
    }

    private void spool(CpfGatewayAuditEvent event, RuntimeException original) {
        Path temporary = null;
        spoolLock.lock();
        try {
            Files.createDirectories(directory);
            String payload = mapper.writeValueAsString(event);
            RecoveryEvent recovery = new RecoveryEvent(1, payload, Instant.now(), sanitize(original));
            byte[] bytes = (mapper.writeValueAsString(recovery) + "\n").getBytes(StandardCharsets.UTF_8);
            if (currentBytes() + bytes.length > capBytes) {
                throw new IllegalStateException("CPF_GATEWAY_AUDIT_RECOVERY_SPOOL_FULL");
            }
            Instant recordedAt = Instant.now();
            Path target = directory.resolve(fileName(recordedAt, UUID.randomUUID()));
            temporary = Files.createTempFile(directory, "audit-", ".tmp");
            Files.write(temporary, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target);
            }
        } catch (Exception spoolFailure) {
            original.addSuppressed(spoolFailure);
            log.error("CPF Gateway audit failure could not be spooled: {} / {}",
                    sanitize(original), sanitize(spoolFailure));
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // 원 Audit 실패가 primary입니다.
                }
            }
            spoolLock.unlock();
        }
    }

    private static String fileName(Instant instant, UUID id) {
        return String.format("%013d-%09d-%s.json", instant.toEpochMilli(), instant.getNano(), id);
    }

    private long currentBytes() throws IOException {
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).mapToLong(this::sizeQuietly).sum();
        }
    }

    private long sizeQuietly(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ignored) {
            return 0;
        }
    }

    private static String sanitize(Throwable failure) {
        String value = failure.getClass().getSimpleName() + ":" + String.valueOf(failure.getMessage());
        value = value.replaceAll(
                "(?i)(token|password|secret|authorization)[=: ]+[^,;\\s]+", "$1=***");
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    public record RecoveryEvent(
            int schemaVersion,
            String payloadJson,
            Instant recordedAt,
            String failureSummary) {}
}
