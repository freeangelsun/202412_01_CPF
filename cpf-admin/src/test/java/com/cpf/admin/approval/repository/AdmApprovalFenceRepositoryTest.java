package com.cpf.admin.approval.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 승인 실행 terminal write의 fencing 회귀를 검증한다.
 * stale owner가 재예약 이후 이전 lease/fence로 완료·UNKNOWN을 기록하려 하면
 * DB CAS 결과 0건을 반드시 실패로 취급해야 한다.
 */
class AdmApprovalFenceRepositoryTest {

    @Test
    void staleFenceCannotFinalizeExecution() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);
        AdmApprovalRepository repository = new AdmApprovalRepository(jdbc);

        assertThatThrownBy(() -> repository.finishExecution(
                41L, "cmd-old", "instance-a", 7L,
                "SUCCEEDED", "OK", "done", false, "operator"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected by fence");
    }

    @Test
    void staleFenceCannotForceUnknownEither() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0, 1);
        AdmApprovalRepository repository = new AdmApprovalRepository(jdbc);

        assertThatThrownBy(() -> repository.markExecutionUnknown(
                41L, "cmd-old", "instance-a", 7L,
                "TIMEOUT", "unknown", "operator"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UNKNOWN transition failed");
    }

    @Test
    void currentFenceCanFinalizeExactlyOneExecutionRow() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        AdmApprovalRepository repository = new AdmApprovalRepository(jdbc);

        repository.finishExecution(
                41L, "cmd-current", "instance-b", 8L,
                "SUCCEEDED", "OK", "done", false, "operator");

        verify(jdbc).update(anyString(), any(Object[].class));
    }
}
