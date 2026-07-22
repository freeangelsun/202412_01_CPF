package com.cpf.common.tlm.core;

import java.util.List;
import java.util.Map;

/** 고정길이 전문의 원문·필드별 값·JSON 변환값·길이 경고를 함께 제공합니다. */
public record CmnTelegramParseResult(
        String originalText,
        int expectedLength,
        int actualLength,
        Map<String, String> rawFields,
        Map<String, Object> typedFields,
        String json,
        List<String> warnings) {
}

