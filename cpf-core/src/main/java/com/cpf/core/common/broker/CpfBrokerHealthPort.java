package com.cpf.core.common.broker;

import java.util.List;

/**
 * broker 연결 상태 조회 port입니다.
 */
public interface CpfBrokerHealthPort {

    List<CpfBrokerStatus> health();
}
