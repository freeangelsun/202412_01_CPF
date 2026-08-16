package com.cpf.gateway.scg;

import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
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

/** 응답을 오염시키지 않으면서 Ledger 실패를 bounded durable spool에 보존·재처리합니다. */
@Component
public final class CpfGatewayLedgerRecoverySpool {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayLedgerRecoverySpool.class);

    private final CpfGatewayLedgerPort ledger;
    private final ObjectMapper mapper;
    private final Path directory;
    private final long capBytes;
    private final ReentrantLock spoolLock = new ReentrantLock();

    public CpfGatewayLedgerRecoverySpool(
            CpfGatewayLedgerPort ledger,
            ObjectMapper mapper,
            CpfGatewaySafetyProperties properties) {
        this.ledger = ledger;
        this.mapper = mapper.copy()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        this.directory = Path.of(properties.getLogSpoolDirectory())
                .toAbsolutePath()
                .normalize()
                .resolve("ledger-recovery");
        this.capBytes = properties.getLogSpoolBytesCap();
        if (capBytes < 1_024L) {
            throw new IllegalArgumentException("Gateway ledger recovery cap must be at least 1024");
        }
    }

    public void begin(CpfGatewayLedgerPort.TransactionStart event) {
        observe("BEGIN", event, () -> ledger.begin(event));
    }

    public void recordAttempt(CpfGatewayLedgerPort.Attempt event) {
        observe("ATTEMPT", event, () -> ledger.recordAttempt(event));
    }

    public void recordCapture(CpfGatewayLedgerPort.CaptureSegment event) {
        observe("CAPTURE", event, () -> ledger.recordCapture(event));
    }

    public void complete(CpfGatewayLedgerPort.TransactionCompletion event) {
        observe("COMPLETE", event, () -> ledger.complete(event));
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.ledger-recovery-interval-ms:5000}")
    public void replay() {
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .limit(100)
                    .forEach(this::replayOne);
        } catch (IOException failure) {
            log.error("CPF Gateway ledger recovery scan failed", failure);
        }
    }

    private void observe(String type, Object payload, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException failure) {
            spool(type, payload, failure);
        }
    }

    private void replayOne(Path path) {
        try {
            RecoveryEvent event = readEvent(path);
            switch (event.type()) {
                case "BEGIN" -> ledger.begin(mapper.readValue(
                        event.payloadJson(), CpfGatewayLedgerPort.TransactionStart.class));
                case "ATTEMPT" -> ledger.recordAttempt(mapper.readValue(
                        event.payloadJson(), CpfGatewayLedgerPort.Attempt.class));
                case "CAPTURE" -> ledger.recordCapture(mapper.readValue(
                        event.payloadJson(), CpfGatewayLedgerPort.CaptureSegment.class));
                case "COMPLETE" -> ledger.complete(mapper.readValue(
                        event.payloadJson(), CpfGatewayLedgerPort.TransactionCompletion.class));
                default -> throw new IllegalStateException(
                        "Unsupported Gateway ledger recovery event type: " + event.type());
            }
            Files.deleteIfExists(path);
        } catch (Exception failure) {
            log.warn("CPF Gateway ledger recovery retry failed: {}", path.getFileName(), failure);
        }
    }

    private RecoveryEvent readEvent(Path path) throws IOException {
        long size = Files.size(path);
        if (size < 1L || size > capBytes) {
            throw new IOException("Gateway ledger recovery event size is invalid: " + size);
        }
        try (InputStream input = new BoundedInputStream(
                Files.newInputStream(path, StandardOpenOption.READ), capBytes)) {
            return mapper.readValue(input, RecoveryEvent.class);
        }
    }

    private void spool(String type, Object payload, RuntimeException original) {
        Path temporary = null;
        spoolLock.lock();
        try {
            Files.createDirectories(directory);
            String payloadJson = mapper.writeValueAsString(payload);
            Instant recordedAt = Instant.now();
            RecoveryEvent event = new RecoveryEvent(1, type, payloadJson, recordedAt, sanitize(original));
            byte[] bytes = (mapper.writeValueAsString(event) + "\n").getBytes(StandardCharsets.UTF_8);
            long current = currentBytes();
            if (bytes.length > capBytes - current) {
                throw new IllegalStateException("CPF_GATEWAY_LEDGER_RECOVERY_SPOOL_FULL");
            }
            Path target = directory.resolve(fileName(recordedAt, UUID.randomUUID()));
            temporary = Files.createTempFile(directory, "ledger-", ".tmp");
            Files.write(temporary, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target);
            }
        } catch (Exception spoolFailure) {
            original.addSuppressed(spoolFailure);
            log.error("CPF Gateway ledger failure could not be spooled: {} / {}",
                    sanitize(original), sanitize(spoolFailure));
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // 원 Ledger 실패가 primary입니다.
                }
            }
            spoolLock.unlock();
        }
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
            return 0L;
        }
    }

    private static String fileName(Instant instant, UUID id) {
        return String.format("%013d-%09d-%s.json", instant.toEpochMilli(), instant.getNano(), id);
    }

    private static String sanitize(Throwable failure) {
        String value = failure.getClass().getSimpleName() + ":" + String.valueOf(failure.getMessage());
        value = value.replaceAll(
                "(?i)(token|password|secret|authorization)[=: ]+[^,;\\s]+", "$1=***");
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    public record RecoveryEvent(
            int schemaVersion,
            String type,
            String payloadJson,
            Instant recordedAt,
            String failureSummary) {}

    private static final class BoundedInputStream extends FilterInputStream {
        private final long limit;
        private long observed;

        private BoundedInputStream(InputStream input, long limit) {
            super(input);
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value >= 0) requireBudget(1L);
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int read = super.read(bytes, offset, length);
            if (read > 0) requireBudget(read);
            return read;
        }

        private void requireBudget(long increment) throws IOException {
            observed += increment;
            if (observed > limit) {
                throw new IOException("Gateway ledger recovery event exceeds configured limit");
            }
        }
    }
}
