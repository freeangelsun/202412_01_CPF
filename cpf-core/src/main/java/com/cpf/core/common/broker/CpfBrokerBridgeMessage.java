package cpf.pfw.common.broker;

import java.time.Instant;
import java.util.Map;

/**
 * PFW broker bridge가 전송하는 범용 메시지 봉투입니다.
 *
 * @param broker 실제 또는 로컬 broker 종류
 * @param destination topic 또는 routing destination
 * @param key partition 또는 업무 식별 key
 * @param payload 직렬화할 업무 payload
 * @param headers 거래 추적과 확장 header
 * @param createdAt 메시지 생성 시각
 */
public record CpfBrokerBridgeMessage(
        String broker,
        String destination,
        String key,
        Object payload,
        Map<String, String> headers,
        Instant createdAt) {

    public CpfBrokerBridgeMessage {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
