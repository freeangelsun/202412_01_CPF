package cpf.bat.edu.ondemand;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.batch.CpfBatchExecutionResult;
import cpf.pfw.common.batch.CpfBatchLauncher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.task.SyncTaskExecutor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BatOnDemandServiceTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final CpfBatchLauncher launcher = mock(CpfBatchLauncher.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<JobExplorer> explorerProvider = mock(ObjectProvider.class);
    private final BatOnDemandService service = new BatOnDemandService(
            repository, launcher, explorerProvider, new SyncTaskExecutor(), new ObjectMapper());

    @AfterEach
    void clearContext() {
        cpf.pfw.common.logging.TransactionContext.clear();
    }

    @Test
    void 허용된표준배치ID를접수하고실행결과를연결한다() {
        when(launcher.run(any())).thenReturn(CpfBatchExecutionResult.of(
                true, BatOnDemandJobConfig.JOB_NAME, 11L, 22L,
                "COMPLETED", "완료", Map.of("writeCount", 1)));

        BatOnDemandStatus status = service.submit(new BatOnDemandRequest(
                BatOnDemandJobConfig.STANDARD_BATCH_ID, "20260716", "IDEMPOTENT-1",
                "교육 샘플 실행", "tester", Map.of("limit", 10)));

        BatOnDemandStatus completed = service.status(status.executionRequestId());
        assertThat(completed.requestStatus()).isEqualTo("COMPLETED");
        assertThat(completed.pfwExecutionId()).isEqualTo(11L);
        assertThat(completed.springBatchExecutionId()).isEqualTo(22L);
    }

    @Test
    void 같은멱등키는기존접수결과를반환한다() {
        when(launcher.run(any())).thenReturn(CpfBatchExecutionResult.of(
                true, BatOnDemandJobConfig.JOB_NAME, 1L, 2L, "COMPLETED", "완료", Map.of()));
        BatOnDemandRequest request = new BatOnDemandRequest(
                BatOnDemandJobConfig.STANDARD_BATCH_ID, "20260716", "SAME-KEY",
                "첫 접수", "tester", Map.of());

        BatOnDemandStatus first = service.submit(request);
        BatOnDemandStatus second = service.submit(request);

        assertThat(second.executionRequestId()).isEqualTo(first.executionRequestId());
    }

    @Test
    void 재시작과재수행을서로다른표준요청으로전달한다() {
        when(launcher.run(any())).thenReturn(CpfBatchExecutionResult.of(
                true, BatOnDemandJobConfig.JOB_NAME, 31L, 41L, "COMPLETED", "완료", Map.of()));
        BatOnDemandStatus submitted = service.submit(new BatOnDemandRequest(
                BatOnDemandJobConfig.STANDARD_BATCH_ID, "20260716", "RESTART-RERUN",
                "교육 샘플 실행", "tester", Map.of()));

        service.restart(submitted.executionRequestId(), "operator", "실패 지점 재개");
        service.rerun(submitted.executionRequestId(), "operator", "전체 신규 재수행");

        org.mockito.Mockito.verify(launcher).run(argThat(request ->
                request.operationType() == cpf.pfw.common.batch.CpfBatchOperationType.RESTART));
        org.mockito.Mockito.verify(launcher).run(argThat(request ->
                request.operationType() == cpf.pfw.common.batch.CpfBatchOperationType.RERUN));
    }

    private static final class MemoryRepository implements BatOnDemandRepository {
        private final Map<String, BatOnDemandStatus> byRequest = new LinkedHashMap<>();
        private final Map<String, String> requestByKey = new LinkedHashMap<>();

        @Override
        public BatOnDemandStatus createOrFind(
                BatOnDemandStatus requested, String parametersJson, String reason, String requestUser) {
            String key = requested.standardBatchId() + ':' + requested.idempotencyKey();
            String existing = requestByKey.putIfAbsent(key, requested.executionRequestId());
            if (existing != null) {
                return byRequest.get(existing);
            }
            byRequest.put(requested.executionRequestId(), requested);
            return requested;
        }

        @Override
        public Optional<BatOnDemandStatus> find(String executionRequestId) {
            return Optional.ofNullable(byRequest.get(executionRequestId));
        }

        @Override
        public void markRunning(String executionRequestId) {
            BatOnDemandStatus current = byRequest.get(executionRequestId);
            byRequest.put(executionRequestId, new BatOnDemandStatus(
                    current.executionRequestId(), current.standardBatchId(), current.idempotencyKey(),
                    current.transactionGlobalId(), current.businessDate(), "RUNNING", current.pfwExecutionId(),
                    current.springBatchExecutionId(), current.result(), current.failureCode(), current.failureMessage(),
                    current.requestedAt(), current.completedAt()));
        }

        @Override
        public void complete(String executionRequestId, String status, Long pfwExecutionId,
                             Long springExecutionId, String resultJson, String failureCode, String failureMessage) {
            BatOnDemandStatus current = byRequest.get(executionRequestId);
            byRequest.put(executionRequestId, new BatOnDemandStatus(
                    current.executionRequestId(), current.standardBatchId(), current.idempotencyKey(),
                    current.transactionGlobalId(), current.businessDate(), status, pfwExecutionId,
                    springExecutionId, Map.of("json", resultJson), failureCode, failureMessage,
                    current.requestedAt(), Instant.now()));
        }
    }
}
