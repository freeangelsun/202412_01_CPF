package cpf.xyz.edu.messaging;

import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.PfwBrokerEducationSample;

/**
 * XYZ 변경 이벤트를 PFW broker envelope로 발행하는 샘플입니다.
 */
public class XyzBrokerPublishEducationSample {

    public CpfBrokerEnvelope publishPlan(String transactionGlobalId, String idempotencyKey) {
        return new PfwBrokerEducationSample().buildEnvelope(transactionGlobalId, idempotencyKey);
    }
}
