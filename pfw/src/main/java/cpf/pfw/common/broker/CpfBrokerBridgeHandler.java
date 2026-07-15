package cpf.pfw.common.broker;

/**
 * broker bridge의 로컬 소비자와 실제 broker listener가 공통으로 사용하는 처리 계약입니다.
 */
@FunctionalInterface
public interface CpfBrokerBridgeHandler {
    void handle(CpfBrokerBridgeMessage message);
}
