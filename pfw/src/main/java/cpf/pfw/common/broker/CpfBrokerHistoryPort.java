package cpf.pfw.common.broker;

import java.util.List;

/**
 * broker 송수신 이력 적재/조회 port입니다.
 */
public interface CpfBrokerHistoryPort {

    void record(CpfBrokerHistoryRecord historyRecord);

    List<CpfBrokerHistoryRecord> findHistory(CpfBrokerHistoryQuery query);
}
