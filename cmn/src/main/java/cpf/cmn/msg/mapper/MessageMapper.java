package cpf.cmn.msg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import cpf.cmn.msg.dto.CommonMessageRequest;

/**
 * MessageMapper.java
 *
 * - 硫붿떆吏 ?곗씠?곕? 議고쉶?섎뒗 MyBatis 留ㅽ띁 ?명꽣?섏씠??
 * - 硫붿떆吏 ?뚯씠釉붿뿉??紐⑤뱺 硫붿떆吏 ?곗씠?곕? 議고쉶?섍굅???뱀젙 硫붿떆吏 ?ㅻ줈 議고쉶?섎뒗 硫붿꽌?쒕? ?쒓났?⑸땲??
 */
@Mapper
public interface MessageMapper {
    /**
     * 紐⑤뱺 硫붿떆吏 ?곗씠?곕? 議고쉶?⑸땲??
     *
     * @return 硫붿떆吏 ?곗씠??紐⑸줉 (Map ?뺥깭濡?諛섑솚)
     */
    List<Map<String, Object>> findAllMessages();

    /**
     * ?뱀젙 硫붿떆吏 ?ㅼ뿉 ?대떦?섎뒗 硫붿떆吏 ?곗씠?곕? 議고쉶?⑸땲??
     *
     * @param messageKey 硫붿떆吏 ??
     * @return 硫붿떆吏 ?곗씠??(Map ?뺥깭濡?諛섑솚)
     */
    Map<String, Object> findMessageByKey(@Param("messageKey") String messageKey);

    /**
     * 硫붿떆吏 ?ㅼ? ?몄뼱 肄붾뱶濡?硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @param messageKey 硫붿떆吏 ??     * @param locale ?몄뼱 肄붾뱶
     * @return 硫붿떆吏 ?곗씠??     */
    Map<String, Object> findMessageByKeyAndLocale(@Param("messageKey") String messageKey, @Param("locale") String locale);

    /**
     * 硫붿떆吏 ?? ?몄뼱 肄붾뱶, 硫붿떆吏 ?좏삎?쇰줈 硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @param messageKey  硫붿떆吏 ??     * @param locale      ?몄뼱 肄붾뱶
     * @param messageType 硫붿떆吏 ?좏삎. EXTERNAL ?먮뒗 INTERNAL
     * @return 硫붿떆吏 ?곗씠??     */
    Map<String, Object> findMessageByKeyLocaleType(
            @Param("messageKey") String messageKey,
            @Param("locale") String locale,
            @Param("messageType") String messageType);

    /**
     * 硫붿떆吏 ID濡?硫붿떆吏 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * @param messageId 硫붿떆吏 ID
     * @return 硫붿떆吏 ?곗씠??     */
    Map<String, Object> findMessageById(@Param("messageId") Long messageId);

    /**
     * 怨듯넻 硫붿떆吏瑜??깅줉?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉 嫄댁닔
     */
    int insertMessage(CommonMessageRequest request);

    /**
     * 怨듯넻 硫붿떆吏瑜??섏젙?⑸땲??
     *
     * @param messageId ?섏젙??硫붿떆吏 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙 嫄댁닔
     */
    int updateMessage(@Param("messageId") Long messageId, @Param("request") CommonMessageRequest request);

    /**
     * 怨듯넻 硫붿떆吏瑜???젣?⑸땲??
     *
     * @param messageId ??젣??硫붿떆吏 ID
     * @return ??젣 嫄댁닔
     */
    int deleteMessage(@Param("messageId") Long messageId);
}

