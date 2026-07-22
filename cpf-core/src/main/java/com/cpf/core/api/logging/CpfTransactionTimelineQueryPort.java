package com.cpf.core.api.logging;

import java.util.List;
import java.util.Map;

/**
 * 운영 모듈이 CPF 거래 구간 테이블을 직접 조회하지 않도록 제공하는 공개 query port입니다.
 */
public interface CpfTransactionTimelineQueryPort {

    GroupQueryResult findGroups(Map<String, String> criteria);

    List<Map<String, Object>> findSegments(String transactionGlobalId);

    List<Map<String, Object>> findExternalCandidates(String transactionGlobalId, int limit);

    record GroupQueryResult(
            boolean available,
            List<Map<String, Object>> items,
            int limit,
            String sort,
            String message) {
    }
}
