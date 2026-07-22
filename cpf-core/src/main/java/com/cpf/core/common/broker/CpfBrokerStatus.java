package cpf.pfw.common.broker;

import java.time.Instant;
import java.util.Map;

/**
 * broker 관제 화면/API에서 공통으로 사용하는 상태 DTO입니다.
 */
public record CpfBrokerStatus(
        String brokerName,
        String brokerType,
        String status,
        Instant checkedAt,
        Map<String, String> metrics) {

    public CpfBrokerStatus {
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
    }
}
