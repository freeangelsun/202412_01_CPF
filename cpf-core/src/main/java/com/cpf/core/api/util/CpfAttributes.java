package com.cpf.core.api.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Header/Context/Extension 속성을 raw Map으로 흩뿌리지 않기 위한 작은 value object입니다. */
public final class CpfAttributes {
    private final Map<String,Object> values = new LinkedHashMap<>();

    public CpfAttributes put(String key, Object value) {
        String normalized = CpfStrings.requireText(key, "attribute key");
        if (value == null) values.remove(normalized); else values.put(normalized, value);
        return this;
    }

    public Object get(String key) { return values.get(key); }
    public String getString(String key) { Object v = values.get(key); return v == null ? null : String.valueOf(v); }
    public boolean contains(String key) { return values.containsKey(key); }

    /** 호출자가 내부 Map을 변경하지 못하도록 순서 보존 snapshot을 반환합니다. */
    public Map<String,Object> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
