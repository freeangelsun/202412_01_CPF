package com.cpf.common.ref.service;

import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;import java.util.LinkedHashMap;import java.util.List;import java.util.Map;

/** DB durable cache event를 Runtime consumer checkpoint 기준으로 재생합니다. */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CacheRefreshEventListener {
    private static final Logger log=LoggerFactory.getLogger(CacheRefreshEventListener.class);
    private final CacheRefreshEventMapper mapper;private final CodeCacheService code;private final MessageCacheService message;private final ResponseCodeCacheService response;private final ConfigCacheService config;
    private long lastEventId;private volatile String lastFailureType;private volatile Instant lastSuccessfulPollAt;
    @Value("${cpf.cmn.cache.event-poll-enabled:true}")private boolean enabled;
    @Value("${cpf.framework.was-id:local}")private String wasId;
    public CacheRefreshEventListener(CacheRefreshEventMapper mapper,CodeCacheService code,MessageCacheService message,ResponseCodeCacheService response,ConfigCacheService config){this.mapper=mapper;this.code=code;this.message=message;this.response=response;this.config=config;}

    @PostConstruct public void initialize(){
        if(!enabled)return;
        String consumer=consumerId();
        try{
            Long checkpoint=mapper.findCheckpoint(consumer);
            if(checkpoint==null){
                // 신규 인스턴스는 정본 DB로 전체 cache를 먼저 구축한 뒤 현재 event tail을 checkpoint로 잡습니다.
                refreshAll();Long max=mapper.findMaxEventId();lastEventId=max==null?0L:max;
                try{mapper.insertCheckpoint(consumer,lastEventId);}catch(RuntimeException race){Long current=mapper.findCheckpoint(consumer);if(current==null)throw race;lastEventId=current;}
            }else lastEventId=checkpoint;
            lastFailureType=null;lastSuccessfulPollAt=Instant.now();
        }catch(RuntimeException ex){lastFailureType=ex.getClass().getSimpleName();throw new IllegalStateException("Cache refresh durable checkpoint 초기화 실패",ex);}
    }

    @Scheduled(fixedDelayString="${cpf.cmn.cache.refresh-poll-millis:5000}",initialDelayString="${cpf.cmn.cache.refresh-initial-delay-millis:5000}")
    public void pollRefreshEvents(){if(!enabled)return;try{
        List<Map<String,Object>> events=mapper.findEventsAfter(lastEventId);
        for(Map<String,Object> event:events){long id=asLong(event.get("eventId"));refreshCache(asString(event.get("cacheName")));
            if(mapper.updateCheckpoint(consumerId(),id)!=1)throw new IllegalStateException("Cache refresh checkpoint update 실패");lastEventId=id;}
        lastFailureType=null;lastSuccessfulPollAt=Instant.now();
    }catch(RuntimeException ex){lastFailureType=ex.getClass().getSimpleName();log.warn("CMN durable cache event polling failed. consumerId={}, lastEventId={}",consumerId(),lastEventId,ex);}}

    public Map<String,Object> status(){Map<String,Object>s=new LinkedHashMap<>();s.put("enabled",enabled);s.put("consumerId",consumerId());s.put("lastEventId",lastEventId);s.put("lastSuccessfulPollAt",lastSuccessfulPollAt==null?null:lastSuccessfulPollAt.toString());s.put("lastFailureType",lastFailureType);s.put("durableCheckpoint",true);return s;}
    private void refreshAll(){code.refreshCodes();message.refreshMessages();response.refreshResponseCodes();config.refreshConfigs();}
    private void refreshCache(String name){switch(name){case"codeCache"->code.refreshCodes();case"messageCache"->message.refreshMessages();case"responseCodeCache"->response.refreshResponseCodes();case"configCache"->config.refreshConfigs();default->throw new IllegalArgumentException("Unknown CMN cache refresh event: "+name);}}
    private String consumerId(){return "CMN_CACHE:"+(wasId==null||wasId.isBlank()?"local":wasId);}
    private long asLong(Object v){return v instanceof Number n?n.longValue():Long.parseLong(String.valueOf(v));}private String asString(Object v){return v==null?"":String.valueOf(v);}
}
