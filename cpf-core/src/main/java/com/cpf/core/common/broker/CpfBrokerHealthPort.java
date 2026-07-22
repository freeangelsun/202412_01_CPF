package cpf.pfw.common.broker;

import java.util.List;

/**
 * broker 연결 상태 조회 port입니다.
 */
public interface CpfBrokerHealthPort {

    List<CpfBrokerStatus> health();
}
