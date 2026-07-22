package cpf.pfw.common.broker;

import java.time.Instant;
import java.util.List;

/**
 * broker 메시지 재처리 port입니다.
 */
public interface CpfBrokerReplayPort {

    CpfBrokerResult replay(String messageId);

    List<CpfBrokerResult> replayRange(String topic, Instant from, Instant to, int limit);
}
