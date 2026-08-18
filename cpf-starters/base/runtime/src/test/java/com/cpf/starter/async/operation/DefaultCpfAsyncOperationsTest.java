package com.cpf.starter.async.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cpf.core.api.async.*;
import com.cpf.core.api.context.*;
import com.cpf.core.api.data.encryption.*;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.security.crypto.CpfEnvelopeCiphertext;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class DefaultCpfAsyncOperationsTest {
    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");

    @Test
    void submitUsesHandlerOperationIdFrameworkExecutionIdAndEncryptedPayload() throws Exception {
        MemoryStore store = new MemoryStore();
        CpfFieldEncryptionOperations encryption = encryption();
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        DefaultCpfAsyncOperations operations = new DefaultCpfAsyncOperations(
                store, List.of(new ExportHandler()), json, new CpfAsyncPayloadCodec(encryption, json), ids(), Clock.fixed(NOW, ZoneOffset.UTC));

        try (AutoCloseable ignored = CpfContexts.bind(root())) {
            CpfAsyncSubmission first = operations.submit(new ExportCommand("member-123"), "idem-1", Duration.ofMinutes(3));
            CpfAsyncSubmission duplicate = operations.submit(new ExportCommand("member-123"), "idem-1", Duration.ofMinutes(3));

            assertThat(first.operationId()).isEqualTo("MBR_MEMBER_EXPORT");
            assertThat(first.executionId()).isEqualTo("EX-1");
            assertThat(first.state()).isEqualTo(CpfAsyncState.ACCEPTED);
            assertThat(first.duplicate()).isFalse();
            assertThat(duplicate.executionId()).isEqualTo(first.executionId());
            assertThat(duplicate.duplicate()).isTrue();
            assertThat(store.last.commandPayload()).doesNotContain("member-123");
            assertThat(store.last.contextPayload()).doesNotContain("TX-ASYNC-TEST");
        }
        assertThat(CpfContexts.current()).isNull();
    }

    @Test
    void acceptedCancelIsTerminalAndDoesNotRequireOperationIdFromCaller() throws Exception {
        MemoryStore store = new MemoryStore();
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        DefaultCpfAsyncOperations operations = new DefaultCpfAsyncOperations(
                store, List.of(new ExportHandler()), json, new CpfAsyncPayloadCodec(encryption(), json), ids(), Clock.fixed(NOW, ZoneOffset.UTC));
        String executionId;
        try (AutoCloseable ignored = CpfContexts.bind(root())) {
            executionId = operations.submit(new ExportCommand("m1"), "idem-2", null).executionId();
        }
        CpfAsyncOperationStatus cancelled = operations.cancel(executionId, "operator request");
        assertThat(cancelled.state()).isEqualTo(CpfAsyncState.CANCELLED);
        assertThat(cancelled.cancellationReason()).isEqualTo("operator request");
    }

    private static CpfContextSnapshot root() {
        CpfContextExecutionFactory f = new CpfContextExecutionFactory(
                () -> "TX-ASYNC-TEST",
                ids(),
                (CpfBusinessDateProvider) () -> LocalDate.of(2026, 8, 18),
                Clock.fixed(NOW, ZoneOffset.UTC));
        return CpfContextSnapshot.capture(f.newRoot(null, "async.test", null, null, NOW.plusSeconds(600)), NOW);
    }

    private static CpfExecutionIdGenerator ids() {
        return new CpfExecutionIdGenerator() {
            private int execution;
            private int segment;
            public String newExecutionId() { return "EX-" + (++execution); }
            public String newSegmentId() { return "SG-" + (++segment); }
        };
    }

    private static CpfFieldEncryptionOperations encryption() {
        CpfFieldEncryptionOperations encryption = mock(CpfFieldEncryptionOperations.class);
        when(encryption.encrypt(anyString(), anyString(), any(CpfFieldClassification.class), anyBoolean()))
                .thenAnswer(invocation -> new CpfEncryptedField(
                        invocation.getArgument(2), null, "k1",
                        new CpfEnvelopeCiphertext("AES/GCM", "test", "k1", new byte[]{1}, new byte[]{2}, new byte[]{3}, new byte[]{4}, new byte[]{5}),
                        "***"));
        return encryption;
    }

    record ExportCommand(String memberId) {}
    record ExportResult(String objectKey) {}
    static final class ExportHandler implements CpfAsyncHandler<ExportCommand, ExportResult> {
        public String operationId() { return "MBR_MEMBER_EXPORT"; }
        public Class<ExportCommand> commandType() { return ExportCommand.class; }
        public Class<ExportResult> resultType() { return ExportResult.class; }
        public CpfResult<ExportResult> execute(ExportCommand command, CpfAsyncExecution execution) {
            return CpfResult.success(new ExportResult("exports/" + command.memberId()));
        }
    }

    static final class MemoryStore implements CpfAsyncOperationStore {
        private final Map<String,CpfAsyncStoredOperation> byId = new LinkedHashMap<>();
        private final Map<String,String> byKey = new HashMap<>();
        CpfAsyncStoredOperation last;
        public CpfAsyncStoredOperation insertOrGet(CpfAsyncStoredOperation operation) {
            String key = operation.operationId() + "|" + operation.idempotencyKey();
            String existingId = byKey.get(key);
            if (existingId != null) return byId.get(existingId);
            byKey.put(key, operation.executionId()); byId.put(operation.executionId(), operation); last=operation; return operation;
        }
        public Optional<CpfAsyncStoredOperation> find(String executionId) { return Optional.ofNullable(byId.get(executionId)); }
        public Optional<CpfAsyncStoredOperation> claimNext(String owner, Instant now, Instant leaseUntil) { return Optional.empty(); }
        public boolean heartbeat(String executionId,String owner,long expectedVersion,Instant now,Instant leaseUntil) { return false; }
        public CpfAsyncStoredOperation requestCancel(String executionId,String reason,Instant now) {
            CpfAsyncStoredOperation o = byId.get(executionId);
            CpfAsyncStoredOperation c = copy(o, CpfAsyncState.CANCELLED, reason, now, o.version()+1);
            byId.put(executionId,c); return c;
        }
        public boolean cancellationRequested(String executionId) { return byId.get(executionId).state()==CpfAsyncState.CANCEL_REQUESTED; }
        public CpfAsyncStoredOperation complete(String executionId,String owner,long expectedVersion,String resultStatus,String resultType,String resultPayload,String errorCode,String errorMessage,String recoveryId,String recoveryAction,Instant now) { throw new UnsupportedOperationException(); }
        public int expireDue(Instant now) { return 0; }
        private static CpfAsyncStoredOperation copy(CpfAsyncStoredOperation o,CpfAsyncState state,String reason,Instant now,long version) {
            return new CpfAsyncStoredOperation(o.executionId(),o.operationId(),o.transactionId(),o.idempotencyKey(),o.commandType(),o.commandPayload(),o.contextPayload(),o.resultType(),o.resultPayload(),state,o.resultStatus(),o.errorCode(),o.errorMessage(),o.recoveryId(),o.recoveryAction(),o.submittedAt(),o.startedAt(),now,now,o.expiresAt(),o.heartbeatAt(),o.leaseOwner(),o.leaseUntil(),reason,version);
        }
    }
}
