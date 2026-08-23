package com.cpf.admin.opr.service;

import com.cpf.data.api.CpfDataRow;
import com.cpf.admin.opr.transaction.repository.AdmTransactionMetaRepository;
import com.cpf.foundation.annotation.CpfService;
import java.util.List;
import java.util.Optional;

/**
 * ADM 온라인 거래 메타 운영 서비스입니다.
 *
 * <p>Runtime Bootstrap이 자동 등록한 Operation Catalog와 ADM이 소유하는 Operation Policy를
 * 같은 DB 정본에서 조회·변경합니다. Source scan은 Runtime 책임이며 ADM이 수동 재스캔하지 않습니다.</p>
 */
@CpfService
public class AdmTransactionMetaService extends com.cpf.admin.common.base.AdmBaseService {
    private final AdmTransactionMetaRepository repository;

    public AdmTransactionMetaService(AdmTransactionMetaRepository repository) {
        this.repository = repository;
    }

    /** ADM 거래 메타 Server Paging 응답입니다. */
    public record TransactionMetaPage(
            boolean available, List<CpfDataRow> items, int page, int size, long totalElements, int totalPages) {
        public TransactionMetaPage {
            items = items == null ? List.of() : List.copyOf(items);
            page = Math.max(0, page); size = Math.max(1, size); totalElements = Math.max(0L, totalElements);
            totalPages = Math.max(0, totalPages);
        }
    }
    public CpfDataRow findTransactions(String moduleCode, String activeYn, String operationId, int limit) {
        return CpfDataRow.of(
                "available", true,
                "items", findAll(moduleCode, activeYn, operationId, limit));
    }

    public List<CpfDataRow> findAll(String moduleCode, String activeYn, String operationId, int limit) {
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(operationId);
        int bounded = Math.max(1, Math.min(limit, 1_000));
        return repository.findAll(module, active, id, bounded);
    }

    public TransactionMetaPage findPage(String moduleCode, String activeYn, String operationId, int page, int size) {
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(operationId);
        int safePage = Math.max(0, page); int safeSize = Math.max(1, Math.min(size, 200)); int offset = safePage * safeSize;
        long count = repository.count(module, active, id);
        List<CpfDataRow> items = repository.findPage(module, active, id, offset, safeSize);
        return new TransactionMetaPage(true, items, safePage, safeSize, count, (int) ((count + safeSize - 1) / safeSize));
    }

    public CpfDataRow findTransaction(String operationId) {
        return CpfDataRow.of(
                "available", true,
                "item", findById(operationId).orElse(CpfDataRow.of()));
    }

    public Optional<CpfDataRow> findById(String operationId) {
        return repository.findById(operationId);
    }

    /** ADM이 소유한 Operation enabled 정책을 optimistic version으로 변경합니다. Catalog metadata는 변경하지 않습니다. */
    public CpfDataRow inactivate(String operationId, long expectedPolicyVersion, String operatorId, String reason) {
        CpfDataRow before = findById(operationId).orElse(CpfDataRow.of());
        int updated = repository.inactivate(operationId, expectedPolicyVersion, operatorId, reason);
        if (updated != 1) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Operation policy version conflict or operation not found: " + operationId);
        }
        CpfDataRow after = findById(operationId).orElse(CpfDataRow.of());
        return CpfDataRow.of("updated", updated, "before", before, "after", after);
    }

    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private static String like(String v) { String n=blankToNull(v); return n == null ? null : "%"+n+"%"; }
}
