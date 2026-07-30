package com.cpf.batch.worker;

import com.cpf.batch.api.BatchJobDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Published Batch Definition의 Worker Runtime Projection을 조회합니다.
 *
 * <p>Worker는 Draft/Approval 원본이나 Job Pack Manifest를 실행 정본으로 사용하지 않습니다.
 * 현재 시각에 유효한 ACTIVE Projection 한 건만 허용하며 중복 Projection은 Fail-closed합니다.</p>
 */
@Repository
public class BatchRuntimeProjectionRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public BatchRuntimeProjectionRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public BatchJobDefinition required(String jobId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<ProjectionRow> rows = jdbc.query("""
                SELECT job_id,definition_version,definition_checksum,projection_json,projection_hash,row_version
                  FROM bat_job_runtime_projection
                 WHERE job_id=?
                   AND projection_status='ACTIVE'
                   AND (effective_from IS NULL OR effective_from<=?)
                   AND (effective_until IS NULL OR effective_until>?)
                 ORDER BY definition_version DESC
                """, (rs, row) -> map(rs), jobId, now, now);
        if (rows.size() != 1) {
            throw rows.isEmpty()
                    ? new NoSuchElementException("Published Batch Runtime Projection not found: " + jobId)
                    : new IllegalStateException("Multiple ACTIVE Batch Runtime Projections detected: " + jobId);
        }
        return decode(rows.getFirst(), jobId);
    }

    /** 이미 생성된 Execution이 고정한 Version/Checksum을 Retire 이후에도 정확히 재현합니다. */
    public BatchJobDefinition required(String jobId, long definitionVersion, String checksum) {
        List<ProjectionRow> rows = jdbc.query("""
                SELECT job_id,definition_version,definition_checksum,projection_json,projection_hash,row_version
                  FROM bat_job_runtime_projection
                 WHERE job_id=? AND definition_version=? AND definition_checksum=?
                   AND projection_status IN ('ACTIVE','RETIRED')
                """, (rs, row) -> map(rs), jobId, definitionVersion, checksum);
        if (rows.size() != 1) {
            throw rows.isEmpty()
                    ? new NoSuchElementException("Pinned Batch Runtime Projection not found: "
                            + jobId + "@" + definitionVersion)
                    : new IllegalStateException("Duplicate Batch Runtime Projection detected: "
                            + jobId + "@" + definitionVersion);
        }
        return decode(rows.getFirst(), jobId);
    }

    private ProjectionRow map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProjectionRow(
                rs.getString("job_id"), rs.getLong("definition_version"),
                rs.getString("definition_checksum"), rs.getString("projection_json"),
                rs.getString("projection_hash"), rs.getLong("row_version"));
    }

    private BatchJobDefinition decode(ProjectionRow row, String jobId) {
        if (blank(row.definitionChecksum()) || blank(row.projectionHash())
                || !row.definitionChecksum().equalsIgnoreCase(row.projectionHash())) {
            throw new IllegalStateException("Batch Runtime Projection checksum drift: " + jobId);
        }
        try {
            BatchJobDefinition definition = mapper.readValue(row.projectionJson(), BatchJobDefinition.class);
            if (!definition.jobId().equals(row.jobId())
                    || definition.definitionVersion() != row.definitionVersion()
                    || !Set.of(BatchJobDefinition.State.PUBLISHED, BatchJobDefinition.State.RETIRED)
                            .contains(definition.state())
                    || !definition.checksum().equalsIgnoreCase(row.definitionChecksum())) {
                throw new IllegalStateException("Batch Runtime Projection contract mismatch: " + jobId);
            }
            return definition;
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("Batch Runtime Projection JSON is invalid: " + jobId, failure);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private record ProjectionRow(
            String jobId,
            long definitionVersion,
            String definitionChecksum,
            String projectionJson,
            String projectionHash,
            long rowVersion) {}
}
