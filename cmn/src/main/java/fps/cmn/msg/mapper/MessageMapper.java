package fps.cmn.msg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import fps.cmn.msg.dto.CommonMessageRequest;

/**
 * MessageMapper.java
 *
 * - 메시지 데이터를 조회하는 MyBatis 매퍼 인터페이스
 * - 메시지 테이블에서 모든 메시지 데이터를 조회하거나 특정 메시지 키로 조회하는 메서드를 제공합니다.
 */
@Mapper
public interface MessageMapper {
    /**
     * 모든 메시지 데이터를 조회합니다.
     *
     * @return 메시지 데이터 목록 (Map 형태로 반환)
     */
    List<Map<String, Object>> findAllMessages();

    /**
     * 특정 메시지 키에 해당하는 메시지 데이터를 조회합니다.
     *
     * @param messageKey 메시지 키
     * @return 메시지 데이터 (Map 형태로 반환)
     */
    Map<String, Object> findMessageByKey(@Param("messageKey") String messageKey);

    /**
     * 메시지 키와 언어 코드로 메시지를 조회합니다.
     *
     * @param messageKey 메시지 키
     * @param locale 언어 코드
     * @return 메시지 데이터
     */
    Map<String, Object> findMessageByKeyAndLocale(@Param("messageKey") String messageKey, @Param("locale") String locale);

    /**
     * 메시지 키, 언어 코드, 메시지 유형으로 메시지를 조회합니다.
     *
     * @param messageKey  메시지 키
     * @param locale      언어 코드
     * @param messageType 메시지 유형. EXTERNAL 또는 INTERNAL
     * @return 메시지 데이터
     */
    Map<String, Object> findMessageByKeyLocaleType(
            @Param("messageKey") String messageKey,
            @Param("locale") String locale,
            @Param("messageType") String messageType);

    /**
     * 메시지 ID로 메시지 한 건을 조회합니다.
     *
     * @param messageId 메시지 ID
     * @return 메시지 데이터
     */
    Map<String, Object> findMessageById(@Param("messageId") Long messageId);

    /**
     * 공통 메시지를 등록합니다.
     *
     * @param request 등록 요청
     * @return 등록 건수
     */
    int insertMessage(CommonMessageRequest request);

    /**
     * 공통 메시지를 수정합니다.
     *
     * @param messageId 수정할 메시지 ID
     * @param request 수정 요청
     * @return 수정 건수
     */
    int updateMessage(@Param("messageId") Long messageId, @Param("request") CommonMessageRequest request);

    /**
     * 공통 메시지를 삭제합니다.
     *
     * @param messageId 삭제할 메시지 ID
     * @return 삭제 건수
     */
    int deleteMessage(@Param("messageId") Long messageId);
}
