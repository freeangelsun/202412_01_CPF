package com.cpf.admin.opr.service;

import com.cpf.data.api.CpfDataRow;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.foundation.annotation.CpfService;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * ADM 온라인 거래 메타 운영 서비스입니다.
 *
 * <p>별도 Core/Consumer 없는 추상화를 만들지 않고 ADM Owner가 Spring MVC scan과 cpfDB 운영 Projection을
 * 직접 연결합니다. SQL은 공식 3개 Vendor Pack의 {@code cpf/repository/transaction-meta-*}만 사용합니다.</p>
 */
@CpfService
public class AdmTransactionMetaService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final RequestMappingHandlerMapping mappings;

    public AdmTransactionMetaService(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider catalogs,
            RequestMappingHandlerMapping mappings) {
        this.jdbc = jdbc;
        this.sql = catalogs.forModule("cpf");
        this.mappings = mappings;
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

    /** 현재 Application의 거래 메타 재스캔 결과입니다. */
    public record TransactionMetaScanResult(
            boolean available, int detectedCount, int upsertedCount, int inactivatedCount,
            List<String> transactionIds, String message) {
        public TransactionMetaScanResult { transactionIds = transactionIds == null ? List.of() : List.copyOf(transactionIds); }
    }

    public boolean tableAvailable() {
        Integer count = jdbc.queryForObject(sql.required("transaction-meta-table-available.sql"), Integer.class);
        return count != null && count > 0;
    }

    public CpfDataRow findTransactions(String moduleCode, String activeYn, String transactionId, int limit) {
        CpfDataRow response = new CpfDataRow();
        boolean available = tableAvailable();
        response.put("available", available);
        response.put("items", available ? findAll(moduleCode, activeYn, transactionId, limit) : List.of());
        return response;
    }

    public List<CpfDataRow> findAll(String moduleCode, String activeYn, String transactionId, int limit) {
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(transactionId);
        int bounded = Math.max(1, Math.min(limit, 1_000));
        return rows(jdbc.queryForList(sql.required("transaction-meta-find-all.sql"), module, module, active, active, id, id, bounded));
    }

    public TransactionMetaPage findPage(String moduleCode, String activeYn, String transactionId, int page, int size) {
        if (!tableAvailable()) return new TransactionMetaPage(false, List.of(), page, size, 0, 0);
        String module = blankToNull(moduleCode); String active = blankToNull(activeYn); String id = like(transactionId);
        int safePage = Math.max(0, page); int safeSize = Math.max(1, Math.min(size, 200)); int offset = safePage * safeSize;
        Long total = jdbc.queryForObject(sql.required("transaction-meta-count.sql"), Long.class,
                module, module, active, active, id, id);
        Object[] pageArgs = new Object[] {module, module, active, active, id, id, offset, safeSize};
        List<CpfDataRow> items = rows(jdbc.queryForList(sql.required("transaction-meta-find-page.sql"), pageArgs));
        long count = total == null ? 0L : total;
        return new TransactionMetaPage(true, items, safePage, safeSize, count, (int) ((count + safeSize - 1) / safeSize));
    }

    public CpfDataRow findTransaction(String transactionId) {
        CpfDataRow response = new CpfDataRow(); boolean available = tableAvailable(); response.put("available", available);
        response.put("item", available ? findById(transactionId).orElse(CpfDataRow.of()) : CpfDataRow.of()); return response;
    }

    public Optional<CpfDataRow> findById(String transactionId) {
        List<CpfDataRow> rows = rows(jdbc.queryForList(sql.required("transaction-meta-find-by-id.sql"), transactionId));
        return rows.stream().findFirst();
    }

    /** 현재 MVC Handler를 스캔해 선언된 거래를 upsert하고 사라진 거래를 inactive 처리합니다. */
    public TransactionMetaScanResult scan(String operatorId) {
        if (!tableAvailable()) return new TransactionMetaScanResult(false, 0, 0, 0, List.of(), "cpf_transaction_meta unavailable");
        List<DetectedTransaction> detected = detectTransactions();
        int upserted = 0;
        for (DetectedTransaction tx : detected) {
            upserted += jdbc.update(sql.required("transaction-meta-upsert.sql"),
                    tx.id(), tx.name(), tx.moduleCode(), tx.domainCode(), tx.httpMethod(), tx.apiPath(),
                    tx.controllerClass(), tx.handlerMethod(), tx.operationId(), null, "N", null, operatorId, operatorId);
        }
        int inactivated = 0;
        if (!detected.isEmpty()) {
            String marks = String.join(",", java.util.Collections.nCopies(detected.size(), "?"));
            String statement = sql.required("transaction-meta-mark-missing-inactive.sql").formatted(marks);
            List<Object> args = new ArrayList<>(); args.add(operatorId); args.addAll(detected.stream().map(DetectedTransaction::id).toList());
            inactivated = jdbc.update(statement, args.toArray());
        }
        List<String> ids = detected.stream().map(DetectedTransaction::id).sorted().toList();
        return new TransactionMetaScanResult(true, detected.size(), upserted, inactivated, ids, "scan completed");
    }

    public CpfDataRow inactivate(String transactionId, String operatorId) {
        CpfDataRow before = findById(transactionId).orElse(CpfDataRow.of());
        int updated = jdbc.update(sql.required("transaction-meta-inactivate.sql"), operatorId, transactionId);
        CpfDataRow after = findById(transactionId).orElse(CpfDataRow.of());
        return CpfDataRow.of("updated", updated, "before", before, "after", after);
    }

    private List<DetectedTransaction> detectTransactions() {
        List<DetectedTransaction> result = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappings.getHandlerMethods().entrySet()) {
            HandlerMethod handler = entry.getValue(); Method method = handler.getMethod();
            CpfOnlineTransaction tx = AnnotatedElementUtils.findMergedAnnotation(method, CpfOnlineTransaction.class);
            if (tx == null) tx = AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(), CpfOnlineTransaction.class);
            if (tx == null || tx.id().isBlank()) continue;
            Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
            String operationId = operation == null || operation.operationId().isBlank() ? null : operation.operationId();
            String httpMethod = entry.getKey().getMethodsCondition().getMethods().stream().map(Enum::name).sorted().findFirst().orElse("ANY");
            String path = entry.getKey().getPatternValues().stream().sorted().findFirst().orElse("/");
            String owner = tx.ownerDomain().isBlank() ? module(handler.getBeanType()) : tx.ownerDomain();
            result.add(new DetectedTransaction(tx.id(), tx.name(), module(handler.getBeanType()), owner, httpMethod, path,
                    handler.getBeanType().getName(), method.getName(), operationId));
        }
        result.sort(Comparator.comparing(DetectedTransaction::id)); return result;
    }

    private static String module(Class<?> type) {
        String name = type.getPackageName();
        if (name.startsWith("com.cpf.admin")) return "ADM";
        if (name.startsWith("com.cpf.bizadmin")) return "BZA";
        String[] parts = name.split("\\."); return parts.length > 2 ? parts[2].toUpperCase(java.util.Locale.ROOT) : "CPF";
    }
    private static List<CpfDataRow> rows(List<Map<String,Object>> source) { return source.stream().map(CpfDataRow::new).toList(); }
    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private static String like(String v) { String n=blankToNull(v); return n == null ? null : "%"+n+"%"; }
    private record DetectedTransaction(String id,String name,String moduleCode,String domainCode,String httpMethod,String apiPath,String controllerClass,String handlerMethod,String operationId) {}
}
