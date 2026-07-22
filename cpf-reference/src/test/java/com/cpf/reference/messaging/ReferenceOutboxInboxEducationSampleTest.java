package com.cpf.reference.messaging;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import com.cpf.core.common.broker.DeterministicCpfBrokerPublisher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceOutboxInboxEducationSampleTest {

    @Test
    void outboxIsPublishedThroughCpfWorker() {
        TestOutbox outbox = new TestOutbox();
        ReferenceOutboxInboxEducationSample sample = new ReferenceOutboxInboxEducationSample(
                outbox,
                new DeterministicCpfBrokerPublisher("REF_EDU", envelope -> false));
        CpfBrokerEnvelope envelope = new ReferenceBrokerPublishEducationSample().publishPlan("TX-1", "IDEM-1");

        ReferenceOutboxInboxEducationSample.PublishScenario scenario = sample.publish(envelope, "ref-worker-1");

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
            return CpfBrokerResult.accepted(envelope.message().messageId(), "CPF_OUTBOX", envelope.message().key());
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
