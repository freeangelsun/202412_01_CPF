package com.cpf.admin.opr.dto;
import com.cpf.core.api.cache.CpfCacheInvalidationEvent;
import java.time.Instant;
/** 위험 캐시 조치 결과입니다. */
public record AdmCacheControlResponse(String operation,String target,boolean accepted,long affected,CpfCacheInvalidationEvent durableEvent,long durableBacklog,Instant completedAt,String message){
 public AdmCacheControlResponse{completedAt=completedAt==null?Instant.now():completedAt;message=message==null?"":message;}
}
