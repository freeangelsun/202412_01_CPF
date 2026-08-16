package com.cpf.common.message.api;

import java.util.Objects;
import java.time.Instant;

/** CMN_MESSAGE의 Public 관리/조회 projection입니다. */
public record CpfMessageRecord(
        long messageId,
        String messageCode,
        String locale,
        String messageFormatType,
        String externalMessage,
        String internalMessage,
        int parameterCount,
        String parameterSample,
        String parameterSchemaJson,
        String escapeHtmlYn,
        String maskArgumentsYn,
        Instant effectiveFrom,
        Instant effectiveTo,
        long catalogVersion,
        String description,
        String useYn,
        Instant updatedAt) {
    /** activeAt 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean activeAt(Instant now) {
        Instant instant = Objects.requireNonNull(now, "now");
        return "Y".equalsIgnoreCase(useYn)
                && (effectiveFrom == null || !instant.isBefore(effectiveFrom))
                && (effectiveTo == null || instant.isBefore(effectiveTo));
    }
}
