package cpf.pfw.common.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwBrokerReliabilityEducationSampleTest {

    @Test
    void reliabilityFlowCoversOutboxInboxDuplicateDlqAndReplay() {
        PfwBrokerReliabilityEducationSample.BrokerReliabilityScenario scenario =
                new PfwBrokerReliabilityEducationSample().runReliabilityFlow("IDEMP-MBR-001");

        assertThat(scenario.envelope().idempotencyKey()).isEqualTo("IDEMP-MBR-001");
        assertThat(scenario.envelope().attributes()).containsEntry("retryCount", "0");
        assertThat(scenario.outbox().status()).isEqualTo("READY");
        assertThat(scenario.firstInbox().status()).isEqualTo("ACCEPTED");
        assertThat(scenario.duplicateInbox().status()).isEqualTo("DUPLICATE");
        assertThat(scenario.dlq().failureCode()).isEqualTo("CONSUMER_TIMEOUT");
        assertThat(scenario.replay().status()).isEqualTo("REPLAY_REQUESTED");
    }

    @Test
    void envelopeDoesNotExposeCredentialOrSecretText() {
        CpfBrokerEnvelope envelope = new PfwBrokerReliabilityEducationSample()
                .envelope("20260709030000000MBRlocal010000001", "IDEMP-SECRET-SAFE");

        assertThat(envelope.message().headers().keySet()).doesNotContain("password", "secret", "token");
        assertThat(new String(envelope.message().payload())).doesNotContain("password", "secret");
    }
}
