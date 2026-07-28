package com.cpf.common.ref.service;

import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 캐시 refresh event 저장 Transaction 경계를 분리합니다. */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CacheRefreshEventStore {
    private final CacheRefreshEventMapper mapper;
    public CacheRefreshEventStore(CacheRefreshEventMapper mapper){this.mapper=mapper;}

    /** 업무 데이터 변경과 같은 cmnTransaction에 반드시 참여하는 durable outbox 기록입니다. */
    @Transactional(transactionManager="cmnTransactionManager",propagation=Propagation.MANDATORY)
    public void insertRequired(String cacheName,String eventType,String eventKey,String wasId,String publishedBy){
        if(mapper.insertEvent(cacheName,eventType,eventKey,wasId,publishedBy)!=1)throw new IllegalStateException("Cache refresh event durable 기록에 실패했습니다.");
    }

    /** 업무 Transaction 종료 후 수동 refresh 등 독립 이벤트를 기록합니다. */
    @Transactional(transactionManager="cmnTransactionManager",propagation=Propagation.REQUIRES_NEW)
    public void insertOutOfBand(String cacheName,String eventType,String eventKey,String wasId,String publishedBy){
        if(mapper.insertEvent(cacheName,eventType,eventKey,wasId,publishedBy)!=1)throw new IllegalStateException("Cache refresh event 독립 기록에 실패했습니다.");
    }
}
