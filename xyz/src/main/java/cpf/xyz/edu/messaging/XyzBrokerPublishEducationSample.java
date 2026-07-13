package cpf.xyz.edu.messaging;

import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.CpfBrokerMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * XYZ 변경 이벤트를 PFW broker envelope로 발행하는 샘플입니다.
 */
public class XyzBrokerPublishEducationSample {

    public CpfBrokerEnvelope publishPlan(String transactionGlobalId, String idempotencyKey) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                "XYZ-" + idempotencyKey,
                "cpf.xyz.changed",
                idempotencyKey,
                "{\"eventType\":\"XYZ_CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                Map.of("x-cpf-transaction-global-id", transactionGlobalId));
        return new CpfBrokerEnvelope(
                transactionGlobalId,
                transactionGlobalId + "-BROKER",
                "XYZ",
                "MBR",
                idempotencyKey,
                Instant.now(),
                message,
                Map.of("sampleId", "XYZ-EDU-MSG-001"));
    }
}
