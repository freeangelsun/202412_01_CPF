package com.cpf.common.message.api;

import com.cpf.core.api.error.CpfErrorDefinition;
import com.cpf.core.api.error.CpfResolvedErrorView;
import java.util.Locale;
import java.util.Objects;

/** Common Error Catalog가 해석한 기술중립 응답/메시지 결과입니다. */
public record CpfResolvedError(
        String responseCode,
        String messageCode,
        CpfErrorDefinition definition,
        String externalMessage,
        String internalMessage,
        Locale locale,
        boolean catalogHit) implements CpfResolvedErrorView {

    public CpfResolvedError {
        responseCode = require(responseCode, "responseCode");
        messageCode = require(messageCode, "messageCode");
        definition = Objects.requireNonNull(definition, "definition");
        externalMessage = require(externalMessage, "externalMessage");
        internalMessage = require(internalMessage, "internalMessage");
        locale = locale == null ? Locale.KOREAN : locale;
    }

    /** category 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfErrorDefinition.Category category() { return definition.category(); }
    public CpfErrorDefinition.RetryDisposition retryDisposition() { return definition.retryDisposition(); }
    public CpfErrorDefinition.Exposure exposure() { return definition.exposure(); }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
}
