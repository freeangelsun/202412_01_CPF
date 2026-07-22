package cpf.bat.operation;

import cpf.pfw.common.batch.CpfBatchExecutionRequest;
import cpf.pfw.common.batch.CpfBatchExecutionResult;
import cpf.pfw.common.batch.CpfBatchLauncher;
import cpf.pfw.common.batch.CpfBatchOperationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatSmokeOperationServiceTest {

    @Test
    void runUsesPfwBatchLauncherAndLoadsExecutionDetail() {
        CpfBatchLauncher launcher = mock(CpfBatchLauncher.class);
        CpfBatchOperationRepository repository = mock(CpfBatchOperationRepository.class);
        BatSmokeOperationService service = new BatSmokeOperationService(launcher, repository);

        when(launcher.run(any(CpfBatchExecutionRequest.class)))
                .thenReturn(CpfBatchExecutionResult.of(
                        true,
                        "CPF_BAT_SMOKE_JOB",
                        10L,
                        20L,
                        "COMPLETED",
                        "done",
                        Map.of()));
        when(repository.available()).thenReturn(true);
        when(repository.findExecutionDetail(10L)).thenReturn(Map.of(
                "execution",
                Map.of("transaction_global_id", "20260622120000000BATbatWK010000001"),
                "steps",
                java.util.List.of(Map.of("spring_batch_step_execution_id", 30L))));

        Map<String, Object> response = service.run("CPF_BAT_SMOKE_JOB", "테스트 실행");

        ArgumentCaptor<CpfBatchExecutionRequest> captor = ArgumentCaptor.forClass(CpfBatchExecutionRequest.class);
        verify(launcher).run(captor.capture());
        assertThat(captor.getValue().jobId()).isEqualTo("CPF_BAT_SMOKE_JOB");
        assertThat(captor.getValue().requestUser()).isEqualTo("BAT_SMOKE");
        assertThat(response)
                .containsEntry("pfwExecutionId", 10L)
                .containsEntry("springBatchExecutionId", 20L)
                .containsEntry("status", "COMPLETED");
        assertThat(response.get("detail")).isInstanceOf(Map.class);
    }
}
