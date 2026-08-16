package com.cpf.education.operations.runtime.persistence;

import com.cpf.education.operations.runtime.application.EduConflictException;
import com.cpf.education.operations.runtime.model.EduAuditRecord;
import com.cpf.education.operations.runtime.model.EduCreateResult;
import com.cpf.education.operations.runtime.model.EduExecutionState;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
import com.cpf.education.operations.runtime.model.EduOperationRecord;
import com.cpf.education.operations.runtime.model.EduOutboxRecord;
import com.cpf.education.operations.runtime.model.EduTargetRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Small, durable EDU repository used only when the explicit file profile is selected.
 *
 * <p>The on-disk format is a versioned binary envelope containing a deterministic,
 * explicitly typed payload. Java native serialization and polymorphic/default typing
 * are deliberately unsupported. Existing Java-serialization streams are rejected
 * fail-closed; they must be exported by a trusted old runtime and recreated through
 * the normal EDU API instead of being deserialized by this implementation.</p>
 */
/** FileEduOperationRepository 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class FileEduOperationRepository implements EduOperationRepository {
    static final String STORE_FILE_NAME = "edu-operation-store.bin";
    static final String TEMP_FILE_NAME = STORE_FILE_NAME + ".tmp";
    static final int MAX_PAYLOAD_BYTES = 8 * 1024 * 1024;
    static final int HEADER_BYTES = 8 + Integer.BYTES + Integer.BYTES + 32;
    static final long MAX_STORE_BYTES = (long) HEADER_BYTES + MAX_PAYLOAD_BYTES;

    private static final byte[] MAGIC = "CPFEDU01".getBytes(StandardCharsets.US_ASCII);
    private static final int FORMAT_VERSION = 1;
    private static final int MAX_STRING_BYTES = 64 * 1024;
    private static final int MAX_OPERATIONS = 10_000;
    private static final int MAX_GROUPS = 10_000;
    private static final int MAX_GROUP_RECORDS = 50_000;
    private static final int MAX_COLLECTION_ENTRIES = 10_000;
    private static final int MAX_VALUE_DEPTH = 16;
    private static final int MAX_VALUE_NODES = 100_000;

    private static final byte VALUE_STRING = 1;
    private static final byte VALUE_BOOLEAN = 2;
    private static final byte VALUE_BYTE = 3;
    private static final byte VALUE_SHORT = 4;
    private static final byte VALUE_INTEGER = 5;
    private static final byte VALUE_LONG = 6;
    private static final byte VALUE_BIG_INTEGER = 7;
    private static final byte VALUE_FLOAT = 8;
    private static final byte VALUE_DOUBLE = 9;
    private static final byte VALUE_BIG_DECIMAL = 10;
    private static final byte VALUE_MAP = 11;
    private static final byte VALUE_LIST = 12;
    private static final byte VALUE_SET = 13;

    private static final Map<Path, ReentrantLock> JVM_LOCKS = new ConcurrentHashMap<>();

    private final Path dataFile;
    private final Path lockFile;
    private final ReentrantLock jvmLock;

    /** FileEduOperationRepository 작업을 CPF 표준 계약에 따라 수행한다. */
    public FileEduOperationRepository(Path directory) {
        Objects.requireNonNull(directory, "directory");
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(normalizedDirectory);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        dataFile = normalizedDirectory.resolve(STORE_FILE_NAME);
        lockFile = normalizedDirectory.resolve("edu-operation-store.lock");
        jvmLock = JVM_LOCKS.computeIfAbsent(lockFile, ignored -> new ReentrantLock(true));
    }

    /** Work 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private interface Work<T> {
        T apply(Snapshot snapshot);
    }

    private <T> T readWrite(boolean write, Work<T> work) {
        jvmLock.lock();
        try {
            rejectSymbolicLink(lockFile, "lock file");
            try (FileChannel channel = FileChannel.open(lockFile, CREATE, WRITE, LinkOption.NOFOLLOW_LINKS);
                    var ignored = channel.lock()) {
                Snapshot snapshot = readSnapshot();
                T result = work.apply(snapshot);
                if (write) {
                    writeSnapshot(snapshot);
                }
                return result;
            }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } finally {
            jvmLock.unlock();
        }
    }

    private Snapshot readSnapshot() {
        if (Files.notExists(dataFile, LinkOption.NOFOLLOW_LINKS)) {
            return new Snapshot();
        }
        if (!Files.exists(dataFile, LinkOption.NOFOLLOW_LINKS)) {
            throw corrupt("store existence cannot be determined", null);
        }
        rejectSymbolicLink(dataFile, "data file");
        if (!Files.isRegularFile(dataFile, LinkOption.NOFOLLOW_LINKS)) {
            throw corrupt("store is not a regular file", null);
        }

        try (FileChannel channel = FileChannel.open(dataFile, READ, LinkOption.NOFOLLOW_LINKS)) {
            long size = channel.size();
            rejectLegacyJavaSerialization(channel, size);
            if (size < HEADER_BYTES) {
                throw corrupt("store is truncated before the header", null);
            }
            if (size > MAX_STORE_BYTES) {
                throw corrupt("store exceeds the maximum allowed size", null);
            }
            channel.position(0);
            try (DataInputStream input = new DataInputStream(Channels.newInputStream(channel))) {
                byte[] magic = new byte[MAGIC.length];
                input.readFully(magic);
                if (!MessageDigest.isEqual(MAGIC, magic)) {
                    throw corrupt("store magic is invalid", null);
                }
                int version = input.readInt();
                if (version != FORMAT_VERSION) {
                    throw corrupt("unsupported store format version: " + version, null);
                }
                int payloadLength = input.readInt();
                if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_BYTES) {
                    throw corrupt("store payload length is invalid: " + payloadLength, null);
                }
                long expectedSize = HEADER_BYTES + (long) payloadLength;
                if (size != expectedSize) {
                    throw corrupt("store length mismatch: expected=" + expectedSize + " actual=" + size, null);
                }
                byte[] expectedDigest = new byte[32];
                input.readFully(expectedDigest);
                byte[] payload = new byte[payloadLength];
                input.readFully(payload);
                if (!MessageDigest.isEqual(expectedDigest, sha256(payload))) {
                    throw corrupt("store checksum mismatch", null);
                }
                return decodeSnapshot(payload);
            }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (EOFException exception) {
            throw corrupt("store is truncated", exception);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void rejectLegacyJavaSerialization(FileChannel channel, long size) throws IOException {
        if (size < 2) {
            return;
        }
        ByteBuffer prefix = ByteBuffer.allocate(2);
        channel.position(0);
        while (prefix.hasRemaining() && channel.read(prefix) >= 0) {
            // Two bytes only; continue until the prefix is complete or EOF is reached.
        }
        if (prefix.position() == 2) {
            byte[] value = prefix.array();
            if ((value[0] & 0xff) == 0xac && (value[1] & 0xff) == 0xed) {
                throw corrupt(
                        "legacy Java-serialization stores are not accepted; recreate through a trusted API export",
                        null);
            }
        }
    }

    private void writeSnapshot(Snapshot snapshot) {
        byte[] payload = encodeSnapshot(snapshot);
        byte[] digest = sha256(payload);
        Path temporary = dataFile.resolveSibling(TEMP_FILE_NAME);
        rejectSymbolicLink(temporary, "temporary data file");
        rejectSymbolicLink(dataFile, "data file");

        try {
            try (FileChannel channel = FileChannel.open(
                            temporary, CREATE, TRUNCATE_EXISTING, WRITE, LinkOption.NOFOLLOW_LINKS);
                    DataOutputStream output = new DataOutputStream(Channels.newOutputStream(channel))) {
                output.write(MAGIC);
                output.writeInt(FORMAT_VERSION);
                output.writeInt(payload.length);
                output.write(digest);
                output.write(payload);
                output.flush();
                channel.force(true);
            }
            moveIntoPlace(temporary);
            forceDataFile();
            forceParentDirectoryBestEffort();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException exception) {
            deleteTemporaryAfterFailure(temporary, exception);
            throw new UncheckedIOException(exception);
        } catch (RuntimeException exception) {
            deleteTemporaryAfterFailure(temporary, exception);
            throw exception;
        }
    }

    private void moveIntoPlace(Path temporary) throws IOException {
        try {
            Files.move(
                    temporary,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (AtomicMoveNotSupportedException exception) {
            // The old file remains intact until the fully forced temporary file is closed.
            Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void forceDataFile() throws IOException {
        try (FileChannel channel = FileChannel.open(dataFile, WRITE, LinkOption.NOFOLLOW_LINKS)) {
            channel.force(true);
        }
    }

    private void forceParentDirectoryBestEffort() {
        try (FileChannel channel = FileChannel.open(dataFile.getParent(), READ)) {
            channel.force(true);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (AccessDeniedException | UnsupportedOperationException ignored) {
            // Windows and some providers do not expose directory fsync. The data file itself was forced.
        } catch (IOException ignored) {
            // The file remains valid and forced; directory fsync support is provider-specific.
        }
    }

    private static void deleteTemporaryAfterFailure(Path temporary, Throwable failure) {
        try {
            Files.deleteIfExists(temporary);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private static byte[] encodeSnapshot(Snapshot snapshot) {
        LimitedByteArrayOutputStream bytes = new LimitedByteArrayOutputStream(MAX_PAYLOAD_BYTES);
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            writeOperations(output, snapshot.operations);
            writeAudits(output, snapshot.audits);
            writeTargets(output, snapshot.targets);
            writeOutbox(output, snapshot.outbox);
            writeLeases(output, snapshot.leases);
            output.flush();
            return bytes.toByteArray();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Snapshot decodeSnapshot(byte[] payload) {
        ReadBudget budget = new ReadBudget();
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(payload);
                DataInputStream input = new DataInputStream(bytes)) {
            Snapshot snapshot = new Snapshot();
            readOperations(input, snapshot, budget);
            readAudits(input, snapshot, budget);
            readTargets(input, snapshot, budget);
            readOutbox(input, snapshot, budget);
            readLeases(input, snapshot, budget);
            if (bytes.available() != 0) {
                throw corrupt("store contains trailing payload bytes", null);
            }
            return snapshot;
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (EOFException exception) {
            throw corrupt("store payload is truncated", exception);
        } catch (IOException | IllegalArgumentException | NullPointerException exception) {
            throw corrupt("store payload is malformed", exception);
        }
    }

    private static void writeOperations(DataOutputStream output, Map<String, EduOperationRecord> operations)
            throws IOException {
        writeCount(output, operations.size(), MAX_OPERATIONS, "operations");
        List<EduOperationRecord> sorted = operations.values().stream()
                .sorted(Comparator.comparing(EduOperationRecord::operationId))
                .toList();
        for (EduOperationRecord record : sorted) {
            writeOperation(output, record);
        }
    }

    private static void readOperations(DataInputStream input, Snapshot snapshot, ReadBudget budget)
            throws IOException {
        int count = readCount(input, MAX_OPERATIONS, "operations");
        for (int index = 0; index < count; index++) {
            budget.consume();
            EduOperationRecord record = readOperation(input, budget);
            if (snapshot.operations.putIfAbsent(record.operationId(), record) != null) {
                throw corrupt("duplicate operation id: " + record.operationId(), null);
            }
            String idempotencyKey = idempotencyKey(record);
            if (snapshot.idempotency.putIfAbsent(idempotencyKey, record.operationId()) != null) {
                throw corrupt("duplicate idempotency key: " + idempotencyKey, null);
            }
        }
    }

    private static void writeAudits(
            DataOutputStream output, Map<String, List<EduAuditRecord>> groupedRecords) throws IOException {
        writeCount(output, groupedRecords.size(), MAX_GROUPS, "audit groups");
        for (Map.Entry<String, List<EduAuditRecord>> entry : new TreeMap<>(groupedRecords).entrySet()) {
            writeString(output, entry.getKey());
            writeCount(output, entry.getValue().size(), MAX_GROUP_RECORDS, "audit records");
            for (EduAuditRecord record : entry.getValue()) {
                writeAudit(output, record);
            }
        }
    }

    private static void readAudits(DataInputStream input, Snapshot snapshot, ReadBudget budget)
            throws IOException {
        int groups = readCount(input, MAX_GROUPS, "audit groups");
        for (int group = 0; group < groups; group++) {
            String operationId = readString(input);
            int count = readCount(input, MAX_GROUP_RECORDS, "audit records");
            List<EduAuditRecord> records = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                budget.consume();
                EduAuditRecord record = readAudit(input);
                if (!operationId.equals(record.operationId())) {
                    throw corrupt("audit operation id does not match its group", null);
                }
                records.add(record);
            }
            if (snapshot.audits.putIfAbsent(operationId, records) != null) {
                throw corrupt("duplicate audit group: " + operationId, null);
            }
        }
    }

    private static void writeTargets(
            DataOutputStream output,
            Map<String, LinkedHashMap<String, EduTargetRecord>> groupedRecords) throws IOException {
        writeCount(output, groupedRecords.size(), MAX_GROUPS, "target groups");
        for (Map.Entry<String, LinkedHashMap<String, EduTargetRecord>> entry
                : new TreeMap<>(groupedRecords).entrySet()) {
            writeString(output, entry.getKey());
            writeCount(output, entry.getValue().size(), MAX_GROUP_RECORDS, "target records");
            for (EduTargetRecord record : entry.getValue().values()) {
                writeTarget(output, record);
            }
        }
    }

    private static void readTargets(DataInputStream input, Snapshot snapshot, ReadBudget budget)
            throws IOException {
        int groups = readCount(input, MAX_GROUPS, "target groups");
        for (int group = 0; group < groups; group++) {
            String operationId = readString(input);
            int count = readCount(input, MAX_GROUP_RECORDS, "target records");
            LinkedHashMap<String, EduTargetRecord> records = new LinkedHashMap<>();
            for (int index = 0; index < count; index++) {
                budget.consume();
                EduTargetRecord record = readTarget(input, budget);
                if (!operationId.equals(record.operationId())) {
                    throw corrupt("target operation id does not match its group", null);
                }
                if (records.putIfAbsent(record.targetId(), record) != null) {
                    throw corrupt("duplicate target id: " + record.targetId(), null);
                }
            }
            if (snapshot.targets.putIfAbsent(operationId, records) != null) {
                throw corrupt("duplicate target group: " + operationId, null);
            }
        }
    }

    private static void writeOutbox(
            DataOutputStream output,
            Map<String, LinkedHashMap<String, EduOutboxRecord>> groupedRecords) throws IOException {
        writeCount(output, groupedRecords.size(), MAX_GROUPS, "outbox groups");
        for (Map.Entry<String, LinkedHashMap<String, EduOutboxRecord>> entry
                : new TreeMap<>(groupedRecords).entrySet()) {
            writeString(output, entry.getKey());
            writeCount(output, entry.getValue().size(), MAX_GROUP_RECORDS, "outbox records");
            for (EduOutboxRecord record : entry.getValue().values()) {
                writeOutboxRecord(output, record);
            }
        }
    }

    private static void readOutbox(DataInputStream input, Snapshot snapshot, ReadBudget budget)
            throws IOException {
        int groups = readCount(input, MAX_GROUPS, "outbox groups");
        for (int group = 0; group < groups; group++) {
            String operationId = readString(input);
            int count = readCount(input, MAX_GROUP_RECORDS, "outbox records");
            LinkedHashMap<String, EduOutboxRecord> records = new LinkedHashMap<>();
            for (int index = 0; index < count; index++) {
                budget.consume();
                EduOutboxRecord record = readOutboxRecord(input, budget);
                if (!operationId.equals(record.operationId())) {
                    throw corrupt("outbox operation id does not match its group", null);
                }
                if (records.putIfAbsent(record.eventId(), record) != null) {
                    throw corrupt("duplicate outbox event id: " + record.eventId(), null);
                }
            }
            if (snapshot.outbox.putIfAbsent(operationId, records) != null) {
                throw corrupt("duplicate outbox group: " + operationId, null);
            }
        }
    }

    private static void writeLeases(DataOutputStream output, Map<String, Lease> leases)
            throws IOException {
        writeCount(output, leases.size(), MAX_GROUPS, "leases");
        for (Map.Entry<String, Lease> entry : new TreeMap<>(leases).entrySet()) {
            writeString(output, entry.getKey());
            writeString(output, entry.getValue().owner());
            output.writeLong(entry.getValue().fencingToken());
            writeInstant(output, entry.getValue().expiresAt());
        }
    }

    private static void readLeases(DataInputStream input, Snapshot snapshot, ReadBudget budget)
            throws IOException {
        int count = readCount(input, MAX_GROUPS, "leases");
        for (int index = 0; index < count; index++) {
            budget.consume();
            String key = readString(input);
            Lease lease = new Lease(readString(input), input.readLong(), readInstant(input));
            if (snapshot.leases.putIfAbsent(key, lease) != null) {
                throw corrupt("duplicate lease key: " + key, null);
            }
        }
    }

    private static void writeOperation(DataOutputStream output, EduOperationRecord record)
            throws IOException {
        writeString(output, record.operationId());
        writeString(output, record.requirementId());
        writeString(output, record.businessKey());
        writeString(output, record.idempotencyKey());
        writeString(output, record.payloadHash());
        writeString(output, record.actorId());
        writeString(output, record.actorRoles());
        writeString(output, record.dataScope());
        writeString(output, record.state().name());
        output.writeLong(record.expectedBusinessVersion());
        output.writeLong(record.recordVersion());
        output.writeLong(record.fencingToken());
        output.writeInt(record.retryCount());
        output.writeInt(record.maxRetries());
        writeString(output, record.failurePoint().name());
        writeString(output, record.resultCode());
        writeString(output, record.resultMessage());
        writeString(output, record.requestId());
        writeString(output, record.traceId());
        writeValueMap(output, record.payload(), 0);
        writeValueMap(output, record.result(), 0);
        writeInstant(output, record.createdAt());
        writeInstant(output, record.updatedAt());
        writeNullableInstant(output, record.completedAt());
    }

    private static EduOperationRecord readOperation(DataInputStream input, ReadBudget budget)
            throws IOException {
        return new EduOperationRecord(
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readEnum(input, EduExecutionState.class),
                input.readLong(),
                input.readLong(),
                input.readLong(),
                input.readInt(),
                input.readInt(),
                readEnum(input, EduFailurePoint.class),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readValueMap(input, budget, 0),
                readValueMap(input, budget, 0),
                readInstant(input),
                readInstant(input),
                readNullableInstant(input));
    }

    private static void writeAudit(DataOutputStream output, EduAuditRecord record)
            throws IOException {
        writeString(output, record.auditId());
        writeString(output, record.operationId());
        writeString(output, record.requirementId());
        writeString(output, record.action());
        writeString(output, record.beforeState());
        writeString(output, record.afterState());
        writeString(output, record.actorId());
        writeString(output, record.reason());
        writeString(output, record.traceId());
        writeInstant(output, record.createdAt());
    }

    private static EduAuditRecord readAudit(DataInputStream input) throws IOException {
        return new EduAuditRecord(
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readInstant(input));
    }

    private static void writeTarget(DataOutputStream output, EduTargetRecord record)
            throws IOException {
        writeString(output, record.targetId());
        writeString(output, record.operationId());
        writeString(output, record.targetKey());
        writeString(output, record.state());
        writeValueMap(output, record.beforeValue(), 0);
        writeValueMap(output, record.afterValue(), 0);
        writeString(output, record.errorCode());
        writeString(output, record.errorMessage());
        output.writeLong(record.version());
        writeInstant(output, record.updatedAt());
    }

    private static EduTargetRecord readTarget(DataInputStream input, ReadBudget budget)
            throws IOException {
        return new EduTargetRecord(
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readValueMap(input, budget, 0),
                readValueMap(input, budget, 0),
                readString(input),
                readString(input),
                input.readLong(),
                readInstant(input));
    }

    private static void writeOutboxRecord(DataOutputStream output, EduOutboxRecord record)
            throws IOException {
        writeString(output, record.eventId());
        writeString(output, record.operationId());
        writeString(output, record.destination());
        writeString(output, record.eventKey());
        writeValueMap(output, record.payload(), 0);
        writeString(output, record.state());
        output.writeInt(record.attemptCount());
        writeInstant(output, record.nextAttemptAt());
        writeString(output, record.claimedBy());
        output.writeLong(record.fencingToken());
        writeInstant(output, record.createdAt());
        writeInstant(output, record.updatedAt());
    }

    private static EduOutboxRecord readOutboxRecord(DataInputStream input, ReadBudget budget)
            throws IOException {
        return new EduOutboxRecord(
                readString(input),
                readString(input),
                readString(input),
                readString(input),
                readValueMap(input, budget, 0),
                readString(input),
                input.readInt(),
                readInstant(input),
                readString(input),
                input.readLong(),
                readInstant(input),
                readInstant(input));
    }

    private static void writeValueMap(DataOutputStream output, Map<String, Object> values, int depth)
            throws IOException {
        requireDepth(depth);
        writeCount(output, values.size(), MAX_COLLECTION_ENTRIES, "map entries");
        TreeMap<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("EDU persistence map keys must be strings");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("EDU persistence values must not be null: " + key);
            }
            sorted.put(key, entry.getValue());
        }
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            writeString(output, entry.getKey());
            writeValue(output, entry.getValue(), depth + 1);
        }
    }

    private static Map<String, Object> readValueMap(
            DataInputStream input, ReadBudget budget, int depth) throws IOException {
        requireDepth(depth);
        int size = readCount(input, MAX_COLLECTION_ENTRIES, "map entries");
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            String key = readString(input);
            if (values.putIfAbsent(key, readValue(input, budget, depth + 1)) != null) {
                throw corrupt("duplicate map key: " + key, null);
            }
        }
        return Map.copyOf(values);
    }

    private static void writeValue(DataOutputStream output, Object value, int depth)
            throws IOException {
        requireDepth(depth);
        if (value instanceof String string) {
            output.writeByte(VALUE_STRING);
            writeString(output, string);
        } else if (value instanceof Boolean bool) {
            output.writeByte(VALUE_BOOLEAN);
            output.writeBoolean(bool);
        } else if (value instanceof Byte number) {
            output.writeByte(VALUE_BYTE);
            output.writeByte(number);
        } else if (value instanceof Short number) {
            output.writeByte(VALUE_SHORT);
            output.writeShort(number);
        } else if (value instanceof Integer number) {
            output.writeByte(VALUE_INTEGER);
            output.writeInt(number);
        } else if (value instanceof Long number) {
            output.writeByte(VALUE_LONG);
            output.writeLong(number);
        } else if (value instanceof BigInteger number) {
            output.writeByte(VALUE_BIG_INTEGER);
            writeString(output, number.toString());
        } else if (value instanceof Float number) {
            if (!Float.isFinite(number)) {
                throw new IllegalArgumentException("EDU persistence floating values must be finite");
            }
            output.writeByte(VALUE_FLOAT);
            output.writeFloat(number);
        } else if (value instanceof Double number) {
            if (!Double.isFinite(number)) {
                throw new IllegalArgumentException("EDU persistence floating values must be finite");
            }
            output.writeByte(VALUE_DOUBLE);
            output.writeDouble(number);
        } else if (value instanceof BigDecimal number) {
            output.writeByte(VALUE_BIG_DECIMAL);
            writeString(output, number.toPlainString());
        } else if (value instanceof Map<?, ?> map) {
            output.writeByte(VALUE_MAP);
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            writeValueMap(output, typed, depth + 1);
        } else if (value instanceof List<?> list) {
            output.writeByte(VALUE_LIST);
            writeCount(output, list.size(), MAX_COLLECTION_ENTRIES, "list entries");
            for (Object item : list) {
                if (item == null) {
                    throw new IllegalArgumentException("EDU persistence list values must not be null");
                }
                writeValue(output, item, depth + 1);
            }
        } else if (value instanceof Set<?> set) {
            output.writeByte(VALUE_SET);
            writeDeterministicSet(output, set, depth + 1);
        } else if (value instanceof Collection<?>) {
            throw new IllegalArgumentException("EDU persistence accepts List and Set collections only");
        } else {
            throw new IllegalArgumentException(
                    "Unsupported EDU persistence value type: " + value.getClass().getName());
        }
    }

    private static Object readValue(DataInputStream input, ReadBudget budget, int depth)
            throws IOException {
        requireDepth(depth);
        budget.consume();
        return switch (input.readUnsignedByte()) {
            case VALUE_STRING -> readString(input);
            case VALUE_BOOLEAN -> input.readBoolean();
            case VALUE_BYTE -> input.readByte();
            case VALUE_SHORT -> input.readShort();
            case VALUE_INTEGER -> input.readInt();
            case VALUE_LONG -> input.readLong();
            case VALUE_BIG_INTEGER -> new BigInteger(readString(input));
            case VALUE_FLOAT -> requireFinite(input.readFloat());
            case VALUE_DOUBLE -> requireFinite(input.readDouble());
            case VALUE_BIG_DECIMAL -> new BigDecimal(readString(input));
            case VALUE_MAP -> readValueMap(input, budget, depth + 1);
            case VALUE_LIST -> readValueList(input, budget, depth + 1);
            case VALUE_SET -> readDeterministicSet(input, budget, depth + 1);
            default -> throw corrupt("unsupported value type tag", null);
        };
    }

    private static void writeDeterministicSet(
            DataOutputStream output, Set<?> values, int depth) throws IOException {
        requireDepth(depth);
        writeCount(output, values.size(), MAX_COLLECTION_ENTRIES, "set entries");
        List<byte[]> encoded = new ArrayList<>(values.size());
        long encodedBytes = 0;
        for (Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException("EDU persistence set values must not be null");
            }
            byte[] item = encodeValue(value, depth + 1);
            encodedBytes += Integer.BYTES + (long) item.length;
            if (encodedBytes > MAX_PAYLOAD_BYTES) {
                throw new IllegalArgumentException("EDU persistence set payload is too large");
            }
            encoded.add(item);
        }
        encoded.sort(Arrays::compareUnsigned);
        for (byte[] item : encoded) {
            output.writeInt(item.length);
            output.write(item);
        }
    }

    private static byte[] encodeValue(Object value, int depth) {
        LimitedByteArrayOutputStream bytes = new LimitedByteArrayOutputStream(MAX_PAYLOAD_BYTES);
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            writeValue(output, value, depth);
            output.flush();
            return bytes.toByteArray();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Set<Object> readDeterministicSet(
            DataInputStream input, ReadBudget budget, int depth) throws IOException {
        requireDepth(depth);
        int size = readCount(input, MAX_COLLECTION_ENTRIES, "set entries");
        Set<Object> values = new LinkedHashSet<>();
        for (int index = 0; index < size; index++) {
            int length = readCount(input, MAX_PAYLOAD_BYTES, "encoded set value bytes");
            byte[] encoded = new byte[length];
            input.readFully(encoded);
            try (ByteArrayInputStream bytes = new ByteArrayInputStream(encoded);
                    DataInputStream itemInput = new DataInputStream(bytes)) {
                Object value = readValue(itemInput, budget, depth + 1);
                if (bytes.available() != 0) {
                    throw corrupt("encoded set value contains trailing bytes", null);
                }
                if (!values.add(value)) {
                    throw corrupt("encoded set contains duplicate values", null);
                }
            }
        }
        return Collections.unmodifiableSet(values);
    }

    private static List<Object> readValueList(DataInputStream input, ReadBudget budget, int depth)
            throws IOException {
        requireDepth(depth);
        int size = readCount(input, MAX_COLLECTION_ENTRIES, "list entries");
        List<Object> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(readValue(input, budget, depth + 1));
        }
        return List.copyOf(values);
    }

    private static Float requireFinite(float value) {
        if (!Float.isFinite(value)) {
            throw corrupt("non-finite float value", null);
        }
        return value;
    }

    private static Double requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw corrupt("non-finite double value", null);
        }
        return value;
    }

    private static void requireDepth(int depth) {
        if (depth > MAX_VALUE_DEPTH) {
            throw corrupt("value nesting exceeds " + MAX_VALUE_DEPTH, null);
        }
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        Objects.requireNonNull(value, "persisted string");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_STRING_BYTES) {
            throw new IllegalArgumentException("EDU persistence string exceeds " + MAX_STRING_BYTES + " bytes");
        }
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > MAX_STRING_BYTES) {
            throw corrupt("string length is invalid: " + length, null);
        }
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (CharacterCodingException exception) {
            throw corrupt("string is not valid UTF-8", exception);
        }
    }

    private static void writeInstant(DataOutputStream output, Instant value) throws IOException {
        Objects.requireNonNull(value, "persisted instant");
        output.writeLong(value.getEpochSecond());
        output.writeInt(value.getNano());
    }

    private static Instant readInstant(DataInputStream input) throws IOException {
        try {
            return Instant.ofEpochSecond(input.readLong(), input.readInt());
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DateTimeException exception) {
            throw corrupt("instant value is invalid", exception);
        }
    }

    private static void writeNullableInstant(DataOutputStream output, Instant value)
            throws IOException {
        output.writeBoolean(value != null);
        if (value != null) {
            writeInstant(output, value);
        }
    }

    private static Instant readNullableInstant(DataInputStream input) throws IOException {
        return input.readBoolean() ? readInstant(input) : null;
    }

    private static <E extends Enum<E>> E readEnum(DataInputStream input, Class<E> enumType)
            throws IOException {
        String value = readString(input);
        try {
            return Enum.valueOf(enumType, value);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IllegalArgumentException exception) {
            throw corrupt("unknown " + enumType.getSimpleName() + ": " + value, exception);
        }
    }

    private static void writeCount(
            DataOutputStream output, int count, int maximum, String label) throws IOException {
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException(label + " exceeds allowed maximum " + maximum);
        }
        output.writeInt(count);
    }

    private static int readCount(DataInputStream input, int maximum, String label) throws IOException {
        int count = input.readInt();
        if (count < 0 || count > maximum) {
            throw corrupt(label + " count is invalid: " + count, null);
        }
        return count;
    }

    private static byte[] sha256(byte[] payload) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(payload);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String idempotencyKey(EduOperationRecord record) {
        return record.requirementId() + "|" + record.idempotencyKey();
    }

    private static void rejectSymbolicLink(Path path, String label) {
        if (Files.isSymbolicLink(path)) {
            throw corrupt(label + " must not be a symbolic link", null);
        }
    }

    private static IllegalStateException corrupt(String message, Throwable cause) {
        return new IllegalStateException("Invalid EDU operation store: " + message, cause);
    }

    @Override
    public EduCreateResult create(EduOperationRecord record) {
        return readWrite(true, snapshot -> {
            String key = idempotencyKey(record);
            String existingId = snapshot.idempotency.get(key);
            if (existingId != null) {
                EduOperationRecord existing = snapshot.operations.get(existingId);
                if (!existing.payloadHash().equals(record.payloadHash())) {
                    throw new EduConflictException("Idempotency key payload mismatch");
                }
                return new EduCreateResult(existing, true);
            }
            snapshot.operations.put(record.operationId(), record);
            snapshot.idempotency.put(key, record.operationId());
            return new EduCreateResult(record, false);
        });
    }

    @Override
    public Optional<EduOperationRecord> find(String operationId) {
        return readWrite(false, snapshot -> Optional.ofNullable(snapshot.operations.get(operationId)));
    }

    @Override
    public Optional<EduOperationRecord> findByIdempotency(String requirementId, String key) {
        return readWrite(
                false,
                snapshot -> Optional.ofNullable(snapshot.idempotency.get(requirementId + "|" + key))
                        .map(snapshot.operations::get));
    }

    @Override
    public List<EduOperationRecord> findByRequirement(String requirementId, int limit) {
        return readWrite(
                false,
                snapshot -> snapshot.operations.values().stream()
                        .filter(value -> value.requirementId().equals(requirementId))
                        .sorted(Comparator.comparing(EduOperationRecord::createdAt).reversed())
                        .limit(limit)
                        .toList());
    }

    @Override
    public EduOperationRecord save(EduOperationRecord record, long expectedRecordVersion) {
        return readWrite(true, snapshot -> {
            EduOperationRecord existing = snapshot.operations.get(record.operationId());
            if (existing == null) {
                throw new NoSuchElementException(record.operationId());
            }
            if (existing.recordVersion() != expectedRecordVersion) {
                throw new EduConflictException(
                        /** version 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
                        "record version conflict expected=" + expectedRecordVersion
                                + " actual=" + existing.recordVersion());
            }
            if (!existing.requirementId().equals(record.requirementId())
                    || !existing.idempotencyKey().equals(record.idempotencyKey())) {
                throw new EduConflictException(
                        "operation requirement/idempotency identity cannot be changed");
            }
            snapshot.operations.put(record.operationId(), record);
            return record;
        });
    }

    @Override
    public void appendAudit(EduAuditRecord audit) {
        readWrite(true, snapshot -> {
            snapshot.audits.computeIfAbsent(audit.operationId(), ignored -> new ArrayList<>()).add(audit);
            return null;
        });
    }

    @Override
    public List<EduAuditRecord> audits(String operationId) {
        return readWrite(
                false, snapshot -> List.copyOf(snapshot.audits.getOrDefault(operationId, List.of())));
    }

    @Override
    public void saveTarget(EduTargetRecord target) {
        readWrite(true, snapshot -> {
            snapshot.targets
                    .computeIfAbsent(target.operationId(), ignored -> new LinkedHashMap<>())
                    .put(target.targetId(), target);
            return null;
        });
    }

    @Override
    public List<EduTargetRecord> targets(String operationId) {
        return readWrite(
                false,
                snapshot -> List.copyOf(
                        snapshot.targets
                                .getOrDefault(operationId, new LinkedHashMap<>())
                                .values()));
    }

    @Override
    public void enqueue(EduOutboxRecord event) {
        readWrite(true, snapshot -> {
            snapshot.outbox
                    .computeIfAbsent(event.operationId(), ignored -> new LinkedHashMap<>())
                    .put(event.eventId(), event);
            return null;
        });
    }

    @Override
    public void saveOutbox(EduOutboxRecord event) {
        enqueue(event);
    }

    @Override
    public List<EduOutboxRecord> outbox(String operationId) {
        return readWrite(
                false,
                snapshot -> List.copyOf(
                        snapshot.outbox
                                .getOrDefault(operationId, new LinkedHashMap<>())
                                .values()));
    }

    @Override
    public long claimLease(String key, String owner, Instant expiresAt) {
        return readWrite(true, snapshot -> {
            Lease lease = snapshot.leases.get(key);
            Instant now = Instant.now();
            if (lease != null && lease.expiresAt().isAfter(now) && !lease.owner().equals(owner)) {
                throw new EduConflictException("lease held by " + lease.owner());
            }
            long fencingToken = lease == null ? 1 : lease.fencingToken() + 1;
            snapshot.leases.put(key, new Lease(owner, fencingToken, expiresAt));
            return fencingToken;
        });
    }

    /** Snapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private static final class Snapshot {
        private final Map<String, EduOperationRecord> operations = new LinkedHashMap<>();
        private final Map<String, String> idempotency = new HashMap<>();
        private final Map<String, List<EduAuditRecord>> audits = new HashMap<>();
        private final Map<String, LinkedHashMap<String, EduTargetRecord>> targets = new HashMap<>();
        private final Map<String, LinkedHashMap<String, EduOutboxRecord>> outbox = new HashMap<>();
        private final Map<String, Lease> leases = new HashMap<>();
    }

    /** Lease 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private record Lease(String owner, long fencingToken, Instant expiresAt) {
        private Lease {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(expiresAt, "expiresAt");
        }
    }

    private static final class ReadBudget {
        private int remaining = MAX_VALUE_NODES;

        private void consume() {
            if (--remaining < 0) {
                throw corrupt("value node count exceeds " + MAX_VALUE_NODES, null);
            }
        }
    }

    /** LimitedByteArrayOutputStream 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private static final class LimitedByteArrayOutputStream extends OutputStream {
        private final int maximum;
        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        private LimitedByteArrayOutputStream(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public void write(int value) throws IOException {
            requireCapacity(1);
            delegate.write(value);
        }

        @Override
        public void write(byte[] values, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, values.length);
            requireCapacity(length);
            delegate.write(values, offset, length);
        }

        private void requireCapacity(int additional) throws IOException {
            if (additional < 0 || delegate.size() > maximum - additional) {
                throw new IOException("EDU persistence payload exceeds " + maximum + " bytes");
            }
        }

        private byte[] toByteArray() {
            return delegate.toByteArray();
        }
    }
}
