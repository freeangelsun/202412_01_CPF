package cpf.cmn.cde.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import cpf.cmn.cde.dto.CommonCodeRequest;

/**
 * CodeMapper.java
 *
 * - 肄붾뱶 ?곗씠?곕? 議고쉶?섎뒗 MyBatis 留ㅽ띁 ?명꽣?섏씠??
 * - 肄붾뱶 ?뚯씠釉붿뿉??⑤뱺 肄붾뱶 ?곗씠?곕? 議고쉶?섍굅???뱀젙 肄붾뱶 ?ㅻ줈 議고쉶?섎뒗 硫붿꽌?쒕? ?쒓났?⑸땲??
 */
@Mapper
public interface CodeMapper {
    /**
     * ⑤뱺 肄붾뱶 ?곗씠?곕? 議고쉶?⑸땲??
     *
     * @return 肄붾뱶 ?곗씠??⑸줉 (Map ?뺥깭濡?諛섑솚)
     */
    List<Map<String, Object>> findAllCodes();

    /**
     * ?뱀젙 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 肄붾뱶 ?곗씠?곕? 議고쉶?⑸땲??
     *
     * @param codeKey 肄붾뱶 ??
     * @return 肄붾뱶 ?곗씠??(Map ?뺥깭濡?諛섑솚)
     */
    Map<String, Object> findCodeByKey(@Param("codeKey") String codeKey);

    /**
     * ?뱀젙 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 肄붾뱶 ⑸줉??議고쉶?⑸땲??
     *
     * @param codeKey 肄붾뱶 ??     * @return 媛숈? 肄붾뱶 ?ㅻ? 媛吏?肄붾뱶 ⑸줉
     */
    List<Map<String, Object>> findCodesByKey(@Param("codeKey") String codeKey);

    /**
     * 肄붾뱶 ID濡?肄붾뱶 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * @param codeId 肄붾뱶 ID
     * @return 肄붾뱶 ?곗씠??     */
    Map<String, Object> findCodeById(@Param("codeId") Long codeId);

    /**
     * 怨듯넻 肄붾뱶瑜??깅줉?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉 嫄댁닔
     */
    int insertCode(CommonCodeRequest request);

    /**
     * 怨듯넻 肄붾뱶瑜??섏젙?⑸땲??
     *
     * @param codeId ?섏젙??肄붾뱶 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙 嫄댁닔
     */
    int updateCode(@Param("codeId") Long codeId, @Param("request") CommonCodeRequest request);

    /**
     * 怨듯넻 肄붾뱶瑜???젣?⑸땲??
     *
     * @param codeId ??젣??肄붾뱶 ID
     * @return ??젣 嫄댁닔
     */
    int deleteCode(@Param("codeId") Long codeId);
}

