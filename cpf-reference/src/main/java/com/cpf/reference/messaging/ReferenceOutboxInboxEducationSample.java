package com.cpf.reference.messaging;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerPublisher;
import com.cpf.core.common.broker.CpfBrokerPublisherWorker;
import com.cpf.core.common.broker.CpfBrokerResult;

/**
 * REF 업무 이벤트를 CPF outbox와 publisher worker로 전달하는 샘플입니다.
 */
public class ReferenceOutboxInboxEducationSample {
    private final CpfBrokerOutboxPort outboxPort;
    private final CpfBrokerPublisherWorker publisherWorker;

    public ReferenceOutboxInboxEducationSample(CpfBrokerOutboxPort outboxPort, CpfBrokerPublisher publisher) {
        this.outboxPort = outboxPort;
        this.publisherWorker = new CpfBrokerPublisherWorker(outboxPort, publisher);
    }

    public PublishScenario publish(CpfBrokerEnvelope envelope, String workerId) {
        CpfBrokerResult accepted = outboxPort.saveOutbox(envelope);
        CpfBrokerPublisherWorker.RunResult workerResult = publisherWorker.runOnce(workerId, 10);
        return new PublishScenario(accepted, workerResult);
    }

    public record PublishScenario(
            CpfBrokerResult accepted,
            CpfBrokerPublisherWorker.RunResult workerResult) {
    }
}
