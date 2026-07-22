package cpf.pfw.common.http;

import cpf.pfw.common.exception.CpfFrameworkErrorCode;
import cpf.pfw.common.exception.CpfFrameworkException;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves service ids to configured base URLs.
 *
 * <p>The actual endpoints are managed in {@code application-pfw.yml} under {@code cpf.services.*}.</p>
 */
public class CpfServiceEndpointRegistry {

    private final Map<String, CpfServiceEndpointProperties.ServiceEndpoint> services;

    public CpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        this.services = properties.getServices();
    }

    /**
     * Returns the base URL for a service id.
     *
     * @param serviceId 서비스 ID 예: {@code mbr}, {@code bza}, {@code xyz}
     * @return normalized base URL without trailing slash
     */
    public String baseUrl(String serviceId) {
        String normalizedServiceId = normalize(serviceId);
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = services.get(normalizedServiceId);

        if (endpoint == null || !hasText(endpoint.getBaseUrl())) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "Service endpoint is not configured. Check cpf.services." + normalizedServiceId + ".base-url.",
                    Map.of("serviceId", normalizedServiceId));
        }
        return trimTrailingSlash(endpoint.getBaseUrl());
    }

    private String normalize(String serviceId) {
        if (!hasText(serviceId)) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "Service id is required.",
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
