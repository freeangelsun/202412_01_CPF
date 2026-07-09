package cpf.pfw.common.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwBrokerEducationSampleTest {

    @Test
    void brokerEnvelopeContainsTraceAndIdempotencyKey() {
        CpfBrokerEnvelope envelope = new PfwBrokerEducationSample()
                .buildEnvelope("20260708000000000XYZlocal010000001", "IDEMP-001");

        assertThat(envelope.transactionGlobalId()).startsWith("20260708");
        assertThat(envelope.idempotencyKey()).isEqualTo("IDEMP-001");
        assertThat(envelope.message().headers())
                .containsEntry("x-cpf-transaction-global-id", envelope.transactionGlobalId());
    }
}
