package cpf.pfw.common.broker;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * PFW broker port를 Kafka/MQ runtime 없이 contract 수준에서 학습하는 샘플입니다.
 */
public class PfwBrokerEducationSample {

    /**
     * 업무 모듈이 특정 broker SDK에 직접 의존하지 않고 envelope를 만드는 기준입니다.
     */
    public CpfBrokerEnvelope buildEnvelope(String transactionGlobalId, String idempotencyKey) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                "MSG-" + idempotencyKey,
                "cpf.xyz.changed",
                idempotencyKey,
                "{\"status\":\"CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                Map.of("x-cpf-transaction-global-id", transactionGlobalId));
        return new CpfBrokerEnvelope(
                transactionGlobalId,
                "SEG-BROKER-001",
                "XYZ",
                "BAT",
                idempotencyKey,
                Instant.parse("2026-07-08T00:00:00Z"),
                message,
                Map.of("runtime", "contract-only"));
    }
}
