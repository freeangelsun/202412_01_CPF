package cpf.pfw.common.broker;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 실 broker 없이 outbox worker 상태 전이를 검증하는 결정적 reference publisher입니다.
 *
 * <p>운영 broker adapter 완료 근거가 아니라 로컬 contract와 장애 시나리오 검증에만 사용합니다.</p>
 */
public class DeterministicCpfBrokerPublisher implements CpfBrokerPublisher {
    private final String adapterName;
    private final Predicate<CpfBrokerEnvelope> failurePredicate;

    public DeterministicCpfBrokerPublisher(String adapterName, Predicate<CpfBrokerEnvelope> failurePredicate) {
        this.adapterName = adapterName == null || adapterName.isBlank() ? "DETERMINISTIC" : adapterName;
        this.failurePredicate = Objects.requireNonNull(failurePredicate, "failurePredicate는 필수입니다.");
    }

    @Override
    public CpfBrokerResult publish(CpfBrokerEnvelope envelope) {
        if (failurePredicate.test(envelope)) {
            return CpfBrokerResult.failed(
                    envelope.message().messageId(),
                    adapterName,
                    "결정적 실패 조건이 적용됐습니다.");
        }
        return CpfBrokerResult.published(
                envelope.message().messageId(),
                adapterName,
                envelope.message().key());
    }
}
