package com.cpf.education.operations.runtime.consumer;
import java.util.*;
/** Sanitized result returned by a concrete product consumer. */
/** EduBusinessConsumerResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduBusinessConsumerResult(String code,String message,Map<String,Object> data,boolean effectCommitted,boolean externalPending) {
    public EduBusinessConsumerResult {
        code=code==null?"":code;
        message=message==null?"":message;
        data=Map.copyOf(data==null?Map.of():data);
    }
    public static EduBusinessConsumerResult completed(String code,Map<String,Object> data){return new EduBusinessConsumerResult(code,"completed",data,true,false);}
    /** pending 작업을 CPF 표준 계약에 따라 수행한다. */
    public static EduBusinessConsumerResult pending(String code,Map<String,Object> data){return new EduBusinessConsumerResult(code,"external acknowledgement pending",data,true,true);}
}
