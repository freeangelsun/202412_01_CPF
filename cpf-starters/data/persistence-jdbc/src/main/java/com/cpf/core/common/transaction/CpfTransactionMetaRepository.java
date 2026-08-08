package com.cpf.core.common.transaction;

import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 온라인 거래 메타 저장소입니다.
 *
 * <p>cpfDB가 아직 설치되지 않았거나 신규 테이블이 적용되지 않은 개발 환경에서도
 * 애플리케이션 기동을 막지 않도록 쓰기 계열은 실패를 호출자에게 전파하지 않습니다.
 * ADM 조회 API처럼 운영자가 직접 호출하는 기능은 조회 결과에 available 상태를 노출해
 * DB 적용 여부를 분명히 확인할 수 있게 합니다.</p>
 */
public class CpfTransactionMetaRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;
    private final CpfVendorSqlCatalog sql;

    public CpfTransactionMetaRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
        this.sql = CpfVendorSqlCatalog.create(environment, "cpf");
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public boolean tableAvailable() {
        if (!available()) {
            return false;
        }
        try {
            Integer count = jdbc().queryForObject(
                    sql.required("transaction-meta-table-available"), Integer.class);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public int upsertAll(Collection<CpfTransactionMeta> metas, String requestUser) {
        if (!tableAvailable() || metas == null || metas.isEmpty()) {
            return 0;
        }
        String user = defaultIfBlank(requestUser, "CPF_TRANSACTION_SCAN");
        int count = 0;
        for (CpfTransactionMeta meta : metas) {
            jdbc().update(sql.required("transaction-meta-upsert"),
                    required(meta.transactionId(), "transactionId"),
                    required(meta.transactionName(), "transactionName"),
                    defaultIfBlank(meta.moduleCode(), "CPF"),
                    blankToNull(meta.domainCode()),
                    defaultIfBlank(meta.httpMethod(), "ANY"),
                    defaultIfBlank(meta.apiPath(), "/"),
                    defaultIfBlank(meta.controllerClass(), "UNKNOWN"),
                    defaultIfBlank(meta.handlerMethod(), "UNKNOWN"),
                    blankToNull(meta.swaggerOperationId()),
                    blankToNull(meta.logPolicyKey()),
                    yn(meta.sensitiveYn(), "N"),
                    blankToNull(meta.maskingPolicyKey()),
                    user,
                    user);
            count++;
        }
        return count;
    }

    public int markMissingInactive(Collection<String> activeTransactionIds, String requestUser) {
        if (!tableAvailable() || activeTransactionIds == null || activeTransactionIds.isEmpty()) {
            return 0;
        }
        List<String> ids = activeTransactionIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        List<Object> args = new ArrayList<>(ids);
        args.add(defaultIfBlank(requestUser, "CPF_TRANSACTION_SCAN"));
        return jdbc().update(
                sql.required("transaction-meta-mark-missing-inactive").formatted(placeholders),
                rotateUserToFront(args).toArray());
    }

    public Optional<CpfDataRow> findById(String transactionId) {
        if (!tableAvailable() || transactionId == null || transactionId.isBlank()) {
            return Optional.empty();
        }
        List<CpfDataRow> rows = CpfDataRow.copyRows(jdbc().queryForList(
                sql.required("transaction-meta-find-by-id"), transactionId.trim()));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public List<CpfDataRow> findAll(String moduleCode, String activeYn, String transactionId, int limit) {
        return findPage(moduleCode, activeYn, transactionId, 0, limit).items();
    }

    public com.cpf.core.api.transaction.CpfTransactionMetaOperations.TransactionMetaPage findPage(
            String moduleCode,
            String activeYn,
            String transactionId,
            int page,
            int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        if (!tableAvailable()) {
            return new com.cpf.core.api.transaction.CpfTransactionMetaOperations.TransactionMetaPage(
                    false, List.of(), safePage, safeSize, 0L, 0);
        }
        QueryCriteria criteria = criteria(moduleCode, activeYn, transactionId);
        Object[] filterArguments = criteria.arguments();
        Long total = jdbc().queryForObject(
                sql.required("transaction-meta-count"), Long.class, filterArguments);
        long totalElements = total == null ? 0L : Math.max(0L, total);
        long offset = (long) safePage * safeSize;
        List<Object> arguments = new ArrayList<>(List.of(filterArguments));
        arguments.add(offset);
        arguments.add(safeSize);
        List<CpfDataRow> items = offset >= totalElements
                ? List.of()
                : CpfDataRow.copyRows(jdbc().queryForList(
                        sql.required("transaction-meta-find-page"), arguments.toArray()));
        int totalPages = totalElements == 0L ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new com.cpf.core.api.transaction.CpfTransactionMetaOperations.TransactionMetaPage(
                true, items, safePage, safeSize, totalElements, totalPages);
    }

    private QueryCriteria criteria(String moduleCode, String activeYn, String transactionId) {
        String normalizedModule = blankToNull(moduleCode);
        if (normalizedModule != null) {
            normalizedModule = normalizedModule.toUpperCase();
        }
        String normalizedActive = blankToNull(activeYn);
        if (normalizedActive != null) {
            normalizedActive = yn(normalizedActive, "Y");
        }
        String transactionPattern = blankToNull(transactionId);
        if (transactionPattern != null) {
            transactionPattern = "%" + transactionPattern + "%";
        }
        return new QueryCriteria(normalizedModule, normalizedActive, transactionPattern);
    }

    private record QueryCriteria(String moduleCode, String activeYn, String transactionPattern) {
        private Object[] arguments() {
            return new Object[]{
                    moduleCode, moduleCode,
                    activeYn, activeYn,
                    transactionPattern, transactionPattern
            };
        }
    }

    public CpfDataRow inactivate(String transactionId, String requestUser) {
        CpfDataRow result = new CpfDataRow();
        Optional<CpfDataRow> before = findById(transactionId);
        int updated = 0;
        if (before.isPresent()) {
            updated = jdbc().update(
                    sql.required("transaction-meta-inactivate"),
                    defaultIfBlank(requestUser, "ADM"), transactionId.trim());
        }
        result.put("updated", updated);
        result.put("before", before.orElse(CpfDataRow.of()));
        result.put("after", findById(transactionId).orElse(CpfDataRow.of()));
        return result;
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("cpfDataSource 또는 cpfJdbcTemplate이 필요합니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private List<Object> rotateUserToFront(List<Object> argsWithUserLast) {
        List<Object> rotated = new ArrayList<>();
        Object user = argsWithUserLast.remove(argsWithUserLast.size() - 1);
        rotated.add(user);
        rotated.addAll(argsWithUserLast);
        return rotated;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String yn(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback).toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }
}
