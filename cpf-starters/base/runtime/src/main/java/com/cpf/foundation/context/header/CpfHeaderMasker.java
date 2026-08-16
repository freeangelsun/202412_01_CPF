package com.cpf.foundation.context.header;

import java.util.LinkedHashMap;
import java.util.Map;

/** 표준 Header의 로그/ADM 노출을 fail-closed로 마스킹합니다. */
public final class CpfHeaderMasker {
    private CpfHeaderMasker() {}
    public static Map<String,String> maskHeaders(Map<String,String> headers) {
        Map<String,String> out=new LinkedHashMap<>();
        if(headers==null) return out;
        headers.forEach((k,v)->{ if(v!=null&&!v.isBlank()) out.put(k,mask(k,v)); });
        return out;
    }
    public static String mask(String headerName,String value) {
        if(value==null||value.isBlank()) return value;
        if(!CpfHeaderSpecs.canLogRaw(headerName)) return "****";
        if(!CpfHeaderSpecs.shouldMask(headerName)) return value;
        int n=value.length();
        if(n<=4) return "****";
        return value.substring(0,2)+"****"+value.substring(n-2);
    }
}
