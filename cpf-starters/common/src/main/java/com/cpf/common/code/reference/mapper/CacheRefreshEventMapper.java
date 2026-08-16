package com.cpf.common.code.reference.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;import java.util.Map;

/** 캐시 durable outbox와 Runtime consumer checkpoint 매퍼입니다. */
@Mapper
public interface CacheRefreshEventMapper {
    int insertEvent(@Param("cacheName")String cacheName,@Param("eventType")String eventType,@Param("eventKey")String eventKey,@Param("sourceWasId")String sourceWasId,@Param("publishedBy")String publishedBy);
    Long findMaxEventId();
    List<Map<String,Object>> findEventsAfter(@Param("lastEventId")long lastEventId);
    Long findCheckpoint(@Param("consumerId")String consumerId);
    int insertCheckpoint(@Param("consumerId")String consumerId,@Param("lastEventId")long lastEventId);
    int updateCheckpoint(@Param("consumerId")String consumerId,@Param("lastEventId")long lastEventId);
}
