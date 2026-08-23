package com.cpf.admin.opr.transaction.repository;

import com.cpf.data.api.CpfDataRow;
import com.cpf.data.persistence.api.CpfRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

/** Canonical Operation Catalog와 ADM-owned Operation Policy의 query owner입니다. */
@CpfRepository
public class AdmTransactionMetaRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public AdmTransactionMetaRepository(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider catalogs) {
        this.jdbc = jdbc;
        this.sql = catalogs.forModule("cpf");
    }

    public List<CpfDataRow> findAll(
            String moduleCode, String activeYn, String operationIdLike, int limit) {
        return rows(jdbc.queryForList(
                sql.required("transaction-meta-find-all"),
                moduleCode, moduleCode, activeYn, activeYn, operationIdLike, operationIdLike, limit));
    }

    public long count(String moduleCode, String activeYn, String operationIdLike) {
        Long count = jdbc.queryForObject(
                sql.required("transaction-meta-count"),
                Long.class,
                moduleCode, moduleCode, activeYn, activeYn, operationIdLike, operationIdLike);
        return count == null ? 0L : count;
    }

    public List<CpfDataRow> findPage(
            String moduleCode, String activeYn, String operationIdLike, int offset, int limit) {
        return rows(jdbc.queryForList(
                sql.required("transaction-meta-find-page"),
                moduleCode, moduleCode, activeYn, activeYn, operationIdLike, operationIdLike, offset, limit));
    }

    public Optional<CpfDataRow> findById(String operationId) {
        return rows(jdbc.queryForList(
                sql.required("transaction-meta-find-by-id"), operationId)).stream().findFirst();
    }

    public int inactivate(
            String operationId, long expectedPolicyVersion, String operatorId, String reason) {
        return jdbc.update(
                sql.required("transaction-meta-inactivate"),
                reason, operatorId, operationId, expectedPolicyVersion);
    }

    private static List<CpfDataRow> rows(List<Map<String, Object>> source) {
        return source.stream().map(CpfDataRow::new).toList();
    }
}
