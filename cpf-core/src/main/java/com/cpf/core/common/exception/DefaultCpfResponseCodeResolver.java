package cpf.pfw.common.exception;

import java.util.Locale;
import java.util.Map;

/**
 * CMN DB 기반 응답코드/메시지 카탈로그를 사용할 수 없을 때 기본 응답 메타를 조립합니다.
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
        String message = failure ? "CPF 처리 중 오류가 발생했습니다." : "정상 처리되었습니다.";
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
