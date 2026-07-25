package com.cpf.core.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/** JSON ↔ DTO/Map/List 변환을 일관되게 제공하는 공개 utility입니다. */
public final class CpfJson {
    private static final ObjectMapper DEFAULT = JsonMapper.builder().findAndAddModules().build();
    private CpfJson(){}
    public static String write(Object value){ try{return DEFAULT.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON 직렬화에 실패했습니다.",e);} }
    public static <T> T read(String json,Class<T> type){ try{return DEFAULT.readValue(json,type);}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.",e);} }
    public static Map<String,Object> map(String json){ try{return DEFAULT.readValue(json,new TypeReference<>(){});}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON Map 변환에 실패했습니다.",e);} }
    public static List<Map<String,Object>> list(String json){ try{return DEFAULT.readValue(json,new TypeReference<>(){});}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON List 변환에 실패했습니다.",e);} }
    public static Map<String,Object> toMap(Object value){ return DEFAULT.convertValue(value,new TypeReference<>(){}); }
    public static <T> T convert(Object value,Class<T> type){ return DEFAULT.convertValue(value,type); }
}
