package com.cpf.common.msg.service;

import com.cpf.core.common.exception.CpfErrorDefinition;
import com.cpf.core.common.exception.CpfMessageResolver;
import com.cpf.core.common.exception.CpfResolvedMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/** DB 캐시 메시지를 우선 사용하고 누락 시 오류 정의 기본값으로 대체합니다. */
@Primary
@Component
public class CmnCpfMessageResolver implements CpfMessageResolver {
    private final MessageCacheService messageCacheService;

    public CmnCpfMessageResolver(MessageCacheService messageCacheService) {
        this.messageCacheService = messageCacheService;
    }

    /** Locale 언어 코드에 맞는 외부·내부 메시지를 해석합니다. */
    @Override
    public CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale) {
        String language = locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()
                ? "ko"
                : locale.getLanguage();

        Map<String, Object> message = messageCacheService.getMessageByKeyAndLocale(errorCode.getMessageCode(), language);
        String externalMessage = mapValue(message, "externalMessage", "external_message", errorCode.getDefaultExternalMessage());
        String internalMessage = mapValue(message, "internalMessage", "internal_message", errorCode.getDefaultInternalMessage());

        return new CpfResolvedMessage(externalMessage, internalMessage);
    }

    private String mapValue(Map<String, Object> message, String camelKey, String snakeKey, String fallback) {
        if (message == null || message.isEmpty()) {
            return fallback;
        }

        Object value = message.get(camelKey);
        if (value == null) {
            value = message.get(snakeKey);
        }
        if (value == null && snakeKey != null) {
            value = message.get(snakeKey.toUpperCase());
        }
        return value == null ? fallback : String.valueOf(value);
    }
}

