package com.cpf.core.api.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** 입력 순서를 보존하는 immutable Map과 안전한 값 조회를 제공하는 CPF API입니다. */
public final class CpfMaps {
    private CpfMaps() {}

    public static <K,V> Map<K,V> emptyIfNull(Map<K,V> values) {
        if (values == null || values.isEmpty()) return Map.of();
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static String string(Map<String,?> values, String key) {
        if (values == null) return null;
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * key 기준으로 안정적인 순서 Map을 만듭니다.
     * 중복 key는 데이터 손실을 숨기지 않고 즉시 실패합니다.
     */
    public static <T,K> Map<K,T> indexBy(Iterable<T> values, Function<T,K> keyExtractor) {
        LinkedHashMap<K,T> result = new LinkedHashMap<>();
        if (values != null) {
            for (T value : values) {
                K key = keyExtractor.apply(value);
                if (result.putIfAbsent(key, value) != null) {
                    throw new IllegalArgumentException("중복 Map key입니다: " + key);
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
