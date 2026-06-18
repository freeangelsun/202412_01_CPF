package cpf.cmn.cfg.mapper;

import cpf.cmn.cfg.dto.CommonConfigRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * PFW 공통 설정 Mapper입니다.
 */
@Mapper
public interface ConfigMapper {
    List<Map<String, Object>> findAllConfigs();

    Map<String, Object> findConfigByKey(@Param("configKey") String configKey);

    Map<String, Object> findConfigById(@Param("configId") Long configId);

    int insertConfig(CommonConfigRequest request);

    int updateConfig(@Param("configId") Long configId, @Param("request") CommonConfigRequest request);

    int deleteConfig(@Param("configId") Long configId);
}
