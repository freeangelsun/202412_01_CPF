package com.cpf.common.message.mapper;

import com.cpf.common.message.dto.CommonResponseCodeRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 고객 업무 공통 응답코드 Catalog의 MyBatis persistence 계약입니다.
 * <p>응답코드 조회/변경은 이 Mapper를 소유한 Common capability를 통해 수행하며 다른 Domain 원장을 직접 다루지 않습니다.
 */
@Mapper
public interface ResponseCodeMapper {
    List<Map<String, Object>> findAllResponseCodes();

    Map<String, Object> findResponseCode(@Param("responseCode") String responseCode);

    int insertResponseCode(CommonResponseCodeRequest request);

    int updateResponseCode(@Param("responseCode") String responseCode, @Param("request") CommonResponseCodeRequest request);

    int deleteResponseCode(@Param("responseCode") String responseCode);
}

