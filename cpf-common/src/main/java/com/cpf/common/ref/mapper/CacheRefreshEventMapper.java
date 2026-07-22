package com.cpf.common.ref.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 캐시 갱신 DB fallback 이벤트를 기록하고 순차 조회하는 매퍼입니다. */
@Mapper
public interface CacheRefreshEventMapper {

    /** 캐시 변경 이벤트를 발행 원본 WAS와 함께 기록합니다. */
    int insertEvent(
            @Param("cacheName") String cacheName,
            @Param("eventType") String eventType,
            @Param("eventKey") String eventKey,
            @Param("sourceWasId") String sourceWasId,
            @Param("publishedBy") String publishedBy);

    /** 현재 저장된 가장 큰 이벤트 ID를 조회합니다. */
    Long findMaxEventId();

    /** 마지막 처리 ID 이후 이벤트를 오름차순으로 조회합니다. */
    List<Map<String, Object>> findEventsAfter(@Param("lastEventId") long lastEventId);
}

