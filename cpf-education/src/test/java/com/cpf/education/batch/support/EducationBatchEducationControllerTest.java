package com.cpf.education.batch.support;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.data.api.CpfDataRow;
import com.cpf.education.batch.support.controller.EducationBatchEducationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationBatchEducationControllerTest {

    @Test
    void delegatesActualRunToPublicBatchOperationsPort() {
        CpfBatchOperationsPort operations = mock(CpfBatchOperationsPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<CpfBatchOperationsPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(operations);
        when(operations.requestRun("CPF_EDU_TASKLET_JOB", "{}", "edu-user", "Tasklet Job 교육 실행"))
                .thenReturn(CpfDataRow.of("status", "ACCEPTED"));
        var controller = controller(provider);

        assertThat(controller.runTasklet("edu-user").getBody())
                .containsEntry("status", "ACCEPTED");
        verify(operations).requestRun(
                "CPF_EDU_TASKLET_JOB", "{}", "edu-user", "Tasklet Job 교육 실행");
    }

    @Test
    void refusesActualRunWhenNoLocalOrRemoteOwnerAdapterExists() {
        @SuppressWarnings("unchecked")
        ObjectProvider<CpfBatchOperationsPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        var controller = controller(provider);

        assertThatThrownBy(() -> controller.runChunk("edu-user"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void exposesPolicyAndValidatedAdmLinkWithoutBatRuntimeImports() {
        @SuppressWarnings("unchecked")
        ObjectProvider<CpfBatchOperationsPort> provider = mock(ObjectProvider.class);
        var controller = controller(provider);

        assertThat(controller.lockPolicy().getBody().owner()).isEqualTo("cpf-batch");
        assertThat(controller.checkpointRestart().getBody().retryableStates())
                .contains("UNKNOWN_RESULT");
        assertThat(controller.schedulePolicy().getBody().owner())
                .isEqualTo("cpf-batch-scheduler");
        assertThat(controller.lifecyclePolicy().getBody().reconciliation())
                .contains("대사");
        assertThat(controller.admLink(
                LocalDate.of(2026, 7, 27), "CPF_EDU_TASKLET_JOB", 11L, "batWK01").getBody())
                .containsEntry("requiredPermission", "RELIABILITY_READ");
    }

    private EducationBatchEducationController controller(
            ObjectProvider<CpfBatchOperationsPort> provider) {
        return new EducationBatchEducationController(
                provider,
                new EducationBatchPolicyEducationSample(),
                new EducationAdmBatchLogQueryEducationSample());
    }
}
