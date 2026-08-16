package com.cpf.admin.opr.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmAuditLogServiceFailClosedTest {
    @Test
    void recordPropagatesDurableDeliveryFailureInsteadOfReturningSuccess() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AdmAuditDeliveryService delivery = mock(AdmAuditDeliveryService.class);
        doThrow(new IllegalStateException("outbox unavailable"))
                .when(delivery).record(any(AdmAuditDeliveryService.AuditCommand.class), any(), any());
        AdmAuditLogService service = new AdmAuditLogService(jdbc, delivery);

        assertThatThrownBy(() -> service.record(
                "tx-1", "operator-1", "UPDATE", "POLICY", "P-1", "approved change", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outbox unavailable");
    }

    @Test
    void queryFailureIsNotReportedAsEmptyAuditResult() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AdmAuditDeliveryService delivery = mock(AdmAuditDeliveryService.class);
        when(jdbc.query(any(String.class), any(org.springframework.jdbc.core.PreparedStatementSetter.class),
                any(org.springframework.jdbc.core.RowMapper.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        AdmAuditLogService service = new AdmAuditLogService(jdbc, delivery);

        assertThatThrownBy(() -> service.findAuditLogs(null, null, null, null, 100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0건과 DB 장애를 구분");
    }
}
