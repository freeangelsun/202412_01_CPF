package com.cpf.admin.opr.service;

import com.cpf.core.common.batch.CpfBatchLauncher;
import com.cpf.core.common.batch.CpfBatchGhostDetectionService;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "varargs"})
class AdmBatchOperationServiceTest {

    private final JdbcTemplate batJdbcTemplate = mock(JdbcTemplate.class);
    private final CpfBatchLauncher batchLauncher = mock(CpfBatchLauncher.class);
    private final ObjectProvider<JobExplorer> jobExplorerProvider = new EmptyJobExplorerProvider();
    private final CpfBatchGhostDetectionService ghostDetectionService = mock(CpfBatchGhostDetectionService.class);
    private final ObjectProvider<CpfBatchGhostDetectionService> ghostDetectionServiceProvider =
            new FixedObjectProvider<>(ghostDetectionService);
    private final AdmBatchOperationService service =
            new AdmBatchOperationService(batJdbcTemplate, batchLauncher, jobExplorerProvider, ghostDetectionServiceProvider);

    @Test
    void findWorkersUsesHeartbeatTimeoutWithSafeLowerBound() {
        // worker heartbeat 조회는 너무 작은 제한초가 들어와도 최소 30초 기준으로 stale 여부를 판단합니다.
        service.findWorkers(1);

        verify(batJdbcTemplate).queryForList(contains("FROM bat_worker"), eq(30));
    }

    @Test
    void findGhostCandidatesRunsAutoDetectionBeforeQuery() {
        service.findGhostCandidates(1);

        verify(ghostDetectionService).detectGhostCandidates(30);
        verify(batJdbcTemplate).queryForList(contains("FROM bat_execution"), eq(30), eq(30));
    }

    @Test
    void releaseLockDeletesLockAndReturnsBeforeSnapshot() {
        // lock 강제 해제는 해제 전 데이터를 먼저 읽어 운영 로그의 before_data 근거로 사용합니다.
        Map<String, Object> lock = new LinkedHashMap<>();
        lock.put("lock_key", "batch:job:CPF_EDU_TASKLET_JOB:test");
        lock.put("job_id", "CPF_EDU_TASKLET_JOB");
        when(batJdbcTemplate.queryForMap(contains("FROM bat_lock"), eq("batch:job:CPF_EDU_TASKLET_JOB:test")))
                .thenReturn(lock);
        when(batJdbcTemplate.update("DELETE FROM bat_lock WHERE lock_key = ?", "batch:job:CPF_EDU_TASKLET_JOB:test"))
                .thenReturn(1);

        Map<String, Object> result = service.releaseLock(
                "batch:job:CPF_EDU_TASKLET_JOB:test",
                "adm-operator",
                "장애 복구 lock 해제");

        assertThat(result)
                .containsEntry("lockKey", "batch:job:CPF_EDU_TASKLET_JOB:test")
                .containsEntry("released", true);
        assertThat(result.get("before")).isEqualTo(lock);
        verify(batJdbcTemplate).update(contains("INSERT INTO bat_operation_log"),
                eq("CPF_EDU_TASKLET_JOB"),
                isNull(),
                eq("LOCK_RELEASE"),
                eq("adm-operator"),
                eq("장애 복구 lock 해제"),
                anyString(),
                eq("deleted=1"),
                eq("adm-operator"),
                eq("adm-operator"));
    }

    @Test
    void actGhostRejectsUnsupportedActionBeforeDbAccess() {
        // 정의되지 않은 ghost 조치 유형은 실행 상태를 읽거나 변경하기 전에 차단합니다.
        assertThatThrownBy(() -> service.actGhostExecution(10L, "KILL_PROCESS", "adm-operator", "오입력"))
                .isInstanceOf(CpfValidationException.class);

        verifyNoInteractions(batJdbcTemplate);
    }

    @Test
    void actGhostFailUpdatesExecutionAndWritesGhostEvent() {
        // FAIL 조치는 실행을 FAILED로 정리하고 ghost_event와 batch_operation_log를 함께 남깁니다.
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("execution_id", 10L);
        before.put("job_id", "CPF_EDU_TASKLET_JOB");
        before.put("spring_batch_execution_id", 77L);
        before.put("server_instance_id", "server-1");
        before.put("worker_id", "worker-1");
        before.put("batch_instance_id", "server-1");

        Map<String, Object> after = new LinkedHashMap<>(before);
        after.put("execution_status", "FAILED");

        when(batJdbcTemplate.queryForMap(contains("FROM bat_execution"), eq(10L)))
                .thenReturn(before, after);

        Map<String, Object> result = service.actGhostExecution(
                10L,
                "FAIL",
                "adm-operator",
                "heartbeat 장기 미수신으로 실패 처리");

        assertThat(result)
                .containsEntry("actionType", "FAIL")
                .containsEntry("execution", after);
        verify(batJdbcTemplate).update(contains("UPDATE bat_execution"),
                eq("adm-operator"),
                eq(10L));
        verify(batJdbcTemplate).update(contains("INSERT INTO bat_ghost_event"),
                eq(10L),
                eq(77L),
                eq("CPF_EDU_TASKLET_JOB"),
                eq("server-1"),
                eq("worker-1"),
                contains("ADM에서 ghost 후보를 조치했습니다."),
                eq("FAIL"),
                eq("heartbeat 장기 미수신으로 실패 처리"),
                eq("adm-operator"),
                eq("N"),
                eq("N"),
                anyString(),
                anyString(),
                eq("adm-operator"),
                eq("adm-operator"));
    }

    private static final class EmptyJobExplorerProvider implements ObjectProvider<JobExplorer> {
        @Override
        public JobExplorer getObject(Object... args) {
            return null;
        }

        @Override
        public JobExplorer getObject() {
            return null;
        }

        @Override
        public JobExplorer getIfAvailable() {
            return null;
        }

        @Override
        public JobExplorer getIfUnique() {
            return null;
        }

        @Override
        public Iterator<JobExplorer> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public Stream<JobExplorer> stream() {
            return Stream.empty();
        }

        @Override
        public Stream<JobExplorer> orderedStream() {
            return Stream.empty();
        }
    }

    private record FixedObjectProvider<T>(T value) implements ObjectProvider<T> {
        @Override
        public T getObject(Object... args) {
            return value;
        }

        @Override
        public T getObject() {
            return value;
        }

        @Override
        public T getIfAvailable() {
            return value;
        }

        @Override
        public T getIfUnique() {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            return value == null ? Collections.emptyIterator() : Collections.singleton(value).iterator();
        }

        @Override
        public Stream<T> stream() {
            return value == null ? Stream.empty() : Stream.of(value);
        }

        @Override
        public Stream<T> orderedStream() {
            return stream();
        }
    }
}
