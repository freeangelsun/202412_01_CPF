package cpf.pfw.common.http;

import cpf.pfw.common.exception.FpsFrameworkErrorCode;
import cpf.pfw.common.exception.FpsFrameworkException;

import java.util.Locale;
import java.util.Map;

/**
 * ?쒕퉬??ID濡?????쒕쾭 二쇱냼瑜?李얠븘二쇰뒗 怨듯넻 ?덉??ㅽ듃由ъ엯?덈떎.
 * ?ㅼ젣 二쇱냼??application-pfw.yml ?먮뒗 ?섍꼍蹂?섏뿉??二쇱엯?⑸땲??
 */
public class FpsServiceEndpointRegistry {

    private final Map<String, FpsServiceEndpointProperties.ServiceEndpoint> services;

    public FpsServiceEndpointRegistry(FpsServiceEndpointProperties properties) {
        this.services = properties.getServices();
    }

    /**
     * ?쒕퉬??ID???대떦?섎뒗 base-url??諛섑솚?⑸땲??
     *
     * @param serviceId mbr, acc, cmn 媛숈? 二쇱젣?곸뿭 ID
     * @return ????쒕퉬??湲곕낯 URL
     */
    public String baseUrl(String serviceId) {
        String normalizedServiceId = normalize(serviceId);
        FpsServiceEndpointProperties.ServiceEndpoint endpoint = services.get(normalizedServiceId);

        if (endpoint == null || !hasText(endpoint.getBaseUrl())) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "?쒕퉬???묒냽 ?뺣낫媛 ?놁뒿?덈떎. cpf.services." + normalizedServiceId + ".base-url ?ㅼ젙???뺤씤?섏꽭??",
                    Map.of("serviceId", normalizedServiceId));
        }
        return trimTrailingSlash(endpoint.getBaseUrl());
    }

    private String normalize(String serviceId) {
        if (!hasText(serviceId)) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,
                    "?쒕퉬??ID???꾩닔?낅땲??",
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

