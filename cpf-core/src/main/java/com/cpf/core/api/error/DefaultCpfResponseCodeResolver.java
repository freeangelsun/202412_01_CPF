package com.cpf.core.api.error;

import java.util.Locale;
import java.util.Map;

/** DB 기반 응답 카탈로그가 없을 때 표준 기본 응답을 조립하는 공개 구현입니다. */
public class DefaultCpfResponseCodeResolver implements CpfResponseCodeResolver {
    @Override
    public CpfResolvedResponse resolve(
            String responseCode,
            Locale locale,
            Map<String, Object> arguments,
            String detail) {
        String resolvedCode = hasText(responseCode)
                ? responseCode
                : CpfErrorCode.INTERNAL_SERVER_ERROR.statusCode();
        boolean failure = resolvedCode.startsWith("E");
        String messageCode = failure
                ? CpfErrorCode.INTERNAL_SERVER_ERROR.messageCode()
                : defaultMessageCode(resolvedCode);
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
        String externalMessage = CpfMessageFormatter.format(
                errorDefinition.getDefaultExternalMessage(), arguments);
        String internalMessage = CpfMessageFormatter.format(
                errorDefinition.getDefaultInternalMessage(), arguments);
        return new CpfResolvedResponse(
                errorDefinition.getHttpStatus().value(),
                errorDefinition.getStatusCode(),
                errorDefinition.getMessageCode(),
                externalMessage,
                internalMessage,
                errorDefinition.getStatusCode(),
                firstText(detail, internalMessage));
    }

    private static String defaultMessageCode(String responseCode) {
        return responseCode != null && responseCode.length() >= 4
                ? "M" + responseCode.substring(1)
                : "MCPF000000";
    }

    private static String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
