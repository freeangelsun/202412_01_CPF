package com.cpf.admin.approval.security;

import com.cpf.admin.common.base.AdmBaseRepository;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.data.persistence.api.CpfRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

/** JDBC-backed cluster-safe single-use ledger for approval execution capability nonces. */
// @CpfRepository 는 @Repository stereotype 이므로 Runtime 이 예외변환 Advice 를 위해 CGLIB
// 프록시를 생성한다. final 클래스는 subclass 를 만들 수 없어 기동 자체가 실패한다.
@CpfRepository
public class AdmApprovalCapabilityNonceRepository extends AdmBaseRepository {
    private final JdbcTemplate jdbc;

    public AdmApprovalCapabilityNonceRepository(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc=jdbc;
    }

    public void issue(String nonce,String approvalReference,Instant expiresAt) {
        try {
            jdbc.update("""
                INSERT INTO ADM_APPROVAL_CAPABILITY_NONCE
                  (NONCE_HASH,APPROVAL_REFERENCE,EXPIRES_AT,CONSUMED_AT,created_by,updated_by)
                VALUES (?,?,?,NULL,'ADM','ADM')
                """,sha256(nonce),approvalReference,java.sql.Timestamp.from(expiresAt));
        } catch (DuplicateKeyException duplicate) {
            throw new SecurityException("approval capability nonce collision",duplicate);
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public boolean consume(String nonce,String approvalReference,Instant now,String consumerId) {
        int changed=jdbc.update("""
            UPDATE ADM_APPROVAL_CAPABILITY_NONCE
               SET CONSUMED_AT=?,CONSUMED_BY=?,updated_by=?
             WHERE NONCE_HASH=? AND APPROVAL_REFERENCE=? AND CONSUMED_AT IS NULL AND EXPIRES_AT>=?
            """,java.sql.Timestamp.from(now),consumerId,consumerId,sha256(nonce),approvalReference,
                java.sql.Timestamp.from(now));
        return changed==1;
    }

    public int purgeExpired(Instant before) {
        return jdbc.update("""
            DELETE FROM ADM_APPROVAL_CAPABILITY_NONCE
             WHERE EXPIRES_AT<? AND (CONSUMED_AT IS NOT NULL OR EXPIRES_AT<?)
            """,java.sql.Timestamp.from(before),java.sql.Timestamp.from(before));
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable",impossible);
        }
    }
}
