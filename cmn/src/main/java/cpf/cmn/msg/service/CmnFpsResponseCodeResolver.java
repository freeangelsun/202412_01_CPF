package cpf.cmn.msg.service;

import cpf.pfw.common.exception.DefaultFpsResponseCodeResolver;
import cpf.pfw.common.exception.FpsErrorDefinition;
import cpf.pfw.common.exception.FpsMessageFormatter;
import cpf.pfw.common.exception.FpsResolvedResponse;
import cpf.pfw.common.exception.FpsResponseCodeResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * DB/cache backed response-code resolver used by PFW exception handling and logging.
 */
@Primary
@Component
public class CmnFpsResponseCodeResolver implements FpsResponseCodeResolver {
    private final ResponseCodeCacheService responseCodeCacheService;
    private final MessageCacheService messageCacheService;
    private final DefaultFpsResponseCodeResolver fallback = new DefaultFpsResponseCodeResolver();

    public CmnFpsResponseCodeResolver(
            ResponseCodeCacheService responseCodeCacheService,
            MessageCacheService messageCacheService) {
        this.responseCodeCacheService = responseCodeCacheService;
        this.messageCacheService = messageCacheService;
    }

    @Override
    public FpsResolvedResponse resolve(
            String responseCode,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        Map<String, Object> response = findResponseCode(responseCode);
        if (response == null || response.isEmpty()) {
            return fallback.resolve(responseCode, locale, arguments, detail);
        }
        return resolveFromCatalog(response, locale, arguments, detail);
    }

    @Override
    public FpsResolvedResponse resolve(
            FpsErrorDefinition errorDefinition,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        if (errorDefinition == null) {
            return fallback.resolve((String) null, locale, arguments, detail);
        }
        Map<String, Object> response = findResponseCode(errorDefinition.getStatusCode());
        if (response == null || response.isEmpty()) {
            return fallback.resolve(errorDefinition, locale, arguments, detail);
        }
        return resolveFromCatalog(response, locale, arguments, detail);
    }

    private FpsResolvedResponse resolveFromCatalog(
            Map<String, Object> response,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        String responseCode = mapValue(response, "responseCode", "response_code", null);
        String messageCode = mapValue(response, "messageCode", "message_code", null);
        String resultType = mapValue(response, "resultType", "result_type", "");
        int httpStatus = intValue(mapValue(response, "httpStatus", "http_status", null), resultType.startsWith("E") ? 500 : 200);
        String language = locale != null && locale.getLanguage() != null && !locale.getLanguage().isBlank()
                ? locale.getLanguage()
                : "ko";

        Map<String, Object> message = findMessage(messageCode, language);
        if (message == null || message.isEmpty()) {
            message = findMessage(messageCode, "ko");
        }

        String externalTemplate = mapValue(message, "externalMessage", "external_message", responseCode);
        String internalTemplate = mapValue(message, "internalMessage", "internal_message", externalTemplate);
        String externalMessage = FpsMessageFormatter.format(externalTemplate, arguments);
        String internalMessage = FpsMessageFormatter.format(internalTemplate, arguments);
        boolean failure = responseCode != null && responseCode.startsWith("E");
        String errorMessage = failure
                ? firstText(detail, internalMessage)
                : null;

        return new FpsResolvedResponse(
                httpStatus,
                responseCode,
                messageCode,
                externalMessage,
                internalMessage,
                failure ? responseCode : null,
                errorMessage);
    }

    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey, String fallback) {
        if (source == null) {
            return fallback;
        }
        Object value = source.get(camelKey);
        if (value == null) {
            value = source.get(snakeKey);
        }
        if (value == null && snakeKey != null) {
            value = source.get(snakeKey.toUpperCase());
        }
        return value == null ? fallback : String.valueOf(value);
    }

    private Map<String, Object> findResponseCode(String responseCode) {
        if (responseCode == null || responseCode.isBlank()) {
            return null;
        }
        try {
            return responseCodeCacheService.getResponseCode(responseCode);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private Map<String, Object> findMessage(String messageCode, String language) {
        if (messageCode == null || messageCode.isBlank()) {
            return null;
        }
        try {
            return messageCacheService.getMessageByKeyAndLocale(messageCode, language);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int intValue(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}

