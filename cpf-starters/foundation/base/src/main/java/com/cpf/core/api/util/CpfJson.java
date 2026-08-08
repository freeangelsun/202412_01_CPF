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
    /** 객체를 CPF 기본 ObjectMapper 설정으로 JSON 문자열로 직렬화합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException JSON 직렬화에 실패한 경우
     */
    public static String write(Object value){ try{return DEFAULT.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON 직렬화에 실패했습니다.",e);} }
    /** JSON 문자열을 지정 DTO 타입으로 역직렬화합니다.
     * @param json 역직렬화할 JSON 문자열
     * @param type 변환 대상 Class 타입
     * @return 계약에 따른 결과 값
     * @throws IllegalArgumentException JSON 역직렬화에 실패한 경우
     */
    public static <T> T read(String json,Class<T> type){ try{return DEFAULT.readValue(json,type);}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.",e);} }
    /** JSON object를 문자열 key Map으로 역직렬화합니다.
     * @param json 역직렬화할 JSON 문자열
     * @return 계약에 따른 결과 Map
     * @throws IllegalArgumentException JSON object 변환에 실패한 경우
     */
    public static Map<String,Object> map(String json){ try{return DEFAULT.readValue(json,new TypeReference<>(){});}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON Map 변환에 실패했습니다.",e);} }
    /** JSON array를 Map 목록으로 역직렬화합니다.
     * @param json 역직렬화할 JSON 문자열
     * @return null이 아닌 결과 목록
     * @throws IllegalArgumentException JSON array 변환에 실패한 경우
     */
    public static List<Map<String,Object>> list(String json){ try{return DEFAULT.readValue(json,new TypeReference<>(){});}catch(JsonProcessingException e){throw new IllegalArgumentException("JSON List 변환에 실패했습니다.",e);} }
    /** 객체를 Map 표현으로 변환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 계약에 따른 결과 Map
     */
    public static Map<String,Object> toMap(Object value){ return DEFAULT.convertValue(value,new TypeReference<>(){}); }
    /** 객체를 지정 타입으로 변환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param type 변환 대상 Class 타입
     * @return 계약에 따른 결과 값
     */
    public static <T> T convert(Object value,Class<T> type){ return DEFAULT.convertValue(value,type); }
}
