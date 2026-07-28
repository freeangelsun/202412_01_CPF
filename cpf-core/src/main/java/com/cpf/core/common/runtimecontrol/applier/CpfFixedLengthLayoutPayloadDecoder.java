package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.fixedlength.CpfFixedLengthAlignment;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldType;
import com.cpf.core.api.fixedlength.CpfFixedLengthGroupSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class CpfFixedLengthLayoutPayloadDecoder {
    CpfFixedLengthLayout decode(Map<String,Object> value) {
        String id = text(value.get("layoutId"), "layoutId");
        String version = text(value.get("version"), "version");
        Charset charset = Charset.forName(optional(value.get("charset"), "UTF-8"));
        int total = integer(value.get("totalLength"), "totalLength");
        List<CpfFixedLengthFieldSpec> fields = fields(value.get("fields"));
        List<CpfFixedLengthGroupSpec> groups = groups(value.get("groups"));
        return new CpfFixedLengthLayout(id, version, charset, total, fields, groups);
    }
    @SuppressWarnings("unchecked")
    private List<CpfFixedLengthFieldSpec> fields(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) throw new IllegalArgumentException("fields 필수");
        List<CpfFixedLengthFieldSpec> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?,?> raw)) throw new IllegalArgumentException("field object 필요");
            Map<String,Object> field=(Map<String,Object>)raw;
            result.add(new CpfFixedLengthFieldSpec(
                    text(field.get("name"),"field.name"), integer(field.get("start"),"field.start"),
                    integer(field.get("length"),"field.length"),
                    CpfFixedLengthFieldType.valueOf(optional(field.get("type"),"STRING").toUpperCase()),
                    bool(field.get("required"),false), character(field.get("padding"),'\0'),
                    CpfFixedLengthAlignment.valueOf(optional(field.get("alignment"),"AUTO").toUpperCase()),
                    bool(field.get("sensitive"),false), number(field.get("scale"),0),
                    optional(field.get("defaultValue"),""), bool(field.get("trim"),true),
                    optional(field.get("converterId"),"")));
        }
        return List.copyOf(result);
    }
    @SuppressWarnings("unchecked")
    private List<CpfFixedLengthGroupSpec> groups(Object value) {
        if (value == null) return List.of();
        if (!(value instanceof List<?> list)) throw new IllegalArgumentException("groups array 필요");
        List<CpfFixedLengthGroupSpec> result=new ArrayList<>();
        for(Object item:list){
            if(!(item instanceof Map<?,?> raw))throw new IllegalArgumentException("group object 필요");
            Map<String,Object> group=(Map<String,Object>)raw;
            result.add(new CpfFixedLengthGroupSpec(text(group.get("name"),"group.name"),
                    text(group.get("countFieldName"),"group.countFieldName"),
                    number(group.get("start"),0),integer(group.get("maxCount"),"group.maxCount"),
                    fields(group.get("fields"))));
        }
        return List.copyOf(result);
    }
    private String text(Object v,String name){if(v==null||String.valueOf(v).isBlank())throw new IllegalArgumentException(name+" 필수");return String.valueOf(v).trim();}
    private String optional(Object v,String fallback){return v==null?fallback:String.valueOf(v).trim();}
    private int integer(Object v,String name){int n=number(v,-1);if(n<0)throw new IllegalArgumentException(name+" 범위 오류");return n;}
    private int number(Object v,int fallback){return v instanceof Number n?n.intValue():v==null?fallback:Integer.parseInt(String.valueOf(v));}
    private boolean bool(Object v,boolean fallback){return v instanceof Boolean b?b:v==null?fallback:Boolean.parseBoolean(String.valueOf(v));}
    private char character(Object v,char fallback){String s=optional(v,"");return s.isEmpty()?fallback:s.charAt(0);}
}
