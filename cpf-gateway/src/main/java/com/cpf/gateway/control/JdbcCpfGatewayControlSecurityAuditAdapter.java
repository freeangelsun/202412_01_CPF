package com.cpf.gateway.control;

import com.cpf.gateway.api.CpfGatewayControlSecurityAuditPort;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Gateway Control 보안 실패를 append-only DB 원장에 기록합니다. */
@Repository
public final class JdbcCpfGatewayControlSecurityAuditAdapter implements CpfGatewayControlSecurityAuditPort {
    private final JdbcTemplate jdbc;

    public JdbcCpfGatewayControlSecurityAuditAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void append(SecurityFailure event) {
        int inserted = jdbc.update("""
                INSERT INTO GW_CONTROL_SECURITY_AUDIT
                    (event_id, occurred_at, audience, key_id, caller_service, operator_id,
                     http_method, request_target, remote_address, result_code, safe_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                event.eventId(), Timestamp.from(event.occurredAt()), event.audience(), event.keyId(),
                event.callerService(), event.operatorId(), event.httpMethod(), event.requestTarget(),
                event.remoteAddress(), event.resultCode(), event.safeMessage());
        if (inserted != 1) {
            throw new IllegalStateException("Gateway Control security audit insert count=" + inserted);
        }
    }
}
