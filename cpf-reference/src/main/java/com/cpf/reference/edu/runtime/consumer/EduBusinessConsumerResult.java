package com.cpf.reference.edu.runtime.consumer;
import java.util.*;
/** Sanitized result returned by a concrete product consumer. */
public record EduBusinessConsumerResult(String code,String message,Map<String,Object> data,boolean effectCommitted,boolean externalPending) {
    public EduBusinessConsumerResult {
        code=code==null?"":code;
        message=message==null?"":message;
        data=Map.copyOf(data==null?Map.of():data);
    }
    public static EduBusinessConsumerResult completed(String code,Map<String,Object> data){return new EduBusinessConsumerResult(code,"completed",data,true,false);}
    public static EduBusinessConsumerResult pending(String code,Map<String,Object> data){return new EduBusinessConsumerResult(code,"external acknowledgement pending",data,true,true);}
}
