package com.cpf.platform.operations.observability.api.logging;

import java.util.List;
import java.util.Map;

/**
 * 운영 모듈이 CPF 거래 구간 테이블을 직접 조회하지 않도록 제공하는 공개 query port입니다.
 */
public interface CpfTransactionTimelineQueryPort {

    /**
     * findGroups 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param criteria 조회 조건입니다. null이면 조건 없는 조회로 해석합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    GroupQueryResult findGroups(Map<String, String> criteria);

    /**
     * findSegments 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String, Object>> findSegments(String transactionId);

    /**
     * findExternalCandidates 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<Map<String, Object>> findExternalCandidates(String transactionId, int limit);

        /**
     * findLineage 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default List<Map<String, Object>> findLineage(String transactionId, int limit) { return List.of(); }

    /**
     * sourceFreshness 공개 계약을 수행합니다. null/default, 상태 변경과 오류 의미는 이 계약을 따릅니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @return 계약에 정의된 결과입니다.
     */
    default Map<String, Object> sourceFreshness(String transactionId) { return Map.of(
            "transactionId", transactionId, "partial", true, "missingSources", List.of("LINEAGE")); }

    record GroupQueryResult(
            boolean available,
            List<Map<String, Object>> items,
            int limit,
            String sort,
            String message) {
    }
}
