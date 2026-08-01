package com.cpf.core.api.transaction;

import java.util.List;
import com.cpf.core.api.data.CpfDataRow;
import java.util.Optional;

/**
 * ADM과 운영 도구가 Core의 JDBC 저장소·MVC scanner 구현에 직접 결합하지 않고
 * 온라인 거래 메타를 조회·재스캔하는 공개 계약입니다.
 */
public interface CpfTransactionMetaOperations {
    /** 거래 메타의 Owner-side server page 계약입니다. */
    record TransactionMetaPage(
            boolean available,
            List<CpfDataRow> items,
            int page,
            int size,
            long totalElements,
            int totalPages) {
        public TransactionMetaPage {
            items = items == null ? List.of() : List.copyOf(items);
            page = Math.max(0, page);
            size = Math.max(1, size);
            totalElements = Math.max(0L, totalElements);
            totalPages = Math.max(0, totalPages);
        }
    }

    boolean tableAvailable();

    List<CpfDataRow> findAll(
            String moduleCode,
            String activeYn,
            String transactionId,
            int limit);

    Optional<CpfDataRow> findById(String transactionId);

    TransactionMetaPage findPage(
            String moduleCode,
            String activeYn,
            String transactionId,
            int page,
            int size);

    CpfTransactionMetaScanResult scanAndUpsert(String requestUser);

    CpfDataRow inactivate(String transactionId, String requestUser);
}
