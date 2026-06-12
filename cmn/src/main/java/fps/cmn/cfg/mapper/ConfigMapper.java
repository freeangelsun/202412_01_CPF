package fps.cmn.cfg.mapper;

import fps.cmn.cfg.dto.CommonConfigRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 공통 설정값을 조회/등록/수정/삭제하는 MyBatis 매퍼입니다.
 */
@Mapper
public interface ConfigMapper {
    /**
     * 전체 설정값을 조회합니다.
     *
     * @return 설정값 목록
     */
    List<Map<String, Object>> findAllConfigs();

    /**
     * 설정 키로 설정값을 조회합니다.
     *
     * @param configKey 설정 키
     * @return 설정값 데이터
     */
    Map<String, Object> findConfigByKey(@Param("configKey") String configKey);

    /**
     * 설정 ID로 설정값을 조회합니다.
     *
     * @param configId 설정 ID
     * @return 설정값 데이터
     */
    Map<String, Object> findConfigById(@Param("configId") Long configId);

    /**
     * 설정값을 등록합니다.
     *
     * @param request 등록 요청
     * @return 등록 건수
     */
    int insertConfig(CommonConfigRequest request);

    /**
     * 설정값을 수정합니다.
     *
     * @param configId 수정할 설정 ID
     * @param request 수정 요청
     * @return 수정 건수
     */
    int updateConfig(@Param("configId") Long configId, @Param("request") CommonConfigRequest request);

    /**
     * 설정값을 삭제합니다.
     *
     * @param configId 삭제할 설정 ID
     * @return 삭제 건수
     */
    int deleteConfig(@Param("configId") Long configId);
}
