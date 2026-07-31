package com.cpf.batch.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.time.Instant;
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

    private AgentProperties properties() {
        AgentProperties properties = new AgentProperties();
        properties.setCommandLedgerRoot(temp.toString());
        properties.setCommandLedgerRetentionSeconds(86_400L);
        return properties;
    }
}
