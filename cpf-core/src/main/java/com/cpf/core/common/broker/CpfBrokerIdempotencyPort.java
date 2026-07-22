package cpf.pfw.common.broker;

/**
 * broker 메시지 idempotency key 중복 여부를 확인하는 port입니다.
 */
public interface CpfBrokerIdempotencyPort {

    boolean isDuplicate(String idempotencyKey);

    void remember(String idempotencyKey, String messageId);
}
