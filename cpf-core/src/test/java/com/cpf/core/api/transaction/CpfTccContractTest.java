package com.cpf.core.api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class CpfTccContractTest {
    @Test
    void participantCanMakeTryConfirmCancelIdempotent() {
        var participant = new IdempotentParticipant();
        var ctx = new CpfTccContext("tx-1", "inventory", "order-1", Instant.now().plusSeconds(30), Map.of());
        assertThat(participant.tryAction(ctx, "10")).isEqualTo(CpfTccResult.APPLIED);
        assertThat(participant.tryAction(ctx, "10")).isEqualTo(CpfTccResult.ALREADY_APPLIED);
        assertThat(participant.confirm(ctx, "10")).isEqualTo(CpfTccResult.APPLIED);
        assertThat(participant.confirm(ctx, "10")).isEqualTo(CpfTccResult.ALREADY_APPLIED);
    }

    private static final class IdempotentParticipant implements CpfTccParticipant<String> {
        private final Map<String, CpfTccPhase> states = new ConcurrentHashMap<>();
        public CpfTccResult tryAction(CpfTccContext c, String command) {
            return states.putIfAbsent(c.idempotencyKey(), CpfTccPhase.TRY) == null
                    ? CpfTccResult.APPLIED : CpfTccResult.ALREADY_APPLIED;
        }
        public CpfTccResult confirm(CpfTccContext c, String command) {
            CpfTccPhase before = states.get(c.idempotencyKey());
            if (before == CpfTccPhase.CONFIRM) return CpfTccResult.ALREADY_APPLIED;
            if (before != CpfTccPhase.TRY) return CpfTccResult.FAILED;
            return states.replace(c.idempotencyKey(), CpfTccPhase.TRY, CpfTccPhase.CONFIRM)
                    ? CpfTccResult.APPLIED : CpfTccResult.RETRY;
        }
        public CpfTccResult cancel(CpfTccContext c, String command) {
            if (!states.containsKey(c.idempotencyKey())) return CpfTccResult.EMPTY_ROLLBACK;
            if (states.get(c.idempotencyKey()) == CpfTccPhase.CANCEL) return CpfTccResult.ALREADY_APPLIED;
            states.put(c.idempotencyKey(), CpfTccPhase.CANCEL);
            return CpfTccResult.APPLIED;
        }
    }
}
