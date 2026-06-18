package cpf.pfw.common.exception;

import java.util.Locale;
import java.util.Map;

/**
 * Fallback resolver used when CMN DB-backed catalogs are not available.
 */
public class DefaultCpfResponseCodeResolver implements CpfResponseCodeResolver {
    @Override
    public CpfResolvedResponse resolve(
            String responseCode,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        String resolvedCode = hasText(responseCode) ? responseCode : CpfErrorCode.INTERNAL_SERVER_ERROR.getStatusCode();
        boolean failure = resolvedCode.startsWith("E");
        String messageCode = failure ? CpfErrorCode.INTERNAL_SERVER_ERROR.getMessageCode() : defaultMessageCode(resolvedCode);
        String message = failure ? "泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎." : "?뺤긽 泥섎━?섏뿀?듬땲??";
        return new CpfResolvedResponse(
                failure ? 500 : 200,
                resolvedCode,
                messageCode,
                message,
                hasText(detail) ? detail : message,
                failure ? resolvedCode : null,
                failure ? firstText(detail, message) : null);
    }

    @Override
    public CpfResolvedResponse resolve(
            CpfErrorDefinition errorDefinition,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        if (errorDefinition == null) {
            return resolve((String) null, locale, arguments, detail);
        }
        String externalMessage = CpfMessageFormatter.format(errorDefinition.getDefaultExternalMessage(), arguments);
        String internalMessage = CpfMessageFormatter.format(errorDefinition.getDefaultInternalMessage(), arguments);
        String errorMessage = firstText(detail, internalMessage);
        return new CpfResolvedResponse(
                errorDefinition.getHttpStatus().value(),
                errorDefinition.getStatusCode(),
                errorDefinition.getMessageCode(),
                externalMessage,
                internalMessage,
                errorDefinition.getStatusCode(),
                errorMessage);
    }

    private String defaultMessageCode(String responseCode) {
        if (responseCode != null && responseCode.length() >= 4) {
            return "M" + responseCode.substring(1);
        }
        return "MPFW000000";
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

