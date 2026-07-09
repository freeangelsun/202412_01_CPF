package cpf.pfw.common.servicecall;

import java.time.Duration;
import java.util.List;

/**
 * 주제영역 간 호출을 URL 직접 조합이 아니라 PFW Service Call 기준으로 설명하는 샘플입니다.
 */
public class PfwServiceCallEducationSample {

    /**
     * timeout, retry, failover, circuit breaker를 같은 호출 계획 안에 묶어 표현합니다.
     */
    public CallPlan buildPlan(String serviceId, String endpointId) {
        if (serviceId == null || serviceId.isBlank()) {
            throw new IllegalArgumentException("serviceId는 필수입니다.");
        }
        return new CallPlan(
                serviceId,
                endpointId,
                Duration.ofSeconds(3),
                2,
                true,
                true,
                List.of("instance-a", "instance-b"),
                "selectedInstanceId는 호출 로그에 반드시 남깁니다.");
    }

    public record CallPlan(
            String serviceId,
            String endpointId,
            Duration timeout,
            int retryCount,
            boolean failoverEnabled,
            boolean circuitBreakerEnabled,
            List<String> candidateInstances,
            String loggingRule) {
    }
}
