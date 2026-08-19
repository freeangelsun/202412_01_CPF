package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmCacheControlResponse;
import com.cpf.admin.opr.dto.AdmCacheSummaryResponse;
import com.cpf.common.management.CpfCommonManagementApi;
import com.cpf.common.management.CpfCommonResource;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshListener;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshPublisher;
import com.cpf.data.cache.api.*;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.util.CpfStrings;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Business Cache와 Provider Cache를 canonical Common cache runtime으로 조회·제어합니다. */
@CpfService
public class AdmCacheOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfCommonManagementApi common;
    private final CpfCommonCacheRefreshPublisher refreshPublisher;
    private final CpfCommonCacheRefreshListener refreshListener;
    private final CpfCache provider;
    private final CpfCacheInvalidationPort invalidations;
    private final CpfCacheInvalidationCoordinator coordinator;

    public AdmCacheOperationService(
            CpfCommonManagementApi common,
            CpfCommonCacheRefreshPublisher refreshPublisher,
            CpfCommonCacheRefreshListener refreshListener,
            ObjectProvider<CpfCache> provider,
            ObjectProvider<CpfCacheInvalidationPort> invalidations,
            ObjectProvider<CpfCacheInvalidationCoordinator> coordinator) {
        this.common=common; this.refreshPublisher=refreshPublisher; this.refreshListener=refreshListener;
        this.provider=provider.getIfAvailable(); this.invalidations=invalidations.getIfAvailable(); this.coordinator=coordinator.getIfAvailable();
    }

    public AdmCacheSummaryResponse summary() {
        List<AdmCacheSummaryResponse.DomainStatus> domains=List.of(
                domain("CODE","CPFDB",sample(CpfCommonResource.CODE,"USER_STATUS")),
                domain("MESSAGE","CPFDB",sample(CpfCommonResource.MESSAGE,"MCMN")),
                domain("RESPONSE_CODE","CPFDB",sample(CpfCommonResource.RESPONSE_CODE,"ECPF")),
                domain("CONFIG","CPFDB",sample(CpfCommonResource.PARAMETER,"CPF.")),
                domain("DURABLE_REFRESH",refreshListener.status(),Map.of("consumer",refreshListener.status().consumerId(),"lastEventId",refreshListener.status().lastEventId())));
        CpfCacheHealth health=provider==null
                ?new CpfCacheHealth(false,"NONE","NONE",false,invalidations!=null,0,List.of("CACHE_PROVIDER_NOT_CONFIGURED"),Instant.now())
                :provider.health();
        CpfCacheMetricsSnapshot metrics=provider==null
                ?new CpfCacheMetricsSnapshot("NONE",0,0,0,0,0,0,backlog(),Instant.now())
                :provider.metrics();
        return new AdmCacheSummaryResponse(true,health,metrics,backlog(),domains,
                health.ready()&&coordinator!=null?"정상":"Cache Provider와 Durable Coordinator 상태를 확인하세요.");
    }

    public AdmCacheControlResponse refresh(String target,String operator,String reason) {
        String n=CpfStrings.normalizeCode(target); if(!CpfStrings.hasText(n))n="ALL"; long affected=0;
        if("ALL".equals(n)||"CODE".equals(n)){refreshPublisher.publishOutOfBand("codeCache","MANUAL_REFRESH","ALL",operator);affected++;}
        if("ALL".equals(n)||"MESSAGE".equals(n)){refreshPublisher.publishOutOfBand("messageCache","MANUAL_REFRESH","ALL",operator);affected++;}
        if("ALL".equals(n)||"RESPONSE_CODE".equals(n)){refreshPublisher.publishOutOfBand("responseCodeCache","MANUAL_REFRESH","ALL",operator);affected++;}
        if("ALL".equals(n)||"CONFIG".equals(n)){refreshPublisher.publishOutOfBand("configCache","MANUAL_REFRESH","ALL",operator);affected++;}
        return result("REFRESH",n,affected,null,"Common cpfDB Durable Refresh Event를 발행했습니다.");
    }

    public AdmCacheControlResponse evictKey(String tenant,String namespace,String key,long version,String operator,String reason){
        CpfCacheInvalidationCoordinator active=requireCoordinator(); CpfCacheKey cacheKey=new CpfCacheKey(namespace,key,tenant);
        CpfCacheInvalidationEvent event=active.request(UUID.randomUUID().toString(),cacheKey,Math.max(0,version),reason,operator);
        return result("EVICT_KEY",cacheKey.canonical(),1,event,"Durable 원장 기록과 현재 Instance 무효화를 완료했습니다.");
    }
    public AdmCacheControlResponse evictNamespace(String tenant,String namespace,long version,String operator,String reason){
        CpfCacheInvalidationCoordinator active=requireCoordinator(); CpfCacheInvalidationEvent event=active.requestNamespace(UUID.randomUUID().toString(),tenant,namespace,Math.max(0,version),reason,operator);
        return result("EVICT_NAMESPACE",event.tenantId()+":"+event.namespace(),1,event,"Namespace 무효화를 Durable 원장에 기록하고 현재 Instance에 적용했습니다.");
    }
    public AdmCacheControlResponse reconcile(String operator,String reason){CpfCacheInvalidationCoordinator active=requireCoordinator();long before=backlog();int applied=active.reconcileNow();long after=backlog();return new AdmCacheControlResponse("RECONCILE",active.consumerId(),true,applied,null,after,Instant.now(),before==0?"재조정 대상이 없습니다.":"Durable Event 재조정을 수행했습니다.");}

    private Object sample(CpfCommonResource resource,String query){try{var p=common.search(resource,query,0,1,true,null);return p.content().isEmpty()?Map.of():p.content().get(0);}catch(RuntimeException e){return Map.of("available",false,"failure",e.getClass().getSimpleName());}}
    private long backlog(){return invalidations==null||coordinator==null?0:invalidations.backlog(coordinator.consumerId());}
    private AdmCacheControlResponse result(String op,String target,long affected,CpfCacheInvalidationEvent event,String message){return new AdmCacheControlResponse(op,target,true,affected,event,backlog(),Instant.now(),message);}
    private CpfCacheInvalidationCoordinator requireCoordinator(){if(provider==null)throw new IllegalStateException("CPF Cache Provider가 구성되지 않았습니다.");if(invalidations==null||coordinator==null)throw new IllegalStateException("CPF Durable Cache Invalidation Coordinator가 구성되지 않았습니다.");return coordinator;}
    private AdmCacheSummaryResponse.DomainStatus domain(String name,Object status,Object sample){return new AdmCacheSummaryResponse.DomainStatus(name,String.valueOf(status),safe(sample));}
    private String safe(Object value){String text=String.valueOf(value).replaceAll("(?i)(password|token|secret)[=:][^, }]+","$1=[REDACTED]");return text.substring(0,Math.min(text.length(),300));}
}
