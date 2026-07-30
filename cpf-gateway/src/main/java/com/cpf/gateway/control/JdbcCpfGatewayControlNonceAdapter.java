package com.cpf.gateway.control;

import com.cpf.core.api.gateway.CpfGatewayControlNoncePort;
import java.sql.Timestamp;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** DB Unique Key를 이용해 Control Nonce를 전 Instance에서 원자 Claim합니다. */
@Repository
public class JdbcCpfGatewayControlNonceAdapter implements CpfGatewayControlNoncePort {
    private final JdbcTemplate jdbc;

    public JdbcCpfGatewayControlNonceAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(NonceClaim claim) {
        // Cleanup 실패도 저장소 이상이므로 숨기지 않고 호출을 거부합니다.
        jdbc.update("DELETE FROM cpf_gateway_control_nonce WHERE expires_at < CURRENT_TIMESTAMP");
        try {
            return jdbc.update("""
                    INSERT INTO cpf_gateway_control_nonce
                        (audience, key_id, caller_id, nonce, claimed_at, expires_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    claim.audience(), claim.keyId(), claim.callerId(), claim.nonce(),
                    Timestamp.from(claim.claimedAt()), Timestamp.from(claim.expiresAt())) == 1;
        } catch (DataIntegrityViolationException replay) {
            return false;
        }
    }
}
