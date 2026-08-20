package com.cpf.common.message.api;

import java.util.Locale;
import java.util.Map;

/** CPF Common Message Catalog의 고객 Application용 조회 계약입니다. */
public interface CpfMessageSource {
    /** locale fallback, parameter schema, escaping/masking 정책을 적용하여 외부 노출용 message를 반환합니다. */
    String getMessage(String messageCode, Locale locale, Map<String, Object> arguments);

    /** Argument가 없는 Message의 간편 조회입니다. */
    default String getMessage(String messageCode, Locale locale) {
        return getMessage(messageCode, locale, Map.of());
    }
}
