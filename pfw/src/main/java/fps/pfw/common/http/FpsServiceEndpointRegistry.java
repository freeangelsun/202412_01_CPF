package fps.pfw.common.http;

import fps.pfw.common.exception.FpsFrameworkErrorCode;
import fps.pfw.common.exception.FpsFrameworkException;

import java.util.Locale;
import java.util.Map;

/**
 * 서비스 ID로 대상 서버 주소를 찾아주는 공통 레지스트리입니다.
 * 실제 주소는 application-pfw.yml 또는 환경변수에서 주입합니다.
 */
public class FpsServiceEndpointRegistry {

    private final Map<String, FpsServiceEndpointProperties.ServiceEndpoint> services;

    public FpsServiceEndpointRegistry(FpsServiceEndpointProperties properties) {
        this.services = properties.getServices();
    }

    /**
     * 서비스 ID에 해당하는 base-url을 반환합니다.
     *
     * @param serviceId mbr, acc, cmn 같은 주제영역 ID
     * @return 대상 서비스 기본 URL
     */
    public String baseUrl(String serviceId) {
        String normalizedServiceId = normalize(serviceId);
        FpsServiceEndpointProperties.ServiceEndpoint endpoint = services.get(normalizedServiceId);

        if (endpoint == null || !hasText(endpoint.getBaseUrl())) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "서비스 접속 정보가 없습니다. fps.services." + normalizedServiceId + ".base-url 설정을 확인하세요.",
                    Map.of("serviceId", normalizedServiceId));
        }
        return trimTrailingSlash(endpoint.getBaseUrl());
    }

    private String normalize(String serviceId) {
        if (!hasText(serviceId)) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "서비스 ID는 필수입니다.",
                    Map.of("serviceId", "EMPTY"));
        }
        return serviceId.trim().toLowerCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String baseUrl) {
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
