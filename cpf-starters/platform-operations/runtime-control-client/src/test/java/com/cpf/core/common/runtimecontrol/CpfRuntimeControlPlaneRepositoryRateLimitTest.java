package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfRuntimeControlPlaneRepositoryRateLimitTest {

    @Test
    void concurrentFirstBucketCreationRetriesConditionalIncrement() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        // initial conditional update misses; insert loses unique race; second conditional update succeeds
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("COUNT(*)"),
                org.mockito.ArgumentMatchers.eq(Integer.class), any()))
                .thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0, 1);
        CpfRuntimeControlPlaneRepository repository = new CpfRuntimeControlPlaneRepository(
                jdbc, new ObjectMapper(), () -> java.time.Instant.parse("2026-08-05T03:00:30Z"));

        assertDoesNotThrow(() -> repository.consumeRateLimit("operator-1", 60));
    }

    @Test
    void rejectsOnlyWhenInsertRaceAndConditionalIncrementBothFail() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("COUNT(*)"),
                org.mockito.ArgumentMatchers.eq(Integer.class), any()))
                .thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0, 0);
        CpfRuntimeControlPlaneRepository repository = new CpfRuntimeControlPlaneRepository(
                jdbc, new ObjectMapper(), () -> java.time.Instant.parse("2026-08-05T03:00:30Z"));

        assertThrows(CpfRuntimeRateLimitException.class,
                () -> repository.consumeRateLimit("operator-1", 60));
    }
    @Test
    void rateBucketUsesDatabaseClockInsteadOfJvmClock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("COUNT(*)"),
                org.mockito.ArgumentMatchers.eq(Integer.class), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        CpfRuntimeControlPlaneRepository repository = new CpfRuntimeControlPlaneRepository(
                jdbc, new ObjectMapper(), () -> java.time.Instant.parse("2026-08-05T03:07:30Z"));

        repository.consumeRateLimit("operator-1", 60);

        verify(jdbc).update(org.mockito.ArgumentMatchers.contains("cpf_runtime_rate_bucket"),
                org.mockito.ArgumentMatchers.eq("operator-1:202608050307"),
                org.mockito.ArgumentMatchers.eq(60));
    }

}
