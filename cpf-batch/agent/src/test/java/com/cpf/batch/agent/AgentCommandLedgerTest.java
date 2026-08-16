package com.cpf.batch.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentCommandLedgerTest {
    @TempDir Path temp;

    @Test
    void returnsStoredTerminalResultWithoutReexecuting() {
        AgentProperties properties = properties();
        AgentCommandLedger ledger = new AgentCommandLedger(properties, new ObjectMapper().findAndRegisterModules());
        AtomicInteger executions = new AtomicInteger();
        String id = "batcmd-" + "a".repeat(64);
        String fingerprint = "b".repeat(64);

        AgentCommandResult first = ledger.execute(id, fingerprint, "svc", "START", (commandId, startedAt) -> {
            executions.incrementAndGet();
            return new AgentCommandResult(commandId, "svc", "START", CommandState.SUCCEEDED,
                    "OK", "done", null, startedAt, Instant.now());
        });
        AgentCommandResult replay = ledger.execute(id, fingerprint, "svc", "START", (commandId, startedAt) -> {
            executions.incrementAndGet();
            throw new AssertionError("must not execute");
        });

        assertThat(first).isEqualTo(replay);
        assertThat(executions).hasValue(1);
    }

    @Test
    void rejectsSameIdWithDifferentFingerprint() {
        AgentCommandLedger ledger = new AgentCommandLedger(properties(), new ObjectMapper().findAndRegisterModules());
        String id = "batcmd-" + "c".repeat(64);
        ledger.execute(id, "d".repeat(64), "svc", "STOP", (commandId, startedAt) ->
                new AgentCommandResult(commandId, "svc", "STOP", CommandState.SUCCEEDED,
                        "OK", "done", null, startedAt, Instant.now()));

        assertThatThrownBy(() -> ledger.execute(id, "e".repeat(64), "svc", "STOP", (commandId, startedAt) -> null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("different request");
    }


    @Test
    void persistsUnknownWhenHandlerOutcomeCannotBeConfirmed() {
        AgentCommandLedger ledger = new AgentCommandLedger(properties(), new ObjectMapper().findAndRegisterModules());
        String id = "batcmd-" + "f".repeat(64);
        String fingerprint = "1".repeat(64);
        AtomicInteger executions = new AtomicInteger();

        AgentCommandResult first = ledger.execute(id, fingerprint, "svc", "RESTART", (commandId, startedAt) -> {
            executions.incrementAndGet();
            throw new java.io.IOException("password=clear response was lost");
        });
        AgentCommandResult replay = ledger.execute(id, fingerprint, "svc", "RESTART", (commandId, startedAt) -> {
            executions.incrementAndGet();
            throw new AssertionError("unknown result must not be replayed");
        });

        assertThat(first.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        assertThat(first.resultCode()).isEqualTo("COMMAND_HANDLER_RESULT_UNKNOWN");
        assertThat(first.message()).doesNotContain("clear");
        assertThat(replay).isEqualTo(first);
        assertThat(executions).hasValue(1);
    }

    @Test
    void serializesTwoLedgerInstancesForTheSameCommand() throws Exception {
        AgentCommandLedger firstLedger = new AgentCommandLedger(properties(), new ObjectMapper().findAndRegisterModules());
        AgentCommandLedger secondLedger = new AgentCommandLedger(properties(), new ObjectMapper().findAndRegisterModules());
        String id = "batcmd-" + "2".repeat(64);
        String fingerprint = "3".repeat(64);
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> firstLedger.execute(id, fingerprint, "svc", "START", (commandId, startedAt) -> {
                executions.incrementAndGet();
                started.countDown();
                Thread.sleep(200L);
                return new AgentCommandResult(commandId, "svc", "START", CommandState.SUCCEEDED,
                        "OK", "done", null, startedAt, Instant.now());
            }));
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> secondLedger.execute(id, fingerprint, "svc", "START", (commandId, startedAt) -> {
                executions.incrementAndGet();
                return new AgentCommandResult(commandId, "svc", "START", CommandState.SUCCEEDED,
                        "OK", "duplicate", null, startedAt, Instant.now());
            }));

            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(first.get(5, TimeUnit.SECONDS));
        }
        assertThat(executions).hasValue(1);
    }

    private AgentProperties properties() {
        AgentProperties properties = new AgentProperties();
        properties.setCommandLedgerRoot(temp.toString());
        properties.setCommandLedgerRetentionSeconds(86_400L);
        return properties;
    }
}
