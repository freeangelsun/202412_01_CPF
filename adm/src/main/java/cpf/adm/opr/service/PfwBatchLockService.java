package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 배치 중복 실행을 막기 위한 DB lock 서비스입니다.
 *
 * <p>여러 WAS 인스턴스가 같은 스케줄을 동시에 감지해도 lock_key 기준으로 하나만 실행되도록 합니다.</p>
 */
@Service
public class PfwBatchLockService {
    private final JdbcTemplate pfwJdbcTemplate;

    public PfwBatchLockService(@Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
    }

    public boolean acquire(String lockKey, String jobId, String jobParameters, String ownerId, int ttlSeconds) {
        releaseExpired();
        try {
            pfwJdbcTemplate.update("""
                    INSERT INTO pfw_batch_lock (
                        lock_key, job_id, job_parameters_hash, owner_id, expire_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, TIMESTAMPADD(SECOND, ?, CURRENT_TIMESTAMP(3)), ?, ?)
                    """,
                    lockKey,
                    jobId,
                    sha256(jobParameters),
                    ownerId,
                    Math.max(30, ttlSeconds),
                    ownerId,
                    ownerId);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    public void release(String lockKey, String ownerId) {
        pfwJdbcTemplate.update(
                "DELETE FROM pfw_batch_lock WHERE lock_key = ? AND owner_id = ?",
                lockKey,
                ownerId);
    }

    public void releaseExpired() {
        pfwJdbcTemplate.update("DELETE FROM pfw_batch_lock WHERE expire_at <= CURRENT_TIMESTAMP(3)");
    }

    public String lockKey(String scheduleId, String plannedRunAt) {
        return "batch:schedule:" + TextUtils.requireText(scheduleId, "scheduleId") + ":" + plannedRunAt;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(TextUtils.defaultIfBlank(value, "{}").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("배치 lock hash를 생성할 수 없습니다.", ex);
        }
    }
}
