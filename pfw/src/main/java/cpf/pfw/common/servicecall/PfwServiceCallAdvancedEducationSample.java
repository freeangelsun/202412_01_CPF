package cpf.pfw.common.servicecall;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * PFW Service Call Engine의 고급 호출 흐름을 학습하기 위한 샘플입니다.
 *
 * <p>실제 HTTP 런타임 없이도 서비스/엔드포인트/인스턴스 레지스트리, 직접 인스턴스 호출,
 * 로드밸런싱 호출, selectedInstanceId 기록, 호출 이력 모델을 하나의 계약으로 검증할 수 있게 구성합니다.</p>
 */
public class PfwServiceCallAdvancedEducationSample {

    /**
     * ADM 서비스 카탈로그와 Service Call Engine이 함께 바라보는 최소 레지스트리 스냅샷을 만듭니다.
     */
    public RegistrySnapshot registrySnapshot() {
        return new RegistrySnapshot(
                Map.of("serviceId", "MBR", "serviceName", "회원 서비스", "moduleCode", "MBR"),
                Map.of("endpointCode", "member-summary", "httpMethod", "GET", "path", "/api/v1/members/{memberNo}/summary"),
                List.of(
                        new InstanceStatus("mbr-local-01", "http://localhost:8081", "UP", 12L),
                        new InstanceStatus("mbr-local-02", "http://localhost:8181", "UP", 4L),
                        new InstanceStatus("mbr-local-03", "http://localhost:8281", "DOWN", 0L)));
    }

    /**
     * 운영자가 특정 인스턴스를 지정해 재현 테스트를 해야 할 때 사용하는 direct instance mode 예시입니다.
     */
    public CallScenario directInstanceCall(String transactionGlobalId, String selectedInstanceId) {
        RegistrySnapshot registry = registrySnapshot();
        InstanceStatus instance = registry.findInstance(selectedInstanceId);
        ServiceCallRequest request = ServiceCallRequest.builder("MBR")
                .endpointCode("member-summary")
                .instanceId(selectedInstanceId)
                .requestPath("/api/v1/members/M0001/summary")
                .timeoutMillis(3000)
                .retryCount(0)
                .header("x-cpf-transaction-global-id", transactionGlobalId)
                .attribute("routingMode", "DIRECT")
                .build();

        ServiceCallResolvedTarget target = toTarget(registry, instance, "DIRECT");
        ServiceCallResult<Map<String, String>> result = ServiceCallResult.success(
                target,
                Map.of("memberNo", "M0001", "summaryStatus", "ACTIVE"),
                200,
                18L,
                1);
        return new CallScenario(request, result, history(transactionGlobalId, request, result));
    }

    /**
     * 일반 업무 호출에서 사용하는 LB endpoint mode 예시입니다.
     *
     * <p>실서비스에서는 health, weight, circuit 상태를 종합하지만 샘플에서는 응답 대기 건수가 가장 적은
     * UP 인스턴스를 선택해 selectedInstanceId가 결과와 이력에 남는지를 보여줍니다.</p>
     */
    public CallScenario lbEndpointCall(String transactionGlobalId) {
        RegistrySnapshot registry = registrySnapshot();
        InstanceStatus selected = registry.instances().stream()
                .filter(instance -> "UP".equals(instance.healthStatus()))
                .min(Comparator.comparingLong(InstanceStatus::inFlightCount))
                .orElseThrow(() -> new IllegalStateException("호출 가능한 서비스 인스턴스가 없습니다."));

        ServiceCallRequest request = ServiceCallRequest.builder("MBR")
                .endpointCode("member-summary")
                .requestPath("/api/v1/members/M0002/summary")
                .timeoutMillis(3000)
                .retryCount(2)
                .header("x-cpf-transaction-global-id", transactionGlobalId)
                .attribute("routingMode", "LB")
                .build();

        ServiceCallResolvedTarget target = toTarget(registry, selected, "LB");
        ServiceCallResult<Map<String, String>> result = ServiceCallResult.success(
                target,
                Map.of("memberNo", "M0002", "summaryStatus", "DORMANT"),
                200,
                22L,
                1);
        return new CallScenario(request, result, history(transactionGlobalId, request, result));
    }

    /**
     * timeout/retry/failover/circuit 결과가 호출 결과와 ADM 이력 조회 모델에 같이 남는 실패 예시입니다.
     */
    public CallScenario circuitOpenFailure(String transactionGlobalId) {
        RegistrySnapshot registry = registrySnapshot();
        InstanceStatus instance = registry.findInstance("mbr-local-01");
        ServiceCallRequest request = ServiceCallRequest.builder("MBR")
                .endpointCode("member-summary")
                .requestPath("/api/v1/members/M0003/summary")
                .timeoutMillis(500)
                .retryCount(2)
                .header("x-cpf-transaction-global-id", transactionGlobalId)
                .attribute("routingMode", "LB")
                .attribute("circuitPolicyId", "CPF-CIRCUIT-MBR-DEFAULT")
                .build();

        ServiceCallResolvedTarget target = toTarget(registry, instance, "LB");
        ServiceCallResult<Map<String, String>> result = ServiceCallResult.failure(
                target,
                503,
                504L,
                3,
                "CIRCUIT_OPEN",
                "회로 차단 정책에 의해 호출이 차단되었습니다.");
        return new CallScenario(request, result, history(transactionGlobalId, request, result));
    }

    private ServiceCallResolvedTarget toTarget(RegistrySnapshot registry, InstanceStatus instance, String routingMode) {
        return new ServiceCallResolvedTarget(
                registry.service(),
                registry.endpoint(),
                Map.of(
                        "instanceId", instance.instanceId(),
                        "baseUrl", instance.baseUrl(),
                        "healthStatus", instance.healthStatus()),
                Map.of("routingMode", routingMode, "failoverEnabledYn", "Y"),
                instance.baseUrl(),
                routingMode);
    }

    private CallHistoryRecord history(
            String transactionGlobalId,
            ServiceCallRequest request,
            ServiceCallResult<?> result) {
        return new CallHistoryRecord(
                transactionGlobalId,
                "SEG-CALL-001",
                "ACC",
                request.serviceId(),
                request.endpointCode(),
                result.target().instanceId(),
                result.target().routingMode(),
                result.status(),
                result.httpStatus(),
                result.durationMillis(),
                result.attemptCount(),
                result.failureCode(),
                Instant.parse("2026-07-09T03:00:00Z"));
    }

    public record RegistrySnapshot(
            Map<String, Object> service,
            Map<String, Object> endpoint,
            List<InstanceStatus> instances) {

        public RegistrySnapshot {
            service = Map.copyOf(service);
            endpoint = Map.copyOf(endpoint);
            instances = List.copyOf(instances);
        }

        public InstanceStatus findInstance(String instanceId) {
            return instances.stream()
                    .filter(instance -> instance.instanceId().equals(instanceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 인스턴스입니다. instanceId=" + instanceId));
        }
    }

    public record InstanceStatus(
            String instanceId,
            String baseUrl,
            String healthStatus,
            long inFlightCount) {
    }

    public record CallScenario(
            ServiceCallRequest request,
            ServiceCallResult<Map<String, String>> result,
            CallHistoryRecord history) {
    }

    public record CallHistoryRecord(
            String transactionGlobalId,
            String segmentId,
            String sourceModule,
            String targetModule,
            String endpointCode,
            String selectedInstanceId,
            String routingMode,
            String status,
            Integer httpStatus,
            Long durationMillis,
            Integer attemptCount,
            String failureCode,
            Instant loggedAt) {
    }
}
