package com.cpf.common.cde.mapper;

import com.cpf.common.cde.dto.CommonCodeRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * CPF 공통 코드 Mapper입니다.
 */
@Mapper
public interface CodeMapper {
    List<Map<String, Object>> findAllCodes();

    Map<String, Object> findCodeByKey(@Param("codeKey") String codeKey);

    List<Map<String, Object>> findCodesByKey(@Param("codeKey") String codeKey);

    Map<String, Object> findCodeById(@Param("codeId") Long codeId);

    int insertCode(CommonCodeRequest request);

    int updateCode(@Param("codeId") Long codeId, @Param("request") CommonCodeRequest request);

    int deleteCode(@Param("codeId") Long codeId);
}
