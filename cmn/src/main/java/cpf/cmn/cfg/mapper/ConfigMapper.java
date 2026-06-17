package cpf.cmn.cfg.mapper;

import cpf.cmn.cfg.dto.CommonConfigRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 怨듯넻 ?ㅼ젙媛믪쓣 議고쉶/?깅줉/?섏젙/??젣?섎뒗 MyBatis 留ㅽ띁?낅땲??
 */
@Mapper
public interface ConfigMapper {
    /**
     * ?꾩껜 ?ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @return ?ㅼ젙媛?⑸줉
     */
    List<Map<String, Object>> findAllConfigs();

    /**
     * ?ㅼ젙 ?ㅻ줈 ?ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @param configKey ?ㅼ젙 ??     * @return ?ㅼ젙媛??곗씠??     */
    Map<String, Object> findConfigByKey(@Param("configKey") String configKey);

    /**
     * ?ㅼ젙 ID濡??ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @param configId ?ㅼ젙 ID
     * @return ?ㅼ젙媛??곗씠??     */
    Map<String, Object> findConfigById(@Param("configId") Long configId);

    /**
     * ?ㅼ젙媛믪쓣 ?깅줉?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉 嫄댁닔
     */
    int insertConfig(CommonConfigRequest request);

    /**
     * ?ㅼ젙媛믪쓣 ?섏젙?⑸땲??
     *
     * @param configId ?섏젙???ㅼ젙 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙 嫄댁닔
     */
    int updateConfig(@Param("configId") Long configId, @Param("request") CommonConfigRequest request);

    /**
     * ?ㅼ젙媛믪쓣 ??젣?⑸땲??
     *
     * @param configId ??젣???ㅼ젙 ID
     * @return ??젣 嫄댁닔
     */
    int deleteConfig(@Param("configId") Long configId);
}

