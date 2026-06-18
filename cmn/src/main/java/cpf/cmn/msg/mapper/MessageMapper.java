package cpf.cmn.msg.mapper;

import cpf.cmn.msg.dto.CommonMessageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * PFW 공통 메시지 Mapper입니다.
 */
@Mapper
public interface MessageMapper {
    List<Map<String, Object>> findAllMessages();

    Map<String, Object> findMessageByKey(@Param("messageKey") String messageKey);

    Map<String, Object> findMessageByKeyAndLocale(@Param("messageKey") String messageKey, @Param("locale") String locale);

    Map<String, Object> findMessageByKeyLocaleType(
            @Param("messageKey") String messageKey,
            @Param("locale") String locale,
            @Param("messageType") String messageType);

    Map<String, Object> findMessageById(@Param("messageId") Long messageId);

    int insertMessage(CommonMessageRequest request);

    int updateMessage(@Param("messageId") Long messageId, @Param("request") CommonMessageRequest request);

    int deleteMessage(@Param("messageId") Long messageId);
}
