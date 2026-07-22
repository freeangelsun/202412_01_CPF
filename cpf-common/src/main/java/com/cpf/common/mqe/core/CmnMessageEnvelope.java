package com.cpf.common.mqe.core;

import java.util.Map;

/** 브로커·목적지·거래 헤더·payload를 함께 전달하는 공통 메시지 봉투입니다. */
public record CmnMessageEnvelope(
        String broker,
        String destination,
        String key,
        Object payload,
        Map<String, String> headers,
        String createdAt) {
}

