package com.cpf.foundation.attribute;
/** CPF 공통 확장 속성을 불변 Map 형태로 전달하기 위한 공개 값 객체입니다. */
public final class CpfAttributes {
    private final Map<String,Object> values;
    private CpfAttributes(Map<String,Object> values) { this.values=Collections.unmodifiableMap(values); }
    public static CpfAttributes empty() { return new CpfAttributes(Map.of()); }
    public static CpfAttributes copyOf(Map<String,?> source) { var m=new LinkedHashMap<String,Object>(); if(source!=null) source.forEach((k,v)->m.put(requireKey(k),v)); return new CpfAttributes(m); }
    public CpfAttributes with(String key,Object value) { var m=new LinkedHashMap<>(values); m.put(requireKey(key),value); return new CpfAttributes(m); }
    public Optional<Object> get(String key) { return Optional.ofNullable(values.get(key)); }
    public <T> Optional<T> get(String key,Class<T> type) { Object value=values.get(key); return value==null?Optional.empty():Optional.of(type.cast(value)); }
    public Map<String,Object> asMap() { return values; }
    private static String requireKey(String key) { if(key==null||key.isBlank()) throw new IllegalArgumentException("attribute key is required"); return key.trim(); }
}
