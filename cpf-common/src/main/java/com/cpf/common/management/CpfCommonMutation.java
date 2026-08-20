package com.cpf.common.management;

import java.util.LinkedHashMap;
import java.util.Map;

/** Common 관리 mutation 입력입니다. identifiers/values는 Resource allowlist에서만 해석됩니다. */
public record CpfCommonMutation(Map<String,Object> identifiers, Map<String,Object> values, Long expectedVersion, String reason) {
    public CpfCommonMutation {
        identifiers = identifiers == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(identifiers));
        values = values == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
