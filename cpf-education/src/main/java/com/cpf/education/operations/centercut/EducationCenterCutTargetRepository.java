package com.cpf.education.operations.centercut;
import com.cpf.batch.spi.CenterCutTargetProvider;
import com.cpf.batch.api.centercut.CpfCenterCutResult;
import com.cpf.batch.api.centercut.CpfCenterCutStatus;
import com.cpf.batch.api.centercut.CpfCenterCutTarget;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * EDU 업무 DB 테이블을 CPF center-cut 표준 계약에 연결하는 adapter입니다.
 *
 * <p>CPF는 {@link CenterCutTargetProvider} 계약만 알고, 실제 대상/결과 저장소는
 * 업무 모듈인 EDU가 소유합니다. 이 구조가 유지되어야 다른 업무 모듈도 CPF 수정 없이
 * 자기 업무 테이블을 center-cut에 연결할 수 있습니다.</p>
 */
@Repository("refCenterCutTargetProvider")
public class EducationCenterCutTargetRepository implements CenterCutTargetProvider {
    private final JdbcTemplate jdbcTemplate;
    private final Statements statements;

    @Autowired
    /** EducationCenterCutTargetRepository 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationCenterCutTargetRepository(
            @Qualifier("educationReferenceFixtureJdbcTemplate") JdbcTemplate jdbcTemplate,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbcTemplate, sqlCatalogProvider.forModule("ref"));
    }

    EducationCenterCutTargetRepository(
            JdbcTemplate jdbcTemplate,
            CpfVendorSqlCatalog sqlCatalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.statements = Statements.load(sqlCatalog);
    }

    @Override
    public String providerKey() {
        return "refCenterCutTargetProvider";
    }

    @Override
    public List<CenterCutTargetProvider.Target> next(
            String jobId, String snapshotId, String cursor, int limit, Map<String, Object> parameters) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        int fetchLimit = Math.min(1000, Math.max(limit, limit * 4));
        List<CpfCenterCutTarget> candidates = findReadyTargets(jobId, fetchLimit);
        java.util.ArrayList<CenterCutTargetProvider.Target> page = new java.util.ArrayList<>();
        boolean afterCursor = cursor == null || cursor.isBlank();
        for (CpfCenterCutTarget target : candidates) {
            if (!afterCursor) {
                if (target.targetId().equals(cursor)) {
                    afterCursor = true;
                }
                continue;
            }
            if (page.size() >= limit) {
                break;
            }
            page.add(new CenterCutTargetProvider.Target(
                    target.businessKey(), target.targetId(), target.payload(), false));
        }
        if (!page.isEmpty() && page.size() < limit) {
            var last = page.get(page.size() - 1);
            page.set(page.size() - 1, new CenterCutTargetProvider.Target(
                    last.businessKey(), last.cursor(), last.payload(), true));
        }
        return List.copyOf(page);
    }

    /** 준비 상태의 업무 대상을 조회하며 CPF가 직접 업무 테이블 구조를 알 필요가 없게 한다. */
    public List<CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit) {
        return jdbcTemplate.query(
                statements.findReadyTargets(),
                (rs, rowNum) -> mapTarget(rs),
                centerCutJobId,
                Math.max(1, limit));
    }

    /** 실행권을 확보한 대상만 RUNNING으로 전이하여 중복 처리 경계를 명확히 한다. */
    public void markRunning(CpfCenterCutTarget target) {
        int updated = jdbcTemplate.update(
                statements.markRunning(),
                target.transactionId(),
                target.parentSegmentId(),
                target.transactionSegmentId(),
                target.targetId(),
                target.centerCutJobId());
        if (updated == 0) {
            throw new IllegalStateException("center-cut 대상 상태를 RUNNING으로 변경하지 못했습니다. targetId=" + target.targetId());
        }
    }

    /** CPF 실행 결과와 업무 결과 원장을 함께 갱신해 최종 대사와 복구 추적을 가능하게 한다. */
    public void markResult(CpfCenterCutTarget target, CpfCenterCutResult result) {
        String statusCode = result.status().name();
        String errorMessage = result.status() == CpfCenterCutStatus.FAILED ? result.message() : null;
        String transactionSegmentId = hasText(result.transactionSegmentId())
                ? result.transactionSegmentId()
                : target.transactionSegmentId();

        jdbcTemplate.update(
                statements.markResultTarget(),
                statusCode,
                target.transactionId(),
                target.parentSegmentId(),
                transactionSegmentId,
                errorMessage,
                target.targetId(),
                target.centerCutJobId());

        jdbcTemplate.update(
                statements.upsertResult(),
                target.targetId(),
                target.centerCutJobId(),
                target.businessKey(),
                statusCode,
                result.resultPayload(),
                result.message(),
                target.transactionId(),
                target.parentSegmentId(),
                transactionSegmentId);
    }

    /** countResultsByStatus 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, Long> countResultsByStatus(String centerCutJobId) {
        return jdbcTemplate.query(
                statements.countResultsByStatus(),
                rs -> {
                    Map<String, Long> counts = new java.util.LinkedHashMap<>();
                    while (rs.next()) {
                        counts.put(rs.getString("result_status"), rs.getLong("result_count"));
                    }
                    return counts;
                },
                centerCutJobId);
    }

    /** findResultSnapshots 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<Map<String, Object>> findResultSnapshots(String centerCutJobId) {
        return jdbcTemplate.queryForList(statements.findResultSnapshots(), centerCutJobId);
    }

    public void resetSampleTargetsForSmoke() {
        jdbcTemplate.update(
                statements.deleteSmokeResults(),
                EducationCenterCutConstants.JOB_ID);
        jdbcTemplate.update(
                statements.resetSmokeTargets(),
                EducationCenterCutConstants.JOB_ID);
    }

    private CpfCenterCutTarget mapTarget(ResultSet rs) throws SQLException {
        Date businessDate = rs.getDate("business_date");
        return new CpfCenterCutTarget(
                rs.getString("target_id"),
                rs.getString("center_cut_job_id"),
                rs.getString("business_key"),
                businessDate == null ? LocalDate.now() : businessDate.toLocalDate(),
                rs.getString("target_payload"),
                rs.getString("transaction_id"),
                rs.getString("parent_segment_id"),
                rs.getString("transaction_segment_id"),
                rs.getInt("retry_count"),
                CpfCenterCutStatus.valueOf(rs.getString("status_code").toUpperCase(Locale.ROOT)));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** Statements 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record Statements(
            String findReadyTargets,
            String markRunning,
            String markResultTarget,
            String upsertResult,
            String countResultsByStatus,
            String findResultSnapshots,
            String deleteSmokeResults,
            String resetSmokeTargets) {
        static Statements load(CpfVendorSqlCatalog catalog) {
            return new Statements(
                    catalog.required("centercut-find-ready-targets"),
                    catalog.required("centercut-mark-running"),
                    catalog.required("centercut-mark-result-target"),
                    catalog.required("centercut-upsert-result"),
                    catalog.required("centercut-count-results-by-status"),
                    catalog.required("centercut-find-result-snapshots"),
                    catalog.required("centercut-delete-smoke-results"),
                    catalog.required("centercut-reset-smoke-targets"));
        }
    }
}
