package cpf.xyz.edu.messaging;

import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.CpfBrokerOutboxPort;
import cpf.pfw.common.broker.CpfBrokerResult;
import cpf.pfw.common.broker.DeterministicCpfBrokerPublisher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XyzOutboxInboxEducationSampleTest {

    @Test
    void outboxIsPublishedThroughPfwWorker() {
        TestOutbox outbox = new TestOutbox();
        XyzOutboxInboxEducationSample sample = new XyzOutboxInboxEducationSample(
                outbox,
                new DeterministicCpfBrokerPublisher("XYZ_EDU", envelope -> false));
        CpfBrokerEnvelope envelope = new XyzBrokerPublishEducationSample().publishPlan("TX-1", "IDEM-1");

        XyzOutboxInboxEducationSample.PublishScenario scenario = sample.publish(envelope, "xyz-worker-1");

        assertThat(scenario.accepted().status()).isEqualTo("ACCEPTED");
        assertThat(scenario.workerResult().successCount()).isEqualTo(1);
        assertThat(outbox.results).extracting(CpfBrokerResult::status).containsExactly("PUBLISHED");
    }

    private static final class TestOutbox implements CpfBrokerOutboxPort {
        private final List<CpfBrokerEnvelope> pending = new ArrayList<>();
        private final List<CpfBrokerResult> results = new ArrayList<>();

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            pending.add(envelope);
            return CpfBrokerResult.accepted(envelope.message().messageId(), "PFW_OUTBOX", envelope.message().key());
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            List<CpfBrokerEnvelope> claimed = List.copyOf(pending);
            pending.clear();
            return claimed;
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
            results.add(result);
        }
    }
}
