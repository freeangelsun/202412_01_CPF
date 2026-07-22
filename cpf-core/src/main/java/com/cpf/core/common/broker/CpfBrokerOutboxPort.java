package cpf.pfw.common.broker;

import java.util.List;

/**
 * 거래 commit 이후 broker 발행을 보장하기 위한 outbox port입니다.
 */
public interface CpfBrokerOutboxPort {

    CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope);

    List<CpfBrokerEnvelope> claimPending(String workerId, int limit);

    void markPublished(String messageId, CpfBrokerResult result);
}
