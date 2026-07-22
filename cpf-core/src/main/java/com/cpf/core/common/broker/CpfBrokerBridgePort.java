package com.cpf.core.common.broker;

import java.util.List;
import java.util.Map;

/**
 * 프로젝트 공통과 업무 모듈이 특정 broker SDK에 의존하지 않고 사용하는 CPF bridge port입니다.
 */
public interface CpfBrokerBridgePort {

    CpfBrokerBridgeResult publish(String destination, String key, Object payload, Map<String, String> headers);

    void subscribe(String destination, CpfBrokerBridgeHandler handler);

    List<CpfBrokerBridgeMessage> findRecent(String destination, int limit);
}
