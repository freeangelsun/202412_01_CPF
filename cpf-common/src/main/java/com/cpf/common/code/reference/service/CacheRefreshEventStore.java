package com.cpf.common.code.reference.service;

import com.cpf.common.code.reference.mapper.CacheRefreshEventMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 캐시 refresh event 저장 Transaction 경계를 분리합니다. */
@Deprecated(forRemoval = false)
public class CacheRefreshEventStore {
    private final CacheRefreshEventMapper mapper;
    @Deprecated
    public CacheRefreshEventStore(CacheRefreshEventMapper mapper){this.mapper=mapper;}

    /** 업무 데이터 변경과 같은 cmnTransaction에 반드시 참여하는 durable outbox 기록입니다. */
    @Transactional(transactionManager="cpfCommonTransactionManager",propagation=Propagation.MANDATORY)
    @Deprecated
    public void insertRequired(String cacheName,String eventType,String eventKey,String wasId,String publishedBy){
        if(mapper.insertEvent(cacheName,eventType,eventKey,wasId,publishedBy)!=1)throw new IllegalStateException("Cache refresh event durable 기록에 실패했습니다.");
    }

    /** 업무 Transaction 종료 후 수동 refresh 등 독립 이벤트를 기록합니다. */
    @Transactional(transactionManager="cpfCommonTransactionManager",propagation=Propagation.REQUIRES_NEW)
    @Deprecated
    public void insertOutOfBand(String cacheName,String eventType,String eventKey,String wasId,String publishedBy){
        if(mapper.insertEvent(cacheName,eventType,eventKey,wasId,publishedBy)!=1)throw new IllegalStateException("Cache refresh event 독립 기록에 실패했습니다.");
    }
}
