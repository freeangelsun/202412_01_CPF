package com.cpf.tools.audit;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Java 21 대체 Runtime용 durable file store. Production DB 구현을 대체하지 않으며 다중 JVM/kill/restart 계약만 검증합니다. */
public final class AuditRuntimeStore {
    private static final Pattern SECRET = Pattern.compile("(?i)(password|passwd|pwd|token|secret|authorization|ssn|residentNumber)\\s*[=:]\\s*([^,;\\s]+)");
    private final Path root;
    private final Path lockFile;
    private final Path recordsFile;

    public AuditRuntimeStore(Path root) {
        this.root = root;
        this.lockFile = root.resolve("audit.lock");
        this.recordsFile = root.resolve("audit.records");
    }

    public void initialize() throws IOException {
        Files.createDirectories(root);
        if (!Files.isDirectory(root)) throw new IOException("audit root is not a directory: " + root);
        // CREATE is intentionally idempotent. A check-then-CREATE_NEW sequence races when two JVMs start together.
        try (FileChannel ignored = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            // The lock file content is irrelevant; the channel is used only for the OS file lock.
        }
        try (FileChannel ignored = FileChannel.open(recordsFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            // Opening without TRUNCATE_EXISTING preserves records across restart.
        }
    }

    public AppendResult append(AuditCommand command) {
        AuditCommand c = command.normalized();
        try {
            initialize();
            try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.WRITE);
                 FileLock ignored = channel.lock()) {
                List<AuditRecord> current = readUnlocked();
                AuditRecord existing = current.stream().filter(r -> r.deliveryId().equals(c.deliveryId())).findFirst().orElse(null);
                if (existing != null) return new AppendResult(existing.auditId(), false);
                long nextId = current.stream().mapToLong(AuditRecord::auditId).max().orElse(0L) + 1L;
                AuditRecord record = new AuditRecord(nextId, c.deliveryId(), c.transactionId(), c.traceId(), c.executionId(),
                        c.instanceId(), c.sequence(), mask(c.reason()), Instant.now().toString(), c.sourceHead());
                String line = encode(record) + System.lineSeparator();
                Files.writeString(recordsFile, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                return new AppendResult(nextId, true);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("AUDIT_WRITE_FAILED: durable audit append failed", ex);
        }
    }

    public List<AuditRecord> readAll() {
        try {
            if (!Files.isRegularFile(recordsFile)) throw new IOException("audit records missing: " + recordsFile);
            try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.WRITE);
                 FileLock ignored = channel.lock()) {
                return List.copyOf(readUnlocked());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("AUDIT_READ_FAILED: database/storage failure must not become an empty result", ex);
        }
    }

    private List<AuditRecord> readUnlocked() throws IOException {
        if (!Files.isRegularFile(recordsFile)) throw new IOException("audit records missing: " + recordsFile);
        List<AuditRecord> result = new ArrayList<>();
        for (String line : Files.readAllLines(recordsFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            result.add(decode(line));
        }
        result.sort(Comparator.comparingLong(AuditRecord::auditId));
        return result;
    }

    public static Validation validate(List<AuditRecord> records, int expectedUnique) {
        Map<String, AuditRecord> byDelivery = new LinkedHashMap<>();
        Map<Long, AuditRecord> byId = new HashMap<>();
        List<String> failures = new ArrayList<>();
        long previous = 0;
        for (AuditRecord record : records) {
            if (byDelivery.putIfAbsent(record.deliveryId(), record) != null) failures.add("duplicate deliveryId=" + record.deliveryId());
            if (byId.putIfAbsent(record.auditId(), record) != null) failures.add("duplicate auditId=" + record.auditId());
            if (record.auditId() <= previous) failures.add("auditId order error=" + record.auditId());
            previous = record.auditId();
            if (record.transactionId().isBlank() || record.traceId().isBlank() || record.executionId().isBlank() || record.instanceId().isBlank()) {
                failures.add("tracking field missing deliveryId=" + record.deliveryId());
            }
            String lower = record.reason().toLowerCase();
            if (lower.contains("supersecret") || lower.contains("rawtoken") || lower.contains("900101-1234567")) {
                failures.add("unmasked sensitive value deliveryId=" + record.deliveryId());
            }
        }
        if (byDelivery.size() != expectedUnique) failures.add("record count expected=" + expectedUnique + " actual=" + byDelivery.size());
        return new Validation(failures.isEmpty(), List.copyOf(failures), byDelivery.size());
    }

    public static String mask(String value) {
        if (value == null) return "";
        return SECRET.matcher(value).replaceAll("$1=***");
    }

    private static String encode(AuditRecord r) {
        return String.join("\t", Long.toString(r.auditId()), esc(r.deliveryId()), esc(r.transactionId()), esc(r.traceId()),
                esc(r.executionId()), esc(r.instanceId()), Integer.toString(r.sequence()), esc(r.reason()), esc(r.createdAt()), esc(r.sourceHead()));
    }

    private static AuditRecord decode(String line) throws IOException {
        String[] p = line.split("\\t", -1);
        if (p.length != 10) throw new IOException("corrupt audit record field count=" + p.length);
        try {
            return new AuditRecord(Long.parseLong(p[0]), unesc(p[1]), unesc(p[2]), unesc(p[3]), unesc(p[4]), unesc(p[5]),
                    Integer.parseInt(p[6]), unesc(p[7]), unesc(p[8]), unesc(p[9]));
        } catch (RuntimeException ex) {
            throw new IOException("corrupt audit record", ex);
        }
    }
    private static String esc(String v) { return v.replace("%", "%25").replace("\t", "%09").replace("\r", "%0D").replace("\n", "%0A"); }
    private static String unesc(String v) { return v.replace("%0A", "\n").replace("%0D", "\r").replace("%09", "\t").replace("%25", "%"); }

    public record AuditCommand(String deliveryId, String transactionId, String traceId, String executionId, String instanceId,
                               int sequence, String reason, String sourceHead) {
        AuditCommand normalized() {
            return new AuditCommand(required(deliveryId, "deliveryId"), required(transactionId, "transactionId"),
                    required(traceId, "traceId"), required(executionId, "executionId"), required(instanceId, "instanceId"),
                    sequence, reason == null ? "" : reason, required(sourceHead, "sourceHead"));
        }
        private static String required(String value, String name) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
            return value.trim();
        }
    }
    public record AuditRecord(long auditId, String deliveryId, String transactionId, String traceId, String executionId,
                              String instanceId, int sequence, String reason, String createdAt, String sourceHead) {}
    public record AppendResult(long auditId, boolean inserted) {}
    public record Validation(boolean passed, List<String> failures, int uniqueCount) {}
}
