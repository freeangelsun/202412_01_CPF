package com.cpf.reference.edu.runtime.persistence;

import com.cpf.reference.edu.runtime.model.EduAuditRecord;
import com.cpf.reference.edu.runtime.model.EduExecutionState;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import com.cpf.reference.edu.runtime.model.EduOperationRecord;
import com.cpf.reference.edu.runtime.model.EduOutboxRecord;
import com.cpf.reference.edu.runtime.model.EduTargetRecord;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileEduOperationRepositoryTest {
    private static final Instant NOW = Instant.parse("2026-08-02T12:34:56.123456789Z");

    @TempDir
    Path directory;

    @Test
    void savesAndReloadsTheCompleteSnapshotAtomically() throws Exception {
        FileEduOperationRepository repository = new FileEduOperationRepository(directory);
        EduOperationRecord original = operation("operation-1", "idempotency-1", NOW);
        assertFalse(repository.create(original).duplicate());

        EduAuditRecord audit = new EduAuditRecord(
                "audit-1", original.operationId(), original.requirementId(), "CREATE", "",
                EduExecutionState.REQUESTED.name(), "actor-1", "test", "trace-1", NOW);
        EduTargetRecord target = new EduTargetRecord(
                "target-1", original.operationId(), "TARGET-A", "APPLIED",
                Map.of("balance", new BigDecimal("10.50")),
                Map.of("balance", new BigDecimal("20.50")), "", "", 1, NOW);
        EduOutboxRecord outbox = new EduOutboxRecord(
                "event-1", original.operationId(), "edu.events", "business-1",
                Map.of("sequence", 1L, "flags", List.of(true, false)), "READY", 0,
                NOW.plusSeconds(10), "", 1, NOW, NOW);
        repository.appendAudit(audit);
        repository.saveTarget(target);
        repository.enqueue(outbox);
        assertEquals(1, repository.claimLease("lease-1", "owner-1", NOW.plusSeconds(300)));

        EduOperationRecord completed = new EduOperationRecord(
                original.operationId(), original.requirementId(), original.businessKey(),
                original.idempotencyKey(), original.payloadHash(), original.actorId(),
                original.actorRoles(), original.dataScope(), EduExecutionState.SUCCEEDED,
                original.expectedBusinessVersion(), 1, 1, 0, original.maxRetries(),
                original.failurePoint(), "SUCCEEDED", "completed", original.requestId(),
                original.traceId(), original.payload(), Map.of("result", "ok"),
                original.createdAt(), NOW.plusSeconds(1), NOW.plusSeconds(1));
        repository.save(completed, 0);

        FileEduOperationRepository restarted = new FileEduOperationRepository(directory);
        assertEquals(completed, restarted.find(completed.operationId()).orElseThrow());
        assertEquals(completed, restarted.findByIdempotency(
                completed.requirementId(), completed.idempotencyKey()).orElseThrow());
        assertEquals(List.of(audit), restarted.audits(completed.operationId()));
        assertEquals(List.of(target), restarted.targets(completed.operationId()));
        assertEquals(List.of(outbox), restarted.outbox(completed.operationId()));
        assertEquals(2, restarted.claimLease("lease-1", "owner-1", NOW.plusSeconds(600)));
        assertFalse(Files.exists(directory.resolve(FileEduOperationRepository.TEMP_FILE_NAME)));
    }

    @Test
    void producesDeterministicBytesForTheSameLogicalSnapshot() throws Exception {
        Path firstDirectory = directory.resolve("first");
        Path secondDirectory = directory.resolve("second");
        FileEduOperationRepository first = new FileEduOperationRepository(firstDirectory);
        FileEduOperationRepository second = new FileEduOperationRepository(secondDirectory);
        EduOperationRecord alpha = operation("operation-a", "idempotency-a", NOW);
        EduOperationRecord beta = operation("operation-b", "idempotency-b", NOW.plusSeconds(1));

        first.create(beta);
        first.create(alpha);
        second.create(alpha);
        second.create(beta);

        assertEquals(
                -1,
                Files.mismatch(
                        firstDirectory.resolve(FileEduOperationRepository.STORE_FILE_NAME),
                        secondDirectory.resolve(FileEduOperationRepository.STORE_FILE_NAME)));
    }

    @Test
    void failedWriteLeavesTheLastDurableSnapshotIntact() throws Exception {
        FileEduOperationRepository repository = new FileEduOperationRepository(directory);
        EduOperationRecord original = operation("operation-1", "idempotency-1", NOW);
        repository.create(original);
        EduOperationRecord unsupported = new EduOperationRecord(
                original.operationId(), original.requirementId(), original.businessKey(),
                original.idempotencyKey(), original.payloadHash(), original.actorId(),
                original.actorRoles(), original.dataScope(), original.state(),
                original.expectedBusinessVersion(), 1, original.fencingToken(),
                original.retryCount(), original.maxRetries(), original.failurePoint(),
                original.resultCode(), original.resultMessage(), original.requestId(),
                original.traceId(), Map.of("untrusted", new Object()), original.result(),
                original.createdAt(), NOW.plusSeconds(1), original.completedAt());

        assertThrows(IllegalArgumentException.class, () -> repository.save(unsupported, 0));
        FileEduOperationRepository restarted = new FileEduOperationRepository(directory);
        assertEquals(original, restarted.find(original.operationId()).orElseThrow());
        assertFalse(Files.exists(directory.resolve(FileEduOperationRepository.TEMP_FILE_NAME)));
    }

    @Test
    void rejectsMalformedInput() throws Exception {
        byte[] malformed = new byte[FileEduOperationRepository.HEADER_BYTES];
        malformed[0] = 'X';
        Files.write(storeFile(), malformed);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> new FileEduOperationRepository(directory).find("operation-1"));
        assertTrue(failure.getMessage().contains("magic"));
    }

    @Test
    void rejectsOversizedInputBeforeAllocation() throws Exception {
        try (FileChannel channel = FileChannel.open(
                storeFile(), CREATE, TRUNCATE_EXISTING, WRITE)) {
            channel.position(FileEduOperationRepository.MAX_STORE_BYTES);
            channel.write(ByteBuffer.wrap(new byte[] {1}));
        }

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> new FileEduOperationRepository(directory).find("operation-1"));
        assertTrue(failure.getMessage().contains("maximum allowed size"));
    }

    @Test
    void rejectsTruncatedInputInsteadOfTreatingItAsAnEmptyStore() throws Exception {
        FileEduOperationRepository repository = new FileEduOperationRepository(directory);
        repository.create(operation("operation-1", "idempotency-1", NOW));
        try (FileChannel channel = FileChannel.open(storeFile(), WRITE)) {
            channel.truncate(channel.size() - 1);
        }

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> new FileEduOperationRepository(directory).find("operation-1"));
        assertTrue(failure.getMessage().contains("length mismatch"));
    }

    @Test
    void rejectsUntrustedLegacyJavaSerializationWithoutDeserializingIt() throws Exception {
        byte[] legacyStream = new byte[FileEduOperationRepository.HEADER_BYTES];
        legacyStream[0] = (byte) 0xac;
        legacyStream[1] = (byte) 0xed;
        legacyStream[2] = 0;
        legacyStream[3] = 5;
        Files.write(storeFile(), legacyStream);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> new FileEduOperationRepository(directory).find("operation-1"));
        assertTrue(failure.getMessage().contains("legacy Java-serialization"));
    }

    private Path storeFile() throws Exception {
        Files.createDirectories(directory);
        return directory.resolve(FileEduOperationRepository.STORE_FILE_NAME);
    }

    private static EduOperationRecord operation(String operationId, String idempotencyKey, Instant now) {
        return new EduOperationRecord(
                operationId,
                "CPF-EDU-DEV-001",
                "business-1",
                idempotencyKey,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "actor-1",
                "EDU_OPERATOR",
                "TENANT-A",
                EduExecutionState.REQUESTED,
                0,
                0,
                0,
                0,
                3,
                EduFailurePoint.NONE,
                "REQUESTED",
                "accepted",
                "request-1",
                "trace-1",
                Map.of(
                        "amount", new BigDecimal("10.50"),
                        "attempt", 1,
                        "items", List.of("A", "B"),
                        "metadata", Map.of("active", true)),
                Map.of(),
                now,
                now,
                null);
    }
}
