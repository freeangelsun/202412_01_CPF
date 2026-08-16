package com.cpf.batch.api;

import java.util.List;
import java.util.Map;

/**
 * Center-Cut target/result가 BAT 표준 저장소가 아닌 업무 Domain 저장소에 있을 때 사용하는 조회 SPI.
 * 구현은 자신의 Owner DB만 접근해야 합니다.
 */
public interface CpfCenterCutOperationsExtension {
    boolean supports(String centerCutJobId);
    Map<String,Object> findSummary(String centerCutJobId);
    List<Map<String,Object>> findTargets(String centerCutJobId, String statusCode, int limit);
    List<Map<String,Object>> findResults(String centerCutJobId, String resultStatus, int limit);
    Map<String,Object> findResultDetail(String resultId);
}
