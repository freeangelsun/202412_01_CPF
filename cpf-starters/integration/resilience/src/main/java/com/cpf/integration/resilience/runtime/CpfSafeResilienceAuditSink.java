package com.cpf.integration.resilience.runtime;

import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Payload를 기록하지 않고 resilience outcome 메타데이터만 남기는 기본 운영 audit sink입니다. */
public final class CpfSafeResilienceAuditSink implements CpfResilienceAuditSink {
    private static final Logger log=LoggerFactory.getLogger(CpfSafeResilienceAuditSink.class);
    @Override public void record(String eventType,String operationId,String actorId,String reason,Map<String,String> attrs,Instant at){
        log.info("CPF RESILIENCE event={} operation={} reason={} at={} attrs={}", safe(eventType),safe(operationId),safe(reason),at,safeAttrs(attrs));
    }
    private static String safe(String v){if(v==null)return null;v=v.replaceAll("[\\r\\n\\t]","_");return v.length()<=128?v:v.substring(0,128);}
    private static Map<String,String> safeAttrs(Map<String,String> in){
        if(in==null||in.isEmpty())return Map.of();java.util.LinkedHashMap<String,String> out=new java.util.LinkedHashMap<>();
        in.forEach((k,v)->{String key=safe(k);if(key!=null&&!key.matches("(?i).*(password|secret|token|authorization|credential|cookie).*"))out.put(key,safe(v));});
        return Map.copyOf(out);
    }
}
