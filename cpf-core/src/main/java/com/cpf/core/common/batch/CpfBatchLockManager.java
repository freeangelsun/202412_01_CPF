package cpf.pfw.common.batch;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * PFW 표준 배치 중복 실행 방지 lock 관리자입니다.
 *
 * <p>동일 Job/파라미터 기준으로 여러 WAS가 동시에 실행하지 못하도록 pfw_batch_lock을 사용합니다.</p>
 */
public class CpfBatchLockManager {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public CpfBatchLockManager(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public boolean acquire(String lockKey, String jobId, String jobParameters, String ownerId, int ttlSeconds) {
        if (!available()) {
            return true;
        }
        releaseExpired();
        try {
            jdbc().update("""
                    INSERT INTO pfw_batch_lock (
                        lock_key, job_id, job_parameters_hash, owner_id, expire_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, TIMESTAMPADD(SECOND, ?, CURRENT_TIMESTAMP(3)), ?, ?)
                    """,
                    required(lockKey, "lockKey"),
                    required(jobId, "jobId"),
                    hash(jobParameters),
                    required(ownerId, "ownerId"),
                    Math.max(30, ttlSeconds),
                    ownerId,
                    ownerId);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    public void release(String lockKey, String ownerId) {
        if (!available()) {
            return;
        }
        jdbc().update(
                "DELETE FROM pfw_batch_lock WHERE lock_key = ? AND owner_id = ?",
                lockKey,
                ownerId);
    }

    public void releaseExpired() {
        if (!available()) {
            return;
        }
        jdbc().update("DELETE FROM pfw_batch_lock WHERE expire_at <= CURRENT_TIMESTAMP(3)");
    }

    public String lockKey(String jobId, String jobParameters) {
        return "batch:job:" + required(jobId, "jobId") + ":" + hash(jobParameters);
    }

    public String scheduleLockKey(String scheduleId, String plannedRunAt) {
        return "batch:schedule:" + required(scheduleId, "scheduleId") + ":" + required(plannedRunAt, "plannedRunAt");
    }

    public String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(defaultIfBlank(value, "{}").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("배치 lock hash를 생성할 수 없습니다.", ex);
        }
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("PFW datasource가 없어 배치 lock을 사용할 수 없습니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
