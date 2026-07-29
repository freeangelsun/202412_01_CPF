package com.cpf.admin.opr.dto;
import com.cpf.core.api.cache.*;
import java.util.List;
/** ADM Cache 운영 요약 응답입니다. */
public record AdmCacheSummaryResponse(boolean available,CpfCacheHealth providerHealth,CpfCacheMetricsSnapshot metrics,long durableBacklog,List<DomainStatus> domains,String message){
 public AdmCacheSummaryResponse{domains=domains==null?List.of():List.copyOf(domains);message=message==null?"":message;}
 public record DomainStatus(String domain,String status,String sample){public DomainStatus{sample=sample==null?"":sample;}}
}
