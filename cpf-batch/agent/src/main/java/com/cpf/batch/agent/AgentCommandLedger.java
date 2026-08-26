package com.cpf.batch.agent;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Host Agent 명령의 멱등성과 결과 불명 복구를 위한 영속 Ledger입니다.
 * 명령 전체를 JVM-local lock과 OS file lock으로 감싸 다중 Agent process에서도
 * 동일 command id의 side effect가 중복 실행되지 않도록 합니다.
 */
public final class AgentCommandLedger {
    private static final int LOCK_STRIPES = 64;

    private final Path root;
    private final ObjectMapper mapper;
    private final long retentionSeconds;
    private static final ReentrantLock[] JVM_LOCKS = new ReentrantLock[LOCK_STRIPES];

    static {
        for (int index = 0; index < JVM_LOCKS.length; index++) {
            JVM_LOCKS[index] = new ReentrantLock();
        }
    }

    public AgentCommandLedger(AgentProperties properties, ObjectMapper objectMapper) {
        if (properties.getCommandLedgerRoot() == null || properties.getCommandLedgerRoot().isBlank()) {
            throw new IllegalStateException("cpf.agent.command-ledger-root is required");
        }
        Path configuredRoot = Path.of(properties.getCommandLedgerRoot()).toAbsolutePath().normalize();
        try {
            if (Files.exists(configuredRoot, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(configuredRoot)) {
                throw new SecurityException("Agent command ledger root must not be a symbolic link");
            }
            Files.createDirectories(configuredRoot);
            this.root = configuredRoot.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger directory cannot be created: " + configuredRoot, failure);
        }
        this.mapper = java.util.Objects.requireNonNull(objectMapper, "objectMapper").copy();
        this.retentionSeconds = properties.getCommandLedgerRetentionSeconds();
        if (retentionSeconds < 3_600L) {
            throw new IllegalStateException("command ledger retention must be at least one hour");
        }
    }

    public AgentCommandResult execute(
            String commandId,
            String fingerprint,
            String serviceId,
            String commandType,
            CommandAction action) {
        validateId(commandId);
        if (fingerprint == null || !fingerprint.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("fingerprint is invalid");
        }
        java.util.Objects.requireNonNull(action, "action");
        return withCommandLock(commandId, () -> {
            Optional<Entry> existing = loadEntry(commandId);
            if (existing.isPresent()) {
                Entry entry = existing.get();
                if (!entry.fingerprint().equals(fingerprint)) {
                    throw new SecurityException("Idempotency key was reused with a different request");
                }
                if (terminal(entry.result().state())) {
                    return entry.result();
                }
                AgentCommandResult interrupted = unknown(
                        entry.result(),
                        "COMMAND_EXECUTION_INTERRUPTED",
                        "Previous execution did not persist a terminal result");
                store(new Entry(fingerprint, interrupted));
                return interrupted;
            }

            Instant startedAt = Instant.now();
            AgentCommandResult executing = new AgentCommandResult(
                    commandId,
                    serviceId,
                    commandType,
                    CommandState.EXECUTING,
                    "EXECUTING",
                    "Command execution started",
                    null,
                    startedAt,
                    startedAt);
            store(new Entry(fingerprint, executing));

            AgentCommandResult result;
            try {
                result = action.run(commandId, startedAt);
                if (result == null || !commandId.equals(result.commandId())) {
                    result = unknown(executing, "INVALID_COMMAND_RESULT", "Command handler returned no matching result");
                } else if (!terminal(result.state())) {
                    result = unknown(result, "NON_TERMINAL_COMMAND_RESULT", "Command handler returned a non-terminal result");
                }
            } catch (Exception failure) {
                result = unknown(
                        executing,
                        "COMMAND_HANDLER_RESULT_UNKNOWN",
                        SensitiveAgentMessage.sanitize(failure.getMessage()));
            }
            store(new Entry(fingerprint, result));
            return result;
        });
    }

    public Optional<AgentCommandResult> find(String commandId) {
        validateId(commandId);
        return loadEntry(commandId).map(value -> value.result());
    }

    public int purgeExpired() {
        Instant threshold = Instant.now().minus(retentionSeconds, ChronoUnit.SECONDS);
        int removed = 0;
        try (var paths = Files.list(root)) {
            for (Path candidate : paths.filter(this::isLedgerEntryCandidate).toList()) {
                String fileName = candidate.getFileName().toString();
                String commandId = fileName.substring(0, fileName.length() - ".json".length());
                boolean deleted = withCommandLock(commandId, () -> {
                    Optional<Entry> loaded = loadEntry(commandId);
                    if (loaded.isEmpty()) {
                        return false;
                    }
                    AgentCommandResult result = loaded.get().result();
                    return result.finishedAt() != null
                            && result.finishedAt().isBefore(threshold)
                            && terminal(result.state())
                            && deleteEntry(path(commandId));
                });
                if (deleted) {
                    removed++;
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger purge failed", failure);
        }
        return removed;
    }

    private boolean isLedgerEntryCandidate(Path candidate) {
        String name = candidate.getFileName().toString();
        return name.endsWith(".json")
                && name.substring(0, name.length() - ".json".length()).matches("batcmd-[0-9a-f]{64}");
    }

    private Optional<Entry> loadEntry(String commandId) {
        Path entryPath = path(commandId);
        if (!Files.exists(entryPath, LinkOption.NOFOLLOW_LINKS)) {
            return Optional.empty();
        }
        if (Files.isSymbolicLink(entryPath) || !Files.isRegularFile(entryPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("Agent command ledger entry is not a regular file: " + commandId);
        }
        try {
            return Optional.of(mapper.readValue(entryPath.toFile(), Entry.class));
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger entry is unreadable: " + commandId, failure);
        }
    }

    private void store(Entry entry) {
        String commandId = entry.result().commandId();
        Path target = path(commandId);
        Path temporary = null;
        try {
            temporary = Files.createTempFile(root, commandId + ".", ".tmp");
            byte[] bytes = mapper.writeValueAsBytes(entry);
            try (FileChannel channel = FileChannel.open(
                    temporary,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    LinkOption.NOFOLLOW_LINKS)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger write failed: " + commandId, failure);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Durable result or primary write failure remains authoritative.
                }
            }
        }
    }

    private boolean deleteEntry(Path entryPath) {
        try {
            if (Files.isSymbolicLink(entryPath)) {
                throw new SecurityException("Agent command ledger entry must not be a symbolic link");
            }
            return Files.deleteIfExists(entryPath);
        } catch (IOException failure) {
            throw new IllegalStateException("Agent command ledger purge failed for " + entryPath.getFileName(), failure);
        }
    }

    private <T> T withCommandLock(String commandId, LockedOperation<T> operation) {
        int stripe = Math.floorMod(java.util.Objects.hash(root, commandId), JVM_LOCKS.length);
        ReentrantLock localLock = JVM_LOCKS[stripe];
        localLock.lock();
        try {
            Path lockPath = lockPath(commandId);
            if (Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(lockPath)) {
                throw new SecurityException("Agent command ledger lock must not be a symbolic link");
            }
            try (FileChannel channel = FileChannel.open(
                            lockPath,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            LinkOption.NOFOLLOW_LINKS);
                    FileLock _ = channel.lock()) {
                return operation.run();
            } catch (IOException failure) {
                throw new IllegalStateException("Agent command ledger lock failed: " + commandId, failure);
            }
        } finally {
            localLock.unlock();
        }
    }

    private Path path(String commandId) {
        return safeChild(commandId + ".json");
    }

    private Path lockPath(String commandId) {
        return safeChild(commandId + ".lock");
    }

    private Path safeChild(String fileName) {
        Path resolved = root.resolve(fileName).normalize();
        if (!resolved.startsWith(root)) {
            throw new SecurityException("Invalid command id path");
        }
        return resolved;
    }

    private static void validateId(String commandId) {
        if (commandId == null || !commandId.matches("batcmd-[0-9a-f]{64}")) {
            throw new IllegalArgumentException("X-CPF-Command-ID must be a stable CPF command id");
        }
    }

    private static boolean terminal(CommandState state) {
        return state == CommandState.SUCCEEDED
                || state == CommandState.FAILED
                || state == CommandState.UNKNOWN_RESULT
                || state == CommandState.ROLLED_BACK
                || state == CommandState.PARTIALLY_ROLLED_BACK;
    }

    private static AgentCommandResult unknown(AgentCommandResult source, String code, String message) {
        return new AgentCommandResult(
                source.commandId(),
                source.serviceId(),
                source.commandType(),
                CommandState.UNKNOWN_RESULT,
                code,
                message,
                source.activeVersion(),
                source.startedAt(),
                Instant.now());
    }

    @FunctionalInterface
    public interface CommandAction {
        AgentCommandResult run(String commandId, Instant startedAt) throws Exception;
    }

    @FunctionalInterface
    private interface LockedOperation<T> {
        T run();
    }

    public record Entry(String fingerprint, AgentCommandResult result) {
        public Entry {
            if (fingerprint == null || !fingerprint.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("fingerprint is invalid");
            }
            if (result == null) {
                throw new IllegalArgumentException("result is required");
            }
        }
    }

    /** Minimal sanitizer to avoid depending on process output or credentials in the durable ledger. */
    static final class SensitiveAgentMessage {
        private SensitiveAgentMessage() { }

        static String sanitize(String value) {
            if (value == null || value.isBlank()) {
                return "Command result is unknown";
            }
            String safe = value.replaceAll(
                    "(?i)(token|password|secret|authorization|cookie|session)\\s*[:=]\\s*[^\\s,;]+",
                    "$1=***");
            return safe.length() > 512 ? safe.substring(0, 512) : safe;
        }
    }
}
