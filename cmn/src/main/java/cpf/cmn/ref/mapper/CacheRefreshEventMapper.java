package cpf.cmn.ref.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Mapper
public interface CacheRefreshEventMapper {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    int insertEvent(
            @Param("cacheName") String cacheName,
            @Param("eventType") String eventType,
            @Param("eventKey") String eventKey,
            @Param("sourceWasId") String sourceWasId,
            @Param("publishedBy") String publishedBy);

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    Long findMaxEventId();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    List<Map<String, Object>> findEventsAfter(@Param("lastEventId") long lastEventId);
}

