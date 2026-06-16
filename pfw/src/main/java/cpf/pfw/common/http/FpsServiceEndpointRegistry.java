package cpf.pfw.common.http;

import cpf.pfw.common.exception.FpsFrameworkErrorCode;
import cpf.pfw.common.exception.FpsFrameworkException;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves service ids to configured base URLs.
 *
 * <p>The actual endpoints are managed in {@code application-pfw.yml} under {@code cpf.services.*}.</p>
 */
public class FpsServiceEndpointRegistry {

    private final Map<String, FpsServiceEndpointProperties.ServiceEndpoint> services;

    public FpsServiceEndpointRegistry(FpsServiceEndpointProperties properties) {
        this.services = properties.getServices();
    }

    /**
     * Returns the base URL for a service id.
     *
     * @param serviceId service id such as {@code mbr}, {@code acc}, or {@code cmn}
     * @return normalized base URL without trailing slash
     */
    public String baseUrl(String serviceId) {
        String normalizedServiceId = normalize(serviceId);
        FpsServiceEndpointProperties.ServiceEndpoint endpoint = services.get(normalizedServiceId);

        if (endpoint == null || !hasText(endpoint.getBaseUrl())) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "Service endpoint is not configured. Check cpf.services." + normalizedServiceId + ".base-url.",
                    Map.of("serviceId", normalizedServiceId));
        }
        return trimTrailingSlash(endpoint.getBaseUrl());
    }

    private String normalize(String serviceId) {
        if (!hasText(serviceId)) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
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
