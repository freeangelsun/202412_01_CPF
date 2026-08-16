package com.cpf.education.operations.runtime.application;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/** EduPayloadHasher 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class EduPayloadHasher {
    private EduPayloadHasher(){}
    public static String hash(Map<String,Object> payload){return sha256(canonical(payload));}
    private static String canonical(Object value){
        if(value==null)return "null";
        if(value instanceof Map<?,?> m){var keys=m.keySet().stream().map(String::valueOf).sorted().toList();StringJoiner j=new StringJoiner(",","{","}");for(String k:keys)j.add(escape(k)+":"+canonical(m.get(k)));return j.toString();}
        if(value instanceof Collection<?> c){StringJoiner j=new StringJoiner(",","[","]");for(Object o:c)j.add(canonical(o));return j.toString();}
        return escape(String.valueOf(value));
    }
    private static String escape(String v){return '"'+v.replace("\\","\\\\").replace("\"","\\\"")+'"';}
    /** sha256 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String sha256(String value){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(b);}catch(Exception e){throw new IllegalStateException(e);}}
}
