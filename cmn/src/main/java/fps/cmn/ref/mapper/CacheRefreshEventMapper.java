package fps.cmn.ref.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * CMN 캐시 리프레시 이벤트를 저장하고 조회하는 MyBatis 매퍼입니다.
 *
 * <p>코드/메시지/설정값은 각 WAS의 로컬 Caffeine 캐시에 올라갑니다.
 * 한 WAS에서 CRUD가 발생하면 이 매퍼로 DB 이벤트를 남기고,
 * 다른 WAS는 새 이벤트를 조회해 자신의 로컬 캐시를 갱신합니다.</p>
 */
@Mapper
public interface CacheRefreshEventMapper {

    /**
     * 캐시 변경 이벤트를 등록합니다.
     *
     * @param cacheName 캐시 이름입니다. 예: codeCache, messageCache, configCache
     * @param eventType 이벤트 유형입니다. 예: CREATE, UPDATE, DELETE, MANUAL_REFRESH
     * @param eventKey 변경된 데이터의 업무 키입니다.
     * @param sourceWasId 이벤트를 발생시킨 WAS 식별자입니다.
     * @param publishedBy 이벤트를 발생시킨 사용자입니다.
     * @return 등록 건수입니다.
     */
    int insertEvent(
            @Param("cacheName") String cacheName,
            @Param("eventType") String eventType,
            @Param("eventKey") String eventKey,
            @Param("sourceWasId") String sourceWasId,
            @Param("publishedBy") String publishedBy);

    /**
     * 현재까지 등록된 마지막 이벤트 ID를 조회합니다.
     *
     * @return 마지막 이벤트 ID입니다. 이벤트가 없으면 null입니다.
     */
    Long findMaxEventId();

    /**
     * 마지막 처리 ID 이후의 캐시 변경 이벤트를 조회합니다.
     *
     * @param lastEventId 각 WAS가 마지막으로 처리한 이벤트 ID입니다.
     * @return 새 이벤트 목록입니다.
     */
    List<Map<String, Object>> findEventsAfter(@Param("lastEventId") long lastEventId);
}
