package cpf.pfw.common.batch;

/**
 * 배치 이벤트 전파 어댑터입니다.
 *
 * <p>운영 환경에서는 Redis/Kafka 구현체로 교체하고, 로컬 환경에서는 로그 fallback으로 동작할 수 있게
 * 공통 인터페이스를 둡니다.</p>
 */
public interface CpfBatchEventPublisher {

    void publish(CpfBatchEvent event);
}
