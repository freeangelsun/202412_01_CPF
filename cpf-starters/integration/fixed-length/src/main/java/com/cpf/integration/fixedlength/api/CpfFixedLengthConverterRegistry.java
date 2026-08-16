package com.cpf.integration.fixedlength.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain별 CUSTOM 필드 converter를 ID로 등록하는 경량 registry입니다.
 * 표준 STRING/NUMBER/DATE 등은 등록 없이 기본 Codec이 처리합니다.
 */
public final class CpfFixedLengthConverterRegistry {
    private final Map<String, CpfFixedLengthValueConverter> converters = new ConcurrentHashMap<>();

    public void register(String id, CpfFixedLengthValueConverter converter) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("converter id는 필수입니다.");
        if (converter == null) throw new IllegalArgumentException("converter는 필수입니다.");
        converters.put(id.trim(), converter);
    }

    /** require는 고정길이 전문을 byte-length·layout 계약에 맞춰 검증하고 변환합니다. */
    public CpfFixedLengthValueConverter require(String id) {
        CpfFixedLengthValueConverter converter = id == null ? null : converters.get(id.trim());
        if (converter == null) throw new IllegalArgumentException("등록된 fixed-length converter가 없습니다: " + id);
        return converter;
    }
}
