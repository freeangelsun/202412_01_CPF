package cpf.pfw.common.broker;

import java.util.List;

/**
 * broker DLQ 처리 port입니다.
 */
public interface CpfBrokerDlqPort {

    CpfBrokerResult sendToDlq(CpfBrokerEnvelope envelope, String reason);

    List<CpfBrokerEnvelope> findDlqMessages(String topic, int limit);
}
