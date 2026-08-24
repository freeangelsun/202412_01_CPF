package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

class JdbcCpfBatchRemoteMessageLedgerTest {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void activeLeaseBlocksDuplicateDeliveryEvenForSameOwner() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        Instant now = Instant.parse("2026-07-31T00:00:00Z");
        when(jdbc.update(anyString(), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("hash");
        when(rs.getString(2)).thenReturn("PROCESSING");
        when(rs.getString(3)).thenReturn("instance-a");
        when(rs.getTimestamp(4)).thenReturn(Timestamp.from(now.plusSeconds(30)));
        when(rs.getTimestamp(5)).thenReturn(Timestamp.from(now.plusSeconds(300)));
        when(rs.getLong(6)).thenReturn(7L);
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenAnswer(invocation -> ((ResultSetExtractor) invocation.getArgument(1)).extractData(rs));

        JdbcCpfBatchRemoteMessageLedger ledger = new JdbcCpfBatchRemoteMessageLedger(
                jdbc, Clock.fixed(now, ZoneOffset.UTC), 60, sql);

        assertThat(ledger.claim(
                "REQUEST", "message-1", "hash", now.plusSeconds(300), "instance-a"))
                .isEqualTo(CpfBatchRemoteMessageLedger.Claim.IN_PROGRESS);
        verify(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
    }
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void rejectsReplayAfterOriginalMessageTtlExpired() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        Instant now = Instant.parse("2026-07-31T00:00:00Z");
        when(jdbc.update(anyString(), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("hash");
        when(rs.getString(2)).thenReturn("FAILED");
        when(rs.getString(3)).thenReturn("instance-a");
        when(rs.getTimestamp(4)).thenReturn(Timestamp.from(now.minusSeconds(1)));
        when(rs.getTimestamp(5)).thenReturn(Timestamp.from(now.minusSeconds(1)));
        when(rs.getLong(6)).thenReturn(8L);
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenAnswer(invocation -> ((ResultSetExtractor) invocation.getArgument(1)).extractData(rs));

        JdbcCpfBatchRemoteMessageLedger ledger = new JdbcCpfBatchRemoteMessageLedger(
                jdbc, Clock.fixed(now, ZoneOffset.UTC), 60, sql);

        assertThatThrownBy(() -> ledger.claim(
                "REQUEST", "message-1", "hash", now.plusSeconds(300), "instance-a"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("BATCH_REMOTE_MESSAGE_REPLAY_EXPIRED");
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void unknownStateRequiresExplicitReconcileAndIsNeverReclaimed() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        Instant now = Instant.parse("2026-07-31T00:00:00Z");
        when(jdbc.update(anyString(), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("hash");
        when(rs.getString(2)).thenReturn("UNKNOWN");
        when(rs.getString(3)).thenReturn("worker-a:attempt-1");
        when(rs.getTimestamp(4)).thenReturn(Timestamp.from(now.minusSeconds(30)));
        when(rs.getTimestamp(5)).thenReturn(Timestamp.from(now.plusSeconds(300)));
        when(rs.getLong(6)).thenReturn(9L);
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenAnswer(invocation -> ((ResultSetExtractor) invocation.getArgument(1)).extractData(rs));

        JdbcCpfBatchRemoteMessageLedger ledger = new JdbcCpfBatchRemoteMessageLedger(
                jdbc, Clock.fixed(now, ZoneOffset.UTC), 60, sql);

        assertThat(ledger.claim(
                "REQUEST", "message-unknown", "hash", now.plusSeconds(300), "worker-b"))
                .isEqualTo(CpfBatchRemoteMessageLedger.Claim.UNKNOWN_RECONCILE_REQUIRED);
        verify(jdbc, never()).update(
                org.mockito.ArgumentMatchers.eq("REMOTE_RECLAIM"), any(Object[].class));
    }

    @Test
    void unknownTransitionUsesTheFencedProcessingOwner() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        Instant now = Instant.parse("2026-07-31T00:00:00Z");
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        JdbcCpfBatchRemoteMessageLedger ledger = new JdbcCpfBatchRemoteMessageLedger(
                jdbc, Clock.fixed(now, ZoneOffset.UTC), 60, sql);

        ledger.unknown("REQUEST", "message-unknown", "worker-a:attempt-1", "response lost");

        verify(jdbc).update(
                org.mockito.ArgumentMatchers.eq("REMOTE_TRANSITION"),
                org.mockito.ArgumentMatchers.eq("UNKNOWN"),
                org.mockito.ArgumentMatchers.eq("response_lost"),
                org.mockito.ArgumentMatchers.eq(Timestamp.from(now)),
                org.mockito.ArgumentMatchers.eq(Timestamp.from(now)),
                org.mockito.ArgumentMatchers.eq("REQUEST"),
                org.mockito.ArgumentMatchers.eq("message-unknown"),
                org.mockito.ArgumentMatchers.eq("worker-a:attempt-1"));
    }

    private static CpfVendorSqlCatalog catalog() {
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        when(catalog.required("execution-remote-message-insert")).thenReturn("REMOTE_INSERT");
        when(catalog.required("execution-remote-message-find")).thenReturn("REMOTE_FIND");
        when(catalog.required("execution-remote-message-reclaim")).thenReturn("REMOTE_RECLAIM");
        when(catalog.required("execution-remote-message-transition")).thenReturn("REMOTE_TRANSITION");
        return catalog;
    }

}
