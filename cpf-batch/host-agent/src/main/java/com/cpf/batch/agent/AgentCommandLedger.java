package com.cpf.batch.agent;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Host Agent 명령의 멱등성과 결과 불명 복구를 위한 영속 Ledger입니다.
 * 각 명령은 실행 전에 EXECUTING으로 기록되고 최종 결과는 원자적으로 교체됩니다.
 */
public final class AgentCommandLedger {
    private final Path root;
    private final ObjectMapper mapper;
    private final long retentionSeconds;
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public AgentCommandLedger(AgentProperties properties, ObjectMapper objectMapper) {
        if (properties.getCommandLedgerRoot() == null || properties.getCommandLedgerRoot().isBlank()) {
            throw new IllegalStateException("cpf.agent.command-ledger-root is required");
        }
        this.root = Path.of(properties.getCommandLedgerRoot()).toAbsolutePath().normalize();
        this.mapper = objectMapper.copy();
        this.retentionSeconds = properties.getCommandLedgerRetentionSeconds();
        if (retentionSeconds < 3_600L) throw new IllegalStateException("command ledger retention must be at least one hour");
        try { Files.createDirectories(root); }
        catch (IOException failure) { throw new IllegalStateException("Agent command ledger directory cannot be created: " + root, failure); }
    }

    public AgentCommandResult execute(
            String commandId,
            String fingerprint,
            String serviceId,
            String commandType,
            CommandAction action) {
        validateId(commandId);
        Object lock = locks.computeIfAbsent(commandId, ignored -> new Object());
        try {
            synchronized (lock) {
            Optional<Entry> existing = loadEntry(commandId);
            if (existing.isPresent()) {
                Entry entry = existing.get();
                if (!entry.fingerprint().equals(fingerprint)) {
                    throw new SecurityException("Idempotency key was reused with a different request");
                }
                if (terminal(entry.result().state())) return entry.result();
                return unknown(entry.result(), "COMMAND_EXECUTION_INTERRUPTED", "Previous execution did not persist a terminal result");
            }
            Instant startedAt = Instant.now();
            AgentCommandResult executing = new AgentCommandResult(
                    commandId, serviceId, commandType, CommandState.EXECUTING,
                    "EXECUTING", "Command execution started", null, startedAt, startedAt);
            store(new Entry(fingerprint, executing));
            AgentCommandResult result;
            try {
                result = action.run(commandId, startedAt);
                if (result == null || !commandId.equals(result.commandId())) {
                    result = unknown(executing, "INVALID_COMMAND_RESULT", "Command handler returned no matching result");
                }
            } catch (Exception failure) {
                result = new AgentCommandResult(
                        commandId, serviceId, commandType, CommandState.FAILED,
                        "COMMAND_HANDLER_FAILED", SensitiveAgentMessage.sanitize(failure.getMessage()),
                        null, startedAt, Instant.now());
            }
            store(new Entry(fingerprint, result));
            return result;
            }
        } finally {
            locks.remove(commandId, lock);
        }
    }

    public Optional<AgentCommandResult> find(String commandId) {
        validateId(commandId);
        return loadEntry(commandId).map(Entry::result);
    }

    public int purgeExpired() {
        Instant threshold = Instant.now().minus(retentionSeconds, ChronoUnit.SECONDS);
        int removed = 0;
        try (var paths = Files.list(root)) {
            for (Path path : paths.filter(candidate -> candidate.getFileName().toString().endsWith(".json")).toList()) {
                try {
                    Entry entry = mapper.readValue(path.toFile(), Entry.class);
                    if (entry.result().finishedAt() != null && entry.result().finishedAt().isBefore(threshold)
                            && terminal(entry.result().state()) && Files.deleteIfExists(path)) removed++;
                } catch (Exception ignored) {
                    // Corrupt evidence is retained for operator inspection; it is never silently deleted.
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger purge failed", failure);
        }
        return removed;
    }

    private Optional<Entry> loadEntry(String commandId) {
        Path path = path(commandId);
        if (!Files.isRegularFile(path)) return Optional.empty();
        try { return Optional.of(mapper.readValue(path.toFile(), Entry.class)); }
        catch (IOException failure) { throw new IllegalStateException("Agent command ledger entry is unreadable: " + commandId, failure); }
    }

    private void store(Entry entry) {
        String commandId = entry.result().commandId();
        Path target = path(commandId);
        Path temporary = null;
        try {
            temporary = Files.createTempFile(root, commandId + ".", ".tmp");
            byte[] bytes = mapper.writeValueAsBytes(entry);
            Files.write(temporary, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger write failed: " + commandId, failure);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException ignored) { /* durable result or primary write failure remains authoritative */ }
            }
        }
    }

    private Path path(String commandId) {
        Path resolved = root.resolve(commandId + ".json").normalize();
        if (!resolved.startsWith(root)) throw new SecurityException("Invalid command id path");
        return resolved;
    }

    private static void validateId(String commandId) {
        if (commandId == null || !commandId.matches("batcmd-[0-9a-f]{64}")) {
            throw new IllegalArgumentException("X-CPF-Command-ID must be a stable CPF command id");
        }
    }

    private static boolean terminal(CommandState state) {
        return state == CommandState.SUCCEEDED || state == CommandState.FAILED
                || state == CommandState.UNKNOWN_RESULT || state == CommandState.ROLLED_BACK
                || state == CommandState.PARTIALLY_ROLLED_BACK;
    }

    private static AgentCommandResult unknown(AgentCommandResult source, String code, String message) {
        return new AgentCommandResult(source.commandId(), source.serviceId(), source.commandType(),
                CommandState.UNKNOWN_RESULT, code, message, source.activeVersion(), source.startedAt(), Instant.now());
    }

    @FunctionalInterface
    public interface CommandAction {
        AgentCommandResult run(String commandId, Instant startedAt) throws Exception;
    }

    public record Entry(String fingerprint, AgentCommandResult result) {
        public Entry {
            if (fingerprint == null || !fingerprint.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("fingerprint is invalid");
            if (result == null) throw new IllegalArgumentException("result is required");
        }
    }

    /** Minimal sanitizer to avoid depending on process output or credentials in the durable ledger. */
    static final class SensitiveAgentMessage {
        private SensitiveAgentMessage() { }
        static String sanitize(String value) {
            if (value == null || value.isBlank()) return "Command failed";
            String safe = value.replaceAll("(?i)(token|password|secret|authorization|cookie|session)\\s*[:=]\\s*[^\\s,;]+", "$1=***");
            return safe.length() > 512 ? safe.substring(0, 512) : safe;
        }
    }
}
