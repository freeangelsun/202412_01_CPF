package com.cpf.common.code.reference.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 다중 WAS cache 동기화용 durable DB event publisher입니다.
 *
 * <p>업무 mutation은 {@link #publishRequired}를 같은 cmnTransaction 안에서 호출합니다. 따라서 event 저장 실패를
 * 메모리 queue로 숨기지 않고 업무 변경도 rollback시켜 "DB는 바뀌었지만 다른 노드는 영원히 모르는" 상태를 금지합니다.</p>
 */
@Deprecated(forRemoval = false)
public class CacheRefreshEventPublisher {
    private final CacheRefreshEventStore store;
    private final Clock clock;
    private final AtomicLong publishedCount=new AtomicLong();
    private final AtomicLong failedCount=new AtomicLong();
    private volatile String lastFailureType; private volatile Instant lastPublishedAt;
    @Value("${cpf.framework.was-id:local}") private String wasId;

    @Deprecated
    public CacheRefreshEventPublisher(CacheRefreshEventStore store){this(store,Clock.systemUTC());}

    @org.springframework.beans.factory.annotation.Autowired
    @Deprecated
    public CacheRefreshEventPublisher(CacheRefreshEventStore store,Clock clock){this.store=store;this.clock=java.util.Objects.requireNonNull(clock,"clock");}

    /** 현재 업무 Transaction과 원자적으로 durable event를 기록합니다. */
    @Deprecated
    public void publishRequired(String cacheName,String eventType,String eventKey,String requestUser){
        try{
            if(TransactionSynchronizationManager.isActualTransactionActive()) store.insertRequired(cacheName,eventType,eventKey,wasId,normalizeUser(requestUser));
            else store.insertOutOfBand(cacheName,eventType,eventKey,wasId,normalizeUser(requestUser));
            success();
        }
        catch(RuntimeException ex){failedCount.incrementAndGet();lastFailureType=ex.getClass().getSimpleName();throw ex;}
    }

    /** 독립 운영 명령에서 즉시 durable event를 기록합니다. */
    @Deprecated
    public void publish(String cacheName,String eventType,String eventKey,String requestUser){
        try{store.insertOutOfBand(cacheName,eventType,eventKey,wasId,normalizeUser(requestUser));success();}
        catch(RuntimeException ex){failedCount.incrementAndGet();lastFailureType=ex.getClass().getSimpleName();throw ex;}
    }

    /** commit 이후 발행이 필요한 비업무 이벤트 전용입니다. 실패는 호출 측에서 관측되며 memory queue로 숨기지 않습니다. */
    @Deprecated
    public void publishAfterCommit(String cacheName,String eventType,String eventKey,String requestUser){
        if(TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){publish(cacheName,eventType,eventKey,requestUser);}});return;
        }
        publish(cacheName,eventType,eventKey,requestUser);
    }

    @Deprecated
    public Map<String,Object> status(){Map<String,Object>s=new LinkedHashMap<>();s.put("durable",true);s.put("memoryRetryQueue",false);s.put("publishedCount",publishedCount.get());s.put("failedCount",failedCount.get());s.put("lastPublishedAt",lastPublishedAt==null?null:lastPublishedAt.toString());s.put("lastFailureType",lastFailureType);return s;}
    private void success(){publishedCount.incrementAndGet();lastPublishedAt=clock.instant();lastFailureType=null;}
    private String normalizeUser(String user){return user==null||user.isBlank()?"SYSTEM":user;}
}
