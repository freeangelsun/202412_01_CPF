package com.cpf.foundation.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** 기술중립 Map Utility입니다. */
public final class CpfMaps {
    private CpfMaps() {
    }

    public static <K, V> Map<K, V> mutableCopy(Map<? extends K, ? extends V> source) {
        return source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
    }
}
