package cpf.xyz.edu.messaging;

import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.CpfBrokerOutboxPort;
import cpf.pfw.common.broker.CpfBrokerPublisher;
import cpf.pfw.common.broker.CpfBrokerPublisherWorker;
import cpf.pfw.common.broker.CpfBrokerResult;

/**
 * XYZ 업무 이벤트를 PFW outbox와 publisher worker로 전달하는 샘플입니다.
 */
public class XyzOutboxInboxEducationSample {
    private final CpfBrokerOutboxPort outboxPort;
    private final CpfBrokerPublisherWorker publisherWorker;

    public XyzOutboxInboxEducationSample(CpfBrokerOutboxPort outboxPort, CpfBrokerPublisher publisher) {
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
