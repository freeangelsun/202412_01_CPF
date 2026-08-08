package com.cpf.core.api.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Header/Context/Extension 속성을 raw Map으로 흩뿌리지 않기 위한 작은 value object입니다. */
public final class CpfAttributes {
    private final Map<String,Object> values = new LinkedHashMap<>();

    /** attribute key를 검증하고 값을 추가하며 null 값은 기존 key를 제거합니다.

     * @param key 조회/저장할 key

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @return 검증된 CPF 값 객체

     */

    public CpfAttributes put(String key, Object value) {
        String normalized = CpfStrings.requireText(key, "attribute key");
        if (value == null) values.remove(normalized); else values.put(normalized, value);
        return this;
    }

    /** key의 원본 값을 nullable로 조회합니다.

     * @param key 조회/저장할 key

     * @return 저장된 원본 값 또는 없으면 null

     */

    public Object get(String key) { return values.get(key); }
    /** key 값을 nullable 문자열로 조회합니다.
     * @param key 조회/저장할 key
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public String getString(String key) { Object v = values.get(key); return v == null ? null : String.valueOf(v); }
    /** key 존재 여부를 확인합니다.
     * @param key 조회/저장할 key
     * @return 조건을 만족하면 true, 아니면 false
     */
    public boolean contains(String key) { return values.containsKey(key); }

        /** 내부 Map 변경을 차단하는 순서 보존 불변 snapshot을 반환합니다.
     * @return 계약에 따른 결과 Map
     */
    public Map<String,Object> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
