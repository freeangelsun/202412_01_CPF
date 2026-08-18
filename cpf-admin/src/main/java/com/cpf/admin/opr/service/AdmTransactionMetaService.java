package com.cpf.admin.opr.service;

import com.cpf.data.api.CpfDataRow;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.annotation.CpfService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ADM 온라인 거래 메타 운영 서비스입니다.
 *
 * <p>Runtime Bootstrap이 자동 등록한 Operation Catalog와 ADM이 소유하는 Operation Policy를
 * 같은 DB 정본에서 조회·변경합니다. Source scan은 Runtime 책임이며 ADM이 수동 재스캔하지 않습니다.</p>
 */
@CpfService
public class AdmTransactionMetaService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public AdmTransactionMetaService(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider catalogs) {
        this.jdbc = jdbc;
        this.sql = catalogs.forModule("cpf");
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


    public boolean tableAvailable() {
        Integer count = jdbc.queryForObject(sql.required("transaction-meta-table-available.sql"), Integer.class);
        return count != null && count > 0;
    }

    public CpfDataRow findTransactions(String moduleCode, String activeYn, String operationId, int limit) {
        CpfDataRow response = new CpfDataRow();
        boolean available = tableAvailable();
        response.put("available", available);
        response.put("items", available ? findAll(moduleCode, activeYn, operationId, limit) : List.of());
        return response;
    }

    public List<CpfDataRow> findAll(String moduleCode, String activeYn, String operationId, int limit) {
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(operationId);
        int bounded = Math.max(1, Math.min(limit, 1_000));
        return rows(jdbc.queryForList(sql.required("transaction-meta-find-all.sql"), module, module, active, active, id, id, bounded));
    }

    public TransactionMetaPage findPage(String moduleCode, String activeYn, String operationId, int page, int size) {
        if (!tableAvailable()) return new TransactionMetaPage(false, List.of(), page, size, 0, 0);
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(operationId);
        int safePage = Math.max(0, page); int safeSize = Math.max(1, Math.min(size, 200)); int offset = safePage * safeSize;
        Long total = jdbc.queryForObject(sql.required("transaction-meta-count.sql"), Long.class,
                module, module, active, active, id, id);
        Object[] pageArgs = new Object[] {module, module, active, active, id, id, offset, safeSize};
        List<CpfDataRow> items = rows(jdbc.queryForList(sql.required("transaction-meta-find-page.sql"), pageArgs));
        long count = total == null ? 0L : total;
        return new TransactionMetaPage(true, items, safePage, safeSize, count, (int) ((count + safeSize - 1) / safeSize));
    }

    public CpfDataRow findTransaction(String operationId) {
        CpfDataRow response = new CpfDataRow(); boolean available = tableAvailable(); response.put("available", available);
        response.put("item", available ? findById(operationId).orElse(CpfDataRow.of()) : CpfDataRow.of()); return response;
    }

    public Optional<CpfDataRow> findById(String operationId) {
        List<CpfDataRow> rows = rows(jdbc.queryForList(sql.required("transaction-meta-find-by-id.sql"), operationId));
        return rows.stream().findFirst();
    }

    /** ADM이 소유한 Operation enabled 정책을 optimistic version으로 변경합니다. Catalog metadata는 변경하지 않습니다. */
    public CpfDataRow inactivate(String operationId, long expectedPolicyVersion, String operatorId, String reason) {
        CpfDataRow before = findById(operationId).orElse(CpfDataRow.of());
        int updated = jdbc.update(sql.required("transaction-meta-inactivate.sql"),
                reason, operatorId, operationId, expectedPolicyVersion);
        if (updated != 1) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Operation policy version conflict or operation not found: " + operationId);
        }
        CpfDataRow after = findById(operationId).orElse(CpfDataRow.of());
        return CpfDataRow.of("updated", updated, "before", before, "after", after);
    }

    private static List<CpfDataRow> rows(List<Map<String,Object>> source) { return source.stream().map(CpfDataRow::new).toList(); }
    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private static String like(String v) { String n=blankToNull(v); return n == null ? null : "%"+n+"%"; }
}
