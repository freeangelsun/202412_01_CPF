package com.cpf.core.api.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** 입력 순서를 보존하는 immutable Map과 안전한 값 조회를 제공하는 CPF API입니다. */
public final class CpfMaps {
    private CpfMaps() {}

    /** null/empty Map을 불변 Map으로 정규화하고 입력 순서를 보존합니다.

     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.

     * @return 계약에 따른 결과 Map

     */

    public static <K,V> Map<K,V> emptyIfNull(Map<K,V> values) {
        if (values == null || values.isEmpty()) return Map.of();
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    /** Map 값을 nullable 문자열로 읽습니다.

     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.

     * @param key 조회/저장할 key

     * @return 정규화/변환된 문자열 또는 계약상 null

     */

    public static String string(Map<String,?> values, String key) {
        if (values == null) return null;
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

        /** key extractor로 순서 보존 index를 만들며 중복 key를 fail-fast 합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @param keyExtractor 각 원소에서 key를 계산할 함수
     * @return 계약에 따른 결과 Map
     * @throws IllegalArgumentException 중복 key가 생성된 경우
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
    /** nullable Map에서 key 값을 읽고 없으면 fallback을 반환합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @param key 조회/저장할 key
     * @param fallback 입력이 blank/null일 때 사용할 대체 값
     * @return 계약에 따른 결과 값
     */
    public static <K,V> V getOrDefault(Map<K,V> values, K key, V fallback) { return values == null ? fallback : values.getOrDefault(key, fallback); }
    /** 입력 Map을 순서 보존 mutable 복사본으로 만듭니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return 계약에 따른 결과 Map
     */
    public static Map<String,Object> mutableCopy(Map<String,?> values) {
        java.util.LinkedHashMap<String,Object> copy = new java.util.LinkedHashMap<>(); if(values!=null) values.forEach(copy::put); return copy;
    }
    /** 임의 key/value Map을 문자열 Map으로 변환해 불변화합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return 계약에 따른 결과 Map
     */
    public static Map<String,String> stringMap(Map<?,?> values) {
        if(values==null||values.isEmpty()) return Map.of(); java.util.LinkedHashMap<String,String> result=new java.util.LinkedHashMap<>();
        values.forEach((k,v)->result.put(String.valueOf(k), v==null?null:String.valueOf(v))); return java.util.Collections.unmodifiableMap(result);
    }
}
