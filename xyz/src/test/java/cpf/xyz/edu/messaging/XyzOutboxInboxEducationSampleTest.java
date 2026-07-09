package cpf.xyz.edu.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzOutboxInboxEducationSampleTest {

    @Test
    void outboxRecordUsesAggregateAndEventForIdempotency() {
        assertThat(new XyzOutboxInboxEducationSample().outbox("A1", "CHANGED").idempotencyKey())
                .isEqualTo("A1:CHANGED");
    }
}
