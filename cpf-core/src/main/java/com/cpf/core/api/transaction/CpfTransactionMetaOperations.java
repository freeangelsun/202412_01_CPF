package com.cpf.core.api.transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADM과 운영 도구가 Core의 JDBC 저장소·MVC scanner 구현에 직접 결합하지 않고
 * 온라인 거래 메타를 조회·재스캔하는 공개 계약입니다.
 */
public interface CpfTransactionMetaOperations {
    boolean tableAvailable();

    List<Map<String, Object>> findAll(
            String moduleCode,
            String activeYn,
            String transactionId,
            int limit);

    Optional<Map<String, Object>> findById(String transactionId);

    CpfTransactionMetaScanResult scanAndUpsert(String requestUser);

    Map<String, Object> inactivate(String transactionId, String requestUser);
}
