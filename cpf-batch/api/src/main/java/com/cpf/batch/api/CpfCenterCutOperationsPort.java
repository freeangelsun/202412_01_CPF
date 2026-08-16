package com.cpf.batch.api;

import java.util.List;
import java.util.Map;

/** ADM과 같은 Control Plane이 BAT Owner를 통해 조회하는 Center-Cut 운영 계약. */
public interface CpfCenterCutOperationsPort {
    /**
     * findJobs 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String,Object>> findJobs();
    /**
     * findJobDetail 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param centerCutJobId Center-Cut Job 식별자입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    Map<String,Object> findJobDetail(String centerCutJobId);
    /**
     * findParameters 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param centerCutJobId Center-Cut Job 식별자입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String,Object>> findParameters(String centerCutJobId);
    /**
     * findSummary 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param centerCutJobId Center-Cut Job 식별자입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    Map<String,Object> findSummary(String centerCutJobId);
    /**
     * findTargets 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param centerCutJobId Center-Cut Job 식별자입니다.
     * @param statusCode Center-Cut 대상 상태 필터입니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String,Object>> findTargets(String centerCutJobId, String statusCode, int limit);
    /**
     * findResults 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param centerCutJobId Center-Cut Job 식별자입니다.
     * @param resultStatus Center-Cut 결과 상태 필터입니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String,Object>> findResults(String centerCutJobId, String resultStatus, int limit);
    /**
     * findResultDetail 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param resultId Center-Cut 결과 식별자입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    Map<String,Object> findResultDetail(String resultId);
}
