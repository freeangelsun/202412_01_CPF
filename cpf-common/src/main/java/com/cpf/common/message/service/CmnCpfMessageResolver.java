package com.cpf.common.message.service;

import com.cpf.core.api.error.CpfErrorDefinition;
import com.cpf.foundation.message.CpfMessageResolver;
import com.cpf.foundation.message.CpfResolvedMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** DB 캐시 메시지를 우선 사용하고 누락 시 오류 정의 기본값으로 대체합니다. */
@Primary
@Component
public class CmnCpfMessageResolver implements CpfMessageResolver {
    private final CmnErrorCatalogStore catalog;

    CmnCpfMessageResolver(CmnErrorCatalogStore catalog) {
        this.catalog = catalog;
    }

    /** Locale 언어 코드에 맞는 외부·내부 메시지를 해석합니다. */
    @Override
    public CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale) {
        String language = locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()
                ? "ko"
                : locale.getLanguage();

        var message = catalog.message(errorCode.getMessageCode(), Locale.forLanguageTag(language));
        String externalMessage = message == null || message.externalMessage() == null ? errorCode.getDefaultExternalMessage() : message.externalMessage();
        String internalMessage = message == null || message.internalMessage() == null ? errorCode.getDefaultInternalMessage() : message.internalMessage();

        return new CpfResolvedMessage(externalMessage, internalMessage);
    }


}
