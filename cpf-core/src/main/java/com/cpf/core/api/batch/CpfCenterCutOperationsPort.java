package com.cpf.core.api.batch;

import java.util.List;
import java.util.Map;

/** ADM과 같은 Control Plane이 BAT Owner를 통해 조회하는 Center-Cut 운영 계약. */
public interface CpfCenterCutOperationsPort {
    List<Map<String,Object>> findJobs();
    Map<String,Object> findJobDetail(String centerCutJobId);
    List<Map<String,Object>> findParameters(String centerCutJobId);
    Map<String,Object> findSummary(String centerCutJobId);
    List<Map<String,Object>> findTargets(String centerCutJobId, String statusCode, int limit);
    List<Map<String,Object>> findResults(String centerCutJobId, String resultStatus, int limit);
    Map<String,Object> findResultDetail(String resultId);
}
