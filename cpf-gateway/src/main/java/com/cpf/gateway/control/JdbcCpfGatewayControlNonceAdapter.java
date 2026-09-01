package com.cpf.gateway.control;

import com.cpf.gateway.api.CpfGatewayControlNoncePort;
import java.sql.Timestamp;
import org.springframework.dao.DataIntegrityViolationException;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** DB Unique Key를 이용해 Control Nonce를 전 Instance에서 원자 Claim합니다. */
@Repository
public class JdbcCpfGatewayControlNonceAdapter implements CpfGatewayControlNoncePort {
    private final JdbcTemplate jdbc;

    public JdbcCpfGatewayControlNonceAdapter(CpfDataSourceRegistry dataSources) {
        // 형제 Gateway Adapter 와 동일하게 canonical role DataSource 에서 직접 만든다.
        // 1-WAS 처럼 JdbcTemplate 후보가 여럿인 구성에서 무자격 주입은 기동을 막고,
        // 후보가 하나뿐일 때는 Gateway 가 아닌 DB 로 쓰게 된다.
        this.jdbc = new JdbcTemplate(dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(NonceClaim claim) {
        // Cleanup 실패도 저장소 이상이므로 숨기지 않고 호출을 거부합니다.
        jdbc.update("DELETE FROM GW_CONTROL_NONCE WHERE expires_at < CURRENT_TIMESTAMP");
        try {
            return jdbc.update("""
                    INSERT INTO GW_CONTROL_NONCE
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
