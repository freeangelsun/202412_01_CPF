package com.cpf.core.api.logging;

import java.util.List;
import java.util.Map;

/**
 * 운영 모듈이 CPF 거래 구간 테이블을 직접 조회하지 않도록 제공하는 공개 query port입니다.
 */
public interface CpfTransactionTimelineQueryPort {

    GroupQueryResult findGroups(Map<String, String> criteria);

    List<Map<String, Object>> findSegments(String transactionId);

    List<Map<String, Object>> findExternalCandidates(String transactionId, int limit);

    /**
     * Cross-source lineage for one-shot ADM transaction lookup. Implementations must query
     * framework-owned lineage state only; caller-provided correlation headers are not authoritative.
     */
    default List<Map<String, Object>> findLineage(String transactionId, int limit) { return List.of(); }

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
