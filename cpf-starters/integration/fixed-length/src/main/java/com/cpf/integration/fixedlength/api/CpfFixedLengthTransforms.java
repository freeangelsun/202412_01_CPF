package com.cpf.integration.fixedlength.api;

import com.cpf.foundation.util.CpfJson;
import java.util.List;
import java.util.Map;

/** 고정길이 전문 ↔ Map/List/JSON 변환을 단순화하는 공개 helper입니다. */
public final class CpfFixedLengthTransforms {
    private CpfFixedLengthTransforms(){}
    public static Map<String,Object> toMap(CpfFixedLengthParser parser,String message,CpfFixedLengthLayout layout){
        CpfFixedLengthParseResult r=parser.parse(message,layout); requireValid(r); return r.typedFields();
    }
    public static List<Map<String,Object>> group(CpfFixedLengthParser parser,String message,CpfFixedLengthLayout layout,String groupName){
        CpfFixedLengthParseResult r=parser.parse(message,layout); requireValid(r); return r.typedGroups().getOrDefault(groupName,List.of());
    }
    /** toJson 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String toJson(CpfFixedLengthParser parser,String message,CpfFixedLengthLayout layout){ return CpfJson.write(toMap(parser,message,layout)); }
    public static String fromMap(CpfFixedLengthWriter writer,Map<String,?> values,CpfFixedLengthLayout layout){
        CpfFixedLengthWriteResult r=writer.write(values,layout); return r.message();
    }
    public static String fromJson(CpfFixedLengthWriter writer,String json,CpfFixedLengthLayout layout){ return fromMap(writer,CpfJson.map(json),layout); }
    private static void requireValid(CpfFixedLengthParseResult r){ if(!r.valid()) throw new CpfFixedLengthException("고정길이 전문 해석에 실패했습니다.", r.errors()); }
}
