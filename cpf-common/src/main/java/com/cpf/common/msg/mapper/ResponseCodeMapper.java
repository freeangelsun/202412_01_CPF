package com.cpf.common.msg.mapper;

import com.cpf.common.msg.dto.CommonResponseCodeRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResponseCodeMapper {
    List<Map<String, Object>> findAllResponseCodes();

    Map<String, Object> findResponseCode(@Param("responseCode") String responseCode);

    int insertResponseCode(CommonResponseCodeRequest request);

    int updateResponseCode(@Param("responseCode") String responseCode, @Param("request") CommonResponseCodeRequest request);

    int deleteResponseCode(@Param("responseCode") String responseCode);
}

