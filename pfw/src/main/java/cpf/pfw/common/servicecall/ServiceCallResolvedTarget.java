package cpf.pfw.common.servicecall;

import java.util.Map;

/**
 * 서비스 레지스트리 기준으로 선택된 실제 호출 대상입니다.
 */
public record ServiceCallResolvedTarget(
        Map<String, Object> service,
        Map<String, Object> endpoint,
        Map<String, Object> instance,
        Map<String, Object> routingPolicy,
        String baseUrl,
        String routingMode) {
}
